#!/usr/bin/env python3
"""Lists the cards on the delayed-action queues run 17z17d changed and their card test classes (effect-slot-refactor.md step 15).

Read-only. The queues are fed by handlers, not by one effect class, so a card is found by its Oracle text (the
card-data cache, by the class's @CardRegistration): it is listed when a face's text creates a delayed triggered
ability of one of the changed kinds:

* "exile / sacrifice / destroy / return / put ... at the beginning of the next end step", "... of your next end step",
  "... of the next upkeep", "... of your next upkeep", "... at end of combat", "... at the beginning of the next
  cleanup step" (the DelayedPermanentAction queue and the two upkeep queues);
* "when you next cast", "when you next activate", "copy the next", "when you spend this mana to cast" (the
  next-cast and next-activation copies);
* "leaves the battlefield or becomes untapped" (the DelayedSourceUntapsOrLeavesTrigger queue: Tawnos's Coffin).

A test class is listed when it is the card's own (<Card>Test) or names the card class in its body.

    scripts/step15-run17z17d-test-list.py            the test classes, one per line, with their letter module
    scripts/step15-run17z17d-test-list.py --gradle   the Gradle arguments, a --tests filter after each module's task
    scripts/step15-run17z17d-test-list.py --cards    the card classes: registration and the pattern that listed it
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
TESTS = ROOT / "magical-vibes-card-test"
CACHE = ROOT / "card-data-cache"

REGISTRATION = re.compile(r'@CardRegistration\(set = "([^"]+)", collectorNumber = "([^"]+)"\)')
TIMED = re.compile(r"(exile|sacrifice|destroy|return|put)[^.\n]*at (the beginning of (the|your) next (end step|upkeep|"
                   r"cleanup step)|end of combat)|at (the beginning of (the|your) next (end step|upkeep|cleanup step)"
                   r"|end of combat)[^.\n]*(exile|sacrifice|destroy|return|put)", re.IGNORECASE)
NEXT = re.compile(r"when you next (cast|activate)|copy the next|when you spend this mana to cast", re.IGNORECASE)
UNTAPS = re.compile(r"leaves the battlefield or becomes untapped", re.IGNORECASE)
PATTERNS = (("timed zone change", TIMED), ("next cast or activation", NEXT), ("untaps or leaves", UNTAPS))


def oracle_texts():
    """(set code, collector number) -> the Oracle text of every face, from the card-data cache."""
    texts = {}
    for path in sorted(CACHE.glob("set-*.json")):
        data = json.loads(path.read_text(encoding="utf-8"))
        code = str(data.get("setCode", path.stem[4:])).upper()
        for printing in data.get("printings", []):
            faces = [printing.get("front"), printing.get("back")] + list(printing.get("splitHalves") or [])
            text = "\n".join(face.get("text") or "" for face in faces if isinstance(face, dict))
            texts[(code, str(printing.get("collectorNumber")))] = text
    return texts


def card_classes():
    """Card class name -> (registration, the pattern that listed it)."""
    texts = oracle_texts()
    listed = {}
    for path in sorted(CARDS.glob("*/src/main/java/**/*.java")):
        source = path.read_text(encoding="utf-8")
        for code, number in REGISTRATION.findall(source):
            text = texts.get((code.upper(), number))
            if text is None:
                continue
            reason = next((label for label, pattern in PATTERNS if pattern.search(text)), None)
            if reason:
                listed[path.stem] = (f"{code} {number}", reason)
            break
    return listed


def test_classes(cards):
    """(letter, fully qualified test class) of every test class that is a listed card's or names one."""
    names = sorted(cards, key=len, reverse=True)
    word = re.compile(r"\b(" + "|".join(re.escape(name) for name in names) + r")\b") if names else None
    found = set()
    for path in sorted(TESTS.glob("*/src/test/java/**/*Test.java")):
        letter = path.relative_to(TESTS).parts[0]
        own = path.stem[:-4] in cards
        if own or (word and word.search(path.read_text(encoding="utf-8"))):
            relative = path.relative_to(TESTS / letter / "src" / "test" / "java")
            found.add((letter, ".".join(relative.with_suffix("").parts)))
    return sorted(found)


def main():
    cards = card_classes()
    if "--cards" in sys.argv:
        for name in sorted(cards):
            print(name + "\t" + "\t".join(cards[name]))
        by_reason = {}
        for _, reason in cards.values():
            by_reason[reason] = by_reason.get(reason, 0) + 1
        print(f"{len(cards)} card classes: "
              + ", ".join(f"{count} {reason}" for reason, count in sorted(by_reason.items())), file=sys.stderr)
        return
    classes = test_classes(cards)
    if "--gradle" in sys.argv:
        arguments = []
        for letter in sorted({letter for letter, _ in classes}):
            arguments.append(f":magical-vibes-card-test:{letter}:test")
            for other, name in classes:
                if other == letter:
                    arguments += ["--tests", name]
        print(" ".join(arguments))
    else:
        for letter, name in classes:
            print(f"{letter}\t{name}")
    print(f"{len(cards)} card classes, {len(classes)} test classes in "
          f"{len({letter for letter, _ in classes})} letter modules", file=sys.stderr)


if __name__ == "__main__":
    main()
