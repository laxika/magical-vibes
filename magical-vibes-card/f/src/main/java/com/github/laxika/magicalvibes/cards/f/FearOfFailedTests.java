package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "55")
public class FearOfFailedTests extends Card {

    public FearOfFailedTests() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(new EventValue()));
    }
}
