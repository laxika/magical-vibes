package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "76")
@CardRegistration(set = "PIP", collectorNumber = "604")
public class GlowingOne extends Card {

    public GlowingOne() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new GiveTargetPlayerRadCountersEffect(4));
        addTrigger(Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY), new GainLifeEffect(1));
    }
}
