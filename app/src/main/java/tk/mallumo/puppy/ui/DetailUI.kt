package tk.mallumo.puppy.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tk.mallumo.compose.navigation.ComposableNavNode
import tk.mallumo.compose.navigation.LocalNavigation
import tk.mallumo.compose.navigation.NavigationViewModel
import tk.mallumo.compose.navigation.navigationViewModel
import tk.mallumo.puppy.*
import tk.mallumo.puppy.R
import tk.mallumo.puppy.ui.components.LargeImage
import tk.mallumo.puppy.ui.components.SmallFloatingButton
import tk.mallumo.puppy.ui.components.Space
import tk.mallumo.puppy.ui.theme.PuppyThemePreview
import tk.mallumo.puppy.ui.theme.icon
import tk.mallumo.puppy.utils.ImageCache
import tk.mallumo.puppy.utils.LocalImageCache


class DetailVM : NavigationViewModel() {

    val item: StateFlow<DataState<Puppy>> get() = itemInternal
    private val itemInternal = MutableStateFlow<DataState<Puppy>>(DataState.Idle())

    override fun onCleared() {
        itemInternal.value = DataState.Idle()
    }

    fun reload(args: ArgsDetailUI) {
        itemInternal.value = DataState.Loading(itemInternal.value.entry)
        viewModelScope.launch {
            itemInternal.value = DataState.Result(Repository.get(args.puppyID))
        }
    }

}

private sealed class ActionDetailUI {
    object Up : ActionDetailUI()
}


class ArgsDetailUI(var puppyID: Int = -1)

@Composable
@ComposableNavNode(ArgsDetailUI::class)
fun DetailUI() {
    val nav = LocalNavigation.current
    val vm = navigationViewModel<DetailVM>()
    val args = remember { nav.bundledArgs<ArgsDetailUI>() }

    val action: (ActionDetailUI) -> Unit = remember {
        { act ->
            when (act) {
                ActionDetailUI.Up -> nav.up()
            }
        }
    }

    CompositionLocalProvider(LocalImageCache provides ImageCache()) {
        ContentDetailUI(vm.item.collectAsState(), action)
    }

    SideEffect {
        vm.reload(args)
    }
}

@Composable
private fun ContentDetailUI(item: State<DataState<Puppy>>, action: (ActionDetailUI) -> Unit) {
    when (item.value) {
        is DataState.Idle -> LoaderOverlay()
        is DataState.Loading -> LoaderOverlay()
        is DataState.Result -> InnerDetailUI(item.value.entry!!, action)
    }
}

@Composable
private fun InnerDetailUI(entry: Puppy, action: (ActionDetailUI) -> Unit) {
    val petLevels = entry.buildLevels()
    val orientation = LocalConfiguration.current.orientation
    val isPortrait = remember { orientation == Configuration.ORIENTATION_PORTRAIT }

    Row(Modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier
                .weight(1F)
                .fillMaxHeight()
        ) {
            item { TopImage(entry.img, action) }
            item { Title(entry.name) }
            item {
                InfoFieldGroup(entry)
            }
            if (isPortrait) {

                item {
                    Space(size = 16.dp)
                    Text(
                        text = stringResource(R.string.stats),
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                items(petLevels) {
                    LevelsRow(it)
                }
//
                item { Space(16.dp) }
            }
        }
        if (!isPortrait) {
            LazyColumn(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight()
            ) {
                item {
                    Space(size = 16.dp)
                    Text(
                        text = stringResource(R.string.stats),
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Space(size = 8.dp)
                }
                items(petLevels) {
                    LevelsRow(it)
                }
                item { Space(16.dp) }
            }

        }
    }
}


@Composable
private fun Title(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        style = MaterialTheme.typography.h5,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(16.dp)
    )
}

@Composable
private fun TopImage(img: String, action: (ActionDetailUI) -> Unit) {
    val imageW = remember {
        mutableStateOf(0.dp)
    }
    val imageH = remember {
        mutableStateOf(0.dp)
    }
    val screenHeight = LocalConfiguration.current.let {
        remember {
            it.screenHeightDp / 2
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(screenHeight.dp)
            .background(Color.LightGray)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minHeight = 0))
                layout(constraints.maxWidth, constraints.maxHeight) {
                    imageW.value = constraints.maxWidth.toDp()
                    imageH.value = constraints.maxHeight.toDp()
                    placeable.place(0, 0)
                }
            }
    ) {
        if (imageW.value > 0.dp) {
            LargeImage(
                key = img,
                width = imageW.value,
                height = imageH.value
            )
        }
        SmallFloatingButton(
            imageVector = Icons.Default.ArrowBack,
            modifier = Modifier.padding(8.dp)
        ) {
            action(ActionDetailUI.Up)
        }

    }
}

@Composable
private fun InfoFieldGroup(entry: Puppy) {

    InfoField(
        imageVector = Icons.Default.Info,
        title = stringResource(id = R.string.info),
        message = entry.info
    )

    InfoField(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_history),
        title = stringResource(id = R.string.history),
        message = entry.history
    )

    InfoField(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_directions_run),
        title = stringResource(id = R.string.temperament),
        message = entry.temperament
    )

    InfoField(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_repair),
        title = stringResource(id = R.string.upkeep),
        message = entry.upkeep
    )

}


@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun InfoField(
    imageVector: ImageVector,
    title: String,
    message: String
) {
    val showContent = remember { mutableStateOf(false) }
    val rotation = animateFloatAsState(if (showContent.value) 90F else 0F)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(MaterialTheme.colors.surface)
    ) {
        Row(Modifier
            .fillMaxWidth()
            .clickable { showContent.value = !showContent.value }
            .padding(16.dp)) {
            Image(
                imageVector = imageVector,
                contentDescription = title,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.icon),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Space(size = 16.dp)
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.weight(1F))
            Image(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = title,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.icon),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .rotate(rotation.value)

            )
        }
        AnimatedVisibility(visible = showContent.value) {
            Text(
                text = message,
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 16.dp)
            )
        }

    }
}

@Composable
private fun LevelsRow(row: List<PuppyLevel>) {
    Space(size = 16.dp)
    Row(Modifier.fillMaxWidth()) {
        row.forEach {
            Spacer(modifier = Modifier.weight(1F))
            PetLevel(it)
        }
        Spacer(modifier = Modifier.weight(1F))
    }
}


@Composable
private fun PetLevel(item: PuppyLevel) {
    Column(Modifier.width(80.dp)) {
        Box(Modifier.size(80.dp)) {
            MaterialTheme.colors.icon
            Spacer(
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxSize()
                    .background(MaterialTheme.colors.onBackground.copy(alpha = 0.3F))
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.background)

            )
            CircularProgressIndicator(
                progress = item.percentage,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 8.dp,
            )
            Text(
                text = "${item.percentageText}%",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Space(size = 8.dp)
        Text(
            text = stringResource(id = item.nameRes),
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }


}

@Preview(showBackground = true)
@Composable
private fun PreviewDetailUI() {
    val args = ArgsDetailUI(0)
    PuppyThemePreview(args) {
        DetailUI()
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDarkDetailUI() {
    val args = ArgsDetailUI(0)
    PuppyThemePreview(args, darkTheme = true) {
        DetailUI()
    }
}