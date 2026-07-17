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
 */
public record EyeForAnEyeReflection(UUID targetPlayerId, int amount, Card eyeCard, UUID eyeControllerId) {
}
