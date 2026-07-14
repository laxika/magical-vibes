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

---

## Step 8 — Close-out  (2026-07-14)

Locked the new rules in and made them discoverable by the daily card workflow. No production source
was changed this step (only the ratchet test's failure message, the `agent-docs/`, and the
`implement-card` skill), so the baseline needed no regeneration.

### Re-audit & burn-down

**Audit tool could not be re-run:** `python` is still unavailable this session (Store-alias stubs
only, as in every step 1–7), so the human report `EFFECT_COUPLING_MATRIX.md` still shows the
pre-step-5 figures. The machine-readable baseline `effect-dispatch-baseline.txt` IS current, and
`EffectDispatchRatchetTest` (which re-implements the audit's counting rules in Java and recomputes
per-file totals from live source) **passes against it** — so the baseline equals a fresh audit's
per-file output. The burn-down below is derived from that ratchet-verified baseline vs the step-1
numbers recorded above. **Re-run `python scripts/effect-coupling-audit.py` when Python is available
to refresh the human report; it will reproduce the current baseline (total 585).**

**Burn-down — per module:**
| Module | Step 1 | Step 8 | Δ |
|--------|-------:|-------:|--:|
| engine | 512 | 512 | 0 |
| ai | 174 | 73 | −101 |
| **total** | **686** | **585** | **−101 (−15%)** |

Engine is unchanged: this program's AI-visibility work never touched engine dispatch; steps 3–4 added
validators in the exempt `service/validate/` zone and refactored the target services without any net
new/removed effect-`instanceof` (the `service/target` package stayed at 19). All 101 removed
violations are in the AI module.

**Burn-down — per AI package / per migrated file:**
| Bucket | Step 1 | Step 8 | Δ |
|--------|-------:|-------:|--:|
| ai (root) | 161 | 67 | −94 |
| ai/simulation | 13 | 6 | −7 |
| · SpellEvaluator | 65 | 26 | −39 |
| · HardAiDecisionEngine | 30 | 7 | −23 |
| · InstantCategoryClassifier | 14 | 2 | −12 |
| · AiTargetSelector | 21 | 10 | −11 |
| · AiManaManager | 12 | 3 | −9 |
| · GameSimulator | 13 | 6 | −7 |

The six migrated files account for the entire −101 (39+23+12+11+9+7). Non-migrated AI files
(AiDecisionEngine 7, BoardEvaluator 8, RaceEvaluator 2, CombatSimulator 1, EasyAiDecisionEngine 1)
were left as-is (out of the step 5–7 family scope).

### Top-5 remaining offenders — classified

All five are ENGINE files, all outside this program's AI-visibility scope. Legend: (a) misdetected
interaction-model dispatch → fix the script; (b) justified survivor documented in an earlier step;
(c) genuine leftover for a future pass.

1. **`MayAbilityHandlerService` (61, service/input)** — **(c)**. Engine interaction-model dispatch:
   routes each concrete effect to its bespoke "you may …" player-choice presentation (may-cast,
   surveil, explore, counter-unless-pays, imprint, copy-spell retarget, …). Not AI, not the
   resolution registry — a different registry problem. Candidate for a future `MayEffectHandler`
   registry (analogous to `normalfx`). NOT (a): its only non-effect tokens (`MayEffect` wrapper,
   `PermanentPredicateTargetFilter`) are already correctly excluded by the script.
2. **`GameQueryService` (55, service/battlefield)** — **(c)**. Static/continuous-effect QUERY
   dispatch (protection / can-be-blocked / animation / double-damage read off continuous effects) —
   CR 613 layer-system query logic. A future pass could move it behind capability interfaces or
   extend the `staticfx` registry with query methods. (`ConditionalEffect` = exempt wrapper.)
3. **`ActivatedAbilityExecutionService` (42, service/ability)** — **(b)**. Documented in Step 7 as a
   SKIPPED ENGINE MANA SITE: the full per-type `Award*Mana*Effect` RESOLUTION switch (rules-critical
   mana routing, a resolution-shaped contract NOT mechanically equivalent to the AI's lightweight
   estimator facets) plus cost records. Deliberately left; migrating it needs resolution-shaped
   interface methods, out of AI scope.
4. **`CombatDamageService` (40, service/combat)** — **(c)**. Engine combat-damage RESOLUTION dispatch
   (route combat damage to lifelink / destroy / exile / draw / mill / poison triggers). Noted as an
   engine consumer already in Step 5; a future pass could route it via a combat-damage-effect handler
   registry. (`ConditionalEffect`/`MayEffect` exempt; `Metalcraft`/`EventValue` are not effect types.)
5. **`AbilityActivationService` (37, service/ability)** — mixed **(b)+(c)**. Majority is cost-record
   dispatch (`Sacrifice*Cost` / `Tap*Cost` / `RemoveCounter*Cost` / …) — the documented cost-record
   survivor class of steps 5–7 (no capability interface); the remainder are a few engine-resolution
   effect checks out of AI scope. (`Fixed` = `DynamicAmount.Fixed`, correctly excluded per Step 1;
   `CostEffect`/`ManaProducingEffect` = exempt interfaces.)

**No (a) case exists → no script classification fix needed.** The audit universe is exactly the
filenames under `model/effect/`, so interaction-model types (`ChoiceContext`, `PendingInteraction`,
`SimulationAction`), `TargetFilter` types, `DynamicAmount.Fixed`, `Metalcraft`, `EventValue`, etc. are
already never counted. The script's classification is correct as written.

### Ratchet tightened

`EffectDispatchRatchetTest` confirmed green against the current baseline. Its "new dispatch added"
failure message was rewritten from the vague "Register knowledge with the effect's handler/validator
instead" to an ACTIONABLE menu of the four sanctioned mechanisms steps 3–7 established, telling a
card-implementing agent exactly what to do instead of adding an `instanceof`:
- resolution behaviour → a `NormalEffectHandlerBean` under `service/effect/normalfx` (EffectHandlerRegistry);
- static/continuous behaviour → a `StaticEffectHandler` under `service/effect/staticfx` (StaticEffectHandlerRegistry);
- legal-target checking for a targeted effect → a `@ValidatesTarget` validator under `service/validate`;
- AI/query needs a FACT → implement the matching capability interface in `magical-vibes-domain`
  `model/effect` (DamageDealingEffect, RemovalEffect, ManaProducingEffect, CardDrawingEffect, …).
(The message change is in the test module — not scanned by the ratchet — and contains no
`instanceof <EffectType>` token, so the baseline is unaffected.)

### Docs updated

- **`agent-docs/ARCHITECTURE.md`** "Key Patterns" → Effect-system bullet rewritten: corrected the
  stale "`GameService.resolveStackEntry()` … via `instanceof`" claim to the class-keyed
  `NormalEffectHandlerBean` + `EffectHandlerRegistry` mechanism, and added the full NEW-effect
  registration checklist (record + normalfx/staticfx + `@ValidatesTarget` IF targeted + capability
  interface IF AI-family, all 12 named) with the ratchet-enforcement note.
- **`agent-docs/EFFECTS_QUICK_REFERENCE.md`** → "Marker interfaces" expanded into a "Capability /
  marker interfaces" catalog: all 12 interfaces (was 4) with what each describes, the step-7
  `ManaProducingEffect` estimator facets, and a "when to implement" instruction.
- **`agent-docs/EFFECTS_INDEX.md`** → "Marker interfaces" table gained the 9 step-6/7 interfaces
  (was 4 rows) plus the ratchet-enforcement note.
- **`agent-docs/TRIGGER_SLOT_TARGETING.md`** → new section "Spell / activated-ability target
  validation (a DIFFERENT mechanism)": targeted single-`targetId` effects REQUIRE a `@ValidatesTarget`
  validator; all three spell-target validation paths share one structural core
  (`TargetLegalityService.checkSpellPermanentTargetableReason`), and the single-target paths run
  per-effect validators via `TargetValidationService.checkEffectTargets`.
- **`agent-docs/CARD_IMPLEMENTATION_PLAYBOOK.md`** → "When a new effect is actually required" gained
  the capability-interface bullet (the record + normalfx/staticfx + cost-mod + `@ValidatesTarget`
  steps were already present and accurate).
- **`.claude/skills/implement-card/SKILL.md`** (skill IS editable) → Step 5 rewritten: corrected the
  stale "Add resolution logic in `GameService.resolveStackEntry()` (instanceof dispatch)" to
  `NormalEffectHandlerBean`/`staticfx`, added the `@ValidatesTarget` (targeted) and capability-interface
  (AI) steps, and the ratchet warning.
- Grepped all `agent-docs/` for stale validation/`resolveStackEntry` claims — none remain
  (`EFFECTS_INDEX.md` §8–12 and `CARD_IMPLEMENTATION_PLAYBOOK.md` already described
  `NormalEffectHandlerBean` + `@ValidatesTarget` correctly).

### Final verification sweep (all green)

- `EffectDispatchRatchetTest` — 1 test, re-run fresh after the failure-message edit (recompiled, 0.8s).
- Card tests, one per letter touched by steps 3–4: `WrackWithMadnessTest` (7), `DarkNourishmentTest`
  (7), `FireballTest` (19), `TerrorTest` (6), `PacifismTest` (14), `ArcTrailTest` (12) — all pass.
- `GameSimulatorTest` — 17 tests, 0 failures.
- `SpellEvaluatorTest` — 63 tests, 0 failures.

### Future work (accumulated across all 8 steps)

1. **Interaction-model dispatch (out of scope by definition):** `MayAbilityHandlerService` (61)
   "may"-choice routing; `GameSimulator`/`HardAiDecisionEngine` non-effect `instanceof`
   (`ChoiceContext`/`PendingInteraction`/`SimulationAction`). A `MayEffectHandler` registry could
   absorb the "may" family.
