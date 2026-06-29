# Static Effect Handlers (`staticfx`)

Static/continuous effects (P/T bonuses, keyword grants, conditionals computed during `computeStaticBonus()`) are resolved by one self-contained handler class per effect type in `magical-vibes-engine/.../service/effect/staticfx/` (the tests live in `magical-vibes-application/src/test/.../service/effect/staticfx/`).

## Pattern

1. **Package is `staticfx`, not `static`.** `static` is a reserved word and cannot be a package segment.
2. **Naming convention.** The non-self handler is `<EffectName>Handler` (e.g. `MetalcraftConditionalEffectHandler`). The self handler inserts `Self` before the trailing `Effect`: `<...>SelfEffectHandler` (e.g. `MetalcraftConditionalSelfEffectHandler`). An effect with only a self handler still uses the `SelfEffectHandler` suffix.
3. **One `@Component` per handler**, implementing `StaticEffectHandlerBean`:
   - `Class<? extends CardEffect> handledEffect()` — the effect type it handles.
   - `boolean selfOnly()` — `true` for characteristic-defining / self handlers (registered into the registry's `selfHandlers` map), `false` (default) for broader-scope handlers.
   - `void apply(StaticEffectContext, CardEffect, StaticBonusAccumulator)` — handler body.
   - Constructor-inject `StaticEffectSupport` and/or `GameQueryService` as needed (`@RequiredArgsConstructor`).
4. **Self vs non-self.** An effect with both a self-only bonus and a broader-scope bonus becomes **two** beans (see Metalcraft below).
5. **Spring registration only.** Annotate each handler `@Component` (use an explicit bean name if the class name collides with a `normalfx` handler). `GameEngineConfig` collects all `StaticEffectHandlerBean` components in `afterSingletonsInstantiated()` and registers them into `StaticEffectHandlerRegistry`.

## Infrastructure

- `staticfx/StaticEffectHandlerBean.java` — interface (extends `StaticEffectHandler`).
- `staticfx/StaticEffectSupport.java` — `@Component`, shared helpers (constructor-injects `GameQueryService`).
- `config/GameEngineConfig.java` — component-scans engine packages, exposes registry beans, and registers all `@Component` static handlers after singletons are created.
- `testutil/GameTestDoublesConfig` + `GameTestEngineContext` — card tests load the same `GameEngineConfig` graph via a cached Spring test context.
- `ai/simulation/HeadlessSimulationDoublesConfig` + `HeadlessSimulationContext` — MCTS loads the same engine graph headlessly (no WebSocket broadcasts).

## Worked example: Metalcraft

- `MetalcraftConditionalSelfEffectHandler` — `handledEffect()=MetalcraftConditionalEffect.class`, `selfOnly()=true`.
- `MetalcraftConditionalEffectHandler` — `handledEffect()=MetalcraftConditionalEffect.class`, `selfOnly()=false`.

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
3. Add card tests covering the static behavior.

Verification after adding a handler:

```
./gradlew :magical-vibes-engine:compileJava :magical-vibes-application:compileTestJava
./gradlew :magical-vibes-application:test --tests "com.github.laxika.magicalvibes.service.effect.staticfx.*"
```

Plus card tests for the relevant effect(s).
