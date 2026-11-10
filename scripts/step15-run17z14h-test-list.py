#!/usr/bin/env python3
"""Step 15, run 17z14h: lists the card test classes of the cards the run touched or read (read-only).

A card is listed when `step15-run17z14h-cards.py` names it (the Sagas whose chapters it merges, the "blocks" abilities
it writes once each combat and the ones it read and left for each creature blocked) or when its source names
`Triggers.blocks()` at all. A test class is listed when it is such a card's (`<Card>Test`, in whichever letter module
it lives) or when its source names a card the script names (a back face, a token maker, an Aura or Equipment another
card's test attaches).

Usage: step15-run17z14h-test-list.py [--gradle]  (--gradle prints the per-module `:test --tests` arguments).
"""
import importlib.util
import pathlib
import re
import sys

REPO = pathlib.Path(__file__).resolve().parent.parent
CARDS = REPO / "magical-vibes-card"
TESTS = REPO / "magical-vibes-card-test"


def run_cards():
    spec = importlib.util.spec_from_file_location("cards", REPO / "scripts" / "step15-run17z14h-cards.py")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return set(module.SAGAS) | set(module.BLOCKS_ONCE) | set(module.BLOCKS_EACH)


def main():
    named = run_cards()
    listed = set(named)
    for path in sorted(CARDS.glob("*/src/main/java/com/github/laxika/magicalvibes/cards/*/*.java")):
        if "Triggers.blocks()" in path.read_text():
            listed.add(path.stem)
    names = re.compile(r"\b[A-Z]\w*\b")
    tests = {}
    for test in sorted(TESTS.glob("*/src/test/java/com/github/laxika/magicalvibes/cards/*/*Test.java")):
        letter = test.relative_to(TESTS).parts[0]
        if test.stem[:-len("Test")] in listed or not named.isdisjoint(names.findall(test.read_text())):
            tests.setdefault(letter, set()).add(test.stem)
    if "--gradle" in sys.argv:
        print(" ".join(f":magical-vibes-card-test:{letter}:test " + " ".join(f"--tests '*.{name}'" for name in sorted(names))
                       for letter, names in sorted(tests.items())))
    else:
        for letter, names in sorted(tests.items()):
            for name in sorted(names):
                print(f"{letter}/{name}")
    print(f"{sum(len(names) for names in tests.values())} test classes in {len(tests)} modules", file=sys.stderr)


if __name__ == "__main__":
    main()
