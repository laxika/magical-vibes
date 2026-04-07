# EFFECTS_QUICK_REFERENCE

Compact lookup: effect name + constructor signature, organized by category.
For detailed descriptions, targeting info, and examples, see EFFECTS_INDEX.md.

**How to use:** Search this file for keywords from the card text (e.g. "graveyard", "life", "shuffle", "destroy"). Once you find a candidate effect, grep EFFECTS_INDEX.md for its name to get full details.

## Targeting rules (summary)

- Effects in SPELL slot with `canTargetPlayer()=true` force player targeting at cast time.
- Effects in SPELL slot with `canTargetPermanent()=true` force permanent targeting at cast time.
- ETB/triggered/saga slots: targeting declarations don't force spell-level targeting.
- `CostEffect` subtypes are excluded from targeting computation.
- Targeting is computed by `EffectResolution.needsTarget(card)` / `needsSpellCastTarget(card)`.
- Never call `setNeedsTarget`/`setNeedsSpellTarget` directly.

## Marker interfaces

- `CostEffect` — additional costs (sacrifice, discard, exile, counter removal, tap creature)
- `ManaProducingEffect` — mana abilities (CR 605.1a)

## Wrapper / modifier effects

Core wrappers (all take `CardEffect wrapped` as first/only effect arg):
- `MayEffect(CardEffect, String prompt)` — "you may"
- `MayPayManaEffect(String manaCost, CardEffect, String prompt)` — "you may pay {X}"
- `MetalcraftConditionalEffect(CardEffect)` — 3+ artifacts
- `MorbidConditionalEffect(CardEffect)` — creature died this turn
- `RaidConditionalEffect(CardEffect)` — attacked this turn
- `SubtypeConditionalEffect(CardSubtype, CardEffect)` — triggering creature has subtype
- `ControlsSubtypeConditionalEffect(CardSubtype, CardEffect)` — controls subtype
- `ControlsAnotherSubtypeConditionalEffect(CardSubtype, CardEffect)` — controls another subtype
- `ControllerLifeThresholdConditionalEffect(int, CardEffect)` — life >= N
- `ControllerTurnConditionalEffect(CardEffect)` — during your turn
- `ControlsPermanentConditionalEffect(PermanentPredicate, CardEffect)` — controls matching
- `ControllerGraveyardCardThresholdConditionalEffect(int, CardPredicate, CardEffect)` — graveyard threshold
- `EnteringCreatureMinPowerConditionalEffect(int, CardEffect)` — entering power >= N
- `EnteringCreatureMaxPowerConditionalEffect(int, CardEffect)` — entering power <= N

Replacement wrappers (pick between base/upgraded at resolution):
- `MetalcraftReplacementEffect(CardEffect base, CardEffect metalcraft)`
- `MorbidReplacementEffect(CardEffect base, CardEffect morbid)`
- `RaidReplacementEffect(CardEffect base, CardEffect raid)`
- `KickerReplacementEffect(CardEffect base, CardEffect kicked)`
- `ControlsSubtypeReplacementEffect(CardSubtype, CardEffect base, CardEffect upgraded)`

Other wrappers:
- `ChooseOneEffect(List<ChooseOneOption>)` — modal spell
- `FlipCoinWinEffect(CardEffect)` — coin flip
- `NthSpellCastTriggerEffect(int, List<CardEffect>)` — Nth spell trigger
- `NoSpellsCastLastTurnConditionalEffect(CardEffect)` — werewolf front
- `TwoOrMoreSpellsCastLastTurnConditionalEffect(CardEffect)` — werewolf back
- `CastFromHandConditionalEffect(CardEffect)` — cast from hand only
- `KickedConditionalEffect(CardEffect)` — kicked adds effect

See EFFECTS_INDEX.md for 20+ additional conditional wrappers (poison, blocker count, etc.)

## Damage

