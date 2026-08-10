package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an opponent who controls strictly more lands than the evaluating controller.
 * The comparison is checked both when the target is chosen and when the ability resolves.
 */
public record PlayerControlsMoreLandsThanControllerPredicate() implements PlayerPredicate {
}
