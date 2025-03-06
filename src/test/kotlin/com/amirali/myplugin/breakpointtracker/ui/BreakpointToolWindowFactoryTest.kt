package com.amirali.myplugin.breakpointtracker.ui

import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.content.ContentManager
import org.mockito.Mockito.*

class BreakpointToolWindowFactoryTest : BasePlatformTestCase() {

    fun testToolWindowCreation() {
        val factory = BreakpointToolWindowFactory()

        // Mock the tool window components
        val toolWindow = mock(ToolWindow::class.java)
        val contentManager = mock(ContentManager::class.java)
        `when`(toolWindow.contentManager).thenReturn(contentManager)

        // Create the tool window content
        factory.createToolWindowContent(project, toolWindow)

        // Verify content was added
        verify(contentManager, times(1)).addContent(any())
    }
}