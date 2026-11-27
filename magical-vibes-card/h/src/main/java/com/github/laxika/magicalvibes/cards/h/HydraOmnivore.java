package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsOpponent;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMD", collectorNumber = "161")
@CardRegistration(set = "DSC", collectorNumber = "185")
@CardRegistration(set = "C18", collectorNumber = "153")
public class HydraOmnivore extends Card {

    public HydraOmnivore() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), TriggerOptions.DEFAULT.withEventCondition(new AttackedTargetIsOpponent()),
                DealDamageEffect.to(Subjects.except(Subjects.eachOpponent(), Subjects.triggeringPlayer()),
                        new EventValue()));
    }
}
