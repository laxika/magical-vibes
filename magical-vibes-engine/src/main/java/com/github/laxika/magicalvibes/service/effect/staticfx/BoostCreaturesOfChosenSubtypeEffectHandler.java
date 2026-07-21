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

@Component
@RequiredArgsConstructor
public class BoostCreaturesOfChosenSubtypeEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostCreaturesOfChosenSubtypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturesOfChosenSubtypeEffect) effect;
        CardSubtype chosenSubtype = context.source().getChosenSubtype();
        if (chosenSubtype == null) return;
        if (!boost.allControllers() && !context.targetOnSameBattlefield()) return;
        Permanent target = context.target();
        // Recursion-safe creature check: the fully layered GameQueryService.isCreature would
        // re-enter static bonus assembly from inside this static pass.
        if (!support.matchesStaticFilter(target, new PermanentIsCreaturePredicate())) return;
        if (support.matchesStaticFilter(target, new PermanentHasSubtypePredicate(chosenSubtype))) {
            int multiplier = boost.scalingCounter() == null
                    ? 1
                    : context.source().getCounterCount(boost.scalingCounter());
            accumulator.addPower(boost.powerBoost() * multiplier);
            accumulator.addToughness(boost.toughnessBoost() * multiplier);
        }
    }
}
