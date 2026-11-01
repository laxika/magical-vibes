#!/usr/bin/env python3
"""EffectSlot refactor step 12a: rewrites agent-docs and the card skills for the zone-change slots that left
EffectSlot.

Usage: step12a-docs.py FILE...

Each line that names one of the moved slots gets the trigger its cards were converted to
(step12a-zone-change-triggers.py's FAMILIES, with the slot's default relation):

- ``addEffect(EffectSlot.<slot>, x)`` becomes ``addTrigger(<trigger>, x)``, or
  ``addTrigger(new TriggerCondition(<trigger>, AbilityZones.<zone>), TriggerOptions.<options>, x)`` for a slot that
  functions from another zone or triggers once for one or more;
- any other mention becomes the trigger.

Every rewritten line is printed with its file and line number for a person to check.
"""
import importlib.util
import pathlib
import re
import sys

HERE = pathlib.Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('triggers', HERE / 'step12a-zone-change-triggers.py')
triggers = importlib.util.module_from_spec(spec)
spec.loader.exec_module(triggers)

SLOTS = {}
for family in triggers.FAMILIES.values():
    SLOTS.update(family)
NAMES = '|'.join(sorted(SLOTS, key=len, reverse=True))
SLOT = re.compile(r'(?:EffectSlot\.)?\b(' + NAMES + r')\b')
ADD = re.compile(r'\baddEffect\(\s*(?:EffectSlot\.)?(' + NAMES + r')\s*,\s*')


def head(slot):
    template, relation, zone = SLOTS[slot][:3]
    trigger = template.format(rel=relation)
    options = SLOTS[slot][3] if len(SLOTS[slot]) > 3 else None
    if zone is None and options is None:
        return trigger, trigger
    condition = 'new TriggerCondition(%s%s), TriggerOptions.%s' % (
        trigger, ', AbilityZones.%s' % zone if zone else '', options or 'DEFAULT')
    return trigger, condition


def rewrite(line):
    line = ADD.sub(lambda m: 'addTrigger(%s, ' % head(m.group(1))[1], line)
    return SLOT.sub(lambda m: head(m.group(1))[0], line)


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
        if changed:
            path.write_text('\n'.join(lines))


if __name__ == '__main__':
    main()
