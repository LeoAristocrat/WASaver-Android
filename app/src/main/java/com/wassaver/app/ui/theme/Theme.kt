package com.wassaver.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val PastelMint = Color(0xFF7FD8BE)
val PastelSage = Color(0xFFBEE3D0)
val PastelSky = Color(0xFFA9D6FF)
val PastelLavender = Color(0xFFD8C4FF)
val PastelPeach = Color(0xFFFFD6B3)
val PastelRose = Color(0xFFFFC4D6)
val PastelCoral = Color(0xFFFF8E8E)
val AppInk = Color(0xFF223047)
val AppInkSoft = Color(0xFF5D6B82)
val AppDarkBackground = Color(0xFF111827)
val AppDarkSurface = Color(0xFF172033)
val AppDarkSurfaceHigh = Color(0xFF1E2940)
val AppShellGradient = listOf(
    Color(0xFFFDF7FF),
    Color(0xFFF6FBFF),
    Color(0xFFFFF8F1)
)
val AppShellGradientDark = listOf(
    Color(0xFF101827),
    Color(0xFF141F33),
    Color(0xFF1B2238)
)
val StatusFeatureGradient = listOf(Color(0xFF9ADBC8), Color(0xFFB3E5D7))
val MediaFeatureGradient = listOf(Color(0xFFAFCBFF), Color(0xFFD6C6FF))
val SavedFeatureGradient = listOf(Color(0xFFFFD6B8), Color(0xFFFFE8CD))
val DeletedFeatureGradient = listOf(Color(0xFFFFC4D6), Color(0xFFE0C7FF))
val DirectFeatureGradient = listOf(Color(0xFFAEE8F5), Color(0xFFC7F0E8))
val UpdateFeatureGradient = listOf(Color(0xFFB7E4C7), Color(0xFFE4F7CF))
val AboutFeatureGradient = listOf(Color(0xFFD2D8F7), Color(0xFFF0DDF8))
val SettingsFeatureGradient = listOf(Color(0xFFFFD6D6), Color(0xFFFFE8B8))

private val DarkColorScheme = darkColorScheme(
    primary = PastelMint,
    onPrimary = AppInk,
    primaryContainer = Color(0xFF204A42),
    onPrimaryContainer = Color(0xFFD7FFF2),
    secondary = PastelLavender,
    onSecondary = AppInk,
    secondaryContainer = Color(0xFF3D3558),
    onSecondaryContainer = Color(0xFFF0E8FF),
    tertiary = PastelPeach,
    onTertiary = AppInk,
    tertiaryContainer = Color(0xFF5A4430),
    onTertiaryContainer = Color(0xFFFFE8D8),
    background = AppDarkBackground,
    onBackground = Color(0xFFF5F7FB),
    surface = AppDarkSurface,
    onSurface = Color(0xFFF5F7FB),
    surfaceVariant = AppDarkSurfaceHigh,
    onSurfaceVariant = Color(0xFFD1D8E5),
    error = Color(0xFFFFB3B8),
    onError = Color(0xFF680018),
    errorContainer = Color(0xFF8C2535),
    onErrorContainer = Color(0xFFFFD9DD),
    outline = Color(0xFF94A0B8),
    surfaceContainerLowest = Color(0xFF0D1422),
    surfaceContainerLow = Color(0xFF131C2C),
    surfaceContainer = Color(0xFF192335),
    surfaceContainerHigh = Color(0xFF202C41),
    surfaceContainerHighest = Color(0xFF293651)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5DAF96),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF7EF),
    onPrimaryContainer = AppInk,
    secondary = Color(0xFF8E7BC7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1EAFF),
    onSecondaryContainer = AppInk,
    tertiary = Color(0xFFE39A74),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEFE5),
    onTertiaryContainer = AppInk,
    background = Color(0xFFF8F4FF),
    onBackground = AppInk,
    surface = Color(0xFFFFFBFF),
    onSurface = AppInk,
    surfaceVariant = Color(0xFFF3ECFF),
    onSurfaceVariant = AppInkSoft,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF8892A6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCF7FF),
    surfaceContainer = Color(0xFFF8F1FF),
    surfaceContainerHigh = Color(0xFFF2E8FA),
    surfaceContainerHighest = Color(0xFFEADFF5)
)

@Composable
fun WASaverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
