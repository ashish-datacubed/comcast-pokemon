package com.ashish.comcastpokemon.models

import com.google.gson.annotations.SerializedName

data class PokemonSearchData(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonBasicItem>
)

data class PokemonBasicItem(
    val name: String,
    @SerializedName("url")
    val detailsURL: String? = null
)

data class PokemonDetailsItem(
    val id: String,
    val sprites: Sprites,
    val abilities: List<PokemonAbility>,
    val stats: List<PokemonStat>,
    val types: List<Type>
)

data class Type(val type: TypeName)

data class TypeName(val name: String)

data class PokemonFilteredDetails(
    var name: String = "",
    var id: String = "-1",
    var detailsCallMade: Boolean = false,
    var detailsURL: String? = null,
    var iconURL: String? = null,
    var types: List<String>? = null,
    var abilities: List<String>? = null,
    var speed: Int? = null,
    var attack: Int? = null,
    var defense: Int? = null
)

data class PokemonStat(
    val base_stat: Int,
    val stat: Stat
)

data class Stat(
    val name: String
)

data class PokemonAbility(
    val ability: PokemonAbilityName
)

data class PokemonAbilityName(
    val abilityName: String
)

data class Sprites(
val back_default: String?,
val back_female: String?,
val back_shiny: String?,
val back_shiny_female: String?,
val front_default: String?,
val front_female: String?,
val front_shiny: String?,
val front_shiny_female: String?
)