- `DealDamageToAnyTargetEffect(int damage, boolean cantRegenerate)` — any target
- `DealDamageEqualToSourcePowerToAnyTargetEffect()` — source power to any target
- `DealDamageEqualToSourceToughnessToTargetCreatureEffect()` — source toughness to creature
- `SourceFightsTargetCreatureEffect()` — source fights target
- `PackHuntEffect(CardSubtype)` — pack hunt
- `DealDamageToTargetAndTheirCreaturesEffect(int)` — player + their creatures
- `DealDamageToEachCreatureDamagedPlayerControlsEffect()` — damage to damaged player's creatures
- `DealDamageToTargetCreatureEffect(int)` or `(int, boolean unpreventable)` — target creature
- `DealDamageToTargetCreatureOrPlaneswalkerEffect(int)` — creature or planeswalker
- `DealDamageToTargetOpponentOrPlaneswalkerEffect(int)` — opponent or planeswalker
- `DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffect(int)` — all target controls
- `DealDamageToAllCreaturesTargetControlsEffect(int)` — creatures target controls
- `DealDamageToTargetPlayerEffect(int)` — target player
- `DealDamageToSecondaryTargetEffect(int)` — secondary target
- `DealDamageToTargetPlayerByHandSizeEffect()` — damage = hand size
- `MassDamageEffect(int)` or `(int, boolean, boolean, PermanentPredicate)` + overloads — mass damage
- `DealDamageToEachPlayerEffect(int)` — each player
- `DealDamageToAnyTargetAndGainLifeEffect(int damage, int lifeGain)` — damage + life gain
- `DealDamageToControllerEffect(int)` — self damage
- `DealDamageToTargetCreatureControllerEffect(int)` — target creature's controller
- `DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect(CardSubtype, boolean)` — damage = subtype count
- `DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect(CardSubtype, boolean)` — any target = subtype count
- `DealDamageToEachOpponentEffect(int)` — each opponent
- `DealOrderedDamageToAnyTargetsEffect(List<Integer>)` — ordered multi-target
- `DealXDamageToAnyTargetEffect()` or `(boolean exileInsteadOfDie)` — X damage any target
- `DealXDamageToAnyTargetAndGainXLifeEffect()` — X damage + X life
- `DealXDamageToTargetCreatureEffect()` — X damage creature
- `DealXDamageDividedAmongTargetAttackingCreaturesEffect()` — X divided among attackers
- `DealXDamageDividedAmongTargetCreaturesCantBlockEffect()` — X divided, can't block
- `DealXDamageDividedEvenlyAmongTargetsEffect()` — X divided evenly (Fireball-style)
- `DealXDamageToEachTargetEffect()` — X to each target
- `FirstTargetDealsPowerDamageToSecondTargetEffect()` — bite
- `FirstTargetFightsSecondTargetEffect()` — fight
- `MassFightTargetCreatureEffect()` — Alpha Brawl-style mass fight
- `DoubleDamageEffect()` — double all damage (static)
- `DoubleControllerDamageEffect(StackEntryPredicate, boolean)` — double controller's damage
- `DealDividedDamageAmongTargetCreaturesEffect(int)` — divided among creatures
- `DealDividedDamageAmongAnyTargetsEffect(int)` — divided among any targets
- `SacrificePermanentThenEffect(PermanentPredicate, CardEffect, String)` — sacrifice then effect
- `SpellCastTriggerEffect(CardPredicate, List<CardEffect>)` + overloads — spell cast trigger

See EFFECTS_INDEX.md "Damage" section for 15+ additional niche damage effects.

## Destruction / sacrifice

