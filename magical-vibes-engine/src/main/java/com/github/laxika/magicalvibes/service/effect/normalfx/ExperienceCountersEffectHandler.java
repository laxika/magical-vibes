package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExperienceCountersEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExperienceCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExperienceCountersEffect experience = (ExperienceCountersEffect) effect;
        int amount = amountEvaluationService.evaluate(gameData, experience.amount(),
                AmountContext.forStackEntry(entry, null));
        if (amount <= 0) {
            return;
        }

        gameData.playerExperienceCounters.merge(entry.getControllerId(), amount, Integer::sum);
        String playerName = gameData.playerIdToName.getOrDefault(entry.getControllerId(), "Player");
        gameLogService.append(gameData, GameLog.text(playerName + " gets " + amount
                + " experience counter" + (amount == 1 ? "." : "s.")));
    }
}
