package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A one-shot damage reflection shield (Eye for an Eye): the next time the chosen source would deal
 * damage to the protected player this turn, that source still deals that much damage to the player
 * <em>and</em> Eye for an Eye deals that much damage to that source's controller. The shield is
 * consumed on first use.
 *
 * <p>Unlike a prevention shield this does NOT reduce the damage dealt to the protected player; it
 * only schedules an equal reflected damage event (see {@link EyeForAnEyeReflection}) back at the
 * source's controller, dealt by {@code eyeCard} under {@code eyeControllerId}'s control.
 *
 * @param protectedPlayerId the player Eye for an Eye's controller protected (its controller)
 * @param sourceId          the chosen source (a permanent, or a spell on the stack by its card id)
 * @param eyeCard           the Eye for an Eye card that deals the reflected damage
 * @param eyeControllerId   the controller of the reflected damage
 * @param sourceControllerId the source's controller when it was chosen; used when the source has
 *                          left the battlefield (or was a spell) by the time the damage is dealt
 */
public record EyeForAnEyeShield(UUID protectedPlayerId, UUID sourceId, Card eyeCard, UUID eyeControllerId,
                                UUID sourceControllerId) {
}
