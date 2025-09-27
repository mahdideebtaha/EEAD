package com.mahdideebtaha.eead.model

data class BenchmarkResult(
    val algorithm: String = "",
    val category: String = "",
    val repetitions: Int = 1,
    val avgTimeMs: Double = 0.0,
    val durationMs: Long = 0L,
    val temperatureBeforeC: Float = -1f,
    val temperatureAfterC: Float = -1f,
    val memoryBeforeMb: Long = 0L,
    val memoryAfterMb: Long = 0L,
    val deltaMemoryMb: Long = 0L,
    val dataSizeBytes: Long = 0L,
    val dataType: String = "",
    val batteryBefore_uA: Int = 0,
    val batteryAfter_uA: Int = 0,
    val deltaBattery_uA: Int = 0,
    val energyConsumed_mAh: Double = 0.0
)
