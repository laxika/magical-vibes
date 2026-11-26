package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "P02", collectorNumber = "140")
public class NorwoodWarrior extends Card {

    public NorwoodWarrior() {
        // Whenever this creature becomes blocked, it gets +1/+1 until end of turn.
        addTrigger(Triggers.becomesBlocked(), BoostEffect.self(1, 1));
    }
}
