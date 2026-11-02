#!/usr/bin/env python3
"""EffectSlot refactor step 12b: rewrites agent-docs and the card skills for ON_ENTER_BATTLEFIELD, which left EffectSlot.

Usage: step12b-docs.py FILE...

- ``addEffect([EffectSlot.]ON_ENTER_BATTLEFIELD, x)`` becomes ``addTrigger(Triggers.enters(ObjectRelation.THIS), x)``,
  and one ending in ``, TriggerMode.INDEPENDENT)`` puts the mode after the trigger;
- ``addSimultaneousEffects([EffectSlot.]ON_ENTER_BATTLEFIELD, ...)`` becomes ``addSimultaneousTrigger(<trigger>, ...)``;
- any other mention becomes the trigger.

Every rewritten line is printed with its file and line number for a person to check.
"""
import pathlib
import re
import sys

ENTERS = 'Triggers.enters(ObjectRelation.THIS)'
SLOT = re.compile(r'(?:EffectSlot\.)?\bON_ENTER_BATTLEFIELD\b')
INDEPENDENT = re.compile(r'\baddEffect\(\s*(?:EffectSlot\.)?ON_ENTER_BATTLEFIELD\s*,\s*(.*?),\s*TriggerMode\.INDEPENDENT\)')
ADD = re.compile(r'\baddEffect\(\s*(?:EffectSlot\.)?ON_ENTER_BATTLEFIELD\s*,\s*')
SIMULTANEOUS = re.compile(r'\baddSimultaneousEffects\(\s*(?:EffectSlot\.)?ON_ENTER_BATTLEFIELD\s*,\s*')


def rewrite(line):
    line = INDEPENDENT.sub(lambda m: 'addTrigger(%s, TriggerMode.INDEPENDENT, %s)' % (ENTERS, m.group(1)), line)
    line = ADD.sub('addTrigger(%s, ' % ENTERS, line)
    line = SIMULTANEOUS.sub('addSimultaneousTrigger(%s, ' % ENTERS, line)
    return SLOT.sub(ENTERS, line)


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
