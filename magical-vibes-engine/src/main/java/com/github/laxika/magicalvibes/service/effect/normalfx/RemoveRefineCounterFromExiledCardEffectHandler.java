package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.CounterTrigger;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardWithCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveRefineCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveRefineCounterFromExiledCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Autowired
    @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveRefineCounterFromExiledCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = ((RemoveRefineCounterFromExiledCardEffect) effect).cardId();
        Integer counters = gameData.exiledCardRefineCounters.get(cardId);
        Card card = gameQueryService.findCardInExileById(gameData, cardId);
        if (counters == null || counters <= 0 || card == null) {
            gameData.exiledCardRefineCounters.remove(cardId);
            return;
        }

        if (counters > 1) {
            gameData.exiledCardRefineCounters.put(cardId, counters - 1);
            gameLogService.append(gameData, GameLog.builder().card(card)
                    .text(" has a refine counter removed (" + (counters - 1) + " remaining).").build());
            return;
        }

        gameData.exiledCardRefineCounters.remove(cardId);
        UUID ownerId = gameQueryService.findExileOwnerById(gameData, cardId);
        if (ownerId == null) {
            return;
        }
        // "When the last refine counter is removed from this card, if it's exiled, you may cast it. It costs {4} less to
        // cast this way", an ability the card gained as it was exiled (Uvilda, Dean of Perfection).
        TriggeredAbility castAbility = new TriggeredAbility(
                new TriggerCondition(new CounterTrigger(CounterTrigger.CounterEvent.COUNTERS_REMOVED,
                        ObjectRelation.THIS), AbilityZones.EXILE),
                new MayCastExiledCardWithCostReductionEffect(4));
        triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(castAbility, card, null, ownerId,
                card.getName() + "'s cast ability", ConditionContext.forCard(card, ownerId), castTrigger -> {
                    castTrigger.setTargetId(cardId);
                    castTrigger.setTargetZone(Zone.EXILE);
                }));
        gameLogService.append(gameData, GameLog.cardThen(card,
                " has its last refine counter removed. You may cast it for {4} less."));
        log.info("Game {} - {} last refine counter removed; cast offer queued", gameData.id, card.getName());
    }
}
