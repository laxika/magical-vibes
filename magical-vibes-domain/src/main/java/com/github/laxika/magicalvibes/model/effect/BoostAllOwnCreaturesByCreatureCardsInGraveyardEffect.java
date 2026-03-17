package com.github.laxika.magicalvibes.model.effect;

/**
 * Creatures you control get +X/+X until end of turn, where X is the number
 * of creature cards in your graveyard at resolution time.
 *
 * <p>Used by Garruk, the Veil-Cursed's −3 loyalty ability (combined with
 * a {@link GrantKeywordEffect} for trample).</p>
 */
public record BoostAllOwnCreaturesByCreatureCardsInGraveyardEffect() implements CardEffect {
}
