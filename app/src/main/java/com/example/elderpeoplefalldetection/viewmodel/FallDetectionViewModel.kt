package com.example.elderpeoplefalldetection.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elderpeoplefalldetection.data.model.FallRecord
import com.example.elderpeoplefalldetection.repository.FallDetectionRepository
import com.example.elderpeoplefalldetection.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FallDetectionViewModel : ViewModel() {
    private val repository = FallDetectionRepository()
    
    private val _records = MutableStateFlow<List<FallRecord>>(emptyList())
    val records: StateFlow<List<FallRecord>> = _records.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()
    
    private val _latestHeartbeat = MutableStateFlow<Int?>(null)
    val latestHeartbeat: StateFlow<Int?> = _latestHeartbeat.asStateFlow()
    
    init {
        // Don't load immediately - let UI handle it
    }
    
    fun loadRecords() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                repository.getAllRecords(Constants.STUDENT_ID)
                    .onSuccess { records ->
                        val sortedRecords = records.sortedByDescending { 
                            it.createdAt ?: it.updatedAt ?: ""
                        }
                        _records.value = sortedRecords
                        // Update latest heartbeat
                        _latestHeartbeat.value = sortedRecords
                            .filter { it.heartbeat != null }
                            .maxByOrNull { it.createdAt ?: it.updatedAt ?: "" }
                            ?.heartbeat
                        _isLoading.value = false
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Unknown error occurred"
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load records"
                _isLoading.value = false
            }
        }
    }
    
    fun getFallRecords(): List<FallRecord> {
        return _records.value.filter { it.fallDetected }
    }
    
    fun deleteAllRecords() {
        viewModelScope.launch {
            try {
                _isDeleting.value = true
                _error.value = null
                
                repository.deleteAllRecords(Constants.STUDENT_ID)
                    .onSuccess {
                        _records.value = emptyList()
                        _latestHeartbeat.value = null
                        _isDeleting.value = false
                        // Reload records to refresh
                        loadRecords()
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Failed to delete records"
                        _isDeleting.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete records"
                _isDeleting.value = false
            }
        }
    }
    
}

