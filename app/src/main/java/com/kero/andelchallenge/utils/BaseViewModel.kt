package com.kero.andelchallenge.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

interface State
/**
 * this architecture was inspired from the mvrx framework
 */
open class BaseViewModel<S: State> (initialState:S ) : ViewModel() {

    private val state: MutableLiveData<S> by lazy {
        val liveData: MutableLiveData<S> = MutableLiveData()
        liveData.value = initialState
        liveData
    }

    private val job = Job()


    /**
     * scope to launch interactors
     * */
    protected val scope = CoroutineScope(Dispatchers.Main + job)

    /**
     * this method will change state in live date
     * */
    protected fun setState(stateChanger: S.() -> S) {
        state.value = state.value?.stateChanger()
    }


    /**
     * observe state in live date
     * */
    fun observe(lifecycleOwner: LifecycleOwner, observer: (S) -> Unit) {
        state.observe(lifecycleOwner, Observer(observer))
    }

    fun observeNotLifecycle(observer: (S) -> Unit) {
        state.observeForever(observer)
    }

    /**
     * this method will not change state
     * it's only used to handle current value of  state
     * */
    fun withState(stateHandler: (S) -> Unit) {
        stateHandler(state.value!!)
    }

    fun dispose() {
        if (job.isActive)
            job.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        dispose()
    }
}