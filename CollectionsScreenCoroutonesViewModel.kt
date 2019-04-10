package com.thewondercreations.wonder.ui.collections.collectionslistcoroutines

import com.thewondercreations.wonder.utils.BaseCoroutinesViewModel
import com.thewondercreations.wonder.utils.produceEvents
import com.thewondercreations.wonder.utils.sendStateChanges
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Random

/**
 * Created by Kirill Chuprov on 4/10/19.
 */
class CollectionsScreenCoroutonesViewModel :
  BaseCoroutinesViewModel<CollectionsScreenState>(CollectionsScreenState()) {

  fun loadData() {
    dispatchEvents(produceEvents {
      sendStateChanges { copy(loading = true) }
      try {
        //Do your async shit here and send state changes then
        val result = asyncMethodFromRepo().await()
        sendStateChanges { copy(loading = result) }
      } catch (e: Exception) {
        sendStateChanges { copy(error = e, loading = false) }
      }
    })

  }
}

/*EXAMPLE*/
suspend fun asyncMethodFromRepo(): Deferred<Boolean> = withContext(Dispatchers.IO) {
  async {
    val random = Random()
    delay(2000)
    random.nextBoolean()
  }
}