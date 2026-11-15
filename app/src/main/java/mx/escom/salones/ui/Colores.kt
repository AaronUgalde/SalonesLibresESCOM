package mx.escom.salones.ui

import androidx.compose.ui.graphics.Color
import mx.escom.salones.domain.Estado

object Colores {
    val libre = Color(0xFF16A34A)
    val enLab = Color(0xFF0D9488)
    val ocupado = Color(0xFFDC2626)
    val probable = Color(0xFFD97706)
    val sinDatos = Color(0xFFE5E7EB)
    val sinDatosTexto = Color(0xFF6B7280)
    val papel = Color(0xFFFFFFFF)
    val pasillo = Color(0xFFF1F5F9)
    val edificio = Color(0xFFCBD5E1)
    val linea = Color(0xFF94A3B8)
    val letras = Color(0xFFFDE68A)
    val seleccion = Color(0xFF2563EB)
    val tinta = Color(0xFF0F172A)
    val tenue = Color(0xFF64748B)

    fun de(estado: Estado): Color = when (estado) {
        is Estado.Libre -> libre
        is Estado.GrupoEnLab -> enLab
        is Estado.Ocupado -> ocupado
        is Estado.Probable -> probable
        Estado.SinDatos -> sinDatos
    }
}
