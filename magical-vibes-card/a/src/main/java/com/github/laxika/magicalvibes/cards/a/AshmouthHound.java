package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "128")
@CardRegistration(set = "DDK", collectorNumber = "45")
public class AshmouthHound extends Card {

    public AshmouthHound() {
        addTrigger(Triggers.blocks(), new DealDamageToTargetCreatureEffect(1));
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, new DealDamageToTargetCreatureEffect(1));
    }
}
