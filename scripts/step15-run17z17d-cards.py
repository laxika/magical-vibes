#!/usr/bin/env python3
"""Card changes of run 17z17d (effect-slot-refactor.md step 15).

Tawnos's Coffin (ATQ 68): "When this artifact leaves the battlefield or becomes untapped, return that exiled card
..." is a delayed triggered ability each resolution of the activated ability creates, so the class no longer prints
a "whenever this becomes untapped" ability that returned every pending card in one stack object:
ExileTargetCreatureAndAurasUntilSourceLeavesEffect's handler creates one DelayedSourceUntapsOrLeavesTrigger per
resolution, and ReturnCardsExiledWithSourceOnUntapEffect is gone.

    scripts/step15-run17z17d-cards.py [LETTER...]

Idempotent; fails if an edit does not apply.
"""
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

# letter -> (file, [(old, new)]); an edit is done when its old text is absent and its new text (if any) present.
EDITS = {
    "t": [(
        "magical-vibes-card/t/src/main/java/com/github/laxika/magicalvibes/cards/t/TawnossCoffin.java",
        [
            ("import com.github.laxika.magicalvibes.model.effect.ReturnCardsExiledWithSourceOnUntapEffect;\n", ""),
            ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n", ""),
            ("        addTrigger(Triggers.becomesUntapped(),\n"
             "                new ReturnCardsExiledWithSourceOnUntapEffect());\n", ""),
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
                if text.count(old) == 1:
                    text = text.replace(old, new)
                    changed = True
                elif old in text or (new and new not in text):
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
