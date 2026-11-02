package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "166")
@CardRegistration(set = "ONC", collectorNumber = "133")
public class IchorclawMyr extends Card {

    public IchorclawMyr() {
        addTrigger(Triggers.becomesBlocked(), new BoostSelfEffect(2, 2));
    }
}
