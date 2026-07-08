# Pending-Interaction Queue Refactor — Working Notes

Goal: collapse the ad-hoc player-input ("pending choice") subsystem into a single unified
decision-queue abstraction (`PendingInteraction` + one queue on `GameData` + an
`InteractionHandlerRegistry` in the engine). Pure refactor — no behavior change, including
game-log text, message ordering, and the relative servicing order of simultaneous choices.

## Status

### ✅ Stage 1 — DONE: 20 trigger-target deques collapsed into one queue

- New `model/PendingInteraction.java` (sealed interface, domain module).
  `PermanentChoiceContext` is its first member (`extends PendingInteraction`), so all
  ~40 `PermanentChoiceContext.*` records are now `PendingInteraction`s.
- `GameData` lost the 20 parallel `Deque<PermanentChoiceContext.X>` fields
  (`pendingDeathTriggerTargets`, `pendingAttackTriggerTargets`, `pendingUpkeepPlayerTargets`, …)
  and gained a single `Deque<PendingInteraction> pendingInteractions` plus type-filtered
  helpers: `queueInteraction`, `queueInteractionFirst`, `hasPendingInteraction(Class)`,
  `peekPendingInteraction(Class)`, `pollPendingInteraction(Class)`, `clearPendingInteractions(Class)`.
- **Order preservation argument**: every legacy deque held exactly one record type, every
  producer appended (`add`), and every consumer drained only its own kind from the front
  (`peekFirst`/`removeFirst`). A type-filtered scan of the unified queue returns exactly the
  element the per-kind deque head would have returned, so per-kind FIFO order — and the
  cross-kind order dictated by the call-site chains below — is byte-for-byte unchanged.
  The one `addFirst` user (`ETBTokenMultiTargetTrigger` slot-by-slot re-queue in
  `ETBTokenTargetService` / `PermanentChoiceTriggerHandlerService`) maps to
  `queueInteractionFirst`, which keeps the updated record first-of-its-type.
- `simulationCopy` now copies the single queue (records are immutable, shallow copy as before).
- agent-docs updated (`TRIGGER_SLOT_TARGETING.md` and the deque name references across
  `ACTIVATED_ABILITY_GUIDE.md`, `EFFECTS_INDEX.md`, `CARD_PATTERNS_*`, `ORACLE_TEXT_EFFECT_MAP.md`).

### Cross-kind servicing order catalog (rules-relevant — do not change)

The order in which multiple simultaneous pending kinds get serviced is encoded at call
sites, NOT in the queue. Preserve these chains when migrating further:

- `AutoPassService.resolveAutoPass` (each step gated on `!interaction.isAwaitingInput()`):
  SpellTargetTriggerAnyTarget → SpellGraveyardTargetTrigger → DiscardTriggerAnyTarget →
  AttackTriggerTarget → DeathTriggerTarget → ExploreTriggerTarget → LifeGainTriggerAnyTarget →
  EntersFromGraveyardTriggerTarget → SagaChapterTarget → EndStepTriggerTarget.
- `StackResolutionService` (post-resolution, ~lines 100–143): DiscardTriggerAnyTarget →
  DeathTriggerTarget → ExploreTriggerTarget → LifeGainTriggerAnyTarget →
  EntersFromGraveyardTriggerTarget → SagaChapterTarget → `processNextMayAbility`;
  saga resolution (~626–641): SagaChapterTarget → SagaChapterGraveyardTarget.
- `StepTriggerService` upkeep chain: UpkeepMultiPlayerTargetTrigger → UpkeepPlayerTargetTrigger
  → UpkeepCopyTriggerTarget → CapriciousEfreetOwnTarget → `processNextMayAbility`.
  End step: EndStepTriggerTarget → `processNextMayAbility`.
  Beginning of combat: BeginningOfCombatTriggerTarget → `processNextMayAbility`.
- `BattlefieldEntryService` (~679–689): ETBSpellTargetTrigger → ETBTokenTargetTrigger →
  ETBTokenMultiTargetTrigger.
- `PermanentChoiceTriggerHandlerService` chains the same continuations after each answered
  choice (per-kind `processNext*` then fall through to the next kind / may abilities).

### ✅ Stage 2 — DONE: card-specific single-value pending fields → interaction records

`GameData` no longer carries any card-named fields. Migrated onto the unified queue:

- `pendingSphinxAmbassadorChoice` → queued `PendingSphinxAmbassadorChoice` (now `implements PendingInteraction`).
  Set in `SphinxAmbassadorEffectHandler`, updated (poll + re-queue with `selectedCard`) in
  `LibraryChoiceHandlerService`, read in `ChoiceHandlerService`/`MayMiscHandlerService`, cleared on completion.
- `pendingCapriciousEfreetState` → queued `PendingCapriciousEfreetState` (now `implements PendingInteraction`).
  The `MultiPermanentChoiceHandlerService` dispatch chain checks `hasPendingInteraction(...)` at the same
  position in the if/else chain the null-check occupied.
- `pendingKarnScionControllerId` → new `PendingKarnScionRevealChoice(controllerId)` record.
- `pendingKarnScionReturnFromExile` (boolean) → new `PendingKarnScionExileReturn()` record
  (presence in queue = the old `true`).
- `pendingKarnRestartCards` + `karnRestartControllerId` → new `PendingKarnRestart(cards, controllerId)`,
  consumed by `MulliganService.continueStartGame`.
- `knowledgePoolSourcePermanentId` → new `PendingKnowledgePoolCast(sourcePermanentId)`,
  set by `KnowledgePoolExileAndCastEffectHandler`, polled by `ExileSupport.handleKnowledgePoolCastChoice`,
  peeked by `ReconnectionService` for prompt replay, cleared by Karn restart.

Deferred to stage 4: removing `KNOWLEDGE_POOL_CAST_CHOICE` / `MIRROR_OF_FATE_CHOICE` from
`AwaitingInput` and the `InteractionContext.KnowledgePoolCastChoice` / `MirrorOfFateChoice`
records — they are part of the active-interaction (`InteractionState`) machinery, not `GameData`
fields, and fall together with the rest of the enum dispatch. Mirror of Fate keeps no auxiliary
`GameData` state at all, so stage 2 required no change for it.

### 🔶 Stage 3 — IN PROGRESS: InteractionHandlerRegistry (engine)

Scaffolding is in place and the first kind (X value choice) is migrated end to end.

**The scaffolding** (mirrors `EffectHandlerRegistry`'s `SmartInitializingSingleton` wiring in
`GameEngineConfig`, so handler beans can never form Spring cycles with dispatching services):

- `engine service/interaction/`: `InteractionHandler<T>` (handledType, legacyInputType,
  answerType, decidingPlayerId, prompt, handleAnswer), `InteractionAnswer` (sealed; one record
  per wire-payload shape, grown per migrated kind), `InteractionHandlerRegistry`
  (begin / dispatchAnswer / replayPrompt / activeDecidingPlayerId; owns the mind-control
  recipient redirect).
- `InteractionState.activeInteraction` — the active registry-managed interaction.
  `beginInteraction(record, legacyEnum)` sets BOTH the record and the legacy `AwaitingInput`
  value, so every `isAwaitingInput(...)` check (incl. `EffectResolutionService`'s re-run
  check) and existing test assertions keep working until the stage-4 enum teardown. The
  legacy `InteractionContext` is NOT set for migrated kinds — its per-kind record and all
  switch cases get deleted with each migration.
- Hook points, all falling back to the legacy path when no registry handler matches:
  `GameService` answer entry points (`dispatchAnswer`), `GameService.resolveActingPlayer`
  (mind-control decider via `activeDecidingPlayerId`), `ReconnectionService.resendAwaitingInput`
  (`replayPrompt` at the top), `GameData.copyInteractionInto` (copies the active record).
- AI side (`ai/interaction/`): `AiInteractionStrategy<T>` + static `AiInteractionStrategies`
  lookup + `AiInteractionContext`; `AiChoiceHandler.handleActiveInteraction` dispatches by the
  active record's class. The AI's outer dispatch stays keyed on wire message type (that's the
  protocol, unchanged); `GameSimulator.getInteractionPlayer` reads the decider off the active
  record (grow this per kind, or inject the registry once construction plumbing is touched).

**Migrated kinds so far:**

- `PendingInteraction.XValueChoice` (X_VALUE_CHOICE). Removed with it:
  `InteractionContext.XValueChoice` + all switch cases, `InteractionState.beginXValueChoice`
  / `xValueChoiceContext`, `PlayerInputService.beginXValueChoice`, `XValueChoiceHandlerService`
  (the `GameService` entry throws the same "Not awaiting X value choice" when dispatch misses).
- `PendingInteraction.Scry` (SCRY). `ScryInteractionHandler` owns the prompt (text derived
  from card count, identical to both old begin sites and the reconnect replay) and the
  answer (ported verbatim from `LibraryChoiceHandlerService.handleScryCompleted`, now
  deleted). Removed: `InteractionContext.Scry`, `InteractionState.beginScry/clearScry/
  scryContext`, the scry fields of `LibraryViewState`, and every switch case. AI: base
  heuristic became `ScryAiStrategy`; **Hard AI keeps its board-aware override** via the
  protected `AiDecisionEngine.handleScry` seam (reads the active record, falls back to
  `choiceHandler.handleScry` → strategy) — kinds with difficulty-specific behavior keep the
  overridable-method seam rather than forcing everything into the static strategy table.
  Tests read the record via the new typed accessor
  `interaction.activeInteraction(PendingInteraction.Scry.class)`.
- `PendingInteraction.HandTopBottomChoice` (HAND_TOP_BOTTOM_CHOICE).
  `HandTopBottomChoiceInteractionHandler`; prompt derived from card count (matches both
  effect-handler begin sites and the old replay). Removed the legacy context/state/case
  set and `LibraryChoiceHandlerService.handleHandTopBottomChosen`. AI:
  `HandTopBottomChoiceAiStrategy` (no difficulty overrides existed).
- `PendingInteraction.LibraryReorder` (LIBRARY_REORDER). The record carries the exact
  begin-time `prompt` string because the six begin sites word it differently (top vs
  "back on top" vs bottom). **One deliberate replay-text correction**: the legacy reconnect
  replay re-derived the prompt from `toBottom` and therefore showed "back on top" for the
  Mirror of Fate exile reorder whose original prompt said "on top" — replay now re-sends
  the original prompt verbatim. Answer handler ported from
  `LibraryChoiceHandlerService.handleLibraryCardsReordered` (deleted), including the
  Warp World `pendingLibraryBottomReorders` continuation; `WarpWorldService`'s own begin
  site now goes through the registry too. `PlayerInputService.beginLibraryReorderFromExile`
  deleted (ExileSupport begins directly). `LibraryViewState` now holds only the library
  reveal fields. AI: `LibraryReorderAiStrategy` (no difficulty overrides existed).
  Unit tests that construct effect handlers directly use the new
  `InteractionRegistryTestSupport.registryFor(...)` helper (real prompt/answer handlers over
  mocked continuation services).
