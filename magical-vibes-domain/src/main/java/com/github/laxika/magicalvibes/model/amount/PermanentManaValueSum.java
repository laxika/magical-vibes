package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The sum of the mana values of battlefield permanents matching {@code filter} within {@code scope}.
 */
public record PermanentManaValueSum(PermanentPredicate filter, CountScope scope) implements DynamicAmount {
}
