package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllCountersFromTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersFromTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            entry.setEventValue(0);
            return;
        }

        int removed = 0;
        int oilRemoved = target.getCounterCount(CounterType.OIL);
        int timeCountersBefore = target.getCounterCount(CounterType.TIME);
        for (CounterType counterType : CounterType.values()) {
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            removed += target.getCounterCount(counterType);
            target.setCounterCount(counterType, 0);
        }
        gameData.recordOilCounterRemoved(target, oilRemoved);
        permanentCounterSupport.checkLastTimeCounterRemoved(gameData, target, CounterType.TIME, timeCountersBefore);
        entry.setEventValue(removed);

        if (removed > 0) {
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" removes all counters from " + target.getCard().getName() + " (" + removed + ").")
                    .build());
        }
        log.info("Game {} - {} removes {} counter(s) from {}", gameData.id,
                entry.getCard().getName(), removed, target.getCard().getName());
    }
}
