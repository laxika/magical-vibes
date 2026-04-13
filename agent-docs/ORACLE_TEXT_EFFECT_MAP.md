# ORACLE_TEXT_EFFECT_MAP

Purpose: quickly map oracle text phrases to the correct effect class + slot. Search this file for keywords from the card's oracle text to find the matching effect without reading EFFECTS_QUICK_REFERENCE.md.

## Damage

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "deals N damage to any target" | `DealDamageToAnyTargetEffect(N, false)` | SPELL | Targeting auto-derived |
| "deals N damage to target creature" | `DealDamageToTargetCreatureEffect(N)` | SPELL | Creature-only targeting |
| "deals N damage to target creature or planeswalker" | `DealDamageToTargetCreatureOrPlaneswalkerEffect(N)` | SPELL | |
| "deals N damage to target opponent or planeswalker" | `DealDamageToTargetOpponentOrPlaneswalkerEffect(N)` | SPELL | |
| "deals N damage to target player" | `DealDamageToTargetPlayerEffect(N)` | SPELL | |
| "deals N damage to each opponent" | `DealDamageToEachOpponentEffect(N)` | SPELL/trigger | No targeting |
| "deals N damage to each player" | `DealDamageToEachPlayerEffect(N)` | SPELL | No targeting |
| "deals N damage to each creature" | `MassDamageEffect(N)` | SPELL | No targeting |
| "deals N damage to each creature and each planeswalker" | `MassDamageEffect(N, false, false, true, null)` | SPELL | damagesPlaneswalkers=true |
| "deals X damage to any target" | `DealXDamageToAnyTargetEffect()` | SPELL | X-cost |
| "deals X damage to target creature" | `DealXDamageToTargetCreatureEffect()` | SPELL | X-cost |
| "deals damage equal to its power to target" | `FirstTargetDealsPowerDamageToSecondTargetEffect()` | SPELL | Bite — multi-target |
| "fights target creature" | `FirstTargetFightsSecondTargetEffect()` | SPELL | Multi-target |
| "target creature fights another target creature" | `FirstTargetFightsSecondTargetEffect()` | SPELL | Multi-target, any two creatures; distinct is the default |
| "deals N damage to you" | `DealDamageToControllerEffect(N)` | SPELL/trigger | Self-damage |

## Creature pump / boost

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "target creature gets +X/+Y until end of turn" | `BoostTargetCreatureEffect(X, Y)` | SPELL | Targeting auto-derived |
| "creatures you control get +X/+Y until end of turn" | `BoostAllOwnCreaturesEffect(X, Y)` | SPELL | No targeting |
| "creatures you control get +X/+Y until end of turn" (with predicate) | `BoostAllOwnCreaturesEffect(X, Y, predicate)` | SPELL | Filtered |
| "all creatures get +X/+Y until end of turn" | `BoostAllCreaturesEffect(X, Y)` | SPELL | Affects all |
| "CARDNAME gets +X/+Y until end of turn" | `BoostSelfEffect(X, Y)` | ability effect | Self-pump |
| "CARDNAME's power and toughness are each equal to the number of lands you control" | `PowerToughnessEqualToControlledLandCountEffect()` | STATIC | |
| "CARDNAME's power and toughness are each equal to the number of creatures you control" | `PowerToughnessEqualToControlledCreatureCountEffect()` | STATIC | |
| "switch target creature's power and toughness" | `SwitchPowerToughnessEffect()` | SPELL | |
| "target creature has base power and toughness X/Y" | `SetBasePowerToughnessUntilEndOfTurnEffect(X, Y)` | SPELL | |
| "double target creature's power and toughness" | `DoubleSelfPowerToughnessEffect()` | — | Self only |

