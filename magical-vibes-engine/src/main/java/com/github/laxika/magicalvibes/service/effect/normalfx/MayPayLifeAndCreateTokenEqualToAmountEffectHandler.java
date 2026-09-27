package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndCreateTokenEqualToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves dynamic optional life payments that create an X/X token. */
@Component
@RequiredArgsConstructor
public class MayPayLifeAndCreateTokenEqualToAmountEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final MayPayLifeEffectResolutionHandler mayPayLifeEffectResolutionHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayLifeAndCreateTokenEqualToAmountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayPayLifeAndCreateTokenEqualToAmountEffect dynamicEffect =
                (MayPayLifeAndCreateTokenEqualToAmountEffect) effect;
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

        var tokenEffect = dynamicEffect.tokenEffect().withPowerToughness(amount, amount);
        mayPayLifeEffectResolutionHandler.resolve(gameData, entry,
                new MayPayLifeEffect(amount, tokenEffect,
                        "Pay " + amount + " life to create a " + amount + "/" + amount
                                + " " + tokenEffect.tokenName() + " token?"));
    }
}
