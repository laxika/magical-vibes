package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Mills cards from the controller's library, then the target player loses life equal to the total
 * mana value of the cards that were actually milled.
 */
public record MillControllerAndTargetPlayerLosesLifeByMilledManaValueEffect(DynamicAmount count)
        implements CardEffect {

    public MillControllerAndTargetPlayerLosesLifeByMilledManaValueEffect(int count) {
        this(new Fixed(count));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
