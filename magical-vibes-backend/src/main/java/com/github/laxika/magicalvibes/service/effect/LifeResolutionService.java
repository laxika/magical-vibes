package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesXLifeAndControllerGainsLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesLifePerCreatureControlledEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeForEachSubtypeOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCreatureOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
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
public class LifeResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @HandlesEffect(GainLifeEffect.class)
    private void resolveGainLife(GameData gameData, StackEntry entry, GainLifeEffect effect) {
        applyGainLife(gameData, entry.getControllerId(), effect.amount());
    }

    private void applyGainLife(GameData gameData, UUID controllerId, int amount) {
        Integer currentLife = gameData.playerLifeTotals.get(controllerId);
        gameData.playerLifeTotals.put(controllerId, currentLife + amount);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " gains " + amount + " life.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gains {} life", gameData.id, playerName, amount);
    }

    @HandlesEffect(GainLifePerCreatureOnBattlefieldEffect.class)
    private void resolveGainLifePerCreatureOnBattlefield(GameData gameData, StackEntry entry) {
        int[] creatureCountHolder = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatureCountHolder[0]++;
            }
        });
        int creatureCount = creatureCountHolder[0];
        if (creatureCount == 0) {
            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            String logEntry = playerName + " gains no life (no creatures on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        applyGainLife(gameData, entry.getControllerId(), creatureCount);
    }

    @HandlesEffect(GainLifeForEachSubtypeOnBattlefieldEffect.class)
    private void resolveGainLifeForEachSubtypeOnBattlefield(GameData gameData, StackEntry entry,
                                                             GainLifeForEachSubtypeOnBattlefieldEffect effect) {
        int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().getSubtypes().contains(effect.subtype())) {
                count[0]++;
            }
        });
        if (count[0] == 0) {
            return;
        }
        applyGainLife(gameData, entry.getControllerId(), count[0]);
    }

    @HandlesEffect(GainLifePerControlledCreatureEffect.class)
    private void resolveGainLifePerControlledCreature(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int creatureCount = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatureCount++;
                }
            }
        }
        if (creatureCount == 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " gains no life (no creatures on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        applyGainLife(gameData, controllerId, creatureCount);
    }

    @HandlesEffect(GainLifePerGraveyardCardEffect.class)
    private void resolveGainLifePerGraveyardCard(GameData gameData, StackEntry entry) {
        applyGainLifePerGraveyardCard(gameData, entry.getControllerId());
    }

    private void applyGainLifePerGraveyardCard(GameData gameData, UUID controllerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        int amount = graveyard != null ? graveyard.size() : 0;
        if (amount == 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " has no cards in their graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no graveyard cards for life gain", gameData.id, playerName);
            return;
        }
        applyGainLife(gameData, controllerId, amount);
    }

    @HandlesEffect(GainLifeEqualToTargetToughnessEffect.class)
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

    @HandlesEffect(GainLifeEqualToChargeCountersOnSourceEffect.class)
    private void resolveGainLifeEqualToChargeCounters(GameData gameData, StackEntry entry) {
        int count = entry.getXValue();
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (count <= 0) {
            String logEntry = playerName + " gains 0 life from " + entry.getCard().getName() + " (no charge counters).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} gains 0 life from {} (no charge counters)", gameData.id, playerName, entry.getCard().getName());
            return;
        }

        int currentLife = gameData.playerLifeTotals.getOrDefault(controllerId, 20);
        gameData.playerLifeTotals.put(controllerId, currentLife + count);

        String logEntry = playerName + " gains " + count + " life from " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gains {} life from {}", gameData.id, playerName, count, entry.getCard().getName());
    }

    @HandlesEffect(DoubleTargetPlayerLifeEffect.class)
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

    @HandlesEffect(EnchantedCreatureControllerLosesLifeEffect.class)
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

    @HandlesEffect(LoseLifeEffect.class)
    private void resolveLoseLife(GameData gameData, StackEntry entry, LoseLifeEffect effect) {
        applyLifeLoss(gameData, entry.getControllerId(), effect.amount(), entry.getCard().getName());
    }

    @HandlesEffect(EachOpponentLosesLifeEffect.class)
    private void resolveEachOpponentLosesLife(GameData gameData, StackEntry entry, EachOpponentLosesLifeEffect effect) {
        UUID controllerId = entry.getControllerId();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            applyLifeLoss(gameData, playerId, effect.amount(), entry.getCard().getName());
        }
    }

    @HandlesEffect(EachOpponentLosesXLifeAndControllerGainsLifeLostEffect.class)
    private void resolveEachOpponentLosesXLifeAndControllerGainsLifeLost(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        int x = entry.getXValue();

        if (x <= 0) {
            return;
        }

        int totalLifeLost = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            applyLifeLoss(gameData, playerId, x, entry.getCard().getName());
            totalLifeLost += x;
        }

        if (totalLifeLost > 0) {
            applyGainLife(gameData, controllerId, totalLifeLost);
        }
    }

    @HandlesEffect(EachPlayerLosesLifePerCreatureControlledEffect.class)
    private void resolveEachPlayerLosesLifePerCreatureControlled(GameData gameData, StackEntry entry,
                                                                 EachPlayerLosesLifePerCreatureControlledEffect effect) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            int creatureCount = 0;
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getType() == CardType.CREATURE) {
                    creatureCount++;
                }
            }

            int lifeLoss = creatureCount * effect.lifePerCreature();
            if (lifeLoss > 0) {
                applyLifeLoss(gameData, playerId, lifeLoss, entry.getCard().getName());
            }
        });
    }

    private void applyLifeLoss(GameData gameData, UUID playerId, int amount, String sourceName) {
        int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
        gameData.playerLifeTotals.put(playerId, currentLife - amount);

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " loses " + amount + " life (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, amount, sourceName);
    }

    @HandlesEffect(TargetPlayerLosesLifeAndControllerGainsLifeEffect.class)
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
        applyGainLife(gameData, controllerId, effect.lifeGain());
    }

    @HandlesEffect(TargetPlayerGainsLifeEffect.class)
    private void resolveTargetPlayerGainsLife(GameData gameData, StackEntry entry, TargetPlayerGainsLifeEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        if (targetPlayerId == null) {
            return;
        }
        applyGainLife(gameData, targetPlayerId, effect.amount());
    }
}

