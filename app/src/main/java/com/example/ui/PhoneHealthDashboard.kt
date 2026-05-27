package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.DeviceAlert
import com.example.data.database.DeviceHealthLog
import com.example.data.database.PhoneUsageEvent
import com.example.monitors.BackgroundApp
import com.example.monitors.LiveMetrics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneHealthDashboard(
    viewModel: PhoneHealthViewModel,
    modifier: Modifier = Modifier
) {
    val liveMetrics by viewModel.liveMetrics.collectAsStateWithLifecycle()
    val backgroundApps by viewModel.backgroundApps.collectAsStateWithLifecycle()
    val allHealthLogs by viewModel.allHealthLogs.collectAsStateWithLifecycle()
    val allAlerts by viewModel.allAlerts.collectAsStateWithLifecycle()
    val allUsageEvents by viewModel.allUsageEvents.collectAsStateWithLifecycle()

    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncResponse by viewModel.syncResponseState.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) }
    val tabs = listOf("Sensor Gauges", "Background Trace", "Usage Log", "API Cloud Sync")

    val totalRamUsageMb = liveMetrics.totalRamMb - liveMetrics.freeRamMb

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "NDK LOGIC ENGINE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "System ",
                                fontWeight = FontWeight.Light,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Text(
                                "Core",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.resetMonitorBaseline() },
                        modifier = Modifier.testTag("reset_sensors_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Metrics Override",
                            tint = Color.Gray
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Overall System Condition HUD Card (Persistent Top Panel)
            DashboardHudPanel(
                metrics = liveMetrics,
                onOptimizeClick = { viewModel.optimizeBackgroundActivity() }
            )

            // 2. Tabs Selector
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    )
                }
            }

            // 3. Main Dashboard Panels
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                when (activeTab) {
                    0 -> SensorGaugesPanel(
                        metrics = liveMetrics,
                        totalRamUsageMb = totalRamUsageMb,
                        logs = allHealthLogs
                    )
                    1 -> BackgroundAppsPanel(
                        apps = backgroundApps,
                        onKillClick = { pkg -> viewModel.terminateBackgroundApp(pkg) },
                        onOptimizeClick = { viewModel.optimizeBackgroundActivity() }
                    )
                    2 -> UsageAndAlertsPanel(
                        alerts = allAlerts,
                        events = allUsageEvents,
                        onClearClick = { viewModel.clearLogsAndTrends() }
                    )
                    3 -> CloudSyncPanel(
                        viewModel = viewModel,
                        isSyncing = isSyncing,
                        syncResponse = syncResponse,
                        logsCount = allHealthLogs.size,
                        alertsCount = allAlerts.size,
                        eventsCount = allUsageEvents.size
                    )
                }
            }
        }
    }
}

