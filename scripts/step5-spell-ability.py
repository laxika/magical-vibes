#!/usr/bin/env python3
"""EffectSlot refactor step 5: spell abilities and casting costs leave EffectSlot.SPELL.

Usage: step5-spell-ability.py [--engine] DIR...

Rewrites every .java file under each DIR (run it per module, per letter module for cards):

- ``addEffect(EffectSlot.SPELL, x)`` becomes ``addCastingCost(x)`` when ``x`` is a
  ``new SomeCost(...)`` or ``SomeCost.factory(...)`` of a CostEffect type, and
  ``addSpellEffect(x)`` otherwise; ``addSimultaneousEffects(EffectSlot.SPELL, ...)`` becomes
  ``addSimultaneousSpellEffects(...)``. A cost added through a target builder
  (``target(f).addEffect(EffectSlot.SPELL, cost)``) can't be routed and is reported.
- ``getEffects(EffectSlot.SPELL)`` becomes ``getCastingCosts()`` when the statement only asks for
  cost types, ``getSpellEffects()`` when it only asks positively for other effect types, and
  ``getCastingCostsAndSpellEffects()`` (exactly what the slot held) otherwise.
  ``getEffectRegistrations(EffectSlot.SPELL)`` becomes ``getSpellEffectRegistrations()``.
- With ``--engine`` (non-card code): ``getSpellTargets()`` becomes ``getTargetGroups()`` and the
  ``SpellTarget`` type becomes ``TargetGroup``; card code keeps the ``SpellTarget`` builder.

An ``EffectSlot`` import left unused is removed. Prints every file it changed and every site it
could not convert.
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

SLOT = r'(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\.SPELL\b'
ADD_EFFECT = re.compile(r'\baddEffect\(\s*' + SLOT + r'\s*,\s*')
ADD_SIMULTANEOUS = re.compile(r'\baddSimultaneousEffects\(\s*' + SLOT + r'\s*,\s*')
GET_EFFECTS = re.compile(r'\bgetEffects\(\s*' + SLOT + r'\s*\)')
GET_REGISTRATIONS = re.compile(r'\bgetEffectRegistrations\(\s*' + SLOT + r'\s*\)')
ARG_TYPE = re.compile(r'(?:new\s+(?:[a-z]\w*\.)*([A-Z]\w*)\s*[(<])|(?:(?:[a-z]\w*\.)*([A-Z]\w*)\s*\.\s*\w+\s*\()')
CLASS_REF = re.compile(r'\b([A-Z]\w*)\.class\b|\binstanceof\s+(?:[a-z]\w*\.)*([A-Z]\w*)')
# The builder and the card that hands it out keep the SpellTarget name in --engine mode.
BUILDER_FILES = {'SpellTarget.java', 'TargetGroup.java', 'Card.java'}
IMPORT_EFFECT_SLOT = re.compile(r'^import com\.github\.laxika\.magicalvibes\.model\.EffectSlot;\n', re.M)


def statement_end(src, start):
    """The index just past the end of the statement containing ``start``."""
    depth = 0
    i = start
    while i < len(src):
        c = src[i]
        if c in '([{':
            depth += 1
        elif c in ')]}':
            depth -= 1
            if depth < 0:
                return i
        elif c == ';' and depth <= 0:
            return i
        i += 1
    return i


def classify_read(src, match):
    """Which accessor a getEffects(SPELL) read becomes."""
    tail = src[match.end():statement_end(src, match.end())]
    if re.match(r'\s*\.stream\(\)\s*\.filter\(\s*(?:[a-z]\w*\.)*CostEffect\.class::isInstance\s*\)', tail):
        return 'costs-filter'
    refs = {a or b for a, b in CLASS_REF.findall(tail)}
    refs.discard('CostEffect')
    if not refs:
        return 'both'
    risky = re.search(r'allMatch|removeIf|!\s*\(|!\s*\w+\s+instanceof|indexOf|getFirst|get\(\d|size\(\)|isEmpty\(\)',
                      tail)
    if risky:
        return 'both'
    if refs <= COST_TYPES:
        return 'costs'
    if not refs & COST_TYPES:
        return 'effects'
    return 'both'


def convert(src, path, report, engine):
    out = []
    pos = 0
    for m in ADD_EFFECT.finditer(src):
        out.append(src[pos:m.start()])
        arg = ARG_TYPE.match(src, m.end())
        arg_type = (arg.group(1) or arg.group(2)) if arg else None
        if arg_type in COST_TYPES:
            receiver = src[max(0, m.start() - 200):m.start()]
            if re.search(r'\)\s*\.\s*$', receiver) and re.search(r'target\w*\([^;]*$', receiver):
                report.append(f'{path}: casting cost {arg_type} added through a target builder')
            out.append('addCastingCost(')
        else:
            out.append('addSpellEffect(')
        pos = m.end()
    out.append(src[pos:])
    src = ''.join(out)
    src = ADD_SIMULTANEOUS.sub('addSimultaneousSpellEffects(', src)
    src = GET_REGISTRATIONS.sub('getSpellEffectRegistrations()', src)

    out = []
    pos = 0
    for m in GET_EFFECTS.finditer(src):
        out.append(src[pos:m.start()])
        kind = classify_read(src, m)
        if kind == 'costs-filter':
            filt = re.match(r'\s*\.stream\(\)\s*\.filter\(\s*(?:[a-z]\w*\.)*CostEffect\.class::isInstance\s*\)',
                            src[m.end():])
            out.append('getCastingCosts().stream()')
            pos = m.end() + filt.end()
            continue
        out.append({'costs': 'getCastingCosts()', 'effects': 'getSpellEffects()',
                    'both': 'getCastingCostsAndSpellEffects()'}[kind])
        pos = m.end()
    out.append(src[pos:])
    src = ''.join(out)

    if engine:
        src = src.replace('.getSpellTargets()', '.getTargetGroups()')
        src = re.sub(r'\bimport com\.github\.laxika\.magicalvibes\.model\.SpellTarget;',
                     'import com.github.laxika.magicalvibes.model.TargetGroup;', src)
        src = re.sub(r'(?<![.\w])SpellTarget\b(?!\.ANY_NUMBER_OF_TARGETS)', 'TargetGroup', src)
        src = src.replace('SpellTarget.ANY_NUMBER_OF_TARGETS', 'TargetGroup.ANY_NUMBER_OF_TARGETS')

    if re.search(SLOT, src):
        report.append(f'{path}: EffectSlot.SPELL left in place')
    body = IMPORT_EFFECT_SLOT.sub('', src)
    if 'EffectSlot' not in re.sub(r'^\s*import .*$', '', body, flags=re.M):
        src = body
    return src


def main(argv):
    engine = '--engine' in argv
    dirs = [a for a in argv if not a.startswith('--')]
    report = []
    for d in dirs:
        for path in sorted(pathlib.Path(d).rglob('*.java')):
            if engine and path.name in BUILDER_FILES:
                continue
            src = path.read_text(encoding='utf-8')
            if 'SPELL' not in src and not (engine and 'SpellTarget' in src):
                continue
            new = convert(src, path, report, engine)
            if new != src:
                path.write_text(new, encoding='utf-8')
                print(path)
    for line in report:
        print('REPORT ' + line)


if __name__ == '__main__':
    main(sys.argv[1:])
