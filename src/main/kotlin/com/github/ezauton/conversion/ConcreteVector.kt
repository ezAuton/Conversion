package com.github.ezauton.conversion

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass


@Serializable
data class ConcreteVectorWrapper(val scalarVector: ScalarVector, val id: Int)

class ConcreteVectorSerializer<T : SIUnit<T>>(private val dataSerializer: KSerializer<T>): KSerializer<ConcreteVector<T>> {

  override fun deserialize(decoder: Decoder): ConcreteVector<T> {
    val wrapper = decoder.decodeSerializableValue(ConcreteVectorWrapper.serializer())
    val type = classList[wrapper.id]
    val scalarVector = wrapper.scalarVector
    return ConcreteVector<T>(scalarVector, type as Nothing) // TODO: really jank type stuff here
  }

  override val descriptor: SerialDescriptor = dataSerializer.descriptor


  override fun serialize(encoder: Encoder, value: ConcreteVector<T>) {
    val idx = classList.indexOfFirst { it == value.type }
    val wrapper = ConcreteVectorWrapper(value.scalarVector, idx)
    encoder.encodeSerializableValue(ConcreteVectorWrapper.serializer(), wrapper)
  }


}

@Serializable(with = ConcreteVectorSerializer::class)
class ConcreteVector<T : SIUnit<T>>(val scalarVector: ScalarVector, val type: KClass<out T>) {

  companion object {
    val DEGENERATE = ScalarVector().toMeasureableVector() // degenerate (0-D) does not have a unit
    fun <T : SIUnit<T>> empty(kClass: KClass<T>) = scalarVec().withUnit(kClass)
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

  val x get() = get(0)
  val y get() = get(1)
  val z get() = get(2)

  operator fun plus(other: ConcreteVector<T>): ConcreteVector<T> = (scalarVector + other.scalarVector).withUnit(other.type)

  operator fun div(scalar: Number) = times(1.0 / scalar.toDouble())

  operator fun times(scalar: Number) = (scalarVector * scalar).withUnit(type)

  fun normalized() = scalarVector.normalized().withUnit(type)

  operator fun get(i: Int) = SIUnit.of(scalarVector[i], type)

  fun dist(other: ConcreteVector<T>) =
    SIUnit.of(scalarVector.dist(other.scalarVector), type)

  fun dist2(other: ConcreteVector<T>) =
    SIUnit.of(scalarVector.dist2(other.scalarVector), type)

  fun mag2() = SIUnit.of(scalarVector.mag2(), type)

  fun mag() = SIUnit.of(scalarVector.mag(), type)
  fun sum() = SIUnit.of(scalarVector.sum(), type)


  operator fun minus(other: ConcreteVector<T>) = ConcreteVector(scalarVector - other.scalarVector, type)
  override fun toString(): String {
    val elements = scalarVector.elements.joinToString()
    val typeStr = type.simpleName!!
    return "${typeStr}[${elements}]"
  }

}

fun scalar(vararg vector: ConcreteVector<*>) = vector.map { it.scalarVector }

inline fun <reified T : SIUnit<T>> cvec(vararg elems: Double) = ConcreteVector(scalarVec(*elems), T::class)
inline fun <reified T : SIUnit<T>> vec(vararg elems: Number) = cvec<T>(*elems.map { it.toDouble() }.toDoubleArray())
inline fun <reified T : SIUnit<T>> vec(vararg elems: SIUnit<T>) = cvec<T>(*elems.map { it.value }.toDoubleArray())
inline fun <reified T : SIUnit<T>> vec() = cvec<T>()
inline fun <reified T : SIUnit<T>> origin(size: Int) = ConcreteVector(ScalarVector.origin(size), T::class)
