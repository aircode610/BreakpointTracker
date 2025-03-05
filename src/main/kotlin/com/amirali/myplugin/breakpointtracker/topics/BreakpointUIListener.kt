package com.amirali.myplugin.breakpointtracker.topics

import com.intellij.util.messages.Topic

/**
 * Interface defining listener for responsive breakpoint UI updates.
 */
interface BreakpointUIListener {
    companion object {
        @JvmStatic
        val TOPIC = Topic.create("Breakpoint UI updates", BreakpointUIListener::class.java)
    }

    fun breakpointsChanged()
}