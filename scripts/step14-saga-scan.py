#!/usr/bin/env python3
"""Lists every Saga chapter and final-chapter ability (Triggers.chapter, finalChapterAbilityTriggers,
finalChapterAbilityResolves) whose effects carry a trigger-only wrapper or are passed as a local variable, with the
wrappers, the condition of each ConditionalEffect and the card's Oracle text (effect-slot-refactor.md step 14, run 14).

Usage: python3 scripts/step14-saga-scan.py [--all] [--summary] > out.tsv
"""
import importlib.util
import os
import re
import sys

HERE = os.path.dirname(__file__)
spec = importlib.util.spec_from_file_location("life", os.path.join(HERE, "step14-life-scan.py"))
life = importlib.util.module_from_spec(spec)
spec.loader.exec_module(life)

life.FAMILY = re.compile(r"Triggers\.(chapter|finalChapterAbilityTriggers|finalChapterAbilityResolves)\(")

if __name__ == "__main__":
    sys.exit(life.main())
