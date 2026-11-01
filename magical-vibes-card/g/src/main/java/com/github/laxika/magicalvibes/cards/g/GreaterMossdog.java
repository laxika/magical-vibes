package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DredgeEffect;

@CardRegistration(set = "RAV", collectorNumber = "169")
@CardRegistration(set = "DDJ", collectorNumber = "59")
@CardRegistration(set = "MMA", collectorNumber = "146")
@CardRegistration(set = "RVR", collectorNumber = "145")
public class GreaterMossdog extends Card {

    public GreaterMossdog() {
        addStaticEffect(new DredgeEffect(3));
    }
}
