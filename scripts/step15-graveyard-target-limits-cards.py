#!/usr/bin/env python3
"""Step 15 run 17c of effect-slot-refactor.md: graveyard targets restricted by the trigger event read the choice's X.

Idempotent; run per letter module (``--module s``) or with no argument for every module this run touches.

- Venerable Warsinger: "mana value X or less, where X is the damage dealt" is ``requiresManaValueAtMostX(true)``;
  a combat damage trigger's graveyard choice has the damage as its X.
- Soundwave, Sonic Spy: "mana value equal to the damage dealt" is ``CardManaValueEqualsXPredicate`` in the filter;
  the flag that had the collector rebuild the filter is gone.
- Riveteers Ascendancy: "target creature card with lesser mana value" is ``CardManaValueLessThanXPredicate``; a
  sacrifice trigger's graveyard choice has the sacrificed permanent's mana value as its X.
"""
import argparse
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"
FILTER = "com.github.laxika.magicalvibes.model.filter"

EDITS = {
    ("v", "VenerableWarsinger"): [
        ("                .dynamicMaxManaValue(new EventValue())\n",
         "                .requiresManaValueAtMostX(true)\n"),
        ("import com.github.laxika.magicalvibes.model.amount.EventValue;\n", ""),
    ],
    ("s", "SoundwaveSonicSpy"): [
        ("""                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                        instantOrSorcery,
                        GraveyardSearchScope.OPPONENT_GRAVEYARD,
                        true,
                        new TransformSelfEffect()));""",
         """                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                        new CardAllOfPredicate(List.of(instantOrSorcery, new CardManaValueEqualsXPredicate())),
                        GraveyardSearchScope.OPPONENT_GRAVEYARD,
                        new TransformSelfEffect()));"""),
        (f"import {FILTER}.CardAnyOfPredicate;\n",
         f"import {FILTER}.CardAllOfPredicate;\nimport {FILTER}.CardAnyOfPredicate;\n"
         f"import {FILTER}.CardManaValueEqualsXPredicate;\n"),
    ],
    ("r", "RiveteersAscendancy"): [
        ("""                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .enterTapped(true)""",
         """                .filter(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardManaValueLessThanXPredicate())))
                .targetGraveyard(true)
                .enterTapped(true)"""),
        (f"import {FILTER}.CardTypePredicate;\n",
         f"import {FILTER}.CardAllOfPredicate;\nimport {FILTER}.CardManaValueLessThanXPredicate;\n"
         f"import {FILTER}.CardTypePredicate;\n"),
    ],
}

# A marker that is present once an edit has been applied, per card.
DONE = {
    "VenerableWarsinger": ".requiresManaValueAtMostX(true)",
    "SoundwaveSonicSpy": "new CardManaValueEqualsXPredicate()",
    "RiveteersAscendancy": "new CardManaValueLessThanXPredicate()",
}


def ensure_list_import(text):
    if "import java.util.List;" in text:
        return text
    return text.replace("\n@CardRegistration", "\nimport java.util.List;\n\n@CardRegistration", 1)


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
    if "List.of(" in text:
        text = ensure_list_import(text)
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
