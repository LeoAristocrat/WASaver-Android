package com.leo.wasaver.data.model

enum class ThemePreference(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class SortOption(val displayName: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    NAME_ASC("Name A-Z"),
    NAME_DESC("Name Z-A"),
    SIZE_DESC("Largest"),
    SIZE_ASC("Smallest")
}

data class AppSettings(
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val autoRefreshStatuses: Boolean = true,
    val autoRefreshSaved: Boolean = true,
    val autoRefreshViewOnce: Boolean = true,
    val statusSort: SortOption = SortOption.NEWEST,
    val savedSort: SortOption = SortOption.NEWEST,
    val viewOnceSort: SortOption = SortOption.NEWEST
)

