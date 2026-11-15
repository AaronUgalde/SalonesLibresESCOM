package mx.escom.salones.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Colores de la interfaz (fondo menta, tarjetas blancas, barra inferior oscura, chips pastel). */
data class Paleta(
    val fondo: Color,
    val tarjeta: Color,
    val tinta: Color,
    val tenue: Color,
    val acento: Color,
    val acentoSuave: Color,
    val barraOscura: Color,
    val pista: Color,
    val raya: Color,
    val chipVerde: Color,
    val chipRosa: Color,
    val chipAzul: Color,
    val chipAmbar: Color,
)

private val PaletaClara = Paleta(
    fondo = Color(0xFFDFEAE2),
    tarjeta = Color(0xFFFFFFFF),
    tinta = Color(0xFF111418),
    tenue = Color(0xFF5B6670),
    acento = Color(0xFF3E9B12),
    acentoSuave = Color(0xFFD3E8C8),
    barraOscura = Color(0xFF151515),
    pista = Color(0xFFEEF2EF),
    raya = Color(0xFFC9D3CC),
    chipVerde = Color(0xFFDDEFD4),
    chipRosa = Color(0xFFFBDDE5),
    chipAzul = Color(0xFFDCEBF7),
    chipAmbar = Color(0xFFFBEBCF),
)

private val PaletaOscura = Paleta(
    fondo = Color(0xFF0F1612),
    tarjeta = Color(0xFF1A231E),
    tinta = Color(0xFFEAF2EC),
    tenue = Color(0xFF9AA8A0),
    acento = Color(0xFF6CC93F),
    acentoSuave = Color(0xFF233A22),
    barraOscura = Color(0xFF000000),
    pista = Color(0xFF26302A),
    raya = Color(0xFF3A463F),
    chipVerde = Color(0xFF233A22),
    chipRosa = Color(0xFF41242F),
    chipAzul = Color(0xFF1F3242),
    chipAmbar = Color(0xFF3D321E),
)

val LocalPaleta = staticCompositionLocalOf { PaletaClara }

private val Tipografia = Typography().let { t ->
    t.copy(
        displayLarge = TextStyle(fontSize = 96.sp, lineHeight = 96.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-4).sp),
        headlineLarge = TextStyle(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp),
        headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.8).sp),
        titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
    )
}

@Composable
fun TemaSalones(contenido: @Composable () -> Unit) {
    val oscuro = isSystemInDarkTheme()
    val p = if (oscuro) PaletaOscura else PaletaClara
    val base = if (oscuro) darkColorScheme() else lightColorScheme()
    val esquema = base.copy(
        primary = p.tinta,
        onPrimary = p.fondo,
        background = p.fondo,
        onBackground = p.tinta,
        surface = p.tarjeta,
        onSurface = p.tinta,
        onSurfaceVariant = p.tenue,
        surfaceContainerLow = p.tarjeta,
        outline = p.raya,
        outlineVariant = p.pista,
    )
    CompositionLocalProvider(LocalPaleta provides p) {
        MaterialTheme(colorScheme = esquema, typography = Tipografia) {
            // Sin Surface raíz: el color de texto por defecto se fija aquí para que funcione en modo oscuro.
            CompositionLocalProvider(LocalContentColor provides p.tinta, content = contenido)
        }
    }
}
