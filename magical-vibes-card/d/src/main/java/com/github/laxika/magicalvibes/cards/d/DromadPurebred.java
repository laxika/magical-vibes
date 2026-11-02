package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "15")
public class DromadPurebred extends Card {

    public DromadPurebred() {
        addTrigger(Triggers.dealtDamage(), new GainLifeEffect(1));
    }
}
