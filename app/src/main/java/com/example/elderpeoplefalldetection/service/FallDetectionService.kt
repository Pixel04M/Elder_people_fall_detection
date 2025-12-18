package com.example.elderpeoplefalldetection.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.elderpeoplefalldetection.MainActivity
import com.example.elderpeoplefalldetection.R
import com.example.elderpeoplefalldetection.data.model.FallRecord
import com.example.elderpeoplefalldetection.repository.FallDetectionRepository
import com.example.elderpeoplefalldetection.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FallDetectionService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.IO)
    private val repository = FallDetectionRepository()
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("fall_detection_prefs", Context.MODE_PRIVATE)
    }
    
    private var isPolling = false
    
    companion object {
        private const val PREF_NOTIFIED_IDS = "notified_record_ids"
        private const val PREF_LAST_NOTIFICATION_TIME = "last_notification_time"
        private const val NOTIFICATION_COOLDOWN_MS = 10000L // 10 seconds cooldown between notifications
    }
    
    fun clearNotifiedIds() {
        prefs.edit().remove(PREF_NOTIFIED_IDS).apply()
    }
    
    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            val notification = createNotification("Monitoring for fall detections")
            startForeground(Constants.FOREGROUND_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            // If notification fails, try to continue anyway
            e.printStackTrace()
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isPolling) {
            startPolling()
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun getNotifiedRecordIds(): Set<Int> {
        val idsString = prefs.getString(PREF_NOTIFIED_IDS, "") ?: ""
        return if (idsString.isEmpty()) {
            emptySet()
        } else {
            idsString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }
    }
    
    private fun saveNotifiedRecordId(id: Int) {
        val currentIds = getNotifiedRecordIds().toMutableSet()
        currentIds.add(id)
        prefs.edit().putString(PREF_NOTIFIED_IDS, currentIds.joinToString(",")).apply()
    }
    
    private fun initializeNotifiedIds(records: List<FallRecord>) {
        val fallRecords = records.filter { it.fallDetected }
        val currentIds = getNotifiedRecordIds().toMutableSet()
        var updated = false
        
        fallRecords.forEach { record ->
            record.id?.let { id ->
                if (!currentIds.contains(id)) {
                    currentIds.add(id)
                    updated = true
                }
            }
        }
        
        if (updated) {
            prefs.edit().putString(PREF_NOTIFIED_IDS, currentIds.joinToString(",")).apply()
        }
    }
    
    private fun startPolling() {
        if (isPolling) return // Prevent multiple polling loops
        isPolling = true
        
        serviceScope.launch {
            // First, get initial records and mark them as notified (don't notify on first run)
            repository.getAllRecords(Constants.STUDENT_ID)
                .onSuccess { records ->
                    initializeNotifiedIds(records)
                }
            
            // Small delay before starting to poll
            delay(2000)
            
            // Now start polling for new records
            while (true) {
                try {
                    repository.getAllRecords(Constants.STUDENT_ID)
                        .onSuccess { records ->
                            // If records list is empty, clear notified IDs (records were deleted)
                            if (records.isEmpty()) {
                                val notifiedIds = getNotifiedRecordIds()
                                if (notifiedIds.isNotEmpty()) {
                                    clearNotifiedIds()
                                }
                                // Don't update foreground notification - keep it silent
                                delay(Constants.POLL_INTERVAL_MS)
                                return@launch
                            }
                            
                            val fallRecords = records.filter { it.fallDetected }
                            val notifiedIds = getNotifiedRecordIds() // Get fresh list after potential clear
                            
                            // Find new fall detections that haven't been notified yet
                            val newFalls = fallRecords.filter { record ->
                                record.id != null && !notifiedIds.contains(record.id!!)
                            }
                            
                            // ONLY notify for NEW fall detections - process ONE at a time
                            // This prevents spam by only showing one notification per polling cycle
                            newFalls.firstOrNull()?.let { record ->
                                record.id?.let { id ->
                                    showFallNotification(record) // This function saves the ID internally
                                }
                            }
                            
                            // DO NOT update foreground notification constantly - only on errors
                        }
                        .onFailure { exception ->
                            // Only update foreground notification on errors, not every cycle
                            updateNotification("Error: ${exception.message}")
                        }
                    
                    delay(Constants.POLL_INTERVAL_MS)
                } catch (e: Exception) {
                    delay(Constants.POLL_INTERVAL_MS)
                }
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    Constants.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for elder people fall detection"
                }
                notificationManager.createNotificationChannel(channel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun createNotification(text: String): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Fall Detection Monitoring")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true) // Make foreground notification silent
            .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority for foreground
            .build()
    }
    
    private fun updateNotification(text: String) {
        // Only update if there's an actual error, not for monitoring status
        notificationManager.notify(Constants.FOREGROUND_NOTIFICATION_ID, createNotification(text))
    }
    
    private fun showFallNotification(record: FallRecord) {
        try {
            val recordId = record.id ?: return // Don't notify if no ID
            
            // Triple-check: make sure we haven't already notified for this ID
            val notifiedIds = getNotifiedRecordIds()
            if (notifiedIds.contains(recordId)) {
                return // Already notified, skip to prevent duplicates
            }
            
            // Check cooldown period to prevent spam
            val lastNotificationTime = prefs.getLong(PREF_LAST_NOTIFICATION_TIME, 0)
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastNotificationTime < NOTIFICATION_COOLDOWN_MS) {
                // Still in cooldown, but save the ID so we don't notify later
                saveNotifiedRecordId(recordId)
                return // Still in cooldown period
            }
            
            // Save immediately BEFORE showing notification to prevent race conditions
            saveNotifiedRecordId(recordId)
            prefs.edit().putLong(PREF_LAST_NOTIFICATION_TIME, currentTime).apply()
            
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                recordId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            val location = record.location
            val locationText = if (location != null) {
                "Location: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}"
            } else {
                "Location: Not available"
            }
            
            val title = record.title ?: "⚠️ FALL DETECTED!"
            val timestamp = record.timestamp.ifEmpty { "Unknown time" }
            
            val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText("Fall detected at $timestamp")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("$title\n" +
                            "Time: $timestamp\n" +
                            "Heartbeat: ${record.heartbeat ?: "N/A"} BPM\n" +
                            locationText +
                            if (record.description != null) "\n\nDetails: ${record.description}" else ""))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            
            // Show notification with record ID as notification ID
            notificationManager.notify(recordId, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
    
}

