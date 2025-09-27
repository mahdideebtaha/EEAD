package com.mahdideebtaha.eead.repository

import android.content.Context
import com.mahdideebtaha.eead.data.FirebaseService
import com.mahdideebtaha.eead.model.BenchmarkResult
import com.mahdideebtaha.eead.utils.EncryptionUtils
import com.mahdideebtaha.eead.utils.EnergyUtils
import com.mahdideebtaha.eead.utils.HuffmanCompression
import com.mahdideebtaha.eead.utils.LZWCompression
import kotlin.random.Random

class BenchmarkRepository(private val context: Context) {

    fun runBenchmark(
        category: String,
        algorithm: String,
        repetitions: Int,
        requestedDataSize: Int = 50000,
        onResult: (BenchmarkResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        Thread {
            try {
                val (inputDataType, inputDataSize) = when (category) {
                    "Sorting" -> {
                        val maxSize = 50000
                        val size = requestedDataSize.coerceAtMost(maxSize).coerceAtLeast(1000)
                        "IntArray" to size
                    }
                    "Compression" -> {
                        val maxSize = 20000
                        val size = requestedDataSize.coerceAtMost(maxSize).coerceAtLeast(1000)
                        "ByteArray" to size
                    }
                    "Encryption" -> {
                        val maxSize = 10000
                        val size = requestedDataSize.coerceAtMost(maxSize).coerceAtLeast(512)
                        "ByteArray" to size
                    }
                    else -> {
                        "Unknown" to 1000
                    }
                }

                // reduce noise battery readings before
                val batteryReadingsBefore = mutableListOf<Long>()
                repeat(5) {
                    batteryReadingsBefore.add(EnergyUtils.averageBatteryCurrent(context).toLong())
                    Thread.sleep(100)
                }
                val tempBefore = getCpuTemperature()
                val memBefore = getUsedMemoryMb()

                // start timer
                val startTime = System.currentTimeMillis()

                val times = mutableListOf<Long>()

                repeat(repetitions + 1) { index ->
                    val elapsedTime = when (category) {
                        "Sorting" -> runSorting(algorithm, inputDataSize)
                        "Compression" -> runCompression(algorithm, inputDataSize)
                        "Encryption" -> runEncryption(algorithm, inputDataSize)
                        else -> -1L
                    }
                    if (index > 0) {
                        times.add(elapsedTime)
                        Thread.sleep(100)
                    }
                }

                val endTime = System.currentTimeMillis()

                val batteryReadingsAfter = mutableListOf<Long>()
                repeat(5) {
                    batteryReadingsAfter.add(EnergyUtils.averageBatteryCurrent(context).toLong())
                    Thread.sleep(100)
                }
                val tempAfter = getCpuTemperature()
                val memAfter = getUsedMemoryMb()

                val batteryBeforeAvg = batteryReadingsBefore.average()
                val batteryAfterAvg = batteryReadingsAfter.average()
                val deltaBattery = batteryAfterAvg - batteryBeforeAvg
                val avgCurrent = (batteryBeforeAvg + batteryAfterAvg) / 2.0
                val avgTimeMs = times.average()
                val durationMs = endTime - startTime
                val deltaMemoryMb = memAfter - memBefore

                val energyConsumed_mAh = EnergyUtils.calculateEnergyConsumption(avgCurrent, durationMs)

                val result = BenchmarkResult(
                    algorithm = algorithm,
                    category = category,
                    repetitions = repetitions,
                    avgTimeMs = avgTimeMs,
                    batteryBefore_uA = batteryBeforeAvg.toLong(),
                    batteryAfter_uA = batteryAfterAvg.toLong(),
                    deltaBattery_uA = deltaBattery.toLong(),
                    energyConsumed_mAh = energyConsumed_mAh,
                    durationMs = durationMs,
                    temperatureBeforeC = tempBefore,
                    temperatureAfterC = tempAfter,
                    memoryBeforeMb = memBefore,
                    memoryAfterMb = memAfter,
                    deltaMemoryMb = deltaMemoryMb,
                    dataSizeBytes = inputDataSize.toLong(),
                    dataType = inputDataType
                )

                FirebaseService.uploadBenchmark(result, {
                    onResult(result)
                }, {
                    onError(it)
                })

            } catch (ex: Exception) {
                onError(ex)
            }
        }.start()
    }

    private fun getUsedMemoryMb(): Long {
        val runtime = Runtime.getRuntime()
        val usedMem = runtime.totalMemory() - runtime.freeMemory()
        return usedMem / (1024 * 1024)
    }

    private fun getCpuTemperature(): Float {
        val thermalPath = "/sys/class/thermal/thermal_zone0/temp"
        return try {
            val reader = java.io.BufferedReader(java.io.FileReader(thermalPath))
            val line = reader.readLine()
            reader.close()
            line.toFloat() / 1000f
        } catch (e: Exception) {
            -1f
        }
    }

    // ==================
    // SORTING
    private fun generateIntArray(size: Int): IntArray {
        return IntArray(size) { Random.nextInt(0, 100000) }
    }

    private fun runSorting(algorithm: String, dataSize: Int): Long {
        val array = generateIntArray(dataSize)
        val start = System.nanoTime()
        when (algorithm) {
            "QuickSort" -> array.sortedArray()
            "MergeSort" -> mergeSort(array)
            "BubbleSort" -> bubbleSort(array)
            else -> array.sortedArray()
        }
        val end = System.nanoTime()
        return (end - start) / 1_000_000
    }

    private fun mergeSort(array: IntArray): IntArray {
        if (array.size <= 1) return array
        val middle = array.size / 2
        val left = mergeSort(array.sliceArray(0 until middle))
        val right = mergeSort(array.sliceArray(middle until array.size))
        return merge(left, right)
    }

    private fun merge(left: IntArray, right: IntArray): IntArray {
        var i = 0
        var j = 0
        val result = mutableListOf<Int>()
        while (i < left.size && j < right.size) {
            if (left[i] <= right[j]) {
                result.add(left[i])
                i++
            } else {
                result.add(right[j])
                j++
            }
        }
        while (i < left.size) {
            result.add(left[i])
            i++
        }
        while (j < right.size) {
            result.add(right[j])
            j++
        }
        return result.toIntArray()
    }

    private fun bubbleSort(array: IntArray): IntArray {
        val arr = array.copyOf()
        for (i in 0 until arr.size - 1) {
            for (j in 0 until arr.size - i - 1) {
                if (arr[j] > arr[j + 1]) {
                    val temp = arr[j]
                    arr[j] = arr[j + 1]
                    arr[j + 1] = temp
                }
            }
        }
        return arr
    }

    // ==================
    // COMPRESSION
    private fun generateByteArray(size: Int): ByteArray {
        return ByteArray(size) { Random.nextInt(0, 256).toByte() }
    }

    private fun runCompression(algorithm: String, dataSize: Int): Long {
        val data = generateByteArray(dataSize)
        val textData = String(data)
        val start = System.nanoTime()
        when (algorithm) {
            "Deflate" -> deflate(data)
            "Huffman" -> HuffmanCompression.compress(textData)
            "LZW" -> LZWCompression.compress(data)
            else -> deflate(data)
        }
        val end = System.nanoTime()
        return (end - start) / 1_000_000
    }

    private fun deflate(data: ByteArray): ByteArray {
        val deflater = java.util.zip.Deflater()
        deflater.setInput(data)
        deflater.finish()
        val output = ByteArray(data.size)
        val compressedLength = deflater.deflate(output)
        deflater.end()
        return output.copyOf(compressedLength)
    }

    // ==================
    // ENCRYPTION
    private fun runEncryption(algorithm: String, dataSize: Int): Long {
        val data = generateByteArray(dataSize)
        val start = System.nanoTime()
        when (algorithm) {
            "AES" -> {
                val key = EncryptionUtils.generateAESKey()
                EncryptionUtils.encryptAES(data, key)
            }
            "RSA" -> {
                val keyPair = EncryptionUtils.generateRSAKeyPair()
                EncryptionUtils.encryptRSA(data, keyPair.public)
            }
            "Blowfish" -> {
                val key = "12345678"
                EncryptionUtils.encryptBlowfish(data, key)
            }
            else -> {
                val key = EncryptionUtils.generateAESKey()
                EncryptionUtils.encryptAES(data, key)
            }
        }
        val end = System.nanoTime()
        return (end - start) / 1_000_000
    }
}

