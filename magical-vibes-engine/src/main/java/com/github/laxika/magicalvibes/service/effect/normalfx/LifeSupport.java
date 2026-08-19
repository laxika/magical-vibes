package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.NefariousLichLifeGainReplacementEffect;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
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
public class LifeSupport {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    @Lazy
    private final DrawService drawService;

    public LifeSupport(GameQueryService gameQueryService,
                       GameLogService gameLogService,
                       TriggerCollectionService triggerCollectionService,
                       @Lazy DrawService drawService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
        this.drawService = drawService;
    }

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
            gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
            return;
        }
        if (!gameQueryService.canPlayerGainLife(gameData, controllerId)) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " can't gain life."));
            return;
        }
        if (amount > 0 && hasNefariousLichLifeGainReplacement(gameData, controllerId)) {
            for (int i = 0; i < amount; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }
            return;
        }
        // Tainted Remedy turns the whole gain event into an equal life loss. Per CR 119.10 a gain of
        // 0 is not a life-gain event, so there is nothing to replace.
        if (amount > 0 && gameQueryService.lifeGainBecomesLifeLoss(gameData, controllerId)) {
            applyLifeLoss(gameData, controllerId, amount, source != null ? source : "replaced life gain");
            return;
        }
        if (amount > 0) {
            amount += gameQueryService.additionalLifeGain(gameData, controllerId);
        }
        // Life-gain replacement effects (e.g. Boon Reflection) replace the amount before it is applied.
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
        gameLogService.append(gameData, GameLog.text(logEntry));

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
            gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
            return false;
        }

        int currentLife = gameData.getLife(playerId);
        if (newLife == currentLife) return true;

        if (newLife > currentLife) {
            if (!gameQueryService.canPlayerGainLife(gameData, playerId)) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameLogService.append(gameData, GameLog.text(playerName + " can't gain life."));
                return false;
            }
            if (hasNefariousLichLifeGainReplacement(gameData, playerId)) {
                for (int i = currentLife; i < newLife; i++) {
                    drawService.resolveDrawCard(gameData, playerId);
                }
                return true;
            }
            if (gameQueryService.lifeGainBecomesLifeLoss(gameData, playerId)) {
                applyLifeLoss(gameData, playerId, newLife - currentLife, "replaced life gain");
                return true;
            }
            int gained = newLife - currentLife;
            gained += gameQueryService.additionalLifeGain(gameData, playerId);
            gained *= gameQueryService.lifeGainMultiplier(gameData, playerId);
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
            gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
            return;
        }
        int currentLife = gameData.getLife(playerId);
        gameData.playerLifeTotals.put(playerId, currentLife - amount);

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " loses " + amount + " life (" + sourceName + ").";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, amount, sourceName);

        triggerCollectionService.checkLifeLossTriggers(gameData, playerId, amount);
    }

    public void applyPoisonCounters(GameData gameData, UUID playerId, int amount, String sourceName) {
        applyPoisonCounters(gameData, playerId, amount, sourceName, gameData.currentlyResolvingControllerId);
    }

    public void applyPoisonCounters(GameData gameData, UUID playerId, int amount, String sourceName,
                                    UUID placingPlayerId) {
        amount = gameQueryService.applyPoisonCounterReplacement(gameData, playerId, amount);
        if (amount <= 0) return;

        amount = gameQueryService.replacePoisonCounters(gameData, playerId, amount);
        if (amount <= 0) return;

        int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
        gameData.playerPoisonCounters.put(playerId, currentPoison + amount);

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " gets " + amount + " poison counter" + (amount > 1 ? "s" : "")
                + " (" + sourceName + ").";
        gameLogService.append(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} gets {} poison counter(s) from {}", gameData.id, playerName, amount, sourceName);
        triggerCollectionService.checkYouPutCountersTriggers(gameData, placingPlayerId, amount);
    }

    private boolean hasNefariousLichLifeGainReplacement(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        return battlefield != null && battlefield.stream().anyMatch(permanent ->
                permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(NefariousLichLifeGainReplacementEffect.class::isInstance));
    }
}
