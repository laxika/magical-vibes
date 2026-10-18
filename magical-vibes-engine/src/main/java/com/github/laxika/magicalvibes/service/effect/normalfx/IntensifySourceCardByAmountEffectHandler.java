package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceCardByAmountEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an intensity increase whose size is a dynamic amount, on one physical card identity. */
@Component
@RequiredArgsConstructor
public class IntensifySourceCardByAmountEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IntensifySourceCardByAmountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var intensify = (IntensifySourceCardByAmountEffect) effect;
        int amount = amountEvaluationService.evaluate(gameData, intensify.amount(),
                AmountContext.forStackEntry(entry, null));
        gameData.intensifyCard(entry.getCard(), amount);
    }
}
