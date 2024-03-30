package org.jetbrains.jewel.markdown.processing

import org.commonmark.Extension
import org.commonmark.parser.Parser
import org.commonmark.parser.Parser.ParserExtension
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlNodeRendererFactory
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.html.HtmlRenderer.HtmlRendererExtension

/**
 * Extension for adding task list items.
 *
 *
 * Create it with [.create] and then configure it on the builders
 * ([org.commonmark.parser.Parser.Builder.extensions],
 * [HtmlRenderer.Builder.extensions]).
 *
 *
 * @since 0.15.0
 */
public class TaskListItemsExtension private constructor() : ParserExtension, HtmlRendererExtension {

    public override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.postProcessor(TaskListItemPostProcessor())
    }

    public override fun extend(rendererBuilder: HtmlRenderer.Builder) {
        TODO()
    }

    public companion object {

        public fun create(): Extension {
            return TaskListItemsExtension()
        }
    }
}