## Static boost (permanents on battlefield)

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "other [subtype] creatures you control get +X/+Y" | `StaticBoostEffect(X, Y, Set.of(), OWN_CREATURES, PermanentHasAnySubtypePredicate(subtype))` | STATIC | Lord |
| "creatures you control get +X/+Y" | `StaticBoostEffect(X, Y, Set.of(), OWN_CREATURES, null)` | STATIC | Anthem |
| "other [subtype] creatures get +X/+Y" (all players) | `StaticBoostEffect(X, Y, Set.of(), ALL_CREATURES, PermanentHasAnySubtypePredicate(subtype))` | STATIC | Global lord |
| "creatures opponents control get -X/-Y" | `StaticBoostEffect(-X, -Y, Set.of(), OPPONENT_CREATURES, null)` | STATIC | |
| "enchanted creature gets +X/+Y" | `StaticBoostEffect(X, Y, Set.of(), ENCHANTED_CREATURE, null)` | STATIC | Aura |
| "equipped creature gets +X/+Y" | `StaticBoostEffect(X, Y, Set.of(), EQUIPPED_CREATURE, null)` | STATIC | Equipment |

## Keywords / abilities

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "target creature gains [keyword] until end of turn" | `GrantKeywordEffect(Keyword.X, GrantScope.TARGET)` | SPELL | |
| "creatures you control gain [keyword] until end of turn" | `GrantKeywordEffect(Keyword.X, GrantScope.OWN_CREATURES)` | SPELL | |
| "enchanted creature has [keyword]" | `GrantKeywordEffect(Keyword.X, GrantScope.ENCHANTED_CREATURE)` | STATIC | |
| "equipped creature has [keyword]" | `GrantKeywordEffect(Keyword.X, GrantScope.EQUIPPED_CREATURE)` | STATIC | |
| "other [subtype] creatures you control have [keyword]" | `GrantKeywordEffect(Keyword.X, GrantScope.OWN_CREATURES, predicate)` | STATIC | |
| "target creature can't be blocked this turn" | `MakeCreatureUnblockableEffect()` | SPELL | |
| "regenerate target creature" | `RegenerateEffect(true)` | ability effect | targetsPermanent=true |
| "regenerate CARDNAME" | `RegenerateEffect()` | ability effect | Self |

## Destruction / sacrifice

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "destroy target [permanent type]" | `DestroyTargetPermanentEffect(false)` | SPELL | + PermanentPredicate filter |
| "destroy target creature. It can't be regenerated" | `DestroyTargetPermanentEffect(true)` | SPELL | cantRegenerate=true |
| "destroy all creatures" | `DestroyAllPermanentsEffect(PermanentIsCreaturePredicate())` | SPELL | |
| "destroy all [type]" | `DestroyAllPermanentsEffect(predicate)` | SPELL | Filtered wipe |
| "sacrifice a creature" | `SacrificeCreatureEffect()` | SPELL | Target player sacrifices |
| "each opponent sacrifices a creature" | `EachOpponentSacrificesCreatureEffect()` | SPELL/trigger | |
| "sacrifice CARDNAME" | `SacrificeSelfEffect()` | trigger/ability | |

## Bounce / tuck

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "return target [permanent] to its owner's hand" | `ReturnTargetPermanentToHandEffect()` | SPELL | + filter |
| "return all creatures to their owners' hands" | `ReturnCreaturesToOwnersHandEffect(filters)` | SPELL | Mass bounce |
| "put target [permanent] on top of its owner's library" | `PutTargetOnTopOfLibraryEffect()` | SPELL | |
| "put target [permanent] on the bottom of its owner's library" | `PutTargetOnBottomOfLibraryEffect()` | SPELL | |

## Counter spells

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "counter target spell" | `CounterSpellEffect()` | SPELL | Targeting auto-derived |
| "counter target spell unless its controller pays {N}" | `CounterUnlessPaysEffect(N)` | SPELL | |
| "this spell can't be countered" | `CantBeCounteredEffect()` | STATIC | |

