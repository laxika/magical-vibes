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
| 2 | Cues for leaving the battlefield | done |
| 3 | Cues for damage | done |
| 4 | Cues for the stack, life and counters | done |
| 5 | Frontend cue model and playback queue | done |
| 6 | Element registry and effects overlay | done |
| 7 | Default effects by cue type and color | done |
| 8 | Showpiece effects: Lightning Bolt and Doom Blade | done |
| 9 | Effect settings and reduced motion | open |
| 10 | Lottie support for authored animations | done |
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

### Step 2

- **Where:** `PermanentRemovalService`'s public entry points emit through a private
  `emitLeaveCue`, which checks that the permanent is really off the battlefield afterwards.
  Replacement reroutes inside the service (exile instead, return to hand instead, library) call
  non-emitting private variants, so the cue names the *event* (a Doom Bladed creature exiled by
  Rest in Peace is `DESTROYED`) and is emitted once. Full rules in `agent-docs/VISUAL-CUES.md` →
  *Leave-the-battlefield cues*.
- **`DIED` is wider than "creature died by SBA":** it now means any put-into-graveyard that is
  not a destroy or a sacrifice (lethal damage, 0 toughness/loyalty, legend and world rules,
  orphaned or illegally attached Auras), always with no source. "Dies" is CR 700.4, which covers
  any permanent. Step 7 should not assume the target was a creature.
- **Resolving source for chokepoints without an entry:** `VisualCueService.resolvingSource(gameData)`
  (fed by `beginResolution`/`endResolution` in `EffectResolutionService.resolveEffectsFrom`, falling
  back to `pendingEffectResolutionEntry`). Step 4 can use it for life and counter cues.
  Removals outside any resolution (costs, combat, delayed end-step actions) have no source.
- **Pending choices:** nothing is emitted while a `DeathExileReplacementChoice` or a "may put it on
  top of its library" prompt holds the permanent. The resumed removal emits the cue in the batch
  that answers the prompt; its source is the resolving or parked entry if there still is one,
  otherwise none. A declined "may" library replacement has lost its cause and emits `DIED`.
- **No cue type yet** for moves to the library, command zone, stack (spellmorph) or outside the
  game; add one to the catalogue if step 7 wants them.
- **Mislabelled sacrifices:** a few engine sacrifices go through the plain
  `removePermanentToGraveyard` rather than `sacrificePermanentToGraveyard` (Saga, "sacrifice unless
  you pay" with no mana cost, same-name sacrifice on entry, an Aura sacrificed to make its creature
  indestructible) and show as `DIED`. Changing them would touch rules code, so I left them.
- **Order in one action:** cues follow the engine's order, so lethal damage gives
  `DAMAGE_TO_PERMANENT` then `DIED` in the same state message.
- **Pre-existing failures (not caused by this step, same counts on a clean `main`):**
  `EffectResolutionServiceTest` (43 of 45), `PermanentRemovalServiceTest` +
  `StateBasedActionServiceTest` (110 of 122, mostly hand-built wiring with null collaborators),
  and the architecture tests `SimulationCopyCompletenessTest`, `CostEffectClassificationTest` and
  `CardImmutabilityArchTest`.
- **Tests:** new `LeaveBattlefieldVisualCueTest` (Doom Blade → `DESTROYED` sourced by Doom Blade and
  projected to the client; Royal Assassin's ability → `DESTROYED` sourced by the Assassin; Lightning
  Bolt lethal damage → damage cue then sourceless `DIED`; Unsummon → `RETURNED_TO_HAND`; Swords to
  Plowshares → `EXILED`; Diabolic Edict → `SACRIFICED`; indestructible and regenerated creatures emit
  nothing; a face-down creature's cue doesn't carry its name, on the fact or the wire; Kalitas
  against Gravebane Zombie emits nothing while the choice is open and `SACRIFICED` after it).
  Changed only to supply the new collaborator: `EffectResolutionServiceTest` (constructor argument,
  a mocked `VisualCueService`) and `PermanentRemovalServiceTest` (a `@Mock VisualCueService` for
  `@InjectMocks`).

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

### Step 4

