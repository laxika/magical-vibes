package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "WTH", collectorNumber = "77")
public class OdylicWraith extends Card {

    public OdylicWraith() {
        addTrigger(Triggers.dealsDamageToPlayer(), new DiscardEffect(1, Subjects.targetPlayer(), false));
    }
}
