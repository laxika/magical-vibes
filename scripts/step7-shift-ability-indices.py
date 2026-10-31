#!/usr/bin/env python3
"""EffectSlot refactor step 7: shift the activated-ability indices in a converted card's own test class.

Usage: step7-shift-ability-indices.py TEST_FILE...

A card whose ``ON_TAP`` mana came before its other activated abilities now lists its "{T}: Add …" ability
first, so each of its other abilities moved up one index. In each given test file this rewrites:

- ``harness.activateAbility(player, permanent, xValue, target)``, which used ability 0, to
  ``harness.activateAbility(player, permanent, 1, xValue, target)``, and the same with a trailing zone;
- an explicit integer-literal ability index ``N`` to ``N + 1`` in ``activateAbility``,
  ``activateAbilityOfPermanent``, ``activateAbilityWithDamageAssignments``, ``activateAbilityWithMultiTargets``
  and ``activateAbilityWithGraveyardTargets``;
- ``getActivatedAbilities(Zone.BATTLEFIELD).get(N)`` and ``getEffectiveActivatedAbilities(…).get(N)`` to
  ``get(N + 1)``.

It assumes every activation in the file is of the card under test; run the tests afterwards and fix by hand the
calls that aimed at another permanent.
"""
import re
import sys

INDEXED = ('activateAbilityOfPermanent', 'activateAbilityWithDamageAssignments',
           'activateAbilityWithMultiTargets', 'activateAbilityWithGraveyardTargets')


def split_args(src, open_index):
    """Returns (args, close_index) for the call whose '(' is at open_index."""
    depth = 0
    args = []
    start = open_index + 1
    i = open_index
    while i < len(src):
        c = src[i]
        if c in '([{':
            depth += 1
        elif c in ')]}':
            depth -= 1
            if depth == 0:
                args.append(src[start:i])
                return args, i
        elif c == ',' and depth == 1:
            args.append(src[start:i])
            start = i + 1
        elif c == '"':
            i += 1
            while src[i] != '"':
                i += 2 if src[i] == '\\' else 1
        i += 1
    raise ValueError('unbalanced call')


def bump(arg):
    m = re.fullmatch(r'(\s*)(\d+)(\s*)', arg)
    return f'{m.group(1)}{int(m.group(2)) + 1}{m.group(3)}' if m else None


def rewrite(src):
    out = []
    pos = 0
    pattern = re.compile(r'\b(activateAbility|' + '|'.join(INDEXED) + r')\(')
    for m in pattern.finditer(src):
        if m.start() < pos:
            continue
        name = m.group(1)
        args, close = split_args(src, m.end() - 1)
        new_args = list(args)
        if name == 'activateAbility':
            # (player, permanent, xValue, target, zone) when the fourth argument is an id or the fifth a zone.
            zoned = len(args) == 5 and ('Zone' in args[4] or re.search(r'Id\b|getId\(\)', args[3]) is not None)
            if len(args) == 4 or zoned:
                new_args.insert(2, ' 1')
            elif len(args) in (5, 6):
                bumped = bump(args[2])
                if bumped is None:
                    continue
                new_args[2] = bumped
            else:
                continue
        else:
            bumped = bump(args[2]) if len(args) > 2 else None
            if bumped is None:
                continue
            new_args[2] = bumped
        out.append(src[pos:m.end() - 1])
        out.append('(' + ','.join(new_args) + ')')
        pos = close + 1
    out.append(src[pos:])
    src = ''.join(out)
    src = re.sub(r'(getActivatedAbilities\(Zone\.BATTLEFIELD\)\.get\()(\d+)\)',
                 lambda m: f'{m.group(1)}{int(m.group(2)) + 1})', src)
    src = re.sub(r'(getEffectiveActivatedAbilities\([^()]*\)\.get\()(\d+)\)',
                 lambda m: f'{m.group(1)}{int(m.group(2)) + 1})', src)
    return src


def main(paths):
    for path in paths:
        with open(path, encoding='utf-8') as f:
            src = f.read()
        out = rewrite(src)
        if out != src:
            with open(path, 'w', encoding='utf-8') as f:
                f.write(out)
            print(f'changed {path}')


if __name__ == '__main__':
    main(sys.argv[1:])
