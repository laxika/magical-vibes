package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.UUID;

/**
 * Capability for symmetric static effects that reduce the generic cost of every targeted spell
 * and activated ability. The reduction applies once per spell or ability, not once per target.
 */
public interface TargetedCostReducingEffect extends ActivatedAbilityCostReducingEffect {

    int amount();

    @Override
    default PermanentPredicate affectedPermanents() {
        return new PermanentTruePredicate();
    }

    @Override
    default int genericCostReduction() {
        return amount();
    }

    @Override
    default boolean appliesTo(ActivatedAbility ability) {
        return false;
    }

    @Override
    default boolean appliesTo(ActivatedAbility ability, UUID reducingPermanentId,
                             UUID targetId, List<UUID> targetIds) {
        return targetId != null || targetIds != null && !targetIds.isEmpty();
    }

    @Override
    default boolean appliesSymmetrically() {
        return true;
    }
}
