package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.AllCountersOnSource;
import com.github.laxika.magicalvibes.model.amount.ArtifactsPutIntoGraveyardFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.amount.AttachedPermanentColorCount;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.BeheldPower;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsDiscardedThisResolution;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongPermanentsSacrificedThisTurn;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongSpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.amount.CardsDelved;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedByTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisResolution;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.amount.CardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.CardsInExile;
import com.github.laxika.magicalvibes.model.amount.SuspendedCards;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CardsLookedAtWhileScrying;
import com.github.laxika.magicalvibes.model.amount.DefendingPlayerPoisonCounters;
import com.github.laxika.magicalvibes.model.amount.CardsPutIntoGraveyardByTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.amount.CardsPutIntoGraveyardFromHandOrLibraryThisTurn;
import com.github.laxika.magicalvibes.model.amount.CaveManaSpentToCast;
import com.github.laxika.magicalvibes.model.amount.DesertManaSpentToCast;
import com.github.laxika.magicalvibes.model.amount.ChosenCreatureOrRevealedCardPower;
import com.github.laxika.magicalvibes.model.amount.ChosenCreatureOrWarpedCardPower;
import com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.ColorManaPairsSpentToCast;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.DevotionToChosenColor;
import com.github.laxika.magicalvibes.model.amount.ColorsSpentToCast;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanentsAndSpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsInHand;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.amount.CompletedDungeonsCount;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.amount.ControllerEnergyCounters;
import com.github.laxika.magicalvibes.model.amount.EnergyCountersPaidOrLostThisTurn;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.ControllerRadCounters;
import com.github.laxika.magicalvibes.model.amount.ControllerSpeed;
import com.github.laxika.magicalvibes.model.amount.ConvokeCreatureCount;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CountersOnGrantingPermanent;
import com.github.laxika.magicalvibes.model.amount.CountersOnLinkedPermanent;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.CountersOnStackEntryCard;
import com.github.laxika.magicalvibes.model.amount.CountersOnTargetPermanent;
import com.github.laxika.magicalvibes.model.amount.CreatureCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.CreatureCardsInGraveyardFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.amount.TimesSourceRegeneratedThisTurn;
import com.github.laxika.magicalvibes.model.amount.TimesSourceMutated;
import com.github.laxika.magicalvibes.model.amount.TimesSourceAbilityResolvedThisTurn;
import com.github.laxika.magicalvibes.model.amount.TokensCreatedThisTurn;
import com.github.laxika.magicalvibes.model.amount.TurnsTakenByController;
import com.github.laxika.magicalvibes.model.amount.TurnsBegunSinceForetell;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.amount.CreatureSubtypeDeathsThisTurn;
import com.github.laxika.magicalvibes.model.amount.CreatureTypesAmongControlledCreatures;
import com.github.laxika.magicalvibes.model.amount.CreaturesAttackedThisTurn;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockedBySource;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.CreaturesDevoured;
import com.github.laxika.magicalvibes.model.amount.CreaturesEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.amount.CreaturesExiledThisTurn;
import com.github.laxika.magicalvibes.model.amount.CreaturesLeftBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.amount.CreaturesPutIntoOwnGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.amount.CreaturesThatCrewedSourceThisTurn;
import com.github.laxika.magicalvibes.model.amount.DamageDealtByTargetPlayerSorceryThisTurn;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToControllerByArtifactsThisTurn;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToControllerThisTurn;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToOpponentsThisTurn;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToSourcePermanentBySourceNameThisTurn;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToSourceByControllerThisTurn;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToSourceThisTurn;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToTargetPermanentThisTurn;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.amount.DescentsThisTurn;
import com.github.laxika.magicalvibes.model.amount.DevouredCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.amount.DistinctColorPairsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.DistinctCountersOnSource;
import com.github.laxika.magicalvibes.model.amount.DistinctCounterKindsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.DistinctKeywordAbilitiesAmongControlledCreatures;
import com.github.laxika.magicalvibes.model.amount.DistinctManaCostsAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.DistinctManaValuesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.DistinctManaValuesAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.DistinctManaValuesAmongStudyCounterCardsInExile;
import com.github.laxika.magicalvibes.model.amount.DistinctPermanentNamesAmongControlled;
import com.github.laxika.magicalvibes.model.amount.DistinctPermanentNamesCount;
import com.github.laxika.magicalvibes.model.amount.DistinctPowersAmongControlledCreatures;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.DuringControllerTurn;
import com.github.laxika.magicalvibes.model.amount.DyingPermanentManaValue;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EnchantedPermanentManaValue;
import com.github.laxika.magicalvibes.model.amount.EnchantedPermanentPower;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.amount.FixedIfAllTargetsControlledByController;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlMoreCreaturesThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlledCreaturesTotalToughnessAtLeast;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlsAllNamed;
import com.github.laxika.magicalvibes.model.amount.FixedIfTargetMatches;
import com.github.laxika.magicalvibes.model.amount.FixedIfTargetPlayerControlsMoreLands;
import com.github.laxika.magicalvibes.model.amount.ForetoldCardsInExile;
import com.github.laxika.magicalvibes.model.amount.GraveyardsAtLeast;
import com.github.laxika.magicalvibes.model.amount.GreatestCreatureCountAmongPlayers;
import com.github.laxika.magicalvibes.model.amount.GreatestCreatureTypeCountAmongControlled;
import com.github.laxika.magicalvibes.model.amount.GreatestDamageDealtBySourceThisTurn;
import com.github.laxika.magicalvibes.model.amount.GreatestDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongAttachedEquipment;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongOwnedCommanders;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongSpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueNotedForSourceThisTurn;
import com.github.laxika.magicalvibes.model.amount.GreatestOpponentCardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.amount.GreatestOpponentHandSize;
import com.github.laxika.magicalvibes.model.amount.GreatestPermanentCountAmongOpponents;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.amount.GreatestStackSourceCountThisTurn;
import com.github.laxika.magicalvibes.model.amount.GreatestToughnessAmongControlled;
import com.github.laxika.magicalvibes.model.amount.HalfControllerLifeRoundedUp;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.HighestLifeTotalAmongPlayers;
import com.github.laxika.magicalvibes.model.amount.HighestOpponentLifeTotal;
import com.github.laxika.magicalvibes.model.amount.IfSourceAttacking;
import com.github.laxika.magicalvibes.model.amount.ImprintedCardManaValue;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreaturePower;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfControlledCreatures;
import com.github.laxika.magicalvibes.model.amount.TotalToughnessOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.TotalToughnessOfControlledCreatures;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellXValue;
import com.github.laxika.magicalvibes.model.amount.UnspentMana;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreatureToughness;
import com.github.laxika.magicalvibes.model.amount.LandsEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.amount.LandsMatchingImprintedName;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardTypeCount;
import com.github.laxika.magicalvibes.model.amount.LastMilledCardColorSymbols;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.amount.LowestLifeTotalAmongPlayers;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInHand;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Min;
import com.github.laxika.magicalvibes.model.amount.NoncombatDamageDealtToOpponentsThisTurn;
import com.github.laxika.magicalvibes.model.amount.NontokenCreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.amount.NontokenCreaturesPutIntoOwnGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.amount.NonCreatureSubtypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.OpponentPoisonCounters;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithCreaturePowerAtLeast;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithAtLeastCardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithAtLeastLandsEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.amount.OpponentsControllingReturnedPermanents;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithAtLeastPoisonCounters;
import com.github.laxika.magicalvibes.model.amount.OpponentsAttackedThisTurn;
import com.github.laxika.magicalvibes.model.amount.OpponentsDealtCombatDamageBySourceNameOrSubtypeThisTurn;
import com.github.laxika.magicalvibes.model.amount.OpponentsDealtCombatDamageThisTurn;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithFewerCreaturesThanController;
import com.github.laxika.magicalvibes.model.amount.OpponentsWhoLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithAtLeastTwoMoreLandsThanController;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithLifeAtMost;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithLessLifeThanController;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithMoreCreaturesThanController;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithMoreLandsThanController;
import com.github.laxika.magicalvibes.model.amount.OtherControlledCreaturesSharingCreatureTypeWithTarget;
import com.github.laxika.magicalvibes.model.amount.OtherAttackersSharingCreatureTypeWithTarget;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.amount.PlayersWhoLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.amount.PermanentManaValueSum;
import com.github.laxika.magicalvibes.model.amount.PermanentsEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.amount.PermanentsSacrificedThisTurn;
import com.github.laxika.magicalvibes.model.amount.PermanentTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.PileGroupingOrGuessCountThisTurn;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.PlayersWhoLostGame;
import com.github.laxika.magicalvibes.model.amount.PlayersWhoDiscardedThisTurn;
import com.github.laxika.magicalvibes.model.amount.PlayersWithCardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.amount.PlayersWithCardsInHandAtMost;
import com.github.laxika.magicalvibes.model.amount.PlusOnePlusOneCountersPutOnControlledCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentColorCount;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentManaValue;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentPower;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentToughness;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.SnowManaSpentToCast;
import com.github.laxika.magicalvibes.model.amount.SourceCardPower;
import com.github.laxika.magicalvibes.model.amount.SourceCardManaValue;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.amount.SourceManaValueMinusOne;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.amount.SpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.amount.SpellsCastSinceBeginningOfLastTurn;
import com.github.laxika.magicalvibes.model.amount.SpellsCastFromOutsideHandThisTurn;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TargetCardsManaValueSum;
import com.github.laxika.magicalvibes.model.amount.TargetGroupCount;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.amount.TargetPermanentColorCount;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerPoisonCounters;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.amount.TargetPowerPlusToughness;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.amount.TargetSpellPower;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfTargetGroup;
import com.github.laxika.magicalvibes.model.amount.TopCardOfLibraryManaValue;
import com.github.laxika.magicalvibes.model.amount.TotalCountersOnSource;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfCardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfCardsOwnedInExile;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfDestroyedPermanents;
import com.github.laxika.magicalvibes.model.amount.TotalManaValueOfOtherSpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.amount.TotalCountersAmongPlayersAndPermanents;
import com.github.laxika.magicalvibes.model.amount.TotalRadCountersAmongPlayers;
import com.github.laxika.magicalvibes.model.amount.TreasureManaSpentToActivate;
import com.github.laxika.magicalvibes.model.amount.TreasureManaSpentToCast;
import com.github.laxika.magicalvibes.model.amount.TreasuresCreatedThisTurn;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellColorCount;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellColorManaSymbols;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellTargetCount;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.amount.UnlockedRoomDoorsCount;
import com.github.laxika.magicalvibes.model.amount.UntappedLandsAtTurnStart;
import com.github.laxika.magicalvibes.model.amount.WebSlingingReturnedCreatureManaValue;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CountAsNamedCardForSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.StationPowerModifierEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The single evaluation point for every {@link DynamicAmount} in the engine (the numeric
 * sibling of {@link ConditionEvaluationService}).
 *
 * <p>The switch in {@link #evaluate} is exhaustive over the sealed {@link DynamicAmount}
 * hierarchy — adding an amount without an evaluation is a compile error, never a silent 0.
 * All evaluation contexts (stack resolution, static bonus computation, AI estimation) call
 * this service with an {@link AmountContext} describing where the values (source permanent,
 * controller, x value, …) come from at that site.</p>
 */
@Service
public class AmountEvaluationService {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final ConditionEvaluationService conditionEvaluationService;

    private static final List<CardSubtype> PARTY_ROLES = List.of(
            CardSubtype.CLERIC, CardSubtype.ROGUE, CardSubtype.WARRIOR, CardSubtype.WIZARD);

    @Autowired
    public AmountEvaluationService(PredicateEvaluationService predicateEvaluationService,
            GameQueryService gameQueryService, ConditionEvaluationService conditionEvaluationService) {
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameQueryService = gameQueryService;
        this.conditionEvaluationService = conditionEvaluationService;
    }

    /**
     * Hand-wires the condition evaluator from the same two collaborators, for the non-Spring call
     * sites (AI evaluators, the headless simulator, handler unit tests) that build this service
     * directly. {@link ConditionEvaluationService} is stateless, so a private instance behaves
     * exactly like the Spring-managed one.
     */
    public AmountEvaluationService(PredicateEvaluationService predicateEvaluationService,
            GameQueryService gameQueryService) {
        this(predicateEvaluationService, gameQueryService,
                new ConditionEvaluationService(gameQueryService, predicateEvaluationService));
    }

    /**
     * Evaluates the current value of the given amount.
     */
    public int evaluate(GameData gameData, DynamicAmount amount, AmountContext ctx) {
        return switch (amount) {
            case ArtifactsPutIntoGraveyardFromBattlefieldThisTurn ignored ->
                    gameData.artifactsPutIntoGraveyardFromBattlefieldThisTurn;
            case Fixed f ->
                    f.value();
            case FixedIfCondition a ->
                    conditionEvaluationService.isMet(gameData, a.condition(), conditionContext(gameData, ctx))
                            ? a.amount() : a.otherwise();
            case FixedIfControlMoreCreaturesThanEachOtherPlayer a ->
                    controlsMoreCreaturesThanEachOtherPlayer(gameData, ctx) ? a.amount() : a.otherwise();
            case FixedIfControlledCreaturesTotalToughnessAtLeast a ->
                    totalToughnessOfControlledCreatures(gameData, ctx) >= a.minTotalToughness() ? a.amount() : 0;
            case FixedIfControlsAllNamed a ->
                    controlsAllNamed(gameData, a, ctx) ? a.amount() : a.otherwise();
            case FixedIfAllTargetsControlledByController a ->
                    allTargetsControlledByController(gameData, a, ctx) ? a.amount() : a.otherwise();
            case FixedIfTargetMatches a ->
                    targetMatches(gameData, a, ctx) ? a.amount() : a.otherwise();
            case FixedIfTargetPlayerControlsMoreLands a ->
                    targetPlayerControlsMoreLands(gameData, ctx) ? a.amount() : a.otherwise();
            case XValue ignored ->
                    ctx.xValue();
            case TriggeringSpellXValue ignored ->
                    ctx.xValue();
            case WebSlingingReturnedCreatureManaValue ignored ->
                    ctx.sourcePermanent() == null || ctx.sourcePermanent().getWebSlingingReturnedCreatureManaValue() == null
                            ? 0 : ctx.sourcePermanent().getWebSlingingReturnedCreatureManaValue();
            // Cast triggers carry the triggering spell's payment in X, independently of
            // the mana spent on the permanent that owns the ability.
            case ManaSpentToCast ignored ->
                    ctx.stackEntry() != null && ctx.stackEntry().getTriggeringCardId() != null
                            ? ctx.xValue()
                            : ctx.sourcePermanent() != null
                            ? ctx.sourcePermanent().getManaSpentToCast()
                            : ctx.stackEntry() != null && ctx.stackEntry().getEntryType() != StackEntryType.TRIGGERED_ABILITY
                            ? ctx.stackEntry().getManaSpentToCast()
                            : ctx.xValue();
            case SnowManaSpentToCast ignored ->
                    ctx.sourceCard() == null ? 0 : gameData.getSpellCastSnowManaSpent(ctx.sourceCard().getId());
            case TreasureManaSpentToCast ignored ->
                    ctx.sourceCard() == null ? 0 : gameData.getSpellCastTreasureManaSpent(ctx.sourceCard().getId());
            case TreasureManaSpentToActivate ignored ->
                    ctx.stackEntry() == null ? 0 : ctx.stackEntry().getActivationTreasureManaSpent();
            case CaveManaSpentToCast ignored ->
                    ctx.sourceCard() == null ? 0 : gameData.getSpellCastCaveManaSpent(ctx.sourceCard().getId());
            case DesertManaSpentToCast ignored ->
                    ctx.sourceCard() == null ? 0 : gameData.getSpellCastDesertManaSpent(ctx.sourceCard().getId());
            case EventValue ignored ->
                    ctx.eventValue();
            case CardsLookedAtWhileScrying ignored ->
                    ctx.stackEntry() == null ? 0 : ctx.stackEntry().getCardsLookedAtWhileScrying();
            case TotalManaValueOfDestroyedPermanents ignored ->
                    ctx.stackEntry() == null ? 0 : ctx.stackEntry().getEventManaValues().stream()
                            .mapToInt(Integer::intValue).sum();
            case ConvokeCreatureCount ignored ->
                    ctx.convokeCreatureCount();
            case RepeatedAdditionalCostCount a ->
                    (int) ctx.repeatedAdditionalCosts().stream().filter(a.manaCost()::equals).count();
            case Scaled s ->
                    s.factor() * evaluate(gameData, s.amount(), ctx);
            case SacrificedPermanentManaValue ignored ->
                    ctx.stackEntry() == null || ctx.stackEntry().getSacrificedPermanentSnapshot() == null
                            ? 0 : Math.max(0, gameQueryService.getPermanentManaValue(ctx.stackEntry().getSacrificedPermanentSnapshot()));
            case SacrificedPermanentPower ignored ->
                    Math.max(0, ctx.sacrificedPower());
            case SacrificedPermanentColorCount ignored ->
                    Math.max(0, ctx.stackEntry() == null ? 0 : ctx.stackEntry().getSacrificedColorCount());
            case SacrificedPermanentToughness ignored ->
                    Math.max(0, ctx.sacrificedToughness());
            case Divided d ->
                    evaluate(gameData, d.amount(), ctx) / d.divisor();
            case Sum s ->
                    s.amounts().stream().mapToInt(a -> evaluate(gameData, a, ctx)).sum();
            case Min m ->
                    m.amounts().stream().mapToInt(a -> evaluate(gameData, a, ctx)).min().orElse(0);
            case Max m ->
                    m.amounts().stream().mapToInt(a -> evaluate(gameData, a, ctx)).max().orElse(0);
            case DuringControllerTurn d ->
                    ctx.controllerId() != null && ctx.controllerId().equals(gameData.activePlayerId)
                            ? evaluate(gameData, d.amount(), ctx) : 0;
            case CompletedDungeonsCount ignored ->
                    gameData.completedDungeonsByPlayer.getOrDefault(ctx.controllerId(), Set.of()).size();
            case CommanderCastsFromCommandZoneThisGame c ->
                    countCommanderCastsFromCommandZoneThisGame(gameData, c, ctx);
            case PermanentCount c ->
                    countPermanents(gameData, c, ctx);
            case TreasuresCreatedThisTurn ignored ->
                    gameData.getTreasureTokensCreatedThisTurn(ctx.controllerId());
            case PileGroupingOrGuessCountThisTurn ignored ->
                    gameData.pileGroupingOrGuessCountThisTurn;
            case UnlockedRoomDoorsCount c ->
                    countUnlockedRoomDoors(gameData, c, ctx);
            case PermanentCounterSum s ->
                    sumPermanentCounters(gameData, s, ctx);
            case PlusOnePlusOneCountersPutOnControlledCreaturesThisTurn ignored ->
                    ctx.controllerId() == null ? 0
                            : gameData.plusOnePlusOneCountersPutOnControlledCreaturesThisTurn
                                    .getOrDefault(ctx.controllerId(), 0);
            case DistinctPermanentNamesCount c ->
                    countDistinctPermanentNames(gameData, c, ctx);
            case PermanentManaValueSum s ->
                    sumPermanentManaValues(gameData, s, ctx);
            case PlayersWithCardsInHandAtMost a ->
                    countPlayersWithCardsInHandAtMost(gameData, a, ctx);
            case PlayersWithCardsInHandAtLeast a ->
                    countPlayersWithCardsInHandAtLeast(gameData, a, ctx);
            case PlayersInGame ignored ->
                    gameData.orderedPlayerIds.size();
            case PlayersWhoLostGame ignored ->
                    gameData.playersWhoLostGameThisMatch.size();
            case TotalCountersAmongPlayersAndPermanents ignored ->
                    totalCountersAmongPlayersAndPermanents(gameData);
            case TotalRadCountersAmongPlayers ignored ->
                    gameData.orderedPlayerIds.stream()
                            .mapToInt(playerId -> gameData.playerRadCounters.getOrDefault(playerId, 0))
                            .sum();
            case PlayersWhoDiscardedThisTurn ignored ->
                    (int) gameData.orderedPlayerIds.stream()
                            .filter(playerId -> gameData.cardsDiscardedThisTurn.getOrDefault(playerId, 0) > 0)
                            .count();
            case PlayersWhoLostLifeThisTurn ignored ->
                    (int) gameData.orderedPlayerIds.stream()
                            .filter(playerId -> gameData.lifeLostThisTurn.getOrDefault(playerId, 0) > 0)
                            .count();
            case AttachedPermanentColorCount ignored ->
                    attachedPermanentColorCount(gameData, ctx);
            case ColorsAmongCardsExiledWithSource ignored ->
                    colorsAmongCardsExiledWithSource(gameData, ctx);
            case TargetPermanentColorCount ignored ->
                    targetPermanentColorCount(gameData, ctx);
            case BasicLandTypesAmongControlledLands domainAmount ->
                    countBasicLandTypesAmongControlledLands(gameData, domainAmount, ctx);
            case CardTypesAmongCardsInGraveyard c ->
                    countCardTypesAmongCardsInGraveyard(gameData, c, ctx);
            case CardTypesAmongPermanentsSacrificedThisTurn ignored ->
                    countCardTypesAmongPermanentsSacrificedThisTurn(gameData, ctx);
            case PermanentTypesAmongCardsInGraveyard ignored ->
                    countPermanentTypesAmongCardsInGraveyard(gameData, ctx);
            case NonCreatureSubtypesAmongCardsInGraveyard c ->
                    countNonCreatureSubtypesAmongCardsInGraveyard(gameData, c, ctx);
            case CardTypesAmongControlledPermanents c ->
                    countCardTypesAmongControlledPermanents(gameData, c, ctx);
            case CardTypesAmongSpellsCastThisTurn ignored ->
                    countCardTypesAmongSpellsCastThisTurn(gameData, ctx);
            case CreatureTypesAmongControlledCreatures ignored ->
                    countCreatureTypesAmongControlledCreatures(gameData, ctx);
            case CardTypesAmongCardsDiscardedThisResolution ignored ->
                    gameData.eachPlayerDiscardsOneThenDrawsForEachCardType.discardedCardTypes.size();
            case CardsDrawnThisResolution ignored ->
                    ctx.stackEntry() == null ? 0 : ctx.stackEntry().getDrawnCardIdsThisResolution().size();
            case CardsDrawnThisTurn c ->
                    countCardsDrawnThisTurn(gameData, c, ctx);
            case OpponentsWithAtLeastCardsDrawnThisTurn c ->
                    opponentsWithAtLeastCardsDrawnThisTurn(gameData, c, ctx);
            case OpponentsControllingReturnedPermanents ignored ->
                    opponentsControllingReturnedPermanents(gameData, ctx);
            case DistinctManaCostsAmongCardsInGraveyard c ->
                    countDistinctManaCostsAmongCardsInGraveyard(gameData, c, ctx);
            case DistinctPermanentNamesAmongControlled c ->
                    countDistinctPermanentNamesAmongControlled(gameData, c, ctx);
            case DistinctManaValuesAmongCardsInGraveyard c ->
                    countDistinctManaValuesAmongCardsInGraveyard(gameData, c, ctx);
            case DistinctColorPairsAmongControlledPermanents ignored ->
                    countDistinctColorPairsAmongControlledPermanents(gameData, ctx);
            case DistinctCounterKindsAmongControlledPermanents ignored ->
                    countDistinctCounterKindsAmongControlledPermanents(gameData, ctx);
            case DistinctManaValuesAmongControlledPermanents ignored ->
                    countDistinctManaValuesAmongControlledPermanents(gameData, ctx);
            case DistinctManaValuesAmongStudyCounterCardsInExile ignored ->
                    (int) gameData.exiledCards.stream()
                            .filter(entry -> ctx.controllerId() != null
                                    && ctx.controllerId().equals(entry.ownerId()))
                            .map(entry -> entry.card())
                            .filter(card -> gameData.exiledCardsWithStudyCounters.contains(card.getId()))
                            .filter(card -> !card.hasType(CardType.LAND))
                            .map(Card::getManaValue)
                            .distinct()
                            .count();
            case DistinctKeywordAbilitiesAmongControlledCreatures a ->
                    countDistinctKeywordAbilitiesAmongControlledCreatures(gameData, a, ctx);
            case DistinctPowersAmongControlledCreatures ignored ->
                    (int) gameData.playerBattlefields.getOrDefault(ctx.controllerId(), List.of()).stream()
                            .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                            .map(permanent -> gameQueryService.getEffectivePower(gameData, permanent))
                            .distinct()
                            .count();
            case DyingPermanentManaValue ignored ->
                    ctx.stackEntry() == null || ctx.stackEntry().getDyingPermanentManaValue() == null
                            ? 0 : ctx.stackEntry().getDyingPermanentManaValue();
            case LandsEnteredBattlefieldThisTurn ignored ->
                    (int) gameData.permanentsEnteredBattlefieldThisTurn
                            .getOrDefault(ctx.controllerId(), List.of()).stream()
                            .filter(card -> card.hasType(CardType.LAND))
                            .count();
            case OpponentsWithAtLeastLandsEnteredBattlefieldThisTurn c ->
                    opponentsWithAtLeastLandsEnteredBattlefieldThisTurn(gameData, c, ctx);
            case CardsInExile c ->
                    countExileCards(gameData, c, ctx);
            case SuspendedCards c ->
                    countSuspendedCards(gameData, c, ctx);
            case CardsExiledWithSource c ->
                    countCardsExiledWithSource(gameData, c, ctx);
            case CardsDelved delved ->
                    countDelvedCards(gameData, delved, ctx);
            case GreatestManaValueAmongCardsExiledWithSource ignored ->
                    greatestManaValueAmongCardsExiledWithSource(gameData, ctx);
            case ForetoldCardsInExile c ->
                    countForetoldCardsInExile(gameData, c, ctx);
            case CardsInGraveyard c ->
                    countGraveyardCards(gameData, c, ctx);
            case GraveyardsAtLeast c ->
                    countGraveyardsAtLeast(gameData, c);
            case CardsInHand c ->
                    countHandCards(gameData, c, ctx);
            case MatchingCardsInHand c ->
                    countMatchingHandCards(gameData, c, ctx);
            case CardsInLibrary c ->
                    countLibraryCards(gameData, c, ctx);
            case MatchingCardsInLibrary c ->
                    countMatchingLibraryCards(gameData, c, ctx);
            case ColorManaSymbolsAmongControlledPermanents c ->
                    countColorManaSymbolsAmongControlledPermanents(gameData, c, ctx);
            case DevotionToChosenColor ignored ->
                    gameData.chosenSpellColor == null ? 0 : gameQueryService.getDevotionToColor(
                            gameData, ctx.controllerId(),
                            ManaColor.valueOf(gameData.chosenSpellColor.name()));
            case ColorManaPairsSpentToCast c ->
                    colorManaPairsSpentToCast(gameData, c, ctx);
            case ColorsSpentToCast ignored ->
                    ctx.sourceCard() == null ? 0 : gameData.getSpellCastColorsSpent(ctx.sourceCard().getId()).size();
            case ColorsAmongControlledPermanents count ->
                    countColorsAmongControlledPermanents(gameData, count, ctx);
            case ColorsAmongControlledPermanentsAndSpellsCastThisTurn ignored ->
                    countColorsAmongControlledPermanentsAndSpellsCastThisTurn(gameData, ctx);
            case ColorManaSymbolsInGraveyard c ->
                    countColorManaSymbolsInGraveyard(gameData, c, ctx);
            case ColorManaSymbolsInHand c ->
                    countColorManaSymbolsInHand(gameData, c, ctx);
            case CountersOnSource c ->
                    countersOnSource(gameData, c, ctx);
            case AllCountersOnSource ignored ->
                    ctx.sourcePermanent() == null ? 0 : ctx.sourcePermanent().getTotalCounterCount();
            case DistinctCountersOnSource ignored ->
                    distinctCountersOnSource(ctx);
            case TotalCountersOnSource ignored ->
                    ctx.sourcePermanent() == null ? 0
                            : ctx.sourcePermanent().getCounters().values().stream().mapToInt(Integer::intValue).sum();
            case CountersOnTargetPermanent c ->
                    countCountersOnTargetPermanent(gameData, c, ctx);
            case CountersOnStackEntryCard c ->
                    countCountersOnStackEntryCard(gameData, c, ctx);
            case TimesSourceRegeneratedThisTurn ignored ->
                    ctx.sourcePermanent() == null ? 0 : ctx.sourcePermanent().getTimesRegeneratedThisTurn();
            case TokensCreatedThisTurn ignored ->
                    ctx.controllerId() == null ? 0
                            : gameData.tokensCreatedThisTurn.getOrDefault(ctx.controllerId(), 0);
            case TimesSourceMutated ignored -> {
                Permanent source = ctx.sourcePermanent();
                if (source == null && ctx.stackEntry() != null) {
                    source = ctx.stackEntry().getSourcePermanentSnapshot();
                }
                yield source == null ? 0 : source.getTimesMutated();
            }
            case TimesSourceAbilityResolvedThisTurn ignored -> {
                UUID sourceId = ctx.stackEntry() != null
                        ? ctx.stackEntry().getSourcePermanentId()
                        : ctx.sourcePermanent() == null ? null : ctx.sourcePermanent().getId();
                if (sourceId == null && ctx.stackEntry() != null
                        && ctx.stackEntry().getSourcePermanentSnapshot() != null) {
                    sourceId = ctx.stackEntry().getSourcePermanentSnapshot().getId();
                }
                yield sourceId == null
                        ? 0 : gameData.permanentAbilityResolutionsThisTurn.getOrDefault(sourceId, 0);
            }
            case CreaturesDevoured ignored ->
                    ctx.sourcePermanent() == null ? 0 : ctx.sourcePermanent().getDevouredCreatures().size();
            case DevouredCreaturesOfSubtype d ->
                    countDevouredCreaturesOfSubtype(ctx, d.subtype());
            case CountersOnLinkedPermanent c ->
                    countCountersOnLinkedPermanent(gameData, c);
            case CountersOnGrantingPermanent c -> {
                Permanent granting = c.grantingPermanentId() == null ? null
                        : gameQueryService.findPermanentById(gameData, c.grantingPermanentId());
                yield granting != null ? granting.getCounterCount(c.counterType())
                        : ctx.stackEntry() == null ? 0 : ctx.stackEntry().getLastKnownPermanentCounters()
                        .getOrDefault(c.grantingPermanentId(), java.util.Map.of())
                        .getOrDefault(c.counterType(), 0);
            }
            case ControllerLifeTotal ignored ->
                    // Null controller happens transiently while the source is still entering the
                    // battlefield (e.g. a CDA evaluated from an entry-time query); playerLifeTotals
                    // is a ConcurrentHashMap, which rejects null keys.
                    ctx.controllerId() == null ? 0 : gameData.playerLifeTotals.getOrDefault(ctx.controllerId(), 0);
            case TurnsTakenByController ignored ->
                    ctx.controllerId() == null ? 0 : gameData.turnsTakenByPlayer.getOrDefault(ctx.controllerId(), 0);
            case TurnsBegunSinceForetell ignored -> {
                StackEntry entry = ctx.stackEntry();
                if (entry == null || !entry.isCastForForetell()
                        || entry.getForetellControllerTurnsAtExile() < 0
                        || ctx.controllerId() == null) {
                    yield 0;
                }
                int turnsTaken = gameData.turnsTakenByPlayer.getOrDefault(ctx.controllerId(), 0);
                yield Math.max(0, turnsTaken - entry.getForetellControllerTurnsAtExile());
            }
            case ControllerSpeed ignored ->
                    ctx.controllerId() == null ? 0 : gameData.playerSpeeds.getOrDefault(ctx.controllerId(), 0);
            case HighestLifeTotalAmongPlayers ignored ->
                    gameData.orderedPlayerIds.stream().mapToInt(gameData::getLife).max().orElse(0);
            case LowestLifeTotalAmongPlayers ignored ->
                    gameData.orderedPlayerIds.stream().mapToInt(gameData::getLife).min().orElse(0);
            case HighestOpponentLifeTotal ignored ->
                    highestOpponentLifeTotal(gameData, ctx);
            case GreatestOpponentHandSize ignored ->
                    greatestOpponentHandSize(gameData, ctx);
            case GreatestOpponentCardsDrawnThisTurn ignored ->
                    greatestOpponentCardsDrawnThisTurn(gameData, ctx);
            case OpponentsWithAtLeastTwoMoreLandsThanController ignored ->
                    opponentsWithAtLeastTwoMoreLandsThanController(gameData, ctx);
            case OpponentsWithLifeAtMost thresholdAmount ->
                    opponentsWithLifeAtMost(gameData, thresholdAmount, ctx);
            case OpponentsWithCreaturePowerAtLeast thresholdAmount ->
                    opponentsWithCreaturePowerAtLeast(gameData, thresholdAmount, ctx);
            case OpponentsWithFewerCreaturesThanController ignored ->
                    opponentsWithFewerCreaturesThanController(gameData, ctx);
            case OpponentsWithMoreCreaturesThanController ignored ->
                    opponentsWithMoreCreaturesThanController(gameData, ctx);
            case OpponentsWithLessLifeThanController ignored ->
                    opponentsWithLessLifeThanController(gameData, ctx);
            case OpponentsWithMoreLandsThanController ignored ->
                    opponentsWithMoreLandsThanController(gameData, ctx);
            case OpponentsWithMoreCardsInHandThanController ignored ->
                    opponentsWithMoreCardsInHandThanController(gameData, ctx);
            case OpponentsAttackedThisTurn ignored ->
                    opponentsAttackedThisTurn(gameData, ctx);
            case OpponentsDealtCombatDamageThisTurn ignored ->
                    opponentsDealtCombatDamageThisTurn(gameData, ctx);
            case OpponentsDealtCombatDamageBySourceNameOrSubtypeThisTurn sourceAmount ->
                    opponentsDealtCombatDamageBySourceNameOrSubtypeThisTurn(gameData, sourceAmount, ctx);
            case OpponentsWhoLostLifeThisTurn ignored ->
                    opponentsWhoLostLifeThisTurn(gameData, ctx);
            case TargetPlayerLifeTotal ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.playerLifeTotals.getOrDefault(ctx.targetPermanentId(), 0);
            case HalvedRoundedUp h ->
                    Math.floorDiv(evaluate(gameData, h.amount(), ctx) + 1, 2);
            case HalfControllerLifeRoundedUp ignored ->
                    ctx.controllerId() == null ? 0
                            : Math.max(0,
                            (gameData.playerLifeTotals.getOrDefault(ctx.controllerId(), 0) + 1) / 2);
            case IfSourceAttacking a ->
                    ctx.sourcePermanent() != null && ctx.sourcePermanent().isAttacking()
                            ? evaluate(gameData, a.whileAttacking(), ctx)
                            : evaluate(gameData, a.otherwise(), ctx);
            case GreatestPowerAmongControlled a ->
                    greatestPowerAmongControlled(gameData, a, ctx);
            case GreatestPowerAmongCardsInGraveyard a ->
                    greatestPowerAmongCardsInGraveyard(gameData, a, ctx);
            case GreatestManaValueAmongCardsInGraveyard a ->
                    greatestManaValueAmongCardsInGraveyard(gameData, a, ctx);
            case GreatestManaValueAmongControlled a ->
                    greatestManaValueAmongControlled(gameData, a, ctx);
            case GreatestManaValueAmongAttachedEquipment ignored ->
                    greatestManaValueAmongAttachedEquipment(gameData, ctx);
            case GreatestManaValueAmongOwnedCommanders ignored ->
                    greatestManaValueAmongOwnedCommanders(gameData, ctx);
            case GreatestManaValueAmongSpellsCastThisTurn a ->
                    greatestManaValueAmongSpellsCastThisTurn(gameData, a, ctx);
            case GreatestManaValueNotedForSourceThisTurn ignored ->
                    ctx.sourcePermanent() == null ? 0
                            : gameData.getGreatestManaValueNotedForPermanentThisTurn(
                                    ctx.sourcePermanent().getId());
            case GreatestStackSourceCountThisTurn ignored ->
                    gameData.getGreatestStackSourceCountThisTurn();
            case GreatestCreatureCountAmongPlayers ignored ->
                    greatestCreatureCountAmongPlayers(gameData);
            case GreatestPermanentCountAmongOpponents a ->
                    greatestPermanentCountAmongOpponents(gameData, a, ctx);
            case GreatestCreatureTypeCountAmongControlled ignored ->
                    greatestCreatureTypeCountAmongControlled(gameData, ctx);
            case GreatestToughnessAmongControlled a ->
                    greatestToughnessAmongControlled(gameData, a, ctx);
            case AttachmentsOnSource a ->
                    countAttachmentsOnSource(gameData, a, ctx);
            case CreaturesBlockedBySource ignored ->
                    countCreaturesBlockedBySource(ctx);
            case CreaturesBlockingSource count ->
                    countCreaturesBlockingSource(gameData, ctx, count.useTarget());
            case OpponentPoisonCounters ignored ->
                    countOpponentPoisonCounters(gameData, ctx);
            case OpponentsWithAtLeastPoisonCounters c ->
                    opponentsWithAtLeastPoisonCounters(gameData, c, ctx);
            case DefendingPlayerPoisonCounters ignored ->
                    countDefendingPlayerPoisonCounters(gameData, ctx);
            case OtherAttackersSharingCreatureTypeWithTarget ignored ->
                    countOtherAttackersSharingCreatureTypeWithTarget(gameData, ctx);
            case OtherControlledCreaturesSharingCreatureTypeWithTarget ignored ->
                    countOtherControlledCreaturesSharingCreatureTypeWithTarget(gameData, ctx);
            case PartySize ignored ->
                    partySize(gameData, ctx);
            case CreatureDeathsThisTurn c ->
                    countCreatureDeathsThisTurn(gameData, c, ctx);
            case CreaturesPutIntoOwnGraveyardThisTurn ignored ->
                    gameData.creaturesPutIntoOwnGraveyardThisTurnCount.getOrDefault(ctx.controllerId(), 0);
            case CreatureCardsInGraveyardFromBattlefieldThisTurn ignored ->
                    countCreatureCardsInGraveyardFromBattlefieldThisTurn(gameData, ctx);
            case NontokenCreaturesPutIntoOwnGraveyardThisTurn ignored ->
                    gameData.nontokenCreaturesPutIntoOwnGraveyardThisTurnCount
                            .getOrDefault(ctx.controllerId(), 0);
            case CreatureCardsExiledWithSource ignored ->
                    countCreatureCardsExiledWithSource(gameData, ctx);
            case CreaturesAttackedThisTurn c ->
                    countCreaturesAttackedThisTurn(gameData, c, ctx);
            case NontokenCreatureDeathsThisTurn c ->
                    countNontokenCreatureDeathsThisTurn(gameData, c, ctx);
            case CreatureSubtypeDeathsThisTurn c ->
                    countCreatureSubtypeDeathsThisTurn(gameData, c, ctx);
            case CreaturesExiledThisTurn c ->
                    countCreaturesExiledThisTurn(gameData, c, ctx);
            case CreaturesEnteredBattlefieldThisTurn c ->
                    countCreaturesEnteredBattlefieldThisTurn(gameData, c, ctx);
            case CreaturesLeftBattlefieldThisTurn c ->
                    countCreaturesLeftBattlefieldThisTurn(gameData, c, ctx);
            case CreaturesThatCrewedSourceThisTurn ignored ->
                    countCreaturesThatCrewedSourceThisTurn(gameData, ctx);
            case PermanentsEnteredBattlefieldThisTurn c ->
                    countPermanentsEnteredBattlefieldThisTurn(gameData, c, ctx);
            case PermanentsSacrificedThisTurn c ->
                    countPermanentsSacrificedThisTurn(gameData, c, ctx);
            case LifeGainedThisTurn c ->
                    countLifeGainedThisTurn(gameData, c, ctx);
            case LifeLostThisTurn c ->
                    countLifeLostThisTurn(gameData, c, ctx);
            case SpellsCastThisTurn c ->
                    countSpellsCastThisTurn(gameData, c, ctx);
            case SpellsCastSinceBeginningOfLastTurn c ->
                    countSpellsCastSinceBeginningOfLastTurn(gameData, c, ctx);
            case SpellsCastFromOutsideHandThisTurn c ->
                    countSpellsCastFromOutsideHandThisTurn(gameData, c, ctx);
            case TargetPlayerPoisonCounters ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.playerPoisonCounters.getOrDefault(ctx.targetPermanentId(), 0);
            case ControllerEnergyCounters ignored ->
                    ctx.controllerId() == null ? 0
                            : gameData.playerEnergyCounters.getOrDefault(ctx.controllerId(), 0);
            case EnergyCountersPaidOrLostThisTurn ignored ->
                    ctx.controllerId() == null ? 0
                            : gameData.energyCountersPaidOrLostThisTurn.getOrDefault(ctx.controllerId(), 0);
            case ControllerExperienceCounters ignored ->
                    ctx.controllerId() == null ? 0
                            : gameData.playerExperienceCounters.getOrDefault(ctx.controllerId(), 0);
            case ControllerRadCounters ignored ->
                    ctx.controllerId() == null ? 0
                            : gameData.playerRadCounters.getOrDefault(ctx.controllerId(), 0);
            case DamageDealtToTargetPlayerThisTurn ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.damageDealtToPlayersThisTurn.getOrDefault(ctx.targetPermanentId(), 0);
            case DamageDealtByTargetPlayerSorceryThisTurn ignored ->
                    damageDealtByTargetPlayerSorceryThisTurn(gameData, ctx);
            case UntappedLandsAtTurnStart ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.untappedLandsAtTurnStart.getOrDefault(ctx.targetPermanentId(), 0);
            case CardsDiscardedByTargetPlayerThisTurn ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.cardsDiscardedThisTurn.getOrDefault(ctx.targetPermanentId(), 0);
            case CardsDiscardedOrCycledThisTurn ignored ->
                    ctx.controllerId() == null ? 0
                            : gameData.cardsDiscardedThisTurn.getOrDefault(ctx.controllerId(), 0);
            case CardsPutIntoGraveyardByTargetPlayerThisTurn ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.cardsPutIntoGraveyardFromAnywhereThisTurn
                                    .getOrDefault(ctx.targetPermanentId(), java.util.Set.of()).size();
            case CardsPutIntoGraveyardFromHandOrLibraryThisTurn ignored -> {
                if (ctx.controllerId() == null) {
                    yield 0;
                }
                Set<UUID> cards = new HashSet<>(gameData.cardsPutIntoGraveyardFromHandThisTurn
                        .getOrDefault(ctx.controllerId(), java.util.Set.of()));
                cards.addAll(gameData.cardsPutIntoGraveyardFromLibraryThisTurn
                        .getOrDefault(ctx.controllerId(), java.util.Set.of()));
                yield cards.size();
            }
            case DescentsThisTurn ignored ->
                    ctx.controllerId() == null ? 0 : gameData.descentsThisTurn.getOrDefault(ctx.controllerId(), 0);
            case DamageDealtToControllerThisTurn ignored ->
                    ctx.controllerId() == null ? 0
                            : gameData.damageDealtToPlayersThisTurn.getOrDefault(ctx.controllerId(), 0);
            case DamageDealtToControllerByArtifactsThisTurn ignored ->
                    ctx.controllerId() == null ? 0
                            : gameData.artifactDamageDealtToPlayersThisTurn.getOrDefault(ctx.controllerId(), 0);
            case DamageDealtToOpponentsThisTurn ignored ->
                    damageDealtToOpponentsThisTurn(gameData, ctx);
            case NoncombatDamageDealtToOpponentsThisTurn ignored ->
                    noncombatDamageDealtToOpponentsThisTurn(gameData, ctx);
            case DamageDealtToSourceThisTurn ignored ->
                    ctx.sourcePermanent() == null ? 0
                            : gameData.damageDealtToPermanentsThisTurn
                                    .getOrDefault(ctx.sourcePermanent().getId(), 0);
            case DamageDealtToSourceByControllerThisTurn ignored ->
                    ctx.sourcePermanent() == null || ctx.controllerId() == null ? 0
                            : gameData.damageDealtToPermanentsBySourceControllerThisTurn
                                    .getOrDefault(ctx.sourcePermanent().getId(), java.util.Map.of())
                                    .getOrDefault(ctx.controllerId(), 0);
            case DamageDealtToTargetPermanentThisTurn ignored ->
                    ctx.targetPermanentId() == null ? 0
                            : gameData.damageDealtToPermanentsThisTurn
                                    .getOrDefault(ctx.targetPermanentId(), 0);
            case GreatestDamageDealtBySourceThisTurn ignored ->
                    greatestDamageDealtBySourceThisTurn(gameData);
            case DamageDealtToSourcePermanentBySourceNameThisTurn sourceDamage ->
                    damageDealtToSourcePermanentBySourceNameThisTurn(gameData, ctx, sourceDamage.sourceName());
            case TotalManaValueOfCardsExiledWithSource ignored ->
                    totalManaValueOfCardsExiledWithSource(gameData, ctx);
            case TotalManaValueOfCardsInGraveyard a ->
                    totalManaValueOfCardsInGraveyard(gameData, a, ctx);
            case TotalManaValueOfCardsOwnedInExile ignored ->
                    totalManaValueOfCardsOwnedInExile(gameData, ctx);
            case TotalManaValueOfOtherSpellsCastThisTurn ignored ->
                    totalManaValueOfOtherSpellsCastThisTurn(gameData, ctx);
            case TotalPowerOfCardsExiledWithSource ignored ->
                    totalPTOfCardsExiledWithSource(gameData, ctx, true);
            case TotalPowerOfControlledCreatures a ->
                    totalPowerOfControlledCreatures(gameData, a, ctx);
            case TotalToughnessOfCardsExiledWithSource ignored ->
                    totalPTOfCardsExiledWithSource(gameData, ctx, false);
            case TotalToughnessOfControlledCreatures ignored ->
                    totalToughnessOfControlledCreatures(gameData, ctx);
            case UnspentMana a ->
                    ctx.controllerId() == null || gameData.playerManaPools.get(ctx.controllerId()) == null
                            ? 0
                            : gameData.playerManaPools.get(ctx.controllerId())
                                    .getColoredManaTotals().getOrDefault(a.color(), 0);
            case LastDiscardedCardManaValue ignored ->
                    ctx.stackEntry() != null && ctx.stackEntry().getDiscardedCardSnapshot() != null
                            ? ctx.stackEntry().getDiscardedCardSnapshot().getManaValue()
                            : gameData.lastDiscardedCardManaValue;
            case LastDiscardedCardTypeCount ignored ->
                    gameData.lastDiscardedCardTypes.size();
            case GreatestDiscardedCardManaValue ignored ->
                    gameData.greatestDiscardedCardManaValue;
            case LastMilledCardColorSymbols a ->
                    gameData.lastMilledCardColorSymbols.getOrDefault(a.color(), 0);
            case ImprintedCardManaValue ignored ->
                    imprintedCardManaValue(gameData, ctx);
            case ImprintedCreaturePower ignored ->
                    imprintedCreaturePT(gameData, ctx, true);
            case ImprintedCreatureToughness ignored ->
                    imprintedCreaturePT(gameData, ctx, false);
            case LandsMatchingImprintedName ignored ->
                    countLandsMatchingImprintedName(gameData, ctx);
            case SourceCardPower cardPower -> {
                if (!cardPower.exiledCostCard()) yield sourceCardPower(gameData, ctx);
                StackEntry entry = ctx.stackEntry();
                if (entry == null || entry.getExiledCostCardSnapshot() == null) yield 0;
                var exiled = gameData.findExiledCard(entry.getExiledCostCardId());
                Integer power = gameQueryService.getEffectiveCardPower(gameData,
                        exiled == null ? entry.getExiledCostCardSnapshot() : exiled.card());
                yield power == null ? 0 : Math.max(0, power);
            }
            case SourceCardManaValue ignored ->
                    ctx.sourceCard() == null ? 0 : ctx.sourceCard().getManaValue();
            case SourceIntensity ignored -> {
                Permanent source = ctx.sourcePermanent();
                if (source == null && ctx.stackEntry() != null) {
                    source = ctx.stackEntry().getSourcePermanentSnapshot();
                }
                if (source != null) {
                    yield gameData.getCardIntensity(source.getCard());
                }
                yield gameData.getCardIntensity(ctx.sourceCard() != null
                        ? ctx.sourceCard()
                        : ctx.stackEntry() == null ? null : ctx.stackEntry().getCard());
            }
            case SourceManaValueMinusOne ignored ->
                    ctx.sourcePermanent() == null ? -1 : ctx.sourcePermanent().getCard().getManaValue() - 1;
            case SourcePower power -> {
                Permanent source = ctx.sourcePermanent();
                if (source == null && ctx.stackEntry() != null) {
                    source = ctx.stackEntry().getSourcePermanentSnapshot();
                }
                if (source == null) yield 0;
                if (power.baseOnly()) {
                    var bonus = gameQueryService.computeStaticBonus(gameData, source);
                    int base = bonus.basePTOverridden() ? bonus.basePowerOverride() : source.getBasePower();
                    yield power.allowNegative() ? base : Math.max(0, base);
                }
                int value = gameQueryService.findPermanentById(gameData, source.getId()) == null
                        && source.getLastKnownPower() != null
                        ? source.getLastKnownPower()
                        : gameQueryService.getEffectivePower(gameData, source);
                yield power.allowNegative() ? value : Math.max(0, value);
            }
            case SourceToughness ignored ->
                    ctx.sourcePermanent() == null ? 0
                            : Math.max(0, gameQueryService.findPermanentById(gameData, ctx.sourcePermanent().getId()) == null
                            && ctx.sourcePermanent().getLastKnownToughness() != null
                            ? ctx.sourcePermanent().getLastKnownToughness()
                            : gameQueryService.getEffectiveToughness(gameData, ctx.sourcePermanent()));
            case TargetToughness ignored ->
                    targetEffectiveToughness(gameData, ctx);
            case TriggeringPermanentToughness ignored ->
                    triggeringPermanentToughness(gameData, ctx);
            case TargetPower power ->
                    targetEffectivePower(gameData, ctx, power.allowNegative());
            case TargetPowerPlusToughness ignored ->
                    targetEffectivePowerPlusToughness(gameData, ctx);
            case TotalPowerOfTargetGroup targetGroup ->
                    totalPowerOfTargetGroup(gameData, targetGroup, ctx);
            case TargetManaValue ignored ->
                    targetManaValue(gameData, ctx);
            case TargetCardsManaValueSum sum ->
                    targetCardsManaValueSum(gameData, ctx, sum.graveyardOnly());
            case TargetGroupCount count ->
                    targetGroupCount(count, ctx);
            case TopCardOfLibraryManaValue ignored ->
                    topCardOfLibraryManaValue(gameData, ctx);
            case EnchantedPermanentManaValue ignored ->
                    enchantedPermanentManaValue(gameData, ctx);
            case EnchantedPermanentPower ignored ->
                    enchantedPermanentPower(gameData, ctx);
            case TargetSpellManaValue ignored ->
                    targetSpellManaValue(gameData, ctx);
            case TargetSpellPower ignored ->
                    targetSpellPower(gameData, ctx);
            case TriggeringSpellColorCount ignored ->
                    triggeringSpellColorCount(gameData, ctx);
            case TriggeringSpellColorManaSymbols symbolAmount ->
                    triggeringSpellColorManaSymbols(gameData, ctx, symbolAmount);
            case TriggeringSpellTargetCount targetAmount ->
                    triggeringSpellTargetCount(gameData, ctx, targetAmount);
            case ChosenPermanentPower ignored ->
                    chosenPermanentEffectivePower(gameData, ctx);
            case ChosenCreatureOrRevealedCardPower ignored ->
                    ctx.chosenPermanentId() == null
                            ? revealedCostCardPower(gameData, ctx)
                            : chosenPermanentEffectivePower(gameData, ctx);
            case BeheldPower ignored ->
                    beheldPower(gameData, ctx);
            case ChosenCreatureOrWarpedCardPower ignored ->
                    chosenCreatureOrWarpedCardPower(gameData, ctx);
            case ChosenNumberOnSource ignored ->
                    ctx.sourcePermanent() == null ? 0 : ctx.sourcePermanent().getChosenNumber();
        };
    }

    /**
     * Evaluates a death-trigger amount using the last-known source permanent when the source has
     * already left the battlefield. This is used for dynamic limits that are fixed as the trigger
     * is collected, such as Kodama of the Center Tree's soulshift value.
     */
    public int evaluateAtDeath(GameData gameData, DynamicAmount amount, UUID controllerId,
                               Permanent dyingPermanent) {
        AmountContext context = new AmountContext(controllerId, dyingPermanent, null, 0, 0);
        if (amount instanceof PermanentCount count) {
            return countPermanents(gameData, count, context, true);
        }
        return evaluate(gameData, amount, context);
    }

    /**
     * Projects the amount context onto a condition context so {@code FixedIfCondition} can reuse
     * the condition hierarchy. Only the values both contexts carry are transferred; cast-time flags
     * Most cast-time flags are not part of an amount context and read as false; madness is carried
     * because some divided amounts differ when a spell is cast using it.
     */
    private ConditionContext conditionContext(GameData gameData, AmountContext ctx) {
        UUID triggeringPermanentId = ctx.stackEntry() == null
                ? null : ctx.stackEntry().getTriggeringPermanentId();
        Card triggeringCard = ctx.stackEntry() == null
                ? null : gameQueryService.findCardById(gameData, ctx.stackEntry().getTriggeringCardId());
        return new ConditionContext(ctx.controllerId(),
                ctx.sourcePermanent() == null ? null : ctx.sourcePermanent().getId(),
                ctx.sourcePermanent(), ctx.sourceCard(), false, false, false, ctx.madness(), false,
                null, ctx.xValue(), ctx.targetPermanentId(), triggeringCard, ctx.staticEvaluation(), false,
                triggeringPermanentId, null, null).withEventValue(ctx.eventValue());
    }

    private int chosenPermanentEffectivePower(GameData gameData, AmountContext ctx) {
        if (ctx.chosenPermanentId() == null) return 0;
        Permanent chosen = gameQueryService.findPermanentById(gameData, ctx.chosenPermanentId());
        if (chosen == null) {
            StackEntry entry = ctx.stackEntry();
            Integer power = ctx.chosenPermanentPowerAtTrigger();
            Integer toughness = entry == null ? null : entry.getChosenPermanentToughnessAtLastKnown();
            if (usesToughnessForStationing(gameData, ctx)
                    && toughness != null && power != null && toughness > power) {
                return Math.max(0, toughness);
            }
            return power == null ? 0 : Math.max(0, power);
        }
        int power = gameQueryService.getEffectivePower(gameData, chosen);
        if (ctx.sourcePermanent() != null && ctx.sourcePermanent().getCard().hasKeyword(Keyword.STATION)) {
            power += stationPowerBonus(gameData, chosen);
        }
        if (usesToughnessForStationing(gameData, ctx)) {
            int toughness = gameQueryService.getEffectiveToughness(gameData, chosen);
            if (toughness > power) {
                return Math.max(0, toughness);
            }
        }
        return Math.max(0, power);
    }

    private int stationPowerBonus(GameData gameData, Permanent permanent) {
        int bonus = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(StationPowerModifierEffect.class::isInstance)
                .mapToInt(effect -> ((StationPowerModifierEffect) effect).powerBonus())
                .sum();
        return bonus + gameQueryService.computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .filter(StationPowerModifierEffect.class::isInstance)
                .mapToInt(effect -> ((StationPowerModifierEffect) effect).powerBonus())
                .sum();
    }

    private boolean usesToughnessForStationing(GameData gameData, AmountContext ctx) {
        if (ctx.sourcePermanent() == null
                || !ctx.sourcePermanent().getCard().hasKeyword(Keyword.STATION)
                || ctx.controllerId() == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        return battlefield.stream().anyMatch(permanent ->
                !permanent.isLosesAllAbilitiesUntilEndOfTurn()
                        && !gameQueryService.computeStaticBonus(gameData, permanent).losesAllAbilities()
                        && permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(effect -> effect instanceof StationPowerModifierEffect modifier
                                && modifier.usesToughnessInsteadOfPower()));
    }

    private int chosenCreatureOrWarpedCardPower(GameData gameData, AmountContext ctx) {
        StackEntry entry = ctx.stackEntry();
        if (entry == null) return 0;
        if (entry.getChosenPermanentId() != null) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, entry.getChosenPermanentId());
            if (chosen != null) {
                return Math.max(0, gameQueryService.getEffectivePower(gameData, chosen));
            }
            return entry.getChosenPermanentPowerAtLastKnown() == null
                    ? 0 : Math.max(0, entry.getChosenPermanentPowerAtLastKnown());
        }
        Card chosenCard = entry.getChosenObjectCard();
        return chosenCard == null || chosenCard.getPower() == null
                ? 0 : Math.max(0, chosenCard.getPower());
    }

    private int revealedCostCardPower(GameData gameData, AmountContext ctx) {
        StackEntry entry = ctx.stackEntry();
        if (entry == null || entry.getRevealedPowerCostCard() == null) return Math.max(0, ctx.xValue());
        Card revealed = entry.getRevealedPowerCostCard();
        boolean remainsInHand = gameData.playerHands.values().stream()
                .anyMatch(hand -> hand.stream().anyMatch(card -> card.getId().equals(revealed.getId())));
        if (remainsInHand) {
            Integer power = gameQueryService.getEffectiveCardPower(gameData, revealed);
            return power == null ? 0 : Math.max(0, power);
        }
        return entry.getRevealedPowerCostCardLastKnownPower();
    }

    private int beheldPower(GameData gameData, AmountContext ctx) {
        StackEntry entry = ctx.stackEntry();
        if (entry == null) return 0;
        if (entry.getBeholdPermanentId() != null) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, entry.getBeholdPermanentId());
            if (chosen != null) {
                return Math.max(0, gameQueryService.getEffectivePower(gameData, chosen));
            }
        }
        return Math.max(0, entry.getBeholdPower());
    }

    private int countOtherAttackersSharingCreatureTypeWithTarget(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        if (target == null) return 0;
        // Each other attacking creature that shares a creature type with the target counts once,
        // regardless of how many types it shares (CR 700.x, Shared Animosity ruling: counted as the
        // ability resolves). Changeling handling lives in GameQueryService.shareCreatureType.
        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttacking() && !permanent.getId().equals(target.getId())
                    && gameQueryService.shareCreatureType(gameData, target, permanent)) {
                count[0]++;
            }
        });
        return count[0];
    }

    private int countOtherControlledCreaturesSharingCreatureTypeWithTarget(
            GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        if (target == null) return 0;
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId == null) return 0;

        return (int) gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> !permanent.getId().equals(target.getId()))
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> gameQueryService.shareCreatureType(gameData, target, permanent))
                .count();
    }

    private int targetEffectiveToughness(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        return target == null ? 0 : Math.max(0, gameQueryService.getEffectiveToughness(gameData, target));
    }

    private int triggeringPermanentToughness(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() != null) {
            Permanent triggering = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
            if (triggering != null) {
                return Math.max(0, gameQueryService.getEffectiveToughness(gameData, triggering));
            }
        }
        return ctx.stackEntry() == null || ctx.stackEntry().getTriggeringPermanentToughnessAtTrigger() == null
                ? 0
                : Math.max(0, ctx.stackEntry().getTriggeringPermanentToughnessAtTrigger());
    }

    private int attachedPermanentColorCount(GameData gameData, AmountContext ctx) {
        Permanent source = ctx.sourcePermanent();
        if (source == null || source.getAttachedTo() == null) {
            return 0;
        }
        var layered = LayerSystemService.activeStateFor(source.getAttachedTo());
        if (layered != null) {
            return layered.getColors().size();
        }
        Permanent attached = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        return attached == null ? 0 : gameQueryService.getEffectiveColors(gameData, attached).size();
    }

    private int targetPermanentColorCount(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        return target == null ? 0 : gameQueryService.getEffectiveColors(gameData, target).size();
    }

    private int targetEffectivePower(GameData gameData, AmountContext ctx) {
        return targetEffectivePower(gameData, ctx, false);
    }

    private int targetEffectivePower(GameData gameData, AmountContext ctx, boolean allowNegative) {
        if (ctx.targetPermanentId() == null) return 0;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        // No legal target at resolution -> 0, matching the fizzle behaviour of the handlers this replaces.
        if (target != null) {
            int power = gameQueryService.getEffectivePower(gameData, target);
            return allowNegative ? power : Math.max(0, power);
        }
        Integer lastKnownPower = ctx.stackEntry() == null ? null
                : ctx.stackEntry().getLastKnownPermanentPowers().get(ctx.targetPermanentId());
        if (lastKnownPower != null) {
            return allowNegative ? lastKnownPower : Math.max(0, lastKnownPower);
        }
        return ctx.stackEntry() != null && ctx.stackEntry().isNonTargeting()
                && ctx.targetPermanentId().equals(ctx.stackEntry().getTriggeringPermanentId())
                && ctx.triggeringPermanentPowerAtTrigger() != null
                ? allowNegative ? ctx.triggeringPermanentPowerAtTrigger()
                        : Math.max(0, ctx.triggeringPermanentPowerAtTrigger()) : 0;
    }

    private int targetEffectivePowerPlusToughness(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        if (target == null) return 0;
        return gameQueryService.getEffectivePower(gameData, target)
                + gameQueryService.getEffectiveToughness(gameData, target);
    }

    private int targetManaValue(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        Card card = target != null ? target.getCard()
                : ctx.stackEntry() != null ? ctx.stackEntry().lastKnownPermanentCard(ctx.targetPermanentId()) : null;
        return card == null ? 0 : card.getManaValue();
    }

    private int topCardOfLibraryManaValue(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        List<Card> library = gameData.playerDecks.get(ctx.controllerId());
        return library == null || library.isEmpty() ? 0 : library.getFirst().getManaValue();
    }

    /** Mana value of the permanent the source Aura enchants (Soul Tithe); 0 if there is none. */
    private int enchantedPermanentManaValue(GameData gameData, AmountContext ctx) {
        Permanent source = ctx.sourcePermanent();
        if (source == null || source.getAttachedTo() == null) return 0;
        Permanent enchanted = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        return enchanted == null ? 0 : enchanted.getCard().getManaValue();
    }

    /** Effective power of the permanent the source Aura enchants; 0 if there is none. */
    private int enchantedPermanentPower(GameData gameData, AmountContext ctx) {
        Permanent source = ctx.sourcePermanent();
        if (source != null && source.getAttachedTo() != null) {
            Permanent enchanted = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
            if (enchanted != null) {
                return Math.max(0, gameQueryService.getEffectivePower(gameData, enchanted));
            }
        }
        Integer lastKnownPower = ctx.triggeringPermanentPowerAtTrigger();
        return lastKnownPower == null ? 0 : Math.max(0, lastKnownPower);
    }

    /** Mana value of the targeted or spell-cast-triggering spell on the stack; 0 if it has left. */
    private int targetSpellManaValue(GameData gameData, AmountContext ctx) {
        UUID triggeringCardId = ctx.stackEntry() == null ? null : ctx.stackEntry().getTriggeringCardId();
        for (StackEntry se : gameData.stack) {
            boolean isTargetedSpell = ctx.targetPermanentId() != null
                    && se.getTargetableId().equals(ctx.targetPermanentId());
            boolean isTriggeringSpell = triggeringCardId != null
                    && se.getTargetableId().equals(triggeringCardId);
            if (isTargetedSpell || isTriggeringSpell) {
                Card spellCard = se.getTargetingCard();
                return (spellCard == null ? se.getCard() : spellCard).getManaValue() + se.getXValue();
            }
        }
        return ctx.stackEntry() == null ? 0 : ctx.stackEntry().getEventValue();
    }

    /** Printed power of the targeted creature spell on the stack (Essence Backlash); 0 if gone. */
    private int targetSpellPower(GameData gameData, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return 0;
        for (StackEntry se : gameData.stack) {
            if (se.getTargetableId().equals(ctx.targetPermanentId())) {
                Integer power = se.getCard().getPower();
                return power == null ? 0 : Math.max(0, power);
            }
        }
        return 0;
    }

    /**
     * Whether the amount (recursively) reads the stack entry's snapshotted x value —
     * used by trigger collectors to decide if an entry needs {@code xValue} populated.
     */
    public boolean referencesXValue(DynamicAmount amount) {
        return switch (amount) {
            case XValue ignored -> true;
            case TriggeringSpellXValue ignored -> false;
            case ManaSpentToCast ignored -> true;
            case ChosenCreatureOrRevealedCardPower ignored -> true;
            case SnowManaSpentToCast ignored -> false;
            case TreasureManaSpentToCast ignored -> false;
            case TreasureManaSpentToActivate ignored -> false;
            case CardsDrawnThisResolution ignored -> false;
            case Scaled s -> referencesXValue(s.amount());
            case Divided d -> referencesXValue(d.amount());
            case HalvedRoundedUp h -> referencesXValue(h.amount());
            case Sum s -> s.amounts().stream().anyMatch(this::referencesXValue);
            case Min m -> m.amounts().stream().anyMatch(this::referencesXValue);
            case Max m -> m.amounts().stream().anyMatch(this::referencesXValue);
            default -> false;
        };
    }

    /**
     * Whether the amount (recursively) reads the stack entry's snapshotted event value — used by
     * trigger collectors (and the excess-damage producer) to decide if an entry needs its
     * {@code eventValue} populated. The event-value analogue of {@link #referencesXValue}.
     */
    public boolean referencesEventValue(DynamicAmount amount) {
        return switch (amount) {
            case null -> false;
            case EventValue ignored -> true;
            case SnowManaSpentToCast ignored -> false;
            case TreasureManaSpentToCast ignored -> false;
            case TreasureManaSpentToActivate ignored -> false;
            case CardsDrawnThisResolution ignored -> false;
            case Scaled s -> referencesEventValue(s.amount());
            case Divided d -> referencesEventValue(d.amount());
            case HalvedRoundedUp h -> referencesEventValue(h.amount());
            case Sum s -> s.amounts().stream().anyMatch(this::referencesEventValue);
            case Min m -> m.amounts().stream().anyMatch(this::referencesEventValue);
            case Max m -> m.amounts().stream().anyMatch(this::referencesEventValue);
            default -> false;
        };
    }

    /** Whether the amount reads the mana value of the spell that caused a trigger. */
    public boolean referencesTargetSpellManaValue(DynamicAmount amount) {
        return switch (amount) {
            case null -> false;
            case TargetSpellManaValue ignored -> true;
            case Scaled scaled -> referencesTargetSpellManaValue(scaled.amount());
            case Divided divided -> referencesTargetSpellManaValue(divided.amount());
            case HalvedRoundedUp halved -> referencesTargetSpellManaValue(halved.amount());
            case Sum sum -> sum.amounts().stream().anyMatch(this::referencesTargetSpellManaValue);
            case Min min -> min.amounts().stream().anyMatch(this::referencesTargetSpellManaValue);
            case Max max -> max.amounts().stream().anyMatch(this::referencesTargetSpellManaValue);
            default -> false;
        };
    }

    private int countPermanents(GameData gameData, PermanentCount count, AmountContext ctx) {
        if (count.declaredAttackersOnly()) {
            return ctx.stackEntry() == null ? 0 : (int) ctx.stackEntry().getAttackingPermanentSnapshots().stream()
                    .filter(attacker -> count.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                            attacker, count.filter(), FilterContext.empty().withSourceControllerId(ctx.controllerId())))
                    .count();
        }
        return countPermanents(gameData, count, ctx, false);
    }

    private int countUnlockedRoomDoors(GameData gameData, UnlockedRoomDoorsCount amount,
                                       AmountContext ctx) {
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) {
                continue;
            }
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (!gameQueryService.isEnchantment(gameData, permanent)
                        || !gameQueryService.hasEffectiveSubtype(gameData, permanent, CardSubtype.ROOM)) {
                    continue;
                }
                if (permanent.isRoomDoorUnlocked(0)) {
                    count++;
                }
                if (permanent.isRoomDoorUnlocked(1)) {
                    count++;
                }
            }
        }
        return count;
    }

    private int targetCardsManaValueSum(GameData gameData, AmountContext ctx, boolean graveyardOnly) {
        List<java.util.UUID> targetCardIds = ctx.targetCardIds();
        if (targetCardIds == null || targetCardIds.isEmpty()) {
            targetCardIds = ctx.targetPermanentId() == null ? List.of() : List.of(ctx.targetPermanentId());
        }
        if (targetCardIds.isEmpty()) {
            return 0;
        }
        return targetCardIds.stream()
                .map(id -> {
                    Card card = gameQueryService.findCardInGraveyardById(gameData, id);
                    if (card == null && !graveyardOnly) {
                        var exiledCard = gameData.findExiledCard(id);
                        card = exiledCard == null ? null : exiledCard.card();
                    }
                    if (card == null && !graveyardOnly) {
                        card = gameData.playerHands.values().stream()
                                .flatMap(List::stream)
                                .filter(handCard -> handCard.getId().equals(id))
                                .findFirst()
                                .orElse(null);
                    }
                    return card;
                })
                .filter(java.util.Objects::nonNull)
                .mapToInt(Card::getManaValue)
                .sum();
    }

    private int countDistinctPermanentNames(GameData gameData, DistinctPermanentNamesCount count,
                                            AmountContext ctx) {
        FilterContext filterContext = GameQueryService.isStaticEvaluationActive()
                ? FilterContext.empty()
                : FilterContext.of(gameData);
        filterContext = filterContext.withSourceControllerId(ctx.controllerId());
        if (ctx.sourcePermanent() != null) {
            filterContext = filterContext
                    .withSourceCardId(ctx.sourcePermanent().getCard().getId())
                    .withSourcePermanentSnapshot(ctx.sourcePermanent());
        }
        java.util.Set<String> names = new java.util.HashSet<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, count.filter(), filterContext)) {
                    names.add(permanent.getCard().getName());
                }
            }
        }
        return names.size();
    }

    private int sumPermanentManaValues(GameData gameData, PermanentManaValueSum amount, AmountContext ctx) {
        FilterContext filterContext = GameQueryService.isStaticEvaluationActive()
                ? FilterContext.empty()
                : FilterContext.of(gameData);
        filterContext = filterContext.withSourceControllerId(ctx.controllerId());
        if (ctx.sourcePermanent() != null) {
            filterContext = filterContext
                    .withSourceCardId(ctx.sourcePermanent().getCard().getId())
                    .withSourcePermanentSnapshot(ctx.sourcePermanent());
        }

        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (amount.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(permanent, amount.filter(), filterContext)) {
                    total += permanent.isFaceDown() ? 0 : permanent.getCard().getManaValue();
                }
            }
        }
        return total;
    }

    private int sumPermanentCounters(GameData gameData, PermanentCounterSum amount, AmountContext ctx) {
        FilterContext filterContext = GameQueryService.isStaticEvaluationActive()
                ? FilterContext.empty()
                : FilterContext.of(gameData);
        filterContext = filterContext.withSourceControllerId(ctx.controllerId());
        if (ctx.sourcePermanent() != null) {
            filterContext = filterContext
                    .withSourceCardId(ctx.sourcePermanent().getCard().getId())
                    .withSourcePermanentSnapshot(ctx.sourcePermanent());
        }

        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (amount.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(permanent, amount.filter(), filterContext)) {
                    total += amount.counterType() == null
                            ? permanent.getTotalCounterCount()
                            : permanent.getCounterCount(amount.counterType());
                }
            }
        }
        return total;
    }

    private int totalCountersAmongPlayersAndPermanents(GameData gameData) {
        int playerCounters = gameData.orderedPlayerIds.stream()
                .mapToInt(playerId -> gameData.playerPoisonCounters.getOrDefault(playerId, 0)
                        + gameData.playerRadCounters.getOrDefault(playerId, 0)
                        + gameData.playerEnergyCounters.getOrDefault(playerId, 0)
                        + gameData.playerSparkCounters.getOrDefault(playerId, 0)
                        + gameData.playerExperienceCounters.getOrDefault(playerId, 0))
                .sum();
        int permanentCounters = gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .mapToInt(Permanent::getTotalCounterCount)
                .sum();
        return playerCounters + permanentCounters;
    }

    private int countPermanents(GameData gameData, PermanentCount count, AmountContext ctx,
                                boolean includeSourceIfAbsent) {
        // In static evaluation ordinary filters use a null GameData: type and keyword checks then
        // use intrinsic values, so counting never calls computeStaticBonus on other permanents
        // (which could recurse back into the count being computed). Ownership-sensitive filters
        // retain live board context because ownership is not intrinsic to a Permanent. The P/T
        // leaves are exempt — they route through GameQueryService's recursion-safe accessors. The
        // source identity is supplied either way: it costs no query, and without it source-relative
        // predicates silently match nothing.
        boolean staticEvaluation = GameQueryService.isStaticEvaluationActive();
        boolean ownershipNeedsBoard = staticEvaluation
                && predicateEvaluationService.requiresGameDataForStaticFilter(count.filter());
        FilterContext filterContext = staticEvaluation && !ownershipNeedsBoard
                ? FilterContext.empty()
                : FilterContext.of(gameData);
        filterContext = filterContext.withSourceControllerId(ctx.controllerId());
        // Source-relative predicates (e.g. PermanentHasSameNameAsSourcePredicate on
        // Powerstone Shard's "for each artifact you control named ~") need the source card.
        // PermanentIsHostOfSourceAuraPredicate also needs the live source permanent when
        // staticEvaluation nulls GameData (Vampirism: "+1/+1 for each other creature you control").
        if (ctx.sourcePermanent() != null) {
            filterContext = filterContext
                    .withSourceCardId(ctx.sourcePermanent().getCard().getId())
                    .withSourcePermanentSnapshot(ctx.sourcePermanent());
        }
        int matches = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (count.excludeSource() && ctx.sourcePermanent() != null
                        && permanent.getId().equals(ctx.sourcePermanent().getId())) {
                    continue;
                }
                boolean matchesFilter = staticEvaluation
                        ? predicateEvaluationService.matchesStaticFilter(permanent, count.filter(), filterContext)
                        : predicateEvaluationService.matchesPermanentPredicate(permanent, count.filter(), filterContext);
                if (matchesFilter) {
                    matches += CreatureCountSupport.countsCreatures(count.filter())
                            ? CreatureCountSupport.creatureCount(gameData, permanent, gameQueryService) : 1;
                }
            }
        }
        if (includeSourceIfAbsent && !count.excludeSource() && ctx.sourcePermanent() != null
                && isPlayerInScope(gameData, ctx.controllerId(), count.scope(), ctx)
                && gameQueryService.findPermanentById(gameData, ctx.sourcePermanent().getId()) == null
                && predicateEvaluationService.matchesPermanentPredicate(ctx.sourcePermanent(), count.filter(), filterContext)) {
            matches += CreatureCountSupport.countsCreatures(count.filter())
                    ? CreatureCountSupport.creatureCount(gameData, ctx.sourcePermanent(), gameQueryService) : 1;
        }
        return matches;
    }

    /**
     * Domain (an ability word, CR 207.2c): the number of distinct basic land types among lands
     * the controller controls. CR 305.7 land-type overrides (Blood Moon, Urborg, Prismatic Omen)
     * count in both branches; static evaluation reads them through the recursion-safe accessor,
     * which falls back to printed types only for a land whose own static bonus is being assembled.
     */
    private int countBasicLandTypesAmongControlledLands(
            GameData gameData, BasicLandTypesAmongControlledLands amount, AmountContext ctx) {
        java.util.Set<CardSubtype> found = java.util.EnumSet.noneOf(CardSubtype.class);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (!permanent.getCard().hasType(CardType.LAND)) continue;
                found.addAll(GameQueryService.isStaticEvaluationActive()
                        ? gameQueryService.basicLandTypesForStaticEvaluation(gameData, permanent)
                        : gameQueryService.effectiveBasicLandTypes(gameData, permanent));
            }
        }
        return found.size();
    }

    /**
     * Distinct card types among non-token cards in the scoped graveyard(s). Multi-type cards
     * contribute each printed type (artifact creature → Artifact + Creature).
     */
    private int countCardTypesAmongCardsInGraveyard(
            GameData gameData, CardTypesAmongCardsInGraveyard amount, AmountContext ctx) {
        java.util.Set<CardType> found = java.util.EnumSet.noneOf(CardType.class);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                if (card.getType() != null) {
                    found.add(card.getType());
                }
                found.addAll(card.getAdditionalTypes());
            }
        }
        return found.size();
    }

    private int countPermanentTypesAmongCardsInGraveyard(GameData gameData, AmountContext ctx) {
        Set<CardType> found = EnumSet.noneOf(CardType.class);
        if (ctx.controllerId() == null) return 0;
        for (Card card : gameData.playerGraveyards.getOrDefault(ctx.controllerId(), List.of())) {
            if (card.isToken()) continue;
            if (card.getType() != null && card.getType().isPermanentType()) {
                found.add(card.getType());
            }
            card.getAdditionalTypes().stream()
                    .filter(CardType::isPermanentType)
                    .forEach(found::add);
        }
        return found.size();
    }

    private int countCardTypesAmongPermanentsSacrificedThisTurn(
            GameData gameData, AmountContext ctx) {
        Set<CardType> found = EnumSet.noneOf(CardType.class);
        if (ctx.controllerId() == null) return 0;
        for (Card card : gameData.permanentsSacrificedThisTurn
                .getOrDefault(ctx.controllerId(), List.of())) {
            if (card.getType() != null && card.getType().isPermanentType()) {
                found.add(card.getType());
            }
            card.getAdditionalTypes().stream()
                    .filter(CardType::isPermanentType)
                    .forEach(found::add);
        }
        return found.size();
    }

    /**
     * Distinct noncreature subtypes among non-token cards in the scoped graveyard(s). A card can
     * contribute multiple subtypes, and creature subtypes such as Human or Lhurgoyf are excluded.
     */
    private int countNonCreatureSubtypesAmongCardsInGraveyard(
            GameData gameData, NonCreatureSubtypesAmongCardsInGraveyard amount, AmountContext ctx) {
        Set<CardSubtype> found = EnumSet.noneOf(CardSubtype.class);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                found.addAll(gameQueryService.getCardSubtypes(card, gameData, playerId).stream()
                        .filter(subtype -> !gameQueryService.isCreatureSubtype(subtype))
                        .toList());
            }
        }
        return found.size();
    }

    private int countCardTypesAmongControlledPermanents(
            GameData gameData, CardTypesAmongControlledPermanents amount, AmountContext ctx) {
        boolean staticEvaluation = GameQueryService.isStaticEvaluationActive();
        boolean ownershipNeedsBoard = staticEvaluation
                && predicateEvaluationService.requiresGameDataForStaticFilter(amount.filter());
        FilterContext filterContext = staticEvaluation && !ownershipNeedsBoard
                ? FilterContext.empty() : FilterContext.of(gameData);
        filterContext = filterContext.withSourceControllerId(ctx.controllerId());
        if (ctx.sourcePermanent() != null) {
            filterContext = filterContext
                    .withSourceCardId(ctx.sourcePermanent().getCard().getId())
                    .withSourcePermanentSnapshot(ctx.sourcePermanent());
        }

        java.util.Set<CardType> found = java.util.EnumSet.noneOf(CardType.class);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (amount.excludeSource() && ctx.sourcePermanent() != null
                        && permanent.getId().equals(ctx.sourcePermanent().getId())) {
                    continue;
                }
                boolean matches = ownershipNeedsBoard
                        ? predicateEvaluationService.matchesStaticFilter(permanent, amount.filter(), filterContext)
                        : predicateEvaluationService.matchesPermanentPredicate(permanent, amount.filter(), filterContext);
                if (!matches) continue;

                if (staticEvaluation) {
                    var state = LayerSystemService.activeStateFor(permanent.getId());
                    if (state != null) {
                        found.addAll(state.getCardTypes());
                        continue;
                    }
                }
                found.addAll(gameQueryService.getEffectiveCardTypes(gameData, permanent));
            }
        }
        return found.size();
    }

    private int countCardTypesAmongSpellsCastThisTurn(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) {
            return 0;
        }
        Set<CardType> found = EnumSet.noneOf(CardType.class);
        for (Card spell : gameData.getSpellsCastThisTurn(ctx.controllerId())) {
            if (spell.getType() != null) {
                found.add(spell.getType());
            }
            found.addAll(spell.getAdditionalTypes());
        }
        return found.size();
    }

    private int countDistinctManaCostsAmongCardsInGraveyard(
            GameData gameData, DistinctManaCostsAmongCardsInGraveyard amount, AmountContext ctx) {
        java.util.Set<String> found = new java.util.HashSet<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.isToken() || card.hasType(CardType.LAND) || card.getManaCost() == null) continue;
                found.add(card.getManaCost());
            }
        }
        return found.size();
    }

    private int countDistinctPermanentNamesAmongControlled(
            GameData gameData, DistinctPermanentNamesAmongControlled amount, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        FilterContext filterContext = GameQueryService.isStaticEvaluationActive()
                ? FilterContext.empty()
                : FilterContext.of(gameData);
        filterContext = filterContext.withSourceControllerId(ctx.controllerId());
        if (ctx.sourcePermanent() != null) {
            filterContext = filterContext
                    .withSourceCardId(ctx.sourcePermanent().getCard().getId())
                    .withSourcePermanentSnapshot(ctx.sourcePermanent());
        }
        java.util.Set<String> found = new java.util.HashSet<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(
                        permanent, amount.filter(), filterContext)) {
                    found.add(permanent.getCard().getName());
                }
            }
        }
        return found.size();
    }

    private int countDistinctManaValuesAmongCardsInGraveyard(
            GameData gameData, DistinctManaValuesAmongCardsInGraveyard amount, AmountContext ctx) {
        Set<Integer> found = new HashSet<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (!card.isToken()
                        && (!amount.nonlandOnly() || !card.hasType(CardType.LAND))
                        && predicateEvaluationService.matchesCardPredicate(
                        card, amount.filter(), null, gameData, playerId)) {
                    found.add(card.getManaValue());
                }
            }
        }
        return found.size();
    }

    private int countDistinctManaValuesAmongControlledPermanents(GameData gameData, AmountContext ctx) {
        if (gameData == null || ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        return (int) battlefield.stream()
                .filter(permanent -> GameQueryService.isStaticEvaluationActive()
                        ? !permanent.getCard().hasType(CardType.LAND)
                        : !gameQueryService.isLand(gameData, permanent))
                .mapToInt(permanent -> permanent.isFaceDown() ? 0 : permanent.getCard().getManaValue())
                .distinct()
                .count();
    }

    private int countDistinctCounterKindsAmongControlledPermanents(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;

        Set<CounterType> found = EnumSet.noneOf(CounterType.class);
        for (Permanent permanent : battlefield) {
            for (CounterType counterType : CounterType.values()) {
                if (counterType != CounterType.ANY && counterType != CounterType.SILVER
                        && permanent.getCounterCount(counterType) > 0) {
                    found.add(counterType);
                }
            }
        }
        return found.size();
    }

    private int countDistinctKeywordAbilitiesAmongControlledCreatures(
            GameData gameData, DistinctKeywordAbilitiesAmongControlledCreatures amount, AmountContext ctx) {
        if (ctx.controllerId() == null || amount.abilities().isEmpty()) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;

        java.util.Set<Keyword> found = java.util.EnumSet.noneOf(Keyword.class);
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) continue;
            for (Keyword ability : amount.abilities()) {
                if (gameQueryService.hasKeyword(gameData, permanent, ability)) {
                    found.add(ability);
                }
            }
        }
        return found.size();
    }

    private int countCreatureTypesAmongControlledCreatures(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;

        Set<CardSubtype> found = EnumSet.noneOf(CardSubtype.class);
        Set<CardSubtype> allCreatureTypes = EnumSet.noneOf(CardSubtype.class);
        for (CardSubtype subtype : CardSubtype.values()) {
            if (gameQueryService.isCreatureSubtype(subtype)) {
                allCreatureTypes.add(subtype);
            }
        }

        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) continue;
            if (gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING)) {
                found.addAll(allCreatureTypes);
            } else {
                found.addAll(gameQueryService.effectiveCreatureSubtypes(gameData, permanent));
            }
        }
        return found.size();
    }

    private int countDistinctColorPairsAmongControlledPermanents(GameData gameData, AmountContext ctx) {
        if (gameData == null || ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        Set<Set<CardColor>> pairs = new HashSet<>();
        for (Permanent permanent : battlefield) {
            Set<CardColor> colors = GameQueryService.isStaticEvaluationActive()
                    ? gameQueryService.colorsForStaticEvaluation(permanent)
                    : gameQueryService.getEffectiveColors(gameData, permanent);
            if (colors.size() == 2) {
                pairs.add(Set.copyOf(colors));
            }
        }
        return pairs.size();
    }

    private int countColorManaSymbolsAmongControlledPermanents(
            GameData gameData, ColorManaSymbolsAmongControlledPermanents amount, AmountContext ctx) {
        return gameQueryService.getDevotionToColor(gameData, ctx.controllerId(), amount.color());
    }

    private int colorManaPairsSpentToCast(
            GameData gameData, ColorManaPairsSpentToCast amount, AmountContext ctx) {
        if (ctx.sourceCard() == null) return 0;
        return gameData.getSpellCastManaSpentByColor(ctx.sourceCard().getId(), amount.color()) / 2;
    }

    private int countColorManaSymbolsInGraveyard(
            GameData gameData, ColorManaSymbolsInGraveyard amount, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                ManaCost cost = card.getParsedManaCost();
                if (cost != null) {
                    total += cost.countColorSymbols(amount.color());
                }
            }
        }
        return total;
    }

    private int countGraveyardCards(GameData gameData, CardsInGraveyard count, AmountContext ctx) {
        StackEntry entry = ctx.stackEntry();
        String spellName = entry == null ? null : switch (entry.getEntryType()) {
            case TRIGGERED_ABILITY, ACTIVATED_ABILITY -> null;
            case CREATURE_SPELL, ENCHANTMENT_SPELL, SORCERY_SPELL, INSTANT_SPELL,
                    ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> entry.getCard().getName();
        };
        int matches = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                Card sourceCard = ctx.sourceCard() != null ? ctx.sourceCard()
                        : ctx.sourcePermanent() != null ? ctx.sourcePermanent().getCard() : null;
                if (count.excludeSourceCard() && sourceCard != null
                        && sourceCard.getId().equals(card.getId())) continue;
                if (matchesGraveyardCountFilter(gameData, card, count.filter(), playerId, spellName)) {
                    matches++;
                }
            }
        }
        return matches;
    }

    private int countCreatureCardsInGraveyardFromBattlefieldThisTurn(GameData gameData,
                                                                      AmountContext ctx) {
        if (ctx.controllerId() == null) {
            return 0;
        }
        Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn
                .getOrDefault(ctx.controllerId(), Set.of());
        if (tracked.isEmpty()) {
            return 0;
        }
        List<Card> graveyard = gameData.playerGraveyards.get(ctx.controllerId());
        if (graveyard == null) {
            return 0;
        }
        return (int) graveyard.stream()
                .filter(card -> !card.isToken() && tracked.contains(card.getId()))
                .count();
    }

    private int sourceCardPower(GameData gameData, AmountContext ctx) {
        Card sourceCard = ctx.sourceCard();
        if (sourceCard == null) {
            return 0;
        }
        for (var effect : sourceCard.getEffects(EffectSlot.STATIC)) {
            if (effect instanceof SetPowerToughnessToAmountEffect cda) {
                return Math.max(0, evaluate(gameData, cda.power(), ctx));
            }
        }
        return sourceCard.getPower() == null ? 0 : Math.max(0, sourceCard.getPower());
    }

    private boolean matchesGraveyardCountFilter(GameData gameData, Card card, CardPredicate filter,
                                                UUID ownerId, String spellName) {
        if (spellName == null || filter == null) {
            return predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData, ownerId);
        }
        // Only name checks within this spell's graveyard count see the extra name. Other
        // characteristics and ordinary predicate evaluation continue to use the original card.
        return switch (filter) {
            case CardNamedPredicate named -> named.cardName().equals(card.getName())
                    || gameQueryService.getEffectiveGraveyardEffects(gameData, card, EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof CountAsNamedCardForSpellEffect countAs
                            && countAs.spellName().equals(spellName)
                            && countAs.cardName().equals(named.cardName()));
            case CardAllOfPredicate all -> all.predicates().stream()
                    .allMatch(part -> matchesGraveyardCountFilter(gameData, card, part, ownerId, spellName));
            case CardAnyOfPredicate any -> any.predicates().stream()
                    .anyMatch(part -> matchesGraveyardCountFilter(gameData, card, part, ownerId, spellName));
            case CardNotPredicate not ->
                    !matchesGraveyardCountFilter(gameData, card, not.predicate(), ownerId, spellName);
            default -> predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData, ownerId);
        };
    }

    private int countGraveyardsAtLeast(GameData gameData, GraveyardsAtLeast count) {
        int matches = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            long cardCount = graveyard.stream().filter(card -> !card.isToken()).count();
            if (cardCount >= count.threshold()) {
                matches++;
            }
        }
        return matches;
    }

    private int greatestPowerAmongCardsInGraveyard(
            GameData gameData, GreatestPowerAmongCardsInGraveyard amount, AmountContext ctx) {
        int greatestPower = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                Integer power = gameQueryService.getEffectiveCardPower(gameData, card);
                if (card.isToken()
                        || !predicateEvaluationService.matchesCardPredicate(card, amount.filter(), null, gameData, playerId)
                        || power == null) {
                    continue;
                }
                greatestPower = Math.max(greatestPower, power);
            }
        }
        return greatestPower;
    }

    private int greatestManaValueAmongCardsInGraveyard(
            GameData gameData, GreatestManaValueAmongCardsInGraveyard amount, AmountContext ctx) {
        int greatestManaValue = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (!card.isToken() && predicateEvaluationService.matchesCardPredicate(
                        card, amount.filter(), null, gameData, playerId)) {
                    greatestManaValue = Math.max(greatestManaValue, card.getManaValue());
                }
            }
        }
        return greatestManaValue;
    }

    private int countExileCards(GameData gameData, CardsInExile count, AmountContext ctx) {
        int matches = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            for (Card card : gameData.getPlayerExiledCards(playerId)) {
                if (card.isToken()) continue;
                var exiledEntry = gameData.findExiledCard(card.getId());
                if (exiledEntry != null && exiledEntry.faceDown() && count.filter() != null) continue;
                if (predicateEvaluationService.matchesCardPredicate(card, count.filter(), null)) {
                    matches++;
                }
            }
        }
        return matches;
    }

    private int countSuspendedCards(GameData gameData, SuspendedCards count, AmountContext ctx) {
        int matches = 0;
        synchronized (gameData.exiledCards) {
            for (var exiled : gameData.exiledCards) {
                if (exiled.faceDown() || !isPlayerInScope(gameData, exiled.ownerId(), count.scope(), ctx)) {
                    continue;
                }
                UUID cardId = exiled.card().getId();
                boolean hasSuspendCounters = gameData.exiledCardTimeCounters.getOrDefault(cardId, 0) > 0;
                boolean isSuspendedSpell = gameData.suspendedSpellExiles.stream()
                        .anyMatch(pending -> cardId.equals(pending.cardId()) && pending.counters() > 0);
                if (hasSuspendCounters || isSuspendedSpell) {
                    matches++;
                }
            }
        }
        return matches;
    }

    private int countDelvedCards(GameData gameData, CardsDelved count, AmountContext ctx) {
        if (ctx.stackEntry() == null) {
            return 0;
        }
        int matches = 0;
        for (UUID cardId : ctx.stackEntry().getDelvedCardIds()) {
            var exiledCard = gameData.findExiledCard(cardId);
            if (exiledCard != null && !exiledCard.card().isToken()
                    && predicateEvaluationService.matchesCardPredicate(exiledCard.card(), count.filter(), null)) {
                matches++;
            }
        }
        return matches;
    }

    private int countForetoldCardsInExile(GameData gameData, ForetoldCardsInExile count,
                                          AmountContext ctx) {
        int matches = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            for (Card card : gameData.getPlayerExiledCards(playerId)) {
                if (!card.isToken() && gameData.foretoldCardIds.contains(card.getId())) {
                    matches++;
                }
            }
        }
        return matches;
    }

    private int countCountersOnLinkedPermanent(GameData gameData, CountersOnLinkedPermanent count) {
        Permanent linked = gameQueryService.findPermanentById(gameData, count.linkedPermanentId());
        return linked == null ? 0 : linked.getCounterCount(count.counterType());
    }

    private int countersOnSource(GameData gameData, CountersOnSource count, AmountContext ctx) {
        if (ctx.sourcePermanent() != null) {
            return count.counterType() == CounterType.ANY
                    ? ctx.sourcePermanent().getTotalCounterCount()
                    : ctx.sourcePermanent().getCounterCount(count.counterType());
        }

        PlanarObject source = sourcePlanarObject(gameData, ctx);
        if (source == null) {
            return 0;
        }
        return count.counterType() == CounterType.ANY
                ? source.getCounters().values().stream().mapToInt(Integer::intValue).sum()
                : source.getCounters().getOrDefault(count.counterType(), 0);
    }

    private PlanarObject sourcePlanarObject(GameData gameData, AmountContext ctx) {
        if (ctx.stackEntry() == null || ctx.stackEntry().getSourcePlanarObject() == null) {
            return null;
        }

        PlanarObject snapshot = ctx.stackEntry().getSourcePlanarObject();
        if (gameData.planechase == null) {
            return snapshot;
        }
        return gameData.planechase.faceUp.stream()
                .filter(object -> object.getId().equals(snapshot.getId()))
                .findFirst()
                .orElse(snapshot);
    }

    private int countCountersOnTargetPermanent(GameData gameData, CountersOnTargetPermanent count,
                                               AmountContext ctx) {
        if (ctx.targetPermanentId() == null) {
            return 0;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        return target == null ? 0 : target.getCounterCount(count.counterType());
    }

    private int distinctCountersOnSource(AmountContext ctx) {
        Permanent source = ctx.sourcePermanent();
        if (source == null && ctx.stackEntry() != null) {
            source = ctx.stackEntry().getSourcePermanentSnapshot();
        }
        return source == null
                ? 0
                : (int) source.getCounters().values().stream().filter(count -> count > 0).count();
    }

    private int countCountersOnStackEntryCard(GameData gameData, CountersOnStackEntryCard amount,
                                              AmountContext ctx) {
        if (ctx.stackEntry() != null) {
            return ctx.stackEntry().getCounterCount(amount.counterType());
        }
        if (ctx.sourceCard() == null) {
            return 0;
        }
        return gameData.stack.stream()
                .filter(entry -> entry.getCard() != null)
                .filter(entry -> entry.getTargetableId().equals(ctx.sourceCard().getId()))
                .findFirst()
                .map(entry -> entry.getCounterCount(amount.counterType()))
                .orElse(0);
    }

    private int triggeringSpellColorCount(GameData gameData, AmountContext ctx) {
        Card triggeringSpell = findTriggeringSpell(gameData, ctx);
        return triggeringSpell == null
                ? 0
                : gameQueryService.getEffectiveCardColors(gameData, triggeringSpell).size();
    }

    private int triggeringSpellColorManaSymbols(GameData gameData, AmountContext ctx,
                                                TriggeringSpellColorManaSymbols amount) {
        Card triggeringSpell = findTriggeringSpell(gameData, ctx);
        ManaCost manaCost = triggeringSpell == null ? null : triggeringSpell.getParsedManaCost();
        return manaCost == null ? 0 : manaCost.countColorSymbols(amount.color());
    }

    private int triggeringSpellTargetCount(GameData gameData, AmountContext ctx,
                                           TriggeringSpellTargetCount amount) {
        if (gameData == null || ctx.stackEntry() == null || ctx.stackEntry().getTriggeringCardId() == null) {
            return 0;
        }
        UUID triggeringCardId = ctx.stackEntry().getTriggeringCardId();
        StackEntry spellEntry = gameData.stack.stream()
                .filter(entry -> entry.getCard() != null)
                .filter(entry -> triggeringCardId.equals(entry.getCard().getId()))
                .findFirst()
                .orElse(null);
        if (spellEntry == null) {
            return 0;
        }
        List<UUID> targetIds = new ArrayList<>(spellEntry.getDeclaredTargetIds());
        if (amount.filter() == null) {
            return (spellEntry.getTargetId() != null ? 1 : 0)
                    + targetIds.size() + spellEntry.getTargetCardIds().size();
        }
        if (spellEntry.getTargetId() != null && spellEntry.getTargetZone() == null
                && (targetIds.isEmpty() || spellEntry.isPrimaryTargetStoredSeparately())) {
            targetIds.add(spellEntry.getTargetId());
        }
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceControllerId(ctx.controllerId());
        return (int) targetIds.stream()
                .map(targetId -> gameQueryService.findPermanentById(gameData, targetId))
                .filter(permanent -> permanent != null)
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, amount.filter(), filterContext))
                .count();
    }

    private Card findTriggeringSpell(GameData gameData, AmountContext ctx) {
        if (gameData == null || ctx.stackEntry() == null || ctx.stackEntry().getTriggeringCardId() == null) {
            return null;
        }
        UUID triggeringCardId = ctx.stackEntry().getTriggeringCardId();
        return gameData.stack.stream()
                .filter(entry -> entry.getCard() != null)
                .filter(entry -> triggeringCardId.equals(entry.getCard().getId()))
                .findFirst()
                .map(StackEntry::getCard)
                .orElseGet(() -> gameData.orderedPlayerIds.stream()
                        .flatMap(playerId -> gameData.getSpellsCastThisTurn(playerId).stream())
                        .filter(card -> triggeringCardId.equals(card.getId()))
                        .findFirst()
                        .orElse(null));
    }

    private int countColorManaSymbolsInHand(
            GameData gameData, ColorManaSymbolsInHand amount, AmountContext ctx) {
        List<Card> hand = gameData.playerHands.get(ctx.controllerId());
        if (hand == null) return 0;
        int total = 0;
        for (Card card : hand) {
            ManaCost cost = card.getParsedManaCost();
            if (cost != null) {
                total += cost.countColorSymbols(amount.color());
            }
        }
        return total;
    }

    private int countHandCards(GameData gameData, CardsInHand count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand != null) {
                total += hand.size();
            }
        }
        return total;
    }

    private int countPlayersWithCardsInHandAtMost(GameData gameData,
            PlayersWithCardsInHandAtMost count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (isPlayerInScope(gameData, playerId, count.scope(), ctx)
                    && gameData.playerHands.getOrDefault(playerId, List.of()).size() <= count.threshold()) {
                total++;
            }
        }
        return total;
    }

    private int countMatchingHandCards(GameData gameData, MatchingCardsInHand count, AmountContext ctx) {
        UUID sourceCardId = ctx.sourcePermanent() != null ? ctx.sourcePermanent().getCard().getId() : null;
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null) continue;
            for (Card card : hand) {
                if (predicateEvaluationService.matchesCardPredicate(card, count.predicate(), sourceCardId)) {
                    total++;
                }
            }
        }
        return total;
    }

    private int countLibraryCards(GameData gameData, CardsInLibrary count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck != null) {
                total += deck.size();
            }
        }
        return total;
    }

    private int countMatchingLibraryCards(GameData gameData, MatchingCardsInLibrary count, AmountContext ctx) {
        UUID sourceCardId = ctx.sourcePermanent() != null ? ctx.sourcePermanent().getCard().getId() : null;
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck == null) continue;
            for (Card card : deck) {
                if (predicateEvaluationService.matchesCardPredicate(card, count.predicate(), sourceCardId)) {
                    total++;
                }
            }
        }
        return total;
    }

    private boolean controlsAllNamed(GameData gameData, FixedIfControlsAllNamed amount, AmountContext ctx) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        for (String requiredName : amount.requiredNames()) {
            boolean found = battlefield.stream()
                    .anyMatch(permanent -> requiredName.equals(permanent.getCard().getName()));
            if (!found) return false;
        }
        return true;
    }

    private boolean targetMatches(GameData gameData, FixedIfTargetMatches amount, AmountContext ctx) {
        if (ctx.targetPermanentId() == null) return false;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetPermanentId());
        return target != null
                && predicateEvaluationService.matchesPermanentPredicate(gameData, target, amount.filter());
    }

    private boolean allTargetsControlledByController(GameData gameData,
                                                      FixedIfAllTargetsControlledByController amount,
                                                      AmountContext ctx) {
        if (ctx.targetIds() == null || ctx.targetIds().size() != 2) return false;
        for (UUID targetId : ctx.targetIds()) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)
                    || !ctx.controllerId().equals(gameQueryService.findPermanentController(gameData, targetId))) {
                return false;
            }
        }
        return true;
    }

    private boolean controlsMoreCreaturesThanEachOtherPlayer(GameData gameData, AmountContext ctx) {
        int controllerCreatures = countCreaturesControlledBy(gameData, ctx.controllerId());
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            if (countCreaturesControlledBy(gameData, playerId) >= controllerCreatures) {
                return false;
            }
        }
        return true;
    }

    private boolean targetPlayerControlsMoreLands(GameData gameData, AmountContext ctx) {
        UUID targetId = targetPlayerId(gameData, ctx);
        if (targetId == null || ctx.controllerId() == null) {
            return false;
        }
        return countLandsControlledBy(gameData, targetId) > countLandsControlledBy(gameData, ctx.controllerId());
    }

    private int countLandsControlledBy(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isLand(gameData, permanent)) {
                count++;
            }
        }
        return count;
    }

    private int countCreaturesControlledBy(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        int count = 0;
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    count += CreatureCountSupport.creatureCount(gameData, permanent, gameQueryService);
            }
        }
        return count;
    }

    private int totalToughnessOfControlledCreatures(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        int total = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)) {
                // Anthems count in both branches. Static evaluation goes through the
                // recursion-safe accessor, which reads the layered toughness except for a
                // creature whose own static bonus is currently being assembled — the one case
                // where the amount would recurse back into the number it is computing.
                total += GameQueryService.isStaticEvaluationActive()
                        ? gameQueryService.toughnessForStaticFilter(permanent)
                        : gameQueryService.getEffectiveToughness(gameData, permanent);
            }
        }
        return total;
    }

    private int totalPowerOfControlledCreatures(
            GameData gameData, TotalPowerOfControlledCreatures amount, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        FilterContext filterContext = null;
        if (amount.filter() != null) {
            filterContext = (GameQueryService.isStaticEvaluationActive()
                    ? FilterContext.empty()
                    : FilterContext.of(gameData))
                    .withSourceControllerId(ctx.controllerId())
                    .withSourceCardId(ctx.sourceCard() == null ? null : ctx.sourceCard().getId())
                    .withSourcePermanentSnapshot(ctx.sourcePermanent())
                    .withSourcePermanentId(ctx.sourcePermanent() == null
                            ? null : ctx.sourcePermanent().getId());
        }
        int total = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (amount.filter() == null
                    || predicateEvaluationService.matchesPermanentPredicate(
                    permanent, amount.filter(), filterContext))) {
                total += GameQueryService.isStaticEvaluationActive()
                        ? gameQueryService.powerForStaticFilter(permanent)
                        : gameQueryService.getEffectivePower(gameData, permanent);
            }
        }
        return total;
    }

    private int greatestPowerAmongControlled(
            GameData gameData, GreatestPowerAmongControlled amount, AmountContext ctx) {
        if (amount.declaredAttackersOnly()) {
            if (ctx.stackEntry() == null) return 0;
            java.util.OptionalInt greatest = ctx.stackEntry().getAttackingPermanentSnapshots().stream()
                    .filter(attacker -> amount.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                            attacker, amount.filter(), FilterContext.empty().withSourceControllerId(ctx.controllerId())))
                    .mapToInt(attacker -> {
                        Permanent current = gameQueryService.findPermanentById(gameData, attacker.getId());
                        return current == null ? attacker.getLastKnownPower()
                                : gameQueryService.getEffectivePower(gameData, current);
                    }).max();
            return greatest.isEmpty() ? 0 : amount.floorAtZero() ? Math.max(0, greatest.getAsInt()) : greatest.getAsInt();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        FilterContext filterContext = null;
        if (amount.filter() != null) {
            filterContext = (GameQueryService.isStaticEvaluationActive()
                    ? FilterContext.empty()
                    : FilterContext.of(gameData))
                    .withSourceControllerId(ctx.controllerId())
                    .withSourceCardId(ctx.sourceCard() == null ? null : ctx.sourceCard().getId())
                    .withSourcePermanentSnapshot(ctx.sourcePermanent())
                    .withSourcePermanentId(ctx.sourcePermanent() == null
                            ? null : ctx.sourcePermanent().getId());
        }
        int greatestPower = amount.floorAtZero() ? 0 : Integer.MIN_VALUE;
        boolean foundCreature = false;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)
                        || (amount.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        permanent, amount.filter(), filterContext))) {
                    continue;
                }
                foundCreature = true;
                int power = gameQueryService.getEffectivePower(gameData, permanent);
                if (power > greatestPower) {
                    greatestPower = power;
                }
            }
        }
        return foundCreature ? greatestPower : 0;
    }

    private int greatestToughnessAmongControlled(
            GameData gameData, GreatestToughnessAmongControlled amount, AmountContext ctx) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        FilterContext filterContext = null;
        if (amount.filter() != null) {
            filterContext = (GameQueryService.isStaticEvaluationActive()
                    ? FilterContext.empty()
                    : FilterContext.of(gameData))
                    .withSourceControllerId(ctx.controllerId())
                    .withSourceCardId(ctx.sourceCard() == null ? null : ctx.sourceCard().getId())
                    .withSourcePermanentSnapshot(ctx.sourcePermanent())
                    .withSourcePermanentId(ctx.sourcePermanent() == null
                            ? null : ctx.sourcePermanent().getId());
        }
        int greatestToughness = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)
                        || (amount.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        permanent, amount.filter(), filterContext))) {
                    continue;
                }
                int toughness = gameQueryService.getEffectiveToughness(gameData, permanent);
                if (toughness > greatestToughness) {
                    greatestToughness = toughness;
                }
            }
        }
        return greatestToughness;
    }

    private int greatestCreatureCountAmongPlayers(GameData gameData) {
        int greatest = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            int creatureCount = 0;
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatureCount += CreatureCountSupport.creatureCount(gameData, permanent, gameQueryService);
                }
            }
            greatest = Math.max(greatest, creatureCount);
        }
        return greatest;
    }

    private int greatestPermanentCountAmongOpponents(
            GameData gameData, GreatestPermanentCountAmongOpponents amount, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;

        boolean staticEvaluation = GameQueryService.isStaticEvaluationActive();
        FilterContext filterContext = (staticEvaluation
                ? FilterContext.empty()
                : FilterContext.of(gameData))
                .withSourceControllerId(ctx.controllerId())
                .withSourceCardId(ctx.sourceCard() == null ? null : ctx.sourceCard().getId())
                .withSourcePermanentSnapshot(ctx.sourcePermanent())
                .withSourcePermanentId(ctx.sourcePermanent() == null ? null : ctx.sourcePermanent().getId());

        int greatest = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            int count = 0;
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                boolean matches = staticEvaluation
                        ? predicateEvaluationService.matchesStaticFilter(
                        permanent, amount.filter(), filterContext)
                        : predicateEvaluationService.matchesPermanentPredicate(
                        permanent, amount.filter(), filterContext);
                if (matches) count++;
            }
            greatest = Math.max(greatest, count);
        }
        return greatest;
    }

    private int greatestCreatureTypeCountAmongControlled(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;

        Map<CardSubtype, Integer> counts = new EnumMap<>(CardSubtype.class);
        int greatest = 0;
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }

            if (gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING)) {
                for (CardSubtype subtype : CardSubtype.values()) {
                    if (gameQueryService.isCreatureSubtype(subtype)) {
                        greatest = Math.max(greatest, counts.merge(subtype, 1, Integer::sum));
                    }
                }
            } else {
                for (CardSubtype subtype : gameQueryService.effectiveCreatureSubtypes(gameData, permanent)) {
                    greatest = Math.max(greatest, counts.merge(subtype, 1, Integer::sum));
                }
            }
        }
        return greatest;
    }

    private int partySize(GameData gameData, AmountContext ctx) {
        return partySize(gameData, ctx.controllerId(), gameQueryService);
    }

    static int partySize(GameData gameData, UUID controllerId, GameQueryService gameQueryService) {
        if (controllerId == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;

        List<Set<CardSubtype>> creatureTypes = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            // Party can define power in layer 7. Read the already applied type and
            // ability layers instead of recursively assembling that power again.
            var state = LayerSystemService.activeStateFor(permanent.getId());
            if (!(state != null ? state.getCardTypes().contains(CardType.CREATURE)
                    : gameQueryService.isCreature(gameData, permanent))) {
                continue;
            }
            Set<CardSubtype> types = new HashSet<>(state != null ? state.getSubtypes()
                    : gameQueryService.effectiveCreatureSubtypes(gameData, permanent));
            if (state != null ? state.getKeywords().contains(Keyword.CHANGELING)
                    : gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING)) {
                types.addAll(PARTY_ROLES);
            }
            creatureTypes.add(types);
        }
        return maximumPartySize(creatureTypes, 0, new HashSet<>());
    }

    private static int maximumPartySize(List<Set<CardSubtype>> creatureTypes, int roleIndex,
                                        Set<Integer> usedCreatureIndices) {
        if (roleIndex == PARTY_ROLES.size()) {
            return 0;
        }

        int maximum = maximumPartySize(creatureTypes, roleIndex + 1, usedCreatureIndices);
        CardSubtype role = PARTY_ROLES.get(roleIndex);
        for (int creatureIndex = 0; creatureIndex < creatureTypes.size(); creatureIndex++) {
            if (usedCreatureIndices.contains(creatureIndex)
                    || !creatureTypes.get(creatureIndex).contains(role)) {
                continue;
            }
            usedCreatureIndices.add(creatureIndex);
            maximum = Math.max(maximum,
                    1 + maximumPartySize(creatureTypes, roleIndex + 1, usedCreatureIndices));
            usedCreatureIndices.remove(creatureIndex);
        }
        return maximum;
    }

    private int greatestManaValueAmongControlled(
            GameData gameData, GreatestManaValueAmongControlled amount, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        FilterContext filterContext = (GameQueryService.isStaticEvaluationActive()
                ? FilterContext.empty()
                : FilterContext.of(gameData))
                .withSourceControllerId(ctx.controllerId());
        int greatest = 0;
        for (Permanent permanent : battlefield) {
            if (amount.excludeSource() && ctx.sourcePermanent() != null
                    && permanent.getId().equals(ctx.sourcePermanent().getId())) {
                continue;
            }
            if (!predicateEvaluationService.matchesPermanentPredicate(
                    permanent, amount.filter(), filterContext)) {
                continue;
            }
            greatest = Math.max(greatest, permanent.isFaceDown() ? 0 : permanent.getCard().getManaValue());
        }
        return greatest;
    }

    private int greatestManaValueAmongAttachedEquipment(GameData gameData, AmountContext ctx) {
        if (ctx.sourcePermanent() == null) return 0;

        UUID sourcePermanentId = ctx.sourcePermanent().getId();
        int greatest = 0;
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (!sourcePermanentId.equals(permanent.getAttachedTo())
                        || permanent.isFaceDown()
                        || !gameQueryService.hasEffectiveSubtype(gameData, permanent, CardSubtype.EQUIPMENT)) {
                    continue;
                }
                greatest = Math.max(greatest, permanent.getCard().getManaValue());
            }
        }
        return greatest;
    }

    private int greatestManaValueAmongOwnedCommanders(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;

        Set<UUID> commanderIds = gameData.playerCommanders
                .getOrDefault(ctx.controllerId(), List.of())
                .stream()
                .map(Card::getId)
                .collect(Collectors.toSet());
        if (commanderIds.isEmpty()) return 0;

        int greatest = 0;
        for (Card card : gameData.playerCommandZones.values().stream()
                .flatMap(List::stream)
                .filter(card -> commanderIds.contains(card.getId()))
                .toList()) {
            greatest = Math.max(greatest, card.getManaValue());
        }
        for (Map<UUID, List<Card>> zone : List.of(gameData.playerHands, gameData.playerDecks,
                gameData.playerGraveyards)) {
            for (List<Card> cards : zone.values()) {
                for (Card card : cards) {
                    if (commanderIds.contains(card.getId())) {
                        greatest = Math.max(greatest, card.getManaValue());
                    }
                }
            }
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Card card : gameData.getPlayerExiledCards(playerId)) {
                if (commanderIds.contains(card.getId())) {
                    greatest = Math.max(greatest, card.getManaValue());
                }
            }
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (commanderIds.contains(permanent.getOriginalCard().getId())) {
                    greatest = Math.max(greatest,
                            permanent.isFaceDown() ? 0 : permanent.getCard().getManaValue());
                }
            }
        }
        return greatest;
    }

    private int countAttachmentsOnSource(GameData gameData, AttachmentsOnSource amount, AmountContext ctx) {
        if (ctx.sourcePermanent() == null) return 0;
        UUID sourceId = ctx.sourcePermanent().getId();
        if (gameQueryService.findPermanentById(gameData, sourceId) == null
                && ctx.sourcePermanent().getLastKnownAuraCount() != null) {
            return (amount.countAuras() ? ctx.sourcePermanent().getLastKnownAuraCount() : 0)
                    + (amount.countEquipment() ? ctx.sourcePermanent().getLastKnownEquipmentCount() : 0);
        }
        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttached() && permanent.getAttachedTo().equals(sourceId)) {
                boolean isAura = permanent.getCard().getSubtypes().contains(CardSubtype.AURA);
                boolean isEquipment = permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT);
                if ((amount.countAuras() && isAura) || (amount.countEquipment() && isEquipment)) {
                    count[0]++;
                }
            }
        });
        return count[0];
    }

    private int countCreaturesBlockingSource(GameData gameData, AmountContext ctx, boolean useTarget) {
        UUID watchedId = useTarget ? ctx.targetPermanentId()
                : ctx.sourcePermanent() == null ? null : ctx.sourcePermanent().getId();
        if (watchedId == null) return 0;
        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isBlocking() && permanent.getBlockingTargetIds().contains(watchedId)) {
                count[0]++;
            }
        });
        return count[0];
    }

    private int countCreaturesBlockedBySource(AmountContext ctx) {
        Permanent source = ctx.sourcePermanent();
        return source == null ? 0 : source.getBlockingTargets().size();
    }

    private int countCreatureDeathsThisTurn(GameData gameData, CreatureDeathsThisTurn count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.creatureDeathCountThisTurn.getOrDefault(playerId, 0);
        }
        return total;
    }

    private int countCreaturesAttackedThisTurn(GameData gameData,
                                                CreaturesAttackedThisTurn count,
                                                AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.creaturesAttackedCountThisTurn.getOrDefault(playerId, 0);
        }
        return total;
    }

    private int countNontokenCreatureDeathsThisTurn(GameData gameData,
                                                     NontokenCreatureDeathsThisTurn count,
                                                     AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.nontokenCreatureDeathCountThisTurn.getOrDefault(playerId, 0);
        }
        return total;
    }

    private int countCreatureSubtypeDeathsThisTurn(GameData gameData,
                                                    CreatureSubtypeDeathsThisTurn count,
                                                    AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.creatureSubtypeDeathCountThisTurn
                    .getOrDefault(playerId, Map.of())
                    .getOrDefault(count.subtype(), 0);
        }
        return total;
    }

    private int countCreaturesExiledThisTurn(GameData gameData,
                                             CreaturesExiledThisTurn count,
                                             AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.creatureExileCountThisTurn.getOrDefault(playerId, 0);
        }
        return total;
    }

    private int countColorsAmongControlledPermanents(
            GameData gameData, ColorsAmongControlledPermanents count, AmountContext ctx) {
        return colorsAmongControlledPermanents(gameData, count, ctx).size();
    }

    private int countColorsAmongControlledPermanentsAndSpellsCastThisTurn(
            GameData gameData, AmountContext ctx) {
        Set<CardColor> colors = colorsAmongControlledPermanents(
                gameData, new ColorsAmongControlledPermanents(), ctx);
        if (gameData == null || ctx.controllerId() == null) return colors.size();
        for (Card spell : gameData.getSpellsCastThisTurn(ctx.controllerId())) {
            colors.addAll(gameQueryService.getEffectiveCardColors(gameData, spell));
        }
        return colors.size();
    }

    private Set<CardColor> colorsAmongControlledPermanents(
            GameData gameData, ColorsAmongControlledPermanents count, AmountContext ctx) {
        if (gameData == null || ctx.controllerId() == null) return EnumSet.noneOf(CardColor.class);
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return EnumSet.noneOf(CardColor.class);

        boolean staticEvaluation = GameQueryService.isStaticEvaluationActive();
        boolean filterNeedsBoard = staticEvaluation
                && count.filter() != null
                && predicateEvaluationService.requiresGameDataForStaticFilter(count.filter());
        FilterContext filterContext = staticEvaluation && !filterNeedsBoard
                ? FilterContext.empty()
                : FilterContext.of(gameData);
        filterContext = filterContext.withSourceControllerId(ctx.controllerId());
        if (ctx.sourcePermanent() != null) {
            filterContext = filterContext
                    .withSourceCardId(ctx.sourcePermanent().getCard().getId())
                    .withSourcePermanentSnapshot(ctx.sourcePermanent());
        }

        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        for (Permanent permanent : battlefield) {
            if (count.filter() != null) {
                boolean matches = filterNeedsBoard
                        ? predicateEvaluationService.matchesStaticFilter(permanent, count.filter(), filterContext)
                        : predicateEvaluationService.matchesPermanentPredicate(permanent, count.filter(), filterContext);
                if (!matches) continue;
            }
            colors.addAll(staticEvaluation
                    ? gameQueryService.colorsForStaticEvaluation(permanent)
                    : gameQueryService.getEffectiveColors(gameData, permanent));
        }
        return colors;
    }

    private int countCreaturesEnteredBattlefieldThisTurn(
            GameData gameData, CreaturesEnteredBattlefieldThisTurn count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.permanentsEnteredBattlefieldThisTurn
                    .getOrDefault(playerId, List.of())
                    .stream()
                    .filter(card -> card.hasType(CardType.CREATURE))
                    .mapToInt(ignored -> 1)
                    .sum();
        }
        return total;
    }

    private int countCreaturesLeftBattlefieldThisTurn(
            GameData gameData, CreaturesLeftBattlefieldThisTurn count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.creatureLeftBattlefieldCountThisTurn.getOrDefault(playerId, 0);
        }
        return total;
    }

    private int countPermanentsEnteredBattlefieldThisTurn(
            GameData gameData, PermanentsEnteredBattlefieldThisTurn count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += (int) gameData.permanentsEnteredBattlefieldThisTurn
                    .getOrDefault(playerId, List.of())
                    .stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(card, count.filter(), null))
                    .count();
        }
        return total;
    }

    private int countPermanentsSacrificedThisTurn(
            GameData gameData, PermanentsSacrificedThisTurn count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            if (count.filter() == null && !count.excludeCurrentCastSacrifices()) {
                total += gameData.sacrificedPermanentCountThisTurn.getOrDefault(playerId, 0);
            } else {
                total += (int) gameData.permanentsSacrificedThisTurn
                        .getOrDefault(playerId, List.of())
                        .stream()
                        .filter(card -> !count.excludeCurrentCastSacrifices()
                                || !gameData.currentCastSacrificedPermanentIds.contains(card.getId()))
                        .filter(card -> count.filter() == null
                                || predicateEvaluationService.matchesCardPredicate(card, count.filter(), null))
                        .count();
            }
        }
        return total;
    }

    private int countLifeLostThisTurn(GameData gameData, LifeLostThisTurn count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.lifeLostThisTurn.getOrDefault(playerId, 0);
        }
        return total;
    }

    private int countSpellsCastThisTurn(GameData gameData, SpellsCastThisTurn count, AmountContext ctx) {
        int total = 0;
        UUID sourceCardId = ctx.sourcePermanent() != null ? ctx.sourcePermanent().getCard().getId()
                : ctx.stackEntry() != null ? ctx.stackEntry().getCard().getId() : null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += (int) gameData.getSpellsCastThisTurn(playerId).stream()
                    .filter(spell -> count.filter() == null
                            || predicateEvaluationService.matchesCardPredicate(
                            spell, count.filter(), sourceCardId, gameData, playerId))
                    .count();
        }
        return total;
    }

    private int countSpellsCastSinceBeginningOfLastTurn(
            GameData gameData, SpellsCastSinceBeginningOfLastTurn count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += (int) gameData.getSpellsCastLastTurn(playerId).stream()
                    .filter(spell -> count.filter() == null
                            || predicateEvaluationService.matchesCardPredicate(
                            spell, count.filter(), null, gameData, playerId))
                    .count();
            total += (int) gameData.getSpellsCastThisTurn(playerId).stream()
                    .filter(spell -> count.filter() == null
                            || predicateEvaluationService.matchesCardPredicate(
                            spell, count.filter(), null, gameData, playerId))
                    .count();
        }
        return total;
    }

    private int countSpellsCastFromOutsideHandThisTurn(GameData gameData,
                                                       SpellsCastFromOutsideHandThisTurn count,
                                                       AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (isPlayerInScope(gameData, playerId, count.scope(), ctx)) {
                total += gameData.getSpellsCastFromOutsideHandThisTurnCount(playerId);
            }
        }
        return total;
    }

    private int totalManaValueOfOtherSpellsCastThisTurn(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) {
            return 0;
        }

        int total = gameData.getSpellsCastThisTurn(ctx.controllerId()).stream()
                .mapToInt(gameData::getSpellCastStackManaValue)
                .sum();
        StackEntry resolvingEntry = ctx.stackEntry();
        if (resolvingEntry != null && resolvingEntry.getCard() != null) {
            UUID resolvingCardId = resolvingEntry.getCard().getId();
            boolean resolvingSpellWasCast = gameData.getSpellsCastThisTurn(ctx.controllerId()).stream()
                    .anyMatch(spell -> resolvingCardId.equals(spell.getId()));
            if (resolvingSpellWasCast) {
                total -= resolvingEntry.getCard().getManaValue();
            }
        }
        return Math.max(0, total);
    }

    private int countCreaturesThatCrewedSourceThisTurn(GameData gameData, AmountContext ctx) {
        if (gameData == null || ctx.sourcePermanent() == null) {
            return 0;
        }
        return gameData.creaturesThatCrewedPermanentThisTurn
                .getOrDefault(ctx.sourcePermanent().getId(), java.util.Set.of())
                .size();
    }

    private int countLifeGainedThisTurn(GameData gameData, LifeGainedThisTurn count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.getLifeGainedThisTurn(playerId);
        }
        return total;
    }

    private int countCardsDrawnThisTurn(GameData gameData, CardsDrawnThisTurn count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0);
        }
        return total;
    }

    private int opponentsControllingReturnedPermanents(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null || ctx.stackEntry() == null) return 0;
        Set<UUID> returnedPermanentIds = ctx.stackEntry().getReturnedPermanentIds();
        Map<UUID, UUID> removedPermanentControllers = ctx.stackEntry().getRemovedPermanentControllers();
        if (returnedPermanentIds.isEmpty() && removedPermanentControllers.isEmpty()) return 0;
        int qualifyingOpponents = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(ctx.controllerId())
                    && (gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                            .anyMatch(permanent -> returnedPermanentIds.contains(permanent.getId()))
                    || removedPermanentControllers.containsValue(playerId))) {
                qualifyingOpponents++;
            }
        }
        return qualifyingOpponents;
    }

    private int opponentsWithAtLeastCardsDrawnThisTurn(
            GameData gameData, OpponentsWithAtLeastCardsDrawnThisTurn count, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int qualifyingOpponents = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(ctx.controllerId())
                    && gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0) >= count.minimum()) {
                qualifyingOpponents++;
            }
        }
        return qualifyingOpponents;
    }

    private int opponentsWithAtLeastLandsEnteredBattlefieldThisTurn(
            GameData gameData, OpponentsWithAtLeastLandsEnteredBattlefieldThisTurn count,
            AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int qualifyingOpponents = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            long landsEntered = gameData.permanentsEnteredBattlefieldThisTurn
                    .getOrDefault(playerId, List.of()).stream()
                    .filter(card -> card.hasType(CardType.LAND))
                    .count();
            if (landsEntered >= count.minimum()) {
                qualifyingOpponents++;
            }
        }
        return qualifyingOpponents;
    }
    private int greatestManaValueAmongSpellsCastThisTurn(
            GameData gameData, GreatestManaValueAmongSpellsCastThisTurn amount, AmountContext ctx) {
        int greatestManaValue = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            for (Card spell : gameData.getSpellsCastThisTurn(playerId)) {
                if (predicateEvaluationService.matchesCardPredicate(
                        spell, amount.filter(), spell.getId(), gameData, playerId)) {
                    greatestManaValue = Math.max(greatestManaValue, spell.getManaValue());
                }
            }
        }
        return greatestManaValue;
    }

    private int countCommanderCastsFromCommandZoneThisGame(GameData gameData,
                                                            CommanderCastsFromCommandZoneThisGame count,
                                                            AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, count.scope(), ctx)) continue;
            total += gameData.commanderCastsFromCommandZoneThisGame.getOrDefault(playerId, 0);
        }
        return total;
    }

    /**
     * Highest life total among the controller's opponents (Malignus). Returns 0 while the controller
     * is unknown — that happens transiently for a CDA evaluated while the source is still entering
     * the battlefield.
     */
    private int highestOpponentLifeTotal(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int highest = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            highest = Math.max(highest, gameData.playerLifeTotals.getOrDefault(playerId, 0));
        }
        return highest;
    }

    private int greatestOpponentHandSize(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int greatest = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            greatest = Math.max(greatest, gameData.playerHands.getOrDefault(playerId, List.of()).size());
        }
        return greatest;
    }

    private int greatestOpponentCardsDrawnThisTurn(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int greatest = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            greatest = Math.max(greatest, gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0));
        }
        return greatest;
    }

    private int targetGroupCount(TargetGroupCount amount, AmountContext ctx) {
        return ctx.stackEntry() == null
                ? 0
                : ctx.stackEntry().targetsForGroup(amount.groupIndex()).size();
    }

    private int totalPowerOfTargetGroup(GameData gameData, TotalPowerOfTargetGroup amount,
                                        AmountContext ctx) {
        if (ctx.stackEntry() == null) {
            return 0;
        }
        int total = 0;
        for (UUID targetId : ctx.stackEntry().targetsForGroup(amount.groupIndex())) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null && gameQueryService.isCreature(gameData, target)) {
                total += gameQueryService.getEffectivePower(gameData, target);
            }
        }
        return total;
    }

    private int opponentsWithMoreCardsInHandThanController(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int controllerHandSize = gameData.playerHands.getOrDefault(ctx.controllerId(), List.of()).size();
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            if (gameData.playerHands.getOrDefault(playerId, List.of()).size() > controllerHandSize) {
                count++;
            }
        }
        return count;
    }

    private int opponentsWithAtLeastTwoMoreLandsThanController(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int controllerLandCount = countLandsControlledBy(gameData, ctx.controllerId());
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(ctx.controllerId())
                    && countLandsControlledBy(gameData, playerId) >= controllerLandCount + 2) {
                count++;
            }
        }
        return count;
    }

    private int opponentsWithLifeAtMost(GameData gameData, OpponentsWithLifeAtMost amount,
                                        AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(ctx.controllerId())
                    && gameData.getLife(playerId) <= (amount.belowHalfStartingLife()
                    ? (gameData.startingLife() - 1) / 2 : amount.threshold())) {
                count++;
            }
        }
        return count;
    }

    private int opponentsWithLessLifeThanController(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int controllerLife = gameData.getLife(ctx.controllerId());
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(ctx.controllerId()) && gameData.getLife(playerId) < controllerLife) {
                count++;
            }
        }
        return count;
    }

    private int opponentsWithMoreLandsThanController(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int controllerLandCount = countLandsControlledBy(gameData, ctx.controllerId());
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(ctx.controllerId())
                    && countLandsControlledBy(gameData, playerId) > controllerLandCount) {
                count++;
            }
        }
        return count;
    }

    private int opponentsWithCreaturePowerAtLeast(GameData gameData,
                                                   OpponentsWithCreaturePowerAtLeast amount,
                                                   AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            boolean qualifies = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                    .anyMatch(permanent -> gameQueryService.isCreature(gameData, permanent)
                            && gameQueryService.getEffectivePower(gameData, permanent) >= amount.minPower());
            if (qualifies) count++;
        }
        return count;
    }

    private int opponentsWithMoreCreaturesThanController(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int controllerCreatureCount = countCreaturesControlledBy(gameData, ctx.controllerId());
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(ctx.controllerId())
                    && countCreaturesControlledBy(gameData, playerId) > controllerCreatureCount) {
                count++;
            }
        }
        return count;
    }

    private int opponentsWithFewerCreaturesThanController(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int controllerCreatureCount = countCreaturesControlledBy(gameData, ctx.controllerId());
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(ctx.controllerId())
                    && countCreaturesControlledBy(gameData, playerId) < controllerCreatureCount) {
                count++;
            }
        }
        return count;
    }

    private int opponentsWhoLostLifeThisTurn(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(ctx.controllerId())
                    && gameData.lifeLostThisTurn.getOrDefault(playerId, 0) > 0) {
                count++;
            }
        }
        return count;
    }

    private int opponentsDealtCombatDamageThisTurn(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        Set<UUID> damagedPlayers = new HashSet<>();
        gameData.combatDamageToPlayersThisTurn.values().forEach(damagedPlayers::addAll);
        return (int) damagedPlayers.stream()
                .filter(gameData.orderedPlayerIds::contains)
                .filter(playerId -> !playerId.equals(ctx.controllerId()))
                .count();
    }

    private int opponentsDealtCombatDamageBySourceNameOrSubtypeThisTurn(
            GameData gameData, OpponentsDealtCombatDamageBySourceNameOrSubtypeThisTurn amount,
            AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        Set<UUID> damagedPlayers = new HashSet<>();
        gameData.combatDamageToPlayersThisTurn.forEach((sourceId, playerIds) -> {
            boolean matchesName = amount.sourceName() != null
                    && amount.sourceName().equals(gameData.combatDamageSourceNamesThisTurn.get(sourceId));
            boolean matchesSubtype = amount.sourceSubtype() != null
                    && gameData.combatDamageSourceSubtypesThisTurn
                            .getOrDefault(sourceId, Set.of()).contains(amount.sourceSubtype());
            if (matchesName || matchesSubtype) {
                damagedPlayers.addAll(playerIds);
            }
        });
        return (int) damagedPlayers.stream()
                .filter(gameData.orderedPlayerIds::contains)
                .filter(playerId -> !playerId.equals(ctx.controllerId()))
                .count();
    }

    private int opponentsAttackedThisTurn(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int count = 0;
        for (Map.Entry<UUID, Set<UUID>> entry : gameData.playersWhoAttackedPlayersThisTurn.entrySet()) {
            if (gameData.orderedPlayerIds.contains(entry.getKey())
                    && !entry.getKey().equals(ctx.controllerId())
                    && entry.getValue().contains(ctx.controllerId())) {
                count++;
            }
        }
        return count;
    }

    private int countOpponentPoisonCounters(GameData gameData, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(ctx.controllerId())) {
                total += gameData.playerPoisonCounters.getOrDefault(playerId, 0);
            }
        }
        return total;
    }

    private int opponentsWithAtLeastPoisonCounters(
            GameData gameData, OpponentsWithAtLeastPoisonCounters count, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int qualifyingOpponents = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(ctx.controllerId())
                    && gameData.playerPoisonCounters.getOrDefault(playerId, 0) >= count.minimum()) {
                qualifyingOpponents++;
            }
        }
        return qualifyingOpponents;
    }

    private int countDefendingPlayerPoisonCounters(GameData gameData, AmountContext ctx) {
        UUID defendingPlayerId = defendingPlayerId(gameData, ctx);
        return defendingPlayerId == null
                ? 0
                : gameData.playerPoisonCounters.getOrDefault(defendingPlayerId, 0);
    }

    /**
     * Number of creatures the source devoured (CR 702.82) that had the given subtype. A devoured
     * Changeling counts as every creature type, so it matches any subtype. Reads the intrinsic card
     * subtypes/keywords snapshotted at devour time (see {@code Permanent.recordDevouredCreature}).
     */
    private int countDevouredCreaturesOfSubtype(AmountContext ctx, CardSubtype subtype) {
        if (ctx.sourcePermanent() == null) return 0;
        return (int) ctx.sourcePermanent().getDevouredCreatures().stream()
                .filter(card -> card.getSubtypes().contains(subtype)
                        || card.hasKeyword(Keyword.CHANGELING))
                .count();
    }

    private int countLandsMatchingImprintedName(GameData gameData, AmountContext ctx) {
        if (ctx.sourcePermanent() == null) return 0;
        Card imprinted = gameData.getImprintedCard(ctx.sourcePermanent().getCard());
        if (imprinted == null) return 0;
        String imprintedName = imprinted.getName();
        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().hasType(CardType.LAND)
                    && imprintedName.equals(permanent.getCard().getName())) {
                count[0]++;
            }
        });
        return count[0];
    }

    private int totalPTOfCardsExiledWithSource(GameData gameData, AmountContext ctx, boolean power) {
        if (ctx.sourcePermanent() == null) return 0;
        int total = 0;
        for (Card exiled : gameData.getCardsExiledByPermanent(ctx.sourcePermanent().getId())) {
            Integer value = power ? gameQueryService.getEffectiveCardPower(gameData, exiled)
                    : gameQueryService.getEffectiveCardToughness(gameData, exiled);
            if (value != null) {
                total += value;
            }
        }
        return total;
    }

    private int totalManaValueOfCardsExiledWithSource(GameData gameData, AmountContext ctx) {
        if (ctx.sourcePermanent() == null) return 0;
        return gameData.getCardsExiledByPermanent(ctx.sourcePermanent().getId()).stream()
                .mapToInt(Card::getManaValue)
                .sum();
    }

    private int totalManaValueOfCardsOwnedInExile(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        return gameData.exiledCards.stream()
                .filter(entry -> ctx.controllerId().equals(entry.ownerId()))
                .filter(entry -> !entry.faceDown() && !entry.card().isToken())
                .mapToInt(entry -> entry.card().getManaValue())
                .sum();
    }

    private int totalManaValueOfCardsInGraveyard(GameData gameData,
                                                  TotalManaValueOfCardsInGraveyard amount,
                                                  AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!isPlayerInScope(gameData, playerId, amount.scope(), ctx)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (!card.isToken() && predicateEvaluationService.matchesCardPredicate(
                        card, amount.filter(), null, gameData, playerId)) {
                    total += card.getManaValue();
                }
            }
        }
        return total;
    }

    private int colorsAmongCardsExiledWithSource(GameData gameData, AmountContext ctx) {
        if (ctx.sourcePermanent() == null) return 0;
        return (int) gameData.getCardsExiledByPermanent(ctx.sourcePermanent().getId()).stream()
                .flatMap(card -> card.getColors().stream())
                .distinct()
                .count();
    }

    private int countCreatureCardsExiledWithSource(GameData gameData, AmountContext ctx) {
        if (ctx.sourcePermanent() == null) return 0;
        return (int) gameData.getCardsExiledByPermanent(ctx.sourcePermanent().getId()).stream()
                .filter(card -> !card.isToken())
                .filter(card -> card.hasType(CardType.CREATURE))
                .count();
    }

    private int countCardsExiledWithSource(GameData gameData, CardsExiledWithSource count,
                                           AmountContext ctx) {
        UUID sourceId = ctx.sourcePermanent() != null
                ? ctx.sourcePermanent().getId()
                : ctx.sourceCard() == null ? null : ctx.sourceCard().getId();
        if (sourceId == null) return 0;

        List<Card> exiledCards = gameData.getCardsExiledByPermanent(sourceId);
        // Delve cards are tracked by the physical card ID until the creature spell resolves into
        // a permanent. Entry replacement effects evaluate before that link is transferred.
        if (exiledCards.isEmpty() && ctx.sourcePermanent() != null && ctx.sourceCard() != null
                && !ctx.sourcePermanent().getId().equals(ctx.sourceCard().getId())) {
            exiledCards = gameData.getCardsExiledByPermanent(ctx.sourceCard().getId());
        }
        if (count.filter() == null) return exiledCards.size();
        return (int) exiledCards.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, count.filter(), null, gameData, ctx.controllerId()))
                .count();
    }

    private int greatestManaValueAmongCardsExiledWithSource(GameData gameData, AmountContext ctx) {
        if (ctx.sourcePermanent() == null) return 0;
        return gameData.getCardsExiledByPermanent(ctx.sourcePermanent().getId()).stream()
                .mapToInt(Card::getManaValue)
                .max()
                .orElse(0);
    }

    private int imprintedCardManaValue(GameData gameData, AmountContext ctx) {
        if (ctx.sourcePermanent() == null) return 0;
        Card imprinted = ctx.stackEntry() != null && ctx.stackEntry().getExiledCostCardSnapshot() != null
                ? ctx.stackEntry().getExiledCostCardSnapshot()
                : gameData.getImprintedCard(ctx.sourcePermanent().getCard());
        return imprinted == null ? 0 : imprinted.getManaValue();
    }

    private int imprintedCreaturePT(GameData gameData, AmountContext ctx, boolean power) {
        if (ctx.sourcePermanent() == null) return 0;
        Card imprinted = gameData.getImprintedCard(ctx.sourcePermanent().getCard());
        if (imprinted == null
                || gameData.findExiledCard(imprinted.getId()) == null
                || !imprinted.hasType(CardType.CREATURE)
                || imprinted.getPower() == null
                || imprinted.getToughness() == null) {
            return 0;
        }
        return power ? imprinted.getPower() : imprinted.getToughness();
    }

    private int damageDealtToOpponentsThisTurn(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            total += gameData.damageDealtToPlayersThisTurn.getOrDefault(playerId, 0);
        }
        return total;
    }

    private int countPlayersWithCardsInHandAtLeast(GameData gameData,
            PlayersWithCardsInHandAtLeast count, AmountContext ctx) {
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (isPlayerInScope(gameData, playerId, count.scope(), ctx)
                    && gameData.playerHands.getOrDefault(playerId, List.of()).size() >= count.threshold()) {
                total++;
            }
        }
        return total;
    }

    private int greatestDamageDealtBySourceThisTurn(GameData gameData) {
        int greatestToPermanent = gameData.damageDealtToPermanentsBySourceThisTurn.values().stream()
                .flatMap(sourceDamage -> sourceDamage.values().stream())
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        int greatestToPlayer = gameData.damageDealtToPlayersBySourceThisTurn.values().stream()
                .flatMap(sourceDamage -> sourceDamage.values().stream())
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        return Math.max(greatestToPermanent, greatestToPlayer);
    }

    private int damageDealtToSourcePermanentBySourceNameThisTurn(GameData gameData, AmountContext ctx,
                                                                  String sourceName) {
        Permanent source = ctx.sourcePermanent();
        if (source == null || sourceName == null) return 0;
        return gameData.damageDealtToPermanentsBySourceThisTurn
                .getOrDefault(source.getId(), Map.of())
                .entrySet().stream()
                .filter(entry -> !source.getId().equals(entry.getKey()))
                .filter(entry -> sourceName.equals(gameData.damageSourceNamesThisTurn.get(entry.getKey())))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    private int damageDealtByTargetPlayerSorceryThisTurn(GameData gameData, AmountContext ctx) {
        UUID targetPlayerId = ctx.targetPermanentId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return 0;
        }
        UUID sorceryId = ctx.chosenPermanentId();
        return sorceryId == null ? 0 : gameData.sorcerySpellDamageDealtThisTurn
                .getOrDefault(sorceryId, 0);
    }

    private int noncombatDamageDealtToOpponentsThisTurn(GameData gameData, AmountContext ctx) {
        if (ctx.controllerId() == null) return 0;
        int total = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            total += gameData.noncombatDamageDealtToPlayersThisTurn.getOrDefault(playerId, 0);
        }
        return total;
    }

    private boolean isPlayerInScope(GameData gameData, UUID playerId, CountScope scope, AmountContext ctx) {
        return switch (scope) {
            case CONTROLLER -> playerId.equals(ctx.controllerId());
            case OPPONENTS -> !playerId.equals(ctx.controllerId());
            case ANY_PLAYER -> true;
            case CHOSEN_PLAYER -> ctx.sourcePermanent() != null
                    && playerId.equals(ctx.sourcePermanent().getRememberedTargetPlayerId());
            // The target channel carries the target player's id for player-targeting effects.
            case TARGET_PLAYER -> playerId.equals(targetPlayerId(gameData, ctx));
            case DEFENDING_PLAYER -> playerId.equals(defendingPlayerId(gameData, ctx));
            case ATTACHED_CONTROLLER -> playerId.equals(attachedControllerId(gameData, ctx));
        };
    }

    /**
     * The controller of the permanent the source Aura/Equipment is attached to, or {@code null}
     * when the source is missing or unattached. See {@link CountScope#ATTACHED_CONTROLLER}.
     */
    private UUID attachedControllerId(GameData gameData, AmountContext ctx) {
        Permanent source = ctx.sourcePermanent();
        if (source == null || !source.isAttached()) {
            return null;
        }
        return gameQueryService.findPermanentController(gameData, source.getAttachedTo());
    }

    /**
     * The player named by the target channel: the targeted player itself, or — when the target is a
     * permanent (a targeted planeswalker) — that permanent's controller. Models the
     * "that player or that planeswalker's controller" wordings (Goblin Lyre) with the same scope
     * that plain "target player" effects use.
     */
    private UUID targetPlayerId(GameData gameData, AmountContext ctx) {
        UUID targetId = ctx.targetPermanentId();
        if (targetId == null || gameData.playerIds.contains(targetId)) {
            return targetId;
        }
        return gameQueryService.findPermanentController(gameData, targetId);
    }

    /**
     * The player the source permanent is attacking (its attack target when that is a player,
     * otherwise the controller of the attacked planeswalker), or {@code null} when the source is
     * not attacking or has no attack target. See {@link CountScope#DEFENDING_PLAYER}.
     */
    private UUID defendingPlayerId(GameData gameData, AmountContext ctx) {
        if (ctx.stackEntry() != null && ctx.stackEntry().getDefendingPlayerId() != null) {
            return ctx.stackEntry().getDefendingPlayerId();
        }
        if (ctx.stackEntry() != null && ctx.stackEntry().getAttackedTargetId() != null) {
            UUID attackedTarget = ctx.stackEntry().getAttackedTargetId();
            return gameData.playerIds.contains(attackedTarget) ? attackedTarget
                    : gameQueryService.findPermanentController(gameData, attackedTarget);
        }
        Permanent source = ctx.sourcePermanent();
        if (source != null) {
            if (!source.isAttacking() || source.getAttackTarget() == null) {
                return null;
            }
            UUID attackTarget = source.getAttackTarget();
            if (gameData.orderedPlayerIds.contains(attackTarget)) {
                return attackTarget;
            }
            // Attacking a planeswalker: the defending player is that planeswalker's controller.
            return gameQueryService.findPermanentController(gameData, attackTarget);
        }

        // A resolving spell has no source permanent. In the two-player combat model, the
        // defending player is the opponent of the active (attacking) player.
        if (gameData.currentStep == null || !gameData.currentStep.isCombatPhase()
                || gameData.activePlayerId == null || gameData.orderedPlayerIds.size() != 2) {
            return null;
        }
        return gameQueryService.getOpponentId(gameData, gameData.activePlayerId);
    }
}
