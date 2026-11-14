package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.rules.CounterChangeEvent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RemoveTimeCounterFromExiledCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameQueryService gameQueryService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;

    public RemoveTimeCounterFromExiledCardEffectHandler(GameLogService gameLogService,
            @Lazy TriggerCollectionService triggerCollectionService,
            GameQueryService gameQueryService,
            @Lazy TriggeredAbilityStackService triggeredAbilityStackService) {
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
        this.gameQueryService = gameQueryService;
        this.triggeredAbilityStackService = triggeredAbilityStackService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveTimeCounterFromExiledCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = ((RemoveTimeCounterFromExiledCardEffect) effect).cardId();
        removeTimeCounter(gameData, cardId);
    }

    public void removeTimeCounter(GameData gameData, UUID cardId) {
        removeTimeCounter(gameData, cardId, null);
    }

    /** Removes a time counter on behalf of {@code removerId} ({@code null} when its owner removes it). */
    public void removeTimeCounter(GameData gameData, UUID cardId, UUID removerId) {
        ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
        Integer counters = gameData.exiledCardTimeCounters.get(cardId);
        if (exiledEntry == null || counters == null || counters <= 0) {
            gameData.exiledCardTimeCounters.remove(cardId);
            return;
        }

        if (counters > 1) {
            int remaining = counters - 1;
            gameData.exiledCardTimeCounters.put(cardId, remaining);
            gameLogService.append(gameData,
                    GameLog.cardThen(exiledEntry.card(), " has a time counter removed (" + remaining + " remaining)."));
            gameData.rulesEvents.occur(new CounterChangeEvent(CounterChangeEvent.Kind.TIME_COUNTER_REMOVED_IN_EXILE,
                    null, null, exiledEntry.card(), exiledEntry.ownerId(), removerId, CounterType.TIME, remaining, 0,
                            false, null));
            return;
        }

        boolean hasAlaundoCastAbility = gameData.exiledCardsWithAlaundoCastAbility.contains(cardId);
        boolean hasSuspend = !gameData.exiledCardsWithNonSuspendTimeCounters.contains(cardId)
                || exiledEntry.card().getActivatedAbilities(Zone.HAND).stream()
                .anyMatch(com.github.laxika.magicalvibes.model.ActivatedAbility::isSuspendsSourceFromHand);
        gameData.exiledCardTimeCounters.remove(cardId);
        gameData.rulesEvents.occur(new CounterChangeEvent(CounterChangeEvent.Kind.TIME_COUNTER_REMOVED_IN_EXILE,
                null, null, exiledEntry.card(), exiledEntry.ownerId(), removerId, CounterType.TIME, 0, 0, false, null));
        if (!hasAlaundoCastAbility && !hasSuspend) {
            return;
        }
        if (hasAlaundoCastAbility) {
            queueCastTrigger(gameData, exiledEntry, false);
        }
        if (hasSuspend) {
            queueCastTrigger(gameData, exiledEntry, true);
        }
        gameLogService.append(gameData,
                GameLog.cardThen(exiledEntry.card(), " has no time counters left; its suspend ability may be cast."));
        log.info("Game {} - {} has no time counters left and may be cast from suspend",
                gameData.id, exiledEntry.card().getName());
    }

    public static boolean isSuspended(GameData gameData, ExiledCardEntry exiled) {
        if (exiled.faceDown()) {
            return false;
        }
        UUID cardId = exiled.card().getId();
        Integer counters = gameData.exiledCardTimeCounters.get(cardId);
        boolean hasSuspend = !gameData.exiledCardsWithNonSuspendTimeCounters.contains(cardId)
                || exiled.card().getActivatedAbilities(Zone.HAND).stream()
                .anyMatch(com.github.laxika.magicalvibes.model.ActivatedAbility::isSuspendsSourceFromHand);
        return (counters != null && counters > 0 && hasSuspend)
                || gameData.suspendedSpellExiles.stream().anyMatch(
                suspended -> suspended.cardId().equals(cardId) && suspended.counters() > 0);
    }

    public void queueCastTrigger(GameData gameData, ExiledCardEntry exiledEntry, boolean suspendHaste) {
        // A suspend cast is still a cast: an owner locked to sorcery timing by an opponent's static
        // effect (Teferi, Mage of Zhalfir) is never offered it outside sorcery timing, so the card stays exiled.
        if (suspendHaste && gameQueryService.isLockedOutByOpponentsSorceryTimingRestriction(
                gameData, exiledEntry.ownerId())) {
            return;
        }
        // "When the last time counter is removed from this card, if it's exiled, you may cast it without paying its
        // mana cost": suspend's (CR 702.62a) or the one the card gained (Alaundo the Seer), the card's own ability.
        Card card = exiledEntry.card();
        UUID cardId = card.getId();
        TriggeredAbility castAbility = new TriggeredAbility(
                new TriggerCondition(Triggers.timeCounterRemovedFromThisInExile(), AbilityZones.EXILE),
                new MayEffect(new CastExiledCardWithoutPayingManaCostEffect(cardId, !suspendHaste, suspendHaste),
                        "Cast " + card.getName() + " without paying its mana cost?"));
        triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(castAbility, card, null,
                exiledEntry.ownerId(), card.getName() + "'s casting ability",
                ConditionContext.forCard(card, exiledEntry.ownerId()), castTrigger -> {
                    castTrigger.setTargetId(cardId);
                    castTrigger.setNonTargeting(true);
                }));
    }
}
