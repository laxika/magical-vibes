package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "186")
public class DeeprootWarrior extends Card {

    public DeeprootWarrior() {
        addTrigger(Triggers.becomesBlocked(), new BoostSelfEffect(1, 1));
    }
}
