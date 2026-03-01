package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handles control-changing effects that move permanents between players' battlefields.
 * Tracks original ownership so permanents can be returned to their owner when the
 * control-changing effect ends or the permanent leaves the battlefield.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreatureControlService {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    /**
     * Transfers control of a permanent from its current controller to a new controller.
     * The permanent becomes summoning sick under the new controller. The original owner
     * is recorded in {@link GameData#stolenCreatures} on the first steal and is never
     * overwritten by subsequent control changes.
     *
     * <p>No-ops if the permanent is not found on any battlefield or is already controlled
     * by {@code newControllerId}.
     *
     * @param gameData        the current game state
     * @param newControllerId the player gaining control
     * @param creature        the permanent changing control
     */
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