- **Two new cue fields** (fact, `VisualCueView`, TS `VisualCue`): `targetCardId` (a spell on the
  stack by card id — the spell a `SPELL_CAST` targets, the spell a `SPELL_COUNTERED` countered) and
  `counterType` (`COUNTERS_ADDED` only, e.g. `"PLUS_ONE_PLUS_ONE"`, `"MINUS_ONE_MINUS_ONE"`,
  `"LOYALTY"`). `CueElementRegistry.targetRect` now falls back to the stack entry of
  `targetCardId`, so a counterspell effect can land on the spell it hits. Step 7 should pick the
  counters look by `counterType` (green growth vs black wither), not only by source color.
- **Where each type comes from** is in the catalogue in `agent-docs/VISUAL-CUES.md`. In short:
  `SPELL_CAST` from `TriggerCollectionService.checkSpellCastTriggers` (all cast paths), with the
  first player / permanent / spell target; `SPELL_RESOLVED` from `StackResolutionService`;
  `SPELL_COUNTERED` from `CounterSupport` (+ the two "counter unless" answers in
  `MayPenaltyChoiceHandlerService`); life cues from `LifeSupport` and four direct life writers.
- **Order inside a resolution:** `SPELL_RESOLVED` comes *first*, before the spell's effects
  (`SPELL_RESOLVED → DAMAGE_TO_PERMANENT → DIED`), so step 7 can show the spell resolving and
  then what it does. `SPELL_CAST` arrives in the batch that cast the spell, whose state already
  shows the spell on the stack — on the stale board the client plays it on, the spell is still
  in hand (or nowhere, for the opponent's hidden hand), so `rects.source` is null and the effect
  needs a fallback origin (the caster's player badge).
- **Fizzles emit nothing.** A spell removed for illegal targets doesn't resolve and isn't
  countered, so it gets neither cue; its card just leaves the stack. Add a `SPELL_FIZZLED` type to
  the catalogue if step 7 wants to show it (the fizzle sites are the `" fizzles ("` logs in
  `StackResolutionService`).
- **Counter cues come from snapshots, not chokepoints.** Counters are written from ~400 engine
  sites with no single path (and `PermanentCounterSupport.notifyCountersPlaced` misses many, such
  as combat wither/infect, loyalty abilities and entering with counters). So `VisualCueService`
  snapshots battlefield counters when an outer action starts (the coordinator calls
  `actionStarted` / `actionCompleting`) and emits the increases before every other cue, at every
  resolution boundary and at the end of the action. Effects: one cue per permanent and counter
  type per window (merged amounts), engine order kept, source = whatever is resolving at the flush
  (else none — combat, costs). Entering with counters emits a cue (planeswalkers entering with
  loyalty, sagas getting lore). Decreases and players' counters (poison) have no cue.
- **Life:** amounts are what actually changed (capped gains, multiplied losses); 0 emits nothing.
  Damage emits only the damage cue, never `LIFE_LOST`. Life casting costs emit `LIFE_LOST` with no
  source (the spell is mid-cast). Lifelink life gain is sourced by the lifelink permanent when it
  is on the battlefield, through `VisualCueService.sourceOf(gameData, card)`.
- **Pre-existing failures (same on a clean `main`):** `SpellCastingServiceTest` (10),
  `EnterTriggerCollectorServiceTest` (3) and `ManaLeakTest.mayPayForUncounterableSpell`.
- **Tests:** new `SpellLifeCounterVisualCueTest` (harness, 14 cases: cast cue with a permanent, a
  player and a spell target — on the wire too; a face-down cast is anonymous; resolve cue before
  the damage cue; a creature spell resolves; a fizzle and an activated ability emit no resolve cue;
  Counterspell's countered cue; Mana Leak's countered cue after declining to pay; Healing Salve's
  life gain; Sign in Blood's life loss and none from Bolt's damage; Dragonscale Boon's two counters
  as one cue with the type, on the wire too; counters set in test setup aren't reported). Frontend:
  a new `cue-effect-player.service.spec.ts` case for the stack fallback in `targetRect`.
  Changed: `LeaveBattlefieldVisualCueTest.lethalDamageDies` (its exact cue list for a cast-and-resolved
  Lightning Bolt now starts with `SPELL_CAST, SPELL_RESOLVED`); `GameEventProjectionSubscriberTest`
  (the two new constructor arguments); `PlayerInteractionHandlerTestSupport` (supplies a mocked
  `VisualCueService`, the new constructor dependency of `LoseLifeUnlessDiscardEffectHandler`); the
  cue fixtures in `cue-playback.service.spec.ts`, `cue-effect-player.service.spec.ts`,
  `game-cue-playback.spec.ts` and `cue-overlay.browser.spec.ts` (the two new required fields).

### Step 5

- **Queue:** `CuePlaybackService` (`services/cue-playback.service.ts`), provided per
  `GameComponent`; every game message goes through `cuePlayback.push`, which calls the old
  `processGameMessage`. Policy is in `agent-docs/VISUAL-CUES.md` → *Client playback queue*.
- **Player contract for step 6:** replace the `CUE_PLAYER` token (`providedIn: 'root'`, default
  `NOOP_CUE_PLAYER`) with the effect registry. `canPlay(cue)` must say whether an effect exists:
  cues it rejects are dropped, and a state with none left is applied synchronously, so
  "no effect registered" stays identical to before. `play(cue, { signal, speed })` is called one
  cue at a time and must resolve when done; honor `signal` (skip, time limit, flush) and scale
  durations by `speed`. A rejected promise is logged and the next cue plays.
- **Grouping:** cues play strictly one after another. If step 7 wants simultaneous effects (a
  combat step's damage), group inside the player (e.g. start several and resolve together) or
  change the queue to hand a whole message's cues over at once.
- **Limits:** `MAX_CUE_PLAYBACK_MS` = 3000 per message at speed 1, divided by `speed`. Step 9's
  "fast" can set `cuePlayback.speed`; "off" can install a player whose `canPlay` is always false
  (or add an `enabled` flag to the service).
- **Skip control:** already visible — a full-screen transparent button (`.cue-skip-catcher`,
  z-index 900, under the game-over overlays) with a "Click to skip" hint, shown while
  `playing()`; any key outside text inputs also skips. It also stops clicks on the stale board,
  where hand indices could point at the wrong card. Step 6's canvas should sit below it
  (or it needs `pointer-events: none` anyway). Step 9 only needs to restyle it if wanted.
- **Not flushed by priority:** a state that gives this player priority still waits for its cues
  (otherwise your own spells would never animate); only the decision messages listed in the doc
  flush. `GAME_OVER` waits so a lethal hit still plays.
- **`websocketService.currentGame`** is updated when a state is *applied*, so it lags by at most
  one playback; `ACTIVE_GAME_CHANGED` drops the queue first so an old-frame state can't overwrite
  the new frame's `currentGame`.
- **Tests:** new `cue-playback.service.spec.ts` (immediate apply without playable cues, default
  no-op player, ordering, queued states, decision flush and each decision type, time limit,
  speed, skip, active-game switch, failing cue) and `game-cue-playback.spec.ts` (component:
  applied at once with no effects; held while a fake player plays, key press skips).
  `targeting-choice.service.spec.ts:415` changed only to add `revealableMissionIds: []` to a
  `CommanderView` mock — the pre-existing type error from step 1's Findings broke every
  `ng test --include` build, so no spec could run without it.

### Step 6

- **Pieces:** `CueElementRegistry` + `appCueAnchor` (where things are), `CueOverlayComponent` (the
  canvas), `CueEffectPlayer` + `CUE_EFFECTS` (which effect plays a cue). `CUE_PLAYER`'s factory now
  returns `CueEffectPlayer`; `NOOP_CUE_PLAYER` is kept for step 9's "off". Details and the effect
  contract are in `agent-docs/VISUAL-CUES.md` → *Effects overlay*.
- **The canvas covers the whole viewport,** not only the battlefield, so step 7's projectiles can
  start at a stack entry or end at a player badge in the side panel. It draws in viewport
  coordinates; rects need no conversion.
- **Step 7 registers its effects by changing the `CUE_EFFECTS` factory** (per-card table first in
  step 8, then the defaults). Today the list is empty unless `localStorage.cueDebugEffects = '1'`
  turns on the debug flash, so real games are unchanged until step 7. Decide in step 7 whether the
  debug flash stays behind the flag or goes.
- **Effects that need no target** (`SPELL_RESOLVED`, `SPELL_COUNTERED`) must say so in `matches`;
  the debug flash only matches cues whose target is on screen. `canPlay` runs when the message
  arrives and again (through `play`) when it is its turn, so a target that vanished in between is
  skipped.
- **Simultaneous effects:** the overlay already runs any number of `animate` calls on one loop, so
  step 7 can group a combat step's damage inside one effect's `play` by awaiting several
  `animate` promises together.
- **Sources:** `rects.source` finds `sourceCardId` on the battlefield (permanents register their
  card id), else on the stack, else null — step 7 needs a fallback origin for null.
- **Not anchored yet:** graveyards, exile, hand and command zone have no anchors; add
  `appCueAnchor` (and a kind) if a later step needs them.
- **Browser specs** need Playwright's Chromium; this machine didn't have it, so I ran
  `npx playwright install chromium` (user cache, not the repo).
- **Tests:** new `cue-overlay.browser.spec.ts` (real `GameComponent`: the flash lands on the target
  and not its neighbours on desktop, on a zoomed-out crowded board, in the phone layout and at
  `devicePixelRatio` 2; player targets; the canvas clears and the loop stops afterwards; no
  effect or an off-screen target applies at once) and `cue-effect-player.service.spec.ts`
  (registry lookups, effect selection, speed scaling, attach/detach, default effects). No existing
  test had to change; `cue-playback.service.spec.ts`, `game-cue-playback.spec.ts` and
  `subgame.browser.spec.ts` still pass.

### Step 7

- **Pieces:** `default-effects.ts` (one `CueEffect` per cue family, `DEFAULT_CUE_EFFECTS`),
  `cue-palette.ts` (looks by source color), `cue-particles.ts` (stateless seeded particles), all in
  `components/game/cue-overlay/`. `CUE_EFFECTS` now returns the defaults, behind the debug flash
  when `localStorage.cueDebugEffects = '1'`. I kept the flash: it's a cheap way to check
  positioning. The full table is in `agent-docs/VISUAL-CUES.md` → *Default effects*.
- **Step 8 adds its table in front:** `CUE_EFFECTS`' factory should become
  `[...SHOWPIECE_EFFECTS, ...DEFAULT_CUE_EFFECTS]`, with each showpiece matching on
  `cue.sourceName`. Remember that face-down sources have no name. The building blocks
  (`fly`, `ring`, `wash`, `drawFloatingText`, `burst` / `swirl` / `drawParticles`,
  `sourcePalette`) are plain functions. Export the private ones from `default-effects.ts` when
  step 8 needs them.
- **Grouping is in the queue:** `cueGroups` (`cue-playback.service.ts`) plays consecutive cues of
  one kind (damage, leave, life, counters) together and waits for the whole group. A showpiece
  that wants the whole screen (Lightning Bolt's shake) will overlap the other cues in its group.
  A Bolt is one damage cue, so this only matters for multi-target spells.
- **Light board:** the board is light beige, so additive (`lighter`) blending washed glows out to
  white. Everything uses normal blending, and bright looks rely on saturated colors and washes.
  Keep that in mind for step 8's flash and step 10's Lottie files.
- **Budget:** each default effect is ≤ 700 ms. A Bolt that kills takes about 1.4 s
  (resolve 350 + damage 450 + died 600, no projectile because a resolved spell's source isn't on
  screen). Step 9's "fast" can just raise `cuePlayback.speed`.
- **Fallback origin:** cues carry no caster/controller id, so a targetless cast and a resolve or
  counter whose stack entry isn't showing play in the middle of the window, not at the caster's
  badge. Add a controller id to the cue if a later step wants that.
- **Tests:** new `default-effects.spec.ts` (jsdom, recording 2D context: an effect for every type,
  spell cues without anything on screen, effect choice, ≤ 700 ms each, projectile only from a
  visible, distinct source, impact drawn around the target with `-3`, life and counter labels, no
  source name drawn, middle-of-window fallback, stops drawing when the target goes, looks by color
  and counter type, counter labels). Six new cases in `cue-overlay.browser.spec.ts` under *Default
  effects in the browser* (registered by default and hold the state, warm red damage that leaves
  other cards alone, dark violet black destroy, both creatures of a combat trade at once, a
  targetless cast in the middle, a whole Bolt applied within the budget). Two new cases in
  `cue-playback.service.spec.ts` (group playback, `cueGroups`).
  Changed: `cue-effect-player.service.spec.ts` (the default list is now the defaults, not empty,
  and the debug flag puts the flash in front of them); `cue-playback.service.spec.ts` "time limit"
  and "keeps going when a cue fails" (their second cue changed from `EXILED` to
  `DAMAGE_TO_PERMANENT`, because `DESTROYED, EXILED` now play together and the tests are about
  sequential cues); `cue-overlay.browser.spec.ts` helpers (`mount` can use the app's own effects,
  and `stateWith` takes several cues). `game-cue-playback.spec.ts` and `subgame.browser.spec.ts`
  pass unchanged.

### Step 8

Done together with step 10, so Doom Blade's cloud and skull are Lottie from the start, with
canvas fallbacks (dark smoke, a plain skull) for when the animations haven't loaded.

- **Pieces:** `showpiece-effects.ts` (`SHOWPIECES` by card name, `SHOWPIECE_EFFECTS` wraps each to
  match only its card's `sourceName`), registered in front of the defaults in `CUE_EFFECTS`. Full
  description in `agent-docs/VISUAL-CUES.md` → *Showpiece effects*.
- **Shake:** new `CueEffectContext.shake(magnitudePx, durationMs)`, implemented by the overlay on
  its `shakeTarget` input (`.game-layout`, via `#gameLayout`). The overlay itself is not inside
  the shaken element, so it stays put while the cards it measures move. `CueSurface.shake` is
  optional; the player scales it by speed and cancels it with playback. Step 9's reduced motion
  should turn shaking off (and probably the Bolt's full-window flash).
- **Timings:** Bolt 650 ms (within the 700 ms default budget), Doom Blade 1300 ms; with the
  spell's 350 ms resolve cue both fit `MAX_CUE_PLAYBACK_MS`. A lethal Bolt plays
  resolve → bolt → the default `DIED` (about 1.6 s).
- **Exported building blocks:** `default-effects.ts` now exports `Anchor`, `anchorOf`,
  `fallbackAnchor`, `randomFor`, `easeOut`, `on`, `wash`, `ring`, `impactParticles`,
  `coverParticles`, `spawn`, `isDamage` and `LOSS_COLOR` for step 11's showpieces.
- **Real game check:** played games against the AI on a separate backend and dev server (scratch
  database, ports 8091/4310), driven by Playwright through the real `GameComponent` (cast, pay with
  lands, click the target card). Lightning Bolt on the AI's Hill Giant: the bolt cracks down from
  the top of the window, a fiery starburst with `-3`, then the `DIED` wisps. Doom Blade on the
  AI's Goblin Raider: the cloud gathers above it (it was in the top row, so the cloud overlaps
  the AI's lands), the skull forms with glowing eyes and drops, the card goes dark under the
  violet ring. Other cards kept the defaults.
- **Showpiece cards:** Lightning Bolt, Doom Blade.
- **Tests:** new `showpiece-effects.spec.ts` (jsdom: Bolt and Doom Blade cues get their
  showpieces; other cards, their other cues and face-down sources get the defaults; target
  needed; budget; the bolt starts above the window and ends on the target; one shake at the
  strike / landing; damage label without the name; deterministic flicker; the skull's path from
  the cloud to the target; the cloud stays on screen for top-row targets; nothing drawn when
  the target goes). Two new cases in `cue-overlay.browser.spec.ts` (*Showpiece effects in the
  browser*: Bolt's ink above the target and the board shake, Doom Blade's dark cloud above the
  target then dark smoke on it). A new case in `cue-effect-player.service.spec.ts` (shakes are
  passed on, scaled by speed; an overlay without `shake` is fine).
  Changed: `cue-effect-player.service.spec.ts` (the registered list is now showpieces + defaults,
  with the debug flash in front of both); `default-effects.spec.ts` (its two fake contexts need
  the new `shake`); `cue-overlay.browser.spec.ts` — the `damage()` helper's source is now Shock and
  the black-destroy test uses Murder, because Lightning Bolt and Doom Blade now get showpieces
  and those tests are about the defaults (Bolt's window flash would also ink the neighbour card),
  and *a whole Lightning Bolt* now sends a Lightning Bolt damage cue so it times the showpiece.

### Step 10

- **Library:** `lottie-web` pinned to `5.13.0` (exact), imported as its ES module light canvas
  build (`lottie-web/build/player/esm/lottie_light_canvas.min.js`, typed by
  `lottie-light-canvas.d.ts`) with a dynamic `import()`. The CommonJS build triggered Angular's
  "not ESM" warning. `package-lock.json` changes only by the lottie entries.
- **Bundle:** before, initial 913.49 kB (main 905.43 kB); after, initial 919.72 kB (main
  911.22 kB + a 449 B chunk of esbuild's object-spread helpers, now shared with the lazy chunk).
  lottie-web is a lazy chunk (204 kB raw, 47.8 kB transfer). The ~6 kB of initial growth is the new
  effect code itself (showpieces, loader, Lottie layering), not the library.
- **Animations:** generated, not downloaded: `tools/cue-animations/generate.mjs` + `lottie.mjs` (a
  small bodymovin builder) write `public/cue-animations/*.json` (12 files, ~100 kB, fetched once
  when a game opens). `public/cue-animations/README.md` has the origin and license note. Details,
  palette slots and the "always have a fallback" rule are in `agent-docs/VISUAL-CUES.md` →
  *Authored animations*. To check a new animation's look, render frames with lottie in
  Chromium (Playwright) on the board's beige and on a dark card. Hand-drawn vector work stays
  clean, not illustrated; an artist's After Effects export would drop into the same folder as
  long as it uses the slot names (or fixed colors) and no expressions.
- **Defaults upgraded:** every default effect now has an authored centrepiece (table in
  `agent-docs/VISUAL-CUES.md` → *Default effects*), with the step 7 canvas drawing as fallback.
- **Tests:** new `cue-animations.spec.ts` (jsdom: recoloring, nothing drawn before loading, a
  failed load stays on the fallback and retries, a single load at a time). Three new cases in
  `cue-overlay.browser.spec.ts` (*Authored animations in the browser*: every file is served and
  uses only known slots; lottie loads and draws an animation in the look's colors; the defaults
  draw on the overlay with it). `mount` in that spec now waits for the animations when it uses
  the app's own effects, so pixel tests don't depend on load timing. The existing pixel tests
  pass unchanged with the Lottie layers.

### After step 10: no cast or resolve effects

- **Removed:** `SPELL_CAST_EFFECT` and `SPELL_RESOLVED_EFFECT` (and the `sigil` animation only the
  cast used). Every cast and every resolution flashed, so casting a creature played two effects
  without anything happening to the board. Those cues now have no default effect and apply at
  once; the engine still emits them, so a showpiece can still match one. `SPELL_COUNTERED` keeps
  its effect.
- **Budget:** a lethal Bolt is now bolt → `DIED` (about 1.25 s).
- **Tests changed:** `default-effects.spec.ts` (no effect for cast/resolve; the
  middle-of-window case uses a countered spell, recorded by its cross), `showpiece-effects.spec.ts`
  (Bolt's and Doom Blade's cast/resolve play nothing; the budget is the showpiece alone),
  `cue-overlay.browser.spec.ts` (the targetless-cast case now checks a creature spell's cast and
  resolve apply at once).

### After step 10: effects on the card itself

- **Pieces:** `cue-card-motion.ts` (element animations with held frames, the SVG burn filter,
  recoil keyframes, shatter copies), `CueRects.targetElement`, `CueEffectContext.animateElement`
  (scaled by speed, stopped with playback), `CueElementRegistry.element/targetElement`, and the
  anchor directive releasing held frames on an id change. Details in `agent-docs/VISUAL-CUES.md`
  → *Card motion*.
- **Looks:** damage recoils the target and flashes it red; destroy jolts and burns the card
  away (rim in the source look's accent); `DIED` greys, sinks and crumbles; sacrifice sinks and
  burns half-grey; exile bleaches white, rises and burns out in gold; bounce spins down into the
  vortex; a countered spell's stack entry shatters. Bolt shakes its target; Doom Blade burns it
  in violet after the skull lands.
- **Held frames:** a leaving card's last (invisible) frame is held until the state removes it,
  and stopping playback jumps to it rather than cancelling, so cards never pop back between
  their effect and the new state (the permanent-leave animation then runs on an invisible card).
- **Wash timing:** leave washes end at 33% (Doom Blade's at 40% of its hit), and bounce has none:
  a wash over a burning or spinning card showed as a ghost rectangle.
- **Checking the look:** in a throwaway browser spec, set `cuePlayback.speed` to 0.1 and capture
  with `cdp().send('Page.captureScreenshot', { clip: { …, scale: 4 } })` written by
  `commands.writeFile` — vitest's `page.screenshot` comes out at the runner's 32% iframe scale.
- **Tests:** new `cue-card-motion.spec.ts`; new cases in `default-effects.spec.ts` (*on the card
  itself*), `showpiece-effects.spec.ts` (Bolt's shake, Doom Blade's burn),
  `cue-effect-player.service.spec.ts` (target element, element animations scaled by speed) and
  `cue-overlay.browser.spec.ts` (*Default effects on the card in the browser*). Spec helpers
  building `CueRects` / `CueEffectContext` gained `targetElement` / `animateElement`.

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
