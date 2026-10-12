package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromTargetPlayerToControllerAndPlaneswalkersUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantProtectionFromTargetPlayerToControllerAndPlaneswalkersUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromTargetPlayerToControllerAndPlaneswalkersUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        gameData.playerProtectionFromPlayerIdsUntilNextTurn
                .computeIfAbsent(entry.getControllerId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(targetPlayerId);
        // Only the planeswalkers controlled as this resolves gain protection (CR 611.2c); it stays on
        // each of them if control changes and does not reach planeswalkers that enter later.
        List<Permanent> controlled = gameData.playerBattlefields.get(entry.getControllerId());
        if (controlled != null) {
            for (Permanent permanent : controlled) {
                if (gameQueryService.isPlaneswalker(gameData, permanent)) {
                    permanent.addProtectionFromPlayerUntilNextTurnOf(entry.getControllerId(), targetPlayerId);
                }
            }
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(entry.getControllerId())
                        + " gains protection from " + gameData.playerIdToName.get(targetPlayerId)
                        + " until their next turn."));
        log.info("Game {} - {} gains protection from {} until their next turn",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()),
                gameData.playerIdToName.get(targetPlayerId));
    }
}
