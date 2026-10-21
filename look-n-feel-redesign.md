# Look-and-feel redesign: spell and combat effects

A step-by-step plan to add Hearthstone-style visual effects to games. Examples: Lightning Bolt
zaps the creature it hits, Doom Blade drops a skull out of a cloud onto its target, and a
creature that dies fades out in smoke. The engine reports *what happened* as visual cues, and the
frontend decides *how it looks* and plays it on an overlay above the board before applying the
new game state.

To start a step, use the prompt at the bottom with the step number. Steps are meant to be done in
order; each one lists what it depends on.

## Status

| Step | Title | Status |
|---|---|---|
| 1 | `VisualCue` fact and wire plumbing | done |
| 2 | Cues for leaving the battlefield | open |
| 3 | Cues for damage | done |
| 4 | Cues for the stack, life and counters | open |
| 5 | Frontend cue model and playback queue | open |
| 6 | Element registry and effects overlay | open |
| 7 | Default effects by cue type and color | open |
| 8 | Showpiece effects: Lightning Bolt and Doom Blade | open |
| 9 | Effect settings and reduced motion | open |
| 10 | Lottie support for authored animations | open |
| 11 | Add a showpiece effect for a card (repeatable) | open |

## Why

Today the board only shows the *result* of an action. When Doom Blade resolves, the next
`GameStateMessage` simply no longer contains the creature, and its generic leave animation plays.
Nothing on screen says *what* removed it or *where* the damage came from. Visual effects make the
game readable (you see which spell hit which creature) and make it feel alive.

## Design

### The engine reports cues; it never acts on them

- Add a new `GameEventFact` variant, `VisualCue`. It goes through the existing
  `GameMutationCoordinator.emit(...)`, so cues are:
  - batched per completed action, in the order they happened (damage → dies → state);
  - sent to the right players through `GameEventAudience` (hidden information stays hidden);
  - suppressed in AI simulations automatically (`DispatchMode.SUPPRESSED_SIMULATION`).
- Cues carry only identifiers and scalars, following the `GameEventFact` contract: cue type,
  source card id and name (or none), source colors, target permanent/player id, and amount.
- Cues are cosmetic. No rules code reads them, so they can never change game behaviour. Adding a
  cue must never change the order or outcome of anything the engine does.
- Cues are emitted from the engine's chokepoints (`PermanentRemovalService`, the damage code paths,
  stack resolution), never from individual card classes. Every card gets cues for free.

### Cues reach the client together with the state they lead to

`GameEventProjectionSubscriber` already puts the batch's new log entries into the
`GameStateMessage`. Cues go the same way, in a new `visualCues` field, so the client always
receives "what happened" and "the state afterwards" in one message and can play the first
before applying the second.

### The frontend owns the look

- **Playback queue:** when a state message has cues, the client plays them on the board *as it
  currently is* (the Doom Blade target is still there), then applies the new state, and the
  existing leave animation runs. Messages that arrive in the meantime wait in the queue. The
  queue can be sped up or skipped, and has a hard time limit, so visuals never block the game.
- **Effects overlay:** a single canvas over the battlefield (`pointer-events: none`). It finds
  cards and players on screen through an element registry (permanent id → DOM element) and
  `getBoundingClientRect()`. Cards themselves stay DOM and keep moving with CSS/Web Animations.
  A full-canvas board is out of scope; the overlay is designed so the battlefield could move into
  the same scene later without changing cues or the queue.
- **Choosing an effect:** a per-card table for showpiece cards (Lightning Bolt, Doom Blade), and
  otherwise a default chosen by cue type and source color (red damage = fire, black destroy =
  smoke, white exile = fading light, blue bounce = swirl).

## Ground rules for every step

- Read `agent-docs/ARCHITECTURE.md` before changing engine, networking or frontend code.
- Cues are cosmetic: never read them in rules code, and never reorder or restructure engine logic
  to make emitting a cue easier. Rules accuracy comes first.
- Respect hidden information. A face-down source or target shows no name; a cue about a card in a
  hidden zone goes only to players who may see it. When unsure, leave out the identifying fields.
