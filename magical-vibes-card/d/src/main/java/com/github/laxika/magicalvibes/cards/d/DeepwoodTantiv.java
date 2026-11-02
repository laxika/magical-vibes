package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "241")
public class DeepwoodTantiv extends Card {

    public DeepwoodTantiv() {
        addTrigger(Triggers.becomesBlocked(), new GainLifeEffect(2));
    }
}
