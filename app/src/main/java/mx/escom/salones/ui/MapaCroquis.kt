package mx.escom.salones.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import mx.escom.salones.domain.Caja
import mx.escom.salones.domain.Croquis
import mx.escom.salones.domain.Estado

@Composable
fun MapaCroquis(
    piso: Int,
    estadoDe: (String) -> Estado,
    seleccionado: String?,
    onSalon: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val plano = remember(piso) { Croquis.piso(piso) }
    val medidor = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(Croquis.ANCHO / Croquis.ALTO)
            .pointerInput(plano) {
                detectTapGestures { toque ->
                    val s = size.width / Croquis.ANCHO
                    plano.salones
                        .firstOrNull { it.caja.contiene(toque.x / s, toque.y / s) }
                        ?.let { onSalon(it.codigo) }
                }
            },
    ) {
        val s = size.width / Croquis.ANCHO
        scale(s, pivot = Offset.Zero) {
            // Fondo claro propio: el croquis usa colores fijos y debe leerse igual en modo oscuro.
            drawRoundRect(Colores.papel, size = Size(Croquis.ANCHO, Croquis.ALTO), cornerRadius = CornerRadius(8f))
            texto(medidor, "▲ Norte · lado metro Politécnico", Croquis.ANCHO / 2, 12f, 9f, Colores.tenue)

            for (p in plano.pasillos) rect(p, Colores.pasillo, 0.5f)

            for (salon in plano.salones) {
                val estado = estadoDe(salon.codigo)
                rect(salon.caja, Colores.de(estado), 1f)
                if (salon.codigo == seleccionado) {
                    drawRect(
                        Colores.seleccion,
                        Offset(salon.caja.x, salon.caja.y),
                        Size(salon.caja.w, salon.caja.h),
                        style = Stroke(width = 3f),
                    )
                }
                val sinDatos = estado == Estado.SinDatos
                texto(
                    medidor, salon.codigo, salon.caja.cx, salon.caja.cy, 9f,
                    if (sinDatos) Colores.sinDatosTexto else Color.White,
                    negrita = !sinDatos,
                )
            }

            for (e in plano.etiquetas) {
                texto(medidor, e.texto, e.x, e.y, e.tamano, Colores.tinta, negrita = true, rotacion = e.rotacion)
            }

            rect(Croquis.labs, Colores.edificio, 1f)
            texto(medidor, "EDIFICIO", Croquis.labs.cx, Croquis.labs.cy - 7f, 10f, Colores.tinta, negrita = true)
            texto(medidor, "DE LABS", Croquis.labs.cx, Croquis.labs.cy + 7f, 10f, Colores.tinta, negrita = true)
            rect(Croquis.letras, Colores.letras, 1f)
            texto(medidor, "LAS LETRAS", Croquis.letras.cx, Croquis.letras.cy, 9f, Colores.tinta, negrita = true)
            rect(Croquis.gestion, Colores.edificio, 1f)
            texto(medidor, "GESTIÓN", Croquis.gestion.cx, Croquis.gestion.cy, 9f, Colores.tinta, negrita = true)
            texto(medidor, "▼ Sur · Zacatenco / Las Letras", Croquis.ANCHO / 2, 480f, 9f, Colores.tenue)
        }
    }
}

private fun DrawScope.rect(caja: Caja, color: Color, borde: Float) {
    drawRect(color, Offset(caja.x, caja.y), Size(caja.w, caja.h))
    drawRect(Colores.linea, Offset(caja.x, caja.y), Size(caja.w, caja.h), style = Stroke(width = borde))
}

/**
 * Dibuja texto centrado en (x, y) en coordenadas del croquis.
 * Se mide en píxeles "virtuales" (tamaño / densidad) porque el DrawScope ya está escalado.
 */
private fun DrawScope.texto(
    medidor: TextMeasurer,
    texto: String,
    x: Float,
    y: Float,
    tamano: Float,
    color: Color,
    negrita: Boolean = false,
    rotacion: Float = 0f,
) {
    val estilo = TextStyle(
        color = color,
        fontSize = (tamano / (density * fontScale)).sp,
        fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
    )
    val medida = medidor.measure(texto, estilo)
    val esquina = Offset(x - medida.size.width / 2f, y - medida.size.height / 2f)
    if (rotacion == 0f) {
        drawText(medida, topLeft = esquina)
    } else {
        rotate(rotacion, pivot = Offset(x, y)) { drawText(medida, topLeft = esquina) }
    }
}
