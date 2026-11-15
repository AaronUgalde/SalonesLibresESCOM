"""Mapeo grupo -> salón de la Distribución de salones 26-1 (transcrito del PDF oficial).
BASE: salón del grupo. EXC: (grupo, palabra clave de la materia) -> salón para esa materia.
"""

BASE = {
    # Automotrices
    "7SV1": "4010",
    # 1.º y 2.º matutino
    "1AM1": "1202", "1AM2": "1203", "1AM3": "1212", "1BM1": "1204", "1BM2": "1205", "1BM3": "1111",
    "1CM1": "1206", "1CM2": "1207", "1CM3": "1209", "1CM4": "1210", "1CM5": "1211", "1CM6": "1212",
    "1CM7": "1213", "1CM8": "1012", "2AM1": "1111", "2AM2": "1112", "2BM1": "1113", "2BM2": "1114",
    "2CM1": "1007", "2CM2": "1011", "2CM3": "1012", "2CM4": "1013", "2CM5": "1014", "2CM6": "1214",
    "2CM7": "2212",
    # 1.º y 2.º vespertino
    "1AV1": "1202", "1AV2": "1203", "1BV1": "1204", "1BV2": "1205", "1CV1": "1206", "1CV2": "1207",
    "1CV3": "1209", "1CV4": "1210", "1CV5": "1211", "1CV6": "1212", "2AV1": "1111", "2AV2": "1112",
    "2BV1": "1113", "2BV2": "1114", "2CV1": "1007", "2CV2": "1011", "2CV3": "1012", "2CV4": "1013",
    "2CV5": "1014", "2CV6": "1112", "2CV7": "1213",
    # 3.º y 4.º matutino
    "3AM1": "2202", "3AM2": "2203", "3BM1": "2204", "3BM2": "2205", "3CM1": "2206", "3CM2": "2207",
    "3CM3": "2209", "3CM4": "2210", "3CM5": "2211", "3CM6": "2212", "4AM1": "2213", "4AM2": "2109",
    "4BM1": "2110", "4BM2": "2002", "4CM1": "2003", "4CM2": "2004", "4CM3": "2005", "4CM4": "2006",
    "4CM5": "2007",
    # 3.º y 4.º vespertino
    "3AV1": "2203", "3BV1": "2204", "3BV2": "2205", "3CV1": "2206", "3CV2": "2207", "3CV3": "2209",
    "3CV4": "2210", "3CV5": "2211", "3CV6": "2212", "4AV1": "2213", "4AV2": "2109", "4BV1": "2110",
    "4CV1": "2003", "4CV2": "2004", "4CV3": "2005", "4CV4": "2006", "4CV5": "2007",
    # 5.º y 6.º matutino
    "5AM1": "3008", "5AM2": "3009", "5BM1": "3010", "5BM2": "3011", "5CM1": "3108", "5CM2": "3109",
    "5CM3": "3110", "5CM4": "3111", "5CM5": "3112", "6AM1": "3113", "6BM1": "3208", "6BM2": "3209",
    "6CM1": "3210", "6CM2": "3211", "6CM3": "3212", "6CM4": "3213",
    # 5.º y 6.º vespertino
    "5AV1": "3008", "5BV1": "3010", "5CV1": "3108", "5CV2": "3109", "5CV3": "3110", "5CV4": "3111",
    "6AV1": "3113", "6BV1": "3208", "6CV1": "3210", "6CV2": "3211", "6CV3": "3212", "6CV4": "3213",
    "6CV5": "4009",
    # 7.º y 8.º matutino
    "7AM1": "4008", "7BM1": "4009", "7BM2": "4010", "7CM1": "4011", "7CM2": "4108", "7CM3": "4109",
    "7CM4": "4110", "7CM5": "1210", "8AM1": "4111", "8BM1": "4112", "8BM2": "4112", "8CM1": "4113",
    "8CM2": "4208", "8CM3": "4113", "8CM5": "1014",
    # 7.º y 8.º vespertino
    "7AV1": "4008", "7BV1": "4009", "7CV1": "4011", "7CV2": "4108", "7CV3": "4109", "7CV4": "4110",
    "7CV5": "2211", "8AV1": "4111", "8BV1": "4112", "8CV1": "4113", "8CV2": "4208", "8CV3": "3011",
    "8CV4": "2110",
}

EXC = [
    ("1AV2", "MATEMATICAS DISCRETAS", "1011"),
    ("2BM2", "FUNDAMENTOS DE DISENO DIGITAL", "1013"),
    ("2CM1", "CALCULO APLICADO", "3111"),
    ("2CM2", "INGENIERIA, ETICA Y SOCIEDAD", "3211"),
    ("2CM3", "CALCULO APLICADO", "2206"),
    ("2CM4", "CALCULO APLICADO", "1114"),
    ("2CV2", "ALGORITMOS Y ESTRUCTURA", "1203"),
    ("2CV3", "MECANICA Y ELECTROMAGNETISMO", "2109"),
    ("2CV3", "ALGORITMOS Y ESTRUCTURA", "1213"),
    ("2CV5", "MECANICA Y ELECTROMAGNETISMO", "1213"),
    ("2CV6", "CALCULO APLICADO", "1111"),
    ("3BM1", "DISENO DE SISTEMAS DIGITALES", "2007"),
    ("3CM1", "FUNDAMENTOS DE DISENO DIGITAL", "1012"),
    ("3CM1", "FINANZAS EMPRESARIALES", "4108"),
    ("3CM2", "FUNDAMENTOS DE DISENO DIGITAL", "2007"),
    ("3CM3", "FINANZAS EMPRESARIALES", "1112"),
    ("4CM1", "MATEMATICAS AVANZADAS", "2211"),
    ("4CM2", "MATEMATICAS AVANZADAS", "2210"),
    ("3CV1", "FINANZAS EMPRESARIALES", "4208"),
    ("4AV1", "BASES DE DATOS AVANZADAS", "2002"),
    ("4AV1", "ESTADISTICA", "2002"),
    ("4BV1", "PROBABILIDAD Y ESTADISTICA", "2007"),
    ("5AM2", "ANALITICA Y VISUALIZACION", "2111"),
    ("5BM1", "TEORIA DE LA COMPUTACION", "3211"),
    ("5CM4", "REDES DE COMPUTADORAS", "1007"),
    ("6CM1", "SOFTWARE QUALITY", "3009"),
    ("6CM2", "INGENIERIA DE SOFTWARE", "3010"),
    ("6CM2", "CRYPTOGRAPHY", "1011"),
    ("6CM5", "INTELIGENCIA ARTIFICIAL", "2004"),
    ("6CM5", "INGENIERIA DE SOFTWARE", "1113"),
    ("6CM5", "APLICACIONES PARA COMUNICACIONES", "2003"),
    ("6CV1", "SISTEMAS EN CHIP", "3209"),
    ("6CV2", "STATISTICAL TOOLS", "3112"),
    ("6CV3", "INTELIGENCIA ARTIFICIAL", "3209"),
    ("7CM2", "SISTEMAS DISTRIBUIDOS", "2206"),
    ("7CM6", "SISTEMAS DISTRIBUIDOS", "2002"),
    ("7CM6", "ADMINISTRACION DE SERVICIOS EN RED", "3210"),
    ("8CM4", "LIDERAZGO PERSONAL", "4110"),
    ("8CM4", "GESTION EMPRESARIAL", "4008"),
    ("8CM4", "HABILIDADES SOCIALES", "2212"),
    ("7AV1", "ADMINISTRACION DE PROYECTOS", "4112"),
    ("8BV1", "GESTION EMPRESARIAL", "4008"),
]
