# Effect Consolidation Plan

Working document for collapsing near-duplicate `CardEffect` records (and their handlers) into
parameterized effects. Derived from a full audit of all 1,658 effect records in
`magical-vibes-domain/.../model/effect/` and their handlers in
`magical-vibes-engine/.../service/effect/{normalfx,mayfx,staticfx}/`.

Roughly **150 effect classes** and a matching number of handlers are collapsible.

---

## How to run this plan

Steps are executed **one at a time, each in a fresh context**. The context is cleared between
steps to save tokens, so every step must be self-contained: this file is the only state that
carries across.

**Protocol for each step:**

1. Read this file and the step's entry below.
2. **Verify the premise yourself before editing.** Read both the effect records *and* their
   handlers. The audit was thorough but is not a substitute for looking. If the handlers have
   diverged since the audit, say so and stop rather than forcing the merge.
3. Read `agent-docs/ARCHITECTURE.md` before any change beyond a card class + its test
   (per `CLAUDE.md`) — card freezing, CR 613 layers, thread safety, Jackson 3 imports,
   view immutability.
4. **Behavior must stay identical** unless the step explicitly says otherwise. Where a step
   notes a behavior change or a latent bug, make the change deliberately and cover it with a test.
5. Verify every Comprehensive Rules number with the `rules` MCP `get_rule` tool before writing it
   anywhere. Numbers drift between releases — several citations in this codebase were already
   found stale.
6. Update the affected card classes, delete the absorbed records **and** their handlers, and
   update the relevant `agent-docs/` files (`EFFECTS_INDEX.md`, `EFFECTS_QUICK_REFERENCE.md`,
   `ORACLE_TEXT_EFFECT_MAP.md`, `PREDICATES_REFERENCE.md` as applicable).
7. Add or update tests for the merged effect. **Never run the full test suite** — run only the
   affected test classes with `--tests` filters.
8. Mark the step `DONE` in the Step Index below, with a one-line note if anything deviated.
9. **Finish by printing the prompt for the next step** (template at the bottom of this file), so
   it can be copied straight into the next session.
10. **Do not commit.** Commits happen only when explicitly asked.

---

## Do not regress these fixes

Seven rules bugs were fixed in commit `9b8147333` in files this plan touches. A merge that
reverts one of them is a regression:

| File | Fix to preserve |
|---|---|
| `SacrificeSelfThenEffectHandler` (absorbed the `SacrificeSelfAndDrawCards` / `SacrificeSelfAndTargetPlayerDiscards` / `SacrificeSelfThenDealDamageToTargetPlayer` handlers in Step 2) | Fire `checkAllyPermanentSacrificedTriggers`, call `removeOrphanedAuras`, and gate the payload on the removal succeeding ("If you do"). **Never** re-express this as `SequenceEffect.of(SacrificeSelfEffect(), payload)` — a sequence splices its steps unconditionally |
| `AttachSourceEquipmentToTargetCreatureEffectHandler`, `AttachTargetEquipmentToTargetCreatureEffectHandler` | Success path logs an attach message, not a fizzle message |
| `CopySpellForEachOtherSubtypePermanentEffectHandler` | Has the `isCantBeCopied()` guard (CR 707.10) |
| `DealDamageToTargetCreatureEqualToChosenTypeCountEffectHandler` | Applies `applyDamageMultiplier` |
| `DestroyEachTargetPermanentEffectHandler`, `DestroyPermanentsTargetPlayerControlsEffectHandler` | Route through `DestructionSupport.destroyBatchCollecting` so deaths are simultaneous |
| `UntapPermanentsEffectHandler.resolveChosenControlled` (absorbed the `UntapUpToControlledPermanentsEffect` handler in Step 11) | Prompts the controller via `MultiPermanentChoiceContext.UntapChosenPermanents` — does not untap the first N in battlefield order |
| `ExileTargetPermanentAndImprintEffect` | `TargetSpec.harmful`, not `benign` |

Copy handlers cite **CR 707.10** for "can't be copied". CR 706.2 is the die-roll rule — do not
reintroduce it.

---

## Step Index

Phase 1–2 are low risk and mechanical. Phase 3 involves real behavior decisions. Phase 4 is
high-churn or needs a design call and is optional.

