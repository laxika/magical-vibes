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
  source has an empty color list. Fields: type, source card id / name / colors, target permanent
  id, target player id, target card id (a spell on the stack, by card id — the spell a spell
  targets, or the countered spell), amount, and counter type (`COUNTERS_ADDED` only).
  `VisualCueView` and the client's `VisualCue` mirror them.
- **Resolving source.** A chokepoint that is not handed the resolving stack entry asks
  `VisualCueService.resolvingSource(gameData)`: the entry `EffectResolutionService` is resolving
  (it calls `beginResolution`/`endResolution` around every effect loop, nesting), else the entry
  parked in `GameData.pendingEffectResolutionEntry`, else `Source.none()`. An ability is sourced by
  its permanent (live, else the entry's source snapshot), so a face-down source stays anonymous.
  This memory is cosmetic and lives in the service, weakly keyed by `GameData`, never in game state.
  `VisualCueService.sourceOf(gameData, card)` resolves a card a chokepoint was handed (a lifelink
  creature) to the battlefield permanent showing it, else falls back to the resolving source.
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

### Client playback queue

`CuePlaybackService` (`magical-vibes-frontend/.../services/cue-playback.service.ts`, provided by
`GameComponent`) sits in front of `GameComponent.processGameMessage`; every game message goes
through `push`.

- Cues are drawn by the `CUE_PLAYER` (`CuePlayer`: `canPlay(cue)`, `play(cue, {signal, speed})`),
  one cue at a time, in order. The default player is the effect registry (*Effects overlay*
  below), which registers no effects by default, so cues are filtered out and every message is
  applied synchronously, exactly as without the queue.
- A `GAME_STATE` with at least one cue the player can play is held while those cues play; every
  later message (states, `GAME_OVER`, …) queues behind it and is applied in arrival order.
- Decision messages (attackers, blockers, interaction prompts, damage assignment, mulligan,
  reveals, valid-targets responses, `OPPONENT_JOINED`) flush: playback stops and everything
  queued is applied at once, then the decision.
- `ACTIVE_GAME_CHANGED` (subgames, returns to a parent) drops the queue unapplied — it belongs to
  the frame being left — and applies the switch. A disconnect flushes; destroying the component
  drops.
- A message's cues get at most `MAX_CUE_PLAYBACK_MS / speed`; `speed` is a signal on the service.
  Skipping (`skip()`), the time limit and flushing all abort the `AbortSignal` the player got.
- While `playing()`, the game template shows a full-screen transparent catcher: any click or key
  skips instead of acting on the stale board (hand indices would otherwise point at the old hand).

### Effects overlay

The queue's `CUE_PLAYER` is `CueEffectPlayer` (`services/cue-effect-player.service.ts`), the
effect registry. Everything below is frontend-only and cosmetic.

- **Element registry.** `CueElementRegistry` (`services/cue-elements.service.ts`, root) maps ids
  to DOM elements. Elements register with the `appCueAnchor` directive
  (`components/game/cue-overlay/cue-anchor.directive.ts`) and unregister when destroyed:
  `appCueAnchor="permanent"` (permanent id, plus `cueAnchorCardId` = its card id) on every
  battlefield card display including attached auras, `"player"` (player id) on the side panel's
  player badges, and `"stack"` (the entry's `cardId`) on each stack entry. `targetRect(cue)` is the
  target permanent, else the target player, else the stack entry of `targetCardId`; `sourceRect(cue)` is the battlefield permanent
  showing `sourceCardId`, else its stack entry, else null. Detached or zero-size elements count as
  not on screen.
- **Overlay.** `CueOverlayComponent` (`<app-cue-overlay>` in the game template) is one canvas,
  `position: fixed; inset: 0`, `pointer-events: none`, `z-index: 899` (under the skip catcher at
  900). It covers the whole viewport, not just the battlefield, so effects can reach the player
  badges and the stack in the side panel. It draws in viewport coordinates, so an effect uses
  `getBoundingClientRect()` rectangles as they are: CSS zoom (`battlefield-zoom.ts`), the phone
  layout and scrolling are already in them. Every frame it resizes the backing store to its CSS
  size × `devicePixelRatio` (sharp on high-DPI, follows window resizes) and clears. One
  `requestAnimationFrame` loop runs only while an animation is active; several animations may run
  at once on the same loop.
- **Effects.** `CueEffect` = `matches(cue, rects)` + `play(cue, rects, context): Promise<void>`.
  `rects.source` / `rects.target` are live getters (re-measured on each read). Draw with
  `context.animate(durationMs, (draw, progress) => …)`: the duration is at normal speed (the
  player divides it by the queue's speed) and the promise resolves early when playback is
  skipped, flushed or times out. `CUE_EFFECTS` (root token) lists the effects, most specific
  first; the player uses the first that matches. `canPlay` is true only when an overlay is
  attached and an effect matches, so a cue whose target is not on screen does not hold the board.
- **Default: no effects.** `CUE_EFFECTS` is empty unless `localStorage.cueDebugEffects === '1'`,
  which registers `DEBUG_FLASH_EFFECT` (a 400 ms yellow flash over any cue's target). With it
  empty every state applies at once, exactly as before the overlay.

## Catalogue

`VisualCueType` (`magical-vibes-domain/.../model/event/`). "Emitted from" is filled in as each
type is wired up; a type with no source listed is declared but not yet emitted.

| Type | Meaning | Source | Target | Amount | Emitted from |
|---|---|---|---|---|---|
| `DESTROYED` | A permanent was destroyed by a spell or ability. | The destroying spell/ability | Permanent | — | `PermanentRemovalService.destroyPermanentToGraveyard` (also reached by `tryDestroyPermanent`) |
| `SACRIFICED` | A permanent was sacrificed. | The spell/ability that made it be sacrificed, if any | Permanent | — | `PermanentRemovalService.sacrificePermanentToGraveyard` |
| `EXILED` | A permanent was exiled from the battlefield. | The exiling spell/ability, if any | Permanent | — | `PermanentRemovalService.removePermanentToExile` (every public variant) |
| `RETURNED_TO_HAND` | A permanent was returned to its owner's hand. | The bouncing spell/ability, if any | Permanent | — | `PermanentRemovalService.removePermanentToHand` |
| `DIED` | A permanent was put into a graveyard from the battlefield other than by destroy or sacrifice (state-based actions: lethal damage, 0 toughness/loyalty, legend rule, orphaned Auras). | none | Permanent | — | `PermanentRemovalService.destroyPermanentByStateBasedAction`, `removePermanentToGraveyard`, `removeOrphanedAuras`, `enforceAttachmentLegality` |
| `DAMAGE_TO_PERMANENT` | Damage was dealt to a permanent. | The damage source | Permanent | Damage dealt, after prevention and replacement | `DamageSupport.finishPermanentDamage`; combat: `CombatDamageService.recordCombatDamageEvent` |
| `DAMAGE_TO_PLAYER` | Damage was dealt to a player. | The damage source | Player | Damage dealt, after prevention and replacement | `DamageSupport.finishPlayerDamage`; combat: `CombatDamageService.recordCombatDamageEvent` |
| `SPELL_CAST` | A spell was cast (put onto the stack). | The spell | Its first player, first permanent and first spell (`targetCardId`) target | — | `TriggerCollectionService.checkSpellCastTriggers` (every cast path calls it) |
| `SPELL_RESOLVED` | A spell (not an ability) resolved. | The spell | — | — | `StackResolutionService`, at the start of each path where the spell resolves |
| `SPELL_COUNTERED` | A spell (not an ability) was countered. | The countering spell/ability | The countered spell (`targetCardId`) | — | `CounterSupport.notifyCounteredSpell`; `MayPenaltyChoiceHandlerService` ("counter unless" answers) via `CounterSupport.emitCounteredCue` |
| `LIFE_GAINED` | A player gained life. | The source, if any | Player | Life gained | `LifeSupport.completeGainLife`, `applySetLifeTotal` |
| `LIFE_LOST` | A player lost life other than from damage. | The source, if any | Player | Life lost | `LifeSupport.applyLifeLoss`, `applyLifePayment`, `applySetLifeTotal`; direct writers: life casting costs (`SpellCastingService`), `LoseLifeUnlessDiscardEffectHandler`, `MayPenaltyChoiceHandlerService`, `EnchantedCreatureControllerLosesLifeEffectHandler` |
| `COUNTERS_ADDED` | Counters were put on a permanent (entering with counters included). | The resolving spell/ability, if any | Permanent | Number of counters added | `VisualCueService` counter snapshots (see *Counter cues*) |

### Damage cues

Every damage event ends in one of two places once its replacement and prevention effects have
applied (`agent-docs/ARCHITECTURE.md`, "Replacement ordering"), and that is where the cue is
emitted, so redirected, combined (simultaneous) and replayed (journaled) damage all get exactly one
cue per event with the amount dealt:

- **Noncombat:** `DamageSupport.finishPermanentDamage` / `finishPlayerDamage`. Damage that a
  replacement turns into something else (the "-1/-1 counters instead" replacement) is not dealt and
  emits no damage cue; infect and wither damage is still damage and does. The source is the explicit
  damage-source permanent if there is one, else the resolving spell, else the card the ability deals
  damage as (`StackEntry.getEffectiveDamageSourceCard`), resolved to its permanent when it is on the
  battlefield or is the entry's source snapshot, so a face-down source stays anonymous.
- **Combat:** `CombatDamageService.recordCombatDamageEvent`, one cue per `DamagePortion` (one per
  source and recipient of the step, whose damage is all dealt at once, CR 510.2), redirected combat
  damage included. The source is the
  attacking or blocking creature.

Nothing is emitted for 0 damage, so fully prevented damage has no cue.

### Stack cues

- **`SPELL_CAST`** is emitted once per cast, when cast triggers are collected — the spell is on the
  stack with its targets chosen. Copies are not cast and get none. Targets are public once chosen,
  so the cue names the first player, the first battlefield permanent and the first spell among
  them; graveyard cards and abilities on the stack are not named. A face-down spell keeps only
  its card id (`Source.of(StackEntry)`).
- **`SPELL_RESOLVED`** is emitted as a spell starts to resolve, *before* its effects, so a
  resolution's cues read `SPELL_RESOLVED → DAMAGE_TO_… → DIED`. It is emitted only on paths where
  the spell resolves: a spell removed for illegal targets ("fizzles", including Aura spells whose
  target is gone) emits nothing, and nothing says it fizzled. Abilities never emit it. A resumed
  resolution (after a prompt) does not emit it again.
- **`SPELL_COUNTERED`** is sourced by the resolving countering spell or ability, or by the one
  parked while its "unless" payment is answered (Mana Leak). A Guile-style "exile instead of
  countering" is not a counter and emits nothing; countering an ability emits nothing.

### Life cues

Emitted where the life total actually changes, with the amount actually gained or lost, and
nothing for 0 (a gain capped at the maximum, a prevented loss). Life loss from **damage** emits
only the damage cue. A life gain replaced by something else emits the cue of what happened
instead (a loss, or nothing for draws). The source is `sourceOf` the event's source card when
the chokepoint has one, else the resolving spell/ability; a life casting cost has no source.

### Counter cues

`Permanent.setCounterCount` is called from hundreds of engine paths, so no path emits
`COUNTERS_ADDED`. Instead `GameMutationCoordinator` calls `VisualCueService.actionStarted` when an
outer action starts (a snapshot of every battlefield permanent's counters) and `actionCompleting`
just before it completes its batch. In between, every other cue, `beginResolution` and
`endResolution` first *flush*: compare counters with the snapshot, emit one cue per permanent and
counter type whose count rose (amount = the increase), and take a new snapshot. So:

- counter cues sit in engine order between the other cues (`SPELL_RESOLVED → COUNTERS_ADDED`);
- several placements in one window merge into one cue (Dragonscale Boon: one cue, amount 2);
- the source is the spell or ability resolving at flush time (`resolvingSource`), else none;
- a permanent that entered in the window counts from zero, so entering with counters emits a
  cue; a permanent that left before the flush emits none; decreases emit nothing;
- counters set outside an action (test setup) are never reported, and simulations snapshot
  nothing.

Players' counters (poison, energy, …) have no cue type.

### Leave-the-battlefield cues

Emitted by `PermanentRemovalService`'s public entry points (`emitLeaveCue`), once per permanent
and only when it is really gone from the battlefield afterwards:

- **The cue names the event, not the final zone.** A destroyed creature that a replacement exiles
  (Rest in Peace, Kalitas) or sends to its hand or library is still `DESTROYED`; a bounce that is
  replaced by exile is still `RETURNED_TO_HAND`. The replacement reroutes inside the service call
  non-emitting private variants, so nothing is emitted twice.
- **Pending replacement choices emit nothing.** When competing death replacements open a
  `DeathExileReplacementChoice` (or a "may put it on top of its library" prompt), the permanent
  stays put and no cue goes out. `resolveDeathExileReplacementChoice` emits the cue once the
  removal resumes, with the type from the choice's destroy/sacrifice flags. A declined "may"
  library replacement (`removePermanentToGraveyardAfterDecliningLibraryReplacement`) has lost its
  cause and emits `DIED`; an accepted one goes to the library and emits nothing.
- **Survivors emit nothing**: indestructible, regeneration, shield counters and other destruction
  replacements return before anything moves.
- **Not covered:** moves to the library, command zone, stack (spellmorph) and outside the game
  have no cue type. Some engine sacrifices bypass `sacrificePermanentToGraveyard` (Saga
  sacrifice, "sacrifice unless you pay" with no mana cost, an Aura sacrificed to make its
  creature indestructible, same-name sacrifice on entry) and call the plain `removePermanentToGraveyard`, so they show as
  `DIED`.
- **Target only by id.** The cue never names the permanent that left, so a face-down permanent's
  cue reveals nothing; the client already knows the id from `PermanentView`.

## Testing

Engine changes that emit cues get tests that run an action through the harness and assert the
emitted cues (`VisualCueServiceTest`, `DamageVisualCueTest`, `LeaveBattlefieldVisualCueTest` and
`SpellLifeCounterVisualCueTest`
show how to read them from a `TestGameEventSubscriber` batch or from the projected `GAME_STATE`
JSON on a `FakeConnection`). Projection rules are pinned
in `GameEventProjectionSubscriberTest`.

The overlay is specified in `cue-overlay.browser.spec.ts` (the real `GameComponent` in Chromium,
reading canvas pixels over the target and its neighbours: desktop, a zoomed-out crowded board, the
phone layout, `devicePixelRatio` 2, player targets) and `cue-effect-player.service.spec.ts`
(registry lookups and effect selection with stubbed rectangles). Browser specs need Playwright's
Chromium (`npx playwright install chromium`).

Client playback is specified in `cue-playback.service.spec.ts` (a fake `CuePlayer` whose cues
finish when the test says so, with fake timers) and `game-cue-playback.spec.ts` (wiring in
`GameComponent`).
