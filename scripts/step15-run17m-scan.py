#!/usr/bin/env python3
"""Step 15, run 17m: scan the step and phase abilities' conditions, classified by the Oracle text.

Run 17i's scan (`step15-run17i-scan.py`) over the step and phase trigger factories (`Triggers.upkeep`, `drawStep`,
`precombatMainPhase`, `beginningOfCombat`, `endOfCombat`, `postcombatMainPhase`, `secondMainPhase`, `endStep`). For
every triggered-ability statement: its option conditions (IF: intervening "if", EV: event condition) and its top-level
effects' `ConditionalEffect` conditions (CE, `--effects`), each classified by the card's Oracle text: "IF" when every
"At the beginning of ..." line has an ", if" right after its trigger condition (CR 603.4), "EVENT" when none has,
"MIXED" otherwise. A CE of an "IF" card is an intervening "if" written as an effect's. Read-only.

Usage: step15-run17m-scan.py [--effects]
"""
import collections
import importlib.util
import pathlib
import re
import sys

spec = importlib.util.spec_from_file_location("scan17i", pathlib.Path(__file__).with_name("step15-run17i-scan.py"))
scan17i = importlib.util.module_from_spec(spec)
spec.loader.exec_module(scan17i)

scan17i.FACTORIES = ("upkeep drawStep precombatMainPhase beginningOfCombat endOfCombat postcombatMainPhase "
                     "secondMainPhase endStep").split()

TRIGGER_LINE = re.compile(r"\bAt (the )?(beginning|end) of\b", re.I)
INTERVENING = re.compile(r"\bAt (the )?(beginning|end) of\b[^,.]*?, if\b", re.I)


def classify(path):
    lines = [line for line in re.split(r"\n|•", scan17i.oracle_named(path) or "") if TRIGGER_LINE.search(line)]
    count = sum(bool(INTERVENING.search(line)) for line in lines)
    return "IF" if lines and count == len(lines) else "EVENT" if lines and count == 0 else "MIXED"


def main():
    with_effects = "--effects" in sys.argv
    result = collections.defaultdict(lambda: collections.defaultdict(set))
    for path, trigger, options, effects in scan17i.abilities():
        kind = classify(path)
        for option in options:
            if option.startswith("IF:"):
                result[(kind, trigger, "IF")][option[3:]].add(path.stem)
        if with_effects:
            for condition in effects:
                result[(kind, trigger, "CE")][condition].add(path.stem)
    for (kind, trigger, form), conditions in sorted(result.items()):
        print(f"## {kind} {trigger} {form}")
        for condition, cards in sorted(conditions.items()):
            print(f"   {condition}: {' '.join(sorted(cards))}")


if __name__ == "__main__":
    main()
