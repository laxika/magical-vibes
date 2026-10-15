package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentsThenEachControllerMayCastEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "737")
@CardRegistration(set = "CMM", collectorNumber = "767")
public class GuffRewritesHistory extends Card {

    public GuffRewritesHistory() {
        PermanentPredicate nonenchantmentNonland = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsEnchantmentPredicate()),
                new PermanentNotPredicate(new PermanentIsLandPredicate())));

        setMultiTargetConstraint(MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE);
        target(new PermanentPredicateTargetFilter(
                nonenchantmentNonland,
                "Target must be a nonenchantment, nonland permanent"), 0, 99)
                .addEffect(EffectSlot.SPELL,
                        new ShuffleTargetPermanentsThenEachControllerMayCastEffect(nonenchantmentNonland));
    }
}
