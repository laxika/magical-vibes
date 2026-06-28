package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeLifeTotalWithCreatureStatEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeLifeTotalWithCreatureStatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeLifeTotalWithCreatureStatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExchangeLifeTotalWithCreatureStatEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        boolean usePower = e.stat() == ExchangeLifeTotalWithCreatureStatEffect.Stat.POWER;
        String statName = usePower ? "power" : "toughness";

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameBroadcastService.logAndBroadcast(gameData, "Source creature is no longer on the battlefield. Exchange doesn't occur.");
            return;
        }

        // CR 701.10e: player's new life = creature's current effective stat
        int currentStat = usePower
                ? gameQueryService.getEffectivePower(gameData, source)
                : gameQueryService.getEffectiveToughness(gameData, source);
        int currentLife = gameData.getLife(controllerId);

        // CR 118.7: if the player's life total can't change, the exchange doesn't occur
        if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change. Exchange doesn't occur.");
            return;
        }

        if (currentLife == currentStat) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " exchanges life total with " + source.getCard().getName()
                            + "'s " + statName + " (both at " + currentLife + ").");
            return;
        }

        // Check if the life portion of the exchange is blocked by can't-gain-life
        boolean lifeWouldIncrease = currentStat > currentLife;
        if (lifeWouldIncrease && !gameQueryService.canPlayerGainLife(gameData, controllerId)) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + " can't gain life. Exchange doesn't occur.");
            return;
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " exchanges life total with " + source.getCard().getName()
                        + "'s " + statName + " (" + playerName + ": " + currentLife + " -> " + currentStat
                        + ", " + source.getCard().getName() + " " + statName + ": " + currentStat + " -> " + currentLife + ").");

        // Set player's life total to the creature's stat
        gameData.playerLifeTotals.put(controllerId, currentStat);
        if (currentStat > currentLife) {
            triggerCollectionService.checkLifeGainTriggers(gameData, controllerId, currentStat - currentLife);
        } else {
            triggerCollectionService.checkLifeLossTriggers(gameData, controllerId, currentLife - currentStat);
        }

        // Set creature's base stat to the player's former life total (layer 7b setting effect, CR 613.4b)
        if (usePower) {
            source.setBasePowerOverriddenPermanently(true);
            source.setPermanentBasePowerOverride(currentLife);
        } else {
            source.setBaseToughnessOverriddenPermanently(true);
            source.setPermanentBaseToughnessOverride(currentLife);
        }

        log.info("Game {} - {} exchanges life ({} -> {}) with {}'s {} ({} -> {})",
                gameData.id, playerName, currentLife, currentStat,
                source.getCard().getName(), statName, currentStat, currentLife);
    }
}
