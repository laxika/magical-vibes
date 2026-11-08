package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution effect for Sunbird's Invocation's triggered ability.
 * Reveal the top X cards of the controller's library, where X is the mana value
 * of the triggering spell. The controller may cast a spell with mana value X or less
 * from among the revealed cards without paying its mana cost. The rest go to the
 * bottom of the library in a random order.
 *
 * <p>X is read as the ability resolves from the stack entry's triggering spell: the live spell while it is on the
 * stack, else as it last existed there.
 */
public record SunbirdsInvocationRevealAndCastEffect() implements CardEffect {
}
