package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "196")
public class JukaiTrainee extends Card {

    public JukaiTrainee() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, BoostEffect.self(1, 1));
        addTrigger(Triggers.becomesBlocked(), BoostEffect.self(1, 1));
    }
}
