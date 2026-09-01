package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.service.input.PlayerInputService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.DelayedEffectOnDeath;
import com.github.laxika.magicalvibes.model.DelayedReturnOnDeath;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealsDamage;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealtDamageByAttackingCreature;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealtDamage;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeSourceWhenTargetLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.LifeGainOpponentLifeLossWatcher;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.CreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.CreatureEntersTriggerWatcher;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.SourceManaValueMinusOne;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LeavingPermanentIdAwareEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeredModalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageDamagedCreatureControllerAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DamagedCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.EquipmentDamagesOtherDefendingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EquipmentTapsAndLocksDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ReflectAllyDamageToDamagedCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndSkipUntapDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellReferencingEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextSpellCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfConditionEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForXValueEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutVoyageCounterOnExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnVoyagingCardFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.effect.CombatDamageTriggerContextEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardOnAllyLandEntersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureCardAwareEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureCounterAwareEffect;
import com.github.laxika.magicalvibes.model.effect.BatchedCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDyingCreatureToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.LeavingCreatureNameAwareEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureCountersAwareEffect;
import com.github.laxika.magicalvibes.model.effect.StormCopyEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.ClashOutcomeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellingEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterCreatureConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardNameMatchesEnteringPermanent;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.OncePerTurnTriggerSupport;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentFirstSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToManaSpentToCastToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnAllyCreatureEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnAllyLandEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemArtifactEntersTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.effect.ExileTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.IncrementTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EvolveTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thin orchestrator that detects trigger events, iterates permanents/effect-slots,
 * and delegates per-effect handling to the {@link TriggerCollectorRegistry}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerCollectionService {

    private final TriggerCollectorRegistry registry;
    private final GameOutcomeService gameOutcomeService;
    private final PlayerInputService playerInputService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetLegalityService targetLegalityService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final GraveyardTargetingSupport graveyardTargetingSupport;

    public List<CardEffect> grantedTriggeredEffects(GameData gameData, Permanent permanent, EffectSlot slot) {
        return grantedTriggeredAbilitySupport.grantedTriggeredEffects(gameData, permanent, slot);
    }

    public void checkYouPutCountersTriggers(GameData gameData, UUID placingPlayerId, int amount) {
        if (placingPlayerId == null || amount <= 0) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.CountersPlaced(placingPlayerId, amount);
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            dispatchSlot(gameData, permanent, placingPlayerId,
                    EffectSlot.ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER, context);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextSpellTargetTrigger(gameData);
        }
    }

    public void checkLoyaltyCounterRemovalTriggers(GameData gameData) {
        gameData.forEachPermanent((controllerId, permanent) -> {
            int amount = permanent.drainLoyaltyCountersRemovedSinceTriggerCheck();
            if (amount <= 0) return;

            dispatchSlot(gameData, permanent, controllerId,
                    EffectSlot.ON_SELF_LOYALTY_COUNTERS_REMOVED,
                    new TriggerContext.LoyaltyCountersRemoved(permanent, amount));
        });
    }

    /** Fires control-change triggers before the changed permanent moves to its new battlefield. */
    public void checkOpponentGainsControlTriggers(GameData gameData, Permanent changedPermanent,
                                                   UUID previousControllerId, UUID newControllerId) {
        if (changedPermanent == null || previousControllerId == null || newControllerId == null
                || previousControllerId.equals(newControllerId)
                || !newControllerId.equals(gameQueryService.getOpponentId(gameData, previousControllerId))) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(previousControllerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.PermanentControlChanged(
                changedPermanent, previousControllerId, newControllerId);
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            dispatchSlot(gameData, permanent, previousControllerId,
                    EffectSlot.ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT, context);
        }
    }

    public void checkTimeCounterRemovedFromExiledCardTriggers(GameData gameData, Card card,
            UUID ownerId, int remainingCounters) {
        if (card == null || ownerId == null) {
            return;
        }

        TriggerContext context = new TriggerContext.TimeCounterRemovedFromExile(remainingCounters);
        for (CardEffect effect : card.getEffects(EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE)) {
            dispatch(new TriggerMatchContext(gameData, null, ownerId, effect, card),
                    EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE, effect, context);
        }
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, true);
    }

    public void checkControllerGivesGiftTriggers(GameData gameData, UUID giverId) {
        TriggerContext context = new TriggerContext.GiftGiven(giverId);
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (controllerId.equals(giverId)) {
                dispatchSlot(gameData, permanent, controllerId, EffectSlot.ON_CONTROLLER_GIVES_GIFT, context);
            }
        });
    }

    public void checkSpellCopyTriggers(GameData gameData, StackEntry copiedSpell) {
        if (copiedSpell.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                || copiedSpell.getEntryType() == StackEntryType.ACTIVATED_ABILITY) {
            return;
        }

        UUID copyingPlayerId = copiedSpell.getControllerId();
        TriggerContext.SpellCopy context = new TriggerContext.SpellCopy(copiedSpell, copyingPlayerId);
        gameData.forEachPermanent((playerId, permanent) -> {
            if (playerId.equals(copyingPlayerId)) {
                dispatchSlot(gameData, permanent, playerId, EffectSlot.ON_CONTROLLER_COPIES_SPELL, context);
            } else {
                dispatchSlot(gameData, permanent, playerId, EffectSlot.ON_OPPONENT_COPIES_SPELL, context);
            }
        });
    }

    /**
     * Fires delayed "until end of turn, whenever you cast a [filter] spell, …" triggers registered
     * this turn (Mountain Titan). One trigger per registration. Registrations that require their
     * source permanent to remain on the battlefield are skipped once it has left.
     */
    private void processDelayedControllerSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        if (!gameData.hasDelayedAction(DelayedControllerSpellCastTrigger.class)) {
            return;
        }
        for (DelayedControllerSpellCastTrigger delayed
                : gameData.getDelayedActions(DelayedControllerSpellCastTrigger.class)) {
            if (!delayed.controllerId().equals(castingPlayerId)) continue;
            if (delayed.sourceMustRemainOnBattlefield()
                    && gameQueryService.findPermanentById(gameData, delayed.sourcePermanentId()) == null) {
                continue;
            }
            if (!predicateEvaluationService.matchesCardPredicate(spellCard, delayed.spellFilter(), null)) continue;

            if (delayed.oneShot()) {
                gameData.delayedActions.remove(delayed);
            }

            if (delayed.targetFilter() != null) {
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        new ArrayList<>(delayed.resolvedEffects()),
                        false,
                        delayed.targetFilter(),
                        0,
                        delayed.sourcePermanentId()));
                gameLogService.append(gameData, GameLog.cardTextCard(
                        delayed.sourceCard(), "'s delayed trigger fires for ", spellCard, "."));
                log.info("Game {} - {} delayed spell-cast trigger awaits a target for {}",
                        gameData.id, delayed.sourceCard().getName(), spellCard.getName());
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(delayed.resolvedEffects()),
                    null,
                    delayed.sourcePermanentId());
            entry.setTriggeringCardId(spellCard.getId());
            entry.setEventValue(spellCard.getManaValue());
            entry.setNonTargeting(true);
            gameData.stack.add(entry);
            gameLogService.append(gameData, GameLog.cardTextCard(
                    delayed.sourceCard(), "'s delayed trigger fires for ", spellCard, "."));
            log.info("Game {} - {} delayed spell-cast trigger fires for {}",
                    gameData.id, delayed.sourceCard().getName(), spellCard.getName());
        }
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId, boolean castFromHand) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId,
                castFromHand ? Zone.HAND : Zone.GRAVEYARD);
    }

    public void checkCrimeTriggers(GameData gameData, StackEntry stackEntry) {
        if (!gameData.isCommittedCrime(stackEntry)) return;

        UUID committingPlayerId = stackEntry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(committingPlayerId);
        TriggerContext ctx = new TriggerContext.Crime(committingPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : List.copyOf(battlefield)) {
                dispatchSlot(gameData, permanent, committingPlayerId,
                        EffectSlot.ON_CONTROLLER_COMMITS_CRIME, ctx);
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(committingPlayerId);
        if (graveyard != null) {
            for (Card card : List.copyOf(graveyard)) {
                for (CardEffect effect : gameQueryService.getEffectiveGraveyardEffects(
                        gameData, card, EffectSlot.GRAVEYARD_ON_CONTROLLER_COMMITS_CRIME)) {
                    queueGraveyardCrimeTrigger(gameData, card, committingPlayerId, effect);
                }
            }
        }
    }

    private void queueGraveyardCrimeTrigger(GameData gameData, Card card, UUID controllerId,
                                             CardEffect effect) {
        if (effect instanceof MayPayManaEffect mayPay) {
            gameData.queueMayAbility(card, controllerId, mayPay, null);
        } else if (effect instanceof MayEffect may) {
            gameData.queueMayAbility(card, controllerId, may);
        } else {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s ability",
                    new ArrayList<>(List.of(effect))));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(card));
    }

    /**
     * Zone-carrying form: {@code castZone} is the zone the spell was cast from, so triggers that
     * care about a specific origin (cast from a graveyard, cast from the top of a library) can tell
     * them apart instead of sharing one "not from hand" flag.
     */
    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone) {
        if (castZone == Zone.HAND) {
            gameData.recordSpellCastFromHand(spellCard);
        }
        var ctx = new TriggerContext.SpellCast(spellCard, castingPlayerId, castZone);
        if (castZone != Zone.HAND) {
            gameData.playersWhoPlayedOrCastFromOutsideHandThisTurn.add(castingPlayerId);
        }

        gameData.expireFloatingEffects(fe ->
                fe.duration() == EffectDuration.UNTIL_MATCHING_SPELL_CAST
                        && castingPlayerId.equals(fe.controllerId())
                        && fe.effect() instanceof ReduceCastCostForNextMatchingSpellEffect reduction
                        && predicateEvaluationService.matchesCardPredicate(
                                spellCard, reduction.predicate(), null, gameData, castingPlayerId));

        if (spellCard.hasType(CardType.CREATURE)) {
            gameData.expireFloatingEffectsOnCreatureSpellCast();
        }

        // Opening hand reveal delayed triggers (Chancellor cycle)
        if (!gameData.openingHandRevealTriggers.isEmpty()
                && !gameData.playersWhoCastFirstSpellInGame.contains(castingPlayerId)) {
            gameData.playersWhoCastFirstSpellInGame.add(castingPlayerId);
            for (OpeningHandRevealTrigger trigger : gameData.openingHandRevealTriggers) {
                if (!trigger.revealingPlayerId().equals(castingPlayerId)
                        && trigger.effect() instanceof CounterUnlessEffect counterEffect) {
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            trigger.sourceCard(),
                            trigger.revealingPlayerId(),
                            trigger.sourceCard().getName() + "'s ability",
                            new ArrayList<>(List.of(counterEffect)),
                            spellCard.getId(),
                            Zone.STACK
                    );
                    gameData.stack.add(entry);
                }
            }
        }

        // ON_ANY_PLAYER_CASTS_SPELL
        gameData.forEachPermanent((playerId, perm) -> {
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, ctx);
        });

        // ON_CONTROLLER_CASTS_SPELL (only controller's own spells)
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(castingPlayerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_CONTROLLER_CASTS_SPELL, ctx);
        });

        collectTemporaryControllerSpellCastTriggers(gameData, spellCard, castingPlayerId);

        processDelayedControllerSpellCastTriggers(gameData, spellCard, castingPlayerId);

        // ON_OPPONENT_CASTS_SPELL (only opponents' permanents)
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(castingPlayerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_OPPONENT_CASTS_SPELL, ctx);
        });

        dispatchSuspendedExiledCardSpellCastTriggers(gameData, castingPlayerId, ctx);

        // Increment keyword (CR keyword): "Whenever you cast a spell, if the amount of mana you spent
        // is greater than this creature's power or toughness, put a +1/+1 counter on it." Driven by the
        // Scryfall-loaded keyword (like Undying) rather than a per-card effect. Read before mana-spent
        // is cleared below.
        collectIncrementTriggers(gameData, spellCard, castingPlayerId);

        // Emblem spell cast triggers (e.g. Venser's emblem, Jace Unraveler of Secrets' emblem,
        // Chandra Dressed to Kill's emblem). Mana-spent readers must run before clearSpellCastManaSpent.
        for (Emblem emblem : gameData.emblems) {
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof ExileTargetOnControllerSpellCastEffect) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    gameData.queueInteraction(new PermanentChoiceContext.EmblemTriggerTarget(
                            "Venser's emblem",
                            emblem.controllerId(),
                            List.of(new ExileTargetPermanentEffect()),
                            emblem.sourceCard()
                    ));
                } else if (effect instanceof CounterOpponentFirstSpellEachTurnEffect) {
                    // Opponent's first spell this turn — auto-target it on the stack (no choice).
                    if (emblem.controllerId().equals(castingPlayerId)) continue;
                    if (gameData.getSpellsCastThisTurnCount(castingPlayerId) != 1) continue;
                    Card source = emblem.sourceCard();
                    String desc = (source != null ? source.getName() : "Jace, Unraveler of Secrets")
                            + "'s emblem";
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source != null ? source : spellCard,
                            emblem.controllerId(),
                            desc,
                            new ArrayList<>(List.of(new CounterSpellEffect())),
                            spellCard.getId(),
                            Zone.STACK
                    ));
                    gameLogService.append(gameData,
                            GameLog.text(desc + " triggers — counter that spell."));
                    log.info("Game {} - {} counters opponent's first spell this turn",
                            gameData.id, desc);
                } else if (effect instanceof SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect) {
                    // Garruk, Caller of Beasts' emblem — fires on the controller's own creature spells.
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (!spellCard.hasType(CardType.CREATURE)) continue;
                    Card source = emblem.sourceCard();
                    String desc = (source != null ? source.getName() : "Garruk, Caller of Beasts")
                            + "'s emblem";
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source != null ? source : spellCard,
                            emblem.controllerId(),
                            desc,
                            new ArrayList<>(List.of(new MayEffect(
                                    new SearchLibraryEffect(
                                            new CardTypePredicate(CardType.CREATURE),
                                            LibrarySearchDestination.BATTLEFIELD),
                                    "Search your library for a creature card and put it onto the battlefield?")))
                    ));
                    gameLogService.append(gameData, GameLog.text(desc + " triggers."));
                    log.info("Game {} - {} creature-spell search trigger queued", gameData.id, desc);
                } else if (effect instanceof MillEffect mill && mill.recipient() == MillRecipient.TARGET_PLAYER) {
                    // "Whenever you cast a spell, target opponent mills N cards" (Jace, Telepath
                    // Unbound's emblem) — the opponent restriction rides the player target filter.
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            sourceCard,
                            emblem.controllerId(),
                            new ArrayList<>(List.of(mill)),
                            true,
                            new PlayerPredicateTargetFilter(
                                    new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                    "Target must be an opponent")
                    ));
                    gameLogService.append(gameData, GameLog.text(
                            (source != null ? source.getName() : "Emblem")
                                    + "'s emblem triggers — choose target opponent."));
                    log.info("Game {} - emblem mill trigger queued", gameData.id);
                } else if (effect instanceof DealDamageEqualToManaSpentToCastToAnyTargetEffect damageTrigger) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (damageTrigger.spellFilter() != null
                            && !predicateEvaluationService.matchesCardPredicate(
                                    spellCard, damageTrigger.spellFilter(), null)) {
                        continue;
                    }
                    int manaSpent = gameData.getSpellCastManaSpent(spellCard.getId());
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    String desc = (source != null ? source.getName() : "Chandra, Dressed to Kill")
                            + "'s emblem";
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            sourceCard,
                            emblem.controllerId(),
                            new ArrayList<>(List.of(new DealDamageToAnyTargetEffect(manaSpent)))
                    ));
                    gameLogService.append(gameData,
                            GameLog.text(desc + " triggers — choose a target for " + manaSpent + " damage."));
                    log.info("Game {} - {} emblem mana-spent damage trigger queued ({} damage)",
                            gameData.id, desc, manaSpent);
                } else if (effect instanceof StormEffect storm) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (storm.instantOrSorceryOnly()
                            && !spellCard.hasType(CardType.INSTANT)
                            && !spellCard.hasType(CardType.SORCERY)) {
                        continue;
                    }
                    StackEntry spellEntry = gameData.stack.stream()
                            .filter(entry -> entry.getCard().getId().equals(spellCard.getId()))
                            .findFirst().orElse(null);
                    if (spellEntry == null) continue;
                    int copies = Math.max(0, gameData.getTotalSpellsCastThisTurnCount() - 1);
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            sourceCard,
                            emblem.controllerId(),
                            sourceCard.getName() + "'s emblem",
                            new ArrayList<>(List.of(new StormCopyEffect(
                                    new StackEntry(spellEntry), castingPlayerId, copies,
                                    storm.tokenCopy())))));
                } else if (effect instanceof CopyControllerCastSpellOnSpellCastEffect copyTrigger) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (copyTrigger.spellFilter() != null
                            && !predicateEvaluationService.matchesCardPredicate(
                            spellCard, copyTrigger.spellFilter(), null)) {
                        continue;
                    }
                    StackEntry spellEntry = gameData.stack.stream()
                            .filter(entry -> entry.getCard().getId().equals(spellCard.getId()))
                            .findFirst().orElse(null);
                    if (spellEntry == null) continue;
                    if (copyTrigger.requiredCastWithAdventure()
                            && (!spellEntry.isCastWithAdventure()
                            || (spellEntry.getEntryType() != StackEntryType.INSTANT_SPELL
                            && spellEntry.getEntryType() != StackEntryType.SORCERY_SPELL))) {
                        continue;
                    }
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    CardEffect copyEffect = new CopyControllerCastSpellEffect(
                            new StackEntry(spellEntry), castingPlayerId,
                            copyTrigger.grantedKeywords(), copyTrigger.additionalTypes(),
                            copyTrigger.tokenCopy(), copyTrigger.mayChooseNewTargets());
                    CardEffect resolutionEffect = copyTrigger.manaCost() == null
                            ? copyEffect
                            : new MayPayManaEffect(copyTrigger.manaCost(), copyEffect,
                            "Pay " + copyTrigger.manaCost() + " to copy " + spellCard.getName() + "?");
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            sourceCard,
                            emblem.controllerId(),
                            sourceCard.getName() + "'s emblem",
                            new ArrayList<>(List.of(resolutionEffect))));
                } else if (effect instanceof SpellCastTriggerEffect trigger
                        && isNonTargetingEmblemSpellCastTrigger(trigger)) {
                    if (!trigger.triggersOnAnyPlayer()
                            && !emblem.controllerId().equals(castingPlayerId)) continue;
                    if (trigger.spellFilter() != null
                            && !predicateEvaluationService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) {
                        continue;
                    }
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    String desc = (source != null ? source.getName() : "Emblem") + "'s emblem";
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            sourceCard,
                            emblem.controllerId(),
                            desc,
                            new ArrayList<>(trigger.resolvedEffects())
                    );
                    entry.setTriggeringCardId(spellCard.getId());
                    entry.setNonTargeting(true);
                    gameData.stack.add(entry);
                    gameLogService.append(gameData, GameLog.text(desc + " triggers."));
                    log.info("Game {} - {} generic emblem spell-cast trigger queued", gameData.id, desc);
                } else if (effect instanceof DealDamageToAnyTargetOnControllerSpellCastEffect damageTrigger) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (damageTrigger.spellFilter() != null
                            && !predicateEvaluationService.matchesCardPredicate(
                                    spellCard, damageTrigger.spellFilter(), null)) {
                        continue;
                    }
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    String desc = (source != null ? source.getName() : "Emblem") + "'s emblem";
                    List<CardEffect> resolvedEffects = new ArrayList<>();
                    resolvedEffects.add(new DealDamageToAnyTargetEffect(damageTrigger.damage()));
                    resolvedEffects.addAll(damageTrigger.additionalEffects());
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            sourceCard,
                            emblem.controllerId(),
                            resolvedEffects
                    ));
                    gameLogService.append(gameData,
                            GameLog.text(desc + " triggers — choose a target for " + damageTrigger.damage() + " damage."));
                    log.info("Game {} - {} damage trigger queued ({})",
                            gameData.id, desc, damageTrigger.damage());
                }
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class)) {
            triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        gameData.clearSpellCastManaSpent(spellCard.getId());
        gameData.clearSpellCastManaSources(spellCard.getId());
        gameData.clearSpellCastCreatureManaSpent(spellCard.getId());

        // GRAVEYARD_ON_CONTROLLER_CASTS_SPELL — graveyard-resident spell-cast triggers
        // (e.g. Lingering Phantom: "Whenever you cast a historic spell, you may pay {B}. If you do, return ~ to hand.")
        List<Card> castingPlayerGraveyard = gameData.playerGraveyards.get(castingPlayerId);
        if (castingPlayerGraveyard != null) {
            for (Card card : new ArrayList<>(castingPlayerGraveyard)) {
                List<CardEffect> graveyardEffects = gameQueryService.getEffectiveGraveyardEffects(
                        gameData, card, EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL);
                if (graveyardEffects == null || graveyardEffects.isEmpty()) continue;

                for (CardEffect effect : graveyardEffects) {
                    if (effect instanceof SpellCastTriggerEffect trigger) {
                        if (!predicateEvaluationService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) continue;
                        if (trigger.nthSpellNumber() > 0 && !isNthMatchingSpell(gameData, trigger, castingPlayerId)) {
                            continue;
                        }

                        if (trigger.manaCost() != null) {
                            // "you may pay {X}" pattern — queue MayPayManaEffect on the stack
                            CardEffect resolvedEffect = trigger.resolvedEffects().getFirst();
                            MayPayManaEffect mayPay = new MayPayManaEffect(
                                    trigger.manaCost(),
                                    resolvedEffect,
                                    "Pay " + trigger.manaCost() + " to return " + card.getName()
                                            + " from your graveyard to your hand?"
                            );
                            gameData.queueMayAbility(card, castingPlayerId, mayPay, null);
                        } else {
                            StackEntry entry = new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    card,
                                    castingPlayerId,
                                    card.getName() + "'s ability",
                                    new ArrayList<>(trigger.resolvedEffects())
                            );
                            entry.setTriggeringCardId(spellCard.getId());
                            gameData.stack.add(entry);
                        }

                        log.info("Game {} - {} graveyard spell-cast trigger queued",
                                gameData.id, card.getName());
                    }
                }
            }
        }

        // COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL — Eminence and similar command-zone spell-cast triggers
        List<Card> castingPlayerCommandZone = gameData.playerCommandZones.get(castingPlayerId);
        if (castingPlayerCommandZone != null) {
            for (Card card : new ArrayList<>(castingPlayerCommandZone)) {
                List<CardEffect> commandEffects = card.getEffects(EffectSlot.COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL);
                if (commandEffects == null || commandEffects.isEmpty()) continue;

                for (CardEffect effect : commandEffects) {
                    if (effect instanceof SpellCastTriggerEffect trigger) {
                        if (!predicateEvaluationService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) {
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                castingPlayerId,
                                card.getName() + "'s ability",
                                new ArrayList<>(trigger.resolvedEffects())
                        ));
                        log.info("Game {} - {} command-zone spell-cast trigger queued",
                                gameData.id, card.getName());
                    }
                }
            }
        }

        // Primal Wellspring delayed mana trigger: copy next instant/sorcery (one-shot)
        Integer pendingCopies = gameData.pendingNextInstantSorceryCopyCount.get(castingPlayerId);
        if (pendingCopies != null && pendingCopies > 0
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int copyNumber = 0; copyNumber < pendingCopies; copyNumber++) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName() + " (Primal Wellspring)",
                            new ArrayList<>(List.of(copyEffect))
                    ));
                }
                gameData.pendingNextInstantSorceryCopyCount.remove(castingPlayerId);
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (Primal Wellspring)."));
                log.info("Game {} - {} spell-copy trigger(s) queued for {} (Primal Wellspring)",
                        gameData.id, pendingCopies, spellCard.getName());
            }
        }

        // Pyromancer's Goggles delayed mana trigger: copy next *red* instant/sorcery (one-shot)
        Integer pendingRedCopies = gameData.pendingNextRedInstantSorceryCopyCount.get(castingPlayerId);
        if (pendingRedCopies != null && pendingRedCopies > 0
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))
                && spellCard.getColors() != null && spellCard.getColors().contains(CardColor.RED)) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int copyNumber = 0; copyNumber < pendingRedCopies; copyNumber++) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName(),
                            new ArrayList<>(List.of(copyEffect))
                    ));
                }
                gameData.pendingNextRedInstantSorceryCopyCount.remove(castingPlayerId);
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                log.info("Game {} - {} red spell-copy trigger(s) queued for {}",
                        gameData.id, pendingRedCopies, spellCard.getName());
            }
        }

        // "When you next cast an instant or sorcery spell this turn, copy that spell"
        // (e.g. Chandra, the Firebrand −2). Same one-shot shape as Primal Wellspring's trigger, but
        // tracked in the turn-scoped counter so it survives mana drain.
        Integer pendingTurnCopies = gameData.pendingNextInstantSorceryCopyThisTurnCount.get(castingPlayerId);
        List<Integer> pendingMaxManaValues =
                gameData.pendingNextInstantSorceryCopyThisTurnMaxManaValues.get(castingPlayerId);
        if ((pendingTurnCopies != null && pendingTurnCopies > 0
                || pendingMaxManaValues != null && !pendingMaxManaValues.isEmpty())
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                int limitedCopies = 0;
                if (pendingMaxManaValues != null) {
                    int manaValue = spellCard.getManaValue();
                    if (spellCard.getParsedManaCost() != null) {
                        manaValue += spellEntry.getXValue() * spellCard.getParsedManaCost().getXSymbolCount();
                    }
                    for (int i = pendingMaxManaValues.size() - 1; i >= 0; i--) {
                        if (manaValue <= pendingMaxManaValues.get(i)) {
                            pendingMaxManaValues.remove(i);
                            limitedCopies++;
                        }
                    }
                    if (pendingMaxManaValues.isEmpty()) {
                        gameData.pendingNextInstantSorceryCopyThisTurnMaxManaValues.remove(castingPlayerId);
                    }
                }
                int unrestrictedCopies = pendingTurnCopies == null ? 0 : pendingTurnCopies;
                int totalCopies = unrestrictedCopies + limitedCopies;
                if (totalCopies > 0) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    List<CardEffect> copyEffects = new ArrayList<>(totalCopies);
                    for (int i = 0; i < totalCopies; i++) {
                        copyEffects.add(new CopyControllerCastSpellEffect(snapshot, castingPlayerId));
                    }
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName(),
                            copyEffects
                    ));
                    if (unrestrictedCopies > 0) {
                        gameData.pendingNextInstantSorceryCopyThisTurnCount.remove(castingPlayerId);
                    }
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                    log.info("Game {} - {} delayed spell-copy trigger(s) queued for {}",
                            gameData.id, totalCopies, spellCard.getName());
                }
            }
        }

        Integer pendingSpellCopies = gameData.pendingNextSpellCopyThisTurnCount.get(castingPlayerId);
        if (pendingSpellCopies != null && pendingSpellCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                boolean permanentSpell = switch (spellEntry.getEntryType()) {
                    case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
                    default -> false;
                };
                StackEntry snapshot = new StackEntry(spellEntry);
                List<CardEffect> copyEffects = new ArrayList<>(pendingSpellCopies);
                for (int i = 0; i < pendingSpellCopies; i++) {
                    copyEffects.add(new CopyControllerCastSpellEffect(
                            snapshot, castingPlayerId, Set.of(), Set.of(), permanentSpell, true));
                }
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        "Copy " + spellCard.getName(),
                        copyEffects
                ));
                gameData.pendingNextSpellCopyThisTurnCount.remove(castingPlayerId);
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                log.info("Game {} - {} all-spell copy trigger(s) queued for {}",
                        gameData.id, pendingSpellCopies, spellCard.getName());
            }
        }

        List<CopyNextSpellCastThisTurnEffect> pendingFilteredCopies =
                gameData.pendingNextFilteredSpellCopiesThisTurn.get(castingPlayerId);
        if (pendingFilteredCopies != null && !pendingFilteredCopies.isEmpty()) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                List<CopyNextSpellCastThisTurnEffect> matchingCopies = new ArrayList<>();
                List<CopyNextSpellCastThisTurnEffect> remainingCopies = new ArrayList<>();
                for (CopyNextSpellCastThisTurnEffect copyEffect : pendingFilteredCopies) {
                    if (copyEffect.spellFilter() == null
                            || predicateEvaluationService.matchesCardPredicate(
                            spellCard, copyEffect.spellFilter(), null, gameData, castingPlayerId)) {
                        matchingCopies.add(copyEffect);
                    } else {
                        remainingCopies.add(copyEffect);
                    }
                }
                if (!matchingCopies.isEmpty()) {
                    boolean permanentSpell = switch (spellEntry.getEntryType()) {
                        case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
                        default -> false;
                    };
                    StackEntry snapshot = new StackEntry(spellEntry);
                    List<CardEffect> copyEffects = new ArrayList<>(matchingCopies.size());
                    for (CopyNextSpellCastThisTurnEffect copyEffect : matchingCopies) {
                        copyEffects.add(new CopyControllerCastSpellEffect(
                                snapshot, castingPlayerId, Set.of(), Set.of(),
                                copyEffect.removedSupertypes(), permanentSpell, true));
                    }
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName(),
                            copyEffects
                    ));
                    if (remainingCopies.isEmpty()) {
                        gameData.pendingNextFilteredSpellCopiesThisTurn.remove(castingPlayerId);
                    } else {
                        gameData.pendingNextFilteredSpellCopiesThisTurn.put(castingPlayerId, remainingCopies);
                    }
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                    log.info("Game {} - {} filtered spell-copy trigger(s) queued for {}",
                            gameData.id, matchingCopies.size(), spellCard.getName());
                }
            }
        }

        // "Whenever you cast a creature spell this turn, draw a card" (Glimpse of Nature).
        // Repeating for the rest of the turn; multiple copies draw one card each.
        Integer creatureCastDraws = gameData.creatureSpellCastDrawsThisTurn.get(castingPlayerId);
        if (creatureCastDraws != null && creatureCastDraws > 0 && spellCard.hasType(CardType.CREATURE)) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    spellCard,
                    castingPlayerId,
                    "Draw a card",
                    new ArrayList<>(List.of(new DrawCardEffect(creatureCastDraws)))
            ));
            log.info("Game {} - creature-spell-cast draw trigger queued for {} ({} cards)",
                    gameData.id, castingPlayerId, creatureCastDraws);
        }

        // "Until end of turn, whenever you cast an instant or sorcery spell, copy it"
        // (e.g. The Mirari Conjecture chapter III)
        if (gameData.playersWithSpellCopyUntilEndOfTurn.contains(castingPlayerId)
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            // Find the spell on the stack to create a snapshot
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                StackEntry snapshot = new StackEntry(spellEntry);
                CopyControllerCastSpellEffect copyEffect =
                        new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        "Copy " + spellCard.getName(),
                        new ArrayList<>(List.of(copyEffect))
                ));
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (The Mirari Conjecture)."));
                log.info("Game {} - {} spell-copy trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
            }
        }

        // Conspire (CR 702.78): "When you [tap the two creatures], copy it and you may choose a new
        // target for the copy." The spell was flagged in gameData.conspiredSpellIds when its conspire
        // cost was paid during casting. One copy per conspired spell.
        if (gameData.conspiredSpellIds.remove(spellCard.getId())) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                StackEntry snapshot = new StackEntry(spellEntry);
                CopyControllerCastSpellEffect copyEffect =
                        new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        "Copy " + spellCard.getName() + " (Conspire)",
                        new ArrayList<>(List.of(copyEffect))
                ));
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (Conspire)."));
                log.info("Game {} - {} conspire copy trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
            }
        }

        // ON_SELF_CAST — "When you cast this spell, ..." triggers scanned against the just-cast card
        // itself (it's a spell on the stack, not a permanent). CopyThisSpellIfConditionEffect (SOS
        // Infusion copy cycle) needs a snapshot of the spell entry; any other effect (e.g. Demigod of
        // Revenge's graveyard return) is queued as a plain triggered ability under the caster.
        List<CardEffect> selfCastTriggeredEffects = new ArrayList<>();
        List<CardEffect> selfCastEffects = new ArrayList<>();
        if (spellCard.getManaCost() != null
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))
                && gameQueryService.hasSpellCastingAbilityGrant(
                        gameData, castingPlayerId, spellCard, Keyword.REPLICATE, castZone)
                && spellCard.getEffects(EffectSlot.ON_SELF_CAST).stream()
                .noneMatch(effect -> effect instanceof ReplicateEffect replicate
                        && spellCard.getManaCost().equals(replicate.manaCost()))) {
            selfCastEffects.add(new ReplicateEffect(spellCard.getManaCost()));
        }
        selfCastEffects.addAll(spellCard.getEffects(EffectSlot.ON_SELF_CAST));
        for (CardEffect effect : selfCastEffects) {
            if (effect instanceof CopyThisSpellIfConditionEffect trigger) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getCard().getId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null) continue;

                // Always triggers; the "if <condition>" is an effect clause re-checked at resolution.
                StackEntry snapshot = new StackEntry(spellEntry);
                CardEffect copyEffect = new ConditionalEffect(trigger.condition(),
                        new CopyControllerCastSpellEffect(snapshot, castingPlayerId,
                                Set.of(), Set.of(), trigger.tokenCopy()));
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(List.of(copyEffect))
                ));
                log.info("Game {} - {} self-cast copy trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
            } else if (effect instanceof CopyThisSpellForXValueEffect) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getCard().getId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null || spellEntry.getXValue() <= 0) continue;

                int copies = spellEntry.getXValue();
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(List.of(new StormCopyEffect(
                                new StackEntry(spellEntry), castingPlayerId, copies, false)))
                ));
                log.info("Game {} - {} self-cast trigger queued ({} copies) for {}",
                        gameData.id, spellCard.getName(), copies, castingPlayerId);
            } else if (effect instanceof ReplicateEffect replicate) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getCard().getId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null) continue;

                int copies = (int) spellEntry.getRepeatedAdditionalCosts().stream()
                        .filter(replicate.manaCost()::equals)
                        .count();
                if (copies <= 0) continue;

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(List.of(new StormCopyEffect(
                                new StackEntry(spellEntry), castingPlayerId, copies, false)))
                ));
                log.info("Game {} - {} replicate trigger queued ({} copies) for {}",
                        gameData.id, spellCard.getName(), copies, castingPlayerId);
            } else if (effect instanceof SpellCastCopyTriggerEffect copyTrigger) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getCard().getId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null) continue;

                int copies = copyTrigger.copyCount(gameData, castingPlayerId);
                StackEntry snapshot = new StackEntry(spellEntry);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(List.of(new StormCopyEffect(
                                snapshot, castingPlayerId, copies, copyTrigger.tokenCopy())))
                ));
                log.info("Game {} - {} spell-copy trigger queued ({} copies) for {}",
                        gameData.id, spellCard.getName(), copies, castingPlayerId);
            } else if (effect instanceof StormEffect storm) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getCard().getId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null) continue;

                // "for each spell cast before it this turn" — this spell is already recorded, so
                // subtract it out. Count is fixed here (spells cast after can't precede this one).
                int copies = Math.max(0, storm.instantOrSorceryOnly()
                        ? (int) gameData.getSpellsCastThisTurn(castingPlayerId).stream()
                                .filter(card -> card.hasType(CardType.INSTANT)
                                        || card.hasType(CardType.SORCERY))
                                .count() - 1
                        : gameData.getTotalSpellsCastThisTurnCount() - 1);
                StackEntry snapshot = new StackEntry(spellEntry);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(List.of(new StormCopyEffect(
                                snapshot, castingPlayerId, copies, storm.tokenCopy())))
                ));
                log.info("Game {} - {} Storm trigger queued ({} copies) for {}",
                        gameData.id, spellCard.getName(), copies, castingPlayerId);
            } else {
                selfCastTriggeredEffects.add(effect);
            }
        }
        if (!selfCastTriggeredEffects.isEmpty()) {
            if (selfCastTriggeredEffects.size() == 1
                    && selfCastTriggeredEffects.getFirst() instanceof MayEffect may
                    && may.targetSpec() == TargetSpec.NONE) {
                gameData.pendingMayAbilities.add(PendingMayAbility.forSpellCastTrigger(
                        spellCard,
                        castingPlayerId,
                        new ArrayList<>(List.of(may.wrapped())),
                        spellCard.getName() + " — " + may.prompt(),
                        null,
                        null,
                        spellCard.getId()));
                log.info("Game {} - {} self-cast may trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
            } else {
                // Targeted ON_SELF_CAST (e.g. Abundant Maw: "target opponent loses 3 life") chooses
                // targets as the ability goes on the stack — reuse SpellTargetTriggerAnyTarget.
                // Multi-target ("up to N target permanents", Elder Deep-Fiend) reuses the ETB
                // multi-target slot walker with a null source permanent id.
                boolean needsPlayerTarget = selfCastTriggeredEffects.stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                boolean needsPermanentTarget = selfCastTriggeredEffects.stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
                boolean needsGraveyardTarget = selfCastTriggeredEffects.stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
                if (needsGraveyardTarget && !needsPlayerTarget && !needsPermanentTarget) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                            spellCard, castingPlayerId, new ArrayList<>(selfCastTriggeredEffects)));
                    log.info("Game {} - {} self-cast graveyard-target trigger queued for {}",
                            gameData.id, spellCard.getName(), castingPlayerId);
                } else if (needsPlayerTarget || needsPermanentTarget) {
                    boolean multiTarget = spellCard.getSpellTargets().size() > 1
                            || etbTokenTargetService.needsSlotBySlotTargetSelection(spellCard);
                    if (multiTarget) {
                        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                spellCard, castingPlayerId, new ArrayList<>(selfCastTriggeredEffects),
                                null, new ArrayList<>(), 0, 0));
                        log.info("Game {} - {} self-cast multi-target trigger queued for {}",
                                gameData.id, spellCard.getName(), castingPlayerId);
                    } else {
                        boolean playerTargetOnly = needsPlayerTarget && !needsPermanentTarget;
                        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                                spellCard, castingPlayerId, new ArrayList<>(selfCastTriggeredEffects),
                                playerTargetOnly, spellCard.getTargetFilter()));
                        log.info("Game {} - {} self-cast targeting trigger queued for {}",
                                gameData.id, spellCard.getName(), castingPlayerId);
                    }
                } else {
                    // Carry the spell's X onto the trigger so "reveal the top X cards" (Genesis Hydra)
                    // sees the value locked in on cast (CR 601.2b); 0 for spells without {X}.
                    int selfCastX = 0;
                    for (StackEntry se : gameData.stack) {
                        if (se.getCard().getId().equals(spellCard.getId())) {
                            selfCastX = se.getXValue();
                            break;
                        }
                    }
                    StackEntry selfCastTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            spellCard.getName() + "'s ability",
                            new ArrayList<>(selfCastTriggeredEffects),
                            selfCastX
                    );
                    if (selfCastTriggeredEffects.stream()
                            .anyMatch(TriggeringSpellReferencingEffect.class::isInstance)) {
                        selfCastTrigger.setTriggeringCardId(spellCard.getId());
                    }
                    gameData.stack.add(selfCastTrigger);
                    log.info("Game {} - {} self-cast trigger queued for {}",
                            gameData.id, spellCard.getName(), castingPlayerId);
                }
            }
        }

        // COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL — Eminence and similar command-zone spell-cast triggers

        // "The first spell you cast each turn has cascade" (Maelstrom Nexus). A permanent-granted
        // keyword, detected by the presence of a GRANT_CASCADE_TO_FIRST_SPELL slot on the caster's
        // battlefield rather than an effect-type check. recordSpellCast runs before this method in
        // every cast path, so a count of 1 identifies the caster's first spell of the turn. The held
        // CascadeEffect is queued keyed to the just-cast spell so CascadeEffectHandler's threshold is
        // the spell's mana value (not the granting permanent's). One trigger per granting permanent.
        if (gameData.getSpellsCastThisTurnCount(castingPlayerId) == 1) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    List<CardEffect> grantEffects = perm.getCard().getEffects(EffectSlot.GRANT_CASCADE_TO_FIRST_SPELL);
                    if (grantEffects.isEmpty()) continue;

                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            spellCard.getName() + "'s ability",
                            new ArrayList<>(grantEffects)
                    ));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to first spell {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        playerInputService.processNextMayAbility(gameData);
    }

    private void dispatchSuspendedExiledCardSpellCastTriggers(GameData gameData, UUID castingPlayerId,
            TriggerContext.SpellCast context) {
        for (ExiledCardEntry exiledEntry : new ArrayList<>(gameData.exiledCards)) {
            UUID ownerId = exiledEntry.ownerId();
            if (ownerId.equals(castingPlayerId)) {
                continue;
            }
            Integer timeCounters = gameData.exiledCardTimeCounters.get(exiledEntry.card().getId());
            if (timeCounters == null || timeCounters <= 0) {
                continue;
            }

            Card card = exiledEntry.card();
            for (CardEffect effect : card.getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL)) {
                registry.dispatch(new TriggerMatchContext(gameData, null, ownerId, effect, card),
                        EffectSlot.ON_OPPONENT_CASTS_SPELL, effect, context);
            }
        }
    }

    private boolean isNthMatchingSpell(GameData gameData, SpellCastTriggerEffect trigger, UUID playerId) {
        long matchingSpells = gameData.getSpellsCastThisTurn(playerId).stream()
                .filter(spell -> predicateEvaluationService.matchesCardPredicate(spell, trigger.spellFilter(), null))
                .count();
        return matchingSpells == trigger.nthSpellNumber();
    }

    private boolean isUnconditionalNonTargetingEmblemTrigger(SpellCastTriggerEffect trigger) {
        return trigger.spellFilter() == null
                && trigger.manaCost() == null
                && trigger.targetFilter() == null
                && trigger.castSpellTargetCondition() == null
                && !trigger.onlyDuringOpponentTurn()
                && !trigger.onlyDuringControllerTurn()
                && trigger.intervening() == null
                && trigger.nthSpellNumber() == 0
                && !trigger.resolvedEffects().isEmpty()
                && trigger.resolvedEffects().stream()
                .allMatch(effect -> effect.targetSpec().equals(TargetSpec.NONE));
    }

    /** Fires effects that care when the given player controls an effect that counters a spell. */
    private boolean isNonTargetingEmblemSpellCastTrigger(SpellCastTriggerEffect trigger) {
        return trigger.manaCost() == null
                && trigger.targetFilter() == null
                && trigger.castSpellTargetCondition() == null
                && !trigger.onlyDuringOpponentTurn()
                && !trigger.onlyDuringControllerTurn()
                && trigger.intervening() == null
                && !trigger.resolvedEffects().isEmpty()
                && trigger.resolvedEffects().stream()
                .allMatch(effect -> effect.targetSpec().equals(TargetSpec.NONE));
    }

    public void checkControllerCountersSpellTriggers(GameData gameData, UUID counteringPlayerId) {
        if (counteringPlayerId == null) return;

        var ctx = new TriggerContext.SpellCountered(counteringPlayerId);
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(counteringPlayerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_CONTROLLER_COUNTERS_SPELL, ctx);
        });
    }

    /** Fires effects that care when a spell cast by the given player is countered. */
    public void checkControllerSpellCounteredTriggers(GameData gameData, UUID spellControllerId) {
        if (spellControllerId == null) return;

        var ctx = new TriggerContext.SpellCastCountered(spellControllerId);
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(spellControllerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_CONTROLLER_SPELL_COUNTERED, ctx);
        });
    }

    // ── Discard triggers ───────────────────────────────────────────────

    public void beginDiscardEvent(GameData gameData, UUID discardingPlayerId) {
        if (gameData.discardEventPlayerId == null) {
            gameData.discardEventPlayerId = discardingPlayerId;
            gameData.discardEventCardCount = 0;
        } else if (!gameData.discardEventPlayerId.equals(discardingPlayerId)) {
            throw new IllegalStateException("A discard event is already in progress for another player");
        }
    }

    public void finishDiscardEvent(GameData gameData) {
        UUID discardingPlayerId = gameData.discardEventPlayerId;
        int discardedCount = gameData.discardEventCardCount;
        gameData.discardEventPlayerId = null;
        gameData.discardEventCardCount = 0;
        if (discardingPlayerId != null && discardedCount > 0) {
            boolean triggered = checkDiscardEventTriggers(gameData, discardingPlayerId, discardedCount);
            if (triggered) {
                gameOutcomeService.checkWinCondition(gameData);
            }
        }
    }

    public void checkDiscardTriggers(GameData gameData, UUID discardingPlayerId, Card discardedCard) {
        // Central discard hook: every discard path routes through here, so count discards per player
        // for this turn (Dream Salvage's "cards target opponent discarded this turn").
        gameData.cardsDiscardedThisTurn.merge(discardingPlayerId, 1, Integer::sum);
        // Also remember which specific cards were discarded (cycling is a discard) so a later effect can
        // return them from the graveyard (Shadow of the Grave).
        // Remember the mana value of the card just discarded, so a later effect of the same spell can
        // scale off it (Blast of Genius's "damage equal to the discarded card's mana value").
        if (discardedCard != null) {
            gameData.lastDiscardedCardManaValue = discardedCard.getManaValue();
            gameData.greatestDiscardedCardManaValue = Math.max(
                    gameData.greatestDiscardedCardManaValue, discardedCard.getManaValue());
            EnumSet<CardType> cardTypes = EnumSet.noneOf(CardType.class);
            if (discardedCard.getType() != null) {
                cardTypes.add(discardedCard.getType());
            }
            cardTypes.addAll(discardedCard.getAdditionalTypes());
            gameData.lastDiscardedCardTypes = Set.copyOf(cardTypes);
        } else {
            gameData.lastDiscardedCardTypes = Set.of();
        }
        if (discardedCard != null && !discardedCard.isToken()) {
            gameData.cardsDiscardedOrCycledThisTurn
                    .computeIfAbsent(discardingPlayerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(discardedCard.getId());
            if (gameData.discardCausedByOpponent) {
                gameData.cardsDiscardedByOpponentThisTurn
                        .computeIfAbsent(discardingPlayerId, ignored -> ConcurrentHashMap.newKeySet())
                        .add(discardedCard.getId());
            }
        }

        if (discardedCard != null && gameData.discardEventPlayerId != null) {
            gameData.discardEventCardCount++;
        }

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.Discard(discardingPlayerId, discardedCard);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(discardingPlayerId)) return;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DISCARDS)) {
                    // "Whenever an opponent discards a creature/land/noncreature-nonland card, …"
                    // (Waste Not) — the discarded card gates the trigger.
                    CardEffect resolved = discardedCard == null
                            ? (effect instanceof TriggeringCardConditionalEffect ? null : effect)
                            : unwrapTriggeringCardConditional(effect, discardedCard, gameData, playerId);
                    if (resolved == null) continue;
                    if (dispatchSlotEffect(gameData, perm, playerId,
                            EffectSlot.ON_OPPONENT_DISCARDS, ctx, resolved)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        if (collectTemporaryGlobalDiscardTriggers(gameData, discardingPlayerId)) {
            anyTriggered[0] = true;
        }

        // "Whenever you discard a card" — scan the discarding player's own battlefield (e.g. Necropotence).
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(discardingPlayerId);
        if (ownBattlefield != null) {
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_DISCARDS)) {
                    CardEffect resolved = discardedCard == null
                            ? (effect instanceof TriggeringCardConditionalEffect ? null : effect)
                            : unwrapTriggeringCardConditional(effect, discardedCard, gameData, discardingPlayerId);
                    if (resolved == null) continue;
                    boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                    resolved = unwrapOncePerTurnTrigger(gameData, perm, resolved);
                    if (resolved == null) continue;
                    var match = new TriggerMatchContext(gameData, perm, discardingPlayerId, resolved);
                    boolean triggered = dispatch(match, EffectSlot.ON_CONTROLLER_DISCARDS, resolved, ctx);
                    if (triggered && oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                    if (triggered) {
                        anyTriggered[0] = true;
                    }
                }
            }
        }

        if (discardedCard != null && gameData.discardEventPlayerId == null
                && checkDiscardEventTriggers(gameData, discardingPlayerId, 1)) {
            anyTriggered[0] = true;
        }

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }

        // Process any pending may abilities added by discard triggers
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }

        // Check the discarded card itself for self-discard triggers
        if (discardedCard != null) {
            // "When you discard this card" — any discard (Edgar's Awakening). Non-targeting effects
            // (e.g. MayPayManaEffect) go straight onto the stack; any-target effects use the
            // DiscardTriggerAnyTarget pipeline (same as Guerrilla Tactics).
            List<CardEffect> anyDiscardTriggers = discardedCard.getEffects(EffectSlot.ON_SELF_DISCARDED);
            if (!anyDiscardTriggers.isEmpty()) {
                boolean needsAnyTarget = anyDiscardTriggers.stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                if (needsAnyTarget) {
                    gameData.queueInteraction(new PermanentChoiceContext.DiscardTriggerAnyTarget(
                            discardedCard, discardingPlayerId, new ArrayList<>(anyDiscardTriggers)
                    ));
                } else {
                    gameData.enqueueTrigger(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            discardedCard,
                            discardingPlayerId,
                            discardedCard.getName() + "'s ability",
                            new ArrayList<>(anyDiscardTriggers)
                    ));
                }
                gameLogService.append(gameData, GameLog.cardThen(discardedCard,
                        " was discarded — its ability triggers!"));
                log.info("Game {} - {} ON_SELF_DISCARDED trigger queued", gameData.id, discardedCard.getName());
                anyTriggered[0] = true;
            }

            // Skip EnterBattlefieldOnDiscardEffect — it's a replacement effect handled earlier in the discard flow
            if (gameData.discardCausedByOpponent) {
                List<CardEffect> selfTriggers = discardedCard.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT).stream()
                        .filter(e -> !(e instanceof EnterBattlefieldOnDiscardEffect))
                        .toList();
                if (!selfTriggers.isEmpty()) {
                    boolean needsAnyTarget = selfTriggers.stream()
                            .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                    || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                    if (needsAnyTarget) {
                        gameData.queueInteraction(new PermanentChoiceContext.DiscardTriggerAnyTarget(
                                discardedCard, discardingPlayerId, new ArrayList<>(selfTriggers)
                        ));
                    } else {
                        gameData.enqueueTrigger(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                discardedCard,
                                discardingPlayerId,
                                discardedCard.getName() + "'s ability",
                                new ArrayList<>(selfTriggers)
                        ));
                    }
                    gameLogService.append(gameData, GameLog.cardThen(discardedCard, " was discarded by an opponent's effect — its ability triggers!"));
                    log.info("Game {} - {} self-discard trigger queued", gameData.id, discardedCard.getName());
                }
            }
        }
    }

    // ── Source deals damage to a player (noncombat) ────────────────────

    private boolean collectTemporaryGlobalDiscardTriggers(GameData gameData, UUID discardingPlayerId) {
        if (!gameData.discardCausedByOpponent) {
            return false;
        }

        boolean triggered = false;
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (watcher.slot() != EffectSlot.ON_OPPONENT_DISCARDS
                    || !watcher.controllerId().equals(discardingPlayerId)) {
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setTargetId(discardingPlayerId);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global discard trigger fires", gameData.id,
                    watcher.sourceCard().getName());
            triggered = true;
        }
        return triggered;
    }

    /**
     * Queues {@link EffectSlot#ON_DAMAGE_TO_PLAYER} triggers when a permanent deals noncombat
     * damage to a player (e.g. Niv-Mizzet, Dracogenius's ping). Combat damage uses the richer
     * collector in {@code CombatDamageService} instead — do not call this from the combat path.
     */
    public void checkScryTriggers(GameData gameData, UUID scryingPlayerId) {
        checkScryTriggers(gameData, scryingPlayerId, 0);
    }

    public void checkScryTriggers(GameData gameData, UUID scryingPlayerId, int bottomedCardCount) {
        var ctx = new TriggerContext.Scry(scryingPlayerId, bottomedCardCount);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(scryingPlayerId);
        if (ownBattlefield == null) {
            return;
        }

        for (Permanent perm : List.copyOf(ownBattlefield)) {
            dispatchSlot(gameData, perm, scryingPlayerId, EffectSlot.ON_CONTROLLER_SCRIES, ctx);
        }
    }

    public void checkInvestigateTriggers(GameData gameData, UUID investigatingPlayerId) {
        if (!gameData.playersWhoInvestigatedThisTurn.add(investigatingPlayerId)) {
            return;
        }

        var ctx = new TriggerContext.Investigate(investigatingPlayerId);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(investigatingPlayerId);
        if (ownBattlefield == null) {
            return;
        }

        for (Permanent perm : List.copyOf(ownBattlefield)) {
            dispatchSlot(gameData, perm, investigatingPlayerId, EffectSlot.ON_CONTROLLER_INVESTIGATES, ctx);
        }
    }

    public void checkForageTriggers(GameData gameData, UUID foragingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(foragingPlayerId);
        if (battlefield == null) {
            return;
        }
        var context = new TriggerContext.Forage(foragingPlayerId);
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchSlot(gameData, permanent, foragingPlayerId, EffectSlot.ON_CONTROLLER_FORAGES, context);
        }
    }

    public void checkSurveilTriggers(GameData gameData, UUID surveilingPlayerId) {
        gameData.playersWhoSurveilledThisTurn.add(surveilingPlayerId);
        var ctx = new TriggerContext.Surveil(surveilingPlayerId);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(surveilingPlayerId);
        if (ownBattlefield != null) {
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                dispatchSlot(gameData, perm, surveilingPlayerId, EffectSlot.ON_CONTROLLER_SURVEILS, ctx);
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(surveilingPlayerId);
        if (graveyard == null) {
            return;
        }

        for (Card card : List.copyOf(graveyard)) {
            for (CardEffect effect : gameQueryService.getEffectiveGraveyardEffects(
                    gameData, card, EffectSlot.GRAVEYARD_ON_CONTROLLER_SURVEILS)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        surveilingPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard ability triggers on surveil",
                        gameData.id, card.getName());
            }
        }
    }

    public void checkDiscoverTriggers(GameData gameData, UUID discoveringPlayerId, int discoverValue) {
        var ctx = new TriggerContext.Discover(discoveringPlayerId, discoverValue);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(discoveringPlayerId);
        if (ownBattlefield == null) {
            return;
        }
        for (Permanent perm : List.copyOf(ownBattlefield)) {
            dispatchSlot(gameData, perm, discoveringPlayerId, EffectSlot.ON_CONTROLLER_DISCOVERS, ctx);
        }
    }

    public void checkAnyCreatureCombatDamageToOpponentTriggers(GameData gameData, Permanent creature,
                                                               UUID creatureControllerId,
                                                               UUID damagedPlayerId, int damageDealt) {
        if (creature == null || creatureControllerId == null || damagedPlayerId == null || damageDealt <= 0) {
            return;
        }

        gameData.forEachPermanent((controllerId, source) -> {
            if (controllerId.equals(damagedPlayerId) || source.isLosesAllAbilitiesUntilEndOfTurn()) {
                return;
            }

            List<CardEffect> effects = new ArrayList<>(
                    source.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT));
            for (CardEffect effect : effects) {
                CardEffect resolvedEffect = effect;
                boolean matches = true;
                while (resolvedEffect instanceof TriggeringPermanentControllerConditionalEffect conditional) {
                    if (!controllerId.equals(creatureControllerId)) {
                        matches = false;
                        break;
                    }
                    resolvedEffect = conditional.wrapped();
                }
                while (matches && resolvedEffect instanceof TriggeringPermanentConditionalEffect conditional) {
                    if (!predicateEvaluationService.matchesPermanentPredicate(gameData, creature,
                            conditional.predicate())) {
                        matches = false;
                        break;
                    }
                    resolvedEffect = conditional.wrapped();
                }
                if (!matches) {
                    continue;
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        controllerId,
                        source.getCard().getName() + "'s triggered ability",
                        List.of(resolvedEffect),
                        damagedPlayerId,
                        source.getId());
                entry.setTriggeringPermanentId(creature.getId());
                entry.setTriggeringPermanentControllerId(creatureControllerId);
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
                log.info("Game {} - {} triggers when a creature deals combat damage to an opponent",
                        gameData.id, source.getCard().getName());
            }
        });
    }

    public void checkSourceDealsDamageToPlayerTriggers(GameData gameData, Permanent source,
                                                       UUID controllerId, UUID damagedPlayerId,
                                                       int damageDealt) {
        if (source == null || controllerId == null || damagedPlayerId == null || damageDealt <= 0) {
            return;
        }

        List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));
        for (CardEffect effect : effects) {
            queueNoncombatDamageToPlayerEffect(gameData, source, controllerId, damagedPlayerId, damageDealt, effect);
        }

        // Auras/Equipment attached to the source with ON_DAMAGE_TO_PLAYER (e.g. Curiosity).
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!perm.isAttached() || perm.getAttachedTo() == null
                    || !perm.getAttachedTo().equals(source.getId())) {
                return;
            }
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER)) {
                // Attached triggers are controlled by the Aura/Equipment's controller.
                UUID attachedControllerId = gameData.findControllerOf(perm);
                if (attachedControllerId == null) continue;
                queueNoncombatDamageToPlayerEffect(gameData, perm, attachedControllerId, damagedPlayerId,
                        damageDealt, effect);
            }
        });
    }

    private void queueNoncombatDamageToPlayerEffect(GameData gameData, Permanent source, UUID controllerId,
                                                    UUID damagedPlayerId, int damageDealt, CardEffect effect) {
        CardEffect toQueue = effect;
        if (toQueue instanceof ConditionalEffect conditional) {
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forPermanent(source, controllerId))) {
                return;
            }
            toQueue = conditional.wrapped();
        }

        if (toQueue instanceof MayEffect may) {
            int mayEventValue = may.wrapped() instanceof DrawCardEffect draw
                    && draw.amount() instanceof com.github.laxika.magicalvibes.model.amount.EventValue
                    ? damageDealt : 0;
            UUID mayTargetId = may.wrapped().targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    ? null : damagedPlayerId;
            gameData.queueMayAbility(source.getCard(), controllerId, may, mayTargetId, source.getId(), mayEventValue);
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage trigger fires."));
            return;
        }

        String desc = source.getCard().getName() + "'s triggered ability";
        StackEntry se;
        CombatDamageTriggerContextEffect.TriggerContext triggerContext =
                toQueue instanceof CombatDamageTriggerContextEffect contextEffect
                        ? contextEffect.combatDamageTriggerContext()
                        : null;
        if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT) {
            se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    desc, List.of(toQueue), damageDealt, damagedPlayerId, null);
        } else if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.SOURCE_SELF) {
            se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    desc, List.of(toQueue), null, source.getId());
        } else if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER) {
            se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    desc, List.of(toQueue), damagedPlayerId, source.getId());
        } else {
            se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    desc, List.of(toQueue));
        }
        if (toQueue instanceof DiscardEffect
                || (toQueue instanceof DrawCardEffect draw
                        && draw.amount() instanceof com.github.laxika.magicalvibes.model.amount.EventValue)
                || (toQueue instanceof MillEffect mill
                        && mill.count() instanceof com.github.laxika.magicalvibes.model.amount.EventValue)) {
            se.setEventValue(damageDealt);
        }
        se.setNonTargeting(true);
        gameData.stack.add(se);
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage trigger goes on the stack."));
    }

    // ── Damage-dealt-to-controller triggers ────────────────────────────

    public void checkDamageDealtToControllerTriggers(GameData gameData, UUID damagedPlayerId, UUID sourcePermanentId, boolean isCombatDamage) {
        if (sourcePermanentId == null) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        boolean hasTrigger = false;
        for (Permanent perm : damagedPlayerBattlefield) {
            if (!perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU).isEmpty()
                    || (isCombatDamage
                            && !perm.getCard().getEffects(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU).isEmpty())) {
                hasTrigger = true;
                break;
            }
        }
        if (!hasTrigger) return;

        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) return;

        if (isCombatDamage) {
            queueCreatureCombatDamageToYouTriggers(gameData, damagedPlayerId, sourcePermanent);
        }

        var ctx = new TriggerContext.DamageToController(damagedPlayerId, sourcePermanentId, isCombatDamage);

        for (Permanent perm : new ArrayList<>(damagedPlayerBattlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU)) {
                var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                boolean triggered = dispatch(match, EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

                // If the source was bounced, stop processing all triggers
                if (triggered && gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
                    return;
                }
            }
        }
    }

    /**
     * Puts every {@link EffectSlot#ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU} ability on the damaged
     * player's battlefield onto the stack when a creature dealt them combat damage. The whole slot
     * becomes one triggered ability per watching permanent whose {@code targetId} is the damaging
     * creature, so "destroy that creature" is expressed with {@code DestroyTargetPermanentEffect}
     * while the ability itself does not target (CR 115.10a).
     */
    private void queueCreatureCombatDamageToYouTriggers(GameData gameData, UUID damagedPlayerId,
                                                        Permanent sourceCreature) {
        if (!gameQueryService.isCreature(gameData, sourceCreature)) return;

        for (Permanent perm : new ArrayList<>(gameData.playerBattlefields.get(damagedPlayerId))) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU);
            if (effects.isEmpty()) continue;

            StackEntry se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, perm.getCard(), damagedPlayerId,
                    perm.getCard().getName() + "'s triggered ability", List.copyOf(effects),
                    sourceCreature.getId(), perm.getId());
            se.setNonTargeting(true);
            gameData.stack.add(se);
            gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                    "'s combat damage trigger goes on the stack."));
        }
    }

    // ── Enchanted-creature-deals-damage-to-you reflect triggers (Backfire) ──

    /**
     * Handles {@link EffectSlot#ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU} — "Whenever enchanted creature
     * deals damage to you, this Aura deals that much damage to that creature's controller" (Backfire).
     *
     * <p>The aura is on its controller's battlefield, so scanning the damaged player's battlefield for auras
     * attached to the damage source naturally restricts the trigger to damage dealt to the aura's controller
     * ("to you"). Called for both combat and non-combat damage dealt to a player. Queues a triggered ability
     * running {@link EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect}, whose handler deals
     * {@code amount} damage to the enchanted creature's controller.
     */
    public void checkEnchantedCreatureDealtDamageToControllerReflectTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourcePermanentId, int amount) {
        if (sourcePermanentId == null || amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (enchantedCreature == null) return;

        for (Permanent aura : new ArrayList<>(damagedPlayerBattlefield)) {
            if (!aura.isAttached() || !sourcePermanentId.equals(aura.getAttachedTo())) continue;
            if (aura.getCard().getEffects(EffectSlot.ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU).isEmpty()) continue;

            UUID creatureControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
            if (creatureControllerId == null) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    aura.getCard(),
                    damagedPlayerId,
                    aura.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect())),
                    amount,
                    creatureControllerId,
                    aura.getId(),
                    Map.of(),
                    null,
                    List.of(),
                    List.of()
            );
            entry.setDamageSourceCard(enchantedCreature.getCard());
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.abilityTriggers(aura.getCard()));
            log.info("Game {} - {} ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU trigger fires ({} damage)",
                    gameData.id, aura.getCard().getName(), amount);
        }
    }

    // ── Controller-dealt-damage triggers (Living Artifact) ─────────────

    /**
     * Handles {@link EffectSlot#ON_CONTROLLER_DEALT_DAMAGE} — "Whenever you're dealt damage, ...".
     * Fires once per damage source (per the CR ruling that simultaneous sources trigger separately),
     * carrying only the amount so an {@code EventValue} amount ("put that many counters") can read it.
     * Scans the damaged player's own battlefield.
     * <p>
     * Also handles {@link EffectSlot#ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT} — "Whenever a source an
     * opponent controls deals damage to you, ..." (Retaliator Griffin) — but only when the damage
     * source is controlled by an opponent of the damaged player. {@code sourceControllerId} is the
     * controller of the damage source (the active player for combat damage to the defender, the
     * spell/ability's controller for non-combat damage); {@code null} disables the opponent-gated slot.
     */
    public void checkControllerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        if (amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, null,
                sourceControllerId);
        boolean fromOpponent = sourceControllerId != null && !sourceControllerId.equals(damagedPlayerId);

        for (Permanent perm : List.copyOf(damagedPlayerBattlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE)) {
                var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                dispatch(match, EffectSlot.ON_CONTROLLER_DEALT_DAMAGE, effect, ctx);
            }
            if (fromOpponent) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT)) {
                    var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                    dispatch(match, EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT, effect, ctx);
                }
            }
        }
    }

    /**
     * Handles {@link EffectSlot#ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT} — "Whenever a source you
     * control deals damage to another player, ..." (Night Dealings). The outbound mirror of
     * {@link #checkControllerDealtDamageTriggers}: it scans the damage source's controller's own
     * battlefield, and only fires when the damaged player is someone else.
     * The source permanent ID is carried for effects that are self-scoped within this slot.
     */
    public void checkAllySourceDealtDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        checkAllySourceDealtDamageToOpponentTriggers(gameData, damagedPlayerId, sourceControllerId, null, amount);
    }

    public void checkAllySourceDealtNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        if (amount <= 0 || sourceControllerId == null || sourceControllerId.equals(damagedPlayerId)) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;
        var ctx = new TriggerContext.NoncombatDamageToOpponent(damagedPlayerId, sourceControllerId, amount);
        for (Permanent permanent : List.copyOf(battlefield)) {
            for (CardEffect effect : permanent.getCard().getEffects(
                    EffectSlot.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT)) {
                var match = new TriggerMatchContext(gameData, permanent, sourceControllerId, effect);
                dispatch(match, EffectSlot.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT, effect, ctx);
            }
        }
    }

    public void checkAllySourceDealtDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, UUID sourcePermanentId, int amount) {
        if (amount <= 0 || sourceControllerId == null || sourceControllerId.equals(damagedPlayerId)) return;

        List<Permanent> sourceControllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (sourceControllerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, sourcePermanentId);
        for (Permanent perm : List.copyOf(sourceControllerBattlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT)) {
                var match = new TriggerMatchContext(gameData, perm, sourceControllerId, effect);
                dispatch(match, EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT, effect, ctx);
            }
        }
    }

    /**
     * Handles effects that trigger whenever an opponent is dealt damage, regardless of the damage
     * source's controller.
     */
    public void checkOpponentDealtDamageTriggers(GameData gameData, UUID damagedPlayerId, int amount) {
        checkOpponentDealtDamageTriggers(gameData, damagedPlayerId, null, amount);
    }

    public void checkOpponentDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourcePermanentId, int amount) {
        if (amount <= 0 || damagedPlayerId == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, sourcePermanentId);
        gameData.forEachBattlefield((watcherPlayerId, battlefield) -> {
            if (watcherPlayerId.equals(damagedPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DEALT_DAMAGE)) {
                    var match = new TriggerMatchContext(gameData, perm, watcherPlayerId, effect);
                    registry.dispatch(match, EffectSlot.ON_OPPONENT_DEALT_DAMAGE, effect, ctx);
                }
            }
        });
    }

    /**
     * Handles {@link EffectSlot#ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT} — "Whenever a
     * creature of the chosen color deals damage to you or a white creature you control, ...".
     * Scans the damaged player's battlefield for watchers; the per-watcher chosen-color and
     * damaged-permanent filtering happens in the dispatched collector.
     *
     * @param damagedPlayerId  the damaged player, or the damaged permanent's controller
     * @param damagedPermanent the damaged permanent, or {@code null} when the player was damaged
     */
    public void checkCreatureDamageToYouOrYourPermanentTriggers(GameData gameData, UUID damagedPlayerId,
            Permanent damagedPermanent, Permanent damageSource, int damage) {
        if (damage <= 0 || damagedPlayerId == null || damageSource == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CreatureDamageToYouOrYourPermanent(
                damageSource, damagedPlayerId, damagedPermanent, damage);
        for (Permanent perm : List.copyOf(battlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT)) {
                var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                dispatch(match, EffectSlot.ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT, effect, ctx);
            }
        }
    }

    /**
     * Fires triggers for each player or permanent recipient of damage during one damage event.
     * A permanent recipient is retained separately from its controller because cards may trigger
     * once for each permanent damaged, in addition to a trigger for damage dealt to the player.
     */
    public void checkOpponentSourceDamageToYouOrYourPermanentTriggers(GameData gameData, Card sourceCard,
            UUID sourceControllerId, UUID sourcePermanentId, Set<UUID> damagedPlayerIds) {
        if (sourceCard == null || sourceControllerId == null || damagedPlayerIds == null
                || damagedPlayerIds.isEmpty()) return;

        List<SourceDamageRecipient> recipients = damagedPlayerIds.stream()
                .filter(Objects::nonNull)
                .map(playerId -> new SourceDamageRecipient(playerId, null))
                .toList();
        checkOpponentSourceDamageToYouOrYourPermanentTriggers(
                gameData, sourceCard, sourceControllerId, sourcePermanentId, recipients);
    }

    public void checkOpponentSourceDamageToYouOrYourPermanentTriggers(GameData gameData, Card sourceCard,
            UUID sourceControllerId, UUID sourcePermanentId, Collection<SourceDamageRecipient> recipients) {
        if (sourceCard == null || sourceControllerId == null || recipients == null || recipients.isEmpty()) return;

        for (SourceDamageRecipient recipient : recipients) {
            if (recipient == null || recipient.damagedPlayerId() == null) continue;
            UUID damagedPlayerId = recipient.damagedPlayerId();
            List<Permanent> battlefield = gameData.playerBattlefields.get(damagedPlayerId);
            if (battlefield == null) continue;

            TriggerContext.SourceDamageToYouOrYourPermanent ctx =
                    new TriggerContext.SourceDamageToYouOrYourPermanent(
                            sourceCard, sourceControllerId, sourcePermanentId, damagedPlayerId,
                            recipient.damagedPermanentId());
            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(
                        EffectSlot.ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT)) {
                    var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                    registry.dispatch(match,
                            EffectSlot.ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT,
                            effect, ctx);
                }
            }
        }
    }

    public record SourceDamageRecipient(UUID damagedPlayerId, UUID damagedPermanentId) {
    }

    // ── Any-source-deals-damage triggers (Justice) ─────────────────────

    /**
     * Handles {@link EffectSlot#ON_ANY_SOURCE_DEALS_DAMAGE} — "Whenever a [color] creature or spell
     * deals damage, ...". Scans every battlefield for permanents with this slot and dispatches the
     * batched damage event (already summed across simultaneous targets) so each watcher can react
     * once. Callers pass the single summed total per source per damage event.
     */
    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, Map.of());
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, null);
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<CardEffect> snapshottedSelfEffects) {
        if (sourceCard == null || sourceControllerId == null || totalDamage <= 0) return;

        var ctx = new TriggerContext.SourceDealsDamage(sourceCard, sourceControllerId, sourcePermanentId, totalDamage,
                damageToPlayers == null ? Map.of() : Map.copyOf(damageToPlayers));
        gameData.forEachBattlefield((watcherPlayerId, battlefield) -> {
            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE)) {
                    var match = new TriggerMatchContext(gameData, perm, watcherPlayerId, effect);
                    dispatch(match, EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE, effect, ctx);
                }
            }
        });

        // Blaze Commando: "whenever an instant or sorcery spell you control deals damage" — only the
        // spell's controller's battlefield watches, and only instant/sorcery sources qualify.
        if (sourceCard.hasType(CardType.INSTANT) || sourceCard.hasType(CardType.SORCERY)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield != null) {
                for (Permanent perm : List.copyOf(battlefield)) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE)) {
                        var match = new TriggerMatchContext(gameData, perm, sourceControllerId, effect);
                        dispatch(match, EffectSlot.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE, effect, ctx);
                    }
                }
            }
        }

        // Self triggers (El-Hajjâj): only the damage source's own "whenever this creature deals
        // damage" abilities fire. Keyed off the source card (not a battlefield scan) so it still
        // triggers when the source died dealing that damage.
        List<CardEffect> selfEffects = snapshottedSelfEffects == null
                ? new ArrayList<>(sourceCard.getEffects(EffectSlot.ON_SELF_DEALS_DAMAGE))
                : new ArrayList<>(snapshottedSelfEffects);
        // Granted "whenever this creature deals damage" abilities live on the permanent, not the card
        // (the Genju cycle grants one to the animated land until end of turn). If no snapshot was
        // captured, the source is still on the battlefield and can be read here.
        Permanent sourcePermanent = findPermanentByCardId(gameData, sourceCard.getId());
        if (snapshottedSelfEffects == null && sourcePermanent != null) {
            selfEffects.addAll(sourcePermanent.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE));
            selfEffects.addAll(sourcePermanent.getPersistentTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE));
            selfEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, sourcePermanent, EffectSlot.ON_SELF_DEALS_DAMAGE));
        }
        for (CardEffect effect : selfEffects) {
            var match = new TriggerMatchContext(gameData, sourcePermanent, sourceControllerId, effect);
            dispatch(match, EffectSlot.ON_SELF_DEALS_DAMAGE, effect, ctx);
        }

        for (DelayedWatchedCreatureDealsDamage watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealsDamage.class)) {
            if (!watch.watchedPermanentId().equals(sourcePermanentId)) continue;

            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    watch.watchedPermanentId(),
                    (UUID) null);
            trigger.setNonTargeting(true);
            trigger.setEventValue(totalDamage);
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
        }
    }

    /**
     * Fires delayed triggers watching a Wall for damage from an attacking creature. The source's
     * attacking status is checked at the damage event, so noncombat damage from a spell and damage
     * from a blocking creature do not qualify.
     */
    public void checkDelayedWatchedCreatureDealtDamageByAttackingCreatureTriggers(
            GameData gameData, Permanent damageSource, Permanent damagedCreature, int damage) {
        if (damageSource == null || damagedCreature == null || damage <= 0
                || !damageSource.isAttacking() || !gameQueryService.isCreature(gameData, damageSource)) {
            return;
        }

        for (DelayedWatchedCreatureDealtDamageByAttackingCreature watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealtDamageByAttackingCreature.class)) {
            if (!watch.watchedPermanentId().equals(damagedCreature.getId())) continue;

            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    watch.watchedPermanentId(),
                    (UUID) null);
            trigger.setNonTargeting(true);
            trigger.setEventValue(damage);
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
        }
    }

    public void queueEnchantedCreatureDealsDamageTriggers(GameData gameData, Permanent sourceCreature,
                                                           int damageDealt) {
        if (sourceCreature == null || damageDealt <= 0) return;

        List<StackEntry> entries = new ArrayList<>();
        collectEnchantedCreatureDealsDamageTriggers(gameData, sourceCreature, damageDealt, entries);
        entries.forEach(gameData::enqueueTrigger);
    }

    public void collectEnchantedCreatureDealsDamageTriggers(GameData gameData, Permanent sourceCreature,
                                                             int damageDealt, List<StackEntry> entries) {
        if (sourceCreature == null || damageDealt <= 0 || entries == null) return;

        gameData.forEachPermanent((auraControllerId, aura) -> {
            if (!aura.isAttached() || !sourceCreature.getId().equals(aura.getAttachedTo())) return;

            List<CardEffect> effects = aura.getCard().getEffects(EffectSlot.ON_ENCHANTED_CREATURE_DEALS_DAMAGE);
            if (effects.isEmpty()) return;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    aura.getCard(),
                    auraControllerId,
                    aura.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    auraControllerId,
                    aura.getId());
            entry.setEventValue(damageDealt);
            entries.add(entry);
        });
    }

    /**
     * Collects Aura triggers for a creature dealing damage to another creature. The damaged
     * creature is captured before state-based actions run, so the trigger still exists when either
     * combat creature dies from the same damage event.
     */
    public void collectEnchantedCreatureDealsDamageToCreatureTriggers(GameData gameData,
                                                                        Permanent sourceCreature,
                                                                        UUID damagedCreatureId,
                                                                        int damageDealt,
                                                                        List<StackEntry> entries) {
        if (sourceCreature == null || damagedCreatureId == null || damageDealt <= 0
                || sourceCreature.getId().equals(damagedCreatureId)
                || !gameQueryService.isCreature(gameData, sourceCreature)
                || entries == null) {
            return;
        }

        gameData.forEachPermanent((auraControllerId, aura) -> {
            if (!aura.isAttached() || !sourceCreature.getId().equals(aura.getAttachedTo())) return;

            List<CardEffect> effects = aura.getCard().getEffects(
                    EffectSlot.ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_CREATURE);
            if (effects.isEmpty()) return;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    aura.getCard(),
                    auraControllerId,
                    aura.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    damagedCreatureId,
                    aura.getId());
            entry.setNonTargeting(true);
            entries.add(entry);
        });
    }

    public void queueEnchantedCreatureDealsDamageToCreatureTriggers(GameData gameData,
                                                                      Permanent sourceCreature,
                                                                      UUID damagedCreatureId,
                                                                      int damageDealt) {
        List<StackEntry> entries = new ArrayList<>();
        collectEnchantedCreatureDealsDamageToCreatureTriggers(
                gameData, sourceCreature, damagedCreatureId, damageDealt, entries);
        entries.forEach(gameData::enqueueTrigger);
    }

    /** The battlefield permanent whose card has this id, or null once it has left the battlefield. */
    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        List<Permanent> found = new ArrayList<>(1);
        gameData.forEachPermanent((playerId, perm) -> {
            if (perm.getCard().getId().equals(cardId)) {
                found.add(perm);
            }
        });
        return found.isEmpty() ? null : found.getFirst();
    }

    // ── Land-tap triggers ──────────────────────────────────────────────

    /**
     * Dispatches combat-damage-only self triggers from the source card. The source permanent ID is
     * retained even when state-based actions removed the source before triggered abilities were put
     * on the stack, so effects that require the source to have survived can check it at resolution.
     */
    public void queueSourceDealsCombatDamageTriggers(GameData gameData, Card sourceCard,
                                                      UUID sourceControllerId, UUID sourcePermanentId,
                                                      int totalDamage, int damageToPlayers,
                                                      List<CardEffect> snapshottedSelfEffects) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) {
            return;
        }

        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers);
        dispatchSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, snapshottedSelfEffects, EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE);

        // "Whenever a creature you control deals combat damage" watchers (Five-Alarm Fire). Scanned on
        // the damage source's controller's battlefield only; the watcher itself needn't be a creature.
        if (!sourceCard.hasType(CardType.CREATURE)) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;
        for (Permanent watcher : List.copyOf(battlefield)) {
            for (CardEffect effect : watcher.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE)) {
                var match = new TriggerMatchContext(gameData, watcher, sourceControllerId, effect);
                dispatch(match, EffectSlot.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE, effect, ctx);
            }
        }

        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcher.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    || !isEquippedBy(gameData, watcher, sourcePermanentId)) {
                return;
            }
            for (CardEffect effect : watcher.getCard().getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE)) {
                var match = new TriggerMatchContext(gameData, watcher, watcherControllerId, effect);
                registry.dispatch(match, EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE, effect, ctx);
            }
        });
    }

    /** Dispatches the player-or-battle combat-damage trigger on Equipment attached to the source. */
    private void queueEquippedCreatureDealsCombatDamageToPlayerOrBattleTriggers(GameData gameData,
                                                                                  Card sourceCard,
                                                                                  UUID sourceControllerId,
                                                                                  UUID sourcePermanentId,
                                                                                  int totalDamage) {
        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage);
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcher.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    || !isEquippedBy(gameData, watcher, sourcePermanentId)) {
                return;
            }
            for (CardEffect effect : watcher.getCard().getEffects(
                    EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE)) {
                var match = new TriggerMatchContext(gameData, watcher, watcherControllerId, effect);
                registry.dispatch(match, EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE,
                        effect, ctx);
            }
        });
    }

    /**
     * Handles {@link EffectSlot#ON_SELF_TAPPED_FOR_MANA} — "Whenever you tap this permanent for
     * mana, …" (Zhur-Taa Druid). Only the tapped permanent's own card is scanned, and only the
     * mana-ability tap path calls this, so tapping to attack or an opponent's forced tap never
     * triggers it. The caller defers the queued trigger like every other mana-ability trigger
     * (CR 603.3).
     *
     * @param gameData        the current game state
     * @param tappedPermanent the permanent that was just tapped for mana
     * @param controllerId    the controller of that permanent (also the player who tapped it)
     */
    public void checkSelfTappedForManaTriggers(GameData gameData, Permanent tappedPermanent, UUID controllerId) {
        List<CardEffect> effects = new ArrayList<>(
                tappedPermanent.getCard().getEffects(EffectSlot.ON_SELF_TAPPED_FOR_MANA));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                gameData, tappedPermanent, EffectSlot.ON_SELF_TAPPED_FOR_MANA));
        if (effects.isEmpty()) return;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                tappedPermanent.getCard(),
                controllerId,
                tappedPermanent.getCard().getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                tappedPermanent.getId());
        entry.setTriggeringPermanentId(tappedPermanent.getId());
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(tappedPermanent.getCard()));
        log.info("Game {} - {} triggers on being tapped for mana", gameData.id, tappedPermanent.getCard().getName());
    }

    public void checkManaAbilityResolutionTriggers(GameData gameData, Permanent sourcePermanent,
                                                    UUID controllerId, int manaProduced) {
        if (manaProduced <= 0) return;

        List<CardEffect> effects = new ArrayList<>(sourcePermanent.getCard().getEffects(
                EffectSlot.ON_SELF_MANA_ABILITY_RESOLVES));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                gameData, sourcePermanent, EffectSlot.ON_SELF_MANA_ABILITY_RESOLVES));
        if (effects.isEmpty()) return;

        TriggerContext context = new TriggerContext.ManaAbilityResolved(controllerId, manaProduced);
        for (CardEffect effect : effects) {
            boolean oncePerTurn = effect instanceof OncePerTurnTriggerEffect;
            CardEffect resolved = unwrapOncePerTurnTrigger(gameData, sourcePermanent, effect);
            if (resolved == null) continue;
            boolean triggered = dispatch(new TriggerMatchContext(
                    gameData, sourcePermanent, controllerId, effect),
                    EffectSlot.ON_SELF_MANA_ABILITY_RESOLVES, resolved, context);
            if (triggered && oncePerTurn) {
                gameData.oncePerTurnTriggersFiredThisTurn.add(sourcePermanent.getId());
            }
        }
    }

    public void checkLandTapTriggers(GameData gameData, UUID tappingPlayerId, UUID tappedLandId) {
        // Desolation et al.: track who tapped a land for mana this turn even if no land-tap
        // trigger permanent is currently on the battlefield (2004-10-04 ruling).
        gameData.playersWhoTappedLandForManaThisTurn.add(tappingPlayerId);

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.LandTap(tappingPlayerId, tappedLandId);

        // Snapshot each battlefield so trigger collection remains stable if a collector mutates it.
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (dispatch(match, EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        applyTurnScopedExtraLandTapMana(gameData, tappingPlayerId, tappedLandId);

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    /**
     * Applies {@code GameData.extraManaOnLandSubtypeTapThisTurn} — a turn-scoped, symmetric
     * "whenever a player taps a land of this subtype for mana, that player adds an additional
     * {X}" granted by a resolved effect rather than a permanent's static ability (Chaos Moon's
     * odd branch). Like the static land-tap triggers above it is a triggered mana ability, so it
     * pays straight into the tapping player's pool without using the stack.
     */
    private void applyTurnScopedExtraLandTapMana(GameData gameData, UUID tappingPlayerId, UUID tappedLandId) {
        if (gameData.extraManaOnLandSubtypeTapThisTurn.isEmpty()) {
            return;
        }
        Permanent tappedLand = gameQueryService.findPermanentById(gameData, tappedLandId);
        if (tappedLand == null) {
            return;
        }
        Set<CardSubtype> types = gameQueryService.effectiveBasicLandTypes(gameData, tappedLand);
        ManaPool pool = gameData.playerManaPools.get(tappingPlayerId);
        if (pool == null) {
            return;
        }
        for (var entry : gameData.extraManaOnLandSubtypeTapThisTurn.entrySet()) {
            if (!types.contains(entry.getKey())) {
                continue;
            }
            pool.add(entry.getValue());
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(tappingPlayerId) + " adds 1 additional "
                            + entry.getValue().getCode() + " mana.")
                    .build());
        }
    }

    // ── Permanent-returned-to-hand triggers ────────────────────────────

    /**
     * Handles {@link EffectSlot#ON_ANY_PERMANENT_RETURNED_TO_HAND} — "Whenever a permanent is returned
     * to a player's hand, ...". Scans every battlefield for permanents with this slot and queues one
     * triggered ability per matching permanent, with {@code returnedToPlayerId} (the owner the permanent
     * was returned to) set as the non-targeting {@code targetId} so player-directed effects act on
     * "that player". Used by Warped Devotion.
     */
    public void checkPermanentReturnedToHandTriggers(GameData gameData, UUID returnedToPlayerId) {
        if (returnedToPlayerId == null) return;

        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;

            for (Permanent perm : new ArrayList<>(battlefield)) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_RETURNED_TO_HAND);
                if (effects.isEmpty()) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
                        returnedToPlayerId,
                        perm.getId());
                // "That player" is the owner the permanent returned to — determined by the event, not chosen.
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} permanent-returned-to-hand trigger pushed onto stack",
                        gameData.id, perm.getCard().getName());
            }
        }
    }

    /**
     * Handles {@link EffectSlot#ON_CONTROLLER_CREATURE_RETURNED_TO_HAND} — "Whenever a creature is
     * returned to your hand from the battlefield, ...". The watcher is checked before the returned
     * permanent leaves, so the returned permanent itself can trigger. The returned card must be a
     * creature and its owner must be the watching permanent's controller.
     */
    public void checkControllerCreatureReturnedToHandTriggers(GameData gameData,
                                                               Permanent returnedPermanent,
                                                               boolean wasCreature,
                                                               UUID returnedToPlayerId) {
        if (!wasCreature || returnedToPlayerId == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(returnedToPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_CREATURE_RETURNED_TO_HAND)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        returnedToPlayerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on a creature returned to its controller's hand ({})",
                        gameData.id, perm.getCard().getName(), returnedPermanent.getCard().getName());
            }
        }
    }

    // ── Ally-permanent-sacrificed triggers ──────────────────────────────

    /**
     * Handles {@link EffectSlot#ON_CONTROLLER_PERMANENT_RETURNED_TO_HAND} — "Whenever a permanent
     * is returned to your hand, ...". The watcher is checked before the returned permanent leaves,
     * so the returned permanent itself can trigger.
     */
    public void checkControllerPermanentReturnedToHandTriggers(GameData gameData,
                                                                UUID returnedToPlayerId) {
        if (returnedToPlayerId == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(returnedToPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_PERMANENT_RETURNED_TO_HAND)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        returnedToPlayerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on a permanent returned to its controller's hand",
                        gameData.id, perm.getCard().getName());
            }
        }
    }

    public void checkAllyPermanentSacrificedTriggers(GameData gameData, UUID sacrificingPlayerId, Card sacrificedCard) {
        checkAllyPermanentSacrificedTriggers(gameData, sacrificingPlayerId, sacrificedCard, null);
    }

    public void checkAllyPermanentSacrificedTriggers(GameData gameData, UUID sacrificingPlayerId,
                                                     Card sacrificedCard, Card castingSpell) {
        gameData.playersWhoSacrificedPermanentsThisTurn.add(sacrificingPlayerId);
        gameData.recordSacrificedPermanent(sacrificingPlayerId, sacrificedCard);
        checkGraveyardAllyPermanentSacrificedTriggers(gameData, sacrificingPlayerId, sacrificedCard);
        List<Permanent> battlefield = gameData.playerBattlefields.get(sacrificingPlayerId);
        if (battlefield != null) {
            var ctx = new TriggerContext.AllySacrificed(sacrificingPlayerId, sacrificedCard);

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    boolean oncePerTurn = effect instanceof OncePerTurnTriggerEffect;
                    CardEffect resolved = unwrapOncePerTurnTrigger(gameData, perm, effect);
                    if (resolved == null) continue;
                    var match = new TriggerMatchContext(gameData, perm, sacrificingPlayerId, effect);
                    if (dispatch(match, EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, resolved, ctx)
                            && oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                }
            }
        }

        checkOpponentPermanentSacrificedTriggers(gameData, sacrificingPlayerId, sacrificedCard);
        checkOpponentNontokenPermanentSacrificedTriggers(gameData, sacrificingPlayerId, sacrificedCard);

        // Fire the global "whenever a player sacrifices a creature" watchers for the
        // sacrifice-self / sacrifice-as-cost paths that funnel through this method.
        checkAnyCreatureSacrificedTriggers(gameData, sacrificingPlayerId, sacrificedCard);

        checkAnyPermanentSacrificedTriggers(gameData, sacrificingPlayerId, sacrificedCard);
        // "When you sacrifice this" — the sacrificed card's own sacrifice-only death triggers
        collectSelfSacrificedTriggers(gameData, sacrificingPlayerId, sacrificedCard, castingSpell);

        playerInputService.processNextMayAbility(gameData);
    }

    /** Fires sacrifice triggers from cards in the sacrificing player's graveyard. */
    public void checkGraveyardAllyPermanentSacrificedTriggers(GameData gameData,
                                                               UUID sacrificingPlayerId,
                                                               Card sacrificedCard) {
        if (sacrificedCard == null) return;

        List<Card> graveyard = gameData.playerGraveyards.get(sacrificingPlayerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            if (card.getId().equals(sacrificedCard.getId())) continue;

            List<CardEffect> effects = card.getEffects(EffectSlot.GRAVEYARD_ON_CONTROLLER_PERMANENT_SACRIFICED);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(
                        effect, sacrificedCard, gameData, sacrificingPlayerId);
                if (resolved == null) continue;

                if (resolved instanceof MayEffect may) {
                    gameData.queueMayAbility(card, sacrificingPlayerId, may);
                } else {
                    gameData.enqueueTrigger(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            sacrificingPlayerId,
                            card.getName() + "'s ability",
                            new ArrayList<>(List.of(resolved))
                    ));
                }
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard permanent-sacrifice trigger queued",
                        gameData.id, card.getName());
            }
        }
    }

    /**
     * Fires triggers for permanents controlled by players other than the player who sacrificed the
     * permanent. Unlike the nontoken-specific slot, this also sees artifact and other permanent
     * tokens. The sacrificing player is carried as the trigger's non-targeting player reference.
     */
    public void checkOpponentPermanentSacrificedTriggers(GameData gameData,
                                                         UUID sacrificingPlayerId,
                                                         Card sacrificedCard) {
        if (sacrificedCard == null) return;

        var ctx = new TriggerContext.OpponentPermanentSacrificed(
                sacrificingPlayerId, sacrificedCard);
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(sacrificingPlayerId)) continue;

            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;

            for (Permanent perm : new ArrayList<>(battlefield)) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_PERMANENT_SACRIFICED);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
                    dispatch(match, EffectSlot.ON_OPPONENT_PERMANENT_SACRIFICED, effect, ctx);
                }
            }
        }
    }

    /**
     * Fires triggers for permanents controlled by players other than the player who sacrificed the
     * nontoken permanent. The sacrificed card is carried on the trigger so effects can still find
     * it in its owner's graveyard when the ability resolves.
     */
    public void checkOpponentNontokenPermanentSacrificedTriggers(GameData gameData,
                                                                  UUID sacrificingPlayerId,
                                                                  Card sacrificedCard) {
        if (sacrificedCard == null || sacrificedCard.isToken()) return;

        var ctx = new TriggerContext.OpponentNontokenPermanentSacrificed(
                sacrificingPlayerId, sacrificedCard);
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(sacrificingPlayerId)) continue;

            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;

            for (Permanent perm : new ArrayList<>(battlefield)) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
                    dispatch(match, EffectSlot.ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED, effect, ctx);
                }
            }
        }
    }

    /**
     * Fires {@link EffectSlot#ON_ANY_CREATURE_SACRIFICED} global watchers ("Whenever a player
     * sacrifices a creature", e.g. Thraximundar). Scans every battlefield, once per sacrificed
     * creature (creature-ness decided by last-known info on {@code sacrificedCard}); the trigger
     * belongs to the scanning permanent's own controller. Called from the two sacrifice choke
     * points — {@code DestructionSupport.sacrificeAndLog} (edict / chosen sacrifices) and
     * {@link #checkAllyPermanentSacrificedTriggers} (sacrifice-self / sacrifice-as-cost) — which are
     * mutually exclusive per event, so a single sacrifice never double-fires. Queues only; the may
     * abilities are drained by the caller / main resolution loop.
     */
    public void checkAnyCreatureSacrificedTriggers(GameData gameData, UUID sacrificingPlayerId, Card sacrificedCard) {
        if (sacrificedCard == null || !sacrificedCard.hasType(CardType.CREATURE)) return;

        var ctx = new TriggerContext.AllySacrificed(sacrificingPlayerId, sacrificedCard);

        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;

            for (Permanent perm : new ArrayList<>(battlefield)) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_SACRIFICED);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
                    dispatch(match, EffectSlot.ON_ANY_CREATURE_SACRIFICED, effect, ctx);
                }
            }
        }
    }

    /**
     * Fires global watchers for every permanent sacrificed by any player. The trigger belongs to
     * the permanent's controller, so target selection and the resulting stack entry use that
     * controller rather than the player who sacrificed the permanent.
     */
    public void checkAnyPermanentSacrificedTriggers(GameData gameData, UUID sacrificingPlayerId,
                                                    Card sacrificedCard) {
        if (sacrificedCard == null) return;

        var ctx = new TriggerContext.PermanentSacrificed(sacrificingPlayerId, sacrificedCard);
        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;

            for (Permanent perm : new ArrayList<>(battlefield)) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_SACRIFICED);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
                    dispatch(match, EffectSlot.ON_ANY_PERMANENT_SACRIFICED, effect, ctx);
                }
            }
        }
    }

    /**
     * Collects ON_DEATH effects that only trigger when the permanent was sacrificed
     * ({@link CardEffect#onlyTriggersOnSacrifice()}). Called from the sacrifice path after
     * the permanent has already left the battlefield.
     */
    private void collectSelfSacrificedTriggers(GameData gameData, UUID sacrificingPlayerId,
                                               Card sacrificedCard, Card castingSpell) {
        if (sacrificedCard == null) return;
        List<CardEffect> deathEffects = sacrificedCard.getEffects(EffectSlot.ON_DEATH);
        if (deathEffects == null || deathEffects.isEmpty()) return;

        boolean wasCreature = sacrificedCard.hasType(CardType.CREATURE);
        var ctx = new TriggerContext.SelfDeath(sacrificedCard, sacrificingPlayerId, wasCreature, null, castingSpell);
        Permanent perm = new Permanent(sacrificedCard);
        for (CardEffect effect : deathEffects) {
            if (!effect.onlyTriggersOnSacrifice()) continue;
            CardEffect resolvedEffect = unwrapCreatureDeathConditional(effect, sacrificedCard, null, gameData, sacrificingPlayerId);
            if (resolvedEffect == null) continue;
            if (!passesInterveningIf(gameData, perm, sacrificingPlayerId, resolvedEffect)) continue;
            var match = new TriggerMatchContext(gameData, perm, sacrificingPlayerId, resolvedEffect);
            dispatch(match, EffectSlot.ON_DEATH, resolvedEffect, ctx);
        }
    }

    // ── Becomes-target-of-spell triggers ───────────────────────────────

    public void checkBecomesTargetOfSpellTriggers(GameData gameData) {
        if (gameData.stack.isEmpty()) return;
        StackEntry spellEntry = null;
        for (int i = gameData.stack.size() - 1; i >= 0; i--) {
            StackEntry candidate = gameData.stack.get(i);
            if (candidate.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                    && candidate.getEntryType() != StackEntryType.ACTIVATED_ABILITY) {
                spellEntry = candidate;
                break;
            }
        }
        if (spellEntry == null) return;
        checkBecomesTargetOfSpellTriggers(gameData, spellEntry);
        checkTargetChoiceTriggers(gameData, spellEntry);
    }

    public void checkBecomesTargetOfSpellTriggers(GameData gameData, StackEntry spellEntry) {
        if (spellEntry.getTargetZone() == Zone.STACK) {
            if (spellEntry.getTargetId() != null) {
                StackEntry targetedEntry = findStackEntryByCardId(gameData, spellEntry.getTargetId());
                collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, spellEntry);
            } else {
                for (UUID targetId : spellEntry.getTargetIds()) {
                    StackEntry targetedEntry = findStackEntryByCardId(gameData, targetId);
                    collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, spellEntry);
                }
            }
        }

        List<UUID> targetIds = new ArrayList<>();
        if (spellEntry.getTargetId() != null
                && spellEntry.getTargetZone() == null) {
            targetIds.add(spellEntry.getTargetId());
        }
        if (spellEntry.getTargetIds() != null) {
            targetIds.addAll(spellEntry.getTargetIds());
        }

        Set<UUID> targetedPlayers = new HashSet<>();
        Set<UUID> targetControllersWithAllyTrigger = new HashSet<>();

        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) {
                if (targetControllersWithAllyTrigger.add(targetId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(gameData, targetId, spellEntry);
                }
                if (targetedPlayers.add(targetId)) {
                    collectControllerBecomesTargetOfSpellTriggers(gameData, targetId, spellEntry);
                    collectControllerBecomesTargetOfOpponentTriggers(gameData, targetId, spellEntry);
                }
                continue;
            }

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());
            if (controllerId == null) continue;

            if (targetControllersWithAllyTrigger.add(controllerId)) {
                collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(gameData, controllerId, spellEntry);
            }
            collectAllyPermanentBecomesTargetOfOpponentTriggers(gameData, controllerId, spellEntry);
            collectAnotherAllyPermanentBecomesTargetOfOpponentTriggers(
                    gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureBecomesTargetOfOpponentTriggers(gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
                    gameData, targetPermanent, controllerId, spellEntry);
            collectAnyCreatureBecomesTargetTriggers(gameData, targetPermanent, spellEntry);
            collectOpponentCreatureBecomesTargetOfYourSpellTriggers(gameData, targetPermanent, controllerId, spellEntry);
            collectAllyCreatureBecomesTargetOfSpellTriggers(gameData, targetPermanent, controllerId);
            collectAllyCreatureBecomesTargetOfInstantOrSorceryTriggers(gameData, targetPermanent, controllerId, spellEntry);
            // Check the targeted permanent itself for "when this becomes the target" triggers.
            // Attached permanents (auras/equipment) use the loop below instead — their triggers
            // monitor the enchanted/equipped creature, not themselves (Spectral Prison is not
            // sacrificed when a spell targets the Aura rather than the enchanted creature).
            if (!targetPermanent.isAttached()) {
                collectBecomesTargetTriggers(gameData, targetPermanent, controllerId, targetPermanent, spellEntry);
                collectBecomesTargetOfAuraSpellTriggers(gameData, targetPermanent, controllerId, spellEntry);
                collectBecomesTargetOfOpponentCounterTriggers(gameData, targetPermanent, controllerId, spellEntry);
                collectBecomesTargetOfOpponentSpellOnlyTriggers(gameData, targetPermanent, controllerId,
                        spellEntry.getControllerId());
                collectBecomesTargetOfSpellOrAbilityTriggers(gameData, targetPermanent, controllerId, spellEntry);
                collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
                        gameData, targetPermanent, controllerId, spellEntry.getControllerId());
            }

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.isAttached()
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        // CR 603.3a: the triggered ability is controlled by the controller of the
                        // permanent that has it (the aura/equipment), not the enchanted creature —
                        // the two differ when an Aura like Spectral Prison enchants an opponent's creature.
                        collectBecomesTargetTriggers(gameData, attached, playerId, targetPermanent, spellEntry);
                        collectBecomesTargetOfOpponentCounterTriggers(gameData, attached, playerId, spellEntry);
                        collectBecomesTargetOfSpellOrAbilityTriggers(gameData, attached, playerId, spellEntry);
                    }
                }
            }
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class) && !gameData.interaction.isAwaitingInput()) {
            processNextSpellTargetTrigger(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextETBTokenMultiTargetTrigger(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
        }
    }

    private StackEntry findStackEntryByCardId(GameData gameData, UUID cardId) {
        return gameData.stack.stream()
                .filter(entry -> entry.getCard() != null && entry.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    /** Drains pending multi-target trigger choices (ETB token copies and ON_SELF_CAST multi-target). */
    public void processNextETBTokenMultiTargetTrigger(GameData gameData) {
        etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
    }

    public boolean needsSlotBySlotTargetSelection(Card card) {
        return etbTokenTargetService.needsSlotBySlotTargetSelection(card);
    }

    /**
     * Checks becomes-target triggers for activated/triggered abilities that target permanents.
     * Must be called after an ability is pushed onto the stack with a target permanent.
     */
    public void checkBecomesTargetOfAbilityTriggers(GameData gameData) {
        if (gameData.stack.isEmpty()) return;
        StackEntry abilityEntry = gameData.stack.getLast();
        checkBecomesTargetOfAbilityTriggers(gameData, abilityEntry);
        checkTargetChoiceTriggers(gameData, abilityEntry);
    }

    /**
     * Checks becomes-target triggers for an explicitly supplied activated or triggered ability.
     * This overload is used when the ability is not the current top stack entry, such as while
     * Psychic Battle is resolving.
     */
    public void checkBecomesTargetOfAbilityTriggers(GameData gameData, StackEntry abilityEntry) {
        if (abilityEntry == null) return;
        if (abilityEntry.getTargetZone() == Zone.STACK && !abilityEntry.isNonTargeting()) {
            if (abilityEntry.getTargetId() != null) {
                StackEntry targetedEntry = findStackEntryByCardId(gameData, abilityEntry.getTargetId());
                collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, abilityEntry);
            } else {
                for (UUID targetId : abilityEntry.getTargetIds()) {
                    StackEntry targetedEntry = findStackEntryByCardId(gameData, targetId);
                    collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, abilityEntry);
                }
            }
        }

        List<UUID> targetIds = new ArrayList<>();
        if (abilityEntry.getTargetId() != null
                && abilityEntry.getTargetZone() == null
                && !abilityEntry.isNonTargeting()) {
            targetIds.add(abilityEntry.getTargetId());
        }
        if (abilityEntry.getTargetIds() != null) {
            targetIds.addAll(abilityEntry.getTargetIds());
        }

        Set<UUID> targetedPlayers = new HashSet<>();
        Set<UUID> targetControllersWithAllyTrigger = new HashSet<>();

        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) {
                if (targetControllersWithAllyTrigger.add(targetId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(gameData, targetId, abilityEntry);
                }
                if (targetedPlayers.add(targetId)) {
                    collectControllerBecomesTargetOfOpponentTriggers(gameData, targetId, abilityEntry);
                }
                continue;
            }

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());

            if (controllerId != null) {
                if (targetControllersWithAllyTrigger.add(controllerId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(gameData, controllerId, abilityEntry);
                }
                collectAllyPermanentBecomesTargetOfOpponentTriggers(gameData, controllerId, abilityEntry);
                collectAnotherAllyPermanentBecomesTargetOfOpponentTriggers(
                        gameData, targetPermanent, controllerId, abilityEntry);
            }

            // Check the targeted permanent itself for "when this becomes the target" triggers.
            // Attached permanents (auras/equipment) use the loop below instead.
            if (!targetPermanent.isAttached() && controllerId != null) {
                collectBecomesTargetOfSpellOrAbilityTriggers(gameData, targetPermanent, controllerId, abilityEntry);
                collectBecomesTargetOfOpponentCounterTriggers(gameData, targetPermanent, controllerId, abilityEntry);
                collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
                        gameData, targetPermanent, controllerId, abilityEntry.getControllerId());
            }

            // Check for "whenever a creature you control becomes the target of opponent's spell or ability"
            if (controllerId != null) {
                collectAllyCreatureBecomesTargetOfOpponentTriggers(gameData, targetPermanent, controllerId, abilityEntry);
                collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
                        gameData, targetPermanent, controllerId, abilityEntry);
                collectOpponentCreatureBecomesTargetOfYourSpellTriggers(gameData, targetPermanent, controllerId, abilityEntry);
                collectAllyCreatureBecomesTargetOfBackupAbilityTriggers(
                        gameData, targetPermanent, controllerId, abilityEntry);
            }

            collectAnyCreatureBecomesTargetTriggers(gameData, targetPermanent, abilityEntry);

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.isAttached()
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        // CR 603.3b: triggered ability controlled by the aura/equipment's controller
                        collectBecomesTargetOfSpellOrAbilityTriggers(gameData, attached, playerId, abilityEntry);
                        collectBecomesTargetOfOpponentCounterTriggers(gameData, attached, playerId, abilityEntry);
                    }
                }
            }
        }
    }

    /**
     * Checks all permanents controlled by the targeted player for effects that watch that player or
     * one of their permanents becoming the target of an opponent's spell or ability.
     */
    private void collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID targetControllerId, StackEntry triggeringEntry) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                    EffectSlot.ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            if (effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (!(effect instanceof CounterUnlessEffect counterEffect)) continue;

                StackEntry counterEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        targetControllerId,
                        source.getCard().getName() + "'s triggered ability",
                        new ArrayList<>(List.of(counterEffect)),
                        triggeringEntry.getCard().getId(),
                        Zone.STACK,
                        source.getId()
                );
                counterEntry.setSourcePermanentSnapshot(new Permanent(source));
                gameData.stack.add(counterEntry);

                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
                log.info("Game {} - {} ally-permanent-or-player counter trigger queued",
                        gameData.id, source.getCard().getName());
            }

            effects = effects.stream()
                    .filter(effect -> !(effect instanceof CounterUnlessEffect))
                    .toList();
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    targetControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    effects,
                    triggeringEntry.getControllerId(),
                    source.getId()
            );
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} ally-permanent-or-player-becomes-target-of-opponent trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    private void collectAnotherAllyPermanentBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID targetControllerId, StackEntry triggeringEntry) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            if (source.getId().equals(targetPermanent.getId())) continue;

            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                    EffectSlot.ON_ANOTHER_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_ANOTHER_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    targetControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    effects,
                    null,
                    source.getId()
            );
            entry.setNonTargeting(true);
            entry.setTriggeringPermanentId(targetPermanent.getId());
            entry.setTriggeringPermanentControllerId(targetControllerId);
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} another-ally-permanent-becomes-target-of-opponent trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    private void collectAllyPermanentBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID targetControllerId, StackEntry triggeringEntry) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                    EffectSlot.ON_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source,
                    EffectSlot.ON_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    targetControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    effects,
                    null,
                    source.getId()
            );
            entry.setSourcePermanentSnapshot(new Permanent(source));
            gameData.stack.add(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        }
    }

    public void checkTargetChoiceTriggers(GameData gameData, StackEntry targetEntry) {
        if (targetEntry == null || !targetEntry.hasAnyTarget() || targetEntry.isNonTargeting()) {
            return;
        }

        gameData.forEachPermanent((controllerId, permanent) -> {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_ANY_PLAYER_CHOOSES_TARGETS)) {
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        permanent.getCard(),
                        controllerId,
                        permanent.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        permanent.getId()
                );
                trigger.setTriggeringCardId(targetEntry.getCard().getId());
                gameData.enqueueTrigger(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                log.info("Game {} - {} triggers when targets are chosen", gameData.id,
                        permanent.getCard().getName());
            }
        });
    }

    private void collectBecomesTargetTriggers(GameData gameData, Permanent source, UUID controllerId,
                                              Permanent targetedCreature, StackEntry spellEntry) {
        if (source.isCloaked()) return;
        List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL);
        if (effects.isEmpty()) return;

        // Split targeting effects (which choose "any target" at resolution — Livewire Lash's damage)
        // from non-targeting ones (which resolve against the source permanent — an Illusion's
        // "sacrifice it"). The latter must go straight on the stack carrying sourcePermanentId; the
        // any-target interaction never records a source, so a self-referential effect would no-op.
        List<CardEffect> targetingEffects = new ArrayList<>();
        List<CardEffect> nonTargetingEffects = new ArrayList<>();
        List<CardEffect> triggeringSpellEffects = new ArrayList<>();
        for (CardEffect effect : effects) {
            if (effect instanceof TriggeringSpellReferencingEffect) {
                triggeringSpellEffects.add(effect);
            } else if (effect.targetSpec().declaredTarget() == null) {
                nonTargetingEffects.add(effect);
            } else {
                targetingEffects.add(effect);
            }
        }

        if (!nonTargetingEffects.isEmpty()) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(nonTargetingEffects),
                    null,
                    source.getId()
            );
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} becomes-target-of-spell trigger queued", gameData.id, source.getCard().getName());
        }

        if (!triggeringSpellEffects.isEmpty()) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(triggeringSpellEffects),
                    spellEntry.getCard().getId(),
                    Zone.STACK
            );
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} becomes-target-of-spell trigger queued against the targeting spell", gameData.id, source.getCard().getName());
        }

        if (!targetingEffects.isEmpty()) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    targetedCreature.getCard(), controllerId, new ArrayList<>(targetingEffects)
            ));

            gameLogService.append(gameData, GameLog.cardThen(targetedCreature.getCard(), "'s triggered ability triggers — choose a target for damage."));
            log.info("Game {} - {} becomes-target-of-spell trigger queued", gameData.id, targetedCreature.getCard().getName());
        }
    }

    private void collectBecomesTargetOfSpellOrAbilityTriggers(
            GameData gameData, Permanent source, UUID controllerId, StackEntry triggeringEntry) {
        if (source.isCloaked()) return;
        List<CardEffect> effects = new ArrayList<>(
                source.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY));
        // Dismiss into Dream continuously grants "When this creature becomes the target of a spell
        // or ability, sacrifice it" to each creature its controller's opponents control.
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                gameData, source, EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY));
        // Makeshift Mannequin: while a permanent has a mannequin counter, it has "When this creature
        // becomes the target of a spell or ability, sacrifice it."
        if (source.getCounterCount(CounterType.MANNEQUIN) > 0) {
            effects.add(new SacrificeSelfEffect());
        }
        if (effects.isEmpty()) return;

        boolean oncePerTurn = false;
        List<CardEffect> resolvedEffects = new ArrayList<>(effects.size());
        for (CardEffect effect : effects) {
            CardEffect resolved = effect;
            if (resolved instanceof TriggeringSpellControllerConditionalEffect conditional) {
                if (!controllerId.equals(triggeringEntry.getControllerId())) continue;
                resolved = conditional.wrapped();
            }
            if (resolved instanceof OncePerTurnTriggerEffect once) {
                if (gameData.oncePerTurnTriggersFiredThisTurn.contains(source.getId())) continue;
                resolved = once.wrapped();
                oncePerTurn = true;
            }
            resolvedEffects.add(resolved);
        }
        if (resolvedEffects.isEmpty()) return;

        // Glyph Keeper: "Whenever this creature becomes the target of a spell or ability for the first
        // time each turn, counter that spell or ability." A counterspelling effect in this slot fires at
        // most once per turn per permanent and counters the object that triggered it — set as the target
        // in the STACK zone so CounterSpellEffect finds the triggering entry sitting below this trigger.
        if (resolvedEffects.stream().anyMatch(CounterSpellingEffect.class::isInstance)) {
            if (source.isBecomeTargetCounterUsedThisTurn()) return;
            source.setBecomeTargetCounterUsedThisTurn(true);

            StackEntry counterEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(resolvedEffects),
                    0,
                    triggeringEntry.getCard().getId(),
                    source.getId(),
                    null,
                    Zone.STACK,
                    null,
                    null
            );
            gameData.stack.add(counterEntry);
            if (oncePerTurn) gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s triggered ability triggers — counter that spell or ability."));
            log.info("Game {} - {} becomes-target-of-spell-or-ability counter trigger queued",
                    gameData.id, source.getCard().getName());
            return;
        }

        if (resolvedEffects.stream().anyMatch(TriggeringSpellReferencingEffect.class::isInstance)) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(resolvedEffects),
                    0,
                    triggeringEntry.getCard().getId(),
                    source.getId(),
                    null,
                    Zone.STACK,
                    null,
                    null
            );
            gameData.stack.add(entry);
            if (oncePerTurn) gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} becomes-target-of-spell-or-ability trigger queued against the triggering object",
                    gameData.id, source.getCard().getName());
            return;
        }

        boolean targetsPermanent = resolvedEffects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        boolean targetsPlayer = resolvedEffects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        if (targetsPermanent || targetsPlayer) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    source.getCard(), controllerId, new ArrayList<>(resolvedEffects),
                    targetsPlayer && !targetsPermanent, source.getCard().getTargetFilter(),
                    0, source.getId()));
            if (oncePerTurn) gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s triggered ability triggers — choose a target."));
            log.info("Game {} - {} becomes-target-of-spell-or-ability trigger queued for target selection",
                    gameData.id, source.getCard().getName());
            return;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(resolvedEffects),
                null,
                source.getId()
        );
        gameData.stack.add(entry);
        if (oncePerTurn) gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
        log.info("Game {} - {} becomes-target-of-spell-or-ability trigger queued", gameData.id, source.getCard().getName());
    }

    /**
     * "Whenever this creature becomes the target of an Aura spell, &lt;effect&gt;." Narrower than
     * {@link EffectSlot#ON_BECOMES_TARGET_OF_SPELL}: only Aura spells count, and there is no controller
     * restriction, so an opponent's Aura triggers it just as the controller's own does. Used by
     * Fugitive Druid.
     */
    private void collectBecomesTargetOfAuraSpellTriggers(GameData gameData, Permanent source, UUID controllerId, StackEntry spellEntry) {
        if (source.isCloaked()) return;
        if (spellEntry.getCard() == null || !spellEntry.getCard().isAura()) return;

        List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_AURA_SPELL);
        if (effects.isEmpty()) return;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId()
        );
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
        log.info("Game {} - {} becomes-target-of-Aura-spell trigger queued", gameData.id, source.getCard().getName());
    }

    private void collectBecomesTargetOfOpponentCounterTriggers(GameData gameData, Permanent source, UUID controllerId, StackEntry triggeringEntry) {
        // Only trigger if the spell/ability is controlled by an opponent
        if (controllerId.equals(triggeringEntry.getControllerId())) return;

        if (source.isCloaked()) {
            enqueueCloakedWardTrigger(gameData, source, controllerId, triggeringEntry);
            return;
        }

        List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                gameData, source, EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL));
        if (effects.isEmpty()) return;

        for (CardEffect effect : effects) {
            CardEffect resolvedEffect = effect;
            if (effect instanceof ConditionalEffect conditional) {
                if (!conditionEvaluationService.isInterveningIfMet(gameData, conditional, source, controllerId)) {
                    continue;
                }
                resolvedEffect = conditional.wrapped();
            }
            if (resolvedEffect instanceof CounterUnlessEffect counterEffect) {
                // Put the counter-unless effect directly on the stack targeting the spell. The pay
                // and discard variants queue an identical trigger entry; only the log wording differs
                // by the kind of ransom demanded.
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        controllerId,
                        source.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(counterEffect)),
                        triggeringEntry.getCard().getId(),
                        Zone.STACK,
                        source.getId()
                );
                entry.setSourcePermanentSnapshot(new Permanent(source));
                gameData.stack.add(entry);

                switch (counterEffect.ransomKind()) {
                    case PAY_MANA -> {
                        String paymentText = counterEffect instanceof CounterUnlessPaysEffect pays
                                ? (pays.manaCost() != null ? pays.manaCost() : "{" + pays.amount() + "}")
                                        + (pays.lifeCost() > 0 ? " and " + pays.lifeCost() + " life" : "")
                                : "{" + counterEffect.ransomMagnitude() + "}";
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                                "'s triggered ability triggers — counter unless controller pays "
                                + paymentText + "."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter trigger queued", gameData.id, source.getCard().getName());
                    }
                    case PAY_WATERBEND -> {
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                                "'s triggered ability triggers — counter unless controller pays {"
                                        + counterEffect.ransomMagnitude() + "} using waterbend."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell waterbend counter trigger queued",
                                gameData.id, source.getCard().getName());
                    }
                    case DISCARD_CARD -> {
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers — counter unless controller discards a card."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter-unless-discard trigger queued", gameData.id, source.getCard().getName());
                    }
                    case PAY_LIFE -> {
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                                "'s triggered ability triggers — counter unless controller pays life equal to its power."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter-unless-life trigger queued", gameData.id, source.getCard().getName());
                    }
                    case SACRIFICE_PERMANENT -> {
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                                "'s triggered ability triggers - counter unless controller sacrifices a permanent."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter-unless-sacrifice trigger queued", gameData.id, source.getCard().getName());
                    }
                    case COLLECT_EVIDENCE -> {
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                                "'s triggered ability triggers - counter unless controller collects evidence."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter-unless-collect-evidence trigger queued",
                                gameData.id, source.getCard().getName());
                    }
                }
            }
        }
    }

    public void checkCollectEvidenceTriggers(GameData gameData, UUID collectingPlayerId) {
        var ctx = new TriggerContext.CollectEvidence(collectingPlayerId);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(collectingPlayerId);
        if (ownBattlefield == null) {
            return;
        }

        for (Permanent perm : List.copyOf(ownBattlefield)) {
            dispatchSlot(gameData, perm, collectingPlayerId,
                    EffectSlot.ON_CONTROLLER_COLLECTS_EVIDENCE, ctx);
        }
    }

    private void enqueueCloakedWardTrigger(GameData gameData, Permanent source, UUID controllerId,
                                           StackEntry triggeringEntry) {
        Card wardSourceCard = Card.namedRuntimePlaceholder("Cloaked creature");
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                wardSourceCard,
                controllerId,
                wardSourceCard.getName() + "'s ward ability",
                new ArrayList<>(List.of(new com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect(2))),
                triggeringEntry.getCard().getId(),
                Zone.STACK,
                source.getId()
        );
        entry.setSourcePermanentSnapshot(new Permanent(source));
        gameData.stack.add(entry);
    }

    private void collectControllerBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID playerId, StackEntry triggeringEntry) {
        if (playerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return;

        for (Permanent source : new ArrayList<>(battlefield)) {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) continue;

            for (CardEffect effect : source.getCard().getEffects(
                    EffectSlot.ON_CONTROLLER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY)) {
                if (!(effect instanceof CounterUnlessEffect)) continue;

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        playerId,
                        source.getCard().getName() + "'s triggered ability",
                        new ArrayList<>(List.of(effect)),
                        triggeringEntry.getCard().getId(),
                        Zone.STACK,
                        source.getId()
                );
                entry.setSourcePermanentSnapshot(new Permanent(source));
                gameData.stack.add(entry);

                gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                        "'s triggered ability triggers — counter unless controller pays {1}."));
                log.info("Game {} - {} controller-target counter trigger queued", gameData.id,
                        source.getCard().getName());
            }
        }
    }

    private void collectControllerBecomesTargetOfSpellTriggers(
            GameData gameData, UUID playerId, StackEntry triggeringEntry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return;

        for (Permanent source : new ArrayList<>(battlefield)) {
            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                    EffectSlot.ON_CONTROLLER_BECOMES_TARGET_OF_SPELL));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_CONTROLLER_BECOMES_TARGET_OF_SPELL));
            if (effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(source.getCard(), playerId, may, null, source.getId());
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source.getCard(),
                            playerId,
                            source.getCard().getName() + "'s triggered ability",
                            new ArrayList<>(List.of(effect)),
                            null,
                            source.getId()
                    ));
                }
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                        "'s triggered ability triggers."));
                log.info("Game {} - {} controller-becomes-target-of-spell trigger queued",
                        gameData.id, source.getCard().getName());
            }
        }
    }

    /**
     * "Whenever this creature becomes the target of a spell or ability an opponent controls, &lt;non-counter effect&gt;."
     * Handles the non-counter effects in {@link EffectSlot#ON_BECOMES_TARGET_OF_OPPONENT_SPELL} (e.g. "you may
     * draw a card"). Counter/Ward effects on that slot are handled separately by
     * {@link #collectBecomesTargetOfOpponentCounterTriggers}. Called from both the spell and
     * ability paths so the "or ability" clause is honored. Used by Tenured Concocter.
     */
    private void collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
            GameData gameData, Permanent source, UUID controllerId, UUID spellOrAbilityControllerId) {
        // Only trigger if the spell/ability is controlled by an opponent
        if (controllerId.equals(spellOrAbilityControllerId)) return;
        if (source.isCloaked()) return;

        List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL).stream()
                .filter(e -> !(e instanceof CounterUnlessEffect)
                        && !(e instanceof ConditionalEffect conditional
                        && conditional.wrapped() instanceof CounterUnlessEffect))
                .toList();
        if (effects.isEmpty()) return;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId()
        );
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
        log.info("Game {} - {} becomes-target-of-opponent-spell-or-ability (non-counter) trigger queued",
                gameData.id, source.getCard().getName());
    }

    private void collectBecomesTargetOfOpponentSpellOnlyTriggers(
            GameData gameData, Permanent source, UUID controllerId, UUID spellControllerId) {
        if (controllerId.equals(spellControllerId)) return;

        List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                gameData, source, EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY));
        if (effects.isEmpty()) return;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId()
        );
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
        log.info("Game {} - {} becomes-target-of-opponent-spell-only trigger queued",
                gameData.id, source.getCard().getName());
    }

    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@link EffectSlot#ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY}.
     * Only fires when the targeted permanent is a creature and the spell/ability
     * is controlled by an opponent of the creature's controller.
     */
    private void collectAllyCreatureBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        // Only trigger for creatures
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;
        // Only trigger if the spell/ability is controlled by an opponent
        if (creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                    EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            if (effects.isEmpty()) continue;

            List<CardEffect> nonCounterEffects = new ArrayList<>();
            for (CardEffect effect : effects) {
                CardEffect resolved = resolveTriggeringPermanentConditional(
                        gameData, source, creatureControllerId, targetPermanent, effect);
                if (resolved == null) continue;

                if (resolved instanceof CounterSpellingEffect) {
                    StackEntry counterTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source.getCard(),
                            creatureControllerId,
                            source.getCard().getName() + "'s triggered ability",
                            List.of(resolved),
                            triggeringEntry.getCard().getId(),
                            Zone.STACK);
                    gameData.stack.add(counterTrigger);
                    gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                            "'s triggered ability triggers."));
                } else {
                    nonCounterEffects.add(resolved);
                }
            }

            List<CardEffect> targetingEffects = nonCounterEffects.stream()
                    .filter(effect -> effect.targetSpec().declaredTarget() != null)
                    .toList();
            List<CardEffect> directEffects = nonCounterEffects.stream()
                    .filter(effect -> effect.targetSpec().declaredTarget() == null)
                    .toList();

            if (!directEffects.isEmpty()) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        creatureControllerId,
                        source.getCard().getName() + "'s triggered ability",
                        directEffects,
                        null,
                        source.getId()
                );
                gameData.stack.add(entry);

                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            }
            if (!targetingEffects.isEmpty()) {
                PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentNotPredicate(new PermanentIsSpecificPermanentPredicate(targetPermanent.getId()))));
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        source.getCard(), creatureControllerId, targetingEffects, false,
                        new PermanentPredicateTargetFilter(
                                targetPredicate, "Target must be another creature you control"),
                        0, source.getId(), new Permanent(source), false,
                        targetPermanent.getId(), creatureControllerId, creatureControllerId));
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                        "'s triggered ability triggers."));
            }
            log.info("Game {} - {} ally-creature-becomes-target-of-opponent trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@link EffectSlot#ON_ALLY_CREATURE_BECOMES_TARGET_OF_INSTANT_OR_SORCERY}. Only fires when the
     * targeted permanent is a creature and the targeting spell is an instant or a sorcery; there is
     * no controller restriction, so the creature's controller's own spells trigger it too. The
     * targeted creature is stored as the non-targeting {@code targetId} so the resolved effect can
     * act on it. {@link TriggeringPermanentConditionalEffect} wrappers are filtered against the
     * targeted creature while the trigger event is collected. Used by Wild Defiance and Silverfur
     * Partisan.
     */
    private void collectAllyCreatureBecomesTargetOfInstantOrSorceryTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry spellEntry) {
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;
        if (!spellEntry.getCard().hasType(CardType.INSTANT) && !spellEntry.getCard().hasType(CardType.SORCERY)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            List<CardEffect> effects = source.getCard().getEffects(
                    EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_INSTANT_OR_SORCERY);
            if (effects.isEmpty()) continue;

            List<CardEffect> resolvedEffects = new ArrayList<>();
            for (CardEffect effect : effects) {
                CardEffect resolved = resolveTriggeringPermanentConditional(
                        gameData, source, creatureControllerId, targetPermanent, effect);
                if (resolved != null) {
                    resolvedEffects.add(resolved);
                }
            }
            if (resolvedEffects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    creatureControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    resolvedEffects,
                    targetPermanent.getId(),
                    source.getId()
            );
            entry.setNonTargeting(true);
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} ally-creature-becomes-target-of-instant-or-sorcery trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    public void checkCreatureTapForManaTriggers(GameData gameData, UUID tappingPlayerId,
                                                UUID tappedCreatureId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(tappingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CreatureTapForMana(tappingPlayerId, tappedCreatureId);
        for (Permanent perm : List.copyOf(battlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(
                    EffectSlot.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA)) {
                var match = new TriggerMatchContext(gameData, perm, tappingPlayerId, effect);
                registry.dispatch(match, EffectSlot.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA, effect, ctx);
            }
        }
    }

    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@link EffectSlot#ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL}. Only the spell path calls this
     * method, so activated abilities do not trigger it. The card's target groups are reused to
     * choose any optional targets as the triggered ability is put on the stack.
     */
    private void collectAllyCreatureBecomesTargetOfSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId) {
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                    EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL));
            if (effects.isEmpty()) continue;

            if (source.getCard().getSpellTargets().size() > 1
                    || etbTokenTargetService.needsSlotBySlotTargetSelection(source.getCard())) {
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        source.getCard(), creatureControllerId, effects, source.getId(), List.of(), 0, 0));
            } else {
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                        source.getCard(), creatureControllerId, effects, source.getId(),
                        source.getCard().getTargetFilter()));
            }

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s triggered ability triggers — choose targets."));
            log.info("Game {} - {} ally-creature-becomes-target-of-spell trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * Checks ALL permanents across every battlefield for
     * {@link EffectSlot#ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY}. Only fires when the
     * targeted permanent is a creature. The targeted creature is stored as the non-targeting
     * {@code targetId} so the resolved effect can act on it. Used by Cowardice.
     */
    private void collectAnyCreatureBecomesTargetTriggers(
            GameData gameData, Permanent targetPermanent, StackEntry triggeringEntry) {
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;

        UUID triggeringSourceControllerId = triggeringEntry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentController(gameData, triggeringEntry.getSourcePermanentId());

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent source : battlefield) {
                List<CardEffect> effects = source.getCard().getEffects(
                        EffectSlot.ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY);
                if (effects.isEmpty()) continue;

                List<CardEffect> resolvedEffects = effects.stream()
                        .map(effect -> effect.resolveForBecomesTargetOfSpellOrAbility(
                                triggeringEntry,
                                source.getId(),
                                targetPermanent.getId(),
                                playerId,
                                triggeringSourceControllerId))
                        .filter(Objects::nonNull)
                        .toList();
                if (resolvedEffects.isEmpty()) continue;

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        playerId,
                        source.getCard().getName() + "'s triggered ability",
                        new ArrayList<>(resolvedEffects),
                        targetPermanent.getId(),
                        source.getId()
                );
                entry.setNonTargeting(true);
                gameData.stack.add(entry);

                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
                log.info("Game {} - {} any-creature-becomes-target trigger queued",
                        gameData.id, source.getCard().getName());
            }
        }
    }

    /**
     * Checks the spell/ability controller's battlefield for
     * {@link EffectSlot#ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY}. Only fires
     * when the targeted permanent is a creature controlled by an opponent of the spell/ability's
     * controller. The targeted creature is stored as the non-targeting {@code targetId} and the
     * listening permanent as the {@code sourcePermanentId}. Used by Willbreaker.
     */
    private void collectOpponentCreatureBecomesTargetOfYourSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;

        UUID triggeringControllerId = triggeringEntry.getControllerId();
        if (triggeringControllerId == null || triggeringControllerId.equals(creatureControllerId)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(triggeringControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            List<CardEffect> effects = source.getCard().getEffects(
                    EffectSlot.ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY);
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    triggeringControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(effects),
                    targetPermanent.getId(),
                    source.getId()
            );
            entry.setNonTargeting(true);
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} opponent-creature-becomes-target-of-your-spell trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    public void checkDealtDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature, int damageDealt, UUID damageSourceControllerId) {
        if (damageDealt > 0) {
            checkEnchantedCreatureDealtDamageTriggers(gameData, damagedCreature, damageDealt);
        }

        List<CardEffect> effects = new ArrayList<>(damagedCreature.getCard().getEffects(EffectSlot.ON_DEALT_DAMAGE));
        effects.addAll(damagedCreature.getTemporaryTriggeredEffects(EffectSlot.ON_DEALT_DAMAGE));
        effects.addAll(damagedCreature.getPersistentTriggeredEffects(EffectSlot.ON_DEALT_DAMAGE));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                gameData, damagedCreature, EffectSlot.ON_DEALT_DAMAGE));
        if (effects.isEmpty()) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        if (controllerId == null) return;

        var ctx = new TriggerContext.DamageToCreature(damagedCreature, damageDealt, damageSourceControllerId);

        for (CardEffect effect : effects) {
            var match = new TriggerMatchContext(gameData, damagedCreature, controllerId, effect);
            dispatch(match, EffectSlot.ON_DEALT_DAMAGE, effect, ctx);
        }
    }

    // ── Enchanted-creature-dealt-damage triggers ───────────────────────

    public void checkEnchantedCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature, int damageDealt) {
        if (damageDealt <= 0) return;

        var ctx = new TriggerContext.DamageToCreature(damagedCreature, damageDealt, null);

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(damagedCreature.getId())) {
                return;
            }
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE)) {
                var match = new TriggerMatchContext(gameData, perm, auraOwnerId, effect);
                dispatch(match, EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE, effect, ctx);
            }
        });
    }

    // ── Opponent-creature-dealt-damage triggers ──────────────────────────

    /**
     * Fires ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers on permanents whose controller
     * is different from the damaged creature's controller (i.e. the damaged creature is
     * an opponent's creature from the perspective of the permanent's controller).
     * Called once per damaged creature — each call produces one trigger per listening permanent.
     */
    public void checkOpponentCreatureDealtDamageTriggers(GameData gameData, UUID damagedCreatureControllerId) {
        gameData.forEachPermanent((playerId, perm) -> {
            // Only fire when the damaged creature was controlled by an opponent of this permanent's controller
            if (playerId.equals(damagedCreatureControllerId)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_DEALT_DAMAGE);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers (opponent creature dealt damage)", gameData.id, perm.getCard().getName());
            }
        });
    }

    /**
     * Fires excess-damage triggers on permanents whose controller is different from the damaged
     * permanent's controller.
     */
    public void checkOpponentPermanentDealtExcessDamageTriggers(GameData gameData,
                                                                Permanent damagedPermanent,
                                                                UUID damagedPermanentControllerId,
                                                                int excessDamage) {
        if (damagedPermanent == null || damagedPermanentControllerId == null || excessDamage <= 0) return;

        TriggerContext context = new TriggerContext.OpponentPermanentDealtExcessDamage(
                damagedPermanent, damagedPermanentControllerId, excessDamage);
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(damagedPermanentControllerId)) return;

            List<CardEffect> effects = new ArrayList<>(perm.getCard().getEffects(
                    EffectSlot.ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE));
            effects.addAll(perm.getTemporaryTriggeredEffects(
                    EffectSlot.ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE));
            effects.addAll(perm.getPersistentTriggeredEffects(
                    EffectSlot.ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE));
            for (CardEffect effect : effects) {
                if (effect instanceof DealDamageToAnyTargetEffect) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            perm.getCard(), playerId, new ArrayList<>(List.of(effect)), false,
                            new AnyTargetPredicateTargetFilter(
                                    new PermanentNotPredicate(
                                            new PermanentIsSpecificPermanentPredicate(damagedPermanent.getId())),
                                    new PlayerRelationPredicate(PlayerRelation.ANY),
                                    "Target must be other than the permanent dealt excess damage."),
                            excessDamage, perm.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers (opponent permanent dealt excess damage)",
                            gameData.id, perm.getCard().getName());
                    continue;
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        damagedPermanent.getId(),
                        perm.getId());
                entry.setNonTargeting(true);
                entry.setEventValue(excessDamage);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers (opponent permanent dealt excess damage)",
                        gameData.id, perm.getCard().getName());
            }
        });
    }

    public void checkOpponentCreatureDealtExcessNoncombatDamageTriggers(GameData gameData,
                                                                          StackEntry damageEntry,
                                                                          Permanent damagedCreature,
                                                                          UUID damagedCreatureControllerId,
                                                                          int excessDamage) {
        if (damageEntry == null || damagedCreature == null || damagedCreatureControllerId == null
                || excessDamage <= 0 || !gameQueryService.isCreature(gameData, damagedCreature)) return;

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(damagedCreatureControllerId)) return;

            List<CardEffect> effects = new ArrayList<>(perm.getCard().getEffects(
                    EffectSlot.ON_OPPONENT_CREATURE_DEALT_EXCESS_NONCOMBAT_DAMAGE));
            effects.addAll(perm.getTemporaryTriggeredEffects(
                    EffectSlot.ON_OPPONENT_CREATURE_DEALT_EXCESS_NONCOMBAT_DAMAGE));
            effects.addAll(perm.getPersistentTriggeredEffects(
                    EffectSlot.ON_OPPONENT_CREATURE_DEALT_EXCESS_NONCOMBAT_DAMAGE));
            for (CardEffect effect : effects) {
                if (!damageEntry.markNoncombatExcessDamageTriggerFired(perm.getId(), effect)) continue;
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        damagedCreature.getId(),
                        perm.getId());
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers (opponent creature dealt excess noncombat damage)",
                        gameData.id, perm.getCard().getName());
            }
        });
    }

    // ── Ally-creature-deals-damage-to-creature reflection (Greatbow Doyen) ──

    /**
     * Fires ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE triggers. When a creature the watcher's
     * controller controls (matching the effect's source filter) deals damage to a creature, the
     * damage-source creature deals that much damage to the damaged creature's controller. Called
     * once per source/target/damage event; {@code combatDamage} tells the combat-only listeners
     * (Sosuke, Son of Seshiro) whether this event was combat damage.
     */
    public void checkAllyDealtDamageToCreatureTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId,
            Permanent damagedCreature, int damage, boolean combatDamage) {
        if (damageSource == null || damageSourceControllerId == null || damagedCreatureControllerId == null || damage <= 0) {
            return;
        }

        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            // "a creature you control" — the damage source must be controlled by the watcher's controller.
            if (!watcherControllerId.equals(damageSourceControllerId)) return;
            // The damage source watches itself below, whether or not it survived the damage.
            if (watcher.getId().equals(damageSource.getId())) return;

            fireAllyDealtDamageToCreatureTrigger(gameData, watcher, damageSource, damageSourceControllerId,
                    damagedCreatureControllerId, damagedCreatureId, damagedCreature, damage, combatDamage);
        });

        // A self-scoped trigger ("Whenever this creature deals damage to a creature, …") triggered
        // when the damage was dealt, so it still goes on the stack when lethal damage back from the
        // blocker has already moved the source off the battlefield.
        fireAllyDealtDamageToCreatureTrigger(gameData, damageSource, damageSource, damageSourceControllerId,
                damagedCreatureControllerId, damagedCreatureId, damagedCreature, damage, combatDamage);
    }

    public void checkAllyDealtDamageToPlaneswalkerTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedPlaneswalkerId, int damage, boolean combatDamage,
            List<StackEntry> deferredTriggers) {
        if (damageSource == null || damageSourceControllerId == null || damagedPlaneswalkerId == null || damage <= 0) {
            return;
        }
        Permanent planeswalker = gameQueryService.findPermanentById(gameData, damagedPlaneswalkerId);
        if (planeswalker == null || !planeswalker.getCard().hasType(CardType.PLANESWALKER)
                || !gameQueryService.isCreature(gameData, damageSource)) return;

        TriggerContext context = new TriggerContext.CreatureDealsDamageToPlaneswalker(
                damageSource, damagedPlaneswalkerId, damage, combatDamage, deferredTriggers);
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcherControllerId.equals(damageSourceControllerId)) return;
            List<CardEffect> effects = new ArrayList<>(
                    watcher.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER));
            effects.addAll(watcher.getTemporaryTriggeredEffects(
                    EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER));
            effects.addAll(watcher.getPersistentTriggeredEffects(
                    EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER));
            for (CardEffect effect : effects) {
                TriggerMatchContext match = new TriggerMatchContext(gameData, watcher, watcherControllerId, effect);
                registry.dispatch(match, EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER, effect, context);
            }
        });
    }

    public void queueSourceDealsCombatDamageToPlayerOrPlaneswalkerTriggers(GameData gameData, Card sourceCard,
                                                                            UUID sourceControllerId,
                                                                            UUID sourcePermanentId,
                                                                            int totalDamage,
                                                                            List<CardEffect> snapshottedEffects) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) return;
        dispatchSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, snapshottedEffects,
                EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER);
    }

    public void queueSourceDealsCombatDamageToPlayerOrBattleTriggers(GameData gameData, Card sourceCard,
                                                                       UUID sourceControllerId,
                                                                       UUID sourcePermanentId,
                                                                       int totalDamage,
                                                                       List<CardEffect> snapshottedEffects) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) return;
        dispatchSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, snapshottedEffects,
                EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE);
        queueEquippedCreatureDealsCombatDamageToPlayerOrBattleTriggers(
                gameData, sourceCard, sourceControllerId, sourcePermanentId, totalDamage);
    }

    private void dispatchSourceDealsCombatDamageTriggers(GameData gameData, Card sourceCard,
                                                         UUID sourceControllerId, UUID sourcePermanentId,
                                                         int totalDamage, List<CardEffect> snapshottedEffects,
                                                         EffectSlot slot) {
        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage);
        List<CardEffect> effects = new ArrayList<>(sourceCard.getEffects(slot));
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (snapshottedEffects != null) {
            effects.addAll(snapshottedEffects);
        } else if (sourcePermanent != null) {
            effects.addAll(sourcePermanent.getTemporaryTriggeredEffects(slot));
            effects.addAll(sourcePermanent.getPersistentTriggeredEffects(slot));
        }
        for (CardEffect effect : effects) {
            var match = new TriggerMatchContext(gameData, sourcePermanent, sourceControllerId, effect);
            registry.dispatch(match, slot, effect, ctx);
        }
    }

    private void fireAllyDealtDamageToCreatureTrigger(GameData gameData, Permanent watcher, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId,
            Permanent damagedCreature, int damage, boolean combatDamage) {
        List<CardEffect> effects = new ArrayList<>(
                watcher.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE));
        // Abilities granted until end of turn (Cruel Deceiver) live on the permanent, not the card.
        effects.addAll(watcher.getTemporaryTriggeredEffects(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE));
        if (watcher.getId().equals(damageSource.getId())) {
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, watcher, damageSourceControllerId,
                    EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE));
        }

        TriggerContext context = new TriggerContext.CreatureDealsDamageToCreature(
                damageSource, damagedCreatureId, damage, combatDamage);

        for (CardEffect effect : effects) {
            TriggerMatchContext match = new TriggerMatchContext(gameData, watcher, damageSourceControllerId, effect);
            if (registry.dispatch(match, EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE, effect, context)) {
                continue;
            }
            if (effect instanceof ReflectAllyDamageToDamagedCreatureControllerEffect reflect) {
                if (reflect.combatOnly() && !combatDamage) continue;
                if (reflect.sourceMustBeWatcher() && !watcher.getId().equals(damageSource.getId())) continue;
                if (!reflect.sourceMustBeWatcher() && reflect.sourceFilter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(gameData, damageSource, reflect.sourceFilter())) {
                    continue;
                }

                // The normal form deals damage; Flayed Nim's self-scoped form causes life loss.
                CardEffect triggeredEffect = reflect.lifeLoss()
                        ? new LoseLifeEffect(damage, LoseLifeRecipient.TARGET_PLAYER)
                        : new DealDamageToPlayersEffect(damage, DamageRecipient.TARGET_PLAYER);
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        damageSource.getCard(),
                        damageSourceControllerId,
                        damageSource.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(triggeredEffect)),
                        damagedCreatureControllerId,
                        damageSource.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                if (reflect.lifeLoss()) {
                    log.info("Game {} - {} causes {} life loss to {}", gameData.id,
                            watcher.getCard().getName(), damage,
                            gameData.playerIdToName.get(damagedCreatureControllerId));
                } else {
                    log.info("Game {} - {} reflects {} damage to {}", gameData.id,
                            watcher.getCard().getName(), damage,
                            gameData.playerIdToName.get(damagedCreatureControllerId));
                }
            } else if (effect instanceof DamageDamagedCreatureControllerAndSelfEffect punisher) {
                // "this creature" — fire only when the watcher itself dealt the damage.
                if (!watcher.getId().equals(damageSource.getId())) continue;

                // This creature deals N damage to that creature's controller and M damage to you.
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        damageSource.getCard(),
                        damageSourceControllerId,
                        damageSource.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(
                                new DealDamageToPlayersEffect(punisher.amountToDamagedCreatureController(), DamageRecipient.TARGET_PLAYER),
                                new DealDamageToPlayersEffect(punisher.amountToSelf(), DamageRecipient.CONTROLLER))),
                        damagedCreatureControllerId,
                        damageSource.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} deals {} to {} and {} to its controller", gameData.id,
                        watcher.getCard().getName(), punisher.amountToDamagedCreatureController(),
                        gameData.playerIdToName.get(damagedCreatureControllerId), punisher.amountToSelf());
            } else if (effect instanceof DamagedCreatureTriggerEffect damagedCreatureTrigger) {
                if (damagedCreatureTrigger.combatDamageOnly() && !combatDamage) continue;
                Permanent triggerSource = damageSource;
                if (damagedCreatureTrigger.equipmentScoped()) {
                    if (!isEquippedBy(gameData, watcher, damageSource)) continue;
                    triggerSource = watcher;
                } else if (!watcher.getId().equals(damageSource.getId())) {
                    // "this creature" — fire only when the watcher itself dealt the damage.
                    continue;
                }
                if (damagedCreatureId == null) continue;
                var damagedCreatureFilter = damagedCreatureTrigger.damagedCreatureFilter();
                if (damagedCreatureFilter != null && (damagedCreature == null
                        || !predicateEvaluationService.matchesPermanentPredicate(
                        gameData, damagedCreature, damagedCreatureFilter))) {
                    continue;
                }

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        triggerSource.getCard(),
                        damageSourceControllerId,
                        triggerSource.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(damagedCreatureTrigger.triggeredEffect())),
                        damagedCreatureId,
                        triggerSource.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} will resolve its damaged-creature trigger", gameData.id,
                        watcher.getCard().getName());
            } else if (effect instanceof TapAndSkipUntapDamagedCreatureEffect) {
                // "this creature" — fire only when the watcher itself dealt the damage.
                if (!watcher.getId().equals(damageSource.getId())) continue;
                if (damagedCreatureId == null) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        damageSource.getCard(),
                        damageSourceControllerId,
                        damageSource.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                new SkipNextUntapEffect(TapUntapScope.TARGET))),
                        damagedCreatureId,
                        damageSource.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} taps and locks the creature it damaged", gameData.id,
                        watcher.getCard().getName());
            } else if (effect instanceof EquipmentTapsAndLocksDamagedCreatureEffect) {
                // "equipped creature" — the watcher is the Equipment, so it must be attached to
                // the creature that dealt the damage. Last-known attachment is valid when that
                // creature died to the same damage event.
                if (!isEquippedBy(gameData, watcher, damageSource)) continue;
                if (damagedCreatureId == null) continue;

                UUID watcherControllerId = gameData.findControllerOf(watcher);
                if (watcherControllerId == null) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(),
                        watcherControllerId,
                        watcher.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                DoesntUntapEffect.targetWhileSourceOnBattlefield())),
                        damagedCreatureId,
                        watcher.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} taps and locks the creature damaged by its equipped creature",
                        gameData.id, watcher.getCard().getName());
            } else if (effect instanceof DestroyDamagedCreatureAtEndOfCombatEffect delayedDestroy) {
                if (delayedDestroy.combatDamageOnly() && !combatDamage) continue;
                if (delayedDestroy.selfOnly() && !watcher.getId().equals(damageSource.getId())) continue;
                if (damagedCreatureId == null) continue;
                if (delayedDestroy.sourceFilter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        damageSource,
                        delayedDestroy.sourceFilter(),
                        FilterContext.of(gameData).withSourcePermanentSnapshot(watcher))) {
                    continue;
                }

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(),
                        damageSourceControllerId,
                        watcher.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(new DestroyTargetPermanentAtEndOfCombatEffect())),
                        damagedCreatureId,
                        watcher.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} schedules the damaged creature for destruction at end of combat",
                        gameData.id, watcher.getCard().getName());
            } else if (effect instanceof EquipmentDamagesOtherDefendingCreaturesEffect) {
                // "equipped creature" — the watcher is the Equipment, so it must be attached to
                // the creature that dealt the damage. The ability triggered when the damage was
                // dealt, so a host that has already died to the same combat damage (which detaches
                // the Equipment) still counts, read from last-known attachment.
                if (!isEquippedBy(gameData, watcher, damageSource)) continue;
                if (damagedCreatureId == null) continue;
                if (!wasBlocking(gameData, damageSource, damagedCreatureId, combatDamage)) continue;

                // The Equipment deals that much damage to each other creature defending player controls.
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(),
                        damageSourceControllerId,
                        watcher.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(new DealDamageToEachMatchingPermanentEffect(damage,
                                new PermanentNotPredicate(new PermanentIsSpecificPermanentPredicate(damagedCreatureId)),
                                EachPermanentScope.TARGET_PLAYER))),
                        damagedCreatureControllerId,
                        watcher.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} deals {} damage to each other creature {} controls", gameData.id,
                        watcher.getCard().getName(), damage,
                        gameData.playerIdToName.get(damagedCreatureControllerId));
            }
        }
    }

    /**
     * Whether the creature that was just dealt damage was a blocking creature. Lethal damage may
     * already have moved it off the battlefield, in which case the fallback reads the damage source:
     * a creature only deals combat damage to creatures blocking it while it is attacking.
     */
    private boolean isEquippedBy(GameData gameData, Permanent equipment, Permanent host) {
        return isEquippedBy(gameData, equipment, host.getId());
    }

    private boolean isEquippedBy(GameData gameData, Permanent equipment, UUID hostId) {
        if (hostId.equals(equipment.getAttachedTo())) return true;
        // Only a host that already left the battlefield may be matched on last-known attachment.
        return hostId.equals(equipment.getLastAttachedTo())
                && gameQueryService.findPermanentById(gameData, hostId) == null;
    }

    /**
     * Whether the creature that was just dealt damage was a blocking creature. For combat damage the
     * damage source answers it: a creature deals combat damage to creatures blocking it only while it
     * is attacking (a blocking creature damages the attacker instead). The damaged creature's own
     * blocking flag is unusable there — lethal damage may already have removed it from the
     * battlefield, and combat state is torn down when the creature it blocked dies.
     */
    private boolean wasBlocking(GameData gameData, Permanent damageSource, UUID damagedCreatureId, boolean combatDamage) {
        if (combatDamage) return damageSource.isAttacking();
        Permanent damaged = gameQueryService.findPermanentById(gameData, damagedCreatureId);
        return damaged != null && damaged.isBlocking();
    }

    // ── Any-creature-dealt-damage triggers ─────────────────────────────

    /**
     * Fires ON_ANY_CREATURE_DEALT_DAMAGE triggers on every permanent with that slot, regardless of
     * who controls the damaged creature. Called once per damaged creature.
     */
    public void checkAnyCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature,
                                                    int damageDealt) {
        UUID damagedCreatureControllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        checkAnyCreatureDealtDamageTriggers(gameData, damagedCreature, damagedCreatureControllerId, damageDealt);
    }

    /**
     * Variant for combat, where the damaged creature may already have left the battlefield by the
     * time its trigger is queued and its controller was captured during damage processing.
     */
    public void checkAnyCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature,
                                                    UUID damagedCreatureControllerId, int damageDealt) {
        if (damagedCreatureControllerId == null || damageDealt <= 0) return;

        var ctx = new TriggerContext.AnyCreatureDealtDamage(
                damagedCreature, damagedCreatureControllerId, damageDealt);
        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                registry.dispatch(match, EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE, effect, ctx);
            }
        });

        collectTemporaryGlobalTriggers(gameData, EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE,
                damagedCreature.getId(), damageDealt);

        for (DelayedWatchedCreatureDealtDamage watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealtDamage.class)) {
            if (!watch.watchedPermanentId().equals(damagedCreature.getId())) {
                continue;
            }

            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    (UUID) null,
                    watch.watchedPermanentId());
            trigger.setNonTargeting(true);
            trigger.setEventValue(damageDealt);
            trigger.setDamageSourceCard(damagedCreature.getCard());
            trigger.setSourcePermanentSnapshot(new Permanent(damagedCreature));
            gameData.enqueueTrigger(trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
            log.info("Game {} - {} delayed trigger fires after damage to watched creature",
                    gameData.id, watch.sourceCard().getName());
        }
    }

    public void checkTemporaryGlobalCreatureAttackTriggers(GameData gameData, Permanent attacker) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (watcher.slot() != EffectSlot.ON_ANY_CREATURE_ATTACKS) continue;
            CardEffect effect = watcher.effect();
            boolean matches = true;
            while (matches) {
                if (effect instanceof TriggeringPermanentControllerConditionalEffect conditional) {
                    if (!watcher.controllerId().equals(gameQueryService.findPermanentController(gameData, attacker.getId()))) {
                        matches = false;
                    } else {
                        effect = conditional.wrapped();
                    }
                } else if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    if (!predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, conditional.predicate())) {
                        matches = false;
                    } else {
                        effect = conditional.wrapped();
                    }
                } else {
                    break;
                }
            }
            if (!matches) continue;
            CardEffect triggeredEffect = effect;
            if (triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                        watcher.sourceCard(), watcher.controllerId(),
                        new ArrayList<>(List.of(triggeredEffect)), null));
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
                continue;
            }
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, watcher.sourceCard(),
                    watcher.controllerId(), watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(triggeredEffect)));
            entry.setTargetId(attacker.getId());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    public void checkBendingTriggers(GameData gameData, UUID bendingPlayerId, BendingType type) {
        if (bendingPlayerId == null || type == null) {
            return;
        }
        gameData.recordBending(bendingPlayerId, type);
        TriggerContext context = new TriggerContext.Bending(bendingPlayerId, type);
        List<Permanent> battlefield = gameData.playerBattlefields.get(bendingPlayerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchSlot(gameData, permanent, bendingPlayerId, EffectSlot.ON_CONTROLLER_BENDS, context);
        }
    }

    /**
     * Fires effects that care about an attacking creature causing one of its own triggered
     * abilities to trigger. The ability is supplied as a snapshot so later stack changes cannot
     * make a copy refer to a different trigger from the same source card.
     */
    public void checkAttackingCreatureTriggeredAbilityTriggers(GameData gameData, Permanent attacker,
                                                                StackEntry triggeredAbility) {
        if (attacker == null || triggeredAbility == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
        if (controllerId == null) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.AttackingCreatureTriggeredAbility(
                attacker, triggeredAbility);
        for (Permanent watcher : List.copyOf(battlefield)) {
            dispatchSlot(gameData, watcher, controllerId, EffectSlot.STATIC, context);
        }
    }

    /** Collects turn-scoped global triggers for an event that identifies a permanent. */
    public void collectTemporaryGlobalTriggers(GameData gameData, EffectSlot slot, UUID targetId,
                                               int eventValue) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (watcher.slot() != slot) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setTargetId(targetId);
            entry.setEventValue(eventValue);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global {} trigger fires",
                    gameData.id, watcher.sourceCard().getName(), slot.name());
        }
    }

    // ── Enchanted-permanent-tap triggers ───────────────────────────────

    public void beginPermanentTapTriggerBatch(GameData gameData) {
        if (gameData.permanentTapTriggerBatchDepth++ == 0) {
            gameData.permanentTapTriggerBatchFiredEffects.clear();
        }
    }

    public void endPermanentTapTriggerBatch(GameData gameData) {
        if (gameData.permanentTapTriggerBatchDepth <= 0) {
            throw new IllegalStateException("No permanent tap trigger batch is active");
        }
        if (--gameData.permanentTapTriggerBatchDepth == 0) {
            gameData.permanentTapTriggerBatchFiredEffects.clear();
        }
    }

    public void checkEnchantedPermanentTapTriggers(GameData gameData, Permanent tappedPermanent) {
        UUID tappedPermanentControllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(tappedPermanent)) {
                tappedPermanentControllerId = pid;
                break;
            }
        }
        if (tappedPermanentControllerId == null) return;

        var ctx = new TriggerContext.EnchantedPermanentTap(tappedPermanent, tappedPermanentControllerId);

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(tappedPermanent.getId())) {
                return;
            }
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED)) {
                var match = new TriggerMatchContext(gameData, perm, auraOwnerId, effect);
                dispatch(match, EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);
            }
        });

        // "Whenever a permanent you control becomes tapped" triggers (e.g. Judge of Currents).
        UUID controllerId = tappedPermanentControllerId;
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(controllerId)) return;
            List<EffectRegistration> registrations = new ArrayList<>(
                    perm.getCard().getEffectRegistrations(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED));
            perm.getTemporaryTriggeredEffects(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED).stream()
                    .map(EffectRegistration::new).forEach(registrations::add);
            perm.getPersistentTriggeredEffects(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED).stream()
                    .map(EffectRegistration::new).forEach(registrations::add);
            grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                            gameData, perm, EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED).stream()
                    .map(EffectRegistration::new).forEach(registrations::add);
            for (EffectRegistration registration : registrations) {
                CardEffect effect = registration.effect();
                if (shouldSkipBatchedTapTrigger(gameData, perm, registration)) continue;
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(tappedPermanent, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                if (resolved.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved))));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on ally permanent tap (awaiting graveyard target)",
                            gameData.id, perm.getCard().getName());
                    markBatchedTapTrigger(gameData, perm, registration);
                    continue;
                }
                // Bake the tapped permanent's controller only when damage needs
                // TRIGGERING_PERMANENT_CONTROLLER (Royal Decree). triggeringPermanentId always
                // carries "it" (Freyalise's Winds).
                boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                resolved = unwrapOncePerTurnTrigger(gameData, perm, resolved);
                if (resolved == null) continue;
                if (resolved instanceof ConditionalEffect conditional && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(perm, ownerId))) {
                    continue;
                }
                // Leave targetId null for may-target tap triggers (Surgespanner). Bake the tapped
                // permanent's controller only when damage needs TRIGGERING_PERMANENT_CONTROLLER
                // (Royal Decree). triggeringPermanentId always carries "it" (Freyalise's Winds).
                if ((resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || resolved.targetSpec().admits(TargetPredicate.Kind.PLAYER))
                        && !(resolved instanceof MayPayManaEffect)) {
                    gameData.queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)), perm.getId(),
                            tappedPermanent.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on ally permanent tap ({}, awaiting target)",
                            gameData.id, perm.getCard().getName(), tappedPermanent.getCard().getName());
                    if (oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                    markBatchedTapTrigger(gameData, perm, registration);
                    continue;
                }
                UUID bakedTargetId = bakeTriggeringPermanentControllerTarget(resolved, controllerId);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        bakedTargetId,
                        perm.getId()
                );
                entry.setTriggeringPermanentId(tappedPermanent.getId());
                gameData.enqueueTrigger(entry);
                if (oncePerTurn) {
                    gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                }
                markBatchedTapTrigger(gameData, perm, registration);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ally permanent tap ({})",
                        gameData.id, perm.getCard().getName(), tappedPermanent.getCard().getName());
            }
        });

        // "Whenever a permanent an opponent controls becomes tapped" triggers (e.g. Thoughtleech).
        gameData.forEachPermanent((ownerId, perm) -> {
            if (ownerId.equals(controllerId)) return;
            for (EffectRegistration registration : perm.getCard().getEffectRegistrations(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED)) {
                CardEffect effect = registration.effect();
                if (shouldSkipBatchedTapTrigger(gameData, perm, registration)) continue;
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(tappedPermanent, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                resolved = unwrapOncePerTurnTrigger(gameData, perm, resolved);
                if (resolved == null) continue;
                  if (resolved instanceof ConditionalEffect conditional && conditional.interveningIf()
                          && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                          ConditionContext.forPermanent(perm, ownerId))) {
                      continue;
                  }
                if (resolved.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved))));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on opponent permanent tap (awaiting graveyard target)",
                            gameData.id, perm.getCard().getName());
                    markBatchedTapTrigger(gameData, perm, registration);
                    continue;
                }
                UUID bakedTargetId = bakeTriggeringPermanentControllerTarget(resolved, controllerId);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        bakedTargetId,
                        perm.getId()
                );
                entry.setTriggeringPermanentId(tappedPermanent.getId());
                gameData.enqueueTrigger(entry);
                if (oncePerTurn) {
                    gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                }
                markBatchedTapTrigger(gameData, perm, registration);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on opponent permanent tap ({})",
                        gameData.id, perm.getCard().getName(), tappedPermanent.getCard().getName());
            }
        });
    }

    private static boolean shouldSkipBatchedTapTrigger(GameData gameData, Permanent source,
                                                        EffectRegistration registration) {
        return registration.triggerMode() == TriggerMode.ONCE_PER_BATCH
                && gameData.permanentTapTriggerBatchDepth > 0
                && gameData.permanentTapTriggerBatchFiredEffects.getOrDefault(source.getId(), Set.of())
                .contains(registration.effect());
    }

    private static void markBatchedTapTrigger(GameData gameData, Permanent source,
                                               EffectRegistration registration) {
        if (registration.triggerMode() != TriggerMode.ONCE_PER_BATCH
                || gameData.permanentTapTriggerBatchDepth <= 0) {
            return;
        }
        gameData.permanentTapTriggerBatchFiredEffects
                .computeIfAbsent(source.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(registration.effect());
    }

    /**
     * Collects abilities that trigger when a creature pays a Vehicle's crew cost. The Vehicle is
     * carried as the triggering permanent so the triggered effect can resolve against that object.
     */
    public void checkCrewsVehicleTriggers(GameData gameData, Permanent crewingCreature, Permanent vehicle) {
        if (vehicle == null || !vehicle.getCard().getSubtypes().contains(CardSubtype.VEHICLE)) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, crewingCreature.getId());
        if (controllerId == null) {
            return;
        }
        for (CardEffect effect : crewingCreature.getCard().getEffects(EffectSlot.ON_CREWS_VEHICLE)) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    crewingCreature.getCard(),
                    controllerId,
                    crewingCreature.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    crewingCreature.getId()
            );
            entry.setTriggeringPermanentId(vehicle.getId());
            gameData.pendingActivatedAbilityCostTriggers.add(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(crewingCreature.getCard()));
            log.info("Game {} - {} triggers when it crews {}",
                    gameData.id, crewingCreature.getCard().getName(), vehicle.getCard().getName());
        }
    }

    /**
     * For ally/opponent becomes-tapped slots: bake the tapped permanent's controller as
     * {@code targetId} only when the resolved effect deals damage to
     * {@link DamageRecipient#TRIGGERING_PERMANENT_CONTROLLER}. Other effects keep {@code null}
     * so may-target tap triggers (Surgespanner) still choose at resolution.
     */
    private static UUID bakeTriggeringPermanentControllerTarget(CardEffect resolved, UUID tappedControllerId) {
        if (resolved instanceof DealDamageToPlayersEffect damage
                && damage.recipient() == DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER) {
            return tappedControllerId;
        }
        return null;
    }

    // ── Becomes-untapped triggers ──────────────────────────────────────

    /**
     * "Whenever this permanent becomes untapped" triggers (e.g. Hollowsage). Called from the untap
     * call sites after a permanent transitions from tapped to untapped. Fires only on the permanent
     * that became untapped, queueing its triggered ability with the permanent as its source;
     * targeted effects choose targets as the ability is put on the stack.
     */
    public void checkBecomesUntappedTriggers(GameData gameData, Permanent untappedPermanent) {
        UUID controllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(untappedPermanent)) {
                controllerId = pid;
                break;
            }
        }
        if (controllerId == null) return;

        for (CardEffect effect : untappedPermanent.getCard().getEffects(EffectSlot.ON_SELF_BECOMES_UNTAPPED)) {
            var match = new TriggerMatchContext(gameData, untappedPermanent, controllerId, effect);
            dispatch(match, EffectSlot.ON_SELF_BECOMES_UNTAPPED, effect,
                    new TriggerContext.SelfBecomesUntapped(controllerId));
        }

        // "Whenever a permanent you control becomes untapped" triggers (e.g. Wake Thrasher).
        UUID untappedControllerId = controllerId;
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(untappedControllerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ALLY_PERMANENT_BECOMES_UNTAPPED)) {
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(untappedPermanent, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ally permanent untap ({})",
                        gameData.id, perm.getCard().getName(), untappedPermanent.getCard().getName());
            }
        });

        gameData.forEachPermanent((ownerId, perm) -> {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_BECOMES_UNTAPPED)) {
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(
                            untappedPermanent, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                );
                entry.setEventPlayerIds(List.of(untappedControllerId));
                entry.setTriggeringPermanentId(untappedPermanent.getId());
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on any permanent untap ({})",
                        gameData.id, perm.getCard().getName(), untappedPermanent.getCard().getName());
            }
        });
    }

    /**
     * Checks triggers that care about one or more permanents untapping during the active player's
     * untap step. The event is dispatched once with the total count.
     */
    public void checkControllerUntapsDuringUntapStepTriggers(GameData gameData, UUID controllerId,
                                                               int untappedPermanentCount) {
        if (untappedPermanentCount <= 0) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        TriggerContext context = new TriggerContext.UntapStep(untappedPermanentCount);
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchSlot(gameData, permanent, controllerId,
                    EffectSlot.ON_CONTROLLER_UNTAPS_DURING_UNTAP_STEP, context);
        }
    }

    /**
     * "When this creature becomes renowned" (Relic Seeker) and "whenever a creature you control
     * becomes renowned" (Valeron Wardens) triggers. Called from
     * {@code RenownEffectHandler} at the moment renown flips the creature from not-renowned to
     * renowned; a creature that was already renowned never reaches this point (CR 702.112c).
     *
     * @param gameData         the current game state to modify
     * @param renownedCreature the creature that just became renowned
     * @param controllerId     the player controlling it
     */
    public void checkBecomesRenownedTriggers(GameData gameData, Permanent renownedCreature, UUID controllerId) {
        for (CardEffect effect : renownedCreature.getCard().getEffects(EffectSlot.ON_SELF_BECOMES_RENOWNED)) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    renownedCreature.getCard(),
                    controllerId,
                    renownedCreature.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    renownedCreature.getId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(renownedCreature.getCard()));
            log.info("Game {} - {} triggers on becoming renowned", gameData.id, renownedCreature.getCard().getName());
        }

        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(controllerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_BECOMES_RENOWNED)) {
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(renownedCreature, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ally creature becoming renowned ({})",
                        gameData.id, perm.getCard().getName(), renownedCreature.getCard().getName());
            }
        });
    }

    /** Fires abilities that trigger whenever the controller solves a Case. */
    public void checkAllyCaseSolvesTriggers(GameData gameData, Permanent solvedCase, UUID controllerId) {
        gameData.forEachPermanent((ownerId, permanent) -> {
            if (!ownerId.equals(controllerId)) return;
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_ALLY_CASE_SOLVES)) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, solvedCase.getCard(), gameData,
                        controllerId);
                if (resolved == null) continue;
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        permanent.getCard(),
                        controllerId,
                        permanent.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        permanent.getId()
                );
                entry.setTriggeringPermanentId(solvedCase.getId());
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                log.info("Game {} - {} triggers when a Case is solved", gameData.id,
                        permanent.getCard().getName());
            }
        });
    }

    /** Fires the newly monstrous permanent's own triggered abilities. */
    public void checkBecomesMonstrousTriggers(GameData gameData, Permanent monstrousPermanent,
                                               UUID controllerId) {
        checkBecomesMonstrousTriggers(gameData, monstrousPermanent, controllerId, 0);
    }

    public void checkBecomesMonstrousTriggers(GameData gameData, Permanent monstrousPermanent,
                                               UUID controllerId, int xValue) {
        TriggerContext.SelfBecomesMonstrous context =
                new TriggerContext.SelfBecomesMonstrous(controllerId, xValue);
        for (CardEffect effect : monstrousPermanent.getCard().getEffects(EffectSlot.ON_SELF_BECOMES_MONSTROUS)) {
            var match = new TriggerMatchContext(gameData, monstrousPermanent, controllerId, effect);
            registry.dispatch(match, EffectSlot.ON_SELF_BECOMES_MONSTROUS, effect, context);
        }
    }

    /**
     * "Whenever this permanent phases out" triggers (e.g. Teferi's Imp). Called from
     * {@code PhasingService} <em>before</em> the permanent leaves the battlefield: a phased-out
     * permanent is treated as though it does not exist (CR 702.26b), so these abilities look back in
     * time to the game state before it phased out (CR 603.10b).
     *
     * @param gameData     the current game state to modify
     * @param permanent    the permanent that is phasing out
     * @param controllerId the player controlling it as it phases out
     */
    public void checkPhasesOutTriggers(GameData gameData, Permanent permanent, UUID controllerId) {
        enqueuePhasingTriggers(gameData, permanent, controllerId, EffectSlot.ON_SELF_PHASES_OUT, "out");
    }

    /**
     * "Whenever this permanent phases in" triggers (e.g. Teferi's Imp). Called from
     * {@code PhasingService} after the permanent is back on its controller's battlefield
     * (CR 702.26c).
     *
     * @param gameData     the current game state to modify
     * @param permanent    the permanent that phased in
     * @param controllerId the player controlling it
     */
    public void checkPhasesInTriggers(GameData gameData, Permanent permanent, UUID controllerId) {
        enqueuePhasingTriggers(gameData, permanent, controllerId, EffectSlot.ON_SELF_PHASES_IN, "in");
    }

    private void enqueuePhasingTriggers(GameData gameData, Permanent permanent, UUID controllerId,
                                        EffectSlot slot, String direction) {
        for (CardEffect effect : permanent.getCard().getEffects(slot)) {
            // Targeted phase-in (Shimmering Efreet): choose the target when the ability is put on the
            // stack. Queued here during the untap-step phasing action and drained at upkeep start.
            if (slot == EffectSlot.ON_SELF_PHASES_IN
                    && effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                gameData.queueInteraction(new PermanentChoiceContext.PhasesInTriggerTarget(
                        permanent.getCard(), controllerId, new ArrayList<>(List.of(effect)), permanent.getId()));
                gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                log.info("Game {} - {} triggers on phasing {} (awaiting target)",
                        gameData.id, permanent.getCard().getName(), direction);
                continue;
            }
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    permanent.getCard(),
                    controllerId,
                    permanent.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    permanent.getId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
            log.info("Game {} - {} triggers on phasing {}", gameData.id, permanent.getCard().getName(), direction);
        }
    }

    // ── Ability-activation triggers ────────────────────────────────────

    /**
     * "Whenever you activate an ability of {a permanent}" triggers (e.g. Ceaseless Searblades).
     * Fires on every permanent the activating player controls that has an
     * {@link EffectSlot#ON_CONTROLLER_ACTIVATES_ABILITY} effect, filtered (when wrapped in
     * {@link TriggeringPermanentConditionalEffect}) by the permanent whose ability was activated.
     */
    public void checkControllerActivatesAbilityTriggers(GameData gameData, UUID activatingPlayerId,
                                                        Permanent activatedPermanent, ActivatedAbility ability) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(activatingPlayerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_ACTIVATES_ABILITY)) {
                CardEffect resolved = effect.resolveForActivatedAbility(ability);
                if (resolved == null) continue;
                resolved = resolveTriggeringPermanentConditional(gameData, perm, ownerId, activatedPermanent, resolved);
                if (resolved == null) continue;
                if (resolved.targetSpec().declares(TargetPredicates.anyTarget())) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)), false, null, 0, perm.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} queues an any-target ability-activation trigger ({})",
                            gameData.id, perm.getCard().getName(), activatedPermanent.getCard().getName());
                    continue;
                }
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ability activation ({})",
                        gameData.id, perm.getCard().getName(), activatedPermanent.getCard().getName());
            }
        });
    }

    /**
     * Fires "whenever you activate an exhaust ability" triggers for the activating player's
     * permanents. The caller invokes this only after the exhaust ability's activation costs have
     * been paid, so the trigger is queued above the activated ability.
     */
    public void checkControllerActivatesExhaustAbilityTriggers(GameData gameData, UUID activatingPlayerId) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(activatingPlayerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on exhaust ability activation", gameData.id, perm.getCard().getName());
            }
        });
    }

    /**
     * Fires graveyard-resident "whenever you activate an exhaust ability" triggers for the
     * activating player. These abilities function from the graveyard and return their own card.
     */
    public void checkControllerActivatesExhaustAbilityTriggersFromGraveyard(
            GameData gameData, UUID activatingPlayerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(activatingPlayerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            for (CardEffect effect : gameQueryService.getEffectiveGraveyardEffects(
                    gameData, card, EffectSlot.GRAVEYARD_ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        activatingPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} triggers from the graveyard on exhaust ability activation",
                        gameData.id, card.getName());
            }
        }
    }

    /**
     * Fires triggers for the creature tapped to saddle a Mount or crew a Vehicle during its
     * controller's main phase. The check runs once for each creature tapped to pay the cost.
     */
    public void checkSelfSaddlesOrCrewsDuringMainPhaseTriggers(GameData gameData, UUID activatingPlayerId,
                                                                Permanent tappedCreature,
                                                                UUID saddledOrCrewedPermanentId) {
        if (!activatingPlayerId.equals(gameData.activePlayerId)
                || (gameData.currentStep != TurnStep.PRECOMBAT_MAIN
                && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN)) {
            return;
        }

        Permanent saddledOrCrewedPermanent = gameQueryService.findPermanentById(gameData, saddledOrCrewedPermanentId);
        if (saddledOrCrewedPermanent == null) return;
        Set<CardSubtype> subtypes = gameQueryService.effectiveCreatureSubtypes(gameData, saddledOrCrewedPermanent);
        if (!subtypes.contains(CardSubtype.MOUNT) && !subtypes.contains(CardSubtype.VEHICLE)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(activatingPlayerId);
        if (battlefield == null) return;
        Permanent watcher = battlefield.stream()
                .filter(permanent -> permanent.getId().equals(tappedCreature.getId()))
                .findFirst()
                .orElse(null);
        if (watcher == null || watcher.isLosesAllAbilitiesUntilEndOfTurn()) return;

        List<CardEffect> effects = new ArrayList<>(watcher.getCard().getEffects(
                EffectSlot.ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                gameData, watcher, EffectSlot.ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE));
        if (effects.isEmpty()) return;

        int triggerCopies = 1 +
                gameQueryService.countAdditionalTriggeredAbilityTriggers(gameData, activatingPlayerId, watcher);
        for (CardEffect effect : effects) {
            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.getCard(),
                    activatingPlayerId,
                    watcher.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    saddledOrCrewedPermanent.getId());
            trigger.setNonTargeting(true);
            trigger.setTriggeringPermanentId(watcher.getId());
            gameData.pendingActivatedAbilityCostTriggers.add(trigger);
            for (int copy = 1; copy < triggerCopies; copy++) {
                gameData.pendingActivatedAbilityCostTriggers.add(new StackEntry(trigger));
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
            log.info("Game {} - {} triggers when its controller saddles or crews {}",
                    gameData.id, watcher.getCard().getName(), saddledOrCrewedPermanent.getCard().getName());
        }
    }

    private boolean checkDiscardEventTriggers(GameData gameData, UUID discardingPlayerId,
                                              int discardedCount) {
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(discardingPlayerId);
        if (ownBattlefield == null) {
            return false;
        }

        boolean[] triggered = {false};
        var ctx = new TriggerContext.DiscardEvent(discardingPlayerId, discardedCount);
        for (Permanent perm : List.copyOf(ownBattlefield)) {
            if (dispatchSlot(gameData, perm, discardingPlayerId,
                    EffectSlot.ON_CONTROLLER_DISCARD_EVENT, ctx)) {
                triggered[0] = true;
            }
        }
        return triggered[0];
    }

    /**
     * "Whenever you activate an eternalize or embalm ability" triggers (Vizier of the Anointed).
     * Fires once per activation on every permanent the activating player controls that has an
     * {@link EffectSlot#ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM} effect. Called from the
     * graveyard-ability activation path only when the activated ability is an embalm/eternalize
     * ability, so no per-permanent condition is needed.
     */
    public void checkControllerActivatesEternalizeOrEmbalmTriggers(GameData gameData, UUID activatingPlayerId) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(activatingPlayerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on eternalize/embalm activation",
                        gameData.id, perm.getCard().getName());
            }
        });
    }

    /**
     * "Whenever an opponent activates an ability of {a permanent}, if it isn't a mana ability, ..."
     * triggers (e.g. Harsh Mentor). Fires on every permanent NOT controlled by the activating player
     * that has an {@link EffectSlot#ON_OPPONENT_ACTIVATES_NONMANA_ABILITY} effect, optionally filtered
     * (when wrapped in {@link TriggeringPermanentConditionalEffect}) by the permanent whose ability was
     * activated. Called only from the non-mana activation path, so mana abilities never trigger it (the
     * "if it isn't a mana ability" clause). The activating player is baked as the non-targeting
     * {@code targetId} so a player-directed effect (e.g. {@code DealDamageToPlayersEffect(2, TARGET_PLAYER)})
     * acts on "that player".
     */
    public void checkOpponentActivatesNonManaAbilityTriggers(GameData gameData, UUID activatingPlayerId,
                                                              StackEntry abilityEntry, ActivatedAbility ability,
                                                              Permanent activatedPermanent) {
        if (abilityEntry == null) return;
        gameData.forEachPermanent((ownerId, perm) -> {
            if (ownerId.equals(activatingPlayerId)) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_ACTIVATES_NONMANA_ABILITY)) {
                CardEffect resolved = effect.resolveForActivatedAbility(ability);
                if (resolved == null) continue;
                resolved = resolveTriggeringPermanentConditional(
                        gameData, perm, ownerId, activatedPermanent, resolved);
                if (resolved == null) continue;
                UUID targetId = EffectResolution.targetsSpellOnStack(resolved)
                        ? abilityEntry.getCard().getId()
                        : activatingPlayerId;
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        targetId,
                        perm.getId());
                // "That player" is the opponent who activated the ability — set by the event, not chosen.
                trigger.setNonTargeting(true);
                gameData.enqueueTrigger(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on opponent non-mana ability activation ({})",
                        gameData.id, perm.getCard().getName(), activatedPermanent.getCard().getName());
            }
        });
    }

    /**
     * Unwraps a {@link TriggeringPermanentConditionalEffect} against the permanent whose event fired.
     * Returns the wrapped effect when the predicate matches (evaluated from {@code watcher}'s point of
     * view), the effect unchanged when it isn't a conditional, or {@code null} to signal the trigger
     * should be skipped for this watcher. Shared by the ability-activation trigger collectors.
     */
    private CardEffect resolveTriggeringPermanentConditional(GameData gameData, Permanent watcher, UUID watcherOwnerId,
                                                             Permanent triggeringPermanent, CardEffect effect) {
        if (!(effect instanceof TriggeringPermanentConditionalEffect conditional)) {
            return effect;
        }
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(watcher.getOriginalCard().getId())
                .withSourceControllerId(watcherOwnerId)
                .withSourcePermanentSnapshot(watcher);
        if (!predicateEvaluationService.matchesPermanentPredicate(triggeringPermanent, conditional.predicate(), filterContext)) {
            return null;
        }
        return conditional.wrapped();
    }

    /**
     * "Whenever you activate an ability, if it isn't a mana ability, you may pay {N} to copy it"
     * triggers (Rings of Brighthearth). Called after the non-mana ability has been put on the stack
     * so it can be snapshotted. Fires on every permanent the activating player controls that has an
     * {@link EffectSlot#ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY} effect — except for
     * {@code equippedCreatureOnly} triggers (Illusionist's Bracers), which instead fire on any
     * permanent attached to the ability's source permanent, and copy the ability for free.
     *
     * @param abilityEntry the activated ability's stack entry (already on the stack)
     * @param ability      the activated ability that was activated (retained for retargeting the copy)
     */
    public void checkControllerActivatesNonManaAbilityTriggers(GameData gameData, UUID activatingPlayerId,
                                                               StackEntry abilityEntry, ActivatedAbility ability,
                                                               Permanent activatedPermanent) {
        if (abilityEntry == null) return;

        Integer pendingLoyaltyCopies = gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingLoyaltyCopies != null && pendingLoyaltyCopies > 0 && ability.getLoyaltyCost() != null) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            List<CardEffect> copyEffects = new ArrayList<>(pendingLoyaltyCopies);
            for (int i = 0; i < pendingLoyaltyCopies; i++) {
                copyEffects.add(new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId));
            }
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    abilityEntry.getCard(),
                    activatingPlayerId,
                    "Copy " + abilityEntry.getCard().getName() + "'s loyalty ability",
                    copyEffects
            ));
            gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.remove(activatingPlayerId);
        }

        Integer pendingExhaustCopies = gameData.pendingNextExhaustAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingExhaustCopies != null && pendingExhaustCopies > 0 && ability.isExhaustAbility()) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            List<CardEffect> copyEffects = new ArrayList<>(pendingExhaustCopies);
            for (int i = 0; i < pendingExhaustCopies; i++) {
                copyEffects.add(new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId));
            }
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    abilityEntry.getCard(),
                    activatingPlayerId,
                    "Copy " + abilityEntry.getCard().getName() + "'s exhaust ability",
                    copyEffects
            ));
            gameData.pendingNextExhaustAbilityCopyThisTurnCount.remove(activatingPlayerId);
            gameLogService.append(gameData, GameLog.cardThen(abilityEntry.getCard(), "'s exhaust ability is copied."));
            log.info("Game {} - {} delayed exhaust-ability copy trigger(s) queued for {}",
                    gameData.id, pendingExhaustCopies, abilityEntry.getCard().getName());
        }

        gameData.forEachPermanent((ownerId, perm) -> {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY)) {
                if (!(effect instanceof CopyControllerActivatedAbilityTriggerEffect trigger)) {
                    if (!ownerId.equals(activatingPlayerId)) continue;
                    CardEffect resolved = effect.resolveForActivatedAbility(ability);
                    if (resolved == null) continue;
                    resolved = resolveTriggeringPermanentConditional(
                            gameData, perm, ownerId, activatedPermanent, resolved);
                    if (resolved == null) continue;
                    if (resolved.targetSpec().declares(TargetPredicates.anyTarget())) {
                        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                                perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)), false, null, 0,
                                perm.getId()));
                        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                        log.info("Game {} - {} queues an any-target non-mana ability trigger ({})",
                                gameData.id, perm.getCard().getName(), abilityEntry.getCard().getName());
                    } else {
                        UUID targetId = EffectResolution.targetsSpellOnStack(resolved)
                                ? abilityEntry.getCard().getId()
                                : null;
                        StackEntry activationTrigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                ownerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(resolved)),
                                targetId,
                                perm.getId()
                        );
                        if (targetId != null) {
                            activationTrigger.setNonTargeting(true);
                        }
                        gameData.enqueueTrigger(activationTrigger);
                        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                        log.info("Game {} - {} triggers on non-mana ability activation ({})",
                                gameData.id, perm.getCard().getName(), abilityEntry.getCard().getName());
                    }
                    continue;
                }
                if (trigger.equippedCreatureOnly()) {
                    // "an ability of equipped creature" — the ability's source must be what this
                    // permanent is attached to, whoever activated it.
                    if (perm.getAttachedTo() == null
                            || !perm.getAttachedTo().equals(abilityEntry.getSourcePermanentId())) {
                        continue;
                    }
                } else if (!ownerId.equals(activatingPlayerId)) {
                    continue;
                }
                if (trigger.sourceFilter() != null && !targetLegalityService.matchesStackEntryPredicate(
                        gameData, abilityEntry, trigger.sourceFilter(), ownerId)) {
                    continue;
                }
                if (trigger.targetPredicate() != null && !targetLegalityService.matchesStackEntryPredicate(
                        gameData, abilityEntry, trigger.targetPredicate(), ownerId)) {
                    continue;
                }
                if (trigger.loyaltyAbilityOnly() && ability.getLoyaltyCost() == null) {
                    continue;
                }

                StackEntry snapshot = new StackEntry(abilityEntry);
                // CR 707.10 — the copy is controlled by the controller of the effect that created it.
                UUID copyControllerId = trigger.equippedCreatureOnly() ? ownerId : activatingPlayerId;
                CardEffect copyEffect = new CopyControllerActivatedAbilityEffect(
                        snapshot, ability, copyControllerId);
                if (trigger.manaCost() != null) {
                    copyEffect = new MayPayManaEffect(
                            trigger.manaCost(),
                            copyEffect,
                            "Pay " + trigger.manaCost() + " to copy " + abilityEntry.getCard().getName() + "'s ability?");
                }

                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(copyEffect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on non-mana ability activation ({})",
                        gameData.id, perm.getCard().getName(), abilityEntry.getCard().getName());
            }
        });
    }

    // ── Life-loss triggers ─────────────────────────────────────────────

    public void checkLifeLossTriggers(GameData gameData, UUID losingPlayerId, int lifeLostAmount) {
        if (lifeLostAmount <= 0) return;

        // Accumulate life lost this turn (damage funnels through here too — "damage causes loss of
        // life"). Read by Wound Reflection at end of turn.
        gameData.lifeLostThisTurn.merge(losingPlayerId, lifeLostAmount, Integer::sum);
        increaseActivePlayerSpeedIfEligible(gameData, losingPlayerId);

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.LifeLoss(losingPlayerId, lifeLostAmount);

        // Snapshot: handlers may modify the battlefield (e.g. Mindcrank mills → Undead Alchemist creates tokens)
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(losingPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_LOSES_LIFE)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (dispatch(match, EffectSlot.ON_OPPONENT_LOSES_LIFE, effect, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        // Controller-loses-life triggers (e.g. Lich's Mastery)
        // Snapshot: handlers may modify the battlefield (exile permanents)
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (!playerId.equals(losingPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_LOSES_LIFE)) {
                    CardEffect toDispatch = effect;
                    if (effect instanceof OncePerTurnTriggerEffect once) {
                        if (gameData.oncePerTurnTriggersFiredThisTurn.contains(perm.getId())) {
                            continue;
                        }
                        toDispatch = once.wrapped();
                    }
                    toDispatch = unwrapLifeChangeConditional(gameData, perm, playerId, toDispatch);
                    if (toDispatch == null) {
                        continue;
                    }
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (registry.dispatch(match, EffectSlot.ON_CONTROLLER_LOSES_LIFE, toDispatch, ctx)) {
                        anyTriggered[0] = true;
                        if (effect instanceof OncePerTurnTriggerEffect) {
                            gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                        }
                    }
                }
            }
        });

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    // ── Life-gain triggers ──────────────────────────────────────────────

    /** Fires triggers for life actually paid by a player, distinct from ordinary life loss. */
    public void checkLifePaymentTriggers(GameData gameData, UUID payingPlayerId, int lifePaidAmount) {
        if (lifePaidAmount <= 0) return;

        var ctx = new TriggerContext.LifePayment(payingPlayerId, lifePaidAmount);
        List<Permanent> battlefield = gameData.playerBattlefields.get(payingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            List<CardEffect> effects = new ArrayList<>(
                    perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_PAYS_LIFE));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, perm, EffectSlot.ON_CONTROLLER_PAYS_LIFE));
            for (CardEffect effect : effects) {
                CardEffect toDispatch = effect;
                if (effect instanceof OncePerTurnTriggerEffect once) {
                    if (gameData.oncePerTurnTriggersFiredThisTurn.contains(perm.getId())) {
                        continue;
                    }
                    toDispatch = once.wrapped();
                }
                var match = new TriggerMatchContext(gameData, perm, payingPlayerId, effect);
                if (dispatch(match, EffectSlot.ON_CONTROLLER_PAYS_LIFE, toDispatch, ctx)
                        && effect instanceof OncePerTurnTriggerEffect) {
                    gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                }
            }
        }
    }

    private void increaseActivePlayerSpeedIfEligible(GameData gameData, UUID losingPlayerId) {
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null || activePlayerId.equals(losingPlayerId)) {
            return;
        }
        int speed = gameData.playerSpeeds.getOrDefault(activePlayerId, 0);
        if (speed < 1 || speed >= 4 || !gameData.playersWhoseSpeedIncreasedThisTurn.add(activePlayerId)) {
            return;
        }
        int newSpeed = speed + 1;
        gameData.playerSpeeds.put(activePlayerId, newSpeed);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(activePlayerId) + " increases their speed to " + newSpeed + "."));
        log.info("Game {} - {} increases speed to {}", gameData.id,
                gameData.playerIdToName.get(activePlayerId), newSpeed);
    }

    public void checkLifeGainTriggers(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount) {
        checkLifeGainTriggers(gameData, gainingPlayerId, lifeGainedAmount, null, null);
    }

    public void checkLifeGainTriggers(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount,
            Card sourceCard, StackEntryType sourceEntryType) {
        if (lifeGainedAmount <= 0) return;

        var ctx = new TriggerContext.LifeGain(gainingPlayerId, lifeGainedAmount, sourceCard, sourceEntryType);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (!playerId.equals(gainingPlayerId)) return;

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = new ArrayList<>(
                        perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_GAINS_LIFE));
                effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                        gameData, perm, EffectSlot.ON_CONTROLLER_GAINS_LIFE));
                for (CardEffect effect : effects) {
                    CardEffect toDispatch = effect;
                    if (effect instanceof OncePerTurnTriggerEffect once) {
                        if (gameData.oncePerTurnTriggersFiredThisTurn.contains(perm.getId())) {
                            continue;
                        }
                        toDispatch = once.wrapped();
                    }
                    toDispatch = unwrapLifeChangeConditional(gameData, perm, playerId, toDispatch);
                    if (toDispatch == null) {
                        continue;
                    }
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (dispatch(match, EffectSlot.ON_CONTROLLER_GAINS_LIFE, toDispatch, ctx)
                            && effect instanceof OncePerTurnTriggerEffect) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                }
            }
        });

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(gainingPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                dispatchSlot(gameData, perm, playerId, EffectSlot.ON_OPPONENT_GAINS_LIFE, ctx);
            }
        });

        collectGraveyardControllerLifeGainTriggers(gameData, gainingPlayerId);
        collectGraveyardOpponentLifeGainTriggers(gameData, gainingPlayerId);
        collectLifeGainOpponentLifeLossTriggers(gameData, gainingPlayerId, lifeGainedAmount);
    }

    /** Fires triggers for a player's positive energy-counter change. */
    public void checkEnergyGainTriggers(GameData gameData, UUID gainingPlayerId, int energyGainedAmount) {
        if (energyGainedAmount <= 0) return;

        var ctx = new TriggerContext.EnergyGain(gainingPlayerId, energyGainedAmount);
        List<Permanent> battlefield = gameData.playerBattlefields.get(gainingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, gainingPlayerId, EffectSlot.ON_CONTROLLER_GETS_ENERGY, ctx);
        }
    }

    /** Fires controller-scoped triggers once for each proliferate event. */
    public void checkProliferateTriggers(GameData gameData, UUID proliferatingPlayerId,
                                         int proliferateCount) {
        if (proliferateCount <= 0) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(proliferatingPlayerId);
        if (battlefield != null) {
            TriggerContext.Proliferate ctx = new TriggerContext.Proliferate(proliferatingPlayerId);
            for (Permanent perm : List.copyOf(battlefield)) {
                for (int i = 0; i < proliferateCount; i++) {
                    dispatchSlot(gameData, perm, proliferatingPlayerId,
                            EffectSlot.ON_CONTROLLER_PROLIFERATES, ctx);
                }
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(proliferatingPlayerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            List<CardEffect> effects = gameQueryService.getEffectiveGraveyardEffects(
                    gameData, card, EffectSlot.GRAVEYARD_ON_CONTROLLER_PROLIFERATES);
            if (effects == null || effects.isEmpty()) continue;

            for (int i = 0; i < proliferateCount; i++) {
                for (CardEffect effect : effects) {
                    gameData.enqueueTrigger(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            proliferatingPlayerId,
                            card.getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    log.info("Game {} - {} graveyard trigger queued on proliferate",
                            gameData.id, card.getName());
                }
            }
        }
    }

    private void collectGraveyardOpponentLifeGainTriggers(GameData gameData, UUID gainingPlayerId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(gainingPlayerId)) continue;

            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;

            for (Card card : new ArrayList<>(graveyard)) {
                for (CardEffect effect : gameQueryService.getEffectiveGraveyardEffects(
                        gameData, card, EffectSlot.GRAVEYARD_ON_OPPONENT_GAINS_LIFE)) {
                    gameData.enqueueTrigger(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            playerId,
                            card.getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    log.info("Game {} - {} triggers from the graveyard when an opponent gains life",
                            gameData.id, card.getName());
                }
            }
        }
    }

    /**
     * Fires the turn-scoped "whenever you gain life this turn, each opponent loses that much life"
     * delayed triggers (Vizkopa Guildmage). One trigger per watcher whose controller is the player
     * who gained life; the amount rides on the entry's event value.
     */
    private void collectLifeGainOpponentLifeLossTriggers(GameData gameData, UUID gainingPlayerId,
                                                        int lifeGainedAmount) {
        for (LifeGainOpponentLifeLossWatcher watcher : List.copyOf(gameData.lifeGainOpponentLifeLossWatchers)) {
            if (!watcher.controllerId().equals(gainingPlayerId)) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new LoseLifeEffect(new EventValue(), LoseLifeRecipient.EACH_OPPONENT))),
                    (UUID) null,
                    (UUID) null);
            entry.setEventValue(lifeGainedAmount);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers (controller gained {} life), each opponent loses that much life",
                    gameData.id, watcher.sourceCard().getName(), lifeGainedAmount);
        }
    }

    // ── Creature-card-milled triggers ─────────────────────────────────

    public void checkCreatureCardMilledTriggers(GameData gameData, UUID milledPlayerId, Card milledCard) {
        var ctx = new TriggerContext.CreatureCardMilled(milledPlayerId, milledCard);

        // Snapshot battlefields: trigger handlers may add tokens to the battlefield
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(milledPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_CARD_MILLED)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    dispatch(match, EffectSlot.ON_OPPONENT_CREATURE_CARD_MILLED, effect, ctx);
                }
            }
        });
    }

    /** Fires once for a library-to-graveyard event containing one or more creature cards. */
    public void checkCreatureCardsPutIntoGraveyardFromLibraryTriggers(
            GameData gameData, UUID graveyardOwnerId, int creatureCardCount) {
        if (creatureCardCount <= 0) return;

        var ctx = new TriggerContext.CreatureCardsPutIntoGraveyardFromLibrary(
                graveyardOwnerId, creatureCardCount);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, graveyardOwnerId,
                    EffectSlot.ON_ALLY_CREATURE_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY, ctx);
        }
    }

    /** Fires once for a library-to-graveyard event containing one or more cards. */
    public void checkCardsPutIntoGraveyardFromLibraryTriggers(
            GameData gameData, UUID graveyardOwnerId, int cardCount) {
        if (cardCount <= 0) return;

        var ctx = new TriggerContext.CardsPutIntoGraveyardFromLibrary(
                graveyardOwnerId, cardCount);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, graveyardOwnerId,
                    EffectSlot.ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY, ctx);
        }
    }

    // ── Noncombat-damage-to-opponent triggers ──────────────────────────

    /** Fires for each creature card put into any player's graveyard from a library. */
    public void checkAnyCreatureCardPutIntoGraveyardFromLibraryTriggers(
            GameData gameData, UUID graveyardOwnerId, Card creatureCard) {
        var ctx = new TriggerContext.CreatureCardPutIntoGraveyardFromLibrary(
                creatureCard, graveyardOwnerId);
        gameData.forEachPermanent((playerId, permanent) -> dispatchSlot(
                gameData, permanent, playerId,
                EffectSlot.ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY, ctx));
    }

    public void checkNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId) {
        checkNoncombatDamageToOpponentTriggers(gameData, damagedPlayerId, null, 0);
    }

    public void checkNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int damageAmount) {
        var ctx = new TriggerContext.NoncombatDamageToOpponent(
                damagedPlayerId, sourceControllerId, damageAmount);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(damagedPlayerId)) return;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE)) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    dispatch(match, EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);
                }
            }
        });
    }

    /**
     * Chandra's Phoenix: "Whenever an opponent is dealt damage by a red instant or sorcery spell you
     * control or by a red planeswalker you control, return this card from your graveyard to your hand."
     *
     * <p>{@code entry} is the stack entry that dealt the damage. It qualifies when it is a red instant
     * or sorcery spell, or an ability whose source card is a red planeswalker. The graveyard scanned is
     * the entry controller's ("you control"), and the damaged player must be one of their opponents.
     */
    public void checkRedSpellOrPlaneswalkerDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
                                                                    StackEntry entry) {
        if (entry == null) return;

        UUID controllerId = entry.getControllerId();
        if (controllerId == null || controllerId.equals(damagedPlayerId)) return;

        Card sourceCard = entry.getEffectiveDamageSourceCard();
        if (sourceCard == null || !sourceCard.getColors().contains(CardColor.RED)) return;

        boolean redSpell = entry.getEntryType() == StackEntryType.INSTANT_SPELL
                || entry.getEntryType() == StackEntryType.SORCERY_SPELL;
        boolean redPlaneswalker = sourceCard.hasType(CardType.PLANESWALKER);
        if (!redSpell && !redPlaneswalker) return;

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            List<CardEffect> effects =
                    gameQueryService.getEffectiveGraveyardEffects(
                            gameData, card, EffectSlot.GRAVEYARD_ON_OPPONENT_DAMAGED_BY_RED_SPELL_OR_PLANESWALKER);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
            }
        }
    }

    private void collectAllyCreatureBecomesTargetOfBackupAbilityTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)
                || triggeringEntry.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                || triggeringEntry.getCard() == null
                || !triggeringEntry.getCard().getKeywords().contains(Keyword.BACKUP)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (source.isBackupAbilityCopyUsedThisTurn()) continue;

            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                    EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_BACKUP_ABILITY));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_BACKUP_ABILITY));
            if (effects.isEmpty()) continue;

            source.setBackupAbilityCopyUsedThisTurn(true);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    creatureControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(effects),
                    triggeringEntry.getCard().getId(),
                    source.getId()
            );
            entry.setNonTargeting(true);
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s triggered ability triggers and copies the backup ability."));
            log.info("Game {} - {} backup-ability copy trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * Bloodfeather Phoenix: "Whenever an instant or sorcery spell you control deals damage to an
     * opponent or battle". The caller has already batched the spell's damage event and supplies
     * whether that event hit a qualifying recipient.
     */
    public void checkInstantOrSorceryDamageToOpponentOrBattleTriggers(GameData gameData,
                                                                       Card sourceCard,
                                                                       UUID sourceControllerId,
                                                                       boolean damagedOpponentOrBattle) {
        if (!damagedOpponentOrBattle || sourceCard == null || sourceControllerId == null
                || (!sourceCard.hasType(CardType.INSTANT) && !sourceCard.hasType(CardType.SORCERY))) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(sourceControllerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            List<CardEffect> effects = gameQueryService.getEffectiveGraveyardEffects(
                    gameData, card, EffectSlot.GRAVEYARD_ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT_OR_BATTLE);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        sourceControllerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
            }
        }
    }

    // ── Queue-processing delegates ─────────────────────────────────────

    public void processNextDeathTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDeathTriggerTarget(gameData);
    }

    public void processNextAttackTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextAttackTriggerTarget(gameData);
    }

    public void processNextAttackCounterMoveFirstTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextAttackCounterMoveFirstTarget(gameData);
    }

    public List<UUID> targetableCreaturesControlledBy(GameData gameData, UUID playerId,
                                                      Card sourceCard, UUID choosingPlayerId) {
        return triggeredAbilityQueueService.targetableCreaturesControlledBy(gameData, playerId, sourceCard, choosingPlayerId);
    }

    public void processNextEntersTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEntersTriggerTarget(gameData);
    }

    public void processNextSelfTriggeredAbilityTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSelfTriggeredAbilityTarget(gameData);
    }

    public void processNextTriggeredModalTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextTriggeredModalTrigger(gameData);
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, ChooseOneEffect.ChooseOneOption chosen) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen);
    }

    public void processNextDiscardSelfTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextDiscardSelfTrigger(gameData);
    }

    public void processNextDiscardControllerTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDiscardControllerTriggerTarget(gameData);
    }

    public void processNextSpellTargetTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextSpellTargetTrigger(gameData);
    }

    public void processNextETBSpellTargetTrigger(GameData gameData) {
        etbTokenTargetService.processNextETBSpellTargetTrigger(gameData);
    }

    public void processNextSpellGraveyardTargetTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextSpellGraveyardTargetTrigger(gameData);
    }

    public void processNextEmblemTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
    }

    public void processNextLifeGainTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextLifeGainTriggerTarget(gameData);
    }

    public void processNextDrawTriggerTarget(GameData gameData) {
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DrawTriggerAnyTarget.class)) {
            triggeredAbilityQueueService.processNextDrawTriggerTarget(gameData);
        } else if (gameData.hasPendingInteraction(PermanentChoiceContext.DrawTriggerPermanentTarget.class)) {
            triggeredAbilityQueueService.processNextDrawTriggerPermanentTarget(gameData);
        }
    }

    private void collectGraveyardControllerLifeGainTriggers(GameData gameData, UUID gainingPlayerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(gainingPlayerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            for (CardEffect effect : gameQueryService.getEffectiveGraveyardEffects(
                    gameData, card, EffectSlot.GRAVEYARD_ON_CONTROLLER_GAINS_LIFE)) {
                CardEffect resolved = effect;
                boolean oncePerTurn = effect instanceof OncePerTurnTriggerEffect;
                if (oncePerTurn) {
                    if (gameData.oncePerTurnTriggersFiredThisTurn.contains(card.getId())) {
                        continue;
                    }
                    resolved = ((OncePerTurnTriggerEffect) effect).wrapped();
                }

                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        gainingPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(resolved))
                ));
                if (oncePerTurn) {
                    gameData.oncePerTurnTriggersFiredThisTurn.add(card.getId());
                }
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} triggers from the graveyard when its controller gains life",
                        gameData.id, card.getName());
            }
        }
    }

    public void processNextEnteringPermanentAnyTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEnteringPermanentAnyTarget(gameData);
    }

    public void processNextSagaChapterTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterTarget(gameData);
    }

    public void processNextSagaChapterPlayerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterPlayerTarget(gameData);
    }

    public void processNextSagaChapterGraveyardTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterGraveyardTarget(gameData);
    }

    // ── Explore triggers ──────────────────────────────────────────────

    /**
     * Scans the exploring creature's controller's battlefield for permanents
     * with {@link EffectSlot#ON_ALLY_CREATURE_EXPLORES} effects and queues
     * them for target selection or directly onto the stack.
     */
    public void checkExploreTriggers(GameData gameData, UUID controllerId, Card exploredCard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            List<CardEffect> registeredEffects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_EXPLORES);
            if (registeredEffects == null || registeredEffects.isEmpty()) continue;

            List<CardEffect> effects = registeredEffects.stream()
                    .map(effect -> {
                        if (!(effect instanceof TriggeringCardConditionalEffect conditional)) {
                            return effect;
                        }
                        return exploredCard != null && predicateEvaluationService.matchesCardPredicate(
                                exploredCard, conditional.predicate(), perm.getCard().getId(), gameData, controllerId)
                                ? conditional.wrapped() : null;
                    })
                    .filter(Objects::nonNull)
                    .toList();
            if (effects.isEmpty()) continue;

            boolean anyTargeting = effects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
            if (anyTargeting) {
                gameData.queueInteraction(
                        new PermanentChoiceContext.ExploreTriggerTarget(
                                perm.getCard(), controllerId, new ArrayList<>(effects), perm.getId()));
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
                        null,
                        perm.getId()
                ));
            }
            log.info("Game {} - {} explore trigger queued", gameData.id, perm.getCard().getName());
        }
    }

    public void processNextExploreTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextExploreTriggerTarget(gameData);
    }

    // ── Exploit triggers ──────────────────────────────────────────────

    /**
     * Queues this permanent's {@link EffectSlot#ON_EXPLOIT} ability after it successfully
     * exploited a creature. Uses the source card's LKI even if the permanent left the battlefield
     * (e.g. it sacrificed itself).
     */
    public void checkExploitTriggers(GameData gameData, Card sourceCard, UUID controllerId, UUID sourcePermanentId) {
        List<CardEffect> effects = sourceCard.getEffects(EffectSlot.ON_EXPLOIT);
        if (effects == null || effects.isEmpty()) return;

        boolean targetsStack = effects.stream().anyMatch(EffectResolution::targetsSpellOnStack);
        if (targetsStack) {
            StackEntryPredicate stackFilter = null;
            boolean includeAbilities = false;
            if (sourceCard.getTargetFilter() instanceof com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter sf) {
                stackFilter = sf.predicate();
                includeAbilities = predicateContainsHasTarget(sf.predicate());
            }
            gameData.queueInteraction(new PermanentChoiceContext.ExploitTriggerTarget(
                    sourceCard, controllerId, new ArrayList<>(effects), sourcePermanentId,
                    stackFilter, includeAbilities));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    controllerId,
                    sourceCard.getName() + "'s exploit ability",
                    new ArrayList<>(effects),
                    null,
                    sourcePermanentId
            ));
        }
        gameLogService.append(gameData,
                GameLog.cardThen(sourceCard, " exploits a creature."));
        log.info("Game {} - {} exploit trigger queued", gameData.id, sourceCard.getName());
    }

    public void processNextExploitTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextExploitTriggerTarget(gameData);
    }

    public static boolean predicateContainsHasTarget(com.github.laxika.magicalvibes.model.filter.StackEntryPredicate predicate) {
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate) {
            return true;
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate typeIn) {
            return typeIn.spellTypes().contains(StackEntryType.ACTIVATED_ABILITY)
                    || typeIn.spellTypes().contains(StackEntryType.TRIGGERED_ABILITY);
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate allOf) {
            return allOf.predicates().stream().anyMatch(TriggerCollectionService::predicateContainsHasTarget);
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate anyOf) {
            return anyOf.predicates().stream().anyMatch(TriggerCollectionService::predicateContainsHasTarget);
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate not) {
            return predicateContainsHasTarget(not.predicate());
        }
        return false;
    }

    // ── Clash ──────────────────────────────────────────────────────────

    /**
     * Performs a clash for {@code clashingPlayerId} against their (single, 2-player) opponent
     * (MTG rule 701.29): both players reveal the top card of their library, the clashing player
     * wins if their revealed card's mana value is strictly greater than the opponent's, and then
     * {@link EffectSlot#ON_CONTROLLER_CLASHES} triggers on the clashing player's permanents.
     *
     * <p>Each player may put their revealed card on the bottom of their library; this engine leaves
     * the revealed cards on top (a legal choice), so no clash-source card yet mutates library order.
     * The "whenever you clash" triggers fire after the clash ends. Invoked from a clash-source card's
     * effect resolution (see {@code ClashEffect}) or a test.
     *
     * @return {@code true} if the clashing player won the clash (their revealed card had a strictly
     *         greater mana value), so callers can apply an "if you won" reward.
     */
    public boolean performClash(GameData gameData, UUID clashingPlayerId) {
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(clashingPlayerId))
                .findFirst().orElse(null);

        Card clashingCard = topCard(gameData, clashingPlayerId);
        Card opponentCard = opponentId != null ? topCard(gameData, opponentId) : null;

        GameLog.Builder clashLog = GameLog.builder()
                .text(gameData.playerIdToName.get(clashingPlayerId) + " clashes: reveals ");
        if (clashingCard != null) {
            clashLog.card(clashingCard);
        } else {
            clashLog.text("no card (empty library)");
        }
        clashLog.text("; opponent reveals ");
        if (opponentCard != null) {
            clashLog.card(opponentCard);
        } else {
            clashLog.text("no card (empty library)");
        }
        clashLog.text(".");
        gameLogService.append(gameData, clashLog.build());

        // 701.29c: win if your revealed card's mana value is higher than each other revealed card.
        boolean won = clashingCard != null
                && (opponentCard == null || clashingCard.getManaValue() > opponentCard.getManaValue());

        String outcome = won
                ? gameData.playerIdToName.get(clashingPlayerId) + " won the clash."
                : "No one won the clash.";
        gameLogService.append(gameData, GameLog.text(outcome));
        log.info("Game {} - {} clashes (won={})", gameData.id, clashingPlayerId, won);

        fireClashTriggers(gameData, clashingPlayerId, won);
        return won;
    }

    private Card topCard(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        return (deck == null || deck.isEmpty()) ? null : deck.getFirst();
    }

    private void fireClashTriggers(GameData gameData, UUID clashingPlayerId, boolean won) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(clashingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_CLASHES);
            if (effects == null || effects.isEmpty()) continue;

            // Resolve win-conditional clauses now: the clash has ended, so the winner is fixed.
            List<CardEffect> resolvedEffects = new ArrayList<>();
            for (CardEffect effect : effects) {
                if (effect instanceof ClashOutcomeConditionalEffect clashOutcome) {
                    if (clashOutcome.appliesOnWin() == won) resolvedEffects.add(clashOutcome.wrapped());
                } else {
                    resolvedEffects.add(effect);
                }
            }
            if (resolvedEffects.isEmpty()) continue;

            // Targeting clash triggers (Entangling Trap) route through the ClashTriggerTarget
            // interaction to pick an opponent's creature; non-targeting ones (Rebellion of the
            // Flamekin) go straight onto the stack as a triggered ability.
            boolean needsTarget = resolvedEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            if (needsTarget) {
                gameData.queueInteraction(new PermanentChoiceContext.ClashTriggerTarget(
                        perm.getCard(), clashingPlayerId, resolvedEffects, perm.getId()));
                log.info("Game {} - {} clash trigger queued", gameData.id, perm.getCard().getName());
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        clashingPlayerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(resolvedEffects),
                        null,
                        perm.getId()));
                log.info("Game {} - {} clash trigger pushed to stack", gameData.id, perm.getCard().getName());
            }
        }

        triggeredAbilityQueueService.processNextClashTriggerTarget(gameData);
    }

    public void processNextClashTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextClashTriggerTarget(gameData);
    }

    // ── Death / leaves-battlefield triggers ───────────────────────────

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature) {
        collectDeathTrigger(gameData, dyingCard, controllerId, wasCreature, null);
    }

    public void collectSpellHauntTrigger(GameData gameData, Card spell, UUID controllerId) {
        List<CardEffect> hauntEffects = spell.getEffects(EffectSlot.ON_DEATH).stream()
                .filter(HauntEffect.class::isInstance)
                .toList();
        if (!hauntEffects.isEmpty()) {
            gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    spell, controllerId, hauntEffects));
        }
    }

    public void checkHauntedCreatureDeathTriggers(GameData gameData, Permanent dyingPermanent) {
        UUID dyingPermanentId = dyingPermanent.getId();
        List<UUID> hauntingCardIds = gameData.hauntingCardToPermanentId.entrySet().stream()
                .filter(entry -> dyingPermanentId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();

        for (UUID hauntingCardId : hauntingCardIds) {
            if (!gameData.hauntingCardToPermanentId.remove(hauntingCardId, dyingPermanentId)) {
                continue;
            }
            ExiledCardEntry exiled = gameData.findExiledCard(hauntingCardId);
            if (exiled == null) {
                continue;
            }

            Card hauntingCard = exiled.card();
            List<CardEffect> effects = hauntingCard.getEffects(EffectSlot.ON_HAUNTED_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) {
                continue;
            }

            List<CardEffect> targetedEffects = new ArrayList<>();
            List<CardEffect> nonTargetedEffects = new ArrayList<>();
            List<TriggeredModalEffect> triggeredModalEffects = new ArrayList<>();
            for (CardEffect effect : effects) {
                if (effect instanceof TriggeredModalEffect triggeredModalEffect) {
                    triggeredModalEffects.add(triggeredModalEffect);
                    continue;
                }
                if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                        || graveyardTargetingSupport.findTarget(List.of(effect)) != null) {
                    targetedEffects.add(effect);
                } else {
                    nonTargetedEffects.add(effect);
                }
            }
            if (!targetedEffects.isEmpty()) {
                gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                        hauntingCard, exiled.ownerId(), targetedEffects));
            }
            if (!nonTargetedEffects.isEmpty()) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        hauntingCard,
                        exiled.ownerId(),
                        hauntingCard.getName() + "'s ability",
                        nonTargetedEffects,
                        null));
                gameLogService.append(gameData, GameLog.abilityTriggers(hauntingCard));
            }
            for (TriggeredModalEffect triggeredModalEffect : triggeredModalEffects) {
                gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        hauntingCard, exiled.ownerId(), triggeredModalEffect.choice(), null));
                gameLogService.append(gameData, GameLog.abilityTriggers(hauntingCard));
            }
        }
    }

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature, Permanent dyingPermanent) {
        collectDeathTrigger(gameData, dyingCard, controllerId, wasCreature, dyingPermanent, null);
    }

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature,
                                    Permanent dyingPermanent, List<CardEffect> precomputedGrantedDeathEffects) {
        int dyingPower = dyingPermanent != null
                ? dyingPermanent.getEffectivePower()
                : dyingCard != null && dyingCard.getPower() != null ? dyingCard.getPower() : 0;
        collectDeathTrigger(gameData, dyingCard, controllerId, wasCreature, dyingPermanent,
                precomputedGrantedDeathEffects, dyingPower);
    }

    public void collectDeathTrigger(GameData gameData, Card dyingCard, UUID controllerId, boolean wasCreature,
                                    Permanent dyingPermanent, List<CardEffect> precomputedGrantedDeathEffects,
                                    int dyingPower) {
        List<CardEffect> deathEffects = dyingPermanent != null && dyingPermanent.isFaceDown()
                ? List.of() : dyingCard.getEffects(EffectSlot.ON_DEATH);

        // Include temporarily granted ON_DEATH effects (e.g. from Verdant Rebirth)
        List<CardEffect> temporaryDeathEffects = dyingPermanent != null
                ? dyingPermanent.getTemporaryTriggeredEffects(EffectSlot.ON_DEATH) : List.of();
        List<CardEffect> persistentDeathEffects = dyingPermanent != null
                ? dyingPermanent.getPersistentTriggeredEffects(EffectSlot.ON_DEATH) : List.of();

        // Include ON_DEATH abilities granted continuously by an attached Aura/Equipment
        // (Infernal Scarring). Read straight off the attachments, not through the layer system:
        // the dying permanent has already left the battlefield, but its Aura is still attached
        // (orphaned Auras only fall off in a later state-based-action pass).
        List<CardEffect> grantedDeathEffects = precomputedGrantedDeathEffects != null
                ? new ArrayList<>(precomputedGrantedDeathEffects)
                : dyingPermanent != null
                ? new ArrayList<>(grantedTriggeredAbilitySupport.grantedTriggeredEffectsFromAttachments(
                        gameData, dyingPermanent.getId(), EffectSlot.ON_DEATH))
                : List.of();

        if (deathEffects.isEmpty() && temporaryDeathEffects.isEmpty()
                && persistentDeathEffects.isEmpty() && grantedDeathEffects.isEmpty()) return;

        var ctx = new TriggerContext.SelfDeath(dyingCard, controllerId, wasCreature, dyingPermanent, dyingPower);
        Permanent perm = dyingPermanent != null ? dyingPermanent : new Permanent(dyingCard);
        UUID dyingCardId = dyingPermanent != null
                ? dyingPermanent.getOriginalCard().getId() : dyingCard.getId();
        for (CardEffect effect : deathEffects) {
            // "When you sacrifice this" effects are collected from the sacrifice path instead.
            if (effect.onlyTriggersOnSacrifice()) continue;
            CardEffect resolvedEffect = unwrapCreatureDeathConditional(effect, dyingCard, dyingPermanent, gameData, controllerId);
            if (resolvedEffect == null) continue;
            if (resolvedEffect instanceof DyingCreatureCardAwareEffect aware && dyingCard != null) {
                resolvedEffect = aware.boundToDyingCard(dyingCardId);
            }
            if (!passesInterveningIf(gameData, perm, controllerId, resolvedEffect)) continue;
            var match = new TriggerMatchContext(gameData, perm, controllerId, resolvedEffect);
            dispatch(match, EffectSlot.ON_DEATH, resolvedEffect, ctx);
        }
        for (CardEffect effect : temporaryDeathEffects) {
            if (effect.onlyTriggersOnSacrifice()) continue;
            CardEffect resolvedEffect = unwrapCreatureDeathConditional(effect, dyingCard, dyingPermanent, gameData, controllerId);
            if (resolvedEffect == null) continue;
            if (resolvedEffect instanceof DyingCreatureCardAwareEffect aware && dyingCard != null) {
                resolvedEffect = aware.boundToDyingCard(dyingCardId);
            }
            if (!passesInterveningIf(gameData, perm, controllerId, resolvedEffect)) continue;
            var match = new TriggerMatchContext(gameData, perm, controllerId, resolvedEffect);
            dispatch(match, EffectSlot.ON_DEATH, resolvedEffect, ctx);
        }
        for (CardEffect effect : persistentDeathEffects) {
            if (effect.onlyTriggersOnSacrifice()) continue;
            CardEffect resolvedEffect = unwrapCreatureDeathConditional(effect, dyingCard, dyingPermanent, gameData, controllerId);
            if (resolvedEffect == null) continue;
            if (!passesInterveningIf(gameData, perm, controllerId, resolvedEffect)) continue;
            var match = new TriggerMatchContext(gameData, perm, controllerId, resolvedEffect);
            dispatch(match, EffectSlot.ON_DEATH, resolvedEffect, ctx);
        }
        for (CardEffect effect : grantedDeathEffects) {
            if (effect.onlyTriggersOnSacrifice()) continue;
            CardEffect resolvedEffect = unwrapCreatureDeathConditional(effect, dyingCard, dyingPermanent, gameData, controllerId);
            if (resolvedEffect == null) continue;
            if (resolvedEffect instanceof DyingCreatureCardAwareEffect aware && dyingCard != null) {
                resolvedEffect = aware.boundToDyingCard(dyingCardId);
            }
            if (!passesInterveningIf(gameData, perm, controllerId, resolvedEffect)) continue;
            var match = new TriggerMatchContext(gameData, perm, controllerId, resolvedEffect);
            dispatch(match, EffectSlot.ON_DEATH, resolvedEffect, ctx);
        }
    }

    public void checkAllyCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId,
                                               Permanent dyingPermanent, int dyingPowerAtDeath) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        Map<UUID, Permanent> sourcesById = new LinkedHashMap<>();
        battlefield.forEach(source -> sourcesById.put(source.getId(), source));
        gameData.simultaneousDyingCreatures.forEach((permanentId, source) -> {
            if (dyingCreatureControllerId.equals(
                    gameData.simultaneousDyingControllers.get(permanentId))) {
                sourcesById.putIfAbsent(permanentId, source);
            }
        });

        Card dyingCard = dyingPermanent.getCard();
        int dyingPower = Math.max(0, dyingPowerAtDeath);
        Map<CounterType, Integer> dyingCounters = snapshotCountersOnPermanent(dyingPermanent);
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId,
                dyingPower, dyingPermanent.getEffectiveToughness(), dyingPermanent.getId(),
                dyingPermanent);

        for (Permanent perm : sourcesById.values()) {
            if (perm.getId().equals(dyingPermanent.getId())) {
                continue;
            }
            List<CardEffect> effects = new ArrayList<>(
                    perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DIES));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, perm, EffectSlot.ON_ALLY_CREATURE_DIES));
            if (effects == null || effects.isEmpty()) continue;

            boolean anyEffectFired = false;
            boolean oncePerTurnFired = false;
            List<CardEffect> stackEffects = new ArrayList<>();

            for (CardEffect effect : effects) {
                CardEffect resolvedEffect = unwrapCreatureDeathConditional(
                        effect, dyingCard, dyingPermanent, gameData, dyingCreatureControllerId, perm);
                if (resolvedEffect == null) continue;
                if (resolvedEffect instanceof BatchedCreatureDeathTriggerEffect) continue;
                if (!passesInterveningIf(gameData, perm, dyingCreatureControllerId, resolvedEffect,
                        dyingPermanent.getId(), dyingPower)) continue;

                boolean oncePerTurn = resolvedEffect instanceof OncePerTurnTriggerEffect;
                resolvedEffect = unwrapOncePerTurnTrigger(gameData, perm, resolvedEffect);
                if (resolvedEffect == null) continue;
                resolvedEffect = snapshotDyingPermanentManaValue(resolvedEffect, dyingCard);
                if (resolvedEffect instanceof DyingCreatureCountersAwareEffect aware) {
                    resolvedEffect = aware.boundToDyingCreatureCounters(dyingCounters);
                }
                if (resolvedEffect instanceof DyingCreatureCardAwareEffect aware
                        && dyingCard != null) {
                    resolvedEffect = aware.boundToDyingCard(dyingCard.getId());
                }
                if (resolvedEffect instanceof GainLifeEqualToDyingCreatureToughnessEffect) {
                    resolvedEffect = new GainLifeEffect(Math.max(0, dyingPermanent.getEffectiveToughness()));
                }

                if (resolvedEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || resolvedEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || resolvedEffect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    // Targeted "another creature you control dies" trigger (e.g. Diregraf Captain):
                    // route through the death target pipeline so the controller picks a target as the
                    // ability is put on the stack (CR 603.3d). The source card here is the watching
                    // permanent, so its own target filter (e.g. opponent-only) is honoured.
                    // The dying creature's last-known power rides along as the event value, so an
                    // amount declared as EventValue resolves to it (Death's Presence — "put X +1/+1
                    // counters on target creature you control, where X is the power of the creature
                    // that died").
                    gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                            perm.getCard(), dyingCreatureControllerId, new ArrayList<>(List.of(resolvedEffect)),
                            dyingPower, new Permanent(perm), dyingCard.getId()
                    ));
                    anyEffectFired = true;
                    if (oncePerTurn) {
                        oncePerTurnFired = true;
                    }
                } else if (resolvedEffect instanceof MayPayManaEffect || resolvedEffect instanceof MayEffect) {
                    var match = new TriggerMatchContext(gameData, perm, dyingCreatureControllerId, resolvedEffect);
                    dispatch(match, EffectSlot.ON_ALLY_CREATURE_DIES, resolvedEffect, ctx);
                    anyEffectFired = true;
                    if (oncePerTurn) {
                        oncePerTurnFired = true;
                    }
                } else {
                    if (resolvedEffect instanceof ReturnTriggeringCardToOwnerHandEffect returnToOwner
                            && returnToOwner.handOwnerId() == null
                            && dyingCard != null) {
                        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCard.getId());
                        if (ownerId != null) {
                            resolvedEffect = new ReturnTriggeringCardToOwnerHandEffect(
                                    dyingCard.getId(), ownerId);
                        }
                    }
                    if (resolvedEffect instanceof DyingCreatureCounterAwareEffect aware) {
                        resolvedEffect = aware.boundToDyingCreatureCounterCount(
                                countCountersOnPermanent(dyingPermanent));
                    }
                    stackEffects.add(resolvedEffect);
                    if (oncePerTurn) {
                        oncePerTurnFired = true;
                    }
                }
            }

            if (!stackEffects.isEmpty()) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        dyingCreatureControllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(stackEffects),
                        null,
                        perm.getId()
                );
                entry.setSourcePermanentSnapshot(new Permanent(perm));
                entry.setTriggeringPermanentId(dyingPermanent.getId());
                entry.setTriggeringPermanentPowerAtTrigger(dyingPower);
                entry.setEventValue(dyingPower);
                int previousCopies = gameData.beginTriggeredAbilityCopies(1
                        + gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(
                                gameData, dyingCreatureControllerId, perm));
                try {
                    gameData.stack.add(entry);
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
                anyEffectFired = true;
            }
            if (oncePerTurnFired) {
                gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
            }

            if (anyEffectFired) {
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers (ally creature died)", gameData.id, perm.getCard().getName());
            }
        }
    }

    private CardEffect snapshotDyingPermanentManaValue(CardEffect effect, Card dyingCard) {
        if (!(effect instanceof ReturnCardFromGraveyardEffect returnEffect)
                || !(returnEffect.dynamicMaxManaValue() instanceof SourceManaValueMinusOne)
                || dyingCard == null) {
            return effect;
        }
        CardPredicate manaValueFilter = new CardMaxManaValuePredicate(dyingCard.getManaValue() - 1);
        CardPredicate filter = returnEffect.filter() == null
                ? manaValueFilter
                : new CardAllOfPredicate(List.of(returnEffect.filter(), manaValueFilter));
        return returnEffect.toBuilder()
                .filter(filter)
                .dynamicMaxManaValue(null)
                .build();
    }

    private void collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;
        if (creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                    EffectSlot.ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source,
                    EffectSlot.ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            if (effects.isEmpty()) continue;

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    creatureControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    effects,
                    null,
                    source.getId()
            ));

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} ally-creature-becomes-target trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    private void collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
            GameData gameData, StackEntry targetedEntry, StackEntry triggeringEntry) {
        if (targetedEntry == null || targetedEntry.getEntryType() != StackEntryType.CREATURE_SPELL) return;

        UUID creatureControllerId = targetedEntry.getControllerId();
        if (creatureControllerId == null || creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(
                    EffectSlot.ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source,
                    EffectSlot.ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY));
            if (effects.isEmpty()) continue;

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    creatureControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    effects,
                    null,
                    source.getId()
            ));

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} ally-creature-spell-becomes-target trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /** Fires creature-death triggers from cards in the dying creature controller's graveyard. */
    public void checkGraveyardAllyCreatureDeathTriggers(GameData gameData,
                                                         UUID dyingCreatureControllerId,
                                                         Permanent dyingPermanent) {
        List<Card> graveyard = gameData.playerGraveyards.get(dyingCreatureControllerId);
        if (graveyard == null) return;

        Card dyingCard = dyingPermanent.getCard();
        for (Card card : new ArrayList<>(graveyard)) {
            if (card.getId().equals(dyingCard.getId())) continue;

            boolean diedAtTheSameTime = gameData.simultaneousDyingCreatures.values().stream()
                    .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
            if (diedAtTheSameTime) continue;

            List<CardEffect> effects = gameQueryService.getEffectiveGraveyardEffects(
                    gameData, card, EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(
                        effect, dyingCard, gameData, dyingCreatureControllerId);
                if (resolved == null) continue;

                if (resolved instanceof MayEffect may) {
                    gameData.queueMayAbility(card, dyingCreatureControllerId, may);
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            dyingCreatureControllerId,
                            card.getName() + "'s ability",
                            new ArrayList<>(List.of(resolved))
                    ));
                }
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard creature-death trigger queued", gameData.id, card.getName());
            }
        }
    }

    /**
     * "Whenever a creature or planeswalker you control dies" ({@link EffectSlot#ON_ALLY_CREATURE_OR_PLANESWALKER_DIES}).
     * Called once per dying permanent that was a creature and/or a planeswalker, so a permanent
     * that is both only triggers once.
     */
    public void checkAllyCreatureOrPlaneswalkerDeathTriggers(GameData gameData, UUID dyingControllerId,
            Permanent dyingPermanent) {
        checkAllyCreatureOrPlaneswalkerDeathTriggers(gameData, dyingControllerId, dyingPermanent,
                dyingPermanent.getCard().hasType(CardType.CREATURE));
    }

    public void checkAllyCreatureOrPlaneswalkerDeathTriggers(GameData gameData, UUID dyingControllerId,
            Permanent dyingPermanent, boolean wasCreature) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingControllerId);
        if (battlefield == null) return;

        Card dyingCard = dyingPermanent.getCard();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingControllerId,
                dyingPermanent.getEffectivePower(), dyingPermanent.getEffectiveToughness(), dyingPermanent.getId(),
                dyingPermanent, wasCreature);

        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_OR_PLANESWALKER_DIES);
            if (effects == null || effects.isEmpty()) continue;

            boolean anyEffectFired = false;
            List<CardEffect> stackEffects = new ArrayList<>();

            for (CardEffect effect : effects) {
                CardEffect resolvedEffect = unwrapCreatureDeathConditional(
                        effect, dyingCard, dyingPermanent, gameData, dyingControllerId);
                if (resolvedEffect == null) continue;

                if (resolvedEffect instanceof MayPayManaEffect || resolvedEffect instanceof MayEffect) {
                    var match = new TriggerMatchContext(gameData, perm, dyingControllerId, resolvedEffect);
                    dispatch(match, EffectSlot.ON_ALLY_CREATURE_OR_PLANESWALKER_DIES, resolvedEffect, ctx);
                    anyEffectFired = true;
                } else {
                    stackEffects.add(resolvedEffect);
                }
            }

            if (!stackEffects.isEmpty()) {
                int previousCopies = gameData.beginTriggeredAbilityCopies(1
                        + (wasCreature
                        ? gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(
                                gameData, dyingControllerId, perm)
                        : 0));
                try {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            dyingControllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(stackEffects),
                            null,
                            perm.getId()
                    ));
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
                anyEffectFired = true;
            }

            if (anyEffectFired) {
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers (ally creature or planeswalker died)", gameData.id, perm.getCard().getName());
            }
        }
    }

    public void checkEquippedCreatureDeathTriggers(GameData gameData, UUID dyingCreatureId,
                                                   UUID dyingCreatureControllerId, Card dyingCard,
                                                   int dyingCreaturePower) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.EquippedCreatureDeath(
                dyingCreatureId, dyingCreatureControllerId, dyingCard, dyingCreaturePower);

        for (Permanent perm : battlefield) {
            if (!dyingCreatureId.equals(perm.getAttachedTo())) continue;
            if (!perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolvedEffect = unwrapTriggeringCardConditional(effect, dyingCard, gameData, dyingCreatureControllerId);
                if (resolvedEffect == null) continue;
                var match = new TriggerMatchContext(gameData, perm, dyingCreatureControllerId, resolvedEffect);
                dispatch(match, EffectSlot.ON_EQUIPPED_CREATURE_DIES, resolvedEffect, ctx);
            }
        }
    }

    public void checkEnchantedPermanentDeathTriggers(GameData gameData, UUID dyingPermanentId,
                                                      UUID dyingPermanentControllerId, UUID dyingCreatureCardId,
                                                      int dyingCreaturePower, int dyingCreatureToughness) {
        checkEnchantedPermanentDeathTriggers(gameData, dyingPermanentId, dyingPermanentControllerId,
                dyingCreatureCardId, dyingCreaturePower, dyingCreatureToughness, true);
    }

    public void checkEnchantedPermanentDeathTriggers(GameData gameData, UUID dyingPermanentId,
                                                      UUID dyingPermanentControllerId, UUID dyingCreatureCardId,
                                                      int dyingCreaturePower, int dyingCreatureToughness,
                                                      boolean wasCreature) {
        var ctx = new TriggerContext.EnchantedPermanentDeath(dyingPermanentId, dyingPermanentControllerId,
                dyingCreatureCardId, dyingCreaturePower, dyingCreatureToughness, wasCreature);

        gameData.forEachPermanent((playerId, perm) -> {
            if (!dyingPermanentId.equals(perm.getAttachedTo())) return;
            if (perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                dispatch(match, EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, effect, ctx);
            }
        });
    }

    public void checkEnchantedPermanentLTBTriggers(GameData gameData, Permanent leavingPermanent,
                                                   UUID leavingControllerId) {
        checkEnchantedPermanentLTBTriggers(gameData, leavingPermanent, leavingControllerId, null);
    }

    public void checkEnchantedPermanentLTBTriggers(GameData gameData, Permanent leavingPermanent,
                                                   UUID leavingControllerId, Zone destination) {
        var ctx = new TriggerContext.EnchantedPermanentLeaves(leavingPermanent, leavingControllerId, destination);

        gameData.forEachPermanent((playerId, perm) -> {
            if (!leavingPermanent.getId().equals(perm.getAttachedTo())) return;
            if (perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                dispatch(match, EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD, effect, ctx);
            }
        });
    }

    public void checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID graveyardOwnerId,
                                                                         UUID artifactControllerId, int artifactManaValue) {
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(graveyardOwnerId, List.of());
        Card artifactCard = graveyard.isEmpty() ? null : graveyard.getLast();
        var ctx = new TriggerContext.ArtifactGraveyard(
                graveyardOwnerId, artifactControllerId, artifactCard, artifactManaValue);

        gameData.forEachPermanent((playerId, perm) -> {
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);

            if (!playerId.equals(graveyardOwnerId)) {
                dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD, ctx);
            }
        });
    }

    public void checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID graveyardOwnerId,
                                                                         UUID artifactControllerId) {
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(graveyardOwnerId, List.of());
        Card artifactCard = graveyard.isEmpty() ? null : graveyard.getLast();
        checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(
                gameData, graveyardOwnerId, artifactControllerId,
                artifactCard == null ? 0 : artifactCard.getManaValue());
    }

    public void checkAnyLandPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID graveyardOwnerId, UUID landControllerId) {
        var ctx = new TriggerContext.AnyLandGraveyard(graveyardOwnerId, landControllerId);

        gameData.forEachPermanent((playerId, perm) ->
                dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx));
    }

    public void checkAnyEnchantmentPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID graveyardOwnerId, UUID enchantmentControllerId) {
        var ctx = new TriggerContext.EnchantmentGraveyard(graveyardOwnerId, enchantmentControllerId);

        gameData.forEachPermanent((playerId, perm) -> {
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
            if (playerId.equals(enchantmentControllerId)) {
                dispatchSlot(gameData, perm, playerId, EffectSlot.ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
            }
        });
    }

    /**
     * Fires ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT triggers (Sacred Ground). Only fires when a
     * spell or ability an opponent of the graveyard owner controls caused the land to be put into the
     * graveyard, and only on permanents the graveyard owner controls.
     */
    public void checkLandPutIntoGraveyardByOpponentTriggers(GameData gameData, Card landCard,
                                                            UUID graveyardOwnerId, UUID causeControllerId) {
        if (causeControllerId == null || causeControllerId.equals(graveyardOwnerId)) return;

        var ctx = new TriggerContext.LandPutIntoGraveyard(landCard, graveyardOwnerId, causeControllerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, graveyardOwnerId, EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT, ctx);
        }
    }

    /** Fires "whenever you win a coin flip" triggers for the winning player's battlefield. */
    public void checkControllerWinsCoinFlipTriggers(GameData gameData, UUID winningPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(winningPlayerId);
        if (battlefield == null) return;

        TriggerContext ctx = new TriggerContext.CoinFlipWon(winningPlayerId);
        for (Permanent perm : List.copyOf(battlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_WINS_COIN_FLIP)) {
                var match = new TriggerMatchContext(gameData, perm, winningPlayerId, effect);
                registry.dispatch(match, EffectSlot.ON_CONTROLLER_WINS_COIN_FLIP, effect, ctx);
            }
        }
    }

    /**
     * Fires Karmic Justice triggers for a noncreature permanent actually destroyed while resolving
     * an opponent's spell or ability. The destroyed permanent is also checked as a source because
     * its triggered ability still triggers when it is destroyed by that spell or ability.
     */
    public void checkNoncreaturePermanentDestroyedByOpponentTriggers(GameData gameData,
                                                                      Permanent destroyedPermanent,
                                                                      UUID destroyedControllerId,
                                                                      UUID causeControllerId) {
        if (causeControllerId == null || causeControllerId.equals(destroyedControllerId)) return;

        var ctx = new TriggerContext.NoncreaturePermanentDestroyed(
                destroyedPermanent.getCard(), destroyedControllerId, causeControllerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(destroyedControllerId);
        if (battlefield != null) {
            for (Permanent perm : List.copyOf(battlefield)) {
                dispatchSlot(gameData, perm, destroyedControllerId,
                        EffectSlot.ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT, ctx);
            }
        }
        if (!destroyedPermanent.getCard().getEffects(
                EffectSlot.ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT).isEmpty()) {
            dispatchSlot(gameData, destroyedPermanent, destroyedControllerId,
                    EffectSlot.ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT, ctx);
        }
    }

    /**
     * Fires ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE triggers (Countryside Crusher) whenever a
     * land card is put into its owner's graveyard from any zone. Fires on every permanent the graveyard
     * owner controls that has this slot.
     */
    public void checkLandPutIntoGraveyardFromAnywhereTriggers(GameData gameData, UUID graveyardOwnerId, Card landCard) {
        var ctx = new TriggerContext.LandPutIntoGraveyard(landCard, graveyardOwnerId, null);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, graveyardOwnerId, EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, ctx);
        }
    }

    /** Fires library-origin land-card triggers after a land has actually entered the graveyard. */
    public void checkLandCardMilledTriggers(GameData gameData, UUID graveyardOwnerId, Card landCard) {
        var ctx = new TriggerContext.LandCardMilled(landCard, graveyardOwnerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, graveyardOwnerId, EffectSlot.ON_ALLY_LAND_CARD_MILLED, ctx);
        }
    }

    /**
     * Fires controller-graveyard triggers whenever a non-token card enters the controller's
     * graveyard from any zone.
     */
    public void checkCardPutIntoGraveyardFromAnywhereTriggers(GameData gameData, UUID graveyardOwnerId,
                                                               Card card) {
        var ctx = new TriggerContext.CardPutIntoGraveyard(card, graveyardOwnerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, graveyardOwnerId,
                    EffectSlot.ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, ctx);
        }
    }

    /**
     * Fires ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE triggers (Soulcipher Board) whenever
     * a creature card is put into its owner's graveyard from any zone. Uses printed card types (not
     * battlefield creature-ness); callers must already exclude tokens. Fires on every permanent the
     * graveyard owner controls that has this slot, and on every permanent an opponent of the
     * graveyard owner controls that has ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE
     * (Profane Memento).
     */
    public void checkCreatureCardPutIntoGraveyardFromAnywhereTriggers(GameData gameData, UUID graveyardOwnerId,
            Card creatureCard) {
        var ctx = new TriggerContext.CreatureCardPutIntoGraveyard(creatureCard, graveyardOwnerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield != null) {
            for (Permanent perm : List.copyOf(battlefield)) {
                dispatchSlot(gameData, perm, graveyardOwnerId,
                        EffectSlot.ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, ctx);
            }
        }

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(graveyardOwnerId)) return;
            dispatchSlot(gameData, perm, playerId,
                    EffectSlot.ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE, ctx);
        });
    }

    /**
     * Fires global creature-card graveyard triggers for cards entering a graveyard from a
     * non-battlefield zone.
     */
    public void checkCreatureCardPutIntoGraveyardFromNonBattlefieldTriggers(GameData gameData,
            UUID graveyardOwnerId, Card creatureCard) {
        var ctx = new TriggerContext.CreatureCardPutIntoGraveyard(creatureCard, graveyardOwnerId);
        gameData.forEachPermanent((playerId, perm) -> dispatchSlot(gameData, perm, playerId,
                EffectSlot.ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD, ctx));
    }

    /**
     * Fires opponent-graveyard triggers whenever a non-token card enters an opponent's graveyard
     * from any zone. The graveyard owner is preserved as the trigger's target context.
     */
    public void checkCardPutIntoOpponentGraveyardFromAnywhereTriggers(GameData gameData,
            UUID graveyardOwnerId, Card card) {
        var ctx = new TriggerContext.CardPutIntoGraveyard(card, graveyardOwnerId);
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(graveyardOwnerId)) return;
            dispatchSlot(gameData, perm, playerId,
                    EffectSlot.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE, ctx);
        });

        for (ExiledCardEntry exiledEntry : new ArrayList<>(gameData.exiledCards)) {
            UUID ownerId = exiledEntry.ownerId();
            if (ownerId.equals(graveyardOwnerId)) continue;
            Integer timeCounters = gameData.exiledCardTimeCounters.get(exiledEntry.card().getId());
            if (timeCounters == null || timeCounters <= 0) continue;

            Card sourceCard = exiledEntry.card();
            for (CardEffect effect : sourceCard.getEffects(
                    EffectSlot.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)) {
                registry.dispatch(new TriggerMatchContext(gameData, null, ownerId, effect, sourceCard),
                        EffectSlot.ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE, effect, ctx);
            }
        }
    }

    public void checkPermanentCardPutIntoGraveyardFromAnywhereTriggers(GameData gameData,
            UUID graveyardOwnerId, Card permanentCard) {
        var ctx = new TriggerContext.PermanentCardPutIntoGraveyard(permanentCard, graveyardOwnerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, graveyardOwnerId,
                    EffectSlot.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, ctx);
        }
    }

    /**
     * Fires ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE triggers (Compost). The card has
     * already entered the graveyard; fires on permanents controlled by an opponent of the graveyard
     * owner when the card is black.
     */
    public void checkBlackCardPutIntoOpponentGraveyardFromAnywhereTriggers(GameData gameData, UUID graveyardOwnerId, Card card) {
        if (card.getColors() == null || !card.getColors().contains(CardColor.BLACK)) return;

        var ctx = new TriggerContext.BlackCardOpponentGraveyard(graveyardOwnerId, card);

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(graveyardOwnerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE, ctx);
        });
    }

    /**
     * Fires ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers (Prince of Thralls)
     * whenever a permanent of any type is put into a graveyard from the battlefield. Fires on
     * permanents controlled by an opponent of the dying permanent's controller.
     */
    public void checkOpponentPermanentPutIntoGraveyardTriggers(GameData gameData, Card dyingCard,
                                                               UUID dyingControllerId, UUID graveyardOwnerId) {
        var ctx = new TriggerContext.OpponentPermanentGraveyard(dyingCard, dyingControllerId, graveyardOwnerId);

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(dyingControllerId)) return;
            dispatchSlot(gameData, perm, playerId, EffectSlot.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
        });
    }

    /**
     * Fires ON_OTHER_PLAYER_OWNED_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers
     * (Kothophed, Soul Hoarder). Ownership-based: only watchers controlled by a player other than the
     * dying permanent's owner see it.
     */
     public void checkOtherPlayerOwnedPermanentPutIntoGraveyardTriggers(GameData gameData, Card dyingCard, UUID ownerId) {
        var ctx = new TriggerContext.OtherPlayerOwnedPermanentGraveyard(dyingCard, ownerId);

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(ownerId)) return;
             dispatchSlot(gameData, perm, playerId,
                     EffectSlot.ON_OTHER_PLAYER_OWNED_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
        });
    }

    /**
     * Fires ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD triggers (Yomiji, Who Bars the Way)
     * whenever a permanent of any type is put into a graveyard from the battlefield. Fires on every
     * permanent still on the battlefield, and on last-known creature permanents dying in the same
     * simultaneous event. The dead permanent never sees its own death. Supports
     * {@code TriggeringCardConditionalEffect} gating on the dead permanent's card.
     */
    public void checkAnyPermanentPutIntoGraveyardTriggers(GameData gameData, Card dyingCard,
                                                          UUID dyingControllerId, UUID graveyardOwnerId) {
        checkAnyPermanentPutIntoGraveyardTriggers(
                gameData, new Permanent(dyingCard), dyingControllerId, graveyardOwnerId);
    }

    public void checkAnyPermanentPutIntoGraveyardTriggers(GameData gameData, Permanent dyingPermanent,
                                                          UUID dyingControllerId, UUID graveyardOwnerId) {
        Card dyingCard = dyingPermanent.getOriginalCard();
        var ctx = new TriggerContext.AnyPermanentGraveyard(dyingCard, dyingControllerId, graveyardOwnerId);

        gameData.forEachPermanent((playerId, perm) -> {
            dispatchAnyPermanentDeathTriggersForWatcher(
                    gameData, playerId, perm, dyingCard, dyingPermanent, ctx);
        });

        for (Map.Entry<UUID, Permanent> entry : gameData.simultaneousDyingCreatures.entrySet()) {
            UUID watcherId = entry.getKey();
            Permanent watcher = entry.getValue();
            if (watcher.getCard().getId().equals(dyingCard.getId())) continue;
            if (gameQueryService.findPermanentById(gameData, watcherId) != null) continue;

            UUID watcherControllerId = gameData.simultaneousDyingControllers.get(watcherId);
            if (watcherControllerId != null) {
                dispatchAnyPermanentDeathTriggersForWatcher(
                        gameData, watcherControllerId, watcher, dyingCard, dyingPermanent, ctx);
            }
        }

        gameData.forEachPermanent((playerId, perm) -> {

            if (!dyingCard.isToken()) {
                dispatchSlot(gameData, perm, playerId,
                        EffectSlot.ON_ANY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
            }

            if (playerId.equals(graveyardOwnerId) && !dyingCard.isToken()) {
                for (CardEffect effect : perm.getCard().getEffects(
                        EffectSlot.ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)) {
                    CardEffect resolved = unwrapTriggeringCardConditional(
                            effect, dyingCard, gameData, playerId);
                    if (resolved == null) continue;
                    var match = new TriggerMatchContext(gameData, perm, playerId, resolved);
                    dispatch(match,
                            EffectSlot.ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                            resolved, ctx);
                }
            }

            if (playerId.equals(graveyardOwnerId)) {
                for (CardEffect effect : perm.getCard().getEffects(
                        EffectSlot.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)) {
                    CardEffect resolved = unwrapTriggeringCardConditional(
                            effect, dyingCard, gameData, playerId);
                    if (resolved == null) continue;
                    var match = new TriggerMatchContext(gameData, perm, playerId, resolved);
                    dispatch(match,
                            EffectSlot.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                            resolved, ctx);
                }
            }

            if (playerId.equals(graveyardOwnerId)) return;
             dispatchSlot(gameData, perm, playerId,
                     EffectSlot.ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD, ctx);
        });
    }

    public void checkCreaturePutIntoOwnersGraveyardFromBattlefieldTriggers(
            GameData gameData, Card dyingCard, UUID graveyardOwnerId, UUID dyingControllerId) {
        var ctx = new TriggerContext.AnyPermanentGraveyard(
                dyingCard, dyingControllerId, graveyardOwnerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                dispatchSlot(gameData, permanent, graveyardOwnerId,
                        EffectSlot.ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD, ctx);
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
        if (graveyard == null) {
            return;
        }

        for (Card card : new ArrayList<>(graveyard)) {
            if (card.getId().equals(dyingCard.getId())) {
                continue;
            }
            boolean diedAtTheSameTime = gameData.simultaneousDyingCreatures.values().stream()
                    .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
            if (diedAtTheSameTime) {
                continue;
            }
            for (CardEffect effect : gameQueryService.getEffectiveGraveyardEffects(
                    gameData, card, EffectSlot.GRAVEYARD_ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD)) {
                queueGraveyardCreaturePutTrigger(gameData, card, graveyardOwnerId, effect,
                        dyingCard);
            }
        }

        for (UUID opponentId : gameData.playerGraveyards.keySet()) {
            if (opponentId.equals(graveyardOwnerId)) {
                continue;
            }
            List<Card> opponentGraveyard = gameData.playerGraveyards.get(opponentId);
            if (opponentGraveyard == null) {
                continue;
            }
            for (Card card : new ArrayList<>(opponentGraveyard)) {
                if (card.getId().equals(dyingCard.getId())) {
                    continue;
                }
                boolean diedAtTheSameTime = gameData.simultaneousDyingCreatures.values().stream()
                        .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
                if (diedAtTheSameTime) {
                    continue;
                }
                for (CardEffect effect : gameQueryService.getEffectiveGraveyardEffects(
                        gameData, card, EffectSlot.GRAVEYARD_ON_CREATURE_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)) {
                    queueGraveyardCreaturePutTrigger(gameData, card, opponentId, effect, dyingCard);
                }
            }
        }
    }

    private void queueGraveyardCreaturePutTrigger(GameData gameData, Card sourceCard, UUID controllerId,
                                                   CardEffect effect, Card dyingCard) {
        CardEffect resolved = unwrapTriggeringCardConditional(effect, dyingCard, gameData, controllerId);
        if (resolved == null) {
            return;
        }
        if (resolved instanceof MayPayManaEffect mayPay) {
            gameData.queueMayAbility(sourceCard, controllerId, mayPay, null);
        } else if (resolved instanceof MayEffect may) {
            gameData.queueMayAbility(sourceCard, controllerId, may);
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    controllerId,
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(resolved))
            ));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} graveyard trigger fires when a creature enters a graveyard",
                gameData.id, sourceCard.getName());
    }

    /** Fires "whenever you lose a coin flip" triggers for the losing player's battlefield. */
    public void checkControllerLosesCoinFlipTriggers(GameData gameData, UUID losingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(losingPlayerId);
        if (battlefield == null) return;

        TriggerContext ctx = new TriggerContext.CoinFlipLost(losingPlayerId);
        for (Permanent perm : List.copyOf(battlefield)) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_CONTROLLER_LOSES_COIN_FLIP)) {
                var match = new TriggerMatchContext(gameData, perm, losingPlayerId, effect);
                registry.dispatch(match, EffectSlot.ON_CONTROLLER_LOSES_COIN_FLIP, effect, ctx);
            }
        }
    }

    public void checkAnyCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId, Permanent dyingPermanent) {
        Card dyingCard = dyingPermanent.getCard();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId,
                dyingPermanent.getEffectivePower(), dyingPermanent.getEffectiveToughness(), dyingPermanent.getId(),
                dyingPermanent);

        gameData.forEachPermanent((playerId, perm) -> {
            dispatchAnyCreatureDeathTriggersForWatcher(
                    gameData, playerId, perm, dyingPermanent, dyingCreatureControllerId, ctx, false);
        });

        // Last-known watchers dying in the same simultaneous event (CR 603.6c / 603.10). Skip self
        // ("other creatures") and skip watchers still on the battlefield (already handled above).
        for (Map.Entry<UUID, Permanent> entry : gameData.simultaneousDyingCreatures.entrySet()) {
            UUID watcherId = entry.getKey();
            if (watcherId.equals(dyingPermanent.getId())) continue;
            if (gameQueryService.findPermanentById(gameData, watcherId) != null) continue;
            Permanent watcher = entry.getValue();
            UUID controllerId = gameData.simultaneousDyingControllers.get(watcherId);
            if (controllerId == null) continue;
            dispatchAnyCreatureDeathTriggersForWatcher(
                    gameData, controllerId, watcher, dyingPermanent, dyingCreatureControllerId, ctx, true);
        }

        collectTemporaryGlobalTriggers(gameData, EffectSlot.ON_ANY_CREATURE_DIES,
                dyingCreatureControllerId, Math.max(0, ctx.dyingCreatureToughness()));

        collectEmblemCreatureDeathTriggers(gameData, dyingCard, ctx);
        collectCreatureDeathTriggerWatchers(gameData);
    }

    /** Fires effects that care when the given player foretells a card. */
    public void checkControllerForetellTriggers(GameData gameData, UUID foretellingPlayerId,
                                                Card foretoldCard) {
        if (foretellingPlayerId == null || foretoldCard == null) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(foretellingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.Foretell(foretellingPlayerId, foretoldCard);
        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchSlot(gameData, perm, foretellingPlayerId, EffectSlot.ON_CONTROLLER_FORETELLS, ctx);
        }
    }

    private void collectCreatureDeathTriggerWatchers(GameData gameData) {
        for (CreatureDeathTriggerWatcher watcher : List.copyOf(gameData.creatureDeathTriggerWatchers)) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    (UUID) null);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers because a creature died",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /**
     * Emblem creature-death triggers — Liliana, Defiant Necromancer's "Whenever a creature dies,
     * return it to the battlefield under your control at the beginning of the next end step."
     * Emblems live outside the battlefield, so the permanent sweep above never sees them.
     */
    private void collectEmblemCreatureDeathTriggers(GameData gameData, Card dyingCard,
                                                     TriggerContext.CreatureDeath context) {
        for (Emblem emblem : gameData.emblems) {
            for (CardEffect effect : emblem.staticEffects()) {
                int previousCopies = gameData.beginTriggeredAbilityCopies(1 +
                        gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggersForEmblem(
                                gameData, emblem.controllerId()));
                try {
                    if (effect instanceof EmblemCreatureDeathTriggerEffect) {
                        var match = new EmblemTriggerMatchContext(
                                gameData, emblem, emblem.controllerId(), dyingCard, effect);
                        registry.dispatchEmblem(match, effect, context);
                        continue;
                    }
                    if (!(effect instanceof RegisterDelayedReturnDyingCreatureUnderControlEffect delayedReturn)) {
                        continue;
                    }
                    Card source = emblem.sourceCard();
                    String desc = (source != null ? source.getName() : "Emblem") + "'s emblem";
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source != null ? source : dyingCard,
                            emblem.controllerId(),
                            desc,
                            new ArrayList<>(List.of(delayedReturn))
                    );
                    entry.setTriggeringCardId(dyingCard.getId());
                    gameData.stack.add(entry);
                    gameLogService.append(gameData, GameLog.text(desc + " triggers (a creature died)."));
                    log.info("Game {} - {} schedules a delayed return of {}", gameData.id, desc, dyingCard.getName());
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
            }
        }
    }

    private void dispatchAnyCreatureDeathTriggersForWatcher(GameData gameData, UUID playerId, Permanent perm,
            Permanent dyingPermanent, UUID dyingCreatureControllerId, TriggerContext.CreatureDeath ctx,
            boolean lastKnownWatcher) {
        List<CardEffect> effects = new ArrayList<>(perm.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_DIES));
        effects.addAll(lastKnownWatcher
                ? gameData.simultaneousDyingGrantedCreatureDeathEffects.getOrDefault(
                        perm.getId(), List.of())
                : grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                        gameData, perm, EffectSlot.ON_ANY_CREATURE_DIES));
        if (effects.isEmpty()) return;

        Card dyingCard = dyingPermanent.getCard();
        for (CardEffect effect : effects) {
            CardEffect toResolve = effect;
            if (effect instanceof OncePerTurnTriggerEffect once) {
                if (gameData.oncePerTurnTriggersFiredThisTurn.contains(perm.getId())) {
                    continue;
                }
                toResolve = once.wrapped();
            }
            // Death conditionals may reference the dying creature's on-battlefield state (e.g.
            // Blowfly Infestation's "if it had a -1/-1 counter on it") — evaluate against the
            // dying permanent, not just its card.
            CardEffect resolvedEffect = unwrapCreatureDeathConditional(
                    toResolve, dyingCard, dyingPermanent, gameData, playerId, perm);
            if (resolvedEffect == null) continue;
            var match = new TriggerMatchContext(gameData, perm, playerId, resolvedEffect);
            if (dispatch(match, EffectSlot.ON_ANY_CREATURE_DIES, resolvedEffect, ctx)
                    && effect instanceof OncePerTurnTriggerEffect) {
                gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
            }
        }
    }

    public void checkAllyNontokenCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId,
                                                       Permanent dyingPermanent, int dyingPowerAtDeath) {
        Card dyingCard = dyingPermanent.getCard();
        if (dyingCard.isToken()) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingCreatureControllerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId,
                dyingPowerAtDeath, dyingPermanent.getEffectiveToughness(), dyingPermanent.getId(), dyingPermanent);

        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, dyingCreatureControllerId, EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, ctx);
        }
    }

    public void checkAnyNontokenCreatureDeathTriggers(GameData gameData, Card dyingCard) {
        if (dyingCard.isToken()) return;

        var ctx = new TriggerContext.CreatureDeath(dyingCard, null,
                dyingCard.getPower() != null ? dyingCard.getPower() : 0,
                dyingCard.getToughness() != null ? dyingCard.getToughness() : 0);

        gameData.forEachPermanent((playerId, perm) -> dispatchSlot(
                gameData, perm, playerId, EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES, ctx));
    }

    private void dispatchAnyPermanentDeathTriggersForWatcher(GameData gameData, UUID watcherControllerId,
                                                              Permanent watcher, Card dyingCard,
                                                              Permanent dyingPermanent,
                                                              TriggerContext.AnyPermanentGraveyard ctx) {
        if (watcher.isLosesAllAbilitiesUntilEndOfTurn()) return;
        if (watcher.getCard().getId().equals(dyingCard.getId())) return;
        for (CardEffect effect : watcher.getCard()
                .getEffects(EffectSlot.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)) {
            CardEffect resolved = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, watcher, effect);
            if (resolved == null) continue;
            resolved = unwrapCreatureDeathConditional(
                    resolved, dyingCard, dyingPermanent, gameData, watcherControllerId, watcher);
            if (resolved == null) continue;
            var match = new TriggerMatchContext(gameData, watcher, watcherControllerId, resolved);
            boolean triggered = dispatch(match,
                    EffectSlot.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, resolved, ctx);
            if (triggered) {
                OncePerTurnTriggerSupport.markIfNeeded(gameData, watcher, effect);
            }
        }
    }

    public void checkOpponentCreatureDeathTriggers(GameData gameData, UUID dyingCreatureControllerId, Permanent dyingPermanent) {
        Card dyingCard = dyingPermanent.getCard();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId,
                dyingPermanent.getEffectivePower(), dyingPermanent.getEffectiveToughness(), dyingPermanent.getId());

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(dyingCreatureControllerId)) return;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
            List<CardEffect> effects = new ArrayList<>();
            List<CardEffect> ownEffects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES);
            if (ownEffects != null) effects.addAll(ownEffects);
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, perm, EffectSlot.ON_OPPONENT_CREATURE_DIES));
            if (effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                // Death conditionals may reference the dying creature's on-battlefield state (e.g.
                // Necroskitter's "with a -1/-1 counter on it") — evaluate against the dying permanent.
                CardEffect resolvedEffect = unwrapCreatureDeathConditional(
                        effect, dyingCard, dyingPermanent, gameData, dyingCreatureControllerId);
                if (resolvedEffect == null) continue;
                var match = new TriggerMatchContext(gameData, perm, playerId, resolvedEffect);
                dispatch(match, EffectSlot.ON_OPPONENT_CREATURE_DIES, resolvedEffect, ctx);
            }
        });
    }

    public void checkSelfLeavesTriggered(GameData gameData, Permanent target, UUID controllerId) {
        checkSelfLeavesTriggered(gameData, target, controllerId, Zone.GRAVEYARD);
    }

    public void checkSelfLeavesTriggered(GameData gameData, Permanent target, UUID controllerId,
                                         Zone destination) {
        checkSelfLeavesTriggered(gameData, target, controllerId, destination, false);
    }

    public void checkSelfLeavesTriggered(GameData gameData, Permanent target, UUID controllerId,
                                         Zone destination, boolean exiledWhileActivatingCraftAbility) {
        List<CardEffect> cardEffects = target.getCard().getEffects(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD);
        List<CardEffect> effects = new ArrayList<>(cardEffects == null ? List.of() : cardEffects);
        effects.addAll(target.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD));
        effects.addAll(target.getPersistentTriggeredEffects(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD));
        if (effects.isEmpty()) return;

        var ctx = new TriggerContext.SelfLeaves(controllerId, destination, exiledWhileActivatingCraftAbility);

        for (CardEffect effect : effects) {
            var match = new TriggerMatchContext(gameData, target, controllerId, effect);
            dispatch(match, EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, effect, ctx);
        }
    }

    /**
     * Fires delayed "when that creature leaves the battlefield this turn, sacrifice this creature"
     * triggers (Kjeldoran Elite Guard). Drains matching {@link DelayedSacrificeSourceWhenTargetLeaves}
     * entries and enqueues a {@link SacrificeSelfEffect} for each. Called from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedSacrificeSourceWhenTargetLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedSacrificeSourceWhenTargetLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedSacrificeSourceWhenTargetLeaves delayed : gameData.drainDelayedActions(
                DelayedSacrificeSourceWhenTargetLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            StackEntry se = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    null,
                    delayed.sourcePermanentId());
            se.setNonTargeting(true);
            gameData.enqueueTrigger(se);
            gameLogService.append(gameData,
                    GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
            log.info("Game {} - {} delayed leave-trigger fires (watched {} left); sacrifice source {}",
                    gameData.id, delayed.sourceCard().getName(), leavingPermanent.getCard().getName(),
                    delayed.sourcePermanentId());
        }
    }

    /**
     * Fires delayed "when this creature leaves the battlefield this turn, sacrifice that creature"
     * triggers (Phantasmal Mount). Drains matching {@link DelayedSacrificeTargetWhenSourceLeaves}
     * entries and enqueues a {@link SacrificeSelfEffect} against the pumped target for each. Called
     * from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedSacrificeTargetWhenSourceLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedSacrificeTargetWhenSourceLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedSacrificeTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedSacrificeTargetWhenSourceLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            StackEntry se = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    null,
                    delayed.targetPermanentId());
            se.setNonTargeting(true);
            gameData.enqueueTrigger(se);
            gameLogService.append(gameData,
                    GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
            log.info("Game {} - {} delayed leave-trigger fires (source {} left); sacrifice target {}",
                    gameData.id, delayed.sourceCard().getName(), leavingPermanent.getCard().getName(),
                    delayed.targetPermanentId());
        }
    }

    /**
     * Fires delayed "when this artifact leaves the battlefield this turn, destroy that creature"
     * triggers (War Barge). Drains matching registrations and enqueues a non-targeting destruction
     * for each captured target. Called from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedDestroyTargetWhenSourceLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedDestroyTargetWhenSourceLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedDestroyTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedDestroyTargetWhenSourceLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            StackEntry se = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new DestroyLinkedPermanentEffect(true, delayed.targetPermanentId()))),
                    null,
                    List.of());
            se.setNonTargeting(true);
            gameData.enqueueTrigger(se);
            gameLogService.append(gameData,
                    GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
            log.info("Game {} - {} delayed leave-trigger fires (source {} left); destroy target {}",
                    gameData.id, delayed.sourceCard().getName(), leavingPermanent.getCard().getName(),
                    delayed.targetPermanentId());
        }
    }

    /**
     * "Whenever another creature leaves the battlefield" triggers (e.g. Extractor Demon). Called from
     * every leave-the-battlefield path in {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}
     * (graveyard, hand, exile, library) after the permanent has been removed. Global watcher: fires on
     * every permanent with {@link EffectSlot#ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD}, on any battlefield,
     * except the leaving creature itself ("another"). No-op unless the leaving permanent was a creature
     * (last-known information, captured before removal). Queues a non-targeting triggered ability whose
     * {@code sourcePermanentId} is the watching permanent; any player targeting for a wrapped
     * {@code MayEffect} happens at resolution.
     */
    public void checkAnotherCreatureLeavesBattlefieldTriggers(GameData gameData, Permanent leavingPermanent, boolean wasCreature) {
        if (!wasCreature) return;
        UUID leavingId = leavingPermanent.getId();

        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.getId().equals(leavingId)) return;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD)) {
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect) {
                    resolved = resolveTriggeringPermanentConditional(gameData, perm, ownerId, leavingPermanent, effect);
                    if (resolved == null) continue;
                }
                if (effect instanceof LeavingCreatureNameAwareEffect aware) {
                    if (leavingPermanent.getCard().isToken()) continue;
                    resolved = aware.boundToLeavingCreatureName(leavingPermanent.getCard().getName());
                }
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another creature leaving the battlefield ({})",
                        gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
            }
        });
    }

    /**
     * "Whenever another permanent leaves the battlefield" triggers. Called from every
     * leave-the-battlefield path after the permanent has been removed.
     */
    public void checkAnotherPermanentLeavesBattlefieldTriggers(GameData gameData, Permanent leavingPermanent) {
        UUID leavingId = leavingPermanent.getId();

        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.getId().equals(leavingId)) return;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD)) {
                CardEffect resolved = effect instanceof LeavingPermanentIdAwareEffect aware
                        ? aware.boundToLeavingPermanentId(leavingId)
                        : effect;
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another permanent leaving the battlefield ({})",
                        gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
            }
        });
    }

    /**
     * "Whenever another creature you control leaves the battlefield" triggers (e.g. Luminous Phantom).
     * Controller-scoped sibling of {@link #checkAnotherCreatureLeavesBattlefieldTriggers}.
     */
    public void checkAllyCreatureLeavesBattlefieldTriggers(GameData gameData, Permanent leavingPermanent,
                                                           boolean wasCreature, UUID controllerId) {
        if (!wasCreature || controllerId == null) return;
        UUID leavingId = leavingPermanent.getId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (perm.getId().equals(leavingId)) continue;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_LEAVES_BATTLEFIELD)) {
                CardEffect resolved = resolveTriggeringPermanentConditional(
                        gameData, perm, controllerId, leavingPermanent, effect);
                if (resolved == null) continue;
                if (resolved instanceof ConditionalEffect conditional
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forStaticEffect(perm, controllerId))) {
                    continue;
                }
                if (resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || resolved.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            perm.getCard(), controllerId, new ArrayList<>(List.of(resolved)), false,
                            perm.getCard().getTargetFilter(), 0, perm.getId()));
                    gameLogService.append(gameData, GameLog.text(
                            perm.getCard().getName() + "'s ability triggers — choose a target."));
                    continue;
                }
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another creature you control leaving the battlefield ({})",
                        gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
            }
        }
    }

    public void checkAllyCreatureLeavesBattlefieldWithoutDyingTriggers(
            GameData gameData, Permanent leavingPermanent, boolean wasCreature, UUID controllerId) {
        if (!wasCreature || controllerId == null) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : battlefield) {
            if (source.getId().equals(leavingPermanent.getId()) || source.isLosesAllAbilitiesUntilEndOfTurn()) {
                continue;
            }
            enqueueCreatureLeavesBattlefieldWithoutDyingTriggers(
                    gameData, source, controllerId,
                    EffectSlot.ON_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING);
        }
    }

    public void checkAllyCreatureLeavesBattlefieldWithoutDyingBatchTriggers(
            GameData gameData, Map<UUID, Permanent> watchers, Map<UUID, UUID> watcherControllers,
            Map<UUID, UUID> leavingCreatures) {
        if (leavingCreatures.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, Permanent> watcherEntry : watchers.entrySet()) {
            UUID watcherId = watcherEntry.getKey();
            UUID controllerId = watcherControllers.get(watcherId);
            if (controllerId == null || leavingCreatures.entrySet().stream().noneMatch(
                    leaving -> controllerId.equals(leaving.getValue()) && !watcherId.equals(leaving.getKey()))) {
                continue;
            }
            Permanent source = watcherEntry.getValue();
            if (!source.isLosesAllAbilitiesUntilEndOfTurn()) {
                enqueueCreatureLeavesBattlefieldWithoutDyingTriggers(
                        gameData, source, controllerId,
                        EffectSlot.ON_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING);
            }
        }
    }

    public void checkSelfOrAllyCreatureLeavesBattlefieldWithoutDyingTriggers(
            GameData gameData, Permanent leavingPermanent, boolean wasCreature, UUID controllerId) {
        if (!wasCreature || controllerId == null) {
            return;
        }
        if (!leavingPermanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            enqueueCreatureLeavesBattlefieldWithoutDyingTriggers(
                    gameData, leavingPermanent, controllerId,
                    EffectSlot.ON_SELF_OR_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING);
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : battlefield) {
            if (!source.isLosesAllAbilitiesUntilEndOfTurn()) {
                enqueueCreatureLeavesBattlefieldWithoutDyingTriggers(
                        gameData, source, controllerId,
                        EffectSlot.ON_SELF_OR_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING);
            }
        }
    }

    public void checkSelfOrAllyCreatureLeavesBattlefieldWithoutDyingBatchTriggers(
            GameData gameData, Map<UUID, Permanent> watchers, Map<UUID, UUID> watcherControllers,
            Map<UUID, UUID> leavingCreatures) {
        if (leavingCreatures.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, Permanent> watcherEntry : watchers.entrySet()) {
            UUID controllerId = watcherControllers.get(watcherEntry.getKey());
            if (controllerId == null || leavingCreatures.values().stream().noneMatch(controllerId::equals)) {
                continue;
            }
            Permanent source = watcherEntry.getValue();
            if (!source.isLosesAllAbilitiesUntilEndOfTurn()) {
                enqueueCreatureLeavesBattlefieldWithoutDyingTriggers(
                        gameData, source, controllerId,
                        EffectSlot.ON_SELF_OR_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING);
            }
        }
    }

    private void enqueueCreatureLeavesBattlefieldWithoutDyingTriggers(
            GameData gameData, Permanent source, UUID controllerId, EffectSlot slot) {
        for (CardEffect effect : source.getCard().getEffects(slot)) {
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        source.getCard(), controllerId, new ArrayList<>(List.of(effect))));
            } else {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                        source.getCard().getName() + "'s ability", new ArrayList<>(List.of(effect)),
                        null, source.getId()));
            }
            gameLogService.append(gameData, GameLog.text(source.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on a creature leaving without dying",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * "Whenever another permanent you control leaves the battlefield during your turn" triggers.
     */
    public void checkAllyPermanentLeavesBattlefieldDuringControllerTurnTriggers(
            GameData gameData, Permanent leavingPermanent, UUID controllerId) {
        if (controllerId == null || !controllerId.equals(gameData.activePlayerId)) return;
        UUID leavingId = leavingPermanent.getId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (perm.getId().equals(leavingId)) continue;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            for (CardEffect effect : perm.getCard().getEffects(
                    EffectSlot.ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD_DURING_CONTROLLER_TURN)) {
                boolean oncePerTurn = effect instanceof OncePerTurnTriggerEffect;
                CardEffect resolved = unwrapOncePerTurnTrigger(gameData, perm, effect);
                if (resolved == null) continue;
                resolved = resolveTriggeringPermanentConditional(
                        gameData, perm, controllerId, leavingPermanent, resolved);
                if (resolved == null) continue;
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                if (oncePerTurn) {
                    gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                }
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another permanent you control leaving the battlefield ({})",
                        gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
            }
        }
    }

    /**
     * "Whenever another creature you control is put into exile from the battlefield" triggers.
     * Controller-scoped and checked after the permanent's card has entered exile.
     */
    public void checkAllyCreatureExiledFromBattlefieldTriggers(GameData gameData, Permanent exiledPermanent,
                                                               boolean wasCreature, UUID controllerId) {
        if (!wasCreature || controllerId == null) return;
        UUID exiledId = exiledPermanent.getId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (perm.getId().equals(exiledId)) continue;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_EXILED_FROM_BATTLEFIELD)) {
                CardEffect resolved = resolveTriggeringPermanentConditional(
                        gameData, perm, controllerId, exiledPermanent, effect);
                if (resolved == null) continue;
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another creature you control being put into exile ({})",
                        gameData.id, perm.getCard().getName(), exiledPermanent.getCard().getName());
            }
        }
    }

    /**
     * "Whenever another artifact you control leaves the battlefield" triggers (e.g. Sludge Strider).
     * Called from every leave-the-battlefield path in {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}
     * (graveyard, hand, exile, library) after the permanent has been removed. Controller-scoped
     * watcher: fires only on permanents on the leaving artifact's controller's battlefield with
     * {@link EffectSlot#ON_ANOTHER_ARTIFACT_LEAVES_BATTLEFIELD}, except the leaving artifact itself
     * ("another"). No-op unless the leaving permanent was an artifact (last-known information, read
     * off the permanent object which is unaffected by removal). Queues a non-targeting triggered
     * ability whose {@code sourcePermanentId} is the watching permanent; any player targeting for a
     * wrapped {@code MayPayManaEffect} happens at resolution.
     */
    public void checkAnotherArtifactLeavesBattlefieldTriggers(GameData gameData, Permanent leavingPermanent, UUID controllerId) {
        if (controllerId == null) return;
        if (!gameQueryService.isArtifact(leavingPermanent)) return;
        UUID leavingId = leavingPermanent.getId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (perm.getId().equals(leavingId)) continue;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_ANOTHER_ARTIFACT_LEAVES_BATTLEFIELD)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another artifact leaving the battlefield ({})",
                        gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
            }
        }
    }

    public void checkAnotherNontokenArtifactPutIntoGraveyardOrExileFromBattlefieldTriggers(
            GameData gameData, Permanent leavingPermanent, UUID controllerId, Zone destination) {
        if (controllerId == null || (destination != Zone.GRAVEYARD && destination != Zone.EXILE)) return;
        if (!gameQueryService.isArtifact(leavingPermanent) || leavingPermanent.getCard().isToken()) return;
        UUID leavingId = leavingPermanent.getId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (perm.getId().equals(leavingId)) continue;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            for (CardEffect effect : perm.getCard().getEffects(
                    EffectSlot.ON_ANOTHER_NONTOKEN_ARTIFACT_PUT_INTO_GRAVEYARD_OR_EXILE_FROM_BATTLEFIELD)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another nontoken artifact entering {} from the battlefield ({})",
                        gameData.id, perm.getCard().getName(), destination, leavingPermanent.getCard().getName());
            }
        }
    }

    public void checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(GameData gameData, Card dyingCard, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.AllyAuraOrEquipmentGraveyard(dyingCard, controllerId);

        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, controllerId, EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, ctx);
        }
    }

    public void checkControllerCardsLeaveGraveyardTriggers(GameData gameData, UUID graveyardOwnerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.ControllerCardsLeaveGraveyard(graveyardOwnerId);

        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, graveyardOwnerId, EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD, ctx);
        }
    }

    public void checkControllerCardsExiledFromGraveyardTriggers(GameData gameData, UUID graveyardOwnerId,
                                                                 int count) {
        if (count <= 0) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.ControllerCardsExiledFromGraveyard(graveyardOwnerId, count);
        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, graveyardOwnerId, EffectSlot.ON_CONTROLLER_CARDS_EXILED_FROM_GRAVEYARD, ctx);
        }
    }

    public void checkControllerCreaturesOrCreatureCardsExiledTriggers(
            GameData gameData, UUID controllerId, int count, List<Card> creatureCards) {
        if (controllerId == null || count <= 0) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.ControllerCreaturesOrCreatureCardsExiled(
                controllerId, count, creatureCards);
        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, controllerId,
                    EffectSlot.ON_CONTROLLER_CREATURES_OR_CREATURE_CARDS_EXILED, ctx);
        }
    }

    public void checkControllerCreatureCardsLeaveGraveyardTriggers(GameData gameData, UUID graveyardOwnerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.ControllerCardsLeaveGraveyard(graveyardOwnerId);
        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, graveyardOwnerId, EffectSlot.ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD, ctx);
        }
    }

    public void checkControllerCreatureCardLeavesGraveyardTriggers(GameData gameData, UUID graveyardOwnerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.ControllerCardsLeaveGraveyard(graveyardOwnerId);
        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, graveyardOwnerId, EffectSlot.ON_CONTROLLER_CREATURE_CARD_LEAVES_GRAVEYARD, ctx);
        }
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosen) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen);
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosen, UUID triggeringCardId) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen, triggeringCardId);
    }

    public void checkControllerCardsExiledDuringTurnTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CardsExiledDuringTurn(activePlayerId);
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchSlot(gameData, permanent, activePlayerId,
                    EffectSlot.ON_CONTROLLER_CARDS_EXILED_DURING_TURN, ctx);
        }
    }

    public void checkCardsExiledFromGraveyardsOrBattlefieldDuringYourTurnTriggers(
            GameData gameData, int count) {
        if (count <= 0 || gameData.activePlayerId == null) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CardsExiledFromGraveyardsOrBattlefield(count);
        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, gameData.activePlayerId,
                    EffectSlot.ON_CARDS_EXILED_FROM_GRAVEYARDS_OR_BATTLEFIELD_DURING_YOUR_TURN, ctx);
        }
    }

    public void checkControllerArtifactOrCreatureCardsLeaveGraveyardTriggers(
            GameData gameData, UUID graveyardOwnerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(graveyardOwnerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.ControllerCardsLeaveGraveyard(graveyardOwnerId);
        for (Permanent perm : battlefield) {
            dispatchSlot(gameData, perm, graveyardOwnerId,
                    EffectSlot.ON_CONTROLLER_ARTIFACT_OR_CREATURE_CARDS_LEAVE_GRAVEYARD, ctx);
        }
    }

    /**
     * Fires {@link EffectSlot#GRAVEYARD_ON_CREATURE_CARD_LEAVES_OPPONENT_GRAVEYARD} for every card
     * sitting in an opponent's graveyard when a creature card leaves {@code graveyardOwnerId}'s
     * graveyard. The trigger lives on a card in a graveyard, so the ability's controller is that
     * card's owner and there is no source permanent.
     */
    public void checkCreatureCardLeavesOpponentGraveyardTriggers(GameData gameData, UUID graveyardOwnerId,
                                                                 Card leavingCard) {
        if (leavingCard == null || !leavingCard.hasType(CardType.CREATURE)) return;

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(graveyardOwnerId)) continue;

            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;

            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> effects = gameQueryService.getEffectiveGraveyardEffects(
                        gameData, card, EffectSlot.GRAVEYARD_ON_CREATURE_CARD_LEAVES_OPPONENT_GRAVEYARD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            playerId,
                            card.getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    log.info("Game {} - {} triggers ({} left an opponent's graveyard)",
                            gameData.id, card.getName(), leavingCard.getName());
                }
            }
        }
    }

    /** Fires graveyard abilities that trigger when the card itself returns to its owner's hand. */
    public void checkCardReturnedToHandFromGraveyardTriggers(GameData gameData, UUID graveyardOwnerId,
                                                              Card returnedCard) {
        if (graveyardOwnerId == null || returnedCard == null) {
            return;
        }

        for (CardEffect effect : gameQueryService.getEffectiveGraveyardEffects(
                gameData, returnedCard, EffectSlot.GRAVEYARD_ON_SELF_RETURNED_TO_HAND)) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    returnedCard,
                    graveyardOwnerId,
                    returnedCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect))
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(returnedCard));
            log.info("Game {} - {} triggers on returning from graveyard to hand",
                    gameData.id, returnedCard.getName());
        }
    }

    public void triggerDelayedPoisonOnDeath(GameData gameData, UUID dyingCreatureCardId, UUID controllerId) {
        Integer poisonAmount = gameData.creatureGivingControllerPoisonOnDeathThisTurn.remove(dyingCreatureCardId);
        if (poisonAmount == null || poisonAmount <= 0) {
            return;
        }

        poisonAmount = gameQueryService.applyPoisonCounterReplacement(gameData, controllerId, poisonAmount);
        if (poisonAmount <= 0) return;

        poisonAmount = gameQueryService.replacePoisonCounters(gameData, controllerId, poisonAmount);
        if (poisonAmount <= 0) return;
        int currentPoison = gameData.playerPoisonCounters.getOrDefault(controllerId, 0);
        gameData.playerPoisonCounters.put(controllerId, currentPoison + poisonAmount);
        checkYouPutCountersTriggers(gameData, controllerId, poisonAmount);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " gets " + poisonAmount + " poison counter"
                + (poisonAmount > 1 ? "s" : "") + " (delayed trigger: creature died this turn).";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gets {} poison counter(s) (delayed trigger: creature died this turn)",
                gameData.id, playerName, poisonAmount);
    }

    /**
     * Delayed "return that card when it dies this turn" (Graceful Reprieve, Supernatural Stamina,
     * Adarkar Valkyrie): if the dying creature's card was registered, push a triggered ability that
     * returns it from its owner's graveyard to the battlefield. Fires at most once per registration.
     */
    public void triggerDelayedReturnOnDeath(GameData gameData, UUID dyingPermanentCardId, Card graveyardCard, UUID ownerId) {
        List<DelayedReturnOnDeath> delayedReturns = gameData.creaturesReturnedToBattlefieldOnDeathThisTurn
                .remove(dyingPermanentCardId);
        if (delayedReturns == null || delayedReturns.isEmpty()) {
            return;
        }

        for (DelayedReturnOnDeath delayedReturn : delayedReturns) {
            if (delayedReturn.requireControllerGraveyard()
                    && !delayedReturn.controllerId().equals(ownerId)) {
                continue;
            }
            UUID triggerControllerId = delayedReturn.returnUnderController()
                    ? delayedReturn.controllerId() : ownerId;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    graveyardCard,
                    triggerControllerId,
                    "Return " + graveyardCard.getName() + " to the battlefield",
                    new ArrayList<>(List.of(new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(
                            delayedReturn.enterTapped(), delayedReturn.returnUnderController())))
            );
            entry.setTriggeringCardId(graveyardCard.getId());
            entry.setTriggeringCardGraveyardEntryVersion(
                    gameData.graveyardEntryVersion(graveyardCard.getId()));
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(graveyardCard, " will return to the battlefield (it died this turn)."));
            log.info("Game {} - Delayed return trigger: {} will return to the battlefield", gameData.id, graveyardCard.getName());
        }
    }

    /**
     * Resolves delayed effects registered for a permanent dying this turn. Creature-only registrations
     * and registrations that can also apply to other permanent types are kept separately, then combined
     * when the permanent dies.
     */
    public void triggerDelayedEffectOnDeath(GameData gameData, UUID dyingPermanentCardId,
                                            UUID dyingPermanentControllerId, int dyingPermanentPower,
                                            int dyingPermanentManaValue) {
        List<DelayedEffectOnDeath> registrations = new ArrayList<>();
        List<DelayedEffectOnDeath> creatureRegistrations =
                gameData.creatureTriggeringEffectOnDeathThisTurn.remove(dyingPermanentCardId);
        if (creatureRegistrations != null) {
            registrations.addAll(creatureRegistrations);
        }
        List<DelayedEffectOnDeath> permanentRegistrations =
                gameData.permanentTriggeringEffectOnDeathThisTurn.remove(dyingPermanentCardId);
        if (permanentRegistrations != null) {
            registrations.addAll(permanentRegistrations);
        }
        if (registrations.isEmpty()) {
            return;
        }

        for (DelayedEffectOnDeath registration : registrations) {
            CardEffect effect = registration.effect();
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                        registration.sourceCard(), registration.controllerId(), List.of(effect),
                        null, null, false));
                log.info("Game {} - Delayed death trigger: {} awaits a target",
                        gameData.id, registration.sourceCard().getName());
                continue;
            }

            StackEntry delayedEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    registration.sourceCard(),
                    registration.controllerId(),
                    registration.sourceCard().getName() + " triggers (a creature dealt damage this way died)",
                    new ArrayList<>(List.of(registration.effect())),
                    dyingPermanentControllerId,
                    registration.sourcePermanentId());
            delayedEntry.setEventValue(Math.max(0, dyingPermanentPower));
            delayedEntry.setDyingPermanentManaValue(Math.max(0, dyingPermanentManaValue));
            gameData.stack.add(delayedEntry);

            log.info("Game {} - Delayed death trigger: {} triggers (a creature it damaged died this turn)",
                    gameData.id, registration.sourceCard().getName());
        }
    }

    // ── Enter-the-battlefield triggers ─────────────────────────────────

    /**
     * "Whenever a creature enters under your control" (ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).
     * The trigger count is carried through the enter-trigger context so stack entries and
     * deferred target choices are duplicated consistently.
     */
    /** Fires "whenever this creature or another creature you control is turned face up" triggers. */
    public void checkSelfOrAllyCreatureTurnsFaceUpTriggers(GameData gameData, UUID controllerId,
                                                            Permanent turnedPermanent) {
        if (!gameQueryService.isCreature(gameData, turnedPermanent)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        TriggerContext.PermanentTurnsFaceUp ctx = new TriggerContext.PermanentTurnsFaceUp(turnedPermanent, controllerId);
        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (perm.isFaceDown() || perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                registry.dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect),
                        EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP, effect, ctx);
            }
        }
    }

    /** Fires "whenever this permanent or another permanent is turned face up" triggers. */
    public void checkSelfOrAnyPermanentTurnsFaceUpTriggers(GameData gameData, UUID controllerId,
                                                            Permanent turnedPermanent) {
        TriggerContext.PermanentTurnsFaceUp ctx = new TriggerContext.PermanentTurnsFaceUp(turnedPermanent, controllerId);
        gameData.forEachBattlefield((ownerId, battlefield) -> {
            for (Permanent perm : new ArrayList<>(battlefield)) {
                if (perm.isFaceDown() || perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;

                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    registry.dispatch(new TriggerMatchContext(gameData, perm, ownerId, effect),
                            EffectSlot.ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP, effect, ctx);
                }
            }
        });
    }

    /** Fires "whenever this permanent or another permanent you control is turned face up" triggers. */
    public void checkSelfOrAllyPermanentTurnsFaceUpTriggers(GameData gameData, UUID controllerId,
                                                              Permanent turnedPermanent) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        TriggerContext.PermanentTurnsFaceUp ctx = new TriggerContext.PermanentTurnsFaceUp(turnedPermanent, controllerId);
        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (perm.isFaceDown() || perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                registry.dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect),
                        EffectSlot.ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP, effect, ctx);
            }
        }
    }

    /** Fires non-targeting triggers watching a permanent controlled by {@code controllerId} transform. */
    public void checkAllyPermanentTransformsTriggers(GameData gameData, UUID controllerId,
                                                      Permanent transformedPermanent, Card transformedCard) {
        if (controllerId == null || transformedPermanent == null || transformedCard == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        TriggerContext.PermanentTransforms ctx =
                new TriggerContext.PermanentTransforms(transformedPermanent, transformedCard, controllerId);
        for (Permanent perm : new ArrayList<>(battlefield)) {
            dispatchSlot(gameData, perm, controllerId, EffectSlot.ON_ALLY_PERMANENT_TRANSFORMS, ctx);
        }
    }

    public void checkDayNightChangeTriggers(GameData gameData, DayNight previous, DayNight current) {
        if (previous == current || previous == DayNight.NEITHER || current == DayNight.NEITHER) {
            return;
        }

        TriggerContext context = new TriggerContext.DayNightChange(previous, current);
        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent permanent : new ArrayList<>(battlefield)) {
                    dispatchSlot(gameData, permanent, controllerId,
                            EffectSlot.ON_DAY_NIGHT_CHANGE, context);
                }
            }
            checkGraveyardDayNightChangeTriggers(gameData, controllerId);
        }
        triggeredAbilityQueueService.processNextDayNightTriggerTarget(gameData);
    }

    private void checkGraveyardDayNightChangeTriggers(GameData gameData, UUID controllerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            for (CardEffect effect : gameQueryService.getEffectiveGraveyardEffects(
                    gameData, card, EffectSlot.GRAVEYARD_ON_DAY_NIGHT_CHANGE)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} triggers from the graveyard when day/night changes",
                        gameData.id, card.getName());
            }
        }
    }

    public void processNextDayNightTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDayNightTriggerTarget(gameData);
    }

    /** Fires triggers watching a permanent controlled by {@code controllerId} enter transformed. */
    public void checkAllyPermanentEntersTransformedTriggers(GameData gameData, UUID controllerId,
                                                             Card enteringCard) {
        if (controllerId == null || enteringCard == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        Permanent enteringPermanent = battlefield.stream()
                .filter(permanent -> permanent.getCard() == enteringCard)
                .findFirst()
                .orElse(null);
        if (enteringPermanent == null || !enteringPermanent.isTransformed()) return;

        TriggerContext.PermanentEnters ctx = new TriggerContext.PermanentEnters(
                enteringCard, controllerId, null,
                1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard),
                enteringPermanent.getId());
        for (Permanent perm : new ArrayList<>(battlefield)) {
            dispatchSlot(gameData, perm, controllerId,
                    EffectSlot.ON_ALLY_PERMANENT_ENTERS_TRANSFORMED, ctx);
        }
    }

    public void checkAllyCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCreature, int extraWizardTriggers) {
        if (enteringCreature.getToughness() == null) return;

        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        var ctx = new TriggerContext.PermanentEnters(
                enteringCreature, controllerId, null, 1 + extraWizardTriggers,
                enteringPermanent != null ? enteringPermanent.getId() : null);

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : new ArrayList<>(battlefield)) {
            // "Whenever this creature or another creature you control enters" is the same scan
            // minus the self-exclusion, so it is checked before the source is skipped.
            List<CardEffect> selfOrAllyEffects = new ArrayList<>(
                    perm.getCard().getEffects(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD));
            selfOrAllyEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, perm, EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD));
            if (!selfOrAllyEffects.isEmpty()) {
                for (CardEffect effect : selfOrAllyEffects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCreature, gameData, controllerId);
                    if (resolved == null) continue;
                    resolved = resolveTriggeringPermanentConditional(
                            gameData, perm, controllerId, enteringPermanent, resolved);
                    if (resolved == null) continue;
                    resolved = unwrapEnterCreatureConditional(gameData, enteringCreature, perm, resolved);
                    if (resolved == null) continue;
                    boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                    resolved = unwrapOncePerTurnTrigger(gameData, perm, resolved);
                    if (resolved == null) continue;
                    if (!passesEnterInterveningIf(gameData, perm, controllerId, enteringPermanent, resolved)) continue;
                    if (dispatchEnter(gameData, perm, controllerId,
                            EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD, resolved, ctx)
                            && oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                }
            }

            if (perm.getCard() == enteringCreature) continue;

            List<CardEffect> effects = new ArrayList<>(
                    perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, perm, EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD));
            if (!effects.isEmpty()) {
                for (CardEffect effect : effects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCreature, gameData, controllerId);
                    if (resolved == null) continue;
                    resolved = resolveTriggeringPermanentConditional(
                            gameData, perm, controllerId, enteringPermanent, resolved);
                    if (resolved == null) continue;
                    resolved = unwrapEnterCreatureConditional(gameData, enteringCreature, perm, resolved);
                    if (resolved == null) continue;
                    boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                    resolved = unwrapOncePerTurnTrigger(gameData, perm, resolved);
                    if (resolved == null) continue;
                    if (!passesEnterInterveningIf(gameData, perm, controllerId, enteringPermanent, resolved)) continue;
                    if (dispatchEnter(gameData, perm, controllerId,
                            EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, resolved, ctx)
                            && oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                }
            }

            if (!enteringCreature.isToken()) {
                List<CardEffect> batchEffects = new ArrayList<>(
                        perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD));
                batchEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                        gameData, perm, EffectSlot.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD));
                for (CardEffect effect : batchEffects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(
                            effect, enteringCreature, gameData, controllerId);
                    if (resolved == null) continue;
                    resolved = resolveTriggeringPermanentConditional(
                            gameData, perm, controllerId, enteringPermanent, resolved);
                    if (resolved == null) continue;
                    resolved = unwrapEnterCreatureConditional(gameData, enteringCreature, perm, resolved);
                    if (resolved == null) continue;
                    boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                    resolved = unwrapOncePerTurnTrigger(gameData, perm, resolved);
                    if (resolved == null) continue;
                    if (!passesEnterInterveningIf(gameData, perm, controllerId, enteringPermanent, resolved)) continue;
                    if (dispatchEnter(gameData, perm, controllerId,
                            EffectSlot.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD, resolved, ctx)
                            && oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                }
            }

            if (enteringPermanent != null && !perm.isLosesAllAbilitiesUntilEndOfTurn()
                    && gameQueryService.hasKeyword(gameData, perm, Keyword.EVOLVE)) {
                collectEvolveTrigger(gameData, controllerId, perm, enteringPermanent,
                        1 + extraWizardTriggers);
            }
        }

        for (CreatureEntersTriggerWatcher watcher
                : List.copyOf(gameData.allyCreatureEntersTriggerWatchers)) {
            if (!watcher.controllerId().equals(controllerId) || enteringPermanent == null) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setTargetId(enteringPermanent.getId());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }

        // Graveyard-resident creature-enters triggers (GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
        // e.g. Unconventional Tactics). A graveyard card is not a permanent, so it is excluded from Naban
        // doubling — hence added after the extra-trigger duplication above.
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> effects = gameQueryService.getEffectiveGraveyardEffects(
                        gameData, card, EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCreature, gameData, controllerId);
                    if (resolved == null) continue;

                    if (resolved instanceof TriggeringPermanentConditionalEffect conditional) {
                        if (enteringPermanent == null) continue;
                        FilterContext filterContext = FilterContext.of(gameData)
                                .withSourceCardId(card.getId())
                                .withSourceControllerId(controllerId);
                        if (!predicateEvaluationService.matchesPermanentPredicate(
                                enteringPermanent, conditional.predicate(), filterContext)) {
                            continue;
                        }
                        resolved = conditional.wrapped();
                    }

                    if (resolved instanceof MayEffect may) {
                        if (may.usesEnteringPermanentReference() && enteringPermanent != null) {
                            gameData.queueMayAbility(card, controllerId, may, enteringPermanent.getId(), null);
                        } else {
                            gameData.queueMayAbility(card, controllerId, may);
                        }
                    } else {
                        StackEntry entry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                controllerId,
                                card.getName() + "'s ability",
                                new ArrayList<>(List.of(resolved))
                        );
                        if (resolved.usesEnteringPermanentReference()) {
                            entry.setTargetId(enteringPermanent != null ? enteringPermanent.getId() : null);
                            entry.setNonTargeting(true);
                        }
                        gameData.stack.add(entry);
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    log.info("Game {} - {} graveyard creature-enters trigger queued", gameData.id, card.getName());
                }
            }
        }

        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (watcher.slot() != EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD
                    || !watcher.controllerId().equals(controllerId)) {
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary ally creature-enters trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }

        if (enteringPermanent != null) {
            for (Emblem emblem : gameData.emblems) {
                if (!emblem.controllerId().equals(controllerId)) continue;
                for (CardEffect effect : emblem.staticEffects()) {
                    if (!(effect instanceof DealDamageToAnyTargetOnAllyCreatureEntersEffect)) continue;
                    Card sourceCard = emblem.sourceCard() != null ? emblem.sourceCard() : enteringCreature;
                    gameData.queueInteraction(new PermanentChoiceContext.EnteringPermanentAnyTargetTrigger(
                            sourceCard, controllerId,
                            List.of(new DealDamageToAnyTargetEffect(new SourcePower())),
                            enteringPermanent.getId()));
                    gameLogService.append(gameData, GameLog.text(
                            sourceCard.getName() + "'s emblem triggers for " + enteringCreature.getName()
                                    + " entering."));
                    log.info("Game {} - {}'s emblem triggers for {} entering",
                            gameData.id, sourceCard.getName(), enteringCreature.getName());
                }
            }
        }
    }

    /**
     * "Whenever another creature enters" (ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD). Naban doubling
     * is applied per effect (only for permanents the entering creature's controller controls); the
     * default fallback skips targeting effects.
     */
    public void checkAnyCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        checkAnyCreatureEntersTriggers(gameData, enteringCreatureControllerId, enteringCreature,
                gameQueryService.countETBExtraTriggers(gameData, enteringCreatureControllerId, enteringCreature));
    }

    public void checkAnyCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId,
                                               Card enteringCreature, int extraWizardTriggers) {
        if (enteringCreature.getToughness() == null) return;

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = new ArrayList<>(perm.getCard().getEffects(
                    EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, perm, EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD));
            if (effects.isEmpty()) return;
            if (perm.getCard() == enteringCreature) return;

            int extraTriggers = gameQueryService.countETBExtraTriggers(
                    gameData, playerId, enteringCreatureControllerId, enteringCreature);
            var ctx = new TriggerContext.PermanentEnters(
                    enteringCreature, enteringCreatureControllerId, null, 1 + extraTriggers, null);

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCreature, gameData, playerId);
                if (resolved == null) continue;
                resolved = unwrapEnterCreatureConditional(gameData, enteringCreature, perm, resolved);
                if (resolved == null) continue;
                dispatchEnter(gameData, perm, playerId, EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, resolved, ctx);
            }
        });
    }

    /**
     * Fires turn-scoped delayed triggers registered by Beck. Each registration is a separate
     * optional draw trigger, including for tokens and creatures controlled by the other player.
     */
    public void checkCreatureEntersThisTurnTriggers(GameData gameData, UUID enteringCreatureControllerId,
                                                    Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        List<UUID> controllers = new ArrayList<>(gameData.orderedPlayerIds);
        int activePlayerIndex = controllers.indexOf(gameData.activePlayerId);
        if (activePlayerIndex > 0) {
            List<UUID> apnapControllers = new ArrayList<>(controllers.subList(activePlayerIndex, controllers.size()));
            apnapControllers.addAll(controllers.subList(0, activePlayerIndex));
            controllers = apnapControllers;
        }

        for (UUID controllerId : controllers) {
            List<Card> sources = gameData.creatureEntersDrawSourcesThisTurn.get(controllerId);
            if (sources != null) {
                for (Card sourceCard : new ArrayList<>(sources)) {
                    gameData.queueMayAbility(sourceCard, controllerId,
                            new MayEffect(new DrawCardEffect(), "Draw a card?"));
                    log.info("Game {} - {} triggers for creature entering (may draw)",
                            gameData.id, sourceCard.getName());
                }
            }

            if (controllerId.equals(enteringCreatureControllerId)) {
                List<Card> persistentSources = gameData.creatureEntersDrawSources.get(controllerId);
                if (persistentSources == null) continue;
                for (Card sourceCard : new ArrayList<>(persistentSources)) {
                    gameData.queueMayAbility(sourceCard, controllerId,
                            new MayEffect(new DrawCardEffect(), "Draw a card?"));
                    log.info("Game {} - {} triggers for controlled creature entering (may draw)",
                            gameData.id, sourceCard.getName());
                }
            }
        }
    }

    /**
     * "Whenever a creature enchanted player controls enters" (ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD).
     * Scans every battlefield for player-enchanting Curse auras attached to the entering creature's controller
     * and queues one triggered ability each, controlled by the Aura's controller ("you"). The enchanted player
     * is baked as the (non-targeting) {@code targetId} so a {@code LoseLifeEffect(TARGET_PLAYER)} lands on them
     * while an accompanying {@code GainLifeEffect} feeds the controller. Used by Trespasser's Curse.
     */
    public void checkEnchantedPlayerCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        gameData.forEachPermanent((auraControllerId, perm) -> {
            if (!perm.isAttached() || !enteringCreatureControllerId.equals(perm.getAttachedTo())) return;
            if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, auraControllerId)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) return;

            int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                    gameData, auraControllerId, enteringCreatureControllerId, enteringCreature);
            for (int i = 0; i < triggerCount; i++) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        auraControllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
                        enteringCreatureControllerId,
                        perm.getId());
                entry.setNonTargeting(true);
                gameData.stack.add(entry);

                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} enchanted-player-creature-enters trigger queued", gameData.id, perm.getCard().getName());
            }
        });
    }

    /**
     * "Whenever a player puts a permanent onto the battlefield" (ON_ANY_PERMANENT_ENTERS_BATTLEFIELD).
     * Fires for every entering permanent regardless of type or controller; wrap the effect in a
     * {@link TriggeringCardConditionalEffect} to restrict which permanents trigger it. The entering
     * permanent's controller is baked in as the non-targeting {@code targetId} so player-directed
     * effects act on "that player". The entering permanent's id is stamped on
     * {@code triggeringPermanentId} (and its card id on {@code triggeringCardId}) so effects can act
     * on "that permanent" / its name (Eye of Singularity). Used by Nature's Wrath.
     */
    public void checkAnyPermanentEntersTriggers(GameData gameData, UUID enteringControllerId, Card enteringCard) {
        UUID enteringPermanentId = null;
        List<Permanent> enteringBattlefield = gameData.playerBattlefields.get(enteringControllerId);
        if (enteringBattlefield != null) {
            for (Permanent p : enteringBattlefield) {
                if (p.getCard() == enteringCard) {
                    enteringPermanentId = p.getId();
                    break;
                }
            }
        }
        final UUID resolvedEnteringPermanentId = enteringPermanentId;

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) return;

            // Each effect in this slot is its own triggered ability (Nature's Wrath has two), so a
            // card whose entry matches both conditions is dispatched separately.
            for (CardEffect effect : effects) {
                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                        gameData, playerId, enteringControllerId, enteringCard);
                var effectContext = new TriggerContext.PermanentEnters(
                        enteringCard, enteringControllerId, enteringControllerId,
                        triggerCount, resolvedEnteringPermanentId);
                dispatchEnter(gameData, perm, playerId,
                        EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD, effect, effectContext);
            }
        });

        if (enteringCard.getToughness() != null || enteringCard.hasType(CardType.ENCHANTMENT)) {
            for (TemporaryGlobalTriggeredAbility watcher
                    : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
                if (watcher.slot() != EffectSlot.ON_ALLY_CREATURE_OR_ENCHANTMENT_ENTERS_BATTLEFIELD
                        || !watcher.controllerId().equals(enteringControllerId)) {
                    continue;
                }

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.sourceCard(),
                        watcher.controllerId(),
                        watcher.sourceCard().getName() + "'s ability",
                        new ArrayList<>(List.of(watcher.effect())));
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
                log.info("Game {} - {} temporary creature-or-enchantment-enter trigger fires",
                        gameData.id, watcher.sourceCard().getName());
            }
        }
    }

    /** "Whenever a creature enters under an opponent's control" (ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD). */
    public void checkOpponentCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(enteringCreatureControllerId)) return;

            int extraTriggers = gameQueryService.countETBExtraTriggers(
                    gameData, playerId, enteringCreatureControllerId, enteringCreature);
            var ctx = new TriggerContext.PermanentEnters(
                    enteringCreature, enteringCreatureControllerId, enteringCreatureControllerId,
                    1 + extraTriggers, null);

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCreature, gameData, playerId);
                    if (resolved == null) continue;
                    dispatchEnter(gameData, perm, playerId, EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD, resolved, ctx);
                }
            }
        });
    }

    /** "Whenever a land enters under an opponent's control" (ON_OPPONENT_LAND_ENTERS_BATTLEFIELD). */
    public void checkOpponentLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(landControllerId)) return;

            int extraTriggers = gameQueryService.countETBExtraTriggers(
                    gameData, playerId, landControllerId, enteringLand);
            var ctx = new TriggerContext.PermanentEnters(
                    enteringLand, landControllerId, landControllerId, 1 + extraTriggers, null);

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringLand, gameData, playerId);
                    if (resolved == null) continue;
                    resolved = unwrapImprintedCardNameConditional(gameData, enteringLand, perm, resolved);
                    if (resolved == null) continue;
                    resolved = unwrapPermanentEnteredThisTurnConditional(gameData, landControllerId, resolved);
                    if (resolved == null) continue;
                    dispatchEnter(gameData, perm, playerId, EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD, resolved, ctx);
                }
            }
        });
    }

    /**
     * "Whenever a nontoken artifact enters under your control" (ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD).
     * The entering permanent's id is preserved on any queued may-pay ability (e.g. Mirrorworks).
     */
    public void checkAllyNontokenArtifactEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (enteringCard.isToken()) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);

        UUID enteringPermanentId = null;
        for (Permanent p : battlefield) {
            if (p.getCard() == enteringCard) {
                enteringPermanentId = p.getId();
                break;
            }
        }
        Permanent enteringPermanent = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, enteringPermanentId);
        if (enteringPermanent == null || !gameQueryService.isArtifact(gameData, enteringPermanent)) return;

        int extraTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard);
        var ctx = new TriggerContext.PermanentEnters(
                enteringCard, controllerId, null, 1 + extraTriggers, enteringPermanentId);

        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCard, gameData, controllerId);
                if (resolved == null) continue;

                dispatchEnter(gameData, perm, controllerId, EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD, resolved, ctx);
            }
        }
    }

    /**
     * "Whenever a nontoken creature enters under your control" (ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD).
     * The entering permanent's id is preserved on any queued may-pay ability (e.g. Minion Reflector), so a
     * token-copy effect knows which creature to copy.
     */
    public void checkAllyNontokenCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (enteringCard.getToughness() == null) return;
        if (enteringCard.isToken()) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);

        UUID enteringPermanentId = null;
        for (Permanent p : battlefield) {
            if (p.getCard() == enteringCard) {
                enteringPermanentId = p.getId();
                break;
            }
        }

        int extraTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard);
        var ctx = new TriggerContext.PermanentEnters(
                enteringCard, controllerId, null, 1 + extraTriggers, enteringPermanentId);

        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCard, gameData, controllerId);
                if (resolved == null) continue;
                dispatchEnter(gameData, perm, controllerId, EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, resolved, ctx);
            }
        }
    }

    /**
     * "Whenever an artifact enters under your control" (ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD).
     * Supports subtype/token gating via {@code TriggeringCardConditionalEffect} and intervening-if
     * {@code ControlsPermanentCount} (e.g. Voldaren Bloodcaster: whenever you create a Blood token,
     * if you control five or more Blood tokens, transform).
     */
    public void checkAllyArtifactEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        UUID enteringPermanentId = null;
        for (Permanent p : battlefield) {
            if (p.getCard() == enteringCard) {
                enteringPermanentId = p.getId();
                break;
            }
        }
        Permanent enteringPermanent = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, enteringPermanentId);
        if (enteringPermanent == null || !gameQueryService.isArtifact(gameData, enteringPermanent)) return;
        int extraTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard);
        var ctx = new TriggerContext.PermanentEnters(
                enteringCard, controllerId, null, 1 + extraTriggers, enteringPermanentId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCard, gameData, controllerId);
                if (resolved == null) continue;
                CardEffect dispatchedEffect = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, perm, resolved);
                if (dispatchedEffect == null) continue;

                // Intervening-if (CR 603.4): gate at trigger time; leave ConditionalEffect wrapped
                // so EffectResolutionService re-checks at resolution.
                if (dispatchedEffect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof ControlsPermanentCount) {
                    if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, controllerId))) {
                        log.info("Game {} - {} ally-artifact trigger skipped ({} not met)",
                                gameData.id, perm.getCard().getName(), conditional.condition().conditionName());
                        continue;
                    }
                }

                boolean triggered = dispatchEnter(gameData, perm, controllerId,
                        EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, effect, dispatchedEffect, ctx);
                if (triggered) {
                    OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, resolved);
                }
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> effects = gameQueryService.getEffectiveGraveyardEffects(
                        gameData, card, EffectSlot.GRAVEYARD_ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCard, gameData, controllerId);
                    if (resolved == null) continue;

                    if (resolved instanceof MayEffect may) {
                        gameData.queueMayAbility(card, controllerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                controllerId,
                                card.getName() + "'s ability",
                                new ArrayList<>(List.of(resolved))
                        ));
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    log.info("Game {} - {} graveyard artifact trigger queued", gameData.id, card.getName());
                }
            }
        }
        collectEmblemArtifactEntersTriggers(gameData, controllerId, enteringCard);
    }

    /** Fires Full Throttle's delayed untap trigger once for each registration at each combat. */
    public void checkUntapAttackedCreaturesEachCombatThisTurnTriggers(GameData gameData) {
        List<UUID> controllers = new ArrayList<>(gameData.orderedPlayerIds);
        int activePlayerIndex = controllers.indexOf(gameData.activePlayerId);
        if (activePlayerIndex > 0) {
            List<UUID> apnapControllers = new ArrayList<>(controllers.subList(activePlayerIndex, controllers.size()));
            apnapControllers.addAll(controllers.subList(0, activePlayerIndex));
            controllers = apnapControllers;
        }

        for (UUID controllerId : controllers) {
            List<Card> sources = gameData.untapAttackedCreaturesEachCombatThisTurnSources.get(controllerId);
            if (sources == null) continue;
            for (Card sourceCard : new ArrayList<>(sources)) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        sourceCard,
                        controllerId,
                        sourceCard.getName() + "'s delayed ability",
                        List.of(new UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES)));
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed combat trigger queued", gameData.id, sourceCard.getName());
            }
        }
    }

    private void collectEmblemArtifactEntersTriggers(GameData gameData, UUID controllerId,
                                                     Card enteringCard) {
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(controllerId)) continue;

            for (CardEffect effect : emblem.staticEffects()) {
                if (!(effect instanceof EmblemArtifactEntersTriggerEffect trigger)) {
                    continue;
                }

                Card source = emblem.sourceCard();
                Card sourceCard = source != null ? source : enteringCard;
                String description = (source != null ? source.getName() : "Emblem") + "'s emblem";
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        sourceCard,
                        controllerId,
                        List.of(new DealDamageToAnyTargetEffect(trigger.damage()))
                ));
                gameLogService.append(gameData, GameLog.text(
                        description + " triggers for an artifact entering and deals "
                                + trigger.damage() + " damage to any target."));
                log.info("Game {} - {} triggers for {} entering (artifact-enter emblem)",
                        gameData.id, description, enteringCard.getName());
            }
        }
    }

    /** Fires once for a batch of one or more tokens entering under a player's control. */
    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count) {
        checkAllyTokenEntersTriggers(gameData, controllerId, count, List.of());
    }

    private void collectTemporaryControllerSpellCastTriggers(GameData gameData, Card spellCard,
                                                               UUID castingPlayerId) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (watcher.slot() != EffectSlot.ON_CONTROLLER_CASTS_SPELL
                    || !watcher.controllerId().equals(castingPlayerId)) {
                continue;
            }

            if (!(watcher.effect() instanceof CopyControllerCastSpellOnSpellCastEffect trigger)) {
                if (watcher.targetFilter() != null) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            watcher.sourceCard(), watcher.controllerId(), List.of(watcher.effect()),
                            false, watcher.targetFilter(), 0, null, true));
                } else {
                    StackEntry triggerEntry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY, watcher.sourceCard(), watcher.controllerId(),
                            watcher.sourceCard().getName() + "'s ability",
                            new ArrayList<>(List.of(watcher.effect())));
                    triggerEntry.setNonTargeting(true);
                    gameData.stack.add(triggerEntry);
                }
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
                continue;
            }
            if (!predicateEvaluationService.matchesCardPredicate(
                    spellCard, trigger.spellFilter(), null, gameData, castingPlayerId)) {
                continue;
            }

            StackEntry spellEntry = gameData.stack.stream()
                    .filter(entry -> entry.getCard().getId().equals(spellCard.getId()))
                    .findFirst()
                    .orElse(null);
            if (spellEntry == null) {
                continue;
            }

            CardEffect copyEffect = new CopyControllerCastSpellEffect(
                    new StackEntry(spellEntry), castingPlayerId,
                    trigger.grantedKeywords(), trigger.additionalTypes(),
                    trigger.tokenCopy(), trigger.mayChooseNewTargets());
            CardEffect resolutionEffect = trigger.manaCost() == null
                    ? copyEffect
                    : new MayPayManaEffect(trigger.manaCost(), copyEffect,
                    "Pay " + trigger.manaCost() + " to copy " + spellCard.getName() + "?");
            StackEntry triggerEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(resolutionEffect)));
            triggerEntry.setNonTargeting(true);
            gameData.stack.add(triggerEntry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId,
                                              List<UUID> permanentIds) {
        checkAllyTokenEntersTriggers(gameData, controllerId, permanentIds.size(), permanentIds);
    }

    private void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count,
                                               List<UUID> permanentIds) {
        if (count <= 0) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.isEmpty()) return;

        int extraTriggers = gameQueryService.countETBExtraTriggersForAnyPermanent(gameData, controllerId);
        TriggerContext.TokensEnter ctx = new TriggerContext.TokensEnter(
                count, 1 + extraTriggers, permanentIds);
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) continue;
            List<CardEffect> effects = permanent.getCard().getEffects(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD);
            if (effects != null) {
                for (CardEffect effect : effects) {
                    boolean oncePerTurn = effect instanceof OncePerTurnTriggerEffect;
                    CardEffect resolved = unwrapOncePerTurnTrigger(gameData, permanent, effect);
                    if (resolved == null) continue;
                    TriggerMatchContext match = new TriggerMatchContext(gameData, permanent, controllerId, resolved);
                    boolean triggered = registry.dispatch(
                            match, EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD, resolved, ctx);
                    if (triggered && oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(permanent.getId());
                    }
                }
            }

            List<CardEffect> batchEffects = new ArrayList<>(
                    permanent.getCard().getEffects(EffectSlot.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD));
            batchEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, permanent, EffectSlot.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD));
            for (CardEffect effect : batchEffects) {
                for (UUID enteringPermanentId : permanentIds) {
                    Permanent enteringPermanent = gameQueryService.findPermanentById(gameData, enteringPermanentId);
                    if (enteringPermanent == null || enteringPermanent.getId().equals(permanent.getId())
                            || !gameQueryService.isCreature(gameData, enteringPermanent)) {
                        continue;
                    }
                    Card enteringCard = enteringPermanent.getCard();
                    CardEffect resolved = unwrapTriggeringCardConditional(
                            effect, enteringCard, gameData, controllerId);
                    if (resolved == null) continue;
                    resolved = resolveTriggeringPermanentConditional(
                            gameData, permanent, controllerId, enteringPermanent, resolved);
                    if (resolved == null) continue;
                    resolved = unwrapEnterCreatureConditional(gameData, enteringCard, permanent, resolved);
                    if (resolved == null) continue;
                    boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                    resolved = unwrapOncePerTurnTrigger(gameData, permanent, resolved);
                    if (resolved == null) break;
                    if (!passesEnterInterveningIf(
                            gameData, permanent, controllerId, enteringPermanent, resolved)) {
                        continue;
                    }
                    TriggerContext.PermanentEnters batchContext = new TriggerContext.PermanentEnters(
                            enteringCard, controllerId, null, 1 + extraTriggers, enteringPermanentId);
                    if (dispatchEnter(gameData, permanent, controllerId,
                            EffectSlot.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD, resolved, batchContext)
                            && oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(permanent.getId());
                    }
                    break;
                }
            }
        }

        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(controllerId)) {
                continue;
            }
            List<Permanent> opponentBattlefield = gameData.playerBattlefields.get(opponentId);
            if (opponentBattlefield == null || opponentBattlefield.isEmpty()) {
                continue;
            }
            int opponentExtraTriggers = gameQueryService.countETBExtraTriggersForAnyPermanent(
                    gameData, opponentId);
            TriggerContext.TokensEnter opponentContext = new TriggerContext.TokensEnter(
                    count, 1 + opponentExtraTriggers, permanentIds);
            for (Permanent permanent : opponentBattlefield) {
                List<CardEffect> effects = permanent.getCard().getEffects(
                        EffectSlot.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD);
                for (CardEffect effect : effects) {
                    boolean oncePerTurn = effect instanceof OncePerTurnTriggerEffect;
                    CardEffect resolved = unwrapOncePerTurnTrigger(gameData, permanent, effect);
                    if (resolved == null) {
                        continue;
                    }
                    TriggerMatchContext match = new TriggerMatchContext(
                            gameData, permanent, opponentId, resolved);
                    boolean triggered = registry.dispatch(match,
                            EffectSlot.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD, resolved, opponentContext);
                    if (triggered && oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(permanent.getId());
                    }
                }
            }
        }
    }

    /**
     * "Whenever an Equipment enters under your control" (ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD).
     */
    public void checkAllyEquipmentEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (!enteringCard.getSubtypes().contains(CardSubtype.EQUIPMENT)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        UUID enteringPermanentId = null;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard() == enteringCard) {
                enteringPermanentId = permanent.getId();
                break;
            }
        }
        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard);
        var ctx = new TriggerContext.PermanentEnters(
                enteringCard, controllerId, null, triggerCount, enteringPermanentId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;
            if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCard, gameData, controllerId);
                if (resolved == null) continue;
                dispatchEnter(gameData, perm, controllerId,
                        EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD, resolved, ctx);
            }
        }
    }

    /**
     * "Whenever an Aura becomes attached to this creature" (ON_AURA_ATTACHED_TO_SELF, e.g. Brood
     * Keeper). Fires on the newly enchanted permanent for its own controller, regardless of who
     * controls the Aura. Call this after the Aura's {@code attachedTo} has been set.
     */
    public void checkAuraAttachedTriggers(GameData gameData, Card auraCard, UUID enchantedPermanentId) {
        if (auraCard == null || enchantedPermanentId == null) return;
        if (!auraCard.getSubtypes().contains(CardSubtype.AURA)) return;

        Permanent enchanted = gameQueryService.findPermanentById(gameData, enchantedPermanentId);
        if (enchanted == null) return;
        UUID controllerId = gameQueryService.findPermanentController(gameData, enchantedPermanentId);
        if (controllerId == null) return;

        for (CardEffect effect : enchanted.getCard().getEffects(EffectSlot.ON_AURA_ATTACHED_TO_SELF)) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    enchanted.getCard(),
                    controllerId,
                    enchanted.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    enchanted.getId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(enchanted.getCard()));
            log.info("Game {} - {} triggers on {} becoming attached to it",
                    gameData.id, enchanted.getCard().getName(), auraCard.getName());
        }
    }

    public void checkAuraAttachedTriggers(GameData gameData, Permanent aura, UUID enchantedPermanentId) {
        if (aura == null) return;
        Card auraCard = aura.getCard();
        checkAuraAttachedTriggers(gameData, auraCard, enchantedPermanentId);
        Permanent enchanted = gameQueryService.findPermanentById(gameData, enchantedPermanentId);
        if (enchanted == null) return;
        UUID enchantedControllerId = gameQueryService.findPermanentController(gameData, enchantedPermanentId);
        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if (auraControllerId != null && auraControllerId.equals(enchantedControllerId)
                && gameQueryService.isCreature(gameData, enchanted)) {
            List<Permanent> alliedBattlefield = gameData.playerBattlefields.get(auraControllerId);
            if (alliedBattlefield != null) {
                for (Permanent watcher : List.copyOf(alliedBattlefield)) {
                    for (CardEffect effect : watcher.getCard().getEffects(
                            EffectSlot.ON_ALLY_AURA_ATTACHED_TO_ALLY_CREATURE)) {
                        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                                watcher.getCard(), auraControllerId,
                                watcher.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(effect)), null, watcher.getId());
                        trigger.setNonTargeting(true);
                        gameData.enqueueTrigger(trigger);
                        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                    }
                }
            }
        }
        if (auraControllerId == null || auraControllerId.equals(enchantedControllerId)
                || gameQueryService.isLand(gameData, enchanted)
                || enchanted.getCard().getManaValue() > auraCard.getManaValue()) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(auraControllerId);
        if (battlefield == null) return;
        for (Permanent watcher : List.copyOf(battlefield)) {
            for (CardEffect effect : watcher.getCard().getEffects(
                    EffectSlot.ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT)) {
                StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(), auraControllerId, watcher.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)), enchanted.getId(), aura.getId());
                trigger.setNonTargeting(true);
                gameData.enqueueTrigger(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
            }
        }
    }

    /** Fires an Equipment's targeted "becomes attached" abilities after a new attachment. */
    public void checkEquipmentAttachedTriggers(GameData gameData, Permanent equipment, UUID oldAttachedTo) {
        if (equipment == null || equipment.getAttachedTo() == null
                || equipment.getAttachedTo().equals(oldAttachedTo)
                || !equipment.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            return;
        }

        Permanent equipped = gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        if (equipped == null || !gameQueryService.isCreature(gameData, equipped)) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, equipment.getId());
        if (controllerId == null) return;

        List<CardEffect> effects = equipment.getCard().getEffects(EffectSlot.ON_EQUIPMENT_ATTACHED);
        if (effects == null || effects.isEmpty()) return;

        for (CardEffect effect : effects) {
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                        equipment.getCard(), controllerId, new ArrayList<>(List.of(effect)),
                        equipment.getId(), equipment.getCard().getTargetFilter(), equipped.getId()));
            } else {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        equipment.getCard(),
                        controllerId,
                        equipment.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        equipment.getId());
                entry.setTriggeringPermanentId(equipped.getId());
                gameData.enqueueTrigger(entry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(equipment.getCard()));
            log.info("Game {} - {} triggers when attached to {}", gameData.id,
                    equipment.getCard().getName(), equipped.getCard().getName());
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
        }
    }

    /**
     * "Whenever an enchantment enters under your control" (ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD).
     * Supports subtype gating via {@code TriggeringCardConditionalEffect} (e.g. Trial of Solidarity's
     * "Whenever a Cartouche you control enters").
     */
    public void checkAllyEnchantmentEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (!enteringCard.hasType(CardType.ENCHANTMENT)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;
            if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringCard, gameData, controllerId);
                if (resolved == null) continue;

                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                        gameData, controllerId, controllerId, enteringCard);

                // A targeted effect can't go on the stack without its target, so queue the choice first.
                if (resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || resolved.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                    for (int i = 0; i < triggerCount; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                                perm.getCard(), controllerId, new ArrayList<>(List.of(resolved)), perm.getId(),
                                null, perm.getId()));
                        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                        log.info("Game {} - {} triggers for {} entering (ally enchantment entered, awaiting target)",
                                gameData.id, perm.getCard().getName(), enteringCard.getName());
                    }
                    continue;
                }

                for (int i = 0; i < triggerCount; i++) {
                    StackEntry triggered = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(resolved)),
                            null,
                            perm.getId()
                    );
                    // Ajani's Chosen needs the entering enchantment itself at resolution ("if that
                    // enchantment is an Aura, you may attach it to the token").
                    triggered.setTriggeringCardId(enteringCard.getId());
                    gameData.stack.add(triggered);
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers for {} entering (ally enchantment entered)",
                            gameData.id, perm.getCard().getName(), enteringCard.getName());
                }
            }
        }
    }

    /**
     * "Whenever you play a land" (ON_CONTROLLER_PLAYS_LAND, e.g. Search the City, Juju Bubble) and
     * its opponent-side mirror ON_OPPONENT_PLAYS_LAND (Dirtcowl Wurm). Called from the land-play
     * sites only, so a land put onto the battlefield by an effect does not trigger either.
     * Dispatches through the collector registry so name-match gates (Search the City) and bare
     * effects (Juju Bubble) both resolve correctly.
     */
    public void checkControllerPlaysLandTriggers(GameData gameData, UUID playingPlayerId, Card landCard) {
        checkControllerPlaysLandTriggers(gameData, playingPlayerId, landCard, false);
    }

    public void checkControllerPlaysLandTriggers(GameData gameData, UUID playingPlayerId, Card landCard,
                                                 boolean fromExile) {
        checkControllerPlaysLandTriggers(gameData, playingPlayerId, landCard,
                fromExile ? Zone.EXILE : Zone.HAND);
    }

    public void checkControllerPlaysLandTriggers(GameData gameData, UUID playingPlayerId, Card landCard,
                                                 Zone playZone) {
        var ctx = new TriggerContext.LandPlayed(playingPlayerId, landCard, playZone);
        if (playZone != Zone.HAND) {
            gameData.playersWhoPlayedOrCastFromOutsideHandThisTurn.add(playingPlayerId);
        }
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(playingPlayerId)) {
                dispatchSlot(gameData, perm, playerId, EffectSlot.ON_CONTROLLER_PLAYS_LAND, ctx);
            } else {
                dispatchSlot(gameData, perm, playerId, EffectSlot.ON_OPPONENT_PLAYS_LAND, ctx);
            }
        });
    }

    /**
     * "Whenever a land enters under your control" (ON_ALLY_LAND_ENTERS_BATTLEFIELD, e.g. Landfall).
     * Bundles all of a permanent's effects into a single stack entry (one landfall trigger).
     */
    public void checkAllyLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(landControllerId);
        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                gameData, landControllerId, landControllerId, enteringLand);
        UUID enteringPermanentId = null;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard() == enteringLand) {
                enteringPermanentId = permanent.getId();
                break;
            }
        }
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringLand) continue;
            if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, landControllerId)) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            List<CardEffect> resolvedEffects = new ArrayList<>();
            for (CardEffect effect : effects) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringLand, gameData, landControllerId);
                if (resolved == null) continue;
                if (resolved instanceof ConditionalEffect conditional
                        && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, landControllerId)
                                        .withTriggeringCard(enteringLand)
                                        .withTriggeringPermanentId(enteringPermanentId))) {
                    log.info("Game {} - {} ally-land trigger skipped ({}) not met",
                            gameData.id, perm.getCard().getName(), conditional.conditionName());
                    continue;
                }
                resolvedEffects.add(resolved);
            }
            if (resolvedEffects.isEmpty()) continue;
            int permanentTriggerCount = triggerCount
                    + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                    gameData, landControllerId, perm);

            boolean needsPlayerTarget = resolvedEffects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            boolean needsPermanentTarget = resolvedEffects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
            if (needsPlayerTarget || needsPermanentTarget) {
                int targetGroupIndex = resolvedEffects.stream()
                        .mapToInt(perm.getCard()::getEffectTargetIndex)
                        .filter(index -> index >= 0 && index < perm.getCard().getSpellTargets().size())
                        .findFirst()
                        .orElse(-1);
                TargetFilter targetFilter = targetGroupIndex >= 0
                        ? perm.getCard().getSpellTargets().get(targetGroupIndex).getFilter()
                        : perm.getCard().getTargetFilter();
                for (int i = 0; i < permanentTriggerCount; i++) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            perm.getCard(),
                            landControllerId,
                            resolvedEffects,
                            needsPlayerTarget && !needsPermanentTarget,
                            targetFilter,
                            0,
                            perm.getId(),
                            enteringPermanentId
                    ));
                    gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                            "'s landfall ability triggers — choose a target."));
                    log.info("Game {} - {} landfall trigger queued for target selection",
                            gameData.id, perm.getCard().getName());
                }
                continue;
            }

            if (resolvedEffects.size() == 1 && resolvedEffects.getFirst() instanceof ChooseOneEffect chooseOneEffect) {
                for (int i = 0; i < permanentTriggerCount; i++) {
                    gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                            perm.getCard(), landControllerId, chooseOneEffect, perm.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on ally land entering", gameData.id, perm.getCard().getName());
                }
                continue;
            }

            for (int i = 0; i < permanentTriggerCount; i++) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        landControllerId,
                        perm.getCard().getName() + "'s ability",
                        resolvedEffects,
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ally land entering", gameData.id, perm.getCard().getName());
            }
        }

        collectEmblemAllyLandEntersTriggers(gameData, landControllerId, enteringLand, triggerCount);

        // Graveyard-resident landfall triggers (GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD, e.g. Reach of Branches).
        List<Card> graveyard = gameData.playerGraveyards.get(landControllerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> effects = gameQueryService.getEffectiveGraveyardEffects(
                        gameData, card, EffectSlot.GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    CardEffect resolved = unwrapTriggeringCardConditional(effect, enteringLand, gameData, landControllerId);
                    if (resolved == null) continue;

                    if (resolved instanceof MayEffect may) {
                        gameData.queueMayAbility(card, landControllerId, may);
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                landControllerId,
                                card.getName() + "'s ability",
                                new ArrayList<>(List.of(resolved))
                        ));
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    log.info("Game {} - {} graveyard landfall trigger queued", gameData.id, card.getName());
                }
            }
        }

        collectExiledVoyageTriggers(gameData, landControllerId);
    }

    private void collectExiledVoyageTriggers(GameData gameData, UUID landControllerId) {
        for (var exiled : new ArrayList<>(gameData.exiledCards)) {
            UUID cardId = exiled.card().getId();
            UUID controllerId = gameData.exiledVoyageControllerIds.get(cardId);
            if (!gameData.exiledVoyageCounters.containsKey(cardId)) {
                continue;
            }
            if (gameData.findExiledCard(cardId) == null || controllerId == null) {
                gameData.exiledVoyageCounters.remove(cardId);
                gameData.exiledVoyageControllerIds.remove(cardId);
                continue;
            }
            if (!landControllerId.equals(controllerId)) {
                continue;
            }

            gameData.queueMayAbility(exiled.card(), controllerId, new MayEffect(
                    new PutVoyageCounterOnExiledCardEffect(),
                    "Put a voyage counter on Cosima?",
                    new ReturnVoyagingCardFromExileEffect()));
            gameLogService.append(gameData, GameLog.abilityTriggers(exiled.card()));
            log.info("Game {} - {} exiled voyage landfall trigger queued", gameData.id,
                    exiled.card().getName());
        }
    }

    private void collectEmblemAllyLandEntersTriggers(GameData gameData, UUID landControllerId,
                                                      Card enteringLand, int triggerCount) {
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(landControllerId)) continue;

            for (CardEffect effect : emblem.staticEffects()) {
                Card source = emblem.sourceCard();
                Card sourceCard = source != null ? source : enteringLand;
                String description = (source != null ? source.getName() : "Emblem") + "'s emblem";
                if (effect instanceof DrawCardOnAllyLandEntersEffect) {
                    for (int i = 0; i < triggerCount; i++) {
                        gameData.queueMayAbility(sourceCard, emblem.controllerId(),
                                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
                        gameLogService.append(gameData, GameLog.text(description + " triggers."));
                        log.info("Game {} - {} landfall draw trigger queued", gameData.id, description);
                    }
                } else if (effect instanceof DealDamageToAnyTargetOnAllyLandEntersEffect damageTrigger
                        && enteringLand.getSubtypes().contains(damageTrigger.landSubtype())) {
                    for (int i = 0; i < triggerCount; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                                sourceCard,
                                emblem.controllerId(),
                                new ArrayList<>(List.of(new DealDamageToAnyTargetEffect(damageTrigger.damage())))
                        ));
                        gameLogService.append(gameData,
                                GameLog.text(description + " triggers — choose a target for "
                                        + damageTrigger.damage() + " damage."));
                        log.info("Game {} - {} landfall damage trigger queued ({})",
                                gameData.id, description, damageTrigger.damage());
                    }
                }
            }
        }
    }

    /**
     * "Whenever a creature enters from a graveyard" (ON_CREATURE_ENTERS_FROM_GRAVEYARD).
     */
    public void checkEntersFromGraveyardTriggers(GameData gameData, UUID enteringControllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        Permanent enteringPermanent = null;
        List<Permanent> controllerBf = gameData.playerBattlefields.get(enteringControllerId);
        if (controllerBf != null) {
            for (Permanent p : controllerBf) {
                if (p.getCard() == enteringCreature) {
                    enteringPermanent = p;
                    break;
                }
            }
        }
        if (enteringPermanent == null || enteringPermanent.getEnteredFromGraveyardOwnerId() == null) {
            return;
        }

        UUID graveyardOwnerId = enteringPermanent.getEnteredFromGraveyardOwnerId();

        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(graveyardOwnerId)) return;
            if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, playerId)) return;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_CREATURE_ENTERS_FROM_GRAVEYARD);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                        gameData, playerId, enteringControllerId, enteringCreature);
                registry.dispatch(
                        new TriggerMatchContext(gameData, perm, playerId, effect),
                        EffectSlot.ON_CREATURE_ENTERS_FROM_GRAVEYARD,
                        effect,
                        new TriggerContext.PermanentEnters(
                                enteringCreature, enteringControllerId, null, triggerCount, null));
            }
        });
    }

    /**
     * "Whenever this creature or another permanent enters from a graveyard"
     * (ON_PERMANENT_ENTERS_FROM_GRAVEYARD). Unlike {@link #checkEntersFromGraveyardTriggers}, fires for
     * ANY permanent (not just creatures) entering from ANY graveyard, and queues a non-targeting stack
     * entry for each source's controller rather than a target choice. Used by River Kelpie.
     */
    public void checkPermanentEntersFromGraveyardTriggers(GameData gameData, UUID enteringControllerId, Card enteringPermanentCard) {
        Permanent enteringPermanent = null;
        List<Permanent> controllerBf = gameData.playerBattlefields.get(enteringControllerId);
        if (controllerBf != null) {
            for (Permanent p : controllerBf) {
                if (p.getCard() == enteringPermanentCard) {
                    enteringPermanent = p;
                    break;
                }
            }
        }
        if (enteringPermanent == null || enteringPermanent.getEnteredFromGraveyardOwnerId() == null) {
            return;
        }

        gameData.forEachPermanent((playerId, perm) -> {
            if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, playerId)) return;
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_PERMANENT_ENTERS_FROM_GRAVEYARD);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                        gameData, playerId, enteringControllerId, enteringPermanentCard);
                for (int i = 0; i < triggerCount; i++) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                    gameLogService.append(gameData, GameLog.cardTextCard(perm.getCard(), "'s ability triggers (",
                            enteringPermanentCard, " entered from a graveyard)."));
                    log.info("Game {} - {} triggers ({} entered from graveyard)",
                            gameData.id, perm.getCard().getName(), enteringPermanentCard.getName());
                }
            }
        });
    }

    /**
     * "Whenever a permanent you control enters from exile" (ON_PERMANENT_ENTERS_FROM_EXILE).
     * Fires for any permanent entering directly from exile under the source controller's control.
     */
    public void checkPermanentEntersFromExileTriggers(GameData gameData, UUID enteringControllerId,
                                                      Card enteringPermanentCard) {
        Permanent enteringPermanent = null;
        List<Permanent> controllerBf = gameData.playerBattlefields.get(enteringControllerId);
        if (controllerBf != null) {
            for (Permanent p : controllerBf) {
                if (p.getCard() == enteringPermanentCard) {
                    enteringPermanent = p;
                    break;
                }
            }
        }
        if (enteringPermanent == null || !enteringPermanent.isEnteredFromExile()) {
            return;
        }

        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(enteringControllerId)) return;
            if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, playerId)) return;
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_PERMANENT_ENTERS_FROM_EXILE);
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                        gameData, playerId, enteringControllerId, enteringPermanentCard);
                for (int i = 0; i < triggerCount; i++) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                    gameLogService.append(gameData, GameLog.cardTextCard(perm.getCard(), "'s ability triggers (",
                            enteringPermanentCard, " entered from exile)."));
                    log.info("Game {} - {} triggers ({} entered from exile)",
                            gameData.id, perm.getCard().getName(), enteringPermanentCard.getName());
                }
            }
        });
    }

    /**
     * "When this creature enters from a graveyard" (ON_SELF_ENTERS_FROM_GRAVEYARD). Unlike the two
     * methods above this fires only for the entering permanent's own ability. A targeting effect
     * picks its target as the ability goes on the stack (CR 603.3b) — the permanent was never cast,
     * so no target was chosen at cast time — reusing the ETB token-target pipeline with the card's
     * {@code target(...)} filter. Used by Treacherous Pit-Dweller.
     */
    public void checkSelfEntersFromGraveyardTriggers(GameData gameData, UUID enteringControllerId, Card enteringCard) {
        List<CardEffect> effects = enteringCard.getEffects(EffectSlot.ON_SELF_ENTERS_FROM_GRAVEYARD);
        if (effects == null || effects.isEmpty()) return;

        Permanent enteringPermanent = null;
        List<Permanent> controllerBf = gameData.playerBattlefields.get(enteringControllerId);
        if (controllerBf != null) {
            for (Permanent p : controllerBf) {
                if (p.getCard() == enteringCard) {
                    enteringPermanent = p;
                    break;
                }
            }
        }
        if (enteringPermanent == null || enteringPermanent.getEnteredFromGraveyardOwnerId() == null) {
            return;
        }
        if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, enteringControllerId)) {
            return;
        }

        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                gameData, enteringControllerId, enteringControllerId, enteringCard);
        for (CardEffect effect : effects) {
            boolean targets = effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
            if (targets) {
                for (int i = 0; i < triggerCount; i++) {
                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                            enteringCard, enteringControllerId, new ArrayList<>(List.of(effect)),
                            enteringPermanent.getId(), enteringCard.getTargetFilter()));
                }
            } else {
                for (int i = 0; i < triggerCount; i++) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            enteringCard,
                            enteringControllerId,
                            enteringCard.getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            null,
                            enteringPermanent.getId()
                    ));
                }
            }
            gameLogService.append(gameData, GameLog.cardThen(enteringCard,
                    "'s ability triggers (it entered from a graveyard)."));
            log.info("Game {} - {} triggers (it entered from a graveyard)", gameData.id, enteringCard.getName());
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
        }
    }

    public void checkGraveyardCreatureEntersFromGraveyardTriggers(GameData gameData,
                                                                   UUID enteringControllerId,
                                                                   Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        if (enteringPermanent == null) return;

        UUID enteredFromGraveyardOwnerId = enteringPermanent.getEnteredFromGraveyardOwnerId();
        boolean castFromGraveyard = enteringPermanent.isCast()
                && enteringPermanent.getCastFromZone() == Zone.GRAVEYARD;
        if (enteredFromGraveyardOwnerId == null && !castFromGraveyard) return;

        gameData.playerGraveyards.forEach((graveyardOwnerId, graveyard) -> {
            boolean enteredFromThisGraveyard = graveyardOwnerId.equals(enteredFromGraveyardOwnerId)
                    || (castFromGraveyard && graveyardOwnerId.equals(enteringControllerId));
            if (!enteredFromThisGraveyard) return;

            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> effects = gameQueryService.getEffectiveGraveyardEffects(
                        gameData, card, EffectSlot.GRAVEYARD_ON_CREATURE_ENTERS_FROM_GRAVEYARD_OR_CAST_FROM_GRAVEYARD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            graveyardOwnerId,
                            card.getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    log.info("Game {} - {} triggers ({} entered from its graveyard or was cast from it)",
                            gameData.id, card.getName(), enteringCreature.getName());
                }
            }
        });
    }

    private boolean dispatchEnter(GameData gameData, Permanent perm, UUID controllerId, EffectSlot slot,
                                  CardEffect effect, TriggerContext.PermanentEnters ctx) {
        var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
        return dispatch(match, slot, effect, ctx);
    }

    private boolean dispatchEnter(GameData gameData, Permanent perm, UUID controllerId, EffectSlot slot,
                                  CardEffect rawEffect, CardEffect resolvedEffect,
                                  TriggerContext.PermanentEnters ctx) {
        var match = new TriggerMatchContext(gameData, perm, controllerId, rawEffect);
        return dispatch(match, slot, resolvedEffect, ctx);
    }

    private boolean dispatch(TriggerMatchContext match, EffectSlot slot, CardEffect effect,
                             TriggerContext context) {
        if (context instanceof TriggerContext.PermanentEnters
                && gameQueryService.areOpponentPermanentETBTriggersSuppressed(
                match.gameData(), match.controllerId())) {
            return false;
        }
        int previousCopies = match.gameData().beginTriggeredAbilityCopies(1 +
                gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        match.gameData(), match.controllerId(), match.permanent(),
                        isDirectAttackTriggerSlot(slot))
                + (context.causedByCreatureDying()
                ? gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(
                        match.gameData(), match.controllerId(), match.permanent())
                : 0));
        try {
            return registry.dispatch(match, slot, effect, context);
        } finally {
            match.gameData().restoreTriggeredAbilityCopies(previousCopies);
        }
    }

    private boolean isDirectAttackTriggerSlot(EffectSlot slot) {
        return switch (slot) {
            case ON_ATTACK, ON_ALLY_CREATURES_ATTACK, ON_ALLY_CREATURES_ATTACK_PLAYER,
                    ON_ALLY_CREATURE_ATTACKS,
                    ON_CREATURE_ATTACKS_YOU, ON_CREATURES_ATTACK_YOU,
                    ON_ANY_CREATURE_ATTACKS, ON_ANY_PLAYER_ATTACKS -> true;
            default -> false;
        };
    }

    private void collectEvolveTrigger(GameData gameData, UUID controllerId, Permanent source,
                                      Permanent enteringPermanent, int triggerCount) {
        if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
            return;
        }
        int enteringPower = gameQueryService.getEffectivePower(gameData, enteringPermanent);
        int enteringToughness = gameQueryService.getEffectiveToughness(gameData, enteringPermanent);
        if (enteringPower <= gameQueryService.getEffectivePower(gameData, source)
                && enteringToughness <= gameQueryService.getEffectiveToughness(gameData, source)) {
            return;
        }

        for (int i = 0; i < triggerCount; i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new EvolveTriggerEffect())),
                    0,
                    source.getId());
            entry.setTriggeringPermanentId(enteringPermanent.getId());
            entry.setTriggeringPermanentPowerAtTrigger(enteringPower);
            entry.setTriggeringPermanentToughnessAtTrigger(enteringToughness);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
            log.info("Game {} - {} triggers evolve for {} entering", gameData.id,
                    source.getCard().getName(), enteringPermanent.getCard().getName());
        }
    }

    private Permanent findPermanentByCard(GameData gameData, Card card) {
        Permanent[] found = new Permanent[1];
        gameData.forEachPermanent((playerId, permanent) -> {
            if (found[0] == null && permanent.getCard() == card) {
                found[0] = permanent;
            }
        });
        return found[0];
    }

    /**
     * Gates a stat-based enter trigger whose condition is computable from the entering creature
     * alone (e.g. Garruk's Packleader), returning the wrapped effect if it fires, {@code null} to
     * skip, or the original effect when it wasn't wrapped.
     */
    private CardEffect unwrapEnterCreatureConditional(GameData gameData, Card enteringCreature,
                                                      Permanent source, CardEffect effect) {
        if (effect instanceof EnterCreatureConditionalEffect conditional) {
            Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
            if (!conditional.testEnteringPermanent(enteringPermanent)) {
                return null;
            }
            log.info("Game {} - {} triggers for {} entering ({})",
                    gameData.id, source.getCard().getName(),
                    enteringCreature.getName(), conditional.triggerDescription(enteringCreature));
            return conditional.wrapped();
        }
        return effect;
    }

    private CardEffect unwrapOncePerTurnTrigger(GameData gameData, Permanent source, CardEffect effect) {
        if (!(effect instanceof OncePerTurnTriggerEffect once)) {
            return effect;
        }
        if (gameData.oncePerTurnTriggersFiredThisTurn.contains(source.getId())) {
            return null;
        }
        return once.wrapped();
    }

    private boolean passesEnterInterveningIf(GameData gameData, Permanent source,
                                             UUID controllerId, Permanent enteringPermanent,
                                             CardEffect effect) {
        if (!(effect instanceof ConditionalEffect conditional) || !conditional.interveningIf()) {
            return true;
        }
        ConditionContext context = ConditionContext.forPermanent(source, controllerId);
        if (enteringPermanent != null) {
            context = context.withTriggeringPermanentId(enteringPermanent.getId())
                    .withTriggeringPermanentPowerAtTrigger(
                            gameQueryService.getEffectivePower(gameData, enteringPermanent));
        }
        return conditionEvaluationService.isMet(gameData, conditional.condition(),
                context);
    }

    private CardEffect unwrapLifeChangeConditional(GameData gameData, Permanent source,
                                                    UUID controllerId, CardEffect effect) {
        if (!(effect instanceof ConditionalEffect conditional)) {
            return effect;
        }
        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forPermanent(source, controllerId))) {
            return null;
        }
        return conditional.wrapped();
    }

    /**
     * Gates an enter trigger on the entering permanent's name matching the source's imprinted card
     * (Invader Parasite).
     */
    private CardEffect unwrapImprintedCardNameConditional(GameData gameData, Card enteringCard, Permanent source, CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof ImprintedCardNameMatchesEnteringPermanent) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
            ConditionContext ctx = ConditionContext.forPermanent(source, controllerId).withTriggeringCard(enteringCard);
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(), ctx)) {
                return null;
            }
            return conditional.wrapped();
        }
        return effect;
    }

    /**
     * Gates an enter trigger on at least {@code minCount} permanents matching a predicate having
     * entered under {@code affectedPlayerId}'s control this turn (Landfall count).
     */
    private CardEffect unwrapPermanentEnteredThisTurnConditional(GameData gameData, UUID affectedPlayerId, CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof PermanentEnteredThisTurn) {
            ConditionContext ctx = new ConditionContext(affectedPlayerId, null, null, null,
                    false, false, false, false, null, 0, null, null, false);
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(), ctx)) {
                return null;
            }
            return conditional.wrapped();
        }
        return effect;
    }

    // ── Internal dispatch ──────────────────────────────────────────────

    private boolean dispatchSlot(GameData gameData, Permanent perm, UUID controllerId, EffectSlot slot, TriggerContext ctx) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return false;
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
        if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()) return false;
        boolean triggered = false;
        for (CardEffect effect : perm.getCard().getEffects(slot)) {
            if (slot == EffectSlot.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE
                    && !passesPermanentCardPutIntoGraveyardInterveningIf(gameData, perm, controllerId, effect)) {
                continue;
            }
            if (dispatchSlotEffect(gameData, perm, controllerId, slot, ctx, effect)) {
                triggered = true;
            }
        }
        // Triggered abilities granted continuously by another permanent (e.g. Pontiff of Blight
        // giving other creatures you control extort) fire off the permanent that has them.
        for (CardEffect effect : grantedTriggeredAbilitySupport.grantedTriggeredEffects(gameData, perm, slot)) {
            if (slot == EffectSlot.ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE
                    && !passesPermanentCardPutIntoGraveyardInterveningIf(gameData, perm, controllerId, effect)) {
                continue;
            }
            if (dispatchSlotEffect(gameData, perm, controllerId, slot, ctx, effect)) {
                triggered = true;
            }
        }
        return triggered;
    }

    private boolean dispatchSlotEffect(GameData gameData, Permanent perm, UUID controllerId,
            EffectSlot slot, TriggerContext ctx, CardEffect effect) {
        CardEffect conditionedEffect = effect;
        if (effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof SourceHasChosenMode) {
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forPermanent(perm, controllerId))) {
                return false;
            }
            conditionedEffect = conditional.wrapped();
        }

        CardEffect toDispatch = unwrapOncePerTurnTrigger(gameData, perm, conditionedEffect);
        if (toDispatch == null) return false;

        boolean dispatched = dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect),
                slot, toDispatch, ctx);
        if (dispatched && toDispatch != conditionedEffect
                && !(conditionedEffect instanceof OncePerTurnTriggerEffect once && once.markOnAcceptance())) {
            gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
        }
        return dispatched;
    }

    private boolean passesPermanentCardPutIntoGraveyardInterveningIf(GameData gameData, Permanent source,
                                                                     UUID controllerId, CardEffect effect) {
        if (!(effect instanceof ConditionalEffect conditional) || !conditional.interveningIf()) return true;
        return conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forPermanent(source, controllerId));
    }

    /**
     * Increment keyword. For each permanent the casting player controls with the {@link Keyword#INCREMENT}
     * keyword, fire the trigger if the mana spent on the cast spell is greater than that creature's current
     * power or toughness (the intervening-if; re-checked again at resolution per CR 603.4). The mana spent is
     * snapshotted into the stack entry's {@code xValue} for the resolution handler.
     */
    private void collectIncrementTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        int manaSpent = gameData.getSpellCastManaSpent(spellCard.getId());
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(castingPlayerId)) return;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
            if (!gameQueryService.hasKeyword(gameData, perm, Keyword.INCREMENT)) return;
            if (manaSpent <= gameQueryService.getEffectivePower(gameData, perm)
                    && manaSpent <= gameQueryService.getEffectiveToughness(gameData, perm)) return;

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    castingPlayerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new IncrementTriggerEffect())),
                    manaSpent,
                    perm.getId()
            ));
            log.info("Game {} - {} increment trigger queued (mana spent {})",
                    gameData.id, perm.getCard().getName(), manaSpent);
        });
    }

    /**
     * Unwraps triggering-card conditionals if present.
     * Returns the inner effect if the triggering card matches the predicate,
     * {@code null} if the condition is not met (caller should skip),
     * or the original effect unchanged if it wasn't wrapped.
     */
    CardEffect unwrapTriggeringCardConditional(CardEffect effect, Card triggeringCard,
                                               GameData gameData, UUID controllerId) {
        if (effect instanceof TriggeringCardConditionalEffect conditional) {
            if (!predicateEvaluationService.matchesCardPredicate(triggeringCard, conditional.predicate(), null,
                    gameData, controllerId)) {
                return null;
            }
            return conditional.wrapped();
        }
        return effect;
    }

    /**
     * Unwraps death-trigger conditionals that reference the dying creature's card or
     * on-battlefield characteristics (e.g. power/toughness).
     */
    CardEffect unwrapCreatureDeathConditional(CardEffect effect, Card dyingCard, Permanent dyingPermanent,
                                              GameData gameData, UUID controllerId) {
        return unwrapCreatureDeathConditional(effect, dyingCard, dyingPermanent, gameData, controllerId, null);
    }

    private CardEffect unwrapCreatureDeathConditional(CardEffect effect, Card dyingCard, Permanent dyingPermanent,
                                                      GameData gameData, UUID controllerId, Permanent watcher) {
        if (effect instanceof TriggeringCardConditionalEffect conditional) {
            if (!predicateEvaluationService.matchesCardPredicate(dyingCard, conditional.predicate(), null,
                    gameData, controllerId)) {
                return null;
            }
            return conditional.wrapped();
        }
        if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
            Permanent perm = dyingPermanent != null ? dyingPermanent : new Permanent(dyingCard);
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceControllerId(controllerId);
            if (watcher != null) {
                filterContext = filterContext
                        .withSourceCardId(watcher.getOriginalCard().getId())
                        .withSourcePermanentSnapshot(watcher);
            }
            boolean matches = predicateEvaluationService.matchesPermanentPredicate(
                    perm, conditional.predicate(), filterContext);
            if (!matches) {
                return null;
            }
            return conditional.wrapped();
        }
        return effect;
    }

    private boolean passesInterveningIf(GameData gameData, Permanent source, UUID controllerId,
                                        CardEffect effect) {
        return passesInterveningIf(gameData, source, controllerId, effect, null, null);
    }

    private boolean passesInterveningIf(GameData gameData, Permanent source, UUID controllerId,
                                        CardEffect effect, UUID triggeringPermanentId,
                                        Integer triggeringPermanentPowerAtTrigger) {
        if (!(effect instanceof ConditionalEffect conditional) || !conditional.interveningIf()) {
            return true;
        }
        return conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forPermanent(source, controllerId)
                        .withTriggeringPermanentId(triggeringPermanentId)
                        .withTriggeringPermanentPowerAtTrigger(triggeringPermanentPowerAtTrigger));
    }

    public void checkPlotTriggers(GameData gameData, UUID plottingPlayerId, Card plottedCard) {
        List<CardEffect> effects = plottedCard.getEffects(EffectSlot.ON_SELF_BECOMES_PLOTTED);
        if (effects.isEmpty()) return;
        boolean needsAnyTarget = effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        if (needsAnyTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.PlotTriggerAnyTarget(
                    plottedCard, plottingPlayerId, new ArrayList<>(effects)));
        } else {
            gameData.enqueueTrigger(new StackEntry(StackEntryType.TRIGGERED_ABILITY, plottedCard,
                    plottingPlayerId, plottedCard.getName() + "'s ability", new ArrayList<>(effects)));
        }
        gameLogService.append(gameData, GameLog.cardThen(plottedCard,
                " became plotted — its ability triggers!"));
    }

    public void processNextPlotTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextPlotTrigger(gameData);
    }

    public void checkBecomesSaddledTriggers(GameData gameData, Permanent saddledPermanent,
                                             UUID controllerId) {
        TriggerContext context = new TriggerContext.SelfBecomesSaddled(controllerId);
        for (CardEffect effect : saddledPermanent.getCard().getEffects(EffectSlot.ON_SELF_BECOMES_SADDLED)) {
            registry.dispatch(new TriggerMatchContext(gameData, saddledPermanent, controllerId, effect),
                    EffectSlot.ON_SELF_BECOMES_SADDLED, effect, context);
        }
    }

    public void checkAllySourceDealtNoncombatDamageToCreatureTriggers(
            GameData gameData, UUID sourceControllerId, Permanent damagedCreature, int damageDealt) {
        if (sourceControllerId == null || damagedCreature == null || damageDealt <= 0) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;

        TriggerContext context = new TriggerContext.SourceDealsNoncombatDamageToCreature(
                damagedCreature, damageDealt, sourceControllerId);
        for (Permanent watcher : List.copyOf(battlefield)) {
            for (CardEffect effect : watcher.getCard().getEffects(
                    EffectSlot.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE)) {
                registry.dispatch(new TriggerMatchContext(gameData, watcher, sourceControllerId, effect),
                        EffectSlot.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE, effect, context);
            }
        }
    }

    public void checkBatchedAllyCreatureDeathTriggers(GameData gameData) {
        if (gameData.simultaneousDyingCreatures.isEmpty()) return;
        List<Map.Entry<UUID, Permanent>> dyingCreatures =
                new ArrayList<>(gameData.simultaneousDyingCreatures.entrySet());
        if (dyingCreatures.stream().anyMatch(entry ->
                gameQueryService.areCreatureDeathTriggersSuppressed(gameData, entry.getValue()))) {
            return;
        }
        for (Map.Entry<UUID, List<Permanent>> battlefield : gameData.playerBattlefields.entrySet()) {
            for (Permanent watcher : battlefield.getValue()) {
                collectBatchedAllyCreatureDeathTriggers(gameData, watcher, battlefield.getKey(), dyingCreatures);
            }
        }
        for (Map.Entry<UUID, Permanent> dyingEntry : dyingCreatures) {
            UUID controllerId = gameData.simultaneousDyingControllers.get(dyingEntry.getKey());
            if (controllerId != null) {
                collectBatchedAllyCreatureDeathTriggers(gameData, dyingEntry.getValue(), controllerId,
                        dyingCreatures);
                collectBatchedOncePerTurnAllyNontokenDeathTrigger(
                        gameData, dyingEntry.getValue(), controllerId, dyingCreatures);
            }
        }
    }

    private void collectBatchedOncePerTurnAllyNontokenDeathTrigger(
            GameData gameData, Permanent watcher, UUID watcherControllerId,
            List<Map.Entry<UUID, Permanent>> dyingCreatures) {
        if (gameData.oncePerTurnTriggersFiredThisTurn.contains(watcher.getId())) return;

        Map.Entry<UUID, Permanent> firstOtherNontokenDeath = dyingCreatures.stream()
                .filter(entry -> !entry.getKey().equals(watcher.getId()))
                .filter(entry -> watcherControllerId.equals(
                        gameData.simultaneousDyingControllers.get(entry.getKey())))
                .filter(entry -> !entry.getValue().getCard().isToken())
                .findFirst().orElse(null);
        if (firstOtherNontokenDeath == null) return;

        Permanent dyingPermanent = firstOtherNontokenDeath.getValue();
        TriggerContext context = new TriggerContext.CreatureDeath(
                dyingPermanent.getCard(), watcherControllerId,
                Math.max(0, dyingPermanent.getEffectivePower()),
                dyingPermanent.getEffectiveToughness(), dyingPermanent.getId(), dyingPermanent);
        for (CardEffect effect : watcher.getCard().getEffects(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES)) {
            if (effect instanceof OncePerTurnTriggerEffect) {
                dispatchSlotEffect(gameData, watcher, watcherControllerId,
                        EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, context, effect);
            }
        }
    }

    private void collectBatchedAllyCreatureDeathTriggers(GameData gameData, Permanent watcher,
                                                          UUID watcherControllerId,
                                                          List<Map.Entry<UUID, Permanent>> dyingCreatures) {
        List<CardEffect> effects = watcher.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_DIES);
        if (effects == null || effects.isEmpty()) return;
        Map.Entry<UUID, Permanent> firstOtherDeath = dyingCreatures.stream()
                .filter(entry -> !entry.getKey().equals(watcher.getId()))
                .filter(entry -> watcherControllerId.equals(
                        gameData.simultaneousDyingControllers.get(entry.getKey())))
                .findFirst().orElse(null);
        if (firstOtherDeath == null) return;
        Permanent dyingPermanent = firstOtherDeath.getValue();
        Card dyingCard = dyingPermanent.getCard();
        int dyingPower = Math.max(0, dyingPermanent.getEffectivePower());
        TriggerContext context = new TriggerContext.CreatureDeath(dyingCard, watcherControllerId,
                dyingPower, dyingPermanent.getEffectiveToughness(), dyingPermanent.getId(), dyingPermanent);
        List<CardEffect> stackEffects = new ArrayList<>();
        for (CardEffect effect : effects) {
            CardEffect resolved = unwrapCreatureDeathConditional(
                    effect, dyingCard, dyingPermanent, gameData, watcherControllerId, watcher);
            if (!(resolved instanceof BatchedCreatureDeathTriggerEffect batched)) continue;
            resolved = batched.wrapped();
            if (!passesInterveningIf(gameData, watcher, watcherControllerId, resolved,
                    dyingPermanent.getId(), dyingPower)) continue;
            if (resolved.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                        watcher.getCard(), watcherControllerId, new ArrayList<>(List.of(resolved)), dyingPower));
            } else if (resolved instanceof MayPayManaEffect || resolved instanceof MayEffect) {
                dispatch(new TriggerMatchContext(gameData, watcher, watcherControllerId, resolved),
                        EffectSlot.ON_ALLY_CREATURE_DIES, resolved, context);
            } else {
                if (resolved instanceof DyingCreatureCardAwareEffect aware) {
                    resolved = aware.boundToDyingCard(dyingCard.getId());
                }
                if (resolved instanceof DyingCreatureCounterAwareEffect aware) {
                    resolved = aware.boundToDyingCreatureCounterCount(countCountersOnPermanent(dyingPermanent));
                }
                stackEffects.add(resolved);
            }
        }
        if (stackEffects.isEmpty()) return;
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, watcher.getCard(),
                watcherControllerId, watcher.getCard().getName() + "'s ability",
                stackEffects, null, watcher.getId());
        entry.setSourcePermanentSnapshot(new Permanent(watcher));
        entry.setTriggeringPermanentId(dyingPermanent.getId());
        entry.setTriggeringPermanentPowerAtTrigger(dyingPower);
        gameData.stack.add(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
    }

    private int countCountersOnPermanent(Permanent permanent) {
        return snapshotCountersOnPermanent(permanent).values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    private Map<CounterType, Integer> snapshotCountersOnPermanent(Permanent permanent) {
        Map<CounterType, Integer> counters = new EnumMap<>(CounterType.class);
        for (CounterType type : CounterType.values()) {
            if (type == CounterType.ANY || type == CounterType.SILVER) {
                continue;
            }
            int count = permanent.getCounterCount(type);
            if (count > 0) {
                counters.put(type, count);
            }
        }
        return Map.copyOf(counters);
    }
}
