package org.jetbrains.jewel.markdown

import org.jetbrains.jewel.markdown.processing.MarkdownProcessor
import org.junit.Test

class MarkdownProcessorDocumentParsingExtraTest {

    private val processor = MarkdownProcessor()

    @Test
    fun `should parse spec sample 461b correctly (Emphasis and strong emphasis)`() {
        val parsed = processor.processMarkdownDocument("*_foo *bar*_*")

        /*
         * Expected HTML:
         * <p><em><em>foo <em>bar</em></em></em></p>
         */
        parsed.assertEquals(paragraph("*_foo *bar*_*"))
    }

    @Test
    fun `should parse spec sample 461c correctly (Emphasis and strong emphasis)`() {
        val parsed = processor.processMarkdownDocument("**foo *bar***")

        /*
         * Expected HTML:
         * <p><strong>foo <em>bar</em></strong></p>
         */
        parsed.assertEquals(paragraph("**foo *bar***"))
    }

    @Test
    fun `should parse spec sample 461d correctly (Emphasis and strong emphasis)`() {
        val parsed = processor.processMarkdownDocument("*_foo *bar* a_*")

        /*
         * Expected HTML:
         * <p><em><em>foo <em>bar</em> a</em></em></p>
         */
        parsed.assertEquals(paragraph("*_foo *bar* a_*"))
    }

    @Test
    fun `should parse spec sample 461e correctly (Emphasis and strong emphasis)`() {
        val parsed = processor.processMarkdownDocument("**foo *bar* a**")

        /*
         * Expected HTML:
         * <p><strong>foo <em>bar</em> a</strong></p>
         */
        parsed.assertEquals(paragraph("**foo *bar* a**"))
    }

    @Test
    fun `should parse spec sample 461f correctly (Emphasis and strong emphasis)`() {
        val parsed = processor.processMarkdownDocument("*_*foo *bar* a*_*")

        /*
         * Expected HTML:
         * <p><strong>foo <em>bar</em> a</strong></p>
         */
        parsed.assertEquals(paragraph("*_*foo *bar* a*_*"))
    }

