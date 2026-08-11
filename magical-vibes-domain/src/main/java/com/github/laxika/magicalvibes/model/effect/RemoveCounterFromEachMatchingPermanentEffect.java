package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Removes up to {@code amount} counters of {@code counterType} from each permanent matching
 * {@code predicate} across the battlefield selected by {@code scope}.
 *
 * <p>The {@link EachPermanentScope#TARGET_PLAYER} form reads a non-targeting player reference from
 * the stack entry. Step-trigger collection supplies the active player for effects that resolve
 * during each player's upkeep.</p>
 */
public record RemoveCounterFromEachMatchingPermanentEffect(
        CounterType counterType,
        int amount,
        PermanentPredicate predicate,
        EachPermanentScope scope
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
