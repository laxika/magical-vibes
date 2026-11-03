package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "31")
public class PrideGuardian extends Card {

    public PrideGuardian() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new GainLifeEffect(3));
    }
}
