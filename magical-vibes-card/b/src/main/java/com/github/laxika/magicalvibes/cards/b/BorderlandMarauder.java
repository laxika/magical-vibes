package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "131")
@CardRegistration(set = "W16", collectorNumber = "11")
@CardRegistration(set = "IMA", collectorNumber = "119")
@CardRegistration(set = "EMA", collectorNumber = "120")
public class BorderlandMarauder extends Card {

    public BorderlandMarauder() {
        // Whenever this creature attacks, it gets +2/+0 until end of turn.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(2, 0));
    }
}
