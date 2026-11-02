package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "273")
public class GracefulCat extends Card {

    public GracefulCat() {
        // Whenever this creature attacks, it gets +1/+1 until end of turn.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(1, 1));
    }
}
