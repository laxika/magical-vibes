package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * "At the beginning of each of that player's upkeeps, if that card is exiled, remove a delay
 * counter from it. If the card has no delay counters on it, the player puts it onto the stack as a
 * copy of the original spell." (Ertai's Meddling)
 *
 * <p>Pushed onto the stack by {@code StepTriggerService} for every exiled card still carrying delay
 * counters whose controller is the active player. Putting the card back onto the stack is not
 * casting it, so cast triggers do not fire.</p>
 *
 * @param cardId the exiled card carrying the delay counters
 */
public record RemoveDelayCounterFromExiledSpellEffect(UUID cardId) implements CardEffect {
}
