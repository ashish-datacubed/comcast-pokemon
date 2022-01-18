package com.ashish.comcastpokemon

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.ashish.comcastpokemon.common.FragmentAction
import com.ashish.comcastpokemon.databinding.ActivityMainBinding
import com.ashish.comcastpokemon.models.PokemonFilteredDetails
import com.ashish.comcastpokemon.ui.FragmentActionListener
import com.ashish.comcastpokemon.viewmodels.AppViewModelFactory
import com.ashish.comcastpokemon.viewmodels.PokemonViewModel

class MainActivity : AppCompatActivity(), FragmentActionListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: PokemonViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(application)
        ).get(PokemonViewModel::class.java)

        //Connect action bar to nav graph
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentNavHost) as NavHostFragment
        navController = navHostFragment.findNavController()
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController)
    }

    //Used by Fragments to pass any instruction/action to the host activity
    //The Fragment could have updated this too, but I do it through the Activity
    override fun onAction(action: FragmentAction, pokemonDetails: PokemonFilteredDetails?) {
        when (action) {
            FragmentAction.ACTION_BUSINESS_DETAILS_ITEM_CLICK -> {
                pokemonDetails?.let {
                    viewModel.currentSelection.value = it
                }
            }
            FragmentAction.ACTION_NAVIGATE_BACK -> {

            }
            FragmentAction.ACTION_CLEAR_VIEWMODEL_DATA -> {
                viewModel.clearData()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}