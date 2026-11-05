#!/usr/bin/env python3
"""Effect-slot refactor step 14, run 16r: `TriggerMode` is gone, so the cards stop passing it.

Every triggered ability is now one object on the stack (CR 603.3) with every collector grouping effects by ability,
so `TriggerMode.INDEPENDENT` ("this registration is an ability of its own") says nothing an ability doesn't, and
`TriggerMode.NORMAL` was the default:

    addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT, options, effect)
        -> addTrigger(Triggers.enters(ObjectRelation.THIS), options, effect)
    new EffectRegistration(effect, TriggerMode.NORMAL) -> new EffectRegistration(effect)

and the import goes. A comment that names the mode is reworded. Running it again changes nothing.

Usage: python3 scripts/step14-trigger-mode-removal.py [--dry-run] <dir>...
"""
import difflib
import os
import re
import sys

MODE_ARGUMENT = re.compile(r",\s*TriggerMode\.(?:INDEPENDENT|NORMAL)(?=\s*[,)])")
IMPORT = re.compile(r"^import com\.github\.laxika\.magicalvibes\.model\.TriggerMode;\n", re.M)
COMMENTS = [
    # "… its own triggered ability\n// (TriggerMode.INDEPENDENT). Each …" -> "… ability.\n// Each …"
    (re.compile(r"\n([ \t]*)//\s*\(TriggerMode\.INDEPENDENT\)\.\s*"), ".\n\\1// "),
    (re.compile(r"\s*\(TriggerMode\.INDEPENDENT\)"), ""),
    (re.compile(r"TriggerMode\.INDEPENDENT"), "an ability of its own"),
]


def convert(src):
    new = MODE_ARGUMENT.sub("", src)
    new = IMPORT.sub("", new)
    for pattern, replacement in COMMENTS:
        new = pattern.sub(replacement, new)
    return new


def main():
    dry_run = "--dry-run" in sys.argv
    changed = 0
    for root_dir in [a for a in sys.argv[1:] if not a.startswith("--")]:
        for root, _, files in os.walk(root_dir):
            if "/build/" in root:
                continue
            for name in sorted(files):
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                if "TriggerMode." not in src and "model.TriggerMode;" not in src:
                    continue
                new = convert(src)
                if new == src:
                    continue
                changed += 1
                if dry_run:
                    sys.stdout.writelines(difflib.unified_diff(src.splitlines(True), new.splitlines(True), path, path))
                else:
                    open(path, "w").write(new)
    print("changed %d" % changed, file=sys.stderr)


if __name__ == "__main__":
    sys.exit(main())
