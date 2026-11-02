package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GS1", collectorNumber = "26")
public class RecklessPangolin extends Card {

    public RecklessPangolin() {
        // Whenever this creature attacks, it gets +1/+1 until end of turn.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(1, 1));
    }
}
