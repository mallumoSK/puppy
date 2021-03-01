package tk.mallumo.puppy.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tk.mallumo.puppy.R


@Composable
fun ToolbarAction(imgRes: Int, tooltipRes: Int, imgColorTint: Color = Color.White, onClick: (() -> Unit)) {
    ToolbarActionImpl(imgRes, tooltipRes, imgColorTint, onClick)
}

@Composable
private fun ToolbarActionImpl(
    imgRes: Int,
    tooltipRes: Int,
    tint: Color,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick == null) {
        Modifier
    } else {
        Modifier.clickable(onClick = onClick)
    }
    Image(
        imageVector = ImageVector.vectorResource(id = imgRes),
        stringResource(id = tooltipRes),
        colorFilter = ColorFilter.tint(tint),
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .then(modifier)
            .padding(12.dp)
    )
}

sealed class ToolbarSearch {
    object InvokeSearch : ToolbarSearch()
    object OnClear : ToolbarSearch()
    class OnTextChange(val query: String) : ToolbarSearch()
    object Up : ToolbarSearch()
}

sealed class SearchMode {
    object None : SearchMode()
    object Enabled : SearchMode()
    object Disabled : SearchMode()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Toolbar(
    title: String,
    subtitle: String = "",
    up: (() -> Unit)? = null,
    searchMode: SearchMode = SearchMode.None,
    searchQuery: MutableState<String> = mutableStateOf(""),
    searchHint: String = "",
    searchAction: ((ToolbarSearch) -> Unit)? = null,
    @SuppressLint("ComposableLambdaParameterNaming") contentActions: @Composable RowScope.() -> Unit = {}
) {
    val (searchState, updateSearchState) = remember(searchMode) { mutableStateOf(searchMode) }
    val (query, setQuery) = remember { searchQuery }
    val focusRequest = remember { FocusRequester() }
    val searchModifier = remember(focusRequest) {
        Modifier
            .fillMaxWidth()
            .focusModifier()
            .focusRequester(focusRequest)
    }
    val lifecycleScope = LocalLifecycleOwner.current.lifecycleScope

    val upIcon: (@Composable (() -> Unit)?) = if (searchState == SearchMode.Enabled || up != null) {
        {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                contentDescription = stringResource(id = R.string.up),
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        when (searchState) {
                            SearchMode.Enabled -> {
                                updateSearchState(SearchMode.Disabled)
                                focusRequest.freeFocus()
                                searchAction?.invoke(ToolbarSearch.Up)
                                setQuery("")
                            }
                            else -> up?.invoke()
                        }
                    }
                    .padding(8.dp)
            )
        }
    } else {
        null
    }
    val titleBar: (@Composable (() -> Unit)) = {
        if (searchState == SearchMode.Enabled) {
            TextField(
                value = query,
                onValueChange = {
                    setQuery(it)
                    searchAction?.invoke(ToolbarSearch.OnTextChange(it))
                },
                singleLine = true,
                maxLines = 1,
                textStyle = MaterialTheme.typography.subtitle1,
                placeholder = {
                    Text(
                        text = searchHint,
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onPrimary.copy(alpha = 0.6F)
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                keyboardActions = KeyboardActions { searchAction?.invoke(ToolbarSearch.InvokeSearch) },
                modifier = searchModifier
            )
        } else {
            Column {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.subtitle2
                    )
                }
            }
        }
    }
    val actions: @Composable RowScope.() -> Unit = {
        AnimatedVisibility(visible = searchState == SearchMode.Disabled) {
            ToolbarAction(imgRes = R.drawable.ic_search, tooltipRes = R.string.search, onClick = {
                updateSearchState(SearchMode.Enabled)
                lifecycleScope.launch {
                    delay(50)
                    focusRequest.requestFocus()
                }
            })
        }
        AnimatedVisibility(visible = searchState != SearchMode.Enabled) {
            contentActions()
        }
        AnimatedVisibility(visible = searchState == SearchMode.Enabled) {
            ToolbarAction(imgRes = R.drawable.ic_clear, tooltipRes = R.string.clean_search, onClick = {
                setQuery("")
                searchAction?.invoke(ToolbarSearch.OnClear)
            })
        }

    }
    TopAppBar(
        title = titleBar,
        navigationIcon = upIcon,
        actions = actions
    )
}