package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * One-shot prevention: the next time {@code sourceId} would deal damage to any target this turn, that
 * damage is prevented and the shield is consumed (Sanctum Guardian).
 *
 * <p>When {@code damageRedSourceController} is true, if the prevented damage came from a red source,
 * {@code passageCard} deals that much damage to the source's controller (Honorable Passage).
 *
 * <p>When {@code recipientId} is non-null the shield only fires for damage dealt to that one
 * recipient (Kithkin Armor's enchanted creature); a null recipient protects any target.
 *
 * @param sourceId                   the chosen damage source permanent
 * @param damageRedSourceController  Honorable Passage rider: reflect prevented red damage
 * @param passageCard                the Honorable Passage card dealing the reflected damage; null when
 *                                   the rider is off
 * @param passageControllerId        controller of Honorable Passage; null when the rider is off
 * @param recipientId                the only permanent/player this shield protects; null = any target
 * @param damageMultiplier           what the next damage event becomes: {@code 0} prevents it (every
 *                                   prevention shield), {@code 2} doubles it instead (Desperate
 *                                   Gambit's won flip). A non-zero multiplier is a replacement, not
 *                                   prevention, so it applies even while damage can't be prevented
 * @param lifeGainPlayerId           player who gains life equal to the prevented damage; null when
 *                                   the shield has no life-gain rider
 * @param combatOnly                 whether this shield only applies to combat damage
 */
public record SourceNextDamageToAnyTargetShield(
        UUID sourceId,
        boolean damageRedSourceController,
        Card passageCard,
        UUID passageControllerId,
        UUID recipientId,
        int damageMultiplier,
        UUID lifeGainPlayerId,
        boolean combatOnly) {

    public SourceNextDamageToAnyTargetShield(UUID sourceId, boolean damageRedSourceController, Card passageCard,
                                             UUID passageControllerId, UUID recipientId, int damageMultiplier,
                                             UUID lifeGainPlayerId) {
        this(sourceId, damageRedSourceController, passageCard, passageControllerId, recipientId,
                damageMultiplier, lifeGainPlayerId, false);
    }

    /** Sanctum Guardian / Circle of Despair: prevention only, no rider. */
    public SourceNextDamageToAnyTargetShield(UUID sourceId) {
        this(sourceId, false, null, null, null, 0, null);
    }

    /** Honorable Passage: any-target prevention plus the red-source rider. */
    public SourceNextDamageToAnyTargetShield(UUID sourceId, boolean damageRedSourceController, Card passageCard,
                                             UUID passageControllerId) {
        this(sourceId, damageRedSourceController, passageCard, passageControllerId, null, 0, null);
    }

    /** Kithkin Armor: prevention limited to a single protected recipient. */
    public static SourceNextDamageToAnyTargetShield forRecipient(UUID sourceId, UUID recipientId) {
        return new SourceNextDamageToAnyTargetShield(sourceId, false, null, null, recipientId, 0, null);
    }

    /** Awe Strike: source-wide prevention plus life gain for the spell's controller. */
    public static SourceNextDamageToAnyTargetShield withLifeGain(UUID sourceId, UUID lifeGainPlayerId) {
        return new SourceNextDamageToAnyTargetShield(sourceId, false, null, null, null, 0, lifeGainPlayerId);
    }

    /** Desperate Gambit's won flip: the next damage event from this source is doubled, not prevented. */
    public static SourceNextDamageToAnyTargetShield doubling(UUID sourceId) {
        return new SourceNextDamageToAnyTargetShield(sourceId, false, null, null, null, 2, null);
    }

    /** Impulsive Maneuvers's won flip: the next combat damage event from this source is doubled. */
    public static SourceNextDamageToAnyTargetShield combatDoubling(UUID sourceId) {
        return new SourceNextDamageToAnyTargetShield(sourceId, false, null, null, null, 2, null, true);
    }

    /** Impulsive Maneuvers's lost flip: the next combat damage event from this source is prevented. */
    public static SourceNextDamageToAnyTargetShield combatPrevention(UUID sourceId) {
        return new SourceNextDamageToAnyTargetShield(sourceId, false, null, null, null, 0, null, true);
    }
}
