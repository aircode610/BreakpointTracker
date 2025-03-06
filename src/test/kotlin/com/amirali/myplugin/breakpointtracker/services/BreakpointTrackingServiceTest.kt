package com.amirali.myplugin.breakpointtracker.services

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import org.mockito.Mockito.*

class BreakpointTrackingServiceTest : BasePlatformTestCase() {

    private lateinit var service: BreakpointTrackingService

    override fun setUp() {
        super.setUp()
        service = BreakpointTrackingService(project)
    }

    fun testAddBreakpoint() {
        // Test adding breakpoints
        val mockBreakpoint = mock(XLineBreakpoint::class.java)
        `when`(mockBreakpoint.fileUrl).thenReturn("file:///test/TestFile.kt")
        `when`(mockBreakpoint.line).thenReturn(42)

        service.addBreakpoint(mockBreakpoint)
        assertEquals(1, service.getTotalBreakpoints())
    }

}