#!/usr/bin/env python3
"""EffectSlot refactor step 10b: rewrites agent-docs and skill snippets for the pilot slots that left EffectSlot.

Usage: step10b-docs.py FILE...

- Every inline code span (`` `...` ``) and fenced code block is rewritten as ``step10b-pilot-triggers.py``
  rewrites card classes: ``addEffect(EffectSlot.ON_DEATH, x)`` (also written ``addEffect(ON_DEATH, x)``)
  becomes ``addTrigger(Triggers.dies(ObjectRelation.THIS), x)``, ``LegacySlots.triggered(EffectSlot.ON_DEATH, x)``
  becomes ``new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), x)`` and a grant's ``EffectSlot.ON_DEATH, x``
  argument pair becomes ``new TriggeredAbility(Triggers.dies(ObjectRelation.THIS), x)``.
- Any other mention of a pilot slot name, such as the tables' ``ON_DEATH x(...)`` shorthand or a "Slot" column,
  becomes the trigger: ``Triggers.dies(THIS)``.

A doc can't tell a sacrifice-only "when you sacrifice this" ability or an instant's haunt from a death trigger, so
every rewritten line that also names "sacrifice" or "haunt" is printed for a person to check, along with every line
that still names a pilot slot.
"""
import importlib.util
import pathlib
import re
import sys

HERE = pathlib.Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('step10b', HERE / 'step10b-pilot-triggers.py')
step10b = importlib.util.module_from_spec(spec)
spec.loader.exec_module(step10b)

SHORT = {slot: trigger.replace('ObjectRelation.THIS', 'THIS') for slot, trigger in step10b.PILOT.items()}
BARE_SLOT = re.compile(r'(?:EffectSlot\.)?\b(' + '|'.join(step10b.PILOT) + r')\b')
UNQUALIFIED_ADD = re.compile(r'\baddEffect\(\s*(' + '|'.join(step10b.PILOT) + r')\s*,')


def rewrite_code(code):
    code = UNQUALIFIED_ADD.sub(lambda m: 'addEffect(EffectSlot.' + m.group(1) + ',', code)
    try:
        return step10b.convert(code, '', set(), set())
    except (ValueError, IndexError):
        return code


def rewrite(text):
    text = re.sub(r'```.*?```', lambda m: rewrite_code(m.group(0)), text, flags=re.S)
    text = re.sub(r'`[^`\n]+`', lambda m: rewrite_code(m.group(0)), text)
    return BARE_SLOT.sub(lambda m: SHORT[m.group(1)], text)


def main():
    for name in sys.argv[1:]:
        path = pathlib.Path(name)
        before = path.read_text()
        after = rewrite(before)
        if after != before:
            path.write_text(after)
            print('changed', path)
        old_lines = before.split('\n')
        for number, line in enumerate(after.split('\n'), 1):
            if 'Triggers.' in line and re.search(r'sacrific|haunt', line, re.I) and line not in old_lines:
                print(f'CHECK {path}:{number}: {line[:160]}')
            if BARE_SLOT.search(line):
                print(f'LEFT {path}:{number}: {line[:160]}')


if __name__ == '__main__':
    main()
