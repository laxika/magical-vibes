#!/usr/bin/env python3
"""Step 15, run 17z12f: agent-docs follow the "you may pay" enter cards and Flayer of the Hatebound.

`MayPayManaEffect.sourceIsTriggeringPermanent` and `PermanentChoiceContext.EnteringPermanentAnyTargetTrigger` are gone;
the enter handler no longer binds "that creature" as a may-pay payload's `targetId`: the payloads say it
(`.ofTriggeringPermanent()`, `dealtByTriggeringPermanent`).
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("use `DealDamageToAnyTargetEffect.fromEnteringPermanent(amount)`: that creature deals the damage (Warstorm Surge, "
         "Terror of the Peaks, Dragon Tempest)",
         "use `DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(amount)`: that creature deals the damage (Warstorm "
         "Surge, Terror of the Peaks, Dragon Tempest; \"that creature deals damage equal to its power\": Flayer of the "
         "Hatebound, Electropotence, `dealtByTriggeringPermanent(new SourcePower())`)"),
        ("| `DealDamageToAnyTargetEffect(new SourcePower())` | `Triggers.entersFrom(Zone.GRAVEYARD, Triggers.CREATURE, "
         "ObjectRelation.ANY)` | Flayer of the Hatebound. The entering creature is the damage source (its power is read at "
         "resolution); any-target choice is handled by the `EnteringPermanentAnyTargetTrigger` pipeline. |",
         "| `DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new SourcePower())` | `Triggers.entersFrom(Zone.GRAVEYARD, "
         "Triggers.CREATURE, ObjectRelation.ANY)` | Flayer of the Hatebound. Flayer is the ability's source (its target is "
         "judged against Flayer, CR 113.7, chosen through `EntersTriggerTarget`); the entering creature deals the damage, "
         "its power read as the ability resolves, else as it last existed (CR 608.2h). |"),
        ("| `MayPayManaEffect(\"{2}\", new CreateTokenCopyOfTargetPermanentEffect(true, false, true), prompt)` | "
         "`Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` | Minion Reflector. The slot "
         "bakes the entering creature's id (skips tokens);",
         "| `MayPayManaEffect(\"{2}\", new CreateTokenCopyOfTargetPermanentEffect(true, false, true).ofTriggeringPermanent(), "
         "prompt)` | `Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` | Minion Reflector. "
         "\"That creature\" is the entry's triggering permanent, as it last existed if it left (tokens skipped by the trigger);"),
        ("| `new CreateTokenCopyOfTargetPermanentEffect()` | `Triggers.enters(PermanentAllOfPredicate(Triggers.NONTOKEN_CREATURE, "
         "PermanentHasSubtypePredicate(subtype)), ObjectRelation.ANOTHER_YOU_CONTROL)` | Necroduality (Zombie). Mandatory copy: "
         "enter collector bakes the entering permanent id as `targetId` (does not go through EntersTriggerTarget).",
         "| `new CreateTokenCopyOfTargetPermanentEffect().ofTriggeringPermanent()` | `Triggers.enters(PermanentAllOfPredicate("
         "Triggers.NONTOKEN_CREATURE, PermanentHasSubtypePredicate(subtype)), ObjectRelation.ANOTHER_YOU_CONTROL)` | "
         "Necroduality (Zombie). Mandatory copy of the entry's triggering permanent, not a target."),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("Used with MayPayManaEffect for Mirrorworks' copy trigger. Entering permanent ID is passed via "
         "PendingMayAbility.targetCardId |",
         "Used with MayPayManaEffect for Mirrorworks' copy trigger: \"a copy of that artifact\" is "
         "`CreateTokenCopyOfTargetPermanentEffect().ofTriggeringPermanent()`, the entering permanent the entry records as "
         "its triggering permanent (a may-pay payload that targets gets its target chosen as the ability goes on the stack) |"),
    ],
    "EFFECTS_INDEX.md": [
        ("so a targeted may-pay trigger has its target chosen when the ability goes on the stack (CR 603.3d) and the "
         "payment prompt at resolution (CR 603.5) ",
         "so a targeted may-pay trigger has its target chosen when the ability goes on the stack (CR 603.3d) and the "
         "payment prompt at resolution (CR 603.5); on an enter trigger the ability is the watcher's and \"that creature\" "
         "is the entry's triggering permanent, which the payload says (`CreateTokenCopyOfTargetPermanentEffect"
         ".ofTriggeringPermanent()`: Mirrorworks; `AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent()`: Brilliant "
         "Wings; `PutCounterOnTargetPermanentEffect.onTriggeringPermanent`: Emiel the Blessed; \"have that creature deal "
         "damage equal to its power to any target\": `DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new "
         "SourcePower())`, Electropotence) "),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("The three that enumerate an unfiltered\n     \"any target\" — `DiscardTriggerAnyTarget`, the filterless branch of "
         "`SpellTargetTriggerAnyTarget`,\n     and `EnteringPermanentAnyTargetTrigger` — share",
         "The two that enumerate an unfiltered\n     \"any target\" — `DiscardTriggerAnyTarget` and the filterless branch of "
         "`SpellTargetTriggerAnyTarget` — share"),
        ("| Entering-permanent any-target (`EnteringPermanentAnyTargetTrigger`) | — | ✅ all players | ✅ the evaluated "
         "`anyTarget()` | ❌ | ❌ | ❌ | ❌ |\n", ""),
    ],
    "TARGET_PREDICATE_PLAN.md": [
        ("| `TriggeredAbilityQueueService` (~:630) | `EnteringPermanentAnyTargetTrigger` target list |",
         "| `TriggeredAbilityQueueService` (~:630) | `EnteringPermanentAnyTargetTrigger` target list (gone: "
         "effect-slot-refactor.md step 15, run 17z12f; Flayer of the Hatebound chooses through `EntersTriggerTarget`) |"),
    ],
}

# EFFECTS_INDEX.md carries the MayPayManaEffect row twice; both get the note.
EXPECTED_COUNT = {("EFFECTS_INDEX.md", 0): 2}

for name, edits in EDITS.items():
    path = DOCS / name
    text = path.read_text()
    for i, (old, new) in enumerate(edits):
        assert text.count(old) == EXPECTED_COUNT.get((name, i), 1), (name, old[:80])
        text = text.replace(old, new)
    path.write_text(text)
    print("edited", name)
