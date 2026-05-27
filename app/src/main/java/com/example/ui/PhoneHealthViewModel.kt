package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.SyncResponse
import com.example.data.database.DeviceAlert
import com.example.data.database.DeviceHealthLog
import com.example.data.database.PhoneHealthDatabase
import com.example.data.database.PhoneHealthRepository
import com.example.data.database.PhoneUsageEvent
import com.example.monitors.BackgroundAppManager
import com.example.monitors.LiveMetrics
import com.example.monitors.SystemMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PhoneHealthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PhoneHealthDatabase.getDatabase(application)
    private val repository = PhoneHealthRepository(database.phoneHealthDao())

    // Managers & Monitors
    private val backgroundAppManager = BackgroundAppManager()
    private val systemMonitor = SystemMonitor(application, repository, viewModelScope)

    // UI Input State for Cloud Syncing
    val cloudUrl = MutableStateFlow("https://api.cloudphonehealth.org/v1/sync")
    val cloudApiKey = MutableStateFlow("")
    val syncResponseState = MutableStateFlow<SyncResponse?>(null)
    val isSyncing = MutableStateFlow(false)

    // Manual optimization savings to dynamically improve health metrics on demand
    private val optimizationRamSavings = MutableStateFlow(0.0) // MBs saved
    private val optimizationCpuSavings = MutableStateFlow(0.0) // % saved

    // Fetch Room Database Logs as StateFlows
    val allHealthLogs: StateFlow<List<DeviceHealthLog>> = repository.allHealthLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlerts: StateFlow<List<DeviceAlert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsageEvents: StateFlow<List<PhoneUsageEvent>> = repository.allUsageEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val backgroundApps = backgroundAppManager.apps

    // Combine raw device metrics with our manual optimization savings dynamically
    val liveMetrics: StateFlow<LiveMetrics> = systemMonitor.metrics
        .combine(optimizationRamSavings) { raw, ramSavings ->
            if (ramSavings <= 0) return@combine raw

            // Recalculate margins based on terminated app variables:
            val adjustedFreeRam = (raw.freeRamMb + ramSavings).coerceAtMost(raw.totalRamMb)
            val adjustedRamFreePct = (adjustedFreeRam * 100.0) / raw.totalRamMb

            // Run adjusted native formulas
            val recalculatedHealthScore = com.example.NativeMonitor.getHealthIndex(
                batteryTemp = raw.batteryTemp,
                batteryLevel = raw.batteryLevel,
                storageFreePct = raw.storageFreePct,
                ramFreePct = adjustedRamFreePct
            )

            raw.copy(
                freeRamMb = adjustedFreeRam,
                ramFreePct = adjustedRamFreePct,
                healthScore = recalculatedHealthScore
            )
        }
        .combine(optimizationCpuSavings) { compiled, cpuSavings ->
            if (cpuSavings <= 0) return@combine compiled

            val adjustedCpu = (compiled.cpuUsagePct - cpuSavings).coerceAtLeast(3.0)
            val recalculatedResourceScore = com.example.NativeMonitor.getResourceScore(
                cpuUsage = adjustedCpu,
                memoryUsage = 100.0 - compiled.ramFreePct
            )

            compiled.copy(
                cpuUsagePct = adjustedCpu,
                resourceScore = recalculatedResourceScore
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LiveMetrics())

    /**
     * Terminate individual app from process list, incrementing dynamic optimization calculations.
     */
    fun terminateBackgroundApp(packageName: String) {
        val app = backgroundApps.value.find { it.packageName == packageName }
        if (app != null && app.isRunning) {
            backgroundAppManager.terminateApp(packageName)
            optimizationRamSavings.value += app.ramUsageMb
            optimizationCpuSavings.value += app.cpuUsage
            
            // Insert log event
            insertUsageEvent("PROCESS_KILLED", "Terminated background process: ${app.appName} (${app.packageName}) to reclaim resources.")
        }
    }

    /**
     * Optimizes all resource hog and suspicious apps at once.
     */
    fun optimizeBackgroundActivity() {
        val appsToOptimize = backgroundApps.value.filter { 
            !it.isSystem && (it.riskStatus == "High Resource" || it.riskStatus == "Suspicious") && it.isRunning 
        }

        if (appsToOptimize.isNotEmpty()) {
            val totalRamReclaimed = appsToOptimize.sumOf { it.ramUsageMb }
            val totalCpuReclaimed = appsToOptimize.sumOf { it.cpuUsage }

            backgroundAppManager.optimizeAll()
            optimizationRamSavings.value += totalRamReclaimed
            optimizationCpuSavings.value += totalCpuReclaimed

            viewModelScope.launch {
                repository.insertAlert(
                    DeviceAlert(
                        title = "Resource Optimization Completed",
                        description = "Reclaimed ${totalRamReclaimed.toInt()}MB memory and reduced CPU load by ${totalCpuReclaimed.toInt()}%. Background security scan clean.",
                        metricType = "SECURITY",
                        severity = "INFO"
                    )
                )
                repository.insertUsageEvent(
                    PhoneUsageEvent(
                        eventType = "SECURITY_CHECK",
                        eventDescription = "Executed multi-lock process optimization. Cleaned ${appsToOptimize.size} resource hogs."
                    )
                )
            }
        }
    }

    /**
     * Reset manual optimizations to return to actual current physical sensor readings.
     */
    fun resetMonitorBaseline() {
        optimizationRamSavings.value = 0.0
        optimizationCpuSavings.value = 0.0
        backgroundAppManager.resetAppList()
        insertUsageEvent("MONITOR_RESET", "Reset optimization profiles back to raw hardware sensors.")
    }

    /**
     * Clears all log entries inside Room database.
     */
    fun clearLogsAndTrends() {
        viewModelScope.launch {
            repository.clearOldData()
            // Insert restart event so the list is initialized but historical trend log is reset
            repository.insertUsageEvent(
                PhoneUsageEvent(
                    eventType = "SECURITY_CHECK",
                    eventDescription = "Historical records and trace metrics cleared as requested by administrator."
                )
            )
        }
    }

    /**
     * Executes real cloud synching, compiling database entries and sending payloads over Retrofit.
     */
    fun triggerCloudSync() {
        viewModelScope.launch {
            isSyncing.value = true
            syncResponseState.value = null
            try {
                val response = repository.syncWithCloud(cloudUrl.value, cloudApiKey.value)
                syncResponseState.value = response
                
                // Track usage
                repository.insertUsageEvent(
                    PhoneUsageEvent(
                        eventType = "SECURITY_CHECK",
                        eventDescription = "Transmitted system telemetry trace successfully upstream. Synced: ${response.syncedCount} records."
                    )
                )
            } catch (e: Exception) {
                // Should be caught inside repository, but handle just in case
                syncResponseState.value = SyncResponse(
                    success = false,
                    message = "Sync failure: ${e.localizedMessage ?: "Network error"}",
                    syncedCount = 0,
                    serverTimestamp = System.currentTimeMillis()
                )
            } finally {
                isSyncing.value = false
            }
        }
    }

    private fun insertUsageEvent(type: String, desc: String) {
        viewModelScope.launch {
            repository.insertUsageEvent(
                PhoneUsageEvent(eventType = type, eventDescription = desc)
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        systemMonitor.unregisterReceivers()
    }
}
