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
public class BoostBySharedCreatureTypeSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostBySharedCreatureTypeEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostBySharedCreatureTypeEffect) effect;
        Permanent source = context.source();
        GameData gameData = context.gameData();
        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(gameData);
        if (!support.isEffectivelyCreature(gameData, source, hasAnimateArtifacts)
                || !support.matchesStaticFilter(context, source, boost.filter())) {
            return;
        }

        final int[] count = {0};
        gameData.forEachPermanent((playerId, other) -> {
            if (other.getId().equals(source.getId())) return;
            if (!support.isEffectivelyCreature(gameData, other, hasAnimateArtifacts)) return;
            if (!support.matchesStaticFilter(context, other, boost.filter())) return;
            if (support.sharesCreatureType(source, other)) count[0]++;
        });

        accumulator.addPower(count[0]);
        accumulator.addToughness(count[0]);
    }
}
