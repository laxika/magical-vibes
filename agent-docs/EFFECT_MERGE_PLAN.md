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
| `UntapUpToControlledPermanentsEffectHandler` | Prompts the controller via `MultiPermanentChoiceContext.UntapChosenPermanents` — does not untap the first N in battlefield order |
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
| 4 | Any-color mana family | 7 | LOW | TODO |
| 5 | Combat-requirement / must-attack | 5 | LOW | TODO |
| 6 | `SetLifeTotalEffect` | 3 | LOW | TODO |
| 7 | `SkipNextEffect` | 3 | MED | TODO |
| 8 | Phase-out + attached-counter placement | 4 | LOW | TODO |
| 9 | Destroy-referenced-permanent | 3 | LOW | TODO |
| 10 | Search-target-library + sacrifice costs | 4 | LOW | TODO |
| 11 | Tap / untap scopes and costs | 3 | LOW | TODO |
| 12 | `Grant*` low-risk batch | 6 | LOW | TODO |
| 13 | Cost-modification batch | 4 | LOW | TODO |
| 14 | Remove-counter batch | 4 | LOW–MED | TODO |
| 15 | Reveal / reorder library batch | 2 | LOW | TODO |
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

### Step 4 — Any-color mana family

**Target**
```java
public record AwardAnyColorManaEffect(DynamicAmount amount,
                                      ManaSpendRestriction restriction,
                                      boolean eachManaChosenSeparately) implements CardEffect
```

**Absorbs**: `AwardXAnyColorManaEffect` (1), `AwardAnyOneColorInstantSorceryOnlyManaEffect` (1),
`AwardFlashbackOnlyAnyColorManaEffect` (1), `AwardAnyColorManaWithInstantSorceryCopyEffect` (1),
`AwardAnyColorCreatureSpellManaEffect` (2), `AwardAnyColorSubtypeSpellOrAbilityManaEffect` (2),
`AwardAnyColorChosenSubtypeCreatureManaEffect` (3).

**Evidence** — the restriction axis is **already unified on the choice side**:
`ChoiceContext.ManaColorChoice` (`ChoiceContext.java:17`) is a single record carrying
`playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly,
spellOrAbilitySubtype, fixedColorOptions, creatureSpellOnly, grantsUncounterable`. The effect side
never followed. The same seven-branch `else if` chain is duplicated verbatim in
`ActivatedAbilityExecutionService:584-666` (activation) and again at `:962-978` (potential-mana
estimation) — collapsing the records collapses all three copies.

`AwardXAnyColorManaEffect` is only "amount comes from xValue"; `AwardManaOfColorsEffect` already
proves `DynamicAmount` + `AmountContext.forManaAbility(permanent, playerId, xValue)` works here
(`:634`).

**Gotchas**
- `AwardAnyColorManaWithInstantSorceryCopyEffect` also does
  `gameData.pendingNextInstantSorceryCopyCount.merge(...)` — a restriction case, not a separate effect.
- `AwardAnyColorManaEffect` is the only one overriding `estimatedCountsAllColors()`; the AI's
  potential-mana model must keep that override for restriction-free instances.

---

## Phase 2 — Scope / enum parameterization, LOW risk

### Step 5 — Combat-requirement / must-attack

**5a Target**
```java
public record SetCombatRequirementThisTurnEffect(CombatRequirement requirement) implements CardEffect
// enum CombatRequirement { MUST_ATTACK, MUST_ATTACK_EFFECT_CONTROLLER, MUST_BLOCK,
//                          MUST_BE_BLOCKED, MUST_BE_BLOCKED_BY_ALL }
```
**Absorbs** (17 cards): `MustAttackThisTurnEffect(boolean)` (7),
`MustBeBlockedByAllCreaturesThisTurnEffect` (4), `MustBeBlockedIfAbleThisTurnEffect` (3),
`MustBlockThisTurnIfAbleEffect` (3).

**Evidence** — all four handlers are the same five lines: `findPermanentById`, null-guard, **one
boolean setter** on `Permanent`, one log line. The four flags are declared adjacently in
`Permanent.java:130-137`, copied together at `:527-531`, and cleared together in
`resetModifiers():1032-1036` — the model already treats them as one family. `MUST_ATTACK_EFFECT_CONTROLLER`
is the only branch that also sets `mustAttackTargetId = entry.getControllerId()`.
Preserve the five distinct log strings via a `switch`.

