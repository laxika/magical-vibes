package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
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
        UUID sourcePermanentId = entry.getSourcePermanentId();
        boolean usePower = e.stat() == ExchangeLifeTotalWithCreatureStatEffect.Stat.POWER;
        String statName = usePower ? "power" : "toughness";

        UUID playerId = e.recipient() == ExchangeLifeTotalWithCreatureStatEffect.Recipient.TARGET_PLAYER
                ? entry.getTargetId()
                : entry.getControllerId();
        if (playerId == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text("No player to exchange with. Exchange doesn't occur."));
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text("Source creature is no longer on the battlefield. Exchange doesn't occur."));
            return;
        }

        // CR 701.10e: player's new life = creature's current effective stat
        int currentStat = usePower
                ? gameQueryService.getEffectivePower(gameData, source)
                : gameQueryService.getEffectiveToughness(gameData, source);
        int currentLife = gameData.getLife(playerId);

        // CR 118.7: if the player's life total can't change, the exchange doesn't occur
        if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s life total can't change. Exchange doesn't occur."));
            return;
        }

        if (currentLife == currentStat) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " exchanges life total with ").card(source.getCard()).text("'s " + statName + " (both at " + currentLife + ").").build());
            return;
        }

        // Check if the life portion of the exchange is blocked by can't-gain-life
        boolean lifeWouldIncrease = currentStat > currentLife;
        if (lifeWouldIncrease && !gameQueryService.canPlayerGainLife(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " can't gain life. Exchange doesn't occur."));
            return;
        }

        String playerName = gameData.playerIdToName.get(playerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " exchanges life total with ").card(source.getCard()).text("'s " + statName + " (" + playerName + ": " + currentLife + " -> " + currentStat + ", ").card(source.getCard()).text(" " + statName + ": " + currentStat + " -> " + currentLife + ").").build());

        // Set player's life total to the creature's stat
        gameData.playerLifeTotals.put(playerId, currentStat);
        if (currentStat > currentLife) {
            triggerCollectionService.checkLifeGainTriggers(gameData, playerId, currentStat - currentLife);
        } else {
            triggerCollectionService.checkLifeLossTriggers(gameData, playerId, currentLife - currentStat);
        }

        // Set creature's base stat to the player's former life total (layer 7b setting effect,
        // CR 613.4b). The timestamp orders the override against other 7b setters in the layered pass.
        if (usePower) {
            source.setBasePowerOverriddenPermanently(true);
            source.setPermanentBasePowerOverride(currentLife);
            source.setPermanentBasePowerOverrideTimestamp(gameData.nextTimestamp());
        } else {
            source.setBaseToughnessOverriddenPermanently(true);
            source.setPermanentBaseToughnessOverride(currentLife);
            source.setPermanentBaseToughnessOverrideTimestamp(gameData.nextTimestamp());
        }

        log.info("Game {} - {} exchanges life ({} -> {}) with {}'s {} ({} -> {})",
                gameData.id, playerName, currentLife, currentStat,
                source.getCard().getName(), statName, currentStat, currentLife);
    }
}
