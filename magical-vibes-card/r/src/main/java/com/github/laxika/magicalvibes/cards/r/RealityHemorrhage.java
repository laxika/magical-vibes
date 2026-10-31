package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "OGW", collectorNumber = "100")
public class RealityHemorrhage extends Card {

    public RealityHemorrhage() {
        addSpellEffect(new DealDamageToAnyTargetEffect(2));
    }
}