**5b Target**: `MustAttackNextTurnEffect(TauntTarget tauntTarget)` — `{ EFFECT_CONTROLLER, SOURCE_PERMANENT }`.
**Absorbs**: `MustAttackControllerNextTurnEffect` (1), `MustAttackSourcePermanentNextTurnEffect` (1).
Handlers are line-for-line identical except `gameData.tauntedNextTurn.put(targetPlayerId, X)` where
X is `entry.getControllerId()` vs `entry.getSourcePermanentId()`. Keep the source-null early return
for the `SOURCE_PERMANENT` case.

**Do NOT merge** the non-`ThisTurn` siblings `MustBeBlockedIfAbleEffect` /
`MustBeBlockedByAllCreaturesEffect`: those are **static abilities** read off `EffectSlot.STATIC` at
block-legality time (`CombatBlockService:804,1421`; `GameQueryService:3493`), not one-shot flag
stamps. Different mechanism and lifetime.

---

### Step 6 — `SetLifeTotalEffect`

**Target**: `SetLifeTotalEffect(LifeRecipient who, DynamicAmount amount)` —
`{ CONTROLLER, TARGET_PLAYER, EACH_PLAYER }`.

**Absorbs**: `SetControllerLifeToAmountEffect` (6), `SetEachPlayerLifeToAmountEffect` (1),
`SetTargetPlayerLifeToSpecificValueEffect` (3), `SetTargetPlayerLifeToHalfStartingEffect` (1).

**Evidence** — all handlers end in the identical three lines: `lifeSupport.applySetLifeTotal(...)`,
then `GameLog.text(playerName + "'s life total becomes " + newLife + " (was " + currentLife + ").")`,
then the same `log.info`. Controller/EachPlayer both build `AmountContext.forStackEntry(entry, source)`
with the same source-or-snapshot fallback. `SetTargetPlayerLifeToSpecificValueEffect(int)` is
`Fixed(n)`; `SetTargetPlayerLifeToHalfStartingEffect()` is `Fixed(GameData.STARTING_LIFE_TOTAL / 2)`.
Both target variants declare identical `TargetSpec.benign(TargetPredicates.player())`.

**Stretch (MED, optional)**: `SetEachPlayerLifeToCreatureCountEffect` (1) needs the amount evaluated
*per player*; `SetEachPlayerLifeToHighestAmongPlayersEffect` (1) needs a new `DynamicAmount`
(existing `HighestOpponentLifeTotal` excludes the controller). Skip unless it falls out easily.

---

### Step 7 — `SkipNextEffect`

**Target**: `SkipNextEffect(SkipKind kind, boolean targetsPlayer)` —
`{ TURN, DRAW_STEP, UNTAP_STEP, COMBAT_PHASE }`.

**Absorbs** (8 cards): `SkipNextTurnEffect` (2), `SkipNextDrawStepEffect` (1),
`SkipNextUntapStepEffect` (1), `SkipNextCombatPhaseEffect` (4).

**Evidence** — all four handlers are the same eight lines: resolve a player id,
`gameData.playerIds.contains` guard, `gameData.skipNextXCount.merge(playerId, 1, Integer::sum)`, log.
The two axes are which of the four sibling counters in `GameData:251-261`
(`skipNextCombatPhaseCount`, `skipNextDrawStepCount`, `skipNextTurnCount`, `skipNextUntapStepCount`),
and `entry.getControllerId()` (Turn, DrawStep) vs `entry.getTargetId()` (UntapStep, CombatPhase).
`SkipNextCombatPhaseEffect` **already** has `boolean targetsPlayer`.

**Risk MED** — `SkipNextCombatPhaseEffect` implements `CombatDamageTriggerContextEffect` returning
`TriggerContext.DAMAGED_PLAYER`, and `targetSpec()` differs per kind (`harmful` for untap-step vs
`benign` for combat-phase). Both must become `switch (kind)`. Turn-engine consumers are untouched.

**Do NOT merge**: `SkipDrawStepEffect` (static marker read by `StepTriggerService`, no handler) and
`SkipNextUntapEffect` (marks per-`Permanent` `skipUntapCount`; the step still happens). The
distinction is documented in their javadocs.

---

### Step 8 — Phase-out + attached-counter placement

