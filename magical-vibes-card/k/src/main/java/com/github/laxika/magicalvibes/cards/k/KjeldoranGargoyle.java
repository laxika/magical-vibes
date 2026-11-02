package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "10")
@CardRegistration(set = "DMR", collectorNumber = "11")
public class KjeldoranGargoyle extends Card {

    public KjeldoranGargoyle() {
        // Whenever this creature deals damage, you gain that much life.
        addTrigger(Triggers.dealsDamage(), new GainLifeEffect(new EventValue()));
    }
}
