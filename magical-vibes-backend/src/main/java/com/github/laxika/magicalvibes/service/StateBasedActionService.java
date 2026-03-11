package com.github.laxika.magicalvibes.service;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateBasedActionService {

    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardService graveyardService;

    public void performStateBasedActions(GameData gameData) {
        boolean anyDied = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            // Two-pass: collect dead permanents first, then remove via PermanentRemovalService
            List<Permanent> toDie = new ArrayList<>();
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p) && gameQueryService.getEffectiveToughness(gameData, p) <= 0) {
                    toDie.add(p);
                } else if (gameQueryService.isCreature(gameData, p)
                        && p.getMarkedDamage() >= gameQueryService.getEffectiveToughness(gameData, p)
                        && !gameQueryService.hasKeyword(gameData, p, Keyword.INDESTRUCTIBLE)
                        && !graveyardService.tryRegenerate(gameData, p)) {
                    // CR 704.5g — creature with damage >= toughness is destroyed (regeneration can replace this)
                    toDie.add(p);
                } else if (p.getCard().getType() == CardType.PLANESWALKER && p.getLoyaltyCounters() <= 0) {
                    toDie.add(p);
                }
            }

            for (Permanent p : toDie) {
                boolean isCreature = gameQueryService.isCreature(gameData, p);
                permanentRemovalService.removePermanentToGraveyard(gameData, p);

                if (isCreature && gameQueryService.getEffectiveToughness(gameData, p) <= 0) {
                    String logEntry = p.getCard().getName() + " is put into the graveyard (0 toughness).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} dies to state-based actions (0 toughness)", gameData.id, p.getCard().getName());
                } else if (isCreature) {
                    String logEntry = p.getCard().getName() + " is destroyed (lethal damage).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} dies to state-based actions (lethal damage)", gameData.id, p.getCard().getName());
                } else {
                    String logEntry = p.getCard().getName() + " has no loyalty counters and is put into the graveyard.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} dies to state-based actions (0 loyalty)", gameData.id, p.getCard().getName());
                }
                anyDied = true;
            }
        }
        if (anyDied) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }

        // CR 704.5c — player with ten or more poison counters loses the game
        for (UUID playerId : gameData.orderedPlayerIds) {
            int poison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
            if (poison >= 10) {
                gameOutcomeService.checkWinCondition(gameData);
                if (gameData.status == GameStatus.FINISHED) {
                    return;
                }
            }
        }

        // CR 704.5q — +1/+1 and -1/-1 counters cancel each other out
        gameData.forEachPermanent((pid, p) -> {
            int plus = p.getPlusOnePlusOneCounters();
            int minus = p.getMinusOneMinusOneCounters();
            if (plus > 0 && minus > 0) {
                int cancelled = Math.min(plus, minus);
                p.setPlusOnePlusOneCounters(plus - cancelled);
                p.setMinusOneMinusOneCounters(minus - cancelled);
            }
        });

        // CR 704.5b — player who attempted to draw from an empty library loses the game
        if (!gameData.playersAttemptedDrawFromEmptyLibrary.isEmpty()) {
            for (UUID playerId : List.copyOf(gameData.playersAttemptedDrawFromEmptyLibrary)) {
                if (gameQueryService.canPlayerLoseGame(gameData, playerId)) {
                    UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                    String logEntry = gameData.playerIdToName.get(playerId) + " attempted to draw from an empty library and loses the game.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} loses (drew from empty library)", gameData.id, gameData.playerIdToName.get(playerId));
                    gameOutcomeService.declareWinner(gameData, winnerId);
                }
            }
            gameData.playersAttemptedDrawFromEmptyLibrary.clear();
        }
    }
}
