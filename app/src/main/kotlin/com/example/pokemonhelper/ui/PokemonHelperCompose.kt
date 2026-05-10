@file:JvmName("PokemonHelperCompose")

package com.example.pokemonhelper.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokemonhelper.data.ItemSearchRepository
import com.example.pokemonhelper.data.PokemonSearchRepository
import com.example.pokemonhelper.data.TypeEffectivenessRepository
import com.example.pokemonhelper.model.ItemEntry
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
private const val PokemonPageSize = 6
private const val ItemPageSize = 6

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
    val itemRepository = remember(context) { ItemSearchRepository(context) }
    var currentPage by remember { mutableStateOf(AppPage.HOME) }
    var selectedType by remember { mutableStateOf(types.first()) }
    var mode by remember { mutableStateOf(DetailMode.ATTACK) }
    var pokemonQuery by remember { mutableStateOf("") }
    var pokemonTypeFilter by remember { mutableStateOf<PokemonType?>(null) }
    var pokemonPageIndex by remember { mutableStateOf(0) }
    var pokemonPageInput by remember { mutableStateOf("1") }
    var selectedPokemon by remember { mutableStateOf<PokemonEntry?>(null) }
    var itemQuery by remember { mutableStateOf("") }
    var itemPageIndex by remember { mutableStateOf(0) }
    var itemPageInput by remember { mutableStateOf("1") }
    var selectedItem by remember { mutableStateOf<ItemEntry?>(null) }
    val profile = remember(selectedType) { TypeEffectivenessRepository.getProfile(selectedType) }
    val pokemonResults = remember(pokemonQuery, pokemonTypeFilter) {
        pokemonRepository.search(pokemonQuery, pokemonTypeFilter, 0)
    }
    val pokemonPageCount = remember(pokemonResults) {
        if (pokemonResults.isEmpty()) {
            1
        } else {
            ((pokemonResults.size - 1) / PokemonPageSize) + 1
        }
    }
    val pagedPokemonResults = remember(pokemonResults, pokemonPageIndex) {
        pokemonResults
            .drop(pokemonPageIndex * PokemonPageSize)
            .take(PokemonPageSize)
    }
    val itemResults = remember(itemQuery) {
        itemRepository.search(itemQuery, 0)
    }
    val itemPageCount = remember(itemResults) {
        if (itemResults.isEmpty()) {
            1
        } else {
            ((itemResults.size - 1) / ItemPageSize) + 1
        }
    }
    val pagedItemResults = remember(itemResults, itemPageIndex) {
        itemResults
            .drop(itemPageIndex * ItemPageSize)
            .take(ItemPageSize)
    }

    LaunchedEffect(pokemonQuery, pokemonTypeFilter) {
        pokemonPageIndex = 0
    }

    LaunchedEffect(itemQuery) {
        itemPageIndex = 0
    }

    LaunchedEffect(pokemonPageCount) {
        if (pokemonPageIndex >= pokemonPageCount) {
            pokemonPageIndex = pokemonPageCount - 1
        }
    }

    LaunchedEffect(itemPageCount) {
        if (itemPageIndex >= itemPageCount) {
            itemPageIndex = itemPageCount - 1
        }
    }

    LaunchedEffect(pagedPokemonResults) {
        if (selectedPokemon == null || !pagedPokemonResults.contains(selectedPokemon)) {
            selectedPokemon = pagedPokemonResults.firstOrNull()
        }
    }

    LaunchedEffect(pagedItemResults) {
        if (selectedItem == null || !pagedItemResults.contains(selectedItem)) {
            selectedItem = pagedItemResults.firstOrNull()
        }
    }

    LaunchedEffect(pokemonPageIndex, pokemonPageCount) {
        pokemonPageInput = (pokemonPageIndex + 1).toString()
    }

    LaunchedEffect(itemPageIndex, itemPageCount) {
        itemPageInput = (itemPageIndex + 1).toString()
    }

    BackHandler(enabled = currentPage != AppPage.HOME) {
        currentPage = AppPage.HOME
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.98f))
                        .togetherWith(fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.99f))
                },
                label = "app-page"
            ) { page ->
                when (page) {
                    AppPage.HOME -> TypePage(
                        types = types,
                        selectedType = selectedType,
                        onTypeSelected = { selectedType = it },
                        mode = mode,
                        onModeSelected = { mode = it },
                        profile = profile
                    )

                    AppPage.POKEMON -> PokemonSearchPage(
                        query = pokemonQuery,
                        onQueryChange = { pokemonQuery = it },
                        selectedType = pokemonTypeFilter,
                        onTypeSelected = { pokemonTypeFilter = it },
                        types = types,
                        results = pagedPokemonResults,
                        totalResultCount = pokemonResults.size,
                        pageIndex = pokemonPageIndex,
                        pageCount = pokemonPageCount,
                        onPreviousPage = {
                            if (pokemonPageIndex > 0) {
                                pokemonPageIndex -= 1
                            }
                        },
                        onNextPage = {
                            if (pokemonPageIndex < pokemonPageCount - 1) {
                                pokemonPageIndex += 1
                            }
                        },
                        pageInput = pokemonPageInput,
                        onPageInputChange = { value ->
                            pokemonPageInput = value.filter { it.isDigit() }.take(4)
                        },
                        onPageInputSubmit = {
                            val requestedPage = pokemonPageInput.toIntOrNull()
                            if (requestedPage == null) {
                                pokemonPageInput = (pokemonPageIndex + 1).toString()
                            } else {
                                pokemonPageIndex = requestedPage.coerceIn(1, pokemonPageCount) - 1
                            }
                        },
                        selectedPokemon = selectedPokemon,
                        onPokemonSelected = { selectedPokemon = it }
                    )

                    AppPage.ITEMS -> ItemSearchPage(
                        query = itemQuery,
                        onQueryChange = { itemQuery = it },
                        results = pagedItemResults,
                        totalResultCount = itemResults.size,
                        pageIndex = itemPageIndex,
                        pageCount = itemPageCount,
                        onPreviousPage = {
                            if (itemPageIndex > 0) {
                                itemPageIndex -= 1
                            }
                        },
                        onNextPage = {
                            if (itemPageIndex < itemPageCount - 1) {
                                itemPageIndex += 1
                            }
                        },
                        pageInput = itemPageInput,
                        onPageInputChange = { value ->
                            itemPageInput = value.filter { it.isDigit() }.take(4)
                        },
                        onPageInputSubmit = {
                            val requestedPage = itemPageInput.toIntOrNull()
                            if (requestedPage == null) {
                                itemPageInput = (itemPageIndex + 1).toString()
                            } else {
                                itemPageIndex = requestedPage.coerceIn(1, itemPageCount) - 1
                            }
                        },
                        selectedItem = selectedItem,
                        onItemSelected = { selectedItem = it }
                    )
                }
            }
            BottomTabBar(
                currentPage = currentPage,
                onPageSelected = { currentPage = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun TypePage(
    types: List<PokemonType>,
    selectedType: PokemonType,
    onTypeSelected: (PokemonType) -> Unit,
    mode: DetailMode,
    onModeSelected: (DetailMode) -> Unit,
    profile: TypeProfile
) {
    var typePickerExpanded by remember { mutableStateOf(true) }
    PageScaffold {
        SectionHeader(
            title = "属性克制查询",
            subtitle = "选择属性，查看攻击与防守关系"
        )
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
                TypePicker(
                    types = types,
                    selectedType = selectedType,
                    onTypeSelected = onTypeSelected,
                    expanded = typePickerExpanded,
                    onExpandedChange = { typePickerExpanded = it }
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
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PokemonSearchPage(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedType: PokemonType?,
    onTypeSelected: (PokemonType?) -> Unit,
    types: List<PokemonType>,
    results: List<PokemonEntry>,
    totalResultCount: Int,
    pageIndex: Int,
    pageCount: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    pageInput: String,
    onPageInputChange: (String) -> Unit,
    onPageInputSubmit: () -> Unit,
    selectedPokemon: PokemonEntry?,
    onPokemonSelected: (PokemonEntry) -> Unit
) {
    PageScaffold {
        SectionHeader(
            title = "宝可梦查询",
            subtitle = "按名称、编号或属性筛选"
        )
        PokemonSearchPanel(
            query = query,
            onQueryChange = onQueryChange,
            selectedType = selectedType,
            onTypeSelected = onTypeSelected,
            types = types,
            results = results,
            totalResultCount = totalResultCount,
            pageIndex = pageIndex,
            pageCount = pageCount,
            onPreviousPage = onPreviousPage,
            onNextPage = onNextPage,
            pageInput = pageInput,
            onPageInputChange = onPageInputChange,
            onPageInputSubmit = onPageInputSubmit,
            selectedPokemon = selectedPokemon,
            onPokemonSelected = onPokemonSelected
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ItemSearchPage(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<ItemEntry>,
    totalResultCount: Int,
    pageIndex: Int,
    pageCount: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    pageInput: String,
    onPageInputChange: (String) -> Unit,
    onPageInputSubmit: () -> Unit,
    selectedItem: ItemEntry?,
    onItemSelected: (ItemEntry) -> Unit
) {
    PageScaffold {
        SectionHeader(
            title = "物品查询",
            subtitle = "按中文名、英文名、分类或编号搜索"
        )
        ItemSearchPanel(
            query = query,
            onQueryChange = onQueryChange,
            results = results,
            totalResultCount = totalResultCount,
            pageIndex = pageIndex,
            pageCount = pageCount,
            onPreviousPage = onPreviousPage,
            onNextPage = onNextPage,
            pageInput = pageInput,
            onPageInputChange = onPageInputChange,
            onPageInputSubmit = onPageInputSubmit,
            selectedItem = selectedItem,
            onItemSelected = onItemSelected
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PageScaffold(content: @Composable () -> Unit) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = statusBarPadding + 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            content()
            Spacer(modifier = Modifier.height(82.dp))
        }
        StatusBarFadeOverlay(
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun StatusBarFadeOverlay(modifier: Modifier = Modifier) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(statusBarPadding + 42.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Cream.copy(alpha = 0.96f),
                        Cream.copy(alpha = 0.82f),
                        Cream.copy(alpha = 0.36f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun BottomTabBar(
    currentPage: AppPage,
    onPageSelected: (AppPage) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE8DDBF), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BottomTabButton(
            page = AppPage.POKEMON,
            currentPage = currentPage,
            label = "宝可梦",
            onPageSelected = onPageSelected,
            modifier = Modifier.weight(1f)
        )
        BottomHomeTabButton(
            currentPage = currentPage,
            onPageSelected = onPageSelected,
            modifier = Modifier.weight(0.82f)
        )
        BottomTabButton(
            page = AppPage.ITEMS,
            currentPage = currentPage,
            label = "物品",
            onPageSelected = onPageSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BottomHomeTabButton(
    currentPage: AppPage,
    onPageSelected: (AppPage) -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = currentPage == AppPage.HOME
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (selected) Color(0xFFFFF0D1) else Color.Transparent)
            .clickable { onPageSelected(AppPage.HOME) },
        contentAlignment = Alignment.Center
    ) {
        PokeballMark(modifier = Modifier.size(31.dp))
    }
}

@Composable
private fun BottomTabButton(
    page: AppPage,
    currentPage: AppPage,
    label: String,
    onPageSelected: (AppPage) -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = page == currentPage
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (selected) PokedexRed else Color.Transparent)
            .clickable { onPageSelected(page) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF6A5D4D),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PokeballMark(modifier = Modifier.size(48.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = Ink,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = subtitle,
                color = Color(0xFF607D8B),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun CollapsibleHeader(
    title: String,
    summary: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Ink,
                maxLines = 1
            )
            AnimatedVisibility(
                visible = !expanded,
                enter = expandVertically(animationSpec = tween(140)) + fadeIn(animationSpec = tween(140)),
                exit = shrinkVertically(animationSpec = tween(100)) + fadeOut(animationSpec = tween(100))
            ) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF78909C),
                    maxLines = 1
                )
            }
        }
        Box(
            modifier = Modifier
                .height(30.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (expanded) Color(0xFFECE4D4) else PokedexRed)
                .clickable { onExpandedChange(!expanded) }
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (expanded) "收起" else "展开",
                color = if (expanded) Color(0xFF6A5D4D) else Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
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
    totalResultCount: Int,
    pageIndex: Int,
    pageCount: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    pageInput: String,
    onPageInputChange: (String) -> Unit,
    onPageInputSubmit: () -> Unit,
    selectedPokemon: PokemonEntry?,
    onPokemonSelected: (PokemonEntry) -> Unit
) {
    var typeFilterExpanded by remember { mutableStateOf(true) }
    var resultListExpanded by remember { mutableStateOf(true) }
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
                onTypeSelected = onTypeSelected,
                expanded = typeFilterExpanded,
                onExpandedChange = { typeFilterExpanded = it }
            )
            PokemonResultList(
                results = results,
                totalResultCount = totalResultCount,
                selectedPokemon = selectedPokemon,
                onPokemonSelected = onPokemonSelected,
                expanded = resultListExpanded,
                onExpandedChange = { resultListExpanded = it }
            )
            PaginationControls(
                pageIndex = pageIndex,
                pageCount = pageCount,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
                pageInput = pageInput,
                onPageInputChange = onPageInputChange,
                onPageInputSubmit = onPageInputSubmit
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
    onTypeSelected: (PokemonType?) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CollapsibleHeader(
            title = "按属性筛选",
            summary = selectedType?.displayName ?: "不限",
            expanded = expanded,
            onExpandedChange = onExpandedChange
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(180)) + fadeIn(animationSpec = tween(160)),
            exit = shrinkVertically(animationSpec = tween(150)) + fadeOut(animationSpec = tween(120))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val filterItems = listOf<PokemonType?>(null) + types
                filterItems.chunked(3).forEach { rowItems ->
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
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ItemSearchPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<ItemEntry>,
    totalResultCount: Int,
    pageIndex: Int,
    pageCount: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    pageInput: String,
    onPageInputChange: (String) -> Unit,
    onPageInputSubmit: () -> Unit,
    selectedItem: ItemEntry?,
    onItemSelected: (ItemEntry) -> Unit
) {
    var resultListExpanded by remember { mutableStateOf(true) }
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
                text = "搜索物品",
                style = MaterialTheme.typography.titleMedium,
                color = Ink
            )
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("名称、分类或编号") },
                placeholder = { Text("大师球 / Potion / Medicine / 1") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = RoundedCornerShape(8.dp)
            )
            ItemDetailSection(
                selectedItem = selectedItem
            )
            ItemResultList(
                results = results,
                totalResultCount = totalResultCount,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                expanded = resultListExpanded,
                onExpandedChange = { resultListExpanded = it }
            )
            PaginationControls(
                pageIndex = pageIndex,
                pageCount = pageCount,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
                pageInput = pageInput,
                onPageInputChange = onPageInputChange,
                onPageInputSubmit = onPageInputSubmit
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ItemDetailSection(
    selectedItem: ItemEntry?
) {
    AnimatedContent(
        targetState = selectedItem,
        transitionSpec = {
            (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.97f))
                .togetherWith(fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.98f))
        },
        label = "item-detail"
    ) { item ->
        if (item == null) {
            EmptyItemState()
        } else {
            ItemDetailPanel(item = item)
        }
    }
}

@Composable
private fun ItemResultList(
    results: List<ItemEntry>,
    totalResultCount: Int,
    selectedItem: ItemEntry?,
    onItemSelected: (ItemEntry) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CollapsibleHeader(
            title = "搜索结果 $totalResultCount",
            summary = selectedItem?.let { "#${it.id} ${it.zhName}" } ?: "未选择",
            expanded = expanded,
            onExpandedChange = onExpandedChange
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(180)) + fadeIn(animationSpec = tween(160)),
            exit = shrinkVertically(animationSpec = tween(150)) + fadeOut(animationSpec = tween(120))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (results.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(LeafGreen)
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "没有找到符合条件的物品",
                            color = Color(0xFF607D3B),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    results.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowItems.forEach { item ->
                                ItemResultTile(
                                    item = item,
                                    selected = item == selectedItem,
                                    onClick = { onItemSelected(item) },
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
    }
}

@Composable
private fun ItemResultTile(
    item: ItemEntry,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFFFFF0D1) else Color.White)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Honey else Color(0xFFE8DDBF),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "#${item.id} ${item.zhName}",
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            maxLines = 1
        )
        Text(
            text = item.enName,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF78909C),
            maxLines = 1
        )
        Text(
            text = item.category,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6A5D4D),
            maxLines = 1
        )
    }
}

@Composable
private fun ItemDetailPanel(item: ItemEntry) {
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemSpriteImage(item = item, sizeDp = 66)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "#${item.id} ${item.zhName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
                Text(
                    text = item.enName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF78909C)
                )
                Text(
                    text = "${item.category} · ₽${item.cost}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6A5D4D)
                )
            }
        }
        Text(
            text = if (item.bestEffectText.isEmpty()) "PokeAPI 暂无该物品的功能说明。" else item.bestEffectText,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink
        )
    }
}

@Composable
private fun EmptyItemState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(LeafGreen)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = "选择一个物品后会显示它的分类、价格和功能说明",
            color = Color(0xFF607D3B),
            style = MaterialTheme.typography.bodyMedium
        )
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
    totalResultCount: Int,
    selectedPokemon: PokemonEntry?,
    onPokemonSelected: (PokemonEntry) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CollapsibleHeader(
            title = "搜索结果 $totalResultCount",
            summary = selectedPokemon?.let { "#${it.id} ${it.zhName}" } ?: "未选择",
            expanded = expanded,
            onExpandedChange = onExpandedChange
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(180)) + fadeIn(animationSpec = tween(160)),
            exit = shrinkVertically(animationSpec = tween(150)) + fadeOut(animationSpec = tween(120))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
    }
}

