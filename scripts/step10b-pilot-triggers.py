#!/usr/bin/env python3
"""EffectSlot refactor step 10b: the pilot slots' cards get real triggers.

Usage: step10b-pilot-triggers.py --scan SCAN_TSV DIR...

Rewrites every .java file under each DIR (run it per letter module for cards and card tests):

- ``addEffect(EffectSlot.SLOT, x)``, on the card, a ``target(...)`` group or another card, becomes
  ``addTrigger(TRIGGER, x)``.
- ``LegacySlots.triggered(EffectSlot.SLOT, x)`` becomes ``new TriggeredAbility(TRIGGER, x)``.
- Any other ``EffectSlot.SLOT, x`` argument pair, the slot and effect of a grant record or factory, becomes the
  one argument ``new TriggeredAbility(TRIGGER, x)``.
- ``getTemporaryTriggeredEffects(EffectSlot.ON_DEATH)`` becomes ``getTemporaryTriggeredAbilities()``.
- A pilot slot named in a comment becomes the ability it stands for ("its dies trigger").

TRIGGER is the slot's ``Triggers`` factory with ``ObjectRelation.THIS``:

- ON_DEATH: ``Triggers.dies``; ``Triggers.sacrificed`` for a registration the scan marks as sacrifice-only, and
  ``Triggers.putIntoGraveyardWhileResolving`` for an instant's or sorcery's haunt (CR 702.55a);
- ON_SELF_LEAVES_BATTLEFIELD: ``Triggers.leavesBattlefield``;
- ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE: ``Triggers.putIntoGraveyardFromAnywhere``;
- ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, _HAND, _LIBRARY: ``Triggers.putIntoGraveyardFrom(Zone.…)``;
- ON_SELF_MILLED: ``Triggers.milled``.

The scan is ``step10b-pilot-scan.java``'s output, taken on the card classes before this step. A card with a
sacrifice-only or haunt-spell registration has only that one ON_DEATH registration, which the script checks.

Imports that a rewrite needs are added, and an ``EffectSlot`` or ``LegacySlots`` import left unused is removed.
Prints every file it changed and every pilot slot it could not convert.
"""
import argparse
import pathlib
import re
import sys

MODEL = 'com.github.laxika.magicalvibes.model'
SLOT_PREFIX = r'(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\.'
PILOT = {
    'ON_DEATH': 'Triggers.dies(ObjectRelation.THIS)',
    'ON_SELF_LEAVES_BATTLEFIELD': 'Triggers.leavesBattlefield(ObjectRelation.THIS)',
    'ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE': 'Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS)',
    'ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD': 'Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS)',
    'ON_SELF_PUT_INTO_GRAVEYARD_FROM_HAND': 'Triggers.putIntoGraveyardFrom(Zone.HAND, ObjectRelation.THIS)',
    'ON_SELF_PUT_INTO_GRAVEYARD_FROM_LIBRARY': 'Triggers.putIntoGraveyardFrom(Zone.LIBRARY, ObjectRelation.THIS)',
    'ON_SELF_MILLED': 'Triggers.milled(ObjectRelation.THIS)',
}
SACRIFICED = 'Triggers.sacrificed(ObjectRelation.THIS)'
HAUNT_SPELL = 'Triggers.putIntoGraveyardWhileResolving(ObjectRelation.THIS)'
SLOT = SLOT_PREFIX + r'(' + '|'.join(PILOT) + r')\b'


def matching_paren(src, open_index):
    """The index of the bracket that closes the one at ``open_index``."""
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
    raise ValueError('unbalanced brackets')


def argument_end(src, start):
    """The index just past the argument that starts at ``start``: the next ``,`` or ``)`` at depth 0."""
    i = start
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
            i = matching_paren(src, i) + 1
            continue
        elif c in ',)':
            return i
        i += 1
    raise ValueError('unterminated argument')


def load_scan(path):
    sacrificed, haunt = set(), set()
    counts = {}
    for line in pathlib.Path(path).read_text().splitlines():
        cls, slot, _index, _effect, sacrifice_only, haunt_spell = line.split('\t')
        simple = cls.rsplit('.', 1)[1]
        if slot == 'ON_DEATH':
            counts[simple] = counts.get(simple, 0) + 1
        if sacrifice_only == 'true':
            sacrificed.add(simple)
        if haunt_spell == 'true':
            haunt.add(simple)
    for simple in sacrificed | haunt:
        if counts[simple] != 1:
            sys.exit(f'{simple} has {counts[simple]} ON_DEATH registrations; convert it by hand')
    return sacrificed, haunt


def trigger_for(slot, class_name, sacrificed, haunt):
    if slot == 'ON_DEATH' and class_name in sacrificed:
        return SACRIFICED
    if slot == 'ON_DEATH' and class_name in haunt:
        return HAUNT_SPELL
    return PILOT[slot]


