package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;

@CardRegistration(set = "IKO", collectorNumber = "6")
public class CoordinatedCharge extends Card {

    public CoordinatedCharge() {
        addSpellEffect(BoostEffect.creaturesYouControl(2, 1));
        addCycling("{2}");
    }
}
