package tk.mallumo.puppy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun drawableIdByKey(key: String): Int {
    val ctx = LocalContext.current
    return remember(key) {
        ctx.resources.getIdentifier(key, "drawable", ctx.packageName)
    }
}

