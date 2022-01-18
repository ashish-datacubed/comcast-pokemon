package com.ashish.comcastpokemon.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ashish.comcastpokemon.R
import com.ashish.comcastpokemon.databinding.FragmentPokemonDetailsBinding
import com.ashish.comcastpokemon.models.PokemonFilteredDetails
import com.ashish.comcastpokemon.utils.StringUtil
import com.ashish.comcastpokemon.viewmodels.AppViewModelFactory
import com.ashish.comcastpokemon.viewmodels.PokemonViewModel
import com.squareup.picasso.Picasso

class PokemonDetailsFragment : Fragment(R.layout.fragment_pokemon_details) {

    private var _binding: FragmentPokemonDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PokemonViewModel
    private var currentSelection: PokemonFilteredDetails? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPokemonDetailsBinding.bind(view)
        initViewModel()
        currentSelection = viewModel.currentSelection.value
        updateUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(
            requireActivity(),
            AppViewModelFactory(requireActivity().application)
        ).get(PokemonViewModel::class.java)
    }

    private fun updateUI() {
        currentSelection?.let {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = "${it.name} details"
            binding.name.text = it.name
            binding.speed.text = it.speed.toString()
            binding.attack.text = it.attack.toString()
            binding.defense.text = it.defense.toString()
            it.iconURL?.let { iconURL ->
                Picasso.get().load(iconURL).into(binding.image)
            }
            it.types?.let {
                binding.types.text = StringUtil.commaSeparatedStrings(it)
            }
        }
    }
}