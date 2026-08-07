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
  2-arg form delegated with `false`.
- Three static-pass callers passed `true`: `StaticEffectSupport.isEffectivelyCreature`,
  `AllLandsAreCreaturesEffectHandler`, `LayerSystemService` layer-7b.
- Two regression tests in `RustedRelicTest` (anthem + metalcraft, anthem without metalcraft).

It was the prerequisite that routed static-pass callers to the recursion-safe branch, which
Stage A then made correct. **Removed in C2 slice 3** together with the flags themselves: the
recursion-safe branch is now selected by `GameQueryService.isStaticEvaluationActive()`, which
observes the same fact the callers used to declare, so those three callers no longer have to.
The `RustedRelicTest` regression tests stay.

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
C2 slice 2 generalized that method into `PredicateEvaluationService.matchesStaticLeaf` and deleted
`isArtifactForStaticFilter`.

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

- [x] Carried to Stage C, **closed 2026-07-31 — but not the way this section predicted.** See
      "The blocker was a modeling bug, not a layer-system gap" under C1 below. The two regression
      tests exist and pass: `TetsukoUmezawaFugitiveTest.anthemRemovesEvasion` and
      `.opponentDebuffConfersEvasion`. No pass restructure was needed, and none is warranted.

Not throwaway work despite Stage C deleting the method: making layered P/T readable mid-pass is
a capability Stage C requires.

## Stage C — conditional wrappers into the pass, then delete the guard

Large, multi-session. C0 and C1 landed 2026-07-31; C2 is the remaining bulk.

### C0 — the fingerprint decision

Conditional wrappers were excluded from `collectInstances` (`LayerSystemService.java:772`, skips
at `:784`/`:803`, via `isConditionalWrapper` `:826`) because their conditions read state that
`computeBoardFingerprint` (`:441`) does not hash. The fingerprint covers permanents, graveyards,
hand sizes, library top, floating effects and exiles — but **not life totals, active player, or
poison**. Caching a board containing such a conditional goes stale.

Options were: (1) extend the fingerprint to the volatile inputs; (2) mark a board non-cacheable
when it contains such a conditional; (3) hybrid — classify conditions as stable vs. volatile and
admit only the stable ones.

**Decided 2026-07-31: option 3, hybrid, default-deny.** `ConditionBoardStability` (engine
`service/effect/`) answers whether a condition reads only fingerprinted state; an unlisted
condition keeps the pre-existing legacy-additive behavior, so introducing a condition can never
silently make the board cache unsound — the cost of forgetting one is a missed optimization, not
a wrong answer.

Rationale: the survey below found that the condition motivating C1 (`Metalcraft`) reads only
battlefield permanents, which are already hashed, so that case lands with zero fingerprint change
and zero cache loss. Option 1 would have widened a hot-path hash for every game and required
rewriting `LayeredBoardCacheTest.conditionalStaticGrantsToggleWithoutInvalidatingTheBoard`
(`:108`), which pins the opposite. Option 2 would have cost a whole game its board memoization
(~26-45× on layered-query throughput, `LAYER_SYSTEM.md` step 13) for one Rusted Relic.

- [x] C0 decided and recorded here.

#### Survey: conditions in STATIC slots (2026-07-31)

149 card classes construct a conditional wrapper in an `EffectSlot.STATIC` slot, using **20 of
the 102** `Condition` variants. Of those 20, only these read state the fingerprint does **not**
cover — everything else is admissible as far as staleness goes:

| Uncovered read | Conditions | Cards |
|---|---|---|
| `playerLifeTotals` | `ControllerLifeAtLeast`, `ControllerLifeAtMost` | 6 |
| `activePlayerId` | `ControllerTurn`, `NotControllerTurn` | 5 |
| `Permanent.isAttacking()` | `SourceIsAttacking` | 3 |
| `hasGainedLifeThisTurn` | `GainedLifeThisTurn` | 3 |
| `creatureDeathCountThisTurn` | `Morbid` | 1 |
| `playerPoisonCounters` | `OpponentPoisoned` | 1 |
| `playerDecks` **size** | `AnyLibraryAtMost` | 1 |
| `isBlocking()` / `getBlockingTargetIds()` | `BlockedByMinCreatures` | 1 |
| `playersWhoseCardsLeftGraveyardThisTurn` | `CardsLeftGraveyardThisTurn` | 1 |

