package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "242")
public class DeepwoodWolverine extends Card {

    public DeepwoodWolverine() {
        addTrigger(Triggers.becomesBlocked(), new BoostSelfEffect(2, 0));
    }
}
