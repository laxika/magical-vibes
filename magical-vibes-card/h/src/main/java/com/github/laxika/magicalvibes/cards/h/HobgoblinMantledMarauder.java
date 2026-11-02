package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "80")
@CardRegistration(set = "OM1", collectorNumber = "77")
@CardRegistration(set = "MSC", collectorNumber = "805")
public class HobgoblinMantledMarauder extends Card {

    public HobgoblinMantledMarauder() {
        addTrigger(Triggers.youDiscardCard(), new BoostSelfEffect(2, 0));
    }
}
