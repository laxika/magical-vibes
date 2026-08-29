package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "M21", collectorNumber = "174")
public class BurlfistOak extends Card {

    public BurlfistOak() {
        // Whenever you draw a card, this creature gets +2/+2 until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new BoostSelfEffect(2, 2));
    }
}
