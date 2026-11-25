# Resolution and interaction

The effect system, parked resolutions, prompts, the resolution scratchpad.

## Effect system

### Adding a new effect
1. A record implementing `CardEffect` in `magical-vibes-domain/.../model/effect/`.
2. A `@Component` declaring `handledEffect()`: `NormalEffectHandlerBean` in `service/effect/normalfx/` (stack), `StaticEffectHandlerBean` in `staticfx/` (static, `LAYER_SYSTEM.md`), `MayEffectHandlerBean` in `mayfx/` (own "you may" flow).
3. If it targets: override `targetSpec()`; a `@ValidatesTarget` method under `service/validate/` only for what the spec cannot express (`EFFECTS_INDEX.md`; a triggered ability's targets: `TRIGGERED_ABILITY_TARGETS.md`).
4. If the AI scores its family: its capability interface (`RemovalEffect`, ...; `EFFECTS_QUICK_REFERENCE.md`).

### A handler finds its permanents, players and cards through the subject resolver
Who an effect acts on is a `PermanentSubject` / `PlayerSubject` / `CardSubject`; a handler resolves it with `SubjectResolutionService`, never with its own lookup of the source, target, attached or triggering permanent, of a player, or of a card an earlier step moved.
Owner: `SubjectResolutionService` (`SUBJECTS_REFERENCE.md`).
Why: private lookups drifted apart (enchanted creature, "its controller") and gave different answers for the same words.

### Effects are dispatched by class
An effect's handling is registered once, keyed by `effect.getClass()` (one handler per record of an interface-typed effect); no concrete-effect `instanceof` outside `service/effect/**` and `service/validate/**`.
Owner: `EffectHandlerRegistry`, `MayEffectHandlerRegistry`, `CardEffect`.
Why: no build check catches a branch the registries don't know.

### A "you may" answer chooses no target (CR 603.5, 603.3d, 608.2b)
A triggered ability's targets, its optional effect's included, are chosen by the stack door (`TriggeredAbilityStackService`) as it goes on the stack; the answer only pays costs and resumes the resolution.
Owner: `MayAbilityHandlerService.handleResolutionTimeMayChoice`.
Why: targets are fixed on the stack; an ability with none resolves without one.

### A cost an effect asks for as it resolves is paid through one service (CR 118.12, 118.3)
"[Do X] unless [P pays]" / "P may pay; if they do": ask the payer unless `ResolutionCostPaymentService.couldPossiblyPay` proves they can't (no mana source at all and a short pool, `canPayLife`, each cost kind's resources; CR 118.3, 608.2d), let them activate mana abilities, a Spirit Guide's in hand too, while asked (CR 605.3a), and pay with `pay` through the shared actions (tax-paid event, discard, sacrifice, counters, poison, damage, exile, waterbend taps), any player as payer; a hand-rolled pool payment uses `payManaFromPool`. A new ransom kind is a case there.
Owner: `ResolutionCostPaymentService`.
Why: hand-rolled payments forgot the tax-paid event (Tax Taker), judged life by "can the life total change" (CR 119.8) and countered on a paid cost's shortfall (CR 118.12).

### An "unless" payer's choice is kept on the resolving entry (CR 118.12, 101.4)
Every player the payer subject names is asked in APNAP order, every one even after another paid; the answers, the players left and an all-of ransom's parts still to pay live on the waiting entry (`StackEntry.unlessAnswer`), and `UnlessPaysEffectHandler.nextStep` picks the branch once nobody is left.
Owner: `UnlessPaysEffectHandler`, `UnlessAnswer`.
Why: a global flag could be taken by another question answered during the payment's own choice.

## Coin flips (Krark's Thumb)

### A marked handler changes nothing before its flip
`ChoosesCoinFlipResultsFirst` handlers flip before any change; `ChoosesCoinFlipResultsAtCheckpoints` handlers call `checkpoint` after each change; a pure condition wrapper is `ResolvesWrappedEffectFirst`. A real d20 roll closes the window.
Owner: `CoinFlipService`, `EffectResolutionService`, `D20RollService.rollEach`.
Why: a contested flip reruns the handler; an earlier change or roll would happen twice.

### Flips outside a resolving handler have their own route
A prompt completion that flips first runs in `answerChoosingResultsFirst`; an "as this enters" flip (CR 614.1c) uses `flipAsPermanentEnters` and is asked before the enter abilities.
Owner: `CoinFlipService`, `FlipCoinOnEnterEffectHandler`.
Why: elsewhere (unmarked handlers, dispatched payloads, another prompt pending, `FlipCoinsCost`) heads is kept unasked.

## Parked resolutions and prompts

### A completion handler ends through the epilogue
A handler answering an interaction ends via an `InputCompletionService` epilogue, never a direct `resolveAutoPass` or `resolveEffectsFrom`.
Owner: `InputCompletionService` (`InputHandlerEpilogueRatchetTest`, `BaseCardTest`).
Why: else `GameData.pendingEffectResolutionEntry` stays parked: remaining effects are dropped, no player can lose.

### A nested resolution that stops for an answer goes on first
Steps resolved inside another resolution without the stack (an "as this Equipment becomes attached" choice and copy) go through `EffectResolutionService.resolveNestedInCurrentResolution`: stopped for an answer, they resume first, then the outer resolution with its next effect.
Owner: `EffectResolutionService`, `GameData.parkedOuterResolutions`.
Why: the outer resolution's park would replace the nested one's and drop its later steps.

