package com.amirali.myplugin.breakpointtracker.startup

import com.amirali.myplugin.breakpointtracker.listeners.BreakpointChangeListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.xdebugger.breakpoints.XBreakpointListener

class BreakpointTrackerStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        // Create our listener
        val listener = BreakpointChangeListener(project)

        // Connect to the project's message bus
        val messageBus = project.messageBus
        val connection = messageBus.connect()

        // Subscribe to breakpoint events
        connection.subscribe(XBreakpointListener.TOPIC, listener)
    }
}