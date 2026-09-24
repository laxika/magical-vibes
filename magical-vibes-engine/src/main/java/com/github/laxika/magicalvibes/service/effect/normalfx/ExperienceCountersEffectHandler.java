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

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExperienceCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExperienceCountersEffect experience = (ExperienceCountersEffect) effect;
        int amount = amountEvaluationService.evaluate(gameData, experience.amount(),
                AmountContext.forStackEntry(entry, null));
        if (amount == 0) {
            return;
        }

        int current = gameData.playerExperienceCounters.getOrDefault(entry.getControllerId(), 0);
        int updated = Math.max(0, current + amount);
        int changed = updated - current;
        if (changed == 0) {
            return;
        }

        gameData.playerExperienceCounters.put(entry.getControllerId(), updated);
        String playerName = gameData.playerIdToName.getOrDefault(entry.getControllerId(), "Player");
        String action = changed > 0 ? "gets " + changed : "loses " + -changed;
        gameLogService.append(gameData,
                GameLog.text(playerName + " " + action + " experience counter(s)."));
    }
}
