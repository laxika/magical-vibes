package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "TLE", collectorNumber = "247")
@CardRegistration(set = "TLE", collectorNumber = "288")
public class ZukosOffense extends Card {

    public ZukosOffense() {
        addSpellEffect(DealDamageEffect.toAnyTarget(2));
    }
}
