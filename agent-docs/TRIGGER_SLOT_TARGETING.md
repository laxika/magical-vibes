# Trigger Slot Targeting Reference

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
| Death (`DeathTriggerTarget`)        | `Options.DEATH`    | ✅ | ✅ creatures only | ✅ | ✅ | ✅ | ❌ (ignored) |
| Attack (`AttackTriggerTarget`)      | `Options.ATTACK`   | ✅ | ✅ any permanent  | ✅ | ✅ | ✅ | ❌ (ignored) |
| End step (`EndStepTriggerTarget`)   | `Options.END_STEP` | ✅ | ✅ any permanent  | ✅ | ✅ | ❌ | ✅ (unwraps `ConditionalEffect`) |
| Discard-self (`DiscardTriggerAnyTarget`) | —                  | ✅ all players | ✅ creatures + planeswalkers only | ❌ | ❌ | ❌ | ❌ |
| Spell-target (`SpellTargetTriggerAnyTarget`) | —                  | ✅ unless filter present | ✅ via `TargetFilter` only | ❌ | ✅ (via `PredicateEvaluationService.matchesFilters`) | ❌ | ❌ |
| Life-gain (`LifeGainTriggerAnyTarget`) | —                  | ✅ all players | ✅ creatures only | ❌ | ❌ | ❌ | ❌ |
| Enters-from-graveyard (`EntersFromGraveyardTriggerTarget`) | — | ✅ all players | ✅ creatures + planeswalkers (any target) | ❌ | ❌ | ❌ | ❌ |
| Enters (`EntersTriggerTarget`)      | `Options.ATTACK`   | ✅ | ✅ any permanent  | ✅ | ✅ | ✅ | ✅ |
| Explore (`ExploreTriggerTarget`)    | —                  | ❌            | ✅ hard-coded to opponent creatures | n/a (hard-coded) | ❌ | ❌ | ❌ |
| Emblem (`EmblemTriggerTarget`)      | —                  | ❌            | ✅ any permanent  | via bespoke `opponentControlledOnly` boolean | ❌ | ❌ | ❌ |
| Saga chapter (`SagaChapterTarget`)  | —                  | "up to one" skip via self-target | ✅ creatures only | ❌ | ✅ (via chapter-level `Set<TargetFilter>`) | ❌ | ✅ first effect's `targetPredicate()` |

