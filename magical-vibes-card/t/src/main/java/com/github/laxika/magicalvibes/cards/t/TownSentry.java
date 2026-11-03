package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "P02", collectorNumber = "25")
public class TownSentry extends Card {

    public TownSentry() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BoostSelfEffect(0, 2));
    }
}
