# Effect-system refactor — progress log

Permanent record of the 8-step program that moves every piece of engine/AI
knowledge about a specific `CardEffect` type into ONE registered place, instead
of scattering it across `instanceof` checks in many subsystems. Each step is
run in a fresh session; this file is the hand-off between them. Read it first.

## Program overview (8 steps)

1. **Audit** — build the effect-coupling matrix and this progress file
   (read-only; no production source touched).
2. **Ratchet** — an architecture test that stops NEW `instanceof`-on-effect
   dispatch from being added, driven by the step-1 baseline.
3. **Validators** — close the `@ValidatesTarget` coverage gap (the
   Fireball-burns-a-Plains class of bug).
4. **Targeting** — unify the three spell-target validation paths behind one
   shared core.
5. **AI metadata part 1** — capability interfaces + migrate SpellEvaluator's
   damage/removal families onto them.
6. **AI metadata part 2** — remaining SpellEvaluator families,
   InstantCategoryClassifier, AiTargetSelector.
7. **AI metadata part 3** — GameSimulator, HardAiDecisionEngine, AiManaManager
   (the mana-producing family; widen `ManaProducingEffect`).
8. **Close-out** — re-audit, tighten the ratchet baseline, update `agent-docs/`.

The reference registry zones that are ALREADY correct (not part of the problem):
resolution dispatch (`service/effect/normalfx` + `EffectHandlerRegistry`),
static/continuous dispatch (`service/effect/staticfx` +
`StaticEffectHandlerRegistry`), target validators (`service/validate` +
`@ValidatesTarget` + `TargetValidatorRegistry`).

---

## Step 1 — Audit  (2026-07-14)

**Deliverables produced:**
- `scripts/effect-coupling-audit.py` — no-argument, deterministic, idempotent
  scanner. Run it as `python scripts/effect-coupling-audit.py` from the repo
  root. It regenerates the two files below.
- `refactor-docs/EFFECT_COUPLING_MATRIX.md` — human report (consumer-file
  table, effect-type→consumers table, per-module/per-package summary, exempt
  counts, validator-coverage-gap section).
