package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class AestheticallyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_RECORDING,
                "Voice Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing voice recording status and controls"
                setShowBadge(false)
            }
            
            val shareChannel = NotificationChannel(
                CHANNEL_SHARING,
                "Shared Voice Notes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for new voice boxes and shared listen-along rooms"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
            manager?.createNotificationChannel(shareChannel)
        }
    }

    companion object {
        const val CHANNEL_RECORDING = "recording_channel"
        const val CHANNEL_SHARING = "sharing_channel"
    }
}
