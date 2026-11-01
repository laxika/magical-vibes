package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SelfWasDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "95")
public class BurningEyeZubera extends Card {

    public BurningEyeZubera() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ConditionalEffect(
                new SelfWasDealtDamageThisTurn(4), new DealDamageToAnyTargetEffect(3)));
    }
}
