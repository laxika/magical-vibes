package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterRemovalSubject;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
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

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveAllCountersEffect) effect;
        UUID subjectId = e.subject() == CounterRemovalSubject.SOURCE && entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId()
                : targetOf(entry, effect);
        Permanent subject = gameQueryService.findPermanentById(gameData, subjectId);
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

    /**
     * The targeted subject: this effect's own target group when it is bound to one (Give // Take's
     * fuse mode chooses two creatures, so the flat list position matters), otherwise the entry's
     * lone target.
     */
    private static UUID targetOf(StackEntry entry, CardEffect effect) {
        var groupTargets = entry.targetsForEffect(effect);
        return groupTargets.isEmpty() ? entry.getTargetId() : groupTargets.getFirst();
    }
}
