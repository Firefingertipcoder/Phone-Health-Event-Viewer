package com.example.monitors

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BackgroundApp(
    val packageName: String,
    val appName: String,
    val cpuUsage: Double,   // %
    val ramUsageMb: Double, // MB
    val isSystem: Boolean,
    val riskStatus: String, // "High Resource", "Suspicious", "Safe", "Optimized"
    val isRunning: Boolean = true
)

class BackgroundAppManager {
    private val _apps = MutableStateFlow<List<BackgroundApp>>(emptyList())
    val apps: StateFlow<List<BackgroundApp>> = _apps.asStateFlow()

    init {
        resetAppList()
    }

    fun resetAppList() {
        _apps.value = listOf(
            BackgroundApp("com.android.systemui", "System UI Runtime", 0.8, 142.0, true, "Safe"),
            BackgroundApp("com.google.android.gms", "Google Play Services", 1.5, 210.0, true, "Safe"),
            BackgroundApp("com.unauthorized.agentx", "Hidden Daemon Thread (Anomalous)", 28.5, 310.0, false, "Suspicious"),
            BackgroundApp("com.social.appconnect", "Ambient Social Hub Service", 4.1, 185.0, false, "Safe"),
            BackgroundApp("com.gametry.resourcehog", "Idle Heavy Physics Asset", 19.8, 420.0, false, "High Resource"),
            BackgroundApp("com.android.chrome.isolated", "Chrome Sandbox Worker", 0.2, 280.0, true, "Safe"),
            BackgroundApp("com.analytics.backgroudlog", "Unsigned Telemetry Loop", 14.5, 95.0, false, "Suspicious"),
            BackgroundApp("com.secure.shield", "Auth Cryptographic Service", 0.1, 38.0, true, "Safe"),
            BackgroundApp("com.videofilter.backgroundimg", "Background Render Task", 11.2, 160.0, false, "High Resource")
        )
    }

    /**
     * Terminates a single process, freeing its resource foot-print.
     */
    fun terminateApp(packageName: String) {
        _apps.value = _apps.value.filter { it.packageName != packageName }
    }

    /**
     * Terminates high risk high consumption apps, reclaiming CPU/RAM metrics instantly.
     */
    fun optimizeAll(): Int {
        var optimizedCount = 0
        _apps.value = _apps.value.map { app ->
            if (!app.isSystem && (app.riskStatus == "High Resource" || app.riskStatus == "Suspicious") && app.isRunning) {
                optimizedCount++
                app.copy(
                    cpuUsage = 0.0,
                    ramUsageMb = 0.0,
                    riskStatus = "Optimized",
                    isRunning = false
                )
            } else {
                app
            }
        }
        return optimizedCount
    }
}
