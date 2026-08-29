package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDestroyAllPermanentsEffect;

@CardRegistration(set = "JOU", collectorNumber = "89")
public class BearerOfTheHeavens extends Card {

    public BearerOfTheHeavens() {
        // When this creature dies, destroy all permanents at the beginning of the next end step.
        addEffect(EffectSlot.ON_DEATH, new RegisterDelayedDestroyAllPermanentsEffect());
    }
}
