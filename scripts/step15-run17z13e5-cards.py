#!/usr/bin/env python3
"""Step 15, run 17z13e5: two cards say what their class-keyed enter handlers judged or chose.

`EnterTriggerCollectorService.handleAnyCreatureExileUntilSourceLeaves` judged "if there are two or more other creatures
on the battlefield" by `ExileTriggeringCreatureUntilSourceLeavesEffect.minimumOtherCreatures` as the ability triggered
(and the effect's handler again as it resolved). It is the ability's intervening "if" now,
`TriggerOptions.DEFAULT.withInterveningIf(new AnotherCreatureOnBattlefield(2))`, checked as it triggers and again as
it resolves (CR 603.4); the effect has no count.

Loran, Disciple of History's "return target artifact card from your graveyard to your hand" reached
`handleCreatureEnterReturnToHand` by its effect's class, which began the graveyard choice as the ability was
collected; its effect declared no target, so the watch's default would put it on the stack with none. It declares the
graveyard card as its target (`ReturnTargetCardsFromGraveyardToHandEffect.exactlyOneForTriggeredAbility`), chosen as the
ability goes on the stack (CR 603.3d), and no ability triggers with no artifact card to target, as before.

Run per letter module: step15-run17z13e5-cards.py [LETTER...] (l p). Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
PKG = "com/github/laxika/magicalvibes/cards"

EDITS = {
    "l/LoranDiscipleOfHistory.java": [
        ("ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(new CardTypePredicate(CardType.ARTIFACT))",
         "ReturnTargetCardsFromGraveyardToHandEffect.exactlyOneForTriggeredAbility(\n"
         "                        new CardTypePredicate(CardType.ARTIFACT))"),
    ],
    "p/Portcullis.java": [
        ("import com.github.laxika.magicalvibes.model.Card;\n",
         "import com.github.laxika.magicalvibes.model.Card;\n"
         "import com.github.laxika.magicalvibes.model.condition.AnotherCreatureOnBattlefield;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n",
         "import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n"
         "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"),
        ("""        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY),
                new ExileTriggeringCreatureUntilSourceLeavesEffect(2));""",
         """        // "If there are two or more other creatures on the battlefield": checked as the creature enters and again
        // as the ability resolves (CR 603.4).
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new AnotherCreatureOnBattlefield(2)),
                new ExileTriggeringCreatureUntilSourceLeavesEffect());"""),
    ],
}


def main():
    letters = set(sys.argv[1:]) or {key.split("/")[0] for key in EDITS}
    for key, edits in EDITS.items():
        if key.split("/")[0] not in letters:
            continue
        path = CARDS / key.split("/")[0] / "src/main/java" / PKG / key
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                sys.exit(f"{key}: pattern not found: {old[:60]!r}")
            text = text.replace(old, new, 1)
        path.write_text(text)
        print("updated", key)


if __name__ == "__main__":
    main()
