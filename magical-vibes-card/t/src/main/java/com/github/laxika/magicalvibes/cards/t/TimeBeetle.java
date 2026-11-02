package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "58")
@CardRegistration(set = "WHO", collectorNumber = "663")
public class TimeBeetle extends Card {

    public TimeBeetle() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new TimeTravelEffect(1));
    }
}
