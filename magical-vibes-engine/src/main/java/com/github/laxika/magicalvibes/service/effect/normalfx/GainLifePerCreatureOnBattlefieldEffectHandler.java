package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCreatureOnBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GainLifePerCreatureOnBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifePerCreatureOnBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int[] creatureCountHolder = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatureCountHolder[0]++;
            }
        });
        int creatureCount = creatureCountHolder[0];
        if (creatureCount == 0) {
            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            String logEntry = playerName + " gains no life (no creatures on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        lifeSupport.applyGainLife(gameData, entry.getControllerId(), creatureCount);
    }
}
