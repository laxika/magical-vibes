package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "148")
@CardRegistration(set = "ME3", collectorNumber = "101")
public class FrostGiant extends Card {

    public FrostGiant() {
        addTrigger(Triggers.becomesBlocked(), new RampageEffect(2));
    }
}
