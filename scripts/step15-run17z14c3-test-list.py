#!/usr/bin/env python3
"""Step 15, run 17z14c3: lists the card test classes of the cards that touch what the run changed (read-only).

A card is listed when its source matches one of `RULES`: a cast trigger with a counter or attached-permanent effect (the
cast collector's two class-keyed branches), an "enchanted permanent" trigger (the death, leave and tap defaults), the
counter watches and counter removals, the target-choice, loses-the-game and becomes-untapped watches, a
damage-to-a-player or graveyard ability with a "you may", a sacrifice trigger, a modal damage or tap trigger, or it is
one of `NAMED` (the converted cards and the cards their tests use).

Usage: step15-run17z14c3-test-list.py [--gradle]  (--gradle prints the per-module `:test --tests` arguments).
"""
import pathlib
import re
import sys

REPO = pathlib.Path(__file__).resolve().parent.parent
CARDS = REPO / "magical-vibes-card"
TESTS = REPO / "magical-vibes-card-test"
EFFECTS = REPO / "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect"


def implementers(marker):
    names = []
    for path in EFFECTS.glob("*.java"):
        if path.stem != marker and re.search(r"\b" + marker + r"\b", path.read_text()):
            names.append(path.stem)
    return "|".join(sorted(names))


CAST = re.compile(r"Triggers\.\w*[cC]ast\w*\(")
MARKED = re.compile(r"\b(" + implementers("CounterSpellingEffect") + "|"
                    + implementers("AttachedPermanentSelfTargetingEffect") + r")\b")
DAMAGE = re.compile(r"Triggers\.\w*[dD]amage\w*\(")
GRAVEYARD_EVENT = re.compile(
    r"Triggers\.\w*([dD]ies|[pP]utIntoGraveyard|plusOneCountersPutOnCreatureYouControl|[rR]oomFullyUnlocked)\w*\(")

RULES = [
    lambda text: CAST.search(text) and MARKED.search(text),
    lambda text: re.search(r"enchantedPermanentBecomesTapped|ObjectRelation\.ENCHANTED\b", text),
    lambda text: re.search(r"youPutCountersOnAnotherCreature|opponentPutsCountersOnCreatureTheyControl"
                           r"|youRemoveLastCounterFromThis|\bRemoveCounterFromSourceEffect\b"
                           r"|RemoveAllCountersFromAllPermanentsEffect|PutSameCountersOnSourceEffect", text),
    lambda text: re.search(r"playerChoosesTargets|playerLosesGame|Triggers\.becomesUntapped\(", text),
    lambda text: DAMAGE.search(text) and re.search(r"\bMayEffect\b|\bChooseOneEffect\b", text),
    lambda text: "GRAVEYARD" in text and GRAVEYARD_EVENT.search(text),
    lambda text: re.search(r"Triggers\.\w*[sS]acrifice\w*\(", text),
    lambda text: re.search(r"DealDamageToEnchantedPermanentControllerEffect|TargetPlayerLosesGameEffect", text),
]
NAMED = {"HidetsuguConsumesAll", "TemporalExtortion", "AetherSnap", "Clockspinning", "DashHopes", "Phantasmagorian",
         "Counterbalance", "SwindlersScheme", "PredatoryHunger", "CapturedByTheConsulate"}


def main():
    listed = {}
    for path in sorted(CARDS.glob("*/src/main/java/com/github/laxika/magicalvibes/cards/*/*.java")):
        text = path.read_text()
        if path.stem in NAMED or any(rule(text) for rule in RULES):
            letter = path.parent.name
            test = TESTS / letter / "src/test/java/com/github/laxika/magicalvibes/cards" / letter / (path.stem + "Test.java")
            if test.exists():
                listed.setdefault(letter, []).append(path.stem + "Test")
    if "--gradle" in sys.argv:
        print(" ".join(f":magical-vibes-card-test:{letter}:test " + " ".join(f"--tests '*.{name}'" for name in names)
                       for letter, names in sorted(listed.items())))
    else:
        for letter, names in sorted(listed.items()):
            for name in names:
                print(f"{letter}/{name}")
    print(f"{sum(len(names) for names in listed.values())} test classes in {len(listed)} modules", file=sys.stderr)


if __name__ == "__main__":
    main()
