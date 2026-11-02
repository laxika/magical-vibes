package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "288")
public class GraspingGiant extends Card {

    public GraspingGiant() {
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER,
                new ExileTargetPermanentUntilSourceLeavesEffect());
    }
}
