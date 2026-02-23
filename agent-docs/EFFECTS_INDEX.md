# EFFECTS_INDEX

Purpose: cut token usage when implementing cards by quickly mapping "card text intent" to existing reusable effects and their constructor signatures.

## How to use this index

1. Parse card text into primitive actions (damage, draw, bounce, etc.).
2. Find each primitive below in the categorized sections and reuse existing effects.
3. Only add new effect records when no existing effect can express the behavior.
4. If you add a new effect record, add an `@HandlesEffect`-annotated resolver method in the matching `*ResolutionService` (see provider map at bottom). No manual registration needed — the annotation auto-registers the handler at startup.
5. If your new effect targets something, override the appropriate `canTarget*()` method(s) on `CardEffect` to return `true` (see targeting section below).

## Effect targeting declarations

Effects declare what they can target via default methods on `CardEffect`. `Card.isNeedsTarget()` and `Card.isNeedsSpellTarget()` are derived automatically — never call `setNeedsTarget`/`setNeedsSpellTarget`.

When creating a new effect, override the relevant method(s) to return `true`:

| Method | Returns `true` on these effects |
|--------|---------------------------------|
| `canTargetPlayer()` | DealDamageToAnyTargetEffect, DealDamageToAnyTargetAndGainLifeEffect, DealDamageToTargetPlayerEffect, DealDamageToTargetPlayerByHandSizeEffect, DealOrderedDamageToAnyTargetsEffect, DealXDamageToAnyTargetEffect, DealXDamageToAnyTargetAndGainXLifeEffect, TargetPlayerLosesLifeAndControllerGainsLifeEffect, TargetPlayerGainsLifeEffect, DoubleTargetPlayerLifeEffect, TargetPlayerDiscardsEffect, ChooseCardFromTargetHandToDiscardEffect, ChooseCardsFromTargetHandToTopOfLibraryEffect, LookAtHandEffect, HeadGamesEffect, RedirectDrawsEffect, MillTargetPlayerEffect, MillHalfLibraryEffect, ExtraTurnEffect, SacrificeCreatureEffect, ShuffleGraveyardIntoLibraryEffect, RevealTopCardOfLibraryEffect, ReturnArtifactsTargetPlayerOwnsToHandEffect, TargetPlayerGainsControlOfSourceCreatureEffect |
| `canTargetPermanent()` | DealDamageToAnyTargetEffect, DealDamageToAnyTargetAndGainLifeEffect, DealDamageToTargetCreatureEffect, DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect, DealOrderedDamageToAnyTargetsEffect, DealXDamageToAnyTargetEffect, DealXDamageToAnyTargetAndGainXLifeEffect, DealXDamageToTargetCreatureEffect, DealXDamageDividedAmongTargetAttackingCreaturesEffect, FirstTargetDealsPowerDamageToSecondTargetEffect, DestroyTargetPermanentEffect, DestroyTargetLandAndDamageControllerEffect, DestroyCreatureBlockingThisEffect, ExileTargetPermanentEffect, ReturnTargetPermanentToHandEffect, PutTargetOnBottomOfLibraryEffect, GainControlOfTargetCreatureUntilEndOfTurnEffect, GainControlOfEnchantedTargetEffect, GainControlOfTargetAuraEffect, BoostTargetCreatureEffect, BoostFirstTargetCreatureEffect, GainLifeEqualToTargetToughnessEffect, PreventDamageToTargetEffect, TapTargetPermanentEffect, TapOrUntapTargetPermanentEffect, UntapTargetPermanentEffect, MakeTargetUnblockableEffect, TargetCreatureCantBlockThisTurnEffect, ChangeColorTextEffect, EquipEffect, CantBlockSourceEffect, SacrificeCreatureCost, GrantKeywordEffect (when scope == Scope.TARGET) |
| `canTargetSpell()` | CounterSpellEffect, CounterUnlessPaysEffect, CopySpellEffect, ChangeTargetOfTargetSpellWithSingleTargetEffect |
| `canTargetGraveyard()` | ReturnCardFromGraveyardToHandEffect, ReturnCreatureFromGraveyardToHandEffect |

