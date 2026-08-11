package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleEnchantedCreatureControllerLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoubleEnchantedCreatureControllerLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleEnchantedCreatureControllerLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId();
        if (playerId == null) return;

        int currentLife = gameData.getLife(playerId);
        int newLife = currentLife * 2;
        if (lifeSupport.applySetLifeTotal(gameData, playerId, newLife)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s life total is doubled from " + currentLife + " to " + newLife + "."));
            log.info("Game {} - {}'s life doubled from {} to {}", gameData.id, playerName, currentLife, newLife);
        }
    }
}
