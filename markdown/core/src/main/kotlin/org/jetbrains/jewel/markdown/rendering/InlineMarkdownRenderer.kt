package org.jetbrains.jewel.markdown.rendering

import androidx.compose.ui.text.AnnotatedString
import org.commonmark.node.Node
import org.jetbrains.jewel.foundation.ExperimentalJewelApi

@ExperimentalJewelApi
public interface InlineMarkdownRenderer {

    /**
     * Render the [inlineMarkdown] as an [AnnotatedString], using the [styling]
     * provided.
     */
    public fun renderAsAnnotatedString(inlineMarkdown: Node, styling: InlinesStyling): AnnotatedString

    public companion object {

        /** Create a default inline renderer, with the [] provided. */
        public fun default(extensions: List<Any> = emptyList()): InlineMarkdownRenderer =
            DefaultInlineMarkdownRenderer()
    }
}
