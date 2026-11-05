package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives a returned creature permanent the suspected designation: the stack entry's triggering card on a death
 * trigger (Presumed Dead's "return it ... and suspect it"), else the spell's target (It Doesn't Add Up).
 */
public record SuspectReturnedPermanentEffect() implements CardEffect {
}
