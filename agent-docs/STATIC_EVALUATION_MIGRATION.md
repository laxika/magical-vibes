# Static-Evaluation Migration Plan

Forward-looking plan for removing the `staticEvaluation` recursion guards and replacing them
with CR 613 layered reads. Companion to `LAYER_SYSTEM.md`, which is the backward-looking step
log — when a stage here completes, record the outcome there as the next numbered step and tick
the checkbox here.

**Line numbers drift.** Re-verify every `file:line` below with a grep before acting on it.
Treat this document as a map, not as ground truth.

## The problem

Static-bonus assembly cannot call the fully layered queries, because those call
`computeStaticBonus`, which re-enters assembly. The engine's workaround is a second, weaker set
of "recursion-safe" matchers selected by `ConditionContext.staticEvaluation()` /
`AmountContext.staticEvaluation()`. Those matchers read printed and stored characteristics only,
so continuous effects are invisible to them.

That is not a theoretical gap. It currently produces an illegal board state.

### Verified reproduction

Battlefield: `Rusted Relic` (SOM 199) + `Silverskin Armor` (MBS 132) equipped to `Grizzly Bears`.

Printed artifacts: Relic + Armor = 2. The Armor's `GrantCardTypeEffect` makes the Bears an
artifact in layer 4 (CR 613.1d), so the true count is 3 and metalcraft is met.

Observed:

```
bears isArtifact (layered) = true       3 artifacts, metalcraft IS met
bears isArtifact (printed) = false
relic isCreature          = true        layered path animates it
relic power / toughness   = 0 / 0       static path did NOT
```

`Rusted Relic` is a 0/0 creature and dies to state-based actions (CR 704.5f). Equipping
`Silverskin Armor` to any creature kills your `Rusted Relic` instead of animating it as a 5/5.

### Why the two paths disagree

`GameQueryService.isCreature` uses the layered artifact count; the static assembly path uses the
printed-only count. Same condition, two answers, depending on the caller.

## Relevant Comprehensive Rules

All verified against the CR effective June 19, 2026 via the `rules` MCP `get_rule` tool.
Re-verify before citing in code or commit messages.

| Rule | Content |
|---|---|
| 613.1d | Layer 4: type-changing effects |
| 613.1e | Layer 5: color-changing effects |
| 613.1f | Layer 6: ability-adding / removing effects |
| 613.1g | Layer 7: power/toughness-changing effects |
| 613.3 | Within layers 2-6: CDAs first, then timestamp order; dependency may alter |
| 613.6 | An effect applying in several layers keeps applying to the same set of objects |
| 613.7 | Timestamp system |
| 613.8 | Dependency overrides timestamp |
| 613.8a | Definition of "depends on" |
| 613.8b | Dependent effects wait; dependency loops fall back to timestamp order |
| 613.8c | Order is re-evaluated after each effect is applied |
| 604.3 | Characteristic-defining abilities |
| 704.5f | A creature with toughness 0 or less is put into its owner's graveyard |

`Rusted Relic` depending on `Silverskin Armor` is textbook CR 613.8a: same layer, applying the
Armor changes what the Relic's effect does, neither is a CDA.

## Current state of the tree

Landed with this document (crash fix for the fuzz-test stack overflow, targeted tests green):

- `GameQueryService.hasSelfBecomeCreatureEffect(GameData, Permanent, boolean)` overload; the
  2-arg form delegates with `false`.
- Three static-pass callers pass `true`: `StaticEffectSupport.isEffectivelyCreature`,
  `AllLandsAreCreaturesEffectHandler`, `LayerSystemService` layer-7b.
- Two regression tests in `RustedRelicTest` (anthem + metalcraft, anthem without metalcraft).

**Keep this.** It is the prerequisite that routes static-pass callers to the recursion-safe
branch. Stage A then makes that branch correct. Dropping it re-opens the stack overflow, because
`ConditionEvaluationService.isMetalcraftMet` takes the layered branch whenever the flag is off.

## Working agreement

- The gate is the **full test suite**, which the user runs. Claude runs targeted tests only.
- **Every slice ends with the suite green.** Never carry a half-migrated leaf across a session
  boundary — the next session cannot distinguish inherited breakage from its own.
