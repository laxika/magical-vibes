package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "UDS", collectorNumber = "6")
public class FalseProphet extends Card {

    public FalseProphet() {
        // When this creature dies, exile all creatures.
        addEffect(EffectSlot.ON_DEATH, new ExileAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
