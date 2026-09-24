package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SLD", collectorNumber = "1418")
@CardRegistration(set = "SLD", collectorNumber = "1766")
public class MazirekKraulDeathPriest extends Card {

    public MazirekKraulDeathPriest() {
        // Whenever a player sacrifices another permanent, put a +1/+1 counter on each creature you control.
        addEffect(EffectSlot.ON_ANY_PERMANENT_SACRIFICED,
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
    }
}
