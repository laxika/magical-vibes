package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WinConditionResolutionService implements EffectHandlerProvider {

    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(WinGameIfCreaturesInGraveyardEffect.class,
                (gd, entry, effect) -> resolveWinGameIfCreaturesInGraveyard(gd, entry, (WinGameIfCreaturesInGraveyardEffect) effect));
        registry.register(TargetPlayerLosesGameEffect.class,
                (gd, entry, effect) -> resolveTargetPlayerLosesGame(gd, entry, (TargetPlayerLosesGameEffect) effect));
    }

    private void resolveWinGameIfCreaturesInGraveyard(GameData gameData, StackEntry entry, WinGameIfCreaturesInGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Intervening-if: check condition again on resolution
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        long creatureCount = 0;
        if (graveyard != null) {
            creatureCount = graveyard.stream()
                    .filter(c -> c.getType() == CardType.CREATURE
                            || c.getAdditionalTypes().contains(CardType.CREATURE))
                    .count();
        }

        if (creatureCount >= effect.threshold()) {
            // Check if the opponent can't lose (e.g. Platinum Angel)
            UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
            if (!gameQueryService.canPlayerLoseGame(gameData, opponentId)) {
                String logEntry = entry.getCard().getName() + "'s win condition is met but " +
                        gameData.playerIdToName.get(opponentId) + " can't lose the game.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} win prevented — opponent can't lose", gameData.id, entry.getCard().getName());
                return;
            }

            String logEntry = playerName + " has " + creatureCount + " creature cards in their graveyard — " + entry.getCard().getName() + " wins the game!";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} wins via {} ({} creatures in graveyard)",
                    gameData.id, playerName, entry.getCard().getName(), creatureCount);

            gameHelper.declareWinner(gameData, controllerId);
        } else {
            String logEntry = entry.getCard().getName() + "'s ability resolves but condition is no longer met (" + creatureCount + " creature cards in graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} intervening-if no longer met ({} creatures in graveyard, need {})",
                    gameData.id, entry.getCard().getName(), creatureCount, effect.threshold());
        }
    }

    private void resolveTargetPlayerLosesGame(GameData gameData, StackEntry entry, TargetPlayerLosesGameEffect effect) {
        UUID losingPlayerId = effect.playerId();
        if (losingPlayerId == null || !gameData.playerIds.contains(losingPlayerId)) {
            return;
        }

        // Check if the player can't lose (e.g. Platinum Angel)
        if (!gameQueryService.canPlayerLoseGame(gameData, losingPlayerId)) {
            String logEntry = gameData.playerIdToName.get(losingPlayerId) + " can't lose the game.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} can't lose the game (protected)", gameData.id,
                    gameData.playerIdToName.get(losingPlayerId));
            return;
        }

        UUID winnerId = gameQueryService.getOpponentId(gameData, losingPlayerId);
        String loserName = gameData.playerIdToName.get(losingPlayerId);
        String winnerName = gameData.playerIdToName.get(winnerId);

        String logEntry = loserName + " loses the game from " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} loses the game from {}", gameData.id, loserName, entry.getCard().getName());

        gameHelper.declareWinner(gameData, winnerId);
        log.info("Game {} - {} wins as {}", gameData.id, winnerName, loserName);
    }
}