Legend: ✅ = supported, ❌ = not supported, — = no shared Options entry.

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
| `ON_DEATH` | `DeathTriggerCollectorService.handleDeathDefault` + `handleDeathMayEffect` + `handleLosesLifeEqualToPower` | Death |
| `ON_EQUIPPED_CREATURE_DIES` | `DeathTriggerCollectorService.handleEquippedCreatureDeathDefault` | Death |
| `ON_ALLY_CREATURE_DIES` (targeting variants) | `TriggerCollectionService.checkAllyCreatureDeathTriggers` | Death |
| `ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD` (targeting branches) | `DeathTriggerCollectorService.addEnchantedPermanentDeathEntry` | Death |
| `ON_ATTACK` (attached-permanent flavour) | `CombatTriggerService` aura/equipment flow | Attack |
| `ON_ATTACK` / `ON_ALLY_CREATURE_ATTACKS` | `CombatAttackService.declareAttackers` (per-attacker mandatory triggers store the triggering attacker as a non-targeting `targetId`, and the attacked player/planeswalker as `attackedTargetId` — so effects can act on "that creature", e.g. Shared Animosity's boost) | Attack |
| `ON_BLOCK` (targeting variant only) | `CombatBlockService.declareBlockers` queues an `AttackTriggerTarget` when the blocker's **card carries a target filter** and a block effect `canTargetPermanent()` (e.g. Elite Javelineer's "deals 1 damage to target attacking creature"); honours the card's `PermanentPredicateTargetFilter`. Block triggers with **no** card-level target filter (Ashmouth Hound, Inferno Elemental — "that creature") still push a non-targeting stack entry referencing the blocked attacker. | Attack |
| `ON_ALLY_CREATURE_ATTACKS_UNBLOCKED` | `CombatBlockService` (declare-blockers step; unblocked creature stored as non-targeting `sourcePermanentId`) | Non-targeting |
| `ON_CREATURE_ATTACKS_YOU` | `CombatAttackService.declareAttackers` (defender's permanents; attacking creature stored as non-targeting `targetId`) | Attack |
| `ON_ANY_CREATURE_ATTACKS` | `CombatAttackService.declareAttackers` (all battlefields, any controller; attacking creature stored as non-targeting `targetId`) | Non-targeting (Caltrops) |
| `ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers` (all battlefields; targeted creature stored as non-targeting `targetId`) | Becomes-target |
| `UPKEEP_TRIGGERED` (any-target effects) | `StepTriggerService.handleUpkeepTriggers` → `UpkeepAnyTargetTrigger` (queued when an effect is `canTargetPlayer() && canTargetPermanent()`, e.g. Form of the Dragon's 5-damage) | End step (reuses `TriggerTargetCollector.Options.END_STEP` for the target list) |
| `UPKEEP_TRIGGERED` (permanent-target effects) | `StepTriggerService.handleUpkeepTriggers` → `UpkeepPermanentTargetTrigger` (queued when a non–any-target, non–player-target effect is `canTargetPermanent()`, e.g. Weed-Pruner Poplar's "target creature other than this creature gets -1/-1"). Honours the card's `PermanentPredicateTargetFilter`; use `PermanentNotPredicate(PermanentIsSourceCardPredicate)` for "other than this creature". | End step (reuses `TriggerTargetCollector.Options.END_STEP` for the target list) |
| `END_STEP_TRIGGERED` | `StepTriggerService.handleEndOfTurnTriggers` (non-kicked / morbid / default) | End step |
| `CONTROLLER_END_STEP_TRIGGERED` | `StepTriggerService.handleEndOfTurnTriggers` (raid / default) | End step |
| `ON_SELF_LEAVES_BATTLEFIELD` (targeting effects only) | `DeathTriggerCollectorService.handleSelfLeavesDefault` → `SelfLeavesTriggerTarget` (queued when an effect is `canTargetPlayer()`/`canTargetPermanent()`, e.g. Meadowboon, or `canTargetGraveyard()`, e.g. Offalsnout). `TriggeredAbilityQueueService.processNextSelfLeavesTriggerTarget` routes graveyard-targeting effects (`ExileGraveyardCardsEffect(TARGET_CARDS_ANY_GRAVEYARD)`) to a `MultiGraveyardChoice` card choice instead of the permanent/player path. | End step (reuses `TriggerTargetCollector.Options.END_STEP`); non-targeting effects push straight to the stack |
| `ON_SELF_DISCARDED_BY_OPPONENT` | `TriggerCollectionService.checkDiscardSelfTriggers` | Discard-self |
| `ON_BECOMES_TARGET_OF_SPELL` / `…_OR_ABILITY` / `…_OF_OPPONENT_SPELL` | `TriggerCollectionService.checkBecomesTargetOfSpell*` | Spell-target |
| `ON_CONTROLLER_CASTS_SPELL` / `ON_ANY_PLAYER_CASTS_SPELL` (targeting variants) | `SpellCastTriggerCollectorService` | Spell-target |
| `ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU` (targeting branch) | `DamageTriggerCollectorService` | Spell-target |
| `ON_CONTROLLER_GAINS_LIFE` | `MiscTriggerCollectorService` | Life-gain |
| `ON_CREATURE_ENTERS_FROM_GRAVEYARD` | `TriggerCollectionService.checkEntersFromGraveyardTriggers` | Enters-from-graveyard (any target) |
| `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD` / `ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD` / `ON_OPPONENT_LAND_ENTERS_BATTLEFIELD` / `ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD` (permanent-targeting effects only) | `EnterTriggerCollectorService.handleEnterDefault` → `EntersTriggerTarget` (queued when `effect.canTargetPermanent()`, e.g. Reaper King's "destroy target permanent"). Player-targeting effects still push straight to the stack with the pre-set `defaultTargetPlayerId`. | Enters (reuses `TriggerTargetCollector.Options.ATTACK` for the target list — any permanent, honours the card's `PermanentPredicateTargetFilter` / `ControlledPermanentPredicateTargetFilter`) |
| `ON_ALLY_CREATURE_EXPLORES` | `TriggerCollectionService.checkExploreTriggers` | Explore |
| `ON_CONTROLLER_CLASHES` | `TriggerCollectionService.fireClashTriggers` | Clash — targeting triggers via `ClashTriggerTarget` (opponent-creature only); non-targeting triggers pushed straight to the stack |
| `ON_CHAMPIONED` | `PermanentChoiceBattlefieldHandlerService.handleChampionCreature` | Player/permanent target via `ChampionedTriggerTarget` (collected with `Options.END_STEP`; Mistbind Clique taps target player's lands) |
| Planeswalker ultimate emblems | `DrawService` / `TriggerCollectionService` | Emblem |
| `SAGA_CHAPTER_I` / `SAGA_CHAPTER_II` / `SAGA_CHAPTER_III` | `StepTriggerService.processSagaChapters` / `StackResolutionService` | Saga chapter |

Slots that currently **only ever push non-targeting entries** (no pending queue):
`ON_TAP`, `STATIC`, `ON_SACRIFICE`, `ON_BLOCK` (only the non-targeting "that creature" flavour; the targeting variant is routed through the Attack pipeline — see the mapping table above),
`GRAVEYARD_UPKEEP_TRIGGERED`, `EACH_UPKEEP_TRIGGERED`, `OPPONENT_UPKEEP_TRIGGERED`,
`ON_DAMAGED_CREATURE_DIES`, `ON_ANY_CREATURE_DIES`,
`ON_ALLY_NONTOKEN_CREATURE_DIES`, `ON_ANY_NONTOKEN_CREATURE_DIES`, `ON_OPPONENT_CREATURE_DIES`,
`ON_COMBAT_DAMAGE_TO_PLAYER`, `ON_COMBAT_DAMAGE_TO_CREATURE`, `ON_DAMAGE_TO_PLAYER`,
`ON_DEALT_DAMAGE`, `ON_BECOMES_BLOCKED`, `DRAW_TRIGGERED`, `EACH_DRAW_TRIGGERED`,
`ON_CONTROLLER_DRAWS`, `ON_OPPONENT_DRAWS`, `ON_OPPONENT_DISCARDS`,
`ON_ANY_PLAYER_TAPS_LAND`, `ON_ALLY_PERMANENT_BECOMES_TAPPED`, `ON_OPPONENT_PERMANENT_BECOMES_TAPPED`,
`ON_ALLY_PERMANENT_SACRIFICED`, `ON_ALLY_CREATURES_ATTACK`,
`ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`,
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
`GraveyardService.addCardToGraveyard`, the single zone→graveyard choke point),
`ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE` (Compost; fires on every permanent controlled by
an opponent of the graveyard owner whenever a black card enters that graveyard from any zone — checked in
`GraveyardService.addCardToGraveyard`), `ON_ENCHANTED_PERMANENT_TAPPED`,
`ON_ALLY_PERMANENT_BECOMES_TAPPED`,
`ON_SELF_BECOMES_UNTAPPED` (Hollowsage; fires when the permanent transitions tapped→untapped, from
the untap step or any untap effect, via `TriggerCollectionService.checkBecomesUntappedTriggers` — driven
from `UntapStepService` and `TapUntapSupport.untapPermanent`. Non-targeting: a "you may have target player
…" is expressed as a `MayEffect`-wrapped targeting effect whose "may" and target are resolved on the stack
via the pending-may-ability flow, not a target-choice pipeline),
`ON_ALLY_PERMANENT_BECOMES_UNTAPPED` (Wake Thrasher; the untapped-ally counterpart of
`ON_ALLY_PERMANENT_BECOMES_TAPPED` — fires once per untapped permanent on every permanent with the slot on
the untapped permanent's controller's battlefield, including the source untapping itself; same
`checkBecomesUntappedTriggers` call sites. Wrap in `TriggeringPermanentConditionalEffect` to filter by the
untapped permanent),
`ON_ENCHANTED_CREATURE_DEALT_DAMAGE`,
`ON_OPPONENT_LAND_ENTERS_BATTLEFIELD`, `ON_ALLY_LAND_ENTERS_BATTLEFIELD`,
`ON_OPENING_HAND_REVEAL`, `ON_OPPONENT_LOSES_LIFE`, `ON_OPPONENT_SHUFFLES_LIBRARY`,
`ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED`, `ENCHANTED_PLAYER_UPKEEP_TRIGGERED`,
`ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD`, `ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD`,
`ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE`, `ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER`,
`ON_OPPONENT_CREATURE_CARD_MILLED`, `ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD`,
`ON_SELF_MILLED`, `STATE_TRIGGERED`, `BEGINNING_OF_COMBAT_TRIGGERED`,
`ON_OPPONENT_CREATURE_DEALT_DAMAGE`, `GRAVEYARD_ON_CONTROLLER_CASTS_SPELL`,
`ON_CONTROLLER_LOSES_LIFE`,
`ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT`,
`ON_MINUS_ONE_MINUS_ONE_COUNTER_PUT_ON_CREATURE` (Flourishing Defenses; global watcher — fires on
every permanent with this slot, under that permanent's controller, once per individual -1/-1 counter put
on any creature from any source, via `PermanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers`;
non-targeting — a "you may create …" is a `MayEffect` resolved on the stack),
`ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`,
`GRAVEYARD_ON_ALLY_CREATURES_ATTACK`, `GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER`,
`ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY`,
`ON_TRANSFORM_TO_BACK_FACE`, `ON_TRANSFORM_TO_FRONT_FACE`,
`ON_CONTROLLER_ACTIVATES_ABILITY` (Ceaseless Searblades; fires on every permanent with this slot on
the activating player's battlefield, once per activated-ability activation incl. mana abilities;
wrap in `TriggeringPermanentConditionalEffect` to filter by the permanent whose ability was activated).

## `ON_ENTER_BATTLEFIELD` targeted triggers

A targeted ETB (the card has a `TargetFilter` and a mandatory `ON_ENTER_BATTLEFIELD` effect, e.g.
Pierce Strider, Geralf's Messenger) normally has its target chosen **at cast time** and the trigger
is pushed directly onto the stack with that target. When the permanent enters **without a cast** —
a token copy, or a creature put onto the battlefield from a graveyard via undying / reanimation /
flicker — there is no cast-time target, so `BattlefieldEntryService.processCreatureETBEffects`
routes the trigger through `ETBTokenTargetTrigger` (single target) or
`ETBTokenMultiTargetTrigger` (multi-target), letting the controller choose the target as the
ability is put on the stack (CR 603.3b / 603.6c). The entering permanent's
`getEnteredFromGraveyardOwnerId()` distinguishes a graveyard return from a cast; "up to N" cast
spells that chose 0 targets are unaffected because they passed through cast-time selection.

**Gate-conditional targeted ETBs** (`ConditionalEffect` whose condition returns
`Condition.isEtbTriggerGate()` — Metalcraft, Morbid, Raid, ControlsAnotherPermanent; e.g. Bleak
Coven Vampires, Morkrut Banshee, Storm Fleet Pyromancer, Dreamcaller Siren) **never** target at
cast time: whether the ability triggers at all depends on game state as the permanent enters
(intervening-if, CR 603.4), so `EffectResolution.computeAllowedTargets` excludes them from the
spell's cast-time target requirement and `EtbEffectResolver` drops the trigger entirely when the
gate isn't met. When it is met, the same `ETBTokenTargetTrigger` / `ETBTokenMultiTargetTrigger`
deferred path prompts for the target as the trigger goes on the stack (CR 603.3d); the wrapped
`ConditionalEffect` stays on the stack entry and the gate is re-checked at resolution. When adding
a new intervening-if condition that gates a **targeted** ETB, override `isEtbTriggerGate()` on the
condition — both the cast-time exclusion and the `EtbEffectResolver` gate key off it.

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

When used with an effect that also sets `canTargetPermanent()` to `true`, the opponent-only restriction
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

- **"My ON_DEATH trigger targets non-creatures."** Give the card an explicit
  `PermanentPredicateTargetFilter` — its predicate then governs and the death pipeline's default
  `creaturesOnly` narrowing is skipped (e.g. Fire Snake targeting a land). Without such a filter,
  death targets are creatures only.
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

## Aura trigger slot selection

Auras have their own trigger slots. Use this table to pick the correct one based on the oracle text:

| Oracle text pattern | Trigger slot | Fires when | Example |
|---|---|---|---|
| "At the beginning of your upkeep, ..." | `UPKEEP_TRIGGERED` | Aura controller's upkeep (aura is on their battlefield) | Call to the Kindred |
| "At the beginning of enchanted creature's controller's upkeep, ..." | `ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED` | Enchanted creature's controller is the active player | Necrotic Plague, Soul Bleed, Numbing Dose |
| "At the beginning of enchanted player's upkeep, ..." | `ENCHANTED_PLAYER_UPKEEP_TRIGGERED` | Enchanted player is the active player (curses) | Curse of Oblivion, Curse of the Bloody Tome |
| "At the beginning of each upkeep, ..." | `EACH_UPKEEP_TRIGGERED` | Every player's upkeep | — |
| "When enchanted creature dies, ..." | `ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD` | Enchanted creature goes to graveyard | Necrotic Plague (return effect) |
| "Whenever enchanted creature is dealt damage, ..." | `ON_ENCHANTED_CREATURE_DEALT_DAMAGE` | Enchanted creature is dealt damage (combat or non-combat) | Spiteful Shadows |
| "Whenever a creature is dealt damage, ..." (any creature) | `ON_ANY_CREATURE_DEALT_DAMAGE` | Any creature is dealt damage (combat or non-combat). Queued entry targets the damaged creature | Death Pits of Rath |

**Key distinction**: "your upkeep" on an aura means the **aura controller's** upkeep → use `UPKEEP_TRIGGERED`. "Enchanted creature's controller's upkeep" means the **enchanted permanent's controller's** upkeep → use `ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED`. These are different when the aura enchants an opponent's creature.

**How `UPKEEP_TRIGGERED` works for auras**: The aura permanent sits on the controller's battlefield. `StepTriggerService` iterates the active player's battlefield looking for permanents with `UPKEEP_TRIGGERED` effects. Since the aura is on the controller's battlefield, the trigger fires during the controller's upkeep. The `sourcePermanentId` on the stack entry is set to the aura permanent's ID (`perm.getId()`), so the resolution handler can find the enchanted creature via `auraPerm.getAttachedTo()`.

**How `ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED` works**: `StepTriggerService` iterates ALL permanents on ALL battlefields, checks if each has this effect slot and is attached, then finds the enchanted permanent's controller. It only fires when that controller is the active player.
