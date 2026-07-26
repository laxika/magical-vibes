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
| `DECISION_REQUESTED` | `GameEventFact.DecisionRequested` | A stable decision identity and decision family need an answer; delivery distinguishes initial open from replay without creating a second logical decision. | No. |
| `PRIVATE_REVEAL` | `GameEventFact.PrivateReveal` | An immutable card snapshot was revealed to a restricted audience. | No. |
| `MULLIGAN_RESOLVED` | `GameEventFact.MulliganResolved` | The existing public mulligan-result notification must be reproduced. | No. |
| `GAME_ENDED` | `GameEventFact.GameEnded` | The runtime game ended in a win, draw, or transport-silent abandonment. | No. |

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
6. A per-instance action lock remains held through delivery so a later mutation of the same
   `GameData` cannot overtake the earlier batch.

A scope started while its caller already holds `synchronized (gameData)` is rejected. This is
intentional: returning from such a scope could still leave a legacy caller holding the monitor,
which would violate post-lock dispatch.

If the mutation throws, its pending event batch is discarded and no completed-action metadata is
allocated. This does not add transactional rollback to the existing mutable engine: mutations
already applied before the exception remain in `GameData`, but their incomplete-action facts are
not observable because the action never reached a stable post-mutation observation point. If a
subscriber throws, `GameEventDispatcher` records the failure and continues to independent
subscribers; the already-completed mutation, state version, and sequences remain committed.

Every successful outer scope advances state version, including an empty event batch. Sequences
advance only for emitted envelopes.

The coordinator stores active context in weakly keyed per-`GameData` action state, never in a
`ThreadLocal`. The context is installed only while the per-game action lock is held and is cleared
in `finally`, so executor tasks and pooled threads cannot inherit scope state from an earlier game.
Different games have different action locks and may mutate concurrently; same-game actions remain
serialized through subscriber delivery.

Canonical runtime mutation boundaries are `GameService` commands, `GameSetupService` create/join,
AI seating, tournament-game creation, and `GameTimeoutService` disconnect and timer
callbacks. Interaction answers (including answers that resume a parked stack entry), surrender,
and auto-pass recursion therefore join one outer command batch. A later answer to a parked
interaction is a new causal action. Lower-level service tests that invoke handlers directly may
establish the supported explicit boundary with `GameMutationCoordinator.mutate`; event emission
without such a boundary is always rejected.

### Required invariants

- No `GameEventSubscriber` is invoked while `Thread.holdsLock(gameData)` is true.
- The outermost mutation scope owns dispatch; nested scopes append to the same ordered batch.
- State invalidations may coalesce only inside one completed action, only for an identical
  audience, and never across a decision event. A decision is an ordering barrier so an
  invalidation before it and another after it remain two observations. Decisions/interactions,
  mulligan notifications, private reveals, and game end are never coalesced.
- Sequences are deterministic, positive, strictly increasing, and game-local.
- Audience omission means `INTERNAL`, never all players.
- `GameData.simulation` produces a `SUPPRESSED_SIMULATION` batch. The simulation copy advances its
  own local version and sequence for deterministic engine behavior, but no external subscriber is
  invoked.
- Events describe domain facts. `GameEventProjectionSubscriber` resolves current state by game ID,
  enforces the envelope audience, creates player-specific view DTOs through
  `GameViewProjectionFactory`, and only then hands typed messages to `GameMessageTransport`.
  `SessionManager` selects the connection and the connection alone serializes.
- Reconnect is an ordered read-only observation: it projects the authoritative active interaction
  through the same exact-class registry and does not allocate a mutation, state version, event
  sequence, or second logical decision.
- State invalidation means “build a post-action view,” not a request to observe an intermediate
  mutation state.

## Current legacy inventory and ratchet

Inventory date: 2026-07-26. Counts are lexical production-source call sites under the engine
`service` tree; `GameBroadcastService.logAndBroadcast`'s declaration and the documented transport
adapters are excluded from the direct-mutation categories. The focused
`LegacyNotificationSurfaceRatchetTest` contains the same package-family allowlist. A missing
family has an implicit baseline of zero. Counts may only shrink, and the explicit eventual target
for every family is zero.

| Legacy surface | Current total | Eventual target |
|---|---:|---:|
| direct runtime state broadcast | 0 | 0 |
| direct engine session send outside transport adapters | 0 | 0 |
| `GameBroadcastService.logAndBroadcast` | 2,290 | 0 |

