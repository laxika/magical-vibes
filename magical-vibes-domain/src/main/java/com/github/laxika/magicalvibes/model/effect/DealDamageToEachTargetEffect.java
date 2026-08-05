package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals damage to each of multiple targets (creatures and/or players). The amount is a
 * {@link DynamicAmount} evaluated once at resolution and each target receives the full
 * amount (not divided). Uses {@code entry.getTargetIds()} for targets.
 * Used by Jaya's Immolating Inferno ("deals X damage to each of up to three targets").
 *
 * <p>An optional {@code filter} restricts which of the targeted permanents actually take
 * damage — targeted players and permanents not matching the filter are skipped. Used by
 * Winter Blast ("deals 2 damage to each of those creatures with flying") where the same
 * target group is also tapped.
 */
public record DealDamageToEachTargetEffect(DynamicAmount damage, PermanentPredicate filter) implements CardEffect {

    public DealDamageToEachTargetEffect(DynamicAmount damage) {
        this(damage, null);
    }

    /**
     * "Each of up to N targets" is "any target" (CR 115.4): a creature, player or planeswalker,
     * never another permanent type. Harmful, so protection from the source blocks targeting
     * (CR 702.16b) — {@link #filter} is a resolution-time filter over the already-chosen targets
     * (Winter Blast's "with flying"), never a targeting restriction.
     */
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.ANY_TARGET);
    }
}
