package com.ashish.comcastpokemon.viewholders

import android.view.View
import android.widget.*
import com.ashish.comcastpokemon.R
import com.ashish.comcastpokemon.models.PokemonFilteredDetails
import com.ashish.comcastpokemon.utils.StringUtil
import com.squareup.picasso.Picasso

class PokemonSearchResultsViewHolder(itemView: View) : BaseViewHolder(itemView) {
    private val pokemonName = itemView.findViewById<TextView>(R.id.tv_pokemon_name)
    private val pokemonImageView = itemView.findViewById<ImageView>(R.id.img_pokemon_image)
    private val typesLabel = itemView.findViewById<TextView>(R.id.types_label)
    private val typesList = itemView.findViewById<TextView>(R.id.text_view_types)

    override fun bind(data: PokemonFilteredDetails) {
        data.apply {
            pokemonName.text = name
            if(data.iconURL != null) {
                Picasso.get().load(data.iconURL).into(pokemonImageView);
            }
            data.types?.let {
                typesLabel.visibility = View.VISIBLE
                typesList.visibility = View.VISIBLE
                val tempList = it
                typesList.text = StringUtil.commaSeparatedStrings(tempList)
            }?: run {
                typesList.visibility = View.GONE
                typesLabel.visibility = View.GONE
            }
        }
    }

}