@Composable
private fun PaginationControls(
    pageIndex: Int,
    pageCount: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    pageInput: String,
    onPageInputChange: (String) -> Unit,
    onPageInputSubmit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE8DDBF), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PageButton(
            label = "上一页",
            enabled = pageIndex > 0,
            onClick = onPreviousPage,
            modifier = Modifier.weight(1f)
        )
        BasicTextField(
            value = pageInput,
            onValueChange = onPageInputChange,
            modifier = Modifier
                .weight(0.72f)
                .height(36.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(CardWhite)
                .border(1.dp, Color(0xFFE8DDBF), RoundedCornerShape(7.dp))
                .padding(horizontal = 10.dp),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = Ink,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onPageInputSubmit() }
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                }
            }
        )
        PageButton(
            label = "下一页",
            enabled = pageIndex < pageCount - 1,
            onClick = onNextPage,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PageButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (enabled) PokedexRed else Color(0xFFECE4D4))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) Color.White else Color(0xFF9E9385),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )
    }
}

@Composable
private fun PokemonSpriteImage(
    pokemon: PokemonEntry,
    sizeDp: Int
) {
    AssetImage(
        assetPath = pokemon.spriteAssetPath,
        contentDescription = pokemon.zhName,
        sizeDp = sizeDp,
        fallback = {
            PokeballMark(modifier = Modifier.size((sizeDp * 0.58f).dp))
        }
    )
}

