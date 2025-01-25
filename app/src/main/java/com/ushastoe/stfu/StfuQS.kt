package com.ushastoe.stfu

import android.content.Context
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class StfuQS: TileService() {
    private val PREFERENCES_NAME = "NotifyControlPrefs"
    private val ENABLE_FUNC = "Enable"

    override fun onStartListening() {
        println("onStartListening")
        super.onStartListening()
        println("onStartListening")
        qsTile.label = if (getEnabledFunc()) "Enable" else "Disable"
        qsTile.state = if (getEnabledFunc()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    override fun onClick() {
        setEnabledFunc(!getEnabledFunc())
        qsTile.label = if (getEnabledFunc()) "Enable" else "Disable"
        qsTile.state = if (getEnabledFunc()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    private fun getEnabledFunc(): Boolean {
        val sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val enabled = sharedPreferences.getBoolean(ENABLE_FUNC, false)
        return enabled
    }

    private fun setEnabledFunc(enabled: Boolean) {
        val sharedPreferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(ENABLE_FUNC, enabled).apply()
    }
}