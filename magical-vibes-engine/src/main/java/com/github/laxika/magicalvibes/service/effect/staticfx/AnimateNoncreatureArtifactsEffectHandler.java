package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnimateNoncreatureArtifactsEffectHandler implements StaticEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateNoncreatureArtifactsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var pass = LayerSystemService.activePass();
        boolean animated = pass != null && pass.board() != null
                ? pass.board().marchAnimatedIds().contains(context.target().getId())
                : gameQueryService.isArtifact(context.target())
                        && !context.target().getCard().hasType(CardType.CREATURE);
        if (animated) {
            accumulator.setAnimatedCreature(true);
            if (((AnimateNoncreatureArtifactsEffect) effect).losesAllAbilities()) {
                accumulator.setLosesAllAbilities(true);
            }
        }
    }
}
