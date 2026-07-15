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
