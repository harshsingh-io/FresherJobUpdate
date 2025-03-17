package com.harshsinghio.fresherjobupdate

import androidx.lifecycle.*
import com.harshsinghio.fresherjobupdate.model.ApplicationStatus
import com.harshsinghio.fresherjobupdate.model.JobPosting
import com.harshsinghio.fresherjobupdate.repository.JobPostingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*

enum class FilterType {
    ALL, BY_SOURCE, BY_DATE, BY_STATUS, FAVORITES
}

class JobFinderViewModel(private val repository: JobPostingRepository) : ViewModel() {

    // All job postings
    val allJobPostings = repository.allJobPostings

    // Unread job postings
    val unreadJobPostings = repository.unreadJobPostings

    // Unread count
    val unreadCount = repository.unreadCount

    // Favorites
    val favoriteJobPostings = repository.favoriteJobPostings

    // Selected job postings for multi-select
    private val _selectedJobPostings = MutableStateFlow<Set<Long>>(emptySet())
    val selectedJobPostings: StateFlow<Set<Long>> = _selectedJobPostings

    // Current job posting for detailed view
    private val _currentJobPosting = MutableStateFlow<JobPosting?>(null)
    val currentJobPosting: StateFlow<JobPosting?> = _currentJobPosting

    // Current filter
    private val _currentFilter = MutableStateFlow(FilterType.ALL)
    val currentFilter: StateFlow<FilterType> = _currentFilter

    // Current source filter
    private val _currentSourceFilter = MutableStateFlow("")

    // Current date filter
    private val _currentStartDate = MutableStateFlow<Date?>(null)
    private val _currentEndDate = MutableStateFlow<Date?>(null)

    // Current status filter
    private val _currentStatusFilter = MutableStateFlow<ApplicationStatus?>(null)

    // Multi-select mode
    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode

    // Get job postings by filter
    // Fixed filteredJobPostings implementation:
    val filteredJobPostings = combine(
        allJobPostings,
        _currentFilter,
        _currentSourceFilter,
        _currentStartDate,
        _currentEndDate,
        _currentStatusFilter
    ) { array ->
        // Now receiving a single array parameter
        val postings = array[0] as List<JobPosting>
        val filterType = array[1] as FilterType
        val source = array[2] as String
        val startDate = array[3] as Date?
        val endDate = array[4] as Date?
        val status = array[5] as ApplicationStatus?

        when (filterType) {
            FilterType.ALL -> postings
            FilterType.BY_SOURCE -> postings.filter { it.source == source }
            FilterType.BY_DATE -> {
                if (startDate != null && endDate != null) {
                    postings.filter {
                        it.timestamp in startDate.time..endDate.time
                    }
                } else {
                    postings
                }
            }
            FilterType.BY_STATUS -> {
                if (status != null) {
                    postings.filter { it.applicationStatus == status }
                } else {
                    postings
                }
            }
            FilterType.FAVORITES -> postings.filter { it.isFavorite }
        }
    }

    // Filter methods
    fun setFilterBySource(source: String) {
        _currentSourceFilter.value = source
        _currentFilter.value = FilterType.BY_SOURCE
    }

    fun setFilterByDateRange(startDate: Date, endDate: Date) {
        _currentStartDate.value = startDate
        _currentEndDate.value = endDate
        _currentFilter.value = FilterType.BY_DATE
    }

    fun setFilterByStatus(status: ApplicationStatus) {
        _currentStatusFilter.value = status
        _currentFilter.value = FilterType.BY_STATUS
    }

    fun showFavorites() {
        _currentFilter.value = FilterType.FAVORITES
    }

    fun resetFilter() {
        _currentFilter.value = FilterType.ALL
        _currentSourceFilter.value = ""
        _currentStartDate.value = null
        _currentEndDate.value = null
        _currentStatusFilter.value = null
    }

    // Mark job posting as read
    fun markAsRead(id: Long) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    // Set current job posting for detailed view
    fun setCurrentJobPosting(jobPosting: JobPosting) {
        _currentJobPosting.value = jobPosting
        markAsRead(jobPosting.id)
    }

    // Clear current job posting
    fun clearCurrentJobPosting() {
        _currentJobPosting.value = null
    }

    // Delete job posting
    fun delete(jobPosting: JobPosting) {
        viewModelScope.launch {
            repository.delete(jobPosting)
        }
    }

    // Update application status
    fun updateApplicationStatus(id: Long, status: ApplicationStatus) {
        viewModelScope.launch {
            repository.updateApplicationStatus(id, status)
        }
    }

    // Toggle favorite status
    fun toggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(id, isFavorite)
        }
    }

    // Multi-select operations
    fun toggleMultiSelectMode() {
        _isMultiSelectMode.value = !_isMultiSelectMode.value
        if (!_isMultiSelectMode.value) {
            _selectedJobPostings.value = emptySet()
        }
    }

    fun toggleSelection(id: Long) {
        val currentSelection = _selectedJobPostings.value.toMutableSet()
        if (currentSelection.contains(id)) {
            currentSelection.remove(id)
        } else {
            currentSelection.add(id)
        }
        _selectedJobPostings.value = currentSelection
    }

    fun selectAll(ids: List<Long>) {
        _selectedJobPostings.value = ids.toSet()
    }

    fun clearSelection() {
        _selectedJobPostings.value = emptySet()
    }

    fun deleteSelectedJobPostings() {
        viewModelScope.launch {
            repository.deleteMultiple(_selectedJobPostings.value.toList())
            _selectedJobPostings.value = emptySet()
            _isMultiSelectMode.value = false
        }
    }

    fun markSelectedAsRead() {
        viewModelScope.launch {
            repository.markMultipleAsRead(_selectedJobPostings.value.toList())
        }
    }

    fun updateSelectedApplicationStatus(status: ApplicationStatus) {
        viewModelScope.launch {
            repository.updateMultipleApplicationStatus(_selectedJobPostings.value.toList(), status)
        }
    }
}

// Factory for creating the ViewModel
class JobFinderViewModelFactory(private val repository: JobPostingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobFinderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JobFinderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}