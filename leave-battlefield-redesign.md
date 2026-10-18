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
| 1 | Behaviour tests for the target rules | open |
| 2 | Departure model types | open |
| 3 | `DepartureService` for state-based deaths | open |
| 4 | Simultaneous removals and destroy batches | open |
| 5 | Fold the leftover replacement workarounds into the choose stage | open |
| 6 | Legacy wrappers and the guard | open |
| 7 | Resolution scratchpad and pausing at the end of a step | open |
| 8 | Convert the "sacrifice this, then …" handlers | open |
| 9 | Convert the "destroy/sacrifice target, then …" handlers | open |
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
