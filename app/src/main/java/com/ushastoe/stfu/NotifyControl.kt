package com.ushastoe.stfu

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log


class NotifyControl : NotificationListenerService() {
    private val PREFERENCES_NAME = "NotifyControlPrefs"
    private val PACKAGES_KEY = "PackageNames"
    private val ENABLE_FUNC = "Enable"

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val packageName = sbn.packageName
        val postTime = sbn.postTime
        val title = notification.extras.getString(Notification.EXTRA_TITLE)
        val notificationText = notification.extras.getString(Notification.EXTRA_TEXT)

        if (isPackageInList(packageName) && getEnabledFunc()) {
            whenCancelled()
            cancelNotification(sbn.key)
        }
    }

    private fun whenCancelled() {
        vibrateDevice(this, 500)
    }

    private fun vibrateDevice(context: Context, duration: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("NotificationListener", "Notification removed: ${sbn.packageName}")
    }

    private fun getSavedPackages(): List<String> {
        val sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val savedPackages = sharedPreferences.getString(PACKAGES_KEY, "") ?: ""
        return savedPackages.split(";").filter { it.isNotEmpty() }
    }

    private fun isPackageInList(packageName: String): Boolean {
        val packageList = getSavedPackages()
        return packageName in packageList
    }

    private fun getEnabledFunc(): Boolean {
        val sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val enabled = sharedPreferences.getBoolean(ENABLE_FUNC, false)
        return enabled
    }
}