Effects that target both players and permanents (any-target): DealDamageToAnyTargetEffect, DealDamageToAnyTargetAndGainLifeEffect, DealOrderedDamageToAnyTargetsEffect, DealXDamageToAnyTargetEffect, DealXDamageToAnyTargetAndGainXLifeEffect.

## Wrapper / modifier effects

| Effect | Constructor | Description |
|--------|-------------|-------------|
| `MayEffect` | `(CardEffect wrapped, String prompt)` | Wraps any effect with "you may" choice |

---

## Damage

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `DealDamageToAnyTargetEffect` | `(int damage, boolean cantRegenerate)` | deal N damage to any target |
| `DealDamageToTargetCreatureEffect` | `(int damage)` | deal N damage to target creature |
| `DealDamageToTargetPlayerEffect` | `(int damage)` | deal N damage to target player |
| `DealDamageToTargetPlayerByHandSizeEffect` | `()` | deal damage equal to hand size to target player |
| `DealDamageToAllCreaturesEffect` | `(int damage)` | deal N damage to all creatures |
| `DealDamageToAllCreaturesAndPlayersEffect` | `(int damage)` | deal N damage to all creatures and each player |
| `DealDamageToAnyTargetAndGainLifeEffect` | `(int damage, int lifeGain)` | deal N damage and gain M life |
| `DealDamageToControllerEffect` | `(int damage)` | deal N damage to the card's controller (pain lands, self-damage) |
| `DealDamageToDiscardingPlayerEffect` | `(int damage)` | deal N damage to any player who discards (trigger) |
| `DealDamageToFlyingAndPlayersEffect` | `()` | deal damage to all flying creatures and all players (e.g. Hurricane-style; uses X) |
| `DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect` | `(CardSubtype subtype)` | deal damage to target creature equal to number of controlled permanents of subtype |
| `DealDamageIfFewCardsInHandEffect` | `(int maxCards, int damage)` | deal N damage to target player if they have maxCards or fewer in hand |
| `DealDamageOnLandTapEffect` | `(int damage)` | deal N damage to a player whenever they tap a land (Manabarbs-style) |
| `DealOrderedDamageToAnyTargetsEffect` | `(List<Integer> damageAmounts)` | deal different amounts to multiple targets (e.g. 3 then 1) |
| `DealXDamageToAnyTargetEffect` | `()` | deal X damage to any target (X spell) |
| `DealXDamageToAnyTargetAndGainXLifeEffect` | `()` | deal X damage and gain X life (X spell) |
| `DealXDamageToTargetCreatureEffect` | `()` | deal X damage to target creature (X spell) |
| `DealXDamageDividedAmongTargetAttackingCreaturesEffect` | `()` | deal X damage divided among attacking creatures |
| `FirstTargetDealsPowerDamageToSecondTargetEffect` | `()` | first target creature deals damage equal to its power to second target creature (bite mechanic) |
| `DoubleDamageEffect` | `()` | double all damage dealt (static) |
| `SacrificeOtherCreatureOrDamageEffect` | `(int damage)` | sacrifice another creature or take N damage (upkeep trigger) |

## Destruction / sacrifice

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `DestroyTargetPermanentEffect` | `(boolean cannotBeRegenerated)` | destroy target permanent |
| `DestroyAllPermanentsEffect` | `(Set<CardType> targetTypes, boolean onlyOpponents, boolean cannotBeRegenerated)` | destroy all permanents of given types |
| `DestroyTargetLandAndDamageControllerEffect` | `(int damage)` | destroy target land and deal N to its controller |
| `DestroyBlockedCreatureAndSelfEffect` | `()` | destroy creature this blocks and itself (Deathtrap-style) |
| `DestroyCreatureBlockingThisEffect` | `()` | destroy creature that blocks this (combat trigger) |
| `SacrificeCreatureEffect` | `()` | controller sacrifices a creature |
| `EachOpponentSacrificesCreatureEffect` | `()` | each opponent sacrifices a creature |
| `SacrificeSelfEffect` | `()` | sacrifice this permanent |
| `SacrificeUnlessDiscardCardTypeEffect` | `(CardType requiredType)` | sacrifice unless you discard a card of type (null = any) |
| `SacrificeAtEndOfCombatEffect` | `()` | sacrifice at end of combat |