    @Test
    fun `test me`() {
        val parsed = processor.processMarkdownDocument("""
            
            # Foam

            **Foam** is a personal knowledge management and sharing system inspired by [Roam Research](https://roamresearch.com/), built on [Visual Studio Code](https://code.visualstudio.com/) and [GitHub](https://github.com/).

            You can use **Foam** for organising your research, keeping re-discoverable notes, writing long-form content and, optionally, publishing it to the web.

            **Foam** is free, open source, and extremely extensible to suit your personal workflow. You own the information you create with Foam, and you're free to share it, and collaborate on it with anyone you want.

            <p class="announcement">
              <b>New!</b> Join <a href="https://foambubble.github.io/join-discord/w" target="_blank">Foam community Discord</a> for users and contributors!
            </p>

            <div class="website-only">
                <a class="github-button" href="https://github.com/foambubble/foam" data-icon="octicon-star" data-size="large" data-show-count="true" aria-label="Star foambubble/foam on GitHub">Star</a>
                <a class="github-button" href="https://github.com/foambubble/foam-template" data-icon="octicon-repo-template" data-size="large" aria-label="Use this template foambubble/foam-template on GitHub">Use this template</a>
            </div>

            ## Table of Contents

            - [Foam](#foam)
              - [Table of Contents](#table-of-contents)
              - [How do I use Foam?](#how-do-i-use-foam)
              - [What's in a Foam?](#whats-in-a-foam)
              - [Getting started](#getting-started)
              - [Features](#features)
              - [Call To Adventure](#call-to-adventure)
              - [Thanks and attribution](#thanks-and-attribution)
              - [License](#license)

            ## How do I use Foam?

            **Foam** is a tool that supports creating relationships between thoughts and information to help you think better.

            Whether you want to build a [Second Brain](https://www.buildingasecondbrain.com/) or a [Zettelkasten](https://zettelkasten.de/posts/overview/), write a book, or just get better at long-term learning, **Foam** can help you organise your thoughts if you follow these simple rules:

            1. Create a single **Foam** workspace for all your knowledge and research following the [Getting started](#getting-started) guide.
            2. Write your thoughts in markdown documents (I like to call them **Bubbles**, but that might be more than a little twee). These documents should be atomic: Put things that belong together into a single document, and limit its content to that single topic. ([source](https://zettelkasten.de/posts/overview/#principles))
            3. Use Foam's shortcuts and autocompletions to link your thoughts together with `[[wikilinks]]`, and navigate between them to explore your knowledge graph.
            4. Get an overview of your **Foam** workspace using a [[graph-visualization]] (⚠️ WIP), and discover relationships between your thoughts with the use of [[backlinking]].

            Foam is a like a bathtub: _What you get out of it depends on what you put into it._

            ## What's in a Foam?

            Like the soapy suds it's named after, **Foam** is mostly air.

            1. The editing experience of **Foam** is powered by VS Code, enhanced by workspace settings that glue together [[recommended-extensions]] and preferences optimised for writing and navigating information.
            2. To back up, collaborate on and share your content between devices, Foam pairs well with [GitHub](http://github.com/).
            3. To publish your content, you can set it up to publish to [GitHub Pages](https://pages.github.com/), or to any website hosting platform like [Netlify](http://netlify.com/) or [Vercel](https://vercel.com).

            > **Fun fact**: This documentation was researched, written and published using **Foam**.

            ## Getting started

            > ⚠️ Foam is still in preview. Expect the experience to be a little rough.

            These instructions assume you have a GitHub account, and you have Visual Studio Code installed.

            1. Use the [foam-template project](https://github.com/foambubble/foam-template) to generate a new repository. If you're logged into GitHub, you can just hit this button:

               <a class="github-button" href="https://github.com/foambubble/foam-template/generate" data-icon="octicon-repo-template" data-size="large" aria-label="Use this template foambubble/foam-template on GitHub">Use this template</a>

               *If you want to keep your thoughts to yourself, remember to set the repository private, or if you don't want to use GitHub to host your workspace at all, choose [**Download as ZIP**](https://github.com/foambubble/foam-template/archive/master.zip) instead of **Use this template**.*

            2. [Clone the repository locally](https://help.github.com/en/github/creating-cloning-and-archiving-repositories/cloning-a-repository) and open it in VS Code.

               *Open the repository as a folder using the `File > Open...` menu item. In VS Code, "open workspace" refers to [multi-root workspaces](https://code.visualstudio.com/docs/editor/multi-root-workspaces).*

            3. When prompted to install recommended extensions, click **Install all** (or **Show Recommendations** if you want to review and install them one by one)

            After setting up the repository, open `.vscode/settings.json` and edit, add or remove any settings you'd like for your Foam workspace.

            * *If using a [multi-root workspace](https://code.visualstudio.com/docs/editor/multi-root-workspaces) as noted above, make sure that your **Foam** directory is first in the list. There are some settings that will need to be migrated from `.vscode/settings.json` to your `.code-workspace` file.*

            To learn more about how to use **Foam**, read the [[recipes]].

            Getting stuck in the setup? Read the [[frequently-asked-questions]].

            Check our [issues on GitHub](http://github.com/foambubble/foam/issues) if you get stuck on something, and create a new one if something doesn't seem right!

            ## Features

            **Foam** doesn't have features in the traditional sense. Out of the box, you have access to all features of VS Code and all the [[recommended-extensions]] you choose to install, but it's up to you to discover what you can do with it!

            ![Short video of Foam in use](assets/images/foam-navigation-demo.gif)

            Head over to [[recipes]] for some useful patterns and ideas!

            ## Call To Adventure

            The goal of **Foam** is to be your personal companion on your quest for knowledge.

            It's currently about "10% ready" relative to all the features I've thought of, but I've only thought of ~1% of the features it could have, and I'm excited to learn from others.

            I am using it as my personal thinking tool. By making it public, I hope to learn from others not only how to improve Foam, but also to improve how I learn and manage information.

            If that sounds like something you're interested in, I'd love to have you along on the journey.

            - Read about our [[principles]] to understand Foam's philosophy and direction
            - Read the [[contribution-guide]] guide to learn how to participate.
            - Feel free to open [GitHub issues](https://github.com/foambubble/foam/issues) to give me feedback and ideas for new features.

            ## Thanks and attribution

        """.trimIndent()
        )

        /*
         * Expected HTML:
         * <p><strong>foo <em>bar</em> a</strong></p>
         */
    }
}
