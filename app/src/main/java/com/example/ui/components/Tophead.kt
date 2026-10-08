package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.LocalVaultPalette

/**
 * Tophead - Unified App Top Bar Component
 * Provides clean, consistent Material 3 headers across screens with:
 * 1. Standard navigation icon with Haptic Feedback.
 * 2. Adaptive title and optional subtitle.
 * 3. Bottom divider border matching app palette.
 * 4. Flexible trailing actions slot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tophead(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backContentDescription: String = "Back",
    showBorder: Boolean = true,
    titleColor: Color? = null,
    actions: @Composable (RowScope.() -> Unit) = {}
) {
    val palette = LocalVaultPalette.current
    val haptic = LocalHapticFeedback.current
    val primaryColor = titleColor ?: palette.textPrimary

    val borderModifier = if (showBorder) {
        Modifier.drawBehind {
            drawLine(
                color = palette.border,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
    } else Modifier

    TopAppBar(
        modifier = modifier.then(borderModifier),
        title = {
            if (subtitle.isNullOrBlank()) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = primaryColor
                )
            } else {
                Column {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = primaryColor
                    )
                    Text(
                        text = subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBack()
                    },
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = backContentDescription,
                        tint = primaryColor
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = palette.surface,
            titleContentColor = palette.textPrimary,
            navigationIconContentColor = palette.textPrimary,
            actionIconContentColor = palette.textPrimary
        )
    )
}

/**
 * SearchableTophead - Expandable Search Top Bar Component
 * Reusable header with integrated search field and auto-focus for Bookmarks, StashDB, etc.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableTophead(
    title: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchExpanded: Boolean,
    onSearchExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    testTag: String = "search_input",
    onBack: (() -> Unit)? = null,
    showBorder: Boolean = true,
    customTitleContent: (@Composable () -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {}
) {
    val palette = LocalVaultPalette.current
    val accent = LocalAccentColor.current
    val haptic = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val borderModifier = if (showBorder) {
        Modifier.drawBehind {
            drawLine(
                color = palette.border,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
    } else Modifier

    TopAppBar(
        modifier = modifier.then(borderModifier),
        title = {
            if (isSearchExpanded) {
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = palette.textPrimary,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag(testTag),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 15.sp,
                                        color = palette.textSecondary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            } else if (customTitleContent != null) {
                customTitleContent()
            } else {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = palette.textPrimary
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (isSearchExpanded) {
                        onSearchExpandedChange(false)
                        onSearchQueryChange("")
                    } else if (onBack != null) {
                        onBack()
                    }
                },
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (isSearchExpanded) "Close Search" else "Back",
                    tint = palette.textPrimary
                )
            }
        },
        actions = {
            if (isSearchExpanded) {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSearchQueryChange("")
                        },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_action_cancel),
                            contentDescription = "Clear Search",
                            tint = palette.textSecondary
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSearchExpandedChange(false)
                            onSearchQueryChange("")
                        },
                        modifier = Modifier.testTag("close_search_button")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_action_cancel),
                            contentDescription = "Close Search",
                            tint = palette.textSecondary
                        )
                    }
                }
            } else {
                actions()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = palette.surface,
            titleContentColor = palette.textPrimary,
            navigationIconContentColor = palette.textPrimary,
            actionIconContentColor = palette.textPrimary
        )
    )
}
