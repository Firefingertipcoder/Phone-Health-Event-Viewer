package com.example.data.api

import com.example.data.database.DeviceHealthLog
import com.example.data.database.DeviceAlert
import com.example.data.database.PhoneUsageEvent
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

@JsonClass(generateAdapter = true)
data class SyncPayload(
    val deviceId: String,
    val logs: List<DeviceHealthLog>,
    val alerts: List<DeviceAlert>,
    val events: List<PhoneUsageEvent>,
    val clientTime: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class SyncResponse(
    val success: Boolean,
    val message: String,
    val syncedCount: Int,
    val serverTimestamp: Long
)

interface CloudSyncApi {
    @POST("api/sync/device-health")
    suspend fun syncDeviceHealth(
        @Header("Authorization") apiKey: String,
        @Body payload: SyncPayload
    ): SyncResponse
}
