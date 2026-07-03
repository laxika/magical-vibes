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

## Remaining stages (in suggested order)

### Stage 2 — Card-specific single-value pending fields → interaction records

Migrate these `GameData` fields into `PendingInteraction` records on the queue (each is a
single value paired with an `AwaitingInput` value today):

- `pendingSphinxAmbassadorChoice` (`PendingSphinxAmbassadorChoice`)
- `pendingKarnScionControllerId` + `pendingKarnScionReturnFromExile` (Karn Scion +1/−1 reveal)
- `pendingKarnRestartCards` + `karnRestartControllerId` (Karn Liberated restart)
- `pendingCapriciousEfreetState` (`PendingCapriciousEfreetState`)
- `knowledgePoolSourcePermanentId` + `InteractionContext.KnowledgePoolCastChoice`
- `InteractionContext.MirrorOfFateChoice`
- Then remove `KNOWLEDGE_POOL_CAST_CHOICE` / `MIRROR_OF_FATE_CHOICE` from `AwaitingInput`.

No card names may remain in `GameData` or shared enums afterwards.

### Stage 3 — InteractionHandlerRegistry (engine)

Same shape as `EffectHandlerRegistry`: keyed by interaction record class. Each handler:
(a) renders/broadcasts the prompt (also used by `ReconnectionService` for replay),
(b) validates + applies the answer, then advances the queue / chains continuations,
(c) optionally exposes AI hooks. AI side: per-type strategy beans in `magical-vibes-ai`
replacing the `AiChoiceHandler` switch on `AwaitingInput`.
Migrate the 18 `service/input/*HandlerService` dispatch sites kind-by-kind.

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
