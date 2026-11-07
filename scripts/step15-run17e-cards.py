#!/usr/bin/env python3
"""Step 15 run 17e of effect-slot-refactor.md: the last two "that player controls" bindings read the trigger event.

Idempotent; run per letter module (``--module d``) or with no argument for every module this run touches.

- Dokuchi Silencer: the reflexive "when you do, destroy target creature or planeswalker that player controls" is
  ``DestroyTargetPermanentEffect`` with ``PermanentControlledByTriggeringPlayerPredicate``; the discard follow-up
  carries the player dealt combat damage as the reflexive target choice's triggering player.
- Chandra's Incinerator: "target creature or planeswalker that player controls" is the card's own target filter with
  ``PermanentControlledByTriggeringPlayerPredicate``; the collector no longer narrows the filter to the damaged
  player.
"""
import argparse
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"
FILTER = "com.github.laxika.magicalvibes.model.filter"
EFFECT = "com.github.laxika.magicalvibes.model.effect"

EDITS = {
    ("d", "DokuchiSilencer"): [
        ("""                        new DestroyPermanentDamagedPlayerControlsEffect(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate()))),""",
         """                        new DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate())),
                                new PermanentControlledByTriggeringPlayerPredicate()))),"""),
        (f"import {EFFECT}.DestroyPermanentDamagedPlayerControlsEffect;\n",
         f"import {EFFECT}.DestroyTargetPermanentEffect;\n"),
        (f"import {FILTER}.PermanentAnyOfPredicate;\n",
         f"import {FILTER}.PermanentAllOfPredicate;\nimport {FILTER}.PermanentAnyOfPredicate;\n"
         f"import {FILTER}.PermanentControlledByTriggeringPlayerPredicate;\n"),
    ],
    ("c", "ChandrasIncinerator"): [
        ("""        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                "Target must be a creature or planeswalker that player controls"))""",
         """        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsPlaneswalkerPredicate())),
                        new PermanentControlledByTriggeringPlayerPredicate())),
                "Target must be a creature or planeswalker that player controls"))"""),
        (f"import {FILTER}.PermanentAnyOfPredicate;\n",
         f"import {FILTER}.PermanentAllOfPredicate;\nimport {FILTER}.PermanentAnyOfPredicate;\n"
         f"import {FILTER}.PermanentControlledByTriggeringPlayerPredicate;\n"),
    ],
}

# A marker that is present once an edit has been applied, per card.
DONE = {
    "DokuchiSilencer": "new PermanentControlledByTriggeringPlayerPredicate()",
    "ChandrasIncinerator": "new PermanentControlledByTriggeringPlayerPredicate()",
}


def convert(letter, name, edits):
    path = CARDS / letter / PKG / letter / f"{name}.java"
    text = path.read_text()
    if DONE[name] in text:
        print(f"unchanged {path.relative_to(ROOT)}")
        return
    for old, new in edits:
        if text.count(old) != 1:
            sys.exit(f"{path}: expected one occurrence of {old!r}")
        text = text.replace(old, new)
    path.write_text(text)
    print(f"converted {path.relative_to(ROOT)}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--module", help="letter module to convert (default: all this run touches)")
    args = parser.parse_args()
    for (letter, name), edits in EDITS.items():
        if args.module is None or args.module == letter:
            convert(letter, name, edits)


if __name__ == "__main__":
    main()
