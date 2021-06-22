package com.cdi.myapplication

import org.junit.Assert.*
import org.junit.Test

class MyDataTest {
    @Test
    fun testMyData() {
        val testString = "test"
        val startInt = 1
        val mutatedInt = 2

        val sut = MyData(
            someData = testString,
            mutableInt = startInt
        )

        assertEquals(testString, sut.someData)
        assertEquals(startInt, sut.mutableInt)
        sut.mutableInt = mutatedInt
        assertEquals(mutatedInt, sut.mutableInt)
    }
}