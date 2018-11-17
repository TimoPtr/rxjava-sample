package com.github.timoptr.sample.rxjava.operators

import com.github.timoptr.sample.rxjava.operators.mergeAllowError
import io.reactivex.Observable
import io.reactivex.exceptions.CompositeException
import org.junit.Test

/**
 * @author Nibeaudeau Timothy <timothy.nibeaudeau@gmail.com>
 * @version 0.1
 * on 17/11/2018 for rxjava-sample with IntelliJ IDEA.
 */
class MergeAllowErrorTest {

    @Test
    fun sampleOneSourceFailure() {
        // Merge 3 sources with one which failed it should emit the values of the two other
        mergeAllowError(listOf(Observable.just(true),
                Observable.error(Exception()),
                Observable.just(false)))
                .doOnNext(::println)
                .test().await().assertNoErrors().assertValues(true, false)
    }

    @Test
    fun sampleAllSourceFailure() {
        // Merge three sources which emit an error, it should emit a CompositeException
        mergeAllowError<Any>(listOf(Observable.error(Exception()),
                Observable.error(Exception()),
                Observable.error(Exception())))
                .doOnError(::println)
                .test().assertError{error ->
                    error is CompositeException && error.size() == 3
                }
    }

    @Test
    fun sampleAllSourceFailedExceptOne() {
        val sources = (0 until 100).map { Observable.error<Boolean>(Exception()) }.toMutableList()
        sources.add(Observable.just(false))

        // Merge the 101 sources and it should emit only one value false
        mergeAllowError(sources)
                .doOnNext(::println)
                .doOnError(::println)
                .test().await().assertNoErrors().assertValue(false)
    }
}