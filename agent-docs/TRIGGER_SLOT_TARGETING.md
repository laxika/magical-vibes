# Trigger Slot Targeting Reference

Quick lookup for deciding whether a given `EffectSlot` can carry a targeted triggered ability, what kind of target (player / permanent) it supports, and which `TargetFilter` types the engine will honour on the card itself.

**Consult this file before deciding whether a new card needs an engine change.** If the slot you want is not wired into one of the targeting pipelines below, adding targeting to it is an engine change, not just a new card.

---

## How trigger targeting is wired

Targeting for triggered abilities happens in two layers:

1. **The collector** that notices the trigger fires and decides whether the resulting ability should:
   - Go straight onto the stack with no user choice (non-targeting), **or**
   - Be parked in a `pendingXxxTriggerTargets` deque in `GameData` and then processed by a target-choice step.

2. **The target-choice step** that turns a pending entry into a concrete target:
   - For the three central pipelines — death, attack, end-step — target collection runs through the shared
     `TriggerTargetCollector` (`service/trigger/TriggerTargetCollector.java`), configured by one of the
     `Options` constants (`DEATH`, `ATTACK`, `END_STEP`). These pipelines honour every filter the collector
     understands: `PlayerPredicateTargetFilter` (incl. `PlayerRelationPredicate.OPPONENT`),
     `PermanentPredicateTargetFilter`, and (for death/attack) `ControlledPermanentPredicateTargetFilter`.
   - Other pipelines (`DiscardSelf`, `SpellTarget`, `LifeGain`, `Explore`, `Emblem`, `SagaChapter*`) are
     bespoke — each supports a different subset of filters. See the table below before assuming any of them
     works the way the death/attack/end-step pipelines do.

If `effect.canTargetPermanent() || effect.canTargetPlayer()` returns `true` on any effect, the collectors
route the trigger into a pending queue. Otherwise the trigger goes directly onto the stack with no target
choice. **An effect must override `canTargetPlayer` / `canTargetPermanent`** or it will silently skip
target selection — this invariant is guarded by `CardEffectTargetingConsistencyTest`.

---

## Pipeline capability matrix

| Pipeline                   | `Options` constant | Player target | Permanent target | `PlayerRelationPredicate.OPPONENT` (via `PlayerPredicateTargetFilter`) | `PermanentPredicateTargetFilter` | `ControlledPermanentPredicateTargetFilter` | Effect-level `targetPredicate()` |
|----------------------------|--------------------|:-------------:|:----------------:|:------:|:---:|:---:|:---:|
| Death (`pendingDeathTriggerTargets`)        | `Options.DEATH`    | ✅ | ✅ creatures only | ✅ | ✅ | ✅ | ❌ (ignored) |
| Attack (`pendingAttackTriggerTargets`)      | `Options.ATTACK`   | ✅ | ✅ any permanent  | ✅ | ✅ | ✅ | ❌ (ignored) |
| End step (`pendingEndStepTriggerTargets`)   | `Options.END_STEP` | ✅ | ✅ any permanent  | ✅ | ✅ | ❌ | ✅ (unwraps `ConditionalEffect`) |
| Discard-self (`pendingDiscardSelfTriggers`) | —                  | ✅ all players | ✅ creatures + planeswalkers only | ❌ | ❌ | ❌ | ❌ |
| Spell-target (`pendingSpellTargetTriggers`) | —                  | ✅ unless filter present | ✅ via `TargetFilter` only | ❌ | ✅ (via generic `TargetFilter.matchesFilters`) | ❌ | ❌ |
| Life-gain (`pendingLifeGainTriggerTargets`) | —                  | ✅ all players | ✅ creatures only | ❌ | ❌ | ❌ | ❌ |
| Explore (`pendingExploreTriggerTargets`)    | —                  | ❌            | ✅ hard-coded to opponent creatures | n/a (hard-coded) | ❌ | ❌ | ❌ |
| Emblem (`pendingEmblemTriggerTargets`)      | —                  | ❌            | ✅ any permanent  | via bespoke `opponentControlledOnly` boolean | ❌ | ❌ | ❌ |
| Saga chapter (`pendingSagaChapterTargets`)  | —                  | "up to one" skip via self-target | ✅ creatures only | ❌ | ✅ (via chapter-level `Set<TargetFilter>`) | ❌ | ✅ first effect's `targetPredicate()` |

Legend: ✅ = supported, ❌ = not supported, — = no shared Options entry.

---

## Trigger slot → pipeline mapping

