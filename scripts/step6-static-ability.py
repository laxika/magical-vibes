#!/usr/bin/env python3
"""EffectSlot refactor step 6: static abilities leave EffectSlot.STATIC and COMMAND_ZONE_STATIC.

Usage: step6-static-ability.py [--signatures-from REV] DIR...

Rewrites every .java file under each DIR (run it per module, per letter module for cards):

- ``addEffect(EffectSlot.STATIC, x[, mode])`` and ``target(f).addEffect(EffectSlot.STATIC, x)`` become
  ``addStaticEffect(x)``; a static ability has no trigger mode. The five effects that were triggers
  filed under STATIC (enlist and "attacking causes a triggered ability to trigger") move to
  ``EffectSlot.ON_ALLY_ATTACKING_CREATURE_ABILITY_TRIGGERS``.
  ``addEffect(EffectSlot.COMMAND_ZONE_STATIC, x)`` becomes ``addStaticEffect(AbilityZones.COMMAND, x)``.
- ``getEffects(EffectSlot.STATIC)`` becomes ``getStaticEffects()`` and
  ``getEffects(EffectSlot.COMMAND_ZONE_STATIC)`` becomes ``getStaticEffects(Zone.COMMAND)``.
- The granted-ability readers and writers of ``Permanent`` and ``GameData`` move to their static
  forms (``getTemporaryStaticEffects()``, ``addPersistentAbility(StaticAbility.of(x))``, ...).
- A grant record built with ``new Grant…(EffectSlot.STATIC, x, ...)`` gets ``StaticAbility.of(x)``.
- Slot-keyed ability maps become ``List<Ability>``: ``Map<EffectSlot, CardEffect>`` and
  ``Map<EffectSlot, List<CardEffect>>`` types become ``List<Ability>``, a slot-keyed ``Map.of(...)``
  becomes ``List.of(...)`` of ``StaticAbility.of(v)`` for STATIC and ``LegacySlots.triggered(slot, v)``
  otherwise, and an empty ``Map.of()`` becomes ``List.of()`` where it is passed as the slot map of
  one of the converted records. Which argument that is comes from the records' constructor and
  factory signatures, read from ``--signatures-from`` (a git revision, default HEAD) so the script can
  run after the records themselves were changed.

Imports that a rewrite needs are added, and an ``EffectSlot`` or ``Map`` import left unused is
removed. Prints every file it changed and every site it could not convert.
"""
import pathlib
import re
import subprocess
import sys

MODEL = 'com.github.laxika.magicalvibes.model'
EFFECT_DIR = 'magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect/'
SLOT_MAP_RECORDS = [
    'CreateTokenEffect',
    'BecomeCopyOfExiledCreaturePermanentlyEffect',
    'BecomeCopyOfTargetCreatureUntilEndOfTurnEffect',
    'ConjureCardIntoControllerLibraryEffect',
    'ConjureCardNamedIntoLibraryEffect',
    'CopyPermanentOnEnterEffect',
    'CreateTokenCopyOfTargetPermanentEffect',
    'TurnFaceUpCopyEffect',
]
# Wither and factory names whose only Map parameter is a slot map.
SLOT_MAP_METHODS = {'withTokenEffects', 'withAdditionalSlotEffects', 'withAdditionalAbilities'}
ENLIST_TRIGGERS = {
    'ScryWhenThisCreatureEnlistsEffect',
    'ConjureDuplicateOfEnlistedNontokenCreatureEffect',
    'FirebenderAscensionEffect',
    'GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect',
    'AradeshTheFounderEffect',
}
ENLIST_SLOT = 'EffectSlot.ON_ALLY_ATTACKING_CREATURE_ABILITY_TRIGGERS'

SLOT_PREFIX = r'(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\.'
STATIC = SLOT_PREFIX + r'STATIC\b'
COMMAND_STATIC = SLOT_PREFIX + r'COMMAND_ZONE_STATIC\b'
SLOT_MAP_TYPE = re.compile(
    r'Map<\s*(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\s*,\s*'
    r'(?:(?:java\.util\.)?List<\s*(?:[a-z]\w*\.)*CardEffect\s*>|(?:[a-z]\w*\.)*CardEffect)\s*>')


