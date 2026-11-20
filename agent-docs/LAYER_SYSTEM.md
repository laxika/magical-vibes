# The CR 613 Layer System (`model/layer`)

> `STATIC` in this file means a static ability (CR 113.3d), written `addStaticEffect(e)`, or
> `target(f).addStaticEffect(e)` for an Aura's enchant ability. It functions from the zones its effect's
> rule names (`CardEffect.staticAbilityZones()`): the battlefield, the stack for kicker, buyback, gift,
> "can't be countered" and "costs less to cast", the hand for splice, mulligan-time actions and opening-hand
> actions (`MayEffect(PregameChoiceEffect)`), and the graveyard for dredge. "Enters with" and "as this enters"
> abilities (CR 614.1c) are static abilities too, never `Triggers.enters(ObjectRelation.THIS)`. An ability that states its
> zones, such as Eminence, is written `addStaticEffect(AbilityZones.of(...), e)`. In a token's or a
> copy's ability list it is `StaticAbility.of(e)`, next to `new TriggeredAbility(Triggers.x(), e)` for
> a trigger. Readers use `getStaticEffects()`.

Entry replacement checks use `LayerSystemService.beginPassWithEnteringPermanent` through `GameQueryService.hasLostPrintedAbilitiesAsEntering` to evaluate the entering permanent in a temporary layered board, without adding it to the battlefield or caching the prospective board. This allows ability-removal effects to suppress the permanent's own enters-tapped and enters-with-counters abilities.

Dependency-trial operations for granted abilities use their effect and cost values, rather than allocation identity: handlers may construct equivalent ability objects independently in each trial. Materialized control-Aura effects retain the Aura's attachment timestamp so simultaneous attachment order is preserved.

**Summary for card-implementation sessions.** Continuous effects are resolved by a
whole-battlefield CR 613 layered pass, not stored on permanents: `LayerSystemService` builds one
`CharacteristicState` per permanent and applies every continuous effect — the static abilities (`getStaticEffects()`)
plus `GameData.floatingEffects` — in layer (L1 copy → L2 control → L3 text → L4 types →
L5 colors → L6 abilities → 7a-7d P/T), CDA-first, timestamp (CR 613.7), and dependency
(CR 613.8, layers 4-6) order; `GameQueryService.computeStaticBonus` assembles the finished
states into the per-permanent `StaticBonus` consumed by every query and view. The finished
board is memoized per `GameData` behind a structural fingerprint (§10) — extend
`computeBoardFingerprint` if the pass starts reading a new input. Rules for new code: register
every new static effect type in `LayerClassifier` (`LayerClassifierTest` enforces it); ask
characteristics via `GameQueryService` with a `GameData` (never `Permanent` fields directly);
every effect a resolution makes is a floating effect with a `Duration` (§6) and nothing else — no
`Permanent` or `GameData` field mirrors it. The regression spec
is `SevenLayerTest` (100/100, never weaken) plus `LayerDependencyTest`; open limitations are
listed in §9 and the current state in §11. The plan for the remaining printed-only static-filter
matchers is `STATIC_EVALUATION_MIGRATION.md`.

Canonical reference for continuous-effect handling under the CR 613 layer system (it replaced
the single-pass accumulator, `GameQueryService.computeStaticBonus` + `StaticBonusAccumulator`).
**Every session that changes continuous effects starts by reading this document** and updates it
to the code it leaves. The step-by-step migration history is in git:
`git show e59a9b3e0b:agent-docs/LAYER_SYSTEM.md` (its "Progress Log").

The acceptance spec is `SevenLayerTest`
(`magical-vibes-application/src/test/java/com/github/laxika/magicalvibes/layers/SevenLayerTest.java`,
100 tests). Since step 9 the suite is FULLY GREEN and serves as the layer-system regression
spec — it must stay 100/100 green; never weaken an expectation (oracle-wrong card data in a
setup may be corrected, Scryfall-verified — step 4/5/7 precedent).

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
  Implemented for layers 4, 5, and 6 (step 11) — see §8 "Dependency" for the doctrine, the
  trial-application algorithm, and its limits. Two land-type setters (Blood Moon vs. an
  "is a Swamp" Aura) are mutually INdependent per official rulings — plain timestamp order;
  the real dependency cases are existence (Blood Moon strips an Urborg-style land's ability)
  and applicability (Xenograft's "creatures you control" growing when March of the Machines
  animates an artifact). Dependency **loops** fall back to plain timestamp order (CR 613.8c).

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

Flanking multiplicity is tracked separately from keyword membership in
`CharacteristicState.flankingInstances`. Printed flanking seeds one instance; each layer grant
adds one, and ability removal clears the count. One-shot flanking grants use their floating
layer effect without also seeding the legacy keyword bucket. Combat blocking queues one
non-targeting trigger per surviving instance against a blocker without flanking.

A whole-battlefield, layer-by-layer pass replacing the per-permanent
`computeStaticBonus` accumulator:

1. Per query epoch, build one mutable **`CharacteristicState`**
   (`model/layer/CharacteristicState.java`) per permanent, seeded from the **post-copy card**
   (L1 applied to the card identity before the pass — clones already mutate
   `Permanent.card`), its counters and its card's perpetual changes. The types, colours and indefinite
   keywords effects give it are floating effects applied at their timestamps, never seeds.
2. **L2 (control)** determines controller attribution for the rest of the pass.
   **Implemented (step 8), but OUTSIDE the per-query pass:** every control-changing effect is
   a floating `L2_CONTROL` effect; the controller of a permanent is DERIVED — the newest
   (highest CR 613.7 timestamp) active control effect wins (`GameData.deriveControllerOf`),
   with none active the permanent belongs to its default controller
   (`GameData.defaultControllerOf`: the `stolenCreatures` ownership record, else the card's
   `ownerId` stamped at setup, else the battlefield it sits on — hand-built test permanents
   and tokens carry no owner). Because the entire engine equates battlefield-list membership
   with control, `CreatureControlService.recomputeControl` PHYSICALLY MOVES the permanent
   between `playerBattlefields` lists eagerly whenever the derived controller changes (on
   effect creation, expiry, and source/aura departure) instead of computing it per query —
   the physical lists are thus always consistent with the derived answer, and the rest of
   the pass keeps reading membership. Moves keep the permanent's timestamp (CR 613.7c, no
   ETB) and set summoning sickness (CR 302.6). `CreatureControlService.reconcileControl`
   (invoked from `removeOrphanedAuras` after every battlefield removal and at the cleanup
   step) is the convergence point: it ensures every attached control Aura has its
   `WHILE_ATTACHED` effect, expires effects whose "for as long as" condition failed
   (source left / creator lost the source / no longer enchanted), and recomputes every
   permanent touched by a control effect or ownership record.
3. For each layer L3, L4, L5, L6, 7a, 7b, 7c, 7d **in order**: collect every continuous effect
   classified into that layer from all sources, order them per §2, and apply them **across
   ALL permanents** before moving to the next layer.
4. **Predicate evaluation during the pass:** when applying layer N, all predicates, filters,
   and dynamic counts evaluate against the `CharacteristicState`s **as of the layers already
   applied** — an Elf lord's "Elf" filter reads L4-resolved subtypes; Nightmare's 7a Swamp
   count sees Evil Presence's L4 land-type override. There is no recursion because layer N
   never reads layer >N state. The null-`FilterContext` recursion guard
   (`AmountEvaluationService` static contexts) is only PARTIALLY replaced (audited step 12):
   subtype/color/keyword predicate leaves answer from the states while a pass is active. Since
   step 16 the P/T predicate leaves and toughness-based amounts no longer fall back to intrinsic
   `Permanent` reads either — they go through `GameQueryService.powerForStaticFilter`, which
   reaches the fully layered number wherever that can be done without re-entering the assembly
   forever, and otherwise reads the 7b/7d results off the board. **Caveat, and the reason step 16
   did not close the stage's headline case:** a filter evaluated while the pass is still applying
   layers 4-6 gets the board-derived reading, because layer 7 has not run. That is the layer order
   for the effect itself. **Resolved in step 18, by deleting the case rather than the invariant:**
   `Tetsuko Umezawa, Fugitive` was never an instance of this — "creatures you control … can't be
   blocked" is a CR 613.11 rules-modifying effect applied *after* the whole layer system, not a
   layer-6 ability grant, and it now sits outside the pass with the ~50 other blocking/attacking/
   untap restrictions. No card in the pool has a genuine P/T-filtered layer-4/5/6 *ability* grant,
   so this invariant stands unchallenged and no "layer 7 ahead of L6" capability is scheduled.
5. State-based actions and all queries (`getEffectivePower`, `hasKeyword`, `isCreature`, ...)
   read the finished states.

