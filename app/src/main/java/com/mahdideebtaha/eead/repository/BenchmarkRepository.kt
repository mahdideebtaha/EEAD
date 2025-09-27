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
        onResult: (BenchmarkResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        Thread {
            try {
                val batteryBefore = EnergyUtils.averageBatteryCurrent(context)
                val startTime = System.currentTimeMillis()

                val times = mutableListOf<Long>()
                repeat(repetitions) {
                    val time = when (category) {
                        "Sorting" -> runSorting(algorithm)
                        "Compression" -> runCompression(algorithm)
                        "Encryption" -> runEncryption(algorithm)
                        else -> -1L
                    }
                    times.add(time)
                }

                val endTime = System.currentTimeMillis()
                val batteryAfter = EnergyUtils.averageBatteryCurrent(context)

                val avgTime = times.average()
                val deltaBattery = batteryAfter - batteryBefore
                val avgCurrent = (batteryBefore + batteryAfter) / 2.0
                val energy = EnergyUtils.calculateEnergyConsumption(avgCurrent, endTime - startTime)

                val result = BenchmarkResult(
                    algorithm = algorithm,
                    category = category,
                    repetitions = repetitions,
                    avgTimeMs = avgTime,
                    batteryBefore_uA = batteryBefore,
                    batteryAfter_uA = batteryAfter,
                    deltaBattery_uA = deltaBattery,
                    energyConsumed_mAh = energy,
                    durationMs = endTime - startTime
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

    // -------------------
    // Sorting implementations
    private fun generateIntArray(size: Int = 50000): IntArray {
        return IntArray(size) { Random.nextInt(0, 100000) }
    }

    private fun runSorting(algorithm: String): Long {
        val array = generateIntArray()
        val start = System.nanoTime()
        when (algorithm) {
            "QuickSort" -> array.sortedArray()  // Kotlin’s built-in quicksort-based sort
            "MergeSort" -> mergeSort(array)
            "BubbleSort" -> bubbleSort(array)
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

    // -------------------
    // Compression implementations
    private fun generateByteArray(size: Int = 50000): ByteArray {
        return ByteArray(size) { Random.nextInt(0, 256).toByte() }
    }

    private fun runCompression(algorithm: String): Long {
        val data = generateByteArray()
        val textData = String(data)
        val start = System.nanoTime()
        when (algorithm) {
            "Deflate" -> deflate(data)
            "Huffman" -> HuffmanCompression.compress(textData)
            "LZW" -> LZWCompression.compress(data)
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

    // -------------------
    // Encryption implementations
    private fun runEncryption(algorithm: String): Long {
        val data = generateByteArray()
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
        }
        val end = System.nanoTime()
        return (end - start) / 1_000_000
    }
}
