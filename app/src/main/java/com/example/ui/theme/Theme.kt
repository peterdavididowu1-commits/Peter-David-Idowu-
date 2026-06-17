package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SchoolNavyPrimary,
    onPrimary = SchoolOnNavy,
    primaryContainer = SchoolNavyPrimary.copy(alpha = 0.1f),
    onPrimaryContainer = SchoolNavyPrimary,
    secondary = SchoolRoyalSecondary,
    onSecondary = SchoolOnRoyal,
    secondaryContainer = SchoolRoyalSecondary.copy(alpha = 0.1f),
    onSecondaryContainer = SchoolRoyalSecondary,
    background = SchoolIceBackground,
    onBackground = SchoolTextPrimary,
    surface = SchoolOffWhiteSurface,
    onSurface = SchoolTextPrimary,
    surfaceVariant = SchoolIceBackground,
    onSurfaceVariant = SchoolTextSecondary,
    outline = SchoolBoundaryOutline,
    error = SchoolErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = SchoolRoyalSecondary,
    onPrimary = Color.White,
    primaryContainer = SchoolNavyPrimary,
    onPrimaryContainer = Color.White,
    secondary = SchoolRoyalSecondary,
    onSecondary = Color.White,
    secondaryContainer = SchoolNavyPrimary.copy(alpha = 0.3f),
    onSecondaryContainer = Color.White,
    background = Color(0xFF0F172A), // Elegant dark slate
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    error = SchoolErrorRed,
    onError = Color.White
)

@Composable
fun HisGraceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
