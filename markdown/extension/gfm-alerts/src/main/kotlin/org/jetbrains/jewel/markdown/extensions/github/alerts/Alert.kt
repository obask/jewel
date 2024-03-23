package org.jetbrains.jewel.markdown.extensions.github.alerts

import org.commonmark.node.CustomBlock
import org.commonmark.node.Node

public sealed interface Alert {

    public val content: List<Node>

    public data class Note(override val content: List<Node>) : CustomBlock(), Alert

    public data class Tip(override val content: List<Node>) : CustomBlock(), Alert

    public data class Important(override val content: List<Node>) : CustomBlock(), Alert

    public data class Warning(override val content: List<Node>) : CustomBlock(), Alert

    public data class Caution(override val content: List<Node>) : CustomBlock(), Alert
}
