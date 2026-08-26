package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnControlledCreaturesByPowerAboveBasePowerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves power-above-base-power counter placement for controlled creatures. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCountersOnControlledCreaturesByPowerAboveBasePowerEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnControlledCreaturesByPowerAboveBasePowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersOnControlledCreaturesByPowerAboveBasePowerEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        int totalPlaced = 0;
        int creatureCount = 0;
        List<Permanent> plusOneTargets = new ArrayList<>();
        Map<Permanent, Integer> minusOneTargets = new LinkedHashMap<>();
        Map<Permanent, Integer> amountsByCreature = new LinkedHashMap<>();
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }

            GameQueryService.StaticBonus bonus = gameQueryService.computeStaticBonus(gameData, permanent);
            int basePower = bonus.basePTOverridden()
                    ? bonus.basePowerOverride()
                    : permanent.getBasePower();
            int amount = gameQueryService.getEffectivePower(permanent, bonus) - basePower;
            if (amount > 0) {
                amountsByCreature.put(permanent, amount);
            }
        }

        for (Map.Entry<Permanent, Integer> target : amountsByCreature.entrySet()) {
            Permanent permanent = target.getKey();
            int amount = target.getValue();
            if (gameQueryService.cantHaveCounters(gameData, permanent)) {
                continue;
            }
            if (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE
                    && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent)) {
                continue;
            }
            if (e.counterType() == CounterType.PLUS_ONE_PLUS_ONE
                    && gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent)) {
                continue;
            }

            int placed = gameQueryService.replaceCounters(
                    gameData, permanent, e.counterType(), amount, entry.getControllerId());
            if (placed <= 0) {
                continue;
            }

            permanent.setCounterCount(e.counterType(),
                    permanent.getCounterCount(e.counterType()) + placed);
            permanentCounterSupport.notifyCountersPlaced(gameData, entry, permanent, placed);
            totalPlaced += placed;
            creatureCount++;
            if (e.counterType() == CounterType.PLUS_ONE_PLUS_ONE) {
                permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                        gameData, permanent, placed);
                plusOneTargets.add(permanent);
            } else if (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE) {
                minusOneTargets.put(permanent, placed);
            }
        }

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        String counterText = totalPlaced == 1
                ? "a " + counterName + " counter"
                : totalPlaced + " " + counterName + " counters";
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" puts " + counterText + " on " + creatureCount + " creature(s).")
                .build());
        log.info("Game {} - {} puts {} {} counter(s) on {} controlled creature(s)",
                gameData.id, entry.getCard().getName(), totalPlaced, counterName, creatureCount);

        for (Permanent permanent : plusOneTargets) {
            permanentCounterSupport.firePlusOnePlusOneCounterTriggers(gameData, permanent);
        }
        for (Map.Entry<Permanent, Integer> placement : minusOneTargets.entrySet()) {
            permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(
                    gameData, placement.getKey(), placement.getValue());
        }
    }
}
