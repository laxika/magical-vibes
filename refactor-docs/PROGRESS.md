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

---

## Step 3 — Validator coverage  (2026-07-14)

Closed the `@ValidatesTarget` gap on the single-`targetId` path
(`TargetLegalityService.checkSpellTargeting` → `TargetValidationService.checkEffectTargets`
on the SPELL slot, and `validateActivatedAbilityTargeting` for abilities). Started from the
162-entry "Validator coverage gap" list in `EFFECT_COUPLING_MATRIX.md`.

**Method used to build the verified worklist:**
1. Bucketed all 162 by which `canTarget*()` each overrides (grep of `model/effect/`).
2. For the permanent-targeting subset, grepped every constructing card and the `EffectSlot` /
   `ActivatedAbility` each usage sits in — an effect is in scope only if it is the target-carrying
   effect of a **spell (SPELL slot)** or **activated ability**, the only two paths that route a
   single `targetId` through `checkEffectTargets`. Trigger/ETB/combat slots choose their targets in
   the trigger pipelines (`TriggerTargetCollector`, honouring the card's `TargetFilter`), never
   through `@ValidatesTarget`, so they are a different mechanism.
3. Determined each kept effect's legal target set from the oracle text of its cards (the
   `"Target must be a …"` filter strings and, where absent/ambiguous, the card names on the web).
   Where cards using one effect disagree (e.g. Erode "creature or planeswalker" vs Ghost Quarter
   "land" both use `DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect`), the
   validator uses the **broadest** legal type (any permanent) so it never rejects a legal target —
   over-restriction is a rules bug too.

**Key architectural facts relied on** (so a later step can re-check):
- `checkSpellTargeting` already splits player-vs-permanent (`computeAllowedTargets`) and applies the
  card's `getTargetFilter()` **before** `checkEffectTargets`, and runs its own `checkSpellProtection`.
  So the effect validator's *unique* contribution on the spell path is narrowing the **permanent
  type** when no card filter does (the Fireball-burns-a-Plains class). Two representative cards carry
  **no** card filter — Wrack with Madness and Dark Nourishment — so their new validators are the only
  gate and were live latent bugs (a land was a legal target before this step).
- The activated-ability path (`validateActivatedAbilityTargeting`) applies the ability's own
  `getTargetFilter()` but does **not** run the general protection or player-vs-permanent split, so the
  effect validator adds real coverage there (e.g. catches a player passed to a "target permanent"
  ability).
- Registration is automatic: every `@Service` under `service/validate/` is scanned for
  `@ValidatesTarget` by `GameEngineConfig.afterSingletonsInstantiated`; the test harness boots the
  same Spring context, so the new `PreventionTargetValidators` bean needs no manual wiring.

