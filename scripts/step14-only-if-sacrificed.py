#!/usr/bin/env python3
"""Effect-slot refactor step 14 run 16a: drops the `onlyIfSacrificed` record components.

"When you sacrifice this" is the trigger's cause now (`Triggers.sacrificed(...)`), so the effect flag
says nothing. For each record the table gives the constructor arities that took the flag and its
argument index; every `new X(...)` call of such an arity, and every `this(...)` call inside X's own
file, loses that argument. The argument must be `false`, the parameter `onlyIfSacrificed` itself or an
`….onlyIfSacrificed()` read (Biolume Egg's `true` is allowed for the transform effect: its trigger is
`Triggers.sacrificed(THIS)`); anything else is reported and left alone. The cards' `X.sacrificeOnly(…)`
factories become the plain constructors they equal without the flag.

The record declarations themselves (parameters, the overloads that become duplicates, the
`onlyTriggersOnSacrifice()` overrides and the factories) are edited by hand afterwards.

Usage: step14-only-if-sacrificed.py [--dry-run]
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# record -> {original arity: flag index}
TABLE = {
    'DrawCardEffect': {2: 1, 3: 1, 4: 1},
    'GainLifeEffect': {4: 3},
    'DiscardEffect': {7: 4},
    'DealDamageToAnyTargetEffect': {6: 5, 7: 5, 8: 5, 9: 5, 10: 5},
    'SearchLibraryEffect': {11: 10, 14: 11, 15: 11, 17: 11, 18: 11, 19: 11, 20: 11, 21: 11},
    'SequenceEffect': {3: 2, 4: 2, 5: 2},
    'RegisterDelayedReturnSourceTransformedEffect': {2: 0},
}
ALLOW_TRUE = {'RegisterDelayedReturnSourceTransformedEffect'}

FACTORIES = [
    (r'DrawCardEffect\.sacrificeOnly\(', 'new DrawCardEffect('),
    (r'GainLifeEffect\.sacrificeOnly\(', 'new GainLifeEffect('),
    (r'DealDamageToAnyTargetEffect\.sacrificeOnly\(', 'new DealDamageToAnyTargetEffect('),
    (r'SearchLibraryEffect\.sacrificeOnly\(', 'new SearchLibraryEffect('),
    (r'SequenceEffect\.sacrificeOnly\(', 'SequenceEffect.of('),
]
# DiscardEffect.sacrificeOnly(n) is "target player discards n"
DISCARD_FACTORY = re.compile(r'DiscardEffect\.sacrificeOnly\(([^()]*)\)')

FLAG_ARG = re.compile(r'^(false|onlyIfSacrificed|[\w.()]*\.onlyIfSacrificed\(\))$')


def split_args(s, i):
    """Splits the argument list starting just after '(' at s[i]; returns (spans, index of ')')."""
    depth = 0
    spans = []
    start = i
    j = i
    in_str = None
    while j < len(s):
        ch = s[j]
        if in_str:
            if ch == '\\':
                j += 2
                continue
            if ch == in_str:
                in_str = None
        elif ch in '"\'':
            in_str = ch
        elif ch in '([{':
            depth += 1
        elif ch in ')]}':
            if depth == 0 and ch == ')':
                if s[start:j].strip():
                    spans.append((start, j))
                return spans, j
            depth -= 1
        elif ch == ',' and depth == 0:
            spans.append((start, j))
            start = j + 1
        j += 1
    return None, None


def drop_flags(s, path, report):
    base = os.path.basename(path)[:-5]
    edits = []
    for name, arities in TABLE.items():
        patterns = [r'\bnew\s+' + re.escape(name) + r'\s*\(']
        if base == name:
            patterns.append(r'\bthis\s*\(')
        for pattern in patterns:
            for m in re.finditer(pattern, s):
                spans, _ = split_args(s, m.end())
                if spans is None or len(spans) not in arities:
                    continue
                index = arities[len(spans)]
                arg = s[spans[index][0]:spans[index][1]].strip()
                if not FLAG_ARG.match(arg) and not (arg == 'true' and name in ALLOW_TRUE):
                    report.append(f'{path}: {name}/{len(spans)} flag argument {arg!r} left alone')
                    continue
                if index + 1 < len(spans):
                    # drop "arg," up to the next argument (and, for the first one, the space before it)
                    end = spans[index + 1][0]
                    while index == 0 and s[end].isspace():
                        end += 1
                    edits.append((spans[index][0], end))
                else:
                    # last argument: drop ", arg"
                    edits.append((spans[index - 1][1], spans[index][1]))
    for start, end in sorted(edits, reverse=True):
        s = s[:start] + s[end:]
    return s, len(edits)


def main():
    dry = '--dry-run' in sys.argv
    report = []
    changed = 0
    for dirpath, dirnames, files in os.walk(ROOT):
        dirnames[:] = [d for d in dirnames if d not in ('build', 'node_modules', '.git', '.gradle', 'scripts')]
        for f in files:
            if not f.endswith('.java'):
                continue
            path = os.path.join(dirpath, f)
            with open(path) as fh:
                s = fh.read()
            orig = s
            if 'magical-vibes-card' + os.sep in path:
                for pattern, replacement in FACTORIES:
                    s = re.sub(pattern, replacement, s)
                s = DISCARD_FACTORY.sub(lambda m: 'new DiscardEffect(' + m.group(1) + ', DiscardRecipient.TARGET_PLAYER)', s)
                if s != orig and 'DiscardRecipient.TARGET_PLAYER' in s and 'import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;' not in s \
                        and 'DiscardRecipient;' not in s:
                    s = s.replace('import com.github.laxika.magicalvibes.model.effect.DiscardEffect;',
                                  'import com.github.laxika.magicalvibes.model.effect.DiscardEffect;\n'
                                  'import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;')
            s, n = drop_flags(s, path, report)
            if s != orig:
                changed += 1
                print(('would change ' if dry else 'changed ') + os.path.relpath(path, ROOT) + f' ({n} calls)')
                if not dry:
                    with open(path, 'w') as fh:
                        fh.write(s)
    for line in report:
        print('REPORT', line)
    print(f'{changed} files')


if __name__ == '__main__':
    main()
