package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsOpponent;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "504")
public class BlackWidowIntelExpert extends Card {

    public BlackWidowIntelExpert() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), TriggerOptions.DEFAULT.withEventCondition(new AttackedTargetIsOpponent()),
                new DrawCardEffect(2, Subjects.allOf(Subjects.you(), Subjects.triggeringPlayer())));
    }
}
