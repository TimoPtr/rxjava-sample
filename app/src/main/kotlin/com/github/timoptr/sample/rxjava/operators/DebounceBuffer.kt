package com.github.timoptr.sample.rxjava.operators

import io.reactivex.Observable
import java.util.concurrent.TimeUnit


/**
 * @author Nibeaudeau Timothy <timothy.nibeaudeau@gmail.com>
 * @version 0.1
 * on 17/11/2018 for rxjava-sample with IntelliJ IDEA.
 */

/**
 * Kotlin extension on Observable, base on some combination of [io.reactivex.Observable] operators
 * This combination of RX operators allow you to debounce the upstream with a given timeout and emit after another given timeout if no value send.
 *
 * @param debounceTimeout the timeout of the debounce (in ms)
 * @param timeout the global timeout use to avoid deadlock (in ms)
 * @return a new Observable which emit of list of item
 */
fun <T> Observable<T>.debounceBuffer(debounceTimeout: Long, timeout: Long): Observable<List<T>> =
    publish { source ->
        // We use publish to be able to use the source twice for buffer and debounce
        source.buffer(source
                .map { true }
                .debounce(debounceTimeout, TimeUnit.MILLISECONDS) // We use a second publish to avoid race condition when an error is emit by the downstream
                .timeout(timeout, TimeUnit.MILLISECONDS, Observable.just(false)).repeat())
    }.filter(List<T>::isNotEmpty) // filter empty list trigger by the timeout when no data received
