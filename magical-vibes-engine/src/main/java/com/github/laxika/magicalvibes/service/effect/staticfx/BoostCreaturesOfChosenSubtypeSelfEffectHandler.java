package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Self-only companion to {@link BoostCreaturesOfChosenSubtypeEffectHandler}: the source permanent
 * is never a target of the "others" handler, so this applies the same +P/+T boost to the source
 * itself when the source is a creature of the chosen subtype (Brass Herald choosing Golem).
 */
@Component
@RequiredArgsConstructor
public class BoostCreaturesOfChosenSubtypeSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostCreaturesOfChosenSubtypeEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturesOfChosenSubtypeEffect) effect;
        CardSubtype chosenSubtype = context.source().getChosenSubtype();
        if (chosenSubtype == null) return;
        Permanent source = context.source();
        // Recursion-safe creature check: the fully layered GameQueryService.isCreature would
        // re-enter static bonus assembly from inside this static pass.
        if (!support.matchesStaticFilter(source, new PermanentIsCreaturePredicate())) return;
        if (support.matchesStaticFilter(source, new PermanentHasSubtypePredicate(chosenSubtype))) {
            int multiplier = boost.scalingCounter() == null
                    ? 1
                    : source.getCounterCount(boost.scalingCounter());
            accumulator.addPower(boost.powerBoost() * multiplier);
            accumulator.addToughness(boost.toughnessBoost() * multiplier);
        }
    }
}
