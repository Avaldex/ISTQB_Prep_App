package com.istqb.prep.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowLayoutInfo
import kotlin.math.roundToInt

@Composable
internal fun FoldAwareViewport(layoutInfo: WindowLayoutInfo?, content: @Composable (Boolean) -> Unit) {
    var origin by remember { mutableStateOf(IntOffset.Zero) }
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                origin = IntOffset(position.x.roundToInt(), position.y.roundToInt())
            }
    ) {
        // WindowManager reports physical pixels relative to the application window.
        val window = IntRect(
            origin.x,
            origin.y,
            origin.x + constraints.maxWidth,
            origin.y + constraints.maxHeight
        )
        val pane = layoutInfo?.displayFeatures.orEmpty().fold(window) { area, feature ->
            if (feature is FoldingFeature &&
                (feature.isSeparating || feature.occlusionType == FoldingFeature.OcclusionType.FULL)
            ) {
                val bounds = feature.bounds
                val hinge = IntRect(bounds.left, bounds.top, bounds.right, bounds.bottom)
                largerPane(area, hinge, vertical = feature.orientation == FoldingFeature.Orientation.VERTICAL)
            } else {
                area
            }
        }
        val insets = WindowInsets(
            left = pane.left - window.left,
            top = pane.top - window.top,
            right = window.right - pane.right,
            bottom = window.bottom - pane.bottom
        )
        val useWidePadding = with(LocalDensity.current) { pane.width.toDp() >= 600.dp }
        // Keep the same composition when the posture changes. Consuming the pane
        // insets also prevents Scaffold from applying system bars on a hidden edge.
        Box(Modifier.fillMaxSize().windowInsetsPadding(insets).clipToBounds()) {
            content(useWidePadding)
        }
    }
}

internal fun largerPane(area: IntRect, hinge: IntRect, vertical: Boolean): IntRect {
    if (vertical) {
        if (maxOf(area.top, hinge.top) >= minOf(area.bottom, hinge.bottom)) return area
        val leftEnd = hinge.left.coerceIn(area.left, area.right)
        val rightStart = hinge.right.coerceIn(area.left, area.right)
        if (leftEnd == area.left && rightStart == area.right) return area
        return if (leftEnd - area.left >= area.right - rightStart) {
            area.copy(right = leftEnd)
        } else {
            area.copy(left = rightStart)
        }
    }
    if (maxOf(area.left, hinge.left) >= minOf(area.right, hinge.right)) return area
    val topEnd = hinge.top.coerceIn(area.top, area.bottom)
    val bottomStart = hinge.bottom.coerceIn(area.top, area.bottom)
    if (topEnd == area.top && bottomStart == area.bottom) return area
    return if (topEnd - area.top >= area.bottom - bottomStart) {
        area.copy(bottom = topEnd)
    } else {
        area.copy(top = bottomStart)
    }
}
