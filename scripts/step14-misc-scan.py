#!/usr/bin/env python3
"""Lists every ability of step 13's family 11 (a Triggers factory of scripts/step13-action-slots.tsv or
scripts/step13-misc-slots.tsv: player and keyword actions, Planechase, control change, attaching, crew, saddle and
station, day and night, Rooms, state triggers) whose effects carry a trigger-only wrapper or are passed as a local
variable, with the wrappers, the condition of each ConditionalEffect and the card's Oracle text
(effect-slot-refactor.md step 14, run 15).

Usage: python3 scripts/step14-misc-scan.py [--all] [--summary] > out.tsv
"""
import importlib.util
import os
import re
import sys

HERE = os.path.dirname(__file__)
spec = importlib.util.spec_from_file_location("life", os.path.join(HERE, "step14-life-scan.py"))
life = importlib.util.module_from_spec(spec)
spec.loader.exec_module(life)


def factories():
    names = set()
    for tsv in ("step13-action-slots.tsv", "step13-misc-slots.tsv"):
        for line in open(os.path.join(HERE, tsv)):
            if line.startswith("#") or not line.strip():
                continue
            names.add(line.split("\t")[1])
    return sorted(names, key=len, reverse=True)


FACTORIES = factories()
life.FAMILY = re.compile(r"Triggers\.(" + "|".join(FACTORIES) + r")\(")

if __name__ == "__main__":
    sys.exit(life.main())
