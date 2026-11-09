package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "8ED", collectorNumber = "31")
@CardRegistration(set = "MMQ", collectorNumber = "32")
public class NoblePurpose extends Card {

    public NoblePurpose() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamage(), new GainLifeEffect(new EventValue()));
    }
}
