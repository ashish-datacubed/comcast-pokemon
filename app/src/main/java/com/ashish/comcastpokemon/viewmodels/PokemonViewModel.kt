package com.ashish.comcastpokemon.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ashish.comcastpokemon.R
import com.ashish.comcastpokemon.Resource
import com.ashish.comcastpokemon.SingleLiveEvent
import com.ashish.comcastpokemon.application.PokemonApplication
import com.ashish.comcastpokemon.models.PokemonDetailsItem
import com.ashish.comcastpokemon.models.PokemonFilteredDetails
import com.ashish.comcastpokemon.models.PokemonSearchData
import com.ashish.comcastpokemon.network.NetworkUtil
import com.ashish.comcastpokemon.network.POKEMON_BASE_URL
import com.ashish.comcastpokemon.ui.COMMON_TAG
import com.ashish.comcastpokemon.ui.NO_MORE_RESULTS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PokemonViewModel(app: Application) : AndroidViewModel(app) {

    val pokemonListResultEvent = SingleLiveEvent<Resource<Int>>()
    val pokemonDetailsResultEvent = SingleLiveEvent<Resource<String>>()
    private var _loadMoreURL = MutableLiveData<String?>(null)
    val loadMoreURL get() = _loadMoreURL

    var currentSelection = MutableLiveData<PokemonFilteredDetails?>()

    var pokemonFullList: MutableList<PokemonFilteredDetails> = mutableListOf()

    //Get list of Pokemon
    fun getPokemonList() {
        pokemonListResultEvent.postValue(Resource.loading())
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val url = if(loadMoreURL.value == null) "pokemon" else {
                    val tempURL = loadMoreURL.value
                    tempURL?.substring(POKEMON_BASE_URL.length) ?: "pokemon"
                }
                //get next list of Pokemon
                if (hasInternetConnection()) {
                    val response = NetworkUtil.getPokemonList(url)
                    if (response.isSuccessful) {
                        response.body()?.let {
                            withContext(Dispatchers.Main) {
                                _loadMoreURL.value = it.next ?: NO_MORE_RESULTS
                                updatePokemonLoadResults(it)
                                pokemonListResultEvent.value =
                                    Resource.success(it.results.size, response.code())
                            }
                        }
                    } else {
                        pokemonListResultEvent.postValue(
                            Resource.error(
                                getApplication<PokemonApplication>().resources.getString(R.string.generic_error_message),
                                response.code()
                            )
                        )
                    }
                }
                // No network
                else {
                    pokemonListResultEvent.postValue(
                        Resource.error(
                            getApplication<PokemonApplication>().resources.getString(R.string.no_internet_error_message),
                            null
                        )
                    )
                }
            }
        }
    }

    //Get additional details for a Pokemon
    fun getPokemonDetails(id: String) {
        viewModelScope.launch {
            pokemonDetailsResultEvent.value = Resource.loading(id)
            withContext(Dispatchers.IO) {
                if (hasInternetConnection()) {
                    val response = NetworkUtil.getPokemonDetails(id)
                    if (response.isSuccessful) {
                        response.body()?.let { pokemonDetailsItem ->
                            withContext(Dispatchers.Main) {
                                updatePokemonDetails(pokemonDetailsItem)
                                pokemonDetailsResultEvent.value =
                                    Resource.success(id, response.code())
                            }
                        }
                    } else {
                        pokemonDetailsResultEvent.postValue(
                            Resource.error(
                                getApplication<PokemonApplication>().resources.getString(R.string.generic_error_message),
                                response.code()
                            )
                        )
                    }
                } else {
                    pokemonDetailsResultEvent.postValue(
                        Resource.error(
                            getApplication<PokemonApplication>().resources.getString(R.string.no_internet_error_message),
                            null
                        )
                    )
                }
            }
        }
    }

    fun clearData() {
        currentSelection.value = null
        _loadMoreURL.value = null
        pokemonFullList.clear()
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager: ConnectivityManager = getApplication<PokemonApplication>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun extractID(detailsURL: String?): String {
        if (detailsURL == null) return "-1"
        val sub = detailsURL.substring(0, detailsURL.length - 1)
        return sub.substring(sub.lastIndexOf("/") + 1)
    }

    private fun updatePokemonLoadResults(searchData: PokemonSearchData) {
        searchData.results.forEach { pokemonItem ->
            val id = extractID(pokemonItem.detailsURL)
            val item = PokemonFilteredDetails(
                name = pokemonItem.name,
                id = id,
                detailsURL = pokemonItem.detailsURL
            )
            pokemonFullList.add(item)
        }
    }

    private fun updatePokemonDetails(pokemonDetailsItem: PokemonDetailsItem) {
        val index =
            pokemonFullList.indexOfFirst { it.id == pokemonDetailsItem.id }
        if (index != -1) {
            pokemonFullList[index].apply {
                //pokemon icon url
                iconURL = pokemonDetailsItem.sprites.back_default

                //pokemon types list
                val typesList = ArrayList<String>()
                pokemonDetailsItem.types.forEach { type ->
                    typesList.add(type.type.name)
                }
                types = typesList

                val stats = pokemonDetailsItem.stats
                //pokemon attack
                val attack = stats.firstOrNull { it.stat.name == "attack" }
                val attackValue = attack?.base_stat
                //pokemon speed
                val speed = stats.firstOrNull { it.stat.name == "speed" }
                val speedValue = speed?.base_stat
                //pokemon defense
                val defense = stats.firstOrNull { it.stat.name == "attack" }
                val defenseValue = defense?.base_stat

                this.speed = speedValue
                this.attack = attackValue
                this.defense = defenseValue
            }
        }
    }
}