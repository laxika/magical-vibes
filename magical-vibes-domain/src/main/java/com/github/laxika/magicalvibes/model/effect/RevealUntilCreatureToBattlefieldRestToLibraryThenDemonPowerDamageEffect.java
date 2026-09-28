package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards until a creature card is found, puts it onto the battlefield, shuffles the other
 * revealed cards into the library, and has it deal damage equal to its power to each opponent if
 * it is a Demon.
 */
public record RevealUntilCreatureToBattlefieldRestToLibraryThenDemonPowerDamageEffect()
        implements CardEffect {
}
