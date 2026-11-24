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
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RemoveTimeCounterFromExiledCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;

    public RemoveTimeCounterFromExiledCardEffectHandler(GameLogService gameLogService,
            GameQueryService gameQueryService,
            @Lazy TriggeredAbilityStackService triggeredAbilityStackService) {
        this.gameLogService = gameLogService;
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

    /**
     * Removes a time counter from the card {@code cardId} in exile on behalf of {@code removerId} ({@code null} when its
     * owner removes it): a card suspended from a hand, a spell exiled with time counters that gained suspend, or a card
     * Alaundo the Seer exiled. Each removal is a time-counter-removed-in-exile event; the last one also queues the
     * card's "when the last time counter is removed from this card, if it's exiled, you may play it without paying its
     * mana cost" (suspend's, CR 702.62a, and Alaundo's).
     */
    public void removeTimeCounter(GameData gameData, UUID cardId, UUID removerId) {
        ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
        int counters = gameData.exiledCardCounterCount(cardId, CounterType.TIME);
        if (exiledEntry == null || counters <= 0) {
            return;
        }

        int remaining = counters - 1;
        gameData.setExiledCardCounters(cardId, CounterType.TIME, remaining);
        if (remaining > 0) {
            gameLogService.append(gameData,
                    GameLog.cardThen(exiledEntry.card(), " has a time counter removed (" + remaining + " remaining)."));
            gameData.rulesEvents.occur(new CounterChangeEvent(CounterChangeEvent.Kind.COUNTER_REMOVED_IN_EXILE,
                    null, null, exiledEntry.card(), exiledEntry.ownerId(), removerId, CounterType.TIME, remaining, 0,
                            false, null));
            return;
        }

        boolean hasAlaundoCastAbility = gameData.exiledCardsWithAlaundoCastAbility.contains(cardId);
        boolean hasSuspend = gameData.exiledCardHasSuspend(exiledEntry.card());
        gameData.rulesEvents.occur(new CounterChangeEvent(CounterChangeEvent.Kind.COUNTER_REMOVED_IN_EXILE,
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

    /** Whether {@code exiled} is suspended (CR 702.62b): {@link GameData#isSuspended}. */
    public static boolean isSuspended(GameData gameData, ExiledCardEntry exiled) {
        return gameData.isSuspended(exiled.card().getId());
    }

    public void queueCastTrigger(GameData gameData, ExiledCardEntry exiledEntry, boolean suspendHaste) {
        // A suspend cast is still a cast: an owner locked to sorcery timing by an opponent's static
        // effect (Teferi, Mage of Zhalfir) is never offered it outside sorcery timing, so the card stays exiled.
        if (suspendHaste && gameQueryService.isHeldToSorceryTiming(
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
