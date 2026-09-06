package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creatures whose effective power is strictly less than the effective power of the
 * permanent that imposed the filter (Champion of Lambholt: "Creatures with power less than this
 * creature's power can't block creatures you control"). The source's last-known information is
 * used when it has left the battlefield before the comparison is made.
 *
 * <p>Needs {@code gameData} and {@code sourceCardId} on the {@link FilterContext}.
 */
public record PermanentPowerLessThanSourcePowerPredicate() implements PermanentPredicate {
}
