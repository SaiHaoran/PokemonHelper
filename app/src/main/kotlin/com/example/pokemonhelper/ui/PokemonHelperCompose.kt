@file:JvmName("PokemonHelperCompose")

package com.example.pokemonhelper.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokemonhelper.data.PokemonSearchRepository
import com.example.pokemonhelper.data.TypeEffectivenessRepository
import com.example.pokemonhelper.model.PokemonDefenseProfile
import com.example.pokemonhelper.model.PokemonEntry
import com.example.pokemonhelper.model.PokemonType
import com.example.pokemonhelper.model.TypeProfile

fun attach(activity: ComponentActivity) {
    activity.setContent {
        PokemonHelperTheme {
            PokemonHelperApp()
        }
    }
}

private val Cream = Color(0xFFFFF8EC)
private val CardWhite = Color(0xFFFFFCF7)
private val Ink = Color(0xFF263238)
private val PokedexRed = Color(0xFFE94343)
private val SoftBlue = Color(0xFFE5F3FF)
private val LeafGreen = Color(0xFFEAF7DF)
private val Honey = Color(0xFFFFD95A)

@Composable
private fun PokemonHelperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = PokedexRed,
            secondary = Color(0xFF2D9CDB),
            tertiary = Honey,
            background = Cream,
            surface = CardWhite,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Ink,
            onBackground = Ink,
            onSurface = Ink
        ),
        typography = MaterialTheme.typography.copy(
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp
            ),
            titleLarge = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            ),
            titleMedium = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            ),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp)
        ),
        content = content
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PokemonHelperApp() {
    val context = LocalContext.current.applicationContext
    val types = remember { TypeEffectivenessRepository.getAllTypes() }
    val pokemonRepository = remember(context) { PokemonSearchRepository(context) }
    var currentPage by remember { mutableStateOf(AppPage.HOME) }
    var selectedType by remember { mutableStateOf(types.first()) }
    var mode by remember { mutableStateOf(DetailMode.ATTACK) }
    var pokemonQuery by remember { mutableStateOf("") }
    var pokemonTypeFilter by remember { mutableStateOf<PokemonType?>(null) }
    var selectedPokemon by remember { mutableStateOf<PokemonEntry?>(null) }
    val profile = remember(selectedType) { TypeEffectivenessRepository.getProfile(selectedType) }
    val pokemonResults = remember(pokemonQuery, pokemonTypeFilter) {
        pokemonRepository.search(pokemonQuery, pokemonTypeFilter, 12)
    }

    LaunchedEffect(pokemonResults) {
        if (selectedPokemon == null || !pokemonResults.contains(selectedPokemon)) {
            selectedPokemon = pokemonResults.firstOrNull()
        }
    }

    BackHandler(enabled = currentPage == AppPage.SEARCH) {
        currentPage = AppPage.HOME
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.98f))
                    .togetherWith(fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.99f))
            },
            label = "app-page"
        ) { page ->
            when (page) {
                AppPage.HOME -> HomePage(
                    types = types,
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = it },
                    mode = mode,
                    onModeSelected = { mode = it },
                    profile = profile,
                    onSearchClick = { currentPage = AppPage.SEARCH }
                )

                AppPage.SEARCH -> SearchPage(
                    query = pokemonQuery,
                    onQueryChange = { pokemonQuery = it },
                    selectedType = pokemonTypeFilter,
                    onTypeSelected = { pokemonTypeFilter = it },
                    types = types,
                    results = pokemonResults,
                    selectedPokemon = selectedPokemon,
                    onPokemonSelected = { selectedPokemon = it },
                    onBackClick = { currentPage = AppPage.HOME }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HomePage(
    types: List<PokemonType>,
    selectedType: PokemonType,
    onTypeSelected: (PokemonType) -> Unit,
    mode: DetailMode,
    onModeSelected: (DetailMode) -> Unit,
    profile: TypeProfile,
    onSearchClick: () -> Unit
) {
    PageScaffold {
        Header()
        SearchEntryButton(onClick = onSearchClick)
        TypePicker(
            types = types,
            selectedType = selectedType,
            onTypeSelected = onTypeSelected
        )
        ModeSwitch(
            selectedMode = mode,
            onModeSelected = onModeSelected
        )
        AnimatedContent(
            targetState = profile to mode,
            transitionSpec = {
                (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.96f))
                    .togetherWith(fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.98f))
            },
            label = "type-detail"
        ) { (targetProfile, targetMode) ->
            DetailPanel(profile = targetProfile, mode = targetMode)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SearchPage(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedType: PokemonType?,
    onTypeSelected: (PokemonType?) -> Unit,
    types: List<PokemonType>,
    results: List<PokemonEntry>,
    selectedPokemon: PokemonEntry?,
    onPokemonSelected: (PokemonEntry) -> Unit,
    onBackClick: () -> Unit
) {
    PageScaffold {
        SearchTopBar(onBackClick = onBackClick)
        PokemonSearchPanel(
            query = query,
            onQueryChange = onQueryChange,
            selectedType = selectedType,
            onTypeSelected = onTypeSelected,
            types = types,
            results = results,
            selectedPokemon = selectedPokemon,
            onPokemonSelected = onPokemonSelected
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PageScaffold(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PokemonSearchPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedType: PokemonType?,
    onTypeSelected: (PokemonType?) -> Unit,
    types: List<PokemonType>,
    results: List<PokemonEntry>,
    selectedPokemon: PokemonEntry?,
    onPokemonSelected: (PokemonEntry) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "搜索宝可梦",
                style = MaterialTheme.typography.titleMedium,
                color = Ink
            )
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("名称或编号") },
                placeholder = { Text("皮卡丘 / Pikachu / 25") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = RoundedCornerShape(8.dp)
            )
            PokemonTypeFilter(
                types = types,
                selectedType = selectedType,
                onTypeSelected = onTypeSelected
            )
            PokemonResultList(
                results = results,
                selectedPokemon = selectedPokemon,
                onPokemonSelected = onPokemonSelected
            )
            AnimatedContent(
                targetState = selectedPokemon,
                transitionSpec = {
                    (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.97f))
                        .togetherWith(fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.98f))
                },
                label = "pokemon-detail"
            ) { pokemon ->
                if (pokemon == null) {
                    EmptyPokemonState()
                } else {
                    PokemonDefensePanel(pokemon = pokemon)
                }
            }
        }
    }
}

