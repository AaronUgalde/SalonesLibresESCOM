package mx.escom.salones.domain

import mx.escom.salones.data.Clase

sealed interface Estado {
    /** Nadie tiene clase. [hasta] = inicio de la siguiente clase del día (null: libre el resto del día). */
    data class Libre(val hasta: Int?) : Estado

    /** El grupo que tiene clase debería estar en el laboratorio, pero a veces se queda en el salón. */
    data class GrupoEnLab(val clase: Clase, val hasta: Int?) : Estado

    /** Hay clase según un dato confirmado (SAES o foto). */
    data class Ocupado(val clase: Clase) : Estado

    /** Hay clase, pero el salón del grupo es estimado. */
    data class Probable(val clase: Clase) : Estado

    /** El salón no aparece en ningún horario (cubículo, oficina, etc.). */
    data object SinDatos : Estado
}

data class Resumen(
    val libres: List<Pair<String, Int?>>,
    val enLab: List<Pair<String, Int?>>,
    val ocupados: Int,
    val probables: Int,
)

class Ocupacion(clases: List<Clase>) {

    private val validas = clases.filter { it.estimado < 2 }
    private val validasPorSalonDia = validas.groupBy { it.salon to it.dia }
    private val todasPorSalonDia = clases.groupBy { it.salon to it.dia }

    val salonesConDatos: Set<String> = validas.map { it.salon }.toSortedSet()

    fun estado(salon: String, dia: Int, minuto: Int): Estado {
        if (salon !in salonesConDatos) return Estado.SinDatos
        val delDia = validasPorSalonDia[salon to dia].orEmpty()
        val enSalon = delDia.filter { !it.laboratorio }
        val ahora = enSalon.filter { it.ini <= minuto && minuto < it.fin }

        ahora.firstOrNull { it.estimado == 0 }?.let { return Estado.Ocupado(it) }
        ahora.firstOrNull()?.let { return Estado.Probable(it) }

        val hasta = enSalon.filter { it.ini > minuto }.minOfOrNull { it.ini }
        delDia.firstOrNull { it.laboratorio && it.ini <= minuto && minuto < it.fin }
            ?.let { return Estado.GrupoEnLab(it, hasta) }
        return Estado.Libre(hasta)
    }

    /** Todas las clases del salón ese día (incluye laboratorio y estimados descartados), ordenadas por hora. */
    fun clasesDelDia(salon: String, dia: Int): List<Clase> =
        todasPorSalonDia[salon to dia].orEmpty().sortedBy { it.ini }

    fun resumen(dia: Int, minuto: Int): Resumen {
        val libres = mutableListOf<Pair<String, Int?>>()
        val enLab = mutableListOf<Pair<String, Int?>>()
        var ocupados = 0
        var probables = 0
        for (salon in salonesConDatos) {
            when (val e = estado(salon, dia, minuto)) {
                is Estado.Libre -> libres += salon to e.hasta
                is Estado.GrupoEnLab -> enLab += salon to e.hasta
                is Estado.Ocupado -> ocupados++
                is Estado.Probable -> probables++
                Estado.SinDatos -> Unit
            }
        }
        return Resumen(libres, enLab, ocupados, probables)
    }
}

fun formatoHora(minuto: Int): String = "%d:%02d".format(minuto / 60, minuto % 60)

/** Rango de horas de clase en la ESCOM (7:00 a 21:30), en minutos desde la medianoche. */
const val PRIMER_MINUTO = 7 * 60
const val ULTIMO_MINUTO = 21 * 60 + 30

/**
 * Lleva un momento real al horario escolar.
 * @param diaSemana 1 = lunes … 7 = domingo (como java.time.DayOfWeek.value)
 * @return (dia 0 = lunes … 5 = sábado, minuto dentro de [PRIMER_MINUTO]..[ULTIMO_MINUTO]).
 * El domingo salta al lunes a primera hora.
 */
fun momentoEscolar(diaSemana: Int, minutoDelDia: Int): Pair<Int, Int> =
    if (diaSemana == 7) 0 to PRIMER_MINUTO
    else (diaSemana - 1) to minutoDelDia.coerceIn(PRIMER_MINUTO, ULTIMO_MINUTO)
