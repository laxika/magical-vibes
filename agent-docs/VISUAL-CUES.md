# VISUAL CUES

Purpose: the catalogue and contract for visual cues — cosmetic reports of what happened in an
action, which the client animates (Lightning Bolt zapping its target, a creature dying in smoke)
before it applies the state the action led to. Read this before emitting a cue or changing the
catalogue. The roll-out plan is `look-n-feel-redesign.md` at the repository root.

## Rules

- **Cosmetic only.** No rules code reads a cue, and no engine logic may be reordered or
  restructured to make emitting one easier. Adding a cue never changes the order or outcome of
  anything the engine does.
- **Emit through `VisualCueService`, from chokepoints.** Engine code calls
  `VisualCueService.emit(gameData, type, source, targetPermanentId, targetPlayerId, amount)` from
  the shared paths (`PermanentRemovalService`, the damage paths, stack resolution), never from a
  card class, so every card gets cues for free. Never call
  `GameMutationCoordinator.emit` with a `VisualCue` directly.
- **Hidden information.** Build the source with `VisualCueService.Source.of(Permanent)` or
  `Source.of(StackEntry)`: a face-down permanent or spell keeps its card id but carries no name
  and no colors. `Source.of(Card)` is only for a face-up card in a public zone; `Source.none()`
  is for a cue without a source (a state-based death). The default audience is every player; a
  cue about a card in a hidden zone must pass an explicit audience naming only the players who
  may see it. When unsure, leave the identifying fields out.
- **Fact contract.** `GameEventFact.VisualCue` holds only ids, scalars and an immutable color
  list (`GameEventPayloadArchitectureTest`). Every field except the type may be null; a missing
  source has an empty color list.
- **No-op contexts.** `VisualCueService` emits nothing in a simulation copy
  (`GameData.simulation`), so MCTS pays nothing for cues, and nothing outside a mutation scope
  (test setup that moves cards directly), so a chokepoint never has to know where it runs.

## Delivery

Cues ride on `GameStateMessage.visualCues` (`VisualCueView`), never in a message of their own.
`GameEventProjectionSubscriber` collects the batch's cues in emission order, filters each by its
envelope audience per recipient, and attaches a recipient's cues to that recipient's **first**
state message of the batch — the same rule as `newLogEntries`. A projected state is always the
state after the whole action, so every cue of the batch belongs in front of it. A later state
message in the same batch (after a decision barrier, or for a second audience) carries none.

A batch in which a recipient receives no state message — no `StateInvalidated` reaches them, or an
active-game switch replaces their board with a join message — delivers no cues to that recipient;
they are dropped, never sent on their own. Anything that emits a cue also changes state or appends
a log (which invalidates every player's view), so this only drops cues that would have had no
state to play in front of.

The client (`websocket.service.ts`, `VisualCue`) plays a state's cues in order on the board as it
currently is, then applies the state.

## Catalogue

`VisualCueType` (`magical-vibes-domain/.../model/event/`). "Emitted from" is filled in as each
type is wired up; a type with no source listed is declared but not yet emitted.

| Type | Meaning | Source | Target | Amount | Emitted from |
|---|---|---|---|---|---|
| `DESTROYED` | A permanent was destroyed by a spell or ability. | The destroying spell/ability | Permanent | — | — |
| `SACRIFICED` | A permanent was sacrificed. | The spell/ability that made it be sacrificed, if any | Permanent | — | — |
| `EXILED` | A permanent was exiled from the battlefield. | The exiling spell/ability | Permanent | — | — |
| `RETURNED_TO_HAND` | A permanent was returned to its owner's hand. | The bouncing spell/ability | Permanent | — | — |
| `DIED` | A creature died by a state-based action. | none | Permanent | — | — |
| `DAMAGE_TO_PERMANENT` | Damage was dealt to a permanent. | The damage source | Permanent | Damage dealt, after prevention and replacement | — |
| `DAMAGE_TO_PLAYER` | Damage was dealt to a player. | The damage source | Player | Damage dealt, after prevention and replacement | — |
| `SPELL_CAST` | A spell was put onto the stack. | The spell | Its target, if public | — | — |
| `SPELL_RESOLVED` | A spell resolved. | The spell | — | — | — |
| `SPELL_COUNTERED` | A spell was countered. | The countering spell/ability | — | — | — |
| `LIFE_GAINED` | A player gained life. | The source, if any | Player | Life gained | — |
| `LIFE_LOST` | A player lost life other than from damage. | The source, if any | Player | Life lost | — |
| `COUNTERS_ADDED` | Counters were put on a permanent. | The source, if any | Permanent | Number of counters | — |

## Testing

Engine changes that emit cues get tests that run an action through the harness and assert the
emitted cues (`VisualCueServiceTest` shows how to read them from a `TestGameEventSubscriber`
batch or from the projected `GAME_STATE` JSON on a `FakeConnection`). Projection rules are pinned
in `GameEventProjectionSubscriberTest`.
