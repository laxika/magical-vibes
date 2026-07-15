package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetEachPlayerLifeToCreatureCountEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetEachPlayerLifeToCreatureCountEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetEachPlayerLifeToCreatureCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            int creatureCount = 0;
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (gameQueryService.isCreature(gameData, permanent)) {
                        creatureCount++;
                    }
                }
            }

            int currentLife = gameData.getLife(playerId);
            if (lifeSupport.applySetLifeTotal(gameData, playerId, creatureCount)) {
                if (currentLife != creatureCount) {
                    String playerName = gameData.playerIdToName.get(playerId);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s life total becomes " + creatureCount + " (was " + currentLife + ")."));
                    log.info("Game {} - {}'s life set to {} (was {})",
                            gameData.id, playerName, creatureCount, currentLife);
                }
            }
        }
    }
}
