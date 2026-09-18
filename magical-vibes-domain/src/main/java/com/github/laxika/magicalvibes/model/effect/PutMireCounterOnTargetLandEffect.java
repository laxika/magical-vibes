package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/** "Put a mire counter on target non-Swamp land." */
public record PutMireCounterOnTargetLandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.land(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SWAMP)));
    }
}