@Composable
private fun PokemonTypeFilter(
    types: List<PokemonType>,
    selectedType: PokemonType?,
    onTypeSelected: (PokemonType?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "按属性筛选",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF78909C)
        )
        val filterItems = listOf<PokemonType?>(null) + types
        filterItems.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { type ->
                    FilterChipTile(
                        label = type?.displayName ?: "不限",
                        color = type?.composeColor() ?: Honey,
                        selected = selectedType == type,
                        onClick = { onTypeSelected(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FilterChipTile(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (selected) color else SoftBlue
    val contentColor = if (selected) color.readableTextColor() else Ink
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Ink.copy(alpha = 0.16f) else Color(0xFFD7E8F7),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun PokemonResultList(
    results: List<PokemonEntry>,
    selectedPokemon: PokemonEntry?,
    onPokemonSelected: (PokemonEntry) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "搜索结果 ${results.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF78909C)
        )
        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LeafGreen)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "没有找到符合条件的宝可梦",
                    color = Color(0xFF607D3B),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            results.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { pokemon ->
                        PokemonResultTile(
                            pokemon = pokemon,
                            selected = pokemon == selectedPokemon,
                            onClick = { onPokemonSelected(pokemon) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(2 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonResultTile(
    pokemon: PokemonEntry,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = pokemon.types.first().composeColor()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFFFFF0D1) else Color(0xFFFFFFFF))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) accent else Color(0xFFE8DDBF),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "#${pokemon.id} ${pokemon.zhName}",
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            maxLines = 1
        )
        Text(
            text = pokemon.enName,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF78909C),
            maxLines = 1
        )
        CompactTypeBadges(types = pokemon.types)
    }
}

@Composable
private fun PokemonDefensePanel(pokemon: PokemonEntry) {
    val defenseProfile = remember(pokemon) {
        TypeEffectivenessRepository.getDefenseProfileFor(pokemon.types)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFFCF7))
            .border(1.dp, Color(0xFFE8DDBF), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "#${pokemon.id} ${pokemon.zhName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
                Text(
                    text = pokemon.enName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF78909C)
                )
            }
            CompactTypeBadges(types = pokemon.types)
        }
        Text(
            text = "防守倍率已按多属性相乘",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF78909C)
        )
        PokemonDefenseRelations(profile = defenseProfile)
    }
}

