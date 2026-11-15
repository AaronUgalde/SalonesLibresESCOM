package mx.escom.salones.data

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Una sesión de clase en un salón.
 * @param dia 0 = lunes … 5 = sábado
 * @param ini/fin minutos desde la medianoche (7:00 = 420)
 * @param estimado 0 = confirmado (SAES o foto), 1 = salón estimado con la distribución 26-1,
 *                 2 = estimado que choca con un dato confirmado (se ignora)
 * @param laboratorio la sesión está marcada con "L": el grupo debería estar en el laboratorio
 */
data class Clase(
    val salon: String,
    val dia: Int,
    val ini: Int,
    val fin: Int,
    val grupo: String,
    val materia: String,
    val profesor: String,
    val estimado: Int = 0,
    val laboratorio: Boolean = false,
)

data class Horarios(
    val capturado: String,
    val fuente: String,
    val clases: List<Clase>,
)

object Repositorio {

    fun cargar(context: Context): Horarios {
        val texto = context.assets.open("data.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        return parsear(texto)
    }

    /** Formato: {"capturado": "...", "fuente": "...", "clases": [[salon, dia, ini, fin, grupo, materia, prof, est, lab], ...]} */
    fun parsear(texto: String): Horarios {
        val raiz = Json.parseToJsonElement(texto).jsonObject
        val clases = raiz.getValue("clases").jsonArray.map { elemento ->
            val a = elemento.jsonArray
            Clase(
                salon = a[0].jsonPrimitive.content,
                dia = a[1].jsonPrimitive.int,
                ini = a[2].jsonPrimitive.int,
                fin = a[3].jsonPrimitive.int,
                grupo = a[4].jsonPrimitive.content,
                materia = a[5].jsonPrimitive.content,
                profesor = a[6].jsonPrimitive.content,
                estimado = a.getOrNull(7)?.jsonPrimitive?.int ?: 0,
                laboratorio = (a.getOrNull(8)?.jsonPrimitive?.int ?: 0) == 1,
            )
        }
        return Horarios(
            capturado = raiz["capturado"]?.jsonPrimitive?.content.orEmpty(),
            fuente = raiz["fuente"]?.jsonPrimitive?.content.orEmpty(),
            clases = clases,
        )
    }
}
