package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "DSK", collectorNumber = "213")
public class DragToTheRoots extends Card {

    public DragToTheRoots() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(), new ReduceOwnCastCostEffect(new Fixed(2))));

        PermanentNotPredicate nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        target(new PermanentPredicateTargetFilter(nonland,
                "Target must be a nonland permanent"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
