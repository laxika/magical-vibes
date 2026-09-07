package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "77")
public class PrimitiveJustice extends Card {

    public PrimitiveJustice() {
        // "You may pay {1}{R} and/or {1}{G} any number of times" — each payment buys one more
        // target artifact, so the announced X is the total number of targets (1 + repetitions)
        // and the target group scales with it.
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{1}{R}", "{1}{G}")));

        targetExactlyX(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Targets must be artifacts"
        ), 100).addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());

        // "and you gain 1 life" rides only on the green payments.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new RepeatedAdditionalCostCount("{1}{G}")));
    }
}
