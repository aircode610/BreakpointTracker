package com.amirali.myplugin.breakpointtracker.ui

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.InputStream
import org.mockito.Mockito.*

class BreakpointBrowserPanelTest : BasePlatformTestCase() {

    fun testResourceLoading() {
        val panel = BreakpointBrowserPanel(project)
        
        val method = BreakpointBrowserPanel::class.java.getDeclaredMethod(
            "loadResourceAsString", String::class.java)
        method.isAccessible = true

        // Load HTML resource
        val html = method.invoke(panel,
            "/com/amirali/myplugin/breakpointtracker/ui/breakpoint_tracker.html")

        assertNotNull(html)
        assertTrue((html as String).contains("<!DOCTYPE html>"))
    }

}