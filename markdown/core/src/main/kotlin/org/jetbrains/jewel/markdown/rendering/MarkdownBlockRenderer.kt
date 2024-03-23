package org.jetbrains.jewel.markdown.rendering

import androidx.compose.runtime.Composable
import org.commonmark.node.Block
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.ListBlock
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.jetbrains.jewel.foundation.ExperimentalJewelApi

@ExperimentalJewelApi
public interface MarkdownBlockRenderer {

    @Composable
    public fun render(blocks: List<Node>)

    @Composable
    public fun render(block: Node)

    @Composable
    public fun render(block: Block)

    @Composable
    public fun render(block: BlockQuote, styling: MarkdownStyling.BlockQuote)

    @Composable
    public fun render(block: Paragraph, styling: MarkdownStyling.Paragraph)

    @Composable
    public fun render(block: Heading, styling: MarkdownStyling.Heading)

    @Composable
    public fun render(block: Heading, styling: MarkdownStyling.Heading.HX)

    @Composable
    public fun render(block: ListBlock, styling: MarkdownStyling.List)

    @Composable
    public fun render(block: OrderedList, styling: MarkdownStyling.List.Ordered)

    @Composable
    public fun render(block: BulletList, styling: MarkdownStyling.List.Unordered)

    @Composable
    public fun render(block: ListItem)

    @Composable
    public fun render(block: IndentedCodeBlock, styling: MarkdownStyling.Code.Indented)

    @Composable
    public fun render(block: FencedCodeBlock, styling: MarkdownStyling.Code.Fenced)

    @Composable
    public fun render(block: Image, styling: MarkdownStyling.Image)

    @Composable
    public fun renderThematicBreak(styling: MarkdownStyling.ThematicBreak)

    @Composable
    public fun render(block: HtmlBlock, styling: MarkdownStyling.HtmlBlock)

    public companion object
}
