package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Applies {@link AnimateNoncreatureArtifactsEffect} for the static-bonus assembly. The layered pass makes the type
 * change (layer 4), the ability loss (layer 6) and the mana-value base P/T (sublayer 7b) and keeps which permanents
 * this effect instance animated; this handler marks those as animated and, for Titania's Song, as having lost their
 * abilities. Without a layered board (a permanent with no layered state) it judges the subject and filter on the
 * permanent as it is.
 */
@Component
@RequiredArgsConstructor
public class AnimateNoncreatureArtifactsEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateNoncreatureArtifactsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        AnimateNoncreatureArtifactsEffect animation = (AnimateNoncreatureArtifactsEffect) effect;
        var pass = LayerSystemService.activePass();
        boolean animated = pass != null && pass.board() != null
                ? pass.board().animatedBy(effect).contains(context.target().getId())
                : support.names(context, animation.subject(), animation.filter());
        if (animated) {
            accumulator.setAnimatedCreature(true);
            if (animation.losesAllAbilities()) {
                accumulator.setLosesAllAbilities(true);
            }
        }
    }
}
