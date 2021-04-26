package com.github.ezauton.conversion

import kotlin.reflect.KClass

interface LinearUnit
interface AngularUnit

interface TimeDerivative<T> {

    class Default<T : SIUnit<T>>(private val currentValue: Double, private val newClass: KClass<T>) : TimeDerivative<T> {
        override fun times(time: Time) = SIUnit.of(currentValue * time.value, newClass)
    }

    operator fun times(time: Time): T
}

interface TimeIntegral<T> {

    class Default<T : SIUnit<T>>(private val currentValue: Double, private val newClass: KClass<T>) : TimeIntegral<T> {
        override fun div(time: Time) = SIUnit.of(currentValue / time.value, newClass)
    }

    operator fun div(time: Time): T
}

class Distance(override val value: Double) :
        SIUnit<Distance>,
        LinearUnit,
        TimeIntegral<LinearVelocity> by TimeIntegral.Default(value, LinearVelocity::class)

class Angle(override val value: Double) :
        SIUnit<Angle>,
        AngularUnit,
        TimeIntegral<AngularVelocity> by TimeIntegral.Default(value, AngularVelocity::class)

class Time(override val value: Double) : SIUnit<Time> {
    val millisL get() = millis.toLong()
    val millis get() = value * 1_000
    val seconds get() = value
    val minutes get() = seconds / 60
    val hours get() = minutes / 60
    val days get() = hours / 24
}

class LinearVelocity(override val value: Double) :
        SIUnit<LinearVelocity>,
        LinearUnit,
        TimeDerivative<Distance> by TimeDerivative.Default(value, Distance::class)

class AngularVelocity(override val value: Double) :
        SIUnit<AngularVelocity>,
        AngularUnit,
        TimeDerivative<Angle> by TimeDerivative.Default(value, Angle::class)

class AngularAcceleration(override val value: Double) :
        SIUnit<AngularVelocity>,
        AngularUnit,
        TimeDerivative<AngularVelocity> by TimeDerivative.Default(value, AngularVelocity::class)

class LinearAcceleration(override val value: Double) :
        SIUnit<LinearAcceleration>,
        LinearUnit,
        TimeDerivative<LinearVelocity> by TimeDerivative.Default(value, LinearVelocity::class)

class Scalar(override val value: Double) : SIUnit<Scalar>

fun now() = Units.now() // kinda jank

object Units {

    fun now() = System.currentTimeMillis().millis

    @JvmStatic
    fun ft(value: Number) = meter(value.toDouble() / 3.28084)

    @JvmStatic
    fun rad(value: Number) = Angle(value.toDouble() / 3.28084)

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

val Number.mps get() = Units.mps(this)
val Number.millis get() = Units.ms(this)
val Number.ms get() = Units.ms(this)
val Number.meters get() = Units.meter(this)
val Number.seconds get() = Units.sec(this)

fun <T : SIUnit<T>> cvec(type: KClass<out T>, vararg x: Double) = vec(*x).withUnit(type)

fun <T : SIUnit<T>> min(a: ConcreteVector<T>, b: ConcreteVector<T>) = if (a.scalarVector.mag2() > b.scalarVector.mag2()) b else a
fun <T : SIUnit<T>> max(a: ConcreteVector<T>, b: ConcreteVector<T>) = if (a.scalarVector.mag2() < b.scalarVector.mag2()) b else a

fun <T : SIUnit<T>> min(a: T, b: T) = if (a > b ) b else a
fun <T : SIUnit<T>> max(a: T, b: T) = if (a < b ) b else a

operator fun <T : SIUnit<T>> Number.times(unit: SIUnit<T>) = unit.times(this)
operator fun <T : SIUnit<T>> Number.times(cv: ConcreteVector<T>) = cv.times(this)
