package com.github.ezauton.conversion

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.reflect.KClass

interface LinearUnit
interface AngularUnit

interface TimeDerivative<T> {

  class Default<T : SIUnit<T>>(private val currentValue: Double, private val newClass: KClass<T>) : TimeDerivative<T> {
    override fun times(time: Time) = SIUnit.of(currentValue * time.value, newClass)
  }

  operator fun times(time: Time): T
}

operator fun <T> Time.times(timeDerivative: TimeDerivative<T>): T {
  return timeDerivative * this
}

interface DistanceDerivative<T> {

  class Default<T : SIUnit<T>>(private val currentValue: Double, private val newClass: KClass<T>) : DistanceDerivative<T> {
    override fun times(distance: Distance) = SIUnit.of(currentValue * distance.value, newClass)
  }

  operator fun times(distance: Distance): T
}


interface TimeIntegral<T> {

  class Default<T : SIUnit<T>>(private val currentValue: Double, private val newClass: KClass<T>) : TimeIntegral<T> {
    override fun div(time: Time) = SIUnit.of(currentValue / time.value, newClass)
  }

  operator fun div(time: Time): T
}

interface DistanceIntegral<T> {

  class Default<T : SIUnit<T>>(private val currentValue: Double, private val newClass: KClass<T>) : DistanceIntegral<T> {
    override fun div(distance: Distance) = SIUnit.of(currentValue / distance.value, newClass)
  }

  operator fun div(distance: Distance): T
}

@Serializable
class Distance(override val value: Double) :
  SIUnit<Distance>(),
  LinearUnit,
  TimeIntegral<LinearVelocity> by TimeIntegral.Default(value, LinearVelocity::class)

@Serializable
class Angle(override val value: Double) :
  SIUnit<Angle>(),
  AngularUnit,
  TimeIntegral<AngularVelocity> by TimeIntegral.Default(value, AngularVelocity::class),
  DistanceDerivative<Distance> by DistanceDerivative.Default(value, Distance::class) // TODO: is this right to call it a derivative?

@Serializable
class Time(override val value: Double) : SIUnit<Time>() {
  val millisL get() = millis.toLong()
  val millis get() = value * 1_000
  val seconds get() = value
  val minutes get() = seconds / 60
  val hours get() = minutes / 60
  val days get() = hours / 24
}

@Serializable
class LinearVelocity(override val value: Double) :
  SIUnit<LinearVelocity>(),
  LinearUnit,
  TimeDerivative<Distance> by TimeDerivative.Default(value, Distance::class),
  TimeIntegral<LinearAcceleration> by TimeIntegral.Default(value, LinearAcceleration::class)

@Serializable
class AngularVelocity(override val value: Double) :
  SIUnit<AngularVelocity>(),
  AngularUnit,
  TimeDerivative<Angle> by TimeDerivative.Default(value, Angle::class),
  DistanceDerivative<LinearVelocity> by DistanceDerivative.Default(value, LinearVelocity::class)

@Serializable
class AngularAcceleration(override val value: Double) :
  SIUnit<AngularVelocity>(),
  AngularUnit,
  TimeDerivative<AngularVelocity> by TimeDerivative.Default(value, AngularVelocity::class),
  DistanceDerivative<AngularVelocity> by DistanceDerivative.Default(value, AngularVelocity::class)

@Serializable
class LinearAcceleration(override val value: Double) :
  SIUnit<LinearAcceleration>(),
  LinearUnit,
  TimeDerivative<LinearVelocity> by TimeDerivative.Default(value, LinearVelocity::class)

@Serializable
class Scalar(override val value: Double) : SIUnit<Scalar>()

fun now() = Units.now() // kinda jank

object Units {

  fun now() = System.currentTimeMillis().millis

  @JvmStatic
  fun ft(value: Number) = meter(value.toDouble() / 3.28084)

  @JvmStatic
  fun rad(value: Number) = Angle(value.toDouble())

  @JvmStatic
  fun deg(value: Number) = rad(value.toDouble() * Math.PI / 180)

  @JvmStatic
  fun meter(value: Number) = Distance(value.toDouble())

  @JvmStatic
  fun mps(value: Number) = LinearVelocity(value.toDouble())

  @JvmStatic
  fun sec(value: Number) = Time(value.toDouble())

  @JvmStatic
  fun ms(value: Number) = Time(value.toDouble() / 1_000)

}

val Number.radians get() = Units.rad(this)
val Number.degrees get() = Units.deg(this)

val Number.mps get() = Units.mps(this)
val Number.mpss get() = Units.mps(this) / sec
val Number.millis get() = Units.ms(this)
val Number.ms get() = Units.ms(this)
val Number.meters get() = Units.meter(this)
val Number.m get() = Units.meter(this)
val Number.ft get() = Units.ft(this)
val Number.seconds get() = Units.sec(this)

val sec = 1.0.seconds

fun <T : SIUnit<T>> min(a: ConcreteVector<T>, b: ConcreteVector<T>) = if (a.scalarVector.mag2() > b.scalarVector.mag2()) b else a
fun <T : SIUnit<T>> max(a: ConcreteVector<T>, b: ConcreteVector<T>) = if (a.scalarVector.mag2() < b.scalarVector.mag2()) b else a

fun <T : SIUnit<T>> min(a: T, b: T) = if (a > b) b else a
fun <T : SIUnit<T>> max(a: T, b: T) = if (a < b) b else a

val Angle.radians get() = value
val Angle.degrees get() = value * 180 / (PI)

operator fun <T : SIUnit<T>> Number.times(unit: SIUnit<T>) = unit.times(this)
operator fun <T : SIUnit<T>> Number.times(cv: ConcreteVector<T>) = cv.times(this)
