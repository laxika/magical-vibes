package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "123")
public class DynamiteDiver extends Card {

    public DynamiteDiver() {
        addStaticEffect(new PowerBoostForCrewAndSaddleEffect(2));
        addTrigger(Triggers.dies(ObjectRelation.THIS), DealDamageEffect.toAnyTarget(1));
    }
}
