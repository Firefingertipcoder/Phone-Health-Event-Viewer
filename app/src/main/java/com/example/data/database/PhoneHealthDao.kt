package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhoneHealthDao {

    // --- Health Logs ---
    @Query("SELECT * FROM health_logs ORDER BY timestamp DESC")
    fun getAllHealthLogs(): Flow<List<DeviceHealthLog>>

    @Query("SELECT * FROM health_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHealthLogs(limit: Int): Flow<List<DeviceHealthLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthLog(log: DeviceHealthLog)

    @Query("DELETE FROM health_logs")
    suspend fun clearHealthLogs()

    // --- Alerts ---
    @Query("SELECT * FROM device_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<DeviceAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: DeviceAlert)

    @Delete
    suspend fun deleteAlert(alert: DeviceAlert)

    @Query("DELETE FROM device_alerts")
    suspend fun clearAlerts()

    // --- Usage Events ---
    @Query("SELECT * FROM usage_events ORDER BY timestamp DESC")
    fun getAllUsageEvents(): Flow<List<PhoneUsageEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageEvent(event: PhoneUsageEvent)

    @Query("DELETE FROM usage_events")
    suspend fun clearUsageEvents()
}