**8a Target**: `PhaseOutEffect(PhaseOutSubject subject)` — `{ SOURCE, TARGET, ATTACHED }`.
**Absorbs** (12 cards): `PhaseOutSelfEffect` (7), `PhaseOutTargetPermanentEffect` (4),
`PhaseOutEnchantedCreatureEffect` (1).
All three handlers end in `phasingService.phaseOut(gameData, List.of(permanent))`; only the lookup
differs (`getSourcePermanentId()` / `getTargetId()` / `findPermanentById(source).getAttachedTo()`).
Attachments, combat removal and phase-in all live inside `PhasingService`. Switch `targetSpec()` on
`subject`, exactly as `PutTargetOnTopOfLibraryEffect` already does for `PutOnTopOfLibraryScope`.
**Keep separate**: `PhaseOutPermanentsEffect` (mass, filter-driven battlefield scan).

**8b Target**: `PutCounterOnAttachedPermanentEffect(CounterType counterType, int count, PermanentPredicate condition)`.
**Absorbs** (17 cards): `PutCounterOnEnchantedCreatureEffect` (12),
`PutCountersOnEquippedCreatureEffect` (5). Optionally fold `PutCounterOnTriggeringPermanentEffect` (1)
with a `subject` enum (`ATTACHED` / `TRIGGERING`) for 3→1.
Both handlers do the same three steps and end in the identical
`permanentCounterSupport.placeCounterOnPermanent(gameData, entry, creature, counterType, count)`.
Aura-vs-Equipment is not a semantic axis — both resolve through `Permanent.getAttachedTo()`.
`placeCounterOnPermanent` centrally handles `cantHaveCounters`, doubling, -1/-1 reduction and triggers.

---

### Step 9 — Destroy-referenced-permanent

**Target**: `DestroyReferencedPermanentEffect(PermanentReference ref, boolean cannotBeRegenerated)` —
`{ SOURCE, ENCHANTED, TRIGGERING }`.

**Absorbs** (12 cards): `DestroySourcePermanentEffect` (3), `DestroyEnchantedPermanentEffect` (8),
`DestroyTriggeringPermanentEffect` (1).

**Evidence** — all three records are exactly `(boolean cannotBeRegenerated)` with a no-arg ctor
defaulting to `false`, no `targetSpec()`, no other members. All three handlers inject exactly
`DestructionSupport` + `GameQueryService` and reduce to: resolve one permanent id, null-check,
`destructionSupport.tryDestroyAndLog(gameData, perm, entry.getCard().getName(), cannotBeRegenerated)`.
The only variation is which id: `getSourcePermanentId()`; `findPermanentById(source).getAttachedTo()`
guarded by `aura.isAttached()`; `getTriggeringPermanentId()`.

**Leave out**: `DestroyLinkedPermanentEffect` (1, Merieke Ri Berit) — it also clears
`source.setChosenPermanentId(null)` so a second untap doesn't re-destroy, and carries a baked `UUID`.

**Also in this step**: `DestroyPermanentsTargetPlayerControlsEffect` (1, Ajani Vengeant −7) →
`DestroyAllPermanentsEffect(filter, cannotBeRegenerated, EachPermanentScope.TARGET_PLAYER, …)`,
which already serves exactly this shape for Rain of Daggers and Overwhelming Forces. Its handler was
already routed through `destroyBatchCollecting`, so this is now a straight record swap. Ajani's
ability needs an explicit player target filter (`target(new PlayerPredicateTargetFilter(...))`), as
Rain of Daggers does.

---

### Step 10 — Search-target-library + sacrifice costs

**10a Target**: `SearchTargetLibraryEffect(DynamicAmount count, CardPredicate filter, LibrarySearchDestination destination, boolean canFailToFind)`
— the target-player mirror of the 165-use `SearchLibraryEffect`, which already carries
`count/filter/destination`.
**Absorbs**: `SearchTargetLibraryForCardsToExileEffect` (3),
`SearchTargetLibraryForCardsToGraveyardEffect` (1),
`SearchTargetLibraryForCardToBattlefieldUnderControlEffect` (1).
All three run the same pipeline ending in
`librarySearchSupport.sendLibrarySearchToPlayer(...)` with a `LibrarySearchParams.builder(...)`;
the **only** varying builder argument is `.destination(...)`
(`EXILE` / `GRAVEYARD` / `BATTLEFIELD_UNDER_SEARCHER`).
*Reconcile*: the exile/graveyard handlers use `isSearchPrevented(...)`, the battlefield one uses
`checkSearchRestriction(...)` and shuffles on refusal. Pick the rules-correct one and apply it
uniformly — the target still shuffles.

