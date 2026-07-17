# Composite-effect fold program

Composite `FooAndBar`-style effect records are being deleted by decomposing them into the existing
primitive effects, composed as multiple `addEffect(...)` calls on the card (the `ActOfTreason` /
`DrainLife` pattern sanctioned by CLAUDE.md). Progress log is below; each session runs with cleared
context, so record enough here to resume cold.

---

## Session 1 — DealDamageToAnyTargetAndGainLifeEffect

### PART A — engine prep: defer the player-loss SBA to end of stack-entry resolution

Problem: a spell whose effect list is "deal N damage to yourself" then "gain N life" as two separate
effects would have the damage handler's inline `checkWinCondition` declare the controller dead at 0
life *between* the two effects. Per CR 704.3 / 104.3b, state-based actions (loss from life ≤ 0) are
only checked when a player would receive priority — i.e. after the whole spell finishes resolving.
The old single composite handler got this right by accident (one check at the end); multi-effect
entries did not.

Changes:
- **`GameData`** (`magical-vibes-domain/.../model/GameData.java`): added two transient fields —
  `boolean deferPlayerLossCheck` (read by `GameOutcomeService`) and `int effectResolutionDepth`
  (reentrancy depth for the resolve loop). Both are copied in the AI deep-copy alongside the other
  per-resolution fields (`rerunCurrentEffectAfterInteraction` etc.).
- **`GameOutcomeService.checkWinCondition`** (`magical-vibes-engine/.../service/GameOutcomeService.java`):
  returns `false` immediately while `deferPlayerLossCheck` is set. Existing inline
  `checkWinCondition` calls in handlers (e.g. `DealDamageToAnyTargetEffectHandler`) are left in place
  — they become harmless no-ops mid-entry and still fire from combat and other non-resolution paths.
- **`EffectResolutionService`** (`magical-vibes-engine/.../service/effect/EffectResolutionService.java`):
  now injects `GameOutcomeService`. `resolveEffectsFrom` increments `effectResolutionDepth` and sets
  `deferPlayerLossCheck = true`, runs the effect loop (extracted to `resolveEffectsLoop`), and in a
  `finally` decrements the depth; only when unwinding the **outermost** frame (`depth == 0`) **and**
  the resolution is not paused for input (`pendingEffectResolutionEntry == null`) does it clear the
  flag and call `checkWinCondition` once. This chokepoint covers both normal completion and the
  async-resume drain path (every resume goes through `resolveEffectsFrom`). The depth counter is
  needed because some handlers resolve a nested effect list synchronously (Kinship
  `KinshipHandler`, counter riders `CounterSupport`); a nested completion must NOT re-enable the
  loss check for the enclosing entry.

Tests: added a `PlayerLossDeferral` nested class to `EffectResolutionServiceTest` (3 tests: suppress
mid-resolution + fire once; keep suppression across an async pause and finalize on resume; nested
sub-resolution does not finalize the outer). Updated that test's constructor call (+ new
`GameOutcomeService` mock). **PASS.**

Traps hit / notes:
- `PendingInteraction` is a sealed interface → not Mockito-mockable; the pause test triggers the
  async pause via a queued `PendingMayAbility` instead.
- No new Spring cycle: `GameOutcomeService` was already a transitive dependency of
  `EffectResolutionService` via the handler registry.
- `GameDataDeepCopyTest` is hand-written (not reflective), so the new fields need no registration
  there; they are covered by the explicit deep-copy field list.

### PART B — fold DealDamageToAnyTargetAndGainLifeEffect

Deleted:
- `magical-vibes-domain/.../model/effect/DealDamageToAnyTargetAndGainLifeEffect.java` (record)
- `magical-vibes-engine/.../service/effect/normalfx/DealDamageToAnyTargetAndGainLifeEffectHandler.java`
- `magical-vibes-application/.../service/effect/normalfx/DealDamageToAnyTargetAndGainLifeEffectHandlerTest.java`
  (its two scenarios — damage-to-player + life, damage-to-creature marked + life — are fully covered
  behaviorally by the three card tests).

