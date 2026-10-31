#!/usr/bin/env python3
"""EffectSlot refactor step 7: rewrite the agent-docs and skill snippets for the one activated ability list.

Usage: step7-docs.py FILE...

- ``addGraveyardActivatedAbility(x)``, ``addHandActivatedAbility(x)`` and ``addStackActivatedAbility(x)`` become
  ``addActivatedAbility(AbilityZones.GRAVEYARD, x)`` (``HAND``, ``STACK``); a bare method name in prose becomes
  ``addActivatedAbility(AbilityZones.GRAVEYARD, …)``.
- ``getGraveyardActivatedAbilities()`` and the others become ``getActivatedAbilities(Zone.GRAVEYARD)``.
- ``.withExileOnly()`` becomes ``.withZones(AbilityZones.EXILE)``.
- ``addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.X))`` becomes
  ``addActivatedAbility(ManaAbilities.tapFor(ManaColor.X))``.

Prints every line that still names ``ON_TAP`` or ``ON_SACRIFICE``; those are prose and are edited by hand.
"""
import re
import sys

ZONES = {'Graveyard': 'GRAVEYARD', 'Hand': 'HAND', 'Stack': 'STACK'}


def convert(text):
    for name, zone in ZONES.items():
        text = re.sub(r'\badd' + name + r'ActivatedAbility\(\)', f'addActivatedAbility(AbilityZones.{zone}, …)', text)
        text = re.sub(r'\badd' + name + r'ActivatedAbility\(', f'addActivatedAbility(AbilityZones.{zone}, ', text)
        text = re.sub(r'\badd' + name + r'ActivatedAbility\b', f'addActivatedAbility(AbilityZones.{zone}, …)', text)
        text = re.sub(r'\bget' + name + r'ActivatedAbilities\(\)', f'getActivatedAbilities(Zone.{zone})', text)
    text = text.replace('.withExileOnly()', '.withZones(AbilityZones.EXILE)')
    text = re.sub(r'addEffect\((?:EffectSlot\.)?ON_TAP, new AwardManaEffect\((ManaColor\.[A-Z]+)\)\)',
                  r'addActivatedAbility(ManaAbilities.tapFor(\1))', text)
    return text


def main(paths):
    for path in paths:
        with open(path, encoding='utf-8') as f:
            src = f.read()
        out = convert(src)
        if out != src:
            with open(path, 'w', encoding='utf-8') as f:
                f.write(out)
            print(f'changed {path}')
        for number, line in enumerate(out.splitlines(), 1):
            if re.search(r'\bON_TAP\b|\bON_SACRIFICE\b', line):
                print(f'{path}:{number}: {line[:160]}')


if __name__ == '__main__':
    main(sys.argv[1:])
