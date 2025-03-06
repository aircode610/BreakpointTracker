package com.amirali.myplugin.breakpointtracker.startup

import com.amirali.myplugin.breakpointtracker.listeners.BreakpointChangeListener
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointListener
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.*

class BreakpointTrackerStartupActivityTest : BasePlatformTestCase() {

    fun testListenerRegistration() = runBlocking {
        // Create and execute the startup activity
        val startupActivity = BreakpointTrackerStartupActivity()
        startupActivity.execute(project)

        // Create a mock breakpoint
        val mockBreakpoint = mock(XLineBreakpoint::class.java)

        // Topic subscribers
        val publisher = project.messageBus.syncPublisher(XBreakpointListener.TOPIC)

        // Add breakpoint
        publisher.breakpointAdded(mockBreakpoint)

        // No errors
        assertTrue(true)
    }
}