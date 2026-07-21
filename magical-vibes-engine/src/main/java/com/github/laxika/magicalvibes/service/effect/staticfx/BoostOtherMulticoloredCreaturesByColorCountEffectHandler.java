package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostOtherMulticoloredCreaturesByColorCountEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

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
        int colorCount = effectiveColorCount(target);
        // "multicolored" = two or more colors; monocolored and colorless creatures are unaffected.
        if (colorCount < 2) {
            return;
        }
        accumulator.addPower(boost.powerPerColor() * colorCount);
        accumulator.addToughness(boost.toughnessPerColor() * colorCount);
    }

    /**
     * The number of colors the target currently has. Layer-5-aware while a CR 613 pass is active:
     * this 7c boost reads the colors decided in layer 5, so color-changing effects are respected.
     */
    private int effectiveColorCount(Permanent target) {
        CharacteristicState layered = LayerSystemService.activeStateFor(target.getId());
        if (layered != null) {
            return layered.getColors().size();
        }
        Set<CardColor> colors = new HashSet<>();
        if (target.isColorOverridden()) {
            colors.addAll(target.getTransientColors());
        } else {
            colors.addAll(target.getEffectiveColors());
            colors.addAll(target.getTransientColors());
        }
        return colors.size();
    }
}
