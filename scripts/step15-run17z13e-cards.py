#!/usr/bin/env python3
"""Step 15, run 17z13e: the cards write the conditions and abilities the collectors judged or merged by class.

- Ob Nixilis, Captive Kingpin: "whenever one or more opponents each lose exactly 1 life" was judged by the opponent
  life-loss collector's `SequenceEffect` handler; it is the ability's event condition, the life lost being the event's
  value (`EventValueAtLeast(1)` and not `EventValueAtLeast(2)`).
- Grave Researcher: "surveil 1. Then if ..., this creature becomes prepared" is one ability (CR 603.3), written as two;
  the your-upkeep reader's `SurveilEffect` branch merged every upkeep effect of the permanent into one entry. It is one
  `SequenceEffect` now.
- Corrupted Roots, Contaminated Ground, Pooling Venom ("its controller loses 2 life") and Chronic Flooding ("its
  controller mills three cards") said `TARGET_PERMANENT_CONTROLLER` / `TARGET_PLAYER`, which the enchanted-tap
  collector's class handlers fed by baking the tapped land or its controller into `targetId`; they read the tapped land
  the entry records, `TRIGGERING_PERMANENT_CONTROLLER` (its controller as the ability resolves, else its last known).
- Haphazard Bombardment: "if two or more permanents you don't control have an aim counter on them" was judged by the
  your-end-step reader's branch for its effect's class; it is the ability's intervening "if" (CR 603.4).

Run per letter module (or with no argument for all). Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = "magical-vibes-card/{0}/src/main/java/com/github/laxika/magicalvibes/cards/{0}/{1}.java"

EDITS = {
    ("o", "ObNixilisCaptiveKingpin"): [
        ("import com.github.laxika.magicalvibes.model.CounterType;\n",
         "import com.github.laxika.magicalvibes.model.CounterType;\n"
         "import com.github.laxika.magicalvibes.model.condition.AllOf;\n"
         "import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;\n"
         "import com.github.laxika.magicalvibes.model.condition.NotCondition;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n",
         "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n\nimport java.util.List;\n"),
        ("        addTrigger(Triggers.opponentLosesLife(), SequenceEffect.of(\n"
         "                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),\n"
         "                new ExileTopCardsMayPlayUntilNextEndStepEffect(1)));\n",
         "        // \"Whenever one or more opponents each lose exactly 1 life\": the life lost is the event's value.\n"
         "        addTrigger(Triggers.opponentLosesLife(),\n"
         "                TriggerOptions.DEFAULT.withEventCondition(new AllOf(List.of(\n"
         "                        new EventValueAtLeast(1), new NotCondition(new EventValueAtLeast(2))))),\n"
         "                SequenceEffect.of(\n"
         "                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),\n"
         "                        new ExileTopCardsMayPlayUntilNextEndStepEffect(1)));\n"),
    ],
    ("g", "GraveResearcherReanimate"): [
        ("import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;\n"
         "import com.github.laxika.magicalvibes.model.effect.SequenceEffect;\n"),
        ("        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SurveilEffect(1));\n"
         "        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(\n"
         "                new GraveyardCardThreshold(3, new CardTypePredicate(CardType.CREATURE)),\n"
         "                new BecomePreparedEffect()));\n",
         "        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(\n"
         "                new SurveilEffect(1),\n"
         "                new ConditionalEffect(\n"
         "                        new GraveyardCardThreshold(3, new CardTypePredicate(CardType.CREATURE)),\n"
         "                        new BecomePreparedEffect())));\n"),
    ],
    ("h", "HaphazardBombardment"): [
        ("import com.github.laxika.magicalvibes.model.CounterType;\n",
         "import com.github.laxika.magicalvibes.model.CounterType;\n"
         "import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;\n"),
        ("import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;\n",
         "import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n",
         "import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;\n"
         "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n",
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n\nimport java.util.List;\n"),
        ("        addTrigger(Triggers.endStep(ActivePlayer.YOU),\n"
         "                new DestroyRandomOpponentPermanentWithCounterEffect(CounterType.AIM, 2));\n",
         "        addTrigger(Triggers.endStep(ActivePlayer.YOU),\n"
         "                TriggerOptions.DEFAULT.withInterveningIf(new AnyPlayerControlsPermanentCount(2,\n"
         "                        new PermanentAllOfPredicate(List.of(\n"
         "                                new PermanentHasCountersPredicate(CounterType.AIM),\n"
         "                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))))),\n"
         "                new DestroyRandomOpponentPermanentWithCounterEffect(CounterType.AIM, 2));\n"),
    ],
}
for letter, name in (("c", "CorruptedRoots"), ("c", "ContaminatedGround"), ("p", "PoolingVenom")):
    EDITS[(letter, name)] = [("LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER)",
                              "LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)")]
EDITS[("c", "ChronicFlooding")] = [("new MillEffect(3, MillRecipient.TARGET_PLAYER)",
                                    "new MillEffect(3, MillRecipient.TRIGGERING_PERMANENT_CONTROLLER)")]


def main():
    letters = set(sys.argv[1:])
    for (letter, name), edits in EDITS.items():
        if letters and letter not in letters:
            continue
        path = ROOT / CARDS.format(letter, name)
        text = path.read_text()
        changed = False
        for old, new in edits:
            if new in text:
                continue
            assert old in text, f"{name}: {old!r}"
            text = text.replace(old, new, 1)
            changed = True
        if changed:
            path.write_text(text)
            print("converted", name)


if __name__ == "__main__":
    main()
