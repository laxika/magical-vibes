#!/usr/bin/env python3
"""Step 15, follow-up after run 17y5: Sower of Discord's "whenever damage is dealt to one of the chosen players"
triggers once for each damage event, not once for each source.

Run 17y4 wrote it on `Triggers.sourceDealsDamageToPermanentOrPlayer()`, which triggers once for each source and
recipient, so two creatures dealing combat damage to a chosen player at once made two triggers. The new
`Triggers.playerDealtDamage()` triggers once for each player and damage event, the damage of every source summed
(CR 603.2c; Boros Reckoner's ruling). The damaged-player filter and the effect are unchanged.

Idempotent: a file already converted is left alone. Per letter module.

Usage: step15-sower-per-event-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PATH = ROOT / "magical-vibes-card/s/src/main/java/com/github/laxika/magicalvibes/cards/s/SowerOfDiscord.java"
OLD = "        addTrigger(Triggers.sourceDealsDamageToPermanentOrPlayer()\n"
NEW = "        addTrigger(Triggers.playerDealtDamage()\n"


def main():
    letters = set("abcdefghijklmnopqrstuvwxyz") if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not letters:
        raise SystemExit(__doc__)
    if "s" not in letters:
        return
    text = PATH.read_text()
    if NEW in text:
        return
    if text.count(OLD) != 1:
        raise SystemExit("SowerOfDiscord: the trigger to convert was not found")
    PATH.write_text(text.replace(OLD, NEW, 1))
    print("converted SowerOfDiscord")


if __name__ == "__main__":
    main()
