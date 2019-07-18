package com.github.andrewgazelka.conversion

class Meter(override val value: Double) : SIUnit<Meter> {
    operator fun div(other: Second) = Velocity(value / other.value)
}
class Second(override val value: Double) : SIUnit<Second>
class Velocity(override val value: Double) : SIUnit<Velocity>
class Scalar(override val value: Double): SIUnit<Scalar>

object Units {

    @JvmStatic
    fun ft(value: Double) = meter(value / 3.28084)

    @JvmStatic
    fun meter(value: Double) = Meter(value)

    @JvmStatic
    fun sec(value: Double) = Second(value)

}