def matching_paren(src, open_index):
    """The index of the parenthesis that closes the one at ``open_index``."""
    depth = 0
    i = open_index
    in_str = None
    while i < len(src):
        c = src[i]
        if in_str:
            if c == '\\':
                i += 2
                continue
            if c == in_str:
                in_str = None
        elif c in '"\'':
            in_str = c
        elif c in '([{':
            depth += 1
        elif c in ')]}':
            depth -= 1
            if depth == 0:
                return i
        i += 1
    raise ValueError('unbalanced parentheses')


def split_args(src, open_index):
    """The top-level arguments of the call whose '(' is at ``open_index``, as (start, end) spans."""
    close = matching_paren(src, open_index)
    spans = []
    depth = 0
    start = open_index + 1
    i = start
    in_str = None
    while i < close:
        c = src[i]
        if in_str:
            if c == '\\':
                i += 2
                continue
            if c == in_str:
                in_str = None
        elif c in '"\'':
            in_str = c
        elif c in '([{':
            depth += 1
        elif c in ')]}':
            depth -= 1
        elif c == ',' and depth == 0:
            spans.append((start, i))
            start = i + 1
        i += 1
    if src[start:close].strip():
        spans.append((start, close))
    return spans, close


def arg_text(src, span):
    return src[span[0]:span[1]].strip()


def split_params(text):
    """The parameter declarations of a parameter list, split at commas outside generics."""
    params = []
    depth = 0
    start = 0
    for i, c in enumerate(text):
        if c in '<(':
            depth += 1
        elif c in '>)':
            depth -= 1
        elif c == ',' and depth == 0:
            params.append(text[start:i])
            start = i + 1
    if text[start:].strip():
        params.append(text[start:])
    return params


def read_signatures(rev):
    """For each converted record: the parameter type lists of its constructors and static factories."""
    signatures = {}
    for record in SLOT_MAP_RECORDS:
        path = EFFECT_DIR + record + '.java'
        src = subprocess.run(['git', 'show', f'{rev}:{path}'], capture_output=True, text=True,
                             check=True).stdout
        sigs = []
        for m in re.finditer(r'(?:public\s+record\s+' + record + r'|public\s+' + record
                             + r'|public\s+static\s+' + record + r'\s+(\w+))\s*\(', src):
            open_index = m.end() - 1
            types = []
            for param in split_params(src[open_index + 1:matching_paren(src, open_index)]):
                param = re.sub(r'@\w+(\([^)]*\))?\s*', '', param.strip())
                param = re.sub(r'\s+', ' ', param)
                types.append(param.rsplit(' ', 1)[0] if ' ' in param else param)
            name = m.group(1) if m.group(1) else '<init>'
            sigs.append((name, types))
        signatures[record] = sigs
    return signatures


def slot_map_positions(signatures, record, name, arity):
    """Argument positions that are a slot map in some signature of ``record.name`` with ``arity`` and
    another kind of map in none, so an empty ``Map.of()`` there can only be the slot map."""
    candidates = [types for sig_name, types in signatures.get(record, []) if sig_name == name
                  and len(types) == arity]
    positions = set()
    for i in range(arity):
        kinds = {('slot' if SLOT_MAP_TYPE.match(types[i]) else 'map' if types[i].startswith('Map')
                  else 'other') for types in candidates}
        if 'slot' in kinds and 'map' not in kinds:
            positions.add(i)
    return positions


