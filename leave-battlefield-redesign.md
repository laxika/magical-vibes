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
| 10 | Convert the choice-completion paths | open |
| 11 | Loops and drained queues | open |
| 12 | Costs and mana abilities | open |
| 13 | Make the guard strict and remove the fallback | open |
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
