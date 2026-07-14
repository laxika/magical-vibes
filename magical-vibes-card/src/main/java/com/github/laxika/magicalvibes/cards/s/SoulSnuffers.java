package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "EVE", collectorNumber = "45")
public class SoulSnuffers extends Card {

    public SoulSnuffers() {
        // When this creature enters, put a -1/-1 counter on each creature.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCounterOnEachMatchingPermanentEffect(
                CounterType.MINUS_ONE_MINUS_ONE, 1,
                new PermanentIsCreaturePredicate(), EachPermanentScope.ALL_PLAYERS));
    }
}
