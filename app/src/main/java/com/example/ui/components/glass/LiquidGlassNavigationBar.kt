package com.example.ui.components.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.ui.components.glass.backdrop.GlassSurface
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.LocalVaultPalette

/**
 * Floating Liquid Glass Navigation Bar - Authentic Square Optics Architecture.
 *
 * Implements:
 * 1. Deep translucent floating glass capsule with multi-angled specular highlights.
 * 2. Diffused ambient floating shadow giving a natural optical elevation.
 * 3. Liquid Glass Indicator (Layer 2) with crystal concentration and velocity stretch.
 * 4. Ultra-crisp foreground row (Layer 3) displaying the 5 exact project icons and labels.
 */
@Composable
fun LiquidGlassNavigationBar(
    items: List<LiquidGlassNavItem> = LiquidGlassNavItem.defaultNavItems,
    selectedId: String,
    onItemSelected: (LiquidGlassNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalVaultPalette.current
    val accent = LocalAccentColor.current
    val density = LocalDensity.current

    val isLight = remember(palette.bg) {
        val lum = 0.2126f * palette.bg.red + 0.7152f * palette.bg.green + 0.0722f * palette.bg.blue
        lum > 0.45f
    }
    val isDark = !isLight

    val selectedIndex by remember(selectedId, items) {
        derivedStateOf {
            items.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
        }
    }

    val barHeight = 64.dp
    val barCornerRadius = 32.dp
    val barShape = RoundedCornerShape(barCornerRadius)

    val unselectedItemColor = if (isDark) {
        Color.White.copy(alpha = 0.72f)
    } else {
        Color(0xFF3C404E).copy(alpha = 0.82f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .height(barHeight)
                // Diffused ambient floating elevation shadow (Square style)
                .shadow(
                    elevation = if (isDark) 16.dp else 20.dp,
                    shape = barShape,
                    ambientColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color(0x2A1E2235),
                    spotColor = if (isDark) accent.copy(alpha = 0.25f) else Color(0x22000000)
                )
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val itemCount = items.size.coerceAtLeast(1)
            val itemWidthPx = totalWidthPx / itemCount
            val itemWidthDp = with(density) { itemWidthPx.toDp() }

            val targetCenterX = (selectedIndex + 0.5f) * itemWidthPx
            val indicatorHeight = barHeight - 12.dp

            // LAYER 1: Deep translucent Glass Surface (Translucent water/crystal tint, 135° directional rim light)
            GlassSurface(
                shape = barShape,
                isDark = isDark,
                accentColor = accent,
                blurRadius = 24.dp,
                refractionStrength = 18f,
                chromaticAberration = 0.05f,
                modifier = Modifier.fillMaxSize()
            )

            // LAYER 2: Moving Glass Selection Indicator with stretch & settling physics
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp)
            ) {
                LiquidGlassSelectionIndicator(
                    targetCenterX = targetCenterX,
                    itemWidth = itemWidthDp,
                    height = indicatorHeight,
                    accentColor = accent,
                    isDarkTheme = isDark,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            // LAYER 3: 100% Crisp Interactive Navigation Items
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    LiquidGlassNavigationItem(
                        item = item,
                        isSelected = index == selectedIndex,
                        itemWidth = itemWidthDp,
                        accentColor = accent,
                        unselectedColor = unselectedItemColor,
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }
    }
}
