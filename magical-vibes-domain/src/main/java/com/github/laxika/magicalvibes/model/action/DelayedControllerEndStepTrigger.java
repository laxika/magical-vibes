package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * Delayed ability for "at the beginning of your next end step" wording: it is processed only at the
 * end step of {@code controllerId} and skips every other player's end step. It otherwise behaves like
 * {@link DelayedEndStepTrigger} and goes on the stack as a non-targeting triggered ability.
 *
 * @param controllerId      the player whose next end step fires the ability
 * @param sourceCard        the card whose ability scheduled it
 * @param sourcePermanentId the permanent whose ability scheduled it
 * @param effect            the effect to resolve
 */
public record DelayedControllerEndStepTrigger(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                              CardEffect effect)
        implements DelayedAction {
}
