# Domain events

This document is the authoritative design and migration ledger for replacing runtime
`GameData` notifications with internal domain events.

## Scope

This is **not event sourcing**. Mutable `GameData` remains the authoritative game state. It is
never rebuilt by replaying events, and event retention is not a correctness requirement. Events
are immutable post-mutation facts used by observers such as WebSocket adapters, reconnect
handling, and AI wake-ups.

The foundation deliberately does not create one event class per card effect or per field write.
The stable fact families are:

| Kind | Fact | Meaning | Coalescible in one action? |
|---|---|---|---|
| `STATE_INVALIDATED` | `GameEventFact.StateInvalidated` | Named sections of the authoritative state need a new audience-specific view. | Yes, but only with the exact same audience. |
| `DECISION_REQUESTED` | `GameEventFact.DecisionRequested` | A stable decision identity and decision family need an answer. | No. |
| `PRIVATE_REVEAL` | `GameEventFact.PrivateReveal` | An immutable card snapshot was revealed to a restricted audience. | No. |
| `GAME_ENDED` | `GameEventFact.GameEnded` | The game ended in a win or draw. | No. |

Domain facts and envelopes live in
`magical-vibes-domain/.../model/event`. They have no networking dependency and may contain only
identifiers, scalar values, enums, and defensively copied immutable snapshots. They must never
contain `GameData`, `Card`, `Permanent`, `StackEntry`, or mutable collections.

`GameEventEnvelope` carries:

- game ID;
- monotonically increasing game-local sequence;
- causal action ID;
- state version;
- event kind;
- immutable fact payload;
- explicit `GameEventAudience` visibility and recipients.

`GameEventAudience.internalOnly()` is the safe default used by the audience-less `emit` overload.
Player visibility always requires an explicit audience. `DECISION_REQUESTED` and
`PRIVATE_REVEAL` additionally reject `PUBLIC` and `INTERNAL` audiences.

## Mutation and dispatch contract

`GameMutationCoordinator` owns the whole lifecycle:

1. A public action starts `mutate(gameData, causalActionId, operation)` **before** acquiring the
   `GameData` monitor.
2. The coordinator runs the operation under `synchronized (gameData)`.
3. Nested coordinator calls for that same `GameData` append to the outer context and inherit the
   outer causal action ID. They do not allocate versions, sequences, or flush.
4. The successful outer scope increments state version once, coalesces compatible state
   invalidations, allocates sequences in list order, and freezes a `GameEventBatch`.
5. The coordinator releases the `GameData` monitor and dispatches the completed batch.
6. A per-instance dispatch stripe remains held through delivery so a later mutation of the same
   `GameData` cannot overtake the earlier batch.

A scope started while its caller already holds `synchronized (gameData)` is rejected. This is
intentional: returning from such a scope could still leave a legacy caller holding the monitor,
which would violate post-lock dispatch.

If the mutation throws, its pending event batch is discarded and no completed-action metadata is
allocated. This does not add transactional rollback to the existing mutable engine. If a
subscriber throws, `GameEventDispatcher` records the failure and continues to independent
subscribers; the already-completed mutation, state version, and sequences remain committed.

Every successful outer scope advances state version, including an empty event batch. Sequences
advance only for emitted envelopes.

### Required invariants

- No `GameEventSubscriber` is invoked while `Thread.holdsLock(gameData)` is true.
- The outermost mutation scope owns dispatch; nested scopes append to the same ordered batch.
- State invalidations may coalesce only inside one completed action and only for an identical
  audience. Decisions, interactions, private reveals, and game end are never coalesced.
- Sequences are deterministic, positive, strictly increasing, and game-local.
- Audience omission means `INTERNAL`, never all players.
- `GameData.simulation` produces a `SUPPRESSED_SIMULATION` batch. The simulation copy advances its
  own local version and sequence for deterministic engine behavior, but no external subscriber is
  invoked.
- Events describe domain facts. A later transport subscriber resolves current state by game ID,
  enforces the envelope audience, creates player-specific view DTOs, and only then sends WebSocket
  messages.
- Reconnect replays an existing decision identity; it does not emit a second logical decision.
- State invalidation means “build a post-action view,” not a request to observe an intermediate
  mutation state.

## Current legacy inventory and ratchet

Inventory date: 2026-07-25. Counts are lexical production-source call sites under the engine
`service` tree; the two `GameBroadcastService` method declarations are excluded. The focused
`LegacyNotificationSurfaceRatchetTest` contains the same package-family allowlist. A missing
family has an implicit baseline of zero. Counts may only shrink, and the explicit eventual target
for every family is zero.