def convert_slot_map_literal(src, start, report, path):
    """``Map.of(k1, v1, ...)`` at ``start`` with EffectSlot keys as a List.of of abilities, or None."""
    m = re.compile(r'Map\s*\.\s*of\s*\(').match(src, start)
    if not m:
        return None
    open_index = m.end() - 1
    spans, close = split_args(src, open_index)
    if not spans:
        return 'List.of()', close + 1
    if len(spans) % 2:
        return None
    parts = []
    for k, v in zip(spans[::2], spans[1::2]):
        key = arg_text(src, k)
        value = arg_text(src, v)
        km = re.fullmatch(SLOT_PREFIX + r'([A-Z_]+)', key)
        if not km:
            return None
        slot = km.group(1)
        if slot == 'STATIC':
            parts.append(f'StaticAbility.of({value})')
        elif slot == 'COMMAND_ZONE_STATIC':
            report.append(f'{path}: COMMAND_ZONE_STATIC in a slot map')
            return None
        else:
            parts.append(f'LegacySlots.triggered(EffectSlot.{slot}, {value})')
    if len(parts) == 1 and '\n' not in parts[0]:
        return f'List.of({parts[0]})', close + 1
    indent = re.search(r'[ \t]*$', src[:start][src[:start].rfind('\n') + 1:]).group(0)
    line_start = src.rfind('\n', 0, start) + 1
    base = re.match(r'[ \t]*', src[line_start:]).group(0)
    inner = base + ' ' * 8
    body = (',\n' + inner).join(parts)
    return f'List.of(\n{inner}{body})', close + 1


def rewrite_calls(src, path, report, signatures):
    """Converts slot maps passed positionally to the converted records' constructors and factories."""
    pattern = re.compile(r'(?:\bnew\s+(' + '|'.join(SLOT_MAP_RECORDS) + r')\s*(?:<[^>]*>)?|\b('
                         + '|'.join(SLOT_MAP_RECORDS) + r')\s*\.\s*(\w+)|\bthis|\.\s*('
                         + '|'.join(sorted(SLOT_MAP_METHODS)) + r'))\s*\(')
    this_record = pathlib.Path(path).stem if pathlib.Path(path).stem in SLOT_MAP_RECORDS else None
    pos = 0
    while True:
        m = pattern.search(src, pos)
        if not m:
            return src
        open_index = m.end() - 1
        if m.group(1):
            record, name = m.group(1), '<init>'
        elif m.group(2):
            record, name = m.group(2), m.group(3)
        elif m.group(4):
            record, name = None, m.group(4)
        else:
            if this_record is None:
                pos = m.end()
                continue
            record, name = this_record, '<init>'
        try:
            spans, _ = split_args(src, open_index)
        except ValueError:
            pos = m.end()
            continue
        if record is None:
            positions = {0} if len(spans) == 1 else set()
        else:
            positions = slot_map_positions(signatures, record, name, len(spans))
        # Rewrite right to left so the spans stay valid.
        for index in sorted(positions, reverse=True):
            span = spans[index]
            text = arg_text(src, span)
            lead = len(src[span[0]:span[1]]) - len(src[span[0]:span[1]].lstrip())
            begin = span[0] + lead
            if re.fullmatch(r'Map\s*\.\s*of\s*\(\s*\)', text):
                src = src[:begin] + 'List.of()' + src[begin + len(text):]
            elif re.match(r'Map\s*\.\s*of\s*\(', text):
                converted = convert_slot_map_literal(src, begin, report, path)
                if converted is None:
                    report.append(f'{path}: slot map not converted: {text[:60]}')
                else:
                    new, end = converted
                    src = src[:begin] + new + src[end:]
            elif text == 'null' or re.fullmatch(r'[a-z]\w*(\(\))?', text):
                pass
            elif text.startswith('List.of(') or text.startswith('LegacySlots.'):
                pass
            else:
                report.append(f'{path}: slot map argument left as is: {text[:60]}')
        pos = m.end()


def rewrite_slot_map_literals(src, path, report):
    """Every remaining ``Map.of(EffectSlot.X, ...)`` becomes a list of abilities."""
    pattern = re.compile(r'Map\s*\.\s*of\s*\(\s*' + SLOT_PREFIX)
    pos = 0
    while True:
        m = pattern.search(src, pos)
        if not m:
            return src
        converted = convert_slot_map_literal(src, m.start(), report, path)
        if converted is None:
            report.append(f'{path}: slot map literal not converted')
            pos = m.end()
            continue
        new, end = converted
        src = src[:m.start()] + new + src[end:]
        pos = m.start() + len(new)


