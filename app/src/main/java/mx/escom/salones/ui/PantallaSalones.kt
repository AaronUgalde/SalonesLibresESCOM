package mx.escom.salones.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.escom.salones.R
import mx.escom.salones.data.Clase
import mx.escom.salones.domain.Croquis
import mx.escom.salones.domain.Estado
import mx.escom.salones.domain.PRIMER_MINUTO
import mx.escom.salones.domain.ULTIMO_MINUTO
import mx.escom.salones.domain.formatoHora
import kotlin.math.roundToInt

private val DIAS = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
private val DIAS_LARGOS = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
private val PISOS_CORTOS = listOf("PB", "1", "2", "3")

private fun nombrePiso(salon: String): String =
    when (val p = salon.getOrNull(1)?.digitToIntOrNull()) {
        0 -> "Planta baja"
        null -> "?"
        else -> "Piso $p"
    }

private fun ubicacion(salon: String) = "Edificio ${salon.first()} · ${nombrePiso(salon)}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSalones(vm: SalonesViewModel = viewModel()) {
    val p = LocalPaleta.current
    val ocupacion = vm.ocupacion
    val dia = vm.dia
    val minuto = vm.minuto
    val resumen = remember(dia, minuto) { ocupacion.resumen(dia, minuto) }
    val conHorario = ocupacion.salonesConDatos.size
    var verControles by rememberSaveable { mutableStateOf(false) }
    var verInfo by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(p.fondo)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Encabezado(onInfo = { verInfo = true }) }
            item {
                Text(
                    "¿Dónde hacemos\ntarea hoy?",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            item { Buscador(onSalon = vm::seleccionar, onAhora = vm::irAAhora) }
            item {
                Heroe(
                    libres = resumen.libres.size,
                    total = conHorario,
                    etiqueta = "${DIAS_LARGOS[dia]} · ${formatoHora(minuto)}",
                    abierto = verControles,
                    onAlternar = { verControles = !verControles },
                )
            }
            item {
                AnimatedVisibility(visible = verControles) {
                    Controles(
                        dia = dia,
                        minuto = minuto,
                        onDia = { vm.dia = it },
                        onMinuto = { vm.minuto = it },
                        onMover = vm::moverMinutos,
                        onAhora = vm::irAAhora,
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TarjetaDato("Con clase", resumen.ocupados, conHorario, Colores.ocupado, Modifier.weight(1f))
                    TarjetaDato("Probable", resumen.probables, conHorario, Colores.probable, Modifier.weight(1f))
                    TarjetaDato("En lab", resumen.enLab.size, conHorario, Colores.enLab, Modifier.weight(1f))
                }
            }
            item {
                TarjetaMapa(
                    piso = vm.piso,
                    estadoDe = { ocupacion.estado(it, dia, minuto) },
                    seleccionado = vm.seleccionado,
                    onSalon = vm::seleccionar,
                )
            }
            item {
                Column(Modifier.padding(top = 8.dp)) {
                    Text("Libres a esta hora", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        if (resumen.libres.isEmpty() && resumen.enLab.isEmpty()) "Ninguno. Prueba otra hora."
                        else "${resumen.libres.size + resumen.enLab.size} salones · toca uno para ver su horario",
                        color = p.tenue,
                        fontSize = 15.sp,
                    )
                }
            }
            items(resumen.libres, key = { "l" + it.first }) { (salon, hasta) ->
                FilaLibre(salon, hasta, enLab = false, onClick = { vm.seleccionar(salon) })
            }
            items(resumen.enLab, key = { "b" + it.first }) { (salon, hasta) ->
                FilaLibre(salon, hasta, enLab = true, onClick = { vm.seleccionar(salon) })
            }
            item {
                Text(
                    "Datos: ${vm.horarios.fuente} (${vm.horarios.capturado.take(10)}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = p.tenue,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        BarraPisos(
            piso = vm.piso,
            onPiso = { vm.piso = it },
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 12.dp),
        )
    }

    if (verInfo) {
        AlertDialog(
            onDismissRequest = { verInfo = false },
            confirmButton = { TextButton(onClick = { verInfo = false }) { Text("Entendido") } },
            title = { Text("Sobre los datos") },
            text = {
                Text(
                    "Horarios del SAES y de las hojas pegadas afuera de cada salón (${vm.horarios.capturado.take(10)}).\n\n" +
                        "\"Libre\" solo significa que no hay clase registrada: puede haber asesorías o cambios. " +
                        "\"Probable\" es un salón estimado con la Distribución 26-1. " +
                        "\"En lab\" es una sesión marcada con L: el grupo debería estar en el laboratorio, pero a veces se queda.\n\n" +
                        "La posición de los salones en el croquis es aproximada.",
                )
            },
            containerColor = p.tarjeta,
        )
    }

    vm.seleccionado?.let { salon ->
        ModalBottomSheet(
            onDismissRequest = { vm.seleccionado = null },
            containerColor = p.tarjeta,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            DetalleSalon(
                salon = salon,
                dia = dia,
                minuto = minuto,
                estado = ocupacion.estado(salon, dia, minuto),
                clases = ocupacion.clasesDelDia(salon, dia),
            )
        }
    }
}

/* ---------- Piezas básicas ---------- */

@Composable
private fun BotonRedondo(icono: Int, descripcion: String, onClick: () -> Unit, tamano: Dp = 56.dp) {
    val p = LocalPaleta.current
    Box(
        Modifier.size(tamano).clip(CircleShape).background(p.tarjeta).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icono), contentDescription = descripcion, tint = p.tinta, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun Pastilla(texto: String, fondo: Color, modifier: Modifier = Modifier) {
    Box(
        modifier.clip(CircleShape).background(fondo).padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(texto, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = LocalPaleta.current.tinta, maxLines = 1)
    }
}

/** Barra de avance: parte llena en verde y el resto rayado, como en el diseño de referencia. */
@Composable
private fun BarraRayada(fraccion: Float, modifier: Modifier = Modifier) {
    val p = LocalPaleta.current
    Canvas(modifier.fillMaxWidth().height(22.dp)) {
        val r = CornerRadius(size.height / 2)
        val hueco = 8.dp.toPx()
        val f = fraccion.coerceIn(0f, 1f)
        val lleno = (size.width - hueco) * f
        if (f > 0f) drawRoundRect(p.acento, size = Size(lleno.coerceAtLeast(size.height), size.height), cornerRadius = r)
        val x0 = if (f > 0f) lleno + hueco else 0f
        val resto = size.width - x0
        if (resto > size.height / 2) {
            val forma = Path().apply { addRoundRect(RoundRect(x0, 0f, size.width, size.height, r)) }
            clipPath(forma) {
                drawRect(p.pista, Offset(x0, 0f), Size(resto, size.height))
                var x = x0 - size.height
                while (x < size.width) {
                    drawLine(p.raya, Offset(x, size.height), Offset(x + size.height, 0f), 1.5.dp.toPx())
                    x += 5.dp.toPx()
                }
            }
            drawRoundRect(p.raya, Offset(x0, 0f), Size(resto, size.height), r, style = Stroke(1.dp.toPx()))
        }
    }
}

/* ---------- Pantalla principal ---------- */

@Composable
private fun Encabezado(onInfo: () -> Unit) {
    val p = LocalPaleta.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(p.acento),
            contentAlignment = Alignment.Center,
        ) {
            Text("E", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("¡Hola!", fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Text("ESCOM · Zacatenco", fontSize = 16.sp, color = p.tenue)
        }
        BotonRedondo(R.drawable.ic_info, "Sobre los datos", onInfo)
    }
}

@Composable
private fun Buscador(onSalon: (String) -> Unit, onAhora: () -> Unit) {
    val p = LocalPaleta.current
    val foco = LocalFocusManager.current
    var texto by rememberSaveable { mutableStateOf("") }
    val noExiste = texto.length == 4 && texto !in Croquis.todosLosSalones

    fun abrir() {
        if (texto in Croquis.todosLosSalones) {
            onSalon(texto)
            texto = ""
            foco.clearFocus()
        }
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.weight(1f).height(60.dp).clip(CircleShape).background(p.tarjeta).padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_buscar), null, tint = p.tinta, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f)) {
                    if (texto.isEmpty()) Text("Buscar salón (ej. 1210)", color = p.tenue, fontSize = 17.sp)
                    BasicTextField(
                        value = texto,
                        onValueChange = { nuevo ->
                            texto = nuevo.filter(Char::isDigit).take(4)
                            if (texto.length == 4) abrir()
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = p.tinta, fontSize = 17.sp),
                        cursorBrush = SolidColor(p.tinta),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { abrir() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            BotonRedondo(R.drawable.ic_reloj, "Ir a la hora actual", onAhora, tamano = 60.dp)
        }
        if (noExiste) {
            Text(
                "El salón $texto no está en el croquis.",
                color = Colores.ocupado,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 20.dp, top = 6.dp),
            )
        }
    }
}

