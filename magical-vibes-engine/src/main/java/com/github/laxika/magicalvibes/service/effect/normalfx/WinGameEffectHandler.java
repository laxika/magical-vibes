package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WinGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Platinum Angel's "your opponents can't win the game". Note this is NOT a loss event: a
        // win effect ends the game immediately rather than making the opponent lose, so no loss
        // replacer (Lich's Mirror) gets a say — see GameOutcomeService.canPlayerWinGame.
        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        if (!gameOutcomeService.canPlayerWinGame(gameData, controllerId)) {
            String logEntry = entry.getCard().getName() + "'s win condition is met but " +
                    gameData.playerIdToName.get(opponentId) + " can't lose the game.";
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text("'s win condition is met but " + gameData.playerIdToName.get(opponentId) + " can't lose the game.").build());
            log.info("Game {} - {} win prevented — opponent can't lose", gameData.id, entry.getCard().getName());
            return;
        }

        gameLogService.append(gameData, GameLog.textCardText(playerName + " wins the game from " , entry.getCard(), "!"));
        log.info("Game {} - {} wins via {}", gameData.id, playerName, entry.getCard().getName());

        gameOutcomeService.declareWinner(gameData, controllerId);
    }
}
