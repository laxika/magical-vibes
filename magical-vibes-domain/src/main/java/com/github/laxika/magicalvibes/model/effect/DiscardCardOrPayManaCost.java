package com.github.laxika.magicalvibes.model.effect;

/**
 * Additional cast cost: "discard a card or pay {manaCost}" (e.g. Lightning Axe's "{5}").
 * Exactly one option is paid — either a hand card via {@code PlayCardRequest.discardHandCardIndex},
 * or the listed mana on top of the spell's normal mana cost. Satisfiable with another card in hand
 * or with enough mana for the combined cost.
 */
public record DiscardCardOrPayManaCost(String manaCost) implements CostEffect {
}
