package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "149")
public class SkewerSlinger extends Card {

    public SkewerSlinger() {
        addTrigger(Triggers.blocks(), DealDamageEffect.toTargetCreature(1));
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, DealDamageEffect.toTargetCreature(1));
    }
}
