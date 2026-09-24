package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesAllCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves "each opponent loses all counters" for the player counters tracked by the engine. */
@Component
@RequiredArgsConstructor
public class EachOpponentLosesAllCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentLosesAllCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }

            boolean hadCounters = gameData.playerPoisonCounters.getOrDefault(playerId, 0) > 0
                    || gameData.playerEnergyCounters.getOrDefault(playerId, 0) > 0
                    || gameData.playerSparkCounters.getOrDefault(playerId, 0) > 0
                    || gameData.playerExperienceCounters.getOrDefault(playerId, 0) > 0;
            gameData.playerPoisonCounters.remove(playerId);
            gameData.playerEnergyCounters.remove(playerId);
            gameData.playerSparkCounters.remove(playerId);
            gameData.playerExperienceCounters.remove(playerId);

            if (hadCounters) {
                String playerName = gameData.playerIdToName.getOrDefault(playerId, "Player");
                gameLogService.append(gameData, GameLog.text(
                        playerName + " loses all counters (" + entry.getCard().getName() + ")."));
            }
        }
    }
}
