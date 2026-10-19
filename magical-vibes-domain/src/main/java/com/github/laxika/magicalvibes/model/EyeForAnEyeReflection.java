package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A pending Eye for an Eye reflected damage event, populated when an {@link EyeForAnEyeShield} matches
 * and consumed by the damage-dealing services. The reflected damage is dealt to {@code targetPlayerId}
 * (the chosen source's controller) by {@code eyeCard} under {@code eyeControllerId}'s control.
 *
 * @param targetPlayerId  the source's controller, who takes the reflected damage
 * @param amount          the reflected damage amount
 * @param eyeCard         the Eye for an Eye card dealing the reflected damage
 * @param eyeControllerId the controller of the reflected damage
 * @param sourcePermanentId the permanent that deals the reflected damage when it is the original damage
 *                        source itself (Reflect Damage), or {@code null} when {@code eyeCard} deals it
 */
public record EyeForAnEyeReflection(UUID targetPlayerId, int amount, Card eyeCard, UUID eyeControllerId,
                                    UUID sourcePermanentId) {

    public EyeForAnEyeReflection(UUID targetPlayerId, int amount, Card eyeCard, UUID eyeControllerId) {
        this(targetPlayerId, amount, eyeCard, eyeControllerId, null);
    }
}
