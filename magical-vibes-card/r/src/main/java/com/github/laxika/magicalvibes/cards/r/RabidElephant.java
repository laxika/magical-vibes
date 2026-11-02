package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "263")
public class RabidElephant extends Card {

    public RabidElephant() {
        // Whenever Rabid Elephant becomes blocked, it gets +2/+2 until end of turn
        // for each creature blocking it.
        addTrigger(Triggers.becomesBlocked(), new BoostSelfEffect(
                new Scaled(new CreaturesBlockingSource(), 2),
                new Scaled(new CreaturesBlockingSource(), 2)));
    }
}