def rewrite_call_args(src, call, build):
    """Rewrites each ``call(args...)``: ``build(args)`` returns the new call text, or None to keep it."""
    pattern = re.compile(r'(?<!\w)' + call + r'\(')
    out = []
    pos = 0
    while True:
        m = pattern.search(src, pos)
        if not m:
            out.append(src[pos:])
            return ''.join(out)
        try:
            spans, close = split_args(src, m.end() - 1)
        except ValueError:
            out.append(src[pos:m.end()])
            pos = m.end()
            continue
        new = build([arg_text(src, s) for s in spans])
        if new is None:
            out.append(src[pos:m.end()])
            pos = m.end()
            continue
        out.append(src[pos:m.start()])
        out.append(new)
        pos = close + 1


def is_slot(text, regex):
    return re.fullmatch(regex, text) is not None


def first_type(text):
    m = re.match(r'\s*(?:new\s+(?:[a-z]\w*\.)*([A-Z]\w*)|(?:[a-z]\w*\.)*([A-Z]\w*)\s*\.)', text)
    return (m.group(1) or m.group(2)) if m else None


def convert(src, path, report, signatures):
    original = src

    def add_effect(args):
        if len(args) < 2:
            return None
        if is_slot(args[0], COMMAND_STATIC) and len(args) == 2:
            return f'addStaticEffect(AbilityZones.COMMAND, {args[1]})'
        if not is_slot(args[0], STATIC):
            return None
        if first_type(args[1]) in ENLIST_TRIGGERS:
            return f'addEffect({ENLIST_SLOT}, {", ".join(args[1:])})'
        if len(args) == 3 and re.fullmatch(r'(?:TriggerMode\.)?[A-Z_]+', args[2]) or len(args) == 2:
            return f'addStaticEffect({args[1]})'
        report.append(f'{path}: addEffect(STATIC, ...) with {len(args)} arguments')
        return None

    src = rewrite_call_args(src, 'addEffect', add_effect)
    src = re.sub(r'\bgetEffects\(\s*' + STATIC + r'\s*\)', 'getStaticEffects()', src)
    src = re.sub(r'\bgetEffects\(\s*' + COMMAND_STATIC + r'\s*\)', 'getStaticEffects(Zone.COMMAND)', src)
    src = re.sub(r'\bgetTemporaryTriggeredEffects\(\s*' + STATIC + r'\s*\)', 'getTemporaryStaticEffects()', src)
    src = re.sub(r'\bgetPersistentTriggeredEffects\(\s*' + STATIC + r'\s*\)', 'getPersistentStaticEffects()', src)
    for old, new in (('addPersistentTriggeredEffect', 'addPersistentAbility'),
                     ('addTemporaryTriggeredEffect', 'addTemporaryAbility'),
                     ('addCombatTriggeredEffect', 'addCombatAbility')):
        src = rewrite_call_args(src, old, lambda args, new=new: f'{new}(StaticAbility.of({args[1]}))'
                                if len(args) == 2 and is_slot(args[0], STATIC) else None)
    src = rewrite_call_args(src, 'perpetualAbilityEffects', lambda args: f'perpetualStaticEffects({args[0]})'
                            if len(args) == 2 and is_slot(args[1], STATIC) else None)
    src = rewrite_call_args(src, 'grantPerpetualAbility',
                            lambda args: f'grantPerpetualAbility({args[0]}, StaticAbility.of({args[2]}))'
                            if len(args) == 3 and is_slot(args[1], STATIC) else None)
    # new Grant…(EffectSlot.STATIC, effect, rest...)
    pattern = re.compile(r'\bnew\s+(\w+)\s*\(\s*' + STATIC + r'\s*,')
    pos = 0
    while True:
        m = pattern.search(src, pos)
        if not m:
            break
        open_index = src.index('(', m.start())
        spans, close = split_args(src, open_index)
        effect = arg_text(src, spans[1])
        rest = src[spans[1][1]:close]
        replacement = f'new {m.group(1)}(StaticAbility.of({effect}){rest})'
        src = src[:m.start()] + replacement + src[close + 1:]
        pos = m.start() + len(replacement)

    src = rewrite_calls(src, path, report, signatures)
    src = rewrite_slot_map_literals(src, path, report)
    src = SLOT_MAP_TYPE.sub('List<Ability>', src)

    for regex in (STATIC, COMMAND_STATIC):
        for m in re.finditer(regex, src):
            line = src.count('\n', 0, m.start()) + 1
            report.append(f'{path}:{line}: {m.group(0)} left in place')
    if src == original:
        return src
    return fix_imports(src, path)


