#!/usr/bin/env python3
"""Step 15, run 17z16b2 (read-only): the card test classes of the cards on the routes and readers the run changed.

A card is listed when its class writes

- a damage trigger (`Triggers.` with "Damage" in the name, or a prevented-damage one): the target choices of the
  damage collectors no longer carry the damage as X;
- a counters-put trigger: the target choice of the "+1/+1 counters put on a permanent you control" collector no
  longer carries the number of counters as X;
- a sacrifice or a cast trigger together with a graveyard card (`Graveyard` in the class): the graveyard target choice
  of those collectors no longer carries the sacrificed permanent's or the spell's mana value as X;
- a mana value limit or restriction of a graveyard target that the choice now reads from the trigger record
  (`dynamicMaxManaValue`, `requiresManaValueEqualsX`, `requiresManaValueAtMostX`, an event-value predicate), or
  `ExileTargetCardFromGraveyardAndMayCastCopyEffect`, whose handler is given the event's value;
- or is one of the classes `scripts/step15-run17z16b2-cards.py` converts.

Usage: python3 scripts/step15-run17z16b2-test-list.py [--gradle] [--letters abc]
"""
import importlib.util
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DAMAGE = re.compile(r"Triggers\.\w*(?:Damage|Prevented)\w*\(")
COUNTERS = re.compile(r"Triggers\.\w*(?:CountersPutOn|CounterPutOn|PutCountersOn|PutPlusOneCounters|"
                      r"PutMinusOneCounter|countersPutOn|counterPutOn)\w*\(")
GRAVEYARD_ROUTE = re.compile(r"Triggers\.(?:sacrificed\w*|\w*[cC]asts?\w*Spell\w*)\(")
READERS = re.compile(r"dynamicMaxManaValue|requiresManaValueEqualsX|requiresManaValueAtMostX|"
                     r"EventValuePredicate|ExileTargetCardFromGraveyardAndMayCastCopyEffect")

spec = importlib.util.spec_from_file_location("cards", ROOT / "scripts/step15-run17z16b2-cards.py")
cards = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cards)
CONVERTED = set(cards.EDITS)

letters = None
if "--letters" in sys.argv:
    letters = set(sys.argv[sys.argv.index("--letters") + 1])

by_letter = {}
for path in sorted((ROOT / "magical-vibes-card").glob("*/src/main/java/**/*.java")):
    text = path.read_text()
    if not (path.stem in CONVERTED or DAMAGE.search(text) or COUNTERS.search(text) or READERS.search(text)
            or GRAVEYARD_ROUTE.search(text) and "Graveyard" in text):
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
