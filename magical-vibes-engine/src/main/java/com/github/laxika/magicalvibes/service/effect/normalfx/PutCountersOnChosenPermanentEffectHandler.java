package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnChosenPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves counter placement on a permanent recorded by an earlier resolution-time choice. */
@Component
@RequiredArgsConstructor
public class PutCountersOnChosenPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnChosenPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getChosenPermanentId() == null) {
            return;
        }

        Permanent chosen = gameQueryService.findPermanentById(gameData, entry.getChosenPermanentId());
        if (chosen == null) {
            return;
        }

        PutCountersOnChosenPermanentEffect counters = (PutCountersOnChosenPermanentEffect) effect;
        int count = amountEvaluationService.evaluate(gameData, counters.amount(),
                AmountContext.forStackEntry(entry, null));
        if (count > 0) {
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, chosen, counters.counterType(), count);
        }
    }
}
