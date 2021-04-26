package com.github.ezauton.conversion

import java.io.Serializable
import java.util.*
import java.util.stream.DoubleStream
import kotlin.reflect.KClass

typealias Operator = (Double, Double) -> Double


operator fun Double.times(n: ScalarVector): ScalarVector {
  return n * this
}

/**
 * An n-dimensional, immutable vector.
 */
class ScalarVector : Serializable, Comparable<ScalarVector> {
  override fun compareTo(other: ScalarVector): Int {
    other.assertDimension(dimension)
    for (i in 0 until dimension) {
      val a = get(i)
      val b = other[i]
      if (a != b) return a.compareTo(b)
    }
    return 0
  }

  val elements: DoubleArray

  val dimension: Int get() = elements.size

  val isFinite: Boolean get() = elements.all { it.isFinite() }

  constructor(vararg x: Double) {
    this.elements = x
  }

  operator fun <T : SIUnit<T>> times(unit: T) =
    ConcreteVector(times(unit.value), unit::class)

  fun <T : SIUnit<T>> withUnit(kClass: KClass<out T>) =
    ConcreteVector(this, kClass)

  inline fun <reified T : SIUnit<T>> withUnit() =
    ConcreteVector(this, T::class)

  fun toMeasureableVector() = withUnit(Scalar::class)

  /**
   * Convert a list into an [ScalarVector]
   *
   * @param list
   */
  constructor(list: List<Double>) {
    elements = DoubleArray(list.size)
    for (i in list.indices) {
      elements[i] = list[i]
    }
  }

  /**
   * A 0-dimensional ScalarVector... how sad 😭
   */
  constructor() {
    elements = DoubleArray(0)
  }

  fun normalized(): ScalarVector {
    return div(mag())
  }

  /**
   * @param size
   * @throws IllegalArgumentException if size does not match
   */
  @Throws(IllegalArgumentException::class)
  fun assertDimension(size: Int) {
    if (dimension != size) {
      throw IllegalArgumentException("Wrong size vector")
    }
  }

  operator fun get(i: Int): Double {
    return elements[i]
  }

  operator fun plus(other: ScalarVector): ScalarVector {
    other.assertDimension(dimension)
    return applyOperator(other) { first, second -> first + second }
  }

  fun dot(other: ScalarVector): Double {
    other.assertDimension(dimension)
    return times(other).sum()
  }

  fun dist(other: ScalarVector): Double {
    other.assertDimension(dimension)
    val sub = this - other
    return sub.mag()
  }

  fun dist2(other: ScalarVector): Double {
    other.assertDimension(dimension)
    val sub = this - other
    return sub.mag2()
  }

  /**
   * @return magnitude squared
   */
  fun mag2(): Double {
    return dot(this)
  }

  /**
   * @return magnitude
   */
  fun mag(): Double {
    return Math.sqrt(mag2())
  }

  fun sum(): Double {
    return elements.sum()
  }

  fun applyOperator(other: ScalarVector, operator: Operator): ScalarVector {
    val temp = DoubleArray(elements.size)
    for (i in elements.indices) {
      temp[i] = operator(elements[i], other.elements[i])
    }
    return ScalarVector(*temp)
  }

  operator fun minus(other: ScalarVector): ScalarVector {
    return applyOperator(other) { first, second -> first - second }
  }

  operator fun times(other: ScalarVector): ScalarVector {
    return applyOperator(other) { first, second -> first * second }
  }

  operator fun div(other: ScalarVector): ScalarVector {
    return applyOperator(other) { first, second -> first / second }
  }

  fun stream(): DoubleStream {
    return Arrays.stream(elements)
  }

  fun iterator(): DoubleIterator {
    return elements.iterator()
  }

  val x get() = get(0)
  val y get() = get(1)
  val z get() = get(2)

  /**
   * Remove instances of a number from a vector
   *
   * @param toTruncate The number to remove
   * @return A new vector that does not have instances of that number
   */
  fun truncateElement(toTruncate: Double): ScalarVector {
    val toReturn = ArrayList<Double>(dimension)
    for (element in elements) {
      if (toTruncate != element) {
        toReturn.add(element)
      }
    }
    return ScalarVector(toReturn)
  }

  operator fun times(scalar: Number): ScalarVector {
    val scalarAsVector = of(scalar.toDouble(), dimension)
    return scalarAsVector.times(this)
  }

  operator fun div(scalar: Number): ScalarVector {
    return times(1.0 / scalar.toDouble())
  }

  /**
   * @param other
   * @return If epsilon equals
   */
  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other == null || javaClass != other.javaClass) {
      return false
    }
    val that = other as ScalarVector?
    if (that!!.dimension != dimension) {
      return false
    }
    for (i in 0 until dimension) {
      if (Math.abs(that.elements[i] - elements[i]) > 1E-6)
      // epsilon eq
      {
        return false
      }
    }
    return true
  }

  override fun hashCode(): Int {
    return Arrays.hashCode(elements)
  }

  override fun toString(): String {
    return "ScalarVector{" +
        "elements=" + Arrays.toString(elements) +
        '}'.toString()
  }

  companion object {

    fun of(element: Double, size: Int): ScalarVector {
      val elements = DoubleArray(size)
      for (i in 0 until size) {
        elements[i] = element
      }
      return ScalarVector(*elements)
    }

    /**
     * throws error if not same dimension
     *
     * @param vectors
     */
    fun assertSameDim(vectors: Collection<ScalarVector>) {
      var initSize = -1
      for (vector in vectors) {
        if (initSize == -1) {
          initSize = vector.dimension
        } else {
          vector.assertDimension(initSize)
        }
      }
    }

    /**
     * @param size The dimension of the vector.
     * @return
     */
    fun origin(size: Int): ScalarVector {
      return of(0.0, size)
    }
  }

  operator fun rangeTo(other: ScalarVector): ClosedRange<ScalarVector> = object : ClosedRange<ScalarVector> {
    override val endInclusive: ScalarVector
      get() = this@ScalarVector
    override val start: ScalarVector
      get() = other
  }
}

fun scalarVec(vararg x: Double) = ScalarVector(*x)
fun svec(vararg x: Double) = ScalarVector(*x)
