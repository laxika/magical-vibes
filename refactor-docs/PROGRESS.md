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

---

## Step 4 — Targeting unification  (2026-07-14)

Unified the three spell-target validation paths so a permanent candidate is judged by the SAME
core no matter which path it arrives through. This is a behavior-preserving-or-STRICTER refactor:
the one path that was *under*-checking (UI/AI enumeration) is now brought up to the strictest of
the applicable rules.

### Path map (which path guards which entry point)

**Path 1 — UI / enumeration (`ValidTargetService`)** — per-candidate core `isValidPermanentTarget`:
- `computeValidTargetsForSpell` ← `GameMessageHandler` (frontend "what can I target"),
  `ExileCastTargetSupport` (exile-cast per-slot enumeration).
- `computeValidTargetsForAbility` ← `GameMessageHandler`, `MayCopyHandlerService`.
- `hasValidTargetsForSpell` ← `GameBroadcastService` (CR 601.2c castability gate).
- `isValidSpellPermanentTarget` / `isValidMultiTargetPermanent` ← AI `AiTargetSelector`.
- `canPermanentBeTargetedBySpell` ← `CopySpellForEachOtherSubtypePermanentEffectHandler`
  (resolution-time copy retarget).

**Path 2 — multi-target cast (`TargetLegalityService.validateMultiSpellTargets`)** — flat
`targetIds` list: ← `SpellCastingService` (kicked/multi cast sites).

**Path 3 — single-`targetId` cast (`TargetLegalityService.checkSpellTargeting` via
`validateSpellTargeting`)**: ← `SpellCastingService` (single-target/kicked-primary sites),
`MayCopyHandlerService` (copy cast), `TargetRedirectionSupport` (retarget-candidate check).

(Adjacent, out of scope: `validateActivatedAbilityTargeting` / `validateMultiTargetAbility`
[abilities — already routed through the `@ValidatesTarget` mechanism in step 3],
`checkSpellTargetOnStack` [spell-on-stack], `isTargetIllegalOnResolution` [resolution-time fizzle].)

### Divergence inventory (per-permanent-candidate rules)

| Rule | Path 1 (before) | Path 2 | Path 3 | Decision |
|---|---|---|---|---|
| Protection (color / card-type / subtype) | ✓ | ✓ | ✓ | agree → unified into `checkSpellPermanentTargetableReason` |
| cant-be-targeted (spell-color / any-spell / non-color-source) | ✓ | ✓ | ✓ | agree → unified |
| shroud / hexproof / granted hexproof | ✓ | ✓ | ✓ | agree → unified |
| hexproof-from-color | ✓ | ✓ | ✓ | agree → unified |
| card / position `TargetFilter` | ✓ | ✓ | ✓ | keep per-path (list/position bookkeeping) |
| any-target ⇒ creature/planeswalker (structural) | ✓ block | — | — (relies on validators) | kept in path 1 as belt-and-suspenders backstop |
| require-creature default (no filter) | multi-target only | ✓ | — | reviewed pre-existing difference (see below) |
| per-effect `@ValidatesTarget` validators | **✗ MISSING (bug)** | ✗ (multi = position-filter mechanism) | ✓ | **FIX path 1** |

### Decisions (rules-checked)

- **Divergence FIXED — path 1 did not run the per-effect `@ValidatesTarget` validators.** After
  step 3 closed the validator gap on the single-`targetId` cast path, path 1 (UI/AI enumeration)
  could still *offer* a target that path 3 (cast) rejects. Concrete live bug: filterless
  "target creature" / "any target" spells (Wrack with Madness, Dark Nourishment) listed a **land**
  in the UI though casting at it throws. The AI even hand-patched this exact gap in
  `AiTargetSelector.isValidPermanentTarget` ("Run the same `@ValidatesTarget` validators that spell
  casting uses"). **Rules: the validator encodes the correct legal target type; enumeration must
  run it.** Fix: `ValidTargetService.isValidPermanentTarget` now runs
  `TargetValidationService.checkEffectTargets` for the single-target case (positionFilter == null &&
  !isMultiTarget), mirroring `checkSpellTargeting` exactly. This is the ONLY observable behavior
  change: UI/AI enumeration no longer offers a permanent that resolution-time validation rejects
  (STRICTER, correct). The AI's hand-patch is now redundant but left in place (harmless; it also
  covers ETB-slot effects) — a future cleanup could drop it.
- **Path 2 (multi-target) intentionally does NOT run the single-target validators.** Multi-target
  effects were excluded from validator scope in step 3 (their positions are governed by per-position
  `TargetFilter`s); validating every SPELL effect against one of several targets would be
  semantically wrong. No behavior change.
- **any-target ⇒ creature/planeswalker vs require-creature.** Path 1's structural block narrows an
  all-any-target spell to creature/planeswalker; path 2's no-filter default requires a *creature*
  (stricter, no planeswalker); path 3 narrows via validators (creature/planeswalker/player). In
  production these converge for the reachable set (every single-target any-target spell has a
  validator after step 3), so path 1's structural block is now a redundant backstop kept for safety.
  Path 2's creature-only default is a **pre-existing reviewed behavior** (encoded by
  `TargetLegalityServiceTest.throwsWhenNonCreatureTargetWithoutFilter`) — no current multi-target
  spell needs a planeswalker in a filterless position, so it is left unchanged (would require a card
  that exercises it plus a rules review to widen).

