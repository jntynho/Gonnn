package com.example.ui.components.glass

import androidx.annotation.DrawableRes
import com.example.R
import com.example.ui.ScreenState

/**
 * Model representing a single item in the Liquid Glass Navigation Bar.
 */
data class LiquidGlassNavItem(
    val id: String,
    val title: String,
    @DrawableRes val iconRes: Int,
    val targetScreen: ScreenState
) {
    companion object {
        /**
         * The exact 5 items required by the application in strict left-to-right order:
         * 1. Home
         * 2. Actor
         * 3. Studio
         * 4. StashDb
         * 5. Setting
         */
        val defaultNavItems = listOf(
            LiquidGlassNavItem(
                id = "home",
                title = "Home",
                iconRes = R.drawable.ic_nav_home,
                targetScreen = ScreenState.Home
            ),
            LiquidGlassNavItem(
                id = "actor",
                title = "Actor",
                iconRes = R.drawable.ic_nav_actor,
                targetScreen = ScreenState.Actors
            ),
            LiquidGlassNavItem(
                id = "studio",
                title = "Studio",
                iconRes = R.drawable.ic_nav_studio,
                targetScreen = ScreenState.Studios
            ),
            LiquidGlassNavItem(
                id = "stashdb",
                title = "StashDb",
                iconRes = R.drawable.ic_nav_stashdb,
                targetScreen = ScreenState.StashDb
            ),
            LiquidGlassNavItem(
                id = "setting",
                title = "Setting",
                iconRes = R.drawable.ic_nav_settings,
                targetScreen = ScreenState.Settings
            )
        )
    }
}
