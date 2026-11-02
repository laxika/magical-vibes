package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "191")
@CardRegistration(set = "FIN", collectorNumber = "343")
public class JumboCactuar extends Card {

    public JumboCactuar() {
        // Whenever this creature attacks, it gets +9999/+0 until end of turn.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(9999, 0));
    }
}
