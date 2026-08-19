package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "RIX", collectorNumber = "145")
public class StrengthOfThePack extends Card {

    public StrengthOfThePack() {
        addEffect(EffectSlot.SPELL, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 2, new PermanentIsCreaturePredicate()));
    }
}
