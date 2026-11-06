#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 12: "those cards" read the stack entry.

Idempotent; run once per letter module:

    for m in magical-vibes-card/*/; do python3 scripts/step15-triggering-cards-cards.py "$m"; done

The "one or more cards" collectors (discarded, put into a graveyard from a library, left a graveyard) no longer bind
the event's cards into a TriggeringCardsAwareEffect; they set them on the stack entry as its triggering cards. So
Polluted Cistern's "each opponent loses 1 life for each card type among those cards" is the existing LoseLifeEffect
with the CardTypesAmongTriggeringCards amount, instead of EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect.
Spirit of Resilience and Diviner of Fates keep their effects, which already had no-argument constructors.
"""
import pathlib
import re
import sys

EFFECT = "com.github.laxika.magicalvibes.model.effect."

CARDS = {
    "PollutedCisternDimOubliette.java": {
        "replace": [
            ("""                                TriggerOptions.ONE_OR_MORE, List.of(new EffectRegistration(
                                        new EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect()))),""",
             """                                TriggerOptions.ONE_OR_MORE, List.of(new EffectRegistration(
                                        new LoseLifeEffect(new CardTypesAmongTriggeringCards(),
                                                LoseLifeRecipient.EACH_OPPONENT)))),"""),
        ],
        "add": ["com.github.laxika.magicalvibes.model.amount.CardTypesAmongTriggeringCards",
                EFFECT + "LoseLifeEffect",
                EFFECT + "LoseLifeRecipient"],
        "remove": [EFFECT + "EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect"],
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
