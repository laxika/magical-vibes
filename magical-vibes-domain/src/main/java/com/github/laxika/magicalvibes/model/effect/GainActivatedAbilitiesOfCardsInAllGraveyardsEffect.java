package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Static self-effect: the source gains all activated abilities of cards of the requested type in
 * all graveyards. Tap-for-mana abilities stored in the {@code ON_TAP} slot are included by the
 * static handler as equivalent activated abilities.
 */
public record GainActivatedAbilitiesOfCardsInAllGraveyardsEffect(CardType cardType) implements CardEffect {
}
