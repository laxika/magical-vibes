package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "158")
public class TunnelingGeopede extends Card {

    public TunnelingGeopede() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                DealDamageEffect.to(Subjects.eachOpponent(), 1));
    }
}
