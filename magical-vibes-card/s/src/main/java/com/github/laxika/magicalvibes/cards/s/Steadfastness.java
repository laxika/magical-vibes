package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "31")
@CardRegistration(set = "S99", collectorNumber = "27")
public class Steadfastness extends Card {

    public Steadfastness() {
        addSpellEffect(BoostEffect.creaturesYouControl(0, 3));
    }
}
