#!/usr/bin/env python3
"""EffectSlot refactor step 13, family 10: rewrites agent-docs for the Saga slots, which left EffectSlot.

Usage: step13-saga-docs.py FILE...

With T the slot's trigger (scripts/step13-saga-triggers.py SLOTS) and N a chapter slot's number:

- ``addEffect([EffectSlot.]X, e)`` becomes ``addTrigger(T, e)``, ``addSimultaneousEffects([EffectSlot.]X, ...)``
  ``addSimultaneousTrigger(T, ...)``;
- ``setSagaChapterTargetFilter / Required / Groups([EffectSlot.]X`` and their getters take ``N``;
- any other mention becomes T; "slot" right after it reads "ability".

Every rewritten line is printed with its file and line number for a person to check.
"""
import os
import pathlib
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
step13 = __import__("step13-saga-triggers")

NAMES = "|".join(sorted(step13.SLOTS, key=len, reverse=True))
SLOT = re.compile(r"(?:EffectSlot\.)?\b(%s)\b" % NAMES)
ADD = re.compile(r"\baddEffect\(\s*(?:EffectSlot\.)?(%s)\s*,\s*" % NAMES)
SIMULTANEOUS = re.compile(r"\baddSimultaneousEffects\(\s*(?:EffectSlot\.)?(%s)\s*,\s*" % NAMES)
TARGETS = re.compile(r"\b%s\(\s*(?:EffectSlot\.)?(%s)\b" % (step13.TARGET_METHODS, NAMES))


def rewrite(line):
    line = ADD.sub(lambda m: "addTrigger(%s, " % step13.SLOTS[m.group(1)], line)
    line = SIMULTANEOUS.sub(lambda m: "addSimultaneousTrigger(%s, " % step13.SLOTS[m.group(1)], line)
    line = TARGETS.sub(lambda m: "%s(%d" % (m.group(1), step13.CHAPTERS[m.group(2)]), line)
    line = re.sub(r"(?:EffectSlot\.)?\b(%s)\b( slots?\b)?" % NAMES,
                  lambda m: step13.SLOTS[m.group(1)] + (" ability" if m.group(2) else ""), line)
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
