#!/usr/bin/env python3
"""Step 15, run 17z10: the copy collector's rebinds are gone.

`SpellCastTriggerCollectorService.collectCopyAbility` kept a class-keyed branch for a lone
`LoseLifeEffect(TARGET_PLAYER)` (Mage Hunter's "whenever an opponent copies an instant or sorcery spell, they lose
1 life"), which bound the copier as the entry's non-targeting `targetId`. "They" is the player who copied the spell,
which the copy collector's generic entry records as its non-targeting player for every ability, so the recipient is
`TRIGGERING_PLAYER`. (Parnesse, the Subtle Brush's `CopyCopiedSpellForTargetPlayerEffect` keeps its form: the effect
lost its snapshot and reads the entry's triggering spell.)

Idempotent, per letter module.

Usage: step15-run17z10-cards.py <letter> [<letter> ...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

MAGE_HUNTER = ("""        addTrigger(Triggers.opponentCopiesSpell().withFilter(instantOrSorcery),
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));""",
               """        addTrigger(Triggers.opponentCopiesSpell().withFilter(instantOrSorcery),
                new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PLAYER));""")

CARDS = {
    "m": {"MageHunter": [MAGE_HUNTER]},
}


def convert(path, replacements):
    text = path.read_text()
    if all(new in text for _, new in replacements):
        return False
    for old, new in replacements:
        if old not in text:
            raise SystemExit(f"{path}: the expected text is missing: {old.strip()[:80]}")
        text = text.replace(old, new)
    path.write_text(text)
    return True


def main(letters):
    for letter in letters:
        for name, replacements in CARDS.get(letter, {}).items():
            path = (ROOT / f"magical-vibes-card/{letter}/src/main/java" / PKG.replace(".", "/")
                    / f"cards/{letter}/{name}.java")
            print(f"{name}: {'converted' if convert(path, replacements) else 'already converted'}")


if __name__ == "__main__":
    main(sys.argv[1:] or sorted(CARDS))
