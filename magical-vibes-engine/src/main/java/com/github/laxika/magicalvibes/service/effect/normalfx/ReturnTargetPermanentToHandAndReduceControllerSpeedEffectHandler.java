package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandAndReduceControllerSpeedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Spikeshell Harrier's ordered bounce and speed-reduction rider. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnTargetPermanentToHandAndReduceControllerSpeedEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ReturnToHandEffectHandler returnToHandEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPermanentToHandAndReduceControllerSpeedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetId);
        returnToHandEffectHandler.resolve(gameData, entry, ReturnToHandEffect.target());
        reduceSpeedIfLeading(gameData, targetControllerId, entry);
    }

    private void reduceSpeedIfLeading(GameData gameData, UUID playerId, StackEntry entry) {
        if (playerId == null) {
            return;
        }

        int speed = gameData.playerSpeeds.getOrDefault(playerId, 0);
        if (speed <= 1 || gameData.orderedPlayerIds.stream()
                .filter(otherPlayerId -> !otherPlayerId.equals(playerId))
                .anyMatch(otherPlayerId -> speed <= gameData.playerSpeeds.getOrDefault(otherPlayerId, 0))) {
            return;
        }

        int newSpeed = speed - 1;
        gameData.playerSpeeds.put(playerId, newSpeed);
        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.text(playerName + "'s speed is reduced to " + newSpeed + "."));
        log.info("Game {} - {} speed is reduced to {} by {}", gameData.id, playerName, newSpeed,
                entry.getCard().getName());
    }
}
