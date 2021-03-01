package tk.mallumo.puppy.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tk.mallumo.puppy.R

@Composable
fun SmallFloatingButton(imageVector: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = modifier
            .padding(8.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        color = MaterialTheme.colors.secondary,
        elevation = FloatingActionButtonDefaults.elevation().elevation(interactionSource).value
    ) {
        Image(
            imageVector = imageVector,
            contentDescription = stringResource(id = R.string.up),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSecondary),
            modifier = Modifier.padding(4.dp),
        )
    }
}
