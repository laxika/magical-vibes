package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "242")
public class DeepwoodWolverine extends Card {

    public DeepwoodWolverine() {
        addTrigger(Triggers.becomesBlocked(), BoostEffect.self(2, 0));
    }
}
