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

/** Resolves the counter-removal mode of Final Act. */
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
        if (controllerId == null) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }

            gameData.playerPoisonCounters.remove(playerId);
            gameData.playerEnergyCounters.remove(playerId);
            gameData.playerExperienceCounters.remove(playerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.getOrDefault(playerId, "Player")
                            + " loses all counters from " + entry.getCard().getName() + "."));
        }
    }
}
