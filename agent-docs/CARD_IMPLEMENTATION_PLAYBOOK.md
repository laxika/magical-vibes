# CARD_IMPLEMENTATION_PLAYBOOK

Purpose: a minimal workflow for adding cards with fewer repeated lookups and lower token usage.

## Fast workflow

1. Find a similar card in `CARD_PATTERN_INDEX.md` for reference.
2. Reuse existing effects from `EFFECTS_INDEX.md`.
3. Check `ACTIVATED_ABILITY_GUIDE.md` for constructor patterns and EffectSlot reference.
4. Add card class + `@CardRegistration`.
5. Add/adjust target flags and target filter.
6. Write focused tests extending `BaseCardTest` (provides `harness`, `player1`, `player2`, `gs`, `gqs`, `gd`). Do NOT test Scryfall metadata.
7. Only if needed: add new effect record + annotated resolver method (see below).

## Card class template

```java
@CardRegistration(set = "10E", collectorNumber = "000")
public class ExampleCard extends Card {
    public ExampleCard() {
        // Optional: setTargetFilter(...)
        // addEffect(...) and/or addActivatedAbility(...)
    }
}
```

## Canonical patterns

- Targeted burn spell:
  - `addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(N))`
  - Targeting is computed from effects — no `setNeedsTarget` call needed.
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/Shock.java`

- Multi-step spell resolution with shared target:
  - chain effects in order with repeated `addEffect(EffectSlot.SPELL, ...)`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/c/Condemn.java`

- Spell that targets stack entries:
  - `addEffect(EffectSlot.SPELL, new CounterSpellEffect())` (or `CopySpellEffect`, etc.)
  - `setTargetFilter(new StackEntryPredicateTargetFilter(...))` if restricted
  - Spell targeting is computed from effects — no `setNeedsSpellTarget` call needed.
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/t/Twincast.java`

- Static combat restriction on self:
  - `addEffect(EffectSlot.STATIC, new CantBlockEffect())`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SpinelessThug.java`

- Static "controller can't cast spells of specified types":
  - `addEffect(EffectSlot.STATIC, new CantCastSpellTypeEffect(Set.of(CardType.CREATURE)))`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SteelGolem.java`

- Aura with static effect:
  - Auras automatically derive targeting from `isAura()` — no `setNeedsTarget` call needed.
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/p/Pacifism.java`

- Aura with static + activated ability:
  - static enchanted effect + `addActivatedAbility(...)`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/ShimmeringWings.java`

- ETB trigger + activated ability with additional cost:
  - add ETB effect + ability containing cost-like effect first, then outcome effect
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SiegeGangCommander.java`

- ETB target-opponent control handoff:
  - `setTargetFilter(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT), ...))`
  - `addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TargetPlayerGainsControlOfSourceCreatureEffect())`
  - Targeting is computed from the ETB effect — no `setNeedsTarget` call needed.
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/s/SleeperAgent.java`

- Composition before custom effect:
  - combine multiple existing effects in one ability/spell
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/o/OrcishArtillery.java`

- Upkeep sacrifice-unless-discard (any card):
  - `addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeUnlessDiscardCardTypeEffect(null))`
  - Pass `CardType.X` instead of `null` to restrict to a specific card type
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/r/RazormaneMasticore.java`

- Controller draw step may-trigger with targeting:
  - `addEffect(EffectSlot.DRAW_TRIGGERED, new MayEffect(new DealDamageToTargetCreatureEffect(N), "prompt"))`
  - `DRAW_TRIGGERED` fires only on the controller's draw step; use `EACH_DRAW_TRIGGERED` for all draw steps
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/r/RazormaneMasticore.java`

- Opponent draw trigger:
  - use `addEffect(EffectSlot.ON_OPPONENT_DRAWS, new DealDamageToTargetPlayerEffect(N))` when the effect should hit the player who drew
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/u/UnderworldDreams.java`

- Conditional self cast-cost reduction:
  - add static effect on the card itself (in hand-relevant card logic): `addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect(M, N))`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/a/AvatarOfMight.java`

