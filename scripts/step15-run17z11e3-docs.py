#!/usr/bin/env python3
"""Step 15, run 17z11e3: Giant Oyster's rows in agent-docs. Idempotent; fails if a doc still names a removed form
outside a "Gone" line."""
import pathlib
import re
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
DOCS = root / 'agent-docs'
ACTIVATION = ('`{T}` ability `TapPermanentsEffect(TapUntapScope.TARGET)` + `DoesntUntapEffect.targetWhileSourceTapped()` + '
              '`RegisterDelayedDrawStepTriggerWhileSourceTappedEffect(new PutCounterOnTargetPermanentEffect('
              'CounterType.MINUS_ONE_MINUS_ONE, 1))` + `RegisterDelayedSourceUntapsOrLeavesTriggerEffect(new '
              'RemoveAllCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, CounterRemovalSubject.TARGET))`: the activation '
              'creates both delayed triggered abilities, the activator\'s (CR 603.7e) (Giant Oyster)')
GONE = ('**Gone** (effect-slot-refactor.md step 15, run 17z11e3): Giant Oyster\'s activation creates its draw-step '
        'counters and its counter removal as delayed triggered abilities '
        '(`RegisterDelayedDrawStepTriggerWhileSourceTappedEffect`, `RegisterDelayedSourceUntapsOrLeavesTriggerEffect`)')


def rewrite(name, fn):
    path = DOCS / name
    text = path.read_text()
    new = fn(text)
    if new != text:
        path.write_text(new)
        print('updated', name)


def line_sub(pattern, repl):
    def fn(text):
        return '\n'.join(repl(line) if re.search(pattern, line) else line for line in text.split('\n'))
    return fn


def map_row(line):
    cells = line.split(' | ')
    return cells[0] + ' | ' + ACTIVATION + ' |' if 'RegisterDelayedDrawStep' not in line else line


rewrite('ORACLE_TEXT_EFFECT_MAP.md', line_sub(r'For as long as CARDNAME remains tapped, target tapped creature', map_row))


def pattern_row(line):
    if 'RegisterDelayedDrawStep' in line:
        return line
    cells = line.split(' | ')
    cells[2] = ('MayNotUntapDuringUntapStepEffect (STATIC) + ' + ACTIVATION)
    return ' | '.join(cells[:3]) + ' |'


rewrite('CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md', line_sub(r'g/GiantOyster\.java', pattern_row))

QUICK_NEW = [
    '- `RegisterDelayedDrawStepTriggerWhileSourceTappedEffect(CardEffect)` — "for as long as this remains tapped, at the '
    'beginning of each of your draw steps, [effect] on that creature": creates a repeating delayed triggered ability of '
    'the activator (CR 603.7e) for the ability\'s target; nothing when the source has untapped or left since activation; '
    'ends when the source untaps or leaves; its entry aims the effect at the creature without targeting it; Giant Oyster',
    '- `RegisterDelayedSourceUntapsOrLeavesTriggerEffect(CardEffect)` — "when this leaves the battlefield or becomes '
    'untapped, [effect] on the creature": creates a one-shot delayed triggered ability of the activator for the ability\'s '
    'target, triggered by `UntapLockReleaseService` the next time the source untaps or leaves (CR 603.7a); Giant Oyster',
]


def quick(text):
    lines = text.split('\n')
    out = []
    for line in lines:
        if line.startswith('- `PutCountersOnUntapLockedPermanentsEffect('):
            out.append('- `PutCountersOnUntapLockedPermanentsEffect` — ' + GONE)
            out.extend(QUICK_NEW)
        elif line.startswith('- `RemoveCountersWhenUntapLockEndsEffect('):
            out.append('- `RemoveCountersWhenUntapLockEndsEffect` — ' + GONE)
        else:
            out.append(line)
    return '\n'.join(out)


rewrite('EFFECTS_QUICK_REFERENCE.md', quick)

INDEX_NEW = [
    '| `RegisterDelayedDrawStepTriggerWhileSourceTappedEffect` | `(CardEffect triggerEffect)` | Declares no target: '
    'creates a `DelayedDrawStepTriggerWhileSourceTapped` for the resolving ability\'s target, its controller the '
    'ability\'s (CR 603.7e), unless the source has untapped or left since activation. `StepTriggerService` puts it on '
    'the stack at the beginning of each of that player\'s draw steps, aimed at the creature without targeting it; '
    '`UntapLockReleaseService` ends it when the source untaps or leaves (Giant Oyster). |',
    '| `RegisterDelayedSourceUntapsOrLeavesTriggerEffect` | `(CardEffect triggerEffect)` | Declares no target: creates '
    'a `DelayedSourceUntapsOrLeavesTrigger` for the resolving ability\'s target while the source is on the battlefield, '
    'tapped or not (CR 603.7a); `UntapLockReleaseService` puts it on the stack the next time the source untaps or '
    'leaves, aimed at the creature without targeting it (Giant Oyster). |',
]


def index(text):
    out = []
    for line in text.split('\n'):
        if line.startswith('| `PutCountersOnUntapLockedPermanentsEffect` |') and 'Gone' not in line:
            out.append('| `PutCountersOnUntapLockedPermanentsEffect` | — | ' + GONE + ' |')
            out.extend(INDEX_NEW)
        elif line.startswith('| `RemoveCountersWhenUntapLockEndsEffect` |') and 'Gone' not in line:
            out.append('| `RemoveCountersWhenUntapLockEndsEffect` | — | ' + GONE + ' |')
        else:
            out.append(line)
    return '\n'.join(out)


rewrite('EFFECTS_INDEX.md', index)

bad = []
for path in list(DOCS.glob('*.md')) + list((root / '.claude/skills').rglob('*.md')):
    for n, line in enumerate(path.read_text().split('\n'), 1):
        if re.search(r'PutCountersOnUntapLockedPermanentsEffect|RemoveCountersWhenUntapLockEndsEffect|untapLockActivator',
                     line) and 'Gone' not in line and 'EFFECT_MERGE_PLAN' not in path.name:
            bad.append(f'{path}:{n}')
if bad:
    sys.exit('still named: ' + ', '.join(bad))
