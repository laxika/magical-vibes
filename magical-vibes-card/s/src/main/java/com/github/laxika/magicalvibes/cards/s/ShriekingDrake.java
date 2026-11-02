package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "43")
public class ShriekingDrake extends Card {

    public ShriekingDrake() {
        // When this creature enters, return a creature you control to its owner's hand.
        // Non-targeting choice at resolution; may (and alone, must) return itself.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReturnPermanentControlledByPlayerToHandEffect(
                new PermanentIsCreaturePredicate(),
                "creature"
        ));
    }
}