@Composable
private fun PokemonDefenseRelations(profile: PokemonDefenseProfile) {
    RelationSection(
        title = "特别怕",
        caption = "受到 4x 伤害",
        types = profile.takesFourTimesDamageFrom,
        emptyText = "没有 4x 弱点"
    )
    RelationSection(
        title = "被克制",
        caption = "受到 2x 伤害",
        types = profile.takesDoubleDamageFrom,
        emptyText = "没有 2x 弱点"
    )
    RelationSection(
        title = "抵抗",
        caption = "受到 0.5x 伤害",
        types = profile.takesHalfDamageFrom,
        emptyText = "没有 0.5x 抵抗"
    )
    RelationSection(
        title = "强抵抗",
        caption = "受到 0.25x 伤害",
        types = profile.takesQuarterDamageFrom,
        emptyText = "没有 0.25x 抵抗"
    )
    RelationSection(
        title = "免疫",
        caption = "受到 0x 伤害",
        types = profile.immuneTo,
        emptyText = "没有免疫属性"
    )
}

@Composable
private fun EmptyPokemonState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(LeafGreen)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = "选择一只宝可梦后会显示它的多属性相克信息",
            color = Color(0xFF607D3B),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PokeballMark(
            modifier = Modifier.size(54.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "PokemonHelper",
                color = Ink,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "属性克制速查",
                color = Color(0xFF607D8B),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SearchEntryButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PokedexRed)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchMark(modifier = Modifier.size(24.dp), color = Color.White)
        Text(
            text = "搜索宝可梦",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "名称 / 属性",
            color = Color.White.copy(alpha = 0.82f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SearchTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE8DDBF), RoundedCornerShape(8.dp))
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = Ink,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "搜索宝可梦",
                color = Ink,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "按名称、编号或属性筛选",
                color = Color(0xFF607D8B),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun PokeballMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = Color.White)
        drawArc(
            color = PokedexRed,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset.Zero,
            size = Size(size.width, size.height)
        )
        drawLine(
            color = Ink,
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = 3.dp.toPx()
        )
        drawCircle(color = Ink, radius = size.minDimension * 0.18f)
        drawCircle(color = Color.White, radius = size.minDimension * 0.105f)
        drawCircle(color = Ink, style = stroke)
    }
}