- `refactor-docs/effect-dispatch-baseline.txt` — machine-readable ratchet
  baseline, one `path=count` line per offending file, sorted alphabetically.
  **Format is a contract** (step 2's ratchet test parses it) — do not change.

**Counting rules (also documented at the top of the audit script; step 2's
Java ratchet test must replicate them exactly):**
- Effect-type universe = every top-level type under
  `magical-vibes-domain/.../model/effect/` (file name minus `.java`): 973 types
  (934 records, 29 enums, 9 interfaces, 1 class).
- A `instanceof X` is a **violation** iff X is an effect type, X is not a
  structural wrapper, X is not a capability/marker interface, and the consuming
  file is not under `service/effect/**` or `service/validate/**`.
- Structural wrappers exempted (mirrors `EffectResolutionService`):
  `ConditionalEffect`, `ConditionalReplacementEffect`, `MayEffect`,
  `MayPayManaEffect`, `MayPayTapPermanentsEffect`, `ChooseOneEffect`,
  `CostEffect`, `CardEffect`.
- Capability interfaces exempted automatically (any effect file whose type is
  declared `interface`): `CardEffect`, `ChooseCardNameEffect`,
  `ChooseColorEffect`, `CostEffect`, `EnterCreatureConditionalEffect`,
  `ManaProducingEffect`, `ManaRestriction`, `ReplacementEffect`,
  `StateTriggerPredicate`.

**Headline numbers:**
- **Total violations: 686** across **55 files**, dispatching on **~200
  distinct concrete effect types**.
- Per module: **engine 512**, **ai 174**.
- Per package (top): engine `service/battlefield` 91, `service/ability` 79,
  `service/input` 78, `service/combat` 77, `service/(root)` 53,
  `service/cast` 44, `service/turn` 29, `service/trigger` 26,
  `service/target` 19; ai root 161, ai/simulation 13.
- Exempt/structural (NOT counted): 31 effect-instanceof inside the exempt
  zones; 127 structural-wrapper/interface instanceof (top: ConditionalEffect
  43, MayEffect 32, ChooseOneEffect 17).

**Top 10 consumer files (file = violation count):**
| # | File | Count |
|---|------|-------|
| 1 | `magical-vibes-ai/.../ai/SpellEvaluator.java` | 65 |
| 2 | `magical-vibes-engine/.../service/input/MayAbilityHandlerService.java` | 61 |
| 3 | `magical-vibes-engine/.../service/battlefield/GameQueryService.java` | 55 |
| 4 | `magical-vibes-engine/.../service/ability/ActivatedAbilityExecutionService.java` | 42 |
| 5 | `magical-vibes-engine/.../service/combat/CombatDamageService.java` | 40 |
| 6 | `magical-vibes-engine/.../service/ability/AbilityActivationService.java` | 37 |
| 7 | `magical-vibes-ai/.../ai/HardAiDecisionEngine.java` | 30 |
| 8 | `magical-vibes-engine/.../service/battlefield/BattlefieldEntryService.java` | 29 |
| 9 | `magical-vibes-engine/.../service/trigger/TriggerCollectionService.java` | 21 |
| 10 | `magical-vibes-ai/.../ai/AiTargetSelector.java` | 21 |

`SpellEvaluator` at 65 confirms the exemption logic is right (the sanity target
was 40–75). No `normalfx`/`staticfx` handler appears as a violator.

**Validator coverage gap: 162 targeted effect types have no `@ValidatesTarget`
validator.** "Targeted" = the effect record overrides a `canTarget*()` to
return true (the target-carrying role per `model/EffectResolution.java`) or is
instanceof-checked in `TargetLegalityService`/`ValidTargetService`. On the
single-`targetId` path (`TargetLegalityService.checkSpellTargeting`) these get
NO type checking — the exact class of the July-2026 Fireball-burns-a-Plains
bug. The full list is in `EFFECT_COUPLING_MATRIX.md` § "Validator coverage
gap" and feeds step 3, which will verify each entry is really single-target
reachable and drop the ones that are not.

**Surprises / methodology caveats:**
- **`Fixed` false-positive avoided.** SpellEvaluator's raw `instanceof` list
  includes `instanceof Fixed` (the `DynamicAmount.Fixed` variant), which is NOT
  a file in the effect package, so it is correctly excluded — the script counts
  only tokens that are actual effect-package type names. Any similar nested
  variant name would be filtered the same way.
- **`SacrificeCreatureCost` / `SacrificeSelfCost` count as violations.** They
  are cost records that live in the effect package but are not on the
  structural-wrapper exemption list, so `instanceof` on them counts. Left as
  violations deliberately — they are concrete-type dispatch like any other.
- **`CostEffect` is both a wrapper AND an interface.** It is on the wrapper
  list and is declared `interface`; either rule exempts it. No double-count
  (each `instanceof` is classified once).
- **The audit's 686 is lower than the "~889 engine + ~230 ai raw instanceof"
  figure in the prompt** because that figure counts ALL `instanceof`
  (wrappers, non-effect types, exempt-zone hits) whereas 686 is the filtered
  violation count. Both are consistent (79 engine files contain some
  `instanceof`; 55 of the combined engine+ai files contain a *violating* one).
- **Counterspell / aura-attach heavy gap.** Many of the 162 gap entries are
  counterspells (`canTargetSpell`) and aura/equipment attachers. Whether each
  truly needs a `@ValidatesTarget` validator is a step-3 rules judgment; the
  audit only lists candidates.
- **Python interpreter note:** no `python`/`py` is on PATH in this environment;
  the script was validated with a locally vendored CPython 3.11. The script is
  pure stdlib (`re`, `collections`, `pathlib`) so any Python 3 will run it.

---

## Step 2 — Ratchet  (2026-07-14)

**Deliverable:** `magical-vibes-application/src/test/java/.../architecture/EffectDispatchRatchetTest.java`
— a pure test-code architecture guard. No production (`src/main`) file was
touched. It re-implements the step-1 audit's counting rules in Java, recomputes
the per-file effect-`instanceof` totals from source, and compares them to the
baseline. It fails when any file:
- **exceeds** its baseline (or is absent from the baseline with count > 0) —
  "New instanceof-on-CardEffect dispatch added in <file> (was N, now M)…";
- **drops below** its baseline — "Good news: <file> dropped from N to M.
  Regenerate the baseline…" (keeps the baseline honest as later steps shrink it);
- **is in the baseline but no longer exists** — same regenerate-the-baseline ask.

**Where the baseline lives:** unchanged — `refactor-docs/effect-dispatch-baseline.txt`.
The test discovers the repo root by walking up from the Gradle test working
directory until a `settings.gradle.kts`/`settings.gradle` is found, then reads
the baseline (and scans sources) relative to that root. The baseline was NOT
relocated and the audit script's output path was NOT changed, so there is a
single source of truth for both producer (Python) and consumer (Java).

**Regenerate the baseline** (only when a drop/rename is legitimately locked in,
or a sanctioned new dispatch is added): `python scripts/effect-coupling-audit.py`
from the repo root. It rewrites `effect-dispatch-baseline.txt` and
`EFFECT_COUPLING_MATRIX.md`.

**Run the ratchet test:**
`.\scripts\run-card-test.ps1 com.github.laxika.magicalvibes.architecture.EffectDispatchRatchetTest`
(use the FQCN — the wrapper treats bare names as `cards.{letter}.{Name}` card tests).

**Lockstep contract:** the counting rules are duplicated in
`scripts/effect-coupling-audit.py` and in `EffectDispatchRatchetTest`; a comment
block at the top of each names the other and requires changing both together.
Verified consistent: the test PASSES against the committed baseline (which the
Python audit generated), proving the two implementations agree token-for-token.

**Counting caveat (documented in both files):** the scan is line-agnostic
(whole-file regex), so an `instanceof <EffectType>` appearing inside a comment or
string literal is counted. This is acceptable for a ratchet — false positives
only tighten it, and since the audit script has the identical behavior the two
stay consistent. If a real dispatch is ever removed but a commented-out mention
lingers, regenerate the baseline (both tools will agree on the new count).

**Verification performed:** ran the test against a real injected violation
(`instanceof DrawCardEffect` added to `GameService`, baseline 1) — it failed
with the exact "was 1, now 2 …" message — then reverted; the tree is clean and
the test passes again. Python was unavailable this session (only Store-alias
stubs), so the baseline was not regenerated; none was needed since no production
source changed since step 1.
