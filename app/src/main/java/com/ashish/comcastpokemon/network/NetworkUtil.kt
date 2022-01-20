package com.ashish.comcastpokemon.network

import com.ashish.comcastpokemon.models.PokemonDetailsItem
import com.ashish.comcastpokemon.models.PokemonSearchData
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val POKEMON_BASE_URL = "https://pokeapi.co/api/v2/"
object NetworkUtil {
    private var instance: Retrofit? = null

    private fun getRetrofitInstance(): Retrofit {
        return instance ?: Retrofit.Builder()
                .baseUrl(POKEMON_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    suspend fun getPokemonList(url: String): Response<PokemonSearchData> {
        return getRetrofitInstance().create(PokemonApi::class.java).getPokemonList(url)
    }

    suspend fun getPokemonDetails(id: String): Response<PokemonDetailsItem> {
        return getRetrofitInstance().create(PokemonApi::class.java).getPokemonDetails(id)
    }
}