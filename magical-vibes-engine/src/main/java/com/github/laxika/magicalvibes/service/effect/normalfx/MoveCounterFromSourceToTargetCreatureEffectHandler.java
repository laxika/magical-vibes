package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MoveCounterFromSourceToTargetCreatureEffect}: removes counters of the given type from the permanent
 * they move from and places them on the other. Nothing moves when either left the battlefield, both are one
 * permanent, there is no such counter, or the counter can't be put on (CR 122.5).
 */
@Component
@RequiredArgsConstructor
public class MoveCounterFromSourceToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveCounterFromSourceToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MoveCounterFromSourceToTargetCreatureEffect moveEffect = (MoveCounterFromSourceToTargetCreatureEffect) effect;
        CounterType counterType = moveEffect.counterType();

        Permanent source = subjectResolutionService.permanent(gameData, entry, effect, Subjects.source());
        Permanent from = subjectResolutionService.permanent(gameData, entry, effect, moveEffect.from());
        Permanent to = subjectResolutionService.permanent(gameData, entry, effect, moveEffect.to());
        if (from == null || to == null || from.getId().equals(to.getId())
                || from.getCounterCount(counterType) <= 0) {
            return;
        }
        int amount = amountEvaluationService.evaluate(gameData, moveEffect.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) {
            return;
        }
        if (gameQueryService.cantHaveCounters(gameData, to)
                || counterType == CounterType.PLUS_ONE_PLUS_ONE
                && gameQueryService.cantHavePlusOnePlusOneCounters(gameData, to)
                || counterType == CounterType.MINUS_ONE_MINUS_ONE
                && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, to)) {
            return;
        }

        int moved = Math.min(amount, from.getCounterCount(counterType));
        from.setCounterCount(counterType, from.getCounterCount(counterType) - moved);
        if (counterType == CounterType.OIL) {
            gameData.recordOilCounterRemoved(from, moved);
        }
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, to, counterType, moved);
    }
}
