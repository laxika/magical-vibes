package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemArtifactGraveyardReturnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithTotalDyingCreaturesPowerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachAuraAttachedToDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureControllerDiscardsCardEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureControllerMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureControllerMaySearchLibraryForSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDiesGainLifeAndDrawEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedControllerSacrificesCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndReturnSourceToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndDrawEqualToDyingPowerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToSourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEqualToDyingPowerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCurseAttachedToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingOpponentCreatureAsTreasureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureAndReattachAuraOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardAttachedToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToChosenCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToSharedTypeCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringLandFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StealDyingOpponentPermanentUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapCreaturesSharingDyingCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Trigger collectors for death and leaves-battlefield events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeathTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport graveyardTargetingSupport;
    @Autowired @Lazy private GraveyardTargetingService graveyardTargetingService;

    /**
     * The handlers of the "whenever [an object] dies" abilities the trigger pipeline matches, by the old slot
     * collector that binds them ({@link CreatureDeathWatch}) and effect class. They left {@code @CollectsTrigger}
     * when their slots left {@code EffectSlot} (effect-slot-refactor.md step 11); step 15 replaces them.
     */
    private final Map<CreatureDeathWatch, Map<Class<? extends CardEffect>, TriggerCollectorHandler>>
            creatureDeathHandlers = new EnumMap<>(CreatureDeathWatch.class);

    {
        registerCreatureDeathHandlers();
    }

    /**
     * Dispatches {@code effect}, an effect of a "whenever [an object] dies" ability that {@code watch}'s collector
     * binds, to its handler, in the order the trigger registry picks one: the effect's class, then the class of the
     * effect a {@link MayEffect}, {@link MayPayManaEffect} or {@link MayPayTapPermanentsEffect} wraps, then the
     * watch's default. Returns whether it triggered.
     */
    public boolean dispatchCreatureDeath(CreatureDeathWatch watch, TriggerMatchContext match, CardEffect effect,
                                         TriggerContext context) {
        Map<Class<? extends CardEffect>, TriggerCollectorHandler> handlers =
                creatureDeathHandlers.getOrDefault(watch, Map.of());
        TriggerCollectorHandler handler = handlers.get(effect.getClass());
        if (handler != null) {
            return handler.handle(match, effect, context);
        }
        CardEffect inner = switch (effect) {
            case MayEffect may -> may.wrapped();
            case MayPayManaEffect mayPay -> mayPay.wrapped();
            case MayPayTapPermanentsEffect mayPayTap -> mayPayTap.wrapped();
            default -> null;
        };
        if (inner != null) {
            handler = handlers.get(inner.getClass());
            if (handler != null) {
                return handler.handle(match, inner, context);
            }
        }
        handler = handlers.get(CardEffect.class);
        return handler != null && handler.handle(match, effect, context);
    }

    private void registerCreatureDeath(CreatureDeathWatch watch, Class<? extends CardEffect> effectClass,
                                       TriggerCollectorHandler handler) {
        creatureDeathHandlers.computeIfAbsent(watch, ignored -> new HashMap<>()).put(effectClass, handler);
    }

    private void registerCreatureDeathHandlers() {
        registerCreatureDeath(CreatureDeathWatch.ALLY, MayPayManaEffect.class,
                (match, effect, context) -> handleAllyCreatureMayPay(match, (MayPayManaEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY, MayPayLifeEffect.class,
                (match, effect, context) -> handleAllyCreatureMayPayLife(match, (MayPayLifeEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY, MayEffect.class,
                (match, effect, context) -> handleAllyCreatureMay(match, (MayEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_CREATURE_OR_PLANESWALKER, MayEffect.class,
                (match, effect, context) -> handleAllyCreatureOrPlaneswalkerMay(match, (MayEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.EQUIPPED, MayPayLifeAndDrawEqualToDyingPowerEffect.class,
                (match, effect, context) -> handleEquippedCreatureDeathMayPayLifeAndDrawEqualToPower(match,
                        (MayPayLifeAndDrawEqualToDyingPowerEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.EQUIPPED, ReturnDyingCreatureToBattlefieldEffect.class,
                (match, effect, context) -> handleEquippedCreatureReturn(match,
                        (ReturnDyingCreatureToBattlefieldEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.EQUIPPED, ConditionalEffect.class,
                (match, effect, context) -> handleEquippedCreatureConditional(match,
                        (ConditionalEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.EQUIPPED, RegisterDelayedReturnDyingCreatureUnderControlEffect.class,
                (match, effect, context) -> handleEquippedCreatureDelayedReturn(match,
                        (RegisterDelayedReturnDyingCreatureUnderControlEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.EQUIPPED, PerpetuallyGrantTriggeredAbilityToSourceCardEffect.class,
                (match, effect, context) -> handleEquippedCreatureDeathPerpetualGrant(match,
                        (PerpetuallyGrantTriggeredAbilityToSourceCardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.EQUIPPED, CardEffect.class,
                (match, effect, context) -> handleEquippedCreatureDeathDefault(match, (CardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.EQUIPPED, ReturnTriggeringCardToOwnerHandEffect.class,
                (match, effect, context) -> handleEquippedCreatureReturnToHand(match,
                        (ReturnTriggeringCardToOwnerHandEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_ARTIFACT_OR_CREATURE, CardEffect.class,
                this::handleAllyArtifactOrCreatureDeath);
        registerCreatureDeath(CreatureDeathWatch.ANY, ReturnTriggeringPermanentToBattlefieldEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathReturnTriggeringPermanent(match,
                        (ReturnTriggeringPermanentToBattlefieldEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, BoostSelfEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathBoostSelf(match, (BoostSelfEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, PutCountersOnSourceEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathPutCounters(match,
                        (PutCountersOnSourceEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, PutCountersOnSelfEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathPutCountersOnSelf(match,
                        (PutCountersOnSelfEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, PutCountersOnSourceEqualToDyingPowerEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathPutCountersEqualToPower(match,
                        (PutCountersOnSourceEqualToDyingPowerEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, UntapPermanentsEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathUntap(match,
                        (UntapPermanentsEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, TapPermanentsEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathTap(match, (TapPermanentsEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, TapOrUntapCreaturesSharingDyingCreatureTypeEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathTapOrUntap(match,
                        (TapOrUntapCreaturesSharingDyingCreatureTypeEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, MayEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathMay(match, (MayEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, DyingCreatureControllerMayDrawCardEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathDyingControllerDraws(match,
                        (DyingCreatureControllerMayDrawCardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, DrawCardForEachAuraAttachedToDyingCreatureEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathDrawsForAttachedAuras(match,
                        (DrawCardForEachAuraAttachedToDyingCreatureEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, DyingCreatureControllerDiscardsCardEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathDyingControllerDiscards(match,
                        (DyingCreatureControllerDiscardsCardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, DyingCreatureControllerSacrificesPermanentsEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathDyingControllerSacrifices(match,
                        (DyingCreatureControllerSacrificesPermanentsEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY,
                EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathBountyReward(match,
                        (EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, DealDamageToPlayersEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathDamageController(match,
                        (DealDamageToPlayersEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, LoseLifeEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathDyingControllerLosesLife(match,
                        (LoseLifeEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, CreateTokenCopyOfDyingCreatureEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathCreateTokenCopy(match,
                        (CreateTokenCopyOfDyingCreatureEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, CardEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathDefault(match, (CardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, RegisterDelayedReturnDyingCreatureUnderControlEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathDelayedReturnUnderOwnersControl(match,
                        (RegisterDelayedReturnDyingCreatureUnderControlEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, ExileTriggeringCreatureAndReturnSourceToHandEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathExileLinkedCreature(match,
                        (ExileTriggeringCreatureAndReturnSourceToHandEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, DestroyReferencedPermanentEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathDestroyEnchanted(match,
                        (DestroyReferencedPermanentEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, PutCounterOnReferencedPermanentEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathCounterOnEnchanted(match,
                        (PutCounterOnReferencedPermanentEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, ConditionalReplacementEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathConditionalReplacement(match,
                        (ConditionalReplacementEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, CreateTokenWithTotalDyingCreaturesPowerEffect.class,
                (match, effect, context) -> handleAllyNontokenCreateTokenWithTotalDyingCreaturesPower(match,
                        (CreateTokenWithTotalDyingCreaturesPowerEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, MayEffect.class,
                (match, effect, context) -> handleAllyNontokenMay(match, (MayEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, MayPayManaEffect.class,
                (match, effect, context) -> handleAllyNontokenMayPay(match, (MayPayManaEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, DyingCreatureControllerMaySearchLibraryForSameNameEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathDyingControllerSearchesSameName(match,
                        (DyingCreatureControllerMaySearchLibraryForSameNameEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, CardEffect.class,
                (match, effect, context) -> handleAllyNontokenDefault(match, (CardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ALLY_NONTOKEN, ConditionalEffect.class,
                (match, effect, context) -> handleAllyNontokenInterveningIfConditional(match,
                        (ConditionalEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY_NONTOKEN, ExileTriggeringCreatureAndTrackWithSourceEffect.class,
                (match, effect, context) -> handleAnyNontokenCreatureDeathExileAndTrack(match,
                        (ExileTriggeringCreatureAndTrackWithSourceEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY_NONTOKEN, ImprintDyingCreatureEffect.class,
                (match, effect, context) -> handleImprintDyingCreature(match,
                        (ImprintDyingCreatureEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY_NONTOKEN, ReturnDyingCreatureToBattlefieldEffect.class,
                (match, effect, context) -> handleReturnDyingCreatureMayPay(match,
                        (ReturnDyingCreatureToBattlefieldEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY_NONTOKEN, CardEffect.class,
                (match, effect, context) -> handleAnyNontokenCreatureDeathDefault(match, (CardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ENCHANTED_PLAYER_NONTOKEN, CardEffect.class,
                (match, effect, context) -> handleEnchantedPlayerNontokenCreatureDeathDefault(match,
                        (CardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.OPPONENT, MayEffect.class,
                (match, effect, context) -> handleOpponentCreatureDeathMay(match, (MayEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.OPPONENT, PutCountersOnSourceEqualToDyingPowerEffect.class,
                (match, effect, context) -> handleOpponentCreatureDeathPutCountersEqualToPower(match,
                        (PutCountersOnSourceEqualToDyingPowerEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.OPPONENT, ReturnDyingOpponentCreatureAsTreasureEffect.class,
                (match, effect, context) -> handleOpponentCreatureReturnAsTreasure(match,
                        (ReturnDyingOpponentCreatureAsTreasureEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.OPPONENT, RegisterDelayedReturnDyingCreatureUnderControlEffect.class,
                (match, effect, context) -> handleOpponentCreatureDelayedReturnUnderControl(match,
                        (RegisterDelayedReturnDyingCreatureUnderControlEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.OPPONENT, CardEffect.class,
                (match, effect, context) -> handleOpponentCreatureDeathDefault(match, (CardEffect) effect, context));
        registerCreatureDeath(CreatureDeathWatch.ANY, PutCountersOnSourceCardEffect.class,
                (match, effect, context) -> handleAnyCreatureDeathPutCountersOnSourceCard(match,
                        (PutCountersOnSourceCardEffect) effect, context));
    }

    private CardEffect snapshotArtifactManaValue(CardEffect effect, int artifactManaValue) {
        if (!(effect instanceof ReturnCardFromGraveyardEffect returnEffect)
                || !returnEffect.targetGraveyard()) {
            return effect;
        }

        CardPredicate manaValueFilter = new CardMaxManaValuePredicate(artifactManaValue - 1);
        CardPredicate filter = returnEffect.filter() == null
                ? manaValueFilter
                : new CardAllOfPredicate(List.of(returnEffect.filter(), manaValueFilter));
        return returnEffect.toBuilder()
                .filter(filter)
                .dynamicMaxManaValue(null)
                .build();
    }

    boolean handleAllyCreatureMayPay(TriggerMatchContext match,
            MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), mayPay,
                null, match.permanent().getId());
        return true;
    }

    boolean handleAllyCreatureMayPayLife(TriggerMatchContext match,
            MayPayLifeEffect mayPay, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                cd.dyingCreatureControllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(mayPay)));
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        match.gameData().stack.add(entry);
        return true;
    }

    boolean handleAllyCreatureMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), may, null,
                match.permanent().getId());
        // Snapshot the source so a transform instruction can tell whether it has already transformed
        // since this trigger was put on the stack (CR 701.27f, Thraben Sentry).
        StackEntry entry = match.gameData().stack.getLast();
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        // "That card" (Colfenor's Urn, Angelic Renewal, Shirei, Shizo's Caretaker): the creature that died.
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        entry.setTriggeringPermanent(cd.dyingPermanent());
        return true;
    }

    @CollectsWatchTrigger(value = MayEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOwnedCreatureGraveyardMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard death = (TriggerContext.AnyPermanentGraveyard) ctx;
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(), match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new MayEffect(may.wrapped(), may.prompt()))),
                null, match.permanent().getId());
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsWatchTrigger(value = RegisterDelayedReturnDyingCreatureUnderControlEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOwnedCreatureGraveyardDelayedReturn(TriggerMatchContext match,
            RegisterDelayedReturnDyingCreatureUnderControlEffect effect, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard death = (TriggerContext.AnyPermanentGraveyard) ctx;
        if (death.dyingCard() == null) {
            return false;
        }
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(), match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null, match.permanent().getId());
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsWatchTrigger(value = MayEffect.class,
            watch = ZoneChangeWatch.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAnyPermanentGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard death = (TriggerContext.AnyPermanentGraveyard) ctx;
        if (!match.controllerId().equals(death.dyingControllerId())) {
            return false;
        }
        Permanent dyingPermanent = death.dyingPermanent() != null
                ? death.dyingPermanent() : new Permanent(death.dyingCard());

        if (may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || may.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || graveyardTargetingSupport.findTarget(List.of(may)) != null) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(may)))
                    .withTriggeringPermanent(dyingPermanent));
        } else {
            match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                    null, match.permanent().getId());
            match.gameData().stack.getLast().setTriggeringPermanent(dyingPermanent);
        }
        return true;
    }

    boolean handleAllyCreatureOrPlaneswalkerMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), may, null,
                match.permanent().getId());
        return true;
    }

    boolean handleEquippedCreatureDeathMayPayLifeAndDrawEqualToPower(TriggerMatchContext match,
            MayPayLifeAndDrawEqualToDyingPowerEffect effect, TriggerContext ctx) {
        TriggerContext.EquippedCreatureDeath ecd = (TriggerContext.EquippedCreatureDeath) ctx;
        int power = Math.max(0, ecd.dyingCreaturePower());
        MayPayLifeEffect resolved = new MayPayLifeEffect(
                power,
                new DrawCardEffect(power),
                "Pay " + power + " life to draw " + power + " cards?");
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolved)),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (equipped creature died)."));
        log.info("Game {} - {} triggers (equipped creature died)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    boolean handleEquippedCreatureReturn(TriggerMatchContext match,
            ReturnDyingCreatureToBattlefieldEffect effect, TriggerContext ctx) {
        TriggerContext.EquippedCreatureDeath ecd = (TriggerContext.EquippedCreatureDeath) ctx;
        if (ecd.dyingCard() == null) {
            return false;
        }
        return queueEquippedCreatureReturn(match, effect, ecd);
    }

    /**
     * "Return that card to the battlefield under your control if it's a Samurai card" (Oathkeeper, Takeno's Daisho):
     * the return reads the dying card as {@link #handleEquippedCreatureReturn} gives it, and its "if" reads that
     * card as the ability resolves. Any other conditional goes on the stack as it is.
     */
    boolean handleEquippedCreatureConditional(TriggerMatchContext match, ConditionalEffect conditional,
                                              TriggerContext ctx) {
        TriggerContext.EquippedCreatureDeath ecd = (TriggerContext.EquippedCreatureDeath) ctx;
        if (!(conditional.wrapped() instanceof ReturnDyingCreatureToBattlefieldEffect)) {
            return handleEquippedCreatureDeathDefault(match, conditional, ctx);
        }
        if (ecd.dyingCard() == null) {
            return false;
        }
        return queueEquippedCreatureReturn(match, conditional, ecd);
    }

    private boolean queueEquippedCreatureReturn(TriggerMatchContext match, CardEffect effect,
                                                TriggerContext.EquippedCreatureDeath ecd) {
        GameData gameData = match.gameData();
        // The effect resolves off the dying card, the entry's triggering card, and the stack entry's
        // targetId carries the source Equipment (needed when it reattaches).
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        );
        entry.setTargetId(match.permanent().getId());
        markDyingCard(gameData, entry, ecd.dyingCard());
        gameData.stack.add(entry);
        gameLogService.append(gameData, GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (equipped creature died)."));
        log.info("Game {} - {} return trigger fires (equipped creature died)", gameData.id, match.permanent().getCard().getName());
        return true;
    }

    boolean handleEquippedCreatureDelayedReturn(TriggerMatchContext match,
            RegisterDelayedReturnDyingCreatureUnderControlEffect effect, TriggerContext ctx) {
        TriggerContext.EquippedCreatureDeath death = (TriggerContext.EquippedCreatureDeath) ctx;
        if (death.dyingCard() == null) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (equipped creature died)."));
        return true;
    }

    boolean handleEquippedCreatureDeathPerpetualGrant(TriggerMatchContext match,
            PerpetuallyGrantTriggeredAbilityToSourceCardEffect effect, TriggerContext ctx) {
        TriggerContext.EquippedCreatureDeath death = (TriggerContext.EquippedCreatureDeath) ctx;
        if (death.dyingCard() == null) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (equipped creature died)."));
        log.info("Game {} - {} perpetual-grant trigger fires (equipped creature died)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    boolean handleEquippedCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        GameData gameData = match.gameData();
        TriggerContext.EquippedCreatureDeath death = (TriggerContext.EquippedCreatureDeath) ctx;
        CardEffect resolvedEffect = effect;
        if (resolvedEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || resolvedEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(resolvedEffect)),
                    null, new Permanent(match.permanent()), match.permanent().getCard().getTargetFilter())
                    .withTriggeringPermanent(death.dyingPermanent());
            gameData.queueInteraction(death.dyingCard() == null ? target : target.withTriggeringCard(
                    death.dyingCard().getId(), gameData.graveyardEntryVersion(death.dyingCard().getId())));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(resolvedEffect)),
                    null,
                    match.permanent().getId()
            );
            // The dying card's graveyard object is fixed at the death (CR 400.7): a later re-entry
            // before this trigger resolves must not satisfy a delayed return meant for the old object.
            markDyingCard(gameData, entry, death.dyingCard());
            entry.setTriggeringPermanent(death.dyingPermanent());
            gameData.stack.add(entry);
        }
        gameLogService.append(gameData, GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (equipped creature died)."));
        log.info("Game {} - {} triggers (equipped creature died)", gameData.id, match.permanent().getCard().getName());
        return true;
    }

    // ── ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD ──────────────────────

    @CollectsWatchTrigger(value = ReturnSourceAuraToOpponentCreatureOnDeathEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnSourceAura(TriggerMatchContext match,
            ReturnSourceAuraToOpponentCreatureOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect effectForStack = epd.dyingPermanentControllerId() != null
                ? new ReturnSourceAuraToOpponentCreatureOnDeathEffect(epd.dyingPermanentControllerId())
                : effect;
        addEnchantedPermanentDeathEntry(match, effectForStack);
        return true;
    }

    @CollectsWatchTrigger(value = ReturnSourceTransformedFromGraveyardAttachedToTargetPlayerEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnSourceTransformedAttachedToTargetPlayer(TriggerMatchContext match,
            ReturnSourceTransformedFromGraveyardAttachedToTargetPlayerEffect effect, TriggerContext ctx) {
        addEnchantedPermanentDeathEntry(match, effect);
        return true;
    }

    @CollectsWatchTrigger(value = ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnSourceAuraOrNonAura(TriggerMatchContext match,
            ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect effectForStack = epd.dyingPermanentControllerId() != null
                ? new ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect(epd.dyingPermanentControllerId())
                : effect;
        addEnchantedPermanentDeathEntry(match, effectForStack);
        return true;
    }

    @CollectsWatchTrigger(value = ReturnSourceAuraToSharedTypeCreatureOnDeathEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnSourceAuraSharedType(TriggerMatchContext match,
            ReturnSourceAuraToSharedTypeCreatureOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect baked = epd.dyingCreatureCardId() != null
                ? new ReturnSourceAuraToSharedTypeCreatureOnDeathEffect(epd.dyingCreatureCardId())
                : effect;
        // "you may return this card ..." — gate the return behind a MayEffect for the aura's controller.
        MayEffect may = new MayEffect(baked,
                "return it to the battlefield attached to a creature that shares a creature type with the creature that died?");
        StackEntry entry = addEnchantedPermanentDeathEntryReturning(match, may, null, null, null);
        // A token that died has ceased to exist, so its creature types survive only as last-known information.
        if (entry != null && epd.dyingCard() != null && epd.dyingCreatureCardId() != null) {
            entry.rememberLastKnownPermanentCard(epd.dyingCreatureCardId(), epd.dyingCard());
        }
        return true;
    }

    @CollectsWatchTrigger(value = ReturnEnchantedCreatureToOwnerHandOnDeathEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnEnchantedCreature(TriggerMatchContext match,
            ReturnEnchantedCreatureToOwnerHandOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        if (!epd.wasCreature()) return false;
        CardEffect effectForStack = epd.dyingCreatureCardId() != null
                ? new ReturnEnchantedCreatureToOwnerHandOnDeathEffect(epd.dyingCreatureCardId(),
                        effect.followUpManaCost(), effect.followUpPrompt())
                : effect;
        addEnchantedPermanentDeathEntry(match, effectForStack, null, null, epd.dyingCreatureCardId());
        return true;
    }

    @CollectsWatchTrigger(value = EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedCreatureControllerMaySearch(TriggerMatchContext match,
            EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect effect,
            TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        // The trigger is the Aura's ability (controlled by the Aura's controller), but the dying creature's
        // controller makes the choice and searches their own library.
        StackEntry entry = addEnchantedPermanentDeathEntryReturning(match,
                new MayEffect(
                        new SearchLibraryEffect(new Fixed(1), new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD,
                                LibrarySearchPlayer.TRIGGERING_PERMANENT_CONTROLLER),
                        "Search your library for a creature card and put it onto the battlefield?",
                        null,
                        MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER),
                null, null, null);
        entry.setNonTargeting(true);
        entry.setTriggeringPermanentControllerId(epd.dyingPermanentControllerId());
        return true;
    }

    @CollectsWatchTrigger(value = ReturnEnchantedCreatureToBattlefieldOnDeathEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnEnchantedCreatureToBattlefield(TriggerMatchContext match,
            ReturnEnchantedCreatureToBattlefieldOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect effectForStack = !epd.dyingPermanentCardIds().isEmpty()
                ? new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(epd.dyingPermanentCardIds(),
                        effect.underAuraControllersControl(), effect.enterTapped(), effect.enterWithCounter())
                : effect;
        addEnchantedPermanentDeathEntry(match, effectForStack, null, null, epd.dyingCreatureCardId());
        return true;
    }

    @CollectsWatchTrigger(value = ReturnEnchantedCreatureAndReattachAuraOnDeathEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnEnchantedCreatureAndReattachAura(TriggerMatchContext match,
            ReturnEnchantedCreatureAndReattachAuraOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect effectForStack = epd.dyingCreatureCardId() != null
                ? new ReturnEnchantedCreatureAndReattachAuraOnDeathEffect(epd.dyingCreatureCardId())
                : effect;
        addEnchantedPermanentDeathEntry(match, effectForStack);
        return true;
    }

    @CollectsWatchTrigger(value = PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handlePutLesserManaValueCreatureAndReturnAura(TriggerMatchContext match,
            PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect effect,
            TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        if (!epd.wasCreature()) {
            return false;
        }
        MayEffect may = new MayEffect(effect,
                "put a creature card with lesser mana value from your hand or command zone onto the battlefield?");
        StackEntry entry = addEnchantedPermanentDeathEntryReturning(match, may, null, null, null);
        if (entry != null && epd.dyingCreatureCardId() != null) {
            // "Lesser mana value" than the creature as it last existed on the battlefield (CR 603.10a).
            entry.setDyingPermanentManaValue(epd.dyingCreatureManaValue());
        }
        return true;
    }

    @CollectsWatchTrigger(value = EnchantedCreatureControllerLosesLifeEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedCreatureControllerLosesLife(TriggerMatchContext match,
            EnchantedCreatureControllerLosesLifeEffect effect, TriggerContext ctx) {
        // Banewasp Affliction: the dying creature's controller loses life equal to its toughness
        // (amount 0 = "use the toughness"). Decomposition instead names a fixed amount, which is
        // used as-is. Snapshot the last-known toughness and controller at trigger time (the creature
        // has already left the battlefield); life loss can't be negative.
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        int amount = effect.amount() > 0 ? effect.amount() : Math.max(0, epd.dyingCreatureToughness());
        CardEffect baked = new EnchantedCreatureControllerLosesLifeEffect(
                amount, epd.dyingPermanentControllerId());
        addEnchantedPermanentDeathEntry(match, baked);
        return true;
    }

    @CollectsWatchTrigger(value = DrawCardForTargetPlayerEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedCreatureControllerDrawsCard(TriggerMatchContext match,
            DrawCardForTargetPlayerEffect effect, TriggerContext ctx) {
        // Fate Foretold: the dying creature's controller draws, not the Aura's controller.
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                epd.dyingPermanentControllerId(),
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent put into graveyard)."));
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughness(TriggerMatchContext match,
            EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect effect, TriggerContext ctx) {
        // Death Watch: controller loses life = power, you gain life = toughness. Both use LKI; one
        // stack entry so SBAs see the net after loss then gain (own-creature case can go to 0 and back).
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        int power = Math.max(0, epd.dyingCreaturePower());
        int toughness = Math.max(0, epd.dyingCreatureToughness());
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(
                        new EnchantedCreatureControllerLosesLifeEffect(power, epd.dyingPermanentControllerId()),
                        new GainLifeEffect(toughness)
                ))
        ));
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent put into graveyard)."));
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = EnchantedCreatureDiesGainLifeAndDrawEqualToToughnessEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedCreatureDiesGainLifeAndDrawEqualToToughness(TriggerMatchContext match,
            EnchantedCreatureDiesGainLifeAndDrawEqualToToughnessEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        int toughness = Math.max(0, epd.dyingCreatureToughness());
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new GainLifeEffect(toughness), new DrawCardEffect(toughness)))
        ));
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent put into graveyard)."));
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = DealDamageToPlayersEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedCreatureDeathDamageController(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        // Creature Bond: the dying creature's controller is dealt damage equal to its toughness.
        // The creature has already left the battlefield, so snapshot its last-known toughness as the
        // fixed damage amount (>= 0) and bake its controller as the stack targetId — the
        // ENCHANTED_PERMANENT_CONTROLLER recipient reads that player from the stack entry.
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        DealDamageToPlayersEffect baked = new DealDamageToPlayersEffect(
                Math.max(0, epd.dyingCreatureToughness()), effect.recipient());
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(baked)),
                epd.dyingPermanentControllerId(),
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent put into graveyard)."));
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)", match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = DealDamageToEnchantedPermanentControllerEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedCreatureDeathDamageEnchantedController(TriggerMatchContext match,
            DealDamageToEnchantedPermanentControllerEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                epd.dyingPermanentControllerId(),
                match.permanent().getId()
        );
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        // When the dying creature deals the damage (last-known information), its lifelink and its
        // controller apply to the damage rather than the Aura's.
        if (effect.enchantedPermanentIsSource()) {
            if (epd.dyingCard() != null) {
                entry.setDamageSourceCard(epd.dyingCard());
            }
            entry.setDamageSourceControllerId(epd.dyingPermanentControllerId());
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent put into graveyard)."));
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)", match.gameData().id,
                match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CreateTokenEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedCreatureDeathTokenCount(TriggerMatchContext match,
            CreateTokenEffect effect, TriggerContext ctx) {
        if (!amountEvaluationService.referencesEventValue(effect.amount())) {
            addEnchantedPermanentDeathEntry(match, effect);
            return true;
        }

        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        addEnchantedPermanentDeathEntry(match, effect.withAmount(Math.max(0, epd.dyingCreaturePower())));
        return true;
    }

    @CollectsWatchTrigger(value = ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnEnchantedCreatureAndSourceTransformed(TriggerMatchContext match,
            ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect effectForStack = epd.dyingCreatureCardId() != null
                ? new ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect(epd.dyingCreatureCardId())
                : effect;
        addEnchantedPermanentDeathEntry(match, effectForStack);
        return true;
    }

    @CollectsWatchTrigger(value = ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleExileTopCardsFromEnchantedCreatureOwnerAndAllowCast(TriggerMatchContext match,
            ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        UUID libraryOwnerId = epd.dyingPermanentOwnerId();
        if (libraryOwnerId == null && epd.dyingCreatureCardId() != null) {
            libraryOwnerId = gameQueryService.findGraveyardOwnerById(match.gameData(), epd.dyingCreatureCardId());
        }
        CardEffect baked = new ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect(
                Math.max(0, epd.dyingCreaturePower()), libraryOwnerId);
        addEnchantedPermanentDeathEntry(match, baked);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedPermanentDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        Integer eventValue = effectReferencesEventValue(effect)
                ? Math.max(0, epd.dyingCreaturePower())
                : null;
        StackEntry entry = addEnchantedPermanentDeathEntryReturning(match, effect, eventValue,
                epd.dyingPermanentControllerId(), null);
        if (entry != null && enchantedPermanentDealsDamage(effect)) {
            // The dying creature deals the damage (last-known information): its lifelink and its
            // controller apply to that damage rather than the Aura's.
            if (epd.dyingCard() != null) {
                entry.setDamageSourceCard(epd.dyingCard());
            }
            entry.setDamageSourceControllerId(epd.dyingPermanentControllerId());
        }
        return true;
    }

    private static boolean enchantedPermanentDealsDamage(CardEffect effect) {
        if (effect instanceof SequenceEffect sequence) {
            return sequence.steps().stream().anyMatch(DeathTriggerCollectorService::enchantedPermanentDealsDamage);
        }
        return effect instanceof DealDamageToEnchantedPermanentControllerEffect damage
                && damage.enchantedPermanentIsSource();
    }

    private void addEnchantedPermanentDeathEntry(TriggerMatchContext match, CardEffect effect) {
        addEnchantedPermanentDeathEntry(match, effect, null);
    }

    private void addEnchantedPermanentDeathEntry(TriggerMatchContext match, CardEffect effect,
            Integer eventValue) {
        addEnchantedPermanentDeathEntry(match, effect, eventValue, null);
    }

    private void addEnchantedPermanentDeathEntry(TriggerMatchContext match, CardEffect effect,
            Integer eventValue, UUID enchantedControllerId) {
        addEnchantedPermanentDeathEntry(match, effect, eventValue, enchantedControllerId, null);
    }

    /**
     * When {@code dyingCardId} is given, the entry records that card's graveyard identity so a
     * resolution after the card has entered a graveyard again can be recognised as a new object.
     */
    private void addEnchantedPermanentDeathEntry(TriggerMatchContext match, CardEffect effect,
            Integer eventValue, UUID enchantedControllerId, UUID dyingCardId) {
        addEnchantedPermanentDeathEntryReturning(match, effect, eventValue, enchantedControllerId, dyingCardId);
    }

    /** As above; returns the stack entry, or {@code null} if the trigger was queued for a target choice. */
    private StackEntry addEnchantedPermanentDeathEntryReturning(TriggerMatchContext match, CardEffect effect,
            Integer eventValue, UUID enchantedControllerId, UUID dyingCardId) {
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)), eventValue,
                    new Permanent(match.permanent()), false));
            return null;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        );
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        if (dyingCardId != null && effect instanceof ReturnEnchantedCreatureToOwnerHandOnDeathEffect returnToHand
                && returnToHand.followUpManaCost() != null) {
            // The follow-up returns the aura from its own graveyard stay, which the triggering-card
            // version tracks; the dying creature's stay is recorded per card.
            entry.getEventCardGraveyardEntryVersions().put(dyingCardId,
                    match.gameData().graveyardEntryVersion(dyingCardId));
            entry.setTriggeringCardId(match.permanent().getCard().getId());
        } else if (dyingCardId != null) {
            entry.setTriggeringCardGraveyardEntryVersion(match.gameData().graveyardEntryVersion(dyingCardId));
        } else if (returnsSourceCardFromGraveyard(effect)) {
            // The aura is still on the battlefield while this is queued and enters the graveyard in the
            // same state-based pass; GameData.markGraveyardEntry binds that stay's identity to this entry.
            entry.setTriggeringCardId(match.permanent().getCard().getId());
        }
        if (effect instanceof ReturnEnchantedCreatureToBattlefieldOnDeathEffect returning) {
            for (UUID cardId : returning.dyingCreatureCardIds()) {
                entry.getEventCardGraveyardEntryVersions().put(cardId,
                        match.gameData().graveyardEntryVersion(cardId));
            }
        }
        if (effect instanceof ReturnEnchantedCreatureAndSourceTransformedOnDeathEffect combined
                && combined.dyingCreatureCardId() != null) {
            entry.getEventCardGraveyardEntryVersions().put(combined.dyingCreatureCardId(),
                    match.gameData().graveyardEntryVersion(combined.dyingCreatureCardId()));
        }
        if (enchantedControllerId != null) {
            entry.setTargetId(enchantedControllerId);
            entry.setNonTargeting(true);
        }
        // Only effects that read the death event's numeric payload (e.g. the dying creature's
        // last-known power) bake one; the field is a primitive, so a null would unbox and throw.
        if (eventValue != null) {
            entry.setEventValue(eventValue);
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent put into graveyard)."));
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)", match.gameData().id, match.permanent().getCard().getName());
        return entry;
    }

    private static boolean returnsSourceCardFromGraveyard(CardEffect effect) {
        if (effect instanceof SequenceEffect sequence) {
            return sequence.steps().stream().anyMatch(DeathTriggerCollectorService::returnsSourceCardFromGraveyard);
        }
        return effect instanceof ReturnSourceCardFromGraveyardToOwnerHandEffect
                || effect instanceof ReturnSourceTransformedFromGraveyardEffect;
    }

    private boolean effectReferencesEventValue(CardEffect effect) {
        if (effect instanceof SequenceEffect sequence) {
            return sequence.steps().stream().anyMatch(this::effectReferencesEventValue);
        }
        if (effect.referencesEventValue()) {
            return true;
        }
        if (effect instanceof LookAtTopCardsEffect lookAtTopCards) {
            return amountEvaluationService.referencesEventValue(lookAtTopCards.lookCount());
        }
        if (effect instanceof LoseLifeEffect loseLife) {
            return amountEvaluationService.referencesEventValue(loseLife.amount());
        }
        if (effect instanceof GainLifeEffect gainLife) {
            return amountEvaluationService.referencesEventValue(gainLife.amount());
        }
        return false;
    }

    // ── ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD ──────────────────────

    @CollectsWatchTrigger(value = ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect.class,
            watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD)
    boolean handleReturnEnchantedPermanentOnDeathOrExile(TriggerMatchContext match,
            ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentLeaves epl = (TriggerContext.EnchantedPermanentLeaves) ctx;
        if (epl.destination() != com.github.laxika.magicalvibes.model.Zone.GRAVEYARD
                && epl.destination() != com.github.laxika.magicalvibes.model.Zone.EXILE) {
            return false;
        }
        UUID cardId = epl.leavingPermanent().getCard().getId();
        var entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect(
                        cardId, epl.destination())))
        );
        if (epl.destination() == com.github.laxika.magicalvibes.model.Zone.GRAVEYARD) {
            // CR 400.7: the trigger refers to the card's stay in the graveyard it just entered; one that leaves and
            // comes back before this resolves is a new object.
            entry.setTriggeringCardId(cardId);
            entry.setTriggeringCardGraveyardEntryVersion(match.gameData().graveyardEntryVersion(cardId));
        }
        match.gameData().stack.add(entry);
        logEnchantedPermanentLTB(match);
        return true;
    }

    @CollectsWatchTrigger(value = EnchantedControllerSacrificesCreatureOnLeaveEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD)
    boolean handleEnchantedControllerSacrificesCreatureOnLeave(TriggerMatchContext match,
            EnchantedControllerSacrificesCreatureOnLeaveEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentLeaves epl = (TriggerContext.EnchantedPermanentLeaves) ctx;
        // Bake in the player who controlled the leaving creature (its controller sacrifices — not the
        // Aura controller, which differs when the Aura enchants an opponent's creature).
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(
                        new EnchantedControllerSacrificesCreatureOnLeaveEffect(epl.leavingControllerId())))
        ));
        logEnchantedPermanentLTB(match);
        return true;
    }

    @CollectsWatchTrigger(value = ReturnSourceAuraToChosenCreatureOnLeaveEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD)
    boolean handleReturnSourceAuraToChosenCreatureOnLeave(TriggerMatchContext match,
            ReturnSourceAuraToChosenCreatureOnLeaveEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentLeaves epl = (TriggerContext.EnchantedPermanentLeaves) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new ReturnSourceAuraToChosenCreatureOnLeaveEffect(
                        epl.leavingControllerId())))
        ));
        logEnchantedPermanentLTB(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD)
    boolean handleEnchantedPermanentLeavesDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        ));
        logEnchantedPermanentLTB(match);
        return true;
    }

    private void logEnchantedPermanentLTB(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent left the battlefield)."));
        log.info("Game {} - {} triggers (enchanted permanent left the battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ────────────

    @CollectsWatchTrigger(value = DealDamageToPlayersEffect.class,
            watch = ZoneChangeWatch.ON_ANY_NONCREATURE_ARTIFACT_SACRIFICED_OR_DESTROYED)
    boolean handleNoncreatureArtifactSacrificedOrDestroyedDamage(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on a noncreature artifact being sacrificed or destroyed",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsWatchTrigger(value = ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect.class,
            watch = ZoneChangeWatch.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardReturnUnlessDamage(TriggerMatchContext match,
            ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect effect, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard artifactGraveyard = (TriggerContext.ArtifactGraveyard) ctx;
        Card artifactCard = artifactGraveyard.artifactCard();
        if (artifactCard == null || artifactCard.isToken()
                || !match.controllerId().equals(artifactGraveyard.graveyardOwnerId())) {
            return false;
        }

        ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect baked =
                new ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect(
                        effect.damage(), artifactCard.getId());
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(baked)),
                true, match.permanent().getCard().getTargetFilter(), 0, match.permanent().getId(),
                new Permanent(match.permanent())));
        logArtifactGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        // A "you may" wrapping a targeted effect (Disciple of the Vault) chooses its target as the
        // trigger goes on the stack (CR 603.3d), before the may decision is asked on resolution.
        boolean targeted = may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || may.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || graveyardTargetingSupport.findTarget(List.of(may)) != null;
        if (targeted) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(may)),
                    null, new Permanent(match.permanent()), match.permanent().getCard().getTargetFilter()));
            logArtifactGraveyard(match);
            return true;
        }
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                null, match.permanent().getId());
        logArtifactGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = DealDamageToPlayersEffect.class, watch = ZoneChangeWatch.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardDamageController(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard ag = (TriggerContext.ArtifactGraveyard) ctx;
        // "Whenever another artifact is put into a graveyard from the battlefield" (Magnetic Mine).
        if (ag.artifactCard() != null && (ag.artifactCard().getId().equals(match.permanent().getCard().getId())
                || ag.artifactCard().getId().equals(match.permanent().getOriginalCard().getId()))) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                ag.artifactControllerId(),
                match.permanent().getId()
        ));
        logArtifactGraveyard(match);
        return true;
    }

    /**
     * "Whenever an artifact [you control] is put into a graveyard from the battlefield": the artifact, as it last
     * existed on the battlefield (CR 603.10a), is the stack entry's triggering permanent, a graveyard target's mana
     * value bound is the artifact's, and its target is chosen as it goes on the stack (CR 603.3d).
     */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard ag = (TriggerContext.ArtifactGraveyard) ctx;
        CardEffect triggerEffect = effect;
        if (triggerEffect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                || graveyardTargetingSupport.findTarget(List.of(triggerEffect)) != null) {
            CardEffect baked = snapshotArtifactManaValue(triggerEffect, ag.artifactManaValue());
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(baked)))
                    .withTriggeringPermanent(ag.artifactPermanent()));
            logArtifactGraveyard(match);
            return true;
        }

        if (triggerEffect.targetSpec().declaredTarget() == null) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(triggerEffect)),
                    null,
                    match.permanent().getId()
            );
            entry.setTriggeringPermanent(ag.artifactPermanent());
            match.gameData().stack.add(entry);
            logArtifactGraveyard(match);
            return true;
        }

        boolean playerTargetOnly = !triggerEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
        // "up to one target" (minimum 0): the controller may decline by choosing themselves.
        int targetIndex = match.permanent().getCard().getEffectTargetIndex(match.rawEffect());
        boolean optionalTarget = targetIndex >= 0
                && targetIndex < match.permanent().getCard().getTargetGroups().size()
                && match.permanent().getCard().getTargetGroups().get(targetIndex).getMinTargets() == 0;
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(),
                new ArrayList<>(List.of(triggerEffect)), playerTargetOnly,
                match.permanent().getCard().getTargetFilter(), 0, match.permanent().getId(),
                new Permanent(match.permanent()), optionalTarget, null, null, match.controllerId(),
                null, null).withDepartedPermanent(ag.artifactPermanent()));
        logArtifactGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_ARTIFACT_EXILED_FROM_BATTLEFIELD)
    boolean handleArtifactExiledDefault(TriggerMatchContext match,
                                        CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (artifact exiled from battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    private void logArtifactGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (artifact put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ────────────────

    @CollectsWatchTrigger(value = DealDamageToPlayersEffect.class, watch = ZoneChangeWatch.ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleLandGraveyardDamageController(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        TriggerContext.AnyLandGraveyard lg = (TriggerContext.AnyLandGraveyard) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                lg.landControllerId(),
                match.permanent().getId()
        ));
        logLandGraveyard(match);
        return true;
    }

    /** The land's controller rides along for "that land's controller" (Thendar, the Overminer). */
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleLandGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.AnyLandGraveyard land = (TriggerContext.AnyLandGraveyard) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        entry.setTriggeringPermanentControllerId(land.landControllerId());
        match.gameData().stack.add(entry);
        logLandGraveyard(match);
        return true;
    }

    private void logLandGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (land put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ─────────

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleEnchantmentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logEnchantmentGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAllyEnchantmentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logEnchantmentGraveyard(match);
        return true;
    }

    private void logEnchantmentGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (enchantment put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD ───────

    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_ARTIFACT_PUT_INTO_OPPONENT_OWNER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentArtifactGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logOpponentArtifactGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_ARTIFACT_PUT_INTO_OPPONENT_OWNER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentArtifactGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logOpponentArtifactGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = DealDamageToPlayersEffect.class,
            watch = ZoneChangeWatch.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentArtifactGraveyardDamage(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        if (effect.recipient() != DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER) {
            return handleOpponentArtifactGraveyardDefault(match, effect, ctx);
        }

        TriggerContext.ArtifactGraveyard artifactGraveyard = (TriggerContext.ArtifactGraveyard) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                artifactGraveyard.artifactControllerId(),
                match.permanent().getId()
        );
        match.gameData().stack.add(entry);
        logOpponentArtifactGraveyard(match);
        return true;
    }

    private void logOpponentArtifactGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (opponent artifact put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE ────────

    @CollectsWatchTrigger(value = MayEffect.class, watch = ZoneChangeWatch.ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    boolean handleBlackCardOpponentGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logBlackCardOpponentGraveyard(match);
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class, watch = ZoneChangeWatch.ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    boolean handleBlackCardOpponentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logBlackCardOpponentGraveyard(match);
        return true;
    }

    private void logBlackCardOpponentGraveyard(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (black card put into opponent's graveyard from anywhere)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT ────────────────────

    @CollectsWatchTrigger(value = ReturnTriggeringLandFromGraveyardToBattlefieldEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT)
    boolean handleLandGraveyardReturn(TriggerMatchContext match,
            ReturnTriggeringLandFromGraveyardToBattlefieldEffect effect, TriggerContext ctx) {
        TriggerContext.LandPutIntoGraveyard lpg = (TriggerContext.LandPutIntoGraveyard) ctx;
        Card landCard = lpg.landCard();
        ReturnTriggeringLandFromGraveyardToBattlefieldEffect concrete =
                new ReturnTriggeringLandFromGraveyardToBattlefieldEffect(
                        landCard.getId(), false, match.gameData().graveyardEntryVersion(landCard.getId()));
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(concrete)),
                null,
                match.permanent().getId()
        ));

        gameLogService.append(match.gameData(), GameLog.cardTextCard(match.permanent().getCard(),
                "'s ability triggers (", landCard, " was put into a graveyard from the battlefield by an opponent)."));
        log.info("Game {} - {} triggers (land {} put into graveyard by opponent)",
                match.gameData().id, match.permanent().getCard().getName(), landCard.getName());
        return true;
    }

    // ── ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ──────

    @CollectsWatchTrigger(value = PutCountersOnSelfEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    private boolean handleOpponentPermanentGraveyardPutCountersOnSelf(TriggerMatchContext match,
            PutCountersOnSelfEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentPermanentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    @CollectsWatchTrigger(value = MayEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT)
    private boolean handleNoncreaturePermanentDestroyedByOpponent(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        GameData gameData = match.gameData();
        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(may)), false,
                match.permanent().getCard().getTargetFilter(), 0, match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers after an opponent destroys a noncreature permanent",
                gameData.id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = StealDyingOpponentPermanentUnlessPaysLifeEffect.class,
            watch = ZoneChangeWatch.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentPermanentGraveyardSteal(TriggerMatchContext match,
            StealDyingOpponentPermanentUnlessPaysLifeEffect effect, TriggerContext ctx) {
        TriggerContext.OpponentPermanentGraveyard opg = (TriggerContext.OpponentPermanentGraveyard) ctx;
        // "That card" is the entry's triggering card and "that opponent" the controller of its triggering permanent.
        // The "unless pays" branch is a resolution-time choice made by that opponent (CR 603.5), so this stacks as a
        // plain triggered ability.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        );
        markDyingCard(match.gameData(), entry, opg.dyingCard());
        entry.setTriggeringPermanent(opg.dyingPermanent());
        entry.setTriggeringPermanentControllerId(opg.dyingControllerId());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (opponent permanent put into graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnTopOfLibraryEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleCreaturePutIntoOwnerGraveyardToLibrary(TriggerMatchContext match,
            com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnTopOfLibraryEffect effect,
            TriggerContext ctx) {
        var death = (TriggerContext.AnyPermanentGraveyard) ctx;
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(), match.controllerId(), match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null, match.permanent().getId());
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        return true;
    }

    /** Bakes the dead card's owner into graveyard-to-hand triggers; the card is the entry's triggering card. */
    @CollectsWatchTrigger(value = ReturnTriggeringCardToOwnerHandEffect.class,
            watch = ZoneChangeWatch.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    @CollectsWatchTrigger(value = ReturnTriggeringCardToOwnerHandEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    @CollectsWatchTrigger(value = ReturnTriggeringCardToOwnerHandEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handlePermanentGraveyardReturnToHand(TriggerMatchContext match,
            ReturnTriggeringCardToOwnerHandEffect effect, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        ReturnTriggeringCardToOwnerHandEffect baked = new ReturnTriggeringCardToOwnerHandEffect(
                apg.graveyardOwnerId());
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(baked)),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, apg.dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (permanent {} put into graveyard from battlefield)",
                match.gameData().id, match.permanent().getCard().getName(), apg.dyingCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = MayPayManaEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleCreaturePutIntoOwnerGraveyardMayPay(TriggerMatchContext match,
            MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), apg.graveyardOwnerId(), mayPay, null);
        // The payment may be answered after the card has left and re-entered a graveyard; the
        // trigger only refers to the graveyard stay it was created for (CR 400.7).
        markDyingCard(match.gameData(), match.gameData().stack.getLast(), apg.dyingCard());
        return true;
    }

    @CollectsWatchTrigger(value = CreateTokenEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleCreaturePutIntoOwnerGraveyardCreateToken(TriggerMatchContext match,
            CreateTokenEffect effect, TriggerContext ctx) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        match.gameData().stack.add(entry);
        return true;
    }

    boolean handleEquippedCreatureReturnToHand(TriggerMatchContext match,
            ReturnTriggeringCardToOwnerHandEffect effect, TriggerContext ctx) {
        TriggerContext.EquippedCreatureDeath ecd = (TriggerContext.EquippedCreatureDeath) ctx;
        if (ecd.dyingCard() == null) {
            return false;
        }
        UUID handOwnerId = gameQueryService.findGraveyardOwnerById(match.gameData(), ecd.dyingCard().getId());
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new ReturnTriggeringCardToOwnerHandEffect(handOwnerId))),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, ecd.dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (equipped creature returns to hand)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect.class,
            watch = ZoneChangeWatch.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleDyingCreatureReturnUnlessTargetPaysLife(TriggerMatchContext match,
            ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect effect, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        // "It" is the entry's triggering card; the target is chosen as the ability goes on the stack (CR 603.3d).
        match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                null, new Permanent(match.permanent()), match.permanent().getCard().getTargetFilter())
                .withTriggeringCard(apg.dyingCard().getId(),
                        match.gameData().graveyardEntryVersion(apg.dyingCard().getId())));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (owned creature put into graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAnyPermanentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                apg.graveyardOwnerId(),
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (permanent put into a graveyard from the battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    boolean handleAllyArtifactOrCreatureDeath(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (controlled artifact or creature put into a graveyard from the battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ANY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAnyNontokenPermanentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                apg.graveyardOwnerId(),
                match.permanent().getId()
        );
        // The graveyard's owner is "that permanent's owner", not a target of the ability.
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (nontoken permanent put into a graveyard from the battlefield)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAllyNontokenPermanentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (nontoken permanent put into controller's graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsWatchTrigger(value = RegisterDelayedReturnCurseAttachedToPlayerEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    @CollectsWatchTrigger(value = RegisterDelayedReturnCurseAttachedToPlayerEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAllyCurseDelayedReturn(TriggerMatchContext match,
            RegisterDelayedReturnCurseAttachedToPlayerEffect effect, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard death = (TriggerContext.AnyPermanentGraveyard) ctx;
        if (death.dyingCard() == null) {
            return true;
        }
        return handleAllyPermanentGraveyardDefault(match,
                new RegisterDelayedReturnCurseAttachedToPlayerEffect(
                        death.dyingCard().getId(), match.controllerId()), ctx);
    }

    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_PERMANENT_PUT_INTO_YOUR_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleAllyPermanentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        if (effect.targetSpec().declaredTarget() != null) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    null, new Permanent(match.permanent()), false));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        if (ctx instanceof TriggerContext.AnyPermanentGraveyard death && death.dyingPermanent() != null) {
            // "It": the permanent put into the graveyard, as it last existed (Martyr's Bond).
            entry.setTriggeringPermanent(death.dyingPermanent());
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (permanent put into controller's graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * Patron of the Nezumi. "That player" is the owner of the graveyard the permanent was put into,
     * so the entry's target is baked to the graveyard owner rather than the dying permanent's
     * controller — a permanent an opponent owns but you control still hits its owner.
     */
    @CollectsWatchTrigger(value = CardEffect.class,
            watch = ZoneChangeWatch.ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handlePermanentIntoOpponentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.AnyPermanentGraveyard apg = (TriggerContext.AnyPermanentGraveyard) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                apg.graveyardOwnerId(),
                match.permanent().getId()
        );
        // "Its toughness" (Grim Feast): the permanent as it last existed on the battlefield.
        markDyingCard(match.gameData(), entry, apg.dyingCard());
        entry.setTriggeringPermanent(apg.dyingPermanent());
        entry.setTriggeringPermanentToughnessAtTrigger(apg.dyingToughness());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (permanent {} put into an opponent's graveyard)",
                match.gameData().id, match.permanent().getCard().getName(), apg.dyingCard().getName());
        return true;
    }

    boolean handleAnyCreatureDeathReturnTriggeringPermanent(TriggerMatchContext match,
            ReturnTriggeringPermanentToBattlefieldEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CreatureDeath death)
                || effect.fromZone() != Zone.GRAVEYARD
                || death.dyingCard() == null) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathBoostSelf(TriggerMatchContext match,
            BoostSelfEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathPutCounters(TriggerMatchContext match,
            PutCountersOnSourceEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathPutCountersOnSelf(TriggerMatchContext match,
            PutCountersOnSelfEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        if (amountEvaluationService.referencesEventValue(effect.amount())) {
            entry.setEventValue(death.dyingCreaturePower());
        }
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathPutCountersEqualToPower(TriggerMatchContext match,
            PutCountersOnSourceEqualToDyingPowerEffect effect, TriggerContext ctx) {
        // Kresh the Bloodbraided: "you may put X +1/+1 counters on this creature, where X is that
        // creature's power." The dying creature's last-known power is snapshotted into the context;
        // bake it into a concrete PutCountersOnSourceEffect that resolves onto this permanent.
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        int power = Math.max(0, cd.dyingCreaturePower());
        var counters = new PutCountersOnSourceEffect(effect.powerModifier(), effect.toughnessModifier(), power);
        if (effect.optional()) {
            var may = new MayEffect(counters, "Put " + power + " +1/+1 counter(s) on this creature?");
            match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                    null, match.permanent().getId());
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(counters)),
                    null,
                    match.permanent().getId()));
        }
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathUntap(TriggerMatchContext match,
            UntapPermanentsEffect effect, TriggerContext ctx) {
        // "Whenever another creature dies, untap this creature." SELF-scope untap needs its source
        // permanent id to locate this creature at resolution (Galvanic Juggernaut).
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathTap(TriggerMatchContext match,
            TapPermanentsEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathTapOrUntap(TriggerMatchContext match,
            TapOrUntapCreaturesSharingDyingCreatureTypeEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        Set<CardSubtype> dyingCreatureTypes = dyingCreatureTypes(match.gameData(), death);
        PermanentPredicate matchingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasAnySubtypePredicate(dyingCreatureTypes)));
        ChooseOneEffect modal = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Tap all untapped creatures that share a creature type with it",
                        new TapPermanentsEffect(TapUntapScope.ALL_CREATURES, matchingCreature)),
                new ChooseOneEffect.ChooseOneOption(
                        "Untap all tapped creatures that share a creature type with it",
                        new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES, matchingCreature))
        ));
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new ChooseOneAtResolutionEffect(modal))),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        // CR 603.3d: a targeted "may" ability (e.g. Vicious Shadows' "you may have this deal damage
        // to target player") needs its target chosen when the trigger is put on the stack.
        Card dyingCard = ctx instanceof TriggerContext.CreatureDeath death ? death.dyingCard() : null;
        if (may.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || may.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(may)),
                    null, match.permanent()
            );
            match.gameData().queueInteraction(dyingCard == null ? target : target.withTriggeringCard(
                    dyingCard.getId(), match.gameData().graveyardEntryVersion(dyingCard.getId())));
        } else {
            // Pass the source permanent id so wrapped source-referencing effects (e.g.
            // PutCountersOnSourceEffect for Scavenger Drake's optional +1/+1 counter) can resolve.
            match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                    null, match.permanent().getId());
            markDyingCard(match.gameData(), match.gameData().stack.getLast(), dyingCard);
        }
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathDyingControllerDraws(TriggerMatchContext match,
            DyingCreatureControllerMayDrawCardEffect effect, TriggerContext ctx) {
        // Fecundity: the dying creature's controller (not the source's controller) may draw.
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(), match.controllerId(), match.permanent().getCard().getName() + "'s ability",
                List.of(MayEffect.forPlayer(new com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect(1),
                        "Draw a card?", cd.dyingCreatureControllerId())),
                cd.dyingCreatureControllerId(), match.permanent().getId());
        entry.setNonTargeting(true);
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        match.gameData().enqueueTrigger(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathDrawsForAttachedAuras(TriggerMatchContext match,
            DrawCardForEachAuraAttachedToDyingCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        if (cd.dyingPermanentId() == null) {
            return false;
        }

        int auraCount = countAurasAttachedToDyingCreature(
                match.gameData(), cd.dyingPermanentId(), match.controllerId());
        if (auraCount == 0) {
            return false;
        }

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DrawCardEffect(auraCount))),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    private int countAurasAttachedToDyingCreature(GameData gameData, UUID dyingPermanentId,
                                                   UUID auraControllerId) {
        int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().isAura()
                    && dyingPermanentId.equals(permanent.getAttachedTo())
                    && auraControllerId.equals(playerId)) {
                count[0]++;
            }
        });

        gameData.simultaneousDyingPermanents.forEach((permanentId, permanent) -> {
            if (gameQueryService.findPermanentById(gameData, permanentId) != null) {
                return;
            }
            if (permanent.getCard().isAura()
                    && dyingPermanentId.equals(permanent.getAttachedTo())
                    && auraControllerId.equals(gameData.simultaneousDyingPermanentControllers.get(permanentId))) {
                count[0]++;
            }
        });
        return count[0];
    }

    boolean handleAnyCreatureDeathDyingControllerDiscards(TriggerMatchContext match,
            DyingCreatureControllerDiscardsCardEffect effect, TriggerContext ctx) {
        // Bereavement: the dying creature's controller (not the source's controller) discards a card.
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DiscardEffect(1, DiscardRecipient.TRIGGERING_PLAYER))),
                cd.dyingCreatureControllerId(),
                match.permanent().getId());
        entry.setNonTargeting(true);
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathDyingControllerSacrifices(TriggerMatchContext match,
            DyingCreatureControllerSacrificesPermanentsEffect effect, TriggerContext ctx) {
        // Earthlink: the dying creature's controller (not the source's controller) sacrifices.
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new SacrificePermanentsEffect(
                        effect.count(), effect.filter(), SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER)))
        );
        entry.setTriggeringPermanentControllerId(cd.dyingCreatureControllerId());
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathBountyReward(TriggerMatchContext match,
            EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.CreatureDeath death)
                || death.dyingPermanent() == null
                || death.dyingPermanent().getCounterCount(CounterType.BOUNTY) < 1) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                death.dyingCreatureControllerId(),
                match.permanent().getId()
        );
        entry.setNonTargeting(true);
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    @CollectsEmblemTrigger(EmblemCreatureDeathTriggerEffect.class)
    boolean handleEmblemCreatureDeath(EmblemTriggerMatchContext match,
            EmblemCreatureDeathTriggerEffect effect, TriggerContext ctx) {
        Card source = match.emblem().sourceCard();
        if (source == null) {
            return false;
        }

        GameData gameData = match.gameData();
        List<CardEffect> effects = new ArrayList<>(effect.effects());
        if (effects.stream().anyMatch(cardEffect ->
                cardEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || cardEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
            gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    source, match.controllerId(), effects, null, null, effect.targetFilter()));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source,
                    match.controllerId(),
                    source.getName() + "'s emblem ability",
                    effects));
        }
        return true;
    }

    @CollectsEmblemTrigger(EmblemArtifactGraveyardReturnTriggerEffect.class)
    boolean handleEmblemArtifactGraveyardReturn(EmblemTriggerMatchContext match,
            EmblemArtifactGraveyardReturnTriggerEffect effect, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard artifactGraveyard = (TriggerContext.ArtifactGraveyard) ctx;
        Card artifactCard = artifactGraveyard.artifactCard();
        if (artifactCard == null || artifactCard.isToken()) {
            return false;
        }

        Card source = match.emblem().sourceCard();
        String description = (source != null ? source.getName() : "Emblem") + "'s emblem";
        RegisterDelayedReturnDyingCreatureUnderControlEffect delayedReturn =
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                        false, null, 0, null, null, false, true, false, false, false);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source != null ? source : artifactCard,
                match.controllerId(),
                description,
                new ArrayList<>(List.of(delayedReturn)));
        markDyingCard(match.gameData(), entry, artifactCard);
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.text(description + " triggers (an artifact was put into a graveyard)."));
        log.info("Game {} - {} schedules a delayed return of {}", match.gameData().id, description, artifactCard.getName());
        return true;
    }

    boolean handleAnyCreatureDeathDamageController(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        if (effect.recipient() != DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER) {
            return handleAnyCreatureDeathDefault(match, effect, ctx);
        }

        // Dingus Staff: bake the dying creature's controller as targetId.
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                cd.dyingCreatureControllerId(),
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathDyingControllerLosesLife(TriggerMatchContext match,
            LoseLifeEffect effect, TriggerContext ctx) {
        if (effect.recipient() != LoseLifeRecipient.DYING_CREATURE_CONTROLLER) {
            return handleAnyCreatureDeathDefault(match, effect, ctx);
        }

        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                cd.dyingCreatureControllerId(),
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathCreateTokenCopy(TriggerMatchContext match,
            CreateTokenCopyOfDyingCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        if (death.dyingCard() == null) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        markDyingCard(match.gameData(), entry, death.dyingCard());
        entry.setTriggeringPermanentId(death.dyingPermanentId());
        entry.rememberLastKnownPermanentCard(death.dyingPermanentId(), death.dyingCard());
        entry.setTriggeringPermanentPowerAtTrigger(death.dyingCreaturePower());
        entry.setEventValue(death.dyingCreaturePower());
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        GameData gameData = match.gameData();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    null, new Permanent(match.permanent())
            );
            if (ctx instanceof TriggerContext.CreatureDeath death && death.dyingCard() != null) {
                target = target.withTriggeringCard(death.dyingCard().getId(),
                        gameData.graveyardEntryVersion(death.dyingCard().getId()))
                        .withTriggeringPermanent(death.dyingPermanent());
            }
            gameData.queueInteraction(target);
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId()
            );
            if (ctx instanceof TriggerContext.CreatureDeath death) {
                if (death.dyingCard() != null) {
                    markDyingCard(gameData, entry, death.dyingCard());
                    if (death.dyingPermanentId() != null) {
                        entry.rememberLastKnownPermanentCard(death.dyingPermanentId(), death.dyingCard());
                    }
                }
                entry.setTriggeringPermanentId(death.dyingPermanentId());
                entry.setTriggeringPermanent(death.dyingPermanent());
                entry.setTriggeringPermanentControllerId(death.dyingCreatureControllerId());
                entry.setTriggeringPermanentPowerAtTrigger(death.dyingCreaturePower());
                entry.setEventValue(death.dyingCreaturePower());
            }
            gameData.stack.add(entry);
        }
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathDelayedReturnUnderOwnersControl(TriggerMatchContext match,
            RegisterDelayedReturnDyingCreatureUnderControlEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        if (death.dyingCard() == null
                || (effect.requireAnotherCreature() && !hasAnotherCreatureAtDeath(match, death))) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    private boolean hasAnotherCreatureAtDeath(TriggerMatchContext match, TriggerContext.CreatureDeath death) {
        GameData gameData = match.gameData();
        UUID sourcePermanentId = match.permanent().getId();
        UUID dyingPermanentId = death.dyingPermanentId();
        if (gameData.anyPermanentMatches(permanent ->
                !permanent.getId().equals(dyingPermanentId)
                        && gameQueryService.isCreature(gameData, permanent))) {
            return true;
        }
        return gameData.simultaneousDyingCreatures.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(sourcePermanentId)
                        && !entry.getKey().equals(dyingPermanentId))
                .anyMatch(entry -> gameQueryService.isCreature(gameData, entry.getValue()));
    }

    boolean handleAnyCreatureDeathExileLinkedCreature(TriggerMatchContext match,
            ExileTriggeringCreatureAndReturnSourceToHandEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        UUID linkedPermanentId = match.permanent().getChosenPermanentId();
        if (linkedPermanentId == null || !linkedPermanentId.equals(death.dyingPermanentId())
                || death.dyingCard() == null) {
            return false;
        }

        // "That creature" is the entry's triggering card.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, death.dyingCard());
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathDestroyEnchanted(TriggerMatchContext match,
            DestroyReferencedPermanentEffect effect, TriggerContext ctx) {
        // Yoke of the Damned: the Aura may leave the battlefield in the same event that triggered it, so the
        // snapshot keeps the creature it was attached to for the destroy to find after the Aura is gone.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathCounterOnEnchanted(TriggerMatchContext match,
            PutCounterOnReferencedPermanentEffect effect, TriggerContext ctx) {
        // Sadistic Glee: needs its source Aura's permanent id to re-derive the enchanted creature.
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAnyCreatureDeathConditionalReplacement(TriggerMatchContext match,
            ConditionalReplacementEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    private void logAnyCreatureDeath(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (any creature died)", match.gameData().id, match.permanent().getCard().getName());
    }

    private Set<CardSubtype> dyingCreatureTypes(GameData gameData, TriggerContext.CreatureDeath death) {
        Permanent dyingPermanent = death.dyingPermanent();
        if (dyingPermanent == null) {
            return Set.of();
        }
        if (gameQueryService.hasKeyword(gameData, dyingPermanent, Keyword.CHANGELING)) {
            EnumSet<CardSubtype> allCreatureTypes = EnumSet.noneOf(CardSubtype.class);
            for (CardSubtype subtype : CardSubtype.values()) {
                if (gameQueryService.isCreatureSubtype(subtype)) {
                    allCreatureTypes.add(subtype);
                }
            }
            return Set.copyOf(allCreatureTypes);
        }
        return Set.copyOf(gameQueryService.effectiveCreatureSubtypes(gameData, dyingPermanent));
    }

    boolean handleAllyNontokenCreateTokenWithTotalDyingCreaturesPower(
            TriggerMatchContext match, CreateTokenWithTotalDyingCreaturesPowerEffect effect,
            TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        if (!isFirstNontokenDeathInSimultaneousEvent(match.gameData(), death)) {
            return false;
        }

        int totalPower = totalPowerOfSimultaneousNontokenDeaths(match.gameData(), death);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                death.dyingCreatureControllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        entry.setEventValue(totalPower);
        match.gameData().stack.add(entry);
        logAllyNontokenCreatureDeath(match);
        return true;
    }

    private boolean isFirstNontokenDeathInSimultaneousEvent(GameData gameData,
                                                              TriggerContext.CreatureDeath death) {
        if (gameData.simultaneousDyingCreatures.isEmpty()) {
            return true;
        }

        UUID firstPermanentId = null;
        for (Map.Entry<UUID, Permanent> entry : gameData.simultaneousDyingCreatures.entrySet()) {
            if (!death.dyingCreatureControllerId().equals(
                    gameData.simultaneousDyingControllers.get(entry.getKey()))
                    || entry.getValue().getCard().isToken()) {
                continue;
            }
            if (firstPermanentId == null || entry.getKey().compareTo(firstPermanentId) < 0) {
                firstPermanentId = entry.getKey();
            }
        }
        return death.dyingPermanentId() != null && death.dyingPermanentId().equals(firstPermanentId);
    }

    private int totalPowerOfSimultaneousNontokenDeaths(GameData gameData,
                                                        TriggerContext.CreatureDeath death) {
        if (gameData.simultaneousDyingCreatures.isEmpty()) {
            return death.dyingCreaturePower();
        }

        int totalPower = 0;
        for (Map.Entry<UUID, Permanent> entry : gameData.simultaneousDyingCreatures.entrySet()) {
            if (!death.dyingCreatureControllerId().equals(
                    gameData.simultaneousDyingControllers.get(entry.getKey()))
                    || entry.getValue().getCard().isToken()) {
                continue;
            }
            Integer power = gameData.simultaneousDyingPowers.get(entry.getKey());
            if (power == null) {
                power = entry.getKey().equals(death.dyingPermanentId())
                        ? death.dyingCreaturePower() : entry.getValue().getEffectivePower();
            }
            totalPower += power;
        }
        return totalPower;
    }

    boolean handleAllyNontokenMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        // "That card": the card that went to the graveyard, for a copy the physical card.
        UUID dyingCardId = cd.dyingCard() == null ? null : cd.dyingPermanent() == null ? cd.dyingCard().getId()
                : cd.dyingPermanent().getOriginalCard().getId();
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), may,
                dyingCardId);
        // "That creature" (its name, its counters) as it last existed on the battlefield (CR 603.10a).
        match.gameData().stack.getLast().setTriggeringPermanent(cd.dyingPermanent() != null ? cd.dyingPermanent()
                : cd.dyingCard() == null ? null : new Permanent(cd.dyingCard()));
        logAllyNontokenCreatureDeath(match);
        return true;
    }

    boolean handleAllyNontokenMayPay(TriggerMatchContext match,
            MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), mayPay,
                null, match.permanent().getId());
        markDyingCard(match.gameData(), match.gameData().stack.getLast(), cd.dyingCard());
        return true;
    }

    boolean handleAnyCreatureDeathDyingControllerSearchesSameName(TriggerMatchContext match,
            DyingCreatureControllerMaySearchLibraryForSameNameEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        String dyingName = cd.dyingCard().getName();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new MayEffect(
                        new SearchTargetLibraryEffect(1, new CardNamedPredicate(dyingName),
                                LibrarySearchDestination.BATTLEFIELD_UNDER_SEARCHER, true,
                                LibrarySearchPlayer.TARGET_PLAYER),
                        "Search your library for a card named " + dyingName
                                + " and put it onto the battlefield?",
                        null, MayChoicePlayer.TARGET_PLAYER))),
                cd.dyingCreatureControllerId(),
                match.permanent().getId()
        );
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleAllyNontokenDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        if (effect.targetSpec().declaredTarget() != null) {
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)));
            match.gameData().queueInteraction((cd.dyingCard() == null ? target : target.withTriggeringCard(
                    cd.dyingCard().getId(), match.gameData().graveyardEntryVersion(cd.dyingCard().getId())))
                    .withTriggeringPermanent(cd.dyingPermanent()));
            logAllyNontokenCreatureDeath(match);
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                cd.dyingCreatureControllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        Card dyingCard = cd.dyingPermanent() != null ? cd.dyingPermanent().getCard() : cd.dyingCard();
        if (dyingCard != null) {
            entry.setDyingPermanentManaValue(dyingCard.getManaValue());
        }
        // "That card" (Purgatory, Hofri, Ghostforge): the creature that died.
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        entry.setTriggeringPermanent(cd.dyingPermanent());
        match.gameData().stack.add(entry);
        logAllyNontokenCreatureDeath(match);
        return true;
    }

    boolean handleAllyNontokenInterveningIfConditional(TriggerMatchContext match,
            ConditionalEffect conditional, TriggerContext ctx) {
        return handleAllyNontokenDefault(match, conditional, ctx);
    }

    private void logAllyNontokenCreatureDeath(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (ally nontoken creature died)", match.gameData().id, match.permanent().getCard().getName());
    }

    boolean handleAnyNontokenCreatureDeathExileAndTrack(TriggerMatchContext match,
            ExileTriggeringCreatureAndTrackWithSourceEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        if (!(match.rawEffect() instanceof MayEffect rawMay)) {
            if (cd.dyingCard() == null
                    || match.gameData().playerGraveyards
                    .getOrDefault(match.controllerId(), List.of()).stream()
                    .noneMatch(card -> card.getId().equals(cd.dyingCard().getId()))) {
                return true;
            }
            return handleAnyNontokenCreatureDeathDefault(match, effect, ctx);
        }
        MayEffect resolvedMay = new MayEffect(effect, rawMay.prompt(), rawMay.elseEffect());
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(),
                resolvedMay, null, match.permanent().getId());
        markDyingCard(match.gameData(), match.gameData().stack.getLast(), cd.dyingCard());
        logAnyCreatureDeath(match);
        return true;
    }

    boolean handleImprintDyingCreature(TriggerMatchContext match,
            ImprintDyingCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        MayEffect rawMay = (MayEffect) match.rawEffect();
        // CR 603.5: the ability goes on the stack and "you may exile that card" is chosen as it resolves. The
        // source permanent is carried as the entry's (non-targeting) target: the imprint still happens if it left.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new MayEffect(effect, rawMay.prompt()))),
                match.permanent().getId(),
                match.permanent().getId());
        entry.setNonTargeting(true);
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s imprint ability triggers."));
        log.info("Game {} - {} imprint triggers (nontoken creature died)", match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    boolean handleReturnDyingCreatureMayPay(TriggerMatchContext match,
            ReturnDyingCreatureToBattlefieldEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        GameData gameData = match.gameData();
        List<Card> playerGraveyard = gameData.playerGraveyards.get(match.controllerId());
        if (playerGraveyard == null || playerGraveyard.stream().noneMatch(c -> c.getId().equals(cd.dyingCard().getId()))) {
            return false;
        }
        MayPayManaEffect rawMayPay = (MayPayManaEffect) match.rawEffect();
        // The trigger goes on the stack; the payment is offered when it resolves (CR 603.5).
        MayPayManaEffect mayPay = new MayPayManaEffect(rawMayPay.manaCost(), rawMayPay.dynamicManaCost(),
                effect, rawMayPay.prompt(), rawMayPay.payer(), rawMayPay.elseEffect(), rawMayPay.lifeCost(),
                rawMayPay.sourceIsTriggeringPermanent());
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(mayPay)),
                null,
                match.permanent().getId()
        );
        markDyingCard(gameData, entry, cd.dyingCard());
        match.gameData().stack.add(entry);
        gameLogService.append(gameData, GameLog.cardTextCard(match.permanent().getCard(),
                "'s ability triggers (", cd.dyingCard(), " died)."));
        log.info("Game {} - {} return trigger fires (nontoken creature {} died)", gameData.id, match.permanent().getCard().getName(), cd.dyingCard().getName());
        return true;
    }

    boolean handleAnyNontokenCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        if (ctx instanceof TriggerContext.CreatureDeath death) {
            markDyingCard(match.gameData(), entry, death.dyingCard());
            entry.setTriggeringPermanent(death.dyingPermanent());
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (any nontoken creature died)", match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    boolean handleEnchantedPlayerNontokenCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath death = (TriggerContext.CreatureDeath) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        if (death.dyingCard() != null) {
            entry.setDyingPermanentManaValue(death.dyingCard().getManaValue());
        }
        markDyingCard(match.gameData(), entry, death.dyingCard());
        entry.setTriggeringPermanent(death.dyingPermanent());
        match.gameData().stack.add(entry);
        log.info("Game {} - {} triggers (enchanted player's nontoken creature died)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    boolean handleOpponentCreatureDeathMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        // "You may return target artifact card from your graveyard" (Glissa, the Traitor): the target
        // is chosen as the trigger goes on the stack; the "may" is decided as it resolves.
        if (graveyardTargetingSupport.findTarget(List.of(may)) != null) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(may)),
                    null, new Permanent(match.permanent()), null, null, true));
            logOpponentCreatureDeath(match);
            return true;
        }
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may, null,
                match.permanent().getId(), match.markSourceOncePerTurnOnAcceptance());
        StackEntry entry = match.gameData().stack.getLast();
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        entry.setTriggeringPermanent(cd.dyingPermanent());
        logOpponentCreatureDeath(match);
        return true;
    }

    boolean handleOpponentCreatureDeathPutCountersEqualToPower(TriggerMatchContext match,
            PutCountersOnSourceEqualToDyingPowerEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        int power = Math.max(0, cd.dyingCreaturePower());
        var counters = new PutCountersOnSourceEffect(effect.powerModifier(), effect.toughnessModifier(), power);
        if (effect.optional()) {
            var may = new MayEffect(counters, "Put " + power + " +1/+1 counter(s) on this creature?");
            match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                    null, match.permanent().getId());
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(counters)),
                    null,
                    match.permanent().getId()));
        }
        logOpponentCreatureDeath(match);
        return true;
    }

    boolean handleOpponentCreatureReturnAsTreasure(TriggerMatchContext match,
            ReturnDyingOpponentCreatureAsTreasureEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        GameData gameData = match.gameData();
        Card dyingCard = cd.dyingCard();
        if (dyingCard == null || dyingCard.isToken()) {
            return false;
        }

        MayPayManaEffect rawMayPay = (MayPayManaEffect) match.rawEffect();
        // CR 603.5: the payment is decided as the ability resolves, so the trigger goes on the stack first.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new MayPayManaEffect(rawMayPay.manaCost(), effect,
                        match.permanent().getCard().getName() + " — Pay " + rawMayPay.manaCost()
                                + " to return " + dyingCard.getName()
                                + " to the battlefield under your control as a Treasure?"))),
                null,
                match.permanent().getId());
        markDyingCard(gameData, entry, dyingCard);
        gameData.stack.add(entry);
        gameLogService.append(gameData, GameLog.cardTextCard(match.permanent().getCard(),
                "'s ability triggers (", dyingCard, " died)."));
        logOpponentCreatureDeath(match);
        return true;
    }

    boolean handleOpponentCreatureDelayedReturnUnderControl(TriggerMatchContext match,
            RegisterDelayedReturnDyingCreatureUnderControlEffect effect, TriggerContext ctx) {
        // Grave Betrayal: the delayed return is registered when this trigger resolves, and the
        // handler reads the dying card id from the entry — the slot's default collector does not
        // stamp it, so bake it in here.
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        if (cd.dyingCard() == null) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        match.gameData().stack.add(entry);
        logOpponentCreatureDeath(match);
        return true;
    }

    boolean handleOpponentCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        return queueOpponentCreatureDeath(match, effect, effect, ctx);
    }

    private boolean queueOpponentCreatureDeath(TriggerMatchContext match, CardEffect effect,
                                               CardEffect declaredTargetEffect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        // CR 603.3d: a graveyard-targeting trigger (Toshiro Umezawa's "you may cast target instant card
        // from your graveyard") picks its target as it goes on the stack, and is skipped without one.
        if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect))
            ));
            logOpponentCreatureDeath(match);
            return true;
        }
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            TargetFilter targetFilter = targetFilterForDeathTrigger(
                    match.permanent().getCard(), declaredTargetEffect);
            PermanentChoiceContext.DeathTriggerTarget target = new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    null, null, targetFilter);
            match.gameData().queueInteraction(cd.dyingCard() == null ? target : target.withTriggeringCard(
                    cd.dyingCard().getId(), match.gameData().graveyardEntryVersion(cd.dyingCard().getId())));
            logOpponentCreatureDeath(match);
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                cd.dyingCreatureControllerId(),
                match.permanent().getId()
        );
        // "Exile it" (Mari, the Killing Quill; Patron of the Vein): the creature that died.
        markDyingCard(match.gameData(), entry, cd.dyingCard());
        match.gameData().stack.add(entry);
        logOpponentCreatureDeath(match);
        return true;
    }

    private TargetFilter targetFilterForDeathTrigger(Card sourceCard, CardEffect effect) {
        int targetIndex = sourceCard.getEffectTargetIndex(effect);
        if (targetIndex < 0 || targetIndex >= sourceCard.getTargetGroups().size()) {
            return null;
        }
        return sourceCard.getTargetGroups().get(targetIndex).getFilter();
    }

    private void logOpponentCreatureDeath(TriggerMatchContext match) {
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (opponent creature died)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ──

    @CollectsWatchTrigger(value = RegisterDelayedReturnCardFromGraveyardToHandEffect.class, watch = ZoneChangeWatch.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleRegisterDelayedReturn(TriggerMatchContext match,
            RegisterDelayedReturnCardFromGraveyardToHandEffect effect, TriggerContext ctx) {
        TriggerContext.AllyAuraOrEquipmentGraveyard aaeg = (TriggerContext.AllyAuraOrEquipmentGraveyard) ctx;
        Card dyingCard = aaeg.dyingCard();
        // The trigger itself is mandatory: it schedules a delayed trigger at the next end step, and the
        // "you may return" choice is made when that delayed trigger resolves.
        RegisterDelayedReturnCardFromGraveyardToHandEffect delayedEffect =
                new RegisterDelayedReturnCardFromGraveyardToHandEffect(true);
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                aaeg.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(delayedEffect)),
                null,
                match.permanent().getId());
        markDyingCard(match.gameData(), entry, dyingCard);
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);

        gameLogService.append(match.gameData(), GameLog.cardTextCard(match.permanent().getCard(),
                "'s ability triggers (", dyingCard, " was put into a graveyard from the battlefield)."));
        log.info("Game {} - {} triggers (ally Aura/Equipment {} put into graveyard from battlefield)",
                match.gameData().id, match.permanent().getCard().getName(), dyingCard.getName());
        return true;
    }

    @CollectsWatchTrigger(value = ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffect.class,
            watch = ZoneChangeWatch.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleExileTriggeringAuraMayCast(TriggerMatchContext match,
            ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffect effect, TriggerContext ctx) {
        TriggerContext.AllyAuraOrEquipmentGraveyard aaeg = (TriggerContext.AllyAuraOrEquipmentGraveyard) ctx;
        Card dyingCard = aaeg.dyingCard();
        if (!dyingCard.isAura()) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setTriggeringCardId(dyingCard.getId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);

        gameLogService.append(match.gameData(), GameLog.cardTextCard(match.permanent().getCard(),
                "'s ability triggers (", dyingCard, " was put into a graveyard from the battlefield)."));
        log.info("Game {} - {} triggers to exile Aura {} and grant cast permission",
                match.gameData().id, match.permanent().getCard().getName(), dyingCard.getName());
        return true;
    }
    boolean handleAnyCreatureDeathPutCountersOnSourceCard(TriggerMatchContext match,
            PutCountersOnSourceCardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    /**
     * Gives {@code entry} the card that died ("that card", "it") as its triggering card, with the graveyard
     * incarnation it has now (CR 400.7), for the effects that read it as they resolve; nothing for {@code null}.
     */
    private static void markDyingCard(GameData gameData, StackEntry entry, Card dyingCard) {
        if (dyingCard != null) {
            entry.setTriggeringCard(dyingCard.getId(), gameData.graveyardEntryVersion(dyingCard.getId()));
        }
    }
}
