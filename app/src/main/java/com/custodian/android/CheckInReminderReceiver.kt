package com.custodian.android

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class CheckInReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val channelId = "custodian_checkin"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "Safety check-in reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        val notification = Notification.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Custodian check-in")
            .setContentText("Take a moment to confirm that you are safe.")
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }
}
