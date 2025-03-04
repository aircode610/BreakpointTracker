package com.amirali.myplugin.breakpointtracker.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class BreakpointToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // Create our browser panel
        val breakpointPanel = BreakpointBrowserPanel(project)

        // Create a content element from our panel
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(
            breakpointPanel.component,
            "Breakpoints",
            false
        )

        // Add the content to the tool window
        toolWindow.contentManager.addContent(content)
    }
}