#!/usr/bin/env python3
"""Step 14, run 4a of effect-slot-refactor.md: rewrites the agent-docs rows that still describe a "this [permanent]
or another [type] you control enters" card as two abilities (a "when this enters" one beside an "another" one), now
that scripts/step14-own-enters-merge.py made each one ability with both trigger conditions. Exact replacements;
a row that no longer matches is reported.

Usage: python3 scripts/step14-own-enters-docs.py [--dry-run]
"""
import sys

TWO = ("one `TriggeredAbility` with both trigger conditions, `new TriggerCondition(Triggers.enters(ObjectRelation.THIS))`"
       " and `new TriggerCondition(%s)` (`addTriggeredAbility`)")

EDITS = {
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("`Triggers.enters(ObjectRelation.THIS) ManifestDreadEffect.forController()` + "
         "`Triggers.enters(PermanentNamedPredicate(\"Verdant Dread\"), ObjectRelation.ANOTHER_YOU_CONTROL) "
         "ManifestDreadEffect.forController()`",
         "`ManifestDreadEffect.forController()` on " + TWO % "Triggers.enters(PermanentNamedPredicate(\"Verdant Dread\"), "
         "ObjectRelation.ANOTHER_YOU_CONTROL)"),
        ("`Triggers.enters(ObjectRelation.THIS)` (+ `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` "
         "for \"or another X you control enters\")",
         "`Triggers.enters(ObjectRelation.THIS)`; \"or another X you control enters\" is " + TWO
         % "Triggers.enters(…, ObjectRelation.ANOTHER_YOU_CONTROL)"),
        ("`Triggers.enters(ObjectRelation.THIS)` + `Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, "
         "PermanentHasSubtypePredicate(KAVU)), ObjectRelation.ANOTHER_YOU_CONTROL)`, both "
         "`ExileTopCardMayPlayWhileControllingSubtypeEffect(KAVU)`",
         "`ExileTopCardMayPlayWhileControllingSubtypeEffect(KAVU)` on " + TWO
         % "Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, PermanentHasSubtypePredicate(KAVU)), "
           "ObjectRelation.ANOTHER_YOU_CONTROL)"),
        ("`Triggers.enters(ObjectRelation.THIS)` + `Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, "
         "PermanentHasSubtypePredicate(ELEMENTAL)), ObjectRelation.ANOTHER_YOU_CONTROL)` | Risen Reef. The separate "
         "`THIS` trigger covers Risen Reef's own entry;",
         TWO % "Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, PermanentHasSubtypePredicate(ELEMENTAL)), "
               "ObjectRelation.ANOTHER_YOU_CONTROL)"
         + " | Risen Reef. The `THIS` condition covers Risen Reef's own entry whatever its types;"),
        ("`MayEffect(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1), prompt)` on both "
         "`Triggers.enters(ObjectRelation.THIS)` (behind `target(TargetFilters.creature())`) and "
         "`Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL)` | see left | Oath of the Ancient "
         "Wood. The self half is an ETB may-ability (target chosen when the prompt is accepted); the ally half is "
         "queued as an `EntersTriggerTarget` choice",
         "`MayEffect(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1), prompt)` on "
         + TWO % "Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL)"
         + ", behind `target(TargetFilters.creature())` | see left | Oath of the Ancient Wood. Its own entry is "
         "collected as an ETB may-ability (target chosen when the prompt is accepted); another enchantment's is "
         "queued as an `EntersTriggerTarget` choice"),
        ("`STATIC StoriedEffect()` + `Triggers.enters(ObjectRelation.THIS)` + "
         "`Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, PermanentHasSubtypePredicate(DWARF)), "
         "ObjectRelation.ANOTHER_YOU_CONTROL)`, both `MayEffect(DiscardOwnHandThenDrawThatManyAndDealDamageEffect("
         "new ControllerHasEnduringStory()))`",
         "`STATIC StoriedEffect()` + `MayEffect(DiscardOwnHandThenDrawThatManyAndDealDamageEffect("
         "new ControllerHasEnduringStory()))` on " + TWO
         % "Triggers.enters(PermanentAllOfPredicate(Triggers.CREATURE, PermanentHasSubtypePredicate(DWARF)), "
           "ObjectRelation.ANOTHER_YOU_CONTROL)"),
    ],
}


def main():
    dry = "--dry-run" in sys.argv
    for path, edits in EDITS.items():
        src = open(path).read()
        for old, new in edits:
            if old not in src:
                print("NOT FOUND in %s: %s" % (path, old[:100]))
                continue
            src = src.replace(old, new, 1)
            print("rewrote %s: %s" % (path, new[:100]))
        if not dry:
            open(path, "w").write(src)


if __name__ == "__main__":
    sys.exit(main())
