package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Self-only (characteristic-defining) static handler for {@link BoostEffect#self} in the
 * {@code STATIC} slot: evaluates the {@code DynamicAmount}s via {@link AmountEvaluationService}
 * and adds the result as a continuous self bonus. Replaces the former per-derivation
 * {@code BoostSelfPer*} self handler classes.
 */
@Component
@RequiredArgsConstructor
public class BoostSelfSelfEffectHandler implements StaticEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostEffect) effect;
        requireStaticSelfBoost(boost);
        AmountContext ctx = AmountContext.forStaticEffect(context.source(), context.sourceControllerId());
        accumulator.addPower(amountEvaluationService.evaluate(context.gameData(), boost.powerBoost(), ctx));
        accumulator.addToughness(amountEvaluationService.evaluate(context.gameData(), boost.toughnessBoost(), ctx));
    }

    /**
     * Only "this creature gets +X/+Y" ({@link BoostEffect#self}) is a static self bonus, re-read at every layered pass
     * (CR 611.3a); another subject or a keyword grant is one-shot only.
     */
    static void requireStaticSelfBoost(BoostEffect boost) {
        if (!(boost.subject() instanceof PermanentSubject.Source) || !boost.keywords().isEmpty()
                || boost.amountsRead() != BoostEffect.AmountsRead.BOOSTED) {
            throw new IllegalStateException("A static BoostEffect boosts only its source and grants no keyword: "
                    + boost);
        }
    }
}