@Composable
private fun SearchMark(
    modifier: Modifier = Modifier,
    color: Color = Ink
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        drawCircle(
            color = color,
            radius = size.minDimension * 0.3f,
            center = Offset(size.width * 0.42f, size.height * 0.42f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.64f, size.height * 0.64f),
            end = Offset(size.width * 0.9f, size.height * 0.9f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun TypePicker(
    types: List<PokemonType>,
    selectedType: PokemonType,
    onTypeSelected: (PokemonType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "选择属性",
                style = MaterialTheme.typography.titleMedium,
                color = Ink
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                types.chunked(3).forEach { rowTypes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTypes.forEach { type ->
                            TypeTile(
                                type = type,
                                selected = type == selectedType,
                                onClick = { onTypeSelected(type) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowTypes.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeTile(
    type: PokemonType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeColor = type.composeColor()
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.015f else 1f,
        animationSpec = tween(160),
        label = "type-tile-scale"
    )
    val background = if (selected) typeColor else SoftBlue
    val contentColor = if (selected) typeColor.readableTextColor() else Ink

    Box(
        modifier = modifier
            .scale(scale)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Ink.copy(alpha = 0.16f) else Color(0xFFD7E8F7),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.displayName,
            color = contentColor,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun ModeSwitch(
    selectedMode: DetailMode,
    onModeSelected: (DetailMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE8DDBF), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DetailMode.values().forEach { mode ->
            val selected = mode == selectedMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (selected) PokedexRed else Color.Transparent)
                    .clickable { onModeSelected(mode) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label,
                    color = if (selected) Color.White else Color(0xFF6A5D4D),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun DetailPanel(profile: TypeProfile, mode: DetailMode) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DetailTitle(type = profile.type)
            when (mode) {
                DetailMode.ATTACK -> AttackRelations(profile)
                DetailMode.DEFENSE -> DefenseRelations(profile)
            }
        }
    }
}

@Composable
private fun DetailTitle(type: PokemonType) {
    val color = type.composeColor()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = type.displayName.take(1),
                color = color.readableTextColor(),
                style = MaterialTheme.typography.titleLarge
            )
        }
        Column {
            Text(
                text = "${type.displayName}属性",
                style = MaterialTheme.typography.titleLarge,
                color = Ink
            )
            Text(
                text = type.apiName.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF78909C)
            )
        }
    }
}

@Composable
private fun AttackRelations(profile: TypeProfile) {
    RelationSection(
        title = "克制",
        caption = "造成 2x 伤害",
        types = profile.strongAgainst,
        emptyText = "没有直接克制的属性"
    )
    RelationSection(
        title = "效果不好",
        caption = "造成 0.5x 伤害",
        types = profile.weakAgainstWhenAttacking,
        emptyText = "没有半减伤目标"
    )
    RelationSection(
        title = "无效",
        caption = "造成 0x 伤害",
        types = profile.noEffectAgainst,
        emptyText = "没有免疫目标"
    )
}

@Composable
private fun DefenseRelations(profile: TypeProfile) {
    RelationSection(
        title = "被克制",
        caption = "受到 2x 伤害",
        types = profile.weakTo,
        emptyText = "没有弱点"
    )
    RelationSection(
        title = "抵抗",
        caption = "受到 0.5x 伤害",
        types = profile.resists,
        emptyText = "没有抵抗属性"
    )
    RelationSection(
        title = "免疫",
        caption = "受到 0x 伤害",
        types = profile.immuneTo,
        emptyText = "没有免疫属性"
    )
}

@Composable
private fun RelationSection(
    title: String,
    caption: String,
    types: List<PokemonType>,
    emptyText: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Ink
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF78909C)
            )
        }
        if (types.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LeafGreen)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    text = emptyText,
                    color = Color(0xFF607D3B),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            FlowBadges(types = types)
        }
    }
}

@Composable
private fun FlowBadges(types: List<PokemonType>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.chunked(3).forEach { rowTypes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTypes.forEach { type ->
                    TypeBadge(
                        type = type,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowTypes.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TypeBadge(
    type: PokemonType,
    modifier: Modifier = Modifier
) {
    val color = type.composeColor()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.displayName,
            color = color.readableTextColor(),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )
    }
}

@Composable
private fun CompactTypeBadges(types: List<PokemonType>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        types.forEach { type ->
            val color = type.composeColor()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.displayName,
                    color = color.readableTextColor(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
        }
    }
}

private enum class DetailMode(val label: String) {
    ATTACK("攻击视角"),
    DEFENSE("防守视角")
}

private enum class AppPage {
    HOME,
    SEARCH
}

private fun PokemonType.composeColor(): Color {
    return Color(android.graphics.Color.parseColor(colorHex))
}

private fun Color.readableTextColor(): Color {
    return if (luminance() > 0.56f) Ink else Color.White
}
