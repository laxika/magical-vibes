package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;

@CardRegistration(set = "PCY", collectorNumber = "133")
public class VitalizingWind extends Card {

    public VitalizingWind() {
        addSpellEffect(BoostEffect.creaturesYouControl(7, 7));
    }
}
