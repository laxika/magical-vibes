package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "174")
public class BurlfistOak extends Card {

    public BurlfistOak() {
        // Whenever you draw a card, this creature gets +2/+2 until end of turn.
        addTrigger(Triggers.youDrawCard(), new BoostSelfEffect(2, 2));
    }
}
