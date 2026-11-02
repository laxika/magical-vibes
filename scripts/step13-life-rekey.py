#!/usr/bin/env python3
"""Re-keys the life gain, loss and payment trigger handlers (effect-slot-refactor.md step 13, family 9).

@CollectsTrigger(value = X.class, slot = EffectSlot.S) becomes @CollectsLifeTrigger(value = X.class,
watch = LifeWatch.S) for every slot S of scripts/step13-life-slots.tsv, and a @CollectsTriggers container that
holds only such handlers becomes @CollectsLifeTriggers. A container mixing the family with other slots is an error.

Usage: python3 scripts/step13-life-rekey.py FILE [FILE ...]
"""
import os
import re
import sys

ROWS = [line.rstrip("\n").split("\t") for line in open(os.path.join(os.path.dirname(__file__), "step13-life-slots.tsv"))
        if not line.startswith("#")]
SLOTS = [row[0] for row in ROWS]

FAMILY = r"EffectSlot\.(%s)\b" % "|".join(SLOTS)
ONE = re.compile(r"@CollectsTrigger\((\s*value\s*=\s*[\w.]+\.class,\s*)slot\s*=\s*%s\s*\)" % FAMILY)
CONTAINER = re.compile(r"@CollectsTriggers\(\{(.*?)\}\)", re.S)


def rekey(src, path):
    def container(m):
        body = m.group(1)
        handlers = re.findall(r"@CollectsTrigger\(", body)
        family = ONE.findall(body)
        if not family:
            return m.group(0)
        if len(family) != len(handlers):
            raise ValueError("%s: a container mixes the family with other slots" % path)
        return "@CollectsLifeTriggers({" + body + "})"

    src = CONTAINER.sub(container, src)
    src = ONE.sub(lambda m: "@CollectsLifeTrigger(%swatch = LifeWatch.%s)" % (m.group(1), m.group(2)), src)
    return src


def main():
    for path in sys.argv[1:]:
        src = open(path).read()
        out = rekey(src, path)
        if out != src:
            open(path, "w").write(out)
            print("rekeyed:", path)


if __name__ == "__main__":
    main()
