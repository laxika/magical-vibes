package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "194")
@CardRegistration(set = "C16", collectorNumber = "204")
public class HorizonChimera extends Card {

    public HorizonChimera() {
        addTrigger(Triggers.youDrawCard(), new GainLifeEffect(1));
    }
}
