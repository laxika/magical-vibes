#!/usr/bin/env python3
"""Step 15, run 17z17 (read-only): the card test classes of the cards on the run's two engine changes.

A card test class is listed when its card, or a card its `@CardUsed` names, writes

  - `PutCountersOnSourceEffect`: its handler fired the "+1/+1 counters are put on another creature you control"
    watchers twice for one placement (Wildwood Scourge, Enduring Scalelord, which are listed too);
  - one of the triggers whose entry now has the source snapshot on every route: "whenever you attack a player",
    "... a player or planeswalker", "whenever one or more creatures attack you".

Usage: python3 scripts/step15-run17z17-test-list.py [--gradle]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
WRITES = re.compile(r"\bPutCountersOnSourceEffect\b|Triggers\.youAttackPlayer\(|Triggers\.youAttackPlayerOrPlaneswalker\("
                    r"|Triggers\.creaturesAttackYou\(|Triggers\.plusOneCountersPutOnAnotherCreatureYouControl\("
                    r"|Triggers\.plusOneCountersPutOnAnotherNonHydraCreatureYouControl\(")

cards = set()
for path in sorted((ROOT / "magical-vibes-card").glob("*/src/main/java/**/*.java")):
    if WRITES.search(path.read_text()):
        cards.add(path.stem)

by_letter = {}
for path in sorted((ROOT / "magical-vibes-card-test").glob("*/src/test/java/**/*Test.java")):
    letter = path.relative_to(ROOT / "magical-vibes-card-test").parts[0]
    text = path.read_text()
    used = set(re.findall(r"\b([A-Z]\w+)\.class", text))
    if path.stem[:-len("Test")] in cards or used & cards:
        by_letter.setdefault(letter, set()).add(path.stem)

total = sum(len(names) for names in by_letter.values())
if "--gradle" in sys.argv:
    args = []
    for letter in sorted(by_letter):
        args.append(f":magical-vibes-card-test:{letter}:test")
        for name in sorted(by_letter[letter]):
            args += ["--tests", f"*.{name}"]
    print(" ".join(args))
else:
    for letter in sorted(by_letter):
        for name in sorted(by_letter[letter]):
            print(f"{letter}/{name}")
    print(f"{len(cards)} cards, {total} test classes in {len(by_letter)} letter modules", file=sys.stderr)
