#!/usr/bin/env python3
"""Card changes of run 17z17b (effect-slot-refactor.md step 15).

Super-Soldier Serum (MSH 38): "Whenever enchanted creature attacks or blocks, attach any number of target Equipment
you control to it" is the Aura's own ability, controlled by the Aura's controller. Its attack ability reached the
stack through the group-by-group target walk, which named the Aura and its controller while the plain entry of an
unflagged Aura names the enchanted creature and its controller; the walk now follows the plain entry, so the card
says what its text says: setAttackTriggerOwnedByAura(true).

    scripts/step15-run17z17b-cards.py [LETTER...]

Idempotent; fails if an edit does not apply.
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

EDITS = {
    "s": [(
        "magical-vibes-card/s/src/main/java/com/github/laxika/magicalvibes/cards/s/SuperSoldierSerum.java",
        "    public SuperSoldierSerum() {\n        target(TargetFilters.creature());\n",
        "    public SuperSoldierSerum() {\n        setAttackTriggerOwnedByAura(true);\n"
        "        target(TargetFilters.creature());\n",
    )],
}


def main():
    letters = sys.argv[1:] or sorted(EDITS)
    failed = False
    for letter in letters:
        for relative, old, new in EDITS.get(letter, []):
            path = ROOT / relative
            text = path.read_text(encoding="utf-8")
            if new in text:
                print(f"already done: {relative}")
            elif text.count(old) == 1:
                path.write_text(text.replace(old, new), encoding="utf-8")
                print(f"changed: {relative}")
            else:
                print(f"FAILED (edit does not apply): {relative}")
                failed = True
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
