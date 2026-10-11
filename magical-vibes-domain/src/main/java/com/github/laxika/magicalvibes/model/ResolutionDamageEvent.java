package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * The damage event a resolving spell or ability is in. Each effect is its own event, except that
 * the steps of one simultaneous sequence share one.
 *
 * @param stackObjectId the resolving stack object's targetable id
 * @param event         {@link StackEntry#getResolvingDamageEvent()} for the effect being dispatched
 */
public record ResolutionDamageEvent(UUID stackObjectId, int event) {
}
