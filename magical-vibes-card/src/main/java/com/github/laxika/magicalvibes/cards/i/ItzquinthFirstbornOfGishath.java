package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "230")
@CardRegistration(set = "LCI", collectorNumber = "331")
public class ItzquinthFirstbornOfGishath extends Card {

    public ItzquinthFirstbornOfGishath() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)
                )),
                "First target must be a Dinosaur creature you control"
        ));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Second target must be another creature"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayPayManaEffect(
                "{2}",
                new TargetDealsPowerDamageToTargetEffect(),
                "Pay {2} to have the Dinosaur deal damage?"
        ));
    }
}
