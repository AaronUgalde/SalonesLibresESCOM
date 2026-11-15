package mx.escom.salones.domain

import mx.escom.salones.data.Clase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OcupacionTest {

    private fun clase(salon: String, ini: Int, fin: Int, est: Int = 0, lab: Boolean = false, grupo: String = "1AM1") =
        Clase(salon, dia = 0, ini = ini, fin = fin, grupo = grupo, materia = "M", profesor = "P", estimado = est, laboratorio = lab)

    private val ocupacion = Ocupacion(
        listOf(
            clase("1202", 420, 510),                     // 7:00-8:30 confirmada
            clase("1202", 630, 720, lab = true),         // 10:30-12:00 en laboratorio
            clase("1202", 720, 810),                     // 12:00-13:30
            clase("1113", 420, 510, est = 1),            // estimada
            clase("1114", 420, 510, est = 2),            // estimada que choca: se ignora
        )
    )

    @Test fun ocupadoConDatoConfirmado() {
        assertTrue(ocupacion.estado("1202", 0, 450) is Estado.Ocupado)
    }

    @Test fun libreHastaLaSiguienteClase() {
        assertEquals(Estado.Libre(630 + 90), ocupacion.estado("1202", 0, 520))
    }

    @Test fun grupoEnLaboratorio() {
        val e = ocupacion.estado("1202", 0, 640)
        assertTrue(e is Estado.GrupoEnLab)
        assertEquals(720, (e as Estado.GrupoEnLab).hasta)
    }

    @Test fun probableConSalonEstimado() {
        assertTrue(ocupacion.estado("1113", 0, 450) is Estado.Probable)
    }

    @Test fun estimadoQueChocaSeIgnora() {
        assertEquals(Estado.SinDatos, ocupacion.estado("1114", 0, 450))
    }

    @Test fun libreTodoElDia() {
        assertEquals(Estado.Libre(null), ocupacion.estado("1202", 1, 600))
    }

    @Test fun croquisTieneSalonesUnicos() {
        val p2 = Croquis.piso(2).salones.map { it.codigo }
        assertEquals(p2.size, p2.toSet().size)
        assertTrue("1214" in p2 && "3213" in p2 && "4208" in p2)
    }
}

class MomentoEscolarTest {
    @Test fun dentroDelHorarioNoCambia() {
        assertEquals(2 to 600, momentoEscolar(3, 600))
    }

    @Test fun madrugadaYNocheSeAjustanAlRango() {
        assertEquals(0 to PRIMER_MINUTO, momentoEscolar(1, 180))
        assertEquals(4 to ULTIMO_MINUTO, momentoEscolar(5, 23 * 60))
    }

    @Test fun domingoSaltaAlLunesTemprano() {
        assertEquals(0 to PRIMER_MINUTO, momentoEscolar(7, 15 * 60))
    }
}
