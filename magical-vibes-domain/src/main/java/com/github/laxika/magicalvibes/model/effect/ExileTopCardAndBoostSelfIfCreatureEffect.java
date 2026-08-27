package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the controller's library. If it is a creature card, the source permanent
 * gets +X/+Y until end of turn, where X and Y are that card's power and toughness.
 */
public record ExileTopCardAndBoostSelfIfCreatureEffect() implements CardEffect {
}
