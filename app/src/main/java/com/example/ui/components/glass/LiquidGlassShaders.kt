package com.example.ui.components.glass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import org.intellij.lang.annotations.Language

/**
 * Shaders and RenderEffect utilities for Liquid Glass optics:
 * - Backdrop Blur
 * - Lens optical refraction
 * - Subtle chromatic dispersion
 * - Vibrancy & highlights
 */
object LiquidGlassShaders {

    @Language("AGSL")
    const val LENS_REFRACTION_SHADER = """
        uniform shader content;
        uniform float2 resolution;
        uniform float refractionStrength;
        uniform float chromaticAberration;

        // Signed distance to rounded capsule border
        float sdRoundedBox(float2 p, float2 b, float r) {
            float2 q = abs(p) - b + r;
            return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / resolution;
            float2 halfRes = resolution * 0.5;
            float2 p = fragCoord - halfRes;
            float radius = min(halfRes.x, halfRes.y);
            float2 boxSize = halfRes - float2(radius, radius);
            
            float d = sdRoundedBox(p, boxSize, radius);
            
            // Normalized distance toward rim [0, 1]
            float edgeDist = clamp(-d / radius, 0.0, 1.0);
            
            // Subtle refraction lens vector
            float2 normal = (length(p) > 0.001) ? normalize(p) : float2(0.0);
            float distortion = pow(1.0 - edgeDist, 2.2) * refractionStrength;
            
            // Dispersive offset for subtle chromatic aberration at glass edges
            float disp = distortion * chromaticAberration;
            
            float2 coordR = fragCoord - normal * (distortion + disp);
            float2 coordG = fragCoord - normal * distortion;
            float2 coordB = fragCoord - normal * (distortion - disp);
            
            half4 colR = content.eval(coordR);
            half4 colG = content.eval(coordG);
            half4 colB = content.eval(coordB);
            
            // Subtle specular glass edge reflection
            float edgeHighlight = pow(1.0 - edgeDist, 3.5) * 0.08;
            
            half4 result = half4(colR.r, colG.g, colB.b, colG.a);
            result.rgb += half3(edgeHighlight);
            
            return result;
        }
    """

    private var cachedRuntimeShader: Any? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createLensRenderEffect(
        widthPx: Float,
        heightPx: Float,
        refractionStrength: Float = 16f,
        chromaticAberration: Float = 0.06f
    ): androidx.compose.ui.graphics.RenderEffect? {
        return try {
            val shader = (cachedRuntimeShader as? RuntimeShader) ?: RuntimeShader(LENS_REFRACTION_SHADER).also {
                cachedRuntimeShader = it
            }
            shader.setFloatUniform("resolution", widthPx.coerceAtLeast(1f), heightPx.coerceAtLeast(1f))
            shader.setFloatUniform("refractionStrength", refractionStrength)
            shader.setFloatUniform("chromaticAberration", chromaticAberration)
            
            val androidEffect = RenderEffect.createRuntimeShaderEffect(shader, "content")
            androidEffect.asComposeRenderEffect()
        } catch (_: Throwable) {
            null
        }
    }
}

/**
 * Modifier to apply glass optical refraction and blur across Android versions.
 * - On Android 13+ (API 33+): uses AGSL RuntimeShader lens refraction + RenderEffect.
 * - On Android 12 (API 31-32): uses RenderEffect blur.
 * - On Android 11 and below: uses lightweight fallback with Compose graphicsLayer.
 */
fun Modifier.liquidGlassOptics(
    widthPx: Float,
    heightPx: Float,
    blurRadiusPx: Float = 24f,
    refractionStrengthPx: Float = 16f,
    chromaticAberration: Float = 0.05f
): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && widthPx > 0f && heightPx > 0f) {
        this.graphicsLayer {
            val lensEffect = LiquidGlassShaders.createLensRenderEffect(
                widthPx = widthPx,
                heightPx = heightPx,
                refractionStrength = refractionStrengthPx,
                chromaticAberration = chromaticAberration
            )
            val blurEffect = if (blurRadiusPx > 0f) {
                RenderEffect.createBlurEffect(
                    blurRadiusPx,
                    blurRadiusPx,
                    Shader.TileMode.CLAMP
                ).asComposeRenderEffect()
            } else null

            renderEffect = if (lensEffect != null && blurEffect != null) {
                // Chain blur + lens refraction
                val chainedAndroid = RenderEffect.createChainEffect(
                    lensEffect.asAndroidRenderEffect(),
                    blurEffect.asAndroidRenderEffect()
                )
                chainedAndroid.asComposeRenderEffect()
            } else {
                lensEffect ?: blurEffect
            }
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.graphicsLayer {
            if (blurRadiusPx > 0f) {
                renderEffect = RenderEffect.createBlurEffect(
                    blurRadiusPx,
                    blurRadiusPx,
                    Shader.TileMode.CLAMP
                ).asComposeRenderEffect()
            }
        }
    } else {
        this
    }
}
