package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEqualToAmountEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a dynamic optional life payment by materializing the ordinary may-pay flow. */
@Component
@RequiredArgsConstructor
public class MayPayLifeEqualToAmountEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final MayPayLifeEffectResolutionHandler mayPayLifeEffectResolutionHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayLifeEqualToAmountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayPayLifeEqualToAmountEffect dynamicEffect = (MayPayLifeEqualToAmountEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        int amount = Math.max(0, amountEvaluationService.evaluate(gameData, dynamicEffect.amount(),
                AmountContext.forStackEntry(entry, source)));
        mayPayLifeEffectResolutionHandler.resolve(gameData, entry,
                new MayPayLifeEffect(amount, dynamicEffect.wrapped(), dynamicEffect.prompt()));
    }
}
