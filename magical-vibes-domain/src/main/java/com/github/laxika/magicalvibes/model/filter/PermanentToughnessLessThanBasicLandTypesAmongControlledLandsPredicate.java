package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose effective toughness is less than the number of distinct basic land
 * types among lands controlled by the source's controller.
 */
public record PermanentToughnessLessThanBasicLandTypesAmongControlledLandsPredicate()
        implements PermanentPredicate {
}