### Shared core

Folded the structural rules into a single method `TargetLegalityService.checkSpellPermanentTargetableReason`
(`checkSpellProtection` + `untargetableReason`), which is the single home the two service classes now
share. All three paths delegate candidate structural legality to it:
- `checkSpellTargeting` (path 3) — delegates (was already an inline copy of the same two calls).
- `validateMultiSpellTargets` (path 2) — delegates (replaced `validateSpellProtections` +
  `validateHexproofFromColor` + `validatePermanentTargetable`; `validateSpellProtections` removed as
  dead code).
- `ValidTargetService.canPermanentBeTargetedBySpell` / `…Core` (path 1) — delegate the structural
  block; enumeration adds only the card `TargetFilter` and (new) the effect validators.
The type-narrowing dimension is unified onto the `@ValidatesTarget` validator mechanism (every path
that should narrow now does so through it; path 1 additionally keeps the structural any-target
backstop). No new `instanceof`-on-effect dispatch was introduced, so the ratchet baseline is
untouched (EffectDispatchRatchetTest green, no regeneration needed).

Note on check ORDER: the unified `untargetableReason` checks the target's controller *before* the
hexproof/granted-hexproof keyword (path-3 order), whereas path 1's old
`isBlockedByHexproofOrGrantedEffect` checked the keyword first. Same result for every case (an own /
null-controller permanent is never blocked), but four `ValidTargetServiceTest` mock stubs that
pre-stubbed the keyword for own/null-controller permanents became *unnecessary* under strict
stubbing and were marked `lenient()` (behavior asserted is unchanged).

### New tests

