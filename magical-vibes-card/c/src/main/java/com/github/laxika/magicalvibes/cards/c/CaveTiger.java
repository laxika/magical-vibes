package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "241")
public class CaveTiger extends Card {

    public CaveTiger() {
        // "Whenever this creature becomes blocked by a creature" triggers once per blocker
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, BoostEffect.self(1, 1));
    }
}
