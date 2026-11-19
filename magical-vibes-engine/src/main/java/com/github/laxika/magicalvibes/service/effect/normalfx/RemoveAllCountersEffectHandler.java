package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveAllCountersEffect}: removes every counter of the given type from the
 * effect's subject and snapshots the removed count onto the stack entry as its event value, so a
 * later effect on the same entry can reference "that much" via an {@code EventValue} amount.
 */
@Component
@RequiredArgsConstructor
public class RemoveAllCountersEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveAllCountersEffect) effect;
        Permanent subject = subjectResolutionService.permanent(gameData, entry, effect, e.subject());
        if (subject == null) {
            entry.setEventValue(0);
            return;
        }

        int removed = subject.getCounterCount(e.counterType());
        subject.setCounterCount(e.counterType(), 0);
        if (e.counterType() == CounterType.OIL) {
            gameData.recordOilCounterRemoved(subject, removed);
        }
        entry.setEventValue(removed);

        if (removed > 0) {
            String counterName = permanentCounterSupport.counterTypeName(e.counterType());
            gameLogService.append(gameData, GameLog.builder().card(subject.getCard()).text(" removes all its " + counterName + " counters (" + removed + ").").build());
        }
    }
}
