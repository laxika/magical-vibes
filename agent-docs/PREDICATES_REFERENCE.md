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
| `TargetFilters.nonlandPermanent()` | nonland permanent | "Target must be a nonland permanent" (Soul Tithe) |
| `TargetFilters.nonlandPermanentAnOpponentControls()` | nonland permanent an opponent controls | "Target must be a nonland permanent an opponent controls" (Archon of the Triumvirate) |
| `TargetFilters.noncreaturePermanentAnOpponentControls()` | noncreature permanent an opponent controls | "Target must be a noncreature permanent an opponent controls" (Sylvan Primordial) |

The message is shown to a player who picks an illegal target, so it is part of the card's
behaviour. If the card needs different wording — "First target must be a creature", "Second
target must be a creature you don't control" — or a restriction with no factory, build the
filter directly rather than reusing a factory whose wording does not match.

## TargetFilter types

| Filter class | Constructor | Use when |
|-------------|-------------|----------|
| `PermanentPredicateTargetFilter` | `(PermanentPredicate, String errorMsg)` | Target any permanent matching predicate |
| `AnyTargetPredicateTargetFilter` | `(PermanentPredicate, PlayerPredicate, String errorMsg)` | Restrict an "any target" (creature/planeswalker/battle/player) effect: the `PermanentPredicate` gates permanent targets, the `PlayerPredicate` gates player targets — both expressing the same restriction. Use for "any target that was dealt damage this turn" (Needle Drop): `PermanentDealtDamageThisTurnPredicate` + `PlayerDealtDamageThisTurnPredicate` |
| `ControlledPermanentPredicateTargetFilter` | `(PermanentPredicate, String errorMsg)` | Target only permanents YOU control matching predicate |
| `OwnedPermanentPredicateTargetFilter` | `(PermanentPredicate, String errorMsg)` | Target only permanents YOU OWN matching predicate (ownership via stolenCreatures map) |
| `StackEntryPredicateTargetFilter` | `(StackEntryPredicate, String errorMsg)` | Target a spell on the stack |
| `PlayerPredicateTargetFilter` | `(PlayerPredicate, String errorMsg)` | Target a player matching predicate |
| `GraveyardCardPredicateTargetFilter` | `(CardPredicate, GraveyardSearchScope)` | Target a card in a graveyard, with the scope declared **per target group** — this is what lets one spell take two graveyard targets with different scopes (Spelltwine: "target instant or sorcery card from your graveyard **and** target instant or sorcery card from an opponent's graveyard"). `null` predicate = any card. Enumeration (`ValidTargetService.computeValidGraveyardTargetsForFilter`), cast-time validation, and the CR 608.2b resolution recheck all key off this filter, so the effect itself just needs a graveyard `targetSpec()` and can stay unbound to read every chosen card |

## PermanentPredicate compositions

