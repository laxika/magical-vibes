package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGetsPoisonCounterEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DamageSourceControllerGetsPoisonCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageSourceControllerGetsPoisonCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DamageSourceControllerGetsPoisonCounterEffect) effect;
        UUID playerId = e.damageSourceControllerId();
        if (playerId == null || !gameData.playerIds.contains(playerId)) return;
        if (!gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) return;

        int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
        gameData.playerPoisonCounters.put(playerId, currentPoison + 1);

        String playerName = gameData.playerIdToName.get(playerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " gets a poison counter (" , entry.getCard(), ")."));

        log.info("Game {} - {} gets a poison counter from {}", gameData.id, playerName, entry.getCard().getName());
    }
}
