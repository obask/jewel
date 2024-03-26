package org.jetbrains.jewel.markdown.extensions.github.alerts

import org.commonmark.internal.util.Parsing
import org.commonmark.node.Block
import org.commonmark.node.CustomBlock
import org.commonmark.node.Node
import org.commonmark.parser.Parser.Builder
import org.commonmark.parser.Parser.ParserExtension
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.ParserState
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.text.TextContentNodeRendererContext
import org.commonmark.renderer.text.TextContentRenderer
import org.commonmark.renderer.text.TextContentRenderer.TextContentRendererExtension
import org.commonmark.text.Characters
import org.jetbrains.jewel.markdown.MarkdownBlock
import org.jetbrains.jewel.markdown.extensions.MarkdownBlockProcessorExtension
import org.jetbrains.jewel.markdown.extensions.MarkdownBlockRendererExtension
import org.jetbrains.jewel.markdown.extensions.MarkdownProcessorExtension
import org.jetbrains.jewel.markdown.extensions.MarkdownRendererExtension
import org.jetbrains.jewel.markdown.extensions.github.alerts.AlertBlock.Kind
import org.jetbrains.jewel.markdown.processing.MarkdownProcessor
import org.jetbrains.jewel.markdown.rendering.MarkdownStyling

public object GitHubAlertProcessorExtension : MarkdownProcessorExtension {

    override val parserExtension: ParserExtension = GitHubAlertCommonMarkExtension
    override val textRendererExtension: TextContentRendererExtension =
        GitHubAlertCommonMarkExtension

    override val processorExtension: MarkdownBlockProcessorExtension = GitHubAlertProcessorExtension

    private object GitHubAlertProcessorExtension : MarkdownBlockProcessorExtension {

        override fun canProcess(block: CustomBlock): Boolean = block is AlertBlock

        override fun processMarkdownBlock(block: CustomBlock, processor: MarkdownProcessor): MarkdownBlock.CustomBlock? {
            error("should use CustomNode parser instead")
        }
    }
}

public class GitHubAlertRendererExtension(
    alertStyling: AlertStyling,
    rootStyling: MarkdownStyling,
) : MarkdownRendererExtension {

    override val blockRenderer: MarkdownBlockRendererExtension =
        GitHubAlertBlockRenderer(alertStyling, rootStyling)
}

private object GitHubAlertCommonMarkExtension : ParserExtension, TextContentRendererExtension {

    private val AlertStartRegex =
        ">\\s+\\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)]\\s*".toRegex(RegexOption.IGNORE_CASE)

    override fun extend(parserBuilder: Builder) {
        parserBuilder.customBlockParserFactory { state, _ ->
            val line = state.line.content.substring(state.column)
            val matchResult = AlertStartRegex.matchEntire(line)

            if (matchResult != null) {
                val type = matchResult.groupValues[1]
                BlockStart.of(AlertParser(type))
                    .atColumn(state.column + state.indent + matchResult.value.length)
            } else {
                BlockStart.none()
            }
        }
    }

    override fun extend(rendererBuilder: TextContentRenderer.Builder) {
        rendererBuilder.nodeRendererFactory { AlertTextContentNodeRenderer(it) }
    }
}

private class AlertParser(type: String) : AbstractBlockParser() {

    private val block =
        when (type.lowercase()) {
            "note" -> AlertBlock(Kind.Note)
            "tip" -> AlertBlock(Kind.Tip)
            "important" -> AlertBlock(Kind.Important)
            "warning" -> AlertBlock(Kind.Warning)
            "caution" -> AlertBlock(Kind.Caution)
            else -> error("Unsupported highlighted blockquote type: '$type'")
        }

    override fun getBlock() = block

    override fun isContainer() = true

    override fun canContain(childBlock: Block?) = childBlock !is AlertBlock

    override fun tryContinue(parserState: ParserState): BlockContinue? {
        val nextNonSpace: Int = parserState.nextNonSpaceIndex

        return if (parserState.isMarker(nextNonSpace)) {
            var newColumn: Int = parserState.column + parserState.indent + 1
            // optional following space or tab
            if (Characters.isSpaceOrTab(parserState.line.content, nextNonSpace + 1)) {
                newColumn++
            }
            BlockContinue.atColumn(newColumn)
        } else {
            BlockContinue.none()
        }
    }

    private fun ParserState.isMarker(index: Int): Boolean {
        val line = line.content
        return indent < Parsing.CODE_BLOCK_INDENT && index < line.length && line[index] == '>'
    }
}

private class AlertTextContentNodeRenderer(private val context: TextContentNodeRendererContext) :
    NodeRenderer {

    private val writer = context.writer

    override fun getNodeTypes(): Set<Class<out Node>> = setOf(AlertBlock::class.java)

    override fun render(node: Node) {
        val premise =
            when ((node as? AlertBlock)?.kind) {
                Kind.Caution -> "\uD83D\uDED1 Caution! "
                Kind.Important -> "⚠\uFE0F Important! "
                Kind.Note -> "ℹ\uFE0F Note: "
                Kind.Tip -> "\uD83D\uDCA1 Tip: "
                Kind.Warning -> "⚠\uFE0F Warning: "
                null -> error("Unsupported node type ${node.javaClass.name}")
            }

        writer.write(premise)
        renderChildren(node)
    }

    private fun renderChildren(node: Node) {
        var child = node.firstChild
        while (child != null) {
            context.render(node)
            child = child.next
        }
    }
}
