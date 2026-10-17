package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostByOtherCreaturesWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Scoped form of {@link BoostByOtherCreaturesWithSameNameEffect}: a continuous effect of its source that
 * boosts each creature in scope by the number of other creatures with that creature's name. The
 * self-only form is handled by {@link BoostByOtherCreaturesWithSameNameSelfEffectHandler}.
 */
@Component
@RequiredArgsConstructor
public class BoostByOtherCreaturesWithSameNameScopedEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostByOtherCreaturesWithSameNameEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostByOtherCreaturesWithSameNameEffect) effect;
        if (boost.scope() == null || !support.matchesCreatureScope(context, boost.scope(), boost.filter())) {
            return;
        }
        Permanent boosted = context.target();
        String name = BoostByOtherCreaturesWithSameNameSelfEffectHandler.sameNameCheckName(boosted);
        if (name == null) {
            return;
        }
        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(context.gameData());

        final int[] count = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.getId().equals(boosted.getId())) return;
            if (boost.onlyControlled() && !playerId.equals(context.sourceControllerId())) return;
            if (!support.isEffectivelyCreature(permanent, hasAnimateArtifacts)) return;
            if (!name.equals(BoostByOtherCreaturesWithSameNameSelfEffectHandler.sameNameCheckName(permanent))) return;
            count[0]++;
        });

        accumulator.addPower(count[0] * boost.powerPerCreature());
        accumulator.addToughness(count[0] * boost.toughnessPerCreature());
    }
}
