package com.amirali.myplugin.breakpointtracker.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class BreakpointToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // Creat browser panel
        val breakpointPanel = BreakpointBrowserPanel(project)

        // Create content
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(
            breakpointPanel.component,
            "Breakpoint Tracker",
            false
        )

        // Add content to tool window
        toolWindow.contentManager.addContent(content)
    }
}