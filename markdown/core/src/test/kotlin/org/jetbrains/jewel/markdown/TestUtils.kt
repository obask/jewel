package org.jetbrains.jewel.markdown

import org.commonmark.internal.InlineParserContextImpl
import org.commonmark.internal.InlineParserImpl
import org.commonmark.internal.LinkReferenceDefinitions
import org.commonmark.node.Block
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.parser.SourceLine
import org.commonmark.parser.SourceLines
import org.commonmark.renderer.html.HtmlRenderer
import org.intellij.lang.annotations.Language
import org.jetbrains.jewel.markdown.MarkdownBlock.BlockQuote
import org.jetbrains.jewel.markdown.MarkdownBlock.CodeBlock.FencedCodeBlock
import org.jetbrains.jewel.markdown.MarkdownBlock.CodeBlock.IndentedCodeBlock
import org.jetbrains.jewel.markdown.MarkdownBlock.Heading
import org.jetbrains.jewel.markdown.MarkdownBlock.HtmlBlock
import org.jetbrains.jewel.markdown.MarkdownBlock.ListBlock
import org.jetbrains.jewel.markdown.MarkdownBlock.ListBlock.BulletList
import org.jetbrains.jewel.markdown.MarkdownBlock.ListBlock.OrderedList
import org.jetbrains.jewel.markdown.MarkdownBlock.ListItem
import org.jetbrains.jewel.markdown.MarkdownBlock.Paragraph
import org.jetbrains.jewel.markdown.MarkdownBlock.ThematicBreak
import org.junit.Assert

fun List<MarkdownBlock>.assertEquals(vararg expected: MarkdownBlock) {
    val differences = findDifferences(expected.toList(), indentSize = 0)
    Assert.assertTrue(
        "The following differences were found:\n\n" +
            "${differences.joinToString("\n").replace('\t', '→')}\n\n",
        differences.isEmpty(),
    )
}

fun List<MarkdownBlock>.findDifferences(
    expected: List<MarkdownBlock>,
    indentSize: Int,
): List<String> = buildList {
    val indent = " ".repeat(indentSize)
    val thisSize = this@findDifferences.size
    if (expected.size != thisSize) {
        add("$indent * Content size mismatch. Was $thisSize, but we expected ${expected.size}")
        add("$indent     Actual:   ${this@findDifferences}")
        add("$indent     Expected: $expected\n")
        add("$indent   ℹ️ Note: skipping cells comparison as it's meaningless")
        return@buildList
    }

    for ((i, item) in this@findDifferences.withIndex()) {
        val difference = item.findDifferenceWith(expected[i], indentSize + 2)
        if (difference.isNotEmpty()) {
            add(
                "$indent * Item #$i is not the same as the expected value.\n\n" +
                    "${difference.joinToString("\n")}\n",
            )
        }
    }
}

private fun MarkdownBlock.findDifferenceWith(
    expected: MarkdownBlock,
    indentSize: Int,
): List<String> {
    val indent = " ".repeat(indentSize)
    if (this.javaClass != expected.javaClass) {
        return listOf(
            "$indent * Block type mismatch.\n\n" +
                "$indent     Actual:   ${javaClass.name}\n" +
                "$indent     Expected: ${expected.javaClass.name}\n",
        )
    }

    return when (this) {
        is Paragraph -> diffParagraph(this, expected, indent)
        is HtmlBlock,
        is BlockQuote,
        is ListItem,
        -> diffMarkdownBlock(this, expected, indent)
        is FencedCodeBlock -> diffFencedCodeBlock(this, expected, indent)
        is IndentedCodeBlock -> diffIndentedCodeBlock(this, expected, indent)
        is Heading -> diffHeading(this, expected, indent)
        is ListBlock -> diffList(this, expected, indent)
        is ThematicBreak -> emptyList() // They can only differ in their node
        else -> error("Unsupported MarkdownBlock: ${this.javaClass.name}")
    }
}

private var htmlRenderer = HtmlRenderer.builder().build()

