#!/usr/bin/env python3
"""Deletes the given constants, with the Javadoc and comment lines right above them, from EffectSlot
(effect-slot-refactor.md step 12a). Usage: python3 scripts/step12a-delete-slots.py SLOT [SLOT ...]"""
import re
import sys

PATH = "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/EffectSlot.java"


def main():
    lines = open(PATH).read().split("\n")
    for slot in sys.argv[1:]:
        index = next((i for i, line in enumerate(lines) if re.match(r"\s*%s\s*[,;(]" % slot, line)), None)
        if index is None:
            sys.exit("no constant " + slot)
        end = index
        while not re.search(r"[,;]\s*(//.*)?$", lines[end]):
            end += 1
        start = index
        while start > 0 and (lines[start - 1].strip().startswith(("*", "/**", "//")) and lines[start - 1].strip()):
            start -= 1
            if lines[start].strip().startswith("/**"):
                break
        if lines[end].rstrip().endswith(";"):
            sys.exit("last constant " + slot)
        del lines[start:end + 1]
    open(PATH, "w").write("\n".join(lines))


if __name__ == "__main__":
    main()
