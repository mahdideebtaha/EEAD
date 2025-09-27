package com.mahdideebtaha.eead.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mahdideebtaha.eead.model.BenchmarkResult
import com.mahdideebtaha.eead.repository.BenchmarkRepository

class BenchmarkViewModel : ViewModel() {

    private val _result = MutableLiveData<BenchmarkResult>()
    val result: LiveData<BenchmarkResult> = _result

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun runBenchmark(context: Context, category: String, algorithm: String, repetitions: Int) {
        val repo = BenchmarkRepository(context)
        repo.runBenchmark(category, algorithm, repetitions, {
            _result.postValue(it)
        }, {
            _error.postValue(it.message)
        })
    }
}