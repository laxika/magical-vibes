package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "OGW", collectorNumber = "8")
public class SpatialContortion extends Card {

    public SpatialContortion() {
        addSpellEffect(BoostEffect.target(3, -3));
    }
}