The lifecycle migration ratchet additionally fixes the direct state/session count at zero for
`GameService`, `GameSetupService`, `MulliganService`, `GameOutcomeService`,
`GameTimeoutService`, `ReconnectionService`, `AutoPassService`, `TurnProgressionService`, and
`KarnRestartGameEffectHandler`. The spell/ability/stack migration ratchet additionally fixes
the direct state/session count at zero for `SpellCastingService`, `AbilityActivationService`,
`ActivatedAbilityExecutionService`, and `StackResolutionService`. The generic-input ratchet fixes
the same surfaces at zero for every `service/input` class, including the multi-permanent and
battlefield, spell, and trigger permanent-choice handlers.
The recursive effect-package ratchet fixes both direct state/session calls and direct
`SessionManager` dependencies at zero for every `service/effect` class.

### Direct runtime state-broadcast classification

| Package family | Count | Workflow classification |
|---|---:|---|
| service root | 0 | auction refresh migrated |
| `interaction` | 0 | auction, keep-cards, and numeric-answer refreshes migrated |

There are no runtime call sites and the public `broadcastGameState` API has been removed.
`PermanentAuctionService` now emits a state invalidation before each non-coalescible auction
decision. Keep-cards publishes through the shared input-completion observation point before the
next player's decision. X-value and Illicit Auction answers use the shared SBA/resume/auto-pass
epilogue and publish only after it reaches a stable state.

All state rendering now occurs in `GameViewProjectionFactory` after the mutation monitor is
released, preserving player-specific hidden-information rules. Every effect-owned invalidation,
including the exile/cast, ultimatum, Karn, source-linked exile, and punisher workflows formerly
listed here, is complete.

### Engine session/transport classification

| Package family | Count | Workflow classification |
|---|---:|---|
| direct mutation/session send | 0 | all delivery is projected |
| `interaction` | 0 | standard registry-managed prompts are event projections |

The documented adapter allowlist is deliberately limited to:

- `GameMessageTransport`: the one raw `SessionManager.sendToPlayer` call and per-recipient failure
  isolation for typed outbound messages.
- `GameSessionTransportAdapter`: connection-state reads plus AI connection close/unregister
  lifecycle operations.

Every other engine source has zero `SessionManager` dependencies. Reconnect state and decision
replay use `GameResyncProjectionService`, `ReconnectionService`, the canonical prompt projector,
and these transport adapters rather than raw sessions.

- Effects: zero. Hand/library looks and rules-public reveals emit audience-restricted facts through
  `CardRevealService`; no effect handler constructs a reveal DTO or owns transport.
- Interaction handlers: zero. Handler files contain only answer validation/mutation logic.
  `InteractionPromptProjectionRegistry` has one exact-class projection strategy for every
  promptable `PendingInteraction` subtype and constructs the unchanged typed wire message only
  after the decision identity is matched against the authoritative active interaction.

Migration classification:

- `GameStateMessage` creation becomes the WebSocket adapter for `STATE_INVALIDATED`.
- Mulligan resolution is now a state observation; “select cards to bottom” is a non-coalescible
  `CARDS_TO_BOTTOM` decision with a stable replay identity.
- Registry-managed interaction begin/retry sites, including the former two direct
  `AbilityActivationService` prompts, now emit non-coalescible `DECISION_REQUESTED` facts.
  Attacker, blocker, and combat-damage assignment keep their distinct decision kinds and carry
  finalized immutable legality snapshots on their pending interactions. Attacker, blocker,
  combat-damage, and standard interaction facts are all projected through the exact-class
  `InteractionPromptProjectionRegistry`; handlers never send or construct networking DTOs.
- Effect hand/library looks, rules-public hand reveals, random reveals, reveal-and-play, and ante
  reveals emit `PRIVATE_REVEAL` with immutable `CardSnapshot` lists and explicit recipients.
  Rules-public reveals explicitly name every seated player; private looks name only the viewer.
  Both project to the existing reveal message shapes, while generic diagnostic rendering redacts
  every card identity field.
- `KarnRestartGameEffectHandler` now emits a public state invalidation/observation, not a
  game-end fact.
- The three former `GameOutcomeService` sends are one non-coalescible `GAME_ENDED` fact per
  terminal action. Tournament notification, timeout cleanup, AI closure, and registry removal
  are ordered subscriber concerns outside the `GameData` monitor.

### `logAndBroadcast` package-family classification

`logAndBroadcast` currently appends an immutable `GameLogEntry` to `GameData.gameLog`; actual
delivery is coupled to the next state projection. Its 2,290 call sites are exhaustively
classified by owning package family below. Card/effect-specific rows intentionally migrate to
the same `GAME_LOG` state invalidation rather than new per-card event kinds.

