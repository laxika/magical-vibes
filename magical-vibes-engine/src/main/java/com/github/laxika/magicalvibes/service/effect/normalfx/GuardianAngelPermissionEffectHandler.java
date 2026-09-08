package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GuardianAngelPermissionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuardianAngelPermissionEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GuardianAngelPermissionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetId = entry.getTargetId();
        if (controllerId == null || targetId == null) {
            return;
        }

        gameData.guardianAngelTargetsUntilEndOfTurn
                .computeIfAbsent(controllerId, ignored -> ConcurrentHashMap.newKeySet())
                .add(targetId);
        gameLogService.append(gameData, GameLog.text(
                "The controller may pay {1} any time they could cast an instant to prevent the next 1 damage to the Guardian Angel target."));
        log.info("Game {} - {} gains Guardian Angel prevention permission for target {}",
                gameData.id, controllerId, targetId);
    }
}
