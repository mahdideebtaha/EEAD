package com.mahdideebtaha.eead.repository

import android.content.Context
import android.util.Log
import com.mahdideebtaha.eead.data.FirebaseService
import com.mahdideebtaha.eead.model.BenchmarkResult
import com.mahdideebtaha.eead.utils.EncryptionUtils
import com.mahdideebtaha.eead.utils.EnergyUtils
import com.mahdideebtaha.eead.utils.LZWCompression
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class BenchmarkRepository(private val context: Context) {

    companion object {
        private const val TAG = "BenchmarkRepository"
    }

    fun runBenchmark(
        category: String,
        algorithm: String,
        repetitions: Int,
        requestedDataSize: Int = 50000,
        onResult: (BenchmarkResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    runBenchmarkInternal(category, algorithm, repetitions, requestedDataSize)
                }
                FirebaseService.uploadBenchmark(result, {
                    onResult(result)
                }, {
                    onError(it)
                })

            } catch (ex: Exception) {
                onError(ex)
            }
        }
    }

    private fun runBenchmarkInternal(
        category: String, algorithm: String, repetitions: Int, requestedDataSize: Int
    ): BenchmarkResult {

        val (inputDataType, inputDataSize) = when (category) {
            "Sorting" -> {
                val maxSize = 70000
                val size = requestedDataSize.coerceIn(1000, maxSize)
                if (requestedDataSize != size) Log.w(
                    TAG, "Requested sorting size adjusted to $size"
                )
                "IntArray" to size
            }

            "Compression" -> {
                val maxSize = 40000
                val size = requestedDataSize.coerceIn(1000, maxSize)
                if (requestedDataSize != size) Log.w(
                    TAG, "Requested compression size adjusted to $size"
                )
                "ByteArray" to size
            }

            "Encryption" -> {
                val maxSize = 20000
                val size = requestedDataSize.coerceIn(512, maxSize)
                if (requestedDataSize != size) Log.w(
                    TAG, "Requested encryption size adjusted to $size"
                )
                "ByteArray" to size
            }

            else -> {
                Log.e(TAG, "Unknown category: $category")
                "Unknown" to 1000
            }
        }


        val tempBefore = getCpuTemperature()
        val memBefore = getUsedMemoryMb()

        val times = mutableListOf<Long>()

        val batteryBefore_uA = EnergyUtils.averageBatteryCurrent(context)
        val startTime = System.currentTimeMillis()

        repeat(repetitions + 1) { index ->
            val elapsedTime = when (category) {
                "Sorting" -> runSorting(algorithm, inputDataSize)
                "Compression" -> runCompression(algorithm, inputDataSize)
                "Encryption" -> runEncryption(algorithm, inputDataSize)
                else -> -1L
            }
            if (index > 0) {
                times.add(elapsedTime)
            }
        }

        val tempAfter = getCpuTemperature()
        val memAfter = getUsedMemoryMb()

        val avgTimeMs = times.average()
        val durationMs = (repetitions * avgTimeMs).toLong()
        val deltaMemoryMb = memAfter - memBefore

        val batteryAfter_uA = EnergyUtils.averageBatteryCurrent(context)
        val deltaBattery_uA = batteryBefore_uA - batteryAfter_uA
        val energyConsumed_mAh = EnergyUtils.calculateEnergyConsumption(
            avgMicroAmps = deltaBattery_uA.toDouble(),
            durationMs = System.currentTimeMillis() - startTime
        )
        return BenchmarkResult(
            algorithm = algorithm,
            category = category,
            repetitions = repetitions,
            avgTimeMs = avgTimeMs,
            durationMs = durationMs,
            temperatureBeforeC = tempBefore,
            temperatureAfterC = tempAfter,
            memoryBeforeMb = memBefore,
            memoryAfterMb = memAfter,
            deltaMemoryMb = deltaMemoryMb,
            dataSizeBytes = inputDataSize.toLong(),
            dataType = inputDataType,
            batteryBefore_uA = batteryBefore_uA,
            batteryAfter_uA = batteryAfter_uA,
            deltaBattery_uA = deltaBattery_uA,
            energyConsumed_mAh = energyConsumed_mAh,
        )
    }


    private fun getUsedMemoryMb(): Long {
        val runtime = Runtime.getRuntime()
        val usedMem = runtime.totalMemory() - runtime.freeMemory()
        return usedMem / (1024 * 1024)
    }

    private fun getCpuTemperature(): Float {
        val thermalPaths = listOf(
            "/sys/class/thermal/thermal_zone0/temp", "/sys/class/thermal/thermal_zone1/temp"
        )
        thermalPaths.forEach { path ->
            try {
                val reader = java.io.BufferedReader(java.io.FileReader(path))
                val line = reader.readLine()
                reader.close()
                return line.toFloat() / 1000f
            } catch (_: Exception) {
            }
        }
        return -1f
    }

    // ==================
    // SORTING
    private fun generateIntArray(size: Int): IntArray {
        return IntArray(size) { Random.nextInt(0, 100000) }
    }

    private fun runSorting(algorithm: String, dataSize: Int): Long {
        val array = generateIntArray(dataSize)
        val start = System.nanoTime()
        val sorted = when (algorithm) {
            "QuickSort" -> array.sortedArray()
            "MergeSort" -> mergeSort(array)
            "BubbleSort" -> bubbleSort(array)
            else -> array.sortedArray()
        }
        sorted.size
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
        val start = System.nanoTime()
        when (algorithm) {
            "Deflate" -> deflate(data)
            "Huffman" -> HuffmanCompression.compress(data)
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
        val output = ByteArray(data.size * 2)
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