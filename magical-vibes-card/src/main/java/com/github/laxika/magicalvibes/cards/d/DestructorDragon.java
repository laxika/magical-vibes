package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "FRF", collectorNumber = "127")
public class DestructorDragon extends Card {

    public DestructorDragon() {
        // When this creature dies, destroy target noncreature permanent.
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                "Target must be a noncreature permanent"
        )).addEffect(EffectSlot.ON_DEATH, new DestroyTargetPermanentEffect());
    }
}
