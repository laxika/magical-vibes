#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 6: Martyr's Bond's "shares a card type with it" reads the triggering permanent.

Idempotent; run on the letter module:

    python3 scripts/step15-martyrs-bond-card.py magical-vibes-card/m

PermanentSharesCardTypeWithSourcePermanentPredicate (which only compared with "it" because the collector rebound the
entry's source snapshot to the permanent that died) becomes PermanentSharesCardTypeWithTriggeringPermanentPredicate.
"""
import pathlib
import sys

OLD = "PermanentSharesCardTypeWithSourcePermanentPredicate"
NEW = "PermanentSharesCardTypeWithTriggeringPermanentPredicate"


def main(module):
    for path in sorted(pathlib.Path(module).rglob("MartyrsBond.java")):
        src = path.read_text()
        converted = src.replace(OLD, NEW)
        if converted != src:
            path.write_text(converted)
            print(f"converted {path}")


if __name__ == "__main__":
    main(sys.argv[1])
