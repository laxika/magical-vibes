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

Suggested order: MAY_ABILITY_CHOICE, the card/graveyard/permanent choice families, LIBRARY_SEARCH /
LIBRARY_REVEAL_CHOICE, COMBAT_DAMAGE_ASSIGNMENT, and last the combat declarations
(ATTACKER/BLOCKER) which are entangled with `CombatService`.

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
