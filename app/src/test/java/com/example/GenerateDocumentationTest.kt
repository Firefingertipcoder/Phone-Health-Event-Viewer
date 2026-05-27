package com.example

import com.lowagie.text.*
import com.lowagie.text.pdf.*
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.awt.Color

class GenerateDocumentationTest {

    @Test
    fun generateAllDocumentation() {
        val markdownContent = buildMarkdownContent()
        
        // Write Markdown document to various potential root paths
        val mdPaths = listOf(
            File("DOCUMENTATION.md"),
            File("../DOCUMENTATION.md"),
            File("src/main/assets/DOCUMENTATION.md")
        )
        for (path in mdPaths) {
            try {
                path.parentFile?.mkdirs()
                path.writeText(markdownContent)
                println("Successfully wrote markdown documentation to: ${path.absolutePath}")
            } catch (e: Exception) {
                println("Failed to write md to ${path.name}: ${e.message}")
            }
        }

        // Write PDF document to various potential root paths
        val pdfPaths = listOf(
            File("PhoneHealthDashboard_Documentation.pdf"),
            File("../PhoneHealthDashboard_Documentation.pdf")
        )
        for (path in pdfPaths) {
            try {
                path.parentFile?.mkdirs()
                generatePdf(path)
                println("Successfully generated PDF documentation at: ${path.absolutePath}")
            } catch (e: Exception) {
                println("Failed to write PDF to ${path.name}: ${e.message}")
            }
        }
    }