- Expect card-test failures that are *not* regressions: cards silently relying on the
  approximation. Each needs a rules verdict, not a reflexive revert. Where a card's correct
  behavior is ambiguous, search for the official ruling (per CLAUDE.md).
- Commit between slices, on the user's instruction.
- Do not automate this loop with `/loop` or workflows. Parallel agents are useful for
  investigation, not for the migration itself — they would conflict on the same files.

## Stage A — type leaves read layer-4 state

Small. Follows a pattern already established six times inside the same method: the creature,
land, planeswalker, subtype, color and keyword leaves of
`StaticEffectSupport.matchesStaticFilter` already read `CharacteristicState` when a pass is
active. These do not.

**Done 2026-07-30** — step 15 in `LAYER_SYSTEM.md`.

- [x] `PermanentIsArtifactPredicate` leaf (~`StaticEffectSupport.java:364`) currently calls the
      printed-only `gameQueryService.isArtifact(target)` and ignores layer-4 state even mid-pass.
      **This one line is the `Rusted Relic` bug.**
- [x] Route `ConditionEvaluationService.isMetalcraftMet`'s static branch (~`:547`) through that
      leaf instead of `gameQueryService::isArtifact`.
- [x] `PermanentIsEnchantmentPredicate` leaf (~`:373`) — printed only; misses transient and
      persistent granted ENCHANTMENT (`Enchanted Evening`, `Song of the Dryads`).
- [x] `PermanentHasSupertypePredicate` leaf (~`:411`) — printed only; misses granted/removed
      legendary and snow.
- [x] `PermanentIsHistoricPredicate` leaf (~`:384`) — uses printed artifact + printed LEGENDARY
      + printed/transient SAGA.
- [x] Divergence: the creature leaf (~`:349`) omits `isPermanentlyAnimated()`, while
      `isCreature`, `isCreatureInStaticPass` and `isCreatureForL4` all include it.
- [x] Divergence: `PermanentColorInPredicate` (~`:291`) does not read `getGrantedColors()`
      outside a pass, while `PermanentIsMulticoloredPredicate` (~`:389`) does.

**Acceptance:** the reproduction above yields `relic power/toughness = 5/5`. Add it as a
permanent test in `RustedRelicTest` (Relic + Armor + Bears, no third printed artifact).

Deviation: metalcraft calls an extracted `StaticEffectSupport.isArtifactForStaticFilter` rather
than going through `matchesStaticFilter` as the second bullet reads. The funnel's CR 613.6
verdict memo is keyed by filter instance and `PermanentIsArtifactPredicate` is a component-less
record, so all instances compare equal — an unrelated caller entering the funnel would collect a
verdict memoized for some other ability. The artifact and historic leaves call the same method.

Residual approximation carried into Stage B/C: the supertype leaf passes a `null` GameData to
`hasEffectiveSupertype`, so global supertype removals (`Melting`) stay invisible — that scan
would re-enter static-bonus assembly. Per-permanent grants and removals ARE honored.

## Stage B — P/T leaves read layered numbers

Medium. The plan's headline — "anthems are invisible to every static filter" — turned out to be
only half-closable here; see the blocker at the end of this section.

**Done 2026-07-30** — step 16 in `LAYER_SYSTEM.md`.

- [x] P/T predicate leaves (~`StaticEffectSupport.java:503-508`) used
      `Permanent.getEffectivePower()/getEffectiveToughness()`, whose own javadoc calls them "the
      legacy pre-switch fallback". Now `GameQueryService.powerForStaticFilter` /
      `toughnessForStaticFilter`.
- [x] `AmountEvaluationService.totalToughnessOfControlledCreatures` (~`:589`) — same accessor.
      **No card produces `TotalToughnessOfControlledCreatures` today**, so this one is a
      correctness fix with no current card behind it.
- [x] `AmountEvaluationService.countPermanents` (~`:346`) — source card id and source controller
      id are now supplied in static evaluation too (`FilterContext.empty()` in place of a null
      context), so source-relative predicates can match. **Layered types are deliberately still
      NOT read here** — that needs the type leaves of `PredicateEvaluationService` to answer from
      `CharacteristicState` the way Stage A did for `StaticEffectSupport`, which is Stage C's
      strangler work. P/T within the count is fixed, via the leaves above.
