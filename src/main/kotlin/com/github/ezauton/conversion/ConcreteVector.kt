package com.github.ezauton.conversion

import kotlin.reflect.KClass

class ConcreteVector<T : SIUnit<T>>(val scalarVector: ScalarVector, val type: KClass<out T>) {

  companion object {
    val DEGENERATE = ScalarVector().toMeasureableVector() // degenerate (0-D) does not have a unit
    fun <T : SIUnit<T>> empty(kClass: KClass<T>) = vec().withUnit(kClass)
    fun <T : SIUnit<T>> of(vararg values: T): ConcreteVector<T> {
      require(values.isNotEmpty()) { "there must be at least one value. If there is not use empty()" }
      return ConcreteVector(
        scalarVector = ScalarVector(values.map { it.value }),
        type = values.first()::class
      )
    }
  }

  val dimension: Int get() = scalarVector.dimension

  val isFinite: Boolean get() = scalarVector.isFinite

  operator fun plus(other: ConcreteVector<T>): ConcreteVector<out T> = (scalarVector + other.scalarVector).withUnit(other.type)

  operator fun times(scalar: Number) = (scalarVector * scalar)

  operator fun get(i: Int) = SIUnit.of(scalarVector[i], type)

  fun dist(other: ConcreteVector<T>) =
    SIUnit.of(scalarVector.dist(other.scalarVector), type)

  fun dist2(other: ConcreteVector<T>) =
    SIUnit.of(scalarVector.dist2(other.scalarVector), type)

  fun mag2() = SIUnit.of(scalarVector.mag2(), type)

  fun mag() = SIUnit.of(scalarVector.mag(), type)
  fun sum() = SIUnit.of(scalarVector.sum(), type)
  operator fun minus(other: ConcreteVector<T>) = ConcreteVector(scalarVector - other.scalarVector, type)
}
