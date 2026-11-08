package com.github.laxika.magicalvibes.model.effect;

/**
 * Lets the controller put a creature card from their hand onto the battlefield tapped and attacking when its mana value
 * is less than the triggering permanent's mana value (Arni Metalbrow), read as the effect resolves: the stack entry's
 * triggering permanent as it is now, else as it last existed on the battlefield (CR 608.2h).
 */
public record PutCreatureFromHandWithManaValueLessThanTriggeringPermanentEffect() implements CardEffect {
}