- `DestroyTargetPermanentEffect(boolean cantRegen)` or `(boolean, CreateTokenEffect)` — destroy target
- `DestroyTargetPermanentAtEndStepEffect()` — destroy at end step
- `DestroyAllPermanentsEffect(PermanentPredicate)` or `(PermanentPredicate, boolean)` — board wipe
- `DestroyAllPermanentsAndGainLifePerDestroyedEffect(PermanentPredicate, int)` — wipe + life
- `EachPlayerChoosesCreatureDestroyRestEffect()` — choose one, destroy rest
- `DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect(String, List, Set)` — wipe + X/X token
- `DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect(CardPredicate, boolean)` — destroy + search
- `DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect()` — destroy + each searches
- `EachOpponentMaySearchLibraryForBasicLandToBattlefieldTappedEffect()` — opponents search
- `DestroyTargetLandAndDamageControllerEffect(int)` — destroy land + damage
- `DestroyTargetPermanentAndDamageControllerIfDestroyedEffect(int)` — destroy + conditional damage
- `DestroyTargetPermanentAndControllerLosesLifeEffect(int)` — destroy + life loss
- `DestroyTargetPermanentAndGiveControllerPoisonCountersEffect(int)` — destroy + poison
- `DestroySourcePermanentEffect()` — destroy source
- `DestroyCreatureBlockingThisEffect()` — destroy blocker
- `DestroyTargetPermanentAndGainLifeEqualToManaValueEffect()` — destroy + life = MV
- `SacrificeCreatureEffect()` — target player sacrifices creature
- `SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect()` — sacrifice + life = toughness
- `ControllerSacrificesCreatureEffect()` — controller sacrifices (non-targeting)
- `SacrificeAttackingCreaturesEffect(int base, int metalcraft)` — sacrifice attackers
- `EachOpponentSacrificesCreatureEffect()` — each opponent sacrifices
- `EachOpponentSacrificesPermanentsEffect(int, PermanentPredicate)` — opponents sacrifice permanents
- `TargetPlayerSacrificesPermanentsEffect(int, PermanentPredicate)` — target sacrifices permanents
- `EachPlayerSacrificesPermanentsEffect(int, PermanentPredicate)` — each player sacrifices
- `EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(int, CardPredicate)` — mass reanimate
- `SacrificeSelfEffect()` — sacrifice self
- `SacrificeUnlessDiscardCardTypeEffect(CardType)` — sacrifice unless discard
- `SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType)` — sacrifice unless bounce own
- `SacrificeSelfAndDrawCardsEffect(int)` — sacrifice + draw
- `SacrificeAtEndOfCombatEffect()` — sacrifice at EOC
- `SacrificeTargetThenRevealUntilTypeToBattlefieldEffect(Set<CardType>)` — Polymorph

See EFFECTS_INDEX.md "Destruction" section for 10+ additional niche destruction/sacrifice effects.

### Sacrifice costs

- `ExileSelfCost()` — exile self as cost
- `SacrificeSelfCost()` — sacrifice self as cost
- `SacrificeCreatureCost()` or `(boolean trackMV)` or `(boolean trackMV, boolean trackPower)` or `(boolean, boolean, boolean trackToughness)` or `(boolean, boolean, boolean, boolean excludeSelf)` — sacrifice creature
- `SacrificeSubtypeCreatureCost(CardSubtype)` — sacrifice subtype creature
- `SacrificeArtifactCost()` — sacrifice artifact
- `SacrificePermanentCost(PermanentPredicate, String)` — sacrifice matching permanent
- `DiscardCardTypeCost(CardPredicate, String)` — discard matching card
- `RemoveCounterFromSourceCost(int, CounterType)` — remove counters from self
- `CrewCost(int)` — crew
- `TapCreatureCost(PermanentPredicate)` — tap creature
- `PayLifeCost(int)` — pay life
- `ExileCardFromGraveyardCost(CardType)` + overloads — exile graveyard card

See EFFECTS_INDEX.md "Sacrifice costs" for additional cost effects.

## Counter spells

- `CounterSpellEffect()` — counter target spell
- `CounterSpellAndCreateTreasureTokensEffect()` — counter + treasures
- `CounterSpellAndExileEffect()` — counter + exile
- `CounterSpellIfControllerPoisonedEffect()` — counter if poisoned
- `TargetSpellControllerLosesLifeEffect(int)` — target spell controller loses life
- `TargetSpellControllerDiscardsEffect(int)` — target spell controller discards
- `CounterUnlessPaysEffect(int)` or `(int, boolean useX, boolean exileIfCountered)` — counter unless pays
- `CantBeCounteredEffect()` — can't be countered (static)
- `CreatureSpellsCantBeCounteredEffect()` — creatures can't be countered (static)
- `CreatureEnteringDontCauseTriggersEffect()` — Torpor Orb (static)
- `ETBDoubleTriggerEffect(CardPredicate)` — double ETB triggers (static)
- `CreaturesEnterAsCopyOfSourceEffect()` — Essence of the Wild (static)
- `ExileOpponentCardsInsteadOfGraveyardEffect()` — Leyline of the Void (static)

