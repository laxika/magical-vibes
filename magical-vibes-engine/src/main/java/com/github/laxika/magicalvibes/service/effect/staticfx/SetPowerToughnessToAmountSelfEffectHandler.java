package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Self-only (characteristic-defining) static handler for {@link SetPowerToughnessToAmountEffect}
 * in the {@code STATIC} slot: evaluates the power/toughness {@code DynamicAmount}s via
 * {@link AmountEvaluationService} and adds the results as a continuous self bonus on the
 * 0/0 base. Replaces the former per-derivation {@code PowerToughnessEqualTo*} self handlers.
 */
@Component
@RequiredArgsConstructor
public class SetPowerToughnessToAmountSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetPowerToughnessToAmountEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var cda = (SetPowerToughnessToAmountEffect) effect;
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        AmountContext ctx = AmountContext.forStaticEffect(context.source(), controllerId);
        accumulator.addPower(amountEvaluationService.evaluate(context.gameData(), cda.power(), ctx));
        accumulator.addToughness(amountEvaluationService.evaluate(context.gameData(), cda.toughness(), ctx));
    }
}
