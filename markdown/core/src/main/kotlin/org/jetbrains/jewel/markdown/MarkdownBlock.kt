package org.jetbrains.jewel.markdown

public sealed interface InlineMarkdown {

    @JvmInline
    public value class Text(public val literal: String) : InlineMarkdown

    @JvmInline
    public value class ItemsList(public val items: List<InlineMarkdown>) : InlineMarkdown

    public data class Emphasis(val content: InlineMarkdown) : InlineMarkdown

    public data class StrongEmphasis(val content: InlineMarkdown) : InlineMarkdown
    public data class Code(val literal: String) : InlineMarkdown
    public data class Link(val destination: String?, val title: String?) : InlineMarkdown
    public object HardLineBreak : InlineMarkdown
}

public interface BlockWithInlineMarkdown {
    public val inlineContent: InlineMarkdown
}

// AbstractVisitor
// Block
// BlockQuote
// BulletList
// Code
// CustomBlock
// CustomNode
// Delimited
// Document
//
// FencedCodeBlock
// HardLineBreak
// Heading
// HtmlBlock
// HtmlInline
// Image
// IndentedCodeBlock
// Link
// LinkReferenceDefinition
// ListBlock
// ListItem
// Node
// Nodes
// OrderedList
// package-info
// Paragraph
// SoftLineBreak
// SourceSpan
// SourceSpans
// StrongEmphasis
// Text
// ThematicBreak
// Visitor

// /**
// * A run of inline Markdown used as content for
// * [block-level elements][MarkdownBlock].
// */
// @JvmInline
// public value class InlineMarkdown(public val content: Node)