fun BlockWithInlineMarkdown.toHtml() = buildString {
    for (node in this@toHtml.inlineContent) {
        // new lines are rendered as spaces in tests
        append(htmlRenderer.render(node.value).replace("\n", " "))
    }
}

private fun diffParagraph(actual: Paragraph, expected: MarkdownBlock, indent: String) = buildList {
    val actualInlineHtml = actual.toHtml()
    val expectedInlineHtml = (expected as Paragraph).toHtml()
    if (actualInlineHtml != expectedInlineHtml) {
        add(
            "$indent * Paragraph raw content mismatch.\n\n" +
                "$indent     Actual:   $actualInlineHtml\n" +
                "$indent     Expected: $expectedInlineHtml\n",
        )
    }
}

private fun diffMarkdownBlock(actual: MarkdownBlock, expected: MarkdownBlock, indent: String) = buildList {
    for ((c1, c2) in actual.children.zip(expected.children)) {
        val differences = c2!!.findDifferenceWith(c1!!, indent.length + 1)
        if (differences.isNotEmpty()) {
            add("$indent * ${actual.javaClass.name} content mismatch:\n\n")
            addAll(differences)
        }
    }
}

private fun diffFencedCodeBlock(actual: FencedCodeBlock, expected: MarkdownBlock, indent: String) =
    buildList {
        if (actual.mimeType != (expected as FencedCodeBlock).mimeType) {
            add(
                "$indent * Fenced code block mime type mismatch.\n\n" +
                    "$indent     Actual:   ${actual.mimeType}\n" +
                    "$indent     Expected: ${expected.mimeType}",
            )
        }

        if (actual.value.literal.trim() != expected.value.literal.trim()) {
            add(
                "$indent * Fenced code block content mismatch.\n\n" +
                    "$indent     Actual:   ${actual.value.literal}\n" +
                    "$indent     Expected: ${expected.value.literal}",
            )
        }
    }

private fun diffIndentedCodeBlock(actual: IndentedCodeBlock, expected: MarkdownBlock, indent: String) =
    buildList {
        if (actual.value.literal.trim() != (expected as IndentedCodeBlock).value.literal.trim()) {
            add(
                "$indent * Indented code block content mismatch.\n\n" +
                    "$indent     Actual:   ${actual.value.literal}\n" +
                    "$indent     Expected: ${expected.value.literal}",
            )
        }
    }

private fun diffHeading(actual: Heading, expected: MarkdownBlock, indent: String) = buildList {
    val actualInlineHtml = actual.toHtml()
    val expectedInlineHtml = (expected as Heading).toHtml()
    if (actualInlineHtml != expectedInlineHtml) {
        add(
            "$indent * Heading raw content mismatch.\n\n" +
                "$indent     Actual:   $actualInlineHtml\n" +
                "$indent     Expected: $expectedInlineHtml",
        )
    }
}

private fun diffList(actual: ListBlock, expected: MarkdownBlock, indent: String) =
    buildList {
        addAll(diffMarkdownBlock(actual, expected, indent))
        when (actual) {
            is OrderedList -> {
                if (expected !is OrderedList) {
                    add("$indent     Actual: OrderedList, Expected: ${expected.javaClass.name}")
                    return@buildList
                }
                if (actual.value.isTight != expected.value.isTight) {
                    add(
                        "$indent * List isTight mismatch.\n\n" +
                            "$indent     Actual:   ${actual.value.isTight}\n" +
                            "$indent     Expected: ${expected.value.isTight}",
                    )
                }
                if (actual.value.markerStartNumber != expected.value.markerStartNumber) {
                    add(
                        "$indent * List startFrom mismatch.\n\n" +
                            "$indent     Actual:   ${actual.value.markerStartNumber}\n" +
                            "$indent     Expected: ${expected.value.markerStartNumber}",
                    )
                }

                if (actual.value.markerDelimiter != expected.value.markerDelimiter) {
                    add(
                        "$indent * List delimiter mismatch.\n\n" +
                            "$indent     Actual:   ${actual.value.markerDelimiter}\n" +
                            "$indent     Expected: ${expected.value.markerDelimiter}",
                    )
                }
            }

            is BulletList -> {
                if (expected !is BulletList) {
                    add("$indent     Actual: BulletList, Expected: ${expected.javaClass.name}")
                    return@buildList
                }
                if (actual.value.isTight != expected.value.isTight) {
                    add(
                        "$indent * List isTight mismatch.\n\n" +
                            "$indent     Actual:   ${actual.value.isTight}\n" +
                            "$indent     Expected: ${expected.value.isTight}",
                    )
                }
                if (actual.value.marker != expected.value.marker) {
                    add(
                        "$indent * List bulletMarker mismatch.\n\n" +
                            "$indent     Actual:   ${actual.value.marker}\n" +
                            "$indent     Expected: ${expected.value.marker}",
                    )
                }
            }

            else -> error(actual.value.toString())
        }
    }