**10b**: `SacrificeArtifactCost` (15 cards) → `SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false)`.
Also deletes `service/ability/cost/ArtifactSacrificeCostHandler.java`.
`SacrificeArtifactCost` already declares `consumedPermanentFilter() -> new PermanentIsArtifactPredicate()`
— the exact value `SacrificePermanentCost.filter()` holds. `ArtifactSacrificeCostHandler` is
`MultiplePermanentSacrificeCostHandler` with `count=1`.
*Touches*: `AbilityActivationService:2225`, `AdditionalSpellCostService:77,207,289`, and the
`ExtractedCosts` record field.
**Stretch**: fold `SacrificeMultiplePermanentsCost` (18) into `SacrificePermanentCost` by adding
`count` — `MultiplePermanentSacrificeCostHandler` already serves both through two constructors
differing only in `count`.
**Do NOT merge** `SacrificeCreatureCost` (86): it overrides `sacrificesChosenCreature() -> true`, is
special-cased in `SpellCastingService:2207` and `AbilityActivationService:2667`, and carries a
`ManaColor trackSacrificedColorSymbols` field.

---

### Step 11 — Tap / untap scopes and costs

**11a**: `UntapEquippedCreatureEffect` (3 cards) → `UntapPermanentsEffect(TapUntapScope.ENCHANTED)`.
`UntapEquippedCreatureEffectHandler.resolve` and `UntapPermanentsEffectHandler.resolveEnchanted` are
the same algorithm through the same three guards to the same `tapUntapSupport.untapPermanent(...)`.
The class name is actively misleading — 2 of its 3 users are Auras, which is what `ENCHANTED`
("the permanent the source aura is attached to") already covers via the same `getAttachedTo()`.

**11b**: `UntapUpToControlledPermanentsEffect` (2 cards) → `UntapPermanentsEffect` with a
`chosenCount` field, mirroring `TapPermanentsEffect` which already has exactly that field
("0 = all in scope; >0 = up to N"). Keep the existing `(scope)` / `(scope, filter)` convenience
constructors so none of the 156 existing call sites change.
**Critical**: the controller-choice flow added in `9b8147333` must be preserved — the merged handler
must still prompt via `MultiPermanentChoiceContext.UntapChosenPermanents`, and
`RewindTest`/`UnwindTest` must keep passing unchanged.

**11c**: `TapXPermanentsCost` (1 card) → `TapMultiplePermanentsCost` with `count` widened from `int`
to `DynamicAmount`. `TapXPermanentsCostHandler` and `MultiplePermanentTapCostHandler` have
character-identical `getValidChoiceIds`, `validateAndPay` and `getPromptMessage`; the sole difference
is `requiredCount()` returning `xValue` vs `cost.count()`.
**Stretch (MED)**: `TapCreatureCost` (13 cards) is the same handler with `requiredCount() == 1`, an
`isCreature` filter (expressible as `PermanentIsCreaturePredicate`), `excludeSelf` (== `excludeSource`)
and `trackTappedCreaturePower` — used by only **1** of its 13 cards (Impelled Giant, consumed at
`AbilityActivationService:2199,2391`).

**Do NOT merge** `TapPermanentsEffect` (135) with `UntapPermanentsEffect` (156): both are already
consolidated survivors, and their scopes genuinely diverge (`ALL_TARGETS` is a dead path for tap but
a live multi-target scope for untap). 291 files of churn for a mode flag.

---

### Step 12 — `Grant*` low-risk batch

| Target | Absorbs | Cards |
|---|---|---|
| `GrantChosenKeywordEffect(List<Keyword> options, GrantScope scope)` | `GrantChosenKeywordToSelfEffect`, `GrantChosenKeywordToTargetEffect` | 2 + 2 |
| `GrantControllerKeywordEffect(Keyword keyword)` | `GrantControllerShroudEffect`, `GrantControllerHexproofEffect` | 2 + 4 |
| `GrantSpellCastingAbilityToSpellsEffect(Keyword grantedAbility, CardPredicate filter)` | `GrantConspireToSpellsEffect`, `GrantConvokeToSpellsEffect` | 1 + 1 |
| `GrantProtectionChoiceUntilEndOfTurnEffect` + `filter` + `GrantScope.OWN_CREATURES` | `GrantProtectionChoiceToOwnCreaturesUntilEndOfTurnEffect` | 1 |
| `GrantPermanentNoMaxHandSizeEffect` + duration | `GrantNoMaximumHandSizeUntilNextTurnEffect` | 1 |