- `GameEventFact` records hold only ids, scalars and immutable snapshots — never `Card`,
  `Permanent`, `StackEntry` or `GameData`.
- Engine changes get engine tests that run actions through the harness and assert the emitted
  cues. Frontend changes get vitest specs next to the code (`*.spec.ts`, or `*.browser.spec.ts` for
  anything that needs real layout or animation).
- Verify any CR number with the `rules` MCP before writing it. This plan cites none on purpose.
- Run only the tests for the code you touched (Gradle `--tests` filters after each module's test
  task; a single vitest spec for frontend). Never run the full suite; ask the user to run it.
  Don't commit until the user says so.
- When you add a new effect type or change the cue catalogue, update the relevant `agent-docs/`
  file (create `agent-docs/VISUAL-CUES.md` in step 1 and keep it current).
- At the end of a step, set its row in **Status** to `done`. Record anything a later step needs to
  know in **Findings** under the step number, and list the tests you had to change and why.

## Steps

### Step 1: `VisualCue` fact and wire plumbing

**Depends on:** nothing.

- Add `GameEventFact.VisualCue` (and a `GameEventKind` value) with a `VisualCueType` enum. Start
  with the types steps 2–4 need: `DESTROYED`, `SACRIFICED`, `EXILED`, `RETURNED_TO_HAND`,
  `DIED`, `DAMAGE_TO_PERMANENT`, `DAMAGE_TO_PLAYER`, `SPELL_CAST`, `SPELL_RESOLVED`,
  `SPELL_COUNTERED`, `LIFE_GAINED`, `LIFE_LOST`, `COUNTERS_ADDED`. Fields: type, source card id,
  source name, source colors, target permanent id, target player id, amount (any may be null).
- Add a `VisualCueView` to `magical-vibes-networking` and a `visualCues` list to
  `GameStateMessage` (empty by default; update the secondary constructor and all call sites).
- In `GameEventProjectionSubscriber`, collect the batch's cues in order, filter them per recipient
  by audience, and attach them to that recipient's state message, the same way `newLogEntries`
  is handled. Decide and document what happens to cues in a batch with no `StateInvalidated`.
- Add a small helper (e.g. `VisualCueService`) that the engine calls to emit a cue, so steps 2–4
  don't repeat the audience and redaction logic. Check that emitting from places that run outside
  a mutation scope (if any, such as test setup) does not throw; if they exist, make the helper a
  no-op there and record it in Findings.
- Add the TypeScript model for `visualCues` in `websocket.service.ts` and update the tutorial mock
  data so it still compiles. The frontend does nothing with cues yet.
- Create `agent-docs/VISUAL-CUES.md` describing the cue catalogue and the rules above.

**Done when:** a test that emits a cue inside an action sees it in the projected
`GameStateMessage` for the right recipients, simulations emit nothing, and nothing else changes.

### Step 2: Cues for leaving the battlefield

**Depends on:** step 1.

Emit cues from `PermanentRemovalService` (`destroyPermanentToGraveyard`,
`destroyPermanentByStateBasedAction`, `removePermanentToGraveyard`, `removePermanentToHand`, the
exile paths, and sacrifice). The cue must be emitted only when the permanent actually left. When a
replacement choice is pending (see `leave-battlefield-redesign.md`), emit nothing until the move
really happens. The source is the resolving spell or ability where one exists (Doom Blade);
state-based deaths have no source. Tests: Doom Blade → `DESTROYED` with Doom Blade as the source;
lethal damage → `DIED`; Unsummon → `RETURNED_TO_HAND`; a regenerated or indestructible creature
emits no destroy cue.

**Done when:** each kind of removal has a test, and a face-down permanent's cue carries no name.

### Step 3: Cues for damage

**Depends on:** step 1.

First survey every place damage is actually dealt: `DamageSupport`, `CombatDamageService`,
`CreatureDamageStep`, and every caller of `Permanent.addMarkedDamage` and player life loss from
damage. Record the list in Findings. Emit `DAMAGE_TO_PERMANENT` / `DAMAGE_TO_PLAYER` with the
amount *after* prevention and replacement, and nothing for fully prevented damage. Combat damage
emits one cue per damage assignment, with the attacking or blocking creature as the source.
Tests: Lightning Bolt on a creature and on a player, a combat trade, prevented damage, and damage
that a replacement effect redirects.

