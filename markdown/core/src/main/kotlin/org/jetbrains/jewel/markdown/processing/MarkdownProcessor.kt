package org.jetbrains.jewel.markdown.processing

import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.CustomBlock
import org.commonmark.node.Document
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.ListBlock
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser
import org.intellij.lang.annotations.Language
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.markdown.InlineMarkdown
import org.jetbrains.jewel.markdown.MarkdownBlock
import org.jetbrains.jewel.markdown.MarkdownBlock.CodeBlock
import org.jetbrains.jewel.markdown.MimeType
import org.jetbrains.jewel.markdown.extensions.MarkdownProcessorExtension
import org.jetbrains.jewel.markdown.rendering.DefaultInlineMarkdownRenderer
import org.jetbrains.jewel.markdown.toInlineNode

@ExperimentalJewelApi
public class MarkdownProcessor(private val extensions: List<MarkdownProcessorExtension> = emptyList()) {

    public constructor(vararg extensions: MarkdownProcessorExtension) : this(extensions.toList())

    private val commonMarkParser =
        Parser.builder().extensions(extensions.map { it.parserExtension }).build()

    /**
     * Parses a Markdown document, translating from CommonMark 0.31.2
     * to a list of [MarkdownBlock]. Inline Markdown in leaf nodes
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
    public fun processMarkdownDocument(@Language("Markdown") rawMarkdown: String): List<MarkdownBlock> {
        val document =
            commonMarkParser.parse(rawMarkdown) as? Document
                ?: error("This doesn't look like a Markdown document")

        return processChildren(document)
    }

    private fun Node.tryProcessMarkdownBlock(): MarkdownBlock? =
        // Non-Block children are ignored
        when (this) {
            is BlockQuote -> toMarkdownBlockQuote()
            is Heading -> toMarkdownHeadingOrNull()
            is Paragraph -> toMarkdownParagraphOrNull()
            is FencedCodeBlock -> toMarkdownCodeBlockOrNull()
            is IndentedCodeBlock -> toMarkdownCodeBlockOrNull()
            is BulletList -> toMarkdownListOrNull()
            is OrderedList -> toMarkdownListOrNull()
            is ThematicBreak -> MarkdownBlock.ThematicBreak
            is HtmlBlock -> toMarkdownHtmlBlockOrNull()
            is CustomBlock -> {
                extensions.find { it.processorExtension.canProcess(this) }
                    ?.processorExtension?.processMarkdownBlock(this, this@MarkdownProcessor)
            }
            else -> null
        }

    private fun BlockQuote.toMarkdownBlockQuote(): MarkdownBlock.BlockQuote =
        MarkdownBlock.BlockQuote(processChildren(this))

    private fun Heading.toMarkdownHeadingOrNull(): MarkdownBlock.Heading? =
        if (level > 6) {
            null
        } else {
            MarkdownBlock.Heading(contentsAsInlineMarkdown(), level)
        }

    private fun Paragraph.toMarkdownParagraphOrNull(): MarkdownBlock.Paragraph? {
        val inlineMarkdown = contentsAsInlineMarkdown()

        if (inlineMarkdown.isEmpty()) return null
        return MarkdownBlock.Paragraph(inlineMarkdown)
    }

    private fun FencedCodeBlock.toMarkdownCodeBlockOrNull(): CodeBlock.FencedCodeBlock =
        CodeBlock.FencedCodeBlock(
            literal.trimEnd('\n'),
            MimeType.Known.fromMarkdownLanguageName(info),
        )

    private fun IndentedCodeBlock.toMarkdownCodeBlockOrNull(): CodeBlock.IndentedCodeBlock =
        CodeBlock.IndentedCodeBlock(literal.trimEnd('\n'))

    private fun BulletList.toMarkdownListOrNull(): MarkdownBlock.ListBlock.BulletList? {
        val children = processListItems()
        if (children.isEmpty()) return null

        return MarkdownBlock.ListBlock.BulletList(children, isTight, marker)
    }

    private fun OrderedList.toMarkdownListOrNull(): MarkdownBlock.ListBlock.OrderedList? {
        val children = processListItems()
        if (children.isEmpty()) return null

        return MarkdownBlock.ListBlock.OrderedList(children, isTight, markerStartNumber, markerDelimiter)
    }

    private fun ListBlock.processListItems() = buildList {
        forEachChild { child ->
            if (child !is ListItem) return@forEachChild
            add(MarkdownBlock.ListItem(processChildren(child)))
        }
    }

    public fun processChildren(node: Node): List<MarkdownBlock> = buildList {
        node.forEachChild { child ->
            val parsedBlock = child.tryProcessMarkdownBlock()
            if (parsedBlock != null) {
                this.add(parsedBlock)
            }
        }
    }

    private fun Node.forEachChild(action: (Node) -> Unit) {
        var child = firstChild

        while (child != null) {
            action(child)
            child = child.next
        }
    }

    private fun HtmlBlock.toMarkdownHtmlBlockOrNull(): MarkdownBlock.HtmlBlock? {
        if (literal.isBlank()) return null
        return MarkdownBlock.HtmlBlock(content = literal.trimEnd('\n'))
    }

    private fun Node.contentsAsInlineMarkdown() = buildList {
        forEachChild {
            add(it.toInlineNode())
        }
    }
}
