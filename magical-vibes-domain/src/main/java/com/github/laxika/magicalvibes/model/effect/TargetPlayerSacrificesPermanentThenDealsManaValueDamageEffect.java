package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Target player sacrifices a permanent matching the filter of their choice, then takes damage
 * equal to that permanent's mana value.
 *
 * <p>The mana value is captured before the permanent leaves the battlefield.
 */
public record TargetPlayerSacrificesPermanentThenDealsManaValueDamageEffect(
        PermanentPredicate filter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
