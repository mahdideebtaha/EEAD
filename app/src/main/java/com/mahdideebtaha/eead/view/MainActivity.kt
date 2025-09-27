package com.mahdideebtaha.eead.view

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mahdideebtaha.eead.databinding.ActivityMainBinding
import com.mahdideebtaha.eead.viewmodel.BenchmarkViewModel

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BenchmarkViewModel by viewModels()

    private val categories = listOf("Sorting", "Compression", "Encryption")
    private val algorithms = mapOf(
        "Sorting" to listOf("QuickSort", "MergeSort", "BubbleSort"),
        "Compression" to listOf("Huffman", "LZW", "Deflate"),
        "Encryption" to listOf("AES", "RSA", "Blowfish")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupSpinners()
        observeViewModel()

        binding.runButton.setOnClickListener {
            val category = binding.categorySpinner.selectedItem as String
            val algorithm = binding.algorithmSpinner.selectedItem as String
            val repetitions = binding.repetitionInput.text.toString().toIntOrNull() ?: 1
            viewModel.runBenchmark(this, category, algorithm, repetitions)
        }

    }


    private fun setupSpinners() {
        binding.categorySpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        binding.categorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    val selected = categories[position]
                    val algos = algorithms[selected] ?: emptyList()
                    binding.algorithmSpinner.adapter =
                        ArrayAdapter(
                            this@MainActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            algos
                        )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Optionally handle no selection here
                }
            }
    }

    private fun observeViewModel() {
        viewModel.result.observe(this) {
            binding.resultTextView.text = getString(
                com.mahdideebtaha.eead.R.string.benchmark_complete_algorithm_avg_time_ms_energy_mah,
                it.algorithm,
                "%.2f".format(it.avgTimeMs),
                "%.6f".format(it.energyConsumed_mAh)
            ).trimIndent()
        }

        viewModel.error.observe(this) {
            binding.resultTextView.text = getString(com.mahdideebtaha.eead.R.string.error, it)
        }
    }
}