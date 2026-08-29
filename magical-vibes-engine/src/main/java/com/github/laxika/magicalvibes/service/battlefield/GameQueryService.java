package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetSpellDamagePreventionShield;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.action.DelayedDamageDoubling;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilityTimingEffect;
import com.github.laxika.magicalvibes.model.effect.BasicLandManaProducesAnyColorEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateTapAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkerLoyaltyAbilitiesCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.CombatTaxKind;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsCantActivateTapAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.AllowExtraLoyaltyActivationEffect;
import com.github.laxika.magicalvibes.model.effect.AllowExtraBoastActivationEffect;
import com.github.laxika.magicalvibes.model.effect.AllowLoyaltyActivationAtInstantSpeedEffect;
import com.github.laxika.magicalvibes.model.effect.AllowExtraExhaustActivationEffect;
import com.github.laxika.magicalvibes.model.effect.AllCardsAreColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateControlledEnchantmentsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.BlockCostEffect;
import com.github.laxika.magicalvibes.model.effect.BlockabilityRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.BlockingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.BuffTargetCreatureIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfKeywordIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesMatchingCantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockCreaturesWithPowerGreaterOrEqualToOwnToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantTransformEffect;
import com.github.laxika.magicalvibes.model.effect.EchoCostAlternativeEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCanCastSpellsOnlyAtSorcerySpeedEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCanCastAndActivateOnlyDuringOwnTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCanCastSpellsOnlyDuringOwnTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCanCastSpellsOnlyDuringOwnTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastInstantsOrActivateNonManaAbilitiesDuringCombatEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentEffectsCantCauseDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentEffectsCantCauseSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentLifeGainBecomesLifeLossEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantTargetLandsEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentsMatchingLoseSupertypeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledSourceCreatureDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.PreventTransformEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsPermanentsCantBeTurnedFaceUpEffect;
import com.github.laxika.magicalvibes.model.effect.AttackCostEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCombatTaxEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalBlockLifeCostEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalBlockCostEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetColorMode;
import com.github.laxika.magicalvibes.model.effect.IgnoreOpponentCreatureHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreOpponentHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreOpponentHexproofUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.WallOnlyTargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingSourceKind;
import com.github.laxika.magicalvibes.model.effect.CantBeEnchantedByOtherAurasEffect;
import com.github.laxika.magicalvibes.model.effect.CantBecomeSuspectedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeEquippedEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveOrGainKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.CantBecomeUntappedEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.CountersCantBePlacedEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveMinusOneMinusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantHavePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CounterReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerCounterReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerCantGetPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MeliraPoisonReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CantWinGameEffect;
import com.github.laxika.magicalvibes.model.effect.AllDamageDealtWithWitherEffect;
import com.github.laxika.magicalvibes.model.effect.NoncombatDamageToOpponentCreaturesAsMinusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.effect.SourceDamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesCombatDamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsAndAbilitiesCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;
import com.github.laxika.magicalvibes.model.effect.LifeFloorCondition;
import com.github.laxika.magicalvibes.model.effect.DamageDealtAsInfectBelowZeroLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourcesOfColorsAreColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerHasProtectionFromChosenNameEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerHasProtectionFromOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenNameEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromInstantAndSorcerySpellsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamageFromSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceSpellDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceDamageAboveThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.OjerAxonilDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceDamageAboveThresholdThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ActivateCreatureAbilitiesAsThoughHasteEffect;
import com.github.laxika.magicalvibes.model.effect.SpendWhiteManaAsAnyColorEffect;
import com.github.laxika.magicalvibes.model.effect.SpendWhiteManaAsRedEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorEffect;
import com.github.laxika.magicalvibes.model.effect.SpendBlueManaAsAnyColorForActivatedAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantActivateAbilitiesOfGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSpellsCantBeCastFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.CardsCantEnterBattlefieldFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPayLifeOrSacrificeCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentLockEffect;
import com.github.laxika.magicalvibes.model.effect.AttackWithoutTappingPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.LifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentLifeLossReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CrewAndSaddlePowerModifierEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ArtifactOrCreatureEnteringDontCauseTriggersEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureEnteringDontCauseTriggersEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureDyingDontCauseTriggersEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCreatureSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ETBDoubleTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentPermanentsEnteringDontCauseTriggersEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalColorSourceDamageEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DamageToPlayersAndBattlesBonusEffect;
import com.github.laxika.magicalvibes.model.effect.NoncreatureSourceDamageBonusEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalDamageToOpponentsFromRedOrArtifactSourcesEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentDamageBonusEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerOpponentDamageBonusEffect;
import com.github.laxika.magicalvibes.model.effect.SourceOpponentDamageBonusEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalDamageToPlayersFromColorSourcesEffect;
import com.github.laxika.magicalvibes.model.effect.SpellDamageBonusEffect;
import com.github.laxika.magicalvibes.model.effect.SpellDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerRecipientDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.effect.SourceDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDeathtouchToControllerSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToControllerAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPlayerCantActivateNonManaNonLoyaltyAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleEquippedCreatureCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeToOwnNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardAbilityGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardSubtypeGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardsCantBeTargetedEffect;
import com.github.laxika.magicalvibes.model.effect.MadnessGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardStaticEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SpellCastingAbilityGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.ManaProducingEffect;
import com.github.laxika.magicalvibes.model.effect.ManaReflectionEffect;
import com.github.laxika.magicalvibes.model.effect.TwistBasicLandManaColorsEffect;
import com.github.laxika.magicalvibes.model.effect.LandManaProducesFixedColorEffect;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageDealtByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndBySelfEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageBySelfEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventColorDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToSelfFromCreaturesItBlocksEffect;
import com.github.laxika.magicalvibes.model.effect.DamagePreventionBySelfEffect;
import com.github.laxika.magicalvibes.model.effect.TargetedSpellDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.SharedColorDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsOfPermanentsYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.layer.ModifierLine;
import com.github.laxika.magicalvibes.service.effect.GrantedEffectAttribution;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandler;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.LoyaltyDamageReplacementHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectConditionResolver;
import com.github.laxika.magicalvibes.service.effect.TextChangeTransformer;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import com.github.laxika.magicalvibes.model.CounterType;

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
    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST,
            CardSubtype.MOUNTAIN,
            CardSubtype.ISLAND,
            CardSubtype.PLAINS,
            CardSubtype.SWAMP,
            CardSubtype.DESERT,
            CardSubtype.GATE,
            CardSubtype.LOCUS,
            CardSubtype.AURA,
            CardSubtype.EQUIPMENT,
            CardSubtype.AJANI,
            CardSubtype.KOTH,
            CardSubtype.BOLAS
    );

    static {
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
     * Aggregated static bonuses from other permanents, auras, emblems, and self-referencing
     * effects for a single permanent. Computed on-the-fly by {@link #computeStaticBonus} and
     * never stored on the permanent itself.
     *
     * @param power                     total power modifier from static effects
     * @param toughness                 total toughness modifier from static effects
     * @param keywords                  keywords granted by static effects
     * @param protectionColors          protection colors granted by static effects
     * @param removedProtectionColors   protection colors removed by static effects
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
                              Set<CardColor> removedProtectionColors, boolean animatedCreature,
                              List<ActivatedAbility> grantedActivatedAbilities, List<CardEffect> grantedEffects,
                              Set<CardColor> grantedColors, List<CardSubtype> grantedSubtypes,
                              Set<CardType> grantedCardTypes, Set<CardSupertype> grantedSupertypes,
                              boolean colorOverriding, boolean subtypeOverriding, boolean landSubtypeOverriding,
                              boolean cardTypeOverriding, Set<Keyword> removedKeywords, boolean basePTOverridden,
                              int basePowerOverride, int baseToughnessOverride, boolean losesAllAbilities,
                              boolean losesAllNonManaAbilities, boolean ptSwitched, String name) {
        static final StaticBonus NONE = new StaticBonus(0, 0, Set.of(), Set.of(), Set.of(), false,
                List.of(), List.of(), Set.of(), List.of(), Set.of(), Set.of(), false, false, false, false,
                Set.of(), false, 0, 0, false, false, false, null);

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
            this(power, toughness, keywords, protectionColors, Set.of(), animatedCreature,
                    grantedActivatedAbilities, grantedEffects, grantedColors, grantedSubtypes,
                    grantedCardTypes, grantedSupertypes, colorOverriding, subtypeOverriding,
                    landSubtypeOverriding, cardTypeOverriding, removedKeywords,
                    basePTOverridden, basePowerOverride != null ? basePowerOverride : 0,
                    baseToughnessOverride != null ? baseToughnessOverride : 0,
                    losesAllAbilities, false, ptSwitched, null);
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
     * Returns true if the given player cast at least one historic spell (artifact, legendary, or Saga) this turn.
     */
    public boolean playerCastHistoricSpellThisTurn(GameData gameData, UUID playerId) {
        return gameData.getSpellsCastThisTurn(playerId).stream()
                .anyMatch(card -> predicateEvaluationService.matchesCardPredicate(card, new CardIsHistoricPredicate(), card.getId()));
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
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        for (Permanent perm : bf) {
            if (perm.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(effectType::isInstance)) {
                return true;
            }
        }
        return false;
    }

    public String findAlternativeEchoCost(GameData gameData, UUID playerId) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return null;
        for (Permanent permanent : bf) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof EchoCostAlternativeEffect alternative) {
                    return alternative.alternativeEchoCost();
                }
            }
        }
        return null;
    }

    private boolean playerHasTemporaryStaticEffect(GameData gameData, UUID playerId,
                                                   Class<? extends CardEffect> effectType) {
        List<CardEffect> effects = gameData.playerStaticEffectsUntilEndOfTurn.get(playerId);
        return effects != null && effects.stream().anyMatch(effectType::isInstance);
    }

    /**
     * The keyword-matching sibling of {@link #playerBattlefieldHasStaticEffect}: {@code true} when the
     * player controls a permanent whose STATIC slot grants them {@code keyword}.
     */
    private boolean playerBattlefieldGrantsControllerKeyword(GameData gameData, UUID playerId, Keyword keyword) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
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
     * "Non-Human Werewolves you control can't transform"), or because it is enchanted by an Aura with
     * {@link EnchantedCreatureCantTransformEffect} (e.g. Bound by Moonsilver). The filter is evaluated
     * against the permanent's current face.
     */
    public boolean isTransformPrevented(GameData gameData, Permanent permanent) {
        if (hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantTransformEffect.class)) {
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
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof PreventTransformEffect prevent
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, prevent.filter())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether a permanent controlled by an opponent of the active player can't be turned
     * face up because the active player controls a matching static restriction.
     */
    public boolean isTurnFaceUpPrevented(GameData gameData, Permanent permanent) {
        UUID permanentControllerId = findPermanentController(gameData, permanent.getId());
        UUID activePlayerId = gameData.activePlayerId;
        if (permanentControllerId == null || activePlayerId == null
                || activePlayerId.equals(permanentControllerId)) {
            return false;
        }
        List<Permanent> activeBattlefield = gameData.playerBattlefields.get(activePlayerId);
        if (activeBattlefield == null) {
            return false;
        }
        return activeBattlefield.stream()
                .filter(source -> !source.isFaceDown())
                .filter(source -> !source.isLosesAllAbilitiesUntilEndOfTurn()
                        && !computeStaticBonus(gameData, source).losesAllAbilities())
                .flatMap(source -> source.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(OpponentsPermanentsCantBeTurnedFaceUpEffect.class::isInstance);
    }

    /**
     * Returns {@code true} if the permanent currently has the given supertype, accounting for
     * global static supertype removals ({@link PermanentsMatchingLoseSupertypeEffect}, e.g.
     * Melting's "All lands are no longer snow") and per-permanent supertype changes recorded by
     * {@code SetTargetPermanentSupertypeEffect} (Arcum's Weathervane). Every supertype check that
     * such an effect can touch must route through this instead of reading the printed supertypes.
     * An explicit per-permanent grant is the most recent effect on that permanent, so it wins over
     * a global removal.
     */
    public boolean hasEffectiveSupertype(GameData gameData, Permanent permanent, CardSupertype supertype) {
        if (permanent.getPersistentGrantedSupertypes().contains(supertype)) {
            return true;
        }
        if (permanent.getPersistentRemovedSupertypes().contains(supertype)) {
            return false;
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
            return activeState.hasSupertype(supertype)
                    && !losesSupertypeFromGlobalStaticEffect(gameData, permanent, supertype);
        }
        if (!permanent.getCard().getSupertypes().contains(supertype)) {
            if (gameData == null) {
                return false;
            }
            return computeStaticBonus(gameData, permanent).grantedSupertypes().contains(supertype);
        }
        if (gameData == null) {
            return true;
        }
        return !losesSupertypeFromGlobalStaticEffect(gameData, permanent, supertype);
    }

    private boolean losesSupertypeFromGlobalStaticEffect(
            GameData gameData, Permanent permanent, CardSupertype supertype) {
        return gameData.anyPermanentMatches(source -> source.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(effect -> effect instanceof PermanentsMatchingLoseSupertypeEffect lose
                        && lose.supertype() == supertype
                        && (isStaticEvaluationActive()
                        ? predicateEvaluationService.matchesStaticFilter(
                                permanent, lose.filter(), FilterContext.of(gameData))
                        : predicateEvaluationService.matchesPermanentPredicate(
                                gameData, permanent, lose.filter()))));
    }

    private boolean anyBattlefieldHasStaticEffect(GameData gameData, Class<? extends CardEffect> effectType) {
        return gameData.anyPermanentMatches(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(effectType::isInstance));
    }

    // --- Permanent / Card lookups ---

    /**
     * Finds a permanent on any player's battlefield by its unique ID.
     *
     * @return the permanent, or {@code null} if not found
     */
    public Permanent findPermanentById(GameData gameData, UUID permanentId) {
        return findInBattlefields(gameData, permanentId, (playerId, p) -> p);
    }

    /**
     * Finds the controller (player ID) of a permanent by the permanent's unique ID.
     *
     * @return the controlling player's ID, or {@code null} if the permanent is not on any battlefield
     */
    public UUID findPermanentController(GameData gameData, UUID permanentId) {
        return findInBattlefields(gameData, permanentId, (playerId, p) -> playerId);
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
            if (se.getCard().getId().equals(cardId)) {
                return se;
            }
        }
        return null;
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
     * the battlefield. The player id is the card's controller for a spell and its owner elsewhere.
     */
    public boolean cardHasType(Card card, CardType type, GameData gameData, UUID playerId) {
        if (card.hasType(type)) return true;
        if (gameData == null || playerId == null || card.getType() == null
                || !card.getType().isPermanentType()
                || card.hasType(CardType.LAND)) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(effect -> effect instanceof GrantCardTypeToOwnNonlandPermanentsEffect grant
                        && grant.cardType() == type);
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
        if (card.getSubtypes().contains(subtype)) return true;
        if (card.hasType(CardType.CREATURE) && isCreatureSubtype(subtype)
                && hasSelfAllCreatureTypesEffect(card)) return true;
        if (gameData == null || cardOwnerId == null) return false;
        if (!card.hasType(CardType.CREATURE)) return false;
        if (computeGrantedSubtypesForOwnedCreatureCard(gameData, cardOwnerId).contains(subtype)) {
            return true;
        }
        return isCardInGraveyard(gameData, cardOwnerId, card)
                && computeGrantedGraveyardSubtypesForOwnedCreatureCard(gameData, cardOwnerId, card)
                .contains(subtype);
    }

    private static final Set<CardSubtype> BASIC_LAND_SUBTYPES = EnumSet.of(
            CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
            CardSubtype.MOUNTAIN, CardSubtype.FOREST);

    /**
     * The effective basic land types (Plains/Island/Swamp/Mountain/Forest) of a permanent,
     * respecting CR 305.7 land-type overrides (Blood Moon, Tideshaper Mystic) — when a land-type
     * setter is active the printed and one-shot-granted types are replaced by the setter's types.
     * Used for Domain counting.
     */
    public Set<CardSubtype> effectiveBasicLandTypes(GameData gameData, Permanent permanent) {
        Set<CardSubtype> result = EnumSet.noneOf(CardSubtype.class);
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (!bonus.landSubtypeOverriding()) {
            for (CardSubtype st : permanent.getCard().getSubtypes()) {
                if (BASIC_LAND_SUBTYPES.contains(st)) result.add(st);
            }
            for (CardSubtype st : permanent.getGrantedSubtypes()) {
                if (BASIC_LAND_SUBTYPES.contains(st)) result.add(st);
            }
        }
        for (CardSubtype st : bonus.grantedSubtypes()) {
            if (BASIC_LAND_SUBTYPES.contains(st)) result.add(st);
        }
        for (CardSubtype st : permanent.getTransientSubtypes()) {
            if (BASIC_LAND_SUBTYPES.contains(st)) result.add(st);
        }
        return result;
    }

    /**
     * Returns all subtypes of a creature card, including those granted by Arcane Adaptation-style effects.
     */
    public Set<CardSubtype> getCardSubtypes(Card card, GameData gameData, UUID cardOwnerId) {
        Set<CardSubtype> subtypes = new java.util.HashSet<>(card.getSubtypes());
        if (card.hasType(CardType.CREATURE) && hasSelfAllCreatureTypesEffect(card)) {
            for (CardSubtype subtype : CardSubtype.values()) {
                if (isCreatureSubtype(subtype)) subtypes.add(subtype);
            }
        }
        if (gameData != null && cardOwnerId != null && card.hasType(CardType.CREATURE)) {
            subtypes.addAll(computeGrantedSubtypesForOwnedCreatureCard(gameData, cardOwnerId));
            if (isCardInGraveyard(gameData, cardOwnerId, card)) {
                subtypes.addAll(computeGrantedGraveyardSubtypesForOwnedCreatureCard(gameData, cardOwnerId, card));
            }
        }
        return subtypes;
    }

    private boolean hasSelfAllCreatureTypesEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(effect -> effect instanceof GrantAllCreatureTypesToOwnCreaturesEffect grant
                        && grant.scope() == GrantScope.SELF);
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
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
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
     * with a {@link GrantAllCreatureTypesToOwnCreaturesEffect}.
     */
    public List<CardSubtype> computeGrantedSubtypesForOwnedCreatureCard(GameData gameData, UUID ownerId) {
        List<CardSubtype> result = new ArrayList<>();
        List<Permanent> bf = gameData.playerBattlefields.get(ownerId);
        if (bf == null) return result;
        for (Permanent perm : bf) {
            CardSubtype chosen = perm.getChosenSubtype();
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GrantChosenSubtypeToOwnCreaturesEffect g && g.affectsAllZones()) {
                    if (chosen != null && !result.contains(chosen)) {
                        result.add(chosen);
                    }
                } else if (effect instanceof GrantAllCreatureTypesToOwnCreaturesEffect grant
                        && grant.scope() != GrantScope.SELF) {
                    for (CardSubtype subtype : CardSubtype.values()) {
                        if (isCreatureSubtype(subtype) && !result.contains(subtype)) {
                            result.add(subtype);
                        }
                    }
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
        List<Permanent> bf = gameData.playerBattlefields.get(ownerId);
        if (bf == null) return result;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GraveyardAbilityGrantingEffect g) {
                    if (!g.appliesTo(card)) continue;
                    ActivatedAbility granted = g.grantedGraveyardAbilityFor(card);
                    if (granted != null) {
                        result.add(granted);
                    }
                }
            }
        }
        if (card != null && gameData.cardsGrantedEmbalmUntilEndOfTurn.contains(card.getId())
                && card.getManaCost() != null && !card.getManaCost().isBlank()) {
            result.add(Card.embalmAbility(card.getManaCost()));
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
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof MadnessGrantingEffect g
                        && predicateEvaluationService.matchesCardPredicate(
                                card, g.madnessGrantFilter(), null, gameData, ownerId)) {
                    return Optional.ofNullable(card.getManaCost());
                }
            }
        }
        return Optional.empty();
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
     * Returns the player ID of whoever currently has priority, following APNAP
     * (Active Player, Non-Active Player) order. Returns {@code null} if both players
     * have already passed priority.
     */
    public UUID getPriorityPlayerId(GameData data) {
        if (data.activePlayerId == null) {
            return null;
        }
        if (!data.priorityPassedBy.contains(data.activePlayerId)) {
            return data.activePlayerId;
        }
        List<UUID> ids = new ArrayList<>(data.orderedPlayerIds);
        UUID nonActive = ids.get(0).equals(data.activePlayerId) ? ids.get(1) : ids.get(0);
        if (!data.priorityPassedBy.contains(nonActive)) {
            return nonActive;
        }
        return null;
    }

    /**
     * Returns {@code true} if the player's life total is allowed to change (i.e. no
     * {@link LifeTotalCantChangeEffect} is present on their battlefield).
     */
    public boolean canPlayerLifeChange(GameData gameData, UUID playerId) {
        return !playerBattlefieldHasStaticEffect(gameData, playerId, LifeTotalCantChangeEffect.class);
    }

    /**
     * Returns {@code true} if the player is able to gain life (i.e. no
     * {@link PlayersCantGainLifeEffect} is present on any battlefield, no opponent controls an
     * {@link OpponentsCantGainLifeEffect}, and no {@link LifeTotalCantChangeEffect} prevents life
     * changes).
     */
    public boolean canPlayerGainLife(GameData gameData, UUID playerId) {
        if (!canPlayerLifeChange(gameData, playerId)) return false;
        if (gameData.playersWhoCantGainLifeRestOfGame.contains(playerId)) return false;
        if (gameData.playersWhoCantGainLifeThisTurn.contains(playerId)) return false;
        if (gameData.playersCantGainLifeThisTurn) return false;
        if (anyBattlefieldHasStaticEffect(gameData, PlayersCantGainLifeEffect.class)) return false;
        for (UUID otherId : gameData.playerBattlefields.keySet()) {
            if (!otherId.equals(playerId)
                    && playerBattlefieldHasStaticEffect(gameData, otherId, OpponentsCantGainLifeEffect.class)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if players may pay life or sacrifice creatures as a cost of casting a
     * spell or activating an ability (i.e. no {@link PlayersCantPayLifeOrSacrificeCreaturesEffect}
     * is present on any battlefield). Costs demanded by a resolving effect are never restricted.
     */
    public boolean canPayLifeOrSacrificeCreaturesForCosts(GameData gameData) {
        return !anyBattlefieldHasStaticEffect(gameData, PlayersCantPayLifeOrSacrificeCreaturesEffect.class);
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
     * Returns the multiplier applied to life the given player gains, per any static life-gain
     * replacement effects they control. Multiple replacements stack multiplicatively.
     */
    public int lifeGainMultiplier(GameData gameData, UUID playerId) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return 1;
        int multiplier = 1;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof LifeGainReplacementEffect replacement) {
                    multiplier *= replacement.lifeGainMultiplier();
                }
            }
        }
        return multiplier;
    }

    /**
     * Returns the additional life applied to each positive life-gain event by static replacement
     * effects controlled by the player.
     */
    public int additionalLifeGain(GameData gameData, UUID playerId) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return 0;
        int additional = 0;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof LifeGainReplacementEffect replacement) {
                    additional += replacement.additionalLifeGain();
                }
            }
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

        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return 1;

        int multiplier = 1;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof OpponentLifeLossReplacementEffect replacement) {
                    multiplier *= replacement.lifeLossMultiplier();
                }
            }
        }
        return multiplier;
    }

    /**
     * Returns the multiplier applied to mana the given player produces by tapping a permanent for
     * mana, per any {@link ManaReflectionEffect} static effects they control (Mana Reflection). Each
     * such effect doubles the mana produced, and multiple stack multiplicatively (2^count). Returns
     * 1 when the player controls none.
     */
    public int manaProductionMultiplier(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 1;
        int multiplier = 1;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ManaReflectionEffect reflection) {
                    multiplier *= reflection.multiplier();
                }
            }
        }
        return multiplier;
    }

    /**
     * Basic-land-type to color remappings contributed by every {@link TwistBasicLandManaColorsEffect}
     * on the battlefield (Reality Twist, Naked Singularity). Global — not controller-scoped. When
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
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
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
     * Fixed color {@code permanent} produces under a land-mana replacement such as
     * {@link LandManaProducesFixedColorEffect}. Null when inactive. Amount is unchanged; only the
     * type is replaced.
     *
     * <p>A player-scoped replacement recorded in {@code GameData.landManaFixedColorThisTurn}
     * applies to lands that player currently controls and takes precedence. A turn-scoped,
     * nonbasic-land replacement recorded in
     * {@code GameData.nonbasicLandsFixedManaColorThisTurn} takes precedence over the
     * subtype-scoped replacement. The subtype-scoped replacement recorded in
     * {@code GameData.landSubtypeFixedManaColorThisTurn} (Chaos Moon's even branch) takes
     * precedence, matched against the permanent's effective basic land types, followed by the
     * turn-scoped all-lands replacement in {@code GameData.allLandsFixedManaColorThisTurn} (Hall of
     * Gemstone).
     */
    public ManaColor fixedLandManaColor(GameData gameData, Permanent permanent) {
        if (permanent != null && !gameData.landManaFixedColorThisTurn.isEmpty()) {
            UUID controllerId = findPermanentController(gameData, permanent.getId());
            ManaColor controllerFixedColor = gameData.landManaFixedColorThisTurn.get(controllerId);
            if (controllerFixedColor != null) {
                return controllerFixedColor;
            }
        }
        if (permanent != null
                && gameData.nonbasicLandsFixedManaColorThisTurn != null
                && !hasEffectiveSupertype(gameData, permanent, CardSupertype.BASIC)) {
            return gameData.nonbasicLandsFixedManaColorThisTurn;
        }
        if (permanent != null && !gameData.landSubtypeFixedManaColorThisTurn.isEmpty()) {
            Set<CardSubtype> types = effectiveBasicLandTypes(gameData, permanent);
            for (var entry : gameData.landSubtypeFixedManaColorThisTurn.entrySet()) {
                if (types.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        if (gameData.allLandsFixedManaColorThisTurn != null) {
            return gameData.allLandsFixedManaColorThisTurn;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent source : battlefield) {
                for (var effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof LandManaProducesFixedColorEffect fixed) {
                        return fixed.color();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns whether a basic land currently produces mana of a color chosen by its controller due
     * to a static effect controlled by that same player.
     */
    public boolean basicLandManaProducesAnyColor(GameData gameData, Permanent permanent) {
        if (gameData == null || permanent == null
                || !isLand(gameData, permanent)
                || !hasEffectiveSupertype(gameData, permanent, CardSupertype.BASIC)) {
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
        return battlefield.stream()
                .flatMap(source -> source.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(BasicLandManaProducesAnyColorEffect.class::isInstance);
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
            colors.addAll(mappings.getOrDefault(type, Set.of()));
        }
        return colors;
    }

    /**
     * Returns {@code true} if players are allowed to cast spells from the given zone.
     * Returns {@code false} when a {@link PlayersCantCastSpellsFromZonesEffect} whose
     * {@code zones} contains {@code zone} is on any battlefield (e.g. Ashes of the Abhorrent
     * for graveyards, Grafdigger's Cage for graveyards and libraries).
     */
    public boolean canPlayersCastSpellsFromZone(GameData gameData, Zone zone) {
        return !gameData.anyPermanentMatches(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(PlayersCantCastSpellsFromZonesEffect.class::isInstance)
                        .map(PlayersCantCastSpellsFromZonesEffect.class::cast)
                        .anyMatch(e -> e.zones().contains(zone)));
    }

    /** Returns whether the given player may cast spells from the given zone. */
    public boolean canPlayerCastSpellsFromZone(GameData gameData, UUID playerId, Zone zone) {
        return canPlayersCastSpellsFromZone(gameData, zone)
                && (zone != Zone.GRAVEYARD
                || !gameData.playersCantPlayFromGraveyardsThisTurn.contains(playerId));
    }

    /**
     * Returns {@code true} if the given spell can be cast from {@code zone}. This includes global
     * zone locks and static effects that restrict only noncreature spells.
     */
    public boolean canCastSpellFromZone(GameData gameData, Card card, Zone zone) {
        if (!canPlayersCastSpellsFromZone(gameData, zone)) {
            return false;
        }
        if (card.hasType(CardType.CREATURE)) {
            return true;
        }
        return !gameData.anyPermanentMatches(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(NoncreatureSpellsCantBeCastFromZonesEffect.class::isInstance)
                        .map(NoncreatureSpellsCantBeCastFromZonesEffect.class::cast)
                        .anyMatch(e -> e.zones().contains(zone)));
    }

    /** Returns whether the given player may cast the given spell from the given zone. */
    public boolean canCastSpellFromZone(GameData gameData, Card card, Zone zone, UUID playerId) {
        if (!canPlayerCastSpellsFromZone(gameData, playerId, zone)) {
            return false;
        }
        if (card.hasType(CardType.CREATURE)) {
            return true;
        }
        return !gameData.anyPermanentMatches(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(NoncreatureSpellsCantBeCastFromZonesEffect.class::isInstance)
                        .map(NoncreatureSpellsCantBeCastFromZonesEffect.class::cast)
                        .anyMatch(e -> e.zones().contains(zone)));
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
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(CardsCantEnterBattlefieldFromZonesEffect.class::isInstance)
                        .map(CardsCantEnterBattlefieldFromZonesEffect.class::cast)
                        .anyMatch(e -> e.zones().contains(zone) && predicateEvaluationService.matchesCardPredicate(card, e.filter(), null)));
    }

    /**
     * Returns {@code true} if players are allowed to activate abilities of cards in graveyards.
     * Returns {@code false} when a {@link PlayersCantActivateAbilitiesOfGraveyardCardsEffect}
     * is on any battlefield (e.g. Ashes of the Abhorrent).
     */
    public boolean canPlayersActivateGraveyardAbilities(GameData gameData) {
        return !anyBattlefieldHasStaticEffect(gameData, PlayersCantActivateAbilitiesOfGraveyardCardsEffect.class);
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
                if (permanent.isLosesAllAbilitiesUntilEndOfTurn()
                        || computeStaticBonus(gameData, permanent).losesAllAbilities()) {
                    continue;
                }
                if (permanent.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(effectType::isInstance)) {
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
     * lifts the summoning sickness restriction on ability activation — it does not grant haste.
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
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ActivatedAbilityTimingEffect timing
                        && timing.allowsInstantSpeedActivation(ability)) {
                    return true;
                }
            }
        }
        return false;
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

    /** Returns true when the player has False Dawn's temporary white-mana permission. */
    public boolean canSpendWhiteManaAsAnyColorUntilEndOfTurn(GameData gameData, UUID playerId) {
        return gameData.playersWithWhiteManaAsAnyColorThisTurn.contains(playerId);
    }

    /** Returns true when a global static effect lets the player spend mana as any color. */
    public boolean canSpendManaAsAnyColor(GameData gameData, UUID playerId) {
        return anyBattlefieldHasStaticEffect(gameData, SpendManaAsAnyColorEffect.class);
    }

    /**
     * Returns whether the source creature's activated abilities may use blue mana as mana of any
     * color, as granted by Quicksilver Elemental.
     */
    public boolean canSpendBlueManaAsAnyColorForActivatedAbilities(GameData gameData, Permanent source) {
        return source != null
                && !hasLostAllAbilities(gameData, source)
                && source.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(SpendBlueManaAsAnyColorForActivatedAbilitiesEffect.class::isInstance);
    }

    /**
     * Returns {@code true} if damage can be prevented. Returns {@code false}
     * when a {@link DamageCantBePreventedEffect} is on any battlefield
     * (e.g. Leyline of Punishment).
     */
    public boolean isDamagePreventable(GameData gameData) {
        return !gameData.damageCantBePreventedThisTurn
                && !gameData.unpreventableDamageInProgress
                && !anyBattlefieldHasStaticEffect(gameData, DamageCantBePreventedEffect.class);
    }

    /**
     * Returns {@code true} if damage dealt by the given source permanent can't be prevented because
     * that permanent itself carries {@link SourceDamageCantBePreventedEffect} (e.g. Malignus). Unlike
     * {@link #isDamagePreventable(GameData)} this is scoped to one source, so callers that know the
     * damage source consult it in addition to the global check.
     */
    public boolean damageCantBePreventedFromSource(GameData gameData, Permanent source) {
        return source != null && source.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(SourceDamageCantBePreventedEffect.class::isInstance);
    }

    /** Returns whether damage from the source can't be prevented for the current damage event. */
    public boolean damageCantBePreventedFromSource(GameData gameData, Permanent source,
                                                   boolean isCombatDamage) {
        return damageCantBePreventedFromSource(gameData, source)
                || (isCombatDamage && hasControlledCreatureCombatDamageCantBePrevented(gameData, source));
    }

    private boolean hasControlledCreatureCombatDamageCantBePrevented(GameData gameData, Permanent source) {
        if (source == null) return false;
        UUID controllerId = findPermanentController(gameData, source.getId());
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream().anyMatch(permanent ->
                hasActiveStaticEffect(gameData, permanent,
                        ControlledCreaturesCombatDamageCantBePreventedEffect.class));
    }

    /** Returns whether this player's active turn-scoped source filters prevent damage from the source. */
    public boolean isDamageFromMatchingSourcePreventedForPlayer(GameData gameData, UUID playerId,
                                                                Permanent source) {
        if (!isDamagePreventable(gameData) || source == null || damageCantBePreventedFromSource(gameData, source)) {
            return false;
        }
        Set<PermanentPredicate> predicates = gameData.playersWithDamageFromMatchingSourcesPrevented.get(playerId);
        return predicates != null && predicates.stream()
                .anyMatch(predicate -> predicateEvaluationService.matchesPermanentPredicate(gameData, source, predicate));
    }

    /**
     * Returns {@code true} if the player is able to lose the game (i.e. no
     * {@link CantLoseGameEffect} is controlled by them and no opponent controls a
     * {@link CantWinGameEffect}).
     */
    public boolean canPlayerLoseGame(GameData gameData, UUID playerId) {
        if (playerBattlefieldHasStaticEffect(gameData, playerId, CantLoseGameEffect.class)
                || playerHasTemporaryStaticEffect(gameData, playerId, CantLoseGameEffect.class)) {
            return false;
        }
        if (playerEmblemHasActiveStaticEffect(gameData, playerId, CantLoseGameEffect.class)) {
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
     * Returns {@code true} if the player controls an active static effect that prevents them from
     * winning the game.
     */
    public boolean playerHasCantWinGameEffect(GameData gameData, UUID playerId) {
        if (playerBattlefieldHasStaticEffect(gameData, playerId, CantWinGameEffect.class)
                || playerHasTemporaryStaticEffect(gameData, playerId, CantWinGameEffect.class)) {
            return true;
        }
        return playerEmblemHasActiveStaticEffect(gameData, playerId, CantWinGameEffect.class);
    }

    private boolean playerEmblemHasActiveStaticEffect(GameData gameData, UUID playerId,
                                                       Class<? extends CardEffect> effectType) {
        for (Emblem emblem : gameData.emblems) {
            if (!playerId.equals(emblem.controllerId())) continue;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effectType.isInstance(effect)) {
                    return true;
                }
                if (effect instanceof ConditionalEffect conditional
                        && effectType.isInstance(conditional.wrapped())
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

    /**
     * Returns the highest life-total floor that damage dealt to this player can't reduce them past,
     * or {@code 0} when no battlefield or turn-scoped life-floor effect currently applies.
     * or {@code 0} when no {@link DamageLifeFloorEffect} on their battlefield or in their temporary
     * player effects currently applies.
     * Callers must treat {@code 0} as "no floor" (do not clamp life to 0). Each such effect only
     * contributes its floor while its {@link LifeFloorCondition} holds, evaluated against the
     * player's state before the damage is applied ({@code currentLife}).
     */
    public int damageLifeFloor(GameData gameData, UUID playerId, int currentLife) {
        int floor = gameData.damageLifeFloorsUntilEndOfTurn.getOrDefault(playerId, 0);
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        boolean controlsCreature = bf != null && bf.stream().anyMatch(p -> isCreature(gameData, p));
        if (bf != null) {
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof DamageLifeFloorEffect lifeFloor) {
                        floor = Math.max(floor, activeDamageLifeFloor(lifeFloor, controlsCreature, currentLife));
                    }
                }
            }
        }
        for (CardEffect effect : gameData.playerStaticEffectsUntilEndOfTurn
                .getOrDefault(playerId, List.of())) {
            if (effect instanceof DamageLifeFloorEffect lifeFloor) {
                floor = Math.max(floor, activeDamageLifeFloor(lifeFloor, controlsCreature, currentLife));
            }
        }
        return floor;
    }

    private int activeDamageLifeFloor(DamageLifeFloorEffect lifeFloor, boolean controlsCreature,
                                      int currentLife) {
        boolean active = switch (lifeFloor.condition()) {
            case ALWAYS -> true;
            case CONTROLS_A_CREATURE -> controlsCreature;
            case LIFE_AT_LEAST_FLOOR -> currentLife >= lifeFloor.floor();
        };
        return active ? lifeFloor.floor() : 0;
    }

    /** Returns the player's life total after applying damage replacement floors. */
    public int lifeAfterDamage(GameData gameData, UUID playerId, int damage) {
        int currentLife = gameData.getLife(playerId);
        int newLife = currentLife - damage;
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
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        return isCreatureWithBonus(gameData, permanent, bonus);
    }

    private boolean isCreatureWithBonus(GameData gameData, Permanent permanent, StaticBonus bonus) {
        if (hasEffectiveCardType(permanent, bonus, CardType.CREATURE)) return true;
        if (permanent.isAnimatedUntilEndOfTurn()) return true;
        if (permanent.isAnimatedUntilEndOfCombat()) return true;
        if (permanent.isAnimatedUntilNextTurn()) return true;
        if (permanent.isPermanentlyAnimated()) return true;
        if (permanent.getCounterCount(CounterType.AWAKENING) > 0) return true;
        if (hasEffectiveCardType(permanent, bonus, CardType.ARTIFACT)
                && hasAnimateArtifactEffect(gameData)) return true;
        if (isAnimatedByStarfield(gameData, permanent)) return true;
        if (hasEffectiveCardType(permanent, bonus, CardType.LAND) && matchesAnimateLand(gameData, permanent)) return true;
        if (hasAuraBecomeCreatureEffect(gameData, permanent)) return true;
        return hasSelfBecomeCreatureEffect(gameData, permanent);
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

    public boolean isBattle(GameData gameData, Permanent permanent) {
        return hasEffectiveCardType(permanent, computeStaticBonus(gameData, permanent), CardType.BATTLE);
    }

    public boolean isKindred(GameData gameData, Permanent permanent) {
        return hasEffectiveCardType(permanent, computeStaticBonus(gameData, permanent), CardType.KINDRED);
    }

    private boolean hasEffectiveCardType(Permanent permanent, StaticBonus bonus, CardType type) {
        if (bonus.cardTypeOverriding()) {
            return bonus.grantedCardTypes().contains(type);
        }
        // A type-changing effect that added a type "in addition to its other types" stores it on
        // the permanent (transient until-end-of-turn or persistent), not in the static bonus — e.g.
        // Phyrexian Scriptures' chapter I making a creature an artifact. Mirror the non-layered
        // isArtifact/isEnchantment so the layered checks see those grants too.
        return hasCardType(permanent, type)
                || permanent.getGrantedCardTypes().contains(type)
                || permanent.getPersistentGrantedCardTypes().contains(type)
                || bonus.grantedCardTypes().contains(type);
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
        cardTypes.addAll(permanent.getPersistentGrantedCardTypes());
        cardTypes.addAll(permanent.getGrantedCardTypes());
        return cardTypes;
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
                || permanent.isAnimatedUntilEndOfTurn()
                || permanent.isAnimatedUntilEndOfCombat()
                || permanent.isAnimatedUntilNextTurn()
                || permanent.isPermanentlyAnimated()
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
     * makes the enchanted permanent a creature.
     */
    private boolean hasAuraBecomeCreatureEffect(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent source : battlefield) {
                if (source.isAttached() && permanent.getId().equals(source.getAttachedTo())) {
                    for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
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
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ConditionalEffect conditional
                    && conditional.wrapped() instanceof AnimatePermanentsEffect animate
                    && animate.scope() == GrantScope.SELF) {
                UUID controllerId = findPermanentController(gameData, permanent.getId());
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

    /** Returns whether {@code playerId} controls strictly more lands than {@code comparedPlayerId}. */
    public boolean controlsMoreLandsThan(GameData gameData, UUID playerId, UUID comparedPlayerId) {
        return countLandsControlled(gameData, playerId) > countLandsControlled(gameData, comparedPlayerId);
    }

    /** Returns whether {@code playerId} controls strictly more creatures than {@code comparedPlayerId}. */
    public boolean controlsMoreCreaturesThan(GameData gameData, UUID playerId, UUID comparedPlayerId) {
        return countCreaturesControlled(gameData, playerId) > countCreaturesControlled(gameData, comparedPlayerId);
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
            if (permanent.getCard().hasType(CardType.LAND)) {
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
                count++;
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
            if (predicateEvaluationService.matchesCardPredicate(spell, filter, spell.getId())) {
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
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return false;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof SpellCastingAbilityGrantingEffect grant
                        && grant.grantedAbility() == ability
                        && grant.appliesToSourceZone(sourceZone)
                        && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null)) {
                    return true;
                }
            }
        }
        return false;
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
     * Returns {@code true} if the permanent is an artifact, either by its natural card type,
     * a transient granted card type (until end of turn), or a persistent granted card type (permanent).
     */
    public boolean isArtifact(Permanent permanent) {
        return hasCardType(permanent, CardType.ARTIFACT)
                || permanent.getGrantedCardTypes().contains(CardType.ARTIFACT)
                || permanent.getPersistentGrantedCardTypes().contains(CardType.ARTIFACT);
    }

    /**
     * Returns {@code true} if the permanent is an artifact, checking natural card type,
     * temporary granted card types, and static card type grants (e.g. from equipment).
     */
    public boolean isArtifact(GameData gameData, Permanent permanent) {
        return hasEffectiveCardType(permanent, computeStaticBonus(gameData, permanent), CardType.ARTIFACT);
    }

    /**
     * Returns {@code true} if the permanent is an enchantment, either by its natural card type,
     * a transient granted card type (until end of turn), or a persistent granted card type (permanent).
     */
    public boolean isEnchantment(Permanent permanent) {
        return hasCardType(permanent, CardType.ENCHANTMENT)
                || permanent.getGrantedCardTypes().contains(CardType.ENCHANTMENT)
                || permanent.getPersistentGrantedCardTypes().contains(CardType.ENCHANTMENT);
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
     * {@link StaticBonus#NONE} means no continuous effect touched the permanent — the intrinsic
     * answer stands.
     */
    public boolean hasKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        return hasKeyword(permanent, computeStaticBonus(gameData, permanent), keyword);
    }

    /**
     * Keyword check against a pre-computed static bonus, for callers that read many keywords
     * off the same permanent (mirrors {@link #getEffectivePower(Permanent, StaticBonus)}).
     */
    public boolean hasKeyword(Permanent permanent, StaticBonus bonus, Keyword keyword) {
        if (bonus == StaticBonus.NONE) {
            return permanent.hasKeyword(keyword);
        }
        if (bonus.removedKeywords().contains(keyword)) return false;
        return bonus.keywords().contains(keyword);
    }

    /**
     * Returns the permanent's colors after the CR 613 layer-5 computation: the natural color
     * plus additive grants, or the replacement set when a color-setting effect ("becomes red")
     * applied. Prefer this over {@link Permanent#getEffectiveColor()} in engine code — the
     * legacy accessor does not see layered color changes.
     */
    public Set<CardColor> getEffectiveColors(GameData gameData, Permanent permanent) {
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (bonus.colorOverriding()) {
            return bonus.grantedColors();
        }
        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        colors.addAll(permanent.getEffectiveColors());
        colors.addAll(permanent.getGrantedColors());
        colors.addAll(bonus.grantedColors());
        return colors;
    }

    /** Returns the controller's current devotion to one color. */
    public int getDevotionToColor(GameData gameData, UUID controllerId, ManaColor color) {
        if (gameData == null || controllerId == null || color == null) {
            return 0;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int devotion = 0;
        for (Permanent permanent : battlefield) {
            var manaCost = permanent.getCard().getParsedManaCost();
            if (manaCost != null) {
                devotion += manaCost.countColorSymbols(color);
            }
        }
        return devotion + layerSystemService.devotionModifierFor(gameData, controllerId);
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
     * Returns the effective colors of a card that is not being represented by a battlefield
     * permanent, including a spell on the stack. Global color-setting effects are applied here
     * because the card's intrinsic color fields remain unchanged.
     */
    public Set<CardColor> getEffectiveCardColors(GameData gameData, Card card) {
        if (card == null) {
            return Set.of();
        }
        if (gameData != null) {
            Set<CardColor> temporary = gameData.spellColorOverridesUntilEndOfTurn.get(card.getId());
            if (temporary != null) {
                return Set.copyOf(temporary);
            }
            Set<CardColor> indefinite = gameData.spellColorOverrides.get(card.getId());
            if (indefinite != null) {
                return Set.copyOf(indefinite);
            }
            if (anyBattlefieldHasStaticEffect(gameData, AllCardsAreColorlessEffect.class)) {
                return Set.of();
            }
        }
        List<CardColor> intrinsic = card.getColors();
        if (intrinsic != null && !intrinsic.isEmpty()) {
            return Set.copyOf(intrinsic);
        }
        CardColor single = card.getColor();
        return single != null ? Set.of(single) : Set.of();
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
        Set<CardColor> colors = EnumSet.noneOf(CardColor.class);
        colors.addAll(permanent.getEffectiveColors());
        colors.addAll(permanent.getGrantedColors());
        return colors;
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

    /** Returns whether a static effect prevents the permanent from having or gaining a keyword. */
    public boolean cantHaveOrGainKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(effect -> effect instanceof CantHaveOrGainKeywordEffect restriction
                        && restriction.keyword() == keyword);
    }

    /** Returns whether a static effect prevents the permanent from becoming suspected. */
    public boolean cantBecomeSuspected(GameData gameData, Permanent permanent) {
        if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantBecomeSuspectedEffect.class::isInstance)) {
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
        if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantBecomeUntappedEffect.class::isInstance)) {
            return true;
        }
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(CantBecomeUntappedEffect.class::isInstance);
    }

    /** Returns whether the permanent cannot be sacrificed during its controller's end step. */
    public boolean cantBeSacrificed(GameData gameData, Permanent permanent) {
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        if (gameData.currentStep != TurnStep.END_STEP
                || controllerId == null
                || !controllerId.equals(gameData.activePlayerId)) {
            return false;
        }
        if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantBeSacrificedEffect.class::isInstance)) {
            return true;
        }
        return hasGrantedEffect(gameData, permanent, CantBeSacrificedEffect.class);
    }

    boolean cantHaveCountersForController(GameData gameData, Permanent permanent, UUID controllerId) {
        if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantHaveCountersEffect.class::isInstance)) {
            return true;
        }
        StaticBonus bonus = computeStaticBonusForController(gameData, permanent, controllerId);
        if (bonus.grantedEffects().stream().anyMatch(CantHaveCountersEffect.class::isInstance)) {
            return true;
        }
        if (lockedWhileSourceOnBattlefield(
                gameData, gameData.countersLockedPermanentsWhileSourceOnBattlefield, permanent.getId())) {
            return true;
        }
        // Solemnity: "Counters can't be put on artifacts, creatures, enchantments, or lands."
        // Planeswalkers (that aren't also one of those types) are unaffected and still get loyalty.
        return anyBattlefieldHasStaticEffect(gameData, CountersCantBePlacedEffect.class)
                && isArtifactCreatureEnchantmentOrLand(gameData, permanent, bonus);
    }

    /**
     * Returns {@code true} if {@code subjectId} (a permanent or a player) is locked out of counters
     * by a Suncleanser-style effect whose source permanent is still on the battlefield. Stale
     * entries — those whose source has left — are ignored rather than cleaned up, matching the
     * lifetime rule documented on the {@code …WhileSourceOnBattlefield} maps.
     */
    private boolean lockedWhileSourceOnBattlefield(
            GameData gameData, Map<UUID, Set<UUID>> locks, UUID subjectId) {
        for (Map.Entry<UUID, Set<UUID>> lock : locks.entrySet()) {
            if (lock.getValue().contains(subjectId) && findPermanentById(gameData, lock.getKey()) != null) {
                return true;
            }
        }
        return false;
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
     * This does NOT block spells — only activated and triggered abilities.
     */
    public boolean cantBeTargetOfOpponentAbilities(GameData gameData, Permanent permanent) {
        if (!permanent.isLosesAllAbilitiesUntilEndOfTurn()
                && !computeStaticBonus(gameData, permanent).losesAllAbilities()) {
            if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(GameQueryService::isOpponentAbilityRestriction)) {
                return true;
            }
        }
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(GameQueryService::isOpponentAbilityRestriction);
    }

    private static boolean isOpponentAbilityRestriction(CardEffect effect) {
        return effect instanceof TargetingRestrictionEffect r
                && r.kind() == TargetingSourceKind.ABILITIES
                && r.mode() == TargetColorMode.ANY;
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
                        && r.mode() == TargetColorMode.ANY);
    }

    /**
     * Returns whether an opponent-controlled spell or ability is blocked by a granted targeting
     * restriction on this permanent. Hexproof-ignoring effects bypass only restrictions marked as
     * hexproof-like; cards that use the same wording without granting hexproof remain protected.
     */
    public boolean cantBeTargetedByOpponentSpellsOrAbilities(GameData gameData, Permanent permanent,
                                                               UUID targetingControllerId) {
        UUID targetControllerId = findPermanentController(gameData, permanent.getId());
        if (targetControllerId == null || targetControllerId.equals(targetingControllerId)) {
            return false;
        }
        boolean hexproofLifted = ignoresOpponentPermanentHexproof(gameData, targetingControllerId)
                || (isCreature(gameData, permanent)
                && ignoresOpponentCreatureHexproof(gameData, targetingControllerId));
        return computeStaticBonus(gameData, permanent).grantedEffects().stream()
                .anyMatch(e -> e instanceof TargetingRestrictionEffect r
                        && r.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES
                        && r.opponentOnly()
                        && r.mode() == TargetColorMode.ANY
                        && (!hexproofLifted || !r.hexproofLike()));
    }

    /**
     * Returns {@code true} if {@code controllerId} controls a permanent whose static effects let
     * them target opponents' hexproof creatures as though they didn't have hexproof (Glaring
     * Spotlight). Only hexproof is lifted — shroud and protection still apply.
     */
    public boolean ignoresOpponentCreatureHexproof(GameData gameData, UUID controllerId) {
        if (controllerId == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(IgnoreOpponentCreatureHexproofEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return controllerHasFloatingHexproofIgnore(gameData, controllerId);
    }

    /**
     * Returns whether {@code controllerId} controls a permanent whose static effects let them
     * target any opponent-controlled permanent with hexproof as though it didn't have hexproof
     * (Kaya, Bane of the Dead). Only hexproof is lifted — shroud and protection still apply.
     */
    public boolean ignoresOpponentPermanentHexproof(GameData gameData, UUID controllerId) {
        if (controllerId == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (hasLostAllAbilities(gameData, permanent)
                        || permanent.isStaticEffectSuppressed(IgnoreOpponentHexproofEffect.class)) {
                    continue;
                }
                if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(IgnoreOpponentHexproofEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if {@code controllerId} has an effect allowing them to target opponents
     * with hexproof as though they didn't have hexproof (Detection Tower's activated ability).
     * Only hexproof is lifted — shroud and protection still apply.
     */
    public boolean ignoresOpponentPlayerHexproof(GameData gameData, UUID controllerId) {
        if (controllerId == null) {
            return false;
        }
        return ignoresOpponentPermanentHexproof(gameData, controllerId)
                || controllerHasFloatingHexproofIgnore(gameData, controllerId);
    }

    private boolean controllerHasFloatingHexproofIgnore(GameData gameData, UUID controllerId) {
        synchronized (gameData.floatingEffects) {
            for (var floatingEffect : gameData.floatingEffects) {
                if (controllerId.equals(floatingEffect.controllerId())
                        && IgnoreOpponentHexproofUntilEndOfTurnEffect.class.isInstance(floatingEffect.effect())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the permanent cannot have -1/-1 counters placed on it,
     * from effects granted by other permanents (e.g. Melira, Sylvok Outcast).
     */
    public boolean cantHaveMinusOneMinusOneCounters(GameData gameData, Permanent permanent) {
        return hasGrantedEffect(gameData, permanent, CantHaveMinusOneMinusOneCountersEffect.class);
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
        if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantHavePlusOnePlusOneCountersEffect.class::isInstance)) {
            return true;
        }
        StaticBonus bonus = computeStaticBonusForController(gameData, permanent, controllerId);
        return bonus.grantedEffects().stream()
                .anyMatch(CantHavePlusOnePlusOneCountersEffect.class::isInstance);
    }

    /**
     * Applies Vizier of Remedies-style replacement effects (CR 616) that reduce the number of -1/-1
     * counters put on a creature its controller controls. A {@code count} of zero or fewer is
     * returned unchanged — no counters "would be put", so the replacement never applies.
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
     * zero or fewer is returned unchanged — no counters "would be put", so no replacement applies.
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
        if (count <= 0 || controllerId == null) {
            return count;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int result = count;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : staticEffectsIncludingTemporary(permanent)) {
                    if (effect instanceof CounterReplacementEffect replacement
                            && replacement.appliesTo(counterType, affectedPermanentIsCreature,
                            affectedPermanentIsArtifact, permanent, affectedPermanent)) {
                        result = replacement.replace(counterType, result);
                    }
                }
            }
        }
        if (affectedPermanentIsEntering) {
            result = applyEnteringPermanentReplacements(counterType, result, affectedPermanent);
        }
        return result;
    }

    private int applyEnteringPermanentReplacements(CounterType counterType, int count,
                                                   Permanent enteringPermanent) {
        if (count <= 0 || enteringPermanent == null) {
            return count;
        }
        boolean creature = enteringPermanent.getCard().hasType(CardType.CREATURE);
        boolean artifact = enteringPermanent.getCard().hasType(CardType.ARTIFACT);
        int result = count;
        for (CardEffect effect : enteringPermanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CounterReplacementEffect replacement
                    && replacement.appliesToWhenEntering(counterType, creature, artifact,
                    enteringPermanent)) {
                result = replacement.replace(counterType, result);
            }
        }
        return result;
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
        if (!affectedPermanentIsNonCreatureVehicle) {
            return replaceCounters(gameData, controllerId, CounterType.PLUS_ONE_PLUS_ONE, count,
                    true, false, affectedPermanent, affectedPermanentIsEntering);
        }
        if (count <= 0 || controllerId == null) {
            return count;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int result = count;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : staticEffectsIncludingTemporary(permanent)) {
                    if (effect instanceof com.github.laxika.magicalvibes.model.effect.PlusOnePlusOneCountersReplacementEffect replacement
                            && replacement.appliesToNonCreatureVehicles()) {
                        result = replacement.replace(result);
                    } else if (effect instanceof CounterReplacementEffect replacement
                            && replacement.appliesTo(CounterType.PLUS_ONE_PLUS_ONE, false, true,
                            permanent, affectedPermanent)) {
                        result = replacement.replace(CounterType.PLUS_ONE_PLUS_ONE, result);
                    }
                }
            }
        }
        if (affectedPermanentIsEntering) {
            result = applyEnteringPermanentReplacements(CounterType.PLUS_ONE_PLUS_ONE, result,
                    affectedPermanent);
        }
        return result;
    }

    private List<CardEffect> staticEffectsIncludingTemporary(Permanent permanent) {
        List<CardEffect> effects = new ArrayList<>(permanent.getCard().getEffects(EffectSlot.STATIC));
        effects.addAll(permanent.getTemporaryTriggeredEffects(EffectSlot.STATIC));
        return effects;
    }

    /** Applies all counter replacements for a permanent already on the battlefield. */
    public int replaceCounters(GameData gameData, Permanent permanent, CounterType counterType, int count) {
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        boolean creature = permanent.getCard().hasType(CardType.CREATURE) || isCreature(gameData, permanent);
        if (counterType == CounterType.PLUS_ONE_PLUS_ONE && !creature
                && effectiveCreatureSubtypes(gameData, permanent).contains(CardSubtype.VEHICLE)) {
            return replacePlusOnePlusOneCounters(gameData, controllerId, count, true, permanent);
        }
        boolean artifact = isArtifact(gameData, permanent);
        return replaceCounters(gameData, controllerId, counterType, count, creature, artifact,
                permanent, false);
    }

    public int replaceCounters(GameData gameData, Permanent permanent, CounterType counterType,
                               int count, UUID placingPlayerId) {
        UUID affectedControllerId = findPermanentController(gameData, permanent.getId());
        boolean creature = permanent.getCard().hasType(CardType.CREATURE) || isCreature(gameData, permanent);
        boolean nonCreatureVehicle = counterType == CounterType.PLUS_ONE_PLUS_ONE && !creature
                && effectiveCreatureSubtypes(gameData, permanent).contains(CardSubtype.VEHICLE);
        boolean artifact = isArtifact(gameData, permanent);
        final int[] result = {count};
        gameData.forEachBattlefield((sourceControllerId, battlefield) -> {
            boolean sourceControlsAffected = Objects.equals(sourceControllerId, affectedControllerId);
            boolean sourceControllerIsPlacing = Objects.equals(sourceControllerId, placingPlayerId);
            for (Permanent source : battlefield) {
                for (CardEffect effect : staticEffectsIncludingTemporary(source)) {
                    if (!(effect instanceof CounterReplacementEffect replacement)) continue;
                    boolean applies;
                    if (replacement instanceof com.github.laxika.magicalvibes.model.effect.PlusOnePlusOneCountersReplacementEffect plusOneReplacement
                            && nonCreatureVehicle && plusOneReplacement.appliesToNonCreatureVehicles()) {
                        applies = sourceControlsAffected;
                    } else if (replacement instanceof com.github.laxika.magicalvibes.model.effect.DoubleCountersOnPermanentsOrPlayersEffect
                            || replacement instanceof com.github.laxika.magicalvibes.model.effect.HalveCountersPutByOpponentsEffect) {
                        applies = replacement.appliesTo(counterType, creature, sourceControlsAffected,
                                sourceControllerIsPlacing, false);
                    } else {
                        applies = sourceControlsAffected && replacement.appliesTo(
                                counterType, creature, artifact, source, permanent);
                    }
                    if (applies) result[0] = replacement.replace(counterType, result[0]);
                }
            }
        });
        return result[0];
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
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ProliferateReplacementEffect replacement) {
                    result = replacement.replace(result);
                }
            }
        }
        return result;
    }

    /** Applies all counter replacements for a permanent entering under {@code controllerId}. */
    public int replaceCounters(GameData gameData, Permanent permanent, UUID controllerId,
                               CounterType counterType, int count) {
        boolean creature = permanent != null && permanent.getCard().hasType(CardType.CREATURE);
        if (counterType == CounterType.PLUS_ONE_PLUS_ONE && permanent != null && !creature
                && permanent.getCard().getSubtypes().contains(CardSubtype.VEHICLE)) {
            return replacePlusOnePlusOneCounters(gameData, controllerId, count, true, permanent, true);
        }
        boolean artifact = permanent != null && permanent.getCard().hasType(CardType.ARTIFACT);
        return replaceCounters(gameData, controllerId, counterType, count, creature, artifact,
                permanent, true);
    }

    public int replaceCounters(GameData gameData, Permanent permanent, UUID controllerId,
                               CounterType counterType, int count, UUID placingPlayerId) {
        boolean creature = permanent != null && permanent.getCard().hasType(CardType.CREATURE);
        boolean nonCreatureVehicle = counterType == CounterType.PLUS_ONE_PLUS_ONE
                && permanent != null && !creature
                && permanent.getCard().getSubtypes().contains(CardSubtype.VEHICLE);
        boolean artifact = permanent != null && permanent.getCard().hasType(CardType.ARTIFACT);
        final int[] result = {count};
        gameData.forEachBattlefield((sourceControllerId, battlefield) -> {
            boolean sourceControlsAffected = Objects.equals(sourceControllerId, controllerId);
            boolean sourceControllerIsPlacing = Objects.equals(sourceControllerId, placingPlayerId);
            for (Permanent source : battlefield) {
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof CounterReplacementEffect replacement)) continue;
                    boolean applies;
                    if (replacement instanceof com.github.laxika.magicalvibes.model.effect.PlusOnePlusOneCountersReplacementEffect plusOneReplacement
                            && nonCreatureVehicle && plusOneReplacement.appliesToNonCreatureVehicles()) {
                        applies = sourceControlsAffected;
                    } else if (replacement instanceof com.github.laxika.magicalvibes.model.effect.DoubleCountersOnPermanentsOrPlayersEffect
                            || replacement instanceof com.github.laxika.magicalvibes.model.effect.HalveCountersPutByOpponentsEffect) {
                        applies = replacement.appliesTo(counterType, creature, sourceControlsAffected,
                                sourceControllerIsPlacing, false);
                    } else {
                        applies = sourceControlsAffected && replacement.appliesTo(counterType, creature, artifact);
                    }
                    if (applies) result[0] = replacement.replace(counterType, result[0]);
                }
            }
        });
        return result[0];
    }

    /** Applies all poison-counter replacements for a player. */
    public int replacePoisonCounters(GameData gameData, UUID playerId, int count) {
        return replacePlayerCounters(gameData, playerId, count);
    }

    public int replacePoisonCounters(GameData gameData, UUID playerId, int count,
                                     UUID placingPlayerId) {
        int result = replacePlayerCounters(gameData, playerId, count);
        if (result <= 0 || placingPlayerId == null) {
            return result;
        }
        final int[] replaced = {result};
        gameData.forEachBattlefield((sourceControllerId, battlefield) -> {
            boolean sourceControlsAffected = Objects.equals(sourceControllerId, playerId);
            boolean sourceControllerIsPlacing = Objects.equals(sourceControllerId, placingPlayerId);
            for (Permanent source : battlefield) {
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof CounterReplacementEffect replacement)) continue;
                    if (!(replacement instanceof com.github.laxika.magicalvibes.model.effect.DoubleCountersOnPermanentsOrPlayersEffect)
                            && !(replacement instanceof com.github.laxika.magicalvibes.model.effect.HalveCountersPutByOpponentsEffect)) {
                        continue;
                    }
                    if (replacement.appliesTo(null, false, sourceControlsAffected,
                            sourceControllerIsPlacing, true)) {
                        replaced[0] = replacement.replace(null, replaced[0]);
                    }
                }
            }
        });
        return replaced[0];
    }

    /** Applies all energy-counter replacements for a player. */
    public int replaceEnergyCounters(GameData gameData, UUID playerId, int count) {
        return replacePlayerCounters(gameData, playerId, count);
    }

    private int replacePlayerCounters(GameData gameData, UUID playerId, int count) {
        if (count <= 0 || playerId == null) {
            return count;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return count;
        }
        int result = count;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof PlayerCounterReplacementEffect replacement) {
                    result = replacement.replace(result);
                }
            }
        }
        return result;
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
                && effectiveCreatureSubtypes(gameData, permanent).contains(CardSubtype.VEHICLE);
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
                && effectiveCreatureSubtypes(gameData, permanent).contains(CardSubtype.VEHICLE);
        if (!affectedPermanentIsCreature && !affectedPermanentIsNonCreatureVehicle) {
            return count;
        }
        if (cantHavePlusOnePlusOneCountersForController(gameData, permanent, controllerId)) {
            return 0;
        }
        return replacePlusOnePlusOneCounters(
                gameData, controllerId, count, affectedPermanentIsNonCreatureVehicle, permanent, true);
    }

    /**
     * Returns {@code true} if the player cannot get poison counters,
     * because they control a permanent with {@link PlayerCantGetPoisonCountersEffect}.
     */
    public boolean canPlayerGetPoisonCounters(GameData gameData, UUID playerId) {
        // Solemnity: "Players can't get counters." (poison is the only player counter here.)
        if (anyBattlefieldHasStaticEffect(gameData, CountersCantBePlacedEffect.class)) {
            return false;
        }
        if (lockedWhileSourceOnBattlefield(
                gameData, gameData.countersLockedPlayersWhileSourceOnBattlefield, playerId)) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return true;
        for (Permanent p : battlefield) {
            if (p.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(PlayerCantGetPoisonCountersEffect.class::isInstance)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Applies poison-counter replacement effects for one poison event and returns the number of
     * counters that should actually be added.
     */
    public int applyPoisonCounterReplacement(GameData gameData, UUID playerId, int amount) {
        if (amount <= 0 || !canPlayerGetPoisonCounters(gameData, playerId)) {
            return 0;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        boolean hasMeliraReplacement = battlefield != null && battlefield.stream()
                .anyMatch(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(MeliraPoisonReplacementEffect.class::isInstance));
        if (!hasMeliraReplacement) {
            return amount;
        }
        return gameData.playersAffectedByMeliraPoisonReplacementThisTurn.add(playerId) ? 1 : 0;
    }

    /**
     * Returns {@code true} if the creature cannot be blocked. Checks the permanent's
     * transient flag, its own static effects, attached auras/equipment, and granted effects.
     */
    public boolean hasCantBeBlocked(GameData gameData, Permanent creature) {
        if (creature.isCantBeBlocked()) return true;
        if (creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof BlockabilityRestrictionEffect r && r.cantBeBlocked())) return true;
        if (hasAuraWithEffect(gameData, creature, CantBeBlockedEffect.class)) return true;
        if (hasGrantedEffect(gameData, creature, CantBeBlockedEffect.class)) return true;
        return controllerMakesMatchingCreaturesUnblockable(gameData, creature);
    }

    /**
     * Third-party evasion: another permanent the creature's controller controls says "creatures you
     * control matching ~ can't be blocked" (Tetsuko Umezawa, Fugitive).
     *
     * <p>CR 613.11 — this modifies the rules of blocking rather than any object's characteristics,
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
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
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
     * True if any attacking creature is attacking the given player directly or one of the planeswalkers
     * they control (i.e. the player "has been attacked" this combat). Defiant Stand, Kongming's Contraptions.
     */
    public boolean isPlayerBeingAttacked(GameData gameData, UUID playerId) {
        List<Permanent> playerBattlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (!perm.isAttacking()) continue;
                UUID target = perm.getAttackTarget();
                if (target == null) continue;
                if (target.equals(playerId)) return true;
                if (playerBattlefield.stream().anyMatch(p -> p.getId().equals(target))) return true;
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
     * Returns the power used when {@code permanent} pays a crew or saddle cost for
     * {@code sourcePermanent}. Pilot-style bonuses affect only those costs, not the permanent's
     * ordinary effective power.
     */
    public int getEffectivePowerForCrewOrSaddle(GameData gameData, Permanent sourcePermanent,
                                                Permanent permanent) {
        int power = getEffectivePower(gameData, permanent);
        if (sourcePermanent == null || !hasMountOrVehicleSubtype(gameData, sourcePermanent)) {
            return power;
        }
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (bonus.losesAllAbilities()) {
            return power;
        }
        boolean usesToughnessInsteadOfPower = false;
        int powerBonus = 0;
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CrewAndSaddlePowerModifierEffect modifier) {
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
        subtypes.addAll(permanent.getTransientSubtypes());
        subtypes.addAll(permanent.getGrantedSubtypes());
        subtypes.addAll(permanent.getUntilNextTurnSubtypes());
        subtypes.addAll(bonus.grantedSubtypes());
        return subtypes.contains(CardSubtype.VEHICLE);
    }

    /**
     * Returns the permanent's effective power using a pre-computed static bonus.
     * {@code bonus.basePTOverridden()} carries the CR 613 layer 7a/7b result (the object's CDA
     * overridden by the timestamp-resolved base-P/T setter winner from the layered pass) — it
     * takes precedence over every legacy {@code Permanent} base field; modifiers (7c) and the
     * bonus sum apply on top. {@code bonus.ptSwitched()} carries the layer-7d switch parity:
     * a switch swaps the values calculated through 7c (CR 613.4d), so a switched permanent's
     * power is the fully-resolved pre-switch toughness — 7b setters and 7c boosts resolving
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
     * See {@link #getEffectivePower(Permanent, StaticBonus)} — the layered 7a/7b base wins
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
            return bonus.basePowerOverride() + permanent.getPowerModifiers() + bonus.power();
        }
        return permanent.getEffectivePower() + bonus.power();
    }

    /** The permanent's toughness through layer 7c (base + modifiers + static bonuses), before 7d. */
    private int preSwitchToughness(Permanent permanent, StaticBonus bonus) {
        if (bonus.basePTOverridden()) {
            return bonus.baseToughnessOverride() + permanent.getToughnessModifiers() + bonus.toughness();
        }
        return permanent.getEffectiveToughness() + bonus.toughness();
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
     * <li><b>Preliminary</b> when the permanent IS the one being assembled — the dominant shape,
     *     since a static effect filtered on P/T ("creatures you control with power 1 or less",
     *     Tetsuko Umezawa, Fugitive) asks about the very permanent whose bonus is being built.
     *     See {@link #preliminaryBonus}: a second assembly answers it, with the P/T leaves inside
     *     that one pinned to the board-derived reading so the cycle closes after one level.
     *     Anthems count here too.</li>
     * <li><b>Board-derived</b> at the bottom of that recursion, and whenever the leaf is reached
     *     mid-pass. Mid-pass is the CR 613 order, not an approximation — a filter applied in
     *     layer 4-6 must not read numbers layer 7 has not produced yet.</li>
     * </ul>
     */
    public int powerForStaticFilter(Permanent permanent) {
        return ptForStaticFilter(permanent, true);
    }

    public int powerFromStaticBoard(Permanent permanent) {
        return ptFromBoard(LayerSystemService.activePass(), permanent, true);
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
     * the power and toughness of the result are trustworthy — every other field was decided with
     * those leaves degraded — which is why it is memoized apart from the real bonus.
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
     * P/T from the layered board alone — everything the pass resolved without running a
     * per-target assembly: the sublayer-7b winner (CR 613.4b) over the permanent's own base,
     * its modifiers and counters (its share of 7c), and the sublayer-7d switch (CR 613.4d).
     * Boosts contributed by OTHER permanents are absent; so is the object's own 7a CDA, which
     * the assembly computes. Falls back to the plain {@link Permanent} accessors with no pass.
     */
    private int ptFromBoard(LayerSystemService.Pass pass, Permanent permanent, boolean wantPower) {
        LayerSystemService.LayeredBoardState board = pass == null ? null : pass.board();
        if (board == null) {
            return wantPower ? permanent.getEffectivePower() : permanent.getEffectiveToughness();
        }
        LayerSystemService.BasePt base = board.basePt7b().get(permanent.getId());
        int power = base != null && base.power() != null
                ? base.power() + permanent.getPowerModifiers()
                : permanent.getEffectivePower();
        int toughness = base != null && base.toughness() != null
                ? base.toughness() + permanent.getToughnessModifiers()
                : permanent.getEffectiveToughness();
        boolean switched = board.switchedPt7d().contains(permanent.getId());
        if (switched) {
            return wantPower ? toughness : power;
        }
        return wantPower ? power : toughness;
    }

    /**
     * Basic land types for Domain (CR 702.42) evaluated from inside the layered pass. Uses the
     * layered {@link #effectiveBasicLandTypes} — so CR 305.7 overrides (Blood Moon, Urborg,
     * Prismatic Omen) count — except for a land whose own static bonus is currently being
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
        // Multiple layered reads for one answer — share a single pass across them.
        return withQueryScope(gameData, () -> {
            int power = getEffectivePower(gameData, creature);
            int toughness = getEffectiveToughness(gameData, creature);

            if (hasSelfToughnessAssignEffect(creature)) {
                return Math.max(0, toughness);
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

    private boolean hasSelfToughnessAssignEffect(Permanent creature) {
        List<CardEffect> effects = new ArrayList<>(creature.getCard().getEffects(EffectSlot.STATIC));
        effects.addAll(creature.getTemporaryTriggeredEffects(EffectSlot.STATIC));
        effects.addAll(creature.getPersistentTriggeredEffects(EffectSlot.STATIC));
        return effects.stream().anyMatch(effect ->
                effect instanceof AssignCombatDamageWithToughnessEffect assign
                        && assign.scope() == GrantScope.SELF);
    }

    /**
     * Returns the amount of damage a creature deals for non-combat effects that deal damage
     * equal to the creature's power (fight, bite, Arc-Lightning-style, Hunters, Berserker,
     * planeswalker power-damage, etc.). Equivalent to {@code Math.max(0, getEffectivePower(...))}.
     * <p>
     * A creature with 0 or negative power deals 0 damage.
     * <p>
     * Do NOT use this for combat damage assignment — use {@link #getEffectiveCombatDamage} instead,
     * which also handles Belligerent-Brontodon / Bark-of-Doran "assign damage equal to toughness"
     * effects that apply only in combat.
     */
    public int getPowerBasedDamage(GameData gameData, Permanent creature) {
        return Math.max(0, getEffectivePower(gameData, creature));
    }

    /**
     * Returns {@code true} if the creature's controller has a permanent on the battlefield
     * with an {@link AssignCombatDamageWithToughnessEffect} whose scope covers this creature
     * ({@link GrantScope#OWN_CREATURES} or {@link GrantScope#ALL_OWN_CREATURES}).
     */
    /**
     * Returns {@code true} if any player controls a permanent on the battlefield with an
     * {@link AssignCombatDamageWithToughnessEffect} scoped to {@link GrantScope#ALL_CREATURES}
     * (e.g. Doran, the Siege Tower — "each creature assigns combat damage equal to its toughness").
     */
    private boolean hasGlobalToughnessAssignEffect(GameData gameData) {
        for (List<Permanent> bf : gameData.playerBattlefields.values()) {
            for (Permanent p : bf) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof AssignCombatDamageWithToughnessEffect acdt
                            && acdt.scope() == GrantScope.ALL_CREATURES) {
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

        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        if (bf == null) return false;

        for (Permanent p : bf) {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AssignCombatDamageWithToughnessEffect acdt) {
                    GrantScope scope = acdt.scope();
                    boolean matchesPredicate = acdt.affectedPredicate() == null
                            || predicateEvaluationService.matchesPermanentPredicate(
                                    creature,
                                    acdt.affectedPredicate(),
                                    FilterContext.of(gameData).withSourceControllerId(controllerId));
                    if (matchesPredicate && scope == GrantScope.ALL_OWN_CREATURES) {
                        return true;
                    }
                    if (matchesPredicate && scope == GrantScope.OWN_CREATURES
                            && !p.getId().equals(creature.getId())) {
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
     * <p>The queries must not mutate game state — the memoized bonuses would go stale. Same
     * contract the nested-pass memo already relies on.
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
                StaticBonus memoized = active.bonusMemo().get(target.getId());
                if (memoized != null) {
                    return memoized;
                }
            }
            StaticBonus bonus = assembleStaticBonusForController(
                    gameData, active.board(), target, targetControllerId);
            if (targetControllerId == null) {
                active.bonusMemo().put(target.getId(), bonus);
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
     * layer-6/7b/7d contributions read from the board's provenance). Display-only — computed
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
            return !(attribution.effect() instanceof ProtectionGrantingEffect protection
                    && !protection.protectionFromColors().isEmpty()
                    && bonus.protectionColors().containsAll(protection.protectionFromColors()));
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
     * overrides are deliberately NOT diffed here — every 7b setter is already recorded with
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
                                long timestamp, int position, boolean fromGraveyard) {
    }

    /**
     * Legacy layer 7 accumulator assembly for one permanent, running against the layered board
     * state: sources apply in timestamp order (battlefield position for equal timestamps),
     * subtype/type filters are answered from the L4-corrected {@code CharacteristicState}s via
     * the active pass, and the sublayer-7b base P/T (every setter — static, floating one-shot,
     * animation, exchange, March MV — resolved in timestamp order by
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
     * never blocks re-entry — only the P/T leaves consult it, and only to pick their fallback.
     */
    private static final ThreadLocal<Set<UUID>> ASSEMBLY_IN_PROGRESS =
            ThreadLocal.withInitial(HashSet::new);

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
     * thing, but said it at the call site — every caller had to know which side of the boundary
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
        List<StaticSource> sources = new ArrayList<>();
        UUID resolvedTargetControllerId = targetControllerId;
        int position = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            if (resolvedTargetControllerId == null && bf.contains(target)) {
                resolvedTargetControllerId = playerId;
            }
            boolean targetOnSameBattlefield = playerId.equals(resolvedTargetControllerId);
            for (Permanent source : bf) {
                sources.add(new StaticSource(
                        source, playerId, targetOnSameBattlefield, source.getTimestamp(), position++, false));
            }
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard != null) {
                for (Card card : graveyard) {
                    if (card.getEffects(EffectSlot.STATIC).stream()
                            .noneMatch(GraveyardStaticEffect.class::isInstance)) {
                        continue;
                    }
                    sources.add(new StaticSource(new Permanent(card), playerId,
                            targetOnSameBattlefield, 0, position++, true));
                }
            }
        }
        sources.sort(Comparator.comparingLong(StaticSource::timestamp).thenComparingInt(StaticSource::position));
        List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
        for (StaticSource sourceSlot : sources) {
            Permanent source = sourceSlot.permanent();
            // CR 613.8a(1)/CR 305.7: a source whose abilities are gone — "loses all abilities"
            // applied in layer 6, or a land whose type was set (removing its printed abilities
            // in layer 4) — contributes nothing in layer 7 either: a lose-all'd lord grants
            // neither its keyword (suppressed by the pass) nor its 7c boost. Managed layer-4
            // replays below still run: the pass already decided existence with correct
            // ordering, and a skipped instance simply recorded no contributions.
            CharacteristicState sourceState = board.states().get(source.getId());
            boolean sourceAbilitiesGone = sourceState != null
                    && (sourceState.isLosesAllAbilities() || sourceState.isLosesAllNonManaAbilities()
                    || sourceState.isPrintedAbilitiesRemoved());
            if (!sourceAbilitiesGone && !source.isAttached()) {
                applyFloatingStaticGrantsFromSource(gameData, sourceSlot, target, accumulator, globalWordChange);
            }
            if (source == target) continue;
            StaticEffectContext context = new StaticEffectContext(
                    source, target, sourceSlot.controllerId(), sourceSlot.sameBattlefieldAsTarget(), gameData);
            AccumulatorSnapshot beforeSource = explain != null ? AccumulatorSnapshot.of(accumulator) : null;
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (sourceSlot.fromGraveyard() != (effect instanceof GraveyardStaticEffect)) {
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
                if (sourceAbilitiesGone) {
                    continue;
                }
                StaticEffectHandler handler = staticEffectRegistry.getHandler(effect);
                if (handler != null) {
                    // Layer 5/6 outputs of pass-managed instances were applied in timestamp
                    // order by the layered pass and merged in below; the handler still runs for
                    // its other-layer outputs (a lord's 7c boost) with those adds discarded.
                    // The handler sees the layer-3 view of the ability — the source's text
                    // changes applied — matching what the layered pass applied.
                    boolean layeredManaged = board.isManagedL56(effect);
                    if (layeredManaged) {
                        accumulator.setLayeredOutputsSuppressed(true);
                    }
                    try {
                        handler.apply(context,
                                TextChangeTransformer.transform(effect, source.getTextReplacements(),
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
                ModifierLine line = beforeSource.diff(source.getCard().getName(), accumulator, false);
                if (!line.isEmpty()) {
                    explain.add(line);
                }
                recordGrantedEffectDiff(beforeSource, source.getCard().getName(),
                        accumulator, explainEffects);
            }
        }
        // Process emblem static effects
        for (Emblem emblem : gameData.emblems) {
            List<Permanent> ownerBf = gameData.playerBattlefields.get(emblem.controllerId());
            if (ownerBf == null || !ownerBf.contains(target)) continue;
            AccumulatorSnapshot beforeEmblem = explain != null ? AccumulatorSnapshot.of(accumulator) : null;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof GrantActivatedAbilityEffect grant
                        && grant.scope() == GrantScope.OWN_PERMANENTS
                        // null GameData: type predicates read printed/granted types without
                        // re-entering computeStaticBonus (same as GrantKeywordEffect below).
                        && (grant.filter() == null || predicateEvaluationService.matchesPermanentPredicate(null, target, grant.filter()))) {
                    accumulator.addActivatedAbility(grant.ability());
                } else if (effect instanceof StaticBoostEffect boost
                        && (boost.scope() == GrantScope.OWN_CREATURES || boost.scope() == GrantScope.ALL_OWN_CREATURES)
                        // Recursion-safe: isCreature(gameData, target) re-enters computeStaticBonus for
                        // this same target (this method IS the assembly), so an emblem's "creatures you
                        // control get +1/+0" (Sorin, Lord of Innistrad) would recurse forever. Read the
                        // layer-4 board state already computed for this pass instead.
                        && isCreatureInStaticPass(board, target)
                        && (boost.filter() == null || predicateEvaluationService.matchesPermanentPredicate(null, target, boost.filter()))) {
                    accumulator.addPower(boost.powerBoost());
                    accumulator.addToughness(boost.toughnessBoost());
                    accumulator.addKeywords(boost.grantedKeywords());
                } else if (effect instanceof GrantKeywordEffect grant
                        && (grant.scope() == GrantScope.OWN_PERMANENTS
                        || grant.scope() == GrantScope.OWN_LANDS && isLandInStaticPass(board, target)
                        || (grant.scope() == GrantScope.OWN_CREATURES
                        || grant.scope() == GrantScope.ALL_OWN_CREATURES)
                        && isCreatureInStaticPass(board, target))
                        // Evaluate the filter with a null GameData so type predicates read the
                        // permanent's printed/granted types directly instead of re-entering
                        // computeStaticBonus for this same target (which would recurse forever).
                        && (grant.filter() == null || predicateEvaluationService.matchesPermanentPredicate(null, target, grant.filter()))) {
                    accumulator.addKeywords(grant.keywords());
                } else if (effect instanceof GrantTriggeredAbilityEffect grant
                        && (grant.scope() == GrantScope.OWN_CREATURES
                                || grant.scope() == GrantScope.ALL_OWN_CREATURES)
                        && isCreatureInStaticPass(board, target)
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

        // Indefinite target buffs (Riding the Dilu Horse): a resolved spell recorded a PERMANENT
        // floating BuffTargetCreatureIndefinitelyEffect on this permanent. It has no static-slot
        // source, so it is read here rather than through a handler — the additive +P/+Y (7c) and
        // granted keywords (layer 6) apply for as long as the permanent exists; multiple copies
        // stack additively.
        AccumulatorSnapshot beforeIndefinite = explain != null ? AccumulatorSnapshot.of(accumulator) : null;
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (board.isManagedL4(floating.effect())) {
                    board.replayL4Contribution(floating.effect(), target.getId(), accumulator);
                }
                if (floating.effect() instanceof BuffTargetCreatureIndefinitelyEffect buff
                        && target.getId().equals(floating.affectedPermanentId())) {
                    accumulator.addPower(buff.powerBoost());
                    accumulator.addToughness(buff.toughnessBoost());
                    accumulator.addKeywords(buff.keywords());
                }
                // Mist Dragon's {0} abilities: an indefinite layer-6 keyword gain or loss on this
                // permanent. Only the most recent activation per keyword survives (the handler
                // drops the previous one), so there is no timestamp ordering to resolve here.
                if (floating.effect() instanceof SetSelfKeywordIndefinitelyEffect keywordChange
                        && target.getId().equals(floating.affectedPermanentId())) {
                    if (keywordChange.gained()) {
                        accumulator.addKeywords(Set.of(keywordChange.keyword()));
                    } else {
                        accumulator.removeKeyword(keywordChange.keyword());
                    }
                }
            }
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
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof GraveyardStaticEffect) {
                continue;
            }
            // A self-including scope (ALL_LANDS_INCLUDING_SELF, ALL_CREATURES_INCLUDING_SELF)
            // records a layer-4 contribution on its own source; the source-loop above skips
            // source == target, so the replay for those has to happen here.
            if (board.isManagedL4(effect)) {
                board.replayL4Contribution(effect, target.getId(), accumulator);
                continue;
            }
            StaticEffectHandler selfHandler = staticEffectRegistry.getSelfHandler(effect);
            if (selfHandler != null) {
                // CR 613.4a: an object whose own static abilities were removed in layer 6
                // ("loses all abilities") contributes no characteristic-defining P/T in 7a —
                // Maro under Merfolk Trickster is 0/0. The removal is NOT retroactive on
                // layers 2-5: type/color contributions the removed abilities already made in
                // earlier layers stay applied (CR 613 layers apply in order; see
                // agent-docs/LAYER_SYSTEM.md §3).
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
                            TextChangeTransformer.transform(effect, target.getTextReplacements(),
                                    globalWordChange),
                            accumulator);
                } finally {
                    if (layeredManaged) {
                        accumulator.setLayeredOutputsSuppressed(false);
                    }
                }
            }
        }
        if (beforeSelf != null) {
            // The 7a CDA base line — emitted before the board's 7b lines fold over it.
            ModifierLine line = beforeSelf.diff(target.getCard().getName(), accumulator, true);
            if (!line.isEmpty()) {
                explain.add(line);
            }
            recordGrantedEffectDiff(beforeSelf, target.getCard().getName(),
                    accumulator, explainEffects);
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
        }

        // "Becomes the basic land type" override (Tideshaper Mystic until end of turn, Orcish Farmer
        // until controller's next untap step): replaces the land's subtypes and mana ability (rule 305.7).
        if (target.getEffectiveLandTypeOverride() != null) {
            accumulator.addGrantedSubtype(target.getEffectiveLandTypeOverride());
            accumulator.setSubtypeOverriding(true);
            accumulator.setLandSubtypeOverriding(true);
        }

        if (state != null && !state.getCardTypes().equals(baseCardTypes(target))) {
            accumulator.getGrantedCardTypes().clear();
            accumulator.getGrantedCardTypes().addAll(state.getCardTypes());
            accumulator.setCardTypeOverriding(true);
        }

        // Sublayer 7d: the parity of the active floating switch effects, resolved by the
        // layered pass; the effective-P/T queries swap the finished 7a-7c values when set.
        boolean ptSwitched = board.switchedPt7d().contains(target.getId());

        boolean layeredTouched = state != null
                && (board.l56Touched().contains(target.getId()) || basePt7b != null || ptSwitched);
        boolean isSelfAnimated = target.isAnimatedUntilEndOfTurn() || target.isAnimatedUntilEndOfCombat() || target.isAnimatedUntilNextTurn() || target.getCounterCount(CounterType.AWAKENING) > 0 || accumulator.isSelfBecomeCreature();
        if (!isNaturalCreature
                && !accumulator.isAnimatedCreature()
                && !isSelfAnimated
                && !layeredTouched
                && accumulator.getKeywords().isEmpty()
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
                && accumulator.getName() == null) {
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
            Set<Keyword> mergedKeywords = new HashSet<>(state.getKeywords());
            mergedKeywords.addAll(accumulator.getKeywords());
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
            mergedProtection.addAll(accumulator.getProtectionColors());
            protectionColors = mergedProtection;
            removedProtectionColors = Set.copyOf(state.getRemovedProtectionColors());
            List<ActivatedAbility> mergedAbilities = new ArrayList<>(state.getGrantedActivatedAbilities());
            mergedAbilities.addAll(accumulator.getGrantedActivatedAbilities());
            grantedActivatedAbilities = mergedAbilities;
            List<CardEffect> mergedEffects = new ArrayList<>(state.getGrantedStaticEffects());
            mergedEffects.addAll(accumulator.getGrantedEffects());
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
            // still be the complete set — reconstruct it with the legacy Permanent.hasKeyword
            // semantics plus the accumulator's grants.
            Set<Keyword> mergedKeywords = new HashSet<>(accumulator.getKeywords());
            if (!target.isLosesAllAbilitiesUntilEndOfTurn()) {
                mergedKeywords.addAll(target.getCard().getKeywords());
                mergedKeywords.addAll(target.getGrantedKeywords());
                mergedKeywords.addAll(target.getPersistentGrantedKeywords());
                mergedKeywords.addAll(target.getUntilNextTurnKeywords());
                mergedKeywords.removeAll(target.getRemovedKeywords());
                if (target.isLosesAllCreatureTypesUntilEndOfTurn()) {
                    mergedKeywords.remove(Keyword.CHANGELING);
                }
            }
            mergedKeywords.removeAll(accumulator.getRemovedKeywords());
            keywords = mergedKeywords;
        }

        return new StaticBonus(accumulator.getPower(), accumulator.getToughness(), keywords,
                protectionColors, removedProtectionColors,
                accumulator.isAnimatedCreature() || isSelfAnimated,
                grantedActivatedAbilities, grantedEffects, grantedColors,
                accumulator.getGrantedSubtypes(), accumulator.getGrantedCardTypes(),
                accumulator.getGrantedSupertypes(), colorOverriding,
                accumulator.isSubtypeOverriding(), accumulator.isLandSubtypeOverriding(),
                accumulator.isCardTypeOverriding(),
                removedKeywords, accumulator.isBasePTOverridden(),
                accumulator.getBasePowerOverride() != null ? accumulator.getBasePowerOverride() : 0,
                accumulator.getBaseToughnessOverride() != null ? accumulator.getBaseToughnessOverride() : 0,
                losesAllAbilities, losesAllNonManaAbilities, ptSwitched, accumulator.getName());
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
                StaticEffectHandler handler = staticEffectRegistry.getHandler(grant.staticEffect());
                if (handler != null) {
                    handler.apply(context,
                            TextChangeTransformer.transform(grant.staticEffect(), source.getTextReplacements(),
                                    globalWordChange),
                            accumulator);
                }
            }
        }
    }

    // --- Protection & evasion ---

    /** Returns {@code true} if the target permanent has protection from the source's controller. */
    public boolean hasProtectionFromOpponents(GameData gameData, Permanent target, UUID sourceControllerId) {
        if (target == null || sourceControllerId == null
                || !target.isProtectionFromOpponentsPermanently()
                || target.isLosesAllAbilitiesUntilEndOfTurn()) {
            return false;
        }
        StaticBonus bonus = computeStaticBonus(gameData, target);
        if (bonus.losesAllAbilities()) {
            return false;
        }
        return target.getProtectionFromPlayerIdsPermanently().contains(sourceControllerId);
    }

    private boolean hasProtectionFromOpponentCreature(GameData gameData, Permanent target, Permanent source) {
        if (target == null || source == null || !target.isProtectionFromOpponentCreaturesUntilEndOfTurn()
                || target.isLosesAllAbilitiesUntilEndOfTurn() || !isCreature(gameData, source)) {
            return false;
        }
        StaticBonus bonus = computeStaticBonus(gameData, target);
        if (bonus.losesAllAbilities()) {
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
                || !target.isProtectionFromOpponentCreaturesUntilEndOfTurn()
                || target.isLosesAllAbilitiesUntilEndOfTurn()
                || (source.getType() != CardType.CREATURE
                && !source.getAdditionalTypes().contains(CardType.CREATURE))) {
            return false;
        }
        StaticBonus bonus = computeStaticBonus(gameData, target);
        if (bonus.losesAllAbilities()) {
            return false;
        }
        UUID targetControllerId = findPermanentController(gameData, target.getId());
        return targetControllerId != null && !targetControllerId.equals(sourceControllerId);
    }

    /**
     * Returns {@code true} if the target permanent has protection from the given color.
     * Checks the permanent's own {@link ProtectionGrantingEffect}, static bonuses from
     * other permanents, and temporary protection grants.
     */
    public boolean hasProtectionFrom(GameData gameData, Permanent target, CardColor sourceColor) {
        if (sourceColor == null) {
            return target.isProtectionFromColorlessUntilEndOfTurn();
        }
        StaticBonus bonus = computeStaticBonus(gameData, target);
        if (bonus == StaticBonus.NONE) {
            // No continuous effect touched the permanent: its own printed protection stands.
            for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ProtectionGrantingEffect protection
                        && protection.protectionScope() == null
                        && protection.protectionFromColors().contains(sourceColor)) {
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
                        && protection.protectionFromColors().contains(sourceColor)) {
                    return true;
                }
            }
        }
        if (target.getProtectionFromColorsUntilEndOfTurn().contains(sourceColor)) {
            return true;
        }
        if (!bonus.removedProtectionColors().contains(sourceColor)
                && !bonus.losesAllAbilities()
                && target.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(effect -> effect instanceof ProtectionFromColorsOfPermanentsYouControlEffect protection
                                && protection.scope() == null)
                && controlsPermanentOfColor(gameData, target, sourceColor)) {
            return true;
        }
        return false;
    }

    /**
     * Empty-Shrine Kannushi: returns {@code true} if the permanent's controller controls any
     * permanent (including the asking permanent itself) whose current colors include
     * {@code color}. Layer-5 aware, so color-changing effects are honoured.
     */
    private boolean controlsPermanentOfColor(GameData gameData, Permanent permanent, CardColor color) {
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        for (Permanent candidate : battlefield) {
            if (getEffectiveColors(gameData, candidate).contains(color)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prismatic Ward: returns {@code true} if {@code creature} is enchanted by an Aura carrying
     * {@link PreventColorDamageToEnchantedCreatureEffect} whose chosen colour is among the damage
     * source's colours. Only prevents damage (not blocking/targeting/enchanting), and only while
     * damage is currently preventable (respects Leyline of Punishment, etc.).
     */
    public boolean isColorDamageToEnchantedCreaturePrevented(GameData gameData, Permanent creature, Set<CardColor> sourceColors) {
        if (creature == null || sourceColors == null || sourceColors.isEmpty()) return false;
        if (!isDamagePreventable(gameData)) return false;
        sourceColors = getDamageSourceColors(gameData, sourceColors);
        if (sourceColors.isEmpty()) return false;
        final Set<CardColor> effectiveColors = sourceColors;
        return gameData.anyPermanentMatches(aura ->
                aura.isAttached() && creature.getId().equals(aura.getAttachedTo())
                        && aura.getChosenColor() != null && effectiveColors.contains(aura.getChosenColor())
                        && aura.getCard().getEffects(EffectSlot.STATIC).stream()
                                .anyMatch(PreventColorDamageToEnchantedCreatureEffect.class::isInstance));
    }

    /**
     * Well-Laid Plans: returns {@code true} when a global shared-color creature damage prevention
     * effect is active and the source and recipient are different creatures with at least one
     * current color in common.
     */
    public boolean isDamageBetweenCreaturesOfSharedColorPrevented(GameData gameData,
                                                                   Permanent target,
                                                                   Permanent source) {
        if (target == null || source == null || target.getId().equals(source.getId())) return false;
        if (!isDamagePreventable(gameData) || !isCreature(gameData, target) || !isCreature(gameData, source)) {
            return false;
        }
        Set<CardColor> targetColors = getEffectiveColors(gameData, target);
        Set<CardColor> sourceColors = getEffectiveColors(gameData, source);
        if (targetColors.isEmpty() || sourceColors.stream().noneMatch(targetColors::contains)) return false;
        return gameData.anyPermanentMatches(permanent ->
                permanent.getCard().getEffects(EffectSlot.STATIC).stream()
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
                    greatest = Math.max(greatest, candidate.getCard().getManaValue());
                }
            }
        }
        return permanent.getCard().getManaValue() == greatest;
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
                .mapToInt(candidate -> candidate.getCard().getManaValue())
                .max()
                .orElse(-1);
        return permanent.getCard().getManaValue() == greatest;
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
                    greatest = Math.max(greatest, candidate.getCard().getManaValue());
                }
            }
        }
        return permanent.getCard().getManaValue() == greatest;
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
                    lowest = Math.min(lowest, candidate.getCard().getManaValue());
                }
            }
        }
        return permanent.getCard().getManaValue() == lowest;
    }

    /**
     * Returns {@code true} if the target permanent has protection from any of the source
     * permanent's card types. Accounts for artifact status (including granted) and creature
     * status (including animation).
     */
    public boolean hasProtectionFromSourceCardTypes(GameData gameData, Permanent target, Permanent source) {
        if (hasProtectionFromMulticolored(gameData, target, getEffectiveColors(gameData, source))) {
            return true;
        }
        Set<CardType> protectedTypes = EnumSet.noneOf(CardType.class);
        protectedTypes.addAll(target.getProtectionFromCardTypes());
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionGrantingEffect protection) {
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
        if (protectedTypes.contains(CardType.ARTIFACT) && isArtifact(source)) return true;
        if (protectedTypes.contains(CardType.CREATURE) && isCreature(gameData, source)) return true;
        if (protectedTypes.contains(source.getCard().getType())) return true;
        for (CardType type : source.getCard().getAdditionalTypes()) {
            if (protectedTypes.contains(type)) return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the target permanent has protection from any of the source
     * card's card types. This overload is used for spells on the stack (which are cards,
     * not permanents) and only checks the card's natural types.
     */
    public boolean hasProtectionFromSourceCardTypes(Permanent target, Card sourceCard) {
        Set<CardType> protectedTypes = EnumSet.noneOf(CardType.class);
        protectedTypes.addAll(target.getProtectionFromCardTypes());
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
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
     * Card-source overload that also evaluates protection granted by continuous battlefield
     * effects. The legacy overload remains for callers that only have a target and a card.
     */
    public boolean hasProtectionFromSourceCardTypes(GameData gameData, Permanent target, Card sourceCard) {
        if (hasProtectionFromMulticolored(gameData, target, sourceCard)) {
            return true;
        }
        if (hasProtectionFromSourceCardTypes(target, sourceCard)) {
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

    public boolean hasProtectionFromMulticolored(GameData gameData, Permanent target, Card sourceCard) {
        if (sourceCard == null || sourceCard.getColors().size() < 2) {
            return false;
        }
        return hasProtectionFromMulticoloredEffects(target.getCard().getEffects(EffectSlot.STATIC))
                || hasProtectionFromMulticoloredEffects(computeStaticBonus(gameData, target).grantedEffects());
    }

    private boolean hasProtectionFromMulticolored(GameData gameData, Permanent target,
                                                   Set<CardColor> sourceColors) {
        return sourceColors.size() >= 2
                && (hasProtectionFromMulticoloredEffects(target.getCard().getEffects(EffectSlot.STATIC))
                || hasProtectionFromMulticoloredEffects(computeStaticBonus(gameData, target).grantedEffects()));
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

    /**
     * Returns {@code true} if the target permanent has protection from any of the source
     * permanent's subtypes. Checks the permanent's own card subtypes, transient subtypes,
     * granted subtypes, and Changeling keyword (which counts as all creature subtypes).
     */
    public boolean hasProtectionFromSourceSubtypes(GameData gameData, Permanent target, Permanent source) {
        Set<CardSubtype> protectedSubtypes = EnumSet.noneOf(CardSubtype.class);
        Set<CardSubtype> creatureOnlySubtypes = EnumSet.noneOf(CardSubtype.class);
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            collectProtectedSubtypes(effect, protectedSubtypes, creatureOnlySubtypes);
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
        for (CardSubtype subtype : source.getCard().getSubtypes()) {
            if (protectedSubtypes.contains(subtype)) return true;
        }
        for (CardSubtype subtype : source.getTransientSubtypes()) {
            if (protectedSubtypes.contains(subtype)) return true;
        }
        for (CardSubtype subtype : source.getGrantedSubtypes()) {
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
        Set<CardSubtype> protectedSubtypes = EnumSet.noneOf(CardSubtype.class);
        Set<CardSubtype> creatureOnlySubtypes = EnumSet.noneOf(CardSubtype.class);
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
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
     * Splits one effect's subtype protection into the unconditional set and the
     * "…creatures only" set, so a "protection from [type] creatures" grant (Riders of Gavony)
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
        Set<CardSubtype> protectedFrom = target.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn();
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
    private boolean hasProtectionFromNonSubtypeCreatures(Permanent target, Card sourceCard) {
        Set<CardSubtype> protectedFrom = target.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn();
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

    /**
     * Returns {@code true} if the target permanent has protection from mana value N or greater
     * and the source's mana value meets that threshold (e.g. Mistmeadow Skulk).
     */
    private boolean hasProtectionFromSourceManaValue(Permanent target, Card sourceCard) {
        int chosenNumber = target.getChosenNumber();
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ProtectionGrantingEffect protection) {
                if (protection.protectionFromManaValueAtLeast().isPresent()
                        && sourceCard.getManaValue() >= protection.protectionFromManaValueAtLeast().getAsInt()) {
                    return true;
                }
                if (protection.protectionFromManaValuesOtherThanChosen().contains(chosenNumber)
                        && sourceCard.getManaValue() != chosenNumber) {
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

    /**
     * Variant taking the source's precomputed effective colors, for sweeps that already
     * snapshotted them (see {@code BlockLegalityContext} in {@code service.combat.block}).
     */
    public boolean hasProtectionFromSource(GameData gameData, Permanent target, Permanent source,
                                           Set<CardColor> sourceColors) {
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
        return (target.isProtectionFromColorlessUntilEndOfTurn() && sourceColors.isEmpty())
                || hasProtectionFromSourceCardTypes(gameData, target, source)
                || hasProtectionFromSourceSubtypes(gameData, target, source)
                || hasProtectionFromNonSubtypeCreatures(gameData, target, source)
                || hasProtectionFromSourceManaValue(target, source.getCard());
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
        UUID protectionSourcePlayerId = sourceControllerId != null
                ? sourceControllerId
                : sourceCard.getOwnerId();
        return hasProtectionFromOpponentCreature(gameData, target, sourceCard, protectionSourcePlayerId)
                || hasProtectionFromOpponents(gameData, target, protectionSourcePlayerId)
                || hasProtectionFrom(gameData, target, getDamageSourceColor(gameData, sourceCard.getColor()))
                || hasProtectionFromColoredSpellSource(gameData, target, sourceCard)
                || hasProtectionFromSourceCardTypes(gameData, target, sourceCard)
                || hasProtectionFromSourceSubtypes(target, sourceCard)
                || hasProtectionFromNonSubtypeCreatures(target, sourceCard)
                || hasProtectionFromSourceManaValue(target, sourceCard);
    }

    public boolean hasProtectionFromSource(GameData gameData, Permanent target, Card sourceCard) {
        return hasProtectionFromSource(gameData, target, sourceCard, null);
    }

    public boolean hasProtectionFromSource(GameData gameData, Permanent target, Card sourceCard,
                                           UUID sourceControllerId) {
        UUID protectionSourcePlayerId = sourceControllerId != null
                ? sourceControllerId
                : sourceCard.getOwnerId();
        Set<CardColor> sourceColors = getEffectiveCardColors(gameData, sourceCard);
        return hasProtectionFromOpponentCreature(gameData, target, sourceCard, protectionSourcePlayerId)
                || hasProtectionFromOpponents(gameData, target, protectionSourcePlayerId)
                || sourceColors.stream().anyMatch(color -> hasProtectionFrom(gameData, target, color))
                || (target.isProtectionFromColorlessUntilEndOfTurn() && sourceColors.isEmpty())
                || hasProtectionFromColoredSpellSource(gameData, target, sourceCard)
                || hasProtectionFromSourceCardTypes(gameData, target, sourceCard)
                || hasProtectionFromSourceSubtypes(target, sourceCard)
                || hasProtectionFromNonSubtypeCreatures(target, sourceCard)
                || hasProtectionFromSourceManaValue(target, sourceCard);
    }

    public boolean hasProtectionFromColoredSpellSource(GameData gameData, Permanent target, Card sourceCard) {
        if (sourceCard == null || !isSpellOnStack(gameData, sourceCard)
                || getEffectiveCardColors(gameData, sourceCard).isEmpty()) {
            return false;
        }
        return hasProtectionFromColoredSpells(gameData, target);
    }

    public boolean hasProtectionFromColoredSpells(GameData gameData, Permanent target) {
        return hasProtectionFromColoredSpellEffect(target.getCard().getEffects(EffectSlot.STATIC))
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
     * Uncle Istvan: whether damage from a creature source to {@code target} is prevented by the target's
     * own {@link PreventDamageToSelfFromCreaturesEffect}. True only while damage is preventable, the target
     * carries the effect, and the damage source is a creature. Combat damage is handled directly in
     * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyCreaturePreventionShield}
     * (its source is always a creature); this covers the noncombat path where the source is known.
     */
    public boolean isCreatureSourceDamageToSelfPrevented(GameData gameData, Permanent target, StackEntry entry, Permanent explicitSource) {
        if (!isDamagePreventable(gameData)) return false;
        boolean preventsAllCreatureDamage = target.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(PreventDamageToSelfFromCreaturesEffect.class::isInstance);
        boolean preventsDamageFromBlockedCreature = target.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(PreventAllDamageToSelfFromCreaturesItBlocksEffect.class::isInstance);
        if (!preventsAllCreatureDamage && !preventsDamageFromBlockedCreature) return false;
        Permanent source = explicitSource;
        if (source == null && entry != null && entry.getSourcePermanentId() != null) {
            source = findPermanentById(gameData, entry.getSourcePermanentId());
        }
        Permanent sourcePermanent = source;
        if (preventsAllCreatureDamage && sourcePermanent != null && isCreature(gameData, sourcePermanent)
                && target.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(PreventDamageToSelfFromCreaturesEffect.class::isInstance)
                .map(PreventDamageToSelfFromCreaturesEffect.class::cast)
                .anyMatch(effect -> effect.sourcePredicate() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, sourcePermanent, effect.sourcePredicate()))) {
            return true;
        }
        if (!preventsDamageFromBlockedCreature) {
            return false;
        }
        return source != null && isCreature(gameData, source)
                && target.isBlocking() && target.getBlockingTargetIds().contains(source.getId());
    }

    public boolean sourceHasKeyword(GameData gameData, StackEntry entry, Permanent explicitSource, Keyword keyword) {
        Permanent source = explicitSource;
        if (source == null && entry.getSourcePermanentId() != null) {
            source = findPermanentById(gameData, entry.getSourcePermanentId());
        }
        if (source != null) {
            return hasKeyword(gameData, source, keyword);
        }
        // No permanent source: the stack entry itself is the source (e.g. an instant/sorcery
        // spell like Puncture Blast). Its printed keywords (wither, etc.) apply. CR 702.80.
        boolean entryHasKeyword = entry.getSourcePermanentId() == null
                && entry.getCard() != null
                && (entry.getCard().getKeywords().contains(keyword)
                || entry.getGrantedKeywordsOnEntry().contains(keyword));
        if (entryHasKeyword) {
            return true;
        }
        return keyword == Keyword.DEATHTOUCH
                && entry.getSourcePermanentId() == null
                && entry.getCard() != null
                && (entry.getCard().hasType(CardType.INSTANT) || entry.getCard().hasType(CardType.SORCERY))
                && playerControlsStaticEffect(gameData, entry.getControllerId(),
                GrantDeathtouchToControllerSpellsEffect.class);
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
        return battlefield.stream().anyMatch(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(effectType::isInstance));
    }

    /** Returns the number of STATIC-slot effects of the given type controlled by a player. */
    public int countPlayerControlledStaticEffects(GameData gameData, UUID playerId,
                                                   Class<? extends CardEffect> effectType) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        return (int) battlefield.stream()
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream())
                .filter(effectType::isInstance)
                .count();
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
        if (spellColor == null) {
            return false;
        }
        boolean opponentControlled = isOpponentControlledSpell(gameData, target, spellControllerId);
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (isSpellColorRestriction(effect, spellColor, opponentControlled)) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isSpellColorRestriction(effect, spellColor, opponentControlled)) {
                return true;
            }
        }

        // Check per-player turn-duration protection (Autumn's Veil style)
        if (isCreature(gameData, target)) {
            UUID controllerId = findPermanentController(gameData, target.getId());
            if (controllerId != null) {
                Set<CardColor> protectedColors = gameData.playerCreaturesCantBeTargetedByColorsThisTurn.get(controllerId);
                if (protectedColors != null && protectedColors.contains(spellColor)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns {@code true} if the target permanent can't be the target of any spell (regardless of
     * color or controller) — Dense Foliage's "Creatures can't be the targets of spells". Abilities are
     * unaffected. Scans both the permanent's own STATIC effects and effects granted to it.
     */
    public boolean cantBeTargetedByAnySpell(GameData gameData, Permanent target) {
        boolean ownAbilitiesActive = !target.isLosesAllAbilitiesUntilEndOfTurn()
                && !computeStaticBonus(gameData, target).losesAllAbilities();
        if (ownAbilitiesActive) {
            for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
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
        return false;
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
        if (target.isLosesAllAbilitiesUntilEndOfTurn() || bonus.losesAllAbilities()) {
            return false;
        }
        if (target.getCard().getEffects(EffectSlot.STATIC).stream()
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
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
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
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantBeEnchantedByOtherAurasEffect) {
                return true;
            }
        }
        return hasGrantedEffect(gameData, target, CantBeEnchantedByOtherAurasEffect.class);
    }

    /**
     * Returns {@code true} if the permanent can't have an Equipment attached to it, from its own
     * static effects or from effects granted by other permanents.
     */
    public boolean cantBeEquipped(GameData gameData, Permanent target) {
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantBeEquippedEffect) {
                return true;
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
                                                   boolean opponentControlled) {
        return effect instanceof TargetingRestrictionEffect r
                && (r.kind() == TargetingSourceKind.SPELLS
                || r.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES)
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
        if (sourceColor == null) {
            return false;
        }
        Set<CardColor> turnColors = gameData.permanentHexproofFromColorsThisTurn.get(target.getId());
        if (turnColors != null && turnColors.contains(sourceColor)) {
            return true;
        }
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
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
        return hasMonocoloredSourceRestriction(gameData, target, true);
    }

    /** Returns whether monocolored spells and abilities can't target this permanent. */
    public boolean cantBeTargetedByMonocoloredSources(GameData gameData, Permanent target) {
        return hasMonocoloredSourceRestriction(gameData, target, false);
    }

    private boolean hasMonocoloredSourceRestriction(GameData gameData, Permanent target, boolean opponentOnly) {
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
            if (isMonocoloredSourceRestriction(effect, opponentOnly)) {
                return true;
            }
        }
        for (CardEffect effect : computeStaticBonus(gameData, target).grantedEffects()) {
            if (isMonocoloredSourceRestriction(effect, opponentOnly)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMonocoloredSourceRestriction(CardEffect effect, boolean opponentOnly) {
        return effect instanceof TargetingRestrictionEffect restriction
                && restriction.kind() == TargetingSourceKind.SPELLS_AND_ABILITIES
                && restriction.mode() == TargetColorMode.MONOCOLORED
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
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
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
        if (sourceCard == null) {
            return false;
        }
        for (CardEffect effect : target.getCard().getEffects(EffectSlot.STATIC)) {
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
                || r.colors().stream().anyMatch(color -> sourceHasColor(sourceCard, color))) {
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

    private boolean sourceHasColor(Card card, CardColor color) {
        if (card.getColor() == color) {
            return true;
        }
        return card.getColors().contains(color);
    }

    /**
     * Returns {@code true} if the given spell cannot be countered by the given counter source,
     * because the spell's controller has turn-duration protection from countering by spells
     * of the source's color (e.g. Autumn's Veil). Only applies when the counter source is
     * a spell (not a triggered or activated ability).
     */
    public boolean isProtectedFromCounterBySpellColor(GameData gameData, UUID spellControllerId, StackEntry counterSource) {
        Set<CardColor> protectedColors = gameData.playerSpellsCantBeCounteredByColorsThisTurn.get(spellControllerId);
        if (protectedColors == null || protectedColors.isEmpty()) {
            return false;
        }
        // Only protects against spells, not abilities
        StackEntryType sourceType = counterSource.getEntryType();
        if (sourceType == StackEntryType.TRIGGERED_ABILITY || sourceType == StackEntryType.ACTIVATED_ABILITY) {
            return false;
        }
        return protectedColors.contains(counterSource.getCard().getColor());
    }

    /**
     * Returns {@code true} if the given spell cannot be countered by the given source card,
     * because the spell's controller has turn-duration protection from countering by spells
     * of that color (e.g. Autumn's Veil). Only applies when the source is an instant or sorcery.
     */
    public boolean isProtectedFromCounterBySourceCard(GameData gameData, UUID spellControllerId, Card sourceCard) {
        Set<CardColor> protectedColors = gameData.playerSpellsCantBeCounteredByColorsThisTurn.get(spellControllerId);
        if (protectedColors == null || protectedColors.isEmpty()) {
            return false;
        }
        // Only protects against spells, not abilities
        CardType sourceType = sourceCard.getType();
        if (sourceType != CardType.INSTANT && sourceType != CardType.SORCERY) {
            return false;
        }
        return protectedColors.contains(sourceCard.getColor());
    }

    /**
     * Returns {@code true} if Peace Talks is active: creatures can't attack, and players and
     * permanents can't be the targets of spells or activated abilities (triggered abilities are
     * unaffected). Active while {@link GameData#peaceTalksTurnsRemaining} {@code > 0}.
     */
    public boolean isPeaceTalksActive(GameData gameData) {
        return gameData.peaceTalksTurnsRemaining > 0;
    }

    /**
     * Returns {@code true} if the player has shroud (cannot be the target of spells or
     * abilities), granted either temporarily or by a permanent they control with a
     * {@link GrantControllerKeywordEffect} for {@link Keyword#SHROUD}.
     */
    public boolean playerHasShroud(GameData gameData, UUID playerId) {
        return gameData.playerKeywordsUntilEndOfTurn
                .getOrDefault(playerId, Set.of()).contains(Keyword.SHROUD)
                || playerBattlefieldGrantsControllerKeyword(gameData, playerId, Keyword.SHROUD);
    }

    /**
     * Returns {@code true} if the player has hexproof (cannot be the target of spells or
     * abilities opponents control), granted either temporarily or by a permanent they control
     * with a {@link GrantControllerKeywordEffect} for {@link Keyword#HEXPROOF}.
     */
    public boolean playerHasHexproof(GameData gameData, UUID playerId) {
        return gameData.playerKeywordsUntilEndOfTurn
                .getOrDefault(playerId, Set.of()).contains(Keyword.HEXPROOF)
                || playerBattlefieldGrantsControllerKeyword(gameData, playerId, Keyword.HEXPROOF);
    }

    /**
     * Returns whether the player has hexproof from the given color until end of turn. This is
     * targeting-only and does not provide protection from damage.
     */
    public boolean playerHasHexproofFromColor(GameData gameData, UUID playerId, CardColor color) {
        if (color == null) {
            return false;
        }
        Set<CardColor> colors = gameData.playerHexproofFromColorsThisTurn.get(playerId);
        return colors != null && colors.contains(color);
    }

    /**
     * Returns {@code true} if the player has protection from the given color until end of turn
     * (e.g. Faith's Shield fateful hour). Such a player can't be targeted by spells or abilities
     * of that color and can't be dealt damage by sources of that color.
     */
    public boolean playerHasProtectionFromColor(GameData gameData, UUID playerId, CardColor color) {
        if (color == null) {
            return false;
        }
        Set<CardColor> colors = gameData.playerProtectionFromColorsUntilEndOfTurn.get(playerId);
        return colors != null && colors.contains(color);
    }

    /** Returns whether the player has protection from every source until their next turn. */
    public boolean playerHasProtectionFromEverything(GameData gameData, UUID playerId) {
        return gameData.playersWithProtectionFromEverythingUntilNextTurn.contains(playerId);
    }

    /**
     * Returns whether the player has protection from a source controlled by the given opponent,
     * through a static effect on a permanent they control.
     */
    public boolean playerHasProtectionFromOpponents(GameData gameData, UUID playerId,
                                                    UUID sourceControllerId) {
        if (playerId == null || sourceControllerId == null || playerId.equals(sourceControllerId)) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent permanent : battlefield) {
            if (permanent.isLosesAllAbilitiesUntilEndOfTurn()
                    || computeStaticBonus(gameData, permanent).losesAllAbilities()
                    || permanent.isStaticEffectSuppressed(PlayerHasProtectionFromOpponentsEffect.class)) {
                continue;
            }
            if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(PlayerHasProtectionFromOpponentsEffect.class::isInstance)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the player controls a permanent with a
     * {@link PlayerHasProtectionFromChosenNameEffect} static effect whose chosen card name equals
     * the given name (Runed Halo). Such a player can't be targeted, dealt damage, or enchanted by
     * anything with that name.
     */
    public boolean playerHasProtectionFromChosenName(GameData gameData, UUID playerId, String cardName) {
        if (cardName == null) {
            return false;
        }
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) {
            return false;
        }
        for (Permanent perm : bf) {
            if (cardName.equals(perm.getChosenName())
                    && perm.getCard().getEffects(EffectSlot.STATIC).stream()
                            .anyMatch(PlayerHasProtectionFromChosenNameEffect.class::isInstance)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if {@code controllerId} controls a permanent with a
     * {@link PreventDamageFromChosenNameEffect} static effect whose chosen card name equals
     * {@code sourceName} (Gideon's Intervention). Damage from such a source to that player or to the
     * permanents they control is prevented (damage only — targeting and enchanting are unaffected).
     */
    public boolean isDamageFromChosenNamePreventedForController(GameData gameData, UUID controllerId, String sourceName) {
        if (sourceName == null || controllerId == null) {
            return false;
        }
        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        if (bf == null) {
            return false;
        }
        for (Permanent perm : bf) {
            if (sourceName.equals(perm.getChosenName())
                    && perm.getCard().getEffects(EffectSlot.STATIC).stream()
                            .anyMatch(PreventDamageFromChosenNameEffect.class::isInstance)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Energy Storm: returns {@code true} when any permanent has
     * {@link PreventDamageFromInstantAndSorcerySpellsEffect} and {@code entry} is an instant or
     * sorcery spell dealing damage as itself (not via a permanent that fights/bites). Combat and
     * ability damage are unaffected. Honours {@link #isDamagePreventable}.
     */
    public boolean isDamageFromInstantOrSorcerySpellPrevented(GameData gameData, StackEntry entry) {
        if (!isDamagePreventable(gameData) || entry == null) {
            return false;
        }
        StackEntryType type = entry.getEntryType();
        if (type != StackEntryType.INSTANT_SPELL && type != StackEntryType.SORCERY_SPELL) {
            return false;
        }
        return anyBattlefieldHasStaticEffect(gameData, PreventDamageFromInstantAndSorcerySpellsEffect.class);
    }

    /** Returns whether a spell's damage is prevented for the given controller or that controller's permanents. */
    public boolean isSpellDamageToControllerAndPermanentsPrevented(GameData gameData, StackEntry entry,
                                                                    UUID recipientControllerId) {
        if (!isDamagePreventable(gameData) || entry == null || recipientControllerId == null
                || !isSpellStackEntry(entry.getEntryType())) {
            return false;
        }
        return playerBattlefieldHasStaticEffect(gameData, recipientControllerId, SpellDamagePreventionEffect.class);
    }

    /** Returns the first turn-scoped damage-prevention shield matching the targeted spell. */
    public TargetSpellDamagePreventionShield getTargetSpellDamagePreventionShield(
            GameData gameData, StackEntry entry) {
        if (!isDamagePreventable(gameData) || entry == null) {
            return null;
        }
        StackEntryType type = entry.getEntryType();
        boolean isSpell = switch (type) {
            case CREATURE_SPELL, ENCHANTMENT_SPELL, SORCERY_SPELL, INSTANT_SPELL,
                    ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            case TRIGGERED_ABILITY, ACTIVATED_ABILITY -> false;
        };
        if (!isSpell || entry.getCard() == null) {
            return null;
        }
        UUID spellCardId = entry.getCard().getId();
        synchronized (gameData.targetSpellDamagePreventionShields) {
            return gameData.targetSpellDamagePreventionShields.stream()
                    .filter(shield -> spellCardId.equals(shield.spellCardId()))
                    .findFirst()
                    .orElse(null);
        }
    }

    /** Returns whether a targeted instant or sorcery spell has its damage prevented this turn. */
    public boolean isDamageFromTargetSpellPrevented(GameData gameData, StackEntry entry) {
        return getTargetSpellDamagePreventionShield(gameData, entry) != null;
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
        return target.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(TargetedSpellDamagePreventionEffect.class::isInstance)
                .map(TargetedSpellDamagePreventionEffect.class::cast)
                .anyMatch(effect -> effect.condition() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, target, effect.condition()));
    }

    /**
     * Benevolent Unicorn: total amount by which damage dealt by {@code entry} — a spell dealing
     * damage as itself — is reduced by {@link ReduceSpellDamageEffect} permanents on any
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
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ReduceSpellDamageEffect reduce) {
                        total[0] += reduce.amount();
                    }
                }
            }
        });
        return total[0];
    }

    /**
     * Returns the amount prevented from each damage event dealt by a spell as itself, or zero when
     * the event is not preventable spell damage.
     */
    public int getSpellDamagePrevention(GameData gameData, StackEntry entry) {
        if (!isDamagePreventable(gameData) || entry == null || entry.getSourcePermanentId() != null
                || !isSpellStackEntry(entry.getEntryType())) {
            return 0;
        }
        int[] total = {0};
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof PreventFixedDamageFromSpellsEffect prevention) {
                        total[0] += prevention.amount();
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
        if (damage <= 0) {
            return damage;
        }
        int[] result = {damage};
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ReplaceDamageAboveThresholdEffect replacement
                            && result[0] >= replacement.threshold()) {
                        result[0] = replacement.replacementDamage();
                    }
                }
            }
        });
        for (ReplaceDamageAboveThresholdThisTurnEffect replacement
                : List.copyOf(gameData.damageReplacementsThisTurn)) {
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
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OjerAxonilDamageReplacementEffect
                            && !permanent.isStaticEffectSuppressed(effect.getClass())) {
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
     * {@link AllowExtraLoyaltyActivationEffect}, allowing planeswalker loyalty abilities
     * to be activated twice per turn instead of once (Oath of Teferi).
     */
    public boolean hasExtraLoyaltyActivation(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, AllowExtraLoyaltyActivationEffect.class);
    }

    public boolean hasExtraBoastActivation(GameData gameData, UUID playerId) {
        return playerBattlefieldHasStaticEffect(gameData, playerId, AllowExtraBoastActivationEffect.class);
    }

    /** Returns whether a global static effect locks the given planeswalker's loyalty abilities. */
    public boolean isPlaneswalkerLoyaltyAbilityLocked(GameData gameData, Permanent permanent) {
        if (!isPlaneswalker(gameData, permanent)) {
            return false;
        }
        return gameData.anyPermanentMatches(source -> source.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(PlaneswalkerLoyaltyAbilitiesCantBeActivatedEffect.class::isInstance));
    }

    public boolean allowsInstantSpeedLoyaltyActivation(Permanent permanent) {
        return permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(AllowLoyaltyActivationAtInstantSpeedEffect.class::isInstance);
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
     * a {@link CreatureSpellsCantBeCounteredEffect} on the battlefield protects creature spells, or
     * a {@link ControllerSpellsCantBeCounteredEffect} protects spells controlled by the spell's controller,
     * or a {@link SpellsCantBeCounteredEffect} protects every spell.
     */
    public boolean isUncounterable(GameData gameData, Card card) {
        if (anyBattlefieldHasStaticEffect(gameData, SpellsAndAbilitiesCantBeCounteredEffect.class)) {
            return true;
        }
        for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantBeCounteredEffect cbc && isCantBeCounteredActive(gameData, card, cbc)) {
                return true;
            }
        }
        if (gameData.spellsMadeUncounterable.contains(card.getId())) {
            return true;
        }
        StackEntry stackEntry = gameData.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(card.getId())
                        && isSpellStackEntry(entry.getEntryType()))
                .findFirst()
                .orElse(null);
        if (stackEntry != null
                && gameData.playersSpellsCantBeCounteredThisTurn.contains(stackEntry.getControllerId())) {
            return true;
        }
        if (stackEntry != null
                && hasCardType(card, CardType.CREATURE)
                && gameData.playersCreatureSpellsCantBeCounteredThisTurn.contains(stackEntry.getControllerId())) {
            return true;
        }
        if (stackEntry != null && controllerSpellsCantBeCountered(gameData, stackEntry, card)) {
            return true;
        }
        if (anyBattlefieldHasStaticEffect(gameData, SpellsCantBeCounteredEffect.class)) {
            return true;
        }
        if (!hasCardType(card, CardType.CREATURE)) {
            return false;
        }
        if (anyBattlefieldHasStaticEffect(gameData, CreatureSpellsCantBeCounteredEffect.class)) {
            return true;
        }
        return controllerProtectsHighPowerCreatureSpell(gameData, card);
    }

    private boolean controllerSpellsCantBeCountered(GameData gameData, StackEntry stackEntry, Card card) {
        UUID controllerId = stackEntry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        boolean creatureSpell = hasCardType(card, CardType.CREATURE);
        int manaValue = card.getManaValue() + stackEntry.getXValue();
        return battlefield.stream()
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream())
                .filter(ControllerSpellsCantBeCounteredEffect.class::isInstance)
                .map(ControllerSpellsCantBeCounteredEffect.class::cast)
                .anyMatch(effect -> (!effect.noncreatureOnly() || !creatureSpell)
                        && (effect.cardTypes().isEmpty()
                        || effect.cardTypes().stream().anyMatch(card::hasType))
                        && (effect.minimumManaValue() == null
                        || manaValue >= effect.minimumManaValue()));
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
     * Returns {@code true} if the given creature spell's controller has a permanent whose
     * {@link ControllerCreatureSpellsCantBeCounteredEffect} protects it — i.e. the spell's power is
     * at least that effect's threshold (Spellbreaker Behemoth: "Creature spells you control with
     * power 5 or greater can't be countered"). Read via {@code Class::isInstance}/{@code cast}, the
     * sanctioned way to poll static marker effects here.
     */
    private boolean controllerProtectsHighPowerCreatureSpell(GameData gameData, Card card) {
        Integer power = card.getPower();
        if (power == null) {
            return false;
        }
        StackEntry entry = findStackEntryByCardId(gameData, card.getId());
        if (entry == null) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return false;
        }
        return battlefield.stream()
                .flatMap(perm -> perm.getCard().getEffects(EffectSlot.STATIC).stream())
                .filter(ControllerCreatureSpellsCantBeCounteredEffect.class::isInstance)
                .map(ControllerCreatureSpellsCantBeCounteredEffect.class::cast)
                .anyMatch(effect -> effect.minimumPower() == null || power >= effect.minimumPower());
    }

    /**
     * An unconditional {@link CantBeCounteredEffect} always applies. A conditional one
     * (Banefire's "If X is 5 or more") applies only while its condition holds, evaluated against
     * the spell's stack entry — the only place a spell is ever the target of a counter.
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
     * Returns {@code true} if any permanent on the battlefield has a
     * {@link CreatureEnteringDontCauseTriggersEffect} static effect (e.g. Torpor Orb),
     * AND the entering card is a creature.
     */
    public boolean areCreatureETBTriggersSuppressed(GameData gameData, Card enteringCard) {
        if (!hasCardType(enteringCard, CardType.CREATURE)) {
            return false;
        }
        return anyBattlefieldHasStaticEffect(gameData, CreatureEnteringDontCauseTriggersEffect.class)
                || anyBattlefieldHasStaticEffect(gameData, ArtifactOrCreatureEnteringDontCauseTriggersEffect.class);
    }

    /**
     * Returns whether an entering artifact or creature is prevented from causing triggered abilities.
     */
    public boolean areArtifactOrCreatureETBTriggersSuppressed(GameData gameData, Card enteringCard) {
        if (!hasCardType(enteringCard, CardType.ARTIFACT)
                && !hasCardType(enteringCard, CardType.CREATURE)) {
            return false;
        }
        return anyBattlefieldHasStaticEffect(gameData, ArtifactOrCreatureEnteringDontCauseTriggersEffect.class)
                || areCreatureETBTriggersSuppressed(gameData, enteringCard);
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
        if (anyBattlefieldHasStaticEffect(gameData, CreatureDyingDontCauseTriggersEffect.class)) {
            return true;
        }
        return gameData.simultaneousDyingCreatures.values().stream()
                .anyMatch(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(CreatureDyingDontCauseTriggersEffect.class::isInstance));
    }

    /**
     * Returns whether entering permanents are prevented from causing triggered abilities of
     * permanents controlled by {@code abilityControllerId} to trigger by an opposing static
     * effect such as Elesh Norn, Mother of Machines.
     */
    public boolean areOpponentPermanentETBTriggersSuppressed(GameData gameData, UUID abilityControllerId) {
        if (gameData == null || abilityControllerId == null) {
            return false;
        }
        UUID opponentId = gameData.getOpponentId(abilityControllerId);
        return opponentId != null
                && playerBattlefieldHasStaticEffect(
                gameData, opponentId, OpponentPermanentsEnteringDontCauseTriggersEffect.class);
    }

    /**
     * Returns the number of extra times a triggered ability of a permanent controlled by
     * {@code sourceControllerId} should fire when {@code enteringPermanent} enters.
     */
    public int countETBExtraTriggers(GameData gameData, UUID sourceControllerId,
                                     UUID enteringControllerId, Card enteringPermanent) {
        List<Permanent> bf = gameData.playerBattlefields.get(sourceControllerId);
        if (bf == null) return 0;
        int count = 0;
        for (Permanent perm : bf) {
            for (CardEffect e : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (e instanceof ETBDoubleTriggerEffect etb
                        && (!etb.requiresEnteringControllerMatch() || sourceControllerId.equals(enteringControllerId))
                        && predicateEvaluationService.matchesCardPredicate(enteringPermanent, etb.predicate(), null)) {
                    count++;
                }
            }
        }
        return count;
    }

    public int countETBExtraTriggers(GameData gameData, UUID controllerId, Card enteringPermanent) {
        return countETBExtraTriggers(gameData, controllerId, controllerId, enteringPermanent);
    }

    public int countETBExtraTriggersForAnyPermanent(GameData gameData, UUID sourceControllerId) {
        List<Permanent> bf = gameData.playerBattlefields.get(sourceControllerId);
        if (bf == null) return 0;
        int count = 0;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ETBDoubleTriggerEffect etb
                        && !etb.requiresEnteringControllerMatch()) {
                    count++;
                }
            }
        }
        return count;
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
     * Returns the number of additional copies for a triggered ability sourced by a permanent,
     * optionally restricting matching static effects to triggers directly caused by an attack.
     */
    public int countAdditionalTriggeredAbilityTriggers(GameData gameData, UUID controllerId,
                                                        Permanent triggeringPermanent,
                                                        boolean attackTrigger) {
        if (controllerId == null || triggeringPermanent == null) return 0;
        int count = 0;
        for (UUID staticControllerId : gameData.playerBattlefields.keySet()) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(staticControllerId);
            if (battlefield == null) continue;
            for (Permanent staticSource : battlefield) {
                if (staticSource.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                for (CardEffect effect : staticSource.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof AdditionalTriggeredAbilityEffect additional)
                            || (additional.attackOnly() && !attackTrigger)
                            || (!additional.allControllers() && !staticControllerId.equals(controllerId))
                            || (!additional.includeSourcePermanent()
                            && staticSource.getId().equals(triggeringPermanent.getId()))) {
                        continue;
                    }
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceControllerId(staticControllerId)
                            .withSourcePermanentId(staticSource.getId())
                            .withSourcePermanentSnapshot(staticSource);
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
        if (staticSource.isLosesAllAbilitiesUntilEndOfTurn()
                || staticSource.isAuraEffectsIgnoredThisTurn()) return 0;

        int count = 0;
        for (CardEffect effect : staticSource.getCard().getEffects(EffectSlot.STATIC)) {
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
        if (staticSource.isLosesAllAbilitiesUntilEndOfTurn()
                || staticSource.isAuraEffectsIgnoredThisTurn()
                || !staticSource.isAttached()) {
            return 0;
        }

        Permanent equippedCreature = findPermanentById(gameData, staticSource.getAttachedTo());
        if (equippedCreature == null) {
            equippedCreature = gameData.simultaneousDyingCreatures.get(staticSource.getAttachedTo());
        }
        if (equippedCreature == null || !isCreature(gameData, equippedCreature)) return 0;

        UUID equippedCreatureController = gameData.findControllerOf(equippedCreature);
        if (equippedCreatureController == null) {
            equippedCreatureController = gameData.simultaneousDyingControllers.get(equippedCreature.getId());
        }
        if (!controllerId.equals(equippedCreatureController)) return 0;

        int count = 0;
        for (CardEffect effect : staticSource.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof AdditionalCreatureDeathTriggerEffect additional
                    && additional.includeOwnedEmblemTriggers()) {
                count++;
            }
        }
        return count;
    }

    // --- Aura & enchantment ---

    /**
     * Returns {@code true} if any permanent on the battlefield has an
     * {@link AnimateNoncreatureArtifactsEffect}, which turns all non-creature artifacts
     * into creatures.
     */
    private boolean hasAnimateArtifactEffect(GameData gameData) {
        return anyBattlefieldHasStaticEffect(gameData, AnimateNoncreatureArtifactsEffect.class);
    }

    /**
     * Returns {@code true} if the permanent is one of the "each other non-Aura enchantment you
     * control" permanents an {@link AnimateControlledEnchantmentsEffect} (Starfield of Nyx) turns
     * into a creature — the effect's source must be on the same battlefield and its controller must
     * control enough enchantments.
     */
    public boolean isAnimatedByStarfield(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            int enchantmentCount = -1;
            for (Permanent source : battlefield) {
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
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
     * Forest) animates only lands carrying that subtype.
     */
    private boolean matchesAnimateLand(GameData gameData, Permanent permanent) {
        return gameData.anyPermanentMatches(source ->
                source.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof AllLandsAreCreaturesEffect animateLands
                                && (animateLands.scope() == GrantScope.ALL_LANDS
                                        || (animateLands.scope() == GrantScope.OWN_LANDS
                                                && sourceControlsPermanent(gameData, source, permanent))
                                        || (animateLands.scope() == GrantScope.OPPONENT_LANDS
                                                && !sourceControlsPermanent(gameData, source, permanent)))
                                && (animateLands.requiredSubtype() == null
                                        || permanent.getCard().getSubtypes().contains(animateLands.requiredSubtype()))));
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
        maxBlockers = Math.min(maxBlockers,
                maxBlockersFromStaticEffects(gameData, attacker, attackerControllerId));
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
                if (floating.effect() instanceof CanBeBlockedByAtMostNCreaturesEffect restriction) {
                    maxBlockers = Math.min(maxBlockers, restriction.maxBlockers());
                }
            }
        }
        return maxBlockers;
    }

    private int maxBlockersFromStaticEffects(GameData gameData, Permanent source, UUID sourceControllerId) {
        int maxBlockers = Integer.MAX_VALUE;
        for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
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

    /**
     * Predicate-matching form of {@link #hasAuraWithEffect(GameData, Permanent, Class)}, for effects
     * where the class alone is not the question — a parameterized restriction such as
     * {@link EnchantedCreatureCantAttackOrBlockEffect} must also be asked which half of it applies.
     * The matcher sees only effects that are actually active, so it never has to repeat the
     * attachment, ignored-Aura or {@link EnchantedPermanentConditionalEffect} handling.
     */
    public boolean hasAuraWithEffect(GameData gameData, Permanent creature, Predicate<CardEffect> effectMatcher) {
        return gameData.anyPermanentMatches(p ->
                p.isAttached() && p.getAttachedTo().equals(creature.getId())
                        && !p.isAuraEffectsIgnoredThisTurn()
                        && p.getCard().getEffects(EffectSlot.STATIC).stream()
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
        gameData.forEachPermanent((id, permanent) -> {
            if (!permanent.isAttached() || !creature.getId().equals(permanent.getAttachedTo())
                    || permanent.isAuraEffectsIgnoredThisTurn()) {
                return;
            }
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
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
     * the attacker's this-turn flag, a static {@link MustBeBlockedByAllCreaturesEffect} on the
     * attacker (optionally filtered), or such an effect on an aura attached to the attacker.
     * A {@code null} {@code blockerFilter} forces every able creature; a non-null filter forces
     * only matching blockers (Talruum Piper: creatures with flying).
     */
    public boolean isRequiredToBlockByLure(GameData gameData, Permanent attacker, Permanent blocker) {
        if (attacker.isMustBeBlockedByAllThisTurn()) {
            return true;
        }
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof EnchantedPermanentConditionalEffect) {
                continue;
            }
            if (matchesLureBlockerFilter(gameData, attacker, attacker, blocker, effect)) {
                return true;
            }
        }
        return gameData.anyPermanentMatches(aura ->
                aura.isAttached() && attacker.getId().equals(aura.getAttachedTo())
                        && aura.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> matchesLureBlockerFilter(gameData, attacker, aura, blocker, e)));
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
        // is green") — same unwrap as hasAuraWithEffect / isActiveEffect.
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
     * Brainwash — {3}) plus every {@link AttackCostEffect} on the creature itself (e.g. Phyrexian
     * Marauder — {1} per +1/+1 counter).
     */
    public int getCreatureAttackTax(GameData gameData, Permanent creature) {
        int total = getEnchantedCreatureAttackTax(gameData, creature);
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof AttackCostEffect attackCost) {
                total += attackCost.attackCost(creature);
            }
        }
        return total;
    }

    /**
     * Returns the total additional generic mana the controller must pay to declare this creature as an
     * attacker, summed over every {@link CombatTaxKind#ATTACK} Aura attached to it
     * (e.g. Brainwash — {3}).
     */
    public int getEnchantedCreatureAttackTax(GameData gameData, Permanent creature) {
        return getEnchantedCreatureCombatTax(gameData, creature, CombatTaxKind.ATTACK);
    }

    /**
     * Returns the additional generic mana the defending player must pay for each creature they declare as a
     * blocker of this attacking creature, summed over every {@link CombatTaxKind#BE_BLOCKED_BY} Aura
     * attached to it (e.g. Awesome Presence — {3}).
     */
    public int getEnchantedCreatureBlockTax(GameData gameData, Permanent creature) {
        return getEnchantedCreatureCombatTax(gameData, creature, CombatTaxKind.BE_BLOCKED_BY);
    }

    /**
     * Returns the additional generic mana the enchanted creature's controller must pay for each
     * block declared by that creature, summed over every {@link CombatTaxKind#BLOCK_WITH} Aura
     * attached to it (e.g. Oppressive Rays — {3}).
     */
    public int getEnchantedCreatureBlockerTax(GameData gameData, Permanent creature) {
        return getEnchantedCreatureCombatTax(gameData, creature, CombatTaxKind.BLOCK_WITH);
    }

    /**
     * Total additional generic mana the defending player must pay to declare {@code blocker} as a
     * blocker of {@code attacker}: the Aura taxes on both creatures plus every
     * {@link BlockCostEffect} the blocker carries (Hipparion — {1} to block power 3 or greater).
     * The single source of truth for the mana part of a block's additional cost, shared by
     * {@code CombatBlockService.declareBlockers} and the AI so the two can never disagree about
     * whether a block is affordable.
     */
    public int getBlockManaTax(GameData gameData, Permanent blocker, Permanent attacker) {
        int attackerPower = getEffectivePower(gameData, attacker);
        int tax = getEnchantedCreatureBlockTax(gameData, attacker)
                + getEnchantedCreatureBlockerTax(gameData, blocker);
        for (CardEffect effect : blocker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof BlockCostEffect blockCost) {
                tax += blockCost.blockCost(blocker, attackerPower);
            }
        }
        return tax;
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
            for (CardEffect effect : aura.getCard().getEffects(EffectSlot.STATIC)) {
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
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
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
     * ({@link RequirePaymentToBlockEffect}, e.g. Archangel of Tithes while attacking).
     * Charged once per blocking creature regardless of how many attackers it blocks.
     */
    public int getGlobalBlockManaTax(GameData gameData, Permanent blocker) {
        int[] total = {0};
        gameData.forEachPermanent((playerId, source) -> {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof RequirePaymentToBlockEffect tax
                        && (tax.activeCondition() == null || conditionEvaluationService.isMet(
                                gameData, tax.activeCondition(), ConditionContext.forPermanent(source, playerId)))) {
                    total[0] += tax.amountPerBlocker();
                }
            }
        });
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floatingEffect : gameData.floatingEffects) {
                if (floatingEffect.effect() instanceof GlobalBlockCostEffect tax) {
                    total[0] += tax.blockCostPerCreature();
                }
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
     * (which would cause infinite recursion). Checks base subtypes, transient subtypes,
     * granted subtypes, and the intrinsic Changeling keyword.
     */
    public static boolean permanentHasSubtype(Permanent permanent, CardSubtype subtype) {
        if (!NON_CREATURE_SUBTYPES.contains(subtype) && !permanent.getTransientCreatureTypeOverrides().isEmpty()) {
            return permanent.getTransientCreatureTypeOverrides().contains(subtype);
        }
        if (!NON_CREATURE_SUBTYPES.contains(subtype) && permanent.getTransientCreatureTypeOverride() != null) {
            return permanent.getTransientCreatureTypeOverride() == subtype;
        }
        // "Loses all creature types" (e.g. Amoeboid Changeling): every creature subtype is treated as absent.
        // hasKeyword already suppresses the Changeling grant while this flag is set.
        if (permanent.isLosesAllCreatureTypesUntilEndOfTurn() && !NON_CREATURE_SUBTYPES.contains(subtype)) {
            return false;
        }
        if (permanent.getTransientRemovedSubtypes().contains(subtype)) {
            return false;
        }
        return permanent.getCard().getSubtypes().contains(subtype)
                || permanent.getTransientSubtypes().contains(subtype)
                || permanent.getGrantedSubtypes().contains(subtype)
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
     * its intrinsic creature types.
     */
    public boolean shareCreatureType(GameData gameData, Permanent permanent, Card card) {
        boolean permanentChangeling = hasKeyword(gameData, permanent, Keyword.CHANGELING);
        boolean cardChangeling = card.hasKeyword(Keyword.CHANGELING);
        Set<CardSubtype> permanentTypes = effectiveCreatureSubtypes(gameData, permanent);
        Set<CardSubtype> cardTypes = card.getSubtypes().stream()
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
    public boolean sharesArtifactCreatureOrLandType(Permanent a, Permanent b) {
        Card aCard = a.getCard();
        Card bCard = b.getCard();
        return (aCard.hasType(CardType.ARTIFACT) && bCard.hasType(CardType.ARTIFACT))
                || (aCard.hasType(CardType.CREATURE) && bCard.hasType(CardType.CREATURE))
                || (aCard.hasType(CardType.LAND) && bCard.hasType(CardType.LAND));
    }

    /**
     * Returns {@code true} if the two permanents share at least one of the card types artifact or
     * creature (Legerdemain's "another target permanent that shares one of those types with it",
     * where "those types" are only artifact and creature). Uses each permanent's card types.
     */
    public boolean sharesArtifactOrCreatureType(Permanent a, Permanent b) {
        Card aCard = a.getCard();
        Card bCard = b.getCard();
        return (aCard.hasType(CardType.ARTIFACT) && bCard.hasType(CardType.ARTIFACT))
                || (aCard.hasType(CardType.CREATURE) && bCard.hasType(CardType.CREATURE));
    }

    /** Returns whether two permanents share at least one card type. */
    public boolean sharesCardType(Permanent a, Permanent b) {
        Card aCard = a.getCard();
        Card bCard = b.getCard();
        for (CardType cardType : CardType.values()) {
            if (aCard.hasType(cardType) && bCard.hasType(cardType)) {
                return true;
            }
        }
        return false;
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

    /** Effective creature subtypes of a permanent (named types only; Changeling handled separately). */
    public Set<CardSubtype> effectiveCreatureSubtypes(GameData gameData, Permanent permanent) {
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
        StaticBonus bonus = computeStaticBonus(gameData, permanent);
        if (!bonus.subtypeOverriding()) {
            addCreatureSubtypes(result, permanent.getCard().getSubtypes());
        }
        addCreatureSubtypes(result, permanent.getTransientSubtypes());
        addCreatureSubtypes(result, permanent.getGrantedSubtypes());
        addCreatureSubtypes(result, permanent.getUntilNextTurnSubtypes());
        addCreatureSubtypes(result, bonus.grantedSubtypes());
        return result;
    }

    /** Returns whether the permanent currently has the Flagbearer creature subtype. */
    public boolean isFlagbearer(GameData gameData, Permanent permanent) {
        return effectiveCreatureSubtypes(gameData, permanent).contains(CardSubtype.FLAGBEARER);
    }

    /** Returns whether an opponent of {@code playerId} controls a Flagbearer. */
    public boolean hasFlagbearerControlledByOpponent(GameData gameData, UUID playerId) {
        for (Map.Entry<UUID, List<Permanent>> entry : gameData.playerBattlefields.entrySet()) {
            if (!entry.getKey().equals(playerId)
                    && entry.getValue().stream().anyMatch(permanent -> isFlagbearer(gameData, permanent))) {
                return true;
            }
        }
        return false;
    }

    private void addCreatureSubtypes(Set<CardSubtype> target, List<CardSubtype> subtypes) {
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
                p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                        && p.isAttached() && p.getAttachedTo().equals(creature.getId()));
    }

    /**
     * Returns {@code true} if the given permanent has at least one aura attached to it.
     */
    public boolean isEnchanted(GameData gameData, Permanent creature) {
        return gameData.anyPermanentMatches(p ->
                p.isAttached() && p.getAttachedTo().equals(creature.getId())
                        && p.getCard().isAura());
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
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
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
     * or {@code null} if none is found.
     */
    public Permanent findControlledPermanentWithStaticEffect(GameData gameData, UUID playerId, Class<? extends CardEffect> effectClass) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return null;
        for (Permanent p : bf) {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
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
     * Returns the global damage multiplier based on {@link GlobalDamageMultiplyingEffect} permanents
     * on the battlefield (e.g. Furnace of Rath). Each instance multiplies by its factor, and multiple
     * instances stack multiplicatively (e.g. two Furnaces = 4x damage).
     */
    private int getDamageMultiplier(GameData gameData) {
        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, p) -> {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GlobalDamageMultiplyingEffect multiplyingEffect) {
                    multiplier[0] *= multiplyingEffect.damageMultiplierFactor();
                }
            }
        });
        return multiplier[0];
    }

    /**
     * Returns the damage multiplier that applies to damage dealt to a specific player based on
     * {@link DoubleDamageToEnchantedPlayerEffect} Curse Auras enchanting that player (e.g. Curse
     * of Bloodletting). Each Curse enchanting the player doubles the multiplier; multiple instances
     * stack multiplicatively. Returns {@code 1} when no such Curse enchants the player.
     */
    public int getEnchantedPlayerDamageMultiplier(GameData gameData, UUID playerId) {
        int[] multiplier = {1};
        gameData.forEachPermanent((controllerId, p) -> {
            if (!p.isAttached() || !playerId.equals(p.getAttachedTo())) return;
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof DoubleDamageToEnchantedPlayerEffect) {
                    multiplier[0] *= 2;
                }
            }
        });
        return multiplier[0];
    }

    /**
     * Returns the damage multiplier that applies to damage dealt to {@code recipientPlayerId} or to a
     * permanent that player controls, based on {@link DoubleDamageToOpponentsAndTheirPermanentsEffect}
     * permanents controlled by that player's opponents (Gisela, Blade of Goldnight). Multiple instances
     * stack multiplicatively. Returns {@code 1} when no such permanent is on the battlefield.
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

    private int getDamageToRecipientMultiplier(GameData gameData, UUID recipientPlayerId,
                                               UUID sourceControllerId, UUID recipientPermanentId,
                                               boolean combatDamage) {
        if (recipientPlayerId == null) return 1;

        int[] multiplier = {1};
        for (DelayedDamageDoubling doubling : gameData.getDelayedActions(DelayedDamageDoubling.class)) {
            if (recipientPlayerId.equals(doubling.targetPlayerId())) {
                multiplier[0] *= 2;
            }
        }
        gameData.forEachPermanent((controllerId, p) -> {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof DoubleDamageToControllerAndSelfEffect
                        && ((recipientPermanentId == null && recipientPlayerId.equals(controllerId))
                        || p.getId().equals(recipientPermanentId))) {
                    multiplier[0] *= 2;
                } else if (!recipientPlayerId.equals(controllerId)
                        && effect instanceof DoubleDamageToOpponentsAndTheirPermanentsEffect) {
                    multiplier[0] *= 2;
                } else if (!recipientPlayerId.equals(controllerId)
                        && sourceControllerId != null
                        && sourceControllerId.equals(controllerId)
                        && effect instanceof ControllerRecipientDamageMultiplyingEffect multiplyingEffect) {
                    if (!combatDamage || !multiplyingEffect.noncombatOnly()) {
                        multiplier[0] *= multiplyingEffect.damageMultiplier();
                    }
                }
            }
        });
        return multiplier[0];
    }

    /**
     * Returns the additive damage bonus from red or artifact sources controlled by the given
     * player when they would damage the given opponent. The bonus is summed across matching static
     * effects controlled by that source's controller.
     */
    public int getAdditionalDamageToOpponentsBonus(GameData gameData, UUID sourceControllerId,
                                                   Card sourceCard, Permanent sourcePermanent,
                                                   UUID recipientPlayerId) {
        if (sourceControllerId == null || recipientPlayerId == null
                || sourceControllerId.equals(recipientPlayerId)) {
            return 0;
        }

        Set<CardColor> sourceColors;
        boolean artifactSource;
        if (sourcePermanent != null) {
            sourceColors = getDamageSourceColors(gameData, getEffectiveColors(gameData, sourcePermanent));
            artifactSource = isArtifact(gameData, sourcePermanent);
        } else if (sourceCard != null) {
            sourceColors = getDamageSourceColors(gameData, getEffectiveCardColors(gameData, sourceCard));
            artifactSource = sourceCard.hasType(CardType.ARTIFACT);
        } else {
            return 0;
        }
        Set<CardColor> effectiveSourceColors = sourceColors;
        int[] bonus = {0};
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!sourceControllerId.equals(controllerId)) return;
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof SourceOpponentDamageBonusEffect additional
                        && additional.appliesTo(effectiveSourceColors, artifactSource)) {
                    bonus[0] += additional.amount();
                }
            }
        });
        return bonus[0];
    }

    /**
     * Returns the additive damage bonus from static effects controlled by {@code sourceControllerId}
     * when that controller's source would deal damage to {@code recipientPlayerId}.
     */
    public int getControllerDamageToOpponentBonus(GameData gameData, UUID sourceControllerId,
                                                   UUID recipientPlayerId) {
        if (sourceControllerId == null || recipientPlayerId == null
                || sourceControllerId.equals(recipientPlayerId)) {
            return 0;
        }

        int[] bonus = {0};
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!sourceControllerId.equals(controllerId)) return;
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                bonus[0] += getControllerDamageToOpponentBonus(gameData, effect, permanent, controllerId);
            }
        });
        return bonus[0];
    }

    /**
     * Returns the additive damage bonus from static effects that apply to spell damage dealt to an
     * opponent of the effect controller or to a permanent that opponent controls.
     */
    public int getAdditionalSpellDamageToOpponentsBonus(GameData gameData, StackEntry entry,
                                                        UUID recipientControllerId) {
        if (entry == null || recipientControllerId == null || !isSpellStackEntry(entry.getEntryType())) {
            return 0;
        }

        int[] bonus = {0};
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (recipientControllerId.equals(controllerId)) return;
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof OpponentDamageBonusEffect damageBonus) {
                    bonus[0] += damageBonus.amount();
                }
            }
        });
        return bonus[0];
    }

    private int getControllerDamageToOpponentBonus(GameData gameData, CardEffect effect,
                                                     Permanent source, UUID controllerId) {
        if (effect instanceof ControllerOpponentDamageBonusEffect damageBonus) {
            return damageBonus.amount();
        }
        if (effect instanceof ConditionalEffect conditional
                && conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forStaticEffect(source, controllerId))) {
            return getControllerDamageToOpponentBonus(gameData, conditional.wrapped(), source, controllerId);
        }
        return 0;
    }

    /**
     * Returns the flat bonus added to damage a source with any of {@code sourceColors} would deal to a
     * player, from {@link AdditionalDamageToPlayersFromColorSourcesEffect} permanents anywhere on the
     * battlefield (Tok-Tok, Volcano Born). Multiple instances stack additively. Returns {@code 0} when
     * no such permanent is on the battlefield or the source shares none of their colours.
     *
     * <p>Source-colour scoped, so it is applied at the two player damage entry points, where the source
     * colours have already been resolved through {@link #getDamageSourceColor} (Ghostly Flame).
     */
    public int getDamageToPlayerColorSourceBonus(GameData gameData, Set<CardColor> sourceColors) {
        if (sourceColors == null || sourceColors.isEmpty()) return 0;

        int[] bonus = {0};
        gameData.forEachPermanent((controllerId, p) -> {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AdditionalDamageToPlayersFromColorSourcesEffect e
                        && !Collections.disjoint(e.colors(), sourceColors)) {
                    bonus[0] += e.amount();
                }
            }
        });
        return bonus[0];
    }

    /**
     * True if {@code playerId} is enchanted by a Curse carrying
     * {@link EnchantedPlayerCantActivateNonManaNonLoyaltyAbilitiesEffect} (Overwhelming Splendor):
     * that player may activate only mana abilities and loyalty abilities.
     */
    public boolean playerCantActivateNonManaOrLoyaltyAbilities(GameData gameData, UUID playerId) {
        return gameData.anyPermanentMatches(p ->
                p.isAttached() && playerId.equals(p.getAttachedTo())
                        && p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof EnchantedPlayerCantActivateNonManaNonLoyaltyAbilitiesEffect));
    }

    /**
     * True if {@code playerId} is locked out by an opponent's
     * {@link OpponentsCantCastOrActivateDuringYourTurnEffect} (Grand Abolisher): that opponent
     * controls the source and it is currently their turn, so {@code playerId} can neither cast
     * spells nor activate abilities of artifacts, creatures or enchantments.
     */
    public boolean isLockedOutByOpponentsTurnRestriction(GameData gameData, UUID playerId) {
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null || activePlayerId.equals(playerId)) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return false;
        return battlefield.stream().anyMatch(p -> p.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(OpponentsCantCastOrActivateDuringYourTurnEffect.class::isInstance));
    }

    /**
     * True if {@code playerId} is locked out by the activated-ability half of a controller's
     * turn restriction.
     */
    public boolean isLockedOutByOpponentsTurnAbilityRestriction(GameData gameData, UUID playerId) {
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null || activePlayerId.equals(playerId)) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return false;
        return battlefield.stream().anyMatch(p -> p.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(effect -> effect instanceof OpponentsCantCastOrActivateDuringYourTurnEffect restriction
                        && restriction.restrictsActivatedAbilities()));
    }

    /**
     * True if {@code playerId} is an opponent of a permanent with a
     * {@link OpponentsCanCastSpellsOnlyAtSorcerySpeedEffect} static effect.
     */
    public boolean isLockedOutByOpponentsSorceryTimingRestriction(GameData gameData, UUID playerId) {
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(playerId)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;
            if (battlefield.stream().anyMatch(p -> p.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(OpponentsCanCastSpellsOnlyAtSorcerySpeedEffect.class::isInstance))) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if {@code playerId} cannot cast spells or activate abilities because a
     * {@link PlayersCanCastAndActivateOnlyDuringOwnTurnEffect} (City of Solitude) is on the
     * battlefield and it is not currently that player's turn. Mana abilities are included.
     */
    public boolean isLockedOutByOwnTurnOnlyRestriction(GameData gameData, UUID playerId) {
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null || activePlayerId.equals(playerId)) return false;
        return anyBattlefieldHasStaticEffect(gameData, PlayersCanCastAndActivateOnlyDuringOwnTurnEffect.class);
    }

    /**
     * True if {@code playerId} cannot cast spells because a
     * {@link PlayersCanCastSpellsOnlyDuringOwnTurnEffect} (Dosan the Falling Leaf) is on the
     * battlefield and it is not currently that player's turn. Activated abilities are unaffected.
     */
    public boolean isLockedOutByOwnTurnOnlySpellRestriction(GameData gameData, UUID playerId) {
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null || activePlayerId.equals(playerId)) return false;
        return anyBattlefieldHasStaticEffect(gameData, PlayersCanCastSpellsOnlyDuringOwnTurnEffect.class)
                || playerBattlefieldHasStaticEffect(gameData, playerId,
                ControllerCanCastSpellsOnlyDuringOwnTurnEffect.class);
    }

    /**
     * True while a {@link PlayersCantCastInstantsOrActivateNonManaAbilitiesDuringCombatEffect}
     * (Hand to Hand) is on the battlefield and the game is currently in a combat step: no player
     * may cast instant spells or activate abilities that aren't mana abilities.
     */
    public boolean isCombatActionLockActive(GameData gameData) {
        return gameData.currentStep != null && gameData.currentStep.isCombatPhase()
                && anyBattlefieldHasStaticEffect(gameData,
                PlayersCantCastInstantsOrActivateNonManaAbilitiesDuringCombatEffect.class);
    }

    /**
     * Applies the global damage multiplier to the given damage amount.
     *
     * @return the damage after applying all {@link GlobalDamageMultiplyingEffect} multipliers
     */
    public int applyDamageMultiplier(GameData gameData, int damage) {
        return damage * getDamageMultiplier(gameData);
    }

    /**
     * Returns the token creation multiplier for a specific player based on
     * {@link MultiplyTokenCreationEffect} permanents they control.
     * Unlike {@link #getDamageMultiplier} which is global, this is controller-specific:
     * only permanents controlled by the given player contribute to the multiplier.
     * Multiple instances stack multiplicatively (e.g. two Parallel Lives = 4x tokens).
     */
    public int getTokenMultiplier(GameData gameData, UUID controllerId) {
        return getTokenMultiplier(gameData, controllerId, false);
    }

    /** Returns the token creation multiplier, optionally restricting it to creature tokens. */
    public int getTokenMultiplier(GameData gameData, UUID controllerId, boolean creatureToken) {
        UUID effectiveControllerId = resolveTokenCreationController(gameData, controllerId, creatureToken);
        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, p) -> {
            if (!playerId.equals(effectiveControllerId)) return;
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof MultiplyTokenCreationEffect mtce
                        && (!mtce.creatureTokensOnly() || creatureToken)) {
                    multiplier[0] *= mtce.multiplier();
                }
            }
        });
        return multiplier[0];
    }

    /** Resolves control-changing replacement effects before a token is created. */
    public UUID resolveTokenCreationController(GameData gameData, UUID controllerId, boolean creatureToken) {
        UUID effectiveControllerId = controllerId;
        Set<UUID> usedGatherers = new HashSet<>();
        Set<UUID> usedTokenGatherers = new HashSet<>();
        boolean changed;
        do {
            changed = false;
            if (creatureToken) {
                for (UUID gatherer : gameData.orderedPlayerIds) {
                    if (!gatherer.equals(effectiveControllerId)
                            && gameData.playersGatheringSpecimensThisTurn.contains(gatherer)
                            && usedGatherers.add(gatherer)) {
                        effectiveControllerId = gatherer;
                        changed = true;
                        break;
                    }
                }
                if (changed) {
                    continue;
                }
            }
            for (UUID gatherer : gameData.orderedPlayerIds) {
                if (!gatherer.equals(effectiveControllerId)
                        && gameData.playersGatheringTokensThisTurn.contains(gatherer)
                        && usedTokenGatherers.add(gatherer)) {
                    effectiveControllerId = gatherer;
                    changed = true;
                    break;
                }
            }
        } while (changed);
        return effectiveControllerId;
    }

    /**
     * Applies the global damage multiplier and per-controller damage multipliers
     * (e.g. {@link DoubleControllerDamageEffect}) to the given damage amount.
     *
     * @param entry the stack entry representing the damage source; used to check controller
     *              and to evaluate stack-entry predicates on controller damage effects
     * @return the damage after applying all multipliers
     */
    public int applyDamageMultiplier(GameData gameData, int damage, StackEntry entry) {
        int bonus = 0;
        if (damage > 0 && entry != null) {
            bonus = getColorSourceDamageBonus(gameData, entry.getControllerId(), entry.getCard().getColors())
                    + getColorSourcePermanentDamageBonus(gameData, entry.getControllerId(),
                            entry.getEffectiveDamageSourceCard().getColors(), entry.getSourcePermanentId())
                    + getControllerDamageBonus(gameData, entry)
                    + getSpellDamageBonus(gameData, entry);
        }
        UUID controllerId = entry != null ? entry.getControllerId() : null;
        Permanent source = entry != null && entry.getSourcePermanentId() != null
                ? findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source != null) {
            UUID sourceControllerId = findPermanentController(gameData, source.getId());
            if (sourceControllerId != null) controllerId = sourceControllerId;
        }
        return (damage + bonus) * getDamageMultiplier(gameData)
                * getControllerDamageMultiplier(gameData, controllerId, entry, false, source)
                * getPermanentDamageMultiplier(gameData, entry != null ? entry.getSourcePermanentId() : null);
    }

    /**
     * Returns the additive damage bonus from static effects that modify damage dealt by matching
     * colored spells. These effects are global to the game and therefore do not depend on either
     * the spell's controller or the effect's controller.
     */
    int getSpellDamageBonus(GameData gameData, StackEntry entry) {
        if (entry == null || entry.getCard() == null || !isSpellEntry(entry.getEntryType())) {
            return 0;
        }

        Set<CardColor> spellColors = EnumSet.noneOf(CardColor.class);
        spellColors.addAll(entry.getCard().getColors());
        if (entry.getCard().getColor() != null) {
            spellColors.add(entry.getCard().getColor());
        }
        if (spellColors.isEmpty()) {
            return 0;
        }

        int[] bonus = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof SpellDamageBonusEffect spellDamageBonus
                        && spellDamageBonus.colors().stream().anyMatch(spellColors::contains)) {
                    bonus[0] += spellDamageBonus.amount();
                }
            }
        });
        return bonus[0];
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
        if (entry == null) return 0;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) return 0;

        int[] bonus = {0};
        gameData.forEachPermanent((playerId, p) -> {
            if (!playerId.equals(controllerId)) return;
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AdditionalControllerDamageEffect acde) {
                    if (acde.stackFilter() == null
                            || predicateEvaluationService.matchesStackEntryPredicate(entry, acde.stackFilter(), null)) {
                        bonus[0] += acde.amount();
                    }
                }
            }
        });
        return bonus[0];
    }

    public int getNoncreatureSourceDamageBonus(GameData gameData, StackEntry entry,
                                                UUID recipientPlayerId, Permanent recipientPermanent) {
        if (entry == null || entry.getControllerId() == null) {
            return 0;
        }
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null : findPermanentById(gameData, entry.getSourcePermanentId());
        boolean creatureSource = sourcePermanent != null
                ? isCreature(gameData, sourcePermanent)
                : entry.getEffectiveDamageSourceCard() != null
                && entry.getEffectiveDamageSourceCard().hasType(CardType.CREATURE);
        if (creatureSource) {
            return 0;
        }
        boolean eligibleRecipient = recipientPermanent != null
                ? isCreature(gameData, recipientPermanent) || isBattle(gameData, recipientPermanent)
                : recipientPlayerId != null && !entry.getControllerId().equals(recipientPlayerId);
        if (!eligibleRecipient) {
            return 0;
        }

        int[] bonus = {0};
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!controllerId.equals(entry.getControllerId())) {
                return;
            }
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof NoncreatureSourceDamageBonusEffect damageBonus) {
                    bonus[0] += damageBonus.amount();
                }
            }
        });
        return bonus[0];
    }

    public int getPlayersAndBattlesDamageBonus(GameData gameData, boolean playerRecipient,
                                                Permanent recipientPermanent) {
        if (!playerRecipient
                && (recipientPermanent == null || !isBattle(gameData, recipientPermanent))) {
            return 0;
        }
        synchronized (gameData.floatingEffects) {
            return gameData.floatingEffects.stream()
                    .map(FloatingContinuousEffect::effect)
                    .filter(DamageToPlayersAndBattlesBonusEffect.class::isInstance)
                    .map(DamageToPlayersAndBattlesBonusEffect.class::cast)
                    .mapToInt(DamageToPlayersAndBattlesBonusEffect::amount)
                    .sum();
        }
    }

    /**
     * Returns the per-controller damage multiplier based on {@link DoubleControllerDamageEffect}
     * permanents on the battlefield and turn-scoped {@code controllerDamageDoublingsThisTurn}
     * (Insult). Only applies when the source is controlled by the same player who controls the
     * permanent with the effect / who resolved the turn-scoped effect.
     *
     * <p>Each static effect has a {@code stackFilter} predicate and an {@code appliesToCombatDamage} flag.
     * For stack-based damage, the effect applies if the filter is {@code null} (matches all) or if
     * the entry matches the filter. For combat damage ({@code isCombat=true}), the effect applies
     * only if {@code appliesToCombatDamage} is {@code true}. Turn-scoped doublings apply to both
     * combat and noncombat damage.
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
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ControllerDamageMultiplyingEffect multiplyingEffect) {
                    if (isCombat) {
                        if (multiplyingEffect.appliesToCombatDamage()) {
                            multiplier[0] *= multiplyingEffect.damageMultiplier();
                        }
                    } else if (entry != null) {
                        if (multiplyingEffect.stackFilter() == null
                                || predicateEvaluationService.matchesStackEntryPredicate(
                                entry, multiplyingEffect.stackFilter(), null)) {
                            multiplier[0] *= multiplyingEffect.damageMultiplier();
                        }
                    }
                } else if (!isCombat && entry != null && damageSource == null
                        && effect instanceof SourceDamageMultiplyingEffect multiplyingEffect
                        && multiplyingEffect.matchesStackEntrySource(entry, p)) {
                    multiplier[0] *= multiplyingEffect.damageMultiplier();
                }
            }
        });
        if (damageSource != null && controllerId.equals(findPermanentController(gameData, damageSource.getId()))) {
            multiplier[0] *= getSourceDamageMultiplier(gameData, controllerId, damageSource);
        }
        // Insult: "if a source you control would deal damage this turn, it deals double instead"
        int turnDoublings = gameData.controllerDamageDoublingsThisTurn.getOrDefault(controllerId, 0);
        for (int i = 0; i < turnDoublings; i++) {
            multiplier[0] *= 2;
        }
        return multiplier[0];
    }

    public int getSourceDamageMultiplier(GameData gameData, UUID controllerId, Permanent damageSource) {
        return getSourceDamageMultiplier(gameData, controllerId, damageSource, false, null);
    }

    private int getSourceDamageMultiplier(GameData gameData, UUID controllerId, Permanent damageSource,
                                          boolean combatDamage, Permanent combatDamageTarget) {
        if (controllerId == null || damageSource == null
                || !controllerId.equals(findPermanentController(gameData, damageSource.getId()))) {
            return 1;
        }

        int[] multiplier = {1};
        gameData.forEachPermanent((playerId, effectSource) -> {
            if (!playerId.equals(controllerId)) return;
            FilterContext context = FilterContext.of(gameData)
                    .withSourceCardId(effectSource.getCard().getId())
                    .withSourceControllerId(controllerId)
                    .withSourcePermanentSnapshot(effectSource);
            for (CardEffect effect : effectSource.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof SourceDamageMultiplyingEffect multiplyingEffect
                        && predicateEvaluationService.matchesPermanentPredicate(
                        damageSource, multiplyingEffect.sourceFilter(), context)
                        && (combatDamage
                        ? multiplyingEffect.appliesToCombatDamageTarget(combatDamageTarget)
                        : multiplyingEffect.appliesToNonCombatDamage())) {
                    multiplier[0] *= multiplyingEffect.damageMultiplier();
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

        int doublings = gameData.permanentDamageDoublingsThisTurn.getOrDefault(permanentId, 0);
        int multiplier = 1;
        for (int i = 0; i < doublings; i++) {
            multiplier *= 2;
        }
        return multiplier;
    }

    /**
     * Returns {@code true} if the given stack entry represents an instant or sorcery spell
     * that should have lifelink due to a {@link GrantLifelinkToControllerSpellsByColorEffect}
     * on the controller's battlefield. A non-null required color must match the spell's color;
     * a null required color matches every spell.
     */
    public boolean shouldControllerSpellHaveLifelink(GameData gameData, StackEntry entry) {
        if (entry == null) return false;
        StackEntryType type = entry.getEntryType();
        if (type != StackEntryType.INSTANT_SPELL && type != StackEntryType.SORCERY_SPELL) return false;
        if (sourceHasKeyword(gameData, entry, null, Keyword.LIFELINK)) return true;

        boolean[] hasLifelink = {false};
        gameData.forEachPermanent((playerId, p) -> {
            if (!playerId.equals(entry.getControllerId())) return;
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GrantLifelinkToControllerSpellsByColorEffect glse
                        && (glse.color() == null || entry.getCard().getColors().contains(glse.color()))) {
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
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof DoubleEquippedCreatureCombatDamageEffect) {
                        multiplier[0] *= 2;
                    }
                }
            }
        });
        return multiplier[0];
    }

    /**
     * Applies the global damage multiplier and creature-specific combat damage multipliers
     * to the given combat damage amount. The source multiplier doubles damage dealt by the
     * source creature, and the target multiplier doubles damage received by the target creature.
     *
     * @param source the creature dealing combat damage
     * @param target the creature receiving combat damage, or {@code null} if damage is to a player
     * @return the damage after applying all multipliers
     */
    public int applyCombatDamageMultiplier(GameData gameData, int damage, Permanent source, Permanent target) {
        int bonus = 0;
        UUID controllerId = findPermanentController(gameData, source.getId());
        if (damage > 0) {
            if (controllerId != null) {
                bonus = getColorSourceDamageBonus(gameData, controllerId, source.getCard().getColors())
                        + getColorSourcePermanentDamageBonus(gameData, controllerId,
                                source.getCard().getColors(), source.getId());
                if (target != null) {
                    UUID targetControllerId = findPermanentController(gameData, target.getId());
                    bonus += getAdditionalDamageToOpponentsBonus(
                            gameData, controllerId, source.getCard(), source, targetControllerId);
                }
            }
        }
        if (target != null) {
            bonus += getControllerDamageToOpponentBonus(gameData, controllerId,
                    findPermanentController(gameData, target.getId()));
        }
        int result = (damage + bonus) * getDamageMultiplier(gameData);
        result *= getControllerDamageMultiplier(gameData, controllerId, null, true);
        result *= getSourceDamageMultiplier(gameData, controllerId, source, true, target);
        result *= getPermanentDamageMultiplier(gameData, source.getId());
        result *= getEquippedCreatureCombatDamageMultiplier(gameData, source);
        if (target != null) {
            result *= getEquippedCreatureCombatDamageMultiplier(gameData, target);
            // Gisela, Blade of Goldnight: double the damage dealt to a permanent an opponent controls.
            result *= getDamageToRecipientMultiplier(
                    gameData, findPermanentController(gameData, target.getId()), controllerId,
                    target.getId(), true);
            for (int i = 0; i < gameData.combatDamageToCreaturesDoublingsThisTurn; i++) {
                result *= 2;
            }
        }
        return result;
    }

    /**
     * Returns the additive damage bonus for sources of matching color controlled by the given
     * player this turn (e.g. The Flame of Keld Chapter III). Returns the sum of all matching
     * color bonuses. Returns 0 if no bonus applies.
     */
    int getColorSourceDamageBonus(GameData gameData, UUID controllerId, List<CardColor> sourceColors) {
        if (controllerId == null || sourceColors == null || sourceColors.isEmpty()) {
            return 0;
        }
        Map<CardColor, Integer> colorMap = gameData.colorSourceDamageBonusThisTurn.get(controllerId);
        if (colorMap == null || colorMap.isEmpty()) {
            return 0;
        }
        int bonus = 0;
        for (CardColor color : sourceColors) {
            bonus += colorMap.getOrDefault(color, 0);
        }
        return bonus;
    }

    /** Returns the additive noncombat damage bonus for sources controlled by {@code controllerId}. */
    public int getControllerNoncombatDamageBonus(GameData gameData, UUID controllerId) {
        if (controllerId == null) {
            return 0;
        }
        return gameData.controllerNoncombatDamageBonusThisTurn.getOrDefault(controllerId, 0);
    }

    /**
     * Returns the additive damage bonus from {@link AdditionalColorSourceDamageEffect} permanents
     * controlled by the damage source's controller (e.g. Embermaw Hellion). The bonus applies to
     * any source of the matching color — spell, ability or combat damage — except the permanent
     * carrying the effect itself, which {@code sourcePermanentId} identifies ("another red source").
     * Multiple instances stack additively.
     */
    int getColorSourcePermanentDamageBonus(GameData gameData, UUID controllerId,
                                           List<CardColor> sourceColors, UUID sourcePermanentId) {
        if (controllerId == null || sourceColors == null || sourceColors.isEmpty()) {
            return 0;
        }
        int[] bonus = {0};
        gameData.forEachPermanent((playerId, p) -> {
            if (!playerId.equals(controllerId)) return;
            if (p.getId().equals(sourcePermanentId)) return;
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AdditionalColorSourceDamageEffect acsde
                        && sourceColors.contains(acsde.color())) {
                    bonus[0] += acsde.amount();
                }
            }
        });
        return bonus[0];
    }

    /**
     * Returns {@code true} if the given creature is prevented from dealing damage. This
     * can be caused by an attached aura with {@link PreventAllDamageToAndByEnchantedCreatureEffect},
     * a global color-based damage prevention effect, or a per-permanent damage prevention flag.
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
        if (!isDamagePreventable(gameData)) return false;
        boolean globalCreaturePrevention = isDamageByCreaturePrevented(gameData, creature)
                && gameData.damageByCreaturesPreventionLifeGainPlayers.isEmpty();
        if (globalCreaturePrevention
                || hasAuraWithEffect(gameData, creature, PreventAllDamageToAndByEnchantedCreatureEffect.class)
                || hasAuraWithEffect(gameData, creature,
                effect -> effect instanceof PreventAllDamageDealtByEnchantedCreatureEffect prevented
                        && (!prevented.combatOnly() || isCombatDamage))
                || gameData.isPreventedFromDealingDamage(creature.getId())) {
            return true;
        }
        if (isDamageFromPermanentSourcePrevented(gameData, creature)) return true;
        if (isCombatDamage && hasAuraWithEffect(gameData, creature, PreventAllCombatDamageToAndByEnchantedCreatureEffect.class)) {
            return true;
        }
        // Fog Bank: "Prevent all combat damage that would be dealt to and dealt by this creature."
        if (isCombatDamage && creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(PreventAllCombatDamageToAndBySelfEffect.class::isInstance)) {
            return true;
        }
        if (isCombatDamage && hasActiveStaticEffect(gameData, creature, PreventAllCombatDamageBySelfEffect.class)) {
            return true;
        }
        if (isCombatDamage && gameData.preventAllCombatDamageByAttackingCreatures && creature.isAttacking()) {
            return true;
        }
        if (isCombatDamage && gameData.creaturesPreventedFromDealingCombatDamage.contains(creature.getId())) {
            return true;
        }
        if (isCombatDamage && isAllCombatDamageByControlledCreaturePrevented(gameData, creature)) {
            return true;
        }
        if (isCombatDamage && gameData.combatDamageExemptPredicate != null
                && !predicateEvaluationService.matchesPermanentPredicate(gameData, creature, gameData.combatDamageExemptPredicate)) {
            return true;
        }
        return false;
    }

    /** Returns whether a permanent's own static slot currently carries the requested effect. */
    public boolean hasActiveStaticEffect(GameData gameData, Permanent source,
                                         Class<? extends CardEffect> effectType) {
        UUID controllerId = findPermanentController(gameData, source.getId());
        if (controllerId == null || source.isStaticEffectSuppressed(effectType)) return false;
        return source.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(effect -> !source.isStaticEffectSuppressed(effect.getClass()))
                .map(effect -> staticEffectConditionResolver.resolve(gameData, source, controllerId, effect))
                .filter(effect -> effect != null && !source.isStaticEffectSuppressed(effect.getClass()))
                .anyMatch(effectType::isInstance);
    }

    /** Returns whether Ethereal Haze-style prevention applies to damage from this source. */
    public boolean isDamageByCreaturePrevented(GameData gameData, Permanent source) {
        return isDamagePreventable(gameData)
                && gameData.preventAllDamageByCreatures
                && source != null
                && isCreature(gameData, source)
                && !damageCantBePreventedFromSource(gameData, source);
    }

    /** Returns whether this creature's damage to the target creature is covered by its own static prevention. */
    public boolean isDamageFromPermanentSourceToCreaturePrevented(GameData gameData, Permanent source,
                                                                   Permanent target) {
        if (!isDamagePreventable(gameData) || source == null || target == null
                || !isCreature(gameData, source) || !isCreature(gameData, target)) {
            return false;
        }
        UUID controllerId = findPermanentController(gameData, source.getId());
        if (controllerId == null) return false;
        return source.getCard().getEffects(EffectSlot.STATIC).stream()
                .map(effect -> staticEffectConditionResolver.resolve(gameData, source, controllerId, effect))
                .filter(DamagePreventionBySelfEffect.class::isInstance)
                .map(DamagePreventionBySelfEffect.class::cast)
                .anyMatch(effect -> effect.targetFilter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, target, effect.targetFilter()));
    }

    /**
     * Ethersworn Shieldmage: "prevent all damage that would be dealt to [permanents matching a
     * predicate] this turn." Returns {@code true} when the given permanent currently matches any
     * active turn-scoped all-damage-prevention predicate. Re-evaluated per damage event, so it
     * covers permanents that start/stop matching after the effect resolved (official ruling).
     */
    public boolean isAllDamagePreventedByPredicate(GameData gameData, Permanent permanent) {
        if (gameData.allDamagePreventionPredicates.isEmpty()) return false;
        return gameData.allDamagePreventionPredicates.stream()
                .anyMatch(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, p));
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
        Set<PermanentPredicate> allDamagePredicates =
                gameData.allDamagePreventionPredicatesByController.get(controllerId);
        if (allDamagePredicates != null && allDamagePredicates.stream()
                .anyMatch(predicate -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, predicate, context))) {
            return true;
        }
        if (!isCombatDamage) return false;
        Set<PermanentPredicate> combatPredicates =
                gameData.combatDamagePreventionPredicatesByController.get(controllerId);
        return combatPredicates != null && combatPredicates.stream()
                .anyMatch(predicate -> predicateEvaluationService.matchesPermanentPredicate(
                        permanent, predicate, context));
    }

    public int getControlledCreatureDamageLimit(GameData gameData, Permanent creature) {
        UUID controllerId = findPermanentController(gameData, creature.getId());
        if (controllerId == null) return Integer.MAX_VALUE;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return Integer.MAX_VALUE;
        int damageLimit = Integer.MAX_VALUE;
        for (Permanent source : battlefield) {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof PreventAllDamageToCreaturesYouControlEffect prevent
                        && (prevent.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(
                        creature,
                        prevent.filter(),
                        FilterContext.of(gameData)
                                .withSourceCardId(source.getCard().getId())
                                .withSourceControllerId(controllerId)
                                .withSourcePermanentSnapshot(source)))) {
                    damageLimit = Math.min(damageLimit, prevent.damageLimit());
                }
            }
        }
        return damageLimit;
    }

    public boolean isAllDamageToControlledCreaturePrevented(GameData gameData, Permanent creature) {
        return getControlledCreatureDamageLimit(gameData, creature) == 0;
    }

    /** Returns whether damage from a player's source to that player's creature is prevented. */
    public boolean isDamageFromControlledSourceToControlledCreaturePrevented(
            GameData gameData, Permanent creature, UUID sourceControllerId) {
        if (!isDamagePreventable(gameData) || creature == null || sourceControllerId == null
                || !isCreature(gameData, creature)) return false;
        UUID creatureControllerId = findPermanentController(gameData, creature.getId());
        if (!sourceControllerId.equals(creatureControllerId)) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .map(effect -> staticEffectConditionResolver.resolve(
                                gameData, permanent, creatureControllerId, effect)))
                .anyMatch(ControlledSourceCreatureDamagePreventionEffect.class::isInstance);
    }

    /**
     * Returns whether a permanent controlled by the creature's controller prevents all combat damage
     * to that creature. Control is checked when damage is dealt, so the effect covers creatures that
     * enter later and follows control changes.
     */
    public boolean isAllCombatDamageToControlledCreaturePrevented(GameData gameData, Permanent creature) {
        UUID controllerId = findPermanentController(gameData, creature.getId());
        return controllerId != null && hasCombatDamageToAndByCreaturePrevention(gameData, controllerId);
    }

    /**
     * Returns whether a permanent controlled by the creature's controller prevents all combat damage
     * dealt by that creature. Control is checked when damage is dealt, so the effect follows control
     * changes.
     */
    public boolean isAllCombatDamageByControlledCreaturePrevented(GameData gameData, Permanent creature) {
        UUID controllerId = findPermanentController(gameData, creature.getId());
        return controllerId != null && hasCombatDamageToAndByCreaturePrevention(gameData, controllerId);
    }

    private boolean hasCombatDamageToAndByCreaturePrevention(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(PreventAllCombatDamageToAndByCreaturesYouControlEffect.class::isInstance);
    }

    /**
     * Returns {@code true} if damage from sources of the given color is currently prevented
     * (e.g. by a "prevent all damage from [color] sources" effect).
     */
    public boolean isDamageFromSourcePrevented(GameData gameData, CardColor sourceColor) {
        sourceColor = getDamageSourceColor(gameData, sourceColor);
        return sourceColor != null && gameData.preventDamageFromColors.contains(sourceColor);
    }

    /** Returns whether damage from the given permanent is prevented by an active source-based effect. */
    public boolean isDamageFromPermanentSourcePrevented(GameData gameData, Permanent source) {
        if (!isDamagePreventable(gameData) || source == null) return false;
        if (gameData.preventAllDamageFromNonHumanSources
                && !effectiveCreatureSubtypes(gameData, source).contains(CardSubtype.HUMAN)) {
            return true;
        }
        return getEffectiveColors(gameData, source).stream()
                .anyMatch(color -> isDamageFromSourcePrevented(gameData, color));
    }

    /** Returns whether damage from the given non-permanent source card is prevented. */
    public boolean isDamageFromCardSourcePrevented(GameData gameData, Card sourceCard) {
        if (!isDamagePreventable(gameData) || sourceCard == null) return false;
        if (gameData.preventAllDamageFromNonHumanSources
                && !getCardSubtypes(sourceCard, gameData, sourceCard.getOwnerId()).contains(CardSubtype.HUMAN)) {
            return true;
        }
        return isDamageFromSourcePrevented(gameData, sourceCard.getColor());
    }

    /** Returns whether damage from the source represented by the stack entry is prevented. */
    public boolean isDamageFromStackEntryPrevented(GameData gameData, StackEntry entry) {
        if (entry == null) return false;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : findPermanentById(gameData, entry.getSourcePermanentId());
        return source != null
                ? isDamageFromPermanentSourcePrevented(gameData, source)
                : isDamageFromCardSourcePrevented(gameData, entry.getEffectiveDamageSourceCard());
    }

    /**
     * Ghostly Flame: returns the colour a damage source is treated as having <em>for damage
     * purposes</em>. Yields {@code null} (colourless) when a {@link DamageSourcesOfColorsAreColorlessEffect}
     * on the battlefield covers {@code sourceColor}; otherwise returns {@code sourceColor} unchanged.
     * Only damage paths may use this — the source keeps its real colour for blocking, targeting,
     * and enchanting.
     */
    public CardColor getDamageSourceColor(GameData gameData, CardColor sourceColor) {
        return isDamageSourceColorNullified(gameData, sourceColor) ? null : sourceColor;
    }

    /** Damage-purposes colour set: {@link #getDamageSourceColor} applied to every colour. */
    public Set<CardColor> getDamageSourceColors(GameData gameData, Set<CardColor> sourceColors) {
        if (sourceColors == null || sourceColors.isEmpty()) return Set.of();
        Set<CardColor> result = new HashSet<>(sourceColors);
        result.removeIf(color -> isDamageSourceColorNullified(gameData, color));
        return result;
    }

    private boolean isDamageSourceColorNullified(GameData gameData, CardColor sourceColor) {
        if (sourceColor == null) return false;
        return gameData.anyPermanentMatches(permanent ->
                permanent.getCard().getEffects(EffectSlot.STATIC).stream()
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
                if (permanentId.equals(fe.affectedPermanentId())
                        && fe.effect() instanceof PermanentLockEffect lock && facet.test(lock)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int countControlledPermanentsMatching(GameData gameData, UUID controllerId, PermanentPredicate predicate) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, predicate)) {
                count++;
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
            if (permanent.getCard().getSubtypes().contains(subtype)
                    || permanent.getTransientSubtypes().contains(subtype)
                    || permanent.getGrantedSubtypes().contains(subtype)) {
                count++;
            }
        }
        return count;
    }

    /**
     * CR 302.6: whether summoning sickness stops {@code permanent} paying a {@code {T}} or
     * {@code {Q}} cost. Only creatures are affected, and haste — printed, granted, or supplied by an
     * "as though it had haste" effect the controller has (Concordant Crossroads on the stack, Hall
     * of the Bandit Lord) — lifts it.
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
     * Returns {@code true} if the permanent has lost its printed abilities, whether continuously
     * (Imprisoned in the Moon, Song of the Dryads) or until end of turn (Merfolk Trickster). Its
     * printed {@code ON_TAP} mana is gone even when a later-timestamp grant leaves it with a mana
     * ability of its own — {@code tapPermanent} refuses outright in that state, so a planner must
     * read the granted ability instead of the printed one.
     */
    public boolean hasLostAllAbilities(GameData gameData, Permanent permanent) {
        return permanent.isLosesAllAbilitiesUntilEndOfTurn()
                || computeStaticBonus(gameData, permanent).losesAllAbilities();
    }

    /** Whether a suspected permanent still has the menace and can't-block abilities. */
    public boolean hasSuspectedAbilities(GameData gameData, Permanent permanent) {
        if (!permanent.isSuspected() || permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
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
     * <p>This is the gate every mana-planning path shares — the virtual pool, the AI's payment
     * search, and the client's "castable if you tap your lands" projection all consult it before
     * counting a source. A lock the engine enforces but this method misses becomes mana a planner
     * counts on and can never produce: it taps everything, comes up short, and fires an action the
     * engine silently refuses. So the player-level locks below are mirrored here even though they
     * are not properties of the permanent.
     */
    public boolean canActivateManaAbility(GameData gameData, Permanent permanent) {
        String cardName = permanent.getCard().getName();

        // City of Solitude: mana abilities can only be activated on the controller's turn.
        UUID controllerId = findPermanentController(gameData, permanent.getId());
        if (controllerId != null && isLockedOutByOwnTurnOnlyRestriction(gameData, controllerId)) {
            return false;
        }

        // Sen Triplets: a player locked out this turn can activate no ability at all, mana included.
        if (controllerId != null && gameData.playersCantActivateAbilitiesThisTurn.contains(controllerId)) {
            return false;
        }

        // Grand Abolisher: on the opponent's turn, abilities of the controller's artifacts,
        // creatures and enchantments are locked. Lands keep producing.
        if (controllerId != null && isLockedOutByOpponentsTurnAbilityRestriction(gameData, controllerId)
                && (isCreature(gameData, permanent) || isArtifact(gameData, permanent)
                        || isEnchantment(gameData, permanent))) {
            return false;
        }

        // Check temporary ability loss (e.g. Merfolk Trickster)
        if (permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            return false;
        }

        // Continuous ability loss strips printed mana abilities, but later-timestamp grants
        // (e.g. Imprisoned in the Moon's "{T}: Add {C}") still work.
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
        if (isLockedFromActivatingTapAbilities(gameData, permanent)) {
            return false;
        }

        // Floating per-permanent lock (Detain, Xathrid Gorgon's petrification) — mana abilities
        // are activated abilities too, so a blanket lock stops them as well.
        if (isLockedFromActivatingAbilities(gameData, permanent.getId())) {
            return false;
        }

        for (UUID pid : gameData.playerIds) {
            for (Permanent p : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect lock
                            && lock.blocksManaAbilities()) {
                        if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, lock.predicate())) {
                            return false;
                        }
                    }
                    if (effect instanceof ActivatedAbilitiesOfChosenNameCantBeActivatedEffect lock) {
                        if (lock.blocksManaAbilities() && cardName.equals(p.getChosenName())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if the permanent's activated abilities with {T} in their costs are
     * locked (Serra Bestiary aura, or a board-wide matching-predicate lock such as Katabatic Winds).
     */
    public boolean isLockedFromActivatingTapAbilities(GameData gameData, Permanent permanent) {
        if (hasAuraWithEffect(gameData, permanent, EnchantedCreatureCantActivateTapAbilitiesEffect.class)) {
            return true;
        }
        for (UUID pid : gameData.playerIds) {
            for (Permanent p : gameData.playerBattlefields.getOrDefault(pid, List.of())) {
                for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof MatchingPermanentsCantActivateTapAbilitiesEffect lock
                            && predicateEvaluationService.matchesPermanentPredicate(
                                    gameData, permanent, lock.affectedPredicate())) {
                        return true;
                    }
                }
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
     * on this permanent (Evil Presence → one color; Lush Growth → red, green, and white), or an
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
}
