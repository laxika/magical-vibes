package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEachPlayerRadCounterEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.GameLogService;
import org.springframework.stereotype.Component;

@Component
public class GiveEachPlayerRadCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    public GiveEachPlayerRadCounterEffectHandler(GameLogService gameLogService,
                                                 AmountEvaluationService amountEvaluationService,
                                                 GameQueryService gameQueryService) {
        this.gameLogService = gameLogService;
        this.amountEvaluationService = amountEvaluationService;
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GiveEachPlayerRadCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int amount = amountEvaluationService.evaluate(gameData,
                ((GiveEachPlayerRadCounterEffect) effect).amount(),
                AmountContext.forStackEntry(entry, null));
        if (amount <= 0) {
            return;
        }

        for (var playerId : gameData.orderedPlayerIds) {
            int placed = gameQueryService.replaceRadCounters(gameData, playerId, amount, entry.getControllerId());
            if (placed <= 0) {
                continue;
            }
            gameData.playerRadCounters.merge(playerId, placed, Integer::sum);
            String playerName = gameData.playerIdToName.getOrDefault(playerId, "Player");
            gameLogService.append(gameData,
                    GameLog.text(playerName + " gets " + placed + " rad counter" + (placed == 1 ? "." : "s.")));
        }
    }
}
