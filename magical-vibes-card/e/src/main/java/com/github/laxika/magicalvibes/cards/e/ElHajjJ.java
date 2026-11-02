package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "134")
@CardRegistration(set = "SUM", collectorNumber = "108")
@CardRegistration(set = "3ED", collectorNumber = "108")
@CardRegistration(set = "ARN", collectorNumber = "24")
public class ElHajjJ extends Card {

    public ElHajjJ() {
        // Whenever this creature deals damage, you gain that much life.
        addTrigger(Triggers.dealsDamage(), new GainLifeEffect(new EventValue()));
    }
}
