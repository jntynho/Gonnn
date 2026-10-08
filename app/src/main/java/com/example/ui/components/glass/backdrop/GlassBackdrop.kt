package com.example.ui.components.glass.backdrop

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import org.intellij.lang.annotations.Language

/**
 * Core Backdrop state for Liquid Glass optical capture.
 * Captures the underlying screen content and feeds it into the glass rendering pipeline.
 */
@Stable
class BackdropState {
    var capturedBitmap by mutableStateOf<Bitmap?>(null)
    var isCapturing by mutableStateOf(false)
}

@Composable
fun rememberBackdropState(): BackdropState {
    return remember { BackdropState() }
}

/**
 * Modifier placed on the background content layer to record and capture
 * the backdrop pixels into the BackdropState.
 */
fun Modifier.recordBackdrop(state: BackdropState): Modifier {
    return this.drawWithContent {
        drawContent()
        if (state.isCapturing && size.width > 0 && size.height > 0) {
            val width = size.width.toInt().coerceAtLeast(1)
            val height = size.height.toInt().coerceAtLeast(1)
            val bitmap = state.capturedBitmap.let { current ->
                if (current != null && current.width == width && current.height == height) {
                    current
                } else {
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
                        state.capturedBitmap = it
                    }
                }
            }
            val canvas = Canvas(bitmap)
            drawIntoCanvas { composeCanvas ->
                composeCanvas.nativeCanvas.drawBitmap(bitmap, 0f, 0f, null)
            }
        }
    }
}

/**
 * AGSL Shaders for Lens Optical Refraction & Dispersion matching Square.
 */
object GlassShaders {
    @Language("AGSL")
    const val LENS_SHADER_SRC = """
        uniform shader content;
        uniform float2 resolution;
        uniform float refraction;
        uniform float dispersion;

        float sdCapsule(float2 p, float2 b, float r) {
            float2 q = abs(p) - b + r;
            return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
        }

        half4 main(float2 fragCoord) {
            float2 halfRes = resolution * 0.5;
            float2 p = fragCoord - halfRes;
            float radius = min(halfRes.x, halfRes.y);
            float2 box = halfRes - float2(radius, radius);
            
            float d = sdCapsule(p, box, radius);
            float edge = clamp(-d / radius, 0.0, 1.0);
            
            // Optical distortion curvature
            float2 normal = (length(p) > 0.001) ? normalize(p) : float2(0.0);
            float strength = pow(1.0 - edge, 2.0) * refraction;
            float disp = strength * dispersion;
            
            // Chromatic aberration sample
            half4 colR = content.eval(fragCoord - normal * (strength + disp));
            half4 colG = content.eval(fragCoord - normal * strength);
            half4 colB = content.eval(fragCoord - normal * (strength - disp));
            
            // Specular rim light
            float rim = pow(1.0 - edge, 3.2) * 0.12;
            
            return half4(colR.r + rim, colG.g + rim, colB.b + rim, colG.a);
        }
    """

    private var runtimeShaderCache: Any? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getLensEffect(
        width: Float,
        height: Float,
        refraction: Float,
        dispersion: Float
    ): androidx.compose.ui.graphics.RenderEffect? {
        return try {
            val shader = (runtimeShaderCache as? RuntimeShader) ?: RuntimeShader(LENS_SHADER_SRC).also {
                runtimeShaderCache = it
            }
            shader.setFloatUniform("resolution", width.coerceAtLeast(1f), height.coerceAtLeast(1f))
            shader.setFloatUniform("refraction", refraction)
            shader.setFloatUniform("dispersion", dispersion)

            val nativeEffect = RenderEffect.createRuntimeShaderEffect(shader, "content")
            nativeEffect.asComposeRenderEffect()
        } catch (_: Throwable) {
            null
        }
    }
}
