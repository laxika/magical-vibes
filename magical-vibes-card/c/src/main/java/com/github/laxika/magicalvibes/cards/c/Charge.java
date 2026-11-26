package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "10")
public class Charge extends Card {

    public Charge() {
        addSpellEffect(BoostEffect.creaturesYouControl(1, 1));
    }
}
