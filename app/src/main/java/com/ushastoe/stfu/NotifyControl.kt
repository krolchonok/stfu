package com.ushastoe.stfu

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotifyControl : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val packageName = sbn.packageName
        val postTime = sbn.postTime

        Log.d("NotificationListener", "Notification posted: $packageName, $postTime")
        Log.d("NotificationListener", "Title: ${notification.extras.getString("android.title")}")
        Log.d("NotificationListener", "Text: ${notification.extras.getString("android.text")}")
        Log.d("NotificationListener", "PackageName: $packageName") // добавлено

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("NotificationListener", "Notification removed: ${sbn.packageName}")
    }
}