2. **Static/continuous-effect query dispatch:** `GameQueryService` (55) — move behind capability
   interfaces or a `staticfx` query API.
3. **Engine combat resolution dispatch:** `CombatDamageService` (40), `CombatBlockService` (18),
   `CombatAttackService` (15) — a combat-effect handler registry (analogous to `normalfx`).
4. **Resolution-side guard TODOs from Step 3:** belt-and-suspenders resolution-time type re-checks in
   the `normalfx` handlers for the creature-only effects (none required by a failing test today; the
   step-3 targeting validators already block the illegal target before resolution).
5. **Skipped engine mana sites from Step 7 (rules-critical, NOT AI-facet-equivalent):**
   `PotentialManaService` (19), `ActivatedAbilityExecutionService` mana RESOLUTION switch (in the 42),
   `AbilityActivationService` (37, mostly cost records), `LandTapTriggerCollectorService` (2).
   Migrating needs resolution-shaped interface methods.
6. **Cost-record dispatch** (`Sacrifice*Cost`/`Tap*Cost`/…): no capability interface yet; pervasive in
   `AbilityActivationService`, `GameSimulator`, `HardAiDecisionEngine`. A `CostEffect`-family facet
   could collapse these if ever wanted.
7. **Record-family collapses the matrix suggests** (see the DynamicAmount-refactor roadmap): the
   remaining `Award*Mana` producer variants and SpellEvaluator's board-wipe / split-damage / no-amount-X
   survivor families.
8. **AiTargetSelector cleanups:** the redundant hand-patch left in place in Step 4, and the 8
   heterogeneous graveyard-targeting effects (Step 6) — could unify behind a graveyard-target
   capability if a uniform fact emerges.
9. **Refresh the human matrix report:** `EFFECT_COUPLING_MATRIX.md` is stale (Python was unavailable
   in every session 1–8). Run `python scripts/effect-coupling-audit.py` once Python is available; it
   reproduces the current ratchet-verified baseline (total 585).

**For the user:** run the full test suite and commit the program's changes (this session touched only
the ratchet test message, `agent-docs/`, and the `implement-card` skill; nothing under `src/main`).

## Step 9 — Engine protection family → `ProtectionGrantingEffect`  (2026-07-14)

First **phase-2** step: this program's phases 1 (steps 1–8) migrated AI-visible effect knowledge;
phase 2 starts migrating ENGINE query dispatch behind capability interfaces. Step 9 collapses the
static-protection family — the exact "static/continuous-effect query dispatch" future-work item #2
called out in Step 8 (`GameQueryService` was the #2 top offender at 55).

### What was migrated

The four printed, statically-known protection records now implement one capability interface, and the
`GameQueryService` protection queries read facts off that interface instead of `instanceof`-ing each
concrete record:

- `ProtectionFromColorsEffect` → `protectionFromColors()` + `protectionScope()`
- `ProtectionFromCardTypesEffect` → `protectionFromCardTypes()`
- `ProtectionFromSubtypesEffect` → `protectionFromSubtypes()`
- `ProtectionFromManaValueEffect` → `protectionFromManaValueAtLeast()` (`OptionalInt`)

### Interface design — `ProtectionGrantingEffect` (domain `model/effect/`)

One umbrella interface with five default methods (empty-set / `null` / `OptionalInt.empty()`), each a
pure read of an existing record component; every record overrides only the facet it carries and
inherits the rest. Rationale:

- **Why one interface, not four markers.** The seven consuming sites in `GameQueryService` are four
  independent query methods (colour / card-type / subtype / mana-value protection). A single
  `instanceof ProtectionGrantingEffect protection` + facet read replaces every concrete `instanceof`,
  and a facet that doesn't apply to a given record returns its empty default, so mixing record types
  in the same STATIC-effect loop is behaviour-neutral (e.g. a card-type protection effect seen by the
  colour loop returns an empty `protectionFromColors()` → no match, exactly as before).
- **Why `protectionScope()` is on the shared interface.** The self-vs-equipped-creature `GrantScope`
  is only carried by `ProtectionFromColorsEffect`, but the "own printed protection stands" branch of
  `hasProtectionFrom` filters on `scope() == null`. Exposing `protectionScope()` (default `null` =
  self) lets that site keep its exact filter without downcasting; the non-colour records are always
  self-scoped, so the `null` default is not just harmless but semantically correct.
- **What deliberately does NOT implement it.** Runtime-only protection whose protected set is not a
  pure function of a record's components: chosen-colour (`ProtectionFromChosenColorEffect`, resolved
  via `ChooseColorEffect` + `Permanent.getChosenColor()`), and "protection from non-[subtype]
  creatures" (tracked on the `Permanent`, not on any effect record). Those query paths in
  `GameQueryService` were left untouched — they never `instanceof`-ed a protection record.

### Sites migrated

All in `GameQueryService` (`service/battlefield`), 7 concrete `instanceof` → interface:

| Method | Was | Now |
|--------|-----|-----|
| `hasProtectionFrom` (own printed, `bonus == NONE`) | `ProtectionFromColorsEffect` + `scope()==null` + `colors()` | `ProtectionGrantingEffect` + `protectionScope()==null` + `protectionFromColors()` |
| `hasProtectionFrom` (granted-effects loop) | `ProtectionFromColorsEffect` + `colors()` | `ProtectionGrantingEffect` + `protectionFromColors()` |
| `hasProtectionFromSourceCardTypes` (×2 overloads) | `ProtectionFromCardTypesEffect` + `cardTypes()` | `ProtectionGrantingEffect` + `protectionFromCardTypes()` |
| `hasProtectionFromSourceSubtypes` (×2 overloads) | `ProtectionFromSubtypesEffect` + `subtypes()` | `ProtectionGrantingEffect` + `protectionFromSubtypes()` |
| `hasProtectionFromSourceManaValue` | `ProtectionFromManaValueEffect` + `minManaValue()` | `ProtectionGrantingEffect` + `protectionFromManaValueAtLeast().isPresent()/getAsInt()` |

The mana-value site gained an `isPresent()` guard (the interface returns `OptionalInt.empty()` for
non-mana-value protection that the interface match now also admits) — behaviour-identical: empty →
skip. The four now-unused record imports were dropped and one `{@link}` in a javadoc retargeted to
the interface; `GrantScope` import stays (used by unrelated animation/boost queries).

### Sites intentionally left

- `service/effect/staticfx/StaticEffectSupport.java` (1) and `service/effect/LayerSystemService.java`
  (2) `instanceof ProtectionFromColorsEffect` — **exempt registry zone** (`service/effect/**`), not
  counted by the ratchet and out of scope by the program's rules. Untouched.
- The chosen-colour / non-subtype-creature protection query paths (see interface design above) — not
  record-`instanceof` sites, no capability fact to expose.

### Per-file violation drop

| File | Before | After | Δ |
|------|-------:|------:|--:|
| `service/battlefield/GameQueryService.java` | 55 | 48 | −7 |

Program total (ratchet-verified): **585 → 578** (−7, all in `GameQueryService`). `GameQueryService`
falls from the #2 top offender toward the pack; the remaining 48 are combat/animation/double-damage
and other continuous-effect query dispatch for a later phase-2 step.

### Tests run (all green)

- Card tests (2–3 per record + the equipped-creature scope path): `SwordOfWarAndPeaceTest` (18),
  `WhiteKnightTest` (11), `PaladinEnVecTest` (16), `TelJiladFallenTest` (6, card-type protection),
  `BaneslayerAngelTest` (6), `GraveBrambleTest` (4, subtype protection), `MistmeadowSkulkTest` (3,
  mana-value protection).
- `SevenLayerTest` — CR 613 layer spec, all pass (protection queries feed layer 6). No expectation
  weakened.
- `EffectDispatchRatchetTest` — green after the baseline drop was locked in.

### Baseline / matrix regeneration — NOTE: Python IS available this session

Contrary to the standing "Python unavailable" caveat from steps 1–8, **`python` (3.14.6) is on PATH
this session**, so `scripts/effect-coupling-audit.py` was run for real:
`effect-dispatch-baseline.txt` and the human report `EFFECT_COUPLING_MATRIX.md` are BOTH refreshed
(total 578). The regenerated baseline is byte-identical to the hand-edit the ratchet asked for (only
`GameQueryService=55` → `=48`), independently confirming the count. **`EFFECT_COUPLING_MATRIX.md` is
no longer stale.**

### Files changed

- `magical-vibes-domain/.../model/effect/ProtectionGrantingEffect.java` (new)
- `ProtectionFromColorsEffect`, `ProtectionFromCardTypesEffect`, `ProtectionFromSubtypesEffect`,
  `ProtectionFromManaValueEffect` (implement the interface)
- `magical-vibes-engine/.../service/battlefield/GameQueryService.java` (7 sites + imports/javadoc)
- `refactor-docs/effect-dispatch-baseline.txt`, `refactor-docs/EFFECT_COUPLING_MATRIX.md` (regenerated)
- `agent-docs/EFFECTS_QUICK_REFERENCE.md`, `agent-docs/EFFECTS_INDEX.md` (catalog entries)

**For the user:** run the full test suite and commit. This step touched `src/main` (domain + engine)
for the first time in the program — a behaviour-preserving query refactor, no game-rules change.

## Step 10 — Engine block/attack restriction family → four combat-restriction capability interfaces  (2026-07-14)

Second **phase-2** step. Collapses the block/attack RESTRICTION family that `GameQueryService`
dispatched via `instanceof <concrete record>` into four descriptive capability interfaces in the
domain effect package. Scope this step: **`GameQueryService` consuming sites only** — `CombatBlockService`
and `CombatAttackService` are deliberately left for the next step (they still `instanceof` these records).