Cards edited (Edit tool, no sed) — replaced the composite with two primitives in oracle order
(damage, then fixed life). All are "deal 3, gain a **fixed** 3" (independent of damage dealt), so the
`int` sugar ctors apply:
- `cards/e/EssenceDrain.java` — `DealDamageToAnyTargetEffect(3)` + `GainLifeEffect(3)` (SPELL)
- `cards/d/DarkNourishment.java` — same (SPELL)
- `cards/a/AjaniVengeant.java` — `List.of(DealDamageToAnyTargetEffect(3), GainLifeEffect(3))` for the
  −2 loyalty ability (decomposition works in ability slots too)

Rules verification: the decomposed spell has a single target (the any-target from the damage effect;
`GainLifeEffect` declares `TargetSpec.NONE`), so if that target becomes illegal the whole spell
fizzles and no life is gained — identical to the composite's documented behavior. The fixed life is
gained whenever the spell resolves, independent of damage dealt/prevented. Confirmed against the CR
and the existing `DrainLife` precedent (damage + `GainLifeEffect` on one slot, no explicit
`target()`).

Trap checklist run:
- Whole-repo grep for `DealDamageToAnyTargetAndGainLifeEffect`: no remaining production/test `.java`
  references (only historical mentions in `refactor-docs/` progress logs and this file).
- No `@ValidatesTarget` registration existed for the record (`DamageTargetValidators` was already
  clean; the `PROGRESS.md` note naming it was stale/historical). The any-target land-rejection
  validation now flows through `DealDamageToAnyTargetEffect`'s validator, exercised by
  `DarkNourishmentTest.cannotTargetLand`.
- No `instanceof` sites in `magical-vibes-engine` / `magical-vibes-ai` (SpellEvaluator, HardAi,
  EasyAiDecisionEngine, AiTargetSelector, RaceEvaluator) referenced the record.
- `model/effect/DamageDealingEffect.java` javadoc did not name the record — no change needed.
- agent-docs updated to show the composed pattern: `EFFECTS_INDEX.md`, `EFFECTS_QUICK_REFERENCE.md`,
  `CARD_PATTERNS_LANDS_SPELLS.md`.

Tests added / updated:
- `EssenceDrainTest`: added `selfTargetAtThreeLifeSurvives` (pinning test — cast at self at 3 life:
  3 damage → 0, then +3 → 3, survives, game not FINISHED). Ran it **before** the decomposition
  against the composite → PASS (composite is rules-correct via its single end-of-handler check).
  Fizzle test already existed (`fizzlesWhenTargetCreatureRemoved`).
- Fizzle coverage already present in both `EssenceDrainTest` and `DarkNourishmentTest`.

Test classes run (all PASS):
- `service.effect.EffectResolutionServiceTest`
- `cards.e.EssenceDrainTest` (6)
- `cards.d.DarkNourishmentTest` (7)
- `cards.a.AjaniVengeantTest` (4)

Deferred / nothing blocked. Full suite not run (per CLAUDE.md — user runs it).

### Candidates for future sessions
The two damage+gain-life composites named here as candidates
(`DealXDamageToAnyTargetAndGainXLifeEffect`, `DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect`)
were folded in **Session 2** below. No other `*And*Effect` damage+lifegain composites remain.

---

## Session 2 — the two remaining damage+gain-life composites

Both folded cleanly using the Session-1 pattern (SBA player-loss deferral from Part A makes the
decomposed damage→lifegain pairs rules-safe). Each composite became two `addEffect(SLOT, ...)`
calls in damage-then-life oracle order, the damage half carrying the `DynamicAmount`.

### Record 1 — DealXDamageToAnyTargetAndGainXLifeEffect (X drain)

