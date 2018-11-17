package com.github.timoptr.sample.rxjava.operators

import io.reactivex.subjects.PublishSubject
import org.junit.Test

/**
 * @author Nibeaudeau Timothy <timothy.nibeaudeau@gmail.com>
 * @version 0.1
 * on 17/11/2018 for rxjava-sample with IntelliJ IDEA.
 */
class DebounceBufferTest {

    @Test
    fun sampleDebounceBufferString() {
        val source = PublishSubject.create<ArrayList<String>>()

        val testObs = source.debounceBuffer(1, 20)
                .doOnNext {
                    println("$it")
                    //reduce (flatten)
                    println(it.reduce { acc, list ->
                        acc.addAll(list)
                        acc
                    }.size)
                }.test()

        // Send two array without pause this will combine the two arrays
        source.onNext(arrayListOf("Hello", "World"))
        source.onNext(arrayListOf("Good", "bye"))

        // We sleep 2 ms which is greater than the timeout of the debounce so it will no concatenate the two arrays
        Thread.sleep(2)
        source.onNext(arrayListOf("Welcome", "back"))
        Thread.sleep(2)

        // In this case we want to illustrate the global timeout
        (0 until 10000).forEach { _ ->
            source.onNext(arrayListOf("End"))
        }
        source.onComplete()

        testObs.await()
        testObs.assertNoErrors()
    }
}