- `PendingInteraction.MayAbilityChoice` (MAY_ABILITY_CHOICE). The record carries
  `(playerId, description, manaCost)` mirroring the head of `GameData.pendingMayAbilities`;
  `canPay` is computed at prompt time from the pool (as both the legacy begin and reconnect
  replay did). The single begin site stays `PlayerInputService.processNextMayAbility` (its
  ~60 callers unchanged) — its body now builds the record from the queue head and calls
  `registry.begin`. **`MayAbilityHandlerService` keeps the entire 900-line answer dispatch**
  (cast-from-zone, penalty choices, copy effects, targeted triggers, resolution-time CR 603.5
  flow, …); `MayAbilityChoiceInteractionHandler.handleAnswer` just delegates to it, and its
  entry validation reads the active record instead of the deleted context (the redundant
  `clearMayAbilityChoice` call went away — `clearAwaitingInput` clears the record).
  Removed: `InteractionContext.MayAbilityChoice` + all switch cases,
  `InteractionState.beginMayAbilityChoice/clearMayAbilityChoice/awaitingMayAbilityPlayerId/
  mayAbilityChoiceContext` (incl. the blank-description fabricated-context fallback and
  ReconnectionService's re-derivation of it — unreachable now that the record always carries
  the begin-time description). AI: `MayAbilityChoiceAiStrategy` (always accept); **Hard AI
  keeps its SpellEvaluator-based override** via the protected
  `AiDecisionEngine.handleMayAbilityChoice` seam, reading the active record.
  `GameSimulator`'s enum-keyed MAY_ABILITY_CHOICE cases still work (answers go through the
  `GameService` entry, which dispatches via the registry); only its decider lookup moved to
  the record. Test side: 106 files' `awaitingMayAbilityPlayerId()` assertions rewritten to
  the typed record accessor; `PlayerInputServiceTest` constructs the service with a real
  registry + `MayAbilityChoiceInteractionHandler` so its `processNextMayAbility`
  message-send tests still exercise the real prompt path.
- `PendingInteraction.KnowledgePoolCastChoice` + `PendingInteraction.MirrorOfFateChoice`
  (KNOWLEDGE_POOL_CAST_CHOICE / MIRROR_OF_FATE_CHOICE), answered via the new shared
  `InteractionAnswer.CardsChosen` shape. `GameService.handleMultipleCardsChosen` now
  tries `dispatchAnswer` first and falls through to the legacy LIBRARY_REVEAL /
  MULTI_ZONE_EXILE / multi-graveyard chain (those kinds migrate later onto the same
  answer record — the wire entry point is genuinely shared, and the active interaction
  disambiguates exactly as the legacy enum checks did). The records carry
  `validCardIds` as an ordered List — the legacy contexts held a `HashSet`, so the
  reconnect replay used to send the IDs in scrambled order relative to the card views;
  replay now re-sends the begin-time order (same replay-fidelity correction precedent
  as the Mirror of Fate reorder prompt). Card views are re-derived at prompt time
  (KP via the queued `PendingKnowledgePoolCast` → pool; MoF from the player's exile
  zone), identical to both the legacy begin sites and the legacy replay. Answer logic
  stays in `ExileSupport` (handlers delegate); KP keeps its legacy no-decider-check
  behavior, MoF keeps its full validation with identical error texts. Removed:
  both `InteractionContext` records + all cases, the six `InteractionState` methods
  (their piggybacking on the `multiSelection` multi-graveyard / multi-zone-exile
  sub-states ends — those sub-states remain for the real MULTI_* kinds), and
  `PlayerInputService.sendKnowledgePoolCastChoice` / `sendMirrorOfFateChoice`.
  AI: `KnowledgePoolCastChoiceAiStrategy` (first card) + `MirrorOfFateChoiceAiStrategy`
  (keep max) replace the enum blocks at the top of `AiChoiceHandler.handleMultiCardChoice`.
- `PendingInteraction.MultiZoneExileChoice` (MULTI_ZONE_EXILE_CHOICE, e.g. Memoricide /
  Surgical Extraction), third user of `InteractionAnswer.CardsChosen`. The begin helper
  `PlayerInputService.beginMultiZoneExileChoice(gameData, playerId, matchingCards, target,
  name)` keeps its two callers and now builds the record; the handler's prompt re-derives
  card views by the same hand → graveyard → library scan both begin sites use to build
  `matchingCards` (and that the legacy replay used), so begin and replay are identical.
  Answer stays in `ChoiceHandlerService.handleMultiZoneExileCardsChosen` (reads the active
  record; validation/error texts unchanged). Removed: the `InteractionContext` record +
  cases, the `InteractionState` begin/clear/context trio, and the multi-zone-exile fields
  of `MultiSelectionState` (now holds only multi-permanent + multi-graveyard).
  AI: `MultiZoneExileChoiceAiStrategy` (exile all).