## Draw / discard

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "draw N cards" / "draw a card" | `DrawCardEffect(N)` | SPELL/trigger | |
| "draw X cards" | `DrawXCardsEffect()` | SPELL | X-cost |
| "target player draws N cards" | `DrawCardForTargetPlayerEffect(N)` | SPELL | |
| "each player draws N cards" | `EachPlayerDrawsCardEffect(N)` | SPELL | |
| "draw N cards, then discard M cards" | `DrawAndDiscardCardEffect(N, M)` | SPELL | Loot |
| "discard N cards, then draw M cards" | `DiscardAndDrawCardEffect(N, M)` | SPELL | Rummage |
| "discard a card" / "discard N cards" | `DiscardCardEffect(N)` | SPELL/trigger | Controller discards |
| "target player discards N cards" | `TargetPlayerDiscardsEffect(N)` | SPELL | |
| "each player discards N cards" | `EachPlayerDiscardsEffect(N)` | SPELL | |
| "each opponent discards a card" | `EachOpponentDiscardsEffect(1)` | SPELL/trigger | |
| "look at target player's hand" | `LookAtHandEffect()` | SPELL | |

## Life

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "you gain N life" / "gain N life" | `GainLifeEffect(N)` | SPELL/trigger | |
| "you lose N life" / "lose N life" | `LoseLifeEffect(N)` | SPELL/trigger | |
| "target player gains N life" | `TargetPlayerGainsLifeEffect(N)` | SPELL | |
| "target player loses N life" | `TargetPlayerLosesLifeEffect(N)` | SPELL | |
| "each opponent loses N life" | `EachOpponentLosesLifeEffect(N)` | SPELL/trigger | |
| "each opponent loses N life and you gain life equal to the life lost" | `EachOpponentLosesLifeAndControllerGainsLifeLostEffect(N)` | SPELL | Drain |
| "double target player's life total" | `DoubleTargetPlayerLifeEffect()` | SPELL | |
| "players can't gain life" | `PlayersCantGainLifeEffect()` | STATIC | |

## Library manipulation

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "search your library for a card" (any to hand) | `SearchLibraryForCardToHandEffect()` | SPELL | |
| "search your library for a basic land card, put it into your hand" | `SearchLibraryForBasicLandToHandEffect()` | SPELL | |
| "search your library for a basic land card, put it onto the battlefield tapped" | `SearchLibraryForCardTypesToBattlefieldEffect(CardTypePredicate(LAND, basic), true)` | SPELL | |
| "search your library for a [type] card, reveal it, put it into your hand" | `SearchLibraryForCardTypesToHandEffect(predicate)` | SPELL | |
| "scry N" | `ScryEffect(N)` | SPELL/trigger | |
| "surveil N" | `SurveilEffect(N)` | SPELL/trigger | |
| "shuffle your library" | `ShuffleLibraryEffect()` | SPELL | |

## Mill

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "target player mills N cards" / "puts the top N cards into their graveyard" | `MillTargetPlayerEffect(N)` | SPELL | |
| "each opponent mills N cards" | `MillEachOpponentEffect(N)` | SPELL/trigger | |
| "mill N cards" (self) | `MillControllerEffect(N)` | SPELL/trigger | |
| "target player mills half their library" | `MillHalfLibraryEffect()` | SPELL | |

## Exile

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "exile target [permanent]" | `ExileTargetPermanentEffect()` | SPELL | |
| "exile target [permanent]. Return it at the beginning of the next end step" | `ExileTargetPermanentEffect(true)` | SPELL | returnEndStep=true |
| "exile all creatures" | `ExileAllCreaturesEffect()` | SPELL | |
| "exile target player's graveyard" | `ExileTargetPlayerGraveyardEffect()` | SPELL | |

