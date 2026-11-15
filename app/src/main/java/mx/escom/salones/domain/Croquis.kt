package mx.escom.salones.domain

/**
 * Croquis esquemático de la ESCOM en un espacio virtual de [ANCHO] x [ALTO] (norte arriba).
 * Supuestos pendientes de confirmar: reparto 4-3-4-3 de salones por bloque en E1/E2 y numeración sur→norte.
 * Salón ABCD: A = edificio, B = piso (0 = planta baja), CD = número.
 */
data class Caja(val x: Float, val y: Float, val w: Float, val h: Float) {
    fun contiene(px: Float, py: Float) = px >= x && px <= x + w && py >= y && py <= y + h
    val cx get() = x + w / 2
    val cy get() = y + h / 2
}

data class SalonCaja(val codigo: String, val caja: Caja)

/** [rotacion] en grados (0, 90 o -90). */
data class Etiqueta(val texto: String, val x: Float, val y: Float, val rotacion: Float = 0f, val tamano: Float = 9f)

data class PlanoPiso(
    val salones: List<SalonCaja>,
    val pasillos: List<Caja>,
    val etiquetas: List<Etiqueta>,
)

object Croquis {
    const val ANCHO = 360f
    const val ALTO = 500f
    val BLOQUES = listOf(4, 3, 4, 3)
    val PISOS = listOf("PB", "Piso 1", "Piso 2", "Piso 3")

    val labs = Caja(128f, 215f, 106f, 120f)
    val letras = Caja(128f, 360f, 106f, 22f)
    val gestion = Caja(140f, 398f, 82f, 30f)

    private fun codigo(edificio: Int, piso: Int, numero: Int) =
        "$edificio$piso${numero.toString().padStart(2, '0')}"

    private class Constructor {
        val salones = mutableListOf<SalonCaja>()
        val pasillos = mutableListOf<Caja>()
        val etiquetas = mutableListOf<Etiqueta>()

        /** Edificios 1 y 2: salones 01-14 en 4 bloques, numerados de sur (abajo) a norte. */
        fun edificioLargo(num: Int, piso: Int, xSalones: Float, xPasillo: Float, xNombre: Float) {
            val y0 = 40f
            val y1 = 430f
            val gap = 14f
            val h = (y1 - y0 - gap * (BLOQUES.size - 1)) / BLOQUES.sum()
            var y = y1
            var r = 1
            for (cantidad in BLOQUES) {
                pasillos += Caja(xPasillo, y - cantidad * h, 18f, cantidad * h)
                repeat(cantidad) {
                    y -= h
                    salones += SalonCaja(codigo(num, piso, r), Caja(xSalones, y, 42f, h))
                    r++
                }
                y -= gap
            }
            etiquetas += Etiqueta("EDIFICIO $num", xNombre, (y0 + y1) / 2, if (num == 2) -90f else 90f, 10f)
        }

        /** Edificios 3 y 4: salones 08-13, de sur a norte. */
        fun edificioCorto(num: Int, piso: Int, xSalones: Float, xPasillo: Float) {
            val y0 = 70f
            val y1 = 190f
            val numeros = 8..13
            val h = (y1 - y0) / numeros.count()
            pasillos += Caja(xPasillo, y0, 12f, y1 - y0)
            numeros.forEachIndexed { i, r ->
                salones += SalonCaja(codigo(num, piso, r), Caja(xSalones, y1 - (i + 1) * h, 38f, h))
            }
            val centro = (minOf(xSalones, xPasillo) + maxOf(xSalones + 38f, xPasillo + 12f)) / 2
            etiquetas += Etiqueta("EDIF. $num", centro, y0 - 9f)
        }
    }

    fun piso(piso: Int): PlanoPiso {
        val c = Constructor()
        if (piso == 3) {
            c.edificioCorto(3, 3, xSalones = 196f, xPasillo = 184f)
        } else {
            c.edificioCorto(4, piso, xSalones = 128f, xPasillo = 166f)
            c.edificioCorto(3, piso, xSalones = 196f, xPasillo = 184f)
            c.edificioLargo(2, piso, xSalones = 36f, xPasillo = 78f, xNombre = 20f)
            c.edificioLargo(1, piso, xSalones = 284f, xPasillo = 266f, xNombre = 340f)
        }
        return PlanoPiso(c.salones, c.pasillos, c.etiquetas)
    }

    val todosLosSalones: Set<String> by lazy {
        (0..3).flatMap { p -> piso(p).salones.map { it.codigo } }.toSet()
    }
}
