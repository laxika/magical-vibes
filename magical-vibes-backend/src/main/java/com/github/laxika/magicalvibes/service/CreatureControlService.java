package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreatureControlService {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    public void stealPermanent(GameData gameData, UUID newControllerId, Permanent creature) {
        UUID originalOwnerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (originalOwnerId == null || originalOwnerId.equals(newControllerId)) {
            return;
        }

        gameData.playerBattlefields.get(originalOwnerId).remove(creature);
        gameData.playerBattlefields.get(newControllerId).add(creature);
        creature.setSummoningSick(true);

        if (!gameData.stolenCreatures.containsKey(creature.getId())) {
            gameData.stolenCreatures.put(creature.getId(), originalOwnerId);
        }

        String newControllerName = gameData.playerIdToName.get(newControllerId);
        String logEntry = newControllerName + " gains control of " + creature.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gains control of {}", gameData.id, newControllerName, creature.getCard().getName());
    }
}
