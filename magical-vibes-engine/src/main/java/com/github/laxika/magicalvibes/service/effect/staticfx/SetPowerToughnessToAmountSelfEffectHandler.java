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
 * {@link AmountEvaluationService} and records the result as the CR 613.4a sublayer-7a base P/T.
 * A layer-7b setter (Diminish, Lignify) applies after 7a and overrides this base — the
 * static-bonus assembly merges the layered 7b winner over the override written here. Replaces
 * the former per-derivation {@code PowerToughnessEqualTo*} self handlers.
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
        accumulator.setBasePTOverride(
                amountEvaluationService.evaluate(context.gameData(), cda.power(), ctx),
                amountEvaluationService.evaluate(context.gameData(), cda.toughness(), ctx));
    }
}