| Legacy surface | Current total | Eventual target |
|---|---:|---:|
| `GameBroadcastService.broadcastGameState` | 118 | 0 |
| engine `SessionManager.sendToPlayer/sendToPlayers` | 51 | 0 |
| `GameBroadcastService.logAndBroadcast` | 2,371 | 0 |

### `broadcastGameState` package-family classification

| Package family | Count | Workflow classification |
|---|---:|---|
| service root | 11 | priority/pass, mulligan, auction, and stack-resolution refreshes |
| `ability` | 14 | ability activation, cost/payment completion, and activated-effect refreshes |
| `effect/normalfx` | 16 | multi-stage exile/reveal/restart effect refreshes |
| `input` | 58 | legacy choice completion and parked-resolution refreshes |
| `interaction` | 3 | auction, keep-cards, and X-value answer refreshes |
| `spell` | 3 | spell-cast payment/target completion refreshes |
| `turn` | 13 | auto-pass and turn/step progression refreshes |

Exhaustive files for these 118 calls:

| Family | Files and counts |
|---|---|
| service root | `GameService` 3; `MulliganService` 5; `PermanentAuctionService` 1; `StackResolutionService` 2 |
| `ability` | `AbilityActivationService` 12; `ActivatedAbilityExecutionService` 2 |
| `effect/normalfx` | `BrilliantUltimatumSupport` 1; `ExileSupport` 8; `ImprovisationCapstoneCastSupport` 2; `KarnRestartGameEffectHandler` 1; `KarnScionReturnSilverCounterCardEffectHandler` 1; `KarnScionRevealTwoOpponentChoosesEffectHandler` 1; `PutCardExiledWithSourceIntoHandEffectHandler` 1; `RevealTopCardsOpponentPaysLifeOrToHandEffectHandler` 1 |
| `input` | `CardChoiceHandlerService` 3; `ChoiceHandlerService` 12; `GraveyardChoiceHandlerService` 1; `InputCompletionService` 2; `LibraryChoiceHandlerService` 1; `MayAbilityTapCostService` 2; `MayCopyHandlerService` 1; `MayMiscHandlerService` 5; `MultiPermanentChoiceHandlerService` 16; `PermanentChoiceBattlefieldHandlerService` 1; `PermanentChoiceSpellHandlerService` 4; `PermanentChoiceTriggerHandlerService` 10 |
| `interaction` | `IllicitAuctionBidChoiceInteractionHandler` 1; `KeepCardsInHandChoiceInteractionHandler` 1; `XValueChoiceInteractionHandler` 1 |
| `spell` | `SpellCastingService` 3 |
| `turn` | `AutoPassService` 9; `TurnProgressionService` 4 |

Migration classification: all become audience-appropriate `STATE_INVALIDATED` facts. A completed
action normally needs one all-player invalidation plus, only where necessary, a private
player-view invalidation. Transport-side state rendering must preserve the current
player-specific hidden-information rules in `GameBroadcastService`.

### Engine `SessionManager` package-family classification

| Package family | Count | Workflow classification |
|---|---:|---|
| service root | 8 | per-player game state 1, hand reveal 1, game end 3, mulligan notifications 3 |
| `ability` | 2 | discard/exile additional-cost decisions |
| `effect/normalfx` | 8 | game restart 1 and private hand/library reveal notifications 7 |
| `interaction` | 33 | registry-managed interaction, attacker, blocker, and combat-damage prompts |

Exhaustive files for these 51 calls:

- Service root: `GameBroadcastService` 2, `GameOutcomeService` 3, `MulliganService` 3.
- Ability: `AbilityActivationService` 2.
- Effects: `KarnRestartGameEffectHandler`,
  `LookAtHandEffectHandler`, `LookAtRandomCardInTargetPlayerHandEffectHandler`,
  `LookAtTopCardsOfTargetLibraryEffectHandler`,
  `RevealRandomCardFromTargetPlayerHandEffectHandler`,
  `RevealRandomCardFromTargetPlayerHandLoseLifeEqualToManaValueEffectHandler`,
  `RevealRandomHandCardAndPlayEffectHandler`, and `TempestEfreetAnteExchangeEffectHandler`,
  one each.
