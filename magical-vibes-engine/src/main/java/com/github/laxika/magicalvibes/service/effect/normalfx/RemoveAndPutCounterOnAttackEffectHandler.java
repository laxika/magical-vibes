package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAndPutCounterOnAttackEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link RemoveAndPutCounterOnAttackEffect} (Decimator Beetle's attack trigger). Reads the two
 * targets from the entry's flat {@code targetIds}: position 0 = the creature you control (remove one
 * counter, no-op if none), position 1 = the optional creature the defending player controls (put one
 * counter, firing the normal put triggers and respecting can't-have). The two halves are independent —
 * either resolves even if the other's target is gone or had nothing to remove.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAndPutCounterOnAttackEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAndPutCounterOnAttackEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterType counterType = ((RemoveAndPutCounterOnAttackEffect) effect).counterType();
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.isEmpty()) {
            return;
        }

        Permanent removeFrom = gameQueryService.findPermanentById(gameData, targets.get(0));
        if (removeFrom != null && removeFrom.getCounterCount(counterType) > 0) {
            removeFrom.setCounterCount(counterType, removeFrom.getCounterCount(counterType) - 1);
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.textCardText("A " + counterType + " counter removed from ", removeFrom.getCard(), "."));
            log.info("Game {} - {} counter removed from {}", gameData.id, counterType, removeFrom.getCard().getName());
        }

        if (targets.size() > 1) {
            Permanent putOn = gameQueryService.findPermanentById(gameData, targets.get(1));
            if (putOn != null && canReceiveCounter(gameData, putOn, counterType)) {
                permanentCounterSupport.placeCounterOnPermanent(gameData, entry, putOn, counterType, 1);
            }
        }
    }

    private boolean canReceiveCounter(GameData gameData, Permanent target, CounterType counterType) {
        if (gameQueryService.cantHaveCounters(gameData, target)) {
            return false;
        }
        return counterType != CounterType.MINUS_ONE_MINUS_ONE
                || !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target);
    }
}
