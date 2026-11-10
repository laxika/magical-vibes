#!/usr/bin/env python3
"""Run 17z14b (effect-slot-refactor.md step 15): the trigger collector service tests call the registry and the
collectors with an ability, not an effect.

`TriggerCollectorRegistry.dispatch(match, watch, ctx)` dispatches the ability `match` carries, a handler is
`(match, ctx) -> ...`, `DeathTriggerCollectorService.dispatchCreatureDeath(watch, match, ctx)` and
`queueDepartureAbility(match, card, ...)` take no effect: each call that passed the effect its match was built with
drops it. Fails if a call it would rewrite passes an effect other than its match's. Idempotent.

Usage: scripts/step15-run17z14b-service-tests.py [REPOSITORY_ROOT]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
TESTS = ROOT / 'magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/service/trigger'
FILES = ['MiscTriggerCollectorServiceTest.java', 'DeathTriggerCollectorServiceTest.java',
         'DamageTriggerCollectorServiceTest.java', 'LandTapTriggerCollectorServiceTest.java',
         'DiscardTriggerCollectorServiceTest.java', 'ScryTriggerCollectorServiceTest.java',
         'TriggerCollectorRegistryTest.java', 'EnterTriggerCollectorServiceTest.java',
         'SpellCastTriggerCollectorServiceTest.java']


def calls(text, name):
    """(start, end, args) of each call of `name(`, its arguments split at depth one."""
    for m in re.finditer(r'(?<![\w])' + re.escape(name) + r'\(', text):
        depth, j, cur, args, quote = 1, m.end(), m.end(), [], None
        while depth:
            c = text[j]
            if quote:
                if c == '\\':
                    j += 2
                    continue
                if c == quote:
                    quote = None
            elif c in '"\'':
                quote = c
            elif c in '([{':
                depth += 1
            elif c in ')]}':
                depth -= 1
                if depth == 0:
                    args.append(text[cur:j])
            elif c == ',' and depth == 1:
                args.append(text[cur:j])
                cur = j + 1
            j += 1
        yield m.start(), j, args


def match_effect(arg):
    """The effect a match argument is built with, or None when it isn't built inline."""
    arg = arg.strip()
    for name in ('match', 'new TriggerMatchContext'):
        for start, end, args in calls(arg, name):
            if start == 0 and end == len(arg):
                index = 2 if name == 'match' else 3
                return args[index].strip() if len(args) > index else None
    return None


def drop_argument(text, name, count, index, match_index):
    out, pos = [], 0
    for start, end, args in calls(text, name):
        if len(args) != count:
            continue
        effect = args[index].strip()
        built = match_effect(args[match_index])
        if built is not None and built != effect:
            raise SystemExit(f'{name} passes {effect} with a match of {built}')
        kept = args[:index] + args[index + 1:]
        out.append(text[pos:start])
        out.append(name + '(' + ','.join(kept) + ')')
        pos = end
    out.append(text[pos:])
    return ''.join(out)


def rewrite(text):
    text = drop_argument(text, 'registry.dispatch', 4, 2, 0)
    text = drop_argument(text, 'svc.dispatchCreatureDeath', 4, 2, 1)
    text = drop_argument(text, 'svc.queueDepartureAbility', 6, 1, 0)
    for name in sorted(set(re.findall(r'svc\.(handle\w+)\(', text))):
        text = drop_argument(text, 'svc.' + name, 3, 1, 0)
    for name in ('sut.collectCycling', 'sut.collectOpponentCausedDiscard'):
        text = drop_argument(text, name, 3, 1, 0)
    text = re.sub(r'\((\w+), dispatchedEffect, (context|ctx)\) ->', r'(\1, \2) ->', text)
    out, pos = [], 0
    for start, end, args in calls(text, 'registry.handles'):
        if len(args) == 2 and not args[1].strip().startswith('List.of('):
            out.append(text[pos:start])
            out.append('registry.handles(' + args[0] + ', List.of(' + args[1].strip() + '))')
            pos = end
    out.append(text[pos:])
    text = ''.join(out)
    text = re.sub(r'\((\w+), (?:inner|innerEffect|effect), (context|ctx)\) ->', r'(\1, \2) ->', text)
    # A context of no effect: an ability with no effects.
    text = re.sub(r'new TriggerMatchContext\(([^;()]*?), null\)', r'new TriggerMatchContext(\1, List.of())', text)
    # The match of a "you may" whose acceptance uses up "do this only once each turn".
    text = re.sub(r'new TriggerMatchContext\((\w+), (\w+), (\w+), (\w+), ([\w.()]+), true\)',
                  r'new TriggerMatchContext(\1, \2, \3, List.of(\4), \5, null, true)', text)
    if 'List.of(' in text and 'import java.util.List;' not in text:
        text = text.replace('import java.util.UUID;', 'import java.util.List;\nimport java.util.UUID;', 1)
    return text


for name in FILES:
    path = TESTS / name
    if not path.exists():
        continue
    text = path.read_text()
    new = rewrite(text)
    if new != text:
        path.write_text(new)
        print('rewrote', name)
