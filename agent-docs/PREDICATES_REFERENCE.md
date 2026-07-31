# PREDICATES_REFERENCE

Complete reference for all `TargetFilter`, `PermanentPredicate`, `StackEntryPredicate`, and `PlayerPredicate` types. Extracted from ACTIVATED_ABILITY_GUIDE.md for standalone readability.

All of these base interfaces are **sealed**: a new predicate/filter must be added to the interface's `permits` clause, and the exhaustive switch in the engine's `PredicateEvaluationService` (`magical-vibes-engine/.../service/filter/`) must gain a matching case — the compiler enforces both. `StackEntryPredicate` types used for *targeting* are evaluated by `TargetLegalityService` instead.

## TargetFilters — prefer these over building a filter by hand

`model/filter/TargetFilters` has factories for the restrictions cards ask for most often.
Reach for one before writing out a filter:

| Factory | Produces | Validation message |
|---------|----------|--------------------|
| `TargetFilters.creature()` | `PermanentPredicateTargetFilter` | "Target must be a creature" |
| `TargetFilters.creatureYouControl()` | `ControlledPermanentPredicateTargetFilter` | "Target must be a creature you control" |
| `TargetFilters.creatureAnOpponentControls()` | `PermanentPredicateTargetFilter` | "Target must be a creature an opponent controls" |
| `TargetFilters.attackingCreature()` | `PermanentPredicateTargetFilter` | "Target must be an attacking creature" |
| `TargetFilters.land()` / `landYouControl()` | permanent / controlled | "Target must be a land\[ you control\]" |
| `TargetFilters.artifact()` | `PermanentPredicateTargetFilter` | "Target must be an artifact" |
| `TargetFilters.enchantment()` | `PermanentPredicateTargetFilter` | "Target must be an enchantment" |
| `TargetFilters.permanent()` / `permanentYouControl()` | permanent / controlled | "Target must be a permanent\[ you control\]" |

The message is shown to a player who picks an illegal target, so it is part of the card's
behaviour. If the card needs different wording — "First target must be a creature", "Second
target must be a creature you don't control" — or a restriction with no factory, build the
filter directly rather than reusing a factory whose wording does not match.

## TargetFilter types

| Filter class | Constructor | Use when |
|-------------|-------------|----------|
| `PermanentPredicateTargetFilter` | `(PermanentPredicate, String errorMsg)` | Target any permanent matching predicate |
| `AnyTargetPredicateTargetFilter` | `(PermanentPredicate, PlayerPredicate, String errorMsg)` | Restrict an "any target" (creature/planeswalker/player) effect: the `PermanentPredicate` gates permanent targets, the `PlayerPredicate` gates player targets — both expressing the same restriction. Use for "any target that was dealt damage this turn" (Needle Drop): `PermanentDealtDamageThisTurnPredicate` + `PlayerDealtDamageThisTurnPredicate` |
| `ControlledPermanentPredicateTargetFilter` | `(PermanentPredicate, String errorMsg)` | Target only permanents YOU control matching predicate |
| `OwnedPermanentPredicateTargetFilter` | `(PermanentPredicate, String errorMsg)` | Target only permanents YOU OWN matching predicate (ownership via stolenCreatures map) |
| `StackEntryPredicateTargetFilter` | `(StackEntryPredicate, String errorMsg)` | Target a spell on the stack |
| `PlayerPredicateTargetFilter` | `(PlayerPredicate, String errorMsg)` | Target a player matching predicate |
| `GraveyardCardPredicateTargetFilter` | `(CardPredicate, GraveyardSearchScope)` | Target a card in a graveyard, with the scope declared **per target group** — this is what lets one spell take two graveyard targets with different scopes (Spelltwine: "target instant or sorcery card from your graveyard **and** target instant or sorcery card from an opponent's graveyard"). `null` predicate = any card. Enumeration (`ValidTargetService.computeValidGraveyardTargetsForFilter`), cast-time validation, and the CR 608.2b resolution recheck all key off this filter, so the effect itself just needs a graveyard `targetSpec()` and can stay unbound to read every chosen card |

## PermanentPredicate compositions

