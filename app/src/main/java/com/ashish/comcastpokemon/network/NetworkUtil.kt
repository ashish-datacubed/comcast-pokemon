package com.ashish.comcastpokemon.network

import com.ashish.comcastpokemon.models.PokemonDetailsItem
import com.ashish.comcastpokemon.models.PokemonSearchData
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val POKEMON_BASE_URL = "https://pokeapi.co/api/v2/"
object NetworkUtil {

    private fun getRetrofitInstance(baseURL: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    suspend fun getPokemonList(url: String): Response<PokemonSearchData> {
        return getRetrofitInstance(POKEMON_BASE_URL).create(PokemonApi::class.java).getPokemonList(url)
    }

    suspend fun getPokemonDetails(id: String): Response<PokemonDetailsItem> {
        return getRetrofitInstance(POKEMON_BASE_URL).create(PokemonApi::class.java).getPokemonDetails(id)
    }
}