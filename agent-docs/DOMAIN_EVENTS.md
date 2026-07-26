# Domain events

This document is the authoritative runtime design for observing mutable `GameData`.

## Model

`GameData` is authoritative mutable state; the engine is not event-sourced. Events are immutable,
post-mutation facts for human projection, AI decisions, lifecycle cleanup, and diagnostics. State
is never rebuilt by replay, and retaining event history is not required for correctness.

Facts and envelopes live in `magical-vibes-domain/.../model/event`. A fact may contain identifiers,
scalars, enums, and defensively copied immutable snapshots. It must not contain networking types,
`GameData`, `Card`, `Permanent`, `StackEntry`, or mutable collections.

| Kind | Fact | Purpose | Coalescible inside one action? |
|---|---|---|---|
| `STATE_INVALIDATED` | `StateInvalidated` | Requests a new audience-specific view of named authoritative-state sections. | Yes, only for an identical audience and never across a decision barrier. |
| `GAME_LOG_APPENDED` | `GameLogAppended` | Identifies one appended structured log entry by its zero-based authoritative index. | No. |
| `DECISION_REQUESTED` | `DecisionRequested` | Identifies a current decision, its owner, family, and delivery mode. | No. |
| `PRIVATE_REVEAL` | `PrivateReveal` | Carries immutable card snapshots for explicitly named recipients. | No. |
| `MULLIGAN_RESOLVED` | `MulliganResolved` | Describes one public mulligan result. | No. |
| `GAME_ENDED` | `GameEnded` | Describes a win, draw, or transport-silent abandonment. | No. |

`GameEventEnvelope` adds the game ID, positive game-local event sequence, positive game-local
causal action ID, state version, kind, and explicit `GameEventAudience`. Event sequence and causal
action ID are allocated from `GameData` counters only after a successful outer action; both are
deterministic and strictly increasing within a game. Nested work inherits the outer action ID.
State version advances once for every successful outer action, including an action with no facts.
Event sequence advances once per emitted envelope.

Audience omission is safe: the audience-less `emit` overload means `INTERNAL`, never public.
Player-visible facts always provide an audience. Decisions and private reveals require a private
audience; rules-public reveals explicitly list every seated recipient.

## Mutation and dispatch

`GameMutationCoordinator` owns the complete action lifecycle:

1. An external command calls `mutate(gameData, operation)` before acquiring the `GameData`
   monitor.
2. The coordinator serializes same-game actions with a game-specific action lock.
3. The operation runs under `synchronized (gameData)`.
4. Nested calls for the same game join the current context and append facts in call order.
5. A successful outer operation allocates its action ID, state version, and event sequences and
   freezes one `GameEventBatch`.
6. The `GameData` monitor is released.
7. `GameEventDispatcher` invokes subscribers while the action lock remains held, preventing a
   later same-game mutation from overtaking delivery.

Starting a scope while already holding the game monitor is rejected unless it is a nested call in
the coordinator-owned action. An event or log append outside the current action is rejected; there
is no implicit scope or direct-delivery fallback.

If mutation code throws, pending facts are discarded and completed-action metadata is not
allocated. The mutable engine is not transactional, so already-applied domain writes are not
rolled back. If a subscriber throws, the dispatcher records the failure and continues with
independent subscribers. The completed game state and allocated ordering metadata remain valid.
Subscribers are observers: they never acquire the game monitor to write `GameData`.

Different games have independent action locks and may mutate concurrently. Active scope state is
weakly keyed by `GameData`, installed only while its action lock is held, and cleared in `finally`;
it is never stored in a `ThreadLocal`.

Canonical outer boundaries are:

- `GameService` commands, including interaction answers, surrender, combat retry, and auto-pass
  continuations;
- `GameSetupService` create/join, AI seating, and tournament-game creation/opening;
- `GameTimeoutService` disconnect/reconnect and timer callbacks.

Lower-level tests that invoke mutation helpers directly establish an explicit coordinator action.
Read-only queries do not create actions.

## Projection and transport

Mutation-driven human output has one path:

`GameEventProjectionSubscriber` → projection factory/interaction registry → typed networking
message → `GameMessageTransport` → `SessionManager` → connection serialization.

The projector resolves current state by game ID only after mutation unlock, enforces the envelope
audience, excludes AI seats, and creates player-specific views. It is the only owner of runtime
game-state, mulligan, interaction/combat-decision, private-reveal, and game-over message
construction. `GAME_LOG_APPENDED` indices in the current batch select the exact structured log
entries included in the next projected state; projection cursors do not live on `GameData`.

