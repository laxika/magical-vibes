package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreSourceAuraEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyCombatDamageFromTargetCreatureToOpponentsUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetCreatureFromTargetingSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GatherSpecimensEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerCreatureSpellsCantBeCounteredThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerSpellsCantBeCounteredByColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerSpellsCantBeCounteredThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromNamedPlanesToControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SenTripletsUpkeepEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LastKnownCardStats;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ChapterTrigger;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.DamageBonusGrant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GrantedAlternateCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.SaturatingMath;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCantSacrificeTargetThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PayBlackManaWithLifeEffect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetSpellDamagePreventionShield;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.Freerunning;
import com.github.laxika.magicalvibes.model.effect.ActivateCreatureAbilitiesAsThoughHasteEffect;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilityTimingEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalSpellCopyEffect;
import com.github.laxika.magicalvibes.model.effect.AllCardsAreColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.AllPermanentsGainChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.AllDamageDealtWithWitherEffect;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.AllowExtraBoastActivationEffect;
import com.github.laxika.magicalvibes.model.effect.AllowExtraExhaustActivationEffect;
import com.github.laxika.magicalvibes.model.effect.AllowExtraLoyaltyActivationEffect;
import com.github.laxika.magicalvibes.model.effect.AllowExtraPowerUpActivationEffect;
import com.github.laxika.magicalvibes.model.effect.AllowLoyaltyActivationAtInstantSpeedEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateControlledEnchantmentsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedPermanentRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageAssignmentEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageAssignmentMode;
import com.github.laxika.magicalvibes.model.effect.AttackCostEffect;
import com.github.laxika.magicalvibes.model.effect.AttackWithoutTappingPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.BandsWithOtherEffect;
import com.github.laxika.magicalvibes.model.effect.BlockCostEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.BlockabilityRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CombatAttackRequirementEffect;
import com.github.laxika.magicalvibes.model.effect.GoadStatusEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.BoostControllerDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LethalDamageModifierEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeRestOfGameEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantLoseLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RequireFlagbearerTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessBoostEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeControlledByOtherPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeEnchantedByOtherAurasEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeEquippedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBecomeSuspectedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBecomeUntappedEffect;
import com.github.laxika.magicalvibes.model.effect.StunCountersCantBeRemovedFromOpponentsPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveOrGainKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CantWinGameEffect;
import com.github.laxika.magicalvibes.model.effect.NoncombatDamageToOpponentCreaturesAsMinusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsAndAbilitiesCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;
import com.github.laxika.magicalvibes.model.effect.LifeFloorCondition;
import com.github.laxika.magicalvibes.model.effect.DamageDealtAsInfectBelowZeroLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDoesNotCauseLifeLossEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourcesOfColorsAreColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerHasProtectionEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceSpellDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceDamageAboveThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.HalveAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.OjerAxonilDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SpendWhiteManaAsAnyColorEffect;
import com.github.laxika.magicalvibes.model.effect.SpendWhiteManaAsRedEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorForActivatedAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantActivateAbilitiesOfGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCycleCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CardsCantEnterBattlefieldFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.JohanCombatEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentLockEffect;
import com.github.laxika.magicalvibes.model.effect.LifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.LifePaymentReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentLifeLossReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceControlledCounterWithExileAndPlayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenPlayersDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.effect.CombatTaxKind;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesMatchingCantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledNonlandPermanentsAreColorEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerRecipientDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CounterLimitEffect;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CounterReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CountersCantBePlacedEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureEnteringDontCauseTriggersEffect;
import com.github.laxika.magicalvibes.model.effect.CrewAndSaddlePowerModifierEffect;
import com.github.laxika.magicalvibes.model.effect.DamagePreventionBySelfEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCombatDamageToCreaturesThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromTargetPermanentThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleEquippedCreatureCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.ETBDoubleTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EchoCostAlternativeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentAbilityLockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCombatTaxEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentToughnessBecomesLoyaltyEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPlayerCantActivateNonManaNonLoyaltyAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.BlitzGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.EvokeGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.FreerunningGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalBlockLifeCostEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeToOwnNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerFlagbearerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDeathtouchToControllerSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentSupertypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.LoseAllLandTypesEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.GrantSplitSecondToControllerSpellsUsingArtifactManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardAbilityGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardsCantBeTargetedEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardsLoseAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardStaticEffect;
import com.github.laxika.magicalvibes.model.effect.WrappedStackStaticEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardSubtypeGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.HandAbilityGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreOpponentHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtFaceDownCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MadnessGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllCreaturesUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.ManaReflectionEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsCantActivateTapAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.MayVoteAdditionalTimeEffect;
import com.github.laxika.magicalvibes.model.effect.MeliraPoisonReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MiracleGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.HalveTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentEffectsCantCauseDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentEffectsCantCauseSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentLifeGainBecomesLifeLossEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeredAbilitiesCantCauseSacrificeOrExileCreatureTokensEffect;
import com.github.laxika.magicalvibes.model.effect.TheValeyardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentRecipientDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantTargetLandsEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantVentureIntoDungeonMoreThanOnceEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OwnCardTypeGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.OwnCreatureSubtypeGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.OwnLandSubtypeGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.OwnLandSupertypeGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.OwnEffectsCantAffectSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkerLoyaltyAbilitiesCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerCantGetPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCounterReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageDealtByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.PreventArtifactDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromDesertsToSelfAndBandedCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventOpponentCreatureWardTriggersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventTransformEffect;
import com.github.laxika.magicalvibes.model.effect.PreventionScope;
import com.github.laxika.magicalvibes.model.effect.ProliferateReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromNonSubtypeCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ProwlGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.RainOfGoreEffect;
import com.github.laxika.magicalvibes.model.effect.RadiationLifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToBlockEffect;
import com.github.laxika.magicalvibes.model.effect.SelfAllZoneSubtypeGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.SelfOutsideBattlefieldCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureOutsideBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.SharedColorDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.SourceDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastingAbilityGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetColorMode;
import com.github.laxika.magicalvibes.model.effect.TargetedSpellDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingSourceKind;
import com.github.laxika.magicalvibes.model.effect.TokenCreationReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TwistBasicLandManaColorsEffect;
import com.github.laxika.magicalvibes.model.effect.WallOnlyTargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.layer.ModifierLine;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.CreatureCountSupport;
import com.github.laxika.magicalvibes.service.effect.GrantedEffectAttribution;
import com.github.laxika.magicalvibes.service.effect.LandPlayPermissionService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.room.RoomNameSupport;
import com.github.laxika.magicalvibes.service.effect.LoyaltyDamageReplacementHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandler;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.TextChangeTransformer;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectConditionResolver;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggerCause;
import java.util.*;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.effect.GrantControllerAndPermanentsHexproofFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromEverythingUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantedCardAbilityEffect;
import java.util.Comparator;

/**
 * Read-only query service for inspecting game state. Provides methods for looking up permanents
 * and cards, evaluating predicates and filters, computing effective stats (including static bonuses
 * from other permanents, auras, emblems, and granted effects), and checking protection, keywords,
 * evasion, and other derived properties.
 *
 * <p>This service never mutates game state. All methods are safe to call from validation,
 * combat resolution, AI evaluation, and view-building code.
 */
@Component
@RequiredArgsConstructor
public class GameQueryService {

    public static final List<String> TEXT_CHANGE_COLOR_WORDS = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
    public static final List<String> TEXT_CHANGE_LAND_TYPES = List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
    private static final PermanentIsLandPredicate LAND_FILTER = new PermanentIsLandPredicate();
    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST,
            CardSubtype.MOUNTAIN,
            CardSubtype.ISLAND,
            CardSubtype.PLAINS,
            CardSubtype.SWAMP,
            CardSubtype.DESERT,
            CardSubtype.CAVE,
            CardSubtype.GATE,
            CardSubtype.LOCUS,
            CardSubtype.URZAS,
            CardSubtype.ULAMOGS,
            CardSubtype.MINE,
            CardSubtype.POWER_PLANT,
            CardSubtype.TOWER,
            CardSubtype.AURA,
            CardSubtype.ARCANE,
            CardSubtype.PLAN,
            CardSubtype.EQUIPMENT,
            CardSubtype.FORTIFICATION,
            CardSubtype.INCUBATOR,
            CardSubtype.MUTAGEN,
            CardSubtype.ASSASSIN_OR_FREERUNNING,
            CardSubtype.OUTLAW,
            CardSubtype.AJANI,
            CardSubtype.KOTH,
            CardSubtype.BOLAS,
            CardSubtype.BLOOD,
            CardSubtype.BOBBLEHEAD,
            CardSubtype.BOOK,
            CardSubtype.CLUE,
            CardSubtype.FOOD,
            CardSubtype.GOLD,
            CardSubtype.JUNK,
            CardSubtype.LANDER,
            CardSubtype.MAP,
            CardSubtype.POWERSTONE,
            CardSubtype.SPACECRAFT,
            CardSubtype.TREASURE,
            CardSubtype.VEHICLE,
            CardSubtype.CARTOUCHE,
            CardSubtype.CURSE,
            CardSubtype.ROLE,
            CardSubtype.ROOM,
            CardSubtype.RUNE,
            CardSubtype.SAGA,
            CardSubtype.SHARD,
            CardSubtype.SHRINE,
            CardSubtype.LESSON,
            CardSubtype.OMEN,
            CardSubtype.TRAP,
            CardSubtype.SIEGE,
            CardSubtype.PLANET,
            CardSubtype.MIRRODIN,
            CardSubtype.SERRAS_REALM
    );
    public static final List<String> TEXT_CHANGE_CREATURE_TYPES = Arrays.stream(CardSubtype.values())
            .filter(subtype -> !NON_CREATURE_SUBTYPES.contains(subtype))
            .map(CardSubtype::name)
            .toList();

    static {
        NON_CREATURE_SUBTYPES.addAll(CardSubtype.landTypes());
        NON_CREATURE_SUBTYPES.addAll(CardSubtype.planeswalkerTypes());
    }

    private final StaticEffectHandlerRegistry staticEffectRegistry;

    @Autowired
    @Lazy
    private LoyaltyDamageReplacementHandlerRegistry loyaltyDamageReplacementHandlerRegistry;

    /**
     * The CR 613 layered engine: computes the whole-battlefield layer-4 (type-changing) pass
     * whose {@code CharacteristicState}s the legacy layer 5-7 accumulator below reads its
     * type/subtype filter answers from. See {@code agent-docs/LAYER_SYSTEM.md}. Injected
     * lazily like the other collaborators because it evaluates predicates, which query game
     * state through this service.
     */
    @Autowired
    @Lazy
    private LayerSystemService layerSystemService;

    /**
     * Evaluates conditional static effects (e.g. metalcraft-animate checks). Injected lazily
     * because the evaluation service itself queries game state through this service.
     */
    @Autowired
    @Lazy
    private ConditionEvaluationService conditionEvaluationService;

    /** Resolves conditional static markers consumed directly by combat and other query paths. */
    @Autowired
    @Lazy
    private StaticEffectConditionResolver staticEffectConditionResolver;

    @Autowired
    @Lazy
    private LandPlayPermissionService landPlayPermissionService;

    /**
     * Evaluates dynamic amounts used by static effects that are queried outside the layered pass,
     * such as combat taxes.
     */
    @Autowired
    @Lazy
    private AmountEvaluationService amountEvaluationService;

    /**
     * Evaluates card/permanent/stack-entry predicates and target filters. Injected lazily
     * because the evaluation service itself queries game state through this service.
     */
    @Autowired
    @Lazy
    private PredicateEvaluationService predicateEvaluationService;

    /**
     * Judges whether a static ability's subject names a permanent (the resolver's static membership check). Injected
     * lazily because the resolver queries game state through this service.
     */
    @Autowired
    @Lazy
    private SubjectResolutionService subjectResolutionService;

    /**
     * The static membership check's reads for a continuous query outside the layered pass: the fully layered
     * creature query.
     */
    private final SubjectResolutionService.StaticReads layeredQueryReads = new SubjectResolutionService.StaticReads() {
        @Override
        public boolean isCreature(GameData gameData, Permanent permanent) {
            return GameQueryService.this.isCreature(gameData, permanent);
        }

        /** A set's other characteristic part (a land), read from the permanent's printed and granted types. */
        @Override
        public boolean matchesLeaf(Permanent permanent, PermanentPredicate leaf) {
            return predicateEvaluationService.matchesPermanentPredicate(null, permanent, leaf);
        }

        @Override
        public boolean matchesFilter(StaticEffectContext context, Permanent permanent, PermanentPredicate filter) {
            throw new UnsupportedOperationException("A continuous query judges an effect's filter itself");
        }
    };

    /**
     * The static membership check's reads inside a static-bonus assembly: creature and land from the finished board of
     * the pass that runs it, which never re-enters the assembly.
     */
    private final SubjectResolutionService.StaticReads assemblyReads = new SubjectResolutionService.StaticReads() {
        @Override
        public boolean isCreature(GameData gameData, Permanent permanent) {
            CharacteristicState state = LayerSystemService.activeStateFor(permanent.getId());
            return (state != null ? state.hasCardType(CardType.CREATURE) : hasCardType(permanent, CardType.CREATURE))
                    || permanent.getCounterCount(CounterType.AWAKENING) > 0;
        }

        @Override
        public boolean matchesLeaf(Permanent permanent, PermanentPredicate leaf) {
            if (!(leaf instanceof PermanentIsLandPredicate)) {
                throw new UnsupportedOperationException("No assembly read for " + leaf);
            }
            CharacteristicState state = LayerSystemService.activeStateFor(permanent.getId());
            return state != null ? state.hasCardType(CardType.LAND) : hasCardType(permanent, CardType.LAND);
        }

        @Override
        public boolean matchesFilter(StaticEffectContext context, Permanent permanent, PermanentPredicate filter) {
            throw new UnsupportedOperationException("The assembly judges an effect's filter itself");
        }
    };


    /**
     * Aggregated static bonuses from other permanents, auras, emblems, and self-referencing
     * effects for a single permanent. Computed on-the-fly by {@link #computeStaticBonus} and
     * never stored on the permanent itself.
     *
     * @param power                     total power modifier from static effects
     * @param toughness                 total toughness modifier from static effects
     * @param creatureCount              number contributed by the permanent to creature counts
     * @param keywords                  keywords granted by static effects
     * @param protectionColors          protection colors granted by static effects
     * @param removedProtectionColors   protection colors removed by static effects
     * @param printedProtectionRemoved  whether a layer-6 "loses all protection" effect removed the permanent's own
     *                                  (printed) protection abilities
     * @param animatedCreature          whether the permanent is animated into a creature
     * @param grantedActivatedAbilities activated abilities granted by static effects
     * @param grantedEffects            card effects granted by static effects
     * @param grantedColors             colors granted by static effects
     * @param grantedSubtypes           subtypes granted by static effects
     * @param grantedCardTypes          card types granted by static effects
     * @param colorOverriding           whether granted colors replace the permanent's natural color
     * @param subtypeOverriding         whether granted subtypes replace the permanent's natural subtypes
     * @param cardTypeOverriding        whether granted card types replace the permanent's natural card types
     */
    public record StaticBonus(int power, int toughness, Set<Keyword> keywords, Set<CardColor> protectionColors,
                              Set<CardColor> removedProtectionColors, boolean printedProtectionRemoved,
                              boolean animatedCreature,
                              int creatureCount,
                              List<ActivatedAbility> grantedActivatedAbilities, List<CardEffect> grantedEffects,
                              Set<CardColor> grantedColors, List<CardSubtype> grantedSubtypes,
                              Set<CardType> grantedCardTypes, Set<CardSupertype> grantedSupertypes,
                              boolean colorOverriding, boolean subtypeOverriding, boolean landSubtypeOverriding,
                              boolean cardTypeOverriding, Set<Keyword> removedKeywords, boolean basePTOverridden,
                              int basePowerOverride, int baseToughnessOverride, boolean losesAllAbilities,
                              boolean losesAllNonManaAbilities, boolean ptSwitched, String name,
                              boolean turnFaceUpPrevented, boolean tokenized) {
        static final StaticBonus NONE = new StaticBonus(0, 0, Set.of(), Set.of(), Set.of(), false, false, 1,
                List.of(), List.of(), Set.of(), List.of(), Set.of(), Set.of(), false, false, false, false,
                Set.of(), false, 0, 0, false, false, false, null, false, false);

        public StaticBonus(int power, int toughness, Set<Keyword> keywords,
                           Set<CardColor> protectionColors, boolean animatedCreature,
                           List<ActivatedAbility> grantedActivatedAbilities,
                           List<CardEffect> grantedEffects, Set<CardColor> grantedColors,
                           List<CardSubtype> grantedSubtypes, Set<CardType> grantedCardTypes,
                           Set<CardSupertype> grantedSupertypes, boolean colorOverriding,
                           boolean subtypeOverriding, boolean landSubtypeOverriding,
                           boolean cardTypeOverriding, Set<Keyword> removedKeywords,
                           boolean basePTOverridden, Integer basePowerOverride, Integer baseToughnessOverride,
                           boolean losesAllAbilities, boolean ptSwitched) {
            this(power, toughness, keywords, protectionColors, Set.of(), false, animatedCreature, 1,
                    grantedActivatedAbilities, grantedEffects, grantedColors, grantedSubtypes,
                    grantedCardTypes, grantedSupertypes, colorOverriding, subtypeOverriding,
                    landSubtypeOverriding, cardTypeOverriding, removedKeywords,
                    basePTOverridden, basePowerOverride != null ? basePowerOverride : 0,
                    baseToughnessOverride != null ? baseToughnessOverride : 0,
                    losesAllAbilities, false, ptSwitched, null, false, false);
        }
    }

    // --- Lookup helpers ---

    private <T> T findInBattlefields(GameData gameData, UUID id, BiFunction<UUID, Permanent, T> mapper) {
        if (id == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getId().equals(id)) return mapper.apply(playerId, p);
            }
        }
        return null;
    }

    private <T> T findInGraveyards(GameData gameData, UUID id, BiFunction<UUID, Card, T> mapper) {
        if (id == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> gy = gameData.playerGraveyards.get(playerId);
            if (gy == null) continue;
            for (Card c : gy) {
                if (c.getId().equals(id)) return mapper.apply(playerId, c);
            }
        }
        return null;
    }

    private boolean hasCardType(Card card, CardType type) {
        return card.hasType(type);
    }

    /**
     * Returns whether {@code card} is a creature at the moment it enters the battlefield: a creature card, or one an
     * {@link AnimateNoncreatureArtifactsEffect} of another face-up battlefield permanent that still has its abilities
     * animates (a noncreature artifact under March of the Machines, a non-Aura enchantment under Opalescence). Such an
     * effect does not apply to its own source card.
     */
    private boolean wouldEnterAsCreature(GameData gameData, Card card) {
        if (hasCardType(card, CardType.CREATURE)) {
            return true;
        }
        return isAnimatedByPrintedStaticAnimation(gameData, new Permanent(card), card.getId(), true);
    }

    /**
     * Whether a permanent with no layered state (a departed snapshot, a permanent judged off the battlefield, a card
     * about to enter) is one an {@link AnimateNoncreatureArtifactsEffect} printed by another face-up battlefield
     * permanent animates, its filter judged on the permanent's own characteristics. A permanent on the battlefield
     * answers from its layered state instead, where the animation applied in layer 4 (CR 613.1d).
     *
     * @param ownCardId           the card the permanent is, so a source never animates itself through it
     * @param skipSourcesWithoutAbilities whether a source that lost its printed abilities is skipped (safe only outside
     *                            the static-bonus assembly)
     */
    public boolean isAnimatedByPrintedStaticAnimation(GameData gameData, Permanent permanent, UUID ownCardId,
                                                      boolean skipSourcesWithoutAbilities) {
        CharacteristicState characteristics = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent source : battlefield) {
                if (source.isFaceDown() || source.getId().equals(permanent.getId())
                        || source.getCard().getId().equals(ownCardId)) {
                    continue;
                }
                for (CardEffect effect : source.getCard().getStaticEffects()) {
                    if (!(effect instanceof AnimateNoncreatureArtifactsEffect animation)) continue;
                    if (skipSourcesWithoutAbilities && hasLostPrintedAbilities(gameData, source)) continue;
                    if (characteristics == null) {
                        characteristics = new CharacteristicState(permanent.getCard(), permanent);
                    }
                    if (predicateEvaluationService.matchesPermanentPredicate(characteristics, permanent,
                            animation.filter(), FilterContext.of(gameData))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the given player has cast at least one noncreature spell this turn.
     */
    public boolean playerCastNoncreatureSpellThisTurn(GameData gameData, UUID playerId) {
        return gameData.getSpellsCastThisTurn(playerId).stream()
                .anyMatch(card -> !card.hasType(CardType.CREATURE));
    }

    private boolean hasCardType(Permanent permanent, CardType type) {
        if (permanent.isFaceDown()) {
            return permanent.getFaceDownCardTypes().contains(type);
        }
        return hasCardType(permanent.getCard(), type);
    }

    private boolean playerBattlefieldHasStaticEffect(GameData gameData, UUID playerId, Class<? extends CardEffect> effectType) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, (Predicate<CardEffect>) effectType::isInstance);
    }

    private boolean playerBattlefieldHasStaticEffect(GameData gameData, UUID playerId, Predicate<CardEffect> matcher) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        for (Permanent perm : bf) {
            if (!perm.isFaceDown()
                    && !gameData.losesAllAbilitiesByEffect(perm)
                    && staticEffectsIncludingTemporary(gameData, perm, playerId).stream()
                    .anyMatch(matcher)) {
                return true;
            }
        }
        return false;
    }

    /** Returns whether a creature currently has a goad attack requirement. */
    public boolean isGoaded(GameData gameData, Permanent creature) {
        if (creature == null || !isCreature(gameData, creature)) {
            return false;
        }

        final long[] latestTimestamp = {Long.MIN_VALUE};
        final boolean[] goaded = {false};
        gameData.forEachPermanent((sourceControllerId, sourcePermanent) -> {
            if (sourcePermanent.isFaceDown()
                    || hasLostPrintedAbilities(gameData, sourcePermanent)) {
                return;
            }

            FilterContext context = FilterContext.of(gameData)
                    .withSourceCardId(sourcePermanent.getOriginalCard().getId())
                    .withSourceControllerId(sourceControllerId)
                    .withSourcePermanentId(sourcePermanent.getId())
                    .withSourcePermanentSnapshot(sourcePermanent);
            for (CardEffect effect : sourcePermanent.getCard().getStaticEffects()) {
                if (effect instanceof GoadStatusEffect status
                        && (!(effect instanceof CombatAttackRequirementEffect requirement)
                        || requirement.isActive(gameData, sourcePermanent))
                        && predicateEvaluationService.matchesPermanentPredicate(
                        creature, status.affectedPredicate(), context)
                        && sourcePermanent.getTimestamp() >= latestTimestamp[0]) {
                    latestTimestamp[0] = sourcePermanent.getTimestamp();
                    goaded[0] = status.makesGoaded();
                }
            }
        });

        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floatingEffect : gameData.floatingEffects) {
                if (!(floatingEffect.effect() instanceof GoadStatusEffect status)
                        || (floatingEffect.affectedPermanentId() != null
                        && !creature.getId().equals(floatingEffect.affectedPermanentId()))) {
                    continue;
                }
                if (predicateEvaluationService.matchesPermanentPredicate(
                        creature, status.affectedPredicate(),
                        FilterContext.of(gameData)
                                .withSourceControllerId(floatingEffect.controllerId())
                                .withSourcePermanentId(floatingEffect.sourcePermanentId()))
                        && floatingEffect.timestamp() >= latestTimestamp[0]) {
                    latestTimestamp[0] = floatingEffect.timestamp();
                    goaded[0] = status.makesGoaded();
                }
            }
        }
        return latestTimestamp[0] != Long.MIN_VALUE && goaded[0];
    }

    private boolean playerBattlefieldHasGrantedEffect(GameData gameData, UUID playerId,
                                                      Class<? extends CardEffect> effectType) {
        return playerBattlefieldHasGrantedEffect(gameData, playerId, (Predicate<CardEffect>) effectType::isInstance);
    }

    private boolean playerBattlefieldHasGrantedEffect(GameData gameData, UUID playerId, Predicate<CardEffect> matcher) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        return bf.stream().anyMatch(perm -> computeStaticBonus(gameData, perm).grantedEffects().stream()
                .anyMatch(matcher));
    }

    public String findAlternativeEchoCost(GameData gameData, UUID playerId) {
        List<ActiveStaticEffect<EchoCostAlternativeEffect>> alternatives =
                activeStaticEffects(gameData, playerId, EchoCostAlternativeEffect.class);
        return alternatives.isEmpty() ? null : alternatives.getFirst().effect().alternativeEchoCost();
    }

    private boolean playerHasTemporaryStaticEffect(GameData gameData, UUID playerId,
                                                   Class<? extends CardEffect> effectType) {
        return playerHasTemporaryStaticEffect(gameData, playerId, (Predicate<CardEffect>) effectType::isInstance);
    }

    private boolean playerHasTemporaryStaticEffect(GameData gameData, UUID playerId, Predicate<CardEffect> matcher) {
        synchronized (gameData.floatingEffects) {
            return gameData.floatingEffects.stream().anyMatch(floating ->
                    playerId.equals(floating.affectedPlayerId())
                            && matcher.test(floating.effect()));
        }
    }

    /**
     * The keyword-matching sibling of {@link #playerBattlefieldHasStaticEffect}: {@code true} when the
     * player controls a permanent whose STATIC slot grants them {@code keyword}.
     */
    private boolean playerBattlefieldGrantsControllerKeyword(GameData gameData, UUID playerId, Keyword keyword) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        for (Permanent perm : bf) {
            if (perm.isFaceDown() || hasLostPrintedAbilities(gameData, perm)) continue;
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
                GrantControllerKeywordEffect grant = null;
                if (effect instanceof GrantControllerKeywordEffect directGrant) {
                    grant = directGrant;
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.wrapped() instanceof GrantControllerKeywordEffect conditionalGrant
                        && conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forStaticEffect(perm, playerId))) {
                    grant = conditionalGrant;
                }
                if (grant != null && grant.keyword() == keyword) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the given permanent can't transform because its controller controls a
     * permanent with a {@link PreventTransformEffect} whose filter matches it (e.g. Immerwolf's
     * "Non-Human Werewolves you control can't transform"), or because an Aura or Equipment attached to it
     * imposes a {@link PreventTransformEffect} through {@link AttachedPermanentRestrictionEffect} (e.g. Bound by
     * Moonsilver). The filter is evaluated against the permanent's current face.
     */
    public boolean isTransformPrevented(GameData gameData, Permanent permanent) {
        if (hasAttachedPermanentRestriction(gameData, permanent, effect -> effect instanceof PreventTransformEffect prevent
                && predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, prevent.filter()))) {
            return true;
        }
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        if (controllerId == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent source : battlefield) {
            if (hasLostAllAbilities(gameData, source)) {
                continue;
            }
            for (CardEffect effect : source.getCard().getStaticEffects()) {
                if (effect instanceof PreventTransformEffect prevent
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, prevent.filter())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether a face-down permanent can't be turned face up: a static {@code CantBeTurnedFaceUpEffect} names it
     * (Unable to Scream's enchanted creature; Karlov Watchdog's opponents' permanents during its controller's turn),
     * judged by the layered pass.
     */
    public boolean isTurnFaceUpPrevented(GameData gameData, Permanent permanent) {
        return permanent != null && permanent.isFaceDown()
                && computeStaticBonus(gameData, permanent).turnFaceUpPrevented();
    }

    /**
     * Returns {@code true} if the permanent currently has the given supertype. On the battlefield the layered pass
     * decides: it applies every supertype change in layer 4 in timestamp order (CR 613.1d, 613.7), printed and granted
     * supertypes, the floating {@code SetTargetPermanentSupertypeEffect}s (Arcum's Weathervane) and the static
     * removals ({@link LoseAllLandTypesEffect#supertype}, Melting's "All lands are no longer snow"). Every supertype
     * check that such an effect can touch must route through this instead of reading the printed supertypes. A
     * permanent off the layered board (one departing) is judged by its own newest change, then its printed or granted
     * supertypes and the static removals whose filter it matches.
     */
    public boolean hasEffectiveSupertype(GameData gameData, Permanent permanent, CardSupertype supertype) {
        if (supertype == CardSupertype.LEGENDARY && isRingBearer(gameData, permanent)) {
            return true;
        }
        CharacteristicState activeState = gameData != null
                ? LayerSystemService.activeStateFor(permanent.getId()) : null;
        if (activeState == null && gameData != null && layerSystemService.activePass(gameData) == null) {
            LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
            try {
                activeState = LayerSystemService.activeStateFor(permanent.getId());
            } finally {
                layerSystemService.endPass(pass);
            }
        }
        if (activeState != null) {
            return activeState.hasSupertype(supertype);
        }
        FloatingContinuousEffect change = gameData == null ? null
                : newestSupertypeChange(gameData, permanent.getId(), supertype);
        if (change != null) {
            return ((SetTargetPermanentSupertypeEffect) change.effect()).gained()
                    && latestMatchingSupertypeRemovalTimestamp(gameData, permanent, supertype) <= change.timestamp();
        }
        if (!permanent.getCard().getSupertypes().contains(supertype)) {
            // A departing permanent may still be queried while its attached Aura is being removed.
            // It is absent from the layered board, so rebuilding its static bonus would recurse
            // through the Aura's supertype filter without ever finding a layered state.
            if (gameData == null || findPermanentById(gameData, permanent.getId()) == null) {
                return false;
            }
            return supertype == CardSupertype.LEGENDARY && isRingBearer(gameData, permanent)
                    || computeStaticBonus(gameData, permanent).grantedSupertypes().contains(supertype);
        }
        if (gameData == null) {
            return true;
        }
        return !losesSupertypeFromGlobalStaticEffect(gameData, permanent, supertype);
    }

    /** Returns whether the permanent is currently designated as its controller's Ring-bearer. */
    public boolean isRingBearer(GameData gameData, Permanent permanent) {
        if (gameData == null || permanent == null) {
            return false;
        }
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        return controllerId != null && permanent.getId().equals(gameData.ringBearerId(controllerId));
    }

    /** Returns the permanent's subtypes after continuous effects are applied in one layer pass. */
    public Set<CardSubtype> getEffectiveSubtypes(GameData gameData, Permanent permanent) {
        if (gameData == null) return fallbackPermanentSubtypes(permanent);
        CharacteristicState activeState = LayerSystemService.activeStateFor(permanent.getId());
        if (activeState != null) return Set.copyOf(activeState.getSubtypes());
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            CharacteristicState state = pass.board().states().get(permanent.getId());
            return state == null ? fallbackPermanentSubtypes(permanent) : Set.copyOf(state.getSubtypes());
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    private Set<CardSubtype> fallbackPermanentSubtypes(Permanent permanent) {
        Set<CardSubtype> result = new HashSet<>();
        for (CardSubtype subtype : CardSubtype.values()) {
            if (permanentHasSubtype(permanent, subtype)) result.add(subtype);
        }
        return result;
    }

    /** Returns whether a permanent has a subtype after continuous effects are applied. */
    public boolean hasEffectiveSubtype(GameData gameData, Permanent permanent, CardSubtype subtype) {
        if (gameData == null) {
            return permanentHasSubtype(permanent, subtype);
        }
        CharacteristicState activeState = LayerSystemService.activeStateFor(permanent.getId());
        if (activeState != null) {
            return activeState.hasSubtype(subtype);
        }
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            CharacteristicState state = pass.board().states().get(permanent.getId());
            return state != null
                    ? state.hasSubtype(subtype)
                    : permanentHasSubtype(permanent, subtype);
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    private static FloatingContinuousEffect newestSupertypeChange(GameData gameData, UUID permanentId,
                                                                  CardSupertype supertype) {
        FloatingContinuousEffect newest = null;
        for (FloatingContinuousEffect fe : gameData.floatingEffectsOnPermanent(permanentId,
                SetTargetPermanentSupertypeEffect.class)) {
            if (((SetTargetPermanentSupertypeEffect) fe.effect()).supertype() == supertype
                    && (newest == null || fe.timestamp() > newest.timestamp())) {
                newest = fe;
            }
        }
        return newest;
    }

    private boolean losesSupertypeFromGlobalStaticEffect(
            GameData gameData, Permanent permanent, CardSupertype supertype) {
        return latestMatchingSupertypeRemovalTimestamp(gameData, permanent, supertype) >= 0;
    }

    private long latestMatchingSupertypeRemovalTimestamp(
            GameData gameData, Permanent permanent, CardSupertype supertype) {
        if (gameData == null) {
            return -1;
        }
        long[] latest = {-1};
        gameData.forEachPermanent((controllerId, source) -> (source.isFaceDown()
                || gameData.losesAllAbilitiesByEffect(source)
                || (!isStaticEvaluationActive() && computeStaticBonus(gameData, source).losesAllAbilities())
                ? java.util.stream.Stream.<CardEffect>empty()
                : source.getCard().getStaticEffects().stream())
                .filter(effect -> effect instanceof LoseAllLandTypesEffect lose
                        && lose.supertype() == supertype
                        && (!(lose.subject() instanceof PermanentSubject.Source)
                        || source.getId().equals(permanent.getId()))
                        && (lose.filter() == null || (isStaticEvaluationActive()
                        ? predicateEvaluationService.matchesStaticFilter(
                                permanent, lose.filter(), FilterContext.of(gameData)
                                        .withSourceCardId(source.getCard().getId())
                                        .withSourceControllerId(controllerId))
                        : predicateEvaluationService.matchesPermanentPredicate(
                                permanent, lose.filter(), FilterContext.of(gameData)
                                        .withSourceCardId(source.getCard().getId())
                                        .withSourceControllerId(controllerId)))))
                .forEach(effect -> latest[0] = Math.max(latest[0], source.getTimestamp())));
        return latest[0];
    }

    private boolean anyBattlefieldHasStaticEffect(GameData gameData, Class<? extends CardEffect> effectType) {
        return boardScan(gameData, new BoardScan(BoardScan.Kind.ACTIVE_PRINTED_STATIC, effectType), () ->
                gameData.anyPermanentMatches(p ->
                        !p.isFaceDown()
                                && p.getCard().getStaticEffects().stream().anyMatch(effectType::isInstance)
                                && !hasLostPrintedAbilities(gameData, p)));
    }

    /**
     * Whether any battlefield permanent's card prints a static effect of {@code effectType}, whatever its state: the
     * cheap first question of a scan that only acts on such effects (an animation effect that makes a noncreature a
     * creature), so a board without one skips the per-permanent judging.
     */
    private boolean anyBattlefieldPrintsStaticEffect(GameData gameData, Class<? extends CardEffect> effectType) {
        return boardScan(gameData, new BoardScan(BoardScan.Kind.PRINTED_STATIC, effectType), () ->
                gameData.anyPermanentMatches(p ->
                        p.getCard().getStaticEffects().stream().anyMatch(effectType::isInstance)));
    }

    /**
     * Answers the board-wide question {@code key} from the active query scope's memo
     * ({@link LayerSystemService.Pass#boardScan}), or by {@code compute} when no scope is active.
     */
    private boolean boardScan(GameData gameData, BoardScan key, java.util.function.BooleanSupplier compute) {
        LayerSystemService.Pass pass = layerSystemService.activePass(gameData);
        return pass == null ? compute.getAsBoolean() : pass.boardScan(key, compute);
    }

    /** The name of a board-wide question {@link #boardScan} memoizes: its kind and the effect type it asks about. */
    private record BoardScan(Kind kind, Class<?> effectType) {

        private enum Kind {
            /** {@link #anyBattlefieldHasStaticEffect}. */
            ACTIVE_PRINTED_STATIC,
            /** {@link #anyBattlefieldPrintsStaticEffect}. */
            PRINTED_STATIC,
            /** {@link #graveyardCardsHaveLostAllAbilities}. */
            GRAVEYARD_CARDS_LOSE_ALL_ABILITIES,
            /** {@link #anyManaAbilityLock}. */
            MANA_ABILITY_LOCK
        }
    }

    // --- Permanent / Card lookups ---

    /**
     * Finds a permanent on any player's battlefield by its unique ID.
     *
     * @return the permanent, or {@code null} if not found
     */
    /**
     * True when {@code playerId} was attacked this turn by an attacker that {@code controllerId}
     * controlled and that had {@code subtype} when it was declared as an attacker, so later changes
     * of control, subtypes or death do not undo the attack. An attack with no declaration snapshot
     * is judged by the attacker's current state.
     */
    public boolean wasPlayerAttackedByControlledSubtypeThisTurn(GameData gameData, UUID playerId,
                                                                UUID controllerId, CardSubtype subtype) {
        for (var attack : gameData.playersAttackedThisTurn.entrySet()) {
            if (!attack.getValue().contains(playerId)) {
                continue;
            }
            Set<GameData.DeclaredAttackerSnapshot> snapshots =
                    gameData.declaredAttackerSnapshotsThisTurn.get(attack.getKey());
            if (snapshots != null && !snapshots.isEmpty()) {
                if (snapshots.stream().anyMatch(snapshot -> controllerId.equals(snapshot.controllerId())
                        && snapshot.subtypes().contains(subtype))) {
                    return true;
                }
                continue;
            }
            Permanent attacker = findPermanentById(gameData, attack.getKey());
            if (attacker != null
                    && controllerId.equals(findPermanentController(gameData, attacker.getId()))
                    && hasEffectiveSubtype(gameData, attacker, subtype)) {
                return true;
            }
        }
        return false;
    }

    public Permanent findPermanentById(GameData gameData, UUID permanentId) {
        return findInBattlefields(gameData, permanentId, (playerId, p) -> p);
    }

    /**
     * Finds a card in any game zone by its unique ID.
     *
     * @return the card, or {@code null} if not found
     */
    public Card findCardById(GameData gameData, UUID cardId) {
        if (cardId == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (cardId.equals(permanent.getCard().getId())) return permanent.getCard();
                }
            }
            Card found = findCard(gameData.playerHands.get(playerId), cardId);
            if (found != null) return found;
            found = findCard(gameData.playerDecks.get(playerId), cardId);
            if (found != null) return found;
            found = findCard(gameData.playerGraveyards.get(playerId), cardId);
            if (found != null) return found;
            found = findCard(gameData.getPlayerExiledCards(playerId), cardId);
            if (found != null) return found;
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (cardId.equals(stackEntry.getCard().getId())) return stackEntry.getCard();
        }
        return null;
    }

    private Card findCard(List<Card> cards, UUID cardId) {
        if (cards == null) return null;
        for (Card card : cards) {
            if (cardId.equals(card.getId())) return card;
        }
        return null;
    }

    /**
     * Finds the controller (player ID) of a permanent by the permanent's unique ID.
     *
     * @return the controlling player's ID, or {@code null} if the permanent is not on any battlefield
     */
    /**
     * The controller of a permanent dealing damage: its current controller, or — when it has left the
     * battlefield — the last-known controller {@code entry} recorded for it (CR 608.2h), falling back to
     * the controller of {@code entry} itself.
     */
    public UUID findDamageSourceController(GameData gameData, StackEntry entry, Permanent source) {
        UUID controllerId = source == null ? null : findPermanentController(gameData, source.getId());
        if (controllerId == null && source != null && entry != null) {
            controllerId = entry.lastKnownPermanentController(source.getId());
        }
        return controllerId != null || entry == null ? controllerId : entry.getControllerId();
    }

    /**
     * The player whose battlefield holds the permanent {@code permanentId}, or {@code null}. Inside a query scope the
     * pass's controller index answers ({@link LayerSystemService.Pass#controllerOf}), each answer checked against the
     * battlefield it names; otherwise, or when the index doesn't know the permanent, the battlefields are searched.
     */
    public UUID findPermanentController(GameData gameData, UUID permanentId) {
        if (permanentId == null) return null;
        LayerSystemService.Pass pass = layerSystemService.activePass(gameData);
        UUID indexed = pass == null ? null : pass.controllerOf(permanentId);
        return indexed != null ? indexed : findInBattlefields(gameData, permanentId, (playerId, p) -> playerId);
    }

    /**
     * The owner of the card a "whenever you play a card" event was about ("that card's owner"): the triggering spell's
     * card, else the land played ({@code StackEntry.triggeringSpellOrCard()}). A card's owner never changes.
     *
     * @return the player, or {@code null} when the entry records no such card or it has no recorded owner
     */
    public UUID findTriggeringCardOwner(GameData gameData, StackEntry entry) {
        Card played = entry.triggeringSpellOrCard();
        return played == null ? null : gameData.ownerOf(played);
    }

    /**
     * The owner of the permanent a triggered ability's event was about ("its owner", Brainstealer Dragon's "they" for
     * "a nonland permanent an opponent owns"), as the ability resolves: the owner of the entry's triggering permanent's
     * card while it is on the battlefield, otherwise the owner of its card as it last existed (CR 608.2h), the card the
     * entry recorded for that permanent. A copy effect keeps the copied permanent's owner, so either card names it.
     *
     * @return the player, or {@code null} when the entry records no triggering permanent or its card has no owner
     */
    public UUID findTriggeringPermanentOwner(GameData gameData, StackEntry entry) {
        UUID permanentId = entry.getTriggeringPermanentId();
        if (permanentId == null) {
            return null;
        }
        Permanent live = findPermanentById(gameData, permanentId);
        Card card = live != null ? live.getOriginalCard() : entry.lastKnownPermanentCard(permanentId);
        return card == null ? null : gameData.ownerOf(card);
    }

    /**
     * The controller of the object a triggered ability's event was about, as the ability resolves ("that source's
     * controller", "its controller"): the current controller of the entry's triggering permanent while it is on the
     * battlefield, otherwise its last known controller (CR 608.2h), which is the controller the entry recorded when it
     * removed the permanent or, failing that, when the ability triggered. A spell or other source that was never a
     * permanent has no triggering permanent, so the recorded controller is used.
     *
     * @return the player, or {@code null} when the entry records neither
     */
    public UUID findTriggeringPermanentController(GameData gameData, StackEntry entry) {
        UUID permanentId = entry.getTriggeringPermanentId();
        if (permanentId != null) {
            UUID currentControllerId = findPermanentController(gameData, permanentId);
            if (currentControllerId != null) {
                return currentControllerId;
            }
            UUID removedControllerId = entry.lastKnownPermanentController(permanentId);
            if (removedControllerId != null) {
                return removedControllerId;
            }
        }
        return entry.getTriggeringPermanentControllerId();
    }

    /**
     * Finds a card in any player's graveyard by its unique ID.
     *
     * @return the card, or {@code null} if not found
     */
    public Card findCardInGraveyardById(GameData gameData, UUID cardId) {
        return findInGraveyards(gameData, cardId, (playerId, c) -> c);
    }

    /** Evaluates a card predicate with the engine's canonical predicate semantics. */
    public boolean matchesCardPredicate(Card card, CardPredicate predicate, UUID sourceCardId) {
        return predicateEvaluationService.matchesCardPredicate(card, predicate, sourceCardId);
    }

    /**
     * Finds the owner (player ID) of a card in a graveyard by the card's unique ID.
     *
     * @return the owning player's ID, or {@code null} if the card is not in any graveyard
     */
    public UUID findGraveyardOwnerById(GameData gameData, UUID cardId) {
        return findInGraveyards(gameData, cardId, (playerId, c) -> playerId);
    }

    /**
     * Finds a card in any player's exile zone by its unique ID.
     *
     * @return the card, or {@code null} if not found
     */
    public Card findCardInExileById(GameData gameData, UUID cardId) {
        return findInExile(gameData, cardId, (playerId, c) -> c);
    }

    /**
     * Finds the owner (player ID) of a card in exile by the card's unique ID.
     *
     * @return the owning player's ID, or {@code null} if the card is not in any exile zone
     */
    public UUID findExileOwnerById(GameData gameData, UUID cardId) {
        return findInExile(gameData, cardId, (playerId, c) -> playerId);
    }

    public StackEntry findStackEntryByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) return null;
        for (StackEntry se : gameData.stack) {
            if (se.getTargetableId().equals(cardId)) {
                return se;
            }
        }
        StackEntry resolving = gameData.pendingEffectResolutionEntry;
        return resolving != null && resolving.getTargetableId().equals(cardId) ? resolving : null;
    }

    private <T> T findInExile(GameData gameData, UUID id, BiFunction<UUID, Card, T> mapper) {
        if (id == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> exile = gameData.getPlayerExiledCards(playerId);
            for (Card c : exile) {
                if (c.getId().equals(id)) return mapper.apply(playerId, c);
            }
        }
        return null;
    }

    // --- All-zone characteristic grants ---

    /**
     * Returns whether a card has the given type, including type grants that apply to cards outside
     * the battlefield. The player id identifies the player whose static effects apply to the card.
     */
    public boolean cardHasType(Card card, CardType type, GameData gameData, UUID playerId) {
        if (card.hasType(type)) return true;
        if (gameData != null && gameData.perpetualCardTypes
                .getOrDefault(card.getId(), Set.of()).contains(type)) {
            return true;
        }
        GameData.GraveyardCardAnimation graveyardAnimation = activeGraveyardCardAnimation(gameData, card);
        if (graveyardAnimation != null && graveyardAnimation.grantedCardTypes().contains(type)) {
            return true;
        }
        if (type == CardType.CREATURE && isOutsideBattlefieldCreature(card, gameData)) {
            return true;
        }
        if (gameData == null || playerId == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (card.getType() != null && card.getType().isPermanentType()
                && !card.hasType(CardType.LAND) && battlefield != null) {
            boolean permanentSpell = isPermanentSpellOnStackControlledBy(gameData, card, playerId);
            for (Permanent source : battlefield) {
                for (CardEffect effect : source.getCard().getStaticEffects()) {
                    GrantCardTypeToOwnNonlandPermanentsEffect grant = null;
                    if (effect instanceof GrantCardTypeToOwnNonlandPermanentsEffect directGrant) {
                        grant = directGrant;
                    } else if (effect instanceof ConditionalEffect conditional
                            && conditional.wrapped() instanceof GrantCardTypeToOwnNonlandPermanentsEffect conditionalGrant
                            && conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forStaticEffect(source, playerId))) {
                        grant = conditionalGrant;
                    }
                    if (grant != null && grant.cardType() == type
                            && (grant.includeNonBattlefieldCards() || permanentSpell)) {
                        return true;
                    }
                }
            }
        }

        List<Permanent> sources = gameData.playerBattlefields.get(playerId);
        if (sources == null) return false;
        for (Permanent source : sources) {
            for (CardEffect effect : source.getCard().getStaticEffects()) {
                if (effect instanceof OwnCardTypeGrantingEffect grant
                        && grant.cardType() == type
                        && (card.getOwnerId() == null || card.getOwnerId().equals(playerId))
                        && predicateEvaluationService.matchesCardPredicate(
                        card, grant.filter(), source.getCard().getId(), gameData, playerId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether a card has the given supertype, including static grants that apply to land
     * cards in their owner's library and layered changes on permanents.
     */
    public boolean cardHasSupertype(Card card, CardSupertype supertype, GameData gameData, UUID cardOwnerId) {
        if (gameData != null) {
            Permanent permanent = findPermanentById(gameData, card.getId());
            if (permanent != null) {
                return hasEffectiveSupertype(gameData, permanent, supertype);
            }
        }
        if (card.getSupertypes().contains(supertype)) return true;
        if (gameData == null || cardOwnerId == null
                || !cardHasType(card, CardType.LAND, gameData, cardOwnerId)
                || !gameData.playerDecks.getOrDefault(cardOwnerId, List.of()).stream()
                .anyMatch(libraryCard -> libraryCard.getId().equals(card.getId()))) {
            return false;
        }

        List<Permanent> sources = gameData.playerBattlefields.get(cardOwnerId);
        if (sources == null) return false;
        for (Permanent source : sources) {
            for (CardEffect effect : source.getCard().getStaticEffects()) {
                if (effect instanceof OwnLandSupertypeGrantingEffect grant
                        && grant.supertype() == supertype) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPermanentSpellOnStackControlledBy(GameData gameData, Card card, UUID playerId) {
        return gameData.stack.stream()
                .filter(entry -> card.getId().equals(entry.getTargetableId()))
                .filter(entry -> playerId.equals(entry.getControllerId()))
                .anyMatch(entry -> switch (entry.getEntryType()) {
                    case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL,
                            PLANESWALKER_SPELL, BATTLE_SPELL -> true;
                    case TRIGGERED_ABILITY, ACTIVATED_ABILITY, SORCERY_SPELL,
                            INSTANT_SPELL -> false;
                });
    }

    // --- Arcane Adaptation / all-zone subtype grants ---

    /**
     * Returns {@code true} if the card has the given subtype, considering both its natural
     * subtypes and any subtypes granted by "affects all zones" static effects (e.g. Arcane
     * Adaptation). Only creature cards receive granted subtypes.
     *
     * <p>When {@code gameData} or {@code cardOwnerId} is {@code null}, falls back to checking
     * only the card's natural subtypes.
     */
    public boolean cardHasSubtype(Card card, CardSubtype subtype, GameData gameData, UUID cardOwnerId) {
        if (gameData != null && cardOwnerId != null && isCreatureSubtype(subtype)
                && hasAllZoneCreatureTypeOverride(gameData, cardOwnerId)
                && cardHasType(card, CardType.CREATURE, gameData, cardOwnerId)) {
            return getCardSubtypes(card, gameData, cardOwnerId).contains(subtype);
        }
        boolean printedCreatureTypeReplaced = gameData != null && isCreatureSubtype(subtype)
                && gameData.perpetualCreatureTypeReplacedCardIds.contains(card.getId());
        if (!printedCreatureTypeReplaced && card.getSubtypes().contains(subtype)) return true;
        if (gameData != null && gameData.perpetualCardSubtypes
                .getOrDefault(card.getId(), Set.of()).contains(subtype)) {
            return true;
        }
        if (gameData != null && cardOwnerId != null && !isOnBattlefield(card, gameData)
                && cardHasType(card, CardType.LAND, gameData, cardOwnerId)
                && computeGrantedSubtypesForOwnedLandCard(gameData, cardOwnerId).contains(subtype)) {
            return true;
        }
        GameData.GraveyardCardAnimation graveyardAnimation = activeGraveyardCardAnimation(gameData, card);
        if (graveyardAnimation != null && graveyardAnimation.grantedSubtypes().contains(subtype)) {
            return true;
        }
        if (isCreatureSubtype(subtype) && card.hasKeyword(Keyword.CHANGELING)) return true;
        if (!isCreatureSubtype(subtype) && cardHasType(card, CardType.LAND, gameData, cardOwnerId)
                && card.getStaticEffects().stream()
                .anyMatch(effect -> effect instanceof SelfAllZoneSubtypeGrantingEffect grant
                        && grant.grantedSubtypes().contains(subtype))) return true;
        if (cardHasType(card, CardType.CREATURE, gameData, cardOwnerId) && isCreatureSubtype(subtype)
                && (hasSelfAllCreatureTypesEffect(card)
                || selfAllZoneGrantedSubtypes(card).contains(subtype)
                || selfOutsideBattlefieldGrantedSubtypes(card, gameData).contains(subtype))) return true;
        if (gameData == null || cardOwnerId == null) return false;
        if (!cardHasType(card, CardType.CREATURE, gameData, cardOwnerId)) return false;
        if (computeGrantedSubtypesForOwnedCreatureCard(gameData, cardOwnerId).contains(subtype)) {
            return true;
        }
        return isCardInGraveyard(gameData, cardOwnerId, card)
                && computeGrantedGraveyardSubtypesForOwnedCreatureCard(gameData, cardOwnerId, card)
                .contains(subtype);
    }

    private List<CardSubtype> computeGrantedSubtypesForOwnedLandCard(GameData gameData, UUID ownerId) {
        List<CardSubtype> result = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(ownerId);
        if (battlefield == null) return result;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                if (effect instanceof OwnLandSubtypeGrantingEffect grant
                        && !result.contains(grant.subtype())) {
                    result.add(grant.subtype());
                }
            }
        }
        return result;
    }

    private static final Set<CardSubtype> BASIC_LAND_SUBTYPES = EnumSet.of(
            CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
            CardSubtype.MOUNTAIN, CardSubtype.FOREST);

    private static final Set<CardSubtype> LAND_SUBTYPES = EnumSet.copyOf(CardSubtype.landTypes());

    /**
     * The effective basic land types (Plains/Island/Swamp/Mountain/Forest) of a permanent,
     * respecting CR 305.7 land-type overrides (Blood Moon, Tideshaper Mystic) â€” when a land-type
     * setter is active the printed and one-shot-granted types are replaced by the setter's types.
     * Used for Domain counting.
     */
    public Set<CardSubtype> effectiveBasicLandTypes(GameData gameData, Permanent permanent) {
        Set<CardSubtype> result = EnumSet.noneOf(CardSubtype.class);
        for (CardSubtype subtype : BASIC_LAND_SUBTYPES) {
            if (hasEffectiveSubtype(gameData, permanent, subtype)) {
                result.add(subtype);
            }
        }
        return result;
    }

    public Set<CardSubtype> effectiveLandTypes(GameData gameData, Permanent permanent) {
        Set<CardSubtype> result = EnumSet.noneOf(CardSubtype.class);
        if (gameData != null) {
            CharacteristicState layered = LayerSystemService.activeStateFor(permanent.getId());
            if (layered == null) {
                LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
                try {
                    layered = pass.board().states().get(permanent.getId());
                    if (layered != null) {
                        addLandTypes(result, layered);
                        return result;
                    }
                } finally {
                    layerSystemService.endPass(pass);
                }
            } else {
                addLandTypes(result, layered);
                return result;
            }
        }
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (!bonus.landSubtypeOverriding()) {
            if (permanent.isFaceDown()) {
                addLandTypes(result, permanent.getFaceDownSubtypes());
            } else {
                addLandTypes(result, permanent.getCard().getSubtypes());
            }
        }
        addLandTypes(result, bonus.grantedSubtypes());
        return result;
    }

    /** Returns the mana colors granted intrinsically by the permanent's effective basic land types. */
    public Set<ManaColor> intrinsicBasicLandManaColors(GameData gameData, Permanent permanent) {
        if (permanent == null || !isLand(gameData, permanent)) {
            return Set.of();
        }
        CharacteristicState activeState = LayerSystemService.activeStateFor(permanent.getId());
        if (activeState != null) {
            if (gameData.losesAllAbilitiesByEffect(permanent) || activeState.isLosesAllAbilities()) {
                return Set.of();
            }
        } else {
            LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
            try {
                CharacteristicState state = pass.board().states().get(permanent.getId());
                if (gameData.losesAllAbilitiesByEffect(permanent)
                        || state != null && state.isLosesAllAbilities()) {
                    return Set.of();
                }
            } finally {
                layerSystemService.endPass(pass);
            }
        }
        Set<ManaColor> colors = EnumSet.noneOf(ManaColor.class);
        for (CardSubtype subtype : effectiveBasicLandTypes(gameData, permanent)) {
            ManaColor color = switch (subtype) {
                case PLAINS -> ManaColor.WHITE;
                case ISLAND -> ManaColor.BLUE;
                case SWAMP -> ManaColor.BLACK;
                case MOUNTAIN -> ManaColor.RED;
                case FOREST -> ManaColor.GREEN;
                default -> null;
            };
            if (color != null) {
                colors.add(color);
            }
        }
        return colors;
    }

    /**
     * The intrinsic "{T}: Add [mana]" abilities of the permanent's basic land types (CR 305.6) that
     * {@code present} doesn't already include, in color order. A face-down permanent has none. These come last in
     * a permanent's activated ability list, after its printed and granted abilities.
     */
    public List<ActivatedAbility> missingIntrinsicManaAbilities(GameData gameData, Permanent permanent,
                                                                List<ActivatedAbility> present) {
        if (permanent.isFaceDown()) {
            return List.of();
        }
        List<ActivatedAbility> missing = new ArrayList<>();
        for (ManaColor color : intrinsicBasicLandManaColors(gameData, permanent)) {
            boolean alreadyPresent = present.stream().anyMatch(ability -> ability.isRequiresTap()
                    && ability.getEffects().size() == 1
                    && ability.getEffects().getFirst() instanceof AwardManaEffect mana
                    && mana.color() == color);
            if (!alreadyPresent) {
                missing.add(ManaAbilities.intrinsic(color));
            }
        }
        return missing;
    }

    public Set<CardSubtype> landTypesOf(Card card) {
        Set<CardSubtype> result = EnumSet.noneOf(CardSubtype.class);
        addLandTypes(result, card.getSubtypes());
        return result;
    }

    private void addLandTypes(Set<CardSubtype> target, List<CardSubtype> subtypes) {
        for (CardSubtype subtype : subtypes) {
            if (LAND_SUBTYPES.contains(subtype)) {
                target.add(subtype);
            }
        }
    }

    private void addLandTypes(Set<CardSubtype> target, CharacteristicState state) {
        for (CardSubtype subtype : LAND_SUBTYPES) {
            if (state.hasSubtype(subtype)) {
                target.add(subtype);
            }
        }
    }

    private void addLandTypes(Set<CardSubtype> target, Set<CardSubtype> subtypes) {
        for (CardSubtype subtype : subtypes) {
            if (LAND_SUBTYPES.contains(subtype)) {
                target.add(subtype);
            }
        }
    }

    /**
     * Returns all subtypes of a creature card, including those granted by Arcane Adaptation-style effects.
     */
    public Set<CardSubtype> getCardSubtypes(Card card, GameData gameData, UUID cardOwnerId) {
        Set<CardSubtype> subtypes = new java.util.HashSet<>(card.getSubtypes());
        if (gameData != null) {
            if (gameData.perpetualCreatureTypeReplacedCardIds.contains(card.getId())) {
                subtypes.removeIf(this::isCreatureSubtype);
            }
            subtypes.addAll(gameData.perpetualCardSubtypes.getOrDefault(card.getId(), Set.of()));
            GameData.GraveyardCardAnimation graveyardAnimation = activeGraveyardCardAnimation(gameData, card);
            if (graveyardAnimation != null) {
                subtypes.addAll(graveyardAnimation.grantedSubtypes());
            }
        }
        boolean creature = cardHasType(card, CardType.CREATURE, gameData, cardOwnerId);
        if (card.hasKeyword(Keyword.CHANGELING) || (creature && hasSelfAllCreatureTypesEffect(card))) {
            for (CardSubtype subtype : CardSubtype.values()) {
                if (isCreatureSubtype(subtype)) subtypes.add(subtype);
            }
        }
        if (creature) {
            subtypes.addAll(selfAllZoneGrantedSubtypes(card));
            subtypes.addAll(selfOutsideBattlefieldGrantedSubtypes(card, gameData));
        }
        if (gameData != null && cardOwnerId != null && creature) {
            if (hasAllZoneCreatureTypeOverride(gameData, cardOwnerId)) {
                subtypes.removeIf(this::isCreatureSubtype);
            }
            subtypes.addAll(computeGrantedSubtypesForOwnedCreatureCard(gameData, cardOwnerId));
            if (isCardInGraveyard(gameData, cardOwnerId, card)) {
                subtypes.addAll(computeGrantedGraveyardSubtypesForOwnedCreatureCard(gameData, cardOwnerId, card));
            }
        }
        return subtypes;
    }

    private boolean hasAllZoneCreatureTypeOverride(GameData gameData, UUID ownerId) {
        for (Permanent source : gameData.playerBattlefields.getOrDefault(ownerId, List.of())) {
            if (source.getChosenSubtype() == null || source.isFaceDown()) continue;
            boolean overrides = source.getCard().getStaticEffects().stream()
                    .anyMatch(effect -> effect instanceof GrantChosenSubtypeToOwnCreaturesEffect grant
                            && grant.affectsAllZones() && grant.overriding());
            if (overrides && !hasLostPrintedAbilities(gameData, source)) return true;
        }
        return false;
    }

    private boolean hasSelfAllCreatureTypesEffect(Card card) {
        return card.getStaticEffects().stream()
                .anyMatch(effect -> effect instanceof GrantAllCreatureTypesToOwnCreaturesEffect grant
                        && grant.subject() instanceof PermanentSubject.Source);
    }

    private List<CardSubtype> selfAllZoneGrantedSubtypes(Card card) {
        return card.getStaticEffects().stream()
                .filter(SelfAllZoneSubtypeGrantingEffect.class::isInstance)
                .map(SelfAllZoneSubtypeGrantingEffect.class::cast)
                .flatMap(effect -> effect.grantedSubtypes().stream())
                .filter(this::isCreatureSubtype)
                .distinct()
                .toList();
    }

    private List<CardSubtype> selfOutsideBattlefieldGrantedSubtypes(Card card, GameData gameData) {
        if (isOnBattlefield(card, gameData)) {
            return List.of();
        }
        return card.getStaticEffects().stream()
                .filter(SelfOutsideBattlefieldCreatureEffect.class::isInstance)
                .map(SelfOutsideBattlefieldCreatureEffect.class::cast)
                .flatMap(effect -> effect.grantedSubtypes().stream())
                .filter(this::isCreatureSubtype)
                .distinct()
                .toList();
    }

    private boolean isOutsideBattlefieldCreature(Card card, GameData gameData) {
        return !isOnBattlefield(card, gameData)
                && card.getStaticEffects().stream()
                .anyMatch(SelfOutsideBattlefieldCreatureEffect.class::isInstance);
    }

    private boolean isOnBattlefield(Card card, GameData gameData) {
        if (card == null || gameData == null) {
            return false;
        }
        return gameData.orderedPlayerIds.stream()
                .map(gameData.playerBattlefields::get)
                .filter(list -> list != null)
                .flatMap(List::stream)
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
    }

    /**
     * Computes subtypes granted to one creature card while it is in its owner's graveyard. Unlike
     * all-zone subtype grants, these grants stop applying as soon as the card changes zones.
     */
    public List<CardSubtype> computeGrantedGraveyardSubtypesForOwnedCreatureCard(
            GameData gameData, UUID ownerId, Card card) {
        List<CardSubtype> result = new ArrayList<>();
        if (gameData == null || ownerId == null || card == null || !card.hasType(CardType.CREATURE)) {
            return result;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(ownerId);
        if (battlefield == null) return result;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                if (!(effect instanceof GraveyardSubtypeGrantingEffect grant)
                        || !grant.appliesTo(card)) {
                    continue;
                }
                CardSubtype subtype = grant.grantedGraveyardSubtypeFor(permanent, card);
                if (subtype != null && !result.contains(subtype)) {
                    result.add(subtype);
                }
            }
        }
        return result;
    }

    private boolean isCardInGraveyard(GameData gameData, UUID ownerId, Card card) {
        return gameData.playerGraveyards.getOrDefault(ownerId, List.of()).stream()
                .anyMatch(graveyardCard -> graveyardCard.getId().equals(card.getId()));
    }

    /**
     * Computes the list of subtypes granted to creature cards owned by the given player in
     * non-battlefield zones (hand, graveyard, library, exile) and creature spells they control
     * on the stack. Scans the owner's battlefield for permanents with
     * {@link GrantChosenSubtypeToOwnCreaturesEffect#affectsAllZones()} == {@code true}, or
     * with a {@link GrantAllCreatureTypesToOwnCreaturesEffect}, or with a fixed subtype grant.
     */
    public List<CardSubtype> computeGrantedSubtypesForOwnedCreatureCard(GameData gameData, UUID ownerId) {
        List<CardSubtype> result = new ArrayList<>();
        List<Permanent> bf = gameData.playerBattlefields.get(ownerId);
        if (bf == null) return result;
        for (Permanent perm : bf.stream().sorted(java.util.Comparator.comparingLong(Permanent::getTimestamp)).toList()) {
            List<CardEffect> staticEffects = perm.getCard().getStaticEffects();
            if (staticEffects.stream().noneMatch(effect ->
                    effect instanceof GrantChosenSubtypeToOwnCreaturesEffect grant && grant.affectsAllZones()
                            || effect instanceof GrantAllCreatureTypesToOwnCreaturesEffect
                            || effect instanceof OwnCreatureSubtypeGrantingEffect)) continue;
            if (perm.isFaceDown() || hasLostPrintedAbilities(gameData, perm)) continue;
            CardSubtype chosen = perm.getChosenSubtype();
            for (CardEffect effect : staticEffects) {
                if (effect instanceof GrantChosenSubtypeToOwnCreaturesEffect g && g.affectsAllZones()) {
                    if (chosen != null && g.overriding()) {
                        result.removeIf(this::isCreatureSubtype);
                    }
                    if (chosen != null && !result.contains(chosen)) {
                        result.add(chosen);
                    }
                } else if (effect instanceof GrantAllCreatureTypesToOwnCreaturesEffect grant
                        && !(grant.subject() instanceof PermanentSubject.Source)
                        && grant.filter() == null) {
                    for (CardSubtype subtype : CardSubtype.values()) {
                        if (isCreatureSubtype(subtype) && !result.contains(subtype)) {
                            result.add(subtype);
                        }
                    }
                } else if (effect instanceof OwnCreatureSubtypeGrantingEffect grant
                        && !result.contains(grant.subtype())) {
                    result.add(grant.subtype());
                }
            }
        }
        return result;
    }

    /**
     * Computes the graveyard-activated abilities granted to a card owned by the given player by
     * static effects on that player's battlefield. The abilities are computed for {@code card}
     * because a grant may derive its cost from the card, and each grant decides whether it applies.
     */
    public List<ActivatedAbility> computeGrantedGraveyardAbilitiesForOwnedCard(GameData gameData, UUID ownerId,
                                                                                Card card) {
        List<ActivatedAbility> result = new ArrayList<>();
        if (graveyardCardsHaveLostAllAbilities(gameData)) return result;
        List<Permanent> bf = gameData.playerBattlefields.get(ownerId);
        if (bf != null) {
            for (Permanent perm : bf) {
                if (perm.isFaceDown() || hasLostAllAbilities(gameData, perm)) continue;
                for (CardEffect effect : perm.getCard().getStaticEffects()) {
                    if (effect instanceof GraveyardAbilityGrantingEffect g) {
                        if (!graveyardGrantApplies(gameData, ownerId, card, g)) continue;
                        ActivatedAbility granted = g.grantedGraveyardAbilityFor(card);
                        if (granted != null) {
                            result.add(granted);
                        }
                    }
                }
            }
        }
        if (gameData.planechase != null && Objects.equals(gameData.planechase.controllerId, ownerId)) {
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof GraveyardAbilityGrantingEffect g) {
                        if (!graveyardGrantApplies(gameData, ownerId, card, g)) continue;
                        ActivatedAbility granted = g.grantedGraveyardAbilityFor(card);
                        if (granted != null) {
                            result.add(granted);
                        }
                    }
                }
            }
        }
        for (Ability perpetual : gameData.perpetualAbilities(card.getId())) {
            if (perpetual instanceof ActivatedAbility activated && activated.functionsIn(Zone.GRAVEYARD)) {
                result.add(activated);
            }
        }
        if (card != null && gameData.hasGrantedCardAbility(card.getId(), GrantedCardAbilityEffect.Ability.EMBALM)
                && card.getManaCost() != null && !card.getManaCost().isBlank()) {
            result.add(Card.embalmAbility(card.getManaCost()));
        }
        if (card != null) {
            String unearthCost = gameData.grantedCardAbility(card.getId(), GrantedCardAbilityEffect.Ability.UNEARTH)
                    .map(GrantedCardAbilityEffect::cost).orElse(null);
            if (unearthCost != null && !unearthCost.isBlank()) {
                result.add(Card.unearthAbility(unearthCost));
            }
        }
        if (card != null && gameData.cardsGrantedPerpetualUnearth.contains(card.getId())
                && card.getManaCost() != null && !card.getManaCost().isBlank()
                && !cardHasUnearthAbilityBeforePerpetualGrant(gameData, ownerId, card)) {
            result.add(Card.unearthAbility(card.getManaCost()));
        }
        return result;
    }

    /** Whether the card currently has native or granted unearth. */
    public boolean cardHasUnearthAbility(GameData gameData, UUID ownerId, Card card) {
        if (card == null) return false;
        if (card.getActivatedAbilities(Zone.GRAVEYARD).stream().anyMatch(ActivatedAbility::isUnearthAbility)) {
            return true;
        }
        if (gameData != null && gameData.cardsGrantedPerpetualUnearth.contains(card.getId())
                && card.getManaCost() != null && !card.getManaCost().isBlank()) {
            return true;
        }
        if (gameData != null && gameData.hasGrantedCardAbility(card.getId(), GrantedCardAbilityEffect.Ability.UNEARTH)) {
            return true;
        }
        return cardHasUnearthAbilityBeforePerpetualGrant(gameData, ownerId, card);
    }

    private boolean cardHasUnearthAbilityBeforePerpetualGrant(GameData gameData, UUID ownerId, Card card) {
        return computeGrantedGraveyardAbilitiesForOwnedCardWithoutPerpetualUnearth(gameData, ownerId, card)
                .stream().anyMatch(ActivatedAbility::isUnearthAbility);
    }

    private List<ActivatedAbility> computeGrantedGraveyardAbilitiesForOwnedCardWithoutPerpetualUnearth(
            GameData gameData, UUID ownerId, Card card) {
        List<ActivatedAbility> abilities = new ArrayList<>();
        if (graveyardCardsHaveLostAllAbilities(gameData)) return abilities;
        List<Permanent> battlefield = gameData.playerBattlefields.get(ownerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (permanent.isFaceDown() || hasLostAllAbilities(gameData, permanent)) continue;
                for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                    if (effect instanceof GraveyardAbilityGrantingEffect grant
                            && graveyardGrantApplies(gameData, ownerId, card, grant)) {
                        ActivatedAbility ability = grant.grantedGraveyardAbilityFor(card);
                        if (ability != null) abilities.add(ability);
                    }
                }
            }
        }
        if (gameData.planechase != null && Objects.equals(gameData.planechase.controllerId, ownerId)) {
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof GraveyardAbilityGrantingEffect grant
                            && graveyardGrantApplies(gameData, ownerId, card, grant)) {
                        ActivatedAbility ability = grant.grantedGraveyardAbilityFor(card);
                        if (ability != null) abilities.add(ability);
                    }
                }
            }
        }
        return abilities;
    }

    private boolean graveyardGrantApplies(GameData gameData, UUID ownerId, Card card,
                                         GraveyardAbilityGrantingEffect grant) {
        CardPredicate filter = grant.cardFilter();
        return card != null && grant.appliesTo(card) && (filter == null
                || predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData, ownerId));
    }

    /**
     * Computes hand-activated abilities granted to a card by face-up permanents on any
     * battlefield. This supports effects worded as applying to cards in each player's hand.
     */
    public List<ActivatedAbility> computeGrantedHandAbilitiesForOwnedCard(GameData gameData, UUID ownerId,
                                                                           Card card) {
        List<ActivatedAbility> result = new ArrayList<>();
        if (card == null) return result;
        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (permanent.isFaceDown() || hasLostPrintedAbilities(gameData, permanent)) continue;
                for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                    if (effect instanceof HandAbilityGrantingEffect grant
                            && (!grant.controllerHandOnly() || controllerId.equals(ownerId))
                            && predicateEvaluationService.matchesCardPredicate(
                            card, grant.filter(), null, gameData, ownerId)) {
                        result.add(grant.grantedAbility().withGrantSource(permanent.getId()));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the madness cost granted to {@code card} by a permanent the owner controls
     * (e.g. Falkenrath Gorger), or empty if no grant applies. The cost equals the card's mana cost.
     * Native {@link com.github.laxika.magicalvibes.model.MadnessCast} is not consulted here.
     */
    public Optional<String> findGrantedMadnessCost(GameData gameData, UUID ownerId, Card card) {
        List<Permanent> bf = gameData.playerBattlefields.get(ownerId);
        if (bf == null || card == null || card.isToken()) {
            return Optional.empty();
        }
        for (Permanent perm : bf) {
            if (perm.isFaceDown() || hasLostPrintedAbilities(gameData, perm)) {
                continue;
            }
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
                if (effect instanceof MadnessGrantingEffect g
                        && predicateEvaluationService.matchesCardPredicate(
                                card, g.madnessGrantFilter(), null, gameData, ownerId)) {
                    return Optional.ofNullable(card.getManaCost());
                }
            }
        }
        return Optional.empty();
    }

    /** Returns the prowl alternate cast granted to a matching spell by a permanent its controller controls. */
    public Optional<AlternateHandCast> findGrantedProwlAlternateCast(GameData gameData, UUID playerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || card == null || card.isToken()) {
            return Optional.empty();
        }
        for (Permanent permanent : battlefield) {
            if (permanent.isFaceDown() || hasLostAllAbilities(gameData, permanent)) {
                continue;
            }
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                CardEffect activeEffect = staticEffectConditionResolver.resolve(
                        gameData, permanent, playerId, effect);
                if (activeEffect instanceof ProwlGrantingEffect grant
                        && predicateEvaluationService.matchesCardPredicate(
                        card, grant.prowlGrantFilter(), null, gameData, playerId)) {
                    Set<CardSubtype> creatureTypes = getCardSubtypes(card, gameData, playerId).stream()
                            .filter(this::isCreatureSubtype)
                            .collect(java.util.stream.Collectors.toSet());
                    if (!creatureTypes.isEmpty()) {
                        return Optional.of(new AlternateHandCast(
                                List.of(new ManaCastingCost(grant.prowlCost())), creatureTypes));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /** Returns the freerunning alternate cast granted to a matching spell by a permanent its controller controls. */
    public Optional<AlternateHandCast> findGrantedFreerunningAlternateCast(
            GameData gameData, UUID playerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || card == null || card.isToken()) {
            return Optional.empty();
        }
        for (Permanent permanent : battlefield) {
            if (permanent.isFaceDown() || hasLostAllAbilities(gameData, permanent)) {
                continue;
            }
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                CardEffect activeEffect = staticEffectConditionResolver.resolve(
                        gameData, permanent, playerId, effect);
                if (activeEffect instanceof FreerunningGrantingEffect grant
                        && predicateEvaluationService.matchesCardPredicate(
                        card, grant.freerunningGrantFilter(), null, gameData, playerId)) {
                    return Optional.of(new AlternateHandCast(
                            List.of(new ManaCastingCost(grant.freerunningCost())),
                            new Freerunning(), false));
                }
            }
        }
        return Optional.empty();
    }

    /** Returns the blitz alternate cast granted to a matching creature spell by a static source. */
    public Optional<AlternateHandCast> findGrantedBlitzAlternateCast(
            GameData gameData, UUID playerId, Card card) {
        return findGrantedBlitzAlternateCast(gameData, playerId, card, null);
    }

    /**
     * As {@link #findGrantedBlitzAlternateCast(GameData, UUID, Card)}, evaluating the grant's filter
     * with the X value chosen for the spell (CR 202.3e: X counts toward mana value on the stack).
     */
    public Optional<AlternateHandCast> findGrantedBlitzAlternateCast(
            GameData gameData, UUID playerId, Card card, Integer xValue) {
        if (card == null || card.isToken()) {
            return Optional.empty();
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (permanent.isFaceDown() || hasLostAllAbilities(gameData, permanent)) {
                    continue;
                }
                for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                    CardEffect activeEffect = staticEffectConditionResolver.resolve(
                            gameData, permanent, playerId, effect);
                    if (activeEffect instanceof BlitzGrantingEffect grant
                            && predicateEvaluationService.matchesCardPredicate(
                            card, grant.blitzGrantFilter(), null, gameData, playerId, null, null, xValue)
                            && (grant.blitzCost() != null || card.getManaCost() != null)) {
                        return Optional.of(AlternateHandCast.blitz(
                                grant.blitzCost() != null ? grant.blitzCost() : card.getManaCost()));
                    }
                }
            }
        }
        if (gameData.planechase != null && Objects.equals(gameData.planechase.controllerId, playerId)) {
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof BlitzGrantingEffect grant
                            && predicateEvaluationService.matchesCardPredicate(
                            card, grant.blitzGrantFilter(), null, gameData, playerId, null, null, xValue)
                            && (grant.blitzCost() != null || card.getManaCost() != null)) {
                        return Optional.of(AlternateHandCast.blitz(
                                grant.blitzCost() != null ? grant.blitzCost() : card.getManaCost()));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * The alternative cost {@code card} has without having it printed: its first perpetual one
     * ({@code GameData.perpetualAlternateCasts}: evoke or a plain "rather than pay this spell's mana cost"), else the
     * evoke a permanent of {@code playerId}'s grants it.
     */
    public Optional<GrantedAlternateCast> findGrantedAlternateCast(GameData gameData, UUID playerId, Card card) {
        List<GrantedAlternateCast> perpetual = card == null
                ? null : gameData.perpetualAlternateCasts.get(card.getId());
        if (perpetual != null && !perpetual.isEmpty()) {
            return Optional.of(perpetual.getFirst());
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || card == null || card.isToken()) {
            return Optional.empty();
        }
        for (Permanent permanent : battlefield) {
            if (permanent.isFaceDown() || gameData.losesAllAbilitiesByEffect(permanent)) {
                continue;
            }
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                CardEffect activeEffect = staticEffectConditionResolver.resolve(
                        gameData, permanent, playerId, effect);
                if (activeEffect instanceof EvokeGrantingEffect grant
                        && predicateEvaluationService.matchesCardPredicate(
                        card, grant.evokeGrantFilter(), null, gameData, playerId)) {
                    return Optional.of(new GrantedAlternateCast(new AlternateHandCast(
                            List.of(new ManaCastingCost(grant.evokeCost()))), true));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the miracle cost granted to {@code card} by a permanent the drawing player controls,
     * or empty if no grant applies. Native miracle is intentionally not consulted here.
     */
    public Optional<String> findGrantedMiracleCost(GameData gameData, UUID playerId, Card card) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null || card == null || card.isToken()) {
            return Optional.empty();
        }
        for (Permanent perm : bf) {
            if (perm.isFaceDown() || hasLostPrintedAbilities(gameData, perm)) {
                continue;
            }
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
                if (effect instanceof MiracleGrantingEffect grant
                        && predicateEvaluationService.matchesCardPredicate(
                                card, grant.miracleGrantFilter(), null, gameData, playerId)) {
                    return Optional.ofNullable(grant.miracleCostFor(card));
                }
            }
        }
        return Optional.empty();
    }

    /** Returns the unused generic reduction that a granted miracle cost applies to chosen X. */
    public int findGrantedMiracleXReduction(GameData gameData, UUID playerId, Card card) {
        if (card == null || card.isToken()) return 0;
        for (ActiveStaticEffect<MiracleGrantingEffect> active
                : activeStaticEffects(gameData, playerId, MiracleGrantingEffect.class)) {
            MiracleGrantingEffect grant = active.effect();
            if (predicateEvaluationService.matchesCardPredicate(
                    card, grant.miracleGrantFilter(), null, gameData, playerId)) {
                return grant.miracleXReductionFor(card);
            }
        }
        return 0;
    }

    // --- Player queries ---

    /**
     * Returns the opponent's player ID in a two-player game.
     */
    public UUID getOpponentId(GameData gameData, UUID playerId) {
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        return ids.get(0).equals(playerId) ? ids.get(1) : ids.get(0);
    }

    /**
     * Returns the player ID of whoever currently has priority: the tracked priority holder
     * (the active player unless someone acted while holding priority, CR 117.3c), moving on in
     * turn order past players who have passed. Returns {@code null} if every player has passed in
     * succession or a subgame is in progress.
     */
    public UUID getPriorityPlayerId(GameData data) {
        if (data.waitingForSubgame) return null;
        return data.currentPriorityPlayerId();
    }

    /**
     * Returns {@code true} if the player's life total is allowed to change (i.e. no
     * {@link LifeTotalCantChangeEffect} is present on their battlefield and no temporary player
     * effect prevents life-total changes).
     */
    public boolean canPlayerLifeChange(GameData gameData, UUID playerId) {
        return !gameData.hasFloatingEffectOnPlayer(playerId, LifeTotalCantChangeEffect.class)
                && !playerBattlefieldHasStaticEffect(gameData, playerId, LifeTotalCantChangeEffect.class);
    }

    /** Returns whether the player can lose life, including damage and life payments. */
    public boolean canPlayerLoseLife(GameData gameData, UUID playerId) {
        return canPlayerLifeChange(gameData, playerId)
                && !gameData.hasFloatingEffectOnPlayer(playerId, PlayersCantLoseLifeThisTurnEffect.class);
    }

    /**
     * Returns {@code true} if the player is able to gain life (i.e. no {@link PlayersCantGainLifeEffect} naming them
     * is active, and no {@link LifeTotalCantChangeEffect} prevents life changes).
     */
    public boolean canPlayerGainLife(GameData gameData, UUID playerId) {
        if (!canPlayerLifeChange(gameData, playerId)) return false;
        if (gameData.hasFloatingEffectOnPlayer(playerId, PlayersCantGainLifeRestOfGameEffect.class,
                PlayersCantGainLifeThisTurnEffect.class)) return false;
        return !staticEffectNamesPlayer(gameData, playerId, PlayersCantGainLifeEffect.class,
                PlayersCantGainLifeEffect::players);
    }

    /**
     * Whether an active static ability of {@code type} names {@code playerId} with its players. A face-down permanent
     * (CR 708.2a) and one that lost its printed abilities (CR 613.1f) have none.
     */
    private <T extends CardEffect> boolean staticEffectNamesPlayer(GameData gameData, UUID playerId, Class<T> type,
                                                                   Function<T, PlayerSubject> players) {
        for (Map.Entry<UUID, List<Permanent>> battlefield : gameData.playerBattlefields.entrySet()) {
            for (Permanent permanent : battlefield.getValue()) {
                if (!printsStaticEffect(gameData, permanent, type, ConditionalEffect.class, SequenceEffect.class)) {
                    continue;
                }
                for (CardEffect effect : staticEffectsIncludingTemporary(gameData, permanent, battlefield.getKey())) {
                    if (type.isInstance(effect) && SubjectResolutionService.namesPlayer(
                            permanent, battlefield.getKey(), playerId, players.apply(type.cast(effect)))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if players may pay life or sacrifice creatures as a cost of casting a
     * spell or activating an ability (i.e. neither the creature-only nor the nonland-permanent
     * restriction is present on any battlefield). Costs demanded by a resolving effect are never
     * restricted.
     */
    public boolean canPayLifeOrSacrificeCreaturesForCosts(GameData gameData) {
        return canPayLifeForCosts(gameData) && canSacrificeCreaturesForCosts(gameData);
    }

    /**
     * Returns whether life may be paid as a spell or ability cost. Karn's Sylex does not prohibit
     * life payments used to activate mana abilities, so that distinction is supplied by callers.
     */
    public boolean canPayLifeForCosts(GameData gameData) {
        return canPayLifeForCosts(gameData, false);
    }

    public boolean canPayLifeForCosts(GameData gameData, boolean manaAbility) {
        return !gameData.hasFloatingEffectOnPlayer(null, PlayersCantLoseLifeThisTurnEffect.class)
                && costProhibitions(gameData).stream()
                .noneMatch(prohibition -> !manaAbility || prohibition.includingManaAbilities());
    }

    /**
     * The active "players can't pay life [or sacrifice …] to cast spells or activate abilities" statics (Karn's Sylex,
     * Angel of Jubilation, Yasharn); a face-down permanent and one that lost its abilities have none.
     */
    private List<PlayersCantPayLifeEffect> costProhibitions(GameData gameData) {
        if (!anyBattlefieldPrintsStaticEffect(gameData, PlayersCantPayLifeEffect.class)) {
            return List.of();
        }
        return activeStaticEffects(gameData, PlayersCantPayLifeEffect.class).stream()
                .map(ActiveStaticEffect::effect)
                .toList();
    }

    /**
     * Returns whether a creature sacrifice is allowed as a cost: no prohibition names creatures as such (Angel of
     * Jubilation). Yasharn's nonland-permanent prohibition is not one, because a land creature remains a legal
     * sacrifice under it; {@link #canSacrificePermanentForCosts} judges each permanent.
     */
    public boolean canSacrificeCreaturesForCosts(GameData gameData) {
        return costProhibitions(gameData).stream()
                .noneMatch(prohibition -> prohibition.sacrificeFilter() instanceof PermanentIsCreaturePredicate);
    }

    /**
     * Returns whether {@code permanent} may be sacrificed as a cost. The permanent's current
     * characteristics are used, so a land creature is still permitted by Yasharn while a nonland
     * creature is not. A permanent already chosen for the cost being paid can't be chosen again.
     */
    public boolean canSacrificePermanentForCosts(GameData gameData, Permanent permanent) {
        if (permanent == null) {
            return false;
        }
        if (gameData.pendingActivationCostSacrificeIds.contains(permanent.getId())) {
            return false;
        }
        if (cantBeSacrificed(gameData, permanent)) {
            return false;
        }
        return costProhibitions(gameData).stream().noneMatch(prohibition -> prohibition.sacrificeFilter() != null
                && predicateEvaluationService.matchesPermanentPredicate(gameData, permanent,
                prohibition.sacrificeFilter()));
    }

    /**
     * Returns whether {@code permanent} may be chosen to pay a sacrifice cost that involves a
     * choice ("Sacrifice a creature", "Sacrifice two artifacts", "Sacrifice any number of lands"):
     * it may be sacrificed as a cost and it can't be chosen is not true of it (Glimpse, the
     * Unthinkable). Costs that name one object ("Sacrifice this") or every matching permanent
     * ("Sacrifice all lands you control") involve no choice and use
     * {@link #canSacrificePermanentForCosts} alone.
     */
    public boolean canChooseToSacrificeForCosts(GameData gameData, Permanent permanent) {
        return canSacrificePermanentForCosts(gameData, permanent) && !cantBeChosen(gameData, permanent);
    }

    /**
     * As {@link #canChooseToSacrificeForCosts(GameData, Permanent)} for a sacrifice cost described
     * by {@code filter}, except that a filter naming one specific object involves no choice, so
     * only {@link #canSacrificePermanentForCosts} applies to it.
     */
    public boolean canChooseToSacrificeForCosts(GameData gameData, Permanent permanent,
                                                com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter) {
        return designatesSpecificObject(filter)
                ? canSacrificePermanentForCosts(gameData, permanent)
                : canChooseToSacrificeForCosts(gameData, permanent);
    }

    /**
     * Returns whether {@code permanent} may be sacrificed to a resolving spell or ability's
     * "sacrifice unless" or upkeep sacrifice. The sacrifice restrictions of
     * {@link #canSacrificePermanentForCosts} only govern costs of casting spells and activating
     * abilities, so they do not apply here; "can't be sacrificed" effects still do.
     */
    public boolean canSacrificePermanentForEffect(GameData gameData, Permanent permanent) {
        return permanent != null && !cantBeSacrificed(gameData, permanent);
    }

    /** Returns whether the player controls a static effect that replaces life payments. */
    public boolean hasLifePaymentReplacement(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, LifePaymentReplacementEffect.class);
    }

    /**
     * Returns {@code true} if a resolving spell or ability controlled by {@code sourceControllerId}
     * may cause {@code playerId} to sacrifice permanents. A player who controls an
     * {@link OpponentEffectsCantCauseSacrificeEffect} (Sigarda, Host of Herons) can't be forced to
     * sacrifice by an opponent's effect; their own effects still work normally.
     */
    public boolean canEffectCauseSacrifice(GameData gameData, UUID playerId, UUID sourceControllerId) {
        if (playerId == null || playerId.equals(sourceControllerId)) {
            return true;
        }
        return !playerBattlefieldHasStaticEffect(gameData, playerId, OpponentEffectsCantCauseSacrificeEffect.class);
    }

    /**
     * Returns whether a triggered ability currently resolving may sacrifice or exile the specified
     * creature token. The restriction applies only when the ability controller also controls the
     * token and a source of the matching static effect.
     */
    public boolean triggeredAbilityCanMoveCreatureToken(GameData gameData, Permanent permanent) {
        if (!isCreatureToken(gameData, permanent)) {
            return true;
        }
        UUID abilityControllerId = resolvingTriggeredAbilityControllerId(gameData);
        if (abilityControllerId == null) {
            return true;
        }
        UUID tokenControllerId = findPermanentController(gameData, permanent.getId());
        return !abilityControllerId.equals(tokenControllerId)
                || !playerBattlefieldHasStaticEffect(
                        gameData, tokenControllerId,
                        TriggeredAbilitiesCantCauseSacrificeOrExileCreatureTokensEffect.class);
    }

    /**
     * Whether a static ability such as The Master, Multiplied's stops the currently resolving
     * triggered ability from making its controller sacrifice this creature token; the inverse of
     * {@link #triggeredAbilityCanMoveCreatureToken}.
     */
    public boolean isCreatureTokenProtectedFromResolvingTriggeredAbility(GameData gameData, Permanent permanent) {
        return !triggeredAbilityCanMoveCreatureToken(gameData, permanent);
    }

    private static UUID resolvingTriggeredAbilityControllerId(GameData gameData) {
        UUID abilityControllerId = gameData.currentlyResolvingTriggeredAbilityControllerId;
        if (abilityControllerId == null && gameData.pendingEffectResolutionEntry != null
                && gameData.pendingEffectResolutionEntry.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            abilityControllerId = gameData.pendingEffectResolutionEntry.getControllerId();
        }
        return abilityControllerId;
    }

    /**
     * Returns whether a delayed triggered ability may move this creature token. As for any triggered
     * ability, the restriction only protects tokens controlled by the player who controls the
     * resolving ability (e.g. a token stolen from a myriad controller is no longer protected from it).
     */
    public boolean delayedTriggeredAbilityCanMoveCreatureToken(GameData gameData, Permanent permanent) {
        if (!isCreatureToken(gameData, permanent)) {
            return true;
        }
        UUID tokenControllerId = findPermanentController(gameData, permanent.getId());
        UUID abilityControllerId = resolvingTriggeredAbilityControllerId(gameData);
        if (abilityControllerId != null && !abilityControllerId.equals(tokenControllerId)) {
            return true;
        }
        return tokenControllerId == null || !playerBattlefieldHasStaticEffect(
                gameData, tokenControllerId,
                TriggeredAbilitiesCantCauseSacrificeOrExileCreatureTokensEffect.class);
    }

    private boolean isCreatureToken(GameData gameData, Permanent permanent) {
        return permanent != null && permanent.getCard().isToken() && isCreature(gameData, permanent);
    }

    /**
     * Returns {@code true} if a resolving spell or ability controlled by {@code sourceControllerId}
     * may cause {@code playerId} to discard cards. A player who controls an
     * {@link OpponentEffectsCantCauseDiscardEffect} can't be forced to discard by an opponent's
     * effect; their own effects and discard costs still work normally.
     */
    public boolean canEffectCauseDiscard(GameData gameData, UUID playerId, UUID sourceControllerId) {
        if (playerId == null || playerId.equals(sourceControllerId)) {
            return true;
        }
        return !playerBattlefieldHasStaticEffect(gameData, playerId, OpponentEffectsCantCauseDiscardEffect.class);
    }

    /** Returns whether an opponent-caused discard is currently prevented for the player. */
    public boolean isDiscardPrevented(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, OpponentEffectsCantCauseDiscardEffect.class);
    }

    /**
     * Returns {@code true} if a life-gain event affecting {@code playerId} must instead become an
     * equal amount of life loss, because one of that player's opponents controls an
     * {@link OpponentLifeGainBecomesLifeLossEffect} (Tainted Remedy).
     */
    public boolean lifeGainBecomesLifeLoss(GameData gameData, UUID playerId) {
        for (UUID otherId : gameData.playerBattlefields.keySet()) {
            if (otherId.equals(playerId)) continue;
            if (playerBattlefieldHasStaticEffect(gameData, otherId, OpponentLifeGainBecomesLifeLossEffect.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a life gain for {@code playerId} caused by a spell or ability becomes life
     * loss because that player is the source's controller and Rain of Gore is on the battlefield.
     */
    public boolean lifeGainFromSpellOrAbilityBecomesLifeLoss(GameData gameData, UUID playerId,
                                                              UUID sourceControllerId) {
        return playerId != null
                && playerId.equals(sourceControllerId)
                && anyBattlefieldHasStaticEffect(gameData, RainOfGoreEffect.class);
    }

    /** Returns whether the player's radiation life loss is replaced by equal life gain. */
    public boolean radiationLifeLossBecomesLifeGain(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, RadiationLifeGainReplacementEffect.class);
    }

    /**
     * Returns the multiplier applied to life the given player gains, per any static life-gain
     * replacement effects they control. Multiple replacements stack multiplicatively.
     */
    public int lifeGainMultiplier(GameData gameData, UUID playerId) {
        int multiplier = 1;
        for (ActiveStaticEffect<LifeGainReplacementEffect> active
                : activeStaticEffects(gameData, playerId, LifeGainReplacementEffect.class)) {
            multiplier *= MaroGoneNutsSupport.apply(
                    gameData, active.effect(), active.effect().lifeGainMultiplier());
        }
        return multiplier;
    }

    /**
     * Returns the additional life applied to each positive life-gain event by static replacement
     * effects controlled by the player.
     */
    public int additionalLifeGain(GameData gameData, UUID playerId) {
        int additional = 0;
        for (ActiveStaticEffect<LifeGainReplacementEffect> active
                : activeStaticEffects(gameData, playerId, LifeGainReplacementEffect.class)) {
            additional += active.effect().additionalLifeGain();
        }
        return additional;
    }

    /**
     * Returns the multiplier applied to life loss by an opponent of the active player. Multiple
     * replacement effects stack multiplicatively.
     */
    public int opponentLifeLossMultiplier(GameData gameData, UUID playerId) {
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null || activePlayerId.equals(playerId)) return 1;

        int multiplier = 1;
        for (ActiveStaticEffect<OpponentLifeLossReplacementEffect> active
                : activeStaticEffects(gameData, activePlayerId, OpponentLifeLossReplacementEffect.class)) {
            multiplier *= MaroGoneNutsSupport.apply(
                    gameData, active.effect(), active.effect().lifeLossMultiplier());
        }
        return multiplier;
    }

    /**
     * Returns the multiplier applied to mana the given player produces by tapping a permanent for
     * mana, per matching {@link ManaReflectionEffect} static effects they control. Each effect
     * applies its configured multiplier, and multiple effects stack multiplicatively. The legacy
     * overload matches every permanent and is retained for callers that do not have a specific
     * mana source.
     */
    public int manaProductionMultiplier(GameData gameData, UUID playerId) {
        return manaProductionMultiplier(gameData, playerId, null);
    }

    public int manaProductionMultiplier(GameData gameData, UUID playerId, Permanent tappedPermanent) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return 1;
        int multiplier = 1;
        for (Permanent perm : bf) {
            if (hasLostAllAbilities(gameData, perm)) continue;
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
                if (effect instanceof ManaReflectionEffect reflection
                        && (tappedPermanent == null
                        || predicateEvaluationService.matchesPermanentPredicate(
                        gameData, tappedPermanent, reflection.permanentFilter()))) {
                    multiplier *= MaroGoneNutsSupport.apply(
                            gameData, effect, reflection.multiplier());
                }
            }
        }
        return multiplier;
    }

    /**
     * Basic-land-type to color remappings contributed by every {@link TwistBasicLandManaColorsEffect}
     * on the battlefield (Reality Twist, Naked Singularity). Global â€” not controller-scoped. When
     * several such effects are active a type may map to more than one color, and the controller
     * chooses among them.
     */
    private Map<CardSubtype, Set<ManaColor>> activeTwistLandManaMappings(GameData gameData) {
        Map<CardSubtype, Set<ManaColor>> mappings = new EnumMap<>(CardSubtype.class);
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent source : battlefield) {
                if (hasLostAllAbilities(gameData, source)) {
                    continue;
                }
                for (CardEffect effect : source.getCard().getStaticEffects()) {
                    if (effect instanceof TwistBasicLandManaColorsEffect twist) {
                        twist.landColorMapping().forEach((type, color) -> mappings
                                .computeIfAbsent(type, t -> EnumSet.noneOf(ManaColor.class))
                                .add(color));
                    }
                }
            }
        }
        return mappings;
    }

    /**
     * One {@link ReplaceLandManaWithColorEffect} that applies to a land: a static ability of a permanent
     * on the battlefield, or a floating effect a resolved spell or ability made.
     *
     * @param key          stable for as long as the replacement applies: the source and the index of the
     *                     static ability, or the floating effect's id
     * @param sourceName   the source's public name, for prompt labels
     * @param controllerId the player the effect's "you" means
     */
    public record LandManaColorReplacement(String key, String sourceName, UUID controllerId,
                                           ReplaceLandManaWithColorEffect effect) {}

    /**
     * The {@link ReplaceLandManaWithColorEffect}s that would apply to {@code land} tapped for mana by
     * {@code tappingPlayerId} (the land's controller when {@code null}), static ones first in seat
     * order, then floating ones oldest first. A source that lost its abilities applies none. Whether a
     * {@code replacedTypes} replacement applies also depends on the mana the tap makes, which the
     * caller checks.
     */
    public List<LandManaColorReplacement> landManaColorReplacements(GameData gameData, Permanent land,
                                                                   UUID tappingPlayerId) {
        List<LandManaColorReplacement> replacements = new ArrayList<>();
        if (gameData == null || land == null) {
            return replacements;
        }
        UUID landControllerId = findPermanentController(gameData, land.getId());
        UUID tapperId = tappingPlayerId != null ? tappingPlayerId : landControllerId;
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent source : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (hasLostAllAbilities(gameData, source)) {
                    continue;
                }
                List<CardEffect> statics = source.getCard().getStaticEffects();
                for (int i = 0; i < statics.size(); i++) {
                    if (statics.get(i) instanceof ReplaceLandManaWithColorEffect replacement
                            && replacement.duration() == null
                            && landManaReplacementApplies(gameData, replacement, playerId, land, tapperId, false)) {
                        replacements.add(new LandManaColorReplacement(source.getId() + ":" + i,
                                FaceDownVisibility.publicName(source), playerId, replacement));
                    }
                }
            }
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (!(floating.effect() instanceof ReplaceLandManaWithColorEffect replacement)) {
                    continue;
                }
                boolean onLand = floating.affectedPermanentId() != null;
                if (onLand && !land.getId().equals(floating.affectedPermanentId())) {
                    continue;
                }
                if (landManaReplacementApplies(gameData, replacement, floating.controllerId(), land, tapperId,
                        onLand)) {
                    replacements.add(new LandManaColorReplacement(floating.id().toString(),
                            floating.sourceCardName(), floating.controllerId(), replacement));
                }
            }
        }
        return replacements;
    }

    private boolean landManaReplacementApplies(GameData gameData, ReplaceLandManaWithColorEffect replacement,
                                               UUID controllerId, Permanent land, UUID tapperId, boolean onLand) {
        if (replacement.onlyYourTaps() && !Objects.equals(controllerId, tapperId)) {
            return false;
        }
        return onLand || replacement.lands() == null || predicateEvaluationService.matchesPermanentPredicate(land,
                replacement.lands(), FilterContext.of(gameData).withSourceControllerId(controllerId));
    }

    /**
     * The one color {@code permanent} produces under a land-mana color replacement, newest first, for
     * previews that can't order replacements (CR 616.1); {@code null} when none fixes its color.
     */
    public ManaColor fixedLandManaColor(GameData gameData, Permanent permanent) {
        List<LandManaColorReplacement> replacements = landManaColorReplacements(gameData, permanent, null);
        for (int i = replacements.size() - 1; i >= 0; i--) {
            ReplaceLandManaWithColorEffect effect = replacements.get(i).effect();
            if (effect.color() != null && (effect.replacedTypes().isEmpty()
                    || effect.replacedTypes().containsAll(ManaColor.COLORS))) {
                return effect.color();
            }
        }
        return null;
    }

    /** Whether a land-mana replacement lets {@code permanent}'s mana be a color of its tapper's choice. */
    public boolean landManaProducesColorOfChoice(GameData gameData, Permanent permanent) {
        return gameData != null && permanent != null && isLand(gameData, permanent)
                && landManaColorReplacements(gameData, permanent, null).stream()
                .anyMatch(replacement -> replacement.effect().color() == null);
    }

    /**
     * Mana colors a land produces under a Reality Twist-style replacement, based on its effective
     * basic land types and the remappings of every {@link TwistBasicLandManaColorsEffect} on the
     * battlefield. Empty when no such effect is active or the permanent has no remapped basic land
     * type. When multiple colors apply the controller chooses one for all mana produced
     * (Gatherer 2013-04-15).
     */
    public Set<ManaColor> twistedLandManaColors(GameData gameData, Permanent permanent) {
        Map<CardSubtype, Set<ManaColor>> mappings = activeTwistLandManaMappings(gameData);
        if (mappings.isEmpty()) {
            return Set.of();
        }
        Set<CardSubtype> types = effectiveBasicLandTypes(gameData, permanent);
        if (types.isEmpty()) {
            return Set.of();
        }
        Set<ManaColor> colors = EnumSet.noneOf(ManaColor.class);
        for (CardSubtype type : types) {
            Set<ManaColor> replacements = mappings.get(type);
            if (replacements != null) {
                colors.addAll(replacements);
            } else {
                ManaColor intrinsicColor = switch (type) {
                    case PLAINS -> ManaColor.WHITE;
                    case ISLAND -> ManaColor.BLUE;
                    case SWAMP -> ManaColor.BLACK;
                    case MOUNTAIN -> ManaColor.RED;
                    case FOREST -> ManaColor.GREEN;
                    default -> null;
                };
                if (intrinsicColor != null) {
                    colors.add(intrinsicColor);
                }
            }
        }
        return colors;
    }

    /**
     * Whether a restriction on {@code playerId} matching {@code stops} applies now: a resolved one
     * ({@code GameData.restrictionsOnPlayer}, a {@link PlayerRestrictionEffect}'s) or an active static one
     * ({@link PlayersCantCastSpellsFromZonesEffect}) whose players name them. A face-down permanent and one that lost its
     * abilities restrict nobody (CR 708.2a, 613.1f); a conditional static restricts while its condition holds.
     */
    public boolean isPlayerRestricted(GameData gameData, UUID playerId, Predicate<PlayerRestriction> stops) {
        return findRestriction(gameData, playerId, stops) != null;
    }

    /**
     * A restriction on {@code playerId} in force now that matches {@code stops}, as {@link #isPlayerRestricted} finds it,
     * or {@code null}: a resolved one, an active {@link PlayersCantCastSpellsFromZonesEffect} naming them, or an active
     * {@link OpponentsCantCastOrActivateDuringYourTurnEffect} lock naming them whose condition is met for them (the
     * condition is judged with the restricted player as the context's target).
     */
    public RestrictionInForce findRestriction(GameData gameData, UUID playerId, Predicate<PlayerRestriction> stops) {
        for (PlayerRestriction restriction : gameData.restrictionsOnPlayer(playerId)) {
            if (stops.test(restriction)) {
                return new RestrictionInForce(restriction, null);
            }
        }
        for (ActiveStaticEffect<PlayersCantCastSpellsFromZonesEffect> active
                : activeStaticEffects(gameData, PlayersCantCastSpellsFromZonesEffect.class)) {
            if (stops.test(active.effect().restriction()) && SubjectResolutionService.namesPlayer(
                    active.source(), active.controllerId(), playerId, active.effect().players())) {
                return new RestrictionInForce(active.effect().restriction(), null);
            }
        }
        for (ActiveStaticEffect<OpponentsCantCastOrActivateDuringYourTurnEffect> active
                : activeStaticEffects(gameData, OpponentsCantCastOrActivateDuringYourTurnEffect.class)) {
            OpponentsCantCastOrActivateDuringYourTurnEffect lock = active.effect();
            boolean matches = lock.spells() != null && stops.test(lock.spells())
                    || lock.abilities() != null && stops.test(lock.abilities());
            if (matches && castLockHolds(gameData, active, playerId)) {
                return new RestrictionInForce(lock.spells() != null && stops.test(lock.spells())
                        ? lock.spells() : lock.abilities(), lock);
            }
        }
        return null;
    }

    /**
     * The card types of the spells {@code playerId} can't cast now by the restrictions in force on them ("can't cast
     * instant spells": Cease-Fire's resolved restriction, Hand to Hand during combat, Priority Avenger while the stack
     * is empty). A restriction on every spell is not a type set ({@link #isPlayerRestricted} with
     * {@link PlayerRestriction#stopsEverySpell()}).
     */
    public Set<CardType> restrictedSpellTypes(GameData gameData, UUID playerId) {
        Set<CardType> types = EnumSet.noneOf(CardType.class);
        for (PlayerRestriction restriction : gameData.restrictionsOnPlayer(playerId)) {
            if (restriction.kind() == PlayerRestriction.Kind.CAST_SPELLS) {
                types.addAll(restriction.spellTypes());
            }
        }
        for (ActiveStaticEffect<OpponentsCantCastOrActivateDuringYourTurnEffect> active
                : activeStaticEffects(gameData, OpponentsCantCastOrActivateDuringYourTurnEffect.class)) {
            PlayerRestriction spells = active.effect().spells();
            if (spells != null && !spells.spellTypes().isEmpty() && castLockHolds(gameData, active, playerId)) {
                types.addAll(spells.spellTypes());
            }
        }
        return types;
    }

    /**
     * Why {@code playerId} can't activate any activated ability of {@code source} now, mana abilities included, or
     * {@code null}: a restriction on every ability (City of Solitude) or on abilities of permanents of some card types
     * (Grand Abolisher; judged by the permanent's types after effects, so it never matches an ability of a card in
     * another zone or of a spell, whose {@code source} is {@code null}).
     */
    public String abilityLockRefusal(GameData gameData, UUID playerId, Permanent source) {
        return refusal(findRestriction(gameData, playerId,
                restriction -> restriction.kind() == PlayerRestriction.Kind.ACTIVATE_ABILITIES
                        && (restriction.abilitySourceTypes().isEmpty() || source != null
                        && restriction.abilitySourceTypes().stream().anyMatch(type -> type == CardType.CREATURE
                        ? isCreature(gameData, source) : hasEffectiveCardType(gameData, source, type)))));
    }

    /**
     * Why {@code playerId} can't activate abilities that aren't mana abilities now (loyalty abilities included), or
     * {@code null}: a resolved restriction (Abeyance) or a static lock (Hand to Hand during combat).
     */
    public String nonManaAbilityLockRefusal(GameData gameData, UUID playerId) {
        return refusal(findRestriction(gameData, playerId,
                restriction -> restriction.kind() == PlayerRestriction.Kind.ACTIVATE_NON_MANA_ABILITIES));
    }

    private static String refusal(RestrictionInForce found) {
        if (found == null) {
            return null;
        }
        return "You can't " + found.restriction().description()
                + (found.lock() == null ? " this turn" : found.lock().whenRestricted());
    }

    /**
     * Whether a lock holds {@code playerId} to sorcery timing now
     * ({@link OpponentsCantCastOrActivateDuringYourTurnEffect#holdsToSorceryTiming()}; Teferi): they may cast spells
     * only in their main phase with an empty stack, and never while another spell or ability resolves.
     */
    public boolean isHeldToSorceryTiming(GameData gameData, UUID playerId) {
        for (ActiveStaticEffect<OpponentsCantCastOrActivateDuringYourTurnEffect> active
                : activeStaticEffects(gameData, OpponentsCantCastOrActivateDuringYourTurnEffect.class)) {
            if (active.effect().holdsToSorceryTiming() && SubjectResolutionService.namesPlayer(
                    active.source(), active.controllerId(), playerId, active.effect().players())) {
                return true;
            }
        }
        return false;
    }

    private boolean castLockHolds(GameData gameData,
                                  ActiveStaticEffect<OpponentsCantCastOrActivateDuringYourTurnEffect> active,
                                  UUID playerId) {
        OpponentsCantCastOrActivateDuringYourTurnEffect lock = active.effect();
        return SubjectResolutionService.namesPlayer(active.source(), active.controllerId(), playerId, lock.players())
                && (lock.condition() == null || conditionEvaluationService.isMet(gameData, lock.condition(),
                ConditionContext.forStaticEffect(active.source(), active.controllerId()).withTargetId(playerId)));
    }

    /**
     * A restriction in force on a player: the restriction, and the static lock it comes from ({@code null} for a
     * resolved restriction or a static zone restriction).
     */
    public record RestrictionInForce(PlayerRestriction restriction, OpponentsCantCastOrActivateDuringYourTurnEffect lock) {
    }

    /**
     * Whether {@code playerId} may cast spells from {@code zone} at all: no zone-bound "can't cast spells" or "can't
     * play cards" restriction covers it (Ashes of the Abhorrent, Grafdigger's Cage, Drannith Magistrate, Avatar's
     * Wrath, Shaman's Trance). A restriction that depends on the spell is judged by
     * {@link #canCastSpellFromZone(GameData, Card, Zone, UUID)}.
     */
    public boolean canPlayerCastSpellsFromZone(GameData gameData, UUID playerId, Zone zone) {
        return !isPlayerRestricted(gameData, playerId, restriction -> restriction.stopsSpellsFrom(zone));
    }

    /**
     * Whether {@code playerId} may cast {@code card} from {@code zone}: no zone-bound restriction stops that spell there
     * (a noncreature one for Soulless Jailer). A land card is played, not cast (CR 305.1): only a land or card-play
     * restriction covering the zone stops it.
     */
    public boolean canCastSpellFromZone(GameData gameData, Card card, Zone zone, UUID playerId) {
        if (card.hasType(CardType.LAND)) {
            return !isPlayerRestricted(gameData, playerId,
                    restriction -> restriction.isZoneBound() && restriction.stopsLandsFrom(zone));
        }
        boolean creatureSpell = card.hasType(CardType.CREATURE);
        return !isPlayerRestricted(gameData, playerId,
                restriction -> restriction.stopsSpellFrom(zone, creatureSpell));
    }

    /**
     * Returns {@code true} if the given card is barred from entering the battlefield from
     * {@code zone} by a {@link CardsCantEnterBattlefieldFromZonesEffect} on any battlefield
     * (e.g. Grafdigger's Cage). The card is tested against each such effect's filter, and the
     * effect must list {@code zone} in its {@code zones}, so only matching cards (e.g. creature
     * cards) entering from a blocked zone are stopped.
     */
    public boolean isCardBlockedFromEnteringFromZone(GameData gameData, Card card, Zone zone) {
        return gameData.anyPermanentMatches(p ->
                !p.isFaceDown() && !hasLostAllAbilities(gameData, p)
                        && p.getCard().getStaticEffects().stream()
                        .filter(CardsCantEnterBattlefieldFromZonesEffect.class::isInstance)
                        .map(CardsCantEnterBattlefieldFromZonesEffect.class::cast)
                        .anyMatch(e -> e.zones().contains(zone) && predicateEvaluationService.matchesCardPredicate(card, e.filter(), null)));
    }

    /**
     * Whether the card can't be put onto the battlefield face up as it is: an instant or sorcery card remains in the
     * zone it was in (CR 400.4a), also one that was a face-down permanent. One put onto the battlefield face down is
     * turned face down first (CR 708.3), so a caller putting it there face down doesn't ask.
     */
    public boolean cantEnterFaceUp(Card card) {
        return card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY);
    }

    /**
     * Returns {@code true} if the given card cannot be manifested from {@code zone}. A manifested card
     * enters face down as a 2/2 creature, so a {@link CardsCantEnterBattlefieldFromZonesEffect} whose
     * filter is creature-typed bars manifesting any card from that zone, noncreature cards included
     * (Grafdigger's Cage).
     */
    public boolean isManifestBlockedFromZone(GameData gameData, Card card, Zone zone) {
        if (isCardBlockedFromEnteringFromZone(gameData, card, zone)) {
            return true;
        }
        CardTypePredicate creatureFilter = new CardTypePredicate(CardType.CREATURE);
        return gameData.anyPermanentMatches(p ->
                !p.isFaceDown() && !hasLostAllAbilities(gameData, p)
                        && p.getCard().getStaticEffects().stream()
                        .filter(CardsCantEnterBattlefieldFromZonesEffect.class::isInstance)
                        .map(CardsCantEnterBattlefieldFromZonesEffect.class::cast)
                        .anyMatch(e -> e.zones().contains(zone) && creatureFilter.equals(e.filter())));
    }

    /**
     * Returns {@code true} if players are allowed to activate abilities of cards in graveyards.
     * Returns {@code false} when a {@link PlayersCantActivateAbilitiesOfGraveyardCardsEffect}
     * is on any battlefield (e.g. Ashes of the Abhorrent).
     */
    public boolean canPlayersActivateGraveyardAbilities(GameData gameData) {
        return !anyBattlefieldHasStaticEffect(gameData, PlayersCantActivateAbilitiesOfGraveyardCardsEffect.class);
    }

    /** Returns whether players may activate cycling abilities. */
    public boolean canPlayersCycleCards(GameData gameData) {
        return !anyBattlefieldHasStaticEffect(gameData, PlayersCantCycleCardsEffect.class);
    }

    /** Returns whether cards in graveyards currently have all of their abilities removed. */
    public boolean graveyardCardsHaveLostAllAbilities(GameData gameData) {
        return boardScan(gameData, new BoardScan(BoardScan.Kind.GRAVEYARD_CARDS_LOSE_ALL_ABILITIES, null), () ->
                gameData.anyPermanentMatches(permanent -> !permanent.isFaceDown()
                        && !gameData.losesAllAbilitiesByEffect(permanent)
                        && permanent.getCard().getStaticEffects().stream()
                        .anyMatch(GraveyardCardsLoseAllAbilitiesEffect.class::isInstance)
                        && !hasLayeredAllAbilitiesLost(gameData, permanent)));
    }

    /**
     * Returns whether a layer-6 "loses all abilities" effect (e.g. an Aura such as Lignify) currently
     * removes the permanent's abilities, which turns off its graveyard-wide static effect.
     */
    private boolean hasLayeredAllAbilitiesLost(GameData gameData, Permanent permanent) {
        CharacteristicState state = LayerSystemService.activeStateFor(permanent.getId());
        if (state == null && layerSystemService.activePass(gameData) == null) {
            LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
            try {
                state = LayerSystemService.activeStateFor(permanent.getId());
            } finally {
                layerSystemService.endPass(pass);
            }
        }
        return state != null && state.isLosesAllAbilities();
    }

    /**
     * The effects of a graveyard card's triggered abilities with {@code trigger} that function from the graveyard,
     * its perpetual grants included, respecting global ability loss.
     */
    public List<CardEffect> getEffectiveGraveyardTriggerEffects(GameData gameData, Card card, Trigger trigger) {
        if (graveyardCardsHaveLostAllAbilities(gameData)) {
            return List.of();
        }
        List<CardEffect> effects = new ArrayList<>(card.getTriggerEffects(trigger, Zone.GRAVEYARD));
        effects.addAll(gameData.perpetualTriggerEffects(card.getId(), trigger, Zone.GRAVEYARD));
        return List.copyOf(effects);
    }

    /**
     * The triggered abilities of a card in a graveyard, its own and those perpetually granted to it, in that order,
     * or none while cards in graveyards have lost all abilities: for a reader that reads abilities themselves.
     */
    public List<TriggeredAbility> getEffectiveGraveyardTriggeredAbilities(GameData gameData, Card card) {
        if (graveyardCardsHaveLostAllAbilities(gameData)) {
            return List.of();
        }
        List<TriggeredAbility> abilities = new ArrayList<>(card.getTriggeredAbilities());
        for (Ability ability : gameData.perpetualAbilities(card.getId())) {
            if (ability instanceof TriggeredAbility triggered) {
                abilities.add(triggered);
            }
        }
        return List.copyOf(abilities);
    }

    /** Like {@link #getEffectiveGraveyardEffects}, for the static abilities of a card in a graveyard. */
    public List<CardEffect> getEffectiveGraveyardStaticEffects(GameData gameData, Card card) {
        if (graveyardCardsHaveLostAllAbilities(gameData)) {
            return List.of();
        }
        List<CardEffect> effects = new ArrayList<>(card.getStaticEffects());
        effects.addAll(gameData.perpetualStaticEffects(card.getId()));
        return List.copyOf(effects);
    }

    /**
     * Returns {@code true} if cards in graveyards may be chosen as targets of spells and abilities.
     * Returns {@code false} when a {@link GraveyardCardsCantBeTargetedEffect} is on any battlefield
     * (e.g. Ground Seal). Non-targeting graveyard interaction is unaffected.
     */
    public boolean canGraveyardCardsBeTargeted(GameData gameData) {
        return !anyBattlefieldHasStaticEffect(gameData, GraveyardCardsCantBeTargetedEffect.class);
    }

    /**
     * Returns whether a land permanent is protected from spells and abilities controlled by the
     * given player through an opponent's static effect.
     */
    public boolean isLandTargetRestricted(GameData gameData, Permanent target, UUID sourcePlayerId) {
        return sourcePlayerId != null
                && isLand(gameData, target)
                && opponentControlsActiveStaticEffect(gameData, sourcePlayerId, OpponentsCantTargetLandsEffect.class);
    }

    /**
     * Returns whether a land card in a graveyard is protected from spells and abilities controlled
     * by the given player through an opponent's static effect.
     */
    public boolean isLandCardTargetRestricted(GameData gameData, Card target, UUID sourcePlayerId) {
        return sourcePlayerId != null
                && target != null
                && target.hasType(CardType.LAND)
                && opponentControlsActiveStaticEffect(gameData, sourcePlayerId, OpponentsCantTargetLandsEffect.class);
    }

    private boolean opponentControlsActiveStaticEffect(GameData gameData, UUID playerId,
                                                        Class<? extends CardEffect> effectType) {
        for (Map.Entry<UUID, List<Permanent>> entry : gameData.playerBattlefields.entrySet()) {
            if (entry.getKey().equals(playerId) || entry.getValue() == null) {
                continue;
            }
            for (Permanent permanent : entry.getValue()) {
                if (permanent.isFaceDown()
                        || gameData.losesAllAbilitiesByEffect(permanent)
                        || computeStaticBonus(gameData, permanent).losesAllAbilities()) {
                    continue;
                }
                if (permanent.getCard().getStaticEffects().stream().anyMatch(effectType::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the given player may activate abilities of creatures they control as
     * though those creatures had haste (i.e. they control a permanent with
     * {@link ActivateCreatureAbilitiesAsThoughHasteEffect}, e.g. Thousand-Year Elixir). This only
     * lifts the summoning sickness restriction on ability activation â€” it does not grant haste.
     */
    public boolean canActivateCreatureAbilitiesAsThoughHaste(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, ActivateCreatureAbilitiesAsThoughHasteEffect.class);
    }

    /**
     * Returns true if a static effect controlled by the player makes the given ability available
     * at instant speed.
     */
    public boolean canActivateAbilityAtInstantSpeed(GameData gameData, UUID playerId,
                                                    ActivatedAbility ability) {
        return activeStaticEffects(gameData, playerId, ActivatedAbilityTimingEffect.class).stream()
                .anyMatch(active -> active.effect().allowsInstantSpeedActivation(ability));
    }

    /**
     * Returns {@code true} if the given player may spend white mana as though it were red mana (i.e.
     * they control a permanent with {@link SpendWhiteManaAsRedEffect}, e.g. Sunglasses of Urza). Set
     * onto the player's {@code ManaPool} at the payment/affordability sites so {@code ManaCost} honors it.
     */
    public boolean canSpendWhiteManaAsRed(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, SpendWhiteManaAsRedEffect.class);
    }

    /**
     * Returns {@code true} if the given player may spend white mana as though it were mana of any
     * color and other mana only as though it were colorless (i.e. they control a permanent with
     * {@link SpendWhiteManaAsAnyColorEffect}, e.g. Celestial Dawn). Set onto the player's
     * {@code ManaPool} at the payment/affordability sites so {@code ManaCost} honors it.
     */
    public boolean canSpendWhiteManaAsAnyColor(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, SpendWhiteManaAsAnyColorEffect.class);
    }

    /**
     * Whether the player may spend white mana as though it were mana of any color, other mana keeping its own color: a
     * {@link SpendManaAsAnyColorEffect} of white mana that applies to them (False Dawn's floated one).
     */
    public boolean canSpendWhiteManaAsAnyColorWithoutRestriction(GameData gameData, UUID playerId) {
        return hasSpendManaAsAnyColorPermission(gameData, playerId, ManaColor.WHITE);
    }

    /** Whether a {@link SpendManaAsAnyColorEffect} of all mana applies to the player (Mycosynth Lattice, Chromatic Orrery). */
    public boolean canSpendManaAsAnyColor(GameData gameData, UUID playerId) {
        return hasSpendManaAsAnyColorPermission(gameData, playerId, null);
    }

    /**
     * Whether a {@link SpendManaAsAnyColorEffect} of {@code fromColor} mana ({@code null}: all mana) applies to the
     * player: printed on a permanent that hasn't lost its abilities (its controller's, or every player's), or floated on
     * the player.
     */
    private boolean hasSpendManaAsAnyColorPermission(GameData gameData, UUID playerId, ManaColor fromColor) {
        boolean floated = gameData.floatingEffectsOnPlayer(playerId, SpendManaAsAnyColorEffect.class).stream()
                .anyMatch(floating -> ((SpendManaAsAnyColorEffect) floating.effect()).fromColor() == fromColor);
        return floated || gameData.anyPermanentMatches(permanent ->
                !permanent.isFaceDown()
                        && !hasLostPrintedAbilities(gameData, permanent)
                        && permanent.getCard().getStaticEffects().stream()
                        .filter(SpendManaAsAnyColorEffect.class::isInstance)
                        .map(SpendManaAsAnyColorEffect.class::cast)
                        .filter(permission -> permission.fromColor() == fromColor)
                        .anyMatch(permission -> permission.allPlayers()
                                || playerId.equals(findPermanentController(gameData, permanent.getId()))));
    }

    /** Returns whether the given card's own perpetual permission allows any mana to cast it. */
    public boolean canSpendManaAsAnyColorToCastCard(GameData gameData, UUID playerId, Card card) {
        return canSpendManaAsAnyColor(gameData, playerId)
                || card != null && gameData.perpetualAnyColorManaForCastCardIds.contains(card.getId());
    }

    /** Returns whether the player controls a permanent allowing black mana to be paid with life. */
    public boolean canPayBlackManaWithLife(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        return battlefield != null && battlefield.stream()
                .filter(permanent -> !permanent.isFaceDown())
                .filter(permanent -> !hasLostAllAbilities(gameData, permanent))
                .anyMatch(permanent -> permanent.getCard().getStaticEffects().stream()
                        .anyMatch(PayBlackManaWithLifeEffect.class::isInstance));
    }

    /** Returns whether a player may look at opposing face-down creatures. */
    public boolean mayLookAtOpposingFaceDownCreatures(GameData gameData, UUID playerId) {
        if (gameData.hasFloatingEffectOnPlayer(playerId, LookAtFaceDownCreaturesEffect.class)) {
            return true;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent source : battlefield) {
            if (source.isFaceDown()
                    || gameData.removesOwnStaticAbility(source.getId(), LookAtFaceDownCreaturesEffect.class)) {
                continue;
            }
            StaticBonus bonus = computeStaticBonus(gameData, source);
            if (bonus.losesAllAbilities() || bonus.losesAllNonManaAbilities()) {
                continue;
            }
            if (hasActiveStaticEffect(gameData, source, LookAtFaceDownCreaturesEffect.class)) {
                return true;
            }
        }
        return false;
    }

    /** Returns whether the source has an activated-ability color permission. */
    public boolean canSpendManaAsAnyColorForActivatedAbilities(GameData gameData, Permanent source) {
        return canSpendManaAsAnyColorForActivatedAbilities(gameData, source, null);
    }

    /** Checks color permission for the particular ability being activated. */
    public boolean canSpendManaAsAnyColorForActivatedAbilities(GameData gameData, Permanent source,
                                                               ActivatedAbility ability) {
        if (source == null) {
            return false;
        }
        UUID controllerId = findPermanentController(gameData, source.getId());
        List<SpendManaAsAnyColorForActivatedAbilitiesEffect> ownPermissions = hasLostAllAbilities(gameData, source)
                ? List.of()
                : source.getCard().getStaticEffects().stream()
                .filter(SpendManaAsAnyColorForActivatedAbilitiesEffect.class::isInstance)
                .map(SpendManaAsAnyColorForActivatedAbilitiesEffect.class::cast)
                .filter(effect -> effect.subject() instanceof PermanentSubject.Source && effect.fromColor() == null)
                .toList();
        if (ownPermissions.stream().anyMatch(effect -> !effect.borrowedAbilitiesOnly())
                || !ownPermissions.isEmpty() && (ability == null || isBorrowedAbility(gameData, source, controllerId,
                ability))) {
            return true;
        }
        if (controllerId == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        return battlefield != null && battlefield.stream()
                .filter(permanent -> !hasLostAllAbilities(gameData, permanent))
                .anyMatch(permanent -> permanent.getCard().getStaticEffects().stream()
                        .filter(SpendManaAsAnyColorForActivatedAbilitiesEffect.class::isInstance)
                        .map(SpendManaAsAnyColorForActivatedAbilitiesEffect.class::cast)
                        .anyMatch(effect -> !(effect.subject() instanceof PermanentSubject.Source)
                                && staticSubjectNames(gameData, permanent, controllerId, controllerId, source,
                                effect.subject())));
    }

    /**
     * Whether {@code ability} is one the source has from its {@link GainActivatedAbilitiesOfOwnCreaturesEffect}: an
     * activated ability of another permanent the borrower names, of the kind it gains. Abilities granted to the source
     * by other effects, such as an Aura, are not.
     */
    private boolean isBorrowedAbility(GameData gameData, Permanent source, UUID controllerId, ActivatedAbility ability) {
        List<GainActivatedAbilitiesOfOwnCreaturesEffect> borrowers = source.getCard().getStaticEffects().stream()
                .filter(GainActivatedAbilitiesOfOwnCreaturesEffect.class::isInstance)
                .map(GainActivatedAbilitiesOfOwnCreaturesEffect.class::cast)
                .filter(borrower -> borrower.abilities().includes(ability))
                .toList();
        if (borrowers.isEmpty() || controllerId == null) {
            return false;
        }
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(source.getCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentSnapshot(source);
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent lender : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (lender.getId().equals(source.getId()) || borrowers.stream().noneMatch(borrower ->
                        staticSubjectNames(gameData, source, controllerId, playerId, lender, borrower.permanents())
                                && (borrower.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                                lender, borrower.filter(), filterContext)))) {
                    continue;
                }
                var supplied = com.github.laxika.magicalvibes.service.effect.staticfx.GainActivatedAbilitiesOfOwnCreaturesSelfEffectHandler
                        .effectiveActivatedAbilities(gameData, lender, this);
                if (supplied.stream().anyMatch(candidate -> candidate == ability
                        || java.util.Objects.equals(candidate.getManaCost(), ability.getManaCost())
                        && candidate.isRequiresTap() == ability.isRequiresTap()
                        && candidate.getEffects().equals(ability.getEffects()))) return true;
            }
        }
        return false;
    }

    /**
     * Whether the source's activated abilities may use blue mana as though it were mana of any color: its own
     * {@link SpendManaAsAnyColorForActivatedAbilitiesEffect} of blue mana (Quicksilver Elemental).
     */
    public boolean canSpendBlueManaAsAnyColorForActivatedAbilities(GameData gameData, Permanent source) {
        return source != null
                && !hasLostAllAbilities(gameData, source)
                && source.getCard().getStaticEffects().stream()
                .anyMatch(effect -> effect instanceof SpendManaAsAnyColorForActivatedAbilitiesEffect permission
                        && permission.fromColor() == ManaColor.BLUE);
    }

    /**
     * Returns {@code true} if damage can be prevented. Returns {@code false}
     * when a {@link DamageCantBePreventedEffect} for every source and all damage is on any battlefield
     * (e.g. Leyline of Punishment).
     */
    public boolean isDamagePreventable(GameData gameData) {
        return !gameData.damageCantBePreventedThisTurn()
                && !gameData.unpreventableDamageInProgress
                && !(anyBattlefieldHasStaticEffect(gameData, DamageCantBePreventedEffect.class)
                && activeStaticEffects(gameData, DamageCantBePreventedEffect.class).stream()
                .anyMatch(active -> active.effect().sources() == null && !active.effect().combatOnly()));
    }

    /** Returns the mana value of a permanent, including its face-down and transformed state. */
    public int getPermanentManaValue(Permanent permanent) {
        if (permanent.isFaceDown()) {
            return 0;
        }
        if (permanent.getMeldComponentCards().stream().anyMatch(component ->
                component.getBackFaceCard() != null
                        && component.getBackFaceCard().hasName(permanent.getCard().getName()))) {
            return permanent.getMeldComponentCards().stream().mapToInt(Card::getManaValue).sum();
        }
        Card original = permanent.getOriginalCard();
        if (permanent.isTransformed() && original.getBackFaceCard() != null
                && original.getBackFaceCard().hasName(permanent.getCard().getName())) {
            return original.getManaValue();
        }
        return permanent.getCard().getManaValue();
    }

    /** Returns whether damage of the requested type can be prevented. */
    public boolean isDamagePreventable(GameData gameData, boolean isCombatDamage) {
        return isDamagePreventable(gameData)
                && (!isCombatDamage || !isCombatDamageCantBePrevented(gameData));
    }

    /** Returns whether an active permanent makes combat damage use the stack. */
    public boolean combatDamageUsesStack(GameData gameData) {
        return gameData.anyPermanentMatches(permanent ->
                hasActiveStaticEffect(gameData, permanent,
                        com.github.laxika.magicalvibes.model.effect.CombatDamageUsesStackEffect.class));
    }

    /** Returns whether the active stack resolves first in, first out. */
    public boolean stackUsesFirstInFirstOut(GameData gameData) {
        return gameData.anyPermanentMatches(permanent ->
                hasActiveStaticEffect(gameData, permanent,
                        com.github.laxika.magicalvibes.model.effect.StackUsesFirstInFirstOutEffect.class));
    }

    /** Returns whether an active battlefield effect for every source makes combat damage unpreventable (Frenzied Baloth). */
    public boolean isCombatDamageCantBePrevented(GameData gameData) {
        return activeStaticEffects(gameData, DamageCantBePreventedEffect.class).stream()
                .anyMatch(active -> active.effect().sources() == null && active.effect().combatOnly());
    }

    /**
     * Returns {@code true} if noncombat damage dealt by the given source permanent can't be prevented because of a
     * {@link DamageCantBePreventedEffect} naming that source (Malignus). Unlike {@link #isDamagePreventable(GameData)}
     * this is scoped to one source, so callers that know the damage source consult it in addition to the global check.
     */
    public boolean damageCantBePreventedFromSource(GameData gameData, Permanent source) {
        return damageCantBePreventedFromSource(gameData, source, false);
    }

    /**
     * Returns whether damage from the source can't be prevented for the current damage event: a
     * {@link DamageCantBePreventedEffect} of the source itself or of a permanent its controller controls, printed or
     * granted, names the source ("this creature", "creatures you control"), for combat damage if it says so.
     */
    public boolean damageCantBePreventedFromSource(GameData gameData, Permanent source,
                                                   boolean isCombatDamage) {
        if (source == null) return false;
        UUID controllerId = findPermanentController(gameData, source.getId());
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        for (Permanent holder : battlefield) {
            if (!hasActiveStaticEffectIncludingGranted(gameData, holder, DamageCantBePreventedEffect.class)) {
                continue;
            }
            List<CardEffect> effects = new ArrayList<>(getActiveStaticEffects(gameData, holder, controllerId));
            effects.addAll(getGrantedEffects(gameData, holder));
            for (CardEffect effect : effects) {
                if (effect instanceof DamageCantBePreventedEffect unpreventable && unpreventable.sources() != null
                        && (isCombatDamage || !unpreventable.combatOnly())
                        && staticSubjectNames(gameData, holder, controllerId, controllerId, source,
                        unpreventable.sources())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns whether this player's active turn-scoped source filters prevent damage from the source. */
    public boolean isDamageFromMatchingSourcePreventedForPlayer(GameData gameData, UUID playerId,
                                                                Permanent source) {
        return isDamageFromMatchingSourcePreventedForPlayer(gameData, playerId, source, false);
    }

    public boolean isDamageFromMatchingSourcePreventedForPlayer(GameData gameData, UUID playerId,
                                                                Permanent source, boolean isCombatDamage) {
        return isDamageFromMatchingSourcePreventedForPlayer(gameData, playerId, source, isCombatDamage, null);
    }

    /** Checks source filters against the live source or the resolving entry's last known source. */
    public boolean isDamageFromMatchingSourcePreventedForPlayer(GameData gameData, UUID playerId,
                                                                StackEntry entry) {
        Permanent source = findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        return isDamageFromMatchingSourcePreventedForPlayer(gameData, playerId, source, false, entry);
    }

    private boolean isDamageFromMatchingSourcePreventedForPlayer(GameData gameData, UUID playerId,
                                                                 Permanent source, boolean isCombatDamage,
                                                                 StackEntry entry) {
        if (!isDamagePreventable(gameData, isCombatDamage) || source == null
                || damageCantBePreventedFromSource(gameData, source, isCombatDamage)) {
            return false;
        }
        java.util.function.Predicate<PermanentPredicate> matches = predicate -> entry == null
                ? predicateEvaluationService.matchesPermanentPredicate(gameData, source, predicate)
                : matchesDamageSourcePredicate(gameData, entry, entry.getEffectiveDamageSourceCard(), null, predicate);
        for (FloatingContinuousEffect next : gameData.preventionEffectsOnPlayer(playerId,
                PreventionScope.NEXT_TO_CONTROLLER_FROM_MATCHING_SOURCES)) {
            if (matches.test(((PreventDamageEffect) next.effect()).sourcePredicate())) {
                gameData.endFloatingEffect(next);
                return true;
            }
        }
        return gameData.preventionEffectsOnPlayer(playerId,
                        PreventionScope.ALL_TO_CONTROLLER_FROM_MATCHING_SOURCES,
                        PreventionScope.ALL_TO_CONTROLLER_AND_CREATURES_FROM_MATCHING_SOURCES,
                        PreventionScope.ALL_TO_PLAYERS_FROM_MATCHING_SOURCES).stream()
                .anyMatch(prevention -> matches.test(((PreventDamageEffect) prevention.effect()).sourcePredicate()));
    }

    /** Returns whether matching source damage to a creature controlled by the player is prevented this turn. */
    public boolean isDamageFromMatchingSourcePreventedForControlledCreature(
            GameData gameData, Permanent creature, Permanent source) {
        if (!isDamagePreventable(gameData) || creature == null || source == null
                || !isCreature(gameData, creature)) {
            return false;
        }
        UUID controllerId = findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;
        return gameData.preventionEffectsOnPlayer(controllerId,
                        PreventionScope.ALL_TO_CONTROLLER_AND_CREATURES_FROM_MATCHING_SOURCES).stream()
                .anyMatch(prevention -> predicateEvaluationService.matchesPermanentPredicate(gameData, source,
                        ((PreventDamageEffect) prevention.effect()).sourcePredicate()));
    }

    /**
     * Returns {@code true} if the player is able to lose the game (i.e. no
     * {@link CantLoseGameEffect} is controlled by them and no opponent controls a
     * {@link CantWinGameEffect}).
     */
    public boolean canPlayerLoseGame(GameData gameData, UUID playerId) {
        if (playerHasCantLoseGameEffect(gameData, playerId, effect -> effect instanceof CantLoseGameEffect)) {
            return false;
        }
        for (UUID opponentId : gameData.playerBattlefields.keySet()) {
            if (!playerId.equals(opponentId) && playerHasCantWinGameEffect(gameData, opponentId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether an opponent of {@code winnerId} has a "can't lose the game" effect that also says its
     * controller's opponents can't win the game (Platinum Angel). A plain "you can't lose the game"
     * (Liliana's Undead Minion) does not stop a player from winning (CR 104.2b).
     */
    public boolean opponentPreventsWinningGame(GameData gameData, UUID winnerId) {
        for (UUID opponentId : gameData.playerBattlefields.keySet()) {
            if (!winnerId.equals(opponentId) && playerHasCantLoseGameEffect(gameData, opponentId,
                    effect -> effect instanceof CantLoseGameEffect cantLose && cantLose.opponentsCantWin())) {
                return true;
            }
        }
        return false;
    }

    private boolean playerHasCantLoseGameEffect(GameData gameData, UUID playerId, Predicate<CardEffect> matcher) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, matcher)
                || playerBattlefieldHasGrantedEffect(gameData, playerId, matcher)
                || playerHasTemporaryStaticEffect(gameData, playerId, matcher)
                || playerEmblemHasActiveStaticEffect(gameData, playerId, matcher);
    }

    /**
     * Returns {@code true} if the player controls an active static effect that prevents them from
     * winning the game.
     */
    public boolean playerHasCantWinGameEffect(GameData gameData, UUID playerId) {
        if (playerBattlefieldHasStaticEffect(gameData, playerId, CantWinGameEffect.class)
                || playerBattlefieldHasGrantedEffect(gameData, playerId, CantWinGameEffect.class)
                || playerHasTemporaryStaticEffect(gameData, playerId, CantWinGameEffect.class)) {
            return true;
        }
        return playerEmblemHasActiveStaticEffect(gameData, playerId, CantWinGameEffect.class);
    }

    private boolean playerEmblemHasActiveStaticEffect(GameData gameData, UUID playerId,
                                                       Class<? extends CardEffect> effectType) {
        return playerEmblemHasActiveStaticEffect(gameData, playerId, (Predicate<CardEffect>) effectType::isInstance);
    }

    private boolean playerEmblemHasActiveStaticEffect(GameData gameData, UUID playerId,
                                                       Predicate<CardEffect> matcher) {
        for (Emblem emblem : gameData.emblems) {
            if (!playerId.equals(emblem.controllerId())) continue;
            for (CardEffect effect : emblem.staticEffects()) {
                if (matcher.test(effect)) {
                    return true;
                }
                if (effect instanceof ConditionalEffect conditional
                        && matcher.test(conditional.wrapped())
                        && conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forCasting(playerId))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the player can lose the game from having 0 or less life
     * (i.e. no {@link CantLoseGameFromLifeEffect} is present on their battlefield or in their
     * temporary player effects).
     */
    public boolean canPlayerLoseFromLife(GameData gameData, UUID playerId) {
        return !playerBattlefieldHasStaticEffect(gameData, playerId, CantLoseGameFromLifeEffect.class)
                && !playerHasTemporaryStaticEffect(gameData, playerId, CantLoseGameFromLifeEffect.class);
    }

    /**
     * Returns {@code true} if damage dealt to this player should be dealt as though
     * its source had infect. This is true when the player controls a permanent with
     * {@link DamageDealtAsInfectBelowZeroLifeEffect} and has 0 or less life.
     */
    public boolean shouldDamageBeDealtAsInfect(GameData gameData, UUID playerId) {
        if (!playerBattlefieldHasStaticEffect(gameData, playerId, DamageDealtAsInfectBelowZeroLifeEffect.class)) {
            return false;
        }
        int life = gameData.getLife(playerId);
        return life <= 0;
    }

    /** Returns whether damage dealt to the player does not cause them to lose life. */
    public boolean damageDoesNotCauseLifeLoss(GameData gameData, UUID playerId) {
        return activeStaticEffects(gameData, playerId, DamageDoesNotCauseLifeLossEffect.class).stream()
                .anyMatch(active -> !active.source().isFaceDown()
                        && !gameData.losesAllAbilitiesByEffect(active.source()));
    }

    /**
     * Returns the highest life-total floor that damage dealt to this player can't reduce them past,
     * or {@code 0} when no {@link DamageLifeFloorEffect} on their battlefield, in an emblem they
     * control, or floating on them ("until end of turn", Angel's Grace, Angel of Grace) currently applies.
     * Callers must treat {@code 0} as "no floor" (do not clamp life to 0). Each such effect only
     * contributes its floor while its {@link LifeFloorCondition} holds, evaluated against the
     * player's state before the damage is applied ({@code currentLife}).
     */
    public int damageLifeFloor(GameData gameData, UUID playerId, int currentLife) {
        int floor = 0;
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        boolean controlsCreature = bf != null && bf.stream().anyMatch(p -> isCreature(gameData, p));
        for (ActiveStaticEffect<DamageLifeFloorEffect> active
                : activeStaticEffects(gameData, playerId, DamageLifeFloorEffect.class)) {
            floor = Math.max(floor, activeDamageLifeFloor(active.effect(), controlsCreature, currentLife));
        }
        for (CardEffect effect : gameData.floatingPlayerEffects(playerId, DamageLifeFloorEffect.class)) {
            if (effect instanceof DamageLifeFloorEffect lifeFloor) {
                floor = Math.max(floor, activeDamageLifeFloor(lifeFloor, controlsCreature, currentLife));
            }
        }
        for (Emblem emblem : gameData.emblems) {
            if (!playerId.equals(emblem.controllerId())) continue;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof DamageLifeFloorEffect lifeFloor) {
                    floor = Math.max(floor, activeDamageLifeFloor(lifeFloor, controlsCreature, currentLife));
                }
            }
        }
        return currentLife < floor ? 0 : floor;
    }

    private int activeDamageLifeFloor(DamageLifeFloorEffect lifeFloor, boolean controlsCreature,
                                      int currentLife) {
        boolean active = switch (lifeFloor.condition()) {
            case ALWAYS -> true;
            case CONTROLS_A_CREATURE -> controlsCreature && currentLife >= lifeFloor.floor();
            case LIFE_AT_LEAST_FLOOR -> currentLife >= lifeFloor.floor();
        };
        return active ? lifeFloor.floor() : 0;
    }

    /** Returns the player's life total after applying damage replacement floors. */
    public int lifeAfterDamage(GameData gameData, UUID playerId, int damage) {
        int currentLife = gameData.getLife(playerId);
        if (!canPlayerLoseLife(gameData, playerId)) return currentLife;
        int newLife = SaturatingMath.subtract(currentLife, damage);
        int lifeFloor = damageLifeFloor(gameData, playerId, currentLife);
        return lifeFloor > 0 ? Math.max(newLife, lifeFloor) : newLife;
    }

    // --- Creature / type classification ---

    /**
     * Returns {@code true} if the permanent is currently a creature. This accounts for
     * the permanent's natural card type, temporary animation effects, awakening counters,
     * global artifact animation, and metalcraft-conditional self-animation.
     */
    public boolean isCreature(GameData gameData, Permanent permanent) {
        return isCreature(gameData, permanent, null);
    }

    /** Evaluates the entering permanent without counting it among battlefield permanents. */
    public boolean isCreature(GameData gameData, Permanent permanent, UUID controllerId) {
        StaticBonus bonus = controllerId == null ? computeStaticBonus(gameData, permanent)
                : computeStaticBonus(gameData, permanent, controllerId);
        if (permanent.isFaceDown() && !bonus.cardTypeOverriding()
                && permanent.getFaceDownCardTypes().contains(CardType.CREATURE)) {
            return true;
        }
        return isCreatureWithBonus(gameData, permanent, bonus, controllerId);
    }

    /** Returns whether a permanent currently has token status, including continuous effects. */
    public boolean isToken(GameData gameData, Permanent permanent) {
        return permanent.getCard().isToken() || computeStaticBonus(gameData, permanent).tokenized();
    }

    private boolean isCreatureWithBonus(GameData gameData, Permanent permanent, StaticBonus bonus) {
        return isCreatureWithBonus(gameData, permanent, bonus, null);
    }

    private boolean isCreatureWithBonus(GameData gameData, Permanent permanent, StaticBonus bonus,
                                        UUID enteringControllerId) {
        if (hasEffectiveCardType(permanent, bonus, CardType.CREATURE)) return true;
        if (bonus.cardTypeOverriding()) return false;
        if (gameData.animatedByFloatingEffect(permanent.getId())) return true;
        if (permanent.getCounterCount(CounterType.AWAKENING) > 0) return true;
        // A permanent on the battlefield answers from its layered state (the bonus above), where the
        // animation applied in layer 4 to what it matched then (CR 613.1d).
        if (isAnimatedByPrintedStaticAnimation(gameData, permanent, permanent.getCard().getId(), false)
                && !gameData.anyPermanentMatches(p -> p.getId().equals(permanent.getId()))) {
            return true;
        }
        if (isAnimatedByStarfield(gameData, permanent)) return true;
        if (hasEffectiveCardType(permanent, bonus, CardType.LAND) && matchesAnimateLand(gameData, permanent)) return true;
        if (hasAuraBecomeCreatureEffect(gameData, permanent)) return true;
        return hasSelfBecomeCreatureEffect(gameData, permanent, enteringControllerId);
    }

    /**
     * Returns {@code true} if the permanent is currently a land, including type-replacing
     * effects such as Imprisoned in the Moon.
     */
    public boolean isLand(GameData gameData, Permanent permanent) {
        return hasEffectiveCardType(permanent, computeStaticBonus(gameData, permanent), CardType.LAND);
    }

    /**
     * Returns {@code true} if the permanent is currently a planeswalker after continuous
     * type-changing effects.
     */
    public boolean isPlaneswalker(GameData gameData, Permanent permanent) {
        return hasEffectiveCardType(permanent, computeStaticBonus(gameData, permanent), CardType.PLANESWALKER);
    }

    /** Whether an attached Aura makes this permanent use toughness as its loyalty resource. */
    public boolean isToughnessAsLoyaltyPermanent(GameData gameData, Permanent permanent) {
        return hasAuraWithEffect(gameData, permanent, EnchantedPermanentToughnessBecomesLoyaltyEffect.class);
    }

    /** Returns the stored base toughness used as the loyalty resource, with a safe pre-initialization fallback. */
    public int getToughnessAsLoyalty(Permanent permanent) {
        return permanent.getToughnessAsLoyalty() != null
                ? permanent.getToughnessAsLoyalty()
                : permanent.getBaseToughness();
    }

    /**
     * The defending player of a creature attacking {@code attackedTargetId}: the attacked player, the controller of an
     * attacked planeswalker, or an attacked battle's protector (CR 310.9d); {@code null} for no attack target.
     */
    public UUID defendingPlayerOfAttackTarget(GameData gameData, UUID attackedTargetId) {
        if (attackedTargetId == null || gameData.playerIds.contains(attackedTargetId)) {
            return attackedTargetId;
        }
        Permanent attacked = findPermanentById(gameData, attackedTargetId);
        if (attacked != null && attacked.getProtectorPlayerId() != null && isBattle(gameData, attacked)) {
            return attacked.getProtectorPlayerId();
        }
        return findPermanentController(gameData, attackedTargetId);
    }

    public boolean isBattle(GameData gameData, Permanent permanent) {
        return hasEffectiveCardType(permanent, computeStaticBonus(gameData, permanent), CardType.BATTLE);
    }

    /** Whether the permanent has {@code type} after the CR 613 layered computation. */
    public boolean hasEffectiveCardType(GameData gameData, Permanent permanent, CardType type) {
        return hasEffectiveCardType(permanent, computeStaticBonus(gameData, permanent), type);
    }

    public boolean isKindred(GameData gameData, Permanent permanent) {
        return hasEffectiveCardType(permanent, computeStaticBonus(gameData, permanent), CardType.KINDRED);
    }

    private boolean hasEffectiveCardType(Permanent permanent, StaticBonus bonus, CardType type) {
        if (bonus.cardTypeOverriding()) {
            return bonus.grantedCardTypes().contains(type);
        }
        return hasCardType(permanent, type) || bonus.grantedCardTypes().contains(type);
    }

    private Set<CardType> baseCardTypes(Permanent permanent) {
        Set<CardType> cardTypes = EnumSet.noneOf(CardType.class);
        if (permanent.isFaceDown()) {
            cardTypes.addAll(permanent.getFaceDownCardTypes());
        } else {
            if (permanent.getCard().getType() != null) {
                cardTypes.add(permanent.getCard().getType());
            }
            cardTypes.addAll(permanent.getCard().getAdditionalTypes());
        }
        return cardTypes;
    }

    /**
     * Recursion-safe creature check for use INSIDE {@link #assembleStaticBonus}: reads the
     * layer-4 board state already computed for this pass rather than calling {@link #isCreature},
     * which re-enters {@code computeStaticBonus} for the same permanent and would recurse forever.
     * Mirrors {@code LayerSystemService.isCreatureForL4} (the layered card type plus the one-shot
     * animation flags).
     */
    /**
     * Whether a source's own static effect applies to the source itself in the regular (non-self)
     * static pass. Only {@link BoostCreaturesOfChosenColorEffect} does: it has no self handler, yet
     * "creatures you control of the chosen color" covers its own source once that is a creature.
     * Scopes with an explicit self-inclusive variant ({@code ALL_OWN_CREATURES}) are applied once by
     * their self handlers instead.
     */
    private boolean appliesToItsOwnSourceInStaticPass(LayerSystemService.LayeredBoardState board,
                                                      Permanent source, CardEffect effect) {
        return effect instanceof BoostCreaturesOfChosenColorEffect && isCreatureInStaticPass(board, source);
    }

    /**
     * Recursion-safe creature check for use INSIDE {@link #assembleStaticBonus}: reads the
     * layer-4 board state already computed for this pass rather than calling {@link #isCreature},
     * which re-enters {@code computeStaticBonus} for the same permanent and would recurse forever.
     * Mirrors {@code LayerSystemService.isCreatureForL4} (the layered card type plus the one-shot
     * animation flags).
     */
    private boolean isCreatureInStaticPass(LayerSystemService.LayeredBoardState board, Permanent permanent) {
        CharacteristicState state = board.states().get(permanent.getId());
        boolean typeCreature = state != null
                ? state.hasCardType(CardType.CREATURE)
                : hasCardType(permanent, CardType.CREATURE);
        return typeCreature
                || permanent.getCounterCount(CounterType.AWAKENING) > 0;
    }

    private boolean isLandInStaticPass(LayerSystemService.LayeredBoardState board, Permanent permanent) {
        CharacteristicState state = board.states().get(permanent.getId());
        return state != null
                ? state.hasCardType(CardType.LAND)
                : hasCardType(permanent, CardType.LAND);
    }

    /**
     * Returns {@code true} if an aura attached to the given permanent carries an
     * {@link EnchantedPermanentBecomesCreatureEffect} (e.g. Living Terrain), which continuously
     * makes the enchanted permanent a creature. A face-down Aura has no abilities (CR 708.2).
     */
    private boolean hasAuraBecomeCreatureEffect(GameData gameData, Permanent permanent) {
        if (!anyBattlefieldPrintsStaticEffect(gameData, EnchantedPermanentBecomesCreatureEffect.class)) {
            return false;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent source : battlefield) {
                if (!source.isFaceDown() && source.isAttached() && permanent.getId().equals(source.getAttachedTo())) {
                    for (CardEffect effect : source.getCard().getStaticEffects()) {
                        if (effect instanceof EnchantedPermanentBecomesCreatureEffect) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the permanent has a conditional self-scope
     * {@link AnimatePermanentsEffect} and its condition is currently met.
     *
     * <p>Callable from inside static-bonus assembly and from the layered queries alike: the
     * animation condition is recursion-safe wherever it has to be, because
     * {@link #isStaticEvaluationActive()} sees the assembly the caller is in. Checking it with
     * the fully layered queries from inside one (Rusted Relic's metalcraft counting artifacts
     * through the layered {@code isArtifact}) re-enters the assembly and recurses forever.
     */
    public boolean hasSelfBecomeCreatureEffect(GameData gameData, Permanent permanent) {
        return hasSelfBecomeCreatureEffect(gameData, permanent, null);
    }

    /**
     * As {@link #hasSelfBecomeCreatureEffect(GameData, Permanent)}; a permanent that is not on a
     * battlefield yet (it is still entering) uses {@code enteringControllerId} as its controller.
     */
    public boolean hasSelfBecomeCreatureEffect(GameData gameData, Permanent permanent, UUID enteringControllerId) {
        for (CardEffect effect : permanent.getCard().getStaticEffects()) {
            if (effect instanceof ConditionalEffect conditional
                    && conditional.wrapped() instanceof AnimatePermanentsEffect animate
                    && animate.subject() instanceof PermanentSubject.Source) {
                UUID controllerId = findPermanentController(gameData, permanent.getId());
                if (controllerId == null) controllerId = enteringControllerId;
                if (controllerId == null) continue;
                ConditionContext context = ConditionContext.forPermanent(permanent, controllerId);
                if (conditionEvaluationService.isMet(gameData, conditional.condition(), context)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the given player controls three or more artifacts (metalcraft).
     */
    public boolean isMetalcraftMet(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        long artifactCount = battlefield.stream()
                .filter(p -> isArtifact(gameData, p))
                .count();
        return artifactCount >= 3;
    }

    /**
     * Returns {@code true} if the given player controls three or more creatures with different
     * powers (Coven). Powers are effective P/T including static bonuses; duplicates are fine so
     * long as at least three distinct power values are present.
     */
    public boolean isCovenMet(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        HashSet<Integer> distinctPowers = new HashSet<>();
        for (Permanent permanent : battlefield) {
            if (!isCreature(gameData, permanent)) continue;
            distinctPowers.add(getEffectivePower(gameData, permanent));
            if (distinctPowers.size() >= 3) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if any opponent controls strictly more lands than the given player
     * (e.g. Gift of Estates, Weathered Wayfarer's "an opponent controls more lands than you").
     */
    public boolean anyOpponentControlsMoreLands(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(controllerId)) continue;
            if (controlsMoreLandsThan(gameData, candidateOpponentId, controllerId)) {
                return true;
            }
        }
        return false;
    }

    /** Returns whether an opponent controls at least two more lands than the given player. */
    public boolean anyOpponentControlsAtLeastTwoMoreLands(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        int controllerLands = countLandsControlled(gameData, controllerId);
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(controllerId)) continue;
            if (countLandsControlled(gameData, candidateOpponentId) >= controllerLands + 2) {
                return true;
            }
        }
        return false;
    }

    /** Returns whether {@code playerId} controls strictly more lands than {@code comparedPlayerId}. */
    public boolean controlsMoreLandsThan(GameData gameData, UUID playerId, UUID comparedPlayerId) {
        return countLandsControlled(gameData, playerId) > countLandsControlled(gameData, comparedPlayerId);
    }

    /** Returns whether {@code playerId} controls strictly more creatures than {@code comparedPlayerId}. */
    public boolean controlsMoreCreaturesThan(GameData gameData, UUID playerId, UUID comparedPlayerId) {
        return countCreaturesControlled(gameData, playerId) > countCreaturesControlled(gameData, comparedPlayerId);
    }

    /**
     * Whether an evasion ability's "as long as / unless defending player …" condition holds for
     * {@code defendingPlayerId}, the player the attacker is attacking or the controller or protector of what it attacks
     * (CR 508.5). It is judged as a static ability of {@code source} (the attacker, or the Aura or Equipment granting
     * the ability) with the defending player as the context's target, and only as blockers are declared: a block
     * already made stays (CR 509.1b).
     */
    public boolean defendingPlayerConditionHolds(GameData gameData, Permanent source, Condition condition,
                                                 UUID defendingPlayerId) {
        UUID controllerId = findPermanentController(gameData, source.getId());
        return conditionEvaluationService.isMet(gameData, condition,
                ConditionContext.forStaticEffect(source, controllerId).withTargetId(defendingPlayerId));
    }

    /** Returns whether {@code playerId} controls the most creatures, including ties for most. */
    public boolean controlsMostCreaturesOrTied(GameData gameData, UUID playerId) {
        if (playerId == null) return false;
        int playerCreatureCount = countCreaturesControlled(gameData, playerId);
        boolean foundPlayer = false;
        for (UUID candidatePlayerId : gameData.orderedPlayerIds) {
            if (candidatePlayerId.equals(playerId)) {
                foundPlayer = true;
            } else if (playerCreatureCount < countCreaturesControlled(gameData, candidatePlayerId)) {
                return false;
            }
        }
        return foundPlayer;
    }

    /**
     * Returns {@code true} if any opponent of the given player controls a creature with flying
     * (Groundling Pouncer's activation restriction).
     */
    public boolean anyOpponentControlsFlyingCreature(GameData gameData, UUID controllerId) {
        if (controllerId == null) return false;
        for (UUID candidateOpponentId : gameData.orderedPlayerIds) {
            if (candidateOpponentId.equals(controllerId)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(candidateOpponentId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (isCreature(gameData, permanent) && hasKeyword(gameData, permanent, Keyword.FLYING)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int countLandsControlled(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        int count = 0;
        for (Permanent permanent : battlefield) {
            // Static conditions can ask for land counts while the layered board is being built.
            // Calling isLand there would re-enter static-bonus assembly for a conditional static
            // effect such as Verge Rangers, so use the already-computed layer-4 state instead.
            if (isStaticEvaluationActive()
                    ? predicateEvaluationService.matchesStaticLeaf(permanent, LAND_FILTER)
                    : isLand(gameData, permanent)) {
                count++;
            }
        }
        return count;
    }

    private int countCreaturesControlled(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (isCreature(gameData, permanent)) {
                count += CreatureCountSupport.creatureCount(gameData, permanent, this);
            }
        }
        return count;
    }

    /**
     * Returns {@code true} if a creature died this turn (morbid condition).
     * Checks all players' death counts since morbid is not controller-specific.
     */
    /**
     * Returns {@code true} if at least one creature is blocking the given attacking creature.
     */
    public boolean isBlockedByAnyCreature(GameData gameData, Permanent attacker) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent blocker : battlefield) {
                if (blocker.isBlocking() && blocker.getBlockingTargetIds().contains(attacker.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isMorbidMet(GameData gameData) {
        return gameData.creatureDeathCountThisTurn.values().stream()
                .anyMatch(count -> count > 0);
    }

    /**
     * Returns {@code true} if the given player has cast another spell matching {@code filter}
     * this turn, excluding {@code excludeSpell} (typically the spell currently resolving).
     */
    public boolean hasControllerCastAnotherSpellThisTurn(
            GameData gameData, UUID controllerId, Card excludeSpell, CardPredicate filter) {
        return hasControllerCastAnotherSpellThisTurn(gameData, controllerId, excludeSpell, filter, false);
    }

    public boolean hasControllerCastAnotherSpellThisTurn(
            GameData gameData, UUID controllerId, Card excludeSpell, CardPredicate filter,
            boolean fromHandOnly) {
        for (Card spell : gameData.getSpellsCastThisTurn(controllerId)) {
            if (spell == excludeSpell) {
                continue;
            }
            if (fromHandOnly && !gameData.wasSpellCastFromHandThisTurn(spell.getId())) {
                continue;
            }
            // A spell's mana value on the stack includes the X it was cast with (CR 202.3e).
            int xSymbols = spell.getParsedManaCost() == null ? 0 : spell.getParsedManaCost().getXSymbolCount();
            Integer castX = xSymbols > 0
                    ? (gameData.getSpellCastStackManaValue(spell) - spell.getManaValue()) / xSymbols : null;
            if (predicateEvaluationService.matchesCardPredicate(spell, filter, spell.getId(),
                    gameData, controllerId, null, null, castX)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the given player has cast at least two spells matching
     * {@code filter} this turn.
     */
    public boolean hasControllerCastTwoOrMoreSpellsThisTurn(
            GameData gameData, UUID controllerId, CardPredicate filter) {
        return gameData.getSpellsCastThisTurn(controllerId).stream()
                .filter(spell -> predicateEvaluationService.matchesCardPredicate(spell, filter, spell.getId()))
                .limit(2)
                .count() == 2;
    }

    public boolean hasSpellCastingAbilityGrant(GameData gameData, UUID playerId, Card card, Keyword ability) {
        return hasSpellCastingAbilityGrant(gameData, playerId, card, ability, Zone.HAND);
    }

    public boolean hasSpellCastingAbilityGrant(GameData gameData, UUID playerId, Card card,
                                                Keyword ability, Zone sourceZone) {
        if (card != null && gameData.perpetualCardSpellCastingAbilityGrants
                .getOrDefault(card.getId(), List.of()).stream()
                .anyMatch(grant -> grant.grantedAbility() == ability
                        && grant.appliesToSourceZone(sourceZone)
                        && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null,
                        gameData, playerId))) {
            return true;
        }
        if (ability == Keyword.CONVOKE && gameData.hasNextSpellConvokeGrant(playerId)) {
            return true;
        }
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                    CardEffect activeEffect = staticEffectConditionResolver.resolve(
                            gameData, permanent, sourceControllerId, effect);
                    if (activeEffect instanceof SpellCastingAbilityGrantingEffect grant
                            && (grant.appliesToAllPlayers() || Objects.equals(sourceControllerId, playerId))
                            && !hasLostAllAbilities(gameData, permanent)
                            && grant.grantedAbility() == ability
                            && grant.appliesToSourceZone(sourceZone)
                            && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null, gameData, playerId)
                            && (!grant.appliesOnlyToFirstMatchingSpellEachTurn()
                            || gameData.getSpellsCastThisTurn(playerId).stream().noneMatch(previousSpell ->
                            predicateEvaluationService.matchesCardPredicate(
                                    previousSpell, grant.filter(), permanent.getCard().getId(), gameData, playerId)))) {
                        return true;
                    }
                }
            }
        }
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            if (!Objects.equals(emblem.controllerId(), playerId)) {
                continue;
            }
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof SpellCastingAbilityGrantingEffect grant
                        && grant.grantedAbility() == ability
                        && grant.appliesToSourceZone(sourceZone)
                        && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null, gameData, playerId)) {
                    return true;
                }
            }
        }
        if (gameData.planechase != null) {
            UUID planarControllerId = gameData.planechase.controllerId;
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof SpellCastingAbilityGrantingEffect grant
                            && (grant.appliesToAllPlayers() || Objects.equals(planarControllerId, playerId))
                            && grant.grantedAbility() == ability
                            && grant.appliesToSourceZone(sourceZone)
                            && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null, gameData, playerId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<Integer> getSpellCastingAbilityGrantValues(GameData gameData, UUID playerId,
                                                            Card card, Keyword ability) {
        return getSpellCastingAbilityGrantValues(gameData, playerId, card, ability, Zone.HAND);
    }

    public List<Integer> getSpellCastingAbilityGrantValues(GameData gameData, UUID playerId,
                                                            Card card, Keyword ability,
                                                            Zone sourceZone) {
        List<Integer> values = new ArrayList<>();
        if (card != null) {
            gameData.perpetualCardSpellCastingAbilityGrants
                    .getOrDefault(card.getId(), List.of()).stream()
                    .filter(grant -> grant.grantedAbility() == ability
                            && grant.appliesToSourceZone(sourceZone)
                            && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null,
                            gameData, playerId))
                    .map(SpellCastingAbilityGrantingEffect::abilityValue)
                    .forEach(values::add);
        }
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                    CardEffect activeEffect = staticEffectConditionResolver.resolve(
                            gameData, permanent, sourceControllerId, effect);
                    if (activeEffect instanceof SpellCastingAbilityGrantingEffect grant
                            && (grant.appliesToAllPlayers() || Objects.equals(sourceControllerId, playerId))
                            && !hasLostAllAbilities(gameData, permanent)
                            && grant.grantedAbility() == ability
                            && grant.appliesToSourceZone(sourceZone)
                            && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null, gameData, playerId)
                            && (!grant.appliesOnlyToFirstMatchingSpellEachTurn()
                            || gameData.getSpellsCastThisTurn(playerId).stream().noneMatch(previousSpell ->
                            predicateEvaluationService.matchesCardPredicate(
                                    previousSpell, grant.filter(), permanent.getCard().getId(), gameData, playerId)))) {
                        values.add(grant.abilityValue());
                    }
                }
            }
        }
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            if (!Objects.equals(emblem.controllerId(), playerId)) {
                continue;
            }
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof SpellCastingAbilityGrantingEffect grant
                        && grant.grantedAbility() == ability
                        && grant.appliesToSourceZone(sourceZone)
                        && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null, gameData, playerId)) {
                    values.add(grant.abilityValue());
                }
            }
        }
        if (gameData.planechase != null) {
            UUID planarControllerId = gameData.planechase.controllerId;
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof SpellCastingAbilityGrantingEffect grant
                            && (grant.appliesToAllPlayers() || Objects.equals(planarControllerId, playerId))
                            && grant.grantedAbility() == ability
                            && grant.appliesToSourceZone(sourceZone)
                            && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null, gameData, playerId)) {
                        values.add(grant.abilityValue());
                    }
                }
            }
        }
        return values;
    }

    /**
     * Returns {@code true} if the given player has cast at least three spells matching
     * {@code filter} this turn.
     */
    public boolean hasControllerCastThreeOrMoreSpellsThisTurn(
            GameData gameData, UUID controllerId, CardPredicate filter) {
        return gameData.getSpellsCastThisTurn(controllerId).stream()
                .filter(spell -> predicateEvaluationService.matchesCardPredicate(spell, filter, spell.getId()))
                .limit(3)
                .count() == 3;
    }

    /**
     * Returns {@code true} if the given player has cast at least four spells matching
     * {@code filter} this turn.
     */
    public boolean hasControllerCastFourOrMoreSpellsThisTurn(
            GameData gameData, UUID controllerId, CardPredicate filter) {
        return gameData.getSpellsCastThisTurn(controllerId).stream()
                .filter(spell -> predicateEvaluationService.matchesCardPredicate(spell, filter, spell.getId()))
                .limit(4)
                .count() == 4;
    }

    /**
     * Returns {@code true} if the permanent is an artifact by its own card type, ignoring every continuous effect;
     * {@link #isArtifact(GameData, Permanent)} answers with the layers applied.
     */
    public boolean isArtifact(Permanent permanent) {
        return hasCardType(permanent, CardType.ARTIFACT);
    }

    /**
     * Returns {@code true} if the permanent is an artifact, checking natural card type,
     * temporary granted card types, and static card type grants (e.g. from equipment).
     */
    public boolean isArtifact(GameData gameData, Permanent permanent) {
        return hasEffectiveCardType(permanent, computeStaticBonus(gameData, permanent), CardType.ARTIFACT);
    }

    /**
     * Returns {@code true} if the permanent is an enchantment by its own card type, ignoring every continuous effect;
     * {@link #isEnchantment(GameData, Permanent)} answers with the layers applied.
     */
    public boolean isEnchantment(Permanent permanent) {
        return hasCardType(permanent, CardType.ENCHANTMENT);
    }

    /**
     * Returns {@code true} if the permanent is an enchantment, checking natural card type,
     * temporary granted card types, and static card type grants (e.g. from Enchanted Evening).
     */
    public boolean isEnchantment(GameData gameData, Permanent permanent) {
        return hasEffectiveCardType(permanent, computeStaticBonus(gameData, permanent), CardType.ENCHANTMENT);
    }

    // --- Keyword & effect checking ---

    /**
     * Returns {@code true} if the permanent has the given keyword after the CR 613 layered
     * computation: {@code bonus.keywords()} is the complete final keyword set (printed keywords
     * included, layer-6 grants added and removals/ability loss applied in timestamp order).
     * {@link StaticBonus#NONE} means no continuous effect touched the permanent â€” the intrinsic
     * answer stands.
     */
    public boolean hasKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (bonus == StaticBonus.NONE && permanentPerpetuallyLostKeyword(gameData, permanent, keyword)) {
            return false;
        }
        return hasKeyword(permanent, bonus, keyword);
    }

    /**
     * Whether the card has perpetually lost {@code keyword} (Double team) and nothing currently grants it back.
     * Used when no static bonus is computed, because the layered state, which drops perpetual removals, is absent.
     */
    private boolean permanentPerpetuallyLostKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        return gameData.perpetualCardRemovedKeywords
                .getOrDefault(permanent.getCard().getId(), Set.of()).contains(keyword)
                && gameData.floatingEffectsOnPermanent(permanent.getId(), GrantKeywordEffect.class).stream()
                        .noneMatch(fe -> ((GrantKeywordEffect) fe.effect()).keywords().contains(keyword));
    }

    /** Counts the flanking abilities that survive the current ability layer. */
    public int flankingInstances(GameData gameData, Permanent permanent) {
        if (!hasKeyword(gameData, permanent, Keyword.FLANKING)) return 0;
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            CharacteristicState state = LayerSystemService.activeStateFor(permanent.getId());
            return state == null ? 1 : Math.max(1, state.getFlankingInstances());
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /** Counts the separate persist / undying abilities the permanent has under the current ability layer. */
    public int triggerKeywordInstances(GameData gameData, Permanent permanent, Keyword keyword) {
        if (!hasKeyword(gameData, permanent, keyword)) return 0;
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            CharacteristicState state = LayerSystemService.activeStateFor(permanent.getId());
            return state == null ? 1 : Math.max(1, state.triggerKeywordInstances(keyword));
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /** Counts the melee abilities that survive the current ability layer. */
    public int meleeInstances(GameData gameData, Permanent permanent) {
        if (!hasKeyword(gameData, permanent, Keyword.MELEE)) return 0;
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            CharacteristicState state = LayerSystemService.activeStateFor(permanent.getId());
            return state == null ? 1 : Math.max(1, state.getMeleeInstances());
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /**
     * Keyword check against a pre-computed static bonus, for callers that read many keywords
     * off the same permanent (mirrors {@link #getEffectivePower(Permanent, StaticBonus)}).
     */
    /**
     * Returns {@code true} when a floating "loses all abilities" effect on the permanent is stamped
     * after the given timestamp, so abilities granted at that timestamp are removed by it (CR 613.7).
     */
    private boolean hasLaterLosesAllAbilitiesEffect(GameData gameData, UUID permanentId, long timestamp) {
        return gameData.floatingEffects.stream()
                .anyMatch(other -> other.effect() instanceof LosesAllAbilitiesEffect loss && loss.losesEveryAbility()
                        && permanentId.equals(other.affectedPermanentId())
                        && other.timestamp() > timestamp);
    }

    public boolean hasKeyword(Permanent permanent, StaticBonus bonus, Keyword keyword) {
        if (bonus == StaticBonus.NONE) {
            return permanent.hasKeyword(keyword);
        }
        if (bonus.removedKeywords().contains(keyword)) return false;
        return bonus.keywords().contains(keyword);
    }

    /**
     * Returns the names in the permanent's active "bands with other" abilities. The ability is
     * separate from ordinary banding and is therefore represented by its named quality rather than
     * by {@link Keyword#BANDING}.
     */
    public Set<String> bandsWithOtherNames(GameData gameData, Permanent permanent) {
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (gameData.losesAllAbilitiesByEffect(permanent)
                || bonus.losesAllAbilities()
                || bonus.losesAllNonManaAbilities()) {
            return Set.of();
        }
        return activeBandsWithOtherEffects(gameData, permanent, bonus).stream()
                .map(BandsWithOtherEffect::creatureName)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    /** The static effects of {@code effectType} the permanent has right now, its own or granted to it. */
    public List<CardEffect> currentStaticEffectsOfType(GameData gameData, Permanent permanent,
                                                       Class<? extends CardEffect> effectType) {
        List<CardEffect> effects = new ArrayList<>(staticEffectsIncludingTemporary(
                gameData, permanent, findPermanentController(gameData, permanent.getId())).stream()
                .filter(effectType::isInstance).toList());
        effects.addAll(computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .filter(effectType::isInstance).toList());
        return effects;
    }

    private List<BandsWithOtherEffect> activeBandsWithOtherEffects(GameData gameData,
                                                                    Permanent permanent,
                                                                    StaticBonus bonus) {
        List<BandsWithOtherEffect> effects = new ArrayList<>();
        effects.addAll(staticEffectsIncludingTemporary(
                gameData, permanent, findPermanentController(gameData, permanent.getId())).stream()
                .filter(effect -> !gameData.removesOwnStaticAbility(permanent.getId(), effect.getClass()))
                .filter(BandsWithOtherEffect.class::isInstance)
                .map(BandsWithOtherEffect.class::cast)
                .toList());
        // The layered pass already removed the granted ones a later removal took away (CR 613.7).
        effects.addAll(bonus.grantedEffects().stream()
                .filter(BandsWithOtherEffect.class::isInstance)
                .map(BandsWithOtherEffect.class::cast)
                .toList());
        return effects;
    }

    /**
     * Returns whether a group of creatures satisfies one creature's "bands with other" ability.
     * Every member must have the named quality, and at least one member must carry that ability.
     */
    public boolean canUseBandsWithOther(GameData gameData, Collection<Permanent> creatures) {
        return canUseBandsWithOther(gameData, creatures, true);
    }

    /** A declared attacking band may consist only of its bands-with-other creature. */
    public boolean canUseBandsWithOther(GameData gameData, Collection<Permanent> creatures,
                                       boolean requireOtherCreature) {
        if (creatures == null || creatures.isEmpty() || (requireOtherCreature && creatures.size() < 2)) {
            return false;
        }
        List<Permanent> band = creatures.stream().toList();
        for (Permanent creature : band) {
            StaticBonus bonus = computeStaticBonus(gameData, creature);
            for (BandsWithOtherEffect effect : activeBandsWithOtherEffects(gameData, creature, bonus)) {
                if (band.stream().allMatch(member ->
                        predicateEvaluationService.matchesPermanentPredicate(
                                gameData, member, effect.creatureFilter()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the permanent's colors after the CR 613 layer-5 computation: the natural color
     * plus additive grants, or the replacement set when a color-setting effect ("becomes red")
     * applied. Prefer this over {@link Permanent#getEffectiveColor()} in engine code â€” the
     * legacy accessor does not see layered color changes.
     */
    public Set<CardColor> getEffectiveColors(GameData gameData, Permanent permanent) {
        if (permanent.getLastKnownColors() != null && findPermanentById(gameData, permanent.getId()) == null) {
            return permanent.getLastKnownColors();
        }
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (bonus.colorOverriding()) {
            return bonus.grantedColors();
        }
        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        if (!hasKeyword(permanent, bonus, Keyword.DEVOID)) {
            colors.addAll(permanent.getEffectiveColors());
        }
        colors.addAll(bonus.grantedColors());
        return colors;
    }

    /** Returns the controller's current devotion to one color. */
    public int getDevotionToColor(GameData gameData, UUID controllerId, ManaColor color) {
        if (gameData == null || controllerId == null || color == null) {
            return 0;
        }
        return countColorManaSymbolsAmongPermanents(gameData, controllerId, color)
                + layerSystemService.devotionModifierFor(gameData, controllerId);
    }

    /**
     * Counts mana symbols of one color in the mana costs of the controller's permanents. Unlike
     * devotion this ignores devotion modifiers, so it is the count chroma abilities use.
     */
    public int countColorManaSymbolsAmongPermanents(GameData gameData, UUID controllerId, ManaColor color) {
        if (gameData == null || controllerId == null || color == null) {
            return 0;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int symbols = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.isFaceDown()) continue;
            var manaCost = permanent.getCard().getParsedManaCost();
            if (manaCost != null) {
                symbols += manaCost.countColorSymbols(color);
            }
        }
        return symbols;
    }

    /** Returns the controller's current devotion to a color combination. */
    public int getDevotionToColors(GameData gameData, UUID controllerId, Set<ManaColor> colors) {
        if (gameData == null || controllerId == null || colors == null || colors.isEmpty()) {
            return 0;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int devotion = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.isFaceDown()) continue;
            var manaCost = permanent.getCard().getParsedManaCost();
            if (manaCost != null) {
                devotion += manaCost.countSymbolsOfAnyColor(colors);
            }
        }
        return devotion + layerSystemService.devotionModifierFor(gameData, controllerId);
    }

    /** Returns the permanent's current card types after continuous type-changing effects. */
    public Set<CardType> getEffectiveCardTypes(GameData gameData, Permanent permanent) {
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (bonus.cardTypeOverriding()) {
            return Set.copyOf(bonus.grantedCardTypes());
        }
        Set<CardType> cardTypes = baseCardTypes(permanent);
        cardTypes.addAll(bonus.grantedCardTypes());
        if (isCreatureWithBonus(gameData, permanent, bonus)) {
            cardTypes.add(CardType.CREATURE);
        }
        return cardTypes;
    }

    /** Returns the permanent's current name after continuous name-changing effects. */
    public String getEffectiveName(GameData gameData, Permanent permanent) {
        if (permanent.isFaceDown()) {
            return null;
        }
        String name = computeStaticBonus(gameData, permanent).name();
        return name != null ? name : permanent.getCard().getName();
    }

    /**
     * Whether {@code permanent} has {@code chosenName} (CR 709.4a). A face-down permanent has no name
     * (CR 708.2), a Room has only its unlocked doors' names (CR 709.5), and a name set by a continuous
     * effect replaces the printed one; otherwise either half of a split card matches.
     */
    public boolean permanentHasName(GameData gameData, Permanent permanent, String chosenName) {
        if (chosenName == null || permanent.isFaceDown()) {
            return false;
        }
        Card card = permanent.getCard();
        if (card.hasAllCardNames()) {
            return true;
        }
        String layeredName = isStaticEvaluationActive() ? null : computeStaticBonus(gameData, permanent).name();
        if (layeredName != null) {
            return chosenName.equals(layeredName);
        }
        if (card.getSubtypes().contains(CardSubtype.ROOM)) {
            return RoomNameSupport.unlockedDoorNames(permanent).contains(chosenName);
        }
        return card.hasName(chosenName);
    }

    /**
     * Whether {@code card} has {@code chosenName} (CR 709.4a), using its battlefield permanent's
     * names ({@link #permanentHasName}) when the card is on the battlefield.
     */
    public boolean cardHasChosenName(GameData gameData, Card card, String chosenName) {
        if (card == null || chosenName == null) {
            return false;
        }
        if (card.getType() == CardType.EMBLEM) {
            // CR 114.3: an emblem has no characteristics other than the abilities defined by its creator, so
            // it has no name even though the engine labels it with its source's name.
            return false;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(card.getId())) {
                    return permanentHasName(gameData, permanent, chosenName);
                }
            }
        }
        return card.hasName(chosenName);
    }

    /**
     * Returns the card's effective colors, using its battlefield permanent's layered colors when
     * present. During static evaluation the recursion-safe card path is retained. Cards outside
     * the battlefield, including spells, retain applicable global color-setting effects.
     */
    public Set<CardColor> getEffectiveCardColors(GameData gameData, Card card) {
        if (card == null) {
            return Set.of();
        }
        if (gameData != null && !isStaticEvaluationActive()) {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent permanent : battlefield) {
                    if (permanent.getCard().getId().equals(card.getId())) {
                        return getEffectiveColors(gameData, permanent);
                    }
                }
            }
        }
        if (gameData != null) {
            Set<CardColor> temporary = gameData.spellColorUntilEndOfTurn(card.getId()).orElse(null);
            if (temporary != null) {
                return addGraveyardAnimationColors(gameData, card, temporary);
            }
            Set<CardColor> indefinite = gameData.spellColorOverrides.get(card.getId());
            if (indefinite != null) {
                return addGraveyardAnimationColors(gameData, card, indefinite);
            }
            UUID affectedPlayerId = gameData.stack.stream()
                    .filter(entry -> entry.getCard() != null && entry.getCard().getId().equals(card.getId()))
                    .map(StackEntry::getControllerId)
                    .findFirst()
                    .orElseGet(() -> findNonBattlefieldCardOwner(gameData, card));
            List<CardColorLayerEffect> colorLayerEffects = new ArrayList<>();
            for (Map.Entry<UUID, List<Permanent>> battlefield : gameData.playerBattlefields.entrySet()) {
                for (Permanent permanent : battlefield.getValue()) {
                    if (permanent.isFaceDown() || gameData.losesAllAbilitiesByEffect(permanent)
                            || gameData.losesAllAbilitiesByEffect(permanent)) continue;
                    for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                        if (gameData.removesOwnStaticAbility(permanent.getId(), effect.getClass())) continue;
                        Set<CardColor> colors = null;
                        if (effect instanceof AllCardsAreColorlessEffect) {
                            colors = Set.of();
                        } else if (effect instanceof ControlledNonlandPermanentsAreColorEffect colorEffect
                                && battlefield.getKey().equals(affectedPlayerId) && !card.hasType(CardType.LAND)) {
                            colors = Set.of(colorEffect.color());
                        }
                        if (effect instanceof AllPermanentsGainChosenColorEffect && permanent.getChosenColor() != null) {
                            colorLayerEffects.add(new CardColorLayerEffect(
                                    permanent.getTimestamp(), Set.of(permanent.getChosenColor()), true));
                        } else if (colors != null) {
                            colorLayerEffects.add(new CardColorLayerEffect(permanent.getTimestamp(), colors, false));
                        }
                    }
                }
            }
            if (!colorLayerEffects.isEmpty()) {
                return addGraveyardAnimationColors(gameData, card,
                        applyCardColorLayerEffects(gameData, card, colorLayerEffects));
            }
        }

        return addGraveyardAnimationColors(gameData, card, intrinsicCardColors(gameData, card));
    }

    /**
     * Applies the color-changing static effects that affect a non-battlefield card in timestamp
     * order: setting effects replace the running color set, additive effects union into it.
     */
    private Set<CardColor> applyCardColorLayerEffects(GameData gameData, Card card,
                                                      List<CardColorLayerEffect> effects) {
        Set<CardColor> current = null;
        for (CardColorLayerEffect effect : effects.stream()
                .sorted(Comparator.comparingLong(CardColorLayerEffect::timestamp)).toList()) {
            if (effect.additive()) {
                EnumSet<CardColor> merged = EnumSet.noneOf(CardColor.class);
                merged.addAll(current != null ? current : intrinsicCardColors(gameData, card));
                merged.addAll(effect.colors());
                current = Set.copyOf(merged);
            } else {
                current = effect.colors();
            }
        }
        return current;
    }

    private Set<CardColor> intrinsicCardColors(GameData gameData, Card card) {
        Set<CardColor> intrinsic = EnumSet.noneOf(CardColor.class);
        if (card.getColors() != null) {
            intrinsic.addAll(card.getColors());
        }
        if (card.getColor() != null) {
            intrinsic.add(card.getColor());
        }
        ManaCost incorporated = gameData == null ? null
                : gameData.perpetualManaCostIncreases.get(card.getId());
        if (incorporated != null) {
            for (CardColor color : CardColor.values()) {
                ManaColor manaColor = ManaColor.fromCode(color.getCode());
                if (incorporated.countColorSymbols(manaColor) > 0) {
                    intrinsic.add(color);
                }
            }
        }
        return Set.copyOf(intrinsic);
    }

    private record CardColorLayerEffect(long timestamp, Set<CardColor> colors, boolean additive) {
    }

    private Set<CardColor> addGraveyardAnimationColors(GameData gameData, Card card, Set<CardColor> colors) {
        GameData.GraveyardCardAnimation animation = activeGraveyardCardAnimation(gameData, card);
        if (animation == null || animation.grantedColors().isEmpty()) {
            return Set.copyOf(colors);
        }
        Set<CardColor> result = EnumSet.noneOf(CardColor.class);
        result.addAll(colors);
        result.addAll(animation.grantedColors());
        return Set.copyOf(result);
    }

    private GameData.GraveyardCardAnimation activeGraveyardCardAnimation(GameData gameData, Card card) {
        if (gameData == null || card == null) {
            return null;
        }
        GameData.GraveyardCardAnimation animation = gameData.graveyardCardAnimation(card.getId()).orElse(null);
        if (animation == null || findCardInGraveyardById(gameData, card.getId()) == null) {
            return null;
        }
        return animation;
    }

    /**
     * Each of {@code cards}, read while it is still in its graveyard, as an exile step records it before the cards leave
     * (CR 608.2h: "the exiled card's toughness" is the card as it last existed in the graveyard, its graveyard-reading
     * characteristic-defining power and any change to graveyard cards included). Cards that leave together are read
     * before the first of them leaves.
     */
    public List<LastKnownCardStats> asInGraveyard(GameData gameData, Collection<Card> cards) {
        List<LastKnownCardStats> known = new ArrayList<>(cards.size());
        for (Card card : cards) {
            known.add(new LastKnownCardStats(card.getId(), getEffectiveCardPower(gameData, card),
                    getEffectiveCardToughness(gameData, card), card.getManaValue()));
        }
        return known;
    }

    /** Returns the current power of a non-battlefield card, including temporary graveyard changes. */
    public Integer getEffectiveCardPower(GameData gameData, Card card) {
        GameData.GraveyardCardAnimation animation = activeGraveyardCardAnimation(gameData, card);
        if (animation == null) {
            return characteristicPowerToughness(gameData, card, true);
        }
        return animation.power();
    }

    /** Returns the current toughness of a non-battlefield card, including temporary graveyard changes. */
    public Integer getEffectiveCardToughness(GameData gameData, Card card) {
        return getEffectiveCardToughness(gameData, card, null);
    }

    /**
     * Returns the current toughness of a non-battlefield card as seen by {@code holderId}, the player whose
     * hand, library, graveyard or exile the card is in. Used when the card has already left that zone, so a
     * characteristic-defining toughness still reads that player's permanents.
     */
    public Integer getEffectiveCardToughness(GameData gameData, Card card, UUID holderId) {
        GameData.GraveyardCardAnimation animation = activeGraveyardCardAnimation(gameData, card);
        if (animation == null) {
            return characteristicPowerToughness(gameData, card, false, holderId);
        }
        return animation.toughness();
    }

    private UUID findNonBattlefieldCardOwner(GameData gameData, Card card) {
        if (card.getOwnerId() != null) {
            return card.getOwnerId();
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (containsCard(gameData.playerHands.get(playerId), card)
                    || containsCard(gameData.playerDecks.get(playerId), card)
                    || containsCard(gameData.playerGraveyards.get(playerId), card)
                    || containsCard(gameData.getPlayerExiledCards(playerId), card)) {
                return playerId;
            }
        }
        for (StackEntry entry : gameData.stack) {
            if (entry.getCard() != null && entry.getCard().getId().equals(card.getId())) {
                return entry.getControllerId();
            }
        }
        return null;
    }

    private Integer characteristicPowerToughness(GameData gameData, Card card, boolean power) {
        return characteristicPowerToughness(gameData, card, power, null);
    }

    private Integer characteristicPowerToughness(GameData gameData, Card card, boolean power, UUID holderId) {
        // Callers without game state (e.g. static predicate checks) only see the printed characteristics.
        if (gameData == null) {
            return power ? card.getPower() : card.getToughness();
        }
        var perpetualBase = gameData.perpetualCardBasePowerToughness.get(card.getId());
        Integer value;
        Integer perpetualValue = perpetualBase == null ? null
                : power ? perpetualBase.power() : perpetualBase.toughness();
        if (perpetualValue != null) {
            value = perpetualValue;
        } else {
            value = power ? card.getPower() : card.getToughness();
            UUID ownerId = holderId != null ? holderId : findNonBattlefieldCardOwner(gameData, card);
            boolean graveyardAbilitiesLost = findCardInGraveyardById(gameData, card.getId()) != null
                    && graveyardCardsHaveLostAllAbilities(gameData);
            if (ownerId != null && amountEvaluationService != null && !graveyardAbilitiesLost) {
                for (CardEffect effect : card.getStaticEffects()) {
                    if (effect instanceof SetPowerToughnessToAmountEffect characteristic) {
                        value = amountEvaluationService.evaluate(gameData,
                                power ? characteristic.power() : characteristic.toughness(),
                                AmountContext.forStaticEffect(new Permanent(card), ownerId));
                        break;
                    }
                    if (effect instanceof BecomeCreatureOutsideBattlefieldEffect creature) {
                        value = power ? creature.power() : creature.toughness();
                        break;
                    }
                }
            }
        }
        if (value == null) {
            return null;
        }
        var cardModifier = gameData.perpetualCardPowerToughnessModifiers.get(card.getId());
        if (cardModifier != null) {
            value += power ? cardModifier.power() : cardModifier.toughness();
        }
        // Counters retained across a zone change (Me, the Immortal) stay on the card, so they still
        // modify its power and toughness outside the battlefield.
        Map<CounterType, Integer> retainedCounters = gameData.countersPreservedAcrossZoneChanges.get(card.getId());
        if (retainedCounters != null) {
            value += retainedCounters.getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)
                    - retainedCounters.getOrDefault(CounterType.MINUS_ONE_MINUS_ONE, 0);
        }
        return value;
    }

    private boolean containsCard(List<Card> cards, Card sought) {
        return cards != null && cards.stream().anyMatch(card -> card.getId().equals(sought.getId()));
    }

    /** Returns the effective single color used by legacy color-specific APIs, or null if colorless. */
    public CardColor getEffectiveCardColor(GameData gameData, Card card) {
        Set<CardColor> colors = getEffectiveCardColors(gameData, card);
        if (colors.isEmpty()) {
            return null;
        }
        CardColor printed = card.getColor();
        return printed != null && colors.contains(printed) ? printed : colors.iterator().next();
    }

    /**
     * Returns colors without re-entering static-bonus assembly when called from a static effect's
     * amount evaluation. The layered pass already has the layer-5 result available for every
     * permanent; outside that pass, the permanent's intrinsic colors are the safe fallback.
     */
    public Set<CardColor> colorsForStaticEvaluation(Permanent permanent) {
        CharacteristicState state = LayerSystemService.activeStateFor(permanent.getId());
        if (state != null) {
            return Set.copyOf(state.getColors());
        }
        return Set.copyOf(permanent.getEffectiveColors());
    }

    /** Returns {@code true} if the permanent currently has the given color (layer-5 aware). */
    public boolean hasColor(GameData gameData, Permanent permanent, CardColor color) {
        return color != null && getEffectiveColors(gameData, permanent).contains(color);
    }

    /**
     * Layer-5 aware replacement for {@link Permanent#getEffectiveColor()} at call sites that
     * need a single color (legacy single-color APIs like the color damage-prevention counters):
     * a color-setting effect's replacement color when one applies, otherwise the intrinsic
     * answer. Multicolor-sensitive checks should iterate {@link #getEffectiveColors} instead.
     */
    public CardColor getEffectiveColor(GameData gameData, Permanent permanent) {
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (bonus.colorOverriding()) {
            return bonus.grantedColors().isEmpty() ? null : bonus.grantedColors().iterator().next();
        }
        return permanent.getEffectiveColor();
    }

    /**
     * Returns {@code true} if the permanent has been granted the given effect type
     * by static effects from other permanents (e.g. via {@code GrantEffectEffect}).
     */
    private boolean hasGrantedEffect(GameData gameData, Permanent permanent, Class<? extends CardEffect> effectType) {
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(effectType::isInstance);
    }

    public List<CardEffect> getGrantedEffects(GameData gameData, Permanent permanent) {
        return List.copyOf(computeStaticBonus(gameData, permanent).grantedEffects());
    }

    /**
     * As {@link #getGrantedEffects(GameData, Permanent)} for a permanent that is not on the battlefield yet
     * (it is entering): its controller is supplied because battlefield membership cannot provide it.
     */
    public List<CardEffect> getGrantedEffects(GameData gameData, Permanent permanent, UUID controllerId) {
        return withEnteringPermanent(gameData, controllerId, permanent,
                () -> List.copyOf(computeStaticBonus(gameData, permanent, controllerId).grantedEffects()));
    }

    /** Returns whether a static effect prevents the permanent from having or gaining a keyword. */
    public boolean cantHaveOrGainKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(effect -> effect instanceof CantHaveOrGainKeywordEffect restriction
                        && restriction.keyword() == keyword);
    }

    /** Returns whether a static effect prevents the permanent from becoming suspected. */
    public boolean cantBecomeSuspected(GameData gameData, Permanent permanent) {
        if (permanent.getCard().getStaticEffects().stream()
                .anyMatch(CantBecomeSuspectedEffect.class::isInstance)
                || hasAttachedPermanentRestriction(gameData, permanent, CantBecomeSuspectedEffect.class::isInstance)) {
            return true;
        }
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(CantBecomeSuspectedEffect.class::isInstance);
    }

    /**
     * Returns {@code true} if the permanent cannot have counters placed on it,
     * either from its own static effects or from effects granted by other permanents.
     */
    public boolean cantHaveCounters(GameData gameData, Permanent permanent) {
        return cantHaveCountersForController(gameData, permanent, null);
    }

    /**
     * Returns {@code true} if the permanent cannot become untapped, either from its own static
     * effects or from effects granted by other permanents.
     */
    public boolean cantBecomeUntapped(GameData gameData, Permanent permanent) {
        if (!hasLostPrintedAbilities(gameData, permanent)
                && permanent.getCard().getStaticEffects().stream()
                .anyMatch(effect -> effect instanceof CantBecomeUntappedEffect restriction
                        && restriction.subject() instanceof PermanentSubject.Source)) {
            return true;
        }
        return hasAttachmentRestriction(gameData, permanent, CantBecomeUntappedEffect.class)
                || computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(CantBecomeUntappedEffect.class::isInstance)
                || stunCountersLocked(gameData, permanent)
                || gameData.hasFloatingEffectOnPermanent(permanent.getId(), CantBecomeUntappedEffect.class);
    }

    /**
     * Returns {@code true} if the permanent has a stun counter that an opponent's
     * {@link StunCountersCantBeRemovedFromOpponentsPermanentsEffect} keeps on it, so the stun
     * replacement (CR 122.1d) can't remove it and the permanent stays tapped.
     */
    private boolean stunCountersLocked(GameData gameData, Permanent permanent) {
        if (permanent.getCounterCount(CounterType.STUN) <= 0) {
            return false;
        }
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        for (Map.Entry<UUID, List<Permanent>> entry : gameData.playerBattlefields.entrySet()) {
            if (entry.getKey().equals(controllerId)) continue;
            for (Permanent source : entry.getValue()) {
                if (!source.isFaceDown() && !hasLostPrintedAbilities(gameData, source)
                        && source.getCard().getStaticEffects().stream()
                        .anyMatch(StunCountersCantBeRemovedFromOpponentsPermanentsEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns whether the permanent cannot be sacrificed. */
    public boolean cantBeSacrificed(GameData gameData, Permanent permanent) {
        // Call for Aid: the player its effect names can't sacrifice it this turn.
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        if (gameData.floatingEffectsOnPermanent(permanent.getId(), ControllerCantSacrificeTargetThisTurnEffect.class)
                .stream().anyMatch(forbidden -> forbidden.controllerId().equals(controllerId))) {
            return true;
        }
        if (!permanent.isFaceDown() && !computeStaticBonus(gameData, permanent).losesAllAbilities()
                && permanent.getCard().getStaticEffects().stream()
                .anyMatch(CantBeSacrificedEffect.class::isInstance)) {
            return true;
        }
        return hasAttachedPermanentRestriction(gameData, permanent, CantBeSacrificedEffect.class::isInstance)
                || hasGrantedEffect(gameData, permanent, CantBeSacrificedEffect.class);
    }

    public boolean cantHaveCountersForController(GameData gameData, Permanent permanent, UUID controllerId) {
        if (!hasLostPrintedAbilities(gameData, permanent)
                && permanent.getCard().getStaticEffects().stream()
                .anyMatch(effect -> effect instanceof CantHaveCountersEffect restriction
                        && restriction.counterType() == null
                        && restriction.subject() instanceof PermanentSubject.Source)) {
            return true;
        }
        if (hasAttachmentRestriction(gameData, permanent, CantHaveCountersEffect.class)) return true;
        StaticBonus bonus = computeStaticBonusForController(gameData, permanent, controllerId);
        if (bonus.grantedEffects().stream().anyMatch(effect -> cantHaveCountersOfKind(effect, null))) {
            return true;
        }
        if (gameData.hasFloatingEffectOnPermanent(permanent.getId(),
                com.github.laxika.magicalvibes.model.effect.CantGetCountersEffect.class)) {
            return true;
        }
        // Solemnity: "Counters can't be put on artifacts, creatures, enchantments, or lands."
        // Planeswalkers (that aren't also one of those types) are unaffected and still get loyalty.
        return anyBattlefieldHasStaticEffect(gameData, CountersCantBePlacedEffect.class)
                && isArtifactCreatureEnchantmentOrLand(gameData, permanent, bonus);
    }

    /** Reads restrictions created by an attachment, independently of its recipient's abilities. */
    private boolean hasAttachmentRestriction(GameData gameData, Permanent permanent,
                                              Class<? extends CardEffect> restrictionType) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent source : battlefield) {
                if (!permanent.getId().equals(source.getAttachedTo())
                        || hasLostPrintedAbilities(gameData, source)) continue;
                for (CardEffect effect : source.getCard().getStaticEffects()) {
                    if (effect instanceof CantHaveCountersEffect restriction && restriction.counterType() != null) {
                        continue;
                    }
                    PermanentSubject subject = effect instanceof CantHaveCountersEffect restriction
                            ? restriction.subject()
                            : effect instanceof CantBecomeUntappedEffect restriction ? restriction.subject() : null;
                    // "Enchanted permanent" applies to a noncreature host too; every other form only to a creature.
                    if (restrictionType.isInstance(effect)
                            && staticSubjectNames(gameData, source, null, null, permanent, subject)
                            && (Subjects.attached().equals(subject) || isCreature(gameData, permanent))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * The restrictions that Auras and Equipment attached to {@code permanent} impose on it through
     * {@link AttachedPermanentRestrictionEffect}, each with the attachment imposing it. They are the
     * attachment's own static abilities, not abilities of {@code permanent} (CR 113.10), so they still
     * apply after {@code permanent} loses all its abilities (CR 613.1f). An attachment that lost its own
     * abilities, or whose effects are ignored for the permanent's controller (Volrath's Curse), imposes
     * nothing. {@link ConditionalEffect} and {@link EnchantedPermanentConditionalEffect} wrappers are
     * unwrapped to their currently active restriction.
     */
    private List<AttachedRestriction> attachedPermanentRestrictions(GameData gameData, Permanent permanent) {
        List<AttachedRestriction> restrictions = new ArrayList<>();
        UUID permanentControllerId = findPermanentController(gameData, permanent.getId());
        gameData.forEachPermanent((controllerId, source) -> {
            if (!source.isAttached() || !permanent.getId().equals(source.getAttachedTo())
                    || gameData.ignoresEffectOf(source.getId(), IgnoreSourceAuraEffectsUntilEndOfTurnEffect.class, permanentControllerId)
                    || hasLostPrintedAbilities(gameData, source)) {
                return;
            }
            for (CardEffect effect : source.getCard().getStaticEffects()) {
                collectAttachedRestriction(gameData, source, controllerId, permanent, effect, restrictions);
            }
        });
        return restrictions;
    }

    private void collectAttachedRestriction(GameData gameData, Permanent source, UUID sourceControllerId,
                                            Permanent permanent, CardEffect effect,
                                            List<AttachedRestriction> restrictions) {
        if (effect instanceof AttachedPermanentRestrictionEffect restriction) {
            restrictions.add(new AttachedRestriction(source, restriction.restriction()));
        } else if (effect instanceof ConditionalEffect conditional) {
            if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStaticEffect(source, sourceControllerId))) {
                collectAttachedRestriction(gameData, source, sourceControllerId, permanent,
                        conditional.wrapped(), restrictions);
            }
        } else if (effect instanceof EnchantedPermanentConditionalEffect cond) {
            CardEffect active = matchesEnchantedPermanentPredicate(gameData, source, permanent, cond.filter())
                    ? cond.ifMatch()
                    : cond.ifNotMatch();
            if (active != null) {
                collectAttachedRestriction(gameData, source, sourceControllerId, permanent, active, restrictions);
            }
        }
    }

    /**
     * Whether an Aura or Equipment attached to {@code permanent} imposes a restriction {@code matcher} accepts through
     * {@link AttachedPermanentRestrictionEffect} (see {@link #attachedPermanentRestrictions(GameData, Permanent)}).
     */
    public boolean hasAttachedPermanentRestriction(GameData gameData, Permanent permanent,
                                                   Predicate<CardEffect> matcher) {
        return attachedPermanentRestrictions(gameData, permanent).stream()
                .anyMatch(attached -> matcher.test(attached.restriction()));
    }

    /**
     * The restrictions of {@code type} that Auras and Equipment attached to {@code permanent} impose on it through
     * {@link AttachedPermanentRestrictionEffect} (see {@link #attachedPermanentRestrictions(GameData, Permanent)}).
     */
    public <T extends CardEffect> List<T> attachedPermanentRestrictions(GameData gameData, Permanent permanent,
                                                                       Class<T> type) {
        return attachedPermanentRestrictions(gameData, permanent).stream()
                .map(AttachedRestriction::restriction)
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

    /**
     * Display-only attribution of the restrictions attached Auras and Equipment currently impose on
     * {@code permanent} through {@link AttachedPermanentRestrictionEffect}, each named after the
     * attachment imposing it, so the client can list them on the restricted permanent even though
     * they are not abilities granted to it.
     */
    public List<GrantedEffectAttribution> attachedRestrictionAttributions(GameData gameData, Permanent permanent) {
        return attachedPermanentRestrictions(gameData, permanent).stream()
                .map(attached -> new GrantedEffectAttribution(
                        FaceDownVisibility.publicName(gameData, attached.source()), attached.restriction()))
                .toList();
    }

    /** One {@link AttachedPermanentRestrictionEffect} restriction and the attachment imposing it. */
    private record AttachedRestriction(Permanent source, CardEffect restriction) {
    }

    /** Whether a resolution's {@code CantGetCountersEffect} keeps {@code playerId} from getting counters (Suncleanser). */
    private static boolean playerCountersLocked(GameData gameData, UUID playerId) {
        return gameData.floatingEffectsOfTypes(
                        com.github.laxika.magicalvibes.model.effect.CantGetCountersEffect.class).stream()
                .anyMatch(lock -> playerId.equals(lock.affectedPlayerId()));
    }

    /**
     * Whether {@code playerId} can't get counters of any kind: players can't get counters (Solemnity), or a
     * resolution's {@code CantGetCountersEffect} says that player can't (Suncleanser). Every player counter path asks
     * it before a counter is put on the player.
     */
    public boolean playerCantGetCounters(GameData gameData, UUID playerId) {
        return playersCantGetCounters(gameData) || playerId != null && playerCountersLocked(gameData, playerId);
    }

    private boolean isArtifactCreatureEnchantmentOrLand(
            GameData gameData, Permanent permanent, StaticBonus bonus) {
        return hasEffectiveCardType(permanent, bonus, CardType.ARTIFACT)
                || isCreatureWithBonus(gameData, permanent, bonus)
                || hasEffectiveCardType(permanent, bonus, CardType.ENCHANTMENT)
                || hasEffectiveCardType(permanent, bonus, CardType.LAND);
    }

    /**
     * Returns {@code true} if the permanent cannot be the target of opponents' abilities,
     * either from its own static effects or from effects granted by other permanents.
     * This does NOT block spells â€” only activated and triggered abilities.
     */
    public boolean cantBeTargetOfOpponentAbilities(GameData gameData, Permanent permanent) {
        if (!gameData.losesAllAbilitiesByEffect(permanent)
                && !computeStaticBonus(gameData, permanent).losesAllAbilities()) {
            if (permanent.getCard().getStaticEffects().stream()
                    .anyMatch(GameQueryService::isOpponentAbilityRestriction)) {
                return true;
            }
        }
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(GameQueryService::isOpponentAbilityRestriction);
    }

    /**
     * Returns {@code true} if the target can't be targeted by an ability whose source has any card type
     * named by an abilities-restriction on the target (static or granted), e.g. Elenda, Saint of Dusk's
     * hexproof from instants or Jaheira's protection from artifact and enchantment sources.
     */
    public boolean cantBeTargetedByAbilityFromCardType(GameData gameData, Permanent target,
                                                        Card sourceCard, UUID sourcePermanentId,
                                                        UUID sourceControllerId) {
        return abilityCardTypeRestriction(gameData, target, sourceCard, sourcePermanentId, sourceControllerId)
                .isPresent();
    }

    /**
     * The first card type named by an abilities-restriction on the target that the ability's source has,
     * or empty when no such restriction applies. Lets callers name the restricted source type in messages.
     */
    public Optional<CardType> abilityCardTypeRestriction(GameData gameData, Permanent target,
                                                         Card sourceCard, UUID sourcePermanentId,
                                                         UUID sourceControllerId) {
        UUID targetControllerId = findPermanentController(gameData, target.getId());
        List<CardEffect> restrictions = new ArrayList<>();
        if (!gameData.losesAllAbilitiesByEffect(target)
                && !computeStaticBonus(gameData, target).losesAllAbilities()) {
            restrictions.addAll(target.getCard().getStaticEffects());
        }
        restrictions.addAll(computeStaticBonus(gameData, target).grantedEffects());
        attachedPermanentRestrictions(gameData, target).forEach(attached -> restrictions.add(attached.restriction()));
        return restrictions.stream()
                .filter(TargetingRestrictionEffect.class::isInstance)
                .flatMap(effect -> ((TargetingRestrictionEffect) effect).sourceCardTypes().stream())
                .distinct()
                .filter(sourceCardType -> sourceHasCardType(
                        gameData, sourceCard, sourcePermanentId, sourceControllerId, sourceCardType)
                        && restrictions.stream().anyMatch(effect -> isAbilityCardTypeRestriction(
                        effect, sourceCardType, targetControllerId, sourceControllerId)))
                .findFirst();
    }

    private boolean sourceHasCardType(GameData gameData, Card sourceCard, UUID sourcePermanentId,
                                      UUID sourceControllerId, CardType sourceCardType) {
        Permanent sourcePermanent = sourcePermanentId == null
                ? findPermanentByCardId(gameData, sourceCard == null ? null : sourceCard.getId())
                : findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent != null) {
            return hasEffectiveCardType(sourcePermanent, computeStaticBonus(gameData, sourcePermanent), sourceCardType);
        }
        return sourceCard != null && cardHasType(sourceCard, sourceCardType, gameData, sourceControllerId);
    }

    private static boolean isAbilityCardTypeRestriction(CardEffect effect, CardType sourceCardType,
                                                        UUID targetControllerId, UUID sourceControllerId) {
        return effect instanceof TargetingRestrictionEffect restriction
                && restriction.kind() == TargetingSourceKind.ABILITIES
                && restriction.sourceCardTypes().contains(sourceCardType)
                && (!restriction.opponentOnly()
                || targetControllerId == null
                || sourceControllerId == null
                || !targetControllerId.equals(sourceControllerId));
    }

    public Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (cardId.equals(permanent.getCard().getId())) return permanent;
            }
        }
        return null;
    }

    private static boolean isOpponentAbilityRestriction(CardEffect effect) {
        return effect instanceof TargetingRestrictionEffect r
                && r.kind() == TargetingSourceKind.ABILITIES
                && r.mode() == TargetColorMode.ANY
                && r.opponentOnly()
                && r.sourceCardTypes().isEmpty();
    }

    /**
     * Returns {@code true} if the permanent has been granted hexproof (opponents' spells and
     * abilities can't target it), e.g. by Asceticism. Only checks effects granted by other
     * permanents, matching the historical behavior of this shroud/hexproof-like marker.
     */
    public boolean cantBeTargetedBySpellsOrAbilities(GameData gameData, Permanent permanent) {
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(e -> e instanceof TargetingRestrictionEffect r
                        && r.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES
                        && r.mode() == TargetColorMode.ANY
                        && r.hexproofLike());
    }

    /**
     * Returns whether an opponent-controlled spell or ability is blocked by a granted targeting
     * restriction on this permanent. Hexproof-ignoring effects bypass only restrictions marked as
     * hexproof-like; cards that use the same wording without granting hexproof remain protected.
     */
    public boolean cantBeTargetedByOpponentSpellsOrAbilities(GameData gameData, Permanent permanent,
                                                               UUID targetingControllerId) {
        // "Enchanted creature can't be the target of spells or abilities your opponents control": the Aura's own
        // restriction, its "opponents" those of the Aura's controller.
        boolean restrictedByAttachment = attachedPermanentRestrictions(gameData, permanent).stream()
                .anyMatch(attached -> attached.restriction() instanceof TargetingRestrictionEffect r
                        && r.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES
                        && r.opponentOnly()
                        && r.mode() == TargetColorMode.ANY
                        && r.sourceCardTypes().isEmpty()
                        && !targetingControllerId.equals(findPermanentController(gameData, attached.source().getId())));
        if (restrictedByAttachment) {
            return true;
        }
        UUID targetControllerId = findPermanentController(gameData, permanent.getId());
        if (targetControllerId == null) {
            return false;
        }
        boolean opponentOfController = !targetControllerId.equals(targetingControllerId);
        boolean hexproofLifted = opponentOfController
                && (ignoresOpponentPermanentHexproof(gameData, targetingControllerId)
                || (isCreature(gameData, permanent)
                && ignoresOpponentCreatureHexproof(gameData, targetingControllerId)));
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(e -> e instanceof TargetingRestrictionEffect r
                        && r.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES
                        && r.opponentOnly()
                        && r.mode() == TargetColorMode.ANY
                        && (r.opponentsOf() != null
                        ? !r.opponentsOf().equals(targetingControllerId)
                        : opponentOfController)
                        && (!hexproofLifted || !r.hexproofLike()));
    }

    /** Returns whether this permanent is protected from spells and abilities controlled by its controller. */
    public boolean cantBeAffectedByOwnEffects(GameData gameData, Permanent permanent,
                                              UUID effectControllerId) {
        if (permanent == null || effectControllerId == null) {
            return false;
        }
        UUID targetControllerId = findPermanentController(gameData, permanent.getId());
        if (!effectControllerId.equals(targetControllerId)) {
            return false;
        }

        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (!gameData.losesAllAbilitiesByEffect(permanent)
                && !bonus.losesAllAbilities()
                && permanent.getCard().getStaticEffects().stream()
                .anyMatch(OwnEffectsCantAffectSourceEffect.class::isInstance)) {
            return true;
        }
        return bonus.grantedEffects().stream()
                .anyMatch(OwnEffectsCantAffectSourceEffect.class::isInstance);
    }

    /**
     * Returns {@code true} if {@code controllerId} may target opponents' hexproof creatures as though they didn't have
     * hexproof: an {@link IgnoreOpponentHexproofEffect} over creatures (Glaring Spotlight; Detection Tower floated).
     * Callers ask {@link #ignoresOpponentPermanentHexproof} too. Only hexproof is lifted; shroud and protection still
     * apply.
     */
    public boolean ignoresOpponentCreatureHexproof(GameData gameData, UUID controllerId) {
        return hasHexproofIgnore(gameData, controllerId,
                ignore -> ignore.permanents() == IgnoreOpponentHexproofEffect.Breadth.CREATURES);
    }

    /**
     * Returns whether ward abilities of creatures controlled by the controller's opponents should
     * be suppressed by a static effect on the controller's battlefield.
     */
    public boolean preventsOpponentCreatureWardTriggers(GameData gameData, UUID controllerId) {
        if (controllerId == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getStaticEffects().stream()
                        .anyMatch(PreventOpponentCreatureWardTriggersEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether {@code controllerId} may target any opponent-controlled permanent with hexproof as though it
     * didn't have hexproof: an {@link IgnoreOpponentHexproofEffect} over permanents (Kaya, Bane of the Dead). Only
     * hexproof is lifted; shroud and protection still apply.
     */
    public boolean ignoresOpponentPermanentHexproof(GameData gameData, UUID controllerId) {
        return hasHexproofIgnore(gameData, controllerId,
                ignore -> ignore.permanents() == IgnoreOpponentHexproofEffect.Breadth.PERMANENTS);
    }

    /**
     * Returns {@code true} if {@code controllerId} may target opponents with hexproof as though they didn't have
     * hexproof (Kaya, Bane of the Dead; Detection Tower's activated ability). Only hexproof is lifted; shroud and
     * protection still apply.
     */
    public boolean ignoresOpponentPlayerHexproof(GameData gameData, UUID controllerId) {
        return hasHexproofIgnore(gameData, controllerId, IgnoreOpponentHexproofEffect::opponents);
    }

    /**
     * Whether a matching {@link IgnoreOpponentHexproofEffect} is {@code controllerId}'s: an active static ability of a
     * permanent they control (none of a face-down or ability-less one's, nor one an effect removed, CR 613.1f), or one
     * floated on them.
     */
    private boolean hasHexproofIgnore(GameData gameData, UUID controllerId,
                                      java.util.function.Predicate<IgnoreOpponentHexproofEffect> matches) {
        if (controllerId == null) {
            return false;
        }
        for (CardEffect effect : gameData.floatingPlayerEffects(controllerId, IgnoreOpponentHexproofEffect.class)) {
            if (matches.test((IgnoreOpponentHexproofEffect) effect)) {
                return true;
            }
        }
        for (var active : activeStaticEffects(gameData, controllerId, IgnoreOpponentHexproofEffect.class)) {
            if (matches.test(active.effect())
                    && !gameData.removesOwnStaticAbility(active.source().getId(), IgnoreOpponentHexproofEffect.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the permanent cannot have -1/-1 counters placed on it,
     * from effects granted by other permanents (e.g. Melira, Sylvok Outcast).
     */
    public boolean cantHaveMinusOneMinusOneCounters(GameData gameData, Permanent permanent) {
        UUID recipientController = findPermanentController(gameData, permanent.getId());
        if (recipientController != null && isCreature(gameData, permanent)) {
            // The restriction is a rule of the source's own static ability, not an ability granted to
            // the creature, so the recipient losing its abilities doesn't lift it.
            for (ActiveStaticEffect<GrantEffectEffect> active
                    : activeStaticEffects(gameData, recipientController, GrantEffectEffect.class)) {
                GrantEffectEffect grant = active.effect();
                if (cantHaveCountersOfKind(grant.effect(), CounterType.MINUS_ONE_MINUS_ONE)
                        && grant.subject() instanceof PermanentSubject.ControlledBy
                        && staticSubjectNames(gameData, active.source(), active.controllerId(),
                        recipientController, permanent, grant.subject())) {
                    return true;
                }
            }
        }
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(effect -> cantHaveCountersOfKind(effect, CounterType.MINUS_ONE_MINUS_ONE));
    }

    /**
     * Whether {@code effect} is a {@link CantHaveCountersEffect} for counters of {@code counterType} ({@code null}: the
     * restriction on every kind).
     */
    private static boolean cantHaveCountersOfKind(CardEffect effect, CounterType counterType) {
        return effect instanceof CantHaveCountersEffect restriction && restriction.counterType() == counterType;
    }

    /**
     * Returns {@code true} if the permanent cannot have +1/+1 counters placed on it, including
     * blanket counter locks and effects granted by other permanents.
     */
    public boolean cantHavePlusOnePlusOneCounters(GameData gameData, Permanent permanent) {
        return cantHavePlusOnePlusOneCountersForController(gameData, permanent, null);
    }

    /**
     * Controller-aware form used while a permanent is entering the battlefield, before its
     * battlefield controller can be resolved from the game state.
     */
    public boolean cantHavePlusOnePlusOneCounters(
            GameData gameData, Permanent permanent, UUID controllerId) {
        return cantHavePlusOnePlusOneCountersForController(gameData, permanent, controllerId);
    }

    private boolean cantHavePlusOnePlusOneCountersForController(
            GameData gameData, Permanent permanent, UUID controllerId) {
        if (cantHaveCountersForController(gameData, permanent, controllerId)) {
            return true;
        }
        if (getActiveStaticEffects(gameData, permanent).stream()
                .anyMatch(effect -> cantHaveCountersOfKind(effect, CounterType.PLUS_ONE_PLUS_ONE)
                        && ((CantHaveCountersEffect) effect).subject() instanceof PermanentSubject.Source)) {
            return true;
        }
        UUID recipientController = controllerId != null ? controllerId
                : findPermanentController(gameData, permanent.getId());
        if (recipientController != null && isCreature(gameData, permanent)) {
            if (activeStaticEffects(gameData, CantHaveCountersEffect.class).stream()
                    .anyMatch(active -> active.effect().counterType() == CounterType.PLUS_ONE_PLUS_ONE
                            && active.effect().subject() instanceof PermanentSubject.ControlledBy
                            && staticSubjectNames(gameData, active.source(), active.controllerId(),
                            recipientController, permanent, active.effect().subject()))) {
                return true;
            }
        }
        StaticBonus bonus = computeStaticBonusForController(gameData, permanent, controllerId);
        return bonus.grantedEffects().stream()
                .anyMatch(effect -> cantHaveCountersOfKind(effect, CounterType.PLUS_ONE_PLUS_ONE));
    }

    /**
     * Applies Vizier of Remedies-style replacement effects (CR 616) that reduce the number of -1/-1
     * counters put on a creature its controller controls. A {@code count} of zero or fewer is
     * returned unchanged â€” no counters "would be put", so the replacement never applies.
     */
    public int reduceMinusOneMinusOneCounters(GameData gameData, UUID controllerId, int count) {
        return replaceCounters(gameData, controllerId, CounterType.MINUS_ONE_MINUS_ONE, count, true);
    }

    /**
     * Convenience overload of {@link #reduceMinusOneMinusOneCounters(GameData, UUID, int)} that
     * resolves the affected creature's controller from a permanent already on the battlefield.
     */
    public int reduceMinusOneMinusOneCounters(GameData gameData, Permanent permanent, int count) {
        return reduceMinusOneMinusOneCounters(gameData, findPermanentController(gameData, permanent.getId()), count);
    }

    /**
     * Applies replacement effects that modify +1/+1 counters put on a creature its controller
     * controls. Replacements are applied in battlefield order, so multiple copies of the same
     * effect stack and mixed replacement effects have a deterministic order. A {@code count} of
     * zero or fewer is returned unchanged â€” no counters "would be put", so no replacement applies.
     * Callers that already know the recipient is a creature (e.g. as-enters) use the UUID overload;
     * the permanent overload also rejects non-creatures.
     */
    public int replacePlusOnePlusOneCounters(GameData gameData, UUID controllerId, int count) {
        return replaceCounters(gameData, controllerId, CounterType.PLUS_ONE_PLUS_ONE, count, true);
    }

    /**
     * Applies all counter replacements for one counter-placement event. The replacements are
     * applied in battlefield order, which gives multiple replacement effects a stable order while
     * preserving the existing behavior of the +1/+1 and -1/-1 pipelines.
     */
    public int replaceCounters(GameData gameData, UUID controllerId, CounterType counterType,
                               int count, boolean affectedPermanentIsCreature) {
        return replaceCounters(gameData, controllerId, counterType, count,
                affectedPermanentIsCreature, false);
    }

    /** Applies counter replacements with the affected permanent's effective artifact status. */
    public int replaceCounters(GameData gameData, UUID controllerId, CounterType counterType,
                               int count, boolean affectedPermanentIsCreature,
                               boolean affectedPermanentIsArtifact) {
        return replaceCounters(gameData, controllerId, counterType, count,
                affectedPermanentIsCreature, affectedPermanentIsArtifact, null, false);
    }

    private int replaceCounters(GameData gameData, UUID controllerId, CounterType counterType,
                                int count, boolean affectedPermanentIsCreature,
                                boolean affectedPermanentIsArtifact, Permanent affectedPermanent,
                                boolean affectedPermanentIsEntering) {
        return replaceCounters(gameData, controllerId, counterType, count,
                affectedPermanentIsCreature, affectedPermanentIsArtifact, affectedPermanent,
                affectedPermanentIsEntering, false);
    }

    private int replaceCounters(GameData gameData, UUID controllerId, CounterType counterType,
                                int count, boolean affectedPermanentIsCreature,
                                boolean affectedPermanentIsArtifact, Permanent affectedPermanent,
                                boolean affectedPermanentIsEntering, boolean modularAbility) {
        if (count <= 0 || controllerId == null) {
            return count;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int result = count;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                // Its own replacement abilities and those granted to it, as the layers leave them (CR 613.1f).
                List<CardEffect> replacementEffects = new ArrayList<>(staticEffectsIncludingTemporary(
                        gameData, permanent, controllerId));
                replacementEffects.addAll(computeStaticBonus(gameData, permanent).grantedEffects());
                for (CardEffect effect : replacementEffects) {
                    boolean applies;
                    if (effect instanceof AddOnePlusOneCountersEffect placement) {
                        applies = placementReplacementApplies(gameData, placement, permanent, controllerId,
                                affectedPermanent, controllerId, null, counterType,
                                affectedPermanentIsCreature, affectedPermanentIsArtifact);
                    } else {
                        applies = effect instanceof CounterReplacementEffect replacement
                                && (!replacement.appliesOnlyToModularAbility() || modularAbility)
                                && !replacement.appliesToAllPermanents()
                                && replacement.appliesTo(counterType, affectedPermanentIsCreature,
                                affectedPermanentIsArtifact, permanent, affectedPermanent);
                    }
                    if (applies) {
                        result = MaroGoneNutsSupport.apply(gameData, effect,
                                ((CounterReplacementEffect) effect).replace(counterType, result));
                    }
                }
            }
        }
        result = applyOtherPlayersPlacementReplacements(gameData, controllerId, affectedPermanent, counterType,
                result, affectedPermanentIsCreature, affectedPermanentIsArtifact);
        for (CardEffect effect : playerGrantedPlusOneReplacements(gameData, controllerId, null, counterType,
                affectedPermanent, affectedPermanentIsCreature, affectedPermanentIsArtifact)) {
            result = MaroGoneNutsSupport.apply(
                    gameData, effect, ((CounterReplacementEffect) effect).replace(counterType, result));
        }
        result = applyGlobalCounterReplacements(gameData, counterType, result,
                affectedPermanentIsCreature, affectedPermanentIsArtifact);
        result = applyPlanarCounterReplacements(gameData, controllerId, counterType, result,
                affectedPermanent, affectedPermanentIsCreature, affectedPermanentIsArtifact);
        if (affectedPermanentIsEntering) {
            result = applyEnteringPermanentReplacements(gameData, controllerId, null, counterType, result,
                    affectedPermanent);
        }
        return limitCounters(gameData, affectedPermanent, controllerId, counterType, result);
    }

    /**
     * Whether a counter-placement replacement applies to counters {@code placingPlayerId} puts on {@code affected}, a
     * permanent {@code affectedControllerId} controls or will control as it enters: the kind, the permanents it names
     * (judged against its source and the source's controller) and who puts them. A placement that names no player is
     * put by the affected permanent's controller (Vorinclex's and Halving Season's rulings). {@code creature} and
     * {@code artifact} are the placement's own reads of the affected permanent: layered on the battlefield, printed as
     * it enters.
     */
    private boolean placementReplacementApplies(GameData gameData, AddOnePlusOneCountersEffect replacement,
                                                Permanent source, UUID sourceControllerId, Permanent affected,
                                                UUID affectedControllerId, UUID placingPlayerId,
                                                CounterType counterType, boolean creature, boolean artifact) {
        if (replacement.permanents() == null || !replacement.replacesKind(counterType)) {
            return false;
        }
        UUID placer = placingPlayerId != null ? placingPlayerId : affectedControllerId;
        return replacement.acceptsPlacer(sourceControllerId, placer)
                && placementAffects(gameData, replacement.permanents(), source, sourceControllerId, affected,
                affectedControllerId, creature, artifact);
    }

    /**
     * Judges a counter-placement replacement's permanents: type and control from the placement's own reads, subtypes
     * from the layered board (printed for a permanent that is entering), any other leaf through
     * {@link PredicateEvaluationService}.
     */
    private boolean placementAffects(GameData gameData, PermanentPredicate predicate, Permanent source,
                                     UUID sourceControllerId, Permanent affected, UUID affectedControllerId,
                                     boolean creature, boolean artifact) {
        return switch (predicate) {
            case PermanentTruePredicate ignored -> true;
            case PermanentIsCreaturePredicate ignored -> creature;
            case PermanentIsArtifactPredicate ignored -> artifact;
            case PermanentControlledBySourceControllerPredicate ignored ->
                    sourceControllerId != null && sourceControllerId.equals(affectedControllerId);
            case PermanentIsSourceCardPredicate ignored ->
                    source != null && affected != null && source.getId().equals(affected.getId());
            case PermanentHasSubtypePredicate has ->
                    affected != null && hasEffectiveSubtype(gameData, affected, has.subtype());
            case PermanentHasAnySubtypePredicate any -> affected != null
                    && any.subtypes().stream().anyMatch(subtype -> hasEffectiveSubtype(gameData, affected, subtype));
            case PermanentAllOfPredicate all -> all.predicates().stream().allMatch(part -> placementAffects(
                    gameData, part, source, sourceControllerId, affected, affectedControllerId, creature, artifact));
            case PermanentAnyOfPredicate any -> any.predicates().stream().anyMatch(part -> placementAffects(
                    gameData, part, source, sourceControllerId, affected, affectedControllerId, creature, artifact));
            case PermanentNotPredicate not -> !placementAffects(gameData, not.predicate(), source, sourceControllerId,
                    affected, affectedControllerId, creature, artifact);
            default -> affected != null && predicateEvaluationService.matchesPermanentPredicate(gameData, affected,
                    predicate);
        };
    }

    /**
     * The counter-placement replacements of other players' permanents that apply to a placement on a permanent
     * {@code controllerId} controls, put by that player (an opponent's "if an opponent would put"), in turn order.
     */
    private int applyOtherPlayersPlacementReplacements(GameData gameData, UUID controllerId, Permanent affected,
                                                       CounterType counterType, int count, boolean creature,
                                                       boolean artifact) {
        if (count <= 0) {
            return count;
        }
        final int[] result = {count};
        gameData.forEachBattlefield((sourceControllerId, battlefield) -> {
            if (Objects.equals(sourceControllerId, controllerId)) {
                return;
            }
            for (Permanent source : battlefield) {
                for (CardEffect effect : staticEffectsIncludingTemporary(gameData, source, sourceControllerId)) {
                    if (effect instanceof AddOnePlusOneCountersEffect placement
                            && placementReplacementApplies(gameData, placement, source, sourceControllerId, affected,
                            controllerId, null, counterType, creature, artifact)) {
                        result[0] = MaroGoneNutsSupport.apply(gameData, effect,
                                placement.replace(counterType, result[0]));
                    }
                }
            }
        });
        return result[0];
    }

    /**
     * The counter-placement replacements the affected permanent's controller was granted until end of turn (Prairie
     * Dog): that player is their "you", and they outlive the permanent that granted them.
     */
    private List<CardEffect> playerGrantedPlusOneReplacements(
            GameData gameData, UUID affectedControllerId, UUID placingPlayerId, CounterType counterType,
            Permanent affected, boolean creature, boolean artifact) {
        if (affectedControllerId == null) {
            return List.of();
        }
        List<CardEffect> applicable = new ArrayList<>();
        for (CardEffect effect : gameData.floatingPlayerEffects(affectedControllerId,
                AddOnePlusOneCountersEffect.class)) {
            if (effect instanceof AddOnePlusOneCountersEffect placement
                    && placementReplacementApplies(gameData, placement, null, affectedControllerId, affected,
                    affectedControllerId, placingPlayerId, counterType, creature, artifact)) {
                applicable.add(effect);
            }
        }
        return applicable;
    }

    private int applyGlobalCounterReplacements(GameData gameData, CounterType counterType, int count,
                                               boolean affectedPermanentIsCreature,
                                               boolean affectedPermanentIsArtifact) {
        if (count <= 0) {
            return count;
        }
        final int[] result = {count};
        gameData.forEachBattlefield((sourceControllerId, battlefield) -> {
            for (Permanent source : battlefield) {
                for (CardEffect effect : staticEffectsIncludingTemporary(
                        gameData, source, sourceControllerId)) {
                    if (effect instanceof CounterReplacementEffect replacement
                            && replacement.appliesToAllPermanents()
                            && replacement.appliesTo(counterType, affectedPermanentIsCreature,
                            affectedPermanentIsArtifact)) {
                        result[0] = MaroGoneNutsSupport.apply(
                                gameData, effect, replacement.replace(counterType, result[0]));
                    }
                }
            }
        });
        return result[0];
    }

    private int applyEnteringPermanentReplacements(GameData gameData, UUID controllerId, UUID placingPlayerId,
                                                   CounterType counterType, int count,
                                                   Permanent enteringPermanent) {
        if (count <= 0 || enteringPermanent == null) {
            return count;
        }
        int result = count;
        for (CardEffect effect : ownEnteringPlacementReplacements(gameData, enteringPermanent, controllerId,
                placingPlayerId, counterType)) {
            result = MaroGoneNutsSupport.apply(gameData, effect,
                    ((CounterReplacementEffect) effect).replace(counterType, result));
        }
        return result;
    }

    /**
     * The entering permanent's own counter-placement replacements that apply to the counters it enters with: only one
     * that affects nothing but its source (Mowu's ruling; Winding Constrictor's and Conclave Mentor's rulings: a
     * "permanent you control" replacement doesn't apply to itself entering).
     */
    private List<CardEffect> ownEnteringPlacementReplacements(GameData gameData, Permanent enteringPermanent,
                                                              UUID controllerId, UUID placingPlayerId,
                                                              CounterType counterType) {
        boolean creature = enteringPermanent.getCard().hasType(CardType.CREATURE);
        boolean artifact = enteringPermanent.getCard().hasType(CardType.ARTIFACT);
        List<CardEffect> own = new ArrayList<>();
        for (CardEffect effect : enteringPermanent.getCard().getStaticEffects()) {
            if (effect instanceof AddOnePlusOneCountersEffect placement && placement.affectsOnlyItsSource()
                    && placementReplacementApplies(gameData, placement, enteringPermanent, controllerId,
                    enteringPermanent, controllerId, placingPlayerId, counterType, creature, artifact)) {
                own.add(effect);
            }
        }
        return own;
    }

    private int replacePlusOnePlusOneCounters(
            GameData gameData, UUID controllerId, int count, boolean affectedPermanentIsNonCreatureVehicle) {
        return replacePlusOnePlusOneCounters(gameData, controllerId, count,
                affectedPermanentIsNonCreatureVehicle, null);
    }

    private int replacePlusOnePlusOneCounters(
            GameData gameData, UUID controllerId, int count, boolean affectedPermanentIsNonCreatureVehicle,
            Permanent affectedPermanent) {
        return replacePlusOnePlusOneCounters(gameData, controllerId, count,
                affectedPermanentIsNonCreatureVehicle, affectedPermanent, false);
    }

    private int replacePlusOnePlusOneCounters(
            GameData gameData, UUID controllerId, int count, boolean affectedPermanentIsNonCreatureVehicle,
            Permanent affectedPermanent, boolean affectedPermanentIsEntering) {
        // A noncreature Vehicle is an artifact: the replacements that name artifacts or Vehicles apply to it.
        return replaceCounters(gameData, controllerId, CounterType.PLUS_ONE_PLUS_ONE, count,
                !affectedPermanentIsNonCreatureVehicle, affectedPermanentIsNonCreatureVehicle, affectedPermanent,
                affectedPermanentIsEntering);
    }

    private List<CardEffect> staticEffectsIncludingTemporary(
            GameData gameData, Permanent permanent, UUID controllerId) {
        if (permanent.isFaceDown()) {
            return List.of();
        }
        List<CardEffect> declaredEffects = new ArrayList<>(
                LayerSystemService.currentCard(gameData, permanent).getStaticEffects());
        if (declaredEffects.isEmpty() || hasLostPrintedAbilities(gameData, permanent)) {
            return List.of();
        }
        List<CardEffect> activeEffects = new ArrayList<>();
        for (CardEffect effect : declaredEffects) {
            collectActiveStaticEffects(gameData, permanent, controllerId, effect, activeEffects);
        }
        return activeEffects;
    }

    /** Returns the active printed and temporary static effects carried by a battlefield permanent. */
    public List<CardEffect> getActiveStaticEffects(GameData gameData, Permanent permanent) {
        return staticEffectsIncludingTemporary(gameData, permanent,
                findPermanentController(gameData, permanent.getId()));
    }

    /**
     * As {@link #getActiveStaticEffects(GameData, Permanent)}, for a caller that already knows the permanent's
     * controller ({@code controllerId}, the player whose battlefield holds it): a scan over every battlefield
     * passes the battlefield's player instead of having each permanent's controller searched for again. Inside a
     * query scope each permanent is judged once per pass ({@link LayerSystemService.Pass#activeStaticEffects}).
     */
    public List<CardEffect> getActiveStaticEffects(GameData gameData, Permanent permanent, UUID controllerId) {
        LayerSystemService.Pass pass = layerSystemService.activePass(gameData);
        if (pass == null) {
            return staticEffectsIncludingTemporary(gameData, permanent, controllerId);
        }
        return pass.activeStaticEffects(permanent, controllerId,
                () -> staticEffectsIncludingTemporary(gameData, permanent, controllerId));
    }

    /**
     * An active static effect of a battlefield permanent: the effect as {@link #getActiveStaticEffects} gives it (a
     * conditional one unwrapped once its condition holds), the permanent that has it, and that permanent's controller.
     */
    public record ActiveStaticEffect<T extends CardEffect>(T effect, Permanent source, UUID controllerId) {
    }

    /**
     * Every active static effect of {@code type} on the battlefield, with its permanent and that permanent's
     * controller, in turn order and then each battlefield's order. The one walk the board scans share: the controller
     * comes from the battlefield being walked, never searched for, and each permanent's static abilities are judged
     * once per pass. The walk only reads, so it runs in one query scope of its own when its caller has none: one board
     * fingerprint for the walk instead of one per permanent judged.
     */
    public <T extends CardEffect> List<ActiveStaticEffect<T>> activeStaticEffects(GameData gameData, Class<T> type) {
        return withQueryScope(gameData, () -> {
            List<ActiveStaticEffect<T>> found = new ArrayList<>();
            for (UUID playerId : gameData.orderedPlayerIds) {
                collectBattlefieldStaticEffects(gameData, playerId, type, found);
            }
            return found;
        });
    }

    /** As {@link #activeStaticEffects(GameData, Class)}, for the permanents {@code controllerId} controls. */
    public <T extends CardEffect> List<ActiveStaticEffect<T>> activeStaticEffects(GameData gameData, UUID controllerId,
                                                                                Class<T> type) {
        return withQueryScope(gameData, () -> {
            List<ActiveStaticEffect<T>> found = new ArrayList<>();
            collectBattlefieldStaticEffects(gameData, controllerId, type, found);
            return found;
        });
    }

    private <T extends CardEffect> void collectBattlefieldStaticEffects(GameData gameData, UUID controllerId,
                                                                       Class<T> type, List<ActiveStaticEffect<T>> found) {
        List<Permanent> battlefield = controllerId == null ? null : gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : getActiveStaticEffects(gameData, permanent, controllerId)) {
                if (type.isInstance(effect)) {
                    found.add(new ActiveStaticEffect<>(type.cast(effect), permanent, controllerId));
                }
            }
        }
    }

    private void collectActiveStaticEffects(GameData gameData, Permanent source, UUID controllerId,
                                            CardEffect effect, List<CardEffect> activeEffects) {
        if (effect instanceof ConditionalEffect conditional) {
            if (conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStaticEffect(source, controllerId))) {
                collectActiveStaticEffects(gameData, source, controllerId,
                        conditional.wrapped(), activeEffects);
            }
            return;
        }
        if (effect instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                collectActiveStaticEffects(gameData, source, controllerId, step, activeEffects);
            }
            return;
        }
        activeEffects.add(effect);
    }

    /** Applies all counter replacements for a permanent already on the battlefield. */
    public int replaceCounters(GameData gameData, Permanent permanent, CounterType counterType, int count) {
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        boolean creature = isCreature(gameData, permanent);
        if (counterType == CounterType.PLUS_ONE_PLUS_ONE && !creature
                && hasEffectiveSubtype(gameData, permanent, CardSubtype.VEHICLE)) {
            return replacePlusOnePlusOneCounters(gameData, controllerId, count, true, permanent);
        }
        boolean artifact = isArtifact(gameData, permanent);
        return replaceCounters(gameData, controllerId, counterType, count, creature, artifact,
                permanent, false);
    }

    public int replaceCounters(GameData gameData, Permanent permanent, CounterType counterType,
                               int count, UUID placingPlayerId) {
        return replaceCounters(gameData, permanent, counterType, count, placingPlayerId, false);
    }

    public int replaceCounters(GameData gameData, Permanent permanent, CounterType counterType,
                               int count, UUID placingPlayerId, boolean modularAbility) {
        int result = count;
        for (var modifier : counterReplacementsFor(gameData, permanent, counterType, placingPlayerId, modularAbility)) {
            result = MaroGoneNutsSupport.apply(gameData, modifier.effect(),
                    ((CounterReplacementEffect) modifier.effect()).replace(counterType, result));
        }
        return limitCounters(gameData, permanent, findPermanentController(gameData, permanent.getId()),
                counterType, result);
    }

    /** The individually applicable replacements for an impending placement on a live permanent. */
    public List<com.github.laxika.magicalvibes.model.ChoiceContext.CounterReplacement> counterReplacementsFor(
            GameData gameData, Permanent permanent, CounterType counterType,
            UUID placingPlayerId, boolean modularAbility) {
        return withQueryScope(gameData, () -> counterReplacementsForInScope(
                gameData, permanent, counterType, placingPlayerId, modularAbility));
    }

    private List<com.github.laxika.magicalvibes.model.ChoiceContext.CounterReplacement> counterReplacementsForInScope(
            GameData gameData, Permanent permanent, CounterType counterType,
            UUID placingPlayerId, boolean modularAbility) {
        UUID affectedControllerId = findPermanentController(gameData, permanent.getId());
        boolean creature = isCreature(gameData, permanent);
        boolean artifact = isArtifact(gameData, permanent);
        return List.copyOf(collectCounterReplacements(gameData, permanent, affectedControllerId, creature,
                artifact, counterType, placingPlayerId, modularAbility));
    }

    /**
     * The individually applicable replacements for counters {@code permanent} enters with under
     * {@code controllerId}, including its own replacements for counters it enters with.
     */
    public List<com.github.laxika.magicalvibes.model.ChoiceContext.CounterReplacement> enteringCounterReplacementsFor(
            GameData gameData, Permanent permanent, UUID controllerId, CounterType counterType,
            UUID placingPlayerId, boolean modularAbility) {
        boolean creature = permanent.getCard().hasType(CardType.CREATURE);
        boolean artifact = permanent.getCard().hasType(CardType.ARTIFACT);
        List<com.github.laxika.magicalvibes.model.ChoiceContext.CounterReplacement> modifiers =
                withQueryScope(gameData, () -> collectCounterReplacements(gameData, permanent, controllerId,
                        creature, artifact, counterType, placingPlayerId, modularAbility));
        for (CardEffect effect : ownEnteringPlacementReplacements(gameData, permanent, controllerId,
                placingPlayerId, counterType)) {
            modifiers.add(new com.github.laxika.magicalvibes.model.ChoiceContext.CounterReplacement(
                    FaceDownVisibility.publicName(gameData, permanent) + " (" + (modifiers.size() + 1) + ")", effect));
        }
        return List.copyOf(modifiers);
    }

    /** Whether applying {@code replacements} to {@code count} counters in different orders gives different totals. */
    public boolean counterReplacementOrderMatters(
            GameData gameData, CounterType counterType, int count,
            List<com.github.laxika.magicalvibes.model.ChoiceContext.CounterReplacement> replacements) {
        for (int i = 0; i < replacements.size(); i++) {
            for (int j = i + 1; j < replacements.size(); j++) {
                var first = replacements.get(i);
                var second = replacements.get(j);
                if (applyCounterReplacement(gameData, counterType,
                        applyCounterReplacement(gameData, counterType, count, first), second)
                        != applyCounterReplacement(gameData, counterType,
                        applyCounterReplacement(gameData, counterType, count, second), first)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Applies one counter replacement to a placement of {@code count} counters. */
    public int applyCounterReplacement(GameData gameData, CounterType counterType, int count,
                                       com.github.laxika.magicalvibes.model.ChoiceContext.CounterReplacement replacement) {
        return MaroGoneNutsSupport.apply(gameData, replacement.effect(),
                ((CounterReplacementEffect) replacement.effect()).replace(counterType, count));
    }

    private List<com.github.laxika.magicalvibes.model.ChoiceContext.CounterReplacement> collectCounterReplacements(
            GameData gameData, Permanent permanent, UUID affectedControllerId, boolean creature,
            boolean artifact, CounterType counterType,
            UUID placingPlayerId, boolean modularAbility) {
        List<com.github.laxika.magicalvibes.model.ChoiceContext.CounterReplacement> modifiers = new ArrayList<>();
        gameData.forEachBattlefield((sourceControllerId, battlefield) -> {
            boolean sourceControlsAffected = Objects.equals(sourceControllerId, affectedControllerId);
            boolean sourceControllerIsPlacing = Objects.equals(sourceControllerId, placingPlayerId);
            for (Permanent source : battlefield) {
                for (CardEffect effect : staticEffectsIncludingTemporary(
                        gameData, source, sourceControllerId)) {
                    if (!(effect instanceof CounterReplacementEffect replacement)) continue;
                    if (replacement.appliesOnlyToModularAbility() && !modularAbility) continue;
                    boolean applies;
                    if (replacement instanceof AddOnePlusOneCountersEffect placement) {
                        // Spacecraft, Planet and Vehicle are read from the layered subtypes, so a Planet turned
                        // into a Forest no longer counts.
                        applies = placementReplacementApplies(gameData, placement, source, sourceControllerId,
                                permanent, affectedControllerId, placingPlayerId, counterType, creature, artifact);
                    } else if (replacement.appliesToAllPermanents()) {
                        applies = replacement.appliesTo(counterType, creature, artifact);
                    } else if (replacement.appliesGlobally()) {
                        applies = replacement.appliesTo(counterType, creature, sourceControlsAffected,
                                sourceControllerIsPlacing, false);
                    } else {
                        applies = sourceControlsAffected && replacement.appliesTo(
                                counterType, creature, artifact, source, permanent);
                    }
                    if (applies) {
                        modifiers.add(new com.github.laxika.magicalvibes.model.ChoiceContext.CounterReplacement(
                                FaceDownVisibility.publicName(gameData, source) + " (" + (modifiers.size() + 1) + ")", effect));
                    }
                }
            }
        });
        for (CardEffect effect : playerGrantedPlusOneReplacements(
                gameData, affectedControllerId, placingPlayerId, counterType, permanent, creature, artifact)) {
            modifiers.add(new com.github.laxika.magicalvibes.model.ChoiceContext.CounterReplacement(
                    "Until end of turn (" + (modifiers.size() + 1) + ")", effect));
        }
        if (gameData.planechase != null && affectedControllerId != null) {
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof CounterReplacementEffect replacement
                            && planarPlacementReplacementApplies(gameData, replacement, permanent,
                            affectedControllerId, placingPlayerId, counterType, creature, artifact)) {
                        modifiers.add(new com.github.laxika.magicalvibes.model.ChoiceContext.CounterReplacement(
                                planar.getCard().getName() + " (" + (modifiers.size() + 1) + ")", effect));
                    }
                }
            }
        }
        return modifiers;
    }

    /** Applies proliferate replacement effects controlled by {@code controllerId}. */
    public int replaceProliferateCount(GameData gameData, UUID controllerId, int count) {
        if (count <= 0 || controllerId == null) {
            return count;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return count;
        }
        int result = count;
        for (Permanent permanent : battlefield) {
            if (hasLostAllAbilities(gameData, permanent)) {
                continue;
            }
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                if (effect instanceof ProliferateReplacementEffect replacement) {
                    result = MaroGoneNutsSupport.apply(
                            gameData, effect, replacement.replace(result));
                }
            }
        }
        return result;
    }

    private int applyPlanarCounterReplacements(GameData gameData, UUID affectedControllerId,
                                               CounterType counterType, int count,
                                               Permanent affectedPermanent,
                                               boolean affectedPermanentIsCreature,
                                               boolean affectedPermanentIsArtifact) {
        if (count <= 0 || gameData.planechase == null || affectedControllerId == null) {
            return count;
        }
        int result = count;
        for (var planar : gameData.planechase.faceUp) {
            for (CardEffect effect : planar.getCard().getStaticEffects()) {
                if (effect instanceof CounterReplacementEffect replacement
                        && planarPlacementReplacementApplies(gameData, replacement, affectedPermanent,
                        affectedControllerId, null, counterType, affectedPermanentIsCreature,
                        affectedPermanentIsArtifact)) {
                    result = MaroGoneNutsSupport.apply(
                            gameData, effect, replacement.replace(counterType, result));
                }
            }
        }
        return result;
    }

    /**
     * Whether a face-up plane's counter replacement applies (Selesnya Loft Gardens): a plane has no permanent, and its
     * controller is the planar controller.
     */
    private boolean planarPlacementReplacementApplies(GameData gameData, CounterReplacementEffect replacement,
                                                      Permanent affected, UUID affectedControllerId,
                                                      UUID placingPlayerId, CounterType counterType,
                                                      boolean creature, boolean artifact) {
        UUID planarControllerId = gameData.planechase.controllerId;
        if (replacement instanceof AddOnePlusOneCountersEffect placement) {
            return placementReplacementApplies(gameData, placement, null, planarControllerId, affected,
                    affectedControllerId, placingPlayerId, counterType, creature, artifact);
        }
        return replacement.appliesTo(counterType, creature,
                Objects.equals(planarControllerId, affectedControllerId), false, false);
    }

    /** Applies all counter replacements for a permanent entering under {@code controllerId}. */
    public int replaceCounters(GameData gameData, Permanent permanent, UUID controllerId,
                               CounterType counterType, int count) {
        boolean creature = permanent != null && permanent.getCard().hasType(CardType.CREATURE);
        if (counterType == CounterType.PLUS_ONE_PLUS_ONE && permanent != null && !creature
                && permanent.getCard().getSubtypes().contains(CardSubtype.VEHICLE)) {
            return recordEntryCounterPlacement(gameData, permanent, controllerId, counterType, count,
                    replacePlusOnePlusOneCounters(gameData, controllerId, count, true, permanent, true),
                    controllerId, false);
        }
        boolean artifact = permanent != null && permanent.getCard().hasType(CardType.ARTIFACT);
        return recordEntryCounterPlacement(gameData, permanent, controllerId, counterType, count,
                replaceCounters(gameData, controllerId, counterType, count, creature, artifact,
                        permanent, true), controllerId, false);
    }

    public int replaceCounters(GameData gameData, Permanent permanent, UUID controllerId,
                               CounterType counterType, int count, UUID placingPlayerId) {
        return replaceCounters(gameData, permanent, controllerId, counterType, count,
                placingPlayerId, false);
    }

    public int replaceCounters(GameData gameData, Permanent permanent, UUID controllerId,
                               CounterType counterType, int count, UUID placingPlayerId,
                               boolean modularAbility) {
        boolean creature = permanent != null && permanent.getCard().hasType(CardType.CREATURE);
        boolean artifact = permanent != null && permanent.getCard().hasType(CardType.ARTIFACT);
        final int[] result = {count};
        gameData.forEachBattlefield((sourceControllerId, battlefield) -> {
            boolean sourceControlsAffected = Objects.equals(sourceControllerId, controllerId);
            boolean sourceControllerIsPlacing = Objects.equals(sourceControllerId, placingPlayerId);
            for (Permanent source : battlefield) {
                for (CardEffect effect : staticEffectsIncludingTemporary(
                        gameData, source, sourceControllerId)) {
                    if (!(effect instanceof CounterReplacementEffect replacement)) continue;
                    if (replacement.appliesOnlyToModularAbility() && !modularAbility) continue;
                    boolean applies;
                    if (replacement instanceof AddOnePlusOneCountersEffect placement) {
                        applies = placementReplacementApplies(gameData, placement, source, sourceControllerId,
                                permanent, controllerId, placingPlayerId, counterType, creature, artifact);
                    } else if (replacement.appliesGlobally()) {
                        applies = replacement.appliesTo(counterType, creature, sourceControlsAffected,
                                sourceControllerIsPlacing, false);
                    } else {
                        applies = sourceControlsAffected && replacement.appliesTo(
                                counterType, creature, artifact, source, permanent);
                    }
                    if (applies) {
                        result[0] = MaroGoneNutsSupport.apply(
                                gameData, effect, replacement.replace(counterType, result[0]));
                    }
                }
            }
        });
        for (CardEffect effect : playerGrantedPlusOneReplacements(
                gameData, controllerId, placingPlayerId, counterType, permanent, creature, artifact)) {
            result[0] = MaroGoneNutsSupport.apply(
                    gameData, effect, ((CounterReplacementEffect) effect).replace(counterType, result[0]));
        }
        result[0] = applyPlanarCounterReplacements(gameData, controllerId, counterType,
                result[0], permanent, creature, artifact);
        result[0] = applyEnteringPermanentReplacements(gameData, controllerId, placingPlayerId, counterType,
                result[0], permanent);
        return recordEntryCounterPlacement(gameData, permanent, controllerId, counterType, count,
                limitCounters(gameData, permanent, controllerId, counterType, result[0]),
                placingPlayerId, modularAbility);
    }

    /**
     * Notes on the entering {@code permanent} a placement whose counter replacements were applied in
     * battlefield order although their order changes the result, so its controller can choose the
     * order before it enters (CR 616.1). Returns {@code placed} unchanged.
     */
    private int recordEntryCounterPlacement(GameData gameData, Permanent permanent, UUID controllerId,
                                            CounterType counterType, int count, int placed,
                                            UUID placingPlayerId, boolean modularAbility) {
        if (permanent == null || controllerId == null || count <= 0
                || findPermanentById(gameData, permanent.getId()) != null) {
            return placed;
        }
        var replacements = enteringCounterReplacementsFor(gameData, permanent, controllerId, counterType,
                placingPlayerId, modularAbility);
        if (counterReplacementOrderMatters(gameData, counterType, count, replacements)) {
            permanent.getPendingEntryCounterPlacements().add(
                    new com.github.laxika.magicalvibes.model.ChoiceContext.EntryCounterPlacement(
                            counterType, count, placed, replacements));
        }
        return placed;
    }

    /** Clamps a counter placement to any active static maximum on the affected permanent. */
    public int limitCounters(GameData gameData, Permanent permanent, UUID controllerId,
                             CounterType counterType, int count) {
        if (permanent == null || count <= 0) {
            return count;
        }
        int maximum = Integer.MAX_VALUE;
        for (CardEffect effect : staticEffectsIncludingTemporary(
                gameData, permanent, controllerId)) {
            if (effect instanceof CounterLimitEffect limit
                    && limit.counterType() == counterType
                    && !gameData.removesOwnStaticAbility(permanent.getId(), effect.getClass())) {
                maximum = Math.min(maximum, limit.maximum());
            }
        }
        if (maximum == Integer.MAX_VALUE) {
            return count;
        }
        return Math.min(count, Math.max(0, maximum - permanent.getCounterCount(counterType)));
    }

    /** Applies all poison-counter replacements for a player. */
    public int replacePoisonCounters(GameData gameData, UUID playerId, int count) {
        return replacePlayerCounters(gameData, playerId, count);
    }

    public int replacePoisonCounters(GameData gameData, UUID playerId, int count,
                                     UUID placingPlayerId) {
        return replacePlayerCountersPlacedBy(gameData, playerId, count, placingPlayerId);
    }

    /** Applies experience-counter replacements (e.g. Winding Constrictor) for a player getting counters. */
    public int replaceExperienceCounters(GameData gameData, UUID playerId, int count) {
        return replacePlayerCounters(gameData, playerId, count);
    }

    /** Applies rad-counter replacements for a player receiving counters placed by {@code placingPlayerId}. */
    public int replaceRadCounters(GameData gameData, UUID playerId, int count, UUID placingPlayerId) {
        return replacePlayerCountersPlacedBy(gameData, playerId, count, placingPlayerId);
    }

    private int replacePlayerCountersPlacedBy(GameData gameData, UUID playerId, int count,
                                              UUID placingPlayerId) {
        int result = replacePlayerCounters(gameData, playerId, count);
        if (result <= 0 || placingPlayerId == null) {
            return result;
        }
        final int[] replaced = {result};
        gameData.forEachBattlefield((sourceControllerId, battlefield) -> {
            for (Permanent source : battlefield) {
                for (CardEffect effect : staticEffectsIncludingTemporary(
                        gameData, source, sourceControllerId)) {
                    // The ones that don't care who puts them were applied with the player's own.
                    if (effect instanceof AddOnePlusOneCountersEffect placement
                            && placement.placedBy() != PlayerRelation.ANY
                            && playerPlacementReplacementApplies(placement, sourceControllerId, playerId,
                            placingPlayerId)) {
                        replaced[0] = MaroGoneNutsSupport.apply(
                                gameData, effect, placement.replace(null, replaced[0]));
                    }
                }
            }
        });
        return replaced[0];
    }

    /**
     * Returns {@code true} if players cannot get counters at all,
     * because a permanent on the battlefield has {@link CountersCantBePlacedEffect}.
     */
    public boolean playersCantGetCounters(GameData gameData) {
        return anyBattlefieldHasStaticEffect(gameData, CountersCantBePlacedEffect.class);
    }

    /** Applies all energy-counter replacements for a player. */
    public int replaceEnergyCounters(GameData gameData, UUID playerId, int count) {
        if (count <= 0 || playerId == null) {
            return count;
        }
        if (playersCantGetCounters(gameData)) {
            return 0;
        }
        if (playerCountersLocked(gameData, playerId)) {
            return 0;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return count;
        }
        int result = count;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : staticEffectsIncludingTemporary(
                    gameData, permanent, playerId)) {
                if (effect instanceof EnergyCounterReplacementEffect replacement) {
                    result = MaroGoneNutsSupport.apply(
                            gameData, effect, replacement.replaceEnergy(result));
                } else if (effect instanceof AddOnePlusOneCountersEffect placement
                        && playerPlacementReplacementApplies(placement, playerId, playerId, playerId)) {
                    // A player getting energy puts the counters on themself, so they are the placing player
                    // (Winding Constrictor, level 3 of Innkeeper's Talent, Vorinclex).
                    result = MaroGoneNutsSupport.apply(
                            gameData, effect, placement.replace(null, result));
                }
            }
        }
        final int[] halved = {result};
        gameData.forEachBattlefield((sourceControllerId, sourceBattlefield) -> {
            if (Objects.equals(sourceControllerId, playerId)) {
                return;
            }
            for (Permanent source : sourceBattlefield) {
                for (CardEffect effect : staticEffectsIncludingTemporary(gameData, source, sourceControllerId)) {
                    if (effect instanceof AddOnePlusOneCountersEffect placement
                            && playerPlacementReplacementApplies(placement, sourceControllerId, playerId, playerId)) {
                        halved[0] = MaroGoneNutsSupport.apply(gameData, effect, placement.replace(null, halved[0]));
                    }
                }
            }
        });
        return halved[0];
    }

    private int replacePlayerCounters(GameData gameData, UUID playerId, int count) {
        if (count <= 0 || playerId == null) {
            return count;
        }
        if (playerCantGetCounters(gameData, playerId)) {
            return 0;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return count;
        }
        int result = count;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : staticEffectsIncludingTemporary(
                    gameData, permanent, playerId)) {
                // Placer unknown: only the ones that don't care who puts them (Winding Constrictor).
                if (effect instanceof AddOnePlusOneCountersEffect placement
                        && playerPlacementReplacementApplies(placement, playerId, playerId, null)) {
                    result = MaroGoneNutsSupport.apply(
                            gameData, effect, placement.replace(null, result));
                }
            }
        }
        return result;
    }

    /**
     * Whether a counter-placement replacement controlled by {@code sourceControllerId} applies to the counters
     * {@code affectedPlayerId} would get from {@code placingPlayerId} ({@code null}: not known). A player's counters
     * have no kind here, so only a replacement of every kind applies.
     */
    private static boolean playerPlacementReplacementApplies(AddOnePlusOneCountersEffect placement,
                                                             UUID sourceControllerId, UUID affectedPlayerId,
                                                             UUID placingPlayerId) {
        return placement.counterType() == CounterType.ANY
                && placement.replacesPlayer(sourceControllerId, affectedPlayerId)
                && placement.acceptsPlacer(sourceControllerId, placingPlayerId);
    }

    /**
     * Compatibility entry point used by existing +1/+1 counter placement paths.
     */
    public int doublePlusOnePlusOneCounters(GameData gameData, UUID controllerId, int count) {
        return replacePlusOnePlusOneCounters(gameData, controllerId, count);
    }

    /**
     * Convenience overload of {@link #replacePlusOnePlusOneCounters(GameData, UUID, int)} that
     * resolves the affected permanent's controller from the battlefield.
     */
    public int doublePlusOnePlusOneCounters(GameData gameData, Permanent permanent, int count) {
        if (permanent == null) {
            return count;
        }
        boolean affectedPermanentIsCreature = isCreature(gameData, permanent);
        boolean affectedPermanentIsNonCreatureVehicle = !affectedPermanentIsCreature
                && hasEffectiveSubtype(gameData, permanent, CardSubtype.VEHICLE);
        if (!affectedPermanentIsCreature && !affectedPermanentIsNonCreatureVehicle) {
            return count;
        }
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        if (cantHavePlusOnePlusOneCountersForController(gameData, permanent, controllerId)) {
            return 0;
        }
        return replacePlusOnePlusOneCounters(
                gameData, controllerId, count, affectedPermanentIsNonCreatureVehicle, permanent, false);
    }

    /**
     * Controller-aware permanent overload for +1/+1 counter placement during battlefield entry.
     */
    public int doublePlusOnePlusOneCounters(
            GameData gameData, Permanent permanent, UUID controllerId, int count) {
        if (permanent == null) {
            return count;
        }
        boolean affectedPermanentIsCreature = isCreature(gameData, permanent);
        boolean affectedPermanentIsNonCreatureVehicle = !affectedPermanentIsCreature
                && hasEffectiveSubtype(gameData, permanent, CardSubtype.VEHICLE);
        if (cantHavePlusOnePlusOneCountersForController(gameData, permanent, controllerId)) {
            return 0;
        }
        if (!affectedPermanentIsCreature && !affectedPermanentIsNonCreatureVehicle) {
            return recordEntryCounterPlacement(gameData, permanent, controllerId, CounterType.PLUS_ONE_PLUS_ONE,
                    count, replaceCounters(gameData, controllerId, CounterType.PLUS_ONE_PLUS_ONE, count,
                            false, isArtifact(gameData, permanent), permanent, true), controllerId, false);
        }
        return recordEntryCounterPlacement(gameData, permanent, controllerId, CounterType.PLUS_ONE_PLUS_ONE,
                count, replacePlusOnePlusOneCounters(gameData, controllerId, count,
                        affectedPermanentIsNonCreatureVehicle, permanent, true), controllerId, false);
    }

    /**
     * Returns {@code true} if the player cannot get poison counters,
     * because they control a permanent with {@link PlayerCantGetPoisonCountersEffect}.
     */
    public boolean canPlayerGetPoisonCounters(GameData gameData, UUID playerId) {
        if (playersCantGetCounters(gameData)) {
            return false;
        }
        if (playerCountersLocked(gameData, playerId)) {
            return false;
        }
        return activeStaticEffects(gameData, playerId, PlayerCantGetPoisonCountersEffect.class).isEmpty();
    }

    /**
     * Applies poison-counter replacement effects for one poison event and returns the number of
     * counters that should actually be added.
     */
    public int applyPoisonCounterReplacement(GameData gameData, UUID playerId, int amount) {
        if (amount <= 0 || !canPlayerGetPoisonCounters(gameData, playerId)) {
            return 0;
        }

        boolean hasMeliraReplacement =
                !activeStaticEffects(gameData, playerId, MeliraPoisonReplacementEffect.class).isEmpty();
        if (!hasMeliraReplacement) {
            // The "can't get additional poison counters this turn" restriction outlives its source.
            return gameData.playersAffectedByMeliraPoisonReplacementThisTurn.contains(playerId) ? 0 : amount;
        }
        return gameData.playersAffectedByMeliraPoisonReplacementThisTurn.add(playerId) ? 1 : 0;
    }

    /**
     * Returns {@code true} if the creature cannot be blocked. Checks the permanent's
     * resolved "can't be blocked this turn" effects, its own static effects, attached auras/equipment, and granted effects.
     */
    public boolean hasCantBeBlocked(GameData gameData, Permanent creature) {
        if (gameData.hasFloatingEffectOnPermanent(creature.getId(), CantBeBlockedEffect.class)) return true;
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (floating.effect() instanceof MakeAllCreaturesUnblockableEffect restriction
                        && isCreature(gameData, creature)
                        && (!restriction.controllerOnly()
                        || floating.controllerId().equals(findPermanentController(gameData, creature.getId())))
                        && (restriction.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                        gameData, creature, restriction.filter()))) {
                    return true;
                }
            }
        }
        if (!computeStaticBonus(gameData, creature).losesAllAbilities()) {
            if (!creature.isFaceDown() && creature.getCard().getStaticEffects().stream()
                    .anyMatch(e -> e instanceof BlockabilityRestrictionEffect r && r.cantBeBlocked())) return true;
        }
        if (computeStaticBonus(gameData, creature).grantedEffects().stream()
                .anyMatch(e -> e instanceof BlockabilityRestrictionEffect r && r.cantBeBlocked())) return true;
        if (hasAuraWithEffect(gameData, creature, CantBeBlockedEffect.class)) return true;
        if (hasGrantedEffect(gameData, creature, CantBeBlockedEffect.class)) return true;
        return controllerMakesMatchingCreaturesUnblockable(gameData, creature);
    }

    /**
     * Whether {@code permanent} has cumulative upkeep (CR 702.24a), for "that doesn't have cumulative upkeep": its
     * card's, or granted to it as the layers leave it.
     */
    public boolean hasCumulativeUpkeep(GameData gameData, Permanent permanent) {
        Trigger yourUpkeep = com.github.laxika.magicalvibes.model.trigger.Triggers.upkeep(
                com.github.laxika.magicalvibes.model.trigger.ActivePlayer.YOU);
        if (permanent.getCard().getTriggerEffects(yourUpkeep, Zone.BATTLEFIELD).stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect.class::isInstance)) {
            return true;
        }
        if (gameData == null) {
            return false;
        }
        for (CardEffect granted : computeStaticBonus(gameData, permanent).grantedEffects()) {
            if (granted instanceof com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect
                    || granted instanceof GrantTriggeredAbilityEffect grant
                    && grant.grantedAbility() instanceof TriggeredAbility triggered
                    && triggered.hasTrigger(yourUpkeep)
                    && com.github.laxika.magicalvibes.model.AbilityEffects.effectsOf(triggered).stream()
                    .anyMatch(com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect.class::isInstance)) {
                return true;
            }
        }
        return false;
    }

    /** Reconfigure permits a creature Equipment to attach through any attachment effect. */
    public boolean hasReconfigure(GameData gameData, Permanent permanent) {
        List<ActivatedAbility> abilities = new ArrayList<>(
                computeStaticBonus(gameData, permanent).grantedActivatedAbilities());
        if (!permanent.isFaceDown() && !hasLostPrintedAbilities(gameData, permanent)) {
            abilities.addAll(permanent.getCard().getActivatedAbilities(Zone.BATTLEFIELD));
        }
        return abilities.stream().flatMap(ability -> ability.getEffects().stream())
                .anyMatch(effect -> effect instanceof EquipEffect equip && equip.permitsCreatureEquipment());
    }

    /**
     * Third-party evasion: another permanent the creature's controller controls says "creatures you
     * control matching ~ can't be blocked" (Tetsuko Umezawa, Fugitive).
     *
     * <p>CR 613.11 â€” this modifies the rules of blocking rather than any object's characteristics,
     * so it is applied <em>after</em> every other continuous effect and the matching set is decided
     * from FULLY LAYERED characteristics: the predicate goes through the {@code GameData}-taking
     * {@code matchesPermanentPredicate}, which reads power and toughness through layer 7. A
     * Glorious Anthem lifting a 1/1 to 2/2 therefore takes the evasion away, and an opponent's
     * Cumber Stone dropping a 2/2 to 1/2 confers it (CR 509.1a checks restrictions at declare
     * blockers; the official ruling that a creature already blocked stays blocked is the
     * declaration-time snapshot, not a frozen set).
     *
     * <p>Deliberately NOT modeled as a layer-6 ability grant: the wording adds no ability to the
     * creatures, and a grant's scope filter is evaluated inside the layered pass, against numbers
     * layer 7 has not produced yet.
     */
    private boolean controllerMakesMatchingCreaturesUnblockable(GameData gameData, Permanent creature) {
        UUID controllerId = findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        for (Permanent source : battlefield) {
            if (hasLostAllAbilities(gameData, source)) continue;
            for (CardEffect effect : source.getCard().getStaticEffects()) {
                if (effect instanceof ControlledCreaturesMatchingCantBeBlockedEffect restriction
                        && predicateEvaluationService.matchesPermanentPredicate(
                                gameData, creature, restriction.filter())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * True if a creature is attacking the given player directly.
     */
    public boolean isPlayerBeingAttacked(GameData gameData, UUID playerId) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (!perm.isAttacking()) continue;
                UUID target = perm.getAttackTarget();
                if (target == null) continue;
                if (target.equals(playerId)) return true;
            }
        }
        return false;
    }

    /**
     * Returns whether {@code playerId} is a defending player: a creature is attacking them, or attacking a
     * planeswalker or battle they control (CR 506.2 — a defending player, the planeswalkers they
     * control and the battles they protect may be attacked).
     */
    public boolean isDefendingPlayer(GameData gameData, UUID playerId) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (!perm.isAttacking()) continue;
                UUID target = perm.getAttackTarget();
                if (target == null) continue;
                if (target.equals(playerId)) return true;
                if (!gameData.playerIds.contains(target)) {
                    Permanent attacked = findPermanentById(gameData, target);
                    if (attacked != null && playerId.equals(gameData.findControllerOf(attacked))) return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether a creature declared as an attacker is attacking {@code playerId}. A creature put
     * onto the battlefield attacking was never declared as an attacker, so it doesn't count (CR 508.4).
     */
    public boolean isPlayerBeingAttackedByDeclaredAttacker(GameData gameData, UUID playerId) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.isAttacking() && perm.isAttackedThisCombat() && !perm.isAttackTargetReselected()
                        && playerId.equals(perm.getAttackTarget())) {
                    return true;
                }
            }
        }
        return false;
    }

    // --- Stats calculation ---

    /**
     * Returns the permanent's effective power, including its base/modified power
     * plus any static bonuses from other permanents on the battlefield.
     */
    public int getEffectivePower(GameData gameData, Permanent permanent) {
        return getEffectivePower(permanent, computeStaticBonus(gameData, permanent));
    }

    /**
     * Returns the permanent's base power after layer 7a characteristic-defining abilities and
     * layer 7b base-setting effects, before counters, modifiers, and other static bonuses.
     */
    public int getEffectiveBasePower(GameData gameData, Permanent permanent) {
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        return bonus.basePTOverridden() ? bonus.basePowerOverride() : permanent.getBasePower();
    }

    /**
     * Returns the power used when {@code permanent} pays a crew or saddle cost for
     * {@code sourcePermanent}. Pilot-style bonuses affect only those costs, not the permanent's
     * ordinary effective power.
     */
    public int getEffectivePowerForCrewOrSaddle(GameData gameData, Permanent sourcePermanent,
                                                Permanent permanent) {
        return getEffectivePowerForCrewOrSaddle(gameData, sourcePermanent, permanent, true);
    }

    public int getEffectivePowerForCrewOrSaddle(GameData gameData, Permanent sourcePermanent,
                                                Permanent permanent, boolean crewing) {
        int power = getEffectivePower(gameData, permanent);
        if (sourcePermanent == null || !hasMountOrVehicleSubtype(gameData, sourcePermanent)) {
            return power;
        }
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (bonus.losesAllAbilities()) {
            return power;
        }
        CrewAndSaddlePowerModifierEffect.Action action = crewing
                ? CrewAndSaddlePowerModifierEffect.Action.CREW : CrewAndSaddlePowerModifierEffect.Action.SADDLE;
        boolean usesToughnessInsteadOfPower = false;
        int powerBonus = 0;
        for (CardEffect effect : permanent.getCard().getStaticEffects()) {
            if (effect instanceof CrewAndSaddlePowerModifierEffect modifier) {
                if (!modifier.appliesTo(action)) continue;
                usesToughnessInsteadOfPower |= modifier.usesToughnessInsteadOfPower();
                powerBonus += modifier.powerBonus();
            }
        }
        for (CardEffect effect : bonus.grantedEffects()) {
            if (effect instanceof CrewAndSaddlePowerModifierEffect modifier) {
                if (!modifier.appliesTo(action)) continue;
                usesToughnessInsteadOfPower |= modifier.usesToughnessInsteadOfPower();
                powerBonus += modifier.powerBonus();
            }
        }
        if (usesToughnessInsteadOfPower) {
            power = getEffectiveToughness(permanent, bonus);
        }
        return power + powerBonus;
    }

    private boolean hasMountOrVehicleSubtype(GameData gameData, Permanent permanent) {
        if (effectiveCreatureSubtypes(gameData, permanent).contains(CardSubtype.MOUNT)) {
            return true;
        }
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        Set<CardSubtype> subtypes = new HashSet<>();
        if (!bonus.subtypeOverriding()) {
            subtypes.addAll(permanent.getCard().getSubtypes());
        }
        subtypes.addAll(bonus.grantedSubtypes());
        return subtypes.contains(CardSubtype.VEHICLE);
    }

    /**
     * Returns the permanent's effective power using a pre-computed static bonus.
     * {@code bonus.basePTOverridden()} carries the CR 613 layer 7a/7b result (the object's CDA
     * overridden by the timestamp-resolved base-P/T setter winner from the layered pass) â€” it
     * takes precedence over every legacy {@code Permanent} base field; modifiers (7c) and the
     * bonus sum apply on top. {@code bonus.ptSwitched()} carries the layer-7d switch parity:
     * a switch swaps the values calculated through 7c (CR 613.4d), so a switched permanent's
     * power is the fully-resolved pre-switch toughness â€” 7b setters and 7c boosts resolving
     * after the switch still apply before the swap, because layers order them, not timestamps.
     */
    public int getEffectivePower(Permanent permanent, StaticBonus bonus) {
        return bonus.ptSwitched()
                ? preSwitchToughness(permanent, bonus)
                : preSwitchPower(permanent, bonus);
    }

    /**
     * Returns the permanent's effective toughness, including its base/modified toughness
     * plus any static bonuses from other permanents on the battlefield.
     */
    public int getEffectiveToughness(GameData gameData, Permanent permanent) {
        return getEffectiveToughness(permanent, computeStaticBonus(gameData, permanent));
    }

    /**
     * Returns the permanent's effective toughness using a pre-computed static bonus.
     * See {@link #getEffectivePower(Permanent, StaticBonus)} â€” the layered 7a/7b base wins
     * and the layer-7d switch parity swaps the finished values.
     */
    public int getEffectiveToughness(Permanent permanent, StaticBonus bonus) {
        return bonus.ptSwitched()
                ? preSwitchPower(permanent, bonus)
                : preSwitchToughness(permanent, bonus);
    }

    /** The permanent's power through layer 7c (base + modifiers + static bonuses), before 7d. */
    private int preSwitchPower(Permanent permanent, StaticBonus bonus) {
        if (bonus.basePTOverridden()) {
            return SaturatingMath.clamp((long) bonus.basePowerOverride() + permanent.getPowerModifiers() + bonus.power());
        }
        return SaturatingMath.add(permanent.getEffectivePower(), bonus.power());
    }

    /** The permanent's toughness through layer 7c (base + modifiers + static bonuses), before 7d. */
    private int preSwitchToughness(Permanent permanent, StaticBonus bonus) {
        if (bonus.basePTOverridden()) {
            return SaturatingMath.clamp((long) bonus.baseToughnessOverride() + permanent.getToughnessModifiers() + bonus.toughness());
        }
        return SaturatingMath.add(permanent.getEffectiveToughness(), bonus.toughness());
    }

    /** The base power and toughness the floating layer-7b setters on one permanent id leave it; a {@code null}
     *  component is one none of them sets. */
    private record BasePtOnId(Integer power, Integer toughness) {
    }

    /**
     * The base P/T the floating layer-7b setters on {@code permanentId} set, applied in timestamp order (CR 613.7),
     * or {@code null} when none is on it. Used for a permanent the layered board does not hold (a departed
     * snapshot), whose setters still count as its last-known characteristics.
     */
    private BasePtOnId floatingBasePt(GameData gameData, UUID permanentId) {
        List<FloatingContinuousEffect> setters = new ArrayList<>(
                gameData.floatingEffectsOnPermanent(permanentId, SetBasePowerToughnessEffect.class));
        if (setters.isEmpty()) {
            return null;
        }
        setters.sort(Comparator.comparingLong(FloatingContinuousEffect::timestamp));
        Integer power = null;
        Integer toughness = null;
        for (FloatingContinuousEffect setter : setters) {
            SetBasePowerToughnessEffect set = (SetBasePowerToughnessEffect) setter.effect();
            if (set.power() != null) {
                power = set.power();
            }
            if (set.toughness() != null) {
                toughness = set.toughness();
            }
        }
        return new BasePtOnId(power, toughness);
    }

    /**
     * {@code permanent}'s own power as it last existed, for a last-known read of a departed copy that recorded no
     * power (CR 608.2h): its intrinsic base or the newest floating base-P/T setter on its id, plus its counters and
     * perpetual modifier and the floating P/T boosts on its id. Effects of other objects' static abilities are not
     * included.
     */
    public int ownPower(GameData gameData, Permanent permanent) {
        BasePtOnId base = floatingBasePt(gameData, permanent.getId());
        int own = base != null && base.power() != null
                ? SaturatingMath.add(base.power(), permanent.getPowerModifiers())
                : permanent.getEffectivePower();
        return SaturatingMath.add(own, gameData.floatingPowerBoost(permanent.getId()));
    }

    /** The toughness counterpart of {@link #ownPower(GameData, Permanent)}. */
    public int ownToughness(GameData gameData, Permanent permanent) {
        BasePtOnId base = floatingBasePt(gameData, permanent.getId());
        int own = base != null && base.toughness() != null
                ? SaturatingMath.add(base.toughness(), permanent.getToughnessModifiers())
                : permanent.getEffectiveToughness();
        return SaturatingMath.add(own, gameData.floatingToughnessBoost(permanent.getId()));
    }

    /**
     * Power as seen by a static filter's power leaf, and the recursion-safe counterpart of
     * {@link #getEffectivePower(GameData, Permanent)}. Static filters are evaluated from inside
     * the layered pass, where the fully layered query is not always reachable: layer 7c lives in
     * {@link #assembleStaticBonusInternal}, so asking for a permanent whose assembly is already
     * running would re-enter it forever.
     *
     * <p>Three answers, by what is safely available:
     * <ul>
     * <li><b>Fully layered</b> (anthems and every other layer-7c boost included) once the pass's
     *     board is finished and this permanent is not itself being assembled.</li>
     * <li><b>Preliminary</b> when the permanent IS the one being assembled â€” the dominant shape,
     *     since a static effect filtered on P/T ("creatures you control with power 1 or less",
     *     Tetsuko Umezawa, Fugitive) asks about the very permanent whose bonus is being built.
     *     See {@link #preliminaryBonus}: a second assembly answers it, with the P/T leaves inside
     *     that one pinned to the board-derived reading so the cycle closes after one level.
     *     Anthems count here too.</li>
     * <li><b>Board-derived</b> at the bottom of that recursion, and whenever the leaf is reached
     *     mid-pass. Mid-pass is the CR 613 order, not an approximation â€” a filter applied in
     *     layer 4-6 must not read numbers layer 7 has not produced yet.</li>
     * </ul>
     */
    public int powerForStaticFilter(Permanent permanent) {
        return ptForStaticFilter(permanent, true);
    }

    public int powerFromStaticBoard(Permanent permanent) {
        return ptFromBoard(LayerSystemService.activePass(), permanent, true);
    }

    /**
     * The toughness counterpart of {@link #powerFromStaticBoard(Permanent)}: the layer 7b base
     * (including a characteristic-defining base) plus modifiers, before any layer 7c bonus.
     */
    public int toughnessFromStaticBoard(Permanent permanent) {
        return ptFromBoard(LayerSystemService.activePass(), permanent, false);
    }

    /** The toughness counterpart of {@link #powerForStaticFilter(Permanent)}. */
    public int toughnessForStaticFilter(Permanent permanent) {
        return ptForStaticFilter(permanent, false);
    }

    private int ptForStaticFilter(Permanent permanent, boolean wantPower) {
        LayerSystemService.Pass pass = LayerSystemService.activePass();
        if (pass == null
                || !pass.isBoardReady()
                // Off-battlefield targets (AI hypothetical scoring) have no state in the pass;
                // assembling a bonus for one would reconstruct it from legacy intrinsics anyway.
                || !pass.board().states().containsKey(permanent.getId())
                || PT_LEAF_FROM_BOARD.get().contains(permanent.getId())) {
            return ptFromBoard(pass, permanent, wantPower);
        }
        StaticBonus bonus = ASSEMBLY_IN_PROGRESS.get().contains(permanent.getId())
                ? preliminaryBonus(pass, permanent)
                : computeStaticBonus(pass.gameData(), permanent);
        return wantPower
                ? getEffectivePower(permanent, bonus)
                : getEffectiveToughness(permanent, bonus);
    }

    /**
     * The layer-7 numbers of a permanent whose own static-bonus assembly is already running.
     * Re-runs the assembly for it with the permanent added to {@link #PT_LEAF_FROM_BOARD}, so
     * any P/T leaf reached inside answers from the board instead of recursing a third time. Only
     * the power and toughness of the result are trustworthy â€” every other field was decided with
     * those leaves degraded â€” which is why it is memoized apart from the real bonus.
     *
     * <p>What still cannot be answered is a layer-7c boost whose own filter reads the power it
     * contributes to ("creatures with power 2 or less get +1/+1"): that is circular in the rules,
     * not just in this implementation, and it lands on the board-derived reading.
     */
    private StaticBonus preliminaryBonus(LayerSystemService.Pass pass, Permanent permanent) {
        StaticBonus memoized = pass.preliminaryBonusMemo().get(permanent.getId());
        if (memoized != null) {
            return memoized;
        }
        Set<UUID> fromBoard = PT_LEAF_FROM_BOARD.get();
        fromBoard.add(permanent.getId());
        try {
            StaticBonus bonus = assembleStaticBonusInternal(
                    pass.gameData(), pass.board(), permanent, null, null, null);
            pass.preliminaryBonusMemo().put(permanent.getId(), bonus);
            return bonus;
        } finally {
            fromBoard.remove(permanent.getId());
        }
    }

    /**
     * P/T from the layered board alone â€” everything the pass resolved without running a
     * per-target assembly: the sublayer-7b winner (CR 613.4b) over the permanent's own base,
     * its counters and the floating 7c effects on it (its share of 7c), and the sublayer-7d
     * switch (CR 613.4d).
     * Boosts contributed by OTHER permanents are absent; so is the object's own 7a CDA, which
     * the assembly computes. Falls back to the plain {@link Permanent} accessors with no pass.
     */
    private int ptFromBoard(LayerSystemService.Pass pass, Permanent permanent, boolean wantPower) {
        LayerSystemService.LayeredBoardState board = pass == null ? null : pass.board();
        if (board == null) {
            return wantPower ? permanent.getEffectivePower() : permanent.getEffectiveToughness();
        }
        LayerSystemService.BasePt base = board.basePt7b().get(permanent.getId());
        GameData gameData = pass.gameData();
        int power = SaturatingMath.add(base != null && base.power() != null
                ? SaturatingMath.add(base.power(), permanent.getPowerModifiers())
                : permanent.getEffectivePower(), gameData.floatingPowerBoost(permanent.getId()));
        int toughness = SaturatingMath.add(base != null && base.toughness() != null
                ? SaturatingMath.add(base.toughness(), permanent.getToughnessModifiers())
                : permanent.getEffectiveToughness(), gameData.floatingToughnessBoost(permanent.getId()));
        boolean switched = board.switchedPt7d().contains(permanent.getId());
        if (switched) {
            return wantPower ? toughness : power;
        }
        return wantPower ? power : toughness;
    }

    /**
     * Basic land types for Domain (CR 702.42) evaluated from inside the layered pass. Uses the
     * layered {@link #effectiveBasicLandTypes} â€” so CR 305.7 overrides (Blood Moon, Urborg,
     * Prismatic Omen) count â€” except for a land whose own static bonus is currently being
     * assembled, where the printed types are the only recursion-free answer.
     */
    public Set<CardSubtype> basicLandTypesForStaticEvaluation(GameData gameData, Permanent permanent) {
        if (ASSEMBLY_IN_PROGRESS.get().contains(permanent.getId())) {
            Set<CardSubtype> printed = EnumSet.noneOf(CardSubtype.class);
            for (CardSubtype subtype : permanent.getCard().getSubtypes()) {
                if (BASIC_LAND_SUBTYPES.contains(subtype)) printed.add(subtype);
            }
            return printed;
        }
        return effectiveBasicLandTypes(gameData, permanent);
    }

    /**
     * Returns the amount of combat damage this creature assigns.
     * Normally equal to effective power, but some effects cause a creature to assign
     * damage equal to its toughness instead:
     * <ul>
     *   <li>Equipment/aura-scoped (e.g. Bark of Doran): only when toughness &gt; power, unless
     *       the effect explicitly requires toughness every time (e.g. Gauntlets of Light).</li>
     *   <li>Controller-scoped (e.g. Belligerent Brontodon): always uses toughness.</li>
     * </ul>
     */
    public int getEffectiveCombatDamage(GameData gameData, Permanent creature) {
        // Multiple layered reads for one answer â€” share a single pass across them.
        return withQueryScope(gameData, () -> {
            int power = getEffectivePower(gameData, creature);
            int toughness = getEffectiveToughness(gameData, creature);

            if (hasCompetingCombatDamageAssignmentEffects(gameData, creature)
                    && gameData.combatDamageAssignmentModeChoices.get(creature.getId())
                    == CombatDamageAssignmentMode.TOUGHNESS) {
                return Math.max(0, toughness);
            }

            if (hasSelfToughnessAssignEffect(gameData, creature)
                    && !hasCompetingCombatDamageAssignmentEffects(gameData, creature)) {
                return Math.max(0, toughness);
            }

            if (hasGlobalCombatDamageAssignmentEffect(gameData, CombatDamageAssignmentMode.MANA_VALUE)) {
                return creature.isFaceDown() ? 0 : Math.max(0, creature.getCard().getManaValue());
            }

            // Global-scoped: every creature uses toughness (e.g. Doran, the Siege Tower)
            if (hasGlobalToughnessAssignEffect(gameData)) {
                return Math.max(0, toughness);
            }

            // Controller-scoped: always use toughness (e.g. Belligerent Brontodon)
            if (hasControllerToughnessAssignEffect(gameData, creature)) {
                return Math.max(0, toughness);
            }

            // Some Aura wordings require toughness regardless of the creature's power.
            if (hasAuraWithEffect(gameData, creature,
                    effect -> effect instanceof AssignCombatDamageWithToughnessEffect acdt
                            && acdt.alwaysUseToughness())) {
                return Math.max(0, toughness);
            }

            // Equipment/aura-scoped effects such as Bark of Doran use toughness only when it is
            // greater than power.
            if (toughness > power && hasAuraWithEffect(gameData, creature, AssignCombatDamageWithToughnessEffect.class)) {
                return Math.max(0, toughness);
            }

            // CR 510.1a: a creature assigns combat damage equal to its power. A creature with
            // 0 or negative power assigns 0 combat damage.
            return Math.max(0, power);
        });
    }

    /**
     * Whether an effect that assigns combat damage equal to mana value and an effect that assigns it equal to
     * toughness both apply to {@code creature} and would give different amounts, so its controller chooses which
     * applies. Its choice is read from {@link GameData#combatDamageAssignmentModeChoices}.
     */
    public boolean hasCompetingCombatDamageAssignmentEffects(GameData gameData, Permanent creature) {
        return withQueryScope(gameData, () -> hasGlobalCombatDamageAssignmentEffect(
                        gameData, CombatDamageAssignmentMode.MANA_VALUE)
                && (hasSelfToughnessAssignEffect(gameData, creature)
                || hasGlobalToughnessAssignEffect(gameData)
                || hasControllerToughnessAssignEffect(gameData, creature))
                && (creature.isFaceDown() ? 0 : Math.max(0, creature.getCard().getManaValue()))
                != Math.max(0, getEffectiveToughness(gameData, creature)));
    }

    private boolean hasSelfToughnessAssignEffect(GameData gameData, Permanent creature) {
        if (hasLostAllAbilities(gameData, creature)) {
            return false;
        }
        List<CardEffect> effects = new ArrayList<>(creature.getCard().getStaticEffects());
        effects.addAll(computeStaticBonus(gameData, creature).grantedEffects());
        return effects.stream().anyMatch(effect ->
                effect instanceof AssignCombatDamageWithToughnessEffect assign
                        && assign.subject() instanceof PermanentSubject.Source
                        && (assign.affectedPredicate() == null
                        || predicateEvaluationService.matchesPermanentPredicate(
                        gameData, creature, assign.affectedPredicate())));
    }

    /**
     * Returns the amount of damage a creature deals for non-combat effects that deal damage
     * equal to the creature's power (fight, bite, Arc-Lightning-style, Hunters, Berserker,
     * planeswalker power-damage, etc.). Equivalent to {@code Math.max(0, getEffectivePower(...))}.
     * <p>
     * A creature with 0 or negative power deals 0 damage.
     * <p>
     * Do NOT use this for combat damage assignment â€” use {@link #getEffectiveCombatDamage} instead,
     * which also handles Belligerent-Brontodon / Bark-of-Doran "assign damage equal to toughness"
     * effects that apply only in combat.
     */
    public int getPowerBasedDamage(GameData gameData, Permanent creature) {
        return Math.max(0, getEffectivePower(gameData, creature));
    }

    /**
     * Returns the amount of marked damage that is lethal to this creature. Zilortha-style effects
     * use power, but a creature with zero or negative power still needs at least one damage marked
     * on it before damage is lethal.
     */
    public int getLethalDamageThreshold(GameData gameData, Permanent creature) {
        if (usesPowerForLethalDamage(gameData, creature)) {
            return Math.max(1, getEffectivePower(gameData, creature));
        }
        return getEffectiveToughness(gameData, creature);
    }

    private boolean usesPowerForLethalDamage(GameData gameData, Permanent creature) {
        StaticBonus bonus = computeStaticBonus(gameData, creature);
        if (!bonus.losesAllAbilities()
                && bonus.grantedEffects().stream()
                .filter(LethalDamageModifierEffect.class::isInstance)
                .map(LethalDamageModifierEffect.class::cast)
                .anyMatch(LethalDamageModifierEffect::usesPowerForLethalDamage)) {
            return true;
        }
        // A creature that loses all abilities loses its own abilities, not the static abilities of other
        // permanents that affect it. A controller's "creatures you control" lethal-damage effect (Zilortha)
        // therefore still applies to a creature enchanted by an Aura that removes its abilities.
        UUID controllerId = findPermanentController(gameData, creature.getId());
        if (controllerId == null || !isCreature(gameData, creature, controllerId)) {
            return false;
        }
        return activeStaticEffects(gameData, controllerId, GrantEffectEffect.class).stream()
                .anyMatch(active -> {
                    GrantEffectEffect grant = active.effect();
                    return grant.filter() == null
                            && grant.effect() instanceof LethalDamageModifierEffect modifier
                            && modifier.usesPowerForLethalDamage()
                            && grant.subject() instanceof PermanentSubject.ControlledBy
                            && staticSubjectNames(gameData, active.source(), controllerId, controllerId, creature,
                            grant.subject());
                });
    }

    /**
     * Returns {@code true} if the creature's controller has a permanent on the battlefield
     * with an {@link AssignCombatDamageWithToughnessEffect} whose subject covers this creature
     * (creatures you control, the source included or not).
     */
    /**
     * Returns {@code true} if any player controls a permanent on the battlefield with an
     * {@link AssignCombatDamageWithToughnessEffect} whose subject is every player's creatures
     * (e.g. Doran, the Siege Tower â€” "each creature assigns combat damage equal to its toughness").
     */
    private boolean hasGlobalToughnessAssignEffect(GameData gameData) {
        return hasGlobalCombatDamageAssignmentEffect(gameData, CombatDamageAssignmentMode.TOUGHNESS);
    }

    private boolean hasGlobalCombatDamageAssignmentEffect(
            GameData gameData, CombatDamageAssignmentMode assignmentMode) {
        for (var entry : gameData.playerBattlefields.entrySet()) {
            UUID controllerId = entry.getKey();
            List<Permanent> bf = entry.getValue();
            for (Permanent p : bf) {
                if (p.isFaceDown() || hasLostPrintedAbilities(gameData, p)) continue;
                List<CardEffect> activeEffects = new ArrayList<>();
                for (CardEffect effect : p.getCard().getStaticEffects()) {
                    collectActiveStaticEffects(gameData, p, controllerId, effect, activeEffects);
                }
                for (CardEffect effect : activeEffects) {
                    if (effect instanceof CombatDamageAssignmentEffect assignment
                            && assignment.assignmentMode() == assignmentMode
                            && namesEveryPlayersPermanents(assignment.subject())) {
                        return true;
                    }
                }
            }
        }
        if (gameData.planechase != null) {
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof CombatDamageAssignmentEffect assignment
                            && assignment.assignmentMode() == assignmentMode
                            && namesEveryPlayersPermanents(assignment.subject())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasControllerToughnessAssignEffect(GameData gameData, Permanent creature) {
        UUID controllerId = findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;

        // A player-wide "until end of turn" grant modifies the rules rather than any characteristic, so it
        // follows the creature's current controller (CR 611.2c) instead of a set fixed on resolution.
        for (CardEffect effect : gameData.floatingPlayerEffects(controllerId,
                AssignCombatDamageWithToughnessEffect.class)) {
            if (effect instanceof AssignCombatDamageWithToughnessEffect acdt
                    && acdt.subject() instanceof PermanentSubject.ControlledBy
                    && staticSubjectNames(gameData, null, controllerId, controllerId, creature,
                    acdt.subject())
                    && (acdt.affectedPredicate() == null
                    || predicateEvaluationService.matchesPermanentPredicate(
                            creature,
                            acdt.affectedPredicate(),
                            FilterContext.of(gameData).withSourceControllerId(controllerId)))) {
                return true;
            }
        }

        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        if (bf == null) return false;

        for (Permanent p : bf) {
            if (p.isFaceDown() || hasLostPrintedAbilities(gameData, p)) continue;
            for (CardEffect effect : p.getCard().getStaticEffects()) {
                if (effect instanceof AssignCombatDamageWithToughnessEffect acdt) {
                    PermanentSubject subject = acdt.subject();
                    boolean matchesPredicate = acdt.affectedPredicate() == null
                            || predicateEvaluationService.matchesPermanentPredicate(
                                    creature,
                                    acdt.affectedPredicate(),
                                    FilterContext.of(gameData).withSourceControllerId(controllerId));
                    if (matchesPredicate && subject instanceof PermanentSubject.ControlledBy
                            && staticSubjectNames(gameData, p, controllerId, controllerId, creature, subject)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Computes the aggregate static bonus for a permanent from all other permanents on the
     * battlefield, emblems, and self-referencing characteristic-defining abilities. This includes
     * power/toughness modifiers, granted keywords, protection colors, animation state, granted
     * abilities/effects/colors/subtypes, and color/subtype overriding flags.
     *
     * <p>Returns {@link StaticBonus#NONE} as an early-out when no bonuses apply to a non-creature
     * permanent.
     */
    /**
     * Runs a batch of read-only queries under one shared layered pass: the board fingerprint
     * is checked once and every {@link #computeStaticBonus} call inside the scope hits the
     * pass-level bonus memo instead of re-assembling per query. Reuses an already-active pass
     * when nested.
     *
     * <p>The queries must not mutate game state â€” the memoized bonuses would go stale. Same
     * contract the nested-pass memo already relies on. Writes that neither the layered board nor
     * the static-bonus assembly reads are allowed: becomes-the-target trigger detection appends
     * triggered abilities to the stack, log lines and queued choices inside its scope.
     */
    public <T> T withQueryScope(GameData gameData, Supplier<T> queries) {
        if (layerSystemService.activePass(gameData) != null) {
            return queries.get();
        }
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            return queries.get();
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /**
     * Runs queries in a new layered pass even when the caller already has an active query scope.
     * Use this only when evaluating a temporary, isolated state adjustment whose layered values
     * must not come from the caller's pre-adjustment pass.
     */
    public <T> T withFreshQueryScope(GameData gameData, Supplier<T> queries) {
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            return queries.get();
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    public StaticBonus computeStaticBonus(GameData gameData, Permanent target) {
        return computeStaticBonusForController(gameData, target, null);
    }

    /**
     * Controller-aware form for a permanent whose controller cannot be discovered from current
     * battlefield membership, such as a last-known-information snapshot.
     */
    public StaticBonus computeStaticBonus(GameData gameData, Permanent target, UUID targetControllerId) {
        return computeStaticBonusForController(gameData, target, targetControllerId);
    }

    private StaticBonus computeStaticBonusForController(
            GameData gameData, Permanent target, UUID targetControllerId) {
        // One layered pass per external query: the layer-4 board state is computed once and
        // shared (via the thread-local pass) with every nested computeStaticBonus call made by
        // handlers while assembling bonuses, together with a per-pass bonus memo.
        LayerSystemService.Pass active = layerSystemService.activePass(gameData);
        if (active != null) {
            // The memo is only valid once the layered board is finished: nested calls made by
            // handlers WHILE the layer 5/6 passes are still applying see partial states and
            // must not cache their answers.
            if (!active.isBoardReady()) {
                return assembleStaticBonusForController(
                        gameData, active.board(), target, targetControllerId);
            }
            if (targetControllerId == null) {
                StaticBonus memoized = active.bonusMemo().get(target);
                if (memoized != null) {
                    return memoized;
                }
            }
            StaticBonus bonus = assembleStaticBonusForController(
                    gameData, active.board(), target, targetControllerId);
            if (targetControllerId == null) {
                active.bonusMemo().put(target, bonus);
            }
            return bonus;
        }
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            return assembleStaticBonusForController(gameData, pass.board(), target, targetControllerId);
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /**
     * A {@link StaticBonus} plus its per-source display attribution: one {@link ModifierLine}
     * per contributing source (7c boosts and unmanaged keyword grants diffed during assembly,
     * layer-6/7b/7d contributions read from the board's provenance). Display-only â€” computed
     * by the view-building path, never consulted by rules code.
     */
    public record ExplainedBonus(StaticBonus bonus, List<ModifierLine> lines,
                                 List<GrantedEffectAttribution> grantedEffectAttributions) {
    }

    /** {@link #computeStaticBonus} plus the per-source attribution lines for the client's
     *  hover breakdown. Runs its own assembly (bypassing the per-pass bonus memo) so the
     *  accumulator diffs can be observed; the returned bonus is identical to
     *  {@code computeStaticBonus}'s. */
    public ExplainedBonus explainStaticBonus(GameData gameData, Permanent target) {
        LayerSystemService.Pass active = layerSystemService.activePass(gameData);
        if (active != null) {
            return explainAgainstBoard(gameData, active.board(), target);
        }
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            return explainAgainstBoard(gameData, pass.board(), target);
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    private ExplainedBonus explainAgainstBoard(GameData gameData, LayerSystemService.LayeredBoardState board, Permanent target) {
        List<ModifierLine> lines = new ArrayList<>();
        List<GrantedEffectAttribution> effectAttributions = new ArrayList<>();
        StaticBonus bonus = assembleStaticBonus(gameData, board, target, lines, effectAttributions);
        // Board-recorded lines (layer 6 keyword grants/removals, 7b base setters in resolved
        // order, 7d switches) follow the assembly lines: base lines must fold AFTER the 7a CDA
        // line the assembly may have emitted, mirroring the merge in assembleStaticBonus.
        List<ModifierLine> recorded = board.provenance().get(target.getId());
        if (recorded != null) {
            lines.addAll(recorded);
        }
        List<GrantedEffectAttribution> recordedEffects =
                board.grantedEffectProvenance().get(target.getId());
        if (recordedEffects != null) {
            effectAttributions.addAll(recordedEffects);
        }
        if (bonus == StaticBonus.NONE) {
            return new ExplainedBonus(bonus, List.of(), List.of());
        }
        Set<CardEffect> finalGrantedEffects = Collections.newSetFromMap(new IdentityHashMap<>());
        finalGrantedEffects.addAll(bonus.grantedEffects());
        effectAttributions.removeIf(attribution -> {
            if (finalGrantedEffects.contains(attribution.effect())) {
                return false;
            }
            if (!(attribution.effect() instanceof ProtectionGrantingEffect protection)) {
                return true;
            }
            Set<CardColor> colors = protectionColors(gameData, protection, target);
            return colors.isEmpty() || !bonus.protectionColors().containsAll(colors);
        });
        return new ExplainedBonus(bonus, mergeModifierLines(lines),
                List.copyOf(effectAttributions));
    }

    /** Merges the additive/keyword lines of one source into a single display line; base-setting
     *  and switch lines keep their identity and relative order (folding is order-sensitive). */
    private static List<ModifierLine> mergeModifierLines(List<ModifierLine> lines) {
        Map<String, ModifierLine> merged = new LinkedHashMap<>();
        List<ModifierLine> orderSensitive = new ArrayList<>();
        for (ModifierLine line : lines) {
            if (line.basePower() != null || line.baseToughness() != null || line.switchesPt()) {
                orderSensitive.add(line);
                continue;
            }
            merged.merge(line.source(), line, (a, b) -> {
                Set<Keyword> gained = new HashSet<>(a.gainedKeywords());
                gained.addAll(b.gainedKeywords());
                Set<Keyword> removed = new HashSet<>(a.removedKeywords());
                removed.addAll(b.removedKeywords());
                return new ModifierLine(a.source(), a.power() + b.power(), a.toughness() + b.toughness(),
                        null, null, gained, removed, a.losesAllAbilities() || b.losesAllAbilities(), false);
            });
        }
        List<ModifierLine> result = new ArrayList<>(merged.values());
        result.addAll(orderSensitive);
        return result;
    }

    /**
     * The explain diff baseline: the accumulator's display-relevant state before one source's
     * handlers ran. {@link #diff} turns the delta into that source's attribution line (base
     * overrides are deliberately NOT diffed here â€” every 7b setter is already recorded with
     * attribution by the layered pass; only the self-CDA section diffs the base).
     */
    private record AccumulatorSnapshot(int power, int toughness, Set<Keyword> keywords,
                                       Set<Keyword> removedKeywords, boolean losesAllAbilities,
                                       boolean basePTOverridden, Integer basePowerOverride, Integer baseToughnessOverride,
                                       int grantedEffectCount) {
        static AccumulatorSnapshot of(StaticBonusAccumulator accumulator) {
            return new AccumulatorSnapshot(accumulator.getPower(), accumulator.getToughness(),
                    Set.copyOf(accumulator.getKeywords()), Set.copyOf(accumulator.getRemovedKeywords()),
                    accumulator.isLosesAllAbilities(), accumulator.isBasePTOverridden(),
                    accumulator.getBasePowerOverride(), accumulator.getBaseToughnessOverride(),
                    accumulator.getGrantedEffects().size());
        }

        ModifierLine diff(String source, StaticBonusAccumulator accumulator, boolean includeBase) {
            Set<Keyword> gained = new HashSet<>(accumulator.getKeywords());
            gained.removeAll(keywords);
            Set<Keyword> removed = new HashSet<>(accumulator.getRemovedKeywords());
            removed.removeAll(removedKeywords);
            boolean baseChanged = includeBase && accumulator.isBasePTOverridden()
                    && (!basePTOverridden
                        || !Objects.equals(accumulator.getBasePowerOverride(), basePowerOverride)
                        || !Objects.equals(accumulator.getBaseToughnessOverride(), baseToughnessOverride));
            return new ModifierLine(source,
                    accumulator.getPower() - power, accumulator.getToughness() - toughness,
                    baseChanged ? accumulator.getBasePowerOverride() : null,
                    baseChanged ? accumulator.getBaseToughnessOverride() : null,
                    gained, removed,
                    !losesAllAbilities && accumulator.isLosesAllAbilities(), false);
        }

        List<CardEffect> newlyGrantedEffects(StaticBonusAccumulator accumulator) {
            if (accumulator.getGrantedEffects().size() <= grantedEffectCount) {
                return List.of();
            }
            return List.copyOf(accumulator.getGrantedEffects()
                    .subList(grantedEffectCount, accumulator.getGrantedEffects().size()));
        }
    }

    private static void recordGrantedEffectDiff(
            AccumulatorSnapshot before,
            String sourceName,
            StaticBonusAccumulator accumulator,
            List<GrantedEffectAttribution> explainEffects) {
        if (explainEffects == null) {
            return;
        }
        for (CardEffect effect : before.newlyGrantedEffects(accumulator)) {
            explainEffects.add(new GrantedEffectAttribution(sourceName, effect));
        }
    }

    /** A static-effect source with the CR 613.7 ordering key used by {@link #assembleStaticBonus}. */
    private record StaticSource(Permanent permanent, UUID controllerId, boolean sameBattlefieldAsTarget,
                                long timestamp, int position, boolean fromGraveyard, boolean fromStack) {

        StaticSource(Permanent permanent, UUID controllerId, boolean sameBattlefieldAsTarget,
                     long timestamp, int position, boolean fromGraveyard) {
            this(permanent, controllerId, sameBattlefieldAsTarget, timestamp, position, fromGraveyard, false);
        }
    }

    /** Test JVMs set {@code -DverifyStaticSourceCache=true}: every reuse of a pass's {@link StaticSources} is checked
     *  against a fresh collection (see {@link LayerSystemService.Pass#staticSources}). */
    private static final boolean STATIC_SOURCE_CACHE_VERIFIED = Boolean.getBoolean("verifyStaticSourceCache");

    /**
     * The part of a static-bonus assembly that is the same whichever permanent it assembles for: every source with
     * something to apply, already in CR 613.7 order, and the permanents a floating effect grants a static ability of
     * their own. Collected once per layered pass ({@link LayerSystemService.Pass#staticSources}). Built only by
     * {@code GameQueryService}; its entries compare their objects by identity.
     */
    public record StaticSources(List<StaticSourceEntry> entries, Set<UUID> selfStaticGrantSourceIds) {
    }

    /**
     * One static-ability source as it is collected, not yet wrapped for a target: a battlefield permanent, or the
     * graveyard card, face-up scheme or spell on the stack that each assembly wraps in a {@code Permanent} of its own,
     * as it did before the collection was shared.
     */
    public record StaticSourceEntry(Permanent permanent, Card card, StackEntry spell, UUID controllerId,
                                    long timestamp, int position, boolean fromGraveyard) {

        private StaticSource forTarget(UUID targetControllerId) {
            boolean sameBattlefield = controllerId.equals(targetControllerId);
            if (permanent != null) {
                return new StaticSource(permanent, controllerId, sameBattlefield, timestamp, position, false);
            }
            if (spell != null) {
                return new StaticSource(Permanent.forSpellOnStack(spell), controllerId, sameBattlefield,
                        timestamp, position, false, true);
            }
            return new StaticSource(new Permanent(card), controllerId, sameBattlefield, timestamp, position,
                    fromGraveyard);
        }
    }

    /** The pass's static-ability sources for {@code board}, collected on the first assembly of a finished board. */
    private StaticSources staticSources(GameData gameData, LayerSystemService.LayeredBoardState board) {
        LayerSystemService.Pass pass = layerSystemService.activePass(gameData);
        if (pass == null || pass.board() != board) {
            return collectStaticSources(gameData, board);
        }
        return pass.staticSources(() -> collectStaticSources(gameData, board), STATIC_SOURCE_CACHE_VERIFIED);
    }

    /**
     * Every static-ability source an assembly against {@code board} applies, in CR 613.7 order (timestamp, then
     * position: battlefield order, then each player's graveyard and command-zone sources, then spells on the stack).
     */
    private StaticSources collectStaticSources(GameData gameData, LayerSystemService.LayeredBoardState board) {
        List<StaticSourceEntry> entries = new ArrayList<>();
        int position = 0;
        // Board-wide, so read at most once per collection, and only once a graveyard holds a static source.
        Boolean graveyardAbilitiesLost = null;
        Set<UUID> selfStaticGrantSourceIds = floatingSelfStaticGrantSourceIds(gameData);
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent source : bf) {
                int sourcePosition = position++;
                if (!hasStaticEffectsToApply(board, source, selfStaticGrantSourceIds)) {
                    continue;
                }
                entries.add(new StaticSourceEntry(
                        source, null, null, playerId, source.getTimestamp(), sourcePosition, false));
            }
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard != null) {
                for (Card card : graveyard) {
                    if (card.getStaticEffects().stream()
                            .noneMatch(GraveyardStaticEffect.class::isInstance)) {
                        continue;
                    }
                    if (graveyardAbilitiesLost == null) {
                        graveyardAbilitiesLost = graveyardCardsHaveLostAllAbilities(gameData);
                    }
                    if (graveyardAbilitiesLost) {
                        break;
                    }
                    entries.add(new StaticSourceEntry(null, card, null, playerId, 0, position++, true));
                }
            }
            // CR 314.4: a face-up scheme's static abilities affect the game from the command zone.
            for (Card scheme : gameData.faceUpSchemes(playerId)) {
                entries.add(new StaticSourceEntry(null, scheme, null, playerId, 0, position++, false));
            }
        }
        // CR 113.6: an instant or sorcery's abilities function while it is on the stack.
        for (StackEntry spell : LayerSystemService.stackStaticSources(gameData)) {
            entries.add(new StaticSourceEntry(null, null, spell, spell.getControllerId(),
                    spell.getStackTimestamp(), position++, false));
        }
        entries.sort(Comparator.comparingLong(StaticSourceEntry::timestamp)
                .thenComparingInt(StaticSourceEntry::position));
        return new StaticSources(List.copyOf(entries), selfStaticGrantSourceIds);
    }

    /**
     * Legacy layer 7 accumulator assembly for one permanent, running against the layered board
     * state: sources apply in timestamp order (battlefield position for equal timestamps),
     * subtype/type filters are answered from the L4-corrected {@code CharacteristicState}s via
     * the active pass, and the sublayer-7b base P/T (every setter â€” static, floating one-shot,
     * animation, exchange, March MV â€” resolved in timestamp order by
     * {@code LayerSystemService.applyLayer7b}) is merged over the 7a CDA / intrinsic base.
     */
    private StaticBonus assembleStaticBonus(GameData gameData, LayerSystemService.LayeredBoardState board, Permanent target) {
        return assembleStaticBonusInternal(gameData, board, target, null, null, null);
    }

    private StaticBonus assembleStaticBonusForController(
            GameData gameData, LayerSystemService.LayeredBoardState board,
            Permanent target, UUID targetControllerId) {
        return assembleStaticBonusInternal(gameData, board, target, targetControllerId, null, null);
    }

    /** With a non-null {@code explain} list, additionally records one attribution line per
     *  contributing source by diffing the accumulator around each source's handlers. Only the
     *  view-building path passes a recorder; rules-code callers pay no diffing cost. */
    private StaticBonus assembleStaticBonus(GameData gameData, LayerSystemService.LayeredBoardState board,
                                            Permanent target, List<ModifierLine> explain,
                                            List<GrantedEffectAttribution> explainEffects) {
        return assembleStaticBonusInternal(gameData, board, target, null, explain, explainEffects);
    }

    /**
     * The permanents whose static-bonus assembly is on this thread's call stack. Purely a
     * cycle detector for {@link #powerForStaticFilter}: a layer-7c boost whose own filter reads
     * the boosted permanent's power ("creatures with power 2 or less get +1/+1") would ask for
     * a number this very assembly is in the middle of producing. Recording is unconditional and
     * never blocks re-entry â€” only the P/T leaves consult it, and only to pick their fallback.
     */
    private static final ThreadLocal<Set<UUID>> ASSEMBLY_IN_PROGRESS =
            ThreadLocal.withInitial(HashSet::new);

    /**
     * Whether the static bonus of the permanent {@code permanentId} is being assembled further up this thread, so a
     * read of it would re-enter that assembly (two ability borrowers lending to each other).
     */
    public static boolean isAssemblingStaticBonus(UUID permanentId) {
        return ASSEMBLY_IN_PROGRESS.get().contains(permanentId);
    }

    /**
     * The permanents whose P/T leaves must answer from the layered board alone. Populated by
     * {@link #preliminaryBonus} for the duration of the one extra assembly it runs, which is
     * what terminates the "filter reads the P/T of the permanent being assembled" recursion.
     */
    private static final ThreadLocal<Set<UUID>> PT_LEAF_FROM_BOARD =
            ThreadLocal.withInitial(HashSet::new);

    /**
     * True when this thread is somewhere the fully layered queries must not be called, because
     * calling one would re-enter the work already in flight: inside a static-bonus assembly, or
     * inside a layered pass that is still building its board.
     *
     * <p>This is the ambient replacement for the {@code staticEvaluation} flag that
     * {@code ConditionContext} and {@code AmountContext} used to carry. The flag said the same
     * thing, but said it at the call site â€” every caller had to know which side of the boundary
     * it was on, and a caller that reached an evaluation service through a chain it did not
     * write (or a layered query that happened to be invoked from inside an assembly) got the
     * layered branch and recursed. Both facts are recorded by the machinery that creates the
     * situation, so the boundary can be observed rather than declared.
     *
     * <p>The condition and amount evaluation services consult this to pick their recursion-safe
     * matchers, which read printed and layer-4 state instead of asking {@code computeStaticBonus}.
     */
    public static boolean isStaticEvaluationActive() {
        return !ASSEMBLY_IN_PROGRESS.get().isEmpty() || LayerSystemService.buildingBoard();
    }

    private StaticBonus assembleStaticBonusInternal(
            GameData gameData, LayerSystemService.LayeredBoardState board,
            Permanent target, UUID targetControllerId, List<ModifierLine> explain,
            List<GrantedEffectAttribution> explainEffects) {
        Set<UUID> inProgress = ASSEMBLY_IN_PROGRESS.get();
        boolean tracked = inProgress.add(target.getId());
        try {
            return assembleStaticBonusUnguarded(
                    gameData, board, target, targetControllerId, explain, explainEffects);
        } finally {
            if (tracked) {
                inProgress.remove(target.getId());
            }
        }
    }

    private StaticBonus assembleStaticBonusUnguarded(
            GameData gameData, LayerSystemService.LayeredBoardState board,
            Permanent target, UUID targetControllerId, List<ModifierLine> explain,
            List<GrantedEffectAttribution> explainEffects) {
        boolean isNaturalCreature = hasCardType(target, CardType.CREATURE);
        StaticBonusAccumulator accumulator = new StaticBonusAccumulator();
        UUID resolvedTargetControllerId = targetControllerId;
        if (resolvedTargetControllerId == null) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> bf = gameData.playerBattlefields.get(playerId);
                if (bf != null && bf.contains(target)) {
                    resolvedTargetControllerId = playerId;
                    break;
                }
            }
        }
        StaticSources collected = staticSources(gameData, board);
        Set<UUID> selfStaticGrantSourceIds = collected.selfStaticGrantSourceIds();
        List<StaticSource> sources = new ArrayList<>(collected.entries().size());
        for (StaticSourceEntry entry : collected.entries()) {
            sources.add(entry.forTarget(resolvedTargetControllerId));
        }
        List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
        for (StaticSource sourceSlot : sources) {
            Permanent source = sourceSlot.permanent();
            // CR 613.8a(1)/CR 305.7: a source whose abilities are gone â€” "loses all abilities"
            // applied in layer 6, or a land whose type was set (removing its printed abilities
            // in layer 4) â€” contributes nothing in layer 7 either: a lose-all'd lord grants
            // neither its keyword (suppressed by the pass) nor its 7c boost. Managed layer-4
            // replays below still run: the pass already decided existence with correct
            // ordering, and a skipped instance simply recorded no contributions.
            CharacteristicState sourceState = board.states().get(source.getId());
            boolean sourceAbilitiesGone = sourceState != null
                    && (sourceState.isLosesAllAbilities() || sourceState.isLosesAllNonManaAbilities()
                    || sourceState.isPrintedAbilitiesRemoved());
            if (!sourceAbilitiesGone && !source.isAttached() && selfStaticGrantSourceIds.contains(source.getId())) {
                applyFloatingStaticGrantsFromSource(gameData, sourceSlot, target, accumulator, globalWordChange);
            }
            StaticEffectContext context = new StaticEffectContext(
                    source, target, sourceSlot.controllerId(), sourceSlot.sameBattlefieldAsTarget(), gameData);
            AccumulatorSnapshot beforeSource = explain != null ? AccumulatorSnapshot.of(accumulator) : null;
            if (sourceState != null) {
                for (CardEffect grantedEffect : sourceState.getGrantedStaticEffects()) {
                    if (!(grantedEffect instanceof com.github.laxika.magicalvibes.model.effect.StaticBoostEffect)) {
                        continue;
                    }
                    StaticEffectHandler grantedHandler = staticEffectRegistry.getHandler(grantedEffect);
                    if (grantedHandler != null) {
                        boolean managed = board.isManagedL56(grantedEffect);
                        accumulator.setLayeredOutputsSuppressed(managed);
                        try {
                            grantedHandler.apply(context, grantedEffect, accumulator);
                        } finally {
                            accumulator.setLayeredOutputsSuppressed(false);
                        }
                    }
                }
            }
            for (CardEffect effect : source.getCard().getStaticEffects()) {
                if (source.isFaceDown()) {
                    continue;
                }
                if (layerSystemService.isExcludedProtectionGrant(gameData, source.getId(), effect)) {
                    continue;
                }
                if (source == target && !appliesToItsOwnSourceInStaticPass(board, source, effect)) {
                    continue;
                }
                if (sourceSlot.fromGraveyard() != (effect instanceof GraveyardStaticEffect)
                        || sourceSlot.fromStack() != (effect instanceof WrappedStackStaticEffect)) {
                    continue;
                }
                // Purely type-changing effects were applied by the layer-4 pass (with filters
                // evaluated as of each effect's own application); replay its recorded decision
                // instead of re-running the handler against the finished states, which would
                // let self-referencing filters negate their own output (Bludgeon Brawl).
                if (board.isManagedL4(effect)) {
                    board.replayL4Contribution(effect, target.getId(), accumulator);
                    continue;
                }
                if (effect instanceof EnchantedPermanentBecomesCreatureEffect becomes
                        && becomes.powerToughnessEqualsManaValue()
                        && !board.hasL4Contribution(effect, target.getId())) {
                    continue;
                }
                if (sourceAbilitiesGone) {
                    continue;
                }
                StaticEffectHandler handler = staticEffectRegistry.getHandler(effect);
                if (handler != null) {
                    // Layer 5/6 outputs of pass-managed instances were applied in timestamp
                    // order by the layered pass and merged in below; the handler still runs for
                    // its other-layer outputs (a lord's 7c boost) with those adds discarded.
                    // The handler sees the layer-3 view of the ability â€” the source's text
                    // changes applied â€” matching what the layered pass applied.
                    boolean layeredManaged = board.isManagedL56(effect);
                    if (layeredManaged) {
                        accumulator.setLayeredOutputsSuppressed(true);
                    }
                    try {
                        handler.apply(context,
                                TextChangeTransformer.transform(effect, gameData.textReplacements(source.getId()),
                                        globalWordChange),
                                accumulator);
                    } finally {
                        if (layeredManaged) {
                            accumulator.setLayeredOutputsSuppressed(false);
                        }
                    }
                }
            }
            if (beforeSource != null) {
                ModifierLine line = beforeSource.diff(FaceDownVisibility.publicName(gameData, source), accumulator, false);
                if (!line.isEmpty()) {
                    explain.add(line);
                }
                recordGrantedEffectDiff(beforeSource, FaceDownVisibility.publicName(gameData, source),
                        accumulator, explainEffects);
            }
            if (sourceState != null) {
                for (CardEffect effect : sourceState.getGrantedStaticEffects()) {
                    // This wrapper marks the triggered ability now possessed by the source;
                    // it is not another static ability that can grant itself onward.
                    if (effect instanceof GrantTriggeredAbilityEffect
                            || effect instanceof StaticBoostEffect) {
                        continue;
                    }
                    StaticEffectHandler handler = staticEffectRegistry.getHandler(effect);
                    if (handler != null) {
                        handler.apply(context, effect, accumulator);
                    }
                }
            }
        }
        if (gameData.planechase != null) {
            UUID controller = gameData.planechase.controllerId;
            for (var planar : gameData.planechase.faceUp) {
                StaticEffectContext context = new StaticEffectContext(null, target, controller,
                        controller.equals(resolvedTargetControllerId), gameData, planar);
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    StaticEffectHandler handler = staticEffectRegistry.getHandler(effect);
                    if (handler == null) continue;
                    accumulator.setLayeredOutputsSuppressed(board.isManagedL56(effect));
                    try {
                        handler.apply(context, effect, accumulator);
                    } finally {
                        accumulator.setLayeredOutputsSuppressed(false);
                    }
                }
            }
        }

        for (Emblem emblem : gameData.emblems) {
            List<Permanent> ownerBf = gameData.playerBattlefields.get(emblem.controllerId());
            // A last-known-information snapshot is no longer on the battlefield, but the controller the
            // caller supplied still has its emblem apply to it.
            boolean targetUnderEmblemControl = ownerBf != null && ownerBf.contains(target)
                    || emblem.controllerId().equals(targetControllerId);
            if (!targetUnderEmblemControl) continue;
            AccumulatorSnapshot beforeEmblem = explain != null ? AccumulatorSnapshot.of(accumulator) : null;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof GrantActivatedAbilityEffect grant
                        && emblemNames(gameData, emblem, target, grant.subject())
                        // null GameData: type predicates read printed/granted types without
                        // re-entering computeStaticBonus (same as GrantKeywordEffect below).
                        && (grant.filter() == null || predicateEvaluationService.matchesPermanentPredicate(null, target, grant.filter()))) {
                    accumulator.addActivatedAbility(grant.ability());
                } else if (effect instanceof DynamicStaticBoostEffect boost
                        && emblemNames(gameData, emblem, target, boost.subject())
                        && (boost.filter() == null
                                || predicateEvaluationService.matchesPermanentPredicate(null, target, boost.filter()))) {
                    AmountContext amountContext = boost.amountsFromTarget()
                            ? AmountContext.forStaticEffect(target, gameData.findControllerOf(target))
                            : AmountContext.forStaticEffect(null, emblem.controllerId());
                    accumulator.addPower(amountEvaluationService.evaluate(gameData, boost.powerBoost(), amountContext));
                    accumulator.addToughness(amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), amountContext));
                } else if (effect instanceof StaticBoostEffect boost
                        // Recursion-safe: isCreature(gameData, target) re-enters computeStaticBonus for
                        // this same target (this method IS the assembly), so an emblem's "creatures you
                        // control get +1/+0" (Sorin, Lord of Innistrad) is judged on the board the pass
                        // already computed (emblemNames).
                        && emblemNames(gameData, emblem, target, boost.subject())
                        // The static-filter path reads the layer-4 state of this pass, so types
                        // granted by continuous effects (Encroaching Mycosynth) are visible.
                        && (boost.filter() == null || predicateEvaluationService.matchesStaticFilter(
                                target, boost.filter(),
                                FilterContext.of(gameData).withSourceControllerId(emblem.controllerId())))) {
                    accumulator.addPower(boost.powerBoost());
                    accumulator.addToughness(boost.toughnessBoost());
                    accumulator.addKeywords(boost.grantedKeywords());
                } else if (effect instanceof GrantKeywordEffect grant
                        && emblemNames(gameData, emblem, target, grant.subject())
                        // Evaluate the filter with a null GameData so type predicates read the
                        // permanent's printed/granted types directly instead of re-entering
                        // computeStaticBonus for this same target (which would recurse forever).
                        && (grant.filter() == null || predicateEvaluationService.matchesPermanentPredicate(null, target, grant.filter()))) {
                    accumulator.addKeywords(grant.keywords());
                } else if (effect instanceof SetBasePowerToughnessEffect setPT
                        && emblemNames(gameData, emblem, target, setPT.subject())
                        && (setPT.filter() == null
                                || predicateEvaluationService.matchesPermanentPredicate(null, target, setPT.filter()))) {
                    accumulator.setBasePTOverride(setPT.power(), setPT.toughness());
                } else if (effect instanceof GrantTriggeredAbilityEffect grant
                        && emblemNames(gameData, emblem, target, grant.subject())
                        && (grant.filter() == null
                                || predicateEvaluationService.matchesPermanentPredicate(null, target, grant.filter()))) {
                    accumulator.addGrantedEffect(grant);
                }
            }
            if (beforeEmblem != null) {
                String emblemName = emblem.sourceCard() != null ? emblem.sourceCard().getName() : "Emblem";
                ModifierLine line = beforeEmblem.diff(emblemName, accumulator, false);
                if (!line.isEmpty()) {
                    explain.add(line);
                }
                recordGrantedEffectDiff(beforeEmblem, emblemName, accumulator, explainEffects);
            }
        }

        // Resolved P/T boosts ("gets +3/+3 until end of turn", Riding the Dilu Horse's indefinite buff): floating
        // PowerToughnessBoostEffects on this permanent with their own durations. They have no static-slot
        // source, so they are read here rather than through a handler: the additive +X/+Y (7c, CR 613.4c) and
        // granted keywords (layer 6) apply while the effect lasts; several stack additively. Each is attributed to
        // the card that made it.
        // A permanent that left has the boosts it had as it left: its last-known information (CR 608.2h).
        List<FloatingContinuousEffect> departedEffects = gameData.departedPermanentEffects.get(target.getId());
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : departedEffects == null ? gameData.floatingEffects
                    : concat(gameData.floatingEffects, departedEffects)) {
                if (board.isManagedL4(floating.effect())) {
                    board.replayL4Contribution(floating.effect(), target.getId(), accumulator);
                }
                if (floating.effect() instanceof PowerToughnessBoostEffect buff
                        && target.getId().equals(floating.affectedPermanentId())) {
                    AccumulatorSnapshot beforeBuff = explain != null ? AccumulatorSnapshot.of(accumulator) : null;
                    accumulator.addPower(buff.powerBoost());
                    accumulator.addToughness(buff.toughnessBoost());
                    if (!hasLaterLosesAllAbilitiesEffect(gameData, target.getId(), floating.timestamp())) {
                        accumulator.addKeywords(buff.keywords());
                    }
                    if (beforeBuff != null) {
                        ModifierLine line = beforeBuff.diff(floating.sourceCardName() != null
                                ? floating.sourceCardName() : "Indefinite buff", accumulator, false);
                        if (!line.isEmpty()) {
                            explain.add(line);
                        }
                    }
                }
            }
        }
        AccumulatorSnapshot beforeIndefinite = explain != null ? AccumulatorSnapshot.of(accumulator) : null;
        // A perpetual P/T change is made to the card; a face-down permanent has none of its card's characteristics
        // (CR 708.2a), so it applies once the permanent is turned face up.
        CardPowerToughnessModifier perpetualModifier = target.isFaceDown() ? null
                : gameData.perpetualCardPowerToughnessModifiers.get(target.getCard().getId());
        if (perpetualModifier != null) {
            accumulator.addPower(perpetualModifier.power());
            accumulator.addToughness(perpetualModifier.toughness());
        }
        // A change made to the physical card while it was elsewhere (in a hand, a library) stays with it when it
        // enters as a copy of something else; a copy's own card is read above.
        CardPowerToughnessModifier physicalCardModifier = target.isFaceDown() || target.getOriginalCard() == null
                || target.getOriginalCard().getId().equals(target.getCard().getId()) ? null
                : gameData.perpetualCardPowerToughnessModifiers.get(target.getOriginalCard().getId());
        if (physicalCardModifier != null) {
            accumulator.addPower(physicalCardModifier.power());
            accumulator.addToughness(physicalCardModifier.toughness());
        }
        if (beforeIndefinite != null) {
            ModifierLine line = beforeIndefinite.diff("Indefinite buff", accumulator, false);
            if (!line.isEmpty()) {
                explain.add(line);
            }
        }

        // Handle characteristic-defining abilities (self-referencing static effects like */* P/T)
        CharacteristicState state = board.states().get(target.getId());
        if (state != null) {
            Set<CardSupertype> layeredGrantedSupertypes = new HashSet<>(state.getSupertypes());
            layeredGrantedSupertypes.removeAll(target.getCard().getSupertypes());
            accumulator.getGrantedSupertypes().addAll(layeredGrantedSupertypes);
            if (!Objects.equals(state.getName(), target.getCard().getName())) {
                accumulator.setName(state.getName());
            }
            if (state.isCardTypesOverridden()) {
                accumulator.getGrantedCardTypes().addAll(state.getCardTypes());
                accumulator.setCardTypeOverriding(true);
            }
        }
        AccumulatorSnapshot beforeSelf = explain != null ? AccumulatorSnapshot.of(accumulator) : null;
        for (CardEffect effect : target.getCard().getStaticEffects()) {
            if (target.isFaceDown()) continue;
            if (effect instanceof GraveyardStaticEffect || effect instanceof WrappedStackStaticEffect) {
                continue;
            }
            // A self-including scope (ALL_LANDS_INCLUDING_SELF, ALL_CREATURES_INCLUDING_SELF)
            // records a layer-4 contribution on its own source; the source-loop above skips
            // source == target, so the replay for those has to happen here.
            if (board.isManagedL4(effect)) {
                board.replayL4Contribution(effect, target.getId(), accumulator);
                continue;
            }
            if (state != null
                    && (state.isLosesAllAbilities() || state.isLosesAllNonManaAbilities()
                    || state.isPrintedAbilitiesRemoved()) && !board.isManagedL56(effect)) {
                continue;
            }
            StaticEffectHandler selfHandler = staticEffectRegistry.getSelfHandler(effect);
            if (selfHandler != null) {
                // CR 613.4a: an object whose own static abilities were removed in layer 6
                // ("loses all abilities") contributes no characteristic-defining P/T in 7a â€”
                // Maro under Merfolk Trickster is 0/0. The removal is NOT retroactive on
                // layers 2-5: type/color contributions the removed abilities already made in
                // earlier layers stay applied (CR 613 layers apply in order; see
                // agent-docs/LAYER_SYSTEM.md Â§3).
                if (effect instanceof SetPowerToughnessToAmountEffect
                        && state != null && state.isLosesAllAbilities()) {
                    continue;
                }
                boolean layeredManaged = board.isManagedL56(effect);
                if (layeredManaged) {
                    accumulator.setLayeredOutputsSuppressed(true);
                }
                try {
                    StaticEffectContext selfContext = new StaticEffectContext(
                            target, target, resolvedTargetControllerId, true, gameData);
                    selfHandler.apply(selfContext,
                            TextChangeTransformer.transform(effect, gameData.textReplacements(target.getId()),
                                    globalWordChange),
                            accumulator);
                } finally {
                    if (layeredManaged) {
                        accumulator.setLayeredOutputsSuppressed(false);
                    }
                }
            }
        }
        if (state != null) {
            for (CardEffect effect : state.getGrantedStaticEffects()) {
                // The wrapper is returned below as a granted ability, not re-applied as a grant.
                if (effect instanceof GrantTriggeredAbilityEffect) {
                    continue;
                }
                StaticEffectHandler selfHandler = staticEffectRegistry.getSelfHandler(effect);
                if (selfHandler != null) {
                    selfHandler.apply(new StaticEffectContext(
                            target, target, resolvedTargetControllerId, true, gameData), effect, accumulator);
                }
            }
        }
        if (beforeSelf != null) {
            // The 7a CDA base line â€” emitted before the board's 7b lines fold over it.
            ModifierLine line = beforeSelf.diff(FaceDownVisibility.publicName(gameData, target), accumulator, true);
            if (!line.isEmpty()) {
                explain.add(line);
            }
            recordGrantedEffectDiff(beforeSelf, FaceDownVisibility.publicName(gameData, target),
                    accumulator, explainEffects);
        }

        int honePower = honeCounterPowerBonus(gameData, target);
        if (honePower > 0 && (isNaturalCreature
                || isCreatureInStaticPass(board, target)
                || accumulator.isAnimatedCreature()
                || accumulator.isSelfBecomeCreature())) {
            accumulator.addPower(honePower);
        }

        // Sublayer 7b: the timestamp-resolved base P/T from the layered pass overrides the base
        // decided so far (the 7a CDA applied just above, or the intrinsic base). CR 613.4:
        // 7a applies before 7b, so the latest-timestamp setter beats the CDA regardless of when
        // either arrived; a component no 7b entry set (power-only exchange) keeps the 7a/ladder
        // value. Precedence between setters lives entirely in LayerSystemService.applyLayer7b.
        LayerSystemService.BasePt basePt7b = board.basePt7b().get(target.getId());
        if (basePt7b != null) {
            Integer accumulatedBasePower = accumulator.getBasePowerOverride();
            Integer accumulatedBaseToughness = accumulator.getBaseToughnessOverride();
            int basePower = basePt7b.power() != null ? basePt7b.power()
                    : accumulatedBasePower != null ? accumulatedBasePower
                    : target.getBasePower();
            int baseToughness = basePt7b.toughness() != null ? basePt7b.toughness()
                    : accumulatedBaseToughness != null ? accumulatedBaseToughness
                    : target.getBaseToughness();
            accumulator.setBasePTOverride(basePower, baseToughness);
        } else if (state == null) {
            // A departed snapshot has no 7b entry on the board: the base-P/T setters still on its id are part
            // of its last-known characteristics (CR 608.2h), applied in timestamp order (CR 613.7).
            BasePtOnId base = floatingBasePt(gameData, target.getId());
            if (base != null) {
                Integer accumulatedBasePower = accumulator.getBasePowerOverride();
                Integer accumulatedBaseToughness = accumulator.getBaseToughnessOverride();
                accumulator.setBasePTOverride(
                        base.power() != null ? base.power()
                                : accumulatedBasePower != null ? accumulatedBasePower : target.getBasePower(),
                        base.toughness() != null ? base.toughness()
                                : accumulatedBaseToughness != null ? accumulatedBaseToughness : target.getBaseToughness());
            }
        }

        // A permanent off the battlefield (a departed snapshot) has no layered state: its one-shot
        // type and keyword grants and keyword removals are still floating effects on its id and count
        // as its last-known characteristics, with the keywords baked into the snapshot (CR 608.2h).
        if (state == null) {
            for (var floating : gameData.floatingEffectsOnPermanent(target.getId(),
                    GrantCardTypeEffect.class, GrantSubtypeEffect.class, GrantKeywordEffect.class,
                    RemoveKeywordEffect.class)) {
                if (floating.effect() instanceof GrantCardTypeEffect grant) {
                    accumulator.getGrantedCardTypes().add(grant.cardType());
                } else if (floating.effect() instanceof GrantSubtypeEffect grant && !grant.overriding()) {
                    accumulator.addGrantedSubtype(grant.subtype());
                } else if (floating.effect() instanceof GrantKeywordEffect grant
                        && !gameData.losesAllAbilitiesByEffect(target)) {
                    accumulator.addKeywords(grant.keywords());
                } else if (floating.effect() instanceof RemoveKeywordEffect removal) {
                    accumulator.removeKeyword(removal.keyword());
                }
            }
            if (target.getLastKnownKeywords() != null) {
                accumulator.addKeywords(target.getLastKnownKeywords());
            }
        }

        if (state != null && (state.isCardTypesOverridden()
                || !state.getCardTypes().equals(baseCardTypes(target))
                || gameData.floatingEffectsOnPermanent(target.getId(), GrantCardTypeEffect.class).stream()
                        .anyMatch(fe -> !state.getCardTypes()
                                .contains(((GrantCardTypeEffect) fe.effect()).cardType())))) {
            accumulator.getGrantedCardTypes().clear();
            accumulator.getGrantedCardTypes().addAll(state.getCardTypes());
            accumulator.setCardTypeOverriding(true);
        }

        // Sublayer 7d: the parity of the active floating switch effects, resolved by the
        // layered pass; the effective-P/T queries swap the finished 7a-7c values when set.
        boolean ptSwitched = board.switchedPt7d().contains(target.getId());

        boolean layeredTouched = state != null
                && (board.l56Touched().contains(target.getId()) || basePt7b != null || ptSwitched);
        boolean isSelfAnimated = gameData.animatedByFloatingEffect(target.getId()) || target.getCounterCount(CounterType.AWAKENING) > 0 || accumulator.isSelfBecomeCreature();
        if (!isNaturalCreature
                && !accumulator.isAnimatedCreature()
                && !isSelfAnimated
                && !layeredTouched
                && accumulator.getKeywords().isEmpty()
                && (state != null || accumulator.getRemovedKeywords().isEmpty())
                && accumulator.getGrantedActivatedAbilities().isEmpty()
                && accumulator.getGrantedEffects().isEmpty()
                && accumulator.getProtectionColors().isEmpty()
                && accumulator.getGrantedColors().isEmpty()
                && accumulator.getGrantedSubtypes().isEmpty()
                && accumulator.getGrantedCardTypes().isEmpty()
                && accumulator.getPower() == 0
                && accumulator.getToughness() == 0
                && !accumulator.isBasePTOverridden()
                && !accumulator.isSubtypeOverriding()
                && !accumulator.isLandSubtypeOverriding()
                && !accumulator.isCardTypeOverriding()
                && accumulator.getGrantedSupertypes().isEmpty()
                && accumulator.getName() == null
                && !accumulator.isTurnFaceUpPrevented()
                && !accumulator.isTokenized()) {
            return StaticBonus.NONE;
        }

        // Merge the layered layer 5/6 results (applied in CR 613.7 timestamp order by the pass)
        // with the unmanaged legacy accumulator outputs (emblems, conditional wrappers), which
        // stay additive outside timestamp order. bonus.keywords() becomes the COMPLETE keyword
        // set (printed included); bonus.removedKeywords() reports the seeded keywords the
        // layered pass removed, so consumers and views see removals and re-grants correctly.
        Set<Keyword> keywords = accumulator.getKeywords();
        Set<Keyword> removedKeywords = accumulator.getRemovedKeywords();
        Set<CardColor> grantedColors = accumulator.getGrantedColors();
        boolean colorOverriding = accumulator.isColorOverriding();
        Set<CardColor> protectionColors = accumulator.getProtectionColors();
        Set<CardColor> removedProtectionColors = Set.of();
        List<ActivatedAbility> grantedActivatedAbilities = accumulator.getGrantedActivatedAbilities();
        List<CardEffect> grantedEffects = accumulator.getGrantedEffects();
        boolean losesAllAbilities = accumulator.isLosesAllAbilities();
        boolean losesAllNonManaAbilities = accumulator.isLosesAllNonManaAbilities();
        if (state != null) {
            boolean abilityGainProhibited = state.isAbilityGainProhibited();
            Set<Keyword> mergedKeywords = new HashSet<>(state.getKeywords());
            if (!abilityGainProhibited) {
                mergedKeywords.addAll(accumulator.getKeywords());
            }
            mergedKeywords.removeAll(accumulator.getRemovedKeywords());
            keywords = mergedKeywords;
            Set<Keyword> mergedRemoved = new HashSet<>(accumulator.getRemovedKeywords());
            for (Keyword seeded : state.getSeededKeywords()) {
                if (!mergedKeywords.contains(seeded)) {
                    mergedRemoved.add(seeded);
                }
            }
            removedKeywords = mergedRemoved;
            Set<CardColor> mergedColors = EnumSet.noneOf(CardColor.class);
            mergedColors.addAll(state.getColors());
            if (state.isColorsOverridden()) {
                colorOverriding = true;
            } else {
                mergedColors.removeAll(state.getSeededColors());
            }
            mergedColors.addAll(accumulator.getGrantedColors());
            grantedColors = mergedColors;
            Set<CardColor> mergedProtection = EnumSet.noneOf(CardColor.class);
            mergedProtection.addAll(state.getProtectionColors());
            if (!abilityGainProhibited) {
                mergedProtection.addAll(accumulator.getProtectionColors());
            }
            protectionColors = mergedProtection;
            removedProtectionColors = Set.copyOf(state.getRemovedProtectionColors());
            List<ActivatedAbility> mergedAbilities = new ArrayList<>(state.getGrantedActivatedAbilities());
            if (!abilityGainProhibited) {
                mergedAbilities.addAll(accumulator.getGrantedActivatedAbilities());
            }
            grantedActivatedAbilities = mergedAbilities;
            List<CardEffect> mergedEffects = new ArrayList<>(state.getGrantedStaticEffects());
            Map<CardEffect, Integer> layeredOccurrences = new IdentityHashMap<>();
            for (CardEffect effect : state.getGrantedStaticEffects()) {
                layeredOccurrences.merge(effect, 1, Integer::sum);
            }
            if (!abilityGainProhibited && !state.isLosesAllAbilities()
                    && !state.isLosesAllNonManaAbilities()) {
                for (CardEffect effect : accumulator.getGrantedEffects()) {
                    int overlap = layeredOccurrences.getOrDefault(effect, 0);
                    if (overlap > 0) {
                        layeredOccurrences.put(effect, overlap - 1);
                    } else {
                        mergedEffects.add(effect);
                    }
                }
            }
            grantedEffects = mergedEffects;
            Set<Keyword> blockedKeywords = mergedEffects.stream()
                    .filter(CantHaveOrGainKeywordEffect.class::isInstance)
                    .map(effect -> ((CantHaveOrGainKeywordEffect) effect).keyword())
                    .collect(java.util.stream.Collectors.toSet());
            mergedKeywords.removeAll(blockedKeywords);
            keywords = mergedKeywords;
            losesAllAbilities = state.isLosesAllAbilities() || accumulator.isLosesAllAbilities();
            losesAllNonManaAbilities = state.isLosesAllNonManaAbilities()
                    || accumulator.isLosesAllNonManaAbilities();
        } else {
            // The target is not on a battlefield (AI hypothetical evaluation of an uncast
            // permanent), so the layered pass carries no state for it. bonus.keywords() must
            // still be the complete set â€” reconstruct it with the legacy Permanent.hasKeyword
            // semantics plus the accumulator's grants.
            Set<Keyword> mergedKeywords = new HashSet<>(accumulator.getKeywords());
            if (!gameData.losesAllAbilitiesByEffect(target)) {
                mergedKeywords.addAll(target.getCard().getKeywords());
            }
            mergedKeywords.removeAll(accumulator.getRemovedKeywords());
            keywords = mergedKeywords;
        }

        return new StaticBonus(accumulator.getPower(), accumulator.getToughness(), keywords,
                protectionColors, removedProtectionColors, state != null && state.isPrintedProtectionRemoved(),
                accumulator.isAnimatedCreature() || isSelfAnimated, accumulator.getCreatureCount(),
                grantedActivatedAbilities, grantedEffects, grantedColors,
                accumulator.getGrantedSubtypes(), accumulator.getGrantedCardTypes(),
                accumulator.getGrantedSupertypes(), colorOverriding,
                accumulator.isSubtypeOverriding(), accumulator.isLandSubtypeOverriding(),
                accumulator.isCardTypeOverriding(),
                removedKeywords, accumulator.isBasePTOverridden(),
                accumulator.getBasePowerOverride() != null ? accumulator.getBasePowerOverride() : 0,
                accumulator.getBaseToughnessOverride() != null ? accumulator.getBaseToughnessOverride() : 0,
                losesAllAbilities, losesAllNonManaAbilities, ptSwitched, accumulator.getName(),
                accumulator.isTurnFaceUpPrevented(), accumulator.isTokenized());
    }

    /** Hone counters grant their power bonus independently of any ability on the Equipment. */
    private int honeCounterPowerBonus(GameData gameData, Permanent target) {
        int powerBonus = 0;
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) continue;
            for (Permanent equipment : battlefield) {
                if (equipment.isAttached()
                        && target.getId().equals(equipment.getAttachedTo())
                        && permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)) {
                    powerBonus += equipment.getCounterCount(CounterType.HONE);
                }
            }
        }
        return powerBonus;
    }

    /**
     * Whether a battlefield permanent has anything for a static-bonus assembly to run: a printed static ability, a
     * static ability the layered pass granted it, or a floating static grant to itself. A source with none would
     * apply no handler, so the assembly leaves it out instead of visiting every permanent on the battlefield.
     */
    private static boolean hasStaticEffectsToApply(LayerSystemService.LayeredBoardState board, Permanent source,
                                                   Set<UUID> selfStaticGrantSourceIds) {
        if (!source.getCard().getStaticEffects().isEmpty()
                || selfStaticGrantSourceIds.contains(source.getId())) {
            return true;
        }
        CharacteristicState state = board.states().get(source.getId());
        return state != null && !state.getGrantedStaticEffects().isEmpty();
    }

    /**
     * The permanents a floating effect grants a static ability of their own, the grants
     * {@link #applyFloatingStaticGrantsFromSource} applies.
     */
    private static Set<UUID> floatingSelfStaticGrantSourceIds(GameData gameData) {
        Set<UUID> sourceIds = null;
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (floating.effect() instanceof GrantStaticEffectToSourceEffect
                        && floating.sourcePermanentId() != null
                        && floating.sourcePermanentId().equals(floating.affectedPermanentId())) {
                    if (sourceIds == null) {
                        sourceIds = new HashSet<>();
                    }
                    sourceIds.add(floating.sourcePermanentId());
                }
            }
        }
        return sourceIds == null ? Set.of() : sourceIds;
    }

    private void applyFloatingStaticGrantsFromSource(GameData gameData, StaticSource sourceSlot,
                                                       Permanent target, StaticBonusAccumulator accumulator,
                                                       List<TextReplacement> globalWordChange) {
        Permanent source = sourceSlot.permanent();
        StaticEffectContext context = new StaticEffectContext(
                source, target, sourceSlot.controllerId(), sourceSlot.sameBattlefieldAsTarget(), gameData);
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (!(floating.effect() instanceof GrantStaticEffectToSourceEffect grant)
                        || !source.getId().equals(floating.sourcePermanentId())
                        || !source.getId().equals(floating.affectedPermanentId())) {
                    continue;
                }
                StaticEffectHandler handler = source.getId().equals(target.getId())
                        ? staticEffectRegistry.getSelfHandler(grant.staticEffect())
                        : staticEffectRegistry.getHandler(grant.staticEffect());
                if (handler == null) {
                    handler = staticEffectRegistry.getHandler(grant.staticEffect());
                }
                if (handler != null) {
                    handler.apply(context,
                            TextChangeTransformer.transform(grant.staticEffect(), gameData.textReplacements(source.getId()),
                                    globalWordChange),
                            accumulator);
                }
            }
        }
    }

    // --- Protection & evasion ---

    /**
     * Returns {@code true} if the target permanent has protection from the source's controller: a granted
     * "protection from [player]" ability that survives the layered pass (CR 613.7), or its own "protection from the
     * chosen player" ability.
     */
    public boolean hasProtectionFromOpponents(GameData gameData, Permanent target, UUID sourceControllerId) {
        if (target == null || sourceControllerId == null) return false;
        StaticBonus bonus = computeStaticBonus(gameData, target);
        for (CardEffect effect : bonus.grantedEffects()) {
            if (effect instanceof ProtectionGrantingEffect protection
                    && protection.protectionFromPlayers().contains(sourceControllerId)) {
                return true;
            }
        }
        if (gameData.losesAllAbilitiesByEffect(target) || bonus.losesAllAbilities() || bonus.printedProtectionRemoved()) {
            return false;
        }
        return target.getChosenPlayerIds().contains(sourceControllerId)
                && hasActiveStaticEffect(gameData, target, ProtectionFromChosenPlayerEffect.class);
    }

    /** Whether the target has a granted "protection from creatures your opponents control" that the layers left it. */
    private boolean hasOpponentCreatureProtection(GameData gameData, Permanent target) {
        return computeStaticBonus(gameData, target).grantedEffects().stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.GrantProtectionFromOpponentCreaturesUntilEndOfTurnEffect.class::isInstance);
    }

    private boolean hasProtectionFromOpponentCreature(GameData gameData, Permanent target, Permanent source) {
        if (target == null || source == null || !isCreature(gameData, source)
                || !hasOpponentCreatureProtection(gameData, target)) {
            return false;
        }
        UUID targetControllerId = findPermanentController(gameData, target.getId());
        UUID sourceControllerId = findPermanentController(gameData, source.getId());
        return targetControllerId != null && sourceControllerId != null
                && !targetControllerId.equals(sourceControllerId);
    }

    private boolean hasProtectionFromOpponentCreature(GameData gameData, Permanent target, Card source,
                                                       UUID sourceControllerId) {
        if (target == null || source == null || sourceControllerId == null
                || (source.getType() != CardType.CREATURE
                && !source.getAdditionalTypes().contains(CardType.CREATURE))
                || !hasOpponentCreatureProtection(gameData, target)) {
            return false;
        }
        UUID targetControllerId = findPermanentController(gameData, target.getId());
        return targetControllerId != null && !targetControllerId.equals(sourceControllerId);
    }

    /**
     * Whether the target has protection from colorless: its own printed protection while it keeps it, or one granted
     * to it that the layers left it.
     */
    private boolean hasProtectionFromColorless(GameData gameData, Permanent target) {
        return Stream.concat(printedProtectionEffects(gameData, target).stream(),
                        computeStaticBonus(gameData, target).grantedEffects().stream())
                .anyMatch(effect -> effect instanceof ProtectionGrantingEffect protection
                        && protection.protectionFromColorless());
    }

    /**
     * Returns {@code true} if the target permanent has protection from the given color.
     * Checks the permanent's own {@link ProtectionGrantingEffect}, static bonuses from
     * other permanents, and temporary protection grants.
     */
    public boolean hasProtectionFrom(GameData gameData, Permanent target, CardColor sourceColor) {
        if (target == null) {
            return false;
        }
        StaticBonus bonus = computeStaticBonus(gameData, target);
        if (sourceColor == null) {
            return hasProtectionFromColorless(gameData, target);
        }
        if (bonus == StaticBonus.NONE && !target.isFaceDown()) {
            // No continuous effect touched the permanent: its own printed protection stands (a face-down one has
            // none, CR 708.2a).
            for (CardEffect effect : target.getCard().getStaticEffects()) {
                if (effect instanceof ProtectionGrantingEffect protection
                        && protection.protectionSubject() == null
                        && protectionColors(gameData, protection, target).contains(sourceColor)) {
                    return true;
                }
            }
        }
        // Layered layer-6 protection state: own printed protection (removable by a
        // later-timestamp "loses all abilities") plus protection grants.
        if (!bonus.removedProtectionColors().contains(sourceColor)
                && bonus.protectionColors().contains(sourceColor)) {
            return true;
        }
        // Protection granted by another permanent's static effect (e.g. Favor of the Mighty
        // via GrantEffectEffect(ProtectionFromColorsEffect, ...)).
        if (!bonus.removedProtectionColors().contains(sourceColor)) {
            for (CardEffect effect : bonus.grantedEffects()) {
                if (effect instanceof ProtectionGrantingEffect protection
                        && protectionColors(gameData, protection, target).contains(sourceColor)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The colours a "protection from [colours]" ability protects from (CR 702.16a), judged for {@code holder}, the
     * permanent whose ability it is (the creature itself, or the Aura or Equipment that gives it): the fixed colours,
     * the colour chosen for the holder, each colour among the permanents its controller controls as they are now
     * (layer 5, CR 613.1e), or each colour outside its controller's commanders' colour identity (CR 903.4). Every rules
     * reader of colour protection asks this, never {@link ProtectionGrantingEffect#protectionFromColors()} alone.
     */
    public Set<CardColor> protectionColors(GameData gameData, ProtectionGrantingEffect protection, Permanent holder) {
        UUID controllerId = gameData == null || holder == null ? null : findPermanentController(gameData, holder.getId());
        return protectionColors(gameData, protection, holder, controllerId,
                permanent -> LayerSystemService.activeStateFor(permanent.getId()) != null
                        ? colorsForStaticEvaluation(permanent)
                        : getEffectiveColors(gameData, permanent));
    }

    /**
     * {@link #protectionColors(GameData, ProtectionGrantingEffect, Permanent)} for a static handler or a
     * recursion-safe read: the colours of other permanents are read without re-entering the static-bonus assembly
     * ({@link #colorsForStaticEvaluation}), and {@code controllerId} is the ability's controller as the caller knows it.
     * With no game, a set that reads the board names no colour.
     */
    public Set<CardColor> protectionColorsForStaticEvaluation(GameData gameData, ProtectionGrantingEffect protection,
                                                              Permanent holder, UUID controllerId) {
        return protectionColors(gameData, protection, holder, controllerId, this::colorsForStaticEvaluation);
    }

    private Set<CardColor> protectionColors(GameData gameData, ProtectionGrantingEffect protection, Permanent holder,
                                            UUID controllerId, Function<Permanent, Set<CardColor>> colorsOf) {
        if (!(protection instanceof ProtectionFromColorsEffect colors)) {
            return protection.protectionFromColors();
        }
        return switch (colors.colorSet()) {
            case FIXED -> colors.colors();
            case CHOSEN -> holder == null || holder.getChosenColor() == null
                    ? Set.of() : Set.of(holder.getChosenColor());
            case AMONG_PERMANENTS_YOU_CONTROL -> {
                Set<CardColor> among = EnumSet.noneOf(CardColor.class);
                List<Permanent> battlefield = gameData == null || controllerId == null
                        ? null : gameData.playerBattlefields.get(controllerId);
                if (battlefield != null) {
                    for (Permanent permanent : battlefield) {
                        among.addAll(colorsOf.apply(permanent));
                    }
                }
                yield among;
            }
            case OUTSIDE_COMMANDER_IDENTITY -> colorsOutsideCommanderIdentity(gameData, controllerId);
        };
    }

    /**
     * Each colour not in the colour identity of {@code playerId}'s commanders, combined (CR 903.4): the designated
     * commanders, the cards in the command zone and the commanders on the battlefield they own. None when the player
     * has no commander (Commander's Plate ruling, 2020-11-10).
     */
    private static Set<CardColor> colorsOutsideCommanderIdentity(GameData gameData, UUID playerId) {
        if (gameData == null || playerId == null) {
            return Set.of();
        }
        List<Card> designatedCommanders = gameData.playerCommanders.getOrDefault(playerId, List.of());
        if (designatedCommanders.isEmpty()) {
            return Set.of();
        }
        Set<CardColor> identity = EnumSet.noneOf(CardColor.class);
        designatedCommanders.forEach(card -> identity.addAll(card.getColorIdentity()));
        for (Card card : gameData.playerCommandZones.getOrDefault(playerId, List.of())) {
            identity.addAll(card.getColorIdentity());
        }
        gameData.playerBattlefields.forEach((controllerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (!permanent.isCommander()) continue;
                UUID ownerId = permanent.getOriginalCard().getOwnerId();
                if (playerId.equals(ownerId) || (ownerId == null && playerId.equals(controllerId))) {
                    identity.addAll(permanent.getOriginalCard().getColorIdentity());
                }
            }
        });
        Set<CardColor> outside = EnumSet.allOf(CardColor.class);
        outside.removeAll(identity);
        return outside;
    }

    /**
     * Well-Laid Plans: returns {@code true} when a global shared-color creature damage prevention
     * effect is active and the source and recipient are different creatures with at least one
     * current color in common.
     */
    public boolean isDamageBetweenCreaturesOfSharedColorPrevented(GameData gameData,
                                                                   Permanent target,
                                                                   Permanent source) {
        return isDamageBetweenCreaturesOfSharedColorPrevented(gameData, target, source, false);
    }

    public boolean isDamageBetweenCreaturesOfSharedColorPrevented(GameData gameData,
                                                                   Permanent target,
                                                                   Permanent source,
                                                                   boolean isCombatDamage) {
        if (target == null || source == null || target.getId().equals(source.getId())) return false;
        if (!isDamagePreventable(gameData, isCombatDamage) || !isCreature(gameData, target)
                || !isCreature(gameData, source)) {
            return false;
        }
        Set<CardColor> targetColors = getEffectiveColors(gameData, target);
        // Ghostly Flame: a colour it covers is not a shared colour for damage purposes.
        Set<CardColor> sourceColors = getEffectiveColors(gameData, source).stream()
                .filter(color -> getDamageSourceColor(gameData, color) != null)
                .collect(java.util.stream.Collectors.toSet());
        if (targetColors.isEmpty() || sourceColors.stream().noneMatch(targetColors::contains)) return false;
        return gameData.anyPermanentMatches(permanent ->
                !permanent.isFaceDown()
                && !hasLostAllAbilities(gameData, permanent)
                && permanent.getCard().getStaticEffects().stream()
                        .anyMatch(SharedColorDamagePreventionEffect.class::isInstance));
    }

    /**
     * Returns {@code true} if the given permanent is a creature with the greatest mana value
     * among all creatures on the battlefield (across every player's battlefield). Ties allowed.
     * Used by Favor of the Mighty.
     */
    public boolean hasGreatestManaValueAmongAllCreatures(GameData gameData, Permanent permanent) {
        if (gameData == null || !isCreature(gameData, permanent)) {
            return false;
        }
        int greatest = -1;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent candidate : battlefield) {
                if (isCreature(gameData, candidate)) {
                    greatest = Math.max(greatest, getPermanentManaValue(candidate));
                }
            }
        }
        return getPermanentManaValue(permanent) == greatest;
    }

    /**
     * Returns {@code true} if the given permanent is a creature or planeswalker tied for greatest
     * mana value among creatures and planeswalkers controlled by its controller.
     */
    public boolean hasGreatestManaValueAmongControllerCreaturesOrPlaneswalkers(GameData gameData,
                                                                                 Permanent permanent) {
        if (gameData == null || permanent == null
                || (!isCreature(gameData, permanent) && !isPlaneswalker(gameData, permanent))) {
            return false;
        }
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        if (controllerId == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        int greatest = battlefield.stream()
                .filter(candidate -> isCreature(gameData, candidate) || isPlaneswalker(gameData, candidate))
                .mapToInt(this::getPermanentManaValue)
                .max()
                .orElse(-1);
        return getPermanentManaValue(permanent) == greatest;
    }

    /**
     * Returns {@code true} if the given permanent is an artifact with the greatest mana value
     * among all artifacts on the battlefield. Ties allowed.
     */
    public boolean hasGreatestManaValueAmongAllArtifacts(GameData gameData, Permanent permanent) {
        if (gameData == null || !isArtifact(gameData, permanent)) {
            return false;
        }
        int greatest = -1;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent candidate : battlefield) {
                if (isArtifact(gameData, candidate)) {
                    greatest = Math.max(greatest, getPermanentManaValue(candidate));
                }
            }
        }
        return getPermanentManaValue(permanent) == greatest;
    }

    /**
     * Returns {@code true} if the given permanent is a creature with the greatest effective power
     * among all creatures on the battlefield (across every player's battlefield). Ties allowed.
     * Used by Topple.
     */
    public boolean hasGreatestPowerAmongAllCreatures(GameData gameData, Permanent permanent) {
        if (gameData == null || !isCreature(gameData, permanent)) {
            return false;
        }
        int greatest = Integer.MIN_VALUE;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent candidate : battlefield) {
                if (isCreature(gameData, candidate)) {
                    greatest = Math.max(greatest, getEffectivePower(gameData, candidate));
                }
            }
        }
        return getEffectivePower(gameData, permanent) == greatest;
    }

    /**
     * Returns {@code true} when the given creature's supplied power is strictly greater than
     * every other creature's effective power on the battlefield. The supplied power also allows
     * callers to use a last-known power snapshot when the triggering creature has left play.
     */
    public boolean hasUniqueGreatestPowerAmongAllCreatures(GameData gameData, Permanent permanent,
                                                            int permanentPower) {
        if (gameData == null || !isCreature(gameData, permanent)) {
            return false;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent candidate : battlefield) {
                if (!candidate.getId().equals(permanent.getId())
                        && isCreature(gameData, candidate)
                        && getEffectivePower(gameData, candidate) >= permanentPower) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if the given permanent is a creature with the least power among all
     * creatures on the battlefield (across every player's battlefield). Ties allowed.
     * Used by Wretched Banquet.
     */
    public boolean hasLeastPowerAmongAllCreatures(GameData gameData, Permanent permanent) {
        if (gameData == null || !isCreature(gameData, permanent)) {
            return false;
        }
        int least = Integer.MAX_VALUE;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent candidate : battlefield) {
                if (isCreature(gameData, candidate)) {
                    least = Math.min(least, getEffectivePower(gameData, candidate));
                }
            }
        }
        return getEffectivePower(gameData, permanent) == least;
    }

    /**
     * Returns {@code true} if the given permanent is nonland and has the lowest mana value among
     * all nonland permanents on the battlefield. Ties allowed.
     */
    public boolean hasLowestManaValueAmongAllNonlandPermanents(GameData gameData, Permanent permanent) {
        if (gameData == null || isLand(gameData, permanent)) {
            return false;
        }
        int lowest = Integer.MAX_VALUE;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent candidate : battlefield) {
                if (!isLand(gameData, candidate)) {
                    lowest = Math.min(lowest, candidate.isFaceDown() ? 0 : candidate.getCard().getManaValue());
                }
            }
        }
        return (permanent.isFaceDown() ? 0 : permanent.getCard().getManaValue()) == lowest;
    }

    /**
     * Returns {@code true} if the target permanent has protection from any of the source
     * permanent's card types. Accounts for artifact status (including granted) and creature
     * status (including animation).
     */
    public boolean hasProtectionFromSourceCardTypes(GameData gameData, Permanent target, Permanent source) {
        if (target == null) {
            return false;
        }
        Set<CardColor> sourceColors = getEffectiveColors(gameData, source);
        if (hasProtectionFromEnemyColoredMulticolored(gameData, target, sourceColors)
                || hasProtectionFromMulticolored(gameData, target, sourceColors)
                || hasProtectionFromMonocolored(gameData, target, sourceColors)) {
            return true;
        }
        Set<CardType> protectedTypes = EnumSet.noneOf(CardType.class);
        for (CardEffect effect : target.getCard().getStaticEffects()) {
            if (effect instanceof ProtectionGrantingEffect protection
                    && !hasLostPrintedProtection(gameData, target)) {
                // Protection from everything (Progenitus): every source has a card type, so this is
                // the shared gate that stops all damage/combat/targeting/enchant from any source.
                if (protection.protectsFromEverything()) return true;
                protectedTypes.addAll(protection.protectionFromCardTypes());
            }
        }
        // Protection granted by another permanent's static effect (e.g. Spirit Mantle via
        // GrantEffectEffect(ProtectionFromCardTypesEffect, ENCHANTED_CREATURE)).
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (effect instanceof ProtectionGrantingEffect protection) {
                if (protection.protectsFromEverything()) return true;
                protectedTypes.addAll(protection.protectionFromCardTypes());
            }
        }
        if (protectedTypes.isEmpty()) return false;
        if (protectedTypes.contains(CardType.ARTIFACT) && isArtifact(gameData, source)) return true;
        if (protectedTypes.contains(CardType.CREATURE) && isCreature(gameData, source)) return true;
        if (protectedTypes.contains(source.getCard().getType())) return true;
        for (CardType type : source.getCard().getAdditionalTypes()) {
            if (protectedTypes.contains(type)) return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent's own printed protection covers any of the source
     * card's card types (the source's natural types). Granted protection is read from the layered
     * state by {@link #hasProtectionFromSourceCardTypes(GameData, Permanent, Card)}.
     */
    private boolean hasPrintedProtectionFromSourceCardTypes(GameData gameData, Permanent target, Card sourceCard) {
        if (target == null) {
            return false;
        }
        Set<CardType> protectedTypes = EnumSet.noneOf(CardType.class);
        List<CardEffect> printedEffects = new ArrayList<>();
        if (!target.isFaceDown() && !gameData.losesAllAbilitiesByEffect(target)) {
            printedEffects.addAll(target.getCard().getStaticEffects());
        }
        for (CardEffect effect : printedEffects) {
            if (effect instanceof ProtectionGrantingEffect protection) {
                // Protection from everything (Progenitus): every source has a card type, so this is
                // the shared gate that stops all damage/combat/targeting/enchant from any source.
                if (protection.protectsFromEverything()) return true;
                protectedTypes.addAll(protection.protectionFromCardTypes());
            }
        }
        if (protectedTypes.isEmpty()) return false;
        if (protectedTypes.contains(sourceCard.getType())) return true;
        for (CardType type : sourceCard.getAdditionalTypes()) {
            if (protectedTypes.contains(type)) return true;
        }
        return false;
    }

    /**
     * Card-source overload: the target's printed protection and the protection the layered state grants it.
     */
    public boolean hasProtectionFromSourceCardTypes(GameData gameData, Permanent target, Card sourceCard) {
        if (target == null) {
            return false;
        }
        if (hasProtectionFromEnemyColoredMulticolored(gameData, target, sourceCard)
                || hasProtectionFromMulticolored(gameData, target, sourceCard)
                || hasProtectionFromMonocolored(gameData, target, sourceCard)) {
            return true;
        }
        if (hasPrintedProtectionFromSourceCardTypes(gameData, target, sourceCard)
                && !hasLostPrintedProtection(gameData, target)) {
            return true;
        }
        if (hasProtectionFromSourceSupertypes(gameData, target, sourceCard)) {
            return true;
        }
        return computeStaticBonus(gameData, target).grantedEffects().stream()
                .filter(ProtectionGrantingEffect.class::isInstance)
                .map(ProtectionGrantingEffect.class::cast)
                .anyMatch(protection -> protection.protectsFromEverything()
                        || protection.protectionFromCardTypes().contains(sourceCard.getType())
                        || sourceCard.getAdditionalTypes().stream()
                                .anyMatch(protection.protectionFromCardTypes()::contains));
    }

    private boolean hasProtectionFromSourceSupertypes(GameData gameData, Permanent target, Card sourceCard) {
        Set<CardSupertype> supertypes = sourceCard.getSupertypes();
        if (supertypes == null || supertypes.isEmpty()) {
            return false;
        }
        List<CardEffect> effects = new ArrayList<>(computeStaticBonus(gameData, target).grantedEffects());
        if (!target.isFaceDown() && !hasLostPrintedProtection(gameData, target)) {
            effects.addAll(target.getCard().getStaticEffects());
        }
        return effects.stream().anyMatch(effect -> effect instanceof ProtectionFromPermanentsMatchingEffect protection
                && protection.predicate() instanceof PermanentHasSupertypePredicate supertype
                && supertypes.contains(supertype.supertype()));
    }

    public boolean hasProtectionFromMulticolored(GameData gameData, Permanent target, Card sourceCard) {
        if (target == null) {
            return false;
        }
        if (sourceCard == null || getEffectiveCardColors(gameData, sourceCard).size() < 2) {
            return false;
        }
        return hasProtectionFromMulticoloredEffects(printedProtectionEffects(gameData, target))
                || hasProtectionFromMulticoloredEffects(computeStaticBonus(gameData, target).grantedEffects());
    }

    public boolean hasProtectionFromEnemyColoredMulticolored(GameData gameData, Permanent target,
                                                              Card sourceCard) {
        if (target == null) {
            return false;
        }
        if (sourceCard == null
                || !isEnemyColoredMulticolored(getEffectiveCardColors(gameData, sourceCard))) {
            return false;
        }
        return hasProtectionFromEnemyColoredMulticoloredEffects(printedProtectionEffects(gameData, target))
                || hasProtectionFromEnemyColoredMulticoloredEffects(
                        computeStaticBonus(gameData, target).grantedEffects());
    }

    public boolean hasProtectionFromMonocolored(GameData gameData, Permanent target, Card sourceCard) {
        if (target == null) {
            return false;
        }
        if (sourceCard == null || getEffectiveCardColors(gameData, sourceCard).size() != 1) {
            return false;
        }
        return hasProtectionFromMonocoloredEffects(printedProtectionEffects(gameData, target))
                || hasProtectionFromMonocoloredEffects(computeStaticBonus(gameData, target).grantedEffects());
    }

    private boolean hasProtectionFromMulticolored(GameData gameData, Permanent target,
                                                   Set<CardColor> sourceColors) {
        return sourceColors.size() >= 2
                && (hasProtectionFromMulticoloredEffects(printedProtectionEffects(gameData, target))
                || hasProtectionFromMulticoloredEffects(computeStaticBonus(gameData, target).grantedEffects()));
    }

    private boolean hasProtectionFromEnemyColoredMulticolored(GameData gameData, Permanent target,
                                                               Set<CardColor> sourceColors) {
        return isEnemyColoredMulticolored(sourceColors)
                && (hasProtectionFromEnemyColoredMulticoloredEffects(printedProtectionEffects(gameData, target))
                || hasProtectionFromEnemyColoredMulticoloredEffects(
                        computeStaticBonus(gameData, target).grantedEffects()));
    }

    private boolean hasProtectionFromMonocolored(GameData gameData, Permanent target,
                                                  Set<CardColor> sourceColors) {
        return sourceColors.size() == 1
                && (hasProtectionFromMonocoloredEffects(printedProtectionEffects(gameData, target))
                || hasProtectionFromMonocoloredEffects(computeStaticBonus(gameData, target).grantedEffects()));
    }

    /**
     * The target's printed static protection effects, which stop applying while it has lost its
     * printed abilities (Turn to Frog on Enemy of the Guildpact) or its protection (Shay Cormac).
     * Protection granted by other permanents and effects is read separately from the computed static bonus.
     */
    private List<CardEffect> printedProtectionEffects(GameData gameData, Permanent target) {
        return hasLostPrintedProtection(gameData, target)
                ? List.of()
                : target.getCard().getStaticEffects();
    }

    private boolean hasProtectionFromMulticoloredEffects(Iterable<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof ProtectionGrantingEffect protection
                    && protection.protectionFromMulticolored()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasProtectionFromEnemyColoredMulticoloredEffects(Iterable<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof ProtectionGrantingEffect protection
                    && protection.protectionFromEnemyColoredMulticolored()) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnemyColoredMulticolored(Set<CardColor> colors) {
        if (colors == null || colors.size() != 2) {
            return false;
        }
        return (colors.contains(CardColor.WHITE) && colors.contains(CardColor.BLACK))
                || (colors.contains(CardColor.BLUE) && colors.contains(CardColor.RED))
                || (colors.contains(CardColor.BLACK) && colors.contains(CardColor.GREEN))
                || (colors.contains(CardColor.RED) && colors.contains(CardColor.WHITE))
                || (colors.contains(CardColor.GREEN) && colors.contains(CardColor.BLUE));
    }

    private boolean hasProtectionFromMonocoloredEffects(Iterable<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof ProtectionGrantingEffect protection
                    && protection.protectionFromMonocolored()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from any of the source
     * permanent's subtypes. Checks the permanent's own card subtypes, transient subtypes,
     * granted subtypes, and Changeling keyword (which counts as all creature subtypes).
     */
    public boolean hasProtectionFromSourceSubtypes(GameData gameData, Permanent target, Permanent source) {
        if (target == null) {
            return false;
        }
        Set<CardSubtype> protectedSubtypes = EnumSet.noneOf(CardSubtype.class);
        Set<CardSubtype> creatureOnlySubtypes = EnumSet.noneOf(CardSubtype.class);
        // CR 708.2: a face-down permanent has no abilities, so its printed protection does not apply.
        if (!target.isFaceDown() && !hasLostPrintedProtection(gameData, target)) {
            for (CardEffect effect : target.getCard().getStaticEffects()) {
                collectProtectedSubtypes(effect, protectedSubtypes, creatureOnlySubtypes);
            }
        }
        // Subtype protection granted by another permanent's static effect (e.g. Riders of Gavony
        // via GrantProtectionFromChosenTypeToOwnCreaturesEffect).
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            collectProtectedSubtypes(effect, protectedSubtypes, creatureOnlySubtypes);
        }
        if (!creatureOnlySubtypes.isEmpty() && isCreature(gameData, source)) {
            protectedSubtypes.addAll(creatureOnlySubtypes);
        }
        if (protectedSubtypes.isEmpty()) return false;
        // CR 708.2a: a face-down permanent has no printed subtypes, only those its face-down effect lists and those
        // effects give it, which the layered subtypes are.
        for (CardSubtype subtype : getEffectiveSubtypes(gameData, source)) {
            if (protectedSubtypes.contains(subtype)) return true;
        }
        if (hasKeyword(gameData, source, Keyword.CHANGELING)
                && protectedSubtypes.stream().anyMatch(this::isCreatureSubtype)) {
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from any of the source
     * card's subtypes. This overload is used for spells on the stack.
     */
    public boolean hasProtectionFromSourceSubtypes(Permanent target, Card sourceCard) {
        if (target == null) {
            return false;
        }
        Set<CardSubtype> protectedSubtypes = EnumSet.noneOf(CardSubtype.class);
        Set<CardSubtype> creatureOnlySubtypes = EnumSet.noneOf(CardSubtype.class);
        for (CardEffect effect : target.getCard().getStaticEffects()) {
            collectProtectedSubtypes(effect, protectedSubtypes, creatureOnlySubtypes);
        }
        if (!creatureOnlySubtypes.isEmpty()
                && (sourceCard.getType() == CardType.CREATURE
                || sourceCard.getAdditionalTypes().contains(CardType.CREATURE))) {
            protectedSubtypes.addAll(creatureOnlySubtypes);
        }
        if (protectedSubtypes.isEmpty()) return false;
        for (CardSubtype subtype : sourceCard.getSubtypes()) {
            if (protectedSubtypes.contains(subtype)) return true;
        }
        if (sourceCard.hasKeyword(Keyword.CHANGELING)
                && protectedSubtypes.stream().anyMatch(this::isCreatureSubtype)) {
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from any of the source card's
     * subtypes. Used for sources with no battlefield permanent (a dead Zombie's death trigger, a spell
     * on the stack). Unlike the card-only overload, this also sees subtype protection granted by
     * another permanent's static effect (e.g. Diregraf Escort's pair grant).
     */
    public boolean hasProtectionFromSourceSubtypes(GameData gameData, Permanent target, Card sourceCard) {
        if (target == null) {
            return false;
        }
        Set<CardSubtype> protectedSubtypes = EnumSet.noneOf(CardSubtype.class);
        Set<CardSubtype> creatureOnlySubtypes = EnumSet.noneOf(CardSubtype.class);
        // CR 708.2: a face-down permanent has no abilities, so its printed protection does not apply.
        if (!target.isFaceDown() && !hasLostPrintedProtection(gameData, target)) {
            for (CardEffect effect : target.getCard().getStaticEffects()) {
                collectProtectedSubtypes(effect, protectedSubtypes, creatureOnlySubtypes);
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            collectProtectedSubtypes(effect, protectedSubtypes, creatureOnlySubtypes);
        }
        if (!creatureOnlySubtypes.isEmpty()
                && (sourceCard.getType() == CardType.CREATURE
                || sourceCard.getAdditionalTypes().contains(CardType.CREATURE))) {
            protectedSubtypes.addAll(creatureOnlySubtypes);
        }
        if (protectedSubtypes.isEmpty()) return false;
        for (CardSubtype subtype : sourceCard.getSubtypes()) {
            if (protectedSubtypes.contains(subtype)) return true;
        }
        return sourceCard.hasKeyword(Keyword.CHANGELING)
                && protectedSubtypes.stream().anyMatch(this::isCreatureSubtype);
    }

    /**
     * Splits one effect's subtype protection into the unconditional set and the
     * "â€¦creatures only" set, so a "protection from [type] creatures" grant (Riders of Gavony)
     * does not also stop a noncreature source that merely carries the type.
     */
    private void collectProtectedSubtypes(CardEffect effect, Set<CardSubtype> unconditional,
                                          Set<CardSubtype> creatureSourcesOnly) {
        if (effect instanceof ProtectionGrantingEffect protection) {
            if (protection.subtypeProtectionRequiresCreatureSource()) {
                creatureSourcesOnly.addAll(protection.protectionFromSubtypes());
            } else {
                unconditional.addAll(protection.protectionFromSubtypes());
            }
        }
    }

    /**
     * Returns {@code true} if the target permanent has protection from non-[subtype] creatures
     * and the source permanent is a creature that lacks that subtype.
     */
    private boolean hasProtectionFromNonSubtypeCreatures(GameData gameData, Permanent target, Permanent source) {
        if (target == null) {
            return false;
        }
        Set<CardSubtype> protectedFrom = getNonSubtypeCreatureProtection(gameData, target);
        if (protectedFrom.isEmpty()) return false;
        if (!isCreature(gameData, source)) return false;
        for (CardSubtype subtype : protectedFrom) {
            if (!permanentHasSubtype(source, subtype)
                    && !(isCreatureSubtype(subtype) && hasKeyword(gameData, source, Keyword.CHANGELING))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from non-[subtype] creatures
     * and the source card (on the stack) is a creature card that lacks that subtype.
     */
    private boolean hasProtectionFromNonSubtypeCreatures(GameData gameData, Permanent target, Card sourceCard) {
        if (target == null) {
            return false;
        }
        Set<CardSubtype> protectedFrom = getNonSubtypeCreatureProtection(gameData, target);
        if (protectedFrom.isEmpty()) return false;
        if (sourceCard.getType() != CardType.CREATURE
                && !sourceCard.getAdditionalTypes().contains(CardType.CREATURE)) return false;
        for (CardSubtype subtype : protectedFrom) {
            if (!sourceCard.getSubtypes().contains(subtype)
                    && !(isCreatureSubtype(subtype) && sourceCard.hasKeyword(Keyword.CHANGELING))) {
                return true;
            }
        }
        return false;
    }

    private Set<CardSubtype> getNonSubtypeCreatureProtection(GameData gameData, Permanent target) {
        Set<CardSubtype> protectedFrom = EnumSet.noneOf(CardSubtype.class);
        if (!computeStaticBonus(gameData, target).losesAllAbilities() && !hasLostPrintedProtection(gameData, target)) {
            for (CardEffect effect : target.getCard().getStaticEffects()) {
                if (effect instanceof ProtectionFromNonSubtypeCreaturesEffect protection) {
                    protectedFrom.add(protection.subtype());
                }
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (effect instanceof ProtectionFromNonSubtypeCreaturesEffect protection) {
                protectedFrom.add(protection.subtype());
            }
        }
        return protectedFrom;
    }

    /**
     * Returns {@code true} if the target permanent has protection from a mana-value threshold
     * and the source's mana value meets it (e.g. Mistmeadow Skulk or Reaver Titan).
     */
    private boolean hasProtectionFromSourceManaValue(GameData gameData, Permanent target, Card sourceCard) {
        if (target == null) {
            return false;
        }
        sourceCard = withSpellManaValue(gameData, sourceCard);
        int chosenNumber = target.getChosenNumber();
        var chosenParity = target.getChosenManaValueParity();
        if (!computeStaticBonus(gameData, target).losesAllAbilities() && !hasLostPrintedProtection(gameData, target)
                && hasProtectionFromSourceManaValue(
                target.getCard().getStaticEffects(), sourceCard, chosenNumber, chosenParity)) {
            return true;
        }
        return hasProtectionFromSourceManaValue(
                computeStaticBonus(gameData, target).grantedEffects(), sourceCard, chosenNumber, chosenParity);
    }

    /** A spell being cast, with the value of X it announced: the mana value its targets are checked against. */
    private record AnnouncedSpellX(UUID cardId, int xValue) {}

    private static final ThreadLocal<AnnouncedSpellX> ANNOUNCED_SPELL_X = new ThreadLocal<>();

    /**
     * Records the X announced for {@code cardId} while it is cast, so protection from mana values judges the
     * spell by its mana value with X included (CR 202.3e) before it is on the stack. Always pair with
     * {@link #endSpellCastX()}.
     */
    public static void beginSpellCastX(UUID cardId, int xValue) {
        ANNOUNCED_SPELL_X.set(new AnnouncedSpellX(cardId, xValue));
    }

    public static void endSpellCastX() {
        ANNOUNCED_SPELL_X.remove();
    }

    /**
     * The card to judge protection from mana values against: a spell on the stack, or one being cast, counts
     * the X it announced in its mana value (CR 202.3e).
     */
    private Card withSpellManaValue(GameData gameData, Card sourceCard) {
        if (sourceCard == null || sourceCard.getParsedManaCost() == null
                || sourceCard.getParsedManaCost().getXSymbolCount() == 0) {
            return sourceCard;
        }
        int xValue = 0;
        AnnouncedSpellX announced = ANNOUNCED_SPELL_X.get();
        if (announced != null && announced.cardId().equals(sourceCard.getId())) {
            xValue = announced.xValue();
        } else {
            for (StackEntry entry : gameData.stack) {
                if (entry.getCard() != null && entry.getCard().getId().equals(sourceCard.getId())
                        && isSpellStackEntry(entry.getEntryType())) {
                    xValue = entry.getXValue();
                    break;
                }
            }
        }
        if (xValue == 0) {
            return sourceCard;
        }
        Card withX = sourceCard.createRuntimeCopy();
        withX.setCombinedManaValue(StackEntry.spellManaValue(sourceCard, xValue));
        return withX;
    }

    private boolean hasProtectionFromSourceManaValue(Iterable<CardEffect> effects, Card sourceCard,
                                                      int chosenNumber, ManaValueParity chosenParity) {
        for (CardEffect effect : effects) {
            if (effect instanceof ProtectionGrantingEffect protection) {
                ManaValueParity parity = protection.protectionFromFixedManaValueParity() != null
                        ? protection.protectionFromFixedManaValueParity() : chosenParity;
                if (protection.protectionFromManaValueParity()
                        && parity != null
                        && parity.matches(sourceCard.getManaValue())) {
                    return true;
                }
                if (protection.protectionFromManaValueAtLeast().isPresent()
                        && sourceCard.getManaValue() >= protection.protectionFromManaValueAtLeast().getAsInt()) {
                    return true;
                }
                if (protection.protectionFromManaValueAtMost().isPresent()
                        && sourceCard.getManaValue() <= protection.protectionFromManaValueAtMost().getAsInt()) {
                    return true;
                }
                int number = protection.protectionFromManaValuesOtherThanFixed() != null
                        ? protection.protectionFromManaValuesOtherThanFixed() : chosenNumber;
                if (protection.protectionFromManaValuesOtherThanChosen().contains(number)
                        && sourceCard.getManaValue() != number) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from the source permanent,
     * checking color-based, card-type-based, subtype-based, and non-subtype-creature protection.
     */
    public boolean hasProtectionFromSource(GameData gameData, Permanent target, Permanent source) {
        return hasProtectionFromSource(gameData, target, source, getEffectiveColors(gameData, source));
    }

    /** Tests independent protection while honoring an Aura's exception for its own grant. */
    public boolean hasProtectionFromAuraIgnoringOwnGrant(GameData gameData, Permanent target, Permanent aura) {
        Set<CardColor> sourceColors = getEffectiveColors(gameData, aura);
        LayerSystemService.Pass pass = layerSystemService.beginPassWithoutProtectionFrom(gameData, aura.getId());
        try {
            return hasProtectionFromSource(gameData, target, aura, sourceColors);
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /**
     * Variant taking the source's precomputed effective colors, for sweeps that already
     * snapshotted them (see {@code BlockLegalityContext} in {@code service.combat.block}).
     */
    public boolean hasProtectionFromSource(GameData gameData, Permanent target, Permanent source,
                                           Set<CardColor> sourceColors) {
        if (target != null && source != null && (hasProtectionFromOpponentCreature(gameData, target, source)
                || hasProtectionFromOpponents(gameData, target,
                findPermanentController(gameData, source.getId())))) return true;
        if (target == null) {
            return false;
        }
        if (hasProtectionFromRingBearer(gameData, target, source)) {
            return true;
        }
        if (hasProtectionFromOpponentCreature(gameData, target, source)) {
            return true;
        }
        if (hasProtectionFromOpponents(gameData, target,
                findPermanentController(gameData, source.getId()))) {
            return true;
        }
        // Layer-5 aware: protection applies if the source has ANY protected color.
        for (CardColor sourceColor : sourceColors) {
            if (hasProtectionFrom(gameData, target, sourceColor)) {
                return true;
            }
        }
        return (sourceColors.isEmpty() && hasProtectionFromColorless(gameData, target))
                || hasProtectionFromEnemyColoredMulticolored(gameData, target, sourceColors)
                || hasProtectionFromMonocolored(gameData, target, sourceColors)
                || hasProtectionFromSourceCardTypes(gameData, target, source)
                || hasProtectionFromSourceSubtypes(gameData, target, source)
                || hasProtectionFromModifiedCreatures(gameData, target, source)
                || hasProtectionFromNonSubtypeCreatures(gameData, target, source)
                || hasProtectionFromSourceManaValue(gameData, target, source.getCard())
                || hasProtectionFromMatchingPermanent(gameData, target, source);
    }

    /**
     * Protection from permanents matching a predicate (Ronom Hulk, Tsabo Tavoc): the target's own ability, unless it
     * lost it, and one an effect granted it that the layers left it (Concerted Effort).
     */
    private boolean hasProtectionFromMatchingPermanent(GameData gameData, Permanent target, Permanent source) {
        if (source == null) {
            return false;
        }
        List<CardEffect> effects = new ArrayList<>(computeStaticBonus(gameData, target).grantedEffects());
        if (!target.isFaceDown() && !gameData.losesAllAbilitiesByEffect(target)
                && !computeStaticBonus(gameData, target).losesAllAbilities()
                && !hasLostPrintedProtection(gameData, target)) {
            effects.addAll(getActiveStaticEffects(gameData, target));
        }
        FilterContext context = FilterContext.of(gameData)
                .withSourceControllerId(findPermanentController(gameData, target.getId()));
        return effects.stream().anyMatch(effect -> effect instanceof ProtectionFromPermanentsMatchingEffect protection
                && predicateEvaluationService.matchesPermanentPredicate(source, protection.predicate(), context));
    }

    private boolean hasProtectionFromRingBearer(GameData gameData, Permanent target, Permanent source) {
        if (target == null || source == null || !isRingBearer(gameData, source)) {
            return false;
        }
        StaticBonus bonus = computeStaticBonus(gameData, target);
        if (bonus.losesAllAbilities()) {
            return false;
        }
        return printedProtectionEffects(gameData, target).stream()
                .filter(ProtectionGrantingEffect.class::isInstance)
                .map(ProtectionGrantingEffect.class::cast)
                .anyMatch(ProtectionGrantingEffect::protectionFromRingBearers)
                || bonus.grantedEffects().stream()
                .filter(ProtectionGrantingEffect.class::isInstance)
                .map(ProtectionGrantingEffect.class::cast)
                .anyMatch(ProtectionGrantingEffect::protectionFromRingBearers);
    }

    private boolean hasProtectionFromModifiedCreatures(GameData gameData, Permanent target, Permanent source) {
        if (!isCreature(gameData, source) || !hasPowerToughnessOrAbilitiesDifferentFromPrinted(gameData, source)) {
            return false;
        }
        return hasProtectionFromModifiedCreatures(printedProtectionEffects(gameData, target))
                || hasProtectionFromModifiedCreatures(computeStaticBonus(gameData, target).grantedEffects());
    }

    /**
     * Louvaq's definition of a modified creature: its power, toughness, or abilities differ from
     * the printed ones. Counters that change none of those do not count.
     */
    private boolean hasPowerToughnessOrAbilitiesDifferentFromPrinted(GameData gameData, Permanent permanent) {
        Card card = permanent.getCard();
        if (card.getPower() != null && getEffectivePower(gameData, permanent) != card.getPower()) {
            return true;
        }
        if (card.getToughness() != null && getEffectiveToughness(gameData, permanent) != card.getToughness()) {
            return true;
        }
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (bonus.losesAllAbilities() || gameData.losesAllAbilitiesByEffect(permanent)
                || !bonus.removedKeywords().isEmpty()
                || !bonus.grantedEffects().isEmpty() || !bonus.grantedActivatedAbilities().isEmpty()) {
            return true;
        }
        return !card.getKeywords().containsAll(bonus.keywords());
    }

    private boolean hasProtectionFromModifiedCreatures(Iterable<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof ProtectionGrantingEffect protection
                    && protection.protectionFromModifiedCreatures()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from the source card
     * (a spell on the stack), checking color-based, card-type-based, subtype-based,
     * and non-subtype-creature protection.
     */
    /**
     * Damage-path variant of {@link #hasProtectionFromSource(GameData, Permanent, Permanent)}: the
     * source's colours are first passed through {@link #getDamageSourceColor} so a Ghostly Flame
     * effect can make it a colourless source of damage. Non-colour protection is unaffected.
     */
    public boolean hasProtectionFromDamageSource(GameData gameData, Permanent target, Permanent source) {
        return hasProtectionFromSource(gameData, target, source,
                getDamageSourceColors(gameData, getEffectiveColors(gameData, source)));
    }

    /** Damage-path variant of {@link #hasProtectionFromSource(GameData, Permanent, Card)}. */
    public boolean hasProtectionFromDamageSource(GameData gameData, Permanent target, Card sourceCard) {
        return hasProtectionFromDamageSource(gameData, target, sourceCard, null);
    }

    public boolean hasProtectionFromDamageSource(GameData gameData, Permanent target, Card sourceCard,
                                                 UUID sourceControllerId) {
        if (target != null && sourceCard != null && (hasProtectionFromOpponentCreature(gameData, target, sourceCard,
                sourceControllerId != null ? sourceControllerId : sourceCard.getOwnerId())
                || hasProtectionFromOpponents(gameData, target,
                sourceControllerId != null ? sourceControllerId : sourceCard.getOwnerId()))) return true;
        if (target == null) {
            return false;
        }
        UUID protectionSourcePlayerId = sourceControllerId != null
                ? sourceControllerId
                : sourceCard.getOwnerId();
        Set<CardColor> sourceColors = getDamageSourceColors(
                gameData, getEffectiveCardColors(gameData, sourceCard));
        return hasProtectionFromOpponentCreature(gameData, target, sourceCard, protectionSourcePlayerId)
                || hasProtectionFromOpponents(gameData, target, protectionSourcePlayerId)
                || sourceColors.stream().anyMatch(color -> hasProtectionFrom(gameData, target, color))
                || (sourceColors.isEmpty() && hasProtectionFromColorless(gameData, target))
                || hasProtectionFromEnemyColoredMulticolored(gameData, target, sourceColors)
                || hasProtectionFromMonocolored(gameData, target, sourceColors)
                // A spell Ghostly Flame makes a colorless damage source isn't a colored spell for damage.
                || (!sourceColors.isEmpty() && isSpellOnStack(gameData, sourceCard)
                && hasProtectionFromColoredSpells(gameData, target))
                || hasProtectionFromSourceCardTypes(gameData, target, sourceCard)
                || hasProtectionFromSourceSubtypes(gameData, target, sourceCard)
                || hasProtectionFromNonSubtypeCreatures(gameData, target, sourceCard)
                || hasProtectionFromSourceManaValue(gameData, target, sourceCard)
                || hasProtectionFromMatchingPermanent(gameData, target, findPermanentByCardId(gameData, sourceCard.getId()));
    }

    public boolean hasProtectionFromSource(GameData gameData, Permanent target, Card sourceCard) {
        return hasProtectionFromSource(gameData, target, sourceCard, null);
    }

    public boolean hasProtectionFromSource(GameData gameData, Permanent target, Card sourceCard,
                                           UUID sourceControllerId) {
        if (target != null && sourceCard != null && (hasProtectionFromOpponentCreature(gameData, target, sourceCard,
                sourceControllerId != null ? sourceControllerId : sourceCard.getOwnerId())
                || hasProtectionFromOpponents(gameData, target,
                sourceControllerId != null ? sourceControllerId : sourceCard.getOwnerId()))) return true;
        if (target == null) {
            return false;
        }
        UUID protectionSourcePlayerId = sourceControllerId != null
                ? sourceControllerId
                : sourceCard.getOwnerId();
        Set<CardColor> sourceColors = getEffectiveCardColors(gameData, sourceCard);
        return hasProtectionFromOpponentCreature(gameData, target, sourceCard, protectionSourcePlayerId)
                || hasProtectionFromOpponents(gameData, target, protectionSourcePlayerId)
                || sourceColors.stream().anyMatch(color -> hasProtectionFrom(gameData, target, color))
                || (sourceColors.isEmpty() && hasProtectionFromColorless(gameData, target))
                || hasProtectionFromEnemyColoredMulticolored(gameData, target, sourceColors)
                || hasProtectionFromMonocolored(gameData, target, sourceColors)
                || hasProtectionFromColoredSpellSource(gameData, target, sourceCard)
                || hasProtectionFromSourceCardTypes(gameData, target, sourceCard)
                || hasProtectionFromSourceSubtypes(gameData, target, sourceCard)
                || hasProtectionFromNonSubtypeCreatures(gameData, target, sourceCard)
                || hasProtectionFromSourceManaValue(gameData, target, sourceCard)
                || hasProtectionFromMatchingPermanent(gameData, target, findPermanentByCardId(gameData, sourceCard.getId()));
    }

    public boolean hasProtectionFromColoredSpellSource(GameData gameData, Permanent target, Card sourceCard) {
        if (target == null) {
            return false;
        }
        if (sourceCard == null || !isSpellOnStack(gameData, sourceCard)
                || getEffectiveCardColors(gameData, sourceCard).isEmpty()) {
            return false;
        }
        return hasProtectionFromColoredSpells(gameData, target);
    }

    /**
     * Returns whether a colored spell that is resolving (and therefore no longer on the stack) is a source
     * that the target has protection from as a colored spell. Its damage is prevented per CR 702.16e.
     */
    public boolean hasProtectionFromResolvingColoredSpell(GameData gameData, Permanent target, Card spellCard) {
        if (target == null || spellCard == null || getEffectiveCardColors(gameData, spellCard).stream()
                .noneMatch(color -> getDamageSourceColor(gameData, color) != null)) {
            return false;
        }
        return hasProtectionFromColoredSpells(gameData, target);
    }

    public boolean hasProtectionFromColoredSpells(GameData gameData, Permanent target) {
        if (target == null) {
            return false;
        }
        return hasProtectionFromColoredSpellEffect(printedProtectionEffects(gameData, target))
                || hasProtectionFromColoredSpellEffect(computeStaticBonus(gameData, target).grantedEffects());
    }

    private boolean hasProtectionFromColoredSpellEffect(Iterable<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof ProtectionGrantingEffect protection
                    && protection.protectionFromColoredSpells()) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpellOnStack(GameData gameData, Card sourceCard) {
        return gameData.stack.stream().anyMatch(entry -> entry.getCard() != null
                && entry.getCard().getId().equals(sourceCard.getId())
                && entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY);
    }

    /**
     * Returns {@code true} if the source of a stack entry has the given keyword. Uses the
     * explicit source permanent if provided; otherwise looks up the source from the entry's
     * {@code sourcePermanentId}.
     *
     * @param explicitSource an already-resolved source permanent, or {@code null} to look up
     *                       from the entry
     */
    /**
     * Returns {@code true} when the source of a damage event is currently a creature: the explicit
     * source permanent if given, else the stack entry's source permanent. A spell source (no source
     * permanent on the battlefield) is never a creature. Used by Uncle Istvan-style "prevent all damage
     * that would be dealt to this creature by creatures" effects.
     */
    public boolean isDamageSourceCreature(GameData gameData, StackEntry entry, Permanent explicitSource) {
        Permanent source = explicitSource;
        if (source == null && entry != null && entry.getSourcePermanentId() != null) {
            source = findPermanentById(gameData, entry.getSourcePermanentId());
        }
        return source != null && isCreature(gameData, source);
    }

    /**
     * Returns {@code true} when the source of a damage event is currently an artifact. A source
     * permanent uses its effective types; a spell source falls back to the artifact type of its card.
     */
    public boolean isDamageSourceArtifact(GameData gameData, StackEntry entry, Permanent explicitSource) {
        if (entry != null && entry.getDamageSourceCard() != null) {
            return entry.getDamageSourceCard().hasType(CardType.ARTIFACT);
        }
        Permanent source = explicitSource;
        if (source == null && entry != null && entry.getSourcePermanentId() != null) {
            source = findPermanentById(gameData, entry.getSourcePermanentId());
        }
        if (source != null) return isArtifact(gameData, source);
        return entry != null
                && entry.getEffectiveDamageSourceCard() != null
                && entry.getEffectiveDamageSourceCard().hasType(CardType.ARTIFACT);
    }

    /**
     * Evaluates a source predicate against current battlefield characteristics, or the source's
     * last-known characteristics when it has left. Card sources use their intrinsic characteristics.
     */
    public boolean matchesDamageSourcePredicate(GameData gameData, StackEntry entry, Card sourceCard,
                                                Permanent explicitSource, PermanentPredicate predicate) {
        return matchesDamageSourcePredicate(gameData, entry, sourceCard, explicitSource, predicate,
                FilterContext.of(gameData), null);
    }

    /**
     * {@link #matchesDamageSourcePredicate(GameData, StackEntry, Card, Permanent, PermanentPredicate)} for a filter
     * that names the source of the effect judging it ("creatures it's blocking"): {@code onBattlefield} judges a source
     * on the battlefield, {@code onCard} (intrinsic values when {@code null}) its last-known state or a spell's card.
     */
    public boolean matchesDamageSourcePredicate(GameData gameData, StackEntry entry, Card sourceCard,
                                                Permanent explicitSource, PermanentPredicate predicate,
                                                FilterContext onBattlefield, FilterContext onCard) {
        if (predicate == null) return true;
        Permanent source = explicitSource;
        if (source == null && entry != null && entry.getSourcePermanentId() != null) {
            source = findPermanentById(gameData, entry.getSourcePermanentId());
        }
        if (source != null) {
            return predicateEvaluationService.matchesPermanentPredicate(source, predicate, onBattlefield);
        }
        Permanent lastKnownSource = entry == null ? null : entry.getSourcePermanentSnapshot();
        if (lastKnownSource == null) {
            Card effectiveSourceCard = sourceCard != null
                    ? sourceCard : entry == null ? null : entry.getEffectiveDamageSourceCard();
            if (effectiveSourceCard != null) {
                lastKnownSource = new Permanent(effectiveSourceCard);
            }
        }
        return lastKnownSource != null
                && predicateEvaluationService.matchesPermanentPredicate(lastKnownSource, predicate, onCard);
    }

    /**
     * Uncle Istvan: whether damage from a creature source to {@code target} is prevented by the target's
     * own {@link PreventDamageToSelfFromCreaturesEffect}. True only while damage is preventable, the target
     * carries the effect, and the damage source is a creature. Combat damage is handled directly in
     * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#creatureDamageSteps()}
     * (its source is always a creature); this covers the noncombat path where the source is known.
     */
    public boolean isCreatureSourceDamageToSelfPrevented(GameData gameData, Permanent target, StackEntry entry,
                                                         Permanent explicitSource) {
        return isCreatureSourceDamageToSelfPrevented(gameData, target, entry, explicitSource, false);
    }

    public boolean isCreatureSourceDamageToSelfPrevented(GameData gameData, Permanent target, StackEntry entry,
                                                         Permanent explicitSource, boolean isCombatDamage) {
        if (!isDamagePreventable(gameData, isCombatDamage)) return false;
        boolean preventsAllCreatureDamage = target.getCard().getStaticEffects().stream()
                .anyMatch(PreventDamageToSelfFromCreaturesEffect.class::isInstance)
                && hasActiveStaticEffect(gameData, target, PreventDamageToSelfFromCreaturesEffect.class);
        if (!preventsAllCreatureDamage) return false;
        Permanent source = explicitSource;
        if (source == null && entry != null && entry.getSourcePermanentId() != null) {
            source = findPermanentById(gameData, entry.getSourcePermanentId());
            if (source == null) {
                source = entry.getSourcePermanentSnapshot();
                if (source == null && entry.getEffectiveDamageSourceCard() != null) {
                    source = new Permanent(entry.getEffectiveDamageSourceCard());
                }
            }
        }
        Permanent sourcePermanent = source;
        if (sourcePermanent == null && isCombatDamage
                && target.getCard().getStaticEffects().stream()
                .filter(PreventDamageToSelfFromCreaturesEffect.class::isInstance)
                .map(PreventDamageToSelfFromCreaturesEffect.class::cast)
                .anyMatch(effect -> !effect.combatOnly() && effect.sourcePredicate() == null)) {
            return true;
        }
        return sourcePermanent != null && isCreature(gameData, sourcePermanent)
                && target.getCard().getStaticEffects().stream()
                .filter(PreventDamageToSelfFromCreaturesEffect.class::isInstance)
                .map(PreventDamageToSelfFromCreaturesEffect.class::cast)
                .anyMatch(effect -> (!effect.combatOnly() || isCombatDamage)
                        && (effect.sourcePredicate() == null
                        || predicateEvaluationService.matchesPermanentPredicate(
                                gameData, sourcePermanent, effect.sourcePredicate())));
    }

    /**
     * Returns whether damage from a Desert is prevented by an attacking Camel for itself or one of
     * the other attacking creatures in that Camel's band.
     */
    public boolean isDamageFromDesertsToCamelOrBandedCreaturePrevented(
            GameData gameData, Permanent target, StackEntry entry, Permanent explicitSource,
            boolean isCombatDamage) {
        if (!isDamagePreventable(gameData, isCombatDamage) || target == null
                || !isCreature(gameData, target)) {
            return false;
        }

        Permanent source = explicitSource;
        if (source == null && entry != null && entry.getSourcePermanentId() != null) {
            source = findPermanentById(gameData, entry.getSourcePermanentId());
        }
        if (source == null && entry != null && entry.getSourcePermanentId() != null) {
            source = entry.getSourcePermanentSnapshot();
        }
        Card sourceCard = source == null && entry != null ? entry.getEffectiveDamageSourceCard() : null;
        boolean desertSource = source != null
                ? hasEffectiveSubtype(gameData, source, CardSubtype.DESERT)
                : sourceCard != null && sourceCard.getSubtypes().contains(CardSubtype.DESERT);
        if (!desertSource) return false;

        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent protector : battlefield) {
                if (!protector.isAttacking()
                        || !hasActiveStaticEffect(
                        gameData, protector, PreventDamageFromDesertsToSelfAndBandedCreaturesEffect.class)) {
                    continue;
                }
                if (protector.getId().equals(target.getId())
                        || (protector.getBandId() != null && target.isAttacking()
                        && protector.getBandId().equals(target.getBandId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean sourceHasKeyword(GameData gameData, StackEntry entry, Permanent explicitSource, Keyword keyword) {
        Card damageSource = entry.getDamageSourceCard();
        if (damageSource != null && (explicitSource == null
                || !explicitSource.getCard().getId().equals(damageSource.getId()))) {
            Permanent liveDamageSource = findPermanentByCardId(gameData, damageSource.getId());
            if (liveDamageSource == null && entry.getSourcePermanentSnapshot() != null
                    && entry.getSourcePermanentSnapshot().getCard().getId().equals(damageSource.getId())) {
                liveDamageSource = entry.getSourcePermanentSnapshot();
            }
            return liveDamageSource != null
                    ? hasKeyword(gameData, liveDamageSource, keyword)
                    : damageSource.getKeywords().contains(keyword)
                    || (entry.getTriggeringPermanentKeywordsAtLeave() != null
                    && entry.getTriggeringPermanentKeywordsAtLeave().contains(keyword));
        }
        Permanent source = explicitSource;
        if (source == null && entry.getSourcePermanentId() != null) {
            source = findPermanentById(gameData, entry.getSourcePermanentId());
        }
        boolean lastKnownSource = false;
        if (source == null && entry.getSourcePermanentId() != null) {
            source = entry.getSourcePermanentSnapshot();
            lastKnownSource = source != null;
        }
        if (source != null) {
            // A source that already left the battlefield is read from its last known information, which
            // includes keywords it had from counters (CR 608.2h).
            return hasKeyword(gameData, source, keyword) || lastKnownSource && source.hasKeyword(keyword);
        }
        // No permanent source: the stack entry itself is the source (e.g. an instant/sorcery
        // spell like Puncture Blast). Its printed keywords (wither, etc.) apply. CR 702.80.
        boolean entryHasKeyword = entry.getSourcePermanentId() == null
                && entry.getCard() != null
                && (entry.getCard().getKeywords().contains(keyword)
                || gameData.perpetualCardKeywords.getOrDefault(entry.getCard().getId(), Set.of()).contains(keyword)
                || entry.getGrantedKeywordsOnEntry().contains(keyword)
                || entry.getGrantedKeywordsWhileOnStack().contains(keyword));
        if (entryHasKeyword) {
            return true;
        }
        return keyword == Keyword.DEATHTOUCH
                && entry.getSourcePermanentId() == null
                && entry.getCard() != null
                && (entry.getCard().hasType(CardType.INSTANT) || entry.getCard().hasType(CardType.SORCERY))
                && countPlayerControlledStaticEffects(gameData, entry.getControllerId(),
                GrantDeathtouchToControllerSpellsEffect.class) > 0;
    }

    /**
     * Whether a stack object currently has split second: printed or cast-time-granted, or granted by a
     * continuing static ability because mana from an artifact was spent to cast it. Split second is a
     * property of spells only (CR 702.61a), so triggered and activated abilities never have it, even when
     * their source card does.
     */
    public boolean hasSplitSecond(GameData gameData, StackEntry entry) {
        if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                || entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY) {
            return false;
        }
        if (entry.hasKeyword(Keyword.SPLIT_SECOND)) {
            return true;
        }
        return entry.isArtifactManaSplitSecond() && playerControlsStaticEffect(gameData,
                entry.getControllerId(), GrantSplitSecondToControllerSpellsUsingArtifactManaEffect.class);
    }

    /** Returns whether a spell cast using artifact-produced mana should have split second. */
    public boolean hasArtifactManaSplitSecond(GameData gameData, UUID playerId, UUID spellCardId) {
        if (!playerControlsStaticEffect(gameData, playerId,
                GrantSplitSecondToControllerSpellsUsingArtifactManaEffect.class)) {
            return false;
        }
        return gameData.getSpellCastArtifactManaSpent(spellCardId) > 0;
    }

    /**
     * Returns {@code true} if the permanent's damage to creatures is dealt as -1/-1 counters:
     * either infect (CR 702.90) or wither (CR 702.80). The two behave identically against
     * creatures; they differ only against players (infect gives poison, wither is normal damage).
     */
    public boolean dealsCounterDamageToCreatures(GameData gameData, Permanent permanent) {
        return allDamageDealtWithWither(gameData)
                || hasKeyword(gameData, permanent, Keyword.INFECT)
                || hasKeyword(gameData, permanent, Keyword.WITHER);
    }

    /**
     * Returns {@code true} when an {@link AllDamageDealtWithWitherEffect} is on any battlefield
     * (e.g. Everlasting Torment), making every damage source deal creature damage as -1/-1 counters.
     */
    private boolean allDamageDealtWithWither(GameData gameData) {
        return anyBattlefieldHasStaticEffect(gameData, AllDamageDealtWithWitherEffect.class);
    }

    /**
     * Stack-entry variant of {@link #dealsCounterDamageToCreatures}: whether the damage source
     * (explicit permanent or the entry's source permanent) deals creature damage as -1/-1 counters.
     */
    public boolean sourceDealsCounterDamageToCreatures(GameData gameData, StackEntry entry, Permanent explicitSource) {
        return allDamageDealtWithWither(gameData)
                || sourceHasKeyword(gameData, entry, explicitSource, Keyword.INFECT)
                || sourceHasKeyword(gameData, entry, explicitSource, Keyword.WITHER);
    }

    /**
     * Soul-Scar Mage: whether noncombat damage from one of {@code sourceControllerId}'s sources to a
     * creature controlled by {@code targetCreatureControllerId} should be dealt as -1/-1 counters
     * instead. True when the source's controller controls a permanent with
     * {@link NoncombatDamageToOpponentCreaturesAsMinusCountersEffect} and the target creature's
     * controller is a different player (an opponent). Combat is not checked here: this is only
     * consulted on the noncombat damage path.
     */
    public boolean noncombatDamageToOpponentCreatureAsCounters(GameData gameData, UUID sourceControllerId,
                                                               UUID targetCreatureControllerId) {
        if (sourceControllerId == null || targetCreatureControllerId == null
                || sourceControllerId.equals(targetCreatureControllerId)) {
            return false;
        }
        return playerControlsStaticEffect(gameData, sourceControllerId,
                NoncombatDamageToOpponentCreaturesAsMinusCountersEffect.class);
    }

    /** Whether {@code playerId} controls a permanent carrying the given STATIC-slot effect. */
    private boolean playerControlsStaticEffect(GameData gameData, UUID playerId, Class<? extends CardEffect> effectType) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        return battlefield.stream()
                .anyMatch(p -> !p.isFaceDown() && printsStaticEffect(gameData, p, effectType)
                        && !hasLostAllAbilities(gameData, p));
    }

    /** Returns the number of STATIC-slot effects of the given type controlled by a player. */
    public int countPlayerControlledStaticEffects(GameData gameData, UUID playerId,
                                                   Class<? extends CardEffect> effectType) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        return (int) battlefield.stream()
                .filter(permanent -> !permanent.isFaceDown())
                .filter(permanent -> !hasLostAllAbilities(gameData, permanent))
                .flatMap(permanent -> permanent.getCard().getStaticEffects().stream())
                .filter(effectType::isInstance)
                .count();
    }

    /** Returns the number of The Valeyard effects affecting an opponent's villainous choice. */
    public int countAdditionalVillainousChoices(GameData gameData, UUID affectedPlayerId) {
        if (affectedPlayerId == null) {
            return 0;
        }
        return gameData.playerBattlefields.keySet().stream()
                .filter(controllerId -> !controllerId.equals(affectedPlayerId))
                .mapToInt(controllerId -> countPlayerControlledStaticEffects(
                        gameData, controllerId, TheValeyardEffect.class))
                .sum();
    }

    /** Returns how many "you may vote an additional time" abilities (Tivit, The Valeyard) a voter controls. */
    public int countAdditionalVotes(GameData gameData, UUID voterId) {
        return voterId == null ? 0
                : countPlayerControlledStaticEffects(gameData, voterId, MayVoteAdditionalTimeEffect.class);
    }

    /**
     * Returns {@code true} if the target permanent cannot be targeted by spells of the
     * given color. Checks both the permanent's own static effects and effects granted by
     * other permanents.
     */
    public boolean cantBeTargetedBySpellColor(GameData gameData, Permanent target, CardColor spellColor) {
        return cantBeTargetedBySpellColor(gameData, target, spellColor, null);
    }

    /**
     * Overload that also honours opponent-gated spell-color restrictions ("can't be the target of
     * [color] spells your opponents control", Fiendslayer Paladin) by comparing the spell's
     * controller with the target's controller. Passing {@code null} skips those restrictions.
     */
    public boolean cantBeTargetedBySpellColor(GameData gameData, Permanent target, CardColor spellColor,
                                              UUID spellControllerId) {
        return cantBeTargetedBySourceColor(gameData, target, spellColor, spellControllerId, true);
    }

    /**
     * Color check for a targeting source that is either a spell ({@code sourceIsSpell}) or an
     * ability. Spell-only restrictions ("can't be the target of [color] spells", Karplusan Strider)
     * block only spells; restrictions covering spells and abilities block both.
     */
    public boolean cantBeTargetedBySourceColor(GameData gameData, Permanent target, CardColor spellColor,
                                               UUID spellControllerId, boolean sourceIsSpell) {
        if (spellColor == null) {
            return false;
        }
        boolean opponentControlled = isOpponentControlledSpell(gameData, target, spellControllerId);
        // "Hexproof from [color]" is a hexproof ability, so an effect that lets the spell's
        // controller target opponents' permanents as though they didn't have hexproof lifts it.
        boolean hexproofLifted = opponentControlled
                && (ignoresOpponentPermanentHexproof(gameData, spellControllerId)
                || (isCreature(gameData, target) && ignoresOpponentCreatureHexproof(gameData, spellControllerId)));
        for (CardEffect effect : getActiveStaticEffects(gameData, target)) {
            if (isSpellColorRestriction(effect, spellColor, opponentControlled, sourceIsSpell)
                    && (!isHexproofFromColorRestriction(effect, spellColor)
                    || (!hexproofLifted && hasHexproofFromColor(gameData, target, spellColor)))) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isSpellColorRestriction(effect, spellColor, opponentControlled, sourceIsSpell)
                    && (!isHexproofFromColorRestriction(effect, spellColor)
                    || (!hexproofLifted && hasHexproofFromColor(gameData, target, spellColor)))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns {@code true} if the target permanent can't be the target of any spell (regardless of
     * color or controller) â€” Dense Foliage's "Creatures can't be the targets of spells". Abilities are
     * unaffected. Scans both the permanent's own STATIC effects and effects granted to it.
     */
    public boolean cantBeTargetedByAnySpell(GameData gameData, Permanent target) {
        boolean ownAbilitiesActive = !gameData.losesAllAbilitiesByEffect(target)
                && !computeStaticBonus(gameData, target).losesAllAbilities();
        if (ownAbilitiesActive) {
            for (CardEffect effect : target.getCard().getStaticEffects()) {
                if (isActiveAnySpellRestriction(gameData, target, effect)) {
                    return true;
                }
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isAnySpellRestriction(effect)) {
                return true;
            }
        }
        return hasAttachedPermanentRestriction(gameData, target, GameQueryService::isAnySpellRestriction);
    }

    private boolean isActiveAnySpellRestriction(GameData gameData, Permanent target, CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional) {
            UUID controllerId = findPermanentController(gameData, target.getId());
            return controllerId != null
                    && conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStaticEffect(target, controllerId))
                    && isActiveAnySpellRestriction(gameData, target, conditional.wrapped());
        }
        return isAnySpellRestriction(effect);
    }

    /**
     * Returns {@code true} if the target permanent cannot be targeted by a source whose legal
     * targets are restricted to Walls.
     */
    public boolean cantBeTargetedByWallOnlySources(GameData gameData, Permanent target) {
        StaticBonus bonus = computeStaticBonus(gameData, target);
        if (gameData.losesAllAbilitiesByEffect(target) || bonus.losesAllAbilities()) {
            return false;
        }
        if (target.getCard().getStaticEffects().stream()
                .anyMatch(WallOnlyTargetingRestrictionEffect.class::isInstance)) {
            return true;
        }
        return bonus.grantedEffects().stream()
                .anyMatch(WallOnlyTargetingRestrictionEffect.class::isInstance);
    }

    private static boolean isAnySpellRestriction(CardEffect effect) {
        return effect instanceof TargetingRestrictionEffect r
                && (r.kind() == TargetingSourceKind.SPELLS
                || r.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES)
                && !r.opponentOnly()
                && r.sourceCardTypes().isEmpty()
                && r.mode() == TargetColorMode.ANY;
    }

    /**
     * Returns {@code true} if the target permanent has hexproof from the given spell card type
     * against a spell controlled by the given player.
     */
    public boolean hasHexproofFromCardType(GameData gameData, Permanent target, CardType sourceCardType,
                                           UUID spellControllerId) {
        if (sourceCardType == null || !isOpponentControlledSpell(gameData, target, spellControllerId)) {
            return false;
        }
        for (CardEffect effect : target.getCard().getStaticEffects()) {
            if (isHexproofFromCardTypeRestriction(effect, sourceCardType)) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isHexproofFromCardTypeRestriction(effect, sourceCardType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHexproofFromCardTypeRestriction(CardEffect effect, CardType sourceCardType) {
        return effect instanceof TargetingRestrictionEffect r
                && r.kind() == TargetingSourceKind.SPELLS
                && r.opponentOnly()
                && r.sourceCardTypes().contains(sourceCardType);
    }

    /**
     * Returns {@code true} if the permanent can't be enchanted by other Auras (e.g. Anti-Magic Aura),
     * from its own static effects or from effects granted by other permanents.
     */
    public boolean cantBeEnchantedByOtherAuras(GameData gameData, Permanent target) {
        return hasAuraAttachmentRestriction(gameData, target, false, null);
    }

    /**
     * As {@link #cantBeEnchantedByOtherAuras(GameData, Permanent)} for one particular Aura: a
     * restriction an attached Aura imposes (Anti-Magic Aura) doesn't stop that same Aura, which is
     * not an "other" Aura.
     *
     * @param auraCardId the card of the Aura that would enchant {@code target}
     */
    public boolean cantBeEnchantedByAura(GameData gameData, Permanent target, UUID auraCardId) {
        return hasAuraAttachmentRestriction(gameData, target, false, auraCardId);
    }

    /** Returns whether Aura spells cannot target this permanent, including attachment restrictions. */
    public boolean cantBeTargetedByAuraSpells(GameData gameData, Permanent target) {
        return hasAuraAttachmentRestriction(gameData, target, true, null);
    }

    /**
     * Returns {@code true} if an attachment restriction on the permanent puts Auras already attached
     * to it into their owner's graveyard. Restrictions marked to keep existing Auras attached (Guardian
     * Beast) only stop new attachment and are excluded, as are Aura-spell-only targeting restrictions
     * (Tetsuo Umezawa), which never make an attached Aura illegal.
     */
    public boolean cantBeEnchantedByOtherAurasRemovingExisting(GameData gameData, Permanent target,
                                                               UUID attachedAuraCardId) {
        List<CardEffect> effects = new ArrayList<>(getActiveStaticEffects(gameData, target));
        effects.addAll(getGrantedEffects(gameData, target));
        effects.addAll(restrictionsFromOtherAttachments(gameData, target, attachedAuraCardId));
        return effects.stream().anyMatch(effect -> effect instanceof CantBeEnchantedByOtherAurasEffect restriction
                && !restriction.auraSpellsOnly() && !restriction.keepsExistingAuras());
    }

    private boolean hasAuraAttachmentRestriction(GameData gameData, Permanent target, boolean auraSpell,
                                                 UUID auraCardId) {
        List<CardEffect> effects = new ArrayList<>(getActiveStaticEffects(gameData, target));
        effects.addAll(getGrantedEffects(gameData, target));
        effects.addAll(restrictionsFromOtherAttachments(gameData, target, auraCardId));
        return effects.stream().anyMatch(effect -> effect instanceof CantBeEnchantedByOtherAurasEffect restriction
                && (auraSpell || !restriction.auraSpellsOnly()));
    }

    /** Attached-permanent restrictions on {@code target}, except those imposed by the given card's permanent. */
    private List<CardEffect> restrictionsFromOtherAttachments(GameData gameData, Permanent target,
                                                              UUID exceptCardId) {
        return attachedPermanentRestrictions(gameData, target).stream()
                .filter(attached -> exceptCardId == null || !exceptCardId.equals(attached.source().getCard().getId()))
                .map(AttachedRestriction::restriction)
                .toList();
    }

    /** Returns whether another player is prevented from gaining control of the permanent. */
    public boolean cantBeControlledByOtherPlayers(GameData gameData, Permanent target) {
        return target.getCard().getStaticEffects().stream()
                .anyMatch(CantBeControlledByOtherPlayersEffect.class::isInstance)
                || hasGrantedEffect(gameData, target, CantBeControlledByOtherPlayersEffect.class);
    }

    /**
     * Returns {@code true} if the permanent can't have an Equipment attached to it, from its own
     * static effects or from effects granted by other permanents.
     */
    public boolean cantBeEquipped(GameData gameData, Permanent target) {
        if (!hasLostAllAbilities(gameData, target)) {
            for (CardEffect effect : target.getCard().getStaticEffects()) {
                if (effect instanceof CantBeEquippedEffect) {
                    return true;
                }
            }
        }
        return hasGrantedEffect(gameData, target, CantBeEquippedEffect.class);
    }

    /**
     * Matches the "can't be the target of spells of [color]" restriction (Karplusan Strider) for the
     * given spell color. Opponent-gated variants (Fiendslayer Paladin) only match when the spell is
     * known to be opponent-controlled.
     */
    private static boolean isSpellColorRestriction(CardEffect effect, CardColor spellColor,
                                                   boolean opponentControlled, boolean sourceIsSpell) {
        return effect instanceof TargetingRestrictionEffect r
                && (r.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES
                || (sourceIsSpell && r.kind() == TargetingSourceKind.SPELLS))
                && r.mode() == TargetColorMode.BLOCKED_COLORS
                && r.colors().contains(spellColor)
                && (!r.opponentOnly() || opponentControlled);
    }

    /** Whether {@code spellControllerId} is a player other than the target permanent's controller. */
    private boolean isOpponentControlledSpell(GameData gameData, Permanent target, UUID spellControllerId) {
        if (spellControllerId == null) {
            return false;
        }
        UUID targetController = findPermanentController(gameData, target.getId());
        return targetController != null && !targetController.equals(spellControllerId);
    }

    /**
     * Returns {@code true} if the target permanent has "hexproof from [color]" that matches
     * the given source color. Unlike full hexproof, this only blocks targeting from sources
     * of the specified color(s). Only blocks opponent-controlled sources.
     */
    public boolean hasHexproofFromColor(GameData gameData, Permanent target, CardColor sourceColor) {
        if (sourceColor == null || computeStaticBonus(gameData, target).removedKeywords().contains(Keyword.HEXPROOF)
                || cantHaveOrGainKeyword(gameData, target, Keyword.HEXPROOF)) {
            return false;
        }
        for (FloatingContinuousEffect turnGrant : gameData.floatingEffectsOnPermanent(target.getId(),
                GrantControllerAndPermanentsHexproofFromColorsEffect.class)) {
            if (((GrantControllerAndPermanentsHexproofFromColorsEffect) turnGrant.effect()).colors()
                    .contains(sourceColor)) {
                return true;
            }
        }
        for (CardEffect effect : getActiveStaticEffects(gameData, target)) {
            if (isHexproofFromColorRestriction(effect, sourceColor)) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isHexproofFromColorRestriction(effect, sourceColor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHexproofFromColorRestriction(CardEffect effect, CardColor sourceColor) {
        return isColorSourceRestriction(effect, sourceColor) && ((TargetingRestrictionEffect) effect).opponentOnly();
    }

    /** Returns whether opponents' monocolored spells and abilities can't target this permanent. */
    public boolean hasHexproofFromMonocolored(GameData gameData, Permanent target) {
        return hasColorModeSourceRestriction(gameData, target, true, TargetColorMode.MONOCOLORED);
    }

    /** Returns whether opponents' multicolored spells and abilities can't target this permanent. */
    public boolean hasHexproofFromMulticolored(GameData gameData, Permanent target) {
        return hasColorModeSourceRestriction(gameData, target, true, TargetColorMode.MULTICOLORED);
    }

    /**
     * Returns whether opponents' spells and abilities from a source with {@code sourceColorCount} colors
     * can't target this permanent under a hexproof-from-monocolored or hexproof-from-multicolored effect.
     */
    public boolean hasAnyHexproofVariant(GameData gameData, Permanent permanent) {
        if (hasKeyword(gameData, permanent, Keyword.HEXPROOF)) {
            return true;
        }
        if (gameData.losesAllAbilitiesByEffect(permanent)) {
            return false;
        }
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (bonus.losesAllAbilities()) {
            return false;
        }
        boolean printedVariant = permanent.getCard().getStaticEffects().stream()
                .anyMatch(effect -> effect instanceof TargetingRestrictionEffect r && r.hexproofLike());
        return printedVariant || bonus.grantedEffects().stream()
                .anyMatch(effect -> effect instanceof TargetingRestrictionEffect r && r.hexproofLike());
    }

    public boolean hasHexproofFromColorCount(GameData gameData, Permanent target, int sourceColorCount) {
        if (sourceColorCount == 1) {
            return hasHexproofFromMonocolored(gameData, target);
        }
        return sourceColorCount >= 2 && hasHexproofFromMulticolored(gameData, target);
    }

    /** Returns whether monocolored spells and abilities can't target this permanent. */
    public boolean cantBeTargetedByMonocoloredSources(GameData gameData, Permanent target) {
        return hasColorModeSourceRestriction(gameData, target, false, TargetColorMode.MONOCOLORED);
    }

    private boolean hasColorModeSourceRestriction(GameData gameData, Permanent target, boolean opponentOnly,
                                                  TargetColorMode mode) {
        for (CardEffect effect : getActiveStaticEffects(gameData, target)) {
            if (isColorModeSourceRestriction(effect, opponentOnly, mode)) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isColorModeSourceRestriction(effect, opponentOnly, mode)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isColorModeSourceRestriction(CardEffect effect, boolean opponentOnly,
                                                        TargetColorMode mode) {
        return effect instanceof TargetingRestrictionEffect restriction
                && restriction.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES
                && restriction.mode() == mode
                && restriction.opponentOnly() == opponentOnly;
    }

    /**
     * Returns {@code true} if the target permanent can't be the target of spells or abilities from
     * sources of the given color, no matter who controls them (Suq'Ata Firewalker). Unlike
     * {@link #hasHexproofFromColor}, the permanent's own controller is restricted too, so callers
     * must not gate this on the source being opponent-controlled.
     */
    public boolean cantBeTargetedByColorSources(GameData gameData, Permanent target, CardColor sourceColor) {
        if (sourceColor == null) {
            return false;
        }
        for (CardEffect effect : getActiveStaticEffects(gameData, target)) {
            if (isAnyControllerColorRestriction(effect, sourceColor)) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isAnyControllerColorRestriction(effect, sourceColor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnyControllerColorRestriction(CardEffect effect, CardColor sourceColor) {
        return isColorSourceRestriction(effect, sourceColor) && !((TargetingRestrictionEffect) effect).opponentOnly();
    }

    private static boolean isColorSourceRestriction(CardEffect effect, CardColor sourceColor) {
        return effect instanceof TargetingRestrictionEffect r
                && r.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES
                && r.mode() == TargetColorMode.BLOCKED_COLORS
                && r.colors().contains(sourceColor);
    }

    /**
     * Returns {@code true} if the target permanent cannot be targeted by spells or abilities
     * from the given source card, because the target has a static effect restricting targeting
     * to only sources of a specific color (e.g. Gaea's Revenge: "can't be the target of
     * nongreen spells or abilities from nongreen sources").
     */
    public boolean cantBeTargetedByNonColorSources(GameData gameData, Permanent target, Card sourceCard) {
        return cantBeTargetedByNonColorSources(gameData, target, sourceCard, null, false);
    }

    /**
     * Returns whether a permanent cannot be targeted by a source of a disallowed color, respecting
     * restrictions that apply only to sources controlled by an opponent of the permanent's controller.
     */
    public boolean cantBeTargetedByNonColorSources(GameData gameData, Permanent target, Card sourceCard,
                                                   UUID sourceControllerId) {
        return cantBeTargetedByNonColorSources(gameData, target, sourceCard, sourceControllerId, true);
    }

    private boolean cantBeTargetedByNonColorSources(GameData gameData, Permanent target, Card sourceCard,
                                                    UUID sourceControllerId, boolean enforceOpponentOnly) {
        if (sourceCard == null || hasLostAllAbilities(gameData, target)) {
            return false;
        }
        for (CardEffect effect : target.getCard().getStaticEffects()) {
            if (isNonColorSourceRestriction(gameData, target, effect, sourceCard,
                    sourceControllerId, enforceOpponentOnly)) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isNonColorSourceRestriction(gameData, target, effect, sourceCard,
                    sourceControllerId, enforceOpponentOnly)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Matches the "can't be the target of non-[color] sources" restriction (Gaea's Revenge): the
     * source is blocked unless it has one of the allowed colors.
     */
    private boolean isNonColorSourceRestriction(GameData gameData, Permanent target, CardEffect effect,
                                                Card sourceCard, UUID sourceControllerId,
                                                boolean enforceOpponentOnly) {
        if (!(effect instanceof TargetingRestrictionEffect r)
                || r.mode() != TargetColorMode.ALLOWED_COLORS_ONLY
                || r.colors().stream().anyMatch(color -> sourceHasColor(gameData, sourceCard, color))) {
            return false;
        }
        if (!enforceOpponentOnly || !r.opponentOnly()) {
            return true;
        }
        UUID targetControllerId = findPermanentController(gameData, target.getId());
        return sourceControllerId == null
                || targetControllerId == null
                || !targetControllerId.equals(sourceControllerId);
    }

    private boolean sourceHasColor(GameData gameData, Card card, CardColor color) {
        if (card.getColor() == color) {
            return true;
        }
        return getEffectiveCardColors(gameData, card).contains(color);
    }

    /**
     * Returns {@code true} if the given spell cannot be countered by the given counter source,
     * because the spell's controller has turn-duration protection from countering by spells
     * of the source's color (e.g. Autumn's Veil). Only applies when the counter source is
     * a spell (not a triggered or activated ability).
     */
    public boolean isProtectedFromCounterBySpellColor(GameData gameData, UUID spellControllerId, StackEntry counterSource) {
        Set<CardColor> protectedColors = spellColorsCantCounter(gameData, spellControllerId);
        if (protectedColors == null || protectedColors.isEmpty()) {
            return false;
        }
        // Only protects against spells, not abilities
        StackEntryType sourceType = counterSource.getEntryType();
        if (sourceType == StackEntryType.TRIGGERED_ABILITY || sourceType == StackEntryType.ACTIVATED_ABILITY) {
            return false;
        }
        return getEffectiveCardColors(gameData, counterSource.getCard()).stream().anyMatch(protectedColors::contains);
    }

    /**
     * Returns {@code true} if the given spell cannot be countered by the given source card,
     * because the spell's controller has turn-duration protection from countering by spells
     * of that color (e.g. Autumn's Veil). Only applies when the source is an instant or sorcery.
     */
    /**
     * Whether {@code controllerId} controls a permanent that replaces their countering of spells with
     * exile and a free play (Guile). A spell exiled this way was not countered.
     */
    public boolean controlsCounterExileReplacement(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent permanent : battlefield) {
            if (hasLostAllAbilities(gameData, permanent)) {
                continue;
            }
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                if (effect instanceof ReplaceControlledCounterWithExileAndPlayEffect) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The colors of spells that can't counter {@code spellControllerId}'s spells this turn (Autumn's Veil). */
    private Set<CardColor> spellColorsCantCounter(GameData gameData, UUID spellControllerId) {
        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        for (FloatingContinuousEffect fe : gameData.floatingEffectsOnPlayer(spellControllerId,
                GrantControllerSpellsCantBeCounteredByColorsEffect.class)) {
            colors.addAll(((GrantControllerSpellsCantBeCounteredByColorsEffect) fe.effect()).colors());
        }
        return colors;
    }

    public boolean isProtectedFromCounterBySourceCard(GameData gameData, UUID spellControllerId, Card sourceCard) {
        Set<CardColor> protectedColors = spellColorsCantCounter(gameData, spellControllerId);
        if (protectedColors == null || protectedColors.isEmpty()) {
            return false;
        }
        // Only protects against spells, not abilities
        CardType sourceType = sourceCard.getType();
        if (sourceType != CardType.INSTANT && sourceType != CardType.SORCERY) {
            return false;
        }
        return getEffectiveCardColors(gameData, sourceCard).stream().anyMatch(protectedColors::contains);
    }

    /**
     * Returns {@code true} if Peace Talks is active: creatures can't attack, and players and
     * permanents can't be the targets of spells or activated abilities (triggered abilities are
     * unaffected). Active while a floating {@code PeaceTalksEffect} lasts.
     */
    public boolean isPeaceTalksActive(GameData gameData) {
        return gameData.hasFloatingEffectOnPlayer(null,
                com.github.laxika.magicalvibes.model.effect.PeaceTalksEffect.class);
    }

    /**
     * Returns {@code true} if the player has shroud (cannot be the target of spells or
     * abilities), granted either temporarily or by a permanent they control with a
     * {@link GrantControllerKeywordEffect} for {@link Keyword#SHROUD}.
     */
    public boolean playerHasShroud(GameData gameData, UUID playerId) {
        return playerHasTemporaryKeyword(gameData, playerId, Keyword.SHROUD)
                || playerBattlefieldGrantsControllerKeyword(gameData, playerId, Keyword.SHROUD);
    }

    /**
     * Returns whether a floating effect gives the player {@code keyword} for a stated duration ("you gain shroud until
     * end of turn", "until your next turn").
     */
    private boolean playerHasTemporaryKeyword(GameData gameData, UUID playerId, Keyword keyword) {
        for (CardEffect effect : gameData.floatingPlayerEffects(playerId,
                GrantControllerKeywordUntilEndOfTurnEffect.class)) {
            if (((GrantControllerKeywordUntilEndOfTurnEffect) effect).keyword() == keyword) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the player has hexproof (cannot be the target of spells or
     * abilities opponents control), granted either temporarily or by a permanent they control
     * with a {@link GrantControllerKeywordEffect} for {@link Keyword#HEXPROOF}.
     */
    public boolean playerHasHexproof(GameData gameData, UUID playerId) {
        return playerHasTemporaryKeyword(gameData, playerId, Keyword.HEXPROOF)
                || playerBattlefieldGrantsControllerKeyword(gameData, playerId, Keyword.HEXPROOF);
    }

    /** Returns whether the player currently has flying, including Sarah's Wings' temporary grant. */
    public boolean playerHasFlying(GameData gameData, UUID playerId) {
        return playerHasTemporaryKeyword(gameData, playerId, Keyword.FLYING);
    }

    /** Returns whether an opposing permanent currently limits this player's dungeon ventures. */
    public boolean playerHasDungeonVentureRestriction(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(playerId))
                .flatMap(entry -> entry.getValue().stream())
                .filter(permanent -> !permanent.isFaceDown() && !hasLostPrintedAbilities(gameData, permanent))
                .anyMatch(permanent -> permanent.getCard().getStaticEffects().stream()
                        .anyMatch(OpponentsCantVentureIntoDungeonMoreThanOnceEachTurnEffect.class::isInstance));
    }

    /**
     * Returns whether the player has hexproof from the given color until end of turn. This is
     * targeting-only and does not provide protection from damage.
     */
    public boolean playerHasHexproofFromColor(GameData gameData, UUID playerId, CardColor color) {
        if (color == null) {
            return false;
        }
        for (CardEffect effect : gameData.floatingPlayerEffects(playerId,
                GrantControllerAndPermanentsHexproofFromColorsEffect.class)) {
            if (((GrantControllerAndPermanentsHexproofFromColorsEffect) effect).colors().contains(color)) {
                return true;
            }
        }
        return false;
    }

    /**
     * What a player's protection is judged against (CR 702.16b, 702.16c, 702.16e): a spell, the source of an ability,
     * an Aura, or a source of damage.
     *
     * @param card         the object's card: its name, and its card types when it isn't a permanent
     * @param permanent    the object while it is a permanent (its layered card types; face down it has no name), else
     *                     {@code null}
     * @param controllerId the object's controller, or {@code null} when it has none (a card in a graveyard)
     * @param colors       the object's colors as the caller judges them: a spell's, an Aura's, or a damage source's
     *                     after Ghostly Flame
     */
    public record ProtectionSource(Card card, Permanent permanent, UUID controllerId, Set<CardColor> colors) {

        public ProtectionSource {
            colors = colors == null ? Set.of() : Set.copyOf(colors);
        }
    }

    /**
     * The quality {@code playerId} has protection from that {@code source} has, as text ("everything", "red", the
     * name), or {@code null} when no protection of the player covers it. The one reader of a player's protection, asked
     * by targeting, Aura attachment and damage prevention (CR 702.16b, 702.16c, 702.16e). It reads protection from
     * everything ({@link #playerHasProtectionFromEverything}), from players ({@link #playerHasProtectionFromOpponents}:
     * the source's controller, or its owner when no player controls it, CR 702.16k), from colors
     * ({@link #playerHasProtectionFromColor}), from the chosen card type (Serra's Emissary) and from the chosen card
     * name ({@link PlayerHasProtectionEffect}, Runed Halo); printed statics, granted ones and copies floated on the
     * player alike.
     */
    public String playerProtectionFrom(GameData gameData, UUID playerId, ProtectionSource source) {
        if (gameData == null || playerId == null || source == null) {
            return null;
        }
        if (playerHasProtectionFromEverything(gameData, playerId)) {
            return "everything";
        }
        UUID sourcePlayerId = source.controllerId() != null ? source.controllerId()
                : source.card() != null ? gameData.ownerOf(source.card()) : null;
        if (playerHasProtectionFromOpponents(gameData, playerId, sourcePlayerId)) {
            return source.controllerId() != null ? "the source's controller" : "the source's owner";
        }
        for (CardColor color : source.colors()) {
            if (playerHasProtectionFromColor(gameData, playerId, color)) {
                return color.name().toLowerCase();
            }
        }
        if (playerHasProtectionFromChosenCardType(gameData, playerId, source.card(), source.permanent())) {
            return "the source's card type";
        }
        if (playerHasProtectionFromChosenName(gameData, playerId, source)) {
            return source.card().getName();
        }
        return null;
    }

    /**
     * Returns {@code true} if the player has protection from the given color until end of turn
     * (e.g. Faith's Shield fateful hour, Seht's Tiger). One facet of {@link #playerProtectionFrom}, which the rules
     * paths ask.
     */
    public boolean playerHasProtectionFromColor(GameData gameData, UUID playerId, CardColor color) {
        if (color == null) {
            return false;
        }
        for (CardEffect effect : gameData.floatingPlayerEffects(playerId,
                GrantProtectionFromColorUntilEndOfTurnEffect.class)) {
            if (((GrantProtectionFromColorUntilEndOfTurnEffect) effect).color() == color) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the player has protection from every source until their next turn (CR 702.16j). One facet of
     * {@link #playerProtectionFrom}, which the rules paths ask.
     */
    public boolean playerHasProtectionFromEverything(GameData gameData, UUID playerId) {
        return gameData.hasFloatingEffectOnPlayer(playerId, GrantProtectionFromEverythingUntilNextTurnEffect.class);
    }

    /**
     * Returns whether the player has protection from {@code sourcePlayerId} (the source's controller, or its owner when
     * no player controls it, CR 702.16k): a {@link ProtectionFromPlayersEffect} floated on them
     * ({@code GrantProtectionFromPlayerEffect}) naming that player, or, for an opponent, a
     * {@link PlayerHasProtectionEffect} from each opponent: an active static ability of a permanent they control (none
     * of a face-down or ability-less one's, nor one an effect removed, CR 613.1f), one granted to it, or one floated on
     * them. One facet of {@link #playerProtectionFrom}, which the rules paths ask.
     */
    public boolean playerHasProtectionFromOpponents(GameData gameData, UUID playerId, UUID sourcePlayerId) {
        if (playerId == null || sourcePlayerId == null) {
            return false;
        }
        for (CardEffect effect : gameData.floatingPlayerEffects(playerId, ProtectionFromPlayersEffect.class)) {
            if (((ProtectionFromPlayersEffect) effect).playerIds().contains(sourcePlayerId)) {
                return true;
            }
        }
        return !playerId.equals(sourcePlayerId)
                && (hasFloatedPlayerProtection(gameData, playerId, PlayerHasProtectionEffect.Quality.EACH_OPPONENT)
                || !playerProtectionPermanents(gameData, playerId, PlayerHasProtectionEffect.Quality.EACH_OPPONENT)
                .isEmpty());
    }

    /**
     * Whether the player has protection from the source's name: a {@link PlayerHasProtectionEffect} from the chosen
     * name of a permanent they control whose chosen name is one of the source's names, either half of a split card
     * included (CR 709.4a). A face-down source has no name (CR 708.2).
     */
    private boolean playerHasProtectionFromChosenName(GameData gameData, UUID playerId, ProtectionSource source) {
        if (source.card() == null || source.permanent() != null && source.permanent().isFaceDown()) {
            return false;
        }
        for (Permanent halo : playerProtectionPermanents(gameData, playerId,
                PlayerHasProtectionEffect.Quality.CHOSEN_NAME)) {
            if (cardHasChosenName(gameData, source.card(), halo.getChosenName())) {
                return true;
            }
        }
        return false;
    }

    /** Whether a {@link PlayerHasProtectionEffect} of {@code quality} is floated on the player. */
    private boolean hasFloatedPlayerProtection(GameData gameData, UUID playerId,
                                               PlayerHasProtectionEffect.Quality quality) {
        for (CardEffect effect : gameData.floatingPlayerEffects(playerId, PlayerHasProtectionEffect.class)) {
            if (((PlayerHasProtectionEffect) effect).from() == quality) {
                return true;
            }
        }
        return false;
    }

    /**
     * The permanents {@code playerId} controls that give them a {@link PlayerHasProtectionEffect} of {@code quality}:
     * an active static ability (none of a face-down or ability-less permanent's, CR 708.2a) that no effect removed
     * (CR 613.1f), or one an effect granted it that the layers left it.
     */
    private List<Permanent> playerProtectionPermanents(GameData gameData, UUID playerId,
                                                       PlayerHasProtectionEffect.Quality quality) {
        List<Permanent> found = new ArrayList<>();
        for (var active : activeStaticEffects(gameData, playerId, PlayerHasProtectionEffect.class)) {
            if (active.effect().from() == quality
                    && !gameData.removesOwnStaticAbility(active.source().getId(), PlayerHasProtectionEffect.class)) {
                found.add(active.source());
            }
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!found.contains(permanent) && computeStaticBonus(gameData, permanent).grantedEffects().stream()
                        .anyMatch(effect -> effect instanceof PlayerHasProtectionEffect granted
                                && granted.from() == quality)) {
                    found.add(permanent);
                }
            }
        }
        return found;
    }

    /**
     * Whether the player controls a permanent granting protection from the source's card type, based on that
     * permanent's chosen card type (Serra's Emissary). The permanent's card types include those granted by the layer
     * system, such as a creature's temporary animation.
     */
    private boolean playerHasProtectionFromChosenCardType(GameData gameData, UUID playerId,
                                                          Card sourceCard, Permanent sourcePermanent) {
        if (gameData == null || playerId == null || (sourceCard == null && sourcePermanent == null)) {
            return false;
        }
        Set<CardType> sourceTypes = sourcePermanent != null
                ? getEffectiveCardTypes(gameData, sourcePermanent)
                : cardTypesOf(sourceCard);
        if (sourceTypes.isEmpty()) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent permanent : battlefield) {
            if (hasLostPrintedAbilities(gameData, permanent)) {
                continue;
            }
            GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect effect =
                    permanent.getCard().getStaticEffects().stream()
                            .filter(GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect.class::isInstance)
                            .map(GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect.class::cast)
                            .findFirst().orElse(null);
            if (effect != null && !gameData.removesOwnStaticAbility(permanent.getId(), effect.getClass())
                    && sourceTypes.contains(permanent.getChosenCardType())) {
                return true;
            }
        }
        return false;
    }

    private Set<CardType> cardTypesOf(Card card) {
        Set<CardType> types = EnumSet.of(card.getType());
        types.addAll(card.getAdditionalTypes());
        return types;
    }

    /**
     * Whether a spell deals the damage of {@code entry} itself: the entry is a spell with no source permanent, so no
     * permanent it made fight or deal damage is the source. A spell copy is a spell (CR 707.10).
     */
    public boolean isDealtBySpell(StackEntry entry) {
        return entry != null && entry.getSourcePermanentId() == null && isSpellStackEntry(entry.getEntryType());
    }

    /** Returns the first turn-scoped damage-prevention shield matching the targeted spell. */
    public TargetSpellDamagePreventionShield getTargetSpellDamagePreventionShield(
            GameData gameData, StackEntry entry) {
        if (!isDamagePreventable(gameData) || entry == null) {
            return null;
        }
        Card damageSourceCard = entry.getDamageSourceCard() != null
                ? entry.getDamageSourceCard() : entry.getCard();
        Permanent source = entry.getSourcePermanentId() != null
                ? findPermanentById(gameData, entry.getSourcePermanentId()) : null;
        if (source == null) source = entry.getSourcePermanentSnapshot();
        if (source != null && damageSourceCard != null
                && !source.getCard().getId().equals(damageSourceCard.getId())) {
            source = findPermanentByCardId(gameData, damageSourceCard.getId());
        }
        UUID sourceId = source != null ? source.getId() : entry.getSourcePermanentId();
        Set<CardColor> sourceColors = source != null ? getEffectiveColors(gameData, source)
                : damageSourceCard != null ? getEffectiveCardColors(gameData, damageSourceCard) : Set.of();
        for (TargetSpellDamagePreventionShield shield : gameData.shields(TargetSpellDamagePreventionShield.class)) {
            boolean matchesSource = shield.sourcePermanentId() != null
                    ? shield.sourcePermanentId().equals(sourceId)
                    : isSpellStackEntry(entry.getEntryType()) && damageSourceCard != null
                            && damageSourceCard.getId().equals(shield.spellCardId());
            if (matchesSource && (shield.requiredColors().isEmpty()
                    || sourceColors.stream().anyMatch(shield.requiredColors()::contains))) {
                return shield;
            }
        }
        return null;
    }

    /**
     * Checks a chosen permanent's identity and its colors when the damage is dealt. Shields that gain
     * life for the damage prevented (Hallow) prevent through the damage replacement pipeline instead.
     */
    private boolean isDamageFromChosenPermanentPrevented(GameData gameData, Permanent source) {
        Set<CardColor> sourceColors = getEffectiveColors(gameData, source);
        return gameData.shields(TargetSpellDamagePreventionShield.class).stream()
                .anyMatch(shield -> source.getId().equals(shield.sourcePermanentId())
                        && shield.lifeGainPlayerId() == null
                        && (shield.requiredColors().isEmpty()
                                || sourceColors.stream().anyMatch(shield.requiredColors()::contains)));
    }

    /** Returns whether a targeted instant or sorcery spell has its damage prevented this turn. */
    public boolean isDamageFromTargetSpellPrevented(GameData gameData, StackEntry entry) {
        return getTargetSpellDamagePreventionShield(gameData, entry) != null;
    }

    /**
     * Returns whether the shield preventing {@code entry}'s damage is a plain "prevent all damage a source would
     * deal" shield on a permanent (no life gain). That is the same prevention as the source-based rules apply, so
     * offering both as separate replacement choices would ask the player to order two identical effects.
     */
    public boolean isTargetSpellShieldPlainPermanentSourcePrevention(GameData gameData, StackEntry entry) {
        TargetSpellDamagePreventionShield shield = getTargetSpellDamagePreventionShield(gameData, entry);
        return shield != null && shield.sourcePermanentId() != null && shield.lifeGainPlayerId() == null;
    }

    /** Returns whether a spell that targets the given permanent has its damage prevented there. */
    public boolean isDamageFromTargetingSpellPrevented(GameData gameData, StackEntry entry, Permanent target) {
        if (!isDamagePreventable(gameData) || entry == null || target == null
                || !isSpellStackEntry(entry.getEntryType())) {
            return false;
        }
        UUID targetId = target.getId();
        if (!targetId.equals(entry.getTargetId()) && !entry.getTargetIds().contains(targetId)) {
            return false;
        }
        return target.getCard().getStaticEffects().stream()
                .filter(TargetedSpellDamagePreventionEffect.class::isInstance)
                .map(TargetedSpellDamagePreventionEffect.class::cast)
                .anyMatch(effect -> effect.condition() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, target, effect.condition()));
    }

    /** Returns whether a spell or ability targeting the given creature has its damage prevented there. */
    public boolean isDamageFromTargetingSpellOrAbilityPrevented(GameData gameData, StackEntry entry,
                                                                 Permanent target) {
        if (!isDamagePreventable(gameData) || entry == null || target == null || entry.isNonTargeting()
                || !gameData.hasFloatingEffectOnPermanent(target.getId(),
                PreventDamageToTargetCreatureFromTargetingSpellOrAbilityEffect.class)) {
            return false;
        }
        UUID targetId = target.getId();
        return targetId.equals(entry.getTargetId()) || entry.getTargetIds().contains(targetId);
    }

    /**
     * Benevolent Unicorn: total amount by which damage dealt by {@code entry} â€” a spell dealing
     * damage as itself â€” is reduced by {@link ReduceSpellDamageEffect} permanents on any
     * battlefield. Returns 0 for abilities and for combat damage. This is a replacement effect,
     * so it is not gated on {@link #isDamagePreventable}.
     */
    public int getSpellDamageReduction(GameData gameData, StackEntry entry) {
        if (entry == null) {
            return 0;
        }
        StackEntryType type = entry.getEntryType();
        if (type == StackEntryType.TRIGGERED_ABILITY || type == StackEntryType.ACTIVATED_ABILITY) {
            return 0;
        }
        int[] total = {0};
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent p : battlefield) {
                if (hasLostAllAbilities(gameData, p)) {
                    continue;
                }
                for (CardEffect effect : p.getCard().getStaticEffects()) {
                    if (effect instanceof ReduceSpellDamageEffect reduce) {
                        total[0] += reduce.amount();
                    }
                }
            }
        });
        return total[0];
    }

    /**
     * Applies global damage replacement effects to one damage event. The amount is passed through
     * each matching effect once, so a replacement can make the event too small for a later copy to
     * apply.
     */
    public int applyDamageReplacementEffects(GameData gameData, int damage) {
        return applyDamageReplacementEffects(gameData, null, null, damage);
    }

    /** Applies global damage replacements, including replacements restricted to a player recipient. */
    public int applyDamageReplacementEffects(GameData gameData, StackEntry entry,
                                             UUID recipientPlayerId, int damage) {
        if (damage <= 0) {
            return damage;
        }
        int[] result = {damage};
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                    if (effect instanceof HalveAllDamageEffect
                            && !hasLostAllAbilities(gameData, permanent)
                            && !gameData.removesOwnStaticAbility(permanent.getId(), effect.getClass())) {
                        result[0] /= 2;
                    }
                    if (recipientPlayerId != null && recipientPlayerId.equals(controllerId)
                            && effect instanceof PlayerDamageReplacementEffect replacement
                            && !hasLostAllAbilities(gameData, permanent)
                            && !gameData.removesOwnStaticAbility(permanent.getId(), effect.getClass())) {
                        result[0] = replacement.replaceDamage(entry, result[0]);
                    }
                    if (effect instanceof ReplaceDamageAboveThresholdEffect replacement
                            && !hasLostAllAbilities(gameData, permanent)
                            && !gameData.removesOwnStaticAbility(permanent.getId(), effect.getClass())
                            && result[0] >= replacement.threshold()) {
                        result[0] = replacement.replacementDamage();
                    }
                }
            }
        });
        for (FloatingContinuousEffect floatingReplacement
                : gameData.floatingEffectsOfTypes(ReplaceDamageAboveThresholdEffect.class)) {
            var replacement = (ReplaceDamageAboveThresholdEffect) floatingReplacement.effect();
            if (result[0] >= replacement.threshold()) {
                result[0] = replacement.replacementDamage();
            }
        }
        return result[0];
    }

    public int applyPlaneswalkerLoyaltyDamageReplacement(GameData gameData, Permanent target, int damage) {
        return loyaltyDamageReplacementHandlerRegistry.apply(gameData, target, damage);
    }

    /** Applies Ojer Axonil's replacement to qualifying damage dealt to an opponent. */
    public int applyOjerAxonilDamageReplacement(GameData gameData, int damage,
                                                Set<CardColor> sourceColors,
                                                UUID sourceControllerId, UUID recipientPlayerId) {
        if (damage <= 0 || sourceControllerId == null || recipientPlayerId == null
                || recipientPlayerId.equals(sourceControllerId)
                || sourceColors == null || !sourceColors.contains(CardColor.RED)) {
            return damage;
        }
        int[] result = {damage};
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            if (!sourceControllerId.equals(controllerId)) {
                return;
            }
            for (Permanent permanent : battlefield) {
                if (gameData.losesAllAbilitiesByEffect(permanent)) {
                    continue;
                }
                StaticBonus bonus = computeStaticBonus(gameData, permanent);
                if (bonus.losesAllAbilities() || bonus.losesAllNonManaAbilities()) {
                    continue;
                }
                for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                    if (effect instanceof OjerAxonilDamageReplacementEffect
                            && !gameData.removesOwnStaticAbility(permanent.getId(), effect.getClass())) {
                        int power = getEffectivePower(gameData, permanent);
                        if (power > result[0]) {
                            result[0] = power;
                        }
                    }
                }
            }
        });
        return result[0];
    }

    /**
     * Returns {@code true} if the player controls a permanent with
     * {@link AllowExtraLoyaltyActivationEffect}, or has it as a floating effect ("twice this turn", Urza Assembles the
     * Titans), allowing planeswalker loyalty abilities to be activated twice per turn instead of once (Oath of Teferi).
     */
    public boolean hasExtraLoyaltyActivation(GameData gameData, UUID playerId) {
        return gameData.hasFloatingEffectOnPlayer(playerId, AllowExtraLoyaltyActivationEffect.class)
                || playerBattlefieldHasStaticEffect(gameData, playerId, AllowExtraLoyaltyActivationEffect.class);
    }

    public boolean hasExtraBoastActivation(GameData gameData, UUID playerId) {
        return java.util.Objects.equals(gameData.activePlayerId, playerId)
                && playerBattlefieldHasStaticEffect(gameData, playerId, AllowExtraBoastActivationEffect.class);
    }

    /** Returns the number of additional activations granted to power-up abilities by permanents
     * the player controls. */
    public int countExtraPowerUpActivations(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.isFaceDown() || hasLostAllAbilities(gameData, permanent)) {
                continue;
            }
            if (hasActiveStaticEffect(gameData, permanent, AllowExtraPowerUpActivationEffect.class)) {
                count++;
            }
        }
        return count;
    }

    /** Returns whether a global static effect locks the given planeswalker's loyalty abilities. */
    public boolean isPlaneswalkerLoyaltyAbilityLocked(GameData gameData, Permanent permanent) {
        if (!isPlaneswalker(gameData, permanent)) {
            return false;
        }
        return gameData.anyPermanentMatches(source -> !hasLostPrintedAbilities(gameData, source)
                && source.getCard().getStaticEffects().stream()
                .anyMatch(PlaneswalkerLoyaltyAbilitiesCantBeActivatedEffect.class::isInstance));
    }

    public boolean allowsInstantSpeedLoyaltyActivation(GameData gameData, Permanent permanent) {
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        if (controllerId == null) {
            return false;
        }
        boolean grantedByPermanent = permanent.getCard().getStaticEffects().stream()
                .map(effect -> staticEffectConditionResolver.resolve(
                        gameData, permanent, controllerId, effect))
                .anyMatch(AllowLoyaltyActivationAtInstantSpeedEffect.class::isInstance)
                || playerEmblemHasActiveStaticEffect(
                        gameData, controllerId, AllowLoyaltyActivationAtInstantSpeedEffect.class);
        return grantedByPermanent;
    }

    /**
     * Returns whether the player may activate an exhaust ability as though it has not been
     * activated, as long as this is that player's turn and they have not begun another exhaust
     * activation this turn.
     */
    public boolean canActivateExhaustAbilityAsThoughNotActivated(GameData gameData, UUID playerId) {
        return playerId.equals(gameData.activePlayerId)
                && !gameData.playersWhoActivatedExhaustAbilityThisTurn.contains(playerId)
                && playerBattlefieldHasStaticEffect(gameData, playerId, AllowExtraExhaustActivationEffect.class);
    }

    /**
     * Returns {@code true} if the given card cannot be countered, either because it has
     * its own "can't be countered" ability ({@link CantBeCounteredEffect}), because it was
     * individually made uncounterable while on the stack (e.g. Vexing Shusher), or because
     * its controller has turn-scoped protection for creature spells, or because
     * a {@link ControllerSpellsCantBeCounteredEffect} on the battlefield protects it.
     */
    /** Whether abilities on the stack can't be countered (Spider-Punk's "Spells and abilities can't be countered"). */
    public boolean areAbilitiesUncounterable(GameData gameData) {
        return anyBattlefieldHasStaticEffect(gameData, SpellsAndAbilitiesCantBeCounteredEffect.class);
    }

    public boolean isUncounterable(GameData gameData, Card printedCard) {
        StackEntry spell = findStackEntryByCardId(gameData, printedCard.getId());
        if (anyBattlefieldHasStaticEffect(gameData, SpellsAndAbilitiesCantBeCounteredEffect.class)) {
            return true;
        }
        boolean faceDown = spell != null && spell.isCastFaceDown();
        // A face-down spell has no text, and protection for spells of a color, type, power or mana value reads the
        // face-down spell's (CR 708.2, 708.4).
        Card card = faceDown ? CastingPermissionService.spellAsEffectsSeeIt(spell).getCard() : printedCard;
        for (CardEffect effect : card.getStaticEffects()) {
            if (effect instanceof CantBeCounteredEffect cbc && isCantBeCounteredActive(gameData, card, cbc)) {
                return true;
            }
        }
        if (gameData.spellsMadeUncounterable.contains(card.getId())) {
            return true;
        }
        StackEntry stackEntry = gameData.stack.stream()
                .filter(entry -> entry.getTargetableId().equals(card.getId())
                        && isSpellStackEntry(entry.getEntryType()))
                .findFirst()
                .orElse(null);
        if (stackEntry != null
                && gameData.hasFloatingEffectOnPlayer(stackEntry.getControllerId(),
                        GrantControllerSpellsCantBeCounteredThisTurnEffect.class)) {
            return true;
        }
        if (stackEntry != null
                && hasCardType(card, CardType.CREATURE)
                && gameData.hasFloatingEffectOnPlayer(stackEntry.getControllerId(),
                GrantControllerCreatureSpellsCantBeCounteredThisTurnEffect.class)) {
            return true;
        }
        return spellsCantBeCounteredProtects(gameData, card, stackEntry != null ? stackEntry : spell);
    }

    /**
     * Whether an active {@link ControllerSpellsCantBeCounteredEffect} protects {@code card}: its players name the
     * spell's controller (its owner when it is on no stack entry) and the spell matches its restrictions. Only active
     * static abilities count, so a face-down permanent or one that lost its abilities protects nothing (CR 708.2,
     * 613.1f).
     */
    private boolean spellsCantBeCounteredProtects(GameData gameData, Card card, StackEntry stackEntry) {
        UUID controllerId = stackEntry != null ? stackEntry.getControllerId() : card.getOwnerId();
        boolean creatureSpell = hasCardType(card, CardType.CREATURE);
        int manaValue = StackEntry.spellManaValue(card, stackEntry == null ? 0 : stackEntry.getXValue());
        for (ActiveStaticEffect<ControllerSpellsCantBeCounteredEffect> active
                : activeStaticEffects(gameData, ControllerSpellsCantBeCounteredEffect.class)) {
            ControllerSpellsCantBeCounteredEffect effect = active.effect();
            if (SubjectResolutionService.namesPlayer(active.source(), active.controllerId(), controllerId,
                    effect.players())
                    && (!effect.castOnly() || stackEntry != null && !stackEntry.isCopy())
                    && (!effect.noncreatureOnly() || !creatureSpell)
                    && (effect.cardTypes().isEmpty() || effect.cardTypes().stream().anyMatch(card::hasType))
                    && (effect.predicate() == null || predicateEvaluationService.matchesCardPredicate(
                            card, effect.predicate(), null, gameData, controllerId))
                    && (effect.minimumManaValue() == null || manaValue >= effect.minimumManaValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpellStackEntry(StackEntryType entryType) {
        return entryType == StackEntryType.CREATURE_SPELL
                || entryType == StackEntryType.ENCHANTMENT_SPELL
                || entryType == StackEntryType.SORCERY_SPELL
                || entryType == StackEntryType.INSTANT_SPELL
                || entryType == StackEntryType.ARTIFACT_SPELL
                || entryType == StackEntryType.PLANESWALKER_SPELL
                || entryType == StackEntryType.BATTLE_SPELL;
    }

    /**
     * An unconditional {@link CantBeCounteredEffect} always applies. A conditional one
     * (Banefire's "If X is 5 or more") applies only while its condition holds, evaluated against
     * the spell's stack entry â€” the only place a spell is ever the target of a counter.
     */
    private boolean isCantBeCounteredActive(GameData gameData, Card card, CantBeCounteredEffect effect) {
        if (effect.condition() == null) {
            return true;
        }
        StackEntry entry = findStackEntryByCardId(gameData, card.getId());
        return entry != null
                && conditionEvaluationService.isMet(gameData, effect.condition(), ConditionContext.forStackEntry(entry));
    }

    /**
     * Whether an entering creature is prevented from causing triggered abilities to trigger by a
     * {@link CreatureEnteringDontCauseTriggersEffect} naming every player's abilities whose types include creature
     * (Torpor Orb, Doorkeeper Thrull).
     */
    public boolean areCreatureETBTriggersSuppressed(GameData gameData, Card enteringCard) {
        if (!wouldEnterAsCreature(gameData, enteringCharacteristics(gameData, enteringCard))) {
            return false;
        }
        return anyActiveTriggerSuppression(gameData, suppression ->
                suppression.event() == CreatureEnteringDontCauseTriggersEffect.Event.ENTERING
                        && suppression.appliesToEveryPlayersAbilities()
                        && (suppression.types().isEmpty() || suppression.types().contains(CardType.CREATURE)));
    }

    /**
     * The characteristics {@code enteringCard} has on the battlefield for an entry rule to judge: those its face-down
     * status lists when it entered face down (CR 708.2), else the card's.
     */
    private static Card enteringCharacteristics(GameData gameData, Card enteringCard) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.isFaceDown() && permanent.getCard().getId().equals(enteringCard.getId())) {
                    return PermanentCopierService.faceDownCopiableCard(permanent);
                }
            }
        }
        return enteringCard;
    }

    /**
     * Whether an entering permanent is prevented from causing triggered abilities to trigger by a
     * {@link CreatureEnteringDontCauseTriggersEffect} naming every player's abilities whose types it has, a creature
     * judged as it would enter (Torpor Orb, Doorkeeper Thrull, Hushbringer).
     */
    public boolean areArtifactOrCreatureETBTriggersSuppressed(GameData gameData, Card enteringCard) {
        if (!anyBattlefieldPrintsStaticEffect(gameData, CreatureEnteringDontCauseTriggersEffect.class)) {
            return false;
        }
        Card entered = enteringCharacteristics(gameData, enteringCard);
        boolean creature = wouldEnterAsCreature(gameData, entered);
        return anyActiveTriggerSuppression(gameData, suppression ->
                suppression.event() == CreatureEnteringDontCauseTriggersEffect.Event.ENTERING
                        && suppression.appliesToEveryPlayersAbilities()
                        && (suppression.types().isEmpty()
                        || suppression.types().stream().anyMatch(type -> type == CardType.CREATURE
                        ? creature : hasCardType(entered, type))));
    }

    /**
     * Returns {@code true} if the given creature's death is prevented from causing triggered
     * abilities by a static effect such as Hushbringer. Last-known permanents in a simultaneous
     * death batch are included because one of them may be the source of the suppression.
     */
    public boolean areCreatureDeathTriggersSuppressed(GameData gameData, Permanent dyingPermanent) {
        if (gameData == null || dyingPermanent == null) {
            return false;
        }
        boolean creatureInDeathBatch =
                gameData.simultaneousDyingCreatures.containsKey(dyingPermanent.getId());
        if (!creatureInDeathBatch && !isCreature(gameData, dyingPermanent)) {
            return false;
        }
        Predicate<CardEffect> dying = effect -> effect instanceof CreatureEnteringDontCauseTriggersEffect suppression
                && suppression.event() == CreatureEnteringDontCauseTriggersEffect.Event.DYING;
        if (anyActiveTriggerSuppression(gameData, dying::test)) {
            return true;
        }
        return gameData.simultaneousDyingCreatures.values().stream()
                .filter(permanent -> !gameData.losesAllAbilitiesByEffect(permanent))
                .anyMatch(permanent -> permanent.getCard().getStaticEffects().stream().anyMatch(dying));
    }

    /**
     * Returns whether entering permanents are prevented from causing triggered abilities of
     * permanents controlled by {@code abilityControllerId} to trigger by a
     * {@link CreatureEnteringDontCauseTriggersEffect} that names that player's abilities but not every player's
     * (Elesh Norn, Mother of Machines: its controller's opponents).
     */
    public boolean areOpponentPermanentETBTriggersSuppressed(GameData gameData, UUID abilityControllerId) {
        if (gameData == null || abilityControllerId == null
                || !anyBattlefieldPrintsStaticEffect(gameData, CreatureEnteringDontCauseTriggersEffect.class)) {
            return false;
        }
        for (UUID controllerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
                if (permanent.isFaceDown()) {
                    continue;
                }
                boolean names = permanent.getCard().getStaticEffects().stream().anyMatch(effect ->
                        effect instanceof CreatureEnteringDontCauseTriggersEffect suppression
                                && suppression.event() == CreatureEnteringDontCauseTriggersEffect.Event.ENTERING
                                && !suppression.appliesToEveryPlayersAbilities()
                                && SubjectResolutionService.namesPlayer(gameData, permanent, controllerId,
                                abilityControllerId, suppression.abilityControllers()));
                if (names && !hasLostPrintedAbilities(gameData, permanent)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether a battlefield permanent that is face up and has its printed abilities has a
     * {@link CreatureEnteringDontCauseTriggersEffect} matching {@code matches} (CR 708.2a, 613.1f).
     */
    private boolean anyActiveTriggerSuppression(GameData gameData,
                                                Predicate<CreatureEnteringDontCauseTriggersEffect> matches) {
        if (!anyBattlefieldPrintsStaticEffect(gameData, CreatureEnteringDontCauseTriggersEffect.class)) {
            return false;
        }
        return gameData.anyPermanentMatches(permanent -> !permanent.isFaceDown()
                && permanent.getCard().getStaticEffects().stream().anyMatch(effect ->
                effect instanceof CreatureEnteringDontCauseTriggersEffect suppression && matches.test(suppression))
                && !hasLostPrintedAbilities(gameData, permanent));
    }

    /**
     * Returns the number of extra times a triggered ability of a permanent controlled by
     * {@code sourceControllerId} should fire when {@code enteringPermanent} enters.
     */
    public int countETBExtraTriggers(GameData gameData, UUID sourceControllerId,
                                     UUID enteringControllerId, Card enteringPermanent) {
        if (gameData.playerBattlefields.get(sourceControllerId) == null) return 0;
        Permanent entering = gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getId().equals(enteringPermanent.getId()))
                .findFirst().orElse(null);
        Card enteringCharacteristics = enteringPermanent;
        if (entering != null) {
            enteringCharacteristics = enteringPermanent.createRuntimeCopy();
            Set<CardType> effectiveTypes = getEffectiveCardTypes(gameData, entering);
            enteringCharacteristics.setType(effectiveTypes.iterator().next());
            enteringCharacteristics.setAdditionalTypes(effectiveTypes);
            List<CardSubtype> effectiveSubtypes = new ArrayList<>(effectiveCreatureSubtypes(gameData, entering));
            enteringPermanent.getSubtypes().stream()
                    .filter(subtype -> !isCreatureSubtype(subtype))
                    .filter(subtype -> hasEffectiveSubtype(gameData, entering, subtype))
                    .forEach(effectiveSubtypes::add);
            enteringCharacteristics.setSubtypes(effectiveSubtypes);
        }
        int count = 0;
        for (ActiveStaticEffect<ETBDoubleTriggerEffect> active
                : activeStaticEffects(gameData, sourceControllerId, ETBDoubleTriggerEffect.class)) {
            ETBDoubleTriggerEffect etb = active.effect();
            if ((!etb.requiresEnteringControllerMatch() || sourceControllerId.equals(enteringControllerId))
                    && predicateEvaluationService.matchesCardPredicate(enteringCharacteristics, etb.predicate(), null)) {
                count++;
            }
        }
        return count;
    }

    public int countETBExtraTriggers(GameData gameData, UUID controllerId, Card enteringPermanent) {
        return countETBExtraTriggers(gameData, controllerId, controllerId, enteringPermanent);
    }

    public int countETBExtraTriggersForAnyPermanent(GameData gameData, UUID sourceControllerId) {
        return (int) activeStaticEffects(gameData, sourceControllerId, ETBDoubleTriggerEffect.class).stream()
                .filter(active -> !active.effect().requiresEnteringControllerMatch())
                .count();
    }

    /** Returns conditional additional land plays from static abilities, capped to avoid overflow. */
    public int getConditionalAdditionalLandPlays(GameData gameData, UUID playerId) {
        return landPlayPermissionService.getMaxLandsThisTurn(gameData, playerId)
                - gameData.getMaxLandsThisTurn(playerId);
    }

    /**
     * Returns the number of additional copies for a triggered ability sourced by a permanent.
     * Each matching static effect contributes one copy. By default, a static effect does not apply
     * to the permanent carrying that effect and only static effects controlled by the triggering
     * ability's controller are considered; an effect can opt into either exception explicitly.
     */
    public int countAdditionalTriggeredAbilityTriggers(GameData gameData, UUID controllerId,
                                                        Permanent triggeringPermanent) {
        return countAdditionalTriggeredAbilityTriggers(gameData, controllerId, triggeringPermanent, false);
    }

    /**
     * Returns one additional spell copy for each active spell-copy replacement effect controlled by
     * {@code copyControllerId}; a copy-replacement effect only applies to its own controller's copies.
     */
    public int countAdditionalSpellCopies(GameData gameData, UUID copyControllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(copyControllerId);
        if (battlefield == null) return 0;
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameData.losesAllAbilitiesByEffect(permanent)) continue;
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                if (effect instanceof AdditionalSpellCopyEffect) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Returns the number of additional copies for a triggered ability sourced by a permanent,
     * optionally restricting matching static effects to triggers directly caused by an attack.
     */
    public int countAdditionalTriggeredAbilityTriggers(GameData gameData, UUID controllerId,
                                                        Permanent triggeringPermanent,
                                                        boolean attackTrigger) {
        return countAdditionalTriggeredAbilityTriggers(
                gameData, controllerId, triggeringPermanent, attackTrigger, null);
    }

    /**
     * Returns the number of additional times a triggered ability sourced by {@code triggeringPermanent} triggers
     * (CR 603.2d), {@code cause} being what caused it to trigger as the effects that judge one read it ({@code null}
     * when none does).
     */
    public int countAdditionalTriggeredAbilityTriggers(GameData gameData, UUID controllerId,
                                                        Permanent triggeringPermanent,
                                                        boolean attackTrigger,
                                                        TriggerCause cause) {
        if (controllerId == null || triggeringPermanent == null) return 0;
        int count = 0;
        // A creature its controller controls becoming the target, for an ability of a permanent that player controls.
        if (cause instanceof TriggerCause.BecameTarget target && target.target() != null
                && controllerId.equals(target.controllerId())) {
            count += countAdditionalTriggeredAbilityTriggersForAllyCreatureBecomesTarget(gameData,
                    target.controllerId(), target.target(), target.target());
        }
        boolean creatureDying = cause != null && cause.creatureDying();
        Map<UUID, List<Permanent>> staticSources = new HashMap<>();
        gameData.playerBattlefields.forEach((playerId, battlefield) ->
                staticSources.put(playerId, new ArrayList<>(battlefield)));
        if (creatureDying) {
            Set<UUID> liveIds = staticSources.values().stream().flatMap(List::stream)
                    .map(Permanent::getId).collect(java.util.stream.Collectors.toSet());
            gameData.simultaneousDyingCreatures.forEach((id, permanent) -> {
                UUID owner = gameData.simultaneousDyingControllers.get(id);
                if (owner != null && !liveIds.contains(id)) {
                    staticSources.computeIfAbsent(owner, ignored -> new ArrayList<>()).add(permanent);
                }
            });
        }
        for (Map.Entry<UUID, List<Permanent>> sources : staticSources.entrySet()) {
            UUID staticControllerId = sources.getKey();
            for (Permanent staticSource : sources.getValue()) {
                for (CardEffect effect : getActiveStaticEffects(gameData, staticSource)) {
                    if (!(effect instanceof AdditionalTriggeredAbilityEffect additional)
                            || additional.allyCreatureBecomesTarget()
                            || (additional.combatDamageToPlayerOnly()
                            && !matchesCombatDamageToPlayerTrigger(gameData, controllerId, cause))
                            || (additional.attackOnly()
                            && (!attackTrigger
                            || (additional.controllerAttackersOnly()
                            && !staticControllerId.equals(gameData.activePlayerId))))
                            || (additional.instantSorceryCastOrCopyOnly()
                            && !matchesInstantSorceryCastOrCopy(cause, staticControllerId))
                            || (additional.permanentTurnsFaceUpOnly()
                            && !(cause instanceof TriggerCause.TurnedFaceUp))
                            || (additional.drawCauseOnly() && !(cause instanceof TriggerCause.Drew))
                            || (additional.controlledCreatureDealtDamageOnly()
                            && !controlledCreatureWasDealtDamage(gameData, controllerId, cause))
                            || (!additional.allControllers() && !staticControllerId.equals(controllerId))
                            || (!additional.includeSourcePermanent()
                            && staticSource.getId().equals(triggeringPermanent.getId()))) {
                        continue;
                    }
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceControllerId(staticControllerId)
                            .withSourcePermanentId(staticSource.getId())
                            .withSourcePermanentSnapshot(staticSource);
                    if (creatureDying) {
                        filterContext = filterContext
                                .withTriggeringPermanentId(triggeringPermanent.getId())
                                .withTriggeringPermanentControllerId(controllerId);
                    }
                    if (predicateEvaluationService.matchesPermanentPredicate(
                            triggeringPermanent, additional.sourcePredicate(), filterContext)
                            && (additional.condition() == null
                            || conditionEvaluationService.isMet(gameData, additional.condition(),
                            ConditionContext.forStaticEffect(staticSource, staticControllerId)))) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Whether {@code cause} is combat damage a creature {@code controllerId} controls dealt to a player: the source as
     * it is now, or its card once it has left, and its controller as it dealt the damage, or as it is now when the
     * cause didn't record one.
     */
    private boolean matchesCombatDamageToPlayerTrigger(GameData gameData, UUID controllerId, TriggerCause cause) {
        if (!(cause instanceof TriggerCause.Damage damage) || damage.combatDamageToPlayer() == null) {
            return false;
        }
        TriggerCause.CombatDamageToPlayer toPlayer = damage.combatDamageToPlayer();
        if (!toPlayer.creatureSource()) {
            return controllerId.equals(toPlayer.sourceControllerId());
        }
        if (toPlayer.sourceControllerId() == null) {
            Permanent source = findPermanentById(gameData, toPlayer.sourcePermanentId());
            return source != null
                    && isCreature(gameData, source)
                    && controllerId.equals(findPermanentController(gameData, source.getId()));
        }
        return controllerId.equals(toPlayer.sourceControllerId())
                && sourceIsCreature(gameData, toPlayer.sourceCard(), toPlayer.sourcePermanentId());
    }

    /** Whether {@code cause} is damage dealt to a creature {@code controllerId} controls. */
    private boolean controlledCreatureWasDealtDamage(GameData gameData, UUID controllerId, TriggerCause cause) {
        if (controllerId == null || !(cause instanceof TriggerCause.Damage damage)) return false;
        return damage.damaged().stream().anyMatch(damaged -> isControlledCreature(gameData, controllerId,
                damaged.permanent() != null ? damaged.permanent()
                        : damaged.permanentId() == null ? null : findPermanentById(gameData, damaged.permanentId()),
                damaged.controllerId()));
    }

    private boolean sourceIsCreature(GameData gameData, Card sourceCard, UUID sourcePermanentId) {
        Permanent source = sourcePermanentId == null ? null : findPermanentById(gameData, sourcePermanentId);
        return source != null ? isCreature(gameData, source)
                : sourceCard != null && sourceCard.hasType(CardType.CREATURE);
    }

    private boolean isControlledCreature(GameData gameData, UUID controllerId,
                                         Permanent permanent, UUID capturedControllerId) {
        if (permanent == null || !isCreature(gameData, permanent)) return false;
        UUID actualControllerId = capturedControllerId != null
                ? capturedControllerId : findPermanentController(gameData, permanent.getId());
        return controllerId.equals(actualControllerId);
    }

    private boolean matchesInstantSorceryCastOrCopy(TriggerCause cause, UUID controllerId) {
        return cause instanceof TriggerCause.SpellCastOrCopied spell
                && controllerId.equals(spell.playerId())
                && isInstantOrSorcery(spell.spell());
    }

    private boolean isInstantOrSorcery(Card card) {
        return card != null && (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY));
    }

    /**
     * Returns the number of additional copies for a triggered ability caused by a creature you
     * control becoming the target of a spell or ability.
     */
    public int countAdditionalTriggeredAbilityTriggersForAllyCreatureBecomesTarget(
            GameData gameData, UUID creatureControllerId, Permanent targetCreature,
            Permanent triggeringPermanent) {
        if (creatureControllerId == null || targetCreature == null || triggeringPermanent == null
                || !targetCreature.getCard().hasType(CardType.CREATURE)) {
            return 0;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) {
            return 0;
        }

        int count = 0;
        for (Permanent staticSource : battlefield) {
            if (gameData.losesAllAbilitiesByEffect(staticSource)) {
                continue;
            }
            for (CardEffect effect : staticSource.getCard().getStaticEffects()) {
                if (!(effect instanceof AdditionalTriggeredAbilityEffect additional)
                        || !additional.allyCreatureBecomesTarget()
                        || (!additional.includeSourcePermanent()
                        && staticSource.getId().equals(triggeringPermanent.getId()))) {
                    continue;
                }
                FilterContext filterContext = FilterContext.of(gameData)
                        .withSourceControllerId(creatureControllerId)
                        .withSourcePermanentId(staticSource.getId())
                        .withSourcePermanentSnapshot(staticSource);
                if (predicateEvaluationService.matchesPermanentPredicate(
                        triggeringPermanent, additional.sourcePredicate(), filterContext)
                        && (additional.condition() == null
                        || conditionEvaluationService.isMet(gameData, additional.condition(),
                        ConditionContext.forStaticEffect(staticSource, creatureControllerId)))) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Returns the number of additional copies for a triggered ability caused by a creature dying.
     * Sources that are dying simultaneously are included because their static abilities apply to
     * the event using last-known information.
     */
    public int countAdditionalCreatureDeathTriggeredAbilityTriggers(GameData gameData, UUID controllerId,
                                                                      Permanent triggeringPermanent) {
        if (controllerId == null || triggeringPermanent == null) return 0;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceControllerId(controllerId);
        Set<UUID> checkedSources = new HashSet<>();
        int count = 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent staticSource : battlefield) {
                if (!checkedSources.add(staticSource.getId())) continue;
                count += countAdditionalCreatureDeathEffects(staticSource, triggeringPermanent,
                        filterContext.withSourcePermanentSnapshot(staticSource)
                                .withSourcePermanentId(staticSource.getId()));
            }
        }

        for (Map.Entry<UUID, Permanent> entry : gameData.simultaneousDyingCreatures.entrySet()) {
            if (!controllerId.equals(gameData.simultaneousDyingControllers.get(entry.getKey()))) continue;
            if (!checkedSources.add(entry.getKey())) continue;
            Permanent staticSource = entry.getValue();
            count += countAdditionalCreatureDeathEffects(staticSource, triggeringPermanent,
                    filterContext.withSourcePermanentSnapshot(staticSource)
                            .withSourcePermanentId(staticSource.getId()));
        }

        // An Equipment or Aura grants its creature-death effect to the creature it is attached to, whoever
        // controls the attachment (Masamune controlled by an opponent still doubles its equipped creature's death triggers).
        for (List<Permanent> otherBattlefield : gameData.playerBattlefields.values()) {
            for (Permanent staticSource : otherBattlefield) {
                if (!staticSource.isAttached() || !triggeringPermanent.getId().equals(staticSource.getAttachedTo())) continue;
                if (!checkedSources.add(staticSource.getId())) continue;
                count += countAdditionalCreatureDeathEffects(staticSource, triggeringPermanent,
                        filterContext.withSourcePermanentSnapshot(staticSource)
                                .withSourcePermanentId(staticSource.getId()));
            }
        }
        return count;
    }

    /**
     * Returns the number of additional copies for a creature-death trigger from an emblem. An
     * attached source applies when its equipped creature is controlled by the emblem's controller,
     * including when the source or host is dying simultaneously.
     */
    public int countAdditionalCreatureDeathTriggeredAbilityTriggersForEmblem(GameData gameData,
                                                                               UUID controllerId) {
        if (controllerId == null) return 0;

        Set<UUID> checkedSources = new HashSet<>();
        int count = 0;
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent staticSource : battlefield) {
                if (!checkedSources.add(staticSource.getId())) continue;
                count += countAdditionalEmblemCreatureDeathEffects(gameData, controllerId, staticSource);
            }
        }
        for (Map.Entry<UUID, Permanent> entry : gameData.simultaneousDyingCreatures.entrySet()) {
            if (!checkedSources.add(entry.getKey())) continue;
            count += countAdditionalEmblemCreatureDeathEffects(gameData, controllerId, entry.getValue());
        }
        return count;
    }

    private int countAdditionalCreatureDeathEffects(Permanent staticSource, Permanent triggeringPermanent,
                                                     FilterContext filterContext) {
        if (filterContext.gameData().losesAllAbilitiesByEffect(staticSource)) return 0;

        int count = 0;
        for (CardEffect effect : staticSource.getCard().getStaticEffects()) {
            if (effect instanceof AdditionalCreatureDeathTriggerEffect additional
                    && predicateEvaluationService.matchesPermanentPredicate(
                            triggeringPermanent, additional.sourcePredicate(), filterContext)) {
                count++;
            }
        }
        return count;
    }

    private int countAdditionalEmblemCreatureDeathEffects(GameData gameData, UUID controllerId,
                                                            Permanent staticSource) {
        if (gameData.losesAllAbilitiesByEffect(staticSource)
                || !staticSource.isAttached()) {
            return 0;
        }

        Permanent equippedCreature = findPermanentById(gameData, staticSource.getAttachedTo());
        if (equippedCreature == null) {
            equippedCreature = gameData.simultaneousDyingCreatures.get(staticSource.getAttachedTo());
        }
        if (equippedCreature == null || !isCreature(gameData, equippedCreature)
                || gameData.losesAllAbilitiesByEffect(equippedCreature)) return 0;

        UUID equippedCreatureController = gameData.findControllerOf(equippedCreature);
        if (equippedCreatureController == null) {
            equippedCreatureController = gameData.simultaneousDyingControllers.get(equippedCreature.getId());
        }
        if (!controllerId.equals(equippedCreatureController)) return 0;

        int count = 0;
        for (CardEffect effect : staticSource.getCard().getStaticEffects()) {
            if (effect instanceof AdditionalCreatureDeathTriggerEffect additional
                    && additional.includeOwnedEmblemTriggers()) {
                count++;
            }
        }
        return count;
    }

    // --- Aura & enchantment ---

    /**
     * Returns {@code true} if the permanent is one of the "each other non-Aura enchantment you
     * control" permanents an {@link AnimateControlledEnchantmentsEffect} (Starfield of Nyx) turns
     * into a creature â€” the effect's source must be on the same battlefield and its controller must
     * control enough enchantments. A face-down source has no abilities (CR 708.2) and animates nothing.
     */
    public boolean isAnimatedByStarfield(GameData gameData, Permanent permanent) {
        if (!anyBattlefieldPrintsStaticEffect(gameData, AnimateControlledEnchantmentsEffect.class)) {
            return false;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            int enchantmentCount = -1;
            for (Permanent source : battlefield) {
                if (source.isFaceDown()) continue;
                for (CardEffect effect : source.getCard().getStaticEffects()) {
                    if (!(effect instanceof AnimateControlledEnchantmentsEffect animate)) continue;
                    if (source.getId().equals(permanent.getId())) continue;
                    if (!battlefield.contains(permanent)) continue;
                    if (!isEnchantment(permanent)
                            || permanent.getCard().getSubtypes().contains(CardSubtype.AURA)) {
                        continue;
                    }
                    if (enchantmentCount < 0) {
                        enchantmentCount = (int) battlefield.stream().filter(this::isEnchantment).count();
                    }
                    if (enchantmentCount >= animate.minEnchantments()) return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the given land is animated into a creature by an
     * {@link AllLandsAreCreaturesEffect} on any battlefield. An effect with no required subtype
     * animates every land (Nature's Revolt); an effect with a required land subtype (Living Lands:
     * Forest) animates only lands carrying that subtype, as rewritten by the source's own text changes
     * (Magical Hack: "All Islands"). Global colour-word changes are not applied: they cannot change
     * which lands are animated. A face-down source has no abilities (CR 708.2) and animates nothing.
     */
    private boolean matchesAnimateLand(GameData gameData, Permanent permanent) {
        return anyBattlefieldPrintsStaticEffect(gameData, AllLandsAreCreaturesEffect.class)
                && gameData.anyPermanentMatches(source -> !source.isFaceDown()
                && source.getCard().getStaticEffects().stream()
                        .filter(AllLandsAreCreaturesEffect.class::isInstance)
                        .map(e -> TextChangeTransformer.transform(e, gameData.textReplacements(source.getId())))
                        .anyMatch(e -> e instanceof AllLandsAreCreaturesEffect animateLands
                                && subjectResolutionService.names(gameData, source, null,
                                        sourceControlsPermanent(gameData, source, permanent), permanent,
                                        animateLands.subject(),
                                        SubjectResolutionService.StaticReads.knownLand())
                                && (animateLands.requiredSubtype() == null
                                        || hasEffectiveSubtype(gameData, permanent, animateLands.requiredSubtype()))));
    }

    /**
     * Whether an emblem's static ability names {@code target}, a permanent of the emblem's controller: an emblem has
     * no source permanent, so its sets are the ones it names of its controller's.
     */
    private boolean emblemNames(GameData gameData, Emblem emblem, Permanent target, PermanentSubject subject) {
        return subject instanceof PermanentSubject.ControlledBy
                && subjectResolutionService.names(gameData, null, emblem.controllerId(), emblem.controllerId(),
                target, subject, assemblyReads);
    }

    /**
     * Whether a static ability of {@code source} ({@code null} for one with no source permanent) names {@code target},
     * a permanent on, or entering, the battlefield of {@code targetControllerId}: the resolver's static membership
     * check, its creature part answered by the fully layered query. For a continuous query outside the layered pass.
     */
    public boolean staticSubjectNames(GameData gameData, Permanent source, UUID sourceControllerId,
                                      UUID targetControllerId, Permanent target, PermanentSubject subject) {
        return subjectResolutionService.names(gameData, source, sourceControllerId, targetControllerId, target,
                subject, layeredQueryReads);
    }

    /**
     * {@link #staticSubjectNames(GameData, Permanent, UUID, UUID, Permanent, PermanentSubject)} with the
     * characteristic reads the caller chooses (for a permanent whose type it already judged,
     * {@link SubjectResolutionService.StaticReads#knownCreature()}).
     */
    public boolean staticSubjectNames(GameData gameData, Permanent source, UUID sourceControllerId,
                                      UUID targetControllerId, Permanent target, PermanentSubject subject,
                                      SubjectResolutionService.StaticReads reads) {
        return subjectResolutionService.names(gameData, source, sourceControllerId, targetControllerId, target,
                subject, reads);
    }

    /**
     * Whether a static ability's set names every player's permanents its predicate allows ("each creature"), for a
     * rule that applies to each of them whatever permanent asks (Doran, the Siege Tower's own toughness included).
     */
    private static boolean namesEveryPlayersPermanents(PermanentSubject subject) {
        return subject instanceof PermanentSubject.ControlledBy set
                && set.players() instanceof PlayerSubject.Each each
                && each.relation() == PlayerSubject.Relation.ALL;
    }

    private boolean sourceControlsPermanent(GameData gameData, Permanent source, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(source)) {
                return battlefield.contains(permanent);
            }
        }
        return false;
    }

    /**
     * Smallest "can't be blocked by more than N creatures" cap that applies to {@code attacker},
     * taking both its own static effects (Stalking Tiger) and the Auras attached to it
     * (Alpha Authority) into account. Returns {@link Integer#MAX_VALUE} when unrestricted.
     */
    public int getMaxBlockersAllowed(GameData gameData, Permanent attacker) {
        int maxBlockers = Integer.MAX_VALUE;
        UUID attackerControllerId = findPermanentController(gameData, attacker.getId());
        // An Equipment's "equipped creature can't be blocked by more than N" static belongs to the creature
        // it is attached to (picked up below); it does not restrict the Equipment itself while animated.
        if (!attacker.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            maxBlockers = Math.min(maxBlockers,
                    maxBlockersFromStaticEffects(gameData, attacker, attackerControllerId));
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent aura : battlefield) {
                if (!aura.isAttached() || !attacker.getId().equals(aura.getAttachedTo())) continue;
                UUID auraControllerId = findPermanentController(gameData, aura.getId());
                maxBlockers = Math.min(maxBlockers,
                        maxBlockersFromStaticEffects(gameData, aura, auraControllerId));
            }
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (!attacker.getId().equals(floating.affectedPermanentId())) continue;
                CardEffect floatingEffect = floating.effect() instanceof GrantEffectEffect grant
                        ? grant.effect() : floating.effect();
                if (floatingEffect instanceof CanBeBlockedByAtMostNCreaturesEffect restriction
                        && (!(floating.effect() instanceof GrantEffectEffect)
                        || grantedAbilitySurvivesRemoval(gameData, attacker, floating.timestamp()))) {
                    maxBlockers = Math.min(maxBlockers, restriction.maxBlockers());
                }
            }
        }
        return maxBlockers;
    }

    private int maxBlockersFromStaticEffects(GameData gameData, Permanent source, UUID sourceControllerId) {
        int maxBlockers = Integer.MAX_VALUE;
        for (CardEffect effect : getActiveStaticEffects(gameData, source)) {
            CardEffect activeEffect = sourceControllerId == null
                    ? effect
                    : staticEffectConditionResolver.resolve(gameData, source, sourceControllerId, effect);
            if (activeEffect instanceof CanBeBlockedByAtMostNCreaturesEffect restriction) {
                maxBlockers = Math.min(maxBlockers, restriction.maxBlockers());
            }
        }
        return maxBlockers;
    }

    /**
     * Returns {@code true} if the given permanent has an aura or equipment attached to it
     * that carries a static effect of the given type. Also unwraps
     * {@link EnchantedPermanentConditionalEffect} wrappers: if the currently active
     * inner effect (based on the enchanted permanent predicate) matches, returns {@code true}.
     * An Aura whose effects are being ignored this turn (Volrath's Curse) is skipped entirely.
     */
    public boolean hasAuraWithEffect(GameData gameData, Permanent creature, Class<? extends CardEffect> effectClass) {
        return hasAuraWithEffect(gameData, creature, effectClass::isInstance);
    }

    public boolean isArtifactDamageToEnchantedCreaturePrevented(GameData gameData, Permanent creature,
                                                                 Permanent source, Card sourceCard) {
        boolean artifactSource = source != null
                ? isArtifact(gameData, source)
                : sourceCard != null && sourceCard.hasType(CardType.ARTIFACT);
        return artifactSource
                && hasAuraWithEffect(gameData, creature, effect -> effect instanceof PreventArtifactDamageToSelfEffect prevention
                        && prevention.subject() instanceof PermanentSubject.Attached);
    }

    public boolean isArtifactDamageToSelfPrevented(GameData gameData, Permanent permanent,
                                                    Permanent source, Card sourceCard) {
        boolean artifactSource = source != null
                ? isArtifact(gameData, source)
                : sourceCard != null && sourceCard.hasType(CardType.ARTIFACT);
        return artifactSource
                && hasActiveStaticEffect(gameData, permanent, PreventArtifactDamageToSelfEffect.class,
                        prevention -> prevention.subject() instanceof PermanentSubject.Source);
    }

    /**
     * Predicate-matching form of {@link #hasAuraWithEffect(GameData, Permanent, Class)}, for effects
     * where the class alone is not the question â€” a parameterized restriction such as
     * {@link EnchantedCreatureCantAttackOrBlockEffect} must also be asked which half of it applies.
     * The matcher sees only effects that are actually active, so it never has to repeat the
     * attachment, ignored-Aura or {@link EnchantedPermanentConditionalEffect} handling.
     */
    public boolean hasAuraWithEffect(GameData gameData, Permanent creature, Predicate<CardEffect> effectMatcher) {
        return gameData.anyPermanentMatches(p ->
                p.isAttached() && p.getAttachedTo().equals(creature.getId())
                        && !gameData.ignoresEffectOf(p.getId(), IgnoreSourceAuraEffectsUntilEndOfTurnEffect.class, findPermanentController(gameData, creature.getId()))
                        && p.getCard().getStaticEffects().stream()
                        .anyMatch(e -> isActiveEffect(gameData, p, creature, e, effectMatcher)));
    }

    /**
     * Returns every static effect of the given type carried by an aura or equipment attached to
     * {@code creature}. The instance-level sibling of
     * {@link #hasAuraWithEffect(GameData, Permanent, Class)} for callers that need the effect's own
     * data (e.g. the attacker predicate of a block restriction) and not just its presence.
     * {@link EnchantedPermanentConditionalEffect} wrappers are unwrapped to their currently active
     * inner effect; an Aura whose effects are ignored this turn (Volrath's Curse) is skipped.
     */
    public <T extends CardEffect> List<T> collectAuraEffects(GameData gameData, Permanent creature, Class<T> effectClass) {
        List<T> collected = new ArrayList<>();
        UUID enchantedControllerId = findPermanentController(gameData, creature.getId());
        gameData.forEachPermanent((id, permanent) -> {
            if (!permanent.isAttached() || !creature.getId().equals(permanent.getAttachedTo())
                    || gameData.ignoresEffectOf(permanent.getId(), IgnoreSourceAuraEffectsUntilEndOfTurnEffect.class, enchantedControllerId)) {
                return;
            }
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                CardEffect active = effect;
                if (effect instanceof EnchantedPermanentConditionalEffect cond) {
                    active = matchesEnchantedPermanentPredicate(gameData, permanent, creature, cond.filter())
                            ? cond.ifMatch()
                            : cond.ifNotMatch();
                }
                if (effectClass.isInstance(active)) {
                    collected.add(effectClass.cast(active));
                }
            }
        });
        return collected;
    }

    /**
     * Whether {@code blocker} is required to block {@code attacker} by a Lure-style effect:
     * a resolved "all creatures able to block it do so" effect on the attacker, a static {@link MustBeBlockedByAllCreaturesEffect} on the
     * attacker (optionally filtered), or such an effect on an aura attached to the attacker.
     * A {@code null} {@code blockerFilter} forces every able creature; a non-null filter forces
     * only matching blockers (Talruum Piper: creatures with flying).
     */
    public boolean isRequiredToBlockByLure(GameData gameData, Permanent attacker, Permanent blocker) {
        if (gameData.hasFloatingEffectOnPermanent(attacker.getId(), MustBeBlockedByAllCreaturesEffect.class)) {
            return true;
        }
        if (!hasLostPrintedAbilities(gameData, attacker)) {
            for (CardEffect effect : attacker.getCard().getStaticEffects()) {
                if (effect instanceof EnchantedPermanentConditionalEffect) {
                    continue;
                }
                if (matchesLureBlockerFilter(gameData, attacker, attacker, blocker, effect)) {
                    return true;
                }
            }
        }
        return gameData.anyPermanentMatches(aura ->
                aura.isAttached() && attacker.getId().equals(aura.getAttachedTo())
                        && !hasLostPrintedAbilities(gameData, aura)
                        && aura.getCard().getStaticEffects().stream()
                        .anyMatch(e -> matchesLureBlockerFilter(gameData, attacker, aura, blocker, e)));
    }

    /**
     * Counts the separate "must be blocked by all creatures able" requirements that {@code attacker}
     * places on {@code blocker}. Each effect is its own requirement (CR 509.1c), so two Lures on the same
     * attacker count twice.
     */
    public int lureRequirementCount(GameData gameData, Permanent attacker, Permanent blocker) {
        int count = gameData.floatingEffectsOnPermanent(attacker.getId(), MustBeBlockedByAllCreaturesEffect.class).size();
        if (!hasLostPrintedAbilities(gameData, attacker)) {
            for (CardEffect effect : attacker.getCard().getStaticEffects()) {
                if (!(effect instanceof EnchantedPermanentConditionalEffect)
                        && matchesLureBlockerFilter(gameData, attacker, attacker, blocker, effect)) {
                    count++;
                }
            }
        }
        int[] attached = {0};
        gameData.forEachPermanent((controllerId, aura) -> {
            if (aura.isAttached() && attacker.getId().equals(aura.getAttachedTo())
                    && !hasLostPrintedAbilities(gameData, aura)
                    && aura.getCard().getStaticEffects().stream()
                    .anyMatch(e -> matchesLureBlockerFilter(gameData, attacker, aura, blocker, e))) {
                attached[0]++;
            }
        });
        return Math.max(1, count + attached[0]);
    }

    private boolean matchesLureBlockerFilter(GameData gameData, Permanent attacker, Permanent source,
                                             Permanent blocker, CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional) {
            UUID controllerId = findPermanentController(gameData, source.getId());
            return controllerId != null
                    && conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStaticEffect(source, controllerId))
                    && matchesLureBlockerFilter(gameData, attacker, source, blocker, conditional.wrapped());
        }
        // Gift of the Deity wraps lure in EnchantedPermanentConditionalEffect ("as long as enchanted
        // is green") â€” same unwrap as hasAuraWithEffect / isActiveEffect.
        if (effect instanceof EnchantedPermanentConditionalEffect cond) {
            CardEffect active = matchesEnchantedPermanentPredicate(gameData, source, attacker, cond.filter())
                    ? cond.ifMatch()
                    : cond.ifNotMatch();
            return active != null && matchesLureBlockerFilter(gameData, attacker, source, blocker, active);
        }
        if (!(effect instanceof MustBeBlockedByAllCreaturesEffect lure)) {
            return false;
        }
        return lure.blockerFilter() == null
                || predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, lure.blockerFilter());
    }

    /**
     * Returns the total additional generic mana the controller must pay to declare this creature as an
     * attacker: every {@link CombatTaxKind#ATTACK} aura attached to it (e.g.
     * Brainwash â€” {3}) plus every {@link AttackCostEffect} on the creature itself (e.g. Phyrexian
     * Marauder â€” {1} per +1/+1 counter).
     */
    public int getCreatureAttackTax(GameData gameData, Permanent creature) {
        int total = getEnchantedCreatureAttackTax(gameData, creature);
        StaticBonus bonus = computeStaticBonus(gameData, creature);
        if (hasLostPrintedAbilities(gameData, creature)
                || bonus.losesAllAbilities()
                || bonus.losesAllNonManaAbilities()) {
            return total;
        }
        UUID controllerId = findPermanentController(gameData, creature.getId());
        List<CardEffect> effects = new ArrayList<>(staticEffectsIncludingTemporary(gameData, creature, controllerId));
        effects.addAll(bonus.grantedEffects());
        for (CardEffect effect : effects) {
            if (effect instanceof AttackCostEffect attackCost) {
                total += attackCost.dynamicAttackCost() == null
                        ? attackCost.attackCost(creature)
                        : amountEvaluationService.evaluate(gameData, attackCost.dynamicAttackCost(),
                        AmountContext.forStaticEffect(creature, controllerId));
            }
        }
        return total;
    }

    /**
     * Returns the total additional generic mana the controller must pay to declare this creature as an
     * attacker, summed over every {@link CombatTaxKind#ATTACK} Aura attached to it
     * (e.g. Brainwash â€” {3}).
     */
    public int getEnchantedCreatureAttackTax(GameData gameData, Permanent creature) {
        return getEnchantedCreatureCombatTax(gameData, creature, CombatTaxKind.ATTACK);
    }

    /**
     * Returns the additional generic mana the defending player must pay for each creature they declare as a
     * blocker of this attacking creature, summed over every {@link CombatTaxKind#BE_BLOCKED_BY} Aura
     * attached to it (e.g. Awesome Presence â€” {3}).
     */
    public int getEnchantedCreatureBlockTax(GameData gameData, Permanent creature) {
        return getEnchantedCreatureCombatTax(gameData, creature, CombatTaxKind.BE_BLOCKED_BY);
    }

    /**
     * Returns the additional generic mana the enchanted creature's controller must pay for each
     * block declared by that creature, summed over every {@link CombatTaxKind#BLOCK_WITH} Aura
     * attached to it (e.g. Oppressive Rays â€” {3}).
     */
    public int getEnchantedCreatureBlockerTax(GameData gameData, Permanent creature) {
        return getEnchantedCreatureCombatTax(gameData, creature, CombatTaxKind.BLOCK_WITH);
    }

    /**
     * Total additional generic mana the defending player must pay to declare {@code blocker} as a
     * blocker of {@code attacker}: the Aura taxes on both creatures plus every
     * {@link BlockCostEffect} the blocker carries (Hipparion â€” {1} to block power 3 or greater).
     * The single source of truth for the mana part of a block's additional cost, shared by
     * {@code CombatBlockService.declareBlockers} and the AI so the two can never disagree about
     * whether a block is affordable.
     */
    public int getBlockManaTax(GameData gameData, Permanent blocker, Permanent attacker) {
        int attackerPower = getEffectivePower(gameData, attacker);
        int tax = getEnchantedCreatureBlockTax(gameData, attacker)
                + getEnchantedCreatureBlockerTax(gameData, blocker);
        UUID controllerId = findPermanentController(gameData, blocker.getId());
        for (CardEffect effect : blockCostEffects(gameData, blocker)) {
            if (effect instanceof CantAttackOrBlockUnlessPaysEffect unlessPays && unlessPays.attackerFilter() != null) {
                // "Can't block creatures with power 3 or greater unless you pay": each matching attacker blocked.
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, attacker,
                        unlessPays.attackerFilter())) {
                    tax += amountEvaluationService.evaluate(gameData, unlessPays.amount(),
                            AmountContext.forStaticEffect(blocker, controllerId));
                }
            } else if (effect instanceof BlockCostEffect blockCost) {
                tax += blockCost.dynamicBlockCost() == null
                        ? blockCost.blockCost(blocker, attackerPower)
                        : amountEvaluationService.evaluate(gameData, blockCost.dynamicBlockCost(),
                        AmountContext.forStaticEffect(blocker, controllerId));
            }
        }
        return tax;
    }

    /**
     * Generic mana the defending player must pay once per blocking creature for an unconditional
     * "can't block unless its controller pays" cost ({@link CantAttackOrBlockUnlessPaysEffect},
     * e.g. Qal Sisma Behemoth). {@link #getBlockManaTax} already includes this amount for every
     * attacker a blocker is assigned to, so declaration code subtracts it again for each repeat
     * assignment of the same blocker.
     */
    public int getPerCreatureBlockTax(GameData gameData, Permanent blocker) {
        UUID controllerId = findPermanentController(gameData, blocker.getId());
        int tax = 0;
        for (CardEffect effect : blockCostEffects(gameData, blocker)) {
            if (effect instanceof CantAttackOrBlockUnlessPaysEffect unlessPays && unlessPays.side().blocks()
                    && unlessPays.attackerFilter() == null) {
                tax += amountEvaluationService.evaluate(gameData, unlessPays.amount(),
                        AmountContext.forStaticEffect(blocker, controllerId));
            }
        }
        return tax;
    }

    /**
     * Static and granted effects of {@code blocker} that can contribute block costs; empty when the
     * blocker has lost its printed or all abilities.
     */
    private List<CardEffect> blockCostEffects(GameData gameData, Permanent blocker) {
        StaticBonus bonus = computeStaticBonus(gameData, blocker);
        if (hasLostPrintedAbilities(gameData, blocker)
                || bonus.losesAllAbilities()
                || bonus.losesAllNonManaAbilities()) {
            return List.of();
        }
        UUID controllerId = findPermanentController(gameData, blocker.getId());
        List<CardEffect> effects = new ArrayList<>(staticEffectsIncludingTemporary(gameData, blocker, controllerId));
        effects.addAll(bonus.grantedEffects());
        return effects;
    }

    /**
     * Sums the {@link EnchantedCreatureCombatTaxEffect} amounts of the given {@code kind} across every
     * Aura attached to {@code creature}. Auras carrying a different kind contribute nothing, so a card
     * taxing several actions at once (Oppressive Rays) is charged once per action.
     */
    private int getEnchantedCreatureCombatTax(GameData gameData, Permanent creature, CombatTaxKind kind) {
        int[] total = {0};
        gameData.forEachPermanent((playerId, aura) -> {
            if (!aura.isAttached() || !aura.getAttachedTo().equals(creature.getId())) {
                return;
            }
            for (CardEffect effect : aura.getCard().getStaticEffects()) {
                if (effect instanceof EnchantedCreatureCombatTaxEffect tax && tax.kind() == kind) {
                    total[0] += amountEvaluationService.evaluate(gameData, tax.amount(),
                            AmountContext.forStaticEffect(aura, playerId));
                }
            }
        });
        return total[0];
    }

    /**
     * Life the defending player must pay for this blocker to block this attacker under every
     * board-wide {@link GlobalBlockLifeCostEffect} (Heat Wave). Summed over matching sources;
     * charge once per unique blocker in {@code CombatBlockService.declareBlockers}.
     */
    public int getGlobalBlockLifeTax(GameData gameData, Permanent blocker, Permanent attacker) {
        int[] total = {0};
        gameData.forEachPermanent((playerId, source) -> {
            if (hasLostAllAbilities(gameData, source)) return;
            for (CardEffect effect : source.getCard().getStaticEffects()) {
                if (effect instanceof GlobalBlockLifeCostEffect tax) {
                    FilterContext ctx = FilterContext.of(gameData)
                            .withSourceControllerId(playerId)
                            .withSourceCardId(source.getOriginalCard().getId());
                    if (predicateEvaluationService.matchesPermanentPredicate(
                            blocker, tax.lifeCostBlockerMatcher(), ctx)
                            && predicateEvaluationService.matchesPermanentPredicate(
                            attacker, tax.lifeCostAttackerMatcher(), ctx)) {
                        total[0] += tax.lifePerBlocker();
                    }
                }
            }
        });
        return total[0];
    }

    /**
     * Board-wide generic mana tax to declare {@code blocker} as a blocker at all
     * ({@link RequirePaymentToBlockEffect}: Archangel of Tithes while attacking, printed; War Cadence's, floated on a
     * player with its amount fixed). Charged once per blocking creature regardless of how many attackers it blocks.
     */
    public int getGlobalBlockManaTax(GameData gameData, Permanent blocker) {
        int[] total = {0};
        for (ActiveStaticEffect<RequirePaymentToBlockEffect> active
                : activeStaticEffects(gameData, RequirePaymentToBlockEffect.class)) {
            Permanent source = active.source();
            RequirePaymentToBlockEffect tax = active.effect();
            if (!hasLostAllAbilities(gameData, source)
                    && (tax.activeCondition() == null || conditionEvaluationService.isMet(
                            gameData, tax.activeCondition(), ConditionContext.forPermanent(source, active.controllerId())))) {
                total[0] += amountEvaluationService.evaluate(gameData, tax.amountPerBlocker(),
                        AmountContext.forStaticEffect(source, active.controllerId()));
            }
        }
        for (FloatingContinuousEffect floatingEffect : gameData.floatingEffectsOnNamedPlayers(RequirePaymentToBlockEffect.class)) {
            RequirePaymentToBlockEffect tax = (RequirePaymentToBlockEffect) floatingEffect.effect();
            if (tax.activeCondition() == null) {
                total[0] += amountEvaluationService.evaluate(gameData, tax.amountPerBlocker(),
                        AmountContext.forEstimation(floatingEffect.affectedPlayerId()));
            }
        }
        return total[0];
    }

    private boolean isActiveEffect(GameData gameData, Permanent aura, Permanent creature, CardEffect effect,
                                   Predicate<CardEffect> effectMatcher) {
        if (effectMatcher.test(effect)) return true;
        if (effect instanceof ConditionalEffect conditional) {
            UUID controllerId = findPermanentController(gameData, aura.getId());
            if (controllerId == null || !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStaticEffect(aura, controllerId))) {
                return false;
            }
            return isActiveEffect(gameData, aura, creature, conditional.wrapped(), effectMatcher);
        }
        if (effect instanceof EnchantedPermanentConditionalEffect cond) {
            CardEffect activeEffect = matchesEnchantedPermanentPredicate(gameData, aura, creature, cond.filter())
                    ? cond.ifMatch()
                    : cond.ifNotMatch();
            return activeEffect != null && isActiveEffect(gameData, aura, creature, activeEffect, effectMatcher);
        }
        return false;
    }

    private boolean matchesEnchantedPermanentPredicate(GameData gameData, Permanent aura,
                                                       Permanent creature, PermanentPredicate predicate) {
        UUID controllerId = findPermanentController(gameData, aura.getId());
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(aura.getOriginalCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentSnapshot(aura)
                .withSourcePermanentId(aura.getId());
        return predicateEvaluationService.matchesPermanentPredicate(creature, predicate, context);
    }

    /**
     * Checks whether a permanent has a given subtype without triggering {@code computeStaticBonus}
     * (which would cause infinite recursion). Checks base subtypes and the intrinsic Changeling keyword; continuous
     * effects are the layered pass's business.
     */
    public static boolean permanentHasSubtype(Permanent permanent, CardSubtype subtype) {
        if (permanent.getLastKnownSubtypes() != null) {
            return permanent.getLastKnownSubtypes().contains(subtype);
        }
        return permanent.getCard().getSubtypes().contains(subtype)
                || permanent.hasKeyword(Keyword.CHANGELING);
    }

    /**
     * Returns {@code true} if the two creatures share at least one creature type, accounting for
     * granted/transient/layer subtypes, "loses all creature types", and Changeling (which counts
     * as having every creature type). Backs the "creatures that share no creature types" targeting
     * restriction (Rivals' Duel).
     */
    public boolean shareCreatureType(GameData gameData, Permanent a, Permanent b) {
        boolean aChangeling = hasKeyword(gameData, a, Keyword.CHANGELING);
        boolean bChangeling = hasKeyword(gameData, b, Keyword.CHANGELING);
        Set<CardSubtype> aTypes = effectiveCreatureSubtypes(gameData, a);
        Set<CardSubtype> bTypes = effectiveCreatureSubtypes(gameData, b);
        // A Changeling has every creature type, so it shares a type with any creature that has
        // at least one creature type (or with another Changeling).
        if (aChangeling) {
            return bChangeling || !bTypes.isEmpty();
        }
        if (bChangeling) {
            return !aTypes.isEmpty();
        }
        return aTypes.stream().anyMatch(bTypes::contains);
    }

    /**
     * Returns whether a last-known battlefield permanent shares a creature type with a card in a
     * non-battlefield zone. The permanent side uses its layered creature types; the card side uses
     * its effective creature types in that zone.
     */
    public boolean shareCreatureType(GameData gameData, Permanent permanent, Card card) {
        return shareCreatureType(gameData, permanent, card, card.getOwnerId());
    }

    /** As above, reading the card's effective creature types for the given owner. */
    public boolean shareCreatureType(GameData gameData, Permanent permanent, Card card, UUID cardOwnerId) {
        boolean permanentChangeling = hasKeyword(gameData, permanent, Keyword.CHANGELING);
        boolean cardChangeling = card.hasKeyword(Keyword.CHANGELING);
        Set<CardSubtype> permanentTypes = effectiveCreatureSubtypes(gameData, permanent);
        Set<CardSubtype> cardTypes = getCardSubtypes(card, gameData, cardOwnerId).stream()
                .filter(this::isCreatureSubtype)
                .collect(java.util.stream.Collectors.toSet());
        if (permanentChangeling) {
            return cardChangeling || !cardTypes.isEmpty();
        }
        if (cardChangeling) {
            return !permanentTypes.isEmpty();
        }
        return permanentTypes.stream().anyMatch(cardTypes::contains);
    }

    /**
     * Returns {@code true} if two cards in a non-battlefield zone share at least one creature type,
     * reading each card's effective creature types in that zone, so creature types granted to
     * cards outside the battlefield (such as Maskwood Nexus's) count.
     */
    public boolean shareCreatureType(GameData gameData, Card a, Card b) {
        Set<CardSubtype> aTypes = getCardSubtypes(a, gameData, a.getOwnerId()).stream()
                .filter(this::isCreatureSubtype)
                .collect(java.util.stream.Collectors.toSet());
        Set<CardSubtype> bTypes = getCardSubtypes(b, gameData, b.getOwnerId()).stream()
                .filter(this::isCreatureSubtype)
                .collect(java.util.stream.Collectors.toSet());
        if (a.hasKeyword(Keyword.CHANGELING)) {
            return b.hasKeyword(Keyword.CHANGELING) || !bTypes.isEmpty();
        }
        if (b.hasKeyword(Keyword.CHANGELING)) {
            return !aTypes.isEmpty();
        }
        return aTypes.stream().anyMatch(bTypes::contains);
    }

    /**
     * Returns {@code true} if two cards in non-battlefield zones share at least one creature type.
     * Only printed subtypes and keywords are relevant for cards in a graveyard.
     */
    public boolean shareCreatureType(Card a, Card b) {
        Set<CardSubtype> aTypes = Set.copyOf(a.getSubtypes());
        Set<CardSubtype> bTypes = Set.copyOf(b.getSubtypes());
        boolean aChangeling = a.hasKeyword(Keyword.CHANGELING);
        boolean bChangeling = b.hasKeyword(Keyword.CHANGELING);
        if (aChangeling) {
            return bChangeling || !bTypes.isEmpty();
        }
        if (bChangeling) {
            return !aTypes.isEmpty();
        }
        return aTypes.stream().anyMatch(bTypes::contains);
    }

    /**
     * Returns {@code true} if the two permanents share at least one of the card types artifact,
     * creature, or land (Gauntlets of Chaos' "shares one of those types with it"). Uses each
     * permanent's card types.
     */
    public boolean sharesArtifactCreatureOrLandType(GameData gameData, Permanent a, Permanent b) {
        return (isArtifact(gameData, a) && isArtifact(gameData, b))
                || (isCreature(gameData, a) && isCreature(gameData, b))
                || (isLand(gameData, a) && isLand(gameData, b));
    }

    /**
     * Returns {@code true} if the two permanents share at least one of the card types artifact or
     * creature (Legerdemain's "another target permanent that shares one of those types with it",
     * where "those types" are only artifact and creature). Uses each permanent's card types.
     */
    public boolean sharesArtifactOrCreatureType(GameData gameData, Permanent a, Permanent b) {
        return (isArtifact(gameData, a) && isArtifact(gameData, b))
                || (isCreature(gameData, a) && isCreature(gameData, b));
    }

    /**
     * Returns whether the two permanents share at least one of {@code types} ("shares one of those types with it"):
     * every card type is {@link #sharesCardType}; otherwise each listed type is read as {@link #isCreature} for a
     * creature and {@link #hasEffectiveCardType} for any other.
     */
    public boolean sharesCardTypeAmong(GameData gameData, Permanent a, Permanent b, Set<CardType> types) {
        if (types.containsAll(EnumSet.allOf(CardType.class))) {
            return sharesCardType(gameData, a, b);
        }
        return types.stream().anyMatch(type -> type == CardType.CREATURE
                ? isCreature(gameData, a) && isCreature(gameData, b)
                : hasEffectiveCardType(gameData, a, type) && hasEffectiveCardType(gameData, b, type));
    }

    /** Returns whether two permanents share at least one card type. */
    public boolean sharesCardType(GameData gameData, Permanent a, Permanent b) {
        Set<CardType> aTypes = getEffectiveCardTypes(gameData, a);
        return getEffectiveCardTypes(gameData, b).stream().anyMatch(aTypes::contains);
    }

    /** Returns whether two permanents share at least one permanent type (kindred alone does not count). */
    public boolean sharesPermanentType(GameData gameData, Permanent a, Permanent b) {
        Set<CardType> aTypes = getEffectiveCardTypes(gameData, a);
        return getEffectiveCardTypes(gameData, b).stream()
                .anyMatch(type -> type.isPermanentType() && aTypes.contains(type));
    }

    /** Returns whether two permanents currently have equal effective toughness. */
    public boolean haveEqualToughness(GameData gameData, Permanent a, Permanent b) {
        return getEffectiveToughness(gameData, a) == getEffectiveToughness(gameData, b);
    }

    /**
     * Returns {@code true} if the given card (in a library, hand, or other non-battlefield zone)
     * shares at least one creature type with a creature the given player controls. Changeling on
     * either side counts as every creature type, and the card side honours all-zone subtype grants
     * (Arcane Adaptation) via {@link #cardHasSubtype}. Backs Descendants' Path.
     */
    public boolean cardSharesCreatureTypeWithControlledCreature(GameData gameData, Card card, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        boolean cardChangeling = card.hasKeyword(Keyword.CHANGELING);
        boolean cardHasAnyCreatureType = card.getSubtypes().stream().anyMatch(this::isCreatureSubtype);

        for (Permanent permanent : battlefield) {
            if (!isCreature(gameData, permanent)) {
                continue;
            }
            boolean permanentChangeling = hasKeyword(gameData, permanent, Keyword.CHANGELING);
            Set<CardSubtype> permanentTypes = effectiveCreatureSubtypes(gameData, permanent);
            if (cardChangeling) {
                if (permanentChangeling || !permanentTypes.isEmpty()) {
                    return true;
                }
                continue;
            }
            if (permanentChangeling) {
                if (cardHasAnyCreatureType) {
                    return true;
                }
                continue;
            }
            for (CardSubtype subtype : permanentTypes) {
                if (cardHasSubtype(card, subtype, gameData, card.getOwnerId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns whether a player controls at least {@code minimum} creatures sharing a creature type. */
    public boolean controlsCreaturesSharingCreatureType(GameData gameData, UUID controllerId, int minimum) {
        if (minimum <= 0) return true;
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;

        Set<CardSubtype> allCreatureTypes = EnumSet.noneOf(CardSubtype.class);
        for (CardSubtype subtype : CardSubtype.values()) {
            if (isCreatureSubtype(subtype)) {
                allCreatureTypes.add(subtype);
            }
        }

        Map<CardSubtype, Integer> countsByType = new HashMap<>();
        for (Permanent permanent : battlefield) {
            if (!isCreature(gameData, permanent)) continue;
            Set<CardSubtype> creatureTypes = effectiveCreatureSubtypes(gameData, permanent);
            if (hasKeyword(gameData, permanent, Keyword.CHANGELING)) {
                creatureTypes = allCreatureTypes;
            }
            for (CardSubtype creatureType : creatureTypes) {
                countsByType.merge(creatureType, 1, Integer::sum);
            }
        }
        return countsByType.values().stream().anyMatch(count -> count >= minimum);
    }

    /** Returns the creature subtypes remaining after continuous effects are applied. */
    public Set<CardSubtype> effectiveCreatureSubtypes(GameData gameData, Permanent permanent) {
        CharacteristicState state = LayerSystemService.activeStateFor(permanent.getId());
        if (state != null) {
            Set<CardSubtype> result = new HashSet<>();
            addCreatureSubtypes(result, state.getSubtypes());
            return result;
        }
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            state = pass.board().states().get(permanent.getId());
            Set<CardSubtype> result = new HashSet<>();
            if (state != null) {
                addCreatureSubtypes(result, state.getSubtypes());
            } else {
                for (CardSubtype subtype : CardSubtype.values()) {
                    if (isCreatureSubtype(subtype) && permanentHasSubtype(permanent, subtype)) {
                        result.add(subtype);
                    }
                }
            }
            return result;
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /** Returns whether the permanent currently has the Flagbearer creature subtype. */
    public boolean isFlagbearer(GameData gameData, Permanent permanent) {
        return predicateEvaluationService.matchesPermanentPredicate(gameData, permanent,
                new PermanentHasSubtypePredicate(CardSubtype.FLAGBEARER));
    }

    /** Returns whether the player currently has the Flagbearer quality. */
    public boolean isFlagbearer(GameData gameData, UUID playerId) {
        return gameData.playerIds.contains(playerId)
                && playerBattlefieldHasStaticEffect(gameData, playerId, GrantControllerFlagbearerEffect.class);
    }

    /** Returns whether an opponent of {@code playerId} controls a Flagbearer. */
    public boolean hasFlagbearerTargetRequirementFromOpponent(GameData gameData, UUID playerId) {
        if (gameData.playerIds.stream()
                .filter(opponentId -> !opponentId.equals(playerId))
                .anyMatch(opponentId -> isFlagbearer(gameData, opponentId))) {
            return true;
        }
        for (Map.Entry<UUID, List<Permanent>> entry : gameData.playerBattlefields.entrySet()) {
            if (!entry.getKey().equals(playerId)
                    && entry.getValue().stream().anyMatch(permanent -> {
                        StaticBonus bonus = computeStaticBonus(gameData, permanent);
                        if (gameData.losesAllAbilitiesByEffect(permanent) || bonus.losesAllAbilities()
                                || bonus.losesAllNonManaAbilities()
                                || gameData.removesOwnStaticAbility(permanent.getId(), RequireFlagbearerTargetEffect.class)) {
                            return false;
                        }
                        return staticEffectsIncludingTemporary(gameData, permanent, entry.getKey()).stream()
                                .anyMatch(RequireFlagbearerTargetEffect.class::isInstance)
                                || bonus.grantedEffects().stream()
                                        .anyMatch(RequireFlagbearerTargetEffect.class::isInstance);
                    })) {
                return true;
            }
        }
        return false;
    }

    private void addCreatureSubtypes(Set<CardSubtype> target, Collection<CardSubtype> subtypes) {
        for (CardSubtype subtype : subtypes) {
            if (isCreatureSubtype(subtype)) {
                target.add(subtype);
            }
        }
    }

    /**
     * Returns {@code true} if the given permanent has at least one Equipment attached to it.
     */
    public boolean isEquipped(GameData gameData, Permanent creature) {
        return gameData.anyPermanentMatches(p ->
                p.isAttached() && p.getAttachedTo().equals(creature.getId())
                        && hasEffectiveSubtype(gameData, p, CardSubtype.EQUIPMENT));
    }

    /**
     * Returns whether a permanent is modified: it has a counter, is equipped, or is enchanted by
     * an Aura controlled by its controller. Last-known permanents in a simultaneous death batch
     * are included so death conditions can inspect the state immediately before the batch moved.
     */
    public boolean isModified(GameData gameData, Permanent permanent) {
        return isModified(gameData, permanent, null);
    }

    /** Same as {@link #isModified(GameData, Permanent)}, with a controller fallback for a removed permanent. */
    public boolean isModified(GameData gameData, Permanent permanent, UUID controllerFallbackId) {
        if (permanent == null) {
            return false;
        }
        for (CounterType type : CounterType.values()) {
            if (type != CounterType.ANY && type != CounterType.SILVER
                    && permanent.getCounterCount(type) > 0) {
                return true;
            }
        }
        if (gameData == null) {
            return false;
        }

        if (hasAttachedPermanent(gameData, permanent, CardSubtype.EQUIPMENT, false)) {
            return true;
        }

        UUID controllerId = gameData.findControllerOf(permanent);
        if (controllerId == null) {
            controllerId = gameData.simultaneousDyingPermanentControllers.get(permanent.getId());
        }
        if (controllerId == null) {
            controllerId = controllerFallbackId;
        }
        if (controllerId == null) {
            return false;
        }
        return hasAttachedAuraControlledBy(gameData, permanent, controllerId);
    }

    private boolean hasAttachedPermanent(GameData gameData, Permanent host,
                                         CardSubtype subtype, boolean aura) {
        return gameData.anyPermanentMatches(permanent -> attachedPermanentMatches(permanent, host, subtype, aura))
                || gameData.simultaneousDyingPermanents.values().stream()
                .anyMatch(permanent -> attachedPermanentMatches(permanent, host, subtype, aura));
    }

    private boolean attachedPermanentMatches(Permanent attached, Permanent host,
                                              CardSubtype subtype, boolean aura) {
        return attached.isAttached()
                && host.getId().equals(attached.getAttachedTo())
                && (aura ? attached.getCard().isAura()
                : attached.getCard().getSubtypes().contains(subtype));
    }

    private boolean hasAttachedAuraControlledBy(GameData gameData, Permanent host, UUID controllerId) {
        return gameData.anyPermanentMatches(permanent ->
                attachedAuraControlledBy(permanent, host, controllerId, gameData))
                || gameData.simultaneousDyingPermanents.values().stream()
                .anyMatch(permanent -> attachedAuraControlledBy(permanent, host, controllerId, gameData));
    }

    private boolean attachedAuraControlledBy(Permanent aura, Permanent host,
                                             UUID controllerId, GameData gameData) {
        return aura.isAttached()
                && host.getId().equals(aura.getAttachedTo())
                && aura.getCard().isAura()
                && controllerId.equals(permanentControllerForModifiedCheck(gameData, aura));
    }

    private UUID permanentControllerForModifiedCheck(GameData gameData, Permanent permanent) {
        UUID controllerId = gameData.findControllerOf(permanent);
        return controllerId != null
                ? controllerId
                : gameData.simultaneousDyingPermanentControllers.get(permanent.getId());
    }

    /**
     * Returns {@code true} if the given permanent has at least one aura attached to it.
     */
    public boolean isEnchanted(GameData gameData, Permanent creature) {
        return gameData.anyPermanentMatches(p ->
                p.isAttached() && p.getAttachedTo().equals(creature.getId())
                        && p.getCard().isAura());
    }

    public List<Permanent> findEnchantedCreaturesByAuraEffect(GameData gameData, UUID playerId,
                                                              Predicate<CardEffect> effectMatcher) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return List.of();
        List<Permanent> enchanted = new ArrayList<>();
        for (Permanent p : bf) {
            if (p.isAttached() && p.getCard().getStaticEffects().stream().anyMatch(effectMatcher)) {
                Permanent creature = findPermanentById(gameData, p.getAttachedTo());
                if (creature != null) enchanted.add(creature);
            }
        }
        return enchanted;
    }

    /**
     * Finds the creature that is enchanted by an aura with the given static effect type,
     * searching only the specified player's battlefield. Returns the enchanted creature,
     * or {@code null} if no matching aura is found.
     */
    public Permanent findEnchantedCreatureByAuraEffect(GameData gameData, UUID playerId, Class<? extends CardEffect> effectClass) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return null;
        for (Permanent p : bf) {
            if (p.isAttached()) {
                for (CardEffect effect : p.getCard().getStaticEffects()) {
                    if (effectClass.isInstance(effect)) {
                        return findPermanentById(gameData, p.getAttachedTo());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds a permanent on the given player's battlefield that itself carries the given static
     * effect type, returning that permanent (e.g. Empyrial Archangel redirecting damage to itself),
     * or {@code null} if none is found. A permanent that has lost its printed abilities (Song of the
     * Dryads on The Golden Throne) no longer provides its static effects.
     */
    public Permanent findControlledPermanentWithStaticEffect(GameData gameData, UUID playerId, Class<? extends CardEffect> effectClass) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return null;
        for (Permanent p : bf) {
            if (hasLostPrintedAbilities(gameData, p)) continue;
            for (CardEffect effect : p.getCard().getStaticEffects()) {
                if (effectClass.isInstance(effect)) {
                    return p;
                }
            }
        }
        return null;
    }

    // --- Other ---

    public boolean isCreatureSubtype(CardSubtype subtype) {
        return !NON_CREATURE_SUBTYPES.contains(subtype);
    }

    /**
     * Returns the global damage multiplier based on {@link GlobalDamageMultiplyingEffect} static
     * effects on the battlefield and face-up planar cards (e.g. Furnace of Rath). Each instance
     * multiplies by its factor, and multiple instances stack multiplicatively (e.g. two Furnaces =
     * 4x damage).
     */
    public int getDamageMultiplier(GameData gameData) {
        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, p) -> {
            if (p.isFaceDown() || hasLostPrintedAbilities(gameData, p)) {
                return;
            }
            for (CardEffect effect : p.getCard().getStaticEffects()) {
                if (effect instanceof GlobalDamageMultiplyingEffect multiplyingEffect) {
                    multiplier[0] *= MaroGoneNutsSupport.apply(
                            gameData, effect, multiplyingEffect.damageMultiplierFactor());
                }
            }
        });
        if (gameData.planechase != null) {
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof GlobalDamageMultiplyingEffect multiplyingEffect) {
                        multiplier[0] *= MaroGoneNutsSupport.apply(
                                gameData, effect, multiplyingEffect.damageMultiplierFactor());
                    }
                }
            }
        }
        return multiplier[0];
    }

    /**
     * Returns the damage multiplier that applies to damage dealt to a specific player by the Curse Auras enchanting
     * that player ({@link DoubleDamageToOpponentsAndTheirPermanentsEffect} over {@code enchantedPlayer()}, Curse of
     * Bloodletting). Each Curse enchanting the player doubles the multiplier; multiple instances stack
     * multiplicatively. Returns {@code 1} when no such Curse enchants the player. An Aura that lost its abilities
     * doubles nothing.
     */
    public int getEnchantedPlayerDamageMultiplier(GameData gameData, UUID playerId) {
        int[] multiplier = {1};
        gameData.forEachPermanent((controllerId, p) -> {
            if (!p.isAttached() || !playerId.equals(p.getAttachedTo())
                    || !printsStaticEffect(gameData, p, OpponentRecipientDamageMultiplyingEffect.class)
                    || hasLostAllAbilities(gameData, p)) return;
            for (CardEffect effect : p.getCard().getStaticEffects()) {
                if (effect instanceof OpponentRecipientDamageMultiplyingEffect multiplyingEffect
                        && multiplyingEffect.players() instanceof PlayerSubject.EnchantedPlayer
                        && multipliesDamageTo(p, controllerId, playerId, null, multiplyingEffect)) {
                    multiplier[0] *= MaroGoneNutsSupport.apply(gameData, effect, multiplyingEffect.damageMultiplier());
                }
            }
        });
        return multiplier[0];
    }

    /**
     * Whether {@code effect} of {@code source}, controlled by {@code controllerId}, multiplies damage dealt to the
     * player {@code recipientPlayerId} or, when {@code recipientPermanentId} is given, to that permanent, which
     * {@code recipientPlayerId} controls.
     */
    private static boolean multipliesDamageTo(Permanent source, UUID controllerId, UUID recipientPlayerId,
                                              UUID recipientPermanentId, OpponentRecipientDamageMultiplyingEffect effect) {
        if (recipientPermanentId == null) {
            return SubjectResolutionService.namesPlayer(source, controllerId, recipientPlayerId, effect.players());
        }
        return switch (effect.permanents()) {
            case null -> false;
            case PermanentSubject.Source ignored -> source.getId().equals(recipientPermanentId);
            case PermanentSubject.ControlledBy their ->
                    SubjectResolutionService.namesPlayer(source, controllerId, recipientPlayerId, their.players());
            default -> throw new IllegalStateException("Unsupported damaged permanents: " + effect.permanents());
        };
    }

    /**
     * Returns the damage multiplier that applies to damage dealt to {@code recipientPlayerId} or to a
     * permanent that player controls, based on opponent-recipient damage multipliers controlled by
     * that player's opponents (such as Gisela, Blade of Goldnight). Multiple instances stack
     * multiplicatively. Returns {@code 1} when no such permanent is on the battlefield.
     *
     * <p>Recipient-scoped, so it is applied where the recipient is known: the two player damage entry
     * points and the two permanent damage entry points.
     */
    public int getDamageToRecipientMultiplier(GameData gameData, UUID recipientPlayerId) {
        return getDamageToRecipientMultiplier(gameData, recipientPlayerId, null, false);
    }

    /**
     * Returns the damage multiplier for a recipient when the controller of the damage source is
     * known. This overload also evaluates source-and-recipient-scoped static effects.
     */
    public int getDamageToRecipientMultiplier(GameData gameData, UUID recipientPlayerId,
                                              UUID sourceControllerId) {
        return getDamageToRecipientMultiplier(gameData, recipientPlayerId, sourceControllerId, null, false);
    }

    /**
     * Returns the damage multiplier for a player or permanent recipient when the controller of the
     * damage source is known. The recipient permanent id is used by effects that apply only to a
     * specific permanent, such as Goldnight Castigator.
     */
    public int getDamageToRecipientMultiplier(GameData gameData, UUID recipientPlayerId,
                                              UUID sourceControllerId, UUID recipientPermanentId) {
        return getDamageToRecipientMultiplier(
                gameData, recipientPlayerId, sourceControllerId, recipientPermanentId, false);
    }

    /**
     * Returns the damage multiplier for a recipient, with the damage kind available to effects
     * that apply only to noncombat damage.
     */
    public int getDamageToRecipientMultiplier(GameData gameData, UUID recipientPlayerId,
                                              UUID sourceControllerId, boolean combatDamage) {
        return getDamageToRecipientMultiplier(
                gameData, recipientPlayerId, sourceControllerId, null, combatDamage);
    }

    /**
     * Returns the damage multiplier for a player or permanent recipient, with both the recipient
     * permanent and the damage kind known.
     */
    public int getDamageToRecipientMultiplier(GameData gameData, UUID recipientPlayerId,
                                              UUID sourceControllerId, UUID recipientPermanentId,
                                              boolean combatDamage) {
        if (recipientPlayerId == null) return 1;

        int[] multiplier = {1};
        for (CardEffect floated : gameData.floatingPlayerEffects(recipientPlayerId,
                DoubleDamageToOpponentsAndTheirPermanentsEffect.class)) {
            // Floated on the player it names ("you"): damage to that player, or to a permanent they control.
            OpponentRecipientDamageMultiplyingEffect multiplyingEffect = (OpponentRecipientDamageMultiplyingEffect) floated;
            if (recipientPermanentId == null || multiplyingEffect.permanents() instanceof PermanentSubject.ControlledBy) {
                multiplier[0] *= MaroGoneNutsSupport.apply(gameData, floated, multiplyingEffect.damageMultiplier());
            }
        }
        if (sourceControllerId != null && !recipientPlayerId.equals(sourceControllerId)) {
            for (CardEffect floated : gameData.floatingPlayerEffects(sourceControllerId,
                    DoubleControllerDamageToOpponentsAndTheirPermanentsEffect.class)) {
                ControllerRecipientDamageMultiplyingEffect multiplyingEffect =
                        (ControllerRecipientDamageMultiplyingEffect) floated;
                if ((recipientPermanentId == null || multiplyingEffect.appliesToOpponentPermanents())
                        && (!combatDamage || !multiplyingEffect.noncombatOnly())) {
                    multiplier[0] *= MaroGoneNutsSupport.apply(gameData, floated, multiplyingEffect.damageMultiplier());
                }
            }
        }
        gameData.forEachPermanent((controllerId, p) -> {
            if (!printsStaticEffect(gameData, p, OpponentRecipientDamageMultiplyingEffect.class,
                    ControllerRecipientDamageMultiplyingEffect.class, ChosenPlayersDamageMultiplyingEffect.class)
                    || hasLostAllAbilities(gameData, p)) return;
            for (CardEffect effect : p.getCard().getStaticEffects()) {
                if (effect instanceof OpponentRecipientDamageMultiplyingEffect multiplyingEffect
                        && !(multiplyingEffect.players() instanceof PlayerSubject.EnchantedPlayer)
                        && multipliesDamageTo(p, controllerId, recipientPlayerId, recipientPermanentId,
                        multiplyingEffect)) {
                    multiplier[0] *= MaroGoneNutsSupport.apply(
                            gameData, effect, multiplyingEffect.damageMultiplier());
                } else if (!recipientPlayerId.equals(controllerId)
                        && sourceControllerId != null
                        && sourceControllerId.equals(controllerId)
                        && effect instanceof ControllerRecipientDamageMultiplyingEffect multiplyingEffect) {
                    if ((recipientPermanentId == null || multiplyingEffect.appliesToOpponentPermanents())
                            && (!combatDamage || !multiplyingEffect.noncombatOnly())) {
                        multiplier[0] *= MaroGoneNutsSupport.apply(
                                gameData, effect, multiplyingEffect.damageMultiplier());
                    }
                } else if (sourceControllerId != null
                        && effect instanceof ChosenPlayersDamageMultiplyingEffect multiplyingEffect
                        && multiplyingEffect.appliesTo(sourceControllerId, recipientPlayerId, p)) {
                    multiplier[0] *= MaroGoneNutsSupport.apply(
                            gameData, effect, multiplyingEffect.damageMultiplier());
                }
            }
        });
        return multiplier[0];
    }

    /**
     * One damage event as the "deals that much damage plus N" bonuses
     * ({@link AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect}) see it.
     *
     * @param sourceControllerId the damage source's controller
     * @param sourcePermanent    the permanent dealing the damage, on the battlefield or as it last existed (CR 113.7a),
     *                           or {@code null} when a card is the source (a spell)
     * @param sourceCard         the source's card, judged when no permanent is the source
     * @param sourcePermanentId  the dealing permanent's id, for "another source"
     * @param spell              the damage is dealt by a spell on the stack itself, not by an object it has deal damage
     * @param combat             combat damage
     * @param recipientPlayerId  the damaged player, or the damaged permanent's controller
     * @param recipientPermanent the damaged permanent, or {@code null} when a player is dealt the damage
     */
    public record AdditionalDamageEvent(UUID sourceControllerId, Permanent sourcePermanent, Card sourceCard,
                                        UUID sourcePermanentId, boolean spell, boolean combat,
                                        UUID recipientPlayerId, Permanent recipientPermanent) {
    }

    /**
     * The {@link AdditionalDamageEvent} of damage caused by {@code entry}: {@code dealingPermanent} deals it (null:
     * the entry's source permanent as it last existed, if it left the battlefield and still deals the damage, else
     * {@code sourceCard}); {@code dealtByEntry} when the spell or ability itself deals it rather than a fighting or
     * biting permanent.
     */
    public AdditionalDamageEvent additionalDamageEvent(StackEntry entry, Permanent dealingPermanent, Card sourceCard,
                                                       UUID sourceControllerId, boolean dealtByEntry, boolean combat,
                                                       UUID recipientPlayerId, Permanent recipientPermanent) {
        Permanent source = dealingPermanent;
        if (source == null && dealtByEntry && entry != null && entry.getDamageSourceCard() == null
                && entry.getSourcePermanentSnapshot() != null && entry.getSourcePermanentId() != null
                && entry.getSourcePermanentId().equals(entry.getSourcePermanentSnapshot().getId())) {
            source = entry.getSourcePermanentSnapshot();
        }
        UUID sourcePermanentId = source != null ? source.getId() : entry != null ? entry.getSourcePermanentId() : null;
        boolean spell = dealtByEntry && !combat && entry != null && isSpellStackEntry(entry.getEntryType());
        return new AdditionalDamageEvent(sourceControllerId, source, sourceCard, sourcePermanentId, spell, combat,
                recipientPlayerId, recipientPermanent);
    }

    /**
     * The "deals that much damage plus N" bonuses ({@link AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect})
     * that apply to {@code event}, one per effect: the static abilities of permanents (none of a permanent that lost
     * its abilities or is face down, CR 613.1f, 708.2a; a conditional one while its condition holds) and the copies
     * floated on a player, whose "you" is that player. Each is its own replacement effect, which the damaged player
     * or the damaged permanent's controller orders (CR 616.1).
     */
    public List<DamageBonus> getAdditionalDamageBonuses(GameData gameData, AdditionalDamageEvent event) {
        if (event == null || event.recipientPlayerId() == null) {
            return List.of();
        }
        List<Set<CardColor>> damageColors = new ArrayList<>(1);
        List<DamageBonus> bonuses = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!printsAdditionalDamage(LayerSystemService.currentCard(gameData, permanent).getStaticEffects())) {
                return;
            }
            List<CardEffect> active = getActiveStaticEffects(gameData, permanent, controllerId);
            for (int i = 0; i < active.size(); i++) {
                if (active.get(i) instanceof AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect bonus
                        && addsDamage(gameData, bonus, permanent, controllerId, event, damageColors)) {
                    int amount = amountEvaluationService.evaluate(gameData, bonus.amount(),
                            AmountContext.forStaticEffect(permanent, controllerId));
                    if (amount != 0) {
                        bonuses.add(new DamageBonus(permanent.getId() + "/" + i,
                                FaceDownVisibility.publicName(gameData, permanent), amount));
                    }
                }
            }
        });
        List<FloatingContinuousEffect> floated;
        synchronized (gameData.floatingEffects) {
            floated = gameData.floatingEffects.stream()
                    .filter(floating -> floating.effect()
                            instanceof AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect)
                    .toList();
        }
        for (FloatingContinuousEffect floating : floated) {
            var bonus = (AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect) floating.effect();
            UUID you = floating.affectedPlayerId() != null ? floating.affectedPlayerId() : floating.controllerId();
            if (addsDamage(gameData, bonus, null, you, event, damageColors)) {
                int amount = amountEvaluationService.evaluate(gameData, bonus.amount(),
                        AmountContext.forStaticEffect(null, you));
                if (amount != 0) {
                    bonuses.add(new DamageBonus(String.valueOf(floating.id()), floating.sourceCardName(), amount));
                }
            }
        }
        return bonuses;
    }

    private static boolean printsAdditionalDamage(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect
                    || effect instanceof ConditionalEffect conditional
                    && printsAdditionalDamage(List.of(conditional.wrapped()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether {@code bonus}, of {@code carrier} (null for a floated copy) with {@code you} its controller, applies to
     * {@code event}. {@code damageColors} memoizes the source's colours for damage.
     */
    private boolean addsDamage(GameData gameData, AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect bonus,
                               Permanent carrier, UUID you, AdditionalDamageEvent event,
                               List<Set<CardColor>> damageColors) {
        if (bonus.noncombatOnly() && event.combat()
                || bonus.spellsOnly() && !event.spell()
                || bonus.excludesSource() && carrier != null && carrier.getId().equals(event.sourcePermanentId())
                || !SubjectResolutionService.namesPlayer(carrier, you, event.sourceControllerId(),
                bonus.sourceControllers())) {
            return false;
        }
        if (event.recipientPermanent() == null) {
            if (bonus.players() == null
                    || !SubjectResolutionService.namesPlayer(carrier, you, event.recipientPlayerId(), bonus.players())) {
                return false;
            }
        } else if (!(bonus.permanents() instanceof PermanentSubject.ControlledBy their)
                || !SubjectResolutionService.namesPlayer(carrier, you, event.recipientPlayerId(), their.players())
                || !predicateEvaluationService.matchesPermanentPredicate(event.recipientPermanent(), their.filter(),
                FilterContext.of(gameData).withSourceControllerId(you))) {
            return false;
        }
        if (bonus.sourceFilter() == null) {
            return true;
        }
        if (damageColors.isEmpty()) {
            damageColors.add(getDamageSourceColors(gameData, event.sourcePermanent() != null
                    ? getEffectiveColors(gameData, event.sourcePermanent())
                    : getEffectiveCardColors(gameData, event.sourceCard())));
        }
        PermanentPredicate filter = withDamageColors(bonus.sourceFilter(), damageColors.getFirst());
        if (event.sourcePermanent() != null) {
            return predicateEvaluationService.matchesPermanentPredicate(event.sourcePermanent(), filter,
                    FilterContext.of(gameData));
        }
        return event.sourceCard() != null
                && predicateEvaluationService.matchesPermanentPredicate(new Permanent(event.sourceCard()), filter, null);
    }

    /**
     * {@code filter} with its colour leaves judged on {@code colors}, the colours the source has as it deals damage
     * (Ghostly Flame makes a black or red source colourless for damage; a spell's colours are its own, CR 609.7b).
     */
    private static PermanentPredicate withDamageColors(PermanentPredicate filter, Set<CardColor> colors) {
        return switch (filter) {
            case PermanentColorInPredicate colorIn ->
                    Collections.disjoint(colorIn.colors(), colors) ? NO_PERMANENT : new PermanentTruePredicate();
            case PermanentIsColorlessPredicate ignored ->
                    colors.isEmpty() ? new PermanentTruePredicate() : NO_PERMANENT;
            case PermanentAllOfPredicate all -> new PermanentAllOfPredicate(all.predicates().stream()
                    .map(predicate -> withDamageColors(predicate, colors)).toList());
            case PermanentAnyOfPredicate any -> new PermanentAnyOfPredicate(any.predicates().stream()
                    .map(predicate -> withDamageColors(predicate, colors)).toList());
            case PermanentNotPredicate not -> new PermanentNotPredicate(withDamageColors(not.predicate(), colors));
            default -> filter;
        };
    }

    private static final PermanentPredicate NO_PERMANENT = new PermanentNotPredicate(new PermanentTruePredicate());

    /**
     * True if {@code playerId} is enchanted by a Curse carrying
     * {@link EnchantedPlayerCantActivateNonManaNonLoyaltyAbilitiesEffect} (Overwhelming Splendor):
     * that player may activate only mana abilities and loyalty abilities.
     */
    public boolean playerCantActivateNonManaOrLoyaltyAbilities(GameData gameData, UUID playerId) {
        return gameData.anyPermanentMatches(p ->
                p.isAttached() && playerId.equals(p.getAttachedTo())
                        && p.getCard().getStaticEffects().stream()
                        .anyMatch(e -> e instanceof EnchantedPlayerCantActivateNonManaNonLoyaltyAbilitiesEffect));
    }

    /**
     * Applies the global damage multiplier to the given damage amount.
     *
     * @return the damage after applying all {@link GlobalDamageMultiplyingEffect} multipliers
     */
    public int applyDamageMultiplier(GameData gameData, int damage) {
        return SaturatingMath.multiply(damage, getDamageMultiplier(gameData));
    }

    /**
     * Returns the token creation multiplier for a specific player based on
     * {@link MultiplyTokenCreationEffect} permanents they control.
     * Unlike {@link #getDamageMultiplier} which is global, this is controller-specific:
     * only permanents controlled by the given player contribute to the multiplier.
     * Multiple instances stack multiplicatively (e.g. two Parallel Lives = 4x tokens).
     */
    public int getTokenMultiplier(GameData gameData, UUID controllerId) {
        return getTokenCreationAmount(gameData, controllerId, 1, null);
    }

    /**
     * Applies the token-count replacements controlled by the player creating the given token
     * profile. The no-profile multiplier method remains for callers that only need the legacy
     * all-token multipliers.
     */
    public int getTokenCreationAmount(GameData gameData, UUID controllerId, int amount,
                                      Collection<CardSubtype> tokenSubtypes) {
        return getTokenCreationAmount(gameData, controllerId, amount, tokenSubtypes, false);
    }

    public int getTokenMultiplier(GameData gameData, UUID controllerId, boolean creatureToken) {
        return getTokenCreationAmount(gameData, controllerId, 1, null, creatureToken);
    }

    public int getTokenCreationAmount(GameData gameData, UUID controllerId, int amount,
                                      Collection<CardSubtype> tokenSubtypes, boolean creatureToken) {
        return getTokenCreationAmount(gameData, controllerId, amount, tokenSubtypes, creatureToken,
                replacement -> true);
    }

    private int getTokenCreationAmount(GameData gameData, UUID controllerId, int amount,
                                       Collection<CardSubtype> tokenSubtypes, boolean creatureToken,
                                       Predicate<TokenCreationReplacementEffect> applicableReplacements) {
        if (amount <= 0) {
            return amount;
        }
        List<TokenCreationReplacement> replacements = new ArrayList<>(
                tokenCreationReplacements(gameData, controllerId, tokenSubtypes, creatureToken, applicableReplacements));
        replacements.sort(Comparator.comparingInt(r -> r.effect().replacementOrder()));
        int adjustedAmount = amount;
        for (TokenCreationReplacement replacement : replacements) {
            adjustedAmount = MaroGoneNutsSupport.apply(
                    gameData, (CardEffect) replacement.effect(), replacement.effect().replaceTokenCount(adjustedAmount));
        }
        return adjustedAmount;
    }

    /**
     * One token-count replacement that applies to a token creation event.
     *
     * @param key    identifies the effect instance for CR 614.5 bookkeeping
     * @param label  text shown to the player ordering the effects
     * @param effect the replacement itself
     */
    public record TokenCreationReplacement(String key, String label, TokenCreationReplacementEffect effect) {}

    /**
     * The token-count replacements that apply to a creation of tokens with the given profile by
     * {@code controllerId}, one per effect instance, in battlefield, planar, then turn-scoped order.
     * The player creating the tokens orders them (CR 616.1); {@link #getTokenCreationAmount} applies
     * them in a fixed order instead.
     */
    public List<TokenCreationReplacement> tokenCreationReplacements(GameData gameData, UUID controllerId,
                                                                    Collection<CardSubtype> tokenSubtypes,
                                                                    boolean creatureToken,
                                                                    Predicate<TokenCreationReplacementEffect> applicableReplacements) {
        UUID effectiveControllerId = resolveTokenCreationController(gameData, controllerId, creatureToken);
        List<TokenCreationReplacement> replacements = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            List<CardEffect> statics = getActiveStaticEffects(gameData, p, playerId);
            for (int i = 0; i < statics.size(); i++) {
                if (statics.get(i) instanceof TokenCreationReplacementEffect replacement
                        && applicableReplacements.test(replacement)
                        && replacement.appliesToTokenCreator(playerId, effectiveControllerId)
                        && replacement.appliesTo(tokenSubtypes)
                        && (!(replacement instanceof MultiplyTokenCreationEffect multiply)
                            || !multiply.creatureTokensOnly() || creatureToken)) {
                    replacements.add(new TokenCreationReplacement("TokenCreation:" + p.getId() + ":" + i,
                            FaceDownVisibility.publicName(gameData, p) + ": " + describeTokenCreationReplacement(replacement), replacement));
                }
            }
        });
        if (gameData.planechase != null) {
            for (var planar : gameData.planechase.faceUp) {
                List<CardEffect> statics = planar.getCard().getStaticEffects();
                for (int i = 0; i < statics.size(); i++) {
                    if (statics.get(i) instanceof TokenCreationReplacementEffect replacement
                            && applicableReplacements.test(replacement)
                            && replacement.appliesTo(tokenSubtypes)
                            && (!(replacement instanceof MultiplyTokenCreationEffect multiply)
                            || !multiply.creatureTokensOnly() || creatureToken)) {
                        replacements.add(new TokenCreationReplacement("TokenCreationPlanar:" + planar.getCard().getId() + ":" + i,
                                planar.getCard().getName() + ": " + describeTokenCreationReplacement(replacement), replacement));
                    }
                }
            }
        }
        List<CardEffect> untilEndOfTurn = gameData.floatingPlayerEffects(effectiveControllerId,
                TokenCreationReplacementEffect.class);
        for (int i = 0; i < untilEndOfTurn.size(); i++) {
            if (untilEndOfTurn.get(i) instanceof TokenCreationReplacementEffect replacement
                    && applicableReplacements.test(replacement)
                    && replacement.appliesTo(tokenSubtypes)
                    && (!(replacement instanceof MultiplyTokenCreationEffect multiply)
                        || !multiply.creatureTokensOnly() || creatureToken)) {
                replacements.add(new TokenCreationReplacement("TokenCreationTurn:" + i,
                        "Until end of turn: " + describeTokenCreationReplacement(replacement), replacement));
            }
        }
        return replacements;
    }

    private static String describeTokenCreationReplacement(TokenCreationReplacementEffect replacement) {
        return switch (replacement) {
            case MultiplyTokenCreationEffect multiply -> "create " + multiply.multiplier() + " times as many tokens";
            case HalveTokenCreationEffect halve -> "create half as many tokens";
            default -> "change the number of tokens created";
        };
    }

    /**
     * Gather Specimens' control-changing replacement effects for an entering creature (CR 614.1):
     * each resolved Gather Specimens applies at most once to the event (CR 614.5) and only while
     * the creature would enter under one of its controller's opponents' control.
     */
    public UUID resolveGatherSpecimensController(GameData gameData, UUID controllerId) {
        List<UUID> gatherers = gatherersOf(gameData, GatherSpecimensEffect.Kind.CREATURES_ENTERING);
        Set<Integer> used = new HashSet<>();
        UUID effectiveControllerId = controllerId;
        UUID gatherer;
        while ((gatherer = applyNextGatherer(gatherers, used, effectiveControllerId)) != null) {
            effectiveControllerId = gatherer;
        }
        return effectiveControllerId;
    }

    /** The controllers of the resolved Gather Specimens effects of {@code kind} still in force, one per effect, oldest first. */
    private static List<UUID> gatherersOf(GameData gameData, GatherSpecimensEffect.Kind kind) {
        return gameData.floatingEffectsOfTypes(GatherSpecimensEffect.class).stream()
                .filter(floating -> ((GatherSpecimensEffect) floating.effect()).kind() == kind)
                .map(FloatingContinuousEffect::affectedPlayerId)
                .toList();
    }

    /** The controller of the first unused gatherer that is not {@code currentControllerId}, marking it used. */
    private static UUID applyNextGatherer(List<UUID> gatherers, Set<Integer> used, UUID currentControllerId) {
        for (int index = 0; index < gatherers.size(); index++) {
            UUID gatherer = gatherers.get(index);
            if (!gatherer.equals(currentControllerId) && used.add(index)) {
                return gatherer;
            }
        }
        return null;
    }

    /** Resolves control-changing replacement effects before a token is created. */
    public UUID resolveTokenCreationController(GameData gameData, UUID controllerId, boolean creatureToken) {
        UUID effectiveControllerId = controllerId;
        List<UUID> creatureGatherers = gatherersOf(gameData, GatherSpecimensEffect.Kind.CREATURES_ENTERING);
        Set<Integer> usedGatherers = new HashSet<>();
        List<UUID> tokenGatherers = gatherersOf(gameData, GatherSpecimensEffect.Kind.TOKENS_CREATED);
        Set<Integer> usedTokenGatherers = new HashSet<>();
        boolean changed;
        do {
            changed = false;
            if (creatureToken) {
                UUID gatherer = applyNextGatherer(creatureGatherers, usedGatherers, effectiveControllerId);
                if (gatherer != null) {
                    effectiveControllerId = gatherer;
                    changed = true;
                    continue;
                }
            }
            for (int index = 0; index < tokenGatherers.size(); index++) {
                UUID gatherer = tokenGatherers.get(index);
                if (!gatherer.equals(effectiveControllerId) && usedTokenGatherers.add(index)) {
                    effectiveControllerId = gatherer;
                    changed = true;
                    break;
                }
            }
        } while (changed);
        return effectiveControllerId;
    }

    /**
     * The damage {@code entry} would deal after every multiplier and "+N" bonus for its source,
     * applied in a fixed order: bonuses first, then multipliers. Damage dealt to a player or a
     * permanent gets these as replacement effects its recipient orders (CR 616.1), so this is
     * only a prediction, for the AI and for effects that read an amount without dealing it.
     *
     * @param entry the stack entry representing the damage source; used to check controller
     *              and to evaluate stack-entry predicates on controller damage effects
     */
    public int applyDamageMultiplier(GameData gameData, int damage, StackEntry entry) {
        int bonus = damage > 0 && entry != null ? getDealingSourceDamageBonus(gameData, entry) : 0;
        return (damage + bonus) * getDamageMultiplier(gameData) * getDealingSourceDamageMultiplier(gameData, entry);
    }

    /** The source permanent of {@code entry} on the battlefield, or its last known information. */
    /**
     * The colors a damage source has now: a permanent source on the battlefield uses its effective
     * colors (a source changed to another color by a color-changing effect no longer counts as its
     * printed color), anything else uses the printed colors of {@code sourceCard}.
     */
    private List<CardColor> damageSourceColors(GameData gameData, Permanent source, Card sourceCard) {
        if (source != null && sourceCard != null && source.getCard() == sourceCard
                && findPermanentById(gameData, source.getId()) != null) {
            return List.copyOf(getEffectiveColors(gameData, source));
        }
        return sourceCard != null ? sourceCard.getColors() : List.of();
    }

    private Permanent dealingSourcePermanent(GameData gameData, StackEntry entry) {
        Permanent source = entry != null && entry.getSourcePermanentId() != null
                ? findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null && entry != null) {
            source = entry.getSourcePermanentSnapshot();
        }
        return source;
    }

    private UUID dealingSourceController(GameData gameData, StackEntry entry, Permanent source) {
        UUID controllerId = entry != null ? entry.getControllerId() : null;
        if (source != null) {
            UUID sourceControllerId = findPermanentController(gameData, source.getId());
            if (sourceControllerId != null) controllerId = sourceControllerId;
        }
        return controllerId;
    }

    /**
     * The "deals that much damage plus N" bonuses for noncombat damage {@code entry} deals, whoever
     * is dealt it: the turn's colour and controller bonuses and the controller's Pyromancer's Gauntlet-style bonuses;
     * the static "plus N" abilities are {@link #getAdditionalDamageBonuses}, judged with the recipient.
     */
    public int getDealingSourceDamageBonus(GameData gameData, StackEntry entry) {
        return totalBonus(getDealingSourceDamageBonuses(gameData, entry));
    }

    /** {@link #getDealingSourceDamageBonus}, one entry per effect. */
    public List<DamageBonus> getDealingSourceDamageBonuses(GameData gameData, StackEntry entry) {
        return getDealingSourceDamageBonuses(gameData, entry, null);
    }

    /**
     * {@link #getDealingSourceDamageBonuses}, for damage dealt by {@code damageSource} when it is
     * not the entry's own source (e.g. the creature a fight's return damage comes from). A null
     * {@code damageSource} falls back to the entry's source.
     */
    public List<DamageBonus> getDealingSourceDamageBonuses(GameData gameData, StackEntry entry,
                                                           Permanent damageSource) {
        Permanent source = damageSource != null ? damageSource : dealingSourcePermanent(gameData, entry);
        UUID controllerId = dealingSourceController(gameData, entry, source);
        List<DamageBonus> bonuses = new ArrayList<>();
        bonuses.addAll(getColorSourceDamageBonusesThisTurn(gameData, controllerId,
                damageSourceColors(gameData, source, entry.getCard())));
        bonuses.addAll(getControllerDamageBonusesThisTurn(gameData, controllerId));
        boolean dealtByEntry = damageSource == null
                || entry.getSourcePermanentId() != null && damageSource.getId().equals(entry.getSourcePermanentId());
        if (dealtByEntry) {
            bonuses.addAll(getControllerDamageBonuses(gameData, entry, controllerId,
                    source != null ? source.getId() : entry.getSourcePermanentId(), false));
        } else {
            bonuses.addAll(getControllerDamageBonuses(gameData, entry, controllerId,
                    source != null ? source.getId() : null, true));
        }
        return bonuses;
    }

    /**
     * The multipliers for noncombat damage {@code entry} deals, apart from the global ones
     * ({@link #getDamageMultiplier}): the controller's and the source's doublings.
     */
    /**
     * The multipliers for noncombat damage that {@code explicitSource} deals while {@code entry} is
     * the stack entry causing it (a fight, where the other creature deals the damage back): the
     * explicit source's controller's doublings and its own. The entry's source does not deal this
     * damage, so its doublings are not applied.
     */
    public int getExplicitSourceDamageMultiplier(GameData gameData, StackEntry entry, Permanent explicitSource) {
        UUID controllerId = findDamageSourceController(gameData, entry, explicitSource);
        return getControllerDamageMultiplier(gameData, controllerId, entry, false, explicitSource)
                * getPermanentDamageMultiplier(gameData, explicitSource.getId());
    }

    public int getDealingSourceDamageMultiplier(GameData gameData, StackEntry entry) {
        Permanent source = dealingSourcePermanent(gameData, entry);
        UUID controllerId = dealingSourceController(gameData, entry, source);
        return getControllerDamageMultiplier(gameData, controllerId, entry, false, source)
                * getPermanentDamageMultiplier(gameData, entry != null ? entry.getSourcePermanentId() : null);
    }

    private static boolean isSpellEntry(StackEntryType entryType) {
        return switch (entryType) {
            case CREATURE_SPELL, ENCHANTMENT_SPELL, SORCERY_SPELL, INSTANT_SPELL,
                    ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            case TRIGGERED_ABILITY, ACTIVATED_ABILITY -> false;
        };
    }

    /**
     * Returns the additive damage bonus from {@link AdditionalControllerDamageEffect} permanents
     * controlled by the stack entry's controller (e.g. Pyromancer's Gauntlet). Each matching
     * effect contributes its {@code amount}; multiple instances stack additively. Returns 0 when
     * the entry is null or no matching effect applies.
     */
    int getControllerDamageBonus(GameData gameData, StackEntry entry) {
        return totalBonus(getControllerDamageBonuses(gameData, entry, entry == null ? null : entry.getControllerId(),
                entry == null ? null : entry.getSourcePermanentId(), false));
    }

    /**
     * @param unfilteredOnly when true, only effects with no stack-entry filter apply; used when the
     *                       damage source is a permanent other than the stack entry's own source, so the
     *                       entry's spell or ability characteristics don't describe that source
     */
    private List<DamageBonus> getControllerDamageBonuses(GameData gameData, StackEntry entry,
                                                         UUID controllerId, UUID sourcePermanentId,
                                                         boolean unfilteredOnly) {
        if (entry == null || controllerId == null) return List.of();

        List<DamageBonus> bonuses = new ArrayList<>();
        Permanent liveSource = isSpellEntry(entry.getEntryType()) || entry.getSourcePermanentId() == null
                ? null : findPermanentById(gameData, entry.getSourcePermanentId());
        gameData.forEachPermanent((playerId, p) -> {
            if (!playerId.equals(controllerId)) return;
            if (!printsStaticEffect(gameData, p, AdditionalControllerDamageEffect.class)
                    || hasLostAllAbilities(gameData, p)) return;
            forEachStaticEffect(p, (effect, instance) -> {
                if (effect instanceof AdditionalControllerDamageEffect acde
                        && !(acde.anotherSourceOnly() && p.getId().equals(sourcePermanentId))
                        && (acde.stackFilter() == null
                        || !unfilteredOnly
                        && predicateEvaluationService.matchesStackEntryPredicate(
                        entry, acde.stackFilter(), null, gameData, liveSource))) {
                    bonuses.add(new DamageBonus(instance, FaceDownVisibility.publicName(gameData, p), acde.amount()));
                }
            });
        });
        return bonuses;
    }

    /**
     * Returns the per-controller damage multiplier based on {@link DoubleControllerDamageEffect}
     * permanents on the battlefield and the copies floated on the controller (Insult). Only applies when the source
     * is controlled by the same player who controls the permanent with the effect / has the floated copy.
     *
     * <p>Each effect has a {@code stackFilter} predicate and an {@code appliesToCombatDamage} flag.
     * For stack-based damage, the effect applies if the filter is {@code null} (matches all) or if
     * the entry matches the filter. For combat damage ({@code isCombat=true}), the effect applies
     * only if {@code appliesToCombatDamage} is {@code true}.
     *
     * <p>Multiple instances stack multiplicatively.
     *
     * @param entry the stack entry, or {@code null} for combat damage
     * @param isCombat whether this is combat damage
     */
    int getControllerDamageMultiplier(GameData gameData, UUID controllerId, StackEntry entry, boolean isCombat) {
        return getControllerDamageMultiplier(gameData, controllerId, entry, isCombat, null);
    }

    private int getControllerDamageMultiplier(GameData gameData, UUID controllerId, StackEntry entry,
                                              boolean isCombat, Permanent damageSource) {
        if (controllerId == null) return 1;

        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, p) -> {
            if (!playerId.equals(controllerId)) return;
            if (p.isFaceDown()
                    || !printsStaticEffect(gameData, p, ControllerDamageMultiplyingEffect.class,
                    ConditionalEffect.class, SourceDamageMultiplyingEffect.class)
                    || hasLostPrintedAbilities(gameData, p)) return;
            for (CardEffect effect : p.getCard().getStaticEffects()) {
                if (effect instanceof ControllerDamageMultiplyingEffect multiplyingEffect) {
                    if (isCombat) {
                        if (multiplyingEffect.appliesToCombatDamage()) {
                            multiplier[0] *= MaroGoneNutsSupport.apply(
                                    gameData, effect, multiplyingEffect.damageMultiplier());
                        }
                    } else if (entry != null) {
                        if (multiplyingEffect.stackFilter() == null
                                || predicateEvaluationService.matchesStackEntryPredicate(
                                entry, multiplyingEffect.stackFilter(), null)) {
                            multiplier[0] *= MaroGoneNutsSupport.apply(
                                    gameData, multiplyingEffect, multiplyingEffect.damageMultiplier());
                        }
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forStaticEffect(p, controllerId))
                        && conditional.wrapped() instanceof ControllerDamageMultiplyingEffect multiplyingEffect) {
                    if (isCombat) {
                        if (multiplyingEffect.appliesToCombatDamage()) {
                            multiplier[0] *= MaroGoneNutsSupport.apply(
                                    gameData, multiplyingEffect, multiplyingEffect.damageMultiplier());
                        }
                    } else if (entry != null
                            && (multiplyingEffect.stackFilter() == null
                            || predicateEvaluationService.matchesStackEntryPredicate(
                            entry, multiplyingEffect.stackFilter(), null))) {
                        multiplier[0] *= MaroGoneNutsSupport.apply(
                                gameData, effect, multiplyingEffect.damageMultiplier());
                    }
                } else if (!isCombat && entry != null && damageSource == null
                        && effect instanceof SourceDamageMultiplyingEffect multiplyingEffect
                        && multiplyingEffect.appliesToNonCombatDamage()
                        && matchesSpellDamageSource(gameData, entry, multiplyingEffect.spellSourceFilter(), p)) {
                    multiplier[0] *= MaroGoneNutsSupport.apply(
                            gameData, effect, multiplyingEffect.damageMultiplier());
                }
            }
        });
        if (damageSource != null && (findPermanentController(gameData, damageSource.getId()) == null
                || controllerId.equals(findPermanentController(gameData, damageSource.getId())))) {
            multiplier[0] *= getSourceDamageMultiplier(gameData, controllerId, damageSource);
        }
        for (CardEffect floated : gameData.floatingPlayerEffects(controllerId, DoubleControllerDamageEffect.class)) {
            ControllerDamageMultiplyingEffect multiplyingEffect = (ControllerDamageMultiplyingEffect) floated;
            if (isCombat ? multiplyingEffect.appliesToCombatDamage()
                    : entry != null && (multiplyingEffect.stackFilter() == null
                    || predicateEvaluationService.matchesStackEntryPredicate(entry, multiplyingEffect.stackFilter(), null))) {
                multiplier[0] *= MaroGoneNutsSupport.apply(gameData, floated, multiplyingEffect.damageMultiplier());
            }
        }
        return multiplier[0];
    }

    /**
     * Whether the spell {@code entry} deals damage as a source matching {@code spellFilter}, judged on its card as effects
     * see it, with its X counted in its mana value while it is a spell (CR 202.3e); the chosen type of
     * {@code effectSource} answers "the chosen type".
     */
    private boolean matchesSpellDamageSource(GameData gameData, StackEntry entry, CardPredicate spellFilter,
                                             Permanent effectSource) {
        Card sourceCard = entry.getEffectiveDamageSourceCard();
        if (spellFilter == null || sourceCard == null) {
            return false;
        }
        int xValue = isSpellEntry(entry.getEntryType()) ? entry.getXValue() : 0;
        return predicateEvaluationService.matchesCardPredicate(sourceCard, spellFilter, effectSource.getCard().getId(),
                gameData, sourceCard.getOwnerId(), effectSource.getId(), null, xValue, effectSource);
    }

    public int getSourceDamageMultiplier(GameData gameData, UUID controllerId, Permanent damageSource) {
        return getSourceDamageMultiplier(gameData, controllerId, damageSource, false, null, null);
    }

    private int getSourceDamageMultiplier(GameData gameData, UUID controllerId, Permanent damageSource,
                                          boolean combatDamage, Permanent combatDamageTarget,
                                          UUID combatDamageRecipientPlayerId) {
        if (controllerId == null || damageSource == null
                || (findPermanentController(gameData, damageSource.getId()) != null
                && !controllerId.equals(findPermanentController(gameData, damageSource.getId())))) {
            return 1;
        }

        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, effectSource) -> {
            if (!playerId.equals(controllerId)) {
                boolean hasCrossControllerSourceMultiplier = effectSource.getCard().getStaticEffects()
                        .stream()
                        .filter(SourceDamageMultiplyingEffect.class::isInstance)
                        .map(SourceDamageMultiplyingEffect.class::cast)
                        .anyMatch(multiplyingEffect -> !multiplyingEffect.requiresSourceControllerMatch());
                if (!hasCrossControllerSourceMultiplier) return;
            }
            if (effectSource.getCard().getStaticEffects().stream()
                    .anyMatch(SourceDamageMultiplyingEffect.class::isInstance)
                    && hasLostAllAbilities(gameData, effectSource)) return;
            FilterContext context = FilterContext.of(gameData)
                    .withSourceCardId(effectSource.getCard().getId())
                    .withSourceControllerId(playerId)
                    .withSourcePermanentSnapshot(effectSource)
                    .withSourcePermanentId(effectSource.getId());
            for (CardEffect effect : effectSource.getCard().getStaticEffects()) {
                if (effect instanceof SourceDamageMultiplyingEffect multiplyingEffect
                        && (!multiplyingEffect.requiresSourceControllerMatch() || playerId.equals(controllerId))
                        && predicateEvaluationService.matchesPermanentPredicate(
                        damageSource, multiplyingEffect.sourceFilter(), context)
                        && (combatDamage
                        ? multiplyingEffect.appliesToCombatDamageTarget(combatDamageTarget)
                        : multiplyingEffect.appliesToNonCombatDamage())) {
                    multiplier[0] *= MaroGoneNutsSupport.apply(
                            gameData, multiplyingEffect, multiplyingEffect.damageMultiplier());
                }
            }
        });
        return multiplier[0];
    }

    /**
     * Returns the turn-scoped damage multiplier for a specific permanent. Multiple effects on the
     * same permanent stack multiplicatively.
     */
    public int getPermanentDamageMultiplier(GameData gameData, UUID permanentId) {
        if (permanentId == null) return 1;

        int doublings = gameData.floatingEffectsOnPermanent(permanentId,
                DoubleDamageFromTargetPermanentThisTurnEffect.class).size();
        int multiplier = 1;
        for (int i = 0; i < doublings; i++) {
            multiplier *= MaroGoneNutsSupport.doublingFactor(gameData) * 2;
        }
        return multiplier;
    }

    /**
     * Returns whether the damage source has lifelink, including a permanent dealing damage
     * through an ability. Instant and sorcery spells can also gain lifelink from a
     * {@link GrantLifelinkToControllerSpellsByColorEffect} on the controller's battlefield.
     */
    public boolean shouldControllerSpellHaveLifelink(GameData gameData, StackEntry entry) {
        if (entry == null) return false;
        if (sourceHasKeyword(gameData, entry, null, Keyword.LIFELINK)) return true;
        StackEntryType type = entry.getEntryType();
        if (type != StackEntryType.INSTANT_SPELL && type != StackEntryType.SORCERY_SPELL) return false;

        boolean[] hasLifelink = {false};
        gameData.forEachPermanent((playerId, p) -> {
            if (!playerId.equals(entry.getControllerId()) || hasLostPrintedAbilities(gameData, p)) return;
            for (CardEffect effect : p.getCard().getStaticEffects()) {
                if (effect instanceof GrantLifelinkToControllerSpellsByColorEffect glse
                        && (glse.color() == null || entry.getCard().getColors().contains(glse.color()))
                        && predicateEvaluationService.matchesCardPredicate(entry.getCard(), glse.filter(),
                        p.getCard().getId(), gameData, entry.getCard().getOwnerId())) {
                    hasLifelink[0] = true;
                }
            }
        });
        return hasLifelink[0];
    }

    /**
     * Returns the combat damage multiplier for a creature based on
     * {@link DoubleEquippedCreatureCombatDamageEffect} on attached equipment.
     * Each such equipment doubles the multiplier.
     */
    private int getEquippedCreatureCombatDamageMultiplier(GameData gameData, Permanent creature) {
        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, p) -> {
            if (p.isAttached() && p.getAttachedTo() != null && p.getAttachedTo().equals(creature.getId())) {
                for (CardEffect effect : p.getCard().getStaticEffects()) {
                    if (effect instanceof DoubleEquippedCreatureCombatDamageEffect) {
                        multiplier[0] *= MaroGoneNutsSupport.apply(gameData, effect, 2);
                    }
                }
            }
        });
        return multiplier[0];
    }

    /** The "deals that much damage plus N" bonuses for combat damage {@code source} deals, whoever is dealt it. */
    public int getCombatSourceDamageBonus(GameData gameData, Permanent source) {
        return totalBonus(getCombatSourceDamageBonuses(gameData, source));
    }

    /** {@link #getCombatSourceDamageBonus}, one entry per effect. */
    public List<DamageBonus> getCombatSourceDamageBonuses(GameData gameData, Permanent source) {
        UUID controllerId = findPermanentController(gameData, source.getId());
        if (controllerId == null) {
            return List.of();
        }
        List<DamageBonus> bonuses = new ArrayList<>();
        bonuses.addAll(getColorSourceDamageBonusesThisTurn(gameData, controllerId,
                damageSourceColors(gameData, source, source.getCard())));
        bonuses.addAll(getControllerDamageBonusesThisTurn(gameData, controllerId));
        return bonuses;
    }

    /**
     * The multipliers for combat damage {@code source} deals to {@code recipientPermanent}, or to the
     * player {@code recipientPlayerId}, apart from the global ones and the recipient's own: the
     * controller's, the source's and its Equipment's doublings.
     */
    public int getCombatSourceDamageMultiplier(GameData gameData, Permanent source, Permanent recipientPermanent,
                                               UUID recipientPlayerId) {
        UUID controllerId = findPermanentController(gameData, source.getId());
        return getControllerDamageMultiplier(gameData, controllerId, null, true)
                * getSourceDamageMultiplier(gameData, controllerId, source, true, recipientPermanent,
                        recipientPlayerId)
                * getDelayedSourceDamageMultiplier(gameData, source, recipientPlayerId)
                * getPermanentDamageMultiplier(gameData, source.getId())
                * getEquippedCreatureCombatDamageMultiplier(gameData, source);
    }

    /**
     * The multipliers for combat damage dealt to the creature {@code target} itself: its
     * Equipment's doublings and "combat damage dealt to creatures is doubled this turn".
     */
    public int getCombatDamageToCreatureMultiplier(GameData gameData, Permanent target) {
        int multiplier = getEquippedCreatureCombatDamageMultiplier(gameData, target);
        for (FloatingContinuousEffect doubling : gameData.floatingEffectsOnPlayer(null,
                DoubleCombatDamageToCreaturesThisTurnEffect.class)) {
            // A doubling effect, so Maro's Gone Nuts upgrades it like every other doubler.
            multiplier *= MaroGoneNutsSupport.apply(gameData, doubling.effect(), 2);
        }
        return multiplier;
    }

    private int getDelayedSourceDamageMultiplier(GameData gameData, Permanent source,
                                                 UUID recipientPlayerId) {
        if (source == null || recipientPlayerId == null) {
            return 1;
        }
        int multiplier = 1;
        for (FloatingContinuousEffect delayed : gameData.floatingEffectsOnPermanent(source.getId(),
                MultiplyCombatDamageFromTargetCreatureToOpponentsUntilNextTurnEffect.class)) {
            if (!recipientPlayerId.equals(delayed.controllerId())) {
                multiplier *= ((MultiplyCombatDamageFromTargetCreatureToOpponentsUntilNextTurnEffect) delayed.effect())
                        .multiplier();
            }
        }
        return multiplier;
    }

    /**
     * Returns the additive damage bonus for sources of matching color controlled by the given
     * player this turn (e.g. The Flame of Keld Chapter III). Returns the sum of all matching
     * color bonuses. Returns 0 if no bonus applies.
     */
    int getColorSourceDamageBonus(GameData gameData, UUID controllerId, List<CardColor> sourceColors) {
        return totalBonus(getColorSourceDamageBonusesThisTurn(gameData, controllerId, sourceColors));
    }

    private List<DamageBonus> getColorSourceDamageBonusesThisTurn(GameData gameData, UUID controllerId,
                                                                  List<CardColor> sourceColors) {
        if (controllerId == null || sourceColors == null || sourceColors.isEmpty()) {
            return List.of();
        }
        List<DamageBonus> bonuses = new ArrayList<>();
        for (FloatingContinuousEffect floating
                : gameData.floatingEffectsOnPlayer(controllerId, BoostControllerDamageThisTurnEffect.class)) {
            BoostControllerDamageThisTurnEffect boost = (BoostControllerDamageThisTurnEffect) floating.effect();
            if (boost.color() != null && boost.fixedBonus() != 0 && sourceColors.contains(boost.color())) {
                bonuses.add(new DamageBonus(String.valueOf(floating.id()), floating.sourceCardName(), boost.fixedBonus()));
            }
        }
        return bonuses;
    }

    /** The damage bonuses this turn for all sources controlled by {@code controllerId}, one per effect. */
    private List<DamageBonus> getControllerDamageBonusesThisTurn(GameData gameData, UUID controllerId) {
        if (controllerId == null) {
            return List.of();
        }
        List<DamageBonus> bonuses = new ArrayList<>();
        for (FloatingContinuousEffect floating
                : gameData.floatingEffectsOnPlayer(controllerId, BoostControllerDamageThisTurnEffect.class)) {
            BoostControllerDamageThisTurnEffect boost = (BoostControllerDamageThisTurnEffect) floating.effect();
            int amount = boost.fixedBonus();
            if (boost.color() == null && !boost.noncombatOnly() && amount != 0) {
                bonuses.add(new DamageBonus(String.valueOf(floating.id()), floating.sourceCardName(), amount));
            }
        }
        return bonuses;
    }

    /** The noncombat damage bonuses this turn for sources controlled by {@code controllerId}, one per effect. */
    public List<DamageBonus> getControllerNoncombatDamageBonuses(GameData gameData, UUID controllerId) {
        if (controllerId == null) {
            return List.of();
        }
        List<DamageBonus> bonuses = new ArrayList<>();
        for (FloatingContinuousEffect floating : gameData.floatingEffectsOnPlayer(controllerId,
                BoostControllerDamageThisTurnEffect.class)) {
            BoostControllerDamageThisTurnEffect boost = (BoostControllerDamageThisTurnEffect) floating.effect();
            int amount = boost.fixedBonus();
            if (boost.noncombatOnly() && amount != 0) {
                bonuses.add(new DamageBonus(String.valueOf(floating.id()), floating.sourceCardName(), amount));
            }
        }
        return bonuses;
    }

    /** The perpetual noncombat damage bonuses granted to the resolving spell card, one per grant. */
    public List<DamageBonus> getPerpetualNoncombatDamageBonuses(GameData gameData, StackEntry entry) {
        if (entry == null || entry.getEffectiveDamageSourceCard() == null) {
            return List.of();
        }
        return grantBonuses("perpetual", gameData.perpetualNoncombatDamageBonuses.get(
                entry.getEffectiveDamageSourceCard().getId()), null);
    }

    /**
     * One "deals that much damage plus N" effect: {@code instance} identifies the effect (a static
     * ability of a permanent, a floating effect, or one grant of a turn-scoped or perpetual bonus),
     * {@code sourceName} names where it comes from and {@code amount} is N.
     */
    public record DamageBonus(String instance, String sourceName, int amount) {}

    private static int totalBonus(List<DamageBonus> bonuses) {
        return bonuses.stream().mapToInt(DamageBonus::amount).sum();
    }

    /**
     * One bonus per grant in {@code grants}, keyed by its place in the list; with {@code sourceColors}
     * only the grants of one of those colors count.
     */
    private static List<DamageBonus> grantBonuses(String family, List<DamageBonusGrant> grants,
                                                  List<CardColor> sourceColors) {
        if (grants == null || grants.isEmpty()) {
            return List.of();
        }
        List<DamageBonus> bonuses = new ArrayList<>();
        for (int i = 0; i < grants.size(); i++) {
            DamageBonusGrant grant = grants.get(i);
            if (grant.amount() != 0 && (sourceColors == null || sourceColors.contains(grant.color()))) {
                bonuses.add(new DamageBonus(family + "/" + i, grant.sourceName(), grant.amount()));
            }
        }
        return bonuses;
    }

    /**
     * Whether {@code permanent}'s card, as a layered pass reads it, prints a static effect that is an
     * instance of one of {@code types}. A scan over the printed static effects tests this before its
     * layered ability-loss query: the loss only matters to a permanent that has the effect, and outside a
     * query scope every such query fingerprints the whole board, so an unscoped scan would otherwise cost
     * the board's size squared.
     */
    private static boolean printsStaticEffect(GameData gameData, Permanent permanent, Class<?>... types) {
        for (CardEffect effect : LayerSystemService.currentCard(gameData, permanent).getStaticEffects()) {
            for (Class<?> type : types) {
                if (type.isInstance(effect)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Visits each static effect of {@code permanent} with an instance key unique to that effect. */
    private static void forEachStaticEffect(Permanent permanent,
                                            java.util.function.BiConsumer<CardEffect, String> visitor) {
        List<CardEffect> statics = permanent.getCard().getStaticEffects();
        for (int i = 0; i < statics.size(); i++) {
            visitor.accept(statics.get(i), permanent.getId() + "/" + i);
        }
    }

    /**
     * Returns {@code true} if the given creature is prevented from dealing damage by an effect read before the damage
     * pools: an Aura's {@link PreventAllDamageDealtByEnchantedCreatureEffect}, a global creature or color prevention,
     * or a per-permanent flag. A static {@code PreventDamageToRecipientsEffect} over the damage's source
     * ({@code dealtBy}) is not one of them: it is an option of the recipient's pool (CR 616.1).
     */
    public boolean isPreventedFromDealingDamage(GameData gameData, Permanent creature) {
        return isPreventedFromDealingDamage(gameData, creature, false);
    }

    /**
     * Returns {@code true} if the given creature is prevented from dealing damage.
     * When {@code isCombatDamage} is {@code true}, also checks for combat-specific
     * prevention effects (e.g. {@link PreventAllCombatDamageToAndByEnchantedCreatureEffect}).
     */
    public boolean isPreventedFromDealingDamage(GameData gameData, Permanent creature, boolean isCombatDamage) {
        // Assigning no combat damage isn't prevention, so unpreventable damage doesn't override it
        if (isCombatDamage && gameData.assignsNoCombatDamage(creature.getId())) return true;
        if (!isDamagePreventable(gameData, isCombatDamage)) return false;
        if (damageCantBePreventedFromSource(gameData, creature, isCombatDamage)) return false;
        UUID sourceControllerId = findPermanentController(gameData, creature.getId());
        if (isCombatDamage && sourceControllerId != null
                && gameData.hasPreventionOnPlayer(sourceControllerId,
                        PreventionScope.ALL_COMBAT_BY_TARGET_OPPONENT_CREATURES)
                && !damageCantBePreventedFromSource(gameData, creature)) {
            return true;
        }
        boolean globalCreaturePrevention = isDamageByCreaturePrevented(gameData, creature)
                && !hasCreaturePreventionLifeGain(gameData);
        if (globalCreaturePrevention
                || hasAuraWithEffect(gameData, creature,
                effect -> effect instanceof PreventAllDamageDealtByEnchantedCreatureEffect prevented
                        && (!prevented.combatOnly() || isCombatDamage))
                || hasTargetCreatureDamagePrevention(gameData, creature)
                || gameData.isPreventedFromDealingDamage(creature.getId())
                || isDamageFromChosenPermanentPrevented(gameData, creature)) {
            return true;
        }
        if (isDamageFromPermanentSourcePrevented(gameData, creature)) return true;
        if (isCombatDamage && hasAuraWithEffect(gameData, creature,
                effect -> effect instanceof PreventAllCombatDamageToAndByEnchantedCreatureEffect prevented
                        && prevented.subject() instanceof PermanentSubject.Attached)) {
            return true;
        }
        // Fog Bank: "Prevent all combat damage that would be dealt to and dealt by this creature."
        if (isCombatDamage && hasActiveStaticEffect(gameData, creature,
                PreventAllCombatDamageToAndByEnchantedCreatureEffect.class,
                prevented -> prevented.subject() instanceof PermanentSubject.Source)) {
            return true;
        }
        if (isCombatDamage && creature.isAttacking()
                && gameData.hasPrevention(PreventionScope.ALL_COMBAT_BY_ATTACKING_CREATURES)) {
            return true;
        }
        if (isCombatDamage
                && gameData.hasPreventionOnPermanent(creature.getId(), true,
                PreventionScope.ALL_BY_TARGET_CREATURES, PreventionScope.ALL_BY_SELF)) {
            return true;
        }
        if (isCombatDamage && gameData.combatDamageExemptPredicate != null
                && !predicateEvaluationService.matchesPermanentPredicate(creature, gameData.combatDamageExemptPredicate,
                FilterContext.of(gameData).withSourceControllerId(gameData.combatDamageExemptControllerId))) {
            return true;
        }
        if (isCombatDamage && gameData.preventionEffects(PreventionScope.ALL_COMBAT_EXCEPT).stream()
                .anyMatch(exemption -> !predicateEvaluationService.matchesPermanentPredicate(creature,
                        ((PreventDamageEffect) exemption.effect()).exemptPredicate(),
                        FilterContext.of(gameData).withSourceControllerId(exemption.affectedPlayerId())))) {
            return true;
        }
        return false;
    }

    /** Returns whether a named face-up plane's damage is prevented to a player's permanents for the game. */
    public boolean isDamageFromNamedPlanePreventedForControlledPermanent(
            GameData gameData, StackEntry entry, UUID recipientControllerId) {
        if (!isDamagePreventable(gameData) || entry == null || recipientControllerId == null
                || entry.getSourcePlanarObject() == null) {
            return false;
        }
        String planeName = entry.getSourcePlanarObject().getCard().getName();
        return gameData.floatingEffectsOnPlayer(recipientControllerId,
                        PreventDamageFromNamedPlanesToControlledPermanentsEffect.class).stream()
                .anyMatch(prevention -> planeName.equals(
                        ((PreventDamageFromNamedPlanesToControlledPermanentsEffect) prevention.effect()).planeName()));
    }

    /** Returns whether a permanent's own static slot currently carries the requested effect. */
    public boolean hasActiveStaticEffect(GameData gameData, Permanent source,
                                         Class<? extends CardEffect> effectType) {
        return hasActiveStaticEffect(gameData, source, effectType, effect -> true);
    }

    /**
     * {@link #hasActiveStaticEffect(GameData, Permanent, Class)} counting only the effects {@code matching} accepts
     * ("this creature" rather than "enchanted creature" of a record with a subject).
     */
    public <T extends CardEffect> boolean hasActiveStaticEffect(GameData gameData, Permanent source,
                                                                Class<T> effectType, Predicate<T> matching) {
        return ownActiveStaticEffects(gameData, source, effectType).anyMatch(matching);
    }

    /**
     * The effects of {@code effectType} in {@code source}'s own active static abilities, as
     * {@link #hasActiveStaticEffect(GameData, Permanent, Class, Predicate)} judges them: none while it is face down,
     * has lost its printed abilities or has that ability removed; a conditional one once its condition holds.
     */
    private <T extends CardEffect> Stream<T> ownActiveStaticEffects(GameData gameData, Permanent source,
                                                                   Class<T> effectType) {
        UUID controllerId = findPermanentController(gameData, source.getId());
        if (controllerId == null || source.isFaceDown() || gameData.removesOwnStaticAbility(source.getId(), effectType)
                || LayerSystemService.currentCard(gameData, source).getStaticEffects().isEmpty()
                || hasLostPrintedAbilities(gameData, source)) return Stream.empty();
        return source.getCard().getStaticEffects().stream()
                .filter(effect -> !gameData.removesOwnStaticAbility(source.getId(), effect.getClass()))
                .map(effect -> staticEffectConditionResolver.resolve(gameData, source, controllerId, effect))
                .filter(effect -> effect != null && !gameData.removesOwnStaticAbility(source.getId(), effect.getClass()))
                .filter(effectType::isInstance)
                .map(effectType::cast);
    }

    /**
     * Every "must be blocked [by a [quality] creature] if able" requirement on {@code attacker} (CR 509.1c): the floating
     * ones a resolution put on it, its own active static abilities (not an Equipment's), and those of the Auras and
     * Equipment attached to it
     * (an {@link EnchantedPermanentConditionalEffect} or {@link ConditionalEffect} one while it applies; none from an
     * Aura whose effects are ignored this turn). Each is its own requirement.
     */
    public List<MustBeBlockedIfAbleEffect> mustBeBlockedRequirements(GameData gameData, Permanent attacker) {
        List<MustBeBlockedIfAbleEffect> requirements = new ArrayList<>();
        for (FloatingContinuousEffect floating : gameData.floatingEffectsOnPermanent(attacker.getId(),
                MustBeBlockedIfAbleEffect.class)) {
            requirements.add((MustBeBlockedIfAbleEffect) floating.effect());
        }
        // An Equipment's "equipped creature must be blocked" is its creature's, not the Equipment's own while it is an
        // attacking creature.
        if (!attacker.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            ownActiveStaticEffects(gameData, attacker, MustBeBlockedIfAbleEffect.class).forEach(requirements::add);
        }
        UUID attackerControllerId = findPermanentController(gameData, attacker.getId());
        gameData.forEachPermanent((id, attachment) -> {
            if (!attachment.isAttached() || !attacker.getId().equals(attachment.getAttachedTo())
                    || gameData.ignoresEffectOf(attachment.getId(), IgnoreSourceAuraEffectsUntilEndOfTurnEffect.class,
                    attackerControllerId)) {
                return;
            }
            for (CardEffect effect : attachment.getCard().getStaticEffects()) {
                isActiveEffect(gameData, attachment, attacker, effect, active -> {
                    if (active instanceof MustBeBlockedIfAbleEffect requirement) {
                        requirements.add(requirement);
                    }
                    return false;
                });
            }
        });
        return requirements;
    }

    /** Whether {@code attacker} must be blocked if able by any creature (a requirement with no blocker filter). */
    public boolean mustBeBlockedIfAble(GameData gameData, Permanent attacker) {
        return mustBeBlockedRequirements(gameData, attacker).stream()
                .anyMatch(requirement -> requirement.blockerFilter() == null);
    }

    /** The requirements on {@code attacker} that only a block by a matching creature obeys ("by an Eldrazi"). */
    public List<MustBeBlockedIfAbleEffect> mustBeBlockedByMatchingCreatureRequirements(GameData gameData,
                                                                                       Permanent attacker) {
        return mustBeBlockedRequirements(gameData, attacker).stream()
                .filter(requirement -> requirement.blockerFilter() != null)
                .toList();
    }

    /** Whether {@code blocker} blocking obeys {@code requirement}. */
    public boolean obeysMustBeBlockedRequirement(GameData gameData, MustBeBlockedIfAbleEffect requirement,
                                                 Permanent blocker) {
        return requirement.blockerFilter() == null
                || predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, requirement.blockerFilter());
    }

    /** Whether {@code blocker} blocking {@code attacker} obeys one of its "must be blocked if able" requirements. */
    public boolean obeysAMustBeBlockedRequirement(GameData gameData, Permanent attacker, Permanent blocker) {
        return mustBeBlockedRequirements(gameData, attacker).stream()
                .anyMatch(requirement -> obeysMustBeBlockedRequirement(gameData, requirement, blocker));
    }

    /**
     * Whether another permanent's static "[these creatures] block each combat if able" names {@code blocker} (Grand
     * Melee's "all creatures"): the ability is its source's, so the blocker losing its own abilities doesn't free it.
     */
    public boolean isNamedByStaticMustBlockSet(GameData gameData, Permanent blocker) {
        UUID blockerControllerId = findPermanentController(gameData, blocker.getId());
        if (blockerControllerId == null) {
            return false;
        }
        return activeStaticEffects(gameData, MustBlockEachCombatEffect.class).stream()
                .filter(active -> !(active.effect().subject() instanceof PermanentSubject.Source))
                .anyMatch(active -> staticSubjectNames(gameData, active.source(), active.controllerId(),
                        blockerControllerId, blocker, active.effect().subject()));
    }

    /** Returns whether a permanent carries the requested active static effect, including a grant. */
    public boolean hasActiveStaticEffectIncludingGranted(GameData gameData, Permanent source,
                                                         Class<? extends CardEffect> effectType) {
        return hasActiveStaticEffect(gameData, source, effectType)
                || hasGrantedEffect(gameData, source, effectType);
    }

    /** Returns the current land-play allowance, including conditional static permissions. */
    public int getMaxLandsThisTurn(GameData gameData, UUID playerId) {
        return landPlayPermissionService.getMaxLandsThisTurn(gameData, playerId);
    }

    /** Returns whether Ethereal Haze-style prevention applies to damage from this source. */
    public boolean isDamageByCreaturePrevented(GameData gameData, Permanent source) {
        if (!isDamagePreventable(gameData) || source == null || !isCreature(gameData, source)
                || damageCantBePreventedFromSource(gameData, source)) {
            return false;
        }
        if (gameData.hasPrevention(PreventionScope.ALL_BY_CREATURES)) {
            return true;
        }
        UUID sourceControllerId = findPermanentController(gameData, source.getId());
        return sourceControllerId != null
                && gameData.preventionEffects(PreventionScope.ALL_BY_OPPONENT_CREATURES).stream()
                .anyMatch(prevention -> !prevention.affectedPlayerId().equals(sourceControllerId));
    }

    /** Whether a "prevent all damage that would be dealt by creatures" effect also gains its controller life. */
    public boolean hasCreaturePreventionLifeGain(GameData gameData) {
        return gameData.preventionEffects(PreventionScope.ALL_BY_CREATURES).stream()
                .anyMatch(prevention -> ((PreventDamageEffect) prevention.effect()).gainLife());
    }

    /**
     * Returns whether the source's combat-damage prevention also shuffles the damaged creature away.
     * The shuffle is an additional effect of the prevention effect, so it still happens when the damage
     * can't be prevented (CR 615.12); callers decide separately whether the damage is actually prevented.
     */
    public boolean shufflesTargetIntoOwnersLibraryAfterCombatDamagePrevention(
            GameData gameData, Permanent source, Permanent target) {
        if (source == null || target == null
                || !isCreature(gameData, source) || !isCreature(gameData, target)) {
            return false;
        }
        UUID controllerId = findPermanentController(gameData, source.getId());
        if (controllerId == null) return false;
        return source.getCard().getStaticEffects().stream()
                .map(effect -> staticEffectConditionResolver.resolve(gameData, source, controllerId, effect))
                .filter(DamagePreventionBySelfEffect.class::isInstance)
                .map(DamagePreventionBySelfEffect.class::cast)
                .anyMatch(effect -> effect.combatOnly()
                        && effect.shuffleTargetIntoOwnersLibrary()
                        && (effect.targetFilter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, target, effect.targetFilter())));
    }

    /**
     * Ethersworn Shieldmage: "prevent all damage that would be dealt to [permanents matching a
     * predicate] this turn." Returns {@code true} when the given permanent currently matches any
     * active turn-scoped all-damage-prevention predicate. Re-evaluated per damage event, so it
     * covers permanents that start/stop matching after the effect resolved (official ruling).
     */
    public boolean isAllDamagePreventedByPredicate(GameData gameData, Permanent permanent) {
        return gameData.preventionEffects(PreventionScope.ALL_TO_MATCHING_PERMANENTS).stream()
                .anyMatch(prevention -> predicateEvaluationService.matchesPermanentPredicate(gameData, permanent,
                        ((PreventDamageEffect) prevention.effect()).victimPredicate()));
    }

    /**
     * Emmara Tandris: returns {@code true} when the given creature's controller controls a permanent
     * carrying a {@link PreventAllDamageToCreaturesYouControlEffect} whose filter the creature matches
     * (a {@code null} filter covers every creature that player controls). Both combat and noncombat
     * damage dealt to such a creature is fully prevented by the caller.
     */
    /** Returns whether a controlled matching permanent is protected from damage this turn. */
    public boolean isDamagePreventedByControlledPredicate(GameData gameData, Permanent permanent) {
        return isDamagePreventedByControlledPredicate(gameData, permanent, true);
    }

    /** Returns whether a controlled matching permanent is protected from the requested damage type this turn. */
    public boolean isDamagePreventedByControlledPredicate(GameData gameData, Permanent permanent,
                                                          boolean isCombatDamage) {
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        if (controllerId == null) return false;
        FilterContext context = FilterContext.of(gameData).withSourceControllerId(controllerId);
        if (gameData.preventionEffectsOnPlayer(controllerId,
                        PreventionScope.ALL_TO_CONTROLLED_MATCHING_PERMANENTS).stream()
                .anyMatch(prevention -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, ((PreventDamageEffect) prevention.effect()).victimPredicate(), context))) {
            return true;
        }
        if (!isCombatDamage) return false;
        return gameData.preventionEffectsOnPlayer(controllerId,
                        PreventionScope.ALL_COMBAT_TO_CONTROLLED_MATCHING_PERMANENTS).stream()
                .anyMatch(prevention -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, ((PreventDamageEffect) prevention.effect()).victimPredicate(), context));
    }

    public int getControlledCreatureDamageLimit(GameData gameData, Permanent creature) {
        UUID controllerId = findPermanentController(gameData, creature.getId());
        if (controllerId == null) return Integer.MAX_VALUE;
        int damageLimit = Integer.MAX_VALUE;
        for (ActiveStaticEffect<PreventAllDamageToCreaturesYouControlEffect> active
                : activeStaticEffects(gameData, controllerId, PreventAllDamageToCreaturesYouControlEffect.class)) {
            PreventAllDamageToCreaturesYouControlEffect prevent = active.effect();
            Permanent source = active.source();
            if (prevent.filter() == null
                    || predicateEvaluationService.matchesPermanentPredicate(
                    creature,
                    prevent.filter(),
                    FilterContext.of(gameData)
                            .withSourceCardId(source.getCard().getId())
                            .withSourceControllerId(controllerId)
                            .withSourcePermanentSnapshot(source))) {
                damageLimit = Math.min(damageLimit, prevent.damageLimit());
            }
        }
        if (gameData.planechase != null
                && Objects.equals(gameData.planechase.controllerId, controllerId)) {
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getStaticEffects()) {
                    if (effect instanceof PreventAllDamageToCreaturesYouControlEffect prevent
                            && (prevent.filter() == null
                            || predicateEvaluationService.matchesPermanentPredicate(
                            creature,
                            prevent.filter(),
                            FilterContext.of(gameData)
                                    .withSourceCardId(planar.getCard().getId())
                                    .withSourceControllerId(controllerId)))) {
                        damageLimit = Math.min(damageLimit, prevent.damageLimit());
                    }
                }
            }
        }
        return damageLimit;
    }

    public boolean isAllDamageToControlledCreaturePrevented(GameData gameData, Permanent creature) {
        return getControlledCreatureDamageLimit(gameData, creature) == 0;
    }

    /**
     * Returns {@code true} if damage from sources of the given color is currently prevented
     * (e.g. by a "prevent all damage from [color] sources" effect).
     */
    public boolean isDamageFromSourcePrevented(GameData gameData, CardColor sourceColor) {
        sourceColor = getDamageSourceColor(gameData, sourceColor);
        CardColor damageColor = sourceColor;
        return damageColor != null && gameData.preventionEffects(PreventionScope.ALL_FROM_COLORS).stream()
                .anyMatch(prevention -> ((PreventDamageEffect) prevention.effect()).sourceColors()
                        .contains(damageColor));
    }

    /** Returns whether damage from the given permanent is prevented by an active source-based effect. */
    public boolean isDamageFromPermanentSourcePrevented(GameData gameData, Permanent source) {
        if (!isDamagePreventable(gameData) || source == null) return false;
        if (hasAuraWithEffect(gameData, source,
                        effect -> effect instanceof PreventAllDamageDealtByEnchantedCreatureEffect prevented
                                && !prevented.combatOnly())
                || hasTargetCreatureDamagePrevention(gameData, source)) {
            return true;
        }
        if (gameData.hasPrevention(PreventionScope.ALL_FROM_NON_HUMAN_SOURCES)
                && !effectiveCreatureSubtypes(gameData, source).contains(CardSubtype.HUMAN)) {
            return true;
        }
        return getEffectiveColors(gameData, source).stream()
                .anyMatch(color -> isDamageFromSourcePrevented(gameData, color));
    }

    private boolean hasTargetCreatureDamagePrevention(GameData gameData, Permanent creature) {
        synchronized (gameData.floatingEffects) {
            return gameData.floatingEffects.stream()
                    .anyMatch(floating -> creature.getId().equals(floating.affectedPermanentId())
                            && floating.effect() instanceof PreventDamageEffect prevention
                            && prevention.scope() == PreventionScope.ALL_BY_TARGET_CREATURES_WHILE_SOURCE_REMAINS);
        }
    }

    /** Returns whether damage from the given non-permanent source card is prevented. */
    public boolean isDamageFromCardSourcePrevented(GameData gameData, Card sourceCard) {
        if (!isDamagePreventable(gameData) || sourceCard == null) return false;
        if (gameData.isPreventedFromDealingDamage(sourceCard.getId())) return true;
        if (gameData.hasPrevention(PreventionScope.ALL_FROM_NON_HUMAN_SOURCES)
                && !getCardSubtypes(sourceCard, gameData, sourceCard.getOwnerId()).contains(CardSubtype.HUMAN)) {
            return true;
        }
        return getEffectiveCardColors(gameData, sourceCard).stream()
                .anyMatch(color -> isDamageFromSourcePrevented(gameData, color));
    }

    /** Returns whether damage from the source represented by the stack entry is prevented. */
    public boolean isDamageFromStackEntryPrevented(GameData gameData, StackEntry entry) {
        if (entry == null) return false;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null && entry.getDamageSourceCard() != null
                && entry.getDamageSourceCard() != source.getCard()) {
            // Another object (a granting Equipment) deals the damage, not the permanent holding the ability.
            source = null;
        }
        return source != null
                ? isDamageFromPermanentSourcePrevented(gameData, source)
                : isDamageFromCardSourcePrevented(gameData, entry.getEffectiveDamageSourceCard());
    }

    /**
     * Ghostly Flame: returns the colour a damage source is treated as having <em>for damage
     * purposes</em>. Yields {@code null} (colourless) when a {@link DamageSourcesOfColorsAreColorlessEffect}
     * on the battlefield covers {@code sourceColor}; otherwise returns {@code sourceColor} unchanged.
     * Only damage paths may use this â€” the source keeps its real colour for blocking, targeting,
     * and enchanting.
     */
    public CardColor getDamageSourceColor(GameData gameData, CardColor sourceColor) {
        return isDamageSourceColorNullified(gameData, sourceColor) ? null : sourceColor;
    }

    /** Damage-purposes colour set: {@link #getDamageSourceColor} applied to every colour. */
    public Set<CardColor> getDamageSourceColors(GameData gameData, Set<CardColor> sourceColors) {
        if (sourceColors == null || sourceColors.isEmpty()) return Set.of();
        if (sourceColors.stream().anyMatch(color -> isDamageSourceColorNullified(gameData, color))) {
            return Set.of();
        }
        return new HashSet<>(sourceColors);
    }

    private boolean isDamageSourceColorNullified(GameData gameData, CardColor sourceColor) {
        if (sourceColor == null) return false;
        return gameData.anyPermanentMatches(permanent ->
                permanent.getCard().getStaticEffects().stream()
                        .anyMatch(effect -> effect instanceof DamageSourcesOfColorsAreColorlessEffect colorless
                                && colorless.colors().contains(sourceColor)));
    }

    /**
     * Counts the number of permanents with the given subtype controlled by the specified player.
     */
    /**
     * Counts the permanents controlled by {@code controllerId} that match {@code predicate}.
     * Used by ability activation restrictions such as Leechridden Swamp's "Activate only if you
     * control two or more black permanents".
     */
    /** Whether a floating {@link PermanentLockEffect} (e.g. Edifice of Authority) forbids the given
     *  permanent from being declared as an attacker. */
    public boolean isLockedFromAttacking(GameData gameData, UUID permanentId) {
        return hasPermanentLock(gameData, permanentId, PermanentLockEffect::locksAttacking);
    }

    /** Whether the given attacker skips tapping because of an active combat permission. */
    public boolean attackingDoesNotCauseTapping(GameData gameData, Permanent attacker) {
        UUID attackerControllerId = findPermanentController(gameData, attacker.getId());
        if (attackerControllerId == null) {
            return false;
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floatingEffect : gameData.floatingEffects) {
                if (!attackerControllerId.equals(floatingEffect.controllerId())
                        || !(floatingEffect.effect() instanceof AttackWithoutTappingPermissionEffect permission)
                        || !permission.allowsAttackingWithoutTapping()) {
                    continue;
                }
                Permanent source = findPermanentById(gameData, floatingEffect.sourcePermanentId());
                if (source != null && !source.isTapped()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether a floating {@link PermanentLockEffect} forbids the given permanent from blocking.
     * The only caller is {@code BlockLegalityService} in {@code service.combat.block}.
     */
    public boolean isLockedFromBlocking(GameData gameData, UUID permanentId) {
        return hasPermanentLock(gameData, permanentId, PermanentLockEffect::locksBlocking);
    }

    /** Whether a floating {@link PermanentLockEffect} forbids activating the given permanent's abilities. */
    public boolean isLockedFromActivatingAbilities(GameData gameData, UUID permanentId) {
        return hasPermanentLock(gameData, permanentId, PermanentLockEffect::locksActivatedAbilities);
    }

    private boolean hasPermanentLock(GameData gameData, UUID permanentId, Predicate<PermanentLockEffect> facet) {
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect fe : gameData.floatingEffects) {
                if (fe.effect() instanceof PermanentLockEffect lock && facet.test(lock)
                        && !johanCantAttackSuppressedByLostAbilities(gameData, fe)
                        && (permanentId.equals(fe.affectedPermanentId())
                        || fe.scope() != null && matchesFloatingLockScope(gameData, permanentId, fe))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Johan's "Johan can't attack" is an ability granted to Johan until end of combat, so it stops
     * applying while Johan has lost all abilities.
     */
    private boolean johanCantAttackSuppressedByLostAbilities(GameData gameData, FloatingContinuousEffect effect) {
        if (!(effect.effect() instanceof JohanCombatEffect)) {
            return false;
        }
        Permanent source = findPermanentById(gameData, effect.sourcePermanentId());
        return source != null && (gameData.losesAllAbilitiesByEffect(source)
                || computeStaticBonus(gameData, source).losesAllAbilities());
    }

    private boolean matchesFloatingLockScope(GameData gameData, UUID permanentId, FloatingContinuousEffect effect) {
        Permanent permanent = findPermanentById(gameData, permanentId);
        Permanent source = findPermanentById(gameData, effect.sourcePermanentId());
        return permanent != null && predicateEvaluationService.matchesPermanentPredicate(permanent, effect.scope(),
                FilterContext.of(gameData).withSourceCardId(source == null ? null : source.getCard().getId())
                        .withSourcePermanentId(effect.sourcePermanentId())
                        .withSourceControllerId(effect.controllerId()));
    }

    public int countControlledPermanentsMatching(GameData gameData, UUID controllerId, PermanentPredicate predicate) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, predicate)) {
                count += CreatureCountSupport.countsCreatures(predicate)
                        ? CreatureCountSupport.creatureCount(gameData, permanent, this) : 1;
            }
        }
        return count;
    }

    public int countControlledSubtypePermanents(GameData gameData, UUID controllerId, CardSubtype subtype) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (hasEffectiveSubtype(gameData, permanent, subtype)
                    || (isCreatureSubtype(subtype) && hasKeyword(gameData, permanent, Keyword.CHANGELING))) {
                count++;
            }
        }
        return count;
    }

    /**
     * CR 302.6: whether summoning sickness stops {@code permanent} paying a {@code {T}} or
     * {@code {Q}} cost. Only creatures are affected, and haste â€” printed, granted, or supplied by an
     * "as though it had haste" effect the controller has (Concordant Crossroads on the stack, Hall
     * of the Bandit Lord) â€” lifts it.
     *
     * <p>Shared by the activation validator and by every mana-planning path. A planner that
     * re-derives this without the "as though" clause silently refuses to tap mana creatures the
     * engine would happily let it tap.
     */
    public boolean isSummoningSickForTapCost(GameData gameData, Permanent permanent, UUID controllerId) {
        return permanent.isSummoningSick()
                && isCreature(gameData, permanent)
                && !hasKeyword(gameData, permanent, Keyword.HASTE)
                && !canActivateCreatureAbilitiesAsThoughHaste(gameData, controllerId);
    }

    /**
     * Runs {@code query} with continuous effects evaluated as though {@code permanent} were already on the
     * battlefield under {@code controllerId}, for "as this enters" choices that read the entering
     * permanent's own characteristics (e.g. Amplify's shared creature type under Conspiracy).
     */
    public <T> T withEnteringPermanent(GameData gameData, UUID controllerId, Permanent permanent,
                                       java.util.function.Supplier<T> query) {
        LayerSystemService.Pass pass = layerSystemService.beginPassWithEnteringPermanent(gameData, controllerId, permanent);
        try {
            return query.get();
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /** Checks which printed abilities an entering permanent retains after continuous effects apply. */
    public boolean hasLostPrintedAbilitiesAsEntering(GameData gameData, UUID controllerId, Permanent permanent) {
        LayerSystemService.Pass pass = layerSystemService.beginPassWithEnteringPermanent(gameData, controllerId, permanent);
        try {
            return hasLostPrintedAbilities(gameData, permanent);
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /** Whether a static "can't be chosen" ability keeps the permanent out of every object choice. */
    public boolean cantBeChosen(GameData gameData, Permanent permanent) {
        return !permanent.isFaceDown()
                && permanent.getCard().getStaticEffects().stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.CantBeChosenEffect.class::isInstance)
                && !hasLostAllAbilities(gameData, permanent);
    }

    /**
     * Whether {@code permanent} is out of the running when a player chooses which permanent to
     * sacrifice to a resolving spell or ability ("sacrifice a creature"): it can't be sacrificed, or
     * it can't be chosen (Glimpse, the Unthinkable). Only for sacrifices that involve a choice —
     * "sacrifice all" and "sacrifice this" sacrifices use {@link #cantBeSacrificed} alone.
     */
    public boolean cantBeChosenToSacrifice(GameData gameData, Permanent permanent) {
        return cantBeSacrificed(gameData, permanent) || cantBeChosen(gameData, permanent);
    }

    /**
     * As {@link #cantBeChosenToSacrifice(GameData, Permanent)} for a sacrifice described by
     * {@code filter}, except that a filter naming one specific object ("sacrifice it", "sacrifice
     * this creature") involves no choice, so only "can't be sacrificed" applies to it.
     */
    public boolean cantBeChosenToSacrifice(GameData gameData, Permanent permanent,
                                           com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter) {
        return designatesSpecificObject(filter)
                ? cantBeSacrificed(gameData, permanent)
                : cantBeChosenToSacrifice(gameData, permanent);
    }

    private static boolean designatesSpecificObject(
            com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter) {
        if (filter instanceof com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate allOf) {
            return allOf.predicates().stream().anyMatch(GameQueryService::designatesSpecificObject);
        }
        return filter instanceof com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate
                || filter instanceof com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate
                || filter instanceof com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate
                || filter instanceof com.github.laxika.magicalvibes.model.filter.PermanentIsTriggeringPermanentPredicate
                || filter instanceof com.github.laxika.magicalvibes.model.filter.PermanentIsTargetCardPredicate;
    }

    /** Whether a battlefield permanent's static ability forbids choosing this card name. */
    public boolean isCardNameUnchoosable(GameData gameData, String name) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (!permanent.isFaceDown()
                        && permanent.getCard().getName().equalsIgnoreCase(name)
                        && permanent.getCard().getStaticEffects().stream().anyMatch(
                                com.github.laxika.magicalvibes.model.effect.SourceNameCantBeChosenEffect.class::isInstance)
                        && !hasLostAllAbilities(gameData, permanent)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the permanent has lost its printed abilities, whether continuously
     * (Imprisoned in the Moon, Song of the Dryads) or until end of turn (Merfolk Trickster). Its
     * printed mana abilities are gone even when a later-timestamp grant leaves it with a mana
     * ability of its own â€” {@code tapPermanent} refuses outright in that state, so a planner must
     * read the granted ability instead of the printed one.
     */
    public boolean hasLostAllAbilities(GameData gameData, Permanent permanent) {
        if (gameData.losesAllAbilitiesByEffect(permanent)) {
            return true;
        }
        CharacteristicState activeState = LayerSystemService.activeStateFor(permanent.getId());
        if (activeState != null) {
            return activeState.isPrintedAbilitiesRemoved() || activeState.isLosesAllAbilities();
        }
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            CharacteristicState state = pass.board().states().get(permanent.getId());
            return state != null && (state.isPrintedAbilitiesRemoved() || state.isLosesAllAbilities());
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /** Whether a continuously granted non-mana ability survives later ability removal. */
    public boolean grantedAbilitySurvivesRemoval(GameData gameData, Permanent permanent, long grantTimestamp) {
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            CharacteristicState state = pass.board().states().get(permanent.getId());
            return state == null ? !gameData.losesAllAbilitiesByEffect(permanent)
                    : !state.isAbilityGainProhibited()
                    && (!state.isLosesAllAbilities() || grantTimestamp > state.getLosesAllAbilitiesTimestamp())
                    && (!state.isLosesAllNonManaAbilities()
                    || grantTimestamp > state.getLosesAllNonManaAbilitiesTimestamp());
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /**
     * Whether an effect makes {@code permanent} lose all its abilities, or all its non-mana abilities (Humility). Unlike
     * {@link #hasLostPrintedAbilities}, a basic land type set on a land (Blood Moon) doesn't count: it removes only the
     * abilities generated from the land's rules text (CR 305.7), so intrinsic abilities the rules give it remain.
     */
    public boolean losesAllAbilitiesByEffects(GameData gameData, Permanent permanent) {
        if (gameData.losesAllAbilitiesByEffect(permanent)) {
            return true;
        }
        CharacteristicState activeState = LayerSystemService.activeStateFor(permanent.getId());
        if (activeState != null) {
            return activeState.isLosesAllAbilities() || activeState.isLosesAllNonManaAbilities();
        }
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            CharacteristicState state = pass.board().states().get(permanent.getId());
            return state != null && (state.isLosesAllAbilities() || state.isLosesAllNonManaAbilities());
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /**
     * The chapter abilities Saga {@code saga} has as the layers leave it (CR 715.2): its card's, unless it lost its
     * printed abilities (CR 613.1f), in the order they were added.
     */
    public List<TriggeredAbility> sagaChapterAbilities(GameData gameData, Permanent saga) {
        if (saga.isFaceDown() || hasLostPrintedAbilities(gameData, saga)) {
            return List.of();
        }
        return saga.getCard().getTriggeredAbilities().stream()
                .filter(ability -> ability.conditions().stream()
                        .anyMatch(condition -> condition.trigger() instanceof ChapterTrigger))
                .toList();
    }

    /**
     * Saga {@code saga}'s final chapter number as the layers leave it: the greatest value among the chapter abilities
     * it has, 0 when it has none (CR 715.2d).
     */
    public int sagaFinalChapterNumber(GameData gameData, Permanent saga) {
        int finalChapter = 0;
        for (TriggeredAbility ability : sagaChapterAbilities(gameData, saga)) {
            for (var condition : ability.conditions()) {
                if (condition.trigger() instanceof ChapterTrigger chapter) {
                    finalChapter = Math.max(finalChapter, chapter.chapter());
                }
            }
        }
        return finalChapter;
    }

    public boolean hasLostPrintedAbilities(GameData gameData, Permanent permanent) {
        if (gameData.losesAllAbilitiesByEffect(permanent)) {
            return true;
        }
        CharacteristicState activeState = LayerSystemService.activeStateFor(permanent.getId());
        if (activeState != null) {
            return activeState.isPrintedAbilitiesRemoved()
                    || activeState.isLosesAllAbilities()
                    || activeState.isLosesAllNonManaAbilities();
        }
        LayerSystemService.Pass pass = layerSystemService.beginPass(gameData);
        try {
            CharacteristicState state = pass.board().states().get(permanent.getId());
            return state != null && (state.isPrintedAbilitiesRemoved()
                    || state.isLosesAllAbilities()
                    || state.isLosesAllNonManaAbilities());
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    /**
     * Whether {@code target}'s own (printed) protection abilities are gone: it lost its printed abilities, or a
     * "loses all protection" effect removed them (CR 613.1f). Nothing gives them back; protection a later effect
     * grants (CR 613.7) is a new ability, read from the computed static bonus.
     */
    public boolean hasLostPrintedProtection(GameData gameData, Permanent target) {
        return hasLostPrintedAbilities(gameData, target)
                || computeStaticBonus(gameData, target).printedProtectionRemoved();
    }

    /** Whether a suspected permanent still has the menace and can't-block abilities. */
    public boolean hasSuspectedAbilities(GameData gameData, Permanent permanent) {
        if (!permanent.isSuspected() || gameData.losesAllAbilitiesByEffect(permanent)) {
            return false;
        }
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        return !bonus.losesAllAbilities() && !bonus.losesAllNonManaAbilities();
    }

    /**
     * Returns {@code true} if the permanent's mana abilities can currently be activated,
     * i.e. no static lock (Stony Silence, Pithing Needle with blocksManaAbilities, Phyrexian Revoker)
     * or aura-based lock (Arrest, Ice Cage, Serra Bestiary) prevents it.
     *
     * <p>This is the gate every mana-planning path shares â€” the virtual pool, the AI's payment
     * search, and the client's "castable if you tap your lands" projection all consult it before
     * counting a source. A lock the engine enforces but this method misses becomes mana a planner
     * counts on and can never produce: it taps everything, comes up short, and fires an action the
     * engine silently refuses. So the player-level locks below are mirrored here even though they
     * are not properties of the permanent.
     */
    public boolean canActivateManaAbility(GameData gameData, Permanent permanent) {
        // City of Solitude, Grand Abolisher: a lock on every ability, or on abilities of the permanent's card types,
        // stops its mana abilities too (lands keep producing under Grand Abolisher).
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        if (controllerId != null && abilityLockRefusal(gameData, controllerId, permanent) != null) {
            return false;
        }

        // Sen Triplets: a player locked out this turn can activate no ability at all, mana included.
        if (controllerId != null && gameData.hasFloatingEffectOnPlayer(controllerId, SenTripletsUpkeepEffect.class)) {
            return false;
        }

        // A "loses all abilities" effect, static (Imprisoned in the Moon) or resolved (Merfolk Trickster), strips the
        // printed mana abilities and those granted before it; a mana ability granted after it still works (CR 613.1f,
        // 613.7).
        StaticBonus staticBonus = computeStaticBonus(gameData, permanent);
        if (staticBonus.losesAllAbilities()) {
            boolean hasGrantedMana = staticBonus.grantedActivatedAbilities().stream()
                    .anyMatch(AbilityActivationService::isManaAbility);
            if (!hasGrantedMana) {
                return false;
            }
        }

        // Check aura-based locks (Arrest, Ice Cage)
        if (hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateAbilitiesEffect.class)) {
            return false;
        }
        if (hasAuraWithEffect(gameData, permanent,
                effect -> effect instanceof EnchantedPermanentAbilityLockEffect lock && lock.blocksManaAbilities())) {
            return false;
        }
        if (isLockedFromActivatingTapAbilities(gameData, permanent)) {
            return false;
        }

        // Floating per-permanent lock (Detain, Xathrid Gorgon's petrification) â€” mana abilities
        // are activated abilities too, so a blanket lock stops them as well.
        if (isLockedFromActivatingAbilities(gameData, permanent.getId())) {
            return false;
        }

        if (!anyManaAbilityLock(gameData)) {
            return true;
        }
        for (ActiveStaticEffect<CardEffect> active : activeStaticEffects(gameData, CardEffect.class)) {
            UUID pid = active.controllerId();
            Permanent p = active.source();
            CardEffect effect = active.effect();
            if (effect instanceof ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect lock
                    && lock.blocksManaAbilities()) {
                FilterContext filterContext = FilterContext.of(gameData)
                        .withSourceCardId(p.getCard().getId())
                        .withSourceControllerId(pid)
                        .withSourcePermanentId(p.getId());
                if (predicateEvaluationService.matchesPermanentPredicate(
                        permanent, lock.predicate(), filterContext)) {
                    return false;
                }
            }
            if (effect instanceof ActivatedAbilitiesOfChosenNameCantBeActivatedEffect lock) {
                if (lock.blocksManaAbilities() && permanentHasName(gameData, permanent, p.getChosenName())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Whether any active static effect on the battlefield is a lock that can stop a mana ability (Stony Silence,
     * Phyrexian Revoker): the board-wide first question of {@link #canActivateManaAbility}, answered once per query
     * scope, so judging every mana source of a board without such a lock skips the per-source walk of the board's
     * static effects. Asks exactly what that walk tests before it judges the permanent.
     */
    private boolean anyManaAbilityLock(GameData gameData) {
        return boardScan(gameData, new BoardScan(BoardScan.Kind.MANA_ABILITY_LOCK, null), () ->
                activeStaticEffects(gameData, CardEffect.class).stream()
                        .map(ActiveStaticEffect::effect)
                        .anyMatch(effect -> (effect instanceof ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect
                                matchingLock && matchingLock.blocksManaAbilities())
                                || (effect instanceof ActivatedAbilitiesOfChosenNameCantBeActivatedEffect namedLock
                                && namedLock.blocksManaAbilities())));
    }

    /**
     * Returns {@code true} if the permanent's activated abilities with {T} in their costs are locked (CR 602.5) by a
     * {@link MatchingPermanentsCantActivateTapAbilitiesEffect}: an attached Aura's lock (Serra Bestiary), or a set the
     * active static ability of a battlefield permanent names (Katabatic Winds); a face-down or ability-less source
     * locks nothing.
     */
    public boolean isLockedFromActivatingTapAbilities(GameData gameData, Permanent permanent) {
        if (hasAuraWithEffect(gameData, permanent, effect -> effect instanceof MatchingPermanentsCantActivateTapAbilitiesEffect lock
                && lock.locksAttachedPermanent())) {
            return true;
        }
        if (!anyBattlefieldPrintsStaticEffect(gameData, MatchingPermanentsCantActivateTapAbilitiesEffect.class)) {
            return false;
        }
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        for (ActiveStaticEffect<MatchingPermanentsCantActivateTapAbilitiesEffect> active
                : activeStaticEffects(gameData, MatchingPermanentsCantActivateTapAbilitiesEffect.class)) {
            if (!active.effect().locksAttachedPermanent() && staticSubjectNames(gameData, active.source(),
                    active.controllerId(), controllerId, permanent, active.effect().subject())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the overridden mana color for a land whose land types have been set to a
     * <em>single</em> basic land type by a type-changing effect (Evil Presence, Convincing Mirage,
     * Blood Moon, Tideshaper Mystic, ...), or {@code null} if no land-type-setting effect applies
     * or the setter grants multiple types (see {@link #getOverriddenLandManaColors}). Resolved by
     * the CR 613 layer-4 pass, so of several setters the latest timestamp wins (CR 613.7).
     */
    public ManaColor getOverriddenLandManaColor(GameData gameData, Permanent permanent) {
        List<ManaColor> colors = getOverriddenLandManaColors(gameData, permanent);
        return colors.size() == 1 ? colors.getFirst() : null;
    }

    /**
     * Returns the mana colors corresponding to every basic land type a type-changing effect set
     * on this permanent (Evil Presence â†’ one color; Lush Growth â†’ red, green, and white), or an
     * empty list if no land-type-setting effect applies. Order follows the effect's subtype list.
     */
    public List<ManaColor> getOverriddenLandManaColors(GameData gameData, Permanent permanent) {
        List<CardSubtype> override = layerSystemService.landTypeOverrideFor(gameData, permanent.getId());
        if (override == null || override.isEmpty()) {
            return List.of();
        }
        return override.stream()
                .map(EnchantedPermanentBecomesTypeEffect::manaColorForLandSubtype)
                .toList();
    }

    private static List<FloatingContinuousEffect> concat(List<FloatingContinuousEffect> first,
                                                         List<FloatingContinuousEffect> second) {
        List<FloatingContinuousEffect> all = new ArrayList<>(first);
        all.addAll(second);
        return all;
    }
}
