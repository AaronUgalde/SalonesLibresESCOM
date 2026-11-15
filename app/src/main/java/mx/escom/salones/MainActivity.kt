package mx.escom.salones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.escom.salones.ui.PantallaSalones
import mx.escom.salones.ui.TemaSalones

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemaSalones {
                PantallaSalones()
            }
        }
    }
}
