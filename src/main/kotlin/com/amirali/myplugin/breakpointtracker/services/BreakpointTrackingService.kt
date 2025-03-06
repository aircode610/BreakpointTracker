package com.amirali.myplugin.breakpointtracker.services

import com.amirali.myplugin.breakpointtracker.topics.BreakpointUIListener
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Service responsible for tracking all breakpoints in the project.
 */
@Service(Service.Level.PROJECT)
class BreakpointTrackingService(private val project: Project) {
    private val LOG = Logger.getInstance(BreakpointTrackingService::class.java)

    // Map to store line breakpoints and their lines
    private val lineBreakpoints = ConcurrentHashMap<String, MutableList<Int>>()

    // Map to store non-line breakpoints and their count
    private val otherBreakpoints = ConcurrentHashMap<String, AtomicInteger>()

    init {
        LOG.info("Initializing BreakpointTrackingService")
        loadExistingBreakpoints()
        notifyUIOfChanges()
    }

    /**
     * Notifies UI listeners that breakpoints have changed.
     */
    private fun notifyUIOfChanges() {
        project.messageBus.syncPublisher(BreakpointUIListener.TOPIC).breakpointsChanged()
    }

    /**
     * Loads all existing breakpoints from the project.
     */
    private fun loadExistingBreakpoints() {
        val breakpointManager = XDebuggerManager.getInstance(project).breakpointManager

        lineBreakpoints.clear()
        otherBreakpoints.clear()

        val allBreakpoints = breakpointManager.allBreakpoints

        for (breakpoint in allBreakpoints) {
            addBreakpointToMap(breakpoint)
        }

        LOG.info("Loaded ${getTotalBreakpoints()} breakpoints (${getLineBreakpointCount()} line, ${getOtherBreakpointCount()} other)")
    }

    /**
     * Adds a breakpoint to the tracking maps.
     *
     * @param breakpoint The breakpoint to add
     */
    fun addBreakpoint(breakpoint: XBreakpoint<*>) {
        addBreakpointToMap(breakpoint)
        LOG.debug("Added breakpoint. Total count: ${getTotalBreakpoints()}")
        notifyUIOfChanges()
    }

    /**
     * Removes a breakpoint from the tracking maps.
     *
     * @param breakpoint The breakpoint to remove
     */
    fun removeBreakpoint(breakpoint: XBreakpoint<*>) {
        if (breakpoint is XLineBreakpoint<*>) {
            removeLineBreakpoint(breakpoint)
        } else {
            removeOtherBreakpoint(breakpoint)
        }
        notifyUIOfChanges()
    }

    /**
     * Updates an existing breakpoint in the tracking maps.
     *
     * @param breakpoint The breakpoint that was changed
     */
    fun updateBreakpoint(breakpoint: XBreakpoint<*>) {
        removeBreakpoint(breakpoint)
        addBreakpoint(breakpoint)
        LOG.debug("Updated breakpoint")
    }

    /**
     * Helper method to add a breakpoint to the appropriate map.
     */
    private fun addBreakpointToMap(breakpoint: XBreakpoint<*>) {
        if (breakpoint is XLineBreakpoint<*>) {
            addLineBreakpoint(breakpoint)
        } else {
            addOtherBreakpoint(breakpoint)
        }
    }

    /**
     * Adds a line breakpoint to the line breakpoints map.
     */
    private fun addLineBreakpoint(breakpoint: XLineBreakpoint<*>) {
        val fileUrl = breakpoint.fileUrl
        if (fileUrl != null) {
            val file = VirtualFileManager.getInstance().findFileByUrl(fileUrl)

            if (file != null) {
                val filePath = file.path
                val line = breakpoint.line

                val lines = lineBreakpoints.getOrPut(filePath) { ArrayList() }

                if (!lines.contains(line)) {
                    lines.add(line)
                    LOG.debug("Added line breakpoint at $filePath:$line")
                }
            } else {
                LOG.warn("Could not find file for breakpoint: $fileUrl")
            }
        } else {
            LOG.warn("Line breakpoint has no file URL")
        }
    }

    /**
     * Adds a non-line breakpoint to the other breakpoints map.
     */
    private fun addOtherBreakpoint(breakpoint: XBreakpoint<*>) {
        val typeId = breakpoint.type.id

        val counter = otherBreakpoints.getOrPut(typeId) { AtomicInteger(0) }
        val newCount = counter.incrementAndGet()

        LOG.debug("Added $typeId breakpoint. Total: $newCount")
    }

    /**
     * Removes a line breakpoint from the tracking map.
     */
    private fun removeLineBreakpoint(breakpoint: XLineBreakpoint<*>) {
        val fileUrl = breakpoint.fileUrl
        if (fileUrl != null) {
            val file = VirtualFileManager.getInstance().findFileByUrl(fileUrl)

            if (file != null) {
                val filePath = file.path
                val line = breakpoint.line

                lineBreakpoints[filePath]?.let { lines ->
                    lines.remove(line)

                    if (lines.isEmpty()) {
                        lineBreakpoints.remove(filePath)
                    }

                    LOG.debug("Removed line breakpoint at $filePath:$line")
                }
            }
        }
    }

    /**
     * Removes a non-line breakpoint from the tracking map.
     */
    private fun removeOtherBreakpoint(breakpoint: XBreakpoint<*>) {
        val typeId = breakpoint.type.id
        otherBreakpoints[typeId]?.let { counter ->
            val count = counter.decrementAndGet()

            if (count <= 0) {
                otherBreakpoints.remove(typeId)
            }

            LOG.debug("Removed $typeId breakpoint. Remaining: $count")
        }
    }

    /**
     * Returns the line breakpoint data with shortened file paths for display.
     * Converts 0-based line numbers to 1-based for display.
     */
    fun getLineBreakpointDisplayData(): Map<String, List<Int>> {
        return lineBreakpoints.entries.associate { (path, lines) ->
            val filename = path.substringAfterLast('/')
            filename to lines.map { it + 1 }.sorted()
        }
    }

    /**
     * Returns the total number of line breakpoints.
     */
    fun getLineBreakpointCount(): Int {
        return lineBreakpoints.values.sumOf { it.size }
    }

    /**
     * Returns the total number of non-line breakpoints.
     */
    fun getOtherBreakpointCount(): Int {
        return otherBreakpoints.values.sumOf { it.get() }
    }

    /**
     * Returns the current total number of all breakpoints.
     */
    fun getTotalBreakpoints(): Int {
        return getLineBreakpointCount() + getOtherBreakpointCount()
    }

    /**
     * Returns the number of files that have line breakpoints.
     */
    fun getFileCount(): Int {
        return lineBreakpoints.size
    }

    /**
     * Gets a readable form of breakpoint type name from its ID.
     * This converts IDs like "java.line" to more readable forms like "Java Line"
     */
    fun getReadableTypeName(typeId: String): String {
        return typeId.split(".").joinToString(" ") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    /**
     * Returns the non-line breakpoint data with readable type names.
     */
    fun getOtherBreakpointDisplayData(): Map<String, Int> {
        return otherBreakpoints.entries.associate { (typeId, counter) ->
            getReadableTypeName(typeId) to counter.get()
        }
    }
}