**Implementation status (step 5):** `LayerSystemService` (engine, `service/effect/`) runs the
whole-battlefield pass for **layers 4, 5, and 6** (`LayeredBoardState`: per-permanent
`CharacteristicState`s, the resolved per-land type override, the March-animated permanent set,
the managed-L4 replay records, the per-filter-instance layer-4 verdicts consumed per CR 613.6,
the managed-L5/L6 effect instances, and the set of permanents whose color/ability state the
pass touched). Layer 4 is applied with hand-written semantics as in step 4. **Layers 5 and 6**
apply their classified instances (static abilities + floating effects) in CDA-first, then
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
emblems. Since step 14 this is ENFORCED at collection: `LayerSystemService.collectInstances`
skips conditional wrappers, because their conditions read volatile game state — active player,
poison, top of library — that the §10 board fingerprint deliberately does not
cover; the assembly evaluates the condition fresh on every query
(`conditionalStaticGrantsToggleWithoutInvalidatingTheBoard` in `LayeredBoardCacheTest`).
Step 17 narrowed that skip rather than removing it: `admitsConditionalWrapper` admits a
`ConditionalEffect` to **layer 4 only**, and only when `ConditionBoardStability` confirms its
condition reads nothing outside the fingerprint (default-deny; `Metalcraft` is the only leaf
condition listed today). Everything else — every layer-5/6/7 conditional grant, every wrapper
with a volatile condition, and `EnchantedPermanentConditionalEffect` entirely — stays
legacy-additive as before. `GameQueryService.hasKeyword` answers from `bonus.keywords()`;
`getEffectiveColors`/`getEffectiveColor(GameData, Permanent)`/`hasColor` are the layered color
queries used by protection (`hasProtectionFromSource` checks every source color), fear/
intimidate, color predicates, chosen-color boosts, convoke, and color-based damage prevention.
The pass registers itself on the ThreadLocal BEFORE computing (nested `computeStaticBonus`
calls made by handlers during L5/L6 read the states as of the layers applied so far; the bonus
memo is only used once the board is finished). **One-shot effects write nothing to the `Permanent`:**
a resolved colour, keyword, ability or "loses all abilities" effect is only its floating effect, applied at its
own timestamp, which is what drives ordering. Remaining direct
`Permanent.getEffectiveColor()` uses to clean up later: `ai/BoardEvaluator` (heuristics),
`Permanent.hasKeyword` direct callers
in null-`GameData` predicate fallbacks. The finished board is **memoized per `GameData` across
external queries** since step 13 (see §10 "Board cache" for the structural-fingerprint
invalidation contract); the per-target `StaticBonus` assembly still runs on every external
query. CR 613.8 dependency is implemented
for layers 4-6 since step 11 (see §8): a Xenograft grant with an earlier timestamp DOES apply
to an artifact a later March of the Machines animates, and existence-removals (Blood Moon on
an ability-granting land, a lose-all on a lord) beat timestamps; the changeling L4+L6 dual
classification (§7) remains as the cross-LAYER bridge dependency cannot provide.

**Implementation status (step 6 — layer 7a/7b/7c rules-accurate):** sublayer **7b** is
first-class: `LayerSystemService.applyLayer7b` (runs after L6) collects EVERY base-P/T setter
into one flat entry list sorted by (timestamp, position) and applies it per component into
`LayeredBoardState.basePt7b` — static setters (Lignify, Deep Freeze) harvested via their legacy
handlers at the attach timestamp (harvested UNmanaged: the assembly's own accumulator write is
overwritten by the merge, so no new suppression was needed); floating one-shot
`SetBasePowerToughnessEffect`s at their resolution timestamp; March of the Machines' MV/MV at
March's own timestamp (moved out of the assembly source loop; still gated off for
self-animating artifacts via `hasSelfBecomeCreatureEffect`, which needed a `@Lazy
GameQueryService` in `LayerSystemService`); migrated animation base P/T (below). A setter with
no stated duration (an exchange of life and power — Evra, Tree of Redemption — a chosen form, an
as-enters base P/T) is a `PERMANENT` floating `SetBasePowerToughnessEffect` on the permanent with
the timestamp it got as it was made (CR 611.2a, 613.7b). Entries are per-component — a
power-only exchange leaves toughness to the earlier layers. **7a**:
`SetPowerToughnessToAmountSelfEffectHandler` now writes a base-P/T *override* instead of adding
onto a 0/0 base, and the assembly merges the 7b winner OVER it — a 7b setter beats the CDA in
layer order no matter the timestamps (`basePTSetterOverridesCda`); the lose-all 7a suppression
is unchanged. **7c**: counters and the permanent's perpetual modifier are its own storage; every
resolved "gets +X/+Y" is a floating `PowerToughnessBoostEffect` on the permanent
(`GameData.addPowerToughnessBoost`, amounts fixed as it begins, CR 608.2h; one per creature for a
group, CR 611.2c) that the assembly adds with the static boosts on top of whichever base won; `getEffectivePower(Permanent, StaticBonus)` trusts
`bonus.basePTOverridden()` unconditionally — the hardcoded "one-shot flag beats static setter"
guard is gone. `Permanent.getRawPower/getRawToughness` split into public
`getBasePower()/getBaseToughness()` + modifiers; the ladder **no longer decides layered
precedence** — it is the fallback for direct `Permanent` readers (views' raw term — the view
path is layered because `GameViewProjectionFactory` passes `gqs.getEffectivePower(p,bonus) �?�
p.getEffectivePower()` as the view bonus — plus last-known-information reads and the power/
toughness predicate leaves). No base-P/T or P/T-modifying effect is kept on the `Permanent`: a
departed snapshot reads the floating 7b and 7c effects still on its id as its last-known
characteristics (`computeStaticBonus`, and `GameQueryService.ownPower/ownToughness` for a
last-known read that recorded no power), and the mass setters add one floating 7b effect per
creature (CR 611.2c).

**Animations:** no animation writes a flag. Whatever its duration (until end of turn or combat, until your next
turn, `PERMANENT`, `WHILE_SOURCE_ON_BATTLEFIELD`), each characteristic an animation changes is its own floating effect
at the animation's timestamp (`AnimationSupport.addAnimationFloatingEffects`): card types, creature types, colours,
keywords, base P/T in 7b. Readers outside the pass ask `GameData.animatedByFloatingEffect`. **Deferred** (flag
only, applies ONLY when no 7b entry exists — any real setter beats it regardless of relative
order; cleanup debt): the AWAKENING-counter 8/8 (counters carry no timestamp; writers in
`MultiPermanentChoiceHandlerService`). **Cleanup debt — direct `Permanent.getEffectivePower()/
getEffectiveToughness()` callers that bypass the layered numbers** (the mana-spent-vs-P/T
checks of the trigger code were migrated to the
layered queries in step 7): the power/toughness predicate leaves (`PredicateEvaluationService`)
and `AmountEvaluationService` toughness-based amounts
— **all migrated in step 16** to `GameQueryService.powerForStaticFilter`, which keeps the direct
`Permanent` read only as the bottom tier (mid-pass, and as the cycle breaker);
and `PermanentViewFactory`'s raw term. The trigger pipeline's last-known power (`TriggerMatcher`,
`TriggeredAbilityStackService`) reads `Permanent.lastKnownPower/Toughness`, which every departure records from the
layered queries as the permanent leaves (CR 603.10a), so it includes a 7d switch (already corrected by the broadcast diff, which since step 7
also carries the 7d switch to the views).

