package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerBecomesMonarchEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetPlayerBecomesMonarchEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerBecomesMonarchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId();
        if (playerId == null || !gameData.playerIds.contains(playerId)
                || playerId.equals(gameData.monarchPlayerId)) {
            return;
        }

        gameData.monarchPlayerId = playerId;
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.getOrDefault(playerId, "A player") + " becomes the monarch."));
        permanentRemovalService.returnExileReturnsOnOpponentBecomesMonarch(gameData, playerId);
        triggerCollectionService.checkBecomesMonarchTriggers(gameData, playerId);
    }
}
