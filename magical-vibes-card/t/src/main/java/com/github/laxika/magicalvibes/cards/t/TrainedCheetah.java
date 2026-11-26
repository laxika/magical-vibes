package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "154")
public class TrainedCheetah extends Card {

    public TrainedCheetah() {
        // Whenever this creature becomes blocked, it gets +1/+1 until end of turn.
        addTrigger(Triggers.becomesBlocked(), BoostEffect.self(1, 1));
    }
}
