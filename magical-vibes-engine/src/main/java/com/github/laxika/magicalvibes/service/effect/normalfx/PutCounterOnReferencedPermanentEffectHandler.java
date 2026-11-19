package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PutCounterOnReferencedPermanentEffect}: resolves the referenced permanent,
 * checks the optional condition against it, then hands it to
 * {@link PermanentCounterSupport#placeCounterOnPermanent}, which owns {@code cantHaveCounters},
 * +1/+1 doubling, -1/-1 reduction and the counter-placement triggers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCounterOnReferencedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnReferencedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterOnReferencedPermanentEffect e = (PutCounterOnReferencedPermanentEffect) effect;
        String sourceName = entry.getCard() != null ? entry.getCard().getName() : "Source";

        Permanent referenced = subjectResolutionService.permanent(gameData, entry, effect, Subjects.of(e.reference()));
        if (referenced == null) {
            log.info("Game {} - {}: the referenced permanent is not on the battlefield", gameData.id, sourceName);
            return;
        }

        if (e.condition() != null
                && !predicateEvaluationService.matchesPermanentPredicate(gameData, referenced, e.condition())) {
            log.info("Game {} - {}: referenced permanent does not match the counter condition", gameData.id, sourceName);
            return;
        }

        int count = amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, referenced));
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, referenced, e.counterType(), count);
    }
}