**Done when:** every damage path in the survey emits cues, and the tests pass.

### Step 4: Cues for the stack, life and counters

**Depends on:** step 1.

Emit `SPELL_CAST` (with chosen targets, if public), `SPELL_RESOLVED`, `SPELL_COUNTERED`,
`LIFE_GAINED`, `LIFE_LOST` (not from damage; step 3 covers that) and `COUNTERS_ADDED`. Keep the
volume sane: cues for the same thing in one action may be merged (e.g. ten +1/+1 counters → one
cue with amount 10). Tests for each type.

**Done when:** the cue catalogue in `agent-docs/VISUAL-CUES.md` is fully emitted and tested.

### Step 5: Frontend cue model and playback queue

**Depends on:** step 1 (steps 2–4 make it useful, but are not required to build it).

Find where `game.component.ts` applies an incoming `GAME_STATE` and put a playback queue in front
of it (a new service, e.g. `cue-playback.service.ts`):

- A message with no cues is applied straight away, exactly as today.
- A message with cues: play its cues in order (through an injectable player that is a no-op until
  step 6), then apply the state. Later messages wait their turn.
- A skip/speed control, a maximum total time per message, and an immediate flush when the player
  must make a decision, so the UI never lags behind a prompt.
- Subgames and active-game switches flush the queue.

Specs: ordering, flushing on prompts, and the time limit, using a fake player.

**Done when:** games behave exactly as before with no effects registered, and the specs pass.

### Step 6: Element registry and effects overlay

**Depends on:** step 5.

- An element registry: permanents, player panels (life totals) and the stack register their DOM
  element by id, and unregister on destroy. The overlay asks it for on-screen rectangles.
- The overlay component: one full-size canvas over the battlefield, `pointer-events: none`,
  resized with the window and the battlefield zoom (`battlefield-zoom.ts`), sharp on high-DPI
  screens. It runs one `requestAnimationFrame` loop only while an effect is playing.
- An effect interface (`play(cue, rects, ctx): Promise<void>`) and a registry that maps a cue to an
  effect. This replaces the no-op player from step 5.
- One debug effect (a flash on the target) to prove positioning works on desktop and phone layouts
  (`game-phone.css`).

**Done when:** a browser spec shows the debug effect lands on the right permanent, and phone and
zoomed layouts are checked.

### Step 7: Default effects by cue type and color

**Depends on:** step 6 and the cues it covers (steps 2–4).

Implement the fallback effects: a small particle system on the canvas for fire, smoke, sparks and
light; floating damage and life numbers; a projectile from the source (stack or permanent) to the
target for damage. Choose by cue type and source color, with a neutral version for colorless
and multicolor sources. Keep each effect short (≤ ~700 ms) so long chains of cues stay quick.

**Done when:** every cue type has a default effect and nothing in a normal game is left without
an effect.

### Step 8: Showpiece effects: Lightning Bolt and Doom Blade

**Depends on:** step 7.

Add a per-card effect table keyed by card name (so reprints share it). Lightning Bolt: a jagged
bolt from the top of the screen to the target with a flash and a short shake. Doom Blade: a dark
cloud gathers over the target, a skull forms and drops, then the creature fades in smoke. Build
both with canvas and SVG first; step 10 lets them move to Lottie later if needed.

**Done when:** both effects play in a real game (check with the `run` skill and a screenshot), and
other cards still use the defaults.

### Step 9: Effect settings and reduced motion

**Depends on:** step 6.

A setting for effects (full / fast / off) saved per user like other settings, and a check for
`prefers-reduced-motion` that switches to "fast" by default. "Off" makes the queue apply state
straight away. Add the click-to-skip control if step 5 did not already include a visible one.

**Done when:** each setting has a spec, and "off" behaves exactly like before this plan.

### Step 10: Lottie support for authored animations

**Depends on:** step 8.