### Basic type/state predicates

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PermanentBlockedBySourceThisTurnPredicate` | `()` | creatures that were blocked by the source permanent this turn (attacker direction only). Reads `GameData.creaturesBlockedThisTurn` and the source's recorded combat-opponent IDs, so it remains usable after combat state is cleared; requires a `FilterContext` source permanent ID or source snapshot. Wall of Nets |
| `PermanentIsCreaturePredicate` | `()` | creatures |
| `PermanentIsArtifactPredicate` | `()` | artifacts |
| `PermanentIsLandPredicate` | `()` | lands |
| `PermanentIsEnchantmentPredicate` | `()` | enchantments |
| `PermanentIsEnchantedPredicate` | `()` | permanents that have at least one Aura attached (i.e. are enchanted), regardless of who controls the Aura — needs game data. Used by Greater Auramancy ("Enchanted creatures you control have shroud") |
| `PermanentIsHostOfSourceAuraPredicate` | `()` | the permanent the **source Aura** is currently attached to (the enchanted permanent) — needs game data + `sourceCardId`, or `FilterContext.sourcePermanentSnapshot` / static `StaticEffectContext.source` when GameData is unavailable. Wrap in `PermanentNotPredicate` for "other than enchanted creature" (Kjeldoran Pride reattach; Vampirism's other-creature count/`OWN_CREATURES` filter) |
| `PermanentSharesColorWithEquippedCreaturePredicate` | `()` | permanents sharing at least one color with the creature the **source Equipment** is attached to — needs game data + `sourceCardId` / `FilterContext.sourcePermanentSnapshot`. Never matches while unattached, and never matches when either side is colorless. Konda's Banner ("Creatures that share a color with equipped creature get +1/+1") |
| `PermanentSharesCreatureTypeWithEquippedCreaturePredicate` | `()` | permanents sharing at least one creature type with the creature the **source Equipment** is attached to; Changeling counts as every creature type. Same context needs as the color form; never matches while unattached. Konda's Banner |
| `PermanentIsAuraAttachedToCreaturePredicate` | `()` | an Aura permanent currently attached to a creature (checks `card.isAura()`, `isAttached()`, and that the host permanent is a creature — needs game data). Used to filter the Aura target of Crown of the Ages ("target Aura attached to a creature") |
| `PermanentIsAuraAttachedToSourcePredicate` | `()` | an Aura permanent currently attached to the **source** permanent, whoever controls the Aura — needs game data + `sourceCardId`. Pair with `DestroyAllPermanentsEffect` for "Destroy all Auras attached to CARDNAME" (Hakim, Loreweaver) |
| `PermanentIsPlaneswalkerPredicate` | `()` | planeswalkers |
| `PermanentIsBattlePredicate` | `()` | battles. Layer-aware like the planeswalker leaf; the permanent half of `TargetPredicates.anyTarget()` (CR 115.4) and the only leaf that separates it from `creatureOrPlaneswalker()`. Deliberately **not** in `matchesStaticFilter`'s whitelist — no static ability filters on "battle" |
| `PermanentIsTappedPredicate` | `()` | tapped permanents |
| `PermanentIsRenownedPredicate` | `()` | renowned permanents (CR 702.112b — the marker `RenownEffect` sets on `Permanent.renowned`). Target-side counterpart of the `SourceIsRenowned` condition: pair with `TargetPermanentMatches` for "if it's renowned, …" (Enshrouding Mist) |
| `PermanentIsAttackingPredicate` | `()` | attacking creatures |
| `PermanentIsAttackingSourceControllerPredicate` | `()` | creatures attacking you (the source controller) — attack target must be the source controller, not a planeswalker/other player; needs a `FilterContext` with source controller (Blessed Reversal). Also usable as a static `StaticBoostEffect`/`GrantKeywordEffect` filter — `matchesStaticFilter` reads the source controller off the context (Boarded Window and Watchdog, "creatures attacking you get -1/-0") |
| `PermanentIsBlockingPredicate` | `()` | blocking creatures (the blockers themselves). Also usable as a static GrantKeywordEffect/StaticBoostEffect filter (`matchesStaticFilter` supports it, like `PermanentIsAttackingPredicate`) — Snow Devil |
| `PermanentIsBlockedPredicate` | `()` | blocked creatures — attacking creatures that at least one creature is blocking. Distinct from `PermanentIsBlockingPredicate`. Pair with `BoostAllCreaturesEffect(.., filter)` / `GrantKeywordEffect(kw, ALL_CREATURES, filter)` for "each blocked creature ..." (Tattermunge Witch) |
| `PermanentIsUnblockedAttackingPredicate` | `()` | unblocked attacking creatures — current step at/after declare blockers, attacking, and no creature blocking it (and not made blocked by an effect). Before blockers are declared, attackers are not "unblocked". Complement of `PermanentIsBlockedPredicate` among attackers; `TargetFilters.unblockedAttackingCreature()`. Dazzling Beauty, Gossamer Chains |
| `PermanentAttackedOrBlockedThisTurnPredicate` | `()` | creatures that were declared as an attacker or blocker at some point this turn (reads `Permanent.attackedThisTurn`/`blockedThisTurn`, which persist after combat ends and clear at the next turn start — so the creature still matches in a later main phase or end step). Unlike `PermanentIsAttacking`/`IsBlockingPredicate` (current combat state only). AND with `PermanentIsCreaturePredicate` for "target creature that attacked or blocked this turn" (Vizier of Deferment) |
| `PermanentBlockedOrWasBlockedThisTurnPredicate` | `()` | creatures that blocked, or were blocked by, another creature this turn — both directions of a block. Reads the key set of `GameData.combatBlockOpponentIdsThisTurn` (recorded at declare-blockers time, turn-scoped not combat-scoped), so an unblocked attacker never matches. AND with `PermanentIsCreaturePredicate` for "each creature that blocked or was blocked this turn" (Heat Stroke) |
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
| `PermanentIsColorlessPredicate` | `()` | permanents with zero effective colors (monocolored and multicolored don't match); zero-color counterpart of `PermanentIsMonocoloredPredicate`; Infernal Reckoning ("target colorless creature" via `PermanentAllOfPredicate` with `PermanentIsCreaturePredicate`) |
| `PermanentIsMulticoloredPredicate` | `()` | permanents with two or more effective colors (colorless and monocolored don't match); complement of `PermanentIsMonocoloredPredicate`, battlefield counterpart of `CardIsMulticoloredPredicate`; Esper Stormblade ("another multicolored permanent" via `ControlsAnotherPermanent`) |
| `PermanentHasSubtypePredicate` | `(CardSubtype)` | permanents with specific subtype |
| `PermanentHasAnySubtypePredicate` | `(Set<CardSubtype>)` | permanents with any of the subtypes |
| `PermanentHasSupertypePredicate` | `(CardSupertype)` | permanents with specific supertype (e.g. LEGENDARY). Evaluated through `GameQueryService.hasEffectiveSupertype`, so a global `PermanentsMatchingLoseSupertypeEffect` (Melting) correctly makes it false |
| `PermanentHasKeywordPredicate` | `(Keyword)` | permanents with specific keyword |
| `PermanentHasProtectionFromColorPredicate` | `(CardColor)` | permanents with protection from that color (Escaped Shapeshifter). One color per instance — "protection from any color" is five predicates. Inside static-bonus assembly it answers from printed protection + the in-flight layer-6 state instead of re-entering `computeStaticBonus` |
| `PermanentHasCountersPredicate` | `(CounterType)` | permanents with one or more counters of the specified type (supports ANY for any counter) |
| `PermanentHasCumulativeUpkeepPredicate` | `()` | permanents that have cumulative upkeep (printed or granted) |

### Static power/toughness/mana-value predicates

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PermanentPowerAtLeastPredicate` | `(int minPower)` | creatures with power >= N |
| `PermanentPowerAtMostPredicate` | `(int maxPower)` | creatures with power <= N |
| `PermanentMaxManaValuePredicate` | `(int maxManaValue)` | permanents with mana value <= N (e.g. Witherbloom Charm) |
| `PermanentManaValueAtMostOwnCountersPredicate` | `(CounterType)` | permanents whose mana value ≤ the number of that counter type on them (Corrosion rust destroy) |
| `PermanentManaValueEqualsSourceCountersPredicate` | `(CounterType)` | permanents whose mana value **equals** the number of that counter type on the evaluating **source** permanent ("destroy each creature with mana value equal to the number of age counters on this enchantment" — Wave of Terror). Falls back to `FilterContext.sourcePermanentSnapshot()` once the source is gone (CR 608.2b) |
| `PermanentMinManaValuePredicate` | `(int minManaValue)` | permanents with mana value >= N (e.g. Austere Command) |
| `PermanentToughnessAtMostPredicate` | `(int maxToughness)` | creatures with toughness <= N |
| `PermanentPowerEqualsToughnessPredicate` | `()` | creatures whose effective power equals their effective toughness. Wrap in `PermanentNotPredicate` for "whose power and toughness aren't equal" (Gilt-Leaf Winnower) |
| `PermanentToughnessAtLeastPredicate` | `(int minToughness)` | creatures with toughness >= N (uses effective/last-known toughness; e.g. Colfenor's Urn) |

### Dynamic/game-state predicates (require FilterContext)

These predicates need `FilterContext` with `gameData` and/or `sourceControllerId`/`sourceCardId` to evaluate. The engine automatically provides the correct FilterContext during target validation.

| Predicate | Constructor | Matches | FilterContext needs |
|-----------|-------------|---------|---------------------|
| `PermanentPowerAtMostXPredicate` | `()` | creatures with power <= X (from FilterContext.xValue) | `xValue` |
| `PermanentPowerLessThanXPredicate` | `()` | creatures with power strictly < X (from FilterContext.xValue). Pair with `SacrificeSelfCost(true)`, which snapshots the source's effective power into the ability's X at payment, for "creatures you control with power less than this creature's power" (Lena, Selfless Champion) — works after the source has left the battlefield, unlike `PermanentPowerLessThanSourcePowerPredicate` | `xValue` |
| `PermanentPowerAtMostControlledCreatureCountPredicate` | `()` | creatures with power <= number of creatures source's controller controls | `gameData` + `sourceControllerId` |
| `PermanentManaValueEqualsXPredicate` | `()` | permanents with mana value == X (returns true when xValue is null) | `xValue` |
| `PermanentMaxManaValueXPredicate` | `()` | permanents with mana value <= X (returns true when xValue is null). Displacement Wave | `xValue` |
| `PermanentManaValueAtMostXPredicate` | `()` | permanents with mana value <= X (returns true when xValue is null) | `xValue` |
| `PermanentMaxManaValueXPredicate` | `()` | permanents with mana value <= X (returns true when xValue is null). Used by Displacement Wave and Quillmane Baku, where X is the remove-X-counters activation cost | `xValue` |
| `PermanentToughnessLessThanSourcePowerPredicate` | `()` | creatures with toughness < source permanent's effective power | `gameData` + `sourceCardId` |
| `PermanentPowerAtMostSourcePowerPredicate` | `()` | creatures with power <= source permanent's effective power (Earthshaker Khenra's ETB "target creature with power less than or equal to this creature's power" — source-relative so a 4/4 Eternalize token can target up to power 4) | `gameData` + `sourceCardId` |
| `PermanentPowerAtMostSourceCountersPredicate` | `(CounterType)` | creatures with power <= the number of counters of that type on the source permanent (Legacy's Allure: "target creature with power less than or equal to the number of treasure counters on this enchantment"). Falls back to `FilterContext.sourcePermanentSnapshot()` once the source is gone, so it works for abilities that sacrifice the source as a cost (CR 608.2b) | `gameData` + `sourceCardId` |
| `PermanentPowerLessThanSourcePowerPredicate` | `()` | creatures with power strictly < source permanent's effective power (Champion of Lambholt: "Creatures with power less than this creature's power can't block creatures you control"). Source must be on the battlefield | `gameData` + `sourceCardId` |
| `PermanentInCombatWithSourcePredicate` | `()` | creatures blocking or blocked by the source permanent. Also usable as a STATIC scope filter (Alms Beast): the recursion-safe static path reads the block assignments off `FilterContext.sourcePermanentSnapshot()`, and combat state is part of the layered-board fingerprint so declaring blockers invalidates the memo | `gameData` + `sourceCardId`, `sourcePermanentSnapshot` on the static path |
| `PermanentBlockedBySourcePredicate` | `()` | creatures the source permanent is blocking — "target creature it's blocking" (Goblin Snowman, Tinder Wall). Narrower than `PermanentInCombatWithSourcePredicate`, which also matches creatures blocking the source. Per CR 608.2b, when the source has left the battlefield (e.g. sacrificed to pay the ability's cost) the resolution-time re-check falls back to `FilterContext.sourcePermanentSnapshot()` (the stack entry's last-known source), so the ability still resolves | `gameData` + `sourceCardId`, optional `sourcePermanentSnapshot` |
| `PermanentBlockingSourcePredicate` | `()` | creatures blocking the source permanent — "target creature blocking this creature" (Barbed-Back Wurm). Mirror image of `PermanentBlockedBySourcePredicate`; narrower than `PermanentInCombatWithSourcePredicate`. Per CR 608.2b, when the source has left the battlefield (e.g. sacrificed to pay the ability's cost — Urborg Panther) the resolution-time re-check falls back to `FilterContext.sourcePermanentSnapshot()`, so the ability still resolves. On an **attached Aura** source it reads the enchanted creature's blockers instead of the Aura's (an Aura is never blocked) — "creatures blocking enchanted creature", Coils of the Medusa | `gameData` + `sourceCardId`, optional `sourcePermanentSnapshot` |
| `PermanentHasGreatestPowerAmongControlledCreaturesPredicate` | `()` | creatures with greatest power among source controller's creatures (ties allowed) | `gameData` + `sourceControllerId` |
| `PermanentHasGreatestManaValueAmongAllCreaturesPredicate` | `()` | creatures with greatest mana value among all creatures on the battlefield across every player (ties allowed) | `gameData` |
| `PermanentHasGreatestPowerAmongAllCreaturesPredicate` | `()` | creatures with the greatest effective power among all creatures on the battlefield across every player (ties allowed). Topple | `gameData` |
| `PermanentHasLeastPowerAmongAllCreaturesPredicate` | `()` | creatures with the least effective power among all creatures on the battlefield across every player (ties allowed). Wretched Banquet | `gameData` |
| `PermanentDealtDamageThisTurnPredicate` | `()` | permanents dealt damage this turn (evaluated against `GameData.permanentsDealtDamageThisTurn`) | `gameData` |
| `PermanentDealtDamageToAnythingThisTurnPredicate` | `()` | permanents that dealt damage — combat or noncombat, to any player or creature — this turn ("target creature that dealt damage this turn", Avenging Arrow). Checks `GameData.combatDamageToPlayersThisTurn` + `noncombatDamageToPlayersThisTurn` + `creatureCardsDamagedThisTurnBySourcePermanent`, keyed by the candidate permanent. Note the opposite direction from `PermanentDealtDamageThisTurnPredicate` (which means *was* dealt damage) | `gameData` |
| `PermanentDealtDamageToSourceControllerThisTurnPredicate` | `()` | permanents that dealt damage — combat or noncombat — to the source's controller this turn ("target creature that dealt damage to you this turn", Giltspire Avenger). Checks `GameData.combatDamageToPlayersThisTurn` + `GameData.noncombatDamageToPlayersThisTurn` for `sourceControllerId` | `gameData` + `sourceControllerId` |
| `PermanentAttackedSourceControllerThisTurnPredicate` | `()` | creatures declared as attackers against the source's controller this turn ("target creature that attacked you this turn", Jabari's Influence). Checks `GameData.playersAttackedThisTurn` (written in `CombatAttackService.declareAttackers`, cleared at turn cleanup) for `sourceControllerId`; attacking a planeswalker that player controls does not match | `gameData` + `sourceControllerId` |
| `PermanentHasSameNameAsSourcePredicate` | `()` | permanents with same name as source (works with clones) | `gameData` + `sourceCardId` |
| `PermanentHasSourceChosenNamePredicate` | `()` | permanents whose card name equals the name chosen by the source permanent | source permanent snapshot or `gameData` + `sourceCardId` |
| `PermanentHasSourceChosenSubtypePredicate` | `()` | permanents carrying the subtype chosen as the **source** entered the battlefield (`Permanent.chosenSubtype`, set by `ChooseBasicLandTypeOnEnterEffect` and friends); matches nothing when the source is gone or made no choice. Supported in `StaticEffectSupport.matchesStaticFilter` (context-aware path), so it works as a static scope filter — Shimmer (`GrantKeywordEffect(PHASING, ALL_LANDS, …)` = "each land of the chosen type has phasing") | `gameData` + `sourceCardId` |
| `PermanentNamedPredicate` | `(String cardName)` | permanents with the given name (exact `Card.getName()` equality); e.g. "a permanent named Guan Yu, Sainted Warrior" | none |
| `PermanentSharesNameWithAnotherPermanentPredicate` | `()` | permanent shares its name with at least one other permanent on any battlefield (Eye of Singularity ETB wipe) | `gameData` |
| `PermanentNameInPredicate` | `(Set<String> cardNames)` | permanents whose name is one of a fixed roster of names (exact `Card.getName()` equality). For "a name originally printed in the Homelands expansion" (Apocalypse Chime) — the card class owns the name list, so a later reprint of a listed name still matches | none |

### Source-relative predicates

| Predicate | Constructor | Matches | FilterContext needs |
|-----------|-------------|---------|---------------------|
| `PermanentIsSourceCardPredicate` | `()` | the source card itself | `sourceCardId` |
| `PermanentIsSourcePermanentPredicate` | `()` | the source **permanent** itself, matched by permanent id (so a second copy of the same card is not matched). Wrap in `PermanentNotPredicate` for "each **other** …" wording (Renegade Krasis) | `sourcePermanentSnapshot` |
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
| `StackEntryMaxManaValuePredicate` | `(int maxManaValue)` | spells with mana value (including chosen X) <= N. "counter target spell with mana value 4 or less" — Thoughtbind |
| `StackEntryHasXInManaCostPredicate` | `()` | spells whose mana cost contains `{X}`, regardless of the chosen X. Used by Frontline Medic |
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
| `StackEntryTruePredicate` | `()` | always matches (no restriction). Stack counterpart of `PermanentTruePredicate` / `CardTruePredicate`; use it where "any spell on the stack" must be spelled as a predicate rather than as a `null` filter — notably inside `TargetPredicate.Spells`, whose inner predicate is never null |
| `StackEntryAllOfPredicate` | `(List<StackEntryPredicate>)` | AND composition |
| `StackEntryAnyOfPredicate` | `(List<StackEntryPredicate>)` | OR composition |
| `StackEntryNotPredicate` | `(StackEntryPredicate)` | NOT inversion |

## TargetPredicate — which predicate hierarchy applies

`TargetPredicate` (`model/effect/`) sits one level above the four hierarchies above: it says which
candidate **domain** a target is drawn from, and carries that domain's predicate as its payload. It
is what every effect *declares*: `TargetSpec.declaredTarget()` holds one, and
`TargetSpec.targetPredicate()` folds the spec's narrowing predicate into its permanent leaf. That
composed value is what the spec interpreter (`TargetValidationService`) and target enumeration
(`ValidTargetService`) evaluate.

`TargetSpec.admits(Kind)` is the null-safe "can this kind ever be legal?" query — a spec that targets
nothing has no predicate at all. It answers from `declaredTarget()` rather than the composed
`targetPredicate()`, because the narrowing predicate only ever replaces the permanent leaf's inner
predicate and so cannot add or remove a kind; that keeps it allocation-free for the trigger
collectors, `StepTriggerService` and the AI, which ask it per effect in loops.

`TargetSpec.declares(target)` is the other question: an identity test against one interned factory
value. A reader that means one specific declaration must use it — `admits(PLAYER) && admits(PERMANENT)`
cannot tell `anyTarget()` (CR 115.4) from `playerOrPermanent()` ("a player or *any* permanent"), and
telling those apart is why the flat category enum was replaced.

| Leaf | Payload | Evaluated by |
|------|---------|--------------|
| `TargetPredicate.Permanents` | `PermanentPredicate` | `PredicateEvaluationService.matchesPermanentPredicate` |
| `TargetPredicate.Players` | `PlayerPredicate` | `TargetLegalityService.matchesPlayerPredicate` |
| `TargetPredicate.GraveyardCards` | `CardPredicate` + `GraveyardSearchScope` | `PredicateEvaluationService.matchesCardPredicate` + the scope |
| `TargetPredicate.ExiledCards` | `CardPredicate` | `PredicateEvaluationService.matchesCardPredicate` |
| `TargetPredicate.Spells` | `StackEntryPredicate` | `TargetLegalityService.matchesStackEntryPredicate` |
| `TargetPredicate.AnyOf` | ≥2 leaves, at most one per kind | dispatches to the leaf of the asked-for kind |

Build them with the `TargetPredicates` factories wherever one fits: the named values are interned, so
specs compare and hash cheaply and `declares(...)` matches by value. A hand-composed target is legal
where no factory fits (say "an artifact or a player"), and both evaluation paths handle it.
Three rules are enforced structurally, not documented:

- **No `AllOf`, no `Not` at this level.** A kind-mismatched leaf is `false`, so a top-level `Not`
  would be true for every candidate of every other kind — over-permissive targeting. Conjunction and
  negation belong inside a kind (`PermanentAllOfPredicate`, `CardNotPredicate`, …), where they are
  sound.
- **`AnyOf` holds at most one leaf per kind**, flattened and sorted by kind, so it is a canonical
  `kind -> predicate` map. Two restrictions on the same kind must be merged into one
  `PermanentAnyOfPredicate` (or the card / stack equivalent).
- **No leaf carries a `null` inner predicate** — use the `*TruePredicate` of that kind. `null` means
  "matches nothing" to the permanent evaluator and "matches everything" to the card evaluator, and a
  target restriction must not depend on which convention a reader assumes.

Evaluate one through `TargetPredicateEvaluationService` (`service/target/`), which has one method per
kind and delegates to the service that already owns that hierarchy — it adds no evaluation logic and
never reaches `PredicateEvaluationService.matchesStaticFilter`. Its permanent/graveyard/exile/spell
methods require a `FilterContext` carrying `GameData`: without it the creature and land leaves fall
back to raw card types and mis-handle an animated land (CR 613.1d). `ValidTargetService`,
`MayAbilityHandlerService` and `PermanentChoiceBattlefieldHandlerService` all take the adapter:
`MayAbilityHandlerService` uses it for the one arm where a may-ability's `TargetSpec` is its only
target restriction (see `agent-docs/TRIGGER_SLOT_TARGETING.md` for that precedence), and
`PermanentChoiceBattlefieldHandlerService` for the reflexive-trigger target of
`SacrificePermanentThenEffect` (Sorin, Imperious Bloodlord).

`TargetValidationService` is the exception that does NOT go through that adapter: injecting it would
close the cycle `TargetValidationService → TargetPredicateEvaluationService → TargetLegalityService →
TargetValidationService`. It reads `TargetPredicate.permanentRestriction()` and evaluates that one
`PermanentPredicate` through `PredicateEvaluationService` directly — the same call the adapter makes.
`TriggerTargetCollector` does the same, for the same reason it already evaluates its `TargetFilter`
predicates that way: it holds a `PredicateEvaluationService` and the any-target narrowing is one
`PermanentPredicate` question.

**Never re-implement a declared restriction as a type check.** An enumeration path that open-codes
`isCreature || hasType(PLANESWALKER)` where the effect declares `TargetPredicates.anyTarget()` is a
recurring defect in this codebase — it silently drifts from the spell path, ignores layer 4, and
does not pick up a widening of the factory. Read the declared target and evaluate it.

## PlayerPredicate compositions

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `PlayerRelationPredicate` | `(PlayerRelation)` | player by relation. `PlayerRelation`: `OPPONENT`, `SELF` |
| `PlayerDealtDamageThisTurnPredicate` | `()` | players dealt damage this turn (evaluated against `GameData.playersDealtDamageThisTurn`). Player-side counterpart of `PermanentDealtDamageThisTurnPredicate`; pair them in an `AnyTargetPredicateTargetFilter` for "any target that was dealt damage this turn" |
| `PlayerDamagedBySourceThisTurnPredicate` | `()` | players dealt damage **by the ability's own source permanent** this turn, combat or noncombat (evaluated against `GameData.combatDamageToPlayersThisTurn` + `noncombatDamageToPlayersThisTurn`, both keyed by source permanent id). Source-relative: `TargetLegalityService.matchesPlayerPredicate` needs the source permanent id, which the activated-ability validation, valid-target enumeration and resolution-time recheck paths supply; any other path passes `null` and matches nobody. Used by Wicked Akuba's `{B}` ability |
| `PlayerControlsMoreLandsThanControllerPredicate` | `()` | an opponent who controls strictly more lands than the controller; checked when the target is chosen and again when the ability resolves. Oath of Lieges |
| `PlayerControlsMoreCreaturesThanControllerPredicate` | `()` | an opponent who controls strictly more creatures than the controller; checked when the target is chosen and again only for the opponent relationship at resolution. Keeper of the Beasts |
| `PlayerHasFewerCreatureCardsInGraveyardThanControllerPredicate` | `(int minimumDifference[, boolean recheckAtResolution])` | an opponent whose graveyard has at least `minimumDifference` fewer creature cards than the controller's when targeted; the optional flag also requires the comparison to remain true at resolution. Keeper of the Dead uses the default, so only the opponent relationship is rechecked |
| `PlayerHasMoreCardsInHandThanControllerPredicate` | `(int minimumDifference[, boolean recheckAtResolution])` | an opponent whose hand has at least `minimumDifference` more cards than the controller's hand when the target is selected; the optional flag also requires that comparison to remain true at resolution. Keeper of the Mind uses the default; Oath of Scholars uses `1, true` |
| `PlayerHasMoreLifeThanControllerPredicate` | `()` | an opponent whose life total is greater than the controller's life total when the target is selected. The life comparison is activation-time only; resolution rechecks only that the target is still an opponent. Keeper of the Light |
| `PlayerLostLifeThisTurnPredicate` | `()` | players that lost life this turn (evaluated against `GameData.lifeLostThisTurn`; damage counts). Used by Rix Maadi Guildmage's "target player who lost life this turn" |

## CardPredicate (spell/card filters)

| Predicate | Constructor | Matches |
|-----------|-------------|---------|
| `CardTruePredicate` | `()` | always matches (no restriction). Card counterpart of `PermanentTruePredicate`; use for unrestricted "spells" wordings (Helm of Awakening) |
| `CardIsTokenPredicate` | `()` | token cards. Wrap in `CardNotPredicate` for "nontoken" (e.g. Militia's Pride: nontoken attacker filter on `ON_ALLY_CREATURE_ATTACKS` via `TriggeringCardConditionalEffect`) |
| `CardIsMulticoloredPredicate` | `()` | a card with two or more colours (`Card.getColors().size() >= 2`); monocoloured and colourless cards never match. Card-in-any-zone counterpart of `PermanentIsMonocoloredPredicate`; used as a graveyard filter for "target multicolored card from your graveyard" (Reborn Hope) |
| `CardIsColorlessPredicate` | `()` | colorless cards (`Card.getColors()` empty). Compose with `CardTypePredicate(CREATURE)` via `CardAllOfPredicate` for "colorless creature card" (Grizzled Angler) |
| `CardControllerDoesNotOwnPredicate` | `()` | a card whose owner is not the perspective player (the `cardOwnerId` argument of `matchesCardPredicate`, which is the casting player in the spell-cast trigger path). Cards with no tracked owner (tokens/copies) never match. Use as a `SpellCastTriggerEffect` filter for "a spell you don't own" (Nita, Forum Conciliator). Ownership is stamped at game setup on `Card.ownerId` and preserved across zones |
| `CardPowerAtMostPredicate` | `(int maxPower)` | a card whose printed power is <= `maxPower`; cards without power (non-creatures) never match. Compose with `CardTypePredicate(CREATURE)` via `CardAllOfPredicate` for library searches like "a creature card with power 2 or less" (Imperial Recruiter) |
| `CardPowerAtLeastPredicate` | `(int minPower)` | a card whose printed power is >= `minPower`; cards without power (non-creatures) never match. Compose with `CardTypePredicate(CREATURE)` via `CardAllOfPredicate` for "a creature card with power 5 or greater" (Sacellum Godspeaker) |
| `CardMaxManaValuePredicate` | `(int maxManaValue)` | a card with mana value ≤ N (e.g. Teshar's "mana value 3 or less" graveyard filter) |
| `CardSharesNameWithAPermanentPredicate` | `()` | a card with the same name as any permanent on any battlefield (Mitotic Manipulation via `LookAtTopCardsEffect.mayPutMatchingOntoBattlefield`). Needs the `GameData` overload of `matchesCardPredicate`; matches nothing without game state |
| `CardHasSourceChosenSubtypePredicate` | `()` | a creature card carrying the creature subtype chosen by the source permanent; Changeling matches every creature type. Needs the `GameData` overload and the source card ID |
| `CardIsAuraEnchantCreaturePredicate` | `()` | an Aura card whose enchant ability restricts it to creatures ("enchant creature", "enchant creature you control", …). An Aura's enchant restriction is its spell target filter, so this looks for a `PermanentIsCreaturePredicate` in that filter (directly or inside a `PermanentAllOfPredicate`); Auras that enchant players, lands, artifacts, or any permanent never match. Use as a `GrantFlashToCardTypeEffect` filter (Rootwater Shaman) |
| `CardHasFlashbackPredicate` | `()` | a card that has a flashback casting option (Runic Repetition's "target exiled card with flashback") |
| `CardHasCyclingPredicate` | `()` | a card with cycling (or typecycling/landcycling) — detected via a hand-activated ability whose description name ends with `"cycling"` (`ActivatedAbility.isCyclingAbility()`). Use as the filter of `ReturnTargetCardsFromGraveyardToHandEffect` for "cards with cycling" (Sacred Excavation) |
| `CardHasEmbalmOrEternalizePredicate` | `()` | a card with an embalm or eternalize ability — detected via a graveyard-activated ability that creates a token copy of its source (`ActivatedAbility.isEmbalmOrEternalize()`, i.e. it carries a `CreateTokenCopyOfSourceEffect`, the shared marker for both keywords). Combine with `CardTypePredicate(CREATURE)` via `CardAllOfPredicate` for "creature card with eternalize or embalm" (Vizier of the Anointed's search) |
