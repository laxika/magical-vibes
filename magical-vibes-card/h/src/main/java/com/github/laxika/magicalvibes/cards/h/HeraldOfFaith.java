package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "13")
@CardRegistration(set = "FDN", collectorNumber = "494")
public class HeraldOfFaith extends Card {

    public HeraldOfFaith() {
        // Whenever this creature attacks, you gain 2 life.
        addTrigger(Triggers.attacks(), new GainLifeEffect(2));
    }
}
