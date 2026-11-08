package com.github.laxika.magicalvibes.model.effect;

/**
 * Looks at the top X cards of a library, where X is the triggering spell's mana value, and offers a spell with mana
 * value less than X for a free cast. X is read as the ability resolves from the stack entry's triggering spell: the
 * live spell while it is on the stack, else as it last existed there.
 */
public record KioraSovereignOfTheDeepRevealAndCastEffect() implements CardEffect {
}
