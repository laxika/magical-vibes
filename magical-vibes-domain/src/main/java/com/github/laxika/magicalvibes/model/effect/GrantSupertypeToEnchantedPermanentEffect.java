package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

/**
 * Static effect: the enchanted permanent gains the specified supertype.
 * E.g. In Bolas's Clutches — "Enchanted permanent is legendary."
 */
public record GrantSupertypeToEnchantedPermanentEffect(CardSupertype supertype) implements CardEffect {
}