## Bounce / return to hand

- `ReturnTargetPermanentToHandEffect()` or `(int lifeLoss)` — bounce target
- `ReturnTargetPermanentToHandWithManaValueConditionalEffect(int, CardEffect)` — bounce + MV bonus
- `ReturnCreaturesToOwnersHandEffect(Set<TargetFilter>)` — mass bounce creatures
- `ReturnSelfToHandEffect()` — bounce self
- `ReturnSelfToHandOnCoinFlipLossEffect()` — bounce self on coin flip loss
- `ReturnPermanentsOnCombatDamageToPlayerEffect()` or `(PermanentPredicate)` — Ninja-style
- `ReturnArtifactsTargetPlayerOwnsToHandEffect()` — bounce target's artifacts
- `ReturnPermanentsTargetPlayerControlsToHandEffect(PermanentPredicate)` — bounce target's permanents
- `PutTargetOnBottomOfLibraryEffect()` — tuck bottom
- `PutTargetOnTopOfLibraryEffect()` — tuck top
- `PutTargetPermanentIntoLibraryNFromTopEffect(int)` — tuck N from top

## Graveyard return

- `ReturnCardFromGraveyardEffect.builder().destination(HAND|BATTLEFIELD|TOP_OF_OWNERS_LIBRARY)...build()` — unified graveyard return (see EFFECTS_INDEX.md for full builder API)
- `ReturnOneOfEachSubtypeFromGraveyardToHandEffect(List<CardSubtype>)` — one of each subtype
- `PutTargetCardsFromGraveyardOnTopOfLibraryEffect(CardPredicate)` — graveyard to top of library
- `ReturnTargetCardsFromGraveyardToHandEffect(CardPredicate, int)` — up to N cards to hand
- `ShuffleTargetCardsFromGraveyardIntoLibraryEffect(CardPredicate, int)` — target player shuffles N cards
- `ReturnDyingCreatureToBattlefieldAndAttachSourceEffect()` — reanimate + equip
- `PutCardFromOpponentGraveyardOntoBattlefieldEffect(boolean tapped)` — opponent's card to battlefield
- `PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect()` — opponent's creature with exile

## Draw / discard / hand manipulation

- `DrawCardEffect(int)` — draw N
- `EachPlayerDrawsCardEffect(int)` — each player draws N
- `DrawCardForTargetPlayerEffect(int)` or `(int, boolean requiresUntapped, boolean targets)` — target draws
- `DrawXCardsEffect()` — draw X
- `DrawXCardsForTargetPlayerEffect()` — target draws X
- `DrawAndDiscardCardEffect(int draw, int discard)` — loot
- `DiscardAndDrawCardEffect(int discard, int draw)` — rummage
- `DiscardCardEffect(int)` — discard N
- `DiscardOwnHandEffect()` — discard entire hand
- `EachPlayerDiscardsEffect(int)` — each player discards
- `EachOpponentDiscardsEffect(int)` — each opponent discards
- `TargetPlayerDiscardsEffect(int)` — target discards
- `ChooseCardFromTargetHandToDiscardEffect(int, List<CardType>)` — choose from hand to discard
- `ChooseCardFromTargetHandToExileEffect(int, List<CardType>)` — choose from hand to exile
- `LookAtHandEffect()` — look at hand
- `ShuffleHandIntoLibraryAndDrawEffect()` — wheel
- `EachPlayerShufflesHandAndGraveyardIntoLibraryEffect()` — Timetwister-style

## Library manipulation

