#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 13a: a chosen card is the stack entry's chosen card.

Idempotent; run once per letter module:

    for m in magical-vibes-card/*/; do python3 scripts/step15-chosen-card-cards.py "$m"; done

The hand, library, spellbook and outside-the-game choices no longer bind the chosen card into a ChosenCardAwareEffect;
they set it on the stack entry (StackEntry.chosenObjectCard) and insert their follow-up unchanged. So Hinterland
Chef's "the drafted card perpetually becomes an artifact Food with ..." is the existing
PerpetuallyGrantChosenCardCharacteristicsEffect, which reads the entry's chosen card, and
PerpetuallyGrantCardCharacteristicsEffect only grants to the source's own card. The other cards keep their effects,
which already had no-argument constructors.
"""
import pathlib
import re
import sys

EFFECT = "com.github.laxika.magicalvibes.model.effect."

CARDS = {
    "HinterlandChef.java": {
        "replace": [
            ("""                new PerpetuallyGrantCardCharacteristicsEffect(
                        CardType.ARTIFACT,
                        CardSubtype.FOOD,
                        new ActivatedAbility(
                                true,
                                "{2}",
                                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                                "{2}, {T}, Sacrifice this creature: You gain 3 life."
                        )))));""",
             """                new PerpetuallyGrantChosenCardCharacteristicsEffect(
                        Set.of(CardType.ARTIFACT),
                        Set.of(CardSubtype.FOOD),
                        List.of(new ActivatedAbility(
                                true,
                                "{2}",
                                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                                "{2}, {T}, Sacrifice this creature: You gain 3 life."
                        ))))));"""),
        ],
        "add": [EFFECT + "PerpetuallyGrantChosenCardCharacteristicsEffect"],
        "add_java": ["java.util.Set"],
        "remove": [EFFECT + "PerpetuallyGrantCardCharacteristicsEffect"],
    },
}


def fix_imports(src, add, remove):
    """Drops the removed imports and adds each missing one after the project import that sorts just before it."""
    for name in remove:
        src = src.replace(f"import {name};\n", "")
    for name in add:
        line = f"import {name};\n"
        if line in src:
            continue
        project = re.findall(r"^import com\.github\.[^;]+;\n", src, flags=re.M)
        before = [existing for existing in project if existing < line]
        anchor = before[-1] if before else project[0]
        at = src.index(anchor) + (len(anchor) if before else 0)
        src = src[:at] + line + src[at:]
    return src


def convert(path, spec):
    src = path.read_text()
    converted = src
    for old, new in spec["replace"]:
        if new in converted:
            continue
        if old not in converted:
            raise SystemExit(f"{path}: neither the old nor the new form of {old!r}")
        converted = converted.replace(old, new)
    converted = fix_imports(converted, spec["add"], spec["remove"])
    for name in spec.get("add_java", []):
        line = f"import {name};\n"
        if line in converted:
            continue
        java = re.findall(r"^import java\.[^;]+;\n", converted, flags=re.M)
        before = [existing for existing in java if existing < line]
        anchor = before[-1] if before else java[0]
        at = converted.index(anchor) + (len(anchor) if before else 0)
        converted = converted[:at] + line + converted[at:]
    if converted != src:
        path.write_text(converted)
        print(f"converted {path}")


def main(module):
    for name, spec in CARDS.items():
        for path in sorted(pathlib.Path(module).rglob(name)):
            if "/build/" not in str(path):
                convert(path, spec)


if __name__ == "__main__":
    main(sys.argv[1])