### Sacrifice costs (for activated abilities)

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `SacrificeSelfCost` | `()` | sacrifice this permanent as cost |
| `SacrificeCreatureCost` | `()` | sacrifice a creature as cost |
| `SacrificeSubtypeCreatureCost` | `(CardSubtype subtype)` | sacrifice a creature of specific subtype as cost |
| `SacrificeAllCreaturesYouControlCost` | `()` | sacrifice all creatures you control as cost |
| `DiscardCardTypeCost` | `(CardType requiredType)` | discard a card of specific type as cost |
| `RemoveCounterFromSourceCost` | `()` | remove a counter from this permanent as cost (prefers -1/-1, then +1/+1) |

## Counter spells

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CounterSpellEffect` | `()` | counter target spell |
| `CounterUnlessPaysEffect` | `(int amount)` | counter unless controller pays N generic mana |
| `CreatureSpellsCantBeCounteredEffect` | `()` | creature spells can't be countered (static) |

## Bounce / return to hand

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ReturnTargetPermanentToHandEffect` | `()` | return target permanent to owner's hand |
| `ReturnCreaturesToOwnersHandEffect` | `(Set<TargetFilter> filters)` | return all creatures matching filters to owners' hands |
| `ReturnSelfToHandEffect` | `()` | return this permanent to owner's hand |
| `ReturnSelfToHandOnCoinFlipLossEffect` | `()` | return self to hand if coin flip is lost |
| `ReturnPermanentsOnCombatDamageToPlayerEffect` | `()` | return permanents when combat damage dealt to player (Ninja-style) |
| `ReturnArtifactsTargetPlayerOwnsToHandEffect` | `()` | return all artifacts target player owns to hand |
| `BounceCreatureOnUpkeepEffect` | `(Scope scope, Set<TargetFilter> filters, String prompt)` | at upkeep, return a creature matching filters. Scope: `SOURCE_CONTROLLER`, `TRIGGER_TARGET_PLAYER` |
| `PutTargetOnBottomOfLibraryEffect` | `()` | put target permanent on bottom of owner's library |