**Notes**
- `GrantChosenKeyword*`: the two handlers are the same file; both end in
  `playerInputService.beginKeywordChoice(gameData, entry.getControllerId(), permanent.getId(), e.options())`.
  Update the name-keyed entry in `TargetPolarityClassifier` (magical-vibes-ai).
- `GrantControllerKeyword*`: both are empty records consumed only by the adjacent pair
  `GameQueryService.playerHasShroud` / `playerHasHexproof` (`:3167-3180`), each a one-liner
  `playerBattlefieldHasStaticEffect(gameData, playerId, X.class)`. Needs a keyword-matching sibling
  to that method. Touches `GameQueryServiceTest` and `TrueBelieverTest`.
- `GrantSpellCastingAbility*`: uniquely clean — **nothing dispatches on the concrete types**. Both
  consumers (`GameActionAvailabilityService:453`, `SpellCastingService:164`) match on the
  `SpellCastingAbilityGrantingEffect` capability interface, which already carries both merged fields.
  Zero handler beans exist.
- `GrantProtectionChoice*`: both terminate in
  `playerInputService.beginProtectionColorChoice(gameData, chooserId, recipientIds, includeArtifacts)`;
  the surviving handler already has `resolveRecipientIds(entry, e)` switching on scope.
- `GrantNoMaximumHandSize*`: one-line handlers adding `entry.getControllerId()` to
  `playersWithNoMaximumHandSizeUntilNextTurn` vs `playersWithNoMaximumHandSize`.

**Do NOT merge**: `GrantProtectionFromCardTypeUntilEndOfTurnEffect` vs
`GrantProtectionFromColorUntilEndOfTurnEffect` — different `Permanent` fields with different
lifetimes (`getProtectionFromCardTypes()` is not an until-EOT bucket and is also written by
`ChoiceHandlerService`).

---

### Step 13 — Cost-modification batch

| Target | Absorbs | Cards |
|---|---|---|
| `IncreaseSpellCostEffect(CardPredicate predicate, int amount, CostTaxScope scope)` — `{ALL_PLAYERS, CONTROLLER, OPPONENTS}` | `IncreaseOwnCastCostEffect`, `IncreaseOpponentCastCostEffect` | 1 + 1 |
| `LimitSpellsPerTurnEffect(int maxSpells, SpellLimitScope scope)` — `{EACH_PLAYER, CONTROLLER, ENCHANTED_PLAYER}` | `LimitSpellsForControllerEffect`, `LimitSpellsForEnchantedPlayerEffect` | 1 + 1 |
| `ReduceCastCostForMatchingSpellsEffect(predicate, amount, CostModificationScope.SELF)` | `ReduceOwnCastCostForCardTypeEffect` | 1 |
| `ReduceOwnCastCostIfTargetingPermanentEffect(predicate, amount, boolean controlledOnly)` | `ReduceOwnCastCostIfTargetingControlledPermanentEffect` | 1 |

**Notes**
- `IncreaseSpellCost*`: the three handlers in `service/cast/costmod/` are the same three lines; the
  own/opponent variants add exactly one `source.controlledBy(context.castingPlayerId())` guard (and
  its negation). `IncreaseOpponentCastCostEffect` uses `Set<CardType>` + `.contains(spell.getType())`
  — confirm the multi-type card (`AuraOfSilence`) still matches under `CardTypePredicate`.
- `LimitSpells*`: all three are `record X(int maxSpells)` with exactly one consumer,
  `CastingPermissionService.getMaxSpellsPerTurn:101-125` — three adjacent
  `if (effect instanceof …) limit = Math.min(limit, x.maxSpells());` branches differing only in the
  applies-to guard. No handler beans, no AI references.
- `ReduceOwnCastCostForCardTypeEffect`: **behavior change** — the absorbed handler tests
  `contains(context.spell().getType())` (primary type only) whereas `CardTypePredicate` uses
  `hasType(...)`, so an artifact creature would newly match. That is a fix; confirm Heartless
  Summoning's intent before shipping.
