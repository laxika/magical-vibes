package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.effect.PopulateNTimesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Expands an X-times populate instruction into ordinary populate resolutions. */
@Component
@RequiredArgsConstructor
public class PopulateNTimesEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PopulateNTimesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PopulateNTimesEffect populateNTimes = (PopulateNTimesEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int repetitions = amountEvaluationService.evaluate(gameData, populateNTimes.times(),
                AmountContext.forStackEntry(entry, source));
        if (repetitions <= 0) {
            return;
        }

        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("PopulateNTimesEffect is not part of the resolving entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1,
                java.util.Collections.nCopies(repetitions, new PopulateEffect()));
    }
}
