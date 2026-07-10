# The CR 613 Layer System (`model/layer`)

Canonical reference for the migration of continuous-effect handling from the single-pass
accumulator (`GameQueryService.computeStaticBonus` + `StaticBonusAccumulator`) to the official
CR 613 layer system. **Every refactor session starts by reading this document** and ends by
appending to the Progress Log at the bottom.

The acceptance spec is `SevenLayerTest`
(`magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/layers/SevenLayerTest.java`,
100 tests). The red tests define the target behavior — never weaken them.

## 1. Layer order (CR 613.1)

Continuous effects are applied in this order, modeled by the `Layer` enum
(`magical-vibes-domain/.../model/layer/Layer.java`, enum order == application order):

| Layer | Enum value | What it changes |
|---|---|---|
| 1 | `L1_COPY` | Copy effects — the copiable values (CR 707.2) |
| 2 | `L2_CONTROL` | Control-changing effects |
| 3 | `L3_TEXT` | Text-changing effects (CR 612) |
| 4 | `L4_TYPE` | Card types, subtypes, supertypes |
| 5 | `L5_COLOR` | Colors |
| 6 | `L6_ABILITIES` | Ability adding/removing |
| 7a | `L7A_CDA` | Characteristic-defining P/T (`*/*` abilities, CR 613.4a) |
| 7b | `L7B_SET_PT` | P/T setting ("has base power and toughness X/Y") |
| 7c | `L7C_MODIFY_PT` | P/T additions — INCLUDING +1/+1 and -1/-1 counters |
| 7d | `L7D_SWITCH_PT` | P/T switching |

## 2. Ordering rules within a layer

- **Layers 1–6** (CR 613.2b): apply characteristic-defining abilities first, then everything
  else in **timestamp order** (CR 613.7).
- **7a and 7b**: apply in timestamp order.
- **7c** is commutative — additions can be summed in any order.
- **7d**: each switch is applied as its **own step** on the current P/T; two switches cancel.
  A switch is never merged or reordered relative to other switches.
- **Dependency (CR 613.8)** overrides timestamp order **within a layer only** (never across
  layers): effect A waits for effect B if applying B first would change
  (a) whether A exists, (b) what A applies to, or (c) what A does.
  Example: Blood Moon vs. an "is a Swamp" Aura on a nonbasic — both in L4; conversely a
  type-change never "depends" on a L6 ability removal, because they are in different layers.
  Dependency **loops** fall back to plain timestamp order.

## 3. Cross-layer semantics that trip people up

State these explicitly because the old accumulator got them wrong:

- **Ability removal happens in L6 and is not retroactive on layers 1–5.** A "loses all
  abilities" effect does NOT un-apply contributions the removed ability already made in
  earlier layers — a changeling under a lose-all effect **keeps all creature types** (the
  changeling ability contributed in L4, before it was removed in L6). But it DOES stop the
  object's own CDAs from applying in **7a**: a `*/*` creature that lost all abilities is 0/0
  (SevenLayerTest: `loseAllRemovesPTDefiningCda`). `CharacteristicState.loseAllAbilities(ts)`
  records the removal timestamp so 7a can check it.
- **P/T boosts are not abilities.** Anthems and lord +1/+1 still apply in 7c to a creature
  that lost all abilities (`boostAppliesToAbilityLessCreature`).
- **Animate-and-set-P/T effects split across layers with ONE timestamp** (CR 613.4 examples):
  the type change applies in L4, the P/T setting in **7b**, both carrying the SAME timestamp.
  So March of the Machines' MV-based P/T is a **7b entry** that a later Lignify/Diminish
  (later timestamp, same sublayer) overrides — while the artifact remains a creature via L4
  (`laterSetterBeatsAnimationBasePT`, `setterOverridesAnimationBasePT`).