## Tokens

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "create N 1/1 white Spirit creature tokens with flying" | `CreateTokenEffect.whiteSpirit(N)` | SPELL/trigger | Static factory |
| "create N 2/2 black Zombie creature tokens" | `CreateTokenEffect.blackZombie(N)` | SPELL/trigger | Static factory |
| "create N 1/1 white Soldier creature tokens" | `CreateTokenEffect.whiteSoldier(N)` | SPELL/trigger | Static factory |
| "create a Treasure token" | `CreateTokenEffect.ofTreasureToken(1)` | SPELL/trigger | Static factory |
| "create a N/N [color] [Subtype] creature token" | `CreateTokenEffect("name", N, N, color, subtype)` | SPELL/trigger | Custom token |
| "create a N/N [color] [Subtype] creature token with [keyword]" | `CreateTokenEffect("name", N, N, color, subtype, keyword)` | SPELL/trigger | With keyword |

## Graveyard return

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "return target [type] card from your graveyard to your hand" | `ReturnCardFromGraveyardEffect.builder().destination(HAND).filter(predicate).targetGraveyard(true).build()` | SPELL | |
| "return target creature card from your graveyard to the battlefield" | `ReturnCardFromGraveyardEffect.builder().destination(BATTLEFIELD).filter(CardTypePredicate(CREATURE)).targetGraveyard(true).build()` | SPELL | |
| "return target card from your graveyard to the top of your library" | `ReturnCardFromGraveyardEffect.builder().destination(TOP_OF_OWNERS_LIBRARY).targetGraveyard(true).build()` | SPELL | |

## Counters

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "put a +1/+1 counter on target creature" | `PutPlusOnePlusOneCounterOnTargetCreatureEffect(1)` | SPELL/trigger | |
| "put N +1/+1 counters on target creature" | `PutPlusOnePlusOneCounterOnTargetCreatureEffect(N)` | SPELL/trigger | |
| "put a +1/+1 counter on each creature you control" | `PutPlusOnePlusOneCounterOnEachOwnCreatureEffect()` | SPELL/trigger | |
| "put a -1/-1 counter on target creature" | `PutMinusOneMinusOneCounterOnTargetCreatureEffect(1)` | SPELL/trigger | |
| "proliferate" | `ProliferateEffect()` | SPELL/trigger | |

## Tap / untap

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "tap enchanted creature" | `TapEnchantedCreatureEffect()` | ability | aura's own activated ability; no targeting |
| "tap target [permanent]" | `TapTargetPermanentEffect()` | SPELL/ability | + filter |
| "untap target [permanent]" | `UntapTargetPermanentEffect(predicate)` | SPELL/ability | |
| "untap all [permanents] you control" | `UntapAllControlledPermanentsEffect(predicate)` | SPELL | |
| "CARDNAME doesn't untap during your untap step" | `DoesntUntapDuringUntapStepEffect()` | STATIC | |

## Control / steal

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "gain control of target [permanent]" | `GainControlOfTargetPermanentEffect()` | SPELL | Permanent |
| "gain control of target creature with power <= creature count" | `GainControlOfTargetPermanentEffect()` + `PermanentPowerAtMostControlledCreatureCountPredicate` filter | ability | Dynamic power check vs creature count |
| "gain control of target creature until end of turn" | `GainControlOfTargetPermanentUntilEndOfTurnEffect()` | SPELL | Threaten |
| "you control enchanted creature" | `GainControlOfEnchantedTargetEffect()` | STATIC | Control Magic |

## Mana

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "add {C}" / "add one mana of [color]" | `AwardManaEffect(ManaColor.X, 1)` | ON_TAP/ability | |
| "add one mana of any color" | `AwardAnyColorManaEffect()` | ON_TAP/ability | |
| "add N mana of any one color" | `AwardAnyColorManaEffect(N)` | ability | |

## Prevention

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "prevent all combat damage that would be dealt this turn" | `PreventAllCombatDamageEffect()` | SPELL | |
| "prevent the next N damage that would be dealt to target" | `PreventDamageToTargetEffect(N)` | SPELL | |

## Conditional wrappers

