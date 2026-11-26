package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "46")
@CardRegistration(set = "UDS", collectorNumber = "20")
public class Solidarity extends Card {

    public Solidarity() {
        addSpellEffect(BoostEffect.creaturesYouControl(0, 5));
    }
}
