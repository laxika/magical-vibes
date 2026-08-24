package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AdditionalUpkeepStepsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdditionalUpkeepStepsEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AdditionalUpkeepStepsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AdditionalUpkeepStepsEffect additionalUpkeeps = (AdditionalUpkeepStepsEffect) effect;
        int count = amountEvaluationService.evaluate(gameData, additionalUpkeeps.count(),
                AmountContext.forStackEntry(entry, null));
        if (count <= 0) {
            return;
        }

        gameData.additionalUpkeepStepsAfterCombat += count;

        String logEntry = count == 1
                ? "After this combat phase, there is an additional upkeep step."
                : "After this combat phase, there are " + count + " additional upkeep steps.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} queued {} additional upkeep step(s)",
                gameData.id, entry.getCard().getName(), count);
    }
}
