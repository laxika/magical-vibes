#!/usr/bin/env python3
"""Effect-slot-refactor step 14, run 16e: the step and phase cards whose abilities the step readers now read
themselves (StepAbilityReader), so each `addTrigger` is one ability, one stack object (CR 603.3):

- Ashling, Rimebound: its two precombat-main abilities drop TriggerMode.INDEPENDENT (each addTrigger is an ability of
  its own).
- Abstract Paintmage ("add {U}{R}") and Sanctum of Stone Fangs ("each opponent loses N life and you gain N life"): the
  one Oracle ability written as two precombat-main addTrigger calls is one ability of both effects.
- Tawnos Endures: the granted "at the beginning of your upkeep, if this card is exiled, ..." takes its intervening
  "if" as a TriggerOptions option instead of an authored ConditionalEffect.

Exact replacements; running it again changes nothing.

Usage: python3 scripts/step14-step-abilities-leftovers.py [--dry-run]
"""
import os
import sys

CARDS = "magical-vibes-card/{letter}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"

EDITS = {
    ("a", "AshlingRimebound"): [
        ("        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), TriggerMode.INDEPENDENT, mana);\n",
         "        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), mana);\n"),
        ("        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), TriggerMode.INDEPENDENT,\n",
         "        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),\n"),
        ("import com.github.laxika.magicalvibes.model.TriggerMode;\n", ""),
    ],
    ("a", "AbstractPaintmage"): [
        ("""        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new AwardRestrictedManaEffect(ManaColor.BLUE, 1, new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY))));
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new AwardRestrictedManaEffect(ManaColor.RED, 1, new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY))));
""",
         """        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new AwardRestrictedManaEffect(ManaColor.BLUE, 1, new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY))),
                new AwardRestrictedManaEffect(ManaColor.RED, 1, new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY))));
"""),
    ],
    ("s", "SanctumOfStoneFangs"): [
        ("""        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new LoseLifeEffect(shrinesYouControl, LoseLifeRecipient.EACH_OPPONENT));
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), new GainLifeEffect(shrinesYouControl));
""",
         """        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU),
                new LoseLifeEffect(shrinesYouControl, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(shrinesYouControl));
"""),
    ],
    ("t", "TawnosEndures"): [
        ("""                                new TriggeredAbility(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.EXILE),
                                new ConditionalEffect(new SourceCardInExile(), SequenceEffect.of(
                                        new PerpetuallyBoostSourceEffect(1, 1),
                                        new MayEffect(new ReturnSourceCardFromExileToBattlefieldEffect(false),
                                                "Return it to the battlefield?"))))))
""",
         """                                new TriggeredAbility(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.EXILE),
                                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInExile()),
                                SequenceEffect.of(
                                        new PerpetuallyBoostSourceEffect(1, 1),
                                        new MayEffect(new ReturnSourceCardFromExileToBattlefieldEffect(false),
                                                "Return it to the battlefield?")))))
"""),
        ("import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;\n", ""),
        ("import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;\n",
         "import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;\n"
         "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"),
    ],
}


def main():
    dry_run = "--dry-run" in sys.argv
    for (letter, name), edits in EDITS.items():
        path = CARDS.format(letter=letter, name=name)
        src = open(path).read()
        out = src
        for old, new in edits:
            if new and old in new and new in out:
                continue
            if old in out:
                if out.count(old) != 1:
                    sys.exit(f"{path}: ambiguous replacement {old!r}")
                out = out.replace(old, new)
            elif new and new not in out:
                sys.exit(f"{path}: neither the old nor the new form of {old!r}")
        if out != src:
            print(("would change " if dry_run else "changed ") + path)
            if not dry_run:
                open(path, "w").write(out)


if __name__ == "__main__":
    os.chdir(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
    main()
