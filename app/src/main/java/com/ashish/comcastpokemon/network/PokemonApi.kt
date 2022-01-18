package com.ashish.comcastpokemon.network

import com.ashish.comcastpokemon.models.PokemonDetailsItem
import com.ashish.comcastpokemon.models.PokemonSearchData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface PokemonApi {

    @GET
    suspend fun getPokemonList(@Url url: String): Response<PokemonSearchData>

    @GET("pokemon/{id}")
    suspend fun getPokemonDetails(@Path("id") id: String): Response<PokemonDetailsItem>
}