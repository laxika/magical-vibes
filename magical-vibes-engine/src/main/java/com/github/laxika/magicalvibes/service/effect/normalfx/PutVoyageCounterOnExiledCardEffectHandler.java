package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
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
            gameData.setExiledCardCounters(cardId, CounterType.VOYAGE, 0);
            gameData.exiledVoyageControllerIds.remove(cardId);
            return;
        }

        if (!gameData.exiledVoyageControllerIds.containsKey(cardId)) {
            return;
        }
        gameData.addExiledCardCounters(cardId, CounterType.VOYAGE, 1);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " gets a voyage counter."));
    }
}
