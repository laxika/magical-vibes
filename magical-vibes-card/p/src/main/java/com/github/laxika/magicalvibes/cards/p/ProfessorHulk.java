package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "37")
@CardRegistration(set = "MSC", collectorNumber = "338")
public class ProfessorHulk extends Card {

    public ProfessorHulk() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(new EventValue()));
    }
}
