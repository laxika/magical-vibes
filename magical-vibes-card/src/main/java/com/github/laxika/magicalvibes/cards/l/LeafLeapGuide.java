package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceAndEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "YBLB", collectorNumber = "26")
public class LeafLeapGuide extends Card {

    public LeafLeapGuide() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new PerpetuallyBoostSourceAndEnteringCreatureEffect(1, 1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(ReturnToHandEffect.target()),
                "{1}, {T}: Return another target creature you control to its owner's hand.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                        )),
                        "Target must be another creature you control")
        ));
    }
}
