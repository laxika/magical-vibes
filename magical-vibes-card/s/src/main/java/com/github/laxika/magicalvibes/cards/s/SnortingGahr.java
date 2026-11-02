package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "272")
public class SnortingGahr extends Card {

    public SnortingGahr() {
        addTrigger(Triggers.becomesBlocked(), new BoostSelfEffect(2, 2));
    }
}
