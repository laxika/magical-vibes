package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Exiles the top card of the controller's library, tracks it with the source permanent, and lets
 * that controller play it for as long as the source remains on the battlefield under the same
 * control and {@code permissionCondition} holds when the play is attempted (Pick Up the Pace:
 * "You may play cards exiled with this enchantment as long as you attacked this turn").
 *
 * <p>The exile still happens when the source has already left the battlefield, but then no play
 * permission is granted because the card is no longer exiled with an object that can be found.
 *
 * @param permissionCondition condition that must hold for the play permission to be usable
 */
public record ExileTopCardToSourceAndMayPlayWhileConditionEffect(Condition permissionCondition)
        implements CardEffect {
}
