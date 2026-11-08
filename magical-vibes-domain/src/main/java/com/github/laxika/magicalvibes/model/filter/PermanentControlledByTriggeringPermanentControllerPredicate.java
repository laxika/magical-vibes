package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents controlled by the controller of the permanent the triggered ability's event was about
 * ({@link FilterContext#triggeringPermanentId()}): wrapped in a {@link PermanentNotPredicate}, Confusion in the
 * Ranks' "target permanent another player controls" chosen by the entering permanent's controller. Once that permanent
 * has left the battlefield it reads its last-known controller, {@link FilterContext#triggeringPermanentControllerId()}
 * (Necrotic Plague's "creature one of their opponents controls", CR 603.10a), and matches nothing when none was given.
 */
public record PermanentControlledByTriggeringPermanentControllerPredicate() implements PermanentPredicate {
}