| Oracle text phrase | Wrapper | Notes |
|---|---|---|
| "you may [effect]" | `MayEffect(innerEffect, "prompt")` | Player chooses |
| "if you control three or more artifacts, [effect]" | `MetalcraftConditionalEffect(innerEffect)` | Metalcraft |
| "if a creature died this turn, [effect]" | `MorbidConditionalEffect(innerEffect)` | Morbid |
| "if you attacked this turn, [effect]" | `RaidConditionalEffect(innerEffect)` | Raid |
| "if [base], [effect]. If kicked, [upgraded effect] instead" | `KickerReplacementEffect(base, kicked)` | Kicker replaces |
| "if this spell was kicked, [additional effect]" | `KickedConditionalEffect(innerEffect)` | Kicker adds |
| "if you control a [subtype], [effect]" | `ControlsSubtypeConditionalEffect(subtype, innerEffect)` | Subtype check |
| "if you control a [matching permanent], [effect]" | `ControlsPermanentConditionalEffect(predicate, innerEffect)` | Permanent check |
| "choose one —" | `ChooseOneEffect(List<ChooseOneOption>)` | Modal |

## Turn / phase

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "take an extra turn after this one" | `ControllerExtraTurnEffect(1)` | SPELL | Non-targeting |
| "untap all creatures that attacked this turn. After this main phase, there is an additional combat phase followed by an additional main phase" | `AdditionalCombatMainPhaseEffect(1)` | SPELL | |

## Copy

| Oracle text phrase | Effect | Slot | Notes |
|---|---|---|---|
| "copy target instant or sorcery spell" | `CopySpellEffect()` | SPELL | |
| "enters the battlefield as a copy of any creature on the battlefield" | `CopyPermanentOnEnterEffect(predicate, label)` | ON_ENTER_BATTLEFIELD | Clone |

## Mana cost → test `addMana` reference

`ManaColor` enum values: `WHITE` (W), `BLUE` (U), `BLACK` (B), `RED` (R), `GREEN` (G), `COLORLESS` (C).

**Key rule:** Generic mana (the number in a mana cost like `{2}`) can be paid with any color. In tests, pay generic mana using the card's primary color for simplicity — one `addMana` call covers both the generic and colored portions.

### Single-color cards

For a card with one colored symbol, add total CMC of that color:

| Mana cost | `addMana` call | Breakdown |
|---|---|---|
| `{W}` | `addMana(player, WHITE, 1)` | 1W |
| `{U}` | `addMana(player, BLUE, 1)` | 1U |
| `{B}` | `addMana(player, BLACK, 1)` | 1B |
| `{R}` | `addMana(player, RED, 1)` | 1R |
| `{G}` | `addMana(player, GREEN, 1)` | 1G |
| `{1}{W}` | `addMana(player, WHITE, 2)` | 1 generic + 1W |
| `{1}{U}` | `addMana(player, BLUE, 2)` | 1 generic + 1U |
| `{1}{B}` | `addMana(player, BLACK, 2)` | 1 generic + 1B |
| `{1}{R}` | `addMana(player, RED, 2)` | 1 generic + 1R |
| `{1}{G}` | `addMana(player, GREEN, 2)` | 1 generic + 1G |
| `{2}{W}` | `addMana(player, WHITE, 3)` | 2 generic + 1W |
| `{2}{U}` | `addMana(player, BLUE, 3)` | 2 generic + 1U |
| `{2}{B}` | `addMana(player, BLACK, 3)` | 2 generic + 1B |
| `{2}{R}` | `addMana(player, RED, 3)` | 2 generic + 1R |
| `{2}{G}` | `addMana(player, GREEN, 3)` | 2 generic + 1G |
| `{3}{W}` | `addMana(player, WHITE, 4)` | 3 generic + 1W |
| `{4}{B}` | `addMana(player, BLACK, 5)` | 4 generic + 1B |
| `{W}{W}` | `addMana(player, WHITE, 2)` | 2W |
| `{U}{U}` | `addMana(player, BLUE, 2)` | 2U |
| `{B}{B}` | `addMana(player, BLACK, 2)` | 2B |
| `{R}{R}` | `addMana(player, RED, 2)` | 2R |
| `{G}{G}` | `addMana(player, GREEN, 2)` | 2G |
| `{1}{W}{W}` | `addMana(player, WHITE, 3)` | 1 generic + 2W |
| `{2}{U}{U}` | `addMana(player, BLUE, 4)` | 2 generic + 2U |
| `{2}{B}{B}` | `addMana(player, BLACK, 4)` | 2 generic + 2B |
| `{3}{R}{R}` | `addMana(player, RED, 5)` | 3 generic + 2R |
| `{3}{G}{G}` | `addMana(player, GREEN, 5)` | 3 generic + 2G |

