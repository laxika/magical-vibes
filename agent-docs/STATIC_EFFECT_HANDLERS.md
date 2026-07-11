# Static Effect Handlers (`staticfx`)

Static/continuous effects (P/T bonuses, keyword grants, conditionals) are resolved by one self-contained handler class per effect type in `magical-vibes-engine/.../service/effect/staticfx/` (the tests live in `magical-vibes-application/src/test/.../service/effect/staticfx/`).

Handlers own the per-effect scope/filter/amount logic; **ordering and precedence across effects is decided by the CR 613 layered pass** (`LayerSystemService` — see `agent-docs/LAYER_SYSTEM.md`, the canonical reference). `GameQueryService.computeStaticBonus()` runs the whole-battlefield pass (layers 4–7d over `CharacteristicState`s, timestamp + dependency ordered), invoking each handler into a `StaticBonusAccumulator` — for pass-managed layers the handler's raw output is harvested/replayed at the effect's own timestamp rather than taken as-is — and assembles the finished `StaticBonus` that views and queries consume. A new handler therefore only says WHAT its effect does to WHICH permanents; it must also be classified in `LayerClassifier` (step 3 below) so the pass knows WHEN to apply it.

## Pattern

1. **Package is `staticfx`, not `static`.** `static` is a reserved word and cannot be a package segment.
2. **Naming convention.** The non-self handler is `<EffectName>Handler` (e.g. `StaticBoostEffectHandler`). The self handler inserts `Self` before the trailing `Effect`: `<...>SelfEffectHandler` (e.g. `StaticBoostSelfEffectHandler`). An effect with only a self handler still uses the `SelfEffectHandler` suffix.
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

## Dynamic-amount boosts (self and attached scopes)

`BoostSelfEffect(DynamicAmount, DynamicAmount)` in the `STATIC` slot is handled by the single
generic `BoostSelfSelfEffectHandler` (selfOnly), which evaluates the amounts via
`AmountEvaluationService` — do NOT add per-derivation `BoostSelfPer*` handlers.

`SetPowerToughnessToAmountEffect(DynamicAmount power, DynamicAmount toughness)` is the
characteristic-defining (`*/*`) counterpart, handled by the single generic
`SetPowerToughnessToAmountSelfEffectHandler` (selfOnly). It evaluates both amounts via
`AmountEvaluationService` on the same `forStaticEffect` context and writes a **base-P/T
override** (CR 613 sublayer 7a): any layer-7b setter (Lignify, Diminish, ...) overrides it in
layer order regardless of timestamps, and a creature that lost all abilities loses its CDA
(Maro under a lose-all is 0/0). It replaced the entire per-derivation
`PowerToughnessEqualTo*SelfEffectHandler` family and the ooze-token
`BoostSelfBySlimeCountersOnLinkedPermanentSelfEffectHandler` — do NOT add new ones.

The attached-scope counterpart is `AttachedBoostEffect(DynamicAmount, DynamicAmount, GrantScope)`,
handled by the single generic `AttachedBoostEffectHandler` (NOT selfOnly). It gates on
`StaticEffectSupport.matchesCreatureScope` (typically `ENCHANTED_CREATURE`/`EQUIPPED_CREATURE`),
then evaluates the amounts with the **source** (the Aura/Equipment) as the amount source and its
controller as the amount controller — so `CountScope.CONTROLLER` resolves to the attachment's
controller ("you"/"you control", CR 109.5), not the enchanted/equipped creature's controller. Do
NOT add per-derivation `BoostCreaturePer*` handlers.

Both evaluate under `AmountContext.forStaticEffect` (static recursion guard). New count sources
become new `model/amount/DynamicAmount` records with a case in `AmountEvaluationService.evaluate`.
Recursion safety in static contexts is two mechanisms working together: while the layered pass
is active, the subtype/color/keyword predicate leaves answer from each permanent's
`CharacteristicState` **as of the layers already applied** (layer N never reads layer ≥N state,
so there is nothing to recurse into — `LayerSystemService.activeStateFor`); the null
`FilterContext` passed in static contexts remains the guard for the leaves the pass does not
manage (power/toughness checks fall back to the permanent's intrinsic pre-switch values instead
of re-entering `computeStaticBonus`). Two permanents counting each other therefore cannot
recurse infinitely, but a static P/T-based filter reads layered-final P/T only OUTSIDE a pass.

## Conditional static effects

Conditional static effects (`ConditionalEffect(condition, wrapped)`) are handled by exactly two
generic handlers — do NOT add per-condition handlers:

- `ConditionalStaticSelfEffectHandler` — `handledEffect()=ConditionalEffect.class`, `selfOnly()=true`.
  Evaluates the condition via `ConditionEvaluationService`, then applies the wrapped effect to the
  source itself via `StaticEffectSupport.applySelfOnlyConditionalStaticEffect` (scope-aware:
  `SELF`/`ALL_OWN_CREATURES` cover the source, `OWN_CREATURES` means "other creatures").
- `ConditionalStaticEffectHandler` — `handledEffect()=ConditionalEffect.class`, `selfOnly()=false`.
  Evaluates the condition, then delegates to the wrapped effect's own registered handler.

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
3. Register the effect type in `LayerClassifier` (`service/effect/LayerClassifier.java`) with
   the CR 613 layer(s) it contributes to — `LayerClassifierTest` walks every registered handler
   and fails on unclassified effect types. See `agent-docs/LAYER_SYSTEM.md` §7 for the
   classification rules.
4. Add card tests covering the static behavior.

Verification after adding a handler:

```
./gradlew :magical-vibes-engine:compileJava :magical-vibes-application:compileTestJava
./gradlew :magical-vibes-application:test --tests "com.github.laxika.magicalvibes.service.effect.staticfx.*"
```

Plus card tests for the relevant effect(s).
