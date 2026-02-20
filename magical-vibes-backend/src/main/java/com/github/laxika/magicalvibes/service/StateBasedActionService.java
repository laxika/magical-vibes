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
                    gameHelper.addCardToGraveyard(gameData, graveyardOwnerId, p.getOriginalCard(), Zone.BATTLEFIELD);
                    gameHelper.collectDeathTrigger(gameData, p.getCard(), playerId, true);
                    gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
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
            gameHelper.removeOrphanedAuras(gameData);
        }
    }
}

