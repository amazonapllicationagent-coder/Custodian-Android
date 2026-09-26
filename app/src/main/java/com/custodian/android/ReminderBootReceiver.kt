package com.custodian.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val enabled = SafetyRepository(context).load().reminderEnabled
        if (enabled) {
            setCheckInReminderForBoot(context)
        }
    }
}

private fun setCheckInReminderForBoot(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
    val pendingIntent = android.app.PendingIntent.getBroadcast(
        context,
        4101,
        Intent(context, CheckInReminderReceiver::class.java),
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )
    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 20)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_YEAR, 1)
    }
    alarmManager.setInexactRepeating(
        android.app.AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        android.app.AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
}
