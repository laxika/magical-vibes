#!/usr/bin/env python3
"""EffectSlot refactor step 11: rewrites agent-docs for the creature-death slots that left EffectSlot.

Usage: step11-docs.py FILE...

Each line that names one of the slots gets the trigger the slot became, with the relation its own quoted Oracle
wording gives (step11-death-classify.py's rules; "a creature you control" when the line quotes none):

- ``addEffect(EffectSlot.<slot>, x)`` becomes ``addTrigger(<trigger>, x)``;
- any other mention, such as the tables' ``<slot> x(...)`` shorthand, becomes the trigger in short form:
  ``Triggers.creatureDies(ANOTHER_YOU_CONTROL)``; a graveyard slot becomes
  ``TriggerCondition(Triggers.creatureDies(...), GRAVEYARD)``.

Every rewritten line is printed with its file and line number for a person to check, and so is every line that
names one of the deleted "one or more" wrapper effects.
"""
import importlib.util
import pathlib
import re
import sys

HERE = pathlib.Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('classify', HERE / 'step11-death-classify.py')
classify = importlib.util.module_from_spec(spec)
spec.loader.exec_module(classify)

KIND = {
    'ON_ALLY_CREATURE_DIES': 'creature', 'ON_ANY_CREATURE_DIES': 'creature', 'ON_OPPONENT_CREATURE_DIES': 'creature',
    'ON_EQUIPPED_CREATURE_DIES': 'creature', 'ON_DAMAGED_CREATURE_DIES': 'creature',
    'GRAVEYARD_ON_ALLY_CREATURE_DIES': 'creature', 'GRAVEYARD_ON_OPPONENT_CREATURE_DIES': 'creature',
    'ON_ALLY_NONTOKEN_CREATURE_DIES': 'nontoken', 'ON_ANY_NONTOKEN_CREATURE_DIES': 'nontoken',
    'ON_ENCHANTED_PLAYER_NONTOKEN_CREATURE_DIES': 'nontoken',
    'ON_ALLY_CREATURE_OR_PLANESWALKER_DIES': 'creatureOrPlaneswalker',
    'ON_ALLY_ARTIFACT_OR_CREATURE_DIES': 'artifactOrCreature',
    'ON_HAUNTED_CREATURE_DIES': 'any',
}
DEFAULT = {
    'ON_ALLY_CREATURE_DIES': 'YOU_CONTROL', 'ON_ANY_CREATURE_DIES': 'ANY', 'ON_ALLY_NONTOKEN_CREATURE_DIES': 'YOU_CONTROL',
    'ON_ANY_NONTOKEN_CREATURE_DIES': 'ANY', 'ON_ALLY_CREATURE_OR_PLANESWALKER_DIES': 'YOU_CONTROL',
    'ON_ALLY_ARTIFACT_OR_CREATURE_DIES': 'YOU_CONTROL', 'GRAVEYARD_ON_ALLY_CREATURE_DIES': 'YOU_CONTROL',
}
SLOT = re.compile(r'(?:EffectSlot\.)?\b(' + '|'.join(sorted(KIND, key=len, reverse=True)) + r')\b')
ADD = re.compile(r'\baddEffect\(\s*(?:EffectSlot\.)?(' + '|'.join(KIND) + r')\s*,\s*')
WRAPPERS = re.compile(r'OneOrMore(?:ArtifactOr)?CreatureDeathTriggerEffect|Batched(?:ArtifactOr)?CreatureDeathTriggerEffect')


def relation(slot, line):
    quotes = re.findall(r'"([^"]*)"', line)
    rel, _ = classify.relation(slot, quotes or [line])
    if rel == '?':
        rel = DEFAULT.get(slot, 'YOU_CONTROL')
    return rel


def trigger(slot, rel, qualified):
    r = ('ObjectRelation.' if qualified else '') + rel
    kind = KIND[slot]
    if kind == 'creature':
        text = 'Triggers.creatureDies(%s)' % r
    elif kind == 'nontoken':
        text = 'Triggers.nontokenCreatureDies(%s)' % r
    elif kind == 'creatureOrPlaneswalker':
        text = 'Triggers.dies(Triggers.CREATURE_OR_PLANESWALKER, %s)' % r
    elif kind == 'artifactOrCreature':
        text = 'Triggers.dies(Triggers.ARTIFACT_OR_CREATURE, %s)' % r
    else:
        text = 'Triggers.dies(%s)' % r
    if slot.startswith('GRAVEYARD_'):
        return 'TriggerCondition(%s, %sGRAVEYARD)' % (text, 'AbilityZones.' if qualified else '')
    return text


def rewrite(line):
    def add(m):
        slot = m.group(1)
        t = trigger(slot, relation(slot, line), True)
        if slot.startswith('GRAVEYARD_'):
            return 'addTrigger(new %s, TriggerOptions.DEFAULT, ' % t
        return 'addTrigger(%s, ' % t
    line = ADD.sub(add, line)
    return SLOT.sub(lambda m: trigger(m.group(1), relation(m.group(1), line), False), line)


def main():
    for name in sys.argv[1:]:
        path = pathlib.Path(name)
        lines = path.read_text().split('\n')
        changed = False
        for i, line in enumerate(lines):
            if SLOT.search(line):
                new = rewrite(line)
                if new != line:
                    lines[i] = new
                    changed = True
                    print('%s:%d: %s' % (name, i + 1, new[:220]))
            if WRAPPERS.search(lines[i]):
                print('%s:%d: CHECK wrapper: %s' % (name, i + 1, lines[i][:220]))
        if changed:
            path.write_text('\n'.join(lines))


if __name__ == '__main__':
    main()
