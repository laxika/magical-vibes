# Cast-Cost Modification Handlers (`costmod`)

Cast-cost modifiers (cost reductions and increases — "this spell costs {2} less", "creatures
you cast cost {1} more", metalcraft/graveyard/opponent-count reductions, etc.) are resolved by
one self-contained handler class per effect type in
`magical-vibes-engine/.../service/cast/costmod/` (tests live in
`magical-vibes-application/src/test/.../service/cast/`).

`CastingCostService` is the single source of truth for effective cast cost. Both the UI cost
preview (`GameBroadcastService.getPlayableCardIndices`) and the actual cast-time payment
(`SpellCastingService`) call the same `CastingCostService` methods, which dispatch through the
handler registry — so a cost modifier applies identically in the preview and at resolution.
Never re-add per-effect `instanceof` chains in `GameBroadcastService` or `SpellCastingService`.

## Pattern

1. **Package is `costmod`, under `service/cast/`.**
2. **Naming convention.** `<EffectName>Handler` (e.g. `IncreaseOpponentCastCostEffectHandler`).
3. **One `@Component` per handler**, implementing `CostModificationHandlerBean`:
   - `Class<? extends CardEffect> handledEffect()` — the effect type it handles.
   - `boolean onSpellItself()` — `true` when the effect is carried by the spell being cast
     ("this spell costs {1} less per creature in your graveyard"; registered into the registry's
     `spellSelfHandlers` map). `false` (default) when the effect lives on a battlefield permanent
     and taxes/discounts other spells (registered into `battlefieldHandlers`).
   - `int modifyCost(CostModificationContext, CardEffect, CostModificationSource)` — returns a
     **signed generic-mana delta**: positive means the spell costs more, negative means less,
     zero means this occurrence doesn't apply.
   - Constructor-inject `CostModificationSupport`, `GameQueryService`, and/or
     `PredicateEvaluationService` as needed.
4. **Scoping is the handler's responsibility.** Cast the `CardEffect` to its concrete type, then
   use `CostModificationSource` to decide whether it applies:
   - `source.controlledBy(context.castingPlayerId())` — is the source permanent controlled by
     the caster? (Use to skip opponent-only taxes when the caster controls the source, or to
     require self-control for own-cost reductions.)
   - `CostModificationSource.SPELL_ITSELF` (both fields null) is passed for `onSpellItself()`
     handlers, which read the effect off the spell's own `EffectSlot.STATIC` effects.
5. **Spring registration only.** Annotate each handler `@Component`. `GameEngineConfig` collects
   all `CostModificationHandlerBean` components after singletons are created and registers them
   into `CostModificationHandlerRegistry` (routing by `onSpellItself()`).

## Spell-self cost reductions: use `ReduceOwnCastCostEffect` — never a per-variant record

"This spell costs {N} less to cast …" reductions are **all** modeled with the single effect
`ReduceOwnCastCostEffect(DynamicAmount amount)` (handled by `ReduceOwnCastCostEffectHandler`,
`onSpellItself() == true`). The handler evaluates the amount through `AmountEvaluationService`
with a cast-time `AmountContext.forCasting(castingPlayerId)` and returns it as a negative
generic-mana delta.

- **Flat reduction** ("costs {2} less"): `new ReduceOwnCastCostEffect(new Fixed(2))`.
- **"For each …" reduction**: pass a counting `DynamicAmount` — e.g. Ghoultree
  `new CardsInGraveyard(new CardTypePredicate(CREATURE), CountScope.CONTROLLER)`, Blasphemous Act
  `new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER)`.
- **Conditional reduction** ("costs {N} less to cast **if** …"): wrap it in the generic
  `ConditionalEffect(condition, new ReduceOwnCastCostEffect(new Fixed(N)))`. The
  `ConditionalCostModificationHandler` (mirrors `ConditionalStaticEffectHandler`) evaluates the
  `Condition` via `ConditionEvaluationService` against `ConditionContext.forCasting(...)` and, when
  met, delegates to the wrapped effect's registered spell-self handler. Reuse existing conditions
  (`Metalcraft`, `ControlsPermanent`, `OpponentControlsMoreCreatures`, `CardsLeftGraveyardThisTurn`,
  …); add a new `Condition` (sealed permit + `ConditionEvaluationService.isMet`) only for a genuinely
  new game-state check — never a new cost effect.

**Forbidden:** adding a new `ReduceOwnCastCostIf<Condition>Effect` or
`ReduceOwnCastCostPer<Thing>Effect` record + bespoke handler. Those collapse onto the two shapes
above (amount axis → `DynamicAmount`, condition axis → `ConditionalEffect`). The
`ReduceOwnCastCostFor*` battlefield-source effects (Heartless Summoning, Semblance Anvil) keep their
own handlers because they filter *which other spells* are discounted, but their reduction quantity is
still a `DynamicAmount`.

**Exception — target-gated reductions.** `ReduceOwnCastCostIfTargetingPermanentEffect`,
`ReduceOwnCastCostIfTargetingControlledPermanentEffect`, and `ReduceOwnCastCostIfTargetingStackEntryEffect`
stay as their own records. Their reduction depends on the being-cast spell's **chosen first target**,
which the generic cost-modifier path (and `ConditionContext.forCasting`) does not carry; they are
resolved inline in `CastingCostService.computeTargetBasedCostReduction(gameData, player, card, targetIds)`,
not through the handler registry.

## Infrastructure

- `cast/CostModificationHandlerBean.java` — interface.
- `cast/CostModificationHandlerRegistry.java` — two maps keyed by effect class
  (`battlefieldHandlers`, `spellSelfHandlers`); `register(...)` routes by `onSpellItself()`.
- `cast/costmod/ReduceOwnCastCostEffectHandler.java` — spell-self handler for
  `ReduceOwnCastCostEffect(DynamicAmount)`; evaluates via `AmountEvaluationService`.
- `cast/costmod/IncreaseOwnCastCostUnlessRevealSubtypeEffectHandler.java` — spell-self handler for
  `IncreaseOwnCastCostUnlessRevealSubtypeEffect(int amount, CardSubtype)`; returns `+amount` unless the
  caster holds a card of the subtype (other than the spell itself) to reveal from hand (Lorwyn
  "reveal a creature-type card or pay {N}" cycle, e.g. Goldmeadow Stalwart).
- `cast/costmod/ConditionalCostModificationHandler.java` — spell-self handler for
  `ConditionalEffect`; evaluates the `Condition` via `ConditionEvaluationService` and delegates to
  the wrapped effect's registered spell-self handler (it injects the registry, like
  `ConditionalStaticEffectHandler` injects `StaticEffectHandlerRegistry`).
