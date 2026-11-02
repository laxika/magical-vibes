package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceDamageToControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLS", collectorNumber = "9")
public class LashknifeBarrier extends Card {

    public LashknifeBarrier() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));
        addStaticEffect(new ReduceDamageToControlledCreaturesEffect(1));
    }
}
