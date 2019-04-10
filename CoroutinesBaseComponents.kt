@file:Suppress("EXPERIMENTAL_FEATURE_WARNING", "EXPERIMENTAL_API_USAGE")

package com.thewondercreations.wonder.utils

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Kirill Chuprov on 4/9/19.
 */

abstract class BaseCoroutinesViewModel<State : Any>(initialState: State) : ViewModel() {

  private val liveData = MutableLiveData<State>().apply { value = initialState }
  private val currentState get() = liveData.value!!

  fun observe(owner: LifecycleOwner, observer: (State) -> Unit) =
    liveData.observe(owner, Observer { observer(it!!) })

  @MainThread
  fun dispatchState(state: State) {
    liveData.value = state
  }

  protected fun dispatchEvents(channel: ReceiveChannel<Reduce<State>>) {
    viewModelScope.launch {
      channel.consumeEach { action ->
        withContext(Dispatchers.Main) {
          dispatchState(action(currentState))
        }
      }
    }
  }
}

inline class Reduce<T>(private val createNewState: T.() -> T) {
  operator fun invoke(t: T) = t.createNewState()
}

fun <T> produceEvents(f: suspend ProducerScope<Reduce<T>>.() -> Unit): ReceiveChannel<Reduce<T>> =
  GlobalScope.produce(block = f, capacity = Channel.CONFLATED)

suspend fun <T> ProducerScope<Reduce<T>>.sendStateChanges(f: T.() -> T) = send(Reduce(f))