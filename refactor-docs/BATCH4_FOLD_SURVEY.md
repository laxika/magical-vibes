# Batch 4 fold survey — And/Then composite records

Survey session (read-only; no code changes, nothing committed). Follows the Batch-3 closeout's "NEXT
SURVEY TASK": re-scan the remaining `And[A-Z]`/`Then[A-Z]` composite records for the **Dominus shape**
(unconditional, in-order, no data flow between steps, currently bespoke only because a single-child wrapper
or trigger slot can't hold two effects) that now folds with the proven `SequenceEffect` recipe, and rank
candidates by user count. Screens out the failable-gate "if you do" family (still blocked — see Batch-3
closeout §b).

## Method
- Population: case-**sensitive** `And[A-Z]|Then[A-Z]` over `magical-vibes-domain/.../model/effect/*.java`
  → **133 files** (132 effect records + `ThenEffectRecipient`, a support enum).
- User counts: `new <Record>(` occurrences across all `src/main/java` (build/ excluded), effect's own def
  file excluded. Ranked list produced; every record scoring ≥2 was read, plus a representative sample of the
  score-1 tail and every plausible clean-bundle name.
- Classification comes from reading the record javadoc/fields (and, where the shape was ambiguous, the handler
  and/or the using card).

## Headline finding
**The And/Then population is overwhelmingly NOT Dominus-shaped.** The top of the user-count ranking is entirely
non-foldable, and clean folds are rare and low-use:

- `DestroyTargetPermanentThenEffect` (**15 users** — #1) is already a *generic reuse primitive*, not a bespoke
  composite: it takes a child `CardEffect thenEffect` and carries machinery `SequenceEffect` deliberately lacks —
  a last-known-stat snapshot onto the `eventValue` channel (data flow), `ThenEffectRecipient` routing
  (CONTROLLER / TARGET_CONTROLLER / TARGET_OWNER), and an optional `thenCondition` predicate gate. **Keep it.**
  It is the *target* several other composites should fold *into* (Tier 3 below), not a fold source.
- `SacrificePermanentThenEffect` (4) and `RemoveCounterFromTargetAndGainLifeEffect` (2) are explicit failable
  gates ("If **and only if** … / If you do") → **BLOCKED**, same class as Batch-3 §b.
- `SearchLibraryAndOrGraveyardForNamedCardToHandEffect` (4) and `ThenEffectRecipient` (0) are name
  false-positives (a single atomic two-zone search; a support enum).

Most of the remaining ~120 records are non-foldable for one of these reasons:
- **Data flow between steps** (the large majority): `…Per<X>`, `…EqualTo<X>`, `…ThatMany`, `…FromDestroyedCount`,
  `…ByManaValue`, `…FromHalfLifeTotal`, treasure/tokens sized by the first step, "copy of the exiled/destroyed
  card", imprint/track linkage, "draw that many", "shuffle then draw that many", etc. `SequenceEffect` has no
  inter-step data channel by design.
- **Failable gate** ("if you do" / sacrifice-gate / discard-gate) → the blocked family.
- **Atomic single operation** despite the `And` in the name: `…AndOr…` two-zone search, prevention shields
  (`PreventAll…ToAndBy…`, `…ToControllerAndCreatures`), one search distributing to two destinations, one AoE
  damage sweep over creatures-and-planeswalkers.
- **Shared-target / recipient routing** the second step needs (e.g. damage to *the target's* controller) — those
  fold into the `DestroyTargetPermanentThenEffect` generic (Tier 3), not plain `SequenceEffect`.

## Tier 1 — confirmed clean Dominus folds (do these first)  — ✅ SHIPPED as Batch 4
Self-only, unconditional, no data flow, all sub-effects already exist, each sits on a single-child wrapper/slot
that forced the bespoke record → exactly the Batch-3 recipe. All three have **1 user** (the ranking barely
separates them; confidence is what ranks them here).

> **Correction (found during implementation):** the "Wrapper / slot" note below claimed `ClashEffect` was a
> splice-capable single-child wrapper. It is **not** — `ClashEffectHandler` (like `FlipCoinWinEffectHandler`)
> dispatches its win reward directly via the registry, not through `resolveEffectsLoop`, so `SequenceEffect`
> (which has no handler) would be silently dropped. Batch 4 added a synchronous `SequenceEffect` expansion to
> both handlers to make the Sentry Oak fold work. Lesson: confirm a wrapper re-enters `resolveEffectsLoop`
> before assuming `SequenceEffect` composes under it. See `COMPOSITE_FOLD_PROGRESS.md` Batch 4.

| Record | Users | Fold recipe | Wrapper / slot that blocked it |
|--------|-------|-------------|--------------------------------|
| `BoostSelfAndLoseKeywordEffect` | 1 (Sentry Oak) | `SequenceEffect.of(new BoostSelfEffect(p,t), new RemoveKeywordEffect(kw))` | win reward inside `ClashEffect` (single child) — javadoc already says "Combines BoostSelfEffect and RemoveKeywordEffect (SELF scope) … as a single wrapped reward" |
| `RemoveCountersAndTransformSelfEffect` | 1 (Primal Amulet) | `SequenceEffect.of(new RemoveAllCountersFromSelfEffect(type), new TransformSelfEffect())` | accepted-half of a `MayEffect` ("you may remove counters and transform") |
| `TapAndTransformSelfEffect` | 1 (Homicidal Brute) | `SequenceEffect.of(new TapPermanentsEffect(TapUntapScope.SELF), new TransformSelfEffect())` | end-step trigger slot (`TapPermanentsEffect(SELF)` *is* the former `TapSelfEffect`) |

All three sub-effects verified present. `RemoveAllCountersFromSelfEffect` snapshots the removed count onto
`eventValue`, but the transform step doesn't read it, so the fold is behavior-identical.

## Tier 2 — candidate, but needs a missing primitive or targetSpec care (verify before folding)
| Record | Users | Blocker to resolve |
|--------|-------|--------------------|
| `DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect` | 2 (Brainstorm, Dream Cache) | no standalone "put K cards from hand onto top/bottom of library" effect exists; would need extracting first |
| `DiscardOwnHandThenDrawEffect` | 1 (Knollspine Dragon) | `SequenceEffect.of(DiscardHandEffect, new DrawCardEffect(dynAmount))` works (both exist; `DrawCardEffect` takes a `DynamicAmount`), but the wrapper must surface the **PLAYER** targetSpec for the `DamageDealtToTargetPlayerThisTurn` amount — `SequenceEffect` does not currently aggregate child target specs |
| `BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect` | 1 (Diviner's Wand) | needs standalone "boost equipped creature" + "grant keyword to equipped creature" effects (only `GrantKeywordEffect` / `BoostSelfEffect` / equip-scoped variants exist; equipped-creature scope not separable today) |

## Tier 3 — fold into the existing generic, NOT `SequenceEffect`
These have a second step routed to *the destroyed target's* controller, which `DestroyTargetPermanentThenEffect`
already models via `ThenEffectRecipient`. Migrate them onto that generic (its own separate mini-batch), not onto
`SequenceEffect`.

| Record | Users | Suggested target |
|--------|-------|------------------|
| `DestroyTargetLandAndDamageControllerEffect` | 2 | `DestroyTargetPermanentThenEffect(DealDamageToPlayers…, TARGET_CONTROLLER, <LAND predicate>)` |
| `DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect` | 2 | `DestroyTargetPermanentThenEffect(<search>, TARGET_CONTROLLER)` |
| `DestroyTargetThenRevealUntilTypeToBattlefieldEffect` | 1 | `DestroyTargetPermanentThenEffect(<reveal-until>, CONTROLLER)` — confirm the reveal step is unconditional |
| `DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect` | 1 | `DestroyTargetPermanentThenEffect(<each-player search>, …)` — confirm recipient semantics |

## Explicitly rejected (looked like candidates, aren't)
- `DestroyBlockedCreatureAndSelfEffect` (2, Loyal Sentry / Alaborn Zealot) — dispatched from
  `CombatBlockService` with block-relationship context; the "destroy the creature this blocked" half has **no**
  standalone primitive (`DestroyCreatureBlockingThisEffect` is the *opposite* relationship). Not a mechanical fold.
- `CounterSpellAndCreateTreasureTokensEffect`, `PutSlimeCounterAndCreateOozeTokenEffect`,
  `ShuffleHandIntoLibraryAndDrawEffect`, `PutHandOnBottomOfLibraryAndDrawEffect`,
  `DiscardOwnHandThenDrawThatManyEffect`, and the whole `…Per…`/`…EqualTo…`/`…ThatMany`/`…Count`/`…ByManaValue`
  set — **data flow** between steps.
- `RemoveCounterFromTargetAndGainLifeEffect`, `SacrificePermanentThenEffect`,
  `SacrificeArtifactThenDealDividedDamageEffect`, `SacrificeTargetThenRevealUntilTypeToBattlefieldEffect`,
  `PutCreatureFromHandThenSacrificeUnlessPayReducedEffect`, `RevealTopCardsBottomThenDamageIfCopyRevealedEffect`,
  `PutCounterOnSelfThenTransformIfThresholdEffect` — **failable gate / conditional** (blocked family, Batch-3 §b).
- Prevention shields (`PreventAll…ToAndBy…`, `PreventAllDamageToControllerAndCreatures…`, `PreventDamageAnd…`
  counters) — continuous/replacement, not one-shot sequences.
- Atomic multi-zone/multi-destination single ops: `SearchLibraryAndOrGraveyardForNamedCardToHandEffect`,
  `SearchLibraryForCardToHandAndCardToGraveyardEffect`, `EachPlayerShufflesHandAndGraveyardIntoLibraryEffect`,
  `ShuffleSelfAndGraveyardIntoLibraryEffect`, `DealDamageToAllCreaturesAndPlaneswalkers…`.

## Recommendation
Batch 4 is **small**: only three high-confidence one-user folds (Tier 1). That is a modest, low-risk mini-batch
— worth doing to retire three more records with the established recipe, but it will not move the metrics much
(≈ −3 types, composite count 133 → ~130). Tier 3 (fold-into-`DestroyTargetPermanentThenEffect`) is a *separate*
generic-consolidation batch with better leverage (recipient routing already built) and should be scoped on its
own. Tier 2 each require extracting a missing primitive first.

**The wrapper-blocked-Dominus vein is largely mined out.** After Batch 3 and this Tier 1, the remaining
composite population is dominated by genuinely-distinct shapes (data flow, failable gates, continuous/replacement,
atomic ops) that need real engine-design work — chiefly the **step-success-signal** design from Batch-3 §b —
rather than more `SequenceEffect` folding.
