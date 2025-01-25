package com.ushastoe.stfu

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotificationDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "notifications.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "notifications"
        private const val COLUMN_ID = "id"
        private const val COLUMN_PACKAGE_NAME = "package_name"
        private const val COLUMN_NOTIFICATION_TEXT = "notification_text"
        private const val COLUMN_POST_TIME = "post_time"
        private const val TITLE_NOTIFICATIONS = "title_notifications"
        private const val READED = "readed"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PACKAGE_NAME TEXT,
                $TITLE_NOTIFICATIONS TEXT,
                $COLUMN_NOTIFICATION_TEXT TEXT,
                $COLUMN_POST_TIME LONG,
                $READED BOOLEAN
            );
        """
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addNotification(packageName: String, notificationText: String?, postTime: Long) {
        val values = ContentValues().apply {
            put(COLUMN_PACKAGE_NAME, packageName)
            put(TITLE_NOTIFICATIONS, notificationText)
            put(COLUMN_NOTIFICATION_TEXT, notificationText)
            put(COLUMN_POST_TIME, postTime)
            put(READED, false)
        }
        writableDatabase.insert(TABLE_NAME, null, values)
    }

    fun markNotificationAsUnread(id: Long) {
        val values = ContentValues().apply {
            put(READED, 0)
        }

        writableDatabase.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    fun getAllNotifications(): List<NotificationData> {
        val notifications = mutableListOf<NotificationData>()
        val cursor: Cursor = readableDatabase.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_PACKAGE_NAME, TITLE_NOTIFICATIONS, COLUMN_NOTIFICATION_TEXT, COLUMN_POST_TIME),
            null, null, null, null, "$COLUMN_POST_TIME DESC"
        )

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val packageName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE_NAME))
            val titleNotifications = cursor.getString(cursor.getColumnIndexOrThrow(TITLE_NOTIFICATIONS))
            val notificationText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_TEXT))
            val postTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_POST_TIME))
            notifications.add(NotificationData(id, packageName, titleNotifications, notificationText, postTime))
        }
        cursor.close()
        return notifications
    }

    fun getUnreadNotificationsByPackageName(packageName: String): List<NotificationData> {
        val notifications = mutableListOf<NotificationData>()
        val cursor: Cursor = readableDatabase.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_PACKAGE_NAME, TITLE_NOTIFICATIONS, COLUMN_NOTIFICATION_TEXT, COLUMN_POST_TIME),
            "$COLUMN_PACKAGE_NAME = ? AND $READED = 0",
            arrayOf(packageName),
            null, null, "$COLUMN_POST_TIME DESC"
        )

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val packageName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE_NAME))
            val titleNotifications = cursor.getString(cursor.getColumnIndexOrThrow(TITLE_NOTIFICATIONS))
            val notificationText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_TEXT))
            val postTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_POST_TIME))
            notifications.add(NotificationData(id, packageName, titleNotifications, notificationText, postTime))
        }
        cursor.close()
        return notifications
    }
}

data class NotificationData(val id: Long, val packageName: String, val titleNotifications: String?, val notificationText: String?, val postTime: Long)