## Graveyard return

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ReturnCardFromGraveyardToHandEffect` | `()` | return target card from your graveyard to hand |
| `ReturnCreatureFromGraveyardToHandEffect` | `()` | return target creature card from your graveyard to hand |
| `ReturnCreatureFromGraveyardToBattlefieldEffect` | `()` | return target creature card from graveyard to battlefield |
| `ReturnArtifactFromGraveyardToHandEffect` | `()` | return target artifact card from graveyard to hand |
| `ReturnArtifactOrCreatureFromAnyGraveyardToBattlefieldEffect` | `()` | return target artifact or creature from ANY graveyard to battlefield |
| `ReturnAuraFromGraveyardToBattlefieldEffect` | `()` | return aura from graveyard to battlefield |
| `ReturnCardOfSubtypeFromGraveyardToHandEffect` | `(CardSubtype subtype)` | return card of specific subtype from graveyard to hand |
| `ReturnSelfFromGraveyardToHandEffect` | `()` | return this card from graveyard to owner's hand |
| `ReturnCreatureCardsPutIntoYourGraveyardFromBattlefieldThisTurnToHandEffect` | `()` | return creature cards that died this turn to hand |

## Draw / discard / hand manipulation

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `DrawCardEffect` | `(int amount)` | draw N cards |
| `DrawCardForTargetPlayerEffect` | `(int amount, boolean requireSourceUntapped)` | target player draws N cards; optionally requires source untapped |
| `DrawAndLoseLifePerSubtypeEffect` | `(CardSubtype subtype)` | draw cards and lose life for each permanent of subtype you control |
| `DiscardCardEffect` | `(int amount)` | discard N cards |
| `TargetPlayerDiscardsEffect` | `(int amount)` | target player discards N cards |
| `RandomDiscardEffect` | `(int amount)` | discard N cards at random |
| `ChooseCardFromTargetHandToDiscardEffect` | `(int count, List<CardType> excludedTypes)` | choose N cards from target's hand to discard (excludedTypes can't be chosen) |
| `ChooseCardsFromTargetHandToTopOfLibraryEffect` | `(int count)` | choose N cards from target hand to put on top of library |
| `LookAtHandEffect` | `()` | look at target player's hand |
| `RevealOpponentHandsEffect` | `()` | reveal all opponents' hands |
| `HeadGamesEffect` | `()` | exchange target player's hand with cards from your library (Head Games) |
| `RedirectDrawsEffect` | `()` | redirect opponent's draws to controller (static, e.g. Plagiarize-style) |

## Library manipulation

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `SearchLibraryForCardToHandEffect` | `()` | search library for any card to hand |
| `SearchLibraryForBasicLandToHandEffect` | `()` | search library for basic land to hand |
| `SearchLibraryForCardTypesToHandEffect` | `(Set<CardType> cardTypes)` | search library for card of specific types to hand |
| `SearchLibraryForCardTypesToBattlefieldEffect` | `(Set<CardType> cardTypes, boolean requiresBasicSupertype, boolean entersTapped)` | search library for card to battlefield |
| `SearchLibraryForCreatureWithMVXOrLessToHandEffect` | `()` | search library for creature with MV X or less to hand |
| `PayManaAndSearchLibraryForCardNamedToBattlefieldEffect` | `(String manaCost, String cardName)` | pay mana, search for named card to battlefield |
| `LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect` | `(int count, Set<CardType> cardTypes)` | look at top N, may reveal matching type to hand, rest on bottom |
| `LookAtTopCardsHandTopBottomEffect` | `(int count)` | look at top N cards, choose hand/top/bottom for each |
| `ReorderTopCardsOfLibraryEffect` | `(int count)` | reorder top N cards of library |
| `RevealTopCardOfLibraryEffect` | `()` | reveal top card of library (static/continuous) |
| `ShuffleIntoLibraryEffect` | `()` | shuffle this permanent into owner's library |
| `ShuffleGraveyardIntoLibraryEffect` | `()` | shuffle graveyard into library |

## Mill

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `MillTargetPlayerEffect` | `(int count)` | target player mills N cards |
| `MillHalfLibraryEffect` | `()` | target player mills half their library |
| `MillByHandSizeEffect` | `()` | target player mills cards equal to hand size |

## Exile

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ExileTargetPermanentEffect` | `()` | exile target permanent |
| `ExileCardsFromGraveyardEffect` | `(int maxTargets, int lifeGain)` | exile up to N cards from graveyard, gain lifeGain per card |
| `ExileCreaturesFromGraveyardAndCreateTokensEffect` | `()` | exile creature cards from graveyard, create tokens for each |
| `ExileTopCardsRepeatOnDuplicateEffect` | `(int count)` | exile top N cards, repeat if duplicate names found |

