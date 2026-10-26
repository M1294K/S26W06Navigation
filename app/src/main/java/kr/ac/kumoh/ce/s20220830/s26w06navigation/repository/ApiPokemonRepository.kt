package kr.ac.kumoh.ce.s20220830.s26w06navigation.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kr.ac.kumoh.ce.s20220830.s26w06navigation.model.Pokemon
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val KOREAN = "ko"

private val koreanTypeNames = mapOf(
    "normal" to "노말",
    "fire" to "불꽃",
    "water" to "물",
    "electric" to "전기",
    "grass" to "풀",
    "ice" to "얼음",
    "fighting" to "격투",
    "poison" to "독",
    "ground" to "땅",
    "flying" to "비행",
    "psychic" to "에스퍼",
    "bug" to "벌레",
    "rock" to "바위",
    "ghost" to "고스트",
    "dragon" to "드래곤",
    "dark" to "악",
    "steel" to "강철",
    "fairy" to "페어리",
    "unknown" to "알 수 없음",
)

class ApiPokemonRepository(
    private val api: PokeApi = pokeService
) : PokemonRepository {

    companion object {
        private val jsonConfig = Json {
            // 서버가 내가 안 만든 데이터(Key)를 보내더라도
            // 에러 내지 말고 그냥 무시
            ignoreUnknownKeys = true
        }

        private val pokeService: PokeApi by lazy {
            Retrofit.Builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                .addConverterFactory(
                    jsonConfig.asConverterFactory("application/json".toMediaType())
                )
                .build()
                .create(PokeApi::class.java)
        }
    }

    override suspend fun getAllPokemon(): List<Pokemon> = coroutineScope {
        api.getPokemonList().results
            .mapNotNull { result ->
                // url: "https://pokeapi.co/api/v2/pokemon/1/"에서
                // id인 1만 분리
                val id = result.url
                    .trimEnd('/')
                    .substringAfterLast('/')
                    .toIntOrNull()
                    ?: return@mapNotNull null
                async {
                    runCatching { fetchPokemon(id) }.getOrNull()
                }
            }
            .awaitAll()
            .filterNotNull()
    }

    override suspend fun getPokemonById(id: Int): Pokemon? =
        runCatching { fetchPokemon(id) }.getOrNull()

    private suspend fun fetchPokemon(id: Int): Pokemon = coroutineScope {
        // 기본 형태의 포켓몬은 pokemon id와 species id가 같으므로
        // 상세 정보와 종 정보를 동시에 요청할 수 있다
        val detail = async { api.getPokemonDetail(id) }
        val species = async { runCatching { api.getPokemonSpecies(id) }.getOrNull() }

        val dto = detail.await()
        // 종 정보를 못 받아도 나머지 정보는 보여줄 수 있게 null을 허용한다
        val speciesDto = species.await()

        // 타입이 여러 개일 때 순서를 유지하며 한국어 이름으로 변환
        val typeList = dto.types
            .sortedBy { it.slot }
            .map { koreanTypeNames[it.type.name] ?: it.type.name }
        val types = typeList
            .joinToString(", ")
            .ifEmpty { "알 수 없음" }

        Pokemon(
            id = dto.id,
            name = speciesDto?.koreanName ?: dto.name,
            type = types,
            info = speciesDto?.koreanInfo.orEmpty(),
            image = dto.sprites.frontDefault ?: ""
        )
    }
}

// 한국어 이름은 모든 종에 대해 하나씩 존재한다
private val PokemonSpeciesDto.koreanName: String?
    get() = names.firstOrNull { it.language.name == KOREAN }?.name

// 게임 버전마다 설명이 여러 개인데, 가장 먼저 나오는(구버전) 설명을 사용한다
private val PokemonSpeciesDto.koreanInfo: String?
    get() = flavorTextEntries
        .firstOrNull { it.language.name == KOREAN }
        ?.flavorText
        // 설명에 줄바꿈(\n)과 페이지 구분자(\f)가 섞여 있어 공백으로 바꾼다
        ?.replace(Regex("\\s+"), " ")
        ?.trim()
