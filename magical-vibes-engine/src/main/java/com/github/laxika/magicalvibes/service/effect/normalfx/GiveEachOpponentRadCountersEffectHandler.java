package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEachOpponentRadCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GiveEachOpponentRadCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GiveEachOpponentRadCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int amount = amountEvaluationService.evaluate(gameData,
                ((GiveEachOpponentRadCountersEffect) effect).amount(),
                AmountContext.forStackEntry(entry, null));
        if (amount <= 0) {
            return;
        }

        for (var playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(entry.getControllerId())) {
                continue;
            }
            gameData.playerRadCounters.merge(playerId, amount, Integer::sum);
            String playerName = gameData.playerIdToName.getOrDefault(playerId, "Player");
            gameLogService.append(gameData,
                    GameLog.text(playerName + " gets " + amount + " rad counter" + (amount == 1 ? "." : "s.")));
        }
    }
}
