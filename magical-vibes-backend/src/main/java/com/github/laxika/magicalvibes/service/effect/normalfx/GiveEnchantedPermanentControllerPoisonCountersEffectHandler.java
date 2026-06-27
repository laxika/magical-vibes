package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEnchantedPermanentControllerPoisonCountersEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GiveEnchantedPermanentControllerPoisonCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GiveEnchantedPermanentControllerPoisonCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GiveEnchantedPermanentControllerPoisonCountersEffect) effect;
        UUID playerId = e.affectedPlayerId();
        if (playerId == null) return;

        if (!gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) return;

        int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
        gameData.playerPoisonCounters.put(playerId, currentPoison + e.amount());

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " gets " + e.amount() + " poison counter" + (e.amount() > 1 ? "s" : "")
                + " (" + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets {} poison counter(s) from {}", gameData.id, playerName, e.amount(), entry.getCard().getName());
    }
}
