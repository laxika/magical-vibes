package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutVoyageCounterOnExiledCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the accepted landfall half of the voyage choice. */
@Component
@RequiredArgsConstructor
public class PutVoyageCounterOnExiledCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutVoyageCounterOnExiledCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var cardId = entry.getCard().getId();
        if (gameData.findExiledCard(cardId) == null) {
            gameData.exiledVoyageCounters.remove(cardId);
            gameData.exiledVoyageControllerIds.remove(cardId);
            return;
        }

        gameData.exiledVoyageCounters.computeIfPresent(cardId, (ignored, counters) -> counters + 1);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " gets a voyage counter."));
    }
}
