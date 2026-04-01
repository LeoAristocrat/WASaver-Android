package com.leo.wasaver.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.OptIn as AndroidOptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.leo.wasaver.data.model.StatusFile
import com.leo.wasaver.ui.theme.AppInk
import com.leo.wasaver.ui.theme.AppShellGradient
import com.leo.wasaver.ui.theme.AppShellGradientDark
import com.leo.wasaver.ui.theme.DirectFeatureGradient
import com.leo.wasaver.ui.theme.PastelMint
import com.leo.wasaver.ui.theme.PastelPeach
import com.leo.wasaver.ui.theme.StatusFeatureGradient
import com.leo.wasaver.viewmodel.StatusViewModel

@Composable
fun StatusViewerScreen(
    statusFiles: List<StatusFile>,
    initialIndex: Int,
    viewModel: StatusViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { statusFiles.size }
    )
    val currentStatus = statusFiles.getOrNull(pagerState.currentPage) ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(if (isDark) AppShellGradientDark else AppShellGradient))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val status = statusFiles[page]
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 96.dp),
                shape = RoundedCornerShape(30.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                tonalElevation = 8.dp,
                shadowElevation = 18.dp
            ) {
                if (status.isVideo) {
                    VideoPlayer(status = status, isCurrentPage = page == pagerState.currentPage)
                } else {
                    ImageViewer(status = status)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                tonalElevation = 4.dp
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = if (currentStatus.isVideo) "Video status" else "Photo status",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${pagerState.currentPage + 1} / ${statusFiles.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            tonalElevation = 10.dp,
            shadowElevation = 18.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(
                    icon = Icons.Default.Share,
                    label = "Repost",
                    containerColor = StatusFeatureGradient.first(),
                    contentColor = AppInk,
                    onClick = { repostStatus(context, currentStatus) }
                )

                ActionButton(
                    icon = Icons.AutoMirrored.Filled.Send,
                    label = "Share",
                    containerColor = DirectFeatureGradient.first(),
                    contentColor = AppInk,
                    onClick = { shareStatus(context, currentStatus) }
                )

                ActionButton(
                    icon = if (currentStatus.isSaved) Icons.Default.CheckCircle else Icons.Default.Download,
                    label = if (currentStatus.isSaved) "Saved" else "Save",
                    containerColor = if (currentStatus.isSaved) PastelPeach else PastelMint,
                    contentColor = AppInk,
                    enabled = !currentStatus.isSaved,
                    onClick = { viewModel.saveStatus(currentStatus) }
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(54.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (enabled) containerColor else MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (enabled) contentColor else MaterialTheme.colorScheme.outline,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                disabledContentColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ImageViewer(status: StatusFile) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(status.uri)
                .crossfade(true)
                .build(),
            contentDescription = status.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@AndroidOptIn(UnstableApi::class)
@Composable
private fun VideoPlayer(status: StatusFile, isCurrentPage: Boolean) {
    val context = LocalContext.current

    val exoPlayer = remember(status.uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(status.uri))
            prepare()
        }
    }

    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) exoPlayer.play() else exoPlayer.pause()
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(30.dp))
    )
}

private fun repostStatus(context: Context, status: StatusFile) {
    try {
        val mimeType = if (status.isVideo) "video/*" else "image/*"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, status.uri)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Repost to WhatsApp"))
    } catch (_: Exception) {
        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
    }
}

private fun shareStatus(context: Context, status: StatusFile) {
    try {
        val mimeType = if (status.isVideo) "video/*" else "image/*"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, status.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    } catch (_: Exception) {
        Toast.makeText(context, "Failed to share", Toast.LENGTH_SHORT).show()
    }
}
