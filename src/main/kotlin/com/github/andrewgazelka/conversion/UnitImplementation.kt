package com.github.andrewgazelka.conversion

class Distance(override val value: Double) : SIUnit<Distance> {
    operator fun div(other: Time) = LinearVelocity(value / other.value)
}

class Angle(override val value: Double) : SIUnit<Angle> {
    operator fun div(other: Time) = Angle(value / other.value)
}

class Time(override val value: Double) : SIUnit<Time>

class LinearVelocity(override val value: Double) : SIUnit<LinearVelocity> {
    operator fun times(other: Time) = Distance(value * other.value)
}

class AngularVelocity(override val value: Double) : SIUnit<AngularVelocity> {
    operator fun times(other: Time) = Angle(value * other.value)
}

class Scalar(override val value: Double): SIUnit<Scalar>

object Units {

    @JvmStatic
    fun ft(value: Double) = meter(value / 3.28084)

    @JvmStatic
    fun rad(value: Double) = Angle(value / 3.28084)

    @JvmStatic
    fun deg(value: Double) = rad(value * Math.PI / 180)

    @JvmStatic
    fun meter(value: Double) = Distance(value)

    @JvmStatic
    fun mps(value: Double) = AngularVelocity(value)

    @JvmStatic
    fun sec(value: Double) = Time(value)

}
