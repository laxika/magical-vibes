package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "POR", collectorNumber = "147")
public class RainOfSalt extends Card {

    public RainOfSalt() {
        // Destroy two target lands.
        // Two distinct target groups (shared targets not allowed) so the two
        // lands must differ, each destroyed by its own effect.
        target(landFilter("First target must be a land"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        target(landFilter("Second target must be a land"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }

    private static PermanentPredicateTargetFilter landFilter(String description) {
        return new PermanentPredicateTargetFilter(new PermanentIsLandPredicate(), description);
    }
}