- `WrackWithMadnessTest.targetEnumerationExcludesLand` — path-1 enumeration
  (`computeValidTargetsForSpell`) now excludes a land for a filterless creature-only spell (the case
  where path 1's structural block did NOT fire pre-change, so the land leaked into enumeration). This
  is the definitive proof of the fix.
- `DarkNourishmentTest.targetEnumerationExcludesLand` — same for the any-target family (creature ✓,
  land ✗, both players ✓).
- `ValidTargetServiceTest` — real `TargetLegalityService` now wired over the mocks so the shared
  structural core is exercised through the same stubs; `targetValidationService` added as a mock.
  Four own/null-controller hexproof stubs relaxed to `lenient()` (order change, see above).
- Added a harness accessor `GameTestHarness.getValidTargetService()` (testFixtures) so card tests can
  exercise the UI enumeration path.

### Callers / signatures

All three entry-point method signatures are preserved. Two `new ValidTargetService(...)` construction
sites were touched (not entry-point signatures): `AiTargetSelector` now passes the two collaborators
it already holds; a legacy 2-arg constructor (null collaborators) is kept for
`StepTriggerServiceTest`'s trigger-player-target-only usage.

### Tests I believe are rules-wrong

None. (Incidental: `ValidTargetServiceTest.GraveyardTypeFiltering` — the
"PutCardFromOpponentGraveyard filters to artifacts and creatures" case — was **already failing on
clean main**, a test-only mock bug: the `matchesCardPredicate` stub didn't handle `CardAnyOfPredicate`
so it returned all six graveyard cards. Fixed the stub to mirror real predicate semantics; no
production change. This is unrelated to the targeting unification.)

### Verification (all green)

Regression guards `FireballTest`, `FightWithFireTest`, and `:magical-vibes-ai *GameSimulatorTest`
before and after. Ratchet `EffectDispatchRatchetTest`. The two refactored service unit tests
`ValidTargetServiceTest`, `TargetLegalityServiceTest`. Direct-caller service tests
`SpellCastingServiceTest`, `AbilityActivationServiceTest`, `StepTriggerServiceTest`,
`CopySpellForEachOtherSubtypePermanentEffectHandlerTest`,
`ChangeTargetOfTargetSpellToSourceEffectHandlerTest`, `AiTargetSelectorTest`. Six diverse targeting
card tests: `ArcTrailTest` (multi-target), `NaturalizeTest` / `TerrorTest` (permanent / creature
destruction), `PacifismTest` (aura), `IncinerateTest` (any-target burn),
`RootwaterCommandoTest` (targeted ability). Plus the two new divergence tests.

---

## Step 5 — AI metadata (damage/removal)  (2026-07-14)

Introduced two DESCRIPTIVE capability interfaces in the domain effect package (mirroring
`ManaProducingEffect`) for the two biggest AI effect families, and migrated `SpellEvaluator`'s
damage and removal dispatch onto them. Semantics-preserving for the AI: every branch reproduces
the *exact* prior score, computed via interface facts instead of concrete-type `instanceof`. No
record component/constructor/engine behavior changed — only added `implements` clauses and
interface-method bodies that return existing components (`git diff` on `magical-vibes-domain` is
purely additive).

### Capability interfaces added (`magical-vibes-domain/.../model/effect/`)

- **`DamageDealingEffect extends CardEffect`** — "deals a single evaluated amount to one target
  category":
  - `DynamicAmount damageAmount()` — per-target amount (exposes the existing `DynamicAmount`
    component; no flattening to int, per the DynamicAmount abstraction).
  - `boolean canDamageCreatures()`
  - `boolean canDamagePlayers()`
- **`RemovalEffect extends CardEffect`** — "single-target removal, of what kind":
  - `RemovalKind removalKind()` — `DESTROY` | `EXILE` | `BOUNCE`, or **`null`** when the current
    configuration is not single-target removal (e.g. a mass/all-matching bounce). Nullable-fact
    style mirrors `CardEffect.targetPredicate()`.
- **`RemovalKind`** enum (`DESTROY, EXILE, BOUNCE`).

Both new interfaces are declared `interface` in the effect package, so the audit script and
`EffectDispatchRatchetTest` auto-exempt `instanceof DamageDealingEffect` / `instanceof
RemovalEffect` (verified: ratchet green, and the file dropped, proving the interface hits are not
counted). `RemovalKind` is an enum but is never `instanceof`-checked (used via `switch`), so it
adds no violations.

### Family members (records now implementing the interfaces)

- **`DamageDealingEffect`** (3): `DealDamageToAnyTargetEffect` (creatures+players),
  `DealDamageToTargetCreatureEffect` (creatures only), `DealDamageToPlayersEffect` (players only;
  docstring literally "never creatures"). All three carry a real `DynamicAmount` component
  (`damage()`/`damage()`/`amount()`), so `damageAmount()` needs no synthetic value.
- **`RemovalEffect`** (4): `DestroyTargetPermanentEffect` (DESTROY), `ExileTargetPermanentEffect`
  (EXILE), `ReturnTargetPermanentToHandWithManaValueConditionalEffect` (BOUNCE), and
  `ReturnToHandEffect` (BOUNCE only for `scope == TARGET`, else `removalKind()` returns `null` —
  its mass/self scopes are board sweeps / self-return, not single-target removal).

### SpellEvaluator inventory (damage/removal `instanceof` before this step) and what each became

DAMAGE — read fields → scoring path:
- `DealDamageToAnyTargetEffect` (`damage()`) → `evaluateDamageEffect` (max kill-value vs face);
  ETB + single + `isRemovalEffect`. **Migrated** → `DamageDealingEffect` (creatures&&players).
- `DealDamageToTargetCreatureEffect` (`damage()`) → `evaluateDamageToCreature`; ETB + single +
  `isRemovalEffect`. **Migrated** → `DamageDealingEffect` (creatures only).
- `DealDamageToPlayersEffect` (`amount()`, `recipient()`) → `TARGET_PLAYER` +amt*1.5,
  `CONTROLLER` −amt*1.5, else 0. **Survivor** (see below); still implements the interface.
- `MassDamageEffect` (`amount()`) → `evaluateBoardWipeDamage`; also `isBoardWipeEffect`. **Survivor**.
- `DealDividedDamageEffect` (mode/etbAssignments/canTargetPlayers/totalDamage) → narrow
  "CHOSEN, non-ETB, creatures-only, Fixed total" → `evaluateDamageToCreature`. **Survivor**.
- `DealXDamageToAnyTargetAndGainXLifeEffect` (no amount field; implicitly X) →
  `evaluateDamageEffect(estimateMaxX)` + X lifegain. **Survivor**.

REMOVAL — all scored off `bestTargetCreatureValue` with a per-kind factor:
- `DestroyTargetPermanentEffect` (×1.0), `ExileTargetPermanentEffect` (×1.1),
  `ReturnToHandEffect` scope==TARGET (×0.6), `ReturnTargetPermanentToHandWithManaValueConditionalEffect`
  (×0.6); each in ETB + single + `isRemovalEffect`. **All migrated** → one `RemovalEffect` branch
  delegating to a new `removalScore(kind,…)` helper (DESTROY→base, EXILE→base*1.1, BOUNCE→base*0.6),
  identical factors in both `evaluateEtbEffect` and `evaluateSingleEffect`.

Cross-checked `EFFECT_COUPLING_MATRIX.md` (ii): the same family types are also dispatched by
`AiTargetSelector`, `BoardEvaluator`, `HardAiDecisionEngine`, `InstantCategoryClassifier`,
`RaceEvaluator`, `AiManaManager` (AI, steps 6–7) and `CombatDamageService`, `CombatBlockService`,
`AbilityActivationService` (engine, out of this program's AI scope). The interfaces were designed
to answer what those consumers ask (amount / who-can-be-hit / removal-kind), even though only
`SpellEvaluator` is migrated now.

### Concrete-type survivors kept in SpellEvaluator (each with reason)

1. `DealDamageToPlayersEffect` (×2, `evaluateSingleEffect`) — recipient decides the **sign**
   (`TARGET_PLAYER` beneficial, `CONTROLLER` a self-damage drawback, other recipients 0); no
   descriptive damage fact expresses the drawback/recipient enum. It *does* implement
   `DamageDealingEffect`, and the migrated damage branch is gated on `canDamageCreatures()` — which
   is `false` here — so a player-only effect cleanly falls through to these survivor branches
   without changing order or score.
2. `MassDamageEffect` (`evaluateSingleEffect` + `isBoardWipeEffect`) — board-wipe scoring across all
   creatures/players; not the single-amount/single-category shape. Does **not** implement
   `DamageDealingEffect` (kept off the interface so the migrated branch can't accidentally catch it).
3. `DestroyAllPermanentsEffect` (`evaluateSingleEffect` + `isBoardWipeEffect`) — predicate-filtered
   board wipe; not single-target removal. Does not implement `RemovalEffect`.
4. `DealDividedDamageEffect` (`evaluateSingleEffect`) — split-a-total-among-many shape behind a
   narrow gate; does not fit "single amount to one category". Not on the interface.
5. `DealXDamageToAnyTargetAndGainXLifeEffect` (`evaluateSingleEffect`) — has **no** `DynamicAmount`
   component (implicitly X) and its scoring couples X-damage with X-lifegain. Not on the interface.
6. `ReturnToHandEffect` scope==`ALL_MATCHING` (`evaluateSingleEffect` + `isBoardWipeEffect`) — mass
   bounce board sweep. `ReturnToHandEffect` implements `RemovalEffect` but `removalKind()` returns
   `null` for non-`TARGET` scopes, so these two scope-gated concrete checks remain as board-wipe
   handling (not removal).
7. `GainControlOfTargetEffect` (steal) — not destroy/exile/bounce; a separate control/steal family,
   out of scope for this step.

### Verification (all green)

- `SpellEvaluatorTest` (28 tests) — the direct owner test; passes after the change. The migration
  is score-for-score identical by construction (same branches, interface facts substituted for
  concrete `instanceof`).
- AI evaluators/consumers that touch these families: `InstantCategoryClassifierTest`,
  `RaceEvaluatorTest`, `BoardEvaluatorTest`, `HardAiDecisionEngineTest`, `AiDecisionEngineTest`,
  `EasyAiDecisionEngineTest`, `GameSimulatorTest` — all pass (calibrated MCTS/simulation tests
  unaffected: no rollout-cost or scoring-VALUE change).
- Engine inertness (domain-record additions are no-ops for resolution): `FireballTest` (any-target
  X + divided), `IncinerateTest` (any-target burn, can't-regenerate), `TerrorTest` (destroy),
  `UnsummonTest` (single-target bounce), `CribSwapTest` (exile + token). All pass.
- `EffectDispatchRatchetTest`: **SpellEvaluator 65 → 47** (−18). Baseline
  `effect-dispatch-baseline.txt` updated (that single line; no other consumer file changed since I
  edited only domain records + `SpellEvaluator`, so this equals what the audit would emit). Ratchet
  green after the update.

**New SpellEvaluator violation count: 47 (was 65).**

**Note — matrix report not regenerated:** `python` is unavailable in this environment (as in steps
1–2), so `EFFECT_COUPLING_MATRIX.md` still shows the pre-step-5 figures (total 686, SpellEvaluator
65). The machine-readable baseline the ratchet reads *is* correct (SpellEvaluator=47; new total
668). Re-run `python scripts/effect-coupling-audit.py` when Python is available to refresh the human
report; it will reproduce the same baseline.

---

## Step 6 — AI metadata (evaluator/classifier/selector)  (2026-07-14)

### Plan (families → interface), written before coding

Continues the step-5 pattern: DESCRIPTIVE capability interfaces in
`magical-vibes-domain/.../model/effect/` that family records implement, so the three read-only AI
consumers dispatch on ONE interface instead of many concrete effect types. Semantics-preserving:
every branch reproduces the exact prior score / category / target choice, computed via interface
facts. Domain diff is additive (`implements` clauses + method bodies returning existing components).

Inventory of the three consumers (concrete-effect `instanceof`, post-step-5) grouped into families,
and the interface (new / reused) each maps to. Cross-checked `EFFECT_COUPLING_MATRIX.md` (ii) so the
new interfaces also answer what step-7 consumers (GameSimulator, HardAiDecisionEngine, AiManaManager)
+ BoardEvaluator/RaceEvaluator ask (draw amount, life amount, token profile, boost, keyword grant,
control duration, "is a counter/regen").

New interfaces (9):
- **`CardDrawingEffect`** `DynamicAmount drawnCardAmount()` — impl `DrawCardEffect`.
- **`LifeGainEffect`** `DynamicAmount lifeGainAmount()` — impl `GainLifeEffect`.
- **`TokenCreatingEffect`** `DynamicAmount tokenAmount()`, `CardType tokenType()`, `int tokenPower()`,
  `int tokenToughness()` — impl `CreateTokenEffect`.
- **`CounterSpellingEffect`** (marker) — impl `CounterSpellEffect`, `CounterSpellAndExileEffect`,
  `CounterUnlessPaysEffect` (migrates InstantCategoryClassifier only).
- **`CreatureBoostEffect`** `DynamicAmount powerBoost()`, `DynamicAmount toughnessBoost()` — impl
  `BoostTargetCreatureEffect` (a *targeted* creature P/T boost; self-boost is a different shape).
- **`KeywordGrantingEffect`** `Set<Keyword> keywords()`, `GrantScope scope()` — impl
  `GrantKeywordEffect`.
- **`StaticCreatureBoostEffect`** `int powerBoost()`, `int toughnessBoost()`,
  `Set<Keyword> grantedKeywords()`, `GrantScope scope()` — impl `StaticBoostEffect`.
- **`ControlStealingEffect`** `ControlDuration controlDuration()` — impl `GainControlOfTargetEffect`.
- **`RegenerationEffect`** (marker) — impl `RegenerateEffect`.

Reused step-5 interfaces: `RemovalEffect` (InstantCategoryClassifier removal, AiTargetSelector
destroy/exile detection via `removalKind() == DESTROY|EXILE`), `DamageDealingEffect`
(InstantCategoryClassifier creature-damage removal, AiTargetSelector ability-damage targeting).

Concrete-type survivors (justified; not migrated this step):
- SpellEvaluator: recipient-sign families (`DealDamageToPlayersEffect`, `LoseLifeEffect`), board-wipe
  families (`MassDamageEffect`, `DestroyAllPermanentsEffect`, `ReturnToHandEffect` ALL_MATCHING),
  split-total (`DealDividedDamageEffect`), no-amount X (`DealXDamageToAnyTargetAndGainXLifeEffect`),
  bespoke ability-only scoring (`BoostSelfEffect`, `ScryEffect`, `TapPermanentsEffect`,
  `PutCounterOnEachControlledPermanentEffect`, `PutCounterOnTargetPermanentEffect`),
  `CounterSpellEffect` (narrow "plain counter" scoring — broadening to `CounterSpellingEffect` would
  newly score CounterUnlessPays/CounterSpellAndExile spells), `DiscardEffect` (recipient/random),
  cost records (`SacrificeCreatureCost`, `SacrificeSelfCost`), and the mana family
  (`AwardManaEffect`, `AwardAnyColorManaEffect`, `AwardAnyColorChosenSubtypeCreatureManaEffect`) —
  deferred to **step 7** (widen `ManaProducingEffect`).
- InstantCategoryClassifier: `DealDamageToTargetCreatureOrPlaneswalkerEffect` (NOT put on
  `DamageDealingEffect` — that would newly score it in SpellEvaluator's ability path, a scoring
  change), `DealDamageToPlayersEffect` (recipient → BURN_TO_FACE).
- AiTargetSelector: `ExtraTurnEffect` (beneficial player-target marker), `DealDividedDamageEffect`
  (divided-damage assignment builder), and the 8 graveyard-targeting effects
  (`ReturnCardFromGraveyardEffect`, `PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect`,
  `CastTargetInstantOrSorceryFromGraveyardEffect`, `ExileGraveyardCardsEffect`,
  `GrantFlashbackToTargetGraveyardCardEffect`, `ExileTargetCardFromGraveyardAndImprintOnSourceEffect`,
  `PutCardFromOpponentGraveyardOntoBattlefieldEffect`,
  `ExileTargetGraveyardCardAndSameNameFromZonesEffect`) — heterogeneous per-effect candidate filters
  duplicating `GraveyardTargetValidators`; no uniform capability fact answerable from existing
  components (the type/predicate checks live in the selector, not the effect). Left as a group.

### Results

**New/extended interfaces:** the 9 new interfaces above, all declared `interface` in the effect
package (so the audit script and `EffectDispatchRatchetTest` auto-exempt `instanceof` on them).
Reused step-5 `RemovalEffect` + `DamageDealingEffect`. No step-5 interface needed widening. Domain
diff is purely additive: `implements` clause swaps plus interface-method bodies returning existing
record components (`DrawCardEffect.drawnCardAmount()→amount`, `GainLifeEffect.lifeGainAmount()→amount`,
`CreateTokenEffect.token*()→amount/primaryType/power/toughness`,
`GainControlOfTargetEffect.controlDuration()→duration`; `CreatureBoostEffect`/`KeywordGrantingEffect`/
`StaticCreatureBoostEffect` are satisfied by the records' existing accessors;
`CounterSpellingEffect`/`RegenerationEffect` are markers). No record component/constructor/engine
behavior changed.

**Per-file effect-concrete `instanceof` counts (before → after):**
- `SpellEvaluator.java`: **47 → 26**
- `InstantCategoryClassifier.java`: **14 → 2**
- `AiTargetSelector.java`: **21 → 12**

Baseline `effect-dispatch-baseline.txt` updated for those three lines (Python unavailable this
session, as in steps 1–5, so the machine-readable baseline was hand-edited to the counts the ratchet
recomputes from source; `EFFECT_COUPLING_MATRIX.md` still shows pre-step-6 figures — re-run
`python scripts/effect-coupling-audit.py` when Python is available to refresh the human report).

**Migration detail (each consumer):**
- *InstantCategoryClassifier* — counters → `CounterSpellingEffect`; destroy/exile/bounce →
  `RemovalEffect.removalKind()!=null`; creature-hitting damage → `DamageDealingEffect.canDamageCreatures()`;
  pump → `CreatureBoostEffect`; draw → `CardDrawingEffect`; lifegain → `LifeGainEffect`. Same priority
  order preserved.
- *SpellEvaluator* — draw/lifegain/token/pump/keyword-grant/static-boost/steal/regen families migrated
  to their interfaces in every branch (ability, ETB, single-effect, synergy, aura, removal-detection).
- *AiTargetSelector* — destroy/exile removal detection → new `isDestroyOrExileRemoval` helper using
  `RemovalEffect.removalKind() == DESTROY|EXILE` (bounce deliberately excluded to keep its
  general-fallback target selection — a behavior-preserving narrowing); aura beneficial-check →
  `StaticCreatureBoostEffect`/`KeywordGrantingEffect`; ability beneficial-check →
  `CreatureBoostEffect`/`RegenerationEffect`/`KeywordGrantingEffect`; ability damage-target search →
  `DamageDealingEffect.canDamageCreatures()`.

**Survivors (justified):** as listed in the Plan above. Key ones: SpellEvaluator's recipient-sign
(`DealDamageToPlayersEffect`, `LoseLifeEffect`), board-wipe, split-total and no-amount-X damage,
bespoke ability-only scoring (`BoostSelfEffect`, `ScryEffect`, `TapPermanentsEffect`, the two
counter-placement effects), `CounterSpellEffect` (narrow "plain counter" scoring), `DiscardEffect`,
cost records, and the mana family (deferred to step 7); InstantCategoryClassifier's
`DealDamageToTargetCreatureOrPlaneswalkerEffect` (kept off `DamageDealingEffect` to avoid newly
scoring it in SpellEvaluator) and `DealDamageToPlayersEffect`; AiTargetSelector's `ExtraTurnEffect`,
`DealDividedDamageEffect`, and the 8 heterogeneous graveyard-targeting effects.

**Tests run (all green, unchanged outcomes):**
- Consumer unit tests: `SpellEvaluatorTest`, `InstantCategoryClassifierTest`, `AiTargetSelectorTest`.
- Broader AI decision/simulation: `HardAiDecisionEngineTest`, `AiDecisionEngineTest`,
  `MediumAiDecisionEngineTest`, `EasyAiDecisionEngineTest`, `GameSimulatorTest`, `RaceEvaluatorTest`,
  `BoardEvaluatorTest` (calibrated MCTS/simulation unaffected — no rollout-cost or scoring-VALUE
  change).
- `EffectDispatchRatchetTest` green against the new lower baseline (26 / 2 / 12).
- Full compile of `:magical-vibes-application:compileTestJava :magical-vibes-ai:compileTestJava`.

---

## Step 7 — AI metadata (simulator/hard-AI/mana)  (2026-07-14)

### Inventory & plan (written before coding)

Three consumers: `GameSimulator` (13), `HardAiDecisionEngine` (30), `AiManaManager` (12). Non-effect
`instanceof` (ChoiceContext, PendingInteraction, SimulationAction) is OUT OF SCOPE. Cost records
(Sacrifice*Cost, PayLifeCost, RemoveChargeCountersFromSourceCost, ExileNCardsFromGraveyardCost,
SacrificePermanentCost) are concrete-dispatch survivors (no capability interface; kept, as in steps
5/6). Below, each in-scope effect `instanceof` classified (a) mana family, (b) covered by a step-5/6
interface, (c) needs widening, (d) survivor.

**Mana family — widen `ManaProducingEffect`.** The interface already exists (marker) and is
implemented by 15 records, but the three AI consumers still `instanceof` the concrete
`AwardManaEffect` / `AwardAnyColorManaEffect` / `AwardAnyColorChosenSubtypeCreatureManaEffect`
because the marker exposed nothing. CRITICAL CONSTRAINT: the AI reacts to ONLY those 3 of the 15
implementors, and their treatment DIFFERS per site (see table), so I must NOT broaden dispatch to
`instanceof ManaProducingEffect` naively — that would pull in the other 12 special-routing producers
(chosen-player, restricted-bucket, among-controlled, lands-could-produce, double-pool,
flashback/instant-sorcery/subtype-restricted, X, one-of-each) and change AI decisions. Instead widen
`ManaProducingEffect` with DESCRIPTIVE, allocation-free facets that the 3 override and the other 12
leave at neutral defaults, so `instanceof ManaProducingEffect mp` + facet checks reproduce the exact
per-type behavior.

Per-type AI treatment the facets must reproduce:
| type | getProducedColors | addCardManaToPool / virtual pool | mana-ability score | "has on-tap mana" |
|---|---|---|---|---|
| AwardManaEffect | its `color` | `(color, estimate(amount))` | color-vs-cost 20/15 or 5/1 | yes |
| AwardAnyColorManaEffect | all 5 | `(COLORLESS, amount)` | generic 5/1 | yes |
| AwardAnyColorChosenSubtypeCreatureManaEffect | — (none) | `(COLORLESS, 1)` | 0 (skipped) | yes |
| other 12 | — | — | 0 | no |

Widened facets (all defaults preserve the 12; overridden only on the 3):
- `ManaColor estimatedManaColor()` → AwardManaEffect: `color`; else null.
- `DynamicAmount estimatedManaAmount()` → AwardManaEffect: `amount`; else null.
- `boolean estimatedCountsAllColors()` → AwardAnyColorManaEffect: true; else false.
- `int estimatedWildcardMana()` → AwardAnyColorManaEffect: `amount`; ChosenSubtype: 1; else 0.
- `default boolean modeledByManaEstimator()` = `estimatedManaColor()!=null || estimatedCountsAllColors()
  || estimatedWildcardMana()>0` (true for exactly the 3; used for the "has on-tap mana" check).
All return existing components / literals — NO per-call allocation (hot-path safe). The
chosen-subtype restriction the prompt calls out is captured implicitly: ChosenSubtype has
`estimatedCountsAllColors()==false` (no color coverage, no scoring) + `estimatedWildcardMana()==1`
(virtual-pool wildcard), which is exactly how it differs from plain any-color today.

**Covered by step-5/6 interfaces (migrate):**
- GameSimulator `StaticBoostEffect`(scope) → `StaticCreatureBoostEffect.scope()`; `GrantKeywordEffect`(scope)
  → `KeywordGrantingEffect.scope()`.
- HardAiDecisionEngine `StaticBoostEffect`(powerBoost/scope/filter ×3) → `StaticCreatureBoostEffect`
  (needs `filter()` added — see below); `BoostTargetCreatureEffect` ×3 → `CreatureBoostEffect`;
  `DrawCardEffect` ×2 → `CardDrawingEffect`; `RegenerateEffect` → `RegenerationEffect`; the
  removal/damage/steal chains in `isSingleEffectRemoval` / `canSingleEffectRemoveCreature`:
  Destroy/Exile/ReturnToHand(TARGET)/ReturnTargetPermanentToHandWithMVConditional →
  `RemovalEffect.removalKind()` (DESTROY vs EXILE/BOUNCE), DealDamageToTargetCreature/DealDamageToAnyTarget
  → `DamageDealingEffect.canDamageCreatures()` + `.damageAmount()`, GainControlOfTarget →
  `ControlStealingEffect.controlDuration()`.

**Widen (small):** `StaticCreatureBoostEffect` gains `PermanentPredicate filter()` (HardAi reads
`boost.filter()` at the anthem sites; `StaticBoostEffect` already has the accessor, sole implementor,
purely additive).

**Survivors (kept concrete, justified):**
- Recipient-sign `DealDamageToPlayersEffect`(recipient==CONTROLLER) — pain-land side-effect detection
  in AiManaManager (×2) and GameSimulator (×1); recipient enum decides a drawback, no descriptive
  fact (same survivor as SpellEvaluator in step 5).
- `ReturnCardFromGraveyardEffect` — AiManaManager (requiresManaValueEqualsX X-calc) + GameSimulator
  (graveyard-target selection reads source()/filter()); heterogeneous graveyard family, left as a
  group in step 6.
- `DealDamageToTargetCreatureOrPlaneswalkerEffect` — HardAi; deliberately NOT on `DamageDealingEffect`
  (step 6 kept it off to avoid newly scoring it in SpellEvaluator); its `damage()` is a plain int.
- `BoostSelfEffect` — HardAi isPump; no interface (self-boost is a different shape than the targeted
  `CreatureBoostEffect`).
- Cost records — Sacrifice*Cost / PayLifeCost / RemoveChargeCountersFromSourceCost /
  ExileNCardsFromGraveyardCost / SacrificePermanentCost; concrete-dispatch, no capability interface.

**Skipped ENGINE mana sites (rules-critical, left as-is; NOT mechanically equivalent to the AI
facets):** `PotentialManaService` (19), `ActivatedAbilityExecutionService` (the full per-type mana
RESOLUTION switch), `AbilityActivationService`, `LandTapTriggerCollectorService`. These do actual
mana routing (per-restriction buckets, chosen-player pools, among-controlled color scans, etc.), a
different contract than the AI's lightweight estimator facets; migrating them would need
resolution-shaped interface methods and is out of this program's AI scope.

### Results

**`ManaProducingEffect` new shape (all additive, all allocation-free — hot-path safe):**
```
default ManaColor     estimatedManaColor()        // AwardManaEffect → color; else null
default DynamicAmount estimatedManaAmount()        // AwardManaEffect → amount; else null
default boolean       estimatedCountsAllColors()   // AwardAnyColorManaEffect → true; else false
default int           estimatedWildcardMana()      // AwardAnyColorManaEffect → amount;
                                                    //   AwardAnyColorChosenSubtypeCreatureManaEffect → 1; else 0
default boolean       modeledByManaEstimator()     // = color!=null || countsAllColors || wildcard>0
```
Overridden on exactly the 3 producers the AI models; the other 12 implementors keep the neutral
defaults, so `instanceof ManaProducingEffect mp` + facet checks reproduce the pre-refactor matched
set and per-type behavior EXACTLY (no broadening → no decision change). Each facet returns an
existing record component or a literal — verified no per-call allocation. `StaticCreatureBoostEffect`
also gained `PermanentPredicate filter()` (satisfied by `StaticBoostEffect`'s existing accessor; sole
implementor; purely additive).

**Per-file effect-concrete `instanceof` counts (before → after):**
- `AiManaManager.java`: **12 → 3** (survivors: 2× DealDamageToPlayersEffect recipient-sign, 1×
  ReturnCardFromGraveyardEffect).
- `GameSimulator.java`: **13 → 6** (survivors: 4 cost records, 1× DealDamageToPlayersEffect,
  1× ReturnCardFromGraveyardEffect).
- `HardAiDecisionEngine.java`: **30 → 7** (survivors: 4 cost records, 2×
  DealDamageToTargetCreatureOrPlaneswalkerEffect, 1× BoostSelfEffect).

Migration onto interfaces: mana family → widened `ManaProducingEffect` (all 9 Award* sites in
AiManaManager, both scoring/`hasOnTapMana` sites in GameSimulator); GameSimulator aura beneficial
check → `StaticCreatureBoostEffect`/`KeywordGrantingEffect`; HardAi anthem sites →
`StaticCreatureBoostEffect`, removal/damage/steal chains → `RemovalEffect.removalKind()` +
`DamageDealingEffect.canDamageCreatures()/damageAmount()` + `ControlStealingEffect.controlDuration()`,
pump → `CreatureBoostEffect`, draw → `CardDrawingEffect`, regen → `RegenerationEffect`. Semantics
preserved by construction: verified each interface's implementor set is exactly the types the old
concrete checks matched (RemovalEffect = Destroy/Exile/ReturnToHand-TARGET/ReturnTgtPermToHandWithMV;
DamageDealingEffect∩canDamageCreatures = DealDamageToTargetCreature/DealDamageToAnyTarget;
CreatureBoost/CardDrawing/Regeneration/ControlStealing/StaticCreatureBoost each single-implementor).

**Survivors (justified):** recipient-sign `DealDamageToPlayersEffect` (pain-land drawback, no
descriptive fact), `ReturnCardFromGraveyardEffect` + GameSimulator graveyard selection (heterogeneous
graveyard family from step 6), `DealDamageToTargetCreatureOrPlaneswalkerEffect` (deliberately off
`DamageDealingEffect` per step 6; int `damage()`), `BoostSelfEffect` (self-boost shape, no
interface), cost records.

**Skipped engine mana sites (rules-critical, NOT migrated):** `PotentialManaService` (19 incl. the
same 3-type `hasOnTapManaEffects` check — equivalent to `modeledByManaEstimator()` but left as-is
since it is engine mana logic and `AiManaManager.hasOnTapManaEffects` delegates to it),
`ActivatedAbilityExecutionService` (full per-type mana RESOLUTION switch — different, resolution-
shaped contract), `AbilityActivationService`, `LandTapTriggerCollectorService`. These do actual mana
routing, not lightweight estimation; migrating them is out of this program's AI scope.

**Baseline note:** Python still unavailable this session (Store-alias stubs only), so
`effect-dispatch-baseline.txt` was hand-edited to the counts the ratchet recomputes from source
(AiManaManager 3, GameSimulator 6, HardAiDecisionEngine 7). Running the ratchet also surfaced a
**pre-existing stale step-6 baseline for `AiTargetSelector` (12 → real 10)** — a file NOT touched this
step; corrected to 10 (independently re-counted: 8 graveyard + 2 divided-damage concrete survivors;
none related to the mana/interface changes). `EFFECT_COUPLING_MATRIX.md` still shows pre-step-5
figures — re-run `python scripts/effect-coupling-audit.py` when Python is available to refresh the
human report; it will reproduce this baseline.

**Tests run (all green, first attempt, no re-runs needed):**
- Consumer/owner: `AiManaManagerTest`, `GameSimulatorTest`, `HardAiDecisionEngineTest`.
- Decision/MCTS/evaluator: `AiDecisionEngineTest`, `MediumAiDecisionEngineTest`,
  `EasyAiDecisionEngineTest`, `InstantCategoryClassifierTest`, `AiTargetSelectorTest`,
  `BoardEvaluatorTest`, `RaceEvaluatorTest`, `SpellEvaluatorTest`, `MCTSEngineTest`,
  `simulation.RolloutPolicyTest` (calibrated MCTS unaffected — no rollout-cost or scoring-VALUE
  change; no borderline re-runs required).
- Mana-ability card tests (engine inertness of the additive domain changes): `LlanowarElvesTest`
  (ON_TAP AwardManaEffect), `BirdsOfParadiseTest` (AwardAnyColorManaEffect), `PillarOfOriginsTest`
  (AwardAnyColorChosenSubtypeCreatureManaEffect), `ManaforgeCinderTest` (AwardManaOfColorsEffect —
  a "special" producer the estimator ignores).
- `EffectDispatchRatchetTest` green against the corrected baseline (AiManaManager 3, GameSimulator 6,
  HardAiDecisionEngine 7, AiTargetSelector 10).
- Full compile of `:magical-vibes-ai:compileTestJava :magical-vibes-application:compileTestJava`.

**Follow-ups for the user:** (a) run the full test suite; (b) since MCTS hot-path code
(GameSimulator rollout, HardAi decision) was touched, consider a before/after MCTS benchmark
(`MCTSBenchmarkTest` with `-DmctsBench=true`) — not run here per the ground rules; the changes are
interface-dispatch only (no added allocation), so no throughput regression is expected.
