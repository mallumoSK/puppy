package tk.mallumo.puppy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object Repository {

    suspend fun getAll() = load().map { it.simple }

    suspend fun get(id: Int) = load().first { it.id == id }

    private suspend fun load(): List<Puppy> = coroutineScope {
        withContext(Dispatchers.IO) {
            val json = app.resources.openRawResource(R.raw.puppy_source).bufferedReader().use { it.readText() }
            Json.decodeFromString(json)
        }
    }

    object Preview {
        fun items() = (0..20).map { index ->
            PuppySimple(
                id = index,
                img = "great_pyrenees",
                name = "$index Name of puppy",
            )
        }

    }
}