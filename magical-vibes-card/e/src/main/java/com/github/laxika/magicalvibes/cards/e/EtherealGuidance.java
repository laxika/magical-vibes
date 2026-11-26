package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;

@CardRegistration(set = "SOI", collectorNumber = "18")
public class EtherealGuidance extends Card {

    public EtherealGuidance() {
        addSpellEffect(BoostEffect.creaturesYouControl(2, 1));
    }
}
