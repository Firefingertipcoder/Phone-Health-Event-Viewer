#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "NativeMonitor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_example_NativeMonitor_getCpuArchitecture(JNIEnv *env, jobject thiz) {
#if defined(__arm__)
    return env->NewStringUTF("ARMv7 (32-bit)");
#elif defined(__aarch64__)
    return env->NewStringUTF("ARM64 (64-bit)");
#elif defined(__i386__)
    return env->NewStringUTF("x86 (32-bit)");
#elif defined(__x86_64__)
    return env->NewStringUTF("x86_64 (64-bit)");
#else
    return env->NewStringUTF("Unknown Architecture");
#endif
}

JNIEXPORT jdouble JNICALL
Java_com_example_NativeMonitor_calculateHealthIndex(
        JNIEnv *env,
        jobject thiz,
        jdouble battery_temp,          // Celsius, e.g. 35.0
        jdouble battery_level,         // Percentage 0-100
        jdouble storage_free_percentage, // Percentage 0-100
        jdouble ram_free_percentage     // Percentage 0-100
) {
    LOGD("calculateHealthIndex entered: temp=%f, level=%f, storage=%f, ram=%f",
         battery_temp, battery_level, storage_free_percentage, ram_free_percentage);

    // Baseline score starts at 100.0
    double score = 100.0;

    // 1. Thermal Penalty: Standard safe temp is below 38C. Thermal throttling begins around 42C-45C.
    // Penalty is non-linear (quadratic) beyond threshold.
    if (battery_temp > 35.0) {
        double excess = battery_temp - 35.0;
        score -= (excess * excess * 1.5); // Penalty increases quickly as temperature climbs
    }

    // 2. Battery Impact: Low battery drains condition
    if (battery_level < 20.0) {
        score -= (20.0 - battery_level) * 0.75;
    }

    // 3. Storage Penalty: If storage free is less than 15%, start applying significant penalties to system health
    if (storage_free_percentage < 15.0) {
        score -= (15.0 - storage_free_percentage) * 1.2;
    }

    // 4. Memory Penalty: Out of memory conditions affect system responsiveness
    if (ram_free_percentage < 10.0) {
        score -= (10.0 - ram_free_percentage) * 1.5;
    }

    // Wrap score, ensuring it remains within JNI safe values between 0 and 100
    if (score > 100.0) score = 100.0;
    if (score < 0.0) score = 0.0;

    LOGD("calculateHealthIndex calculated score: %f", score);
    return (jdouble) score;
}

JNIEXPORT jint JNICALL
Java_com_example_NativeMonitor_estimateResourceScore(
        JNIEnv *env,
        jobject thiz,
        jdouble cpu_usage,      // Percentage 0-100
        jdouble memory_usage    // Percentage 0-100
) {
    // Estimating standard deviation or resource pressure
    // Lower score is better (represents lower pressure).
    // Values close to 100 indicate critical resources exhaustion.
    double pressure = (cpu_usage * 0.5) + (memory_usage * 0.5);

    // Apply multiplier if both are highly loaded (interactive resource consumption)
    if (cpu_usage > 80.0 && memory_usage > 80.0) {
        pressure += 15.0;
    }

    int result = (int) pressure;
    if (result > 100) result = 100;
    if (result < 0) result = 0;

    return (jint) result;
}

}