`GameMessageTransport` is the only engine adapter that sends through `SessionManager` and isolates
delivery failure per recipient. `GameSessionTransportAdapter` is the only engine adapter that
reads connection state, exposed to mutation code through the transport-free
`PlayerConnectionState` port. Serialization occurs only at the connection boundary.

Join and reconnect responses are ordered read-side projections through the same projection
factories and transport adapter; they derive authoritative current state and do not synthesize or
replay mutation events.

Mutation and rules services depend on `GameMutationCoordinator`, `GameLogService`, and semantic
read ports. They do not depend on projectors, message transport, sessions, connections, or
networking output types.

`GameViewProjectionFactory` builds complete views only for requested human recipients.
`PrivateInformationProjectionFactory` creates reveal DTOs from authorized immutable snapshots.
`InteractionPromptProjectionRegistry` uses exact `PendingInteraction` class registration and has
one projector for every promptable subtype, including attacker declaration, blocker declaration,
and combat-damage assignment.

## Decisions, visibility, and reconnect

Every open interaction has an authoritative pending interaction and stable decision ID.
`DECISION_REQUESTED` is non-coalescible and acts as an ordering barrier. Invalid combat answers
re-request the same logical decision in a new successful action without changing its decision ID.
Mind-control recipient redirection is reflected explicitly in the audience.

Private hand/library looks name only the viewer. Rules-public reveals name every seated player.
Generic diagnostic rendering redacts card ID, name, set code, and collector number. Human
projection tests verify that unauthorized recipients receive neither the reveal message nor hidden
state in a game view.

Reconnect is an ordered read-only observation through `GameMutationCoordinator.observe`:

- `GameResyncProjectionService` derives the current player-specific join/state view;
- `ReconnectionService` reads only the authoritative active interaction or current
  cards-to-bottom requirement;
- `InteractionPromptProjectionRegistry` projects that current decision for the authorized
  reconnecting recipient.

Reconnect does not replay historical batches and does not allocate an action ID, state version,
event sequence, decision ID, or log entry.

## AI and simulation

`AiDecisionEventSubscriber` consumes completed internal facts and maps them to
`AiDecisionKind`. `AiDecisionScheduler` schedules only those internal kinds; it has no networking
message or connection dependency. Delayed execution reads authoritative live state. AI seats are
registered before initial mulligan facts, including tournament games, and are never human
projection recipients.

A simulation copy has `GameData.simulation == true`. Its successful actions still allocate local
action IDs, state versions, and event sequences so engine ordering remains deterministic, but the
batch dispatch mode is `SUPPRESSED_SIMULATION`; `GameEventDispatcher` invokes no subscriber.
The headless session port throws if transport output is attempted, the headless registry is
isolated, timers are not started, and no live AI, tournament, draft, reconnect, registry, or
WebSocket side effect is reachable.

## Lifecycle and logs

`GameOutcomeService` finalizes the authoritative result and emits at most one `GAME_ENDED` fact.
Human game-over projection runs before `GameEndLifecycleSubscriber`, which performs tournament
notification, timeout cleanup, and game-registry removal after unlock. The AI subscriber closes
registered schedulers independently. A game restart emits state/mulligan observations and fresh
decisions; it is not a game-end event.

`GameLogService.append` is the only production API that mutates `GameData.gameLog`. It requires
the current coordinator action, appends one immutable `GameLogEntry`, records one index-only
`GAME_LOG_APPENDED` fact, and emits a public `GAME_LOG` state invalidation. Facts never carry
structured segments or card identity.

## Permanent build invariants

Architecture tests enforce all of the following:

- removed notification facades and forwarding methods cannot reappear;
- raw engine session access is confined to the two named transport adapters;
- typed runtime game messages are constructed only by their projectors;
- mutation/rules services cannot depend on projection or transport services;
- event payloads contain no networking DTO or mutable domain reference;
- every externally callable mutation family joins an outer coordinator action;
- input handlers cannot bypass the shared completion epilogue;
- event subscribers cannot acquire the game monitor to mutate authoritative state;
- AI scheduling cannot consume networking messages;
- interaction and effect packages cannot send through sessions;
- event ordering, audience validation, hidden-information redaction, simulation suppression,
  reconnect derivation, and subscriber failure isolation remain behaviorally tested.

Any violation of these permanent zero-tolerance rules fails the build.