`ConditionBoardStability` currently lists only `Metalcraft` plus the four combinators
(`NotCondition`, `AllConditions`, `AllOf`, `AnyOf`, which recurse into their operands). The rest
of the stable set above is verified but unlisted, because admission is gated on layer 4 and
nothing else can reach it yet — list them when C2 widens admission past layer 4.

### C1 — admit layer-4-producing conditionals to the pass

**Done 2026-07-31** — step 17 in `LAYER_SYSTEM.md`.

- [x] Classify `ConditionalEffect` by its wrapped effect's layer. `LayerClassifier` already
      delegated wrappers to `wrapped()`; the real blocker was that **`AnimatePermanentsEffect` had
      no classification at all**, so `ConditionalEffect(Metalcraft, AnimatePermanentsEffect)` was
      excluded twice over — once as a wrapper, once as an unclassified effect. Now registered as
      L4 only; base P/T, colour and keywords stay with the legacy self-handler in the accumulator
      pass (the `AllLandsAreCreaturesEffect` / `EnchantedPermanentBecomesCreatureEffect` split).
- [x] Evaluate its condition against the in-flight `CharacteristicState`. `applyL4Instance` was
      split into a guard plus `applyL4Effect(instance, effect, …)`, so an admitted wrapper hands
      its wrapped effect straight back in, keeping the instance's source, timestamp and CR 613.6
      bookkeeping. Metalcraft's static branch already read the in-flight state through
      `StaticEffectSupport.isArtifactForStaticFilter` (Stage A; now `matchesStaticLeaf`).
- [x] Let the existing `orderByDependency` (`:904`) handle Armor → Relic. **No new ordering logic
      was needed** — the trial-application fingerprints pick it up, because the animation's
      recorded operations appear only in the world where the Armor applied first.
- [x] Delete the stale comment. There were two, not one: `LayerSystemService.java:1250-1251`
      ("wrappers never wrap layer-4 effects today") and the `isCreatureForL4` javadoc, plus the
      now-inaccurate notes on `classifyOrNull` and `computeBoardFingerprint`.

**Scope actually admitted:** `admitsConditionalWrapper` requires layer 4 **and** a stable
condition, so of the 149 STATIC-slot conditional cards exactly **one** enters the pass —
`Rusted Relic`. `Warden of the Wall` is the only other conditional self-animation, but its
`NotControllerTurn` reads `activePlayerId`, so it stays legacy-only with today's behavior.

**What changed observably:** the animated relic's CREATURE type and GOLEM subtype now sit in its
`CharacteristicState`, so the recursion-safe static-filter leaves see them. `Wing Splicer`'s
"Golem creatures you control have flying" reaches it; previously the subtype existed only in the
accumulator, which no filter reads. The P/T path is unchanged — Stage A already fixed that.

**Residual approximation:** the pass evaluates the condition mid-layer-4 while the assembly
re-evaluates it against the finished states. They agree because type changes are layer-4-only and
dependency ordering puts the conditional after whatever it reads — except inside a CR 613.8c
dependency loop, where timestamp order applies and the two evaluations can disagree. No current
card pair forms such a loop.

### The blocker was a modeling bug, not a layer-system gap (2026-07-31)

Stage B's closing section diagnosed `Tetsuko Umezawa, Fugitive` as "layer-6 filters still cannot
see layer 7" and prescribed making per-permanent layer-7 numbers computable ahead of L6 — a pass
restructure. **That diagnosis was wrong, and the restructure would have been the wrong fix.**

Verified against the CR effective June 19, 2026 via the `rules` MCP:

| Rule | Content |
|---|---|
| 613.11 | Continuous effects that affect the rules of the game rather than objects are applied **after all other continuous effects** |
| 509.1a | Blocking restrictions are checked at declare blockers; an evasion ability is "a static ability **an attacking creature has**" |

