package tk.mallumo.puppy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun LoaderOverlay(background: Color = Color(0x33000000)) {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .clickable(onClick = { })
    )
//    Surface(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(background)
//            .clickable(onClick = { })
//    )
//    {
//        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//    }
}



