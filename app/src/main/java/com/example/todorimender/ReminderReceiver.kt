package com.example.todorimender

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Tugas"
        val desc = intent.getStringExtra("description") ?: "Deskripsi tugas"

        // Buat channel jika Android >= Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Tugas Reminder"
            val descriptionText = "Channel pengingat tugas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("tugas_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Buat notifikasi
        val builder = NotificationCompat.Builder(context, "tugas_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ganti dengan icon kamu
            .setContentTitle("Tugas: $title")
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
