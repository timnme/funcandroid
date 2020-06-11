package com.example.reactiveandroid.presentation

import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.example.reactiveandroid.startOnMain
import com.example.reactiveandroid.toMain
import com.example.reactiveandroid.unitSingle
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable

abstract class ViewState<VS : ViewState<VS>> : Parcelable {
    abstract fun eventsResetState(): VS
}

abstract class BaseActivity<VS : ViewState<VS>> : AppCompatActivity() {
    companion object {
        private const val SAVED_STATE = "SAVED_STATE"
    }

    protected abstract val layoutId: Int

    protected abstract var currentViewState: VS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutId)

        setup()

        inputs()

        savedInstanceState?.getParcelable<VS>(SAVED_STATE)?.render()
            ?: unitSingle().onAbsentSavedViewState().renderSubscribe()
    }

    protected abstract fun setup()

    protected abstract fun inputs()

    protected abstract fun VS.render()

    protected abstract fun Single<Unit>.onAbsentSavedViewState(): Single<VS>

    protected fun Single<VS>.renderSubscribe(): Disposable = this
        .startOnMain().toMain().subscribe(::onNewViewState)

    protected fun Observable<VS>.renderSubscribe(): Disposable = this
        .startOnMain().toMain().subscribe(::onNewViewState)

    private fun onNewViewState(viewState: VS) {
        currentViewState = viewState.eventsResetState()
        viewState.render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SAVED_STATE, currentViewState)
    }
}