Only slots that are actually routed through a targeting pipeline are listed. Every other slot in
`EffectSlot.java` pushes its stack entry directly via
`gameData.stack.add(new StackEntry(...))` and **does not support a user target choice today**. Wiring a
new slot into a pipeline is an engine change.

| Slot | Collector service | Pipeline |
|---|---|---|
| `ON_DEATH` | `DeathTriggerCollectorService.handleDeathDefault` + `handleDeathMayEffect` + `handleLosesLifeEqualToPower` | Death |
| `ON_EQUIPPED_CREATURE_DIES` | `DeathTriggerCollectorService.handleEquippedCreatureDeathDefault` | Death |
| `ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD` (targeting branches) | `DeathTriggerCollectorService.addEnchantedPermanentDeathEntry` | Death |
| `ON_ATTACK` (attached-permanent flavour) | `CombatTriggerService` aura/equipment flow | Attack |
| `ON_ATTACK` / `ON_ALLY_CREATURE_ATTACKS` | `CombatAttackService.declareAttackers` | Attack |
| `END_STEP_TRIGGERED` | `StepTriggerService.handleEndOfTurnTriggers` (non-kicked / morbid / default) | End step |
| `CONTROLLER_END_STEP_TRIGGERED` | `StepTriggerService.handleEndOfTurnTriggers` (raid / default) | End step |
| `ON_SELF_DISCARDED_BY_OPPONENT` | `TriggerCollectionService.checkDiscardSelfTriggers` | Discard-self |
| `ON_BECOMES_TARGET_OF_SPELL` / `…_OR_ABILITY` / `…_OF_OPPONENT_SPELL` | `TriggerCollectionService.checkBecomesTargetOfSpell*` | Spell-target |
| `ON_CONTROLLER_CASTS_SPELL` / `ON_ANY_PLAYER_CASTS_SPELL` (targeting variants) | `SpellCastTriggerCollectorService` | Spell-target |
| `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` (targeting branch) | `DamageTriggerCollectorService` | Spell-target |
| `ON_CONTROLLER_GAINS_LIFE` | `MiscTriggerCollectorService` | Life-gain |
| `ON_ALLY_CREATURE_EXPLORES` | `TriggerCollectionService.checkExploreTriggers` | Explore |
| Planeswalker ultimate emblems | `DrawService` / `TriggerCollectionService` | Emblem |
| `SAGA_CHAPTER_I` / `SAGA_CHAPTER_II` / `SAGA_CHAPTER_III` | `StepTriggerService.processSagaChapters` / `StackResolutionService` | Saga chapter |

Slots that currently **only ever push non-targeting entries** (no pending queue):
`ON_TAP`, `ON_ENTER_BATTLEFIELD`, `STATIC`, `ON_SACRIFICE`, `ON_BLOCK`, `UPKEEP_TRIGGERED`,
`GRAVEYARD_UPKEEP_TRIGGERED`, `EACH_UPKEEP_TRIGGERED`, `OPPONENT_UPKEEP_TRIGGERED`,
`ON_ALLY_CREATURE_DIES`, `ON_DAMAGED_CREATURE_DIES`, `ON_ANY_CREATURE_DIES`,
`ON_ALLY_NONTOKEN_CREATURE_DIES`, `ON_ANY_NONTOKEN_CREATURE_DIES`, `ON_OPPONENT_CREATURE_DIES`,
`ON_COMBAT_DAMAGE_TO_PLAYER`, `ON_COMBAT_DAMAGE_TO_CREATURE`, `ON_DAMAGE_TO_PLAYER`,
`ON_DEALT_DAMAGE`, `ON_BECOMES_BLOCKED`, `DRAW_TRIGGERED`, `EACH_DRAW_TRIGGERED`,
`ON_CONTROLLER_DRAWS`, `ON_OPPONENT_DRAWS`, `ON_OPPONENT_DISCARDS`,
`ON_ANY_PLAYER_TAPS_LAND`, `ON_ALLY_PERMANENT_SACRIFICED`, `ON_ALLY_CREATURES_ATTACK`,
`ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`,
`ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD`, `ON_ENCHANTED_PERMANENT_TAPPED`,
`ON_OPPONENT_LAND_ENTERS_BATTLEFIELD`, `ON_ALLY_LAND_ENTERS_BATTLEFIELD`,
`ON_OPENING_HAND_REVEAL`, `ON_OPPONENT_LOSES_LIFE`, `ON_OPPONENT_SHUFFLES_LIBRARY`,
`ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED`, `ENCHANTED_PLAYER_UPKEEP_TRIGGERED`,
`ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD`, `ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD`,
`ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE`, `ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER`,
`ON_OPPONENT_CREATURE_CARD_MILLED`, `ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD`,
`ON_SELF_MILLED`, `STATE_TRIGGERED`, `BEGINNING_OF_COMBAT_TRIGGERED`,
`ON_OPPONENT_CREATURE_DEALT_DAMAGE`, `GRAVEYARD_ON_CONTROLLER_CASTS_SPELL`,
`ON_CONTROLLER_LOSES_LIFE`, `ON_SELF_LEAVES_BATTLEFIELD`,
`ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`,
`GRAVEYARD_ON_ALLY_CREATURES_ATTACK`,
`ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY`,
`ON_TRANSFORM_TO_BACK_FACE`.

