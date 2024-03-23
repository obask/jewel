package org.jetbrains.jewel.markdown.rendering

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.dp
import org.commonmark.node.Block
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.CustomBlock
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
import org.commonmark.node.ThematicBreak
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.modifier.onHover
import org.jetbrains.jewel.foundation.theme.LocalContentColor
import org.jetbrains.jewel.markdown.MimeType
import org.jetbrains.jewel.markdown.extensions.MarkdownRendererExtension
import org.jetbrains.jewel.markdown.processing.forEachChild
import org.jetbrains.jewel.ui.Orientation.Horizontal
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Text

@ExperimentalJewelApi
public open class DefaultMarkdownBlockRenderer(
    private val rootStyling: MarkdownStyling,
    private val rendererExtensions: List<MarkdownRendererExtension>,
    private val inlineRenderer: InlineMarkdownRenderer,
    private val onUrlClick: (String) -> Unit,
) : MarkdownBlockRenderer {

    @Composable
    override fun render(blocks: List<Node>) {
        Column(verticalArrangement = Arrangement.spacedBy(rootStyling.blockVerticalSpacing)) {
            for (block in blocks) {
                render(block)
            }
        }
    }

    @Composable
    override fun render(block: Node) {
        when (block) {
            is BlockQuote -> render(block, rootStyling.blockQuote)
            is FencedCodeBlock -> render(block, rootStyling.code.fenced)
            is IndentedCodeBlock -> render(block, rootStyling.code.indented)
            is Heading -> when (block.level) {
                1 -> render(block, rootStyling.heading.h1)
                2 -> render(block, rootStyling.heading.h2)
                3 -> render(block, rootStyling.heading.h3)
                4 -> render(block, rootStyling.heading.h4)
                5 -> render(block, rootStyling.heading.h5)
                6 -> render(block, rootStyling.heading.h6)
            }

            is HtmlBlock -> render(block, rootStyling.htmlBlock)
            is Image -> render(block, rootStyling.image)
            is BulletList -> render(block, rootStyling.list.unordered)
            is OrderedList -> render(block, rootStyling.list.ordered)
            is ListItem -> render(block)
            is Paragraph -> render(block, rootStyling.paragraph)
            is ThematicBreak -> renderThematicBreak(rootStyling.thematicBreak)
            is CustomBlock -> {
                rendererExtensions.find { it.blockRenderer.canRender(block) }
                    ?.blockRenderer?.render(block, this, inlineRenderer)
            }
        }
    }

    @Composable
    override fun render(block: Block) {
        TODO("Not yet implemented")
    }

    @Composable
    override fun render(block: Paragraph, styling: MarkdownStyling.Paragraph) {
        val renderedContent = rememberRenderedContent(block, styling.inlinesStyling)
        SimpleClickableText(text = renderedContent, textStyle = styling.inlinesStyling.textStyle)
    }

    @Composable
    override fun render(block: Heading, styling: MarkdownStyling.Heading) {
        TODO("Not yet implemented")
    }

    @Composable
    override fun render(block: Heading, styling: MarkdownStyling.Heading.HX) {
        val renderedContent = rememberRenderedContent(block, styling.inlinesStyling)
        Heading(
            renderedContent,
            styling.inlinesStyling.textStyle,
            styling.padding,
            styling.underlineWidth,
            styling.underlineColor,
            styling.underlineGap,
        )
    }

    @Composable
    private fun Heading(
        renderedContent: AnnotatedString,
        textStyle: TextStyle,
        paddingValues: PaddingValues,
        underlineWidth: Dp,
        underlineColor: Color,
        underlineGap: Dp,
    ) {
        Column(modifier = Modifier.padding(paddingValues)) {
            SimpleClickableText(text = renderedContent, textStyle = textStyle)

            if (underlineWidth > 0.dp && underlineColor.isSpecified) {
                Spacer(Modifier.height(underlineGap))
                Divider(Horizontal, color = underlineColor, thickness = underlineWidth)
            }
        }
    }

    @Composable
    override fun render(
        block: BlockQuote,
        styling: MarkdownStyling.BlockQuote,
    ) {
        Column(
            Modifier.drawBehind {
                val isLtr = layoutDirection == Ltr
                val lineWidthPx = styling.lineWidth.toPx()
                val x = if (isLtr) lineWidthPx / 2 else size.width - lineWidthPx / 2

                drawLine(
                    styling.lineColor,
                    Offset(x, 0f),
                    Offset(x, size.height),
                    lineWidthPx,
                    styling.strokeCap,
                    styling.pathEffect,
                )
            }
                .padding(styling.padding),
            verticalArrangement = Arrangement.spacedBy(rootStyling.blockVerticalSpacing),
        ) {
            CompositionLocalProvider(LocalContentColor provides styling.textColor) {
                render(buildList { block.forEachChild { add(it) } })
            }
        }
    }

    @Composable
    override fun render(block: ListBlock, styling: MarkdownStyling.List) {
        when (block) {
            is OrderedList -> render(block, styling.ordered)
            is BulletList -> render(block, styling.unordered)
        }
    }

    @Composable
    override fun render(block: OrderedList, styling: MarkdownStyling.List.Ordered) {
        val itemSpacing =
            if (block.isTight) {
                styling.itemVerticalSpacingTight
            } else {
                styling.itemVerticalSpacing
            }

        Column(
            modifier = Modifier.padding(styling.padding),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            var index = 0
            block.forEachChild { item ->
                Row {
                    val number = block.markerStartNumber + index
                    Text(
                        text = "$number${block.markerDelimiter}",
                        style = styling.numberStyle,
                        modifier = Modifier.widthIn(min = styling.numberMinWidth)
                            .pointerHoverIcon(PointerIcon.Default, overrideDescendants = true),
                        textAlign = styling.numberTextAlign,
                    )

                    Spacer(Modifier.width(styling.numberContentGap))

                    render(item)
                }
                index += 1
            }
        }
    }

    @Composable
    override fun render(block: BulletList, styling: MarkdownStyling.List.Unordered) {
        val itemSpacing =
            if (block.isTight) {
                styling.itemVerticalSpacingTight
            } else {
                styling.itemVerticalSpacing
            }

        Column(
            modifier = Modifier.padding(styling.padding),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            block.forEachChild { item ->
                Row {
                    Text(
                        text = styling.bullet.toString(),
                        style = styling.bulletStyle,
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Default, overrideDescendants = true),
                    )

                    Spacer(Modifier.width(styling.bulletContentGap))

                    render(item)
                }
            }
        }
    }

    @Composable
    override fun render(block: ListItem) {
        Column(verticalArrangement = Arrangement.spacedBy(rootStyling.blockVerticalSpacing)) {
            render(buildList { block.forEachChild { add(it) } })
        }
    }

    @Composable
    override fun render(block: IndentedCodeBlock, styling: MarkdownStyling.Code.Indented) {
        HorizontallyScrollingContainer(
            isScrollable = styling.scrollsHorizontally,
            Modifier.background(styling.background, styling.shape)
                .border(styling.borderWidth, styling.borderColor, styling.shape)
                .then(if (styling.fillWidth) Modifier.fillMaxWidth() else Modifier),
        ) {
            Text(
                text = block.literal,
                style = styling.textStyle,
                modifier = Modifier.padding(styling.padding)
                    .pointerHoverIcon(PointerIcon.Default, overrideDescendants = true),
            )
        }
    }

    @Composable
    override fun render(block: FencedCodeBlock, styling: MarkdownStyling.Code.Fenced) {
        HorizontallyScrollingContainer(
            isScrollable = styling.scrollsHorizontally,
            Modifier.background(styling.background, styling.shape)
                .border(styling.borderWidth, styling.borderColor, styling.shape)
                .then(if (styling.fillWidth) Modifier.fillMaxWidth() else Modifier),
        ) {
            val mimeType = remember { MimeType.Known.fromMarkdownLanguageName(block.info) }
            Column(Modifier.padding(styling.padding)) {
                if (mimeType != null && styling.infoPosition.verticalAlignment == Alignment.Top) {
                    FencedBlockInfo(
                        mimeType.displayName(),
                        styling.infoPosition.horizontalAlignment
                            ?: error("No horizontal alignment for position ${styling.infoPosition.name}"),
                        styling.infoTextStyle,
                        Modifier.fillMaxWidth().padding(styling.infoPadding),
                    )
                }

                Text(
                    text = block.literal,
                    style = styling.textStyle,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Default, overrideDescendants = true),
                )

                if (mimeType != null && styling.infoPosition.verticalAlignment == Alignment.Bottom) {
                    FencedBlockInfo(
                        mimeType.displayName(),
                        styling.infoPosition.horizontalAlignment
                            ?: error("No horizontal alignment for position ${styling.infoPosition.name}"),
                        styling.infoTextStyle,
                        Modifier.fillMaxWidth().padding(styling.infoPadding),
                    )
                }
            }
        }
    }

    @Composable
    private fun FencedBlockInfo(
        infoText: String,
        alignment: Alignment.Horizontal,
        textStyle: TextStyle,
        modifier: Modifier,
    ) {
        Column(modifier, horizontalAlignment = alignment) {
            DisableSelection { Text(infoText, style = textStyle) }
        }
    }

    @Composable
    override fun render(block: Image, styling: MarkdownStyling.Image) {
        // TODO implement image rendering support (will require image loading)
        Text(
            "⚠️ Images are not supported yet",
            Modifier.border(1.dp, Color.Red)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .pointerHoverIcon(PointerIcon.Default, overrideDescendants = true),
            color = Color.Red,
        )
    }

    @Composable
    override fun renderThematicBreak(styling: MarkdownStyling.ThematicBreak) {
        Box(Modifier.padding(styling.padding).height(styling.lineWidth).background(styling.lineColor))
    }

    @Composable
    override fun render(block: HtmlBlock, styling: MarkdownStyling.HtmlBlock) {
        // HTML blocks are intentionally not rendered
    }

    @Composable
    private fun rememberRenderedContent(block: Node, styling: InlinesStyling) =
        remember(block, styling) {
            inlineRenderer.renderAsAnnotatedString(block, styling)
        }

    @OptIn(ExperimentalTextApi::class)
    @Composable
    private fun SimpleClickableText(
        text: AnnotatedString,
        textStyle: TextStyle,
        modifier: Modifier = Modifier,
    ) {
        var pointerIcon by remember { mutableStateOf(PointerIcon.Default) }

        ClickableText(
            text = text,
            style = textStyle.merge(LocalContentColor.current),
            modifier = modifier.pointerHoverIcon(pointerIcon, true),
            onHover = { offset ->
                pointerIcon =
                    if (offset == null || text.getUrlAnnotations(offset, offset).isEmpty()) {
                        PointerIcon.Default
                    } else {
                        PointerIcon.Hand
                    }
            },
        ) { offset ->
            val span = text.getUrlAnnotations(offset, offset).firstOrNull() ?: return@ClickableText
            onUrlClick(span.item.url)
        }
    }

    @Composable
    private fun HorizontallyScrollingContainer(
        isScrollable: Boolean,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ) {
        var isHovered by remember { mutableStateOf(false) }

        Layout(
            content = {
                val scrollState = rememberScrollState()
                Box(
                    Modifier.layoutId("mainContent")
                        .then(if (isScrollable) Modifier.horizontalScroll(scrollState) else Modifier),
                ) {
                    content()
                }

                val canScroll by derivedStateOf {
                    scrollState.canScrollBackward || scrollState.canScrollForward
                }

                if (isScrollable && canScroll) {
                    val alpha by animateFloatAsState(
                        if (isHovered) 1f else 0f,
                        tween(durationMillis = 150, easing = LinearEasing),
                    )

                    HorizontalScrollbar(
                        rememberScrollbarAdapter(scrollState),
                        Modifier.layoutId("containerHScrollbar")
                            .padding(start = 2.dp, end = 2.dp, bottom = 2.dp)
                            .alpha(alpha),
                    )
                }
            },
            modifier.onHover { isHovered = it },
            { measurables, incomingConstraints ->
                val contentMeasurable =
                    measurables.singleOrNull { it.layoutId == "mainContent" }
                        ?: error("There must be one and only one child with ID 'mainContent'")

                val contentPlaceable = contentMeasurable.measure(incomingConstraints)

                val scrollbarMeasurable = measurables.find { it.layoutId == "containerHScrollbar" }
                val scrollbarPlaceable = scrollbarMeasurable?.measure(incomingConstraints)
                val scrollbarHeight = scrollbarPlaceable?.measuredHeight ?: 0

                layout(contentPlaceable.width, contentPlaceable.height + scrollbarHeight) {
                    contentPlaceable.place(0, 0)
                    scrollbarPlaceable?.place(0, contentPlaceable.height)
                }
            },
        )
    }
}
