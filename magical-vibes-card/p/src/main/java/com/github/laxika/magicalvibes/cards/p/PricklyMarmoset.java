package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "129")
public class PricklyMarmoset extends Card {

    public PricklyMarmoset() {
        addTrigger(Triggers.youCycleCard(),
                new BoostSelfEffect(2, 0));
    }
}
