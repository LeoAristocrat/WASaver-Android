package com.leo.wasaver.ui.screens

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.leo.wasaver.data.StatusRepository
import com.leo.wasaver.data.model.MediaFilter
import com.leo.wasaver.data.model.StatusFile
import com.leo.wasaver.data.model.WhatsAppType
import com.leo.wasaver.ui.theme.AppInk
import com.leo.wasaver.ui.theme.AppShellGradient
import com.leo.wasaver.ui.theme.AppShellGradientDark
import com.leo.wasaver.ui.theme.PastelCoral
import com.leo.wasaver.ui.theme.PastelMint
import com.leo.wasaver.ui.theme.StatusFeatureGradient
import com.leo.wasaver.viewmodel.StatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: StatusViewModel,
    onStatusClick: (StatusFile, Int, List<StatusFile>) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val selectedWhatsApp by viewModel.selectedWhatsApp.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val statuses by viewModel.filteredStatuses.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val permissions by viewModel.hasPermission.collectAsState()
    val hasCurrentPermission = permissions[selectedWhatsApp] == true
    var searchQuery by remember { mutableStateOf("") }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<String>()) }
    val visibleStatuses = remember(statuses, searchQuery) {
        val query = searchQuery.trim().lowercase()
        if (query.isBlank()) statuses
        else statuses.filter { it.name.lowercase().contains(query) }
    }
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    LaunchedEffect(selectionMode) {
        if (!selectionMode) {
            selectedItems = emptySet()
        }
    }

    // Auto-load statuses when screen appears and has permission
    LaunchedEffect(hasCurrentPermission, settings.autoRefreshStatuses) {
        if (hasCurrentPermission && settings.autoRefreshStatuses) {
            viewModel.loadStatuses()
        }
    }

    // SAF folder picker launcher
    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.onPermissionGranted(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(if (isDark) AppShellGradientDark else AppShellGradient))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
        // ── Top App Bar ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Column {
                // Title bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = if (onBack != null) 4.dp else 16.dp, vertical = if (onBack != null) 4.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectionMode) {
                            IconButton(onClick = { selectionMode = false }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Exit selection",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${selectedItems.size} selected",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(StatusFeatureGradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = null,
                                tint = AppInk,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Status Viewer",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Swipe through statuses in a calmer gallery.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (selectionMode) {
                        if (visibleStatuses.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    selectedItems = if (selectedItems.size == visibleStatuses.size) {
                                        emptySet()
                                    } else {
                                        visibleStatuses.map { it.uri.toString() }.toSet()
                                    }
                                }
                            ) {
                                Text(
                                    if (selectedItems.size == visibleStatuses.size) "Deselect All" else "Select All",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                shareStatuses(
                                    context = context,
                                    statuses = visibleStatuses.filter { it.uri.toString() in selectedItems }
                                )
                            },
                            enabled = selectedItems.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share selected",
                                tint = if (selectedItems.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.saveStatuses(
                                    visibleStatuses.filter { it.uri.toString() in selectedItems }
                                )
                                selectionMode = false
                            },
                            enabled = selectedItems.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Save selected",
                                tint = if (selectedItems.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        if (visibleStatuses.isNotEmpty()) {
                            IconButton(onClick = { selectionMode = true }) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Select items",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.loadStatuses() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // ── WA / WAB Tab Row ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WhatsAppType.entries.forEach { type ->
                        val isSelected = selectedWhatsApp == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectWhatsApp(type) },
                            label = {
                                Text(
                                    text = type.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (type == WhatsAppType.WHATSAPP)
                                        Icons.AutoMirrored.Default.Chat else Icons.Default.Business,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ── ALL / Photos / Videos Filter ──
                if (hasCurrentPermission) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MediaFilter.entries.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            AssistChip(
                                onClick = { viewModel.selectFilter(filter) },
                                label = {
                                    Text(
                                        text = filter.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (filter) {
                                            MediaFilter.ALL -> Icons.Default.GridView
                                            MediaFilter.PHOTOS -> Icons.Default.Image
                                            MediaFilter.VIDEOS -> Icons.Default.VideoLibrary
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    labelColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    leadingIconContentColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null
                            )
                        }
                    }

                    MediaSearchAndSortBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        sortOption = settings.statusSort,
                        onSortSelected = viewModel::updateStatusSort,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // ── Content Area ──
        if (!hasCurrentPermission) {
            // Permission needed UI
            PermissionRequestCard(
                whatsAppType = selectedWhatsApp,
                onGrantClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val initialUri = StatusRepository.getInitialUri(selectedWhatsApp)
                        safLauncher.launch(initialUri)
                    } else {
                        // For older Android, directly load
                        viewModel.loadStatuses()
                    }
                }
            )
        } else if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Loading statuses...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (visibleStatuses.isEmpty()) {
            EmptyStatusView(selectedFilter, hasSearch = searchQuery.isNotBlank())
        } else {
            // Status Grid
            StatusGrid(
                statuses = visibleStatuses,
                selectionMode = selectionMode,
                selectedItems = selectedItems,
                onStatusClick = onStatusClick,
                onStatusSelectionToggle = { status ->
                    val key = status.uri.toString()
                    selectedItems = if (key in selectedItems) selectedItems - key else selectedItems + key
                },
                onSaveClick = { viewModel.saveStatus(it) }
            )
        }
        }
    }

}

@Composable
private fun PermissionRequestCard(
    whatsAppType: WhatsAppType,
    onGrantClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(colors = StatusFeatureGradient)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = AppInk,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Grant Access",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "WASaver needs access to ${whatsAppType.displayName} status folder to show your contacts' statuses.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "When the folder picker opens, simply tap \"Use this folder\" to grant access.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onGrantClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Open ${whatsAppType.displayName} Folder",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStatusView(filter: MediaFilter, hasSearch: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = when (filter) {
                    MediaFilter.ALL -> Icons.Outlined.FolderOff
                    MediaFilter.PHOTOS -> Icons.Outlined.HideImage
                    MediaFilter.VIDEOS -> Icons.Outlined.VideocamOff
                },
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (filter) {
                    MediaFilter.ALL -> if (hasSearch) "No Matching Statuses" else "No Statuses Found"
                    MediaFilter.PHOTOS -> if (hasSearch) "No Matching Photos" else "No Photos Found"
                    MediaFilter.VIDEOS -> if (hasSearch) "No Matching Videos" else "No Videos Found"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasSearch) {
                    "Try a different filename or clear the search to see more results."
                } else {
                    "View some statuses on WhatsApp first, then come back here to save them."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun StatusGrid(
    statuses: List<StatusFile>,
    selectionMode: Boolean = false,
    selectedItems: Set<String> = emptySet(),
    onStatusClick: (StatusFile, Int, List<StatusFile>) -> Unit,
    onStatusSelectionToggle: (StatusFile) -> Unit = {},
    onSaveClick: (StatusFile) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(statuses, key = { it.uri.toString() }) { status ->
            StatusGridItem(
                status = status,
                selectionMode = selectionMode,
                isSelected = status.uri.toString() in selectedItems,
                onClick = {
                    if (selectionMode) {
                        onStatusSelectionToggle(status)
                    } else {
                        val index = statuses.indexOf(status)
                        onStatusClick(status, index, statuses)
                    }
                },
                onSaveClick = { onSaveClick(status) }
            )
        }
    }
}

@Composable
private fun StatusGridItem(
    status: StatusFile,
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Thumbnail
            val imageRequest = if (status.isVideo) {
                ImageRequest.Builder(context)
                    .data(status.uri)
                    .decoderFactory(VideoFrameDecoder.Factory())
                    .crossfade(true)
                    .build()
            } else {
                ImageRequest.Builder(context)
                    .data(status.uri)
                    .crossfade(true)
                    .build()
            }

            AsyncImage(
                model = imageRequest,
                contentDescription = status.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (selectionMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                            else Color.Transparent
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Video play icon overlay
            if (status.isVideo && !selectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Bottom gradient overlay with save button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (status.isVideo) PastelCoral else PastelMint,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = if (status.isVideo) "VIDEO" else "PHOTO",
                            color = AppInk,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Save button
                    if (status.isSaved) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Saved",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    } else if (!selectionMode) {
                        IconButton(
                            onClick = onSaveClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Save",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun shareStatuses(context: android.content.Context, statuses: List<StatusFile>) {
    if (statuses.isEmpty()) return

    val uris = ArrayList(statuses.map { it.uri })
    val hasMixedMedia = statuses.any { it.isVideo } && statuses.any { it.isImage }
    val mimeType = when {
        hasMixedMedia -> "*/*"
        statuses.first().isVideo -> "video/*"
        else -> "image/*"
    }

    try {
        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uris.first())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = mimeType
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    } catch (_: Exception) {
        Toast.makeText(context, "Failed to share selected items", Toast.LENGTH_SHORT).show()
    }
}

