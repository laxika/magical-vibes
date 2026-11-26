package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "186")
public class DeeprootWarrior extends Card {

    public DeeprootWarrior() {
        addTrigger(Triggers.becomesBlocked(), BoostEffect.self(1, 1));
    }
}
