package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "OGW", collectorNumber = "8")
public class SpatialContortion extends Card {

    public SpatialContortion() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, -3));
    }
}
