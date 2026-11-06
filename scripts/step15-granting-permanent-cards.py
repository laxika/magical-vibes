#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 13b: the granting permanent is the stack entry's granting permanent.

Idempotent; run once per letter module:

    for m in magical-vibes-card/*/; do python3 scripts/step15-granting-permanent-cards.py "$m"; done

A granted triggered ability names the permanent that granted it (TriggeredAbility.grantingPermanentId), and the
collector that puts it on the stack copies that to the entry (StackEntry.grantingPermanentId); no effect is bound to
it any more. BecomePreparedEffect said "the granting permanent becomes prepared" by carrying a bound id and "the
source becomes prepared" by carrying none, so Stensian Sanguinist's granted "this creature becomes prepared" now says
so with BecomePreparedEffect.ofGrantingPermanent(). The other cards (Animal Friend, Optimus Prime, Trickster's
Talisman, Spare Dagger, Sunfire Torch, Dire Blunderbuss) keep their effects, whose unbound constructors are unchanged.
"""
import pathlib
import sys

CARDS = {
    "StensianSanguinistExsanguinate.java": [
        ("""                                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                                new BecomePreparedEffect())));""",
         """                                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                                BecomePreparedEffect.ofGrantingPermanent())));"""),
    ],
}


def convert(path, replacements):
    src = path.read_text()
    converted = src
    for old, new in replacements:
        if new in converted:
            continue
        if old not in converted:
            raise SystemExit(f"{path}: neither the old nor the new form of {old!r}")
        converted = converted.replace(old, new)
    if converted != src:
        path.write_text(converted)
        print(f"converted {path}")


def main(module):
    for name, replacements in CARDS.items():
        for path in sorted(pathlib.Path(module).rglob(name)):
            if "/build/" not in str(path):
                convert(path, replacements)


if __name__ == "__main__":
    main(sys.argv[1])
