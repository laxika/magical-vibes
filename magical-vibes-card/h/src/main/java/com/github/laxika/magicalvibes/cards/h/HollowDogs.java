package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "9ED", collectorNumber = "139")
@CardRegistration(set = "7ED", collectorNumber = "141")
@CardRegistration(set = "USG", collectorNumber = "137")
@CardRegistration(set = "S99", collectorNumber = "81")
@CardRegistration(set = "BTD", collectorNumber = "28")
public class HollowDogs extends Card {

    public HollowDogs() {
        // Whenever this creature attacks, it gets +2/+0 until end of turn.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(2, 0));
    }
}
