package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureGainLifeAndDrawEqualToPowerEffect;


@CardRegistration(set = "M13", collectorNumber = "88")
public class DiscipleOfBolas extends Card {

    public DiscipleOfBolas() {
        // When this creature enters, sacrifice another creature. You gain X life and draw X cards,
        // where X is that creature's power.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificeAnotherCreatureGainLifeAndDrawEqualToPowerEffect());
    }
}
