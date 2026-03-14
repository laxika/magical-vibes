package com.github.laxika.magicalvibes.model.effect;

/**
 * Sphinx Ambassador's combat damage trigger: search the damaged player's library for a card,
 * then that player chooses a card name. If you searched for a creature card that doesn't have
 * that name, you may put it onto the battlefield under your control. Then that player shuffles.
 */
public record SphinxAmbassadorEffect() implements CardEffect {
}
