package kr.ac.kumoh.ce.s20220830.s26w06navigation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kr.ac.kumoh.ce.s20220830.s26w06navigation.model.Pokemon
import kr.ac.kumoh.ce.s20220830.s26w06navigation.repository.PokemonRepository
import kr.ac.kumoh.ce.s20220830.s26w06navigation.ui.theme.pokemonCardColor
import kr.ac.kumoh.ce.s20220830.s26w06navigation.viewmodel.PokemonListViewModel
import androidx.compose.runtime.setValue
@Composable
fun PokemonListScreen(
    repository: PokemonRepository,
    viewModel: PokemonListViewModel = viewModel {
        PokemonListViewModel(repository)
    },
    onNavigateToDetail: (Int) -> Unit,
) {
    val pokemonList by viewModel.pokemonList.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredPokemonList = remember(pokemonList, searchQuery) {
        val query = searchQuery.trim()
        pokemonList.filter { it.name.contains(query, ignoreCase = true) }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(searchQuery) {
        listState.scrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("포켓몬 이름 검색") },
            placeholder = { Text("이름을 입력하세요") },
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    TextButton(onClick = { searchQuery = "" }) {
                        Text("지우기")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (searchQuery.isNotBlank() && filteredPokemonList.isEmpty()) {
                item {
                    Text(
                        text = "검색 결과가 없습니다.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(filteredPokemonList, key = { it.id }) { pokemon ->
                PokemonCard(pokemon) {
                    onNavigateToDetail(pokemon.id)
                }
            }
        }
    }
}


@Composable
fun PokemonCard(
    pokemon: Pokemon,
    onClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = pokemonCardColor(pokemon.type),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        onClick = { onClick(pokemon.id)}
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = pokemon.image,
                contentDescription = "${pokemon.name} 이미지",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
                    //.clip(CircleShape)
                    .clip(RoundedCornerShape(percent = 10))
                    .background(Color.White)
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
//                    .background(MaterialTheme.colorScheme.primaryContainer),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = pokemon.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = pokemon.type,
                )
            }
            Spacer(Modifier.width(16.dp))
        }
    }
}
