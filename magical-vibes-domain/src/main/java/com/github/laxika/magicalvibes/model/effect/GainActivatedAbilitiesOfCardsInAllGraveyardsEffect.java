package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Static self-effect: the source gains all activated abilities of cards of the requested type in
 * all graveyards, mana abilities included.
 */
public record GainActivatedAbilitiesOfCardsInAllGraveyardsEffect(CardType cardType) implements CardEffect {
}
