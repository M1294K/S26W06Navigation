package kr.ac.kumoh.ce.s20220830.s26w06navigation.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kr.ac.kumoh.ce.s20220830.s26w06navigation.repository.PokemonRepository
import kr.ac.kumoh.ce.s20220830.s26w06navigation.viewmodel.PokemonDetailViewModel

@Composable
fun PokemonDetailScreen(
    pokemonId: Int,
    repository: PokemonRepository,
    viewModel: PokemonDetailViewModel = viewModel {
        PokemonDetailViewModel(
            repository = repository,
            pokemonId = pokemonId
        )
    },
) {
    val pokemon by viewModel.pokemon.collectAsStateWithLifecycle()

    Column(

    ) {
        AsyncImage(
            model = pokemon.image,
            contentDescription = "${pokemon.name} 이미지",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(16.dp),
        )
        Text(pokemon.name)
        Text(pokemon.info)
    }
}
