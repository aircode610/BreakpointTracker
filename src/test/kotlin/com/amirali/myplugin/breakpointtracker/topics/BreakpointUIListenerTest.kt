package com.amirali.myplugin.breakpointtracker.topics

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.messages.MessageBusConnection
import org.mockito.Mockito.*

class BreakpointUIListenerTest : BasePlatformTestCase() {

    fun testMessageBusNotification() {
        // Test that subscribers get notified when breakpoints change
        val connection = project.messageBus.connect()
        val mockListener = mock(BreakpointUIListener::class.java)

        connection.subscribe(BreakpointUIListener.TOPIC, mockListener)

        // Publish a message
        project.messageBus.syncPublisher(BreakpointUIListener.TOPIC).breakpointsChanged()

        // Verify the listener was called
        verify(mockListener, times(1)).breakpointsChanged()
    }
}