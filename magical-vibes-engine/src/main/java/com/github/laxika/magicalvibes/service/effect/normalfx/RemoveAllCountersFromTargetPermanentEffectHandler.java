package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveAllCountersFromTargetPermanentEffect}: removes every counter of every kind from each
 * permanent the effect's subject names, then runs the vanishing last-time-counter check and the battle's
 * defense check for it, and records the total removed as the entry's event value.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllCountersFromTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final BattleDefeatSupport battleDefeatSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersFromTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveAllCountersFromTargetPermanentEffect) effect;
        int total = 0;
        for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect, e.subject())) {
            total += removeAll(gameData, entry, target);
        }
        entry.setEventValue(total);
    }

    private int removeAll(GameData gameData, StackEntry entry, Permanent target) {
        int removed = 0;
        int oilRemoved = target.getCounterCount(CounterType.OIL);
        int timeCountersBefore = target.getCounterCount(CounterType.TIME);
        boolean hadDefenseCounters = target.getCounterCount(CounterType.DEFENSE) > 0;
        for (CounterType counterType : CounterType.values()) {
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            removed += target.getCounterCount(counterType);
            target.setCounterCount(counterType, 0);
        }
        gameData.recordOilCounterRemoved(target, oilRemoved);
        permanentCounterSupport.checkLastTimeCounterRemoved(gameData, target, CounterType.TIME, timeCountersBefore);
        if (hadDefenseCounters) {
            battleDefeatSupport.checkAfterDefenseRemoved(gameData, target);
        }

        if (removed > 0) {
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" removes all counters from " + target.getCard().getName() + " (" + removed + ").")
                    .build());
        }
        log.info("Game {} - {} removes {} counter(s) from {}", gameData.id,
                entry.getCard().getName(), removed, target.getCard().getName());
        return removed;
    }
}
