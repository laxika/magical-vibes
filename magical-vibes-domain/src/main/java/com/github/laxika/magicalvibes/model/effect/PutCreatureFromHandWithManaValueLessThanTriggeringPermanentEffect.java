package com.github.laxika.magicalvibes.model.effect;

/**
 * Lets the controller put a creature card from their hand onto the battlefield tapped and
 * attacking when its mana value is less than the triggering permanent's mana value.
 */
public record PutCreatureFromHandWithManaValueLessThanTriggeringPermanentEffect()
        implements TriggeringPermanentManaValueEffect {
}