- **Copiable values (CR 707.2)** are the printed values as modified ONLY by other copy effects
  and by as-enters choices (e.g. a copy's "as enters" color choice) — NOT by type/color/text
  changes, counters, control effects, or P/T effects from other layers. Clone of a Lignified
  Air Elemental is a normal 4/4 flying Air Elemental.
- **Text changes (L3) rewrite what later-layer effects do.** Mind Bend on Goblin King changes
  which landwalk his L6 grant gives; Mind Bend on Sea's Claim changes which land type its L4
  override sets. Text changes are not copiable and persist indefinitely (no duration).

## 4. Timestamps (CR 613.7)

- A **permanent** gets its timestamp when it enters the battlefield
  (`Permanent.timestamp`, stamped from `GameData.nextTimestamp()` in
  `BattlefieldEntryService.putPermanentOntoBattlefield` — the single entry funnel; verified
  July 2026 that all other direct `playerBattlefields.get(..).add(..)` sites in engine main
  code are control-change *moves*, which per CR 613.7c do NOT re-stamp).
- An **Aura/Equipment gets a NEW timestamp each time it becomes attached** (CR 613.7e).
  Stamped at every resolution site that assigns a non-null `attachedTo` on an
  already-on-battlefield permanent (equip, reattach, Aura Graft, living weapon, ...). Sites
  that attach a *newly created* permanent immediately before `putPermanentOntoBattlefield`
  (an Aura spell resolving, search-to-battlefield-attached) rely on the entry stamp — entry
  and attach are the same event there.
- A **continuous effect created by a resolving spell/ability** (a
  `FloatingContinuousEffect`) gets its timestamp at creation (introduced in step 2).
- **Battlefield lists stamp direct insertions** (step 5): every battlefield list is created
  via `GameData.newBattlefieldList()`, which stamps any still-unstamped permanent
  (`timestamp == 0`) with `nextTimestamp()` on insertion. The engine's entry funnel stamps
  before adding (no-op there); test setups adding permanents by hand get real
  insertion-order timestamps automatically — this is what lets one-shot floating effects and
  hand-built permanents/attachments order correctly against each other (SevenLayerTest
  simulates timestamps by insertion order). Control-change moves re-insert already-stamped
  permanents and keep their stamp (CR 613.7c). Creation sites: `GameSetupService`,
  `DraftService`, `KarnRestartGameEffectHandler`, `GameData.simulationCopy`.
- **Equal-timestamp fallback:** when two timestamps ARE equal (both 0 in unit tests that
  replace the battlefield lists themselves), the layered engine falls back to **battlefield
  position order** (owner order, then list index). Within one object's equal-timestamp
  effects, ability removals apply before grants (an aura's "loses all OTHER abilities" must
  not eat the keyword the same aura grants — Deep Freeze's defender).
- `GameData.timestampCounter` is monotonic, never reset, and copied by
  `GameData.simulationCopy()` so AI simulations continue the sequence.

## 5. Target computation model

A whole-battlefield, layer-by-layer pass replacing the per-permanent
`computeStaticBonus` accumulator:

1. Per query epoch, build one mutable **`CharacteristicState`**
   (`model/layer/CharacteristicState.java`) per permanent, seeded from the **post-copy card**
   (L1 applied to the card identity before the pass — clones already mutate
   `Permanent.card`) plus the permanent's persistent one-shot grants and counters.
2. **L2 (control)** determines controller attribution for the rest of the pass (today control
   is physical battlefield membership; the layered model computes it instead).
3. For each layer L3, L4, L5, L6, 7a, 7b, 7c, 7d **in order**: collect every continuous effect
   classified into that layer from all sources, order them per §2, and apply them **across
   ALL permanents** before moving to the next layer.
4. **Predicate evaluation during the pass:** when applying layer N, all predicates, filters,
   and dynamic counts evaluate against the `CharacteristicState`s **as of the layers already
   applied** — an Elf lord's "Elf" filter reads L4-resolved subtypes; Nightmare's 7a Swamp
   count sees Evil Presence's L4 land-type override. This replaces the current
   null-`FilterContext` recursion guard (`AmountEvaluationService` static contexts): there is
   no recursion because layer N never reads layer ≥N state.
5. State-based actions and all queries (`getEffectivePower`, `hasKeyword`, `isCreature`, ...)
   read the finished states.

**Implementation status (step 5):** `LayerSystemService` (engine, `service/effect/`) runs the
whole-battlefield pass for **layers 4, 5, and 6** (`LayeredBoardState`: per-permanent
`CharacteristicState`s, the resolved per-land type override, the March-animated permanent set,
the managed-L4 replay records, the per-filter-instance layer-4 verdicts consumed per CR 613.6,
the managed-L5/L6 effect instances, and the set of permanents whose color/ability state the
pass touched). Layer 4 is applied with hand-written semantics as in step 4. **Layers 5 and 6**
apply their classified instances (STATIC slots + floating effects) in CDA-first, then
(timestamp, position) order by invoking the legacy staticfx handlers into a throwaway
accumulator and harvesting the per-target result into the states (`applyStaticInstanceViaHandlers`
— all scope/filter logic stays in the handlers); floating one-shots (Incite, Merfolk
Trickster, Wings of Velis Vel grants, one-shot keyword removals) apply directly per affected
permanent. L5: additive grants add to the color set, setting effects replace it. L6: grants
add; removals remove as of their timestamp; `LosesAllAbilitiesEffect` clears keywords,
protection, activated abilities and granted effects and records its timestamp; the object's
own `*/*` CDA is suppressed in 7a when its abilities were lost (Maro → 0/0) WITHOUT undoing
its earlier-layer contributions. The static-bonus assembly runs the legacy handlers with
**layered outputs suppressed** for pass-managed instances (`StaticBonusAccumulator.setLayeredOutputsSuppressed`
— a lord's 7c boost still lands) and merges the finished states into the `StaticBonus`:
`keywords()` is now the COMPLETE final keyword set (printed included), `removedKeywords()` the
seeded keywords the pass removed, `grantedColors()`/`colorOverriding()`/`protectionColors()`/
`grantedActivatedAbilities()`/`grantedEffects()`/`losesAllAbilities()` come from the states.
Off-battlefield targets (AI hypothetical scoring) have no state and fall back to legacy
intrinsic reconstruction. **Unmanaged L5/L6 sources stay legacy-additive outside timestamp
order:** conditional wrappers (`ConditionalEffect`, `EnchantedPermanentConditionalEffect`) and
emblems. `GameQueryService.hasKeyword` answers from `bonus.keywords()`;
`getEffectiveColors`/`getEffectiveColor(GameData, Permanent)`/`hasColor` are the layered color
queries used by protection (`hasProtectionFromSource` checks every source color), fear/
intimidate, color predicates, chosen-color boosts, convoke, and color-based damage prevention.
The pass registers itself on the ThreadLocal BEFORE computing (nested `computeStaticBonus`
calls made by handlers during L5/L6 read the states as of the layers applied so far; the bonus
memo is only used once the board is finished). **One-shot migrated handlers dual-write:** the
normalfx `GrantColorUntilEndOfTurn`/`GrantKeyword` (SELF/TARGET/TOKENS paths)/`RemoveKeyword`/
`LosesAllAbilities` handlers still write the legacy `Permanent` fields for direct
`Permanent.hasKeyword`/`getEffectiveColor` callers AND create the floating effect; the pass
seeds the legacy fields (a legacy lose-all flag = removal-before-everything) and then replays
the floating instance at its real timestamp, which is what drives ordering. Remaining direct
`Permanent.getEffectiveColor()` uses to clean up later: `ai/BoardEvaluator` (heuristics),
`PermanentViewFactory`'s legacy transient-color branch, `Permanent.hasKeyword` direct callers
in null-`GameData` predicate fallbacks. There is **no cross-call cache** yet (no GameData
modification counter) — a dedicated perf step comes later. CR 613.8 dependency is not
implemented: ordering is pure timestamp order (known consequences: a Xenograft grant with an
earlier timestamp does not apply to an artifact a later March of the Machines animates; a
lord's L6 keyword grant does not apply to a creature whose matching subtype arrives from a
LATER-timestamp L6-only source — changeling grants sidestep this by also being classified
into L4, see §7).

**Implementation status (step 6 — layer 7a/7b/7c rules-accurate):** sublayer **7b** is
first-class: `LayerSystemService.applyLayer7b` (runs after L6) collects EVERY base-P/T setter
into one flat entry list sorted by (timestamp, position) and applies it per component into
`LayeredBoardState.basePt7b` — static setters (Lignify, Deep Freeze) harvested via their legacy
handlers at the attach timestamp (harvested UNmanaged: the assembly's own accumulator write is
overwritten by the merge, so no new suppression was needed); floating one-shot
`SetBasePowerToughnessEffect`s at their resolution timestamp; March of the Machines' MV/MV at
March's own timestamp (moved out of the assembly source loop; still gated off for
self-animating artifacts via `hasSelfBecomeCreatureEffect`, which needed a `@Lazy
GameQueryService` in `LayerSystemService`); migrated animation base P/T (below); and permanent
exchange overrides (Evra, Tree of Redemption) which keep their `Permanent` field storage but
are ordered via the new `permanentBase*OverrideTimestamp` fields stamped at exchange time
(timestamp 0 = "before everything" for pre-migration/hand-built state). Entries are
per-component — a power-only exchange leaves toughness to the earlier layers. **7a**:
`SetPowerToughnessToAmountSelfEffectHandler` now writes a base-P/T *override* instead of adding
onto a 0/0 base, and the assembly merges the 7b winner OVER it — a 7b setter beats the CDA in
layer order no matter the timestamps (`basePTSetterOverridesCda`); the lose-all 7a suppression
is unchanged. **7c**: `powerModifier`/counters/static boosts remain storage and sum on top of
whichever base won; `getEffectivePower(Permanent, StaticBonus)` trusts
`bonus.basePTOverridden()` unconditionally — the hardcoded "one-shot flag beats static setter"
guard is gone. `Permanent.getRawPower/getRawToughness` split into public
`getBasePower()/getBaseToughness()` + modifiers; the ladder **no longer decides layered
precedence** — it is the fallback for direct `Permanent` readers (views' raw term — the view
path is layered because `GameBroadcastService` passes `gqs.getEffectivePower(p,bonus) −
p.getEffectivePower()` as the view bonus — plus last-known-information reads and the power/
toughness predicate leaves). One-shot setters **dual-write** (step-5 pattern, deviation from
this step's "migrate away from the fields" plan): `SetBasePowerToughnessEffectHandler` and
`SetAllOwnCreaturesBasePowerToughnessEffectHandler` still set
`basePowerToughnessOverriddenUntilEndOfTurn`/`basePowerOverride`/`baseToughnessOverride` for
direct/LKI readers AND create the floating 7b instance that drives precedence (per-permanent
instances for the mass setter — locks the affected set per CR 611.2c).

**Animation-flag 7b migration status (step 6):** migrated (flag still written; a floating
`SetBasePowerToughnessEffect` at the animation's timestamp drives 7b ordering — all writers
funnel through `AnimationSupport.addAnimationBasePtFloatingEffect`):
`animatedUntilEndOfTurn`/`animatedPower` (`animateSingle` UEOT branch — manlands, Crew,
Chimeric Staff/Mass; `animateOwnLands` UEOT branch; `animateAllLands` — Natural Affinity;
`animateControlledPermanents` — The Antiquities War III → `UNTIL_END_OF_TURN`),
`animatedUntilNextTurn` (`animateOwnLands` — Sylvan Awakening → `UNTIL_YOUR_NEXT_TURN`), and
`permanentlyAnimated` (`animatePermanentTarget` — Tezzeret, Waker of the Wilds → `PERMANENT`;
`animateWhileSource` — Awakener Druid → `WHILE_SOURCE_ON_BATTLEFIELD` with the source id, so
the floating entry and the flag revert together when the source leaves). **Deferred** (flag
only, applies ONLY when no 7b entry exists — any real setter beats it regardless of relative
order; cleanup debt): `animatedUntilEndOfCombat` (Jade Statue — `UNTIL_END_OF_COMBAT` floating
expiry is not plumbed) and the AWAKENING-counter 8/8 (counters carry no timestamp; writers in
`MultiPermanentChoiceHandlerService`). **Cleanup debt — direct `Permanent.getEffectivePower()/
getEffectiveToughness()` callers that bypass the layered numbers:** the power/toughness
predicate leaves (`PredicateEvaluationService`, `StaticEffectSupport.matchesStaticFilter`),
`AmountEvaluationService` toughness-based amounts, `TriggerCollectionService`/
`IncrementTriggerEffectHandler` mana-spent-vs-P/T checks, `DeathTriggerCollectorService`
last-known-information power, and `PermanentViewFactory`'s raw term (already corrected by the
broadcast diff).

## 6. Sources of continuous effects

Each source is classified into one or more layers:

1. **Static abilities of battlefield permanents** — the existing `EffectSlot.STATIC` effects
   (anthems, lords, Blood Moon, Humility-likes, aura/equipment grants). See
   `STATIC_EFFECT_HANDLERS.md` for the current handler inventory that must be re-classified.
2. **Emblems** (`GameData.emblems`).
3. **Floating effects** created by resolved spells/abilities (`FloatingContinuousEffect`,
   stored on `GameData` — introduced in step 2). These replace today's scattered
   `Permanent` fields (`powerModifier`, `grantedKeywords`, `basePowerToughnessOverriddenUntilEndOfTurn`,
   `powerToughnessSwitched`, `losesAllAbilitiesUntilEndOfTurn`, `transientColors`, steal
   tracking sets on `GameData`, ...) and expire by duration instead of by
   `resetModifiers()` bucket.

### Duration enum

Floating effects reuse the existing **`EffectDuration`** enum (reuse over creation) —
`FloatingDuration` from the original design maps onto it as follows; `WHILE_ATTACHED` was
added for attachment-backed effects:

| Design name | `EffectDuration` value |
|---|---|
| UNTIL_END_OF_TURN | `UNTIL_END_OF_TURN` |
| UNTIL_SOURCE_LEAVES_BATTLEFIELD | `WHILE_SOURCE_ON_BATTLEFIELD` |
| WHILE_ATTACHED | `WHILE_ATTACHED` (new) |
| UNTIL_CONTROLLER_NEXT_TURN | `UNTIL_YOUR_NEXT_TURN` |
| PERMANENT | `PERMANENT` |

(`CONTINUOUS` and `UNTIL_END_OF_COMBAT` also exist on the enum for its pre-existing users;
floating effects may use `UNTIL_END_OF_COMBAT`, and `CONTINUOUS` is not valid for them.)

## 7. Classification notes

`LayerClassifier` (engine, `service/effect/LayerClassifier.java`, static registry) maps every
continuous-effect type to the `Layer`s it contributes to:
`classify(CardEffect, boolean fromOwnStaticSlot)` returns a `LayerClassification(layers,
characteristicDefining, colorSetting)`; `possibleLayers(Class)` returns the per-type union for
coverage checks. `fromOwnStaticSlot` means source == affected AND the effect comes from the
object's own `EffectSlot.STATIC` — the CDA criterion. Consumed by `LayerSystemService`
(currently for the layer-4 pass only; effect types that throw on classification are treated
as legacy-only and skipped by the pass). **Every new static effect must be registered there** —
`LayerClassifierTest` walks all `StaticEffectHandlerBean`s and fails on unclassified types.

Reasoning behind the non-obvious mappings:

- **One effect, several layers, ONE timestamp.** `StaticBoostEffect` with granted keywords →
  L6 + 7c (keywordless → 7c only). `AnimateNoncreatureArtifactsEffect` (March of the
  Machines) → L4 + 7b per CR 613.4: the artifact stays a creature via L4 even when a later 7b
  setter (Lignify/Diminish) overrides the MV-based base P/T.
- **7a vs 7b.** `SetPowerToughnessToAmountEffect` from the creature's own STATIC slot is the
  `*/*` characteristic-defining ability → 7a with the CDA flag; anywhere else (granted,
  one-shot) → 7b. `SetBasePowerToughnessEffect` ("has base power and toughness X/Y") is
  ALWAYS 7b, even self-applied.
- **Color additive vs setting** (`colorSetting` on the classification): `GrantColorEffect`
  follows its existing `overriding` flag. Verified against Scryfall oracle text 2026-07-10
  (cache AND live API): Deep Freeze is "is a blue Wall **in addition to** its other colors
  and types" → ADDITIVE (the migration plan's earlier assumption that Deep Freeze is setting
  was wrong; the card class's `overriding=false` matches the oracle — SevenLayerTest's
  `colorSetterReplacesNaturalColor` was corrected in step 5 to use Nim Deathmantle, the true
  setter, instead). Incite "becomes red" and Grand Architect "becomes blue" replace colors
  (CR 105.3) → `GrantColorUntilEndOfTurnEffect` is always setting. Nim Deathmantle "is black"
  (no "in addition") → setting (`overriding=true`).
- **Changeling keyword grants are L4 + L6** (`GrantKeywordEffect` whose keywords contain
  `CHANGELING` → both layers, one timestamp): "gains all creature types" defines the object's
  creature types (CR 702.73a), a layer-4 contribution — so an Elf lord with an EARLIER
  timestamp still boosts a creature that gains changeling later (Amoeboid Changeling, Wings
  of Velis Vel) without needing CR 613.8 dependency. Keywordless-changeling caveat: the
  keyword itself remains removable in L6; a lose-all also hides the L4 contribution for
  FLOATING changeling grants (subtype leaves check the state's keyword OR the intrinsic one —
  a NATURAL changeling under a lose-all keeps its types via the intrinsic check, per §3).
- **CDA flag on the Cairn Wanderer family** (`GainKeywordsOfCreatureCardsInAllGraveyards`,
  `GainActivatedAbilitiesOfCreatureCardsInAllGraveyards`, `GainActivatedAbilitiesOfExiledCards`
  → L6 + CDA): strictly, CR 604.3a limits CDAs to colors/subtypes/power/toughness — an
  ability-adding effect is NOT a CR-legal CDA. The flag is a deliberate modeling decision from
  the migration plan: these self-scans read out-of-battlefield zones only, so applying them
  before the timestamp-ordered L6 effects cannot change any outcome (a later "loses all
  abilities" strips the scanned keywords either way; ordering among grants is commutative).
  All three members of the family are flagged for consistency.
- **Blood Moon** (`NonbasicLandsBecomeTypeEffect`) is L4 ONLY: the affected land losing its
  printed abilities is a consequence of the land-type override itself (CR 305.7), not a
  separate L6 contribution.
- **Ability grants are L6 regardless of the granted ability's content**:
  `GrantEffectEffect`, `GrantActivatedAbilityEffect`, `GrantEquipByManaValueEffect` add an
  ability to the object; what that ability later does is the ability's business.
- **Wrappers classify by what they wrap.** `ConditionalEffect` delegates to `wrapped()`
  (passing `fromOwnStaticSlot` through); `EnchantedPermanentConditionalEffect` unions both
  branches, since which branch applies is game-state-dependent. Their declared
  `possibleLayers` union is L4–L7d (conditions never wrap copy/control/text effects here).
- **One-shot continuous effects** (future floating effects) are classified alongside the
  statics: copy (`BecomeCopyOfTargetCreature*`, `CopyPermanentOnEnterEffect`) → L1; control
  (`GainControlOfTargetEffect`, `ControlEnchantedCreatureEffect`,
  `GainControlOfEnchantedTargetEffect`, `GainControlOfTargetAuraEffect`,
  `TargetPlayerGainsControlOfSourceCreatureEffect`) → L2; `ChangeColorTextEffect` (Mind Bend)
  → L3; `LoseAllCreatureTypesEffect` → L4; `BoostTargetCreatureEffect` (Giant Growth) → 7c;
  `SwitchPowerToughnessEffect` → 7d.

## 8. Migration plan context

This document is step 1 of ~14. Rough sequence: (1) design doc + domain scaffolding +
timestamps ✅, (2) `FloatingContinuousEffect` storage on `GameData` + creation/expiry
plumbing, (3+) layer-by-layer migration of effect families, flipping SevenLayerTest groups
green as each layer's ordering semantics land. Keep `computeStaticBonus` callers working at
every step; the layered engine replaces its internals incrementally.

---

## Progress Log

Append one entry per refactor session: what was done, what flipped green in SevenLayerTest,
and any deviations from this document.

1. **2026-07-10 — Step 1: design doc, domain scaffolding, timestamp infrastructure.**
   Created this document. Added `model/layer/` in `magical-vibes-domain` with `Layer`,
   `FloatingContinuousEffect`, and `CharacteristicState` (not yet wired into the engine).
   Extended `EffectDuration` with `WHILE_ATTACHED` instead of adding the planned
   `FloatingDuration` enum (deviation: reuse over creation; mapping table in §6). Added
   `GameData.timestampCounter`/`nextTimestamp()` (copied in `simulationCopy`),
   `Permanent.timestamp` (copied in the copy constructor; no field-count guard test exists
   for `Permanent`, only `CardFreezeTest` for `Card`), entry stamping in
   `BattlefieldEntryService.putPermanentOntoBattlefield`, and CR 613.7e re-stamps at the ten
   resolution sites that attach an already-on-battlefield permanent
   (`EquipEffectHandler`, `AttachTarget*`/`AttachSource*` handlers, `AnimationSupport`
   transform-and-attach, `LivingWeaponEffectHandler`, `ReturnDyingCreature...`,
   `SacrificeEnchantedPermanentAndReattachSourceAura...`, `MayMiscHandlerService`,
   `PermanentChoiceBattlefieldHandlerService` Aura Graft + reattach-after-sacrifice).
   Control-change moves (`CreatureControlService`, `AuraAttachmentService` revert,
   `GainControlOfTargetAuraEffectHandler`) intentionally do NOT re-stamp (CR 613.7c). New
   unit tests: `service/battlefield/PermanentTimestampTest` (entry stamping, attach
   re-stamp, steal keeps stamp, copy semantics). SevenLayerTest unchanged at 69 green /
   31 red (no behavior change intended or made).

2. **2026-07-10 — Step 2: floating-effect store + lifecycle plumbing.** Added
   `GameData.floatingEffects` (`Collections.synchronizedList`, matching existing GameData
   conventions) with `addFloatingEffect(...)` stamping `nextTimestamp()` on insertion (via a
   new `FloatingContinuousEffect.withTimestamp(...)`; the record is immutable, so
   `simulationCopy()` shares instances via `addAll`, list itself independent). Expiry helpers
   on `GameData` (mirroring the `drainDelayedActions` pattern, each returns the removed
   effects): `expireEndOfTurnFloatingEffects()` — called at the top of
   `TurnCleanupService.resetEndOfTurnModifiers` BEFORE the `resetModifiers()` loop (covers
   both the cleanup step and the "end the turn" effect via `TurnSupport`);
   `expireFloatingEffectsForDepartedSource(id)` (WHILE_SOURCE_ON_BATTLEFIELD + WHILE_ATTACHED)
   — called from `PermanentRemovalService.processRemovalCleanup` (the funnel both
   `removeFromBattlefield` and `processAlreadyRemovedToGraveyard` converge on) AND from the
   one leave-battlefield path outside the funnel found by grep:
   `AuraAttachmentService.removeOrphanedAuras`'s orphaned-aura `it.remove()` (the other direct
   `battlefield.remove(` sites are control-change moves or a lookahead that restores state);
   `expireFloatingEffectsForUnattachedSource(id)` (WHILE_ATTACHED) — called at every
   `setAttachedTo(null)` site (AuraAttachmentService equipment-orphan + control-change
   unattach, AnimationSupport equipment-animation ×2 + transform-to-non-Equipment,
   UnattachEquipmentFromTargetPermanentsEffectHandler) and, since reattaching ends the old
   attachment's effects, immediately before the attach at the same ten CR 613.7e re-stamp
   sites from step 1; `expireFloatingEffectsAtTurnStart(playerId)` (UNTIL_YOUR_NEXT_TURN) —
   called next to the `clearUntilNextTurnEffects()` hook in `TurnProgressionService`. No
   game-log entries for expiry (deviation from "log if obvious pattern": existing wear-offs —
   `resetModifiers`, `clearUntilNextTurnEffects` — are not logged either, so there is no
   pattern to follow). UNTIL_END_OF_COMBAT floating expiry is NOT plumbed yet — add it at
   `clearCombatState` time when a consumer needs it. Nothing reads `floatingEffects` yet.
   New tests: `layers/FloatingEffectLifecycleTest` (stamping, cleanup-step expiry,
   destroyed-source expiry, unattach expiry, per-controller turn-start expiry, simulation-copy
   independence). SevenLayerTest unchanged at 69 green / 31 red.

3. **2026-07-10 — Step 3: layer classification for every known continuous-effect type.**
   Added `LayerClassifier` (engine `service/effect/`, static registry keyed by effect class —
   no behavior change, nothing consumes it yet) covering every effect type registered in
   `StaticEffectHandlerRegistry` plus the one-shot continuous effects SevenLayerTest exercises
   (copy/control/text/type-loss/color/base-P/T/boost/switch). Full reasoning in the new §7
   "Classification notes"; headline calls: `StaticBoostEffect` w/ keywords → L6+7c,
   `AnimateNoncreatureArtifactsEffect` → L4+7b, `SetPowerToughnessToAmountEffect` own-STATIC-slot
   → 7a CDA else 7b, Cairn-Wanderer-family self scans → L6 with a (rules-inexact, documented)
   CDA flag, wrappers delegate to their wrapped effect. Deviation from the step's plan found by
   oracle-text check: Deep Freeze's "is a blue Wall" is ADDITIVE ("in addition to its other
   colors and types"), not setting — `GrantColorEffect` classifies by its `overriding` flag;
   `GrantColorUntilEndOfTurnEffect` ("becomes red/blue") is always setting. New test:
   `service/effect/staticfx/LayerClassifierTest` — walks every `StaticEffectHandlerBean` in the
   engine context and asserts a non-empty layer set per handled effect type (enforces that new
   static effects get classified), plus direct pins of the mappings above. SevenLayerTest
   unchanged at 69 green / 31 red; staticfx suite green.

4. **2026-07-10 — Step 4: whole-battlefield layer-4 pass + timestamp-ordered legacy 5–7.**
   Added `LayerSystemService` (engine `service/effect/`): `computeBoardState(GameData)` seeds a
   `CharacteristicState` per permanent (constructor seeding plus engine-side transient granted
   card types and transient subtypes — one-shot type state is not yet floating effects), collects
   every L4-classified effect from STATIC slots and `gameData.floatingEffects` into uniform
   instances (source, effect, timestamp, CDA flag; unclassified types — e.g. the conditional
   self-animations wrapping `AnimatePermanentsEffect` — are skipped and stay legacy-only), and
   applies them CDA-first then (timestamp, battlefield position) order. Real L4 semantics:
   additive grants append; creature-type setters (`GrantSubtypeEffect(overriding)`) clear the
   creature-type class; land-type setters (`EnchantedPermanentBecomes[Chosen]TypeEffect`,
   `NonbasicLandsBecomeTypeEffect`) clear the land types (incl. LOCUS per CR 305.7) and record
   the per-land override that now drives `getOverriddenLandManaColor`/`tapPermanent`;
   `AnimateNoncreatureArtifactsEffect` adds CREATURE and records the animated set. Tideshaper's
   transient land override applies after all statics (legacy precedence), the
   `losesAllCreatureTypes` flag strips creature types last. Scope filters during the pass are
   evaluated via the new `PredicateEvaluationService.matchesPermanentPredicate(CharacteristicState, ...)`
   overload with a **null FilterContext** — the pass must never recurse into
   `computeStaticBonus`. Two hand-off mechanisms keep the legacy assembly consistent with the
   pass (both born from Bludgeon Brawl's "each non-Equipment artifact is an Equipment"
   self-negating when its filter was re-evaluated against the finished states): (a) effects the
   pass fully applied are **managed** — the assembly skips their legacy handler and replays the
   recorded per-target `L4Contribution` (same accumulator ops the handler would have done, but
   with membership decided as of the effect's own application); (b) every filter evaluated
   during L4 memoizes its per-permanent **verdict keyed by filter instance**, and
   `StaticEffectSupport.matchesStaticFilter` returns that verdict when the same filter object
   shows up in a later-layer part of the same ability (`GrantEquipByManaValueEffect` shares
   Bludgeon Brawl's filter instance) — CR 613.6: all parts of one effect apply to the set
   determined when the first part applied. Layers 5–7: `computeStaticBonus` wraps everything in a per-call "pass"
   (ThreadLocal; nested calls reuse the board and a per-pass bonus memo) and assembles the
   legacy accumulator with sources sorted by (timestamp, position) — 7b setter last-write-wins
   is now timestamp-correct — plus the March MV base P/T injected as a 7b entry at March's
   timestamp (the additive MV hack is deleted; gated off for self-animated artifacts). The two
   subtype predicate leaves in `PredicateEvaluationService` and
   `StaticEffectSupport.matchesStaticFilter` answer from the L4 states while a pass is active
   (`LayerSystemService.activeStateFor`), so lord/aura filters and 7a CDA counts
   (`AmountEvaluationService`'s null-context counting) see L4-decided types. `StaticBonus`
   keeps its exact shape and its type fields keep their legacy-accumulated values (views
   unchanged); only P/T base-override values and filter-driven contributions changed.
   **Cache status:** one L4 board computation per external
   `computeStaticBonus`/`getOverriddenLandManaColor` call, per-pass memo only — no cross-call
   `GameData` modCount cache yet; that is the recorded perf follow-up (AI simulation calls
   these constantly). CR 613.8 dependency not implemented (see §5 status note for the known
   timestamp-only consequence). **Rules adjudications** (oracle checked via cache + live
   Scryfall): Nim Deathmantle costs {2}, so the March tests' hard-coded 4/4 was wrong card
   data — `marchAnimatesArtifact`/`setterOverridesAnimationBasePT` now expect MV 2/2 (the CR
   semantics "base P/T = MV as a 7b entry" is unchanged and is what the tests pin); Lignify is
   a Kindred Enchantment — Treefolk, so Dauntless Dourbark's CDA counts it (CR 205.3f) —
   `cdaSeesLayer4TypeChanges` corrected from 3 to 4 (its baseline green at 3 was a
   coincidence: the accumulator counted the kindred aura but missed the granted Treefolk type
   on the enchanted bear). SevenLayerTest 69 → **78 green (22 red)**; the flips are exactly the
   step's targets: `laterLandTypeOverrideWins` (both orders), `bloodMoonAfterAuraWins`,
   `subtypeOverrideRemovesLordBoost`, `chosenSubtypeGrantFeedsLaterLayers`,
   `marchAnimatesArtifact`, `laterSetterBeatsAnimationBasePT`, `cdaSeesLandTypeOverrides`,
   `setterOverridesAnimationBasePT`. New unit tests: `GameQueryServiceTest`
   `getOverriddenLandManaColor` timestamp-ordering pins (two auras; Blood Moon after aura).
   Post-verification fix: `CharacteristicState`'s constructor now tolerates a null card type
   (bare `new Card()` test stand-ins carry only a name — Urza's land tests seat such cards;
   the constructor already guarded null color and null P/T the same way).

5. **2026-07-10 — Step 5: first-class layers 5 (colors) and 6 (abilities).** The
   whole-battlefield pass now applies L5 and L6 with real CR 613 semantics (see the rewritten
   §5 status note for the architecture): instances collected from STATIC slots + floating
   effects, CDA-first then (timestamp, position, removal-before-grant) order, static instances
   applied by invoking their legacy staticfx handlers into a throwaway accumulator and
   harvesting into `CharacteristicState` (which grew protection colors, a colors-overridden
   flag, and seeded-color/keyword snapshots); the assembly suppresses managed handlers'
   layered outputs and merges the finished states into `StaticBonus` (complete final keyword
   set, diff-based removed keywords, layered colors/protection/abilities/effects/lose-all).
   `hasKeyword`, `hasProtectionFrom(Source)`, fear/intimidate, color predicates, chosen-color
   boosts, convoke, and color damage-prevention all read the layered answers
   (`getEffectiveColors`/`hasColor`/`getEffectiveColor(GameData,·)` added). One-shot handlers
   migrated to floating effects with DUAL-WRITE of the legacy fields (deviation from the plan's
   "migrate away from Permanent fields": ~50 card tests and direct engine callers read
   `Permanent.hasKeyword`/`getEffectiveColor`; the pass seeds the legacy state and replays the
   floating instance at its real timestamp, so ordering is layered while intrinsic reads keep
   working): Incite/Grand Architect color setters, Merfolk Trickster lose-all, one-shot keyword
   removals, and the SELF/TARGET/TOKENS paths of the one-shot `GrantKeywordEffect` handler
   (OWN_CREATURES/ALL_CREATURES/TARGET_PLAYERS_CREATURES stay bucket-only — locked-set effects,
   later step). **New timestamp infrastructure:** battlefield lists stamp unstamped permanents
   on insertion (`GameData.newBattlefieldList`, §4) — required because the Layer6 target tests
   distinguish cross-battlefield INSERTION order, which the owner-order position fallback
   cannot express. 7a suppression: the assembly skips the own `SetPowerToughnessToAmountEffect`
   self handler when the state lost its abilities (CR 613.4a, not retroactive on L2–L5).
   **Rules adjudications:** Deep Freeze re-verified additive (live Scryfall) —
   `colorSetterReplacesNaturalColor` corrected to Nim Deathmantle (wrong card data, step-4
   precedent); changeling keyword grants classified L4+L6 (CR 702.73a) so an earlier-timestamp
   lord boosts a later changeling grant without CR 613.8 dependency (fixes what would have
   been a regression in `allCreatureTypesGrantFeedsLordBoost`). SevenLayerTest 78 → **86 green
   (14 red)**; flips: both Layer5 targets (`colorSetterReplacesNaturalColor`,
   `laterAttachmentColorOverridesEarlier`) and all six red Layer6 targets
   (`keywordGrantAfterLoseAllApplies`, `keywordGrantBeforeLoseAllIsRemoved`,
   `keywordGrantAfterOneShotLoseAllApplies`, `loseAllRemovesPTDefiningCda`,
   `lordGrantBeforeLoseAllIsRemoved`, `grantAfterRemovalApplies`); the other four Layer6
   targets were already coincidentally green and stayed green. Remaining 14 red = L2 control
   (3), L3 text (7), 7a/7b one-shot-flag precedence (3), 7d self-switch SBA (1) — all
   future-step targets, verified identical to the pre-step baseline. Test updates: white-box
   `transientColors`/`colorOverridden`/intrinsic-`hasKeyword` assertions in
   Incite/GrandArchitect/WingsOfVelisVel tests replaced with layered queries;
   MerfolkTrickster/GrandArchitect cleanup simulations now also call
   `expireEndOfTurnFloatingEffects()`; GameQueryServiceTest/PredicateEvaluationServiceTest
   inject the registry into their hand-built `LayerSystemService`;
   AbilityActivationServiceTest sacrifice tests stub `getEffectiveColors`; new
   battlefield-list stamping tests in `PermanentTimestampTest`. Ran SevenLayerTest, the
   staticfx/battlefield/filter/turn/combat/spell/ability unit suites, the layer lifecycle
   tests, ~20 affected card tests (DeepFreeze, MerfolkTrickster, MagebaneArmor, Incite,
   NimDeathmantle, GoblinKing, ElvishChampion, KnightOfGrace, PaladinEnVec, VoiceOfAll,
   WingsOfVelisVel, CairnWanderer, AshlingsPrerogative, GrandArchitect, AmoeboidChangeling,
   Lignify, BloodMoon, Xenograft, changeling family, AdamantWill, AssaultStrobe,
   BlessingOfBelzenlok, MarchOfTheMachines) and the full AI module suite — all green
   (BoardEvaluator hypothetical scoring needed the off-battlefield keyword fallback in the
   assembly).

6. **2026-07-10 — Step 6: rules-accurate layer 7a/7b/7c.** Sublayer 7b is now resolved
   entirely by `LayerSystemService.applyLayer7b` (see the new §5 step-6 status note for the
   architecture): ALL base-P/T setters — static aura setters via handler harvest, one-shot
   floating effects, March MV entries, animation base P/T, permanent exchange overrides —
   apply in one (timestamp, position) order, per component, into
   `LayeredBoardState.basePt7b`; the assembly merges the result OVER the 7a base and
   `getEffectivePower(Permanent, StaticBonus)` trusts `bonus.basePTOverridden()`
   unconditionally (the `!isBasePowerToughnessOverriddenUntilEndOfTurn()` hardcoded precedence
   guard and the assembly's in-loop March injection are deleted). 7a:
   `SetPowerToughnessToAmountSelfEffectHandler` switched from `addPower/addToughness` on a 0/0
   base to `setBasePTOverride`, so a 7b setter overrides the CDA instead of the CDA amount
   surviving as a 7c addition. `Permanent.getRawPower/getRawToughness` split into public
   `getBasePower()/getBaseToughness()` + modifiers — the if-else ladder is demoted to a
   fallback for direct readers (list in §5) and no longer decides layered precedence.
   **Deviation:** the one-shot `SetBasePowerToughnessEffect` handlers DUAL-WRITE the legacy
   UEOT fields instead of dropping them (step-5 precedent: last-known-information reads —
   `DeathTriggerCollectorService` — plus predicate leaves and white-box card tests read the
   fields directly; the floating instance is what drives precedence). **Animation flags
   migrated** (dual-write flag + floating 7b entry): `animatedUntilEndOfTurn`,
   `animatedUntilNextTurn`, `permanentlyAnimated` (both writers, incl. Awakener Druid's
   `WHILE_SOURCE_ON_BATTLEFIELD`). **Deferred:** `animatedUntilEndOfCombat` (no
   end-of-combat floating expiry yet) and the AWAKENING-counter 8/8 (no timestamp) — both
   flag-only, beaten by ANY real 7b entry. Exchange overrides got
   `Permanent.permanentBase*OverrideTimestamp` stamped in
   `ExchangeLifeTotalWithCreatureStatEffectHandler`. SevenLayerTest 86 → **89 green (11
   red)**; flips are exactly the step's remaining targets: `basePTSetterOverridesCda`,
   `auraSetterAfterSpellSetterWins`, `deepFreezeAfterWingsWins` (`wingsAfterDeepFreezeWins`,
   `setterOverridesAnimationBasePT`, `laterSetterBeatsAnimationBasePT` were already green from
   step 4/5 and stayed green, as did the whole Layer7c group, `secondSpellSetterWins` both
   orders, `countersApplyOnTopOfSetter`, `pumpBeforeSetterStillApplies`,
   `temporarySetterExpires`, `loseAllRemovesPTDefiningCda`). Remaining 11 red = L2 control
   (3), L3 text (7), 7d self-switch SBA (1) — future-step targets. Test setups for the
   hand-built `LayerSystemService` (GameQueryServiceTest, PredicateEvaluationServiceTest) now
   also inject `gameQueryService`. Ran SevenLayerTest, the staticfx/battlefield/turn/filter/
   state/combat/spell/ability unit suites, FloatingEffectLifecycleTest, the card tests
   (Diminish, WingsOfVelisVel, Lignify, DeepFreeze, MarchOfTheMachines, GiantGrowth,
   GloriousAnthem, Nightmare, Maro, DauntlessDourbark, QuandrixCharm, MarshFlitter), the
   animation/exchange card tests (AwakenerDruid, ChimericMass/Staff, Evra, TreeOfRedemption,
   FaerieConclave, ForbiddingWatchtower, GhituEncampment, Koth, RustedRelic, SleekSchooner,
   SpawningPool, SylvanAwakening, Tezzeret, TheAntiquitiesWar, TreetopVillage, WardenOfTheWall,
   Weatherlight, WakerOfTheWilds, InkmothNexus, GlintHawkIdol, JadeStatue, NaturalAffinity,
   ElvishBranchbender, FellFlagship, ConquerorsGalleon, ShadowedCaravel, DuskLegionDreadnought)
   and the full AI module suite — all green (two MCTS time-budget tests flaked under load and
   pass in isolation).