- Attacker blocked-only-by-flying-or-subtype:
  - prefer composed permanent predicates on attacker: `addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(new PermanentAnyOfPredicate(List.of(new PermanentHasKeywordPredicate(Keyword.FLYING), new PermanentHasSubtypePredicate(CardSubtype.X))), "creatures with flying or Xs"))`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/e/ElvenRiders.java`

- Equipment with static keyword + evasion:
  - `addEffect(EffectSlot.STATIC, new CantBeBlockedEffect())` + `addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.X, GrantScope.EQUIPPED_CREATURE))` + equip ability
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/w/WhispersilkCloak.java`

- Creature land (manland) — enters tapped, taps for mana, animates:
  - `setEntersTapped(true)` + `addEffect(EffectSlot.ON_TAP, new AwardManaEffect(...))` + `addActivatedAbility(new ActivatedAbility(false, cost, List.of(new AnimateLandEffect(power, toughness, subtypes, keywords, color)), description))`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/f/FaerieConclave.java`

- Kindred Enchantment with ETB token creation + activated token ability:
  - `addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateCreatureTokenWithColorsEffect(N, ...))` + `addActivatedAbility(new ActivatedAbility(false, cost, List.of(new CreateCreatureTokenWithColorsEffect(...)), description))`
  - Example: `magical-vibes-card/src/main/java/com/github/laxika/magicalvibes/cards/c/ClachanFestival.java`

- Predicate-based targeting:
  - prefer `setTargetFilter(new PermanentPredicateTargetFilter(...))` over ad-hoc `TargetFilter` permutations
  - compose with `PermanentAllOfPredicate`, `PermanentAnyOfPredicate`, and atoms like `PermanentIsCreaturePredicate`, `PermanentIsTappedPredicate`, `PermanentColorInPredicate`, `PermanentHasSubtypePredicate`

## Targeting checklist

- Targeting is computed automatically from effects — both for spells (`Card`) and activated abilities (`ActivatedAbility`).
- Override `canTargetPlayer()`, `canTargetPermanent()`, `canTargetSpell()`, or `canTargetGraveyard()` on your effect record to return `true`.
- `Card.isNeedsTarget()`, `Card.isNeedsSpellTarget()`, `ActivatedAbility.isNeedsTarget()`, and `ActivatedAbility.isNeedsSpellTarget()` are all derived getters — never stored as fields.
- `Card.getAllowedTargets()` returns a `Set<TargetType>` computed from SPELL and ON_ENTER_BATTLEFIELD effects, plus `isAura()`.
- For non-battlefield targets on stack entries, use `Zone` (`Zone.GRAVEYARD`, `Zone.STACK`), not `TargetZone`.
- Add `setTargetFilter(...)` (on Card) or pass a `TargetFilter` to the `ActivatedAbility` constructor when target legality is restricted.

## When a new effect is actually required

Create a new `CardEffect` record only if both are true:
- behavior cannot be represented by existing effect composition
- behavior cannot be represented by existing target filter + existing effects

Then do all of:
- Add effect record in `magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect/`
  - Override `canTargetPlayer()`, `canTargetPermanent()`, `canTargetSpell()`, or `canTargetGraveyard()` to return `true` as appropriate. This drives automatic `isNeedsTarget()`/`isNeedsSpellTarget()` computation on `Card` and the `targetsPlayer` flag in `CardViewFactory`.
- Add an annotated resolver method in the correct resolution service (see `EFFECTS_INDEX.md` provider map):
  ```java
  @HandlesEffect(YourNewEffect.class)
  void resolveYourNewEffect(GameData gameData, StackEntry entry) { ... }
  // or with typed effect access:
  @HandlesEffect(YourNewEffect.class)
  void resolveYourNewEffect(GameData gameData, StackEntry entry, YourNewEffect effect) { ... }
  ```
  The `@HandlesEffect` annotation auto-registers the handler at startup — no manual `registry.register()` call needed. For static/continuous effects, use `@HandlesStaticEffect(YourEffect.class)` (or `@HandlesStaticEffect(value = YourEffect.class, selfOnly = true)` for self-only bonuses) in `StaticEffectResolutionService`.
- Add test coverage for normal path + invalid/fizzle path if applicable

## Quick anti-patterns

- Adding new effect records for simple two-step effects that already compose.
- Skipping target filters when oracle text requires constraints.
- Adding frontend/view changes for pure engine-only behavior that already serializes correctly.

