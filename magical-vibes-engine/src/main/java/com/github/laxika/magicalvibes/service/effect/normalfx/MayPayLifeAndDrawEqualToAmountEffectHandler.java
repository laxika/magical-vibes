package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndDrawEqualToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves dynamic optional life payments by materializing the ordinary may-pay flow. */
@Component
@RequiredArgsConstructor
public class MayPayLifeAndDrawEqualToAmountEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final MayPayLifeEffectResolutionHandler mayPayLifeEffectResolutionHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayLifeAndDrawEqualToAmountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayPayLifeAndDrawEqualToAmountEffect dynamicEffect =
                (MayPayLifeAndDrawEqualToAmountEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        int amount = Math.max(0, amountEvaluationService.evaluate(gameData, dynamicEffect.amount(),
                AmountContext.forStackEntry(entry, source)));
        if (amount == 0) {
            return;
        }

        mayPayLifeEffectResolutionHandler.resolve(gameData, entry,
                new MayPayLifeEffect(amount, new DrawCardEffect(amount),
                        "Pay " + amount + " life to draw " + amount + " cards?"));
    }
}
