package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * One unresolved Mystic Reflection replacement. The copied values are the target's copiable values as
 * of the moment the replacement applies (CR 707.2), so a target that was changed after the spell
 * resolved is read from the battlefield; once it has left, its last-known permanent supplies them.
 *
 * @param targetPermanentId  the chosen creature
 * @param lastKnownTargetCard the target's card when the spell resolved, used if nothing fresher exists
 * @param targetPermanent    the target permanent object, which keeps its last copiable values after leaving
 */
public record PendingMysticReflection(UUID targetPermanentId, Card lastKnownTargetCard,
                                      Permanent targetPermanent) {
}
