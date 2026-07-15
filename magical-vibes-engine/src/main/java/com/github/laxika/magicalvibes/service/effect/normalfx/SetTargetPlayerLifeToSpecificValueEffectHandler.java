package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPlayerLifeToSpecificValueEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetTargetPlayerLifeToSpecificValueEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetTargetPlayerLifeToSpecificValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetTargetPlayerLifeToSpecificValueEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) return;
        int currentLife = gameData.getLife(targetPlayerId);
        int newLife = e.targetLifeTotal();

        if (lifeSupport.applySetLifeTotal(gameData, targetPlayerId, newLife)) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s life total becomes " + newLife + " (was " + currentLife + ")."));
            log.info("Game {} - {}'s life set to {} (was {})", gameData.id, playerName, newLife, currentLife);
        }
    }
}
