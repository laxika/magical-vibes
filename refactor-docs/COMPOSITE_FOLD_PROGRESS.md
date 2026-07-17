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

---

## Session 4 — three "independent-pair" candidates: ALL THREE DEFERRED (no folds)

This session investigated three composites slated for decompose-onto-card. **All three turned out to
be legitimate bundles that the engine requires to stay a single effect-object**, for reasons that only
show up in the *composition context* (not in the handler body). Each handler is a clean "do A, then B
(, then C)" with no data flow and no extra branches — so by the handler-only DEFER criterion they look
foldable — but decomposing each onto the card breaks rules-correct behavior. **No card, record, handler,
agent-doc, or test was changed.** Working tree left clean. Detail + evidence below so future sessions
don't re-attempt these.

### ⭐ Load-bearing verdict for future batches: the ASYNC-RESUME (interaction-then-rider) machinery WORKS

Confirmed by code reading (no fold needed to establish it):
- **Pause side** — `EffectResolutionService.resolveEffectsLoop` (magical-vibes-engine): after each effect
  it checks `gameData.interaction.isAwaitingInput() || !pendingMayAbilities.isEmpty()`; if paused it
  stores `pendingEffectResolutionEntry = entry` and `pendingEffectResolutionIndex = i + 1` (the *next*
  effect, unless the effect is a re-run kind — X-value / stack-may / `rerunCurrentEffectAfterInteraction`)
  and returns.
