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

### Stage 3 continuation — migrate the remaining kinds

Suggested order: PERMANENT_CHOICE,
LIBRARY_SEARCH / LIBRARY_REVEAL_CHOICE, COMBAT_DAMAGE_ASSIGNMENT, and last the combat
declarations (ATTACKER/BLOCKER) which are entangled with `CombatService`.

### Stage 4 — Generic `AwaitingInput` kinds → interaction records

`InteractionState` / `InteractionContext` (the "currently active interaction" holder) becomes
a thin pointer to the active `PendingInteraction`; the 26-value `AwaitingInput` enum and the
grouped sub-states (`CardChoiceState`, `LibraryViewState`, `MultiSelectionState`, …) go away.
Wire messages must not change — derive any legacy enum names needed by the frontend from the
interaction record class.

### Stage 5 — Effect-resolution resumption

`pendingEffectResolutionEntry` / `pendingEffectResolutionIndex` / `resolvingMayEffectFromStack`
/ `resolvedMayAccepted` / `resolvedMayTargetingEntry` (see `EffectResolutionService.resolveEffectsFrom`)
become a queue-driven resumption item — only if provably behavior-preserving; otherwise keep
the fields and document why here.

## Constraints (from the task)

- Work on `main`. No behavior change (log text, ordering, wire protocol, frontend untouched).
- `GameData` accessed under `synchronized (gameData)` — the queue is a plain `ArrayDeque`,
  same safety level as the fields it replaced.
- AI (`magical-vibes-ai`) must be updated in lockstep with each migrated kind.
- Keep the codebase consistent (compiles, targeted tests green, no half-wired kind) at every
  stop point; the user runs the full suite after each commit.
