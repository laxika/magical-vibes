package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToCreaturesOfChosenParityEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Grants the effect's keyword(s) to every creature (any controller) whose mana value matches the
 * source permanent's chosen odd/even quality. Mirrors {@link BoostCreaturesOfChosenColorEffectHandler}
 * but keys off {@link Permanent#getChosenManaValueParity()} and is not restricted to the source's
 * battlefield. Used by Ashling's Prerogative.
 */
@Component
@RequiredArgsConstructor
public class GrantKeywordToCreaturesOfChosenParityEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordToCreaturesOfChosenParityEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantKeywordToCreaturesOfChosenParityEffect) effect;
        ManaValueParity chosen = context.source().getChosenManaValueParity();
        if (chosen == null) return;
        Permanent target = context.target();
        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(context.gameData());
        if (!support.isEffectivelyCreature(context.gameData(), target, hasAnimateArtifacts)) return;
        if (chosen.matches(target.getCard().getManaValue())) {
            accumulator.addKeywords(grant.keywords());
        }
    }
}
