package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "119")
public class RabidWolverines extends Card {

    public RabidWolverines() {
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, BoostEffect.self(1, 1));
    }
}