| # | Step | Classes deleted | Risk | Status |
|---|---|---|---|---|
| 1 | Emblem effects → `CreateEmblemEffect` | 16 | LOW | **DONE** — premise held (all 16 records zero-component, all 16 handlers identical bar Garruk's `getTargetId()`); two deliberate deltas: the `playerIds.contains` guard now runs on the controller path too, and Garruk's emblem log gains the trailing `.` the other 15 already had |
| 2 | Outright deletions (compose from existing effects) | 6 | LOW | **DONE** — rows 1–4 held; the `SacrificeSelf*` row did **not** (`SequenceEffect` has no "if you do" gate, so composing it would have reverted a `9b8147333` fix) and was replaced with a new `SacrificeSelfThenEffect(CardEffect)`, which also absorbs `SacrificeSelfThenDealDamageToTargetPlayerEffect` — that class was **not** unused |
| 3 | Redirect-next-damage family | 6 | LOW | **DONE** — premise held for all six handlers; the audit missed a third axis (`TargetSpec.harmful`), which is now derived from `destinationRole == TARGET` and makes Zealous Inquisitor harmful like its Zhalfirin Crusader twin (CR 702.16b). The `PermanentPredicate` moved out of the effect onto the two white-creature cards |
| 4 | Any-color mana family | 8 | LOW | **DONE** — premise held; `eachManaChosenSeparately` is derived from the restriction, not a component. The audit missed an eighth sibling (`AwardAnyColorSubtypeSpellManaEffect`, Sliver Hive), absorbed in a follow-up commit. Three deliberate deltas, all rules fixes: Damping Sphere / land-type replacement now see spend-restricted land mana, "could produce" scans (CR 106.7) now include restricted any-color lands, and the AI's choice-prompt filter covers all restrictions |
| 5 | Combat-requirement / must-attack | 6 | LOW | **DONE** — premise held for all six handlers; the audit undercounted by one (5b absorbs 2 classes, not 1, so 6 records + 6 handlers went). One unlisted axis found: `MustBlockThisTurnIfAbleEffect` alone carried a `PermanentIsCreaturePredicate` in its spec, preserved by branching `targetSpec()`. Zero behavior change |
| 6 | `SetLifeTotalEffect` | 6 | LOW | **DONE** — premise held for all four listed handlers; the audit undercounted (4 records, not 3) and the optional stretch fell out easily, so both each-player siblings went too: 6 records + 6 handlers. `EACH_PLAYER` now evaluates the amount once per player, which is what made the stretch free |
| 7 | `SkipNextEffect` | 4 | MED | **DONE** — premise held for all four handlers; the audit undercounted (4 records, not 3). One correction: `boolean targetsPlayer` cannot express the family — Blinding Angel is `targetId`-bound but non-targeting — so the second component is a 3-value `SkipRecipient`, which keeps "which player" off the `SkipKind` axis. Polarity unified on `benign` |
| 8 | Phase-out + attached-counter placement | 6 | LOW | **DONE** — premise held for all six handlers; the optional 8b fold was taken, so 6 records + 6 handlers went. The audit missed that `@CollectsTrigger` is class-keyed: two collector annotations had to be re-keyed. Zero behavior change beyond one redundant guard dropped |
| 9 | Destroy-referenced-permanent | 4 | LOW | **DONE** — premise held for all four handlers; `ENCHANTED` became `PermanentReference.ATTACHED` as the note directed and the enum gained `SOURCE`. One correction: the Ajani half needed no player target filter but **did** need `DestroyAllPermanentsEffect.targetSpec()`, which the two sibling scoped effects already had and it was silently missing |
| 10 | Search-target-library + sacrifice costs | 5 | LOW | **DONE** — 10a's premise held in shape but not in detail (the audit's "only `.destination()` varies" is wrong on four other axes) and the audit undercounted: a fourth sibling, the play-permission form, folds in as two destinations, so 5 records + 5 handlers went. The reconcile picked `checkSearchRestriction` — `isSearchPrevented` shuffled the *searcher's* library, not the searched one. The 10b stretch was not taken (reason in the step body) |
| 11 | Tap / untap scopes and costs | 3 | LOW | **DONE** — all three premises held. Two corrections: the `ON_ANY_CREATURE_DIES` collector for 11a was already occupied by an identical `UntapPermanentsEffect` one (a deletion, not a re-key), and 11c's `DynamicAmount` count cannot be evaluated inside the handler (`requiredCount()` takes no game state), so a new `TapCostSupport` resolves it at construction and `toPermanentChoiceCostHandler` gained a `GameData` parameter. The MED stretch was not taken |
| 12 | `Grant*` low-risk batch | 6 | LOW | **DONE** — all five premises held; the audit's net count is one high (5, not 6 — the sixth would have been folding away the `SpellCastingAbilityGrantingEffect` capability interface, which is deliberately kept). One correction: the hand-size row is a rename, not an extension — `GrantPermanentNoMaxHandSizeEffect` would have lied once `UNTIL_NEXT_TURN` was folded in |
| 13 | Cost-modification batch | 6 | LOW | **DONE** — all four premises held; the audit's net count is two low (6 records + 3 handlers, not 4). One correction: the tax scope reuses the existing `CostModificationScope`, not a new `CostTaxScope`. Two rules fixes shipped, both from `getType()` → `hasType` (CR 205.2b) |
| 14 | Remove-counter batch | 4 | LOW–MED | **DONE** — all four premises held; the audit's count is two low (6 records + 5 handlers, not 4). Two corrections: both `fromTarget` booleans became a shared `CounterRemovalSubject` enum, and the two "identical" gain-life handlers were not identical (one rendered the counter name, the other the enum constant). The flagged decision went **for** adding `sourceCountersRemoved()` to the survivor — rationale in the step body |
| 15 | Reveal / reorder library batch | 2 | LOW | **DONE** — both premises held. Three corrections: the two `boolean targetPlayer` flags became one shared `LibraryOwner` enum (which keeps the reorder family's nine own-library call sites untouched); the reveal's `@ValidatesTarget` validator had to be branched or the Deceivers would demand a player target; and `Fixed(0)` is honest to the *amount*-reading `LifeGainEffect` consumers but not to the two presence-checking ones, so the capability gained a `gainsNoLife()` default |
| 16 | Damage target-category batch | 3 | LOW | TODO |
| 17 | Exile-top-cards families | 7 | LOW–MED | TODO |
| 18 | Enchanted-creature aura batch | 3 | LOW–MED | TODO |
| 19 | Entering-creature conditionals + `GrantEffectTo*` | 5 | LOW–MED | TODO |
| 20 | Misc low-risk 2→1 batch | 12 | LOW | TODO |
| 21 | `RevealUntilEffect` | 4 | MED | TODO |
| 22 | Static damage-prevention markers | 6 | MED | TODO |
| 23 | Attach family | 4 | LOW–MED | TODO |
| 24 | Colour-setting `Become*` family | 4 | LOW–MED | TODO |
| 25 | Copy-spell-for-each-other | 1 | MED | TODO |
| 26 | Exile-instead-of-graveyard replacements | 3 | MED | TODO |
| 27 | Exile-target-permanent linkage | 2 | MED | TODO |
| 28 | Remaining MED items | ~14 | MED | TODO |
| 29 | High-churn merges (needs sign-off) | ~3 | LOW behavior / HIGH churn | TODO |

---

## Phase 1 — Mechanical, LOW risk

### Step 1 — Emblem effects → `CreateEmblemEffect`

**Target**
```java
public record CreateEmblemEffect(List<CardEffect> staticEffects,
                                 String reminderText,
                                 EmblemRecipient recipient) implements CardEffect
// convenience ctor (List<CardEffect>, String) -> recipient = CONTROLLER
// enum EmblemRecipient { CONTROLLER, TARGET_PLAYER }
```

**Absorbs** (16 classes + 15 handlers, 1 card each):
`AjaniSteadfastEmblemEffect`, `ArlinnEmbracedByTheMoonEmblemEffect`,
`ChandraDressedToKillEmblemEffect`, `DomriRadeEmblemEffect`, `ElspethKnightErrantEmblemEffect`,
`GarrukApexPredatorEmblemEffect`, `GarrukCallerOfBeastsEmblemEffect`,
`GideonOfTheTrialsEmblemEffect`, `JaceUnravelerOfSecretsEmblemEffect`, `JayaBallardEmblemEffect`,
`KothEmblemEffect`, `LilianaOfTheDarkRealmsEmblemEffect`, `SorinLordOfInnistradEmblemEffect`,
`TamiyoMoonSageEmblemEffect`, `TeferiHeroEmblemEffect`, `VenserEmblemEffect`.

**Evidence** — every handler is the same four statements: resolve `controllerId`, look up the
player name, `gameData.emblems.add(new Emblem(controllerId, List.of(<effects>), entry.getCard()))`,
log `" gets an emblem with \"<text>\"."`. `Emblem` is already
`record Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard)`, so the payload
is a pass-through. `List<CardEffect>` inside an effect record has 18 existing precedents
(`SequenceEffect`, `ChooseOneEffect`, `ClashEffect`, …).

**Only variation**: `GarrukApexPredatorEmblemEffectHandler` uses `entry.getTargetId()` instead of
`entry.getControllerId()` (the emblem goes to an opponent) → `EmblemRecipient.TARGET_PLAYER`.

**Gotchas** — the per-card effect lists move into the card constructors, which is where engine
logic belongs. No emblem effect class is referenced outside its own handler and its own card
(no AI, view, or serialization special-casing) — confirm this still holds before deleting.

**Keep**: `EmblemGrantsFlashbackEffect` is an emblem *ability*, not an emblem creator.

---

### Step 2 — Outright deletions — **DONE**

| Deleted | Replaced with | Cards |
|---|---|---|
| `WinGameIfCreaturesInGraveyardEffect` | `ConditionalEffect(new GraveyardCardThreshold(20, new CardTypePredicate(CardType.CREATURE)), new WinGameEffect())` | 1 |
| `CreateLifeTotalAvatarTokenEffect` | `CreateTokenEffect` with the existing `tokenEffects` overload, carrying `SetPowerToughnessToAmountEffect(new ControllerLifeTotal(), new ControllerLifeTotal())` | 1 |
| `ChooseTwoColorsOnEnterEffect` | `ChooseColorOnEnterEffect(2)` | 1 |
| `CantAttackOrBlockUnlessEquippedEffect` | `CantAttackOrBlockUnlessEffect(new Equipped(), "it's equipped")` | 1 |
| `SacrificeSelfAndDrawCardsEffect`, `SacrificeSelfAndTargetPlayerDiscardsEffect`, `SacrificeSelfThenDealDamageToTargetPlayerEffect` | **new** `SacrificeSelfThenEffect(CardEffect thenEffect)` | 2 + 2 + 1 |

Net: 7 records and 5 handlers deleted, 1 record and 1 handler added.

**What held**
- `ChooseTwoColorsOnEnterEffect` was a **zero-engine-change** merge as predicted — every read site is
  interface-typed on `ChooseColorEffect` (`BattlefieldEntryService:1012,1154`,
  `StackResolutionService:478,536`, `PlayerInputService:163`). `ChooseColorOnEnterEffect` gained an
  `int choicesRequired` component; all 15 existing call sites use the no-arg/varargs ctors and were untouched.
- `CantAttackOrBlockUnlessEquippedEffect`: `CombatHelper.isCantAttackOrBlockUnlessEquipped` and its one
  call site (`AttackLegalityService:94`) are gone, as is the `BlockLegalityService:358` special case —
  both sides already handle `AttackOrBlockRestrictionEffect` generically. `isSourceEquipped` and
  `GameQueryService.isEquipped` are equivalent.
- `WinGameIfCreaturesInGraveyardEffect`: the nontoken difference is real and is now asserted
  (`MortalCombatTest.tokensInGraveyardDoNotCount`). `StepTriggerService` gained a
  `ConditionalEffect + GraveyardCardThreshold` upkeep branch in place of the class-keyed one.
- `CreateLifeTotalAvatarTokenEffect`: the generic token path is a strict superset — it also runs
  `handleCreatureEnteredBattlefield` and the legend rule, which the deleted handler skipped. That
  latent bug is fixed and asserted (`AjaniGoldmaneTest.avatarTokenFiresEnterTriggers`).

**What did not hold — two corrections**
- **`SequenceEffect` cannot express "if you do".** Its own javadoc says so and
  `EffectResolutionService:230` confirms it: the steps are spliced into the entry unconditionally.
  Composing `SequenceEffect.of(new SacrificeSelfEffect(), payload)` would have let Impaler Shrike draw
  three cards after being killed in response — a straight revert of the `9b8147333` fix in the
  regression table. Replaced with `SacrificeSelfThenEffect(CardEffect thenEffect)`, which keeps the
  gate, delegates `targetSpec()` to the payload, and dispatches the payload synchronously through
  `EffectHandlerRegistry` (expanding a `SequenceEffect` payload the way `FlipCoinWinEffectHandler`
  does). Gating is covered by `ImpalerShrikeTest.noDrawWhenShrikeLeavesBeforeResolution` and
  `MindstabThrullTest.unblockedNoDiscardWhenThrullLeavesBeforeResolution`.
- **`SacrificeSelfThenDealDamageToTargetPlayerEffect` was not unused.** `DrawService:700` constructs it
  at runtime for Booby Trap's draw trigger (`BoobyTrapEffect` is only the static marker). Deleting it
  outright would have removed Booby Trap's trigger. It is absorbed by `SacrificeSelfThenEffect` instead,
  with `DealDamageToPlayersEffect(10, DamageRecipient.TARGET_PLAYER)` as the payload.

**Left for a later step**
- `SacrificeSelfThenDestroyTargetEffect` (Wasp of the Bitter End) was **not** absorbed: it declares
  `TargetSpec.harmful(CREATURE)` and no existing destroy effect reproduces that category
  (`DestroyTargetPermanentEffect` is `harmful(PERMANENT)`), so folding it in would have silently
  widened the spec. Revisit alongside Step 9.

**Deliberate behaviour deltas**
- Mindstab Thrull / Dauthi Mindripper now set `discardCausedByOpponent = true` (the generic
  `DiscardEffect(TARGET_PLAYER)` path); the old bespoke handler left the flag untouched. The defending
  player's discard *is* opponent-caused, so discard punishers should see it.
- Those two cards also no longer skip the sacrifice when the trigger carries no target player — the
  sacrifice is not contingent on there being a defender.
- Log strings changed for Mortal Combat (generic `ConditionalEffect` / `WinGameEffect` wording) and for
  the Avatar token (generic token log). Assertions updated.

---

### Step 3 — Redirect-next-damage family — **DONE**

**Shipped**
```java
public record RedirectNextDamageEffect(RedirectRole protectedRole,   // SOURCE_PERMANENT | TARGET
                                       RedirectRole destinationRole, // TARGET | SOURCE_PERMANENT | CONTROLLER
                                       DynamicAmount amount,
                                       TargetPredicate declaredTarget, // null | creature() | anyTarget()
                                       PermanentPredicate targetPredicate) implements CardEffect
// + (…, DynamicAmount, TargetPredicate) and (…, int, TargetPredicate) convenience ctors
// enum RedirectRole { SOURCE_PERMANENT, TARGET, CONTROLLER }
```

Six records and six handlers deleted, one record + one enum + one handler added. Cards:
Zhalfirin Crusader, Zealous Inquisitor, Personal Incarnation, Martyrdom, Hazduhr the Abbot,
Daughter of Autumn, Vassal's Duty.

**What held** — all six handlers really do reduce to one shield insert varying only in which of
`getSourcePermanentId()` / `getTargetId()` / `getControllerId()` fills each slot. The
`playerNextDamageRedirectShields` branch was a strict superset and is kept: it now fires whenever the
resolved *protected* id is a player, which is reachable only from Martyrdom's any-target grant.

**What the audit missed — a third axis.** `targetSpec()` varied in **polarity**, not just category:
Zhalfirin Crusader declared `harmful(ANY_TARGET)` while Zealous Inquisitor — the same shape narrowed
to a creature — declared `benign(CREATURE)`. Rather than carry a `boolean harmful` component, polarity
is now derived: `harmful` exactly when `destinationRole == TARGET`, because that object is the one
that takes the redirected damage. Verified against CR 702.16b ("can't be targeted by abilities from a
source with the stated quality"). **Deliberate delta**: Zealous Inquisitor can no longer target a
creature with protection from white; covered by
`ZealousInquisitorTest.cannotTargetCreatureWithProtectionFromWhite`.

**Other deliberate deltas**
- The `TargetSpec` white-creature `PermanentPredicate` was baked into
  `RedirectNextDamageToTargetCreatureToSourceEffect`; it is now the `targetPredicate` component,
  built once per card and shared with the card's own `TargetFilter`. The "you control" half still
  rides only on Hazduhr's `ControlledPermanentPredicateTargetFilter` — the spec is evaluated
  without a source permanent, so a controller predicate cannot work there.
- The `amount <= 0` early return, previously on only the two `DynamicAmount` handlers, is now
  universal (no-op for the five `Fixed` cards).
- The protected object must still exist: the three protected-by-source handlers used to install a
  shield keyed to a departed permanent, with a `"the creature"` log fallback. That shield was inert;
  it is no longer created. Covered by
  `ZealousInquisitorTest.noShieldWhenInquisitorLeavesBeforeResolution`.
- The amount now evaluates against the ability's own source permanent in every case (the
  to-controller handler passed the *target*). No observable change — all seven cards use `Fixed` or
  `XValue`, neither of which reads the context permanent.
- One log phrasing replaces two ("… dealt to X this turn is dealt to Y instead"), with a card chip
  for permanents on both ends rather than a plain name on the protected side. No test asserted these.
- `TargetPolarityClassifier` swaps its single name-keyed entry for an `instanceof` branch keyed on
  `destinationRole`, so all seven cards classify instead of one.

---

### Step 4 — Any-color mana family — **DONE**

**Shipped**
```java
public record AwardAnyColorManaEffect(DynamicAmount amount,
                                      ManaSpendRestriction restriction,
                                      CardSubtype subtype) implements ManaProducingEffect
// sugar: (), (int), (DynamicAmount), (int, ManaSpendRestriction), (int, ManaSpendRestriction, CardSubtype)
// enum ManaSpendRestriction { NONE, INSTANT_SORCERY_COPY, INSTANT_SORCERY_ONLY, FLASHBACK_ONLY,
//                             CREATURE_SPELL_ONLY, CHOSEN_SUBTYPE_CREATURE,
//                             CHOSEN_SUBTYPE_CREATURE_UNCOUNTERABLE, SUBTYPE_SPELL,
//                             SUBTYPE_SPELL_OR_ABILITY }
```

Eight records and two handlers deleted, one record + one enum + one support class
(`service/effect/AnyColorManaChoiceSupport`) added. Cards: Springjack Pasture, Resonating Lute,
Altar of the Lost, Primal Wellspring, Ancient Ziggurat, Somberwald Sage, Smokebraider, Primal Beyond,
Cavern of Souls, Pillar of Origins, Unclaimed Territory, Sliver Hive. The survivor's 45 existing call
sites were untouched — the `()` / `(int)` constructors still mean the same thing.

**What held** — the restriction axis really was already unified on the choice side
(`ChoiceContext.ManaColorChoice`), and every branch reduces to "evaluate an amount, then open one
`PendingInteraction.ColorChoice` with the matching context". `AnyColorManaChoiceSupport.beginColorChoice`
is now the single expression of the nine cases, shared by the mana-ability path, the stack handler and
the enchanted-land-tap trigger.

**What the audit missed**
- **`eachManaChosenSeparately` is not an independent axis.** `ChoiceHandlerService.handleManaColorChosen`
  decides per-mana vs per-batch colour picking from *which restriction branch it took* — there is no
  such flag on `ManaColorChoice`. Carrying a boolean on the effect would have needed new plumbing to
  mean anything, so polarity is derived instead (`FLASHBACK_ONLY` and `SUBTYPE_SPELL_OR_ABILITY` are
  the "any combination of colors" wordings), exactly as Step 3 derived `harmful`.
- **There was an eighth sibling.** `AwardAnyColorSubtypeSpellManaEffect` (Sliver Hive) — a *printed*
  subtype routed into the same spell-only bucket as Cavern of Souls — is the natural
  `CHOSEN_SUBTYPE_CREATURE` counterpart with a fixed type. It was outside the plan's stated scope, so
  it landed as an immediate follow-up commit: `ManaSpendRestriction.SUBTYPE_SPELL`, with the `subtype`
  component now serving both printed-subtype restrictions. **8 records absorbed in total, not 7.**
  Same CR 106.7 delta as the rest — Sliver Hive now counts as a source of every colour for
  "could produce".
- **The X variant is not just "amount from xValue".** It also reported nothing to the AI's estimator
  where the plain form reports full colour coverage. `estimatedCountsAllColors()` is therefore
  `restriction == NONE && amount instanceof Fixed`, which keeps Springjack Pasture invisible to the
  estimator instead of making the AI believe it taps for a free any-colour mana.

**Deliberate deltas** (all three widen a scan that had silently omitted spend-restricted producers)
- `calculateTotalManaProduction` now counts every restriction, so **Damping Sphere and the
  land-type replacements (Infernal Darkness / Reality Twist) finally see restricted land mana**. It
  previously counted flashback and subtype-spell mana but not creature-spell, chosen-subtype,
  instant/sorcery-only or copy mana, so Cavern of Souls under Infernal Darkness got a free any-colour
  prompt while `PotentialManaService.estimateLandManaAmount` — the AI mirror — already modelled it as
  one replaced mana. Verified against CR 106.6 ("This doesn't affect the mana's type"). Covered by
  `DampingSphereTest.multiManaLandWithSpendRestrictionProducesColorlessInstead`.
- `collectManaTypesFromEffects` / `collectManaColorsFromEffects` now treat a spend-restricted
  any-colour land as a source of all five colours, per CR 106.7 (what a permanent "could produce" is
  about the mana's type). Covered by
  `FellwarStoneTest.spendRestrictedOpponentLandContributesEveryColor`.
- `PotentialManaService.wouldManaAbilityTriggerChoice` collapses from a five-class list to one
  `instanceof`, so the AI's "safe" virtual pool now also skips the X, instant/sorcery-only and
  subtype-restricted abilities — all of which do open a colour prompt. Strictly more conservative.

**Guards that preserve behaviour deliberately**
- The revertable-mana-activation check (`ActivatedAbilityExecutionService:~330`) still requires
  `restriction == NONE`: the undo snapshot only covers the ordinary pool and creature mana, so a
  restricted-bucket activation must not be parked as revertable.
- The virtual-pool contributions in `PotentialManaService` (`buildVirtualManaPool` /
  `buildLandOnlyVirtualManaPool`) also require `restriction == NONE`, and `LandManaDrainSupport`
  skips restricted producers rather than laundering them into plain colorless.
- `hasOnTapManaEffects` now matches every restriction. Inert today — no card carries a restricted
  any-colour producer in `EffectSlot.ON_TAP`; all eleven are activated abilities.

---

## Phase 2 — Scope / enum parameterization, LOW risk

### Step 5 — Combat-requirement / must-attack — **DONE**

**Shipped**
```java
public record SetCombatRequirementThisTurnEffect(CombatRequirement requirement) implements CardEffect
// enum CombatRequirement { MUST_ATTACK, MUST_ATTACK_EFFECT_CONTROLLER, MUST_BLOCK,
//                          MUST_BE_BLOCKED, MUST_BE_BLOCKED_BY_ALL }

public record MustAttackNextTurnEffect(TauntTarget tauntTarget) implements CardEffect
// enum TauntTarget { EFFECT_CONTROLLER, SOURCE_PERMANENT }
```

Six records and six handlers deleted, two records + two enums + two handlers added. Cards (17 usages
across 18 files): Incite, Imp's Taunt, Courtly Provocateur (both abilities), Heckling Fiends,
Chemister's Trick, Alluring Siren, Norritt; Taunting Challenge, Alluring Scent, Revenge of the Hunted,
Lead; Enlarge, Emergent Growth, Deadly Allure; Nacatl Hunt-Pride, Mark for Death; Taunt, Gideon Jura.

**What held** — all four 5a handlers really are `findPermanentById` → null-guard → **one boolean
setter** → one log line, and the five flags are already one family on `Permanent` (declared
adjacently, copied together, cleared together in `resetModifiers()`). `MUST_ATTACK_EFFECT_CONTROLLER`
is the only branch that also sets `mustAttackTargetId`. The five log strings differ in *shape*, not
just wording (`GameLog.cardThen` / `builder().card().text()` / `textCardText` / plain `text`), so the
handler is a five-arm `switch` and every string is preserved byte-for-byte. 5b held line-for-line,
including the source-null early return, which is kept for `SOURCE_PERMANENT`.

**What the audit missed**
- **The 5a spec has a second axis.** `MustBlockThisTurnIfAbleEffect` declared
  `benign(creature(), new PermanentIsCreaturePredicate())` while the other three declared plain
  `benign(creature())`. Carrying the predicate on every branch would have narrowed targeted-trigger
  candidates for four cards, so `targetSpec()` branches on `requirement == MUST_BLOCK` instead.
- **5b absorbs two classes, not one.** The Step Index said 5 deleted; the two 5b siblings are both
  deletions, so the real count is 6 records + 6 handlers.

**Other call sites updated**
- `MustAttackUnlessControllerPaysManaValueEffectHandler` (Arcum's Whistle) injects the merged handler
  and builds `MUST_ATTACK` for its synthetic penalty entry — it was constructing the deleted record.
- `TargetPolarityClassifier` (magical-vibes-ai) loses three name-keyed entries and one `instanceof`
  in favour of a single `switch (requirement)` branch: attack/block requirements are `HARMFUL`,
  the two "must BE blocked" lures are `BENEFICIAL`. `TargetPolarityGuardTest`'s exhaustiveness
  ratchet covers the new shape.

**Tests** — no test referenced any of the six classes by name, and every branch already had card-level
coverage (both 5b ids are pinned by `TauntTest` / `GideonJuraTest`). Added the cross-contamination
assertions a `switch` merge needs: `CourtlyProvocateurTest.abilitiesImposeOnlyTheirOwnRequirement`
(same card, two requirements, neither leaks into the other three flags),
`AlluringScentTest.resolvingDoesNotSetTheIfAbleFlag`, and by-all/attack/block negatives in
`EmergentGrowthTest.resolvingBoostsAndSetsMustBeBlocked`.

**Do NOT merge** the non-`ThisTurn` siblings `MustBeBlockedIfAbleEffect` /
`MustBeBlockedByAllCreaturesEffect`: those are **static abilities** read off `EffectSlot.STATIC` at
block-legality time (`CombatBlockService:804,1421`; `GameQueryService:3493`), not one-shot flag
stamps. Different mechanism and lifetime. Still true after the merge.

---

### Step 6 — `SetLifeTotalEffect` — **DONE**

**Shipped**
```java
public record SetLifeTotalEffect(DynamicAmount amount,
                                 SetLifeTotalRecipient recipient) implements CardEffect
// sugar: (int, recipient), (DynamicAmount), (int) — the last two default to CONTROLLER
// enum SetLifeTotalRecipient { CONTROLLER, TARGET_PLAYER, EACH_PLAYER }
```

Six records and six handlers deleted; one record + one enum + one handler + one `DynamicAmount`
(`HighestLifeTotalAmongPlayers`) added, plus `AmountContext.withControllerId`. Cards: Form of the
Dragon, Invincible Hymn, Oketra's Last Mercy, Touch of the Eternal, Elderscale Wurm, Resolute
Archangel; Magister Sphinx, Sorin Markov, Vraska Relic Seeker, Torgaar; Worldfire, Biorhythm,
Arbiter of Knollridge. Argument order follows the `LoseLifeEffect` / `GainLifeEffect` siblings
(amount first, recipient second), not the plan's `(who, amount)`.

**What held** — all four listed handlers really do end in the same three statements, and the two
target records really are `Fixed(1)` / `Fixed(10)` / `Fixed(GameData.STARTING_LIFE_TOTAL / 2)` with
identical `TargetSpec.benign(player())`. `targetSpec()` branches on `recipient == TARGET_PLAYER`,
exactly as `LoseLifeEffect` already does. Torgaar's null-target no-op is preserved (its "up to one
target player" test still passes unchanged).

**What the audit missed**
- **Four records, not three.** The Step Index said 3 deleted; the body correctly listed four.
- **The each-player handlers gate the log on the total actually changing.** All three
  `SetEachPlayer*` handlers wrap the log in `currentLife != newLife`; the controller and
  target-player handlers logged unconditionally, emitting `"X's life total becomes 5 (was 5)."`.
  Unified on the gate — see the deliberate delta below.

**The stretch was taken.** `EACH_PLAYER` evaluates the amount **once per player**, re-pointing the
`AmountContext` at that player (new `withControllerId` wither) so a `CountScope.CONTROLLER` amount
reads "the creatures *they* control". That makes Biorhythm exactly
`PermanentCount(PermanentIsCreaturePredicate, CountScope.CONTROLLER)` — verified equivalent, since
`countPermanents` routes that predicate to the same `GameQueryService.isCreature` the deleted
handler called. Arbiter of Knollridge needed one new amount, `HighestLifeTotalAmongPlayers` (the
controller-*including* sibling of `HighestOpponentLifeTotal`); the merged handler determines every
player's new total **before** applying any of them, which is what keeps that cross-player amount
snapshotted. Worldfire's `Fixed(1)` is unaffected by per-player evaluation.

`CONTROLLER` and `TARGET_PLAYER` deliberately do **not** re-point the context: "target player's life
total becomes X" reads X from the controller's point of view, and `CountScope.TARGET_PLAYER` is the
existing way to read the target. No card exercises this today.

**Deliberate deltas** (both log/no-op only)
- The `currentLife != newLife` log gate now applies on every path, so a redundant
  `"X's life total becomes 5 (was 5)."` line is no longer emitted for the controller and
  target-player forms. No test asserted it; `applySetLifeTotal` already returned early in that case,
  so nothing else changes.
- The `Math.max(0, …)` clamp and the source-or-snapshot `AmountContext` lookup are now universal.
  No-ops for the two absorbed target-player forms, whose amounts were positive constants.

**Tests** — no test referenced any of the six classes by name, and every recipient already had card
coverage (Biorhythm asserts different per-player counts; Arbiter asserts the snapshot; Torgaar
asserts the null target). Added the cross-contamination assertions a `switch` merge needs:
`FormOfTheDragonTest.endStepLeavesOpponentLifeAlone` (CONTROLLER must not leak to EACH_PLAYER) and
`MagisterSphinxTest.etbLeavesTheUntargetedPlayerAlone` (TARGET_PLAYER must not leak either way).

---

### Step 7 — `SkipNextEffect` — **DONE**

**Shipped**
```java
public record SkipNextEffect(SkipKind kind, SkipRecipient recipient)
        implements CombatDamageTriggerContextEffect
// sugar: (SkipKind) -> recipient = CONTROLLER
// enum SkipKind      { TURN, UNTAP_STEP, DRAW_STEP, COMBAT_PHASE }
// enum SkipRecipient { CONTROLLER, DAMAGED_PLAYER, TARGET_PLAYER }
```

Four records and four handlers deleted, one record + two enums + one handler added. Cards (8):
Chronatog, Meditate; Ivory Gargoyle; Yosei, the Morning Star; Blinding Angel, False Peace,
Empty City Ruse, Stonehorn Dignitary.

**What held** — all four handlers really are the same eight lines (resolve a player id,
`playerIds.contains` guard, `merge(playerId, 1, Integer::sum)` on one of the four sibling counters,
one log line), and no consumer outside those handlers ever named the four records: no AI file, no
view, no serialization. Turn-engine consumers were untouched.

**What the audit missed**
- **Four records, not three.** The Step Index said 3 deleted.
- **`boolean targetsPlayer` cannot express the family.** The plan derived the affected id from
  `kind` (controller for Turn/DrawStep, target for UntapStep/CombatPhase), but Blinding Angel is
  `COMBAT_PHASE` with `targetsPlayer = false` and still reads `getTargetId()` — the combat-damage
  trigger bakes the damaged player in there. So `targetsPlayer` and "which id" are not the same
  axis, and keying the id on `kind` would have made *whose* occurrence is skipped a property of
  *what* is skipped — an accident of the current card pool ("target player skips their next turn"
  is a printable wording). Replaced by a 3-value `SkipRecipient`: `CONTROLLER` reads
  `controllerId`; `DAMAGED_PLAYER` and `TARGET_PLAYER` both read `targetId` and differ in whether
  the player was targeted. `targetsPlayer` is exactly `recipient == TARGET_PLAYER`.

**Deliberate deltas** (all three inert today)
- **Polarity unified on `benign`** instead of the planned `switch (kind)`.
  `SkipNextUntapStepEffect` was the family's only `harmful` spec while the three combat-phase
  cards were `benign`. Per `TargetSpec`'s own contract `harmful` means "protection from the source
  must be honoured" — damage / destroy / exile / sacrifice / fight — which a skipped step is not,
  and the flag is unreadable here regardless: `TargetValidationService:121` only runs
  `checkProtection` against a permanent target, `ValidTargetService:686` gates on
  `admits(PERMANENT)`, and `GameSimulator.rankAbilityTargets` only ranks *activated-ability*
  targets (none of the eight cards is one).
- **`combatDamageTriggerContext()` is now `null` except for `DAMAGED_PLAYER`.** The absorbed record
  returned `DAMAGED_PLAYER` unconditionally, including for its targeted form, which never sits in
  the `ON_COMBAT_DAMAGE_TO_PLAYER` slot. The merged record implements the interface for *every*
  kind, so this was checked against all four readers — `CombatDamageService:1219`,
  `TriggerCollectionService:801`, `SequenceEffect:77`, `SacrificeSelfThenEffect:38` — and every one
  calls the method rather than using `instanceof` as a bare marker, with `null` documented as "no
  special context". Yosei's `SequenceEffect` therefore still reports the same context it did when
  its untap step was a non-implementing record.
- The slf4j line for `COMBAT_PHASE` now reads "skips their next combat phase" rather than "will
  skip …", matching the game-log line it always disagreed with. Every **game-log** string is
  preserved byte-for-byte (one phrase per kind, shared by both sinks).

**Tests** — no test referenced any of the four classes by name, and every kind already had card
coverage. Added the cross-contamination assertions a `switch` merge needs — each kind must land on
its own counter and no other, and each recipient on the right player:
`MeditateTest.queuesNothingButTheTurnSkip`,
`IvoryGargoyleTest.queuesNothingButTheControllersDrawStepSkip`,
`YoseiTheMorningStarTest.queuesNothingButTheTargetPlayersUntapStepSkip`,
`BlindingAngelTest.flagsOnlyTheDamagedPlayersCombatPhase` (the `DAMAGED_PLAYER` branch is the one
where a merged handler could read the controller instead), and the sibling-queue negatives in
`FalsePeaceTest.resolvingFlagsTargetPlayer`.

**Do NOT merge**: `SkipDrawStepEffect` (static marker read by `StepTriggerService`, no handler),
`PlayersSkipUntapStepEffect` (global static, Sands of Time) and `SkipNextUntapEffect` (marks
per-`Permanent` `skipUntapCount`; the step still happens). The distinction is documented in
`SkipKind`'s javadoc and in each of their own. Still true after the merge.

---

### Step 8 — Phase-out + attached-counter placement — **DONE**

**Shipped**
```java
public record PhaseOutEffect(PhaseOutSubject subject) implements CardEffect
// enum PhaseOutSubject { SOURCE, TARGET, ATTACHED }

public record PutCounterOnReferencedPermanentEffect(PermanentReference reference,
                                                    CounterType counterType,
                                                    int count,
                                                    PermanentPredicate condition) implements CardEffect
// sugar: (CounterType), (CounterType, int), (CounterType, int, PermanentPredicate) — all ATTACHED;
//        (PermanentReference, CounterType)
// enum PermanentReference { ATTACHED, TRIGGERING }
```

Six records and six handlers deleted, two records + two enums + two handlers added. Cards (30):
Mist Dragon, Crystal Golem, Frenetic Efreet, Rainbow Efreet, Teferi's Honor Guard, Vaporous Djinn,
Warping Wurm; Reality Ripple, Sapphire Charm, Shimmering Efreet, Vision Charm; Vanishing —
Biting Tether, Consuming Fervor, Daily Regimen, Essence Flare, Forced Adaptation, Glistening Oil,
Krovikan Plague, Primal Cocoon, Sadistic Glee, Spirit Shackle, Torture, Unstable Mutation;
Ring of Evos Isle / Kalonia / Thune / Valkas / Xathrid; Freyalise's Winds.

**What held** — all three 8a handlers really do reduce to one `phasingService.phaseOut(gameData,
List.of(permanent))` varying only in the lookup, and `targetSpec()` switches on `subject` exactly as
`PutTargetOnTopOfLibraryEffect` does. All three 8b handlers really do end in the identical
`placeCounterOnPermanent(gameData, entry, creature, counterType, count)`, and Aura-vs-Equipment is
not a semantic axis — the two guards (`!aura.isAttached()` vs `getAttachedTo() == null`) are the
same test spelled two ways.

**The optional 8b fold was taken.** `PutCounterOnTriggeringPermanentEffect` is genuinely the same
shape with a different non-targeting reference, so it became `PermanentReference.TRIGGERING` rather
than a fourth class. The enum is deliberately named for Step 9 to extend with `SOURCE` when
`DestroyReferencedPermanentEffect` lands — the two families select a permanent the same way.
The plan's `PutCounterOnAttachedPermanentEffect` name would have lied once `TRIGGERING` was folded
in, hence `PutCounterOnReferencedPermanentEffect`.

**What the audit missed — `@CollectsTrigger` is class-keyed.** `TriggerCollectorRegistry` keys on
the exact `(EffectSlot, effect.getClass())` pair, so two collector annotations had to be re-keyed to
the merged class: `MiscTriggerCollectorService:238` (`ON_ENCHANTED_PERMANENT_TAPPED`, Spirit Shackle)
and `DeathTriggerCollectorService:1149` (`ON_ANY_CREATURE_DIES`, Sadistic Glee). Both stay
slot-scoped, and no card puts a `TRIGGERING` reference in either slot, so the re-key is inert; had it
been missed, both cards' triggers would have silently stopped firing. Neither trigger-collection
lookup nor `UPKEEP_TRIGGERED` (which is generic) needed anything else.

**Other call sites updated**
- `DestructionSupport:520` matched `elseEffect instanceof PhaseOutSelfEffect` for the "unless you pay
  {cost}, this creature phases out" fallback (Vaporous Djinn); now `instanceof PhaseOutEffect p &&
  p.subject() == SOURCE`.
- `TargetPolarityClassifier` (magical-vibes-ai) swaps its single name-keyed entry for an `instanceof`
  branch keyed on `subject`: `TARGET` is `HARMFUL_REMOVAL`, the two non-targeting subjects return
  `null` as they always did (they never reached `FIXED_BY_CLASS_NAME`).

**Deliberate delta** (one, inert)
- `PutCounterOnTriggeringPermanentEffectHandler` re-checked `gameQueryService.cantHaveCounters`
  before calling `placeCounterOnPermanent`, whose own first statement is that same check. The
  duplicate is dropped, not the guard.

The `condition` predicate is now available on every reference rather than only the Equipment form,
and the `ATTACHED` path gained the Equipment handler's slf4j fizzle lines (the Aura form logged
nothing). Both are no-ops for today's pool — no Aura or `TRIGGERING` card passes a condition.

**Keep separate**: `PhaseOutPermanentsEffect` (mass, filter-driven battlefield scan),
`PhaseOutSelfAndCombatOpponentEffect` (two permanents, combat-opponent reference) and
`PhaseOutChosenTypeNontokenPermanentsEffect`. `PreventPhaseOutTargetPermanentEffect` is the
protection marker, not a phase-out.

**Tests** — no test referenced any of the six classes by name, and all 30 cards already had test
classes. Added the cross-contamination assertions a `switch` merge needs, one per axis where a
misread is observable: `CrystalGolemTest.phasesOutOnlyItself` (SOURCE must not reach other
permanents), `ShimmeringEfreetTest.phasesInPresentsTargetChoice` gains "the Efreet stays" (TARGET
must not read `sourcePermanentId` — the strongest case, since that card's source *is* on the
battlefield), `SadisticGleeTest.counterLandsOnlyOnTheEnchantedCreature` (ATTACHED must not become
TRIGGERING: `ON_ANY_CREATURE_DIES` is exactly where a merged handler could reach for the dying
creature) and `FreyalisesWindsTest.windCounterLandsOnlyOnTheTappedPermanent` (the mirror).
Vanishing's existing `isPhasedOutIndirectly()` assertions already pin ATTACHED against a SOURCE
misread, and the Ring tests already pin the `condition` branch both ways.

---

### Step 9 — Destroy-referenced-permanent — **DONE**

**Shipped**
```java
public record DestroyReferencedPermanentEffect(PermanentReference reference,
                                               boolean cannotBeRegenerated) implements CardEffect
// sugar: (PermanentReference) -> cannotBeRegenerated = false
// enum PermanentReference { SOURCE, ATTACHED, TRIGGERING }   // SOURCE added by this step
```

Four records and four handlers deleted, one record + one handler added. Cards (13): Aether Storm,
Arachnus Web, Ice Cage; Aggression, Blight, Brink of Disaster, Hot Soup, Mortal Wound, Spinal Graft,
Spreading Algae, Yoke of the Damned; Suleiman's Legacy; Ajani Vengeant.

**What held** — all three destroy records really are `(boolean cannotBeRegenerated)` with a no-arg
ctor, no `targetSpec()` and no other members, and all three handlers really do reduce to one
`tryDestroyAndLog` varying only in which id fills the slot. `ENCHANTED` became
`PermanentReference.ATTACHED` exactly as the Step 8 note directed. The forcing function fired as
predicted: adding `SOURCE` broke `PutCounterOnReferencedPermanentEffectHandler`'s exhaustive
`switch`, and the answer is that a source-counter case does **not** belong in that family
(`PutCountersOnSourceEffect` owns it, and the engine materialises that record at runtime for several
other effects). Rather than only throwing from the handler arm, the record's **compact constructor**
rejects `SOURCE`, so the mistake fails at card-construction time; the switch arm throws as documented
dead code.

**What the audit missed — `@CollectsTrigger` is class-keyed, three times.** As in Step 8, the
collector annotations key on the exact `(EffectSlot, effect.getClass())` pair, so three had to be
re-keyed to the merged class: `MiscTriggerCollectorService:199` (`ON_ENCHANTED_PERMANENT_TAPPED`),
`DeathTriggerCollectorService:1133` (`ON_ANY_CREATURE_DIES`) and `DamageTriggerCollectorService:295`
(`ON_ENCHANTED_CREATURE_DEALT_DAMAGE`). Two of those slots now hold **two** merged-class collectors
each — Step 8's `PutCounterOnReferencedPermanentEffect` and this one — which is fine because the
registry key includes the class. Each re-key widens the key from "the enchanted-destroy record" to
"any referenced-destroy record" in that slot; no card puts a `SOURCE` or `TRIGGERING` reference in
any of the three slots, so the widening is inert. Had the re-key been missed, Spreading Algae /
Blight / Brink of Disaster, Yoke of the Damned and Mortal Wound / Hot Soup would have silently
stopped triggering.

**Other call sites updated**
- `DestructionSupport:552` matched `elseEffect instanceof DestroySourcePermanentEffect` for the
  "unless you pay {cost}, destroy this" fallback (Musician); now `instanceof
  DestroyReferencedPermanentEffect d && d.reference() == SOURCE`.
- `DestroyUnlessPaysPerCounterEffectHandler:47` **constructs** the deleted record at runtime for that
  same fallback — it is not only a read site.
- `BoardEvaluator:294`'s `DestroyPermanentsTargetPlayerControlsEffect` branch is deleted outright:
  the `DestroyAllPermanentsEffect` branch twelve lines above it is character-for-character the same
  test once `scope() == TARGET_PLAYER` is folded in.
- `TargetPolarityClassifier` needed nothing — it classifies *permanent*-target polarity, and neither
  effect targets a permanent.

**The Ajani half — the plan's prescription was wrong in one direction and short in another.**
`DestroyPermanentsTargetPlayerControlsEffect` → `DestroyAllPermanentsEffect(PermanentIsLandPredicate,
TARGET_PLAYER, null)` is indeed a straight record swap, but:
- **No player target filter is needed.** Rain of Daggers and Overwhelming Forces carry
  `PlayerPredicateTargetFilter(OPPONENT)` because their oracle text says "target *opponent*".
  Ajani's −7 says "target player", which `PlayerRelation.ANY` would express as a no-op.
- **`DestroyAllPermanentsEffect` was missing its `targetSpec()`, and that is load-bearing.**
  `ActivatedAbility.isNeedsTarget()` (`ActivatedAbility:374`) is derived **only** from the effects'
  `targetSpec()` — it never consults `getTargetFilter()`. Swapping the record without adding a spec
  would have made Ajani's ultimate stop asking for a target, resolve with a null `targetId`, and do
  nothing. The two sibling effects that share `EachPermanentScope` already branch their spec on the
  scope (`DealDamageToEachMatchingPermanentEffect:26`,
  `PutCounterOnEachMatchingPermanentEffect:28`); `DestroyAllPermanentsEffect` was the odd one out.
  It now returns `TargetSpec.harmful(TargetPredicates.player())` for `TARGET_PLAYER` and
  `TargetSpec.NONE` otherwise, which keeps Ajani identical and **fixes a latent bug for Rain of
  Daggers and Overwhelming Forces**: neither declared a target through any effect, so
  `EffectResolution.needsTarget(card)` was false and the client was never told to pick a player.
  Both cards' tests pass a target explicitly, which is why nothing caught it. Validation is
  unaffected — a bare `player()` predicate has no `permanentRestriction()`, so `validateSpec`
  neither demands a target nor runs a protection check.

`SpellEvaluator.evaluateDestroyAllValue` was deliberately **not** made scope-aware. Ajani's ultimate
now reaches it where the deleted record had no branch at all, but that method scores nonland
permanents by mana value and lands are almost all MV 0, so the ultimate scores ~0 either way. The
pre-existing scope blindness — it subtracts the AI's own board for a target-player wipe, mis-scoring
Rain of Daggers and Overwhelming Forces — is a separate AI bug, not this merge's to fix.

**Deliberate deltas** (both inert)
- The merged handler keeps the three absorbed destroy handlers' silence rather than adopting the
  counter sibling's slf4j fizzle lines. Every game-log string is unchanged.
- The `SOURCE` path gained a null-id guard before `findPermanentById`, which the two absorbed
  handlers lacked. It cannot change an outcome, only skip a lookup.

**Kept separate**: `DestroyLinkedPermanentEffect` (Merieke Ri Berit) as the plan says — it also
clears `source.setChosenPermanentId(null)` and carries a baked `UUID`.
`DestroyCreatureAttachedToEnchantedEquipmentEffect` (Artificer's Hex) is a two-hop
Aura→Equipment→creature walk, not a `PermanentReference`.
`DestroyOtherPermanentsWithEnteringNameEffect` reads the triggering permanent but destroys everything
*else*.

**Tests** — no test referenced any of the four classes by name, and all 13 cards had test classes.
Added the cross-contamination assertions a `switch` merge needs, one per axis where a misread is
observable: `ArachnusWebTest.destroyedAtEndStepWhenPowerIsFourOrGreater` and
`IceCageTest.destroyedWhenEnchantedCreatureTargetedByAbility` gain "the enchanted creature survives"
(SOURCE must not read `attachedTo` — the strongest case, since both sources *are* attached, and Ice
Cage's ability variant is the one that leaves the creature alive to check),
`SuleimansLegacyTest.enteringDjinnIsDestroyed` gains "Suleiman's Legacy itself survives" (TRIGGERING
must not read `sourcePermanentId`), and `AjaniVengeantTest.minusSevenSparesTargetPlayersNonlands` is
new (the record swap moved the filter onto the `FilterContext`-aware evaluation path). ATTACHED is
already pinned against a SOURCE misread by the existing host-dies assertions in
`YokeOfTheDamnedTest`, `MortalWoundTest`, `SpreadingAlgaeTest` and `SpinalGraftTest` — and
`YokeOfTheDamnedTest.anotherCreatureDyingDestroysEnchantedCreature` pins it against a TRIGGERING
misread too, since the triggering creature is already dead there.

Ran green: the 13 card tests plus `RainOfDaggersTest`, `OverwhelmingForcesTest`, `MusicianTest`,
`PhantasmalSphereTest`, `TargetPolarityGuardTest`, `AiManaManagerTest`.

---

### Step 10 — Search-target-library + sacrifice costs — **DONE**

**Shipped**
```java
public record SearchTargetLibraryEffect(DynamicAmount count,
                                        CardPredicate filter,
                                        LibrarySearchDestination destination,
                                        boolean canFailToFind) implements CardEffect
// sugar: (int count, CardPredicate, LibrarySearchDestination, boolean)
// compact ctor rejects any destination outside
//   { EXILE, GRAVEYARD, BATTLEFIELD_UNDER_SEARCHER, EXILE_PLAYABLE, EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP }
```
plus `SacrificeArtifactCost` → `SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false)`.

**10a** — four records and four handlers deleted, one record + one handler added. Cards (7):
Jester's Cap, Earwig Squad, Nightmare Incursion; Life's Finale; Bribery; Praetor's Grasp,
Grinning Totem.

**What held** — every branch really does reduce to one `sendLibrarySearchToPlayer` with a
`LibrarySearchParams.builder`, and the per-card pick loop really is driven entirely by `destination`
in `LibraryChoiceHandlerService`. `targetSpec()` was already `TargetSpec.benign(player())` on all
four and is unchanged. No AI, view or serialization file named any of the four records.

**What the audit missed**
- **`.destination(...)` was not the only varying argument.** The candidate list varied (whole deck
  for the unfiltered exile form, pre-filtered for the other two), the *filter mechanism* varied
  (`Set<CardType>` + `.filterCardTypes(...)` for graveyard vs `CardPredicate` + the
  `PredicateEvaluationService` for battlefield), `remainingCount` varied (clamped to the deck size /
  raw / never set), and so did `canFailToFind`, the prompt wording and `.sourceCards(...)`. They all
  reduce to the four components, but the merge is not the one-line switch the audit described.
- **The two filter mechanisms are already unified downstream.** `LibraryChoiceHandlerService:878`
  gives `filterPredicate` priority over `filterCardTypes`, and a `CardTypePredicate` resolves to
  `Card.hasType` — character-identical to `LibrarySearchSupport.matchesCardTypes`. Life's Finale
  therefore carries `new CardTypePredicate(CardType.CREATURE)`, and the `filterCardTypes` field stays
  for its remaining user, `SearchLibraryForCardTypeToExileAndImprintEffectHandler`.
- **There was a fourth sibling.** `SearchTargetLibraryForCardToExileWithPlayPermissionEffect`
  (Praetor's Grasp, Grinning Totem) is the same shape whose only component,
  `boolean expiresAtNextUpkeep`, its own handler already spelled as a *destination* pair
  (`EXILE_PLAYABLE` / `EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP`). Absorbed: **4 records + 4 handlers, not
  3.** Its `.sourceCards(List.of(entry.getCard()))` is derived from the destination — Step 3's
  derived-axis pattern — because only the until-next-upkeep branch reads it back
  (`LibraryChoiceHandlerService:749`).
- **`canFailToFind` is not derivable from `filter != null`.** Nightmare Incursion is an unfiltered
  "up to X" search, so it must be able to fail to find with a null filter. It stays an explicit
  component, which is also what makes it document the rule split: `false` = a bare quantity, which
  must be found if present (CR 701.23d — Jester's Cap, Earwig Squad, Praetor's Grasp, Grinning
  Totem); `true` = a stated quality or an "up to" wording (CR 701.23b).

**The reconcile the step asked for — `isSearchPrevented` was the wrong one.** It shuffles the
**searching player's own** library, which is right for a self-search and wrong for every card in this
family: the instruction is "then *that player* shuffles", i.e. the library that was to be searched.
Unified on the Bribery handler's shape (`checkSearchRestriction` → shuffle the target → log
`"<target>'s library is shuffled."`). **Deliberate behaviour change** for the six cards that used
`isSearchPrevented`; covered by `JestersCapTest.preventedSearchShufflesOnlyTheTargetsLibrary`, which
asserts both halves — the target's shuffle log fires and the searcher's own library is untouched.

**Other deliberate deltas** (all prompt/log only)
- `remainingCount` is now `min(count, candidates.size())` on every destination; only the exile form
  clamped before. For Jester's Cap this is byte-identical; for Life's Finale with fewer than three
  creatures it changes which tail-log branch fires (`"puts cards into their graveyard for X"` instead
  of `"finds no more matching cards"`). Both shuffle and both run the same follow-ups.
- The two "found nothing" strings unified on the graveyard wording
  (`"… but finds no matching cards. Library is shuffled."`), which `LifesFinaleTest` already
  asserts; Bribery's differently-worded line is the one that changes.
- Prompts are now built from `CardPredicateUtils.describeFilter`, so the exile and graveyard prompts
  stay byte-identical ("a card" / "a creature card") and Bribery's gains "creature".
- The `count <= 0` early return and the universal clamp mean an empty-after-filter library and a zero
  count are now two distinct log lines on every destination.

**10b** — one record and one handler deleted (`SacrificeArtifactCost`,
`ArtifactSacrificeCostHandler`) across 15 cards: Atog, Barrage Ogre, Etherium Astrolabe, Ferrovore,
Gnathosaur, Kuldotha Rebirth, Orcish Vandal, Oxidda Daredevil, Phyrexia's Core, Piston Sledge,
Rusted Slasher, Sage of Lat-Nam, Shrapnel Blast, Throne of Geth, Trading Post.
`AdditionalSpellCostService.ExtractedCosts` loses its `sacrificeArtifact` field (and with it the
`extractAndRemove` / `any()` / `satisfiable` / `validateAll` arms), `SpellCastingService` loses its
payment arm and its graveyard-cast reject arm, and `AbilityActivationService` loses one
`toPermanentChoiceCostHandler` line. The AI mock-suite stub `AiTestPlayabilityStub` gains a
`SacrificePermanentCost` arm restricted to the artifact predicate, replacing its
`SacrificeArtifactCost` one.

**Deliberate deltas**
- The satisfiability path was already identical (`isArtifact(gameData, p)` on both sides), but the
  **payment and validation paths widen**: `gameQueryService.isArtifact(permanent)` (natural + granted
  card types) becomes `matchesPermanentPredicate` → `isArtifact(gameData, permanent)`, the
  layer-aware form that also sees static card-type grants. Rules-correct, and it removes a
  disagreement between satisfiability and payment.
- Error and prompt strings become the shared ones: `"No permanent to sacrifice matching: an
  artifact"` (was `"No artifact to sacrifice"`) and `"Choose a permanent to sacrifice (an
  artifact)."` (was `"Choose an artifact to sacrifice."`). Eight card tests asserted the old error
  and were updated.
- `excludeSource=false` is load-bearing — the merged handler excludes the source when it is `true`,
  and four of the 15 cards are artifacts whose own ability may sacrifice them. Already pinned by
  `ThroneOfGethTest."Can sacrifice itself as the only artifact"` and
  `EtheriumAstrolabeTest."Can sacrifice itself to pay the ability and still draws"`.

**The stretch was NOT taken.** Folding `SacrificeMultiplePermanentsCost` into `SacrificePermanentCost`
is not the one-field addition the audit describes: the two are separate `ExtractedCosts` fields fed by
separate `AdditionalSpellCostService` accumulation and validation paths, are paid through different
`SpellCastingService` methods against different `CostSelection` fields (`sacrificePermanentId` vs
`sacrificePermanentIds`), and branch to genuinely different flows in `ForcedCostOrElseEffectHandler`
(`:124` vs `:134`) and `MayPenaltyChoiceHandlerService` (`:1340` vs `:1369`). `SacrificePermanentCost`
would also grow to seven components. It needs its own step, not a stretch on this one.

**Do NOT merge** `SacrificeCreatureCost` (86): it overrides `sacrificesChosenCreature() -> true`, is
special-cased in `SpellCastingService` and `AbilityActivationService`, and carries a
`ManaColor trackSacrificedColorSymbols` field.

**Tests** — no test referenced any of the five deleted classes by name except the deleted
`ArtifactSacrificeCostHandlerTest` and the AI mock-suite fixtures. Added the cross-contamination
assertions a destination-switch merge needs, one per axis where a misread is observable:
`JestersCapTest.preventedSearchShufflesOnlyTheTargetsLibrary` (the rules fix, both halves),
`JestersCapTest.cannotDeclineToFind` (the `canFailToFind=false` arm, which Life's Finale's and
Bribery's existing decline tests pin the other way),
`LifesFinaleTest.foundCreatureLandsOnlyInTheTargetsGraveyard` (GRAVEYARD must reach neither exile nor
the caster's graveyard), and exile/graveyard negatives in
`BriberyTest.putsChosenCreatureUnderControl` (BATTLEFIELD_UNDER_SEARCHER must not fall through). The
`EXILE_PLAYABLE` pair is already pinned by `PraetorsGraspTest` / `GrinningTotemTest`, and the
face-down-vs-face-up split by `JestersCapTest.exilesThreeCards`.

---

### Step 11 — Tap / untap scopes and costs — **DONE**

**Shipped**
```java
public record UntapPermanentsEffect(TapUntapScope scope, PermanentPredicate filter, int chosenCount)
        implements CardEffect
// sugar: (scope), (scope, filter) — both chosenCount = 0

public record TapMultiplePermanentsCost(DynamicAmount count, PermanentPredicate filter,
                                        boolean excludeSource) implements CostEffect
// sugar: (int, filter), (int, filter, excludeSource) — both wrap Fixed
// + new @Component service/ability/cost/TapCostSupport
```

Three records and three handlers deleted, one support component added. Cards (6): Thornbite Staff,
Paralyze, Dance of the Dead; Rewind, Unwind; Aryel, Knight of Windgrace. The survivors' existing call
sites were untouched — all 156 `UntapPermanentsEffect` and 31 `TapMultiplePermanentsCost` uses still
mean the same thing.

**What held**
- **11a** — `UntapEquippedCreatureEffectHandler.resolve` really is `resolveEnchanted` with the two
  null guards fused into one `||`. `ENCHANTED` reads `getAttachedTo()` off the source permanent
  whether that permanent is an Aura or an Equipment, which is why 2 of the 3 "equipped" users were
  Auras all along.
- **11b** — the up-to-N flow moved into `resolveControlled`'s `chosenCount > 0` branch verbatim,
  `MultiPermanentChoiceContext.UntapChosenPermanents` included, so the `9b8147333` fix in the
  regression table is preserved. `targetSpec()` needed nothing: `CONTROLLED` already fell through to
  `TargetSpec.NONE`, which is what the absorbed record had.
- **11c** — `getValidChoiceIds`, `validateAndPay` and `getPromptMessage` really are
  character-identical across the two cost handlers.

**What the audit missed**
- **The `ON_ANY_CREATURE_DIES` collector was already occupied.** Steps 8 and 9 both had to *re-key* a
  class-keyed `@CollectsTrigger` onto the merged class; here `DeathTriggerCollectorService` already
  carried an `UntapPermanentsEffect` collector on that slot for Galvanic Juggernaut's `SELF` untap,
  and it is character-identical to the Equipment one (same `StackEntry`, same
  `logAnyCreatureDeath`). Re-keying would have registered two collectors under the same
  `(slot, class)` key; the equipment method is simply deleted and Thornbite Staff now rides the
  surviving one. Its comment gained the `ENCHANTED` case.
- **`requiredCount()` has no game state, so the handler cannot evaluate a `DynamicAmount`.** The
  plan's "widen `count` to `DynamicAmount`" stops one step short: the interface method takes no
  arguments, and widening it would touch all 13 `PermanentChoiceCostHandler` implementations. The
  count is therefore evaluated **by the caller** — new `@Component TapCostSupport`, which builds an
  `AmountContext` from the source permanent, its controller and the activation `xValue` — and handed
  to `MultiplePermanentTapCostHandler` as a plain int. That forced
  `AbilityActivationService.toPermanentChoiceCostHandler` to take `GameData` (both overloads, 10 call
  sites, all of which already had it in scope) and `MayAbilityTapCostService` to inject the support
  at its two construction sites. `MultiplePermanentTapCostHandler`'s 4-arg convenience constructor
  had no user left and is gone.
- **The AI's refusal cannot key on the merged class.** `AiDecisionEngine.acceptsAbilityCosts` refused
  every `TapXPermanentsCost` because the AI has no way to announce an X. Widened naively that would
  have stopped the AI activating all 26 fixed-count tap-cost cards, so the branch is now
  `TapMultiplePermanentsCost c && !(c.count() instanceof Fixed)` — the derived-axis pattern again.

**Deliberate deltas** (all three inert or log-only)
- The `ENCHANTED` game log replaces `GameLog.textCardText(sourceName + " untaps ", host, ".")` with
  `GameLog.cardTextCard(entry.getCard(), " untaps ", host, ".")` — a card chip for the source instead
  of a plain name, matching what the other ten untap scopes already emit and the same delta Step 3
  took. No test asserted the old string. With it goes the `"Equipment"` fallback for a null
  `entry.getCard()`, which every other scope already assumes is present.
- The `xValue <= 0` early return in `TapXPermanentsCostHandler.validateCanPay` is
  dropped as redundant: with `requiredCount == 0`, `validIds.size() < 0` is never true, so the merged
  method returns in the same place. Pinned by `AryelKnightOfWindgraceTest.secondAbilityWithXZero`.
- `TapCostSupport` passes the source permanent and its controller into the `AmountContext` even
  though neither `Fixed` nor `XValue` reads them, so a counting amount would evaluate correctly if
  one is ever used. For a graveyard-activated ability there is no source permanent and both are null,
  which only `Fixed` reaches today.

**The MED stretch was NOT taken.** `TapCreatureCost` is not "the same handler with
`requiredCount() == 1`": its `getPromptMessage` ignores `remaining` ("Choose an untapped creature to
tap."), its `validateCanPay` and all four `validateAndPay` rejections carry different strings, and
the `isCreature` check is in the *handler* rather than the predicate — so folding it in changes the
prompt and five error messages for 13 cards and needs a `PermanentAllOfPredicate(creature, …)`
wrapper on each. `trackTappedCreaturePower` is also read by two `instanceof TapCreatureCost` sites
(`AbilityActivationService:2197,2389`) and is only well-defined at count 1. It needs its own step.

**Do NOT merge** `TapPermanentsEffect` (135) with `UntapPermanentsEffect` (156): both are already
consolidated survivors, and their scopes genuinely diverge (`ALL_TARGETS` is a dead path for tap but
a live multi-target scope for untap). 291 files of churn for a mode flag. Still true after the merge —
`chosenCount` now exists on both, which makes them look closer than they are.

**Tests** — no test referenced any of the three deleted classes by name, and every branch already had
card coverage (Rewind/Unwind pin the up-to-N prompt from four angles including declining entirely;
Aryel pins X = 0/1/2 and the more-Knights-than-X choice; Thornbite/Paralyze/Dance pin the ENCHANTED
untap). Added the two cross-contamination assertions a scope merge needs:
`ThornbiteStaffTest.untapsTheEquippedCreatureAndNotTheStaff` (ENCHANTED must not read
`sourcePermanentId` — the strongest case, since the Staff itself is on the battlefield and is tapped
here) and `VitalizeTest.untapsWithoutPromptingForAChoice` (the `chosenCount == 0` mirror: a plain
CONTROLLED untap must not fall into the up-to-N prompt branch).

Ran green: the 6 card tests plus `TeferiHeroOfDominariaTest`, `GalvanicJuggernautTest`,
`EnterTriggerCollectorServiceTest`, `CostEffectClassificationTest`, the other 22
`TapUntapScope.CONTROLLED` cards (`VitalizeTest` included), all 31 `TapMultiplePermanentsCost` cards, `SwarmIntelligenceTest`,
`ClovenCastingTest`, `TapCreatureCostHandlerTest` and `MultiplePermanentSacrificeCostHandlerTest`.

---

### Step 12 — `Grant*` low-risk batch — **DONE**

**Shipped**
```java
public record GrantChosenKeywordEffect(List<Keyword> options, GrantScope scope) implements CardEffect
// compact ctor rejects every scope but SELF / TARGET

public record GrantControllerKeywordEffect(Keyword keyword) implements CardEffect
// static marker, no handler; compact ctor rejects every keyword but SHROUD / HEXPROOF

public record GrantSpellCastingAbilityToSpellsEffect(Keyword grantedAbility, CardPredicate filter)
        implements SpellCastingAbilityGrantingEffect
// no handler; compact ctor rejects every ability but CONSPIRE / CONVOKE

public record GrantProtectionChoiceUntilEndOfTurnEffect(boolean includeArtifacts,
                                                        boolean targetControllerChooses,
                                                        GrantScope scope,
                                                        PermanentPredicate filter) implements CardEffect
// + the four existing sugar ctors (filter = null) and a new (GrantScope, PermanentPredicate)

public record GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration duration) implements CardEffect
// enum NoMaximumHandSizeDuration { REST_OF_GAME, UNTIL_NEXT_TURN }
```

Nine records and five handlers deleted, four records + one enum + two handlers added — net −5 records
and −3 handlers. Cards (21): Urza's Avenger, Illusionary Presence, Golem Artisan, Practiced Offense;
True Believer, Ivory Mask, Leyline of Sanctity, Witchbane Orb, Shalai, Spirit of the Hearth;
Wort the Raidmother, Chief Engineer; Brave the Elements; Praetor's Counsel, Tamiyo the Moon Sage,
Wrenn and Seven, Enter the Infinite.

**What held** — every one of the five premises survived reading the records *and* the handlers.
The two `GrantChosenKeyword*` handlers really are the same file bar the id lookup, and the SELF
lookup's `sourcePermanentId != null ? … : targetId` fallback is preserved verbatim. The two
`GrantControllerKeyword*` records really are empty markers with `playerHasShroud` / `playerHasHexproof`
as their only consumers. `GrantSpellCastingAbility*` really is dispatch-free: both consumers
(`GameActionAvailabilityService:453`, `SpellCastingService:165`) match on the capability interface, so
the merge touched no engine logic at all. `GrantProtectionChoice*` really does terminate in one
`beginProtectionColorChoice`, and the mass form slots into the surviving handler's existing
`resolveRecipientIds` switch as a third arm. `GrantNoMaximumHandSize*` really is a one-line set insert.

**Two corrections to the plan**
- **The hand-size row is a rename, not an in-place extension.** The plan said
  "`GrantPermanentNoMaxHandSizeEffect` + duration", but "Permanent" there means *permanently*, so the
  name would have contradicted its own `UNTIL_NEXT_TURN` value — the same trap Step 8 avoided by
  renaming to `PutCounterOnReferencedPermanentEffect`. Both records are deleted and
  `GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration)` replaces them, so this row deletes 2 and
  adds 1 rather than deleting 1.
- **The `SpellCastingAbilityGrantingEffect` interface is deliberately kept** even though it now has a
  single implementor. It is the capability contract `ARCHITECTURE.md` requires the engine to read
  instead of a concrete type, and the plan's own note names it as the shared read surface. Deleting it
  would be the only way to reach the Step Index's count of 6.

**Three guards added — this step's real content beyond the merges.** Each merged enum axis has values
the family cannot express, and in every case the mistake is *silent*: a scope with no handler arm, a
keyword no query reads, an ability no cost gate consults. Following Step 9's precedent
(`DestroyReferencedPermanentEffect` rejecting `SOURCE`), all three reject in the **compact
constructor**, so the mistake fails at card-construction time rather than resolving to a no-op:
`GrantChosenKeywordEffect` accepts only SELF / TARGET, `GrantControllerKeywordEffect` only SHROUD /
HEXPROOF, `GrantSpellCastingAbilityToSpellsEffect` only CONSPIRE / CONVOKE.
`GrantProtectionChoiceUntilEndOfTurnEffect` gets both halves: scope limited to TARGET / SELF /
OWN_CREATURES, and a non-null `filter` rejected on any scope but OWN_CREATURES (it is only read
there). Widening any of these means wiring the consumer at the same time.

**Other call sites updated**
- `GameQueryService` gained `playerBattlefieldGrantsControllerKeyword`, the keyword-matching sibling of
  the existing class-keyed `playerBattlefieldHasStaticEffect`; `playerHasShroud` / `playerHasHexproof`
  are still one-liners and every caller is untouched.
- `TargetPolarityClassifier` (magical-vibes-ai) re-keys its single name entry from
  `GrantChosenKeywordToTargetEffect` to `GrantChosenKeywordEffect`. No `instanceof` branch was needed:
  the SELF form appears only in an activated ability and an `UPKEEP_TRIGGERED` slot, never in
  SPELL / ON_ENTER_BATTLEFIELD, and the sibling `GrantProtectionChoiceUntilEndOfTurnEffect` entry has
  already been name-keyed across its own scopes since Step 8. `TargetPolarityGuardTest` passes.
- **Nothing else.** Unlike Steps 8 and 9, no `@CollectsTrigger` collector is keyed on any of these
  nine classes — all five families live in SPELL / STATIC / activated-ability slots — so there was no
  re-key to miss.

**Deliberate delta** (one)
- **The until-next-turn grant now writes a game-log line.** The rest-of-game handler logged
  `"X has no maximum hand size for the rest of the game."`; the until-next-turn one resolved silently.
  A duration `switch` that logs one arm and not the other is the kind of asymmetry that reads as a bug,
  so both log, with the period as the only varying phrase (`"… for the rest of the game."` /
  `"… until their next turn."`). The rest-of-game string is byte-for-byte unchanged. Covered by
  `GrantNoMaximumHandSizeEffectHandlerTest.logsRestOfGameGrant` / `logsUntilNextTurnGrant`.

**Tests** — `TrueBelieverTest.grantsControllerShroudOnBattlefield` was a white-box wiring test
(`getEffects(STATIC)` + `instanceof`) of exactly the kind `CLAUDE.md` forbids; it is deleted rather
than repointed, since the same file already asserts the shroud behaviourally three ways.
`GameQueryServiceTest` and the hand-size handler test are repointed at the merged records, the latter
renamed and extended to assert **each duration lands on its own set and not the other**.

The cross-contamination assertions a `switch` merge needs were added only where an axis was not
already pinned both ways:
- `GolemArtisanTest.keywordLandsOnlyOnTheTarget` — TARGET must not read `sourcePermanentId`. The
  strongest case in the family: the Artisan's own permanent is on the battlefield while it targets
  another creature, so a merged handler reaching for the source would have looked correct until this
  assertion. The SELF direction is already pinned by Urza's Avenger and Illusionary Presence, whose
  abilities carry no target at all (a `targetId` read yields null and no prompt).
- `StaveOffTest.protectionLandsOnlyOnTheTarget` — TARGET must not widen into the OWN_CREATURES scan.
  The reverse is already pinned: `BraveTheElementsTest` asserts the non-white own creature and the
  opponent's creature are both untouched, and would fail outright if OWN_CREATURES were read as TARGET
  (no target id → no prompt).
- The SHROUD / HEXPROOF axis needed nothing — it is already pinned in **both** directions by existing
  behavioural tests. `IvoryMaskTest` / `TrueBelieverTest` assert the controller cannot target
  themselves (fails if SHROUD is misread as HEXPROOF); `LeylineOfSanctityTest` /
  `SpiritOfTheHearthTest` assert the controller still can (fails if HEXPROOF is misread as SHROUD).

Ran: the 29 affected card test classes plus `GameQueryServiceTest`,
`GrantNoMaximumHandSizeEffectHandlerTest` and `TargetPolarityGuardTest`. All green.

**Do NOT merge**: `GrantProtectionFromCardTypeUntilEndOfTurnEffect` vs
`GrantProtectionFromColorUntilEndOfTurnEffect` — different `Permanent` fields with different
lifetimes (`getProtectionFromCardTypes()` is not an until-EOT bucket and is also written by
`ChoiceHandlerService`). Still true after the merge.

---

### Step 13 — Cost-modification batch — **DONE**

**Shipped**
```java
public record IncreaseSpellCostEffect(CardPredicate predicate,
                                      int amount,
                                      CostModificationScope scope) implements CardEffect
// reuses the existing enum: CostModificationScope { SELF, OPPONENT, ALL }

public record LimitSpellsPerTurnEffect(int maxSpells, SpellLimitScope scope) implements CardEffect
// enum SpellLimitScope { EACH_PLAYER, CONTROLLER, ENCHANTED_PLAYER }

public record ReduceOwnCastCostIfTargetingPermanentEffect(PermanentPredicate predicate,
                                                          int amount,
                                                          boolean controlledByCaster) implements CardEffect
// sugar: (predicate, amount) -> controlledByCaster = false
```

Six records and three handlers deleted, one enum added; no new record and no new handler. Cards (17):
Thalia Guardian of Thraben, Chill, Gloom, Feroz's Ban, Thorn of Amethyst, Irini Sengir, Derelor,
Aura of Silence; Rule of Law, Arcane Laboratory, Colfenor's Plans, Curse of Exhaustion;
Heartless Summoning; Savage Stomp, Ajani's Response.

**What held** — all four premises did. The three `Increase*` handlers really are the same two
statements with an added `source.controlledBy(context.castingPlayerId())` guard and its negation; the
three `LimitSpells*` reads really are three adjacent `Math.min` branches in the single consumer
`CastingPermissionService.getMaxSpellsPerTurn`, differing only in the applies-to guard; and the two
`ReduceOwnCastCostIfTargeting*Permanent*` records really are the same `(PermanentPredicate, int)`
consumed only by `CastingCostService.computeTargetBasedCostReduction` and
`GameActionAvailabilityService`. No AI file, view, or serialization site named any of the six.

**What the audit missed**
- **The tax scope already existed.** The plan prescribed a new `CostTaxScope {ALL_PLAYERS,
  CONTROLLER, OPPONENTS}`, but `ReduceCastCostForMatchingSpellsEffect` — the exact reduction-side
  mirror of this family, in the same `costmod` package — has carried
  `CostModificationScope {SELF, OPPONENT, ALL}` since before this plan. A second three-value enum
  meaning the same three things would have been the duplication the plan exists to remove, so the
  survivor reuses it and the two handlers are now the same five-line `switch`. That also makes the
  13c row a pure record swap: Heartless Summoning is `ReduceCastCostForMatchingSpellsEffect` with a
  `CardTypePredicate(CREATURE)` and `SELF`, so **`ReduceOwnCastCostForCardTypeEffect`'s handler is
  deleted outright rather than merged** — hence 3 handlers for 6 records.
- **The net count is two low.** The Step Index said 4 deleted; the body's own rows list six.
- **No default scope.** Both merged records take their scope explicitly at all 17 call sites rather
  than defaulting the previous meaning through a 2-arg sugar. `ReduceCastCostForMatchingSpellsEffect`
  already requires it, and after the merge a bare `IncreaseSpellCostEffect(pred, 1)` would have been
  the one cost modifier in the package whose scope you cannot read at the call site. The
  `controlledByCaster` flag does keep a `false` sugar — there the survivor's name still says what the
  default is.

**Deliberate deltas — two rules fixes, both `getType()` to `hasType` (CR 205.2b verified: "Some
objects have more than one card type … Such objects satisfy the criteria for any effect that applies
to any of their card types")**
- **Heartless Summoning now discounts artifact creature spells.** This is the change the step
  flagged, and the oracle text settles it: "Creature spells you cast cost {2} less to cast" — an
  artifact creature spell **is** a creature spell. The absorbed handler tested
  `Set.of(CREATURE).contains(spell.getType())`, and `TypeLineParser` assigns the *first* type word as
  the primary type, so "Artifact Creature" parses as `type=ARTIFACT, additionalTypes={CREATURE}` and
  every artifact creature silently missed the discount. Covered by
  `HeartlessSummoningTest.artifactCreatureSpellsAreReduced` (Juggernaut, {4} to {2}) and its negative
  `nonCreatureArtifactSpellsNotReduced` (Angel's Feather, a plain artifact, still full price).
- **Aura of Silence is unchanged in practice, and the audit's worry was the wrong way round.** The
  step asked to confirm the multi-type card "still matches" under `CardTypePredicate`; it does, and
  strictly more so. Because canonical type lines put Artifact and Enchantment *before* Creature, every
  card with either type already had it as its primary type, so the old `Set.contains(getType())` and
  the new `hasType` agree across today's pool. Pinned by
  `AuraOfSilenceTest.opponentArtifactCreaturesCostMore`.
- The merged reduce path evaluates its amount against the **source permanent**
  (`ReduceCastCostForMatchingSpellsEffectHandler`'s `AmountContext`) where the absorbed handler passed
  `AmountContext.forCasting` (null permanent). A strict widening — source-relative amounts now work
  on this shape too. Inert for Heartless Summoning, whose amount is `Fixed(2)`.

**Other call sites updated**
- `CastingCostService.computeTargetBasedCostReduction` collapses from two `findFirst()` scans to one.
  The two scans had **opposite precedence** to `GameActionAvailabilityService`'s loop (general-first
  vs controlled-first); with one record there is no precedence left to disagree about. Behaviour is
  unchanged because no card carries both — Ajani's Response has the any-controller form, Savage Stomp
  the controlled one.
- `GameActionAvailabilityService`'s playability branch keeps both battlefield probes, now selected by
  the flag: `controlsPermanent` when `controlledByCaster`, `battlefieldHasPermanentMatching` otherwise.
- `GameViewProjectionFactory` imported all three `ReduceOwnCastCostIfTargeting*` records and used
  none of them; the dead imports are gone.
- `TargetPolarityClassifier` and the AI module needed nothing — none of the six records was ever
  named outside the engine.

**Tests** — no test referenced the six classes by name except the service tests that construct them
(`CastingCostServiceTest`, `GameActionAvailabilityServiceTest`, `CastingPermissionServiceTest`,
`SpellCastingServiceTest`, plus `CostModificationTestRegistry`), all converted. Two of those mock
`PredicateEvaluationService`, and the absorbed increase/reduce handlers never called it — they did a
raw `Set.contains`. Converting them to a predicate would therefore have made the modifier silently
vanish behind a default-`false` mock, so both classes gained an `evaluateCardTypePredicates()` helper
that really evaluates `CardTypePredicate`/`CardAnyOfPredicate`; one pre-existing local stub that cast
argument 1 straight to `CardTypePredicate` was folded into it (a `CardAnyOfPredicate` made it throw).

Every scope branch was already pinned both ways by existing card tests, which is why only the two
behaviour-change tests above are new: `DerelorTest.opponentBlackSpellNotTaxed` (SELF must not leak to
ALL) and `AuraOfSilenceTest.ownEnchantmentsNotAffected` (OPPONENT must not either), `ChillTest`'s
"Opponent's red spell also costs {2} more" (ALL must not narrow to SELF),
`ColfenorsPlansTest.opponentIsNotRestricted` and `CurseOfExhaustionTest.doesNotLimitNonEnchantedPlayer`
(CONTROLLER and ENCHANTED_PLAYER must not leak to EACH_PLAYER — the latter casts the Curse *and* is
unrestricted, which is exactly the mirror), `RuleOfLawTest`/`ArcaneLaboratoryTest` "affects both
players", and `AjanisResponseTest`'s "Reduced cost applies when targeting opponent's tapped creature"
(`controlledByCaster=false` must not behave like `true`).

**Keep separate**: `IncreaseSpellCostExceptOnControllersTurnEffect` (Defense Grid — its waiver is
keyed on the active player, not on who controls the source),
`IncreaseOwnCastCostUnlessRevealSubtypeEffect` and `IncreaseCostOfSpellsTargetingThisSpellEffect`
(both spell-self, not battlefield-source), `IncreaseOpponentCostForTargetingControlledPermanentEffect`
(target-gated), `IncreaseActivatedAbilityCostEffect` (taxes abilities, not spells),
`ReduceOwnCastCostForSharedCardTypeWithImprintEffect` (compares against the imprinted card, not a
predicate), and `ReduceOwnCastCostIfTargetingStackEntryEffect` (a `StackEntryPredicate`, not a
`PermanentPredicate`).

---

### Step 14 — Remove-counter batch — **DONE**

**Shipped**
```java
public enum CounterRemovalSubject { SOURCE, TARGET }

public record RemoveCounterAndGainLifeEffect(CounterType counterType,
                                             int lifeGain,
                                             CounterRemovalSubject subject) implements CardEffect

public record RemoveAllCountersEffect(CounterType counterType, CounterRemovalSubject subject)
        implements CombatDamageTriggerContextEffect
// sugar: (CounterType) -> subject = SOURCE

public record RemoveCounterFromTargetPermanentEffect(CounterType counterType,
                                                     PermanentPredicate targetPredicate,
                                                     int amount) implements CardEffect
// sugar: () -> (null, null, 1); (CounterType, PermanentPredicate) -> amount 1

public record RemoveCounterFromSourceCost(int count, CounterType counterType) implements CostEffect
// now overrides sourceCountersRemoved() -> count
```

Six records and five handlers deleted, two records + one enum + two handlers added. Cards (39):
Living Artifact, Exemplar of Strength, Woeleecher, Chainbreaker; Ammit Eternal, Ashling the Pilgrim,
Discordant Spirit, Energy Vortex, Rogue Skycaptain, Ventifact Bottle, Witherscale Wurm, Hapatra's
Mark; Gremlin Mine; Conversion Chamber, Golem Foundry, Ice Cauldron, Jeweled Amulet, Lux Cannon, Mana
Bloom, Necrogen Censer, Shriekhorn, Sigil of Distinction, Sphere of the Suns, Surge Node, Titan
Forge, Trigon of Corruption / Infestation / Mending / Rage / Thought, Tumble Magnet, Vivid Crag /
Creek / Grove / Marsh / Meadow.

**What held** — all four premises. The gain-life handlers really do share the same seven statements
and the source form's `getSourcePermanentId() != null ? … : getTargetId()` fallback really is the
target form, so one expression covers both subjects. Both `RemoveAllCounters*` handlers really do
reduce to `removed = getCounterCount(ct)` → `setCounterCount(ct, 0)` → the same log, and the flagged
gotcha was real: `targetSpec()` and `combatDamageTriggerContext()` both branch on `subject`, with
`TARGET` reporting `null` (Step 7's precedent — every reader calls the method rather than using the
interface as a bare marker, and `null` is documented as "no special context"). The charge-cost
availability check at `~:2870` really is the `default ->` branch of the generic check at `~:2799`,
and payment `~:2055` the generic `default ->` at `~:1996`.

**What the audit missed**
- **Six records, not four.** The Step Index said 4 deleted; the body's own table lists six.
- **`boolean fromTarget` reads as noise at the call site.** Rows 1 and 2 both wanted the same
  two-valued axis, and row 1's four cards split 2/2 between the values, so there is no honest
  default to hide the flag behind. Both use one shared `CounterRemovalSubject` enum instead —
  the same "which permanent does this effect act on" axis Steps 8 and 9 spelled `PhaseOutSubject` /
  `PermanentReference`. Neither of those enums offers both `SOURCE` and `TARGET`, so extending one
  would have broken its exhaustive switches for no gain.
- **The gain-life handlers were not identical.** The source form rendered the counter through
  `PermanentCounterSupport.counterTypeName` ("-1/-1"), the target form interpolated the enum
  constant ("MINUS_ONE_MINUS_ONE"). Unified on `counterTypeName`, which is what the rest of the
  counter code already does — see the deliberate deltas.

**The flagged decision: `sourceCountersRemoved()` now returns `count`.** Taken deliberately, for
the survivor rather than against it:
- The facet is **descriptive** by `CostEffect`'s own contract — "every facet returns an existing
  record component … never a score". After the merge the survivor genuinely removes `count` counters
  from the source, so `0` would be a false statement about the record.
- It keeps all 23 absorbed charge cards' AI estimate **exactly** as it was; the alternative
  (inheriting `0`) would have silently dropped it for them.
- It ends an accidental split the merge exposes: Druid's Repository already used
  `RemoveCounterFromSourceCost(1, CHARGE)` and reported `0` while Golem Foundry's charge record
  reported `3` for the same resource.
- The 39 cards that gain the estimate are overwhelmingly limited-use resource counters (charge,
  wish, study, divinity, blaze, hoofprint, currency, …) plus +1/+1 removals, where one point of cost
  per counter is the intended reading. For the seven `MINUS_ONE_MINUS_ONE` cards the counter is still
  a limited number of activations, but the P/T the source gains back is not modelled — an
  understatement of value in `SpellEvaluator:161`, not a wrong sign on the fact. Recorded in the
  record's javadoc as a consumer-side shortcoming rather than papered over with a `counterType ==
  CHARGE` special case, which would have made the facet a score.

**Deliberate deltas** (all log-only or inert)
- The gain-life `TARGET` form now logs "A -1/-1 counter removed from X." instead of
  "A MINUS_ONE_MINUS_ONE counter removed from X.". Same unification applied to
  `RemoveCounterFromTargetPermanentEffectHandler`, which had the same raw-enum bug. No test asserted
  either string.
- `RemoveCounterFromTargetPermanentEffect` gained an `amount` upper bound, so Gremlin Mine's log
  loses the charge handler's "(N remaining)" suffix and reads "4 charge counters removed from X.".
  `Math.min(amount, present)` is identical to the old "remove 1 if > 0" at `amount == 1`; both
  amount-1 branches stay pinned by existing tests that stack more than one counter on the target
  (`MedicineRunnerTest`, `DefiantGreatmawTest`).
- Charge-cost activation now throws "Not enough counters to remove (need N, have M)" rather than
  "Not enough charge counters (…)", and its payment log drops "(N remaining)". Asserted message
  updated in `AbilityActivationServiceTest`.
- `RemoveAllCountersEffect` sets the entry's event value on the `TARGET` path too (including `0`
  when the subject is gone). Hapatra's Mark is the only `TARGET` card and nothing on its entry reads
  `EventValue`.

**Other call sites updated**
- `DestructionSupport:560` gates its `ForcedCostOrElse` fallback on `subject == SOURCE` (Step 9's
  precedent), so a `TARGET` form in that slot falls through to the existing unsupported-fallback
  `log.warn` instead of silently reading the payer as a permanent id. Rogue Skycaptain unchanged.
- `PutCounterOnSelfThenTransformIfThresholdEffectHandler:101` **constructs** the record at runtime
  for Primal Amulet's may-transform — a write site, not just a read site.
- `AbilityActivationService` loses both charge-specific blocks (availability + payment) and its
  import; the generic remove-counter blocks already covered them.
- `TargetPolarityClassifier:246` keys on `RemoveAllCountersEffect` **and** `subject == TARGET`, so
  the seven self-form cards keep returning `null` as they always did.
- `CostEffectClassificationTest.ABILITY_ONLY_COST_TYPES` drops the deleted cost name (its
  stale-entry ratchet fails otherwise).

**Tests** — added the cross-contamination assertions a subject merge needs, one per axis where a
misread is observable: `WoeleecherTest.counterComesOffTheTargetNotTheSource` (the strongest case —
an activated ability whose source permanent is on the battlefield and also carries a -1/-1 counter,
so a `TARGET` branch reading `sourcePermanentId` is caught),
`ExemplarOfStrengthTest.attackTriggerLeavesOtherCreaturesCountersAlone` (the `SOURCE` mirror) and
`HapatrasMarkTest.leavesTheUntargetedCreaturesCountersAlone`. The `SOURCE_SELF` trigger context is
already pinned by `AmmitEternalTest.combatDamageRemovesCounters`, the event-value snapshot by
`AshlingThePilgrimTest`, the `ForcedCostOrElse` fallback by `RogueSkycaptainTest`, and the `amount`
bound by `GremlinMineTest`'s up-to-four pair.

**Kept separate**: `RemoveAllCountersAsCostEffect` (paid at activation, snapshots into xValue, no
`subject`), `RemoveCounterFromSourceEffect` (a *resolved* self-effect, not a cost, with
`selfTargeting()`), `RemoveCounterFromControlledCreatureCost` / `RemoveOneOrMoreCountersFrom*` /
`RemoveXCountersFromSourceCost` (different payment mechanics), and
`RemoveCountersInsteadOfUntappingEffect` (a static replacement).

---

### Step 15 — Reveal / reorder library batch — **DONE**

**Shipped**
```java
public record ReorderTopCardsOfLibraryEffect(int count, LibraryOwner owner) implements CardEffect
// sugar: (int count) -> owner = CONTROLLER

public record RevealTopCardOfLibraryEffect(LibraryOwner owner, int lifeGainIfLand)
        implements LifeGainEffect
// sugar: (LibraryOwner owner) -> lifeGainIfLand = 0
// enum LibraryOwner { CONTROLLER, TARGET_PLAYER }
```

Two records and two handlers deleted, one enum added. Cards (17): Index, Ponder, Omen, Sage Owl,
Sage Aven, Inkfathom Divers, Gilt-Leaf Seer, Mirri's Guile, Discombobulate; Portent, Elemental
Augury, Architects of Will; Aven Windreader, Prophecy; Callous Deceiver, Cruel Deceiver, Harsh
Deceiver. Only eight card files changed — the reorder family's nine own-library call sites still
read `new ReorderTopCardsOfLibraryEffect(N)` and were untouched.

**What held** — both premises. The two reorder handlers really do share the `Math.min`, the
`count == 1` look-only shortcut, the `deck.subList(0, count)` snapshot-and-clear and the same
`PendingInteraction.LibraryReorder(controllerId, topCards, false, <deckOwnerId>, prompt)` shape
(`GameLog.cardThen(card, s)` and `GameLog.builder().card(card).text(s).build()` are the same call).
The two reveal handlers are copies bar the id and the `lifeGainIfLand` rider. The target-variant
fallback to `controllerId` on a null `getTargetId()` is a strict generalization and is kept.

**What the audit missed — three corrections**
- **A `boolean targetPlayer` on each record would have been two spellings of one axis, and would
  have inverted the reveal family's defaults.** The reveal survivor's existing `()` / `(int)`
  constructors mean "target player", so a trailing boolean would have made `false` the value nobody
  wanted to write and forced `new RevealTopCardOfLibraryEffect(0, false)` on the three Deceivers.
  Both records now take a shared `LibraryOwner` enum instead, in the codebase's `MillRecipient` /
  `SkipRecipient` idiom. It leads on the reveal record (where the life-gain rider is the optional
  tail) and trails on the reorder record (where `count` is the mandatory head), which is what keeps
  all nine own-library reorder call sites source-compatible.
- **The reveal's `@ValidatesTarget` had to be branched, not just re-keyed.**
  `LibraryTargetValidators.validateRevealTopCardOfLibrary` called `tvs.requireTargetPlayer(ctx)`
  unconditionally. The class key is unchanged by the merge, so nothing would have failed to compile
  — the three Deceivers' second ability would simply have started demanding a player target it
  never had. It now takes the effect as a second parameter and gates on
  `owner == TARGET_PLAYER`, exactly as the `MillEffect` validator eight lines above it already does.
- **`Fixed(0)` is honest to the amount-reading `LifeGainEffect` consumers but not to the
  presence-checking ones.** `SpellEvaluator:479,628` evaluate `lifeGainAmount()` and correctly score
  a rider-less reveal at 0, but `SpellEvaluator.isLifeGainEffect` and
  `InstantCategoryClassifier:85` are bare `instanceof` tests — under them a `Fixed(0)` reveal is a
  lifegain spell, worth a 3× danger multiplier and a `CARD_ADVANTAGE` classification. That was
  already latently wrong for Aven Windreader; the merge would have extended it to every reveal card.
  Rather than leave it accidental, `LifeGainEffect` gained a `gainsNoLife()` default (the amount is
  `Fixed(0)`) and both presence checks now consult it. **Inert today**: `hasLifeGainEffect` scans
  only `SPELL` / `ON_ENTER_BATTLEFIELD` and `InstantCategoryClassifier` only `SPELL`, while all
  four rider-less reveals sit in activated abilities; Prophecy's rider is 1 and unaffected.

**Deliberate deltas** (all log/prompt only)
- **The reorder wording is derived from `deckOwnerId.equals(controllerId)`, not from the owner
  axis.** Every string is byte-for-byte preserved for both families' normal paths; the one change is
  that Portent / Elemental Augury / Architects of Will aimed at *yourself* — "target player" includes
  you, and both cards have a test for it — now read "looks at the top 3 cards of their library" and
  prompt "back on top of **your** library" instead of naming you in the third person.
- The reveal path gained the own-library handler's `deck == null` guard and the reorder path's
  null-target fallback, so a target form resolving with no target no longer NPEs. Unreachable
  today — the validator requires the target.
- The two slf4j lines are unified per family (the game-log lines they used to disagree with are
  unchanged).

**Tests** — the two handler tests keep their coverage and gain one branch each: the reorder's
`controllerOwnerIgnoresTargetId` / `targetPlayerOwnerReadsTargetLibrary`, and the reveal's
`controllerOwnerIgnoresTargetId`. At card level, `PortentTest` and `ArchitectsOfWillTest` already
pinned `deckOwnerId` **and** `playerId` for `TARGET_PLAYER` both ways (including the
target-yourself case), so the gaps were the `CONTROLLER` side and the reveal family:
`DiscombobulateTest.resolvingEntersLibraryReorderState` gains a `deckOwnerId` assertion — the
strongest case in the pool, because the countered spell genuinely occupies the entry's `targetId`,
so a merged handler that read it would resolve `playerDecks.get(<spell id>)`;
`CruelDeceiverTest.revealReadsTheControllersOwnLibrary` (the `CONTROLLER` reveal must name the
caster's own top card and never the opponent's) and
`AvenWindreaderTest.revealReadsTheTargetLibraryOnly` (the mirror).

**Kept separate**: `LookAtTopCardsOfTargetLibraryEffect` — a *private* look with a `TargetLibraryAction`
protocol (search interactions, exile/graveyard dispositions), not a public reveal or a reorder;
`RevealTopCardGainLifeEqualToManaValueEffect`, `RevealTopCardsBottomThenDamageIfCopyRevealedEffect`
and `RevealTopCardPutLandsIntoGraveyardRepeatEffect` (each moves cards or repeats);
`PutTopCardsOfLibraryOnBottomEffect` (bottom, not top).

---

### Step 16 — Damage target-category batch

| Target | Absorbs | Cards |
|---|---|---|
| `DealDamageToTargetPlayerOrPlaneswalkerEffect(DynamicAmount, boolean opponentOnly)` | `DealDamageToTargetOpponentOrPlaneswalkerEffect` | 14 |
| `DealDamageToAllCreaturesTargetControlsEffect(int, boolean mustAttack, boolean includePlaneswalkers)` | `DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect` | 1 |
| `DealDamageEqualToChosenTypeCountEffect(TargetPredicate declaredTarget)` | both chosen-type-count effects | 1 + 1 |

**Notes**
- Player-or-planeswalker: the records are identical (one `DynamicAmount`, same
  `TargetSpec.harmful(TargetPredicates.playerOrPlaneswalker())`), and the two handlers are **byte-for-byte identical**
  apart from the class name and one comment word. The only real difference is external:
  `@ValidatesTarget(DealDamageToTargetOpponentOrPlaneswalkerEffect.class)` in
  `service/validate/DamageTargetValidators.java:30`, which rejects the controller — exactly what the
  boolean encodes. Also update `service/combat/CombatDamageService.java:1711` and
  `service/trigger/DamageTriggerCollectorService.java:154` (`instanceof X` → `instanceof X e && e.opponentOnly()`).
- All-creatures: handlers are copies; the sole divergence is the type gate
  (`if (!isCreature) continue;` vs `if (!isCreature && !hasType(PLANESWALKER)) continue;`).
- Chosen-type-count: both records are no-component; the handlers share the entire two-phase
  creature-type-choice protocol verbatim and differ only in the final
  `resolveAnyTargetDamage(...)` vs `resolveCreatureTargetDamage(...)`. The multiplier bug is already
  fixed — both now call `applyDamageMultiplier`, so this is a clean merge.

---

### Step 17 — Exile-top-cards families

**17a Target**
```java
record ExileTopCardsMayPlayEffect(DynamicAmount count, CardPredicate playableFilter,
                                  ExilePlayWindow window, boolean faceDown,
                                  boolean withoutPayingManaCost)
// window: END_OF_TURN | END_OF_NEXT_TURN | NEXT_UPKEEP | NEXT_UPKEEP_THEN_GRAVEYARD
```
**Absorbs**: `ExileTopCardMayPlayThisTurnEffect` (4), `ExileTopCardsMayPlayUntilNextTurnEffect` (3),
`ExileTopCardMayCastNonlandThisTurnEffect` (1), `ExileTopCardMayPlayUntilNextUpkeepEffect` (1),
`ExileTopCardsFaceDownMayPlayUntilNextUpkeepEffect` (1),
`ExileTopCardsMayCastMatchingThisTurnEffect` (1).

All six handlers share the same body verbatim, including the identical
`"'s library is empty — nothing to exile."` log. The axes are exactly the proposed fields.

**Do the free sub-merge first**: `ExileTopCardMayCastNonlandThisTurnEffect` is semantically identical
to `ExileTopCardsMayCastMatchingThisTurnEffect(1, new CardNotPredicate(new CardTypePredicate(LAND)))`,
and `PredicateEvaluationService.matchesCardPredicate` returns `true` for a `null` predicate (`:217`),
so `ExileTopCardMayPlayThisTurnEffect(n, false)` also collapses in. **2 classes + 2 handlers deleted
with zero new machinery.**

**17b Target**: `ExileTopCardsToSourceEffect(int count, boolean faceDown, boolean toGraveyardOnControlLoss, LibraryScope scope)`
— `{ CONTROLLER, TARGET_OPPONENT, EACH_PLAYER }`.
**Absorbs**: `ExileTopCardsOfOpponentLibraryToSourceEffect` (1),
`EachPlayerExilesTopCardsToSourceEffect` (1). Survivor has 3 cards and **already has `faceDown`**.
All three resolve the source permanent, share the same
`"Source permanent no longer on battlefield … fizzles"` log, then
`Math.min(e.count(), deck.size())` and the same exile loop. The survivor's javadoc already names the
other two as its counterparts.

---

### Step 18 — Enchanted-creature aura batch

**18a Target**: `EnchantedCreatureCombatTaxEffect(int amount, CombatTaxKind kind)` —
`{ ATTACK, BLOCK_WITH, BE_BLOCKED_BY }`.
**Absorbs**: `EnchantedCreatureCantAttackUnlessPaysEffect` (2),
`EnchantedCreatureCantBlockUnlessPaysEffect` (1),
`EnchantedCreatureCantBeBlockedUnlessPaysEffect` (1).
All three are single-`int` records. Their three consumers in `GameQueryService` —
`getEnchantedCreatureAttackTax:3521`, `getEnchantedCreatureBlockTax:3542`,
`getEnchantedCreatureBlockerTax:3560` — are line-for-line identical, differing only in the class and
accessor. Collapse to one `getEnchantedCreatureCombatTax(gameData, creature, kind)` and keep the
three public methods as thin delegating wrappers so call sites are untouched.

**18b Target**: `EnchantedCreatureCantAttackOrBlockEffect(boolean preventsAttacking, boolean preventsBlocking)`.
**Absorbs**: `EnchantedCreatureCantAttackEffect` (2); survivor has 15.
`AttackLegalityService:92-93` checks both on adjacent lines with the identical expression;
`BlockLegalityService:353` checks only the survivor. `CantAttack` is a strict subset.
**Prerequisite**: `GameQueryService.hasAuraWithEffect:3453` is class-based only — it needs a
`Predicate<CardEffect>` overload first.

**Optional follow-on** (only if the overload lands): `EnchantedCreatureCantActivateTapAbilitiesEffect`
(1) → `EnchantedCreatureCantActivateAbilitiesEffect` (7) with `boolean tapOnly`. Forces the overload
across 5 call sites (`AbilityActivationService:208,685,2617`, `GameQueryService:4383,4420`) to delete
one class.

---

### Step 19 — Entering-creature conditionals + `GrantEffectTo*`

**19a Target**: `EnteringCreatureConditionalEffect(CardPredicate filter, CardEffect wrapped) implements EnterCreatureConditionalEffect`.
**Absorbs**: `EnteringCreatureMinPowerConditionalEffect` (5),
`EnteringCreatureMaxPowerConditionalEffect` (1), `EnteringCreatureExactStatsConditionalEffect` (1).
All three implement the same interface and are consumed by a **single** dispatch site,
`TriggerCollectionService.unwrapEnterCreatureConditional:4121`, which only calls
`testEnteringCreature()`, `triggerDescription()`, `wrapped()`. There is no per-class handler.
`CardPowerAtLeastPredicate` / `CardPowerAtMostPredicate` already exist with matching
null-power-never-matches semantics, so Min+Max collapse for free (LOW). Folding in `ExactStats`
needs one new toughness predicate (`CardAllOfPredicate` composes it) — still net −2 (MED).

**19b Target**
```java
GrantEffectToTargetEffect(EffectSlot slot, CardEffect grantedEffect, EffectDuration duration,
                          boolean skipIfAlreadyPresent, GrantScope scope, TargetPredicate declaredTarget)
```
**Absorbs**: `GrantEffectToTargetUntilEndOfTurnEffect` (6),
`GrantEffectToSourceUntilEndOfTurnEffect` (1), `GrantEffectToOwnCreaturesUntilEndOfTurnEffect` (2).
All four converge on `permanent.addTemporaryTriggeredEffect(e.slot(), e.grantedEffect())`; the only
branch is the survivor's `duration() == UNTIL_END_OF_TURN ? addTemporary… : addPersistent…`, which is
exactly the axis the other three hardcode. Recipient selection uses three shapes that already exist
verbatim in `GrantProtectionChoiceUntilEndOfTurnEffectHandler.resolveRecipientIds`.
**Two deltas to preserve**: `skipIfAlreadyPresent` defaults `true` on the survivor but the absorbed
three have no such check (migrated call sites must pass `false`); and `targetSpec()` is `CREATURE`
on the until-EOT variant vs `PERMANENT` on the base. Update `TargetPolarityClassifier`.

---

### Step 20 — Misc low-risk 2→1 batch

Independent, all LOW. Do as many as land cleanly; note any that don't.

| Target | Absorbs | Cards |
|---|---|---|
| `BoostEquippedCreatureUntilEndOfTurnEffect(DynamicAmount, DynamicAmount, Set<Keyword>)` | `BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect` | 11 + 3 |
| `AwardManaOfColorsAmongControlledEffect(PermanentPredicate, boolean oneOfEach)` | `AwardOneManaOfEachColorAmongControlledEffect` | 1 + 1 |
| `AddManaWhenLandOfSubtypeTappedForManaEffect(+ ManaRestriction)` | `AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect` | 2 + 1 |
| `AddOneOfEachManaTypeProducedByLandEffect(boolean controllerOnly, PermanentPredicate landFilter)` | `AddProducedManaWhenLandOfSubtypeTappedEffect`, `AddProducedManaWhenSnowLandTappedEffect` | 1 + 1 |
| `PlayWithHandRevealedEffect(boolean controllerOnly)` | `PlayWithHandsRevealedEffect`, `PlayWithOwnHandRevealedEffect` (+ delete the `PubliclyRevealedHandEffect` interface) | 1 + 1 |
| `PayXManaThenEffect(String manaCost, CardEffect payload)` | `PayXManaGainXLifeEffect`, `PayXManaCreateXTokensEffect` | 1 + 1 |
| `TargetPlayerChoosesCreatureEffect(boolean exile)` | `TargetPlayerChoosesCreatureDestroyEffect`, `TargetPlayerChoosesCreatureExileEffect` | 2 + 1 |
| `TargetPlayerCantThisTurnEffect(TurnRestriction)` | `TargetPlayerCantPlayLandsThisTurnEffect`, `TargetPlayerCantCastCreatureSpellsThisTurnEffect` | 2 + 1 |
| `SacrificeCreatureForTokenEffect(CreateTokenEffect, PermanentPredicate, SacrificedStat, TokenScaling)` | `SacrificeCreatureCreateSizedTokenEqualToPowerEffect`, `SacrificeCreatureToCreateTokensEqualToToughnessEffect` | 1 + 1 |
| `SacrificeAllYouControlCost(PermanentPredicate, boolean trackTotalPower)` | `SacrificeAllCreaturesYouControlCost`, `SacrificeAllPermanentsYouControlCost` | 1 + 1 |
| `MillEffect` + `MillRecipient.DEFENDING_PLAYER` | `MillDefendingPlayerEffect` | 1 |
| `GainLifeEffect` — replace `boolean targetsPlayer` with `TargetPredicate declaredTarget` | `GainLifeEqualToTargetCreatureStatEffect` | 2 |

**Notes**
- `AwardOneManaOfEachColor…`: identical record components, and both branches in
  `ActivatedAbilityExecutionService:675,700` call the same `collectColorsAmongControlled(...)`.
  **Surfaces a latent bug**: the `AwardOneManaOfEachColor` branch multiplies by `manaMultiplier` and
  calls `pool.addCreatureMana` when `isCreatureSource`; the other branch does neither (`:680` adds a
  flat 1). Decide deliberately.
- Land-tap trio: the three collectors in `trigger/LandTapTriggerCollectorService.java:205,233,263` are
  token-for-token identical including a byte-identical log line; only the land-qualification guard
  differs. The snow guard uses layer-aware `hasEffectiveSupertype`, so predicate evaluation must use
  the layer-aware path.
- `PlayWithHandRevealed*`: both records have **zero components** and exist solely to hardcode a
  boolean behind an interface, read at exactly one site (`GameViewProjectionFactory:293`).
- `PayXMana*`: the two handlers are byte-identical (100 vs 101 lines) except the injected support
  bean, one payload line, and the prompt string — the whole `chosenXValue` re-entry protocol,
  X=0 decline branch, `payableFromPool` re-check and `beginXPrompt`/`payableFromPool` helpers are
  duplicated verbatim. Leave `PayXManaDealXDamageToAnyTargetEffect` (colored cost + targeting) out.
- `TargetPlayerChoosesCreature*`: the choice context is **already parameterized** — destroy passes
  `PermanentChoiceContext.DestroyChosenCreature(targetPlayerId, cardName)`, exile passes the same
  record with `true`. No context work needed.
- `SacrificeCreatureForToken*`: identical components; handlers are line-for-line identical, differing
  only in which `PermanentChoiceContext` is set. Downstream,
  `PermanentChoiceBattlefieldHandlerService:1097,1129` also share their whole body, differing only in
  `getEffectiveToughness` vs `getEffectivePower` and `withAmount(n)` vs rebuilding the template.
  **Preserve `Math.max(0, …)` on both paths** (the power path clamps, the toughness path doesn't).
- `GainLifeEqualToTargetCreatureStatEffect`: its own javadoc says the only reason it exists is its
  creature `TargetSpec`. One delta — it passes `null` as the source permanent into
  `AmountContext.forStackEntry`; `TargetPower`/`TargetToughness` read the entry target, so this should
  be inert, but confirm for Predator's Rapport's `Sum(TargetPower(), TargetToughness())`.

---

## Phase 3 — MEDIUM risk, real decisions

### Step 21 — `RevealUntilEffect`

**Target**
```java
record RevealUntilEffect(CardPredicate matchPredicate, int matchCount,
                         RevealMatchDestination matchDest,   // HAND | BATTLEFIELD
                         RevealRestDestination restDest)     // GRAVEYARD | EXILE | BOTTOM_ORDERED | BOTTOM_RANDOM
```
**Absorbs** (1 card each): `RevealUntilBasicLandToHandRestToGraveyardEffect`,
`RevealUntilColorToHandRestExiledEffect`, `RevealUntilNonlandCardsToHandRestToBottomEffect`,
`RevealUntilLandToBattlefieldRestToBottomEffect`,
`RevealUntilCardPredicateToBattlefieldRestOnBottomRandomEffect`.

The three to-hand handlers are line-for-line copies (same reveal loop, same empty-library return,
same "reveals X from the top of their library with Y" log, same `rest.remove(found)`); only the
rest-disposal call differs. Match tests are already expressible with existing predicates
(`CardAllOfPredicate(CardTypePredicate(LAND), CardSupertypePredicate(BASIC))`, `CardColorPredicate`,
`CardNotPredicate(CardTypePredicate(LAND))`).

**Gotchas** — (a) `RevealUntilColorToHandRestExiledEffectHandler` has a private `hasColor` that falls
back to `card.getColor()` when `getColors()` is empty, whereas `CardColorPredicate` uses
`getColors().contains(color)`: equal for Sacred Guide but not literally identical. (b) Keep
bottom-ordered and bottom-random as distinct enum values. (c) Absorbing
`RevealUntilLandToBattlefield…` into the predicate variant also **fixes its missing legend-rule check**.

---

### Step 22 — Static damage-prevention markers

**Target**: `StaticDamagePreventionEffect(PreventionHost host, PreventionDirection direction, DamageKind kind)`
— host `{SELF, ATTACHED}`, direction `{TO, BY, TO_AND_BY}`, kind `{ALL, COMBAT, NONCOMBAT}`.

**Absorbs** (all 1 card each except where noted): `PreventAllDamageEffect`,
`PreventAllCombatDamageToSelfEffect`, `PreventAllCombatDamageToAndBySelfEffect`,
`PreventAllDamageToAndByEnchantedCreatureEffect`,
`PreventAllCombatDamageToAndByEnchantedCreatureEffect` (2),
`PreventAllDamageDealtByEnchantedCreatureEffect`,
`PreventAllNoncombatDamageToAttachedCreatureEffect`.

All seven are **empty records** probed by class literal at exactly two kinds of site:
`DamagePreventionService.applyCreaturePreventionShield:201-220` is seven consecutive `return 0` lines
alternating `getEffects(STATIC).stream().anyMatch(X.class::isInstance)` (SELF) and
`gameQueryService.hasAuraWithEffect(gameData, permanent, X.class)` (ATTACHED), each optionally guarded
by `isCombatDamage` / `!isCombatDamage`. The "by" side mirrors this at
`GameQueryService.isPreventedFromDealingDamage:4147-4166` and `DamageSupport:470-471`.

**Risk MED** — touches three engine services rather than one handler.
**Keep out**: `PreventDamageToSelfFromCreaturesEffect` (Uncle Istvan) adds an orthogonal
source-restriction axis handled in `DamageSupport.dealCreatureDamage`.

---

### Step 23 — Attach family

**23a Target**: `AttachSourceToTargetPermanentEffect()` — source attaches to `targetId`.
**Absorbs**: `AttachSourceAuraToTargetCreatureEffect` (2),
`AttachSourceEquipmentToTargetCreatureEffect` (3).

**23b Target**: `AttachTargetToTargetPermanentEffect()` — `targetIds[0]` attaches to `targetIds[1]`.
**Absorbs**: `AttachTargetAuraToTargetCreatureEffect` (2),
`AttachTargetEquipmentToTargetCreatureEffect` (1).

All four handlers share the identical core — `expireFloatingEffectsForUnattachedSource(x.getId())` →
`x.setAttachedTo(target.getId())` → `x.setTimestamp(gameData.nextTimestamp())` with the same CR 613.7e
comment. The two type-specific tails are **already self-guarding**, so the union is safe with no branch:
`TriggerCollectionService.checkAuraAttachedTriggers:3782` returns immediately unless the card has the
`AURA` subtype, and `EquipSupport.applySacrificeOnUnattachIfNeeded:45` returns immediately unless the
card carries `SacrificeOnUnattachEffect`. The equipment handlers' extra card-id fallback
(`equipSupport.findEquipmentByCardId`, for the death-trigger path) is harmless on both.

**Gotchas** — the success-log fix from `9b8147333` must survive. And
`AttachTargetAuraToTargetCreatureEffect`/`AttachTargetEquipmentToTargetCreatureEffect` both declare
`TargetSpec.benign(PLAYER_OR_PERMANENT)` for a two-permanent target, which looks wrong already —
check it, don't copy it forward blindly.

**Keep separate**: `AttachTargetToSourcePermanentEffect` (opposite orientation).

---

### Step 24 — Colour-setting `Become*` family

**Target**: `SetColorsEffect(Set<CardColor> colors, boolean chooseColors, boolean targeted, EffectDuration duration)`.
**Absorbs**: `BecomeAllColorsUntilEndOfTurnEffect` (1), `BecomeChosenColorsIndefinitelyEffect` (2),
`BecomeColorlessUntilEndOfTurnEffect` (2), `BecomeColorlessIndefinitelyEffect` (1) — surviving into
`BecomeChosenColorsUntilEndOfTurnEffect` (3).

All five are the same CR 613 layer-5 override. `LayerSystemService.applyL5Instance:1733-1751` already
proves it: the chosen-colors case does `overrideColors(Set.copyOf(becomes.colors()))` and colorless
does `overrideColors(Set.of())` — colorless is literally the empty-set case.
`BecomeAllColorsUntilEndOfTurnEffectHandler` **already floats** a
`BecomeChosenColorsUntilEndOfTurnEffect(EnumSet.allOf(CardColor.class))`, and
`BecomeColorlessIndefinitelyEffectHandler` **already floats** a `BecomeColorlessUntilEndOfTurnEffect(false)`
with `EffectDuration.PERMANENT` — the merge is what the code already does by hand.

**Gotcha** — the explicit `chooseColors` flag is load-bearing: `applyL5Instance:1734` currently uses
`if (becomes.colors().isEmpty()) return;` to distinguish a prompting instance from an applied one,
which is exactly why colorless needed its own record.
**Leave out**: `BecomeColorlessEffect(GrantScope)` is a **staticfx** effect on a different dispatch path.
**Follow-up**: `SetTargetColorEffect` (S slice) already handles colorless via `color() == null`
(`:1745`) and should be folded in a later sweep.

---

### Step 25 — Copy-spell-for-each-other

**Target**: `CopySpellForEachOtherMatchingPermanentEffect(PermanentPredicate filter, StackEntry spellSnapshot, UUID castingPlayerId, UUID originalTargetId)`.
**Absorbs**: `CopySpellForEachOtherControlledCreatureEffect` (1),
`CopySpellForEachOtherSubtypePermanentEffect` (1).

Identical trigger-descriptor shape; handlers are structurally identical (`spellSnapshot == null` guard,
`forEachPermanent` collection, skip `originalTargetId`,
`validTargetService.canPermanentBeTargetedBySpell`, per-target `copySupport.createCopyCard` +
`createCopyStackEntry` + push, same `GameLog` line). Only the eligibility filter differs, and both are
expressible: `PermanentAllOfPredicate(PermanentIsCreaturePredicate, PermanentControlledBySourceControllerPredicate)`
and `PermanentHasSubtypePredicate(GOLEM)`.

**Gotchas** — `PermanentControlledBySourceControllerPredicate` must be evaluated with
`FilterContext.withSourceControllerId(castingPlayerId)` — the **caster**, not the trigger source's
controller (an opponent can cast the spell). And the `isCantBeCopied()` guard added in `9b8147333`
must be on the merged handler.

---

### Step 26 — Exile-instead-of-graveyard replacements

**Target**: generalize `OwnGraveyardExileReplacement` into `GraveyardExileReplacement` with
`scope()` (`OWNER` | `AN_OPPONENT_OF_OWNER` | `ANY_PLAYER`), the existing `filter()`, plus
`battlefieldSourceZoneOnly()`, `exemptWhenCycled()`, `appliesToTokens()`.

**Absorbs**: `ExileOpponentCardsInsteadOfGraveyardEffect` (2),
`ExilePermanentsInsteadOfGraveyardEffect` (1),
`ExileInstantSorceryCardsInsteadOfGraveyardEffect` (1). Existing implementors
`ExileOwnCardsInsteadOfGraveyardEffect` (2) and `ExileOwnCyclingCardsUnlessCycledEffect` (1) become
instances of the same shape.

`GraveyardService.addCardToGraveyard` has four consecutive blocks (`:254,288,298,307`) whose bodies
are character-identical, guarded by four private helpers that are the same nested scan differing only
in the player filter and effect class. `shouldExileOwnCardInsteadOfGraveyard` is **already** the
generalized version — it consults `filter()`, `exemptWhenCycled()`, `appliesToTokens()` off the
interface.

**Risk MED** — this is the CR 614 replacement path; collapsing four `if` blocks into one loop fixes
their evaluation order into a single pass. Order is unobservable because every branch produces the
identical outcome, but it needs test coverage before shipping.
**Keep separate**: `ExileInsteadOfGraveyardReplacementEffect` (4) is a per-card self-replacement read
off the dying card itself, not a battlefield-wide static.

---

### Step 27 — Exile-target-permanent linkage

**Target**: `ExileTargetPermanentUntilSourceLeavesEffect(boolean imprint, boolean returnOnSourceLeave, PermanentPredicate targetPredicate)`.
**Absorbs**: `ExileTargetPermanentAndImprintEffect` (3),
`ExileTargetPermanentAndTrackWithSourceEffect` (4). Survivor has 14.

The three handlers are the same routine, and the survivor **already performs all three** tail
bookkeeping calls (`setImprintedCard`, `addExileReturnOnPermanentLeave`, and the source-tracking
re-add) — the other two are strict subsets of its handler.

**Gotchas** — the imprint effect's polarity was already corrected to `harmful` in `9b8147333`, so the
target-spec mismatch the audit flagged is resolved; verify rather than re-fix. Merging still makes
source-tracking unconditional for the imprint variant (inert unless the card also carries
`AllowCastFromCardsExiledWithSourceEffect` — none of the 3 do, but it is a real widening).

---

### Step 28 — Remaining MEDIUM items

Do these individually; each is self-contained. Reassess before starting — several are low value.

| Target | Absorbs | Note |
|---|---|---|
| `DealDamageToTargetCreatureEffect(+ includePlaneswalkers, targetRestriction)` | `DealDamageToTargetCreatureOrPlaneswalkerEffect` (4) | Migrated cards gain group-binding + excess-damage tracking they lack today |
| `DiscardOwnHandThenDrawEffect(new EventValue())` | `DiscardOwnHandThenDrawThatManyEffect` (4) | Handler must `entry.setEventValue(discardCount)` before evaluating; reconcile the empty-hand log |
| `DealDamageToEachMatchingPermanentEffect(+ OPPONENTS scope)` | `DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect` (1) | Needs a new scope **and** the hard `isCreature` filter relaxed; touches 12 cards — do last |
| `EachPlayerDiscardsHandThenDrawsEffect(mode)` | 2 classes | **Surfaces a bug**: one handler sets `discardCausedByOpponent = false` unconditionally, the other `!playerId.equals(controllerId)`. The `false` looks wrong |
| `MakeCreatureUnblockableEffect(+ massFilter)` | `MakeAllCreaturesUnblockableEffect` (1) | `targetSpec()` becomes three-way; 34 existing call sites must keep their current spec |
| `SetPreparedEffect(boolean prepared)` | 2 classes | Pre-existing inconsistency: one is `benign(CREATURE)`, the other `benign(PERMANENT)`. Merging forces a decision |
| `NameCardMillTargetEffect(reward)` | 2 classes | Also collapses 2 `ChoiceContext` records + 2 `ChoiceHandlerService` methods. **Changes a Jackson-serialized wire shape — do alone** |
| `PutCounterOnEachMatchingPermanentEffect` | `PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect` (2) | Survivor's javadoc already names this case. Confirm the `entry.targetsForEffect(effect)` fallback |
| `PutTargetIntoLibraryNFromTopEffect(position, allowSpellTarget, targetFilter)` | 2 classes | The spell path is gated by a class-keyed stack validator that must become field-driven |
| `ShuffleTargetCardsFromGraveyardIntoLibraryEffect(+ targetPlayer)` | 1 class | `boolean targetPlayer` is already the idiom in this family. Unify on `GraveyardReturnSupport.processTargetedGraveyardCards` |
| `SetBasePowerToughnessEffect(+ scopes)` | 2 classes | Both mass handlers **already construct** `SetBasePowerToughnessEffect` as the floating payload. Needs `DynamicAmount`; re-verify the layer-7b path |
| `DealsPowerDamageEffect(...)` | 4 classes (15 cards) | **Behavior change**: two variants build a synthetic StackEntry for CR 608.2h attribution, two don't. The synthetic form looks correct; the others look like a latent bug |
| `TapOrUntapChosenPermanentEffect` / `TapAnyNumberOfCreaturesForEachEffect` | 2 + 2 | Both need `MultiPermanentChoiceContext` records and their handler branches unified |
| `RevealRandomCardFromTargetPlayerHandEffect(+ loseLife…)` | 1 class | Merged type must expose both target-supply modes (trigger context vs `targetSpec`) |
| `GainControlOfTargetEffect(+ recipient axis)` | `GainControlOfEnchantedPermanentEffect` (1) | 1 class for a change to the slice's hottest effect (41 cards). Optional |
| `MillEffect` + `Halved` amount | `MillHalfLibraryEffect` (2) | Blocked on adding a `Halved(DynamicAmount, boolean roundUp)` wrapper (which would also subsume `HalfControllerLifeRoundedUp`) |
| `MustAttackEffect(scope, matcher)` | `MatchingCreaturesMustAttackEffect` (2) | Consolidates the class but not the four scanning branches in `AttackLegalityService:373-439` |
| `CreateTokenWithCountersEffect` | 2 classes | Needs a new `CardsDrawnThisTurn` `DynamicAmount` |
| `CounterSpellIf*` trio → `ConditionalEffect.unless(...)` | 3 classes | Needs 2 new `StackEntryPredicate`s. **Do Hisoka last** — `PredicateEvaluationService:1359` hardcodes `StackEntryManaValueEqualsXPredicate -> false` in the resolution path |
| `CounterMatchingSpellsEffect` | `CounterSpellsNamedLikeCardsExiledWithSourceEffect` (1) | The new predicate needs the resolving entry's `sourcePermanentId`, which the current call site doesn't pass |
| `CantAttackOrBlockUnlessCountAlsoDoesEffect(1)` | `CantAttackOrBlockAloneEffect` (5) | Also **fixes a gap**: only the "alone" form has the `CombatAttackService:125-129` pre-filter. Read by `magical-vibes-ai` |
| `CantHaveCountersEffect(Set<CounterType>)` | `CantHaveMinusOneMinusOneCountersEffect` | Readers are asymmetric (own+granted vs granted-only) — preserve or deliberately unify |
| `BlockerCountRestrictionEffect(Integer min, Integer max)` | 2 classes | Fold in `EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect` in the same pass |
| `AdditionalCombatPhaseEffect(count, withMainPhase)` | `AdditionalCombatMainPhaseEffect` (3) | Records identical; the engine still needs both `GameData` counters. Modest payoff |
| `PutCountersOnSourceEqualTo{Dying,Entering}PowerEffect` | 2 classes | Non-optional paths differ (`stack.add` vs `enqueue`); both current cards use `optional=true` |
| `PutAuraFromHandOntoSelfEffect(capByX, exileIfDeclined)` | 2 classes | X variant passes an extra arg to `beginTargetedCardChoice` for the decline path |
| `LookAtTopCardMayPutMatchingOntoBattlefieldEffect` | `LookAtTopCardPutLandOrCreatureWithinLoyaltyEffect` (1) | Both mayfx handlers already delegate to the same method; needs a library-card predicate for "MV ≤ loyalty counters" |
| `GrantFlashbackToGraveyardCardsEffect(+ targeted)` | 1 class | Mass path skips cards with a native `FlashbackCast`; target path has fizzle logging. Both must survive |
| `AssignNoCombatDamageAndDefendingPlayerDiscardsEffect` → composition | 1 class | **Blocked**: `CombatBlockService:940-957` bakes the defender into `targetId`, but `DiscardEffectHandler`'s `DEFENDING_PLAYER` branch reads `getAttackedTargetId()` (`:104`). Fix the collector first |
| `BoostSelfWhenCombatOpponentMatchesEffect` | `BoostSelfWhenBlockingKeywordEffect` (3) | **Rules question, not mechanical**: keyword variant is checked at trigger time, survivor re-checks at resolution. Settle which is right (verify the CR with the `rules` MCP) before merging |
| `CreateTokenCopyOfSourceEffect` + subject selector | `CreateTokenCopyOfEquippedCreatureEffect` (1) | Prefer extracting a shared `TokenCopyFactory` over merging the records |

---

## Phase 4 — High churn, needs sign-off

### Step 29 — Ask before doing any of these

| Merge | Cards touched | Why it needs a decision |
|---|---|---|
| `BoostAllOwnCreaturesEffect` → `BoostAllCreaturesEffect` + `EachPermanentScope.CONTROLLER` | **81** | Handlers are the most literal copies in the codebase (byte-identical log format string); purely mechanical, but 81 card-file edits plus `CombatAttackService` and `InnerFlameIgniterEffectHandler`. Schedule alone so it can't hide a real diff |
| `TransformSelfEffect` → `TransformPermanentEffect(scope)` | **81** | 81 import edits to delete 1 class. Poor ratio. Do **not** fold in `TransformToBackFaceEffect` — it deliberately skips `isTransformPrevented` |
| `PutCountersOnSourceEffect` → `PutCountersOnSelfEffect` | **~215** | Genuinely duplicative, but `PutCountersOnSourceEffect` is constructed at runtime by six engine services and is the trigger-materialisation target for several other effects. Needs its own project |

---

## Explicitly rejected

Checked and found to differ semantically. Do not re-propose without new evidence.

- `MustBeBlockedIfAbleEffect` / `MustBeBlockedByAllCreaturesEffect` / `MustAttackEffect` vs the one-shot `SetCombatRequirementThisTurnEffect` constants that share their wording — static ability vs one-shot flag.
- `SkipDrawStepEffect` / `PlayersSkipUntapStepEffect` / `SkipNextUntapEffect` vs the one-shot `SkipNextEffect` kinds that share their wording — static marker vs one-shot, permanent-level vs step-level.
- `DamageCantBePreventedEffect` / `…ThisTurnEffect`; `DoubleControllerDamageEffect` / `…ThisTurnEffect` — static layer effect vs turn flag.
- `BecomeCopyOfTargetCreatureEffect` vs `…UntilEndOfTurnEffect` — deferred `PendingMayAbility` vs immediate `applyCloneCopy` + layer-1 revert.
- `ReturnTargetCardsFromGraveyardToHandEffect` vs `…ToBattlefieldEffect` — 3-line delegation vs the full ETB pipeline. A rewrite, not a merge.
- `DrawCardEffect` (423) / `DrawCardForTargetPlayerEffect` (30) — the target variant carries a CR 603.4 intervening-if re-check and multi-target fan-out.
- `AwardManaEffect` (192) — read structurally by the land-tap collectors (`instanceof AwardManaEffect` to discover what a land produces).
- `SetPowerToughnessToAmountEffect` (67) vs `SetBasePowerToughnessEffect` — CDA vs one-shot layer-7b setter.
- `SacrificeCreatureCost` vs `SacrificePermanentCost` — see Step 10.
- `TapOrUntapTargetPermanentEffect` (17) — a resolution-time mode *choice*, not a fixed tap or untap.
- `TargetPlayerLosesGameEffect` vs `WinGameEffect` — `resolveLoss` (loss replacement applies) vs `canPlayerWinGame` (bypasses it). Both handlers carry comments explaining why.
- `CantLoseGameEffect` vs `CantLoseGameFromLifeEffect` — the former also stops opponents winning.
- `ChooseNumberEffect` vs `ChooseNumberOnEnterEffect` — marker read via interface at `StackResolutionService:659` vs a registered handler. Merging without an ETB exclusion would double-prompt.
- `IfWonClashEffect` / `IfLostClashEffect` — the pair deliberately models both branches.
- `GainLifeEqualToToughness/DamageDealt/DyingCreatureToughness/ControlledCreatureCombatDamage` — marker records with **no handlers**, matched by `instanceof` at trigger-collection time. Reworking that is out of scope for an effect merge.
- Capability interfaces (`AttackCostEffect`, `BlockCostEffect`, `BoardWipeEffect`, `CounterUnlessEffect`, the `ProtectionGrantingEffect` family, …) — these *are* the consolidation mechanism, not duplication.
- Already-consolidated survivors: `ExileGraveyardCardsEffect`, `ReturnCardFromGraveyardEffect`, `ReturnToHandEffect`, `PreventDamageEffect`, `TapPermanentsEffect`, `UntapPermanentsEffect`.

---

## Next-step prompt template

After finishing a step, print the next step's prompt in a copy-pasteable block, in this form:

```
Read agent-docs/EFFECT_MERGE_PLAN.md and execute Step <N> — <title>.

Follow the protocol in that file: verify the merge premises yourself before editing (read both the
records and their handlers), keep behavior identical unless the step says otherwise, respect the
"Do not regress these fixes" table, verify any CR number with the rules MCP, update the affected
cards and agent-docs, and add or update tests. Run only the affected test classes — never the full
suite. Then mark the step DONE in the plan file and print the prompt for the next step.

Do not commit.
```