def fix_imports(src, path):
    package = re.search(r'^package ([\w.]+);', src, re.M).group(1)
    body = re.sub(r'^\s*import .*$', '', src, flags=re.M)
    wanted = []
    for name in ('StaticAbility', 'LegacySlots', 'AbilityZones', 'Ability', 'Zone', 'EffectSlot'):
        if re.search(r'(?<![\w.])' + name + r'\b', body) and package != MODEL:
            wanted.append(f'{MODEL}.{name}')
    if re.search(r'(?<![\w.])List\b', body):
        wanted.append('java.util.List')
    for fq in wanted:
        simple = fq.rsplit('.', 1)[1]
        if re.search(r'^import ' + re.escape(fq) + r';', src, re.M):
            continue
        if re.search(r'^import [\w.]+\.' + simple + r';', src, re.M):
            continue
        if re.search(r'^import ' + re.escape(fq.rsplit('.', 1)[0]) + r'\.\*;', src, re.M):
            continue
        src = insert_import(src, fq)
    for fq, simple in ((f'{MODEL}.EffectSlot', 'EffectSlot'), ('java.util.Map', 'Map'),
                       ('java.util.EnumMap', 'EnumMap')):
        if not re.search(r'(?<![\w.])' + simple + r'\b', body):
            src = re.sub(r'^import ' + re.escape(fq) + r';\n', '', src, flags=re.M)
    return src


def insert_import(src, fq):
    line = f'import {fq};\n'
    imports = list(re.finditer(r'^import (?!static )([\w.]+);\n', src, re.M))
    if not imports:
        m = re.search(r'^package [\w.]+;\n', src, re.M)
        return src[:m.end()] + '\n' + line + src[m.end():]
    group = [i for i in imports if i.group(1).startswith('java.') == fq.startswith('java.')]
    for i in group:
        if i.group(1) > fq:
            return src[:i.start()] + line + src[i.start():]
    if group:
        last = group[-1]
        return src[:last.end()] + line + src[last.end():]
    if fq.startswith('java.'):
        last = imports[-1]
        return src[:last.end()] + '\n' + line + src[last.end():]
    first = imports[0]
    return src[:first.start()] + line + '\n' + src[first.start():]


def main(argv):
    rev = 'HEAD'
    if '--signatures-from' in argv:
        i = argv.index('--signatures-from')
        rev = argv[i + 1]
        argv = argv[:i] + argv[i + 2:]
    signatures = read_signatures(rev)
    report = []
    for d in argv:
        for path in sorted(pathlib.Path(d).rglob('*.java')):
            if '/build/' in str(path):
                continue
            src = path.read_text(encoding='utf-8')
            if 'STATIC' not in src and 'EffectSlot' not in src and not any(
                    r in src for r in SLOT_MAP_RECORDS):
                continue
            new = convert(src, str(path), report, signatures)
            if new != src:
                path.write_text(new, encoding='utf-8')
                print(path)
    for line in report:
        print('REPORT ' + line)


if __name__ == '__main__':
    main(sys.argv[1:])
