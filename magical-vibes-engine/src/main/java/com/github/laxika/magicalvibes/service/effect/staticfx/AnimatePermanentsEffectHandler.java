package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("staticAnimatePermanentsEffectHandler")
@RequiredArgsConstructor
public class AnimatePermanentsEffectHandler implements StaticEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimatePermanentsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var animate = (AnimatePermanentsEffect) effect;
        if (animate.scope() != GrantScope.ALL_PERMANENTS
                || !support.matchesStaticFilter(context, context.target(), animate.filter())) {
            return;
        }

        AmountContext amountContext = AmountContext.forStaticEffect(
                context.source(), context.sourceControllerId());
        int power = evaluate(animate.power(), context.target().getCard().getPower(), context, amountContext);
        int toughness = evaluate(animate.toughness(), context.target().getCard().getToughness(), context,
                amountContext);
        accumulator.setAnimatedCreature(true);
        accumulator.setBasePTOverride(power, toughness);
        accumulator.addGrantedCardType(CardType.CREATURE);
        animate.grantedCardTypes().forEach(accumulator::addGrantedCardType);
        for (CardSubtype subtype : animate.grantedSubtypes()) {
            accumulator.addGrantedSubtype(subtype);
        }
        accumulator.addKeywords(animate.grantedKeywords());
        if (!animate.animatedColors().isEmpty()) {
            animate.animatedColors().forEach(accumulator::addGrantedColor);
            accumulator.setColorOverriding(true);
        } else if (animate.animatedColor() != null) {
            accumulator.addGrantedColor(animate.animatedColor());
            accumulator.setColorOverriding(true);
        }
    }

    private int evaluate(DynamicAmount amount, Integer fallback, StaticEffectContext context,
                         AmountContext amountContext) {
        if (amount == null) {
            return fallback == null ? 0 : fallback;
        }
        return amountEvaluationService.evaluate(context.gameData(), amount, amountContext);
    }
}
