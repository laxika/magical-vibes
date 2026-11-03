package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "17")
public class IlluminatorVirtuoso extends Card {

    public IlluminatorVirtuoso() {
        addTrigger(Triggers.becomesTargetOfYourSpell(), new DrawDiscardAndConniveEffect());
    }
}
