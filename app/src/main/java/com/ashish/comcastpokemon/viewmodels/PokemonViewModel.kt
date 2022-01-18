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

    private var _pokemonListResult = MutableLiveData<Resource<PokemonSearchData>>()
    val pokemonListResult get() = _pokemonListResult
    private var _pokemonDetailsResult = MutableLiveData<Resource<PokemonDetailsItem>>()
    val pokemonDetailsResult get() = _pokemonDetailsResult

    private var _loadMoreURL = MutableLiveData<String?>(null)
    val loadMoreURL get() = _loadMoreURL

    var currentSelection = MutableLiveData<PokemonFilteredDetails?>()

    //Get list of Pokemon
    fun getPokemonList() {
        _pokemonListResult.postValue(Resource.loading())
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var url = ""
                if (loadMoreURL.value == null) {
                    url = "pokemon"
                } else {
                    val tempURL = loadMoreURL.value
                    url = tempURL?.substring(POKEMON_BASE_URL.length) ?: "pokemon"
                }
                //get next list of Pokemon
                if (hasInternetConnection()) {
                    val response = NetworkUtil.getPokemonList(url)
                    if (response.isSuccessful) {
                        Log.d(COMMON_TAG, "RESPONSE WAS SUCCESSFUL")
                        response.body()?.let {
                            Log.d(COMMON_TAG, "NEXT: ${it.next}")
                            withContext(Dispatchers.Main) {
                                if (it.next == null) {
                                    _loadMoreURL.value = NO_MORE_RESULTS
                                } else {
                                    _loadMoreURL.value = it.next
                                }
                                _pokemonListResult.value = Resource.success(it, response.code())
                            }

                        }
                    } else {
                        _pokemonListResult.postValue(
                            Resource.error(
                                getApplication<PokemonApplication>().resources.getString(R.string.generic_error_message),
                                response.code()
                            )
                        )
                    }
                }
                // No network
                else {
                    _pokemonListResult.postValue(
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
            withContext(Dispatchers.IO) {
                if (hasInternetConnection()) {
                    val response = NetworkUtil.getPokemonDetails(id)
                    if (response.isSuccessful) {
                        response.body()?.let { pokemonDetailsItem ->
                            withContext(Dispatchers.Main) {
                                _pokemonDetailsResult.value =
                                    Resource.success(pokemonDetailsItem, response.code())
                            }
                        }
                    } else {
                        _pokemonDetailsResult.postValue(
                            Resource.error(
                                getApplication<PokemonApplication>().resources.getString(R.string.generic_error_message),
                                response.code()
                            )
                        )
                    }
                } else {
                    _pokemonDetailsResult.postValue(
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
        _pokemonListResult.value = Resource.empty()
        _pokemonDetailsResult.value = Resource.empty()
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
}