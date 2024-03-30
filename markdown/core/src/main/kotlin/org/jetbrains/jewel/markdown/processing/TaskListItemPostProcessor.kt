package org.jetbrains.jewel.markdown.processing

import org.commonmark.node.AbstractVisitor
import org.commonmark.node.CustomNode
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.Text
import org.commonmark.parser.PostProcessor
import java.util.regex.Pattern

public class TaskListItemPostProcessor : PostProcessor {

    public override fun process(node: Node): Node {
        val visitor = TaskListItemVisitor()
        node.accept(visitor)
        return node
    }

    private class TaskListItemVisitor : AbstractVisitor() {

        override fun visit(listItem: ListItem) {
            val child = listItem.firstChild
            if (child is Paragraph) {
                val node = child.getFirstChild()
                if (node is Text) {
                    val textNode = node
                    val matcher = REGEX_TASK_LIST_ITEM.matcher(textNode.literal)
                    if (matcher.matches()) {
                        // Add the task list item marker node as the first child of the list item.
                        listItem.prependChild(object : CustomNode() {})

                        // Parse the node using the input after the task marker (in other words, group 2 from the matcher).
                        // (Note that the String has been trimmed, so we should add a space between the
                        // TaskListItemMarker and the text that follows it when we come to render it).
                        textNode.literal = matcher.group(2)
                    }
                }
            }
            visitChildren(listItem)
        }
    }

    public companion object {

        private val REGEX_TASK_LIST_ITEM: Pattern = Pattern.compile("^\\[([xX\\s])]\\s+(.*)")
    }
}