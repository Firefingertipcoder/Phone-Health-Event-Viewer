package com.example

import android.util.Log

object NativeMonitor {
    private const val TAG = "NativeMonitor"
    var isLibLoaded = false
        private set

    init {
        try {
            System.loadLibrary("native-lib")
            isLibLoaded = true
            Log.d(TAG, "Successfully loaded native-lib C++ library via NDK")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native-lib: ${e.message}. Using high-performance Kotlin fallback algorithms.")
            isLibLoaded = false
        }
    }

    // Native method signatures
    private external fun getCpuArchitecture(): String
    private external fun calculateHealthIndex(
        batteryTemp: Double,
        batteryLevel: Double,
        storageFreePct: Double,
        ramFreePct: Double
    ): Double

    private external fun estimateResourceScore(
        cpuUsage: Double,
        memoryUsage: Double
    ): Int

    /**
     * Get Compiled CPU architecture via NDK macro inspection.
     */
    fun getCpuArch(): String {
        return if (isLibLoaded) {
            try {
                getCpuArchitecture()
            } catch (e: UnsatisfiedLinkError) {
                getCpuArchKotlinFallback()
            }
        } else {
            getCpuArchKotlinFallback()
        }
    }

    /**
     * Evaluates total condition score (0 to 100) using the optimized NDK formula.
     */
    fun getHealthIndex(
        batteryTemp: Double,
        batteryLevel: Double,
        storageFreePct: Double,
        ramFreePct: Double
    ): Double {
        return if (isLibLoaded) {
            try {
                calculateHealthIndex(batteryTemp, batteryLevel, storageFreePct, ramFreePct)
            } catch (e: UnsatisfiedLinkError) {
                calculateHealthIndexKotlinFallback(batteryTemp, batteryLevel, storageFreePct, ramFreePct)
            }
        } else {
            calculateHealthIndexKotlinFallback(batteryTemp, batteryLevel, storageFreePct, ramFreePct)
        }
    }

    /**
     * Estimates resource pressure indicators (0 to 100) using NDK matrix factors.
     */
    fun getResourceScore(cpuUsage: Double, memoryUsage: Double): Int {
        return if (isLibLoaded) {
            try {
                estimateResourceScore(cpuUsage, memoryUsage)
            } catch (e: UnsatisfiedLinkError) {
                estimateResourceScoreKotlinFallback(cpuUsage, memoryUsage)
            }
        } else {
            estimateResourceScoreKotlinFallback(cpuUsage, memoryUsage)
        }
    }

    // --- Safety Kotlin Fallbacks to prevent crash and preserve 100% functionality ---

    private fun getCpuArchKotlinFallback(): String {
        val arch = System.getProperty("os.arch") ?: "aarch64"
        return "$arch (Kotlin JRE)"
    }

    private fun calculateHealthIndexKotlinFallback(
        batteryTemp: Double,
        batteryLevel: Double,
        storageFreePct: Double,
        ramFreePct: Double
    ): Double {
        var score = 100.0
        // Thermal Penalty
        if (batteryTemp > 35.0) {
            val excess = batteryTemp - 35.0
            score -= (excess * excess * 1.5)
        }
        // Low Battery Impact
        if (batteryLevel < 20.0) {
            score -= (20.0 - batteryLevel) * 0.75
        }
        // Storage Pressure Impact
        if (storageFreePct < 15.0) {
            score -= (15.0 - storageFreePct) * 1.2
        }
        // RAM Exhaustion Impact
        if (ramFreePct < 10.0) {
            score -= (10.0 - ramFreePct) * 1.5
        }

        if (score > 100.0) score = 100.0
        if (score < 0.0) score = 0.0
        return score
    }

    private fun estimateResourceScoreKotlinFallback(cpuUsage: Double, memoryUsage: Double): Int {
        var pressure = (cpuUsage * 0.5) + (memoryUsage * 0.5)
        if (cpuUsage > 80.0 && memoryUsage > 80.0) {
            pressure += 15.0
        }
        var result = pressure.toInt()
        if (result > 100) result = 100
        if (result < 0) result = 0
        return result
    }
}
