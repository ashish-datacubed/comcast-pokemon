package com.ashish.comcastpokemon.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ashish.comcastpokemon.models.PokemonFilteredDetails

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    abstract fun bind(data: PokemonFilteredDetails)

}