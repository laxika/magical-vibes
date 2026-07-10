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
- **Equal-timestamp fallback:** tests frequently add permanents to battlefield lists directly,
  bypassing the stamping funnel, leaving `timestamp == 0`. The layered engine MUST fall back
  to **battlefield position order** (owner order, then list index) when two timestamps are
  equal — SevenLayerTest simulates timestamps by insertion order. Do not "fix" the tests to
  stamp manually; the fallback is part of the contract.
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

## 7. Migration plan context

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
