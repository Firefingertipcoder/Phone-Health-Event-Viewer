# SYSTEM CORE - NDK PHONE HEALTH ENGINE & TELEMETRY DASHBOARD
### Official Application Documentation & Technical Reference

Welcome to the official developer documentation for the **System Core (NDK Phone Health Monitor)** application. This document details the application's overall design, underlying architecture layers, mathematical formulas, and instructions for testing the compiled binary artifact on a physical device.

---

## 1. TECHNICAL ARCHITECTURE

The application implements a modern, offline-first MVVM architecture using **Jetpack Compose (Material M3)** for UI rendering, combined with a **C++ NDK Library** for low-level resource analysis and a **SQLite Room Database** for historical monitoring.

```
+----------------------------------------------------------------------------+
|                          JETPACK COMPOSE COMPOSABLES                       |
|   [Dynamic Gauge Rails]   [HUD Top Alerts]   [Process Process Monitors]    |
+-------------------------------------+--------------------------------------+
                                      | (Subscribes to StateFlows)
                                      v
+----------------------------------------------------------------------------+
|                     PHONE HEALTH STATE VIEW MODEL (MVVM)                   |
|   - Combines Hardware Metrics with manual RAM/CPU Optimizer savings         |
|   - Controls room queries, cloud background Retrofit, and thread states  |
+-------------------------------------+--------------------------------------+
                                      | (Data & Sync Tasks)
                                      v
+-------------------------------------+--------------------------------------+
|                     NATIVE LOGIC MONITOR INTERFACE (NDK)                   |
|   - Loads binary JNI library 'native-lib' compiled in modern C++            |
|   - System Fallback: High-precision native Kotlin algorithms               |
+------------------+----------------------------------+----------------------+
                   |                                  |
                   v (Offline Records)                v (Upstream Trace)
+--------------------------------------+   +---------------------------------+
|        SQLITE ROOM DATABASE LAYER    |   |     RETROFIT CLOUD SYNCHRONIZER |
| [HealthLogs] [Alerts] [UsageEvents]  |   |     Transmits Telemetry packets |
+--------------------------------------+   +---------------------------------+
```

---

## 2. COMPONENT DETAILS

### A. NDK Native Monitor Engine (`NativeMonitor.kt`)
Evaluates physical resources directly using a high-density C++ JNI compiled binary:
1. **CPU Architecture query**: Reads target platform JNI compiler flags directly.
2. **Dynamic Math Scoring Formulas**:
   * **Thermal Stress Penalty**: Activates once battery temperature crosses 35°C. The penalty factor increases quadratically to alert users of device degradation:  
     Penalty = (Temperature - 35.0)^2 * 1.5
   * **Power Strain Penalty**: Applied when charge is low (< 20%):  
     Penalty = (20.0 - BatteryLevel) * 0.75
   * **Storage Stress Penalty**: Triggers when remaining device storage space drops below 15%:  
     Penalty = (15.0 - StorageFreePct) * 1.2
   * **RAM Exhaustion Penalty**: Strongest deduction, applied below 10% available memory:  
     Penalty = (10.0 - RamFreePct) * 1.5

If the low-level C++ binary library cannot be registered, the system smoothly falls back to native high-performance Kotlin equivalent routines to guarantee 100% operation.

### B. Reactive MVVM View Engine (`PhoneHealthViewModel.kt`)
* Manages UI state inputs completely as robust Kotlin StateFlow streams.
* Merges absolute device telemetry with virtual savings accumulated on terminating suspected background resource hogs.
* Schedules concurrent async Coroutines for database actions and Retrofit REST API cloud posts.

### C. Polished Material Design 3 UI Layer (`PhoneHealthDashboard.kt`)
* Styled around rich dynamic Material Design 3 container curves (28.dp corner radius).
* Features specialized M3 component designs representing live system alerts, warning banners, real-time power gauges, RAM pressure monitoring grids, and diagnostic logs.

---

## 3. THRESHOLDS & PENALTIES MATRIX

| Metric Inspected | Healthy Limit | Calculation Penalty Rate | Risk Type |
| :--- | :---: | :---: | :---: |
| **Battery Temp** | <= 35.0°C | Quadratic excess score impact * 1.5 | Thermal (Critical) |
| **Charge Level** | >= 20.0% | Linear deduction (20.0 - Level) * 0.75 | Power (Warning) |
| **Storage Space** | >= 15.0% | Linear deduction (15.0 - FreePct) * 1.2 | Disk (Info) |
| **Memory Buffer** | >= 10.0% | Linear deduction (10.0 - FreePct) * 1.5 | RAM (Critical) |

