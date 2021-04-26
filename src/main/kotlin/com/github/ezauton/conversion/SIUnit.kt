package com.github.ezauton.conversion

import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.reflect.KClass

interface SIUnit<T : SIUnit<T>> : Comparable<SIUnit<T>> {
  val value: Double

  companion object {
    fun <T : SIUnit<T>> of(double: Double, kClass: KClass<out SIUnit<T>>): T {
      return when (kClass) {
        Distance::class -> Distance(double)
        AngularVelocity::class -> AngularVelocity(double)
        Time::class -> Time(double)
        LinearVelocity::class -> LinearVelocity(double) // TODO: add all implementations
        else -> throw IllegalArgumentException("unit for $kClass has not yet been defined")
      } as T
    }
  }

  operator fun minus(other: T) = of(value - other.value, other::class)
  operator fun plus(other: T) = of(value + other.value, other::class)
  operator fun div(other: T) = value / other.value
  operator fun times(scalar: Number) = of(value * scalar.toDouble(), this::class)
  operator fun div(scalar: Number) = of(value / scalar.toDouble(), this::class)
  override fun compareTo(other: SIUnit<T>) = value.compareTo(other.value)
  operator fun rangeTo(other: T) = object : ClosedFloatingPointRange<T> {
    override val endInclusive: T get() = other
    override val start: T get() = this@SIUnit as T
    override fun lessThanOrEquals(a: T, b: T): Boolean = a.value <= b.value
  }

  fun abs() = of(value.absoluteValue, this::class)

  val isPositive get() = this.value > 0.001
  val isNegative get() = this.value < 0.001
  val isZero get() = abs(this.value) <= 0.001
  val isExactZero get() = this.value == 0.0
  val isNonZero get() = abs(this.value) > 0.001
  val isFinite get() = this.value.isFinite()
  val isInfinite get() = this.value.isInfinite()
  val isNaN get() = this.value.isNaN()

  operator fun unaryMinus() = this * (-1)
}

operator fun <T : SIUnit<T>> Number.times(unit: T): T = SIUnit.of(unit.value * toDouble(), unit::class)
operator fun <T : SIUnit<T>> Number.div(unit: T): T = SIUnit.of(unit.value / toDouble(), unit::class)

fun <T: SIUnit<T>> Number.withUnit(type: KClass<T>) = SIUnit.of(toDouble(), type)

inline fun <reified T: SIUnit<T>> zero() = SIUnit.of(0.0, T::class)
inline fun <reified T: SIUnit<T>> one() = SIUnit.of(1.0, T::class)
inline fun <reified T: SIUnit<T>> of(value: Double) = SIUnit.of(value, T::class)
