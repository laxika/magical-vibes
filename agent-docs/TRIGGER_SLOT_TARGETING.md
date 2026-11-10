# Trigger Slot Targeting Reference

`Triggers.plusOneCounterPutOnThis()` ("whenever a +1/+1 counter is put on this") triggers for each individual counter, such as Bloodcrazed Hoplite; the counter collector reads that from the trigger. The counter-placement collector queues a separate ability and mandatory target choice for each counter actually placed; all targets are chosen before those abilities resolve. `Triggers.plusOneCountersPutOnThis()` ("one or more") still fires once per placement event.

`Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL)` triggers for each token entering, such as Junk Winder's "whenever a token you control enters". The token-enter collector queues one dispatch per entering token, each seeing only that token. The default mode still fires once per token-creation event.

`MultiTargetConstraint.ONE_PER_OPPONENT` requires one legal permanent target controlled by every opponent, even when an opponent has no legal targets. Spells using it cannot be cast without satisfying every opponent's target requirement. `ONE_PER_CONTROLLER_IF_ABLE` retains its optional omission for opponents without legal targets. `ONE_PER_OPPONENT_IF_ABLE` is the same optional omission applied to opponents only, with the controller never required (Sylvan Primordial, "for each opponent").

`Triggers.youCastSpell()` graveyard targets restricted by the triggering spell ("target card with the same name as that spell", Harness the Storm's `CardSharesNameWithTriggeringSpellPredicate` in the effect's filter) are chosen through the generic `SpellGraveyardTargetTrigger`, which carries the triggering spell (`withTriggeringSpell`) so the choice binds its name; the graveyard target operation and the entry keep it for the check as the ability resolves.

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
     works the way the death/attack/end-step pipelines do. The two that enumerate an unfiltered
     "any target" — `DiscardTriggerAnyTarget` and the filterless branch of `SpellTargetTriggerAnyTarget` — share
     `TriggeredAbilityQueueService.anyTargetPermanents`, which evaluates the declared
     `TargetPredicates.anyTarget()` rather than re-implementing it, exactly as the collector does.
     `LifeGainTriggerAnyTarget`'s filterless branch offers the permanents its effect's declared target
     admits ("target creature or player", `DealDamageToTargetCreatureOrPlayerEffect`: creatures only;
     an any-target effect: creatures, planeswalkers and battles; a player-only target: none); with the
     ability's target filter ("target opponent": Sanguine Bond's `target(PlayerPredicateTargetFilter(
     opponent))`) it offers what the filter admits, and the stack entry keeps the filter for a copy's new
     target. The "you gain life" collector's default handler queues it for any targeted effect that no
     handler of its own collects; `DrawTriggerAnyTarget` is creature-only.

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

The pipelines above are for TRIGGERED abilities. A **spell** (its spell ability) or **activated ability**
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
| Life-gain (`LifeGainTriggerAnyTarget`) | `Options.ATTACK` with the ability's target filter, else — | ✅ all players, or the filter's | ✅ the effect's declared permanents, or the filter's | ✅ with the filter | ✅ with the filter | ✅ with the filter | ❌ |
| Enters (`EntersTriggerTarget`)      | `Options.ATTACK`   | ✅ | ✅ any permanent  | ✅ | ✅ | ✅ | ✅ |
| Explore (`ExploreTriggerTarget`)    | —                  | ❌            | ✅ hard-coded to opponent creatures | n/a (hard-coded) | ❌ | ❌ | ❌ |
| Emblem (`EmblemTriggerTarget`)      | `Options.UPKEEP`   | ❌            | ✅ any permanent  | ❌ | ✅ the ability's own (`TriggeredAbility.withTargetFilter`, kept on the stack entry; Teferi, Hero of Dominaria) | ✅ the ability's own | ❌ |
| Saga chapter (`SagaChapterTarget`)  | —                  | "up to one" skip via self-target | ✅ creatures only | ❌ | ✅ (via chapter-level `Set<TargetFilter>`) | ❌ | ✅ first effect's spec predicate (`EffectResolution.targetPredicateOf`) |

Legend: ✅ = supported, ❌ = not supported, — = no shared Options entry.

