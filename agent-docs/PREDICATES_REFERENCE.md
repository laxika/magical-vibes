# PREDICATES_REFERENCE

Complete reference for all `TargetFilter`, `PermanentPredicate`, `StackEntryPredicate`, and `PlayerPredicate` types. Extracted from ACTIVATED_ABILITY_GUIDE.md for standalone readability.

All of these base interfaces are **sealed**: a new predicate/filter must be added to the interface's `permits` clause, and the exhaustive switch in the engine's `PredicateEvaluationService` (`magical-vibes-engine/.../service/filter/`) must gain a matching case — the compiler enforces both. `StackEntryPredicate` types used for *targeting* are evaluated by `TargetLegalityService` instead.

## TargetFilter types

| Filter class | Constructor | Use when |
|-------------|-------------|----------|
| `PermanentPredicateTargetFilter` | `(PermanentPredicate, String errorMsg)` | Target any permanent matching predicate |
| `AnyTargetPredicateTargetFilter` | `(PermanentPredicate, PlayerPredicate, String errorMsg)` | Restrict an "any target" (creature/planeswalker/player) effect: the `PermanentPredicate` gates permanent targets, the `PlayerPredicate` gates player targets — both expressing the same restriction. Use for "any target that was dealt damage this turn" (Needle Drop): `PermanentDealtDamageThisTurnPredicate` + `PlayerDealtDamageThisTurnPredicate` |
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
| `PermanentIsEnchantedPredicate` | `()` | permanents that have at least one Aura attached (i.e. are enchanted), regardless of who controls the Aura — needs game data. Used by Greater Auramancy ("Enchanted creatures you control have shroud") |
| `PermanentIsPlaneswalkerPredicate` | `()` | planeswalkers |
| `PermanentIsTappedPredicate` | `()` | tapped permanents |
| `PermanentIsAttackingPredicate` | `()` | attacking creatures |
| `PermanentIsAttackingSourceControllerPredicate` | `()` | creatures attacking you (the source controller) — attack target must be the source controller, not a planeswalker/other player; needs a `FilterContext` with source controller (Blessed Reversal) |
| `PermanentIsBlockingPredicate` | `()` | blocking creatures (the blockers themselves) |
| `PermanentIsBlockedPredicate` | `()` | blocked creatures — attacking creatures that at least one creature is blocking. Distinct from `PermanentIsBlockingPredicate`. Pair with `BoostAllCreaturesEffect(.., filter)` / `GrantKeywordEffect(kw, ALL_CREATURES, filter)` for "each blocked creature ..." (Tattermunge Witch) |
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
| `PermanentMaxManaValuePredicate` | `(int maxManaValue)` | permanents with mana value <= N (e.g. Witherbloom Charm) |
| `PermanentMinManaValuePredicate` | `(int minManaValue)` | permanents with mana value >= N (e.g. Austere Command) |
| `PermanentToughnessAtMostPredicate` | `(int maxToughness)` | creatures with toughness <= N |
| `PermanentToughnessAtLeastPredicate` | `(int minToughness)` | creatures with toughness >= N (uses effective/last-known toughness; e.g. Colfenor's Urn) |

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
| `PermanentHasGreatestManaValueAmongAllCreaturesPredicate` | `()` | creatures with greatest mana value among all creatures on the battlefield across every player (ties allowed) | `gameData` |
| `PermanentDealtDamageThisTurnPredicate` | `()` | permanents dealt damage this turn (evaluated against `GameData.permanentsDealtDamageThisTurn`) | `gameData` |
| `PermanentHasSameNameAsSourcePredicate` | `()` | permanents with same name as source (works with clones) | `gameData` + `sourceCardId` |
| `PermanentNamedPredicate` | `(String cardName)` | permanents with the given name (exact `Card.getName()` equality); e.g. "a permanent named Guan Yu, Sainted Warrior" | none |

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
| `StackEntrySubtypeInPredicate` | `(Set<CardSubtype>)` | spells whose card has any of the given subtypes. Wrap in `StackEntryNotPredicate` for "non-[subtype] spell" (e.g. Faerie Trickery: counter target non-Faerie spell) |
| `StackEntryManaValuePredicate` | `(int manaValue)` | spells with exact mana value |
| `StackEntryManaValueEqualsXPredicate` | `()` | spells whose mana value equals the casting spell's chosen X. "counter target spell with mana value X" — Spell Blast. The chosen X is threaded from `SpellCastingService` into `TargetLegalityService.matchesStackEntryPredicate(..., xValue)` at cast-time targeting; when X is unknown (target enumeration) it matches permissively |
| `StackEntryManaValueAtMostControlledCountPredicate` | `(PermanentPredicate countFilter)` | spells whose mana value ≤ the number of permanents the evaluating player controls matching `countFilter`. "counter target spell with mana value X or less, where X is the number of [type] you control" — Spellstutter Sprite with `PermanentHasAnySubtypePredicate(FAERIE)` (counts itself, since it's already on the battlefield when the ETB resolves) |
| `StackEntryIsSingleTargetPredicate` | `()` | spells with exactly one target |
| `StackEntryHasTargetPredicate` | `()` | matches any spell or ability on the stack (always true). Signals to include triggered/activated abilities, not just spells. Used by Spellskite |
| `StackEntryControlledByPredicate` | `()` | spells controlled by the evaluating player (the source's own controller) |
| `StackEntryCastFromZonePredicate` | `(Zone)` | spells cast from the given zone (via the entry's `sourceZone`); e.g. `Zone.GRAVEYARD` for "casts a spell from a graveyard" (River Kelpie), distinguishing graveyard casts from exile casts |
| `StackEntryControlledByEnchantedPlayerPredicate` | `()` | spells controlled by the player the source aura is attached to (the enchanted player). The enchanted player's ID is supplied externally by the evaluating service (`PredicateEvaluationService.matchesStackEntryPredicate(entry, predicate, enchantedPlayerId)`). Used by Curse of Echoes |
| `StackEntrySharesChosenNameWithSourcePredicate` | `()` | spells whose card name equals the chosen name recorded on the source permanent (via a "choose a card name" ETB — `ChooseCardNameOnEnterEffect`). "counter target spell with the chosen name" — Declaration of Naught. Source-dependent: matches nothing unless the source permanent is passed to `TargetLegalityService.matchesStackEntryPredicate(..., source)`; the ability-activation path supplies it automatically |
| `StackEntryTargetsYourPermanentPredicate` | `()` | spells targeting a permanent you control |
| `StackEntryTargetsYouOrCreatureYouControlPredicate` | `()` | spells/abilities targeting you or a creature you control |
| `StackEntryTargetsYouPredicate` | `()` | spells/abilities targeting you (the player only, not your permanents). "... spell that targets you" — Mirror Sheen |
| `StackEntryTargetsPermanentPredicate` | `(PermanentPredicate filter)` | spells/abilities targeting at least one permanent matching `filter` (any controller; filter evaluated with the evaluating source's controller as `sourceControllerId`). Used as `SpellCastTriggerEffect.castSpellTargetCondition` — e.g. Repartee ("cast an instant or sorcery spell that targets a creature") with `new PermanentIsCreaturePredicate()` |
| `StackEntryAllOfPredicate` | `(List<StackEntryPredicate>)` | AND composition |
| `StackEntryAnyOfPredicate` | `(List<StackEntryPredicate>)` | OR composition |
| `StackEntryNotPredicate` | `(StackEntryPredicate)` | NOT inversion |

## PlayerPredicate compositions

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PlayerRelationPredicate` | `(PlayerRelation)` | player by relation. `PlayerRelation`: `OPPONENT`, `SELF` |
| `PlayerDealtDamageThisTurnPredicate` | `()` | players dealt damage this turn (evaluated against `GameData.playersDealtDamageThisTurn`). Player-side counterpart of `PermanentDealtDamageThisTurnPredicate`; pair them in an `AnyTargetPredicateTargetFilter` for "any target that was dealt damage this turn" |

## CardPredicate (spell/card filters)

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `CardIsTokenPredicate` | `()` | token cards. Wrap in `CardNotPredicate` for "nontoken" (e.g. Militia's Pride: nontoken attacker filter on `ON_ALLY_CREATURE_ATTACKS` via `TriggeringCardConditionalEffect`) |
| `CardControllerDoesNotOwnPredicate` | `()` | a card whose owner is not the perspective player (the `cardOwnerId` argument of `matchesCardPredicate`, which is the casting player in the spell-cast trigger path). Cards with no tracked owner (tokens/copies) never match. Use as a `SpellCastTriggerEffect` filter for "a spell you don't own" (Nita, Forum Conciliator). Ownership is stamped at game setup on `Card.ownerId` and preserved across zones |
| `CardPowerAtMostPredicate` | `(int maxPower)` | a card whose printed power is <= `maxPower`; cards without power (non-creatures) never match. Compose with `CardTypePredicate(CREATURE)` via `CardAllOfPredicate` for library searches like "a creature card with power 2 or less" (Imperial Recruiter) |
| `CardPowerAtLeastPredicate` | `(int minPower)` | a card whose printed power is >= `minPower`; cards without power (non-creatures) never match. Compose with `CardTypePredicate(CREATURE)` via `CardAllOfPredicate` for "a creature card with power 5 or greater" (Sacellum Godspeaker) |
| `CardMaxManaValuePredicate` | `(int maxManaValue)` | a card with mana value ≤ N (e.g. Teshar's "mana value 3 or less" graveyard filter) |
