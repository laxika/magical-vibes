#!/usr/bin/env python3
"""Moves engine code off the zone-change EffectSlots a step 12a family moves (effect-slot-refactor.md): each
@CollectsTrigger naming one of the slots becomes a @CollectsWatchTrigger naming the ZoneChangeWatch of that name,
and every other EffectSlot.<slot> in engine, AI and application code becomes ZoneChangeWatch.<slot>. A reader that
still asks a card for the slot's effects then fails to compile, so it can be converted by hand.
Usage: python3 scripts/step12a-rekey.py SLOT [SLOT ...]
"""
import os
import re
import sys

ROOTS = ["magical-vibes-engine/src", "magical-vibes-ai/src", "magical-vibes-application/src/test",
         "magical-vibes-networking/src"]
WATCH_FQCN = "com.github.laxika.magicalvibes.service.trigger.ZoneChangeWatch"
ANNOTATION_FQCN = "com.github.laxika.magicalvibes.service.trigger.CollectsWatchTrigger"
TRIGGER_PACKAGE = "package com.github.laxika.magicalvibes.service.trigger;"


def add_import(src, fqcn):
    simple = fqcn.rsplit(".", 1)[1]
    if TRIGGER_PACKAGE in src or re.search(r"^import %s;$" % re.escape(fqcn), src, re.M):
        return src
    if not re.search(r"\b%s\b" % simple, re.sub(r"^import .*$", "", src, flags=re.M)):
        return src
    imports = list(re.finditer(r"^import com\.github[\w.]+;$", src, re.M))
    line = "import %s;" % fqcn
    for m in imports:
        if m.group(0) > line:
            return src[:m.start()] + line + "\n" + src[m.start():]
    last = imports[-1] if imports else list(re.finditer(r"^package .*;$", src, re.M))[-1]
    return src[:last.end()] + "\n" + line + src[last.end():]


def unpack_containers(src):
    """An explicit @CollectsTriggers({...}) container can't hold a @CollectsWatchTrigger: writes its annotations one
    per line instead, both kinds being repeatable."""
    def unpack(m):
        indent = m.group(1)
        annotations = re.findall(r"@Collects(?:Watch)?Trigger\([^)]*\)", m.group(2))
        return "\n".join(indent + a for a in annotations)
    return re.sub(r"^([ \t]*)@CollectsTriggers\(\{(.*?)\}\)", lambda m: unpack(m) if "@CollectsWatchTrigger" in m.group(2)
                  else m.group(0), src, flags=re.M | re.S)


def main():
    slots = "|".join(sys.argv[1:])
    annotation = re.compile(r"@CollectsTrigger\((\s*value\s*=\s*[\w.]+\.class,\s*)slot(\s*=\s*)EffectSlot\.(%s)\b"
                            % slots)
    reference = re.compile(r"\bEffectSlot\.(%s)\b" % slots)
    for root_dir in ROOTS:
        for root, _, files in os.walk(root_dir):
            for name in files:
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                new = annotation.sub(r"@CollectsWatchTrigger(\1watch\2ZoneChangeWatch.\3", src)
                new = reference.sub(r"ZoneChangeWatch.\1", new)
                new = unpack_containers(new)
                if new == src:
                    continue
                new = add_import(new, WATCH_FQCN)
                new = add_import(new, ANNOTATION_FQCN)
                if "EffectSlot" not in re.sub(r"^import .*$", "", new, flags=re.M):
                    new = re.sub(r"^import com\.github\.laxika\.magicalvibes\.model\.EffectSlot;\n", "", new,
                                 flags=re.M)
                open(path, "w").write(new)
                print("rekeyed:", path)


if __name__ == "__main__":
    main()