- [x] `AmountEvaluationService.countBasicLandTypesAmongControlledLands` (~`:388`) — Domain now
      honors CR 305.7 overrides in both branches, through
      `GameQueryService.basicLandTypesForStaticEvaluation`.
- [x] Same leaves in `PredicateEvaluationService` (~`:436-492`) — five null-`GameData` branches,
      not the four the old line reference implied.

### The accessor: three answers, not one

`GameQueryService.powerForStaticFilter` answers by what is reachable without re-entering the
assembly forever. Layer 7c lives in `assembleStaticBonusInternal`, not in the pass, so there is
no in-flight 7c number to read the way Stage A read `CharacteristicState` for types.

1. **Fully layered** — board finished, and the permanent is not itself being assembled.
2. **Preliminary** — the permanent IS the one being assembled, which is the dominant shape (a
   filter on P/T describes the permanent whose bonus is being built). A second assembly answers
   it with that permanent added to `PT_LEAF_FROM_BOARD`, so the leaves inside it drop to (3) and
   the recursion closes after one level. Only its power and toughness are trustworthy — every
   other field was decided with degraded leaves — hence the separate `Pass.preliminaryBonusMemo`.
3. **Board-derived** — the 7b winner from `basePt7b`, the permanent's own modifiers and counters,
   and the 7d parity from `switchedPt7d`. Also what a leaf reached mid-pass gets.

Two `ThreadLocal<Set<UUID>>`s in `GameQueryService` drive this: `ASSEMBLY_IN_PROGRESS` (recorded
by every assembly, never blocks re-entry) and `PT_LEAF_FROM_BOARD`. `LayerSystemService` grew a
static `activePass()` ambient hook and `Pass.gameData()`, matching the existing `activeStateFor`
pattern — the leaves are reached through handler chains that no longer carry the game state.

