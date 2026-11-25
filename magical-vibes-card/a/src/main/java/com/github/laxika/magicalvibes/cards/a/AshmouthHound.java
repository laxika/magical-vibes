package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "128")
@CardRegistration(set = "DDK", collectorNumber = "45")
public class AshmouthHound extends Card {

    public AshmouthHound() {
        addTrigger(Triggers.blocks(), DealDamageEffect.toTargetCreature(1));
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, DealDamageEffect.toTargetCreature(1));
    }
}
