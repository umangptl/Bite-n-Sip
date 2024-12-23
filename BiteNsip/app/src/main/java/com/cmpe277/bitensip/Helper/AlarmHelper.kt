package com.cmpe277.bitensip

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import java.util.concurrent.TimeUnit

class AlarmHelper {
    private var alarmManager: AlarmManager? = null

    private val ACTION_BD_NOTIFICATION = "com.practice.biteNslip.NOTIFICATION"

    fun setAlarm(context: Context, notificationFrequency: Long) {
        val notificationFrequencyMs = TimeUnit.MINUTES.toMillis(notificationFrequency)

        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(context, NotifierReceiver::class.java)
        alarmIntent.action = ACTION_BD_NOTIFICATION

        // Add FLAG_IMMUTABLE for API level 31 and above
        val pendingAlarmIntent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        Log.i("AlarmHelper", "Setting Alarm Interval to: $notificationFrequency minutes")

        alarmManager!!.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            notificationFrequencyMs,
            pendingAlarmIntent
        )

        /* Restart if rebooted */
        val receiver = ComponentName(context, BootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun cancelAlarm(context: Context) {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(context, NotifierReceiver::class.java)
        alarmIntent.action = ACTION_BD_NOTIFICATION

        // Add FLAG_IMMUTABLE for API level 31 and above
        val pendingAlarmIntent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        alarmManager!!.cancel(pendingAlarmIntent)

        /* Alarm won't start again if device is rebooted */
        val receiver = ComponentName(context, BootReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        Log.i("AlarmHelper", "Cancelling alarms")
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun checkAlarm(context: Context): Boolean {
        val alarmIntent = Intent(context, NotifierReceiver::class.java)
        alarmIntent.action = ACTION_BD_NOTIFICATION

        // Add FLAG_IMMUTABLE for API level 31 and above
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) != null
        } else {
            PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_NO_CREATE
            ) != null
        }
    }

}