@Composable
private fun Heroe(libres: Int, total: Int, etiqueta: String, abierto: Boolean, onAlternar: () -> Unit) {
    val p = LocalPaleta.current
    Column(Modifier.padding(top = 4.dp)) {
        Text(etiqueta, fontSize = 20.sp, color = p.tenue)
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$libres", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.width(14.dp))
            Text(
                if (libres == 1) "salón libre" else "salones libres",
                fontSize = 30.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.8).sp,
                modifier = Modifier.padding(bottom = 14.dp),
            )
        }
        Text("de $total con horario", fontSize = 17.sp, color = p.tenue)
        Spacer(Modifier.height(12.dp))
        BarraRayada(if (total == 0) 0f else libres.toFloat() / total)
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .border(BorderStroke(1.dp, p.raya), CircleShape)
                .clickable(onClick = onAlternar)
                .padding(start = 18.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Cambiar día y hora", fontSize = 15.sp)
            Icon(
                painterResource(R.drawable.ic_flecha_abajo),
                null,
                tint = p.tinta,
                modifier = Modifier.size(22.dp).rotate(if (abierto) 180f else 0f),
            )
        }
    }
}

@Composable
private fun Controles(
    dia: Int,
    minuto: Int,
    onDia: (Int) -> Unit,
    onMinuto: (Int) -> Unit,
    onMover: (Int) -> Unit,
    onAhora: () -> Unit,
) {
    val p = LocalPaleta.current
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(p.tarjeta).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DIAS.forEachIndexed { i, nombre ->
                val elegido = i == dia
                Box(
                    Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(if (elegido) p.barraOscura else p.pista)
                        .clickable { onDia(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        nombre,
                        color = if (elegido) Color.White else p.tinta,
                        fontSize = 14.sp,
                        fontWeight = if (elegido) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatoHora(minuto), fontSize = 36.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp)
            Spacer(Modifier.weight(1f))
            CirculoTexto("−15") { onMover(-15) }
            Spacer(Modifier.width(8.dp))
            CirculoTexto("+15") { onMover(15) }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.height(48.dp).clip(CircleShape).background(p.barraOscura).clickable(onClick = onAhora)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Ahora", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
        Slider(
            value = minuto.toFloat(),
            onValueChange = { onMinuto((it / 15f).roundToInt() * 15) },
            valueRange = PRIMER_MINUTO.toFloat()..ULTIMO_MINUTO.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = p.tinta,
                activeTrackColor = p.acento,
                inactiveTrackColor = p.pista,
            ),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("7:00", "10:30", "13:30", "16:30", "20:00", "21:30").forEach {
                Text(it, fontSize = 12.sp, color = p.tenue)
            }
        }
    }
}

@Composable
private fun CirculoTexto(texto: String, onClick: () -> Unit) {
    val p = LocalPaleta.current
    Box(
        Modifier.size(48.dp).clip(CircleShape).border(1.dp, p.raya, CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(texto, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TarjetaDato(titulo: String, valor: Int, total: Int, color: Color, modifier: Modifier = Modifier) {
    val p = LocalPaleta.current
    Column(
        modifier.clip(RoundedCornerShape(24.dp)).background(p.tarjeta).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$valor", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(" / $total", fontSize = 15.sp, color = p.tenue)
        }
        Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(p.pista)) {
            val f = if (total == 0) 0f else valor.toFloat() / total
            if (f > 0f) {
                Box(Modifier.fillMaxWidth(f.coerceAtLeast(0.08f)).fillMaxHeight().clip(CircleShape).background(color))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaMapa(
    piso: Int,
    estadoDe: (String) -> Estado,
    seleccionado: String?,
    onSalon: (String) -> Unit,
) {
    val p = LocalPaleta.current
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(p.tarjeta).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)) {
            Text("Croquis", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(10.dp))
            Pastilla(Croquis.PISOS[piso], p.chipVerde)
        }
        MapaCroquis(piso = piso, estadoDe = estadoDe, seleccionado = seleccionado, onSalon = onSalon)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 6.dp, top = 10.dp),
        ) {
            listOf(
                Colores.libre to "Libre",
                Colores.enLab to "Grupo en lab",
                Colores.ocupado to "Con clase",
                Colores.probable to "Probable",
                Colores.sinDatos to "Sin datos",
            ).forEach { (color, texto) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(5.dp))
                    Text(texto, fontSize = 13.sp, color = p.tenue)
                }
            }
        }
        Text(
            "Posiciones aproximadas.",
            fontSize = 12.sp,
            color = p.tenue,
            modifier = Modifier.padding(start = 6.dp, top = 4.dp),
        )
    }
}

