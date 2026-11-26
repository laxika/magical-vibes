package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "171")
public class BrambleCreeper extends Card {

    public BrambleCreeper() {
        addTrigger(Triggers.attacks(), BoostEffect.self(5, 0));
    }
}
