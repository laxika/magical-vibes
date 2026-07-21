package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "INR", collectorNumber = "17")
public class CatharsCrusade extends Card {

    public CatharsCrusade() {
        // Whenever a creature you control enters, put a +1/+1 counter on each creature you control.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
    }
}
