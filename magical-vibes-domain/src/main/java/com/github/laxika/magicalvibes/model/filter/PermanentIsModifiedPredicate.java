package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that has a counter, is equipped, or is enchanted by an Aura controlled by
 * the permanent's controller. When evaluated against a permanent that has left the battlefield,
 * the controller is taken from {@link FilterContext#sourceControllerId()}.
 */
public record PermanentIsModifiedPredicate() implements PermanentPredicate {
}
