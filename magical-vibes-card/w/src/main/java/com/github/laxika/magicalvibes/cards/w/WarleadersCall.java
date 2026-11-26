package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "242")
@CardRegistration(set = "MKM", collectorNumber = "315")
@CardRegistration(set = "MAR", collectorNumber = "96")
public class WarleadersCall extends Card {

    public WarleadersCall() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.creaturesYouControl()));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                DealDamageEffect.to(Subjects.eachOpponent(), 1));
    }
}
