package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyOtherCreaturesSharingColorWithEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "125")
public class SpreadingPlague extends Card {

    public SpreadingPlague() {
        addTrigger(Triggers.enters(new PermanentIsCreaturePredicate(), ObjectRelation.ANY),
                new DestroyOtherCreaturesSharingColorWithEnteringCreatureEffect());
    }
}
