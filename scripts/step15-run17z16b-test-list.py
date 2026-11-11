#!/usr/bin/env python3
"""Step 15, run 17z16b (read-only): the card test classes of the cards whose triggered abilities reach the stack
through a choice.

A card is listed when it writes a triggered ability (`addTrigger`, `TriggeredAbility`, a `Triggers.` condition, a Saga
chapter) and anything that makes the ability choose as it goes on the stack or resolves: a target (a declared target
group or an effect that names one), a mode (`ChooseOne…`), a graveyard card, or a "you may" (`May…Effect`). Run
17z16b gives every such route the trigger record of the ability that chooses nothing.

Usage: python3 scripts/step15-run17z16b-test-list.py [--gradle] [--letters abc]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TRIGGERED = re.compile(r"addTrigger\(|TriggeredAbility|Triggers\.|addChapter|chapter\(")
CHOOSES = re.compile(r"\btarget\w*\(|Target\w*Effect|ChooseOne|Graveyard|May\w*Effect|MayEffect")

letters = None
if "--letters" in sys.argv:
    letters = set(sys.argv[sys.argv.index("--letters") + 1])

by_letter = {}
for path in sorted((ROOT / "magical-vibes-card").glob("*/src/main/java/**/*.java")):
    text = path.read_text()
    if not (TRIGGERED.search(text) and CHOOSES.search(text)):
        continue
    name = path.stem + "Test"
    test_letter = path.stem[0].lower()
    if letters is not None and test_letter not in letters:
        continue
    test = (ROOT / "magical-vibes-card-test" / test_letter / "src/test/java/com/github/laxika/magicalvibes/cards"
            / test_letter)
    if (test / (name + ".java")).exists():
        by_letter.setdefault(test_letter, []).append(name)

if "--gradle" in sys.argv:
    print(" ".join(f":magical-vibes-card-test:{letter}:test " + " ".join(f"--tests *.{name}" for name in names)
                   for letter, names in sorted(by_letter.items())))
else:
    for letter, names in sorted(by_letter.items()):
        for name in names:
            print(letter, name)
    print(sum(len(names) for names in by_letter.values()), "test classes in", len(by_letter), "modules",
          file=sys.stderr)
