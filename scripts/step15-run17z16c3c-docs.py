#!/usr/bin/env python3
"""Step 15, run 17z16c3c: agent-docs follow the effects that no longer carry a chosen object.

Rewrites the lines of `agent-docs/` that describe the changed authoring API: `QueueReflexiveAbilityEffect` has no
permanent id (`aboutPickedPermanent`), a `LibrarySelectionFollowUp` is `effect()` reading the resolving entry's picks,
the `...Selected...` effects carry no ids, `FightTargetsEffect` and `SourceFightsTargetCreatureControlledByPlayerEffect`
read the entry, and the deleted effects and predicate are marked gone.

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z16c3c-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

GONE_ADVOKIST = ("**Gone** (effect-slot-refactor.md step 15, run 17z16c3c; no card wrote it): Orzhov Advokist and "
                 "Agitator Ant write `EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect`")

EDITS = {
    "EFFECTS_INDEX.md": [
        ("| `ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect` | `(UUID permanentId, Integer power, Integer toughness)` | conjures a non-token duplicate of the permanent selected by a preceding non-targeting choice",
         "| `ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect` | `(Integer power, Integer toughness)` | conjures a non-token duplicate of the permanent selected by a preceding non-targeting choice (the first of the resolving entry's picks, `StackEntry.recordPicks`)"),
        ("reusing `TransformChosenPermanentEffect` for the selected permanent |",
         "reusing `TransformChosenPermanentEffect.pickedPermanent()`, which chooses among the resolving entry's picks |"),
        ("then queues a reflexive target choice for `SourceFightsTargetCreatureControlledByPlayerEffect`; the target is another creature controlled by the selected player. Used by Strax, Sontaran Nurse |",
         "then queues a reflexive target choice for `SourceFightsTargetCreatureControlledByPlayerEffect()`; the selected player is the reflexive ability's triggering player, and the target is another creature that player controls. Used by Strax, Sontaran Nurse |"),
        ("| `SourceFightsTargetCreatureControlledByPlayerEffect` | `(UUID playerId)` / `(UUID playerId, UUID excludedPermanentId)` | targeted reflexive fight effect whose target spec is a creature controlled by the previously selected player; the optional excluded permanent enforces \"another\".",
         "| `SourceFightsTargetCreatureControlledByPlayerEffect` | `()` | targeted reflexive fight effect whose target spec is another creature controlled by the player the ability is about (the stack entry's triggering player: `PermanentControlledByTriggeringPlayerPredicate`, not the source: `PermanentIsSourcePermanentPredicate`)."),
        ("| `FightTargetsEffect` | `()` = groups (0, 1) / `(int firstTargetGroup, int secondTargetGroup)` / `(UUID firstTargetId, UUID secondTargetId)` |",
         "| `FightTargetsEffect` | `()` = groups (0, 1) / `(int firstTargetGroup, int secondTargetGroup)` / `ofReflexiveAbilityTargets()` |"),
        ("the UUID form is used for targets captured by a reflexive ability |",
         "`ofReflexiveAbilityTargets()` reads the targets of a reflexive ability an effect handler put on the stack with targets of its own (Earth Rumble: the last is the creature an opponent controls, the one before it, when chosen, the creature you control) |"),
        ("| `AttachSelectedEquipmentToTargetCreatureEffect` | `(UUID equipmentPermanentId)` | reflexive follow-up that attaches a selected Equipment permanent to up to one target creature controlled by the ability controller. Used by Masterpiece Vault (YDFT 29) |",
         "| `AttachSelectedEquipmentToTargetCreatureEffect` | — | **Gone** (effect-slot-refactor.md step 15, run 17z16c3c): Masterpiece Vault (YDFT 29) and Adaptive Armorer (YECL 1) write `QueueReflexiveAbilityEffect.aboutPickedPermanent(new AttachCreatedEquipmentToTargetCreatureEffect()[, true])` in their `LibrarySelectionFollowUp.effect()`; the Equipment put onto the battlefield is the reflexive ability's source permanent |"),
        ("| `AttachSelectedAurasToLegalTargetsEffect` | `(List<UUID> auraPermanentIds)` | mandatory library-selection follow-up: each selected Aura",
         "| `AttachSelectedAurasToLegalTargetsEffect` | `()` | mandatory library-selection follow-up: each selected Aura (the resolving entry's picks)"),
        ("| `AttachOneOfEquipmentToCreatureEffect` | `(List<UUID>)` | `LibrarySelectionFollowUp`: from the Equipment permanents selected by a top-library battlefield effect,",
         "| `AttachOneOfEquipmentToCreatureEffect` | `()` | `LibrarySelectionFollowUp`: from the Equipment permanents selected by a top-library battlefield effect (the entry's picks),"),
        ("| `MakeChosenPermanentAttackingEffect` | `(UUID permanentId)` / `attackingTargetOfTriggeringPlayer()` | asks for a player or planeswalker and makes the selected battlefield permanent attack that object;",
         "| `MakeChosenPermanentAttackingEffect` | `pickedPermanent()` / `attackingTargetOfTriggeringPlayer()` | asks for a player or planeswalker and makes a battlefield permanent attack that object; `pickedPermanent()` is the permanent an earlier step of the resolution picked (a library-to-battlefield follow-up's \"tapped and attacking\"; the `(UUID)` form is the engine's);"),
        ("| `ReturnSelectedPermanentToHandAtEndOfCombatEffect` | `(UUID permanentId)` | schedules a permanent selected by a library-to-battlefield effect",
         "| `ReturnSelectedPermanentToHandAtEndOfCombatEffect` | `()` | schedules the permanent selected by a library-to-battlefield effect (the first of the resolving entry's picks)"),
        ("| `EachPlayerMayPutCountersOnCreatureEffect` | `(CounterType counterType, int count[, CardEffect acceptedFollowUp])` | in APNAP order, queues an independent may-choice for each player with a creature they control; the accepting branch chooses that player's creature and applies the optional follow-up only after successful counter placement. Used by Orzhov Advokist (NCC 207) and Agitator Ant (NCC 263) |",
         "| `EachPlayerMayPutCountersOnCreatureEffect` | — | " + GONE_ADVOKIST + " |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("`QueueReflexiveAbilityEffect(effect, UUID sourcePermanentId)` names another permanent as its source, and",
         "`QueueReflexiveAbilityEffect.aboutPickedPermanent(effect[, optionalTarget])` makes the next permanent an earlier step of the resolution picked (the resolving entry's picks: the permanent a library-to-battlefield effect selected, a created token) its source permanent, never a permanent id, and"),
        ("resolution reuses `TransformChosenPermanentEffect` narrowed to that permanent (Nick Fury, Agent of S.H.I.E.L.D.)",
         "resolution reuses `TransformChosenPermanentEffect.pickedPermanent()`, the resolving entry's picks (Nick Fury, Agent of S.H.I.E.L.D.)"),
        ("- `RandomPlayerThenFightsTargetCreatureEffect()` + `SourceFightsTargetCreatureControlledByPlayerEffect(UUID playerId, UUID excludedPermanentId)` — choose a player uniformly at random,",
         "- `RandomPlayerThenFightsTargetCreatureEffect()` + `SourceFightsTargetCreatureControlledByPlayerEffect()` — choose a player uniformly at random (the reflexive ability's triggering player),"),
        ("- `MakeChosenPermanentAttackingEffect(UUID)` — choose a player or planeswalker for a selected permanent to attack; used by Winota's library-entered Human follow-up.",
         "- `MakeChosenPermanentAttackingEffect.pickedPermanent()` — choose a player or planeswalker for the permanent an earlier step of the resolution picked to attack; used by Winota's library-entered Human follow-up."),
        ("- `ReturnSelectedPermanentToHandAtEndOfCombatEffect(UUID)` — schedule a library-selected permanent",
         "- `ReturnSelectedPermanentToHandAtEndOfCombatEffect()` — schedule the library-selected permanent (the first of the resolving entry's picks)"),
        ("- `AttachSelectedEquipmentToCreatureEffect(List<UUID>)` — reflexive optional follow-up for a library-to-battlefield selection;",
         "- `AttachSelectedEquipmentToCreatureEffect()` — reflexive optional follow-up for a library-to-battlefield selection (the selected permanent is its entry's pick);"),
        ("- `AttachSelectedEquipmentToTargetCreatureEffect(UUID equipmentPermanentId)` — reflexive follow-up that attaches a selected Equipment permanent to up to one target creature controlled by the ability controller. Used by Masterpiece Vault (YDFT 29).",
         "- `AttachSelectedEquipmentToTargetCreatureEffect` — **gone** (effect-slot-refactor.md step 15, run 17z16c3c): write `QueueReflexiveAbilityEffect.aboutPickedPermanent(new AttachCreatedEquipmentToTargetCreatureEffect()[, true])` in the `LibrarySelectionFollowUp.effect()` (Masterpiece Vault, Adaptive Armorer)."),
        ("- `AttachOneOfEquipmentToCreatureEffect(List<UUID>)` — `LibrarySelectionFollowUp` for a top-library battlefield selection;",
         "- `AttachOneOfEquipmentToCreatureEffect()` — `LibrarySelectionFollowUp` for a top-library battlefield selection (the selected Equipment are the entry's picks);"),
        ("`QueueReflexiveAbilityEffect(effect, UUID sourcePermanentId)` makes a chosen permanent the source of the reflexive ability (Vivien's Invocation via a `LibrarySelectionFollowUp`),",
         "`QueueReflexiveAbilityEffect.aboutPickedPermanent(effect)` makes the picked permanent the source of the reflexive ability (Vivien's Invocation via a `LibrarySelectionFollowUp.effect()`),"),
        ("then queue the supplied reflexive follow-up when a card was selected;",
         "then queue the supplied follow-up when a card was selected (`LibrarySelectionFollowUp.effect()` is the card's own effect; the selected permanents are the picks of the entry it resolves in, and one is also its chosen permanent);"),
        ("- `AttachSelectedAurasToLegalTargetsEffect(List<UUID>)` — mandatory follow-up for selected library Auras;",
         "- `AttachSelectedAurasToLegalTargetsEffect()` — mandatory follow-up for selected library Auras (the entry's picks);"),
        ("- `EachPlayerMayPutCountersOnCreatureEffect(CounterType, int[, CardEffect])` — in APNAP order, each player with a creature they control may choose one and put the specified counters on it; the accepting branch is represented by the counter/reflexive effect pipeline. The optional follow-up receives the chosen creature as the reflexive triggering permanent (Orzhov Advokist, Agitator Ant)",
         "- `EachPlayerMayPutCountersOnCreatureEffect` — " + GONE_ADVOKIST),
        ("- `ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect(UUID permanentId, Integer power, Integer toughness)` — conjures a non-token duplicate of a permanent captured by an earlier non-targeting choice",
         "- `ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect(Integer power, Integer toughness)` — conjures a non-token duplicate of the permanent an earlier non-targeting choice picked (the first of the resolving entry's picks)"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect(selected, 1, 1)` follow-up)",
         "ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect(1, 1)` follow-up)"),
        ("LibrarySelectionFollowUp(QueueReflexiveAbilityEffect(AttachSelectedEquipmentToTargetCreatureEffect)))`",
         "LibrarySelectionFollowUp(QueueReflexiveAbilityEffect.aboutPickedPermanent(AttachCreatedEquipmentToTargetCreatureEffect(), true)))`"),
        ("with `SequenceEffect.of(MakeChosenPermanentAttackingEffect, GrantKeywordToChosenCreatureUntilEndOfTurnEffect(INDESTRUCTIBLE))`",
         "with `SequenceEffect.of(MakeChosenPermanentAttackingEffect.pickedPermanent(), GrantKeywordToChosenCreatureUntilEndOfTurnEffect(INDESTRUCTIBLE, null, true))`"),
        ("follow-up=SequenceEffect(MakeChosenPermanentAttackingEffect, ReturnSelectedPermanentToHandAtEndOfCombatEffect)))`",
         "follow-up=SequenceEffect(MakeChosenPermanentAttackingEffect.pickedPermanent(), ReturnSelectedPermanentToHandAtEndOfCombatEffect())))`"),
        ("`Triggers.upkeep(ActivePlayer.YOU) EachPlayerMayPutCountersOnCreatureEffect(PLUS_ONE_PLUS_ONE, 2)`; each accepted choice uses `PutCountersOnTargetPermanentThenReflexiveEffect(PLUS_ONE_PLUS_ONE, 2, CreaturesCantAttackControllerUntilNextTurnEffect(choicePlayerId, true), false, creatureControlledBy(choicePlayerId), false)`",
         "`Triggers.upkeep(ActivePlayer.YOU) EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect(PLUS_ONE_PLUS_ONE, 2)`; each player is asked in turn in the same resolution, and an accepted choice puts the counters and makes the restriction there"),
        ("`Triggers.endStep(ActivePlayer.YOU) EachPlayerMayPutCountersOnCreatureEffect(PLUS_ONE_PLUS_ONE, 2, GoadTriggeringCreatureUntilNextTurnEffect())`; the chosen creature is carried as the reflexive ability's triggering permanent and is goaded only after at least one counter is placed",
         "`Triggers.endStep(ActivePlayer.YOU) EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect(PLUS_ONE_PLUS_ONE, 2, GoadTriggeringCreatureUntilNextTurnEffect())`; the chosen creature is goaded in the same resolution, only after at least one counter is placed"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("`o/OrzhovAdvokist.java` + `EachPlayerMayPutCountersOnCreatureEffect` |",
         "`o/OrzhovAdvokist.java` + `EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect` |"),
    ],
    "CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md": [
        ("then queues `SourceFightsTargetCreatureControlledByPlayerEffect` so the target is chosen after that player is known and excludes the source permanent |",
         "then queues `SourceFightsTargetCreatureControlledByPlayerEffect()` with that player as the reflexive ability's triggering player, so the target is chosen after that player is known and excludes the source permanent |"),
    ],
    "PREDICATES_REFERENCE.md": [
        ("| `PermanentPowerAtMostTrackedPermanentPowerPredicate` | `(UUID trackedPermanentId)` | creatures with power <= the tracked permanent's effective power, re-read at every evaluation (last known power once it has left the battlefield)",
         "| `PermanentPowerAtMostTriggeringPermanentPowerPredicate` | `()` | creatures with power <= the effective power of the permanent the ability is about (the stack entry's triggering permanent, `FilterContext.triggeringPermanentId`), re-read at every evaluation (last known power once it has left the battlefield); matches nothing without one"),
    ],
}

ARCHITECTURE_MARK = "Nor does a handler put an effect with a chosen object bound in on the stack"
ARCHITECTURE_ANCHOR = "\nA spell-cast trigger's \"that spell\""
ARCHITECTURE = (
    "\n" + ARCHITECTURE_MARK + ", and no follow-up a card writes takes one (effect-slot-refactor.md step 15, run 17z16c3c): "
    "a reflexive ability's effect, a library follow-up and a question's payload have no permanent's, card's or "
    "player's id filled in for them. (A handler does rebuild its own effect with the progress of its resolution, "
    "and hands a step to another handler with what the resolution determined; those are steps of the resolving "
    "object, never another stack object.) A permanent an earlier step of "
    "the resolution picked (the permanent a library-to-battlefield effect put onto the battlefield, a created token, "
    "the targets a \"for each of them\" step still has to do) is on the resolving entry's picks "
    "(`StackEntry.recordPicks`, `ResolutionResults.pickedIds`), and the step that follows reads it there: a "
    "`LibrarySelectionFollowUp` is `effect()`, the card's own effect "
    "(`MakeChosenPermanentAttackingEffect.pickedPermanent()`, `TransformChosenPermanentEffect.pickedPermanent()`, "
    "the `...Selected...` effects), and `LibraryChoiceHandlerService` records the selected permanents on the entry "
    "the follow-up resolves in (one is also its chosen permanent). `QueueReflexiveAbilityEffect.aboutPickedPermanent"
    "(effect)` takes the next pick as the reflexive ability's source permanent, so one such step per created token "
    "puts one ability on the stack for each. What the preceding action determined about a reflexive ability (\"that "
    "player\" chosen at random, \"the tapped creature\") is set on its record, as its triggering player and "
    "permanent, through `QueueReflexiveAbilityEffectHandler.resolve(gameData, entry, queueEffect, recordSetup)`; the "
    "reflexive target choice's `FilterContext` has the record's triggering permanent and player, so the effect's "
    "target is written with `PermanentControlledByTriggeringPlayerPredicate` and "
    "`PermanentPowerAtMostTriggeringPermanentPowerPredicate` and judged again as the ability resolves. A question "
    "the resolving ability asks about a card names the card (`PendingMayAbility.targetCardId`; "
    "`BecomeCopyOfCardUntilEndOfTurnEffect.ofQuestionCard()`). `scripts/step15-run17z16c3c-scan.py` lists every "
    "effect a service constructs with runtime data and where it goes; a construction it cannot classify prints "
    "`UNCLASSIFIED`.\n")


def main():
    changed = 0
    for name, edits in sorted(EDITS.items()):
        path = DOCS / name
        text = path.read_text(encoding="utf-8", errors="surrogateescape")
        original = text
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                sys.exit(f"{name}: edit does not apply:\n{old}")
            text = text.replace(old, new)
        if text != original:
            path.write_text(text, encoding="utf-8", errors="surrogateescape")
            changed += 1
            print(f"updated agent-docs/{name}")
    path = DOCS / "ARCHITECTURE.md"
    text = path.read_text(encoding="utf-8")
    if ARCHITECTURE_MARK not in text:
        if ARCHITECTURE_ANCHOR not in text:
            sys.exit("ARCHITECTURE.md: anchor not found")
        text = text.replace(ARCHITECTURE_ANCHOR, ARCHITECTURE + ARCHITECTURE_ANCHOR, 1)
        path.write_text(text, encoding="utf-8")
        changed += 1
        print("updated agent-docs/ARCHITECTURE.md")
    print(f"{changed} files changed")


if __name__ == "__main__":
    main()
