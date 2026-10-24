# Leave-the-battlefield redesign

A step-by-step plan for two engine changes. Together they let a player answer every question about a
permanent leaving the battlefield *before* it leaves, and let a resolving effect pause between its
parts instead of carrying on while a question is open. This replaces the approaches listed in
`missing-features-to-do.md` entry 34, which is removed in step 13.

To start a step, use the prompt at the bottom with the step number. Steps are meant to be done in
order; each one lists what it depends on.

## Status

| Step | Title | Status |
|---|---|---|
| 1 | Behaviour tests for the target rules | done |
| 2 | Departure model types | done |
| 3 | `DepartureService` for state-based deaths | done |
| 4 | Simultaneous removals and destroy batches | done |
| 5 | Fold the leftover replacement workarounds into the choose stage | done (rider and non-batch regeneration choices remain only for callers that can't pause; see Findings) |
| 6 | Legacy wrappers and the guard | done |
| 7 | Resolution scratchpad and pausing at the end of a step | done |
| 8 | Convert the "sacrifice this, then …" handlers | done (bespoke records kept where engine code or data needs them; see Findings) |
| 9 | Convert the "destroy/sacrifice target, then …" handlers | done (Goblin Crash Pilot and Goblin Welder depart at once, not as a step; see Findings) |
| 10 | Convert the choice-completion paths | done (choice completions depart through replay windows rather than scratchpad steps; see Findings) |
| 11 | Loops and drained queues | done (six non-cost callers outside every step's list remain on the allowlist; see Findings) |
| 12 | Costs and mana abilities | done (every sacrifice cost of an activation, an attack, a cast from any zone, a free cast and a morph face-up cost asks before anything leaves; a commander whose tax is paid with life and the suspend / exile-mana tails don't ask, no card needs them; see Findings) |
| 13 | Make the guard strict and remove the fallback | in progress (first part done: the allowlist is empty and 13 more handlers are step departures; the fallback stays until the callers listed in Findings can pause) |
| 14 | Move other pausing effects onto the step model (repeatable) | open |

## Why

**CR 616.1** (checked): when several replacement effects would modify how an event affects an object,
its controller chooses one to apply, and choices made at the same time go in APNAP order
(**CR 101.4**, checked). Only then does the event happen.

Today the engine asks this question before the event only for state-based deaths
(`PermanentRemovalService.awaitStateBasedDeathReplacementChoices`). Everywhere else, when the options
send the creature to different zones (Kalitas's exile vs Gravebane Zombie's library, Firestorm
Phoenix's hand, Pulmonic Sliver's "may"), `removePermanentToGraveyard` opens a
`DeathExileReplacementChoice` with `stateBased = false`, leaves the creature on the battlefield and
returns `false`. The calling code then keeps running as if the removal had failed.

A survey of all 64 files that remove permanents to the graveyard found these consequences:

- About 13 handlers only run their sacrifice triggers, orphaned-Aura cleanup, draws, tokens or
  reflexive abilities when the removal returned `true`, so they silently skip them.
- Some handlers read the zone the card went to:
  - Polymorph and Reweave reveal the library right after the removal, and Gravebane Zombie may be
    in it.
  - Victimize returns cards from the graveyard, possibly including the creature it sacrificed.
  - Garruk, the Veil-Cursed searches the library right after its sacrifice.
- Tyrant of Discord (`TargetPlayerSacrificesRandomPermanentsUntilLandEffectHandler`) stops its loop
  early.
- `StepTriggerService` (lines ~5029 and ~5102) drains its delayed actions before it loops, so a
  question in the middle loses the actions that are left.
- Costs are paid as if the sacrifice happened. `SpellCastingService.paySacrificeCreaturesForCostReduction`
  turns the number sacrificed into mana, so it gets the wrong number.
- `TimmerianFiendsAnteExchangeEffectHandler.moveCardToGraveyard` appears to call itself forever when
  the removal is still waiting for an answer.
- Creatures that die in the same event lose **CR 603.10a** look-back (checked) for one that stayed
  behind.
- Wheel of Sun and Moon is applied later in the graveyard pipeline (`GraveyardService` ~line 649),
  so it is apparently never offered as a CR 616.1 option. This is not yet confirmed by a test.

The engine can't pause a handler partway through; 182 files work around that with
`rerunCurrentEffectAfterInteraction`, and `DamageJournal` exists for the same reason.

Making handlers block on a player answer (a thread waiting inside the handler) is ruled out. The AI
simulator copies `GameData` at an open prompt and answers it on each copy (`GameSimulator` →
`handleInteractionAnswer`). `GameService` also mutates under `synchronized(gameData)`, and reconnect
rebuilds the current prompt from data. Anything paused must therefore be **data in `GameData`**,
copied for simulations.

## Design

### Change 1: a single pipeline for leaving the battlefield

This mirrors how entering already works (`BattlefieldEntryRequest`, `BattlefieldEntryBatchSupport`):
choices are gathered while every card stays where it is, the pending batch lives in `GameData`, and
then everything is placed together.

`DepartureService.depart(gameData, DepartureBatch)` takes one batch of intents. Each intent holds the
permanent, the cause (destroy, sacrifice, state-based, other) and the default destination
(graveyard, or a specific player's graveyard). The batch is the CR 603.10a "same event". The service
runs four stages:

1. **Gather**: for destroys only, indestructible, regeneration, shield counters and the other
   destruction replacements that `GraveyardService.destructionReplacementOptions` collects. This is
   the same order `prepareRegenerationChoice` uses today, so a creature that will regenerate is never
   asked about dying.
2. **Choose**: for every object still leaving, the CR 616.1 options (`deathExileReplacementOptions`,
   plus "may" library replacements), asked in APNAP order. Answers are stored on the pending batch.
   While any answer is outstanding, `depart` returns a *pending* result and nothing has moved.
3. **Perform**: every object leaves together, with the snapshot that the `simultaneousDying*` maps
   hold today. Those maps become part of the batch.
4. **Report**: a `DepartureResult` per object: where it ended up (graveyard, exile, hand, library,
   still on the battlefield, or not there), its new card ID, its last-known power and toughness, and
   whether it was destroyed or sacrificed. Callers read this instead of a `boolean`.

When an answer arrives, the pending batch resumes at the stage where it stopped.

### Change 2: resolution in steps that pass results along

The rule is that **a pause may only happen at the end of a step**. A step is one effect in the
stack entry's effect list. `EffectResolutionService` already parks between effects, and
`StackEntry.spliceSequenceSteps` can insert steps.

- **Resolution scratchpad**: named results kept on `StackEntry` (for example `sacrificed`, `destroyed`
  and `power`), copied by the stack-entry copy constructor like `resolutionExiledCardIds`. A step's
  `DepartureResult` is written there.
- **"If you do" and "this way" steps** read the scratchpad. For example,
  `SacrificeSelfAndControllerDrawsEffect` becomes a sequence of the sacrifice-self effect and an "if
  it was sacrificed" condition wrapping the draw-cards effect. This follows AGENTS.md: combine
  existing effects rather than keeping bespoke classes. **CR 608.2c** (checked) is still followed
  because the steps run in the order written.
- **A step that hits a pending departure ends the step.** The next step stays parked until the batch
  completes.
- **Loops add themselves again.** Tyrant of Discord becomes one step: "sacrifice one at random; if it
  wasn't a land, splice this step in again".
- **Player picks write to the scratchpad**, and the next step removes what was picked.
- **Costs**: **CR 601.2h** (checked) lets non-random costs be paid in any order, so a sacrifice cost
  asks its CR 616.1 question when the player selects what to sacrifice. The answer is stored with the
  pending cast or activation, and the batch is performed during payment.
- **Mana abilities** (**CR 605.3a**, checked) ask through the existing nested prompt window
  (`InteractionState.openNestedPromptWindow`).

## Ground rules for every step

- Read `agent-docs/ARCHITECTURE.md` before changing engine code.
- Every new `GameData` or `StackEntry` field must be copied for simulations, and pending batches must
  survive a `GameSimulator` copy taken while a question is open.
- Battlefield list order matters (timestamps, trigger order, wire indices). Never re-add removed
  permanents to restore state; replay a snapshot, as the entry batch does.
- Each behaviour change gets a card test that resolves the case through the engine (no wiring tests).
- When a step converts a handler into existing effects, delete the bespoke effect/handler pair and
  update the `agent-docs/` effect references.
- Verify every CR number with the `rules` MCP before writing it.
- Run only the tests for the code you touched. Never run the full suite; ask the user to run it.
  Don't commit until the user says so.
- At the end of a step, set its row in **Status** to `done`. Record anything a later step needs to
  know in that step's **Findings**, and list the tests you had to change and why.

## Steps

### Step 1: Behaviour tests for the target rules

**Depends on:** nothing.

Write card tests that describe the correct behaviour, in the per-letter test modules:

- Kalitas, Traitor of Ghet with an opponent's Gravebane Zombie, and Murder on the Zombie. The Zombie's
  controller is asked "library or Kalitas" while Murder resolves. After the answer, the Zombie is
  in the chosen zone and was never on the battlefield after the event.
- Rest in Peace and Firestorm Phoenix, with a destroy-all spell that also kills another creature. One
  question; both creatures leave in the same event; a "whenever a creature dies" watcher sees the
  right count.
- An edict (Diabolic Edict) on the Zombie with Kalitas out: the sacrifice is reported as done after
  the answer, and "sacrifice, then …" follow-ups run only after it.
- A spell with a sacrifice cost (Bone Splinters or similar) sacrificing the Zombie with Kalitas out:
  the question is asked before payment, and the spell is cast correctly.
- Polymorph on the Zombie with Kalitas out, choosing library: the reveal sees the Zombie.
- Tyrant of Discord with an opponent's Gravebane Zombie and Kalitas: the loop continues after the
  answer.
- Wheel of Sun and Moon on a player whose creature dies with Kalitas out: the controller is offered
  both options.

Run only these tests. Leave each failing test in place with `@Disabled("leave-battlefield-redesign
step N")`, naming the step that fixes it. **Findings** lists which pass already.

**Done when:** the tests exist, each is either passing or disabled with its fixing step, and
Findings is filled in.

### Step 2: Departure model types

**Depends on:** 1.

Add the domain types with no behaviour change: `DepartureIntent`, `DepartureBatch` (intents, stage,
answers so far, snapshots), `DepartureResult`, and a `GameData` field for the pending batch. Add them
to the `GameData` copy and to `GameSimulator`'s copying, with a test that copies a game while a batch
is pending.

**Done when:** the types exist, they are copied for simulations, and nothing calls them yet.

### Step 3: `DepartureService` for state-based deaths

**Depends on:** 2.

Create `DepartureService` with the gather, choose, perform and report stages. Move the logic of
`awaitStateBasedDeathReplacementChoices`, `chosenStateBasedDeathReplacements`,
`stateBasedMayLibraryChoices` and the state-based regeneration pre-choice
(`prepareRegenerationChoice`) into it. Switch `StateBasedActionService` to one `depart` call per
**CR 704.3** check (checked). Resuming after an answer goes through the pending batch.

**Done when:** state-based deaths go through `DepartureService`, the old state-based fields are gone,
and the existing Kalitas, Gisa, Void Maw and Pulmonic Sliver tests pass.

### Step 4: Simultaneous removals and destroy batches

**Depends on:** 3.

Route `PermanentRemovalService.performSimultaneousRemovals` and `DestructionSupport.destroyBatch` /
`destroyBatchCollecting` through `depart`. Their callers get `DepartureResult`s, and
`destroyBatchCollecting`'s `died` list comes from the results. Callers that exile or bounce (exile-all,
convoke exile, return-to-hand) must not be asked death questions. Either give them a non-death cause or
leave them on the old path, and record which in Findings.

**Done when:** wraths and "sacrifice all" effects ask every question before anything leaves. The
Rest in Peace and Firestorm Phoenix test from step 1 passes.

### Step 5: Fold the leftover replacement workarounds into the choose stage

**Depends on:** 4.

Remove the "exile now, pick the rider later" path (`DeathExileRiderChoice`); riders are now chosen
before the event like every other option. Remove the split regeneration choice for non-state-based
destroys (`RegenerationShieldChoice` with `stateBasedDestruction = false`); it is answered in the
gather stage. Add Wheel of Sun and Moon (`RevealAndPutOnBottomOfLibraryInsteadOfGraveyardEffect`) and
any other "instead of the graveyard" sources found along the way as options. Check each against its
Oracle text and rulings.

**Done when:** the rider and regeneration workarounds are gone and the Wheel of Sun and Moon test
passes.

### Step 6: Legacy wrappers and the guard

**Depends on:** 5.

Make `removePermanentToGraveyard`, `sacrificePermanentToGraveyard`, `destroyPermanentToGraveyard`,
`tryDestroyPermanent` and `removePermanentToPlayerGraveyard` thin wrappers that build a one-intent
batch. When a wrapper's caller can't pause and an answer is needed, keep today's fallback (the
creature stays and the call returns `false`). Then:

- log a warning through `GameLogService` or the logger, naming the caller;
- make the test harness fail on that fallback when a test opts in;
- add an architecture test, next to `CardImmutabilityArchTest`, that freezes the current list of
  wrapper callers so new code must use `depart`.

**Done when:** the wrappers delegate, the guard exists, and the architecture test allows exactly the
current callers.

### Step 7: Resolution scratchpad and pausing at the end of a step

**Depends on:** 6.

- Add the resolution scratchpad to `StackEntry`, including its copy constructor.
- Teach `EffectResolutionService` that a step leaving a pending departure parks at the *next* index
  (`i + 1`) and does not rerun.
- Add the conditions later steps need ("if it was sacrificed / destroyed this way", "its last-known
  power"). Reuse existing conditional and amount effects first, and grep `agent-docs/` before adding
  new ones.
- Cover it with a two-step test: sacrifice, then draw, with a question in between.

**Done when:** a sequence step can pause on a departure and the next step resumes with the result.

### Step 8: Convert the "sacrifice this, then …" handlers

**Depends on:** 7.

Convert the source and linked-permanent handlers into sequences of existing effects that read the
scratchpad, then delete the bespoke classes. The handlers:

- `SacrificeSelfEffectHandler`
- `SacrificeSelfThenEffectHandler`
- `SacrificeSelfAndControllerDrawsEffectHandler`
- `SacrificeSelfAndDealDamageToDamagedPlayerEffectHandler`
- `SacrificeSelfAndReturnCardsExiledWithSourceEffectHandler`
- `SacrificeSelfAndTargetDiscardsPerPoisonCounterEffectHandler`
- `SacrificeSelfThenDestroyTargetEffectHandler` (Wasp of the Bitter End)
- `SacrificeEnchantedCreatureEffectHandler`
- `SacrificeEnchantedCreatureAndCreateTokenEffectHandler`
- `SacrificeEnchantedCreatureOnLeaveEffectHandler`
- `SacrificeEnchantedPermanentAndReattachSourceAuraEffectHandler`
- `SacrificeEquippedCreatureThenDrawPowerEffectHandler`
- `SacrificeGrantingEquipmentThenEffectHandler`
- `SacrificeGrantingPermanentAndControllerDrawsEffectHandler`
- `SacrificeTriggeringPermanentEffectHandler`
- `SacrificeTriggeringPermanentThenConjureDuplicateEffectHandler`
- `SacrificeUnless*EffectHandler` (the immediate branch)
- `SoulEchoUpkeepEffectHandler`
- `ChampionCreatureEffectHandler`
- `DestructionSupport.sacrificeSource`

Where a handler can't be expressed as a sequence, port it to `depart` directly and record why in
Findings.

**Done when:** none of these call the legacy wrappers, and they are removed from the architecture
test allowlist.

### Step 9: Convert the "destroy/sacrifice target, then …" handlers

**Depends on:** 8.

The same treatment for target-based handlers:

- `SacrificeTargetCreatureThenCreateTokenEffectHandler`
- `SacrificeTargetCreatureThenCreateTokensEqualToPowerEffectHandler`
- `SacrificeTargetPermanentThenDealPowerDamageToAnyTargetEffectHandler`
- `SacrificeTargetThenRevealUntilTypeToBattlefieldEffectHandler` (Reweave)
- `SacrificeTargetArtifactAndReturnTargetArtifactFromGraveyardEffectHandler`
- `SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffectHandler`
- `DestroyTargetThenRevealUntilTypeToBattlefieldEffectHandler` (Polymorph)
- `DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffectHandler`
- `DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffectHandler`
- `DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffectHandler`
- `ExchangeControlOfTargetPermanentsEffectHandler` (the fizzle sacrifice)
- `DestructionSupport.tryDestroyAndLog`

**Done when:** the Murder and Polymorph tests from step 1 pass and these files are off the
allowlist.

### Step 10: Convert the choice-completion paths

**Depends on:** 9.

The player's pick writes to the scratchpad (or straight into a departure batch), and the removal is
its own step. This covers:

- `PermanentChoiceBattlefieldHandlerService`: every sacrifice and destroy handler, including the
  legend rule, exploit, sacrifice-to-enter, land casualty and reattaching an Aura after a sacrifice
- `MultiPermanentChoiceHandlerService`: sacrifice-self-to-destroy, attacking-creature sacrifices,
  Scapeshift-style sacrifices, sacrifice-any-number
- `MayPenaltyChoiceHandlerService`: the sacrifice-unless branches
- `CardChoiceHandlerService` (~line 3261)
- `SacrificePermanentAndReturnTargetCardsFromGraveyardEffectHandler` (Victimize)
- `SacrificeCreatureSearchLibraryForCreatureToHandEffectHandler` (Garruk)
- `SacrificeCreatureThenMassDamageEqualToPowerEffectHandler` (Rupture)
- `TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffectHandler`
- `DestructionSupport.sacrificeAndLog`
- the pile-separation paths (`DestructionSupport.completePileSeparationStep2`, `MakeAnExampleEffectHandler`)

For the pile separations, the departure must finish before the next opponent's split begins.

**Done when:** the edict test from step 1 passes and these files are off the allowlist.

### Step 11: Loops and drained queues

**Depends on:** 10.

- Tyrant of Discord becomes a step that splices itself in again.
- `DestroyCombatOpponentsOfTargetAtEndOfCombatEffectHandler` puts all its victims in one batch.
- `StepTriggerService` (~5029, ~5102): ask before draining, or put unprocessed delayed actions back
  in the queue on a pause.
- `TurnCleanupService` (~183): don't clear the "sacrifice at next cleanup" flag before the departure
  is done.
- `RemoveLinkedPermanentEffectHandler`: break the link only once the removal happens.
- `TimmerianFiendsAnteExchangeEffectHandler`: port to `depart`, and stop `moveCardToGraveyard` from
  calling itself when the card is still on the battlefield.
- `BattlefieldPlacementService` (Sheltered Valley) and `LifeSupport.replaceLifeLossWithShield`: port
  to `depart`. Their victims are noncreatures today, so this is for consistency.

**Done when:** the Tyrant of Discord test passes and these files are off the allowlist.

### Step 12: Costs and mana abilities

**Depends on:** 11.

Sacrifice costs ask their CR 616.1 question when the player selects what to sacrifice, before any
payment. The answer is kept with the pending cast or activation, and payment performs the batch. This
covers:

- `SpellCastingService`: `paySingleSacrificeCost`, the sacrifice-all costs,
  `paySacrificeCreaturesForCostReduction`, flashback sacrifice costs
- `AbilityActivationService`: `sacrificePermanentAsCost`, ON_SACRIFICE abilities, graveyard-activated
  sacrifice costs
- `ActivatedAbilityExecutionService` (batched activation sacrifices, `SacrificeSourceEquipmentCost`)
- `AttackSacrificeCostService`
- `SequencePermanentSacrificeCostHandler`

Keep `AdditionalSpellCostService`'s rule: validate before anything is paid. Mana abilities that
sacrifice their source (`ActivatedAbilityExecutionService` ~1786) ask through the nested prompt
window before they add mana. Make sure the AI and the simulator can answer the new pre-payment
question.

**Done when:** the sacrifice-cost test from step 1 passes and these files are off the allowlist.

### Step 13: Make the guard strict and remove the fallback

**Depends on:** 12.

- The allowlist should now be empty. Delete the legacy fallback: the `DeathExileReplacementChoice`
  `stateBased = false` path, and "the creature stays and the call returns `false`". A death that
  needs an answer outside a departure batch becomes an engine error.
- Remove the `@Disabled` markers that remain from step 1 (they should all pass).
- Update `agent-docs/ARCHITECTURE.md` with the departure pipeline and the step-pause rule.
- Remove entry 34 from `missing-features-to-do.md`.

**Done when:** no legacy wrapper remains, the step 1 tests all run, and the docs are updated.

### Step 14: Move other pausing effects onto the step model (repeatable)

**Depends on:** 13.

Each run takes the next family of `rerunCurrentEffectAfterInteraction` users and moves it to "pause
only at the end of a step". Start with the families that have the most files, and finish with
`DamageJournal`. Each run records in Findings which family it moved and what is left. Mark the step
`done` only when no `rerunCurrentEffectAfterInteraction` remains.

## Findings

Steps add notes here for later steps, under their number.

### Step 1

- Already covered by existing tests: Murder (`KalitasTraitorOfGhetTest.murderAsksForTheDeathReplacementBeforeTheZombieLeaves`),
  Diabolic Edict (`KalitasTraitorOfGhetTest.edictAsksForTheDeathReplacementBeforeTheZombieIsSacrificed` and
  `edictChoiceAsksForTheDeathReplacementBeforeTheChosenZombieIsSacrificed`; the "sacrifice, then" follow-ups by
  the Twisted Justice, Devour Flesh and Mercy Killing tests in `GravebaneZombieTest`) and Wrath of God
  (`GravebaneZombieTest.wrathAsksBeforeAnyCreatureIsDestroyed`).
- Passing: `FirestormPhoenixTest.restInPeaceWrathAsksOnceThenBothCreaturesLeaveTogether`. Its "whenever a
  creature dies" watcher (Moonlit Wake) correctly counts 0: under Rest in Peace nothing dies.
- Passing: `BoneSplintersTest.sacrificeCostAsksTheDeathReplacementBeforePayment`. A single sacrifice cost
  already asks before payment, so step 12 needs a test that fails today, such as one through
  `SpellCastingService.paySacrificeCreaturesForCostReduction`.
- Disabled for step 9: `PolymorphTest.revealFindsGravebaneZombiePutIntoItsLibraryOverKalitas`. Polymorph logs
  "is destroyed" and reveals the library before the question is answered.
- Disabled for step 11: `TyrantOfDiscordTest.processRepeatsAfterEachDeathReplacementChoice` (two Zombies, so it
  doesn't depend on the random pick). The loop stops after the first answer.
- Disabled for step 5: `WheelOfSunAndMoonTest.dyingCreatureOffersWheelAndKalitasReplacements`. Confirmed that
  Wheel of Sun and Moon is never offered today: Kalitas applies silently.
  `PermanentRemovalService.deathExileReplacementOptions` must include it. The test picks "the option that
  isn't Kalitas" because Wheel's option label doesn't exist yet.
- `TimmerianFiendsAnteExchangeEffectHandler` recursing forever is still unconfirmed.

### Step 3

- `DepartureService.depart(GameData, DepartureBatch)` returns a sealed `Outcome`: `Pending()` (a question is
  open, nothing moved, the batch is in `gameData.pendingDepartureBatch`) or `Done(results, survivorIds)` (one
  `DepartureResult` per intent in intent order; `survivorIds` are what a destruction replacement kept, since
  "regenerated" and "removal failed" both report `BATTLEFIELD`). Resume by passing the pending batch back.
  Answers are recorded with `recordDestructionAnswer` / `recordReplacementAnswer` / `recordMayLibraryAnswer`.
- Choices raised by a batch carry `inDepartureBatch = true` (renamed from `stateBased` on
  `DeathExileReplacementChoice` and from `stateBasedDestruction` on `RegenerationShieldChoice`);
  `ChoiceHandlerService` routes them to `DepartureService`.
- Each permanent leaves through `PermanentRemovalService.departToGraveyard(gd, perm, intent, chosenReplacement)`.
  Its `DESTROY` / `SACRIFICE` / `OTHER` branches mirror the old public wrappers but no batch uses them yet.
- Perform still publishes the batch's snapshots through the eight `simultaneousDying*` maps (saved and
  restored around it). Step 4 can drop the maps once `performSimultaneousRemovals` and `destroyBatch` read
  from the batch.
- Gather uses `tryReplaceDestruction(perm, true)`. Steps 4/6 need a "can't be regenerated" flag on
  `DepartureIntent` (Wrath of God, `tryDestroyPermanent(..., cannotBeRegenerated)`) and must cover
  `cantBeAffectedByOwnEffects`, which `destroyPermanentToGraveyard` checks today.
- Only one batch can be pending. The state-based check skips while any batch awaits an answer and only
  resumes batches with a state-based intent; step 4 needs a resume hook for other batches (the input
  epilogue reaches only state-based actions today).
- Death log lines are written after the whole batch. `report` finds zones by card id, so a token sent to
  the graveyard reports `GRAVEYARD` until the next check removes it.
- Rules fixes: the state-based gather no longer offers shield counters (they replace destruction only "as
  the result of an effect", CR 122.1c), and Pyramids' land shield now replaces lethal-damage destruction
  even when it is the only option.
- Deleted: `awaitStateBasedDeathReplacementChoices`, `recordStateBasedMayLibraryChoice`,
  `destroyPermanentByStateBasedAction`, `GraveyardService.prepareRegenerationChoice`,
  `GameData.chosenStateBasedDeathReplacements` / `stateBasedMayLibraryChoices` / `chosenRegenerationShields`.
- Tests: `GameSimulatorTest.copyAnswersPendingStateBasedDeathQuestionIndependently` (passes) and
  `PyramidsTest.removesLethalDamageInsteadOfTheLandCreatureDyingToStateBasedActions` (fails with the other
  five `PyramidsTest` tests, which fail at HEAD because Pyramids' mode activation gets stuck on a
  `ColorChoice`). `StateBasedActionServiceTest`, `PermanentRemovalServiceTest`, `CombatDamageServiceTest`
  and one `InputCompletionServiceTest` test fail at HEAD with the same counts.

### Step 4

- Through `depart` now: `DestructionSupport.destroyBatch` / `destroyBatchCollecting` / `destroyBatchDestroyed`
  (via `departDestroyBatch`; intents `DepartureIntent.destroy(id, cantBeRegenerated)`; `died` = results that
  went to a graveyard plus tokens that ended in no zone), `performSimultaneousSacrifice` (via
  `sacrificeTogether`), `SacrificeEachMatchingPermanentEffectHandler`, Make an Example's pile, the sacrificed
  pile of `completePileSeparationStep2`, and the Scapeshift and "sacrifice any number and record count"
  multi-choice completions.
- Still on `performSimultaneousRemovals` (now package-private, snapshots only): exile/bounce callers, which
  are not deaths (`ExileAllPermanentsEffectHandler`, the convoke exile, `ExileEachTargetPermanentThenReveal…`,
  `ReturnTargetAndAttachedMatchingToHandEffectHandler`, Warp World); sacrifice costs (step 12:
  `SpellCastingService` ×2, `ActivatedAbilityExecutionService`, `AbilityActivationService`,
  `SequencePermanentSacrificeCostHandler`); and `tryDestroyPermanent` (step 6).
  `askDeathReplacementsBeforeSacrifice` (one-at-a-time sacrificers like Torment of Hailfire) is unchanged.
- The `simultaneousDying*` maps stay: about 270 readers in 12 engine files. `perform` fills them from the
  batch snapshots and restores them; moving the readers is a separate change.
- API: `DepartureIntent.cantBeRegenerated` (+ `destroy(id, cantBeRegenerated)` / `sacrifice(id)`);
  `DepartureBatch.isStateBased()` / `withoutSurvivor(id)`; `depart(gd, batch, Hooks(beforePerform, afterEach))`
  (hooks are not stored on the batch); `departNow`; `discardAnsweredRerunBatch`.
- Gather only decides survivors now. Regeneration, shields and other destruction replacements are applied at
  the start of perform, after every answer is in, so a caller stopped for a question has changed nothing (state-based
  batches too). `cantBeAffectedByOwnEffects` (and `!triggeredAbilityCanMoveCreatureToken` for sacrifices) makes a
  permanent unaffected: no question, no snapshot, reported on the battlefield.
- A non-state-based batch still pauses through stop-and-rerun: it asks only inside `deathReplacementsCanRerunEffect`
  with no other batch pending, stores itself in `pendingDepartureBatch`, sets `rerunCurrentEffectAfterInteraction`
  and throws `DeathReplacementChoicePending`. The rerun builds the same intents and `depart` continues the
  answered batch if its intents are equal (a stale one is dropped; the `EffectResolutionService` /
  `runAskingDeathReplacementsFirst` windows drop an answered batch no rerun took). Answers already in
  `chosenDeathReplacementsForRerun` answer the batch's question too (otherwise Killing Wave, Fade Away and
  Tragic Arrogance asked twice). Outside the window nothing is asked and each member leaves the old way.
- For step 5: `DeathExileRiderChoice` and `RegenerationShieldChoice(inDepartureBatch=false)` are now only
  reached outside a window and by the old-path callers above.
- For step 6: `tryDestroyPermanent` still pre-applies shields and regeneration, then uses
  `performSimultaneousRemovals`; it can become a one-intent DESTROY batch. `afterEach` keeps the old
  `destroyBatch` "is destroyed" log for a removal that failed, and `sacrificeTogether` keeps the old
  `sacrificeAndLog` log and "sacrificed" triggers for a failed sacrifice; fix both when the wrappers change.
- For step 7: replace "throw and rerun" with "park at `i + 1` and record `Done.results()` on the parked entry";
  the batch then needs an owner instead of matching intents. `Hooks.afterEach` is a place to record results.
- Tests: new `GravebaneZombieTest.wrathAsksBothPlayersInApnapOrderBeforeAnyCreatureIsDestroyed`;
  `SacrificePermanentsEffectHandlerTest` now injects a real `DepartureService` and verifies `departToGraveyard`
  with a SACRIFICE intent. A sweep of about 1,960 card test classes covering every handler that calls the
  changed APIs had 65 failures, all also failing at the synced baseline. `PendingInteractionContractTest`
  and `CardImmutabilityArchTest` also fail at the baseline.

### Step 5

- New CR 616.1 options in `deathExileReplacementOptions` (`ChoiceContext.DeathExileReplacement.Kind`):
  `BOTTOM_OF_LIBRARY` (Wheel of Sun and Moon; never for tokens), `SHUFFLE_INTO_LIBRARY` (Progenitus,
  Blightsteel/Darksteel Colossus; Kalitas used to win silently), `VOID_COUNTER` (Dauthi Voidwalker), Valgavoth,
  Terror Eater's exile (reusing `EXILED_WITH_PERMANENT`), and Sanctifier en-Vec's colour exile as a `PLAIN` exile.
  A chosen library option is applied directly in `processGraveyardAndTriggers`, so the pipeline's fixed order
  can't override it. Non-death moves (discard, mill, resolving spells) still use the unchanged Wheel branch.
- `tryDestroyPermanent` is now a one-intent DESTROY batch through `departNow`, except
  `PermanentRemovalService.destroyPermanentInEnclosingBatch` (a permanent already snapshotted by an enclosing
  `performSimultaneousRemovals` group keeps the old inline path, so the group's batched death triggers aren't
  flushed early).
- Inside a rerun window, `askDeathReplacementsBeforeRemoval` and `removePermanentToGraveyard` also ask when
  every option exiles, so riders are chosen before the creature leaves.
- Removed: the two-argument `RegenerationShieldChoice` constructor and the dead multi-shield branch in
  `tryReplaceDestruction`.
- Still left for steps 6/12 (callers that can't pause): `DeathExileRiderChoice` when every option exiles
  (single-removal wrappers from unmarked handlers, triggered sacrifices and costs, `performSimultaneousRemovals`
  callers, `departNow` outside a window), and `RegenerationShieldChoice(inDepartureBatch = false)` from
  `DepartureService.perform` → `applyDestructionReplacement` outside a window and from
  `destroyPermanentInEnclosingBatch`. Step 6's guard should count both (`riderOptions` in
  `removePermanentToGraveyard`, and `queueDestructionReplacementChoice(…, false)` when `canAsk` is false).
- Not done: Yawgmoth's Testament (`playersPuttingCardsOnBottomOfLibraryInsteadOfGraveyardOrExileThisTurn`) is
  still applied silently in the graveyard pipeline (it would map to `BOTTOM_OF_LIBRARY`); a noncreature with the
  shuffle replacement (Legacy Weapon) still uses the after-the-move `GraveyardShuffleOrExileReplacementChoice`.
- Tests: `WheelOfSunAndMoonTest.dyingCreatureOffersWheelAndKalitasReplacements` enabled, plus
  `choosingKalitasOverWheelExilesTheCreature`; one "offered alongside Kalitas" test each in `SanctifierEnVecTest`,
  `DauthiVoidwalkerTest`, `ValgavothTerrorEaterTest`, `BlightsteelColossusTest`. Changed to the new rule (the
  creature stays until answered): two `KalitasTraitorOfGhetTest` tests (one renamed to
  `sacrificedCreatureStaysUntilItsControllerChoosesTheExileReplacement`), two `ProgenitusTest` tests (both
  failed at baseline, now pass), and `GravebaneZombieTest`'s Essence Vortex (`inDepartureBatch()`) and Valgavoth
  ward tests. A sweep of 1,838 card test classes found no new failures. Paths reaching `tryDestroyPermanent` only
  through choice-handler services (multi-choice destroys, delayed actions, `DamagePreventionService`, aura
  destroy in `GraveyardService`) are only partly covered by tests.

### Step 6

- `removePermanentToGraveyard` / `removePermanentToPlayerGraveyard` / `sacrificePermanentToGraveyard` /
  `destroyPermanentToGraveyard` build one intent (OTHER / OTHER to a player / SACRIFICE /
  `DepartureIntent.destroyAfterDestructionReplacements(id)`) and run it through the private `departAlone` →
  `departNow`. `destructionReplacementsApplied` makes gather skip an intent whose caller already applied
  indestructible and regeneration (otherwise regeneration could save a "can't be regenerated" target); steps
  8–12 should switch those callers to plain `DepartureIntent.destroy` and then drop it.
- A wrapper stays on the inline path (`departToGraveyard(..., null)`) inside an enclosing simultaneous-removal
  group, while another batch is pending (unless it is this call's answered rerun), or when the `Permanent`
  passed in isn't the one on the battlefield. `tryDestroyPermanent` still uses the narrower
  `containsKey(target)` enclosing check; consider switching it to `departsAlone`.
- Guard: `LegacyDepartureFallback.reached(gd, kind, permanent)` logs a warning naming the caller
  (`StackWalker`, fallback path only) for `RIDER_CHOSEN_AFTER_EXILE`, `DEATH_REPLACEMENT_ASKED_AFTER_EVENT`,
  `MAY_LIBRARY_ASKED_AFTER_EVENT` and `DESTRUCTION_REPLACEMENT_ASKED_AFTER_EVENT`. Tests opt in with
  `@FailOnLegacyDepartureFallback` (class or method); `KalitasTraitorOfGhetTest`, `KalitasBloodchiefOfGhetTest`
  and `GravebaneZombieTest` do. `PolymorphTest.destroyThatCannotAskFirstIsReportedAsLegacyFallback` asserts the
  guard fires for Polymorph: update or remove it in step 9. Step 13 makes `reached` throw.
- Ratchet: `LegacyDepartureWrapperRatchetTest` (application module) freezes the 97 current callers of the
  public wrappers (`OuterClass.method`, lambdas folded into their method). ArchUnit 1.3.0 can't read Java 25
  class files, which is why `CardImmutabilityArchTest` fails, so it reads bytecode with the JDK class-file API.
  The allowlist is the work list for steps 8–12.
- Log fixes (CR 701.8a, 701.21a): a permanent the destruction or sacrifice couldn't touch is no longer logged as
  destroyed / sacrificed and no longer fires "sacrificed" triggers. Tests in `AGoodThingTest`.
- Inside replay windows a wrapper's single removal is now asked by its one-intent batch, so 16 assertions in
  12 card tests (Barrow Ghoul, Cyclone, Drake Familiar, Elder Spawn, Glint Hawk, Icy Prison, Lithophage,
  Masticore, Magmasaur, Phantasmal Forces, Indoraptor, Vexing Devil) now check `choice.inDepartureBatch()`
  instead of `choice.rerunsEffect()`.
- For step 7: pauses go through `DepartureService.paused` (sets `rerunCurrentEffectAfterInteraction`, throws
  `DeathReplacementChoicePending`); the rerun finds the batch by equal intents (`answeredRerunBatch`). Hooks
  (`DestructionSupport` log and trigger hooks, `departAlone`'s capture) are closures and don't survive a pause:
  derive logs and triggers from `DepartureResult`s or store what's needed on the batch. Nested `departNow` calls
  happen inside `perform` (e.g. an aura sacrificed during `applyDestructionReplacement`) and should stay
  immediate.

### Step 7

- First half done (ahead of steps 3–6, since it doesn't touch removal): `StackEntry.resolutionResults`
  (immutable `ResolutionResults(departures, pickedIds)`, shared by the copy constructor), written with
  `recordDepartures` / `recordPicks`. A removal step records its results even when nothing left (an empty
  list), so "if you do" never reads an older step's result.
- Conditions and amounts: `PermanentLeftThisWay.sacrificed()` / `destroyed()` (true wherever the permanent
  ended up; regenerated or indestructible is not destroyed), `PermanentLeftThisWayPower` /
  `PermanentLeftThisWayToughness`. `ConditionContext` carries the entry's `resolutionResults`; use
  `ConditionalEffect.unless(...)` for "if you do". Tests: `EffectResolutionServiceTest$ResolutionScratchpad`
  and `ResolutionScratchpadAmountTest` (stub removal handlers stand in for step 8).
- Pausing (second half): a batch started by a resolution step is owned by it (`DepartureBatch.ownerStackEntryId`,
  the parked entry's `getTargetableId()`; set with `ownedBy`). `EffectResolutionService` sets
  `GameData.stepDepartureEffect` around a top-level `handler.resolve` (same depth and entry conditions as the rerun
  window). After the handler returns, `DepartureService.awaitsStepDeparture` parks at `i + 1` (never a rerun). On
  resume `resolveEffectsLoop` first calls `finishStepDeparture`, which continues `depart`, records `Done.results()`
  with `recordDepartures`, and keeps the entry parked while a question is still open. Owned batches always ask,
  return `Pending` without throwing, and are skipped by the rerun matching (`answeredRerunBatch`,
  `discardAnsweredRerunBatch`, `answeredForRerun`, `departsAlone`).
- API for steps 8–10: `DepartureService.departAsStep(gd, entry, effect, batch)`; return right after it and never
  depart twice in one step. Outside the step window, or while another batch is pending, it falls back to
  `departNow` and still records results. Logs and triggers come from `stepHooks`, rebuilt per call: SACRIFICE
  logs "X is sacrificed." and runs `checkAllyPermanentSacrificedTriggers` inside the event (CR 603.10a
  look-back; before, `SacrificeSelfEffectHandler` fired them after the removal); DESTROY logs `isDestroyed`.
  Callers that log "Player sacrifices X" or credit another sacrificing player need that stored on the batch first.
- `SacrificeSelfEffectHandler` uses `departAsStep` (except its token-rider form, still on the old wrapper and on
  the ratchet allowlist; step 8 should make that rider its own step). It keeps `AsksDeathReplacementsFirst` for
  the fallback. Dreamcatcher is converted to `MayEffect(SequenceEffect.of(SacrificeSelfEffect,
  ConditionalEffect.unless(PermanentLeftThisWay.sacrificed(), DrawCardEffect(1))))`; `SacrificeSelfThenEffect`
  stays for its other users.
- Not yet handled: `departAsStep`'s `departNow` fallback inside an enclosing simultaneous-removal group would flush
  that group's batched triggers early (`departsAlone` avoids it; `departAsStep` doesn't yet). An owned batch whose
  entry never resumes (game over, entry removed while parked) stays pending and blocks state-based deaths;
  step 11 or 13 should clean it up.
- Tests: `DreamcatcherTest` (question ends the sacrifice step and the draw waits; Kalitas exile still counts as
  sacrificed; no sacrifice, no draw), `GameSimulatorTest.copyFinishesAPendingStepDepartureAndResumesTheNextStep`,
  `EffectResolutionServiceTest$ResolutionScratchpad.stepEndingOnAPendingDepartureParksAtTheNextStep` and
  `resumeStaysParkedWhileTheStepDepartureIsUnanswered`. Sweeps of about 520 `SacrificeSelfEffect` /
  `SacrificeSelfThenEffect` / may-sequence card test classes found no new failures.
- The other `EffectResolutionServiceTest` tests fail at HEAD: its `DamageSupport` mock returns false from
  `beginDamageEvent`, so every resolution parks at once. The new nested class stubs it for itself.

### Step 8

- All 20 listed handlers are off the legacy wrappers and the ratchet allowlist (23 entries removed: every handler
  plus `DestructionSupport.sacrificeSource`; the allowlist now has 74 callers).
- New pieces: `StepDepartureSupport` (normalfx) with `sacrifice` (nothing follows), `sacrificeThenIfSacrificed`
  (an "if you do" payload: dispatched at once when the departure is done, otherwise inserted after the step as
  `ConditionalEffect.unless(PermanentLeftThisWay.sacrificed(), payload)`) and `sacrificeThenContinue` (the handler
  implements `ContinuesAfterDeparture`; its `continueAfterDeparture` runs at once, or as an inserted internal
  `ContinueAfterDepartureEffect` step). Follow-ups read the scratchpad, never state captured before the departure.
  `DepartureResult` gained `lastKnownControllerId` (also set for a permanent the event couldn't touch) for "its
  controller draws" (Soul Ransom, Hold for Ransom) and "the player who sacrificed it creates" (Basalt Golem's token
  rider on `SacrificeSelfEffect`).
- Converted to existing effects and deleted: `SacrificeSelfThenDestroyTargetEffect` + handler (Wasp of the Bitter
  End is now `MayEffect(SacrificeSelfThenEffect(DestroyTargetPermanentEffect(PermanentIsCreaturePredicate)))`).
- Kept as card-facing records, handlers ported: `SacrificeSelfThenEffect` (about 44 cards, and read by
  `TriggeredAbilityQueueService`, `MayEffectHandler`, `DrawService` (Booby Trap), `Card` target registration and
  two handlers that dispatch it nested); `SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect`,
  `SacrificeSelfAndReturnCardsExiledWithSourceEffect`, `SoulEchoUpkeepEffect`, `SacrificeEnchanted*`,
  `SacrificeGrantingPermanentAndControllerDrawsEffect`, `SacrificeTriggeringPermanentEffect` and
  `ChampionCreatureEffect` (engine services construct or inspect them); the rest because no existing effect does
  their follow-up (draw for the sacrificing player, Prototype X-8's copy profile, Spare Dagger's reflexive trigger,
  Soul Ransom). Merging those is optional cleanup, not needed for the guard.
- Ported to `depart` directly, not as a step:
  - `SacrificeEnchantedPermanentAndReattachSourceAuraEffectHandler` (Nettlevine Blight, Breath of Fury), single
    destination path: `departNow` with no hooks. The Aura is attached right after the sacrifice, but
    `departAsStep`'s finish removes orphaned Auras first, so a pause would bin the Aura. A question there stops and
    reruns the handler (it changed nothing yet). The several-destinations path is still the choice completion
    `handleReattachSourceAuraAfterSacrifice` (step 10). Step 13/14 needs a way to finish a step departure without the
    orphan sweep, or to attach inside it.
  - `DestructionSupport.sacrificeSource`: `departAsStep(gd, entry, null, …)` (always `departNow`), since the
    forced-cost fallback effects run in one synchronous loop, also from choice completions with synthetic entries.
- Nested dispatches (`RemoveCounterOrSacrificeSelfThenEffectHandler`, `SacrificeSelfThenDealDamageToEnteringCreatureEffectHandler`,
  `DestructionSupport` calling `SacrificeEnchantedCreatureEffectHandler` for Soul Tithe) are not the step, so they
  never pend: `departNow` asks and reruns inside a marked handler's window, otherwise the old fallback applies.
- Rules fixes along the way: Furnace Scamp and Whispering Specter now deal damage / force discards only if the
  creature was sacrificed ("if you do"); Soul Echo, Champion, the four `SacrificeUnless*` immediate branches,
  `SacrificeTriggeringPermanentEffect`, Parasitic Implant, Animate Dead-style leave sacrifices and Nettlevine Blight
  now fire "whenever you sacrifice" triggers (CR 701.21a sacrifices); `SacrificeSelfThenEffect` checks
  "can't be sacrificed"; Soul Echo checks that its controller still controls it. Reason logs ("X controls no
  Ys.", "does not have enough …s to discard.") are now separate from the departure's "X is sacrificed." line.
- Tests: new `ImpalerShrikeTest.deathReplacementQuestionEndsTheSacrificeStepBeforeTheDraw` (the "if you do" path,
  Kalitas exile still counts as sacrificed) and `LimDLsPaladinTest.deathReplacementQuestionComesBeforeTheDraw` (the
  continuation path: the draw takes the old top card after the Paladin went to the bottom). Changed:
  `HiddenHorrorTest` (log assertion split into the reason line and "Hidden Horror is sacrificed.") and the
  `DepartureResult` constructors in `EffectResolutionServiceTest` / `ResolutionScratchpadAmountTest`. A sweep of 282
  card test classes using the touched effects (1,048 tests), plus `LegacyDepartureWrapperRatchetTest`,
  `GameSimulatorTest`, the scratchpad tests and the destroy/sacrifice handler unit tests, had no new failures. Failing
  at baseline too: `AjanisLastStandTest`, `AngelicRenewalTest`, `BasaltGolemTest`, `BloodSpeakerTest`,
  `BreathOfFuryTest` (6), `CheeringCrowdTest` (7), `LavabrinkFloodgatesTest`, `ParasiticImplantTest` (2),
  `PhantasmalForcesTest`, `PreponderantPearlTest`, `PrototypeX8Test`, `SupremeExemplarTest` (2),
  `DestroyAllPermanentsEffectHandlerTest` (12), `SacrificePermanentsEffectHandlerTest` (6).

### Step 9

- All 12 listed callers are off the legacy wrappers and the ratchet allowlist (now 62 callers).
- New pieces: `StepDepartureSupport.depart` (any batch, nothing follows), `destroyThenContinue` and the general
  `departThenContinue` (`sacrificeThenContinue` delegates to it). `DepartureResult.lastKnownCardTypes` (from the
  snapshot's last-known card, CR 608.2h) for Reweave's "shares a card type with the sacrificed permanent"; a
  nine-argument constructor without it keeps the test helpers unchanged. `departAsStep`'s hooks now also log
  "is indestructible" for a DESTROY intent (as `departDestroyBatch` does), and `DepartureService.perform` wraps a
  batch of two or more permanents in one `beginPermanentLeaveBatch` / `endPermanentLeaveBatch`, so "whenever one or
  more ... leave" watchers see the whole event (this replaces Corrosive Ooze's own wrapping and now also covers
  wraths; nesting inside `sacrificeTogether`'s wrap is harmless).
- Converted to step departures with `ContinuesAfterDeparture`: Polymorph (`DestroyTargetThenRevealUntilType…`; the
  reveal uses the result's `lastKnownControllerId`, also set when the target was indestructible), Reweave / Shape
  Anew, Sarkhan the Mad's -2, Mercy Killing (power from `lastKnownPower`, or the current power of a creature that
  stayed), Arcum Dagsson, Field of Ruin / Demolition Field (searchers from the last controller), Goblin Vandal /
  Floral Spuzzem (no combat damage after the destroy). Nothing follows: Corrosive Ooze (all Equipment in one DESTROY
  batch) and the exchange fizzle sacrifice (`StepDepartureSupport.sacrifice`). The "Player sacrifices X." logs of the
  target handlers are now the departure's "X is sacrificed."
- Ported to `depart` directly, not as a step (`departAsStep(gd, entry, null, …)`, i.e. `departNow`; both handlers
  keep `AsksDeathReplacementsFirst`, so a question stops and reruns them before anything changed):
  - Goblin Crash Pilot (`SacrificeTargetPermanentThenDealPowerDamageToAnyTarget…`): its reflexive ability needs the
    sacrificed creature's `Permanent` as the damage source, which a paused step can't carry. The power and "was a
    creature that was sacrificed" now come from the `DepartureResult`.
  - Goblin Welder: "simultaneously sacrifices … and returns", and whether the return is allowed (a sacrificed
    Grafdigger's Cage still stops it) is decided as the event happens. Steps 13/14 need either the return inside the
    departure event or a way to carry that check across a pause.
- `DestructionSupport.tryDestroyAndLog` now departs a one-intent DESTROY batch through `departDestroyBatch` (logs
  and indestructible log as before, orphaned Auras removed only when destroyed). It still can't end a step, since
  its ~55 callers go on in the same handler; it asks inside a marked handler's rerun window, else the old fallback
  applies. It no longer has `tryDestroyPermanent`'s special case for a permanent already snapshotted by an
  enclosing `performSimultaneousRemovals` group (`destroyPermanentInEnclosingBatch`); no path was found that calls
  it inside such a group (the group callers exile, bounce or pay sacrifice costs), but steps 10–13 should keep it
  in mind. Its callers can become step departures one by one.
- Rules fixes: Reweave reveals only if the permanent was sacrificed ("If the player does"), using its last-known
  card types; Polymorph and Field of Ruin ask CR 616.1 questions before the target leaves (they weren't marked, so
  they reached the legacy fallback); Corrosive Ooze's Equipment are destroyed as one event.
- Tests: enabled `PolymorphTest.revealFindsGravebaneZombiePutIntoItsLibraryOverKalitas`; replaced
  `destroyThatCannotAskFirstIsReportedAsLegacyFallback` (it asserted the fallback fired) with
  `destroyAsksFirstAndTheRevealWaitsForTheAnswer` (no fallback, the reveal waits). New
  `ReweaveTest.revealWaitsForTheReplacementChoiceBeforeTheSacrifice`. Mock-based unit tests switched from verifying
  `tryDestroyPermanent` to a real `DepartureService` and verifying `departToGraveyard` with a DESTROY intent:
  `DestroyTargetPermanentEffectHandlerTest`, `DestroyAttachmentsOnTargetCreatureEffectHandlerTest` and
  `DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffectHandlerTest` (whose targets are now on the
  battlefield; its constructor changed). A sweep of 1,744 card test classes using the touched effects and
  `DestructionSupport` destroy helpers (15,552 tests), plus `LegacyDepartureWrapperRatchetTest`, `GameSimulatorTest`,
  the scratchpad tests and the destroy/sacrifice handler unit tests, had no new failures; the 91 failures in 43
  classes (Ordeal cycle, `PyramidsTest`, `BreathOfFuryTest`, `AlphaDeathclawTest`, `FloralSpuzzemTest`,
  `FieldOfRuinTest`, …) fail at baseline too, and `EffectResolutionServiceTest`, `PermanentRemovalServiceTest`,
  `DestroyAllPermanentsEffectHandlerTest`, `SacrificePermanentsEffectHandlerTest` and six
  `DestroyTargetAndEachPlayerSearches…HandlerTest` search tests fail at baseline with the same tests.
  `OrzhovCharmTest$BounceMode.doesNotBounceOpponentAura` is flaky (player2's random opening hand may hold a
  Pacifism).

### Step 10

- All 31 listed callers are off the legacy wrappers and the ratchet allowlist (now 31 callers, all for steps 11–12:
  costs, loops, delayed actions, ante, Sheltered Valley, `LifeSupport`, world rule, roles, Sagas, cleanup). The
  ratchet's "main classes are scanned" check now looks for a `SpellCastingService.` caller, since no
  `DestructionSupport` method calls a wrapper any more.
- How completions pause: not through the scratchpad. A choice completion has no resolution step of its own, so each
  converted completion runs inside the existing replay window (`PermanentChoiceHandlerService.runAskingDeathReplacementsFirst`):
  it changes nothing before its departure, a CR 616.1 question stops it, and the same answer is completed again once
  the last answer is in. Its departure is a `DepartureBatch` through `departNow`. `StackEntry.recordPicks` is still
  unused; moving completions onto real steps (pick to the parked entry's scratchpad, removal as an inserted step) is
  step 14 work, together with retiring the replay windows.
- `DestructionSupport.sacrificeAndLog` (about 60 callers) is now a one-permanent `sacrificeTogether` batch and returns
  its `DepartureResult`. It logs "P sacrifices X." and fires "whenever you sacrifice" triggers only when the sacrifice
  happened (CR 701.21a). It asks only inside a window, like `tryDestroyAndLog`.
- Newly inside a replay window, so they now ask before the permanent leaves (they reached the legacy fallback before):
  the legend rule (CR 704.5j; the others leave as one OTHER batch), a declined champion (`completeKillingChoice` gets a
  `null` permanent id), Sacred Mesa's "sacrifice another or this", sacrifice-to-enter and land casualty when not
  declined, the sacrifice before an Aura is reattached, the attack sacrifice cost (one batch), the unblocked-attacker
  "destroy the chosen permanent" and the "sacrifice this to destroy" multi-choice, and pile separations whose chosen
  pile is sacrificed or destroyed (Make an Example, Liliana of the Veil, Do or Die) through
  `MayAbilityHandlerService.completeKillingPileChoice` and a new `GameData.PileChoiceReplay`. The pile completions
  now peek the `PendingPileSeparation` and poll it in the batch's `beforePerform`, so a replay finds it again; Make an
  Example's next opponent is prompted only after the pile has left.
- Two removals in one completion ("sacrifice it; if you do, destroy ..."): after the first departure, a second window is
  opened with a replay of only the rest (`MultiPermanentChoiceContext.SacrificeSelfToDestroy.sourceSacrificed`). Nested
  `runAskingDeathReplacementsFirst` calls work because the inner one owns the question; the outer one doesn't drop the
  pending batch, since it is still awaiting its answer. Use this pattern for later completions with sequential removals.
- A completion that has no departure on some branch must close the window itself (`deathReplacementsCanRerunEffect =
  false`) before the epilogue, or a removal in the epilogue would stop and replay the whole completion. Logs a replayed
  completion writes must come after its departure (or in `beforePerform`), or a replay writes them twice (the "declines
  to ..." lines of the `SacrificeUnless*` may handlers, the champion line, the pile lines).
- Already in windows and only moved off the wrappers: the other `PermanentChoiceBattlefieldHandlerService` sacrifices
  (now `sacrificeAndLog`; exploit, divided damage, power damage, life-and-draw, boost, keyword, sized token, tokens per
  toughness, search library, "sacrifice a permanent, then"), the four `MayPenaltyChoiceHandlerService` sacrifice-unless
  branches, `handleSacrificeDamagedPlayerControlsPermanent`, attacking-creature sacrifices (now one `sacrificeTogether`
  batch instead of asking ahead and sacrificing one by one), Victimize, Garruk, Rupture (power from the result's
  `lastKnownPower`) and `TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffect`. Rules fixes: those sacrifices now fire "whenever you
  sacrifice" triggers (several didn't), and Victimize, Garruk and the subtype tokens follow "if you do".
- Still a fallback, off the wrappers: `CardChoiceHandlerService` sacrifices a creature put onto the battlefield with an
  unpayable "sacrifice it unless you pay" cost (CR 118.6, Flash with a creature that has no mana cost) through
  `sacrificeAndLog`; it has just entered, so the completion can't be redone and nothing is asked. It is now a real
  sacrifice (it used to be a plain move to the graveyard).
- Found along the way (step 8 leftover, fixed): `StepDepartureSupport.sacrificeThenIfSacrificed` resolved its payload as
  a nested dispatch when the sacrifice needed no question, so Blind Zealot's "if you do, destroy" (its
  `forDamagedPlayer` form is a `SacrificeSelfThenEffect`) reached the legacy fallback. When the sacrifice is the
  resolution step itself the payload is now always inserted as the next step, so its removal gets its own window.
  The `SacrificeSelfToDestroy` multi-choice context is reached only when `SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect`
  resolves unconverted.
- Rules gap noted, not fixed: Make an Example says "each opponent sacrifices the creatures in their chosen pile", one
  event after every pile is chosen; the engine sacrifices each opponent's pile before the next opponent separates. This
  matches the step's wording but is wrong with two or more opponents. Fixing it means collecting every chosen pile and
  departing one batch at the end.
- Tests: new `KalitasTraitorOfGhetTest.legendRuleAsksTheDeathReplacementBeforeTheOtherLegendLeaves`,
  `ChangelingHeroTest.decliningToChampionAsksTheDeathReplacementBeforeTheSacrifice`,
  `BlindZealotTest.destroyAsksTheDeathReplacementAfterTheSacrificeBeforeTheTargetLeaves` and
  `MakeAnExampleTest.chosenPileAsksTheDeathReplacementBeforeAnyOfItIsSacrificed` (all `@FailOnLegacyDepartureFallback`).
  The step 1 edict tests were already passing and still pass. Changed: `SacrificePermanentsEffectHandlerTest` (three
  auto-sacrifice tests now verify `departToGraveyard` with a SACRIFICE intent) and
  `SacrificeOtherCreatureOrDamageEffectHandlerTest` (injects a real `DepartureService`, verifies `departToGraveyard`),
  since `sacrificeAndLog` no longer calls the wrapper. A sweep of 1,206 card test classes (5,921 tests) for every
  handler calling `sacrificeAndLog` / `sacrificeTogether` / `sacrificeThenIfSacrificed` / the pile completions, every
  card using the touched choice contexts, and legend-rule, exploit, champion, casualty, pile, Kalitas, Wheel of Sun and
  Moon and Gravebane Zombie tests had 67 failures; 65 fail at baseline too (Cheering Crowd, Breath of Fury, Mantle of
  Leadership, Dauthi Voidwalker, ...). The other two (`LithophageTest`, the "declines to" line written before the
  question) were fixed by moving the reason logs after the departure. `LegacyDepartureWrapperRatchetTest`,
  `GameSimulatorTest` and the touched handler unit tests show no new failures (`EffectResolutionServiceTest`,
  `DestroyAllPermanentsEffectHandlerTest`, six `SacrificePermanentsEffectHandlerTest` tests, six
  `DestroyTargetAndEachPlayerSearches…` tests and `CardImmutabilityArchTest` fail at baseline with the same tests).

### Step 11

- All listed callers are off the legacy wrappers and the ratchet allowlist, plus two found on the way (Spinal Embrace's
  delayed sacrifice and `PermanentRemovalService.resolveDelayedPermanentAction`); the allowlist now has 20 callers.
- Tyrant of Discord (`TargetPlayerSacrificesRandomPermanentsUntilLandEffectHandler`) is a `ContinuesAfterDeparture` step:
  it picks one permanent at random and departs it with `sacrificeThenContinue`; `continueAfterDeparture` inserts the same
  effect again as the next step when a permanent was sacrificed whose last-known types (`DepartureResult.lastKnownCardTypes`,
  CR 608.2h) don't include land. A re-inserted step re-checks the target and "can't be made to sacrifice". This is the
  pattern for other "repeat this process" loops. Its log line is now the departure's "X is sacrificed." instead of
  "P sacrifices X.".
- Delayed sacrifices and destroys: `StepTriggerService` no longer removes anything while it drains. Every drained
  `DelayedPermanentAction` already becomes a stack trigger with one `ResolveDelayedPermanentActionEffect` step per permanent,
  so the "lose the rest of the queue on a question" problem no longer exists. `ResolveDelayedPermanentActionEffectHandler` now
  does SACRIFICE and DESTROY itself as step departures (`departThenContinue`). A trigger's run of consecutive steps with the
  same operation leaves as **one batch** in the run's first step ("sacrifice those tokens", Goblin Sappers' "destroy it and
  this creature"), so every CR 616.1 question comes before any of them leaves; the later steps of the run find their
  permanent among the recorded departures and do nothing. The continuation returns the exiled card of each sacrificed
  action (`PermanentRemovalService.returnExiledCardToBattlefield`, now public). `resolveDelayedPermanentAction` handles only
  the other moves (exile, hand, command zone, library top) and throws for SACRIFICE / DESTROY. Logs come from the step hooks,
  so `DESTROY_AT_END_STEP`'s " is destroyed at end step." is now " is destroyed.".
- `DestroyCombatOpponentsOfTargetAtEndOfCombatEffectHandler`: all victims are one DESTROY batch through
  `StepDepartureSupport.depart` (Venomous Breath, Glyph of Doom, Gaze of the Gorgon, Kjeldoran Frostbeast).
- Spinal Embrace (`SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffectHandler`): the delayed sacrifice is a
  `sacrificeThenContinue` step; the life gain follows only if it was sacrificed, from `lastKnownToughness()` (it used the
  toughness read before the sacrifice). It keeps `AsksDeathReplacementsFirst` for the `departNow` fallback.
- `TurnCleanupService.sacrificePermanentsFlaggedForCleanup`: no permanent on the battlefield normally carries the flag (a
  resolved spell turns the Mirage flash rider into a `DelayedCleanupTrigger` as it enters). A flag that is still set now puts
  the same kind of cleanup trigger on the stack (`SacrificePermanentsEffect` of that permanent, CR 514.3a) and is cleared when
  the trigger owns it, instead of sacrificing inline during cleanup.
- `RemoveLinkedPermanentEffectHandler`: SACRIFICE and SACRIFICE_PARTNER are `StepDepartureSupport.sacrifice` steps (they now
  also fire "whenever you sacrifice" triggers). The partner's link is still broken before the departure: it points at the
  permanent that has already left, so breaking it while the question is open can't lose a trigger, and the link is read when
  the partner itself leaves, so it can't be broken inside the event without a new hook. EXILE is unchanged.
- Ante exchanges: Timmerian Fiends' artifact (and the Fiends, if it is on the battlefield) leave as one OTHER batch straight
  to the new owners' graveyards (`DepartureIntent` with `graveyardOwnerId`), so a replacement that sends the artifact
  elsewhere (Kalitas) is no longer undone by the old "relocate from exile" step; `moveCardToGraveyard` no longer touches the
  battlefield, so the suspected self-recursion is gone. Tempest Efreet leaves the battlefield (if it is there) before the
  random reveal. Both effect handlers are now `AsksDeathReplacementsFirst` (nothing changes before the departure; the owner
  override written first is idempotent), and both may handlers run the decline through `answerAskingDeathReplacementsFirst`.
- Sheltered Valley (`BattlefieldPlacementService.applySacrificeOtherPermanentsWithSameName`) sacrifices its namesakes as one
  SACRIFICE batch through `departNow` (it was an OTHER removal that logged as a sacrifice); Lich's Duel Mastery
  (`LifeSupport.replaceLifeLossWithShield`) is a one-intent SACRIFICE batch. Both can't pause (an entry and a life loss in
  progress), so they ask only inside a window; their permanents are noncreatures with no death replacements today. Both use
  `@Autowired @Lazy` `DepartureService` fields so the hand-built unit-test constructors are unchanged.
- **Left on the allowlist and not in any step's list** (step 12 or 13 must take them): `StateBasedActionService.applyWorldRule`,
  `removeRedundantRoles`, `sacrificeCompletedSagas` (state-based actions, CR 704; they belong in the state-based departure
  batch), `GraveyardService.performUmbraArmorReplacement` / `performSacrificeAuraAndGrantIndestructibleReplacement` (run inside
  `DepartureService.perform` → `applyDestructionReplacement`; step 6 says nested `departNow` calls there should stay
  immediate) and `PermanentRemovalService.destroyPermanentInEnclosingBatch`. The other 14 are step 12's costs and mana
  abilities.
- Not done: the stale owned-batch cleanup from step 7 (an owned batch whose entry never resumes) is still open for step 13.
- Tests: enabled `TyrantOfDiscordTest.processRepeatsAfterEachDeathReplacementChoice`. New
  `SpinalEmbraceTest.delayedSacrificeAsksTheDeathReplacementBeforeTheLifeGain` and
  `GoblinSappersTest.destroysBothTogetherAfterTheDeathReplacementChoice` (the Sappers stays on the battlefield while the
  Zombie's question is open; Kalitas exiles the Sappers in the same event), both `@FailOnLegacyDepartureFallback`. Changed:
  `TimeStopTest.processesPermanentsScheduledForNextCleanup` (the flagged permanent is now sacrificed by a cleanup trigger on
  the stack, so the test resolves it). A sweep of 1,518 card test classes (every card test mentioning end step, end of combat,
  upkeep or cleanup together with sacrifice or destroy, plus every card using a touched effect), with
  `LegacyDepartureWrapperRatchetTest`, `GameSimulatorTest` (35/35), `TurnProgressionServiceTest`, `TurnCleanupServiceTest`,
  `StepTriggerServiceTest`, `ResolutionScratchpadAmountTest`, `EffectResolutionServiceTest`, `PermanentRemovalServiceTest` and
  `BattlefieldPlacementServiceTest` (11,233 tests), had 237 failures in 61 classes; the same 237 fail at the baseline, none new.

### Step 12 (first part)

- Done: every sacrifice a **permanent's activated ability** makes as it is activated asks its CR 616.1 questions before
  anything of it leaves, with no legacy fallback: the permanents chosen for its sacrifice costs
  (`pendingActivationCostSacrificeIds`), a `SacrificeSelfCost` source, a `SacrificeSourceEquipmentCost` Equipment and a
  mana ability's `SacrificeSelfEffect` source. Mana abilities are covered, also when activated during a payment prompt
  (CR 605.3a). Also `ON_SACRIFICE` abilities (`AbilityActivationService.sacrificePermanent`, Aura of Silence).
  Off the ratchet allowlist (now 16 callers): `AbilityActivationService.sacrificePermanent`,
  `ActivatedAbilityExecutionService.completeActivationAfterCosts`, `doResolveManaAbility` and `sacrificeCostPermanents`.
- How it pauses (a continuation, not "before any payment"): `completeActivationAfterCosts` is the single point every
  activation reaches once its other costs are chosen and its mana is paid, and the sacrifices come after it starts. At
  its top, `askCostSacrificesFirst` builds a SACRIFICE batch of everything it will sacrifice and calls the new
  `DepartureService.askBeforePayment`: the batch's choose stage runs with asking allowed and, when a question opens,
  stays in `pendingDepartureBatch` (not owned, not state-based); nothing has been tapped or paid by this method yet, so it
  stores a `GameData.ActivationCostReplay` (the method's arguments, the permanent by id) in
  `pendingDeathReplacementChoiceReplay` and returns. The answer handlers already call
  `completeChoiceAgainAfterDeathReplacements` once no answer is outstanding; its new cases take the batch's answers into
  `chosenDeathReplacementsForRerun` (`DepartureService.takeAnswersForPayment`, which drops the batch), complete the
  activation again (`completeActivationAfterCostSacrificeAnswers`) and then remove the answers the payment didn't
  consume. CR 601.2h lets the costs be paid in any order, so paying the mana first and asking at the sacrifice is
  rules-correct; what matters is that the rest of the payment can't fail, which the activation's validation already
  ensures. The ability isn't on the stack while the question is open.
- The sacrifices themselves are now `DepartureBatch`es through `departNow` (`sacrificeCostPermanents` is one batch of
  the source and the chosen permanents; the Equipment and the mana ability's source are one-intent batches). Outside a
  window `choose` doesn't ask but consumes `chosenDeathReplacementsForRerun`, so the replay needs no window. Their
  "whenever you sacrifice" triggers and "P sacrifices X." logs now come from `Hooks.afterEach` and only for a permanent
  that left (CR 701.21a).
- Nested prompts: `PermanentRemovalService.openDeathReplacementChoice` now nests a death replacement prompt in front of
  the active one inside a nested-prompt window (`nestsNewPrompt`), as `AnyColorManaChoiceSupport.beginOrQueueChoice`
  does. Completing the stopped activation first resumes the suspended payment prompt and reopens the window around the
  completion, as when the activation began; without that, the completion re-began the payment's may prompt while it was
  also still suspended, and paying it brought it back a second time.
- Replays must be data: the AI simulator answers prompts through its own Spring context (`HeadlessSimulationContext`),
  so a replay holding a lambda over the harness's services fails silently in a copy (`applyAction` swallows
  exceptions). Spells will need their own replay record for the same reason.
- **Left for the next run(s)** (still on the allowlist): `SpellCastingService` (all eight callers: `paySingleSacrificeCost`
  and the other spell sacrifice costs), `AbilityActivationService.sacrificePermanentAsCost` (graveyard abilities, which
  sacrifice as each permanent is chosen), `AttackSacrificeCostService.sacrificeMatching` and
  `SequencePermanentSacrificeCostHandler` (`performSimultaneousRemovals` for graveyard sequence costs), plus the six
  non-cost callers step 11 listed (they belong to step 13). Spells are the hard part: `playCardInternalImpl` (about 4,000
  lines, 39 parameters) removes the card from hand, pays commander tax and buyback discards *before* its per-type
  validation, then pays sacrifices between other payments, and has no continuation point like
  `completeActivationAfterCosts`. A workable plan: compute the cost's sacrifice ids where the request is read (only
  unambiguous fields: `sacrificePermanentId` means a tap or bounce for some costs), ask with `askBeforePayment` at the
  very start of `playCardInternal` before anything is paid, and store a data replay of the cast request. The step 1
  `BoneSplintersTest.sacrificeCostAsksTheDeathReplacementBeforePayment` still passes only through the legacy fallback
  (it isn't `@FailOnLegacyDepartureFallback`); mark it once spells are done.
- Tests: new `NantukoHuskTest.sacrificeCostAsksTheDeathReplacementBeforePayment`,
  `AshnodsAltarTest.sacrificeAsksTheDeathReplacementBeforeAddingMana` and
  `AshnodsAltarTest.deathReplacementQuestionNestsInsideAPaymentPrompt` (Force Spike's payment prompt), all
  `@FailOnLegacyDepartureFallback` and all failing at the baseline; `GameSimulatorTest.copyCompletesAnActivationStoppedBeforeItsSacrificeCost`.
  No existing test changed. A sweep of 2,083 card test classes (18,360 tests: every card using a sacrifice
  cost effect or `ON_SACRIFICE`, plus every Kalitas and Gravebane Zombie test), with `LegacyDepartureWrapperRatchetTest` and
  `GameSimulatorTest` (36/36), had 55 failures in 34 classes; the same 55 fail at the baseline, none new.

### Step 12 (second part: spells cast from hand)

- Done: a spell cast from hand (every `playCard*` entry point that reaches the 39-argument
  `SpellCastingService.playCardInternal`: plain casts, gift, splice, evoke, alternative costs, adventure, prowl,
  overload, conspire, casualty) asks the CR 616.1 question of every permanent it sacrifices as a cost before any cost
  is paid. Off the ratchet allowlist (now 8 callers): all eight `SpellCastingService` callers. The ratchet's "main
  classes are scanned" check now looks for a `StateBasedActionService.` caller.
- Where it asks: not at the very start of `playCardInternal` (the sketch from the first part), because which request
  field is a sacrifice depends on the cost structure the impl only works out during validation. Both cast branches of
  `playCardInternalImpl` (permanent spells, instants/sorceries) validate every non-mana cost before paying any ("CR
  601.2h: a cast either completes or leaves the game state untouched"); right after that block,
  `askCastCostSacrificesFirst` asks `DepartureService.askBeforePayment` for `castCostSacrificeIds`: the sacrifices
  `payAdditionalCosts` / `payAllSacrificeCosts` make (sacrifice a creature / permanent, the "or" costs when a permanent
  was chosen, sacrifice all creatures / permanents, multiple, any number, fraction, escalate), kicker and buyback
  sacrifices, creatures sacrificed to reduce the cost (Torgaar), an alternative cost's `SacrificePermanentsCost`,
  casualty, an imposed tax (Drought) and splice sacrifices. A question throws the private
  `CastStoppedForCostSacrificeAnswers`; the new `playCardInternal` wrapper catches it, puts the card back where it was
  (`restoreAttemptedCardAfterFailedCast`, as a failed cast does; the impl only removed it from hand by then) and stores
  `GameData.SpellCastReplay` (the request as plain data: lists and maps copied immutably, the card by id). Once every
  answer is in, `PermanentChoiceHandlerService.completeChoiceAgainAfterDeathReplacements` takes the answers
  (`payAgainAfterDeathReplacements`) and `SpellCastingService.completeCastAfterCostSacrificeAnswers` casts again; the
  sacrifices consume the answers, and a cast no longer legal fails with the card back in hand.
  `playCardAfterOpponentChosenTarget` carries its opponent-chosen player across a stop
  (`SpellCastReplay.withOpponentChosenTargetPlayerId`).
- It doesn't ask (the old fallback applies, reported by `LegacyDepartureFallback`) when the cast can't be stopped and
  made again: during a resolution (`currentlyResolvingControllerId`), with a parked resolution
  (`pendingEffectResolutionEntry`) or inside a rerun window, for a commander cast (its life tax is paid before
  validation), for a cast with `fromGraveyard` set, and when the mana pool, life, stack or game log changed since the
  impl started (`CastStartState`; e.g. the exile-from-hand alternative cost pays a mana portion during validation).
- Spell cost sacrifices are now `DepartureBatch`es through `departNow` (`sacrificeForSpellCost`): `paySingleSacrificeCost`
  (still sets `sacrificingAsSpellCost`), the sacrifice-all costs, the creatures sacrificed to reduce the cost (now one
  batch instead of one by one, so they leave as one event), alternative-cost, flashback and graveyard-cast sacrifices.
  Logs and "whenever you sacrifice" triggers come from `Hooks.afterEach`, only for a permanent that left, with the
  same cards the old code passed (snapshots computed before the batch).
- **Left for the next run(s)**: the other cast paths still sacrifice through `departNow` without asking first, so a
  death replacement there still reaches the legacy fallback: graveyard casts (`playFlashbackSpellFromLocation`:
  flashback, graveyard-cast permissions, harmonize, escape, Muldrotha-style casts; payments such as convoke taps,
  `consumeFreeCastFromBattlefield` and the cost-reduction sacrifice are interleaved with its validations, so it needs
  its own "validated, nothing paid" point), `playAdventureCardFromGraveyardInternal`, `playCardFromExileInternal`,
  `playCardFromLibraryTop` (`payTopLibraryAdditionalCosts`), the free casts (`payAdditionalCostsForFreeCast`,
  `paySacrificeCreatureCostForFreeCast`, cast during a resolution) and morph (`payMorphSacrificeCost`). Plus, unchanged
  from the first part: `AbilityActivationService.sacrificePermanentAsCost` (graveyard abilities sacrifice as each
  permanent is chosen, so they need the "choose all, sacrifice together after" shape `pendingActivationCostSacrificeIds`
  gives permanents), `AttackSacrificeCostService.sacrificeMatching` (its auto path, one at a time) and
  `SequencePermanentSacrificeCostHandler` (`performSimultaneousRemovals`), and the six non-cost callers for step 13.
- Tests: `BoneSplintersTest.sacrificeCostAsksTheDeathReplacementBeforePayment` is now `@FailOnLegacyDepartureFallback`
  and also asserts that the stack is empty, the card is still in hand and the mana unpaid while the question is open.
  New (both `@FailOnLegacyDepartureFallback`, both failing at the baseline):
  `TorgaarFamineIncarnateTest.costReductionSacrificeAsksTheDeathReplacementBeforePayment` (two creatures sacrificed for
  the cost reduction, the question before anything is paid, both still reduce the cost) and
  `SoulblastTest.sacrificeAllAsksTheDeathReplacementBeforePayment` (sacrifice all creatures; total power still 5);
  `GameSimulatorTest.copyCastsASpellStoppedBeforeItsSacrificeCost`. Changed: `SpellCastingServiceTest` injects a
  real `DepartureService` (and stubs `triggeredAbilityCanMoveCreatureToken`), and its two sacrifice-cost tests verify
  `departToGraveyard` with a SACRIFICE intent instead of `sacrificePermanentToGraveyard` (10 of its tests fail at the
  baseline with the same tests). A sweep of 1,248 card test classes (10,802 tests: every card using a spell or ability
  sacrifice cost effect, kicker, buyback, splice, casualty, emerge or the cost-reduction sacrifice, plus Drought and
  every Kalitas and Gravebane Zombie test) had 40 failures in 24 classes (Aang's Journey, Ana Battlemage, Archangel of
  Wrath, Dusk Rose Reliquary, Psychic Puppetry, Sloppity Bilepiper, ...); the same 40 fail at the baseline, none new.
  `LegacyDepartureWrapperRatchetTest` and `GameSimulatorTest` (37/37) pass.

### Step 12 (third part: graveyard and hand abilities, attack costs)

- Done: a graveyard or hand card's activated ability (Tymaret's return, Necrosavant, Whiteout, Metalwork Colossus,
  Edge of Autumn's cycling, ...) and the forced payment of an attack sacrifice cost (Exalted Dragon, Leviathan,
  Flooded Woodlands, Reclamation) ask the CR 616.1 question of every permanent they sacrifice before any of them
  leaves. Off the ratchet allowlist (now 6 callers, all step 13's non-cost callers):
  `AbilityActivationService.sacrificePermanentAsCost` (deleted) and `AttackSacrificeCostService.sacrificeMatching`.
  `SequencePermanentSacrificeCostHandler` no longer removes anything itself (its `performSimultaneousRemovals` path and
  `PermanentRemovalService` constructor argument are gone).
- Graveyard and hand abilities now use the "choose all, sacrifice together after" shape of a permanent's ability: every
  sacrifice cost handler chooses into `pendingActivationCostSacrificeIds` (`chooseSacrificeAsCost`), so a chosen
  permanent can't be chosen again and stays until the activation's completion. `sacrificeChosenCostPermanents` runs
  at the top of `completeGraveyardAbilityActivation`, `completeHandAbilityActivation` and
  `completeHandAbilityActivationAfterCosts` (before the life payment, the discard of a cycled card and the stack
  entry): it asks with `askBeforePayment`, and when a question opens stores `GameData.GraveyardAbilityCostReplay` /
  `HandAbilityCostReplay` (plain data; the card and ability by reference, the permanents by id in the pending list)
  and returns; `completeChoiceAgainAfterDeathReplacements` completes the activation again through
  `payAgainAfterDeathReplacements`. Otherwise the chosen permanents leave as one SACRIFICE batch, logging
  "P sacrifices X." and firing "whenever you sacrifice" triggers only for those that left. Mana, energy, mill and
  graveyard-exile costs are paid before the question (CR 601.2h, CR 602.2b allow any order; the activation's
  validation makes the rest unfailing), as for a permanent's ability in the first part. The suspend and
  exile-from-hand-mana-ability tails sacrifice without asking first (no card has a sacrifice cost there). A failing
  graveyard or hand activation (or cost-choice completion, or replay) drops the permanents it had chosen.
- Rules fix: a graveyard ability's cost-sacrifice triggers used to go on the stack below the ability; they are now
  deferred and put on the stack above it (CR 603.3), as hand abilities already did. Several sacrifices for one
  graveyard ability are now one event (they were, through `performSimultaneousRemovals`, only for an auto-paid
  `MultiplePermanentSacrificeCostHandler`).
- Attack costs: the forced path (no more candidates than required) no longer sacrifices one at a time. It runs
  `DestructionSupport.sacrificeTogether` inside `runAskingDeathReplacementsFirst` with a `MultiPermanentChoiceReplay`
  of a synthesized `SacrificeAttackCost` choice of those permanents, so a question stops the payment (the declaration
  is already committed and goes on as when a choice prompt opens) and the answer completes it through the same
  `handleSacrificeAttackCost` as a chosen payment.
- **Left for the next run(s)**: the cast paths listed in the second part (graveyard casts via
  `playFlashbackSpellFromLocation`, `playAdventureCardFromGraveyardInternal`, `playCardFromExileInternal`,
  `playCardFromLibraryTop`, the free casts, morph's `payMorphSacrificeCost` and commander casts) still sacrifice through
  `departNow` without asking first, so a death replacement there reaches the legacy fallback. They aren't on the
  allowlist; finishing them is what remains of step 12. The six non-cost allowlist callers stay for step 13.
- Tests: new (all `@FailOnLegacyDepartureFallback`)
  `TymaretTheMurderKingTest.graveyardAbilitySacrificeAsksTheDeathReplacementBeforeTheZombieLeaves` (auto-paid) and
  `chosenGraveyardAbilitySacrificeAsksTheDeathReplacementBeforeTheZombieLeaves` (chosen from a prompt),
  `EdgeOfAutumnTest.cyclingAsksTheSacrificedArborsDeathReplacementBeforeItLeaves` (Dryad Arbor with Necromancer's
  Magemark under an opponent's Kalitas; the card stays in hand until the answer),
  `ExaltedDragonTest.forcedAttackCostAsksTheDeathReplacementBeforeTheLandLeaves` and
  `GameSimulatorTest.copyCompletesAGraveyardActivationStoppedBeforeItsSacrificeCost`. No existing test changed.
  All four card tests fail at the baseline. A sweep of 897 card test classes (every card with a graveyard or hand
  activated ability, cycling, unearth, scavenge, embalm or eternalize, a sacrifice sequence cost or an attack sacrifice
  cost) had 31 failures in 15 classes (Despoiler of Souls, Phyrexian Dragon Engine, Geistblast, Resounding Silence,
  Priest of Forgotten Gods, ...); the same 31 fail at the baseline, none new. `LegacyDepartureWrapperRatchetTest` and
  `GameSimulatorTest` (38/38) pass.

### Step 12 (fourth part: the other cast paths)

- Done: the cast paths the second and third parts left ask the CR 616.1 question of every permanent they sacrifice
  as a cost before any of them leaves. Step 12 is finished; the ratchet allowlist still holds only step 13's six
  non-cost callers.
- Reused machinery: `askCastCostSacrificesFirst` (its gate `cannotBeMadeAgain`, the old `notFromHand`) now throws
  `CastStoppedForCostSacrificeAnswers` from every cast path, and each path's wrapper catches it and stores its own
  plain-data replay; the shared core `askCostSacrificesFirst` (refused while something resolves, a resolution is
  parked or a rerun window is open) also serves morph. Sacrifice ids are read with `addCastingCostSacrificeIds`,
  which takes ids from the selection the way `SacrificePermanentsCost` payments do.
  - **Graveyard casts**: the 19-argument `playFlashbackSpellFromLocation` is now a wrapper around
    `playFlashbackSpellFromLocationOnce` and stores `GameData.GraveyardCastReplay` (the card by id in its owner's
    graveyard); `completeGraveyardCastAfterCostSacrificeAnswers` casts again. The "validated, nothing paid" point is
    right after `planConvokeContributions`, before `consumeFreeCastFromBattlefield`, convoke taps and the cost
    payments; `graveyardCastCostSacrificeIds` lists the sacrifice/casualty/any-number additional costs, a forage
    paid with a Food, the cost-reduction creatures, printed flashback sacrifices (only when the printed flashback
    branch of `payFlashbackOrGraveyardCastCost` pays them), the card's `GraveyardCast` sacrifices and a filtered
    permission's sacrifices. The cost-reduction sacrifice (Torgaar through Muldrotha) used to be paid in the middle
    of the validations; it is now validated there (`validateSacrificeCreaturesForCostReduction`, its expected
    reduction applied for the checks in between) and paid after the question, so a later failed validation no
    longer leaves it paid.
  - **Adventure from a graveyard**: reached through `playFlashbackSpellFromLocation`, so it shares that replay;
    `playAdventureCardFromGraveyardInternal` takes the cast's `CastStartState` and asks before its mana payment
    (the direct `playAdventureCardFromGraveyard` passes `null` and has no sacrifice selection).
  - **Library top**: `playCardFromLibraryTop` (8 arguments) wraps `playCardFromLibraryTopOnce`, asks after the
    counter-cost validation and stores `GameData.LibraryTopCastReplay` (made again only while the card is still on
    top).
  - **Commander casts**: no longer excluded. `SpellCastReplay.commandCastCardId` names the commander, and
    `completeCastAfterCostSacrificeAnswers` sets `commandCastPlayerId` / `commandCastCardId` around the new cast, as
    `GameService.castCommander` does. A tax paid with life (Liesa) is paid before validation, so `CastStartState`
    refuses the stop there and the old fallback applies; no commander with that effect has a sacrifice cost.
  - **Free casts and casts from exile**: the exile cast path never sacrifices itself; its sacrifice costs, and those
    of every prepared free cast, are paid by the `FreeCastSacrificeCost` permanent choice completion, which is now in
    `PermanentChoiceHandlerService.killsBeforeChangingAnything` when the spell's remaining costs are all sacrifices
    (the same context also picks a permanent to return or to put counters on). An escalate sacrifice goes through
    `MultiPermanentChoiceContext.FreeCastTapCost` the same way. `ExileFreeCastQueueSupport.completeSacrificeCost` /
    `completeTapCost` close the window right after the payment, so finishing the cast and casting the next queued
    spell are never redone. The auto-paid "sacrifice all creatures you control" (Soulblast) asks with
    `SpellCastingService.askFreeCastSacrificeAllFirst` (`askBeforePayment` directly; the free cast happens during a
    resolution, so it pauses rather than stops) and pauses the cast like a chosen sacrifice, storing a
    `PermanentChoiceReplay` of a `FreeCastSacrificeCost` choice with no permanent, which
    `completeChoiceAgainAfterDeathReplacements` completes through `payAgainAfterDeathReplacements`. Those two
    replays hold a mutable `StackEntry`, so `GameData.copyDeathReplacementChoiceReplay` copies it for simulations.
  - **Morph** (CR 702.37e, a special action, CR 116.2b): `GameService.turnFaceUpInternal` asks
    (`SpellCastingService.askMorphSacrificeCostFirst`) right before the face-up mana payment, after any X choice,
    and stores `GameData.TurnFaceUpReplay`; `GameService.completeTurnFaceUpAfterCostSacrificeAnswers` turns it face
    up again (`PermanentChoiceHandlerService` reaches `GameService` through an `ObjectProvider`).
- Left (no card needs them): a commander tax paid with life together with a sacrifice cost, and the suspend and
  exile-from-hand-mana-ability tails from the third part. `payMorphSacrificeCost` still sacrifices its permanents one
  at a time (Skirk Volcanist's two Mountains are two events).
- Tests: new, all `@FailOnLegacyDepartureFallback` and all failing at the baseline:
  `CabalTherapyTest.flashbackSacrificeAsksTheDeathReplacementBeforePayment`,
  `TorgaarFamineIncarnateTest.graveyardCastCostReductionSacrificeAsksTheDeathReplacementBeforePayment` (Muldrotha)
  and `commanderCastCostReductionSacrificeAsksTheDeathReplacementBeforePayment`,
  `FutureSightTest.libraryTopSacrificeAsksTheDeathReplacementBeforePayment` (Bone Splinters from the top),
  `ViolentOutburstTest.cascadeSacrificeAsksTheDeathReplacementBeforeTheZombieLeaves` (cascaded Bone Splinters),
  `SoulblastTest.freeCastSacrificeAllAsksTheDeathReplacementBeforePayment` (through Sunbird's Invocation) and
  `GiftOfDoomTest.morphSacrificeAsksTheDeathReplacementBeforeTurningFaceUp`; `GameSimulatorTest`
  `copyCastsAGraveyardSpellStoppedBeforeItsSacrificeCost` and `copyCompletesAFreeCastStoppedBeforeItsSacrificeCost`.
  No existing test changed. A sweep of 2,160 card test classes (every card with flashback, escape, a graveyard-cast
  permission, harmonize, disturb, an Adventure, cascade, discover, foretell, plot, a cast from exile or the library top,
  morph, disguise, manifest, cloak, a commander or the cost-reduction sacrifice, plus every Kalitas and Gravebane
  Zombie test) had 108 failures in 66 classes (Pantlaza, Emeritus of Truce, Printlifter Ooze, Sloppity Bilepiper,
  ...); the same 108 fail at the baseline, none new. `LegacyDepartureWrapperRatchetTest` and `GameSimulatorTest`
  (40/40) pass. `SpellCastingServiceTest` has the same 10 baseline failures.

### Step 13 (first part)

- Done: the ratchet allowlist is **empty**; no engine code calls a legacy wrapper. The wrappers themselves stay public:
  about 1,460 test files use them to put a permanent into a graveyard directly, and they are one-intent batches through
  `departNow` like any other caller. The ratchet now checks that the scan saw `StateBasedActionService` (it used to look
  for one of its wrapper calls).
- The six non-cost callers:
  - `StateBasedActionService`: completed Sagas (CR 704.5s), the world rule (CR 704.5k) and redundant Roles (CR 704.5z)
    are now intents of the check's one state-based departure batch (`performStateBasedDepartures`, formerly
    `destroyLethalCreaturesAndPlaneswalkers`), so they leave in the same event as that check's other deaths (CR 704.3)
    and a Saga creature's CR 616.1 question is asked with the others. They use `DepartureCause.STATE_BASED` (as
    before, through `removePermanentToGraveyard`, the Saga isn't a `SACRIFICE` intent: an all-Saga batch would then not be
    state-based); the Saga's "whenever you sacrifice" triggers still fire after it left, from the result's
    `lastKnownControllerId`. Their log lines are unchanged but are now written only for a permanent that left, and a
    resumed batch recognises them in `deathReason`. A permanent already in the batch for another reason isn't added
    twice.
  - `GraveyardService.performUmbraArmorReplacement` / `performSacrificeAuraAndGrantIndestructibleReplacement`: the Aura
    is a nested one-intent batch through `departNow` (`departAuraForReplacement`, DESTROY with the old indestructible log
    and orphan sweep, or OTHER as before), with `deathReplacementsCanRerunEffect` closed first: it runs while
    `DepartureService.perform` applies a destruction replacement, after every answer, so it must never stop and rerun.
    `DepartureService` is an `@Autowired @Lazy` field, so the hand-built constructors are unchanged.
  - `PermanentRemovalService.destroyPermanentInEnclosingBatch` calls `departToGraveyard` with
    `destroyAfterDestructionReplacements` directly, which is exactly the inline path the wrapper took there.
- Step 7's stale owned batch: `DepartureService.discardOrphanedStepBatch`, called at the top of every state-based
  departure check, drops an answered batch owned by a resolution step whose entry is neither the parked resolution nor
  on the stack. A batch with a question still open is kept until it is answered (the next check then drops it).
- `departAsStep` no longer ends the step while an interaction is already open (a damage replacement prompt the same
  handler raised): that step is parked to be resolved again for its prompt, and a pending departure would have parked
  it past itself. It departs at once instead (`departNow`), as before this part.
- Converted to step departures with the new `StepDepartureSupport.destroy` (a list destroyed as one event; nothing
  follows), so a CR 616.1 question now ends the step instead of reaching the legacy fallback. Each was unmarked because
  it changes the game before its destroy (so it couldn't be stopped and rerun); a step departure isn't rerun, so that no
  longer matters: the chosen-type and chosen-parity wraths (`DestroyAllCreaturesOfChosenType`,
  `DestroyAllCreaturesExceptChosenType(s)`, `DestroyAllCreaturesOfChosenParity`,
  `DestroyOtherCreaturesOfChosenManaValueParity`: Extinction, Kindred Dominance, Harsh Mercy, Mutinous Massacre, Thanos),
  the random destroys (`DestroyOneOfTargetsAtRandom`, `DestroyRandomAttackingCreature`,
  `DestroyRandomOpponentPermanentWithCounter`: Wild Swing, Capricious Efreet, Metzali, Haphazard Bombardment), the
  damage-then-destroy handlers (`DealDamageToTargetCreatureThenDestroyIfDamaged`, `DealDamageToAnyTargetThenDestroyIfDamaged`:
  Silver Bolt, The Black Arrow; the latter's damaged permanents are now one batch), `CounterSpellOrAbilityAndDestroySource`
  (Ouphe Vandals, Teferi's Response), `FlipCoinForEachMatchingPermanentDestroyOnLoss` (Rakdos the Showstopper) and
  `AddCounterToEnchantedCreatureThenDestroyAtThreshold` (Consuming Ferocity). Their logs now come from the step hooks
  ("X is destroyed." only for a permanent that left, "X is indestructible.").
- **Left for the next run(s)** (why the fallback can't be deleted yet): every departure outside a window still asks after
  the event. `grep -rlE "sacrificeAndLog|tryDestroyAndLog|destroyBatch|departNow|sacrificeTogether|performSimultaneousSacrifice|departAsStep|sacrificeSource\(" service/effect/normalfx | xargs grep -L "AsksDeathReplacementsFirst\|StepDepartureSupport"`
  lists the 38 normalfx files left (some only start a choice whose completion already asks in a replay window, e.g.
  Make an Example, Victimize, Rupture; check each): coin flips and votes (`EachPlayerFlipsCoinAndTailsSacrificesCreature`,
  `VoteForCreatureThenDestroyMostVoted`, Whims of the Fates), `RemoveAllCountersThenDestroyReferencedPermanentAndDamageController`
  (destroy, then damage: needs `destroyThenContinue`), `ChooseNonlandPermanentForEachOpponentThenDestroyOneAtRandom` (its
  destroy also runs from a choice completion), Living Death, Forbidden Ritual, Bend or Break, Raiding Party, Worms of the
  Earth, the "sacrifice or ..." / "unless sacrifices" families (`EachPlayerSacrificesPermanentOrDiscards` / `OrLosesLife`,
  `DrawCardUnlessTargetSacrificesCreatureOrPaysLife`, `EachOpponentCreatesTokenUnlessSacrificesCreature`,
  `DamageSourceControllerLosesLifeUnlessSacrificesPermanents`, the `AnyOpponentMay…` / `AnyPlayerMay…` supports,
  `MaySacrificeForCounterSupport`, `TariffSupport`), the upkeep supports (Gargantuan Gorilla, Magmasaur),
  `EachPlayerChooses…ThenSacrificesRest` (two), `ExchangeControlOfSharedTypeTargetsAndDestroyAuras`,
  `DestroyUpToOneAttachedPermanent`, `TargetPlayerDiscardsAndDestroysCreaturesOfChosenType`, Eumidian Wastewaker,
  `PlayersWhoTappedLandForManaSacrificeLandDamageIfSubtype`, `EachTargetPlayerLosesLifeAndSacrificesCreature` and
  `LifeSupport`. Outside normalfx: `DamagePreventionService` / `DamagePreventionReplacementSupport` (destroys a damage
  prevention causes), `TriggerCollectionService`, `LandEquilibriumSupport`, `CombatService`, `CardChoiceHandlerService`'s
  CR 118.6 sacrifice (can't be redone), the multi-choice completions outside `killsBeforeChangingAnything`, and the
  `performSimultaneousRemovals` groups. Only once each of these asks first (as a step departure, a marked handler or a
  replay window) can `LegacyDepartureFallback.reached` throw and the `stateBased = false` / "stays and returns `false`"
  paths in `PermanentRemovalService.removePermanentToGraveyard` and `GraveyardService.tryReplaceDestruction` be deleted.
  Making `reached` throw before then would crash those games instead of asking late. Entry 34 stays open until then.
- No `@Disabled("leave-battlefield-redesign …")` marker is left from step 1; all step 1 tests run.
- Tests: new (both `@FailOnLegacyDepartureFallback`) `ExtinctionTest.asksTheDeathReplacementBeforeAnyCreatureIsDestroyed`
  (two Gravebane Zombies under Kalitas: both asked after the type is chosen, both still on the battlefield until the
  answers, one ends in the library and one in exile) and `WildSwingTest.asksTheDeathReplacementBeforeTheRandomTargetIsDestroyed`;
  `DepartureServiceOrphanedStepBatchTest` (four cases of the orphaned-batch cleanup). Changed:
  `LegacyDepartureWrapperRatchetTest` (empty allowlist, new scan check) and `StateBasedActionServiceTest`: the Saga tests
  verify `departToGraveyard` with a state-based intent instead of `removePermanentToGraveyard`, and its setup now injects
  a mocked `StoriedEffectHandler` (every test of the class failed at HEAD with a `NullPointerException` on that field; 51
  of them pass now, and the 13 that still fail fail the same way with the old engine code).
  A sweep of 1,426 card test classes (13,064 tests: every card using a converted effect, a Saga chapter, a Role, Umbra
  armor or Crackling Emergence's replacement, and every test mentioning Kalitas, Gravebane Zombie, the world rule, a Saga,
  a Role or the "is destroyed" / "is sacrificed" log lines) had 44 failures in 31 classes (Dauthi Voidwalker, Promise of
  Loyalty, Phantom Steed, Diminisher Witch, ...); the same 44 fail with the old engine code, none new.
  `LegacyDepartureWrapperRatchetTest`, `GameSimulatorTest` (40/40), the Kalitas and Gravebane Zombie test classes,
  `EffectResolutionServiceTest`, `PermanentRemovalServiceTest` and `ResolutionScratchpadAmountTest` show no new failures
  (the first two of the last three fail at HEAD with the same tests).

### Step 2

- Types live in `magical-vibes-domain` `model/`: `DepartureCause`, `DepartureStage`, `DepartureIntent`,
  `DepartureSnapshot`, `DepartureBatch`, `DepartureResult`. `GameData.pendingDepartureBatch` is copied in
  `simulationFrameCopy()`, which feeds `GameSession.simulationCopy` (used by `Determinizer` and
  `GameSimulator`), the turn-start snapshot and `restoreMutableStateFrom`. Test:
  `GameDataDeepCopyTest.deepCopyPreservesPendingDepartureBatch`.
- `DepartureCause` has five values, not four: lethal damage is destruction that regeneration can
  replace (CR 704.5g), 0 toughness is not (CR 704.5f), so the gather stage needs `STATE_BASED_DESTROY`
  apart from `STATE_BASED`.
- Intents refer to permanents by id, so a simulation copy finds the permanent on its own battlefield.
  `DepartureService` must treat a missing permanent as "not there" (`DepartureResult.destination` null).
- `DepartureSnapshot` holds what the eight `simultaneousDying*` maps hold today and clones its
  `Permanent` on copy (the maps share it between copies today).
- `DeathExileReplacementChoice` has no batch reference: step 3 links answers to the one pending batch
  by permanent id. There is no batch-level pending/done result yet; step 3 decides what `depart` returns.
- `SimulationCopyCompletenessTest` already fails on HEAD (22 older fields missing from
  `simulationCopy()`); `pendingDepartureBatch` is not among them.

## Prompt

Replace `<N>` with the step number:

```
Implement step <N> of leave-battlefield-redesign.md. First read the whole file (Why, Design, Ground
rules) and step <N>'s section, and check in the Status table that every step it depends on is done;
if one isn't, stop and tell me. Follow the ground rules: read agent-docs/ARCHITECTURE.md before
touching engine code, keep new state copyable for simulations, verify every CR number with the rules
MCP, and test behaviour through the engine. Run only the tests for what you touched — never the full
suite; ask me to run it. When the step is done, mark it done in Status, add anything later steps need
under Findings, and tell me which tests changed and why. Don't commit until I say so.
```