- `SearchLibraryForCardToHandEffect()` — any card to hand
- `SearchLibraryForBasicLandToHandEffect()` — basic land to hand
- `SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect()` — Cultivate
- `SearchLibraryForCardTypesToHandEffect(CardPredicate)` — filtered to hand
- `SearchLibraryForCardTypesToBattlefieldEffect(CardPredicate, boolean tapped)` or `(CardPredicate, boolean, int max)` — filtered to battlefield
- `SearchLibraryForCreatureWithMVXOrLessToHandEffect()` — creature MV<=X to hand
- `SearchLibraryForCreatureWithSubtypeToBattlefieldEffect(CardSubtype)` — creature subtype to battlefield
- `SearchLibraryForCreatureWithExactMVToBattlefieldEffect(int mvOffset)` — Birthing Pod
- `SearchLibraryForCardToTopOfLibraryEffect()` — any card to top
- `SearchLibraryForCreatureToTopOfLibraryEffect()` — creature to top
- `SearchTargetLibraryForCardsToGraveyardEffect(int, Set<CardType>)` — target library to graveyard
- `RevealTopCardOfLibraryEffect()` or overloads — reveal top card
- `RevealTopCardsAndSeparateEffect(int)` — reveal + separate into piles
- `ScryEffect(int)` — scry N
- `SurveilEffect(int)` — surveil N
- `ShuffleLibraryEffect()` — shuffle library
- `ShuffleIntoLibraryEffect()` — shuffle spell into library
- `ShuffleSelfAndGraveyardIntoLibraryEffect()` — shuffle self + graveyard into library
- `ShuffleGraveyardIntoLibraryEffect(boolean targetPlayer)` — shuffle graveyard into library (targetPlayer=true targets, false=controller's)
- `ShuffleTargetCardsFromGraveyardIntoLibraryEffect(CardPredicate, int)` — shuffle N cards from graveyard
- `CastTopOfLibraryWithoutPayingManaCostEffect(Set<CardType>)` — cast top free
- `RevealTopCardMayPlayFreeOrExileEffect()` — reveal top, play free or exile

## Mill

- `MillControllerEffect(int)` — self-mill N
- `MillTargetPlayerEffect(int)` — mill target N
- `MillTargetPlayerByChargeCountersEffect()` — mill = charge counters
- `MillHalfLibraryEffect()` — mill half
- `MillEachPlayerEffect(int)` — mill each player N
- `MillEachOpponentEffect(int)` — mill each opponent N

## Exile

- `ExileTargetPermanentEffect()` or `(boolean returnEndStep)` — exile target
- `ExileTargetPermanentAndReturnAtEndStepEffect(boolean tapped)` — exile + return at end step
- `ExileTargetPlayerGraveyardEffect()` — exile target's graveyard
- `ExileAllCreaturesEffect()` — exile all creatures
- `ExileAllPermanentsEffect(PermanentPredicate)` — exile matching permanents
- `ReturnTargetCardFromExileToHandEffect(CardPredicate, boolean ownedOnly)` — exile to hand

## Tokens

- `CreateTokenEffect(...)` — create tokens (many constructors, see EFFECTS_INDEX.md)
- `CreateTokenEffect.whiteSpirit(int)` — 1/1 white Spirit creature token with flying
- `CreateTokenEffect.blackZombie(int)` — 2/2 black Zombie creature token
- `CreateTokenEffect.whiteSoldier(int)` — 1/1 white Soldier creature token
- `CreateTokenEffect.ofTreasureToken(int)` — treasure tokens
- `CreateTokenForEachControlledPermanentEffect(PermanentPredicate, ...)` — token per permanent
- `CreateTokenForEachOpponentCreatureEffect(...)` — token per opponent creature

## Life

- `GainLifeEffect(int)` — gain N life
- `GainLifeForEachSubtypeOnBattlefieldEffect(CardSubtype)` — life per subtype
- `GainLifePerControlledCreatureEffect()` — life per creature
- `GainLifePerControlledMatchingPermanentEffect(List<PermanentPredicate>)` — life per matching
- `GainLifePerCreatureOnBattlefieldEffect()` — life per all creatures
- `GainLifePerMatchingPermanentOnBattlefieldEffect(List<PermanentPredicate>)` — life per matching (all)
- `GainLifePerCardsInHandEffect()` — life per hand card
- `GainLifePerCreatureCardInGraveyardEffect(int)` — N life per creature in graveyard
- `GainLifePerGraveyardCardEffect(int lifePerCard)` — N life per card in graveyard
- `GainLifeEqualToTargetToughnessEffect()` — life = target toughness
- `GainLifeEqualToXValueEffect()` — life = X
- `GainLifeMultipliedByXValueEffect(int)` — life = X * multiplier
- `TargetPlayerGainsLifeEffect(int)` — target gains life
- `DoubleTargetPlayerLifeEffect()` — double target life
- `SetTargetPlayerLifeToSpecificValueEffect(int)` — set life to value
- `LoseLifeEffect(int)` — lose N life
- `EachOpponentLosesLifeEffect(int)` — each opponent loses
- `EachOpponentLosesLifeAndControllerGainsLifeLostEffect(int)` — drain each opponent
- `TargetPlayerLosesLifeEffect(int)` — target loses
- `TargetPlayerLosesLifeAndControllerGainsLifeEffect(int, int)` — drain target
- `EachPlayerLosesLifeEffect(int)` — each player loses
- `PlayersCantGainLifeEffect()` — can't gain life (static)

## Poison counters

- `GiveControllerPoisonCountersEffect(int)` — self poison
- `GiveEachPlayerPoisonCountersEffect(int)` — each player poison
- `GiveTargetPlayerPoisonCountersEffect(int)` — target poison

## Creature pump / boost

- `BoostTargetCreatureEffect(int power, int toughness)` — target +X/+Y
- `BoostSelfEffect(int, int)` — self +X/+Y
- `DoubleSelfPowerToughnessEffect()` — double self P/T
- `BoostAllOwnCreaturesEffect(int, int)` or `(int, int, PermanentPredicate)` — all own +X/+Y
- `BoostAllCreaturesEffect(int, int)` or `(int, int, PermanentPredicate)` — all creatures +X/+Y
- `StaticBoostEffect(int, int, Set<Keyword>, GrantScope, PermanentPredicate)` — static +X/+Y + keywords
- `SetBasePowerToughnessUntilEndOfTurnEffect(int, int)` — set base P/T
- `SwitchPowerToughnessEffect()` — switch P/T

## P/T setting / counters

- `PowerToughnessEqualToControlledLandCountEffect()` — P/T = lands
- `PowerToughnessEqualToControlledCreatureCountEffect()` — P/T = creatures
- `PowerToughnessEqualToCardsInHandEffect()` — P/T = hand size
- `PowerToughnessEqualToControllerLifeTotalEffect()` — P/T = life total
- `PutCountersOnSourceEffect(int power, int toughness, int amount)` — counters on self
- `PutPlusOnePlusOneCounterOnTargetCreatureEffect(int)` — +1/+1 on target
- `PutMinusOneMinusOneCounterOnTargetCreatureEffect(int)` — -1/-1 on target
- `PutPlusOnePlusOneCounterOnEachOwnCreatureEffect()` or `(int)` — +1/+1 on each own creature
- `EnterWithXChargeCountersEffect()` — ETB X charge counters
- `EnterWithXPlusOnePlusOneCountersEffect()` — ETB X +1/+1 counters
- `EnterWithFixedChargeCountersEffect(int)` — ETB fixed charge counters
- `PutChargeCounterOnSelfEffect()` — charge counter on self
- `ProliferateEffect()` — proliferate
- `KickerEffect(String cost)` — kicker declaration

## Keywords / abilities

- `GrantKeywordEffect(Keyword, GrantScope)` or `(Keyword, GrantScope, PermanentPredicate)` or `(Set<Keyword>, GrantScope)` — grant keywords
- `GrantFlashToCardTypeEffect(CardPredicate)` — flash to card types (static)
- `GrantActivatedAbilityEffect(ActivatedAbility, GrantScope)` or `(ActivatedAbility, GrantScope, PermanentPredicate)` — grant ability
- `GrantAdditionalBlockEffect(int)` — block N additional
- `RegenerateEffect()` or `(boolean targetsPermanent)` — regenerate
- `ProtectionFromColorsEffect(Set<CardColor>)` — protection from colors (static)
- `ProtectionFromSubtypesEffect(Set<CardSubtype>)` — protection from subtypes (static)

## Combat restrictions / evasion

- `CantBeBlockedEffect()` — unblockable (static)
- `CantBlockEffect()` — can't block (static)
- `MustAttackEffect()` — must attack (static)
- `MustBeBlockedIfAbleEffect()` — must be blocked (static)
- `MustBeBlockedByAllCreaturesEffect()` — Lure (static)
- `EnchantedCreatureCantAttackOrBlockEffect()` — Pacifism (static)
- `MakeCreatureUnblockableEffect()` — target unblockable this turn

## Tap / untap

- `TapTargetPermanentEffect()` — tap target
- `UntapTargetPermanentEffect(PermanentPredicate)` — untap target
- `TapSelfEffect()` — tap self
- `UntapSelfEffect()` — untap self
- `DoesntUntapDuringUntapStepEffect()` — doesn't untap (static)
- `UntapAllControlledPermanentsEffect(PermanentPredicate)` — untap all matching

## Control / steal

- `GainControlOfTargetPermanentEffect()` or `(CardSubtype)` — gain control permanently
- `GainControlOfTargetPermanentUntilEndOfTurnEffect()` — gain control until EOT
- `GainControlOfTargetPermanentWhileSourceEffect()` — control while source on battlefield
- `GainControlOfEnchantedTargetEffect()` — Control Magic (static)

## Mana

- `AwardManaEffect(ManaColor, int)` or `(ManaColor)` — add mana
- `AwardAnyColorManaEffect(int)` or `()` — add any color mana
- `AddManaPerControlledPermanentEffect(ManaColor, PermanentPredicate, String)` — mana per permanent
- `DoubleManaPoolEffect()` — double mana pool
- `AwardRestrictedManaEffect(ManaColor, int, Set<CardType>)` — restricted mana
- `AwardFlashbackOnlyAnyColorManaEffect(int)` — flashback-only mana

## Copy / clone

- `CopyPermanentOnEnterEffect(PermanentPredicate, String)` + overloads — Clone-style
- `CopySpellEffect()` or `(StackEntryPredicate)` — copy target spell
- `ChangeTargetOfTargetSpellWithSingleTargetEffect()` — redirect spell
- `ChooseNewTargetsForTargetSpellEffect()` — choose new targets

## Turn / phase

- `ControllerExtraTurnEffect(int)` — extra turns (non-targeting)
- `ExtraTurnEffect(int)` — target extra turns
- `AdditionalCombatMainPhaseEffect(int)` — additional combat phases
- `EndTurnEffect()` — end the turn

## Animate / transform

- `AnimateLandEffect(int, int, List<CardSubtype>, Set<Keyword>, CardColor)` + overloads — animate land
- `AnimateSelfAsCreatureEffect()` — vehicle crew
- `TransformSelfEffect()` — transform DFC
- `TransformAllEffect(PermanentPredicate)` — transform all matching

## Static restrictions / taxes

- `EntersTappedEffect()` — enters tapped
- `NoMaximumHandSizeEffect()` — no max hand size (static)
- `IncreaseOpponentCastCostEffect(Set<CardType>, int)` — opponents' spells cost more
- `ReduceOwnCastCostForCardTypeEffect(Set<CardType>, int)` — own spells cost less
- `LimitSpellsPerTurnEffect(int)` — max spells per turn
- `CantSearchLibrariesEffect()` — can't search (static)
- `AlternativeCostForSpellsEffect(String, CardPredicate)` — alternative cast cost

## Choose / name

- `ChooseCardNameOnEnterEffect()` — choose card name ETB
- `ChooseColorOnEnterEffect()` — choose color ETB
- `ChooseSubtypeOnEnterEffect()` — choose creature type ETB

## Provider map

| Category | Service |
|----------|---------|
| Damage | `combat.DamageResolutionService` |
| Destruction | `DestructionResolutionService` |
| Bounce | `battlefield.BounceResolutionService` |
| Counter | `CounterResolutionService` |
| Library/search/mill | `LibraryResolutionService` |
| Graveyard/exile | `GraveyardReturnResolutionService` |
| Draw/discard | `effect/PlayerInteractionResolutionService` |
| Life | `effect/LifeResolutionService` |
| Creature mods | `effect/CreatureModResolutionService` |
| Permanent control/tokens | `effect/PermanentControlResolutionService` |
| Static effects | `effect/StaticEffectResolutionService` |
| Prevention | `PreventionResolutionService` |
| Turn | `TurnResolutionService` |
| Copy/retarget | `CopyResolutionService`, `TargetRedirectionResolutionService` |
| Exile | `ExileResolutionService` |
| Win conditions | `effect/WinConditionResolutionService` |
