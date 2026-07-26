package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsByConvergeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsByConvergeEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsByConvergeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int converge = entry.getXValue();
        UUID targetPlayerId = entry.getTargetId();

        if (converge <= 0) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " discards 0 cards (Converge is 0).";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        gameData.discardCausedByOpponent = true;
        playerInteractionSupport.resolveDiscardCards(gameData, targetPlayerId, converge);
    }
}
