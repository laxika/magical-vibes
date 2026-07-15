package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
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
public class WinGameEffectHandler implements NormalEffectHandlerBean {

    private final GameOutcomeService gameOutcomeService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WinGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Check if the opponent can't lose (e.g. Platinum Angel)
        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        if (!gameQueryService.canPlayerLoseGame(gameData, opponentId)) {
            String logEntry = entry.getCard().getName() + "'s win condition is met but " +
                    gameData.playerIdToName.get(opponentId) + " can't lose the game.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} win prevented — opponent can't lose", gameData.id, entry.getCard().getName());
            return;
        }

        String logEntry = playerName + " wins the game from " + entry.getCard().getName() + "!";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} wins via {}", gameData.id, playerName, entry.getCard().getName());

        gameOutcomeService.declareWinner(gameData, controllerId);
    }
}
