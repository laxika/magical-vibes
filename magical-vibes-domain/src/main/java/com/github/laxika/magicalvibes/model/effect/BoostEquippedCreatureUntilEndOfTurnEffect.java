package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * "Equipped creature or a creature you control matching ... gets +X/+Y until end of turn" (Hand of Vecna): as it
 * resolves, the candidates are the creature the source is attached to and the controller's creatures matching
 * {@code orControlledCreatureMatching} (each counted once), and the controller chooses one when there are several.
 * The amounts are evaluated with the chosen creature as the amount source. Without an attached creature the source
 * can still boost a matching creature, which is how a token that copied the trigger works. A boost of the attached
 * creature alone is {@link BoostEffect#of} with {@link Subjects#attached()}.
 */
public record BoostEquippedCreatureUntilEndOfTurnEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost,
        PermanentPredicate orControlledCreatureMatching
) implements CardEffect {

    public BoostEquippedCreatureUntilEndOfTurnEffect {
        Objects.requireNonNull(orControlledCreatureMatching, "orControlledCreatureMatching");
    }

    @Override
    public boolean hasAbilityResolutionCondition() {
        return powerBoost.requiresAbilityResolutionCount()
                || toughnessBoost.requiresAbilityResolutionCount();
    }
}
