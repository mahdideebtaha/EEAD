package com.mahdideebtaha.eead.utils

import android.content.Context
import android.os.BatteryManager

object EnergyUtils {
    fun averageBatteryCurrent(context: Context, durationMs: Long = 2000, intervalMs: Long = 200): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val readings = mutableListOf<Int>()
        val start = System.currentTimeMillis()

        while (System.currentTimeMillis() - start < durationMs) {
            val reading = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            if (reading != Int.MIN_VALUE) readings.add(reading)
            Thread.sleep(intervalMs)
        }

        return if (readings.isNotEmpty()) readings.average().toInt() else 0
    }

    fun calculateEnergyConsumption(avgMicroAmps: Double, durationMs: Long): Double {
        return (avgMicroAmps / 1_000_000) * (durationMs / 1000.0 / 3600.0)
    }
}