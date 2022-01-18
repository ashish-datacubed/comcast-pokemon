package com.ashish.comcastpokemon.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ashish.comcastpokemon.R
import com.ashish.comcastpokemon.models.PokemonFilteredDetails
import com.ashish.comcastpokemon.viewholders.BaseViewHolder
import com.ashish.comcastpokemon.viewholders.PokemonSearchResultsViewHolder

class PokemonSearchResultsAdapter(private val list: List<PokemonFilteredDetails>?,
                                  private val clickListener: (PokemonFilteredDetails) -> Unit):   RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_pokemon_list_item, parent, false)
        return PokemonSearchResultsViewHolder(view)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        list?.let {
            holder.bind(list[position])
            holder.itemView.setOnClickListener {
                clickListener(list[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return list?.size?:0
    }
}