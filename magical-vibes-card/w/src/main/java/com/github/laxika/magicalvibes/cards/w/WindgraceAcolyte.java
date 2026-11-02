package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "112")
public class WindgraceAcolyte extends Card {

    public WindgraceAcolyte() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(3, MillRecipient.CONTROLLER));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
    }
}
