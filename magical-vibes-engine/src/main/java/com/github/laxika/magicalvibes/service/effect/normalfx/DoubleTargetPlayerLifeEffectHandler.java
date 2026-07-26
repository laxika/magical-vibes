package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoubleTargetPlayerLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleTargetPlayerLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        int currentLife = gameData.getLife(targetPlayerId);
        int newLife = currentLife * 2;

        if (lifeSupport.applySetLifeTotal(gameData, targetPlayerId, newLife)) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.text(playerName + "'s life total is doubled from " + currentLife + " to " + newLife + "."));
            log.info("Game {} - {}'s life doubled from {} to {}", gameData.id, playerName, currentLife, newLife);
        }
    }
}
