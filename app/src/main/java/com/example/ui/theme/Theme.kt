package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = RoyalBlueLight,
  onPrimary = Color.White,
  primaryContainer = RoyalBlueDark,
  onPrimaryContainer = RoyalBlueSurface,
  secondary = RoyalBlueSecondary,
  onSecondary = Color.White,
  tertiary = GoldAccent,
  onTertiary = Color.White,
  background = BgDark,
  onBackground = TextLight,
  surface = SurfaceDark,
  onSurface = TextLight,
  surfaceVariant = Color(0xFF334155),
  onSurfaceVariant = TextSecondaryDark,
  outline = BorderDark,
  error = RedDanger
)

private val LightColorScheme = lightColorScheme(
  primary = RoyalBluePrimary,
  onPrimary = Color.White,
  primaryContainer = RoyalBlueSurface,
  onPrimaryContainer = RoyalBluePrimary,
  secondary = RoyalBlueSecondary,
  onSecondary = Color.White,
  tertiary = GoldAccent,
  onTertiary = Color.White,
  background = BgLight,
  onBackground = TextDark,
  surface = SurfaceLight,
  onSurface = TextDark,
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = TextSecondary,
  outline = BorderLight,
  error = RedDanger
)

@Composable
fun McKaysTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
