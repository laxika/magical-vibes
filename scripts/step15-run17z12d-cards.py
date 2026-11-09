#!/usr/bin/env python3
"""Step 15, run 17z12d: the cards whose trigger condition a class-keyed handler judged write it as the trigger's
filter: Oskar, Rubbish Reclaimer's "a nonland card", Hedge Shredder's "one or more land cards" and Colossal
Grave-Reaver's "one or more creature cards"."""
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


edit("o/src/main/java/com/github/laxika/magicalvibes/cards/o/OskarRubbishReclaimer.java", [
    ("import com.github.laxika.magicalvibes.model.Card;\n",
     "import com.github.laxika.magicalvibes.model.Card;\nimport com.github.laxika.magicalvibes.model.CardType;\n"),
    ("import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;\n",
     "import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;\n"
     "import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;\n"
     "import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;\n"),
    ("addTrigger(Triggers.youDiscardCard(), new CastDiscardedCardFromGraveyardEffect());",
     "addTrigger(Triggers.youDiscardCard().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.LAND))),\n"
     "                new CastDiscardedCardFromGraveyardEffect());"),
])
edit("h/src/main/java/com/github/laxika/magicalvibes/cards/h/HedgeShredder.java", [
    ("Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN,\n                ObjectRelation.YOU_OWN)",
     "Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.LAND_CARD,\n                ObjectRelation.YOU_OWN)"),
])
edit("c/src/main/java/com/github/laxika/magicalvibes/cards/c/ColossalGraveReaver.java", [
    ("Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN,\n                ObjectRelation.YOU_OWN)",
     "Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE,\n                ObjectRelation.YOU_OWN)"),
])
