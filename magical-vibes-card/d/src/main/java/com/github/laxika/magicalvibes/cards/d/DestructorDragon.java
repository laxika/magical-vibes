package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "127")
public class DestructorDragon extends Card {

    public DestructorDragon() {
        // When this creature dies, destroy target noncreature permanent.
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                "Target must be a noncreature permanent"
        )).addTrigger(Triggers.dies(ObjectRelation.THIS), new DestroyTargetPermanentEffect());
    }
}
