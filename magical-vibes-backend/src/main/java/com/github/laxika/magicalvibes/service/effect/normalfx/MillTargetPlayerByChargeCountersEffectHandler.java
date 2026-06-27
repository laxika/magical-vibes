package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByChargeCountersEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillTargetPlayerByChargeCountersEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillTargetPlayerByChargeCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        int chargeCounters = entry.getXValue();

        if (chargeCounters <= 0) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " mills 0 cards (no charge counters).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} mills 0 cards (no charge counters)", gameData.id, playerName);
            return;
        }

        graveyardService.resolveMillPlayer(gameData, targetPlayerId, chargeCounters);
    }
}
