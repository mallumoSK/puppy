package tk.mallumo.puppy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.serialization.Serializable

sealed class DataState<T>(val entry: T?) {
    class Idle<T>(entry: T? = null) : DataState<T>(entry)
    class Loading<T>(entry: T? = null) : DataState<T>(entry)
    class Result<T>(entry: T) : DataState<T>(entry)
}

class PuppySimple(
    val id: Int = 0,
    val img: String = "",
    val name: String = ""
)

@Serializable
class Puppy(
    val id: Int = 0,
    val img: String = "",
    val name: String = "",

    val info: String = "",
    val history: String = "",
    val temperament: String = "",
    val upkeep: String = "",

    val level_energy: Int = 0,
    val level_excercise: Int = 0,
    val level_playfulness: Int = 0,
    val level_affection: Int = 0,
    val level_favor_dogs: Int = 0,
    val level_favor_pets: Int = 0,
    val level_favor_stranger: Int = 0,
    val level_watchfulness: Int = 0,
    val level_train: Int = 0,
    val level_grooming: Int = 0,
    val level_heat: Int = 0,
    val level_vocality: Int = 0
)

class PuppyLevel(
    value: Int,
    val nameRes: Int,
    val percentage: Float = (value.toFloat() / 5F),
    val percentageText: Int = (percentage * 100F).toInt()
)

@Composable
fun Puppy.buildLevels(): List<List<PuppyLevel>> = remember {
    listOf(
        PuppyLevel(level_energy, R.string.energy),
        PuppyLevel(level_excercise, R.string.exercise_requirements),
        PuppyLevel(level_playfulness, R.string.playfulness),
        PuppyLevel(level_affection, R.string.affection_level),
        PuppyLevel(level_favor_dogs, R.string.friendliness_to_dogs),
        PuppyLevel(level_favor_pets, R.string.friendliness_to_other_pets),
        PuppyLevel(level_favor_stranger, R.string.friendliness_to_strangers),
        PuppyLevel(level_watchfulness, R.string.watchfulness),
        PuppyLevel(level_train, R.string.ease_of_training),
        PuppyLevel(level_grooming, R.string.grooming_requirements),
        PuppyLevel(level_heat, R.string.heat_sensitivity),
        PuppyLevel(level_vocality, R.string.vocality),
    ).chunked(3)
}

val Puppy.simple
    get() = PuppySimple(
        id = id,
        img = img,
        name = name
    )

