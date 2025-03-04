package com.amirali.myplugin.breakpointtracker.services

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
 * Service responsible for tracking all types of breakpoints across the project.
 * Maintains separate data structures for line breakpoints and other types.
 */
@Service(Service.Level.PROJECT)
class BreakpointTrackingService(private val project: Project) {
    private val LOG = Logger.getInstance(BreakpointTrackingService::class.java)

    // Map of file path to list of line numbers for line breakpoints
    private val lineBreakpoints = ConcurrentHashMap<String, MutableList<Int>>()

    // Map of breakpoint type to count for non-line breakpoints
    private val otherBreakpoints = ConcurrentHashMap<String, AtomicInteger>()

    init {
        LOG.info("Initializing BreakpointTrackingService")
        loadExistingBreakpoints()
    }

    /**
     * Loads all existing breakpoints from the project.
     * Called once during service initialization.
     */
    private fun loadExistingBreakpoints() {
        val breakpointManager = XDebuggerManager.getInstance(project).breakpointManager

        // Clear existing data to ensure a clean state
        lineBreakpoints.clear()
        otherBreakpoints.clear()

        // Get all breakpoints
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
                    // Remove the specific line
                    lines.remove(line)

                    // If no more breakpoints in this file, remove the file entry
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
            // Decrement the counter
            val count = counter.decrementAndGet()

            // If count reaches 0, remove the type entry
            if (count <= 0) {
                otherBreakpoints.remove(typeId)
            }

            LOG.debug("Removed $typeId breakpoint. Remaining: $count")
        }
    }

    /**
     * Updates an existing breakpoint in the tracking maps.
     * This is called when a breakpoint's properties change.
     *
     * @param breakpoint The breakpoint that was changed
     */
    fun updateBreakpoint(breakpoint: XBreakpoint<*>) {
        // For changes, we need to remove old entry and add new one
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

                // Get or create file entry
                val lines = lineBreakpoints.getOrPut(filePath) { ArrayList() }

                // Add line if it doesn't already exist
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

        // Get or create counter for this type and increment it
        val counter = otherBreakpoints.getOrPut(typeId) { AtomicInteger(0) }
        val newCount = counter.incrementAndGet()

        LOG.debug("Added $typeId breakpoint. Total: $newCount")
    }

    /**
     * Returns the line breakpoint data.
     *
     * @return Map of file paths to lists of line numbers
     */
    fun getLineBreakpointData(): Map<String, List<Int>> {
        // Return an immutable copy to prevent external modification
        return lineBreakpoints.mapValues { (_, lines) ->
            lines.toList().sorted()
        }
    }

    /**
     * Returns the line breakpoint data with shortened file paths for display.
     *
     * @return Map of shortened file paths to lists of line numbers
     */
    fun getLineBreakpointDisplayData(): Map<String, List<Int>> {
        return lineBreakpoints.entries.associate { (path, lines) ->
            // Extract just the filename for display purposes
            val filename = path.substringAfterLast('/')
            filename to lines.toList().sorted()
        }
    }

    /**
     * Returns the line breakpoint data with file path and convenient display format.
     *
     * @return Map of file paths to formatted line information
     */
    fun getLineBreakpointDetailedData(): Map<String, String> {
        return lineBreakpoints.entries.associate { (path, lines) ->
            val filename = path.substringAfterLast('/')
            val sortedLines = lines.sorted()
            val linesText = when {
                sortedLines.isEmpty() -> "No breakpoints"
                sortedLines.size == 1 -> "Line: ${sortedLines[0]}"
                else -> "Lines: ${sortedLines.joinToString(", ")}"
            }

            filename to linesText
        }
    }

    /**
     * Returns the non-line breakpoint type counts.
     *
     * @return Map of breakpoint type IDs to counts
     */
    fun getOtherBreakpointData(): Map<String, Int> {
        return otherBreakpoints.mapValues { (_, counter) -> counter.get() }
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
     * Returns the number of different non-line breakpoint types.
     */
    fun getOtherBreakpointTypeCount(): Int {
        return otherBreakpoints.size
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