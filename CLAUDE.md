# CLAUDE.md — Salones libres · ESCOM

## Qué es
App Android (Kotlin + Jetpack Compose, un solo módulo `:app`) para alumnos de la ESCOM (IPN, Zacatenco).
Muestra en un croquis por piso qué salones están libres a un día/hora para ir a hacer tarea.
Todo funciona offline: los horarios vienen empaquetados en `app/src/main/assets/data.json`.

Estado (2026-11-13): compila, pasan las pruebas y se probó en el emulador `Medium_Phone_API_37.0` (modo claro y oscuro, toque en salón).
Compilar: `./gradlew :app:assembleDebug` y `./gradlew :app:testDebugUnitTest`. En esta Mac no hay Java del sistema:
`export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`. `local.properties` (no versionado) apunta al SDK.
Versiones copiadas del proyecto hermano `../MetroAR-CDMX` (AGP 9.4.1 con Kotlin integrado, Kotlin 2.4.21,
Compose BOM 2026.09.00, compileSdk 37, minSdk 26). Si algo de Gradle falla, compara con ese proyecto, que sí compila.

## Arquitectura
```
app/src/main/java/mx/escom/salones/
  MainActivity.kt            setContent + TemaSalones
  data/Horarios.kt           Clase, Horarios, Repositorio (lee y parsea assets/data.json con kotlinx.serialization)
  domain/Ocupacion.kt        Estado (sealed) + Ocupacion.estado(salon, dia, minuto) + resumen() + formatoHora()
  domain/Croquis.kt          Geometría del croquis en un espacio virtual 360x500 (norte arriba), por piso
  ui/SalonesViewModel.kt     AndroidViewModel: dia, minuto, piso, seleccionado (mutableState); irAAhora() en America/Mexico_City
  ui/Tema.kt                 Paleta (fondo menta, tarjetas blancas, chips pastel, barra oscura; clara y oscura) + tipografía; LocalPaleta
  ui/PantallaSalones.kt      Pantalla única: buscador de salón, número grande de libres + barra rayada, día/hora plegable,
                             tarjetas por estado, croquis, lista de libres, barra flotante de pisos, hoja de detalle
  ui/MapaCroquis.kt          Canvas que dibuja el croquis escalado; detecta toques por salón
  ui/Colores.kt              Colores por estado del salón (mapa, leyenda)
app/src/test/.../OcupacionTest.kt   Pruebas JVM de la lógica de ocupación y del croquis
datos/                        Pipeline en Python que genera data.json (no forma parte del APK)
```
No hay red, base de datos ni DI. Mantenerlo simple salvo que se pida lo contrario.

## Modelo de datos (`data.json`)
`{"capturado": ISO, "fuente": str, "clases": [[salon, dia, ini, fin, grupo, materia, profesor, estimado, laboratorio], ...]}`
- `salon`: código ABCD → A = edificio (1-4), B = piso (0 = planta baja), CD = número. Ej. `1214`.
- `dia`: 0 = lunes … 5 = sábado. `ini`/`fin`: minutos desde medianoche (7:00 = 420).
- `estimado`: 0 = confirmado (SAES o foto), 1 = salón estimado con la Distribución 26-1, 2 = estimado que choca con un dato confirmado (se ignora para ocupación, solo se muestra en el detalle).
- `laboratorio`: 1 = sesión marcada con "L" en la hoja del salón → el grupo debería estar en un laboratorio, el salón queda libre (pero a veces el profe no baja al lab).

## Reglas de ocupación (`Ocupacion.estado`)
Para un salón, día y minuto, ignorando clases con `estimado == 2`:
1. Salón sin ninguna clase → `SinDatos` (cubículo, oficina…).
2. Clase en el salón ahora (no laboratorio) con `estimado == 0` → `Ocupado`; si solo hay estimadas → `Probable`.
3. Si no, y hay una sesión de laboratorio ahora → `GrupoEnLab(hasta)`.
4. Si no → `Libre(hasta)`. `hasta` = inicio de la siguiente clase no-laboratorio del día (null = libre el resto del día).
Colores: libre verde, grupo en lab verde azulado, ocupado rojo, probable ámbar, sin datos gris.

## Croquis (supuestos sin confirmar)
- Edificios 1 (este) y 2 (oeste, espejo del 1): una sola fila de salones 01-14 numerados de sur (Las Letras/Gestión) a norte (metro Politécnico), pasillo por dentro hacia el Edificio de Labs, salones por fuera. Reparto por bloque **4-3-4-3** (sur→norte) = supuesto; `Croquis.BLOQUES`.
- Edificios 3 y 4 (arriba al centro): salones 08-13; 01-07 son laboratorios/cubículos. Numeración sur→norte = supuesto.
- Pisos PB, 1, 2; el Edificio 3 tiene piso 3 (3308, 3313).
- Falta el Edificio de Aulas y Laboratorios inaugurado en 2022 (no aparece en los datos).

## Pipeline de datos (`datos/`)
`python3 datos/build.py` regenera `app/src/main/assets/data.json`. Prioridad de fuentes:
1. `fotos.py` — transcripción de las hojas "HORARIO · SALÓN XXXX" pegadas afuera de cada salón (periodo 26-27/1):
   `FOTOS` (salón → grupo → claves de materia), `LABS` (sesiones con "L"), `EXTRAS` (clases que no están en el SAES, p. ej. Taller de Nivelación).
2. `horarios_saes_2026-11-13.json` — extraído del SAES (saes.escom.ipn.mx/Academica/horarios.aspx, todas las carreras/turnos/semestres, periodo actual). El SAES pone `Edificio="X", Salón="X"` en ~150 clases (casi toda Ing. en IA, grupos B) y **no marca las sesiones de laboratorio**.
3. `mapeo_26_1.py` — Distribución de salones 26-1 (grupo → salón, más excepciones por materia). Solo para clases sin salón; se marcan `estimado = 1`. Coincide con el SAES en ~82 %.
Las claves de materia se comparan sin acentos y en mayúsculas (substring). Si un salón está en `FOTOS`, cualquier estimado que apunte a él y no esté en su hoja se descarta.
El script imprime: huecos llenados por foto, correcciones al SAES, estimados descartados, sesiones L aplicadas y grupos aún sin confirmar.

## Pendientes / ideas
- Probar en un teléfono real (tamaño del texto del croquis, precisión del toque con el dedo).
- Confirmar el reparto real de salones por bloque y ajustar `Croquis.BLOQUES`.
- Más fotos (faltan sobre todo Edificio 4, 2205, 3010, 3011, 3209) para quitar estimados y conocer sesiones "L".
- Reportes en tiempo real ("está ocupado / está libre") para corregir datos y aprender qué profes no bajan al lab (requeriría backend).
- Calendario escolar (vacaciones, días inhábiles, semanas de exámenes).

## Convenciones
- Código, nombres y textos de UI en español (sin anglicismos innecesarios).
- La lógica va en `domain/` y debe ser Kotlin puro testeable en JVM; agregar pruebas en `OcupacionTest` al cambiar reglas.
- No editar `data.json` a mano: cambiar `datos/*.py` y regenerar.