### Interface designs (domain `model/effect/`, all pure component reads, engine owns evaluation)

1. **`BlockabilityRestrictionEffect`** (Group A — attacker-side "who may block this creature"):
   - `boolean cantBeBlocked()` — default `false`
   - `PermanentPredicate unblockableIfDefenderControls()` — default `null`
   - `boolean unblockableIfControllerCastHistoricSpellThisTurn()` — default `false`
   - `boolean unblockableWhileAttackingAlone()` — default `false`
   - `PermanentPredicate blockableOnlyBy()` — default `null`
   - `String blockableOnlyByDescription()` — default `null`
   - `PermanentPredicate cantBeBlockedByCreaturesMatching()` — default `null`
   Impl: `CantBeBlockedEffect`, `CanBeBlockedOnlyByFilterEffect`,
   `CantBeBlockedByCreaturesMatchingPredicateEffect`,
   `CantBeBlockedIfDefenderControlsMatchingPermanentEffect`,
   `CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect`, `CantBeBlockedIfAttackingAloneEffect`.

2. **`BlockingRestrictionEffect`** (Group B — blocker-side "what a creature may block"):
   - `boolean cantBlock()` — default `false`
   - `PermanentPredicate canBlockOnlyAttackersMatching()` — default `null`
   - `String canBlockOnlyAttackersDescription()` — default `null`
   - `PermanentPredicate globalCantBlockBlockerMatcher()` — default `null`
   - `PermanentPredicate globalCantBlockAttackerMatcher()` — default `null`
   - `String globalCantBlockDescription()` — default `null`
   Impl: `CantBlockEffect`, `CanBlockOnlyIfAttackerMatchesPredicateEffect`,
   `MatchingCreaturesCantBlockMatchingCreaturesEffect`.

3. **`AttackOrBlockRestrictionEffect`** (Group C — combined "can't attack or block"):
   - `PermanentPredicate globallyCantAttackOrBlock()` — default `null`
   - `Condition cantAttackOrBlockUnless()` — default `null`
   - `String restrictionDescription()` — default `null`
   Impl: `MatchingCreaturesCantAttackOrBlockEffect`, `CantAttackOrBlockUnlessEffect`.

4. **`NoDefenderAttackPermissionEffect`** (Group D — "attack as though no defender"):
   - `boolean grantsCarrierAttackAsThoughNoDefender()` — default `false`
   - `PermanentPredicate noDefenderAttackMatcher()` — default `null`
   Impl: `CanAttackAsThoughNoDefenderEffect`, `MatchingCreaturesCanAttackAsThoughNoDefenderEffect`.

Design notes:
- **Umbrella-with-defaults pattern** (as `ProtectionGrantingEffect`): each record overrides only the
  facet it carries; a query site matches the interface once and reads the relevant facet, and records
  that don't carry that facet return the empty/`false`/`null` default → behaviour-neutral when several
  record kinds share a static-effect loop.
- **Predicates are exposed, never evaluated in the domain.** Methods return the raw `PermanentPredicate`
  / `Condition` off the record; the engine keeps calling `predicateEvaluationService` /
  `conditionEvaluationService`. The domain imports nothing new (both are already domain types).
- **Escape hatch used at the attacking-alone site.** `unblockableWhileAttackingAlone()` is a pure
  marker; the engine-side `isAttackingAlone(gameData, attacker)` computation (combat state not on
  `GameData`) stays in `GameQueryService` and is `&&`-ed with the marker. Likewise the historic-spell
  fact stays engine-side (`playerCastHistoricSpellThisTurn`).
- **Group C uses one combined interface**, not separate attack/block interfaces: the "can't attack or
  block" records read the SAME components for both sides, so `AttackOrBlockRestrictionEffect` serves the
  block-side consumer here AND the attack-side consumer (`CombatAttackService`) in the next step.

### Sites migrated in `GameQueryService` (15 concrete `instanceof` → interface)

| Method | Was `instanceof` | Now |
|--------|------------------|-----|
| `canAttackDespiteDefender` (self) | `CanAttackAsThoughNoDefenderEffect` | `NoDefenderAttackPermissionEffect` + `grantsCarrierAttackAsThoughNoDefender()` |
| `canAttackDespiteDefender` (Conditional-wrapped) | `CanAttackAsThoughNoDefenderEffect` | same interface + facet |
| `canAttackDespiteDefender` (floating) | `CanAttackAsThoughNoDefenderEffect` | same interface + facet |
| `canAttackDespiteDefender` (global) | `MatchingCreaturesCanAttackAsThoughNoDefenderEffect` | `NoDefenderAttackPermissionEffect` + `noDefenderAttackMatcher() != null` |
| `hasCantBeBlocked` | `CantBeBlockedEffect` | `BlockabilityRestrictionEffect` + `cantBeBlocked()` |
| `hasGlobalCantAttackOrBlockRestriction` | `MatchingCreaturesCantAttackOrBlockEffect` | `AttackOrBlockRestrictionEffect` + `globallyCantAttackOrBlock() != null` |
| `isCantBlockUnlessConditionUnmet` | `CantAttackOrBlockUnlessEffect` | `AttackOrBlockRestrictionEffect` + `cantAttackOrBlockUnless() != null` |
| `findBlockDenial` pair loop (x2) | `CanBeBlockedOnlyByFilterEffect`, `CantBeBlockedByCreaturesMatchingPredicateEffect` | one `BlockabilityRestrictionEffect` match + `blockableOnlyBy()` / `cantBeBlockedByCreaturesMatching()` facets |
| `buildAttackerFacts` unblockable (x3) | `CantBeBlockedIfDefenderControlsMatchingPermanentEffect`, `CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect`, `CantBeBlockedIfAttackingAloneEffect` | one `BlockabilityRestrictionEffect` match + `unblockableIfDefenderControls()` / `unblockableIfControllerCastHistoricSpellThisTurn()` / `unblockableWhileAttackingAlone()` |
| `buildAttackerFacts` pair collect (x2) | `CanBeBlockedOnlyByFilterEffect` OR `CantBeBlockedByCreaturesMatchingPredicateEffect` | `BlockabilityRestrictionEffect` + `blockableOnlyBy()!=null` / `cantBeBlockedByCreaturesMatching()!=null` |
| `buildBlockerFacts` cant-block | `CantBlockEffect` | `BlockingRestrictionEffect` + `cantBlock()` |

Two `instanceof` in the `findBlockDenial` pair loop collapsed into a single interface match, so the 15
concrete-`instanceof` removals cover the 13 logical checks listed above.

### Sites intentionally left concrete (3) — pinned by out-of-scope `BlockLegalityContext` typed collections

