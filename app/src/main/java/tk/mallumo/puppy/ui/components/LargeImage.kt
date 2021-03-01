package tk.mallumo.puppy.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import tk.mallumo.puppy.drawableIdByKey
import tk.mallumo.puppy.utils.LocalImageCache

@Composable
@SuppressLint("ModifierParameter")
fun LargeImage(key: String, width: Dp, height: Dp = width, modifier: Modifier = Modifier) {
    val imageCache = LocalImageCache.current
    val imageState = imageCache.largeImage(drawableIdByKey(key = key), width, height)
    val alpha by animateFloatAsState(if (imageState.loadComplete.value) 1F else 0F)
    Image(
        bitmap = imageState.image.value,//ImageBitmap.imageResource(id = drawableIdByKey(key = item.img)),
        contentDescription = key,
        contentScale = ContentScale.Crop,
        alpha = alpha,
        modifier = modifier.size(width, height)
    )
}
