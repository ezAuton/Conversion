package com.github.ezauton.conversion

import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.reflect.KClass

val classList = listOf(
  Distance::class,
  Time::class,
  LinearVelocity::class,
  LinearAcceleration::class,
  AngularVelocity::class,
  AngularAcceleration::class,
  Angle::class,
  Scalar::class,
)

abstract class SIUnit<T : SIUnit<T>> : Comparable<SIUnit<T>> {
  abstract val value: Double

  companion object {

    @Suppress("UNCHECKED_CAST")
    fun <T : SIUnit<T>> of(double: Double, kClass: KClass<out SIUnit<T>>): T {
      return when (kClass) {
        Distance::class -> Distance(double)
        Time::class -> Time(double)
        LinearVelocity::class -> LinearVelocity(double) // TODO: add all implementations
        LinearAcceleration::class -> LinearAcceleration(double)
        AngularVelocity::class -> AngularVelocity(double) // TODO: add all implementations
        AngularAcceleration::class -> AngularAcceleration(double)
        Angle::class -> Angle(double)
        Scalar::class -> Scalar(double)
        else -> throw IllegalArgumentException("unit for $kClass has not yet been defined")
      } as T
    }
  }

  override fun toString(): String {
    val typeStr = this.javaClass.simpleName
    val valueStr = "%.2f".format(value)
    return "${typeStr}($valueStr)"
  }


  operator fun minus(other: T) = of(value - other.value, other::class)
  operator fun plus(other: T) = of(value + other.value, other::class)
  operator fun div(other: T) = value / other.value
  operator fun times(scalar: Number) = of(value * scalar.toDouble(), this::class)
  operator fun div(scalar: Number) = of(value / scalar.toDouble(), this::class)
  override fun compareTo(other: SIUnit<T>) = value.compareTo(other.value)
  operator fun rangeTo(other: T) = object : ClosedFloatingPointRange<T> {
    override val endInclusive: T get() = other

    @Suppress("UNCHECKED_CAST")
    override val start: T
      get() = this@SIUnit as T

    override fun lessThanOrEquals(a: T, b: T): Boolean = a.value <= b.value
  }

  fun abs() = of(value.absoluteValue, this::class)

  val isPositive get() = this.value > 0.00
  val isNegative get() = this.value < 0.00
  val isApproxZero get() = abs(this.value) <= 0.001
  val isZero get() = this.value == 0.0
  val isNonZero get() = this.value != 0.00
  val isFinite get() = this.value.isFinite()
  val isInfinite get() = this.value.isInfinite()
  val isNaN get() = this.value.isNaN()
  val isInvalid get() = this.isNaN

  operator fun unaryMinus() = this * (-1)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as SIUnit<*>

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value.hashCode()
  }
}

operator fun <T : SIUnit<T>> Number.times(unit: T): T = SIUnit.of(unit.value * toDouble(), unit::class)
operator fun <T : SIUnit<T>> Number.div(unit: T): T = SIUnit.of(unit.value / toDouble(), unit::class)

fun <T : SIUnit<T>> Number.withUnit(type: KClass<T>) = SIUnit.of(toDouble(), type)
inline fun <reified T : SIUnit<T>> Number.withUnit() = SIUnit.of(toDouble(), T::class)

inline fun <reified T : SIUnit<T>> zero() = SIUnit.of(0.0, T::class)
inline fun <reified T : SIUnit<T>> invalid() = SIUnit.of(Double.NaN, T::class)
inline fun <reified T : SIUnit<T>> SI(value: Double) = SIUnit.of(value, T::class)
val <T : SIUnit<T>> T.s get() = value
