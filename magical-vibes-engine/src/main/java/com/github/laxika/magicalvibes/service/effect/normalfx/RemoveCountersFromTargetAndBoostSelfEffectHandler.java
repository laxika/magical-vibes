package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromTargetAndBoostSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveCountersFromTargetAndBoostSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCountersFromTargetAndBoostSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        int maxToRemove = entry.getXValue();
        int totalRemoved = 0;

        // Remove counters from all counter types, up to X total
        // Order: +1/+1, charge, loyalty, -1/-1, awakening
        int remaining = maxToRemove;

        if (remaining > 0 && target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0) {
            int remove = Math.min(remaining, target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE));
            target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getCounterCount(CounterType.CHARGE) > 0) {
            int remove = Math.min(remaining, target.getCounterCount(CounterType.CHARGE));
            target.setCounterCount(CounterType.CHARGE, target.getCounterCount(CounterType.CHARGE) - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getCounterCount(CounterType.LOYALTY) > 0) {
            int remove = Math.min(remaining, target.getCounterCount(CounterType.LOYALTY));
            target.setCounterCount(CounterType.LOYALTY, target.getCounterCount(CounterType.LOYALTY) - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getCounterCount(CounterType.PHYLACTERY) > 0) {
            int remove = Math.min(remaining, target.getCounterCount(CounterType.PHYLACTERY));
            target.setCounterCount(CounterType.PHYLACTERY, target.getCounterCount(CounterType.PHYLACTERY) - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getCounterCount(CounterType.SLIME) > 0) {
            int remove = Math.min(remaining, target.getCounterCount(CounterType.SLIME));
            target.setCounterCount(CounterType.SLIME, target.getCounterCount(CounterType.SLIME) - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getCounterCount(CounterType.HATCHLING) > 0) {
            int remove = Math.min(remaining, target.getCounterCount(CounterType.HATCHLING));
            target.setCounterCount(CounterType.HATCHLING, target.getCounterCount(CounterType.HATCHLING) - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getCounterCount(CounterType.STUDY) > 0) {
            int remove = Math.min(remaining, target.getCounterCount(CounterType.STUDY));
            target.setCounterCount(CounterType.STUDY, target.getCounterCount(CounterType.STUDY) - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0) {
            int remove = Math.min(remaining, target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE));
            target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getCounterCount(CounterType.AWAKENING) > 0) {
            int remove = Math.min(remaining, target.getCounterCount(CounterType.AWAKENING));
            target.setCounterCount(CounterType.AWAKENING, target.getCounterCount(CounterType.AWAKENING) - remove);
            totalRemoved += remove;
        }

        if (totalRemoved > 0) {
            String logEntry = totalRemoved + " counter(s) removed from " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} counter(s) removed from {}", gameData.id, totalRemoved, target.getCard().getName());
        }

        // Boost source creature +1/+0 per counter removed
        if (totalRemoved > 0) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                source.setPowerModifier(source.getPowerModifier() + totalRemoved);
                String boostLog = source.getCard().getName() + " gets +" + totalRemoved + "/+0 until end of turn.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(boostLog));
                log.info("Game {} - {} gets +{}/+0", gameData.id, source.getCard().getName(), totalRemoved);
            }
        }
    }
}
