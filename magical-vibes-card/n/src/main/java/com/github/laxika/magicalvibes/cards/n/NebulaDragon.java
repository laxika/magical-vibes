package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "147")
public class NebulaDragon extends Card {

    public NebulaDragon() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), DealDamageEffect.toAnyTarget(3));
    }
}
