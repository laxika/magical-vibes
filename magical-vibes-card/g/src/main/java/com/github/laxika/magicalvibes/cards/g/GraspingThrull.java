package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "177")
public class GraspingThrull extends Card {

    public GraspingThrull() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                DealDamageEffect.to(Subjects.eachOpponent(), 2),
                new GainLifeEffect(2));
    }
}
