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
Other `*And*Effect` composite records still exist (e.g.
`DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect` (Corrupt),
`DealXDamageToAnyTargetAndGainXLifeEffect`). These couple damage to a *dynamic* life amount ("gain
life equal to the damage dealt"), which is a different decomposition (`GainLifeEffect(new XValue())`
or an `EventValue`-style amount) and needs its own rules check per card before folding.
