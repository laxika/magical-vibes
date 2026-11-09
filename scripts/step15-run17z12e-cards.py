#!/usr/bin/env python3
"""Step 15, run 17z12e: Lurker in the Deep writes its "during your turn" as its seek ability's event condition (the
seek collector judged it by the effect's class), and the ability's effect reads "those cards" from the stack entry
(the collector rebuilt it with the sought cards)."""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"


def edit(rel, pairs):
    path = CARDS / rel
    src = path.read_text()
    for old, new in pairs:
        if src.count(old) != 1:
            sys.exit(f"{rel}: expected one match of\n{old}")
        src = src.replace(old, new)
    path.write_text(src)
    print("converted", rel)


edit("l/src/main/java/com/github/laxika/magicalvibes/cards/l/LurkerInTheDeep.java", [
    ("import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;\n",
     "import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;\n"
     "import com.github.laxika.magicalvibes.model.condition.ControllerTurn;\n"),
    ("addTrigger(Triggers.youSeek(), new ConjureDuplicatesOfSoughtCardsAndManifestEffect());",
     "addTrigger(Triggers.youSeek(), TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn()),\n"
     "                new ConjureDuplicatesOfSoughtCardsAndManifestEffect());"),
])