`Triggers.enters(null, ObjectRelation.ANY)` effects with a permanent target use `EntersTriggerTarget` and choose the target as the trigger is put on the stack. The entering permanent is preserved as the first exchange participant and as `FilterContext.sourcePermanentSnapshot`; its controller is the player who chooses the target, even when that player differs from the source permanent's controller.

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
| `Triggers.dies(THIS)` | `DeathTriggerCollectorService.handleDeathDefault` + `handleDeathMayEffect` + `handleDeathMayPayMana` + `handleLosesLifeEqualToPower` | Death |
| `Triggers.dies(HAUNTED)` | The trigger pipeline matches the cards in exile that haunted the departed permanent (`ZoneChangeEvent.hauntedBy`); `TriggerCollectionService.triggerCreatureDeath` (`CreatureDeathWatch.HAUNTED`) then uses the death-trigger target queue for targeted effects | Death |
| `Triggers.creatureDies(EQUIPPED)` | `DeathTriggerCollectorService.handleEquippedCreatureDeathDefault` | Death |
| `Triggers.equippedCreatureDealsCombatDamage()` | `DamageTriggerCollectorService.handleEquippedCreatureDealsCombatDamage` (scans attached Equipment and preserves last-known attachment) | Combat damage |
| `Triggers.enchantedPlayerDealtDamage()` | `TriggerCollectionService.checkEnchantedPlayerDealtDamageTriggers` + `DamageTriggerCollectorService.handleEnchantedPlayerDealtDamage` | Damage (non-targeting) |
| `Triggers.transforms(null, ObjectRelation.EQUIPPED)` | `AnimationSupport.fireEquipmentTransformTriggers` (non-targeting; pushed with the Equipment as `sourcePermanentId`) | Transform |
| `ON_EQUIPMENT_ATTACHED_TO_CREATURE` | `TriggerCollectionService.checkEquipmentAttachedTriggers` (non-targeting; called by `EquipSupport.attachEquipment` and for permanents entering already attached) | Attachment |
| `Triggers.transforms(null, ObjectRelation.YOU_CONTROL)` | `AnimationSupport.fireAllyPermanentTransformTriggers` → `TriggerCollectionService.checkAllyPermanentTransformsTriggers` (non-targeting; the trigger's filter is judged on the transformed face) | Transform |
| `Triggers.creatureDies(YOU_CONTROL)` (targeting variants) | `TriggerCollectionService.triggerCreatureDeath` (`CreatureDeathWatch.ALLY`) | Death |
| `Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY)` (targeting variants) | `DeathTriggerCollectorService.handleArtifactGraveyardControllerConditional` / `handleArtifactGraveyardReturnUnlessDamage` | Spell target; the latter carries the exact graveyard card id and uses the source permanent snapshot |
| `Triggers.postcombatMainPhase(ActivePlayer.YOU)` (graveyard-targeting variants) | `StepTriggerService.handlePostcombatMainTriggers` | Spell graveyard target |
| `Triggers.creatureDies(YOU_CONTROL)` (targeting variants) | `TriggerCollectionService.triggerCreatureDeath` (`CreatureDeathWatch.ALLY`; non-may effects are batched; the stack entry carries the dying permanent as it last existed on the battlefield (CR 603.10a), which effects read for its counters as they resolve) | Death |
| `Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY)` (targeting wrapper) | `DeathTriggerCollectorService.handleArtifactGraveyardControllerConditional` | Spell target |
| `Triggers.dies(ObjectRelation.ENCHANTED)` (targeting branches) | `DeathTriggerCollectorService.addEnchantedPermanentDeathEntry` | Death |
| `Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL)` | `TriggerCollectionService.checkAllyLandEntersTriggers` (targeted effects use `SpellTargetTriggerAnyTarget`; other effects go directly to the stack) | Spell target |
| `Triggers.enters(Triggers.LAND, ObjectRelation.ENCHANTED_PLAYER_CONTROLS)` | `TriggerCollectionService.checkEnchantedPlayerLandEntersTriggers` scans player-enchanting Curses attached to the entering land's controller | Non-targeting |
| `Triggers.nontokenCreatureDies(ENCHANTED_PLAYER_CONTROLS)` | The trigger pipeline matches player-enchanting Curses attached to the dying creature's controller; `TriggerCollectionService.triggerCreatureDeath` (`CreatureDeathWatch.ENCHANTED_PLAYER_NONTOKEN`) | Non-targeting |
| `Triggers.attacks()` (attached-permanent flavour) | `CombatTriggerService` aura/equipment flow | Attack |
| `Triggers.youAttack()` (targeting variants) | `CombatAttackService.declareAttackers` queues `AttackTriggerTarget`; non-targeting variants keep the direct stack-entry path. The target collector sees the attacking battlefield state, so restrictions such as "target attacking Goblin you control" are enforced | Attack |
| `Triggers.youAttack() from the command zone` | `CombatAttackService.declareAttackers` scans face-up cards in the attacking player's command zone and queues `AttackTriggerTarget` with `sourcePermanentId = null`; targeted effects reuse `TriggerTargetCollector.Options.ATTACK` and the card's target filter | Attack |
| `Triggers.attacks()` / `Triggers.creatureYouControlAttacks()` | `CombatAttackService.declareAttackers` (per-attacker mandatory triggers store the triggering attacker as a non-targeting `targetId`, and the attacked player/planeswalker as `attackedTargetId` — so effects can act on "that creature", e.g. Shared Animosity's boost). Single permanent/player targets use `AttackTriggerTarget`; several target groups, or one optional or dynamic group (`needsSlotBySlotTargetSelection`), reuse `ETBTokenMultiTargetTrigger` (Archon of the Triumvirate); a single static "up to N" group (Armory Automaton's "any number of target Equipment") takes the multi-permanent `AttackTriggerTargets` choice, all its targets chosen in one answer. Routing reads the target groups only, never an effect's class | Attack |
| `Triggers.attacksUnblocked()` (graveyard-targeting) | `CombatBlockService.collectUnblockedAttackTriggers` routes effects implementing `GraveyardCardChoosingEffect` (Rysorian Badger's `ExileCardsFromGraveyardEffect`) to `GraveyardTargetingService.handleUnblockedAttackGraveyardChoiceTargeting` — an up-to-N multi-select over the defending player's graveyard, filtered by `graveyardChoiceFilter()`, as the trigger goes on the stack. The attacker rides along as `sourcePermanentId` (for "if you do, it assigns no combat damage"); no matching cards ⇒ the trigger is still pushed with 0 targets | Declare blockers |
| `Triggers.dealsCombatDamageToPlayer()` (targeting variants) | `CombatDamageService` routes effects whose `targetSpec()` includes permanents or players to the shared `AttackTriggerTarget` pipeline; triggers with multiple target groups or slot-by-slot optional targets use `ETBTokenMultiTargetTrigger`. Effects carrying baked combat context remain non-targeting, and graveyard-targeting effects still use `GraveyardTargetingService.handleCombatDamageGraveyardChoiceTargeting` (Skullsnatcher). The target-group filter is taken from the effect's `target(...)` declaration | Combat damage |
| `Triggers.creatureYouControlDealsCombatDamageToPlayer()` (targeting variants) | `CombatDamageService` routes the effect (read through `CombatAbilityReader.read`, which takes the trigger's filter and "one or more" from the ability) through the same `AttackTriggerTarget` pipeline; the effect's target-group binding is kept, and `attackedTargetId` supplies the damaged player to defending-player predicates | Combat damage |
| "Target [permanent] that player controls" on any damage-to-a-player trigger | Every collector that queues an `AttackTriggerTarget` for a damage-to-a-player event gives it the player dealt damage (`withTriggeringPlayer`), which `PermanentControlledByTriggeringPlayerPredicate` reads through `FilterContext.triggeringPlayerId` as the target is chosen; the stack entry keeps it (`StackEntry.triggeringPlayerId`) for the resolution recheck. A reflexive "when you do" target (Dokuchi Silencer) is chosen against the triggering player its discard follow-up carries (`DiscardFollowUp.withTriggeringPlayer`, `MayAbilityTriggerTarget.withTriggeringPlayer`); a noncombat damage collector that queues a `SpellTargetTriggerAnyTarget` gives it the opponent dealt damage (`withTriggeringPlayer`, Chandra's Incinerator). No collector binds the player into an effect | Damage |
| `Triggers.attacksUnblocked()` (permanent-targeting may) | Same collector routes `MayEffect`s whose `targetSpec()` includes permanents (Dwarven Vigilantes) via `queueMayAbility(..., null, attackerId)` — CR 603.5 may at resolution, then creature target; non-targeting mays / mandatory effects still bake the defending player as `targetId` | Declare blockers |
| `Triggers.attacksUnblocked()` (multi-target) | A card with more than one `target(...)` group (Goblin Grenadiers' "destroy target creature and target land") is routed by `collectUnblockedAttackTriggers` to `PermanentChoiceContext.ETBTokenMultiTargetTrigger` before the may/plain branches — the single-target pipelines can't collect two targets. Targets are picked slot-by-slot as the trigger goes on the stack; a wrapping `MayEffect` still prompts at resolution and `MayAbilityHandlerService` leaves the entry's `targetIds` alone (`targetAlreadySet`) | Declare blockers |
| `Triggers.becomesBlocked()` ("becomes blocked by a creature") | Printed or granted abilities with `TriggerOptions.EACH_BLOCKER`, or a filter on the blocker, are collected once for each declared blocker; the entry records that blocker as its triggering permanent and non-targeting target ("that creature"). They are omitted when an effect makes a creature blocked without a blocker. On a block or "becomes blocked by" ability a permanent target the card declares (`target(...)`) is chosen; one it doesn't declare is that creature. | Declare blockers |
| `Triggers.attacks()` (graveyard-targeting) | `CombatAttackService.declareAttackers` routes effects whose `targetSpec().admits(Kind.GRAVEYARD_CARD)` (e.g. Graven Abomination's `ExileGraveyardCardsEffect(TARGET_CARDS_OPPONENT_GRAVEYARD)`) to `GraveyardTargetingService.handleAttackGraveyardTargeting` — chooses from the defending player's graveyard (from the attacker's `attackTarget`) as the trigger goes on the stack. Dynamic mana-value caps on return effects are evaluated here for target selection and re-evaluated by the return handler at resolution. No legal target ⇒ trigger skipped (CR 603.3c). Exception: a `CastTargetInstantOrSorceryFromGraveyardEffect` (The Dawning Archaic) routes to `handleAttackGraveyardCastTargeting` instead, which picks the graveyards from the effect's own `GraveyardSearchScope` | Attack |
| `Triggers.attacks()` (two-target counter move) | `CombatAttackService.declareAttackers` routes any trigger whose effect implements the marker `AttackCounterMoveEffect` (Decimator Beetle's `RemoveAndPutCounterOnAttackEffect`) to the bespoke two-step `AttackCounterMoveFirstTarget` → `AttackCounterMoveSecondTarget` flow, because the normal Attack pipeline collects only ONE target. Stage 1 = a creature you control; stage 2 = up to one creature the defending player controls (choose yourself to decline). Drained in `AutoPassService`; both stages filter targetability via `TargetLegalityService.checkSpellPermanentTargetableReason`; the two chosen ids land on the entry's flat `targetIds` (0, 1) | bespoke |
| `Triggers.dealtDamage()` (targeting effects) | `DamageTriggerCollectorService` routes targeted effects through `SpellTargetTriggerAnyTarget` with the card's own `TargetFilter`; mandatory targets are chosen as the trigger goes on the stack, while targeted `MayEffect`s preserve the separate resolution-time may choice. The damage amount is carried as the triggered entry's X payload. Non-targeting effects keep the plain entry from `handleDealtDamageDefault` (damaged permanent as `targetId`, damage amount on `eventValue`) | Spell target |
| `Triggers.dealtNoncombatDamage()` | `DamageSupport` calls `TriggerCollectionService.checkNoncombatDamageToSelfTriggers` after noncombat damage to a permanent; the shared damage collector queues the effect with the damage amount on `eventValue`. Combat damage is collected separately and never checks this slot | Damage (non-targeting by default) |
| `Triggers.blocks()` (targeting variant only) | `CombatBlockService.declareBlockers` queues an `AttackTriggerTarget` when the blocker's **card carries a target filter** and a block effect's `targetSpec()` includes permanents (e.g. Elite Javelineer's "deals 1 damage to target attacking creature"); honours the card's `PermanentPredicateTargetFilter`. The trigger's filter is judged on the blocked attacker (`Triggers.blocks().withFilter(...)`, Arrogant Bloodlord). Block triggers with **no** card-level target filter (Ashmouth Hound, Inferno Elemental - "that creature") still push a non-targeting stack entry referencing the blocked attacker. | Attack |
| `Triggers.blocks()` (targeting variant only) | `CombatBlockService.declareBlockers` queues an `AttackTriggerTarget` when the blocker's **card carries a target filter** and a block effect's `targetSpec()` includes permanents (e.g. Elite Javelineer's "deals 1 damage to target attacking creature"); honours the card's `PermanentPredicateTargetFilter`. Block triggers with **no** card-level target filter (Ashmouth Hound, Inferno Elemental — "that creature") still push a non-targeting stack entry referencing the blocked attacker. | Attack |
| `Triggers.creatureYouControlBecomesBlocked()` (targeting variants) | `CombatBlockService.checkAllyBecomesBlockedTriggers` queues effects whose `targetSpec()` includes permanents or players through the shared `AttackTriggerTarget` pipeline; the watching permanent is the source for every entry, and the blocked creature the entry's (and the choice's) triggering permanent. | Declare blockers |
| `Triggers.creatureYouControlAttacksUnblocked()` | `CombatBlockService` (declare-blockers step; unblocked creature stored as non-targeting `sourcePermanentId`) | Non-targeting |
| `Triggers.creaturesAttackYouUnblocked()` | `CombatBlockService` (declare-blockers step; fires once when at least one direct attacker is unblocked, attacking player stored as non-targeting `targetId`) | Non-targeting |
| `Triggers.creaturesAttackYouUnblocked()` | `CombatBlockService` (declare-blockers step; fires once per defending permanent when an opponent's creature attacks the player directly and at least one such attacker is unblocked; attacking player stored as non-targeting `targetId`) | Declare blockers |
| `Triggers.creatureAttacksYouOrYourPlaneswalker()` | `CombatAttackService.declareAttackers` (defender's permanents; attacking creature stored as non-targeting `targetId`) | Attack |
| `Triggers.creatureAttacksYou()` | `CombatAttackService.declareAttackers` (directly attacked player's permanents; attacking creature stored as non-targeting `targetId`) | Attack |
| `Triggers.enlists()` | `CombatAttackService.collectEnlistAbilities` (the enlisting attacker's abilities, as its enlist cost is paid; the enlisted creature is the entry's triggering permanent) | none (no card targets) |
| `Triggers.creatureAttacksWhileYouDefend()` | `CombatAttackService.declareAttackers` (defending player's permanents; fires for attacks on that player, their planeswalkers, and battles they protect; battles fire only this slot, not `Triggers.creatureAttacksYouOrYourPlaneswalker()`; attacking creature stored as non-targeting `targetId`) | Attack |
| `Triggers.playerAttacks()` | `CombatAttackService.declareAttackers` (all battlefields, any attacking player; attacking player stored as non-targeting `targetId`) | Non-targeting (Total War) |
| `Triggers.playerAttacksOneOfYourOpponents()` / `Triggers.opponentAttacksAnotherOpponent()` | `CombatAttackService.checkPlayerAttacksYourOpponentTriggers` (once for each of the source controller's opponents attacked; that opponent the attacked target, the attacking player the non-targeting `targetId`) | Non-targeting (Ellie, Brick Master; Karazikar) |
| `Triggers.creatureAttacks()` from the graveyard | `CombatAttackService.checkGraveyardCreatureAttacksTriggers` (each graveyard, once for each attacking creature matching the filter, judged from the card and its owner: "whenever your commander attacks") | Non-targeting (Vivi's Persistence) |
| `Triggers.creatureAttacks()` | `CombatAttackService.declareAttackers` (all battlefields, any controller; attacking creature stored as non-targeting `targetId`; the trigger's filter decides which attackers trigger) | Non-targeting (Caltrops, Windreader Sphinx) |
| `Triggers.creatureYouControlAttacksUnblocked() from exile` | `CombatBlockService` (declare-blockers step; scans the controller's face-up exile cards and stores the unblocked creature as non-targeting `targetId`, its attack destination as `attackedTargetId`, and any exile source ID as `sourcePermanentId`) | Non-targeting |
| `Triggers.youAttack() from exile` | `CombatAttackService.declareAttackers` (scans the controller's face-up exile cards once after attackers are declared; attacker count is stored as `xValue`) | Non-targeting |
| `Triggers.playerAttacks()` | `CombatAttackService.declareAttackers` (all battlefields, any attacking player; attacking player stored as non-targeting `targetId`) | Non-targeting (Total War) |
| `Triggers.creatureAttacks()` | `CombatAttackService.declareAttackers` (all battlefields, any controller; attacking creature stored as non-targeting `targetId`, attack target stored as `attackedTargetId`; the trigger's filter decides which attackers trigger) | Non-targeting (Caltrops, Windreader Sphinx) |
| `Triggers.creatureOpponentControlsBecomesTargetOfYourSpellOrAbility()` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers` (spell controller's battlefield; targeted creature stored as non-targeting `targetId`, listener as `sourcePermanentId`) | Becomes-target |
| `Triggers.creatureOrCreatureSpellYouControlBecomesTargetOfOpponentSpellOrAbility()` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers` (controller's battlefield; handles both creature permanents and creature spells targeted by an opponent) | Becomes-target |
| `Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility()` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers` (controller's battlefield; targeted effects honor the card-level target filter, with opponent-relative permanent filters scoped to the specific triggering spell/ability controller) | Becomes-target |
| `Triggers.creatureYouControlBecomesTargetOfOpponentSpell()` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers` (controller's battlefield; opponent spells only, not activated or triggered abilities) | Becomes-target |
| `Triggers.creatureYouControlBecomesTargetOfSpell()` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers` (targeted creature's controller's battlefield; spell path only; the triggered ability uses the shared ETB multi-target picker so optional targets can be declined) | Becomes-target |
| `Triggers.anotherPermanentYouControlBecomesTargetOfOpponentSpellOrAbility()` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers` (one trigger per other permanent, opponent-only; targeted permanent stored as non-targeting `triggeringPermanentId`) | Becomes-target |
| `Triggers.creatureBecomesTargetOfSpellOrAbility()` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers` (all battlefields; targeted creature stored as non-targeting `targetId`; `TargetTrigger.withSource(...)` restricts it to an ability of a matching permanent (Skophos Maze-Warden: a land you control named Labyrinth of Skophos) targeting another creature) | Becomes-target |
| `Triggers.permanentYouControlBecomesTargetOfOpponentSpellOrAbility()` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers`/`checkBecomesTargetOfAbilityTriggers` (targeted permanent's controller's battlefield; only permanent targets qualify, and the opposing controller is checked before queuing) | Becomes-target |
| `Triggers.upkeep(ActivePlayer.YOU)` (any-target effects) | `StepTriggerService.handleUpkeepTriggers` → `UpkeepAnyTargetTrigger` (queued when an effect targets both — `targetSpec()` admits both `Kind.PLAYER` and `Kind.PERMANENT`, e.g. Form of the Dragon's 5-damage) | End step (reuses `TriggerTargetCollector.Options.END_STEP` for the target list) |
| `Triggers.upkeep(ActivePlayer.YOU)` (permanent-target effects) | `StepTriggerService.handleUpkeepTriggers` → `UpkeepPermanentTargetTrigger` (queued when a non–any-target, non–player-target effect's `targetSpec()` includes permanents, e.g. Weed-Pruner Poplar's "target creature other than this creature gets -1/-1"). Honours the card's `PermanentPredicateTargetFilter`; use `PermanentNotPredicate(PermanentIsSourceCardPredicate)` for "other than this creature". | End step (reuses `TriggerTargetCollector.Options.END_STEP` for the target list) |
| `Triggers.upkeep(ActivePlayer.ANY) from exile` | `StepTriggerService` scans suspended cards in exile on every player's upkeep; an active-player `MayEffect` preserves that player as the choice and effect context | Non-targeting; source card must remain suspended at resolution |
| `Triggers.upkeep(ActivePlayer.YOU) from exile` | `StepTriggerService` scans cards in exile during their owners' upkeeps while they have positive scream-counter entries | Non-targeting; the counter-removal handler rechecks the card's exile and counter state |
| `Triggers.upkeep(ActivePlayer.YOU) from exile` | `StepTriggerService` scans cards in exile during their owners' upkeeps | Non-targeting; the effect should recheck `SourceCardInExile` when resolution depends on the card remaining exiled |
| `Triggers.timeCounterRemovedFromThisInExile()` | `TriggerCollectionService.checkTimeCounterRemovedFromExiledCardTriggers` dispatches the slot when the source card loses a time counter in exile; targeted effects use the shared self-trigger target queue | Non-targeting unless an effect declares a player/permanent target; source card is the suspended exiled card; Benalish Commander (`PLC`), Roiling Horror (`PLC`) |
| `Triggers.timeCountersRemovedFromThis()` | `TriggerCollectionService.checkLoyaltyCounterRemovalTriggers` dispatches the slot after a permanent's tracked time-counter removals are drained | Non-targeting; source permanent is carried on the triggered stack entry; Regenerations Restored (`WHO`) |
| `Triggers.phasesIn(null, ObjectRelation.THIS)` (permanent-target effects) | `TriggerCollectionService.enqueuePhasingTriggers` → `PhasesInTriggerTarget` (queued during the untap-step phasing action when `targetSpec()` includes permanents, e.g. Shimmering Efreet's "target creature phases out"); drained at upkeep start via `StepTriggerService.processNextPhasesInTriggerTarget` (also AutoPass). Honours the card's `PermanentPredicateTargetFilter`. | End step (reuses `TriggerTargetCollector.Options.END_STEP`) |
| `Triggers.phasesOut(null, ObjectRelation.ANOTHER)` | `TriggerCollectionService.checkOtherPermanentPhasesOutTriggers` dispatches before the phased-out permanent leaves the battlefield; the watcher that phased out is excluded. | Non-targeting; the phased-out permanent is carried only in `TriggerContext.PermanentPhasedOut` |
| `Triggers.exiledFrom(Set.of(), Triggers.NONTOKEN, ObjectRelation.ANY)` | `GameData`'s non-token card-exile listener dispatches `TriggerCollectionService.checkAnyCardExiledTriggers` after a card enters exile, regardless of its previous zone. | Non-targeting |
| `Triggers.exiledFrom(Set.of(Zone.HAND), null, ObjectRelation.YOU_OWN)` | Hand-exile paths dispatch `TriggerCollectionService.checkControllerCardsExiledFromHandTriggers` after one or more cards leave the controller's hand for exile. | Non-targeting |
| `Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, ObjectRelation.ANOTHER).causedBy(PlayerRelation.YOU)` | `PermanentRemovalService` dispatches `TriggerCollectionService.checkControllerSpellOrAbilityExilesPermanentTriggers` after a battlefield permanent is exiled by the resolving controller's spell or ability. | Non-targeting |
| `Triggers.exiledWhileActivatingCraftAbility(ObjectRelation.THIS)` | The source's own move from the battlefield to exile caused by paying a craft cost (`ZoneChangeCause.CRAFT`, as the permanent with craft or a material), collected by the leave pipeline and deferred with the activation's cost triggers (Market Gnome). | Non-targeting |
| `Triggers.endStep(ActivePlayer.ANY)` | `StepTriggerService.handleEndOfTurnTriggers` (non-kicked / morbid / default) | End step |
| `Triggers.endStep(ActivePlayer.YOU)` | `StepTriggerService.handleEndOfTurnTriggers` (raid / default; multi-target groups reuse `ETBTokenMultiTargetTrigger`) | End step |
| `Triggers.endStep(ActivePlayer.OPPONENT)` | `StepTriggerService.handleEndStepTriggers` (fires only when the end-step player is an opponent of the permanent's controller; end-step player baked into `targetId` for the intervening-if `ConditionalEffect`, e.g. Predatory Advantage's `EndStepPlayerDidntCastSpell`) | Non-targeting |
| `Triggers.leavesBattlefield(THIS)` (targeting effects only) | `DeathTriggerCollectorService.handleSelfLeavesDefault` → `SelfTriggeredAbilityTarget` (queued when an effect's `targetSpec()` includes players/permanents, e.g. Meadowboon, or admits a graveyard card — `admits(Kind.GRAVEYARD_CARD)`, e.g. Offalsnout). `TriggeredAbilityQueueService.processNextSelfTriggeredAbilityTarget` routes graveyard-targeting effects (`ExileGraveyardCardsEffect(TARGET_CARDS_ANY_GRAVEYARD)`) to a `MultiGraveyardChoice` card choice instead of the permanent/player path. | End step (reuses `TriggerTargetCollector.Options.END_STEP`); non-targeting effects push straight to the stack |
| `Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` | `PermanentRemovalService` → `TriggerCollectionService.checkAllyCreatureExiledFromBattlefieldTriggers`; controller-scoped watcher for another creature controlled by the same player, after the card enters exile | Non-targeting effects push directly to the stack |
| `Triggers.leavesBattlefieldWithoutDying(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` | `PermanentRemovalService` → `TriggerCollectionService.checkAllyCreatureLeavesBattlefieldWithoutDyingTriggers` (and the leave-batch variant, which enqueues one trigger per other creature leaving together); per-creature counterpart of `Triggers.leavesBattlefieldWithoutDying(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` ("one or more"). Used by Imperial Cosmographer | Non-targeting effects push directly to the stack |
| `ON_CREATURE_EXILED_FROM_BATTLEFIELD` | `PermanentRemovalService` → `TriggerCollectionService.checkCreatureExiledFromBattlefieldTriggers`; global watcher for any creature exiled from the battlefield, after the card enters exile | Non-targeting effects push directly to the stack |
| `Triggers.leavesGraveyard(Triggers.ARTIFACT_OR_CREATURE, ObjectRelation.YOU_OWN)` (targeting effects only) | `MiscTriggerCollectorService.handleControllerArtifactOrCreatureCardsLeaveGraveyard` → `SelfTriggeredAbilityTarget`; intervening-if conditions are checked when the trigger event occurs, then permanent/player targets are collected as the ability is put on the stack | End step (reuses `TriggerTargetCollector.Options.END_STEP`) |
| `Triggers.leavesGraveyard(Triggers.ARTIFACT, ObjectRelation.YOU_OWN)` (targeting effects only) | `MiscTriggerCollectorService.handleControllerArtifactCardsLeaveGraveyard` → `SelfTriggeredAbilityTarget`; intervening-if conditions are checked when the trigger event occurs, then permanent/player targets are collected as the ability is put on the stack | End step (reuses `TriggerTargetCollector.Options.END_STEP`) |
| `Triggers.leavesGraveyard(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN)` | `GraveyardService.notifyCardsLeftGraveyard` → `TriggerCollectionService.checkControllerCreatureCardLeavesGraveyardTriggers` (one trigger per nontoken creature card) | Non-targeting |
| `Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)` | `GraveyardService.notifyCardsLeftGraveyard` → `TriggerCollectionService.checkControllerInstantOrSorceryCardLeavesGraveyardTriggers` (one trigger per nontoken instant or sorcery card) | Non-targeting |
| `Triggers.becomesMonstrous()` (targeting effects only) | `MonstrosityEffectHandler` → `TriggerCollectionService.checkBecomesMonstrousTriggers` → `SelfTriggeredAbilityTarget`; target selection uses the same permanent-targeting queue as other self-triggered abilities. | When monstrosity resolves |
| `Triggers.youCycleThisCard()` | `TriggerCollectionService.checkCycleTriggers` | Self-cycle (non-targeting → stack; targeted → `DiscardControllerTriggerTarget` with no source permanent) |
| `Triggers.youDiscardThisCard()` | `TriggerCollectionService.checkDiscardTriggers` | Discard-self (any cause; non-targeting → stack, any-target → `DiscardTriggerAnyTarget`) |
| `Triggers.opponentCausesYouToDiscardThisCard()` | `TriggerCollectionService.checkDiscardSelfTriggers` | Discard-self |
| `Triggers.youDiscardCard()` (targeting variants) | `DiscardTriggerCollectorService` → `DiscardControllerTriggerTarget` (queued when a controller-discard effect's `targetSpec()` includes permanents, e.g. Zenith Seeker's "target creature gains flying"). Non-targeting controller-discard effects (Hekma Sentinels self-boost, Curator of Mysteries scry, Necropotence exile) still enqueue a `TRIGGERED_ABILITY` straight onto the stack. | Controller-discard (reuses `TriggerTargetCollector.Options.ATTACK`; honours the effect's `targetSpec().predicate()`) |
| `Triggers.youScry()` | `ScryTriggerCollectorService` | Controller scry; non-targeting effects enqueue directly, while targeted effects use the standard spell-target trigger choice |
| `Triggers.opponentScries()` | `ScryTriggerCollectorService` | Opponent scry; the scrying player is supplied as the implicit player context and event-driven effects enqueue directly |
| `Triggers.youSearchYourLibrary()` | `LibrarySearchTriggerHelper` | Controller searches their own library; non-targeting effects enqueue directly |
| `Triggers.putIntoHandFrom(Zone.LIBRARY, null, ObjectRelation.YOU_OWN)` | `TriggerCollectionService.checkControllerCardPutIntoHandFromLibraryTriggers` + `MiscTriggerCollectorService` | Controller's card enters their hand from their library; non-targeting effects enqueue directly |
| `Triggers.ringTemptsYou()` | `TriggerCollectionService.checkRingTemptsYouTriggers` + `RingTemptsYouTriggerCollectorService` | Non-targeting |
| `Triggers.youBecomeMonarch()` / `Triggers.opponentBecomesMonarch()` | `TriggerCollectionService.checkBecomesMonarchTriggers` | Non-targeting; opponent becomes-monarch effects receive the new monarch as a baked-in player context |
| `Triggers.youRollOneOrMoreDice()` (targeting variants) | `DiceRollTriggerCollectorService` → `SpellTargetTriggerAnyTarget`; non-targeting effects enqueue directly | Controller rolls one or more dice |
| `Triggers.youRollDie()` | `TriggerCollectionService.checkControllerRollsHighestNaturalResultTriggers` (fired for every die, before the highest-result filter) → ordinary trigger dispatch | One trigger per die rolled; the die's result is the event value, so "whenever you roll a 4 or higher" is `ConditionalEffect(EventValueAtLeast(4), ...)` (Mr. House) |
| `Triggers.youRollHighestNaturalResult()` | `TriggerCollectionService.checkControllerRollsHighestNaturalResultTriggers` → ordinary trigger dispatch | One trigger for each non-ignored die whose natural face is that die's highest result |
| `Triggers.youInvestigateFirstTimeEachTurn()` | `InvestigateTriggerCollectorService` | The controller's first investigate event each turn; non-targeting effects enqueue directly |
| `Triggers.youInvestigate()` | `InvestigateTriggerCollectorService` | Every controller investigate event; non-targeting effects enqueue directly |
| `Triggers.youSurveil()` | `MiscTriggerCollectorService` | Controller surveils; non-targeting effects enqueue directly |
| `Triggers.opponentSurveils()` | `MiscTriggerCollectorService` | Opponent surveils; the surveiling player is supplied as the implicit player context and event-driven effects enqueue directly |
| `Triggers.youSeek()` | `SeekTriggerCollectorService` | Controller seeks one or more cards (no dispatch for a seek that found nothing); every entry records the seeking player as triggering player and the sought cards as triggering cards whatever the effects are, and non-targeting effects enqueue directly |
| `Triggers.youDiscardOneOrMoreCards()` | `TriggerCollectionService.checkDiscardEventTriggers` → `DiscardTriggerCollectorService` | One trigger for a one-or-more-card discard event; the count is carried by the trigger context and stack entry |
| `Triggers.becomesTargetOfSpell()` / `becomesTargetOfSpellOrAbility()` / `becomesTargetOfOpponentSpellOrAbility()` / `becomesTargetOfOpponentSpell()` | `TriggerCollectionService.checkBecomesTargetOfSpell*` | Spell-target. "A spell an opponent controls" (Reality Smasher's discard counter) is `becomesTargetOfOpponentSpell()`, which no ability triggers; ward's "spell or ability" is `becomesTargetOfOpponentSpellOrAbility()` |
| `Triggers.youBecomeTargetOfSpell()` | `TriggerCollectionService.checkBecomesTargetOfSpellTriggers` (targeted player; spell path only) | Spell-target |
| `Triggers.playerChoosesTargets()` | `TriggerCollectionService.checkTargetChoiceTriggers` after spell/ability target selection | Target-choice event; the chosen spell/ability is carried as non-targeting `triggeringCardId`. `Triggers.playerOrPermanentBecomesTargetOfYourAbility()` (Loki) narrows this event to an activated or triggered ability controlled by the source controller that targets a player or permanent; this collector reads it |
| `Triggers.youCastSpell()` / `Triggers.playerCastsSpell()` (targeting variants) | `SpellCastTriggerCollectorService` | Spell-target |
| `Triggers.youCopySpell()` / `Triggers.opponentCopiesSpell()` (targeting variants) | `TriggerCollectionService.checkSpellCopyTriggers` + `SpellCastTriggerCollectorService` | Spell-target when the copy ability is bound to a target group (`target(filter[, 0, 1]).addTrigger(Triggers.youCopySpell().withFilter(spellFilter), …)`; the collector reads the group's filter and an "up to one" minimum); otherwise non-targeting |
| `Triggers.youCastThisSpell()` (targeting variants) | `TriggerCollectionService.checkSpellCastTriggers` | Spell-target (single); multi-target (`maxTargets > 1`) reuses `ETBTokenMultiTargetTrigger` |
| `Triggers.permanentDealsDamageToYou()` (targeting branch) | `DamageTriggerCollectorService` | Spell-target |
| `Triggers.sourceOpponentControlsDealsDamageToYouOrYourPermanent()` | `TriggerCollectionService.checkOpponentSourceDamageToYouOrYourPermanentTriggers` + `DamageTriggerCollectorService` | Non-targeting; source permanent and controller are bound in the queued may ability |
| `Triggers.creatureDealsCombatDamageToYou()` | `TriggerCollectionService.queueCreatureCombatDamageToYouTriggers` — never targets; the damaging creature is baked in as the stack entry's `targetId` with `nonTargeting = true` and is its triggering permanent, so no pending-choice is queued. The entry goes through the collector's dispatch and `enqueueTrigger`. Teysa, Envoy of Ghosts; Hixus, Prison Warden. | None (baked-in subject) |
| `Triggers.youGainLife()` | `MiscTriggerCollectorService` | Life-gain; the legacy creature/player path is used when no card-level permanent target filter is declared, while declared permanent filters are evaluated for target selection |
| `Triggers.opponentGainsLife()` | `TriggerCollectionService.checkLifeGainTriggers` + `MiscTriggerCollectorService` | Life-gain |
| `Triggers.youDrawCard()` / `Triggers.youDrawSecondCardEachTurn()` (targeted effects) | `DrawService.checkControllerDrawTriggers` queues `DrawTriggerPermanentTarget` for a permanent-target effect bound to the card's `target(...)` filter (e.g. Mantle of Tides). `TriggeredAbilityQueueService.processNextDrawTriggerPermanentTarget` uses the shared `TriggerTargetCollector`; the controller chooses the permanent before the ability goes on the stack. | Draw (permanent target) |
| `Triggers.opponentGainsLife() from the graveyard` | `TriggerCollectionService.checkLifeGainTriggers` | None (graveyard-resident, non-targeting) |
| `Triggers.youGainLife() from the graveyard` | `TriggerCollectionService.checkLifeGainTriggers` | None (graveyard-resident, non-targeting) |
| `Triggers.milled(Triggers.NONLAND, ObjectRelation.OPPONENT_OWNS)` | `TriggerCollectionService.checkGraveyardOpponentNonlandCardMilledTriggers` | None (graveyard-resident, non-targeting; one trigger per nonland card milled) |
| `Triggers.opponentDrawsSecondCardEachTurn() from the graveyard` | `DrawService.checkOpponentDrawTriggers` scans each non-drawing player's graveyard on every draw (`LibraryWatch.GRAVEYARD_ON_OPPONENT_DRAWS`); the collector judges the ability's count (`LibraryAbilityReader`) and fires it on the second | None (graveyard-resident, non-targeting) |
| `Triggers.youDrawCard()` (any-target effects) | `DrawService.checkControllerDrawTriggers` → `DrawTriggerAnyTarget` (queued when the effect's `targetSpec().declares(TargetPredicates.anyTarget())`, e.g. Niv-Mizzet, the Firemind's "deals 1 damage to any target"). Processed by `TriggeredAbilityQueueService.processNextDrawTriggerTarget` (creature/player any-target choice). Non–any-target draw triggers (Psychosis Crawler) still push a non-targeting entry straight to the stack. | Draw (any target) |
| `Triggers.opponentDrawsCard()` (any-target effects) | `DrawService.checkOpponentDrawTriggers` → `DrawTriggerAnyTarget` (the source controller chooses the target before the ability goes on the stack). | Draw (any target) |
| `Triggers.entersFrom(Zone.GRAVEYARD, Triggers.CREATURE, ObjectRelation.ANY)` | `TriggerCollectionService.checkEntersFromGraveyardTriggers` | Enters-from-graveyard (any target) |
| `Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL)` / `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` / `Triggers.enters(null, ObjectRelation.ANOTHER_YOU_CONTROL)` / `Triggers.enters(Triggers.CREATURE, ObjectRelation.OPPONENT_CONTROLS)` / `Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS)` / `Triggers.enters(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL)` (targeting effects) | `EnterTriggerCollectorService.handleEnterDefault` → `EntersTriggerTarget` (queued when the effect's `targetSpec()` includes permanents, e.g. Reaper King's "destroy target permanent"). Player-targeting effects on `Triggers.enters(null, ObjectRelation.ANOTHER_YOU_CONTROL)` also queue this choice because the entering player's identity is not a default target; other player-targeting enter effects may use their pre-set `defaultTargetPlayerId`. | Enters (reuses `TriggerTargetCollector.Options.ATTACK` for the target list — permanents honouring the card's `PermanentPredicateTargetFilter` / `ControlledPermanentPredicateTargetFilter`; true "any target" effects are narrowed by evaluating `TargetPredicates.anyTarget()`) |
| `Triggers.enters(null, ObjectRelation.ANY)` | `TriggerCollectionService.checkAllyPermanentEntersTriggers` scans the controller's graveyard for each permanent entering under that controller's control; the trigger's filter restricts the entering permanent (`PermanentIsCommanderPredicate(true)` for Endless Ranks of HYDRA) and `MayEffect` is queued as a may-ability. | None (graveyard-resident, non-targeting) |
| `Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL)` | `PermanentControlSupport.applyCreateToken` → `BattlefieldEntryService.checkAllyTokenEntersTriggers` → `TriggerCollectionService.checkAllyTokenEntersTriggers`; ordinary effects fire once per token-creation batch and snapshot the batch count into `eventValue`, while effects branching on ability resolution count fire once per token | Token creation (non-targeting) |
| `Triggers.enters(Triggers.TOKEN, ObjectRelation.YOU_CONTROL)` | `PermanentControlSupport.applyCreateToken` → `BattlefieldEntryService.checkAllyTokenEntersTriggers` → `TriggerCollectionService.checkAllyTokenEntersTriggers`; ordinary effects fire once per token-creation batch and snapshot the batch count into `eventValue`, while effects branching on ability resolution count fire once per token. Permanent-targeting effects queue `EntersTriggerTarget` for target choice before the trigger is put on the stack. | Token creation |
| `Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL)` (permanent-targeting effects only) | `TriggerCollectionService.checkAllyEnchantmentEntersTriggers` → `EntersTriggerTarget` (queued when the resolved effect's `targetSpec()` includes permanents — including a `MayEffect` wrapper, whose spec delegates to the wrapped effect; Oath of the Ancient Wood's "you may put a +1/+1 counter on target creature"). Non-targeting effects still push straight to the stack with `triggeringCardId` set. | Enters (same `Options.ATTACK` target list; honours the card's `PermanentPredicateTargetFilter`) |
| `Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL)` | `TriggerCollectionService.checkAllyEnchantmentEntersTriggers` scans the entering player's graveyard; a `MayEffect` is queued as a may-ability and other effects are pushed onto the stack. | None (graveyard-resident, non-targeting) |
| `Triggers.auraBecomesAttachedToThis()` (non-targeting only) | `TriggerCollectionService.checkAuraAttachedTriggers` — called after `setAttachedTo` from `StackResolutionService` (Aura spell resolving, incl. reanimation Auras), `AttachSourceAuraToTargetCreatureEffectHandler`, `AttachTargetAuraToTargetCreatureEffectHandler`, and the Aura-move player choices in `PermanentChoiceBattlefieldHandlerService` (Aura Graft, attach-all-Auras, reattach-after-sacrifice). Fires on the newly enchanted permanent for **its** controller, so an opponent's Aura triggers it. Brood Keeper. | — (pushes a non-targeting entry straight to the stack) |
| `Triggers.auraYouControlBecomesAttachedToNonlandPermanentOpponentControls()` (non-targeting only) | `TriggerCollectionService.checkAuraAttachedTriggers` — uses the Aura permanent's current controller, the newly enchanted permanent's current controller, effective land type, and mana values at the attachment event. Scans the Aura controller's battlefield for the slot and stores the Aura as `sourcePermanentId` plus the enchanted permanent as `targetId`; Eriette the Beguiler. | — (pushes a non-targeting entry straight to the stack) |
| `Triggers.youOrYourPlaneswalkerDealtCombatDamage() from the graveyard` | `CombatDamageService.checkGraveyardCombatDamageToYouOrPlaneswalkerTriggers` — fires from the graveyard of every player dealt combat damage this step, directly or on a planeswalker they control. The only targeting graveyard slot: it queues an `AttackTriggerTarget` whose `sourceCard` is the graveyard card (no source permanent), and `CombatDamageService` drains it before the damage step ends so "attacking creature" target filters still see the attackers. Vengeful Pharaoh. | Attack |
| `Triggers.creatureYouControlExplores()` | `TriggerCollectionService.checkExploreTriggers` | Explore |
| `Triggers.crewsVehicle()` | `TriggerCollectionService.checkCrewsVehicleTriggers` from `CrewCostHandler`; the Vehicle is stored as the triggered entry's `triggeringPermanentId` so effects can resolve against "that Vehicle" | Non-targeting |
| `Triggers.permanentEnteringCausesAbilityToTrigger()` | `EtbTriggerService.queueTriggeredAbilityCounters`, as an own enters ability goes on the stack: the abilities go on the stack after it as written, recording it as the triggering spell, its controller as the triggering player and its stack object as the non-targeting "that ability"; a watcher that lost its abilities doesn't trigger (Strict Proctor's `CounterUnlessPaysEffect(2)`) | Non-targeting |
| `Triggers.creatureYouControlCrewsVehicle()` | `TriggerCollectionService.checkAllyCreatureCrewsVehicleTriggers` from `CrewCostHandler`; "when this or another [filter] creature you control crews a Vehicle" is the trigger's `withObjectFilter(PermanentPredicate)` on the crewing creature, judged from the ability's source as the crew cost is paid; the effects go on the stack as written with the Vehicle as the entry's triggering permanent ("that Vehicle") | Non-targeting |
| `Triggers.exploits()` | `TriggerCollectionService.checkExploitTriggers` | Exploit; player-targeting effects use `ExploitPlayerTriggerTarget`, permanent-targeting effects use `ExploitPermanentTriggerTarget`, and stack-targeting effects use `ExploitTriggerTarget` |
| `Triggers.youClash()` | `TriggerCollectionService.fireClashTriggers` | Clash — targeting triggers via `ClashTriggerTarget` (opponent-creature only); non-targeting triggers pushed straight to the stack |
| `Triggers.championsCreature()` | `PermanentChoiceBattlefieldHandlerService.handleChampionCreature` | Player/permanent target via `ChampionedTriggerTarget` (collected with `Options.END_STEP`; Mistbind Clique taps target player's lands) |
| `Triggers.creatureDies(DAMAGED_BY_THIS)` (targeting effects) | `TriggerCollectionService.triggerCreatureDeath` (`CreatureDeathWatch.DAMAGED`) → `SelfTriggeredAbilityTarget`; the target is chosen as the trigger is put on the stack. Non-targeting effects are pushed directly. | Damaged-creature death (reuses `Options.END_STEP`) |
| Planeswalker ultimate emblems | `DrawService` / `TriggerCollectionService` (including land-entry emblem markers) | Emblem |
| `Triggers.chapter(N)` | `StepTriggerService.handleSagaLoreCounters` (precombat main lore counter) / `SagaChapterService.triggerSagaChapter` (entering, effect-placed lore counters, copied chapters) | Saga chapter; targets from `Card#getSagaChapterTargetFilters(N)` / `getSagaChapterTargetGroups(N)` |

Slots that currently **only ever push non-targeting entries** (no pending queue):
The controller-draw slots are an exception when an effect declares a target: any-target effects use
`DrawTriggerAnyTarget`, and permanent-target effects use `DrawTriggerPermanentTarget` as described above.
`Triggers.attackingCreatureYouControlAbilityTriggers()`, `Triggers.blocks()` (only the non-targeting "that creature" flavour; the targeting variant is routed through the Attack pipeline — see the mapping table above),
`Triggers.upkeep(ActivePlayer.YOU) from the graveyard`, `Triggers.upkeep(ActivePlayer.ANY)`, `Triggers.upkeep(ActivePlayer.ANY) from exile`, `Triggers.upkeep(ActivePlayer.YOU) from exile`, `Triggers.upkeep(ActivePlayer.YOU) from exile`, `Triggers.upkeep(ActivePlayer.OPPONENT)`,
`Triggers.creatureDies(ANY)`,
`Triggers.nontokenCreatureDies(YOU_CONTROL)`, `Triggers.nontokenCreatureDies(ANY)`, `Triggers.creatureDies(OPPONENT_CONTROLS)`,
`Triggers.dealsCombatDamageToPlayer()` (when no target is declared), `Triggers.dealsCombatDamageToCreature()`, `Triggers.dealsDamageToPlayer()`,
`Triggers.equippedCreatureDealsCombatDamage()`, `ON_EQUIPMENT_ATTACHED_TO_CREATURE`,
`Triggers.dealsDamage()`, `Triggers.instantOrSorceryYouControlDealsDamage()`, `Triggers.becomesBlocked()` (only the non-targeting flavour; a
permanent-targeting `MayEffect` is routed through `queueMayAbility` — see the mapping table above),
`Triggers.drawStep(ActivePlayer.YOU)`, `Triggers.drawStep(ActivePlayer.ANY)`,
`Triggers.youDrawCard()` (only the non–any-target flavour; the any-target variant is routed through the
`DrawTriggerAnyTarget` pipeline — see the mapping table above), `Triggers.opponentDrawsCard()` (only the
non–any-target flavour; any-target effects use the same `DrawTriggerAnyTarget` pipeline),
`Triggers.enchantedPlayerDrawsCard()`, `Triggers.opponentDiscardsCard()`,
`Triggers.enters(Triggers.LAND, ObjectRelation.ENCHANTED_PLAYER_CONTROLS)`,
`Triggers.playerTapsLandForMana()`, `Triggers.permanentYouControlBecomesTapped()`, `Triggers.permanentOpponentControlsBecomesTapped()`, `Triggers.crewsVehicle()`,
`Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL)`, `Triggers.sacrificed(null, ObjectRelation.OPPONENT_CONTROLS)` (carries the sacrificing player as a non-targeting player reference), `Triggers.sacrificed(Triggers.NONTOKEN, ObjectRelation.OPPONENT_CONTROLS)` (carries the sacrificed card id on the trigger for effects such as It That Betrays), `Triggers.youAttack()`,
`Triggers.playerTapsLandForMana()`, `Triggers.crewsVehicle()`,
`Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL)`, `Triggers.youAttack()`,
`Triggers.dies(Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` (Jinxed Ring; fires only for nontoken permanents entering the graveyard owned by the slot's controller),
`Triggers.dies(Triggers.NONTOKEN, ObjectRelation.ANY)` (Liability; fires for nontoken permanents entering any player's graveyard and bakes the graveyard owner as the trigger's target),
`Triggers.dies(null, ObjectRelation.YOU_OWN)` (Scrapheap; fires for any permanent, including tokens, entering the graveyard owned by the slot's controller),
`Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY)` (non-targeting effects use the direct stack path; a declared target, "an artifact you control" (`ObjectRelation.YOU_CONTROL`) included, uses the spell-target pipeline),
`Triggers.dies(Triggers.ENCHANTMENT, ObjectRelation.ANY)` (Femeref Enchantress; fires on every permanent with the slot
whenever an enchantment is put into a graveyard from the battlefield — checked in `PermanentRemovalService`),
`Triggers.dies(Triggers.LAND, ObjectRelation.ANY)` (Dingus Egg; fires on every permanent with the slot
whenever a land is put into a graveyard from the battlefield — checked in `PermanentRemovalService`; the
collector pre-sets the damage target to the land's controller; a `TriggeringCardMatches` "if"
can filter the triggering land card and preserve its controller for resolution),
`Triggers.dies(Triggers.ARTIFACT, ObjectRelation.OPPONENT_CONTROLS)` ("an artifact an opponent controls" — keyed on the artifact's controller),
`Triggers.dies(Triggers.ARTIFACT, ObjectRelation.OPPONENT_OWNS)` (Viridian Revel "into an opponent's graveyard" — keyed on the graveyard owner, so it fires for your stolen-back opponent-owned artifact and not for your own artifact an opponent controls),
`Triggers.dies(Triggers.LAND, ObjectRelation.YOU_OWN).causedBy(PlayerRelation.OPPONENT)` (Sacred Ground; fires only on permanents the
graveyard owner controls, and only when `GameData.currentlyResolvingControllerId` — the controller of
the resolving spell/ability — is an opponent of the graveyard owner; the collector gives the stack
entry the dying land as its triggering card, which `ReturnTriggeringLandFromGraveyardToBattlefieldEffect` reads),
`Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD, ObjectRelation.YOU_OWN)` (Countryside Crusher; fires on every permanent the
graveyard owner controls whenever a non-token land card enters their graveyard from any zone — checked in
`GraveyardService.addCardToGraveyard`, the single zone→graveyard choke point; wrap the effect in
`TriggerLimit.FIRST_TIME_EACH_TURN` for “the first time each turn”),
`Triggers.milled(Triggers.LAND_CARD, ObjectRelation.YOU_OWN)` (Pedantic Learning; fires on every permanent the graveyard owner controls
whenever a non-token land card actually enters their graveyard from their library — checked in
`GraveyardService.addCardToGraveyard` after replacement effects),
`Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN)` (Sidisi, Brood Tyrant; fires once per
library-to-graveyard event when one or more non-token creature cards actually enter the graveyard
owner's graveyard — mill batches are checked in `GraveyardService.resolveMillPlayer` after replacement
effects),
`Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` (Devourer of Memory; fires once per
library-to-graveyard event when one or more non-token cards actually enter the controller's graveyard —
mill batches are checked after replacement effects, and the entry records the cards as its triggering cards with
their graveyard stays; a narrower filter such as Hedge Shredder's `Triggers.LAND_CARD` makes the event those cards),
`Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE, ObjectRelation.ANY)` (Dreadhound; fires once for each non-token
creature card that actually enters any player's graveyard from a library, after replacement effects;
checked in `GraveyardService.addCardToGraveyard`),
`Triggers.putIntoLibrary(null, ObjectRelation.ANOTHER)` (Dutiful Knowledge Seeker; fires once when one or more cards enter any
player's library from another zone, with the library owner preserved in `TriggerContext.CardsPutIntoLibrary`),
`Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_OWN)` (Soulcipher Board; fires on every permanent the
graveyard owner controls whenever a non-token creature card enters their graveyard from any zone — uses
printed card types, not battlefield creature-ness; checked in `GraveyardService.addCardToGraveyard`),
`Triggers.putIntoGraveyardFromAnywhereButBattlefield(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANY)` (global watcher; fires on every permanent
whenever a non-token creature card enters any graveyard from a non-battlefield zone; checked in
`GraveyardService.addCardToGraveyard`),
`Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.ANOTHER_YOU_OWN)` (Quest for Ancient Secrets; fires on every permanent the
graveyard owner controls whenever a non-token card enters their graveyard from any zone — checked in
`GraveyardService.addCardToGraveyard`),
`Triggers.putIntoGraveyardFromAnywhereButBattlefield(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.YOU_OWN)` (Ultron the Annihilator; fires on every
permanent the graveyard owner controls whenever a non-token artifact card enters their graveyard from a
non-battlefield zone),
`Triggers.putIntoGraveyardFromAnywhere(Triggers.BLACK_CARD, ObjectRelation.OPPONENT_OWNS)` (Compost; fires on every permanent controlled by
an opponent of the graveyard owner whenever a black card enters that graveyard from any zone — checked in
`GraveyardService.addCardToGraveyard`), `Triggers.enchantedPermanentBecomesTapped()`,
`Triggers.permanentYouControlBecomesTapped()`, `Triggers.permanentOpponentControlsBecomesTapped()`, and
`Triggers.youTapPermanentOpponentControls()` (non-targeting effects use the direct
stack path; graveyard-card targets use the shared `SpellGraveyardTargetTrigger` flow). The tap collector
also reads "for the first time during your turn" (`Triggers.becomesTappedForFirstTimeDuringYourTurn()`,
`creatureYouControlBecomesTappedForFirstTimeDuringYourTurn()`, judged from the ability through
`TapAbilityReader`): it checks the tapped creature's controller against
the active player, records its turn-wide tap count, and preserves the event permanent in
`StackEntry.triggeringPermanentId` for effects such as `UntapPermanentsEffect(TapUntapScope.TRIGGERING)`;
the effect rechecks the first-event condition when it resolves.
`Triggers.becomesUntapped()` (Hollowsage; fires when the permanent transitions tapped→untapped, from
the untap step or any untap effect, via `TriggerCollectionService.checkBecomesUntappedTriggers` — driven
from `UntapStepService` and `TapUntapSupport.untapPermanent`. Targeted effects choose targets as the ability
is put on the stack; a "you may have target player …" is expressed as a `MayEffect`-wrapped targeting effect
whose "may" and target are resolved on the stack via the pending-may-ability flow),
`Triggers.permanentYouControlBecomesUntapped()` (Wake Thrasher; the untapped-ally counterpart of
`Triggers.permanentYouControlBecomesTapped()` — fires once per untapped permanent on every permanent with the ability on
the untapped permanent's controller's battlefield, including the source untapping itself; same
`checkBecomesUntappedTriggers` call sites. Filter the untapped permanent with the trigger's `.withFilter(p)`),
`Triggers.permanentBecomesUntapped()` (Mesmeric Orb; fires once for every permanent that transitions
tapped→untapped on any battlefield. Filter the untapped permanent with the trigger's `.withFilter(p)`. The untapped permanent and its controller are recorded on the non-targeting
triggered entry so an effect can use that controller even if the permanent leaves before resolution),
`Triggers.becomesRenowned()` / `Triggers.creatureYouControlBecomesRenowned()` (Relic Seeker / Valeron Wardens; fired from
`RenownEffectHandler` via `TriggerCollectionService.checkBecomesRenownedTriggers` on the flip to renowned
only. The ally slot fires on every permanent with it on the renowned creature's controller's battlefield,
including that creature itself; give the trigger a filter to restrict the renowned
creature),
`Triggers.phasesOut(null, ObjectRelation.THIS)` / `Triggers.phasesIn(null, ObjectRelation.THIS)` (Teferi's Imp; fired from `PhasingService` via
`TriggerCollectionService.checkPhasesOutTriggers` / `checkPhasesInTriggers` — phase-out triggers are collected
before the permanent leaves the battlefield, since they look back in time (CR 603.10b). Non-targeting
phase triggers cover the untap-step turn-based action and effect-driven phase-outs. **Targeted**
`Triggers.phasesIn(null, ObjectRelation.THIS)` effects — when `targetSpec().admits(Kind.PERMANENT)` — queue a
`PhasesInTriggerTarget` instead and are drained at upkeep start via
`StepTriggerService.processNextPhasesInTriggerTarget` (Shimmering Efreet's "target creature phases out";
reuses `TriggerTargetCollector.Options.END_STEP`)),
`Triggers.phasesOut(null, ObjectRelation.ANOTHER)` (The War Doctor; global non-targeting watcher dispatched before
the phased-out permanent is removed), and `Triggers.exiledFrom(Set.of(), Triggers.NONTOKEN, ObjectRelation.ANY)` (The War Doctor; global non-targeting
watcher dispatched after a non-token card enters exile),
`Triggers.enchantedCreatureDealtDamage()`,
`Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS)`, `Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL)`,
`Triggers.opponentLosesLife()`, `Triggers.opponentShufflesLibrary()`,
`Triggers.playerShufflesLibrary()`,
`Triggers.opponentSearchesTheirLibrary()` (Ob Nixilis, Unshackled; fired by `LibrarySearchTriggerHelper` from
`LibrarySearchSupport.sendLibrarySearchToPlayer` — the choke point every card-presenting search passes
through — plus the empty-library / no-match early returns of `performLibrarySearch`, which are searches
too. Only a player searching **their own** library counts (`params.targetPlayerId()` unset); a search
prevented by Leonin Arbiter never happens and does not fire. Non-targeting, but the searching player is
baked in as the trigger's `targetId`, so `TARGET_PLAYER`-scoped effects act on them),
`Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER)`, `Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER)`,
`Triggers.enters(Triggers.EQUIPMENT, ObjectRelation.ANOTHER_YOU_CONTROL)`,
`Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL)` (Trial of Solidarity; "Whenever an enchantment enters under
your control" — checked in `TriggerCollectionService.checkAllyEnchantmentEntersTriggers` from
`BattlefieldEntryService.processCreatureETBEffects`, skips the entering permanent itself; gate by subtype
through the trigger's filter (`PermanentAllOfPredicate(Triggers.ENCHANTMENT, PermanentHasSubtypePredicate(...))`) for "Whenever a Cartouche you control enters"),
`Triggers.enters(Triggers.CREATURE, ObjectRelation.OPPONENT_CONTROLS)`,
`Triggers.opponentDealtNoncombatDamage()`, `Triggers.opponentDealtDamageByRedInstantSorceryOrPlaneswalkerYouControl() from the graveyard`,
`Triggers.creaturesYouControlDealDamageToPlayer()`,
`Triggers.creaturesYouControlDealDamageToOpponent()`,
`Triggers.creatureYouControlDealsCombatDamageToPlayer()`,
`Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY)` (The Wise Mothman; fires once when a mill event contains one or more
nonland cards, regardless of which player milled them),
`Triggers.mills(null, ObjectRelation.OPPONENT_OWNS)` (Lo and Li, Royal Advisors; fires once when an opponent mills one or more
cards in a single mill event), `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS)`, `Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED)`,
`Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER)` (Extractor Demon; global watcher — fires on every permanent
with the slot whenever another creature leaves the battlefield by any means, checked in
`PermanentRemovalService` via `TriggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers`.
`ON_CREATURE_EXILED_FROM_BATTLEFIELD` (Soulherder; global watcher — fires on every permanent with
the slot whenever a creature is exiled from the battlefield, checked after the card enters exile).
`Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` is the controller-scoped sibling; the stack
entry carries the leaving creature as it last existed on the battlefield (CR 603.10a), so effects read
its counters as they resolve.
`Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER_YOU_CONTROL)` is the controller-scoped watcher for any permanent type;
use the trigger's filter to narrow it to tokens or another permanent predicate.
`Triggers.leavesBattlefield(null, ObjectRelation.YOU_CONTROL)` is the batched controller-scoped watcher for "one or more"
permanents, including the source permanent when it leaves; the removal service snapshots a batch
before dispatching one trigger per source.
The stack entry carries the leaving permanent as it last existed on the battlefield (CR 603.10a) and
effects read its counters as they resolve; targeted effects are routed through the trigger-target
interaction flow (which carries it via `withTriggeringPermanent`).
Non-targeting: a "you may have target player mill two cards" is a `MayEffect`-wrapped
`MillEffect(2, TARGET_PLAYER)` whose "may" and player target are resolved on the stack),
`Triggers.milled(THIS)`, `Triggers.putIntoGraveyardFrom(Zone.LIBRARY, THIS)`, the state triggers (`Triggers.state(…)`), `Triggers.beginningOfCombat(ActivePlayer.YOU)`,
`Triggers.beginningOfCombat(ActivePlayer.ANY)`, `Triggers.beginningOfCombat(ActivePlayer.OPPONENT)`,
`Triggers.creatureOpponentControlsDealtDamage()`, `Triggers.youCastSpell() from the graveyard`,
`Triggers.youLoseLife()`,
`Triggers.plusOneCountersPutOnThis()`,
`Triggers.trains()` (Savior of Ollenbock; fires only when training successfully puts its +1/+1 counter on the source),
`Triggers.plusOneCountersPutOnHumanYouControl()` (Cloaked Cadet; fires when one or more +1/+1
counters are put on a Human the controller controls, including counters the Human enters with),
`Triggers.countersPutOnThis()` (fires when one or more counters are put on the source permanent; a
`SourceCounterThreshold` conditional in this slot can fire when the source crosses the threshold),
`Triggers.plusOneCountersPutOnAnotherNonHydraCreatureYouControl()` (Wildwood Scourge; fires once when
one or more +1/+1 counters are put on another non-Hydra creature the controller controls),
`Triggers.youPutPlusOneCountersOnAnotherCreature()` (Knight of Wundagore; fires once when
the controller puts one or more +1/+1 counters on a creature other than the watcher, regardless of
that creature's controller; a restriction on that creature such as "another colorless creature" is the
trigger's `withFilter(…)`),
`Triggers.youPutPlusOneCountersOnAnotherHeroYouControl()` (Invisible Woman, Sue Storm; fires once when
the controller puts one or more +1/+1 counters on another Hero they control),
`Triggers.plusOneCountersPutOnAnotherCreatureYouControl()` (Enduring Scalelord; fires once when
one or more +1/+1 counters are put on another creature the controller controls),
`Triggers.youPutPlusOneCountersOnCreatureYouControl()` (Terrasymbiosis; fires once when the
controller puts one or more +1/+1 counters on a creature they control and carries the number of
counters placed as the trigger event value; "do this only once each turn" is
`TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)`),
`Triggers.plusOneCountersPutOnCreatureYouControl() from the graveyard` (Moss-Pit Skeleton; fires once for
each +1/+1 counter-placement event on a creature controlled by the graveyard card's owner),
`Triggers.youPutCountersOnPermanentOrPlayer()` (All Will Be One; fires once for each counter-placement
event caused by the controller, including poison counters, and uses the spell-target trigger pipeline; "that much damage" is `DealDamageToAnyTargetEffect(new EventValue())`, the counters put the choice's event value),
`Triggers.youPutTimeCountersOnPermanentYouControl()` (Kate Stewart; fires once when one or more time
counters are put on a permanent controlled by the placing player, including entry counters; non-targeting),
`Triggers.youPutCountersOnCreature()` (Rikku, Resourceful Guardian; fires once per placement event for
one or more counters of any type put on any creature by the controller, carrying the affected creature
as a non-targeting event subject),
`Triggers.youPutCountersOnAnotherCreature()` (Captain Marvel, Apex Avenger; fires once per placement
event when the controller puts one or more counters of any kind on another creature, regardless of
that creature's controller; non-targeting and captures the counter kind, amount, and creature for
resolution),
`Triggers.opponentPutsCountersOnCreatureTheyControl()` (Bold Plagiarist; fires once per placement
event when an opponent puts one or more counters on a creature they control; the trigger remains
controlled by Bold Plagiarist's controller and captures the opponent who placed the counters so the
same number and kind are placed by that opponent),
`Triggers.countersPutOnCreatureYouControl()` (Hollowmurk Siege; fires for counters of any type put on a creature
the controller controls, including counters the creature enters with; its once-each-turn limit
is marked only after its intervening "if" (the chosen mode) is met. `TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE`
is the first-time-per-creature wording and keys the marker to the watcher and triggering creature;
`Triggers.youPutCountersOnCreatureYouControl()` counts only the controller's placements),
`Triggers.evolves()` (Renegade Krasis; fired by `EvolveEffectHandler` only when the evolve keyword's ability
actually places the +1/+1 counter),
`Triggers.minusOneCounterPutOnCreature()` (Flourishing Defenses; global watcher — fires on
every permanent with this slot, under that permanent's controller, once per individual -1/-1 counter put
on any creature from any source, via `PermanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers`;
non-targeting — a "you may create …" is a `MayEffect` resolved on the stack),
`Triggers.minusOneCountersPutOnCreature()` (Auntie Ool, Cursewretch; global watcher — fires
once per creature per -1/-1 counter-placement event regardless of the number of counters placed;
the affected creature is carried as the trigger's target and its trigger-time controller is retained),
`Triggers.youPutMinusOneCounterOnCreature()` (no card uses it yet; controller-restricted variant of
the above — same firing method and per-counter cadence, but a permanent only triggers when its controller
is the player who put the counters. The placing player is `gameData.currentlyResolvingControllerId` for
spell/ability resolution and the damage source's / permanent's controller for combat placements, passed via
the 4-arg overload of `fireMinusOneMinusOneCounterPutOnCreatureTriggers`; non-targeting),
`Triggers.youPutMinusOneCountersOnCreature()` (Hapatra, Vizier of Poisons; the "one or more
counters, do it once" cadence — same controller restriction and firing method as the per-counter slot
above, but fires exactly one trigger per creature per placement instance regardless of how many -1/-1
counters were placed at once; non-targeting — the Snake creation is a plain `CreateTokenEffect`),
`Triggers.dies(Triggers.AURA_OR_EQUIPMENT, ObjectRelation.YOU_CONTROL)`,
`Triggers.youAttack() from the graveyard`, `Triggers.creatureYouControlDealsCombatDamageToPlayer() from the graveyard`,
`Triggers.milled(Triggers.NONLAND, ObjectRelation.OPPONENT_OWNS)` (Infesting Radroach; scans each owner's graveyard and
fires once per nonland card milled by an opponent),
`Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL)` (graveyard mirror of `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)`;
subtype filter on the trigger + `MayPayManaEffect` pay-to-return — Unconventional Tactics),
`Triggers.enters(null, ObjectRelation.ANY)` (graveyard mirror of
`Triggers.enters(null, ObjectRelation.ANOTHER_YOU_CONTROL)`; narrow it with the trigger's filter for a commander or
other permanent predicate — Endless Ranks of HYDRA),
`Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD, ObjectRelation.ANY)` (graveyard watcher for any non-token land card entering any graveyard from any zone; `MayPayManaEffect` pay-to-return — Centaur Vinecrasher),
`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY)` (graveyard watcher for any creature entering;
the trigger's filter (`PermanentMinManaValuePredicate`) can gate on the entering card's mana value — Dragon Scales),
`Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)` (scans each graveyard for
creatures that entered from that graveyard or were cast from it; Prized Amalgam),
`Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility()`,
`Triggers.transformsToBackFace(ObjectRelation.THIS)`, `Triggers.transformsToFrontFace(ObjectRelation.THIS)`,
`Triggers.youActivateAbility()` (Ceaseless Searblades; fires on every permanent with this slot on
the activating player's battlefield, once per activated-ability activation incl. mana abilities;
`.withSource(filter)` filters by the permanent whose ability was activated),
`Triggers.opponentActivatesNonManaAbility()` (Harsh Mentor; the opponent-scoped mirror — fires on every
permanent NOT controlled by the activating player, only on the non-mana activation path so mana
abilities never trigger it; `.withSource(filter)` filters by the activated
permanent's type; the activating opponent is baked as the non-targeting `targetId`),
`Triggers.youActivateEternalizeOrEmbalm()` (Vizier of the Anointed; fires once per activation on
every permanent with this slot on the activating player's battlefield when the activated graveyard
ability is embalm/eternalize — `ActivatedAbility.isEmbalmOrEternalize()`; no conditional wrapper, the
keyword gate is applied at the call site in `AbilityActivationService.completeGraveyardAbilityActivation`).

## `Triggers.enters(ObjectRelation.THIS)` targeted triggers

A targeted ETB (the card has a `TargetFilter` and a mandatory `Triggers.enters(ObjectRelation.THIS)` effect, e.g.
Pierce Strider, Geralf's Messenger) chooses its targets **as the trigger is put on the stack**
(CR 603.3d), never while the permanent spell is cast. A permanent spell whose only targets belong to
its ETB abilities (`EffectResolution.targetsOnlyWithEtbAbilities`) takes no targets as it is cast —
`SpellCastingService` rejects any — and `EffectResolution.needsTarget(Card)` /
`computeAllowedTargets(Card)` leave ETB effects out, so the client, the AI and the engine's own
cast prompts never ask for them. `EtbTriggerService.processCreatureETBEffects` drops the targets of
a resolved permanent spell (keeping only an Aura's enchant target) and routes every targeted ETB
through `ETBTokenTargetTrigger` (single target) or `ETBTokenMultiTargetTrigger` (multi-target or
"up to N"), the same path token copies, reanimated or flickered permanents and lands use. An effect
bound to a declared `target(...)` group counts as targeted even when its own spec names no target
kind. "Up to N" abilities prompt too; choosing zero still puts the ability on the stack, where it
resolves doing nothing. Choosing yourself declines an optional single target, or stops a group once
its minimum is met, only when you are not yourself a legal target for it
(`ETBTokenTargetService.controllerChoiceDeclines` / `controllerChoiceStopsGroup`).

The AI answers these prompts through `AiDecisionEngine.answerEtbTargetPrompt`, reusing
`AiTargetSelector`'s spell-target judgement. In tests, a target passed to a harness cast helper for
such a card is checked against the card's target groups at the cast call and then answers the
trigger's prompt (`DeclaredEtbTargets`); an explicit empty list declares zero targets, while a helper
called without targets leaves the prompt to the test, as it does any prompt none of the declared
targets fits. A cast that only a raw `GameService` overload covers goes through
`harness.castDeclaringEtbTargets(...)`, which hands the call the targets the engine should get.
A divided triggered ability ("N damage divided as you choose among …", a counter distribution, divided
prevention) announces its targets and division as it is put on the stack, never from anything staged
before it triggered; a test declares them with `harness.declareDivision(Map.of(target, amount, …))`, which
answers the ability's target choice and per-target amounts once they open (`DeclaredDivisions`, one
declaration per announced division, an empty map choosing no target). A card whose divided ability has a
declared target group takes its targets from the cast helper's target list as above.

A land with a targeted ETB still declares its `target(...)` filter like any
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
gate isn't met (it checks every "when this enters, if …" option as the ability triggers, gate or
not; the gate flag only decides when the target is chosen). When it is met, the same `ETBTokenTargetTrigger` / `ETBTokenMultiTargetTrigger`
deferred path prompts for the target as the trigger goes on the stack (CR 603.3d); the wrapped
`ConditionalEffect` stays on the stack entry and the gate is re-checked at resolution. When adding
a new intervening-if condition that gates a **targeted** ETB, override `isEtbTriggerGate()` on the
condition — the cast-time exclusion keys off it.

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

**Graveyard-targeting postcombat main triggers** ("At the beginning of your second main phase,
return target creature card from your graveyard to your hand" - Savior of the Small) use the same
`SpellGraveyardTargetTrigger` flow. `StepTriggerService.handlePostcombatMainTriggers` queues the
graveyard target after evaluating the trigger condition, and the selected card id is attached when
the ability is put on the stack. The wrapped `ConditionalEffect` remains on the stack so the
survival tap condition is checked again on resolution; mandatory targets with no legal card do not
create a stack entry.

**Graveyard-targeting upkeep triggers** ("At the beginning of your upkeep, you may return target
enchantment card from your graveyard to the battlefield" - Starfield of Nyx) use the same
`SpellGraveyardTargetTrigger` flow. `StepTriggerService.handleUpkeepTriggers` pulls every
`Triggers.upkeep(ActivePlayer.YOU)` effect whose `targetSpec().admits(Kind.GRAVEYARD_CARD)` out of the per-effect loop
and queues them as one interaction, drained at the end of the same method; no matching card in the
graveyard means the trigger is never put on the stack (CR 603.3c). As above, the "you may" needs no
`MayEffect` wrapper - the up-to-one pick can be left empty.

**Graveyard-targeting controller end-step triggers** use the same trigger-time selection even when
the graveyard effect is wrapped in `MayEffect` (Spirit-Sister's Call). `StepTriggerService` queues
the `SpellGraveyardTargetTrigger` before the resolution-time may choice, so the chosen card is
already on the stack entry when the optional sacrifice resolves.

**Graveyard-targeting death triggers** ("When ~ dies, exile target card from an opponent's graveyard" —
Ruin Rat) use the same trigger-time graveyard selection on `Triggers.dies(THIS)` and `Triggers.creatureDies(YOU_CONTROL)`.
`handleDeathDefault` and `TriggerCollectionService.triggerCreatureDeath` (`CreatureDeathWatch.ALLY`) route any death
effect whose `targetSpec().admits(Kind.GRAVEYARD_CARD)` to a `DeathTriggerTarget` (alongside the
permanent/player routing). `TriggeredAbilityQueueService.processNextDeathTriggerTarget` detects the
graveyard target and calls `beginDeathGraveyardTarget`, which searches the declared scope and prompts a
`MultiGraveyardChoice`. Optional up-to-one targets may be declined; a mandatory target with no legal
card skips the trigger. An ally-death target may carry an excluded dying-card ID for an "other"
restriction, so `CardNotPredicate(CardIsSelfPredicate())` is evaluated against the actual dying card.
Use `Triggers.dies(THIS)`, never `Triggers.leavesBattlefield(THIS)` (Offalsnout), for a "dies" trigger — the latter
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

**Death-event value (`Triggers.creatureDies(YOU_CONTROL)`):** a targeted ally-death trigger is queued with the dying
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
attach the ETB filter with `target(...).addTrigger(Triggers.enters(ObjectRelation.THIS), …)` and add the death effect via
plain `addTrigger(Triggers.dies(ObjectRelation.THIS), …)` (no `target(...)`); the ETB filter then does not leak into the death
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

- **"My Triggers.dies(THIS) trigger targets non-creatures."** Give the card an explicit
  `PermanentPredicateTargetFilter` — its predicate then governs and the death pipeline's default
  `creaturesOnly` narrowing is skipped (e.g. Fire Snake targeting a land). Without such a filter,
  death targets are creatures only.
- **"My Triggers.dies(THIS) trigger lets the controller pick themselves as the target."** You forgot the
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
- **"My optional ability asks its question though no target was chosen."** `MayEffectHandler` skips a "you may" whose bound target group took no target while other groups took some, and a "you may" whose wrapped effect declares `actsOnlyOnItsTargets()` (`AttachTargetEquipmentToTriggeringPermanentEffect`, `ReturnTargetCardsFromGraveyardToHandEffect`, `TapPermanentsEffect(TARGET)`) when the entry has no target at all: no permanent or player and no card. Override `actsOnlyOnItsTargets()` only for an effect that does nothing but act on its targets: a "you may pay / sacrifice ... If you do, ... up to N target ..." (Aetherstorm Roc, Glorifier of Suffering) must still be asked with none chosen.
- **"My effect gets no valid targets offered even though the filter matches."** The effect probably
  leaves `targetSpec()` at `NONE`. The `CardEffectTargetingConsistencyTest`
  catches this for effects named `Target*Effect`, but not for other naming conventions.

## Aura trigger slot selection

Auras have their own trigger slots. Use this table to pick the correct one based on the oracle text:

| Oracle text pattern | Trigger slot | Fires when | Example |
|---|---|---|---|
| "At the beginning of your upkeep, ..." | `Triggers.upkeep(ActivePlayer.YOU)` | Aura controller's upkeep (aura is on their battlefield) | Call to the Kindred |
| "At the beginning of enchanted creature's controller's upkeep, ..." | `Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER)` | Enchanted creature's controller is the active player | Soul Bleed, Numbing Dose, Erosion (enchanted land); an ability the Aura grants in quotes (Necrotic Plague's "enchanted creature has '…'") is a `GrantTriggeredAbilityEffect` of a `Triggers.upkeep(ActivePlayer.YOU)` ability instead |
| "At the beginning of your upkeep, enchanted permanent's controller ..." | `Triggers.upkeep(ActivePlayer.YOU)` | Aura controller is the active player; enchanted permanent's controller baked in as `targetId` (`TARGET_PLAYER` recipients) | Strangling Grasp |
| "At the beginning of enchanted player's upkeep, ..." | `Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER)` | Enchanted player is the active player (curses) | Curse of Oblivion, Curse of the Bloody Tome |
| "At the beginning of enchanted player's draw step, ..." | `Triggers.drawStep(ActivePlayer.ENCHANTED_PLAYER)` | Enchanted player is the active player (curses) | Curse of Obsession (`MIC`) |
| "Whenever enchanted opponent draws a card, ..." | `Triggers.enchantedPlayerDrawsCard()` | The player attached to this player Aura draws a card | Psychic Possession (`DIS`) |
| "At the beginning of each upkeep, ..." | `Triggers.upkeep(ActivePlayer.ANY)` | Every player's upkeep; targeted permanents are chosen by the source controller as the trigger is put on the stack | — |
| "At the beginning of each player's upkeep, if this card is suspended, ..." | `Triggers.upkeep(ActivePlayer.ANY) from exile` | Every player's upkeep while the card is exiled with a positive time-counter entry | Curse of the Cabal (`TSP`) |
| "At the beginning of your upkeep, if this card is exiled with a scream counter on it, remove a scream counter from it. If there are no more scream counters on it, put it into your graveyard and each player returns all creature cards from their graveyard to the battlefield." | `Triggers.upkeep(ActivePlayer.YOU) from exile` | The card owner's upkeep while it is exiled with a positive scream-counter entry; the last-counter handler moves it to its owner's graveyard and inserts the existing mass-reanimation effect | All Hallow's Eve (`LEG`) |
| "When enchanted creature dies, ..." | `Triggers.dies(ObjectRelation.ENCHANTED)` | Enchanted creature goes to graveyard | Necrotic Plague (return effect), Banewasp Affliction (life loss = toughness), Creature Bond (damage = toughness), Death Watch (lose life = power + gain life = toughness) |
| "Whenever enchanted creature is dealt damage, ..." | `Triggers.enchantedCreatureDealtDamage()` | Enchanted creature is dealt damage (combat or non-combat) | Spiteful Shadows |
| "Whenever enchanted creature deals damage to a creature, ..." | `Triggers.enchantedCreatureDealsDamageToCreature()` | Enchanted creature deals damage to another creature (combat or non-combat); the damaged creature is captured as a non-targeting `targetId` | Venomous Fangs |
| "Whenever enchanted creature attacks and isn't blocked, ..." | `Triggers.enchantedCreatureAttacksUnblocked()` | Enchanted attacker ends up unblocked (declare-blockers step). Non-targeting effects use `sourcePermanentId`=enchanted attacker and `targetId`=defending player. Permanent-targeting `MayEffect`s defer target selection and use the enchanted creature's controller for the may choice | Cloak of Confusion, Farrel's Mantle |
| "Whenever a creature is dealt damage, ..." (any creature) | `Triggers.creatureDealtDamage()` | Any creature is dealt damage (combat or non-combat). Queued entry targets the damaged creature | Death Pits of Rath |
| "Whenever a permanent you control is dealt damage, ..." | `Triggers.permanentYouControlDealtDamage()` | Any permanent is dealt damage (combat or non-combat); the watching permanent's controller chooses any target as the trigger is put on the stack | Howlpack Avenger |
| "Whenever a creature you control fights, ..." | `Triggers.creatureYouControlFights()` | A creature controlled by the watcher’s controller fights; the fighting creature is captured for the triggered effect | Foe-Razer Regent |

**`Triggers.creatureDealtDamage()`, `Triggers.creatureOpponentControlsDealtDamage()`,
`Triggers.creatureYouControlDealsDamageToCreature()` and `Triggers.enchantedCreatureDealsDamageToCreature()` fire only when the damaged permanent is a creature.** All
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

**Key distinction**: "your upkeep" on an aura means the **aura controller's** upkeep → use `Triggers.upkeep(ActivePlayer.YOU)`. "Enchanted creature's controller's upkeep" means the **enchanted permanent's controller's** upkeep → use `Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER)`. These are different when the aura enchants an opponent's creature.

**How `Triggers.upkeep(ActivePlayer.YOU)` works for auras**: The aura permanent sits on the controller's battlefield. `StepTriggerService` iterates the active player's battlefield looking for permanents with `Triggers.upkeep(ActivePlayer.YOU)` effects. Since the aura is on the controller's battlefield, the trigger fires during the controller's upkeep. The `sourcePermanentId` on the stack entry is set to the aura permanent's ID (`perm.getId()`), so the resolution handler can find the enchanted creature via `auraPerm.getAttachedTo()`.

**How `Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER)` works**: `StepTriggerService` iterates ALL permanents on ALL battlefields, checks if each has this effect slot and is attached, then finds the enchanted permanent's controller. It only fires when that controller is the active player.

**Targeted graveyard-return death triggers** use `ReturnCardFromGraveyardEffect` with
`targetGraveyard(true)` in the `Triggers.dies(THIS)` ability. The death-trigger queue applies the effect's card
predicate and `source()` scope while the trigger is put on the stack, then stores the selected card ID
on the triggered entry for the normal return handler. An empty selection models an optional return.

Every becomes-the-target entry records the spell or ability that targeted as its triggering spell and its
controller as its triggering player, whatever the effects are; an entry with no other referent whose effect acts
only on a spell ("counter that spell or ability") is aimed at that object on the stack without targeting it. The
source permanent id remains available so an Aura effect can re-derive its host at resolution.

"Whenever this permanent becomes the target of a spell [or ability] you control" is
`Triggers.becomesTargetOfYourSpell()` / `Triggers.becomesTargetOfYourSpellOrAbility()` (valiant, with
`TriggerLimit.ONCE_EACH_TURN` for "for the first time each turn"). The collectors of
`becomesTargetOfSpell()` / `becomesTargetOfSpellOrAbility()` read them, judging "you control" from the
ability's own event (`TargetAbilityReader`) as the trigger is collected.

`Triggers.youDrawSecondCardEachTurn()` (and `youDrawNthCardEachTurn(n)`, the opponent forms and
"first or second" as two conditions of one ability) is read by the `Triggers.youDrawCard()`
collector in `DrawService.checkControllerDrawTriggers`, which judges the ability's count
(`LibraryAbilityReader`), so the ability fires only when the controller's per-turn draw count is
exactly that number, with the same stack and any-target handling.
A draw ability's `withFilter(CardPredicate)` ("whenever you draw a Dragon card") is judged by the draw collectors on the drawn card as it is drawn, the card the entry's triggering card; "whenever you reveal a [card] this way" (`Triggers.youRevealCardThisWay()`) is collected for the permanent whose `RevealDrawnCardsEffect` revealed it, with the same stack and any-target handling.

`Triggers.creatureDealtDamage()` also captures the damaged creature's controller as
`triggeringPermanentControllerId`. Pair that capture with `MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER`
and `ExileTopCardsMayPlayUntilNextTurnEffect.forTriggeringPermanentController(...)` when a trigger says
that creature's controller may use their library.

Creature-enter watchers whose trigger effect targets a graveyard card use the same
`handleGraveyardCardsExileETBTargeting` selector directly from `EnterTriggerCollectorService`, since the
effect belongs to the watching permanent rather than to the entering card.

Controller end-step effects bound to multiple declared target groups are queued together as one ability. The slot target walker selects the groups before that ability goes on the stack.

## Planar event slots

`Triggers.planeswalkToThis()`, `Triggers.planeswalkAwayFromThis()`, `Triggers.chaosEnsues()` and `Triggers.encounterThis()` are collected by `PlanechaseService`. Single-target payloads reuse `SpellTargetTriggerAnyTarget` with a planar source snapshot; optional or multi-target groups use the slot-by-slot target walker. Never resolve a target-requiring encounter before its choice, or let phenomenon state-based actions skip that choice. See [PLANECHASE.md](PLANECHASE.md).

`Triggers.putIntoGraveyardFrom(Zone.LIBRARY, THIS)` is collected by `GraveyardService` when the
source zone is the library. It includes non-mill moves such as Call of the Wild; use
`Triggers.milled(THIS)` only when the ability specifically requires milling.

Optional target groups of the slot-by-slot walker (any size, including a single "up to one
target player" slot such as Volatile Arsonist's) use
`MultiPermanentChoiceContext.EtbPlayerTargetGroup` when the controller is a legal target for the
group; any exiled-card candidates are offered in the same prompt (`validCardIds`). The whole group
is chosen in one answer (`harness.handleMultiplePermanentsChosen`); an empty selection declines it,
leaving the controller available as an actual target. Groups that can't target the controller keep
the single-slot prompt, where choosing yourself stops the group.

The single-target `PermanentChoiceContext.ETBTokenTargetTrigger` prompt works the same way: an
optional ("up to one") target that the controller could legally take opens a
`MultiPermanentChoiceContext.EtbOptionalTarget` prompt with at most one pick, and an empty answer
puts the ability on the stack without a target. Only when the controller isn't a legal target does
the prompt keep "choose yourself to decline" (`ETBTokenTargetService.controllerChoiceDeclines`).

End-of-combat planar abilities with multiple declared target groups use the same slot-by-slot
walker as ETB token triggers. A dependent constraint can filter later groups from the first
target's combat-damage recipients; `GameData.combatDamageToPlayersThisCombat` supplies the
current-combat relationship and the legality check must be repeated when the ability resolves.
When a planar slot contains one standalone single-target effect bound to a later declared group,
the planar trigger path uses that group's filter directly rather than walking unrelated groups
belonging to the plane's other abilities.

`Triggers.equippedCreatureDealsCombatDamageToPlayer()` fires only when the equipped creature deals combat damage to a player. It uses the Equipment controller and the same targeting pipeline as `Triggers.equippedCreatureDealsCombatDamage()`, which also fires for damage to permanents.
| `Triggers.youAreDealtDamageBySourceYouControl()` | `TriggerCollectionService.checkControllerDealtDamageTriggers` when the damage source controller matches the damaged player + `DamageTriggerCollectorService` | The source-control gate is checked at the damage event; the damage amount is carried in `eventValue`, and player-targeting effects use `SpellTargetTriggerAnyTarget` |

Triggered modal choices check required graveyard and exile targets when offering a mode and again when accepting the choice. TriggerTargetCollector.hasLegalModeTargets reuses the normal exile target collector, including face-up visibility and the effect's card predicate, so an exile mode without an eligible card is omitted (Blade of the Swarm). Chosen-player upkeep watchers recognize static ChooseOpponentOnEnterEffect and retain the chosen player's identity through control changes (Black Vise).

Independent ETB abilities retain their own declared target group and copy ETB counter distributions onto the queued stack entry. Simultaneous evoke abilities open the existing trigger-order choice before priority even when no additional target choice is required.
