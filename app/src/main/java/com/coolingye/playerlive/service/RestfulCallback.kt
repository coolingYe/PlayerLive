package com.coolingye.playerlive.service

import io.reactivex.observers.DisposableObserver

abstract class RestfulCallback<T : Any> : DisposableObserver<T>() {
    abstract fun onSuccess(t: T)
    abstract fun onFailure(e: Throwable?)
    override fun onNext(t: T) {
        onSuccess(t)
    }

    override fun onError(e: Throwable) {
        onFailure(e)
    }

    override fun onComplete() {}
}