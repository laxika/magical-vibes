#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 7: Confusion in the Ranks' "another player" and "it" read the triggering permanent.

Idempotent; run on the letter module:

    python3 scripts/step15-enters-rebinding-card.py magical-vibes-card/c

The target's "permanent another player controls that shares a card type with it" was written relative to the source
(PermanentControlledBySourceControllerPredicate, PermanentSharesCardTypeWithSourcePermanentPredicate) and only meant
the entering permanent because the collector rebuilt the effect and the target handler rebound the entry's source
snapshot to it. Both now name the triggering permanent.
"""
import pathlib
import sys

REPLACEMENTS = [
    ("PermanentControlledBySourceControllerPredicate", "PermanentControlledByTriggeringPermanentControllerPredicate"),
    ("PermanentSharesCardTypeWithSourcePermanentPredicate", "PermanentSharesCardTypeWithTriggeringPermanentPredicate"),
]


def main(module):
    for path in sorted(pathlib.Path(module).rglob("ConfusionInTheRanks.java")):
        src = path.read_text()
        converted = src
        for old, new in REPLACEMENTS:
            converted = converted.replace(old, new)
        if converted != src:
            path.write_text(converted)
            print(f"converted {path}")


if __name__ == "__main__":
    main(sys.argv[1])
