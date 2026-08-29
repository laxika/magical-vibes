package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "71")
public class TekuthalInquiryDominus extends Card {

    public TekuthalInquiryDominus() {
        addEffect(EffectSlot.STATIC, new DoubleProliferateEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U/P}{U/P}",
                List.of(
                        new RemoveCounterFromControlledPermanentCost(
                                3,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate())),
                                true),
                        new PutCountersOnSelfEffect(CounterType.INDESTRUCTIBLE)
                ),
                "{1}{U/P}{U/P}, Remove three counters from among other artifacts, creatures, and planeswalkers you control: Put an indestructible counter on Tekuthal."
        ));
    }
}
