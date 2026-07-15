# TargetSpec migration — progress log

Permanent record of the 11-step program that replaces the per-effect targeting
methods on `CardEffect`
(`magical-vibes-domain/.../model/effect/CardEffect.java`) with a single
declarative `TargetSpec`, then deletes the legacy methods. Each step runs in a
fresh session; this file is the hand-off between them. **Read it first.**

## Program overview (11 steps)

Anchored facts: step 1 is the read-only audit; step 2 introduces `TargetSpec` +
`targetSpec()` and rewires `CardEffect`'s legacy defaults to derive from it (plus
the ratchet test); step 10 deletes the legacy methods; step 11 is close-out. The
middle migration steps (3–9) are the audit's **proposed** bucket decomposition,
derived from the record counts in `TARGETSPEC_MATRIX.md` — a later prompt may
re-sequence them, but every record listed in the matrix must be migrated exactly
once and the ratchet baseline must shrink monotonically.

1. **Audit** — build `scripts/targetspec-audit.py`, `TARGETSPEC_MATRIX.md`,
   `targetspec-baseline.txt`, and this file (read-only; no production source
   touched).
2. **TargetSpec + ratchet** — add the immutable `TargetSpec` record and the
   derived `CardEffect.targetSpec()` interface method; make each legacy default
   derive from `targetSpec()` (so an unmigrated per-record override still wins);
   add the ratchet test that parses `targetspec-baseline.txt` and fails when any
   effect file's legacy-override count exceeds its baseline.
3. **Permanent-only, validated** (subset of 111) — records whose validator picks
   the category (`CREATURE` / `PERMANENT` / `LAND`); migrate onto `targetSpec()`,
   deleting their legacy overrides, keeping any escape-hatch validator whole.
4. **Permanent-only, unvalidated** (rest of 111) — reproduce the `canTarget*`
   booleans EXACTLY (no narrowing without a web ruling + a note here).
5. **Player + permanent** (29, the any-target family) — `ANY_TARGET` /
   `PLAYER_OR_PLANESWALKER` / `PLAYER_OR_PERMANENT`; several carry conditional
   overrides (per-instance specs).
6. **Player-only** (83) — mostly `PLAYER`; validators are rare here (player split
   is structural).
7. **Spell-on-stack + exile** (17 + 1) — `SPELL_ON_STACK` (validated on the
   stack path, so no `@ValidatesTarget`) and the one `EXILE_CARD`.
8. **Graveyard + any-graveyard** (7 + 7) — `GRAVEYARD_CARD` /
   `ANY_GRAVEYARD_CARD`; most keep an escape-hatch validator (own/opponent
   graveyard = controller comparison).
9. **Metadata-only + structural wrappers** (19 + 3) — effects overriding only
   `isSelfTargeting` / `isDamageOrDestruction` / `targetPredicate` /
   `requiredPlayerTargetCount` (spec `NONE` + flags), and the delegating wrappers
   (`ConditionalEffect`, `ConditionalReplacementEffect`, `MayEffect`) whose
   `targetSpec()` delegates to the inner effect.
10. **Delete legacy methods** — remove `canTargetPlayer`, `canTargetPermanent`,
    `canTargetSpell`, `canTargetGraveyard`, `canTargetAnyGraveyard`,
    `targetsControllersGraveyardOnly`, `canTargetExile`, `targetPredicate`,
    `isSelfTargeting`, `requiredPlayerTargetCount`, `isDamageOrDestruction` from
    `CardEffect` and every remaining override. `@ValidatesTarget` survives only
    as the escape hatch for non-structural logic.
11. **Close-out** — re-run the audit, tighten/retire the ratchet baseline, update
    `agent-docs/`.

## END STATE

- The 11 legacy targeting methods listed in step 10 are **deleted** from
  `CardEffect`; every effect exposes its targeting through one derived
  `TargetSpec targetSpec()`.
- **`isPowerToughnessDefining()` is KEPT** — it is a copy/CDA rule (CR 707.9d),
  not a targeting method, and is out of scope for this program.
- `@ValidatesTarget` validators survive ONLY where they encode logic beyond
  target-type structure (see the escape-hatch list in `TARGETSPEC_MATRIX.md`).
- `targetspec-baseline.txt` shrinks to empty (or the ratchet is retired) once all
  277 in-scope records are migrated.

## Confirmed `TargetCategory` enum (audit output)

All 13 proposed values are used by ≥1 effect (none dropped); the data demanded no
new value. Counts (non-wrapper effects), from `scripts/targetspec-audit.py`:

`NONE` 19 · `PLAYER` 83 · `PLAYER_OR_PERMANENT` 19 · `PERMANENT` 68 ·
`CREATURE` 41 · `LAND` 1 · `CREATURE_OR_PLANESWALKER` 1 ·
`PLAYER_OR_PLANESWALKER` 3 · `ANY_TARGET` 7 · `SPELL_ON_STACK` 17 ·
`GRAVEYARD_CARD` 7 · `ANY_GRAVEYARD_CARD` 7 · `EXILE_CARD` 1.

Fine-grained narrowing (artifact-only, nonland, subtype) is expressed with the
spec's `PermanentPredicate` field, NOT a new enum value. For **validated**
effects the final category is read off the kept validator during migration; the
matrix value is the audit's inference from the validator body. For
**unvalidated** effects the category must reproduce the `canTarget*` booleans
exactly.

## Shared invariants

These rules are VERBATIM from the program brief; later steps rely on reading them
here.

- **BEHAVIOR-PRESERVING-OR-STRICTER.** The oracle for an effect's legal target
  set is its existing @ValidatesTarget method. For effects WITHOUT a validator,
  the spec must reproduce the current canTarget* booleans EXACTLY — no
  narrowing. Narrowing an unvalidated effect requires checking the official card
  ruling on the web and a note in this file.
- **MIGRATION MECHANICS.** CardEffect's legacy defaults will derive from
  targetSpec() (step 2). A per-record override always wins over an interface
  default, so unmigrated records are unaffected. Migrating a record means: add
  the targetSpec() override AND delete ALL legacy targeting overrides on that
  record in the same edit. Never leave a record half-migrated.
- **KEPT VALIDATORS ARE KEPT WHOLE.** If a validator has any non-structural
  logic, keep the entire method unchanged (redundant structural re-checks are
  harmless). Never partially edit a kept validator.
- **TargetSpec is an immutable record.** targetSpec() is a derived interface
  method — NEVER a record component (effects are Jackson-serialized by
  component; adding a component changes wire format and equals()).
- **HARMFUL FLAG POLICY:** harmful=true exactly when the effect's validator
  calls tvs.checkProtection (damage / fight / destroy / exile / sacrifice
  families). Do not add protection checks the old validator did not do.
- **TEST COMMANDS.** Run tests only via .\scripts\run-card-test.ps1 — bare
  names are treated as cards.{letter}.{Name} card tests; everything else needs
  the fully qualified class name. Fixed regression set to run at the end of
  every migration step: WrackWithMadnessTest, DarkNourishmentTest, FireballTest,
  com.github.laxika.magicalvibes.architecture.EffectDispatchRatchetTest, the
  TargetSpec ratchet test (from step 2),
  com.github.laxika.magicalvibes.service.target.ValidTargetServiceTest, and
  TargetLegalityServiceTest (same package). Additionally run at least one card
  test per migrated effect (grep cards/ for the record name to find one); if an
  effect has no card test, say so in the progress entry.
- Every step appends a "## Step N" entry here: what migrated, what was kept as
  escape hatch and why, oracle judgment calls, tests run, whether
  scripts/targetspec-audit.py was re-run to regenerate the baseline.
- Do not regenerate refactor-docs/effect-dispatch-baseline.txt (the OTHER
  ratchet) unless you actually changed instanceof-on-effect counts.

---

## Step 1 — Audit  (2026-07-15)

**Deliverables produced (no production `src/main` source touched):**
- `scripts/targetspec-audit.py` — no-argument, deterministic, idempotent,
  pure-stdlib (`re`, `collections`, `pathlib`) scanner. Run from the repo root as
  `python scripts/targetspec-audit.py` (Python 3.14 on PATH). Verified idempotent
  (two consecutive runs produce byte-identical outputs). Regenerates the two
  files below on every run.
- `refactor-docs/TARGETSPEC_MATRIX.md` — the human worklist: legacy-method tally,
  the confirmed `TargetCategory` table with per-value counts, one worklist table
  per bucket (with a `Done` checkbox column the migration steps tick), the
  escape-hatch section, and a validator index proving all 124 `@ValidatesTarget`
  methods are accounted for exactly once.
- `refactor-docs/targetspec-baseline.txt` — machine-readable ratchet baseline,
  one `path=count` line per in-scope effect file (count = number of distinct
  legacy methods overridden), sorted alphabetically by path. **Format is a
  contract** — step 2's ratchet test parses it.

**Scope rule.** A record is in scope iff it provides an own override of ≥1 of the
11 legacy targeting methods (isPowerToughnessDefining excluded). Structural
wrappers that override by delegation (`ConditionalEffect`,
`ConditionalReplacementEffect`, `MayEffect`) are kept IN the baseline — they, too,
must migrate (their `targetSpec()` will delegate) and the ratchet recomputes the
same way — but they are bucketed separately and carry no fixed category.

**Headline numbers:**
- **Records in scope: 277.** Sum of baseline counts = **385** legacy
  method-overrides.
- Legacy-method override counts (reproduced exactly from the brief):
  canTargetPermanent **146**, canTargetPlayer 118, canTargetSpell 20,
  canTargetGraveyard 19, canTargetAnyGraveyard 8, canTargetExile 1,
  targetsControllersGraveyardOnly 2, targetPredicate 5, isSelfTargeting 25,
  requiredPlayerTargetCount 2, isDamageOrDestruction 39.
- **@ValidatesTarget methods: 124** across the 10 validator files (counts match
  the brief exactly). **122** of the 277 in-scope records have a validator; the
  remaining **2** validators (`StaticBoostEffect`, `AttachedBoostEffect` in
  `CreatureModTargetValidators`) validate effects that override NO legacy method
  (they target via the equip/attach mechanism) — both appear in the validator
  index with "In a bucket table? = no".
- **53** in-scope records have ≥1 CONDITIONAL override (body reads record
  components / branches on scope) — these need per-instance specs computed by the
  migration step, and are flagged in the matrix with a `*` after the method name.
- **Escape-hatch validators: 10** (validators with logic beyond target-type
  structure — must be kept whole).

**Bucket sizes:** permanent-only 111 · player+permanent 29 · player-only 83 ·
spell-on-stack 17 · graveyard 7 · graveyard+any 7 · exile 1 · metadata-only 19 ·
structural wrappers 3.

**Escape-hatch inventory (kept validators, with the non-structural signal that
disqualifies a declarative spec and whether the validator is harmful):**
- `DealDamageToTargetOpponentOrPlaneswalkerEffect` — opponent-relation +
  controller-compare — **harmful**.
- `DealDividedDamageEffect` — null-target tolerance (CHOSEN/ETB carry targets
  outside `targetId`) — **harmful**.
- `PreventDividedDamageEffect` — null-target tolerance — not harmful.
- `ReturnCardFromGraveyardEffect`, `GrantFlashbackToTargetGraveyardCardEffect`,
  `ExileTargetCardFromGraveyardAndCreateTokenCopyEffect`,
  `ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect`,
  `PlayTargetCardFromGraveyardWithoutPayingManaCostEffect` — controller-compare
  ("from your graveyard") — not harmful.
- `ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect`,
  `PutCardFromOpponentGraveyardOntoBattlefieldEffect` — opponent-relation +
  controller-compare ("from an opponent's graveyard") — not harmful.

**Oracle / methodology judgment calls (double-check in later steps):**
- **Category inference is validator-driven for validated effects.** The
  `canTarget*` booleans cannot distinguish CREATURE vs PERMANENT vs LAND (all are
  `canTargetPermanent=true`); the audit refines the category by scanning the
  validator body (`requireCreature`/`isCreature(` → CREATURE, `CardType.LAND` →
  LAND, planeswalker/player splits → the mixed values). This is the audit's
  inference; the migration step must confirm it against the kept validator. For
  UNVALIDATED effects the category is coarse-from-booleans and must NOT be
  narrowed without a web ruling + a note here.
- **`BecomeCreatureTypeWithBasePowerToughnessEffect`** redundantly overrides
  `canTargetPermanent()` to `return false;` (the interface default) and nothing
  else. It targets nothing, so it is bucketed `metadata-only` / category `NONE`;
  it stays in the baseline (the override must still be deleted at migration). The
  audit prints a WARNING naming it.
- **`DealDamageToPlayersEffect`** overrides both `canTargetPlayer` and
  `canTargetPermanent` CONDITIONALLY (by `recipient`: TARGET_PLAYER →
  player, TARGET_PERMANENT_CONTROLLER → permanent). Its validator is structural
  (requires a player only when recipient is TARGET_PLAYER), so it is NOT an
  escape hatch — its per-instance spec is computed from `recipient`.
- **All 13 proposed `TargetCategory` values are used**; none dropped, none added.

**Tests run:** none — this step touches no production source (audit + docs only),
so nothing to compile or regress. `scripts/targetspec-audit.py` was run (and
re-run for idempotence) to generate `TARGETSPEC_MATRIX.md` and
`targetspec-baseline.txt`. `effect-dispatch-baseline.txt` (the OTHER ratchet) was
NOT touched — no instanceof-on-effect counts changed.

**Sanity checks (all pass):**
- Sum of baseline counts (385) ≥ 146 (canTargetPermanent alone). ✓
- All 124 `@ValidatesTarget` methods appear exactly once in the matrix (validator
  index). ✓
- Spot-checked 5 rows against record + validator source: `DealDamageToTarget-
  CreatureEffect` → CREATURE, `DealDamageToAnyTargetEffect` → ANY_TARGET,
  `DealDamageToTargetPlayerOrPlaneswalkerEffect` → PLAYER_OR_PLANESWALKER,
  `AnimatePermanentsEffect` (conditional `scope==TARGET`) → PERMANENT flagged,
  `ReturnTargetCardFromExileToHandEffect` → EXILE_CARD. ✓

---

## Step 2 — TargetSpec + ratchet + 3 pilots  (2026-07-15)

**Part A — domain machinery (`magical-vibes-domain/.../model/effect/`):**
- `TargetCategory` enum — the confirmed 13 values, each carrying
  `includesPermanents()` / `includesPlayers()` booleans so step 10 can retire
  `canTargetPermanent` / `canTargetPlayer` through them. Mapping (perm, player):
  NONE(f,f), PLAYER(f,t), PLAYER_OR_PERMANENT(t,t), PERMANENT(t,f), CREATURE(t,f),
  LAND(t,f), CREATURE_OR_PLANESWALKER(t,f), PLAYER_OR_PLANESWALKER(t,t),
  ANY_TARGET(t,t), SPELL_ON_STACK(f,f), GRAVEYARD_CARD(f,f), ANY_GRAVEYARD_CARD(f,f),
  EXILE_CARD(f,f). These reproduce today's `canTarget*` booleans per bucket exactly.
