#!/usr/bin/env python3
"""EffectSlot refactor step 13, family 3: rewrites agent-docs for the combat slots, which left EffectSlot.

Usage: step13-combat-docs.py FILE...

With T the slot's trigger and Z the zone it functions from (scripts/step13-combat-triggers.py SLOTS):

- ``addEffect([EffectSlot.]X, e)`` becomes ``addTrigger(T, e)``, or from another zone
  ``addTrigger(new TriggerCondition(T, AbilityZones.Z), TriggerOptions.DEFAULT, e)``; one ending in
  ``, TriggerMode.M)`` puts the mode after the trigger;
- ``addSimultaneousEffects([EffectSlot.]X, ...)`` becomes ``addSimultaneousTrigger(T, ...)``;
- any other mention becomes T, and "T from the Z" for a slot of another zone; "slot" right after it reads
  "ability".

Every rewritten line is printed with its file and line number for a person to check.
"""
import os
import pathlib
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
step13 = __import__("step13-combat-triggers")

NAMES = "|".join(sorted(step13.SLOTS, key=len, reverse=True))
SLOT = re.compile(r"(?:EffectSlot\.)?\b(%s)\b" % NAMES)
INDEPENDENT = re.compile(r"\baddEffect\(\s*(?:EffectSlot\.)?(%s)\s*,\s*(.*?),\s*TriggerMode\.(\w+)\)" % NAMES)
ADD = re.compile(r"\baddEffect\(\s*(?:EffectSlot\.)?(%s)\s*,\s*" % NAMES)
SIMULTANEOUS = re.compile(r"\baddSimultaneousEffects\(\s*(?:EffectSlot\.)?(%s)\s*,\s*" % NAMES)
ZONE_NAMES = {"GRAVEYARD": "graveyard", "EXILE": "exile", "COMMAND": "command zone", "HAND": "hand"}


def mention(slot):
    trigger, zone = step13.SLOTS[slot]
    return trigger if zone is None else "%s from the %s" % (trigger, ZONE_NAMES[zone])


def add(m):
    slot = m.group(1)
    if step13.SLOTS[slot][1] is None:
        return "addTrigger(%s, " % step13.trigger_of(slot)
    return "addTrigger(%s, TriggerOptions.DEFAULT, " % step13.condition_of(slot)


def rewrite(line):
    line = INDEPENDENT.sub(lambda m: "addTrigger(%s, TriggerMode.%s, %s)"
                           % (step13.trigger_of(m.group(1)), m.group(3), m.group(2)), line)
    line = ADD.sub(add, line)
    line = SIMULTANEOUS.sub(lambda m: "addSimultaneousTrigger(%s, " % step13.trigger_of(m.group(1)), line)
    line = re.sub(r"(?:EffectSlot\.)?\b(%s)\b( slots?\b)?" % NAMES,
                  lambda m: mention(m.group(1)) + (" ability" if m.group(2) else ""), line)
    return line


def main():
    for name in sys.argv[1:]:
        path = pathlib.Path(name)
        lines = path.read_text().split("\n")
        changed = False
        for i, line in enumerate(lines):
            if SLOT.search(line):
                new = rewrite(line)
                if new != line:
                    lines[i] = new
                    changed = True
                    print("%s:%d: %s" % (name, i + 1, new[:220]))
        if changed:
            path.write_text("\n".join(lines))


if __name__ == "__main__":
    main()
