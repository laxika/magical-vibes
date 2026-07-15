package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerLosesGameEffectHandler implements NormalEffectHandlerBean {

    private final GameOutcomeService gameOutcomeService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerLosesGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerLosesGameEffect) effect;
        UUID losingPlayerId = e.playerId();
        if (losingPlayerId == null || !gameData.playerIds.contains(losingPlayerId)) {
            return;
        }

        // Check if the player can't lose (e.g. Platinum Angel)
        if (!gameQueryService.canPlayerLoseGame(gameData, losingPlayerId)) {
            String logEntry = gameData.playerIdToName.get(losingPlayerId) + " can't lose the game.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} can't lose the game (protected)", gameData.id,
                    gameData.playerIdToName.get(losingPlayerId));
            return;
        }

        UUID winnerId = gameQueryService.getOpponentId(gameData, losingPlayerId);
        String loserName = gameData.playerIdToName.get(losingPlayerId);
        String winnerName = gameData.playerIdToName.get(winnerId);

        String logEntry = loserName + " loses the game from " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} loses the game from {}", gameData.id, loserName, entry.getCard().getName());

        gameOutcomeService.declareWinner(gameData, winnerId);
        log.info("Game {} - {} wins as {}", gameData.id, winnerName, loserName);
    }
}
