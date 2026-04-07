package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "ISD", collectorNumber = "11")
public class DoomedTraveler extends Card {

    public DoomedTraveler() {
        // When Doomed Traveler dies, create a 1/1 white Spirit creature token with flying.
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.whiteSpirit(1));
    }
}
