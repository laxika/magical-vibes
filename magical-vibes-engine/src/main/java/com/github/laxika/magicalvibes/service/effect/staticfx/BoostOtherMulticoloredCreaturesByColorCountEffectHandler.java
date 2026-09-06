package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostOtherMulticoloredCreaturesByColorCountEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class BoostOtherMulticoloredCreaturesByColorCountEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostOtherMulticoloredCreaturesByColorCountEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostOtherMulticoloredCreaturesByColorCountEffect) effect;
        // "each other ... creature you control": your battlefield, excluding the source itself.
        if (!context.targetOnSameBattlefield()) {
            return;
        }
        Permanent target = context.target();
        if (target.getId().equals(context.source().getId())) {
            return;
        }
        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(context.gameData());
        if (!support.isEffectivelyCreature(context.gameData(), target, hasAnimateArtifacts)) {
            return;
        }
        int colorCount = support.effectiveColorCount(target);
        // "multicolored" = two or more colors; monocolored and colorless creatures are unaffected.
        if (colorCount < 2) {
            return;
        }
        accumulator.addPower(boost.powerPerColor() * colorCount);
        accumulator.addToughness(boost.toughnessPerColor() * colorCount);
    }

}
