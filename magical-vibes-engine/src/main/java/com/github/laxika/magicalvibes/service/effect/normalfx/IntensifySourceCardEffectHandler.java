package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceCardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an intensity increase on one physical card identity, its amount read as the effect resolves. */
@Component
@RequiredArgsConstructor
public class IntensifySourceCardEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IntensifySourceCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        IntensifySourceCardEffect intensify = (IntensifySourceCardEffect) effect;
        int amount = amountEvaluationService.evaluate(gameData, intensify.amount(),
                AmountContext.forStackEntry(entry, null));
        gameData.intensifyCard(entry.getCard(), amount);
    }
}