def convert(src, class_name, sacrificed, haunt):
    src = re.sub(r'getTemporaryTriggeredEffects\(\s*' + SLOT_PREFIX + r'ON_DEATH\s*\)',
                 'getTemporaryTriggeredAbilities()', src)
    out = []
    pos = 0
    pattern = re.compile(r'(?P<call>\baddEffect\(\s*|\bLegacySlots\.triggered\(\s*)?' + SLOT + r'\s*,')
    while True:
        m = pattern.search(src, pos)
        if not m:
            out.append(src[pos:])
            break
        trigger = trigger_for(m.group(2), class_name, sacrificed, haunt)
        call = m.group('call')
        out.append(src[pos:m.start()])
        if call and call.startswith('addEffect'):
            out.append('addTrigger(' + trigger + ',')
            pos = m.end()
        elif call:
            out.append('new TriggeredAbility(' + trigger + ',')
            pos = m.end()
        else:
            effect_start = m.end()
            effect_end = argument_end(src, effect_start)
            effect = src[effect_start:effect_end]
            out.append('new TriggeredAbility(' + trigger + ',' + effect + ')')
            pos = effect_end
    src = ''.join(out)
    src = re.sub(r'\bits ON_DEATH (may-)?trigger', r'its dies \1trigger', src)
    src = re.sub(r'\bON_DEATH (clash )?trigger', r'dies \1trigger', src)
    return '\n'.join(comment_names(line) for line in src.split('\n'))


COMMENT_NAMES = {
    'ON_DEATH': 'its dies trigger',
    'ON_SELF_LEAVES_BATTLEFIELD': 'its leaves-the-battlefield trigger',
    'ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE': 'its put-into-a-graveyard-from-anywhere trigger',
    'ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD': 'its put-into-a-graveyard-from-the-battlefield trigger',
    'ON_SELF_PUT_INTO_GRAVEYARD_FROM_HAND': 'its put-into-a-graveyard-from-hand trigger',
    'ON_SELF_PUT_INTO_GRAVEYARD_FROM_LIBRARY': 'its put-into-a-graveyard-from-library trigger',
    'ON_SELF_MILLED': 'its milled trigger',
}


def comment_names(line):
    """A comment line that names a pilot slot names the ability instead."""
    stripped = line.strip()
    if not (stripped.startswith('//') or stripped.startswith('*') or stripped.startswith('/*')):
        return line
    line = re.sub(r'\b(?:by|on|from) (?:the )?(' + '|'.join(COMMENT_NAMES) + r')\b(?: trigger)?(?: pipeline)?',
                  lambda m: 'by ' + COMMENT_NAMES[m.group(1)], line)
    return re.sub(r'\b(' + '|'.join(COMMENT_NAMES) + r')\b(?: trigger)?(?: pipeline)?',
                  lambda m: COMMENT_NAMES[m.group(1)], line)


def uses(src, name):
    body = re.sub(r'^import .*;$', '', src, flags=re.M)
    return re.search(r'\b' + name + r'\b', body) is not None


def fix_imports(src):
    wanted = {
        'Triggers': MODEL + '.trigger.Triggers',
        'ObjectRelation': MODEL + '.trigger.ObjectRelation',
        'TriggeredAbility': MODEL + '.TriggeredAbility',
        'Zone': MODEL + '.Zone',
    }
    lines = src.split('\n')
    imports = [i for i, line in enumerate(lines) if line.startswith('import ')]
    present = {line[len('import '):-1] for line in lines if line.startswith('import ')}
    package = re.search(r'^package ([\w.]+);', src, flags=re.M).group(1)
    for simple, fqn in wanted.items():
        if fqn in present or not uses(src, simple):
            continue
        if fqn.rsplit('.', 1)[0] == package:
            continue
        line = 'import ' + fqn + ';'
        imports = [i for i, l in enumerate(lines) if l.startswith('import ') and not l.startswith('import static')]
        before = [i for i in imports if lines[i] < line]
        lines.insert(before[-1] + 1 if before else imports[0], line)
    src = '\n'.join(lines)
    for simple in ('EffectSlot', 'LegacySlots'):
        if not uses(src, simple):
            src = re.sub(r'^import ' + re.escape(MODEL) + r'\.' + simple + r';\n', '', src, flags=re.M)
    return src


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('--scan', required=True)
    parser.add_argument('dirs', nargs='+')
    args = parser.parse_args()
    sacrificed, haunt = load_scan(args.scan)
    leftover = re.compile(SLOT)
    for directory in args.dirs:
        for path in sorted(pathlib.Path(directory).rglob('*.java')):
            src = path.read_text()
            if not re.search(r'\b(' + '|'.join(PILOT) + r')\b', src):
                continue
            converted = convert(src, path.stem, sacrificed, haunt)
            if converted != src:
                converted = fix_imports(converted)
                path.write_text(converted)
                print('changed', path)
            for m in leftover.finditer(converted):
                line = converted.count('\n', 0, m.start()) + 1
                print(f'NOT CONVERTED {path}:{line}', file=sys.stderr)


if __name__ == '__main__':
    main()