### A mana ability's prompt nests under the payment prompt (CR 605.3a)
A mana ability activated inside a payment prompt runs within `InteractionState.openNestedPromptWindow()`; the payment prompt then returns under its decision id with no SBA, parked resolution or auto-pass in between.
Owner: `GameService.activateDuringManaPayment`, `InteractionHandlerRegistry.resumeSuspended`.
Why: the game is still inside that decision.

### Refining a begun prompt is presentation-only
`InteractionState.replaceActiveInteraction` keeps the decision id and never changes what is decided or the legal answers: a colour greyed by `ManaChoiceNarrowingService` stays answerable. (A `ManaPaymentIntent` also holds the tap's non-mana triggers until the spell or ability is on the stack.)
Owner: `InteractionState`, `ManaPaymentIntent`.
Why: reconnect replay and answer matching break otherwise.

## Resolution scratchpad

### Later steps read what happened from the scratchpad (CR 608.2c)
Every step of a `StepKind` records its `StepOutcome` (`StackEntry.recordStep`; a removal step's `DepartureResult`s) even when nothing happened; "if you do", "that many" and "those creatures" read it there (`HappenedThisWay`, `AmountThisWay`, `thisWay` / `playersThisWay`), never earlier state.
Owner: `ResolutionResults`, `StepKind`, `StackEntry`.
Why: a step that didn't happen must not count, and an older step of the same kind must not be read.

### Only a step that finishes before the next one starts may be read
An earlier step is a handler that never prompts, pauses by resolving itself again before it changes anything, or pauses at the end of its step with its result recorded by then (list: `StepKind` Javadoc's handlers); a handler that pauses mid-step by `rerunCurrentEffectAfterInteraction` or a bespoke continuation is not.
Owner: `EffectResolutionService`, the recording handlers.
Why: a later step would run before the earlier one finished, or read half its result.

### A delayed ability about a step reads that step (CR 603.7c)
"Return those cards at the next end step" / "when this leaves" after a step is `RegisterDelayedStepTriggerEffect` / `RegisterDelayedEventTriggerEffect`: its entry carries the step's outcome (`DelayedReferents.stepResults`), each card bound to its stay (`MovedCard.entryVersion`), and the number the resolution noted (`NoteAmountEffect`, `keepingNotedAmount`; CR 608.2h); "that creature" of one about another permanent than its event's is its triggering permanent (`DelayedReferents.triggeringPermanentId`).
Owner: the two registering handlers, `EventAbilitySupport.DelayedRecord`, `StackEntry.readStepResults`, `SubjectResolutionService`.
Why: a bespoke exile-and-register handler forgot the stay and returned a card that had left and come back (CR 400.7).

### A removal step asks before anything leaves (CR 616.1)
A step removes through `DepartureService.departAsStep`; on `Pending` the handler returns and is not run again, so what follows goes through `StepDepartureSupport`. `DestructionSupport.tryDestroyAndLog` / `sacrificeAndLog` never end a step.
Owner: `DepartureService`, `StepDepartureSupport`.
Why: replacements are chosen with everything still on the battlefield; nothing is done twice.

### A removal after a change is its own step
A handler or completion that already changed the game inserts the removal as a step (`departAsNextStep`, `departAfterChoice`), unless it is the handler's last action (`destroy` / `depart`).
Owner: `StepDepartureSupport`.
Why: earlier changes and random picks are never redone.

### "Cards exiled this way" come from the entry
Follow-ups read `StackEntry.resolutionExiledCardIds`, not source-linked exile records; a free cast keeps its `sourceZone` in its mode and target contexts until it is on the stack.
Owner: `StackEntry`, `ChoiceContext.LibraryCastModeChoice`.
Why: earlier uses of the source must not be included.

### Chaotic Transformation replaces one permanent at a time
Controllers and card types are recorded before the simultaneous exile; each replacement is its own step.
Owner: `ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffectHandler`.
Why: one reveal finishes before the next.

## Sequential phases (Runed Terror)

### "This turn" state ends at its creator's cleanup step (CR 514.2)
An "until end of turn" effect is a floating effect with `Duration.UNTIL_END_OF_TURN`; the duration clock ends it at its creator's share of the cleanup step. Every other store the cleanup step clears (turn history, per-turn bookkeeping) has an `EndOfTurnStateField` (how to add one: the enum's Javadoc).
Owner: `DurationClock`, `EndOfTurnStateField`, `TurnCleanupService`.
Why: an untracked store ends at another player's cleanup step.

### A partly used shield keeps its place
Using part of a shield ("prevent the next 3 damage") replaces its floating effect with a reduced copy through `GameData.reduceShield` (`replaceFloatingEffect`), keeping its place and timestamp; several "can't go below" floors (`DamageLifeFloorEffect` floated on a player) are separate floating effects and the strongest applies.
Owner: `GameData`, `GameQueryService`.
Why: a shield that moved last would be offered in the wrong order; a weaker floor must not outlive or override the stronger one.

## Unverified

- Code differs (a disabled `ChooseModeChoice` option is rejected): "`ChoiceHandlerService` never consults `disabledOptions`."
- Ruling text not confirmed: "(Runed Terror ruling, CR 514.2)" and "the Runed Terror ruling that an "each of your turns" permission resets after that player's ending phase".
