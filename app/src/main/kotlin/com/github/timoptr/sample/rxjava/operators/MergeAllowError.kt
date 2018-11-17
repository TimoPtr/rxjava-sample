package com.github.timoptr.sample.rxjava.operators

import io.reactivex.Observable
import io.reactivex.exceptions.CompositeException
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Nibeaudeau Timothy <timothy.nibeaudeau@gmail.com>
 * @version 0.1
 * on 17/11/2018 for rxjava-sample with IntelliJ IDEA.
 */


/**
 * This operator merge all the sources and doesn't failed unless all sources emit an error, in this case a [CompositeException]
 * is thrown with the list of error thrown by all the sources. If only one succeed none error will be propagate.
 *
 * @param <T> the common element base type
 * @param sources the Collection of Observable
 * @return an Observable that emits items that are the result of flattening the items emitted by the
 *         ObservableSources in the Iterable, if all the sources failed an error is thrown
 */
fun <T> mergeAllowError(sources: Collection<Observable<T>>): Observable<T> {
    val errorCounter = AtomicInteger()
    val errors = ArrayList<Throwable>(sources.size)
    val sourcesWithCatch  = sources.map {source ->
        source.onErrorResumeNext { error: Throwable ->
            errors.add(error)
            if (errorCounter.incrementAndGet() >= sources.size){
                Observable.error(CompositeException(errors))
            } else {
                Observable.empty()
            }
        }
    }
    return Observable.merge(sourcesWithCatch)
}