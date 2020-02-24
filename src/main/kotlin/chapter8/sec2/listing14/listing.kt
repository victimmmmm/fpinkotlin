package chapter8.sec2.listing14

import arrow.core.getOrElse
import arrow.core.toOption
import chapter8.*

data class Prop(val run: (TestCases, RNG) -> Result)

data class Gen<A>(val sample: State<RNG, A>)

//tag::init[]
fun <A> forAll(ga: Gen<A>, f: (A) -> Boolean): Prop =
    Prop { n, rng ->
        randomSequence(ga, rng).mapIndexed { i, a -> // <1>
            try {
                if (f(a)) Passed else Falsified(a.toString(), i) // <2>
            } catch (e: Exception) {
                Falsified(buildMessage(a, e), i) // <3>
            }
        }.take(n)
            .find { it.isFalsified() }
            .toOption()
            .getOrElse { Passed }
    }

fun <A> randomSequence(ga: Gen<A>, rng: RNG): Sequence<A> = // <4>
    sequence {
        val (a: A, rng2: RNG) = ga.sample.run(rng)
        yield(a)
        yieldAll(randomSequence(ga, rng2))
    }

fun <A> buildMessage(a: A, e: Exception) = // <5>
    """
    |test case: $a
    |generated and exception: ${e.message}
    |stacktrace:
    |${e.stackTrace.joinToString("\n")}
""".trimMargin()
//end::init[]

fun main() {
    fun nextInt(rng: RNG): Pair<Int, RNG> {
        val (i1, rng2) = rng.nextInt()
        return Pair(i1, rng2)
    }

    fun integerGen(): Gen<Int> =
        Gen(State { rng -> nextInt(rng) })

    //success
    println(
        forAll(integerGen()) { it >= -2015756020 }
            .run(50, SimpleRNG(42))
    )

    //legit failure
    println(
        forAll(integerGen()) { it >= -2015756020 }
            .run(100, SimpleRNG(42))
    )

    //exceptional failure
    println(
        forAll(integerGen()) { throw Exception("BLAM") }
            .run(100, SimpleRNG(42))
    )
}