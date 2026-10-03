package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnControllerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DoubleCountersOnControllerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleCountersOnControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getControllerId();
        int multiplier = MaroGoneNutsSupport.apply(gameData, effect, 2);
        String sourceName = entry.getCard().getName();

        doublePoisonCounters(gameData, entry, playerId, multiplier, sourceName);
        doubleEnergyCounters(gameData, playerId, multiplier, sourceName);
        doubleRadCounters(gameData, entry, playerId, multiplier, sourceName);
        doubleSimpleCounterMap(gameData, gameData.playerSparkCounters, playerId, multiplier,
                sourceName, "spark");
        doubleSimpleCounterMap(gameData, gameData.playerExperienceCounters, playerId, multiplier,
                sourceName, "experience");
    }

    private void doublePoisonCounters(GameData gameData, StackEntry entry, UUID playerId,
                                      int multiplier, String sourceName) {
        int current = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
        if (current > 0) {
            lifeSupport.applyPoisonCounters(gameData, playerId, current * multiplier - current,
                    sourceName, entry.getControllerId());
        }
    }

    private void doubleEnergyCounters(GameData gameData, UUID playerId, int multiplier,
                                      String sourceName) {
        int current = gameData.playerEnergyCounters.getOrDefault(playerId, 0);
        int amount = current > 0
                ? gameQueryService.replaceEnergyCounters(gameData, playerId, current * multiplier - current)
                : 0;
        if (amount <= 0) {
            return;
        }

        gameData.setPlayerEnergyCounters(playerId, current + amount);
        String playerName = gameData.playerIdToName.getOrDefault(playerId, "Player");
        gameLogService.append(gameData,
                GameLog.text(playerName + " gets " + amount + " energy counter"
                        + (amount > 1 ? "s" : "") + " (" + sourceName + ")."));
        triggerCollectionService.checkEnergyGainTriggers(gameData, playerId, amount);
    }

    private void doubleRadCounters(GameData gameData, StackEntry entry, UUID playerId,
                                   int multiplier, String sourceName) {
        int current = gameData.playerRadCounters.getOrDefault(playerId, 0);
        if (current > 0) {
            lifeSupport.applyRadCounters(gameData, playerId, current * multiplier - current,
                    sourceName, entry.getControllerId());
        }
    }

    private void doubleSimpleCounterMap(GameData gameData, Map<UUID, Integer> counters,
                                        UUID playerId, int multiplier, String sourceName,
                                        String counterName) {
        int current = counters.getOrDefault(playerId, 0);
        int amount = current > 0 ? current * multiplier - current : 0;
        if (amount <= 0) {
            return;
        }

        counters.merge(playerId, amount, Integer::sum);
        String playerName = gameData.playerIdToName.getOrDefault(playerId, "Player");
        gameLogService.append(gameData, GameLog.text(playerName + " gets " + amount + " "
                + counterName + " counter" + (amount > 1 ? "s" : "") + " (" + sourceName + ")."));
    }
}
