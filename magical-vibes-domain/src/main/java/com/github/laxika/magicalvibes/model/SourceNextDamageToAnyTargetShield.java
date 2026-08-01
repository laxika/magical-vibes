package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * One-shot prevention: the next time {@code sourceId} would deal damage to any target this turn, that
 * damage is prevented and the shield is consumed (Sanctum Guardian).
 *
 * <p>When {@code damageRedSourceController} is true, if the prevented damage came from a red source,
 * {@code passageCard} deals that much damage to the source's controller (Honorable Passage).
 *
 * @param sourceId                   the chosen damage source permanent
 * @param damageRedSourceController  Honorable Passage rider: reflect prevented red damage
 * @param passageCard                the Honorable Passage card dealing the reflected damage; null when
 *                                   the rider is off
 * @param passageControllerId        controller of Honorable Passage; null when the rider is off
 */
public record SourceNextDamageToAnyTargetShield(
        UUID sourceId,
        boolean damageRedSourceController,
        Card passageCard,
        UUID passageControllerId) {

    /** Sanctum Guardian / Circle of Despair: prevention only, no rider. */
    public SourceNextDamageToAnyTargetShield(UUID sourceId) {
        this(sourceId, false, null, null);
    }
}
