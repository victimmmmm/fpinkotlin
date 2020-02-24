package chapter8.sec2.listing12

import chapter8.FailedCase
import chapter8.SuccessCount

//tag::init[]
sealed class Result {
    abstract fun isFalsified(): Boolean
}

object Passed : Result() {
    override fun isFalsified(): Boolean = false
}

data class Falsified(
    val failure: FailedCase,
    val successes: SuccessCount
) : Result() {
    override fun isFalsified(): Boolean = true
}
//end::init[]