These collect a single record type into a `List<ConcreteRecord>` field that lives on
`BlockLegalityContext` (a separate class, outside this step's `GameQueryService`-only scope). Migrating
them requires re-typing those fields to the interface, so they stay concrete for now:
- `createBlockLegalityContext` → `MatchingCreaturesCantBlockMatchingCreaturesEffect` (into
  `List<MatchingCreaturesCantBlockMatchingCreaturesEffect> globalBlockRestrictions`).
- `buildBlockerFacts` → `CanBlockOnlyIfAttackerMatchesPredicateEffect` (into
  `List<CanBlockOnlyIfAttackerMatchesPredicateEffect> attackerFilterRestrictions`).
- `getAuraGrantedBlockingRestrictions` → `CanBeBlockedOnlyByFilterEffect` (into
  `List<CanBeBlockedOnlyByFilterEffect>`).
All three records DO implement their interface (facets exposed), so the pinned sites can be re-pointed
whenever `BlockLegalityContext`'s field types are widened.

`.class` / `::isInstance` sites are NOT counted by the ratchet (only `instanceof <ConcreteType>` is), so
the aura/granted-effect lookups (`hasAuraWithEffect(..., CantBeBlockedEffect.class)`, the `canBlock`
`CantBlockEffect.class::isInstance` stream, etc.) were left on the concrete class — they need a concrete
`Class<?>` argument and don't affect the count. Imports kept for those: `CanAttackAsThoughNoDefenderEffect`,
`CantBeBlockedEffect`, `CantBlockEffect`, plus the three pinned records. Seven now-unused record imports
were dropped and two javadoc `{@link}`s (retargeted off the removed `MatchingCreaturesCantAttackOrBlockEffect`
and `CantAttackOrBlockUnlessEffect`) reworded to plain phrases.

### Per-file violation drop

| File | Before | After | Delta |
|------|-------:|------:|------:|
| `service/battlefield/GameQueryService.java` | 48 | 33 | -15 |

Program total (ratchet-verified, regenerated via `python scripts/effect-coupling-audit.py`): **578 → 563**
(-15, all in `GameQueryService`). Both `effect-dispatch-baseline.txt` and `EFFECT_COUPLING_MATRIX.md`
refreshed; the baseline diff is the single line `GameQueryService=48` → `=33`.

### >>> For the NEXT session — interfaces available for the CombatBlockService / CombatAttackService re-point

All four interfaces are in `com.github.laxika.magicalvibes.model.effect` and fully implemented on the 13
records. `CombatBlockService` (block side) and `CombatAttackService` (attack side) still `instanceof`
these concrete records; re-point them with:

- **`BlockabilityRestrictionEffect`** (attacker evasion): `boolean cantBeBlocked()`,
  `PermanentPredicate unblockableIfDefenderControls()`, `boolean unblockableIfControllerCastHistoricSpellThisTurn()`,
  `boolean unblockableWhileAttackingAlone()`, `PermanentPredicate blockableOnlyBy()`,
  `String blockableOnlyByDescription()`, `PermanentPredicate cantBeBlockedByCreaturesMatching()`.
- **`BlockingRestrictionEffect`** (blocker restriction): `boolean cantBlock()`,
  `PermanentPredicate canBlockOnlyAttackersMatching()`, `String canBlockOnlyAttackersDescription()`,
  `PermanentPredicate globalCantBlockBlockerMatcher()`, `PermanentPredicate globalCantBlockAttackerMatcher()`,
  `String globalCantBlockDescription()`.
- **`AttackOrBlockRestrictionEffect`** (combined; serves BOTH services):
  `PermanentPredicate globallyCantAttackOrBlock()`, `Condition cantAttackOrBlockUnless()`,
  `String restrictionDescription()`.
- **`NoDefenderAttackPermissionEffect`** (attack-permission):
  `boolean grantsCarrierAttackAsThoughNoDefender()`, `PermanentPredicate noDefenderAttackMatcher()`.

When re-pointing, also widen the three `BlockLegalityContext` `List<ConcreteRecord>` fields noted above so
the last three `GameQueryService` pinned sites can move too.

### Tests run (all green)

- Card tests — one+ per record: `BlightedAgentTest` (CantBeBlocked), `DreadWarlockTest`
  (CanBeBlockedOnlyByFilter), `BogRatsTest` (CantBeBlockedByCreaturesMatchingPredicate),
  `ScrapdiverSerpentTest` (CantBeBlockedIfDefenderControls), `RelicRunnerTest`
  (CantBeBlockedIfControllerCastHistoric), `DreamProwlerTest` (CantBeBlockedIfAttackingAlone),
  `AesthirGliderTest` (CantBlock), `CloudDragonTest` (CanBlockOnlyIfAttackerMatches),
  `BoldwyrIntimidatorTest` (MatchingCreaturesCantBlockMatchingCreatures — pinned site),
  `KulrathKnightTest` + `LightOfDayTest` (MatchingCreaturesCantAttackOrBlock), `BlindSpotGiantTest`
  (CantAttackOrBlockUnless), `AnimateWallTest` + `SpireSerpentTest` (CanAttackAsThoughNoDefender),
  `RollingStonesTest` (MatchingCreaturesCanAttackAsThoughNoDefender).
- `SevenLayerTest` — CR 613 layer spec, all 100 pass. No expectation weakened.
- `EffectDispatchRatchetTest` — green after the baseline regen (48 → 33).

### Files changed

- New: `BlockabilityRestrictionEffect`, `BlockingRestrictionEffect`, `AttackOrBlockRestrictionEffect`,
  `NoDefenderAttackPermissionEffect` (domain `model/effect/`).
- 13 records implement their interface: `CantBeBlockedEffect`, `CanBeBlockedOnlyByFilterEffect`,
  `CantBeBlockedByCreaturesMatchingPredicateEffect`, `CantBeBlockedIfDefenderControlsMatchingPermanentEffect`,
  `CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect`, `CantBeBlockedIfAttackingAloneEffect`,
  `CantBlockEffect`, `CanBlockOnlyIfAttackerMatchesPredicateEffect`,
  `MatchingCreaturesCantBlockMatchingCreaturesEffect`, `MatchingCreaturesCantAttackOrBlockEffect`,
  `CantAttackOrBlockUnlessEffect`, `CanAttackAsThoughNoDefenderEffect`,
  `MatchingCreaturesCanAttackAsThoughNoDefenderEffect`.
- `magical-vibes-engine/.../service/battlefield/GameQueryService.java` (15 sites + imports/javadoc).
- `refactor-docs/effect-dispatch-baseline.txt`, `refactor-docs/EFFECT_COUPLING_MATRIX.md` (regenerated).
- `agent-docs/EFFECTS_QUICK_REFERENCE.md`, `agent-docs/EFFECTS_INDEX.md` (catalog entries).

**For the user:** run the full test suite and commit. Behaviour-preserving query refactor — no
game-rules change.

## Step 11 — Re-point combat services to the block/attack-restriction capability interfaces  (2026-07-14)

Follow-on to step 10: the four block/attack-restriction interfaces were created and `GameQueryService`
migrated last step; this step re-points the two combat consumers that still `instanceof`-ed the concrete
records — `CombatAttackService` (attack side) and the shared `CombatHelper` — plus the AI mirror in
`CombatSimulator`. No interface was extended: every fact these sites read was already exposed by step 10's
records.

### Sites migrated (4 concrete `instanceof` → interface)

| File | Method | Was `instanceof` | Now |
|------|--------|------------------|-----|
| `service/combat/CombatAttackService.java` | `isCantAttackUnlessConditionUnmet` | `CantAttackOrBlockUnlessEffect` | `AttackOrBlockRestrictionEffect` + `cantAttackOrBlockUnless()` (flows into the existing `condition != null` guard) |
| `service/combat/CombatAttackService.java` | `isCantAttackDueToGlobalRestriction` | `MatchingCreaturesCantAttackOrBlockEffect` | `AttackOrBlockRestrictionEffect` + `globallyCantAttackOrBlock() != null` guard, then reads the same predicate |
| `service/combat/CombatHelper.java` | `isCantBeBlockedDueToDefenderCondition` | `CantBeBlockedIfDefenderControlsMatchingPermanentEffect` | `BlockabilityRestrictionEffect` + `unblockableIfDefenderControls()` (null-facet `continue`) |
| `ai/CombatSimulator.java` | `isCantBeBlockedDueToDefenderCondition` (AI mirror) | `CantBeBlockedIfDefenderControlsMatchingPermanentEffect` | same as CombatHelper |

Behaviour-preservation notes:
- **Umbrella-with-defaults, guard on the facet.** Where a site sits in an `else if` chain against other
  concrete records (`isCantAttackDueToGlobalRestriction`), the interface match is `&&`-guarded on the
  facet being non-`null` so it fires only for the record that carries it (currently
  `MatchingCreaturesCantAttackOrBlockEffect`); the other `AttackOrBlockRestrictionEffect` record
  (`CantAttackOrBlockUnlessEffect`) returns `null` there and falls through exactly as before. Verified none
  of the sibling chain records (`CreaturesCantAttackUnlessPredicateEffect`,
  `CreaturesWithPowerGreaterThanAmountCantAttackEffect`, `ControlledCreaturesCantAttackUnlessPredicateEffect`,
  `CantAttackUnlessEffect`) implement the interface, so the chain semantics are unchanged.
- **`isCantAttackUnlessConditionUnmet`** needs no explicit guard: reading `cantAttackOrBlockUnless()`
  yields `null` for a `MatchingCreatures…` record, and the pre-existing `if (condition != null)` check
  already skips `null` — so the umbrella match is neutral.
- **Predicate hoisted, not re-derived per element.** The CombatHelper / CombatSimulator loops now read the
  facet into a local `PermanentPredicate` once and capture that in the `anyMatch` lambda, instead of the
  old per-element `restriction.defenderPermanentPredicate()` call. Strictly fewer accessor calls; no new
  allocations, no stream pipeline added. Performance invariant (these run in MCTS rollouts) respected —
  plain dispatch swap only.

### Sites intentionally left concrete

- **`CombatBlockService`** carries **zero** of the 13 records as `instanceof`: its block-legality checks
  (`canBlock`, `canBlockAttacker`, `hasCantBeBlocked`, `getBlockingIllegalityReason`,
  `createBlockLegalityContext`) all delegate to `GameQueryService`, which step 10 already migrated. Nothing
  to change; baseline count unchanged at 18 (its 18 `instanceof` are of other, out-of-family record types).
- **Three `GameQueryService` sites remain concrete** — the same pinned trio documented in step 10, blocked by
  `BlockLegalityContext`'s `List<ConcreteRecord>` fields (out of this step's scope):
  `createBlockLegalityContext` → `MatchingCreaturesCantBlockMatchingCreaturesEffect` (2503),
  `buildBlockerFacts` → `CanBlockOnlyIfAttackerMatchesPredicateEffect` (2691),
  `getAuraGrantedBlockingRestrictions` → `CanBeBlockedOnlyByFilterEffect` (2741). Widening those typed
  collections to the interfaces is a separate refactor.
- **`.class::isInstance` presence checks left concrete** (not counted by the ratchet, need a concrete
  `Class<?>`): CombatHelper's `isCantBeBlockedDueToHistoricCast` / `isCantBeBlockedDueToAttackingAlone` and
  CombatSimulator's `isCantBeBlockedDueToHistoricCast` still name
  `CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect` / `CantBeBlockedIfAttackingAloneEffect`. Those
  imports were kept.

### Per-file violation drop

| File | Before | After | Delta |
|------|-------:|------:|------:|
| `service/combat/CombatAttackService.java` | 15 | 13 | -2 |
| `service/combat/CombatHelper.java` | 1 | 0 (line removed) | -1 |
| `ai/CombatSimulator.java` | 1 | 0 (line removed) | -1 |

Program total (ratchet-verified, regenerated via `python scripts/effect-coupling-audit.py`): **563 → 559**
(-4). `python` (3.14.6) is on PATH this session, so `effect-dispatch-baseline.txt` and
`EFFECT_COUPLING_MATRIX.md` were both regenerated for real; the regenerated baseline is byte-identical to the
hand-edit the ratchet's three "Good news" drops asked for.

### Tests run (all green)

- Card tests: `BlindSpotGiantTest` (5, CantAttackOrBlockUnless — attack side migrated), `KulrathKnightTest`
  (5) + `LightOfDayTest` (5, MatchingCreaturesCantAttackOrBlock — attack side migrated), `ScrapdiverSerpentTest`
  (3, CantBeBlockedIfDefenderControls — CombatHelper + CombatSimulator path), `AesthirGliderTest` (2,
  CantBlock — GameQueryService block-side regression check).
