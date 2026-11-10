#!/usr/bin/env python3
"""Step 15, run 17z13e4: the cards say what the death and leave handlers judged by an effect's class.

- Lifeline's "if another creature is on the battlefield" is its intervening "if" (`AnotherCreatureOnBattlefield`, CR
  603.4), no longer `RegisterDelayedReturnDyingCreatureUnderControlEffect.requireAnotherCreature`, which is gone (its
  eight-argument constructor is seven: Agyrem, Daretti, Scrap Savant's emblem, Resurrection Orb, Lifeline).
- Diabolic Servitude's "the creature put onto the battlefield with Diabolic Servitude" is its trigger's filter
  (`PermanentIsChosenPermanentPredicate`, as Havengul Mystery's).
- The Skullspore Nexus's "one or more nontoken creatures you control die" is `TriggerOptions.ONE_OR_MORE`; the total
  power of those creatures is the entry's event value.
- Brood of Cockroaches' and Triumph of Saint Katherine's "is put into your graveyard from the battlefield" is the
  trigger's filter (`PermanentOwnedBySourceControllerPredicate`: a card goes to its owner's graveyard).
- Laquatus's Champion's and Soul Scourge's "that player gains N life" is `GainLifeEffect` for the player the source
  remembered (`GainLifeRecipient.SOURCE_REMEMBERED_PLAYER`), not `TargetPlayerGainsLifeEffect` with a target the
  collector filled.

Run per letter module: step15-run17z13e4-cards.py [LETTER...] (a b d l r s t). Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
PKG = "com/github/laxika/magicalvibes/cards"

EIGHT_ARG_RETURN = ("false, null, 0, null, null, false, true, false)", "false, null, 0, null, null, false, true)")

EDITS = {
    "a/Agyrem.java": {"replace": [EIGHT_ARG_RETURN]},
    "d/DarettiScrapSavant.java": {"replace": [EIGHT_ARG_RETURN]},
    "r/ResurrectionOrb.java": {"replace": [EIGHT_ARG_RETURN]},
    "l/Lifeline.java": {
        "replace": [(
            """        RegisterDelayedReturnDyingCreatureUnderControlEffect returnDyingCreature =
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                        false, null, 0, null, null, false, true, true);
        // Lifeline itself counts when it is an animated creature that dies.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), returnDyingCreature);""",
            """        // "If another creature is on the battlefield": checked as it triggers, looking back at the creatures
        // that died with it, and again as it resolves (CR 603.4). Lifeline itself counts when it is an animated
        // creature that dies.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new AnotherCreatureOnBattlefield()),
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true));""",
        )],
        "imports": ["com.github.laxika.magicalvibes.model.condition.AnotherCreatureOnBattlefield",
                    "com.github.laxika.magicalvibes.model.trigger.TriggerOptions"],
    },
    "d/DiabolicServitude.java": {
        "replace": [(
            "addTrigger(Triggers.creatureDies(ObjectRelation.ANY), new ExileTriggeringCreatureAndReturnSourceToHandEffect());",
            "addTrigger(Triggers.creatureDies(new PermanentIsChosenPermanentPredicate(), ObjectRelation.ANY),\n"
            "                new ExileTriggeringCreatureAndReturnSourceToHandEffect());",
        )],
        "imports": ["com.github.laxika.magicalvibes.model.filter.PermanentIsChosenPermanentPredicate"],
    },
    "t/TheSkullsporeNexus.java": {
        "replace": [(
            "addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL),\n",
            "addTrigger(new TriggerCondition(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL)),\n"
            "                TriggerOptions.ONE_OR_MORE,\n",
        )],
        "imports": ["com.github.laxika.magicalvibes.model.trigger.TriggerCondition",
                    "com.github.laxika.magicalvibes.model.trigger.TriggerOptions"],
    },
    "b/BroodOfCockroaches.java": {
        "replace": [(
            "addTrigger(Triggers.dies(ObjectRelation.THIS),",
            "addTrigger(Triggers.dies(new PermanentOwnedBySourceControllerPredicate(), ObjectRelation.THIS),",
        )],
        "imports": ["com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate"],
    },
    "t/TriumphOfSaintKatherine.java": {
        "replace": [(
            "addTrigger(Triggers.dies(ObjectRelation.THIS),\n",
            "// \"Is put into your graveyard from the battlefield\": a card goes to its owner's graveyard.\n"
            "        addTrigger(Triggers.dies(new PermanentOwnedBySourceControllerPredicate(), ObjectRelation.THIS),\n",
        )],
        "imports": ["com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate"],
    },
    "l/LaquatussChampion.java": {
        "replace": [("new TargetPlayerGainsLifeEffect(6)",
                     "new GainLifeEffect(new Fixed(6), GainLifeRecipient.SOURCE_REMEMBERED_PLAYER)")],
        "imports": ["com.github.laxika.magicalvibes.model.amount.Fixed",
                    "com.github.laxika.magicalvibes.model.effect.GainLifeEffect",
                    "com.github.laxika.magicalvibes.model.effect.GainLifeRecipient"],
        "drop_imports": ["com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect"],
    },
    "s/SoulScourge.java": {
        "replace": [("new TargetPlayerGainsLifeEffect(3)",
                     "new GainLifeEffect(new Fixed(3), GainLifeRecipient.SOURCE_REMEMBERED_PLAYER)")],
        "imports": ["com.github.laxika.magicalvibes.model.amount.Fixed",
                    "com.github.laxika.magicalvibes.model.effect.GainLifeEffect",
                    "com.github.laxika.magicalvibes.model.effect.GainLifeRecipient"],
        "drop_imports": ["com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect"],
    },
}


def add_import(text, name):
    line = f"import {name};\n"
    if line in text:
        return text
    imports = [l for l in text.splitlines(keepends=True) if l.startswith("import com.github")]
    ordered = sorted(imports + [line])
    index = ordered.index(line)
    if index + 1 < len(ordered):
        return text.replace(ordered[index + 1], line + ordered[index + 1], 1)
    return text.replace(ordered[index - 1], ordered[index - 1] + line, 1)


def main():
    letters = set(sys.argv[1:]) or {key.split("/")[0] for key in EDITS}
    for key, edit in EDITS.items():
        letter, name = key.split("/")
        if letter not in letters:
            continue
        path = CARDS / letter / "src/main/java" / PKG / letter / name
        text = path.read_text()
        before = text
        for old, new in edit.get("replace", []):
            if new not in text:
                assert old in text, (key, old)
                text = text.replace(old, new)
        for name_ in edit.get("imports", []):
            text = add_import(text, name_)
        for name_ in edit.get("drop_imports", []):
            text = text.replace(f"import {name_};\n", "")
        path.write_text(text)
        print(f"{key}: {'changed' if text != before else 'unchanged'}")


if __name__ == "__main__":
    main()
