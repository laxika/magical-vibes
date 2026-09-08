package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveCounterFromTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterFromTargetPermanentEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Specific counter type ("remove a -1/-1 counter", "remove up to four charge counters"):
        // remove as many of exactly that type as are there, up to the effect's amount.
        if (e.counterType() != null) {
            removeUpTo(gameData, target, e.counterType(), e.amount());
            return;
        }

        for (CounterType counterType : CounterType.values()) {
            // ANY and SILVER are wildcard categories, not concrete stored counters.
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            if (removeUpTo(gameData, target, counterType, e.amount())) {
                return;
            }
        }
    }

    /** Removes up to {@code amount} counters of one type; returns whether any came off. */
    private boolean removeUpTo(GameData gameData, Permanent target, CounterType counterType, int amount) {
        int removed = Math.min(amount, target.getCounterCount(counterType));
        if (removed <= 0) {
            return false;
        }

        target.setCounterCount(counterType, target.getCounterCount(counterType) - removed);
        if (counterType == CounterType.OIL) {
            gameData.recordOilCounterRemoved(target, removed);
        }
        String counterName = permanentCounterSupport.counterTypeName(counterType);
        String prefix = removed == 1
                ? "A " + counterName + " counter removed from "
                : removed + " " + counterName + " counters removed from ";
        gameLogService.append(gameData, GameLog.textCardText(prefix, target.getCard(), "."));
        log.info("Game {} - {} {} counter(s) removed from {}", gameData.id, removed, counterType, target.getCard().getName());
        return true;
    }
}
