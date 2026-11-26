package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DKA", collectorNumber = "2")
public class BarTheDoor extends Card {

    public BarTheDoor() {
        addSpellEffect(BoostEffect.creaturesYouControl(0, 4));
    }
}
