package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostBySharedCreatureTypeEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostBySharedCreatureTypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostBySharedCreatureTypeEffect) effect;
        Permanent target = context.target();
        GameData gameData = context.gameData();
        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(gameData);
        if (!support.isEffectivelyCreature(gameData, target, hasAnimateArtifacts)
                || !support.matchesStaticFilter(context, target, boost.filter())) {
            return;
        }

        final int[] count = {0};

        gameData.forEachPermanent((playerId, other) -> {
            if (other.getId().equals(target.getId())) return;
            if (!support.isEffectivelyCreature(gameData, other, hasAnimateArtifacts)) return;
            if (!support.matchesStaticFilter(context, other, boost.filter())) return;

            if (support.sharesCreatureType(target, other)) count[0]++;
        });

        accumulator.addPower(count[0]);
        accumulator.addToughness(count[0]);
    }
}
