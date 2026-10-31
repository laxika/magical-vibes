package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "OGW", collectorNumber = "8")
public class SpatialContortion extends Card {

    public SpatialContortion() {
        addSpellEffect(new BoostTargetCreatureEffect(3, -3));
    }
}
