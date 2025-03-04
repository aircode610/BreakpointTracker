package com.amirali.myplugin.breakpointtracker.listeners

// Local
import com.amirali.myplugin.breakpointtracker.services.BreakpointTrackingService

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointListener

class BreakpointChangeListener(private val project: Project) : XBreakpointListener<XBreakpoint<*>> {

    private val breakpointService = project.service<BreakpointTrackingService>()

    override fun breakpointAdded(breakpoint: XBreakpoint<*>) {
        breakpointService.addBreakpoint(breakpoint)
    }

    override fun breakpointRemoved(breakpoint: XBreakpoint<*>) {
        breakpointService.removeBreakpoint(breakpoint)
    }

    override fun breakpointChanged(breakpoint: XBreakpoint<*>) {
        breakpointService.updateBreakpoint(breakpoint)
    }
}