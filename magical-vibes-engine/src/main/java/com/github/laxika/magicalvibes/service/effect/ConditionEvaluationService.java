package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerControlsMoreLandsThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtMost;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.ActivationCount;
import com.github.laxika.magicalvibes.model.condition.AllNonartifactCreaturesShareColor;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AllMatchingCreaturesAttack;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ArtifactOrCreaturePutIntoGraveyardFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.condition.PermanentPutIntoGraveyardFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredLastTurn;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHandEmpty;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.condition.AnyGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.condition.AnyLibraryAtMost;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetMatches;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentAttackedTargetMatches;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.BlockedByMinCreatures;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.condition.CameUnderControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.CanBeholdSubtype;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.condition.CardsInExileAtLeast;
import com.github.laxika.magicalvibes.model.condition.CardsInHandMatchingAtLeast;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.condition.CardDirectlyAboveSelfInGraveyard;
import com.github.laxika.magicalvibes.model.condition.CardsAboveSelfInGraveyard;
import com.github.laxika.magicalvibes.model.condition.CardsLeftGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.CastForForetellCost;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.CastDuringMainPhase;
import com.github.laxika.magicalvibes.model.condition.CastForMadnessCost;
import com.github.laxika.magicalvibes.model.condition.CastNotFromHand;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.condition.ChosenColorStrictlyMostCommonAmongOpponentNontokens;
import com.github.laxika.magicalvibes.model.condition.ColorMostCommonAmongAllPermanents;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerCastTwoOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerCastTwoOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.condition.CommittedCrimeThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControlledMountAsCast;
import com.github.laxika.magicalvibes.model.condition.NoManaSpentToCast;
import com.github.laxika.magicalvibes.model.condition.ControllerCastThreeOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerCreatureSpellCounteredByOpponentThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerDidntPlayCardFromExileThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsMoreLandsThanOpponent;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsMorePermanentsThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.condition.ControllerDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerWasNotDealtCombatDamageSinceLastTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerHadNoCardsInHandAtTurnStart;
import com.github.laxika.magicalvibes.model.condition.ControllerDealtDamageByAtLeastCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerDrewAtLeastCardsThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedPermanentSubtypeAtLeastThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerDidntLoseLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerHasNoLandCardsInHand;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreCardsInHandThanEachOpponent;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerHasMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerLifeTotalEquals;
import com.github.laxika.magicalvibes.model.condition.NoCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.condition.NoCreaturesAttackedThisTurn;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreLifeThanController;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreLifeThanAnOpponent;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerMainPhase;
import com.github.laxika.magicalvibes.model.condition.ControllerEndStep;
import com.github.laxika.magicalvibes.model.condition.ControllerSurveiledThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerLostLifeLastTurn;
import com.github.laxika.magicalvibes.model.condition.BasicLandTypesAmongControlledLandsAtLeast;
import com.github.laxika.magicalvibes.model.condition.EachPlayerLifeAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerOwnTurnCountAtMost;
import com.github.laxika.magicalvibes.model.condition.ControllerPlayedOrCastFromOutsideHandThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerPlayedAtLeastLandsThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedPermanentThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsDistinctPermanentNamesCount;
import com.github.laxika.magicalvibes.model.condition.ControlsMoreCreaturesThanOpponent;
import com.github.laxika.magicalvibes.model.condition.APlayerControlsMoreCreaturesThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.condition.APlayerHasMoreCardsInHandThanEachOtherPlayer;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherThanTriggeringPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.condition.ControlsCreaturesSharingCreatureType;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentsWithDifferentNames;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentsWithSameName;
import com.github.laxika.magicalvibes.model.condition.ControlledCreatureCounterKindsAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalToughnessAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControlsCreatureWithGreatestPower;
import com.github.laxika.magicalvibes.model.condition.ControlsEachCreatureWithGreatestPower;
import com.github.laxika.magicalvibes.model.condition.Coven;
import com.github.laxika.magicalvibes.model.condition.CreatureAttackingController;
import com.github.laxika.magicalvibes.model.condition.CreaturesDiedThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.condition.CreatureDeathsThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerPoisoned;
import com.github.laxika.magicalvibes.model.condition.DealtDamageByRedSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorAtLeast;
import com.github.laxika.magicalvibes.model.condition.DevotionToColorsAtLeast;
import com.github.laxika.magicalvibes.model.condition.DevouredCreature;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.EnchantedCreatureDidntAttack;
import com.github.laxika.magicalvibes.model.condition.EnchantedCreaturePowerAtLeast;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.condition.DidntGainLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.condition.DuringCombat;
import com.github.laxika.magicalvibes.model.condition.EnchantedByAtLeastAuras;
import com.github.laxika.magicalvibes.model.condition.EndStepPlayerDidntCastCreatureSpell;
import com.github.laxika.magicalvibes.model.condition.EquippedCreatureDidntDealCombatDamageToCreatureThisTurn;
import com.github.laxika.magicalvibes.model.condition.ExtraTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentCastThreeOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.GiantWizardOrSpellDealtDamageToTargetThisTurn;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardMatches;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardNameMatchesEnteringPermanent;
import com.github.laxika.magicalvibes.model.condition.IsNight;
import com.github.laxika.magicalvibes.model.condition.CastForProwlCost;
import com.github.laxika.magicalvibes.model.condition.CastForSpectacleCost;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.PutCounterCostPaid;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.condition.BeholdCostPaid;
import com.github.laxika.magicalvibes.model.condition.PutCounterOnCreatureThisTurn;
import com.github.laxika.magicalvibes.model.condition.PlusOnePlusOneCounterPutOnControlledPermanentThisTurn;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackingCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.condition.AttachedPermanentControllerControlsNoOther;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.condition.SourceRegeneratedThisTurn;
import com.github.laxika.magicalvibes.model.condition.NoPlayerHasCardsInHand;
import com.github.laxika.magicalvibes.model.condition.TotalPermanentCountEven;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.DidntActivateLoyaltyAbilityThisTurn;
import com.github.laxika.magicalvibes.model.condition.DidntPlayLandThisTurn;
import com.github.laxika.magicalvibes.model.condition.EnteredFromZone;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotKicked;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreCreatures;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsMoreLands;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksPlaneswalker;
import com.github.laxika.magicalvibes.model.condition.OpponentDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentDrewAtLeastCardsThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentGainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.condition.OpponentPutThreeOrMoreCardsIntoGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeLastTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentOwnsCardInExile;
import com.github.laxika.magicalvibes.model.condition.OpponentPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.OilCounterEventThisTurn;
import com.github.laxika.magicalvibes.model.condition.NoncreaturePermanentDestroyedByOpponentThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentPoisoned;
import com.github.laxika.magicalvibes.model.condition.OpponentSearchedLibraryThisTurn;
import com.github.laxika.magicalvibes.model.condition.PermanentPutIntoYourHandFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.CreatureDiedUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesOfSubtypeThisTurn;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.condition.SelfDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.condition.SelfDealtDamageToOpponentThisTurn;
import com.github.laxika.magicalvibes.model.condition.SelfWasDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceDamagedCreatureDiedThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.SelfHasKeyword;
import com.github.laxika.magicalvibes.model.condition.SourceCardInCommandZone;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.condition.SourceCardSuspended;
import com.github.laxika.magicalvibes.model.condition.SourceCanSoulbond;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedOrBlockedThisCombat;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedOrBlockedThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceAddedManaThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedBattleThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceCounterCountParity;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.SourceExiledCardsThreshold;
import com.github.laxika.magicalvibes.model.condition.SourceExiledDifferentManaValuesThreshold;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.condition.SourceHasColor;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.condition.SourceHasDealtDamage;
import com.github.laxika.magicalvibes.model.condition.SourceBlockedOrWasBlockedByColorThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttackingOrBlocking;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.condition.SourceWasBlockedThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceWasSecondSpellCastThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.condition.SourceIsHarnessed;
import com.github.laxika.magicalvibes.model.condition.SourceIsRenowned;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.condition.SourceIsToken;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.condition.SacrificedCardMatches;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.condition.SpellCreatureManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentGreaterThanSourcePower;
import com.github.laxika.magicalvibes.model.condition.SnowManaSpentToCast;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.condition.TargetManaValueAtMostControllerGraveyardCount;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentManaValueEqualsControllerUnspentMana;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentPowerGreaterThanSourcePower;
import com.github.laxika.magicalvibes.model.condition.TargetSpellCanBeCountered;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsMoreCreaturesThanTargetSpellController;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.condition.TargetSpellSharesColorWithControlledCreature;
import com.github.laxika.magicalvibes.model.condition.TargetToughnessAtMostControllerGraveyardCount;
import com.github.laxika.magicalvibes.model.condition.TotalCreatureCardsInGraveyardsAtLeast;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryColor;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryMatchesPredicate;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.condition.WonClash;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The single evaluation point for every {@link Condition} in the engine.
 *
 * <p>The switch in {@link #isMet} is exhaustive over the sealed {@link Condition} hierarchy —
 * adding a condition without an evaluation is a compile error, never a silent
 * {@code false}. All evaluation contexts (stack resolution, trigger collection, ETB gating,
 * combat triggers, static bonus computation) call this service with a {@link ConditionContext}
 * describing where the values (kicked flag, source zone, x value, …) come from at that site.</p>
 */
@Service
@RequiredArgsConstructor
public class ConditionEvaluationService {

    private static final PermanentIsCreaturePredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();
    private static final PermanentIsArtifactPredicate ARTIFACT_FILTER = new PermanentIsArtifactPredicate();
    private static final ThreadLocal<Set<UUID>> COVEN_EVALUATION =
            ThreadLocal.withInitial(HashSet::new);

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    /**
     * Evaluates whether the given condition is currently met.
     */
    public boolean isMet(GameData gameData, Condition condition, ConditionContext ctx) {
        return switch (condition) {
            case NotCondition c ->
                    !isMet(gameData, c.inner(), ctx);
            case AllConditions c ->
                    c.conditions().stream().allMatch(inner -> isMet(gameData, inner, ctx));
            case CreatureAttackingController ignored ->
                    ctx.controllerId() != null && creatureAttackingPlayer(gameData, ctx.controllerId());
            case AllOf c ->
                    c.conditions().stream().allMatch(inner -> isMet(gameData, inner, ctx));
            case AnyOf c ->
                    c.conditions().stream().anyMatch(inner -> isMet(gameData, inner, ctx));
            case AnotherPermanentEnteredLastTurn c ->
                    anotherPermanentEnteredLastTurn(gameData, ctx, c);
            case AnotherPermanentEnteredThisTurn c ->
                    anotherPermanentEnteredThisTurn(gameData, ctx, c);
            case CameUnderControlThisTurn ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.isSummoningSick();
            }
            case CardDiscardedThisTurn ignored ->
                    ctx.controllerId() != null
                            && ctx.sourceCard() != null
                            && gameData.cardsDiscardedOrCycledThisTurn
                            .getOrDefault(ctx.controllerId(), Set.of())
                            .contains(ctx.sourceCard().getId());
            case SourceEnteredThisTurn ignored ->
                    sourceEnteredThisTurn(gameData, ctx);
            case SourceEnteredBattlefieldThisTurn ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && sourceEnteredBattlefieldThisTurn(gameData, source);
            }
            case Metalcraft ignored ->
                    isMetalcraftMet(gameData, ctx);
            case BasicLandTypesAmongControlledLandsAtLeast c ->
                    basicLandTypesAmongControlledLandsAtLeast(gameData, ctx, c.threshold());
            case MaxSpeed ignored ->
                    ctx.controllerId() != null && gameData.playerSpeeds.getOrDefault(ctx.controllerId(), 0) == 4;
            case Delirium ignored ->
                    isDeliriumMet(gameData, ctx);
            case DevotionToColorAtLeast c ->
                    devotionToColorAtLeast(gameData, ctx, c);
            case DevotionToColorsAtLeast c ->
                    devotionToColorsAtLeast(gameData, ctx, c);
            case Coven ignored ->
                    isCovenMet(gameData, ctx);
            case Morbid ignored ->
                    gameQueryService.isMorbidMet(gameData);
            case ArtifactOrCreaturePutIntoGraveyardFromBattlefieldThisTurn ignored ->
                    gameData.artifactOrCreaturePutIntoGraveyardFromBattlefieldThisTurn;
            case PermanentPutIntoGraveyardFromBattlefieldThisTurn ignored ->
                    gameData.permanentPutIntoGraveyardFromBattlefieldThisTurn;
            case CreaturesDiedThisTurnAtLeast c ->
                    gameData.creatureDeathCountThisTurn.values().stream()
                            .mapToInt(Integer::intValue)
                            .sum() >= c.minimum();
            case CreatureDiedUnderYourControlThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.creatureDeathCountThisTurn.getOrDefault(ctx.controllerId(), 0) > 0;
            case CreatureDeathsThisTurnAtLeast c ->
                    gameData.orderedPlayerIds.stream()
                            .mapToInt(playerId -> gameData.creatureDeathCountThisTurn.getOrDefault(playerId, 0))
                            .sum() >= c.minimum();
            case Kicked ignored ->
                    ctx.kicked();
            case NotKicked ignored ->
                    !ctx.kicked();
            case BuybackPaid ignored ->
                    ctx.buyback();
            case BeholdCostPaid ignored ->
                    ctx.beholdCostPaid();
            case CanBeholdSubtype c ->
                    canBeholdSubtype(gameData, ctx.controllerId(), c.subtype());
            case PutCounterCostPaid ignored ->
                    ctx.putCounterCostPaid();
            case CastForForetellCost ignored ->
                    ctx.castForForetell();
            case CastForAlternateCost ignored ->
                    ctx.alternateCost();
            case RepeatedAdditionalCostPaid c ->
                    ctx.repeatedAdditionalCosts().contains(c.manaCost());
            case CastForMadnessCost ignored ->
                    ctx.madness();
            case CastForProwlCost ignored ->
                    ctx.prowl();
            case CastForSpectacleCost ignored ->
                    ctx.spectacle();
            case Overloaded ignored ->
                    ctx.overloaded();
            case Raid ignored ->
                    ctx.controllerId() != null
                            && gameData.playersDeclaredAttackersThisTurn.contains(ctx.controllerId());
            case ControllerSacrificedPermanentThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.playersWhoSacrificedPermanentsThisTurn.contains(ctx.controllerId());
            case AttackedWithCreaturesThisTurn c ->
                    ctx.controllerId() != null
                            && gameData.creaturesAttackedCountThisTurn.getOrDefault(ctx.controllerId(), 0) >= c.minimum();
            case AttackedWithCreaturesOfSubtypeThisTurn c ->
                    ctx.controllerId() != null
                            && gameData.creaturesAttackedCountBySubtypeThisTurn
                            .getOrDefault(ctx.controllerId(), Map.of())
                            .getOrDefault(c.subtype(), 0) >= c.minimum();
            case Equipped ignored ->
                    isSourceEquipped(gameData, ctx);
            case EquippedCreatureDidntDealCombatDamageToCreatureThisTurn ignored ->
                    equippedCreatureDidntDealCombatDamageToCreatureThisTurn(gameData, ctx);
            case Enchanted ignored ->
                    isSourceEnchanted(gameData, ctx);
            case EnchantedByAtLeastAuras c ->
                    countAurasAttachedToSource(gameData, ctx) >= c.minimum();
            case EndStepPlayerDidntCastCreatureSpell ignored ->
                    ctx.targetId() != null
                            && gameData.getSpellsCastThisTurn(ctx.targetId()).stream()
                                    .noneMatch(spell -> spell.hasType(CardType.CREATURE));
            case ExtraTurn ignored -> gameData.currentTurnIsExtraTurn;
            case GainedLifeThisTurn gainedLife ->
                    ctx.controllerId() != null
                            && gameData.getLifeGainedThisTurn(ctx.controllerId()) >= gainedLife.minimumAmount();
            case CommittedCrimeThisTurn ignored ->
                    ctx.controllerId() != null && gameData.hasCommittedCrimeThisTurn(ctx.controllerId());
            case ControlledMountAsCast ignored -> ctx.controlledMountAsCast();
            case GiantWizardOrSpellDealtDamageToTargetThisTurn ignored ->
                    ctx.controllerId() != null
                            && ctx.targetId() != null
                            && gameData.qualifyingDamageControllersByPermanentThisTurn
                            .getOrDefault(ctx.targetId(), Set.of())
                            .contains(ctx.controllerId());
            case DidntGainLifeThisTurn ignored ->
                    ctx.controllerId() != null && !gameData.hasGainedLifeThisTurn(ctx.controllerId());
            case ControlsPermanent c ->
                    controlsMatchingPermanent(gameData, ctx, c.filter());
            case ControlsAnotherPermanent c ->
                    controlsAnotherMatchingPermanent(gameData, ctx, c.filter());
            case ControlsDistinctPermanentNamesCount c ->
                    countControlledMatchingPermanentNames(gameData, ctx, c.filter()) >= c.minCount();
            case OpponentControlsPermanent c ->
                    opponentControlsMatchingPermanent(gameData, ctx, c.filter());
            case AnyPlayerControlsPermanent c ->
                    anyPlayerControlsMatchingPermanent(gameData, ctx, c.filter());
            case AnyPlayerControlsPermanentCount c ->
                    countMatchingPermanentsOnBattlefield(gameData, ctx, c.filter()) >= c.minCount();
            case AnyPlayerControlsPermanentCountAtMost c ->
                    countMatchingPermanentsOnBattlefield(gameData, ctx, c.filter()) <= c.maxCount();
            case ControlsPermanentCount c ->
                    countControlledMatchingPermanents(gameData, ctx, c.filter()) >= c.minCount();
            case ControlsPermanentCountAtMost c ->
                    countControlledMatchingPermanents(gameData, ctx, c.filter()) <= c.maxCount();
            case ControlsCreaturesSharingCreatureType c ->
                    controlsCreaturesSharingCreatureType(gameData, ctx, c.minimum());
            case ControlsPermanentsWithDifferentNames c ->
                    countControlledMatchingPermanentNames(gameData, ctx, c.filter()) >= c.minCount();
            case ControlsPermanentsWithSameName c ->
                    controlsMatchingPermanentsWithSameName(gameData, ctx, c.minCount(), c.filter());
            case ControlledCreatureCounterKindsAtLeast c ->
                    controlledCreatureCounterKinds(gameData, ctx) >= c.threshold();
            case ControlsOtherPermanentCount c ->
                    countOtherControlledMatchingPermanents(gameData, ctx, c.filter()) >= c.minCount();
            case ControlsOtherThanTriggeringPermanentCount c ->
                    countOtherThanTriggeringControlledMatchingPermanents(gameData, ctx, c.filter()) >= c.minCount();
            case ControlledCreaturesTotalPowerAtLeast c ->
                    controlledCreaturesTotalPower(gameData, ctx) >= c.threshold();
            case ControlledCreaturesTotalToughnessAtLeast c ->
                    controlledCreaturesTotalToughness(gameData, ctx) >= c.threshold();
            case ControlsCreatureWithGreatestPower ignored ->
                    controlsCreatureWithGreatestPower(gameData, ctx);
            case ControlsEachCreatureWithGreatestPower ignored ->
                    controlsEachCreatureWithGreatestPower(gameData, ctx);
            case SourceRegeneratedThisTurn ignored ->
                    sourcePermanent(gameData, ctx) != null
                            && sourcePermanent(gameData, ctx).getTimesRegeneratedThisTurn() > 0;
            case NoOtherPermanent c ->
                    noOtherMatchingPermanent(gameData, ctx, c.filter());
            case AttachedPermanentControllerControlsNoOther c ->
                    attachedPermanentControllerControlsNoOther(gameData, ctx, c.filter());
            case ControllerHasMoreLifeThanAnOpponent ignored ->
                    controllerHasMoreLifeThanAnOpponent(gameData, ctx.controllerId());
            case ControllerHasCityBlessing ignored ->
                    ctx.controllerId() != null && gameData.playersWithCityBlessing.contains(ctx.controllerId());
            case ControllerHasMoreCardsInHandThanEachOpponent ignored ->
                    controllerHasMoreCardsInHandThanEachOpponent(gameData, ctx.controllerId());
            case AnOpponentHasMoreCardsInHandThanController ignored ->
                    anOpponentHasMoreCardsInHandThanController(gameData, ctx.controllerId());
            case AnOpponentHasMoreLifeThanController ignored ->
                    anOpponentHasMoreLifeThanController(gameData, ctx.controllerId());
            case ControllerLifeAtLeast c ->
                    ctx.controllerId() != null
                            && gameData.playerLifeTotals.getOrDefault(ctx.controllerId(), 20) >= c.threshold();
            case ControllerLifeAtMost c ->
                    ctx.controllerId() != null
                            && gameData.playerLifeTotals.getOrDefault(ctx.controllerId(), 20) <= c.threshold();
            case ControllerEnergyAtLeast c ->
                    ctx.controllerId() != null
                            && gameData.playerEnergyCounters.getOrDefault(ctx.controllerId(), 0) >= c.threshold();
            case AnOpponentLifeAtMost c ->
                    ctx.controllerId() != null
                            && gameData.orderedPlayerIds.stream()
                            .filter(pid -> !pid.equals(ctx.controllerId()))
                            .anyMatch(pid -> gameData.getLife(pid) <= c.threshold());
            case EachPlayerLifeAtMost c ->
                    gameData.orderedPlayerIds.stream()
                            .allMatch(pid -> gameData.playerLifeTotals.getOrDefault(pid, 20) <= c.threshold());
            case GraveyardCardThreshold c ->
                    countMatchingGraveyardCards(gameData, ctx, c) >= c.threshold();
            case CardsAboveSelfInGraveyard c ->
                    countCardsAboveSelfInGraveyard(gameData, ctx, c) >= c.threshold();
            case CardDirectlyAboveSelfInGraveyard c ->
                    matchesCardDirectlyAboveSelfInGraveyard(gameData, ctx, c);
            case CardsInLibraryAtLeast c ->
                    countCardsInLibrary(gameData, ctx.controllerId()) >= c.threshold();
            case AnyGraveyardAtLeast c ->
                    anyGraveyardAtLeast(gameData, c.threshold());
            case TotalCreatureCardsInGraveyardsAtLeast c ->
                    totalCreatureCardsInGraveyards(gameData) >= c.threshold();
            case AnyLibraryAtMost c ->
                    anyLibraryAtMost(gameData, c.threshold());
            case CardsInHandAtLeast c ->
                    countCardsInHand(gameData, ctx.controllerId()) >= c.threshold();
            case CardsInHandAtMost c ->
                    countCardsInHand(gameData, ctx.controllerId()) <= c.threshold();
            case CardsInExileAtLeast c ->
                    gameData.exiledCards.size() >= c.threshold();
            case CardsInHandMatchingAtLeast c ->
                    countMatchingCardsInHand(gameData, ctx.controllerId(), c.filter()) >= c.threshold();
            case ActivePlayerControlsPermanent c ->
                    activePlayerControlsMatchingPermanent(gameData, ctx, c.filter());
            case ActivePlayerControlsMoreLandsThanEachOtherPlayer ignored ->
                    activePlayerControlsMoreLandsThanEachOtherPlayer(gameData);
            case ControllerControlsMorePermanentsThanEachOtherPlayer ignored ->
                    controllerControlsMorePermanentsThanEachOtherPlayer(gameData, ctx.controllerId());
            case ActivePlayerHandAtLeast c ->
                    countCardsInHand(gameData, gameData.activePlayerId) >= c.threshold();
            case ActivePlayerHandAtMost c ->
                    countCardsInHand(gameData, gameData.activePlayerId) <= c.threshold();
            case ActivePlayerHandEmpty ignored ->
                    countCardsInHand(gameData, gameData.activePlayerId) == 0;
            case NoCardsExiledWithSource ignored ->
                    ctx.sourcePermanentId() != null
                            && gameData.exiledCards.stream()
                                    .noneMatch(e -> ctx.sourcePermanentId().equals(e.sourcePermanentId()));
            case ControllerHandEmpty ignored ->
                    countCardsInHand(gameData, ctx.controllerId()) == 0;
            case ControllerHasNoLandCardsInHand ignored ->
                    ctx.controllerId() != null
                            && gameData.playerHands.getOrDefault(ctx.controllerId(), List.of()).stream()
                            .noneMatch(card -> card.hasType(CardType.LAND));
            case ControllerHadNoCardsInHandAtTurnStart ignored ->
                    ctx.controllerId() != null
                            && gameData.handSizeAtTurnStart.getOrDefault(ctx.controllerId(), -1) == 0;
            case TargetPlayerHandEmpty ignored ->
                    ctx.targetId() != null && countCardsInHand(gameData, ctx.targetId()) == 0;
            case TargetPlayerHasMoreCardsInHandThanController ignored ->
                    ctx.targetId() != null
                            && ctx.controllerId() != null
                            && countCardsInHand(gameData, ctx.targetId())
                            > countCardsInHand(gameData, ctx.controllerId());
            case TargetPlayerControlsPermanent c ->
                    targetPlayerControlsMatchingPermanent(gameData, ctx, c.filter());
            case TargetPlayerLifeTotalEquals c ->
                    ctx.targetId() != null && gameData.getLife(ctx.targetId()) == c.lifeTotal();
            case CastFromZone c ->
                    c.sourceZone() == ctx.sourceZone();
            case EnteredFromZone c ->
                    ctx.sourcePermanent() != null && c.sourceZone() == ctx.sourcePermanent().getEnteredFromZone();
            case CastNotFromHand ignored ->
                    ctx.sourceZone() != Zone.HAND;
            case NoManaSpentToCast ignored -> {
                Permanent castPermanent = ctx.triggeringPermanentId() == null
                        ? ctx.sourcePermanent()
                        : gameQueryService.findPermanentById(gameData, ctx.triggeringPermanentId());
                yield castPermanent == null || !castPermanent.isCast()
                        || castPermanent.getManaSpentToCast() == 0;
            }
            case WasCast ignored -> {
                Permanent triggeringPermanent = ctx.triggeringPermanentId() == null
                        ? null : gameQueryService.findPermanentById(gameData, ctx.triggeringPermanentId());
                yield triggeringPermanent != null
                        ? triggeringPermanent.isCast()
                        : ctx.sourcePermanent() != null
                        ? ctx.sourcePermanent().isCast()
                        : ctx.sourceZone() != null;
            }
            case DidntAttack ignored ->
                    sourceDidntAttackThisTurn(gameData, ctx);
            case EnchantedCreatureDidntAttack ignored ->
                    enchantedCreatureDidntAttackThisTurn(gameData, ctx);
            case EnchantedCreaturePowerAtLeast c ->
                    enchantedCreaturePowerAtLeast(gameData, ctx, c.threshold());
            case EnchantedPermanentMatches c ->
                    enchantedPermanentMatches(gameData, ctx, c.filter());
            case AttacksAlone ignored ->
                    countAttackingCreatures(gameData, ctx.controllerId()) == 1;
            case AttackedTargetMatches c -> {
                Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
                yield target != null && matchesPermanent(gameData, target, c.filter(), ctx);
            }
            case TargetPermanentAttackedTargetMatches c -> {
                Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
                Permanent attackedTarget = target == null || target.getAttackTarget() == null
                        ? null : gameQueryService.findPermanentById(gameData, target.getAttackTarget());
                yield attackedTarget != null && matchesPermanent(gameData, attackedTarget, c.filter(), ctx);
            }
            case AllMatchingCreaturesAttack c ->
                    allMatchingCreaturesAttack(gameData, ctx, c.filter());
            case DuringCombat ignored ->
                    gameData.currentStep != null && gameData.currentStep.isCombatPhase();
            case FirstCombatPhase ignored ->
                    gameData.combatPhasesThisTurn == 1;
            case EventValueAtLeast c ->
                    ctx.eventValue() >= c.minimum();
            case MinimumAttackers c ->
                    ctx.xValue() >= c.minimumAttackers();
            case MinimumMatchingAttackers c ->
                    countMatchingAttackers(gameData, ctx, c.predicate()) >= c.minimum();
            case OpponentAttacksWithAtLeastCreatures c ->
                    countOpponentAttackersAtControllerOrPlaneswalkers(gameData, ctx) >= c.minimum();
            case OpponentAttacksPlaneswalker ignored ->
                    opponentAttacksPlaneswalker(gameData, ctx);
            case MinimumAttackingCreaturesOfSubtype c ->
                    countAttackingCreaturesOfSubtype(gameData, ctx.controllerId(), c.subtype()) >= c.minimum();
            case HasAttacker c ->
                    hasMatchingAttacker(gameData, ctx, c.predicate());
            case NoPlayerHasCardsInHand ignored ->
                    noPlayerHasCardsInHand(gameData);
            case TotalPermanentCountEven ignored ->
                    totalPermanentCount(gameData) % 2 == 0;
            case AnOpponentHandEmpty ignored ->
                    isAnyOpponentHandEmpty(gameData, ctx.controllerId());
            case NoSpellsCastLastTurn ignored ->
                    noSpellsCastLastTurn(gameData);
            case IsNight ignored ->
                    gameData.dayNight == DayNight.NIGHT;
            case NoCreaturesAttackedThisTurn ignored ->
                    gameData.creaturesAttackedCountThisTurn.values().stream().noneMatch(count -> count > 0);
            case TwoOrMoreSpellsCastLastTurn ignored ->
                    gameData.spellsCastLastTurn.values().stream().anyMatch(count -> count >= 2);
            case DefendingPlayerControlsPermanent c ->
                    defendingPlayerControlsMatchingPermanent(gameData, ctx, c.filter());
            case DefendingPlayerPoisoned ignored ->
                    isDefendingPlayerPoisoned(gameData, ctx.controllerId());
            case OpponentPoisoned c ->
                    isAnyOpponentPoisoned(gameData, ctx.controllerId(), c.minimumPoisonCounters());
            case OpponentGraveyardAtLeast c ->
                    anyOpponentGraveyardAtLeast(gameData, ctx.controllerId(), c.threshold());
            case OpponentPutThreeOrMoreCardsIntoGraveyardThisTurn ignored ->
                    opponentPutThreeOrMoreCardsIntoGraveyardThisTurn(gameData, ctx);
            case OpponentOwnsCardInExile ignored ->
                    opponentOwnsCardInExile(gameData, ctx.controllerId());
            case OpponentSearchedLibraryThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.orderedPlayerIds.stream()
                            .filter(playerId -> !playerId.equals(ctx.controllerId()))
                            .anyMatch(gameData.playersWhoSearchedLibraryThisTurn::contains);
            case PermanentPutIntoYourHandFromBattlefieldThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.playersWhoReceivedPermanentFromBattlefieldToHandThisTurn
                                    .contains(ctx.controllerId());
            case PermanentLeftBattlefieldUnderYourControlThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.playersWhosePermanentsLeftBattlefieldThisTurn
                                    .contains(ctx.controllerId());
            case DidntActivateLoyaltyAbilityThisTurn ignored ->
                    ctx.controllerId() != null
                            && !gameData.playersWhoActivatedLoyaltyAbilityThisTurn.contains(ctx.controllerId());
            case DidntPlayLandThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.landsPlayedThisTurn.getOrDefault(ctx.controllerId(), 0) == 0;
            case ControllerPlayedAtLeastLandsThisTurn c ->
                    ctx.controllerId() != null
                            && gameData.landsPlayedThisTurn.getOrDefault(ctx.controllerId(), 0) >= c.minimum();
            case DealtDamageByRedSpellThisTurn ignored ->
                    gameData.lastRedSpellDamagerThisTurn.containsKey(ctx.controllerId());
            case OpponentDealtDamageThisTurn c ->
                    wasAnyOpponentDealtDamageThisTurn(gameData, ctx.controllerId(), c.minimumAmount());
            case OpponentDrewAtLeastCardsThisTurn c ->
                    opponentDrewAtLeastCardsThisTurn(gameData, ctx.controllerId(), c.minimum());
            case OpponentGainedLifeThisTurn c ->
                    didAnyOpponentGainLifeThisTurn(gameData, ctx.controllerId(), c.minimumAmount());
            case SelfDealtDamageThisTurn c ->
                    ctx.sourcePermanentId() != null
                            && gameData.damageDealtThisTurnBySource.getOrDefault(ctx.sourcePermanentId(), 0)
                            >= c.minimumAmount();
            case SourceHasDealtDamage ignored ->
                    ctx.sourcePermanentId() != null
                            && gameData.permanentsThatHaveDealtDamage.contains(ctx.sourcePermanentId());
            case ControllerDealtDamageThisTurn c ->
                    ctx.controllerId() != null
                            && gameData.damageDealtToPlayersThisTurn.getOrDefault(ctx.controllerId(), 0)
                                    >= Math.max(1, c.minimumAmount());
            case ControllerWasNotDealtCombatDamageSinceLastTurn ignored ->
                    ctx.controllerId() != null
                            && !gameData.playersDealtCombatDamageLastTurn.contains(ctx.controllerId());
            case ControllerDealtDamageByAtLeastCreaturesThisTurn c ->
                    countCreatureDamageSourcesToPlayer(gameData, ctx.controllerId())
                            >= Math.max(1, c.minimumCreatures());
            case ControllerDrewAtLeastCardsThisTurn c ->
                    ctx.controllerId() != null
                            && gameData.cardsDrawnThisTurn.getOrDefault(ctx.controllerId(), 0) >= c.minimum();
            case ControllerSacrificedPermanentSubtypeAtLeastThisTurn c ->
                    ctx.controllerId() != null
                            && gameData.sacrificedPermanentSubtypeCountThisTurn
                                    .getOrDefault(ctx.controllerId(), Map.of())
                                    .getOrDefault(c.subtype(), 0) >= c.minimum();
            case ControllerSurveiledThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.playersWhoSurveilledThisTurn.contains(ctx.controllerId());
            case SelfDealtDamageToOpponentThisTurn ignored ->
                    sourceDealtDamageToOpponentThisTurn(gameData, ctx);
            case SelfWasDealtDamageThisTurn c ->
                    ctx.sourcePermanentId() != null
                            && gameData.damageDealtToPermanentsThisTurn.getOrDefault(ctx.sourcePermanentId(), 0)
                            >= Math.max(1, c.minimumAmount());
            case SourceDamagedCreatureDiedThisTurn ignored ->
                    ctx.sourcePermanentId() != null
                            && gameData.sourcesWhoseDamagedCreaturesDiedThisTurn.contains(ctx.sourcePermanentId());
            case OpponentLostLifeThisTurn c ->
                    didAnyOpponentLoseLifeThisTurn(gameData, ctx.controllerId(), c.minimumAmount());
            case AnyPlayerLostLifeThisTurn c ->
                    didAnyPlayerLoseLifeThisTurn(gameData, c.minimumAmount());
            case OpponentLostLifeLastTurn ignored ->
                    didAnyOpponentLoseLifeLastTurn(gameData, ctx.controllerId());
            case ControllerDidntLoseLifeThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.lifeLostThisTurn.getOrDefault(ctx.controllerId(), 0) <= 0;
            case ControllerLostLifeLastTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.lifeLostLastTurn.getOrDefault(ctx.controllerId(), 0) > 0;
            case ActivationCount c ->
                    activationCountThisTurn(gameData, ctx, c.abilityIndex()) >= c.threshold();
            // Exact equality: "if this is the Nth time this ability has resolved this turn"
            // fires on the exact n-th resolution and never on a later one.
            case NthAbilityResolutionThisTurn c ->
                    ctx.sourcePermanentId() != null
                            && gameData.permanentAbilityResolutionsThisTurn
                                    .getOrDefault(ctx.sourcePermanentId(), 0) == c.n();
            case PermanentEnteredThisTurn c ->
                    countPermanentsEnteredThisTurn(gameData, ctx, c) >= c.minCount();
            case PutCounterOnCreatureThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.playersWhoPutCountersOnCreaturesThisTurn.contains(ctx.controllerId());
            case PlusOnePlusOneCounterPutOnControlledPermanentThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn
                            .contains(ctx.controllerId());
            case OilCounterEventThisTurn ignored ->
                    (ctx.controllerId() != null
                            && gameData.playersWhoRemovedOilCountersFromControlledPermanentsThisTurn
                            .contains(ctx.controllerId()))
                            || gameData.permanentWithOilCounterPutIntoGraveyardThisTurn;
            case ControllerCastAnotherSpellThisTurn c ->
                    ctx.controllerId() != null && gameQueryService.hasControllerCastAnotherSpellThisTurn(
                            gameData, ctx.controllerId(), ctx.sourceCard(), c.filter());
            case ControllerCastSpellThisTurn c ->
                    ctx.controllerId() != null && gameQueryService.hasControllerCastAnotherSpellThisTurn(
                            gameData, ctx.controllerId(), null, c.filter(), c.fromHandOnly());
            case SourceWasSecondSpellCastThisTurn ignored ->
                    ctx.controllerId() != null
                            && ctx.sourceCard() != null
                            && gameData.getSpellsCastThisTurn(ctx.controllerId()).size() > 1
                            && ctx.sourceCard().getId().equals(
                            gameData.getSpellsCastThisTurn(ctx.controllerId()).get(1).getId());
            case ControllerCastTwoOrMoreSpellsThisTurn c ->
                    ctx.controllerId() != null && gameQueryService.hasControllerCastTwoOrMoreSpellsThisTurn(
                            gameData, ctx.controllerId(), c.filter());
            case ControllerCastThreeOrMoreSpellsThisTurn c ->
                    ctx.controllerId() != null && gameQueryService.hasControllerCastThreeOrMoreSpellsThisTurn(
                            gameData, ctx.controllerId(), c.filter());
            case ControllerCreatureSpellCounteredByOpponentThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.playersWhoseCreatureSpellsWereCounteredByOpponentsThisTurn
                            .contains(ctx.controllerId());
            case ControllerDidntPlayCardFromExileThisTurn ignored ->
                    ctx.controllerId() != null
                            && !gameData.playersWhoPlayedCardFromExileThisTurn.contains(ctx.controllerId());
            case ControllerControlsMoreLandsThanOpponent ignored ->
                    ctx.controllerId() != null
                            && gameQueryService.controlsMoreLandsThan(
                                    gameData, ctx.controllerId(), gameQueryService.getOpponentId(gameData, ctx.controllerId()));
            case OpponentCastSpellThisTurn c ->
                    opponentCastMatchingSpellThisTurn(gameData, ctx, c.filter());
            case OpponentCastThreeOrMoreSpellsThisTurn ignored ->
                    opponentCastThreeOrMoreSpellsThisTurn(gameData, ctx);
            case OpponentPermanentEnteredThisTurn c ->
                    opponentPermanentEnteredThisTurn(gameData, ctx, c);
            case NoncreaturePermanentDestroyedByOpponentThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.playersWhoseNoncreaturePermanentsWereDestroyedByOpponentThisTurn
                            .contains(ctx.controllerId());
            case SpellManaSpentAtLeast c ->
                    ctx.xValue() >= c.minMana();
            case SpellCreatureManaSpentAtLeast c ->
                    ctx.xValue() >= c.minMana();
            case SpellManaSpentGreaterThanSourcePower ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && ctx.xValue() > gameQueryService.getEffectivePower(gameData, source);
            }
            case SpellXAtLeast c ->
                    ctx.xValue() >= c.minX();
            case TargetManaValueAtMostControllerGraveyardCount ignored ->
                    targetManaValueAtMostControllerGraveyardCount(gameData, ctx);
            case ColorSpentToCast c ->
                    ctx.sourceCard() != null
                            && gameData.getSpellCastManaSpentByColor(ctx.sourceCard().getId(), c.color())
                            >= c.minimumAmount();
            case SnowManaSpentToCast ignored ->
                    ctx.sourceCard() != null
                            && gameData.getSpellCastSnowManaSpent(ctx.sourceCard().getId()) > 0;
            case CastDuringMainPhase ignored ->
                    ctx.castDuringMainPhase();
            case ControllerTurn ignored ->
                    ctx.controllerId() != null && ctx.controllerId().equals(gameData.activePlayerId);
            case ControllerMainPhase ignored ->
                    ctx.controllerId() != null
                            && ctx.controllerId().equals(gameData.activePlayerId)
                            && (gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                            || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN);
            case ControllerEndStep ignored ->
                    ctx.controllerId() != null
                            && ctx.controllerId().equals(gameData.activePlayerId)
                            && gameData.currentStep == TurnStep.END_STEP;
            case ControllerOwnTurnCountAtMost c ->
                    ctx.controllerId() != null && ctx.controllerId().equals(gameData.activePlayerId)
                            && gameData.turnsTakenByPlayer.getOrDefault(ctx.controllerId(), 0) <= c.maxTurns();
            case ControllerPlayedOrCastFromOutsideHandThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.playersWhoPlayedOrCastFromOutsideHandThisTurn.contains(ctx.controllerId());
            case NotControllerTurn ignored ->
                    ctx.controllerId() != null && !ctx.controllerId().equals(gameData.activePlayerId);
            case TargetPermanentMatches c -> {
                Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
                yield target != null && matchesPermanent(gameData, target, c.filter(), ctx);
            }
            case TargetPermanentManaValueEqualsControllerUnspentMana ignored -> {
                Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
                var manaPool = ctx.controllerId() == null
                        ? null
                        : gameData.playerManaPools.get(ctx.controllerId());
                yield target != null && manaPool != null
                        && target.getCard().getManaValue() == manaPool.getTotalAllMana();
            }
            case TargetToughnessAtMostControllerGraveyardCount ignored ->
                    targetToughnessAtMostControllerGraveyardCount(gameData, ctx);
            case TargetSpellCanBeCountered ignored -> {
                com.github.laxika.magicalvibes.model.StackEntry targetSpell = ctx.targetId() == null ? null
                        : gameData.stack.stream()
                                .filter(se -> se.getCard().getId().equals(ctx.targetId()))
                                .findFirst().orElse(null);
                yield targetSpell != null
                        && !gameQueryService.isUncounterable(gameData, targetSpell.getCard())
                        && !(ctx.sourceCard() != null && gameQueryService.isProtectedFromCounterBySourceCard(
                                gameData, targetSpell.getControllerId(), ctx.sourceCard()));
            }
            case ControllerControlsMoreCreaturesThanTargetSpellController ignored ->
                    controllerControlsMoreCreaturesThanTargetSpellController(gameData, ctx);
            case TargetSpellMatches c -> {
                com.github.laxika.magicalvibes.model.StackEntry targetSpell = ctx.targetId() == null ? null
                        : gameData.stack.stream()
                                .filter(se -> se.getCard().getId().equals(ctx.targetId()))
                                .findFirst().orElse(null);
                yield targetSpell != null
                        && predicateEvaluationService.matchesStackEntryPredicate(targetSpell, c.filter(), null);
            }
            case TargetSpellSharesColorWithControlledCreature ignored ->
                    targetSpellSharesColorWithControlledCreature(gameData, ctx);
            case SourceHasSubtype c ->
                    sourceHasSubtype(gameData, ctx, c.subtype());
            case SourceHasColor c -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && gameQueryService.getEffectiveColors(gameData, source).contains(c.color());
            }
            case SourceHasChosenMode c -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.getChosenModeLabels().contains(c.mode());
            }
            case SelfHasKeyword c -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.hasKeyword(c.keyword());
            }
            case SourceCardInCommandZone ignored ->
                    isSourceCardInCommandZone(gameData, ctx);
            case SourceCardInGraveyard ignored ->
                    isSourceCardInGraveyard(gameData, ctx);
            case SourceCardSuspended ignored ->
                    isSourceCardSuspended(gameData, ctx);
            case SourceAttackedOrBlockedThisCombat ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && (source.isAttackedThisCombat() || source.isBlockedThisCombat());
            }
            case SourceAttackedOrBlockedThisTurn ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && (source.isAttackedThisTurn() || source.isBlockedThisTurn());
            }
            case SourceIsPaired ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.getPairedWithId() != null;
            }
            case SourceCanSoulbond ignored ->
                    canSoulbond(gameData, ctx);
            case SourceIsRenowned ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.isRenowned();
            }
            case SourceIsMonstrous ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.isMonstrous();
            }
            case SourceIsHarnessed ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.isHarnessed();
            }
            case SourceIsSaddled ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.isSaddled();
            }
            case SourceCounterThreshold c -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.getCounterCount(c.counterType()) >= c.threshold();
            }
            case SourceExiledCardsThreshold c ->
                    ctx.sourcePermanentId() != null
                            && gameData.exiledCards.stream()
                                    .filter(e -> ctx.sourcePermanentId().equals(e.sourcePermanentId()))
                                    .filter(e -> !e.card().isToken())
                                    .count() >= c.threshold();
            case SourceExiledDifferentManaValuesThreshold c ->
                    ctx.sourcePermanentId() != null
                            && gameData.exiledCards.stream()
                                    .filter(e -> ctx.sourcePermanentId().equals(e.sourcePermanentId()))
                                    .filter(e -> !e.card().isToken())
                                    .map(e -> e.card().getManaValue())
                                    .distinct()
                                    .count() >= c.threshold();
            case TriggeringPermanentPowerGreaterThanSourcePower ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                if (source == null || ctx.triggeringPermanentPowerAtTrigger() == null) {
                    yield false;
                }
                Permanent triggeringPermanent = ctx.triggeringPermanentId() == null
                        ? null : gameQueryService.findPermanentById(gameData, ctx.triggeringPermanentId());
                int triggeringPower = triggeringPermanent == null
                        ? ctx.triggeringPermanentPowerAtTrigger()
                        : gameQueryService.getEffectivePower(gameData, triggeringPermanent);
                yield triggeringPower > gameQueryService.getEffectivePower(gameData, source);
            }
            case SourceCounterCountParity c -> {
                Permanent source = sourcePermanent(gameData, ctx);
                int counterCount = source == null
                        ? -1
                        : source.getCounters().values().stream().mapToInt(Integer::intValue).sum();
                yield source != null && c.parity().matches(counterCount);
            }
            case SourceAddedManaThisTurn ignored ->
                    ctx.sourcePermanentId() != null
                            && gameData.permanentsThatAddedManaWithAbilityThisTurn.contains(ctx.sourcePermanentId());
            case SourceAttackedBattleThisTurn ignored ->
                    ctx.sourcePermanentId() != null
                            && gameData.permanentsThatAttackedBattlesThisTurn.contains(ctx.sourcePermanentId());
            case DevouredCreature ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && !source.getDevouredCreatures().isEmpty();
            }
            case SourceUntapped ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && !source.isTapped();
            }
            case SourceBlockedOrWasBlockedByColorThisTurn c ->
                    ctx.sourcePermanentId() != null
                            && gameData.combatBlockOpponentColorsThisTurn
                                    .getOrDefault(ctx.sourcePermanentId(), java.util.Set.of())
                                    .contains(c.color());
            case SourceWasBlockedThisTurn ignored ->
                    ctx.sourcePermanentId() != null
                            && gameData.creaturesBlockedThisTurn.contains(ctx.sourcePermanentId());
            case SourceIsAttacking ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.isAttacking();
            }
            case SourceIsAttackingOrBlocking ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && (source.isAttacking() || source.isBlocking());
            }
            case SourceIsCreature ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && gameQueryService.isCreature(gameData, source);
            }
            case SourceIsEnchantment ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && gameQueryService.isEnchantment(gameData, source);
            }
            case SourceIsOnBattlefield ignored -> ctx.sourcePermanentId() != null
                    && gameQueryService.findPermanentById(gameData, ctx.sourcePermanentId()) != null;
            case SourcePowerAtLeast c -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && gameQueryService.getEffectivePower(gameData, source) >= c.threshold();
            }
            case SourceIsTapped ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.isTapped();
            }
            case SourceIsToken ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null && source.getCard().isToken();
            }
            case SacrificedCardMatches c ->
                    ctx.sacrificedCard() != null
                            && predicateEvaluationService.matchesCardPredicate(
                            ctx.sacrificedCard(), c.filter(), null, gameData, ctx.sacrificedCard().getOwnerId());
            case TopCardOfLibraryColor c ->
                    isTopCardOfLibraryColor(gameData, ctx.controllerId(), c);
            case TopCardOfLibraryMatchesPredicate c ->
                    isTopCardOfLibraryMatchesPredicate(
                            gameData, resolveLibraryOwner(gameData, ctx, c.libraryOwner()), c);
            case TopCardOfLibraryType c ->
                    isTopCardOfLibraryType(gameData, resolveLibraryOwner(gameData, ctx, c.libraryOwner()), c);
            case BlockedByMinCreatures c ->
                    countBlockersOfSource(gameData, ctx) >= c.minBlockers();
            case ImprintedCardMatches c -> imprintedCardMatches(gameData, ctx, c);
            case ImprintedCardNameMatchesEnteringPermanent ignored ->
                    imprintedCardNameMatches(gameData, ctx);
            case OpponentControlsMoreCreatures c ->
                    anyOpponentControlsAtLeastNMoreCreatures(gameData, ctx, c.minimumCreatureDifference());
            case ControlsMoreCreaturesThanOpponent ignored ->
                    controlsMoreCreaturesThanOpponent(gameData, ctx);
            case APlayerControlsMoreCreaturesThanEachOtherPlayer ignored ->
                    aPlayerControlsMoreCreaturesThanEachOtherPlayer(gameData);
            case APlayerHasMoreCardsInHandThanEachOtherPlayer ignored ->
                    aPlayerHasMoreCardsInHandThanEachOtherPlayer(gameData);
            case OpponentControlsMoreLands ignored ->
                    gameQueryService.anyOpponentControlsMoreLands(gameData, ctx.controllerId());
            case OpponentControlsPermanentCount c ->
                    opponentControlsAtLeastMatchingPermanents(gameData, ctx, c.minCount(), c.filter());
            case ChosenColorStrictlyMostCommonAmongOpponentNontokens ignored -> {
                Permanent source = sourcePermanent(gameData, ctx);
                yield source != null
                        && ChosenColorStrictlyMostCommonAmongOpponentNontokens.isStrictlyMostCommon(
                                gameData, source, ctx.controllerId());
            }
            case ColorMostCommonAmongAllPermanents c ->
                    ColorMostCommonAmongAllPermanents.isMostCommon(gameData, c.color());
            case AllNonartifactCreaturesShareColor ignored ->
                    allNonartifactCreaturesShareColor(gameData);
            case CardsLeftGraveyardThisTurn ignored ->
                    ctx.controllerId() != null
                            && gameData.playersWhoseCardsLeftGraveyardThisTurn.contains(ctx.controllerId());
            case WonClash ignored ->
                    ctx.controllerId() != null
                            && gameData.lastClashWonByController.getOrDefault(ctx.controllerId(), false);
        };
    }

    /** Returns whether a trigger effect's intervening-if condition is met at trigger time. */
    public boolean isInterveningIfMet(GameData gameData, CardEffect effect, Permanent source,
                                     UUID controllerId) {
        if (!(effect instanceof ConditionalEffect conditional)) {
            return true;
        }
        return isMet(gameData, conditional.condition(), ConditionContext.forPermanent(source, controllerId));
    }

    /**
     * True if any opponent controls at least {@code minimumDifference} more creatures than the
     * controller (Avatar of Might's cast-cost reduction).
     */
    private boolean anyOpponentControlsAtLeastNMoreCreatures(GameData gameData, ConditionContext ctx, int minimumDifference) {
        UUID controllerId = ctx.controllerId();
        if (controllerId == null) return false;
        int yourCreatures = countCreaturesControlled(gameData, controllerId);
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(controllerId)) continue;
            if (countCreaturesControlled(gameData, candidateOpponentId) >= yourCreatures + minimumDifference) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if the controller has strictly more life than at least one opponent
     * (Feudkiller's Verdict).
     */
    private boolean controllerHasMoreLifeThanAnOpponent(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        int yourLife = gameData.getLife(controllerId);
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(controllerId)) continue;
            if (yourLife > gameData.getLife(candidateOpponentId)) {
                return true;
            }
        }
        return false;
    }

    private boolean anOpponentHasMoreLifeThanController(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        int yourLife = gameData.getLife(controllerId);
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(controllerId)) continue;
            if (gameData.getLife(candidateOpponentId) > yourLife) {
                return true;
            }
        }
        return false;
    }

    private boolean anOpponentHasMoreCardsInHandThanController(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        int yourHandSize = countCardsInHand(gameData, controllerId);
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(controllerId)) continue;
            if (countCardsInHand(gameData, candidateOpponentId) > yourHandSize) {
                return true;
            }
        }
        return false;
    }

    private boolean controllerHasMoreCardsInHandThanEachOpponent(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        int controllerHandSize = countCardsInHand(gameData, controllerId);
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (!candidateOpponentId.equals(controllerId)
                    && controllerHandSize <= countCardsInHand(gameData, candidateOpponentId)) {
                return false;
            }
        }
        return true;
    }

    /** Sum of the effective power of every creature the given player controls. */
    private int controlledCreaturesTotalPower(GameData gameData, ConditionContext ctx) {
        UUID controllerId = ctx.controllerId();
        if (controllerId == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;
        int totalPower = 0;
        for (Permanent permanent : battlefield) {
            if (isCreatureForCondition(gameData, permanent)) {
                totalPower += gameQueryService.getEffectivePower(gameData, permanent);
            }
        }
        return totalPower;
    }

    /** Sum of the effective toughness of every creature the given player controls. */
    private int controlledCreaturesTotalToughness(GameData gameData, ConditionContext ctx) {
        UUID controllerId = ctx.controllerId();
        if (controllerId == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;
        int totalToughness = 0;
        for (Permanent permanent : battlefield) {
            if (isCreatureForCondition(gameData, permanent)) {
                totalToughness += gameQueryService.getEffectiveToughness(gameData, permanent);
            }
        }
        return totalToughness;
    }

    /**
     * Whether the controller controls a creature tied for (or holding) the greatest effective power
     * among all creatures on the battlefield. False when they control no creature at all.
     */
    private boolean controlsCreatureWithGreatestPower(GameData gameData, ConditionContext ctx) {
        UUID controllerId = ctx.controllerId();
        if (controllerId == null) return false;
        Integer bestControlled = null;
        Integer bestOverall = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (!isCreatureForCondition(gameData, permanent)) continue;
                int power = gameQueryService.getEffectivePower(gameData, permanent);
                if (bestOverall == null || power > bestOverall) bestOverall = power;
                if (playerId.equals(controllerId) && (bestControlled == null || power > bestControlled)) {
                    bestControlled = power;
                }
            }
        }
        return bestControlled != null && bestControlled.equals(bestOverall);
    }

    /**
     * Whether every creature tied for the greatest effective power on the battlefield is controlled
     * by the condition's controller. Vacuously true with no creatures anywhere.
     */
    private boolean controlsEachCreatureWithGreatestPower(GameData gameData, ConditionContext ctx) {
        UUID controllerId = ctx.controllerId();
        if (controllerId == null) return false;
        Integer bestOverall = null;
        boolean opponentHoldsBest = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (!isCreatureForCondition(gameData, permanent)) continue;
                int power = gameQueryService.getEffectivePower(gameData, permanent);
                if (bestOverall == null || power > bestOverall) {
                    bestOverall = power;
                    opponentHoldsBest = !playerId.equals(controllerId);
                } else if (power == bestOverall && !playerId.equals(controllerId)) {
                    opponentHoldsBest = true;
                }
            }
        }
        return !opponentHoldsBest;
    }

    private int countCreaturesControlled(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (isCreatureForCondition(gameData, permanent)) {
                count++;
            }
        }
        return count;
    }

    private boolean allNonartifactCreaturesShareColor(GameData gameData) {
        Set<CardColor> sharedColors = EnumSet.allOf(CardColor.class);
        boolean foundCreature = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (!isCreatureForCondition(gameData, permanent)
                        || isArtifactForCondition(gameData, permanent)) {
                    continue;
                }
                foundCreature = true;
                Set<CardColor> colors = effectiveColorsForCondition(gameData, permanent);
                if (colors.isEmpty()) return false;
                sharedColors.retainAll(colors);
                if (sharedColors.isEmpty()) return false;
            }
        }
        return foundCreature && !sharedColors.isEmpty();
    }

    private boolean isArtifactForCondition(GameData gameData, Permanent permanent) {
        return GameQueryService.isStaticEvaluationActive()
                ? predicateEvaluationService.matchesStaticLeaf(permanent, ARTIFACT_FILTER)
                : gameQueryService.isArtifact(gameData, permanent);
    }

    private Set<CardColor> effectiveColorsForCondition(GameData gameData, Permanent permanent) {
        if (GameQueryService.isStaticEvaluationActive()) {
            CharacteristicState state = LayerSystemService.activeStateFor(permanent.getId());
            if (state != null) return state.getColors();
        }
        return gameQueryService.getEffectiveColors(gameData, permanent);
    }

    private boolean controlsMoreCreaturesThanOpponent(GameData gameData, ConditionContext ctx) {
        UUID controllerId = ctx.controllerId();
        if (controllerId == null) return false;
        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        return countCreaturesControlled(gameData, controllerId)
                > countCreaturesControlled(gameData, opponentId);
    }

    private boolean controllerControlsMoreCreaturesThanTargetSpellController(
            GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null || ctx.targetId() == null) return false;
        com.github.laxika.magicalvibes.model.StackEntry targetSpell = gameData.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(ctx.targetId()))
                .findFirst()
                .orElse(null);
        if (targetSpell == null) return false;
        return countCreaturesControlled(gameData, ctx.controllerId())
                > countCreaturesControlled(gameData, targetSpell.getControllerId());
    }

    private boolean targetSpellSharesColorWithControlledCreature(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null || ctx.targetId() == null) return false;
        com.github.laxika.magicalvibes.model.StackEntry targetSpell = gameData.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(ctx.targetId()))
                .findFirst()
                .orElse(null);
        if (targetSpell == null) return false;

        Set<CardColor> spellColors = gameQueryService.getEffectiveCardColors(gameData, targetSpell.getCard());
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (spellColors.isEmpty() || battlefield == null) return false;

        return battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(permanent -> gameQueryService.getEffectiveColors(gameData, permanent))
                .anyMatch(creatureColors -> creatureColors.stream().anyMatch(spellColors::contains));
    }

    private boolean aPlayerControlsMoreCreaturesThanEachOtherPlayer(GameData gameData) {
        int highestCreatureCount = -1;
        int playersWithMostCreatures = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            int creatureCount = countCreaturesControlled(gameData, playerId);
            if (creatureCount > highestCreatureCount) {
                highestCreatureCount = creatureCount;
                playersWithMostCreatures = 1;
            } else if (creatureCount == highestCreatureCount) {
                playersWithMostCreatures++;
            }
        }
        return playersWithMostCreatures == 1;
    }

    private boolean aPlayerHasMoreCardsInHandThanEachOtherPlayer(GameData gameData) {
        int highestHandSize = -1;
        int playersWithMostCards = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            int handSize = countCardsInHand(gameData, playerId);
            if (handSize > highestHandSize) {
                highestHandSize = handSize;
                playersWithMostCards = 1;
            } else if (handSize == highestHandSize) {
                playersWithMostCards++;
            }
        }
        return playersWithMostCards == 1;
    }

    /**
     * Creature check for condition evaluation. Inside static bonus computation
     * ({@link GameQueryService#isStaticEvaluationActive()}) the fully layered
     * {@link GameQueryService#isCreature} would recurse back into static assembly, so the
     * recursion-safe static filter matcher is used instead — the same contract
     * {@link #matchesPermanent} follows for permanent predicates.
     */
    private boolean isCreatureForCondition(GameData gameData, Permanent permanent) {
        return GameQueryService.isStaticEvaluationActive()
                ? predicateEvaluationService.matchesStaticLeaf(permanent, CREATURE_FILTER)
                : gameQueryService.isCreature(gameData, permanent);
    }

    /** True if any opponent controls strictly more lands than the controller (Gift of Estates). */
    /**
     * Resolves the source permanent from the context, preferring the permanent handed in by
     * the call site and falling back to a battlefield lookup by id.
     */
    private Permanent sourcePermanent(GameData gameData, ConditionContext ctx) {
        if (ctx.sourcePermanentId() != null) {
            Permanent live = gameQueryService.findPermanentById(gameData, ctx.sourcePermanentId());
            if (live != null) return live;
        }
        return ctx.sourcePermanent();
    }

    /**
     * Matches a permanent against a predicate. Static bonus computation must use the
     * recursion-safe static filter matcher (the general matcher can re-enter static bonus
     * computation via e.g. creature checks); every other context uses the general matcher.
     */
    private boolean matchesPermanent(GameData gameData, Permanent permanent, PermanentPredicate filter,
                                     ConditionContext ctx) {
        // Pass source card/controller so ownership and "is source" predicates work in conditions
        // (e.g. Gisela's "own and control Gisela and Bruna" intervening-if).
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceControllerId(ctx.controllerId());
        if (ctx.sourceCard() != null) {
            filterContext = filterContext.withSourceCardId(ctx.sourceCard().getId());
        } else if (ctx.sourcePermanent() != null) {
            filterContext = filterContext.withSourceCardId(ctx.sourcePermanent().getOriginalCard().getId());
        }
        if (GameQueryService.isStaticEvaluationActive()) {
            return predicateEvaluationService.matchesStaticFilter(permanent, filter, filterContext);
        }
        return predicateEvaluationService.matchesPermanentPredicate(permanent, filter, filterContext);
    }

    /** Returns {@code true} if the given permanent is the condition's own source. */
    private boolean isSource(Permanent permanent, ConditionContext ctx) {
        return (ctx.sourcePermanentId() != null && permanent.getId().equals(ctx.sourcePermanentId()))
                || (ctx.sourceCard() != null && permanent.getCard() == ctx.sourceCard());
    }

    /** True when the stack entry's source card object is still in its controller's command zone. */
    private boolean isSourceCardInCommandZone(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null || ctx.sourceCard() == null) return false;
        List<Card> commandZone = gameData.playerCommandZones.get(ctx.controllerId());
        return commandZone != null && commandZone.contains(ctx.sourceCard());
    }

    /** True when the stack entry's source card object is still in its controller's graveyard. */
    private boolean isSourceCardInGraveyard(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null || ctx.sourceCard() == null) return false;
        List<Card> graveyard = gameData.playerGraveyards.get(ctx.controllerId());
        return graveyard != null && graveyard.contains(ctx.sourceCard());
    }

    /** True when the source card is still exiled with a positive time-counter entry. */
    private boolean isSourceCardSuspended(GameData gameData, ConditionContext ctx) {
        if (ctx.sourceCard() == null) return false;
        UUID cardId = ctx.sourceCard().getId();
        Integer timeCounters = gameData.exiledCardTimeCounters.get(cardId);
        return timeCounters != null && timeCounters > 0 && gameData.findExiledCard(cardId) != null;
    }

    /**
     * Metalcraft: three or more controlled artifacts. Static bonus computation cannot call the
     * general artifact check — it re-enters static bonus computation — so it counts through
     * {@link PredicateEvaluationService#matchesStaticLeaf}, which reads the layer-4 state
     * already computed for the in-flight pass (CR 613.1d). Counting printed types only made an
     * Equipment's "equipped creature is an artifact" invisible here while the layered
     * {@code isCreature} path saw it, animating a Rusted Relic as a 0/0 that then died to
     * CR 704.5f.
     */
    private boolean isMetalcraftMet(GameData gameData, ConditionContext ctx) {
        // No controller (e.g. static self bonus computed while the permanent is being removed):
        // controller-dependent conditions are simply not met
        if (ctx.controllerId() == null) return false;
        if (!GameQueryService.isStaticEvaluationActive()) {
            return gameQueryService.isMetalcraftMet(gameData, ctx.controllerId());
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        return battlefield.stream()
                .filter(permanent -> predicateEvaluationService.matchesStaticLeaf(permanent, ARTIFACT_FILTER))
                .count() >= 3;
    }

    private boolean basicLandTypesAmongControlledLandsAtLeast(GameData gameData,
                                                               ConditionContext ctx,
                                                               int threshold) {
        if (ctx.controllerId() == null) return false;
        Set<CardSubtype> basicLandTypes = EnumSet.noneOf(CardSubtype.class);
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().hasType(CardType.LAND)) {
                basicLandTypes.addAll(GameQueryService.isStaticEvaluationActive()
                        ? gameQueryService.basicLandTypesForStaticEvaluation(gameData, permanent)
                        : gameQueryService.effectiveBasicLandTypes(gameData, permanent));
            }
        }
        return basicLandTypes.size() >= threshold;
    }

    /** Coven: three or more controlled creatures with different effective powers. */
    private boolean isCovenMet(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null) return false;
        if (!GameQueryService.isStaticEvaluationActive()) {
            return gameQueryService.isCovenMet(gameData, ctx.controllerId());
        }
        Set<UUID> evaluatingControllers = COVEN_EVALUATION.get();
        if (!evaluatingControllers.add(ctx.controllerId())) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
            if (battlefield == null) return false;
            return battlefield.stream()
                    .filter(permanent -> predicateEvaluationService.matchesStaticLeaf(permanent, CREATURE_FILTER))
                    .map(gameQueryService::powerFromStaticBoard)
                    .distinct()
                    .limit(3)
                    .count() >= 3;
        }
        try {
            return gameQueryService.isCovenMet(gameData, ctx.controllerId());
        } finally {
            evaluatingControllers.remove(ctx.controllerId());
            if (evaluatingControllers.isEmpty()) {
                COVEN_EVALUATION.remove();
            }
        }
    }

    /**
     * Delirium: four or more distinct card types among non-token cards in the controller's
     * graveyard (mirrors {@code CardTypesAmongCardsInGraveyard} with CONTROLLER scope).
     */
    private boolean isDeliriumMet(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null) return false;
        List<Card> graveyard = gameData.playerGraveyards.get(ctx.controllerId());
        if (graveyard == null || graveyard.isEmpty()) return false;
        Set<CardType> found = EnumSet.noneOf(CardType.class);
        for (Card card : graveyard) {
            if (card.isToken()) continue;
            if (card.getType() != null) {
                found.add(card.getType());
            }
            found.addAll(card.getAdditionalTypes());
        }
        return found.size() >= 4;
    }

    private boolean devotionToColorAtLeast(GameData gameData, ConditionContext ctx,
                                           DevotionToColorAtLeast condition) {
        if (ctx.controllerId() == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        int devotion = 0;
        for (Permanent permanent : battlefield) {
            var manaCost = permanent.getCard().getParsedManaCost();
            if (manaCost != null) {
                devotion += manaCost.countColorSymbols(condition.color());
            }
        }
        return devotion >= condition.threshold();
    }

    private boolean devotionToColorsAtLeast(GameData gameData, ConditionContext ctx,
                                            DevotionToColorsAtLeast condition) {
        if (ctx.controllerId() == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        int devotion = 0;
        for (Permanent permanent : battlefield) {
            var manaCost = permanent.getCard().getParsedManaCost();
            if (manaCost != null) {
                devotion += manaCost.countSymbolsOfAnyColor(condition.colors());
            }
        }
        return devotion >= condition.threshold();
    }

    private boolean isSourceEquipped(GameData gameData, ConditionContext ctx) {
        UUID sourcePermanentId = ctx.sourcePermanentId();
        if (sourcePermanentId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                        && sourcePermanentId.equals(perm.getAttachedTo())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean equippedCreatureDidntDealCombatDamageToCreatureThisTurn(GameData gameData,
                                                                             ConditionContext ctx) {
        Permanent equipment = ctx.sourcePermanent();
        if (equipment == null && ctx.sourcePermanentId() != null) {
            equipment = gameQueryService.findPermanentById(gameData, ctx.sourcePermanentId());
        }
        if (equipment == null || !equipment.isAttached()) return false;
        UUID equippedCreatureId = equipment.getAttachedTo();
        return equippedCreatureId != null
                && gameQueryService.findPermanentById(gameData, equippedCreatureId) != null
                && !gameData.combatDamageSourcesThatDealtToCreaturesThisTurn.contains(equippedCreatureId);
    }

    private boolean isSourceEnchanted(GameData gameData, ConditionContext ctx) {
        UUID sourcePermanentId = ctx.sourcePermanentId();
        if (sourcePermanentId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.getCard().getSubtypes().contains(CardSubtype.AURA)
                        && sourcePermanentId.equals(perm.getAttachedTo())) {
                    return true;
                }
            }
        }
        return false;
    }

    private int countAurasAttachedToSource(GameData gameData, ConditionContext ctx) {
        UUID sourcePermanentId = ctx.sourcePermanentId();
        if (sourcePermanentId == null) return 0;
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.getCard().getSubtypes().contains(CardSubtype.AURA)
                        && sourcePermanentId.equals(perm.getAttachedTo())) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean controlsMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        return battlefield.stream().anyMatch(p -> matchesPermanent(gameData, p, filter, ctx));
    }

    private boolean targetPlayerControlsMatchingPermanent(GameData gameData, ConditionContext ctx,
                                                          PermanentPredicate filter) {
        UUID targetPlayerId = ctx.targetId();
        if (targetPlayerId == null) return false;

        if (!gameData.playerIds.contains(targetPlayerId)) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetPlayerId);
            if (target == null) return false;
            targetPlayerId = gameQueryService.findPermanentController(gameData, target.getId());
        }

        if (targetPlayerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return false;
        return battlefield.stream().anyMatch(p -> matchesPermanent(gameData, p, filter, ctx));
    }

    private boolean activePlayerControlsMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (gameData.activePlayerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
        if (battlefield == null) return false;
        return battlefield.stream().anyMatch(p -> matchesPermanent(gameData, p, filter, ctx));
    }

    private boolean activePlayerControlsMoreLandsThanEachOtherPlayer(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)
                    && !gameQueryService.controlsMoreLandsThan(gameData, activePlayerId, playerId)) {
                return false;
            }
        }
        return true;
    }

    private boolean controllerControlsMorePermanentsThanEachOtherPlayer(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        int controllerCount = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).size();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)
                    && controllerCount <= gameData.playerBattlefields.getOrDefault(playerId, List.of()).size()) {
                return false;
            }
        }
        return true;
    }

    private boolean controlsAnotherMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        return battlefield.stream()
                .anyMatch(p -> !isSource(p, ctx) && matchesPermanent(gameData, p, filter, ctx));
    }

    private boolean opponentControlsMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            if (battlefield.stream().anyMatch(p -> matchesPermanent(gameData, p, filter, ctx))) {
                return true;
            }
        }
        return false;
    }

    private boolean opponentControlsAtLeastMatchingPermanents(GameData gameData, ConditionContext ctx,
                                                               int minCount, PermanentPredicate filter) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            long count = battlefield.stream()
                    .filter(p -> matchesPermanent(gameData, p, filter, ctx))
                    .count();
            if (count >= minCount) return true;
        }
        return false;
    }

    private boolean opponentCastMatchingSpellThisTurn(GameData gameData, ConditionContext ctx, CardPredicate filter) {
        if (ctx.controllerId() == null) {
            return false;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            if (gameQueryService.hasControllerCastAnotherSpellThisTurn(gameData, playerId, null, filter)) {
                return true;
            }
        }
        return false;
    }

    private boolean opponentCastThreeOrMoreSpellsThisTurn(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null) return false;
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(ctx.controllerId()))
                .anyMatch(playerId -> gameData.getSpellsCastThisTurnCount(playerId) >= 3);
    }

    private boolean opponentPutThreeOrMoreCardsIntoGraveyardThisTurn(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null) return false;
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(ctx.controllerId()))
                .anyMatch(playerId -> gameData.cardsPutIntoGraveyardFromAnywhereThisTurn
                        .getOrDefault(playerId, Set.of()).size() >= 3);
    }

    private boolean opponentPermanentEnteredThisTurn(GameData gameData, ConditionContext ctx,
                                                       OpponentPermanentEnteredThisTurn condition) {
        if (ctx.controllerId() == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ctx.controllerId())) continue;
            long count = gameData.permanentsEnteredBattlefieldThisTurn
                    .getOrDefault(playerId, List.of())
                    .stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, condition.predicate(), null))
                    .count();
            if (count >= condition.minCount()) return true;
        }
        return false;
    }

    private boolean anyPlayerControlsMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        Boolean layeredResult = gameQueryService.withQueryScope(gameData,
                () -> anyPlayerControlsMatchingPermanentUnscoped(gameData, ctx, filter));
        return layeredResult != null
                ? layeredResult
                : anyPlayerControlsMatchingPermanentUnscoped(gameData, ctx, filter);
    }

    private boolean anyPlayerControlsMatchingPermanentUnscoped(GameData gameData, ConditionContext ctx,
                                                               PermanentPredicate filter) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            if (battlefield.stream().anyMatch(p -> matchesPermanent(gameData, p, filter, ctx))) {
                return true;
            }
        }
        return false;
    }

    private long countMatchingPermanentsOnBattlefield(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        Long layeredResult = gameQueryService.withQueryScope(gameData,
                () -> countMatchingPermanentsOnBattlefieldUnscoped(gameData, ctx, filter));
        return layeredResult != null
                ? layeredResult
                : countMatchingPermanentsOnBattlefieldUnscoped(gameData, ctx, filter);
    }

    private long countMatchingPermanentsOnBattlefieldUnscoped(GameData gameData, ConditionContext ctx,
                                                              PermanentPredicate filter) {
        long count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            count += battlefield.stream().filter(p -> matchesPermanent(gameData, p, filter, ctx)).count();
        }
        return count;
    }

    private boolean defendingPlayerControlsMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return false;
        UUID defendingPlayerId = gameQueryService.getOpponentId(gameData, ctx.controllerId());
        if (defendingPlayerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(defendingPlayerId);
        if (battlefield == null) return false;
        return battlefield.stream().anyMatch(p -> matchesPermanent(gameData, p, filter, ctx));
    }

    private long countControlledMatchingPermanents(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        return battlefield.stream().filter(p -> matchesPermanent(gameData, p, filter, ctx)).count();
    }

    private boolean controlsCreaturesSharingCreatureType(GameData gameData, ConditionContext ctx, int minimum) {
        if (minimum <= 0) return true;
        if (ctx.controllerId() == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;

        Set<CardSubtype> allCreatureTypes = EnumSet.noneOf(CardSubtype.class);
        for (CardSubtype subtype : CardSubtype.values()) {
            if (gameQueryService.isCreatureSubtype(subtype)) {
                allCreatureTypes.add(subtype);
            }
        }

        Map<CardSubtype, Integer> countsByType = new HashMap<>();
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) continue;
            Set<CardSubtype> creatureTypes = gameQueryService.effectiveCreatureSubtypes(gameData, permanent);
            if (gameQueryService.hasKeyword(gameData, permanent, Keyword.CHANGELING)) {
                creatureTypes = allCreatureTypes;
            }
            for (CardSubtype creatureType : creatureTypes) {
                countsByType.merge(creatureType, 1, Integer::sum);
            }
        }
        return countsByType.values().stream().anyMatch(count -> count >= minimum);
    }

    /**
     * Counts the DISTINCT names among the controller's permanents matching the filter — "four or
     * more Demons with different names" is satisfied by four differently named Demons, not by four
     * copies of one Demon.
     */
    private long countControlledMatchingPermanentNames(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        return battlefield.stream()
                .filter(p -> matchesPermanent(gameData, p, filter, ctx))
                .map(p -> p.getCard().getName())
                .distinct()
                .count();
    }

    private long controlledCreatureCounterKinds(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;

        Set<CounterType> counterKinds = EnumSet.noneOf(CounterType.class);
        for (Permanent permanent : battlefield) {
            boolean creature = GameQueryService.isStaticEvaluationActive()
                    ? predicateEvaluationService.matchesStaticLeaf(permanent, CREATURE_FILTER)
                    : gameQueryService.isCreature(gameData, permanent);
            if (creature) {
                permanent.getCounters().forEach((counterType, count) -> {
                    if (count > 0) counterKinds.add(counterType);
                });
            }
        }
        return counterKinds.size();
    }

    private long countOtherControlledMatchingPermanents(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        return battlefield.stream()
                .filter(p -> !isSource(p, ctx) && matchesPermanent(gameData, p, filter, ctx))
                .count();
    }

    private long countOtherThanTriggeringControlledMatchingPermanents(GameData gameData, ConditionContext ctx,
                                                                       PermanentPredicate filter) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        return battlefield.stream()
                .filter(p -> !isSource(p, ctx)
                        && (ctx.triggeringPermanentId() == null
                        || !p.getId().equals(ctx.triggeringPermanentId()))
                        && matchesPermanent(gameData, p, filter, ctx))
                .count();
    }

    private boolean noOtherMatchingPermanent(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return true;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return true;
        return battlefield.stream()
                .noneMatch(p -> !isSource(p, ctx) && matchesPermanent(gameData, p, filter, ctx));
    }

    /**
     * True when the controller of the permanent the source is attached to controls no other
     * matching permanent (Predator's Gambit). Not met while the source isn't attached.
     */
    private boolean attachedPermanentControllerControlsNoOther(GameData gameData, ConditionContext ctx,
                                                               PermanentPredicate filter) {
        Permanent source = sourcePermanent(gameData, ctx);
        if (source == null || !source.isAttached()) return false;
        UUID attachedToId = source.getAttachedTo();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            if (battlefield.stream().noneMatch(p -> p.getId().equals(attachedToId))) continue;
            return battlefield.stream()
                    .noneMatch(p -> !p.getId().equals(attachedToId) && matchesPermanent(gameData, p, filter, ctx));
        }
        return false;
    }

    private int countMatchingGraveyardCards(GameData gameData, ConditionContext ctx, GraveyardCardThreshold c) {
        if (ctx.controllerId() == null) return 0;
        List<Card> graveyard = gameData.playerGraveyards.get(ctx.controllerId());
        if (graveyard == null) return 0;
        int count = 0;
        for (Card card : graveyard) {
            if (card.isToken()) continue;
            boolean matches = GameQueryService.isStaticEvaluationActive()
                    ? predicateEvaluationService.matchesCardPredicate(card, c.filter(), null)
                    : c.filter() == null || predicateEvaluationService.matchesCardPredicate(card, c.filter(),
                            null, gameData, ctx.controllerId());
            if (matches) count++;
        }
        return count;
    }

    private boolean targetToughnessAtMostControllerGraveyardCount(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null || ctx.targetId() == null) return false;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
        if (target == null) return false;
        List<Card> graveyard = gameData.playerGraveyards.get(ctx.controllerId());
        int graveyardSize = graveyard == null ? 0 : (int) graveyard.stream().filter(card -> !card.isToken()).count();
        return gameQueryService.getEffectiveToughness(gameData, target) <= graveyardSize;
    }

    private boolean targetManaValueAtMostControllerGraveyardCount(GameData gameData, ConditionContext ctx) {
        if (ctx.controllerId() == null || ctx.targetId() == null) return false;
        Permanent target = gameQueryService.findPermanentById(gameData, ctx.targetId());
        if (target == null) return false;
        List<Card> graveyard = gameData.playerGraveyards.get(ctx.controllerId());
        int graveyardSize = graveyard == null ? 0 : (int) graveyard.stream().filter(card -> !card.isToken()).count();
        return target.getCard().getManaValue() <= graveyardSize;
    }

    /**
     * Counts cards matching the condition's filter positioned above the source card in its
     * controller's (ordered) graveyard. The graveyard is a list where later indices are higher
     * on the pile; "above" therefore means a strictly greater index than the source card.
     */
    private int countCardsAboveSelfInGraveyard(GameData gameData, ConditionContext ctx, CardsAboveSelfInGraveyard c) {
        if (ctx.controllerId() == null || ctx.sourceCard() == null) return 0;
        List<Card> graveyard = gameData.playerGraveyards.get(ctx.controllerId());
        if (graveyard == null) return 0;
        int selfIndex = -1;
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getId().equals(ctx.sourceCard().getId())) {
                selfIndex = i;
                break;
            }
        }
        if (selfIndex < 0) return 0;
        int count = 0;
        for (int i = selfIndex + 1; i < graveyard.size(); i++) {
            Card above = graveyard.get(i);
            if (above.isToken()) continue;
            if (c.filter() == null
                    || predicateEvaluationService.matchesCardPredicate(above, c.filter(), null, gameData, ctx.controllerId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks whether the card immediately above the source card in its controller's (ordered)
     * graveyard matches the condition's filter. Later indices are higher on the pile, so "directly
     * above" is exactly index {@code selfIndex + 1}.
     */
    private boolean matchesCardDirectlyAboveSelfInGraveyard(GameData gameData, ConditionContext ctx,
                                                            CardDirectlyAboveSelfInGraveyard c) {
        if (ctx.controllerId() == null || ctx.sourceCard() == null) return false;
        List<Card> graveyard = gameData.playerGraveyards.get(ctx.controllerId());
        if (graveyard == null) return false;
        int selfIndex = -1;
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getId().equals(ctx.sourceCard().getId())) {
                selfIndex = i;
                break;
            }
        }
        if (selfIndex < 0 || selfIndex + 1 >= graveyard.size()) return false;
        Card above = graveyard.get(selfIndex + 1);
        return c.filter() == null
                || predicateEvaluationService.matchesCardPredicate(above, c.filter(), null, gameData, ctx.controllerId());
    }

    private boolean sourceDidntAttackThisTurn(GameData gameData, ConditionContext ctx) {
        if (ctx.sourcePermanentId() == null) return true;
        Permanent source = sourcePermanent(gameData, ctx);
        if (source == null) return false;
        return !source.isAttackedThisTurn();
    }

    /**
     * True if the creature the source Aura is attached to didn't attack this turn. Used by
     * Aggression's end-step trigger, which checks the enchanted creature rather than the source.
     */
    private boolean enchantedCreatureDidntAttackThisTurn(GameData gameData, ConditionContext ctx) {
        if (ctx.targetId() != null) {
            Permanent enchanted = gameQueryService.findPermanentById(gameData, ctx.targetId());
            return enchanted != null && !enchanted.isAttackedThisTurn();
        }
        Permanent aura = sourcePermanent(gameData, ctx);
        if (aura == null || !aura.isAttached()) return false;
        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) return false;
        return !enchanted.isAttackedThisTurn();
    }

    private boolean enchantedCreaturePowerAtLeast(GameData gameData, ConditionContext ctx, int threshold) {
        Permanent aura = sourcePermanent(gameData, ctx);
        if (aura == null || !aura.isAttached()) return false;
        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) return false;
        return gameQueryService.getEffectivePower(gameData, enchanted) >= threshold;
    }

    private boolean enchantedPermanentMatches(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        Permanent aura = sourcePermanent(gameData, ctx);
        if (aura == null || !aura.isAttached()) return false;
        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) return false;
        return matchesPermanent(gameData, enchanted, filter, ctx);
    }

    private long countAttackingCreatures(GameData gameData, UUID controllerId) {
        if (controllerId == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;
        return battlefield.stream().filter(Permanent::isAttacking).count();
    }

    private long countAttackingCreaturesOfSubtype(GameData gameData, UUID controllerId, CardSubtype subtype) {
        if (controllerId == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;
        return battlefield.stream()
                .filter(Permanent::isAttacking)
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> gameQueryService.effectiveCreatureSubtypes(gameData, p).contains(subtype)
                        || (gameQueryService.hasKeyword(gameData, p, com.github.laxika.magicalvibes.model.Keyword.CHANGELING)
                        && gameQueryService.isCreatureSubtype(subtype)))
                .count();
    }

    /**
     * True if any creature is attacking {@code playerId} themselves (the attack target is the
     * player, not a planeswalker they control). Qasali Ambusher's "a creature is attacking you".
     */
    private boolean creatureAttackingPlayer(GameData gameData, UUID playerId) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (perm.isAttacking() && playerId.equals(perm.getAttackTarget())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasMatchingAttacker(GameData gameData, ConditionContext ctx, PermanentPredicate predicate) {
        if (ctx.controllerId() == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;
        return battlefield.stream()
                .filter(Permanent::isAttacking)
                .anyMatch(p -> matchesPermanent(gameData, p, predicate, ctx));
    }

    private long countMatchingAttackers(GameData gameData, ConditionContext ctx,
                                        PermanentPredicate predicate) {
        if (ctx.controllerId() == null) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return 0;
        return battlefield.stream()
                .filter(Permanent::isAttacking)
                .filter(p -> matchesPermanent(gameData, p, predicate, ctx))
                .count();
    }

    private long countOpponentAttackersAtControllerOrPlaneswalkers(GameData gameData,
                                                                    ConditionContext ctx) {
        UUID controllerId = ctx.controllerId();
        UUID attackingPlayerId = ctx.targetId();
        if (controllerId == null || attackingPlayerId == null || controllerId.equals(attackingPlayerId)) {
            return 0;
        }

        List<Permanent> controlledPermanents = gameData.playerBattlefields.get(controllerId);
        List<Permanent> attackers = gameData.playerBattlefields.get(attackingPlayerId);
        if (controlledPermanents == null || attackers == null) return 0;

        Set<UUID> controlledPlaneswalkerIds = controlledPermanents.stream()
                .filter(permanent -> gameQueryService.isPlaneswalker(gameData, permanent))
                .map(Permanent::getId)
                .collect(java.util.stream.Collectors.toSet());

        return attackers.stream()
                .filter(Permanent::isAttacking)
                .filter(attacker -> controllerId.equals(attacker.getAttackTarget())
                        || controlledPlaneswalkerIds.contains(attacker.getAttackTarget()))
                .count();
    }

    private boolean opponentAttacksPlaneswalker(GameData gameData, ConditionContext ctx) {
        UUID controllerId = ctx.controllerId();
        UUID attackingPlayerId = ctx.targetId();
        if (controllerId == null || attackingPlayerId == null || controllerId.equals(attackingPlayerId)) {
            return false;
        }

        List<Permanent> controlledPermanents = gameData.playerBattlefields.get(controllerId);
        List<Permanent> attackers = gameData.playerBattlefields.get(attackingPlayerId);
        if (controlledPermanents == null || attackers == null) return false;

        Set<UUID> controlledPlaneswalkerIds = controlledPermanents.stream()
                .filter(permanent -> gameQueryService.isPlaneswalker(gameData, permanent))
                .map(Permanent::getId)
                .collect(java.util.stream.Collectors.toSet());

        return attackers.stream()
                .filter(Permanent::isAttacking)
                .anyMatch(attacker -> controlledPlaneswalkerIds.contains(attacker.getAttackTarget()));
    }

    private boolean controlsMatchingPermanentsWithSameName(GameData gameData, ConditionContext ctx,
                                                            int minimum, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return false;

        Map<String, Long> countsByName = new HashMap<>();
        for (Permanent permanent : battlefield) {
            if (matchesPermanent(gameData, permanent, filter, ctx)) {
                countsByName.merge(permanent.getCard().getName(), 1L, Long::sum);
            }
        }
        return countsByName.values().stream().anyMatch(count -> count >= minimum);
    }

    private boolean canBeholdSubtype(GameData gameData, UUID controllerId, CardSubtype subtype) {
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(controllerId, List.of());
        if (battlefield.stream().anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                gameData, permanent, new PermanentHasSubtypePredicate(subtype)))) {
            return true;
        }
        return gameData.playerHands.getOrDefault(controllerId, List.of()).stream()
                .anyMatch(card -> predicateEvaluationService.matchesCardPredicate(
                        card, new CardSubtypePredicate(subtype), card.getId()));
    }

    /**
     * True when every controlled permanent matching {@code filter} is attacking (vacuous if none match).
     */
    private boolean allMatchingCreaturesAttack(GameData gameData, ConditionContext ctx, PermanentPredicate filter) {
        if (ctx.controllerId() == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ctx.controllerId());
        if (battlefield == null) return true;
        return battlefield.stream()
                .filter(p -> matchesPermanent(gameData, p, filter, ctx))
                .allMatch(Permanent::isAttacking);
    }

    private boolean noPlayerHasCardsInHand(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand != null && !hand.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Total permanents on the battlefield across every player. */
    private int totalPermanentCount(GameData gameData) {
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                count += battlefield.size();
            }
        }
        return count;
    }

    private boolean noSpellsCastLastTurn(GameData gameData) {
        if (gameData.spellsCastLastTurn.isEmpty()) return true;
        return gameData.spellsCastLastTurn.values().stream().mapToInt(Integer::intValue).sum() == 0;
    }

    private boolean isDefendingPlayerPoisoned(GameData gameData, UUID attackingPlayerId) {
        if (attackingPlayerId == null) return false;
        UUID defendingPlayerId = gameQueryService.getOpponentId(gameData, attackingPlayerId);
        if (defendingPlayerId == null) return false;
        return gameData.playerPoisonCounters.getOrDefault(defendingPlayerId, 0) > 0;
    }

    private boolean isAnyOpponentHandEmpty(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnyOpponentPoisoned(GameData gameData, UUID controllerId, int minimumPoisonCounters) {
        if (controllerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)
                    && gameData.playerPoisonCounters.getOrDefault(playerId, 0) >= minimumPoisonCounters) {
                return true;
            }
        }
        return false;
    }

    private boolean wasAnyOpponentDealtDamageThisTurn(GameData gameData, UUID controllerId, int minimumAmount) {
        if (controllerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            int dealt = gameData.damageDealtToPlayersThisTurn.getOrDefault(playerId, 0);
            if (dealt >= Math.max(1, minimumAmount)) {
                return true;
            }
        }
        return false;
    }

    private boolean opponentDrewAtLeastCardsThisTurn(GameData gameData, UUID controllerId, int minimum) {
        if (controllerId == null) return false;
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .anyMatch(playerId -> gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0) >= minimum);
    }

    /**
     * True if the source permanent dealt combat damage to an opponent of its current controller this
     * turn (Whirling Dervish). Reads the per-source combat-damage-to-players tracking and treats any
     * damaged player other than the source's current controller as an opponent.
     */
    private boolean sourceDealtDamageToOpponentThisTurn(GameData gameData, ConditionContext ctx) {
        if (ctx.sourcePermanentId() == null || ctx.controllerId() == null) return false;
        Set<UUID> damagedPlayers = gameData.combatDamageToPlayersThisTurn.get(ctx.sourcePermanentId());
        if (damagedPlayers == null) return false;
        return damagedPlayers.stream().anyMatch(playerId -> !playerId.equals(ctx.controllerId()));
    }

    private boolean didAnyOpponentLoseLifeThisTurn(GameData gameData, UUID controllerId, int minimumAmount) {
        if (controllerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            int lost = gameData.lifeLostThisTurn.getOrDefault(playerId, 0);
            if (lost >= Math.max(1, minimumAmount)) {
                return true;
            }
        }
        return false;
    }

    private long countCreatureDamageSourcesToPlayer(GameData gameData, UUID playerId) {
        if (playerId == null) return 0;
        return gameData.creatureDamageToPlayersThisTurn.values().stream()
                .filter(players -> players.contains(playerId))
                .count();
    }

    private boolean didAnyOpponentGainLifeThisTurn(GameData gameData, UUID controllerId, int minimumAmount) {
        if (controllerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            int gained = gameData.getLifeGainedThisTurn(playerId);
            if (gained >= Math.max(1, minimumAmount)) {
                return true;
            }
        }
        return false;
    }

    private boolean didAnyPlayerLoseLifeThisTurn(GameData gameData, int minimumAmount) {
        int threshold = Math.max(1, minimumAmount);
        return gameData.orderedPlayerIds.stream()
                .anyMatch(playerId -> gameData.lifeLostThisTurn.getOrDefault(playerId, 0) >= threshold);
    }

    private int activationCountThisTurn(GameData gameData, ConditionContext ctx, int abilityIndex) {
        if (ctx.sourcePermanentId() == null) return 0;
        var perAbilityCounts = gameData.activatedAbilityUsesThisTurn.get(ctx.sourcePermanentId());
        if (perAbilityCounts == null) return 0;
        return perAbilityCounts.getOrDefault(abilityIndex, 0);
    }

    /**
     * "if this permanent entered the battlefield this turn" — the source's card is looked up in the
     * entered-this-turn record. The source may already have left the battlefield when the condition
     * is re-checked at resolution, so the card, not a live permanent, is what is matched.
     */
    private boolean sourceEnteredThisTurn(GameData gameData, ConditionContext ctx) {
        Permanent source = sourcePermanent(gameData, ctx);
        Card sourceCard = source != null ? source.getCard() : ctx.sourceCard();
        if (sourceCard == null) return false;
        return gameData.permanentsEnteredBattlefieldThisTurn.values().stream()
                .flatMap(List::stream)
                .anyMatch(card -> card == sourceCard);
    }

    private long countPermanentsEnteredThisTurn(GameData gameData, ConditionContext ctx, PermanentEnteredThisTurn c) {
        if (ctx.controllerId() == null) return 0;
        List<Card> entered = gameData.permanentsEnteredBattlefieldThisTurn
                .getOrDefault(ctx.controllerId(), List.of());
        return entered.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, c.predicate(), null))
                .count();
    }

    private boolean anotherPermanentEnteredThisTurn(
            GameData gameData, ConditionContext ctx, AnotherPermanentEnteredThisTurn condition) {
        if (ctx.controllerId() == null || ctx.sourceCard() == null) return false;
        UUID sourceCardId = ctx.sourceCard().getId();
        return gameData.permanentsEnteredBattlefieldThisTurn
                .getOrDefault(ctx.controllerId(), List.of())
                .stream()
                .filter(card -> !card.getId().equals(sourceCardId))
                .anyMatch(card -> predicateEvaluationService.matchesCardPredicate(
                        card, condition.predicate(), null));
    }

    private boolean anotherPermanentEnteredLastTurn(
            GameData gameData, ConditionContext ctx, AnotherPermanentEnteredLastTurn condition) {
        if (ctx.controllerId() == null || ctx.sourceCard() == null) return false;
        UUID sourceCardId = ctx.sourceCard().getId();
        return gameData.permanentsEnteredBattlefieldLastTurn
                .getOrDefault(ctx.controllerId(), List.of())
                .stream()
                .filter(card -> !card.getId().equals(sourceCardId))
                .anyMatch(card -> predicateEvaluationService.matchesCardPredicate(
                        card, condition.predicate(), null));
    }

    private boolean sourceEnteredBattlefieldThisTurn(GameData gameData, Permanent source) {
        return gameData.permanentsEnteredBattlefieldThisTurn.values().stream()
                .flatMap(List::stream)
                .anyMatch(card -> card.getId().equals(source.getCard().getId()));
    }

    private boolean sourceHasSubtype(GameData gameData, ConditionContext ctx, CardSubtype subtype) {
        Permanent source = sourcePermanent(gameData, ctx);
        if (source != null) {
            return source.getCard().getSubtypes().contains(subtype)
                    || source.getGrantedSubtypes().contains(subtype);
        }
        return ctx.sourceCard() != null && ctx.sourceCard().getSubtypes().contains(subtype);
    }

    private int countCardsInLibrary(GameData gameData, UUID controllerId) {
        if (controllerId == null) return 0;
        List<Card> deck = gameData.playerDecks.get(controllerId);
        return deck == null ? 0 : deck.size();
    }

    private boolean anyGraveyardAtLeast(GameData gameData, int threshold) {
        return gameData.playerGraveyards.values().stream().anyMatch(graveyard -> graveyard.size() >= threshold);
    }

    private int totalCreatureCardsInGraveyards(GameData gameData) {
        return gameData.playerGraveyards.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .filter(card -> !card.isToken() && card.hasType(CardType.CREATURE))
                .mapToInt(card -> 1)
                .sum();
    }

    private boolean anyOpponentGraveyardAtLeast(GameData gameData, UUID controllerId, int threshold) {
        if (controllerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard != null && graveyard.size() >= threshold) {
                return true;
            }
        }
        return false;
    }

    private boolean didAnyOpponentLoseLifeLastTurn(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            if (gameData.lifeLostLastTurn.getOrDefault(playerId, 0) > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean opponentOwnsCardInExile(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        return gameData.exiledCards.stream()
                .anyMatch(entry -> !controllerId.equals(entry.ownerId()));
    }

    private boolean anyLibraryAtMost(GameData gameData, int threshold) {
        return gameData.playerDecks.values().stream().anyMatch(deck -> deck.size() <= threshold);
    }

    private int countCardsInHand(GameData gameData, UUID controllerId) {
        if (controllerId == null) return 0;
        List<Card> hand = gameData.playerHands.get(controllerId);
        return hand == null ? 0 : hand.size();
    }

    private int countMatchingCardsInHand(GameData gameData, UUID controllerId,
                                         com.github.laxika.magicalvibes.model.filter.CardPredicate filter) {
        if (controllerId == null) return 0;
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null) return 0;
        return (int) hand.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, null, gameData, controllerId))
                .count();
    }

    private boolean isTopCardOfLibraryColor(GameData gameData, UUID controllerId, TopCardOfLibraryColor c) {
        if (controllerId == null) return false;
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) return false;
        return deck.getFirst().getColors().contains(c.color());
    }

    private boolean isTopCardOfLibraryType(GameData gameData, UUID controllerId, TopCardOfLibraryType c) {
        if (controllerId == null) return false;
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) return false;
        return deck.getFirst().hasType(c.cardType());
    }

    private boolean isTopCardOfLibraryMatchesPredicate(
            GameData gameData, UUID controllerId, TopCardOfLibraryMatchesPredicate c) {
        if (controllerId == null) return false;
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) return false;
        return predicateEvaluationService.matchesCardPredicate(
                deck.getFirst(), c.predicate(), null, gameData, controllerId);
    }

    private UUID resolveLibraryOwner(GameData gameData, ConditionContext ctx, LibraryOwner owner) {
        return switch (owner) {
            case CONTROLLER -> ctx.controllerId();
            case OPPONENT -> gameQueryService.getOpponentId(gameData, ctx.controllerId());
            case TARGET_PLAYER, ENCHANTED_PERMANENT_CONTROLLER -> ctx.targetId();
        };
    }

    /**
     * Soulbond self-ETB intervening-if: source is unpaired and controller controls another unpaired creature.
     */
    private boolean canSoulbond(GameData gameData, ConditionContext ctx) {
        Permanent source = sourcePermanent(gameData, ctx);
        if (source == null || source.getPairedWithId() != null) {
            return false;
        }
        if (!isCreatureForCondition(gameData, source)) {
            return false;
        }
        UUID controllerId = ctx.controllerId();
        if (controllerId == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent p : battlefield) {
            if (p.getId().equals(source.getId())) {
                continue;
            }
            if (p.getPairedWithId() == null && isCreatureForCondition(gameData, p)) {
                return true;
            }
        }
        return false;
    }

    private int countBlockersOfSource(GameData gameData, ConditionContext ctx) {
        if (ctx.sourcePermanentId() == null) return 0;
        final int[] blockerCount = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isBlocking() && permanent.getBlockingTargetIds().contains(ctx.sourcePermanentId())) {
                blockerCount[0]++;
            }
        });
        return blockerCount[0];
    }

    private boolean imprintedCardMatches(GameData gameData, ConditionContext ctx, ImprintedCardMatches condition) {
        Permanent source = sourcePermanent(gameData, ctx);
        if (source == null) return false;
        Card imprintedCard = gameData.getImprintedCard(source.getCard());
        boolean discardedCard = "discarded card".equals(condition.subject());
        return imprintedCard != null
                && (discardedCard || gameData.findExiledCard(imprintedCard.getId()) != null)
                && predicateEvaluationService.matchesCardPredicate(
                imprintedCard, condition.filter(), source.getCard().getId(), gameData, ctx.controllerId());
    }

    private boolean imprintedCardNameMatches(GameData gameData, ConditionContext ctx) {
        if (ctx.triggeringCard() == null) return false;
        Permanent source = sourcePermanent(gameData, ctx);
        if (source == null) return false;
        Card imprintedCard = gameData.getImprintedCard(source.getCard());
        return imprintedCard != null && imprintedCard.getName().equals(ctx.triggeringCard().getName());
    }
}
