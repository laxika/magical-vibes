package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "USG", collectorNumber = "144")
@CardRegistration(set = "DDE", collectorNumber = "11")
public class OrderOfYawgmoth extends Card {

    public OrderOfYawgmoth() {
        addTrigger(Triggers.dealsDamageToPlayer(),
                new DiscardEffect(1, Subjects.targetPlayer(), false));
    }
}
