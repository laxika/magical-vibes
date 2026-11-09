#!/usr/bin/env python3
"""Step 15, run 17z13: Phage the Untouchable writes "if you didn't cast it from your hand" as its enters ability's
intervening "if" and "you lose the game" as the existing `ControllerLosesGameEffect` (the enters resolver rewrote its
`LoseGameIfNotCastFromHandEffect` into a `TargetPlayerLosesGameEffect` bound to the controller). Idempotent."""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"


def edit(rel, pairs, done_marker):
    path = CARDS / rel
    src = path.read_text()
    if done_marker in src:
        print("already converted", rel)
        return
    for old, new in pairs:
        if src.count(old) != 1:
            sys.exit(f"{rel}: expected one match of\n{old}")
        src = src.replace(old, new)
    path.write_text(src)
    print("converted", rel)


edit("p/src/main/java/com/github/laxika/magicalvibes/cards/p/PhageTheUntouchable.java", [
    ("import com.github.laxika.magicalvibes.model.Card;\n",
     "import com.github.laxika.magicalvibes.model.Card;\n"
     "import com.github.laxika.magicalvibes.model.Zone;\n"
     "import com.github.laxika.magicalvibes.model.condition.CastFromZone;\n"
     "import com.github.laxika.magicalvibes.model.condition.NotCondition;\n"
     "import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;\n"),
    ("import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;\n", ""),
    ("import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n",
     "import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n"
     "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"),
    ("        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseGameIfNotCastFromHandEffect());\n",
     "        addTrigger(Triggers.enters(ObjectRelation.THIS),\n"
     "                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new CastFromZone(Zone.HAND))),\n"
     "                new ControllerLosesGameEffect());\n"),
], "ControllerLosesGameEffect")
