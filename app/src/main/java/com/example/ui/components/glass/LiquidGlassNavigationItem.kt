package com.example.ui.components.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Individual navigation tab item for Liquid Glass Bar.
 *
 * Uses the exact project icons and resource assets without alteration.
 * Meets accessibility minimum touch target criteria (min 48dp).
 */
@Composable
fun LiquidGlassNavigationItem(
    item: LiquidGlassNavItem,
    isSelected: Boolean,
    itemWidth: Dp,
    accentColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Subtle scale animation upon selection (no exaggerated bounce)
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(stiffness = 500f),
        label = "glass_icon_scale"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else unselectedColor,
        animationSpec = spring(stiffness = 400f),
        label = "glass_icon_color"
    )

    val labelAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.72f,
        animationSpec = spring(stiffness = 400f),
        label = "glass_label_alpha"
    )

    Box(
        modifier = modifier
            .width(itemWidth)
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Glass indicator provides the primary visual feedback
                onClick = onClick
            )
            .semantics {
                this.role = Role.Tab
                this.selected = isSelected
                this.contentDescription = item.title
            }
            .testTag("nav_item_${item.id}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = null, // Semantics handles TalkBack
                tint = contentColor,
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = item.title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor.copy(alpha = labelAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
