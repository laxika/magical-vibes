package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.GreatestCreatureTypeCountAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "122")
public class BasaltRavager extends Card {

    public BasaltRavager() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToAnyTargetEffect(new GreatestCreatureTypeCountAmongControlled()));
    }
}
