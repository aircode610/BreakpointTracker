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

class BreakpointBrowserPanel(private val project: Project) {
    // JCEF browser component
    private val jbCefBrowser = JBCefBrowser()

    // Main panel that will contain the browser
    private val panel = JPanel(BorderLayout())

    // Get reference to our breakpoint tracking service
    private val breakpointService = project.service<BreakpointTrackingService>()

    // JSON serializer for data communication
    private val gson = Gson()

    // Timer for periodically updating the display
    private val updateTimer: Timer

    // Initialize the panel
    init {
        // Add the browser to our panel
        panel.add(jbCefBrowser.component, BorderLayout.CENTER)

        // Load the initial HTML content
        loadHtmlContent()

        // Create a timer to update the content every second
        updateTimer = Timer(1000) { updateContent() }
        updateTimer.start()
    }

    // Getter for the Swing component
    val component: JComponent get() = panel

    // Load the initial HTML content
    private fun loadHtmlContent() {
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Breakpoint Tracker</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        padding: 20px;
                        background-color: ${getBackgroundColor()};
                        color: ${getTextColor()};
                    }
                    .stats {
                        display: flex;
                        justify-content: space-between;
                        margin-bottom: 20px;
                        background-color: ${getCardBackgroundColor()};
                        padding: 15px;
                        border-radius: 5px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .stat-block {
                        text-align: center;
                    }
                    .stat-value {
                        font-size: 24px;
                        font-weight: bold;
                        color: ${getAccentColor()};
                    }
                    .stat-label {
                        font-size: 12px;
                        color: ${getSecondaryTextColor()};
                    }
                    h2 {
                        margin-top: 30px;
                        margin-bottom: 15px;
                        color: ${getPrimaryTextColor()};
                    }
                    .file-card {
                        background-color: ${getCardBackgroundColor()};
                        border-radius: 5px;
                        padding: 12px 15px;
                        margin-bottom: 10px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .file-name {
                        font-weight: bold;
                        margin-bottom: 5px;
                    }
                    .line-list {
                        font-family: monospace;
                        color: ${getSecondaryTextColor()};
                    }
                    .other-type-card {
                        display: flex;
                        justify-content: space-between;
                        background-color: ${getCardBackgroundColor()};
                        border-radius: 5px;
                        padding: 12px 15px;
                        margin-bottom: 10px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .type-name {
                        font-weight: bold;
                    }
                    .type-count {
                        font-weight: bold;
                        color: ${getAccentColor()};
                    }
                </style>
            </head>
            <body>
                <h1>Breakpoint Tracker</h1>
                
                <div class="stats">
                    <div class="stat-block">
                        <div class="stat-value" id="total-count">0</div>
                        <div class="stat-label">Total Breakpoints</div>
                    </div>
                    <div class="stat-block">
                        <div class="stat-value" id="line-count">0</div>
                        <div class="stat-label">Line Breakpoints</div>
                    </div>
                    <div class="stat-block">
                        <div class="stat-value" id="file-count">0</div>
                        <div class="stat-label">Files</div>
                    </div>
                    <div class="stat-block">
                        <div class="stat-value" id="other-count">0</div>
                        <div class="stat-label">Other Breakpoints</div>
                    </div>
                </div>
                
                <h2>Line Breakpoints</h2>
                <div id="line-breakpoints-container">
                    <!-- Line breakpoints will be added here dynamically -->
                </div>
                
                <h2>Other Breakpoints</h2>
                <div id="other-breakpoints-container">
                    <!-- Other breakpoints will be added here dynamically -->
                </div>
                
                <script>
                    // Function to update the UI with new breakpoint data
                    function updateBreakpoints(data) {
                        // Update statistics
                        document.getElementById('total-count').textContent = data.totalCount;
                        document.getElementById('line-count').textContent = data.lineCount;
                        document.getElementById('file-count').textContent = data.fileCount;
                        document.getElementById('other-count').textContent = data.otherCount;
                        
                        // Update line breakpoints
                        const lineContainer = document.getElementById('line-breakpoints-container');
                        lineContainer.innerHTML = '';
                        
                        if (Object.keys(data.lineBreakpoints).length === 0) {
                            lineContainer.innerHTML = '<p>No line breakpoints found.</p>';
                        } else {
                            for (const [file, lines] of Object.entries(data.lineBreakpoints)) {
                                const fileCard = document.createElement('div');
                                fileCard.className = 'file-card';
                                
                                const fileName = document.createElement('div');
                                fileName.className = 'file-name';
                                fileName.textContent = file;
                                
                                const lineList = document.createElement('div');
                                lineList.className = 'line-list';
                                lineList.textContent = 'Lines: ' + lines.join(', ');
                                
                                fileCard.appendChild(fileName);
                                fileCard.appendChild(lineList);
                                lineContainer.appendChild(fileCard);
                            }
                        }
                        
                        // Update other breakpoints
                        const otherContainer = document.getElementById('other-breakpoints-container');
                        otherContainer.innerHTML = '';
                        
                        if (Object.keys(data.otherBreakpoints).length === 0) {
                            otherContainer.innerHTML = '<p>No other breakpoints found.</p>';
                        } else {
                            for (const [type, count] of Object.entries(data.otherBreakpoints)) {
                                const typeCard = document.createElement('div');
                                typeCard.className = 'other-type-card';
                                
                                const typeName = document.createElement('div');
                                typeName.className = 'type-name';
                                typeName.textContent = type;
                                
                                const typeCount = document.createElement('div');
                                typeCount.className = 'type-count';
                                typeCount.textContent = count;
                                
                                typeCard.appendChild(typeName);
                                typeCard.appendChild(typeCount);
                                otherContainer.appendChild(typeCard);
                            }
                        }
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        jbCefBrowser.loadHTML(htmlContent)
    }

    // Update the content with the latest breakpoint data
    private fun updateContent() {
        // Collect all data needed for display
        val data = mapOf(
            "totalCount" to breakpointService.getTotalBreakpoints(),
            "lineCount" to breakpointService.getLineBreakpointCount(),
            "fileCount" to breakpointService.getFileCount(),
            "otherCount" to breakpointService.getOtherBreakpointCount(),
            "lineBreakpoints" to breakpointService.getLineBreakpointDisplayData(),
            "otherBreakpoints" to breakpointService.getOtherBreakpointDisplayData()
        )

        // Convert data to JSON and send to browser
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