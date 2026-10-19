package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * "All [from]s are [to]s. (We'll errata this to be true.)" Once the source is revealed in any zone,
 * every creature with the {@code from} creature type is a {@code to} instead for the rest of the
 * match, even after the source has left the battlefield. This is errata, not a type-changing effect
 * (Ruff, Underdog Champ). The engine records it on the game when the source is on the battlefield
 * and replaces the creature type in the layer system.
 *
 * @param from the creature type that no longer exists
 * @param to   the creature type that replaces it
 */
public record ErrataCreatureTypeForMatchEffect(CardSubtype from, CardSubtype to) implements CardEffect {
}
