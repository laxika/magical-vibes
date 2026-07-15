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
