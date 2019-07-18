package com.github.andrewgazelka.conversion

import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

interface Length<T: SIUnit<T>> : SIUnit<T>

interface SIUnit<T: SIUnit<T>> {
    val value: Double

    companion object {
        fun <T : SIUnit<T>> of(double: Double, kClass: KClass<out T>): T {
            return when(kClass){
                Distance::class -> Distance(double)
                AngularVelocity::class -> AngularVelocity(double)
                Time::class -> Time(double)
                else -> throw IllegalArgumentException("that unit has not yet been defined")
            } as T
        }
    }

    operator fun minus(other: T) = of(value - other.value, other::class)
    operator fun plus(other: T) = of(value + other.value, other::class)
    operator fun div(other: T) = Scalar(value / other.value)
}