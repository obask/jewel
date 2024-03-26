package org.jetbrains.jewel.markdown

import org.commonmark.node.Node
import org.jetbrains.jewel.markdown.MarkdownBlock.BlockQuote
import org.jetbrains.jewel.markdown.MarkdownBlock.CodeBlock
import org.jetbrains.jewel.markdown.MarkdownBlock.CustomBlock
import org.jetbrains.jewel.markdown.MarkdownBlock.Heading
import org.jetbrains.jewel.markdown.MarkdownBlock.HtmlBlock
import org.jetbrains.jewel.markdown.MarkdownBlock.ListBlock
import org.jetbrains.jewel.markdown.MarkdownBlock.ListItem
import org.jetbrains.jewel.markdown.MarkdownBlock.Paragraph
import org.jetbrains.jewel.markdown.MarkdownBlock.ThematicBreak
import org.commonmark.node.Block as CMBlock
import org.commonmark.node.BlockQuote as CMBlockQuote
import org.commonmark.node.BulletList as CMBulletList
import org.commonmark.node.CustomBlock as CMCustomBlock
import org.commonmark.node.FencedCodeBlock as CMFencedCodeBlock
import org.commonmark.node.Heading as CMHeading
import org.commonmark.node.HtmlBlock as CMHtmlBlock
import org.commonmark.node.IndentedCodeBlock as CMIndentedCodeBlock
import org.commonmark.node.LinkReferenceDefinition as CMLinkReferenceDefinition
import org.commonmark.node.ListItem as CMListItem
import org.commonmark.node.OrderedList as CMOrderedList
import org.commonmark.node.Paragraph as CMParagraph
import org.commonmark.node.ThematicBreak as CMThematicBreak

public sealed interface MarkdownBlock {

    public val value: CMBlock

    @JvmInline
    public value class BlockQuote(override val value: CMBlockQuote) : MarkdownBlock

    public sealed interface CodeBlock : MarkdownBlock {
        @JvmInline
        public value class FencedCodeBlock(override val value: CMFencedCodeBlock) : CodeBlock {

            public val mimeType: MimeType?
                get() = MimeType.Known.fromMarkdownLanguageName(value.info.orEmpty())
        }

        @JvmInline
        public value class IndentedCodeBlock(override val value: CMIndentedCodeBlock) : CodeBlock
    }

    @JvmInline
    public value class CustomBlock(override val value: CMCustomBlock) : MarkdownBlock

    @JvmInline
    public value class Heading(override val value: CMHeading) : MarkdownBlock, BlockWithInlineMarkdown {

        public val level: Int
            get() = value.level
    }

    @JvmInline
    public value class HtmlBlock(override val value: CMHtmlBlock) : MarkdownBlock

    public sealed interface ListBlock : MarkdownBlock {
        @JvmInline
        public value class BulletList(override val value: CMBulletList) : ListBlock

        @JvmInline
        public value class OrderedList(override val value: CMOrderedList) : ListBlock
    }

    @JvmInline
    public value class ListItem(override val value: CMListItem) : MarkdownBlock

    @JvmInline
    public value class ThematicBreak(override val value: CMThematicBreak) : MarkdownBlock

    @JvmInline
    public value class Paragraph(override val value: CMParagraph) : MarkdownBlock, BlockWithInlineMarkdown

    public val children: List<MarkdownBlock>
        get() = buildList {
            var current = this@MarkdownBlock.value.firstChild
            while (current != null) {
                current.toMarkdownBlock()
                    ?.let { add(it) }
                current = current.next
            }
        }
}

public interface BlockWithInlineMarkdown {

    public val inlineContent: List<InlineMarkdown>
        get() = buildList {
            var child = (this@BlockWithInlineMarkdown as MarkdownBlock).value.firstChild

            while (child != null) {
                add(child.toInlineNode())
                child = child.next
            }
        }
}

public fun Node.toMarkdownBlock(): MarkdownBlock? = when (this) {
    is CMParagraph -> Paragraph(this)
    is CMHeading -> Heading(this)
    is CMHtmlBlock -> HtmlBlock(this)
    is CMBlockQuote -> BlockQuote(this)
    is CMFencedCodeBlock -> CodeBlock.FencedCodeBlock(this)
    is CMIndentedCodeBlock -> CodeBlock.IndentedCodeBlock(this)
    is CMBulletList -> ListBlock.BulletList(this)
    is CMOrderedList -> ListBlock.OrderedList(this)
    is CMListItem -> ListItem(this)
    is CMThematicBreak -> ThematicBreak(this)
    is CMCustomBlock -> CustomBlock(this)
    is CMLinkReferenceDefinition -> null
    else -> error("Unexpected block $this")
}
