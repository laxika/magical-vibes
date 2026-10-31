#!/usr/bin/env python3
"""EffectSlot refactor step 7: list a former ON_TAP mana ability after the card's other battlefield abilities.

Usage: step7-mana-ability-last.py CARD_FILE...

``step7-activated-abilities.py`` turned ``addEffect(EffectSlot.ON_TAP, …)`` into
``addActivatedAbility(ManaAbilities.tapFor(…))`` where the registration was, usually first in the constructor. The
old engine kept that mana outside the activated ability list, so every other ability's index (which tests, the AI
and the client use) counted from 0 without it. This moves each ``addActivatedAbility(ManaAbilities.tapFor(…));``
statement, with the comment lines right above it, to after the last battlefield ``addActivatedAbility(…)`` statement
of the constructor, so those indices don't change. Ability order is not a rules concept. Abilities added with a zone
(``addActivatedAbility(AbilityZones.…, …)``) are in other zones' lists and don't count.
"""
import re
import sys

MANA = re.compile(r'^([ \t]*(?://[^\n]*\n[ \t]*)*)addActivatedAbility\(ManaAbilities\.tapFor\([^;]*\)\);[ \t]*\n', re.M)


def statement_end(src, start):
    """Index just past the ';' that ends the call statement starting at start."""
    depth = 0
    i = src.index('(', start)
    while True:
        c = src[i]
        if c == '"':
            i += 1
            while src[i] != '"':
                i += 2 if src[i] == '\\' else 1
        elif c == '(':
            depth += 1
        elif c == ')':
            depth -= 1
            if depth == 0:
                end = src.index(';', i) + 1
                return src.index('\n', end) + 1
        i += 1


def move(src):
    mana = list(MANA.finditer(src))
    if not mana:
        return src
    others = [m for m in re.finditer(r'^[ \t]*addActivatedAbility\((?!ManaAbilities\.tapFor|AbilityZones\.)', src, re.M)]
    if not others:
        return src
    last_end = statement_end(src, others[-1].start())
    if all(m.start() >= last_end for m in mana):
        return src
    block = ''.join(m.group(0) for m in mana)
    out = src
    for m in reversed(mana):
        out = out[:m.start()] + out[m.end():]
    removed_before = sum(len(m.group(0)) for m in mana if m.start() < last_end)
    insert_at = last_end - removed_before
    return out[:insert_at] + block + out[insert_at:]


def main(paths):
    for path in paths:
        with open(path, encoding='utf-8') as f:
            src = f.read()
        out = move(src)
        if out != src:
            with open(path, 'w', encoding='utf-8') as f:
                f.write(out)
            print(f'changed {path}')


if __name__ == '__main__':
    main(sys.argv[1:])
