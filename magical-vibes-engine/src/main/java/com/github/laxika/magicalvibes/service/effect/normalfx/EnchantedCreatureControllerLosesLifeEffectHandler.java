package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnchantedCreatureControllerLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedCreatureControllerLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EnchantedCreatureControllerLosesLifeEffect) effect;
        UUID playerId = e.affectedPlayerId();
        if (playerId == null) return;

        if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s life total can't change."));
            return;
        }

        int currentLife = gameData.getLife(playerId);
        gameData.playerLifeTotals.put(playerId, currentLife - e.amount());

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " loses " + e.amount() + " life (" + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, e.amount(), entry.getCard().getName());
    }
}
