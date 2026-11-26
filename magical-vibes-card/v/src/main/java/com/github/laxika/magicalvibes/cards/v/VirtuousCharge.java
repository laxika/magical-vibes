package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "29")
public class VirtuousCharge extends Card {

    public VirtuousCharge() {
        addSpellEffect(BoostEffect.creaturesYouControl(1, 1));
    }
}
