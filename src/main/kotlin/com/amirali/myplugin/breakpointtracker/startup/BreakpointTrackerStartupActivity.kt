package com.amirali.myplugin.breakpointtracker.startup

import com.amirali.myplugin.breakpointtracker.listeners.BreakpointChangeListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.xdebugger.breakpoints.XBreakpointListener

class BreakpointTrackerStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Create listener
        val listener = BreakpointChangeListener(project)

        // Connect to the message bus
        val messageBus = project.messageBus
        val connection = messageBus.connect()

        // Subscribe to breakpoint events
        connection.subscribe(XBreakpointListener.TOPIC, listener)
    }
}