- `cast/CostModificationContext.java` — `record(GameData gameData, UUID castingPlayerId, Card spell)`.
- `cast/CostModificationSource.java` — `record(Permanent sourcePermanent, UUID controllerId)`
  with `SPELL_ITSELF` constant and `controlledBy(UUID)`.
- `cast/CostModificationSupport.java` — `@Component`, shared queries (`sharesCardType`,
  `anyOpponentControlsAtLeastNMoreCreatures`, `countCreaturesControlled`,
  `countCreaturesOnAllBattlefields`, `countCreatureCardsInGraveyard`, `controlsPermanent`,
  `battlefieldHasPermanentMatching`, `stackHasMatchingSpell`).
- `cast/CastingCostService.java` — `@Component`, the query facade. Builds a
  `CostModifierSnapshot` (a single pass over battlefield permanents' cost modifiers) and computes
  `getCastCostModifier`, targeting taxes/reductions, alternative-cost affordability, and attack
  payment amounts.
- `cast/CastingPermissionService.java` — `@Component`, sibling service for casting *permissions*
  (timing/flash, graveyard/library/exile cast permission, spell limits, restrictions, forbidden
  names). Not cost math, but the other half of what used to live in `GameBroadcastService`.
- `service/GameEngineConfig.java` — exposes the registry bean and registers all
  `@Component` cost handlers after singletons are created.
- Tests build the registry via `CostModificationTestRegistry.build(...)`
  (`magical-vibes-application/src/test/.../service/cast/`), mirroring `GameEngineConfig`.

## Adding a new cost-modifier card

**First check whether it's a spell-self reduction** ("this spell costs {N} less to cast …"). If so,
do NOT add a record or handler — use `ReduceOwnCastCostEffect(DynamicAmount)`, optionally wrapped in
`ConditionalEffect`, per the section above. Only the steps below apply to genuinely new *shapes*
(a new battlefield-source tax/discount, or a new `Condition` for the wrapper).

1. Add the effect record in `magical-vibes-domain/.../model/effect/` if it does not exist (or a new
   `Condition` in `model/condition/`, wired into `ConditionEvaluationService.isMet`).
2. Create the handler class in `costmod/` following the naming convention above; decide
   `onSpellItself()` and do the scoping inside `modifyCost`.
3. Register it in `CostModificationTestRegistry.build(...)` so unit tests exercise real dispatch.
4. Add tests in `CastingCostServiceTest` (cost math) and/or a card test (end-to-end).

Verification after adding a handler:

```
./gradlew :magical-vibes-engine:compileJava :magical-vibes-application:compileTestJava
./gradlew :magical-vibes-application:test --tests "com.github.laxika.magicalvibes.service.cast.*"
```

Plus card tests for the relevant effect(s).
