package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "141")
public class CuratorOfSunsCreation extends Card {

    public CuratorOfSunsCreation() {
        addTrigger(Triggers.youDiscover(),
                new OncePerTurnTriggerEffect(new DiscoverEffect(new EventValue())));
    }
}
