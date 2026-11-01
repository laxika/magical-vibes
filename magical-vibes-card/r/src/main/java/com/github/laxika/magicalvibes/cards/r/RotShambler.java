package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "187")
@CardRegistration(set = "CMM", collectorNumber = "318")
public class RotShambler extends Card {

    public RotShambler() {
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
