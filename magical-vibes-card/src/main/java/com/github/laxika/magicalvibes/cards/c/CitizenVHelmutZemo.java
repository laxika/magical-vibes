package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "650")
public class CitizenVHelmutZemo extends Card {

    public CitizenVHelmutZemo() {
        // Whenever you gain life, put a +1/+1 counter on each Villain you control.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                1,
                new PermanentHasSubtypePredicate(CardSubtype.VILLAIN)));
    }
}
