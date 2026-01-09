package com.example.elderpeoplefalldetection.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.elderpeoplefalldetection.data.model.FallRecord
import com.example.elderpeoplefalldetection.viewmodel.FallDetectionViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private enum class MonitorTab {
    HEARTBEAT,
    FALL_ALERTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FallDetectionScreen(viewModel: FallDetectionViewModel) {
    val records by viewModel.records.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val latestHeartbeat by viewModel.latestHeartbeat.collectAsState()
    
    var selectedTab by remember { mutableStateOf(MonitorTab.HEARTBEAT) }
    var selectedRecord by remember { mutableStateOf<FallRecord?>(null) }

    // Auto-refresh every 2 seconds for real-time monitoring
    LaunchedEffect(Unit) {
        viewModel.loadRecords()
        while (true) {
            kotlinx.coroutines.delay(2000)
            viewModel.loadRecords()
        }
    }

    val isInDetails = selectedRecord != null
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            isInDetails -> "Fall Alert Details"
                            selectedTab == MonitorTab.HEARTBEAT -> "Heartbeat Monitor"
                            else -> "Fall Alerts"
                        }
                    )
                },
                actions = {
                    if (!isInDetails) {
                        Text(
                            text = "Refresh",
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable { viewModel.loadRecords() },
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (!isInDetails) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == MonitorTab.HEARTBEAT,
                        onClick = { selectedTab = MonitorTab.HEARTBEAT },
                        icon = { Text("💓") },
                        label = { Text("Heartbeat") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == MonitorTab.FALL_ALERTS,
                        onClick = { selectedTab = MonitorTab.FALL_ALERTS },
                        icon = { Text("⚠️") },
                        label = { Text("Fall Alerts") }
                    )
                }
            }
        }
    ) { paddingValues ->
        Crossfade(
            targetState = Triple(selectedTab, selectedRecord, isInDetails),
            label = "main_pages"
        ) { (tab, record, details) ->
            when {
                details && record != null -> {
                    FallAlertDetailPage(
                        record = record,
                        onBack = { selectedRecord = null }
                    )
                }

                tab == MonitorTab.HEARTBEAT -> {
                    HeartbeatPage(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        latestHeartbeat = latestHeartbeat,
                        records = records,
                        isDeleting = isDeleting,
                        onDeleteAll = { viewModel.deleteAllRecords() },
                        error = error,
                        isLoading = isLoading
                    )
                }

                else -> {
                    FallAlertsPage(
                    modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        allRecords = records,
                        isLoading = isLoading,
                        error = error,
                        isDeleting = isDeleting,
                        onDeleteAll = { viewModel.deleteAllRecords() },
                        onSelectRecord = { selectedRecord = it }
                    )
                }
            }
        }
    }
}

