package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPlayerLifeToHalfStartingEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetTargetPlayerLifeToHalfStartingEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetTargetPlayerLifeToHalfStartingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) return; // "up to one target" — player chose no target
        int currentLife = gameData.getLife(targetPlayerId);
        int newLife = GameData.STARTING_LIFE_TOTAL / 2;

        if (lifeSupport.applySetLifeTotal(gameData, targetPlayerId, newLife)) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + "'s life total becomes " + newLife + " (was " + currentLife + ").");
            log.info("Game {} - {}'s life set to {} (was {})", gameData.id, playerName, newLife, currentLife);
        }
    }
}