Still unreachable, and circular in the rules rather than only in this implementation: a layer-7c
boost whose own filter reads the power it contributes to ("creatures with power 2 or less get
+1/+1"). It lands on (3).

### Blocker found: layer-6 filters still cannot see layer 7

`ConditionalEffect`-free, pass-managed L4/L5/L6 instances evaluate their filters **during**
`applyLayer5And6`, where the board is not ready and `basePt7b`/`switchedPt7d` do not exist yet —
so a P/T-filtered layer-6 grant reads printed numbers no matter what the accessor does. The
assembly cannot fix it either: for a managed instance the handler re-runs with
`setLayeredOutputsSuppressed(true)`, so the pass's mid-pass decision is the one that counts.

`Tetsuko Umezawa, Fugitive` (DOM 69, "Creatures you control with power or toughness 1 or less
can't be blocked") is the reproduction: a `Glorious Anthem` lifting a 1/1 to 2/2 should take the
ability away, and does not. The engine's own doctrine outside the pass already disagrees with
itself here — `PredicateEvaluationService`'s non-null-`GameData` branch decides the same
applicability question from the fully layered P/T.

This is the "layer N never reads layer >N" invariant (`LAYER_SYSTEM.md` §5.4) meeting a case the
CR does not actually make circular: nothing about the P/T of these creatures depends on the
layer-6 grant, so layer 7 could be resolved for a permanent before the layer-6 decision is taken.
Closing it means making per-permanent layer-7 numbers computable ahead of L6 — a restructure that
belongs with C1, not a leaf change.

- [ ] Carried to Stage C: P/T-filtered layer-4/5/6 grants (`Tetsuko Umezawa, Fugitive`). Write
      the two regression tests to the correct answer when C1 lands — an anthem lifting a 1/1 to
      2/2 removes the ability, an opponent's `Cumber Stone` dropping a 2/2 to 1/2 grants it.

Not throwaway work despite Stage C deleting the method: making layered P/T readable mid-pass is
a capability Stage C requires.

## Stage C — conditional wrappers into the pass, then delete the guard

Large, multi-session. **Gated on the fingerprint decision below — do not start code first.**

### C0 — the fingerprint decision (design only, no code)

Conditional wrappers are deliberately excluded from `collectInstances`
(~`LayerSystemService.java:756,775`, via `isConditionalWrapper` ~`:798`) because their conditions
read state that `computeBoardFingerprint` (~`:413-452`) does not hash. The fingerprint covers
permanents, graveyards, hand sizes, library top, floating effects and exiles — but **not life
totals, active player, or poison**. Caching a board containing such a conditional goes stale.

Options:

1. Extend the fingerprint to the volatile inputs that in-board conditions actually read.
   Correct, but widens a hot-path hash and risks over-invalidation.
2. Mark a board non-cacheable when it contains such a conditional. Simpler and always correct,
   but loses board caching for any game containing one.
3. Hybrid: classify conditions as stable vs. volatile; admit only stable ones to the board.

Decision owner: the user. This is a correctness-vs-performance tradeoff on a hot path.

- [ ] C0 decided and recorded here.

### C1 — admit layer-4-producing conditionals to the pass

- [ ] Classify `ConditionalEffect` by its wrapped effect's layer, so
      `ConditionalEffect(Metalcraft, AnimatePermanentsEffect)` is collected as an L4 instance
      with a timestamp.
- [ ] Evaluate its condition against the in-flight `CharacteristicState`.
- [ ] Let the existing `orderByDependency` (~`:876-936`) handle Armor → Relic. **No new ordering
      logic is needed** — that machinery already implements CR 613.8 with trial-application
      fingerprinting, Kahn topological sort, and a dependency-loop fallback to timestamp order.
- [ ] Delete the stale comment at ~`LayerSystemService.java:1421-1423`, which currently
      describes this bug as if it were a constraint.

### C2 — strangler-fig the leaves

`matchesStaticFilter` handles **22 of 65** `PermanentPredicate` implementations and throws on the
rest; **17 handlers** call it, plus both evaluation services. Migrate **one predicate family per
slice**, keeping the method as the funnel until it is empty, then delete it. Suite green at every
step.

Suggested family order: type leaves (done in A) → P/T leaves (done in B) → composites
(`Not`/`AllOf`/`AnyOf`, which propagate and *invert* the approximation) → the four context-needing
predicates currently handled only by the private overload → the ~40 that throw.

- [ ] Composites
- [ ] Context-needing predicates (`IsEnchanted`, `ControllerControlsPermanent`,
      `HasGreatestManaValueAmongAllCreatures`, `HasSourceChosenSubtype`)
- [ ] Remaining throwing predicates
- [ ] Delete `matchesStaticFilter`
- [ ] Delete `ConditionContext.staticEvaluation` / `AmountContext.staticEvaluation`
- [ ] Revert the `hasSelfBecomeCreatureEffect` boolean overload (its reason for existing is gone)

### Known secondary approximations to fold in

- `isEffectivelyCreature`'s 2-arg overload passes `gameData == null`, dropping animate-lands and
  self-become-creature entirely. Callers: `BoostByOtherCreaturesWithSameNameSelfEffectHandler`,
  `BoostBySharedCreatureTypeEffectHandler`.
- `hasAnimateArtifactEffect` / `matchesAnimateLand` scan printed `EffectSlot.STATIC` only, and
  compare against printed subtypes.
- Emblem handling in `GameQueryService` (~`:1858-1880`) passes null `GameData` for the same
  reason, with mixed fidelity between the boost and its filter.
- `applySelfOnlyConditionalStaticEffect` (~`StaticEffectSupport.java:253`) drops non-`Fixed`
  animation P/T to 0.

## Verification assets

The primary regression spec already exists and outranks anything written for this migration:

- `SevenLayerTest` (`magical-vibes-application/.../layers/SevenLayerTest.java`, 100 tests) —
  must stay 100/100 green; never weaken an expectation.
- `LayerDependencyTest` — the CR 613.8 spec, directly relevant to C1.
- `LayerClassifierTest` — enforces that every static effect type is registered in
  `LayerClassifier`. C1 changes classification, so expect to extend this.

Build these in addition, before Stage A, and keep them green throughout:

- [x] The `Rusted Relic` + `Silverskin Armor` reproduction as a permanent test.
      `RustedRelicTest.grantedArtifactTypeCountsTowardMetalcraft` (2026-07-30).
- [ ] Characterization tests for the 17 handlers calling `matchesStaticFilter`. Pin behavior
      derived from the CR, **not** from current output — some current output is wrong (anthems
      invisible to static filters is a bug, not a baseline). Where current behavior is knowingly
      wrong, write the test to the correct answer and let it fail until the stage that fixes it.
      Those failures are the progress bar.