---

## 4. HOW TO INSTALL THE APK ON YOUR DEVICES

To transfer and try the newly generated app directly onto your physical Android phone, follow these step-by-step instructions:

1. **Download the Compiled APK**: Use the file browser in the editor sidebar to find and download `/app/build/outputs/apk/debug/app-debug.apk`.
2. **Transfer to Mobile Device**: Copy the file to your telephone via a USB connection, Bluetooth, or any secure cloud drive.
3. **Open through File Manager**: Locate the copied `app-debug.apk` inside your phone's default Downloads of Files application and select it to install.
4. **Enable Installation Permissions**: If your device blocks the setup, go to System Settings and tap *Allow Installs from Unknown Sources* or *Install Unknown Apps* for that particular file manager.
5. **Execute 'System Core'**: The app will launch as "System Core" on your screen. You can run immediate memory cleanups, execute remote cloud sync reports, and monitor threshold alerts happily.

---

## 5. RELEASING YOUR APP FREELY WITHOUT COST

While publishing to the official Google Play Console requires a one-time USD 25 registration fee, you can distribute and host your application completely at zero cost using these alternative hosting channels:

1. **GitHub Releases (Recommended)**
   * **Cost**: Free
   * **Method**: Create a GitHub Repository, upload your source files, and attach your compiled Android Package (`app-debug.apk`) as an official Release Binary. Users can immediately download your app without any cost.
2. **F-Droid Application Store**
   * **Cost**: Free
   * **Method**: F-Droid provides a fully compiled catalogue dedicated purely to Free and Open Source Software (FOSS). Simply submit a build request with your GitHub Repository link, and their automated pipelines will compile and host your releases securely.
3. **Amazon Appstore**
   * **Cost**: Free
   * **Method**: Register free-of-charge as an Amazon Developer. You can submit your compiled APK file to distribute it to standard Android devices and Amazon Fire/Kindle tablet ecosystems.
4. **Direct Web APK Distribution / Alternative Markets**
   * **Cost**: Free
   * **Method**: Register free developer profiles on Alternative Markets like SlideME or APKPure, or simply distribute the APK from your private website.

---

## 6. COMPLETE STEP-BY-STEP GITHUB UPLOAD GUIDE

Follow these complete steps to backup, upload, and track this entire responsive Jetpack Compose + C++ NDK project on your personal GitHub Account:

### Step 1: Export Project ZIP from AI Studio
1. Look at the upper right section of your AI Studio interface (near the project builder header).
2. Click the **Settings** gear icon / menu button.
3. Choose **Export project** or **Export as ZIP** from the list.
4. A compressed archive containing your entire workspace directory will download to your computer.

### Step 2: Unpack the ZIP File
1. Locate the downloaded ZIP file on your computer.
2. Extract the archive into a dedicated project folder (e.g., `C:\AndroidProjects\SystemCore\`).

### Step 3: Create a New Repository on GitHub
1. Navigate to [GitHub.com](https://github.com/) and sign in.
2. Click the green **New** button (or click the **+** dropdown and pick *New Repository*).
3. Enter `SystemCore-PhoneHealth` as your Repository Name.
4. Select either **Public** (visible to all) or **Private** (secured to you).
5. **IMPORTANT**: Do *not* check options for adding a README, `.gitignore`, or licence. Leaving them unchecked avoids conflicts. Click **Create repository**.

### Step 4: Run Git Commands in Terminal
Open your terminal (macOS/Linux Terminal or Windows Git Bash / Command Prompt) and set the path:

```bash
# 1. Access the unpacked project directory
cd "C:\AndroidProjects\SystemCore\"

# 2. Setup your local Git repository context
git init

# 3. Add every project file
git add .

# 4. Commit files with a description
git commit -m "Initial commit of full Jetpack Compose NDK Phone Health App"
```

### Step 5: Push Local Branch to GitHub
Configure the paths using commands copied from your GitHub creation panel (replace the remote address with your exact GitHub link):

```bash
# 1. Designation of default branch as main
git branch -M main

# 2. Associated remote link URL
git remote add origin https://github.com/YOUR_ACCOUNT_NAME/SystemCore-PhoneHealth.git

# 3. Execute transmission to the cloud
git push -u origin main
```

Your GitHub browser tab will refresh with the uploaded source tree immediately upon completion!

---
*Documentation compiled dynamically by AIS Code Engine.*