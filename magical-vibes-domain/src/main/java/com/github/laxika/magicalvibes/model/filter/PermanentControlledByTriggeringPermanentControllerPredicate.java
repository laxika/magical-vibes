package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents controlled by the controller of the permanent the triggered ability's event was about
 * ({@link FilterContext#triggeringPermanentId()}): wrapped in a {@link PermanentNotPredicate}, Confusion in the
 * Ranks' "target permanent another player controls" chosen by the entering permanent's controller. Matches nothing
 * while that permanent isn't on the battlefield.
 */
public record PermanentControlledByTriggeringPermanentControllerPredicate() implements PermanentPredicate {
}
