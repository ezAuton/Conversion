package com.github.andrewgazelka.conversion

import org.junit.jupiter.api.Test

class KotlinTest {

    @Test
    fun test123(){
        val a = SIUnit.of(1.0, Meter::class)
        val b = SIUnit.of(2.0, Second::class)
    }
}