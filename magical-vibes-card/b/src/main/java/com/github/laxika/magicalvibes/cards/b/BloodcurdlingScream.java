package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "P02", collectorNumber = "63")
public class BloodcurdlingScream extends Card {

    public BloodcurdlingScream() {
        // Target creature gets +X/+0 until end of turn.
        addSpellEffect(BoostEffect.target(new XValue(), new Fixed(0)));
    }
}
