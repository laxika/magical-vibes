package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateBasedActionService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    public void performStateBasedActions(GameData gameData) {
        boolean anyDied = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                if (gameQueryService.isCreature(gameData, p) && gameQueryService.getEffectiveToughness(gameData, p) <= 0) {
                    it.remove();
                    UUID graveyardOwnerId = gameData.stolenCreatures.getOrDefault(p.getId(), playerId);
                    gameData.stolenCreatures.remove(p.getId());
                    boolean wentToGraveyard = gameHelper.addCardToGraveyard(gameData, graveyardOwnerId, p.getOriginalCard(), Zone.BATTLEFIELD);
                    if (wentToGraveyard) {
                        gameHelper.collectDeathTrigger(gameData, p.getCard(), playerId, true);
                        gameData.creatureDeathCountThisTurn.merge(playerId, 1, Integer::sum);
                        gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
                        gameHelper.checkAnyNontokenCreatureDeathTriggers(gameData, p.getCard());
                        gameHelper.checkOpponentCreatureDeathTriggers(gameData, playerId);
                        gameHelper.checkEquippedCreatureDeathTriggers(gameData, p.getId(), playerId);
                    }
                    String logEntry = p.getCard().getName() + " is put into the graveyard (0 toughness).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} dies to state-based actions (0 toughness)", gameData.id, p.getCard().getName());
                    anyDied = true;
                } else if (p.getCard().getType() == CardType.PLANESWALKER && p.getLoyaltyCounters() <= 0) {
                    it.remove();
                    gameHelper.addCardToGraveyard(gameData, playerId, p.getOriginalCard(), Zone.BATTLEFIELD);
                    String logEntry = p.getCard().getName() + " has no loyalty counters and is put into the graveyard.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} dies to state-based actions (0 loyalty)", gameData.id, p.getCard().getName());
                    anyDied = true;
                }
            }
        }
        if (anyDied) {
            permanentRemovalService.removeOrphanedAuras(gameData);
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
                    gameHelper.declareWinner(gameData, winnerId);
                }
            }
            gameData.playersAttemptedDrawFromEmptyLibrary.clear();
        }
    }
}

