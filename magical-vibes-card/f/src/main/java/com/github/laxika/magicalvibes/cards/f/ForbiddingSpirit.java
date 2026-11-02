package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "9")
@CardRegistration(set = "GN3", collectorNumber = "10")
public class ForbiddingSpirit extends Card {

    public ForbiddingSpirit() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RequirePaymentToAttackUntilNextTurnEffect(2));
    }
}
