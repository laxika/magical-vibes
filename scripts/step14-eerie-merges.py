#!/usr/bin/env python3
"""Effect-slot refactor step 14 run 16a: the Eerie cards' two abilities become one.

"Eerie — Whenever an enchantment you control enters and whenever you fully unlock a Room, [effect]" is one triggered
ability with two trigger conditions (CR 603.3: each time it triggers it is one stack object of that ability). The
cards wrote it as two abilities sharing the effect:

    addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), E);
    addTrigger(Triggers.youFullyUnlockRoom(), E);
        -> addTrigger(List.of(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                Triggers.youFullyUnlockRoom()), E);

and, for the graveyard form (Fear of Infinity), two `TriggerCondition`s of `AbilityZones.GRAVEYARD`, which become one
`TriggeredAbility` of both conditions. The two calls must be next to each other and their effect arguments equal as
text. Running it again changes nothing.

Usage: step14-eerie-merges.py [--dry-run]
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CARDS = os.path.join(ROOT, 'magical-vibes-card')

ENTERS = 'Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL)'
UNLOCK = 'Triggers.youFullyUnlockRoom()'
PAIR = re.compile(
    r'(?P<indent>[ \t]*)addTrigger\(' + re.escape(ENTERS) + r',\s*(?P<effect>.+?)\);\n'
    r'(?P=indent)addTrigger\(' + re.escape(UNLOCK) + r',\s*(?P<effect2>.+?)\);\n',
    re.S)

GRAVEYARD_OLD = '''        addTrigger(new TriggerCondition(Triggers.enters(Triggers.ENCHANTMENT,
                ObjectRelation.YOU_CONTROL), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                returnToHand);
        addTrigger(new TriggerCondition(Triggers.youFullyUnlockRoom(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, returnToHand);
'''
GRAVEYARD_NEW = '''        addTriggeredAbility(new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL),
                        AbilityZones.GRAVEYARD),
                new TriggerCondition(Triggers.youFullyUnlockRoom(), AbilityZones.GRAVEYARD)),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(returnToHand))));
'''


def normalize(text):
    return re.sub(r'\s+', ' ', text).strip()


def add_import(s, imp):
    line = 'import ' + imp + ';'
    if line in s:
        return s
    imports = [m for m in re.finditer(r'^import [\w.]+;$', s, re.M)]
    for m in imports:
        if m.group(0) > line:
            return s[:m.start()] + line + '\n' + s[m.start():]
    last = imports[-1]
    gap = '\n' if imp.startswith('java.') and not last.group(0).startswith('import java.') else ''
    return s[:last.end()] + '\n' + gap + line + s[last.end():]


def convert(s):
    def merge(m):
        if normalize(m.group('effect')) != normalize(m.group('effect2')):
            return m.group(0)
        indent = m.group('indent')
        return (f'{indent}addTrigger(List.of({ENTERS},\n{indent}        {UNLOCK}), {m.group("effect").strip()});\n')
    new = PAIR.sub(merge, s)
    if new != s:
        new = add_import(new, 'java.util.List')
    if GRAVEYARD_OLD in new:
        new = new.replace(GRAVEYARD_OLD, GRAVEYARD_NEW)
        new = add_import(new, 'com.github.laxika.magicalvibes.model.TriggeredAbility')
        new = add_import(new, 'com.github.laxika.magicalvibes.model.EffectRegistration')
        new = add_import(new, 'java.util.List')
    return new


def main():
    dry = '--dry-run' in sys.argv
    changed = []
    left = []
    for dirpath, dirnames, files in os.walk(CARDS):
        dirnames[:] = [d for d in dirnames if d != 'build']
        for f in files:
            if not f.endswith('.java'):
                continue
            path = os.path.join(dirpath, f)
            s = open(path).read()
            if UNLOCK not in s:
                continue
            new = convert(s)
            if new != s:
                changed.append(f[:-5])
                if not dry:
                    open(path, 'w').write(new)
            elif 'List.of(' + ENTERS not in new and 'TriggerCondition(Triggers.youFullyUnlockRoom()' not in new:
                left.append(f[:-5])
    for name in sorted(changed):
        print(('would merge ' if dry else 'merged ') + name)
    for name in sorted(left):
        print('LEFT', name)


if __name__ == '__main__':
    main()
