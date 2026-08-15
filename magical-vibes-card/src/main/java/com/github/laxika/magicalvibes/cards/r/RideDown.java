package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTrampleToCreaturesBlockedByTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "194")
@CardRegistration(set = "EMN", collectorNumber = "188")
public class RideDown extends Card {

    public RideDown() {
        target(new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsBlockingPredicate())),
                        "Target must be a blocking creature"))
                .addEffect(EffectSlot.SPELL, new GrantTrampleToCreaturesBlockedByTargetEffect())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