## Tokens

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CreateCreatureTokenEffect` | `(int amount, String tokenName, int power, int toughness, CardColor color, List<CardSubtype> subtypes, Set<Keyword> keywords, Set<CardType> additionalTypes)` | create N creature tokens |
| `CreateCreatureTokenWithColorsEffect` | `(String tokenName, int power, int toughness, Set<CardColor> colors, CardColor primaryColor, List<CardSubtype> subtypes)` | create multi-colored creature token |

## Life

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `GainLifeEffect` | `(int amount)` | gain N life |
| `GainLifePerGraveyardCardEffect` | `()` | gain life equal to cards in graveyard |
| `GainLifeEqualToTargetToughnessEffect` | `()` | gain life equal to target creature's toughness |
| `GainLifeEqualToToughnessEffect` | `()` | gain life equal to own toughness (self, e.g. dies trigger) |
| `GainLifeEqualToDamageDealtEffect` | `()` | gain life equal to damage dealt (lifelink-style, static) |
| `GainLifeOnColorSpellCastEffect` | `(CardColor triggerColor, int amount)` | gain N life when a spell of that color is cast (trigger, wrap in MayEffect) |
| `TargetPlayerGainsLifeEffect` | `(int amount)` | target player gains N life |
| `DoubleTargetPlayerLifeEffect` | `()` | double target player's life total |
| `LoseLifeEffect` | `(int amount)` | lose N life |
| `TargetPlayerLosesLifeAndControllerGainsLifeEffect` | `(int lifeLoss, int lifeGain)` | drain: target loses N, you gain M |
| `EnchantedCreatureControllerLosesLifeEffect` | `(int amount, UUID affectedPlayerId)` | enchanted creature's controller loses N life (trigger) |
| `EachPlayerLosesLifePerCreatureControlledEffect` | `(int lifePerCreature)` | each player loses N life per creature they control |

## Win / lose game

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `TargetPlayerLosesGameEffect` | `(UUID playerId)` | target player loses the game |
| `LoseGameIfNotCastFromHandEffect` | `()` | lose the game if not cast from hand (ETB check) |
| `WinGameIfCreaturesInGraveyardEffect` | `(int threshold)` | win if N+ creature cards in graveyard |
| `CantLoseGameEffect` | `()` | you can't lose and opponents can't win (static) |

## Creature pump / boost

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `BoostTargetCreatureEffect` | `(int powerBoost, int toughnessBoost)` | target creature gets +X/+Y until end of turn |
| `BoostSelfEffect` | `(int powerBoost, int toughnessBoost)` | this creature gets +X/+Y until end of turn |
| `BoostAllOwnCreaturesEffect` | `(int powerBoost, int toughnessBoost)` | all your creatures get +X/+Y (static) |
| `BoostOwnCreaturesEffect` | `(int powerBoost, int toughnessBoost)` | your creatures get +X/+Y until end of turn |
| `BoostAllCreaturesXEffect` | `(int powerMultiplier, int toughnessMultiplier)` | all creatures get +X/+X where X is mana paid |
| `BoostAttachedCreatureEffect` | `(int powerBoost, int toughnessBoost)` | enchanted/equipped creature gets +X/+Y (static, works for both auras and equipment) |
| `BoostCreaturesBySubtypeEffect` | `(Set<CardSubtype> affectedSubtypes, int powerBoost, int toughnessBoost, Set<Keyword> grantedKeywords)` | creatures of subtypes get +X/+Y and keywords (lord, static) |
| `BoostOtherCreaturesByColorEffect` | `(CardColor color, int powerBoost, int toughnessBoost)` | other creatures of color get +X/+Y (static) |
| `BoostNonColorCreaturesEffect` | `(CardColor excludedColor, int powerBoost, int toughnessBoost)` | non-[color] creatures get +X/+Y (static) |
| `BoostEnchantedCreaturePerControlledSubtypeEffect` | `(CardSubtype subtype, int powerPerSubtype, int toughnessPerSubtype)` | enchanted creature gets +X/+Y per controlled subtype |
| `BoostByOtherCreaturesWithSameNameEffect` | `(int powerPerCreature, int toughnessPerCreature)` | +X/+Y per other creature with same name (static) |
| `BoostBySharedCreatureTypeEffect` | `()` | +1/+1 for each other creature sharing a creature type (static) |
| `BoostFirstTargetCreatureEffect` | `(int powerBoost, int toughnessBoost)` | first target creature in multi-target spell gets +X/+Y until end of turn |
| `BoostSelfPerEnchantmentOnBattlefieldEffect` | `(int powerPerEnchantment, int toughnessPerEnchantment)` | +X/+Y per enchantment on battlefield (static) |
| `BoostSelfPerBlockingCreatureEffect` | `(int powerPerBlockingCreature, int toughnessPerBlockingCreature)` | +X/+Y for each creature blocking this (combat trigger) |

## P/T setting / counters

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PowerToughnessEqualToControlledLandCountEffect` | `()` | P/T = number of lands you control (static) |
| `PowerToughnessEqualToControlledCreatureCountEffect` | `()` | P/T = number of creatures you control (static) |
| `PowerToughnessEqualToControlledSubtypeCountEffect` | `(CardSubtype subtype)` | P/T = number of permanents of subtype you control (static) |
| `PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect` | `()` | P/T = number of creature cards in all graveyards (static) |
| `PutCountersOnSourceEffect` | `(int powerModifier, int toughnessModifier, int amount)` | put N counters on this creature (e.g. `(1,1,1)` for +1/+1, `(-1,-1,2)` for two -1/-1) |
| `PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect` | `(Set<CardColor> triggerColors, int amount, boolean onlyOwnSpells)` | put +1/+1 counters when spell of matching color is cast |

