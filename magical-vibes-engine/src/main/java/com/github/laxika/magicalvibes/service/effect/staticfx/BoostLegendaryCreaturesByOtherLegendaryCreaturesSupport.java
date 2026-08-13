package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostLegendaryCreaturesByOtherLegendaryCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;

import java.util.List;

final class BoostLegendaryCreaturesByOtherLegendaryCreaturesSupport {

    private static final PermanentHasSupertypePredicate LEGENDARY =
            new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY);

    private BoostLegendaryCreaturesByOtherLegendaryCreaturesSupport() {
    }

    static void apply(StaticEffectContext context,
                      BoostLegendaryCreaturesByOtherLegendaryCreaturesEffect effect,
                      StaticBonusAccumulator accumulator,
                      StaticEffectSupport support) {
        if (!support.matchesCreatureScope(context, GrantScope.OWN_CREATURES, LEGENDARY)) {
            return;
        }

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(context.sourceControllerId());
        if (battlefield == null) {
            return;
        }

        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(context.gameData());
        int count = 0;
        for (Permanent other : battlefield) {
            if (other.getId().equals(context.target().getId())
                    || !support.isEffectivelyCreature(context.gameData(), other, hasAnimateArtifacts)
                    || !support.matchesStaticLeaf(other, LEGENDARY)) {
                continue;
            }
            count++;
        }

        accumulator.addPower(count * effect.powerPerCreature());
        accumulator.addToughness(count * effect.toughnessPerCreature());
    }
}
