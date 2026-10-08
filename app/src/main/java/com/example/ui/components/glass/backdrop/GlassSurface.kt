package com.example.ui.components.glass.backdrop

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium Liquid Glass Surface inspired directly by the Square project optics.
 *
 * Replaces opaque dark fills with authentic liquid glass optics:
 * 1. Hyper-translucent water/crystal tinting allowing background content to flow through smoothly.
 * 2. 135° directional specular rim illumination (simulating physical studio lighting).
 * 3. Double-layer inner refractive chamfer for tangible 3D glass thickness.
 * 4. Micro-damped inner shadow for convex lens volume.
 * 5. Hardware AGSL Lens Refraction & Gaussian Blur pipeline for API 33+/31+.
 */
@Composable
fun GlassSurface(
    shape: RoundedCornerShape,
    isDark: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    blurRadius: Dp = 24.dp,
    refractionStrength: Float = 16f,
    chromaticAberration: Float = 0.05f
) {
    // Square liquid glass color formulation with 24.dp frosted diffusion
    val glassBgGradient = if (isDark) {
        listOf(
            Color.White.copy(alpha = 0.16f),
            Color.White.copy(alpha = 0.07f),
            accentColor.copy(alpha = 0.08f),
            Color.Black.copy(alpha = 0.32f)
        )
    } else {
        listOf(
            Color.White.copy(alpha = 0.76f),
            Color.White.copy(alpha = 0.50f),
            accentColor.copy(alpha = 0.10f),
            Color.White.copy(alpha = 0.40f)
        )
    }

    // Specular border gradient simulating 135° angled directional light source
    val glassBorderBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.45f), // Top-left high specular catch
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.22f)  // Bottom-right subtle ground bounce reflection
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.85f),
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.20f),
                Color.White.copy(alpha = 0.50f)
            )
        },
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            // Optical Shader layer strictly on the glass background
            .graphicsLayer {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && size.width > 0 && size.height > 0) {
                    val lensEffect = GlassShaders.getLensEffect(
                        width = size.width,
                        height = size.height,
                        refraction = refractionStrength,
                        dispersion = chromaticAberration
                    )
                    val blurEffect = if (blurRadius > 0.dp) {
                        RenderEffect.createBlurEffect(
                            blurRadius.toPx(),
                            blurRadius.toPx(),
                            Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    } else null

                    renderEffect = if (lensEffect != null && blurEffect != null) {
                        RenderEffect.createChainEffect(
                            lensEffect.asAndroidRenderEffect(),
                            blurEffect.asAndroidRenderEffect()
                        ).asComposeRenderEffect()
                    } else {
                        lensEffect ?: blurEffect
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.dp) {
                    renderEffect = RenderEffect.createBlurEffect(
                        blurRadius.toPx(),
                        blurRadius.toPx(),
                        Shader.TileMode.CLAMP
                    ).asComposeRenderEffect()
                }
            }
            .background(Brush.verticalGradient(glassBgGradient))
            .border(
                width = 1.dp,
                brush = glassBorderBrush,
                shape = shape
            )
            .drawBehind {
                val cornerPx = size.height * 0.5f

                // 1. Top Specular Light Ridge (Simulates physical convex glass rim)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.40f else 0.70f),
                            Color.White.copy(alpha = if (isDark) 0.12f else 0.25f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.42f
                    ),
                    cornerRadius = CornerRadius(cornerPx, cornerPx)
                )

                // 2. Bottom Optical Bevel & Damped Inner Shade for Curvature
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = if (isDark) 0.18f else 0.05f)
                        ),
                        startY = size.height * 0.55f,
                        endY = size.height
                    ),
                    cornerRadius = CornerRadius(cornerPx, cornerPx)
                )

                // 3. Ultra-fine Inner Specular Edge (1.2px) for crisp cut-crystal precision
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.35f else 0.60f),
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.15f else 0.30f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(width = 1.2f)
                )
            }
    )
}
