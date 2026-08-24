# Trigger Slot Targeting Reference

`ON_CONTROLLER_CASTS_SPELL` same-name graveyard casts use the dedicated `CastSameNameCardFromGraveyardOnSpellCastEffect` collector path: the cast spell's name is snapshotted, the graveyard card is chosen through `SpellGraveyardTargetTrigger`, and the existing graveyard-cast effect offers the normal-cost cast after targeting.

Quick lookup for deciding whether a given `EffectSlot` can carry a targeted triggered ability, what kind of target (player / permanent) it supports, and which `TargetFilter` types the engine will honour on the card itself.

**Consult this file before deciding whether a new card needs an engine change.** If the slot you want is not wired into one of the targeting pipelines below, adding targeting to it is an engine change, not just a new card.

---

## How trigger targeting is wired

Targeting for triggered abilities happens in two layers:

1. **The collector** that notices the trigger fires and decides whether the resulting ability should:
   - Go straight onto the stack with no user choice (non-targeting), **or**
   - Be parked in the unified `GameData.pendingInteractions` queue as a `PermanentChoiceContext.XxxTriggerTarget`
     record (queued via `gameData.queueInteraction(...)`) and then processed by a target-choice step. Each
     pipeline services only its own record type via the type-filtered helpers
     (`hasPendingInteraction` / `peekPendingInteraction` / `pollPendingInteraction`), so per-kind FIFO order is preserved.

2. **The target-choice step** that turns a pending entry into a concrete target:
   - For the three central pipelines — death, attack, end-step — target collection runs through the shared
     `TriggerTargetCollector` (`service/trigger/TriggerTargetCollector.java`), configured by one of the
     `Options` constants (`DEATH`, `ATTACK`, `END_STEP`). These pipelines honour every filter the collector
     understands: `PlayerPredicateTargetFilter` (incl. `PlayerRelationPredicate.OPPONENT`),
     `PermanentPredicateTargetFilter`, `AnyTargetPredicateTargetFilter`, and (for death/attack/end-step)
     `ControlledPermanentPredicateTargetFilter`. **True "any target" effects** (those whose spec
     `declares(TargetPredicates.anyTarget())` — Flameblast Dragon, Form of the Dragon, etc.) further
     restrict permanent candidates by *evaluating* that declared target's permanent restriction
     through `PredicateEvaluationService` — never by re-implementing the type check. So the collector
     admits exactly what CR 115.4 admits, is layer-aware (CR 613.1d: a planeswalker turned into a
     land by Imprisoned in the Moon is no longer offered), and picks up any future widening of the
     `anyTarget()` factory for free. That matches spell-path `ValidTargetService` /
     `TargetValidationService`, which read the same declared target. An explicit
     `PermanentPredicateTargetFilter` overrides that narrowing (e.g. Fire Snake's destroy-land).
   - Other pipelines (`DiscardSelf`, `SpellTarget`, `LifeGain`, `Explore`, `Emblem`, `SagaChapter*`) are
     bespoke — each supports a different subset of filters. See the table below before assuming any of them
     works the way the death/attack/end-step pipelines do. The three that enumerate an unfiltered
     "any target" — `DiscardTriggerAnyTarget`, the filterless branch of `SpellTargetTriggerAnyTarget`,
     and `EnteringPermanentAnyTargetTrigger` — share
     `TriggeredAbilityQueueService.anyTargetPermanents`, which evaluates the declared
     `TargetPredicates.anyTarget()` rather than re-implementing it, exactly as the collector does.
     `LifeGainTriggerAnyTarget` and `DrawTriggerAnyTarget` are creature-only by their own oracle text
     ("target creature or player") and deliberately do not use it.

If an effect's targeting (read from its `targetSpec()`: `admits(TargetPredicate.Kind.PERMANENT)` or
`admits(Kind.PLAYER)`) is true, the collectors route the trigger into a pending queue. Otherwise the
trigger goes directly onto the stack with no target choice. **An effect must declare a non-NONE
`targetSpec()`** or it will silently skip target selection — this invariant is guarded by
`CardEffectTargetingConsistencyTest`. (The eleven legacy `canTarget*` booleans were deleted in the
TargetSpec migration; the collectors ask the declared `TargetPredicate` directly through
`TargetSpec.admits(Kind)` — the five kinds are `PERMANENT`, `PLAYER`, `GRAVEYARD_CARD`,
`EXILED_CARD`, `SPELL`. `admits` reads `declaredTarget()` and never allocates, so it is safe in the
per-effect loops these collectors run.)

---

## Spell / activated-ability target validation (a DIFFERENT mechanism)

The pipelines above are for TRIGGERED abilities. A **spell** (SPELL slot) or **activated ability**
that carries a single `targetId` is validated on a separate path, and it narrows the legal target type
from the effect's **`targetSpec()`** — the category (`CREATURE` / `LAND` / `PERMANENT` / …) plus its
optional `PermanentPredicate` — interpreted by `TargetValidationService`. Declare the spec and the
cast path type-checks the target automatically; an effect that targets a permanent but leaves
`targetSpec()` at `NONE` gets NO type checking — the Fireball-burns-a-Plains class of bug.

- The declarative interpreter needs no per-effect wiring. Add a `@ValidatesTarget` validator (a
  `@Service` under `service/validate/`, auto-discovered) ONLY as an escape hatch for a non-structural
  rule the category/predicate cannot express (opponent-relation, controller/owner compare,
  chosen-source, null-target tolerance) — and still declare the structural `targetSpec()`.
- All **three** spell-target validation paths — UI/AI enumeration (`ValidTargetService`), multi-target
  cast, and single-`targetId` cast (`TargetLegalityService.checkSpellTargeting`) — share ONE structural
  core, `TargetLegalityService.checkSpellPermanentTargetableReason` (protection / shroud / hexproof /
  cant-be-targeted); the single-target paths additionally run the spec interpreter (and any escape-hatch
  validator) via `TargetValidationService.checkEffectTargets`. So the spec you declare is honoured by
  cast-time AND by what the UI/AI offers — you cannot narrow one without the other.
- Categories with no permanent-type gate are validated elsewhere: spell-on-stack (`SPELL_ON_STACK`,
  validated on the stack path), player-only (`PLAYER`, the structural player/permanent pre-split), and
  multi-target effects (card / position `TargetFilter`); trigger/ETB-slot targets are chosen by the
  pipelines below.

---

## Pipeline capability matrix

