package com.github.ezauton.conversion

import kotlin.reflect.KClass

class ConcreteVector<T : SIUnit<T>>(val scalarVector: ScalarVector, val type: KClass<out T>) {

    companion object {
        val DEGENERATE = ScalarVector().toMeasureableVector() // degenerate (0-D) does not have a unit
        fun <T : SIUnit<T>> empty(kClass: KClass<T>) = vec().withUnit(kClass)
    }

    operator fun plus(other: ConcreteVector<T>): ConcreteVector<out T> = (scalarVector + other.scalarVector).withUnit(other.type)
    operator fun get(i: Int) = SIUnit.of(scalarVector[i], type)
    fun dist(other: ConcreteVector<T>) =
        SIUnit.of(scalarVector.dist(other.scalarVector), type)
    fun mag() = SIUnit.of(scalarVector.mag(), type)
    fun sum() = SIUnit.of(scalarVector.sum(), type)
}

fun <T: SIUnit<T>> cvec(type: KClass<out T>, vararg x: Double) = vec(
    *x
).withUnit(type)

fun <T: SIUnit<T>> min(a: ConcreteVector<T>, b: ConcreteVector<T>) = if( a.scalarVector.mag2() > b.scalarVector.mag2()) b else a

fun <T: SIUnit<T>> max(a: ConcreteVector<T>, b: ConcreteVector<T>) = if( a.scalarVector.mag2() < b.scalarVector.mag2()) b else a