### Basic type/state predicates

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PermanentIsCreaturePredicate` | `()` | creatures |
| `PermanentIsArtifactPredicate` | `()` | artifacts |
| `PermanentIsLandPredicate` | `()` | lands |
| `PermanentIsEnchantmentPredicate` | `()` | enchantments |
| `PermanentIsEnchantedPredicate` | `()` | permanents that have at least one Aura attached (i.e. are enchanted), regardless of who controls the Aura — needs game data. Used by Greater Auramancy ("Enchanted creatures you control have shroud") |
| `PermanentIsHostOfSourceAuraPredicate` | `()` | the permanent the **source Aura** is currently attached to (the enchanted permanent) — needs game data + `sourceCardId`. Wrap in `PermanentNotPredicate` for "target creature other than enchanted creature" (Kjeldoran Pride's `{2}{U}` reattach ability) |
| `PermanentIsAuraAttachedToCreaturePredicate` | `()` | an Aura permanent currently attached to a creature (checks `card.isAura()`, `isAttached()`, and that the host permanent is a creature — needs game data). Used to filter the Aura target of Crown of the Ages ("target Aura attached to a creature") |
| `PermanentIsAuraAttachedToSourcePredicate` | `()` | an Aura permanent currently attached to the **source** permanent, whoever controls the Aura — needs game data + `sourceCardId`. Pair with `DestroyAllPermanentsEffect` for "Destroy all Auras attached to CARDNAME" (Hakim, Loreweaver) |
| `PermanentIsPlaneswalkerPredicate` | `()` | planeswalkers |
| `PermanentIsTappedPredicate` | `()` | tapped permanents |
| `PermanentIsAttackingPredicate` | `()` | attacking creatures |
| `PermanentIsAttackingSourceControllerPredicate` | `()` | creatures attacking you (the source controller) — attack target must be the source controller, not a planeswalker/other player; needs a `FilterContext` with source controller (Blessed Reversal) |
| `PermanentIsBlockingPredicate` | `()` | blocking creatures (the blockers themselves). Also usable as a static GrantKeywordEffect/StaticBoostEffect filter (`matchesStaticFilter` supports it, like `PermanentIsAttackingPredicate`) — Snow Devil |
| `PermanentIsBlockedPredicate` | `()` | blocked creatures — attacking creatures that at least one creature is blocking. Distinct from `PermanentIsBlockingPredicate`. Pair with `BoostAllCreaturesEffect(.., filter)` / `GrantKeywordEffect(kw, ALL_CREATURES, filter)` for "each blocked creature ..." (Tattermunge Witch) |
| `PermanentIsUnblockedAttackingPredicate` | `()` | unblocked attacking creatures — attacking and no creature blocking it (and not made blocked by an effect). The complement of `PermanentIsBlockedPredicate` among attackers; `TargetFilters.unblockedAttackingCreature()`. Dazzling Beauty |
| `PermanentAttackedOrBlockedThisTurnPredicate` | `()` | creatures that were declared as an attacker or blocker at some point this turn (reads `Permanent.attackedThisTurn`/`blockedThisTurn`, which persist after combat ends and clear at the next turn start — so the creature still matches in a later main phase or end step). Unlike `PermanentIsAttacking`/`IsBlockingPredicate` (current combat state only). AND with `PermanentIsCreaturePredicate` for "target creature that attacked or blocked this turn" (Vizier of Deferment) |
| `PermanentAttackedDuringControllersLastTurnPredicate` | `()` | creatures that attacked during their controller's *previous* turn (Halls of Mist). Reads `Permanent.attackedDuringControllersLastTurn`, which is rolled over from `attackedDuringControllersCurrentTurn` by `TurnProgressionService` only when that permanent's controller's turn begins — so it survives the intervening opponent turns, unlike `PermanentAttackedOrBlockedThisTurnPredicate` |
| `PermanentBlockedOrWasBlockedBySubtypeThisTurnPredicate` | `(CardSubtype)` | creatures that blocked or were blocked by a creature of the subtype at any point this turn (turn-scoped, recorded at declare-blockers time in `GameData.combatBlockOpponentSubtypesThisTurn`; Changeling opponents count as every subtype). Subtype-ness is judged at block time, so the target stays legal after combat ends or the other creature leaves/changes types. Needs `gameData`. AND with `PermanentIsCreaturePredicate` for "target creature that ..." (Time to Reflect) |
| `PermanentIsTokenPredicate` | `()` | token permanents |
| `PermanentIsHistoricPredicate` | `()` | historic permanents (artifacts, legendaries, Sagas) |
| `PermanentTruePredicate` | `()` | always matches (no restriction) |

### Subtype/supertype/color/keyword predicates

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PermanentColorInPredicate` | `(Set<CardColor>)` | permanents of specified colors. **A land never matches**: CR 202.2 gives an object the colors of its mana cost, and a land has none, so Plains is colorless and Anarchy ("destroy all white permanents") leaves it alone. A land's color identity is carried separately as `Card.getColorIdentity()`, which is display-only (it tints the frame) and must never be read by a predicate. This is also why Mistveil Plains and friends do not count themselves toward "two or more white permanents" |
| `PermanentIsMonocoloredPredicate` | `()` | permanents with exactly one effective color (colorless and multicolored don't match); Defiler of Souls |
| `PermanentIsMulticoloredPredicate` | `()` | permanents with two or more effective colors (colorless and monocolored don't match); complement of `PermanentIsMonocoloredPredicate`, battlefield counterpart of `CardIsMulticoloredPredicate`; Esper Stormblade ("another multicolored permanent" via `ControlsAnotherPermanent`) |
| `PermanentHasSubtypePredicate` | `(CardSubtype)` | permanents with specific subtype |
| `PermanentHasAnySubtypePredicate` | `(Set<CardSubtype>)` | permanents with any of the subtypes |
| `PermanentHasSupertypePredicate` | `(CardSupertype)` | permanents with specific supertype (e.g. LEGENDARY). Evaluated through `GameQueryService.hasEffectiveSupertype`, so a global `PermanentsMatchingLoseSupertypeEffect` (Melting) correctly makes it false |
| `PermanentHasKeywordPredicate` | `(Keyword)` | permanents with specific keyword |
| `PermanentHasCountersPredicate` | `(CounterType)` | permanents with one or more counters of the specified type (supports ANY for any counter) |
| `PermanentHasCumulativeUpkeepPredicate` | `()` | permanents that have cumulative upkeep (printed or granted) |

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
| `PermanentPowerAtMostSourcePowerPredicate` | `()` | creatures with power <= source permanent's effective power (Earthshaker Khenra's ETB "target creature with power less than or equal to this creature's power" — source-relative so a 4/4 Eternalize token can target up to power 4) | `gameData` + `sourceCardId` |
| `PermanentPowerLessThanSourcePowerPredicate` | `()` | creatures with power strictly < source permanent's effective power (Champion of Lambholt: "Creatures with power less than this creature's power can't block creatures you control"). Source must be on the battlefield | `gameData` + `sourceCardId` |
| `PermanentInCombatWithSourcePredicate` | `()` | creatures blocking or blocked by the source permanent | `gameData` + `sourceCardId` |
| `PermanentBlockedBySourcePredicate` | `()` | creatures the source permanent is blocking — "target creature it's blocking" (Goblin Snowman, Tinder Wall). Narrower than `PermanentInCombatWithSourcePredicate`, which also matches creatures blocking the source. Per CR 608.2b, when the source has left the battlefield (e.g. sacrificed to pay the ability's cost) the resolution-time re-check falls back to `FilterContext.sourcePermanentSnapshot()` (the stack entry's last-known source), so the ability still resolves | `gameData` + `sourceCardId`, optional `sourcePermanentSnapshot` |
| `PermanentBlockingSourcePredicate` | `()` | creatures blocking the source permanent — "target creature blocking this creature" (Barbed-Back Wurm). Mirror image of `PermanentBlockedBySourcePredicate`; narrower than `PermanentInCombatWithSourcePredicate`. Per CR 608.2b, when the source has left the battlefield (e.g. sacrificed to pay the ability's cost — Urborg Panther) the resolution-time re-check falls back to `FilterContext.sourcePermanentSnapshot()`, so the ability still resolves | `gameData` + `sourceCardId`, optional `sourcePermanentSnapshot` |
| `PermanentHasGreatestPowerAmongControlledCreaturesPredicate` | `()` | creatures with greatest power among source controller's creatures (ties allowed) | `gameData` + `sourceControllerId` |
| `PermanentHasGreatestManaValueAmongAllCreaturesPredicate` | `()` | creatures with greatest mana value among all creatures on the battlefield across every player (ties allowed) | `gameData` |
| `PermanentHasLeastPowerAmongAllCreaturesPredicate` | `()` | creatures with the least effective power among all creatures on the battlefield across every player (ties allowed). Wretched Banquet | `gameData` |
| `PermanentDealtDamageThisTurnPredicate` | `()` | permanents dealt damage this turn (evaluated against `GameData.permanentsDealtDamageThisTurn`) | `gameData` |
| `PermanentDealtDamageToSourceControllerThisTurnPredicate` | `()` | permanents that dealt damage — combat or noncombat — to the source's controller this turn ("target creature that dealt damage to you this turn", Giltspire Avenger). Checks `GameData.combatDamageToPlayersThisTurn` + `GameData.noncombatDamageToPlayersThisTurn` for `sourceControllerId` | `gameData` + `sourceControllerId` |
| `PermanentAttackedSourceControllerThisTurnPredicate` | `()` | creatures declared as attackers against the source's controller this turn ("target creature that attacked you this turn", Jabari's Influence). Checks `GameData.playersAttackedThisTurn` (written in `CombatAttackService.declareAttackers`, cleared at turn cleanup) for `sourceControllerId`; attacking a planeswalker that player controls does not match | `gameData` + `sourceControllerId` |
| `PermanentHasSameNameAsSourcePredicate` | `()` | permanents with same name as source (works with clones) | `gameData` + `sourceCardId` |
| `PermanentHasSourceChosenSubtypePredicate` | `()` | permanents carrying the subtype chosen as the **source** entered the battlefield (`Permanent.chosenSubtype`, set by `ChooseBasicLandTypeOnEnterEffect` and friends); matches nothing when the source is gone or made no choice. Supported in `PredicateEvaluationService.matchesStaticFilter`, so it works as a static scope filter — Shimmer (`GrantKeywordEffect(PHASING, ALL_LANDS, …)` = "each land of the chosen type has phasing") | `gameData` + `sourceCardId` |
| `PermanentNamedPredicate` | `(String cardName)` | permanents with the given name (exact `Card.getName()` equality); e.g. "a permanent named Guan Yu, Sainted Warrior" | none |
| `PermanentNameInPredicate` | `(Set<String> cardNames)` | permanents whose name is one of a fixed roster of names (exact `Card.getName()` equality). For "a name originally printed in the Homelands expansion" (Apocalypse Chime) — the card class owns the name list, so a later reprint of a listed name still matches | none |

### Source-relative predicates

| Predicate | Constructor | Matches | FilterContext needs |
|-----------|-------------|---------|---------------------|
| `PermanentIsSourceCardPredicate` | `()` | the source card itself | `sourceCardId` |
| `PermanentIsSpecificPermanentPredicate` | `(UUID permanentId)` | exactly one permanent, by id — for effects whose stored predicate must be narrowed to a chosen target at resolution (Terrifying Presence) | none |
| `PermanentControlledBySourceControllerPredicate` | `()` | permanents controlled by source's controller | `gameData` + `sourceControllerId` |
| `PermanentControlledByActivePlayerPredicate` | `()` | permanents controlled by the active player (`gameData.activePlayerId`) | `gameData` |
| `PermanentControlledByDefendingPlayerPredicate` | `()` | permanents controlled by a defending player of the current combat (a player attacked directly or via one of their planeswalkers, per `GameQueryService.isPlayerBeingAttacked`). Matches nothing outside combat, so a spell using it is uncastable before attackers are declared. Yare | `gameData` |
| `PermanentControlledContinuouslySinceBeginningOfTurnPredicate` | `()` | permanents controlled continuously since the beginning of the turn (`!isSummoningSick()`; same signal as `CameUnderControlThisTurn` / Siren's Call exemption). Norritt | — |
| `PermanentCastBySourceControllerThisTurnPredicate` | `()` | permanents whose card the source's controller cast as a spell this turn ("target creature you cast this turn"). Card-identity match against `GameData.getSpellsCastThisTurn(sourceControllerId)`, so tokens/reanimated/Show-and-Tell arrivals never match. Needs a controller-aware `FilterContext` — usable in an ability `TargetFilter`, NOT in an effect `targetSpec()` predicate (`TargetValidationContext` carries no controller). Cycle of Life | — |
| `PermanentOwnedBySourceControllerPredicate` | `()` | permanents OWNED by source's controller (ownership via `stolenCreatures`). Pair inside `ControlledPermanentPredicateTargetFilter` for "you both own and control" (Obelisk of Undoing) | `gameData` + `sourceControllerId` |
| `PermanentControllerControlsPermanentPredicate` | `(PermanentPredicate filter)` / `(PermanentPredicate filter, boolean excludeSelf)` | permanents whose OWN controller controls at least one permanent matching `filter` (Seasinger — "target creature whose controller controls an Island" with `PermanentHasSubtypePredicate(ISLAND)`). `excludeSelf=true` skips the permanent being tested, i.e. "controls **another** creature" (Favorable Destiny). Supported in `PredicateEvaluationService.matchesStaticFilter`, so it works as a static aura filter | `gameData` |
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
| `StackEntryCardTypeInPredicate` | `(Set<CardType>)` | stack entries whose card has any of the given card types. On an activated/triggered ability entry the card is the ability's **source**, so `Set.of(CardType.ARTIFACT)` + `StackEntryTypeInPredicate(ACTIVATED_ABILITY)` is "activated ability from an artifact source" (Brown Ouphe) |
| `StackEntrySubtypeInPredicate` | `(Set<CardSubtype>)` | spells whose card has any of the given subtypes. Wrap in `StackEntryNotPredicate` for "non-[subtype] spell" (e.g. Faerie Trickery: counter target non-Faerie spell) |
| `StackEntryManaValuePredicate` | `(int manaValue)` | spells with exact mana value |
| `StackEntryManaValueEqualsXPredicate` | `()` | spells whose mana value equals the casting spell's chosen X. "counter target spell with mana value X" — Spell Blast. The chosen X is threaded from `SpellCastingService` into `TargetLegalityService.matchesStackEntryPredicate(..., xValue)` at cast-time targeting; when X is unknown (target enumeration) it matches permissively |
| `StackEntryManaValueEqualsSourceCountersPredicate` | `(CounterType)` | spells whose mana value (including chosen X) equals the number of that counter type on the evaluating **source** permanent. "whenever you cast a spell with mana value equal to the number of doom counters on this" — Imminent Doom. Source-dependent: the spell-cast collector passes the source permanent into `TargetLegalityService.matchesStackEntryPredicate` |
| `StackEntryManaValueAtMostControlledCountPredicate` | `(PermanentPredicate countFilter)` | spells whose mana value ≤ the number of permanents the evaluating player controls matching `countFilter`. "counter target spell with mana value X or less, where X is the number of [type] you control" — Spellstutter Sprite with `PermanentHasAnySubtypePredicate(FAERIE)` (counts itself, since it's already on the battlefield when the ETB resolves) |
| `StackEntryIsNthSpellCastThisTurnPredicate` | `(int spellNumber)` | the spell at 1-based position `spellNumber` in this turn's **global** cast order across all players. "counter target spell that's the second spell cast this turn" — Second Guess with `2`. Read from `GameData.getSpellCastOrdinalThisTurn(cardId)` (appended by `recordSpellCast`, cleared each turn), so copies put on the stack without being cast never match |
| `StackEntryIsSingleTargetPredicate` | `()` | spells with exactly one target |
| `StackEntryHasTargetPredicate` | `()` | matches any spell or ability on the stack (always true). Signals to include triggered/activated abilities, not just spells. Used by Spellskite |
| `StackEntryControlledByPredicate` | `()` | spells controlled by the evaluating player (the source's own controller) |
| `StackEntryCastFromZonePredicate` | `(Zone)` | spells cast from the given zone (via the entry's `sourceZone`); e.g. `Zone.GRAVEYARD` for "casts a spell from a graveyard" (River Kelpie), distinguishing graveyard casts from exile casts |
| `StackEntryControlledByEnchantedPlayerPredicate` | `()` | spells controlled by the player the source aura is attached to (the enchanted player). The enchanted player's ID is supplied externally by the evaluating service (`PredicateEvaluationService.matchesStackEntryPredicate(entry, predicate, enchantedPlayerId)`). Used by Curse of Echoes |
| `StackEntrySharesChosenNameWithSourcePredicate` | `()` | spells whose card name equals the chosen name recorded on the source permanent (via a "choose a card name" ETB — `ChooseCardNameOnEnterEffect`). "counter target spell with the chosen name" — Declaration of Naught. Source-dependent: matches nothing unless the source permanent is passed to `TargetLegalityService.matchesStackEntryPredicate(..., source)`; the ability-activation path supplies it automatically |
| `StackEntryTargetsYourPermanentPredicate` | `()` | spells targeting a permanent you control |
| `StackEntryTargetsSourcePredicate` | `()` | spells/abilities targeting the evaluating **source** permanent. "counter target spell that targets this creature" — Mistfolk. Source-dependent: matches nothing unless the source permanent is passed to `TargetLegalityService.matchesStackEntryPredicate(..., source)`; the ability-activation path supplies it automatically |
| `StackEntryTargetsYouOrCreatureYouControlPredicate` | `()` | spells/abilities targeting you or a creature you control. Also usable as `SpellCastTriggerEffect.castSpellTargetCondition` in `ON_OPPONENT_CASTS_SPELL` — the condition is evaluated from the trigger source's controller (Reparations) |
| `StackEntryTargetsYouPredicate` | `()` | spells/abilities targeting you (the player only, not your permanents). "... spell that targets you" — Mirror Sheen |
| `StackEntryTargetsAnyPlayerPredicate` | `()` | spells/abilities targeting at least one player (any player, not just you). "counter target spell that targets a player" — Outwit |
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
| `CardIsMulticoloredPredicate` | `()` | a card with two or more colours (`Card.getColors().size() >= 2`); monocoloured and colourless cards never match. Card-in-any-zone counterpart of `PermanentIsMonocoloredPredicate`; used as a graveyard filter for "target multicolored card from your graveyard" (Reborn Hope) |
| `CardIsColorlessPredicate` | `()` | colorless cards (`Card.getColors()` empty). Compose with `CardTypePredicate(CREATURE)` via `CardAllOfPredicate` for "colorless creature card" (Grizzled Angler) |
| `CardControllerDoesNotOwnPredicate` | `()` | a card whose owner is not the perspective player (the `cardOwnerId` argument of `matchesCardPredicate`, which is the casting player in the spell-cast trigger path). Cards with no tracked owner (tokens/copies) never match. Use as a `SpellCastTriggerEffect` filter for "a spell you don't own" (Nita, Forum Conciliator). Ownership is stamped at game setup on `Card.ownerId` and preserved across zones |
| `CardPowerAtMostPredicate` | `(int maxPower)` | a card whose printed power is <= `maxPower`; cards without power (non-creatures) never match. Compose with `CardTypePredicate(CREATURE)` via `CardAllOfPredicate` for library searches like "a creature card with power 2 or less" (Imperial Recruiter) |
| `CardPowerAtLeastPredicate` | `(int minPower)` | a card whose printed power is >= `minPower`; cards without power (non-creatures) never match. Compose with `CardTypePredicate(CREATURE)` via `CardAllOfPredicate` for "a creature card with power 5 or greater" (Sacellum Godspeaker) |
| `CardMaxManaValuePredicate` | `(int maxManaValue)` | a card with mana value ≤ N (e.g. Teshar's "mana value 3 or less" graveyard filter) |
| `CardSharesNameWithAPermanentPredicate` | `()` | a card with the same name as any permanent on any battlefield (Mitotic Manipulation via `LookAtTopCardsEffect.mayPutMatchingOntoBattlefield`). Needs the `GameData` overload of `matchesCardPredicate`; matches nothing without game state |
| `CardHasFlashbackPredicate` | `()` | a card that has a flashback casting option (Runic Repetition's "target exiled card with flashback") |
| `CardHasCyclingPredicate` | `()` | a card with cycling (or typecycling/landcycling) — detected via a hand-activated ability whose description name ends with `"cycling"` (`ActivatedAbility.isCyclingAbility()`). Use as the filter of `ReturnTargetCardsFromGraveyardToHandEffect` for "cards with cycling" (Sacred Excavation) |
| `CardHasEmbalmOrEternalizePredicate` | `()` | a card with an embalm or eternalize ability — detected via a graveyard-activated ability that creates a token copy of its source (`ActivatedAbility.isEmbalmOrEternalize()`, i.e. it carries a `CreateTokenCopyOfSourceEffect`, the shared marker for both keywords). Combine with `CardTypePredicate(CREATURE)` via `CardAllOfPredicate` for "creature card with eternalize or embalm" (Vizier of the Anointed's search) |
