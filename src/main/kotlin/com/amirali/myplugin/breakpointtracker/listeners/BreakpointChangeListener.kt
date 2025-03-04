package com.amirali.myplugin.breakpointtracker.listeners

import com.amirali.myplugin.breakpointtracker.services.BreakpointTrackingService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointListener

/**
 * Listens for breakpoint events and updates the BreakpointTrackingService accordingly.
 */
class BreakpointChangeListener(private val project: Project) : XBreakpointListener<XBreakpoint<*>> {

    private val LOG = Logger.getInstance(BreakpointChangeListener::class.java)
    private val breakpointService = project.service<BreakpointTrackingService>()

    override fun breakpointAdded(breakpoint: XBreakpoint<*>) {
        LOG.debug("Breakpoint added event received")
        breakpointService.addBreakpoint(breakpoint)
    }

    override fun breakpointRemoved(breakpoint: XBreakpoint<*>) {
        LOG.debug("Breakpoint removed event received")
        breakpointService.removeBreakpoint(breakpoint)
    }

    override fun breakpointChanged(breakpoint: XBreakpoint<*>) {
        LOG.debug("Breakpoint changed event received")
        breakpointService.updateBreakpoint(breakpoint)
    }
}