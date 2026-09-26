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

import java.util.Collections;
import java.util.List;
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
        var e = (TargetPlayerBecomesMonarchEffect) effect;
        List<UUID> targetPlayerIds = e.targetGroup() >= 0
                ? entry.targetsForGroup(e.targetGroup()) : entry.targetsForEffect(effect);
        if (e.targetGroup() < 0 && targetPlayerIds.isEmpty() && entry.getTargetId() != null) {
            targetPlayerIds = Collections.singletonList(entry.getTargetId());
        }

        for (UUID targetPlayerId : targetPlayerIds) {
            if (!gameData.playerIds.contains(targetPlayerId)
                    || targetPlayerId.equals(gameData.monarchPlayerId)) {
                continue;
            }

            gameData.monarchPlayerId = targetPlayerId;
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.getOrDefault(targetPlayerId, "A player")
                            + " becomes the monarch."));
            permanentRemovalService.returnExileReturnsOnOpponentBecomesMonarch(gameData, targetPlayerId);
            triggerCollectionService.checkBecomesMonarchTriggers(gameData, targetPlayerId);
            return;
        }
    }
}
