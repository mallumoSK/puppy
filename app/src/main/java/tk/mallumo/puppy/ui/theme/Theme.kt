package tk.mallumo.puppy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import tk.mallumo.activity.result.ActivityResult
import tk.mallumo.activity.result.LocalActivityResult
import tk.mallumo.compose.navigation.LocalNavigation
import tk.mallumo.compose.navigation.Navigation
import tk.mallumo.puppy.utils.ImageCache
import tk.mallumo.puppy.utils.LocalImageCache

private val DarkColorPalette = darkColors(
    primary = Purple400,
    primaryVariant = Purple700,

    secondary = Pink600,
    secondaryVariant = Pink600,

    background = DarkWindowBG,
    surface = DarkSurface,

    onPrimary = Color.White,
    onSurface = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
)

private val LightColorPalette = lightColors(
    primary = Purple400,
    primaryVariant = Purple700,

    secondary = Pink600,
    secondaryVariant = Pink600,

    background = LightWindowBG,
    surface = LightSurface,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

val Colors.icon: Color
    @Composable
    get() = remember { onBackground.copy(alpha = 0.5F) }

@Composable
fun PuppyThemePreview(
    args: Any? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    CompositionLocalProvider(
        LocalNavigation provides Navigation.preview(args),
        LocalImageCache provides ImageCache()
    ) {
        PuppyTheme(darkTheme) {
            Surface(color = MaterialTheme.colors.background, content = content)
        }
    }

}

@Composable
fun PuppyTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    CompositionLocalProvider(
        LocalActivityResult provides ActivityResult.get()
    ) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }

}