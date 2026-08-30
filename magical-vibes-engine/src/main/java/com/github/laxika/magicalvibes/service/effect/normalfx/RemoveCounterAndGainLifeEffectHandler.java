package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterRemovalSubject;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndGainLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveCounterAndGainLifeEffect}: removes one counter of the given type from the
 * effect's subject, and the controller gains life only if a counter was actually removed
 * ("If you do").
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveCounterAndGainLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterAndGainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterAndGainLifeEffect) effect;
        UUID subjectId = e.subject() == CounterRemovalSubject.SOURCE && entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId()
                : entry.getTargetId();
        Permanent subject = gameQueryService.findPermanentById(gameData, subjectId);
        if (subject == null) {
            return;
        }

        if (subject.getCounterCount(e.counterType()) <= 0) {
            // No counter to remove -> "If you do" fails, no life gained.
            return;
        }

        subject.setCounterCount(e.counterType(), subject.getCounterCount(e.counterType()) - 1);
        if (e.counterType() == com.github.laxika.magicalvibes.model.CounterType.OIL) {
            gameData.recordOilCounterRemoved(subject, 1);
        }
        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        gameLogService.append(gameData, GameLog.textCardText("A " + counterName + " counter removed from ", subject.getCard(), "."));
        log.info("Game {} - {} counter removed from {}", gameData.id, e.counterType(), subject.getCard().getName());

        lifeSupport.applyGainLife(gameData, entry.getControllerId(), e.lifeGain(), null,
                entry.getCard(), entry.getEntryType());
    }
}
