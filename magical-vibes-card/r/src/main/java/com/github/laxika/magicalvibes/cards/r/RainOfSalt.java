package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "POR", collectorNumber = "147")
@CardRegistration(set = "USG", collectorNumber = "206")
public class RainOfSalt extends Card {

    public RainOfSalt() {
        // Destroy two target lands. Both are destroyed by one instruction, so they are destroyed simultaneously.
        target(new PermanentPredicateTargetFilter(new PermanentIsLandPredicate(), "Targets must be lands"), 2, 2)
                .addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