- `TargetSpec(category, harmful, predicate, selfTargeting, playerTargetCount)`
  record — immutable, in the effect package. Factories: `TargetSpec.NONE`,
  `harmful(cat)`, `benign(cat)`, `harmful(cat, predicate)`, `benign(cat, predicate)`
  (the withPredicate variants). Defaults: harmful false, predicate null,
  selfTargeting false, playerTargetCount 1.
- `CardEffect.targetSpec()` added as a derived default (`TargetSpec.NONE`); the 11
  legacy targeting defaults now DERIVE from it: canTargetPlayer →
  `category().includesPlayers()`, canTargetPermanent → `...includesPermanents()`,
  canTargetSpell → `category == SPELL_ON_STACK`, canTargetGraveyard →
  `GRAVEYARD_CARD || ANY_GRAVEYARD_CARD`, canTargetAnyGraveyard →
  `ANY_GRAVEYARD_CARD`, canTargetExile → `EXILE_CARD`, targetPredicate →
  `predicate()`, isSelfTargeting → `selfTargeting()`, requiredPlayerTargetCount →
  `playerTargetCount()`, isDamageOrDestruction → `harmful()`.
  `isPowerToughnessDefining` untouched. For `TargetSpec.NONE` every derived value
  equals the historical default, and a per-record override still wins over the
  interface default, so nothing changes for unmigrated records.
  - **Deviation (documented):** `targetsControllersGraveyardOnly` has NO
    `TargetCategory` correlate — both controller-only and opponent-only "return a
    card from a graveyard" effects are `GRAVEYARD_CARD`, and `TargetSpec` has no
    field for it (the record signature is fixed). It is kept as a constant-`false`
    default (still equals today's default for NONE). The only 2 effects that set it
    true (`GrantFlashbackToTargetGraveyardCardEffect`,
    `PlayTargetCardFromGraveyardWithoutPayingManaCostEffect`) keep their own
    override; the graveyard bucket (step 8) must resolve how they express it before
    step 10 deletes the method.

**Part B — engine spec interpreter (`TargetValidationService`):**
- Added a `PredicateEvaluationService` constructor dependency (Spring-injected
  everywhere; `TargetValidationService` is never `new`-ed — the harness/AI get it
  via `context.getBean`, so no manual wiring site needed updating; no dependency
  cycle: `PredicateEvaluationService` depends only on `GameQueryService`).
- `checkEffectTargets` (the Optional-returning core the throwing overload
  delegates to) now, for the SAME effect the registry lookup selects (after the
  existing `ConditionalReplacementEffect` unwrap): if
  `effect.targetSpec().category() != NONE`, runs a new private `validateSpec(ctx,
  spec)` FIRST, then the registered class validator (if any) SECOND. Both are in
  one try/catch that maps `IllegalStateException` → `Optional.of(message)`. Living
  in the service (not a scanned `@ValidatesTarget` bean) guarantees every context —
  including the AI simulator that builds the registry via `scanBean` outside
  Spring — gets the interpreter.
- `validateSpec` semantics mirror the hand validators exactly: CREATURE =
  requireBattlefieldTarget + requireCreature (layer-aware via
  `gameQueryService.isCreature`); CREATURE_OR_PLANESWALKER / ANY_TARGET /
  PLAYER_OR_PLANESWALKER copied from the corresponding `DamageTargetValidators`
  bodies (including the `playerIds.contains` pre-check for the player-capable
  ones); PERMANENT = requireBattlefieldTarget only; LAND = battlefield + hasType
  LAND; PLAYER / PLAYER_OR_PERMANENT / SPELL_ON_STACK / graveyard / exile do no
  permanent-type check (guarded by their own paths). Then, on a permanent target
  only: if `predicate() != null`, require
  `predicateEvaluationService.matchesPermanentPredicate`; if `harmful()`, run
  `checkProtection`.

**Part C — ratchet + lockstep:**
- `TargetSpecRatchetTest` (application `architecture/`) re-implements the audit's
  per-file legacy-override counting (same 11 methods, same
  `public <boolean|int|PermanentPredicate> name()` regex, count = distinct
  methods), parses `targetspec-baseline.txt`, and fails on any increase (names the
  file + overrides present + "declare a TargetSpec instead") and any decrease
  ("regenerate the baseline with python scripts/targetspec-audit.py"). Lockstep
  contract comment added to BOTH the test and `scripts/targetspec-audit.py`.
- `instanceof TargetSpec` WOULD count as a coupling violation (TargetSpec is a
  record in the effect package, not a wrapper/interface), so `TargetSpec` and
  `TargetCategory` were added to the structural-wrapper exemption list in BOTH
  `scripts/effect-coupling-audit.py` and `EffectDispatchRatchetTest` (in lockstep).
  No `instanceof TargetSpec`/`instanceof TargetCategory` exists in code, so
  `effect-dispatch-baseline.txt` was NOT regenerated (instanceof-on-effect counts
  unchanged).
- Verified the ratchet passes clean, then verified it CATCHES a violation:
  temporarily added `canTargetPermanent()` to `GainLifeEffect` (count 1→2) → test
  failed with "Legacy targeting overrides grew in …/GainLifeEffect.java (was 1, now
  2). Overrides present: canTargetPermanent, canTargetPlayer. … declare a
  TargetSpec instead" → reverted (file clean).

**Part D — 3 pilots migrated end to end** (all three verified purely structural
by reading their validators first — requireBattlefieldTarget/requireCreature/
checkProtection only, no opponent/controller/chosen-source/null-tolerance logic):
- `DealDamageToTargetCreatureEffect` → `targetSpec() = harmful(CREATURE)`; deleted
  `canTargetPermanent` + `isDamageOrDestruction` overrides and validator
  `validateDealDamageToTargetCreature`.
- `DealDamageToAnyTargetEffect` → `harmful(ANY_TARGET)`; deleted `canTargetPlayer`
  + `canTargetPermanent` + `isDamageOrDestruction` and validator
  `validateDealDamageToAnyTarget`.
- `DestroyTargetPermanentEffect` → `harmful(PERMANENT)`; deleted
  `canTargetPermanent` + `isDamageOrDestruction` and validator
  `validateDestroyTargetPermanent`.
- Unused effect imports pruned from `DamageTargetValidators` /
  `DestructionTargetValidators`. The kept `validateDestroyTargetPermanentThen`
  comment updated to note the unwrapped base now validates via its PERMANENT spec.

**Audit re-run:** `python scripts/targetspec-audit.py` re-run to regenerate
`targetspec-baseline.txt` + `TARGETSPEC_MATRIX.md`. Records in scope 277 → **274**;
sum of baseline counts 385 → **378** (−7 = 2+3+2); canTargetPermanent overrides
146 → 143; `@ValidatesTarget` methods 124 → **121**; escape-hatch count still 10.
Category counts shifted CREATURE 41→40, ANY_TARGET 7→6, PERMANENT 68→67. The
hardcoded "must equal 124" phrasing in the audit's generated matrix/print was
relaxed to a moving-target note (validators shrink as structural ones retire).
`effect-dispatch-baseline.txt` NOT touched.

**Tests run** (via `./gradlew :magical-vibes-application:test --tests …`, all
green): fixed regression set — `WrackWithMadnessTest`, `DarkNourishmentTest`,
`FireballTest`, `EffectDispatchRatchetTest`, `TargetSpecRatchetTest`,
`ValidTargetServiceTest`, `TargetLegalityServiceTest`; new
`TargetValidationServiceSpecTest` (6 required cases: creature rejects land,
ANY_TARGET accepts player+creature / rejects land, harmful runs protection,
predicate narrowing both ways, NONE does nothing, spec + kept class validator both
run); one card test per pilot effect — `SpittingEarthTest`
(DealDamageToTargetCreatureEffect), `IncinerateTest` (DealDamageToAnyTargetEffect),
`TerrorTest` (DestroyTargetPermanentEffect).

**Oracle / judgment calls:** the 3 pilots' categories were confirmed against their
kept validator bodies before deletion (CREATURE / ANY_TARGET / PERMANENT, all
harmful because each validator called `checkProtection`). No card behavior narrowed;
no web ruling needed (behavior-preserving structural migration).

---

## Step 3 — Damage + Prevention validators  (2026-07-15)

