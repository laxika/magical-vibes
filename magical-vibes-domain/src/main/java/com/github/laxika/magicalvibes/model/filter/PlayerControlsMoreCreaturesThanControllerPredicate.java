package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an opponent who controls strictly more creatures than the evaluating controller.
 * The comparison is checked when the target is chosen; resolution rechecks only the opponent
 * relationship.
 */
public record PlayerControlsMoreCreaturesThanControllerPredicate() implements PlayerPredicate {
}
