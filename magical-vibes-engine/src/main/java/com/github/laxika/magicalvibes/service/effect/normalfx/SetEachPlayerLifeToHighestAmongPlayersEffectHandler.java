package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetEachPlayerLifeToHighestAmongPlayersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetEachPlayerLifeToHighestAmongPlayersEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetEachPlayerLifeToHighestAmongPlayersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int highestLife = gameData.orderedPlayerIds.stream()
                .mapToInt(gameData::getLife)
                .max()
                .orElse(0);

        for (UUID playerId : gameData.orderedPlayerIds) {
            int currentLife = gameData.getLife(playerId);
            if (lifeSupport.applySetLifeTotal(gameData, playerId, highestLife)) {
                if (currentLife != highestLife) {
                    String playerName = gameData.playerIdToName.get(playerId);
                    gameLogService.append(gameData, GameLog.text(playerName + "'s life total becomes " + highestLife + " (was " + currentLife + ")."));
                    log.info("Game {} - {}'s life set to {} (was {})",
                            gameData.id, playerName, highestLife, currentLife);
                }
            }
        }
    }
}
