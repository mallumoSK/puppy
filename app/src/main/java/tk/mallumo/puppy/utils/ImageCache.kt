package tk.mallumo.puppy.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tk.mallumo.puppy.R


val LocalImageCache = staticCompositionLocalOf<ImageCache> { throw Exception("provider of LocalImageCache is not defined") }

class ImageCache {
    private var size = ((Runtime.getRuntime().maxMemory() / 1024F) / 8F).toInt()
    private val cache = object : LruCache<String, Bitmap?>(size) {

        override fun sizeOf(key: String, bitmap: Bitmap?): Int {
            return (bitmap?.byteCount ?: 0) / 1024
        }
    }

    class LargeImageState(
        val image: State<ImageBitmap>,
        val loadComplete: State<Boolean>
    )

    @Composable
    fun largeImage(resId: Int, maxW: Dp, maxH: Dp): LargeImageState {
        val ctx = LocalContext.current
        val livecycle = LocalLifecycleOwner.current

        val default = ImageBitmap.imageResource(id = R.drawable.default_image)
        return remember(resId) {
            val image = mutableStateOf(default)
            val loadComplete = mutableStateOf(false)

            livecycle.lifecycleScope.launch(Dispatchers.IO) {
                val cached = cache.get("res-$resId")
                if (cached == null) {
                    val density = ctx.resources.displayMetrics.density
                    val bitmap = decodeSampledBitmapFromResource(
                        ctx.resources,
                        resId = resId,
                        (maxW.value * density).toInt(),
                        (maxH.value * density).toInt()
                    )
                    launch(Dispatchers.Main) {
                        cache.put("res-$resId", bitmap)
                        image.value = bitmap.asImageBitmap()
                        loadComplete.value = true
                    }
                } else {
                    launch(Dispatchers.Main) {
                        image.value = cached.asImageBitmap()
                        loadComplete.value = true
                    }
                }

            }
            LargeImageState(image, loadComplete)
        }
    }

    fun decodeSampledBitmapFromResource(
        res: Resources,
        resId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeResource(res, resId, this)
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

}