**Protection-check policy** (matching existing validators' scope, no more): included
`tvs.checkProtection` only on clearly *harmful* effects (damage, fight, exile, destroy, sacrifice) —
mirroring the `DealDamageToAnyTarget` / `DestroyTargetPermanent` exemplars; omitted it on benign /
neutral effects (boost, grant, counters, combat-shaping, prevention, redirect-protective, bounce,
land-grant, equip) — mirroring `BoostTargetCreature` / `AnimatePermanents`. `checkProtection` never
rejects a *legal* target (protection = can't be targeted), so this is safe either way; the split just
keeps scope consistent with the existing files.

**Validators added — 55 effects (effect → file, legal target):**

- `DamageTargetValidators.java` (13): any-target burn `DealDamageToAnyTargetAndGainLifeEffect`,
  `DealDamageToAnyTargetEqualToChosenTypeCountEffect`, `DealXDamageToAnyTargetAndGainXLifeEffect`,
  `MillControllerAndDealDamageByHighestManaValueEffect`; creature damage/fight
  `DealDamageToTargetControllerIfTargetHasKeywordEffect`,
  `DealDamageToTargetCreatureEqualToChosenTypeCountEffect`,
  `PlaneswalkerDealDamageAndReceivePowerDamageEffect`, `TargetCreatureDealsPowerDamageToSelfEffect`,
  `TargetCreatureDealsPowerDamageToControllerEffect`, `SourceFightsTargetCreatureEffect`; creature
  damage-redirection (no protection) `RedirectNextDamageToTargetCreatureEffect`,
  `RedirectTargetCreatureDamageFromChosenSourceToSelfEffect`,
  `RedirectTargetCreatureNextDamageFromChosenSourceToControllerEffect`.
- `PreventionTargetValidators.java` (NEW, 6): any-target `PreventDamageToTargetEffect`,
  `PreventDamageToTargetFromChosenSourceEffect`,
  `PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect` (harmful redirect → protection);
  creature `PreventAllDamageToTargetCreatureEffect`, `PreventAllDamageByTargetCreatureEffect`;
  divided any-target (tolerates null `targetId` like `DealDividedDamage`) `PreventDividedDamageEffect`.
- `ExileTargetValidators.java` (6): any-permanent `ExileTargetPermanentEffect` (19 cards),
  `ExileTargetPermanentAndTrackWithSourceEffect`, `ExileTargetPermanentMayPlayUntilNextTurnEffect`;
  creature `ExileTargetCreatureAndAllWithSameNameEffect`,
  `ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect`,
  `MarkTargetCreatureExileInsteadOfDieThisTurnEffect`.
- `DestructionTargetValidators.java` (6): land `DestroyTargetLandAndDamageControllerEffect`,
  `DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect`; any-permanent
  `DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect`,
  `DestroyTargetPermanentAtEndStepEffect`, `SacrificeTargetPermanentAtEndStepEffect`; creature
  `SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect`.
- `BounceTargetValidators.java` (2): any-permanent `ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect`,
  `ReturnTargetPermanentToHandAtEndStepEffect`.
- `CreatureModTargetValidators.java` (22): creature `BoostTargetCreaturePerChosenTypeCountEffect`,
  `EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffect`,
  `GrantEffectToTargetUntilEndOfTurnEffect`, `GrantChosenKeywordToTargetEffect`,
  `TargetCreatureBecomesSubtypeUntilEndOfTurnEffect`, `MustAttackThisTurnEffect`,
  `MustBeBlockedByAllCreaturesThisTurnEffect`, `MustBeBlockedIfAbleThisTurnEffect`,
  `CantBlockSourceEffect`, `RemoveTargetFromCombatEffect`, `MakeTargetCreaturePreparedEffect`,
  `RemoveCounterFromTargetAndGainLifeEffect`, `EquipEffect`; creature fights (protection)
  `MassFightTargetCreatureEffect`, `PackHuntEffect`; any-permanent
  `GrantProtectionChoiceUntilEndOfTurnEffect`, `CreateTokenCopyOfTargetPermanentEffect`,
  `SetChosenColorUntilEndOfTurnEffect`, `DoubleCountersOnTargetPermanentEffect`,
  `RemoveChargeCountersFromTargetPermanentEffect`, `RemoveCountersFromTargetAndBoostSelfEffect`;
  land `GrantBasicLandTypeToTargetEffect`.

**Representative tests** (imitating `FireballTest`'s illegal-target section — "coherent group sharing
one code path" → one test each for the type-narrowing paths):
- `WrackWithMadnessTest.cannotTargetLand` — creature path (`requireCreature`), on a **filterless**
  card, so the validator is the sole gate (real fix).
- `DarkNourishmentTest.cannotTargetLand` — any-target path, also **filterless** (real fix).
- The land path is already covered by the pre-existing `MeltTerrainTest.cannotTargetCreature` (its
  card filter and the new validator agree on "Target must be a land"), so no new land test was added.

**Regression / safety runs (all green):** the two edited tests; `FireballTest`, `FightWithFireTest`
(original Fireball fix); `EffectDispatchRatchetTest` (validators live in the exempt `service/validate/`
zone, so the baseline was untouched and did not need regenerating); `:magical-vibes-ai *GameSimulatorTest`;
and 49 tests for cards whose effects gained a validator (BlackbladeReforged, GolemArtisan,
DistortingLens, HexParasite, GilderBairn, Woeleecher, Chainbreaker, AlluringSiren, ConsignToDream,
Mirrorweave, HarmsWay, HealingGrace, Bandage, InquisitorsSnare, StoneGiant, GhostQuarter, FieldOfRuin,
Erode, GarrukRelentless, ApostlesBlessing, TowerAbove, RabidAttack, MercyKilling, HeatedArgument,
Unmake, Dispatch, CoordinatedBarrage, BurnTheImpure, RoarOfTheCrowd, EssenceDrain, ConsumeSpirit,
HereticsPunishment, TraitorsRoar, WiltInTheHeat, DeadlyAllure, TauntingChallenge, AlluringScent,
Incite, MasterOfTheWildHunt, DragonMask, LowlandOaf, GremlinMine, AppliedGeometry, CacklingCounterpart,
SuspendAggression, Helvault, KarnLiberated, OblivionRing, FiendHunter) — these all cast at *legal*
targets, so they would fail if any validator over-restricted; none did.

**Dropped worklist entries (107) and why:**
- **Spell-on-stack targeting (16)** — `canTargetSpell` effects are validated by
  `TargetLegalityService.checkSpellTargetOnStack` via the card's `TargetFilter`, a different path that
  `checkEffectTargets` never reaches (a stack target makes `findPermanentById` null →
  `checkSpellTargeting` returns "Invalid target" first): every `CounterSpell*`, `CounterUnless*`,
  `Counterlash`, `CopySpellEffect`, `ChangeTargetOfTargetSpell*`, `ChooseNewTargetsForTargetSpellEffect`,
  `MakeTargetSpellUncounterableEffect`.
- **Player-only targeting (~60)** — `canTargetPlayer`-only effects: `checkSpellTargeting`'s
  `computeAllowedTargets` pre-check already rejects a permanent target ("This spell can only target
  players"), and opponent/relation restrictions ride on the card's `PlayerPredicateTargetFilter`. All
  the `TargetPlayer*`, `Reveal*Hand*`, `Search*Library*`, `Discard*`, `Mill*Player*`, `LookAtHand*`,
  `SetTargetPlayerLife*`, `ControlTargetPlayer*`, `ExtraTurnEffect`, `ManaClashEffect`, `HeadGamesEffect`,
  `JuxtaposeEffect`, `PsychicTransferEffect`, etc.
- **Trigger / ETB / combat-slot only (~18)** — targets are chosen by the trigger pipelines, honouring
  the card's `TargetFilter`, never through `@ValidatesTarget`: `AttachAllAurasToAnotherPermanentEffect`,
  `AttachSourceEquipmentToTargetCreatureEffect`, `BecomeCopyOfTargetCreature*Effect`,
  `DestroyCombatOpponentAtEndOfCombatEffect`, `DestroySubtypeCombatOpponentEffect`,
  `DestroyTargetPermanentAndDamageControllerIfDestroyedEffect`, `ExileTargetPermanentAndImprintEffect`,
  `ExileTargetPermanentUntilSourceLeavesEffect` (Fiend Hunter / Oblivion Ring — ETB),
  `GainControlUntapAndHasteTargetEffect`, `MakeTargetCreatureUnpreparedEffect`,
  `MoveDyingSourceCountersToTargetCreatureEffect`, `PutCounterOnTargetForEachDyingSourceCounterEffect`,
  `RemoveCounterFromTargetPermanentEffect`, `DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect`,
  etc. (Effects with a mixed spell/ability usage — e.g. `RemoveTargetFromCombatEffect`,
  `MakeTargetCreaturePreparedEffect`, `MustAttackThisTurnEffect`,
  `TargetCreatureDealsPowerDamageToControllerEffect` — were **kept** for the spell/ability usage; the
  validator simply isn't invoked on their ETB/trigger usage.)
- **Multi-target (9)** — two target groups / X targets / "up to N", validated by
  `validateMultiSpellTargets` / `validateMultiTargetAbility` per-position `TargetFilter`s, not the
  single-`targetId` path: `FightTargetsEffect`, `TargetDealsPowerDamageToTargetEffect`,
  `MoveCounterFromTargetCreatureToTargetCreatureEffect`, `MustBlockTargetCreatureEffect`,
  `MakeTargetCopyOfTargetCreatureUntilNextTurnEffect`, `CreateTokenCopyOfTargetCreatureForTargetPlayerEffect`,
  `AttachTargetEquipmentToTargetCreatureEffect`, `DealDamageToEachTargetEffect`,
  `DestroyEachTargetPermanentEffect`, `DestroyUpToTargetsThenReturnFromGraveyardEffect`.
- **Graveyard/exile-zone multi-target (2)** — `ExileCardsFromGraveyardEffect`,
  `ExileGraveyardCardWithConditionalBonusEffect` — graveyard-card targets validated by
  `validateMultiTargetGraveyardAbility` / the graveyard-retarget path, not the single permanent path.
- **Not a targeted effect (1)** — `TargetingRestrictionEffect` is a STATIC continuous effect
  (overrides no `canTarget*()`); it appears in the gap list only because it is `instanceof`-checked in
  the targeting services, not because it carries a target.
- **Dual spell/permanent, no narrower type (1)** — `ChangeColorTextEffect` (Glamerdye, Mind Bend):
  targets a spell **or** any permanent; the permanent branch is "any permanent" so there is no type to
  narrow, and the spell branch is routed to `checkSpellTargetOnStack` (see the `isSpellOnStack`
  comment in `TargetLegalityService`).
- **No card constructs it (3)** — `AttachSourceAuraToTargetCreatureEffect`,
  `ExileTargetOnControllerSpellCastEffect`, `ExileTargetOpponentPermanentOnDrawEffect` (only referenced
  reflectively/never `new`-ed by any card).

**Resolution-side guards TODO (not fixed this step, none required by a failing test):**
No normalfx handler was found to *require* a guard: the two filterless bug cards' effects already
no-op safely on a non-creature (a fight/`*DealsPowerDamage*` reads `0` power off a land and deals
nothing; damage/exile/destroy would still act, but the new targeting validators now block the illegal
target before resolution). A future belt-and-suspenders pass could add resolution-side type re-checks
to the `normalfx` handlers for the creature-only effects listed above, but it is out of scope here and
no test needed it.

**Oracle-text judgment calls to double-check:**
- `DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect` → **any permanent** (not
  land): Erode targets "creature or planeswalker", Ghost Quarter targets "land" — the two disagree, so
  the validator only requires a battlefield permanent.
- `GrantChosenKeywordToTargetEffect` → **creature**: Golem Artisan ("artifact creature") and Practiced
  Offense both target creatures; no card grants a keyword to a non-creature permanent.
- `ExileTargetPermanentMayPlayUntilNextTurnEffect` / `SacrificeTargetPermanentAtEndStepEffect` /
  `DestroyTargetPermanentAtEndStepEffect` → **any permanent**: named `…Permanent…` and the sole users
  (Suspend Aggression "nonland permanent", Lowland Oaf, Stone Giant) narrow further on the card, so the
  validator stays at "any permanent" to avoid over-restriction.
- Prevention/"any target" effects (Bandage, Healing Salve line, Harm's Way, Remedy) treated as
  creature/planeswalker/player per the modern "any target" erratum.
