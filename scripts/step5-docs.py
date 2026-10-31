#!/usr/bin/env python3
"""EffectSlot refactor step 5: rewrites the spell-ability authoring calls in agent-docs.

Usage: step5-docs.py FILE...

Code snippets move to the new API (``addSpellEffect``, ``addSimultaneousSpellEffects``,
``addCastingCost`` for a CostEffect type, ``getSpellEffects``, ``getTargetGroups``), and
"SPELL-slot additional (cast) cost" wording becomes "casting cost". The tables' ``SPELL`` label is
left alone; each table doc explains it in a note.
"""
import importlib.util
import pathlib
import re
import sys

HERE = pathlib.Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('step5_cost_types', HERE / 'step5-cost-types.py')
cost_types_module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cost_types_module)
COST_TYPES = cost_types_module.cost_types()

ADD = re.compile(r'\baddEffect\(\s*(?:EffectSlot\.)?SPELL\s*,\s*')
ARG_TYPE = re.compile(r'(?:new\s+)?`?([A-Z]\w*)')


def convert(src):
    out = []
    pos = 0
    for m in ADD.finditer(src):
        out.append(src[pos:m.start()])
        arg = ARG_TYPE.match(src, m.end())
        out.append('addCastingCost(' if arg and arg.group(1) in COST_TYPES else 'addSpellEffect(')
        pos = m.end()
    out.append(src[pos:])
    src = ''.join(out)
    src = re.sub(r'\baddSimultaneousEffects\(\s*(?:EffectSlot\.)?SPELL\s*,\s*', 'addSimultaneousSpellEffects(', src)
    src = re.sub(r'\bgetEffects\(\s*EffectSlot\.SPELL\s*\)', 'getSpellEffects()', src)
    src = src.replace('getSpellTargets()', 'getTargetGroups()')
    src = re.sub(r'SPELL-slot additional cast cost', 'casting cost (`addCastingCost`)', src)
    src = re.sub(r'SPELL-slot additional cost', 'casting cost (`addCastingCost`)', src)
    return src


for name in sys.argv[1:]:
    path = pathlib.Path(name)
    src = path.read_text(encoding='utf-8', errors='surrogateescape')
    new = convert(src)
    if new != src:
        path.write_text(new, encoding='utf-8', errors='surrogateescape')
        print(path)
