package com.ashish.comcastpokemon.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ashish.comcastpokemon.R
import com.ashish.comcastpokemon.Status
import com.ashish.comcastpokemon.adapters.PokemonSearchResultsAdapter
import com.ashish.comcastpokemon.common.FragmentAction
import com.ashish.comcastpokemon.databinding.FragmentPokemonListBinding
import com.ashish.comcastpokemon.models.PokemonFilteredDetails
import com.ashish.comcastpokemon.viewholders.BaseViewHolder
import com.ashish.comcastpokemon.viewmodels.AppViewModelFactory
import com.ashish.comcastpokemon.viewmodels.PokemonViewModel


const val COMMON_TAG = "COMMON_TAG"
const val NO_MORE_RESULTS = "NO_MORE_RESULTS"

class PokemonListFragment : Fragment(R.layout.fragment_pokemon_list),
    SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentPokemonListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PokemonViewModel
    private var pokemonList = mutableListOf<PokemonFilteredDetails>()
    private lateinit var adapter: RecyclerView.Adapter<BaseViewHolder>
    private var fragmentActionListener: FragmentActionListener? = null
    private lateinit var layoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPokemonListBinding.bind(view)
        layoutManager = LinearLayoutManager(activity)
        fragmentActionListener = requireActivity() as FragmentActionListener
        initViewModel()
        initListeners()
        initRecyclerView()
        initObservers()
        initSwipeRefreshLayout()
        getPokemonList()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onRefresh() {
        fragmentActionListener?.onAction(FragmentAction.ACTION_CLEAR_VIEWMODEL_DATA, null)
        //Hiding swipe refresh because we show a progress bar when we make the API call
        binding.swipeRefresh.isRefreshing = false
        viewModel.getPokemonList()
    }

    //region private
    private fun initViewModel() {
        viewModel =
            ViewModelProvider(requireActivity(), AppViewModelFactory(requireActivity().application))
                .get(PokemonViewModel::class.java)
    }

    private fun initListeners() {
        binding.btnLoadMore.setOnClickListener {
            getPokemonList()
        }
    }

    private fun initRecyclerView() {
        adapter = PokemonSearchResultsAdapter(pokemonList, ::onItemClicked)
        binding.recyclerviewPokemonList.adapter = adapter
        binding.recyclerviewPokemonList.layoutManager = layoutManager

        binding.recyclerviewPokemonList.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    checkItemsDisplayed()
                }
            }
        })

        binding.recyclerviewPokemonList.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            checkItemsDisplayed()
        }
    }

    private fun checkItemsDisplayed() {
        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
        //get pokemon details for visible items
        if (firstVisibleItem >= 0 && lastVisibleItem >= 0) {
            for (index in firstVisibleItem..lastVisibleItem) {
                if (!pokemonList[index].detailsCallMade) {
                    pokemonList[index].detailsCallMade = true
                    val id = pokemonList[index].id
                    viewModel.getPokemonDetails(id)
                }
            }
        }
    }

    private fun initObservers() {
        viewModel.pokemonListResult.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.frameLayout.visibility = VISIBLE
                    binding.btnLoadMore.isEnabled = false
                }
                Status.SUCCESS -> {
                    val list = viewModel.pokemonListResult.value?.data
                    val oldListSize = pokemonList.size
                    list?.results?.let { resultsList ->
                        resultsList.forEach { pokemonItem ->
                            val id = extractID(pokemonItem.detailsURL)
                            val item = PokemonFilteredDetails(
                                name = pokemonItem.name,
                                id = id,
                                detailsURL = pokemonItem.detailsURL
                            )
                            pokemonList.add(item)
                        }
                        adapter.notifyItemRangeChanged(oldListSize, resultsList.size)
                        binding.frameLayout.visibility = GONE
                        binding.btnLoadMore.isEnabled = true
                    }
                }
                Status.ERROR -> {
                    binding.frameLayout.visibility = GONE
                    binding.btnLoadMore.isEnabled = true
                    Toast.makeText(requireActivity(), it.message, Toast.LENGTH_LONG).show()
                }
                Status.EMPTY -> {
                    val listSize = pokemonList.size
                    pokemonList.clear()
                    adapter.notifyItemRangeChanged(0, pokemonList.size)
                }
            }
        }

        viewModel.pokemonDetailsResult.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    val detailsItem = viewModel.pokemonDetailsResult.value?.data
                    detailsItem?.let {
                        val index = pokemonList.indexOfFirst { it.id == detailsItem.id }
                        if (index != -1) {
                            pokemonList[index].apply {
                                //pokemon icon url
                                iconURL = detailsItem.sprites.back_default

                                //pokemon types list
                                val typesList = ArrayList<String>()
                                detailsItem.types.forEach { type ->
                                    typesList.add(type.type.name)
                                }
                                types = typesList

                                val stats = detailsItem.stats
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
                            adapter.notifyItemChanged(index)
                        }
                    }
                }
                Status.ERROR -> {

                }
                Status.EMPTY -> {

                }
            }
        }

        viewModel.loadMoreURL.observe(viewLifecycleOwner) {
            if (it == NO_MORE_RESULTS) {
                binding.btnLoadMore.isEnabled = false
                Toast.makeText(activity, R.string.no_more_results_message, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun extractID(detailsURL: String?): String {
        if (detailsURL == null) return "-1"
        val sub = detailsURL.substring(0, detailsURL.length - 1)
        return sub.substring(sub.lastIndexOf("/") + 1)
    }

    private fun onItemClicked(pokemonDetails: PokemonFilteredDetails) {
        fragmentActionListener?.onAction(
            FragmentAction.ACTION_BUSINESS_DETAILS_ITEM_CLICK,
            pokemonDetails
        )
        val action =
            PokemonListFragmentDirections.actionPokemonListFragmentToPokemonDetailsFragment()
        findNavController().navigate(action)
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener(this)
    }

    private fun getPokemonList() {
        viewModel.getPokemonList()
    }

    //endregion
}