If the card you are implementing needs one of these slots **and** a user target choice (either player
or permanent), **that is an engine change**. The work required is:

1. Add a new `PermanentChoiceContext.XxxTriggerTarget` record (or reuse an existing one).
2. Add a `pendingXxxTriggerTargets` deque on `GameData` (plus copy-on-fork in `GameData.deepCopy`).
3. In the collector that notices the trigger, route targeting effects into the new deque.
4. In the step that drains the queue, call
   `TriggerTargetCollector.collect(...)` with an appropriate `Options` — or extend `Options` if none of
   `DEATH` / `ATTACK` / `END_STEP` match the semantics you need.
5. Handle the empty-target case (log + skip) and the prompt wording.
6. Wire the queue into `AutoPassService` so the turn doesn't advance while it is non-empty.
7. Cover it in `TriggerTargetCollectorTest` and an end-to-end card test.

---

## Filter-by-filter reference

### `PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT))`

Card says "target opponent". Honoured only in **Death / Attack / End-step** pipelines (`Options.DEATH`,
`ATTACK`, `END_STEP`). Any other pipeline will offer the controller as a valid target too — **that's a
bug** and must be fixed in the pipeline, not papered over at the card level.

When used with an effect that also sets `canTargetPermanent()` to `true`, the opponent-only restriction
applies **only** to the player branch of the target list; permanents are still filtered via
`PermanentPredicateTargetFilter` (see below). If you need "target opponent OR a permanent an opponent
controls", combine the two: `PlayerPredicateTargetFilter` for the player side and
`PermanentPredicateTargetFilter(opponentControlled(...))` for the permanent side.

### `PermanentPredicateTargetFilter(PermanentPredicate)`

Honoured in **Death / Attack / End-step**. See `PREDICATES_REFERENCE.md` for the full list of
`PermanentPredicate` compositions (e.g. `opponentControlled(creature())`, `nonToken(creature())`, etc.).

Note that the death pipeline additionally forces `creaturesOnly = true` — a permanent predicate that
allows non-creatures is silently intersected with "is a creature". End-step has no such restriction.

### `ControlledPermanentPredicateTargetFilter(PermanentPredicate)`

Honoured **only** by Death and Attack pipelines. End-step does not read this filter; use
`PermanentPredicateTargetFilter` with `opponentControlled(...)` / `allied(...)` instead.

### Effect-level `targetPredicate()`

Honoured **only** by End-step (and Saga chapter) pipelines. Death and Attack ignore `targetPredicate()`
entirely — put the predicate on the card's `TargetFilter` instead. The end-step pipeline will also
unwrap `ConditionalEffect` (morbid / metalcraft / raid / …) wrappers before inspecting
`canTargetPlayer`, `canTargetPermanent`, and `targetPredicate()`.

---

## Common pitfalls

- **"My ON_DEATH trigger targets non-creatures."** It can't. Death forces `creaturesOnly = true`. Use a
  different slot (or add a new `Options` variant).
- **"My ON_DEATH trigger lets the controller pick themselves as the target."** You forgot the
  `PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT))` on the card — or
  you wired it on a pipeline that doesn't honour it (everything outside death / attack / end-step).
- **"My end-step trigger ignores `targetPredicate()`."** It shouldn't, unless the effect is wrapped in a
  `ConditionalEffect` subclass that `Options.END_STEP.unwrapConditional()` doesn't know about. Check
  `TriggerTargetCollector.collect` — it unwraps any `ConditionalEffect` generically.
- **"My attack trigger uses `targetPredicate()` and it's ignored."** Move the predicate onto the card's
  `TargetFilter` — attack (and death) read only the card-level filter.
- **"My effect gets no valid targets offered even though the filter matches."** The effect probably
  doesn't override `canTargetPlayer` / `canTargetPermanent`. The `CardEffectTargetingConsistencyTest`
  catches this for effects named `Target*Effect`, but not for other naming conventions.