- `SevenLayerTest` — CR 613 layer spec, all pass. No expectation weakened.
- `GameSimulatorTest` (`:magical-vibes-ai:test`) — AI/MCTS rollout regression, BUILD SUCCESSFUL. Combat runs
  inside rollouts, so this guards the performance-sensitive path.
- `EffectDispatchRatchetTest` — green after the baseline regen (three drops locked in).

### Files changed

- `magical-vibes-engine/.../service/combat/CombatAttackService.java` (2 sites + imports: added
  `AttackOrBlockRestrictionEffect`, dropped now-unused `CantAttackOrBlockUnlessEffect` and the duplicated
  `MatchingCreaturesCantAttackOrBlockEffect` imports).
- `magical-vibes-engine/.../service/combat/CombatHelper.java` (1 site + imports: added
  `BlockabilityRestrictionEffect` and `PermanentPredicate`, dropped
  `CantBeBlockedIfDefenderControlsMatchingPermanentEffect`).
- `magical-vibes-ai/.../ai/CombatSimulator.java` (1 site + imports: same swap as CombatHelper).
- `refactor-docs/effect-dispatch-baseline.txt`, `refactor-docs/EFFECT_COUPLING_MATRIX.md` (regenerated).
- No `agent-docs/` change: no interface extended (all facts already exposed by step 10).

**For the user:** run the full test suite and commit. Behaviour-preserving dispatch swap — no game-rules
change. The three pinned `GameQueryService` block sites plus the `BlockLegalityContext` typed-collection
widening remain for a future step.

## Step 12 — One capability interface for combat-damage trigger targeting  (2026-07-14)

Collapsed the combat-damage-to-player **trigger-classification** chains in `CombatDamageService` (the
three `if / else if` blocks that decided how a fired `ON_COMBAT_DAMAGE_TO_PLAYER` trigger's `StackEntry`
should be populated) into a single capability-interface match + enum switch. The engine now asks each
effect "what trigger context do you need?" instead of `instanceof`-ing 17 concrete record types.

### Buckets found (the code, not the prompt, was authoritative)

The chain assigned every effect to one of four `StackEntry` shapes:

| Bucket | `StackEntry` shape (as built) | Enum constant |
|--------|-------------------------------|---------------|
| A | `(…, List.of(effect), xValue=damageDealt, targetId=defenderId, source=null)` | `DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT` |
| B | `(…, List.of(effect), targetId=null, source=creature.getId())` | `SOURCE_SELF` |
| C | `(…, List.of(effect), targetId=defenderId, source=creature.getId())` | `DAMAGED_PLAYER` |
| default | `(…, List.of(effect))` (plain, no target/source) | *(interface not implemented, or returns `null`)* |

### Interface design

New domain effect-package interface `CombatDamageTriggerContextEffect extends CardEffect` with nested
`enum TriggerContext { DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT, SOURCE_SELF, DAMAGED_PLAYER }` and one method
`TriggerContext combatDamageTriggerContext()`. Pure function of record components. A `null` result means
"needs the plain default entry" — identical to an effect that doesn't implement the interface — so the
switch's `else` branch handles both non-implementers and `null`-returners. The engine site became:

```java
CombatDamageTriggerContextEffect.TriggerContext triggerContext =
        effect instanceof CombatDamageTriggerContextEffect contextEffect
                ? contextEffect.combatDamageTriggerContext() : null;
if (triggerContext == …DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT) { … }
else if (triggerContext == …SOURCE_SELF) { … }
else if (triggerContext == …DAMAGED_PLAYER) { … }
else { /* plain entry */ }
```

Each `StackEntry` constructor call is byte-for-byte the same as before (same literal args, so overload
resolution is unchanged) — only the *predicate* deciding which one runs moved into the records.

### Records migrated (17)

- **Bucket A → `DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT`** (3, unconditional):
  `ReturnPermanentsOnCombatDamageToPlayerEffect`, `DealDamageToEachCreatureDamagedPlayerControlsEffect`,
  `LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect`.
- **Bucket B → `SOURCE_SELF`** (3, unconditional): `PutCountersOnSourceEffect`,
  `RemoveAllCountersFromSelfEffect`, `ExploreEffect`.
- **Bucket C → `DAMAGED_PLAYER`** (11): unconditional — `ExileTopCardsRepeatOnDuplicateEffect`,
  `TargetPlayerRandomDiscardOrControllerDrawsEffect`, `RevealRandomCardFromTargetPlayerHandEffect`,
  `SphinxAmbassadorEffect`, `TargetPlayerExilesFromHandEffect`, `ChooseCardsFromTargetHandEffect`,
  `SkipNextCombatPhaseEffect`, `TargetPlayerCantGainLifeRestOfGameEffect`; **recipient-conditional** —
  `DiscardEffect` (`recipient == TARGET_PLAYER ? DAMAGED_PLAYER : null`), `MillEffect` (same on
  `MillRecipient.TARGET_PLAYER`), `DealDamageToPlayersEffect` (same on `DamageRecipient.TARGET_PLAYER`).
  The three conditional records return `null` for their other recipients, so those instances fall to the
  default entry exactly as the old per-recipient `instanceof … && recipient() == …` guards did.

`DealDamageToPlayersEffect` now implements `DamageDealingEffect, CombatDamageTriggerContextEffect` (added
the second interface); the rest previously implemented `CardEffect` directly.

### Types left concrete in `CombatDamageService` (deliberate, out of this step's scope)

- **`MayEffect`-wrapped bespoke flows** (lines ~896-921): the may-ability queueing with its own
  target-validity pre-checks (`ExilePermanentDamagedPlayerControlsEffect`, `DrawCardEffect` event-value
  wiring) — a different mechanism (`queueMayAbility`), not `StackEntry` classification.
- **`DestroyPermanentDamagedPlayerControlsEffect` / `SacrificePermanentDamagedPlayerControlsEffect`**
  (lines ~923-985): each has a `minimumDamage` gate + valid-target scan + a distinct non-targeting
  `StackEntry`; bespoke, left concrete as instructed.
- **`instanceof DiscardEffect` at the event-value line** (post-classification `se.setEventValue(damageDealt)`
  for "discards that many cards", Needle Specter): a *different* fact (does this effect read combat damage
  as an event value), not trigger context — intentionally not folded into this interface. Kept concrete.
- **`instanceof PutCountersOnSourceEffect` in `checkPlayerAttachedCurseCombatDamageTriggers`** (curse path,
  ~line 1096): separate method with its own `StackEntry` shape; not part of the migrated chain. Kept.
- Lifelink / `AllyCombatDamageTriggerEffect` / `ReplaceCombatDamageWithMillEffect` / `AssignCombatDamage*`
  and all damage-to-creature flows: untouched (survivors for later passes, per scope limit).

### Per-file violation drop

| File | Before | After | Delta |
|------|-------:|------:|------:|
| `service/combat/CombatDamageService.java` | 40 | 23 | -17 |

Program total (ratchet sum): **559 → 542** (-17). All 17 removed `instanceof` were the trigger-classification
chain; the two surviving migrated-type `instanceof` in the file (`DiscardEffect` event-value line,
`PutCountersOnSourceEffect` curse path) are unrelated concerns documented above. `effect-dispatch-baseline.txt`
and `EFFECT_COUPLING_MATRIX.md` regenerated via `python scripts/effect-coupling-audit.py` (Python 3.14.6 on
PATH this session); the regenerated baseline is byte-identical to the ratchet's requested drop (40→23).

### Tests run (all green)

- Card tests covering all three buckets: `BalefireDragonTest` (5 — bucket A,
  `DealDamageToEachCreatureDamagedPlayerControls`), `EmperorsVanguardTest` (7 — bucket B, combat-damage
  `Explore`), `StigmaLasherTest` (3 — bucket C, `TargetPlayerCantGainLife`), `MerfolkSpyTest` (7 — bucket C,
  `RevealRandomCardFromTargetPlayerHand`), `ScalpelexisTest` (10 — bucket C,
  `ExileTopCardsRepeatOnDuplicate`), `BlindingAngelTest` (4 — bucket C, `SkipNextCombatPhase`).
- `GameSimulatorTest` (`:magical-vibes-ai:test`) — AI/MCTS rollout regression, BUILD SUCCESSFUL. Combat
  damage runs inside rollouts, so this guards the performance-sensitive path (plain dispatch swap only, no
  new allocations/streams).
- `EffectDispatchRatchetTest` — green after baseline regen.

### Files changed

- **New:** `magical-vibes-domain/.../model/effect/CombatDamageTriggerContextEffect.java` (interface + enum).
- **17 records** in `magical-vibes-domain/.../model/effect/` re-pointed to implement the interface (listed
  above); each adds one pure `combatDamageTriggerContext()` method.
- `magical-vibes-engine/.../service/combat/CombatDamageService.java` — 3-chain block replaced with the
  interface match + switch; 15 now-unused effect imports removed, `CombatDamageTriggerContextEffect` added
  (`DiscardEffect`, `PutCountersOnSourceEffect`, `DealDamageToPlayersEffect`, `DamageRecipient`,
  `ReturnPermanentsOnCombatDamageToPlayerEffect` imports kept — still referenced by the surviving sites).
- `refactor-docs/effect-dispatch-baseline.txt`, `refactor-docs/EFFECT_COUPLING_MATRIX.md` (regenerated).
- `agent-docs/EFFECTS_QUICK_REFERENCE.md`, `agent-docs/EFFECTS_INDEX.md` (catalog entries).

**For the user:** run the full test suite and commit. Behaviour-preserving refactor — no game-rules change.
The `MayEffect`-wrapped and destroy/sacrifice "damaged player controls" bespoke flows remain concrete for a
later pass.

