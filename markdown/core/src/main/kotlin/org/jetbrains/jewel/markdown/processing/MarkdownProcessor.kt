package org.jetbrains.jewel.markdown.processing

import org.commonmark.node.CustomBlock
import org.commonmark.node.Document
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.intellij.lang.annotations.Language
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.markdown.InlineMarkdown
import org.jetbrains.jewel.markdown.extensions.MarkdownProcessorExtension
import org.jetbrains.jewel.markdown.rendering.DefaultInlineMarkdownRenderer

@ExperimentalJewelApi
public class MarkdownProcessor(private val extensions: List<MarkdownProcessorExtension>) {

    private val commonMarkParser =
        Parser.builder()
            .extensions(extensions.map { it.parserExtension })
            .build()

    /**
     * Parses a Markdown document, translating from CommonMark 0.31.2
     * to a list of [Node]. Inline Markdown in leaf nodes
     * is contained in [InlineMarkdown], which can be rendered
     * to an [androidx.compose.ui.text.AnnotatedString] by using
     * [DefaultInlineMarkdownRenderer.renderAsAnnotatedString].
     *
     * The contents of [InlineMarkdown] is equivalent to the original, but
     * normalized and simplified, and cleaned up as follows:
     * * Replace HTML entities with the corresponding character (escaped, if it
     *   is necessary)
     * * Inline link and image references and omit the reference blocks
     * * Use the destination as text for links when no text is set (escaped, if
     *   it is necessary)
     * * Normalize link titles to always use double quotes as enclosing
     *   character
     * * Normalize backticks in inline code runs
     * * Convert links in image descriptions to plain text
     * * Drop empty nodes with no visual representation (e.g., links with no
     *   text and destination)
     * * Remove unnecessary escapes
     * * Escape non-formatting instances of ``*_`~<>[]()!`` for clarity
     *
     * The contents of code blocks aren't transformed in any way. HTML blocks
     * get their outer whitespace trimmed, and so does inline HTML.
     *
     * @see DefaultInlineMarkdownRenderer
     */
    public fun processMarkdownDocument(@Language("Markdown") rawMarkdown: String): List<Node> {
        val tmp = commonMarkParser.parse(rawMarkdown) as? Document
            ?: error("This doesn't look like a Markdown document")
        return buildList {
            tmp.forEachChild { block ->
                add(
                    when (block) {
                        is CustomBlock ->
                            // TODO move this to parser implementation
                            extensions.find { it.processorExtension.canProcess(block) }
                                ?.processorExtension?.processMarkdownBlock(block, this@MarkdownProcessor)!!
                        else -> block
                    },
                )
            }
        }
    }
}

public inline fun Node.forEachChild(action: (Node) -> Unit) {
    var child = firstChild

    while (child != null) {
        action(child)
        child = child.next
    }
}
