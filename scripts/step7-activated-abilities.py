#!/usr/bin/env python3
"""EffectSlot refactor step 7: activated and mana abilities in one list.

Usage: step7-activated-abilities.py [--readers] DIR...

Rewrites every .java file under each DIR (run it per module, per letter module for cards):

- ``addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.X))`` becomes
  ``addActivatedAbility(ManaAbilities.tapFor(ManaColor.X))``, and ``new AwardManaEffect(ManaColor.X, n)``
  becomes ``ManaAbilities.tapFor(ManaColor.X, n)``: a "{T}: Add …" mana ability (CR 605.1a).
- ``addGraveyardActivatedAbility(x)``, ``addHandActivatedAbility(x)`` and ``addStackActivatedAbility(x)``
  become ``addActivatedAbility(AbilityZones.GRAVEYARD, x)`` (``HAND``, ``STACK``).
- ``.withExileOnly()`` becomes ``.withZones(AbilityZones.EXILE)``.
- In the twelve cards whose only tap mana is what their basic land type gives (``INTRINSIC_ONLY``, from their
  Scryfall type lines: the basics, the snow basics, Dryad Arbor and Gobland), the mana ability is not printed
  at all: a land's basic land type gives it that ability (CR 305.6), and the engine adds it after the
  permanent's printed and granted abilities.

With ``--readers`` it also rewrites the readers of the old per-zone lists:

- ``getActivatedAbilities()`` becomes ``getActivatedAbilities(Zone.BATTLEFIELD)``: the old list held the
  battlefield's abilities. ``getGraveyardActivatedAbilities()``, ``getHandActivatedAbilities()`` and
  ``getStackActivatedAbilities()`` become ``getActivatedAbilities(Zone.GRAVEYARD)`` (``HAND``, ``STACK``).
- ``x.getActivatedAbilities().add(a)`` becomes ``x.addActivatedAbility(a)``.
- ``harness.sacrificePermanent(player, index, target)`` becomes
  ``harness.activateAbility(player, index, null, target)``: the only "sacrifice this" ability that used
  the dedicated action, Aura of Silence's, is now its first activated ability.

Cards are never run with ``--readers``: no card reads its own lists. Imports that a rewrite needs are
added, and an ``EffectSlot`` or ``AwardManaEffect`` import left unused is removed. Prints every file it
changed and every ``ON_TAP`` site it could not convert.
"""
import pathlib
import re
import sys

MODEL = 'com.github.laxika.magicalvibes.model'

SLOT_PREFIX = r'(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\.'
ON_TAP_SINGLE = re.compile(
    r'addEffect\(\s*' + SLOT_PREFIX + r'ON_TAP\s*,\s*new AwardManaEffect\(\s*(ManaColor\.[A-Z]+)\s*\)\s*\)')
ON_TAP_AMOUNT = re.compile(
    r'addEffect\(\s*' + SLOT_PREFIX + r'ON_TAP\s*,\s*new AwardManaEffect\(\s*(ManaColor\.[A-Z]+)\s*,\s*(\d+)\s*\)\s*\)')
ZONED_ADDERS = {
    'addGraveyardActivatedAbility': 'GRAVEYARD',
    'addHandActivatedAbility': 'HAND',
    'addStackActivatedAbility': 'STACK',
}
INTRINSIC_ONLY = {
    'Plains', 'Island', 'Swamp', 'Mountain', 'Forest',
    'SnowCoveredPlains', 'SnowCoveredIsland', 'SnowCoveredSwamp', 'SnowCoveredMountain', 'SnowCoveredForest',
    'DryadArbor', 'Gobland',
}
COLOR_CODES = {'WHITE': 'W', 'BLUE': 'U', 'BLACK': 'B', 'RED': 'R', 'GREEN': 'G'}
ZONED_READERS = {
    'getGraveyardActivatedAbilities': 'GRAVEYARD',
    'getHandActivatedAbilities': 'HAND',
    'getStackActivatedAbilities': 'STACK',
}


def convert(src, readers, class_name=None):
    src = ON_TAP_SINGLE.sub(r'addActivatedAbility(ManaAbilities.tapFor(\1))', src)
    if class_name in INTRINSIC_ONLY:
        src = re.sub(r'addActivatedAbility\(ManaAbilities\.tapFor\(ManaColor\.([A-Z]+)\)\);',
                     lambda m: '// {T}: Add {' + COLOR_CODES[m.group(1)] + '} comes from its basic land type (CR 305.6).',
                     src)
    src = ON_TAP_AMOUNT.sub(r'addActivatedAbility(ManaAbilities.tapFor(\1, \2))', src)
    for adder, zone in ZONED_ADDERS.items():
        src = re.sub(r'\b' + adder + r'\(', f'addActivatedAbility(AbilityZones.{zone}, ', src)
    src = re.sub(r'\.withExileOnly\(\)', '.withZones(AbilityZones.EXILE)', src)
    if readers:
        src = re.sub(r'\.getActivatedAbilities\(\)\.add\(', '.addActivatedAbility(', src)
        src = re.sub(r'\bharness\.sacrificePermanent\(([^,()]+),\s*([^,()]+),',
                     r'harness.activateAbility(\1, \2, null,', src)
        src = re.sub(r'\bgetActivatedAbilities\(\)', 'getActivatedAbilities(Zone.BATTLEFIELD)', src)
        for reader, zone in ZONED_READERS.items():
            src = re.sub(r'\b' + reader + r'\(\)', f'getActivatedAbilities(Zone.{zone})', src)
    return src


def fix_imports(src):
    package = re.search(r'^package ([\w.]+);', src, re.M).group(1)
    body = re.sub(r'^\s*import .*$', '', src, flags=re.M)
    wanted = []
    for name in ('ManaAbilities', 'AbilityZones', 'Zone'):
        if re.search(r'(?<![\w.])' + name + r'\b', body) and package != MODEL:
            wanted.append(f'{MODEL}.{name}')
    for fq in wanted:
        simple = fq.rsplit('.', 1)[1]
        if re.search(r'^import [\w.]+\.' + simple + r';', src, re.M):
            continue
        if re.search(r'^import ' + re.escape(fq.rsplit('.', 1)[0]) + r'\.\*;', src, re.M):
            continue
        src = insert_import(src, fq)
    for fq in (f'{MODEL}.EffectSlot', f'{MODEL}.effect.AwardManaEffect', f'{MODEL}.ManaAbilities',
               f'{MODEL}.ManaColor'):
        simple = fq.rsplit('.', 1)[1]
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
    first = imports[0]
    return src[:first.start()] + line + '\n' + src[first.start():]


def main(argv):
    readers = '--readers' in argv
    dirs = [a for a in argv if not a.startswith('--')]
    changed = 0
    for d in dirs:
        for path in sorted(pathlib.Path(d).rglob('*.java')):
            src = path.read_text(encoding='utf-8')
            out = convert(src, readers, path.stem)
            if out == src:
                continue
            out = fix_imports(out)
            path.write_text(out, encoding='utf-8')
            changed += 1
            print(f'changed {path}')
            if re.search(SLOT_PREFIX + r'ON_TAP\b', out):
                print(f'UNCONVERTED ON_TAP {path}')
    print(f'{changed} files changed', file=sys.stderr)


if __name__ == '__main__':
    main(sys.argv[1:])