- `ReduceOwnCastCostIfTargeting*`: identical components `(PermanentPredicate, int)`, consumed only by
  `CastingCostService.computeTargetBasedCostReduction:537-560` and
  `GameActionAvailabilityService:413-436`; the controlled variant adds one `findPermanentController`
  line and swaps `battlefieldHasPermanentMatching` for `controlsPermanent`.

---

### Step 14 — Remove-counter batch

| Target | Absorbs | Cards |
|---|---|---|
| `RemoveCounterAndGainLifeEffect(CounterType, int lifeGain, boolean fromTarget)` | `RemoveCounterFromSourceAndGainLifeEffect`, `RemoveCounterFromTargetAndGainLifeEffect` | 2 + 2 |
| `RemoveAllCountersEffect(CounterType, boolean fromTarget)` | `RemoveAllCountersFromSelfEffect`, `RemoveAllCountersFromTargetPermanentEffect` | 7 + 1 |
| `RemoveCounterFromTargetPermanentEffect(CounterType, PermanentPredicate, int amount)` | `RemoveChargeCountersFromTargetPermanentEffect` | 1 |
| `RemoveCounterFromSourceCost(count, CounterType.CHARGE)` | `RemoveChargeCountersFromSourceCost` | 23 |

**Notes**
- Gain-life pair: identical components `(CounterType, int lifeGain)`; handlers are the same seven
  statements including the `getCounterCount(ct) <= 0` early return that implements "If you do". The
  source form's fallback (`getSourcePermanentId() != null ? … : getTargetId()`) **is** the target form.
- `RemoveAllCounters*`: identical single `CounterType` component; both do
  `removed = getCounterCount(ct)` → `setCounterCount(ct, 0)` → identical log. The self form also does
  `entry.setEventValue(removed)` (Ashling) — harmless on both paths.
  **Gotcha**: the self form implements `CombatDamageTriggerContextEffect` with
  `TriggerContext.SOURCE_SELF` and `TargetSpec(NONE, …, requiresSource=true, 1)`, the target form is
  `TargetSpec.benign(CREATURE)` — branch `targetSpec()`/`combatDamageTriggerContext()` on the flag.
- Charge-counter cost: `AbilityActivationService` availability check `~:2870` is the `default ->`
  branch of the generic check `~:2799`, and payment `~:2055` is the generic `default ->` at `~:1996`.
  **Decision required**: `RemoveChargeCountersFromSourceCost` overrides
  `CostEffect.sourceCountersRemoved()` to return `count` while `RemoveCounterFromSourceCost` inherits
  `0`. The only consumer is `magical-vibes-ai/.../SpellEvaluator.java:161`. Adding the override to the
  survivor is arguably correct but changes AI cost estimation for its 39 existing cards — decide
  deliberately, do not let it ride in silently.

---

### Step 15 — Reveal / reorder library batch

| Target | Absorbs | Cards |
|---|---|---|
| `ReorderTopCardsOfLibraryEffect(int count, boolean targetPlayer)` | `ReorderTopCardsOfTargetLibraryEffect` | 8 + 3 |
| `RevealTopCardOfLibraryEffect` + `boolean targetPlayer` | `RevealTopCardOfOwnLibraryEffect` | 2 + 3 |

**Notes**
- Reorder: identical single `int count` component; handlers share the same `Math.min`, empty-library
  log, `count == 1` look-only shortcut, `deck.subList(0, count)` snapshot-and-clear, and
  `PendingInteraction.LibraryReorder(controllerId, topCards, false, <deckOwnerId>, prompt)`. The
  target variant already falls back to `controllerId` when `getTargetId()` is null — it is a strict
  generalization. LOW risk, MED churn (11 card classes).
- Reveal: handlers are copies (resolve deck, empty-library log, identical `GameLog.textCardText`);
  differences are `getTargetId()` vs `getControllerId()` and the survivor's extra `lifeGainIfLand`
  rider (already defaulted to 0). The survivor implements `LifeGainEffect`, which stays valid with
  `Fixed(0)`.

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

- `MustBeBlockedIfAbleEffect` / `MustBeBlockedByAllCreaturesEffect` vs their `*ThisTurn` counterparts — static ability vs one-shot flag.
- `SkipDrawStepEffect` vs `SkipNextDrawStepEffect`; `SkipNextUntapEffect` vs `SkipNextUntapStepEffect` — static marker vs one-shot, permanent-level vs step-level.
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
