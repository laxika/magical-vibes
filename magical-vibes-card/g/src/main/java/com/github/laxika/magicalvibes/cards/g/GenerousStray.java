package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "129")
@CardRegistration(set = "ANB", collectorNumber = "95")
public class GenerousStray extends Card {

    public GenerousStray() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
    }
}
