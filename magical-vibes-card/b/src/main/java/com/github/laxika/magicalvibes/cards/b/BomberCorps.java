package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "86")
@CardRegistration(set = "GK1", collectorNumber = "80")
@CardRegistration(set = "RVR", collectorNumber = "102")
public class BomberCorps extends Card {

    public BomberCorps() {
        // Battalion — Whenever this creature and at least two other creatures attack,
        // this creature deals 1 damage to any target.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(3)),
                new DealDamageToAnyTargetEffect(1, false));
    }
}
