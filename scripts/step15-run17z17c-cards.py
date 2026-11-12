#!/usr/bin/env python3
"""Card changes of run 17z17c (effect-slot-refactor.md step 15).

Wizened Mentor (DRC 8): "Whenever an opponent activates an ability of a permanent that isn't a mana ability, ..."
had no source filter, so an ability of a card in a hand or a graveyard (cycling, ninjutsu, unearth) triggered it.
"An ability of a permanent" is the trigger's source filter: withSource(new PermanentTruePredicate()), which an
ability of a card that isn't a permanent never matches (CR 109.2).

    scripts/step15-run17z17c-cards.py [LETTER...]

Idempotent; fails if an edit does not apply.
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

# letter -> (file, [(old, new)]); an edit is done when its old text is absent and its new text present.
EDITS = {
    "w": [(
        "magical-vibes-card/w/src/main/java/com/github/laxika/magicalvibes/cards/w/WizenedMentor.java",
        [
            ("import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;\n",
             "import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;\n"
             "import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;\n"),
            ("        addTrigger(Triggers.opponentActivatesNonManaAbility(),\n",
             "        addTrigger(Triggers.opponentActivatesNonManaAbility().withSource(new PermanentTruePredicate()),\n"),
        ],
    )],
}


def main():
    letters = sys.argv[1:] or sorted(EDITS)
    failed = False
    for letter in letters:
        for relative, edits in EDITS.get(letter, []):
            path = ROOT / relative
            text = path.read_text(encoding="utf-8")
            changed = False
            for old, new in edits:
                if new in text:
                    continue
                if text.count(old) == 1:
                    text = text.replace(old, new)
                    changed = True
                else:
                    print(f"FAILED (edit does not apply): {relative}: {old.strip()[:60]}")
                    failed = True
            if changed and not failed:
                path.write_text(text, encoding="utf-8")
                print(f"changed: {relative}")
            elif not failed:
                print(f"already done: {relative}")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
