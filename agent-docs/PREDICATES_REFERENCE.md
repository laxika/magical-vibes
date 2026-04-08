# PREDICATES_REFERENCE

Complete reference for all `TargetFilter`, `PermanentPredicate`, `StackEntryPredicate`, and `PlayerPredicate` types. Extracted from ACTIVATED_ABILITY_GUIDE.md for standalone readability.

## TargetFilter types

| Filter class | Constructor | Use when |
|-------------|-------------|----------|
| `PermanentPredicateTargetFilter` | `(PermanentPredicate, String errorMsg)` | Target any permanent matching predicate |
| `ControlledPermanentPredicateTargetFilter` | `(PermanentPredicate, String errorMsg)` | Target only permanents YOU control matching predicate |
| `OwnedPermanentPredicateTargetFilter` | `(PermanentPredicate, String errorMsg)` | Target only permanents YOU OWN matching predicate (ownership via stolenCreatures map) |
| `StackEntryPredicateTargetFilter` | `(StackEntryPredicate, String errorMsg)` | Target a spell on the stack |
| `PlayerPredicateTargetFilter` | `(PlayerPredicate, String errorMsg)` | Target a player matching predicate |

## PermanentPredicate compositions

### Basic type/state predicates

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PermanentIsCreaturePredicate` | `()` | creatures |
| `PermanentIsArtifactPredicate` | `()` | artifacts |
| `PermanentIsLandPredicate` | `()` | lands |
| `PermanentIsEnchantmentPredicate` | `()` | enchantments |
| `PermanentIsPlaneswalkerPredicate` | `()` | planeswalkers |
| `PermanentIsTappedPredicate` | `()` | tapped permanents |
| `PermanentIsAttackingPredicate` | `()` | attacking creatures |
| `PermanentIsBlockingPredicate` | `()` | blocking creatures |
| `PermanentIsTokenPredicate` | `()` | token permanents |
| `PermanentIsHistoricPredicate` | `()` | historic permanents (artifacts, legendaries, Sagas) |
| `PermanentTruePredicate` | `()` | always matches (no restriction) |

### Subtype/supertype/color/keyword predicates

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PermanentColorInPredicate` | `(Set<CardColor>)` | permanents of specified colors |
| `PermanentHasSubtypePredicate` | `(CardSubtype)` | permanents with specific subtype |
| `PermanentHasAnySubtypePredicate` | `(Set<CardSubtype>)` | permanents with any of the subtypes |
| `PermanentHasSupertypePredicate` | `(CardSupertype)` | permanents with specific supertype (e.g. LEGENDARY) |
| `PermanentHasKeywordPredicate` | `(Keyword)` | permanents with specific keyword |
| `PermanentHasCountersPredicate` | `(CounterType)` | permanents with one or more counters of the specified type (supports ANY for any counter) |

### Static power/toughness/mana-value predicates

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PermanentPowerAtLeastPredicate` | `(int minPower)` | creatures with power >= N |
| `PermanentPowerAtMostPredicate` | `(int maxPower)` | creatures with power <= N |
| `PermanentToughnessAtMostPredicate` | `(int maxToughness)` | creatures with toughness <= N |

### Dynamic/game-state predicates (require FilterContext)

These predicates need `FilterContext` with `gameData` and/or `sourceControllerId`/`sourceCardId` to evaluate. The engine automatically provides the correct FilterContext during target validation.

| Predicate | Constructor | Matches | FilterContext needs |
|-----------|-------------|---------|---------------------|
| `PermanentPowerAtMostXPredicate` | `()` | creatures with power <= X (from FilterContext.xValue) | `xValue` |
| `PermanentPowerAtMostControlledCreatureCountPredicate` | `()` | creatures with power <= number of creatures source's controller controls | `gameData` + `sourceControllerId` |
| `PermanentManaValueEqualsXPredicate` | `()` | permanents with mana value == X (returns true when xValue is null) | `xValue` |
| `PermanentToughnessLessThanSourcePowerPredicate` | `()` | creatures with toughness < source permanent's effective power | `gameData` + `sourceCardId` |
| `PermanentInCombatWithSourcePredicate` | `()` | creatures blocking or blocked by the source permanent | `gameData` + `sourceCardId` |
| `PermanentHasGreatestPowerAmongControlledCreaturesPredicate` | `()` | creatures with greatest power among source controller's creatures (ties allowed) | `gameData` + `sourceControllerId` |
| `PermanentDealtDamageThisTurnPredicate` | `()` | permanents dealt damage this turn (evaluated against `GameData.permanentsDealtDamageThisTurn`) | `gameData` |
| `PermanentHasSameNameAsSourcePredicate` | `()` | permanents with same name as source (works with clones) | `gameData` + `sourceCardId` |

### Source-relative predicates

| Predicate | Constructor | Matches | FilterContext needs |
|-----------|-------------|---------|---------------------|
| `PermanentIsSourceCardPredicate` | `()` | the source card itself | `sourceCardId` |
| `PermanentControlledBySourceControllerPredicate` | `()` | permanents controlled by source's controller | `gameData` + `sourceControllerId` |
| `PermanentAttachedToSourceControllerPredicate` | `()` | permanents attached to source's controller | `sourceControllerId` |

### Composition predicates

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PermanentAllOfPredicate` | `(List<PermanentPredicate>)` | AND: all predicates must match |
| `PermanentAnyOfPredicate` | `(List<PermanentPredicate>)` | OR: at least one predicate matches |
| `PermanentNotPredicate` | `(PermanentPredicate)` | NOT: inverts a predicate |

## StackEntryPredicate compositions

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `StackEntryTypeInPredicate` | `(Set<StackEntryType>)` | spells of specific types |
| `StackEntryColorInPredicate` | `(Set<CardColor>)` | spells of specific colors |
| `StackEntryManaValuePredicate` | `(int manaValue)` | spells with exact mana value |
| `StackEntryIsSingleTargetPredicate` | `()` | spells with exactly one target |
| `StackEntryHasTargetPredicate` | `()` | matches any spell or ability on the stack (always true). Signals to include triggered/activated abilities, not just spells. Used by Spellskite |
| `StackEntryControlledByPredicate` | `()` | spells controlled by the evaluating player |
| `StackEntryTargetsYourPermanentPredicate` | `()` | spells targeting a permanent you control |
| `StackEntryTargetsYouOrCreatureYouControlPredicate` | `()` | spells/abilities targeting you or a creature you control |
| `StackEntryAllOfPredicate` | `(List<StackEntryPredicate>)` | AND composition |
| `StackEntryAnyOfPredicate` | `(List<StackEntryPredicate>)` | OR composition |
| `StackEntryNotPredicate` | `(StackEntryPredicate)` | NOT inversion |

## PlayerPredicate compositions

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PlayerRelationPredicate` | `(PlayerRelation)` | player by relation. `PlayerRelation`: `OPPONENT`, `SELF` |