## Keywords / abilities

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `GrantKeywordEffect` | `(Keyword keyword, Scope scope)` | grant keyword. Scope: `SELF`, `TARGET`, `ENCHANTED_CREATURE`, `EQUIPPED_CREATURE`, `OWN_TAPPED_CREATURES`, `OWN_CREATURES` |
| `GrantActivatedAbilityToOwnLandsEffect` | `(ActivatedAbility ability)` | grant activated ability to all lands you control |
| `GrantActivatedAbilityToEnchantedCreatureEffect` | `(ActivatedAbility ability)` | grant activated ability to enchanted creature |
| `GrantAdditionalBlockEffect` | `(int additionalBlocks)` | can block N additional creatures |
| `RegenerateEffect` | `()` | regenerate target/self |

## Combat restrictions / evasion

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CantBeBlockedEffect` | `()` | can't be blocked (static) |
| `CantBlockEffect` | `()` | this creature can't block (static) |
| `CantBlockSourceEffect` | `(UUID sourcePermanentId)` | target creature can't block source permanent |
| `CanBeBlockedOnlyByFilterEffect` | `(PermanentPredicate blockerPredicate, String allowedBlockersDescription)` | can only be blocked by matching creatures (static) |
| `CanBeBlockedByAtMostNCreaturesEffect` | `(int maxBlockers)` | can be blocked by at most N creatures (static) |
| `CanBlockOnlyIfAttackerMatchesPredicateEffect` | `(PermanentPredicate attackerPredicate, String allowedAttackersDescription)` | this creature can only block attackers matching predicate (static) |
| `CantAttackUnlessDefenderControlsMatchingPermanentEffect` | `(PermanentPredicate defenderPermanentPredicate, String requirementDescription)` | can't attack unless defender controls matching permanent (static) |
| `MustAttackEffect` | `()` | this creature must attack each turn if able (static) |
| `MustBeBlockedByAllCreaturesEffect` | `()` | all creatures able to block this must do so (static, Lure-style) |
| `AssignCombatDamageAsThoughUnblockedEffect` | `()` | assign combat damage as though unblocked (static) |
| `AssignCombatDamageWithToughnessEffect` | `()` | assign combat damage using toughness instead of power (static) |
| `MakeTargetUnblockableEffect` | `()` | target creature is unblockable this turn |
| `TargetCreatureCantBlockThisTurnEffect` | `()` | target creature can't block this turn |
| `EnchantedCreatureCantAttackOrBlockEffect` | `()` | enchanted creature can't attack or block (static, Pacifism-style) |

## Tap / untap

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `TapTargetPermanentEffect` | `()` | tap target permanent |
| `TapOrUntapTargetPermanentEffect` | `()` | tap or untap target permanent |
| `UntapTargetPermanentEffect` | `()` | untap target permanent |
| `UntapSelfEffect` | `()` | untap this permanent |
| `UntapAttackedCreaturesEffect` | `()` | untap creatures that attacked this turn (end of combat) |
| `TapCreaturesEffect` | `(Set<TargetFilter> filters)` | tap all creatures matching filters |
| `DoesntUntapDuringUntapStepEffect` | `()` | this permanent doesn't untap during untap step (static) |
| `EnchantedCreatureDoesntUntapEffect` | `()` | enchanted creature doesn't untap during untap step (static) |
| `UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect` | `(TurnStep step)` | untap all your permanents during each other player's step |

## Control / steal

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `TargetPlayerGainsControlOfSourceCreatureEffect` | `()` | target opponent gains control of this creature (ETB) |
| `GainControlOfTargetCreatureUntilEndOfTurnEffect` | `()` | gain control of target creature until end of turn (Threaten-style) |
| `GainControlOfEnchantedTargetEffect` | `()` | gain control of enchanted permanent (static, Control Magic-style) |
| `GainControlOfTargetAuraEffect` | `()` | gain control of target aura |
| `ControlEnchantedCreatureEffect` | `()` | control enchanted creature (static) |

## Prevention / protection / redirection

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PreventDamageToTargetEffect` | `(int amount)` | prevent next N damage to target |
| `PreventNextDamageEffect` | `(int amount)` | prevent next N damage to target creature or player |
| `PreventAllCombatDamageEffect` | `()` | prevent all combat damage this turn |
| `PreventAllDamageEffect` | `()` | prevent all damage (e.g. Fog-style) |
| `PreventAllDamageToAndByEnchantedCreatureEffect` | `()` | prevent all damage to and dealt by enchanted creature |
| `PreventDamageFromColorsEffect` | `(Set<CardColor> colors)` | prevent all damage from sources of specified colors (static) |
| `PreventNextColorDamageToControllerEffect` | `(CardColor chosenColor)` | prevent next damage of chosen color to controller |
| `ProtectionFromColorsEffect` | `(Set<CardColor> colors)` | protection from specified colors (static) |
| `ProtectionFromChosenColorEffect` | `()` | protection from chosen color (static, requires ChooseColorOnEnterEffect) |
| `CantBeTargetedBySpellColorsEffect` | `(Set<CardColor> colors)` | can't be targeted by spells of specified colors (static) |
| `RedirectPlayerDamageToEnchantedCreatureEffect` | `()` | redirect damage dealt to player to enchanted creature |
| `RedirectUnblockedCombatDamageToSelfEffect` | `()` | redirect unblocked combat damage to this creature |
| `GrantControllerShroudEffect` | `()` | controller has shroud (can't be targeted) (static) |

## Mana

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `AwardManaEffect` | `(ManaColor color)` | add one mana of specified color |
| `AwardAnyColorManaEffect` | `()` | add one mana of any color |
| `AddManaOnEnchantedLandTapEffect` | `(ManaColor color, int amount)` | when enchanted land is tapped, add N mana of color |
| `DoubleManaPoolEffect` | `()` | double your mana pool |
| `PreventManaDrainEffect` | `()` | players don't lose unspent mana as steps/phases end (static) |

## Copy / clone

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CopyPermanentOnEnterEffect` | `(PermanentPredicate filter, String typeLabel)` | enter as copy of permanent matching filter (Clone-style) |
| `CopySpellEffect` | `()` | copy target spell |
| `ChangeTargetOfTargetSpellWithSingleTargetEffect` | `()` | change target of target spell with single target |

## Turn / phase

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ExtraTurnEffect` | `(int count)` | take N extra turns |
| `AdditionalCombatMainPhaseEffect` | `(int count)` | get N additional combat + main phases |
| `EndTurnEffect` | `()` | end the turn |

## Animate / transform

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `AnimateLandEffect` | `(int power, int toughness, List<CardSubtype> grantedSubtypes, Set<Keyword> grantedKeywords, CardColor animatedColor)` | land becomes creature until end of turn (manlands) |
| `AnimateSelfEffect` | `(List<CardSubtype> grantedSubtypes)` | this permanent becomes a creature (e.g. Mutavault-style) |
| `AnimateNoncreatureArtifactsEffect` | `()` | animate all noncreature artifacts into creatures (March of the Machines-style) |

## Enchantment-specific

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PutAuraFromHandOntoSelfEffect` | `()` | put an aura from hand onto this creature (ETB) |
| `ReturnAuraFromGraveyardToBattlefieldEffect` | `()` | return aura from graveyard to battlefield |
| `GainControlOfTargetAuraEffect` | `()` | gain control of target aura |
| `ChangeColorTextEffect` | `()` | change color words in enchanted permanent's text (Sleight of Mind-style) |

## Equipment

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `EquipEffect` | `()` | equip to target creature |
| `BoostAttachedCreatureEffect` | `(int powerBoost, int toughnessBoost)` | equipped creature gets +X/+Y (static) |

## Static restrictions / taxes

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `CantCastSpellTypeEffect` | `(Set<CardType> restrictedTypes)` | controller can't cast spells of specified types (static) |
| `LimitSpellsPerTurnEffect` | `(int maxSpells)` | each player can cast at most N spells per turn (static) |
| `IncreaseOpponentCastCostEffect` | `(Set<CardType> affectedTypes, int amount)` | opponent's spells of types cost N more (static) |
| `RequirePaymentToAttackEffect` | `(int amountPerAttacker)` | must pay N mana per attacking creature (static) |
| `ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect` | `(int minimumCreatureDifference, int amount)` | reduce cast cost by N if opponent has M+ more creatures |
| `NoMaximumHandSizeEffect` | `()` | you have no maximum hand size (static) |
| `EnterPermanentsOfTypesTappedEffect` | `(Set<CardType> cardTypes)` | permanents of specified types enter tapped (static) |

## Choose / name

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `ChooseCardNameOnEnterEffect` | `()` | choose a card name as ETB (implements `ChooseCardNameEffect` marker) |
| `ActivatedAbilitiesOfChosenNameCantBeActivatedEffect` | `()` | activated abilities of chosen name can't be activated (static) |
| `ChooseColorOnEnterEffect` | `()` | choose a color as ETB (implements `ChooseColorEffect` marker) |

## Draw replacement / library interaction

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `AbundanceDrawReplacementEffect` | `()` | replace draws with Abundance's reveal-until mechanic (static) |
| `ReplaceSingleDrawEffect` | `(UUID playerId, DrawReplacementKind kind)` | replace a single draw with a replacement effect |
| `PlayLandsFromGraveyardEffect` | `()` | you may play lands from your graveyard (static) |

## Put onto battlefield

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `PutCardToBattlefieldEffect` | `(CardType cardType)` | you may put a card of type from hand onto battlefield (wrap in MayEffect) |
| `OpponentMayPlayCreatureEffect` | `()` | opponent may put a creature card from hand onto battlefield |

## Card-specific / one-off

| Effect | Constructor | Intent |
|--------|-------------|--------|
| `AjaniUltimateEffect` | `()` | Ajani's ultimate: put 100 counters (planeswalker-specific) |

---

## Provider map (where to add `@HandlesEffect` resolver methods)

| Category | Resolution service |
|----------|--------------------|
| Damage | `DamageResolutionService` |
| Destruction/sacrifice | `DestructionResolutionService` |
| Bounce | `BounceResolutionService` |
| Counter | `CounterResolutionService` |
| Library/search/mill | `LibraryResolutionService` |
| Graveyard return/exile | `GraveyardReturnResolutionService` |
| Player interaction (draw/discard/choices) | `effect/PlayerInteractionResolutionService` |
| Life | `effect/LifeResolutionService` |
| Creature mods (tap/pump/keyword) | `effect/CreatureModResolutionService` |
| Permanent control/tokens/regeneration | `effect/PermanentControlResolutionService` |
| Static continuous effects | `effect/StaticEffectResolutionService` |
| Prevention | `PreventionResolutionService` |
| Turn effects | `TurnResolutionService` |
| Copy/retarget | `CopyResolutionService`, `TargetRedirectionResolutionService` |
| Exile permanent | `ExileResolutionService` |
| Card-specific one-offs | `effect/CardSpecificResolutionService` |
| Land-tap triggers | `GameHelper` (`checkLandTapTriggers`) |
| Win conditions | `effect/WinConditionResolutionService` |

All resolution services are in `magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/`.

## Canonical card examples

- Burn spell: `cards/s/Shock.java`
- Multi-effect targeted spell: `cards/c/Condemn.java`
- Effect composition in activated ability: `cards/o/OrcishArtillery.java`
- Spell-copy targeting stack: `cards/t/Twincast.java`
- Aura static lock: `cards/p/Pacifism.java`
- Static "can't block" creature: `cards/s/SpinelessThug.java`
- ETB token + activated cost/effect composition: `cards/s/SiegeGangCommander.java`
- ETB control handoff + upkeep drawback: `cards/s/SleeperAgent.java`
- Opponent draw trigger damage: `cards/u/UnderworldDreams.java`
- Conditional self cast-cost reduction: `cards/a/AvatarOfMight.java`
- Evasion blocked-only-by-wall-or-flying: `cards/e/ElvenRiders.java`
- ETB card name choice + static ability lock: `cards/p/PithingNeedle.java`
- Pain land (tap for colorless or colored + damage): `cards/s/SulfurousSprings.java`
- Creature land (manland): `cards/t/TreetopVillage.java`
- Lord with subtype boost + keywords: `cards/g/GoblinKing.java`
- Equipment with boost: `cards/l/LoxodonWarhammer.java`
