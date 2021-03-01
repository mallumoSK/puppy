package tk.mallumo.puppy.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tk.mallumo.compose.navigation.*
import tk.mallumo.puppy.DataState
import tk.mallumo.puppy.PuppySimple
import tk.mallumo.puppy.R
import tk.mallumo.puppy.Repository
import tk.mallumo.puppy.ui.components.LargeImage
import tk.mallumo.puppy.ui.components.SearchMode
import tk.mallumo.puppy.ui.components.Toolbar
import tk.mallumo.puppy.ui.components.ToolbarSearch
import tk.mallumo.puppy.ui.theme.PuppyThemePreview
import tk.mallumo.puppy.utils.ImageCache
import tk.mallumo.puppy.utils.LocalImageCache
import tk.mallumo.utils.alsoIf
import tk.mallumo.utils.noDiacriticsLower

class ItemsVM : NavigationViewModel() {

    val items: StateFlow<DataState<List<PuppySimple>>> get() = itemsInternal
    private val itemsInternal = MutableStateFlow<DataState<List<PuppySimple>>>(DataState.Idle())

    private var lastItem = -1

    val lastSelectedItemId: () -> Int = {
        lastItem.also {
            lastItem = -1
        }
    }
    private var toolbarQueryFilter = ""
    private var allItems: List<PuppySimple>? = null

    fun saveScrollerPossition(id: Int) {
        lastItem = id
    }

    override fun onCleared() {
        itemsInternal.value = DataState.Idle()
        allItems = null
        toolbarQueryFilter = ""
        lastItem = -1
    }

    fun invokeSearch(filter: String) {
        toolbarQueryFilter = filter
        viewModelScope.launch {
            itemsInternal.value = DataState.Result(applyQueryFilter(allItems ?: listOf()))
        }
    }


    fun reload() {
        itemsInternal.value = DataState.Loading(itemsInternal.value.entry)
        viewModelScope.launch {
            allItems = Repository.getAll()
            itemsInternal.value = DataState.Result(applyQueryFilter(allItems!!))
        }
    }

    private suspend fun applyQueryFilter(items: List<PuppySimple>): List<PuppySimple> = coroutineScope {
        val query = toolbarQueryFilter.noDiacriticsLower

        if (query.isEmpty()) items
        else items.filter { it.name.noDiacriticsLower.contains(query) }
    }


}

private sealed class ActionItemsUI {
    class ItemSelected(val info: PuppySimple) : ActionItemsUI()
    class InvokeSearch(val query: String) : ActionItemsUI()
}

@Composable
@ComposableNavNode
fun ItemsUI() {
    val vm = navigationViewModel<ItemsVM>()
    val nav = LocalNavigation.current

    val action: (ActionItemsUI) -> Unit = remember {
        { act ->
            when (act) {
                is ActionItemsUI.ItemSelected -> {
                    vm.saveScrollerPossition(act.info.id)
                    nav.navTo_DetailUI(ArgsDetailUI(puppyID = act.info.id))
                }
                is ActionItemsUI.InvokeSearch ->
                    vm.invokeSearch(act.query)
            }
        }
    }

    CompositionLocalProvider(LocalImageCache provides ImageCache()) {
        ContentItemsUI(vm.items.collectAsState(), vm.lastSelectedItemId, action)
    }
    SideEffect {
        vm.reload()
    }
}


@Composable
private fun ContentItemsUI(items: State<DataState<List<PuppySimple>>>, lastSelectedItemId: () -> Int, action: (ActionItemsUI) -> Unit) {
    when (items.value) {
        is DataState.Idle -> LoaderOverlay()
        is DataState.Loading -> LoaderOverlay()
        is DataState.Result -> InnerItemsUI(items.value.entry!!, lastSelectedItemId, action)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InnerItemsUI(
    items: List<PuppySimple>,
    lastSelectedItemId: () -> Int,
    action: (ActionItemsUI) -> Unit
) {
    val listState = rememberLazyListState()

    Column(Modifier.fillMaxSize()) {
        TopItemsUI(action)
        LazyVerticalGrid(
            cells = GridCells.Adaptive(188.dp),
            state = listState,
            contentPadding = PaddingValues(4.dp)
        ) {
            items(items) {
                CellItemsUI(it, Modifier.align(Alignment.CenterHorizontally)) {
                    action(ActionItemsUI.ItemSelected(it))
                }
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        lastSelectedItemId().alsoIf({ it > 0 }) { id ->
            items.indexOfFirst { it.id == id }.alsoIf({ it > -1 }) {
                //not working!!
                listState.scrollToItem(it)
            }
        }
    }
}

@Composable
private fun CellItemsUI(item: PuppySimple, modifier: Modifier, onClick: () -> Unit) {
    val imageSize = remember {
        mutableStateOf(0.dp)
    }
    Card(
        modifier = modifier
            .padding(4.dp)
            .clickable { onClick() },
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints.copy(minHeight = 0))
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            imageSize.value = constraints.maxWidth.toDp()
                            placeable.place(0, 0)
                        }
                    }
            ) {
                if (imageSize.value > 0.dp) {
                    LargeImage(
                        key = item.img,
                        width = imageSize.value
                    )
                }
            }

            Text(
                text = item.name,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun TopItemsUI(action: (ActionItemsUI) -> Unit) {

    val searchActions: (ToolbarSearch) -> Unit = remember {
        { act ->
            when (act) {
                is ToolbarSearch.InvokeSearch -> {
                    // search is in runtime ...
                }
                is ToolbarSearch.OnClear -> {
                    action(ActionItemsUI.InvokeSearch(""))
                }
                is ToolbarSearch.OnTextChange -> {
                    action(ActionItemsUI.InvokeSearch(act.query))
                }
                is ToolbarSearch.Up -> {
                    action(ActionItemsUI.InvokeSearch(""))
                }
            }
        }
    }
    Toolbar(
        title = stringResource(id = R.string.app_name),
        searchHint = stringResource(id = R.string.name_of_puppy),
        searchMode = SearchMode.Disabled,
        searchAction = searchActions,
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewInnerItemsUI() {
    PuppyThemePreview {
        InnerItemsUI(Repository.Preview.items(), { -1 }) {

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewItemsUI() {
    PuppyThemePreview {
        ItemsUI()
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDarkItemsUI() {
    PuppyThemePreview(darkTheme = true) {
        ItemsUI()
    }
}