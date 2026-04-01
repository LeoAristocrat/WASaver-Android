package com.leo.wasaver.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leo.wasaver.data.AppPreferences
import com.leo.wasaver.data.StatusRepository
import com.leo.wasaver.data.model.MediaFilter
import com.leo.wasaver.data.model.SortOption
import com.leo.wasaver.data.model.StatusFile
import com.leo.wasaver.data.model.ThemePreference
import com.leo.wasaver.data.model.WhatsAppType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatusViewModel(application: Application) : AndroidViewModel(application) {

    val repository = StatusRepository(application)
    private val preferences = AppPreferences(application)
    val viewOnceWatcher = com.leo.wasaver.data.ViewOnceWatcher(application, repository)
    val settings = preferences.settings

    private val _selectedWhatsApp = MutableStateFlow(WhatsAppType.WHATSAPP)
    val selectedWhatsApp: StateFlow<WhatsAppType> = _selectedWhatsApp.asStateFlow()

    private val _selectedFilter = MutableStateFlow(MediaFilter.ALL)
    val selectedFilter: StateFlow<MediaFilter> = _selectedFilter.asStateFlow()

    private val _allStatuses = MutableStateFlow<List<StatusFile>>(emptyList())

    private val _filteredStatuses = MutableStateFlow<List<StatusFile>>(emptyList())
    val filteredStatuses: StateFlow<List<StatusFile>> = _filteredStatuses.asStateFlow()

    private val _savedStatuses = MutableStateFlow<List<StatusFile>>(emptyList())
    val savedStatuses: StateFlow<List<StatusFile>> = _savedStatuses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasPermission = MutableStateFlow<Map<WhatsAppType, Boolean>>(emptyMap())
    val hasPermission: StateFlow<Map<WhatsAppType, Boolean>> = _hasPermission.asStateFlow()

    private val _savedFilteredStatuses = MutableStateFlow<List<StatusFile>>(emptyList())
    val savedFilteredStatuses: StateFlow<List<StatusFile>> = _savedFilteredStatuses.asStateFlow()

    private val _savedFilter = MutableStateFlow(MediaFilter.ALL)
    val savedFilter: StateFlow<MediaFilter> = _savedFilter.asStateFlow()

    // View Once
    private val _viewOnceWhatsApp = MutableStateFlow(WhatsAppType.WHATSAPP)
    val viewOnceWhatsApp: StateFlow<WhatsAppType> = _viewOnceWhatsApp.asStateFlow()

    private val _viewOnceFilter = MutableStateFlow(MediaFilter.ALL)
    val viewOnceFilter: StateFlow<MediaFilter> = _viewOnceFilter.asStateFlow()

    private val _allViewOnceMedia = MutableStateFlow<List<StatusFile>>(emptyList())

    private val _filteredViewOnceMedia = MutableStateFlow<List<StatusFile>>(emptyList())
    val filteredViewOnceMedia: StateFlow<List<StatusFile>> = _filteredViewOnceMedia.asStateFlow()

    private val _viewOncePermissions = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val viewOncePermissions: StateFlow<Map<String, Boolean>> = _viewOncePermissions.asStateFlow()

    init {
        checkPermissions()

        // Combine allStatuses and selectedFilter to produce filteredStatuses
        viewModelScope.launch {
            combine(_allStatuses, _selectedFilter, settings) { statuses, filter, appSettings ->
                statuses
                    .filterBy(filter)
                    .sortedWithOption(appSettings.statusSort)
            }.collect { _filteredStatuses.value = it }
        }

        // Combine savedStatuses and savedFilter
        viewModelScope.launch {
            combine(_savedStatuses, _savedFilter, settings) { statuses, filter, appSettings ->
                statuses
                    .filterBy(filter)
                    .sortedWithOption(appSettings.savedSort)
            }.collect { _savedFilteredStatuses.value = it }
        }

        // Combine viewOnce media and filter
        viewModelScope.launch {
            combine(_allViewOnceMedia, _viewOnceFilter, settings) { media, filter, appSettings ->
                media
                    .filterBy(filter)
                    .sortedWithOption(appSettings.viewOnceSort)
            }.collect { _filteredViewOnceMedia.value = it }
        }
    }

    fun checkPermissions() {
        _hasPermission.value = mapOf(
            WhatsAppType.WHATSAPP to repository.hasPermission(WhatsAppType.WHATSAPP),
            WhatsAppType.WHATSAPP_BUSINESS to repository.hasPermission(WhatsAppType.WHATSAPP_BUSINESS)
        )
    }

    fun onPermissionGranted(uri: Uri) {
        repository.persistPermission(uri)
        checkPermissions()
        loadStatuses()
    }

    fun selectWhatsApp(type: WhatsAppType) {
        _selectedWhatsApp.value = type
        _selectedFilter.value = MediaFilter.ALL
        loadStatuses()
    }

    fun selectFilter(filter: MediaFilter) {
        _selectedFilter.value = filter
    }

    fun selectSavedFilter(filter: MediaFilter) {
        _savedFilter.value = filter
    }

    fun updateThemePreference(value: ThemePreference) {
        preferences.updateThemePreference(value)
    }

    fun updateStatusSort(value: SortOption) {
        preferences.updateStatusSort(value)
    }

    fun updateSavedSort(value: SortOption) {
        preferences.updateSavedSort(value)
    }

    fun updateViewOnceSort(value: SortOption) {
        preferences.updateViewOnceSort(value)
    }

    fun updateAutoRefreshStatuses(enabled: Boolean) {
        preferences.updateAutoRefreshStatuses(enabled)
    }

    fun updateAutoRefreshSaved(enabled: Boolean) {
        preferences.updateAutoRefreshSaved(enabled)
    }

    fun updateAutoRefreshViewOnce(enabled: Boolean) {
        preferences.updateAutoRefreshViewOnce(enabled)
    }

    fun loadStatuses() {
        viewModelScope.launch {
            _allStatuses.value = emptyList()
            _isLoading.value = true
            val statuses = withContext(Dispatchers.IO) {
                repository.loadStatuses(_selectedWhatsApp.value)
            }
            _allStatuses.value = statuses
            _isLoading.value = false
            
            // Debug: Log what was loaded
            val videoCount = statuses.count { it.isVideo }
            val photoCount = statuses.count { it.isImage }
            android.util.Log.d("StatusViewModel", "Loaded ${statuses.size} statuses: $photoCount photos, $videoCount videos")
            android.util.Log.d("StatusViewModel", "Current filter: ${_selectedFilter.value}")
        }
    }

    fun loadSavedStatuses() {
        viewModelScope.launch {
            _savedStatuses.value = emptyList()
            _isLoading.value = true
            val saved = withContext(Dispatchers.IO) {
                repository.loadSavedStatuses()
            }
            _savedStatuses.value = saved
            _isLoading.value = false
        }
    }

    // View Once methods
    fun checkViewOncePermissions() {
        val type = _viewOnceWhatsApp.value
        _viewOncePermissions.value = mapOf(
            "img" to repository.hasViewOnceImagePermission(type),
            "vid" to repository.hasViewOnceVideoPermission(type)
        )
    }

    fun onViewOncePermissionGranted(uri: Uri) {
        repository.persistPermission(uri)
        checkViewOncePermissions()
        loadViewOnceMedia()
    }

    fun selectViewOnceWhatsApp(type: WhatsAppType) {
        _viewOnceWhatsApp.value = type
        _viewOnceFilter.value = MediaFilter.ALL
        checkViewOncePermissions()
        loadViewOnceMedia()
    }

    fun selectViewOnceFilter(filter: MediaFilter) {
        _viewOnceFilter.value = filter
    }

    fun loadViewOnceMedia() {
        viewModelScope.launch {
            _isLoading.value = true
            val media = withContext(Dispatchers.IO) {
                repository.loadViewOnceMedia(_viewOnceWhatsApp.value)
            }
            _allViewOnceMedia.value = media
            _isLoading.value = false
        }
    }

    fun saveStatus(statusFile: StatusFile) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                repository.saveStatus(statusFile)
            }
            if (success) {
                Toast.makeText(
                    getApplication(),
                    "Status saved to Gallery/WASaver",
                    Toast.LENGTH_SHORT
                ).show()
                // Refresh to update saved state
                loadStatuses()
                loadSavedStatuses()
            } else {
                Toast.makeText(
                    getApplication(),
                    "Failed to save status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun saveStatuses(statusFiles: List<StatusFile>) {
        viewModelScope.launch {
            if (statusFiles.isEmpty()) return@launch

            val savedCount = withContext(Dispatchers.IO) {
                statusFiles.count { repository.saveStatus(it) }
            }

            if (savedCount > 0) {
                Toast.makeText(
                    getApplication(),
                    "$savedCount item${if (savedCount > 1) "s" else ""} saved to Gallery/WASaver",
                    Toast.LENGTH_SHORT
                ).show()
                loadStatuses()
                loadSavedStatuses()
                loadViewOnceMedia()
            } else {
                Toast.makeText(
                    getApplication(),
                    "Failed to save selected items",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun deleteSavedStatus(statusFile: StatusFile) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                repository.deleteSavedStatus(statusFile)
            }
            if (success) {
                Toast.makeText(
                    getApplication(),
                    "Status deleted",
                    Toast.LENGTH_SHORT
                ).show()
                loadSavedStatuses()
            } else {
                Toast.makeText(
                    getApplication(),
                    "Failed to delete status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun deleteSavedStatuses(statusFiles: List<StatusFile>) {
        viewModelScope.launch {
            val deletedCount = withContext(Dispatchers.IO) {
                repository.deleteSavedStatuses(statusFiles)
            }
            if (deletedCount > 0) {
                Toast.makeText(
                    getApplication(),
                    "$deletedCount status${if (deletedCount > 1) "es" else ""} deleted",
                    Toast.LENGTH_SHORT
                ).show()
                loadSavedStatuses()
            } else {
                Toast.makeText(
                    getApplication(),
                    "Failed to delete statuses",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun List<StatusFile>.filterBy(filter: MediaFilter): List<StatusFile> {
        return when (filter) {
            MediaFilter.ALL -> this
            MediaFilter.PHOTOS -> filter { it.isImage }
            MediaFilter.VIDEOS -> filter { it.isVideo }
        }
    }

    private fun List<StatusFile>.sortedWithOption(sortOption: SortOption): List<StatusFile> {
        return when (sortOption) {
            SortOption.NEWEST -> sortedByDescending { it.dateModified }
            SortOption.OLDEST -> sortedBy { it.dateModified }
            SortOption.NAME_ASC -> sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> sortedByDescending { it.name.lowercase() }
            SortOption.SIZE_DESC -> sortedByDescending { it.size }
            SortOption.SIZE_ASC -> sortedBy { it.size }
        }
    }
}