| Package family | Count | Workflow classification |
|---|---:|---|
| service root | 112 | draw, outcomes, mulligan, stack, triggered queue, reveal, reset/auction/warp observations |
| `ability` | 40 | activation and payment observations |
| `ability/cost` | 8 | ability-cost payment observations |
| `aura` | 4 | attachment legality observations |
| `battle` | 4 | battle defeat/transform observations |
| `battlefield` | 40 | entry, removal, clone, and control observations |
| `effect` | 2 | shared effect-resolution observations |
| `effect/mayfx` | 23 | accepted/declined may-effect observations |
| `effect/normalfx` | 1,271 | normal card-effect resolution observations |
| `graveyard` | 17 | graveyard movement observations |
| `input` | 468 | choice answers and resumed-resolution observations |
| `interaction` | 16 | registry-managed choice-answer observations |
| `paradigm` | 8 | paradigm workflow observations |
| `spell` | 27 | casting and spell-cost observations |
| `state` | 8 | state-based action and state-trigger observations |
| `trigger` | 143 | trigger collection/queue observations |
| `turn` | 99 | step, turn, cleanup, and auto-pass observations |

The service-root 112 are: `CardRevealService` 3, `DrawService` 27,
`GameOutcomeService` 4, `GameService` 2, `LichsMirrorResetService` 2,
`MulliganService` 7, `PermanentAuctionService` 3, `StackResolutionService` 29,
`TriggeredAbilityQueueService` 33, and `WarpWorldService` 2. The remaining 2,178 calls are covered
by the package-family rows and ratchet; their handler class names identify the individual card or
workflow, and no handler is exempt.

Migration classification: append the existing structured `GameLogEntry` to authoritative state,
then emit/merge a `STATE_INVALIDATED(GAME_LOG)` fact for the appropriate audience. Do not put the
entry itself into an event because its card segments hold `Card` references. The transport adapter
continues to build immutable log views from authoritative state after the lock is released.

All 78 former combat-package calls now append through
`GameMutationCoordinator.appendPublicGameLog`, which records a coalescible public
`STATE_INVALIDATED(GAME_LOG)` fact in the same mutation action. Combat packages therefore have
zero legacy log calls as well as zero direct state broadcasts and session sends.

## Named workflow ledger

Every workflow below is open unless marked complete. “Open” means current runtime behavior is
intentionally unchanged by this foundation prompt.

