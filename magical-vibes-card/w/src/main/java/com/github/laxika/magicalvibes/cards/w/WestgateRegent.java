package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "126")
public class WestgateRegent extends Card {

    public WestgateRegent() {
        // Ward — Discard a card.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessDiscardsEffect());

        // Whenever this creature deals combat damage to a player, put that many +1/+1 counters on it.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
    }
}
