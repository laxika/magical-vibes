package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies dynamic base power/toughness setters such as an Equipment's life-total ability. */
@Component
@RequiredArgsConstructor
public class SetBasePowerToughnessToAmountEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetBasePowerToughnessToAmountEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        SetBasePowerToughnessToAmountEffect setPT = (SetBasePowerToughnessToAmountEffect) effect;
        if (!support.matchesCreatureScope(context, setPT.scope(), null)) {
            return;
        }
        AmountContext amountContext = AmountContext.forStaticEffect(context.source(), context.sourceControllerId());
        accumulator.setBasePTOverride(
                amountEvaluationService.evaluate(context.gameData(), setPT.power(), amountContext),
                amountEvaluationService.evaluate(context.gameData(), setPT.toughness(), amountContext));
    }
}