Add `lottie-web` (pin an exact version), an effect that plays a Lottie JSON file over a target's
rectangle, and a folder under `public/` for animation files with a short note on where they come
from and their licenses. Lazy-load the library so it isn't in the initial bundle. Optionally move
Doom Blade's skull to a Lottie file if it looks better.

**Done when:** one effect plays from a Lottie file and the initial bundle size didn't grow.

### Step 11: Add a showpiece effect for a card (repeatable)

**Depends on:** step 8 (step 10 for Lottie-based effects).

Each run adds one card (or a small family, e.g. all "destroy target creature" black removal) to
the per-card table, reusing existing effect building blocks where possible. Record in Findings
which cards have showpiece effects. This step stays `open`.

## Findings

Steps add notes here for later steps, under their number.

### Step 1

- **Emitting:** call `VisualCueService.emit(gameData, type, source, targetPermanentId,
  targetPlayerId, amount[, audience])` (`magical-vibes-engine/.../service/event/`). Build the
  source with `VisualCueService.Source.of(Permanent | StackEntry | Card)` or `Source.none()`;
  the `Permanent`/`StackEntry` factories drop name and colors when face down. The default
  audience is every player.
- **No-op contexts:** the helper does nothing when `GameData.simulation` is set or when called
  outside a mutation scope (`GameMutationCoordinator.isInAction` is false). Production never
  reaches the chokepoints outside a scope, because every outer boundary opens one and log
  appends already throw outside one; test code that calls a service bare (`harness.addToBattlefield`,
  direct `PermanentRemovalService` calls without `harness.inMutationScope`) does, and now
  silently gets no cue instead of throwing. Tests that assert cues must run the action through
  `gs`/`harness` or wrap it in `harness.inMutationScope`.
- **Delivery:** a recipient's cues ride on their *first* `GameStateMessage` of the batch, like
  `newLogEntries`; later state messages in the same batch carry none. A batch with no state
  message for a recipient (no `StateInvalidated`, or an active-game switch) drops that
  recipient's cues. Real chokepoints always change state or log, so this should not bite.
- **Colors** are the card's printed `Card.getColors()`, not layer-effective colors. If a step
  wants painted/changed colors, compute them with `GameQueryService` at the chokepoint and
  build the `Source` by hand.
