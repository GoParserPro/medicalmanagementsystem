package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MedicalTealPrimaryDark,
    secondary = MedicalBlueSecondaryDark,
    tertiary = MedicalAccentGold,
    background = MedicalBackgroundBlack,
    surface = MedicalSurfaceDark,
    onPrimary = MedicalTealLightDark,
    onSecondary = MedicalBackgroundBlack,
    onBackground = MedicalBackgroundWhite,
    onSurface = MedicalBackgroundWhite
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalTealPrimary,
    secondary = MedicalBlueSecondary,
    tertiary = MedicalAccentGold,
    background = MedicalBackgroundWhite,
    surface = MedicalTealLight,
    onPrimary = MedicalBackgroundWhite,
    onSecondary = MedicalBackgroundWhite,
    onBackground = MedicalSteelGrey,
    onSurface = MedicalSteelGrey
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false to force our customized gorgeous clinic style instead of dynamic wallpaper color!
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
        typography = Typography,
        content = content
    )
}
