package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "BNG", collectorNumber = "128")
public class NessianDemolok extends Card {

    public NessianDemolok() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                "Target must be a noncreature permanent"))
                .addEffect(EffectSlot.STATIC, new TributeEffect(3))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new TributeNotPaidEffect(new DestroyTargetPermanentEffect()));
    }
}
