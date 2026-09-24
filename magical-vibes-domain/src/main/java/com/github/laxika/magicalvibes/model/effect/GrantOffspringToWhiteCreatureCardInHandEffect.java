package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution-time choice that permanently gives a white creature card in the controller's hand
 * an offspring ability with the supplied cost.
 */
public record GrantOffspringToWhiteCreatureCardInHandEffect(String offspringCost) implements CardEffect {
}