private val parser = Parser.builder().build()

private fun Node.children() = buildList {
    var child = firstChild
    while (child != null) {
        add(child)
        child = child.next
    }
}

/** skip root Document and Paragraph nodes */
private fun inlineMarkdowns(content: String): List<InlineMarkdown> {
    val document = parser.parse(content).firstChild ?: return emptyList()
    return if (document.firstChild is org.commonmark.node.Paragraph) {
        document.firstChild
    } else {
        document
    }.children().map { x -> x.toInlineNode() }
}

private val inlineParser = InlineParserImpl(InlineParserContextImpl(emptyList(), LinkReferenceDefinitions()))

private fun <B : Block> B.addInlineNodes(content: String): B {
    inlineParser.parse(SourceLines.of(content.lines().map { SourceLine.of(it, null) }), this)
    return this
}

fun paragraph(@Language("Markdown") content: String): Paragraph = Paragraph(
    org.commonmark.node.Paragraph().addInlineNodes(content),
)

fun heading(level: Int, @Language("Markdown") content: String) = Heading(
    org.commonmark.node.Heading().apply {
        this.addInlineNodes(content)
        this.level = level
    },
)

fun indentedCodeBlock(content: String) = IndentedCodeBlock(
    org.commonmark.node.IndentedCodeBlock().apply {
        this.literal = content
    },
)

fun fencedCodeBlock(content: String, mimeType: MimeType? = null) = FencedCodeBlock(
    org.commonmark.node.FencedCodeBlock().apply {
        this.literal = content
        this.info = mimeType?.displayName()
    },
)

fun blockQuote(vararg contents: MarkdownBlock) = BlockQuote(
    org.commonmark.node.BlockQuote().apply {
        contents.forEach {
            this.appendChild(it.value)
        }
    },
)

fun unorderedList(
    vararg items: MarkdownBlock,
    isTight: Boolean = true,
    bulletMarker: String = "-",
) = BulletList(
    org.commonmark.node.BulletList().apply {
        this.marker = bulletMarker
        this.isTight = isTight
        items.forEach {
            this.appendChild(it.value)
        }
    },
)

fun orderedList(
    vararg items: MarkdownBlock,
    isTight: Boolean = true,
    startFrom: Int = 1,
    delimiter: String = ".",
) = OrderedList(
    org.commonmark.node.OrderedList().apply {
        items.forEach {
            this.appendChild(it.value)
        }
        this.isTight = isTight
        this.markerStartNumber = startFrom
        this.markerDelimiter = delimiter
    },
)

fun listItem(vararg items: MarkdownBlock) = ListItem(
    org.commonmark.node.ListItem().apply {
        items.forEach {
            this.appendChild(it.value)
        }
    },
)

fun htmlBlock(content: String) = HtmlBlock(
    org.commonmark.node.HtmlBlock().apply {
        this.literal = content
    },
)

fun thematicBreak() = ThematicBreak(org.commonmark.node.ThematicBreak())
