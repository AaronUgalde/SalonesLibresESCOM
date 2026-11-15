package mx.escom.salones.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import mx.escom.salones.data.Horarios
import mx.escom.salones.data.Repositorio
import mx.escom.salones.domain.Ocupacion
import mx.escom.salones.domain.PRIMER_MINUTO
import mx.escom.salones.domain.ULTIMO_MINUTO
import mx.escom.salones.domain.momentoEscolar
import java.time.ZoneId
import java.time.ZonedDateTime

class SalonesViewModel(app: Application) : AndroidViewModel(app) {

    val horarios: Horarios = Repositorio.cargar(app)
    val ocupacion = Ocupacion(horarios.clases)

    var dia by mutableIntStateOf(0)
    var minuto by mutableIntStateOf(10 * 60)
    var piso by mutableIntStateOf(2)
    var seleccionado by mutableStateOf<String?>(null)

    init {
        irAAhora()
    }

    fun irAAhora() {
        val ahora = ZonedDateTime.now(ZoneId.of("America/Mexico_City"))
        val (d, m) = momentoEscolar(ahora.dayOfWeek.value, ahora.hour * 60 + ahora.minute)
        dia = d
        minuto = m
    }

    fun moverMinutos(delta: Int) {
        minuto = (minuto + delta).coerceIn(PRIMER_MINUTO, ULTIMO_MINUTO)
    }

    fun seleccionar(salon: String) {
        seleccionado = salon
        salon.getOrNull(1)?.digitToIntOrNull()?.let { piso = it }
    }
}
