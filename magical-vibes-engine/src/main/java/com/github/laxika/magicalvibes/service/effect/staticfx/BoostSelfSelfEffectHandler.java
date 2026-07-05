package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Self-only (characteristic-defining) static handler for {@link BoostSelfEffect} in the
 * {@code STATIC} slot: evaluates the {@code DynamicAmount}s via {@link AmountEvaluationService}
 * and adds the result as a continuous self bonus. Replaces the former per-derivation
 * {@code BoostSelfPer*} self handler classes.
 */
@Component
@RequiredArgsConstructor
public class BoostSelfSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfEffect) effect;
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        AmountContext ctx = AmountContext.forStaticEffect(context.source(), controllerId);
        accumulator.addPower(amountEvaluationService.evaluate(context.gameData(), boost.powerBoost(), ctx));
        accumulator.addToughness(amountEvaluationService.evaluate(context.gameData(), boost.toughnessBoost(), ctx));
    }
}
