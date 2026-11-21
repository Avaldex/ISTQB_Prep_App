package com.istqb.prep.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.istqb.prep.data.AppDisplayMode

private val StandardLightColors = lightColorScheme(
    primary = Color(0xFF1A56DB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1EFFE),
    onPrimaryContainer = Color(0xFF1E429F),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    outline = Color(0xFFCBD5E1)
)

private val ReadingSepiaColors = lightColorScheme(
    primary = Color(0xFF7C4A21),
    onPrimary = Color(0xFFFDF8EE),
    primaryContainer = Color(0xFFEADBC0),
    onPrimaryContainer = Color(0xFF3E240E),
    background = Color(0xFFF4ECD8),
    onBackground = Color(0xFF3B3126),
    surface = Color(0xFFF9F3E3),
    onSurface = Color(0xFF3B3126),
    surfaceVariant = Color(0xFFEAE0C8),
    outline = Color(0xFFC5B496)
)

private val EInkMonochromeColors = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE5E5E5),
    onPrimaryContainer = Color(0xFF000000),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF2F2F2),
    outline = Color(0xFF000000)
)

@Composable
fun IstqbPrepTheme(
    displayMode: AppDisplayMode,
    content: @Composable () -> Unit
) {
    val colorScheme = when (displayMode) {
        AppDisplayMode.STANDARD -> StandardLightColors
        AppDisplayMode.READING -> ReadingSepiaColors
        AppDisplayMode.EINK -> EInkMonochromeColors
    }

    val typography = when (displayMode) {
        AppDisplayMode.READING -> Typography(
            bodyLarge = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 28.sp
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 30.sp
            )
        )
        AppDisplayMode.EINK -> Typography(
            bodyLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                lineHeight = 25.sp
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 21.sp,
                lineHeight = 28.sp
            )
        )
        AppDisplayMode.STANDARD -> Typography(
            bodyLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
