package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "6")
@CardRegistration(set = "CMD", collectorNumber = "13")
public class FalseProphet extends Card {

    public FalseProphet() {
        // When this creature dies, exile all creatures.
        addTrigger(Triggers.dies(ObjectRelation.THIS), ExileTargetPermanentEffect.all(new PermanentIsCreaturePredicate()));
    }
}
