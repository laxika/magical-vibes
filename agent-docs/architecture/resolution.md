# Resolution and interaction

The effect system, parked resolutions, prompts, the resolution scratchpad.

## Effect system

### Adding a new effect
1. A record implementing `CardEffect` in `magical-vibes-domain/.../model/effect/`.
2. A `@Component` declaring `handledEffect()`: `NormalEffectHandlerBean` in `service/effect/normalfx/` (stack), `StaticEffectHandlerBean` in `staticfx/` (static, `LAYER_SYSTEM.md`), `MayEffectHandlerBean` in `mayfx/` (own "you may" flow).
3. If it targets: override `targetSpec()`; a `@ValidatesTarget` method under `service/validate/` only for what the spec cannot express (`TRIGGER_SLOT_TARGETING.md`, `EFFECTS_INDEX.md`).
4. If the AI scores its family: its capability interface (`RemovalEffect`, ...; `EFFECTS_QUICK_REFERENCE.md`).

### Effects are dispatched by class
An effect's handling is registered once, keyed by `effect.getClass()` (one handler per record of an interface-typed effect); no concrete-effect `instanceof` outside `service/effect/**` and `service/validate/**`.
Owner: `EffectHandlerRegistry`, `MayEffectHandlerRegistry`, `CardEffect`.
Why: no build check catches a branch the registries don't know.

### A "you may" answer chooses no target (CR 603.5, 603.3d, 608.2b)
A triggered ability's targets, its optional effect's included, are chosen by its collector as it goes on the stack; the answer only pays costs and resumes the resolution.
Owner: `MayAbilityHandlerService.handleResolutionTimeMayChoice`.
Why: targets are fixed on the stack; an ability with none resolves without one.

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
A removal step records its `DepartureResult`s on `StackEntry.resolutionResults` even when nothing left; "if you do" and last-known values are read there, never from earlier state.
Owner: `ResolutionResults`, `StackEntry`.
Why: a removal that didn't happen must not count.

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
Every store the cleanup step clears has an `EndOfTurnStateField` (how to add one: the enum's Javadoc).
Owner: `EndOfTurnStateField`, `TurnCleanupService`.
Why: an untracked store ends at another player's cleanup step.

### A partly used shield reports its replacement
Replacing a `LIST` shield record by a reduced copy calls `replaceGameEndOfTurnElement`; keep-the-strongest stores are written through `addDamageLifeFloorUntilEndOfTurn` / `limitSpellsThisTurn`.
Owner: `GameData`.
Why: the copy stays credited to its player; a weaker value outlives the stronger one's cleanup.

## Unverified

- Code differs (a disabled `ChooseModeChoice` option is rejected): "`ChoiceHandlerService` never consults `disabledOptions`."
- Ruling text not confirmed: "(Runed Terror ruling, CR 514.2)" and "the Runed Terror ruling that an "each of your turns" permission resets after that player's ending phase".
