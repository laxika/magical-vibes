package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LifeResolutionService implements EffectHandlerProvider {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(GainLifeEffect.class,
                (gd, entry, effect) -> resolveGainLife(gd, entry.getControllerId(), ((GainLifeEffect) effect).amount()));
        registry.register(GainLifePerGraveyardCardEffect.class,
                (gd, entry, effect) -> resolveGainLifePerGraveyardCard(gd, entry.getControllerId()));
        registry.register(GainLifeEqualToTargetToughnessEffect.class,
                (gd, entry, effect) -> resolveGainLifeEqualToTargetToughness(gd, entry));
        registry.register(DoubleTargetPlayerLifeEffect.class,
                (gd, entry, effect) -> resolveDoubleTargetPlayerLife(gd, entry));
        registry.register(EnchantedCreatureControllerLosesLifeEffect.class,
                (gd, entry, effect) -> resolveEnchantedCreatureControllerLosesLife(gd, entry, (EnchantedCreatureControllerLosesLifeEffect) effect));
        registry.register(LoseLifeEffect.class,
                (gd, entry, effect) -> resolveLoseLife(gd, entry, (LoseLifeEffect) effect));
        registry.register(TargetPlayerLosesLifeAndControllerGainsLifeEffect.class,
                (gd, entry, effect) -> resolveTargetPlayerLosesLifeAndControllerGainsLife(gd, entry, (TargetPlayerLosesLifeAndControllerGainsLifeEffect) effect));
    }

    private void resolveGainLife(GameData gameData, UUID controllerId, int amount) {
        Integer currentLife = gameData.playerLifeTotals.get(controllerId);
        gameData.playerLifeTotals.put(controllerId, currentLife + amount);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " gains " + amount + " life.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gains {} life", gameData.id, playerName, amount);
    }

    private void resolveGainLifePerGraveyardCard(GameData gameData, UUID controllerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        int amount = graveyard != null ? graveyard.size() : 0;
        if (amount == 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " has no cards in their graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no graveyard cards for life gain", gameData.id, playerName);
            return;
        }
        resolveGainLife(gameData, controllerId, amount);
    }

    private void resolveGainLifeEqualToTargetToughness(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        int toughness = gameQueryService.getEffectiveToughness(gameData, target);

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(target)) {
                int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
                gameData.playerLifeTotals.put(playerId, currentLife + toughness);

                String logEntry = gameData.playerIdToName.get(playerId) + " gains " + toughness + " life.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);

                log.info("Game {} - {} gains {} life (equal to {}'s toughness)",
                        gameData.id, gameData.playerIdToName.get(playerId), toughness, target.getCard().getName());
                break;
            }
        }
    }

    private void resolveDoubleTargetPlayerLife(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();

        int currentLife = gameData.playerLifeTotals.getOrDefault(targetPlayerId, 20);
        int newLife = currentLife * 2;
        gameData.playerLifeTotals.put(targetPlayerId, newLife);

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = playerName + "'s life total is doubled from " + currentLife + " to " + newLife + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {}'s life doubled from {} to {}", gameData.id, playerName, currentLife, newLife);
    }

    private void resolveEnchantedCreatureControllerLosesLife(GameData gameData, StackEntry entry, EnchantedCreatureControllerLosesLifeEffect effect) {
        UUID playerId = effect.affectedPlayerId();
        if (playerId == null) return;

        int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
        gameData.playerLifeTotals.put(playerId, currentLife - effect.amount());

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " loses " + effect.amount() + " life (" + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, effect.amount(), entry.getCard().getName());
    }

    private void resolveLoseLife(GameData gameData, StackEntry entry, LoseLifeEffect effect) {
        UUID controllerId = entry.getControllerId();
        int currentLife = gameData.playerLifeTotals.getOrDefault(controllerId, 20);
        gameData.playerLifeTotals.put(controllerId, currentLife - effect.amount());

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " loses " + effect.amount() + " life (" + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, effect.amount(), entry.getCard().getName());
    }

    private void resolveTargetPlayerLosesLifeAndControllerGainsLife(GameData gameData, StackEntry entry, TargetPlayerLosesLifeAndControllerGainsLifeEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        UUID controllerId = entry.getControllerId();

        // Target loses life
        int targetCurrentLife = gameData.playerLifeTotals.getOrDefault(targetPlayerId, 20);
        gameData.playerLifeTotals.put(targetPlayerId, targetCurrentLife - effect.lifeLoss());

        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String lossLog = targetName + " loses " + effect.lifeLoss() + " life (" + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, lossLog);
        log.info("Game {} - {} loses {} life from {}", gameData.id, targetName, effect.lifeLoss(), entry.getCard().getName());

        // Controller gains life
        resolveGainLife(gameData, controllerId, effect.lifeGain());
    }
}

