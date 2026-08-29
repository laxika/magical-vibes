package com.github.laxika.magicalvibes.service.filter;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.condition.ColorMostCommonAmongAllPermanents;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.CardControllerDoesNotOwnPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasDisturbPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasCyclingPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyTwoColorsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasEmbalmOrEternalizePredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasForetellPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasFlashbackPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasAdventurePredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasManaAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasNonManaActivatedAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenCardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasNoAbilitiesPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraEnchantCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsDoubleFacedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNameInControllerGraveyardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostPermanentCardsInControllerGraveyardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueLessThanSourceLoyaltyPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerToughnessTotalAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.CardToughnessGreaterThanPowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithAPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardToughnessLessThanSourceToughnessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentActivatedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToCreatureControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedDuringControllersLastTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedOrBlockedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockedOrWasBlockedBySubtypeThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockedOrWasBlockedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCastBySourceControllerThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledContinuouslySinceBeginningOfTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentCountAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerPoisonCountersAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCrewedBySourceThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCounterCountAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedSourceControllerThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageToAnythingThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageToSourceControllerThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisOrLastTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAdventurePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasManaAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCumulativeUpkeepPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasExactlyTwoColorsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestManaValueAmongAllCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestManaValueAmongAllArtifactsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestManaValueAmongControllerCreaturesOrPlaneswalkersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongAllCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControllerCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControlledCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasLowestManaValueAmongAllNonlandPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasProtectionFromColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasLeastPowerAmongAllCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenNamePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasNonManaActivatedAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasProtectionFromColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasLeastPowerAmongAllCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesColorWithEquippedCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCardTypeWithSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCreatureTypeWithEquippedCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesMostCommonColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesNameWithAnotherPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockedBySourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockedBySourceThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttacksPlayerWithMostLifePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttacksWhileSourceControllerHasMostLifePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOfSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToPermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsUnblockedAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedBySourceControllerAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsKindredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMonocoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsRenownedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSuspectedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTransformedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostOwnCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostControlledCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostControllerGraveyardCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostSourceControllerHandSizePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNameInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastSourceControllerLifeTotalPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostControlledCreatureCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostControlledCreatureCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostControlledCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostControlledSubtypeCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerEqualsToughnessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerGreaterThanActivePlayerHandSizePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerGreaterThanBasePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanControllerGraveyardCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentProtectedByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentProtectedByOpponentOfSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostControlledSubtypeCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostXWhenMadnessOtherwisePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessGreaterThanPowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentThatSaddledSourceThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PhyrexianManaPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySubtypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySupertypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTruePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsNthSpellCastThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryKickedPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueAtMostControlledCountPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueAtMostControllerGraveyardCountPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotTargetedByNamedCreatureAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesChosenNameWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesNameWithCardExiledWithSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySharesColorOrManaValueWithImprintedCardPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySingleCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouOrCreatureYouControlPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsAnyPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * The single evaluation point for the predicate and target-filter hierarchies
 * ({@link CardPredicate}, {@link PermanentPredicate}, {@link StackEntryPredicate},
 * {@link TargetFilter}).
 *
 * <p>Each family is dispatched with a switch that is exhaustive over its sealed hierarchy —
 * adding a predicate without an evaluation is a compile error, never a silent {@code false}.
 * Predicates are pure data in the domain; anything that needs engine-computed state (effective
 * power/toughness with static bonuses, changeling-aware subtype checks, animation-aware
 * creature checks) delegates to {@link GameQueryService}.</p>
 *
 * <p>Several evaluations have an explicit {@code gameData == null} fallback that uses only the
 * permanent's intrinsic values — these are relied on by callers that match predicates outside
 * a full game context and must be preserved.</p>
 */
@Service
@RequiredArgsConstructor
public class PredicateEvaluationService {

    private final GameQueryService gameQueryService;

    /** Creature leaf built here rather than taken from an ability, so it never reads the CR 613.6 memo. */
    private static final PermanentIsCreaturePredicate STATIC_CREATURE_LEAF = new PermanentIsCreaturePredicate();

    /** Changeling leaf, built here for the same reason as {@link #STATIC_CREATURE_LEAF}. */
    private static final PermanentHasKeywordPredicate CHANGELING_PREDICATE =
            new PermanentHasKeywordPredicate(Keyword.CHANGELING);

    private static final Set<CardSubtype> BASIC_LAND_SUBTYPES = EnumSet.of(
            CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
            CardSubtype.MOUNTAIN, CardSubtype.FOREST);

    // --- Card predicate matching ---

    /**
     * Tests whether a card satisfies the given {@link CardPredicate}. Supports composite predicates
     * ({@link CardAllOfPredicate}, {@link CardAnyOfPredicate}, {@link CardNotPredicate}) as well as
     * leaf predicates for type, subtype, keyword, color, aura status, and self-identity.
     *
     * @param card         the card to test
     * @param predicate    the predicate to evaluate, or {@code null} (always matches)
     * @param sourceCardId the ID of the source card, used by {@link CardIsSelfPredicate}
     * @return {@code true} if the card matches the predicate
     */
    public boolean matchesCardPredicate(Card card, CardPredicate predicate, UUID sourceCardId) {
        return matchesCardPredicateInternal(card, predicate, sourceCardId, null, null, null, null, null);
    }

    /**
     * Overload that accounts for Arcane Adaptation-style effects: when a {@link GameData} and
     * card owner are provided, {@link CardSubtypePredicate} checks also include subtypes
     * granted by all-zone subtype grants (see {@link GameQueryService#cardHasSubtype}).
     */
    public boolean matchesCardPredicate(Card card, CardPredicate predicate, UUID sourceCardId,
                                        GameData gameData, UUID cardOwnerId) {
        return matchesCardPredicateInternal(card, predicate, sourceCardId, gameData, cardOwnerId, null, null, null);
    }

    /**
     * Card-predicate matching for a resolution-time target re-check that can use the source
     * permanent's exact identity and last-known power if that permanent has left the battlefield.
     */
    public boolean matchesCardPredicate(Card card, CardPredicate predicate, UUID sourceCardId,
                                        GameData gameData, UUID cardOwnerId, UUID sourcePermanentId,
                                        Integer sourcePowerAtTrigger) {
        return matchesCardPredicateInternal(card, predicate, sourceCardId, gameData, cardOwnerId,
                sourcePermanentId, sourcePowerAtTrigger, null);
    }

    public boolean matchesCardPredicate(Card card, CardPredicate predicate, UUID sourceCardId,
                                        GameData gameData, UUID cardOwnerId, UUID sourcePermanentId,
                                        Integer sourcePowerAtTrigger, Integer xValue) {
        return matchesCardPredicateInternal(card, predicate, sourceCardId, gameData, cardOwnerId,
                sourcePermanentId, sourcePowerAtTrigger, xValue);
    }

