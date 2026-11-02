package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "149")
public class SkewerSlinger extends Card {

    public SkewerSlinger() {
        addTrigger(Triggers.blocks(), new DealDamageToTargetCreatureEffect(1));
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, new DealDamageToTargetCreatureEffect(1));
    }
}
