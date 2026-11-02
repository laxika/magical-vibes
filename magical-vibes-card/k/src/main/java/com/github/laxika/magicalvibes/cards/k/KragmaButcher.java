package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "100")
public class KragmaButcher extends Card {

    public KragmaButcher() {
        addTrigger(Triggers.becomesUntapped(), new BoostSelfEffect(2, 0));
    }
}
