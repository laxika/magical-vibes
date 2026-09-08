package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfEachKindToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddAnotherCounterOfEachKindToTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddAnotherCounterOfEachKindToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = !targetIds.isEmpty() ? targetIds.getFirst() : entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
        if (permanent != null) {
            addCountersToPermanent(gameData, entry, permanent);
        } else if (gameData.playerIdToName.containsKey(targetId)) {
            addCountersToPlayer(gameData, entry, targetId);
        }
    }

    private void addCountersToPermanent(GameData gameData, StackEntry entry, Permanent target) {
        List<CounterType> counterTypes = new ArrayList<>();
        for (CounterType counterType : CounterType.values()) {
            if (counterType != CounterType.ANY && counterType != CounterType.SILVER
                    && target.getCounterCount(counterType) > 0) {
                counterTypes.add(counterType);
            }
        }

        for (CounterType counterType : counterTypes) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, counterType, 1);
        }
    }

    private void addCountersToPlayer(GameData gameData, StackEntry entry, UUID playerId) {
        if (gameData.playerPoisonCounters.getOrDefault(playerId, 0) > 0) {
            lifeSupport.applyPoisonCounters(gameData, playerId, 1, entry.getCard().getName());
        }

        if (gameData.playerEnergyCounters.getOrDefault(playerId, 0) > 0) {
            int energyAmount = gameQueryService.replaceEnergyCounters(gameData, playerId, 1);
            if (energyAmount > 0) {
                gameData.playerEnergyCounters.merge(playerId, energyAmount, Integer::sum);
                String playerName = gameData.playerIdToName.getOrDefault(playerId, "Player");
                gameLogService.append(gameData,
                        GameLog.text(playerName + " gets " + energyAmount + " energy counter"
                                + (energyAmount > 1 ? "s" : "") + " (" + entry.getCard().getName() + ")."));
                triggerCollectionService.checkEnergyGainTriggers(gameData, playerId, energyAmount);
            }
        }
    }
}
