package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "241")
public class CaveTiger extends Card {

    public CaveTiger() {
        // "Whenever this creature becomes blocked by a creature" triggers once per blocker
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, new BoostSelfEffect(1, 1));
    }
}
