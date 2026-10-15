package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "UDS", collectorNumber = "99")
public class WakeOfDestruction extends Card {

    public WakeOfDestruction() {
        PermanentIsLandPredicate landPredicate = new PermanentIsLandPredicate();
        target(new PermanentPredicateTargetFilter(landPredicate, "Target must be a land"))
                .addEffect(EffectSlot.SPELL,
                        new DestroyTargetPermanentAndAllWithSameNameEffect(landPredicate));
    }
}
