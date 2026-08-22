# Cast-Cost Modification Handlers (`costmod`)

One-shot reductions use `ReduceCastCostForNextSpellOfTypesThisTurnEffect`. Its normal-effect handler evaluates the dynamic amount when it resolves, stores a pending player-scoped reduction, and `CastingCostService` exposes it only while computing the next matching spell; `GameData.recordSpellCast` consumes it after a successful matching cast.

`ReduceColoredCastCostForMatchingSpellsEffectHandler` handles battlefield reductions that remove
only matching colored components from a spell's mana cost. Unmatched colored reduction does not
reduce generic mana; Ragemonger uses this for `{B}{R}`.

`ReduceOwnColoredCastCostEffectHandler` is the spell-self counterpart for dynamic colored
reductions. It removes matching colored symbols first, then generic mana if the colored component
is exhausted; Khalni Hydra uses it for one `{G}` per green creature controlled.

Cast-cost modifiers (cost reductions and increases — "this spell costs {2} less", "creatures
you cast cost {1} more", metalcraft/graveyard/opponent-count reductions, etc.) and optional
buyback-cost modifiers are resolved by
one self-contained handler class per effect type in
`magical-vibes-engine/.../service/cast/costmod/` (tests live in
`magical-vibes-application/src/test/.../service/cast/`).

`CastingCostService` is the single source of truth for effective cast cost. Both the UI cost
preview (`GameActionAvailabilityService.getPlayableCardIndices`) and the actual cast-time payment
(`SpellCastingService`) call the same `CastingCostService` methods, which dispatch through the
handler registry — so a cost modifier applies identically in the preview and at resolution.
Never re-add per-effect `instanceof` chains in `GameActionAvailabilityService` or `SpellCastingService`.

## Pattern

1. **Package is `costmod`, under `service/cast/`.**
2. **Naming convention.** `<EffectName>Handler` (e.g. `IncreaseSpellCostEffectHandler`).
3. **One `@Component` per handler**, implementing `CostModificationHandlerBean`:
   - `Class<? extends CardEffect> handledEffect()` — the effect type it handles.
   - `boolean onSpellItself()` — `true` when the effect is carried by the spell being cast
     ("this spell costs {1} less per creature in your graveyard"; registered into the registry's
     `spellSelfHandlers` map). `false` (default) when the effect lives on a battlefield permanent
     and taxes/discounts other spells (registered into `battlefieldHandlers`).
   - `int modifyCost(CostModificationContext, CardEffect, CostModificationSource)` — returns a
     **signed generic-mana delta**: positive means the spell costs more, negative means less,
     zero means this occurrence doesn't apply.
   - `appliesAfterOtherCostModifiers()` / `modifyCostAfterOtherModifiers(...)` — use this for a
     floor or other modifier whose result depends on the already-adjusted mana cost. The cast-cost
     service evaluates these handlers after ordinary increases and reductions.
   - `int modifyBuybackCost(CostModificationContext, CardEffect, CostModificationSource)` —
     returns a signed generic-mana delta for an optional buyback cost. It defaults to zero so
     ordinary spell-cost modifiers do not affect buyback.
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

When the oracle wording names a colored mana symbol rather than generic mana, use
`ReduceOwnColoredCastCostEffect(ManaColor, DynamicAmount)`. Its handler evaluates the amount at
cast time and returns a colored reduction that spills into generic mana after matching colored
components are exhausted.

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

  Battlefield permanents use the same `ConditionalEffect` wrapper around
  `ReduceCastCostForMatchingSpellsEffect`; `ConditionalBattlefieldCostModificationHandler` evaluates
  the condition against the source permanent and delegates to the wrapped battlefield handler.

**Forbidden:** adding a new `ReduceOwnCastCostIf<Condition>Effect` or
`ReduceOwnCastCostPer<Thing>Effect` record + bespoke handler. Those collapse onto the two shapes
above (amount axis → `DynamicAmount`, condition axis → `ConditionalEffect`). A battlefield-source
effect that filters *which other spells* are discounted is
`ReduceCastCostForMatchingSpellsEffect(CardPredicate, DynamicAmount, CostModificationScope[, Set<Zone>, boolean])` — not a
new record. Heartless Summoning is that effect with a `CardTypePredicate(CREATURE)` and `SELF` scope;
`ReduceOwnCastCostForSharedCardTypeWithImprintEffect` (Semblance Anvil) keeps its own handler because
it compares against the imprinted card rather than a predicate.

**Exception — target-gated reductions.** `ReduceOwnCastCostIfTargetingPermanentEffect` (whose
`controlledByCaster` flag covers both "targets a matching permanent" and "targets one you control"),
`ReduceOwnCastCostIfTargetingStackEntryEffect`, and `ReduceOwnCastCostPerTargetEffect` stay as
their own records. Their reductions depend on the being-cast spell's chosen targets, which the
generic cost-modifier path (and `ConditionContext.forCasting`) does not carry; they are resolved
inline in `CastingCostService.computeTargetBasedCostReduction(gameData, player, card, targetIds)`,
not through the handler registry. When `ReduceOwnCastCostIfTargetingPermanentEffect` is carried
by a battlefield permanent, it reduces that controller's spells once per effect when any chosen
permanent target matches; the spell-self form continues to inspect its first target.

`ReduceOwnCastCostIfTargetingGraveyardCardEffect` is the corresponding target-gated record for a
graveyard card. Its `CardPredicate` is evaluated against the chosen first graveyard target in the
same `CastingCostService.computeTargetBasedCostReduction` path.

Target-gated increases use the parallel `TargetBasedCastCostIncreaseEffect` interface and
`IncreaseOwnCastCostIfTargetingPermanentEffect` record. Their surcharge is evaluated against the
chosen first permanent target by `CastingCostService.getTargetBasedCostIncrease`, rather than by a
generic cost-modification handler.

## Buyback-cost reductions

Effects that reduce the optional buyback cost, rather than the spell's own mana cost, use a
dedicated battlefield handler and the `CastingCostService.getBuybackCostModifier` channel.
`ReduceBuybackCostEffect(int)` reduces only the generic mana component of mana buyback costs and
applies to buyback costs paid by every player while its source is on the battlefield. It does not
change discard, life, or sacrifice buyback costs, and it does not reduce the spell's normal cast
cost.

Foretell special-action modifiers use the same battlefield handler registry. A handler may
override `modifyForetellCost` for the generic action cost and
`allowsForetellDuringAnyTurn` for a source-controller timing permission; ordinary spell-cost
modifiers do not affect foretell.

## Infrastructure

- `cast/CostModificationHandlerBean.java` — interface.
- `cast/CostModificationHandlerRegistry.java` — two maps keyed by effect class
  (`battlefieldHandlers`, `spellSelfHandlers`); `register(...)` routes by `onSpellItself()`.
- `cast/costmod/ReduceOwnCastCostEffectHandler.java` — spell-self handler for
  `ReduceOwnCastCostEffect(DynamicAmount)`; evaluates via `AmountEvaluationService`.
- `cast/costmod/ReduceOwnColoredCastCostEffectHandler.java` — spell-self handler for
  `ReduceOwnColoredCastCostEffect(ManaColor, DynamicAmount)`; evaluates via
  `AmountEvaluationService` and returns a colored-only reduction.
- `cast/costmod/IncreaseOwnCastCostUnlessRevealSubtypeEffectHandler.java` — spell-self handler for
  `IncreaseOwnCastCostUnlessRevealSubtypeEffect(int amount, CardSubtype)`; returns `+amount` unless the
  caster holds a card of the subtype (other than the spell itself) to reveal from hand (Lorwyn
  "reveal a creature-type card or pay {N}" cycle, e.g. Goldmeadow Stalwart).
- `cast/costmod/IncreaseOwnCastCostEffectHandler.java` — spell-self handler for
  `IncreaseOwnCastCostEffect(int amount)`; returns `+amount` for the spell being cast. Wrap it in
  `ConditionalEffect` for a cast-time condition such as `NotControllerTurn`.
- `cast/costmod/ReduceCastCostForMatchingSpellsEffectHandler.java` — battlefield handler for
  `ReduceCastCostForMatchingSpellsEffect(CardPredicate, DynamicAmount, CostModificationScope[, Set<Zone>, boolean])`; scopes by
  `SELF`/`OPPONENT`/`ALL` (`ALL` = symmetric, every player's matching spells — Arcane Melee), optionally
  restricts by source zone or hand plotting, matches the spell against the predicate, and evaluates the
  amount with the **source permanent** in the `AmountContext` so `CountersOnSource` works ("costs {1} less
  for each +1/+1 counter on this creature" — Herald of War).
- `cast/costmod/ReduceBuybackCostEffectHandler.java` — battlefield handler for
  `ReduceBuybackCostEffect(int)`; contributes only through `modifyBuybackCost`, so the effect is
  isolated from ordinary spell-cost calculations.
- `cast/costmod/ForetellCostReductionEffectHandler.java` — battlefield handler for
  `ForetellCostReductionEffect(int, boolean)`; contributes through the foretell action-cost and
  any-player-turn channels for the source controller.
- `cast/costmod/ReduceCastCostForChosenNameSpellsEffectHandler.java` — battlefield handler for
  `ReduceCastCostForChosenNameSpellsEffect(int amount)`; applies only to the source controller's spells
  whose name equals the source permanent's `chosenName` (Council of the Absolute, {2}). Its own record
  because the matching name lives on the source permanent, which the `CardPredicate` path does not carry.
- `cast/costmod/ReduceCastCostForChosenSubtypeSpellsEffectHandler.java` — battlefield handler for
  `ReduceCastCostForChosenSubtypeSpellsEffect(int amount)`; applies only to the source controller's
  creature spells with the source permanent's chosen creature subtype (Urza's Incubator, {2}). It
  handles the source-relative subtype and changeling check that a fixed `CardPredicate` cannot carry.
- `cast/costmod/IncreaseSpellCostEffectHandler.java` — battlefield handler for
  `IncreaseSpellCostEffect(CardPredicate, int, CostModificationScope)`, the tax-side mirror of
  `ReduceCastCostForMatchingSpellsEffect`; scopes by the same `SELF`/`OPPONENT`/`ALL` and returns
  `+amount` when the spell matches the predicate. `ALL` = symmetric (Thalia, Chill, Gloom, Feroz's
  Ban, Thorn of Amethyst, Irini Sengir); `SELF` = only the source controller's spells (Derelor:
  black spells you cast cost {B} more, modeled as +1 generic); `OPPONENT` = only their opponents'
  (Aura of Silence).
- `cast/costmod/MinimumSpellCostEffectHandler.java` — battlefield handler for
  `MinimumSpellCostEffect(int)`, evaluated after ordinary cost modifiers and active only while the
  source permanent is untapped (Trinisphere uses a minimum of three mana).
- `cast/costmod/IncreaseSpellCostExceptOnControllersTurnEffectHandler.java` — battlefield handler for
  `IncreaseSpellCostExceptOnControllersTurnEffect(int amount)`; returns `+amount` for every spell unless
  it is being cast during its controller's own turn (`castingPlayerId == activePlayerId`). Defense Grid.
- `cast/costmod/ConditionalCostModificationHandler.java` — spell-self handler for
  `ConditionalEffect`; evaluates the `Condition` via `ConditionEvaluationService` and delegates to
  the wrapped effect's registered spell-self handler (it injects the registry, like
  `ConditionalStaticEffectHandler` injects `StaticEffectHandlerRegistry`).
