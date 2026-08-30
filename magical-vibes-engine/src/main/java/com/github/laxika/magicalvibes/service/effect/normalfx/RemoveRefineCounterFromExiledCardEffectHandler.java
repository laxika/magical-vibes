package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
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
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                card,
                ownerId,
                java.util.List.of(new MayCastExiledCardWithCostReductionEffect(4)),
                "Cast " + card.getName() + " for {4} less?",
                cardId));
        gameLogService.append(gameData, GameLog.cardThen(card,
                " has its last refine counter removed. You may cast it for {4} less."));
        log.info("Game {} - {} last refine counter removed; cast offer queued", gameData.id, card.getName());
    }
}
