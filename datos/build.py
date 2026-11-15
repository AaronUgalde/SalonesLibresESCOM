"""Construye web/data.json combinando: SAES (13-nov) > fotos > mapeo 26-1 (estimado)."""
import json, sys, unicodedata, collections
import os
AQUI = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, AQUI)
from mapeo_26_1 import BASE, EXC
from fotos import FOTOS, EXTRAS, LABS

norm = lambda s: ''.join(c for c in unicodedata.normalize('NFD', s.upper()) if unicodedata.category(c) != 'Mn')
DIAS = ['Lun', 'Mar', 'Mie', 'Jue', 'Vie', 'Sab']
mins = lambda h: int(h[:2]) * 60 + int(h[3:])

rows = json.load(open(os.path.join(AQUI, 'horarios_saes_2026-11-13.json'), encoding='utf-8'))['actual']

foto_idx = collections.defaultdict(list)  # grupo -> [(salon, kw)]
for salon, grupos in FOTOS.items():
    for g, kws in grupos.items():
        for kw in kws:
            foto_idx[g].append((salon, kw))

def mapeo(r):
    g, m = r['Grupo'], norm(r['Asignatura'])
    for eg, kw, s in EXC:
        if eg == g and kw in m:
            return s
    return BASE.get(g)

report = collections.defaultdict(list)
clases = []
labs_usados = set()
for r in rows:
    g, m = r['Grupo'], norm(r['Asignatura'])
    e, s = r['Edificio'].strip(), r['Salón'].strip()
    saes = e + s if (e.isdigit() and s.isdigit()) else None
    foto = next((sal for sal, kw in foto_idx.get(g, []) if kw in m), None)
    if foto:
        room, est = foto, 0
        if saes and saes != foto:
            report['foto_corrige_saes'].append(f"{g} {r['Asignatura'][:35]}: SAES {saes} -> foto {foto}")
        elif not saes:
            report['foto_llena_hueco'].append(f"{g} {r['Asignatura'][:35]} -> {foto}")
    elif saes:
        room, est = saes, 0
    else:
        room, est = mapeo(r), 1
        if room in FOTOS:  # salón fotografiado: si no aparece en la hoja, el estimado es falso
            report['estimado_descartado'].append(f"{g} {r['Asignatura'][:35]} (no está en la hoja de {room})")
            room = None
        if not room:
            continue
    for i, dk in enumerate(DIAS):
        for rng in r[dk].split():
            a, b = rng.split('-')
            lab = 1 if any(lg == g and kw in m and ld == i and lt == mins(a) for lg, kw, ld, lt in LABS) else 0
            if lab: labs_usados.add((g, m, i, mins(a)))
            clases.append([room, i, mins(a), mins(b), g, r['Asignatura'].title(), ' '.join(r['Profesor'].split()).title(), est, lab])

for salon, dia, a, b, g, mat in EXTRAS:
    if not any(c[0] == salon and c[1] == dia and c[2] == a and c[4] == g for c in clases):
        clases.append([salon, dia, a, b, g, mat, '', 0, 1 if any(lg == g and kw in norm(mat) and ld == dia and lt == a for lg, kw, ld, lt in LABS) else 0])

for c in clases:  # estimados que chocan con algo confirmado
    if c[7] == 1 and any(o[7] == 0 and o[8] == 0 and o[0] == c[0] and o[1] == c[1] and o[2] < c[3] and c[2] < o[3] for o in clases):
        c[7] = 2

if __name__ == '__main__':
    json.dump({'capturado': '2026-11-13T22:58:00Z', 'fuente': 'saes.escom.ipn.mx/Academica/horarios.aspx + fotos',
               'clases': clases}, open(os.path.join(AQUI, '..', 'app', 'src', 'main', 'assets', 'data.json'), 'w', encoding='utf-8'), ensure_ascii=False, separators=(',', ':'))
    for k, v in report.items():
        print(f'== {k} ({len(v)})'); print('\n'.join(v[:40]))
    hechos = {(g, kw) for g, l in foto_idx.items() for _, kw in l}
    usados = {(r['Grupo'], kw) for r in rows for (_, kw) in foto_idx.get(r['Grupo'], []) if kw in norm(r['Asignatura'])}
    print('== claves de foto sin match en SAES:', sorted(hechos - usados))
    print('sesiones L aplicadas', sum(c[8] for c in clases), 'de', len(LABS))
    print('total', len(clases), 'estimadas', sum(c[7] == 1 for c in clases), 'choques', sum(c[7] == 2 for c in clases))
    sinsalon = sorted({r['Grupo'] for r in rows if not (r['Edificio'].strip().isdigit() and r['Salón'].strip().isdigit())
                       and any(r[d] for d in DIAS)
                       and not any(kw in norm(r['Asignatura']) for _, kw in foto_idx.get(r['Grupo'], []))})
    print('grupos con clases aún sin confirmar por foto:', sinsalon)