| Pipeline                   | `Options` constant | Player target | Permanent target | `PlayerRelationPredicate.OPPONENT` (via `PlayerPredicateTargetFilter`) | `PermanentPredicateTargetFilter` | `ControlledPermanentPredicateTargetFilter` | Effect-level target predicate (`targetSpec().predicate()`, read via `EffectResolution.targetPredicateOf`) |
|----------------------------|--------------------|:-------------:|:----------------:|:------:|:---:|:---:|:---:|
| Death (`DeathTriggerTarget`)        | `Options.DEATH`    | ✅ | ✅ creatures only | ✅ | ✅ | ✅ | ❌ (ignored) |
| Attack (`AttackTriggerTarget`)      | `Options.ATTACK`   | ✅ | ✅ any permanent  | ✅ | ✅ | ✅ | ❌ (ignored) |
| End step (`EndStepTriggerTarget`)   | `Options.END_STEP` | ✅ | ✅ any permanent  | ✅ | ✅ | ✅ | ✅ (unwraps `ConditionalEffect`) |
| Discard-self (`DiscardTriggerAnyTarget`) | —                  | ✅ all players | ✅ the evaluated `anyTarget()` | ❌ | ❌ | ❌ | ❌ |
| Controller-discard (`DiscardControllerTriggerTarget`) | `Options.ATTACK` | ✅ | ✅ any permanent | ✅ | ✅ | ✅ | ✅ (Zenith Seeker's creature-only grant) |
| Spell-target (`SpellTargetTriggerAnyTarget`) | —                  | ✅ (honours `PlayerPredicateTargetFilter` / OPPONENT when `playerTargetOnly`) | ✅ via `TargetFilter`, else the evaluated `anyTarget()` | ✅ when `playerTargetOnly` | ✅ (via `PredicateEvaluationService.matchesFilters`) | ❌ | ❌ |
| Life-gain (`LifeGainTriggerAnyTarget`) | —                  | ✅ all players | ✅ creatures only | ❌ | ❌ | ❌ | ❌ |
| Entering-permanent any-target (`EnteringPermanentAnyTargetTrigger`) | — | ✅ all players | ✅ the evaluated `anyTarget()` | ❌ | ❌ | ❌ | ❌ |
| Enters (`EntersTriggerTarget`)      | `Options.ATTACK`   | ✅ | ✅ any permanent  | ✅ | ✅ | ✅ | ✅ |
| Explore (`ExploreTriggerTarget`)    | —                  | ❌            | ✅ hard-coded to opponent creatures | n/a (hard-coded) | ❌ | ❌ | ❌ |
| Emblem (`EmblemTriggerTarget`)      | —                  | ❌            | ✅ any permanent  | via bespoke `opponentControlledOnly` boolean | ❌ | ❌ | ❌ |
| Saga chapter (`SagaChapterTarget`)  | —                  | "up to one" skip via self-target | ✅ creatures only | ❌ | ✅ (via chapter-level `Set<TargetFilter>`) | ❌ | ✅ first effect's spec predicate (`EffectResolution.targetPredicateOf`) |

Legend: ✅ = supported, ❌ = not supported, — = no shared Options entry.

`ON_ANY_PERMANENT_ENTERS_BATTLEFIELD` effects with a permanent target use `EntersTriggerTarget` and choose the target as the trigger is put on the stack. The entering permanent is preserved as the first exchange participant and as `FilterContext.sourcePermanentSnapshot`; its controller is the player who chooses the target, even when that player differs from the source permanent's controller.

---

## Trigger slot → pipeline mapping

Only slots that are actually routed through a targeting pipeline are listed. Every other slot in
`EffectSlot.java` pushes its stack entry directly via
`gameData.stack.add(new StackEntry(...))` and **does not support a user target choice today**. Wiring a
new slot into a pipeline is an engine change.

Combat-damage slots are checked separately for each combat damage step. If first strike or double strike
creates a first-strike combat damage step, those triggers go on the stack and resolve before the regular
combat damage step is processed.

| Slot | Collector service | Pipeline |
|---|---|---|
| `ON_DEATH` | `DeathTriggerCollectorService.handleDeathDefault` + `handleDeathMayEffect` + `handleDeathMayPayMana` + `handleLosesLifeEqualToPower` | Death |
| `ON_EQUIPPED_CREATURE_DIES` | `DeathTriggerCollectorService.handleEquippedCreatureDeathDefault` | Death |
| `ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE` | `DamageTriggerCollectorService.handleEquippedCreatureDealsCombatDamage` (scans attached Equipment and preserves last-known attachment) | Combat damage |
| `ON_EQUIPPED_CREATURE_TRANSFORMS` | `AnimationSupport.fireEquipmentTransformTriggers` (non-targeting; pushed with the Equipment as `sourcePermanentId`) | Transform |
| `ON_EQUIPMENT_ATTACHED_TO_CREATURE` | `TriggerCollectionService.checkEquipmentAttachedTriggers` (non-targeting; called by `EquipSupport.attachEquipment` and for permanents entering already attached) | Attachment |
| `ON_ALLY_PERMANENT_TRANSFORMS` | `AnimationSupport.fireAllyPermanentTransformTriggers` → `TriggerCollectionService.checkAllyPermanentTransformsTriggers` (non-targeting; supports `TriggeringCardConditionalEffect` against the transformed face) | Transform |
| `ON_ALLY_CREATURE_DIES` (targeting variants) | `TriggerCollectionService.checkAllyCreatureDeathTriggers` | Death |
| `ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` (targeting variants) | `DeathTriggerCollectorService.handleArtifactGraveyardControllerConditional` / `handleArtifactGraveyardReturnUnlessDamage` | Spell target; the latter carries the exact graveyard card id and uses the source permanent snapshot |
| `ON_ALLY_CREATURE_DIES` (targeting variants) | `TriggerCollectionService.checkAllyCreatureDeathTriggers` (non-may effects are batched; `DyingCreatureCountersAwareEffect` implementations receive the dying permanent's concrete counter snapshot before stacking) | Death |
| `ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` (targeting wrapper) | `DeathTriggerCollectorService.handleArtifactGraveyardControllerConditional` | Spell target |
| `ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD` (targeting branches) | `DeathTriggerCollectorService.addEnchantedPermanentDeathEntry` | Death |
| `ON_ALLY_LAND_ENTERS_BATTLEFIELD` | `TriggerCollectionService.checkAllyLandEntersTriggers` (targeted effects use `SpellTargetTriggerAnyTarget`; other effects go directly to the stack) | Spell target |
| `ON_ATTACK` (attached-permanent flavour) | `CombatTriggerService` aura/equipment flow | Attack |
| `ON_ALLY_CREATURES_ATTACK` (targeting variants) | `CombatAttackService.declareAttackers` queues `AttackTriggerTarget`; non-targeting variants keep the direct stack-entry path. The target collector sees the attacking battlefield state, so restrictions such as "target attacking Goblin you control" are enforced | Attack |
| `ON_ATTACK` / `ON_ALLY_CREATURE_ATTACKS` | `CombatAttackService.declareAttackers` (per-attacker mandatory triggers store the triggering attacker as a non-targeting `targetId`, and the attacked player/planeswalker as `attackedTargetId` — so effects can act on "that creature", e.g. Shared Animosity's boost). Single permanent/player targets use `AttackTriggerTarget`; multi-target / "up to N" (`needsSlotBySlotTargetSelection`) reuses `ETBTokenMultiTargetTrigger` (Archon of the Triumvirate) | Attack |
| `ON_ATTACKS_UNBLOCKED` (graveyard-targeting) | `CombatBlockService.collectUnblockedAttackTriggers` routes effects implementing `GraveyardCardChoosingEffect` (Rysorian Badger's `ExileCardsFromGraveyardEffect`) to `GraveyardTargetingService.handleUnblockedAttackGraveyardChoiceTargeting` — an up-to-N multi-select over the defending player's graveyard, filtered by `graveyardChoiceFilter()`, as the trigger goes on the stack. The attacker rides along as `sourcePermanentId` (for "if you do, it assigns no combat damage"); no matching cards ⇒ the trigger is still pushed with 0 targets | Declare blockers |
| `ON_COMBAT_DAMAGE_TO_PLAYER` (targeting variants) | `CombatDamageService` routes effects whose `targetSpec()` includes permanents or players to the shared `AttackTriggerTarget` pipeline; effects carrying baked combat context remain non-targeting, and graveyard-targeting effects still use `GraveyardTargetingService.handleCombatDamageGraveyardChoiceTargeting` (Skullsnatcher). The target-group filter is taken from the effect's `target(...)` declaration | Combat damage |
| `ON_ATTACKS_UNBLOCKED` (permanent-targeting may) | Same collector routes `MayEffect`s whose `targetSpec()` includes permanents (Dwarven Vigilantes) via `queueMayAbility(..., null, attackerId)` — CR 603.5 may at resolution, then creature target; non-targeting mays / mandatory effects still bake the defending player as `targetId` | Declare blockers |
| `ON_ATTACKS_UNBLOCKED` (multi-target) | A card with more than one `target(...)` group (Goblin Grenadiers' "destroy target creature and target land") is routed by `collectUnblockedAttackTriggers` to `PermanentChoiceContext.ETBTokenMultiTargetTrigger` before the may/plain branches — the single-target pipelines can't collect two targets. Targets are picked slot-by-slot as the trigger goes on the stack; a wrapping `MayEffect` still prompts at resolution and `MayAbilityHandlerService` leaves the entry's `targetIds` alone (`targetAlreadySet`) | Declare blockers |
| `ON_BECOMES_BLOCKED` (granted combat-opponent effects) | Temporary or persistent granted effects implementing `CombatOpponentReferencingEffect` are collected once for each declared blocker and auto-reference that blocker. They are omitted when an effect makes a creature blocked without a blocker. | Declare blockers |
| `ON_ATTACK` (graveyard-targeting) | `CombatAttackService.declareAttackers` routes effects whose `targetSpec().admits(Kind.GRAVEYARD_CARD)` (e.g. Graven Abomination's `ExileGraveyardCardsEffect(TARGET_CARDS_OPPONENT_GRAVEYARD)`) to `GraveyardTargetingService.handleAttackGraveyardTargeting` — chooses from the defending player's graveyard (from the attacker's `attackTarget`) as the trigger goes on the stack. No legal target ⇒ trigger skipped (CR 603.3c). Exception: a `CastTargetInstantOrSorceryFromGraveyardEffect` (The Dawning Archaic) routes to `handleAttackGraveyardCastTargeting` instead, which picks the graveyards from the effect's own `GraveyardSearchScope` | Attack |
| `ON_ATTACK` (two-target counter move) | `CombatAttackService.declareAttackers` routes any trigger whose effect implements the marker `AttackCounterMoveEffect` (Decimator Beetle's `RemoveAndPutCounterOnAttackEffect`) to the bespoke two-step `AttackCounterMoveFirstTarget` → `AttackCounterMoveSecondTarget` flow, because the normal Attack pipeline collects only ONE target. Stage 1 = a creature you control; stage 2 = up to one creature the defending player controls (choose yourself to decline). Drained in `AutoPassService`; both stages filter targetability via `TargetLegalityService.checkSpellPermanentTargetableReason`; the two chosen ids land on the entry's flat `targetIds` (0, 1) | bespoke |
| `ON_DEALT_DAMAGE` (targeting effects) | `DamageTriggerCollectorService` routes targeted effects through `SpellTargetTriggerAnyTarget` with the card's own `TargetFilter`; mandatory targets are chosen as the trigger goes on the stack, while targeted `MayEffect`s preserve the separate resolution-time may choice. The damage amount is carried as the triggered entry's X payload. Non-targeting effects keep the plain entry from `handleDealtDamageDefault` (damaged permanent as `targetId`, damage amount on `eventValue`) | Spell target |
| `ON_BLOCK` (targeting variant only) | `CombatBlockService.declareBlockers` queues an `AttackTriggerTarget` when the blocker's **card carries a target filter** and a block effect's `targetSpec()` includes permanents (e.g. Elite Javelineer's "deals 1 damage to target attacking creature"); honours the card's `PermanentPredicateTargetFilter`. `TriggeringPermanentConditionalEffect` wrappers are checked against the blocked attacker at trigger creation and unwrapped when they match (Arrogant Bloodlord). Block triggers with **no** card-level target filter (Ashmouth Hound, Inferno Elemental - "that creature") still push a non-targeting stack entry referencing the blocked attacker. | Attack |
| `ON_BLOCK` (targeting variant only) | `CombatBlockService.declareBlockers` queues an `AttackTriggerTarget` when the blocker's **card carries a target filter** and a block effect's `targetSpec()` includes permanents (e.g. Elite Javelineer's "deals 1 damage to target attacking creature"); honours the card's `PermanentPredicateTargetFilter`. Block triggers with **no** card-level target filter (Ashmouth Hound, Inferno Elemental — "that creature") still push a non-targeting stack entry referencing the blocked attacker. | Attack |
| `ON_ALLY_CREATURE_BECOMES_BLOCKED` (targeting variants) | `CombatBlockService.checkAllyBecomesBlockedTriggers` queues effects whose `targetSpec()` includes permanents or players through the shared `AttackTriggerTarget` pipeline; the watching permanent is the source for damage and other source-dependent effects. Non-targeting effects retain the blocked creature as `sourcePermanentId`. | Declare blockers |
| `ON_ALLY_CREATURE_ATTACKS_UNBLOCKED` | `CombatBlockService` (declare-blockers step; unblocked creature stored as non-targeting `sourcePermanentId`) | Non-targeting |
| `ON_CREATURE_ATTACKS_YOU` | `CombatAttackService.declareAttackers` (defender's permanents; attacking creature stored as non-targeting `targetId`) | Attack |
| `ON_ANY_PLAYER_ATTACKS` | `CombatAttackService.declareAttackers` (all battlefields, any attacking player; attacking player stored as non-targeting `targetId`) | Non-targeting (Total War) |
| `ON_ANY_CREATURE_ATTACKS` | `CombatAttackService.declareAttackers` (all battlefields, any controller; attacking creature stored as non-targeting `targetId`; `TriggeringPermanentConditionalEffect` filters which attackers trigger) | Non-targeting (Caltrops, Windreader Sphinx) |
| `ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers` (spell controller's battlefield; targeted creature stored as non-targeting `targetId`, listener as `sourcePermanentId`) | Becomes-target |
| `ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers` (controller's battlefield; handles both creature permanents and creature spells targeted by an opponent) | Becomes-target |
| `ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers` (targeted creature's controller's battlefield; spell path only; the triggered ability uses the shared ETB multi-target picker so optional targets can be declined) | Becomes-target |
| `ON_ANOTHER_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers` (one trigger per other permanent, opponent-only; targeted permanent stored as non-targeting `triggeringPermanentId`) | Becomes-target |
| `ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers` (all battlefields; targeted creature stored as non-targeting `targetId`) | Becomes-target |
| `ON_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers` (targeted permanent's controller's battlefield; only permanent targets qualify, and the opposing controller is checked before queuing) | Becomes-target |
| `UPKEEP_TRIGGERED` (any-target effects) | `StepTriggerService.handleUpkeepTriggers` → `UpkeepAnyTargetTrigger` (queued when an effect targets both — `targetSpec()` admits both `Kind.PLAYER` and `Kind.PERMANENT`, e.g. Form of the Dragon's 5-damage) | End step (reuses `TriggerTargetCollector.Options.END_STEP` for the target list) |
| `UPKEEP_TRIGGERED` (permanent-target effects) | `StepTriggerService.handleUpkeepTriggers` → `UpkeepPermanentTargetTrigger` (queued when a non–any-target, non–player-target effect's `targetSpec()` includes permanents, e.g. Weed-Pruner Poplar's "target creature other than this creature gets -1/-1"). Honours the card's `PermanentPredicateTargetFilter`; use `PermanentNotPredicate(PermanentIsSourceCardPredicate)` for "other than this creature". | End step (reuses `TriggerTargetCollector.Options.END_STEP` for the target list) |
| `SUSPENDED_EACH_UPKEEP_TRIGGERED` | `StepTriggerService` scans suspended cards in exile on every player's upkeep; an active-player `MayEffect` preserves that player as the choice and effect context | Non-targeting; source card must remain suspended at resolution |
| `ON_SELF_PHASES_IN` (permanent-target effects) | `TriggerCollectionService.enqueuePhasingTriggers` → `PhasesInTriggerTarget` (queued during the untap-step phasing action when `targetSpec()` includes permanents, e.g. Shimmering Efreet's "target creature phases out"); drained at upkeep start via `StepTriggerService.processNextPhasesInTriggerTarget` (also AutoPass). Honours the card's `PermanentPredicateTargetFilter`. | End step (reuses `TriggerTargetCollector.Options.END_STEP`) |
| `END_STEP_TRIGGERED` | `StepTriggerService.handleEndOfTurnTriggers` (non-kicked / morbid / default) | End step |
| `CONTROLLER_END_STEP_TRIGGERED` | `StepTriggerService.handleEndOfTurnTriggers` (raid / default; multi-target groups reuse `ETBTokenMultiTargetTrigger`) | End step |
| `OPPONENT_END_STEP_TRIGGERED` | `StepTriggerService.handleEndStepTriggers` (fires only when the end-step player is an opponent of the permanent's controller; end-step player baked into `targetId` for the intervening-if `ConditionalEffect`, e.g. Predatory Advantage's `EndStepPlayerDidntCastCreatureSpell`) | Non-targeting |
| `ON_SELF_LEAVES_BATTLEFIELD` (targeting effects only) | `DeathTriggerCollectorService.handleSelfLeavesDefault` → `SelfTriggeredAbilityTarget` (queued when an effect's `targetSpec()` includes players/permanents, e.g. Meadowboon, or admits a graveyard card — `admits(Kind.GRAVEYARD_CARD)`, e.g. Offalsnout). `TriggeredAbilityQueueService.processNextSelfTriggeredAbilityTarget` routes graveyard-targeting effects (`ExileGraveyardCardsEffect(TARGET_CARDS_ANY_GRAVEYARD)`) to a `MultiGraveyardChoice` card choice instead of the permanent/player path. | End step (reuses `TriggerTargetCollector.Options.END_STEP`); non-targeting effects push straight to the stack |
| `ON_CONTROLLER_ARTIFACT_OR_CREATURE_CARDS_LEAVE_GRAVEYARD` (targeting effects only) | `MiscTriggerCollectorService.handleControllerArtifactOrCreatureCardsLeaveGraveyard` → `SelfTriggeredAbilityTarget`; intervening-if conditions are checked when the trigger event occurs, then permanent/player targets are collected as the ability is put on the stack | End step (reuses `TriggerTargetCollector.Options.END_STEP`) |
| `ON_SELF_BECOMES_MONSTROUS` (targeting effects only) | `MonstrosityEffectHandler` → `TriggerCollectionService.checkBecomesMonstrousTriggers` → `SelfTriggeredAbilityTarget`; target selection uses the same permanent-targeting queue as other self-triggered abilities. | When monstrosity resolves |
| `ON_SELF_DISCARDED` | `TriggerCollectionService.checkDiscardTriggers` | Discard-self (any cause; non-targeting → stack, any-target → `DiscardTriggerAnyTarget`) |
| `ON_SELF_DISCARDED_BY_OPPONENT` | `TriggerCollectionService.checkDiscardSelfTriggers` | Discard-self |
| `ON_CONTROLLER_DISCARDS` (targeting variants) | `DiscardTriggerCollectorService` → `DiscardControllerTriggerTarget` (queued when a controller-discard effect's `targetSpec()` includes permanents, e.g. Zenith Seeker's "target creature gains flying"). Non-targeting controller-discard effects (Hekma Sentinels self-boost, Curator of Mysteries scry, Necropotence exile) still enqueue a `TRIGGERED_ABILITY` straight onto the stack. | Controller-discard (reuses `TriggerTargetCollector.Options.ATTACK`; honours the effect's `targetSpec().predicate()`) |
| `ON_CONTROLLER_SCRIES` | `ScryTriggerCollectorService` | Controller scry; non-targeting effects enqueue directly, while targeted effects use the standard spell-target trigger choice |
| `ON_CONTROLLER_INVESTIGATES` | `InvestigateTriggerCollectorService` | The controller's first investigate event each turn; non-targeting effects enqueue directly |
| `ON_CONTROLLER_SURVEILS` | `MiscTriggerCollectorService` | Controller surveils; non-targeting effects enqueue directly |
| `ON_CONTROLLER_DISCARD_EVENT` | `TriggerCollectionService.checkDiscardEventTriggers` → `DiscardTriggerCollectorService` | One trigger for a one-or-more-card discard event; the count is carried by the trigger context and stack entry |
| `ON_BECOMES_TARGET_OF_SPELL` / `…_OR_ABILITY` / `…_OF_OPPONENT_SPELL` / `…_OF_OPPONENT_SPELL_ONLY` | `TriggerCollectionService.checkBecomesTargetOfSpell*` | Spell-target |
| `ON_CONTROLLER_BECOMES_TARGET_OF_SPELL` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers` (targeted player; spell path only) | Spell-target |
| `ON_ANY_PLAYER_CHOOSES_TARGETS` | `TriggerCollectionService.checkTargetChoiceTriggers` after spell/ability target selection | Target-choice event; the chosen spell/ability is carried as non-targeting `triggeringCardId` |
| `ON_CONTROLLER_CASTS_SPELL` / `ON_ANY_PLAYER_CASTS_SPELL` (targeting variants) | `SpellCastTriggerCollectorService` | Spell-target |
| `ON_CONTROLLER_COPIES_SPELL` / `ON_OPPONENT_COPIES_SPELL` (targeting variants) | `TriggerCollectionService.checkSpellCopyTriggers` + `SpellCastTriggerCollectorService` | Spell-target when `SpellCopyTriggerEffect` carries a `TargetFilter`; otherwise non-targeting |
| `ON_SELF_CAST` (targeting variants) | `TriggerCollectionService.checkSpellCastTriggers` | Spell-target (single); multi-target (`maxTargets > 1`) reuses `ETBTokenMultiTargetTrigger` |
| `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` (targeting branch) | `DamageTriggerCollectorService` | Spell-target |
| `ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT` | `TriggerCollectionService.checkOpponentSourceDamageToYouOrYourPermanentTriggers` + `DamageTriggerCollectorService` | Non-targeting; source permanent and controller are bound in the queued may ability |
| `ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU` | `TriggerCollectionService.queueCreatureCombatDamageToYouTriggers` — never targets; the damaging creature is baked in as the stack entry's `targetId` with `nonTargeting = true`, so no pending-choice is queued. Teysa, Envoy of Ghosts. | None (baked-in subject) |
| `ON_CONTROLLER_GAINS_LIFE` | `MiscTriggerCollectorService` | Life-gain |
| `GRAVEYARD_ON_OPPONENT_GAINS_LIFE` | `TriggerCollectionService.checkLifeGainTriggers` | None (graveyard-resident, non-targeting) |
| `ON_CONTROLLER_DRAWS` (any-target effects) | `DrawService.checkControllerDrawTriggers` → `DrawTriggerAnyTarget` (queued when the effect's `targetSpec().declares(TargetPredicates.anyTarget())`, e.g. Niv-Mizzet, the Firemind's "deals 1 damage to any target"). Processed by `TriggeredAbilityQueueService.processNextDrawTriggerTarget` (creature/player any-target choice). Non–any-target draw triggers (Psychosis Crawler) still push a non-targeting entry straight to the stack. | Draw (any target) |
| `ON_CREATURE_ENTERS_FROM_GRAVEYARD` | `TriggerCollectionService.checkEntersFromGraveyardTriggers` | Enters-from-graveyard (any target) |
| `ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD` / `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD` / `ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD` / `ON_OPPONENT_LAND_ENTERS_BATTLEFIELD` / `ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD` (permanent-targeting effects only) | `EnterTriggerCollectorService.handleEnterDefault` → `EntersTriggerTarget` (queued when the effect's `targetSpec()` includes permanents, e.g. Reaper King's "destroy target permanent"). Player-targeting effects still push straight to the stack with the pre-set `defaultTargetPlayerId`. | Enters (reuses `TriggerTargetCollector.Options.ATTACK` for the target list — permanents honouring the card's `PermanentPredicateTargetFilter` / `ControlledPermanentPredicateTargetFilter`; true "any target" effects are narrowed by evaluating `TargetPredicates.anyTarget()`) |
| `ON_ALLY_TOKEN_ENTERS_BATTLEFIELD` | `PermanentControlSupport.applyCreateToken` → `BattlefieldEntryService.checkAllyTokenEntersTriggers` → `TriggerCollectionService.checkAllyTokenEntersTriggers`; fires once per token-creation batch and snapshots the batch count into the trigger's `eventValue` | Token creation (non-targeting) |
| `ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD` (permanent-targeting effects only) | `TriggerCollectionService.checkAllyEnchantmentEntersTriggers` → `EntersTriggerTarget` (queued when the resolved effect's `targetSpec()` includes permanents — including a `MayEffect` wrapper, whose spec delegates to the wrapped effect; Oath of the Ancient Wood's "you may put a +1/+1 counter on target creature"). Non-targeting effects still push straight to the stack with `triggeringCardId` set. | Enters (same `Options.ATTACK` target list; honours the card's `PermanentPredicateTargetFilter`) |
| `ON_AURA_ATTACHED_TO_SELF` (non-targeting only) | `TriggerCollectionService.checkAuraAttachedTriggers` — called after `setAttachedTo` from `StackResolutionService` (Aura spell resolving, incl. reanimation Auras), `AttachSourceAuraToTargetCreatureEffectHandler`, `AttachTargetAuraToTargetCreatureEffectHandler`, and the Aura-move player choices in `PermanentChoiceBattlefieldHandlerService` (Aura Graft, attach-all-Auras, reattach-after-sacrifice). Fires on the newly enchanted permanent for **its** controller, so an opponent's Aura triggers it. Brood Keeper. | — (pushes a non-targeting entry straight to the stack) |
| `ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT` (non-targeting only) | `TriggerCollectionService.checkAuraAttachedTriggers` — uses the Aura permanent's current controller, the newly enchanted permanent's current controller, effective land type, and mana values at the attachment event. Scans the Aura controller's battlefield for the slot and stores the Aura as `sourcePermanentId` plus the enchanted permanent as `targetId`; Eriette the Beguiler. | — (pushes a non-targeting entry straight to the stack) |
| `GRAVEYARD_ON_COMBAT_DAMAGE_TO_YOU_OR_YOUR_PLANESWALKER` | `CombatDamageService.checkGraveyardCombatDamageToYouOrPlaneswalkerTriggers` — fires from the graveyard of every player dealt combat damage this step, directly or on a planeswalker they control. The only targeting graveyard slot: it queues an `AttackTriggerTarget` whose `sourceCard` is the graveyard card (no source permanent), and `CombatDamageService` drains it before the damage step ends so "attacking creature" target filters still see the attackers. Vengeful Pharaoh. | Attack |
| `ON_ALLY_CREATURE_EXPLORES` | `TriggerCollectionService.checkExploreTriggers` | Explore |
| `ON_CREWS_VEHICLE` | `TriggerCollectionService.checkCrewsVehicleTriggers` from `CrewCostHandler`; the Vehicle is stored as the triggered entry's `triggeringPermanentId` so effects can resolve against "that Vehicle" | Non-targeting |
| `ON_EXPLOIT` | `TriggerCollectionService.checkExploitTriggers` | Exploit |
| `ON_CONTROLLER_CLASHES` | `TriggerCollectionService.fireClashTriggers` | Clash — targeting triggers via `ClashTriggerTarget` (opponent-creature only); non-targeting triggers pushed straight to the stack |
| `ON_CHAMPIONED` | `PermanentChoiceBattlefieldHandlerService.handleChampionCreature` | Player/permanent target via `ChampionedTriggerTarget` (collected with `Options.END_STEP`; Mistbind Clique taps target player's lands) |
| `ON_DAMAGED_CREATURE_DIES` (targeting effects) | `GraveyardService.enqueueDamagedCreatureDiesTriggers` → `SelfTriggeredAbilityTarget`; the target is chosen as the trigger is put on the stack. Non-targeting effects are pushed directly. | Damaged-creature death (reuses `Options.END_STEP`) |
| Planeswalker ultimate emblems | `DrawService` / `TriggerCollectionService` (including land-entry emblem markers) | Emblem |
| `SAGA_CHAPTER_I` / `SAGA_CHAPTER_II` / `SAGA_CHAPTER_III` | `StepTriggerService.processSagaChapters` / `StackResolutionService` | Saga chapter |

Slots that currently **only ever push non-targeting entries** (no pending queue):
`ON_TAP`, `STATIC`, `ON_SACRIFICE`, `ON_BLOCK` (only the non-targeting "that creature" flavour; the targeting variant is routed through the Attack pipeline — see the mapping table above),
`GRAVEYARD_UPKEEP_TRIGGERED`, `EACH_UPKEEP_TRIGGERED`, `SUSPENDED_EACH_UPKEEP_TRIGGERED`, `OPPONENT_UPKEEP_TRIGGERED`,
`ON_ANY_CREATURE_DIES`,
`ON_ALLY_NONTOKEN_CREATURE_DIES`, `ON_ANY_NONTOKEN_CREATURE_DIES`, `ON_OPPONENT_CREATURE_DIES`,
`ON_COMBAT_DAMAGE_TO_PLAYER` (when no target is declared), `ON_COMBAT_DAMAGE_TO_CREATURE`, `ON_DAMAGE_TO_PLAYER`,
`ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE`, `ON_EQUIPMENT_ATTACHED_TO_CREATURE`,
`ON_SELF_DEALS_DAMAGE`, `ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE`, `ON_BECOMES_BLOCKED` (only the non-targeting flavour; a
permanent-targeting `MayEffect` is routed through `queueMayAbility` — see the mapping table above),
`DRAW_TRIGGERED`, `EACH_DRAW_TRIGGERED`,
`ON_CONTROLLER_DRAWS` (only the non–any-target flavour; the any-target variant is routed through the
`DrawTriggerAnyTarget` pipeline — see the mapping table above), `ON_OPPONENT_DRAWS`, `ON_OPPONENT_DISCARDS`,
`ON_ANY_PLAYER_TAPS_LAND`, `ON_ALLY_PERMANENT_BECOMES_TAPPED`, `ON_OPPONENT_PERMANENT_BECOMES_TAPPED`, `ON_CREWS_VEHICLE`,
`ON_ALLY_PERMANENT_SACRIFICED`, `ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED` (carries the sacrificed card id on the trigger for effects such as It That Betrays), `ON_ALLY_CREATURES_ATTACK`,
`ON_ANY_PLAYER_TAPS_LAND`, `ON_CREWS_VEHICLE`,
`ON_ALLY_PERMANENT_SACRIFICED`, `ON_ALLY_CREATURES_ATTACK`,
`ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` (Jinxed Ring; fires only for nontoken permanents entering the graveyard owned by the slot's controller),
`ON_ANY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` (Liability; fires for nontoken permanents entering any player's graveyard and bakes the graveyard owner as the trigger's target),
`ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` (Scrapheap; fires for any permanent, including tokens, entering the graveyard owned by the slot's controller),
`ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` (non-targeting effects use the direct stack path; `TriggeringArtifactControllerConditionalEffect` uses the spell-target pipeline),
`ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` (Femeref Enchantress; fires on every permanent with the slot
whenever an enchantment is put into a graveyard from the battlefield — checked in `PermanentRemovalService`),
`ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` (Dingus Egg; fires on every permanent with the slot
whenever a land is put into a graveyard from the battlefield — checked in `PermanentRemovalService`; the
collector pre-sets the damage target to the land's controller),
`ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD`,
`ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT` (Sacred Ground; fires only on permanents the
graveyard owner controls, and only when `GameData.currentlyResolvingControllerId` — the controller of
the resolving spell/ability — is an opponent of the graveyard owner; the collector stamps the dying
land card id onto a fresh `ReturnTriggeringLandFromGraveyardToBattlefieldEffect`),
`ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE` (Countryside Crusher; fires on every permanent the
graveyard owner controls whenever a non-token land card enters their graveyard from any zone — checked in
`GraveyardService.addCardToGraveyard`, the single zone→graveyard choke point; wrap the effect in
`OncePerTurnTriggerEffect` for “the first time each turn”),
`ON_ALLY_LAND_CARD_MILLED` (Pedantic Learning; fires on every permanent the graveyard owner controls
whenever a non-token land card actually enters their graveyard from their library — checked in
`GraveyardService.addCardToGraveyard` after replacement effects),
`ON_ALLY_CREATURE_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY` (Sidisi, Brood Tyrant; fires once per
library-to-graveyard event when one or more non-token creature cards actually enter the graveyard
owner's graveyard — mill batches are checked in `GraveyardService.resolveMillPlayer` after replacement
effects),
`ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE` (Soulcipher Board; fires on every permanent the
graveyard owner controls whenever a non-token creature card enters their graveyard from any zone — uses
printed card types, not battlefield creature-ness; checked in `GraveyardService.addCardToGraveyard`),
`ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE` (Quest for Ancient Secrets; fires on every permanent the
graveyard owner controls whenever a non-token card enters their graveyard from any zone — checked in
`GraveyardService.addCardToGraveyard`),
`ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE` (Compost; fires on every permanent controlled by
an opponent of the graveyard owner whenever a black card enters that graveyard from any zone — checked in
`GraveyardService.addCardToGraveyard`), `ON_ENCHANTED_PERMANENT_TAPPED`,
`ON_ALLY_PERMANENT_BECOMES_TAPPED` and `ON_OPPONENT_PERMANENT_BECOMES_TAPPED` (non-targeting effects use the direct
stack path; graveyard-card targets use the shared `SpellGraveyardTargetTrigger` flow),
`ON_SELF_BECOMES_UNTAPPED` (Hollowsage; fires when the permanent transitions tapped→untapped, from
the untap step or any untap effect, via `TriggerCollectionService.checkBecomesUntappedTriggers` — driven
from `UntapStepService` and `TapUntapSupport.untapPermanent`. Targeted effects choose targets as the ability
is put on the stack; a "you may have target player …" is expressed as a `MayEffect`-wrapped targeting effect
whose "may" and target are resolved on the stack via the pending-may-ability flow),
`ON_ALLY_PERMANENT_BECOMES_UNTAPPED` (Wake Thrasher; the untapped-ally counterpart of
`ON_ALLY_PERMANENT_BECOMES_TAPPED` — fires once per untapped permanent on every permanent with the slot on
the untapped permanent's controller's battlefield, including the source untapping itself; same
`checkBecomesUntappedTriggers` call sites. Wrap in `TriggeringPermanentConditionalEffect` to filter by the
untapped permanent),
`ON_ANY_PERMANENT_BECOMES_UNTAPPED` (Mesmeric Orb; fires once for every permanent that transitions
tapped→untapped on any battlefield. Wrap in `TriggeringPermanentConditionalEffect` to filter by the
untapped permanent. The untapped permanent and its controller are recorded on the non-targeting
triggered entry so an effect can use that controller even if the permanent leaves before resolution),
`ON_SELF_BECOMES_RENOWNED` / `ON_ALLY_CREATURE_BECOMES_RENOWNED` (Relic Seeker / Valeron Wardens; fired from
`RenownEffectHandler` via `TriggerCollectionService.checkBecomesRenownedTriggers` on the flip to renowned
only. The ally slot fires on every permanent with it on the renowned creature's controller's battlefield,
including that creature itself; wrap in `TriggeringPermanentConditionalEffect` to filter by the renowned
creature),
`ON_SELF_PHASES_OUT` / `ON_SELF_PHASES_IN` (Teferi's Imp; fired from `PhasingService` via
`TriggerCollectionService.checkPhasesOutTriggers` / `checkPhasesInTriggers` — phase-out triggers are collected
before the permanent leaves the battlefield, since they look back in time (CR 603.10b). Non-targeting
phase triggers cover the untap-step turn-based action and effect-driven phase-outs. **Targeted**
`ON_SELF_PHASES_IN` effects — when `targetSpec().admits(Kind.PERMANENT)` — queue a
`PhasesInTriggerTarget` instead and are drained at upkeep start via
`StepTriggerService.processNextPhasesInTriggerTarget` (Shimmering Efreet's "target creature phases out";
reuses `TriggerTargetCollector.Options.END_STEP`)),
`ON_ENCHANTED_CREATURE_DEALT_DAMAGE`,
`ON_OPPONENT_LAND_ENTERS_BATTLEFIELD`, `ON_ALLY_LAND_ENTERS_BATTLEFIELD`,
`ON_OPENING_HAND_REVEAL`, `ON_OPPONENT_LOSES_LIFE`, `ON_OPPONENT_SHUFFLES_LIBRARY`,
`ON_OPPONENT_SEARCHES_LIBRARY` (Ob Nixilis, Unshackled; fired by `LibrarySearchTriggerHelper` from
`LibrarySearchSupport.sendLibrarySearchToPlayer` — the choke point every card-presenting search passes
through — plus the empty-library / no-match early returns of `performLibrarySearch`, which are searches
too. Only a player searching **their own** library counts (`params.targetPlayerId()` unset); a search
prevented by Leonin Arbiter never happens and does not fire. Non-targeting, but the searching player is
baked in as the trigger's `targetId`, so `TARGET_PLAYER`-scoped effects act on them),
`ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED`, `ENCHANTED_PLAYER_UPKEEP_TRIGGERED`,
`ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD`,
`ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD` (Trial of Solidarity; "Whenever an enchantment enters under
your control" — checked in `TriggerCollectionService.checkAllyEnchantmentEntersTriggers` from
`BattlefieldEntryService.processCreatureETBEffects`, skips the entering permanent itself; gate by subtype
with a `TriggeringCardConditionalEffect(CardSubtypePredicate(...))` for "Whenever a Cartouche you control enters"),
`ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD`,
`ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE`, `GRAVEYARD_ON_OPPONENT_DAMAGED_BY_RED_SPELL_OR_PLANESWALKER`,
`ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER`,
`ON_OPPONENT_CREATURE_CARD_MILLED`, `ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD`,
`ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD` (Extractor Demon; global watcher — fires on every permanent
with the slot whenever another creature leaves the battlefield by any means, checked in
`PermanentRemovalService` via `TriggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers`.
Non-targeting: a "you may have target player mill two cards" is a `MayEffect`-wrapped
`MillEffect(2, TARGET_PLAYER)` whose "may" and player target are resolved on the stack),
`ON_SELF_MILLED`, `STATE_TRIGGERED`, `BEGINNING_OF_COMBAT_TRIGGERED`,
`EACH_BEGINNING_OF_COMBAT_TRIGGERED`, `OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED`,
`ON_OPPONENT_CREATURE_DEALT_DAMAGE`, `GRAVEYARD_ON_CONTROLLER_CASTS_SPELL`,
`ON_CONTROLLER_LOSES_LIFE`,
`ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT`,
`ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_NON_HYDRA_CREATURE` (Wildwood Scourge; fires once when
one or more +1/+1 counters are put on another non-Hydra creature the controller controls),
`ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER` (All Will Be One; fires once for each counter-placement
event caused by the controller, including poison counters, and uses the spell-target trigger pipeline),
`ON_ALLY_COUNTER_PUT_ON_CREATURE` (Hollowmurk Siege; fires for counters of any type put on a creature
the controller controls, including counters the creature enters with; a `OncePerTurnTriggerEffect`
is marked only after its mode condition is met. `OncePerTurnPerCreatureTriggerEffect` uses the same
slot for first-time-per-creature wording and keys the marker to the watcher and triggering creature),
`ON_SELF_EVOLVES` (Renegade Krasis; fired by `EvolveTriggerEffectHandler` only when the evolve trigger
actually places the +1/+1 counter),
`ON_MINUS_ONE_MINUS_ONE_COUNTER_PUT_ON_CREATURE` (Flourishing Defenses; global watcher — fires on
every permanent with this slot, under that permanent's controller, once per individual -1/-1 counter put
on any creature from any source, via `PermanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers`;
non-targeting — a "you may create …" is a `MayEffect` resolved on the stack),
`ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTER_ON_CREATURE` (Nest of Scarabs; controller-restricted variant of
the above — same firing method and per-counter cadence, but a permanent only triggers when its controller
is the player who put the counters. The placing player is `gameData.currentlyResolvingControllerId` for
spell/ability resolution and the damage source's / permanent's controller for combat placements, passed via
the 4-arg overload of `fireMinusOneMinusOneCounterPutOnCreatureTriggers`; non-targeting),
`ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTERS_ON_CREATURE` (Hapatra, Vizier of Poisons; the "one or more
counters, do it once" cadence — same controller restriction and firing method as the per-counter slot
above, but fires exactly one trigger per creature per placement instance regardless of how many -1/-1
counters were placed at once; non-targeting — the Snake creation is a plain `CreateTokenEffect`),
`ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`,
`GRAVEYARD_ON_ALLY_CREATURES_ATTACK`, `GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER`,
`GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD` (graveyard mirror of `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD`;
`TriggeringCardConditionalEffect` subtype-gate + `MayPayManaEffect` pay-to-return — Unconventional Tactics),
`GRAVEYARD_ON_CREATURE_ENTERS_FROM_GRAVEYARD_OR_CAST_FROM_GRAVEYARD` (scans each graveyard for
creatures that entered from that graveyard or were cast from it; Prized Amalgam),
`ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY`,
`ON_TRANSFORM_TO_BACK_FACE`, `ON_TRANSFORM_TO_FRONT_FACE`,
`ON_CONTROLLER_ACTIVATES_ABILITY` (Ceaseless Searblades; fires on every permanent with this slot on
the activating player's battlefield, once per activated-ability activation incl. mana abilities;
wrap in `TriggeringPermanentConditionalEffect` to filter by the permanent whose ability was activated),
`ON_OPPONENT_ACTIVATES_NONMANA_ABILITY` (Harsh Mentor; the opponent-scoped mirror — fires on every
permanent NOT controlled by the activating player, only on the non-mana activation path so mana
abilities never trigger it; wrap in `TriggeringPermanentConditionalEffect` to filter by the activated
permanent's type; the activating opponent is baked as the non-targeting `targetId`),
`ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM` (Vizier of the Anointed; fires once per activation on
every permanent with this slot on the activating player's battlefield when the activated graveyard
ability is embalm/eternalize — `ActivatedAbility.isEmbalmOrEternalize()`; no conditional wrapper, the
keyword gate is applied at the call site in `AbilityActivationService.completeGraveyardAbilityActivation`).

## `ON_ENTER_BATTLEFIELD` targeted triggers

A targeted ETB (the card has a `TargetFilter` and a mandatory `ON_ENTER_BATTLEFIELD` effect, e.g.
Pierce Strider, Geralf's Messenger) normally has its target chosen **at cast time** and the trigger
is pushed directly onto the stack with that target. When the permanent enters **without a cast** —
a token copy, a creature put onto the battlefield from a graveyard via undying / reanimation /
flicker, or **any land** (lands are played, never cast; e.g. Sunscorched Desert's "deals 1 damage
to target player or planeswalker") — there is no cast-time target, so
`BattlefieldEntryService.processCreatureETBEffects` routes the trigger through
`ETBTokenTargetTrigger` (single target) or `ETBTokenMultiTargetTrigger` (multi-target), letting the
controller choose the target as the ability is put on the stack (CR 603.3b / 603.6c). The
`choosesTargetAtTriggerTime` gate is `card.isToken() || enteredFromGraveyard || enteredFromExile ||
hasType(LAND)` (`enteredFromExile` covers a delayed return from exile — flicker, Obzedat, Ghost Council); the
entering permanent's `getEnteredFromGraveyardOwnerId()` distinguishes a graveyard return from a
cast; "up to N" cast spells that chose 0 targets are unaffected because they passed through
cast-time selection. A land with a targeted ETB still declares its `target(...)` filter like any
other card — for a "player or planeswalker" effect use a `PermanentPredicateTargetFilter(new
PermanentIsPlaneswalkerPredicate(), …)` (the permanent side narrows to planeswalkers; players are
always legal), the same idiom as Noggle Hedge-Mage. Sunscorched Desert is the reference land.

**Gate-conditional targeted ETBs** (`ConditionalEffect` whose condition returns
`Condition.isEtbTriggerGate()` — Metalcraft, Morbid, Raid, ControlsAnotherPermanent, ControlsPermanent,
OpponentControlsMoreLands, `OpponentLostLifeThisTurn`, `ControllerHandEmpty`; e.g. Bleak
Coven Vampires, Morkrut Banshee, Storm Fleet Pyromancer, Dreamcaller Siren, Parasitic Strix, Knight of the White Orchid, Voldaren Ambusher, Bloodhall Priest) **never** target at
cast time: whether the ability triggers at all depends on game state as the permanent enters
(intervening-if, CR 603.4), so `EffectResolution.computeAllowedTargets` excludes them from the
spell's cast-time target requirement and `EtbEffectResolver` drops the trigger entirely when the
gate isn't met. When it is met, the same `ETBTokenTargetTrigger` / `ETBTokenMultiTargetTrigger`
deferred path prompts for the target as the trigger goes on the stack (CR 603.3d); the wrapped
`ConditionalEffect` stays on the stack entry and the gate is re-checked at resolution. When adding
a new intervening-if condition that gates a **targeted** ETB, override `isEtbTriggerGate()` on the
condition — both the cast-time exclusion and the `EtbEffectResolver` gate key off it.

**Graveyard-targeting ETBs** ("When ~ enters, return/exile/… target card from a graveyard") never
target at cast time. `BattlefieldEntryService.queueMandatoryETBEffects` partitions these by kind and
routes each to its trigger-time graveyard selector: `ExileCardsFromGraveyardEffect` (all graveyards) →
`handleGraveyardExileETBTargeting`, `ExileGraveyardCardsEffect` with a graveyard-card scope (Disposal
Mummy: opponent's graveyard, or `TARGET_CARDS_ANY_GRAVEYARD`) → `handleGraveyardCardsExileETBTargeting`,
cast/flashback/may-play/opponent-steal → their dedicated handlers, return-to-hand →
`handleReturnToHandETBTargeting`, controller-graveyard shuffle-into-library →
`handleShuffleIntoLibraryETBTargeting`, and a player-targeted graveyard-card choice → the ETB
player-target flow followed by the multi-graveyard choice flow. Any remaining
`targetSpec().admits(Kind.GRAVEYARD_CARD)` effect (i.e. a
`targetGraveyard(true)` `ReturnCardFromGraveyardEffect`, e.g. Bladewing the Risen reanimating to the
battlefield) is routed through the shared `SpellGraveyardTargetTrigger` flow, which prompts the
controller with a `MultiGraveyardChoice` (maxCount 1) as the trigger goes on the stack; the chosen id
lands on the entry's `targetCardIds` and the effect handler's pre-targeted path resolves it. Because
the trigger path allows an empty selection, a "you may return target …" reads correctly as up-to-one
(choose 0 to decline) with no `MayEffect` wrapper. `BecomeAuraReanimateFromGraveyardEffect` (Necromancy)
uses the same flow with `ALL_GRAVEYARDS` — any player's graveyard, creature cards only. `PutCardFromOpponentGraveyardOntoBattlefieldEffect` uses it too (`OPPONENT_GRAVEYARD`); on a combat-damage trigger `CombatDamageService` sets the pending trigger's `graveyardOwnerId` to the damaged player, so Ink-Eyes, Servant of Oni sees only **that player's** graveyard.

**Graveyard-targeting upkeep triggers** ("At the beginning of your upkeep, you may return target
enchantment card from your graveyard to the battlefield" - Starfield of Nyx) use the same
`SpellGraveyardTargetTrigger` flow. `StepTriggerService.handleUpkeepTriggers` pulls every
`UPKEEP_TRIGGERED` effect whose `targetSpec().admits(Kind.GRAVEYARD_CARD)` out of the per-effect loop
and queues them as one interaction, drained at the end of the same method; no matching card in the
graveyard means the trigger is never put on the stack (CR 603.3c). As above, the "you may" needs no
`MayEffect` wrapper - the up-to-one pick can be left empty.

**Graveyard-targeting death triggers** ("When ~ dies, exile target card from an opponent's graveyard" —
Ruin Rat) use the same trigger-time graveyard selection, but on the `ON_DEATH` path. `handleDeathDefault`
routes any death effect whose `targetSpec().admits(Kind.GRAVEYARD_CARD)` to a `DeathTriggerTarget` (alongside
the permanent/player routing), and `TriggeredAbilityQueueService.processNextDeathTriggerTarget` detects the
`ExileGraveyardCardsEffect` and calls `beginDeathGraveyardTarget`, which searches the graveyards the
effect's declared `GraveyardSearchScope` names (`TargetSpec.graveyardScope()`) and prompts a
`MultiGraveyardChoice`. With no legal target the death trigger is skipped, never put on the stack (CR
603.3c). Use `ON_DEATH`, never `ON_SELF_LEAVES_BATTLEFIELD` (Offalsnout), for a "dies" trigger — the latter
also fires on exile/bounce.

If the card you are implementing needs one of these slots **and** a user target choice (either player
or permanent), **that is an engine change**. The work required is:

1. Add a new `PermanentChoiceContext.XxxTriggerTarget` record (or reuse an existing one).
2. Queue it on the unified `GameData.pendingInteractions` queue via `gameData.queueInteraction(...)`
   (no new field needed; `simulationCopy` already copies the queue).
3. In the collector that notices the trigger, route targeting effects into the queue.
4. In the step that drains it (via `peekPendingInteraction` / `pollPendingInteraction` on the record class), call
   `TriggerTargetCollector.collect(...)` with an appropriate `Options` — or extend `Options` if none of
   `DEATH` / `ATTACK` / `END_STEP` match the semantics you need.
5. Handle the empty-target case (log + skip) and the prompt wording.
6. Wire the queue into `AutoPassService` so the turn doesn't advance while it is non-empty.
7. Cover it in `TriggerTargetCollectorTest` and an end-to-end card test.

---

## Filter-by-filter reference

### `PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT))`

Card says "target opponent". Honoured in the **Death / Attack / End-step** pipelines (`Options.DEATH`,
`ATTACK`, `END_STEP`) and in the single-player **upkeep** pipeline (`UpkeepPlayerTargetTrigger`, which
filters candidates through `ValidTargetService.filterValidPlayerTargets` using the card's target filter —
e.g. Nath of the Gilt-Leaf). Any other pipeline that offers the controller as a valid target too is **a
bug** and must be fixed in the pipeline, not papered over at the card level.

When used with an effect whose `targetSpec()` also includes permanents, the opponent-only restriction
applies **only** to the player branch of the target list; permanents are still filtered via
`PermanentPredicateTargetFilter` (see below). If you need "target opponent OR a permanent an opponent
controls", combine the two: `PlayerPredicateTargetFilter` for the player side and
`PermanentPredicateTargetFilter(opponentControlled(...))` for the permanent side.

### `PermanentPredicateTargetFilter(PermanentPredicate)`

Honoured in **Death / Attack / End-step**. See `PREDICATES_REFERENCE.md` for the full list of
`PermanentPredicate` compositions (e.g. `opponentControlled(creature())`, `nonToken(creature())`, etc.).

Note that the death pipeline defaults to `creaturesOnly = true`, **but** an explicit
`PermanentPredicateTargetFilter` overrides it — the filter's predicate then fully governs which
permanents are legal (e.g. Fire Snake's "destroy target land"). A death trigger with **no** target
filter (or a `ControlledPermanentPredicateTargetFilter`) still narrows to creatures. End-step has no
such restriction.

**Death-event value (`ON_ALLY_CREATURE_DIES`):** a targeted ally-death trigger is queued with the dying
creature's last-known effective power (clamped at 0) in `PermanentChoiceContext.DeathTriggerTarget.eventValue`;
`PermanentChoiceTriggerHandlerService.handleDeathTrigger` copies it onto the stack entry. An effect whose
amount is `new EventValue()` therefore resolves to that power — Death's Presence, "put X +1/+1 counters on
target creature you control, where X is the power of the creature that died". No dedicated effect record is
needed for that shape.

### `ControlledPermanentPredicateTargetFilter(PermanentPredicate)`

Honoured **only** by Death and Attack pipelines. End-step does not read this filter; use
`PermanentPredicateTargetFilter` with `opponentControlled(...)` / `allied(...)` instead.

**Per-effect attribution (Death):** the card-level `getTargetFilter()` is the first `target(...)`
group's filter. When a card has two differently-targeted abilities (e.g. Soulstinger's cast-time ETB
"target creature you control" plus a death trigger that targets any creature),
`TriggeredAbilityQueueService.processNextDeathTriggerTarget` skips that filter unless one of the death
trigger's own effects is bound to a declared target group (checked via `Card.getEffectTargetIndex`). So
attach the ETB filter with `target(...).addEffect(ON_ENTER_BATTLEFIELD, …)` and add the death effect via
plain `addEffect(ON_DEATH, …)` (no `target(...)`); the ETB filter then does not leak into the death
trigger.

### Effect-level target predicate (`targetSpec().predicate()`)

The predicate an effect carries in its `targetSpec()` (read via `EffectResolution.targetPredicateOf`,
which also honours the `PutCounterOnTargetPermanentEffect.targetPredicate` component dual) is honoured
by the End-step (and Saga chapter), Attack, Upkeep and Death pipelines. Death honours it because a
granted death trigger has no card-level filter of its own — the dying creature's card is not the card
that granted the ability (Showstopper). For a card's own death trigger either place works; the
card's `TargetFilter` is still the conventional home. The end-step pipeline will also unwrap
`ConditionalEffect` (morbid / metalcraft / raid / …) wrappers before inspecting the wrapped effect's
targeting (`targetSpec()` category + predicate).

### May-ability target enumeration

A targeting effect wrapped in `MayEffect` still has its target chosen as the triggered ability is
put on the stack. `TriggerTargetCollector` unwraps the optional effect to discover that restriction;
the may decision remains a resolution-time choice. Effects that explicitly create a later reflexive
trigger are non-targeting until that trigger is created.

When `MayAbilityHandlerService` performs a resolution-time target choice for one of those later
effects, `mayAbilityPermanentTargets` uses this precedence:

1. the card-level `TargetFilter` (`mayAbilityTargetFilter`, which prefers the effect's own declared
   target group over the card's primary filter), **and**
2. the effect's own `PermanentPredicate` (`EffectResolution.targetPredicateOf`), if it carries one;
3. only when the ability declares **neither** does the effect's `TargetSpec` restrict anything — and
   then it is evaluated by `TargetPredicateEvaluationService`, the same shared evaluator cast-time
   validation uses.

A card-level filter therefore *replaces* the spec's own type restriction here; it does not stack with
it. Arm 3 used to be an open-coded target-category switch whose `default` rejected every permanent,
so a bare `land()` or `playerOrPlaneswalker()` spec found no legal target at all (Boggart Shenanigans
never offered a planeswalker).

### Unfiltered spell / activated-ability slots

A cast-time or activation target slot that carries **neither** a card/ability-level `TargetFilter`
**nor** a per-position one is restricted by what its effects declare:
`EffectResolution.declaredPermanentRestriction` conjoins every permanent restriction those effects
carry on their `TargetSpec`, and `EffectResolution.allowsPlayerTargets` says whether a player may be
chosen. Enumeration (`ValidTargetService.isValidPermanentTarget` /
`computeValidTargetsForAbility`) and validation (`TargetLegalityService.validateMultiSpellTargets` /
`validateMultiTargetAbility`) both read them, so the UI and the cast path cannot disagree.

"Any target" (CR 115.4 — a creature, player or planeswalker) is one such declaration. It used to be
*inferred* from "every permanent-targeting effect also accepts players", which could not tell it
apart from `PLAYER_OR_PERMANENT`; effects that picked the latter as an unchecked escape hatch got
the creature/planeswalker narrowing they never asked for, while their real restriction ("among any
number of target creatures") stayed unexpressed, and the cast path — which required a creature —
rejected the planeswalker enumeration had just offered.

A slot no effect restricts still falls back to the legacy creature-only default when it is
multi-target (Karn's Temporal Sundering's bare "target player" group).

---

## Common pitfalls

- **"My ON_DEATH trigger targets non-creatures."** Give the card an explicit
  `PermanentPredicateTargetFilter` — its predicate then governs and the death pipeline's default
  `creaturesOnly` narrowing is skipped (e.g. Fire Snake targeting a land). Without such a filter,
  death targets are creatures only.
- **"My ON_DEATH trigger lets the controller pick themselves as the target."** You forgot the
  `PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT))` on the card — or
  you wired it on a pipeline that doesn't honour it (everything outside death / attack / end-step).
- **"My end-step trigger ignores its target predicate."** It shouldn't, unless the effect is wrapped in a
  `ConditionalEffect` subclass that `Options.END_STEP.unwrapConditional()` doesn't know about. Check
  `TriggerTargetCollector.collect` — it unwraps any `ConditionalEffect` generically.
- **"My attack trigger uses a `targetSpec()` predicate and it's ignored."** Move the predicate onto the
  card's `TargetFilter` — attack (and death) read only the card-level filter.
- **"My trigger's two target groups affect each other's targets."** A trigger with more than one
  `target(...)` group must go through the slot-by-slot walker
  (`ETBTokenTargetService.processNextETBTokenMultiTargetTrigger`) so each group is prompted, and
  declined, separately — beginning-of-combat triggers route there via `hasMultipleTargetGroups`
  (Boros Battleshaper). The walker records how many targets each group actually took in
  `StackEntry.targetGroupSizes`; without that a declined "up to N" group would leave the flat target
  list sliced one group short, handing group 1's target to group 0's effect.
- **"My effect gets no valid targets offered even though the filter matches."** The effect probably
  leaves `targetSpec()` at `NONE`. The `CardEffectTargetingConsistencyTest`
  catches this for effects named `Target*Effect`, but not for other naming conventions.

## Aura trigger slot selection

Auras have their own trigger slots. Use this table to pick the correct one based on the oracle text:

| Oracle text pattern | Trigger slot | Fires when | Example |
|---|---|---|---|
| "At the beginning of your upkeep, ..." | `UPKEEP_TRIGGERED` | Aura controller's upkeep (aura is on their battlefield) | Call to the Kindred |
| "At the beginning of enchanted creature's controller's upkeep, ..." | `ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED` | Enchanted creature's controller is the active player | Necrotic Plague, Soul Bleed, Numbing Dose, Erosion (enchanted land) |
| "At the beginning of enchanted player's upkeep, ..." | `ENCHANTED_PLAYER_UPKEEP_TRIGGERED` | Enchanted player is the active player (curses) | Curse of Oblivion, Curse of the Bloody Tome |
| "At the beginning of each upkeep, ..." | `EACH_UPKEEP_TRIGGERED` | Every player's upkeep; targeted permanents are chosen by the source controller as the trigger is put on the stack | — |
| "At the beginning of each player's upkeep, if this card is suspended, ..." | `SUSPENDED_EACH_UPKEEP_TRIGGERED` | Every player's upkeep while the card is exiled with a positive time-counter entry | Curse of the Cabal (`TSP`) |
| "When enchanted creature dies, ..." | `ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD` | Enchanted creature goes to graveyard | Necrotic Plague (return effect), Banewasp Affliction (life loss = toughness), Creature Bond (damage = toughness), Death Watch (lose life = power + gain life = toughness) |
| "Whenever enchanted creature is dealt damage, ..." | `ON_ENCHANTED_CREATURE_DEALT_DAMAGE` | Enchanted creature is dealt damage (combat or non-combat) | Spiteful Shadows |
| "Whenever enchanted creature deals damage to a creature, ..." | `ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_CREATURE` | Enchanted creature deals damage to another creature (combat or non-combat); the damaged creature is captured as a non-targeting `targetId` | Venomous Fangs |
| "Whenever enchanted creature attacks and isn't blocked, ..." | `ON_ENCHANTED_CREATURE_ATTACKS_UNBLOCKED` | Enchanted attacker ends up unblocked (declare-blockers step). Non-targeting effects use `sourcePermanentId`=enchanted attacker and `targetId`=defending player. Permanent-targeting `MayEffect`s defer target selection and use the enchanted creature's controller for the may choice | Cloak of Confusion, Farrel's Mantle |
| "Whenever a creature is dealt damage, ..." (any creature) | `ON_ANY_CREATURE_DEALT_DAMAGE` | Any creature is dealt damage (combat or non-combat). Queued entry targets the damaged creature | Death Pits of Rath |

**`ON_ANY_CREATURE_DEALT_DAMAGE`, `ON_OPPONENT_CREATURE_DEALT_DAMAGE`,
`ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE` and `ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_CREATURE` fire only when the damaged permanent is a creature.** All
three are worded around damage dealt *to a creature*, so `DamageSupport.dealCreatureDamage` queues all
three behind one `gameQueryService.isCreature` gate (CR 603.2 — a trigger fires only when the event
matches its trigger event). The gate is layer-aware (CR 613.1d), unlike the printed type lines the
CR 120.3c loyalty and CR 120.3h defense branches in the same method key off: a planeswalker or battle
routed through this method is a *damage destination*, not a creature, so it must not fire them. A
permanent that is both a creature and a planeswalker does fire them.

The gate lives at the `DamageSupport` call site rather than inside `TriggerCollectionService` because
the call site still holds the live `Permanent` — `checkAllyDealtDamageToCreatureTriggers` takes only a
`UUID`, and the damaged creature may already have died to the same damage, so a lookup there would
suppress a trigger that legitimately fired. `checkCreatureDamageToYouOrYourPermanentTriggers`
(Mangara's Equity) is deliberately **outside** the gate: it also covers damage to the player, and the
effect's own `damagedPermanentFilter` does the narrowing in `DamageTriggerCollectorService`.

**Key distinction**: "your upkeep" on an aura means the **aura controller's** upkeep → use `UPKEEP_TRIGGERED`. "Enchanted creature's controller's upkeep" means the **enchanted permanent's controller's** upkeep → use `ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED`. These are different when the aura enchants an opponent's creature.

**How `UPKEEP_TRIGGERED` works for auras**: The aura permanent sits on the controller's battlefield. `StepTriggerService` iterates the active player's battlefield looking for permanents with `UPKEEP_TRIGGERED` effects. Since the aura is on the controller's battlefield, the trigger fires during the controller's upkeep. The `sourcePermanentId` on the stack entry is set to the aura permanent's ID (`perm.getId()`), so the resolution handler can find the enchanted creature via `auraPerm.getAttachedTo()`.

**How `ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED` works**: `StepTriggerService` iterates ALL permanents on ALL battlefields, checks if each has this effect slot and is attached, then finds the enchanted permanent's controller. It only fires when that controller is the active player.

**Targeted graveyard-return death triggers** use `ReturnCardFromGraveyardEffect` with
`targetGraveyard(true)` in the `ON_DEATH` slot. The death-trigger queue applies the effect's card
predicate and `source()` scope while the trigger is put on the stack, then stores the selected card ID
on the triggered entry for the normal return handler. An empty selection models an optional return.

For `ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY`, effects implementing `TriggeringSpellReferencingEffect`
also carry the triggering spell or activated ability as an internal `STACK` reference. The source
permanent id remains available so an Aura effect can re-derive its host at resolution.

`ON_CONTROLLER_DRAWS_SECOND_CARD` is checked by `DrawService.checkControllerDrawTriggers` after
the controller's per-turn draw count reaches exactly two. It uses the same stack and any-target
handling as `ON_CONTROLLER_DRAWS`, but does not fire on later draws in the same turn.
