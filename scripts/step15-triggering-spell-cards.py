#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 14: the triggering spell is the stack entry's triggering spell.

Idempotent; run once per letter module:

    for m in magical-vibes-card/*/; do python3 scripts/step15-triggering-spell-cards.py "$m"; done

The spell a spell-cast trigger was about is the stack entry's triggering spell (StackEntry.setTriggeringSpell, a copy
of its stack entry as the ability triggered); no effect is rebuilt with the spell, its caster, its colors or its mana
value bound in. The cards whose effect only existed to be rebuilt with the mana value now use existing effects:

- Geometric Nexus: PutCountersOnSelfEffect(CHARGE, new TargetSpellManaValue()) for the removed
  PutCountersOnSelfEqualToTriggeringSpellManaValueEffect.
- Saruman, the White Hand: AmassGoblinsEffect(new TargetSpellManaValue(), ORC) for the removed
  AmassGoblinsEqualToCastSpellManaValueEffect.
- Saruman of Many Colors: ExileTargetCardFromGraveyardAndMayCastCopyEffect with a CardMaxManaValueXPredicate in its
  filter, the reflexive ability's X being the entry's event value (the spell's mana value), for the removed
  ExileTargetCardFromOpponentGraveyardAndMayCastCopyAtTriggeringSpellManaValueEffect.
- Bill Potts: one TriggeredAbility of two trigger conditions (an instant or sorcery spell, an ability, each "that
  targets only Bill Potts") with TriggerLimit.ONCE_EACH_TURN and CopyTriggeringSpellOrAbilityEffect, for the removed
  OncePerTurnTriggerEffect around two condition-as-effect copy descriptors.
"""
import pathlib
import sys

CARDS = {
    "GeometricNexus.java": [
        ("import com.github.laxika.magicalvibes.model.amount.XValue;\n",
         "import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;\n"
         "import com.github.laxika.magicalvibes.model.amount.XValue;\n"),
        ("import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEqualToTriggeringSpellManaValueEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;\n"),
        ("                new PutCountersOnSelfEqualToTriggeringSpellManaValueEffect(CounterType.CHARGE));",
         "                new PutCountersOnSelfEffect(CounterType.CHARGE, new TargetSpellManaValue()));"),
    ],
    "SarumanTheWhiteHand.java": [
        ("import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEqualToCastSpellManaValueEffect;\n",
         "import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;\n"
         "import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;\n"),
        ("                new AmassGoblinsEqualToCastSpellManaValueEffect(CardSubtype.ORC));",
         "                new AmassGoblinsEffect(new TargetSpellManaValue(), CardSubtype.ORC));"),
    ],
    "SarumanOfManyColors.java": [
        ("import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromOpponentGraveyardAndMayCastCopyAtTriggeringSpellManaValueEffect;\n",
         "import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;\n"),
        ("import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;\n",
         "import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;\n"
         "import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;\n"),
        ("""                        new QueueReflexiveAbilityEffect(
                                new ExileTargetCardFromOpponentGraveyardAndMayCastCopyAtTriggeringSpellManaValueEffect(
                                        noncreatureSpell, GraveyardSearchScope.OPPONENT_GRAVEYARD))));""",
         """                        new QueueReflexiveAbilityEffect(
                                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                                        new CardAllOfPredicate(List.of(noncreatureSpell, new CardMaxManaValueXPredicate())),
                                        GraveyardSearchScope.OPPONENT_GRAVEYARD),
                                false, true)));"""),
    ],
    "BillPotts.java": [
        ("""import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
""", """import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
"""),
        ("""import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
""", """import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
"""),
        ("""        addTrigger(Triggers.youCastSpell(), new OncePerTurnTriggerEffect(
                CopyControllerCastSpellOnSpellCastEffect.withCastTargetCondition(
                        instantOrSorcery, targetsOnlyBill, Set.of())));
        addTrigger(Triggers.youActivateAbility(), new OncePerTurnTriggerEffect(
                new CopyControllerActivatedAbilityTriggerEffect(
                        null, null, false, false, targetsOnlyBill)));""",
         """        addTrigger(List.of(
                        Triggers.youCastSpell().withFilter(instantOrSorcery).withStackEntryFilter(targetsOnlyBill),
                        Triggers.youActivateAbility().withStackEntryFilter(targetsOnlyBill)),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CopyTriggeringSpellOrAbilityEffect());"""),
    ],
}


def convert(path, replacements):
    src = path.read_text()
    converted = src
    for old, new in replacements:
        if new in converted:
            continue
        if old not in converted:
            raise SystemExit(f"{path}: neither the old nor the new form of {old!r}")
        converted = converted.replace(old, new)
    if converted != src:
        path.write_text(converted)
        print(f"converted {path}")


def main(module):
    for name, replacements in CARDS.items():
        for path in sorted(pathlib.Path(module).rglob(name)):
            if "/build/" not in str(path):
                convert(path, replacements)


if __name__ == "__main__":
    main(sys.argv[1])
