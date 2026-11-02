#!/usr/bin/env python3
"""Step 14, run 1 of effect-slot-refactor.md: rewrites the agent-docs rows that describe the source's own zone-change
abilities with trigger-only wrappers, the way scripts/step14-pilot-options.py rewrote the cards. Each replacement
must match exactly once. Usage: python3 scripts/step14-pilot-docs.py
"""
import sys

DOCS = "agent-docs/"

REPLACEMENTS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `TriggeringPermanentConditionalEffect(Not(PermanentHasSubtypePredicate(SPIRIT)), "
         "ReturnDyingCreatureToOwnerBattlefieldEffect(CHARGE, 4, null, Set.of(), false, false, List.of(SPIRIT, "
         "DETECTIVE)))` | `Triggers.dies(THIS)` |",
         "| `ReturnDyingCreatureToOwnerBattlefieldEffect(CHARGE, 4, null, Set.of(), false, false, List.of(SPIRIT, "
         "DETECTIVE))` | `Triggers.dies(Not(PermanentHasSubtypePredicate(SPIRIT)), THIS)`: the \"if it's not a "
         "Spirit\" is the trigger's filter, judged as it died (CR 603.10a) |"),
        ("`Triggers.dies(THIS) ConditionalEffect(SourcePowerAtLeast(1), "
         "RegisterOneShotCreatureSpellPerpetualPowerBoostEffect())`",
         "`Triggers.dies(THIS)` with `TriggerOptions.DEFAULT.withInterveningIf(SourcePowerAtLeast(1))` + "
         "`RegisterOneShotCreatureSpellPerpetualPowerBoostEffect()`"),
        ("+ `Triggers.dies(THIS) SacrificeOnlyEffect(DraftCardFromSpellbookEffect(SPELLBOOK))` | "
         "Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL) + Triggers.dies(THIS) | Slayer's Bounty (YMID 10). "
         "The ally-sacrifice trigger handles other Clues; the sacrifice-only death trigger handles sacrificing "
         "Slayer's Bounty itself. |",
         "+ `Triggers.sacrificed(THIS) DraftCardFromSpellbookEffect(SPELLBOOK)` | "
         "Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL) + Triggers.sacrificed(THIS) | Slayer's Bounty "
         "(YMID 10). The ally-sacrifice trigger handles other Clues; the \"when you sacrifice this\" trigger handles "
         "sacrificing Slayer's Bounty itself. |"),
        ("+ `ConditionalEffect(SourceCounterThreshold(1, ANY), PutCountersOnTargetForEachLeavingSourceCountersEffect("
         "PermanentTruePredicate()))` on `Triggers.leavesBattlefield(THIS)`",
         "+ `PutCountersOnTargetForEachLeavingSourceCountersEffect(PermanentTruePredicate())` on "
         "`Triggers.leavesBattlefield(new PermanentHasCountersPredicate(ANY), THIS)`"),
        ("-> `Triggers.dies(THIS)` + `DyingPermanentWasCreatureConditionalEffect(new "
         "ReturnSourceCardFromGraveyardToBattlefieldEffect(false, Set.of(CardType.ENCHANTMENT)))` (Enduring "
         "Innocence). The death collector evaluates the condition from the permanent's status as it died.",
         "-> `Triggers.dies(Triggers.CREATURE, ObjectRelation.THIS)` + `new "
         "ReturnSourceCardFromGraveyardToBattlefieldEffect(false, Set.of(CardType.ENCHANTMENT))` (Enduring "
         "Innocence). The filter is judged on the card types the permanent died with (CR 603.10a)."),
        ("-> `Triggers.dies(THIS)` + `DyingPermanentWasNonlandCreatureConditionalEffect(new "
         "ReturnDyingCreatureToBattlefieldAsNamedLandEffect(\"Moon\"))`;",
         "-> `Triggers.dies(new PermanentAllOfPredicate(List.of(Triggers.CREATURE, Triggers.NONLAND)), "
         "ObjectRelation.THIS)` + `new ReturnDyingCreatureToBattlefieldAsNamedLandEffect(\"Moon\")`;"),
        ("| \"Whenever a modified creature you control leaves the battlefield, put a +1/+1 counter on this "
         "artifact\" | `Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` + "
         "`TriggeringPermanentConditionalEffect(new PermanentIsModifiedPredicate(), new PutCountersOnSourceEffect(1, "
         "1, 1))` | trigger |",
         "| \"Whenever a modified creature you control leaves the battlefield, put a +1/+1 counter on this "
         "artifact\" | `Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` + "
         "`TriggeringPermanentConditionalEffect(new PermanentIsModifiedPredicate(), new PutCountersOnSourceEffect(1, "
         "1, 1))`, and for this artifact itself as a modified creature `Triggers.leavesBattlefield(new "
         "PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), new PermanentIsModifiedPredicate())), "
         "ObjectRelation.THIS)` + `new PutCountersOnSourceEffect(1, 1, 1)` | trigger |"),
        ("`Triggers.dies(THIS) ConditionalEffect(SourceCounterThreshold(1, MINUS_ONE_MINUS_ONE), "
         "SequenceEffect(",
         "`Triggers.dies(THIS)` with `TriggerOptions.DEFAULT.withInterveningIf(SourceCounterThreshold(1, "
         "MINUS_ONE_MINUS_ONE))` + `SequenceEffect("),
        ("| \"When this creature dies, if it was blocked this turn, you gain N life\" | `ConditionalEffect(new "
         "SourceWasBlockedThisTurn(), new GainLifeEffect(N))` | `Triggers.dies(THIS)` |",
         "| \"When this creature dies, if it was blocked this turn, you gain N life\" | `new GainLifeEffect(N)` with "
         "`TriggerOptions.DEFAULT.withInterveningIf(new SourceWasBlockedThisTurn())` | `Triggers.dies(THIS)` |"),
        ("| `ConditionalEffect(new DuringCombat(), new CreateTokenEffect(4, \"Dog\", 1, 1, GREEN, List.of(DOG), "
         "Set.of(), Set.of()))` | `Triggers.dies(THIS)` |",
         "| `new CreateTokenEffect(4, \"Dog\", 1, 1, GREEN, List.of(DOG), Set.of(), Set.of())` with "
         "`TriggerOptions.DEFAULT.withInterveningIf(new DuringCombat())` | `Triggers.dies(THIS)` |"),
        ("+ `ConditionalEffect(new NotCondition(new SourceIsToken()), new CreateTokenCopyOfSourceEffect(false, 1, "
         "Set.of(CardType.ARTIFACT)))` | Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL) + "
         "Triggers.dies(THIS) |",
         "+ `new CreateTokenCopyOfSourceEffect(false, 1, Set.of(CardType.ARTIFACT))` | "
         "Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL) + Triggers.dies(Triggers.NONTOKEN, THIS) |"),
    ],
    "EFFECTS_INDEX.md": [
        ("encodes \"dies during combat\" — Mongrel Pack's `Triggers.dies(THIS)` `ConditionalEffect`)",
         "encodes \"dies during combat\" — Mongrel Pack's `Triggers.dies(THIS)` intervening-if option)"),
        ("| `DyingPermanentWasCreatureConditionalEffect` | `(CardEffect wrapped)` |", None),
        ("| `DyingPermanentWasNonlandCreatureConditionalEffect` | `(CardEffect wrapped)` |", None),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("(Fyndhorn Druid: `Triggers.dies(THIS) ConditionalEffect(new SourceWasBlockedThisTurn(), new "
         "GainLifeEffect(4))`)",
         "(Fyndhorn Druid: `addTrigger(Triggers.dies(ObjectRelation.THIS), "
         "TriggerOptions.DEFAULT.withInterveningIf(new SourceWasBlockedThisTurn()), new GainLifeEffect(4))`)"),
        ("- `DyingPermanentWasCreatureConditionalEffect(CardEffect wrapped)` —", None),
        ("- `DyingPermanentWasNonlandCreatureConditionalEffect(CardEffect wrapped)` —", None),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("`d/DragonbornImmolator.java` + `ConditionalEffect(SourcePowerAtLeast(1), "
         "RegisterOneShotCreatureSpellPerpetualPowerBoostEffect)`",
         "`d/DragonbornImmolator.java` + `TriggerOptions.DEFAULT.withInterveningIf(SourcePowerAtLeast(1))` + "
         "`RegisterOneShotCreatureSpellPerpetualPowerBoostEffect`"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("+ `Triggers.dies(THIS) ConditionalEffect(NotCondition(SourceIsToken), CreateTokenCopyOfSourceEffect(false, "
         "1, Set.of(ARTIFACT)))`",
         "+ `Triggers.dies(Triggers.NONTOKEN, THIS) CreateTokenCopyOfSourceEffect(false, 1, Set.of(ARTIFACT))`"),
    ],
}


def main():
    failed = False
    for name, replacements in REPLACEMENTS.items():
        path = DOCS + name
        text = open(path).read()
        for old, new in replacements:
            if text.count(old) != 1:
                print("%s: %d matches for %r" % (path, text.count(old), old[:80]))
                failed = True
                continue
            if new is None:
                start = text.rindex("\n", 0, text.index(old)) + 1
                end = text.index("\n", start) + 1
                text = text[:start] + text[end:]
            else:
                text = text.replace(old, new)
        open(path, "w").write(text)
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
