package com.github.ezauton.conversion

import org.junit.jupiter.api.Test

class KotlinTest {

    @Test
    fun test123(){
        val a = SIUnit.of(1.0, Distance::class)
        val b = SIUnit.of(2.0, Time::class)
    }
}