## Step 13 — Audit the damage/life/mana/token MULTIPLIER family; migrate only the global damage doubler  (2026-07-14)

Audited the seven multiplier records. Grepped `magical-vibes-engine/src/main/java` and
`magical-vibes-ai/src/main/java` for `instanceof <RecordName>`, excluding the exempt zones
(`service/effect/**`, `service/validate/**`). The AI module has **zero** multiplier `instanceof`. Every
non-exempt site lives in `GameQueryService` except one: a duplicate of the global-damage loop in
`MultiPermanentChoiceHandlerService` (Myr Battlesphere's ping). Each `GameQueryService` site is its own
dedicated single-record method (`getDamageMultiplier`, `getEnchantedPlayerDamageMultiplier`,
`getTokenMultiplier`, `getControllerDamageMultiplier`, `getEquippedCreatureCombatDamageMultiplier`,
`lifeGainMultiplier`, `manaProductionMultiplier`) — **no single loop matches 2+ of these records**, so the
"collapse several branches" arm of the rule never fires.

### Audit table

| Record | Non-exempt sites | Files | Decision | Reason |
|--------|------------------|------:|----------|--------|
| `DoubleDamageEffect` | `GameQueryService.getDamageMultiplier` (2792), `MultiPermanentChoiceHandlerService` (818) | **2** | **MIGRATE** | Same global-doubler check duplicated across two files (2+ non-exempt files). |
| `DoubleDamageToEnchantedPlayerEffect` | `GameQueryService.getEnchantedPlayerDamageMultiplier` (2811) | 1 | LEAVE | Single dedicated check; selects only auras enchanting the damaged player. An interface renames, doesn't reduce. |
| `DoubleControllerDamageEffect` | `GameQueryService.getControllerDamageMultiplier` (2887) | 1 | LEAVE | Single dedicated check with its own `stackFilter` / `appliesToCombatDamage` branching; distinct selection semantics. |
| `DoubleEquippedCreatureCombatDamageEffect` | `GameQueryService.getEquippedCreatureCombatDamageMultiplier` (2936) | 1 | LEAVE | Single dedicated check; selects only equipment attached to the given creature. |
| `DoubleLifeGainEffect` | `GameQueryService.lifeGainMultiplier` (473) | 1 | LEAVE | Single dedicated check; per-player life-gain doubler. |
| `ManaReflectionEffect` | `GameQueryService.manaProductionMultiplier` (491) | 1 | LEAVE | Single dedicated check; per-player mana doubler. |
| `MultiplyTokenCreationEffect` | `GameQueryService.getTokenMultiplier` (2840) | 1 | LEAVE | Single dedicated check; already reads `.multiplier()` (not a bare `instanceof`), per-controller token scaling. |

**Why the four damage doublers can't share one interface:** they are checked in *separate* methods precisely
because each selects a *different* permanent subset (global / auras on a player / same-controller with a
stack-filter / equipment on a creature). A shared marker read by `getDamageMultiplier` would wrongly count the
selective doublers and change damage results. So the migrated interface is scoped to the *global,
unconditional* doubler only.

### Migration done

New domain effect-package interface
`magical-vibes-domain/.../model/effect/GlobalDamageMultiplyingEffect.java` (`extends CardEffect`, one pure
fact `int damageMultiplierFactor()`). `DoubleDamageEffect` now implements it (`return 2`) instead of
`CardEffect`. Both consumer sites read the interface:

- `GameQueryService.getDamageMultiplier` — `instanceof GlobalDamageMultiplyingEffect e` → `*= e.damageMultiplierFactor()`.
- `MultiPermanentChoiceHandlerService` (Myr Battlesphere) — same swap; the `* 2` magic number now comes from the record.

Both loops are otherwise byte-for-byte unchanged (same iteration, same multiplicative stacking), so damage
results are identical. Imports and `{@link}` javadoc in both files re-pointed to the interface.

### Per-file violation drop

| File | Before | After | Delta |
|------|-------:|------:|------:|
| `service/battlefield/GameQueryService.java` | 33 | 32 | -1 |
| `service/input/MultiPermanentChoiceHandlerService.java` | 1 | 0 | -1 |

Program total (ratchet sum): **542 → 540** (-2). Baseline hand-edited (Python not used this step to avoid
recomputing against the earlier-step dirty tree): `GameQueryService` set to 32, the now-zero
`MultiPermanentChoiceHandlerService` line deleted. `EFFECT_COUPLING_MATRIX.md` left as-is for the same reason
(it is informational and not read by the ratchet); regenerate it alongside the next full `python
scripts/effect-coupling-audit.py` run.

### Tests run (all green)

- `FurnaceOfRathTest` (20 — the sole `DoubleDamageEffect` card, exercises `getDamageMultiplier`).
- `MyrBattlesphereTest` (9 — exercises the migrated `MultiPermanentChoiceHandlerService` damage path).
- `SevenLayerTest` (100 — CR 613 layer regression, unaffected but confirms no continuous-effect fallout).
- `EffectDispatchRatchetTest` — green after the baseline hand-edit.

### Files changed

- **New:** `magical-vibes-domain/.../model/effect/GlobalDamageMultiplyingEffect.java`.
- `magical-vibes-domain/.../model/effect/DoubleDamageEffect.java` — implements the interface, adds
  `damageMultiplierFactor()`.
- `magical-vibes-engine/.../service/battlefield/GameQueryService.java`,
  `magical-vibes-engine/.../service/input/MultiPermanentChoiceHandlerService.java` — interface swap + import/javadoc.
- `refactor-docs/effect-dispatch-baseline.txt` (hand-edited, -2).
- `agent-docs/EFFECTS_QUICK_REFERENCE.md`, `agent-docs/EFFECTS_INDEX.md` (catalog entry).

**For the user:** run the full test suite and commit. Behaviour-preserving refactor — no game-rules change.
The six selective/per-player multiplier records stay concrete by design (single dedicated query methods);
migrating them would rename coupling without reducing it.

## Step 14 — Unify the "counter unless …" family; collapse the clash-outcome pair; audit the rest  (2026-07-14)

Trigger-collection cleanup. Migrated two genuine multi-implementor families to capability interfaces
and audited every other non-exempt effect-type `instanceof` in `TriggerCollectionService` and
`StepTriggerService`. The remaining sites are single-implementor special cases and stay concrete.

### Part 1 — the counter-unless family

New descriptive interface `magical-vibes-domain/.../model/effect/CounterUnlessEffect.java`
(`extends CardEffect`, nested `RansomKind { PAY_MANA, DISCARD_CARD }`, two pure facts
`ransomKind()` + `ransomMagnitude()`). Implemented by both records:

- `CounterUnlessPaysEffect` — now `implements CounterSpellingEffect, CounterUnlessEffect`;
  `ransomKind()` = `PAY_MANA`, `ransomMagnitude()` = `amount()`. (Keeps `CounterSpellingEffect` so the
  AI instant classifier still sees it as a counterspell.)
- `CounterUnlessDiscardsEffect` — now `implements CounterUnlessEffect` (was bare `CardEffect`);
  `ransomKind()` = `DISCARD_CARD`, `ransomMagnitude()` = 1. (Deliberately NOT `CounterSpellingEffect`,
  preserving its prior non-counterspell classification.)

Recognition sites rewritten to the interface (the pay/discard choice-flow orchestration itself is
unchanged — it now switches on `ransomKind()` instead of the concrete types):

- `TriggerCollectionService` — Chancellor opening-hand-reveal match (was `CounterUnlessPaysEffect`),
  the "becomes target of opponent spell" branch (the two per-kind branches collapsed into one shared
  stack-entry build + a `ransomKind()` switch for the log wording, reading `ransomMagnitude()` where it
  read `amount()`), and the negative filter (`!(e instanceof CounterUnlessPaysEffect) && !(e instanceof
  CounterUnlessDiscardsEffect)` → `!(e instanceof CounterUnlessEffect)`).
- `MayAbilityHandlerService` — the two routing `anyMatch` checks collapsed into one interface filter +
  `ransomKind()` switch dispatching to the same pay/discard handlers.
- `MayPenaltyChoiceHandlerService` — the pay handler filters on `instanceof CounterUnlessEffect ce &&
  ce.ransomKind()==PAY_MANA` then casts to `CounterUnlessPaysEffect` for its per-kind fields (`amount`,
  `exileIfCountered`, `onNotPaidEffects`); the discard handler's presence check filters on the interface
  + `DISCARD_CARD`. (Plain casts aren't ratchet violations, so keeping the concrete cast in the kept
  per-kind branch is fine.)

### Part 2 — bounded audit of the two trigger files

Decision rule applied: migrate a type only if it collapses a genuine multi-implementor family sharing a
loop (or a clean cross-file family). A single concrete record checked on its own — even across several
files — is NOT migrated: extracting an interface with one implementor just renames the coupling, it
doesn't collapse any multi-type branch. (Matches on condition types — Metalcraft/Morbid/Raid/… — and the
exempt structural wrappers `ConditionalEffect`/`MayEffect`/… are not effect-type violations and are
excluded, as are `TargetFilter` matches.)