@Composable
fun RealTimeHeartbeatCard(heartbeat: Int) {
    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Determine heart rate status and color
    val (status, statusColor, heartColor) = when {
        heartbeat < 60 -> Triple("Low", Color(0xFF2196F3), Color(0xFF64B5F6))
        heartbeat > 100 -> Triple("High", Color(0xFFFF5722), Color(0xFFFF8A65))
        else -> Triple("Normal", Color(0xFF4CAF50), Color(0xFF81C784))
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Real-Time Heartbeat Monitor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Animated Heart Icon
            Box(
                modifier = Modifier
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulse effect
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .background(
                            color = heartColor.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
                Text(
                    text = "💓",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.scale(scale)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Heartbeat Value
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$heartbeat",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = heartColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status Badge
            Surface(
                color = statusColor.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Heart Rate Range Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Normal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "60-100",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Current",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$heartbeat",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = heartColor
                    )
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: String): String {
    if (timestamp.isEmpty()) return "No timestamp"
    return try {
        val inputFormat = if (timestamp.contains(".")) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        }
        val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(timestamp)
        date?.let { outputFormat.format(it) } ?: timestamp
    } catch (_: Exception) {
        timestamp
    }
}

@Composable
private fun HeartbeatPage(
    modifier: Modifier = Modifier,
    latestHeartbeat: Int?,
    records: List<FallRecord>,
    isDeleting: Boolean,
    onDeleteAll: () -> Unit,
    error: String?,
    isLoading: Boolean
) {
    Column(
        modifier = modifier
    ) {
        // Real-time Heartbeat Monitoring Card
        latestHeartbeat?.let { heartbeat ->
            RealTimeHeartbeatCard(heartbeat = heartbeat)
        } ?: run {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💓",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Waiting for heartbeat data...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Monitoring Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Total Records: ${records.size}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Fall Detections: ${records.count { it.fallDetected }}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDeleteAll,
                    enabled = !isDeleting && records.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Deleting...")
                    } else {
                        Text("🗑️ Delete All Records")
                    }
                }
            }
        }

        // Error Message
        error?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Error: $it",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Loading Indicator / Simple history information
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (records.isNotEmpty()) {
                Text(
                    text = "Recent activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(records.take(5)) { record ->
                        MiniRecordRow(record = record)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniRecordRow(record: FallRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (record.fallDetected) "Fall detected" else "Normal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (record.fallDetected) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = formatTimestamp(record.createdAt ?: record.updatedAt ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        record.heartbeat?.let {
            Text(
                text = "$it BPM",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FallAlertsPage(
    modifier: Modifier = Modifier,
    allRecords: List<FallRecord>,
    isLoading: Boolean,
    error: String?,
    isDeleting: Boolean,
    onDeleteAll: () -> Unit,
    onSelectRecord: (FallRecord) -> Unit
) {
    var showOnlyFalls by remember { mutableStateOf(true) }

    val filteredRecords = remember(allRecords, showOnlyFalls) {
        if (showOnlyFalls) {
            allRecords.filter { it.fallDetected }
        } else {
            allRecords
        }
    }

    Column(
        modifier = modifier
    ) {
        // Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fall Alerts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            FilterChip(
                selected = showOnlyFalls,
                onClick = { showOnlyFalls = !showOnlyFalls },
                label = {
                    Text(
                        if (showOnlyFalls) "Showing: Falls only" else "Showing: All records"
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer
                )
            )
        }

        // Delete all button dedicated for this page as well
        if (filteredRecords.isNotEmpty()) {
            Button(
                onClick = onDeleteAll,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onError
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deleting...")
                } else {
                    Text("🗑️ Delete All Alerts")
                }
            }
        }

        // Error section
        error?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Error: $it",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showOnlyFalls) "No fall alerts found" else "No records found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRecords) { record ->
                        FallRecordCard(
                            record = record,
                            onClick = { onSelectRecord(record) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FallRecordCard(
    record: FallRecord,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (record.fallDetected) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (record.fallDetected) "⚠️ FALL DETECTED" else "Normal Record",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (record.fallDetected) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = formatTimestamp(record.createdAt ?: record.updatedAt ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            // Heartbeat
            record.heartbeat?.let {
                Text(
                    text = "Heartbeat: $it BPM",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Acceleration
            record.acceleration?.let { accel ->
                val ax = String.format(Locale.getDefault(), "%.2f", accel.x)
                val ay = String.format(Locale.getDefault(), "%.2f", accel.y)
                val az = String.format(Locale.getDefault(), "%.2f", accel.z)
                Text(
                    text = "Acceleration: X=$ax, Y=$ay, Z=$az",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Location
            record.location?.let { loc ->
                Spacer(modifier = Modifier.height(4.dp))
                val lat = String.format(Locale.getDefault(), "%.6f", loc.latitude)
                val lon = String.format(Locale.getDefault(), "%.6f", loc.longitude)
                Text(
                    text = "📍 Location: $lat, $lon",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Title
            record.title?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Title: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Description
            record.description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Details: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FallAlertDetailPage(
    record: FallRecord,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "← Back to alerts",
            modifier = Modifier
                .clickable { onBack() }
                .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (record.fallDetected) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (record.fallDetected) "Fall Alert" else "Record Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // When it happened
                Text(
                    text = "When",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatTimestamp(record.timestamp),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Where (location)
                Text(
                    text = "Where",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                record.location?.let { loc ->
                    val lat = String.format(Locale.getDefault(), "%.6f", loc.latitude)
                    val lon = String.format(Locale.getDefault(), "%.6f", loc.longitude)
                    val mapsUrl = "https://www.google.com/maps/search/?api=1&query=$lat,$lon"

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Latitude: $lat\nLongitude: $lon",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Open in Google Maps",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            uriHandler.openUri(mapsUrl)
                        }
                    )
                } ?: Text(
                    text = "Location not available",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ID information
                Text(
                    text = "ID",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Record ID: ${record.id ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Heartbeat information
                Text(
                    text = "Heartbeat",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = record.heartbeat?.let { "$it BPM" } ?: "No heartbeat data",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Optional extra context
                record.title?.let {
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                record.description?.let {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
