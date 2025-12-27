package com.rama.mako

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class BatteryManagerHelper(
    private val context: Context,
    private val callback: (String) -> Unit
) {
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent == null) return
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val tempF = (((intent.getIntExtra(
                BatteryManager.EXTRA_TEMPERATURE,
                -1
            ) / 10f) * 9f / 5f) + 32f).toInt()

            if (level >= 0 && scale > 0) {
                val status = "${(level * 100 / scale.toFloat()).toInt()}% :: $tempF°F"
                callback(status)
            }
        }
    }

    fun register() =
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    fun unregister() = context.unregisterReceiver(batteryReceiver)
}
