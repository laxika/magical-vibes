# Subgames

Shahrazad (`ARN 10`) composes `StartSubgameEffect` with
`EachPlayerLosesFractionOfLifeRoundedUpEffect(2, SUBGAME_NON_WINNERS)`.
Deck legality continues to use the normal format validator. Subgames support
Casual games, including AI opponents and the optional Planechase variant.

## State and transitions

`GameSession` owns an ordered list of game frames. The first frame is the stable
session identity; the last is the active game. There is no fixed nesting limit.
Each push and pop increments the activation epoch, including returns to a game ID
that was previously active.

The lock order is session lock, mutation action lock, game monitor. A single
mutation scope must never span two frames. `SubgameService` drains requests only
after the originating mutation and its event dispatch have completed. It performs
successive mutations on the parent and child while retaining the session lock.
A failed transition blocks further mutation rather than retrying partial card
transfers.

The parent retains its deferred stack entry and effect index while waiting.
Its libraries become the child's decks. The child has fresh life totals, mana,
zones, counters, pregame decisions, and turn state. Its external card pool and
face-down planar deck travel with the active frame; face-up parent planes remain
in the parent. Opening hands and mulligans tolerate libraries shorter than seven
cards, retaining failed-draw flags until normal state-based checks begin.

On completion, physical traditional cards return to their owners' parent
libraries, which are shuffled. Tokens and spell copies are excluded. The
`subgameCards` inventory also accounts for cards temporarily held by unresolved
interactions. External and command-zone cards are excluded from the return.
The creating spell resumes, applies the result, and finishes its normal
disposition before deferred ancestor triggers are released. A draw has no
winners. Concession ends only the active game; abandonment tears down the session.

## Cards outside the active game

Use `OutsideGameCards.view(game, owner)` for general outside-game searches and
casting. It presents the external pool and owned physical cards in suspended
ancestors, including a currently resolving Shahrazad. Removing a candidate queues
an ancestor transfer; the transition service performs its departure in a separate
ancestor mutation and holds that game's triggers until resumption.

Effects specifically referring to a sideboard still use `playerSideboards`.
An ancestor's resolving spell can lose its physical card while retaining its
resolution continuation; mark its disposition handled to prevent a duplicate
graveyard move when resolution eventually completes.

## Runtime clients and simulation

Exact registry lookup is for frame-specific events. Player actions, reconnect,
timeouts, and AI decisions follow the session's active frame. Lobby listings and
terminal cleanup belong to the root. `SubgameEnded` does not trigger match cleanup.

`GameContext` binds a command to session ID, active game ID, and activation epoch.
The server rejects obsolete contexts and sends the current board and decision.
`ActiveGameChanged` carries a complete recipient-specific board and is projected
before other messages in the transition batch. The client replaces its board,
clears selections and dialogs, and displays the current nesting depth.

Simulation copies clone every frame and continuation. Frozen cards and private
turn-start snapshots are shared read-only; restoring a turn first copies the
snapshot and preserves the current session.
Subgame transitions in simulations never register or remove live games and never
deliver runtime events. The simulator follows the active frame and automatically
keeps opening hands when a rollout starts a subgame.

## Focused verification

Run `scripts/run-subgame-tests.ps1` for card, continuation, registry, request,
reconnect, timeout, projection, and lifecycle regressions. Use `-AiOnly` for the
affected AI regressions and `-TurnOnly` for turn restart and priority-loop checks.
Its init script narrows application test compilation;
ordinary build configuration is unchanged.

Frontend regressions are `websocket-subgame.spec.ts`, `game-subgame.spec.ts`, and
`subgame.browser.spec.ts`. The browser test renders both player perspectives
through five nesting levels and the return path.
