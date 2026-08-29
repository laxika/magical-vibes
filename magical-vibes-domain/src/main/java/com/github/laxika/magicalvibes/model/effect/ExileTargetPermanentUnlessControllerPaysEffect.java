package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Exiles the target creature unless its controller pays the evaluated mana amount.
 *
 * @param manaAmount the amount the target creature's controller may pay
 */
public record ExileTargetPermanentUnlessControllerPaysEffect(DynamicAmount manaAmount)
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
