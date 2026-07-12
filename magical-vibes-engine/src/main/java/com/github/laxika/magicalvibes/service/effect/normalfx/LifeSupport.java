package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Shared life/poison helpers used by every "normal" Life effect handler and by other services
 * (combat, damage, destruction, graveyard return, activated abilities, input handlers).
 *
 * <p>These helpers were extracted verbatim from the original {@code LifeResolutionService} monolith;
 * behavior (log strings, trigger order) is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LifeSupport {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;

    public void applyGainLife(GameData gameData, UUID controllerId, int amount) {
        applyGainLife(gameData, controllerId, amount, null);
    }

    public void applyGainLife(GameData gameData, UUID controllerId, int amount, String source) {
        applyGainLife(gameData, controllerId, amount, source, null, null);
    }

    /**
     * Overload that carries the source card and stack entry type through to life-gain triggers.
     * Used by spell lifelink to let triggers distinguish spell-caused life gain from other sources.
     */
    public void applyGainLife(GameData gameData, UUID controllerId, int amount, String source,
                              Card sourceCard, StackEntryType sourceEntryType) {
        if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            return;
        }
        if (!gameQueryService.canPlayerGainLife(gameData, controllerId)) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + " can't gain life.");
            return;
        }
        // Life-gain doublers (e.g. Boon Reflection) replace the amount before it is applied.
        amount *= gameQueryService.lifeGainMultiplier(gameData, controllerId);
        Integer currentLife = gameData.playerLifeTotals.get(controllerId);
        gameData.playerLifeTotals.put(controllerId, currentLife + amount);
        if (amount > 0) {
            gameData.lifeGainedThisTurn.merge(controllerId, amount, Integer::sum);
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = source != null
                ? playerName + " gains " + amount + " life from " + source + "."
                : playerName + " gains " + amount + " life.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gains {} life", gameData.id, playerName, amount);

        triggerCollectionService.checkLifeGainTriggers(gameData, controllerId, amount, sourceCard, sourceEntryType);
    }

    /**
     * Sets a player's life total per CR 119.5: the change is applied as gaining or losing
     * the necessary amount. Respects canPlayerLifeChange, canPlayerGainLife, and fires triggers.
     *
     * @return true if the life total was changed, false if blocked
     */
    public boolean applySetLifeTotal(GameData gameData, UUID playerId, int newLife) {
        if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            return false;
        }

        int currentLife = gameData.getLife(playerId);
        if (newLife == currentLife) return true;

        if (newLife > currentLife) {
            if (!gameQueryService.canPlayerGainLife(gameData, playerId)) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData, playerName + " can't gain life.");
                return false;
            }
            int gained = (newLife - currentLife) * gameQueryService.lifeGainMultiplier(gameData, playerId);
            gameData.playerLifeTotals.put(playerId, currentLife + gained);
            gameData.lifeGainedThisTurn.merge(playerId, gained, Integer::sum);
            triggerCollectionService.checkLifeGainTriggers(gameData, playerId, gained);
        } else {
            gameData.playerLifeTotals.put(playerId, newLife);
            triggerCollectionService.checkLifeLossTriggers(gameData, playerId, currentLife - newLife);
        }
        return true;
    }

    public void applyLifeLoss(GameData gameData, UUID playerId, int amount, String sourceName) {
        if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            return;
        }
        int currentLife = gameData.getLife(playerId);
        gameData.playerLifeTotals.put(playerId, currentLife - amount);

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " loses " + amount + " life (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, amount, sourceName);

        triggerCollectionService.checkLifeLossTriggers(gameData, playerId, amount);
    }

    public void applyPoisonCounters(GameData gameData, UUID playerId, int amount, String sourceName) {
        if (!gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) return;

        int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
        gameData.playerPoisonCounters.put(playerId, currentPoison + amount);

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " gets " + amount + " poison counter" + (amount > 1 ? "s" : "")
                + " (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets {} poison counter(s) from {}", gameData.id, playerName, amount, sourceName);
    }
}