- `cast/costmod/ConditionalBattlefieldCostModificationHandler.java` — battlefield handler for
  `ConditionalEffect`; evaluates the condition against the source permanent and delegates to the
  wrapped battlefield cost handler.
- `cast/CostModificationContext.java` — `record(GameData gameData, UUID castingPlayerId, Card spell)`.
- `cast/CostModificationSource.java` — `record(Permanent sourcePermanent, UUID controllerId)`
  with `SPELL_ITSELF` constant and `controlledBy(UUID)`.
- `cast/CostModificationSupport.java` — `@Component`, shared queries (`sharesCardType`,
  `anyOpponentControlsAtLeastNMoreCreatures`, `countCreaturesControlled`,
  `countCreaturesOnAllBattlefields`, `countCreatureCardsInGraveyard`, `controlsPermanent`,
  `battlefieldHasPermanentMatching`, `stackHasMatchingSpell`).
- `cast/CastingCostService.java` — `@Component`, the query facade. Builds a
  `CostModifierSnapshot` (a single pass over battlefield permanents' cost modifiers) and computes
  `getCastCostModifier`, `getBuybackCostModifier`, targeting taxes/reductions, alternative-cost
  affordability, and attack payment amounts.
- `cast/CastingPermissionService.java` — `@Component`, sibling service for casting *permissions*
  (timing/flash, graveyard/library/exile cast permission, spell limits, restrictions, forbidden
  names). Not cost math, but the other half of what used to live in `GameActionAvailabilityService`.
- `service/GameEngineConfig.java` — exposes the registry bean and registers all
  `@Component` cost handlers after singletons are created.
- Tests build the registry via `CostModificationTestRegistry.build(...)`
  (`magical-vibes-application/src/test/.../service/cast/`), mirroring `GameEngineConfig`.

## Adding a new cost-modifier card

Temporary reductions are represented by `ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect`, whose normal-effect handler adds the existing `ReduceCastCostForMatchingSpellsEffect` as an until-end-of-turn floating effect. `CastingCostService` includes active floating cost modifiers in its snapshot so preview and payment use the same result.

One-shot reductions use `ReduceCastCostForNextMatchingSpellEffect`; its floating effect remains visible to both cost previews and payment, then `TriggerCollectionService` removes it after the controller casts a matching spell (or during end-of-turn cleanup if unused).

**First check whether it's a spell-self reduction** ("this spell costs {N} less to cast …"). If so,
use `ReduceOwnCastCostEffect(DynamicAmount)` for generic mana, or
`ReduceOwnColoredCastCostEffect(ManaColor, DynamicAmount)` when the wording names a colored symbol;
optionally wrapped in
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
