package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DynamicStaticBoostSelfEffectHandler implements StaticEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DynamicStaticBoostEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (DynamicStaticBoostEffect) effect;
        if (boost.scope() != GrantScope.SELF
                && boost.scope() != GrantScope.ALL_OWN_CREATURES
                && boost.scope() != GrantScope.ALL_CREATURES_INCLUDING_SELF) {
            return;
        }
        AmountContext amountContext = AmountContext.forStaticEffect(context.source(), context.sourceControllerId());
        accumulator.addPower(amountEvaluationService.evaluate(
                context.gameData(), boost.powerBoost(), amountContext));
        accumulator.addToughness(amountEvaluationService.evaluate(
                context.gameData(), boost.toughnessBoost(), amountContext));
    }
}