**Implementation status (step 7 — layer 7d switches as floating effects):**
`Permanent.powerToughnessSwitched` is **deleted** — the first legacy field fully replaced by
floating effects with NO dual-write (nothing needed the intrinsic read: combat, SBAs,
power-based damage and the views all go through the layered queries, and the broadcast diff
carries the swap to `PermanentView`). Each resolution of `SwitchPowerToughnessEffect`
(Twisted Image, Turtleshell Changeling's own ability — the only producers in the card module)
adds ONE floating `L7D_SWITCH_PT` effect (`UNTIL_END_OF_TURN`, `affectedPermanentId` = the
switched permanent; expiry via the step-2 lifecycle). `LayerSystemService.applyLayer7d` (runs
after `applyLayer7b`) applies each active switch as its own step — since switches only swap,
all that survives is the per-permanent parity, kept in `LayeredBoardState.switchedPt7d` (odd
count = swapped; also vetoes the assembly's `StaticBonus.NONE` early-out). `StaticBonus` grew
`ptSwitched`, and `GameQueryService.getEffectivePower/getEffectiveToughness(Permanent,
StaticBonus)` now compute the FULL pre-switch power and toughness through 7c (7b winner or
legacy base, plus own modifiers, plus the bonus sum) and swap the finished values when the
parity is odd — CR 613.4d rules fix over the old flag implementation, which swapped
base+own-modifiers but added the static-bonus term post-swap, mis-assigning asymmetric static
boosts (a +2/+0 aura granted before a switch used to land on the switched power; it now
correctly follows the pre-switch power into toughness). `Permanent.getEffectivePower()/
getEffectiveToughness()` are plain pre-switch accessors (base + modifiers + counters) for the
debt-listed direct readers above.

**Implementation status (step 9 — layer 3 text changes, CR 612/613.1c):** text changes rewrite
the WORDS of an object's abilities and everything downstream (layers 4–7, protection, mana
abilities) sees the rewritten text. Each text change is a floating `TextChangeEffect(fromWord, toWord)` on the
permanent, made by Mind Bend's resolution (`ChoiceHandlerService`) with the stated duration (until end of
turn, or none: CR 611.2a, ending as the permanent leaves), read in timestamp order through
`GameData.textReplacements(permanentId)`, and NOT copiable (Clone copies the `Card`, never the target's
text changes — CR 707.2). Application lives in `TextChangeTransformer` (engine `service/effect/`, static
identity-preserving visitor): given a `CardEffect` and the source's replacement list (applied in
order — they compose, replacement N applies to the output of N�?�1), it rewrites color parameters
(`ProtectionFromColorsEffect` colors, `GrantColorEffect` color), basic-land-type parameters
(`EnchantedPermanentBecomesTypeEffect`/`NonbasicLandsBecomeTypeEffect`/`GrantSubtypeEffect`
subtypes, `AllLandsAreCreaturesEffect` required land type and colour — its two printed-effect scans
`matchesAnimateLand` in `GameQueryService`/`StaticEffectSupport` apply the source's own replacements too), landwalk keywords inside `StaticBoostEffect`/`GrantKeywordEffect` keyword sets
(via the reverse of `Keyword.LANDWALK_MAP`), land-type and color predicates inside filters
(`PermanentHasSubtypePredicate`, `PermanentHasAnySubtypePredicate`, `PermanentColorInPredicate`,
recursing through AllOf/AnyOf/Not), stack-entry color predicates inside target-spell conditions,
and the wrappers `GrantEffectEffect`/`ConditionalEffect`/
`EnchantedPermanentConditionalEffect` (recursing into what they wrap). An unchanged effect
comes back as the SAME instance — the pass's managed/filter-verdict maps are identity-keyed.
Wiring: (a) `LayerSystemService.collectInstances` transforms every static-ability effect with its
source's replacements before any layer applies it; `EffectInstance` carries the transformed
effect plus the `original`, which keys `managedL4Effects`/`l4Contributions`/`managedL56Effects`
(the assembly looks effects up by the instance it iterates off the card's static abilities);
(b) `seedLegacyColorAndAbilityState` seeds the object's own printed protection with its
replacements applied, plus protection tied to that object's chosen-as-enters color; (c)
`applyTextChangesToPrintedLandTypes` applies replacements to the
object's own printed basic land types at seed time — a Mind Bent Forest IS an Island, recorded
in `landTypeOverrides` so the tap funnel produces the new mana color (CR 305.6); a later L4
setter simply overwrites the entry; (d) both handler loops in
`GameQueryService.assembleStaticBonus` pass the transformed view to the legacy handlers (managed
suppression still checks the original), so unmanaged outputs (7c boosts behind text-changed
filters, self CDAs counting a text-changed land word) follow the rewritten text too;
(e) the trigger pipeline (`EventAbilitySupport`, `ZoneChangeRequests`) transforms each battlefield triggered ability with the permanent's own and
global replacements before matching, and `dispatchSpellWatch` a cast trigger's `SpellFilter`
(`TextChangeTransformer.transformSpellFilter`, on `Triggers.youCastSpell().withFilter(...)`'s
`CardColorPredicate`/`CardSubtypePredicate`, recursing through card AllOf/AnyOf/Not), so it
follows the rewritten words (Glamerdye on Riverfall Mimic); (f)
`TargetLegalityService`'s resolution-time target checks read a spell's target restriction
through its own `GameData.spellTextReplacements` (then the global change), so a text change
made to a spell on the stack can make its target illegal (Glamerdye on Soul Reap). Those
replacements are dropped when the spell is cast or leaves the stack, and carried onto the
permanent a permanent spell becomes (CR 400.7a).
Chosen-as-enters colors stay handled by the resolution-time `chosenColor` update. **Known gaps**
(effect types with color/basic-land-type words that are continuously read but NOT yet rewritten;
extend the transformer's visitor when a card needs them): `PreventDamageEffect.fromColors(...)`,
`TargetingRestrictionEffect`, `GrantControllerCreaturesCantBeTargetedByColorsEffect`,
`GrantControllerSpellsCantBeCounteredByColorsEffect`, `GrantLifelinkToControllerSpellsByColorEffect`,
`BoostColorSourceDamageThisTurnEffect`, a life-gain trigger's `causedBySpell` filter,
`AwardAnyColorManaEffect`'s `subtype`, color/land-type words inside trigger effects other
than a cast trigger's spell filter, and activated-ability target restrictions. The remaining `CardColor`/`CardSubtype`-carrying
effect types (token creators, one-shot damage/draw/return effects, costs) consume their
parameters at resolution and are never re-read from a permanent's text. Text changes applied to
a spell on the stack instead rewrite that stack entry's frozen resolving effects immediately.
A text-changed filter that is shared across an ability's parts (CR 613.6 memo) gets
its layer-4 verdict recorded under the TRANSFORMED filter instance; the legacy funnel asks with
the original, misses, and re-evaluates — acceptable drift, no current card hits it.

**Implementation status (step 10 — layer 1 copy effects audited & integrated):** L1 stays
card-swap-based and is applied BEFORE the pass by construction: every copy application funnels
through `PermanentCopierService.applyCloneCopy`, which builds the runtime copy card from the
target's CURRENT `Card` only — the printed card as modified by other copy effects (a copy of a
copy picks up the first copy's card), never counters, never `Permanent` modifiers, never layered
state (CR 707.2) — and `CharacteristicState` seeds from `Permanent.card`, so copies re-enter the
pass with the copied identity automatically. "Copy with exceptions" is baked into the copy card
as part of the copiable value: Quicksilver Gargantuan's 7/7 override (with the CR 707.9d skip of
P/T-defining CDAs), Phyrexian Metamorph's added ARTIFACT type, Evil Twin's added ability
(`CloneService`), Cryptoplasm's retained upkeep ability (`MayCopyHandlerService`) — so a Clone
of a Quicksilver Gargantuan copy is a 7/7 (`QuicksilverGargantuanTest`). Every copy a
resolution makes, whatever its duration (until end of turn: Tilonalli's Skinshifter, Cytoshape; until your next turn:
Shapesharer, The Animus, Volrath the Shapestealer; for as long as the target remains tapped; until the next end step;
none: Mizzium Transreliquat's second ability), is one floating `ResolvedCopyEffect(copiedCard, preCopyCard)` (`L1_COPY`)
on the permanent alongside the card swap (`PermanentCopierService.beginCopy`, with `preCopyCard` from
`PermanentCopierService.preCopyCard`). It is the only copy state. As copies end (the duration clock ends those that end
together as one batch, `PermanentCopierService.endCopies`), the newest one left applies again (CR 613.7), or with none
left the permanent shows `preCopyCard`, so an OLDER effect ending cannot revert a NEWER still-active copy of any
duration. A newer permanent copy that isn't a floating effect (Mirrorform) or a merge updates what they keep
(`PermanentCopierService.updateCopies`). "And gains haste" with a copy is a layer-6 grant beside it, not part of the
copiable values (CR 707.9a; March from Velis Vel). The attachment copies are the same `ResolvedCopyEffect`s
(`AuraCopyService`): `WHILE_REMAINS_ATTACHED` for Blade of Shared Souls and Assimilation Aegis, `WHILE_ATTACHED` for
Metamorphic Alteration's static copy, Secret Invasion and Dinosaur Headdress, so a copy of any duration ending under a
newer one of another leaves the newer one in place. Revert at cleanup happens alongside damage removal, per the
official Tilonalli ruling. Cryptoplasm's copy is INDEFINITE (official ruling: "lasts
indefinitely") and correctly gets no duration marker. CR 611.2c verified against the official
Tilonalli ruling (2017-09-29): an effect that began to apply before the permanent became a copy
continues to apply — true structurally because pre-copy effects live on the `Permanent` (7c
modifiers/counters) or in `GameData.floatingEffects`, not on the swapped card
(`pumpBeforeCopyPersistsOnTheCopy` in `TilonallisSkinshifterTest`). **Out of scope / known
gaps:** face-down/manifest copying, tokens copying permanents, "enters as a copy"
replacement-effect timing beyond the current implementation
(`CloneService.prepareCloneReplacementEffect` resolves the choice mid-entry), stacking a UEOT
copy and an until-next-turn copy on the SAME permanent (the per-duration pre-copy card fields
don't compose as layered L1 timestamps would), and the runtime copy card is never re-frozen
after exception baking (`Card.freeze` hardening gap — the original card froze on `Permanent`
construction; the swap-in copy stays mutable so Cryptoplasm/Evil Twin can bake their extra
abilities after `applyCloneCopy`).

## 6. Sources of continuous effects

Each source is classified into one or more layers:

1. **Static abilities of battlefield permanents** — the existing `addStaticEffect` effects
   (anthems, lords, Blood Moon, Humility-likes, aura/equipment grants). See
   `STATIC_EFFECT_HANDLERS.md` for the current handler inventory that must be re-classified.
2. **Emblems** (`GameData.emblems`).
3. **Floating effects** created by resolved spells/abilities (`FloatingContinuousEffect`,
   stored on `GameData`). Every change a resolution makes to a permanent, a player, a card or the game rules
   is one of these, ended by its `Duration` (below); no `Permanent` or `GameData` field holds such a change.
   Control is derived from the floating layer-2 effects; `stolenCreatures` is only the ownership record,
   maintained by `recomputeControl`.

### Duration

A floating effect's **`Duration`** (`model.effect`) is how long it lasts (CR 611.2a): an `End` event that ends it
(`NEVER` for one with no stated duration), or, for a "for as long as" duration, an `AsLongAs` condition it lasts
while and ends for good once it stops holding (CR 611.2b). Every duration is one of its named constants:
`UNTIL_END_OF_TURN` (CR 514.2), `UNTIL_END_OF_COMBAT`, `UNTIL_YOUR_NEXT_TURN`, `UNTIL_END_OF_YOUR_NEXT_TURN`,
`THIS_TURN_AND_NEXT_TURN` (Peace Talks: the next turn's cleanup, whoever's it is),
the upkeep, untap-step, next-end-step, spell-cast and planeswalk ends, `PERMANENT`, the source-linked
`WHILE_SOURCE_ON_BATTLEFIELD` / `_CONTROLLED` / `_REMAINS` / `_TAPPED` / `_REMAINS_TAPPED` (these "for as long as"
durations also end as their source phases out, CR 702.26f), `UNTIL_SOURCE_LEAVES` for "until ~ leaves the battlefield"
(Oubliette's hold, Gaea's Liege; phasing out isn't leaving, CR 702.26d), `UNTIL_CONTROL_LOST` for "until you lose
control of" it (suspend's haste, CR 702.62a), `WHILE_REMAINS_ATTACHED` for a resolution's "for as long as this Equipment
remains attached to it" (Blade of Shared Souls; ends as the Equipment or the creature phases out), `WHILE_ATTACHED` for
attachment-backed effects, `WHILE_SOURCE_CARD_EXILED` (teach's and cipher's grants, ended by `GameData.removeFromExile`)
and `WHILE_FACE_DOWN` (ended by the clock as the permanent is turned face up). Compare durations with `equals`. `STATIC` marks the effect of a static ability (CR 611.3)
and is not valid for a floating effect.

Only `GameData`'s methods change `floatingEffects`: `addFloatingEffect` (stamps a new timestamp), `putFloatingEffect`
(keeps the effect's own, CR 613.7a), `expireFloatingEffects` and the `expire…` methods built on it,
`endFloatingEffect` and `replaceFloatingEffect`. Each reports the effect beginning or ending to the game's
`RulesObserver`, which the trigger ledger writes.

**Rules-modifying effects.** An effect a resolution makes that changes the rules rather than characteristics
("you can't lose life this turn", "creatures can't attack this turn", "prevent all damage that would be dealt to it this
turn", "you may cast spells as though they had flash") is a floating effect too (CR 611.2a). The layers skip it; it
applies after them, in timestamp order (CR 613.10 for players, 613.11 for game rules). Its payload is the resolving
effect record (with a field for what the resolution chose: a colour, a name, a card), on a player through
`FloatingContinuousEffect.onPlayer` (a `null` player is every player) or on one permanent through `onPermanent`, with
the `Duration` the card states. Readers ask `GameData.hasFloatingEffectOnPlayer` / `floatingEffectsOnPlayer` /
`floatingPlayerEffects`, `hasFloatingEffectOnPermanent` / `floatingEffectsOnPermanent`, `floatingEffectsOfTypes`
(whatever it applies to), and for damage prevention (CR 615) `hasPrevention…` / `preventionEffects…` by
`PreventionScope`. No `GameData` field, cleanup reset or `EndOfTurnStateField` holds such an effect, and the clock ends
it (Runed Terror's per-player cleanup included). Several of one kind combine as the rule says: any of a set, the
newest (maximum life, fixed land colour), the strictest (a spell limit), a sum (damage bonuses).

**Indefinite grants.** "It's a black Zombie in addition to its other colors and types", "it gains haste" with no stated
duration, "it becomes an artifact in addition to its other types", "it enters with haste" (CR 614.1c) and "target land
becomes snow" are floating effects on the permanent with `Duration.PERMANENT` (CR 611.2a), stamped as they are made
(CR 613.7b) and applied in their layer in timestamp order: `GrantSubtypeEffect`, `GrantColorEffect`, `GrantKeywordEffect`,
`GrantCardTypeEffect` (mostly `Subjects.target()`; a floating payload applies to the permanent it is on, whatever its `PermanentSubject`) and the resolving `SetTargetPermanentSupertypeEffect` itself. A site
with no resolving entry at hand (a replacement as the permanent enters, a choice's continuation) uses
`GameData.addEffectOnPermanent(sourceCardName, sourcePermanentId, controllerId, effect, permanentId, duration)`. The
`Permanent` keeps none of them: an earlier "loses all abilities" (Humility) no longer removes a later grant, as it did
while the grants were seeded before every effect. Perpetual
changes are not effects: the pass seeds them from `GameData.perpetualKeywords`, `perpetualCardTypes`,
`perpetualCardSubtypes` and `perpetualCreatureTypeReplacedCardIds` by the permanent's own card, as part of its
characteristics. Tests give and read these grants through `testutil.PermanentGrants`.

**Keyword and protection grants with a duration.** "Gains flying until end of turn", "loses flying", "gains hexproof
until your next turn", "gains protection from red / from artifacts / from colorless / from each of your opponents /
from creatures your opponents control until end of turn" are floating effects on the permanent with the stated
`Duration` (CR 611.2a), applied in layer 6 in timestamp order with the static ones (CR 613.1f, 613.7):
`GrantKeywordEffect` and `RemoveKeywordEffect` (`Subjects.target()`), `ProtectionFromColorsEffect(colors,
Subjects.target())`, and `GrantEffectEffect(protection, Subjects.target())` for `ProtectionFromCardTypesEffect`,
`ProtectionFromColorlessEffect`, `ProtectionFromPlayersEffect` (players fixed as it resolves),
`ProtectionFromNonSubtypeCreaturesEffect` and `GrantProtectionFromOpponentCreaturesUntilEndOfTurnEffect` (its
"opponents" are read at each check). A "loses all abilities" or "loses all protection" removes only the grants older
than it; `CharacteristicState.removeAllProtection` drops the protection abilities granted so far and marks the
permanent's own printed protection removed (`isPrintedProtectionRemoved`; the printed abilities precede every effect,
so no later grant brings them back), and `GameQueryService.hasLostPrintedProtection` gates only the printed reads. Readers ask the
layered queries (`GameQueryService.hasKeyword`, `hasProtectionFrom…`, `computeStaticBonus(...).grantedEffects()`);
`Permanent.hasKeyword` is intrinsic only. The player chosen as a permanent enters (True-Name Nemesis, Guardian Archon)
is `Permanent.chosenPlayerIds`, read by `ProtectionFromChosenPlayerEffect`.

**P/T boosts and base P/T.** "Gets +2/+2 until end of turn" (or until end of combat, or your next turn) is a floating
`PowerToughnessBoostEffect` on the permanent (`GameData.addPowerToughnessBoost`), applied in sublayer 7c
(CR 613.4c); "has base power and toughness 1/1 until end of turn" and a base P/T with no stated duration (an exchange
of life and power, a chosen form) are floating `SetBasePowerToughnessEffect`s on it, applied in sublayer 7b in
timestamp order (CR 613.4b, 613.7); switching stays 7d, after both. An amount computed as the effect begins is fixed
(CR 608.2h), and a boost to a group is one effect per creature (CR 611.2c). Counters and perpetual modifiers are the
permanent's own. Tests give and read these through `testutil.PermanentGrants` (`boostUntilEndOfTurn`,
`powerBoostOf`, `basePowerToughnessUntilEndOfTurn`).

**Abilities.** "It gains '[ability]'" until end of turn, until end of combat, until the effect controller's next upkeep or
with no stated duration (Cipher, Dread Wight's removal ability, a token copy's added death ability, a land's added mana
ability) is a floating effect on the permanent (`GameData.grantAbilityToPermanent`, CR 611.2a), applied in layer 6 in
timestamp order (CR 613.1f, 613.7): a triggered ability as `GrantTriggeredAbilityEffect`, each effect of a static one as
`GrantEffectEffect`, an activated one as `GrantActivatedAbilityEffect` (`Subjects.target()`). "Loses all abilities"
(`LosesAllAbilitiesEffect`) and the removal of one kind of static ability ("loses 'prevent all damage…'", "loses all
'bands with other' abilities": the `SuppressStaticEffect…` records as payloads) are floating layer-6 effects too, so a
grant older than a removal is gone and a newer one stays; a rules restriction added as a non-ability
(`CharacteristicState.addNonAbilityEffect`) is never removed. Readers ask the layered state
(`computeStaticBonus(...).grantedEffects()`, `grantedActivatedAbilities()`, `losesAllAbilities()`,
`GrantedTriggeredAbilitySupport`); the permanent's own abilities precede every effect, so readers of printed abilities
may ask `GameData.losesAllAbilitiesByEffect` / `removesOwnStaticAbility` instead. "Then this creature loses this ability"
is a `LoseThisActivatedAbilityEffect` naming the ability object, with no end (`GameData.hasLostActivatedAbility`). A
departed permanent's last-known copy records whether it had lost its printed abilities
(`Permanent.lastKnownLostPrintedAbilities`) and the departure the abilities granted it as the layers left them. Cipher's grant
lasts while the encoded card stays exiled (`WHILE_SOURCE_CARD_EXILED`, CR 702.99c). Perpetual abilities and keywords
(`GameData.perpetualAbilities`, `perpetualActivatedAbilities`, `perpetualKeywords`) are part of the card: the pass seeds
them, so any "loses all abilities" removes them, and seeds none while the permanent is face down (CR 708.2a). Tests give and read these through `testutil.PermanentGrants`
(`abilityUntilEndOfTurn`, `ability`, `loseAllAbilitiesUntilEndOfTurn`, `grantedTriggerEffects`,
`grantedActivatedAbilities`).

An effect on a card outside the battlefield ("you may play that card this turn", "it gains flashback until end of
turn", "it is revealed until your next turn") is on that card through `onCard` (`affectedCardId`); it ends as the card
leaves its zone, where the engine calls `GameData.endFloatingEffectsOnCard`, because the card becomes a new object
(CR 400.7). Readers ask `floatingEffectsOnCard` / `hasFloatingEffectOnCard` or the named queries built on them
(`exilePlayPermission`, `grantedCardAbility`, …). A grant to every matching card locks its set as it begins: one effect
per card (CR 611.2c). An effect that applies later ("during your next turn", "during target player's next turn",
"during its controller's next combat", "after this turn", CR 702.185a) has a `Duration.start` other than `NOW`:
`addFloatingEffect` keeps it in `GameData.scheduledFloatingEffects`, where no query sees it, and the clock begins it
with its creation timestamp at the turn or combat its start names. "During that combat" of an added combat phase is
`Duration.duringCombatPhase(n)`: the turn's `n`-th combat phase, begun as that combat begins and ended at its end;
adding combat phases before it renumbers it (`GameData.replaceScheduledFloatingEffects`, CR 500.8), and one whose
combat never began is dropped as the next turn begins. "Only land creatures can attack during that combat" (Bumi,
Unleashed) and Illusionist's Gambit's must-attack and can't-attack-you are such effects, not triggered abilities. A
"skip your next …" effect (`SkipNextEffect`, CR 614.10) is a `PERMANENT` effect on the player, one per resolution,
used up as it applies (`GameData.applySkipNext`; two skip the next two, CR 614.10a). A shield with an amount ("prevent the next 3
damage", CR 615.7) carries the amount left; using part of it replaces it with a reduced copy (`replaceFloatingEffect`,
same place and timestamp), and a used-up or "next time" shield ends. A permanent's shields are such effects on it
(`onPermanent`): "prevent the next N damage" (`DamagePreventionShield`), each regeneration shield (`RegenerationShield`
with its rider, CR 701.19a), "destroy it instead of the next damage" and "remove all damage instead of the next
destruction" (the resolving effect itself), and they apply to that object only (CR 400.7). When several apply to one
event the affected player, or the affected permanent's controller, chooses one at a time (CR 616.1): each shield and
regeneration rider is its own option, identical ones one option. The "next spell you cast this turn" grants implement `NextMatchingSpellEffect` and end at
the matching cast (`Duration.UNTIL_MATCHING_SPELL_CAST`) or at cleanup.
"Exile it instead" replacements (CR 614.1a) are such effects too: on a permanent ("if that creature would die this
turn", `GameData.exileInsteadOfDying`, optionally linking the exiled card to its source; "if it would leave the
battlefield", whose timestamp tells whether a later loss of abilities removed it; "creatures dealt damage by it") and on
a card a resolution lets be cast (`exileInsteadOfGraveyard`): that one is on its caster, not on the card, because it
follows the card onto the stack as the spell it becomes, and is used up as the card is cast with it or exiled.
Permissions to play cards from outside the game (`permitPlayFromOutsideGame`, a single-play group ending for all its cards
when one is played) and "[matching] permanents don't untap during [that player's] next untap step"
(`restrictNextUntapStep`, matched as that step begins, CR 611.2c) are on a player.
Control of a player (CR 724) is a `ControlPlayerEffect` on the controlled player (`GameData.controlPlayer`):
Mindslaver's is `Duration.THROUGH_AFFECTED_PLAYERS_NEXT_TURN`, begun by the clock as that player's next turn begins and
ended as the turn after it begins (CR 724.1, so it covers the cleanup steps); Secret of Bloodbending's is
`DURING_AFFECTED_CONTROLLERS_NEXT_COMBAT`; Word of Command's is ended where its window closes. A new one on a player
ends the older ones, begun or not, keeping an older promised extra turn when the new one promises one too (CR 724.1a;
Emrakul, the Promised End). `mindControlledPlayerId()` / `mindControllerPlayerId()` read the newest begun one;
`pendingTurnControl()` / `pendingCombatControl()` the waiting ones.

**The clock.** Nothing calls an expiry for a duration that ends at an event: `DurationClock` (`service/duration`) ends
those effects as `RulesEventPipeline` hands it each rules event batch, before the batch's abilities trigger, and undoes
what the effect kept on its permanent (a copy's card, a tap restriction, mirrored keywords). The events:
`TurnEvent.Began` (`UNTIL_YOUR_NEXT_TURN`), `TurnEvent.StepEnded` (`UNTIL_END_OF_COMBAT` as the combat phase ends,
`UNTIL_END_OF_CONTROLLERS_NEXT_UPKEEP`, CR 500.5), `TurnEvent.Cleanup` (`UNTIL_END_OF_TURN`,
`UNTIL_END_OF_YOUR_NEXT_TURN` control effects and `THIS_TURN_AND_NEXT_TURN` included, CR 514.2; Runed Terror's per-player share), `StepEvent`
(`UNTIL_CONTROLLERS_NEXT_UPKEEP`, `UNTIL_NEXT_END_STEP`), `TapEvent.UntappedDuringUntapStep`
(`UNTIL_CONTROLLERS_NEXT_UNTAP_STEP`), `PermanentChangeEvent` turning a permanent face up (`WHILE_FACE_DOWN`),
`SpellEvent.Cast` (the spell-cast ends) and `ActionEvent.Planeswalked`
(`UNTIL_PLANESWALK`). A test ends a duration the same way: `gd.rulesEvents.occur(new TurnEvent.Cleanup(...))`.
A "for as long as" duration (CR 611.2b) ends as its condition stops holding, so it ends where the state changes, not
at a batch: the engine tells the clock `sourceLeft` (`PermanentRemovalService`, the orphaned-Aura sweep),
`trackedPermanentPhasedOut` (`PhasingService`: a "for as long as" duration tracking a permanent that phases out ends,
its source or the permanent it affects, the permanent itself included, CR 702.26f; "until it leaves the battlefield",
"until you lose control of it" and an attachment a rule or static ability keeps do not, CR 702.26d, 702.26g),
`sourceUntapped` (`TapUntapSupport.untapPermanent`, the one path every untap takes — untap costs and choices too —
via `CreatureControlService.onSourceUntapped`), `sourceUnattached`
(every site that unattaches or moves an Aura or Equipment, before or after it sets `attachedTo`) and
`sourceChangedControl` (`CreatureControlService`'s control move). Which effects need their creator to control the
source is the clock's (`SOURCE_CONTROLLED`, and source-linked control, goad, attack-requirement, lock and grant
effects). `DurationClock.holds` judges the same conditions on the board for the control reconciliation's safety net.
Every end, by event or by state, undoes what the effect kept, copies included; control is
recomputed by the caller. As a permanent leaves the battlefield, `affectedLeft` ends the effects on it whatever their
durations (CR 400.7); they stay readable as its last-known information through `GameData.floatingEffectsOnPermanent`
(`departedPermanentEffects`) until their own durations would have ended them.

## 7. Classification notes

`LayerClassifier` (engine, `service/effect/LayerClassifier.java`, static registry) maps every
continuous-effect type to the `Layer`s it contributes to:
`classify(CardEffect, boolean fromOwnStaticSlot)` returns a `LayerClassification(layers,
characteristicDefining, colorSetting)`; `possibleLayers(Class)` returns the per-type union for
coverage checks. `fromOwnStaticSlot` means source == affected AND the effect comes from the
object's own static abilities — the CDA criterion. Consumed by `LayerSystemService`
(currently for the layer-4 pass only; effect types that throw on classification are treated
as legacy-only and skipped by the pass). **Every new static effect must be registered there** —
`LayerClassifierTest` walks all `StaticEffectHandlerBean`s and fails on unclassified types.

Reasoning behind the non-obvious mappings:

- **One effect, several layers, ONE timestamp.** `StaticBoostEffect` with granted keywords →
  L6 + 7c (keywordless → 7c only). `AnimateNoncreatureArtifactsEffect` (March of the
  Machines) → L4 + 7b per CR 613.4: the artifact stays a creature via L4 even when a later 7b
  setter (Lignify/Diminish) overrides the MV-based base P/T.
- **7a vs 7b.** `SetPowerToughnessToAmountEffect` from the creature's own static ability is the
  `*/*` characteristic-defining ability → 7a with the CDA flag; anywhere else (granted,
  one-shot) → 7b. `SetBasePowerToughnessEffect` ("has base power and toughness X/Y") is
  ALWAYS 7b, even self-applied.
- **Fixed creature markers outside the battlefield.** `SelfBecomesCreatureOutsideBattlefieldEffect`
  contributes creature type in layer 4 and fixed base power/toughness in layer 7b. Its static
  handler contributes no battlefield accumulator values because the oracle condition explicitly
  excludes the battlefield; `GameQueryService` consumes the marker for cards in other zones.
- **Color additive vs setting** (`colorSetting` on the classification): `GrantColorEffect`
  follows its existing `overriding` flag. Verified against Scryfall oracle text 2026-07-10
  (cache AND live API): Deep Freeze is "is a blue Wall **in addition to** its other colors
  and types" → ADDITIVE (the migration plan's earlier assumption that Deep Freeze is setting
  was wrong; the card class's `overriding=false` matches the oracle — SevenLayerTest's
  `colorSetterReplacesNaturalColor` was corrected in step 5 to use Nim Deathmantle, the true
  setter, instead). Incite "becomes red" and Grand Architect "becomes blue" replace colors
  (CR 105.3) → `GrantColorUntilEndOfTurnEffect` is always setting. Nim Deathmantle "is black"
  (no "in addition") → setting (`overriding=true`).
- **`AllLandsAreCreaturesEffect` is L4, plus L5 when it names a colour.** The creature type is a
  layer-4 contribution; the fixed base P/T and creature-ness are filled by the effect's handler in
  the accumulator pass, so the layer-4 case deliberately does NOT mark the effect managed. Kormus
  Bell ("All Swamps are 1/1 **black** creatures") additionally sets the colour — a land has no mana
  cost and is therefore colourless (CR 202.2), so without a layer-5 contribution a later-layer lord
  reading the layer-5 state (Bad Moon's "black creatures get +1/+1") would not see the black. The
  L5 pass harvests only the colour from the same handler and, again, leaves the effect unmanaged so
  the accumulator pass still runs it. Living Lands and Nature's Revolt pass no colour and classify
  as L4 + 7b (the fixed base P/T is harvested from the handler as a timestamped 7b entry, so a newer
  or older base-P/T setter such as Lignify or an animated Mishra's Factory wins by timestamp).
  `EnchantedPermanentBecomesCreatureEffect` with a named colour (Living Terrain's green) is likewise
  L4 + L5 + 7b; its colour is harvested in layer 5 and managed, so a later colour-setting effect replaces it.
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
  separate L6 contribution. Since step 11 this is modeled concretely: every land-type setter
  (`setLandType`) marks the state `printedAbilitiesRemoved`, which kills the land's own
  static-ability effects from layer 4 onward (§8 "Dependency" — existence).
- **Ability grants are L6 regardless of the granted ability's content**:
  `GrantEffectEffect`, `GrantActivatedAbilityEffect`, `GrantEquipByManaValueEffect` add an
  ability to the object; what that ability later does is the ability's business.
- **Devotion modifiers use a special board-wide query value**: `IncreaseDevotionEffect` is
  classified with the static-effect registry so its self handler is collected after text changes,
  then its controller modifier is stored on `LayeredBoardState` before other characteristic
  changes are evaluated. `GameQueryService.getDevotionToColor` and
  `getDevotionToColors` are the only devotion reads that add this modifier; dynamic amounts and
  devotion conditions must use those helpers.
- **Wrappers classify by what they wrap.** `ConditionalEffect` delegates to `wrapped()`
  (passing `fromOwnStaticSlot` through); `EnchantedPermanentConditionalEffect` unions both
  branches, since which branch applies is game-state-dependent. Their declared
  `possibleLayers` union is L4–L7d (conditions never wrap copy/control/text effects here).
  Classification alone does not admit a wrapper to the pass — see `admitsConditionalWrapper`
  (§5, step 17).
- **`AnimatePermanentsEffect` is L4** (step 17): "~ becomes a P/T creature" from an object's own
  static ability, always behind a condition today (Rusted Relic, Warden of the Wall). The creature
  card type and the granted subtypes/card types are the layer-4 contribution; base P/T (7b),
  colour (5) and keywords (6) stay with the legacy self-handler in the accumulator pass — the
  same split `AllLandsAreCreaturesEffect` and `EnchantedPermanentBecomesCreatureEffect` use.
  Outside a static ability the effect is a one-shot resolution (manlands, Crew) that registers its
  own floating 7b entry and never reaches classification.
- **One-shot continuous effects** (future floating effects) are classified alongside the
  statics: copy (`BecomeCopyOfTargetCreature*`,
  `MakeTargetCopyOfTargetCreatureUntilNextTurnEffect`, `CopyPermanentOnEnterEffect`) → L1; control
  (`GainControlOfTargetEffect`, `ControlEnchantedCreatureEffect`,
  `GainControlOfEnchantedTargetEffect`, `GainControlOfTargetAuraEffect`,
  `GainControlOfAuraAttachedPermanentEffect`,
  `TargetPlayerGainsControlOfSourceCreatureEffect`) → L2; `ChangeColorTextEffect` (Mind Bend)
  → L3; `LoseAllCreatureTypesEffect` → L4; `BoostTargetCreatureEffect` (Giant Growth) → 7c;
  `SwitchPowerToughnessEffect` → 7d.
- **Global text changes** (`AllColorWordsBecomeChosenColorEffect`, Swirl the Mists) are the one
  layer-3 effect with NO classifier entry and NO handler. They stamp nothing onto objects;
  instead `TextChangeTransformer.globalColorWordReplacements(gameData)` scans the battlefield and
  the derived replacements are appended after each object's own `TextReplacement`s at every
  transform site (`LayerSystemService.collectInstances`, `seedLegacyColorAndAbilityState`,
  `GameQueryService.assembleStaticBonusUnguarded`). Rewriting every colour word to one word, they
  subsume an object's own colour-word replacements regardless of relative timestamps. Because the
  substitution is derived, `Permanent.getChosenColor()` is part of the board-cache hash.
  Not covered: colour words on spells while they are still on the stack, and the printed-text
  strings shown in `PermanentViewFactory` / `GrantedAbilityViewFactory` (display only).

## 8. Dependency (CR 613.8)

Within one layer/sublayer, effect A is **dependent** on effect B when (1) both apply in the
same layer, and (2) applying B first would change (a) whether A exists, (b) what A applies to,
or (c) what A does. Dependent effects apply AFTER what they depend on, regardless of
timestamps; dependency **loops** are ignored wholesale and fall back to pure timestamp order
(CR 613.8c). Implemented in `LayerSystemService.orderByDependency` for **layers 4, 5, and 6**
(step 11), running only when a layer has 2+ non-CDA instances — the 0/1 case (overwhelmingly
common) skips the machinery entirely.

### Doctrine: "what it does" means "what it attempts to do"

Verified against official rulings (web, 2026-07-10) before implementing, because it decides
the detection function:

- **"Loses all abilities" vs. a grant to the same creature is NOT a dependency** — Humility
  doctrine: ordering is pure timestamps; a grant with a later timestamp than the lose-all
  sticks (SevenLayerTest pins this). The lose-all always *attempts* the same thing ("remove
  all") even though the concrete removal set differs, so it does not "depend" on earlier
  grants. Consequently the detector must NOT compare concrete state deltas.
- **Blood Moon vs. Dryad-of-the-Ilysian-Grove-style additive land types is NOT a dependency
  either** — timestamps decide (Dryad after Blood Moon → lands have all basic types). The
  earlier plan-era assumption that these were dependent was wrong.
- **The real dependency drivers** are (a) **existence** — Blood Moon's land-type set removes an
  Urborg-style land's printed abilities as part of layer 4 itself (CR 305.7), so the Urborg
  grant applies after Blood Moon and never does anything, both orders; and (b) **applicability**
  — Xenograft's "creatures you control" set grows when March of the Machines animates an
  artifact in the same layer, so Xenograft applies after March regardless of timestamps
  (also the Mycosynth-Lattice-family shape).

### Algorithm: trial application over operation fingerprints

Before a layer's non-CDA instances apply (the CDA prefix applies first per CR 613.2b, outside
the dependency relation), `orderByDependency`:

1. Computes each instance A's **fingerprint**: trial-apply A onto `RecordingCharacteristicState`
   copies of the current states (a `CharacteristicState` subclass that logs every mutator call
   as a normalized operation string — `addKeyword:FLYING`, `loseAllAbilities` WITHOUT its
   concrete result, `removeSubtypesIf` opaquely) and collect the per-permanent operation lists.
   A state is copied when the trial first reads it (`TrialStates`); iterating the map copies the rest.
   The trial board copies the L4 filter verdicts (CR 613.6) and land-type overrides; the active
   pass's board is swapped to the trial board for the duration so legacy handlers and nested
   queries read the trial states; all bookkeeping lands on the trial board and is discarded.
2. For each ordered pair (A, B): recompute A's fingerprint in a world where B was applied
   first; if it differs from A's base fingerprint, **A depends on B**. Operation strings make
   this "what A attempts": a changed target set or a vanished application (existence) changes
   the fingerprint; a merely different concrete outcome does not. Pairs where B's own base
   fingerprint is empty are skipped (a no-op cannot change anything), and so are pairs where B's
   base trial wrote nothing A's base trial read: each base trial records a `DependencyFootprint`
   (state reads and writes per permanent and characteristic; side-table keys read through
   `ReadTrackingSet`/`ReadTrackingMap` and written as found by diffing the tables afterwards).
   Until A reads something B changed, A runs as in its base trial, so k Equipment granting to one
   creature cost k trials, not k². Test JVMs run with `-DverifyLayerDependencyPruning=true`,
   which also computes the exhaustive relation and throws on any difference;
   `-DdisableLayerDependencyPruning=true` turns the pruning off.
3. Topologically sorts (Kahn) preferring the collected (timestamp, position) order; when no
   remaining instance is dependency-free, a loop exists — per CR 613.8c the earliest-timestamp
   remaining instance applies next.

**Existence** is modeled in the apply path itself (`staticSourceAbilitiesGone`), which is what
the trials detect: a static-ability instance is skipped when its source's state has
`printedAbilitiesRemoved` (set by `setLandType` as part of the L4 type change, CR 305.7 —
checked from layer 4 onward) or `losesAllAbilities` (an L6 removal — checked from **layer 6**
onward only: ability removal is not retroactive on layers 1-5, a changeling under a lose-all
keeps its types, §3). The same check gates the 7b static setters and — via
`assembleStaticBonus`'s per-source `sourceAbilitiesGone` skip — all layer-7 handler outputs:
a lose-all'd lord grants neither its keyword nor its 7c boost (Adaptive-Automaton-under-Humility
doctrine), and a Blood-Mooned land's static grants stop entirely.
**Exception (CR 613.6):** an effect that already started applying in an earlier layer keeps applying
to the same objects in later layers even if its ability is removed. A card declares that a 7b setter
and a layer-4 effect are one ability with `SetBasePowerToughnessEffect#partOfSameAbilityAs(layer4Effect)`
(the argument is the exact instance passed to `addStaticEffect`, e.g. the `ConditionalEffect` wrapper
for Goddric; Graaz's "are Juggernauts ... base 5/3"). The 7b step then keeps the setter, per target,
only where `board.hasL4Contribution(layer4Effect, target)` — ungrouped effects are unchanged. A
grouped `ConditionalEffect(SetBasePowerToughnessEffect)` is admitted in 7b by `admitsConditionalWrapper`
so its entry takes part in the layered pass. Floating effects are the
one-shot results of already-resolved spells/abilities and exist independently of their source.

**The Cairn Wanderer distinction:** its graveyard-scanning keyword gain is applied CDA-first
per the engine's 613.2 modeling (§7), NOT via dependency — reading out-of-battlefield zones is
not a same-layer dependency on anything, so the CDA prefix stays outside the dependency
relation entirely.

The counter-gated battlefield ability scan used by Experiment Kraj is a normal timestamp-ordered
layer-6 effect rather than a characteristic-defining scan: its applicability depends on the current
battlefield creature characteristics and counters, and its source must still retain the static ability.

Graveyard static effects that modify other battlefield permanents implement
`GraveyardStaticEffect`. The layered pass collects those effects from the controller's graveyard
as synthetic source instances, so their ordinary static handlers can evaluate battlefield scope
and conditions while the graveyard card remains outside the battlefield.

Instant and sorcery spells on the stack are static sources too (CR 113.6): an effect wrapped in
`WrappedStackStaticEffect` functions only while its spell is on the stack.
`LayerSystemService.stackStaticSources` lists those spells, copies included (a copy copies the
original's abilities, CR 707.2); the layered pass and the static-bonus assembly wrap each one in a
synthetic source (`Permanent.forSpellOnStack`, whose id is the spell's stack-object id, so a granted
ability's grant source names that spell or copy) controlled by the spell's controller, with the CR 613.7d timestamp the spell got
when it was put on the stack (`StackEntry.stackTimestamp`, stamped by `GameData`'s stack list). The
board fingerprint hashes those spells, so the cached board drops their effects when they leave the
stack. Torrent of Lava's granted "{T}: Prevent the next 1 damage ..." uses it, binding "this spell" to the
grant source when the ability is activated.

Face-up schemes in the command zone (`GameData.faceUpSchemes`) are static sources the same way
(CR 314.4): the layered pass and the static-bonus assembly in `GameQueryService` wrap each one in
a synthetic source owned and controlled by the scheme's owner, so an ordinary `STATIC` effect such as
`StaticBoostEffect(..., Subjects.creaturesYouControl())` applies from the command zone. A face-down scheme
(`faceDownCommandZoneCards`, i.e. in the scheme deck) contributes nothing; the board fingerprint
hashes that face-down flag so abandoning a scheme invalidates the cached board.

### Limits

- **Compute-once approximation:** the CR re-evaluates dependencies after each application;
  we compute the relation once against the layer-entry states and topo-sort. The difference
  is theoretical.
- **Detection is only as good as the recorded operations:** an effect whose applicability or
  parameters hinge on state OUTSIDE `CharacteristicState` (side conditions read from
  `Permanent`/`GameData`, e.g. tapped-ness, counters, chosen values) produces identical
  fingerprints in both worlds — false negative. No same-layer pair in the current pool hits
  this.
- **Normalization false positives are harmless:** two effects granting the same
  already-present subtype detect as mutually dependent (add-op appears/disappears) — a loop,
  which falls back to timestamp order, and their outcomes commute anyway.
- **7b/7d are not dependency-ordered:** 7b setters' applicability cannot currently key on
  another 7b entry (base P/T lives outside `CharacteristicState` during collection), and 7d
  switches only contribute parity. Layers 1-3 are outside the pass (L1/L2 are storage-derived,
  L3 applies at collection time).
- The **assembly's `sourceAbilitiesGone` skip** also silences *unmanaged* legacy outputs
  (conditional wrappers) of a stripped source, including any hypothetical L4/L5-ish wrapper
  contribution that strictly should have survived a layer-6 lose-all — acceptable over-removal,
  no current card hits it (the self-handler loop keeps only the pre-existing 7a CDA lose-all
  guard, so conditional SELF-animations of a stripped artifact still animate — pre-existing
  gap, unchanged).

## 9. Known limitations

The CR 613 migration and the move of every resolution-made change onto floating effects are complete; §11 sums up
where the code stands. Open limitations:

- **Flag-only 7b straggler**: the AWAKENING-counter 8/8 (counters carry no timestamp) — any
  real 7b entry beats it regardless of relative order.
- **Dependency limits** (§8): computed once per layer, not re-evaluated per application;
  blind to applicability conditions living outside `CharacteristicState`; 7b/7d and layers 1-3
  are not dependency-ordered; the assembly's `sourceAbilitiesGone` skip can over-remove a
  hypothetical conditional-wrapper contribution of a stripped source.
- **Unmanaged L5/L6 sources** (conditional wrappers, emblems) still apply additively,
  outside timestamp order. Conditional wrappers are admitted to layer 4 only, and only for
  fingerprint-stable conditions (`admitsConditionalWrapper`); layers 5-7 are untouched.
- **L1 gaps**: face-down/manifest copying, tokens copying permanents, enters-as-copy replacement
  timing, and the unfrozen runtime copy card
  (`Card.freeze` hardening gap — also the board cache's one blind spot).
- **L2**: equipment auto-unattach on control revert happens only for reverts to the default controller.
- **L3 transformer coverage**: the effect types listed in the layer-3 status note (§5) are not
  yet rewritten; a text-changed filter shared across an ability's parts misses the CR 613.6
  verdict memo in the legacy funnel (acceptable drift, no current card hits it).
- **Ad-hoc characteristic reads outside the layered funnel**: `BoostBySharedCreatureTypeEffectHandler`
  (Coat of Arms) and `LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffectHandler` (Call to the
  Kindred) compare the permanents' own subtypes and intrinsic changeling, so they never see statically or
  floating-granted subtypes (e.g. Arcane Adaptation).

## 10. Board cache (cross-query memoization, step 13)

Every external `computeStaticBonus`/`landTypeOverrideFor` call runs `LayerSystemService.beginPass`,
which used to recompute the whole-battlefield board every time — the AI (MCTS simulation,
`BoardEvaluator`, `CombatSimulator`) hammers these queries. The finished
`LayeredBoardState` is now **memoized per `GameData`**: `beginPass` computes a structural
fingerprint of every input the board computation reads and reuses the cached board
(`LayerSystemService.BoardCache(fingerprint, board)`, stored in the opaque
`GameData.layeredBoardCache` slot — the domain module cannot reference the engine type) when
the fingerprint is unchanged. What is **NOT cached**: the per-target `StaticBonus` assembly
(and the per-`Pass` `bonusMemo`) still runs on every external query — assembly reads inputs the
fingerprint deliberately does not cover (emblems, the conditions of the conditional wrappers the
pass did not collect, turn/step state, amount evaluation), so caching it would be dishonest;
shrinking scope to the board only is the correctness-first trade. Dynamic amounts used by
layer-7b base P/T setters are evaluated during board construction, so player life totals and recorded game losses are part
of the board fingerprint. The wrappers the
pass DOES collect (§5, step 17) are exactly those whose conditions read only fingerprinted state
— that is what `ConditionBoardStability` decides, so widening it means widening
`computeBoardFingerprint` too.

For read-only *batches* of queries there is `GameQueryService.withQueryScope(gameData, supplier)`:
it opens one pass around the whole batch (reusing an already-active pass when nested), so the
fingerprint is checked once and every `computeStaticBonus` inside hits the per-`Pass`
`bonusMemo` instead of re-assembling per query. The caller must not mutate game state inside
the scope — the memoized bonuses would go stale (same contract the nested-pass memo already
relies on). `CombatSimulator`'s attack/block searches use this to share one pass across all
`CreatureInfo` builds, block-legality queries, and creature scores of a decision.
`CombatDamageService` uses it for the first-strike check, the manual-assignment pre-check, and
— via `snapshotDamagePhase` — to capture per-combatant `CombatantStats` (P/T, combat keywords,
protection pairs) once per damage step; the dealing loop then runs on that pre-damage snapshot
(CR 510.4 simultaneity), while casualty determination stays on live queries so it sees infect
counters placed during the step. `GameQueryService.getEffectiveCombatDamage` also opens a scope
internally to share one pass across its power/toughness/battlefield-scan reads.
`TargetTriggers.trigger` detects every becomes-the-target trigger of one targeting event inside
one scope (CR 603.2: they all trigger on the same event). Each target runs a scan of the
controller's battlefield per kind of becomes-the-target ability, so unscoped this was targets × scans × permanents fingerprints of an O(board)
hash — a 100-target Fireball over 100 creatures spent ~20 s re-confirming an unchanged board.
Detection writes inside the scope, but only state neither the board nor the assembly reads
(triggered abilities on the stack, log lines, queued choices/may abilities, once-per-turn
markers); the pending-target prompts it queues are started after the scope closes.
`LibraryTriggers` (a draw), `TapTriggers` (a permanent becoming tapped) and `RulesEventPipeline` (a zone-change
batch's observer and moving-object candidates; one attack, block, spell, control-change, crew, day/night, Room, Saga or
entry-caused-trigger event) detect in one scope each on the same terms. The tapped-for-mana events
stay unscoped: a triggered mana ability resolves as it triggers (CR 605.4a), and a later scan must see what it changed.
So do damage and combat damage detection (they record the turn's damage history, which static conditions read, as
they go), life events (a win check), actions (a player losing) and attachments ("as this becomes attached" choices).
A step or phase beginning finds and matches its candidates in one scope and puts the matched abilities on the stack
after it closes (an ended delayed ability is removed there). The read-only board lookups that judge each permanent in
turn open one scope each: `GameQueryService.activeStaticEffects` (both overloads, so every board scan built on it),
`BlockLegalityService.createBlockLegalityContext`, `AttackLegalityService`'s attack-target ids,
`CastingCostService.hasAttackPaymentForAnyCreature`/`hasAttackLifePaymentForAnyCreature`,
`AttackSacrificeCostService`'s global sacrifice costs, `TurnProgressionService.hasRunedTerror`,
`DrawService`'s look-at-top-choose-one replacement source and `PermanentRemovalService.exileReplacementApplies`.
A pass also memoizes each permanent's active static effects (`Pass.activeStaticEffects`, on the bonus memo's terms),
the board-wide yes/no scans (`Pass.boardScan`: `anyBattlefieldHasStaticEffect`, `graveyardCardsHaveLostAllAbilities`,
the printed-effect presence checks in front of `isCreature`'s animation scans, and `anyManaAbilityLock` and the
board-wide tap-ability lock check in front of `canActivateManaAbility`'s per-source lock walks), and answers `findPermanentController` from a battlefield index it builds after a few lookups, each answer checked
against the battlefield it names. A scan over the board's static abilities uses
`GameQueryService.activeStaticEffects(gameData[, controllerId], type)` rather than looping over the battlefields:
it gives each effect with its permanent and that permanent's controller, never searched for.
`ReplacementOrderService` collects each event's applicable replacement and prevention effects, previews the
pure ones and decides whether their order matters inside one scope (a candidate source's `applicable` and
`transform` change nothing): each `DamageCandidateSource` rule scans the battlefield, so unscoped every damage
event paid rules × permanents fingerprints.
State-based sweeps that act as they go (`AuraAttachmentService.removeOrphanedAuras`/`enforceAttachmentLegality`,
`StateTriggerService.checkStateTriggers`) judge permanents in one scope until they find one to act on, close it,
act, and open a new one for the rest: a clean board costs one fingerprint per sweep, and each later permanent is
still judged against the state the earlier action left. The read-only ones (the departure batch's Aura prediction,
the legend-rule search, the speed check) run in one scope each.
`DepartureService.gather`/`choose` find a departure batch's destruction and death replacement
questions inside one scope (both change nothing; the questions are asked once it closes) — each
permanent's lookups scan the whole board, so a 100-creature V.A.T.S. paid a fingerprint and an
assembly per permanent looked at, per creature. Performing the batch can't share one scope, since
the board changes between removals; instead each removal's read-only source scans
(`findReturnToHandReplacement`, `exilesBattlefieldCardWithSources`, the lost-printed-ability
watcher record, the battlefield exile replacement's ability-loss reads) open their own, as do
`deathExileReplacementOptions` as a whole, the batch's snapshot loop (`DepartureService.perform`,
before anything is published or removed) and the look-back reads `removePermanentToGraveyard` takes
just before the permanent leaves (`lookBackAtDeath`). So does each batch of zone-change trigger
detection — `RulesEventPipeline` judges every observer's and moving object's ability for one batch of
zone-change events in one scope, since matching only puts abilities through the stack door (same
CR 603.2 argument and write audit as becomes-the-target). State an assembly may read — the death counts and the
`graveyardLookBackExcludedCardIds` set/cleared around the look-back call — is written between those
scopes, never inside one; `removeFromBattlefield`'s last-known reads stay unscoped, because they write
the live permanent's `lastKnownPower` (read by `PermanentPowerDifferentFromBasePowerPredicate` while
it is still on the battlefield) and can destroy an attached permanent on unattach. Independent of
scopes, `assembleStaticBonusUnguarded` leaves out battlefield sources with nothing to apply — no
printed static effect, no static effect the pass granted them, no floating static grant to
themselves — so a target's assembly costs the board's static sources, not its size. Which sources those are (and
their CR 613.7 order) doesn't depend on the target, so a finished pass collects them once (`Pass.staticSources`,
on the bonus memo's terms) and each assembly only judges what is per target: the target's controller, and so
whether each source is on its battlefield. Graveyard cards, schemes and spells are kept as collected and wrapped
in a fresh `Permanent` per assembly, as before. Test JVMs run with `-DverifyStaticSourceCache=true`, which
collects again on every reuse and throws if the sources changed — a scope that mutates what an assembly reads
fails the test instead of reading stale sources. The `bonusMemo` is keyed by
object identity, not id: a dying permanent's LKI snapshot (`simultaneousDyingPermanents`) shares the
live permanent's id but assembles on its own, so a scope that reads both never hands one the other's
bonus. Target legality runs scoped as well: `TargetLegalityService.validateMultiSpellTargets` (every
public overload funnels into the scoped one) and `validateSpellTargetGroup` at cast, and
`isTargetIllegalOnResolution` as a spell or ability resolves (its only writes are the entry's
illegal-target marks and a dropped primary target, which no layered read looks at) — each target's
protection, hexproof and "can't be the target of" checks are layered queries. Within it the
`SHARE_TOUGHNESS` recheck compares each legal target with the first. Note that
only `computeStaticBonus` and the `activeStateFor`-first queries (`hasLostPrintedAbilities`,
`hasLostAllAbilities`, subtype/keyword reads) answer from the scope's board — a direct nested
`beginPass` still fingerprints. A fresh pass nested in a scope (`withFreshQueryScope`, over a previewed state) that
misses the cache reuses a board an earlier nested pass of the same scope computed for that fingerprint
(`Pass.nestedBoard`): the cost previews of every hand card leaving its hand in one playability sweep share one board.

### Invalidation contract

There is **no mutation counter to bump** — this is a deliberate deviation from the original
"modCount in the mutation funnels" plan: step 12 documented many direct `Permanent` field
writers with no funnel (bucket-only keyword grants, animation flags, ...), and tests
(SevenLayerTest included) mutate battlefield lists and permanents directly. The honest scheme
is to **re-derive validity from the inputs themselves on every query**: the fingerprint hashes
battlefield composition/order, player life totals, every pass-read `Permanent` field (tap state, attachments,
counters, chosen values, P/T modifiers/overrides, animation state, granted/removed
keywords/colors/subtypes/types, lose-all flags, text replacements, persistent granted
activated abilities), the current `Card` identity (L1 copy swaps) plus, for unfrozen cards only,
printed values and the identity of each of its abilities (insurance for `TestCards.mutableCard` and
clone runtime copies; abilities are immutable and a change replaces one; a frozen card's mutators
throw, so identity covers it), the floating effects, graveyard/exile contents (CDA
scan inputs) and hand sizes, and `timestampCounter`. **The canonical input list lives in the
`computeBoardFingerprint` javadoc — extend it whenever the pass starts reading a new input.**
A stale fingerprint can only produce a false MISS (safe recompute); a false HIT requires a
64-bit hash collision on the same `GameData`. The fingerprint is O(board) cheap field reads
(~40 mixes per permanent), orders of magnitude cheaper than the pass it guards.

The flip side of the contract: **anything the fingerprint does not cover must not be read by
the board computation.** Conditional wrappers are the enforced case (step 14): their
conditions read active player/poison/top-of-library, so `collectInstances`
excludes them from the pass entirely and the per-query assembly evaluates them — baking a
condition's result into the cached board is exactly the staleness the seven step-14 card-test
failures exposed (Serra Ascendant, Village Survivors, Viridian Betrayers, Vampire Nocturnus,
Jousting Lance).

### Safety invariants

- **The finished board is read-only.** `l4FilterVerdicts` and the states are only written
  while `computeBoardState` runs; dependency trials swap in throwaway trial boards; assembly
  and predicate leaves only read. That is what makes sharing one board across queries (and
  threads — immutable entry, `volatile` publish, racing fillers overwrite benignly) sound.

### Display provenance (`LayeredBoardState.provenance`)

The board additionally records **per-source attribution lines** (`ModifierLine`, a domain
record) for the client's hover breakdown: the layer-6 application sites record keyword
grants/removals and lose-alls with their source's card name, `applyLayer7b` records every
base setter in resolved order (base lines fold last-wins per component downstream), and
`applyLayer7d` records each switch (parity derived from the line count). Provenance is a pure
function of the same fingerprinted inputs as the rest of the board, so caching the board
caches it too; trial boards get their own discarded map. The assembly side lives in
`GameQueryService.explainStaticBonus`: it re-runs `assembleStaticBonus` with an explain
recorder that diffs the accumulator around each source's handlers (7c boosts, emblems,
conditional wrappers, the 7a self-CDA base), appends the board lines, and merges one line per
source name (base/switch lines keep their order — folding is order-sensitive). Only
`GameViewProjectionFactory` calls it (per broadcast, per permanent); rules code and the AI never
pay the diffing cost, and `PermanentView.modifierLines` carries the result to the client.
The layer-6 board also keeps engine-internal source attribution for non-keyword effects granted
through `GrantEffectEffect` and for direct scoped protection grants from Auras/Equipment. View
construction filters that provenance against the final layered ability state, formats the
surviving effects as `GrantedAbilityView` records, and sends only their display text/source name
— raw `CardEffect` records never cross the wire. Chosen-color protection and temporary
unblockability are projected from their final runtime state; source attribution is omitted when
legacy storage did not retain it.
One-shot pumps (floating layer-7c boosts on the permanent) are
attributed to the card that made them; anything else unattributed the client reconciles as an
"Other effects" remainder against the aggregate fields, which stay authoritative. Pinned by
`ModifierExplanationTest`.
- **AI simulation copies start cold**: `simulationCopy()` builds a fresh `GameData` and never
  copies the slot, so a simulated board can never be served to the real game or vice versa
  (the shared-`Card` leak precedent). Pinned by `LayeredBoardCacheTest`.
- **Escape hatch**: `-DdisableLayerBoardCache=true` restores the recompute-every-query
  behavior (also how the benchmark measures before/after from one build).
- **Known gap**: mutating the one unfrozen runtime copy card in place after a query was
  cached would not invalidate (identity + printed values are hashed, added abilities only by
  relevant ability counts); today Cryptoplasm/Evil Twin exception baking happens inside the same
  resolution as the copy swap, so no query can intervene.

### Benchmark

`LayerPassBenchmarkTest` (application `layers/`, run with `-DlayerBench=true`, add
`-DdisableLayerBoardCache=true` for the pre-cache baseline): 24-permanent static-heavy board
(anthems, lords, Blood Moon, March, Maro, Nightmare, Lignify/Dub attachments), sweeps of 4
layered queries per permanent (96 external queries/sweep), time-boxed. 2026-07-10 numbers
(dev machine, test JVM `-XX:TieredStopAtLevel=1`; ratios are what matter):

| Scenario | before (cache off) | after (cache on) | speedup |
|---|---|---|---|
| steady state (no mutation between sweeps) | 6.2 sweeps/s �? 595 queries/s | 280.9 sweeps/s �? 26 970 queries/s | ~45A� |
| one tap-toggle per sweep (recompute every sweep) | 6.2 sweeps/s �? 593 queries/s | 163.3 sweeps/s �? 15 678 queries/s | ~26A� |

The mutating case still wins big because a sweep is 96 queries: before, every query recomputed
the board; after, only the first query after the mutation does.

2026-10-30, hashing frozen cards by identity only (no walk of each card's abilities per
fingerprint), same build otherwise: steady state 433.9 → 753.0 sweeps/s, one tap-toggle per sweep
351.5 → 538.0 sweeps/s.

---

## 11. Current state

- **Every change a resolution makes is a floating effect** with a `Duration` (§6): characteristics (copies, control,
  text, types, colours, abilities, keywords, protection, P/T) in their layers, rules changes on a player, a permanent,
  a card or the game after the layers (CR 613.10, 613.11). `GameData` alone changes `floatingEffects` (§6), and
  `DurationClock` ends each effect at its rules event or as its "for as long as" condition stops holding (CR 611.2a,
  611.2b). No `Permanent` or `GameData` field mirrors such an effect, and `Permanent`'s `GameData`-less accessors
  (`hasKeyword`, `getEffectiveColor(s)`, `getEffectivePower/Toughness`, the type checks) are intrinsic only: ask
  `GameQueryService` with a `GameData`. `EndOfTurnStateField` holds only turn history and per-turn bookkeeping
  (blocked-without-blockers, saddled, regeneration counts, crew and tap-for-ability records, the "this turn" delayed
  abilities, the per-turn player and card sets), reset at cleanup.
- **Delayed triggered abilities are not floating effects**: each is a `TemporaryGlobalTriggeredAbility`
  (`architecture/triggers.md`), including boons, watcher lists and opening-hand reveals.
- **Rules effects stay out of the layers.** A static blocking, attacking or untap restriction ("creatures you control
  with power 1 or less can't be blocked", Tetsuko Umezawa) has no `LayerClassifier` entry and no static handler; the
  legality services read it straight off the static abilities and judge its filter on the fully layered board
  (CR 613.11), so a P/T filter sees layer 7 (`ControlledCreaturesMatchingCantBeBlockedEffect`,
  `GameQueryService.controllerMakesMatchingCreaturesUnblockable`). No card has a P/T-filtered layer-4/5/6 grant, so
  "layer N never reads layer >N" (§5) holds.
- **Recursion safety is ambient**: `GameQueryService.isStaticEvaluationActive()` is true inside a static-bonus
  assembly or while `LayerSystemService.buildingBoard()` reports an unfinished board on this thread (the whole `Pass`
  parent chain); condition and amount evaluation then take their recursion-safe branch instead of re-entering the pass.
- **Basic land mana follows the layered land types**: `GameQueryService.intrinsicBasicLandManaColors` derives each
  colour from the effective basic land subtypes, so an added land type needs only `GrantSubtypeEffect`; a separate
  `GrantActivatedAbilityEffect` would leave a stale mana ability after a later effect replaces the type.
- **Tests**: `SevenLayerTest` (100/100) and `LayerDependencyTest` are the spec; `testutil.PermanentGrants` gives and
  reads floating grants; a test ends a duration by emitting its event (`gd.rulesEvents.occur(...)`).
  `python3 -I scripts/duration-inventory.py` counts any duration still held outside `Duration`.
- **History**: the per-step migration log (steps 1-20) is in `git show e59a9b3e0b:agent-docs/LAYER_SYSTEM.md`.
