package org.jetbrains.jewel.samples.ideplugin.releasessample.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider

@Composable
fun MarkdownCompose() {
    Row(Modifier.trackActivation().fillMaxSize().background(JewelTheme.globalColors.paneBackground)) {
        var currentMarkdown by remember { mutableStateOf(JewelReadme) }
        MarkdownEditor(currentMarkdown, { currentMarkdown = it }, Modifier.fillMaxHeight().weight(1f))

        Divider(Orientation.Vertical, Modifier.fillMaxHeight())

        MarkdownPreview(currentMarkdown, Modifier.fillMaxHeight().weight(1f))
    }
}