| Workflow | Current path | Required event closure | Status |
|---|---|---|---|
| Foundation | domain event records → `GameMutationCoordinator` → `GameEventDispatcher` | immutable facts/envelopes, nested batching, ordering, audience safety, simulation suppression, failure isolation | **Complete** |
| Canonical projection subscriber | `GameEventProjectionSubscriber` → `GameViewProjectionFactory`/interaction registry → typed messages → `GameMessageTransport` | post-lock authoritative projection, explicit audience enforcement, no serialization, per-recipient transport failure isolation, human/AI typed-message parity | **Complete** |
| Public game-state refresh | `STATE_INVALIDATED` → player-specific `GameStateMessage` | coalesced `STATE_INVALIDATED`; canonical subscriber constructs the player-specific wire DTO after unlock | **Complete; zero direct runtime broadcasts and no public broadcast API** |
| Generic input completion | answer handler → required SBA/may processing → parked-resolution resume → auto-pass stable point → state observation | one coalesced all-player `STATE_INVALIDATED` per completed answer; a queued interaction keeps its non-coalescible `DECISION_REQUESTED` barrier; validation failure and game end do not leak an intermediate state observation | **Complete**, including permanent and multi-permanent selection |
| Game log | 2,290 `logAndBroadcast` calls → `gameLog` → next state message | append under lock plus `GAME_LOG` invalidation; preserve structured segments and incremental wire behavior | Combat package complete; remaining packages open |
| Generic interactions | begin site → authoritative pending interaction + stable ID → `DECISION_REQUESTED` → `InteractionPromptProjectionRegistry` → typed prompt | one non-coalescible private fact per interaction; exact identity match before projection; every promptable subtype (including combat) has one explicit strategy and no raw-state fallback; handlers have no session/networking dependency | **Complete** |
| Attackers | `CombatAttackService` finalizes an immutable legality snapshot → registry decision event → canonical subscriber → `AvailableAttackersMessage` | `DECISION_REQUESTED(ATTACKER_DECLARATION)` with stable retry/replay identity and Mindslaver audience | **Complete** |
| Blockers | `CombatBlockService` finalizes legal pairs and requirements → registry decision event → canonical subscriber → `AvailableBlockersMessage` | `DECISION_REQUESTED(BLOCKER_DECLARATION)` with stable retry/replay identity | **Complete** |
| Combat damage assignment | `CombatDamageService` snapshots targets/trample/deathtouch → registry decision event → canonical subscriber → notification | `DECISION_REQUESTED(COMBAT_DAMAGE_ASSIGNMENT)` with stable retry/replay identity | **Complete** |
| Combat lifecycle | beginning of combat → declarations/taxes/requirements → first-strike and regular assignment/application → SBA/triggers → cleanup/later steps | ordered `STATE_INVALIDATED` barriers plus audience-restricted combat decisions; no combat-owned direct state/session/log notification | **Complete** |
| Ability additional-cost choices | registry interaction plus non-coalescible decision fact | discard/exile/permanent cost choices preserve stable identity and validate before payment | **Complete** |
| Mulligan | `MulliganService` emits resolved/state/decision facts with stable mulligan and bottom decision identities | `MULLIGAN_RESOLVED` plus state observation and `CARDS_TO_BOTTOM` decision without changing message timing | **Complete** |
| Private hand/library reveals | effect handlers → `CardRevealService` → audience-restricted fact → canonical subscriber → existing reveal DTO | `PRIVATE_REVEAL`, explicit recipients, immutable snapshots, diagnostic redaction; private look and rules-public reveal remain distinct | **Complete** |
| Game over | `GameOutcomeService` finalizes authoritative result and emits one `GAME_ENDED`; ordered subscribers project `GameOverMessage`, close AI, notify tournaments, cancel timers, and remove the registry entry | one `GAME_ENDED` fact; adapters preserve `GameOverMessage`, draft callback, timer cleanup, and registry removal ordering | **Complete** |
| Game restart | `KarnRestartGameEffectHandler` emits state, restart mulligan observation, and fresh stable mulligan decisions | public state/observation event; not `GAME_ENDED` | **Complete** |
| Reconnect state | `GameResyncProjectionService.currentState` builds a monitor-protected current `JoinGame` projection | Preserve the exact login response envelope and build its hidden player view through the shared projection factory rather than a domain event | **Complete** |
| Reconnect decision replay | `GameMessageHandler` → `GameService.resendAwaitingInput` → `ReconnectionService` → ordered read-only observation → `InteractionPromptProjectionRegistry` → typed prompt | project only the current authoritative interaction to the reconnecting authorized recipient; no mutation, event/version/sequence allocation, stale historical choice, duplicate logical decision, or log | **Complete** |
| Live AI wake-up | canonical subscriber combat DTO → `AiConnection.actionableType` → delayed executor → `AiDecisionEngine.handleEvent` | Combat attacker/blocker/damage decisions wake independently and in event order; broader direct domain-event AI consumption remains open | Combat decisions complete; broader migration open |
| AI initial mulligan | `AiPlayerService` → `AiConnection.scheduleInitialAction` | retain explicit initial wake-up or model it as the first mulligan decision | Open |
| MCTS/headless | `GameData.simulationCopy`, `HeadlessWebSocketSessionManager`, `GameBroadcastService`/`GameOutcomeService` guards, `AutoPassService` simulation branches, `SimulationLogSuppressor` | coordinator produces `SUPPRESSED_SIMULATION`; no external subscriber, WebSocket, registry, timeout, draft, or live-AI side effect | Foundation complete; legacy guards remain open |

## Migration order

Later prompts should migrate vertical workflows, not raw call counts:

1. Add the canonical transport projection subscriber with human/AI typed-message parity tests but
   keep production emission dormant. **Complete.**
2. Wrap one public action family in `GameMutationCoordinator`, preserving validation inside the
   coordinator-owned monitor.
3. Migrate generic interaction begin/replay once so all decision types share stable identity.
4. Migrate private reveals and game end as non-coalescible facts. **Complete.**
5. Replace public state refreshes package by package. **Complete.**
6. Convert log append sites package by package to `GAME_LOG` invalidation.
7. Remove the dormant legacy surfaces and drive every ratchet family to zero. **State broadcast
   and direct session-send categories complete; game-log migration remains open.**

At every step, hidden-information parity, event audience checks, current WebSocket message shape
and ordering, live-AI wake-up behavior, reconnect replay, and headless suppression require focused
tests before lowering the ledger count.