- `PendingInteraction.MultiPermanentChoice` + `PendingInteraction.MultiGraveyardChoice`
  (MULTI_PERMANENT_CHOICE / MULTI_GRAVEYARD_CHOICE). Both records carry the exact begin-time
  `prompt` — **two deliberate replay-fidelity corrections**: the legacy reconnect replay sent
  a generic "Choose permanents." / "Exile up to N cards from graveyards." instead of the
  original prompt (and hash-scrambled ID order); replay now re-sends the begin-time prompt
  and order. `MultiGraveyardChoice` carries `List<Card> cards` (not IDs — same precedent as
  `Scry`/`LibraryReorder`) because the Boneyard Parley pile-separation begin site prompts over
  just-**exiled** cards that no graveyard scan can rebuild; IDs/views derive from the card
  list (`validCardIds()` is a derived accessor), and the legacy replay's graveyard re-scan
  (which silently sent an empty list for pile separation) is gone. The 11 graveyard begin
  sites (`GraveyardTargetingService` ×8, `TriggeredAbilityQueueService` ×2, the pile-separation
  effect handler) now pass their matched `Card` list; `PlayerInputService.beginMultiGraveyardChoice`
  dropped its redundant `cardViews` param (the sites' pairwise id+view building collapsed to
  one list; `GraveyardTargetingService`/`TriggeredAbilityQueueService` lost their
  `CardViewFactory` deps). `beginMultiPermanentChoice` keeps its ~20 callers unchanged.
  Answers: new `InteractionAnswer.PermanentsChosen` for the `handleMultiplePermanentsChosen`
  entry (sole consumer — dispatch miss now throws the same "Not awaiting multi-permanent
  choice"; the `PermanentChoiceHandlerService` pass-through delegate was deleted);
  multi-graveyard joins the shared `CardsChosen` dispatch, and the legacy tail of
  `GameService.handleMultipleCardsChosen` shrank to LIBRARY_REVEAL_CHOICE → else the same
  "Not awaiting multi-graveyard choice" error. The big answer services keep their full
  dispatch logic (`MultiPermanentChoiceHandlerService`'s 13-branch pending-operation chain,
  `GraveyardChoiceHandlerService.handleMultipleCardsChosen`'s exact-X/pile-separation flow);
  only their entry validation reads the active record. Removed: both `InteractionContext`
  records + all cases, both `InteractionState` begin/clear/context trios, and
  **`MultiSelectionState` entirely** (these were its last two sub-states; the `multiSelection`
  field/accessor and its deepCopy lines are gone). AI: `MultiPermanentChoiceAiStrategy`
  (opponent's strongest by effective power, fall back to first valid) +
  `MultiGraveyardChoiceAiStrategy` (first maxCount); `AiChoiceHandler.handleMultiPermanentChoice`
  is a thin alias to `handleActiveInteraction`, and MULTI_GRAVEYARD_CHOICE joined the
  registry-managed head check of `handleMultiCardChoice` (its legacy tail block deleted).
  ~20 test files' `multiSelection()` / `multiPermanentChoiceContext()` reads rewritten to the
  typed active-record accessors (scratchpad perl script `fix_multi_tests.pl`).
- `PendingInteraction.ColorChoice` (COLOR_CHOICE) — the whole single-value "choose from a list"
  family (mana color, protection color, keyword / creature-type / permanent-type /
  basic-land-type, card name, text-change word, Abundance land/nonland, Conundrum-Sphinx
  name-and-reveal, …). The specific variant stays in the record's `context` (a `ChoiceContext`,
  a domain type) and drives answer handling, exactly as the legacy `InteractionContext.ColorChoice`
  did. The record also carries the exact begin-time `options` (`List<String>`) and `prompt`, so
  reconnect replay re-sends byte-identical content. `ColorChoiceInteractionHandler.prompt` just
  sends `ChooseFromListMessage(options, prompt)` — the per-begin-site log lines stay at the begin
  sites (there are ~15 distinct ones), so replay does not re-log, matching the legacy
  `ReconnectionService.resendFromContext` (which never logged). Answered via the new single-String
  `InteractionAnswer.ListChoiceMade`; `GameService.handleListChoice` (COLOR_CHOICE's sole entry
  point) now routes through `dispatchAnswer` and throws the same "Not awaiting color choice" on a
  miss. The entire variant dispatch stays in `ChoiceHandlerService.handleListChoice` (now reading
  the active record; its ~17 redundant `clearColorChoice()` calls removed — `clearAwaitingInput()`
  clears the record; its three internal re-begins — flashback-mana continuation, text-change
  second step, each-player next-player — go through the registry too). Begin sites converted:
  `PlayerInputService`'s 11 `begin*` helpers (signatures unchanged), the five
  `ActivatedAbilityExecutionService` mana-color sites, `AwardAnyColorManaEffectHandler` /
  `AwardAnyColorManaWithInstantSorceryCopyEffectHandler` / `AddManaPerAttackingCreatureEffectHandler`
  / `ChangeColorTextEffectHandler` / `EachPlayerNameCardRevealTopEffectHandler`, and
  `MayMiscHandlerService` (Abundance) — each dropped its now-unused `SessionManager` /
  `ChooseFromListMessage` in favor of the registry. **Two deliberate replay-fidelity corrections**
  (same precedent as the Mirror-of-Fate / multi-permanent prompt corrections): the legacy replay
  re-derived options+prompt from the context and *diverged* from the begin-time message — (1) every
  `ManaColorChoice`, `ProtectionColorChoice`, `MassProtectionColorChoice`, `PermanentTypeChoice`,
  `SphinxAmbassadorNameChoice`, `AttackManaSplitChoice`, and `TextChangeToWord` fell through to a
  generic `["WHITE","BLUE","BLACK","RED","GREEN"]` / "Choose a color." (e.g. a mana choice replayed
  as "Choose a color." instead of "Choose a color of mana to add.", protection-with-artifacts
  dropped its `ARTIFACT` option, a permanent-type choice replayed the wrong five options entirely);
  (2) replay now re-sends the exact begin-time options+prompt carried on the record. A related
  consequence: the direct (non-helper) begin sites previously sent to the deciding player *without*
  the mind-control recipient redirect; routing them through `registry.begin` applies
  `resolveMessageRecipient` uniformly (rules-correct, and consistent with every other migrated kind).
  AI: `ColorChoiceAiStrategy` ports `AiChoiceHandler.handleColorChoice` verbatim (inlining
  `AiUtils.getOpponentId`, which is package-private to `…ai`); `handleColorChoice` is now a thin
  alias to `handleActiveInteraction`; **Hard AI keeps its creature-type / basic-land-type override**
  via the protected `AiDecisionEngine.handleListChoice` seam, reading the active record.
  `GameSimulator` reads the record in both COLOR_CHOICE action-gen and resolution branches and gains
  a `getInteractionPlayer` decider case. Removed: `InteractionContext.ColorChoice` + all switch
  cases (GameService, ReconnectionService ×2, GameSimulator, GameData.copyInteractionInto),
  `InteractionState.beginColorChoice/clearColorChoice/colorChoiceContext/colorChoiceContextView` +
  the `ChoiceState colorChoice` sub-state field, and the now-empty `interaction/ChoiceState` class.
  ~25 test files' `colorChoice()` / `colorChoiceContext()` reads rewritten to the typed accessor
  (scratchpad `fix_color_tests.pl`); the five color effect-handler tests now verify
  `interactionHandlerRegistry.begin(…, ColorChoice)` instead of the send.
- `PendingInteraction.RevealedHandChoice` (REVEALED_HAND_CHOICE) — Duress-style "choose a card
  from the target's revealed hand" incl. the multi-pick discard/exile/top-of-library countdown.
  The record carries `(choosingPlayerId, targetPlayerId, validIndices, remainingCount,
  discardMode, exileMode, chosenCards, sourcePermanentId, prompt)`; the mutable-state countdown
  (`addRevealedHandChosenCard` / `decrementRevealedHandChoiceRemainingCount`) became "each
  answered pick begins a fresh record with the decremented count and accumulated cards" — the
  legacy mid-flow re-begin did NOT carry `sourcePermanentId` across picks, and the record
  re-begin passes null to preserve that byte-for-byte. Card views are re-derived from the
  target's current full hand at prompt time (identical to legacy begin AND replay, since any
  hand change begins a fresh record). Answered via the new `InteractionAnswer.CardIndexChosen`;
  `GameService.handleCardChosen` dispatches through the registry FIRST and keeps the full legacy
  fallback (`CardChoiceHandlerService.handleCardChosen` still serves CARD_CHOICE /
  TARGETED_CARD_CHOICE / DISCARD_CHOICE / EXILE_FROM_HAND / IMPRINT / activated-ability discard
  cost — only its REVEALED_HAND_CHOICE branch was deleted). The answer logic stays in
  `CardChoiceHandlerService.handleRevealedHandCardChosen` (now public; reads the active record;
  log/error texts unchanged; the redundant `clearRevealedHandChoiceProgress` went away). Begin
  sites: `PlayerInputService.beginRevealedHandChoice` deleted — the legacy two-step begin
  (full-state `interaction.beginRevealedHandChoice(...)` + helper re-begin-from-current-state
  + post-hoc `setSourcePermanentId`) collapses to one `registry.begin` at each of the three
  sites (`ChooseCardsFromTargetHandToTopOfLibraryEffectHandler`,
  `PlayerInteractionSupport.resolveHandRevealAndChoose`, the mid-flow re-begin); the uniform
  "Awaiting {} to choose a card from revealed hand" log moved into the handler's prompt
  (multi-zone-exile precedent). **Two deliberate replay-fidelity corrections**: the legacy
  replay always sent "Choose a card to put on top of X's library." — wrong for discard/exile
  modes ("…to discard.", "…to exile.") and for the "Choose another card…" follow-up picks —
  and hash-scrambled the index order; replay now re-sends the begin-time prompt and ordered
  indices. **`RevealedHandChoiceState` is deleted entirely**, along with its
  "backwards-compatibility" `cardChoice` sub-state mirror (set at every begin); its
  piggybacking `discardRemainingCount` — which belongs to the separate DISCARD_CHOICE /
  EXILE_FROM_HAND countdown, not this kind — became a plain int field on `InteractionState`
  (`discardRemainingCount()` getter added; deepCopy + `copyInteractionInto` preserved; the
  lazy `ensureRevealedHandChoice` hack is gone). AI: `RevealedHandChoiceAiStrategy` (highest
  mana value) — no difficulty overrides existed; `GameSimulator` reads the record in the
  REVEALED_HAND_CHOICE action-gen case (split out of the shared `cardChoiceContext()` case,
  which the deleted mirror had been feeding) and the resolve case, plus a
  `getInteractionPlayer` decider case (`choosingPlayerId`). ~23 test files rewritten:
  `revealedHandChoice().remainingCount()/discardMode()/exileMode()` → the typed record
  accessor, `revealedHandChoice().discardRemainingCount()` →
  `interaction.discardRemainingCount()`, and the RH-context `cardChoice().playerId()/
  validIndices()` mirror reads → record accessors (discard-flow `cardChoice()` reads stay
  legacy); `PlayerInputServiceTest`'s begin-helper block moved into the new focused
  `RevealedHandChoiceInteractionHandlerTest`.
- `PendingInteraction.GraveyardChoice` + `PendingInteraction.GraveyardExileCostChoice`
  (GRAVEYARD_CHOICE / ACTIVATED_ABILITY_GRAVEYARD_EXILE_COST_CHOICE), migrated together as
  **two records** because the code separates them cleanly: different answer services
  (`GraveyardChoiceHandlerService.handleGraveyardCardChosen` vs
  `AbilityActivationService.handleActivatedAbilityGraveyardExileCostChosen`), different begin
  shapes (the exile-cost begin was a direct `interaction.beginGraveyardChoice(...,null,null)` +
  `setAwaitingInput` enum-override hack + its own send with no log line — the new handler
  likewise emits no prompt log), and only shared plumbing at the wire entry. Both answered via
  the new `InteractionAnswer.GraveyardCardChosen`; `GameService.handleGraveyardCardChosen` was
  single-family, so it now dispatches through the registry and a miss throws the same
  "Not awaiting graveyard choice" (the per-kind enum branch is gone; GameService lost its
  `GraveyardChoiceHandlerService` dep). `GraveyardChoice` carries all twelve auxiliary fields
  of the deleted `GraveyardChoiceState` (destination, cardPool — non-null drives the message's
  all-graveyards flag —, gainLifeEqualToManaValue, attachToSourcePermanentId, grantColor,
  grantSubtype, exileRemainingCount, gainLifeIfCreature amount+player,
  trackWithSourcePermanentId, the four-part may-ability context) plus the begin-time `prompt`
  and ordered `validIndices`; a nested `Builder` (LibrarySearchParams precedent) mirrors the
  legacy pre-seed setters, so the two-step `prepareGraveyardChoice` + `setGraveyardChoice*` +
  `beginGraveyardChoice`-with-aux-preserving-merge dance collapses to one `registry.begin` at
  all eight begin sites (`GraveyardReturnSupport` ×4 incl. the return-queue continuation,
  `TargetPlayerExilesCardFromGraveyardEffectHandler`, `MayAbilityHandlerService` ×2 may-ability
  targeting, `MayPenaltyChoiceHandlerService`) and the mid-flow exile-countdown re-begin in the
  answer handler; `PlayerInputService.beginGraveyardChoice` deleted (its "Awaiting {} to choose
  a card from graveyard" log moved into the GRAVEYARD_CHOICE handler's prompt). The dead
  `setGraveyardChoiceTrackWithSourcePermanentId` setter (no callers) is gone; the record keeps
  the field for the answer's exile-tracking branch (still always null, as legacy).
  **Replay-fidelity corrections**: the legacy replay sent a generic "Choose a card from the
  graveyard." for BOTH kinds — wrong for every varied begin prompt ("Return a creature card
  from your graveyard to your hand.", "Choose a card to exile from your graveyard.", the
  exile-cost prompt, …) — and hash-scrambled the index order; replay now re-sends the
  begin-time prompt and ordered indices. The exile-cost begin also previously sent to the
  deciding player without the mind-control recipient redirect; `registry.begin` applies it
  uniformly (COLOR_CHOICE precedent). `AbilityActivationService.clearPendingAbilityActivation`
  lost its redundant `clearGraveyardChoice()`. Removed: the `InteractionContext.GraveyardChoice`
  record + all cases, the `InteractionState` begin/clear/prepare/8-setter/context/accessor set
  + the lazy `ensureGraveyardChoice`, and **`GraveyardChoiceState` deleted entirely**.
  AI: `GraveyardChoiceAiStrategy` + `GraveyardExileCostChoiceAiStrategy` (both the legacy
  highest-mana-value heuristic; the choice strategy reads the record's card pool for
  cross-graveyard picks) — no difficulty overrides existed; `AiChoiceHandler.handleGraveyardChoice`
  is a thin alias to `handleActiveInteraction`; `GameSimulator`'s combined resolve case split
  per record type and `getInteractionPlayer` gained both decider cases. Tests: only two
  accessor rewrites were needed (`BeaconOfUnrestTest` cardPool, `EntomberExarchTest`
  validIndices — the ~50 other graveyard tests assert only the enum);
  `ReturnCardFromGraveyardEffectHandlerTest` verifies `registry.begin` (mocked registry added
  to its `@InjectMocks` support); `PlayerInputServiceTest`'s begin-helper block moved into the
  new focused `GraveyardChoiceInteractionHandlersTest` (both kinds).
- The **hand-card choice family** — six records for the six kinds the legacy shared
  `InteractionContext.CardChoice(type, playerId, validIndices, targetId)` served:
  `PendingInteraction.HandCardChoice` (CARD_CHOICE), `TargetedHandCardChoice`
  (TARGETED_CARD_CHOICE, carries `targetId`), `DiscardChoice` (DISCARD_CHOICE),
  `ExileFromHandChoice` (EXILE_FROM_HAND_CHOICE, carries `sourcePermanentId`),
  `ImprintFromHandChoice` (IMPRINT_FROM_HAND_CHOICE, carries `sourcePermanentId`), and
  `DiscardCostChoice` (ACTIVATED_ABILITY_DISCARD_COST_CHOICE). **Per-kind records** because
  the legacy answer dispatch branched into genuinely different handler methods (and each kind
  needs its own legacy enum via `beginInteraction`); CARD/TARGETED share one answer method.
  All six implement the new `PendingInteraction.HandChoice` accessor interface
  (playerId/validIndices/prompt) so generic consumers — the base AI heuristic, the Hard/Medium
  `handleCardChoice` overrides, the `RandomAiDecisionEngine` test engine, and
  `GameSimulator`'s CARD_CHOICE/DISCARD_CHOICE action-gen + resolve cases and decider lookup —
  read the family uniformly. Handlers live in one file
  (`HandCardChoiceInteractionHandlers`, six nested `@Component` classes over a shared base:
  same `ChooseCardFromHandMessage(validIndices, prompt, canDecline)` send; canDecline=true only
  for CARD/TARGETED; per-kind "Awaiting …" log; no log for the discard cost, matching its
  legacy begin). Answered via `InteractionAnswer.CardIndexChosen` (shared with the already
  migrated revealed-hand kind) — with the whole `handleCardChosen` wire entry now
  registry-managed, `GameService.handleCardChosen` collapsed to dispatch-or-throw with the
  legacy "Not awaiting card choice" text, and `CardChoiceHandlerService` lost its enum
  dispatch (the per-kind answer methods are now public, read their active record, keep all
  log/error texts, and keep the legacy invalid-index re-prompt quirks: the discard/exile
  re-prompts still reset to full-hand indices + default prompt via the same helper, imprint
  still re-prompts with "Choose a card from your hand.", and the discard cost still re-sends
  its message without re-beginning). **`discardRemainingCount` (the int this refactor had
  parked on `InteractionState` during the revealed-hand migration) is gone** — the
  discard/exile multi-pick countdown rides on `DiscardChoice.remainingCount()` /
  `ExileFromHandChoice.remainingCount()` with the fresh-record-per-pick pattern; the six
  pre-seed sites (`PlayerInteractionSupport.resolveDiscardCards` / `startNextEachPlayerDiscard`,
  `TargetPlayerExilesFromHandEffectHandler`, `MayPenaltyChoiceHandlerService` ×3,
  `TurnProgressionService` cleanup discard) now pass the count into
  `beginDiscardChoice(gameData, playerId[, indices, prompt], remainingCount)` /
  `beginExileFromHandChoice(gameData, playerId, sourcePermanentId, remainingCount)`, and its
  `InteractionState` deepCopy/`copyInteractionInto` lines are deleted. **`CardChoiceState` is
  deleted entirely** (no other piggybackers — the revealed-hand mirror was already gone), as
  is `InteractionState.beginCardChoice/clearCardChoice/cardChoiceContext` and the
  `InteractionContext.CardChoice` record + all cases;
  `AbilityActivationService.beginDiscardCostChoice` drops its `setAwaitingInput` enum-override
  hack and `clearPendingAbilityActivation` its redundant `clearCardChoice()`.
  **Replay-fidelity corrections**: the legacy replay re-derived prompts ("Choose a card to
  discard." / "Choose a card from your hand.") — wrong for every per-site prompt
  (MayPenalty's "Choose a land card to discard.", "Choose a creature card from your hand to
  put onto the battlefield.", imprint's and exile's prompts, …) — with hash-scrambled index
  order, and **EXILE_FROM_HAND_CHOICE / IMPRINT_FROM_HAND_CHOICE were entirely absent from
  the replay switch** (reconnecting players got no prompt re-send at all); replay now re-sends
  the begin-time prompt and ordered indices for all six kinds. The discard-cost begin and its
  invalid-index re-send also gain the mind-control redirect / ordered indices respectively.
  AI: no strategy-table entries — the family keeps the overridable
  `AiDecisionEngine.handleCardChoice` seam (Hard/Medium discard evaluators), all three
  implementations now reading the active record via `HandChoice`. ~45 test files' mechanical
  rewrites: `cardChoice().playerId()/validIndices()` → `((PendingInteraction.HandChoice)
  activeInteraction())` casts, `interaction.discardRemainingCount()` →
  `activeInteraction(DiscardChoice.class).remainingCount()` (all 15 such files were discard
  flows), begin-helper verify signatures gained the count arg, and
  `PlayerInputServiceTest` registers the six handlers; new focused
  `HandCardChoiceInteractionHandlersTest` covers all six kinds.
- `PendingInteraction.LibraryRevealChoice` (LIBRARY_REVEAL_CHOICE) — Lead the Stampede /
  Genesis Wave battlefield picks, choose-N-to-hand looks, punisher reveals (Sword-Point
  Diplomacy), Gishath random-bottom, Karn Scion picks. The record carries the legacy context's
  nine components plus the begin-time ordered `validCardIds`, `maxCount`, and `prompt`; card
  views derive from `validCardIds` against the held-out `allCards` at prompt time, reproducing
  each begin site's exact message. **A null `prompt` means "no choice message"**: the two Karn
  Scion begin sites never sent one (the client prompts off the game-state broadcast), so the
  handler sends nothing at begin — and, as a replay-fidelity correction, nothing on reconnect
  either (the legacy replay sent them a spurious message). The other **replay corrections**:
  the legacy replay hardcoded the Ajani/Genesis-Wave prompt ("Choose any number of nonland
  permanent cards with mana value 3 or less…") and `maxCount = validCardIds.size()` for every
  variant (wrong prompt for all others; wrong maxCount for the choose-N-to-hand looks) with
  hash-scrambled ID order — replay now re-sends the begin-time prompt/maxCount/order.
  The registry gained a **two-phase begin** (`beginWithoutPrompt` + `promptActive`) for
  `RevealTopCardsOpponentPaysLifeOrToHandEffectHandler`, whose legacy sequence interleaves a
  game-state broadcast (whose payload depends on the awaiting flag) between the state change
  and the choice message — the two-phase API preserves that wire order byte-for-byte (the
  prompt also gains the uniform mind-control redirect, as at every migrated begin). Fifth user
  of `InteractionAnswer.CardsChosen`; with it, `GameService.handleMultipleCardsChosen`'s whole
  legacy population (KP, MoF, multi-zone exile, multi-graveyard, library reveal) is
  registry-managed, so the entry collapsed to dispatch-or-throw — keeping the CURRENT miss
  text "Not awaiting multi-graveyard choice", since that is what a stray message already hit
  (the reveal branch was checked first and a miss fell through to the multi-graveyard error).
  Answer logic stays in `LibraryChoiceHandlerService.handleLibraryRevealChoice` (reads the
  active record; Karn/punisher branches and all log/error texts unchanged; the redundant
  `clearLibraryRevealChoice` went away). Removed: the `InteractionContext.LibraryRevealChoice`
  record (+ its two convenience constructors) and all cases incl.
  `GameData.copyInteractionInto`'s three-variant begin reconstruction, the four
  `InteractionState.beginLibraryRevealChoice*` overloads + clear + context accessor, and
  **`LibraryViewState` deleted entirely** (reveal was its last resident).
  AI: `LibraryRevealChoiceAiStrategy` (punisher → deny nothing, else choose all; identical
  logs); `AiChoiceHandler.handleMultiCardChoice` collapsed to a thin
  `handleActiveInteraction` alias (its whole population is registry-managed);
  `GameSimulator`'s LIBRARY_REVEAL_CHOICE resolve case reads the record (same punisher split)
  and the decider lookup gained the record case. Tests: `LookAtTopXCards…Test` +
  `LibraryChoiceHandlerServiceTest` accessor/begin rewrites, four effect-handler tests'
  manual constructions gained the shared test registry (which now registers the real reveal
  handler), and the new focused `LibraryRevealChoiceInteractionHandlerTest` covers begin
  content, the null-prompt no-send rule, dispatch delegation, and replay gating.

- `PendingInteraction.LibrarySearch` (LIBRARY_SEARCH) — search-style single-card picks from a
  presented library subset: tutors, look-at-top-N picks, Head Games, Sphinx Ambassador, the
  each-player basic-land searches, Sunbird's Invocation cast-without-paying, imprint-from-top.
  The record wraps the immutable `LibrarySearchParams` the begin site built plus the exact
  begin-time `messagePrompt` + `messageCanFailToFind` (several begin sites word the
  `ChooseCardFromLibraryMessage` differently from `params.prompt()`, so both are carried and
  re-sent verbatim on reconnect); card views derive from `params.cards()` at prompt time. The
  multi-pick countdown (`remainingCount`/`accumulatedCards`) and the Sphinx Ambassador
  `PendingSphinxAmbassadorChoice` poll/re-queue stay in
  `LibraryChoiceHandlerService.handleLibraryCardChosen` (now reads the active record; the
  fail-to-find "fail to find" decline path and all log/error texts unchanged), and each mid-flow
  pick begins a fresh record via `registry.begin` (immutable re-begin precedent). Answered via the
  new single-index `InteractionAnswer.LibraryCardChosen` (kept distinct from the graveyard/card
  index shapes, per the per-entry-point convention); `GameService.handleLibraryCardChosen` was
  single-family, so it collapsed to dispatch-or-throw with the same "Not awaiting library search"
  and GameService lost its `LibraryChoiceHandlerService` dep. **Replay-fidelity correction**: the
  legacy reconnect replay derived a blank-prompt fallback ("Search your library for a basic land
  card to put into your hand." vs "...a card..." off `canFailToFind`) that diverged from the
  begin-time message; replay now re-sends the exact begin-time prompt/flag (matching begin — same
  precedent as the prior prompt corrections), and the direct begin sites gain the uniform
  mind-control recipient redirect via `registry.begin`. **`copyInteractionInto` finding**: the
  legacy simulation copy rebuilt the context through `LibrarySearchParams.builder` and silently
  DROPPED `filterCardName`/`attachToPlayerId`/`filterPredicate`/`accumulatedCards`; the
  active-record copy preserves them verbatim — a simulation-only fidelity improvement. Removed:
  `InteractionContext.LibrarySearch` (+ its convenience constructor) and all cases
  (`controlledPlayerMatchesContext`, `ReconnectionService` ×2, `GameSimulator`,
  `GameData.copyInteractionInto`), the `InteractionState.beginLibrarySearch/clearLibrarySearch/
  librarySearchContext` set + the `librarySearch` sub-state field, and **`LibrarySearchState`
  deleted entirely**. `LibrarySearchParams` stays (the record wraps it; the answer handler and
  begin sites build it); the now-unused `SessionManager`/`CardViewFactory` deps were dropped from
  `LibrarySearchSupport`, `LibraryChoiceHandlerService`, `ReconnectionService`, and the six
  reveal/tutor effect handlers. AI: `LibrarySearchAiStrategy` (highest-value nonland, else first —
  ported verbatim; no difficulty overrides existed); `AiChoiceHandler.handleLibrarySearch` is a
  thin `handleActiveInteraction` alias; `GameSimulator`'s LIBRARY_SEARCH resolve case reads the
  record (picks index 0) and `getInteractionPlayer` gained the record case. Tests: ~70 card /
  effect-handler tests' `interaction.librarySearch().X` reads rewritten to
  `activeInteraction(PendingInteraction.LibrarySearch.class).params().X`; `LibraryChoiceHandlerServiceTest`
  and `InteractionRegistryTestSupport` register the real handler; new focused
  `LibrarySearchInteractionHandlerTest` covers begin content, dispatch delegation, and replay gating.
- `PendingInteraction.PermanentChoice` (PERMANENT_CHOICE) — the single-pick battlefield/player
  targeting prompt serving the ~45 `PermanentChoiceContext` operations (trigger-slot targets,
  sacrifices, clone copies, spell retargets, legend rule, aura placement, …). The record carries
  `(playerId, validPermanentIds, validPlayerIds, context, prompt)`: the TWO begin-time ordered ID
  lists exactly as `ChoosePermanentMessage` sent them (`beginPermanentChoice` sends only a
  permanent list; `beginAnyTargetChoice` sends permanents + players — `validPlayerIds` is empty
  for the plain variant), with validation over the derived merged `validIds()` set as legacy.
  **The independent `InteractionState.permanentChoiceContext` field is deliberately KEPT** (only
  the prompt/validation state migrated): its lifecycle spans interactions — the ~60 begin sites
  pre-seed it via `setPermanentChoiceContext(...)` before calling the (signature-unchanged)
  `PlayerInputService.beginPermanentChoice`/`beginAnyTargetChoice` helpers, which snapshot it
  into the record, and `MayCopyHandlerService`'s clone-copy decline clears a pre-seed that never
  reaches any permanent-choice begin (set across the MAY_ABILITY_CHOICE window). Folding it into
  begin arguments would have meant rewiring all ~60 sites for no behavior gain. The answer's
  entry (`PermanentChoiceHandlerService.handlePermanentChosen`) reads the active record, keeps
  its clear-before-invalid-throw sequence and all error texts ("Not your turn to choose",
  "Invalid permanent: X", "No pending permanent choice context"), and keeps the entire ~45-branch
  `instanceof` dispatch plus the `pendingAuraCard` fallback untouched; the per-variant "Awaiting …"
  log lines stay at the `PlayerInputService` helpers (ColorChoice precedent — replay does not
  re-log). Answered via the new `InteractionAnswer.PermanentChosen(UUID)`;
  `GameService.handlePermanentChosen` was single-kind, so it collapsed to dispatch-or-throw with
  the legacy "Not awaiting permanent choice" (GameService lost its `PermanentChoiceHandlerService`
  dep). **Replay-fidelity corrections**: the legacy reconnect replay sent a generic
  "Choose a permanent." (wrong for every varied begin prompt) with the merged validIds set in
  hash-scrambled order stuffed into the permanent-ID list — the any-target player-ID list was
  degraded into it entirely — and replay now re-sends the begin-time prompt and both ordered
  lists. **`copyInteractionInto` change**: the pre-seed carrier field is now copied
  unconditionally at the top (legacy only restored it while a PERMANENT_CHOICE was active, via
  the `beginPermanentChoice` rebuild; pre-seeds parked across other interaction windows — e.g.
  the clone-copy may-choice — were silently dropped from simulation copies, so a simulated
  accept hit "No pending permanent choice context"; simulation-only fidelity improvement,
  LIBRARY_SEARCH finding precedent). Removed: `InteractionContext.PermanentChoice` + all cases
  (`controlledPlayerMatchesContext`, `ReconnectionService` ×2, `GameSimulator` legacy decider,
  `GameData.copyInteractionInto`), `InteractionState.beginPermanentChoice/clearPermanentChoice/
  permanentChoiceContextView` + the `permanentChoice` sub-state field, and **`PermanentChoiceState`
  deleted entirely** (it was the last grouped sub-state — the "Grouped sub-states" section of
  `InteractionState` is gone). AI: `PermanentChoiceAiStrategy` (opponent's strongest creature by
  effective power → opponent's highest MV → own cheapest → first valid; ported verbatim, inlining
  the package-private `AiUtils.getOpponentId`); `AiChoiceHandler.handlePermanentChoice` is a thin
  `handleActiveInteraction` alias (no difficulty overrides existed — the CHOOSE_PERMANENT wire
  case calls the choice handler directly); `GameSimulator`'s PERMANENT_CHOICE action-gen and
  resolve cases read the record (first-valid pick order changes from HashSet-iteration to
  begin-time order — same arbitrary-to-ordered correction as prior kinds) and the decider lookup
  gained the record case. Tests: 48 files' `permanentChoice().playerId()/validIds()` /
  `permanentChoiceContextView()` reads rewritten to the typed accessor (scratchpad
  `fix_permanent_tests.pl`); the ~30 test files reading the kept `permanentChoiceContext()` field
  needed no changes; `PlayerInputServiceTest` registers the new handler; new focused
  `PermanentChoiceInteractionHandlerTest` covers both message variants, the merged validIds,
  dispatch delegation, and replay gating.
- `PendingInteraction.CombatDamageAssignment` (COMBAT_DAMAGE_ASSIGNMENT) — the active player's
  damage split for one multi-blocked (or trample/unblocked-overflow) attacker, fired
  mid-damage-step by `CombatDamageService.sendNextCombatDamageAssignment`. The record mirrors
  the legacy context 1:1 (`playerId, attackerIndex, attackerPermanentId, attackerName,
  totalDamage, List<CombatDamageTarget> validTargets, isTrample, isDeathtouch`); the handler
  derives the notification's `CombatDamageTargetView`s from the domain targets (the legacy
  begin site built both lists pairwise from the same data, and the legacy replay derived views
  the same way — **this kind's legacy replay was already faithful**, no prompt/order correction
  needed; the begin site's pairwise duplication collapsed to the domain list only). The legacy
  begin recipient (`CombatHelper.getEffectiveRecipient`) is the identical mind-control redirect
  `registry.begin` applies. **None of the assignment/validation math moved**: the answer is
  validated against the combat state on `GameData` (`combatDamagePhase1Complete`,
  `combatDamagePendingIndices`, the trample/deathtouch lethal-minimum checks in
  `CombatDamageService.handleCombatDamageAssigned`) — NOT the active record, deliberately
  preserving the legacy tolerance for out-of-order answers to still-pending attackers while
  another attacker's prompt is active. Answered via the new
  `InteractionAnswer.CombatDamageAssigned(attackerIndex, Map<UUID,Integer>)`; the handler's
  `handleAnswer` runs the exact legacy `GameService` entry body (apply → on
  `IllegalStateException` re-send the prompt via `resolveCombatDamage` and rethrow → else
  continue the loop via `turnProgressionService.handleCombatResult`, which begins a fresh
  record for the next pending attacker — sequential re-begin chain unchanged), exposed as the
  static `CombatDamageAssignmentInteractionHandler.applyAssignment`.
  **`GameService.handleCombatDamageAssigned` keeps a legacy fallback on dispatch miss** (calls
  the same `applyAssignment`) instead of dispatch-or-throw: the legacy entry never consulted
  the interaction, so a stray request with no assignment active took exactly that path (the
  combat flow rejects it with "Not in combat damage assignment phase" and re-sends); the
  fallback preserves it byte-for-byte and dies with the stage-4 teardown. Removed:
  `InteractionContext.CombatDamageAssignment` + all cases (`controlledPlayerMatchesContext`,
  `ReconnectionService` ×2, `GameSimulator` legacy decider, `GameData.copyInteractionInto`),
  `InteractionState.beginCombatDamageAssignment/clearCombatDamageAssignment(no-op)/
  combatDamageAssignmentContext` — with it `InteractionContext` is down to the two combat
  declarations. AI: `CombatDamageAssignmentAiStrategy` (lethal per blocker in presented order,
  remainder to the overflow player else piled on the first blocker; ported verbatim incl. the
  wrong-player warn log); `AiChoiceHandler.handleCombatDamageAssignment` is a thin
  `handleActiveInteraction` alias — the legacy handler was the only one reading its context
  inside `synchronized (gameData)`; the alias reads the immutable record unsynchronized like
  every other migrated kind (server-side thread-safety nuance only, no wire/log effect); no
  difficulty overrides existed. `GameSimulator`'s resolve case and `autoAssignCombatDamage`
  read the record; the decider lookup gained the record case. Tests: none read the legacy
  context (only `awaitingInputType()` enum asserts — unchanged); `CombatDamageServiceTest`
  swapped `@InjectMocks` for manual construction with a real registry + the real handler
  (mocked answer deps), since its trample-validation tests drive `resolveCombatDamage` into
  the begin; new focused `CombatDamageAssignmentInteractionHandlerTest` covers begin content,
  answer application + loop continuation, the invalid-assignment re-send-and-rethrow path,
  and replay gating.
- `PendingInteraction.AttackerDeclaration(activePlayerId)` +
  `PendingInteraction.BlockerDeclaration(defenderId)` (ATTACKER_DECLARATION /
  BLOCKER_DECLARATION) — **the final stage-3 kinds; `InteractionContext` is deleted
  entirely**, along with `InteractionState.currentContext()`/the `context` field,
  `GameService.controlledPlayerMatchesContext` and its `resolveActingPlayer` fallback (the
  registry's `activeDecidingPlayerId` covers everything), `GameData.copyInteractionInto`'s
  legacy context switch, `GameSimulator`'s legacy decider fallback, and the whole legacy half
  of `ReconnectionService` (now a one-line `replayPrompt` delegate; it lost its
  SessionManager/CombatService/GameQueryService/GameBroadcastService deps).
  The records carry only the decider: both prompts are re-derived from live combat state
  at prompt time, exactly as the legacy begin sites AND legacy replay did (attackers:
  attackable/must-attack/targets/tax/forced-attack; blockers: blockable + the filtered
  attacker list). The blocker filter was extracted into
  `CombatBlockService.getBlockableAttackerIndices` (exposed on the `CombatService` facade),
  used by both the step's skip-check and the handler prompt — **one replay-fidelity
  correction**: the legacy reconnect replay applied only the plain `hasCantBeBlocked` filter,
  omitting the defender-condition and historic-cast filters, so replay could show more
  attackers than the original prompt. The legacy begin recipient
  (`CombatHelper.getEffectiveRecipient`) is the identical mind-control redirect
  `registry.begin` applies; both combat services lost their `SessionManager` deps.
  Answers: `InteractionAnswer.AttackersDeclared(indices, attackTargets)` /
  `BlockersDeclared(assignments)`; the handlers run the exact legacy `GameService` entry
  bodies (attackers keep the catch → `handleDeclareAttackersStep` re-send → rethrow;
  answer validation stays in `CombatAttackService.declareAttackers` /
  `CombatBlockService.declareBlockers`, enum-checked, error texts untouched). **Both
  `GameService` entries keep a legacy fallback on dispatch miss** (combat-damage precedent):
  the legacy entries never consulted the interaction, so a stray declaration takes exactly
  the legacy path ("Not awaiting attacker/blocker declaration", attackers with the re-send
  quirk); the fallbacks die with the stage-4 teardown.
  `GameService.isAttackTaxManaPayment` (the CR 508.1i mana-ability window) reads the active
  record; test-only `setAwaitingInput(...)`-without-record states behave identically (the
  legacy context was equally absent there). AI: NO strategy — the decision engines answer
  combat via wire-message-driven logic keyed on the enum (unchanged); `GameSimulator`'s
  action-gen/resolve cases already read combat state, only its decider gained the record
  cases. Tests: 9 files' `beginAttackerDeclaration/beginBlockerDeclaration` calls rewritten
  to `beginInteraction(record, enum)` (scratchpad perl); new
  `CombatDeclarationInteractionHandlersTest` covers both prompts' live-state derivation,
  answer delegation incl. the attacker re-send-and-rethrow, replay gating, and cross-kind
  answer-shape gating.

**Migration recipe per kind** (repeat for each remaining `AwaitingInput` value):
1. Add the record to `PendingInteraction` (+ permits) and the answer shape to
   `InteractionAnswer` if new.
2. Write the `*InteractionHandler` bean: prompt = the old `PlayerInputService.begin*` message
   send; handleAnswer = the old `*HandlerService` logic (keep log/error text identical).
3. Switch begin sites to `interactionHandlerRegistry.begin(...)`; delete the old begin method.
4. Route the `GameService` entry point through `dispatchAnswer` (keep the legacy fallback for
   entry points shared by several kinds until all of them are migrated).
5. Delete the legacy context record + its cases (GameService.controlledPlayerMatchesContext,
   ReconnectionService both switches, GameSimulator.getInteractionPlayer,
   GameData.copyInteractionInto, InteractionState methods).
6. AI: add the `AiInteractionStrategy`, point the wire-message case at
   `handleActiveInteraction`, extend `GameSimulator.getInteractionPlayer` (and its
   `resolveInteraction` case if the simulator answers that kind).
7. Update tests that read the old context (`interaction.*Context()`) to read
   `interaction.activeInteraction()`.

## Remaining stages (in suggested order)

### ✅ Stage 3 — DONE: every `AwaitingInput` kind is registry-managed

`InteractionContext` and all grouped sub-states are gone; `InteractionState` is down to the
`awaitingInput` enum value, the `activeInteraction` record, and the independent
pre-seed/aura/equipment fields.

### ✅ Stage 4 — DONE: `AwaitingInput` enum teardown

The enum is deleted. It never reached the wire (zero references in `magical-vibes-networking`
and the frontend — `GameBroadcastService` only ever used the no-arg `isAwaitingInput()`), so
the teardown was purely internal:

- **`InteractionState`**: the `awaitingInput` field, `awaitingInputType()`, `setAwaitingInput`,
  and per-kind `isAwaitingInput(KIND)` are gone; `isAwaitingInput()` now means
  `activeInteraction != null`; `beginInteraction(record)` is single-argument.
  `GameData.copyInteractionInto` copies the pre-seed carrier + the active record only
  (enum-only states no longer exist, so the null-enum early return went away).
- **`InteractionHandler.legacyInputType()` removed** from the interface and all ~25 handlers
  (`HandCardChoiceInteractionHandlers.Base` lost its `legacyType` field/ctor param);
  the registry's `begin`/`beginWithoutPrompt` call the single-arg `beginInteraction`.
- **Per-kind checks → record checks** (12 sites): the entry validations in
  `AbilityActivationService` / `ChoiceHandlerService` (×2) / `GraveyardChoiceHandlerService`
  (×2) / `LibraryChoiceHandlerService` / `MayAbilityHandlerService` /
  `MultiPermanentChoiceHandlerService` / `ExileSupport`, the combat services'
  `declareAttackers`/`declareBlockers` guards, `EffectResolutionService`'s
  X_VALUE_CHOICE re-run check, and `AiDecisionEngine`'s blocker-rejection fallback all read
  `activeInteraction(PendingInteraction.X.class)`. `GameService.isAttackTaxManaPayment`
  dropped its now-redundant enum conjunct (the `instanceof` subsumes it).
- **The `GameService` dispatch-miss fallbacks stay** (`declareAttackers`/`declareBlockers`/
  `handleCombatDamageAssigned`): they are no longer a migration crutch but the genuine
  stray-message path — a declaration/assignment arriving with no matching interaction active
  takes the same legacy body, whose combat-flow validation now throws off the record check
  with the identical error texts (attackers keep the re-send-and-rethrow quirk).
- **AI**: `GameSimulator`'s three enum switches (`getLegalActions`, `autoResolveDecisions`,
  `resolveInteraction`) now switch on the active record with type patterns —
  behavior-identical since enum value ↔ record class is a bijection; the enum's
  `CARD_CHOICE, DISCARD_CHOICE` multi-label became two record cases sharing extracted
  helpers (`addHandChoiceActions` / `resolveHandCardChoice`), matching exactly the two kinds
  the enum label covered (TARGETED/EXILE/IMPRINT/DISCARD_COST still fall to `default`, as
  legacy). `getInteractionPlayer` lost its dead enum parameter. `AiManaManager`'s three
  "did a mana ability trigger a NEW prompt" compares became record-**class** compares
  (`interactionKind` helper) — byte-equivalent to the enum compare.
- **Tests** (~700 files, mechanical perl): `awaitingInputType()).isEqualTo(KIND)` →
  `activeInteraction()).isInstanceOf(PendingInteraction.X.class)`;
  `isNotEqualTo(KIND)` → `activeInteraction(PendingInteraction.X.class)).isNull()` (preserves
  the legacy passes-when-nothing-active semantics — `isNotInstanceOf` would fail on null);
  `awaitingInputType()).isNull()` → `activeInteraction()).isNull()`;
  `beginInteraction(record, KIND)` → single-arg; `setAwaitingInput(null)` →
  `clearAwaitingInput()`. The ~525 enum-only combat states
  (`setAwaitingInput(ATTACKER/BLOCKER_DECLARATION)` before calling the declare entries) now
  begin real records via new `GameTestHarness.beginAttackerDeclarationInput()` /
  `beginBlockerDeclarationInput()` (active player / non-active defender — the record IDs are
  not consulted by the answer path, which validates against `gd.activePlayerId`); the few
  non-harness sites (AiManaManagerTest, Easy/Hard/MediumAi, TurnProgressionServiceTest,
  AutoPassServiceTest, StackResolutionServiceTest) begin minimal real records inline
  (presence/kind is all those tests exercise). agent-docs snippets updated
  (TEST_RECIPES.md, CARD_IMPLEMENTATION_PLAYBOOK.md).

### ✅ Stage 5 — DONE: effect-resolution resumption fields stay (with one simulation-copy fix)

**Decision: the five fields (`pendingEffectResolutionEntry` / `pendingEffectResolutionIndex`
/ `resolvingMayEffectFromStack` / `resolvedMayAccepted` / `resolvedMayTargetingEntry`) are
NOT migrated to the queue.** The migration is not provably behavior-preserving, for
structural reasons — not implementation effort:

1. **They hold mutable continuation state, not an immutable prompt.**
   `pendingEffectResolutionEntry` / `resolvedMayTargetingEntry` reference a mutable
   `StackEntry` that the resumption protocol mutates in place (`setTargetId` in
   `GraveyardChoiceHandlerService.MAY_ABILITY_TARGET`, `PermanentChoiceTriggerHandlerService
   .handleMayAbilityTrigger`, `MayAbilityHandlerService.setUpSelfTargetIfNeeded` / the
   single-match graveyard path). The unified queue's invariant since stage 1 is *immutable
   records, shallow `simulationCopy`* (`copy.pendingInteractions.addAll(...)`), while these
   fields get per-field deep `StackEntry` copies. Queueing them would either share a mutable
   `StackEntry` between the live game and MCTS simulation copies (rollout mutations
   corrupting live state) or force a per-element deep-copy special case that breaks the
   queue's uniform copy semantics.
2. **The protocol depends on object identity across two fields.**
   `resolvedMayTargetingEntry` is assigned the *same object* as
   `pendingEffectResolutionEntry` (`MayAbilityHandlerService.handleResolutionTimeMayChoice`
   reads it into `pendingEntry` and parks it). `PermanentChoiceTriggerHandlerService` then
   sets the chosen target *through the alias* and resumes *through
   `pendingEffectResolutionEntry`* — correct only because both reference one object. Any
   record-bundling design that copies or wraps the entry silently drops the target hand-off.
3. **The index travels separately from the entry.** The `MAY_ABILITY_TARGET` resumptions
   call `resolveEffectsFrom(gameData, resolvedMayTargetingEntry-alias,
   gameData.pendingEffectResolutionIndex)` — so `(entry, index)` cannot be sealed into one
   immutable item without first proving the pairing invariant across every mid-flow
   re-suspension.
4. **Nothing here is queue-shaped.** At most one suspended resolution exists at a time
   (each `resolveEffectsFrom` run either completes — clearing the fields — or re-suspends,
   overwriting them); `resolvedMayAccepted` is a one-shot answer mailbox consumed by the
   CR 603.5 re-run of the same effect, and `resolvingMayEffectFromStack` is the mode flag
   for it. FIFO order and type-filtered scans add nothing; the fields already copy correctly
   (deep) for simulation.

**One code change landed with this analysis — a simulation-only fidelity fix** (third of its
kind, after the LIBRARY_SEARCH and PERMANENT_CHOICE `copyInteractionInto` findings):
`simulationCopy` used to deep-copy `pendingEffectResolutionEntry` and
`resolvedMayTargetingEntry` **separately**, breaking their aliasing in the copy — an MCTS
rollout answering a pending CR 603.5 targeting choice set the target on one copy and resumed
the other, losing the target. The copy now preserves the shared identity when the two fields
alias (and keeps independent deep copies when they are genuinely distinct). Covered by two
new `GameDataDeepCopyTest` cases.

### ✅ Stage 6 — COMPLETE: per-mechanic `pending*` carry-over fields → interaction payloads

Goal: move the ~50 per-mechanic global mutable fields on `GameData` (one per card mechanic,
each with hand-written set/clear discipline, a `simulationCopy` line, and a Karn-restart
clear) into the payload of the interaction they belong to, so the state exists only while its
interaction/flow is active and copies with it.

**Batch 1 — DONE: `MultiPermanentChoiceContext` (12 fields removed).**
New sealed `model/MultiPermanentChoiceContext` — the multi-select analogue of
`PermanentChoiceContext`. `PendingInteraction.MultiPermanentChoice` gained a `context`
component (null = legacy `GameData`-flag dispatch, kept only for the queued-state kinds
below); `PlayerInputService.beginMultiPermanentChoice` gained a context overload (the 5-arg
form delegates with null). `MultiPermanentChoiceHandlerService.handleMultiplePermanentsChosen`
dispatches on the context record instead of the flag chain — behavior-identical since every
begin site set exactly one flag and exactly one context is snapshotted per begin. Migrated
(field → record): `pendingExileDamagedPlayerControlsPermanent` → `ExileDamagedPlayerControls`,
`pendingSacrificeSelfToDestroySourceId` → `SacrificeSelfToDestroy(sourceId)`,
`pendingTransformAndAttachSourceId` → `TransformAndAttach(sourceId)`,
`pendingSacrificeAttackingCreature` → `SacrificeAttackingCreatures`,
`pendingCombatDamageBounceTargetPlayerId` → `CombatDamageBounce(targetPlayerId)`,
`pendingAimCounterPlacement` → `AimCounterPlacement`,
`pendingOwnPermanentCounterPlacement/Type/Count` → `OwnPermanentCounterPlacement(type, count)`,
`pendingAwakeningCounterPlacement` → `AwakeningCounterPlacement`,
`pendingProliferateCount` → `Proliferate(remainingCount)` (multi-round countdown rides on the
fresh-record-per-pick re-begin, DiscardChoice precedent),
`pendingTapSubtypeBoostSourcePermanentId` → `TapSubtypeBoost(sourceId)`.
Begin sites converted in the ten owning effect handlers / `PermanentCounterSupport`; the
answer methods take the context instead of reading+clearing fields (the flag-clear-at-top
lines are gone — `clearAwaitingInput` retires the record). `GameData` lost the 12 fields +
copy lines; `KarnRestartGameEffectHandler` lost their resets (the active-interaction clear
covers them). The AI reads none of these fields (verified) — no AI change.

**Batch 2 — DONE: forced-sacrifice / destroy-rest family (7 fields removed).**
`MultiPermanentChoiceContext.ForcedSacrifice(sacrificingPlayerId, remainingChoosers,
accumulatedSacrificeIds)` and `.DestroyRestChoice(remainingChoosers, protectedIds,
sourceName)` carry the APNAP queue remainder (`List<PendingForcedSacrifice>`, record kept)
and the accumulated ids across sequential re-begins; every re-begin constructs fresh
immutable lists (`List.copyOf`), so the shallow interaction copy stays safe. The legacy
answer-time discriminator `simultaneousFlow = !simultaneousIds.isEmpty() || !queue.isEmpty()`
is computed from the carried lists — identical inputs, including the edge where an
each-player flow with a single chooser and no auto-picks takes the direct path.
`DestructionSupport.beginNextForcedSacrificeFromQueue/beginNextDestroyRestChoice/
completeDestroyRestChoice/performDestroyAllCreaturesExcept/performSimultaneousSacrifice` now
take the carried state as parameters; the four each-player/each-opponent seeding handlers
collect auto-ids + choosers locally; the two direct-flow handlers
(`TargetPlayerSacrifices…`, `DamageSourceControllerSacrifices…`) begin with
`ForcedSacrifice(playerId, List.of(), List.of())`. Removed: `pendingForcedSacrificeCount`,
`pendingForcedSacrificePlayerId`, `pendingForcedSacrificeQueue`,
`pendingSimultaneousSacrificeIds`, `pendingDestroyRestMode`,
`pendingDestroyRestProtectedIds`, `pendingDestroyRestSourceName`.

**Batch 3 — DONE: pile separation (8 fields removed → queued `PendingPileSeparation`).**
The flow spans two interaction windows (pile-1 multi-choice → pile-choice may prompt), so it
became a queued `PendingInteraction` record (`PendingKnowledgePoolCast` precedent), not an
interaction payload: `PendingPileSeparation(controllerId, targetPlayerId, allPermanentIds,
cards, cardOwners, pile1Ids, pile2Ids)` with compact-constructor `List.copyOf` defensiveness;
`cardPileMode()` = `!cards.isEmpty()` (the legacy mode discriminator verbatim). The effect
handlers queue it; step 1 (`DestructionSupport.completePileSeparationStep1` /
`GraveyardReturnSupport.completeCardPileSeparationStep1`) polls it and re-queues it with the
piles filled; step 2 (both `…Step2` methods, reached via `MayAbilityHandlerService`'s
peek-check) polls it to completion — the poll replaces the legacy 6/8-line field cleanup.
The dispatch checks (`MultiPermanentChoiceHandlerService`, `GraveyardChoiceHandlerService`,
`MayAbilityHandlerService`) read `has/peekPendingInteraction(PendingPileSeparation.class)`
instead of the flag. Removed: `pendingPileSeparation`, `…ControllerId`, `…TargetPlayerId`,
`…AllPermanentIds`, `…Pile1Ids`, `…Pile2Ids`, `…Cards`, `…CardOwners`.

**Batch 4 — DONE: discard carry-over (5 fields removed → `DiscardFollowUp` on the record).**
New `model/DiscardFollowUp` record carried as a `followUp` component on
`PendingInteraction.DiscardChoice`; the fresh-record-per-pick re-begins (continuation and the
invalid-index re-prompt in `CardChoiceHandlerService.handleDiscardCardChosen`) pass it forward
unchanged, and the sequence-completion branch reads it instead of `GameData`. Migrated:
`pendingRummageDrawCount` → `DiscardFollowUp.rummage(drawCount)` ("discard, then draw that
many": `DiscardAndDrawCardEffectHandler`, `DiscardUpToThenDrawThatManyEffectHandler`),
`pendingUntapAfterDiscardPermanentId` → `DiscardFollowUp.untap(permanentId)` (Elaborate
Firecannon), and the `pendingEachPlayerDiscardQueue`/`…ControllerId`/`…Amount` APNAP trio →
`DiscardFollowUp.eachPlayer(remainingChoosers, controllerId, amount)`;
`PlayerInteractionSupport.startNextEachPlayerDiscard` takes the follow-up, pops the local
remainder (same skip-empty-hands loop, same `discardCausedByOpponent` assignment per chooser),
and re-begins with `withRemainingEachPlayerDiscards`. `beginDiscardChoice`/`resolveDiscardCards`
gained follow-up overloads (existing arities delegate with `DiscardFollowUp.NONE` — the
cleanup-discard, may-penalty, and plain-discard call sites are untouched). Bonus:
`pendingRummageDrawCount`/`pendingUntapAfterDiscardPermanentId` had NO `simulationCopy` lines
(pre-existing copy gap) — carried on the interaction record they now copy correctly for free.
Deliberately NOT migrated (discard-EVENT observers, read on the random/immediate discard paths
too, e.g. `StackResolutionService` ~535, `TriggerCollectionService` ~282):
`pendingReturnToHandOnDiscardType`, `pendingTransformOnCreatureDiscard`,
`discardCausedByOpponent` — they would migrate only if the discard event itself grew a context
parameter.

**Batch 5 — DONE: library-search follow-ups (6 fields removed → `LibrarySearchFollowUp`).**
New `model/LibrarySearchFollowUp` record carried on `LibrarySearchParams` (new `followUp`
component + builder method, compact-constructor null→NONE); every re-begin (the multi-pick
countdown in `LibraryChoiceHandlerService`) passes it forward, and the follow-up starter
methods consume it from the record: `pendingBasicLandToHandSearch` →
`forBasicLandToHand()` (Cultivate second pick; the begun hand search carries
`clearBasicLandToHand()`), `pendingCardToGraveyardSearch` → `forCardToGraveyard()` (Final
Parting), `pendingEachPlayerBasicLandSearchQueue`/`…Tapped` →
`eachPlayerBasicLand(remainingSearchers, tapped)` (Field of Ruin / Old-Growth Dryads; both
the `LibrarySearchSupport.startNextEachPlayerBasicLandSearch` loop and the
`LibraryChoiceHandlerService` private continuation take the follow-up and re-begin with
`withRemainingEachPlayerBasicLandSearches`), `pendingOpponentExileChoice` →
`opponentExile(choice)` (Distant Memories; the `PendingOpponentExileChoice` record survives
as the payload), `imprintSourcePermanentId` → `imprint(sourceId)` (Strata Scythe / Hoarding
Dragon / Clone Shell; consumed at both EXILE_IMPRINT completion paths).
`performLibrarySearch` gained a follow-up overload. Note: the static factories are named
`forBasicLandToHand`/`forCardToGraveyard` because bare names would collide with the record
accessors. Bonus copy-gap closures: the two booleans had no `simulationCopy` lines and the
queue had no Karn-restart reset — all moot now.

**Batch 6 — DONE: `pendingExileFromHandPlayPermissionController` (1 field removed).**
`PendingInteraction.ExileFromHandChoice` gained a `playPermissionControllerId` component
(Fiend of the Shadows: controller may play the exiled card); `beginExileFromHandChoice`
gained the overload, the multi-pick re-begins carry it, and the per-card grant in
`CardChoiceHandlerService.handleExileFromHandChosen` reads it off the record (the
completion-time null-out is gone with the field).

Note: a pre-existing failure in `MCTSEngineTest` ("MCTS selects removal over creature when
opponent has a blocker") reproduces identically on a clean checkout — unrelated to stage 6.

**Deliberately staying as documented explicit fields** (lifecycle is not a single
interaction):

- `pendingGraveyardReturnQueue` — considered for the batch-2 carried-queue treatment but
  left global on purpose: its continuation check
  (`GraveyardChoiceHandlerService.handleGraveyardCardChosen` end) fires on the completion of
  ANY graveyard choice, so an interleaved `GraveyardChoice` begun mid-flow (e.g. by an ETB
  trigger of a creature returned to the battlefield by a queue step) still advances the
  queue. Carried on the record, that interleaved choice would hold an empty queue and the
  continuation would be lost. Already an immutable-record queue with a correct
  `simulationCopy` line.

- The effect-resolution quintet (stage 5 decision above).
- `chosenXValue` — one-shot answer mailbox consumed by the re-run of the same effect
  (`XValueChoiceInteractionHandler` writes it; same protocol as `resolvedMayAccepted`).
- `pendingETBDamageAssignments` — cast-time data consumed at resolution; spans cast →
  resolve, not an interaction.
- `pendingSearchContext` — Leonin Arbiter tax; parked across the may-ability window
  (`permanentChoiceContext` precedent).
- `pendingAbilityActivation` — activation continuation spanning its cost-choice interactions
  (already documented on the DiscardCostChoice/GraveyardExileCostChoice records).
- `pendingMayAbilities` — a genuine FIFO whose head drives `MayAbilityChoice`; its own
  refactor if ever.
- `pendingLibraryBottomReorders` — continuation queue spanning reorder interactions
  (Warp World).
- `graveyardTargetOperation` / `cloneOperation` / `warpWorldOperation` — pre-existing grouped
  operation-state objects.
- Everything that is delayed-trigger or turn-scoped state rather than interaction state
  (`pendingDelayed*`, `pendingDestroyAtEndStep`, `pendingTokenExiles*`,
  `pendingLethalDamageDestructions`, `pendingManaAbilityTriggers`, `pendingTurnControl`,
  `pendingNextInstantSorceryCopyCount`, `pendingExileReturns`, …) — out of scope.
  **(Superseded by stage 7 below: the delayed-action / turn-scoped subset — `pendingDelayed*`,
  `pendingDestroyAtEndStep`, `pendingTokenExiles*`, `pendingExileReturns`, and the end-of-combat
  sac/exile/equipment/transform fields — were later collapsed onto one `delayedActions` queue.
  The truly-not-migrated ones stay: `pendingLethalDamageDestructions`,
  `pendingManaAbilityTriggers`, `pendingTurnControl`, `pendingNextInstantSorceryCopyCount`.)**

## Constraints (from the task)

- Work on `main`. No behavior change (log text, ordering, wire protocol, frontend untouched).
- `GameData` accessed under `synchronized (gameData)` — the queue is a plain `ArrayDeque`,
  same safety level as the fields it replaced.
- AI (`magical-vibes-ai`) must be updated in lockstep with each migrated kind.
- Keep the codebase consistent (compiles, targeted tests green, no half-wired kind) at every
  stop point; the user runs the full suite after each commit.

## Stage 7 — DONE: delayed-action / turn-scoped `pending*` fields → one `delayedActions` queue

Goal: collapse the 13 ad-hoc "do X later at timing point Y" fields that stage 6 explicitly
scoped OUT ("delayed-trigger or turn-scoped state rather than interaction state … out of
scope") into a single unified queue, the delayed-action analogue of stage 1's
`pendingInteractions`. Same motivation: each field needed hand-written add/drain discipline, its
own `simulationCopy` line (repeated copy-gap findings — stage 6 batch 4/5), and often a
Karn-restart clear.

**Design** (mirrors `PendingInteraction`):
- New **`com.github.laxika.magicalvibes.model.action` package** (domain module) holding the
  **marker** sealed interface `DelayedAction` (no methods) plus one file per record family — all 13
  are standalone top-level records in that package, referenced by simple name (each consumer imports
  the specific records it uses). This keeps them OUT of the already-large `GameData` god-class.
  `PendingExileReturn` moved here from `model` too (it must share the sealed interface's package —
  the domain module is an unnamed module, so a sealed interface's permitted types must be
  same-package); it stays a standalone record because it is also the value type of the unrelated
  `exileReturnOnPermanentLeave` O-ring map. **Deliberate divergence from the task's suggested
  `Timing` enum**: nothing dispatches on timing — the drain happens by record `Class` at fixed call
  sites, and the cross-family order is a property of those call-site chains (below), exactly as
  `PendingInteraction` keeps its cross-kind order at the call sites. A `timing()` method would be
  dead code, so it was omitted; the `PendingInteraction` marker precedent won over the suggestion.
- `GameData` gained ONE field `public final List<DelayedAction> delayedActions =
  Collections.synchronizedList(new ArrayList<>())` plus type-filtered helpers copied from the
  `pendingInteractions` pattern: `queueDelayedAction`, `hasDelayedAction(Class)`,
  `getDelayedActions(Class)` (read-only snapshot, insertion order — for the per-combat-step loot
  read), `drainDelayedActions(Class)` (remove+return all of a kind, insertion order),
  `drainDelayedActions(Class, Predicate)` (filtered drain, leaves non-matching in place — for the
  per-step exile returns), `clearDelayedActions(Class)`, plus the two keyed-accumulator helpers
  `addDelayedPlusOneCounters(id, delta)` / `getDelayedPlusOneCounters(id)` (see below).
- ONE `simulationCopy` line — `copy.delayedActions.addAll(this.delayedActions)` — replacing 13
  scattered lines. Every record holds only UUIDs / ints / enums / immutable `PermanentPredicate` /
  shared `Card` refs, so the shallow `addAll` reproduces the exact copy depth of the fields it
  replaced (the old lines were all `addAll`/`putAll` shallow copies; `Card` was shared before and
  is shared now). Covered by a new `GameDataDeepCopyTest.deepCopyPreservesDelayedActions` case
  (value + insertion-order equality, list independence).

**Records** (removed field → record; each its own file in `model.action`, referenced by simple
name). New: `SacrificeAtEndOfCombat(permanentId)`, `ExileTokenAtEndOfCombat(permanentId)`,
`DestroyEquipmentAtEndOfCombat(creatureId)`, `ExileAndReturnTransformedAtEndOfCombat(permanentId)`,
`ExileTokenAtEndStep(permanentId)`, `SacrificeAtEndStep(permanentId)`, `DestroyAtEndStep(permanentId)`,
`DelayedPlusOneCounters(permanentId, totalCounters)`. Four records that were previously nested in
`GameData` moved here unchanged apart from `implements DelayedAction`: `DelayedUntapPermanents`,
`DelayedGraveyardToHandReturn`, `DelayedGraveyardToBattlefieldTransformedReturn`,
`DelayedCombatDamageLoot`. `PendingExileReturn` is the 13th (moved `model` → `model.action`).

**Cross-family servicing order (rules-relevant — do not change; preserved at the drain call sites):**
- **End of combat** — `TurnProgressionService.advanceStep` guard (now four `hasDelayedAction`
  checks) then `CombatService` in this exact order:
  `processEndOfCombatSacrifices` (SacrificeAtEndOfCombat) → `processEndOfCombatExiles`
  (ExileTokenAtEndOfCombat) → `processEndOfCombatEquipmentDestruction`
  (DestroyEquipmentAtEndOfCombat) → `processEndOfCombatExileAndReturnTransformed`
  (ExileAndReturnTransformedAtEndOfCombat).
- **End step** — `StepTriggerService.handleEndStepTriggers` in this exact order:
  ExileTokenAtEndStep → SacrificeAtEndStep → DestroyAtEndStep → DelayedPlusOneCounters →
  DelayedUntapPermanents → DelayedGraveyardToHandReturn →
  DelayedGraveyardToBattlefieldTransformedReturn (the pre-existing non-delayed end-step steps that
  follow — saga chapters, etc. — are unchanged).
- **Per turn step** — `StepTriggerService.processPendingExileReturns(step)`, called from
  `advanceStep` with the upcoming step, drains only `PendingExileReturn`s whose `returnStep()`
  matches (via the filtered `drainDelayedActions` overload; non-matching entries stay in place in
  their original order, exactly as the old collect-remaining-and-re-add did).
- **Combat damage step** — `CombatDamageService.processDelayedCombatDamageLootTriggers` READS
  (does not drain) `DelayedCombatDamageLoot` via `getDelayedActions`, firing once per combat-damage
  step; the family is drained (cleared) only at turn cleanup (`TurnProgressionService`, via
  `clearDelayedActions`).

**Set→ordered-iteration notes** (the same arbitrary-to-ordered change stage 3 documented
repeatedly). The seven families that were `Set<UUID>` (`ConcurrentHashMap.newKeySet()`) or a
`Map<UUID,Integer>` had NO deterministic iteration order; the `List`-backed queue makes each
family's drain iterate in **insertion order**. Observable only in the relative order of the
independent per-permanent log lines within one family's drain (e.g. two Mimic Vat tokens exiling
at the same end step, two equipped creatures losing Equipment at end of combat). No rules effect.
The end-of-combat sacrifice/exile drains were already deterministic there (they iterate the
battlefield and test set membership) — that battlefield-order iteration is unchanged; only the
membership set is now built from the drained list.

**Keyed-accumulator family (`DelayedPlusOneCounters`).** The old
`pendingDelayedPlusOnePlusOneCounters` was a `Map<UUID,Integer>` accumulated via
`getOrDefault + put` (Protean Hydra: each removed +1/+1 counter schedules 2, summed per permanent,
and the drain emits ONE merged log line + `total/2` triggers per permanent). To preserve that
exactly, `addDelayedPlusOneCounters(id, delta)` keeps the at-most-one-record-per-permanent
invariant (removes any existing record for the id, adds a fresh one with the summed total). This
is the "set-membership/dedup semantics" the task flagged — handled with a per-family accumulate
helper rather than a generic dedup on `queueDelayedAction`. The two producers
(`DamagePreventionService`, `StateBasedActionService`) call it; the drain sums are byte-identical
because each producer's delta is even, so `sum/2 == Σ(delta/2)`.

**Karn-restart / end-the-turn partial clears — pre-existing gaps preserved (NOT fixed).**
`KarnRestartGameEffectHandler` clears ONLY 5 of the 13 families today
(`PendingExileReturn`, `ExileTokenAtEndStep`, `SacrificeAtEndStep`, `SacrificeAtEndOfCombat`,
`ExileTokenAtEndOfCombat`) and `TurnSupport.clearCombatState` (Time Stop / end-the-turn) clears
ONLY 2 (`SacrificeAtEndOfCombat`, `ExileTokenAtEndOfCombat`). The other 8 families
(`DestroyEquipmentAtEndOfCombat`, `ExileAndReturnTransformedAtEndOfCombat`, `DestroyAtEndStep`,
`DelayedPlusOneCounters`, `DelayedUntapPermanents`, `DelayedGraveyardToHandReturn`,
`DelayedGraveyardToBattlefieldTransformedReturn`, `DelayedCombatDamageLoot`) were never reset in
either place. Both sites therefore use **per-kind `clearDelayedActions(...)` calls, NOT a blanket
`delayedActions.clear()`**, preserving the exact pre-existing behavior (a blanket clear would be a
behavior change — documenting the gap rather than silently fixing it, per the task).

**AI:** `magical-vibes-ai` reads none of these 13 fields (verified by grep across
`GameSimulator`/`CombatSimulator`/decision engines) — no lockstep change needed.

**Tests** (~25 files, targeted-green): direct `gd.<field>` reads/writes rewritten to the new
accessors — writes → `queueDelayedAction(new GameData.X(id))` (plus counters →
`addDelayedPlusOneCounters`); `.contains(id)` → `getDelayedActions(GameData.X.class)).contains(new
GameData.X(id))` (record value equality); `.isEmpty()`/`.hasSize`/`.getFirst().field()` →
`getDelayedActions(GameData.X.class)` (record accessors unchanged); plus-counter map reads
(`.get(id)`/`.doesNotContainKey`) → `getDelayedPlusOneCounters(id)` (`isZero()` for absence).
`GameDataDeepCopyTest` gained the delayed-action copy case. agent-docs field references
(`EFFECTS_INDEX`, `ORACLE_TEXT_EFFECT_MAP`, `EFFECTS_QUICK_REFERENCE`) updated to the record /
`delayedActions` names. Ran green: `StepTriggerServiceTest`, `TurnProgressionServiceTest`,
`CombatServiceTest`, `StateBasedActionServiceTest`, `EndTurnEffectHandlerTest`,
`FlickerEffectHandlerTest`, `RegisterDelayedCombatDamageLootEffectHandlerTest`,
`PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffectHandlerTest`,
`GameDataDeepCopyTest`, and the 15 affected card tests (Geist of Saint Traft, Teferi Hero of
Dominaria, Tiana, Protean Hydra, Jace Cunning Castaway, Sudden Disappearance, Valduk, Postmortem
Lunge, Gruesome Encore, Choreographed Sparks, Time Stop, Venser, Glimmerpoint Stag, Oath of
Teferi, Conciliator's Duelist).
