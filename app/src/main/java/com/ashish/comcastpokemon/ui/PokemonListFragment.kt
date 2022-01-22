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
        if (viewModel.pokemonFullList.isEmpty())
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
        adapter = PokemonSearchResultsAdapter(viewModel.pokemonFullList, ::onItemClicked)
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
                if (!viewModel.pokemonFullList[index].detailsCallMade) {
                    viewModel.pokemonFullList[index].detailsCallMade = true
                    val id = viewModel.pokemonFullList[index].id
                    viewModel.getPokemonDetails(id)
                }
            }
        }
    }

    private fun initObservers() {
        viewModel.pokemonListResultEvent.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.frameLayout.visibility = VISIBLE
                    binding.btnLoadMore.isEnabled = false
                }
                Status.SUCCESS -> {
                    Log.d(COMMON_TAG, "SUCCESS OBSERVER")
                    val newElementsCount: Int = it.data!!
                    val totalElementsCount = viewModel.pokemonFullList.size
                    adapter.notifyItemRangeChanged(
                        totalElementsCount - newElementsCount,
                        newElementsCount
                    )
                    binding.frameLayout.visibility = GONE
                    binding.btnLoadMore.isEnabled = true
                }
                Status.ERROR -> {
                    binding.frameLayout.visibility = GONE
                    binding.btnLoadMore.isEnabled = true
                    Toast.makeText(requireActivity(), it.message, Toast.LENGTH_LONG).show()
                }
                Status.EMPTY -> {
                    val numElements = viewModel.pokemonFullList.size
                    adapter.notifyItemRangeChanged(0, numElements)
                }
            }
        }

        viewModel.pokemonDetailsResultEvent.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.LOADING -> {
                    val id: String = result.data!!
                    val index = viewModel.pokemonFullList.indexOfFirst { it.id == id }
                    if (index != -1) {
                        viewModel.pokemonFullList[index].isDetailsLoading = true
                        adapter.notifyItemChanged(index)
                    }
                }
                Status.SUCCESS -> {
                    val id: String = result.data!!
                    val index = viewModel.pokemonFullList.indexOfFirst { it.id == id }
                    if (index != -1) {
                        viewModel.pokemonFullList[index].isDetailsLoading = false
                        adapter.notifyItemChanged(index)
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