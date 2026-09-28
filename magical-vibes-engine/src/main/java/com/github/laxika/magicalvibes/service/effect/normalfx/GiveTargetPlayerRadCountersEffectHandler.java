package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GiveTargetPlayerRadCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GiveTargetPlayerRadCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        int amount = amountEvaluationService.evaluate(gameData,
                ((GiveTargetPlayerRadCountersEffect) effect).amount(),
                AmountContext.forStackEntry(entry, null));
        if (amount <= 0) {
            return;
        }

        gameData.playerRadCounters.merge(targetPlayerId, amount, Integer::sum);
        String playerName = gameData.playerIdToName.getOrDefault(targetPlayerId, "Player");
        gameLogService.append(gameData,
                GameLog.text(playerName + " gets " + amount + " rad counter" + (amount == 1 ? "." : "s.")));
    }
}
