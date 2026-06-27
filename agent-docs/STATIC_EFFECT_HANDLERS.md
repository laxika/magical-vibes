# Static Effect Handlers (`staticfx`)

Static/continuous effects (P/T bonuses, keyword grants, conditionals computed during `computeStaticBonus()`) are resolved by one self-contained handler class per effect type in `magical-vibes-backend/.../service/effect/staticfx/`.

## Pattern

1. **Package is `staticfx`, not `static`.** `static` is a reserved word and cannot be a package segment.
2. **Naming convention.** The non-self handler is `<EffectName>Handler` (e.g. `MetalcraftConditionalEffectHandler`). The self handler inserts `Self` before the trailing `Effect`: `<...>SelfEffectHandler` (e.g. `MetalcraftConditionalSelfEffectHandler`). An effect with only a self handler still uses the `SelfEffectHandler` suffix.
3. **One `@Component` per handler**, implementing `StaticEffectHandlerBean`:
   - `Class<? extends CardEffect> handledEffect()` — the effect type it handles.
   - `boolean selfOnly()` — `true` for characteristic-defining / self handlers (registered into the registry's `selfHandlers` map), `false` (default) for broader-scope handlers.
   - `void apply(StaticEffectContext, CardEffect, StaticBonusAccumulator)` — handler body.
   - Constructor-inject `StaticEffectSupport` and/or `GameQueryService` as needed (`@RequiredArgsConstructor`).
4. **Self vs non-self.** An effect with both a self-only bonus and a broader-scope bonus becomes **two** beans (see Metalcraft below).
5. **Dual registration.** Every handler must be registered in two ways:
   - As a Spring `@Component` (collected via `List<StaticEffectHandlerBean>` in `EffectRegistryConfig` and registered through `StaticEffectHandlerBeanFactory.registerAll`).
   - Added to `StaticEffectHandlerBeanFactory.createAll(...)` so non-Spring sites (`GameTestHarness`, `GameSimulator`) register the same set. `createAll` is the single source of truth for non-Spring instantiation.

## Infrastructure

- `staticfx/StaticEffectHandlerBean.java` — interface (extends `StaticEffectHandler`).
- `staticfx/StaticEffectSupport.java` — `@Component`, shared helpers (constructor-injects `GameQueryService`).
- `staticfx/StaticEffectHandlerBeanFactory.java` — `createAll(support, gameQueryService, registry)` returns every handler; `registerAll(beans, registry)` registers each (self vs non-self based on `bean.selfOnly()`).
- `EffectRegistryConfig` — field-injects `List<StaticEffectHandlerBean>` and calls `registerAll` in `afterSingletonsInstantiated()`.
- `GameTestHarness` & `GameSimulator` — construct `StaticEffectSupport`, build the registry, then call `StaticEffectHandlerBeanFactory.createAll(...)` + `registerAll(...)`.

## Worked example: Metalcraft

- `MetalcraftConditionalSelfEffectHandler` — `handledEffect()=MetalcraftConditionalEffect.class`, `selfOnly()=true`.
- `MetalcraftConditionalEffectHandler` — `handledEffect()=MetalcraftConditionalEffect.class`, `selfOnly()=false`.
- Both listed in `StaticEffectHandlerBeanFactory.createAll(...)`.

## `StaticEffectSupport` public helpers

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

## Adding a new static effect handler

1. Add the effect record in `magical-vibes-domain/.../model/effect/` if it does not exist.
2. Create the handler class(es) in `staticfx/` following the naming convention above.
3. Add each bean to `StaticEffectHandlerBeanFactory.createAll(...)`.
4. Add card tests covering the static behavior.

Verification after adding a handler:

```
./gradlew :magical-vibes-backend:compileJava :magical-vibes-backend:compileTestJava
./gradlew :magical-vibes-backend:test --tests "*StaticEffectResolutionServiceTest"
```

Plus card tests for the relevant effect(s).
