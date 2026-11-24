package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AdditionalUpkeepStepEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Queues {@link AdditionalUpkeepStepEffect}'s upkeep steps: after the current upkeep step in
 * {@link GameData#additionalUpkeepsRemaining} (none during an additional upkeep step), or after the current combat
 * phase in {@link GameData#additionalUpkeepStepsAfterCombat}, which {@code TurnProgressionService} takes before the
 * rest of the turn.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdditionalUpkeepStepEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AdditionalUpkeepStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AdditionalUpkeepStepEffect additionalUpkeeps = (AdditionalUpkeepStepEffect) effect;
        if (additionalUpkeeps.placement() == AdditionalUpkeepStepEffect.Placement.AFTER_THIS_STEP
                && gameData.currentUpkeepIsAdditional) {
            return;
        }
        int count = amountEvaluationService.evaluate(gameData, additionalUpkeeps.count(),
                AmountContext.forStackEntry(entry, null));
        if (count <= 0) {
            return;
        }

        switch (additionalUpkeeps.placement()) {
            case AFTER_THIS_STEP -> {
                gameData.additionalUpkeepsRemaining += count;
                String playerName = gameData.playerIdToName.get(entry.getTargetId() != null
                        ? entry.getTargetId() : entry.getControllerId());
                String logEntry = count == 1
                        ? playerName + " gets an additional upkeep step."
                        : playerName + " gets " + count + " additional upkeep steps.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} gets {} additional upkeep step(s)", gameData.id, playerName, count);
            }
            case AFTER_THIS_COMBAT_PHASE -> {
                gameData.additionalUpkeepStepsAfterCombat += count;
                gameData.additionalCombatCreatedAfterUpkeepSteps = false;
                String logEntry = count == 1
                        ? "After this combat phase, there is an additional upkeep step."
                        : "After this combat phase, there are " + count + " additional upkeep steps.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} queued {} additional upkeep step(s)",
                        gameData.id, entry.getCard().getName(), count);
            }
        }
    }
}
