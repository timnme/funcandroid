package com.example.reactiveandroid

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> T.toObservable(): Observable<T> = Observable.just(this)
fun <T> T.toSingle(): Single<T> = Single.just(this)
fun unitSingle(): Single<Unit> = Single.just(Unit)

fun <T> Observable<T>.startOnMain(): Observable<T> = subscribeOn(AndroidSchedulers.mainThread())
fun <T> Single<T>.startOnMain(): Single<T> = subscribeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.toMain(): Observable<T> = observeOn(AndroidSchedulers.mainThread())
fun <T> Single<T>.toMain(): Single<T> = observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.toIo(): Observable<T> = observeOn(Schedulers.io())
fun <T> Single<T>.toIo(): Single<T> = observeOn(Schedulers.io())