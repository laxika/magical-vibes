package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution effect for Sunbird's Invocation's triggered ability.
 * Reveal the top X cards of the controller's library, where X is the mana value
 * of the triggering spell. The controller may cast a spell with mana value X or less
 * from among the revealed cards without paying its mana cost. The rest go to the
 * bottom of the library in a random order.
 *
 * @param manaValue the mana value of the spell that triggered this ability (determines X)
 */
public record SunbirdsInvocationRevealAndCastEffect(int manaValue) implements CardEffect {
}
