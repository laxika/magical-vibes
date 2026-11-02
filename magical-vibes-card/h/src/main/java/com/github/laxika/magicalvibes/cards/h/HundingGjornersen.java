package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "231")
@CardRegistration(set = "ME3", collectorNumber = "152")
public class HundingGjornersen extends Card {

    public HundingGjornersen() {
        addTrigger(Triggers.becomesBlocked(), new RampageEffect(1));
    }
}