@Composable
private fun ItemSpriteImage(
    item: ItemEntry,
    sizeDp: Int
) {
    AssetImage(
        assetPath = item.spriteAssetPath,
        contentDescription = item.zhName,
        sizeDp = sizeDp,
        fallback = {
            ItemMark(
                modifier = Modifier.size((sizeDp * 0.58f).dp),
                color = item.categoryIconColor()
            )
        }
    )
}

@Composable
private fun AssetImage(
    assetPath: String,
    contentDescription: String,
    sizeDp: Int,
    fallback: @Composable () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(assetPath) {
        try {
            context.assets.open(assetPath).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (exception: Exception) {
            null
        }
    }
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SoftBlue),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap == null) {
            fallback()
        } else {
            Image(
                bitmap = bitmap,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
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
            .clip(RoundedCornerShape(7.dp))
            .background(if (selected) Color(0xFFFFF0D1) else Color(0xFFFFFFFF))
            .border(
                width = 1.dp,
                color = if (selected) accent else Color(0xFFE8DDBF),
                shape = RoundedCornerShape(7.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "#${pokemon.id} ${pokemon.zhName}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Ink,
            maxLines = 1
        )
        Text(
            text = pokemon.enName,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF78909C),
            maxLines = 1
        )
        ResultTypeBadges(types = pokemon.types)
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PokemonSpriteImage(pokemon = pokemon, sizeDp = 74)
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
private fun ItemMark(
    modifier: Modifier = Modifier,
    color: Color = Honey
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        drawCircle(color = color, radius = size.minDimension * 0.42f)
        drawCircle(
            color = Ink,
            radius = size.minDimension * 0.42f,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawCircle(
            color = Color.White,
            radius = size.minDimension * 0.18f,
            center = Offset(size.width * 0.42f, size.height * 0.4f)
        )
        drawLine(
            color = Ink,
            start = Offset(size.width * 0.34f, size.height * 0.68f),
            end = Offset(size.width * 0.66f, size.height * 0.68f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun TypePicker(
    types: List<PokemonType>,
    selectedType: PokemonType,
    onTypeSelected: (PokemonType) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CollapsibleHeader(
            title = "选择属性",
            summary = selectedType.displayName,
            expanded = expanded,
            onExpandedChange = onExpandedChange
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(180)) + fadeIn(animationSpec = tween(160)),
            exit = shrinkVertically(animationSpec = tween(150)) + fadeOut(animationSpec = tween(120))
        ) {
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
            .height(32.dp)
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
            style = MaterialTheme.typography.bodyMedium,
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFFCF7))
            .border(1.dp, Color(0xFFE8DDBF), RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DetailTitle(type = profile.type)
        when (mode) {
            DetailMode.ATTACK -> AttackRelations(profile)
            DetailMode.DEFENSE -> DefenseRelations(profile)
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

@Composable
private fun ResultTypeBadges(types: List<PokemonType>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        types.forEach { type ->
            val color = type.composeColor()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(color)
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.displayName,
                    color = color.readableTextColor(),
                    style = MaterialTheme.typography.bodySmall,
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
    POKEMON,
    ITEMS
}

private fun PokemonType.composeColor(): Color {
    return Color(android.graphics.Color.parseColor(colorHex))
}

private fun Color.readableTextColor(): Color {
    return if (luminance() > 0.56f) Ink else Color.White
}

private fun ItemEntry.categoryIconColor(): Color {
    val normalizedCategory = category.lowercase()
    return when {
        "ball" in normalizedCategory -> PokedexRed
        "medicine" in normalizedCategory ||
                "healing" in normalizedCategory ||
                "revival" in normalizedCategory -> LeafGreen
        "berry" in normalizedCategory -> Color(0xFFFFA7B7)
        "machine" in normalizedCategory -> Color(0xFF8EC5FF)
        "evolution" in normalizedCategory -> Color(0xFFC8B6FF)
        "battle" in normalizedCategory || "stat" in normalizedCategory -> Color(0xFFFFB066)
        "held" in normalizedCategory || "choice" in normalizedCategory || "jewels" in normalizedCategory -> Color(0xFFBDE0FE)
        "key" in normalizedCategory -> Honey
        else -> Honey
    }
}
