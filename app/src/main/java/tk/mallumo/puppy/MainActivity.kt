package tk.mallumo.puppy

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import tk.mallumo.compose.navigation.ItemsUI
import tk.mallumo.compose.navigation.NavigationContent
import tk.mallumo.compose.navigation.Node
import tk.mallumo.puppy.ui.theme.PuppyTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PuppyTheme {
                NavigationContent(startupNode = Node.ItemsUI)
            }
        }
    }
}