| File | Type(s) | Site(s) | Other non-exempt files | Impls | Decision | Reason |
|------|---------|---------|------------------------|------:|----------|--------|
| TriggerCollectionService | `IfWonClashEffect` + `IfLostClashEffect` | 1478/1480 | — | 2 | **MIGRATE** | Two wrapper records in one `if/else-if` clash loop; both just carry `wrapped()` gated on the outcome. Collapsed to `ClashOutcomeConditionalEffect` (`wrapped()` + `appliesOnWin()`): `if (won == clash.appliesOnWin()) add(wrapped)`. Clean fact collapse. |
| TriggerCollectionService | `ReflectAllyDamageToDamagedCreatureControllerEffect` + `DamageDamagedCreatureControllerAndSelfEffect` | 911/934 | — | 1 each | LEAVE | Share a loop, but the two branches build **structurally different** stack entries (source-filter predicate + a single `damage` reflect vs. two fixed damage amounts gated on a self-fire check). An interface can't express both without per-type reconstruction — construction-shaped, not a fact read. |
| TriggerCollectionService | `SpellCastTriggerEffect` | 145 | — | 1 | LEAVE | Single-site special case. |
| TriggerCollectionService | `ExileTargetOnControllerSpellCastEffect` | 179 | — | 1 | LEAVE | Single-site. |
| TriggerCollectionService | `CopyThisSpellIfConditionEffect` | 298 | — | 1 | LEAVE | Single-site. |
| TriggerCollectionService | `EnterBattlefieldOnDiscardEffect` | 376 (neg filter) | CardChoiceHandlerService:984 | 1 | LEAVE | Technically hits the "2+ files" arm, but it's a **single concrete record** checked for mere presence/absence — a marker interface would have exactly one implementor and collapse no multi-type branch (rename only). Its 2nd site is outside the trigger-collection area. Deferred unless the program later decides single-impl markers pay off. |
| TriggerCollectionService | `CopyControllerActivatedAbilityTriggerEffect` | 1197 | — | 1 | LEAVE | Single-site. |
| TriggerCollectionService | `TriggeringPermanentConditionalEffect` | 1026/1055/1119/1157/2371 | — | 1 | LEAVE | One wrapper type across five dedicated methods; no multi-type chain to collapse. |
| TriggerCollectionService | `EnterCreatureConditionalEffect` | 2255 | — | 1 | LEAVE | Single wrapper type. |
| TriggerCollectionService | `TriggeringCardConditionalEffect` | 2348/2364 | — | 1 | LEAVE | Single wrapper type across two dedicated methods. |
| StepTriggerService | `ExileGraveyardCardsEffect` | 690, 2300 | TriggeredAbilityQueueService:89, ValidTargetService (×3), TargetLegalityService:186 | 1 | LEAVE | Hits "2+ files" heavily, but is a **single concrete record** whose own fields (`scope()`, `canTargetGraveyard()`) are read — an interface would 1:1-rename the type, not collapse a family. Most of its sites live in the target-validation subsystem (`service/target`), whose sanctioned home is `@ValidatesTarget` validators — out of this step's scope. |
| StepTriggerService | `SurveilEffect` | 453 | MayAbilityHandlerService:279 | 1 | LEAVE | 2 files but a single record checked for presence — marker rename, no family. |
| StepTriggerService | `ExchangeControlOfTargetPermanentsEffect` | 260/261 | — | 1 | LEAVE | Single type (direct + `MayEffect`-wrapped in the same expression). |
| StepTriggerService | `MayRevealSubtypeFromHandEffect` | 271 | — | 1 | LEAVE | Single-site. |
| StepTriggerService | `BecomeCopyOfTargetCreatureEffect` | 279 | — | 1 | LEAVE | Single-site. |
| StepTriggerService | `DestroyOneOfTargetsAtRandomEffect` | 299 | — | 1 | LEAVE | Single-site. |
| StepTriggerService | `WinGameIfCreaturesInGraveyardEffect` | 471 | — | 1 | LEAVE | Single-site. |
| StepTriggerService | `DealDamageIfFewCardsInHandEffect` | 609 | — | 1 | LEAVE | Single-site. |
| StepTriggerService | `EnchantedCreatureControllerLosesLifeEffect` | 656 | — | 1 | LEAVE | Single-site. |
| StepTriggerService | `LeylineStartOnBattlefieldEffect` | 1117 | — | 1 | LEAVE | Single-site (`MayEffect`-wrapped). |
| StepTriggerService | `SkipDrawStepEffect` | 1185 | — | 1 | LEAVE | Single-site. |
| StepTriggerService | `DrawCardForTargetPlayerEffect` | 1232 | — | 1 | LEAVE | Single-site. |
| StepTriggerService | `DealDamageIfDidntCastSpellThisTurnEffect` | 1734 | — | 1 | LEAVE | Single-site. |
| StepTriggerService | `SacrificeSelfAndReturnCardsExiledWithSourceEffect` | 1755 | — | 1 | LEAVE | Single-site. |
| StepTriggerService | `DestroyRandomOpponentPermanentWithCounterEffect` | 1951 | — | 1 | LEAVE | Single-site. |
| StepTriggerService | `GainControlIfSubtypesDealtCombatDamageEffect` | 2034 | — | 1 | LEAVE | Single-site. |

**Conclusion for future sessions:** apart from the two families migrated this step, the remaining
trigger-collection dispatch is interaction/construction-shaped or single-implementor. Do NOT re-litigate
it — extracting an interface for any single concrete record (including the cross-file
`EnterBattlefieldOnDiscardEffect` / `ExileGraveyardCardsEffect` / `SurveilEffect`) renames coupling
without collapsing a multi-type branch, and the `ExileGraveyardCardsEffect` target-validation sites
belong to the `@ValidatesTarget` subsystem, not a capability interface.

### Per-file violation drop

| File | Before | After | Delta |
|------|-------:|------:|------:|
| `service/trigger/TriggerCollectionService.java` | 21 | 14 | -7 |
| `service/input/MayAbilityHandlerService.java` | 61 | 59 | -2 |
| `service/input/MayPenaltyChoiceHandlerService.java` | 12 | 10 | -2 |

Program total (ratchet sum): **540 → 529** (-11). Working tree was clean apart from this step, so the
baseline and `EFFECT_COUPLING_MATRIX.md` were regenerated with `python scripts/effect-coupling-audit.py`
(Python 3.14.6 on PATH); the regenerated baseline diff touches exactly these three files.

### Tests run (all green)

- `ManaLeakTest` (7 — counter-unless-pays), `ForumNecroscribeTest` (6 — counter-unless-discards / Ward),
  `ChancellorOfTheAnnexTest` (15 — opening-hand-reveal counter path), `SpiketailHatchlingTest` (10 —
  becomes-target-of-opponent-spell counter path), `FrostTitanTest` (may-ability counter path),
  `RebellionOfTheFlamekinTest` (4 — clash `IfWon`/`IfLost`), `EntanglingTrapTest` (4 — targeting clash).
- `EffectDispatchRatchetTest` — green after baseline regen.

### Files changed

- **New:** `magical-vibes-domain/.../model/effect/CounterUnlessEffect.java`,
  `magical-vibes-domain/.../model/effect/ClashOutcomeConditionalEffect.java`.
- `CounterUnlessPaysEffect`, `CounterUnlessDiscardsEffect`, `IfWonClashEffect`, `IfLostClashEffect` —
  implement the new interfaces (pure facts only).
- `service/trigger/TriggerCollectionService.java`, `service/input/MayAbilityHandlerService.java`,
  `service/input/MayPenaltyChoiceHandlerService.java` — recognition sites re-pointed; imports updated.
- `refactor-docs/effect-dispatch-baseline.txt`, `refactor-docs/EFFECT_COUPLING_MATRIX.md` (regenerated).
- `agent-docs/EFFECTS_QUICK_REFERENCE.md`, `agent-docs/EFFECTS_INDEX.md` (catalog entries).

**For the user:** run the full test suite and commit. Behaviour-preserving refactor — no game-rules change.

## Step 15 — Build the `mayfx` "you may …" handler registry; migrate the first half of `MayAbilityHandlerService`  (2026-07-14)

`MayAbilityHandlerService.handleMayAbilityChosen` was one long fixed-order chain: for each pending
may-ability it scanned `ability.effects()` for one concrete effect type after another and routed to a
bespoke accept/decline block. This step built the class-keyed registry that mirrors `normalfx`/`staticfx`
and migrated the first 15 effect-type blocks into it. The remaining chain is untouched and unmigrated
types fall through to it.

### Registry design (mirrors `normalfx` exactly)

- **`service/effect/mayfx/MayEffectHandlerBean`** — interface: `Class<? extends CardEffect> handledEffect()`
  + `void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability)`.
  (Uniform signature — the old branches only ever needed those four values; handlers that want a typed
  view of their effect re-extract it from `ability.effects()` with the same `filter/map/findFirst` the
  branch used.)
- **`service/effect/MayEffectHandlerRegistry`** — class-keyed `LinkedHashMap`, same shape as
  `EffectHandlerRegistry` (`register` / `getHandler(effect)` by `effect.getClass()`).
- **`GameEngineConfig`** — new `@Bean mayEffectHandlerRegistry()`; `afterSingletonsInstantiated` collects
  all `MayEffectHandlerBean`s via `getBeansOfType` and registers them (identical mechanism to the normal
  handlers). Startup log line now reports the may-handler count too.
- **Dispatch** in `handleMayAbilityChosen`: after the three non-effect preambles, a loop
  `for (CardEffect effect : ability.effects()) { var h = registry.getHandler(effect); if (h != null) { h.handle(...); return; } }`.
  Unmigrated types find no handler and fall through to the untouched chain below.

### Preambles that stay in the service (out of scope, run BEFORE dispatch)

1. Pile-separation completion (`completeCardPileSeparationStep2` / `completePileSeparationStep2`).
2. `resolvingMayEffectFromStack` resolution-time path (`handleResolutionTimeMayChoice`).
3. Pending equipment-attach (`handleEquipmentAttachChoice`).

### Ordering-hazard decision (step 2/3)

