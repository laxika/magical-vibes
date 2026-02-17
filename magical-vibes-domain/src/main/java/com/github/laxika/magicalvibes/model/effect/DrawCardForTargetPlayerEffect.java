package com.github.laxika.magicalvibes.model.effect;

/**
 * Draws cards for the player stored in the stack entry's targetPermanentId field
 * (typically the active player whose draw/upkeep step triggered the ability).
 *
 * @param amount                 number of cards to draw
 * @param requireSourceUntapped  if true, the source permanent (via sourcePermanentId)
 *                               must still be untapped at resolution time (intervening-if)
 */
public record DrawCardForTargetPlayerEffect(int amount, boolean requireSourceUntapped) implements CardEffect {

    public DrawCardForTargetPlayerEffect(int amount) {
        this(amount, false);
    }
}
