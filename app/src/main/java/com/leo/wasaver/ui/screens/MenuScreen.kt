package com.leo.wasaver.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leo.wasaver.data.UpdateChecker
import com.leo.wasaver.ui.theme.AppInk
import com.leo.wasaver.ui.theme.AboutFeatureGradient
import com.leo.wasaver.ui.theme.DeletedFeatureGradient
import com.leo.wasaver.ui.theme.DirectFeatureGradient
import com.leo.wasaver.ui.theme.MediaFeatureGradient
import com.leo.wasaver.ui.theme.SavedFeatureGradient
import com.leo.wasaver.ui.theme.SettingsFeatureGradient
import com.leo.wasaver.ui.theme.StatusFeatureGradient
import com.leo.wasaver.ui.theme.UpdateFeatureGradient
import com.leo.wasaver.ui.theme.AppShellGradient
import com.leo.wasaver.ui.theme.AppShellGradientDark

enum class MenuDestination {
    STATUS_VIEWER,
    MEDIA_BROWSER,
    SAVED_STATUSES,
    DELETED_MESSAGES,
    DIRECT_CHAT,
    UPDATE,
    ABOUT,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onNavigate: (MenuDestination) -> Unit
) {
    val context = LocalContext.current
    val currentVersion = remember { UpdateChecker.getCurrentVersion(context) }
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    val primaryItems = listOf(
        DashboardItem(
            icon = Icons.Default.Home,
            title = "Status Viewer",
            description = "Browse fresh statuses with calmer cards and faster save actions.",
            gradientColors = StatusFeatureGradient,
            destination = MenuDestination.STATUS_VIEWER
        ),
        DashboardItem(
            icon = Icons.Default.PermMedia,
            title = "Media Browser",
            description = "Open private photos and videos in one softer pastel gallery.",
            gradientColors = MediaFeatureGradient,
            destination = MenuDestination.MEDIA_BROWSER
        ),
        DashboardItem(
            icon = Icons.Default.Bookmark,
            title = "Saved Statuses",
            description = "Keep your saved media tidy with search, filters, and bulk actions.",
            gradientColors = SavedFeatureGradient,
            destination = MenuDestination.SAVED_STATUSES
        ),
        DashboardItem(
            icon = Icons.Default.DeleteSweep,
            title = "Deleted Messages",
            description = "Review captured deleted chats in a cleaner timeline.",
            gradientColors = DeletedFeatureGradient,
            destination = MenuDestination.DELETED_MESSAGES
        ),
        DashboardItem(
            icon = Icons.AutoMirrored.Filled.Send,
            title = "Direct Message",
            description = "Message unsaved numbers without jumping through extra steps.",
            gradientColors = DirectFeatureGradient,
            destination = MenuDestination.DIRECT_CHAT
        )
    )

    val supportItems = listOf(
        DashboardItem(
            icon = Icons.Default.SystemUpdate,
            title = "Check for Updates",
            description = "Current version: v$currentVersion",
            gradientColors = UpdateFeatureGradient,
            destination = MenuDestination.UPDATE
        ),
        DashboardItem(
            icon = Icons.Default.Info,
            title = "About",
            description = "Version details, links, and credit information.",
            gradientColors = AboutFeatureGradient,
            destination = MenuDestination.ABOUT
        ),
        DashboardItem(
            icon = Icons.Default.Settings,
            title = "Settings",
            description = "Theme, refresh, and sorting preferences.",
            gradientColors = SettingsFeatureGradient,
            destination = MenuDestination.SETTINGS
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(if (isDark) AppShellGradientDark else AppShellGradient))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                shape = RoundedCornerShape(30.dp),
                tonalElevation = 8.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Brush.linearGradient(StatusFeatureGradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = null,
                                tint = AppInk,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "WASaver",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Save statuses, browse media, and open chats faster.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your WhatsApp utility hub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Status saving, private media, deleted messages, and direct chat in one place.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "v$currentVersion",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            DashboardSection(title = "Browse") {
                primaryItems.forEach { item ->
                    MenuItemCard(item = item, onClick = { onNavigate(item.destination) })
                }
            }

            DashboardSection(title = "App") {
                supportItems.forEach { item ->
                    MenuItemCard(item = item, onClick = { onNavigate(item.destination) })
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Built by Leo Aristocrat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Designed to feel gentler on the eyes.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DashboardSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        content()
    }
}

@Composable
private fun MenuItemCard(
    item: DashboardItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 450f),
        label = "menuCardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(item.gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = AppInk,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class DashboardItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val gradientColors: List<Color>,
    val destination: MenuDestination
)
