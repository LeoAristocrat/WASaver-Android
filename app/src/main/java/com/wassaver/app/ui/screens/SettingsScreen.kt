package com.wassaver.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wassaver.app.data.model.SortOption
import com.wassaver.app.data.model.ThemePreference
import com.wassaver.app.viewmodel.StatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: StatusViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SettingsSectionCard(
                    title = "Appearance",
                    subtitle = "Control how the app looks across your device."
                ) {
                    ChipGroup(
                        options = ThemePreference.entries,
                        selected = settings.themePreference,
                        labelFor = { it.displayName },
                        onSelect = viewModel::updateThemePreference
                    )
                }
            }

            item {
                SettingsSectionCard(
                    title = "Auto Refresh",
                    subtitle = "Decide which screens should reload content automatically."
                ) {
                    ToggleRow(
                        title = "Status Viewer",
                        subtitle = "Load statuses automatically when you open the screen.",
                        checked = settings.autoRefreshStatuses,
                        onCheckedChange = viewModel::updateAutoRefreshStatuses
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Saved Statuses",
                        subtitle = "Refresh saved items when you open the screen.",
                        checked = settings.autoRefreshSaved,
                        onCheckedChange = viewModel::updateAutoRefreshSaved
                    )
                    Divider(modifier = Modifier.padding(vertical = 10.dp))
                    ToggleRow(
                        title = "Media Browser",
                        subtitle = "Scan private media folders automatically when available.",
                        checked = settings.autoRefreshViewOnce,
                        onCheckedChange = viewModel::updateAutoRefreshViewOnce
                    )
                }
            }

            item {
                SettingsSectionCard(
                    title = "Default Sorting",
                    subtitle = "Choose how each media screen should sort items by default."
                ) {
                    SortSettingGroup(
                        label = "Status Viewer",
                        selected = settings.statusSort,
                        onSelect = viewModel::updateStatusSort
                    )
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    SortSettingGroup(
                        label = "Saved Statuses",
                        selected = settings.savedSort,
                        onSelect = viewModel::updateSavedSort
                    )
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    SortSettingGroup(
                        label = "Media Browser",
                        selected = settings.viewOnceSort,
                        onSelect = viewModel::updateViewOnceSort
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SortSettingGroup(
    label: String,
    selected: SortOption,
    onSelect: (SortOption) -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(10.dp))
    ChipGroup(
        options = SortOption.entries,
        selected = selected,
        labelFor = { it.displayName },
        onSelect = onSelect
    )
}

@Composable
private fun <T> ChipGroup(
    options: List<T>,
    selected: T,
    labelFor: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { option ->
                    FilterChip(
                        selected = option == selected,
                        onClick = { onSelect(option) },
                        label = { Text(labelFor(option)) },
                        leadingIcon = if (option == selected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