@Composable
private fun FilaLibre(salon: String, hasta: Int?, enLab: Boolean, onClick: () -> Unit) {
    val p = LocalPaleta.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(p.tarjeta)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(52.dp).clip(CircleShape).background(if (enLab) p.chipAzul else p.chipVerde),
            contentAlignment = Alignment.Center,
        ) {
            Text("E${salon.first()}", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(salon, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text(
                if (enLab) "${nombrePiso(salon)} · grupo en lab" else nombrePiso(salon),
                fontSize = 14.sp,
                color = p.tenue,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(if (hasta == null) "libre" else "hasta", fontSize = 13.sp, color = p.tenue)
            Text(
                hasta?.let(::formatoHora) ?: "todo el día",
                fontSize = if (hasta == null) 17.sp else 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(painterResource(R.drawable.ic_flecha_derecha), null, tint = p.tenue, modifier = Modifier.size(22.dp))
    }
}

/** Barra flotante oscura para cambiar de piso. */
@Composable
private fun BarraPisos(piso: Int, onPiso: (Int) -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPaleta.current
    Row(
        modifier.clip(CircleShape).background(p.barraOscura).padding(7.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Croquis.PISOS.forEachIndexed { i, nombre ->
            if (i == piso) {
                Row(
                    Modifier.height(54.dp).clip(CircleShape).background(Color.White).padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.ic_capas), null, tint = Color(0xFF1E2A4A), modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(nombre, color = Color(0xFF111418), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            } else {
                Box(
                    Modifier.size(54.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)).clickable { onPiso(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(PISOS_CORTOS[i], color = Color.White.copy(alpha = 0.75f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/* ---------- Hoja de detalle ---------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetalleSalon(salon: String, dia: Int, minuto: Int, estado: Estado, clases: List<Clase>) {
    val p = LocalPaleta.current
    val (frase, colorFrase) = when (estado) {
        is Estado.Libre -> (estado.hasta?.let { "Libre hasta las ${formatoHora(it)}" } ?: "Libre el resto del día") to Colores.libre
        is Estado.GrupoEnLab ->
            "El ${estado.clase.grupo} debería estar en laboratorio hasta las ${formatoHora(estado.clase.fin)}; a veces se quedan" to Colores.enLab
        is Estado.Ocupado -> "Con clase: ${estado.clase.grupo}" to Colores.ocupado
        is Estado.Probable -> "Probablemente con clase: ${estado.clase.grupo} (salón estimado)" to Colores.probable
        Estado.SinDatos -> "No aparece en ningún horario" to p.tenue
    }
    val (cambio, etiquetaCambio) = when (estado) {
        is Estado.Libre -> (estado.hasta?.let(::formatoHora) ?: "—") to "libre hasta"
        is Estado.GrupoEnLab -> formatoHora(estado.clase.fin) to "regresan"
        is Estado.Ocupado -> formatoHora(estado.clase.fin) to "termina"
        is Estado.Probable -> formatoHora(estado.clase.fin) to "termina"
        Estado.SinDatos -> "—" to "sin datos"
    }
    val (chipEstado, colorChip) = when (estado) {
        is Estado.Libre -> "Libre" to p.chipVerde
        is Estado.GrupoEnLab -> "Grupo en lab" to p.chipAzul
        is Estado.Ocupado -> "Con clase" to p.chipRosa
        is Estado.Probable -> "Probable" to p.chipAmbar
        Estado.SinDatos -> "Sin datos" to p.pista
    }
    val clasesValidas = clases.count { it.estimado < 2 && !it.laboratorio }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text("Salón $salon", style = MaterialTheme.typography.headlineLarge)
                Text("${DIAS_LARGOS[dia]} ${formatoHora(minuto)} · $frase", color = colorFrase, fontSize = 16.sp)
            }
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(28.dp))
                    .background(p.acentoSuave)
                    .padding(vertical = 16.dp),
            ) {
                val datos = listOf(
                    cambio to etiquetaCambio,
                    "$clasesValidas" to "clases hoy",
                    "${salon.first()}" to "edificio",
                    (if (salon[1] == '0') "PB" else "${salon[1]}") to "piso",
                )
                datos.forEachIndexed { i, (valor, etiqueta) ->
                    if (i > 0) Box(Modifier.width(1.dp).fillMaxHeight().padding(vertical = 6.dp).background(p.raya))
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(valor, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp)
                        Text(etiqueta, fontSize = 14.sp, color = p.tenue, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Pastilla(chipEstado, colorChip)
                Pastilla("Edificio ${salon.first()}", p.chipAzul)
                Pastilla(nombrePiso(salon), p.chipRosa)
            }
        }
        item {
            Column(Modifier.padding(top = 6.dp)) {
                Text("Horario", style = MaterialTheme.typography.headlineMedium)
                Text(
                    if (clases.isEmpty()) "Sin clases registradas el ${DIAS_LARGOS[dia].lowercase()}."
                    else "${clases.size} sesiones el ${DIAS_LARGOS[dia].lowercase()} · ${ubicacion(salon)}",
                    color = p.tenue,
                    fontSize = 15.sp,
                )
            }
        }
        items(clases) { c -> FilaClase(c, minuto) }
    }
}

@Composable
private fun FilaClase(c: Clase, minuto: Int) {
    val p = LocalPaleta.current
    val ahora = c.estimado < 2 && c.ini <= minuto && minuto < c.fin
    val fondo = when {
        !ahora -> p.pista
        c.laboratorio -> p.chipAzul
        c.estimado == 1 -> p.chipAmbar
        else -> p.chipRosa
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(fondo).padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.width(64.dp)) {
            Text(formatoHora(c.ini), fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
            Text(formatoHora(c.fin), fontSize = 14.sp, color = p.tenue, maxLines = 1, softWrap = false)
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(c.materia, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                listOf(c.grupo, c.profesor).filter(String::isNotBlank).joinToString(" · "),
                fontSize = 14.sp,
                color = p.tenue,
            )
            val notas = buildList {
                if (c.laboratorio) add("en laboratorio: el salón queda libre")
                if (c.estimado == 1) add("salón estimado")
                if (c.estimado == 2) add("choca con un dato confirmado: se ignora")
            }
            if (notas.isNotEmpty()) {
                Text(notas.joinToString(" · "), fontSize = 13.sp, color = Colores.probable, fontWeight = FontWeight.Medium)
            }
        }
    }
}
