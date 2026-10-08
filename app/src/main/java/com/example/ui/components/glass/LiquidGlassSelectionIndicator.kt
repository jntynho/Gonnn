package com.example.ui.components.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Premium Liquid Glass Selection Indicator.
 *
 * Implements Square's "Glass-on-Glass" optical layering:
 * - A denser, jewel-like refractive glass pill floating inside the main glass bar
 * - Multi-directional specular light catch (135° diagonal highlight)
 * - Pure physical spring dynamics with velocity-based lateral stretch
 * - Dynamic color luminance adapted smoothly to Dark and Light palettes
 */
@Composable
fun LiquidGlassSelectionIndicator(
    targetCenterX: Float,
    itemWidth: Dp,
    height: Dp,
    accentColor: Color,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val positionAnim = remember { Animatable(targetCenterX) }
    val stretchAnim = remember { Animatable(1.0f) }

    LaunchedEffect(targetCenterX) {
        val distance = abs(targetCenterX - positionAnim.value)
        if (distance > 2f) {
            val targetStretch = 1.0f + (distance / 160f).coerceIn(0.10f, 0.28f)
            
            // Dynamic stretch impulse
            launch {
                stretchAnim.animateTo(
                    targetValue = targetStretch,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                stretchAnim.animateTo(
                    targetValue = 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            // Damped physical spring navigation
            positionAnim.animateTo(
                targetValue = targetCenterX,
                animationSpec = spring(
                    dampingRatio = 0.80f,
                    stiffness = 380f
                )
            )
        } else {
            positionAnim.snapTo(targetCenterX)
        }
    }

    val baseWidth = (itemWidth - 10.dp).coerceAtLeast(40.dp)
    val currentWidth = baseWidth * stretchAnim.value
    val cornerRadius = 24.dp
    val shape = RoundedCornerShape(cornerRadius)

    // Concentrated liquid glass jewel gradient for indicator pill
    val indicatorGradient = if (isDarkTheme) {
        listOf(
            Color.White.copy(alpha = 0.28f),
            accentColor.copy(alpha = 0.32f),
            accentColor.copy(alpha = 0.18f),
            Color.Black.copy(alpha = 0.15f)
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.90f),
            accentColor.copy(alpha = 0.25f),
            accentColor.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.50f)
        )
    }

    val indicatorBorderBrush = Brush.linearGradient(
        colors = if (isDarkTheme) {
            listOf(
                Color.White.copy(alpha = 0.65f),
                accentColor.copy(alpha = 0.40f),
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.30f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.95f),
                accentColor.copy(alpha = 0.55f),
                Color.White.copy(alpha = 0.30f),
                Color.White.copy(alpha = 0.60f)
            )
        },
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = modifier
            .offset {
                val leftPx = (positionAnim.value - (currentWidth.toPx() / 2f)).roundToInt()
                IntOffset(x = leftPx, y = 0)
            }
            .width(currentWidth)
            .height(height)
            .shadow(
                elevation = if (isDarkTheme) 8.dp else 10.dp,
                shape = shape,
                ambientColor = accentColor.copy(alpha = 0.35f),
                spotColor = accentColor.copy(alpha = 0.45f)
            )
            .clip(shape)
            .background(Brush.verticalGradient(indicatorGradient))
            .border(
                width = 1.dp,
                brush = indicatorBorderBrush,
                shape = shape
            )
            .drawBehind {
                val cornerPx = cornerRadius.toPx()

                // High-specular top-down glass lens curvature sheen
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDarkTheme) 0.50f else 0.80f),
                            Color.White.copy(alpha = if (isDarkTheme) 0.15f else 0.30f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.45f
                    ),
                    cornerRadius = CornerRadius(cornerPx, cornerPx)
                )

                // Sub-lens bottom curvature shade
                drawRoundRect(
                    color = Color.Black.copy(alpha = if (isDarkTheme) 0.22f else 0.08f),
                    topLeft = Offset(0f, size.height * 0.65f),
                    size = Size(size.width, size.height * 0.35f),
                    cornerRadius = CornerRadius(cornerPx, cornerPx)
                )

                // Precision inner edge reflection stroke
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDarkTheme) 0.45f else 0.70f),
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDarkTheme) 0.20f else 0.35f)
                        )
                    ),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(width = 1.2f)
                )
            }
    )
}
