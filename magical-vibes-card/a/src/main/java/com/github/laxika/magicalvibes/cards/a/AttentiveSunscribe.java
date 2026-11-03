package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "4")
public class AttentiveSunscribe extends Card {

    public AttentiveSunscribe() {
        addTrigger(Triggers.becomesTapped(), new ScryEffect(1));
    }
}
