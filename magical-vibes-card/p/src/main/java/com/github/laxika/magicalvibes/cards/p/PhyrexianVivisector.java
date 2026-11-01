package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "100")
public class PhyrexianVivisector extends Card {

    public PhyrexianVivisector() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), new ScryEffect(1));
    }
}