    private boolean matchesCardPredicateInternal(Card card, CardPredicate predicate, UUID sourceCardId,
                                                 GameData gameData, UUID cardOwnerId, UUID sourcePermanentId,
                                                 Integer sourcePowerAtTrigger, Integer xValue) {
        if (predicate == null) return true;

        return switch (predicate) {
            case CardTypePredicate p ->
                    gameQueryService.cardHasType(card, p.cardType(), gameData, cardOwnerId);
            case CardSubtypePredicate p ->
                    gameQueryService.cardHasSubtype(card, p.subtype(), gameData, cardOwnerId);
            case CardHasSourceChosenSubtypePredicate p -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                Permanent source = findPermanentByOriginalCardId(gameData, sourceCardId);
                if (source == null || source.getChosenSubtype() == null
                        || p.creatureOnly() && !card.hasType(CardType.CREATURE)) {
                    yield false;
                }
                yield gameQueryService.cardHasSubtype(card, source.getChosenSubtype(), gameData, cardOwnerId)
                        || (gameQueryService.isCreatureSubtype(source.getChosenSubtype())
                        && card.hasKeyword(Keyword.CHANGELING));
            }
            case CardHasSourceChosenCardTypePredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                Permanent source = findPermanentByOriginalCardId(gameData, sourceCardId);
                CardType chosenType = source == null ? null : source.getChosenCardType();
                yield chosenType != null
                        && gameQueryService.cardHasType(card, chosenType, gameData, cardOwnerId);
            }
            case CardHasSourceChosenColorPredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                Permanent source = findPermanentByOriginalCardId(gameData, sourceCardId);
                CardColor chosenColor = source == null ? null : source.getChosenColor();
                yield chosenColor != null && card.getColors().contains(chosenColor);
            }
            case CardKeywordPredicate p ->
                    card.getKeywords().contains(p.keyword());
            case CardIsSelfPredicate ignored ->
                    sourceCardId != null && card.getId().equals(sourceCardId);
            case CardColorPredicate p ->
                    card.getColors().contains(p.color());
            case CardHasExactlyTwoColorsPredicate ignored ->
                    card.getColors().size() == 2;
            case CardIsMulticoloredPredicate ignored ->
                    card.getColors().size() >= 2;
            case CardIsColorlessPredicate ignored ->
                    card.getColors().isEmpty();
            case CardIsDoubleFacedPredicate ignored ->
                    card.getBackFaceCard() != null
                            && (card.isModalDoubleFaced()
                            || card.hasType(CardType.BATTLE)
                            || card.getKeywords().contains(Keyword.TRANSFORM)
                            || card.getKeywords().contains(Keyword.DISTURB));
            case PhyrexianManaPredicate ignored ->
                    card.getManaCost() != null && new ManaCost(card.getManaCost()).hasPhyrexianMana();
            case CardIsAuraPredicate ignored ->
                    card.isAura();
            case CardIsAuraEnchantCreaturePredicate ignored ->
                    isAuraEnchantingCreature(card);
            case CardHasFlashbackPredicate ignored ->
                    card.getCastingOption(FlashbackCast.class).isPresent();
            case CardHasAdventurePredicate ignored ->
                    card.getCastingOption(AdventureCast.class).isPresent();
            case CardHasDisturbPredicate ignored ->
                    card.getCastingOption(DisturbCast.class).isPresent();
            case CardHasCyclingPredicate ignored ->
                    card.getHandActivatedAbilities().stream()
                            .anyMatch(ActivatedAbility::isCyclingAbility);
            case CardHasEmbalmOrEternalizePredicate ignored ->
                    card.getGraveyardActivatedAbilities().stream()
                            .anyMatch(ActivatedAbility::isEmbalmOrEternalize);
            case CardHasForetellPredicate ignored ->
                    card.getCastingOption(ForetellCast.class).isPresent();
            case CardHasManaAbilityPredicate ignored ->
                    PotentialManaService.hasOnTapManaEffects(card)
                            || card.getActivatedAbilities().stream()
                            .anyMatch(AbilityActivationService::isManaAbility);
            case CardHasNonManaActivatedAbilityPredicate ignored ->
                    card.getActivatedAbilities().stream()
                            .anyMatch(ability -> !AbilityActivationService.isManaAbility(ability));
            case CardHasNoAbilitiesPredicate ignored ->
                    card.getCardText() == null && card.getKeywords().isEmpty();
            case CardIsPermanentPredicate ignored ->
                    card.getType().isPermanentType();
            case CardIsTokenPredicate ignored ->
                    card.isToken();
            case CardIsHistoricPredicate ignored ->
                    gameQueryService.cardHasType(card, CardType.ARTIFACT, gameData, cardOwnerId)
                            || card.getSupertypes().contains(CardSupertype.LEGENDARY)
                            || card.getSubtypes().contains(CardSubtype.SAGA);
            case CardSupertypePredicate p ->
                    card.getSupertypes().contains(p.supertype());
            case CardManaValueAtMostPermanentCardsInControllerGraveyardPredicate ignored ->
                    gameData != null && cardOwnerId != null
                            && card.getManaValue() <= gameData.playerGraveyards
                            .getOrDefault(cardOwnerId, List.of()).stream()
                            .filter(graveyardCard -> !graveyardCard.isToken()
                                    && graveyardCard.getType().isPermanentType())
                            .count();
            case CardManaValueAtMostSourcePowerPredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                Permanent sourcePermanent = sourcePermanentId == null
                        ? findPermanentByOriginalCardId(gameData, sourceCardId)
                        : gameQueryService.findPermanentById(gameData, sourcePermanentId);
                Integer sourcePower = sourcePermanent != null
                        ? gameQueryService.getEffectivePower(gameData, sourcePermanent)
                        : sourcePowerAtTrigger != null
                        ? sourcePowerAtTrigger
                        : basePowerOfCardInAnyZone(gameData, sourceCardId);
                yield sourcePower != null && card.getManaValue() <= sourcePower;
            }
            case CardManaValueLessThanSourcePowerPredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                Permanent sourcePermanent = sourcePermanentId == null
                        ? findPermanentByOriginalCardId(gameData, sourceCardId)
                        : gameQueryService.findPermanentById(gameData, sourcePermanentId);
                Integer sourcePower = sourcePermanent != null
                        ? gameQueryService.getEffectivePower(gameData, sourcePermanent)
                        : sourcePowerAtTrigger != null
                        ? sourcePowerAtTrigger
                        : basePowerOfCardInAnyZone(gameData, sourceCardId);
                yield sourcePower != null && card.getManaValue() < sourcePower;
            }
            case CardManaValueLessThanSourceLoyaltyPredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                yield sourcePermanent != null
                        && card.getManaValue() < sourcePermanent.getCounterCount(CounterType.LOYALTY);
            }
            case CardMaxManaValuePredicate p ->
                    card.getManaValue() <= p.maxManaValue();
            case CardMaxManaValueXPredicate ignored ->
                    xValue == null || card.getManaValue() <= xValue;
            case CardMinManaValuePredicate p ->
                    card.getManaValue() + (p.includeXValue() && xValue != null ? xValue : 0)
                            >= p.minManaValue();
            case CardPowerAtLeastPredicate p ->
                    card.getPower() != null && card.getPower() >= p.minPower();
            case CardPowerAtMostPredicate p ->
                    card.getPower() != null && card.getPower() <= p.maxPower();
            case CardPowerToughnessTotalAtMostPredicate p ->
                    card.getPower() != null && card.getToughness() != null
                            && card.getPower() + card.getToughness() <= p.maxTotal();
            case CardToughnessAtLeastPredicate p ->
                    card.getToughness() != null && card.getToughness() >= p.minToughness();
            case CardToughnessGreaterThanPowerPredicate ignored ->
                    card.getPower() != null && card.getToughness() != null
                            && card.getToughness() > card.getPower();
            case CardSharesCardTypeWithImprintedCardPredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield true;
                }
                Card imprintedCard = gameData.imprintedCards.get(sourceCardId);
                if (imprintedCard == null) {
                    yield true;
                }
                yield java.util.Arrays.stream(CardType.values())
                    .anyMatch(type -> gameQueryService.cardHasType(card, type, gameData, cardOwnerId)
                            && gameQueryService.cardHasType(imprintedCard, type, gameData,
                            imprintedCard.getOwnerId()));
            }
            case CardToughnessLessThanSourceToughnessPredicate ignored -> {
                if (gameData == null || sourceCardId == null || card.getToughness() == null) {
                    yield false;
                }
                Permanent source = findPermanentByOriginalCardId(gameData, sourceCardId);
                if (source == null) {
                    yield false;
                }
                yield card.getToughness() < gameQueryService.getEffectiveToughness(gameData, source);
            }
            case CardNamedPredicate p ->
                    p.cardName().equals(card.getName());
            case CardNameInControllerGraveyardPredicate ignored ->
                    gameData != null && cardOwnerId != null
                            && gameData.playerGraveyards.getOrDefault(cardOwnerId, List.of()).stream()
                            .anyMatch(graveyardCard -> graveyardCard.getName().equals(card.getName()));
            case CardSharesNameWithAPermanentPredicate ignored ->
                    gameData != null && gameData.playerBattlefields.values().stream()
                            .flatMap(java.util.List::stream)
                            .anyMatch(perm -> perm.getCard().getName().equals(card.getName()));
            case CardNotPredicate p ->
                    !matchesCardPredicateInternal(card, p.predicate(), sourceCardId, gameData, cardOwnerId,
                            sourcePermanentId, sourcePowerAtTrigger, xValue);
            case CardAllOfPredicate p ->
                    p.predicates().stream().allMatch(sub -> matchesCardPredicateInternal(
                            card, sub, sourceCardId, gameData, cardOwnerId,
                            sourcePermanentId, sourcePowerAtTrigger, xValue));
            case CardAnyOfPredicate p ->
                    p.predicates().stream().anyMatch(sub -> matchesCardPredicateInternal(
                            card, sub, sourceCardId, gameData, cardOwnerId,
                            sourcePermanentId, sourcePowerAtTrigger, xValue));
            case CardControllerDoesNotOwnPredicate ignored ->
                    card.getOwnerId() != null && cardOwnerId != null && !card.getOwnerId().equals(cardOwnerId);
            case CardTruePredicate ignored ->
                    true;
        };
    }

    // --- Permanent predicate matching ---

    /**
     * Tests whether a permanent satisfies the given {@link PermanentPredicate},
     * using game data for keyword/stat resolution.
     *
     * @see #matchesPermanentPredicate(Permanent, PermanentPredicate, FilterContext)
     */
    public boolean matchesPermanentPredicate(GameData gameData, Permanent permanent, PermanentPredicate predicate) {
        return matchesPermanentPredicate(permanent, predicate, FilterContext.of(gameData));
    }

    /**
     * Tests whether a permanent satisfies the given {@link PermanentPredicate}. Supports
     * composite predicates (all-of, any-of, not) and leaf predicates for card type, subtype,
     * keyword, color, tapped/attacking/blocking status, token status, power threshold, and
     * source identity. When a {@link FilterContext} is provided, keyword and power checks
     * include static bonuses; otherwise they use only intrinsic values.
     *
     * @param permanent     the permanent to test
     * @param predicate     the predicate to evaluate, or {@code null} (never matches)
     * @param filterContext context providing game data, source card ID, and source controller ID
     * @return {@code true} if the permanent matches the predicate
     */
    public boolean matchesPermanentPredicate(Permanent permanent,
                                             PermanentPredicate predicate,
                                             FilterContext filterContext) {
        if (predicate == null) return false;
        GameData gameData = filterContext != null ? filterContext.gameData() : null;
        UUID sourceCardId = filterContext != null ? filterContext.sourceCardId() : null;
        UUID sourceControllerId = filterContext != null ? filterContext.sourceControllerId() : null;

        return switch (predicate) {
            case PermanentActivatedThisTurnPredicate ignored ->
                    gameData != null && gameData.activatedAbilityUsesThisTurn.containsKey(permanent.getId());
            case PermanentHasKeywordPredicate hasKeywordPredicate -> {
                if (gameData == null) {
                    yield permanent.hasKeyword(hasKeywordPredicate.keyword());
                }
                yield gameQueryService.hasKeyword(gameData, permanent, hasKeywordPredicate.keyword());
            }
            case PermanentHasProtectionFromColorPredicate hasProtectionPredicate -> {
                if (gameData == null) {
                    yield hasRecursionSafeProtectionFrom(permanent, hasProtectionPredicate.color());
                }
                yield gameQueryService.hasProtectionFrom(gameData, permanent, hasProtectionPredicate.color());
            }
            case PermanentHasSubtypePredicate hasSubtypePredicate -> {
                // While a CR 613 layered pass is active, subtype questions are answered from the
                // layer-4-corrected characteristic state (types decided in layer 4 are visible
                // to layer 5-7 filters and to CDA amount counts).
                CharacteristicState layered = LayerSystemService.activeStateFor(permanent.getId());
                if (layered != null) {
                    yield matchesPermanentPredicate(layered, permanent, hasSubtypePredicate, filterContext);
                }
                if (gameData != null && BASIC_LAND_SUBTYPES.contains(hasSubtypePredicate.subtype())) {
                    yield gameQueryService.effectiveBasicLandTypes(gameData, permanent)
                            .contains(hasSubtypePredicate.subtype());
                }
                boolean creatureSubtype = gameQueryService.isCreatureSubtype(hasSubtypePredicate.subtype());
                // "Loses all creature types" strips every creature subtype (base/transient/granted) and,
                // via hasKeyword, the Changeling grant too.
                if (creatureSubtype && permanent.isLosesAllCreatureTypesUntilEndOfTurn()) {
                    yield false;
                }
                if (permanent.getTransientRemovedSubtypes().contains(hasSubtypePredicate.subtype())) {
                    yield false;
                }
                // "Becomes a [creature type]" (Boldwyr Intimidator) replaces every creature subtype
                // with the single override, overwriting base/transient/granted types and Changeling.
                if (creatureSubtype && !permanent.getTransientCreatureTypeOverrides().isEmpty()) {
                    yield permanent.getTransientCreatureTypeOverrides().contains(hasSubtypePredicate.subtype());
                }
                if (creatureSubtype && permanent.getTransientCreatureTypeOverride() != null) {
                    yield permanent.getTransientCreatureTypeOverride() == hasSubtypePredicate.subtype();
                }
                if (gameData != null && creatureSubtype) {
                    yield gameQueryService.effectiveCreatureSubtypes(gameData, permanent)
                            .contains(hasSubtypePredicate.subtype())
                            || gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING);
                }
                yield permanent.getCard().getSubtypes().contains(hasSubtypePredicate.subtype())
                        || permanent.getTransientSubtypes().contains(hasSubtypePredicate.subtype())
                        || permanent.getGrantedSubtypes().contains(hasSubtypePredicate.subtype())
                        || (creatureSubtype && permanent.hasKeyword(Keyword.CHANGELING));
            }
            case PermanentHasAnySubtypePredicate hasAnySubtypePredicate -> {
                CharacteristicState layered = LayerSystemService.activeStateFor(permanent.getId());
                if (layered != null) {
                    yield matchesPermanentPredicate(layered, permanent, hasAnySubtypePredicate, filterContext);
                }
                Set<CardSubtype> wanted = permanent.isLosesAllCreatureTypesUntilEndOfTurn()
                        ? hasAnySubtypePredicate.subtypes().stream()
                                .filter(st -> !gameQueryService.isCreatureSubtype(st))
                                .collect(java.util.stream.Collectors.toSet())
                        : hasAnySubtypePredicate.subtypes();
                if (!permanent.getTransientRemovedSubtypes().isEmpty()) {
                    wanted = wanted.stream()
                            .filter(st -> !permanent.getTransientRemovedSubtypes().contains(st))
                            .collect(java.util.stream.Collectors.toSet());
                }
                boolean hasSubtype = permanent.getCard().getSubtypes().stream().anyMatch(wanted::contains)
                        || permanent.getTransientSubtypes().stream().anyMatch(wanted::contains)
                        || permanent.getGrantedSubtypes().stream().anyMatch(wanted::contains);
                if (!hasSubtype && gameData != null && !GameQueryService.isStaticEvaluationActive()) {
                    hasSubtype = gameQueryService.effectiveCreatureSubtypes(gameData, permanent).stream()
                            .anyMatch(wanted::contains);
                }
                boolean canUseChangeling = wanted.stream().anyMatch(gameQueryService::isCreatureSubtype);
                yield hasSubtype || (canUseChangeling && (gameData == null
                        ? permanent.hasKeyword(Keyword.CHANGELING)
                        : gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING)));
            }
            case PermanentHasAdventurePredicate ignored ->
                    permanent.getCard().getCastingOption(AdventureCast.class).isPresent();
            case PermanentHasNonManaActivatedAbilityPredicate hasNonManaAbilityPredicate ->
                    hasNonManaActivatedAbility(gameData, permanent, hasNonManaAbilityPredicate.levelUpOnly());
            case PermanentHasManaAbilityPredicate ignored ->
                    hasManaAbility(gameData, permanent);
            case PermanentIsCreaturePredicate ignored -> {
                if (gameData == null) {
                    yield permanent.getCard().hasType(CardType.CREATURE)
                            || permanent.isAnimatedUntilEndOfTurn()
                            || permanent.isAnimatedUntilEndOfCombat()
                            || permanent.isAnimatedUntilNextTurn()
                            || permanent.isPermanentlyAnimated()
                            || permanent.getCounterCount(CounterType.AWAKENING) > 0;
                }
                yield gameQueryService.isCreature(gameData, permanent);
            }
            case PermanentIsLandPredicate ignored -> {
                if (gameData == null) {
                    yield permanent.getCard().hasType(CardType.LAND);
                }
                yield gameQueryService.isLand(gameData, permanent);
            }
            case PermanentIsArtifactPredicate ignored -> {
                if (gameData == null) {
                    yield gameQueryService.isArtifact(permanent);
                }
                yield gameQueryService.isArtifact(gameData, permanent);
            }
            case PermanentIsHistoricPredicate ignored -> {
                boolean artifact = gameData == null
                        ? gameQueryService.isArtifact(permanent)
                        : gameQueryService.isArtifact(gameData, permanent);
                yield artifact
                        || gameQueryService.hasEffectiveSupertype(gameData, permanent, CardSupertype.LEGENDARY)
                        || hasSagaSubtype(permanent);
            }
            case PermanentIsEnchantedPredicate ignored ->
                    gameData != null && gameQueryService.isEnchanted(gameData, permanent);
            case PermanentIsEnchantedBySourceControllerAuraPredicate ignored ->
                    hasAuraControlledBySourceControllerAttachedTo(gameData, permanent, sourceControllerId);
            case PermanentIsEquippedPredicate ignored ->
                    gameData != null && gameQueryService.isEquipped(gameData, permanent);
            case PermanentIsModifiedPredicate ignored -> isModified(gameData, permanent, filterContext);
            case PermanentAttachedToCreaturePredicate ignored -> {
                if (gameData == null || !permanent.isAttached()) {
                    yield false;
                }
                Permanent host = gameQueryService.findPermanentById(gameData, permanent.getAttachedTo());
                yield host != null && gameQueryService.isCreature(gameData, host);
            }
            case PermanentAttachedToCreatureControlledBySourceControllerPredicate ignored -> {
                if (gameData == null || sourceControllerId == null || !permanent.isAttached()) {
                    yield false;
                }
                Permanent host = gameQueryService.findPermanentById(gameData, permanent.getAttachedTo());
                UUID hostControllerId = host == null ? null : gameData.findControllerOf(host);
                yield host != null && gameQueryService.isCreature(gameData, host)
                        && sourceControllerId.equals(hostControllerId);
            }
            case PermanentIsAuraAttachedToSourcePredicate ignored -> {
                if (gameData == null || sourceCardId == null
                        || !permanent.getCard().isAura() || !permanent.isAttached()) {
                    yield false;
                }
                Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                yield sourcePermanent != null && permanent.getAttachedTo().equals(sourcePermanent.getId());
            }
            case PermanentIsHostOfSourceAuraPredicate ignored -> {
                Permanent sourceAura = null;
                if (gameData != null && filterContext != null && filterContext.sourcePermanentId() != null) {
                    sourceAura = gameQueryService.findPermanentById(gameData, filterContext.sourcePermanentId());
                }
                if (sourceAura == null && gameData != null && sourceCardId != null) {
                    sourceAura = findPermanentByOriginalCardId(gameData, sourceCardId);
                }
                if (sourceAura == null && filterContext != null) {
                    sourceAura = filterContext.sourcePermanentSnapshot();
                }
                yield sourceAura != null && sourceAura.isAttached()
                        && sourceAura.getAttachedTo().equals(permanent.getId());
            }
            case PermanentAttachedToSourcePermanentPredicate ignored -> {
                Permanent sourcePermanent = null;
                if (gameData != null && filterContext != null && filterContext.sourcePermanentId() != null) {
                    sourcePermanent = gameQueryService.findPermanentById(gameData, filterContext.sourcePermanentId());
                }
                if (sourcePermanent == null && filterContext != null) {
                    sourcePermanent = filterContext.sourcePermanentSnapshot();
                }
                if (sourcePermanent == null && gameData != null && sourceCardId != null) {
                    sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                }
                yield sourcePermanent != null && permanent.isAttached()
                        && sourcePermanent.getId().equals(permanent.getAttachedTo());
            }
            case PermanentSharesColorWithEquippedCreaturePredicate ignored -> {
                Permanent equipped = equippedCreatureOfSource(gameData, sourceCardId, filterContext);
                if (equipped == null) {
                    yield false;
                }
                Set<CardColor> equippedColors = gameQueryService.getEffectiveColors(gameData, equipped);
                yield !equippedColors.isEmpty() && gameQueryService.getEffectiveColors(gameData, permanent)
                        .stream().anyMatch(equippedColors::contains);
            }
            case PermanentSharesCreatureTypeWithEquippedCreaturePredicate ignored -> {
                Permanent equipped = equippedCreatureOfSource(gameData, sourceCardId, filterContext);
                yield equipped != null && gameQueryService.shareCreatureType(gameData, permanent, equipped);
            }
            case PermanentSharesCardTypeWithSourcePermanentPredicate ignored ->
                    sharesCardTypeWithSourcePermanent(permanent, filterContext);
            case PermanentSharesMostCommonColorPredicate ignored -> {
                if (gameData == null) {
                    yield false;
                }
                yield permanent.getEffectiveColors().stream()
                        .anyMatch(color -> ColorMostCommonAmongAllPermanents.isMostCommon(gameData, color));
            }
            case PermanentIsAuraAttachedToCreaturePredicate ignored -> {
                if (gameData == null || !permanent.getCard().isAura() || !permanent.isAttached()) {
                    yield false;
                }
                Permanent host = gameQueryService.findPermanentById(gameData, permanent.getAttachedTo());
                yield host != null && (GameQueryService.isStaticEvaluationActive()
                        ? isCreatureForStaticEvaluation(host)
                        : gameQueryService.isCreature(gameData, host));
            }
            case PermanentIsAuraAttachedToLandPredicate ignored -> {
                if (gameData == null || !permanent.getCard().isAura() || !permanent.isAttached()) {
                    yield false;
                }
                Permanent host = gameQueryService.findPermanentById(gameData, permanent.getAttachedTo());
                yield host != null && gameQueryService.isLand(gameData, host);
            }
            case PermanentIsAuraAttachedToPermanentControlledBySourceControllerPredicate ignored -> {
                if (gameData == null || sourceControllerId == null
                        || !permanent.getCard().isAura() || !permanent.isAttached()) {
                    yield false;
                }
                Permanent host = gameQueryService.findPermanentById(gameData, permanent.getAttachedTo());
                yield host != null && sourceControllerId.equals(gameData.findControllerOf(host));
            }
            case PermanentIsEnchantmentPredicate ignored -> {
                if (gameData == null) {
                    yield gameQueryService.isEnchantment(permanent);
                }
                yield gameQueryService.isEnchantment(gameData, permanent);
            }
            case PermanentIsFaceDownPredicate ignored -> permanent.isFaceDown();
            case PermanentIsPlaneswalkerPredicate ignored -> {
                if (gameData == null) {
                    yield permanent.getCard().hasType(CardType.PLANESWALKER);
                }
                yield gameQueryService.isPlaneswalker(gameData, permanent);
            }
            case PermanentIsBattlePredicate ignored -> {
                if (gameData == null) {
                    yield permanent.getCard().hasType(CardType.BATTLE);
                }
                yield gameQueryService.isBattle(gameData, permanent);
            }
            case PermanentProtectedByOpponentOfSourceControllerPredicate ignored ->
                    gameData != null && sourceControllerId != null
                            && gameQueryService.isBattle(gameData, permanent)
                            && permanent.getProtectorPlayerId() != null
                            && !sourceControllerId.equals(permanent.getProtectorPlayerId());
            case PermanentProtectedByDefendingPlayerPredicate ignored ->
                    gameData != null && filterContext != null
                            && gameQueryService.isBattle(gameData, permanent)
                            && permanent.getProtectorPlayerId() != null
                            && filterContext.defendingPlayerId() != null
                            && filterContext.defendingPlayerId().equals(permanent.getProtectorPlayerId());
            case PermanentIsKindredPredicate ignored -> {
                if (gameData == null) {
                    yield permanent.getCard().hasType(CardType.KINDRED);
                }
                yield gameQueryService.isKindred(gameData, permanent);
            }
            case PermanentIsTappedPredicate ignored ->
                    permanent.isTapped();
            case PermanentIsRenownedPredicate ignored ->
                    permanent.isRenowned();
            case PermanentIsSuspectedPredicate ignored ->
                    permanent.isSuspected();
            case PermanentIsTokenPredicate ignored ->
                    permanent.getCard().isToken();
            case PermanentIsTransformedPredicate ignored ->
                    permanent.isTransformed();
            case PermanentIsAttackingPredicate ignored ->
                    permanent.isAttacking();
            case PermanentAttacksPlayerWithMostLifePredicate ignored ->
                    attacksPlayerWithMostLife(gameData, permanent);
            case PermanentAttacksWhileSourceControllerHasMostLifePredicate ignored ->
                    attacksWhileSourceControllerHasMostLife(gameData, permanent, sourceControllerId);
            case PermanentIsAttackingOpponentOfSourceControllerPredicate ignored ->
                    permanent.isAttacking() && sourceControllerId != null && gameData != null
                            && gameData.playerIds.contains(permanent.getAttackTarget())
                            && !sourceControllerId.equals(permanent.getAttackTarget());
            case PermanentIsAttackingSourceControllerPredicate ignored ->
                    permanent.isAttacking() && sourceControllerId != null
                            && sourceControllerId.equals(permanent.getAttackTarget());
            case PermanentIsBlockingPredicate ignored ->
                    permanent.isBlocking();
            case PermanentAttackedDuringControllersLastTurnPredicate ignored ->
                    permanent.isAttackedDuringControllersLastTurn();
            case PermanentAttackedThisTurnPredicate ignored ->
                    permanent.isAttackedThisTurn();
            case PermanentAttackedOrBlockedThisTurnPredicate ignored ->
                    permanent.isAttackedThisTurn() || permanent.isBlockedThisTurn();
            case PermanentIsBlockedPredicate ignored ->
                    gameData != null && permanent.isAttacking() && isBlocked(gameData, permanent);
            case PermanentIsUnblockedAttackingPredicate ignored ->
                    // "Unblocked" only after blockers are declared (Gatherer: Gossamer Chains).
                    gameData != null
                            && gameData.currentStep != null
                            && !gameData.currentStep.isBeforeBlockersDeclared()
                            && permanent.isAttacking()
                            && !isBlocked(gameData, permanent);
            case PermanentBlockedOrWasBlockedThisTurnPredicate ignored ->
                    gameData != null && gameData.combatBlockOpponentIdsThisTurn.containsKey(permanent.getId());
            case PermanentBlockedOrWasBlockedBySubtypeThisTurnPredicate p -> {
                if (gameData == null) {
                    yield false;
                }
                UUID id = permanent.getId();
                yield gameData.creaturesInCombatWithChangelingThisTurn.contains(id)
                        || gameData.combatBlockOpponentSubtypesThisTurn
                                .getOrDefault(id, java.util.Set.of()).contains(p.subtype());
            }
            // The null-GameData branches of the P/T leaves are the recursion-safe path taken
            // from inside the layered pass. They no longer read the permanent's printed numbers:
            // powerForStaticFilter answers with the layered value wherever that is reachable
            // without re-entering the assembly (see GameQueryService#powerForStaticFilter).
            case PermanentPowerAtMostPredicate powerAtMostPredicate -> {
                if (gameData == null) {
                    yield gameQueryService.powerForStaticFilter(permanent) <= powerAtMostPredicate.maxPower();
                }
                yield gameQueryService.getEffectivePower(gameData, permanent) <= powerAtMostPredicate.maxPower();
            }
            case PermanentPowerAtMostXPredicate ignored -> {
                int xVal = filterContext != null && filterContext.xValue() != null ? filterContext.xValue() : 0;
                if (gameData == null) {
                    yield gameQueryService.powerForStaticFilter(permanent) <= xVal;
                }
                yield gameQueryService.getEffectivePower(gameData, permanent) <= xVal;
            }
            case PermanentPowerLessThanXPredicate ignored -> {
                int xVal = filterContext != null && filterContext.xValue() != null ? filterContext.xValue() : 0;
                if (gameData == null) {
                    yield gameQueryService.powerForStaticFilter(permanent) < xVal;
                }
                yield gameQueryService.getEffectivePower(gameData, permanent) < xVal;
            }
            case PermanentPowerLessThanControllerGraveyardCountPredicate ignored -> {
                if (gameData == null || sourceControllerId == null) {
                    yield false;
                }
                int graveyardCount = gameData.playerGraveyards
                        .getOrDefault(sourceControllerId, List.of())
                        .size();
                yield gameQueryService.getEffectivePower(gameData, permanent) < graveyardCount;
            }
            case PermanentPowerAtMostControlledCreatureCountPredicate ignored -> {
                if (gameData == null || sourceControllerId == null) {
                    yield false;
                }
                List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
                int creatureCount = 0;
                if (controllerBattlefield != null) {
                    for (Permanent p : controllerBattlefield) {
                        if (gameQueryService.isCreature(gameData, p)) {
                            creatureCount++;
                        }
                    }
                }
                yield gameQueryService.getEffectivePower(gameData, permanent) <= creatureCount;
            }
            case PermanentPowerAtMostControlledCountPredicate countPredicate -> {
                if (gameData == null || sourceControllerId == null) {
                    yield false;
                }
                List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
                int matchingCount = 0;
                if (controllerBattlefield != null) {
                    for (Permanent controlledPermanent : controllerBattlefield) {
                        if (matchesPermanentPredicate(controlledPermanent, countPredicate.countFilter(), filterContext)) {
                            matchingCount++;
                        }
                    }
                }
                yield gameQueryService.getEffectivePower(gameData, permanent) <= matchingCount;
            }
            case PermanentPowerAtMostControlledCreatureCountersPredicate countersPredicate -> {
                if (gameData == null || sourceControllerId == null) {
                    yield false;
                }
                List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
                int counterCount = 0;
                if (controllerBattlefield != null) {
                    for (Permanent controlledPermanent : controllerBattlefield) {
                        if (gameQueryService.isCreature(gameData, controlledPermanent)) {
                            counterCount += controlledPermanent.getCounterCount(countersPredicate.counterType());
                        }
                    }
                }
                yield gameQueryService.getEffectivePower(gameData, permanent) <= counterCount;
            }
            case PermanentPowerGreaterThanActivePlayerHandSizePredicate ignored -> {
                if (gameData == null || gameData.activePlayerId == null) {
                    yield false;
                }
                List<Card> activePlayerHand = gameData.playerHands.get(gameData.activePlayerId);
                int handSize = activePlayerHand == null ? 0 : activePlayerHand.size();
                yield gameQueryService.getEffectivePower(gameData, permanent) > handSize;
            }
            case PermanentPowerGreaterThanBasePowerPredicate ignored -> {
                CharacteristicState layered = LayerSystemService.activeStateFor(permanent.getId());
                if (gameData == null) {
                    yield layered != null
                            ? layered.getEffectivePower() > layered.getBasePower()
                            : gameQueryService.powerForStaticFilter(permanent) > permanent.getBasePower();
                }
                GameQueryService.StaticBonus bonus = gameQueryService.computeStaticBonus(gameData, permanent);
                int basePower = bonus.basePTOverridden()
                        ? bonus.basePowerOverride()
                        : permanent.getBasePower();
                yield gameQueryService.getEffectivePower(gameData, permanent) > basePower;
            }
            case PermanentPowerAtMostControlledSubtypeCountPredicate subtypeCountPredicate -> {
                if (gameData == null || sourceControllerId == null) {
                    yield false;
                }
                List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
                int subtypeCount = 0;
                if (controllerBattlefield != null) {
                    PermanentHasSubtypePredicate subtypePredicate =
                            new PermanentHasSubtypePredicate(subtypeCountPredicate.subtype());
                    for (Permanent p : controllerBattlefield) {
                        if (matchesPermanentPredicate(gameData, p, subtypePredicate)) {
                            subtypeCount++;
                        }
                    }
                }
                yield gameQueryService.getEffectivePower(gameData, permanent) <= subtypeCount;
            }
            case PermanentToughnessAtMostControlledSubtypeCountPredicate countPredicate -> {
                if (gameData == null || sourceControllerId == null) {
                    yield false;
                }
                List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
                int subtypeCount = 0;
                if (controllerBattlefield != null) {
                    PermanentHasSubtypePredicate subtypePredicate =
                            new PermanentHasSubtypePredicate(countPredicate.subtype());
                    for (Permanent controlledPermanent : controllerBattlefield) {
                        if (matchesPermanentPredicate(gameData, controlledPermanent, subtypePredicate)) {
                            subtypeCount++;
                        }
                    }
                }
                yield gameQueryService.getEffectiveToughness(gameData, permanent) <= subtypeCount;
            }
            case PermanentManaValueAtMostControlledCountPredicate countPredicate -> {
                if (gameData == null || sourceControllerId == null) {
                    yield false;
                }
                List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
                int matchingCount = 0;
                if (controllerBattlefield != null) {
                    for (Permanent controlledPermanent : controllerBattlefield) {
                        if (matchesPermanentPredicate(controlledPermanent, countPredicate.countFilter(), filterContext)) {
                            matchingCount++;
                        }
                    }
                }
                yield permanent.getCard().getManaValue() <= matchingCount;
            }
            case PermanentManaValueAtMostSourceControllerHandSizePredicate ignored -> {
                if (gameData == null || sourceControllerId == null) {
                    yield false;
                }
                List<Card> sourceControllerHand = gameData.playerHands.get(sourceControllerId);
                int handSize = sourceControllerHand == null ? 0 : sourceControllerHand.size();
                yield permanent.getCard().getManaValue() <= handSize;
            }
            case PermanentManaValueAtMostControllerGraveyardCountPredicate ignored -> {
                if (gameData == null) {
                    yield false;
                }
                UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                if (controllerId == null) {
                    yield false;
                }
                List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
                int graveyardSize = graveyard == null
                        ? 0
                        : (int) graveyard.stream().filter(card -> !card.isToken()).count();
                yield permanent.getCard().getManaValue() <= graveyardSize;
            }
            case PermanentManaValueAtMostXPredicate ignored -> {
                // Before X is known (target enumeration / static filters) treat every permanent as
                // potentially matching, since X can be any non-negative integer.
                if (filterContext == null || filterContext.xValue() == null) {
                    yield true;
                }
                yield permanent.getCard().getManaValue() <= filterContext.xValue();
            }
            case PermanentManaValueEqualsXPredicate ignored -> {
                // When xValue is null (e.g. during valid-target checks before X is chosen),
                // any creature is potentially valid since X can be any non-negative integer.
                if (filterContext == null || filterContext.xValue() == null) {
                    yield true;
                }
                yield permanent.getCard().getManaValue() == filterContext.xValue();
            }
            case PermanentMaxManaValueXPredicate ignored -> {
                // Same permissive fallback as the equals-X sibling: with no X chosen yet, every
                // permanent is potentially legal because X can be arbitrarily large.
                if (filterContext == null || filterContext.xValue() == null) {
                    yield true;
                }
                yield permanent.getCard().getManaValue() <= filterContext.xValue();
            }
            case PermanentMaxManaValuePredicate maxManaValuePredicate ->
                    permanent.getCard().getManaValue() <= maxManaValuePredicate.maxManaValue();
            case PermanentMinManaValuePredicate minManaValuePredicate ->
                    permanent.getCard().getManaValue() >= minManaValuePredicate.minManaValue();
            case PermanentManaValueEqualsSourceCountersPredicate equalsSourceCounters -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                if (sourcePermanent == null && filterContext != null) {
                    sourcePermanent = filterContext.sourcePermanentSnapshot();
                }
                if (sourcePermanent == null) {
                    yield false;
                }
                yield permanent.getCard().getManaValue()
                        == sourcePermanent.getCounterCount(equalsSourceCounters.counterType());
            }
            case PermanentManaValueAtMostOwnCountersPredicate atMostOwnCounters ->
                    permanent.getCard().getManaValue()
                            <= permanent.getCounterCount(atMostOwnCounters.counterType());
            case PermanentPowerAtLeastPredicate powerAtLeastPredicate -> {
                if (gameData == null) {
                    yield gameQueryService.powerForStaticFilter(permanent) >= powerAtLeastPredicate.minPower();
                }
                yield gameQueryService.getEffectivePower(gameData, permanent) >= powerAtLeastPredicate.minPower();
            }
            case PermanentPowerAtLeastSourceControllerLifeTotalPredicate ignored -> {
                if (gameData == null || sourceControllerId == null) {
                    yield false;
                }
                yield gameQueryService.getEffectivePower(gameData, permanent)
                        >= gameData.getLife(sourceControllerId);
            }
            case PermanentToughnessAtMostPredicate toughnessAtMostPredicate -> {
                if (gameData == null) {
                    yield gameQueryService.toughnessForStaticFilter(permanent) <= toughnessAtMostPredicate.maxToughness();
                }
                yield gameQueryService.getEffectiveToughness(gameData, permanent) <= toughnessAtMostPredicate.maxToughness();
            }
            case PermanentToughnessAtMostXPredicate ignored -> {
                int xVal = filterContext != null && filterContext.xValue() != null ? filterContext.xValue() : 0;
                if (gameData == null) {
                    yield gameQueryService.toughnessForStaticFilter(permanent) <= xVal;
                }
                yield gameQueryService.getEffectiveToughness(gameData, permanent) <= xVal;
            }
            case PermanentToughnessAtMostXWhenMadnessOtherwisePredicate madnessPredicate -> {
                int maxToughness = filterContext != null && filterContext.madness()
                        ? filterContext.xValue() != null ? filterContext.xValue() : 0
                        : madnessPredicate.normalMaxToughness();
                if (gameData == null) {
                    yield gameQueryService.toughnessForStaticFilter(permanent) <= maxToughness;
                }
                yield gameQueryService.getEffectiveToughness(gameData, permanent) <= maxToughness;
            }
            case PermanentToughnessAtLeastPredicate toughnessAtLeastPredicate -> {
                if (gameData == null) {
                    yield gameQueryService.toughnessForStaticFilter(permanent) >= toughnessAtLeastPredicate.minToughness();
                }
                yield gameQueryService.getEffectiveToughness(gameData, permanent) >= toughnessAtLeastPredicate.minToughness();
            }
            case PermanentPowerEqualsToughnessPredicate ignored -> {
                if (gameData == null) {
                    yield gameQueryService.powerForStaticFilter(permanent)
                            == gameQueryService.toughnessForStaticFilter(permanent);
                }
                yield gameQueryService.getEffectivePower(gameData, permanent)
                        == gameQueryService.getEffectiveToughness(gameData, permanent);
            }
            case PermanentToughnessGreaterThanPowerPredicate ignored -> {
                if (gameData == null) {
                    yield gameQueryService.toughnessForStaticFilter(permanent)
                            > gameQueryService.powerForStaticFilter(permanent);
                }
                yield gameQueryService.getEffectiveToughness(gameData, permanent)
                        > gameQueryService.getEffectivePower(gameData, permanent);
            }
            case PermanentHasSupertypePredicate hasSupertypePredicate -> {
                CharacteristicState layered = LayerSystemService.activeStateFor(permanent.getId());
                if (layered != null) {
                    yield layered.hasSupertype(hasSupertypePredicate.supertype());
                }
                yield gameQueryService.hasEffectiveSupertype(gameData, permanent, hasSupertypePredicate.supertype());
            }
            case PermanentColorInPredicate colorInPredicate -> {
                // While a CR 613 layered pass is active, colors come from the layer-5 state
                // (answering from the state also avoids recursing into computeStaticBonus for
                // the permanent whose bonus is being assembled right now).
                CharacteristicState layeredColors = LayerSystemService.activeStateFor(permanent.getId());
                if (layeredColors != null) {
                    yield layeredColors.getColors().stream().anyMatch(colorInPredicate.colors()::contains);
                }
                if (gameData != null) {
                    yield gameQueryService.getEffectiveColors(gameData, permanent).stream()
                            .anyMatch(colorInPredicate.colors()::contains);
                }
                if (permanent.isColorOverridden()) {
                    yield permanent.getTransientColors().stream().anyMatch(colorInPredicate.colors()::contains);
                }
                yield permanent.getEffectiveColors().stream().anyMatch(colorInPredicate.colors()::contains)
                        || permanent.getTransientColors().stream().anyMatch(colorInPredicate.colors()::contains)
                        || permanent.getGrantedColors().stream().anyMatch(colorInPredicate.colors()::contains);
            }
            case PermanentIsMonocoloredPredicate ignored -> {
                // Monocolored = exactly one effective colour (colourless and multicoloured don't match).
                // Colours come from the same sources as PermanentColorInPredicate above.
                CharacteristicState layeredColors = LayerSystemService.activeStateFor(permanent.getId());
                if (layeredColors != null) {
                    yield layeredColors.getColors().size() == 1;
                }
                if (gameData != null) {
                    yield gameQueryService.getEffectiveColors(gameData, permanent).size() == 1;
                }
                if (permanent.isColorOverridden()) {
                    yield permanent.getTransientColors().size() == 1;
                }
                Set<CardColor> combined = EnumSet.noneOf(CardColor.class);
                combined.addAll(permanent.getEffectiveColors());
                combined.addAll(permanent.getTransientColors());
                combined.addAll(permanent.getGrantedColors());
                yield combined.size() == 1;
            }
            case PermanentIsColorlessPredicate ignored -> {
                // Colourless = zero effective colours.
                // Colours come from the same sources as PermanentIsMonocoloredPredicate above.
                CharacteristicState layeredColors = LayerSystemService.activeStateFor(permanent.getId());
                if (layeredColors != null) {
                    yield layeredColors.getColors().isEmpty();
                }
                if (gameData != null) {
                    yield gameQueryService.getEffectiveColors(gameData, permanent).isEmpty();
                }
                if (permanent.isColorOverridden()) {
                    yield permanent.getTransientColors().isEmpty();
                }
                Set<CardColor> combined = EnumSet.noneOf(CardColor.class);
                combined.addAll(permanent.getEffectiveColors());
                combined.addAll(permanent.getTransientColors());
                combined.addAll(permanent.getGrantedColors());
                yield combined.isEmpty();
            }
            case PermanentIsMulticoloredPredicate ignored -> {
                // Multicolored = two or more effective colours (colourless and monocoloured don't match).
                // Colours come from the same sources as PermanentIsMonocoloredPredicate above.
                CharacteristicState layeredColors = LayerSystemService.activeStateFor(permanent.getId());
                if (layeredColors != null) {
                    yield layeredColors.getColors().size() >= 2;
                }
                if (gameData != null) {
                    yield gameQueryService.getEffectiveColors(gameData, permanent).size() >= 2;
                }
                if (permanent.isColorOverridden()) {
                    yield permanent.getTransientColors().size() >= 2;
                }
                Set<CardColor> combined = EnumSet.noneOf(CardColor.class);
                combined.addAll(permanent.getEffectiveColors());
                combined.addAll(permanent.getTransientColors());
                combined.addAll(permanent.getGrantedColors());
                yield combined.size() >= 2;
            }
            case PermanentHasExactlyTwoColorsPredicate ignored -> {
                CharacteristicState layeredColors = LayerSystemService.activeStateFor(permanent.getId());
                if (layeredColors != null) {
                    yield layeredColors.getColors().size() == 2;
                }
                if (gameData != null) {
                    yield gameQueryService.getEffectiveColors(gameData, permanent).size() == 2;
                }
                if (permanent.isColorOverridden()) {
                    yield permanent.getTransientColors().size() == 2;
                }
                Set<CardColor> combined = EnumSet.noneOf(CardColor.class);
                combined.addAll(permanent.getEffectiveColors());
                combined.addAll(permanent.getTransientColors());
                combined.addAll(permanent.getGrantedColors());
                yield combined.size() == 2;
            }
            case PermanentAnyOfPredicate anyOfPredicate -> {
                for (PermanentPredicate nested : anyOfPredicate.predicates()) {
                    if (matchesPermanentPredicate(permanent, nested, filterContext)) {
                        yield true;
                    }
                }
                yield false;
            }
            case PermanentAllOfPredicate allOfPredicate -> {
                for (PermanentPredicate nested : allOfPredicate.predicates()) {
                    if (!matchesPermanentPredicate(permanent, nested, filterContext)) {
                        yield false;
                    }
                }
                yield true;
            }
            case PermanentNotPredicate notPredicate ->
                    !matchesPermanentPredicate(permanent, notPredicate.predicate(), filterContext);
            case PermanentIsSourcePermanentPredicate ignored -> {
                if (filterContext == null) {
                    yield false;
                }
                if (filterContext.sourcePermanentId() != null) {
                    yield filterContext.sourcePermanentId().equals(permanent.getId());
                }
                Permanent sourcePermanent = filterContext.sourcePermanentSnapshot();
                if (sourcePermanent == null && gameData != null && sourceCardId != null) {
                    sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                }
                yield sourcePermanent != null && sourcePermanent.getId().equals(permanent.getId());
            }
            case PermanentIsSourceCardPredicate ignored ->
                    sourceCardId != null && permanent.getOriginalCard().getId().equals(sourceCardId);
            case PermanentIsSpecificPermanentPredicate specific ->
                    specific.permanentId() != null && specific.permanentId().equals(permanent.getId());
            case PermanentControlledBySourceControllerPredicate ignored -> {
                if (sourceControllerId == null || gameData == null) {
                    yield false;
                }
                List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
                yield controllerBattlefield != null && controllerBattlefield.contains(permanent);
            }
            case PermanentControlledByActivePlayerPredicate ignored -> {
                if (gameData == null || gameData.activePlayerId == null) {
                    yield false;
                }
                List<Permanent> activeBattlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
                yield activeBattlefield != null && activeBattlefield.contains(permanent);
            }
            case PermanentControlledByDefendingPlayerPredicate ignored -> {
                if (gameData == null) {
                    yield false;
                }
                UUID controllerId = gameData.findControllerOf(permanent);
                UUID defendingPlayerId = filterContext == null ? null : filterContext.defendingPlayerId();
                yield controllerId != null && (defendingPlayerId != null
                        ? defendingPlayerId.equals(controllerId)
                        : gameQueryService.isPlayerBeingAttacked(gameData, controllerId));
            }
            case PermanentControlledContinuouslySinceBeginningOfTurnPredicate ignored ->
                    !permanent.isSummoningSick();
            case PermanentEnteredBattlefieldThisTurnPredicate ignored -> {
                if (gameData == null) {
                    yield false;
                }
                UUID currentCardId = permanent.getCard().getId();
                UUID originalCardId = permanent.getOriginalCard().getId();
                yield gameData.permanentsEnteredBattlefieldThisTurn.values().stream()
                        .flatMap(List::stream)
                        .anyMatch(card -> card.getId().equals(currentCardId)
                                || card.getId().equals(originalCardId));
            }
            case PermanentCrewedBySourceThisTurnPredicate ignored -> {
                Permanent sourcePermanent = filterContext == null
                        ? null : filterContext.sourcePermanentSnapshot();
                yield sourcePermanent != null
                        && sourcePermanent.getCreaturesThatCrewedThisTurn().contains(permanent.getId());
            }
            case PermanentEnteredBattlefieldThisOrLastTurnPredicate ignored -> {
                if (gameData == null || sourceControllerId == null
                        || gameData.turnsTakenByPlayer.getOrDefault(sourceControllerId, 0) < 2) {
                    yield false;
                }
                UUID currentCardId = permanent.getCard().getId();
                UUID originalCardId = permanent.getOriginalCard().getId();
                yield Stream.concat(
                                gameData.permanentsEnteredBattlefieldThisTurn.values().stream(),
                                gameData.permanentsEnteredBattlefieldLastTurn.values().stream())
                        .flatMap(List::stream)
                        .anyMatch(card -> card.getId().equals(currentCardId)
                                 || card.getId().equals(originalCardId));
            }
            case PermanentCastBySourceControllerThisTurnPredicate ignored -> {
                // "Target creature you cast this turn" — identity match against the spells the
                // source's controller cast this turn, so tokens and non-cast arrivals never match.
                if (gameData == null || sourceControllerId == null) {
                    yield false;
                }
                UUID cardId = permanent.getCard().getId();
                yield gameData.getSpellsCastThisTurn(sourceControllerId).stream()
                        .anyMatch(cast -> cast.getId().equals(cardId));
            }
            case PermanentOwnedBySourceControllerPredicate ignored -> {
                yield ownedBySourceController(permanent, filterContext);
            }
            case PermanentControllerControlsPermanentPredicate controllerControlsPredicate -> {
                if (gameData == null) {
                    yield false;
                }
                UUID targetController = gameData.findControllerOf(permanent);
                List<Permanent> targetBattlefield = targetController == null ? null
                        : gameData.playerBattlefields.get(targetController);
                yield targetBattlefield != null && targetBattlefield.stream()
                        .filter(p -> !controllerControlsPredicate.excludeSelf() || !p.getId().equals(permanent.getId()))
                        .anyMatch(p -> matchesPermanentPredicate(p, controllerControlsPredicate.filter(), filterContext));
            }
            case PermanentControllerControlsPermanentCountAtMostPredicate countPredicate -> {
                if (gameData == null) {
                    yield false;
                }
                UUID targetController = gameData.findControllerOf(permanent);
                List<Permanent> targetBattlefield = targetController == null ? null
                        : gameData.playerBattlefields.get(targetController);
                if (targetBattlefield == null) {
                    yield false;
                }
                long matchingCount = targetBattlefield.stream()
                        .filter(p -> matchesPermanentPredicate(p, countPredicate.countFilter(), filterContext))
                        .limit((long) countPredicate.maxCount() + 1)
                        .count();
                yield matchingCount <= countPredicate.maxCount();
            }
            case PermanentControllerPoisonCountersAtLeastPredicate poisonPredicate -> {
                if (gameData == null) {
                    yield false;
                }
                UUID targetController = gameData.findControllerOf(permanent);
                yield targetController != null
                        && gameData.playerPoisonCounters.getOrDefault(targetController, 0)
                        >= poisonPredicate.minimumPoisonCounters();
            }
            case PermanentAttachedToSourceControllerPredicate ignored ->
                    sourceControllerId != null && permanent.isAttached()
                            && sourceControllerId.equals(permanent.getAttachedTo());
            case PermanentToughnessLessThanSourcePowerPredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                if (sourcePermanent == null) {
                    yield false;
                }
                int sourcePower = gameQueryService.getEffectivePower(gameData, sourcePermanent);
                int targetToughness = gameQueryService.getEffectiveToughness(gameData, permanent);
                yield targetToughness < sourcePower;
            }
            case PermanentPowerAtMostSourceCountersPredicate countersPredicate -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                // CR 608.2b: the source is sacrificed to pay the ability's cost, so at the
                // resolution-time re-check its last known information supplies the counter count.
                Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                if (sourcePermanent == null && filterContext != null) {
                    sourcePermanent = filterContext.sourcePermanentSnapshot();
                }
                if (sourcePermanent == null) {
                    yield false;
                }
                int counters = sourcePermanent.getCounterCount(countersPredicate.counterType());
                yield gameQueryService.getEffectivePower(gameData, permanent) <= counters;
            }
            case PermanentPowerAtMostSourcePowerPredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                // "target creature with power <= this creature's power" (Earthshaker Khenra). When the
                // source is already on the battlefield (e.g. the 4/4 Eternalize token choosing its
                // target at trigger time), use its effective power. During cast-time ETB validation the
                // source isn't a permanent yet, so fall back to the source card's base power.
                Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                Integer sourcePower = sourcePermanent != null
                        ? gameQueryService.getEffectivePower(gameData, sourcePermanent)
                        : basePowerOfCardInAnyZone(gameData, sourceCardId);
                if (sourcePower == null) {
                    yield false;
                }
                int targetPower = gameQueryService.getEffectivePower(gameData, permanent);
                yield targetPower <= sourcePower;
            }
            case PermanentPowerLessThanSourcePowerPredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                if (sourcePermanent == null && filterContext != null) {
                    sourcePermanent = filterContext.sourcePermanentSnapshot();
                }
                if (sourcePermanent == null) {
                    yield false;
                }
                int sourcePower = gameQueryService.getEffectivePower(gameData, sourcePermanent);
                int targetPower = gameQueryService.getEffectivePower(gameData, permanent);
                yield targetPower < sourcePower;
            }
            case PermanentBlockedBySourcePredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                // CR 608.2b: once the source has left the battlefield (e.g. sacrificed to pay the
                // ability's cost), its last known information answers "creature it's blocking".
                Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                if (sourcePermanent == null && filterContext != null) {
                    sourcePermanent = filterContext.sourcePermanentSnapshot();
                }
                yield sourcePermanent != null
                        && sourcePermanent.isBlocking()
                        && sourcePermanent.getBlockingTargetIds().contains(permanent.getId());
            }
            case PermanentBlockedBySourceThisTurnPredicate ignored -> {
                if (gameData == null) {
                    yield false;
                }
                UUID sourcePermanentId = filterContext != null ? filterContext.sourcePermanentId() : null;
                if (sourcePermanentId == null && filterContext != null
                        && filterContext.sourcePermanentSnapshot() != null) {
                    sourcePermanentId = filterContext.sourcePermanentSnapshot().getId();
                }
                if (sourcePermanentId == null && sourceCardId != null) {
                    Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                    if (sourcePermanent != null) {
                        sourcePermanentId = sourcePermanent.getId();
                    }
                }
                yield sourcePermanentId != null
                        && gameData.creaturesBlockedThisTurn.contains(permanent.getId())
                        && gameData.combatBlockOpponentIdsThisTurn
                                .getOrDefault(permanent.getId(), java.util.Set.of())
                                .contains(sourcePermanentId);
            }
            case PermanentBlockingSourcePredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                // CR 608.2b: once the source has left the battlefield (e.g. sacrificed to pay the
                // ability's cost), its last known information answers "creature blocking it".
                Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                if (sourcePermanent == null && filterContext != null) {
                    sourcePermanent = filterContext.sourcePermanentSnapshot();
                }
                if (sourcePermanent == null) {
                    yield false;
                }
                // On an attached Aura the ability speaks about the enchanted creature ("creatures
                // blocking enchanted creature", Coils of the Medusa): an Aura is never blocked itself.
                UUID blockedId = sourcePermanent.getCard().isAura() && sourcePermanent.isAttached()
                        ? sourcePermanent.getAttachedTo()
                        : sourcePermanent.getId();
                yield permanent.isBlocking()
                        && permanent.getBlockingTargetIds().contains(blockedId);
            }
            case PermanentThatSaddledSourceThisTurnPredicate ignored -> {
                if (gameData == null) {
                    yield false;
                }
                UUID sourcePermanentId = filterContext != null ? filterContext.sourcePermanentId() : null;
                if (sourcePermanentId == null && filterContext != null
                        && filterContext.sourcePermanentSnapshot() != null) {
                    sourcePermanentId = filterContext.sourcePermanentSnapshot().getId();
                }
                if (sourcePermanentId == null && sourceCardId != null) {
                    Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                    if (sourcePermanent != null) {
                        sourcePermanentId = sourcePermanent.getId();
                    }
                }
                yield sourcePermanentId != null
                        && gameData.creaturesThatSaddledPermanentThisTurn
                                .getOrDefault(sourcePermanentId, java.util.Set.of())
                                .contains(permanent.getId());
            }
            case PermanentInCombatWithSourcePredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                Permanent sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                boolean sourceLeftBattlefield = sourcePermanent == null;
                if (sourcePermanent == null && filterContext != null) {
                    sourcePermanent = filterContext.sourcePermanentSnapshot();
                }
                if (sourcePermanent == null) {
                    yield false;
                }
                // Target is blocking source
                if (permanent.isBlocking() && permanent.getBlockingTargetIds().contains(sourcePermanent.getId())) {
                    yield true;
                }
                // Source is blocking target
                if (sourcePermanent.isBlocking() && sourcePermanent.getBlockingTargetIds().contains(permanent.getId())) {
                    yield true;
                }
                // A death trigger may resolve after combat cleanup removed the dead attacker's ID
                // from surviving blockers. The current-combat block record preserves that relationship.
                yield sourceLeftBattlefield
                        && filterContext != null
                        && filterContext.sourcePermanentSnapshot() != null
                        && gameData.combatBlockOpponentIdsThisCombat
                                .getOrDefault(permanent.getId(), java.util.Set.of())
                                .contains(sourcePermanent.getId());
            }
            case PermanentHasSameNameAsSourcePredicate ignored -> {
                if (gameData == null || sourceCardId == null) {
                    yield false;
                }
                // Find the source permanent by its current card ID (important for clones
                // where card differs from originalCard)
                Permanent sourcePermanent = findPermanentByCurrentCardId(gameData, sourceCardId);
                if (sourcePermanent == null) {
                    yield false;
                }
                yield namesMatch(
                        effectiveName(permanent, filterContext),
                        effectiveName(sourcePermanent, filterContext));
            }
            case PermanentSharesNameWithAnotherPermanentPredicate ignored -> {
                if (gameData == null) {
                    yield false;
                }
                String name = effectiveName(permanent, filterContext);
                boolean[] foundOther = {false};
                gameData.forEachBattlefield((playerId, battlefield) -> {
                    if (foundOther[0]) return;
                    for (Permanent other : battlefield) {
                        if (!other.getId().equals(permanent.getId())
                                && namesMatch(name, effectiveName(other, filterContext))) {
                            foundOther[0] = true;
                            return;
                        }
                    }
                });
                yield foundOther[0];
            }
            case PermanentHasSourceChosenSubtypePredicate ignored -> {
                Permanent sourcePermanent = filterContext == null ? null : filterContext.sourcePermanentSnapshot();
                if (sourcePermanent == null && gameData != null && sourceCardId != null) {
                    sourcePermanent = findPermanentByCurrentCardId(gameData, sourceCardId);
                }
                CardSubtype chosenSubtype = sourcePermanent == null ? null : sourcePermanent.getChosenSubtype();
                yield chosenSubtype != null
                        && matchesPermanentPredicate(permanent, new PermanentHasSubtypePredicate(chosenSubtype), filterContext);
            }
            case PermanentHasSourceChosenNamePredicate ignored -> {
                Permanent sourcePermanent = filterContext == null ? null : filterContext.sourcePermanentSnapshot();
                if (sourcePermanent == null && gameData != null && sourceCardId != null) {
                    sourcePermanent = findPermanentByCurrentCardId(gameData, sourceCardId);
                }
                String chosenName = sourcePermanent == null ? null : sourcePermanent.getChosenName();
                yield chosenName != null && chosenName.equals(effectiveName(permanent, filterContext));
            }
            case PermanentHasSourceChosenColorPredicate ignored -> {
                Permanent sourcePermanent = filterContext == null ? null : filterContext.sourcePermanentSnapshot();
                if (sourcePermanent == null && gameData != null && sourceCardId != null) {
                    sourcePermanent = findPermanentByCurrentCardId(gameData, sourceCardId);
                }
                if (sourcePermanent == null) {
                    yield false;
                }
                CardColor chosenColor = sourcePermanent.getChosenColor();
                yield chosenColor != null
                        && matchesPermanentPredicate(permanent,
                                new PermanentColorInPredicate(Set.of(chosenColor)), filterContext);
            }
            case PermanentNamedPredicate namedPredicate ->
                    namesMatch(effectiveName(permanent, filterContext), namedPredicate.cardName());
            case PermanentNameInPredicate nameInPredicate -> {
                String name = effectiveName(permanent, filterContext);
                yield name != null && nameInPredicate.cardNames().contains(name);
            }
            case PermanentHasCountersPredicate hasCountersPredicate -> {
                if (hasCountersPredicate.counterType() == CounterType.ANY) {
                    boolean any = false;
                    for (CounterType type : CounterType.values()) {
                        if (type == CounterType.ANY || type == CounterType.SILVER) continue;
                        if (permanent.getCounterCount(type) > 0) {
                            any = true;
                            break;
                        }
                    }
                    yield any;
                }
                yield permanent.getCounterCount(hasCountersPredicate.counterType()) > 0;
            }
            case PermanentHasAtLeastCountersPredicate atLeastCountersPredicate ->
                    permanent.getCounterCount(atLeastCountersPredicate.counterType())
                            >= atLeastCountersPredicate.minimum();
            case PermanentCounterCountAtLeastPredicate counterCountPredicate ->
                    permanent.getCounterCount(counterCountPredicate.counterType()) >= counterCountPredicate.threshold();
            case PermanentHasCumulativeUpkeepPredicate ignored -> permanent.hasCumulativeUpkeep();
            case PermanentDealtDamageThisTurnPredicate ignored ->
                    gameData != null && gameData.permanentsDealtDamageThisTurn.contains(permanent.getId());
            case PermanentDealtDamageToAnythingThisTurnPredicate ignored -> {
                if (gameData == null) {
                    yield false;
                }
                Set<UUID> combatVictims = gameData.combatDamageToPlayersThisTurn.get(permanent.getId());
                Set<UUID> noncombatVictims = gameData.noncombatDamageToPlayersThisTurn.get(permanent.getId());
                Set<UUID> damagedCreatures =
                        gameData.creatureCardsDamagedThisTurnBySourcePermanent.get(permanent.getId());
                yield (combatVictims != null && !combatVictims.isEmpty())
                        || (noncombatVictims != null && !noncombatVictims.isEmpty())
                        || (damagedCreatures != null && !damagedCreatures.isEmpty());
            }
            case PermanentDealtDamageToSourceControllerThisTurnPredicate ignored -> {
                if (sourceControllerId == null || gameData == null) {
                    yield false;
                }
                Set<UUID> combatVictims = gameData.combatDamageToPlayersThisTurn.get(permanent.getId());
                Set<UUID> noncombatVictims = gameData.noncombatDamageToPlayersThisTurn.get(permanent.getId());
                yield (combatVictims != null && combatVictims.contains(sourceControllerId))
                        || (noncombatVictims != null && noncombatVictims.contains(sourceControllerId));
            }
            case PermanentAttackedSourceControllerThisTurnPredicate ignored -> {
                if (sourceControllerId == null || gameData == null) {
                    yield false;
                }
                Set<UUID> attackedPlayers = gameData.playersAttackedThisTurn.get(permanent.getId());
                yield attackedPlayers != null && attackedPlayers.contains(sourceControllerId);
            }
            case PermanentTruePredicate ignored ->
                    true;
            case PermanentHasGreatestManaValueAmongAllCreaturesPredicate ignored ->
                    gameQueryService.hasGreatestManaValueAmongAllCreatures(gameData, permanent);
            case PermanentHasGreatestManaValueAmongAllArtifactsPredicate ignored ->
                    gameQueryService.hasGreatestManaValueAmongAllArtifacts(gameData, permanent);
            case PermanentHasGreatestManaValueAmongControllerCreaturesOrPlaneswalkersPredicate ignored ->
                    gameQueryService.hasGreatestManaValueAmongControllerCreaturesOrPlaneswalkers(gameData, permanent);
            case PermanentHasGreatestPowerAmongAllCreaturesPredicate ignored ->
                    gameQueryService.hasGreatestPowerAmongAllCreatures(gameData, permanent);
            case PermanentHasLowestManaValueAmongAllNonlandPermanentsPredicate ignored ->
                    gameQueryService.hasLowestManaValueAmongAllNonlandPermanents(gameData, permanent);
            case PermanentHasGreatestPowerAmongControllerCreaturesPredicate ignored -> {
                if (gameData == null || !gameQueryService.isCreature(gameData, permanent)) {
                    yield false;
                }
                UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                if (controllerId == null) {
                    controllerId = sourceControllerId;
                }
                List<Permanent> controllerBattlefield = controllerId == null
                        ? null
                        : gameData.playerBattlefields.get(controllerId);
                if (controllerBattlefield == null) {
                    yield false;
                }
                int maxPower = Math.max(gameQueryService.getEffectivePower(gameData, permanent),
                        controllerBattlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .mapToInt(p -> gameQueryService.getEffectivePower(gameData, p))
                        .max().orElse(0));
                yield gameQueryService.getEffectivePower(gameData, permanent) == maxPower;
            }
            case PermanentHasLeastPowerAmongAllCreaturesPredicate ignored ->
                    gameQueryService.hasLeastPowerAmongAllCreatures(gameData, permanent);
            case PermanentHasGreatestPowerAmongControlledCreaturesPredicate ignored -> {
                if (gameData == null || sourceControllerId == null) yield false;
                List<Permanent> controllerBf = gameData.playerBattlefields.get(sourceControllerId);
                if (controllerBf == null || !controllerBf.contains(permanent)) yield false;
                if (!gameQueryService.isCreature(gameData, permanent)) yield false;
                int maxPower = controllerBf.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .mapToInt(p -> gameQueryService.getEffectivePower(gameData, p))
                        .max().orElse(0);
                yield gameQueryService.getEffectivePower(gameData, permanent) == maxPower;
            }
        };
    }

    private boolean attacksPlayerWithMostLife(GameData gameData, Permanent permanent) {
        if (gameData == null || !permanent.isAttacking()
                || !gameData.playerIds.contains(permanent.getAttackTarget())) {
            return false;
        }
        int attackedPlayerLife = gameData.getLife(permanent.getAttackTarget());
        return gameData.orderedPlayerIds.stream()
                .allMatch(playerId -> attackedPlayerLife >= gameData.getLife(playerId));
    }

    private boolean attacksWhileSourceControllerHasMostLife(GameData gameData, Permanent permanent,
                                                             UUID sourceControllerId) {
        if (gameData == null || sourceControllerId == null || !permanent.isAttacking()
                || !gameData.playerIds.contains(permanent.getAttackTarget())) {
            return false;
        }
        int controllerLife = gameData.getLife(sourceControllerId);
        return gameData.orderedPlayerIds.stream()
                .allMatch(playerId -> controllerLife >= gameData.getLife(playerId));
    }

    /** Whether a static amount filter needs the live board to evaluate permanent ownership. */
    public boolean requiresGameDataForStaticFilter(PermanentPredicate predicate) {
        if (predicate instanceof PermanentOwnedBySourceControllerPredicate) {
            return true;
        }
        if (predicate instanceof PermanentHasGreatestManaValueAmongControllerCreaturesOrPlaneswalkersPredicate) {
            return true;
        }
        if (predicate instanceof PermanentIsModifiedPredicate) {
            return true;
        }
        if (predicate instanceof PermanentIsEnchantedBySourceControllerAuraPredicate) {
            return true;
        }
        if (predicate instanceof PermanentIsAuraAttachedToCreaturePredicate) {
            return true;
        }
        if (predicate instanceof PermanentNotPredicate notPredicate) {
            return requiresGameDataForStaticFilter(notPredicate.predicate());
        }
        if (predicate instanceof PermanentAllOfPredicate allOf) {
            return allOf.predicates().stream().anyMatch(this::requiresGameDataForStaticFilter);
        }
        if (predicate instanceof PermanentAnyOfPredicate anyOf) {
            return anyOf.predicates().stream().anyMatch(this::requiresGameDataForStaticFilter);
        }
        return false;
    }

    private boolean sharesCardTypeWithSourcePermanent(Permanent permanent, FilterContext filterContext) {
        if (filterContext == null || filterContext.sourcePermanentSnapshot() == null) {
            return false;
        }
        Permanent source = filterContext.sourcePermanentSnapshot();
        GameData gameData = filterContext.gameData();
        boolean permanentArtifact = gameData == null
                ? gameQueryService.isArtifact(permanent)
                : gameQueryService.isArtifact(gameData, permanent);
        boolean sourceArtifact = gameData == null
                ? gameQueryService.isArtifact(source)
                : gameQueryService.isArtifact(gameData, source);
        boolean permanentCreature = gameData == null
                ? permanent.getCard().hasType(CardType.CREATURE)
                : gameQueryService.isCreature(gameData, permanent);
        boolean sourceCreature = gameData == null
                ? source.getCard().hasType(CardType.CREATURE)
                : gameQueryService.isCreature(gameData, source);
        boolean permanentEnchantment = gameData == null
                ? gameQueryService.isEnchantment(permanent)
                : gameQueryService.isEnchantment(gameData, permanent);
        boolean sourceEnchantment = gameData == null
                ? gameQueryService.isEnchantment(source)
                : gameQueryService.isEnchantment(gameData, source);
        return (permanentArtifact && sourceArtifact)
                || (permanentCreature && sourceCreature)
                || (permanentEnchantment && sourceEnchantment);
    }

    private boolean isCreatureForStaticEvaluation(Permanent permanent) {
        CharacteristicState layered = LayerSystemService.activeStateFor(permanent.getId());
        if (layered != null) {
            return layered.hasCardType(CardType.CREATURE);
        }
        return permanent.getCard().hasType(CardType.CREATURE)
                || permanent.isAnimatedUntilEndOfTurn()
                || permanent.isAnimatedUntilEndOfCombat()
                || permanent.isAnimatedUntilNextTurn()
                || permanent.isPermanentlyAnimated()
                || permanent.getCounterCount(CounterType.AWAKENING) > 0;
    }

    /**
     * Recursion-safe variant for callers running inside static-bonus assembly: the static effect
     * handlers and {@link com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService}.
     * The fully layered {@link #matchesPermanentPredicate(Permanent, PermanentPredicate, FilterContext)}
     * would re-enter {@code GameQueryService.computeStaticBonus}, which is what is currently being
     * computed, so every leaf is answered from the in-flight {@link CharacteristicState} while a
     * pass is active and from the permanent's own stored state otherwise.
     *
     * <p>A {@code null} predicate matches — this evaluates scope filters, where "no filter" means
     * "every permanent in scope", not the "no predicate, no match" of a target predicate.
     *
     * <p>Only the predicates that have a recursion-safe answer are accepted. The rest throw rather
     * than degrading to the {@code null}-GameData fallback, which for a predicate that genuinely
     * needs the board (controlled-by-source, is-blocked) is a silent {@code false} — a wrong answer
     * dressed as a legitimate one.
     *
     * <p>The context supplies the board shape and the source's identity, which some predicates
     * need to answer at all. It is deliberately <em>not</em> passed down to the characteristic
     * leaves: those stay on the recursion-safe path whether or not a {@code GameData} is at hand,
     * because having one is exactly what would let them re-enter the assembly.
     */
    public boolean matchesStaticFilter(Permanent permanent, PermanentPredicate predicate, FilterContext context) {
        if (predicate == null) return true;
        // CR 613.6: when this exact filter instance was already evaluated by the layer-4 pass
        // (effect parts of one printed ability share the filter object), every later-layer part
        // applies to the layer-4-determined set — re-evaluating against the finished states
        // would let a self-referencing filter (Bludgeon Brawl's "non-Equipment artifact")
        // negate its own output.
        Boolean layer4Verdict = LayerSystemService.activeL4FilterVerdict(predicate, permanent.getId());
        if (layer4Verdict != null) {
            return layer4Verdict;
        }
        return switch (predicate) {
            case PermanentNotPredicate p -> !matchesStaticFilter(permanent, p.predicate(), context);
            case PermanentControlledBySourceControllerPredicate ignored -> {
                GameData gameData = context == null ? null : context.gameData();
                UUID sourceControllerId = context == null ? null : context.sourceControllerId();
                yield gameData != null && sourceControllerId != null
                        && sourceControllerId.equals(gameData.findControllerOf(permanent));
            }
            case PermanentOwnedBySourceControllerPredicate ignored -> {
                GameData gameData = context == null ? null : context.gameData();
                UUID sourceControllerId = context == null ? null : context.sourceControllerId();
                UUID currentControllerId = gameData == null ? null : gameData.findControllerOf(permanent);
                UUID ownerId = gameData == null ? null : gameData.defaultControllerOf(permanent.getId());
                yield sourceControllerId != null && sourceControllerId.equals(ownerId)
                        && currentControllerId != null;
            }
            case PermanentAllOfPredicate p ->
                    p.predicates().stream().allMatch(nested -> matchesStaticFilter(permanent, nested, context));
            case PermanentAnyOfPredicate p ->
                    p.predicates().stream().anyMatch(nested -> matchesStaticFilter(permanent, nested, context));
            case PermanentIsEnchantedPredicate ignored -> {
                GameData gameData = context == null ? null : context.gameData();
                yield gameData != null && gameQueryService.isEnchanted(gameData, permanent);
            }
            case PermanentIsEnchantedBySourceControllerAuraPredicate ignored ->
                    hasAuraControlledBySourceControllerAttachedTo(
                            context == null ? null : context.gameData(), permanent,
                            context == null ? null : context.sourceControllerId());
            case PermanentIsAuraAttachedToCreaturePredicate ignored -> {
                GameData gameData = context == null ? null : context.gameData();
                if (gameData == null || !permanent.getCard().isAura() || !permanent.isAttached()) {
                    yield false;
                }
                Permanent host = gameQueryService.findPermanentById(gameData, permanent.getAttachedTo());
                yield host != null && isCreatureForStaticEvaluation(host);
            }
            case PermanentControllerControlsPermanentPredicate p ->
                    controllerControlsMatchingStatic(permanent, p, context);
            case PermanentControllerControlsPermanentCountAtMostPredicate p ->
                    controllerControlsAtMostMatchingStatic(permanent, p, context);
            case PermanentHasGreatestManaValueAmongAllCreaturesPredicate ignored ->
                    hasGreatestManaValueAmongAllCreaturesStatic(permanent, context);
            case PermanentHasGreatestManaValueAmongAllArtifactsPredicate ignored ->
                    hasGreatestManaValueAmongAllArtifactsStatic(permanent, context);
            case PermanentHasGreatestManaValueAmongControllerCreaturesOrPlaneswalkersPredicate ignored ->
                    hasGreatestManaValueAmongControllerCreaturesOrPlaneswalkersStatic(permanent, context);
            case PermanentInCombatWithSourcePredicate ignored -> inCombatWithSourceStatic(permanent, context);
            case PermanentHasSourceChosenSubtypePredicate ignored -> {
                CardSubtype chosen = sourceChosenSubtype(context);
                yield chosen != null
                        && matchesStaticLeaf(permanent, new PermanentHasSubtypePredicate(chosen));
            }
            case PermanentHasSourceChosenNamePredicate ignored -> {
                Permanent source = context == null ? null : context.sourcePermanentSnapshot();
                String chosenName = source == null ? null : source.getChosenName();
                yield chosenName != null && chosenName.equals(effectiveName(permanent, context));
            }
            case PermanentHasSourceChosenColorPredicate ignored -> {
                CardColor chosen = sourceChosenColor(context);
                yield chosen != null
                        && matchesStaticLeaf(permanent, new PermanentColorInPredicate(Set.of(chosen)));
            }
            case PermanentColorInPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentHasAnySubtypePredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentHasCountersPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentHasAtLeastCountersPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentCounterCountAtLeastPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentHasKeywordPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentHasSubtypePredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentHasAdventurePredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentHasSupertypePredicate p -> gameQueryService.hasEffectiveSupertype(
                    context == null ? null : context.gameData(), permanent, p.supertype());
            case PermanentIsArtifactPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsAttackingPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentAttackedDuringControllersLastTurnPredicate ignored ->
                    matchesStaticLeaf(permanent, predicate);
            case PermanentIsAttackingOpponentOfSourceControllerPredicate ignored -> {
                GameData gameData = context == null ? null : context.gameData();
                UUID sourceControllerId = context == null ? null : context.sourceControllerId();
                yield permanent.isAttacking() && sourceControllerId != null && gameData != null
                        && gameData.playerIds.contains(permanent.getAttackTarget())
                        && !sourceControllerId.equals(permanent.getAttackTarget());
            }
            case PermanentIsAttackingSourceControllerPredicate ignored -> {
                // Recursion-safe: attack state and attack target are stored on the permanent, so
                // "creatures attacking you" only needs the source controller from the context
                // (Boarded Window, Watchdog).
                UUID sourceControllerId = context == null ? null : context.sourceControllerId();
                yield permanent.isAttacking() && sourceControllerId != null
                        && sourceControllerId.equals(permanent.getAttackTarget());
            }
            case PermanentIsBlockingPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsCreaturePredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsEnchantmentPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsEquippedPredicate ignored -> {
                GameData gameData = context == null ? null : context.gameData();
                yield gameData != null && gameQueryService.isEquipped(gameData, permanent);
            }
            case PermanentIsModifiedPredicate ignored ->
                    isModified(context == null ? null : context.gameData(), permanent, context);
            case PermanentIsFaceDownPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsHistoricPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsKindredPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsAuraAttachedToPermanentControlledBySourceControllerPredicate ignored -> {
                GameData gameData = context == null ? null : context.gameData();
                UUID sourceControllerId = context == null ? null : context.sourceControllerId();
                if (gameData == null || sourceControllerId == null
                        || !permanent.getCard().isAura() || !permanent.isAttached()) {
                    yield false;
                }
                Permanent host = gameQueryService.findPermanentById(gameData, permanent.getAttachedTo());
                yield host != null && sourceControllerId.equals(gameData.findControllerOf(host));
            }
            case PermanentIsHostOfSourceAuraPredicate ignored -> {
                // Recursion-safe: attachment state is stored on the source snapshot, not derived
                // through computeStaticBonus. Used by Vampirism-style "other than enchanted creature".
                Permanent sourceAura = context == null ? null : context.sourcePermanentSnapshot();
                yield sourceAura != null && sourceAura.isAttached()
                        && sourceAura.getAttachedTo().equals(permanent.getId());
            }
            case PermanentAttachedToSourcePermanentPredicate ignored -> {
                GameData gameData = context == null ? null : context.gameData();
                UUID sourceCardId = context == null ? null : context.sourceCardId();
                Permanent sourcePermanent = null;
                if (gameData != null && context != null && context.sourcePermanentId() != null) {
                    sourcePermanent = gameQueryService.findPermanentById(gameData, context.sourcePermanentId());
                }
                if (sourcePermanent == null && context != null) {
                    sourcePermanent = context.sourcePermanentSnapshot();
                }
                if (sourcePermanent == null && gameData != null && sourceCardId != null) {
                    sourcePermanent = findPermanentByOriginalCardId(gameData, sourceCardId);
                }
                yield sourcePermanent != null && permanent.isAttached()
                        && sourcePermanent.getId().equals(permanent.getAttachedTo());
            }
            case PermanentIsSourcePermanentPredicate ignored -> {
                UUID sourcePermanentId = context == null ? null : context.sourcePermanentId();
                if (sourcePermanentId != null) {
                    yield sourcePermanentId.equals(permanent.getId());
                }
                Permanent source = context == null ? null : context.sourcePermanentSnapshot();
                yield source != null && source.getId().equals(permanent.getId());
            }
            case PermanentSharesColorWithEquippedCreaturePredicate ignored -> {
                // Recursion-safe: both colour sets come from the in-flight layer state (or the
                // permanents' own stored colours), never from computeStaticBonus. Konda's Banner.
                Permanent equipped = equippedCreatureStatic(context);
                if (equipped == null) {
                    yield false;
                }
                Set<CardColor> equippedColors = recursionSafeColors(equipped);
                yield !equippedColors.isEmpty()
                        && recursionSafeColors(permanent).stream().anyMatch(equippedColors::contains);
            }
            case PermanentSharesCreatureTypeWithEquippedCreaturePredicate ignored -> {
                Permanent equipped = equippedCreatureStatic(context);
                yield equipped != null && recursionSafeSharesCreatureType(permanent, equipped);
            }
            case PermanentIsLandPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsMulticoloredPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsPlaneswalkerPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsRenownedPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsSuspectedPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsTappedPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsTokenPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentIsTransformedPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentNamedPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentNameInPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            // Recursion-safe: asking GameQueryService.hasProtectionFrom would re-enter the
            // static-bonus assembly that is currently running for another permanent.
            case PermanentHasProtectionFromColorPredicate p -> hasRecursionSafeProtectionFrom(permanent, p.color());
            case PermanentPowerAtLeastPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentPowerAtMostPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentMaxManaValuePredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentToughnessAtMostPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentToughnessGreaterThanPowerPredicate ignored -> matchesStaticLeaf(permanent, predicate);
            case PermanentTruePredicate ignored -> matchesStaticLeaf(permanent, predicate);
            default -> throw new IllegalArgumentException(
                    "Unsupported static filter predicate: " + predicate.getClass().getSimpleName());
        };
    }

    /**
     * One recursion-safe leaf, bypassing the CR 613.6 layer-4 verdict memo that
     * {@link #matchesStaticFilter} consults. Use this for a predicate the evaluator or a caller
     * builds itself rather than one taken from a card's ability: the memo is keyed by filter
     * instance and most leaf predicates are component-less records, so a locally constructed
     * instance compares equal to some unrelated ability's filter and would collect that ability's
     * verdict. Only a filter that really is the ability's own may consult the memo.
     */
    /**
     * Protection from a color without consulting {@code GameQueryService.computeStaticBonus}:
     * the in-flight layer-6 state when a CR 613 pass is running (it already carries the
     * permanent's own printed protection plus any grants applied so far), the printed static
     * ability otherwise, plus until-end-of-turn grants either way.
     */
    private boolean hasRecursionSafeProtectionFrom(Permanent permanent, CardColor color) {
        if (color == null) return false;
        if (permanent.getProtectionFromColorsUntilEndOfTurn().contains(color)) return true;
        CharacteristicState layered = LayerSystemService.activeStateFor(permanent.getId());
        if (layered != null) {
            return layered.hasProtectionColor(color);
        }
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionGrantingEffect protection
                    && protection.protectionScope() == null
                    && protection.protectionFromColors().contains(color)) {
                return true;
            }
        }
        return false;
    }

    private boolean ownedBySourceController(Permanent permanent, FilterContext context) {
        if (context == null || context.sourceControllerId() == null || context.gameData() == null) {
            return false;
        }
        GameData gameData = context.gameData();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(permanent)) {
                UUID ownerId = gameData.stolenCreatures.getOrDefault(permanent.getId(), playerId);
                return ownerId.equals(context.sourceControllerId());
            }
        }
        return false;
    }

    public boolean matchesStaticLeaf(Permanent permanent, PermanentPredicate predicate) {
        return matchesPermanentPredicate(
                LayerSystemService.activeStateFor(permanent.getId()), permanent, predicate, null);
    }

    private boolean isModified(GameData gameData, Permanent permanent, FilterContext filterContext) {
        for (CounterType type : CounterType.values()) {
            if (type != CounterType.ANY && type != CounterType.SILVER
                    && permanent.getCounterCount(type) > 0) {
                return true;
            }
        }
        if (gameData == null) {
            return false;
        }
        if (gameQueryService.isEquipped(gameData, permanent)) {
            return true;
        }

        UUID controllerId = gameData.findControllerOf(permanent);
        if (controllerId == null && filterContext != null) {
            controllerId = filterContext.sourceControllerId();
        }
        if (controllerId == null) {
            return false;
        }
        UUID effectiveControllerId = controllerId;
        return gameData.anyPermanentMatches(aura ->
                aura.getCard().isAura()
                        && aura.isAttached()
                        && aura.getAttachedTo().equals(permanent.getId())
                        && effectiveControllerId.equals(gameData.findControllerOf(aura)));
    }

    private boolean hasAuraControlledBySourceControllerAttachedTo(
            GameData gameData, Permanent permanent, UUID sourceControllerId) {
        if (gameData == null || permanent == null || sourceControllerId == null) {
            return false;
        }
        if (gameData.anyPermanentMatches(aura -> aura.getCard().isAura()
                && aura.isAttached()
                && aura.getAttachedTo().equals(permanent.getId())
                && sourceControllerId.equals(gameData.findControllerOf(aura)))) {
            return true;
        }
        return gameData.simultaneousDyingCreatures.entrySet().stream()
                .filter(entry -> entry.getValue().getCard().isAura())
                .filter(entry -> entry.getValue().isAttached())
                .filter(entry -> entry.getValue().getAttachedTo().equals(permanent.getId()))
                .anyMatch(entry -> sourceControllerId.equals(
                        gameData.simultaneousDyingControllers.get(entry.getKey())));
    }

    /**
     * Recursion-safe "the target's own controller controls a matching permanent" (Favorable
     * Destiny's "as long as its controller controls another creature"). The inner filter is part
     * of the ability's own predicate, so it goes back through the funnel rather than
     * {@link #matchesStaticLeaf}.
     */
    private boolean controllerControlsMatchingStatic(Permanent target,
                                                     PermanentControllerControlsPermanentPredicate predicate,
                                                     FilterContext context) {
        GameData gameData = context == null ? null : context.gameData();
        if (gameData == null) return false;
        UUID controllerId = gameData.findControllerOf(target);
        List<Permanent> battlefield = controllerId == null ? null : gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .filter(candidate -> !predicate.excludeSelf() || !candidate.getId().equals(target.getId()))
                .anyMatch(candidate -> matchesStaticFilter(candidate, predicate.filter(), context));
    }

    /** Recursion-safe count of permanents matching a filter on the tested permanent's battlefield. */
    private boolean controllerControlsAtMostMatchingStatic(
            Permanent target, PermanentControllerControlsPermanentCountAtMostPredicate predicate,
            FilterContext context) {
        GameData gameData = context == null ? null : context.gameData();
        if (gameData == null) return false;
        UUID controllerId = gameData.findControllerOf(target);
        List<Permanent> battlefield = controllerId == null ? null : gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        long matchingCount = battlefield.stream()
                .filter(candidate -> matchesStaticFilter(candidate, predicate.countFilter(), context))
                .limit((long) predicate.maxCount() + 1)
                .count();
        return matchingCount <= predicate.maxCount();
    }

    /**
     * The creature the source Equipment is attached to, on the fully layered path, or {@code null}
     * while the Equipment is unattached or unfindable. The source is located by card id when the
     * board is at hand and falls back to the snapshot the caller carries.
     */
    private Permanent equippedCreatureOfSource(GameData gameData, UUID sourceCardId, FilterContext filterContext) {
        if (gameData == null) return null;
        Permanent equipment = sourceCardId == null ? null : findPermanentByOriginalCardId(gameData, sourceCardId);
        if (equipment == null && filterContext != null) {
            equipment = filterContext.sourcePermanentSnapshot();
        }
        if (equipment == null || !equipment.isAttached()) return null;
        return gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
    }

    /**
     * The creature the source Equipment is attached to, on the recursion-safe path: attachment
     * state lives on the snapshot the static pass carries, so no layered query is needed to find
     * the host.
     */
    private Permanent equippedCreatureStatic(FilterContext context) {
        Permanent equipment = context == null ? null : context.sourcePermanentSnapshot();
        GameData gameData = context == null ? null : context.gameData();
        if (equipment == null || gameData == null || !equipment.isAttached()) return null;
        return gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
    }

    /**
     * Resolves a permanent's effective name from the in-flight layered state when available. An
     * entering permanent has no layered state yet, so its intrinsic name is used while its
     * game's pass is being built instead of recursively starting another static-bonus assembly.
     */
    private String effectiveName(Permanent permanent, FilterContext context) {
        CharacteristicState layered = LayerSystemService.activeStateFor(permanent.getId());
        if (layered != null) {
            return layered.getName();
        }
        GameData gameData = context == null ? null : context.gameData();
        LayerSystemService.Pass activePass = LayerSystemService.activePass();
        if (gameData != null && activePass != null && activePass.gameData() == gameData) {
            // An entering permanent is not in the layered board yet. Falling back to the
            // fully layered query here would re-enter the same static-bonus assembly.
            return permanent.getCard().getName();
        }
        return gameData == null
                ? permanent.getCard().getName()
                : gameQueryService.getEffectiveName(gameData, permanent);
    }

    private static boolean namesMatch(String firstName, String secondName) {
        return firstName != null && firstName.equals(secondName);
    }

    private Set<CardColor> recursionSafeColors(Permanent permanent) {
        CharacteristicState layered = LayerSystemService.activeStateFor(permanent.getId());
        if (layered != null) {
            return layered.getColors();
        }
        if (permanent.isColorOverridden()) {
            return permanent.getTransientColors();
        }
        Set<CardColor> combined = EnumSet.noneOf(CardColor.class);
        combined.addAll(permanent.getEffectiveColors());
        combined.addAll(permanent.getTransientColors());
        combined.addAll(permanent.getGrantedColors());
        return combined;
    }

    /**
     * Recursion-safe counterpart of {@code GameQueryService.shareCreatureType}: Changeling counts
     * as every creature type, and a permanent with no creature types shares none.
     */
    private boolean recursionSafeSharesCreatureType(Permanent a, Permanent b) {
        boolean aChangeling = matchesStaticLeaf(a, CHANGELING_PREDICATE);
        boolean bChangeling = matchesStaticLeaf(b, CHANGELING_PREDICATE);
        Set<CardSubtype> aTypes = recursionSafeCreatureSubtypes(a);
        Set<CardSubtype> bTypes = recursionSafeCreatureSubtypes(b);
        if (aChangeling) {
            return bChangeling || !bTypes.isEmpty();
        }
        if (bChangeling) {
            return !aTypes.isEmpty();
        }
        return aTypes.stream().anyMatch(bTypes::contains);
    }

    /**
     * Named creature subtypes of a permanent without consulting {@code computeStaticBonus}
     * (Changeling is handled by the caller). Mirrors the {@code PermanentHasSubtypePredicate} leaf:
     * the in-flight layer-4 state when a pass is running, the permanent's own stored types
     * otherwise, honouring "becomes a [type]", "loses all creature types", and removed subtypes.
     */
    private Set<CardSubtype> recursionSafeCreatureSubtypes(Permanent permanent) {
        CharacteristicState layered = LayerSystemService.activeStateFor(permanent.getId());
        if (layered != null) {
            return layered.getSubtypes().stream()
                    .filter(gameQueryService::isCreatureSubtype)
                    .collect(java.util.stream.Collectors.toSet());
        }
        if (!permanent.getTransientCreatureTypeOverrides().isEmpty()) {
            return new HashSet<>(permanent.getTransientCreatureTypeOverrides());
        }
        if (permanent.getTransientCreatureTypeOverride() != null) {
            return Set.of(permanent.getTransientCreatureTypeOverride());
        }
        if (permanent.isLosesAllCreatureTypesUntilEndOfTurn()) {
            return Set.of();
        }
        Set<CardSubtype> result = new HashSet<>();
        result.addAll(permanent.getCard().getSubtypes());
        result.addAll(permanent.getTransientSubtypes());
        result.addAll(permanent.getGrantedSubtypes());
        result.removeAll(permanent.getTransientRemovedSubtypes());
        result.removeIf(subtype -> !gameQueryService.isCreatureSubtype(subtype));
        return result;
    }

    /**
     * Recursion-safe "blocking or blocked by the source" (Alms Beast's static lifelink grant).
     * Block assignments live on the permanents themselves, so the answer needs no layered
     * characteristic query; the source comes from the snapshot the static pass carries rather
     * than a battlefield lookup.
     */
    private boolean inCombatWithSourceStatic(Permanent permanent, FilterContext context) {
        Permanent source = context == null ? null : context.sourcePermanentSnapshot();
        if (source == null) return false;
        if (permanent.isBlocking() && permanent.getBlockingTargetIds().contains(source.getId())) {
            return true;
        }
        return source.isBlocking() && source.getBlockingTargetIds().contains(permanent.getId());
    }

    /**
     * Recursion-safe "greatest mana value among all creatures" (Favor of the Mighty).
     * {@link GameQueryService#hasGreatestManaValueAmongAllCreatures} calls the fully layered
     * {@code isCreature}, which re-enters static-bonus assembly. Mana value is a copiable
     * characteristic unaffected by layer 7, so the printed value is authoritative.
     */
    private boolean hasGreatestManaValueAmongAllCreaturesStatic(Permanent target, FilterContext context) {
        GameData gameData = context == null ? null : context.gameData();
        if (gameData == null || !matchesStaticLeaf(target, STATIC_CREATURE_LEAF)) {
            return false;
        }
        int greatest = -1;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent candidate : battlefield) {
                if (matchesStaticLeaf(candidate, STATIC_CREATURE_LEAF)) {
                    greatest = Math.max(greatest, candidate.getCard().getManaValue());
                }
            }
        }
        return target.getCard().getManaValue() == greatest;
    }

    private boolean hasGreatestManaValueAmongAllArtifactsStatic(Permanent target, FilterContext context) {
        GameData gameData = context == null ? null : context.gameData();
        if (gameData == null || !matchesStaticLeaf(target, new PermanentIsArtifactPredicate())) {
            return false;
        }
        int greatest = -1;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent candidate : battlefield) {
                if (matchesStaticLeaf(candidate, new PermanentIsArtifactPredicate())) {
                    greatest = Math.max(greatest, candidate.getCard().getManaValue());
                }
            }
        }
        return target.getCard().getManaValue() == greatest;
    }

    private boolean hasGreatestManaValueAmongControllerCreaturesOrPlaneswalkersStatic(
            Permanent target, FilterContext context) {
        GameData gameData = context == null ? null : context.gameData();
        if (gameData == null
                || (!matchesStaticLeaf(target, STATIC_CREATURE_LEAF)
                && !matchesStaticLeaf(target, new PermanentIsPlaneswalkerPredicate()))) {
            return false;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        int greatest = -1;
        for (Permanent candidate : battlefield) {
            if (matchesStaticLeaf(candidate, STATIC_CREATURE_LEAF)
                    || matchesStaticLeaf(candidate, new PermanentIsPlaneswalkerPredicate())) {
                greatest = Math.max(greatest, candidate.getCard().getManaValue());
            }
        }
        return target.getCard().getManaValue() == greatest;
    }

    /** The subtype the ability's source chose as it entered, or {@code null} if it made no choice. */
    private CardSubtype sourceChosenSubtype(FilterContext context) {
        if (context == null) return null;
        if (context.sourcePermanentSnapshot() != null) {
            return context.sourcePermanentSnapshot().getChosenSubtype();
        }
        if (context.gameData() == null || context.sourceCardId() == null) return null;
        Permanent source = findPermanentByCurrentCardId(context.gameData(), context.sourceCardId());
        return source == null ? null : source.getChosenSubtype();
    }

    private CardColor sourceChosenColor(FilterContext context) {
        if (context == null) return null;
        if (context.sourcePermanentSnapshot() != null) {
            return context.sourcePermanentSnapshot().getChosenColor();
        }
        if (context.gameData() == null || context.sourceCardId() == null) return null;
        Permanent source = findPermanentByCurrentCardId(context.gameData(), context.sourceCardId());
        return source == null ? null : source.getChosenColor();
    }

    private boolean hasNonManaActivatedAbility(GameData gameData, Permanent permanent, boolean levelUpOnly) {
        return effectiveActivatedAbilities(gameData, permanent).stream()
                .anyMatch(ability -> !AbilityActivationService.isManaAbility(ability)
                        && (!levelUpOnly || ability.isLevelUpAbility()));
    }

    private boolean hasManaAbility(GameData gameData, Permanent permanent) {
        return PotentialManaService.hasOnTapManaEffects(permanent.getCard())
                || effectiveActivatedAbilities(gameData, permanent).stream()
                .anyMatch(AbilityActivationService::isManaAbility);
    }

    private List<ActivatedAbility> effectiveActivatedAbilities(GameData gameData, Permanent permanent) {
        List<ActivatedAbility> abilities = new ArrayList<>();
        if (gameData == null) {
            abilities.addAll(permanent.getCard().getActivatedAbilities());
        } else {
            GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, permanent);
            if (!staticBonus.losesAllAbilities() && !permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
                if (staticBonus.losesAllNonManaAbilities()) {
                    abilities.addAll(permanent.getCard().getActivatedAbilities().stream()
                            .filter(AbilityActivationService::isManaAbility)
                            .toList());
                } else {
                    abilities.addAll(permanent.getCard().getActivatedAbilities());
                }
            }
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        }
        abilities.addAll(permanent.getPersistentGrantedActivatedAbilities());
        abilities.addAll(permanent.getTemporaryActivatedAbilities());
        abilities.addAll(permanent.getUntilNextTurnActivatedAbilities());
        return abilities;
    }

    /**
     * CR 613 layered-pass variant: answers type/subtype leaf predicates from the given
     * {@link CharacteristicState} (the permanent's characteristics as computed by the layers
     * applied so far) instead of the raw permanent, and delegates every other predicate to
     * {@link #matchesPermanentPredicate(Permanent, PermanentPredicate, FilterContext)}.
     * Composite predicates recurse with the state so nested type leaves stay state-answered.
     */
    public boolean matchesPermanentPredicate(CharacteristicState state,
                                             Permanent permanent,
                                             PermanentPredicate predicate,
                                             FilterContext filterContext) {
        if (predicate == null) return false;
        if (state == null) return matchesPermanentPredicate(permanent, predicate, filterContext);
        GameData gameData = filterContext != null ? filterContext.gameData() : null;

        return switch (predicate) {
            case PermanentHasSubtypePredicate p -> {
                boolean creatureSubtype = gameQueryService.isCreatureSubtype(p.subtype());
                // Legacy guard kept: "loses all creature types" is absolute and also nullifies
                // the Changeling grant (the state already had its creature types stripped).
                if (creatureSubtype && permanent.isLosesAllCreatureTypesUntilEndOfTurn()) {
                    yield false;
                }
                yield state.hasSubtype(p.subtype())
                        || (creatureSubtype && (state.hasKeyword(Keyword.CHANGELING) || (gameData == null
                        ? permanent.hasKeyword(Keyword.CHANGELING)
                        : gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING))));
            }
            case PermanentHasAnySubtypePredicate p -> {
                Set<CardSubtype> wanted = permanent.isLosesAllCreatureTypesUntilEndOfTurn()
                        ? p.subtypes().stream()
                                .filter(st -> !gameQueryService.isCreatureSubtype(st))
                                .collect(java.util.stream.Collectors.toSet())
                        : p.subtypes();
                boolean hasSubtype = wanted.stream().anyMatch(state::hasSubtype);
                boolean canUseChangeling = wanted.stream().anyMatch(gameQueryService::isCreatureSubtype);
                yield hasSubtype || (canUseChangeling && (state.hasKeyword(Keyword.CHANGELING) || (gameData == null
                        ? permanent.hasKeyword(Keyword.CHANGELING)
                        : gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING))));
            }
            case PermanentHasAdventurePredicate ignored ->
                    permanent.getCard().getCastingOption(AdventureCast.class).isPresent();
            case PermanentIsCreaturePredicate ignored ->
                    state.hasCardType(CardType.CREATURE) || isOneShotAnimated(permanent);
            case PermanentIsArtifactPredicate ignored ->
                    state.hasCardType(CardType.ARTIFACT);
            case PermanentIsLandPredicate ignored ->
                    state.hasCardType(CardType.LAND);
            case PermanentIsEnchantmentPredicate ignored ->
                    state.hasCardType(CardType.ENCHANTMENT);
            case PermanentIsPlaneswalkerPredicate ignored ->
                    state.hasCardType(CardType.PLANESWALKER);
            case PermanentIsBattlePredicate ignored ->
                    state.hasCardType(CardType.BATTLE);
            case PermanentIsKindredPredicate ignored ->
                    state.hasCardType(CardType.KINDRED);
            case PermanentHasKeywordPredicate p ->
                    state.hasKeyword(p.keyword());
            case PermanentNamedPredicate p ->
                    namesMatch(state.getName(), p.cardName());
            case PermanentNameInPredicate p ->
                    state.getName() != null && p.cardNames().contains(state.getName());
            case PermanentHasProtectionFromColorPredicate p ->
                    state.hasProtectionColor(p.color())
                            || permanent.getProtectionFromColorsUntilEndOfTurn().contains(p.color());
            case PermanentHasSupertypePredicate p ->
                    hasSupertype(permanent, gameData, p.supertype());
            case PermanentIsHistoricPredicate ignored ->
                    state.hasCardType(CardType.ARTIFACT)
                            || hasSupertype(permanent, gameData, CardSupertype.LEGENDARY)
                            || state.hasSubtype(CardSubtype.SAGA);
            case PermanentNotPredicate p ->
                    !matchesPermanentPredicate(state, permanent, p.predicate(), filterContext);
            case PermanentAllOfPredicate p -> {
                for (PermanentPredicate nested : p.predicates()) {
                    if (!matchesPermanentPredicate(state, permanent, nested, filterContext)) {
                        yield false;
                    }
                }
                yield true;
            }
            case PermanentAnyOfPredicate p -> {
                for (PermanentPredicate nested : p.predicates()) {
                    if (matchesPermanentPredicate(state, permanent, nested, filterContext)) {
                        yield true;
                    }
                }
                yield false;
            }
            default -> matchesPermanentPredicate(permanent, predicate, filterContext);
        };
    }

    /**
     * The animation flags that make a permanent a creature without any layer-4 effect saying so.
     * Kept alongside the state read because a {@link CharacteristicState} is built from continuous
     * effects only, while these are one-shot resolutions recorded on the permanent.
     */
    private static boolean isOneShotAnimated(Permanent permanent) {
        return permanent.isAnimatedUntilEndOfTurn()
                || permanent.isAnimatedUntilEndOfCombat()
                || permanent.isAnimatedUntilNextTurn()
                || permanent.isPermanentlyAnimated()
                || permanent.getCounterCount(CounterType.AWAKENING) > 0;
    }

    /**
     * Answers supertype predicates through the central effective-characteristic query so
     * persistent changes, the in-flight layered state, and global removals agree.
     */
    private boolean hasSupertype(Permanent permanent, GameData gameData, CardSupertype supertype) {
        return gameQueryService.hasEffectiveSupertype(gameData, permanent, supertype);
    }

    /**
     * Saga is not a creature type, so unlike {@link PermanentHasSubtypePredicate} there is no
     * Changeling or "loses all creature types" interaction to honor here.
     */
    private static boolean hasSagaSubtype(Permanent permanent) {
        return permanent.getCard().getSubtypes().contains(CardSubtype.SAGA)
                || permanent.getTransientSubtypes().contains(CardSubtype.SAGA)
                || permanent.getGrantedSubtypes().contains(CardSubtype.SAGA);
    }

    /** An attacking creature is blocked if any permanent references its id as a blocking target. */
    private boolean isBlocked(GameData gameData, Permanent attacker) {
        if (attacker.isBlockedWithoutBlockers()) {
            return true;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent blocker : battlefield) {
                if (blocker.isBlocking() && blocker.getBlockingTargetIds().contains(attacker.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Permanent findPermanentByOriginalCardId(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (p.getOriginalCard().getId().equals(cardId)
                            || p.getCard().getId().equals(cardId)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Base (printed) power of a card identified by id, searched across the hand, graveyard, and exile
     * zones. Used by source-relative power filters (e.g. Earthshaker Khenra's ETB) during cast-time
     * target validation, when the source is not yet a battlefield permanent. Returns {@code null} when
     * the card can't be found or has no power (non-creature).
     */
    private Integer basePowerOfCardInAnyZone(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            Integer fromHand = basePowerOfCardInList(gameData.playerHands.get(playerId), cardId);
            if (fromHand != null) {
                return fromHand;
            }
            Integer fromGraveyard = basePowerOfCardInList(gameData.playerGraveyards.get(playerId), cardId);
            if (fromGraveyard != null) {
                return fromGraveyard;
            }
            Integer fromExile = basePowerOfCardInList(gameData.getPlayerExiledCards(playerId), cardId);
            if (fromExile != null) {
                return fromExile;
            }
        }
        return null;
    }

    private Integer basePowerOfCardInList(List<Card> cards, UUID cardId) {
        if (cards == null) {
            return null;
        }
        for (Card card : cards) {
            if (card.getId().equals(cardId)) {
                return card.getPower();
            }
        }
        return null;
    }

    private Permanent findPermanentByCurrentCardId(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                for (Permanent p : battlefield) {
                    if (p.getCard().getId().equals(cardId)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    // --- Stack entry predicate matching ---

    /**
     * Evaluates a {@link StackEntryPredicate} against a stack entry, supporting predicates that
     * reference the "enchanted player" — the player the evaluating source permanent is attached to.
     *
     * <p>This is the static-effect-context evaluation (e.g. damage multipliers). Targeting-context
     * predicates (single-target, has-target, mana-value, controlled-by, targets-your-permanent,
     * targets-you-or-creature-you-control) are evaluated by
     * {@code TargetLegalityService#matchesStackEntryPredicate} instead and never match here.</p>
     *
     * @param enchantedPlayerId the player the source aura is attached to, or {@code null} when no
     *                          such context applies (e.g. damage-multiplier evaluation)
     */
    public boolean matchesStackEntryPredicate(StackEntry entry, StackEntryPredicate predicate, UUID enchantedPlayerId) {
        if (predicate == null) return false;
        return switch (predicate) {
            case StackEntryControlledByEnchantedPlayerPredicate ignored ->
                    enchantedPlayerId != null && enchantedPlayerId.equals(entry.getControllerId());
            case StackEntryTypeInPredicate typeIn ->
                    typeIn.spellTypes().contains(entry.getEntryType());
            case StackEntryColorInPredicate colorIn -> {
                List<CardColor> colors = entry.getCard().getColors();
                if (colors == null) yield false;
                for (CardColor color : colors) {
                    if (colorIn.colors().contains(color)) yield true;
                }
                yield false;
            }
            case StackEntryIsMulticoloredPredicate ignored ->
                    entry.getCard().getColors() != null && entry.getCard().getColors().size() >= 2;
            case StackEntryCardTypeInPredicate cardTypeIn ->
                    cardTypeIn.cardTypes().stream().anyMatch(entry.getCard()::hasType);
            case StackEntrySubtypeInPredicate subtypeIn ->
                    entry.getCard().getSubtypes().stream().anyMatch(subtypeIn.subtypes()::contains);
            case StackEntrySupertypeInPredicate supertypeIn ->
                    entry.getCard().getSupertypes().stream().anyMatch(supertypeIn.supertypes()::contains);
            case StackEntryAllOfPredicate allOf -> {
                for (StackEntryPredicate nested : allOf.predicates()) {
                    if (!matchesStackEntryPredicate(entry, nested, enchantedPlayerId)) yield false;
                }
                yield true;
            }
            case StackEntryAnyOfPredicate anyOf -> {
                for (StackEntryPredicate nested : anyOf.predicates()) {
                    if (matchesStackEntryPredicate(entry, nested, enchantedPlayerId)) yield true;
                }
                yield false;
            }
            case StackEntryNotPredicate not ->
                    !matchesStackEntryPredicate(entry, not.predicate(), enchantedPlayerId);
            case StackEntryCastFromZonePredicate castFrom ->
                    entry.getSourceZone() == castFrom.sourceZone();
            case StackEntryKickedPredicate ignored -> entry.wasKicked();
            case StackEntryTruePredicate ignored -> true;
            case StackEntryIsSingleTargetPredicate ignored -> false;
            case StackEntryHasTargetPredicate ignored -> false;
            case StackEntryHasXInManaCostPredicate ignored -> false;
            case StackEntryIsNthSpellCastThisTurnPredicate ignored -> false;
            case StackEntryManaValuePredicate manaValue ->
                    entry.getCard().getManaValue() + entry.getXValue() == manaValue.manaValue();
            case StackEntryMaxManaValuePredicate maxManaValue ->
                    entry.getCard().getManaValue() + entry.getXValue() <= maxManaValue.maxManaValue();
            // Targeting-only predicates: evaluated by TargetLegalityService, never in this context.
            case StackEntryManaValueEqualsXPredicate ignored -> false;
            case StackEntryManaValueEqualsSourceCountersPredicate ignored -> false;
            case StackEntryManaValueEqualsSourcePowerPredicate ignored -> false;
            case StackEntryManaValueAtMostControlledCountPredicate ignored -> false;
            case StackEntryManaValueAtMostControllerGraveyardCountPredicate ignored -> false;
            case StackEntrySharesColorOrManaValueWithImprintedCardPredicate ignored -> false;
            case StackEntryControlledByPredicate ignored -> false;
            case StackEntryNotTargetedByNamedCreatureAbilityPredicate ignored -> false;
            case StackEntryTargetsYourPermanentPredicate ignored -> false;
            case StackEntryTargetsSourcePredicate ignored -> false;
            case StackEntryTargetsYouOrCreatureYouControlPredicate ignored -> false;
            case StackEntryTargetsYouPredicate ignored -> false;
            case StackEntryTargetsAnyPlayerPredicate ignored -> false;
            case StackEntryTargetsOnlySingleCreaturePredicate ignored -> false;
            case StackEntryTargetsPermanentPredicate ignored -> false;
            case StackEntrySharesChosenNameWithSourcePredicate ignored -> false;
            case StackEntrySharesNameWithCardExiledWithSourcePredicate ignored -> false;
        };
    }

    // --- Target filtering & validation ---

    /**
     * Returns {@code true} if the permanent passes all of the given target filters.
     *
     * @see #matchesFilters(Permanent, Set, FilterContext)
     */
    public boolean matchesFilters(GameData gameData, Permanent permanent, Set<TargetFilter> filters) {
        return matchesFilters(permanent, filters, FilterContext.of(gameData));
    }

    /**
     * Returns {@code true} if the permanent passes all of the given target filters,
     * using the provided {@link FilterContext} for source-aware checks (e.g. "controlled
     * by source's controller" or "owned by source's controller").
     */
    public boolean matchesFilters(Permanent permanent,
                                  Set<TargetFilter> filters,
                                  FilterContext filterContext) {
        for (TargetFilter filter : filters) {
            if (!matchesSingleFilter(filter, permanent, filterContext)) return false;
        }
        return true;
    }

    /**
     * Validates that the target permanent passes the given filter.
     * Returns the filter's error message if it does not.
     */
    public Optional<String> checkTargetFilter(TargetFilter filter, Permanent target) {
        return checkTargetFilter(filter, target, FilterContext.empty());
    }

    public Optional<String> checkTargetFilter(TargetFilter filter,
                                              Permanent target,
                                              FilterContext filterContext) {
        if (!matchesSingleFilter(filter, target, filterContext)) {
            return Optional.of(getFilterErrorMessage(filter));
        }
        return Optional.empty();
    }

    public void validateTargetFilter(TargetFilter filter, Permanent target) {
        checkTargetFilter(filter, target).ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    /**
     * Validates that the target permanent passes the given filter, using game data
     * for source-aware checks. Throws {@link IllegalStateException} if it does not.
     */
    public void validateTargetFilter(GameData gameData, TargetFilter filter, Permanent target) {
        checkTargetFilter(filter, target, FilterContext.of(gameData))
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    /**
     * Validates that the target permanent passes the given filter, using the provided
     * {@link FilterContext}. Throws {@link IllegalStateException} with the filter's
     * error message if it does not.
     */
    public void validateTargetFilter(TargetFilter filter,
                                     Permanent target,
                                     FilterContext filterContext) {
        checkTargetFilter(filter, target, filterContext)
                .ifPresent(reason -> { throw new IllegalStateException(reason); });
    }

    private boolean matchesSingleFilter(TargetFilter filter, Permanent target, FilterContext filterContext) {
        if (filter == null) return true;
        GameData gameData = filterContext != null ? filterContext.gameData() : null;
        UUID sourceControllerId = filterContext != null ? filterContext.sourceControllerId() : null;

        return switch (filter) {
            case ControlledPermanentPredicateTargetFilter controlledFilter -> {
                if (sourceControllerId == null || gameData == null) yield false;
                List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
                if (controllerBattlefield == null || !controllerBattlefield.contains(target)) yield false;
                yield matchesPermanentPredicate(target, controlledFilter.predicate(), filterContext);
            }
            case OwnedPermanentPredicateTargetFilter ownedFilter -> {
                if (gameData == null || sourceControllerId == null) yield false;
                boolean ownedByController = false;
                for (UUID playerId : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                    if (battlefield != null && battlefield.contains(target)) {
                        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                        ownedByController = ownerId.equals(sourceControllerId);
                        break;
                    }
                }
                if (!ownedByController) yield false;
                yield matchesPermanentPredicate(target, ownedFilter.predicate(), filterContext);
            }
            case PermanentPredicateTargetFilter f ->
                    matchesPermanentPredicate(target, f.predicate(), filterContext);
            // Any-target restriction: the permanent side is checked against the permanent predicate.
            case AnyTargetPredicateTargetFilter f ->
                    matchesPermanentPredicate(target, f.permanentPredicate(), filterContext);
            case PlayerPredicateTargetFilter ignored -> false;
            // A graveyard-card group never matches a permanent target.
            case GraveyardCardPredicateTargetFilter ignored -> false;
            // Stack-entry filters never restrict a permanent target.
            case StackEntryPredicateTargetFilter ignored -> true;
        };
    }

    private String getFilterErrorMessage(TargetFilter filter) {
        return switch (filter) {
            case ControlledPermanentPredicateTargetFilter f -> f.errorMessage();
            case OwnedPermanentPredicateTargetFilter f -> f.errorMessage();
            case PermanentPredicateTargetFilter f -> f.errorMessage();
            case AnyTargetPredicateTargetFilter f -> f.errorMessage();
            case PlayerPredicateTargetFilter f -> f.errorMessage();
            case GraveyardCardPredicateTargetFilter ignored -> "Target must be a card in a graveyard";
            case StackEntryPredicateTargetFilter f -> f.errorMessage();
        };
    }

    /**
     * Returns true if the card is an Aura whose enchant ability restricts it to creatures. An
     * Aura's enchant restriction lives in its spell target filter, so a creature restriction is
     * detected by looking for a creature requirement in that filter's predicate.
     */
    private boolean isAuraEnchantingCreature(Card card) {
        if (!card.isAura() || card.isEnchantPlayer()) return false;
        TargetFilter filter = card.getTargetFilter();
        if (filter == null) return false;
        PermanentPredicate predicate = switch (filter) {
            case ControlledPermanentPredicateTargetFilter f -> f.predicate();
            case OwnedPermanentPredicateTargetFilter f -> f.predicate();
            case PermanentPredicateTargetFilter f -> f.predicate();
            default -> null;
        };
        return requiresCreature(predicate);
    }

    private boolean requiresCreature(PermanentPredicate predicate) {
        if (predicate instanceof PermanentIsCreaturePredicate) return true;
        if (predicate instanceof PermanentAllOfPredicate all) {
            return all.predicates().stream().anyMatch(this::requiresCreature);
        }
        return false;
    }
}
