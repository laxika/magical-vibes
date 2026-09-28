package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an attacking creature whose direct player attack target has more life than that
 * creature's controller.
 */
public record PermanentAttacksPlayerWithMoreLifeThanControllerPredicate() implements PermanentPredicate {
}