Tetsuko reads "Creatures you control with power or toughness 1 or less **can't be blocked**" — it
states a blocking restriction and grants the creatures no ability (contrast "…gain 'this creature
can't be blocked'"). That makes it a CR 613.11 rules-modifying effect, applied *after* the layer
system, so its matching set is decided from fully layered P/T by rule. There is no
layer-6-reads-layer-7 circularity to engineer around; the effect simply did not belong in layer 6.
The official Gatherer ruling agrees on the dynamics ("once a creature you control has been
blocked, changing its power to 1 or less won't cause it to become unblocked" — i.e. before blockers
are declared, changing power *does* change qualification).

The engine already models every other blocking/attacking/untap restriction this way: ~50 such
effect types carry **no** `LayerClassifier` entry and no static handler, and are read straight off
`EffectSlot.STATIC` by `BlockLegalityService` / `AttackLegalityService` / `UntapStepService` using
the fully layered `matchesPermanentPredicate`. A survey of the pool found **17 P/T-filtered
STATIC-slot effects across 16 cards, of which Tetsuko was the only layer-classified one** — it
reached layer 6 solely because it was written as `GrantEffectEffect(CantBeBlockedEffect, …)`.

**Fix:** new `ControlledCreaturesMatchingCantBeBlockedEffect(PermanentPredicate filter)`
(magical-vibes-domain `model/effect/`), consumed by a new branch in
`GameQueryService.hasCantBeBlocked`, mirroring `ControlledCreaturesCantAttackUnlessPredicateEffect`
/ `AttackLegalityService`. Tetsuko now uses it. Both regression tests were confirmed to fail under
the old `GrantEffectEffect` modeling and pass under the new one.

**Consequence for the plan:** there is **no remaining card** with a genuine P/T-filtered layer-4/5/6
*ability* grant, so the "make layer 7 readable ahead of L6" capability has zero cards behind it and
is not scheduled. Should such a card ever appear, note that the CR does not clearly sanction a
layer-6 grant reading layer-7 P/T either — the invariant "layer N never reads layer >N"
(`LAYER_SYSTEM.md` §5.4) would be correct there, and the card would need a rules verdict first.

**Known cosmetic regression:** qualifying creatures no longer list "Can't be blocked" among their
granted abilities in the client (`GrantedAbilityViewFactory`), because it is no longer a granted
ability. This matches how the other 16 P/T-filtered restriction cards already behave.

### C2 — strangler-fig the leaves

`PredicateEvaluationService.matchesStaticFilter` handles **29 of the 68** `PermanentPredicate`
implementations and throws on the rest. **14 handlers** reach it, and of the two evaluation services
only `ConditionEvaluationService` still does — Stage B moved `AmountEvaluationService` onto
`FilterContext.empty()` into `PredicateEvaluationService`. Migrate **one predicate family per
slice**. Suite green at every step.

Suggested family order: type leaves (done in A) → P/T leaves (done in B) → composites (done in C2
slice 1) → the four context-needing predicates (done in C2 slice 2) → the ~40 that throw.

- [x] Composites — **done 2026-07-31**, see "Slice 1" below.
- [x] Context-needing predicates (`IsEnchanted`, `ControllerControlsPermanent`,
      `HasGreatestManaValueAmongAllCreatures`, `HasSourceChosenSubtype`) — **done 2026-07-31**,
      see "Slice 2" below.
- [x] Delete `StaticEffectSupport.matchesStaticFilter` — done in slice 2; what is left on
      `StaticEffectSupport` is a two-line scope adapter of the same name, not an evaluator.
- [ ] Remaining throwing predicates
- [x] Delete `ConditionContext.staticEvaluation` / `AmountContext.staticEvaluation` — **done
      2026-07-31**, see "Slice 3" below.
- [x] Revert the `hasSelfBecomeCreatureEffect` boolean overload (its reason for existing is gone)
      — done in slice 3.

**Standing decision — unsupported predicates throw, they do not answer `false`.** Routing the
remaining predicates through `PredicateEvaluationService` with no `GameData` would make several of
them (`ControlledBySourceController`, `IsBlocked`, and friends) return `false` rather than throw:
a wrong answer dressed as a legitimate one, and invisible in a card test that only asserts a bonus
is absent. Keep the `IllegalArgumentException` default arm. A predicate leaves the throwing set only
when someone works out what its recursion-safe answer actually is and implements it — never by
falling through.

**Next slice (4).** Only the throwing predicates are left, and nothing in the codebase needs them
yet — anything that reached one would be failing loudly today. Take one when a card demands it,
and only with a worked-out recursion-safe answer per the standing decision above.

#### Slice 1: one evaluator for the leaves (2026-07-31)

The funnel moved to `PredicateEvaluationService.matchesStaticFilter`, leaving
`StaticEffectSupport.matchesStaticFilter` a one-line delegate kept only because the private
context-aware overload still had to intercept the four context-needing predicates before it (slice 2
removed that overload).
Composites and the CR 613.6 layer-4 verdict memo moved with the funnel, so nested sub-predicates
keep consulting the memo exactly as before.

Why the funnel could not simply *become* the state overload: `matchesL4Filter` writes into
`board.l4FilterVerdicts()` on every call, so a memo read inside the overload would freeze the first
verdict and defeat `orderByDependency`'s trial applications. The memo read belongs in the funnel,
which layer 4 does not go through.

The Stage A/B corrections had all landed in the funnel, not in `PredicateEvaluationService`, so its
recursion-safe path had to catch up. Its `CharacteristicState` overload only answered subtypes and
composites; the creature, artifact, land, enchantment, planeswalker, keyword, supertype and historic
leaves now answer from the state too. **That also fixes layer 4 itself** — `matchesL4Filter`
evaluates layer-4 scope filters through that overload, so before this a layer-4 scope filter on
`PermanentIsArtifactPredicate` read the printed type and could not see a grant an earlier-applied
layer-4 effect had just made.

Divergences resolved rather than preserved, each toward the more correct answer:

- `PermanentIsHistoricPredicate` now reads the effective LEGENDARY supertype and granted SAGA in
  **both** paths. The layered path previously read printed LEGENDARY and printed/transient SAGA
  while answering the artifact third of the same question from the layered type.
- `PermanentHasSubtypePredicate` outside a pass now honors `getTransientCreatureTypeOverride()`
  (Boldwyr Intimidator). The funnel's copy never did.
- `GameQueryService.NON_CREATURE_SUBTYPES` was missing `KOTH`, so it disagreed with
  `StaticEffectSupport`'s copy about whether "loses all creature types" strips a planeswalker's
  KOTH subtype. KOTH added; the two sets are now identical.

Residual: `StaticEffectSupport.isCreatureSubtype` still exists as a second definition of the same
set, because `LayerSystemService` uses it as a method reference in three `removeSubtypesIf` calls.
Fold it into `GameQueryService` when the funnel goes.

#### Slice 2: the four board-reading predicates (2026-07-31)

`PredicateEvaluationService.matchesStaticFilter` now takes a `FilterContext` and answers
`IsEnchanted`, `ControllerControlsPermanent`, `HasGreatestManaValueAmongAllCreatures` and
`HasSourceChosenSubtype` itself, so `StaticEffectSupport`'s private context-aware overload and its
two recursion-safe helpers are gone. What remains on `StaticEffectSupport` is
`matchesStaticFilter(StaticEffectContext, Permanent, PermanentPredicate)`, a two-line adapter that
builds the `FilterContext` from the static-effect context; the target stays an explicit parameter
because several handlers filter permanents other than the one being assembled.

No recursion-safe marker on `FilterContext` was needed, contrary to the earlier plan. The context is
consulted only by those four cases; every characteristic leaf goes through `matchesStaticLeaf`,
which passes no context down, so a `GameData` being present cannot pull a leaf onto the layered
path. That also makes the invariant checkable by reading the funnel alone.

`matchesStaticLeaf` is slice 1's `recursionSafeLeaf`, now public and named for the second thing it
guarantees: it bypasses the CR 613.6 verdict memo. The memo is keyed by filter *instance* and most
leaf predicates are component-less records, so a locally constructed instance compares equal to some
unrelated ability's filter and would collect that ability's verdict. Only a filter that really is
the ability's own may read it. Everything that constructs its own predicate now routes through
`matchesStaticLeaf`: the land check in `matchesLandScope`, the creature checks inside
`hasGreatestManaValueAmongAllCreatures`, the subtype check behind `HasSourceChosenSubtype`, the two
`BoostCreaturesOfChosenSubtype` handlers and
`GrantProtectionFromChosenTypeToOwnCreaturesSelfEffectHandler`. This subsumes
`StaticEffectSupport.isArtifactForStaticFilter`, which existed only to dodge the same memo and is
deleted; metalcraft calls `matchesStaticLeaf` directly.

`ConditionEvaluationService` no longer depends on `StaticEffectSupport` at all — its three uses
(`isCreatureForCondition`, `matchesPermanent`, `isMetalcraftMet`) go straight to the evaluator. Its
static branch now also builds the same source card/controller `FilterContext` the non-static branch
builds, so a source-relative predicate inside a static condition is no longer silently sourceless.

#### Slice 3: recursion-safety became ambient, and the flags are gone (2026-07-31)

`ConditionContext.staticEvaluation` and `AmountContext.staticEvaluation` are deleted, along with
their seven branches (four in `ConditionEvaluationService`, three in `AmountEvaluationService`) and
the `hasSelfBecomeCreatureEffect(GameData, Permanent, boolean)` overload. The branches now ask
`GameQueryService.isStaticEvaluationActive()`, which is true when either

- `ASSEMBLY_IN_PROGRESS` is non-empty — this thread is inside `assembleStaticBonusInternal`; or
- `LayerSystemService.buildingBoard()` — some pass on this thread has not finished its board.

Both facts were already recorded by the machinery that creates the situation (Stage B added the
first, the pass has always had the second), so the boundary is observed instead of declared. Why
that is better than the flag and not merely tidier: the flag was a claim each caller made about
where it was, and a caller reached through a chain it did not write got it wrong. That is exactly
how the original stack overflow happened — `isCreature` → `hasSelfBecomeCreatureEffect` →
metalcraft took the layered branch while an assembly was running, because the layered query has no
way to know its caller is a static handler. The boolean overload was a manual patch for the three
call chains someone had found. Ambient detection covers the ones nobody has found.

`buildingBoard()` walks the whole parent chain rather than reading `activePass().isBoardReady()`:
a nested pass opened mid-build finishes its own board while the outer build is still in flight, so
the innermost pass alone would report ready and re-open the hole.

Two contexts widened rather than narrowed, both deliberately:

- `LayerSystemService.buildingBoard()` makes *everything* reached during a board build
  recursion-safe, not just the handlers that remembered to say so. This is also the CR 613 reading
  order — mid-build there are no layer-7 numbers and only the layers applied so far.
- `AttackLegalityService`'s `CreaturesWithPowerGreaterThanAmountCantAttackEffect` branch used
  `AmountContext.forStaticEffect` from outside any assembly, purely to carry the source and
  controller, and so ran on the degraded matchers for no reason. It now evaluates fully layered.
  Inert in practice: `Ensnaring Bridge` is the only card with the effect and its amount is
  `CardsInHand`, which reads no permanent.

`forStaticEffect` survives on both context types — identical to `forPermanent` on
`ConditionContext` now — because it still says at the call site which kind of evaluation is being
set up. Neither selects anything any more.

**Verification:** `layers.*` (SevenLayerTest 100/100, LayerDependencyTest, LayeredBoardCacheTest,
LayerClassifierTest, ModifierExplanationTest, FloatingEffectLifecycleTest), GameQueryServiceTest,
PredicateEvaluationServiceTest, the staticfx suite, RustedRelicTest, WardenOfTheWallTest,
TetsukoUmezawaFugitiveTest, EnsnaringBridgeTest, the metalcraft batch (ArdentRecruit,
AuriokEdgewright, EtchedChampion, IndomitableArchangel, JorKadeenThePrevailer, PuresteelPaladin,
SilverskinArmor, SpireSerpent, CarapaceForger, ChromeSteed, MoxOpal), the animation batch
(MarchOfTheMachines, JadeStatue, Mutavault, InkmothNexus), the Splicer/Golem batch, and the
Stage A/B divergence and Domain batches. All green.

### Known secondary approximations to fold in

- `isEffectivelyCreature`'s 2-arg overload passes `gameData == null`, dropping animate-lands and
  self-become-creature entirely. Callers: `BoostByOtherCreaturesWithSameNameSelfEffectHandler`,
  `BoostBySharedCreatureTypeEffectHandler`.
- `hasAnimateArtifactEffect` / `matchesAnimateLand` scan printed `EffectSlot.STATIC` only, and
  compare against printed subtypes.
- Emblem handling in `GameQueryService` (~`:2106-2144`) passes null `GameData` for the same
  reason, with mixed fidelity between the boost and its filter.
- `applySelfOnlyConditionalStaticEffect` (~`StaticEffectSupport.java:235`) drops non-`Fixed`
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
- [ ] Characterization tests for the 14 handlers calling `matchesStaticFilter`. Pin behavior
      derived from the CR, **not** from current output — some current output is wrong (anthems
      invisible to static filters is a bug, not a baseline). Where current behavior is knowingly
      wrong, write the test to the correct answer and let it fail until the stage that fixes it.
      Those failures are the progress bar.