**Pattern:** For `{N}{C}{C}` where C is a single color, use `addMana(player, COLOR, N + coloredCount)`.

### Multi-color cards

For cards with multiple colored symbols, add each color separately. Pay generic mana as COLORLESS:

| Mana cost | `addMana` calls | Breakdown |
|---|---|---|
| `{R}{W}` | `addMana(player, RED, 1); addMana(player, WHITE, 1)` | 1R + 1W |
| `{U}{B}` | `addMana(player, BLUE, 1); addMana(player, BLACK, 1)` | 1U + 1B |
| `{B}{G}` | `addMana(player, BLACK, 1); addMana(player, GREEN, 1)` | 1B + 1G |
| `{R}{G}` | `addMana(player, RED, 1); addMana(player, GREEN, 1)` | 1R + 1G |
| `{W}{U}` | `addMana(player, WHITE, 1); addMana(player, BLUE, 1)` | 1W + 1U |
| `{1}{R}{W}` | `addMana(player, RED, 1); addMana(player, WHITE, 1); addMana(player, COLORLESS, 1)` | 1R + 1W + 1 generic |
| `{1}{B}{G}` | `addMana(player, BLACK, 1); addMana(player, GREEN, 1); addMana(player, COLORLESS, 1)` | 1B + 1G + 1 generic |
| `{2}{W}{U}` | `addMana(player, WHITE, 1); addMana(player, BLUE, 1); addMana(player, COLORLESS, 2)` | 1W + 1U + 2 generic |
| `{2}{B}{R}` | `addMana(player, BLACK, 1); addMana(player, RED, 1); addMana(player, COLORLESS, 2)` | 1B + 1R + 2 generic |
| `{1}{W}{U}{B}` | `addMana(player, WHITE, 1); addMana(player, BLUE, 1); addMana(player, BLACK, 1); addMana(player, COLORLESS, 1)` | 3-color + 1 generic |

**Pattern:** Add exact colored amounts per color, then add generic as `COLORLESS`.

### X-cost cards

X is chosen at cast time. Add the colored requirement plus X of any color:

| Mana cost | `addMana` call for X=3 | Breakdown |
|---|---|---|
| `{X}{R}` | `addMana(player, RED, 4)` | X(3) + 1R |
| `{X}{U}` | `addMana(player, BLUE, 4)` | X(3) + 1U |
| `{X}{B}{B}` | `addMana(player, BLACK, 5)` | X(3) + 2B |
| `{X}{R}{G}` | `addMana(player, RED, 1); addMana(player, GREEN, 1); addMana(player, COLORLESS, 3)` | 1R + 1G + X(3) generic |

### Colorless / artifact cards

| Mana cost | `addMana` call | Notes |
|---|---|---|
| `{0}` | *(no mana needed)* | Free cast |
| `{1}` | `addMana(player, COLORLESS, 1)` | Any color also works |
| `{2}` | `addMana(player, COLORLESS, 2)` | |
| `{3}` | `addMana(player, COLORLESS, 3)` | |
| `{5}` | `addMana(player, COLORLESS, 5)` | |
