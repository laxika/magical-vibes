# Static Effect Resolver Migration (`staticfx`)

This is the **shared memory** for the ongoing refactor that breaks the monolithic
`StaticEffectResolutionService` (~66 `@HandlesStaticEffect` methods) into one self-contained
handler class per effect. Read this fully before migrating any batch.

## Why
`StaticEffectResolutionService` is a ~1440-line god class. We are migrating each handler into its
own `@Component` implementing `StaticEffectHandlerBean`, reusing the shared helpers now living in
`StaticEffectSupport`. The legacy monolith and the new bean handlers **coexist** during migration —
they handle disjoint effect types, so order is irrelevant.

## The established pattern

1. **Package is `staticfx`, not `static`.** `static` is a reserved word and cannot be a package
   segment. All new classes live in
   `magical-vibes-backend/.../service/effect/staticfx/`.
2. **Naming convention.** The non-self handler is `<EffectName>Handler` (e.g.
   `MetalcraftConditionalEffectHandler` for `MetalcraftConditionalEffect`). The self handler inserts
   `Self` before the trailing `Effect`: `<...>SelfEffectHandler` (e.g.
   `MetalcraftConditionalSelfEffectHandler`). An effect with only a self handler still uses the
   `SelfEffectHandler` suffix.
3. **One `@Component` per handler**, implementing `StaticEffectHandlerBean`:
   - `Class<? extends CardEffect> handledEffect()` — the effect type it handles.
   - `boolean selfOnly()` — `true` for characteristic-defining / self handlers (registered into the
     registry's `selfHandlers` map), `false` (the default) for the "others" / broad-scope handlers.
   - `void apply(StaticEffectContext, CardEffect, StaticBonusAccumulator)` — the body, moved
     **verbatim** from the monolith.
   - Constructor-inject `StaticEffectSupport` and/or `GameQueryService` as needed
     (`@RequiredArgsConstructor`).
4. **Behavior must be identical.** This is a pure refactor. Move the method body verbatim and only
   swap `this.helper(...)` calls for `support.helper(...)` / `gameQueryService.…`. Do not "improve"
   logic.
5. **Self vs non-self mapping.** If the monolith method had `@HandlesStaticEffect(value = X.class,
   selfOnly = true)` → bean's `selfOnly()` returns `true`. If it had
   `@HandlesStaticEffect(X.class)` (or `selfOnly = false`) → leave `selfOnly()` as default `false`.
   An effect with BOTH a self and a non-self method becomes **two** beans (see Metalcraft).
6. **Dual registration.** Every migrated handler must be registered in **two** ways:
   - As a Spring `@Component` (collected via `List<StaticEffectHandlerBean>` in
     `EffectRegistryConfig` and registered through `StaticEffectHandlerBeanFactory.registerAll`).
   - Added to `StaticEffectHandlerBeanFactory.createAll(...)` so the **non-Spring** sites
     (`GameTestHarness`, `GameSimulator`) register it too. `createAll` is the single source of truth
     for non-Spring instantiation — those two sites never need per-batch edits again.
7. **Delete the monolith method after migrating.** Remove the `@HandlesStaticEffect` method (both
   self and non-self variants) from `StaticEffectResolutionService` once the bean(s) exist and are
   registered. The legacy reflection scan must not also pick it up.

## Infrastructure (already in place)

- `staticfx/StaticEffectHandlerBean.java` — interface (extends `StaticEffectHandler`).
- `staticfx/StaticEffectSupport.java` — `@Component`, holds all shared helpers (constructor-injects
  `GameQueryService`).
- `staticfx/StaticEffectHandlerBeanFactory.java` — `createAll(support, gameQueryService, registry)`
  returns every migrated bean; `registerAll(beans, registry)` registers each into the registry
  (self vs non-self based on `bean.selfOnly()`).
- `EffectRegistryConfig` — field-injects `List<StaticEffectHandlerBean>` (constructor injection
  would create a bootstrap cycle via the `staticEffectHandlerRegistry` `@Bean`) and calls
  `registerAll` in `afterSingletonsInstantiated()`, alongside the legacy `@HandlesStaticEffect`
  scan. Logs the bean-handler count.
- `GameTestHarness` & `GameSimulator` — construct a `StaticEffectSupport`, build the registry, scan
  the legacy monolith, then call `StaticEffectHandlerBeanFactory.createAll(...)` + `registerAll(...)`.

## `StaticEffectSupport` public method list

These are the shared helpers available to handlers (all `public`, behavior identical to the
original monolith privates):

- `boolean matchesStaticFilter(Permanent target, PermanentPredicate filter)`
- `static boolean isCreatureSubtype(CardSubtype subtype)`
- `boolean matchesCreatureScope(StaticEffectContext context, GrantScope scope, PermanentPredicate filter)`
- `boolean isEffectivelyCreature(Permanent permanent, boolean hasAnimateArtifacts)`
- `boolean isEffectivelyCreature(GameData gameData, Permanent permanent, boolean hasAnimateArtifacts)`
- `int countControlledPermanents(StaticEffectContext context, Predicate<Permanent> filter)`
- `UUID findControllerId(GameData gameData, Permanent permanent)`
- `boolean hasAnimateArtifactEffect(GameData gameData)`
- `int countCardsInAllGraveyards(GameData gameData, CardPredicate filter)`
- `boolean isControllerLifeAtOrBelow(StaticEffectContext context, int threshold)`
- `boolean isTopCardOfLibraryColor(StaticEffectContext context, CardColor color)`
- `boolean isEquipped(StaticEffectContext context)`
- `void applySelfOnlyConditionalStaticEffect(StaticEffectContext context, CardEffect wrapped, StaticBonusAccumulator accumulator)`

(`NON_CREATURE_SUBTYPES` is a private static set backing `isCreatureSubtype`.)

While migration is in progress, `StaticEffectResolutionService` keeps thin **private delegating
wrappers** (e.g. `matchesStaticFilter(...)` → `support.matchesStaticFilter(...)`) so its
not-yet-migrated handler bodies remain unchanged. These wrappers shrink as handlers migrate and
disappear at final cleanup.

## Worked example: Metalcraft (DONE)

- `staticfx/MetalcraftConditionalSelfEffectHandler` — `handledEffect()=MetalcraftConditionalEffect.class`,
  `selfOnly()=true`.
- `staticfx/MetalcraftConditionalEffectHandler` — `handledEffect()=MetalcraftConditionalEffect.class`,
  `selfOnly()=false`.
- Both added to `StaticEffectHandlerBeanFactory.createAll(...)`.
- `resolveMetalcraftConditional` and `resolveMetalcraftConditionalOthers` deleted from the monolith.

## Remaining batches (checklist)

Each item is a monolith `@HandlesStaticEffect` method to migrate. Effects marked `(self+non-self)`
have two methods → two beans.

- [x] **Pilot:** Metalcraft (`MetalcraftConditionalEffect`, self + non-self)

### Batch A — keyword/type/color grants
- [x] `GrantKeywordEffect`
- [x] `RemoveKeywordEffect`
- [x] `GrantColorEffect`
- [x] `GrantSubtypeEffect`
- [x] `GrantCardTypeEffect`
- [x] `GrantSupertypeToEnchantedPermanentEffect`
- [x] `LosesAllAbilitiesEffect`
- [x] `SetBasePowerToughnessStaticEffect`

### Batch B — boosts/grants (self + non-self) and abilities
- [x] `StaticBoostEffect` (self + non-self)
- [x] `GrantEffectEffect` (self + non-self)
- [x] `GrantActivatedAbilityEffect`
- [x] `ProtectionFromColorsEffect`

### Batch C — enchanted/chosen/animate
- [x] `EnchantedPermanentConditionalEffect`
- [x] `EnchantedPermanentBecomesTypeEffect`
- [x] `EnchantedPermanentBecomesChosenTypeEffect`
- [x] `GrantChosenSubtypeToOwnCreaturesEffect`
- [x] `BoostCreaturesOfChosenColorEffect`
- [x] `BoostCreaturesOfChosenSubtypeEffect`
- [x] `AnimateNoncreatureArtifactsEffect`
- [x] `GrantEquipByManaValueEffect`

### Batch D — boost creature per X
- [x] `BoostCreaturePerCardsInAllGraveyardsEffect`
- [x] `BoostCreaturePerCardsInControllerGraveyardEffect`
- [x] `BoostCreaturePerMatchingLandNameEffect`
- [x] `BoostCreaturePerControlledSubtypeEffect`
- [x] `BoostCreaturePerControlledCardTypeEffect`
- [x] `BoostBySharedCreatureTypeEffect`

### Batch E — boost self per X
- [x] `BoostSelfPerEnchantmentOnBattlefieldEffect`
- [x] `BoostSelfPerControlledPermanentEffect`
- [x] `BoostSelfPerCardsInControllerGraveyardEffect`
- [x] `BoostSelfPerOpponentPermanentEffect`
- [x] `BoostSelfPerEquipmentAttachedEffect`
- [x] `BoostSelfPerAttachmentEffect`
- [x] `BoostSelfByImprintedCreaturePTEffect`
- [x] `BoostSelfPerOpponentPoisonCounterEffect`
- [x] `BoostSelfBySlimeCountersOnLinkedPermanentEffect`
- [x] `BoostSelfPerOtherControlledSubtypeEffect`
- [x] `BoostByOtherCreaturesWithSameNameEffect`

### Batch F — power/toughness equal to X
- [x] `PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect`
- [x] `PowerToughnessEqualToCardsInAllGraveyardsEffect`
- [x] `PowerToughnessEqualToCardsInControllerGraveyardEffect`
- [x] `PowerToughnessEqualToControlledLandCountEffect`
- [x] `PowerToughnessEqualToControlledPermanentCountEffect`
- [x] `PowerToughnessEqualToControlledCreatureCountEffect`
- [x] `PowerToughnessEqualToCardsInHandEffect`
- [x] `PowerToughnessEqualToControllerLifeTotalEffect`

### Batch G — gained abilities & control conditionals
- [x] `GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect`
- [x] `GainActivatedAbilitiesOfExiledCardsEffect`
- [x] `AnyPlayerControlsPermanentConditionalEffect`
- [x] `ControlsPermanentConditionalEffect`
- [x] `OpponentControlsPermanentConditionalEffect`
- [x] `EquippedConditionalEffect`
- [x] `BlockedByMinCreaturesConditionalEffect`
- [x] `ControllerTurnConditionalEffect`
- [x] `OpponentPoisonedConditionalEffect`
- [x] `ControlsAnotherPermanentConditionalEffect`
- [x] `SelfHasKeywordConditionalEffect`

### Batch H — life / graveyard / top-card thresholds
- [x] `ControllerLifeThresholdConditionalEffect`
- [x] `ControllerLifeAtOrBelowThresholdConditionalEffect` (self + non-self)
- [x] `ControllerGraveyardCardThresholdConditionalEffect`
- [x] `TopCardOfLibraryColorConditionalEffect` (self + non-self)

## Verification commands

Run after each batch (do **not** run the full suite — it takes 20+ min):

```
./gradlew :magical-vibes-backend:compileJava :magical-vibes-backend:compileTestJava
./gradlew :magical-vibes-backend:test --tests "*StaticEffectResolutionServiceTest"
```

Plus the card tests covering the migrated effect(s). Find them by grepping the test tree for the
effect class name or the relevant card names, then run those specific test classes, e.g.:

```
./gradlew :magical-vibes-backend:test --tests "*SomeCardTest"
```

## Final cleanup (last prompt, after the monolith is empty)

Once every effect has migrated and `StaticEffectResolutionService` holds no more
`@HandlesStaticEffect` methods (only delegating wrappers, then none):
- Delete `StaticEffectResolutionService` (or reduce to nothing) and the `@HandlesStaticEffect`
  annotation.
- Remove the reflective `@HandlesStaticEffect` scanning from `EffectRegistryConfig`,
  `GameTestHarness`, and `GameSimulator`.
- Remove the now-unused legacy scan plumbing.
