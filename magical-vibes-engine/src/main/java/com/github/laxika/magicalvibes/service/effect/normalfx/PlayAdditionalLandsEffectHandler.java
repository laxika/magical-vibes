package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayAdditionalLandsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayAdditionalLandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PlayAdditionalLandsEffect) effect;
        UUID controllerId = entry.getControllerId();
        int count = amountEvaluationService.evaluate(gameData, e.count(), AmountContext.forStackEntry(entry, null));
        gameData.additionalLandsThisTurn.merge(controllerId, count, Integer::sum);

        String controllerName = gameData.playerIdToName.get(controllerId);
        String logEntry = controllerName + " may play up to " + count + " additional land"
                + (count == 1 ? "" : "s") + " this turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} may play {} additional lands this turn", gameData.id, controllerName, count);
    }
}
