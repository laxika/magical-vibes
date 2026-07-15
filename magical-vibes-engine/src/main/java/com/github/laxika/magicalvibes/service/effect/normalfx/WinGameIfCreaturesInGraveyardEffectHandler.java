package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WinGameIfCreaturesInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameOutcomeService gameOutcomeService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WinGameIfCreaturesInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (WinGameIfCreaturesInGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Intervening-if: check condition again on resolution
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        long creatureCount = 0;
        if (graveyard != null) {
            creatureCount = graveyard.stream()
                    .filter(c -> c.hasType(CardType.CREATURE))
                    .count();
        }

        if (creatureCount >= e.threshold()) {
            // Check if the opponent can't lose (e.g. Platinum Angel)
            UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
            if (!gameQueryService.canPlayerLoseGame(gameData, opponentId)) {
                String logEntry = entry.getCard().getName() + "'s win condition is met but " +
                        gameData.playerIdToName.get(opponentId) + " can't lose the game.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} win prevented — opponent can't lose", gameData.id, entry.getCard().getName());
                return;
            }

            String logEntry = playerName + " has " + creatureCount + " creature cards in their graveyard — " + entry.getCard().getName() + " wins the game!";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} wins via {} ({} creatures in graveyard)",
                    gameData.id, playerName, entry.getCard().getName(), creatureCount);

            gameOutcomeService.declareWinner(gameData, controllerId);
        } else {
            String logEntry = entry.getCard().getName() + "'s ability resolves but condition is no longer met (" + creatureCount + " creature cards in graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} intervening-if no longer met ({} creatures in graveyard, need {})",
                    gameData.id, entry.getCard().getName(), creatureCount, e.threshold());
        }
    }
}
