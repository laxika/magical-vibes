package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "24")
public class WallOfHope extends Card {

    public WallOfHope() {
        addTrigger(Triggers.dealtDamage(), new GainLifeEffect(new EventValue()));
    }
}