- **Locating a source on screen:** cues carry `sourceCardId`, not a permanent or stack-entry id.
  The client finds it as `PermanentView.card.id` on the battlefield or `StackEntryView.cardId`
  on the stack (an ability on the stack carries its source card's id). A face-down permanent's
  card id is already public in `PermanentView`, so cues keep it.
- **Wire:** `GameStateMessage` is now `@Builder(toBuilder = true)`; derive variants with
  `toBuilder()` (the subscriber attaches cues that way, so `GameViewProjectionFactory` and its
  mocks are unchanged). JSON enums serialize by name: `type` is e.g. `"DESTROYED"`, colors are
  e.g. `"RED"`; null fields are sent as `null`, `visualCues` is always a list.
- **Frontend:** `VisualCue`/`VisualCueType` and the required `GameStateNotification.visualCues`
  are in `websocket.service.ts`; nothing reads them yet. No tutorial mock or spec builds a
  `GameStateNotification` (they build `Game`), so no mock data had to change.
  `tsc -p tsconfig.spec.json` has one error that predates this step
  (`targeting-choice.service.spec.ts:413`, `CommanderView.revealableMissionIds`).
- **Tests:** new `VisualCueServiceTest` (harness: projected cues per recipient and in order,
  private audience, face-down redaction, simulation, outside-scope no-op, empty list by
  default); two new cases in `GameEventProjectionSubscriberTest` (first-state-message and
  audience filtering; no state message → nothing sent). No existing test had to change.

### Step 3

- **Survey — where damage is actually dealt.** Every damage event, noncombat or combat, runs
  through `DamageSupport.processDamageEvent` / `continueDamage` (replacement ordering) and ends in
  `DamageSupport.finishDamage`, which hands it to one of three result stages:
  - `DamageSupport.finishPermanentDamage` — all noncombat damage to creatures, planeswalkers and
    battles (the only noncombat `Permanent.addMarkedDamage` caller), including Bite/fight damage
    with an explicit source permanent, divided damage, simultaneous (combined) damage, redirect
    shields (`processPendingRedirectDamage`, `dealRedirectedDamage`) and journal resumes.
  - `DamageSupport.finishPlayerDamage` — all noncombat damage to players (life loss, poison,
    "can't lose life", Worship floors), including Eye for an Eye reflections and redirects.
  - `CombatDamageService.recordCombatDamageEvent` — all combat damage: `recordCombatEventToPlayer`,
    `recordCombatEventToAttackedPermanent` (planeswalkers, battles), `recordCombatEventToCreature`
    (marked damage and infect/wither counters) and `dealRedirectedCombatDamage` (Pariah-style
    redirects inside the step). The marked damage itself is applied later in the same step by
    `applyStepDamageToPermanents` / `applyPlaneswalkerDamage` / `applyDamageToEachPlayer`, from the
    maps these record into.
  - `CreatureDamageStep` deals nothing — it is one prevention/replacement step of the permanent
    pool. Other `playerLifeTotals` writers (`LifeSupport`, life costs, "lose life unless") are life
    loss, not damage, and belong to step 4.
- **Emission:** the cue is emitted at those result stages, only for damage > 0, so replays of a
  journaled resolution don't repeat it and fully prevented damage has none. Replacing damage with
  something else (`noncombatDamageToOpponentCreatureAsCounters`, the mill replacement, Dralnu) is
  not damage and emits no damage cue. Infect/wither damage is damage and emits one.
- **Ordering inside a combat step:** combat cues are emitted as each recipient's event finishes,
  slightly before the step applies the marked damage. All of that happens in one action, so it
  doesn't show on the client — except when a combat damage event stops for a replacement-order or
  "you may" choice: the cues of the events already finished then go out with the prompt's state,
  and the damage appears on the board only after the answer. Step 5's queue should not assume a
  damage cue's state already shows the damage.
- **Sources:** combat cues use `Source.of(creature)` (the live permanent, or the portion's snapshot
  if it already left). Noncombat cues resolve the source in `DamageSupport.damageCueSource`; a
  face-down source keeps its card id but no name or colors. `sourceCardId` may name a card that is
  no longer anywhere visible (a sacrificed Mogg Fanatic, a resolved spell), so step 7's projectile
  needs a fallback origin.
- **Planeswalker damage** from combat and noncombat sources is `DAMAGE_TO_PERMANENT`, the same as
  creature damage; distinguish by what the client's target permanent is if needed.
- **Pre-existing failures (not caused by this step):** `CombatDamageServiceTest` has 40 failing
  tests on a clean `main` (NPE: `StateBasedActionService.storiedEffectHandler` is null in its
  hand-built wiring), and `PariahTest.multiplePariahsOfferRedirectionChoice` fails on a clean `main`.
- **Tests:** new `DamageVisualCueTest` (harness: Lightning Bolt on a creature and on a player, the
  projected cue on the wire, a combat trade, unblocked damage, a face-down attacker, fully and
  partly prevented noncombat damage, Fog, and Pariah redirecting noncombat and combat damage).
  `CombatDamageServiceTest` changed only to pass a mocked `VisualCueService` to the new
  `CombatDamageService` constructor parameter.

## Prompt

Replace `<N>` with the step number:

```
Implement step <N> of look-n-feel-redesign.md. First read the whole file (Why, Design, Ground
rules) and step <N>'s section, and check in the Status table that every step it depends on is done;
if one isn't, stop and tell me. Follow the ground rules: read agent-docs/ARCHITECTURE.md before
touching engine, networking or frontend code, keep cues cosmetic (no rules code may depend on
them), respect hidden information, verify any CR number with the rules MCP, and test behaviour
through the engine harness or vitest specs. Run only the tests for what you touched — never the
full suite; ask me to run it. When the step is done, mark it done in Status, add anything later
steps need under Findings, and tell me which tests changed and why. Don't commit until I say so.
```
