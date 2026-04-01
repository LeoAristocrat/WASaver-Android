package com.leo.wasaver.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leo.wasaver.data.StatusRepository
import com.leo.wasaver.data.model.MediaFilter
import com.leo.wasaver.data.model.StatusFile
import com.leo.wasaver.data.model.WhatsAppType
import com.leo.wasaver.ui.theme.AppInk
import com.leo.wasaver.ui.theme.StatusFeatureGradient
import com.leo.wasaver.viewmodel.StatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewOnceScreen(
    viewModel: StatusViewModel,
    onStatusClick: (StatusFile, Int, List<StatusFile>) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val selectedWhatsApp by viewModel.viewOnceWhatsApp.collectAsState()
    val selectedFilter by viewModel.viewOnceFilter.collectAsState()
    val media by viewModel.filteredViewOnceMedia.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val permissions by viewModel.viewOncePermissions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<String>()) }
    val context = LocalContext.current
    val visibleMedia = remember(media, searchQuery) {
        val query = searchQuery.trim().lowercase()
        if (query.isBlank()) media
        else media.filter { it.name.lowercase().contains(query) }
    }

    LaunchedEffect(selectionMode) {
        if (!selectionMode) {
            selectedItems = emptySet()
        }
    }

    val hasImagePerm = permissions["img"] == true
    val hasVideoPerm = permissions["vid"] == true
    val hasAnyPermission = hasImagePerm || hasVideoPerm

    // SAF launchers for images and videos private folders
    val imgSafLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.onViewOncePermissionGranted(it) }
    }

    val vidSafLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.onViewOncePermissionGranted(it) }
    }

    // Check permissions on first load
    LaunchedEffect(Unit) {
        viewModel.checkViewOncePermissions()
    }

    // Re-check and load when WA type changes
    LaunchedEffect(selectedWhatsApp) {
        viewModel.checkViewOncePermissions()
    }

    // Auto-load when permissions become available
    LaunchedEffect(hasImagePerm, hasVideoPerm, settings.autoRefreshViewOnce) {
        if ((hasImagePerm || hasVideoPerm) && settings.autoRefreshViewOnce) {
            viewModel.loadViewOnceMedia()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Column {
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
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${selectedItems.size} selected",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Icon(
                            imageVector = Icons.Default.PermMedia,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Media Browser",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (selectionMode) {
                        if (visibleMedia.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    selectedItems = if (selectedItems.size == visibleMedia.size) {
                                        emptySet()
                                    } else {
                                        visibleMedia.map { it.uri.toString() }.toSet()
                                    }
                                }
                            ) {
                                Text(
                                    if (selectedItems.size == visibleMedia.size) "Deselect All" else "Select All",
                                    color = Color.White
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                shareStatuses(
                                    context = context,
                                    statuses = visibleMedia.filter { it.uri.toString() in selectedItems }
                                )
                            },
                            enabled = selectedItems.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share selected",
                                tint = if (selectedItems.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.saveStatuses(
                                    visibleMedia.filter { it.uri.toString() in selectedItems }
                                )
                                selectionMode = false
                            },
                            enabled = selectedItems.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Save selected",
                                tint = if (selectedItems.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    } else if (hasAnyPermission) {
                        if (visibleMedia.isNotEmpty()) {
                            IconButton(onClick = { selectionMode = true }) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Select items",
                                    tint = Color.White
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.loadViewOnceMedia() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White
                            )
                        }
                    }
                }

                // WA / WAB Tab Row
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
                            onClick = { viewModel.selectViewOnceWhatsApp(type) },
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
                                selectedContainerColor = Color.White,
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color.White.copy(alpha = 0.15f),
                                labelColor = Color.White,
                                iconColor = Color.White
                            ),
                            border = null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Media filter chips
                if (hasAnyPermission) {
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
                                onClick = { viewModel.selectViewOnceFilter(filter) },
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
                                    containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f),
                                    labelColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                    leadingIconContentColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                                ),
                                border = null
                            )
                        }
                    }

                    MediaSearchAndSortBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        sortOption = settings.viewOnceSort,
                        onSortSelected = viewModel::updateViewOnceSort,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Content
        if (!hasAnyPermission) {
            // Permission setup
            ViewOncePermissionSetup(
                selectedWhatsApp = selectedWhatsApp,
                hasImagePerm = hasImagePerm,
                hasVideoPerm = hasVideoPerm,
                onGrantImageAccess = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        imgSafLauncher.launch(StatusRepository.getViewOnceImageInitialUri(selectedWhatsApp))
                    }
                },
                onGrantVideoAccess = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        vidSafLauncher.launch(StatusRepository.getViewOnceVideoInitialUri(selectedWhatsApp))
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
                        "Scanning media folders...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (visibleMedia.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PermMedia,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "No Media Found" else "No Matching Media",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isBlank()) {
                            "Photos and videos from WhatsApp's private media folders will appear here. Open them in WhatsApp first."
                        } else {
                            "Try a different filename or clear the search to see all media."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )

                    // Show buttons to grant missing permissions
                    if (!hasImagePerm || !hasVideoPerm) {
                        Spacer(modifier = Modifier.height(20.dp))
                        if (!hasImagePerm) {
                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        imgSafLauncher.launch(StatusRepository.getViewOnceImageInitialUri(selectedWhatsApp))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(0.7f)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Grant Image Access")
                            }
                        }
                        if (!hasVideoPerm) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        vidSafLauncher.launch(StatusRepository.getViewOnceVideoInitialUri(selectedWhatsApp))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(0.7f)
                            ) {
                                Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Grant Video Access")
                            }
                        }
                    }
                }
            }
        } else {
            StatusGrid(
                statuses = visibleMedia,
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

@Composable
private fun ViewOncePermissionSetup(
    selectedWhatsApp: WhatsAppType,
    hasImagePerm: Boolean,
    hasVideoPerm: Boolean,
    onGrantImageAccess: () -> Unit,
    onGrantVideoAccess: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = StatusFeatureGradient
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PermMedia,
                        contentDescription = null,
                        tint = AppInk,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Media Access",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "WASaver needs access to ${selectedWhatsApp.displayName}'s private media folders to browse and save photos & videos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "You need to grant access to both the Images and Video Private folders separately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Image folder access
                Button(
                    onClick = onGrantImageAccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !hasImagePerm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        if (hasImagePerm) Icons.Default.CheckCircle else Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (hasImagePerm) "✓ Images Access Granted" else "Grant Image Folder Access",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Video folder access
                Button(
                    onClick = onGrantVideoAccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !hasVideoPerm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(
                        if (hasVideoPerm) Icons.Default.CheckCircle else Icons.Default.VideoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (hasVideoPerm) "✓ Videos Access Granted" else "Grant Video Folder Access",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Private media may be temporarily stored by WhatsApp. Save them quickly before they are deleted.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

