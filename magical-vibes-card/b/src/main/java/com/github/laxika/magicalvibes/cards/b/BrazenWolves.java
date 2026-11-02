package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "122")
public class BrazenWolves extends Card {

    public BrazenWolves() {
        // Whenever this creature attacks, it gets +2/+0 until end of turn.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(2, 0));
    }
}
