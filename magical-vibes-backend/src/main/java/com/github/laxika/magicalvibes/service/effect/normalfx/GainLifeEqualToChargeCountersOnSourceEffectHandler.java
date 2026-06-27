package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GainLifeEqualToChargeCountersOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifeEqualToChargeCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int count = entry.getXValue();
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (count <= 0) {
            String logEntry = playerName + " gains 0 life from " + entry.getCard().getName() + " (no charge counters).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} gains 0 life from {} (no charge counters)", gameData.id, playerName, entry.getCard().getName());
            return;
        }

        lifeSupport.applyGainLife(gameData, controllerId, count, entry.getCard().getName());
    }
}