The old chain tests effect types in **fixed code order**; the new loop dispatches in **`effects()` list
order**. These differ only if a single may-ability's top-level `effects()` list contains two
registered types. Grepped all 16 first-half types across `magical-vibes-card/src/main/java`: every hit is
a lone type (import + one usage). The one file with two of them — `LeafCrownedElder` (`KinshipEffect` +
`RevealTopCardMayPlayFreeOrExileEffect`) — nests the second **inside** `KinshipEffect.revealEffects()`, so
at the top level `effects()` is `[KinshipEffect]` only. **No top-level co-occurrence exists**, so list
order == code order for the migrated set and **no explicit priority was needed**. Documented here so the
next session re-checks before adding more.

**Block 5 deliberately NOT migrated** (`CastTopOfLibraryWithoutPayingManaCostEffect`, Galvanoth): its
branch has an extra runtime guard
(`castFromLibEffect.castableTypes().contains(ability.sourceCard().getType())`) and **falls through to the
generic handling when the guard fails**. A plain class-keyed handler can't express "matched the type but
decline to handle, keep going," so migrating it would need either a `boolean`/tri-state return from
`handle` or the handler to replicate the generic stack-entry fallthrough. Left in the chain verbatim; the
dispatch loop simply doesn't find a handler for it and falls through as before (`GalvanothTest` green).

### Full ordered block list (next session resumes from here)

Migrated → `mayfx` bean; Remaining → still in the in-service chain.

- [x] 1. `RevealSubtypeOrEntersTappedEffect` → `RevealSubtypeOrEntersTappedHandler` (inline)
- [x] 2. `MayCastFromHandWithoutPayingManaCostEffect` → `MayCastFromHandWithoutPayingHandler`
- [x] 3. `ParadigmMayCastFromExileEffect` → `ParadigmMayCastFromExileHandler` (@Lazy ParadigmService)
- [x] 4. `MayPlayExiledCounteredCardEffect` → `MayPlayExiledCounteredCardHandler` (inline)
- [ ] 5. `CastTopOfLibraryWithoutPayingManaCostEffect` — **REMAINING** (conditional guard, see above)
- [x] 6. `RevealTopCardMayPlayFreeOrExileEffect` → `RevealTopCardMayPlayFreeOrExileHandler`
- [x] 7. `SurveilEffect` → `SurveilMayGraveyardHandler`
- [x] 8. `LookAtTargetPlayerTopCardMayGraveyardEffect` → `LookAtTargetPlayerTopCardMayGraveyardHandler`
- [x] 9. `ExploreEffect` → `ExploreMayGraveyardHandler`
- [x] 10. `RevealTopCardCreatureToBattlefieldOrMayBottomEffect` → `RevealTopCardCreatureToBattlefieldOrMayBottomHandler`
- [x] 11. `LookAtTopCardMayRevealTypeTransformEffect` → `LookAtTopCardMayRevealTypeTransformHandler` (inline)
- [x] 12. `KinshipEffect` → `KinshipHandler` (inline)
- [x] 13. `CastTargetInstantOrSorceryFromGraveyardEffect` → `CastTargetInstantOrSorceryFromGraveyardHandler`
- [x] 14. `PlayImprintedCardWithoutPayingManaCostEffect` → `PlayImprintedCardWithoutPayingHandler`
- [x] 15. `PlayTargetCardFromGraveyardWithoutPayingManaCostEffect` → `PlayTargetCardFromGraveyardWithoutPayingHandler`
- [x] 16. `MayNotUntapDuringUntapStepEffect` → `MayNotUntapDuringUntapStepHandler`
- [ ] 17. `LeylineStartOnBattlefieldEffect` — REMAINING (`handleLeylineChoice`)
- [ ] 18. `RegisterDelayedCounterTriggerEffect` — REMAINING (`handleOpeningHandDelayedCounterTrigger`; also used in the resolution-time preamble — check both)
- [ ] 19. `RegisterDelayedManaTriggerEffect` — REMAINING (`handleOpeningHandDelayedManaTrigger`; also resolution-time preamble)
- [ ] 20. `CounterUnlessEffect` (interface) — REMAINING (`switch(ransomKind())` → pay/discard handlers)
- [ ] 21. `LoseLifeUnlessDiscardEffect` — REMAINING
- [ ] 22. `LoseLifeUnlessPaysEffect` — REMAINING
- [ ] 23. `DiscardHandUnlessPaysLifeEffect` — REMAINING
- [ ] 24. `EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect` — REMAINING (`tariffSupport`)
- [ ] 25. `OpponentMayReturnExiledCardOrDrawEffect` — REMAINING
- [ ] 26. `LibraryOfLatNamEffect` — REMAINING
- [ ] 27. `DiscardUnlessExileCardFromGraveyardEffect` — REMAINING
- [ ] 28. `SacrificeUnlessDiscardCardTypeEffect` — REMAINING
- [ ] 29. `SacrificeUnlessReturnOwnPermanentTypeToHandEffect` — REMAINING
- [ ] 30. `ForcedCostOrElseEffect` — REMAINING
- [ ] 31. `ReplaceSingleDrawEffect` — REMAINING
- [ ] 32. `ChooseNewTargetsForTargetSpellEffect` — REMAINING (also resolution-time preamble — check both)
- [ ] 33. `CopySpellEffect` — REMAINING
- [ ] 34. `CopyActivatedAbilityRetargetEffect` — REMAINING
- [ ] 35. `BecomeCopyOfTargetCreatureEffect` — REMAINING
- [ ] 36. `CopyPermanentOnEnterEffect` — REMAINING
- [ ] 37. `SacrificeArtifactThenDealDividedDamageEffect` — REMAINING
- [ ] 38. `SphinxAmbassadorPutOnBattlefieldEffect` — REMAINING
- [ ] 39. `ShuffleLibraryEffect` — REMAINING (inline)

**Below block 39 is NOT a routing chain** — it is the generic accept path: a mana-payment preamble, then
targeted/untargeted stack-entry construction. Its two `instanceof` clusters (`needsSelfTarget` over
`PutCountersOnSelfEffect` / `AnimatePermanentsEffect(SELF)` / `BoostSelfEffect` / `ImprintDyingCreatureEffect`
/ `ExileFromHandToImprintEffect` / `ReturnDyingCreatureToBattlefieldAndAttachSourceEffect` /
`BecomeCopyOfDyingCreatureEffect`, and `needsEnteringTarget` over `CreateTokenCopyOfTargetPermanentEffect`)
plus the resolution-time helpers (`extractInnerEffect`, `setUpSelfTargetIfNeeded`,
`handleGraveyardTargetedMayAbility`, `handleResolutionTimeGraveyardTargetSelection`, which read
`ExileTargetCardFromGraveyard*Effect` fields) are **construction-shaped**, not bespoke accept/decline
routing — they likely resolve via capability interfaces (`needsSelfTarget()` / entering-target facts)
rather than `mayfx` beans. Decide their fate as a separate sub-step.

### Circular-dependency note

`MayAbilityHandlerService` no longer injects `ParadigmService` or `ExileFreeCastSupport` (their only
callers, blocks 3 and 4, moved out); both fields + ctor params and the now-unused `@Lazy` import were
removed. The `@Lazy` edge moved to `ParadigmMayCastFromExileHandler` (explicit ctor, `@Lazy ParadigmService`),
preserving the original cycle break. `mayfx` beans that orchestrate a sub-service inject it directly; the
registry itself is populated post-construction (`getBeansOfType`), so it adds no ctor edge back into
`MayAbilityHandlerService`. (`turnProgressionService` was already an unused field before this step — left
as-is, out of scope.)

### Per-file violation drop

| File | Before | After | Delta |
|------|-------:|------:|------:|
| `service/input/MayAbilityHandlerService.java` | 59 | 44 | -15 |

Program total (ratchet sum): **529 → 514** (-15). The 15 migrated blocks moved into `mayfx` beans under
`service/effect/**`, which is an EXEMPT zone, so they add zero counted violations. Baseline + matrix
regenerated with `python scripts/effect-coupling-audit.py`; `git diff` of the baseline touches exactly one
line (`MayAbilityHandlerService` 59→44, a pure drop).

### Tests run (all green)

`DelverOfSecretsTest` (7, block 11), `AncientAmphitheaterTest` (5, block 1), `CounterlashTest` (12, block 2),
`GuileTest` (7, block 4), `DjinnOfWishesTest` (9, block 6), `SearchForAzcantaTest` (8, block 7),
`EyeSpyTest` (4, block 8), `DeadeyeTrackerTest` (10, block 9), `LurkingPredatorsTest` (10, block 10),
`WolfSkullShamanTest` (4, block 12), `LeafCrownedElderTest` (4, blocks 6+12 nested), `ChancellorOfTheSpiresTest`
(14, block 13), `HowltoothHollowTest` (4, block 14), `HordeOfNotionsTest` (4, block 15), `RustTickTest`
(12, block 16), `GalvanothTest` (9, kept block 5 guard path). `EffectDispatchRatchetTest` — green after
baseline regen.

### Files changed

- **New:** `service/effect/MayEffectHandlerRegistry.java`, `service/effect/mayfx/MayEffectHandlerBean.java`
  + 15 `@Component` handler beans in `service/effect/mayfx/`.
- `service/GameEngineConfig.java` — registry bean + auto-registration + log line.
- `service/input/MayAbilityHandlerService.java` — dispatch loop added; 15 branches deleted; two unused
  service deps + `@Lazy` import removed; migrated-type imports pruned.
- `refactor-docs/effect-dispatch-baseline.txt`, `refactor-docs/EFFECT_COUPLING_MATRIX.md` (regenerated).
- `agent-docs/ARCHITECTURE.md`, `agent-docs/EFFECTS_QUICK_REFERENCE.md` (mayfx registry documented).

**For the user:** run the full test suite and commit. Behaviour-preserving refactor — no game-rules change.