Decomposition (Consume Spirit, the only user): the exact shape Drain Life already ships as —
```java
addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue()));
addEffect(EffectSlot.SPELL, new GainLifeEffect(new XValue()));
```
`setXColorRestriction(ManaColor.BLACK)` kept (engine logic, not metadata). The `xValue` (mana paid
for X) is snapshotted onto the stack entry generically from the card's mana cost — **not** keyed on
any effect class (verified: `DrainLife`/`DevilsPlay`/`Disintegrate` already use
`DealDamageToAnyTargetEffect(new XValue())` as spells; there is no `setXValue` call anywhere, and
`ActivatedAbilityExecutionService`'s xValue snapshotting does not name these records), so the
decomposition needs no engine change.

Rules: Consume Spirit's oracle is "you gain X life" — life = X, **independent of damage
dealt/prevented** (confirmed on Gatherer: "the amount of life you gain … is equal to the number
chosen for X, not the amount of damage"). `GainLifeEffect(new XValue())` reproduces this exactly, so
Record 1 is oracle-correct (no discrepancy).

Deleted: the record, its handler
(`DealXDamageToAnyTargetAndGainXLifeEffectHandler`), and its handler unit test
(`DealXDamageToAnyTargetAndGainXLifeEffectHandlerTest` — its single scenario "deals X damage and
gains X life" is covered behaviorally by `ConsumeSpiritTest`).

AI: removed the `instanceof DealXDamageToAnyTargetAndGainXLifeEffect` special case + its import from
`SpellEvaluator.evaluateSingleEffect` (was ~line 582). The decomposed pair is scored by the two
existing survivor branches: `DamageDealingEffect` (X damage estimated via
`estimateDamageAmount` → `estimateMaxX`, **preserved exactly**) and `LifeGainEffect`. **Fidelity
note:** the special case added a life term `estimatedX * 0.5 * lifeGainMultiplier`; the
`LifeGainEffect` branch evaluates `XValue` under `forEstimation` (xValue=0) → 0, so the life term is
dropped. This is *intentional and consistent*: the already-decomposed Drain Life has no such life
term either, so deleting the case makes Consume Spirit's AI scoring match the established
decomposed-X-drain baseline rather than being an outlier. All calibrated AI tests stayed green
(see below), so no observable AI behavior changed.

### Record 2 — DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect (Corrupt)

Decomposition (Corrupt, the only user):
```java
addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(
        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.CONTROLLER)));
addEffect(EffectSlot.SPELL, new GainLifeEffect(
        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.CONTROLLER)));
```
Both `PermanentCount`s evaluate at resolution to the same value: nothing on the battlefield changes
between the two effects of one entry (the damage's creature deaths are deferred to the SBA check per
Session 1; Corrupt never damages a land, so the Swamp count is stable). Handler diffed against the
recipe first — it did `count = controlled Swamps; deal count; if gainLife && count>0 gain count` —
which is exactly the two-`PermanentCount` decomposition (the `count>0` guard is a no-op: gaining 0
life is a no-op).

Deleted: the record and its handler. **No handler unit test existed** for this record (nothing to
port).

**Oracle discrepancy (recorded, NOT fixed — behavior-preserving fold):** Corrupt's real oracle is
"You gain life equal to the damage **dealt this way**" (verified on Gatherer / Scryfall). Real MTG
ties the life to *actual damage dealt*, so if the damage is prevented/reduced (protection,
prevention shields, damage→0 replacement) you gain **less** than your Swamp count. The old composite
ignored prevention and gained the full count; this fold reproduces that (gains the full
`PermanentCount`). Correct behavior would gain life equal to the damage the `DealDamageToAnyTargetEffect`
*actually dealt* — implementable via an `EventValue`-style "damage dealt by the prior effect on this
entry" amount fed to `GainLifeEffect`, matching how Rite of Consumption-style "damage dealt" life
gain should work. **Candidate for a separate rules-fix session** (would also want a prevention test).
Note Consume Spirit does **not** share this bug — its oracle is "gain X life", not "damage dealt".

### Trap checklist (both records)
- Whole-repo grep after edits: no remaining `.java` references outside stale `build/` artifacts
  (regenerated on next build) and historical `refactor-docs/` logs. No live source/test hit.
- `@ValidatesTarget` validators: none registered for either record (`service/validate/` grep clean);
  the any-target land-rejection now flows through `DealDamageToAnyTargetEffect`'s own validator
  (exercised by `DrainLifeTest.castAtLandIsRejected` for the shared path).
- No `instanceof` sites in `magical-vibes-engine` or elsewhere in `magical-vibes-ai` (HardAi,
  EasyAiDecisionEngine, AiTargetSelector, RaceEvaluator) named either record — only the one
  `SpellEvaluator` case (Record 1).
- X-value trap: `ActivatedAbilityExecutionService` xValue/charge snapshotting does not name either
  record; `EffectResolution.java` has no reference (grep clean). No `hasManaSpentToCast*` helper
  names them.
- `model/effect/DamageDealingEffect.java` javadoc updated — dropped the "carry no amount component
  (`DealXDamageToAnyTargetAndGainXLifeEffect`)" clause (both such composites are now gone).
- agent-docs rewritten to the composed-primitives pattern: `EFFECTS_INDEX.md` (both rows →
  `_compose, no dedicated effect_` rows), `EFFECTS_QUICK_REFERENCE.md` (unified the
  damage+gain-life bullet across fixed / X / dynamic-count shapes), `CARD_PATTERNS_LANDS_SPELLS.md`
  (Corrupt row). `ORACLE_TEXT_EFFECT_MAP.md` needed no change (it already documented the composed
  "damage = subtype count + gain life" pattern via Tendrils of Corruption).
- `refactor-docs/EFFECT_COUPLING_MATRIX.md` still lists the deleted records — it is a
  script-generated baseline (`scripts/effect-coupling-audit.py`), refreshed separately, so left as-is
  per Session-1 precedent.

### Tests
- `ConsumeSpiritTest`: added `fizzlesWhenTargetCreatureRemoved` (target creature removed pre-resolution
  → spell fizzles, controller life unchanged, no gain). Already had normal player + creature damage
  with correct life, and `canCastWithXZero` (X=0 resolves cleanly, no damage/life). **8 tests, PASS.**
- `CorruptTest`: added `fizzlesWhenTargetCreatureRemoved` (2 Swamps controlled, target removed
  pre-resolution → no damage, no life gain). Already had explicit life-amount assertions (23/24/21)
  proving the second `PermanentCount` evaluates to the Swamp count, plus `countsSwampsAtResolution`
  (0 Swamps → 0 damage 0 life). **7 tests, PASS.**
- `DrainLifeTest` (shared X-drain decomposition sanity): **3 tests, PASS.**
- AI regression (module `magical-vibes-ai`, run directly — not via run-card-test.ps1): all green,
  proving compile after the SpellEvaluator edit and no calibrated flip —
  `SpellEvaluatorTest` (66), `HardAiDecisionEngineTest` (77), `MediumAiDecisionEngineTest` (26),
  `EasyAiDecisionEngineTest` (31), `AiDecisionEngineTest` (117); 0 failures/errors each.

### Notes
- Spotless not run: it is opt-in (`isEnforceCheck=false`) and only does `removeUnusedImports()`; the
  touched files introduce no unused imports, so it would be a no-op (and repo-wide/module-wide runs
  risk unrelated churn per prior sessions).
- Nothing deferred; neither record hit a stop-rule. Full suite not run (per CLAUDE.md — user runs it).

---

## Session 3 — countered-spell-destination variants → `CounterSpellEffect(destination)`

Unlike Sessions 1–2 (decompose-onto-card), this session **parameterizes the base effect** with an enum,
the same style as the other enum-collapses in the codebase. Two composites folded into `CounterSpellEffect`.

### Enum added
- `model/effect/CounteredSpellDestination { GRAVEYARD, EXILE, LIBRARY_TOP }`.
- `CounterSpellEffect` gained a `destination` field. Added a **no-arg sugar ctor** defaulting to
  `GRAVEYARD` so all ~50 existing `new CounterSpellEffect()` users (Counterspell, Cancel, Negate,
  Spellstutter Sprite, activated-ability counters, modal charms, …) compile unchanged. Verified there
  are **no record-deconstruction patterns** (`case CounterSpellEffect(...)`) anywhere — every use is
  `new CounterSpellEffect()` or `instanceof CounterSpellEffect`, so the added component is safe.

### Records deleted (+ handlers + tests)
- `CounterSpellAndExileEffect` (record) + `CounterSpellAndExileEffectHandler` + `CounterSpellAndExileEffectHandlerTest`.
- `CounterSpellAndPutOnTopOfLibraryEffect` (record) + `CounterSpellAndPutOnTopOfLibraryEffectHandler`
  (no handler unit test existed for this one — nothing to port).

### Handler fold
`CounterSpellEffectHandler.resolve` now switches on `((CounterSpellEffect) effect).destination()` and
dispatches to the **existing, unchanged** `CounterSupport` methods:
`GRAVEYARD → counterSpell`, `EXILE → counterSpellAndExile`, `LIBRARY_TOP → counterSpellAndPutOnTopOfLibrary`.
The three composite handlers were byte-identical modulo which `CounterSupport` method they called
(all did `getTargetId()` null-guard → `findCounterTarget` → dispose), so this dispatch is behavior-preserving.
`CounterSupport` was **not touched** — disposal calls, logs (`" is countered."` /
`" is countered and exiled."` / `" is countered and put on top of its owner's library."`), Guile
replacement, copy/ability handling, and `stateTriggerService` cleanup all stay exactly as before.

**Copies / uncounterable / abilities (COUNTER-TRAP):** `findCounterTarget` (shared) still enforces
uncounterable + protection-from-counter-by-color; the destination only applies where a real card would
have gone to the graveyard. The graveyard path (`counterSpell`) keeps its `isAbility` guard + ability
log wording; the exile/library-top paths keep their `!isCopy()`-only guard exactly as the old composite
handlers had them (the folded cards target `SPELL_ON_STACK` and can't hit abilities, so that asymmetry
is unobservable but preserved). No stop-rule hit.

### Cards edited
- `d/Dissipate.java` → `new CounterSpellEffect(CounteredSpellDestination.EXILE)`.
- `f/FaerieTrickery.java` → `...EXILE` (its `target(StackEntryPredicateTargetFilter(non-Faerie))` chain
  is on the card class and untouched — target filtering preserved).
- `m/MemoryLapse.java` → `new CounterSpellEffect(CounteredSpellDestination.LIBRARY_TOP)`.

Oracle wording verified: Dissipate/Faerie Trickery = "exile it instead of … graveyard"; Memory Lapse =
"put it on **top** of its owner's library" (no top-or-bottom / shuffle variant among the users). None of
these is an X/shuffle Memory Lapse variant, so `LIBRARY_TOP` (index-0 insert) is exact.

### AI fidelity (recorded — consistency change, verified green)
Two AI-facing consequences of folding, both **making the variants consistent with the base counter**,
neither flipping a calibrated test (Session-2 precedent):
- **`SpellEvaluator`** scores `instanceof CounterSpellEffect` at `manaValue*5`. The two variant records
  were separate types → previously fell through to `0`. After the fold they flow through that one
  branch and are scored like any counterspell. No `SpellEvaluator` edit needed (it never imported the
  variants); did **not** add any exile/library-top special-casing ("do not improve").
- **`InstantCategoryClassifier`** keys off the `CounterSpellingEffect` marker. `CounterSpellAndExileEffect`
  already implemented it (Dissipate/Faerie Trickery unchanged: still `COUNTERSPELL`). But
  `CounterSpellAndPutOnTopOfLibraryEffect` implemented **plain `CardEffect`** — an oversight — so Memory
  Lapse classified as `OTHER`. Folding onto `CounterSpellEffect` (a `CounterSpellingEffect`) now
  correctly classifies Memory Lapse as `COUNTERSPELL`. This is more rules-correct (it *is* a counterspell)
  and aligns with the task's "scored the same by whatever branch handles CounterSpellEffect."

### Trap checklist
- Whole-repo grep after edits: **no tracked `.java`** references `CounterSpellAndExileEffect` /
  `CounterSpellAndPutOnTopOfLibraryEffect` (only stale `build/` spotless-clean copies, gitignored,
  regenerated on next build; and historical `refactor-docs/` logs).
- `@ValidatesTarget` validators: **none** existed for any of the three (base or variants) —
  `service/validate/` grep clean; target legality flows through `TargetSpec.benign(SPELL_ON_STACK)` +
  Faerie Trickery's card-class `target(...)` filter.
- Stale test helpers: three surviving handler tests (`CounterSpellEffectHandlerTest`,
  `CounterUnlessPaysEffectHandlerTest`, `CounterSpellIfControllerPoisonedEffectHandlerTest`) each carried
  a **copy-pasted, unused** `counterAndExileEntry` helper + `CounterSpellAndExileEffect` import →
  removed from all three so they still compile.
- Engine `instanceof` sites: only the three handlers named the records (deleted two, folded one). No
  "spell-was-countered trigger" or copy-disposal code branched on the effect subtype (all in
  `CounterSupport`, untouched).

### Ported coverage
Behavioral card tests already asserted the final zones: `DissipateTest`/`FaerieTrickeryTest` (exiled, not
graveyard/battlefield) and `MemoryLapseTest` (asserts `playerDecks.get(owner).getFirst()` == countered
card, i.e. **library-top order**; not graveyard/exile/battlefield). The only handler-test scenario not
already covered behaviorally — "countered **copy** ceases to exist and is not exiled" — was ported into
`DissipateTest.counteredCopyCeasesToExistNotExiled` (mark the stacked creature spell `setCopy(true)`,
resolve Dissipate, assert it's in no zone). A normal `CounterSpellEffect` user still sending to graveyard
is pinned by `CounterSpellEffectHandlerTest.countersCreatureSpellAndPutsInGraveyard`.

### Tests run (all PASS)
- `cards.d.DissipateTest` (6 — incl. new copy test), `cards.m.MemoryLapseTest` (4), `cards.f.FaerieTrickeryTest` (4).
- `service.effect.normalfx.CounterSpellEffectHandlerTest` (10), `CounterUnlessPaysEffectHandlerTest` (8),
  `CounterSpellIfControllerPoisonedEffectHandlerTest` (confirms compile after helper removal).
- AI regression (`:magical-vibes-ai:test`, run via gradle — not `run-card-test.ps1`, which only targets
  `:magical-vibes-application`): `SpellEvaluatorTest` (66), `HardAiDecisionEngineTest` (77),
  `MediumAiDecisionEngineTest` (26), `EasyAiDecisionEngineTest` (31), `AiDecisionEngineTest` (117) —
  **0 failures/errors**, so the scoring `0→manaValue*5` and Memory Lapse `OTHER→COUNTERSPELL` changes
  flipped nothing.

### Notes
- Spotless: no-op (no unused imports introduced); not run, per prior-session policy.
- agent-docs updated: `EFFECTS_INDEX.md` (marker impl list + collapsed the two rows into the
  `CounterSpellEffect` row with the `CounteredSpellDestination` param), `EFFECTS_QUICK_REFERENCE.md`
  (marker + counter-spell section), `ORACLE_TEXT_EFFECT_MAP.md` (Memory Lapse row + new Dissipate/exile
  row), `CARD_PATTERNS_LANDS_SPELLS.md` (Faerie Trickery row). `EFFECT_COUPLING_MATRIX.md` left as-is
  (script-generated baseline, refreshed separately).
- **Do-not-touch composites left intact** as instructed: `CounterSpellAndExileAllWithSameNameEffect`,
  `CounterSpellAndCreateTreasureTokensEffect`, `CounterSpellAndGainControlIfArtifactOrCreatureEffect`,
  `CounterSpellIfControllerPoisonedEffect`, `CounterSpellsNamedLikeCardsExiledWithSourceEffect` — each
  couples extra behavior (search+exile, treasures, gain-control, poison condition, name-matching).
- Nothing deferred. Full suite not run (per CLAUDE.md — user runs it).