- Interaction prompts, one each:
  `AdNauseamRepeatChoiceInteractionHandler`, `AttackerDeclarationInteractionHandler`,
  `BlockerDeclarationInteractionHandler`, `BrilliantUltimatumPlayChoiceInteractionHandler`,
  `ColorChoiceInteractionHandler`, `CombatDamageAssignmentInteractionHandler`,
  `DoomsdayChoiceInteractionHandler`, `GraveyardChoiceInteractionHandler`,
  `GraveyardExileCostChoiceInteractionHandler`, `HandCardChoiceInteractionHandlers`,
  `HandTopBottomChoiceInteractionHandler`, `IllicitAuctionBidChoiceInteractionHandler`,
  `ImprovisationCapstoneCastChoiceInteractionHandler`,
  `KeepCardsInHandChoiceInteractionHandler`, `KnowledgePoolCastChoiceInteractionHandler`,
  `LibraryReorderInteractionHandler`, `LibraryRevealChoiceInteractionHandler`,
  `LibrarySearchInteractionHandler`, `MayAbilityChoiceInteractionHandler`,
  `MirrorOfFateChoiceInteractionHandler`, `MultiGraveyardChoiceInteractionHandler`,
  `MultiPermanentChoiceInteractionHandler`, `MultiZoneExileChoiceInteractionHandler`,
  `PermanentAuctionChoiceInteractionHandler`, `PermanentChoiceInteractionHandler`,
  `PutCardsFromHandOnLibraryCardChoiceInteractionHandler`,
  `PutCardsFromHandOnLibraryDestinationChoiceInteractionHandler`,
  `RevealCardsDiscardChoiceInteractionHandler`, `RevealedHandChoiceInteractionHandler`,
  `ScryInteractionHandler`, `SearchLibraryToTopChoiceInteractionHandler`,
  `SylvanLibraryChoiceInteractionHandler`, and `XValueChoiceInteractionHandler`.

Migration classification:

- `GameStateMessage` creation becomes the WebSocket adapter for `STATE_INVALIDATED`.
- Mulligan resolution is a state observation; “select cards to bottom” is a non-coalescible
  `CARDS_TO_BOTTOM` decision.
- The two `AbilityActivationService` prompts and all 33 interaction handlers become
  non-coalescible `DECISION_REQUESTED` facts. Attacker, blocker, and combat-damage assignment keep
  their distinct decision kinds.
- The seven effect reveal sends plus `GameBroadcastService.revealOpponentHandToPlayer` become
  `PRIVATE_REVEAL` with immutable `CardSnapshot` lists and explicit recipients.
- `KarnRestartGameEffectHandler` is a public state invalidation/observation, not a game-end fact.
- The three `GameOutcomeService` sends become one non-coalescible `GAME_ENDED` fact per terminal
  action. Tournament notification, timeout cleanup, and registry removal remain post-result
  application concerns and must not occur under the `GameData` monitor.

### `logAndBroadcast` package-family classification

`logAndBroadcast` currently appends an immutable `GameLogEntry` to `GameData.gameLog`; actual
delivery is coupled to the next `broadcastGameState`. Its 2,371 call sites are exhaustively
classified by owning package family below. Card/effect-specific rows intentionally migrate to
the same `GAME_LOG` state invalidation rather than new per-card event kinds.

| Package family | Count | Workflow classification |
|---|---:|---|
| service root | 111 | draw, outcomes, mulligan, stack, triggered queue, reset/auction/warp observations |
| `ability` | 40 | activation and payment observations |
| `ability/cost` | 8 | ability-cost payment observations |
| `aura` | 4 | attachment legality observations |
| `battle` | 4 | battle defeat/transform observations |
| `battlefield` | 40 | entry, removal, clone, and control observations |
| `combat` | 78 | declarations, blocks, damage, and combat-trigger observations |
| `effect` | 2 | shared effect-resolution observations |
| `effect/mayfx` | 23 | accepted/declined may-effect observations |
| `effect/normalfx` | 1,275 | normal card-effect resolution observations |
| `graveyard` | 17 | graveyard movement observations |
| `input` | 468 | choice answers and resumed-resolution observations |
| `interaction` | 16 | registry-managed choice-answer observations |
| `paradigm` | 8 | paradigm workflow observations |
| `spell` | 27 | casting and spell-cost observations |
| `state` | 8 | state-based action and state-trigger observations |
| `trigger` | 143 | trigger collection/queue observations |
| `turn` | 99 | step, turn, cleanup, and auto-pass observations |

The service-root 111 are: `DrawService` 27, `GameBroadcastService` 2,
`GameOutcomeService` 4, `GameService` 2, `LichsMirrorResetService` 2,
`MulliganService` 7, `PermanentAuctionService` 3, `StackResolutionService` 29,
`TriggeredAbilityQueueService` 33, and `WarpWorldService` 2. The remaining 2,260 calls are covered
by the package-family rows and ratchet; their handler class names identify the individual card or
workflow, and no handler is exempt.

Migration classification: append the existing structured `GameLogEntry` to authoritative state,
then emit/merge a `STATE_INVALIDATED(GAME_LOG)` fact for the appropriate audience. Do not put the
entry itself into an event because its card segments hold `Card` references. The transport adapter
continues to build immutable log views from authoritative state after the lock is released.

