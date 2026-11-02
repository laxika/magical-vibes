#!/usr/bin/env python3
"""Rewrites engine, AI, networking and test-fixture readers of the ON_ENTER_BATTLEFIELD slot to read the "when this
enters" trigger instead (effect-slot-refactor.md step 12b): getEffects -> getTriggerEffects,
getEffectRegistrations -> getTriggerEffectRegistrations, and adds the imports they need.

Usage: python3 scripts/step12b-readers.py FILE [FILE ...]
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
step11 = __import__("step11-death-triggers")

SLOT = r"(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\.ON_ENTER_BATTLEFIELD"
ENTERS = "Triggers.enters(ObjectRelation.THIS)"


def convert(src):
    src = re.sub(r"\.getEffects\(\s*%s\s*\)" % SLOT, ".getTriggerEffects(%s)" % ENTERS, src)
    src = re.sub(r"\.getEffectRegistrations\(\s*%s\s*\)" % SLOT, ".getTriggerEffectRegistrations(%s)" % ENTERS, src)
    return src


def main():
    for path in sys.argv[1:]:
        src = open(path).read()
        out = convert(src)
        if out != src:
            out = step11.fix_imports(out)
            open(path, "w").write(out)
            print("converted:", path)


if __name__ == "__main__":
    main()
