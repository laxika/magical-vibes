package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "134")
public class AkoumBoulderfoot extends Card {

    public AkoumBoulderfoot() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), DealDamageEffect.toAnyTarget(1));
    }
}