## Named workflow ledger

Every workflow below is open unless marked complete. “Open” means current runtime behavior is
intentionally unchanged by this foundation prompt.

| Workflow | Current path | Required event closure | Status |
|---|---|---|---|
| Foundation | domain event records → `GameMutationCoordinator` → `GameEventDispatcher` | immutable facts/envelopes, nested batching, ordering, audience safety, simulation suppression, failure isolation | **Complete** |
| Public game-state refresh | 118 `broadcastGameState` calls → player-specific `GameStateMessage` | coalesced `STATE_INVALIDATED`; WebSocket subscriber constructs the same per-player wire DTO after unlock | Open |
| Game log | 2,371 `logAndBroadcast` calls → `gameLog` → next state message | append under lock plus `GAME_LOG` invalidation; preserve structured segments and incremental wire behavior | Open |
| Generic interactions | begin site → `InteractionHandlerRegistry.begin`/handler `prompt` → 30 generic prompt sends | stable decision ID plus non-coalescible `DECISION_REQUESTED(INTERACTION)` | Open |
| Attackers | `CombatAttackService` → `AttackerDeclarationInteractionHandler` → `AvailableAttackersMessage` | `DECISION_REQUESTED(ATTACKER_DECLARATION)` | Open |
| Blockers | `CombatBlockService` → `BlockerDeclarationInteractionHandler` → `AvailableBlockersMessage` | `DECISION_REQUESTED(BLOCKER_DECLARATION)` | Open |
| Combat damage assignment | `CombatDamageService` → `CombatDamageAssignmentInteractionHandler` → notification | `DECISION_REQUESTED(COMBAT_DAMAGE_ASSIGNMENT)` | Open |
| Ability additional-cost choices | two `AbilityActivationService` direct prompts | registry interaction plus non-coalescible decision fact | Open |
| Mulligan | `MulliganService` direct resolved/bottom sends | state observation plus `MULLIGAN`/`CARDS_TO_BOTTOM` decisions without changing message timing | Open |
| Private hand/library reveals | `GameBroadcastService` plus seven normal-effect handlers | `PRIVATE_REVEAL`, explicit recipient, immutable snapshots; never public by default | Open |
| Game over | three `GameOutcomeService` sends (loss, declared winner, draw) | one `GAME_ENDED` fact; adapters preserve `GameOverMessage`, draft callback, timer cleanup, and registry removal ordering | Open |
| Game restart | `KarnRestartGameEffectHandler` direct restart send | public state/observation event; not `GAME_ENDED` | Open |
| Reconnect state | login builds `JoinGame` via `GameBroadcastService.getJoinGame` | preserve current reconnect snapshot and hidden player view | Open |
| Reconnect decision replay | `GameMessageHandler` → `GameService.resendAwaitingInput` → `ReconnectionService` → `InteractionHandlerRegistry.replayPrompt` → same handler `prompt` | replay the existing decision ID only to the reconnecting authorized recipient; no duplicate logical decision or log | Open |
| Live AI wake-up | outbound DTO → `AiConnection.actionableType` → delayed executor → `AiDecisionEngine.handleEvent` | AI subscriber consumes domain state invalidation/decision/game-end facts; coalesce only invalidations, never decisions | Open |
| AI initial mulligan | `AiPlayerService` → `AiConnection.scheduleInitialAction` | retain explicit initial wake-up or model it as the first mulligan decision | Open |
| MCTS/headless | `GameData.simulationCopy`, `HeadlessWebSocketSessionManager`, `GameBroadcastService`/`GameOutcomeService` guards, `AutoPassService` simulation branches, `SimulationLogSuppressor` | coordinator produces `SUPPRESSED_SIMULATION`; no external subscriber, WebSocket, registry, timeout, draft, or live-AI side effect | Foundation complete; legacy guards remain open |

## Migration order

Later prompts should migrate vertical workflows, not raw call counts:

1. Add transport and AI subscribers with parity tests but keep them dormant.
2. Wrap one public action family in `GameMutationCoordinator`, preserving validation inside the
   coordinator-owned monitor.
3. Migrate generic interaction begin/replay once so all decision types share stable identity.
4. Migrate private reveals and game end as non-coalescible facts.
5. Replace public state refreshes package by package.
6. Convert log append sites package by package to `GAME_LOG` invalidation.
7. Remove the dormant legacy surfaces and drive every ratchet family to zero.

At every step, hidden-information parity, event audience checks, current WebSocket message shape
and ordering, live-AI wake-up behavior, reconnect replay, and headless suppression require focused
tests before lowering the ledger count.
