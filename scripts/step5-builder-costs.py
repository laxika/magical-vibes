#!/usr/bin/env python3
"""EffectSlot refactor step 5: lifts casting costs out of target-builder chains.

Usage: step5-builder-costs.py DIR...

``step5-spell-ability.py`` turns ``target(f).addEffect(EffectSlot.SPELL, cost)`` into
``target(f).addCastingCost(cost)``, but a casting cost is not a target binding and the builder has
no such method. This rewrites ``target(f)\\n    .addCastingCost(cost)\\n    .addSpellEffect(e);`` into
``addCastingCost(cost);\\n    target(f)\\n    .addSpellEffect(e);``. Prints every file it changed and every
chained cost it could not lift.
"""
import pathlib
import re
import sys

CHAINED = re.compile(r'(?:\n([ \t]*)|(?<=\)))\.addCastingCost\(')


def balanced_end(src, open_paren):
    depth = 0
    for i in range(open_paren, len(src)):
        if src[i] == '(':
            depth += 1
        elif src[i] == ')':
            depth -= 1
            if depth == 0:
                return i + 1
    raise ValueError('unbalanced')


def lift(src, path):
    while True:
        m = CHAINED.search(src)
        if not m:
            return src
        end = balanced_end(src, m.end() - 1)
        cost = src[m.end():end - 1]
        statement_start = src.rfind(';', 0, m.start())
        statement_start = max(statement_start, src.rfind('{', 0, m.start()))
        head = src[statement_start + 1:m.start()]
        t = re.search(r'\n([ \t]*)((?://[^\n]*\n[ \t]*)*)((?:\w+\.)?)target\w*\(', head)
        if not t:
            print(f'REPORT {path}: chained cost without a target builder')
            return src
        indent = t.group(1)
        # The cost moves to the statement's indent, and its continuation lines to one level deeper.
        continuation = [len(line) - len(line.lstrip(' ')) for line in cost.split('\n')[1:] if line.strip()]
        shift = min(continuation) - len(indent) - 8 if continuation else 0
        cost = re.sub(r'\n' + ' ' * shift, '\n', cost) if shift > 0 else cost
        target_at = statement_start + 1 + t.start(2) + len(t.group(2))
        src = (src[:target_at] + t.group(3) + 'addCastingCost(' + cost + ');\n' + indent + src[target_at:m.start()]
               + src[end:])


def main(dirs):
    for d in dirs:
        for path in sorted(pathlib.Path(d).rglob('*.java')):
            src = path.read_text(encoding='utf-8')
            if '.addCastingCost(' not in src:
                continue
            new = lift(src, path)
            if new != src:
                path.write_text(new, encoding='utf-8')
                print(path)


if __name__ == '__main__':
    main(sys.argv[1:])
