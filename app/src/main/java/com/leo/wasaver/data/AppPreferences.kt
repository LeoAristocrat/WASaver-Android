package com.leo.wasaver.data

import android.content.Context
import com.leo.wasaver.data.model.AppSettings
import com.leo.wasaver.data.model.SortOption
import com.leo.wasaver.data.model.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun updateThemePreference(value: ThemePreference) = update {
        it.copy(themePreference = value)
    }

    fun updateStatusSort(value: SortOption) = update {
        it.copy(statusSort = value)
    }

    fun updateSavedSort(value: SortOption) = update {
        it.copy(savedSort = value)
    }

    fun updateViewOnceSort(value: SortOption) = update {
        it.copy(viewOnceSort = value)
    }

    fun updateAutoRefreshStatuses(enabled: Boolean) = update {
        it.copy(autoRefreshStatuses = enabled)
    }

    fun updateAutoRefreshSaved(enabled: Boolean) = update {
        it.copy(autoRefreshSaved = enabled)
    }

    fun updateAutoRefreshViewOnce(enabled: Boolean) = update {
        it.copy(autoRefreshViewOnce = enabled)
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        prefs.edit()
            .putString(KEY_THEME, updated.themePreference.name)
            .putString(KEY_STATUS_SORT, updated.statusSort.name)
            .putString(KEY_SAVED_SORT, updated.savedSort.name)
            .putString(KEY_VIEW_ONCE_SORT, updated.viewOnceSort.name)
            .putBoolean(KEY_AUTO_REFRESH_STATUSES, updated.autoRefreshStatuses)
            .putBoolean(KEY_AUTO_REFRESH_SAVED, updated.autoRefreshSaved)
            .putBoolean(KEY_AUTO_REFRESH_VIEW_ONCE, updated.autoRefreshViewOnce)
            .apply()
        _settings.value = updated
    }

    private fun loadSettings(): AppSettings {
        return AppSettings(
            themePreference = prefs.getString(KEY_THEME, null)
                ?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
                ?: ThemePreference.SYSTEM,
            autoRefreshStatuses = prefs.getBoolean(KEY_AUTO_REFRESH_STATUSES, true),
            autoRefreshSaved = prefs.getBoolean(KEY_AUTO_REFRESH_SAVED, true),
            autoRefreshViewOnce = prefs.getBoolean(KEY_AUTO_REFRESH_VIEW_ONCE, true),
            statusSort = prefs.getString(KEY_STATUS_SORT, null)
                ?.let { runCatching { SortOption.valueOf(it) }.getOrNull() }
                ?: SortOption.NEWEST,
            savedSort = prefs.getString(KEY_SAVED_SORT, null)
                ?.let { runCatching { SortOption.valueOf(it) }.getOrNull() }
                ?: SortOption.NEWEST,
            viewOnceSort = prefs.getString(KEY_VIEW_ONCE_SORT, null)
                ?.let { runCatching { SortOption.valueOf(it) }.getOrNull() }
                ?: SortOption.NEWEST
        )
    }

    private companion object {
        const val PREFS_NAME = "wassaver_settings"
        const val KEY_THEME = "theme_preference"
        const val KEY_STATUS_SORT = "status_sort"
        const val KEY_SAVED_SORT = "saved_sort"
        const val KEY_VIEW_ONCE_SORT = "view_once_sort"
        const val KEY_AUTO_REFRESH_STATUSES = "auto_refresh_statuses"
        const val KEY_AUTO_REFRESH_SAVED = "auto_refresh_saved"
        const val KEY_AUTO_REFRESH_VIEW_ONCE = "auto_refresh_view_once"
    }
}