// --- PERSISTENT CONDITION STATUS PANEL ---
@Composable
fun DashboardHudPanel(
    metrics: LiveMetrics,
    onOptimizeClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Neon progress circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track background
                    drawCircle(
                        color = Color(0xFF222222),
                        radius = size.minDimension / 2 - 4.dp.toPx(),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    // Neon colored circle
                    val sweepAngle = (metrics.healthScore / 100.0 * 360f).toFloat()
                    val color = when {
                        metrics.healthScore >= 85.0 -> Color(0xFF00FF66)
                        metrics.healthScore >= 60.0 -> Color(0xFFFF9900)
                        else -> Color(0xFFFF0055)
                    }
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${metrics.healthScore.toInt()}%",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "HEALTH",
                        color = Color.Gray,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            // JNI information feeds
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF242424), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CPU JNI C++ API",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = metrics.compiledCpuArch,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Threat Index",
                        tint = if (metrics.resourceScore < 50) Color(0xFF00FF66) else Color(0xFFFF0055),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "System Stress Index: ${metrics.resourceScore}/100",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Optimizer
            Button(
                onClick = onOptimizeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("boost_now_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Optimize",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text("BOOST", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- PANEL 1: HARDWARE SENSORS & METERS ---
@Composable
fun SensorGaugesPanel(
    metrics: LiveMetrics,
    totalRamUsageMb: Double,
    logs: List<DeviceHealthLog>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Line chart plotting native C++ condition metrics history
        item {
            HealthLogTrendChart(logs = logs)
        }

        // Live Gauge Metrics Cards Grid (Row by Row)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Battery",
                                tint = Color(0xFFFF9900),
                                modifier = Modifier.size(20.dp)
                              )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Real-Time Power Condition", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF261D10), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${metrics.batteryLevel.toInt()}% ${metrics.batteryStatus}",
                                color = Color(0xFFFF9900),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { (metrics.batteryLevel / 100f).toFloat() },
                        color = Color(0xFFFF9900),
                        trackColor = Color(0xFF2D251A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SensorMetricLabel("Thermal Temp", "${metrics.batteryTemp}°C")
                        SensorMetricLabel("Safety Health", metrics.batteryHealth)
                        SensorMetricLabel("Voltage", "${metrics.batteryVoltage} mV")
                    }
                }
            }
        }

        // RAM details
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "RAM",
                                tint = Color(0xFF00FF66),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SRAM Memory Allocation", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF102D15), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${metrics.ramFreePct.toInt()}% Free",
                                color = Color(0xFF00FF66),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { ((100.0 - metrics.ramFreePct) / 100f).toFloat() },
                        color = Color(0xFF00FF66),
                        trackColor = Color(0xFF102816),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SensorMetricLabel("Allocated", "${totalRamUsageMb.toInt()} MB")
                        SensorMetricLabel("Free Physical", "${metrics.freeRamMb.toInt()} MB")
                        SensorMetricLabel("System Total", "${metrics.totalRamMb.toInt()} MB")
                    }
                }
            }
        }

        // Storage and Throttling Sensors
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Storage",
                                tint = Color(0xFFD400FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ROM Space", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${String.format("%.1f", metrics.freeStorageGb)} GB free",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "of ${metrics.totalStorageGb.toInt()} GB Total",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { ((100.0 - metrics.storageFreePct) / 100f).toFloat() },
                            color = Color(0xFFD400FF),
                            trackColor = Color(0xFF260033),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Thermal",
                                tint = Color(0xFFFF2E2E),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Thermal State", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = metrics.thermalStatus,
                            color = when {
                                metrics.thermalStatus.contains("Normal") -> Color(0xFF00FF66)
                                metrics.thermalStatus.contains("Light") -> Color(0xFFFF9900)
                                else -> Color(0xFFFF2E2E)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Dynamic Throttling Indicator",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "CPU load",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Live CPU: ${metrics.cpuUsagePct.toInt()}%",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorMetricLabel(label: String, value: String) {
    Column {
        Text(text = label, color = Color.Gray, fontSize = 10.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

// --- PANEL 2: BACKGROUND APPLICATION ACTIVITY ---
@Composable
fun BackgroundAppsPanel(
    apps: List<BackgroundApp>,
    onKillClick: (String) -> Unit,
    onOptimizeClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "Background Security Auditing",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Scan active ambient software threads to intercept resource over-abuse and suspect scripts.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOptimizeClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF2E2E),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scan_security_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Sec Scan"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LOCK & SHIELD ALL PROCESSES", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            "Ambient Process Threads (${apps.filter { it.isRunning }.size} Active)",
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(apps) { app ->
                BackgroundAppItem(app = app, onKillClick = { onKillClick(app.packageName) })
            }
        }
    }
}

@Composable
fun BackgroundAppItem(
    app: BackgroundApp,
    onKillClick: () -> Unit
) {
    val statusColor = when (app.riskStatus) {
        "Suspicious" -> Color(0xFFFF2E2E)
        "High Resource" -> Color(0xFFFF9900)
        "Optimized" -> Color.Gray
        else -> Color(0xFF00FF66)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131313)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (app.riskStatus == "Suspicious" && app.isRunning) Color(0x33FF2E2E) else Color(0xFF1E1E1E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(statusColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName,
                        color = if (app.isRunning) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (app.isSystem) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF222222), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("SYS", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(
                    text = app.packageName,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (app.isRunning) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${app.ramUsageMb.toInt()} MB | CPU: ${app.cpuUsage}%",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (app.isRunning) {
                IconButton(
                    onClick = onKillClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1C1313), RoundedCornerShape(8.dp))
                        .testTag("kill_${app.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kill task",
                        tint = Color(0xFFFF2E2E),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1A1A1A), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "INACTIVE",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// --- PANEL 3: USAGE LOGS & REAL-TIME ALERTS ---
@Composable
fun UsageAndAlertsPanel(
    alerts: List<DeviceAlert>,
    events: List<PhoneUsageEvent>,
    onClearClick: () -> Unit
) {
    var filterType by remember { mutableStateOf(0) } // 0 = Alerts Panel, 1 = Screen/Lock Events Tracker

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .background(Color(0xFF131313), RoundedCornerShape(8.dp))
                    .padding(3.dp)
            ) {
                Button(
                    onClick = { filterType = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (filterType == 0) Color(0xFF222222) else Color.Transparent,
                        contentColor = if (filterType == 0) Color.White else Color.Gray
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Sensor Alerts & Safety", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = { filterType = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (filterType == 1) Color(0xFF222222) else Color.Transparent,
                        contentColor = if (filterType == 1) Color.White else Color.Gray
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Interaction Trace", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = onClearClick,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF2E2E)),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.testTag("clear_history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear DB"
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text("RESET SQL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filterType == 0) {
            // Alerts View
            if (alerts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Lock,
                    text = "No anomalous behavior caught. Live threshold monitoring active."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(alerts) { alert ->
                        DeviceAlertItem(alert = alert)
                    }
                }
            }
        } else {
            // Usage View
            if (events.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Lock,
                    text = "No recorded triggers compiled. Locked events will trace here."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(events) { ev ->
                        PhoneUsageEventItem(ev = ev)
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceAlertItem(alert: DeviceAlert) {
    val cardBorderColor = when (alert.severity) {
        "CRITICAL" -> Color(0xFFFF2E2E)
        "WARNING" -> Color(0xFFFF9900)
        else -> MaterialTheme.colorScheme.outline
    }

    val icon = when (alert.metricType) {
        "BATTERY" -> Icons.Default.Warning
        "THERMAL" -> Icons.Default.Warning
        "STORAGE" -> Icons.Default.Info
        "RAM" -> Icons.Default.Settings
        else -> Icons.Default.Check
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = alert.metricType,
                tint = cardBorderColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = alert.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alert.description,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(alert.timestamp)),
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun PhoneUsageEventItem(ev: PhoneUsageEvent) {
    val indicatorColor = when (ev.eventType) {
        "USER_ACTIVITY" -> Color(0xFF00FF66)
        "SCREEN_EVENT" -> Color(0xFF00E5FF)
        "CHARGER_EVENT" -> Color(0xFFFF9900)
        "PROCESS_KILLED" -> Color(0xFFFF2E2E)
        else -> Color(0xFFD400FF)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131313)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(indicatorColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(indicatorColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ev.eventType,
                            color = indicatorColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(ev.timestamp)),
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ev.eventDescription,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(icon: ImageVector, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Empty",
            tint = Color.DarkGray,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = text,
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// --- PANEL 4: API SERVER TELEMETRY & CLOUD SYNC ---
@Composable
fun CloudSyncPanel(
    viewModel: PhoneHealthViewModel,
    isSyncing: Boolean,
    syncResponse: com.example.data.api.SyncResponse?,
    logsCount: Int,
    alertsCount: Int,
    eventsCount: Int
) {
    val cloudUrl by viewModel.cloudUrl.collectAsStateWithLifecycle()
    val cloudApiKey by viewModel.cloudApiKey.collectAsStateWithLifecycle()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131313)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF222222))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cloud Synchronization Settings",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Transmit compiled log metrics to an external diagnostics database for fleet analysis.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("API Server URL", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = cloudUrl,
                        onValueChange = { viewModel.cloudUrl.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("cloud_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00FF66),
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedContainerColor = Color(0xFF080808),
                            unfocusedContainerColor = Color(0xFF080808)
                        ),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Authorization Key", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = cloudApiKey,
                        onValueChange = { viewModel.cloudApiKey.value = it },
                        placeholder = { Text("Optional authorization bearer token", color = Color.DarkGray, fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("cloud_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00FF66),
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedContainerColor = Color(0xFF080808),
                            unfocusedContainerColor = Color(0xFF080808)
                        ),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.triggerCloudSync() },
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("sync_submit_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Sync"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PUSH CHRONOLOGY TO CLOUD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Offline metrics counts info
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Local SQL Diagnostics Store",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("NDK Log Points", color = Color.Gray, fontSize = 11.sp)
                            Text("$logsCount rows", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                        }
                        Column {
                            Text("Alert History", color = Color.Gray, fontSize = 11.sp)
                            Text("$alertsCount logs", color = Color(0xFFFF9900), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                        }
                        Column {
                            Text("Chronology Traces", color = Color.Gray, fontSize = 11.sp)
                            Text("$eventsCount traces", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Response Banner
        if (syncResponse != null) {
            item {
                val bannerColor = if (syncResponse.success) Color(0xFF00FF66) else Color(0xFFFF2E2E)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, bannerColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (syncResponse.success) Icons.Default.Done else Icons.Default.Close,
                                contentDescription = "Sync Report",
                                tint = bannerColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (syncResponse.success) "Synchronization Active" else "Sync Issue Detected",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = syncResponse.message,
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Count Synced: ${syncResponse.syncedCount} blocks | Server Time: ${syncResponse.serverTimestamp}",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// Custom trend chart drawing logic
@Composable
fun HealthLogTrendChart(
    logs: List<DeviceHealthLog>,
    modifier: Modifier = Modifier
) {
    if (logs.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(Color(0xFF131313), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF222222), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF00FF66), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Calibrating JNI logs and mapping system trace curves...",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    // Limit to latest 20 snapshots sorted chronologically
    val chartLogs = logs.take(20).reversed()
    val maxScore = 100f
    val minScore = 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131313)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF222222)),
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Condition Performance Trend (JNI Logs)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF102816), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                    Text(
                        "${chartLogs.size} cycles",
                        color = Color(0xFF00FF66),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                val width = size.width
                val height = size.height

                // Draw background grid lines (horizontal subdivisions)
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = height * (i.toFloat() / gridLines)
                    drawLine(
                        color = Color(0xFF1E1E1E),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                if (chartLogs.size > 1) {
                    val stepX = width / (chartLogs.size - 1)
                    val points = chartLogs.mapIndexed { index, log ->
                        val ratio = (log.healthScore.toFloat() - minScore) / (maxScore - minScore)
                        val x = index * stepX
                        val y = height - (ratio * height)
                        Offset(x, y)
                    }

                    // Draw area under line with beautiful brush gradient
                    val path = Path().apply {
                        moveTo(points.first().x, height)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, height)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x2200FF66),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw the JNI trend line
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = Color(0xFF00FF66),
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Highlight the final/latest reading coordinate
                    drawCircle(
                        color = Color(0xFF00FF66),
                        radius = 5f,
                        center = points.last()
                    )
                }
            }
        }
    }
}
