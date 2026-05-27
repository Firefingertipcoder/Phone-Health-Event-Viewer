package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_logs")
data class DeviceHealthLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batteryLevel: Double,
    val batteryTemp: Double,
    val storageFreePct: Double,
    val ramFreePct: Double,
    val healthScore: Double,
    val resourceScore: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "device_alerts")
data class DeviceAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val metricType: String, // BATTERY, THERMAL, STORAGE, RAM, SECURITY
    val severity: String,   // INFO, WARNING, CRITICAL
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "usage_events")
data class PhoneUsageEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,       // SCREEN_ON, SCREEN_OFF, BOOT_COMPLETED, CHARGER_CONNECTED, CHARGER_DISCONNECTED, SECURITY_CHECK
    val eventDescription: String,
    val timestamp: Long = System.currentTimeMillis()
)
