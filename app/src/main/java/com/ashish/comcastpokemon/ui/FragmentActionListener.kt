package com.ashish.comcastpokemon.ui

import com.ashish.comcastpokemon.common.FragmentAction
import com.ashish.comcastpokemon.models.PokemonFilteredDetails

interface FragmentActionListener {

    abstract fun onAction(action: FragmentAction, pokemonDetails: PokemonFilteredDetails?)
}