    private fun generatePdf(file: File) {
        val document = Document(PageSize.A4, 36f, 36f, 54f, 54f)
        val writer = PdfWriter.getInstance(document, FileOutputStream(file))
        
        // Set standard metadata
        document.addTitle("System Core (NDK Phone Health Dashboard) Documentation")
        document.addAuthor("AI Studio Coding Agent")
        document.addSubject("Technical Explanation & Architecture Diagram")

        document.open()

        // Helper fonts
        val titleFont = Font(Font.HELVETICA, 24f, Font.BOLD, Color(0, 102, 204))
        val subtitleFont = Font(Font.HELVETICA, 12f, Font.ITALIC, Color(102, 102, 102))
        val h1Font = Font(Font.HELVETICA, 16f, Font.BOLD, Color(0, 51, 102))
        val h2Font = Font(Font.HELVETICA, 12f, Font.BOLD, Color(51, 102, 153))
        val bodyFont = Font(Font.HELVETICA, 10f, Font.NORMAL, Color(51, 51, 51))
        val boldBodyFont = Font(Font.HELVETICA, 10f, Font.BOLD, Color(51, 51, 51))
        val codeFont = Font(Font.COURIER, 9f, Font.NORMAL, Color(0, 102, 0))
        val noteFont = Font(Font.HELVETICA, 9.5f, Font.ITALIC, Color(153, 51, 51))

        // Document Title
        val titlePara = Paragraph("SYSTEM CORE ENGINE", titleFont).apply {
            alignment = Element.ALIGN_CENTER
            spacingAfter = 4f
        }
        document.add(titlePara)

        val subtitlePara = Paragraph("NDK Phone Health Dashboard & Telemetry System", subtitleFont).apply {
            alignment = Element.ALIGN_CENTER
            spacingAfter = 20f
        }
        document.add(subtitlePara)

        // Divider
        val line = Paragraph("______________________________________________________________________________", 
            Font(Font.HELVETICA, 10f, Font.NORMAL, Color(200, 200, 200))).apply {
            spacingAfter = 20f
        }
        document.add(line)

        // Executive Summary
        document.add(Paragraph("EXECUTIVE SUMMARY", h1Font).apply { spacingAfter = 8f })
        document.add(Paragraph(
            "This document provides the full system documentation and architecture reference for the System Core Phone Health Dashboard application. " +
            "The system is built as a high-performance, edge-to-edge Android interface styled around Material Design 3 (M3). It is powered by a dual-layered computation paradigm: " +
            "an NDK C++ native library logic layer providing matrix diagnostics and high-performance sensor metric compilation, with a safe, responsive Kotlin fallback algorithm.",
            bodyFont
        ).apply { spacingAfter = 14f })

        // 1. Architecture Flow Representation
        document.add(Paragraph("1. ARCHITECTURE AND WORKFLOW FLOWCHART", h1Font).apply { spacingAfter = 8f })
        
        // Let's draw an elegant ASCII-based flow inside a Table to make it look like a schema
        val flowTable = PdfPTable(1).apply {
            widthPercentage = 100f
            setSpacingAfter(14f)
        }
        val flowText = 
            "+----------------------------------------------------------------------------+\n" +
            "|                          JETPACK COMPOSE COMPOSABLES                       |\n" +
            "|   [Dynamic Gauge Rails]   [HUD Top Alerts]   [Process Process Monitors]    |\n" +
            "+-------------------------------------+--------------------------------------+\n" +
            "                                      | (Subscribes to StateFlows)\n" +
            "                                      v\n" +
            "+----------------------------------------------------------------------------+\n" +
            "|                     PHONE HEALTH STATE VIEW MODEL (MVVM)                   |\n" +
            "|   - Combines Hardware Metrics with manual RAM/CPU Optimizer savings         |\n" +
            "|   - Controls room queries, cloud background Retrofit, and thread states  |\n" +
            "+-------------------------------------+--------------------------------------+\n" +
            "                                      | (Data & Sync Tasks)\n" +
            "                                      v\n" +
            "+-------------------------------------+--------------------------------------+\n" +
            "|                     NATIVE LOGIC MONITOR INTERFACE (NDK)                   |\n" +
            "|   - Loads binary JNI library 'native-lib' compiled in modern C++            |\n" +
            "|   - System Fallback: High-precision native Kotlin algorithms               |\n" +
            "+------------------+----------------------------------+----------------------+\n" +
            "                   |                                  |\n" +
            "                   v (Offline Records)                v (Upstream Trace)\n" +
            "+--------------------------------------+   +---------------------------------+\n" +
            "|        SQLITE ROOM DATABASE LAYER    |   |     RETROFIT CLOUD SYNCHRONIZER |\n" +
            "| [HealthLogs] [Alerts] [UsageEvents]  |   |     Transmits Telemetry packets |\n" +
            "+--------------------------------------+   +---------------------------------+"
        
        val cell = PdfPCell(Phrase(flowText, codeFont)).apply {
            backgroundColor = Color(245, 245, 245)
            setPadding(10f)
            border = Rectangle.BOX
            borderColor = Color(200, 200, 200)
        }
        flowTable.addCell(cell)
        document.add(flowTable)

        // 2. Core Components System Details
        document.add(Paragraph("2. KEY ARCHITECTURAL LAYERS", h1Font).apply { spacingAfter = 8f })
        
        document.add(Paragraph("A. NDK Native Monitor Layer (NativeMonitor.kt)", h2Font).apply { spacingAfter = 4f })
        document.add(Paragraph(
            "The native calculation subsystem interacts with a custom C++ library (native-lib) via JNI. It exposes three primary formulas to execute high-density floating evaluations on sensor readings:\n" +
            "  1. CPU Architecture query: inspects target C++ macros to locate instruction sets (ARM64, x86_64).\n" +
            "  2. Health Index formula: scores system state out of 100 with strict penalties applied dynamically:\n" +
            "     - Battery Overheat: applies a quadratic factor penalty for battery temp values exceeding 35.0°C.\n" +
            "     - Low Battery: deducts safety margins when charge registers below 20%.\n" +
            "     - Storage pressure: penalizes free spaces dropping below 15%.\n" +
            "     - RAM exhaustion: deducts significant scores when free resources decrease below 10%.\n" +
            "  3. Resource Score estimator: evaluates global system stress indices based on real CPU loads and combined active memories.\n" +
            "A fallback Kotlin algorithm is integrated to ensure the application remains perfectly functional even if the native binaries are absent or unsynced.",
            bodyFont
        ).apply { spacingAfter = 10f })

        document.add(Paragraph("B. Managed MVVM ViewModel (PhoneHealthViewModel.kt)", h2Font).apply { spacingAfter = 4f })
        document.add(Paragraph(
            "The viewModel maintains operational states as immutable Kotlin flows. It gathers hardware measurements from local system intent receivers and merges them in real-time with calculated optimization overrides. " +
            "When users terminate background tasks, the ViewModel increments virtual savings variables, causing Jetpack Compose UI frames to refresh instantly. " +
            "Additionally, it manages all asynchronous coroutine scopes when coordinating Room Database queries or executing upstream HTTP server transmissions.",
            bodyFont
        ).apply { spacingAfter = 10f })

        document.add(Paragraph("C. Jetpack Compose UI & Theme", h2Font).apply { spacingAfter = 4f })
        document.add(Paragraph(
            "Designed inside a unified Material Design 3 (M3) Scaffold leveraging dynamic background schemes. Cards are themed under responsive Surface accents, using 28.dp container radius profiles, distinct high-contrast outline borders, and a balanced custom grid. " +
            "The app implements fluid layouts that adapt efficiently across multiple display sizes.",
            bodyFont
        ).apply { spacingAfter = 12f })

        // 3. Mathematical Calculations Table
        document.add(Paragraph("3. SYSTEM CONDITION PENALTY STANDARDS", h1Font).apply { spacingAfter = 10f })
        val table = PdfPTable(4).apply {
            widthPercentage = 100f
            setSpacingAfter(14f)
        }
        val headers = listOf("Sensor Metric", "Safe Threshold", "Penalty Equation", "Metric Type")
        for (h in headers) {
            val hCell = PdfPCell(Phrase(h, boldBodyFont)).apply {
                backgroundColor = Color(220, 230, 242)
                setPadding(6f)
                horizontalAlignment = Element.ALIGN_CENTER
            }
            table.addCell(hCell)
        }

        val rows = listOf(
            listOf("Battery Temperature", "<= 35.0 °C", "Score Penalty = (excess_temp ^ 2) * 1.5", "THERMAL (CRITICAL)"),
            listOf("Battery Level", ">= 20.0 %", "Score Penalty = (20.0 - level) * 0.75", "POWER (WARNING)"),
            listOf("Free Storage Space", ">= 15.0 %", "Score Penalty = (15.0 - free_pct) * 1.2", "STORAGE (INFO)"),
            listOf("Free System Memory", ">= 10.0 %", "Score Penalty = (10.0 - free_pct) * 1.5", "RAM (CRITICAL)")
        )

        for (row in rows) {
            for (cellVal in row) {
                val rCell = PdfPCell(Phrase(cellVal, bodyFont)).apply {
                    setPadding(5f)
                    horizontalAlignment = if (cellVal.startsWith("Score")) Element.ALIGN_LEFT else Element.ALIGN_CENTER
                }
                table.addCell(rCell)
            }
        }
        document.add(table)

        // 4. Instructions Block
        document.add(Paragraph("4. PHYSICAL DEVICE INSTALLATION INSTRUCTIONS", h1Font).apply { spacingAfter = 8f })
        
        val instructionPoints = Paragraph().apply {
            add(Chunk("To install, run, and test this compiled APK file on a physical Android mobile device, follow these instructions:\n\n", boldBodyFont))
            add(Chunk("  Step 1: Download the newly compiled APK file direct from this workspace build outputs path: /app/build/outputs/apk/debug/app-debug.apk\n", bodyFont))
            add(Chunk("  Step 2: Transfer the downloaded APK file over USB or a custom cloud drive to your physical Android device.\n", bodyFont))
            add(Chunk("  Step 3: On your physical device, open your File Manager application, navigate to the APK location, and click it to unpack.\n", bodyFont))
            add(Chunk("  Step 4: If prompted, enable system permission settings to allow install packages from non-Play Store sources ('Install Unknown Apps').\n", bodyFont))
            add(Chunk("  Step 5: Run the newly installed application 'System Core' directly from your home screen. You can monitor resource gauges, clear database thresholds, and execute remote synchronizer triggers directly.\n", bodyFont))
            spacingAfter = 14f
        }
        document.add(instructionPoints)

        // 5. Free App Publishing Block
        document.add(Paragraph("5. FREE APP PUBLISHING ALTERNATIVES", h1Font).apply { spacingAfter = 8f })
        val publishPoints = Paragraph().apply {
            add(Chunk("You can publish your Android application completely at zero cost using these standard host distribution platforms:\n\n", boldBodyFont))
            add(Chunk("  A. GitHub Releases: Host your APK files direct on your GitHub repository release tags. Users can browse your repository and download new APK updates free of charges, with no developer license dues.\n", bodyFont))
            add(Chunk("  B. F-Droid Repository: The most popular, completely free catalog for Free and Open Source (FOSS) Android applications. You can submit your GitHub repository link, and F-Droid's machines compile your source into verified signed APK entries.\n", bodyFont))
            add(Chunk("  C. Amazon Appstore: Joining Amazon's Android Developer portal is 100% free with no registration fees. You can upload compiled APK files to reach millions of Kindle/Fire and standard Android telephones.\n", bodyFont))
            add(Chunk("  D. SlideME Ecosystem: A reputable alternative market that allows global developer registration at absolutely zero cost.\n", bodyFont))
            spacingAfter = 14f
        }
        document.add(publishPoints)

        // 6. GitHub Upload Block
        document.add(Paragraph("6. HOW TO UPLOAD THIS PROJECT TO GITHUB", h1Font).apply { spacingAfter = 8f })
        val githubPoints = Paragraph().apply {
            add(Chunk("Follow this step-by-step roadmap to publish your entire project source files to your personal GitHub account:\n\n", boldBodyFont))
            add(Chunk("  Step 1 - Export Workspace: Click on the 'Settings' gear icon in the AI Studio platform header, then choose 'Export as ZIP' to download the entire unpacked code folder to your desktop.\n", bodyFont))
            add(Chunk("  Step 2 - Unpack Folder: Right-click and extract the downloaded ZIP folder to a secure project directory on your personal computer.\n", bodyFont))
            add(Chunk("  Step 3 - Create GitHub Repository: Log in to https://github.com/ and click the '+' icon -> 'New repository'. Provide a descriptive name, select either Public or Private, and leave README and .gitignore unchecked.\n", bodyFont))
            add(Chunk("  Step 4 - Initialize Git Locally: Open your system terminal (or git-cmd prompt), navigate into the extracted project directory, and initialize a local repository:\n", bodyFont))
            add(Chunk("          git init\n", codeFont))
            add(Chunk("          git add .\n", codeFont))
            add(Chunk("          git commit -m \"Initial commit - NDK Phone Health Dashboard\"\n", codeFont))
            add(Chunk("  Step 5 - Link and Push: Set branch to main and complete the push using URLs copied from your GitHub creation panel:\n", bodyFont))
            add(Chunk("          git branch -M main\n", codeFont))
            add(Chunk("          git remote add origin <YOUR_GITHUB_REPOSITORY_URL>\n", codeFont))
            add(Chunk("          git push -u origin main\n", codeFont))
            spacingAfter = 14f
        }
        document.add(githubPoints)

        // Notes box
        val noteTable = PdfPTable(1).apply {
            widthPercentage = 100f
            setSpacingAfter(10f)
        }
        val noteCell = PdfPCell(Phrase("IMPORTANT SAFETY NOTICE:\n" +
            "This debug build is signed with a sandbox local developer keystore. " +
            "It is highly optimized for performance diagnostics and safety testing. " +
            "No personal user tracking payloads are executed offline or transmitted without active manual synchronizer trigger action.", noteFont)).apply {
            backgroundColor = Color(255, 243, 243)
            setPadding(10f)
            border = Rectangle.BOX
            borderColor = Color(230, 150, 150)
        }
        noteTable.addCell(noteCell)
        document.add(noteTable)

        // Footer with signature
        document.add(Paragraph("Document compiled dynamically by AIS Code Engine.", subtitleFont).apply {
            alignment = Element.ALIGN_RIGHT
            spacingBefore = 30f
        })

        document.close()
    }

    private fun buildMarkdownContent(): String {
        return """
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
""".trimIndent()
    }
}
