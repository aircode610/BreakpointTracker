package com.amirali.myplugin.breakpointtracker.ui

import com.amirali.myplugin.breakpointtracker.services.BreakpointTrackingService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.Timer
import com.google.gson.Gson
import com.intellij.ui.JBColor
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class BreakpointBrowserPanel(private val project: Project) {
    // JCEF browser component
    private val jbCefBrowser = JBCefBrowser()

    // Main panel
    private val panel = JPanel(BorderLayout())

    // Breakpoint tracking service
    private val breakpointService = project.service<BreakpointTrackingService>()

    // JSON serializer
    private val gson = Gson()

    private val updateTimer: Timer

    // Initialize the panel
    init {
        panel.add(jbCefBrowser.component, BorderLayout.CENTER)

        loadHtmlContent()

        updateTimer = Timer(1000) { updateContent() }
        updateTimer.start()
    }

    // Getter for the Swing component
    val component: JComponent get() = panel

    // Load the initial HTML content
    private fun loadHtmlContent() {
        val htmlTemplate = loadResourceAsString("/com/amirali/myplugin/breakpointtracker/ui/breakpoint_tracker.html")

        val htmlContent = htmlTemplate
            .replace("\${backgroundColor}", getBackgroundColor())
            .replace("\${textColor}", getTextColor())
            .replace("\${cardBackgroundColor}", getCardBackgroundColor())
            .replace("\${primaryTextColor}", getPrimaryTextColor())
            .replace("\${secondaryTextColor}", getSecondaryTextColor())
            .replace("\${accentColor}", getAccentColor())

        jbCefBrowser.loadHTML(htmlContent)
    }

    // Helper method to load a resource file as a string
    private fun loadResourceAsString(resourcePath: String): String {
        val inputStream = javaClass.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        return BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
            reader.lines().reduce { a, b -> "$a\n$b" }.orElse("")
        }
    }

    // Update the content with the latest breakpoint data
    private fun updateContent() {
        val data = mapOf(
            "totalCount" to breakpointService.getTotalBreakpoints(),
            "lineCount" to breakpointService.getLineBreakpointCount(),
            "fileCount" to breakpointService.getFileCount(),
            "otherCount" to breakpointService.getOtherBreakpointCount(),
            "lineBreakpoints" to breakpointService.getLineBreakpointDisplayData(),
            "otherBreakpoints" to breakpointService.getOtherBreakpointDisplayData()
        )

        val json = gson.toJson(data)
        val updateScript = "updateBreakpoints($json)"

        jbCefBrowser.cefBrowser.executeJavaScript(
            updateScript,
            jbCefBrowser.cefBrowser.url,
            0
        )
    }

    // Helper methods to get theme-appropriate colors
    private fun getBackgroundColor(): String {
        return if (!JBColor.isBright()) "#2b2b2b" else "#f5f5f5"
    }

    private fun getCardBackgroundColor(): String {
        return if (!JBColor.isBright()) "#3c3f41" else "#ffffff"
    }

    private fun getTextColor(): String {
        return if (!JBColor.isBright()) "#bbbbbb" else "#333333"
    }

    private fun getPrimaryTextColor(): String {
        return if (!JBColor.isBright()) "#cccccc" else "#2c2c2c"
    }

    private fun getSecondaryTextColor(): String {
        return if (!JBColor.isBright()) "#999999" else "#777777"
    }

    private fun getAccentColor(): String {
        return if (!JBColor.isBright()) "#589df6" else "#4078c0"
    }
}