Migrated every effect whose `@ValidatesTarget` method lives in
`DamageTargetValidators` (the 20 remaining after step 2's 2 pilots) or
`PreventionTargetValidators` (6) — 26 effects total. Each validator was read and
classified STRUCTURAL vs NON-STRUCTURAL before touching the record.

**Migrated onto `targetSpec()`, validator DELETED (22 structural):**
- `harmful(CREATURE_OR_PLANESWALKER)`: `DealDamageToTargetCreatureOrPlaneswalkerEffect`.
- `harmful(PLAYER_OR_PLANESWALKER)`: `DealDamageToTargetPlayerOrPlaneswalkerEffect`,
  `DealDamageToTargetAndTheirCreaturesEffect` (its validator is byte-identical to the
  player-or-planeswalker one: player accepted, else planeswalker + checkProtection).
- `harmful(ANY_TARGET)`: `DealDamageToAnyTargetAndGainLifeEffect`,
  `DealDamageToAnyTargetEqualToChosenTypeCountEffect`,
  `DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect`,
  `DealXDamageToAnyTargetAndGainXLifeEffect`,
  `MillControllerAndDealDamageByHighestManaValueEffect`.
- `harmful(CREATURE)`: `DealDamageToTargetControllerIfTargetHasKeywordEffect`,
  `DealDamageToTargetCreatureEqualToChosenTypeCountEffect`,
  `PlaneswalkerDealDamageAndReceivePowerDamageEffect`,
  `TargetCreatureDealsPowerDamageToSelfEffect`,
  `TargetCreatureDealsPowerDamageToControllerEffect`, `SourceFightsTargetCreatureEffect`.
- `benign(CREATURE)` (redirect validators explicitly do NOT call checkProtection):
  `RedirectNextDamageToTargetCreatureEffect`,
  `RedirectTargetCreatureDamageFromChosenSourceToSelfEffect`,
  `RedirectTargetCreatureNextDamageFromChosenSourceToControllerEffect`.
- `benign(ANY_TARGET)`: `PreventDamageToTargetEffect`,
  `PreventDamageToTargetFromChosenSourceEffect`.
- `harmful(ANY_TARGET)`: `PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect`
  (Harm's Way — redirects the damage ONTO its target, so its validator calls
  checkProtection).
- `benign(CREATURE)`: `PreventAllDamageToTargetCreatureEffect`,
  `PreventAllDamageByTargetCreatureEffect`.

Deleted the two private helpers in each file once their callers were gone
(`validateAnyDamageTarget` + `validateCreatureDamageTarget` in Damage;
`validateAnyTargetType` + `requireCreatureOrPlaneswalker` in Prevention) and pruned the
now-unused imports (17 effect imports from Damage; 5 effect imports + `CardType` from
Prevention). Both class javadocs rewritten to describe the remaining escape hatches.

**Escape hatch — spec carries the structural part, validator KEPT WHOLE (4):**
- `DealDamageToTargetOpponentOrPlaneswalkerEffect` → `harmful(PLAYER_OR_PLANESWALKER)`.
  Non-structural: opponent-relation + controller-compare (rejects the controller as a
  player target). The `PLAYER_OR_PLANESWALKER` spec accepts any player + planeswalker +
  checkProtection; the kept validator then narrows players to opponents. Not
  null-tolerant, so the target-requiring spec category is safe.
- `DealDividedDamageEffect` → `NONE` when `etbAssignments`, else
  `harmful(PLAYER_OR_PERMANENT)`. Non-structural: null-`targetId` tolerance (CHOSEN-mode
  targets ride `StackEntry.damageAssignments`). `PLAYER_OR_PERMANENT` is a **no-op** in the
  spec interpreter, so it does NOT call `requireTarget` and the null tolerance survives;
  the kept validator does the real per-target work. `etbAssignments` → `NONE` preserves
  the "collects targets via `pendingETBDamageAssignments`, takes no pipeline target"
  bypass (a non-`NONE` category would make ValidTargetService offer targets).
- `DealDamageToPlayersEffect` → per-recipient switch: `benign(PLAYER)` for
  `TARGET_PLAYER`, `benign(PERMANENT)` for `TARGET_PERMANENT_CONTROLLER`, `NONE`
  otherwise. **Deviation from step 1's note** ("NOT an escape hatch"): with the actual
  interpreter the `PLAYER` category is a no-op, so it cannot reproduce the validator's
  `requireTargetPlayer` guard for `TARGET_PLAYER`; deleting the validator would drop that
  guard (less strict — forbidden). So the validator is KEPT and the spec is computed
  per-recipient to reproduce the old conditional `canTargetPlayer`/`canTargetPermanent`
  booleans. Benign because the validator performs no checkProtection (the damage lands on
  a player, not the permanent). The audit's escape-hatch heuristic does not auto-flag it;
  it is an escape hatch by this deliberate decision.
- `PreventDividedDamageEffect` → `benign(PLAYER_OR_PERMANENT)`. Null-`targetId`-tolerant
  (Remedy, same CHOSEN pattern) → no-op category preserves the tolerance; validator kept.

**Oracle / judgment calls:**
- **HARMFUL FLAG applied by validator, not by pre-existing `isDamageOrDestruction`
  override.** Five migrated effects called `checkProtection` in their validator but did
  NOT override `isDamageOrDestruction` (it sat at the interface default `false`):
  `DealXDamageToAnyTargetAndGainXLifeEffect`, `MillControllerAndDealDamageByHighestManaValueEffect`,
  `DealDamageToTargetControllerIfTargetHasKeywordEffect`, `SourceFightsTargetCreatureEffect`,
  `PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect`. Per HARMFUL FLAG POLICY
  (harmful iff the validator calls `checkProtection`) their spec is `harmful`, so the
  derived `isDamageOrDestruction()` now returns `true`. Effect: ValidTargetService
  (`dealsDamageOrDestroys` at line ~500) now also filters protection-having permanents
  from the OFFERED target list. Final legality is unchanged (the kept-then-deleted
  validator already rejected them at cast); this only makes the offering consistent with
  the validator and is the more rules-correct behavior (a protected creature is not a
  legal target). Stricter-or-equal, allowed.
- **Prevention "any target" → `ANY_TARGET`, not the audit's coarse `PLAYER_OR_PERMANENT`
  inference.** `validatePreventDamageToTarget` / `…FromChosenSource` /
  `…AndRedirectToAnyTarget` enforce player OR (creature-or-planeswalker), which is exactly
  the interpreter's `ANY_TARGET`. `PLAYER_OR_PERMANENT` (a no-op) would wrongly widen the
  legal set to any permanent (artifact/enchantment), so category was read off the
  enforced type check per the "category from the type check" rule.
- **`DealDamageToPlayersEffect` `TARGET_PERMANENT_CONTROLLER` → `PERMANENT` adds a
  `requireBattlefieldTarget`** the old validator did not run for that recipient (it only
  branched on `TARGET_PLAYER`). Inert and stricter: all four cards using that recipient
  (Chandra's Outrage, Gloomlance, Lash Out, Fodder Launch) pair it with a
  target-creature damage effect, so the shared `targetId` is always a battlefield
  creature. `PERMANENT` preserves the old `canTargetPermanent()==true` boolean exactly.
- **`DealDividedDamageEffect` creature-only CHOSEN casts:** `canTargetPlayer()` derives
  `true` under `PLAYER_OR_PERMANENT` where the old conditional returned `false`
  (`canTargetPlayers==false`). Inert: those spells select targets through creature
  `multiTargetFilters` (not the boolean single-target path), and the kept validator still
  throws "cannot target players" for a player target. `canTargetPermanent` and
  `isDamageOrDestruction` are preserved exactly, and `etbAssignments → NONE` keeps the
  no-target bypass exact.
- No card behavior narrowed illegally; no web ruling needed (all category choices are
  either behavior-preserving or the documented stricter-and-more-correct offering filter).

**Audit re-run:** `python scripts/targetspec-audit.py` regenerated
`targetspec-baseline.txt` + `TARGETSPEC_MATRIX.md`. Records in scope 274 → **248**
(−26); sum of baseline counts 378 → **326** (−52); `canTargetPermanent` overrides
143 → 117; `@ValidatesTarget` methods 121 → **99** (−22 deleted: 17 Damage + 5
Prevention). The `CREATURE_OR_PLANESWALKER`, `PLAYER_OR_PLANESWALKER`, and `ANY_TARGET`
categories now have **0** in-scope records (fully migrated); CREATURE 40 → 29,
PLAYER_OR_PERMANENT 19 → 14. The auto-generated escape-hatch table counts only in-scope
records, so the 4 kept damage/prevention escape hatches dropped out of it (like step 2's
pilots); they remain in the validator index flagged "In a bucket table? = no" and are
enumerated above for step 10. `effect-dispatch-baseline.txt` NOT touched (no
instanceof-on-effect counts changed).

**Tests run** (via `./gradlew :magical-vibes-application:test --tests …`, all green,
`BUILD SUCCESSFUL`): fixed regression set — `WrackWithMadnessTest`, `DarkNourishmentTest`,
`FireballTest`, `EffectDispatchRatchetTest`, `TargetSpecRatchetTest` (both ratchets pass —
baseline shrank monotonically), `ValidTargetServiceTest`, `TargetLegalityServiceTest`,
plus `TargetValidationServiceSpecTest`; one card test per migrated effect —
`ChandraBoldPyromancerTest`, `BoggartShenanigansTest`, `ChandraNalaarTest`, `CorruptTest`,
`RoarOfTheCrowdTest`, `ConsumeSpiritTest`, `HereticsPunishmentTest`, `BurnTheImpureTest`,
`CoordinatedBarrageTest`, `GarrukRelentlessTest`, `TraitorsRoarTest`,
`DongZhouTheTyrantTest`, `CyclopsGladiatorTest`, `ZealousInquisitorTest`,
`OraclesAttendantsTest`, `JadeMonolithTest`, `BandageTest`, `HealingGraceTest`,
`HarmsWayTest`, `WellgabberApothecaryTest`, `SoulParryTest`, `BurningSunsAvatarTest`,
`ChandrasOutrageTest` (DealDamageToPlayers), `RemedyTest` (PreventDividedDamage).
FireballTest covers `DealDividedDamageEffect`; DarkNourishmentTest covers
`DealDamageToAnyTargetAndGainLifeEffect`.

---

## Step 4 — Destruction / Exile / Bounce / PermanentControl validators  (2026-07-15)

Migrated every effect whose `@ValidatesTarget` method lives in `DestructionTargetValidators`
(12), `ExileTargetValidators` (7), `BounceTargetValidators` (4), or
`PermanentControlTargetValidators` (11) — **34 effects total**. Each validator was read and
classified STRUCTURAL vs NON-STRUCTURAL before touching the record. (The step-4 brief said "13"
for Destruction; the file holds 12 `@ValidatesTarget` methods plus the private `requireLandTarget`
helper — 12 effects migrated.)

**Migrated onto `targetSpec()`, validator DELETED (28 = 10 Destruction + 6 Exile + 4 Bounce + 8
PermanentControl):**
- Destruction (10), all `harmful` (each validator called `checkProtection`):
  `harmful(CREATURE)` — `DestroyAttachmentsOnTargetCreatureEffect`,
  `SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect`;
  `harmful(PERMANENT)` — `DestroyTargetPermanentThenEffect`,
  `SacrificeTargetThenRevealUntilTypeToBattlefieldEffect`,
  `DestroyTargetThenRevealUntilTypeToBattlefieldEffect`,
  `DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect`,
  `DestroyTargetPermanentAtEndStepEffect`, `SacrificeTargetPermanentAtEndStepEffect`;
  `harmful(LAND)` — `DestroyTargetLandAndDamageControllerEffect`,
  `DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect`.
- Exile (6), all `harmful` (each validator called `checkProtection`):
  `harmful(PERMANENT)` — `ExileTargetPermanentEffect`, `ExileTargetPermanentAndTrackWithSourceEffect`,
  `ExileTargetPermanentMayPlayUntilNextTurnEffect`;
  `harmful(CREATURE)` — `ExileTargetCreatureAndAllWithSameNameEffect`,
  `ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect`,
  `MarkTargetCreatureExileInsteadOfDieThisTurnEffect`.
- Bounce (4), no `checkProtection`: `benign(PERMANENT)` —
  `ReturnTargetPermanentToHandWithManaValueConditionalEffect`,
  `ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect`,
  `ReturnTargetPermanentToHandAtEndStepEffect`; per-scope conditional —
  `ReturnToHandEffect` (`benign(PERMANENT)` for `TARGET`, `benign(PLAYER)` for the two
  `TARGET_PLAYERS_*` scopes, else `NONE`). `ReturnToHandEffect`'s validator only guarded
  `requireBattlefieldTarget` for the `TARGET` scope — which the non-no-op `PERMANENT` category
  reproduces exactly, and the player/self/all scopes carried no guard — so it is fully reproduced by
  the per-scope spec and DELETED (this is the 4th Bounce validator, leaving `BounceTargetValidators`
  empty → the file was removed). Contrast the no-op-PLAYER cases below whose validators cannot be
  deleted.
- PermanentControl (8), all `benign` (control-change / library tuck — no `checkProtection`):
  `benign(CREATURE)` — `GainControlOfEnchantedTargetEffect`, `IllicitAuctionEffect`;
  `benign(PERMANENT)` — `GainControlOfTargetEffect`, `AttachTargetToSourcePermanentEffect`,
  `PutTargetOnBottomOfLibraryEffect`, `PutTargetPermanentIntoLibraryNFromTopEffect`,
  `ShuffleTargetPermanentIntoLibraryEffect`, `GrantSubtypeToTargetCreatureEffect` (see the judgment
  note — `PERMANENT`, not `CREATURE`, because its validator omits `requireCreature`).

`BounceTargetValidators` became empty and was **deleted** (git rm). Deleted the private helper
`requireLandTarget` in Destruction once its two callers were gone; removed the now-unused `tvs`
field from `ExileTargetValidators`; pruned all now-unused imports and rewrote each class javadoc to
describe the remaining escape hatch(es).

**Migrated with the structural part in the spec, validator KEPT WHOLE (escape hatch — 6):**
- `SacrificePermanentsEffect` → per-recipient: `benign(PLAYER)` for `TARGET_PLAYER`, else `NONE`.
  Same pattern as step 3's `DealDamageToPlayersEffect`: the no-op PLAYER category cannot reproduce
  the validator's `requireTargetPlayer` guard for `TARGET_PLAYER`, so the validator is kept and the
  spec computed per-recipient to reproduce the conditional `canTargetPlayer` boolean. Benign (no
  `checkProtection`; the target is the sacrificing player, not a permanent).
- `DestroyCreatureBlockingThisEffect` → `harmful(CREATURE)`. Non-structural: combat-relation state
  (`isBlocking()` + `blockingTargets.contains(source)`). See the judgment note on `harmful`.
- `ReturnTargetCardFromExileToHandEffect` → `benign(EXILE_CARD)`. Non-structural: validates a card
  in the EXILE zone (a no-op category) and applies the effect's own card filter. Benign (return to
  hand, no `checkProtection`).
- `GainControlOfTargetAuraEffect` → `benign(PERMANENT)`. Non-structural: requires the target be an
  Aura **attached** to a permanent (`isAttached()` — an attachment-state check).
- `PutTargetOnTopOfLibraryEffect` → per-scope: `benign(PERMANENT)` for `TARGET`, else `NONE`.
  Its validator READS `effect.canTargetPermanent()` (now derived from this spec:
  `PERMANENT.includesPermanents()==true` for TARGET, `NONE`→false for SELF — the reader is preserved
  exactly). Per the step-4 brief the reader is left intact; step 10 rewrites it. The per-instance
  spec fully reproduces the validator's conditional `requireBattlefieldTarget`, but the validator is
  kept because the brief forbids touching the reader now.
- `TargetPlayerGainsControlOfSourceCreatureEffect` → `benign(PLAYER)`. Same no-op-PLAYER reasoning as
  `SacrificePermanentsEffect`: the validator's sole job is `requireTargetPlayer`, which the PLAYER
  category cannot reproduce, so the validator is kept.

The distinguishing rule for kept-vs-deleted conditional validators: a validator whose only guard is a
permanent requirement (`requireBattlefieldTarget`) is reproducible because `PERMANENT` is NOT a no-op
category (it calls `requireBattlefieldTarget`) — so `ReturnToHandEffect` and, structurally,
`PutTargetOnTopOfLibraryEffect` are fully reproduced; a validator whose guard is a PLAYER requirement
(`requireTargetPlayer`) is NOT reproducible because `PLAYER` is a no-op — so `SacrificePermanentsEffect`
and `TargetPlayerGainsControlOfSourceCreatureEffect` must keep their validators.
`PutTargetOnTopOfLibraryEffect` is kept anyway (its validator reads `canTargetPermanent()` — the brief
forbids touching that reader before step 10).

**Oracle / judgment calls:**
- **`harmful` flips `isDamageOrDestruction()` false→true on many effects (step-3 precedent).** Most
  migrated Destruction/Exile effects overrode only `canTargetPermanent` (never
  `isDamageOrDestruction`), yet their validators called `checkProtection`. Per HARMFUL FLAG POLICY
  the spec is `harmful`, so the derived `isDamageOrDestruction()` is now `true`. Effect:
  `ValidTargetService` also filters protection-having permanents from the OFFERED list. Final
  legality is unchanged (the kept-then-deleted validator already rejected protected targets at cast);
  the offering is now consistent with the validator — stricter-or-equal, allowed, and more
  rules-correct (a protected permanent is not a legal target).
- **`DestroyCreatureBlockingThisEffect` is the ONE case where the record already overrode
  `isDamageOrDestruction`=`true` but its (kept) validator does NOT call `checkProtection`.** Choosing
  `benign` would flip `isDamageOrDestruction` true→false (less strict offering — FORBIDDEN by
  BEHAVIOR-PRESERVING-OR-STRICTER), so `harmful(CREATURE)` is required to preserve it. The
  consequence is that the spec now runs `checkProtection` at cast where the old validator did not —
  strictly stricter, and more rules-correct (protection = can't be targeted; a blocker with
  protection from the source can't be the target of "destroy target creature blocking this"). This
  is the deliberate resolution of the HARMFUL FLAG POLICY vs. BEHAVIOR-PRESERVING tension; documented
  here as required.
- **Two LAND effects were `PERMANENT` in the matrix's coarse inference but their validator
  (`requireLandTarget`) enforces LAND.** Category read off the kept validator per the "category from
  the type check" rule: `DestroyTargetLandAndDamageControllerEffect` and
  `DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect` → `harmful(LAND)`.
- **`GrantSubtypeToTargetCreatureEffect` → `PERMANENT`, not `CREATURE`.** Despite the name, its
  validator does only `requireBattlefieldTarget` (no `requireCreature`); the creature restriction is
  the card's own target filter. `PERMANENT` preserves `canTargetPermanent()==true` exactly.
- **No `checkProtection` added to any bounce or pure control-change effect.** Per the brief, bounce
  and control-change are harmful only if their old validator called `checkProtection` — none did — so
  all are `benign`.
- No card behavior narrowed illegally; no web ruling needed (every choice is behavior-preserving or
  the documented stricter-and-more-correct offering/target filter).

**Audit re-run:** `python scripts/targetspec-audit.py` regenerated `targetspec-baseline.txt` +
`TARGETSPEC_MATRIX.md`. Records in scope 248 → **214** (−34); sum of baseline counts 326 → **283**
(−43); `canTargetPermanent` overrides 117 → **86** (−31); `@ValidatesTarget` methods 99 → **71**
(−28 deleted: 10 Destruction + 6 Exile + 4 Bounce + 8 PermanentControl). `LAND` in-scope count is
still 1 (the two migrated LAND effects dropped out of scope; the remaining 1 is the unmigrated
`GrantBasicLandTypeToTargetEffect`); `CREATURE` 21, `PERMANENT` 45, `PLAYER_OR_PERMANENT` 13. The
auto-generated escape-hatch table still shows 7 (the graveyard controller/opponent-compare set); the
8 escape hatches kept this step are not auto-flagged by the audit heuristic — they are kept by
deliberate decision (no-op PLAYER/EXILE_CARD categories, attachment/combat state, the reader) and are
enumerated above for step 10. `effect-dispatch-baseline.txt` NOT touched (no instanceof-on-effect
counts changed).

**Tests run** (one targeted `:magical-vibes-application:test --tests …` invocation, `BUILD
SUCCESSFUL`): fixed regression set — `WrackWithMadnessTest`, `DarkNourishmentTest`, `FireballTest`,
`EffectDispatchRatchetTest`, `TargetSpecRatchetTest` (both ratchets green — baseline shrank
monotonically), `ValidTargetServiceTest`, `TargetLegalityServiceTest`, `TargetValidationServiceSpecTest`;
one card test per migrated effect (all 34 exist and pass) — `CruelEdictTest` (SacrificePermanents),
`KnightOfDuskTest` (DestroyCreatureBlockingThis), `StripBareTest`, `DeathsCaressTest`
(DestroyTargetPermanentThen), `ShapeAnewTest`, `PolymorphTest`, `CryoclasmTest`, `FieldOfRuinTest`,
`ErodeTest`, `StoneGiantTest`, `LowlandOafTest`, `MercyKillingTest`, `RunicRepetitionTest`
(ReturnTargetCardFromExile), `ArchonOfJusticeTest` (ExileTargetPermanent), `HelvaultTest`,
`SuspendAggressionTest`, `SeverTheBloodlineTest`, `HeatedArgumentTest`, `WiltInTheHeatTest`,
`AcademyJourneymageTest` (ReturnToHand), `PerilousVoyageTest`, `ConsignToDreamTest`, `DragonMaskTest`,
`RootwaterMatriarchTest` (GainControlOfEnchantedTarget), `ActOfAggressionTest` (GainControlOfTarget),
`IllicitAuctionTest`, `OgreGeargrabberTest` (AttachTargetToSourcePermanent), `AuraGraftTest`
(GainControlOfTargetAura), `BrutalizerExarchTest` (PutTargetOnBottomOfLibrary), `AethertowTest`
(PutTargetOnTopOfLibrary), `TeferiHeroOfDominariaTest` (PutTargetPermanentIntoLibraryNFromTop),
`DeglamerTest` (ShuffleTargetPermanentIntoLibrary), `OliviaVoldarenTest` (GrantSubtypeToTargetCreature),
`JinxedIdolTest` (TargetPlayerGainsControlOfSourceCreature).

---

## Step 5 — CreatureMod / Life / Library validators  (2026-07-15)

Migrated every effect whose `@ValidatesTarget` method lives in `CreatureModTargetValidators` (39
methods), `LifeTargetValidators` (5), or `LibraryTargetValidators` (4) — **46 effects total**. Each
validator was read and classified STRUCTURAL (spec-reproducible → delete) vs NON-STRUCTURAL /
no-op-player (keep whole) before touching the record.

**CreatureMod — 37 records migrated, validator DELETED (structural, fully reproduced by the spec):**
- `benign(PERMANENT)` (validator = `requireBattlefieldTarget` only): `TapOrUntapTargetPermanentEffect`,
  `GrantColorUntilEndOfTurnEffect`, `BecomeChosenColorsUntilEndOfTurnEffect`,
  `UnattachEquipmentFromTargetPermanentsEffect`, `AddCardTypeToTargetPermanentEffect`,
  `GrantProtectionChoiceUntilEndOfTurnEffect`, `CreateTokenCopyOfTargetPermanentEffect`,
  `SetChosenColorUntilEndOfTurnEffect`, `DoubleCountersOnTargetPermanentEffect`,
  `RemoveChargeCountersFromTargetPermanentEffect`, `RemoveCountersFromTargetAndBoostSelfEffect`.
- `benign(CREATURE)` (validator = `requireBattlefieldTarget` + `requireCreature`):
  `BoostTargetCreatureEffect`, `BuffTargetCreatureIndefinitelyEffect`,
  `GrantProtectionFromCardTypeUntilEndOfTurnEffect`, `BoostTargetCreaturePerChosenTypeCountEffect`,
  `EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffect`,
  `GrantEffectToTargetUntilEndOfTurnEffect`, `GrantChosenKeywordToTargetEffect`,
  `TargetCreatureBecomesSubtypeUntilEndOfTurnEffect`, `MustAttackThisTurnEffect`,
  `MustBeBlockedByAllCreaturesThisTurnEffect`, `MustBeBlockedIfAbleThisTurnEffect`,
  `CantBlockSourceEffect`, `RemoveTargetFromCombatEffect`, `MakeTargetCreaturePreparedEffect`,
  `RemoveCounterFromTargetAndGainLifeEffect`, `EquipEffect`.
- `benign(CREATURE, PermanentIsCreaturePredicate)`: `MustBlockSourceEffect` — the predicate is KEPT on
  the spec (deleting the old `targetPredicate()` override, whose value it reproduces) because
  trigger-target collection (`TriggeredAbilityQueueService` / `TriggerTargetCollector`, filter
  `canTargetPermanent() && targetPredicate() != null`) restricts the granted "must block" trigger's
  candidates to creatures (Tower Above). CREATURE + creature-predicate is redundant but preserves the
  exact `targetPredicate()` output.
- `benign(LAND)` (validator = `requireBattlefieldTarget` + `hasType(LAND)`): `GrantBasicLandTypeToTargetEffect`.
- `harmful(CREATURE)` (validator = `requireBattlefieldTarget` + `requireCreature` + `checkProtection`):
  `MassFightTargetCreatureEffect` (already `isDamageOrDestruction=true`; kept),
  `PackHuntEffect` (its validator called `checkProtection` but the record did NOT override
  `isDamageOrDestruction` — per HARMFUL FLAG POLICY the spec is `harmful`, flipping the derived
  `isDamageOrDestruction` false→true; step-3 precedent: the offering now also filters protected
  creatures — stricter-and-more-correct).
- Per-scope specs (conditional records; the switch reproduces the old conditional booleans exactly,
  and the validator's scope-guarded `requireBattlefieldTarget`/`requireCreature`, so all are DELETED):
  - `TapPermanentsEffect` — `TARGET`/`ALL_TARGETS` → `benign(PERMANENT)`,
    `TARGET_PLAYERS_PERMANENTS` → `benign(PLAYER)` (no-op, matches the old validator's no-check),
    `SELF` → self-targeting `NONE`, else `NONE`.
  - `UntapPermanentsEffect` — `TARGET` → `benign(PERMANENT, filter)` (predicate carried for the
    VoltaicServant end-step trigger, which untaps target artifact via `targetPredicate()`);
    **`ALL_TARGETS` → `benign(PLAYER_OR_PERMANENT)`** — a no-op category, chosen because
    `ALL_TARGETS` is a LIVE multi-target scope (Garruk Wildspeaker's "untap two target lands") whose
    targets ride `entry.getTargetIds()` and are validated on the multi-target path; a non-no-op
    `PERMANENT` would add a `requireBattlefieldTarget` on the null single-`targetId` and break Garruk.
    This widens the derived `canTargetPlayer` false→true for `ALL_TARGETS`, inert per step-3's
    documented precedent (multi-target selection goes through `multiTargetFilters`, not the boolean).
    `SELF` → self-targeting `NONE`, else `NONE`. (Tap's `ALL_TARGETS` is unused → `PERMANENT`, exact
    booleans, dead path.)
  - `CantBlockThisTurnEffect` — `TARGET` → `benign(CREATURE)` (validator did `requireCreature` here,
    so CREATURE, not the matrix's coarse `PLAYER_OR_PERMANENT`), `TARGET_PLAYERS_PERMANENTS` →
    `benign(PLAYER)`, else `NONE`.
  - `SwitchPowerToughnessEffect` — `self` → self-targeting `NONE`, else `benign(CREATURE)`.
  - `SetBasePowerToughnessEffect` — `GrantScope.TARGET` → `benign(CREATURE)`, else `NONE`.
  - `AnimatePermanentsEffect` — `GrantScope.TARGET` → `benign(PERMANENT)`, `SELF` → self-targeting
    `NONE`, else `NONE`.
  - Self-targeting `NONE` is written `new TargetSpec(TargetCategory.NONE, false, null, true, 1)` (no
    factory; reproduces the old `isSelfTargeting()` true exactly).

**CreatureMod — 2 validators KEPT (records override NO legacy method → not in scope, not migrated):**
`validateStaticBoost` (`StaticBoostEffect`) and `validateAttachedBoost` (`AttachedBoostEffect`) target
through the equip/attach mechanism (per the step-3/ETB-slot note: those slot usages bypass the
single-target validator path), so their "the attached/boosted permanent must be a creature" check has
no `targetSpec()` correlate and stays hand-written. `CreatureModTargetValidators` was rewritten down to
just these two (all effect/`CardType`/`GrantScope`/`TapUntapScope` imports pruned; javadoc rewritten).

**Life (5) + Library (4) — ALL 9 validators KEPT WHOLE (no-op-PLAYER escape hatch); records migrated:**
Every validator here calls `requireTargetPlayer`, which the `PLAYER` category CANNOT reproduce (it is a
no-op in the spec interpreter — same reason `DealDamageToPlayersEffect` (step 3) and
`SacrificePermanentsEffect` (step 4) keep theirs). Deleting them would drop the player-target guard
(less strict — forbidden). Each record gets a `benign(PLAYER)` spec (or per-recipient), deleting only
its `canTargetPlayer` override; the validator is untouched, so `LifeTargetValidators` /
`LibraryTargetValidators` are unchanged.
- `benign(PLAYER)`: `TargetPlayerLosesLifeAndControllerGainsLifeEffect`, `TargetPlayerGainsLifeEffect`,
  `DrainLifePerControlledPermanentEffect`, `RevealTopCardOfLibraryEffect`,
  `ChooseCardNameAndExileFromZonesEffect`, `MillBottomOfTargetLibraryConditionalTokenEffect`.
- per-recipient (`TARGET_PLAYER` → `benign(PLAYER)`, else `NONE`, reproducing the conditional
  `canTargetPlayer`): `LoseLifeEffect`, `GivePoisonCountersEffect`, `MillEffect`.

**Oracle / judgment calls:**
- **Predicate enforcement is new for `Untap`/`MustBlockSource` and changed two error messages.** The old
  `validateUntapPermanents` did `requireBattlefieldTarget` only; the effect's `filter` was exposed via
  `targetPredicate()` for offering/trigger collection but was NOT a hard cast-time gate. Carrying it on
  the spec (mandatory — `VoltaicServant`'s trigger needs `targetPredicate()`) means `validateSpec` now
  enforces it at cast. For the two activated abilities that ALSO have an ability-level
  `PermanentPredicateTargetFilter` (Jandor's Saddlebags, Thousand-Year Elixir), the spec's predicate
  check now fires first and rejects a non-creature with the engine-standard "Target does not match the
  required predicate" instead of the filter's "Target must be a creature". The legal target SET is
  unchanged (non-creature still rejected) — only the message differs; the two card tests' message
  assertions were updated (documented in-test).
- **`CardEffectTargetingConsistencyTest` updated** to accept a `targetSpec()` override as "declares its
  targeting" (the `canTarget*` booleans are derived from it). Step 5 is the first to migrate
  `Target*`-prefixed effects (`TargetCreatureBecomesSubtypeUntilEndOfTurnEffect`,
  `TargetPlayerGainsLifeEffect`, `TargetPlayerLosesLifeAndControllerGainsLifeEffect`); steps 3–4 only
  touched `Deal*`/`Destroy*`/`Exile*` names, so this guard first tripped now. Not a weakening — a
  `targetSpec()` override is a strictly-equivalent declaration.
- No card behavior narrowed illegally; no web ruling needed (every choice is behavior-preserving, the
  documented inert widening, or the stricter-and-more-correct predicate/protection enforcement).

**Audit re-run:** `python scripts/targetspec-audit.py` regenerated `targetspec-baseline.txt` +
`TARGETSPEC_MATRIX.md`. Records in scope 214 → **168** (−46); sum of baseline counts 283 → **228**
(−55); `canTargetPermanent` overrides 86 → **49**; `@ValidatesTarget` methods 71 → **34** (−37 deleted
from CreatureMod; the 9 Life/Library validators are kept but their records left scope, and the 2
aura/attach validators were never in scope). `CREATURE` and `LAND` now have **0** in-scope records
(fully migrated); `PERMANENT` 45 → 32, `PLAYER` 81 → 72, `PLAYER_OR_PERMANENT` 13 → 11. The
auto-generated escape-hatch table still shows 7 (the graveyard controller/opponent set); the 11 kept
validators this step (9 no-op-PLAYER Life/Library + 2 aura/attach) are kept by deliberate decision and
enumerated above for step 10. `effect-dispatch-baseline.txt` NOT touched (no instanceof-on-effect
counts changed).

**Tests run** (fixed regression set via `scripts/run-card-test.ps1`, all green:
`TargetSpecRatchetTest`, `EffectDispatchRatchetTest` — both ratchets pass, baseline shrank
monotonically; `TargetValidationServiceSpecTest`, `ValidTargetServiceTest`, `TargetLegalityServiceTest`,
`FireballTest`, `WrackWithMadnessTest`, `DarkNourishmentTest`). One card test per migrated effect plus
the tricky cases (one targeted `:magical-vibes-application:test --tests …` invocation, `BUILD
SUCCESSFUL`): `TwiddleTest`, `BlindingMageTest`/`ClaustrophobiaTest` (TapPermanents),
`GarrukWildspeakerTest` (Untap ALL_TARGETS), `CaptivatingCrewTest`/`CeruleanWispsTest` (Untap TARGET),
`VoltaicServantTest` (Untap TARGET artifact trigger — targetPredicate path),
`JandorsSaddlebagsTest`/`ThousandYearElixirTest` (Untap creature-predicate, updated messages),
`GiantGrowthTest` (BoostTargetCreature), `RidingTheDiluHorseTest`, `DuelTacticsTest` (CantBlockThisTurn),
`TangleAnglerTest`/`TowerAboveTest` (MustBlockSource + granted-trigger predicate),
`TwistedImageTest`/`TurtleshellChangelingTest` (SwitchPT), `DiminishTest` (SetBasePT),
`GrandArchitectTest`, `PrismwakeMerrowTest`, `TelJiladDefianceTest`, `FulgentDistractionTest`,
`LiquimetalCoatingTest`, `InkmothNexusTest` (Animate SELF), `PacksDisdainTest`, `MirrorweaveTest`,
`VerdantRebirthTest`, `GolemArtisanTest`, `BoldwyrIntimidatorTest`, `InciteTest`, `AlluringScentTest`,
`EmergentGrowthTest`, `DuctCrawlerTest`, `HollowhengeSpiritTest`, `SkycoachWaypointTest`,
`WoeleecherTest`, `FlayerHuskTest` (Equip), `AlphaBrawlTest` (MassFight), `MasterOfTheWildHuntTest`
(PackHunt), `ApostlesBlessingTest`, `MirrorworksTest`, `DistortingLensTest`, `GilderBairnTest`,
`GremlinMineTest`, `HexParasiteTest`, `TideshaperMysticTest`; Life — `SyphonLifeTest`,
`HarrowingJourneyTest` (LoseLife TARGET_PLAYER), `StreamOfLifeTest`, `CaressOfPhyrexiaTest` (GivePoison
TARGET_PLAYER), `TezzeretAgentOfBolasTest` (DrainLifePerControlledPermanent); Library —
`WeightOfMemoryTest` (Mill TARGET_PLAYER), `AvenWindreaderTest`, `MemoricideTest`, `CellarDoorTest`;
plus `CardEffectTargetingConsistencyTest` (updated). (An accidental unfiltered run of the whole
application suite during setup surfaced exactly the 3 failures fixed above and nothing else.)

---

## Step 6 — Graveyard / any-graveyard / controllers-only / exile  (2026-07-15)

Migrated every effect overriding `canTargetGraveyard` (7 records), `canTargetAnyGraveyard` (7),
and/or `targetsControllersGraveyardOnly` (2, both also `canTargetGraveyard`) — **14 records total**
(the two controllers-only effects are inside the 7 graveyard records). The audit's step-1 counts
(19 / 8 / 2 / 1) are the ORIGINAL brief numbers; by step 6 the remaining in-scope graveyard records
are the 14 in the matrix's two graveyard bucket tables, the rest of the raw `canTargetGraveyard`
overrides being the 5 delegating wrappers (`ConditionalEffect`, `ConditionalReplacementEffect`,
`MayEffect`, `TriggeringCardConditionalEffect`, `TriggeringPermanentConditionalEffect` — steps 7/9).

**EXILE_CARD — nothing to do.** The one `canTargetExile` effect
(`ReturnTargetCardFromExileToHandEffect`) was already migrated to `benign(EXILE_CARD)` in step 4;
the current audit shows `canTargetExile` overrides = **0**. Verified no effect record still overrides
`canTargetExile` (only the `CardEffect` default remains). `EXILE_CARD` therefore has 0 in-scope
records and needed no work this step.

### How the three graveyard flags are read (checked before assigning categories)

Read the three consumers named in the brief:
- **`SpellCastingService`** (`needsGraveyardEffectTargeting` branch, ~lines 891–902): for a
  graveyard-effect target it computes `inControllersGraveyard`, then — `targetsControllersGraveyardOnly`
  → require controller's graveyard; else if `!canTargetAnyGraveyard && inControllersGraveyard` → reject
  ("must be in an opponent's graveyard"); else (any) → accept. So the THREE zone-states are:
  **controller-only** = `controllersOnly`; **opponent-only** = `!controllersOnly && !anyGraveyard`
  (the default triple T/F/F); **any** = `!controllersOnly && anyGraveyard`.
- **`AiTargetSelector`** (line ~505): identical precedence — `controllersOnly` → CONTROLLERS_GRAVEYARD,
  else `canTargetAnyGraveyard` → ALL_GRAVEYARDS, else OPPONENT_GRAVEYARD.
- **`TargetLegalityService.validateGraveyardEffectTargetOnly`** (line 452,
  `.filter(CardEffect::canTargetGraveyard)` — the method-reference form): selects which SPELL-slot
  effects to validate as graveyard targets; only requires the derived `canTargetGraveyard()` to stay
  true for every migrated record.
- **Multi-target graveyard path** (`TargetLegalityService.validateMultiTargetGraveyardAbility`,
  line 160): does NOT read the three flags — it `instanceof`-dispatches and reads record components
  (`ReturnCardFromGraveyardEffect.targetGraveyard()`/`source()`,
  `ExileGraveyardCardsEffect.scope()`, `ExileCardsFromGraveyardEffect.maxTargets()`). Unaffected.
- `GraveyardTargetingService` (ETB/begin-of-combat paths) reads `canTargetAnyGraveyard()` (lines 125,
  222) to pick the search scope. Preserved by the exact-boolean derivation.

### Category mapping chosen (recorded per the brief) — DEDICATED CATEGORY

Added a new `TargetCategory.CONTROLLERS_GRAVEYARD_CARD(false,false)`, giving a **bijection** between the
three graveyard zone-states and three categories:

| (canTargetGraveyard, canTargetAnyGraveyard, targetsControllersGraveyardOnly) | Category |
|---|---|
| (T, F, F) — opponent-only (the default state) | `GRAVEYARD_CARD` |
| (T, T, F) — any graveyard | `ANY_GRAVEYARD_CARD` |
| (T, F, T) — controller's graveyard only | `CONTROLLERS_GRAVEYARD_CARD` |

`CardEffect` derivations updated: `canTargetGraveyard()` now also true for
`CONTROLLERS_GRAVEYARD_CARD`; `canTargetAnyGraveyard()` unchanged (`ANY_GRAVEYARD_CARD` only);
`targetsControllersGraveyardOnly()` changed from constant-`false` to
`category() == CONTROLLERS_GRAVEYARD_CARD` (retiring the step-2 "no correlate" deviation).

**Dedicated category, NOT a spec field — justification.** The three zone-states are one
mutually-exclusive dimension (an effect is opponent-only XOR any XOR controller-only), which an enum
expresses exactly and unambiguously; each triple ↔ one category and back with zero ambiguity, matching
both readers' precedence. A 6th `TargetSpec` boolean component would be meaningless for every
non-graveyard category (a `controllersGraveyardOnly=true` `CREATURE` spec is nonsense), would force
touching the record signature + all factories, and re-introduces the orthogonal-flag muddle the enum
avoids. `GRAVEYARD_CARD` vs `ANY_GRAVEYARD_CARD` already encode the `anyGraveyard` bit in this same
dimension; `CONTROLLERS_GRAVEYARD_CARD` is the natural third value.

### Spec interpreter does NO zone re-check for graveyard categories — verified, no fix needed

Confirmed from step 2: in `TargetValidationService.validateSpec`, `GRAVEYARD_CARD` /
`ANY_GRAVEYARD_CARD` / `CONTROLLERS_GRAVEYARD_CARD` / `EXILE_CARD` are in the no-op switch arm, and the
trailing predicate/`harmful` block resolves the target via `findPermanentById(targetId)` — which
returns `null` for a graveyard card (not a battlefield permanent) → early return. So the interpreter
validates NOTHING for a graveyard target. Added `CONTROLLERS_GRAVEYARD_CARD` to that no-op arm (the
only `switch` over `TargetCategory` in the codebase; the AI-engine switches are on `InstantCategory`,
not affected). The graveyard PATHS (`SpellCastingService` zone checks + the kept validators) remain the
sole enforcement, exactly as before.

### ALL 13 GraveyardTargetValidators KEPT WHOLE (deviation from the optimistic step-8 overview)

The step-1/overview text ("most keep an escape-hatch validator") anticipated deleting the "structural"
graveyard validators. **None were deletable.** Because the spec interpreter is a total no-op for
graveyard categories, the spec's `predicate` field (which only evaluates against a *battlefield*
permanent) **cannot** express any graveyard card-predicate/card-type/zone check. Per the brief's rule
— "if the check is exactly a predicate the spec's predicate field can express … migrate; otherwise
escape hatch" — every graveyard validator falls in *otherwise*: each enforces at minimum
`targetZone==GRAVEYARD` + non-null + card-found (and usually a card-type/predicate and/or a
controller/opponent compare), none of which the no-op spec reproduces. Deleting any would drop real
validation (strictly less strict — forbidden). So `GraveyardTargetValidators` is **unchanged** (all 13
`@ValidatesTarget` methods kept, no file/import churn). All are `benign` (no validator calls
`checkProtection` — graveyard cards aren't battlefield permanents; the escape-hatch table's harmful=no
for all 7 auto-flagged ones confirms it).

Kept validators by effect (13): `validateReturnCardFromGraveyard`,
`validatePutCreatureFromOpponentGraveyardWithExile`, `validateCastTargetInstantOrSorceryFromGraveyard`,
`validateGrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilities`,
`validateGrantFlashbackToTargetGraveyardCard`, `validateExileTargetCardFromGraveyardAndImprint`,
`validateExileTargetCardFromGraveyardAndCreateTokenCopy`, `validateExileGraveyardCards`,
`validateExileTargetCardFromGraveyardMayPlay`, `validatePlayTargetCardFromGraveyardWithoutPaying`,
`validateExileTargetInstantOrSorceryFromOpponentGraveyardMayCast`,
`validateExileTargetGraveyardCardAndSameName`, `validatePutCardFromOpponentGraveyard`.

### Migrated onto `targetSpec()`, legacy overrides DELETED (14)

- `benign(GRAVEYARD_CARD)`: `CastTargetInstantOrSorceryFromGraveyardEffect`,
  `ExileTargetCardFromGraveyardAndImprintOnSourceEffect`,
  `PutCardFromOpponentGraveyardOntoBattlefieldEffect`,
  `PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect`.
- `benign(CONTROLLERS_GRAVEYARD_CARD)`: `GrantFlashbackToTargetGraveyardCardEffect`,
  `PlayTargetCardFromGraveyardWithoutPayingManaCostEffect` (deleting BOTH the `canTargetGraveyard`
  and `targetsControllersGraveyardOnly` overrides in one edit each).
- `benign(ANY_GRAVEYARD_CARD)`: `ExileGraveyardCardWithConditionalBonusEffect` (no validator),
  `ExileTargetGraveyardCardAndSameNameFromZonesEffect`,
  `ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect`,
  `GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect`.
- Per-instance (conditional) specs, reproducing the old conditional booleans exactly:
  - `ReturnCardFromGraveyardEffect` — `targetGraveyard ? benign(GRAVEYARD_CARD) : NONE`. The
    own/opponent/all narrowing rides `source()` on the kept validator + the
    `needsSingleGraveyardTargeting` path; the legacy `canTargetAnyGraveyard` was ALWAYS false for this
    effect regardless of `source`, so `GRAVEYARD_CARD` is correct for every source.
  - `ExileGraveyardCardsEffect` — `switch(scope)`: `TARGET_CARDS_ANY_GRAVEYARD` → `ANY_GRAVEYARD_CARD`,
    `TARGET_CARDS_OPPONENT_GRAVEYARD` → `GRAVEYARD_CARD`, `TARGET_PLAYER_ENTIRE` → `PLAYER`, else
    `NONE`. Reproduces the three conditional booleans (`canTargetGraveyard`/`canTargetAnyGraveyard`/
    `canTargetPlayer`) exactly.
  - `ExileTargetCardFromGraveyardAndCreateTokenCopyEffect`,
    `ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect` —
    `ownGraveyardOnly ? benign(GRAVEYARD_CARD) : benign(ANY_GRAVEYARD_CARD)`.

### Oracle / judgment calls

- **`ownGraveyardOnly` is NOT `targetsControllersGraveyardOnly`.** Two effects
  (`ExileTargetCardFromGraveyard{AndCreateTokenCopy,MayPlayUntilNextTurn}Effect`) have an
  `ownGraveyardOnly` record component that, when true, sets `canTargetAnyGraveyard=false` but leaves
  `targetsControllersGraveyardOnly=false` (they never overrode it) — the own-graveyard restriction is
  enforced by their kept validator's controller-compare, not by the flag. So `ownGraveyardOnly=true`
  reproduces the (T,F,F) triple = **`GRAVEYARD_CARD`**, NOT `CONTROLLERS_GRAVEYARD_CARD`. Only the two
  effects that literally overrode `targetsControllersGraveyardOnly()` to true
  (`GrantFlashback…`, `PlayTargetCardFromGraveyard…`) get `CONTROLLERS_GRAVEYARD_CARD`. This is the
  BEHAVIOR-PRESERVING-EXACTLY reading of the readers' precedence (controllers-only wins over any-vs-
  opponent), and it matters: mis-assigning `CONTROLLERS_GRAVEYARD_CARD` here would flip the derived
  boolean and change the AI/SpellCasting search scope.
- **`ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect` keeps `ANY_GRAVEYARD_CARD`** even
  though it is "from an opponent's graveyard": its legacy booleans were (T,T,F), and the opponent
  restriction lives in the kept validator's opponent-relation check (`graveyardOwnerId.equals` throw).
  Reproducing booleans exactly forbids "narrowing" it to `GRAVEYARD_CARD`.
- No behavior narrowed; no web ruling needed — every category reproduces the prior three-flag triple
  exactly, and the spec interpreter adds no new check for any graveyard category (total no-op).

### Audit re-run + lockstep

`python scripts/targetspec-audit.py` re-run (idempotent — two runs byte-identical) to regenerate
`targetspec-baseline.txt` + `TARGETSPEC_MATRIX.md`. Records in scope 168 → **154** (−14); sum of
baseline counts 228 → **204** (−24 = the 24 deleted overrides: 14×`canTargetGraveyard` + 7×
`canTargetAnyGraveyard` + 2×`targetsControllersGraveyardOnly` + 1×`canTargetPlayer` on
`ExileGraveyardCardsEffect`). `canTargetGraveyard` overrides 19→**5**, `canTargetAnyGraveyard` 8→**1**,
`targetsControllersGraveyardOnly` 2→**0** (the residue are the 5 delegating wrappers, steps 7/9).
`GRAVEYARD_CARD`/`ANY_GRAVEYARD_CARD` in-scope counts → **0**; `CONTROLLERS_GRAVEYARD_CARD` added to the
audit's confirmed-enum list (new `structural_flags`/`assign_category`/`bucket_of` branch keyed on
`targetsControllersGraveyardOnly` — kept in lockstep with the enum) and shows 0 in-scope. The
`@ValidatesTarget` total stays **34** (all 13 graveyard validators kept whole; they now show
"In matrix? = no" as their effects left scope, and the auto escape-hatch table drops to 0 — the same
in-scope-only artifact seen in steps 2–5; the 13 kept validators are enumerated above for step 10).
`effect-dispatch-baseline.txt` NOT touched (no `instanceof`-on-effect counts changed; `TargetSpec`/
`TargetCategory` were already on the coupling exemption from step 2, and adding an enum VALUE doesn't
change that).

**Tests run** (one filtered `:magical-vibes-application:test` invocation, `BUILD SUCCESSFUL`): fixed
regression set — `WrackWithMadnessTest`, `DarkNourishmentTest`, `FireballTest`,
`EffectDispatchRatchetTest`, `TargetSpecRatchetTest` (both ratchets green — baseline shrank
monotonically), `ValidTargetServiceTest`, `TargetLegalityServiceTest`, `TargetValidationServiceSpecTest`,
`CardEffectTargetingConsistencyTest`; one card test per migrated effect —
`MemoryPlunderTest` (Cast…FromGraveyard), `MyrWelderTest` (ExileTargetCardFromGraveyardAndImprint),
`SnapcasterMageTest` (GrantFlashback — CONTROLLERS_GRAVEYARD_CARD), `HordeOfNotionsTest`
(PlayTargetCardFromGraveyard — CONTROLLERS_GRAVEYARD_CARD), `GethLordOfTheVaultTest`
(PutCardFromOpponentGraveyard), `GruesomeEncoreTest` (PutCreatureFromOpponentGraveyardWithExile),
`DisentombTest` (ReturnCardFromGraveyard, targetGraveyard=true), `DeathgorgeScavengerTest`
(ExileGraveyardCardWithConditionalBonus), `BeckonApparitionTest` (ExileGraveyardCards),
`SeanceTest` (ExileTargetCardFromGraveyardAndCreateTokenCopy), `PracticedScrollsmithTest`
(ExileTargetCardFromGraveyardMayPlay), `SurgicalExtractionTest` (ExileTargetGraveyardCardAndSameName),
`NitaForumConciliatorTest` (ExileTargetInstantOrSorceryFromOpponentGraveyardMayCast),
`HavengulLichTest` (GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilities).

---

## Step 7 — Spell-on-stack (canTargetSpell) + the ChangeColorText dual  (2026-07-15)

Migrated the **16 leaf spell-targeting effects**: the 15 pure `canTargetSpell` records
(counterspells, copy-spell, change-target) + `ChangeColorTextEffect` (the spell-OR-permanent dual).
The 5 remaining `canTargetSpell` overrides are the **delegating wrappers** — deferred to step 9 with
the safety rationale below. None of the 16 has a `@ValidatesTarget` validator: spell targets are
validated on the stack path (`TargetLegalityService.checkSpellTargetOnStack` → the card's
`StackEntryPredicateTargetFilter`), which this program does not touch. So the migration is purely
declarative — no validator files were read/edited or deleted this step.

### Interpreter is a total no-op for a spell target — verified before touching anything

`TargetValidationService.validateSpec`: `SPELL_ON_STACK` sits in the no-op switch arm (no
`requireBattlefieldTarget`), and the trailing predicate/`harmful` block resolves the target via
`findPermanentById(targetId)` — which returns `null` for a spell on the stack (not a battlefield
permanent) → early return. `benign(SPELL_ON_STACK)` also has predicate `null` + harmful `false`, so
even the no-op block does nothing. The interpreter therefore validates NOTHING for a spell target; the
stack path stays the sole enforcement, exactly as before migration (where these effects had
`targetSpec() == NONE` and `validateSpec` never ran).

### Migrated onto `targetSpec()`, `canTargetSpell` override DELETED — 15 pure, all `benign(SPELL_ON_STACK)`

Every one overrode ONLY `canTargetSpell()` (no `isDamageOrDestruction`/other legacy override; all are
countering/copying/retargeting, none deal damage or destroy → benign, never harmful):
`ChangeTargetOfTargetSpellToSourceEffect`, `ChangeTargetOfTargetSpellWithSingleTargetEffect`,
`ChooseNewTargetsForTargetSpellEffect`, `CopySpellEffect`, `CounterSpellAndCreateTreasureTokensEffect`,
`CounterSpellAndExileAllWithSameNameEffect`, `CounterSpellAndExileEffect`,
`CounterSpellAndGainControlIfArtifactOrCreatureEffect`, `CounterSpellAndPutOnTopOfLibraryEffect`,
`CounterSpellEffect`, `CounterSpellIfControllerPoisonedEffect`, `CounterUnlessDiscardsEffect`,
`CounterUnlessPaysEffect`, `CounterlashEffect`, `MakeTargetSpellUncounterableEffect`. Derived
`canTargetSpell()` = `category()==SPELL_ON_STACK` = `true` (preserved exactly); `canTargetPlayer` /
`canTargetPermanent` / `isDamageOrDestruction` all derive `false` from `SPELL_ON_STACK(f,f)` +
benign, matching the old interface defaults exactly.

### `ChangeColorTextEffect` → `benign(PERMANENT)`, keeping the `canTargetSpell` record COMPONENT (dual)

`ChangeColorTextEffect(boolean landTypesAllowed, boolean canTargetSpell)` is the one dual case
(Glamerdye targets a spell OR a permanent; Mind Bend targets a permanent only). It overrode
`canTargetPermanent()` (hand-written) and additionally exposes `canTargetSpell()` **via its record
component's auto-generated accessor** (the audit's `public boolean canTargetSpell()` regex does NOT
match a record component, so the matrix counted only `canTargetPermanent` and bucketed it `PERMANENT`).
Migration: deleted the `canTargetPermanent()` override, added `targetSpec() = benign(PERMANENT)`, and
**kept the `canTargetSpell` component untouched** (it is data / wire-format, not a legacy method
override — cannot be removed without changing the record signature + `equals()`). Result, both
capabilities preserved EXACTLY:
- `canTargetPermanent()` derives `PERMANENT.includesPermanents()` = `true` (was `true`).
- `canTargetSpell()` — the component accessor overrides the interface default, returning the component
  value (Glamerdye `true`, Mind Bend `false`), independent of the `PERMANENT` category.
- `canTargetPlayer()` derives `PERMANENT.includesPlayers()` = `false` (unchanged).

**Why `PERMANENT` is safe for the spell mode (the dual-category worry).** `PERMANENT` is NOT a no-op —
it runs `requireBattlefieldTarget`. But it only ever runs on the PERMANENT validation path
(`checkSpellTargeting` → `checkEffectTargets`), which is reached only when the actual target is a
battlefield permanent; a spell target routes through the SEPARATE `checkSpellTargetOnStack` (which
never calls `checkEffectTargets`). `SpellCastingService` (lines 677–683) resolves a "spell or
permanent" chooser by the actual target's zone (`isSpellOnStack(targetId)`), keyed off `canTargetSpell`
(component) + `canTargetPermanent` (derived) — both preserved — so Glamerdye's spell mode still uses
the stack path and its permanent mode the permanent path. `validateSpec(PERMANENT)` thus fires only on
a real permanent (inert re-check; the target was already found non-null at `checkSpellTargeting`
line 348) and NEVER on a spell target. A no-op category (e.g. `PLAYER_OR_PERMANENT`) was rejected: it
would widen the derived `canTargetPlayer` `false→true`, offering players Glamerdye/Mind Bend cannot
target (single-target boolean path, not inert here). `benign` (not harmful): text change, no
`checkProtection`, no prior `isDamageOrDestruction` override.

### The 5 delegating wrappers are DEFERRED to step 9 (not migrated here) — safety rationale

`ConditionalEffect`, `ConditionalReplacementEffect`, `MayEffect`, `TriggeringCardConditionalEffect`,
`TriggeringPermanentConditionalEffect` each override `canTargetSpell()` by **delegating to
`wrapped`** (e.g. `TriggeringCardConditionalEffect.canTargetSpell()` = `wrapped.canTargetSpell()`).
The audit mis-bucketed the two `Triggering*` wrappers into `SPELL_ON_STACK` (its wrapper heuristic
only matched the 3 `Conditional*`/`May` names), but functionally all 5 are identical delegating
wrappers and belong with step 9's structural-wrapper family. They CANNOT take a static
`benign(SPELL_ON_STACK)`: they wrap arbitrary effects (damage / destroy / …), so forcing
`SPELL_ON_STACK` would erase every non-spell wrapped category. The correct migration is
`targetSpec() = wrapped.targetSpec()`, but that is **unsafe until step 9**: an unmigrated wrapped leaf
still returns `TargetSpec.NONE` from the default `targetSpec()` while its legacy `canTarget*` overrides
return the real values, so a wrapper delegating through `targetSpec()` now would derive `false` where
the leaf is `true`. Many leaves (all unvalidated permanent-only / player-only / metadata-only records)
are still unmigrated, so delegation is only reliable once step 9 runs after every leaf. Left in the
baseline: `SPELL_ON_STACK` in-scope count = **2** (the two `Triggering*`); the 3 `Conditional*`/`May`
wrappers stay in the `(delegated)` bucket.

### Step-10 note (recorded now, not acted on)

Once step 10 deletes `canTargetSpell()` from `CardEffect`, `ChangeColorTextEffect`'s record-component
accessor `canTargetSpell()` will no longer `@Override` an interface method, and the two consumers that
read the spell capability polymorphically — `SpellCastingService` line 664 (`CardEffect::canTargetSpell`)
and `EffectResolution.needsSpellTarget` — will no longer compile against a `CardEffect` reference.
Step 10 must either keep a spell-capability reader for this one dual effect or route the two consumers
through a `SPELL_ON_STACK`-aware helper. The `canTargetSpell` component itself must survive (wire
format); only the interface method goes.

### Oracle / judgment calls

- No behavior narrowed or widened for any of the 16: pure spell effects reproduce the `canTargetSpell`
  boolean exactly and the interpreter is a total no-op for spell targets; `ChangeColorTextEffect`
  preserves all three targeting booleans and adds only an inert `requireBattlefieldTarget` on the
  permanent path. No web ruling needed (behavior-preserving declarative migration).
- All 16 are `benign` — no counter/copy/retarget/text-change effect calls `checkProtection`, and none
  overrode `isDamageOrDestruction`.

### Audit re-run + lockstep

`python scripts/targetspec-audit.py` re-run (idempotent) to regenerate `targetspec-baseline.txt` +
`TARGETSPEC_MATRIX.md`. Records in scope 154 → **138** (−16); sum of baseline counts 204 → **188**
(−16, all single overrides: 15×`canTargetSpell` + 1×`canTargetPermanent` on `ChangeColorTextEffect`).
`canTargetSpell` overrides 20 → **5** (the 5 delegating wrappers); `canTargetPermanent` overrides
49 → **48**. `SPELL_ON_STACK` in-scope count → **2** (the `Triggering*` wrappers). `@ValidatesTarget`
methods unchanged at **34** (no validators existed for these effects, none deleted). Category counts
now: NONE 19, PERMANENT 31, PLAYER 72, PLAYER_OR_PERMANENT 11, SPELL_ON_STACK 2. Diff verified: exactly
the 16 expected records left the baseline, no additions. `effect-dispatch-baseline.txt` NOT touched
(no `instanceof`-on-effect counts changed).

**Tests run** (one filtered `:magical-vibes-application:test` invocation, `-x
:magical-vibes-frontend:buildAngular`, `BUILD SUCCESSFUL`): fixed regression set —
`WrackWithMadnessTest`, `DarkNourishmentTest`, `FireballTest`, `EffectDispatchRatchetTest`,
`TargetSpecRatchetTest` (both ratchets green — baseline shrank monotonically), `ValidTargetServiceTest`,
`TargetLegalityServiceTest`, `TargetValidationServiceSpecTest`; card tests covering the migrated
effects — `CounterspellTest`/`CancelTest` (CounterSpell), `ManaLeakTest` (CounterUnlessPays),
`CounterlashTest` (Counterlash), `SpellSwindleTest` (CounterSpellAndCreateTreasureTokens),
`VexingShusherTest` (MakeTargetSpellUncounterable), `TwincastTest`/`NaruMehaMasterWizardTest`
(CopySpell — plain + filtered), `DeflectionTest` (ChangeTargetOfTargetSpellWithSingleTarget),
`RedirectTest`/`WildRicochetTest` (ChangeTargetOfTargetSpellToSource / ChooseNewTargetsForTargetSpell),
`GlamerdyeTest` (ChangeColorText spell+permanent dual), `MindBendTest` (ChangeColorText permanent-only).

---

## Step 8 — Player-only (canTargetPlayer, not canTargetPermanent)  (2026-07-15)

Migrated the **72 player-only records** (the matrix's player-only bucket: every effect overriding
`canTargetPlayer` but NOT `canTargetPermanent` — discard, mill, reveal-hand, search-library, life-set,
extra-turn, shuffle-library, etc.). All map to category `PLAYER`. **No validator files were read,
edited, or deleted this step: none of the 72 has a `@ValidatesTarget` method.** Player targeting is
enforced structurally by the player/permanent pre-split in `computeAllowedTargets` (rejects permanents
for a player-category effect) and each card's `PlayerPredicateTargetFilter` (opponent restrictions) —
both **unchanged** by this program.

### Interpreter is a total no-op for the PLAYER category — no new cast-time check added

Confirmed from step 2/3: in `TargetValidationService.validateSpec` the `PLAYER` arm does NO
`requireBattlefieldTarget`, and the trailing predicate/`harmful` block resolves the target via
`findPermanentById(targetId)` — which returns `null` for a player → early return. `benign(PLAYER)` also
has predicate `null` + harmful `false`, so even the no-op block does nothing. So migrating a player-only
effect onto `benign(PLAYER)` adds NOTHING at cast time; the derived `canTargetPlayer()` =
`PLAYER.includesPlayers()` = `true` reproduces the old boolean exactly, and every other derived legacy
value is unchanged (`PLAYER(f,t)` + benign). Behavior-preserving-EXACTLY.

### No OPPONENT category (per brief)

Opponent-ness lives on each card's `PlayerPredicateTargetFilter` today; the spec must not duplicate it
(invariant: no narrowing without an existing check). Every one of the 72 got `PLAYER`, never a narrower
value.

### 59 flat records → `benign(PLAYER)` (legacy override deleted)

The 58 records whose `canTargetPlayer()` was an unconditional `return true;` → `benign(PLAYER)`. Plus
**`ExchangeTargetPlayersLifeTotalsEffect`** → `new TargetSpec(TargetCategory.PLAYER, false, null,
false, 2)`, deleting BOTH its `canTargetPlayer` and `requiredPlayerTargetCount` overrides in one edit;
`playerTargetCount 2` reproduces the two-player requirement exactly (Axis of Mortality / Soul Conduit).
It is the ONLY `requiredPlayerTargetCount` override in this bucket — the audit's step-1 count of 2
included `MayEffect`'s delegating override, which is a step-9 wrapper, not a leaf.

### 13 conditional (starred) records → per-instance specs — DEVIATION FROM THE BRIEF'S BLANKET `benign(PLAYER)`

The brief said "targetSpec() = benign(PLAYER)" for the whole bucket, but 13 records override
`canTargetPlayer` **conditionally** (the matrix flags them `canTargetPlayer*`), returning `false` in a
branch where the effect targets nothing. A flat `benign(PLAYER)` would widen the derived
`canTargetPlayer` `false→true` in that branch — offering/requiring a player target where the effect has
none (the single-target boolean path, per step-3 precedent, is a live widening, not inert). Per
BEHAVIOR-PRESERVING-OR-STRICTER (and CLAUDE.md's rules-accuracy-first rule) each got a per-instance spec
reproducing its branch EXACTLY, mirroring the per-scope/per-recipient specs of steps 3–7:
- `scope == EachPermanentScope.TARGET_PLAYER ? benign(PLAYER) : NONE`: `BoostAllCreaturesEffect`,
  `PutCounterOnEachMatchingPermanentEffect`.
- `recipient == …TARGET_PLAYER ? benign(PLAYER) : NONE`: `DiscardEffect` (DiscardRecipient),
  `PlayerDestroysPermanentsEffect` (DestroyRecipient).
- `targetsPlayer ? benign(PLAYER) : NONE`: `DrawCardForTargetPlayerEffect`, `GainLifeEffect`,
  `SkipNextCombatPhaseEffect`.
- `targetPlayer ? benign(PLAYER) : NONE`: `ShuffleGraveyardIntoLibraryEffect`, `ShuffleLibraryEffect`.
- `!sacrificerIsController ? benign(PLAYER) : NONE`: `SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect`.
- amount-type-keyed: `DiscardOwnHandThenDrawEffect`
  (`amount instanceof DamageDealtToTargetPlayerThisTurn`), `DrawCardEffect`
  (`amount instanceof CardsDiscardedByTargetPlayerThisTurn`).
- **`DealDamageToEachMatchingPermanentEffect`** — the one record that ALSO overrode
  `isDamageOrDestruction()` (unconditionally `true`) while `canTargetPlayer` was conditional. A single
  `TargetSpec` can carry both: `scope == TARGET_PLAYER ? harmful(PLAYER) : new TargetSpec(NONE, true,
  null, false, 1)`. **Both branches keep `harmful=true`** so the derived `isDamageOrDestruction()` stays
  `true` in every scope (preserved exactly). The `NONE` branch means `validateSpec` never runs (guarded
  by `category() != NONE`), so no `checkProtection` is added; the `harmful(PLAYER)` branch runs the
  no-op PLAYER arm and resolves the player target to `null` → no `checkProtection` either. So `harmful`
  here is purely the `isDamageOrDestruction` carrier — inert in the interpreter (the effect damages
  permanents, and only ever *targets* a player).

### No escape hatches kept this step

Unlike step 5's Life/Library records (which kept `requireTargetPlayer` validators because the no-op
PLAYER category can't reproduce them), NONE of these 72 has a validator to keep — the player-target
guard for them is the structural pre-split, not a `@ValidatesTarget` method. So `@ValidatesTarget`
total is unchanged (34) and no validator file was touched.

### Oracle / judgment calls

- No behavior narrowed or widened for any of the 72: flat records reproduce the `canTargetPlayer`
  boolean exactly, the 13 conditionals reproduce their branch exactly, and the PLAYER category is a
  total interpreter no-op. No web ruling needed (behavior-preserving declarative migration).
- All 72 are `benign` except the `harmful` branch of `DealDamageToEachMatchingPermanentEffect` (kept
  solely to preserve its pre-existing `isDamageOrDestruction=true`); no player-only effect calls
  `checkProtection` (the target is a player, not a battlefield permanent).
- Non-bucket effects that also override `canTargetPlayer` were left untouched (verified by grep): the
  interface default, the delegating wrappers (`ConditionalEffect`, `ConditionalReplacementEffect`,
  `MayEffect`, `Triggering*ConditionalEffect`, the Clash wrappers), and the player+permanent /
  player-or-planeswalker effects that also override `canTargetPermanent` (`FlickerEffect`,
  `GrantKeywordEffect`, `LoseAllCreatureTypesEffect`, `SkipNextUntapEffect`, `DealDamageToEachTargetEffect`,
  `CreateTokenCopyOfTargetCreatureForTargetPlayerEffect`,
  `DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect`,
  `RevealTopCardsBottomThenDamageIfCopyRevealedEffect`) — all belong to other buckets.

### Audit re-run + lockstep

`python scripts/targetspec-audit.py` re-run to regenerate `targetspec-baseline.txt` +
`TARGETSPEC_MATRIX.md`. Records in scope 138 → **66** (−72); sum of baseline counts 188 → **114** (−74
= 72×`canTargetPlayer` + 1×`requiredPlayerTargetCount` (Exchange) + 1×`isDamageOrDestruction`
(DealDamageToEachMatchingPermanent)). `PLAYER` in-scope count → **0** (fully migrated).
`canTargetPermanent` overrides unchanged at **48** (untouched this step); `@ValidatesTarget` methods
unchanged at **34** (no validators existed for these effects). Remaining category counts: `NONE` 19,
`PERMANENT` 31, `PLAYER_OR_PERMANENT` 11, `SPELL_ON_STACK` 2. The domain module was recompiled after
every ~15 records (4 clean compiles). `effect-dispatch-baseline.txt` NOT touched (no
`instanceof`-on-effect counts changed).

**Tests run** (one filtered `:magical-vibes-application:test` invocation, `-x
:magical-vibes-frontend:buildAngular`, `BUILD SUCCESSFUL`): fixed regression set —
`WrackWithMadnessTest`, `DarkNourishmentTest`, `FireballTest`, `EffectDispatchRatchetTest`,
`TargetSpecRatchetTest` (both ratchets green — baseline shrank monotonically), `ValidTargetServiceTest`,
`TargetLegalityServiceTest`, `TargetValidationServiceSpecTest`; one card test per family plus the tricky
cases — `MindRotTest` (DiscardEffect TARGET_PLAYER — discard), `TraumatizeTest` (MillHalfLibraryEffect —
mill), `JestersCapTest` (SearchTargetLibraryForCardsToExileEffect — search-library), `VraskaRelicSeekerTest`
(SetTargetPlayerLifeToSpecificValueEffect — life-set), `AxisOfMortalityTest`
(ExchangeTargetPlayersLifeTotalsEffect — the 2-player-target spec), `BoggartForagerTest`
(ShuffleLibraryEffect `targetPlayer=true` → benign(PLAYER) branch), `PonderTest` (ShuffleLibraryEffect
`targetPlayer=false` → NONE branch, non-targeting).

---

## Step 9 — Trigger/combat/multi-target permanent-slot, metadata-only, and the delegating wrappers  (2026-07-15)

Migrated the **final 66 in-scope records** — everything left in `targetspec-baseline.txt` after step 8.
**No `@ValidatesTarget` method was touched this step: none of the 66 has a validator** (`records with a
validator: 0`). The audit's `validators total` stays **34** (the step-2..8 escape hatches). After this
step **`targetspec-baseline.txt` is EMPTY** — all 277 in-scope records expose their targeting through
one derived `TargetSpec targetSpec()`. `@ValidatesTarget` now survives only as the escape hatch for
step 10.

### How the two null-target hazards were resolved (read before the buckets)

`TargetValidationService.validateSpec` runs only inside `checkEffectTargets`, whose call sites are:
`TargetLegalityService.checkSpellTargeting` (a `null` `targetId` short-circuits at "Invalid target"
BEFORE `checkEffectTargets`), `validateActivatedAbilityTargeting` (returns early when
`minTargets==0 && targetId==null`), the graveyard/zone/retarget entry points (targetId supplied), and
`AiTargetSelector.isValidPermanentTarget` (passes a concrete permanent id). The **multi-target cast
paths** (`validateMultiSpellTargets` / `validateMultiTargetAbility`) do NOT call `checkEffectTargets`.
Consequences that drive the category choices below:
- A **non-no-op** category (`PERMANENT`/`CREATURE`/`LAND`) is safe for a **single-target** or
  **trigger/ETB/combat-slot** effect: its `requireBattlefieldTarget` either runs on the real permanent
  target (inert) or never runs (trigger effects don't reach `checkEffectTargets`).
- For a **multi-target** effect (targets ride `entry.getTargetIds()`/`targetsForGroup(...)`, single
  `targetId` null — step-5's Garruk lesson), a non-no-op category would `requireBattlefieldTarget` on
  the null single-target and throw, so the **no-op `PLAYER_OR_PERMANENT`** is used (documented inert
  `canTargetPlayer` widening — multi-target selection goes through per-position filters, not the
  single-target boolean). Each candidate effect's handler was inspected for `getTargetIds`/
  `targetsForGroup` to classify single vs multi.

### The permanent+player decision (brief item 1) — PLAYER_OR_PERMANENT, recorded

Every permanent+player record uses **`PLAYER_OR_PERMANENT`** (never `ANY_TARGET`). Rationale, checked
against `ValidTargetService`'s structural any-target backstop (lines ~366-378, the `allAnyTarget`
block): that backstop is an **offering-time** restriction that reads the derived
`canTargetPermanent`/`canTargetPlayer` booleans — both `true` under `PLAYER_OR_PERMANENT`, unchanged —
so it keeps firing exactly as before (a no-target-filter perm+player spell still offers only
creature/planeswalker/player). It never runs at cast, and these records have **no validator = no
cast-time type gate**, so the no-op `PLAYER_OR_PERMANENT` is behavior-preserving-at-cast. `ANY_TARGET`
was rejected: it would (a) add a cast gate these validator-less effects never had and (b) `requireTarget`
would throw on the null single-`targetId` of the multi-target member (`DealDamageToEachTargetEffect`).

### Bucket 1 — trigger/ETB/combat + multi-target permanent records (no validator)

- **Single-target / trigger / ETB / combat permanent-only** get `benign`/`harmful(PERMANENT)` (inert or
  never-run `requireBattlefieldTarget`): `AttachAllAurasToAnotherPermanentEffect`,
  `AttachSourceAuraToTargetCreatureEffect`, `AttachSourceEquipmentToTargetCreatureEffect`,
  `BecomeCopyOfTargetCreatureEffect`, `BecomeCopyOfTargetCreatureUntilEndOfTurnEffect`,
  `ExileTargetOnControllerSpellCastEffect`, `ExileTargetOpponentPermanentOnDrawEffect`,
  `ExileTargetPermanentAndImprintEffect`, `ExileTargetPermanentUntilSourceLeavesEffect`,
  `GainControlUntapAndHasteTargetEffect`, `MakeTargetCreatureUnpreparedEffect`,
  `RemoveCounterFromTargetPermanentEffect` (all benign); `harmful(PERMANENT)` (kept
  `isDamageOrDestruction=true`) — `DestroyTargetPermanentAndDamageControllerIfDestroyedEffect`,
  `DestroyCombatOpponentAtEndOfCombatEffect`, `DestroySubtypeCombatOpponentEffect`.
- **Multi-target permanent** get `PLAYER_OR_PERMANENT` (no-op, verified via handler `getTargetIds`/
  `targetsForGroup`): benign — `FightTargetsEffect`, `TargetDealsPowerDamageToTargetEffect`,
  `MoveCounterFromTargetCreatureToTargetCreatureEffect`, `MustBlockTargetCreatureEffect`,
  `AttachTargetEquipmentToTargetCreatureEffect`, `MakeTargetCopyOfTargetCreatureUntilNextTurnEffect`;
  harmful (kept `isDamageOrDestruction`) — `DestroyEachTargetPermanentEffect`,
  `DestroyUpToTargetsThenReturnFromGraveyardEffect`.
- **Permanent+player** get `PLAYER_OR_PERMANENT`: `CreateTokenCopyOfTargetCreatureForTargetPlayerEffect`
  (benign, multi), `DealDamageToEachTargetEffect` (benign — never overrode `isDamageOrDestruction`; its
  protection is the multi-target structural core, so `harmful` would be a widening, not a preservation),
  `DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect` and
  `RevealTopCardsBottomThenDamageIfCopyRevealedEffect` (harmful — kept `isDamageOrDestruction`).
- **Per-scope conditional records** (switch reproduces the old conditional booleans exactly; all
  single-target per handler): `FlickerEffect` (`TARGET`→PERMANENT, `TARGET_PLAYERS_PERMANENTS`→PLAYER,
  else NONE), `SkipNextUntapEffect` (same shape), `GrantActivatedAbilityEffect`
  (`GrantScope.TARGET`→PERMANENT else NONE), `LosesAllAbilitiesEffect`
  (`UNTIL_END_OF_TURN`→PERMANENT else NONE), `RemoveKeywordEffect` (`TARGET`→PERMANENT, `SELF`→self-NONE,
  else NONE), `MakeCreatureUnblockableEffect` (`selfTargeting`→self-NONE else PERMANENT), `RegenerateEffect`
  (`targetsPermanent`→PERMANENT else self-NONE), `LoseAllCreatureTypesEffect` (`TARGET`→PERMANENT,
  `TARGET_PLAYERS_CREATURES`→PLAYER, else NONE).
- **`GrantKeywordEffect`** uses a per-scope switch carrying the predicate:
  `TARGET`→`benign(PERMANENT, filter)`, `TARGET_PLAYERS_CREATURES`→`benign(PLAYER)`, `SELF`→self-NONE,
  default→NONE. The `filter` moves into the spec's predicate field (reproducing the old
  `targetPredicate() = scope==TARGET ? filter : null` for the end-step/saga pipelines). Where `filter`
  is non-null and the effect is a single-target spell, `validateSpec` now enforces it at cast — the same
  stricter-and-more-correct predicate enforcement documented for `UntapPermanents`/`MustBlockSource` in
  step 5; almost all `GrantKeyword` usages have a `null` filter (no change).
- **`BecomeCreatureTypeWithBasePowerToughnessEffect`** (the step-1 WARNING record, redundantly
  `canTargetPermanent()==false`) becomes `TargetSpec.NONE`.

### Bucket 2 — `isSelfTargeting`-only (metadata) become `new TargetSpec(NONE, false, null, true, 1)`

`BecomePreparedEffect`, `BoostSelfAndLoseKeywordEffect`, `BoostSelfEffect`,
`DoubleSelfPowerToughnessEffect`, `IncrementTriggerEffect`, `PutCounterOnSelfThenTransformIfThresholdEffect`,
`PutCountersOnSelfEffect`, `PutSlimeCounterAndCreateOozeTokenEffect`, `RemoveAllCountersFromSelfEffect`,
`RemoveCounterFromSourceEffect`, `RemoveCountersAndTransformSelfEffect`, `TapAndTransformSelfEffect`. Plus
`NthSpellCastTriggerEffect` becomes `new TargetSpec(NONE, false, null,
resolvedEffects.stream().anyMatch(CardEffect::isSelfTargeting), 1)` (only `isSelfTargeting` was
delegated; its other booleans were its own `NONE` defaults and stay `NONE`).

### Bucket 3 — `targetPredicate` records

- `MoveDyingSourceCountersToTargetCreatureEffect`, `PutCounterOnTargetForEachDyingSourceCounterEffect`
  (both `ON_DEATH`, single-target) become `benign(PERMANENT, new PermanentIsCreaturePredicate())`. The
  predicate moves into the spec (preserving the derived `targetPredicate()`); `validateSpec` never runs
  for a death trigger, so no cast-time enforcement is added. Unused `PermanentPredicate` import pruned.
- `GrantKeywordEffect`'s predicate — handled in bucket 1 above.
- `PutCounterOnTargetPermanentEffect` — its `canTargetPermanent()` was `predicate == null`, so
  `targetSpec() = predicate == null ? benign(PERMANENT) : NONE`. Its `targetPredicate` **record
  component** already satisfies the interface `targetPredicate()` (like `ChangeColorTextEffect`'s
  `canTargetSpell` component in step 7), so it is NOT copied into the spec — the accessor keeps
  returning it independently, and the spec's `null` predicate preserves the old "no cast-time predicate
  gate" behavior.

### Bucket 4 — `isDamageOrDestruction`-only (non-targeting damage siblings) become `new TargetSpec(NONE, true, null, false, 1)`

`DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect`,
`DealDamageToAllCreaturesTargetControlsEffect`,
`DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect`,
`DealDamageToEachPlayerControllingMatchingPermanentEffect`, `DestroySelfAtEndOfCombatEffect`. `harmful`
is a pure `isDamageOrDestruction` carrier here (category `NONE` means `validateSpec` never runs, so no
`checkProtection` is added) — same pattern as step 8's `DealDamageToEachMatchingPermanentEffect` NONE
branch.

### Bucket 5 — the delegating wrappers use FULL delegation

- `ConditionalEffect`, `MayEffect`, `TriggeringCardConditionalEffect`,
  `TriggeringPermanentConditionalEffect`, `IfWonClashEffect`, `IfLostClashEffect` become
  `targetSpec() = wrapped.targetSpec()`.
- `ConditionalReplacementEffect` becomes `targetSpec() = upgradedEffect.targetSpec()`. Verified from every
  non-enters-tapped card usage (Brimstone Volley, Galvanic Blast, Elder Cathar, Huatli's Spurring, ...)
  that base and upgraded are always the SAME targeting shape (magnitude-only replacement), so
  `upgraded`'s spec reproduces the old `base OR upgraded` booleans exactly; the enters-tapped
  convenience form has a `null` base and a non-targeting `upgraded`. Class javadoc updated. (The spec
  interpreter still unwraps this wrapper to `baseEffect()` for validation, unchanged.)
- `ClashEffect` computes from its aggregate (`onWin` + `beforeClash`): `perm`/`player` booleans map to
  `PLAYER_OR_PERMANENT`/`PERMANENT`/`PLAYER`/`NONE`, benign, non-self. (All current clash cards
  aggregate to `NONE`; the mapping preserves the exact booleans for any future targeting reward.)

**Deliberate strict-improvement from full delegation (documented per BEHAVIOR-PRESERVING-OR-STRICTER):**
these wrappers previously delegated only a SUBSET of the targeting booleans (e.g. `ConditionalEffect`
delegated `canTargetGraveyard` but left `canTargetAnyGraveyard`/`canTargetExile`/
`targetsControllersGraveyardOnly`/`targetPredicate`/`requiredPlayerTargetCount` at the wrapper's own
defaults; the `Clash`/`Triggering*` wrappers left `isDamageOrDestruction`/`canTargetSpell` at default).
`targetSpec() = wrapped.targetSpec()` now delegates ALL of them, which is strictly more faithful (a
wrapper honestly reports the wrapped effect's real targeting). This can only *widen* a previously-false
boolean to the wrapped effect's true value (e.g. a "may deal damage" `MayEffect` now reports
`isDamageOrDestruction=true`, so the offering filters protected permanents) — never narrow. Verified by
the regression + `StepTriggerServiceTest` + `CardEffectTargetingConsistencyTest` + the wrapper card
tests; no card relied on the old partial-delegation gap.

### Oracle / judgment calls

- No card behavior narrowed illegally. `harmful` flags exactly reproduce each record's prior
  `isDamageOrDestruction` (bucket 1/4) except where a non-no-op harmful category adds `checkProtection`
  at cast for a single-target destroy/damage effect — the same stricter-and-more-correct resolution of
  the HARMFUL-FLAG-vs-narrowing tension established in steps 3-8 (protection = can't be targeted).
- The `PLAYER_OR_PERMANENT` `canTargetPlayer` widening on permanent-only multi-target effects is inert
  (per-position filters govern multi-target selection) — step-5 precedent.
- No web ruling needed (behavior-preserving-at-cast declarative migration; the documented widenings are
  offering-consistency / rules-correct-protection improvements).

### Audit re-run + lockstep

`python scripts/targetspec-audit.py` re-run: **records in scope 66 -> 0**; sum of baseline counts
**114 -> 0**; every category in-scope count -> **0**; `targetspec-baseline.txt` is EMPTY (both ratchets
green — the baseline shrank to nothing). `@ValidatesTarget` total unchanged at **34** (no validator
touched; `records with a validator: 0`). `TARGETSPEC_MATRIX.md` regenerated. `effect-dispatch-baseline.txt`
NOT touched (no `instanceof`-on-effect counts changed).

**Tests run** (one filtered `:magical-vibes-application:test` invocation `-x
:magical-vibes-frontend:buildAngular`, `BUILD SUCCESSFUL`, plus a separate `:magical-vibes-ai:test`):
fixed regression set — `WrackWithMadnessTest`, `DarkNourishmentTest`, `FireballTest`,
`EffectDispatchRatchetTest`, `TargetSpecRatchetTest` (both ratchets green — baseline now empty),
`ValidTargetServiceTest`, `TargetLegalityServiceTest`, `TargetValidationServiceSpecTest`; step-9
verification — `com.github.laxika.magicalvibes.service.turn.StepTriggerServiceTest` (trigger-slot
effects), `CardEffectTargetingConsistencyTest`, `OblivionRingTest` + `FiendHunterTest`
(ExileTargetPermanentUntilSourceLeaves ETB), `PacifismTest` (aura), and the AI simulator
`:magical-vibes-ai` `com.github.laxika.magicalvibes.ai.GameSimulatorTest`; one card test per migrated
family — `FistfulOfForceTest`/`GiltLeafAmbushTest`/`HoardersGreedTest` (Clash), `BrimstoneVolleyTest`/
`GalvanicBlastTest` (ConditionalReplacement), `BloodFeudTest` (FightTargets), `AncientAnimusTest`
(PutCounterOnTargetPermanent), `DregsOfSorrowTest` (DestroyEachTargetPermanent), `HuntDownTest`
(MustBlock), `BrassSquireTest` (AttachTargetEquipment), `ShapesharerTest` (MakeTargetCopy/BecomeCopy),
`GriefTyrantTest` (PutCounterOnTargetForEachDyingSourceCounter), `ScoldingAdministratorTest`
(MoveDyingSourceCounters), `ActOfTreasonTest`/`BanishingKnackTest` (GrantKeyword/GrantActivatedAbility),
`MirrorweaveTest`, `SentryOakTest` (Clash + BoostSelfAndLoseKeyword), `VancesBlastingCannonsTest`
(NthSpellCast + MayEffect + transform). All green. Effects without a dedicated card test are covered by
the family representative above and the AI simulator sweep.

---

## Step 10 — Delete the eleven legacy targeting methods from CardEffect  (2026-07-15)

**The goal of the program.** `CardEffect` now declares ONLY `targetSpec()` and `isPowerToughnessDefining()`.
The eleven legacy targeting methods — `canTargetPlayer`, `canTargetPermanent`, `canTargetSpell`,
`canTargetGraveyard`, `canTargetAnyGraveyard`, `canTargetExile`, `targetsControllersGraveyardOnly`,
`targetPredicate`, `isSelfTargeting`, `requiredPlayerTargetCount`, `isDamageOrDestruction` — are DELETED.
Precondition verified first: `refactor-docs/targetspec-baseline.txt` was EMPTY (step 9 migrated all 277
records). Every reader was repointed at `targetSpec()` BEFORE the deletion, then the compiler proved
completeness (all modules — engine, ai, domain, application, testFixtures, tests — compile clean).
**This step changes NO behavior — only the accessor** (except the two documented, inert widenings that the
earlier steps' `harmful`/`PLAYER_OR_PERMANENT` choices already baked in). All tests green.

### The accessor mapping every reader now uses

| deleted method | spec accessor it became |
|---|---|
| `e.canTargetPlayer()` | `e.targetSpec().category().includesPlayers()` |
| `e.canTargetPermanent()` | `e.targetSpec().category().includesPermanents()` |
| `e.canTargetGraveyard()` | `e.targetSpec().category().isGraveyard()` (new `TargetCategory` helper) |
| `e.canTargetAnyGraveyard()` | `e.targetSpec().category() == ANY_GRAVEYARD_CARD` |
| `e.targetsControllersGraveyardOnly()` | `e.targetSpec().category() == CONTROLLERS_GRAVEYARD_CARD` |
| `e.canTargetExile()` | `e.targetSpec().category() == EXILE_CARD` |
| `e.canTargetSpell()` | `EffectResolution.targetsSpellOnStack(e)` (see dual note) |
| `e.targetPredicate()` | `EffectResolution.targetPredicateOf(e)` (see dual note) |
| `e.isSelfTargeting()` | `e.targetSpec().selfTargeting()` |
| `e.requiredPlayerTargetCount()` | `e.targetSpec().playerTargetCount()` |
| `e.isDamageOrDestruction()` | `e.targetSpec().harmful()` |

`ValidTargetService`'s combined `canTargetPermanent() && isDamageOrDestruction()` became
`includesPermanents() && harmful()` exactly as the brief specified.

### Two record-component duals — the only non-mechanical part

Two effects expose a targeting capability through a **record component** whose value DIVERGES from
`targetSpec()`, so a naive `targetSpec()` read would silently drop it (a behavior change / bug):
- **`ChangeColorTextEffect.canTargetSpell`** (Glamerdye): `targetSpec()` is `benign(PERMANENT)`, but the
  effect also targets a spell via its `canTargetSpell` component (Mind Bend = false, Glamerdye = true).
- **`PutCounterOnTargetPermanentEffect.targetPredicate`**: `targetSpec().predicate()` is always `null`
  (the spec deliberately carries no predicate, to avoid a cast-time gate — step 9 bucket 3), but the
  saga-chapter / end-step targeting pipelines honour a targeting restriction on the effect's dedicated
  `targetPredicate` component (`withTargetRestriction`).

Both were resolved with a **static helper in `EffectResolution` (domain)** that consults the component:
- `EffectResolution.targetsSpellOnStack(e)` = `category()==SPELL_ON_STACK || (e instanceof
  ChangeColorTextEffect c && c.canTargetSpell())`.
- `EffectResolution.targetPredicateOf(e)` = `e instanceof PutCounterOnTargetPermanentEffect p ?
  p.targetPredicate() : e.targetSpec().predicate()`.

**Why the helpers live in `EffectResolution` (domain), not engine/ai.** They use `instanceof` on a concrete
effect, which WOULD be an effect-dispatch coupling violation under `EffectDispatchRatchetTest` /
`scripts/effect-coupling-audit.py` — but BOTH scanners only scan `magical-vibes-engine` and
`magical-vibes-ai`, never `magical-vibes-domain`, and `EffectResolution` already `instanceof`-dispatches
concrete effects freely. So the helpers are invisible to the dispatch ratchet, and every engine/ai spell /
predicate reader routes through them (no new `instanceof` in engine/ai). **`effect-dispatch-baseline.txt`
is byte-for-byte UNCHANGED** (verified by diff). All previously-polymorphic `canTargetSpell` /
`targetPredicate` readers now call the helper, preserving the exact value for every effect.

### New permanent API added (not bridge cruft — kept)

- **`TargetCategory.isGraveyard()`** — `GRAVEYARD_CARD || ANY_GRAVEYARD_CARD || CONTROLLERS_GRAVEYARD_CARD`,
  the successor to `canTargetGraveyard()` (the old boolean was exactly this three-state test; the redundant
  `|| canTargetAnyGraveyard()` some readers appended was always subsumed and is now dropped). Sits beside
  `includesPermanents()` / `includesPlayers()` as permanent category API.
- **`EffectResolution.targetsSpellOnStack` / `targetPredicateOf`** — the two dual helpers above.

### Files rewritten (the compiler-verified completeness list)

- **domain** — `CardEffect.java` (deleted the 11 methods; kept `targetSpec()` + `isPowerToughnessDefining()`;
  pruned the now-unused `PermanentPredicate` import); `TargetCategory.java` (+`isGraveyard()`);
  `EffectResolution.java` (+two helpers; rewrote `collectTargetTypes`, the ETB branch of
  `computeAllowedTargets`, `needsSpellTarget`); reader rewrites in `ActivatedAbility.java`, `Card.java`,
  `ClashEffect.java`, `NthSpellCastTriggerEffect.java`; doc-only fixes to `ChangeColorTextEffect.java`,
  `GrantKeywordEffect.java`, `UntapPermanentsEffect.java`, `MustBlockSourceEffect.java`, `TapUntapScope.java`
  (stale `{@link #targetPredicate()}` / `canTargetSpell()` references).
- **engine** — `ValidTargetService`, `TargetLegalityService`, `SpellCastingService`, `StepTriggerService`,
  `StackResolutionService`, `TriggerCollectionService`, `TriggerTargetCollector`,
  `SpellCastTriggerCollectorService`, `EnterTriggerCollectorService`, `DeathTriggerCollectorService`,
  `CombatTriggerService`, `CombatAttackService`, `CombatBlockService`, `GraveyardTargetingService`,
  `BattlefieldEntryService`, `ETBTokenTargetService`, `ActivatedAbilityExecutionService`,
  `TriggeredAbilityQueueService`, `MayAbilityHandlerService`, `MayCastHandlerService`,
  `PermanentControlTargetValidators`.
- **ai** — `AiTargetSelector`, `AiDecisionEngine`, `simulation/GameSimulator`.
- **test** — `MayCastHandlerServiceTest` + `ValidTargetServiceTest` (anonymous `CardEffect` stubs that
  overrode `canTarget*` now override `targetSpec()`); `ReturnToHandEffectHandlerTest` (an
  `effect.canTargetPlayer()` assertion → `targetSpec().category().includesPlayers()`).

### `@ValidatesTarget` after step 10 — the escape hatch, unchanged at 34

The prompt's step-10 clause "`@ValidatesTarget` survives only as the escape hatch" is already the state:
the 34 kept validators are the step-2..8 escape hatches (opponent/controller-compare graveyard validators,
null-tolerant divided-damage, no-op-PLAYER Life/Library guards, attachment/combat-state checks, the two
aura/attach non-target validators). **`PermanentControlTargetValidators.validatePutTargetOnTopOfLibrary`**
— kept in step 4 solely because it READ `effect.canTargetPermanent()` (the brief forbade touching that
reader before step 10) — was repointed to `effect.targetSpec().category().includesPermanents()`. It is now
redundant (its per-scope `PERMANENT` spec runs the same `requireBattlefieldTarget` in `validateSpec`), but
KEEPT-VALIDATORS-ARE-KEPT-WHOLE: a redundant structural re-check is harmless, so it stays. No validator was
deleted this step.

### The OTHER audit (step 5 of the brief) — repointed, lockstep verified

`scripts/effect-coupling-audit.py` detected an effect's "targeted role" via a `canTarget*(){return true}`
regex over the effect files. Post-migration NO effect overrides `canTarget*` (all deleted), so that
detection would collapse to near-empty. Repointed `load_targeted_types` to the successor signal: an effect
overrides `targetSpec()` to a **non-NONE** spec — detected with a new brace-depth `targetspec_body()`
extractor (handles nested switch/lambda braces) that is targeted iff the body uses a `benign(`/`harmful(`
factory or names a `TargetCategory` other than `NONE`. Docstring + the matrix's generated
"validator coverage gap" prose updated to say "override `targetSpec()` to a non-NONE spec".
**Lockstep check:** `EffectDispatchRatchetTest`'s header only replicates the INSTANCEOF-counting rules
(read both headers to confirm), NOT the coverage-gap / targeted-role rule — so per the brief only the
script changed. Re-ran `python scripts/effect-coupling-audit.py`: the effect-dispatch baseline is UNCHANGED
(diff empty — no `instanceof`-on-effect counts changed), and the coverage-gap now reads 221 targeted types
without a hand validator (expected: the migration replaced per-effect validators with the declarative spec
interpreter for the structural cases; the gap is informational matrix output, not a ratchet).

### Tests run (all green, `BUILD SUCCESSFUL`)

- **Compile proof:** `compileTestJava` across every module (main + testFixtures + test) clean.
- **AI module** (one `:magical-vibes-ai:test` invocation): `AiTargetSelectorTest`, `AiDecisionEngineTest`,
  `GameSimulatorTest`.
- **Application module** (two filtered `:magical-vibes-application:test` invocations, `-x
  :magical-vibes-frontend:buildAngular`): fixed regression set — `WrackWithMadnessTest`,
  `DarkNourishmentTest`, `FireballTest`, `EffectDispatchRatchetTest`, `TargetSpecRatchetTest` (baseline
  still empty — both ratchets green), `ValidTargetServiceTest`, `TargetLegalityServiceTest`,
  `TargetValidationServiceSpecTest`; rewritten-service unit tests — `SpellCastingServiceTest`,
  `StepTriggerServiceTest`, `AbilityActivationServiceTest`, `ETBTokenTargetServiceTest`,
  `GameBroadcastServiceTest`, `MayCastHandlerServiceTest`, `TriggerTargetCollectorTest`,
  `ReturnToHandEffectHandlerTest`, `CardEffectTargetingConsistencyTest` (still passes — it already accepted
  a `targetSpec()` override as declaring targeting since step 5; the vestigial `canTarget*` name list is
  now dead but harmless).
- **One card test per earlier-step family** — step 2 `SpittingEarthTest`/`IncinerateTest`/`TerrorTest`;
  step 3 `ChandrasOutrageTest`/`RemedyTest`/`HarmsWayTest`; step 4 `CruelEdictTest`/`ActOfAggressionTest`/
  `AethertowTest` (the repointed `PutTargetOnTopOfLibrary` validator); step 5 `GiantGrowthTest`/
  `GarrukWildspeakerTest` (multi-target untap)/`VoltaicServantTest`+`JandorsSaddlebagsTest`+`TowerAboveTest`
  (the `targetPredicate` pipelines); step 6 `SnapcasterMageTest`/`DisentombTest`/`SurgicalExtractionTest`
  (graveyard categories); step 7 `CounterspellTest`/`GlamerdyeTest`+`MindBendTest` (the `ChangeColorText`
  spell dual — the `targetsSpellOnStack` helper); step 8 `MindRotTest`/`AxisOfMortalityTest`/`PonderTest`;
  step 9 `BrimstoneVolleyTest`/`AncientAnimusTest` (`PutCounterOnTargetPermanent`)/`FistfulOfForceTest`
  (Clash)/`GriefTyrantTest` (death-trigger predicate).

`refactor-docs/effect-dispatch-baseline.txt` NOT regenerated with changes (diff-verified unchanged);
`targetspec-baseline.txt` remains empty; the TargetSpec migration program is complete through its goal
(step 11 close-out remaining: retire the ratchet + refresh `agent-docs/`).
