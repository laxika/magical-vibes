package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "8ED", collectorNumber = "218")
@CardRegistration(set = "PCY", collectorNumber = "103")
public class SearingWind extends Card {

    public SearingWind() {
        addSpellEffect(new DealDamageToAnyTargetEffect(10));
    }
}