- **Resume side** — the interaction-completion handlers re-enter `effectResolutionService.resolveEffectsFrom(
  gameData, pendingEffectResolutionEntry, pendingEffectResolutionIndex)`. For a **discard** specifically this
  is `CardChoiceHandlerService` (~line 270, comment: *"Resume resolving remaining effects on the same
  spell/ability (e.g. 'Target player discards a card, then mills a card.')"*). Same pattern in
  `ChoiceHandlerService`, `GraveyardChoiceHandlerService`, `LibraryChoiceHandlerService`,
  `InputCompletionService`, etc.

**So a plain "discard/other-interaction, then [rider]" (no `MayEffect`, no "if you do" contingency) IS
foldable into `DiscardEffect` + rider — the rider runs after the choice is answered.** The blockers below
are NOT the async machinery.

### Record 1 — GainControlUntapAndHasteTargetEffect (Dominus of Fealty) → DEFER

- Handler (`GainControlUntapAndHasteTargetEffectHandler`) is exactly `GainControlOfTargetEffect(END_OF_TURN)`
  → `UntapPermanentsEffect(TARGET)` → `GrantKeywordEffect(HASTE, TARGET)` on `entry.getTargetId()`. Clean.
- **Blocker — single `MayEffect` gate.** The card is
  `target(...).addEffect(UPKEEP_TRIGGERED, new MayEffect(new GainControlUntapAndHasteTargetEffect(), "…"))`.
  Oracle: *"you **may** gain control of target permanent until end of turn. **If you do**, untap it and it
  gains haste until end of turn."* `MayEffect(CardEffect wrapped, String)` wraps **exactly one** effect
  (verified: `MayEffectHandler` queues `List.of(e.wrapped())`; `EffectResolutionService` unwraps only that
  one on accept, or `continue`s on decline). The composite's own javadoc states this is *why* it exists.
  - Mirroring Act of Treason (three flat `addEffect`s, no may) would make the ability **mandatory** — drops
    "you may" — rules-wrong.
  - Wrapping only gain-control in the may and adding untap+haste as flat effects would **untap and haste a
    permanent you declined to steal** on a "no" — rules-wrong.
  - There is **no generic sequence/bundle effect** for a `MayEffect` to gate (searched: no
    `SequenceEffect`/`CompositeEffect`/`MultiEffect`/`AllOfEffect`; the `List<CardEffect>`-holding effects —
    `ChooseOneEffect`, `ClashEffect`, `KinshipEffect`, … — are all specialized, not a plain "do all of
    these").
  - The task's Record-1 description omitted the "you may … if you do" wording (called it just "gain control
    …, untap it, it gains haste"); the real card and oracle have the may. Rules accuracy (CLAUDE.md #1)
    wins → **left untouched.**

### Record 2 — DiscardCardAndUntapSelfEffect (Elaborate Firecannon) → DEFER

- Handler (`DiscardCardAndUntapSelfEffectHandler`): empty hand → log "has no cards to discard" and
  **return without untapping**; else `resolveDiscardCards(…, DiscardFollowUp.untap(sourcePermanentId))` — the
  untap rides the discard follow-up so it fires only **after** a card is actually discarded.
- Async machinery is fine (see verdict above), **but two independent blockers remain**:
  1. **Single `MayEffect` gate** — card is
     `addEffect(UPKEEP_TRIGGERED, new MayEffect(new DiscardCardAndUntapSelfEffect(), "Discard a card to
     untap …?"))`. Oracle: *"you **may** discard a card. **If you do**, untap …"*. Same one-effect-wrap
     problem as Record 1: the given decomposition (`DiscardEffect` + `UntapPermanentsEffect`, no may) makes
     the discard **mandatory** every upkeep — rules-wrong.
  2. **"If you do" empty-hand contingency** — the old handler does **not** untap when the hand is empty
     (and, via the follow-up, only untaps once a card is genuinely discarded). Two flat effects would run
     `UntapPermanentsEffect(SELF)` **unconditionally**, so Elaborate Firecannon would untap on an empty hand
     — rules-wrong (CR "if you do" is unsatisfied when nothing was discarded).
- Both blockers are on the *card-composition* side; the handler body itself is clean. No engine primitive
  today expresses "may-gate a discard **and** a contingent untap as one object" other than this composite →
  **left untouched.**

### Record 3 — ReturnSelfToHandAndCreateTokensEffect (Thopter Assembly) → DEFER

- **Rules first (web-verified):** Thopter Assembly's ability is a **single linked effect** —
  *"…return this creature to its owner's hand **and** create five 1/1 … Thopters."* The tokens are **NOT**
  contingent on the return ("if it leaves the battlefield you **still** get the five Thopters"). So the
  current handler (bounce if present, then **always** create tokens) is rules-correct; there is no "if you
  do" here. (Gatherer/Salvation rulings.)
- Handler is a clean "return self (or no-op if gone), then create tokens" — looked foldable into
  `ConditionalEffect(NoOtherPermanent(THOPTER), ReturnToHandEffect.self())` +
  `ConditionalEffect(NoOtherPermanent(THOPTER), CreateTokenEffect(5, "Thopter", …))`. **It is not.**
- **Blocker — the upkeep-trigger collector pushes ONE stack entry per `ConditionalEffect`.**
  `StepTriggerService.handleUpkeepTriggers` groups multiple slot effects into a single entry **only** for
  the targeting paths (any-target / player-target / exchange-control). Everything else falls into a
  `for (CardEffect effect : upkeepEffects)` loop where each `ConditionalEffect(NoOtherPermanent)` (and the
  other intervening-if variants) is pushed as its **own** `StackEntry` with a one-effect list. Two such
  effects ⇒ **two separate triggered abilities**, not one atomic ability.
  - `GameData.stack` resolves **LIFO** (`StackResolutionService` does `stack.removeLast()`). The
    token-creation entry (pushed last) resolves **first**, putting 5 Thopters onto the battlefield; then the
    bounce entry resolves and its `NoOtherPermanent(THOPTER)` intervening-if now sees those 5 Thopters →
    condition **fails** → the bounce is **skipped**. Net result: Assembly never returns to hand and the
    self-recycling loop is dead. Also breaks CR 603 atomicity (players would get priority between the two
    halves).
- **Empirically confirmed** this session: temporarily rewrote the card to the two-`ConditionalEffect`
  decomposition and ran `ThopterAssemblyTest` → **4 of 7 failed** — `doesNothingIfConditionFailsAtResolution`
  and `stillCreatesTokensIfDestroyedBeforeResolution` saw `stack size 2 (expected 1)`, and
  `triggersWhenNoOtherThopters` / `triggersWhenOpponentHasThopters` found **Thopter Assembly still on the
  battlefield** (bounce skipped) exactly as predicted. Card was then reverted via `git checkout --`; tree is
  clean.
- The composite bundles both halves into one effect so the single upkeep trigger stays one atomic entry →
  **left untouched.**

### Common theme / guidance for future sessions
These three are **not redundant composites**. Each exists to keep multiple sub-effects inside one
effect-object because a *wrapper the card must use* only accepts one effect:
- `MayEffect` / `ConditionalEffect` wrap exactly one `CardEffect` (Records 1, 2, and the *card-level* wrapper
  of 3).
- The upkeep-trigger collector emits one stack entry per intervening-if `ConditionalEffect` (Record 3).

Decomposing any of them onto the card is only possible with an **engine change** (a generic sequential
"do-all-of" bundle effect that `MayEffect`/`ConditionalEffect` could wrap, and/or batching same-condition
intervening-if effects into one upkeep entry) — out of scope for a decompose-onto-card fold, and none is
needed for correctness today. Before folding any future `FooAndBar` composite, check whether the *user card*
wraps it in `MayEffect`/`ConditionalEffect` or registers it on a per-effect trigger slot (`UPKEEP_TRIGGERED`
et al.); if so, the composite is load-bearing.

### Tests run
- `cards.t.ThopterAssemblyTest` — run once against a temporary decomposition to prove the split (4/7 failed
  as documented), then the card was reverted; **no committed test changes**. On the unchanged repo the suite
  is the pre-existing 7-test green baseline.
- No handler unit tests deleted (Record 2's `DiscardCardAndUntapSelfEffectHandlerTest` and the Record 1/3
  handlers all remain in place, since nothing was folded).

### Notes
- No agent-docs touched (`CARD_PATTERNS_CREATURES_TRIGGERED.md`, `EFFECTS_INDEX.md`,
  `EFFECTS_QUICK_REFERENCE.md`, `ORACLE_TEXT_EFFECT_MAP.md` still reference all three records — correct, they
  still exist).
- Nothing deleted. Full suite not run (per CLAUDE.md — user runs it).

---

## Session 5 — three sacrifice-self riders + one drain record: ALL FOUR DEFERRED (no folds)

Same outcome as Session 4, same root cause: every one of these composites is bundled inside a
*single-effect wrapper* the card must use (`MayEffect` / `FlipCoinWinEffect` / `ConditionalEffect` /
`TriggeringPermanentConditionalEffect`) and/or encodes an **"if you do" contingency** that two flat
`addEffect`s cannot reproduce. The Session-4 rule fired on all four: *before folding a `FooAndBar`
composite, check whether the user card wraps it in a one-effect wrapper or registers it on a per-effect
trigger slot; if so, it is load-bearing.* **No card, record, handler, validator, agent-doc, or test was
changed. Working tree left clean.** Evidence below so future sessions don't re-attempt these.

### Record 1 — SacrificeSelfAndDrawCardsEffect → DEFER

- Handler (`SacrificeSelfAndDrawCardsEffectHandler`): `sourcePermanentId == null` → return; source **not on
  battlefield** → log "…fizzles — source no longer on the battlefield." and **return WITHOUT drawing**;
  else `removePermanentToGraveyard` + log "…is sacrificed." + `applyDrawCards(controllerId, amount)`. So the
  draw is contingent on the source still being present ("if you do").
- **Both users wrap it in a one-effect wrapper** (verified `MayEffect(CardEffect, String)` and
  `FlipCoinWinEffect(CardEffect wrapped[, CardEffect lost])` each hold exactly one `CardEffect`; no list
  variant):
  - `i/ImpalerShrike` — `addEffect(ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(new SacrificeSelfAndDrawCardsEffect(3),
    "You may sacrifice it. If you do, draw three cards."))`. Oracle *"you **may** sacrifice it. **If you do**,
    draw three cards."*
  - `s/SorcerersStrongbox` — `List.of(new FlipCoinWinEffect(new SacrificeSelfAndDrawCardsEffect(3)))` in a
    `{2},{T}` activated ability. Oracle *"Flip a coin. **If you win the flip**, sacrifice … **and** draw three
    cards."*
- **Blockers:** (a) the expected decomposition `SacrificeSelfEffect` + `DrawCardEffect(3)` can't sit inside one
  `MayEffect`/`FlipCoinWinEffect` → mirroring Act of Treason (flat effects, no wrapper) drops the "may"/"win the
  flip" gate = mandatory sac+draw every time = rules-wrong; wrapping only the sac in the gate leaves an
  unconditional draw. (b) even ignoring the wrapper, two flat effects draw **unconditionally**, but the composite
  does **not** draw when the source is already gone — an observable "if you do" difference (source can be
  removed in response to the ability). No generic "do-all-of" bundle exists for the wrapper to gate (Session-4
  search still holds). **Left untouched.**
- LKI note gathered (for a future engine-enabled session): `SacrificeSelfEffect`'s own handler is a clean no-op
  when the source is missing and fires `checkAllyPermanentSacrificedTriggers` + `removeOrphanedAuras` on success;
  `DrawCardEffect(int)` exists. The pieces exist — only the wrapper/contingency blocks the fold.

### Record 2 — SacrificeSelfAndTargetPlayerDiscardsEffect → DEFER

- Handler (`SacrificeSelfAndTargetPlayerDiscardsEffectHandler`): `targetId`/`sourcePermanentId` null → return;
  source gone → fizzle log + **return without discard**; else sacrifice + `resolveDiscardCards(targetPlayerId,
  amount)`. Discard contingent on successful sacrifice ("if you do").
- **User `m/MindstabThrull` wraps it in a one-effect `MayEffect`:**
  `addEffect(ON_ATTACKS_UNBLOCKED, new MayEffect(new SacrificeSelfAndTargetPlayerDiscardsEffect(3),
  "You may sacrifice it. If you do, defending player discards three cards."))`. Oracle *"…you **may** sacrifice
  it. **If you do**, defending player discards three cards."*
- **Blockers:** same one-effect-`MayEffect` wrapper as Record 1 (the given `DiscardEffect(3, TARGET_PLAYER)` +
  `SacrificeSelfEffect` split can't be gated by one may → makes the sac mandatory) plus the "if you do"
  empty-sacrifice contingency. The async-resume machinery for the discard interaction is fine (Session-4
  verdict), but that was never the blocker. **Left untouched.**

### Record 3 — SacrificeSelfThenDealDamageToTargetPlayerEffect → DEFER

- **Not wrapped in `MayEffect`** — but it is built inside the engine, not on a card:
  `DrawService.checkBoobyTraps` pushes a single-effect `StackEntry` `List.of(new
  SacrificeSelfThenDealDamageToTargetPlayerEffect(10))` with `targetId = drawingPlayer`,
  `sourcePermanentId = the Booby Trap`. (`b/BoobyTrap` itself only carries `ChooseCardNameOnEnterEffect` +
  `BoobyTrapEffect`.)
- Rules (Booby Trap, 9ED): *"…sacrifice Booby Trap. **If you do**, Booby Trap deals 10 damage to that player."*
  Handler encodes this exactly: `self == null || !removePermanentToGraveyard(...)` → **return, no damage**; on
  success it fires `checkAllyPermanentSacrificedTriggers`, logs the sacrifice, `removeOrphanedAuras`, then (if the
  target is still a player and the source isn't prevented) `dealDamageToPlayer` + `checkWinCondition`.
- **Blocker — the "if you do" contingency is inexpressible as two flat effects.** The expected split
  `SacrificeSelfEffect` + `DealDamageToPlayersEffect(10, TARGET_PLAYER)` would run the damage **unconditionally**:
  `SacrificeSelfEffect` is a silent no-op when the source is already gone (Booby Trap can be destroyed/sacrificed
  in response to the draw trigger), so the damage half would still deal 10 to the drawer — rules-wrong (CR "if you
  do" is unsatisfied when nothing was sacrificed). A `ConditionalEffect` can't rescue it either: any condition
  would evaluate **after** `SacrificeSelfEffect` has run, when the source is gone in *both* the "I just
  sacrificed it" and the "it was already gone" cases — indistinguishable. Only the composite, which branches on
  `removePermanentToGraveyard`'s boolean return, can tell them apart.
- LKI traps checked and found **non-blocking** (so this is purely the contingency): `SacrificeSelfEffectHandler`
  already fires the identical `checkAllyPermanentSacrificedTriggers` + `removeOrphanedAuras`; and
  `DealDamageToPlayersEffect` already has a `TARGET_PLAYER` recipient and its damage keys off the source's
  live-or-snapshot LKI + prevention. Every *piece* exists — only the sacrifice-succeeded gate is missing.
  **Left untouched.**

### Record 4 — TargetPlayerLosesLifeAndControllerGainsLifeEffect (drain) → DEFER

The task framed this as a one-card (Syphon Life) one-liner. It is **not**: the record has **11 card users
across five wiring shapes + a dynamic engine construction site**, and two of those users make it **undeletable**.

- `controllerGainsLifeLost` check done first, per the task: the `LoseLifeEffect` handler honors the drain flag
  **only** on the `EACH_PLAYER`/`EACH_OPPONENT` branches (`eachPlayerLosesLife`). The `TARGET_PLAYER` branch
  (`loseTargetPlayerLife`) applies the loss inline (no drain, and — matching the old composite — **without firing
  "loses life" triggers**). So the one-line `LoseLifeEffect(N, TARGET_PLAYER, true)` fold is unavailable; the
  correct fold would be the **decompose** path `LoseLifeEffect(N, TARGET_PLAYER)` + `GainLifeEffect(N)` (nominal
  gain, gated `>0` — the old handler gains the fixed `lifeGain` independent of actual loss, and skips the gain
  when `lifeGain == 0`).
- **Hard blocker — two users wrap the drain in a one-effect conditional wrapper** (verified `ConditionalEffect(
  Condition, CardEffect wrapped)` and `TriggeringPermanentConditionalEffect(PermanentPredicate, CardEffect
  wrapped)` each hold exactly one `CardEffect`):
  - `b/BleakCovenVampires` — `addEffect(ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Metalcraft(),
    new TargetPlayerLosesLifeAndControllerGainsLifeEffect(4, 4)))`.
  - `a/ArnynDeathbloomBotanist` — `TriggeringPermanentConditionalEffect(powerOrToughness≤1, DRAIN(2,2))` on
    `ON_ALLY_CREATURE_DIES` + `ON_DEATH`.
  The decomposed `LoseLifeEffect` + `GainLifeEffect` pair cannot be placed inside one such wrapper without a
  generic "do-all-of" bundle effect (engine change, out of scope). **Because these two can't migrate, the record
  + handler + its `@ValidatesTarget` cannot be deleted** — so folding the other nine users would leave the
  composite alive (zero cleanup payoff) **and** introduce two ways to express the same drain (a net regression
  for a program whose goal is deletion). All-or-nothing → DEFER the whole record.
- Secondary friction confirming the defer even if the wrappers didn't exist:
  - **`(2,0)` special case** — `n/NecrogenCenser` (`{T}`, remove charge: *"target player loses 2 life"*, **no
    gain**). Its decomposition must **omit** `GainLifeEffect` entirely (the old handler's `lifeGain>0` guard),
    so the fold isn't a uniform two-effect substitution.
  - **Per-effect trigger-slot splitting risk** — `f/FalkenrathNoble` (`ON_DEATH` + `ON_ANY_CREATURE_DIES`),
    `h/HighwayRobber` & `h/HierophantsChalice` (`ON_ENTER_BATTLEFIELD`), `p/PollutedBonds`
    (`ON_OPPONENT_LAND_ENTERS_BATTLEFIELD`) each register the **targeted** drain on a trigger slot. Splitting it
    into two slot effects risks the Session-4 Record-3 trap (collector emitting one stack entry per effect →
    the targeted loss and the gain become two abilities → the gain no longer fizzles with an illegal target,
    breaking CR atomicity). Would need per-slot verification the collector bundles them into one targeted entry.
  - **Dynamic construction** — `SpellCastTriggerCollectorService` (~line 724) builds `new
    TargetPlayerLosesLifeAndControllerGainsLifeEffect(trigger.lifeLoss(), trigger.lifeGain())` for every
    `SpellCastLifeDrainEffect` trigger; that site would also need to push the decomposed pair.
  - The remaining plain users (`s/SyphonLife`+Retrace, `s/SoulFeast`, `m/MorselTheft` `SPELL`; `b/BlightKeeper`
    activated ability) *would* fold cleanly in isolation, but that doesn't help while the record must survive.
- **Full-fold requires an engine change** (a generic sequential bundle effect that
  `ConditionalEffect`/`TriggeringPermanentConditionalEffect` can wrap, plus trigger-collector batching of
  same-slot targeted effects). Out of scope for a decompose-onto-card fold. **Left untouched.**

### Common theme (reinforces Session 4)
All four are load-bearing for the same structural reason: a wrapper the card *must* use accepts exactly one
`CardEffect` (`MayEffect`, `FlipCoinWinEffect`, `ConditionalEffect`, `TriggeringPermanentConditionalEffect`),
and/or the composite encodes an **"if you do" contingency** (draw/discard/damage only when the self-sacrifice
actually happened) that a post-sacrifice `ConditionalEffect` can't recover (the source is gone either way).
The wrapper set to check before any future fold is now: `MayEffect`, `FlipCoinWinEffect`, `ConditionalEffect`,
`TriggeringPermanentConditionalEffect`, and per-effect trigger-slot registration.

### Tests run
- **None.** No production or test code changed (working tree clean), so there was nothing to verify — every
  defer rests on a code-read blocker (the four wrapper records literally hold a single `CardEffect`; the "if you
  do" handlers branch on `removePermanentToGraveyard`'s boolean / a `source == null` fizzle). Unlike Session 4's
  Thopter Assembly (subtle LIFO trigger-split needing empirical proof), no temporary decomposition was needed.

### Notes
- No agent-docs touched — `EFFECTS_INDEX.md`, `EFFECTS_QUICK_REFERENCE.md`, `ORACLE_TEXT_EFFECT_MAP.md`,
  `CARD_PATTERNS_CREATURES_TRIGGERED.md`, `CARD_PATTERNS_PERMANENTS_ARTIFACTS.md`, `CARD_PATTERNS_CREATURES_ETB.md`
  still reference all four records, correctly (they all still exist).
- Nothing deleted. Full suite not run (per CLAUDE.md — user runs it).

---

## Session 6 — batch-1 closeout: verify + metrics + batch-2 scope (docs only, NO functional change)

Verification/metrics session. No card, record, handler, or agent-doc changed. Only this file updated
(+ `EFFECT_COUPLING_MATRIX.md`/`effect-dispatch-baseline.txt`, which the audit script regenerated).

### Deletion verification (Sessions 1–3, five records)
Whole-repo grep for each of the five deleted records —
`DealDamageToAnyTargetAndGainLifeEffect`, `DealXDamageToAnyTargetAndGainXLifeEffect`,
`DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect`, `CounterSpellAndExileEffect`,
`CounterSpellAndPutOnTopOfLibraryEffect` — hits **only** in `refactor-docs/` historical logs (this file,
`TARGETSPEC_PROGRESS.md`, `PROGRESS.md`). **No `.java` references** (production or test). **No agent-docs
straggler**: none of EFFECTS_INDEX / EFFECTS_QUICK_REFERENCE / ORACLE_TEXT_EFFECT_MAP presents any of the
five as available. The script-generated `EFFECT_COUPLING_MATRIX.md` (which Session 3 had left listing the
two counter records) was refreshed by this session's audit run and now lists **none** of the five.

### Build (touched modules only)
`.\gradlew :magical-vibes-domain:compileJava :magical-vibes-engine:compileJava :magical-vibes-card:compileJava
:magical-vibes-ai:compileJava` → **BUILD SUCCESSFUL** (all up-to-date/from-cache, no errors). Tests not run.

### Metrics (2026-07-17)
- Effect types: **1102 → 1098** (−4). Violations: **483 → 482** (−1). (`scripts/effect-coupling-audit.py`.)
  Net −4 vs. five records deleted — the 1102/483 baseline was most likely snapshotted after Session 1's
  deletion (or reflects unrelated repo churn); recorded as-measured, not reconciled.
- `And[A-Z]`/`Then[A-Z]` files in `model/effect/`: **137** (case-SENSITIVE; a case-insensitive match reports
  an inflated 234 by catching `Land`/`Hand`/`Random`/… + capital). Of the 137: 1 is the `ThenEffectRecipient`
  enum; 2 are the rider-infra precedents (`DestroyTargetPermanentThenEffect`, `SacrificePermanentThenEffect`);
  7 are still-present Session 4/5 deferrals. ⇒ **~127 genuine unfolded composite records remain.**

### KEY SCOPING FINDING
Batch 1 essentially **exhausted the pure decompose-onto-card seam** (category a). Almost every remaining
And/Then composite is either (i) wrapped by its card in a single-effect wrapper (`MayEffect` /
`ConditionalEffect` / `FlipCoinWinEffect` / …) — the Session 4/5 blocker — or (ii) **event-coupled** ("if you
do", "for each destroyed/discarded", "equal to toughness"). Continuing past batch 1 means committing to the
**`StackEntry.eventValue` rider channel** (categories b/c); the two `...ThenEffect` precedents are the template.
Batch 2 is a **decision point**, not more of the same.

---

## Batch 2 candidates (proposed, NOT implemented)

Each record read (record only, per task); user wiring verified by usage grep (Session 4/5 rule). Ranked by
value = deletability (all users migratable) × tractability. Classification: (a) pure decomposition,
(b) event-coupled (needs `eventValue`; precedents `DestroyTargetPermanentThenEffect`/`SacrificePermanentThenEffect`),
(c) interaction-coupled (async-resume works — Session 4 verdict), (d) leave alone.

### Tier 1 — event-coupled "per-destroyed / per-sacrificed count" cluster  ⟶ highest value
One shared piece of infra — capture the count/stat of the destroyed-or-sacrificed permanents onto `eventValue`,
then run a rider (the `DestroyTargetPermanentThenEffect` pattern generalized to destroy-all / edict) — folds
this whole family. `DestroyAllPermanentsEffect` primitive already exists.

1. **DestroyAllPermanentsAndGainLifePerDestroyedEffect** — (b). Users: Fracturing Gust, Righteous Fury,
   Paraselene (all SPELL, flat, unwrapped). Replace: `DestroyAllPermanentsEffect(filter)` + gain-life rider
   reading count-destroyed `eventValue`. **HIGH.**
2. **DestroyCreaturesTargetPlayerControlsAndLoseLifePerDestroyedEffect** — (b). User: Rain of Daggers (SPELL).
   Same infra, lose-life rider. **HIGH.**
3. **DestroyCreaturesTargetPlayerControlsAndDrawPerDestroyedEffect** — (b). User: Overwhelming Forces (SPELL).
   Same infra, draw rider. **HIGH.**
4. **DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect** — (b), *already a composite in the tree*.
   User: Phyrexian Rebirth (SPELL). Fold into the same generalized rider (token rider) so the family collapses
   to one mechanism instead of four. **MED** (include to unify).
5. **SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect** — (b), stat = toughness (not a count).
   Users: Tribute to Hunger (edict, targets opponent, SPELL) + Doomgape (self-sac, UPKEEP_TRIGGERED). Needs
   toughness-of-sacrificed snapshot on `eventValue` (`SacrificePermanentThenEffect` precedent) + an edict
   (target-player-sacrifices) variant + **Doomgape trigger-slot-split check** (Session-4 Record-3). **MED.**

### Tier 2 — count-after-interaction (b + c)
6. **TargetPlayerDiscardsThenDrawsThatManyEffect** — (b)+(c). User: Forget (SPELL). Draw count = cards actually
   discarded; draw rides after the discard interaction (async-resume). **MED.**
7. **RemoveCounterFromTargetAndGainLifeEffect** — (b) "if you do". Users: Woeleecher (gain 2 if removed),
   Chainbreaker (lifeGain 0 → no gain). Blocker: **no standalone remove-counter primitive exists** — a
   `RemoveCounterFromTargetEffect` primitive **and** a "then gain life if removed" rider must both be built;
   Chainbreaker collapses trivially to the bare primitive. **MED-LOW.**

### Tier 3 — decomposition with a slot/companion caveat
8. **DrawAndLoseLifePerSubtypeEffect** — (a) dynamic-count decomposition: `DrawCardEffect(PermanentCount(ZOMBIE))`
   + `LoseLifeEffect(PermanentCount(ZOMBIE), CONTROLLER)` (Corrupt precedent, Session 2). User: Graveborn Muse,
   **UPKEEP_TRIGGERED** → must verify the upkeep collector doesn't split the two effects into separate LIFO
   entries (Session-4 Record-3 trap). Order-independent here (draw N / lose N, N = Zombie count, stable), so a
   split is functionally benign but a CR-603 atomicity deviation — verify collector first. **LOW-MED.**
9. **DealDamageToTargetAndTheirCreaturesEffect** — (a)/(b) via targeting-companion (shared `targetId`, not a
   computed value). Users: Chandra Nalaar −8 (loyalty), Flame Wave (SPELL). The decomposed pattern already
   ships on `ChandraBoldPyromancer` (`DealDamageToPlayersEffect(TARGET_PLAYER)` +
   `DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect`), but this record's semantics differ
   (target = PLAYER_OR_PLANESWALKER, damages that player/pw-controller's **creatures only**) → needs a companion
   matching those exact semantics. **MED.**

### Tier 4 — harder / low payoff
10. **DiscardThenReturnFromGraveyardToHandEffect** — (c)+(b). User: Recall (SPELL, XValue). Two interactions
    (discard, then graveyard-returns) + returns = actual discarded count. **HARDER**, 1 user.

### Leave alone (d) — recorded so future sessions don't re-attempt
- **DrawAndDiscardCardEffect** — DEFER (mixed): ~19 users, several `MayEffect`/`ConditionalEffect`-wrapped
  (Stadium Tidalmage, Daring Saboteur, Murder of Crows, Shipwreck Looter, Marauding Looter, Muse Seeker). Flat
  users (Faithless Looting, Catalog, Jalum Tome, Owl Familiar, …) would fold to `DrawCardEffect(N)` +
  `DiscardEffect(N)` (draw first, discard terminal — no rider), but wrapped users can't → **all-or-nothing;
  record survives → no payoff.** Unblocks only with a generic do-all-of `SequenceEffect` bundle (engine change,
  which would also unlock most Session 4/5 deferrals).
- **DiscardAndDrawCardEffect** — DEFER: all users `MayEffect`-wrapped (Keldon Raider, Rubble Rouser, Pursue the
  Past) + "draw only if actually discarded" if-you-do contingency.
- **RemoveCounterFromSourceAndGainLifeEffect** — DEFER: Living Artifact wraps it in `MayEffect` + if-you-do
  (Session-5 shape).
- **MillControllerAndDealDamageByHighestManaValueEffect** — leave alone: bespoke (damage = greatest mana value
  among milled cards, not a count); 1 user (Heretic's Punishment).
- **PutHandOnBottomOfLibraryAndDrawEffect** / **ShuffleHandIntoLibraryAndDrawEffect** — leave alone: single
  linked reshuffle-then-draw-that-many; bespoke.
- **RemoveCountersFromTargetAndBoostSelfEffect** — leave alone: "up to X" counter choice (interaction) + boost =
  removed count; bespoke (Hex Parasite).
- **Prevent…And…Effect** family (14 records) + replacement effects — out of scope per program charter.

### Cross-cutting recommendation
Highest-leverage batch-2 move: build the **generalized destroy/sacrifice-count → rider** infra (folds Tier-1
items 1–5 and unifies the already-present token-count composite). It is the natural extension of the
`DestroyTargetPermanentThenEffect` / `SacrificePermanentThenEffect` precedents to the destroy-all / edict case.
Everything else is lower-yield or blocked on a `SequenceEffect` do-all-of bundle (which, if built, would in turn
unlock the wrapper-blocked Session 4/5 records and the loot/rummage records — a possible batch 3 theme).

### Notes
- Docs-only session. No production/test code touched; working tree change is limited to this file plus the
  audit script's regenerated `EFFECT_COUPLING_MATRIX.md` / `effect-dispatch-baseline.txt`.
- Full suite not run (per CLAUDE.md — user runs it before committing).

## Batch 3 Session 1 — SequenceEffect primitive

Built the keystone primitive the batch-2 closeout flagged as the unlock for the wrapper-blocked Session 4/5
records and the loot/rummage records: a generic **do-all-of** bundle that lets a single-effect wrapper
(`MayEffect`, `ConditionalEffect`, `TriggeringPermanentConditionalEffect`, `FlipCoinWinEffect`) gate several
steps, and lets a trigger slot carry a multi-step atomic ability (collectors push one stack entry per slot
effect, so a multi-step bundle previously could not live on a trigger slot). **No card was changed this
session** — this is primitive + engine support + tests only.

### What was added
- **`SequenceEffect(List<CardEffect> steps)`** (`magical-vibes-domain/.../model/effect/SequenceEffect.java`) —
  record; compact constructor `List.copyOf(steps)` + throws `IllegalArgumentException` on `< 2` steps; static
  `of(CardEffect...)` factory. `targetSpec()` returns the FIRST step's non-`NONE` spec (the entry's single
  target is shared by every targeting step, exactly like multiple flat targeting effects on one spell slot).
  Javadoc spells out: (a) strict-order, NO "if you do" data flow between steps — a no-op step does not stop
  later steps; (b) use ONLY to gate steps under a single-effect wrapper or to keep steps one atomic trigger-slot
  ability — prefer flat `addEffect(...)` for plain spell/ability lists (Act of Treason / Drain Life pattern);
  (c) multi-target groups inside a sequence are unsupported (spliced steps aren't in the card's
  effect→target-group table, so every targeting step reads the shared `targetId`).
- **`StackEntry`** — dropped `final` from `effectsToResolve` and added
  `insertEffectsToResolve(int index, List<CardEffect> steps)` (copies into a fresh `ArrayList`, splices, and
  reassigns the field; never mutates the caller's list — safe against `List.of(...)`-backed entries). Deep-copy
  constructor already did `new ArrayList<>(source.effectsToResolve)`, untouched.
- **`EffectResolutionService.resolveEffectsLoop`** — new inline branch AFTER the `MayPayTapPermanentsEffect`
  re-entry block and BEFORE the "Multi-target support" comment: when `effectToResolve instanceof SequenceEffect`,
  splice `sequence.steps()` at `i + 1`, reassign the local `effects = entry.getEffectsToResolve()`, and
  `continue`. No handler class — like `ConditionalEffect` it is engine-inline and must never reach
  `registry.getHandler`.
- **`SpellEvaluator.evaluateSingleEffect`** (AI) — new branch after the `ChooseOneEffect` case: a
  `SequenceEffect` scores as the SUM of recursively scoring each step. Nothing else changed.

### Splice design (why it's correct)
The splice persists **on the entry**, so the standard pause/resume machinery carries it for free: async pause
stores `pendingEffectResolutionEntry` + `pendingEffectResolutionIndex` (indices into the entry's live list) and
every resume re-enters `resolveEffectsFrom` which re-reads `entry.getEffectsToResolve()`. An interaction raised
by step 1 therefore resumes into step 2 with no special handling. Nested sequences splice depth-first when
reached (the inner sequence is itself a spliced step, re-detected on its own iteration). `MayEffect(SequenceEffect)`
works because the may re-entry sets `effectToResolve = may.wrapped()` before the sequence branch runs. The for-loop's
`i < effects.size()` re-reads the reassigned local each iteration; `continue` advances `i` onto the first spliced
step (inserted at `i + 1`), so the sequence entry itself is not re-processed.

Deliberately **NOT** touched: `TargetValidationService.checkEffectTargets` / `@ValidatesTarget` machinery — class-keyed
validators run unconditionally for registered classes, and making them see effects nested inside sequences would fire
validators in contexts where they never ran before (recurring trap in refactor-docs). Validation left exactly as-is.

### Tests
`EffectResolutionServiceTest` → new `@Nested class SequenceEffects` (mirrors the file's handler-registry-mock style;
uses distinct `DrawCardEffect(1..4)` steps so identity-keyed handler stubs stay distinct):
- `resolvesBothStepsInOrder` — two-step sequence resolves both in order; asserts the sequence never reaches
  `registry.getHandler`.
- `conditionalUnmetResolvesNoStep` / `conditionalMetResolvesBothSteps` — `ConditionalEffect(Metalcraft, sequence)`:
  unmet resolves no step; met resolves both in order.
- `pauseInFirstStepResumesIntoSecondStep` — first step's handler queues a `PendingMayAbility` (async pause); asserts
  the resumption index points at the spliced second step, then `resolveEffectsFrom` at that index resolves it.
- `nestedSequenceResolvesLeavesDepthFirst` — `SequenceEffect.of(a, inner, d)` with `inner = of(b, c)` resolves leaves
  a, b, c, d depth-first.
- `effectAfterSequenceResolvesLast` — an effect following the sequence in the original list resolves after all
  spliced steps.

### Verification
- `:magical-vibes-domain :magical-vibes-engine :magical-vibes-ai :magical-vibes-application` compile green
  (`compileJava` / `compileTestJava`).
- `EffectResolutionServiceTest` — PASS. `SpellEvaluatorTest` (`magical-vibes-ai`) — PASS (no calibrated-AI flip).
- Full suite not run (per CLAUDE.md — user runs it before committing).

### Notes / next
- No card folded yet: the next batch-3 sessions can now decompose the wrapper-blocked Session 4/5 records and the
  loot/rummage records (`DrawAndDiscardCardEffect`, `DiscardAndDrawCardEffect`, `RemoveCounterFromSourceAndGainLifeEffect`,
  …) by wrapping their steps in `SequenceEffect` inside the existing `MayEffect`/`ConditionalEffect` gate.
- Reminder for those sessions: `SequenceEffect` is do-all-of with NO if-you-do contingency. Records whose second step
  is conditional on the first *actually happening* ("draw only if you discarded") still need their own primitive or a
  count-carried rider — a sequence alone does not express that.

## Batch 3 Session 2 — GainControlUntapAndHasteTargetEffect folded (MayEffect(SequenceEffect) proven)

First real fold onto the Session-1 `SequenceEffect` primitive. Deleted the composite record
`GainControlUntapAndHasteTargetEffect` and its handler, decomposing Dominus of Fealty onto the three existing
Threaten primitives wrapped in a `SequenceEffect` inside its `MayEffect`.

### Why a plain (non-contingent) sequence is rules-correct
Dominus oracle: "you MAY gain control of target permanent until end of turn. IF YOU DO, untap it and it gains haste
until end of turn." The may-gate is what blocked decomposition before — `MayEffect(CardEffect wrapped, String prompt)`
holds exactly one effect. But accepting the may is what makes the gain-control happen, so once accepted the untap +
haste are unconditional. There is no *inner* "if you do" between the three steps → a plain `SequenceEffect` (no
contingency) is exactly right.

### What changed
- **`DominusOfFealty.java`** — inside its unchanged `MayEffect(..., "Gain control of target permanent until end of
  turn?")` on `UPKEEP_TRIGGERED`, replaced `new GainControlUntapAndHasteTargetEffect()` with
  `SequenceEffect.of(GainControlOfTargetEffect(END_OF_TURN), UntapPermanentsEffect(TARGET), GrantKeywordEffect(HASTE, TARGET))`.
  The `target(PermanentPredicateTargetFilter(PermanentTruePredicate(), ...))` clause and slot are byte-identical; only
  imports changed.
- **Step order = the deleted handler's order** (gain control → untap → haste), which is also Dominus's oracle order.
  Note Act of Treason lists the same three constructions in a different order (untap → control → haste); per the fold
  brief the HANDLER is source of truth, so I mirrored the handler, not Act of Treason. (Order is functionally
  irrelevant here — untap/haste don't depend on who controls the permanent — but mirroring the handler keeps it
  provably behavior-preserving.)
- **Deleted** `magical-vibes-domain/.../model/effect/GainControlUntapAndHasteTargetEffect.java` and
  `magical-vibes-engine/.../service/effect/normalfx/GainControlUntapAndHasteTargetEffectHandler.java`.
- **`MayAbilityHandlerService`** — NOT touched. The known-risk `extractInnerEffect` / `setUpSelfTargetIfNeeded`
  helpers did not need to learn about `SequenceEffect`: Dominus targets via the card's explicit `target(...)` clause
  (entry carries the `targetId`), and every spliced step reads that shared `targetId`, so the may flow worked
  unchanged.

### Greps run (whole-repo *.java, build/ excluded)
- `GainControlUntapAndHasteTargetEffect` in `*.java` → zero (all references gone).
- `magical-vibes-engine/**/validate/` → no `@ValidatesTarget` for the record (its checks flow through the primitives'
  own validators; nothing to delete).
- `magical-vibes-ai` → zero (no bespoke scoring; the deleted composite was never AI-scored, and the decomposed
  primitives already score individually via the Session-1 `SpellEvaluator` `SequenceEffect` sum branch).
- `GainControlUntapAndHasteTargetEffectHandlerTest` → does not exist (nothing to port/delete).
- Remaining `.md` hits: `agent-docs/` rewritten (below); `refactor-docs/` history untouched
  (`PROGRESS.md`, `TARGETSPEC_PROGRESS.md` deletion lists, `EFFECT_COUPLING_MATRIX.md` row) — the matrix is a
  generated audit artifact and its stale row will clear on the next `effect-coupling-audit.py` refresh.

### Docs
- `agent-docs/EFFECTS_INDEX.md` — deleted the `GainControlUntapAndHasteTargetEffect` row.
- `agent-docs/EFFECTS_QUICK_REFERENCE.md` — Control/steal bullet rewritten to the "no dedicated effect; compose
  `MayEffect(SequenceEffect.of(...))`" pattern.
- `agent-docs/ORACLE_TEXT_EFFECT_MAP.md` — both rows rewritten: the Threaten rider row → flat three effects
  (Act of Treason); the Dominus row → `MayEffect(SequenceEffect.of(...))`.
- `agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md` — no Dominus row existed; nothing to change.

### Tests
- `DominusOfFealtyTest` — PASS (4 tests; baseline green before the change, still green after: accept → control
  stolen + untapped + haste until EOT; decline → nothing; control reverts at end of turn).
- `ActOfTreasonTest` — PASS (7 tests) as a control that the shared primitives are unharmed.
- Full suite not run (per CLAUDE.md — user runs it).

### Traps hit
- None. `MayAbilityHandlerService` known risk did not materialize (targeting via explicit `target(...)`, not the
  inner-effect self-target inference).

---

## Batch 3 Session 3 — ReturnSelfToHandAndCreateTokensEffect folded (trigger-slot atomicity via ConditionalEffect(SequenceEffect) proven)

Deleted the composite record `ReturnSelfToHandAndCreateTokensEffect` and its handler by decomposing Thopter
Assembly onto two existing primitives wrapped in a `SequenceEffect`, kept inside the card's unchanged
`ConditionalEffect` on the `UPKEEP_TRIGGERED` slot.

### Why a plain (non-contingent) sequence is rules-correct
Thopter Assembly oracle: "At the beginning of your upkeep, if you control no other Thopters, return Thopter
Assembly to its owner's hand AND create five 1/1 colorless Thopter artifact creature tokens with flying."
Verified ruling: the tokens are NOT contingent on the return — if the Assembly already left the battlefield you
STILL get the five Thopters. Both steps are unconditional (the only gate is the shared intervening-if "no other
Thopters"), so a plain `SequenceEffect` (no data-flow, no "if you do") is exactly right. This mirrors the deleted
handler, which bounced-or-skipped then unconditionally created tokens.

### Why this needed SequenceEffect (not two ConditionalEffect slot effects)
Recorded in Session 4 of the earlier batch: decomposing into TWO `ConditionalEffect(NoOtherPermanent(THOPTER), ...)`
slot effects failed 4/7 cases. The upkeep-trigger collector pushes one stack entry PER slot effect; the stack
resolves LIFO, so the token entry resolved first and the bounce entry's intervening-if then saw 5 Thopters and
skipped the bounce. ONE `ConditionalEffect` wrapping ONE `SequenceEffect` keeps it a single atomic stack entry
(what CR 603 requires), condition checked once, both steps spliced in oracle order.

### What changed
- **`ThopterAssembly.java`** — same `EffectSlot.UPKEEP_TRIGGERED` + same
  `ConditionalEffect(new NoOtherPermanent(new PermanentHasSubtypePredicate(THOPTER)), ...)`; the wrapped effect is
  now `SequenceEffect.of(ReturnToHandEffect.self(), new CreateTokenEffect(5, "Thopter", 1, 1, null,
  List.of(THOPTER), Set.of(FLYING), Set.of(ARTIFACT)))`. Bounce step first (oracle order). The `CreateTokenEffect`
  is byte-identical to the one the deleted record wrapped; imports updated (added `CreateTokenEffect`,
  `ReturnToHandEffect`, `SequenceEffect`; dropped `ReturnSelfToHandAndCreateTokensEffect`).
- **Behavior-preservation check**: the deleted handler bounced via `permanentRemovalService.removePermanentToHand`
  (skipping silently if the source was gone) then `applyCreateToken(controllerId, tokenEffect, setCode)`.
  `ReturnToHandEffect.self()` → `BounceSupport.applyReturnSelfToHand` is that exact bounce (logs "no longer on the
  battlefield" and returns when the source is gone — a returning handler cannot stop later spliced steps anyway,
  since they are independent list entries). `CreateTokenEffectHandler` calls the identical
  `applyCreateToken(controllerId, e, amount=Fixed(5), setCode)` (plus a benign `entry.getCreatedPermanentIds()`
  add). Token output identical.
- **Deleted** `magical-vibes-domain/.../model/effect/ReturnSelfToHandAndCreateTokensEffect.java` and
  `magical-vibes-engine/.../service/effect/normalfx/ReturnSelfToHandAndCreateTokensEffectHandler.java`.

### Greps run (whole-repo *.java, build/ excluded)
- `ReturnSelfToHandAndCreateTokensEffect` in `*.java` → zero (all references gone).
- `magical-vibes-engine/**/validate/` → no `@ValidatesTarget` for the record (nothing to delete).
- `magical-vibes-ai` → zero (no bespoke scoring; the `SpellEvaluator` `SequenceEffect` sum branch covers the parts).
- `ReturnSelfToHandAndCreateTokensEffectHandlerTest` → does not exist (nothing to port/delete).
- `ThopterAssemblyTest` → no white-box `instanceof`/reflective references to the record; all 7 tests are behavioral.
- Remaining `.md` hits: `agent-docs/` rewritten (below); `refactor-docs/` history untouched.

### Docs
- `agent-docs/EFFECTS_INDEX.md` — deleted the `ReturnSelfToHandAndCreateTokensEffect` row.
- `agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md` — Thopter Assembly row rewritten to
  `ConditionalEffect(NoOtherPermanent(...), SequenceEffect.of(ReturnToHandEffect.self(), CreateTokenEffect(...)))`,
  noting the trigger-slot atomicity requirement and the "tokens still created if source gone" ruling.

### Tests
- `ThopterAssemblyTest` — PASS (7 tests; baseline green before the change, still green after). Confirmed the two
  gotcha cases: `stillCreatesTokensIfDestroyedBeforeResolution` (tokens created, no bounce) and the trigger tests
  asserting one stack entry + the Assembly returning to hand.
- Full suite not run (per CLAUDE.md — user runs it).

### Traps hit
- None. Single-entry atomicity held: `gd.stack` size 1 on the fire tests, intervening-if re-checked once, both
  steps spliced and resolved in order.

---

## Batch 3 Session 4 — drain composite part 1: nine unwrapped users migrated

Migrated the **nine unwrapped card users** of `TargetPlayerLosesLifeAndControllerGainsLifeEffect(lifeLoss,
lifeGain)` onto the decomposed pair `LoseLifeEffect(N, TARGET_PLAYER)` + `GainLifeEffect(N)`. **The record,
its handler, its `@ValidatesTarget` entry in `LifeTargetValidators`, the two wrapper-blocked users
(`BleakCovenVampires`, `ArnynDeathbloomBotanist`) and the dynamic construction site in
`SpellCastTriggerCollectorService` were NOT touched** — deletion is the next session's job (this is part 1 of 2).
Nothing deleted; no agent-docs edited (the record still exists).

### Semantics verified before touching any card (STEP 0)
Read `TargetPlayerLosesLifeAndControllerGainsLifeEffectHandler`: it does exactly (a) target loses `lifeLoss`
applied **inline** via the same `loseTargetPlayerLife` body as `LoseLifeEffectHandler`'s `TARGET_PLAYER` branch —
respects `canPlayerLifeChange`, **does NOT fire "loses life" triggers** (known pre-existing quirk, preserved); and
(b) `if (lifeGain > 0) lifeSupport.applyGainLife(controllerId, lifeGain)`. No contingency, no "if you do", no extra
triggers/logs. The decomposition reproduces this: `LoseLifeEffect(N, TARGET_PLAYER)` hits the identical loss body;
`GainLifeEffect(N)` gains the nominal fixed amount (independent of actual loss); the gain is **omitted entirely**
when `lifeGain == 0` (matches the handler's `>0` guard). `SequenceEffect.targetSpec()` delegates to its first
targeting step → `LoseLifeEffect(TARGET_PLAYER).targetSpec()` = `TargetSpec.benign(PLAYER)` = the composite's own
`targetSpec()`, so trigger-slot targeting is byte-preserved and **no trigger collector needed changing**.

### One discovered nuance (recorded, NOT a stop — unobservable today, strictly more rules-correct where it applies)
The composite gains via `lifeSupport.applyGainLife(gd, controllerId, lifeGain)` (3-arg overload → passes
`sourceCard=null, sourceEntryType=null` to `checkLifeGainTriggers`). The decomposed `GainLifeEffectHandler` gains
via the 6-arg overload, threading `entry.getCard()` + `entry.getEntryType()` into the life-gain trigger context.
The **only** consumer of those fields is `MiscTriggerCollectorService.handleLifeGainDealDamageOnSpell`
(`DealDamageOnSpellLifeGainEffect`, the spell-lifelink "deal N when you gain life from a [color] instant/sorcery"
trigger). So for the three **spell** users (Syphon Life, Soul Feast, Morsel Theft) the decomposition *could* fire
that trigger where the composite would not. In practice it cannot with the current pool: the sole
`DealDamageOnSpellLifeGainEffect` card is **Firesong and Sunspeaker** (`triggeringColor = WHITE`) and all three
drain spells are **black**, so the color check fails either way → identical behavior. The six ability/trigger users
pass a non-spell `sourceEntryType`, so `handleLifeGainDealDamageOnSpell` short-circuits identically to null. Where
this could ever manifest (a hypothetical black-triggering card) the decomposition is *more* rules-correct — a drain
spell's life gain genuinely IS "life gained from a [black] spell." Consistent with the Session 2/3 "more-correct
consistency change, verified green, proceed" precedent. The loss side is byte-identical.

### Per-card shape chosen (each card's exact x/y preserved)
FLAT PAIR (spell/ability list — one stack entry, flat effects; Drain Life / Ajani Vengeant −2 precedent):
1. `s/SyphonLife` (2,2) — SPELL, no explicit `target(...)`: `addEffect(SPELL, LoseLifeEffect(2, TARGET_PLAYER))` +
   `addEffect(SPELL, GainLifeEffect(2))`. Retrace casting option left untouched.
2. `s/SoulFeast` (4,4) — SPELL: same flat pair with 4/4.
3. `m/MorselTheft` (3,3) — SPELL with explicit `target(PlayerRelation.ANY)` chain: the `LoseLifeEffect(3,
   TARGET_PLAYER)` stays bound to that target group (`.addEffect` on the `target(...)` builder); `GainLifeEffect(3)`
   added as a plain (`TargetSpec.NONE`) SPELL effect. Prowl `AlternateHandCast` and the `ConditionalEffect(
   CastForProwlCost, DrawCardEffect)` draw left untouched.
4. `b/BlightKeeper` (4,4) — activated ability `List.of(...)`: `SacrificeSelfCost()`, `LoseLifeEffect(4,
   TARGET_PLAYER)`, `GainLifeEffect(4)`.
5. `n/NecrogenCenser` (2,0) — activated ability: `LoseLifeEffect(2, TARGET_PLAYER)` **ONLY**, no `GainLifeEffect`
   (lifeGain == 0 → handler skipped the gain). `RemoveChargeCountersFromSourceCost` kept.

SEQUENCEEFFECT (trigger slots — one atomic entry per slot; `SequenceEffect.of(lose, gain)`):
6. `f/FalkenrathNoble` (1,1) — `SequenceEffect.of(LoseLifeEffect(1, TARGET_PLAYER), GainLifeEffect(1))` on **BOTH**
   `ON_DEATH` and `ON_ANY_CREATURE_DIES`.
7. `h/HighwayRobber` (2,2) — `ON_ENTER_BATTLEFIELD` SequenceEffect.
8. `h/HierophantsChalice` (1,1) — `ON_ENTER_BATTLEFIELD` SequenceEffect. `{T}: Add {C}` ability left untouched.
9. `p/PollutedBonds` (2,2) — `ON_OPPONENT_LAND_ENTERS_BATTLEFIELD` SequenceEffect (targetId auto-set by the
   collector to the land's controller; `SequenceEffect.targetSpec()` = benign(PLAYER) as before).

### Tests run (each via `.\scripts\run-card-test.ps1`, all PASS)
`SyphonLifeTest` (4), `SoulFeastTest` (5), `MorselTheftTest` (3), `BlightKeeperTest` (5), `NecrogenCenserTest` (6),
`FalkenrathNobleTest` (4 — confirms SequenceEffect-on-trigger-slot still prompts/auto-selects the target correctly),
`HighwayRobberTest` (6), `HierophantsChaliceTest` (6), `PollutedBondsTest` (2). Full suite not run (per CLAUDE.md).

### Whole-repo grep (build/ excluded)
`new TargetPlayerLosesLifeAndControllerGainsLifeEffect` now appears ONLY in `SpellCastTriggerCollectorService.java`,
`BleakCovenVampires.java`, `ArnynDeathbloomBotanist.java` (plus this progress doc) — every unwrapped user migrated.
The record, handler, `LifeTargetValidators` entry, and `SpellCastLifeDrainEffect` reference remain in place for the
next session to delete once the two wrapper-blocked users and the dynamic site are folded.

### Traps hit
- None. The gain-trigger-context nuance above was investigated to ground (traced `checkLifeGainTriggers` →
  `TriggerContext.LifeGain` → sole `DealDamageOnSpellLifeGainEffect` consumer = Firesong/WHITE) and found
  unobservable + more-correct, so not a stop-condition. Loss side byte-identical; no collector changed.

## Batch 3 Session 5 — drain composite deleted (wrapper users + collector site migrated)

Folded the **three remaining construction sites** of `TargetPlayerLosesLifeAndControllerGainsLifeEffect(lifeLoss,
lifeGain)` onto the decomposed pair `LoseLifeEffect(N, TARGET_PLAYER)` + `GainLifeEffect(M)` (gain omitted when M = 0),
then **deleted the record, its handler, and its `@ValidatesTarget` registration**. The record is now gone repo-wide
(only refactor-docs history retains the name).

### The three sites
- **`b/BleakCovenVampires`** (ETB, `ConditionalEffect` wrapper) — `ConditionalEffect(new Metalcraft(),
  SequenceEffect.of(new LoseLifeEffect(4, TARGET_PLAYER), new GainLifeEffect(4)))`. At resolution
  `EffectResolutionService` re-checks the intervening-if, unwraps the `ConditionalEffect` to the `SequenceEffect`,
  which splices its two steps into the entry's live effect list; the entry's cast-time PLAYER target (spec unchanged —
  `ConditionalEffect.targetSpec()` → `SequenceEffect.targetSpec()` → first non-NONE = the `TARGET_PLAYER` loss = benign
  PLAYER) feeds `entry.getTargetId()` for the loss, controller gains the fixed 4.
- **`a/ArnynDeathbloomBotanist`** (two death slots, `TriggeringPermanentConditionalEffect` wrapper) — `DRAIN` static
  field is now `SequenceEffect.of(new LoseLifeEffect(2, TARGET_PLAYER), new GainLifeEffect(2))`, still wrapped in
  `TriggeringPermanentConditionalEffect(power/toughness≤1, DRAIN)` on `ON_ALLY_CREATURE_DIES` + `ON_DEATH`. Verified the
  unwrap path: `unwrapCreatureDeathConditional` evaluates the predicate against the dying permanent and returns
  `conditional.wrapped()` (the `SequenceEffect`); because that effect's `targetSpec()` includes players, the death
  pipeline routes it through `PermanentChoiceContext.DeathTriggerTarget`. `TriggerTargetCollector.collect` derives
  `opponentOnly` from the **card-level** `dyingCard.getTargetFilter()` (Arnyn's opponent-only `PlayerRelationPredicate`),
  independent of the wrapped effect object — so the "target opponent" restriction is preserved (the pre-existing code
  already passed a distinct unwrapped object, proving identity-independence). Chosen opponent → `entry.getTargetId()` →
  loss; controller gains 2.
- **`SpellCastTriggerCollectorService.handleOpponentSpellCastDrain`** (dynamic, Yawgmoth's Edict via
  `SpellCastLifeDrainEffect`) — the built effect fed a **single stack entry's `List<CardEffect>`**, so replaced with the
  two flat effects added to that same list: `LoseLifeEffect(trigger.lifeLoss(), TARGET_PLAYER)` always, plus
  `GainLifeEffect(trigger.lifeGain())` guarded on `lifeGain() > 0`. Entry still `setTargetId(castingPlayerId)` +
  `setNonTargeting(true)`, so the loss lands on the casting opponent with no target choice.

### Deletions + doc
- Deleted `model/effect/TargetPlayerLosesLifeAndControllerGainsLifeEffect.java` and
  `service/effect/normalfx/TargetPlayerLosesLifeAndControllerGainsLifeEffectHandler.java`.
- Removed `validateTargetPlayerLosesLifeAndControllerGainsLife` (+ import) from `LifeTargetValidators`; the existing
  `@ValidatesTarget(LoseLifeEffect.class)` guard (gated on `recipient == TARGET_PLAYER`) already covers the decomposed
  users.
- Rewrote `SpellCastLifeDrainEffect`'s javadoc to describe the decomposed build instead of the deleted resolver.
- Semantics unchanged from Session 4: the `TARGET_PLAYER` loss is applied inline via `loseTargetPlayerLife` (respects
  `canPlayerLifeChange`, **does not fire "loses life" triggers** — pre-existing quirk, preserved); the `GainLifeEffect`
  gains the nominal fixed amount for the controller.

### Traps checklist
- Whole-repo grep for the record name: hits only in `refactor-docs/*.md` (history) — zero `.java`, zero `magical-vibes-ai`.
- No `TargetPlayerLosesLifeAndControllerGainsLifeEffectHandlerTest` existed (nothing to port/delete).
- No card-test white-box references (imports/instanceof) existed.
- `agent-docs` rewritten to the composed pattern: `EFFECTS_INDEX.md` (new `_compose, no dedicated effect_` row + the
  `SpellCastLifeDrainEffect` row), `EFFECTS_QUICK_REFERENCE.md`, `ORACLE_TEXT_EFFECT_MAP.md` (trigger row + a new direct
  spell-form "loses N life and you gain N life" row), `CARD_PATTERNS_CREATURES_ETB.md` (Bleak Coven),
  `CARD_PATTERNS_PERMANENTS_ARTIFACTS.md` (Necrogen Censer, loss-only).

### Tests run (per CLAUDE.md, full suite not run)
`BleakCovenVampiresTest` (10), `ArnynDeathbloomBotanistTest` (3), `YawgmothsEdictTest` (collector site card),
and regression `SyphonLifeTest`, `FalkenrathNobleTest`, `NecrogenCenserTest` (three Session-4 users covering all three
shapes) — all green.

### Traps hit
- None. Both wrappers unwrap to their inner effect **before** resolution, so once unwrapped the `SequenceEffect`
  resolves identically to the Session-4 flat-list users; targeting is driven by the card-level filter + the effect's
  (unchanged) `targetSpec()`, not the wrapped object's identity.
